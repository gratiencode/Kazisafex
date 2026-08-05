package com.endeleya.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

public class GratienExpenseWorkflow {

    private static final String OLLAMA_BASE_URL = AiAgents.OLLAMA_BASE_URL;
    private static final String MODEL_NAME = AiAgents.MODEL_NAME;

    private final ChatModel model = OllamaChatModel.builder()
            .baseUrl(OLLAMA_BASE_URL)
            .modelName(MODEL_NAME)
            .temperature(0.0)
            .timeout(Duration.ofMinutes(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExpenseAgentRunner expenseAgentRunner;

    {
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
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
        if (looksLikeInfoQuery(value)) {
            return false;
        }
        boolean expenseText = value.contains("depense") || value.contains("dépense")
                || value.contains("recu") || value.contains("reçu")
                || value.contains("ticket") || value.contains("note de frais");
        boolean expenseImage = attachments != null && attachments.stream().anyMatch(ImageAttachment::isImage)
                && (value.isBlank() || expenseText);
        return expenseText || expenseImage;
    }

    public String handle(String question, List<File> attachments) {
        if (pendingDraft != null && awaitingConfirmation) {
            if (!isConfirmation(question)) {
                // Un rejet peut porter une correction plutot qu'une annulation
                // (ex: "non c'est 500 USD carburant"). On tente une relecture; a defaut
                // d'une depense exploitable, on annule comme avant.
                ExpenseDraft corrected = question != null && question.chars().anyMatch(Character::isDigit)
                        ? extractDraft(question, null)
                        : null;
                if (corrected != null && corrected.isUsable()) {
                    pendingDraft = corrected;
                    return confirmationPrompt(corrected);
                }
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
        if (attachments != null && attachments.stream().anyMatch(ImageAttachment::isImage)) {
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
        Double amount = parseLeadingAmount(value);
        String currency = detectCurrency(value);
        String rawName = cells.length > 0 ? cells[0] : value;
        String name = cleanExpenseName(rawName, amount, currency);
        draft.setExpenseName(name.isBlank() ? inferExpenseName(value) : name);
        if (cells.length > 1) {
            Double cellAmount = parseDouble(cells[1], null);
            if (cellAmount != null) {
                draft.setAmount(cellAmount);
            }
        }
        if (draft.getAmount() == null) {
            draft.setAmount(amount);
        }
        if (cells.length > 2 && looksLikeCurrency(cells[2])) {
            draft.setCurrency(cells[2].trim().toUpperCase(Locale.ROOT));
        }
        if (draft.getCurrency() == null) {
            draft.setCurrency(currency);
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

    private Double parseLeadingAmount(String value) {
        String text = value == null ? "" : value;
        // 1) Un montant est presque toujours colle a la devise (USD/CDF/$/FC/francs):
        //    on prefere ce nombre a n'importe quelle quantite ou date du texte.
        java.util.regex.Matcher nearCurrency = java.util.regex.Pattern
                .compile("(?:^|[^0-9])(\\d+(?:[.,]\\d+)?)\\s*(?:USD|CDF|\\$|FC|FRANC|FRANCS)")
                .matcher(text.toUpperCase(Locale.ROOT));
        if (nearCurrency.find()) {
            try {
                return Double.parseDouble(nearCurrency.group(1).replace(",", "."));
            } catch (NumberFormatException ignored) {
            }
        }
        java.util.regex.Matcher currencyFirst = java.util.regex.Pattern
                .compile("(?:USD|CDF|\\$|FC|FRANC|FRANCS)\\s*(?:de\\s+)?(\\d+(?:[.,]\\d+)?)")
                .matcher(text.toUpperCase(Locale.ROOT));
        if (currencyFirst.find()) {
            try {
                return Double.parseDouble(currencyFirst.group(1).replace(",", "."));
            } catch (NumberFormatException ignored) {
            }
        }
        // 2) Sans devise, on prend le plus grand nombre: c'est generalement le montant,
        //    pas une quantite ni une date (ex: "carburant 3 cartons 20000" -> 20000).
        Double max = null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\d+(?:[.,]\\d+)?")
                .matcher(text);
        while (matcher.find()) {
            try {
                double number = Double.parseDouble(matcher.group().replace(",", "."));
                if (max == null || number > max) {
                    max = number;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return max;
    }

    private String detectCurrency(String value) {
        String upper = (value == null ? "" : value).toUpperCase(Locale.ROOT);
        if (upper.contains("USD") || upper.contains("DOLLAR") || upper.contains("$")) {
            return "USD";
        }
        if (upper.contains("CDF") || upper.contains("FRANC") || upper.contains(" FC")) {
            return "CDF";
        }
        return null;
    }

    private boolean looksLikeCurrency(String value) {
        String upper = value == null ? "" : value.toUpperCase(Locale.ROOT).trim();
        return upper.equals("USD") || upper.equals("CDF") || upper.contains("USD") || upper.contains("CDF")
                || upper.contains("DOLLAR") || upper.contains("FRANC") || upper.contains("$");
    }

    private String cleanExpenseName(String raw, Double amount, String currency) {
        if (raw == null) {
            return "";
        }
        String name = raw
                .replace("dépense", " ").replace("depense", " ")
                .replace("enregistre", " ").replace("enregistrer", " ").replace("ajouter", " ")
                .replace("une", " ").replace("un", " ").replace("la", " ").replace("le", " ")
                .replace("les", " ").replace("de", " ").replace("du", " ").replace("des", " ")
                .replace("pour", " ").replace("à", " ")
                .replace("non", " ").replace("c'est", " ").replace("plutot", " ").replace("plutôt", " ")
                .replace("à la place", " ").replace("au lieu", " ").replace("corrige", " ").replace("corriger", " ");
        if (amount != null) {
            String dot = String.valueOf(amount).replace(',', '.');
            String comma = dot.replace('.', ',');
            long whole = Math.round(amount);
            name = name.replace(dot, " ").replace(comma, " ")
                    .replace(String.valueOf(whole), " ");
        }
        if (currency != null) {
            name = name.replace(currency, " ").replace(currency.toLowerCase(Locale.ROOT), " ")
                    .replace("usd", " ").replace("cdf", " ").replace("$", " ");
        }
        return name.replaceAll("[\\s,]+", " ").trim();
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

    private boolean looksLikeInfoQuery(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.contains("combien")
                || value.contains("liste")
                || value.contains("montre")
                || value.contains("affiche")
                || value.contains("rapport")
                || value.contains("resume")
                || value.contains("résumé")
                || value.contains("etat")
                || value.contains("état")
                || value.contains("statistique")
                || value.contains("total des")
                || value.contains("le plus")
                || value.contains("la plus")
                || value.contains("eleve")
                || value.contains("élevé")
                || value.contains("semaine")
                || value.contains("du mois")
                || value.contains("de ce mois")
                || value.contains("de l'année")
                || value.contains("de l'annee")
                || value.contains("historique")
                || value.contains("synthese")
                || value.contains("synthèse")
                || value.contains("bilan");
    }

    public void clearPendingWorkflow() {
        pendingDraft = null;
        awaitingConfirmation = false;
    }

    private Content imageContent(File file) {
        return ImageAttachment.imageContent(file);
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
