package com.endeleya.ia;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class GratienAssistantClient {

    private static final String OLLAMA_BASE_URL = AiAgents.OLLAMA_BASE_URL;
    private static final String MODEL_NAME = AiAgents.MODEL_NAME;
    private static final int MAX_FILE_CHARS = 18_000;
    private static final int VISION_RETRY_ATTEMPTS = 1;
    private static final GratienAssistantClient INSTANCE = new GratienAssistantClient();
    private static final String AGENT_DIR = java.nio.file.Paths.get(
            System.getProperty("user.dir"), "Media", "ia", "gratien").toString();
    private static final java.io.File AGENT_FILE = new java.io.File(AGENT_DIR, "AGENT.md");
    private static final java.io.File USER_FILE = new java.io.File(AGENT_DIR, "USER.md");

    private final StreamingChatModel model;
    // La vision (photos jointes) doit passer par un appel NON-streaming: le serveur
    // Ollama distant renvoie un 500 (Internal Server Error ref:...) des qu'une image
    // est envoyee en streaming (stream:true), alors que stream:false fonctionne.
    private final ChatModel visionModel;
    // AiAgents orchestre les factures et expose les GratienTools partages.
    private final AiAgents aiAgents = AiAgents.getInstance();

    public interface StreamCallback {

        void onToken(String token);

        void onComplete();

        void onError(Throwable error);

        default void onProcess(String message) {
        }
    }

    private GratienAssistantClient() {
        model = OllamaStreamingChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName(MODEL_NAME)
                .temperature(0.25)
                .timeout(Duration.ofMinutes(5))
                .build();
        visionModel = OllamaChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName(MODEL_NAME)
                .temperature(0.25)
                .timeout(Duration.ofMinutes(5))
                .build();
    }

    public static GratienAssistantClient getInstance() {
        return INSTANCE;
    }

    public void stream(String question, List<File> attachments,String entreprise, StreamCallback callback) {
        aiAgents.startForCurrentSession();
        // Le compactage de memoire et le mode swarm (sous-agents) signalent leur execution via le canal de progression du chat.
        aiAgents.setCompactionSignal(callback::onProcess);
        aiAgents.setProgressSignal(callback::onProcess);
        if (question != null && question.trim().toLowerCase(Locale.ROOT).startsWith("/kanuni ")) {
            String instruction = question.trim().substring(8).strip();
            String result = saveUserInstruction(instruction);
            callback.onToken(result + "\n\n" + loadAgentContext());
            callback.onComplete();
            return;
        }
        if (aiAgents.hasPendingInvoiceIntentClarification()) {
            // Une facture/recu a ete joint sans intention claire; la reponse utilisateur choisit le workflow.
            callback.onToken(aiAgents.resolveInvoiceIntentClarification(question));
            callback.onComplete();
            return;
        }
        if (aiAgents.hasPendingWorkflowCancellationRequest()) {
            // Une confirmation d'annulation est en attente: la reponse oui/non doit rejoindre
            // l'agent d'annulation avant tout workflow pour ne jamais etre avalee ailleurs.
            callback.onToken(aiAgents.orchestrateWorkflowCancellation(question));
            callback.onComplete();
            return;
        }
        if (aiAgents.shouldHandleProductImage(question, attachments)) {
            // Image simple de produits: Gratien lit les articles puis demande quantites/prix avant approvisionnement generique.
            callback.onToken(aiAgents.orchestrateProductImage(question, attachments));
            callback.onComplete();
            return;
        }
        if (aiAgents.shouldClarifyInvoiceIntent(question, attachments)) {
            // Avant toute lecture de facture ambigue, Gratien demande si c'est une entree, une sortie ou une depense.
            callback.onToken(aiAgents.askInvoiceIntentClarification(question, attachments));
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
        if (aiAgents.shouldHandleWorkflowCancellation(question)) {
            // L'annulation a son agent dedie afin de demander confirmation avant toute interruption.
            callback.onToken(aiAgents.orchestrateWorkflowCancellation(question));
            callback.onComplete();
            return;
        }
        String context = loadAgentContext();
        String contextualized = context.isEmpty() ? question : question + "\n\nContexte:\n" + context;

        if (attachments == null || attachments.isEmpty()) {
            // Chat texte: proxy LangChain4j avec GratienTools, memoire et function calling.
            aiAgents.streamGeneral(contextualized, entreprise, callback::onToken, callback::onProcess,
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

        ChatRequest request = buildRequest(contextualized, entreprise, attachments);
        // La vision ne peut pas streame vers ce serveur (500 sur image en streaming):
        // on fait un appel chat() non-streaming, avec une nouvelle tentative si le serveur echoue.
        ChatResponse response = null;
        Throwable lastError = null;
        for (int attempt = 0; attempt <= VISION_RETRY_ATTEMPTS; attempt++) {
            try {
                response = visionModel.chat(request);
                break;
            } catch (Exception ex) {
                lastError = ex;
                if (attempt < VISION_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        if (response == null || response.aiMessage() == null || response.aiMessage().text() == null) {
            callback.onError(lastError == null
                    ? new IllegalStateException("Reponse vide du modele de vision pour les pieces jointes.")
                    : lastError);
            return;
        }
        aiAgents.appendMemory("user",
                question == null || question.isBlank() ? "[pièce jointe]" : question);
        aiAgents.appendMemory("assistant", response.aiMessage().text());
        callback.onToken(response.aiMessage().text());
        callback.onComplete();
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
        if (requestsQuarterlyYear(value)) {
            int anchorYear = requestedAnchorYear(question);
            if (value.contains("excel") || value.contains("xlsx")) {
                return aiAgents.getGratienTools().generateFinancialStatementsQuarterlyExcel(anchorYear, null);
            }
            if (value.contains("pdf") || value.contains("télécharge") || value.contains("telecharge")
                    || value.contains("download")) {
                return aiAgents.getGratienTools().generateFinancialStatementsQuarterlyPdf(anchorYear, null);
            }
        }
        int yearlySpan = requestedYearSpan(value);
        if (yearlySpan > 0) {
            int anchorYear = requestedAnchorYear(question);
            if (value.contains("excel") || value.contains("xlsx")) {
                return aiAgents.getGratienTools().generateFinancialStatementsYearlyExcel(anchorYear, yearlySpan, null);
            }
            if (value.contains("pdf") || value.contains("télécharge") || value.contains("telecharge")
                    || value.contains("download")) {
                return aiAgents.getGratienTools().generateFinancialStatementsYearlyPdf(anchorYear, yearlySpan, null);
            }
        }
        LocalDateRange range = LocalDateRange.from(question);
        if (value.contains("excel") || value.contains("xlsx")) {
            return aiAgents.getGratienTools().generateFinancialStatementsExcel(range.start(), range.end(), null);
        }
        if (value.contains("pdf") || value.contains("télécharge") || value.contains("telecharge")
                || value.contains("download")) {
            return aiAgents.getGratienTools().generateFinancialStatementsPdf(range.start(), range.end(), null);
        }
        return null;
    }

    private boolean requestsQuarterlyYear(String value) {
        return value != null && (value.contains("trimestre") || value.contains("trimestriel")
                || value.contains("trimestrielle") || value.contains("quarter"));
    }

    private int requestedYearSpan(String value) {
        if (value == null) {
            return 0;
        }
        if (value.contains("5 ans") || value.contains("cinq ans") || value.contains("5 années")
                || value.contains("cinq années")) {
            return 5;
        }
        if (value.contains("3 ans") || value.contains("trois ans") || value.contains("3 années")
                || value.contains("trois années")) {
            return 3;
        }
        return 0;
    }

    private int requestedAnchorYear(String question) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\b(20\\d{2})\\b")
                .matcher(question == null ? "" : question);
        int year = java.time.LocalDate.now().getYear();
        while (matcher.find()) {
            year = Integer.parseInt(matcher.group(1));
        }
        return year;
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
                Integer year = findYear(text);
                first = year == null ? today.withDayOfMonth(1).toString()
                        : java.time.LocalDate.of(year, 1, 1).toString();
            }
            if (second == null) {
                Integer year = findYear(text);
                second = year == null ? today.toString()
                        : java.time.LocalDate.of(year, 12, 31).toString();
            }
            return new LocalDateRange(first, second);
        }

        private static Integer findYear(String text) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("\\b(20\\d{2})\\b")
                    .matcher(text == null ? "" : text);
            Integer year = null;
            while (matcher.find()) {
                year = Integer.parseInt(matcher.group(1));
            }
            return year;
        }
    }

    public static String loadAgentContext() {
        StringBuilder ctx = new StringBuilder();
        if (AGENT_FILE.exists()) {
            try {
                ctx.append(Files.readString(AGENT_FILE.toPath(), StandardCharsets.UTF_8)).append("\n\n");
            } catch (IOException ignored) {}
        }
        if (USER_FILE.exists()) {
            try {
                ctx.append("## Instructions personnalisees de l'utilisateur\n")
                        .append(Files.readString(USER_FILE.toPath(), StandardCharsets.UTF_8)).append("\n\n");
            } catch (IOException ignored) {}
        }
        return ctx.toString().strip();
    }

    public static String saveUserInstruction(String instruction) {
        try {
            java.io.File dir = new java.io.File(AGENT_DIR);
            if (!dir.exists()) dir.mkdirs();
            String existing = "";
            if (USER_FILE.exists()) {
                existing = Files.readString(USER_FILE.toPath(), StandardCharsets.UTF_8).strip();
            }
            String updated = existing.isEmpty() ? instruction : existing + "\n" + instruction;
            Files.writeString(USER_FILE.toPath(), updated, StandardCharsets.UTF_8);
            return "Instruction enregistree dans USER.md.";
        } catch (IOException e) {
            return "Erreur lors de l'enregistrement: " + e.getMessage();
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
                        Tu es Gratien, l'assistant de Kazisafe. Réponds dans la langue de l'utilisateur, de maniere utile et structuré.
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
        // Compression commune (sous ~700 ko) via ImageAttachment pour rester sous la
        // limite de taille du corps de requete du serveur Ollama (HTTP 413 sinon).
        return ImageAttachment.imageContent(file);
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
