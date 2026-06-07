package com.endeleya.ia;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class JemimaAssistantClient {

    private static final String OLLAMA_BASE_URL = AiAgents.OLLAMA_BASE_URL;
    private static final String MODEL_NAME = System.getProperty(
            "kazisafex.ai.model",
            System.getenv().getOrDefault("AI_MODEL", AiAgents.getSpeedModel()));
    private static final int MAX_FILE_CHARS = 18_000;
    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final JemimaAssistantClient INSTANCE = new JemimaAssistantClient();

    private final StreamingChatModel model;
    // AiAgents orchestre les factures et expose les JemimaTools partages.
    private final AiAgents aiAgents = AiAgents.getInstance();

    public interface StreamCallback {

        void onToken(String token);

        void onComplete();

        void onError(Throwable error);

        default void onProcess(String message) {
        }
    }

    private JemimaAssistantClient() {
        model = OllamaStreamingChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName(MODEL_NAME)
                .temperature(0.25)
                .timeout(Duration.ofMinutes(5))
                .build();
    }

    public static JemimaAssistantClient getInstance() {
        return INSTANCE;
    }

    public void stream(String question, List<File> attachments,String entreprise, StreamCallback callback) {
        aiAgents.startForCurrentSession();
        if (aiAgents.shouldHandleWorkflowCancellation(question)) {
            // L'annulation a son agent dedie afin de demander confirmation avant toute interruption.
            callback.onToken(aiAgents.orchestrateWorkflowCancellation(question));
            callback.onComplete();
            return;
        }
        if (aiAgents.shouldHandleExpense(question, attachments)) {
            // Les depenses lues depuis reçu/facture passent par leur workflow specialise.
            callback.onToken(aiAgents.orchestrateExpense(question, attachments));
            callback.onComplete();
            return;
        }
        if (aiAgents.shouldHandleInvoice(question, attachments)) {
            // Les factures quittent le chat generique pour passer par le workflow agentique.
            callback.onToken(aiAgents.orchestrateInvoice(question, attachments));
            callback.onComplete();
            return;
        }
        if (aiAgents.shouldHandleSale(question, attachments)) {
            // Les ventes/sorties passent par leur workflow agentique specialise.
            callback.onToken(aiAgents.orchestrateSale(question, attachments));
            callback.onComplete();
            return;
        }
        if (attachments == null || attachments.isEmpty()) {
            // Chat texte: proxy LangChain4j avec JemimaTools, memoire et function calling.
            aiAgents.streamGeneral(question, entreprise, callback::onToken, callback::onProcess,
                    callback::onComplete, callback::onError);
            return;
        }

        String toolResult = tryLocalTool(question);
        if (toolResult != null) {
            aiAgents.appendMemory("user", question);
            aiAgents.appendMemory("assistant", toolResult);
            callback.onToken(toolResult);
            callback.onComplete();
            return;
        }

        model.chat(buildRequest(question, entreprise,attachments), new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                callback.onToken(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                callback.onComplete();
            }

            @Override
            public void onError(Throwable error) {
                callback.onError(error);
            }
        });
    }

    private String tryLocalTool(String question) {
        if (question == null) {
            return null;
        }
        String value = question.toLowerCase(Locale.ROOT);
        if (!(value.contains("état financier") || value.contains("etat financier")
                || value.contains("bilan") || value.contains("compte de résultat")
                || value.contains("compte de resultat"))) {
            return null;
        }
        LocalDateRange range = LocalDateRange.from(question);
        if (value.contains("excel") || value.contains("xlsx")) {
            return aiAgents.getJemimaTools().generateFinancialStatementsExcel(range.start(), range.end(), null);
        }
        if (value.contains("pdf") || value.contains("télécharge") || value.contains("telecharge")
                || value.contains("download")) {
            return aiAgents.getJemimaTools().generateFinancialStatementsPdf(range.start(), range.end(), null);
        }
        return null;
    }

    private record LocalDateRange(String start, String end) {

        static LocalDateRange from(String text) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(20\\d{2}-\\d{2}-\\d{2})")
                    .matcher(text == null ? "" : text);
            String first = null;
            String second = null;
            if (matcher.find()) {
                first = matcher.group(1);
            }
            if (matcher.find()) {
                second = matcher.group(1);
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            if (first == null) {
                first = today.withDayOfMonth(1).toString();
            }
            if (second == null) {
                second = today.toString();
            }
            return new LocalDateRange(first, second);
        }
    }

    public static String attachmentSummary(List<File> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "";
        }
        return "\n\nPièces jointes: " + attachments.stream()
                .map(file -> file.getName() + " (" + readableSize(file.length()) + ")")
                .collect(Collectors.joining(", "));
    }

    private ChatRequest buildRequest(String question,String entreprise, List<File> attachments) {
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(buildPrompt(question, attachments)));
        if (attachments != null && !attachments.isEmpty()) {
            for (File file : attachments) {
                Content image = imageContent(file);
                if (image != null) {
                    contents.add(image);
                }
            }
        }
        String sm=new StringBuilder()
                .append("""
                        Tu es Jemima, l'assistant de Kazisafe. Réponds dans la langue de l'utilisateur, de maniere utile et structuré.
                        Tu aide l'utilisateur dans ses taches quotidiennes au sein de leur entreprise
                        """)
                .append(entreprise)
                .append("""
                        Quand la réponse contient des données tabulaires, utilise un tableau Markdown valide.
                        """)
                .toString();
        return ChatRequest.builder()
                .messages(SystemMessage.from(sm),UserMessage.from(contents))
                .build();
    }

    private String buildPrompt(String question, List<File> attachments) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Question utilisateur:");
        prompt.append(question == null ? "" : question.trim());

        if (attachments != null && !attachments.isEmpty()) {
            prompt.append("\n\nFichiers joints fournis par l'utilisateur:\n");
            for (File file : attachments) {
                prompt.append(readAttachmentForPrompt(file)).append("\n");
            }
        }
        return prompt.toString();
    }

    private String readAttachmentForPrompt(File file) {
        if (file == null) {
            return "- Fichier invalide.";
        }
        StringBuilder info = new StringBuilder();
        info.append("- ").append(file.getName())
                .append(" (").append(readableSize(file.length())).append(")");
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (isImage(contentType)) {
                return info.append(": image jointe et transmise au modèle multimodal.").toString();
            }
            if (!isReadableText(file, contentType)) {
                return info.append(": fichier binaire ou image; utilise son nom comme contexte.").toString();
            }
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            if (content.length() > MAX_FILE_CHARS) {
                content = content.substring(0, MAX_FILE_CHARS) + "\n...[contenu tronqué]";
            }
            return info.append("\n```text\n").append(content).append("\n```").toString();
        } catch (IOException | RuntimeException ex) {
            return info.append(": impossible de lire le contenu (").append(ex.getMessage()).append(").").toString();
        }
    }

    private Content imageContent(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (!isImage(contentType) || Files.size(file.toPath()) > MAX_IMAGE_BYTES) {
                return null;
            }
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            Image image = Image.builder()
                    .base64Data(base64)
                    .mimeType(contentType)
                    .build();
            return ImageContent.from(image);
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private boolean isReadableText(File file, String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("text/")) {
            return true;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".txt")
                || name.endsWith(".csv")
                || name.endsWith(".json")
                || name.endsWith(".xml")
                || name.endsWith(".md")
                || name.endsWith(".log")
                || name.endsWith(".sql")
                || name.endsWith(".java")
                || name.endsWith(".properties");
    }

    private boolean isImage(String contentType) {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/");
    }

    private static String readableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " o";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.ROOT, "%.1f Ko", kb);
        }
        return String.format(Locale.ROOT, "%.1f Mo", kb / 1024.0);
    }
}
