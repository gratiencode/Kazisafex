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
import java.util.Map;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

public class GratienExpenseWorkflow {

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
    private final ExpenseAgentRunner expenseAgentRunner;
    private ExpenseDraft pendingDraft;
    private boolean awaitingConfirmation;

    public GratienExpenseWorkflow(GratienTools tools, ExpenseAgentRunner expenseAgentRunner) {
        GratienTools usedTools = tools == null ? new GratienTools() : tools;
        this.expenseAgentRunner = expenseAgentRunner == null ? usedTools::insertExpenseOutput : expenseAgentRunner;
    }

    public boolean shouldHandle(String question, List<File> attachments) {
        if (pendingDraft != null && awaitingConfirmation) {
            return true;
        }
        String value = question == null ? "" : question.toLowerCase(Locale.ROOT);
        boolean expenseText = value.contains("depense") || value.contains("dépense")
                || value.contains("recu") || value.contains("reçu")
                || value.contains("ticket") || value.contains("note de frais");
        boolean expenseImage = attachments != null && attachments.stream().anyMatch(this::isImage)
                && (value.isBlank() || expenseText);
        return expenseText || expenseImage;
    }

    public String handle(String question, List<File> attachments) {
        if (pendingDraft != null && awaitingConfirmation) {
            if (!isConfirmation(question)) {
                ExpenseDraft rejected = pendingDraft;
                pendingDraft = null;
                awaitingConfirmation = false;
                return "Enregistrement annulé. Aucune dépense n'a été créée pour "
                        + nullToDash(rejected.getExpenseName()) + ".";
            }
            ExpenseDraft draft = pendingDraft;
            pendingDraft = null;
            awaitingConfirmation = false;
            return expenseAgentRunner.run(draft);
        }

        ExpenseDraft draft = extractDraft(question, attachments);
        if (draft == null || !draft.isUsable()) {
            return "Je n'ai pas pu lire une dépense complète. Envoyez une image plus lisible ou précisez: nom dépense, montant, devise, compte et date.";
        }
        pendingDraft = draft;
        awaitingConfirmation = true;
        return confirmationPrompt(draft);
    }

    private String confirmationPrompt(ExpenseDraft draft) {
        return "J'ai lu cette dépense sur le reçu/facture. Confirmez avant enregistrement:\n\n"
                + "|Champ|Valeur|\n"
                + "|---|---|\n"
                + "|Référence|" + tableCell(nullToDash(draft.getReference())) + "|\n"
                + "|Date|" + tableCell(nullToDash(draft.getExpenseDate())) + "|\n"
                + "|Dépense|" + tableCell(nullToDash(draft.getExpenseName())) + "|\n"
                + "|Imputation|" + tableCell(nullToDash(draft.getImputation())) + "|\n"
                + "|Motif|" + tableCell(nullToDash(draft.getDescription())) + "|\n"
                + "|Montant|" + tableCell(draft.getAmount() + " " + nullToDash(draft.getCurrency())) + "|\n"
                + "|Compte|" + tableCell(nullToDash(draft.getAccountName())) + "|\n"
                + "|Type compte|" + tableCell(nullToDash(draft.getAccountType())) + "|\n\n"
                + "Répondez `oui` pour enregistrer cette dépense, ou `non` pour annuler.";
    }

    private ExpenseDraft extractDraft(String question, List<File> attachments) {
        if (attachments != null && attachments.stream().anyMatch(this::isImage)) {
            ExpenseDraft draft = extractDraftFromImages(attachments);
            if (draft != null && draft.isUsable()) {
                return draft;
            }
        }
        return extractDraftFromText(question);
    }

    private ExpenseDraft extractDraftFromImages(List<File> attachments) {
        try {
            List<Content> contents = new ArrayList<>();
            contents.add(TextContent.from("""
                    Lis cette image comme une facture ou un reçu de dépense.
                    Retourne uniquement un JSON valide:
                    {
                      "reference": "numero du recu/facture si visible sinon null",
                      "expenseDate": "yyyy-MM-dd ou null",
                      "expenseName": "categorie courte deduite par connaissance generale, ex: carburant, transport, loyer, internet",
                      "imputation": "fonction/service concerné si visible sinon GENERAL",
                      "description": "motif clair généré depuis l'analyse du reçu ou de la facture",
                      "currency": "USD ou CDF",
                      "amount": 0,
                      "accountName": "caisse, banque ou mobile money si visible sinon null",
                      "accountType": "CAISSE, BANQUE ou ELECTRONIQUE si identifiable sinon CAISSE"
                    }
                    Si plusieurs totaux existent, prends le total à payer.
                    """));
            for (File file : attachments) {
                Content image = imageContent(file);
                if (image != null) {
                    contents.add(image);
                }
            }
            ChatRequest request = ChatRequest.builder().messages(UserMessage.from(contents)).build();
            String answer = model.chat(request).aiMessage().text();
            return enrichDraft(mapper.readValue(extractJson(answer), ExpenseDraft.class));
        } catch (Exception ex) {
            return null;
        }
    }

    private ExpenseDraft extractDraftFromText(String question) {
        ExpenseDraft draft = new ExpenseDraft();
        String value = question == null ? "" : question.trim();
        if (value.isBlank()) {
            return draft;
        }
        String[] cells = value.split(",");
        draft.setExpenseName(cells.length > 0 ? cleanExpenseWord(cells[0]) : "Dépense");
        if (cells.length > 1) {
            draft.setAmount(parseDouble(cells[1], null));
        }
        if (cells.length > 2) {
            draft.setCurrency(cells[2].trim().toUpperCase(Locale.ROOT));
        }
        if (cells.length > 3) {
            draft.setDescription(cells[3].trim());
        } else {
            draft.setDescription(value);
        }
        draft.setImputation("GENERAL");
        draft.setAccountType("CAISSE");
        return enrichDraft(draft);
    }

    private String cleanExpenseWord(String value) {
        return value == null ? "" : value.replace("dépense", "").replace("depense", "").trim();
    }

    private ExpenseDraft enrichDraft(ExpenseDraft draft) {
        if (draft == null) {
            return null;
        }
        String source = (nullToDash(draft.getExpenseName()) + " "
                + nullToDash(draft.getDescription()) + " "
                + nullToDash(draft.getReference())).toLowerCase(Locale.ROOT);
        if (draft.getExpenseName() == null || draft.getExpenseName().isBlank()
                || draft.getExpenseName().equalsIgnoreCase("dépense")) {
            draft.setExpenseName(inferExpenseName(source));
        }
        if (draft.getDescription() == null || draft.getDescription().isBlank()
                || draft.getDescription().equalsIgnoreCase("-")) {
            draft.setDescription(generateExpenseMotif(draft));
        }
        if (draft.getImputation() == null || draft.getImputation().isBlank()) {
            draft.setImputation("GENERAL");
        }
        if (draft.getCurrency() == null || draft.getCurrency().isBlank()) {
            draft.setCurrency("USD");
        }
        if (draft.getAccountType() == null || draft.getAccountType().isBlank()) {
            draft.setAccountType("CAISSE");
        }
        return draft;
    }

    private String inferExpenseName(String text) {
        String value = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (value.contains("fuel") || value.contains("carburant") || value.contains("essence") || value.contains("gasoil")) {
            return "Carburant";
        }
        if (value.contains("transport") || value.contains("taxi") || value.contains("bus") || value.contains("parking")) {
            return "Transport";
        }
        if (value.contains("internet") || value.contains("data") || value.contains("wifi") || value.contains("telecom")) {
            return "Internet et communication";
        }
        if (value.contains("loyer") || value.contains("rent")) {
            return "Loyer";
        }
        if (value.contains("electric") || value.contains("courant") || value.contains("eau")) {
            return "Eau et électricité";
        }
        if (value.contains("papier") || value.contains("stylo") || value.contains("fourniture")) {
            return "Fournitures de bureau";
        }
        if (value.contains("repas") || value.contains("restaurant") || value.contains("food")) {
            return "Restauration";
        }
        return "Dépense générale";
    }

    private String generateExpenseMotif(ExpenseDraft draft) {
        String name = nullToDash(draft.getExpenseName());
        String reference = draft.getReference() == null || draft.getReference().isBlank() ? "" : " - référence " + draft.getReference();
        String date = draft.getExpenseDate() == null || draft.getExpenseDate().isBlank() ? "" : " du " + draft.getExpenseDate();
        return "Paiement " + name + date + reference;
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

    private Double parseDouble(String value, Double fallback) {
        try {
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String tableCell(String value) {
        return nullToDash(value).replace("|", "\\|").replace("\n", " ");
    }

    public static class ExpenseWorkflowState extends AgentState {

        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                "step", Channels.appender(ArrayList::new)
        );

        public ExpenseWorkflowState(Map<String, Object> initData) {
            super(initData);
        }
    }
}
