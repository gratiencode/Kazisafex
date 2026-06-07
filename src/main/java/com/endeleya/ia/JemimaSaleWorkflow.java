package com.endeleya.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import java.util.Map;

public class JemimaSaleWorkflow {

    private static final String OLLAMA_BASE_URL = AiAgents.OLLAMA_BASE_URL;
    private static final String MODEL_NAME = System.getProperty(
            "kazisafex.ai.model",
            System.getenv().getOrDefault("AI_MODEL", AiAgents.getSpeedModel()));

    private final ChatModel model = OllamaChatModel.builder()
            .baseUrl(OLLAMA_BASE_URL)
            .modelName(MODEL_NAME)
            .temperature(0.0)
            .timeout(Duration.ofMinutes(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final JemimaTools tools;
    private final SaleAgentRunner saleAgentRunner;
    private SaleDraft pendingDraft;
    private boolean awaitingQuantityConfirmation;
    private boolean awaitingClient;

    public JemimaSaleWorkflow(JemimaTools tools, SaleAgentRunner saleAgentRunner) {
        this.tools = tools == null ? new JemimaTools() : tools;
        this.saleAgentRunner = saleAgentRunner == null ? this.tools::insertSaleOutput : saleAgentRunner;
    }

    public boolean shouldHandle(String question, List<File> attachments) {
        if (pendingDraft != null && (awaitingQuantityConfirmation || awaitingClient || isConfirmation(question))) {
            return true;
        }
        String value = question == null ? "" : question.toLowerCase(Locale.ROOT);
        boolean saleText = value.contains("vente") || value.contains("sortie") || value.contains("vendre")
                || value.contains("client");
        boolean saleImage = attachments != null && attachments.stream().anyMatch(this::isImage)
                && (value.isBlank() || saleText);
        return saleText || saleImage;
    }

    public String handle(String question, List<File> attachments) {
        if (pendingDraft != null && awaitingQuantityConfirmation) {
            if (!isConfirmation(question)) {
                applyQuantityCorrections(question, pendingDraft);
                return quantityConfirmationPrompt(pendingDraft);
            }
            awaitingQuantityConfirmation = false;
            awaitingClient = true;
            return "Indiquez le client au format: nom, téléphone. Si vous voulez utiliser le client anonyme, répondez simplement *anonyme*.";
        }
        if (pendingDraft != null && awaitingClient) {
            applyClient(question, pendingDraft);
            awaitingClient = false;
            String result = saleAgentRunner.run(pendingDraft);
            pendingDraft = null;
            return result;
        }

        SaleDraft draft = extractDraft(question, attachments);
        if (draft == null || !draft.hasLines()) {
            return "Je n'ai pas trouvé d'articles à sortir. Donnez la liste des produits avec les quantités, ou joignez une image plus lisible.";
        }
        pendingDraft = draft;
        awaitingQuantityConfirmation = true;
        return quantityConfirmationPrompt(draft);
    }

    private String quantityConfirmationPrompt(SaleDraft draft) {
        StringBuilder builder = new StringBuilder("Confirmez les quantités pour cette sortie:\n\n");
        for (int i = 0; i < draft.getLines().size(); i++) {
            SaleLine line = draft.getLines().get(i);
            builder.append(i + 1).append(". ")
                    .append(nullToDash(line.getProductName()))
                    .append(" | quantité: ").append(line.getQuantity())
                    .append(line.getSalePrice() == null ? "" : " | prix: " + line.getSalePrice())
                    .append("\n");
        }
        builder.append("""

                Répondez *oui* si tout est correct.
                Pour corriger, envoyez une ou plusieurs lignes au format:
                numero, quantite
                """);
        return builder.toString();
    }

    private SaleDraft extractDraft(String question, List<File> attachments) {
        if (attachments != null && attachments.stream().anyMatch(this::isImage)) {
            SaleDraft draft = extractDraftFromImages(attachments);
            if (draft != null && draft.hasLines()) {
                return draft;
            }
        }
        return extractDraftFromText(question);
    }

    private SaleDraft extractDraftFromText(String question) {
        SaleDraft draft = new SaleDraft();
        if (question == null || question.isBlank()) {
            return draft;
        }
        for (String row : question.split("\\R|;")) {
            String cleaned = row.trim();
            if (cleaned.isBlank()) {
                continue;
            }
            String[] cells = cleaned.split(",");
            SaleLine line = new SaleLine();
            if (cells.length >= 2 && parseDouble(cells[1], -1) > 0) {
                line.setProductName(cells[0].trim());
                line.setQuantity(parseDouble(cells[1], 1d));
                if (cells.length >= 3) {
                    line.setSalePrice(parseDouble(cells[2], 0d));
                }
            } else {
                line.setProductName(cleaned.replace("vente", "").replace("sortie", "").trim());
                line.setQuantity(1d);
            }
            if (line.getProductName() != null && !line.getProductName().isBlank()) {
                draft.getLines().add(line);
            }
        }
        return draft;
    }

    private SaleDraft extractDraftFromImages(List<File> attachments) {
        try {
            List<Content> contents = new ArrayList<>();
            contents.add(TextContent.from("""
                    Lis cette image comme une liste de produits pour une vente/sortie.
                    Retourne uniquement un JSON valide:
                    {
                      "saleDate": "yyyy-MM-dd ou null",
                      "clientName": "nom client si visible sinon null",
                      "clientPhone": "telephone si visible sinon null",
                      "currency": "USD ou CDF",
                      "lines": [
                        {"productName": "nom produit", "quantity": 1, "salePrice": null, "measureName": "Pièce"}
                      ]
                    }
                    Ne crée pas d'article si le nom n'est pas lisible.
                    """));
            for (File file : attachments) {
                Content image = imageContent(file);
                if (image != null) {
                    contents.add(image);
                }
            }
            ChatRequest request = ChatRequest.builder().messages(UserMessage.from(contents)).build();
            String answer = model.chat(request).aiMessage().text();
            return mapper.readValue(extractJson(answer), SaleDraft.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private void applyQuantityCorrections(String text, SaleDraft draft) {
        for (String row : text.split("\\R")) {
            String[] cells = row.split(",");
            if (cells.length < 2) {
                continue;
            }
            int index = (int) parseDouble(cells[0], 0) - 1;
            if (index >= 0 && index < draft.getLines().size()) {
                draft.getLines().get(index).setQuantity(parseDouble(cells[1], draft.getLines().get(index).getQuantity()));
                if (cells.length >= 3) {
                    draft.getLines().get(index).setSalePrice(parseDouble(cells[2], 0d));
                }
            }
        }
    }

    private void applyClient(String text, SaleDraft draft) {
        String value = text == null ? "" : text.trim();
        if (value.isBlank() || value.equalsIgnoreCase("anonyme")) {
            draft.setClientName("Anonyme");
            return;
        }
        String[] cells = value.split(",");
        draft.setClientName(cells[0].trim());
        if (cells.length >= 2) {
            draft.setClientPhone(cells[1].trim());
        }
    }

    private boolean isConfirmation(String question) {
        String value = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        return value.equals("oui") || value.equals("ok") || value.equals("confirme") || value.equals("valider");
    }

    private boolean isImage(File file) {
        String name = file == null ? "" : file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp");
    }

    private Content imageContent(File file) throws Exception {
        if (!isImage(file)) {
            return null;
        }
        String mime = file.getName().toLowerCase(Locale.ROOT).endsWith(".png") ? "image/png" : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        return ImageContent.from(Image.builder().base64Data(base64).mimeType(mime).build());
    }

    private String extractJson(String answer) {
        int start = answer == null ? -1 : answer.indexOf('{');
        int end = answer == null ? -1 : answer.lastIndexOf('}');
        return start >= 0 && end > start ? answer.substring(start, end + 1) : "{}";
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public static class SaleWorkflowState extends AgentState {

        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                "step", Channels.appender(ArrayList::new)
        );

        public SaleWorkflowState(Map<String, Object> initData) {
            super(initData);
        }
    }
}
