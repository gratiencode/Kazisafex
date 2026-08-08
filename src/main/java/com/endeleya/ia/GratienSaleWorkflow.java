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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class GratienSaleWorkflow {

    private static final String OLLAMA_BASE_URL = AiAgents.OLLAMA_BASE_URL;
    private static final String VISION_MODEL_NAME = AiAgents.VISION_MODEL_NAME;
    private static final String TEXT_MODEL_NAME = AiAgents.MODEL_NAME;

    private final ChatModel model = OllamaChatModel.builder()
            .baseUrl(OLLAMA_BASE_URL)
            .modelName(TEXT_MODEL_NAME)
            .temperature(0.0)
            .timeout(Duration.ofMinutes(5))
            .build();
    private final OllamaModelFallback imageModel = new OllamaModelFallback(0.0, Duration.ofMinutes(5),
            VISION_MODEL_NAME, TEXT_MODEL_NAME);
    private final ObjectMapper mapper = new ObjectMapper();
    private final GratienTools tools;
    private final SaleAgentRunner saleAgentRunner;

    {
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private SaleDraft pendingDraft;
    private boolean awaitingConfirmation;
    private boolean awaitingModification;
    private boolean awaitingPaymentType;
    private boolean awaitingCurrency;
    private boolean awaitingDueDate;
    private boolean awaitingClientInfo;
    private boolean awaitingPartialPayment;

    public GratienSaleWorkflow(GratienTools tools, SaleAgentRunner saleAgentRunner) {
        this.tools = tools == null ? new GratienTools() : tools;
        this.saleAgentRunner = saleAgentRunner == null ? this.tools::insertSaleOutput : saleAgentRunner;
    }

    public boolean shouldHandle(String question, List<File> attachments) {
        if (pendingDraft != null) {
            // Une question d'information pendant un dialogue vente ne doit pas etre
            // avalee par le workflow: le chat generique repond, le dialogue reprend apres.
            return !looksLikeInfoQuery(question);
        }
        String value = question == null ? "" : question.toLowerCase(Locale.ROOT);
        if (looksLikeInfoQuery(value)) {
            return false;
        }
        boolean saleText = value.contains("vente") || value.contains("sortie") || value.contains("vendre")
                || value.contains("client");
        boolean saleImage = attachments != null && attachments.stream().anyMatch(ImageAttachment::isImage)
                && (value.isBlank() || saleText);
        return saleText || saleImage;
    }

    public String handle(String question, List<File> attachments) {
        if (pendingDraft != null) {
            String normalizedQuestion = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();
            if (isCancellation(question)) {
                clearPendingWorkflow();
                return "D'accord, la vente n'est pas enregistrée.";
            }
            if (normalizedQuestion.equals("oui") || normalizedQuestion.equals("confirmer") || normalizedQuestion.equals("ok")) {
                if (awaitingConfirmation) {
                    String result = saleAgentRunner.run(pendingDraft);
                    clearPendingWorkflow();
                    return result;
                }
            } else if (normalizedQuestion.equals("non")) {
                clearPendingWorkflow();
                return "D'accord, la vente n'est pas enregistrée.";
            } else {
                if (awaitingModification) {
                    return applyModification(question, pendingDraft);
                } else if (awaitingPaymentType) {
                    return handlePaymentType(question);
                } else if (awaitingCurrency) {
                    return handleCurrency(question);
                } else if (awaitingDueDate) {
                    return handleDueDate(question);
                } else if (awaitingClientInfo) {
                    return handleClientInfo(question);
                } else if (awaitingPartialPayment) {
                    return handlePartialPayment(question);
                } else if (awaitingConfirmation) {
                    awaitingModification = true;
                    return applyModification(question, pendingDraft);
                }
            }
        }

        // Start new workflow
        SaleDraft draft = extractDraft(question, attachments);
        if (draft == null || !draft.hasLines()) {
            return "Je n'ai pas pu extraire les articles de la vente. Veuillez fournir la liste des produits avec leurs quantités (ex: 'Produit A, 2; Produit B, 5').";
        }
        pendingDraft = draft;

        // Initialize with defaults if missing
        if (pendingDraft.getSaleDate() == null || pendingDraft.getSaleDate().isBlank()) {
            pendingDraft.setSaleDate(LocalDateTime.now().toString());
        }
        if (pendingDraft.getReference() == null || pendingDraft.getReference().isBlank()) {
            pendingDraft.setReference(generateInvoiceNumber());
        }

        return nextStep();
    }

    private String nextStep() {
        awaitingPaymentType = false;
        awaitingCurrency = false;
        awaitingDueDate = false;
        awaitingClientInfo = false;
        awaitingPartialPayment = false;
        awaitingConfirmation = false;

        // 1. Payment Type
        if (pendingDraft.getPaymentType() == null || pendingDraft.getPaymentType().isBlank()) {
            awaitingPaymentType = true;
            return "Veuillez indiquer le type de paiement: 'cash' (comptant), 'credit' (crédit) ou 'partial' (partiel).";
        }

        String pm = pendingDraft.getPaymentType().toUpperCase();
        if (!pm.equals("CASH") && !pm.equals("CREDIT") && !pm.equals("PARTIAL")) {
            awaitingPaymentType = true;
            return "Type de paiement non reconnu. Veuillez choisir: 'cash', 'credit' ou 'partial'.";
        }
        pendingDraft.setPaymentType(pm);

        // 2. Currency
        if (pendingDraft.getCurrency() == null || pendingDraft.getCurrency().isBlank()) {
            String defaultCurrency = Preferences.userNodeForPackage(tools.SyncEngine.class).get("currency", "USD");
            pendingDraft.setCurrency(defaultCurrency);
        }

        // 3. If credit or partial, we need due date and client info
        if ("CREDIT".equals(pm) || "PARTIAL".equals(pm)) {
            // Due date: if not given, default to 30 days from now
            if (pendingDraft.getDueDate() == null || pendingDraft.getDueDate().isBlank()) {
                pendingDraft.setDueDate(LocalDate.now().plusDays(30).toString());
            }

            // Client info (name and phone are required)
            if (pendingDraft.getClientName() == null || pendingDraft.getClientName().isBlank()) {
                awaitingClientInfo = true;
                return "Veuillez indiquer le nom et le numéro de téléphone du client (ex: 'Jean Dupont, +243812345678').";
            }
            if (pendingDraft.getClientPhone() == null || pendingDraft.getClientPhone().isBlank()) {
                awaitingClientInfo = true;
                return "Le numéro de téléphone du client est obligatoire pour les ventes à crédit. Veuillez l'indiquer.";
            }
        }

        // 4. If partial, we need cash amount or percentage
        if ("PARTIAL".equals(pm)) {
            if ((pendingDraft.getCashAmount() == null || pendingDraft.getCashAmount() <= 0) &&
                (pendingDraft.getCashPercentage() == null || pendingDraft.getCashPercentage() <= 0)) {
                awaitingPartialPayment = true;
                return "Veuillez indiquer le paiement cash effectué: soit un montant (ex: '100 USD'), soit un pourcentage (ex: '50%').";
            }
        }

        // All info gathered, show summary for confirmation!
        return showSummaryForConfirmation();
    }

    private String generateInvoiceNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "FAC-" + LocalDateTime.now().format(formatter);
    }

    private String handlePaymentType(String question) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();
        if (normalized.contains("cash") || normalized.contains("comptant")) {
            pendingDraft.setPaymentType("CASH");
        } else if (normalized.contains("credit") || normalized.contains("crédit")) {
            pendingDraft.setPaymentType("CREDIT");
        } else if (normalized.contains("partial") || normalized.contains("partiel")) {
            pendingDraft.setPaymentType("PARTIAL");
        } else {
            return "Veuillez choisir un type de paiement valide: 'cash', 'credit' ou 'partial'.";
        }
        return nextStep();
    }

    private String handleCurrency(String question) {
        String normalized = question == null ? "" : question.toUpperCase(Locale.ROOT).trim();
        if (normalized.equals("DEFAUT") || normalized.equals("DEFAULT")) {
            pendingDraft.setCurrency(Preferences.userNodeForPackage(tools.SyncEngine.class).get("currency", "USD"));
        } else if (normalized.equals("USD") || normalized.equals("CDF")) {
            pendingDraft.setCurrency(normalized);
        } else {
            return "Devise non reconnue. Veuillez indiquer 'USD', 'CDF' ou 'defaut'.";
        }
        return nextStep();
    }

    private String handleDueDate(String question) {
        String normalized = question == null ? "" : question.trim();
        if (normalized.equalsIgnoreCase("defaut") || normalized.equalsIgnoreCase("default")) {
            pendingDraft.setDueDate(LocalDate.now().plusDays(30).toString());
        } else {
            try {
                LocalDate.parse(normalized);
                pendingDraft.setDueDate(normalized);
            } catch (Exception e) {
                return "Format de date invalide. Veuillez utiliser le format YYYY-MM-DD, ou répondez 'defaut'.";
            }
        }
        return nextStep();
    }

    private String handleClientInfo(String question) {
        if (question == null || question.isBlank()) {
            return "Veuillez entrer le nom et le téléphone du client.";
        }
        String[] parts = question.split(",");
        if (parts.length >= 1) {
            pendingDraft.setClientName(parts[0].trim());
            if (parts.length >= 2) {
                pendingDraft.setClientPhone(parts[1].trim());
            }
        }
        return nextStep();
    }

    private String handlePartialPayment(String question) {
        String normalized = question == null ? "" : question.trim();
        if (normalized.contains("%")) {
            try {
                double percentage = Double.parseDouble(normalized.replace("%", "").trim());
                pendingDraft.setCashPercentage(percentage);
                pendingDraft.setCashAmount(null);
            } catch (NumberFormatException e) {
                return "Pourcentage invalide. Veuillez indiquer un pourcentage (ex: '50%') ou un montant (ex: '100 USD').";
            }
        } else {
            try {
                String[] parts = normalized.split(" ");
                double amount = Double.parseDouble(parts[0]);
                pendingDraft.setCashAmount(amount);
                pendingDraft.setCashPercentage(null);
            } catch (Exception e) {
                return "Montant invalide. Veuillez indiquer un montant (ex: '100 USD') ou un pourcentage (ex: '50%').";
            }
        }
        return nextStep();
    }

    private String showSummaryForConfirmation() {
        awaitingConfirmation = true;
        awaitingModification = false;
        StringBuilder sb = new StringBuilder();
        sb.append("**Résumé de la vente**\n\n");
        sb.append("Numéro de facture: ").append(pendingDraft.getReference()).append("\n");
        sb.append("Date de vente: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Devise: ").append(pendingDraft.getCurrency()).append("\n");
        sb.append("Type de paiement: ").append(pendingDraft.getPaymentType()).append("\n");

        if ("CREDIT".equals(pendingDraft.getPaymentType()) || "PARTIAL".equals(pendingDraft.getPaymentType())) {
            sb.append("Date d'échéance: ").append(pendingDraft.getDueDate()).append("\n");
            sb.append("Client: ").append(pendingDraft.getClientName());
            if (pendingDraft.getClientPhone() != null && !pendingDraft.getClientPhone().isEmpty()) {
                sb.append(" (").append(pendingDraft.getClientPhone()).append(")");
            }
            sb.append("\n");
        }

        if ("PARTIAL".equals(pendingDraft.getPaymentType())) {
            sb.append("Paiement partiel: ");
            if (pendingDraft.getCashAmount() != null) {
                sb.append(pendingDraft.getCashAmount()).append(" ").append(pendingDraft.getCurrency());
            } else if (pendingDraft.getCashPercentage() != null) {
                sb.append(pendingDraft.getCashPercentage()).append("%");
            }
            sb.append("\n");
        }

        sb.append("\n**Articles**\n");
        sb.append("| Produit | Quantité | Mesure |\n");
        sb.append("|---------|----------|--------|\n");
        for (int i = 0; i < pendingDraft.getLines().size(); i++) {
            SaleLine line = pendingDraft.getLines().get(i);
            sb.append("| ")
                    .append(line.getProductName())
                    .append(" | ")
                    .append(line.getQuantity())
                    .append(" | ")
                    .append(line.getMeasureName() != null ? line.getMeasureName() : "Pièce")
                    .append(" |\n");
        }

        sb.append("\nVeuillez confirmer cette vente (répondez 'oui') ou indiquer les modifications à apporter (ex: 'Changer la quantité du Produit A à 3').");
        return sb.toString();
    }

    private String applyModification(String question, SaleDraft draft) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();

        if (normalized.contains("quantité") || normalized.contains("quantity")) {
            for (int i = 0; i < draft.getLines().size(); i++) {
                SaleLine line = draft.getLines().get(i);
                if (normalized.contains(line.getProductName().toLowerCase(Locale.ROOT))) {
                    String[] words = normalized.split(" ");
                    for (String word : words) {
                        try {
                            double newQty = Double.parseDouble(word);
                            line.setQuantity(newQty);
                            awaitingModification = false;
                            return "Quantité de " + line.getProductName() + " modifiée à " + newQty + ".\n\n" + nextStep();
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            return "Je n'ai pas pu comprendre la modification. Veuillez réessayer (ex: 'Changer la quantité du " + draft.getLines().get(0).getProductName() + " à 3').";
        }

        if (normalized.contains("cash") || normalized.contains("comptant")) {
            draft.setPaymentType("CASH");
            return "Type de paiement modifié en CASH.\n\n" + nextStep();
        }
        if (normalized.contains("credit") || normalized.contains("crédit")) {
            draft.setPaymentType("CREDIT");
            return "Type de paiement modifié en CREDIT.\n\n" + nextStep();
        }
        if (normalized.contains("partial") || normalized.contains("partiel")) {
            draft.setPaymentType("PARTIAL");
            return "Type de paiement modifié en PARTIAL.\n\n" + nextStep();
        }

        if (normalized.contains("usd")) {
            draft.setCurrency("USD");
            return "Devise modifiée en USD.\n\n" + nextStep();
        }
        if (normalized.contains("cdf")) {
            draft.setCurrency("CDF");
            return "Devise modifiée en CDF.\n\n" + nextStep();
        }

        if (normalized.contains("date") || normalized.contains("échéance")) {
            awaitingDueDate = true;
            awaitingConfirmation = false;
            awaitingModification = false;
            return "Veuillez indiquer la nouvelle date d'échéance (format YYYY-MM-DD).";
        }

        if (normalized.contains("client") || normalized.contains("nom") || normalized.contains("téléphone") || normalized.contains("tel")) {
            awaitingClientInfo = true;
            awaitingConfirmation = false;
            awaitingModification = false;
            return "Veuillez indiquer les nouvelles informations du client: nom et téléphone, séparés par une virgule (ex: 'Jean Dupont, +243812345678').";
        }

        if (normalized.contains("partie") || normalized.contains("payé") || normalized.contains("cash payé") || normalized.contains("pourcentage")) {
            awaitingPartialPayment = true;
            awaitingConfirmation = false;
            awaitingModification = false;
            return "Veuillez indiquer la partie payée cash (montant ou pourcentage).";
        }

        return "Je n'ai pas pu comprendre la modification. Veuillez réessayer (ex: 'Changer la quantité du " + draft.getLines().get(0).getProductName() + " à 3' ou 'Changer le type de paiement à cash').";
    }

    private SaleDraft extractDraft(String question, List<File> attachments) {
        SaleDraft draft = null;
        if (attachments != null && attachments.stream().anyMatch(ImageAttachment::isImage)) {
            draft = extractDraftFromImages(attachments);
        }
        if (draft == null || !draft.hasLines()) {
            draft = extractDraftFromText(question);
        }
        if (draft != null) {
            String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
            if (q.contains("cash") || q.contains("comptant")) {
                draft.setPaymentType("CASH");
            } else if (q.contains("credit") || q.contains("crédit") || q.contains("dette")) {
                draft.setPaymentType("CREDIT");
            } else if (q.contains("partiel") || q.contains("partial")) {
                draft.setPaymentType("PARTIAL");
            } else {
                draft.setPaymentType(null);
            }

            if (q.contains("usd") || q.contains("dollars") || q.contains("$")) {
                draft.setCurrency("USD");
            } else if (q.contains("cdf") || q.contains("francs") || q.contains("fc")) {
                draft.setCurrency("CDF");
            } else {
                draft.setCurrency(null);
            }
        }
        return draft;
    }

    private SaleDraft extractDraftFromText(String question) {
        try {
            String systemPrompt = """
                    Analyse le texte utilisateur décrivant une vente/sortie de stock et retourne uniquement un JSON valide sans texte autour.
                    Le JSON doit suivre ce format exact:
                    {
                      "reference": "numero de facture si mentionné, sinon null",
                      "saleDate": "date de vente si mentionnée, sinon null",
                      "clientName": "nom client si mentionné, sinon null",
                      "clientPhone": "téléphone si mentionné, sinon null",
                      "currency": "devise CDF ou USD si mentionnée, sinon null",
                      "paymentType": "CASH, CREDIT ou PARTIAL si mentionné, sinon null",
                      "dueDate": "date d'échéance YYYY-MM-DD si mentionnée, sinon null",
                      "cashAmount": null, // montant payé en cash si partiel, sinon null
                      "cashPercentage": null, // pourcentage payé en cash si partiel, sinon null
                      "lines": [
                        {
                          "productName": "nom de l'article",
                          "quantity": 1.0, // quantité (nombre)
                          "measureName": "nom de la mesure/unité si mentionnée (ex: carton, sac, pièce), sinon null"
                        }
                      ]
                    }
                    Règles:
                    - Extrais bien tous les produits, leurs quantités et leurs mesures (ex: "5 cartons de lait" -> productName: "lait", quantity: 5.0, measureName: "carton").
                    - Si la devise n'est pas spécifiée, retourne null pour currency.
                    - Si le type de paiement n'est pas spécifié, retourne null pour paymentType.
                    - Si le client (nom, téléphone) ou l'échéance/date ne sont pas mentionnés, retourne null pour ces champs.
                    - Si le type de paiement est partiel (credit et cash/partiellement), détermine la partie cash payée soit sous forme de montant (cashAmount) soit sous forme de pourcentage (cashPercentage).
                    """;

            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            messages.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
            messages.add(dev.langchain4j.data.message.UserMessage.from(question));
            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .build();
            String answer = model.chat(request).aiMessage().text();
            return mapper.readValue(extractJson(answer), SaleDraft.class);
        } catch (Exception ex) {
            return extractDraftFromTextFallback(question);
        }
    }

    private static final java.util.regex.Pattern QUANTITY_MEASURE_PRODUCT = java.util.regex.Pattern.compile(
            "(?:^|[^0-9])"
            + "(\\d+(?:[.,]\\d+)?)\\s*"
            + "(carton|cartons|sac|sacs|piece|pièce|paquet|paquets|bouteille|bouteilles|bidon|bidons|kg|kilogramme|litre|litres|l|boite|boîte|boites|boîtes|colis)?\\s*"
            + "(?:de\\s+|d'|du\\s+|des\\s+)?"
            + "([A-Za-zÀ-ÿ][A-Za-zÀ-ÿ0-9\\-\\s/]*?)(?:[,\\;]|\\s+$|$)");

    private SaleDraft extractDraftFromTextFallback(String question) {
        SaleDraft draft = new SaleDraft();
        if (question == null || question.isBlank()) {
            return draft;
        }
        String[] itemStrings = question.split("[;\\n]");
        for (String itemString : itemStrings) {
            String cleaned = itemString.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            String[] parts = cleaned.split(",");
            SaleLine line = new SaleLine();
            if (parts.length >= 2) {
                line.setProductName(parts[0].trim());
                try {
                    line.setQuantity(Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException e) {
                    line.setQuantity(1d);
                }
                if (parts.length >= 3) {
                    line.setMeasureName(parts[2].trim());
                }
            } else {
                java.util.regex.Matcher matcher = QUANTITY_MEASURE_PRODUCT.matcher(cleaned);
                if (matcher.find()) {
                    line.setQuantity(parseQty(matcher.group(1)));
                    line.setMeasureName(matcher.group(2));
                    line.setProductName(matcher.group(3).trim());
                } else {
                    line.setProductName(cleaned.replace("vente", "").replace("sortie", "").replace("vendre", "").trim());
                    line.setQuantity(1d);
                }
            }
            if (line.getProductName() != null && !line.getProductName().isBlank()) {
                draft.getLines().add(line);
            }
        }
        return draft;
    }

    private double parseQty(String value) {
        try {
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return 1d;
        }
    }

    private SaleDraft extractDraftFromImages(List<File> attachments) {
        try {
            List<Content> contents = new ArrayList<>();
            contents.add(TextContent.from("""
                    Lis cette image comme une liste de produits pour une vente/sortie.
                    Retourne uniquement un JSON valide:
                    {
                      "reference": "numero de facture si visible",
                      "saleDate": "YYYY-MM-DD ou null",
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
            String answer = imageModel.chat(request);
            return mapper.readValue(extractJson(answer), SaleDraft.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public void clearPendingWorkflow() {
        pendingDraft = null;
        awaitingConfirmation = false;
        awaitingModification = false;
        awaitingPaymentType = false;
        awaitingCurrency = false;
        awaitingDueDate = false;
        awaitingClientInfo = false;
        awaitingPartialPayment = false;
    }

    private boolean isCancellation(String question) {
        String value = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        return value.equals("non")
                || value.equals("no")
                || value.equals("annule")
                || value.equals("annuler")
                || value.equals("annuler tout")
                || value.equals("cancel")
                || value.equals("abandonne")
                || value.equals("abandonner")
                || value.equals("j'annule")
                || value.equals("stop")
                || value.equals("arrete")
                || value.equals("arrête")
                || value.equals("arret")
                || value.equals("arrêt")
                || value.equals("quitte")
                || value.equals("quitter")
                || value.contains("annul")
                || value.contains("abandon");
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

    private Content imageContent(File file) {
        return ImageAttachment.imageContent(file);
    }

    private String extractJson(String answer) {
        int start = answer == null ? -1 : answer.indexOf('{');
        int end = answer == null ? -1 : answer.lastIndexOf('}');
        return start >= 0 && end > start ? answer.substring(start, end + 1) : "{}";
    }
}
