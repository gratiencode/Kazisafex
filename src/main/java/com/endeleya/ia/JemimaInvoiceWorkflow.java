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
import java.util.Optional;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class JemimaInvoiceWorkflow {

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
    private final InvoiceAgentRunner invoiceAgentRunner;
    private InvoiceDraft pendingDraft;
    private boolean awaitingSaveConfirmation;
    private boolean awaitingPaymentAnswer;
    private boolean awaitingPaymentAmount;
    private boolean awaitingLotDetails;
    private boolean awaitingInsertConfirmation;
    private boolean awaitingSalePriceCorrectionChoice;
    private boolean awaitingSalePriceCorrection;

    public JemimaInvoiceWorkflow() {
        this(new JemimaTools());
    }

    public JemimaInvoiceWorkflow(JemimaTools tools) {
        this.tools = tools == null ? new JemimaTools() : tools;
        this.invoiceAgentRunner = this.tools::insertInvoiceSupply;
    }

    public JemimaInvoiceWorkflow(JemimaTools tools, InvoiceAgentRunner invoiceAgentRunner) {
        this.tools = tools == null ? new JemimaTools() : tools;
        this.invoiceAgentRunner = invoiceAgentRunner == null ? this.tools::insertInvoiceSupply : invoiceAgentRunner;
    }

    public boolean shouldHandle(String question, List<File> attachments) {
        if (pendingDraft != null && (awaitingSaveConfirmation || awaitingPaymentAnswer || awaitingPaymentAmount || awaitingLotDetails
                || isConfirmation(question) || isCancellation(question) || looksLikePriceTable(question)
                || looksLikeLotTable(question))) {
            return true;
        }
        return attachments != null && attachments.stream().anyMatch(this::isImage)
                && (question == null || question.isBlank()
                || question.toLowerCase(Locale.ROOT).contains("facture")
                || question.toLowerCase(Locale.ROOT).contains("approvisionnement")
                || question.toLowerCase(Locale.ROOT).contains("réquisition")
                || question.toLowerCase(Locale.ROOT).contains("requisition"));
    }

    public String handle(String question, List<File> attachments) {
        if (pendingDraft != null && isCancellation(question)) {
            clearPendingWorkflow();
            return "D'accord, je n'insère rien en base.";
        }
        if (pendingDraft != null && awaitingSaveConfirmation) {
            if (!isConfirmation(question)) {
                clearPendingWorkflow();
                return "D'accord, cette facture ne sera pas enregistrée comme approvisionnement.";
            }
            awaitingSaveConfirmation = false;
            awaitingPaymentAnswer = true;
            return "Avant de créer la livraison, avez-vous déjà tout payé au fournisseur ?"
                    + "\nRépondez *oui* si tout est payé, ou *non* si ce n'est pas totalement payé.";
        }
        if (pendingDraft != null && awaitingPaymentAmount) {
            applyPaymentAmount(question, pendingDraft);
            awaitingPaymentAmount = false;
            return continueAfterPayment();
        }
        if (pendingDraft != null && awaitingPaymentAnswer) {
            if (isNegativePaymentAnswer(question)) {
                awaitingPaymentAnswer = false;
                awaitingPaymentAmount = true;
                return "Combien avez-vous réellement payé au fournisseur ? Indiquez le montant, par exemple: *120 USD* ou *0*.";
            }
            applyPaymentAnswer(question, pendingDraft);
            awaitingPaymentAnswer = false;
            return continueAfterPayment();
        }
        if (pendingDraft != null && awaitingLotDetails && looksLikeLotTable(question)) {
            // Les lots manquants sont collectes avant de passer aux prix de vente et a l'insertion.
            applyLotDetails(question, pendingDraft);
            if (!tools.findMissingLotDetails(pendingDraft).isEmpty()) {
                return tools.missingLotTemplate(pendingDraft);
            }
            awaitingLotDetails = false;
            return continueAfterLotDetails();
        }
        if (pendingDraft != null && awaitingSalePriceCorrectionChoice) {
            awaitingSalePriceCorrectionChoice = false;
            if (isConfirmation(question)) {
                awaitingSalePriceCorrection = true;
                return salePriceCorrectionTemplate(pendingDraft);
            }
            awaitingInsertConfirmation = true;
            return "D'accord, les prix saisis seront gardés. Répondez *oui* pour insérer la livraison et les réquisitions en base.";
        }
        if (pendingDraft != null && awaitingSalePriceCorrection) {
            applySalePriceCorrections(question, pendingDraft);
            awaitingSalePriceCorrection = false;
            return continueAfterSalePrices();
        }
        if (pendingDraft != null && looksLikePriceTable(question) && !isConfirmation(question)) {
            applySalePrices(question, pendingDraft);
            if (!tools.findMissingSalePrices(pendingDraft).isEmpty()) {
                pendingDraft.setMissingSalePrices(tools.findMissingSalePrices(pendingDraft));
                return tools.missingPriceTemplate(pendingDraft);
            }
            return continueAfterSalePrices();
        }
        if (pendingDraft != null && awaitingInsertConfirmation && isConfirmation(question)) {
            String result = invoiceAgentRunner.run(pendingDraft);
            if (!result.contains("Certains produits")) {
                clearPendingWorkflow();
            }
            return result;
        }

        pendingDraft = runInvoiceGraph(attachments);
        if (pendingDraft == null || !pendingDraft.hasLines()) {
            pendingDraft = null;
            return "Je n'ai pas pu lire correctement les articles de cette facture. Envoyez une photo plus nette ou un scan cadré.";
        }
        List<String> missing = tools.findMissingSalePrices(pendingDraft);
        pendingDraft.setMissingSalePrices(missing);
        awaitingSaveConfirmation = true;
        awaitingInsertConfirmation = false;
        return formatDraft(pendingDraft)
                + "\n\nVoulez-vous enregistrer cette facture comme approvisionnement dans la base de donnée ?"
                + "\nRépondez *oui* pour continuer ou *non* pour annuler.";
    }

    private InvoiceDraft extractDraft(List<File> attachments) {
        try {
            List<Content> contents = new ArrayList<>();
            contents.add(TextContent.from("""
                    Analyse cette image de facture et retourne uniquement un JSON valide sans texte autour.
                    Le JSON doit suivre ce format exact:
                    {
                      "supplier": "nom fournisseur(emetteur) si visible",
                      "supplierIdNat": "ID-NAT fournisseur si visible, sinon null",
                      "supplierRccm": "RCCM fournisseur si visible, sinon null",
                      "supplierTaxNumber": "Numero impot fournisseur si visible, sinon null",
                      "supplierAddress": "adresse fournisseur si visible, sinon null",
                      "supplierPhone": "telephone fournisseur si visible, sinon null",
                      "reference": "numéro facture ou null",
                      "invoiceDate": "yyyy-MM-dd ou null",
                      "currency": "USD/CDF/autre",
                      "payed": 0,
                      "reduction": 0,
                      "lines": [
                        {
                          "productName": "nom article",
                          "category": "catégorie commerciale probable selon les connaissances générales du web",
                          "measureName": "mesure/unite precisee sur la ligne de facture ou Pièce",
                          "quantity": 1,
                          "purchaseUnitPrice": 0,
                          "total": 0,
                          "lotNumber": "numero de lot si visible, sinon null",
                          "expiryDate": "date expiration yyyy-MM-dd si visible, sinon null"
                        }
                      ]
                    }
                    Règles:
                    - Ne crée pas d'article si tu n'es pas sûr de lire son nom.
                    - supplier est l'emetteur de la facture, donc le fournisseur.
                    - Lis ID-NAT, RCCM, Numero Impot, adresse et telephone du fournisseur uniquement s'ils sont visibles.
                    - purchaseUnitPrice est le prix d'achat unitaire lu sur la facture; il sera enregistre dans Recquisition.coutAchat.
                    - Si le prix d'achat unitaire n'est pas visible, calcule purchaseUnitPrice depuis total / quantité.
                    - measureName doit reprendre l'unite de la facture: carton, paquet, pièce, kg, litre, etc. Si absent, mets "Pièce".
                    - lotNumber et expiryDate ne doivent etre renseignes que s'ils sont visibles sur la facture.
                    - Ne retourne jamais de prix de vente, de quantite minimale ou de quantite maximale: ces valeurs ne viennent pas de la facture.
                    - payed est le montant réellement payé si visible; sinon 0.
                    - reduction est la remise/réduction si visible; sinon 0.
                    - La catégorie doit être courte; si incertain, mets "Divers".
                    """));
            for (File file : attachments) {
                Content image = imageContent(file);
                if (image != null) {
                    contents.add(image);
                }
            }
            ChatRequest request = ChatRequest.builder()
                    .messages(UserMessage.from(contents))
                    .build();
            String answer = model.chat(request).aiMessage().text();
            return mapper.readValue(extractJson(answer), InvoiceDraft.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private InvoiceDraft runInvoiceGraph(List<File> attachments) {
        InvoiceDraft[] result = new InvoiceDraft[1];
        try {
            // Workflow facture interne: extraction, verification livraison, preparation recquisitions.
            CompiledGraph<InvoiceAgentState> graphs = new StateGraph<>(InvoiceAgentState.SCHEMA, InvoiceAgentState::new)
                    .addNode("product_creator_agent", node_async(state -> {
                        // L'agent createur lit la facture et deduit categorie/produit/mesure.
                        result[0] = extractDraft(attachments);
                        return Map.of("step", "product_creator_agent");
                    }))
                    .addNode("supplier_delivery_agent", node_async(state -> {
                        // L'agent livraison controle les donnees avant le dialogue paiement/lot.
                        if (result[0] != null) {
                            result[0].setMissingSalePrices(tools.findMissingSalePrices(result[0]));
                        }
                        return Map.of("step", "supplier_delivery_agent");
                    }))
                    .addNode("requisition_agent", node_async(state -> {
                        // L'agent recquisition repere les prix manquants avant insertion.
                        if (result[0] != null) {
                            result[0].setMissingSalePrices(tools.findMissingSalePrices(result[0]));
                        }
                        return Map.of("step", "requisition_agent");
                    }))
                    .addEdge(START, "product_creator_agent")
                    .addEdge("product_creator_agent", "supplier_delivery_agent")
                    .addEdge("supplier_delivery_agent", "requisition_agent")
                    .addEdge("requisition_agent", END)
                    .compile();
            Optional<InvoiceAgentState> d = graphs.invoke(Map.of("step", "start"));
            
//            for (var ignored : graphs.stream(Map.of("step", "start"))) {
//                // Iterating executes the graph; state updates are kept in the side-effect result.
//            }
        } catch (Exception ex) {
            result[0] = extractDraft(attachments);
        }
        return result[0];
    }

    private String formatDraft(InvoiceDraft draft) {
        StringBuilder builder = new StringBuilder();
        builder.append("*Facture lue par Jemima*\n\n");
        builder.append("Fournisseur: ").append(nullToDash(draft.getSupplier())).append("\n");
        builder.append("ID-NAT fournisseur: ").append(nullToDash(draft.getSupplierIdNat())).append("\n");
        builder.append("RCCM fournisseur: ").append(nullToDash(draft.getSupplierRccm())).append("\n");
        builder.append("Numéro impot fournisseur: ").append(nullToDash(draft.getSupplierTaxNumber())).append("\n");
        builder.append("Adresse fournisseur: ").append(nullToDash(draft.getSupplierAddress())).append("\n");
        builder.append("Téléphone fournisseur: ").append(nullToDash(draft.getSupplierPhone())).append("\n");
        builder.append("Référence: ").append(nullToDash(draft.getReference())).append("\n");
        builder.append("Date: ").append(nullToDash(draft.getInvoiceDate())).append("\n\n");
        builder.append("Montant payé lu: ").append(draft.getPayed() == null ? 0 : draft.getPayed()).append("\n");
        builder.append("Réduction lue: ").append(draft.getReduction() == null ? 0 : draft.getReduction()).append("\n\n");
        builder.append("| Article | Catégorie | Qté | Prix achat | Total |\n");
        builder.append("|---|---|---:|---:|---:|\n");
        for (InvoiceLine line : draft.getLines()) {
            builder.append("| ").append(nullToDash(line.getProductName()))
                    .append(" | ").append(nullToDash(line.getCategory()))
                    .append(" | ").append(line.getQuantity())
                    .append(" | ").append(line.getPurchaseUnitPrice())
                    .append(" | ").append(line.getTotal())
                    .append(" |\n");
        }
        return builder.toString();
    }

    private void applySalePrices(String text, InvoiceDraft draft) {
        Map<String, InvoiceLine> index = new java.util.HashMap<>();
        List<String> missingNames = tools.findMissingSalePrices(draft).stream()
                .map(this::normalize)
                .toList();
        List<InvoiceLine> missingLines = new ArrayList<>();
        for (InvoiceLine line : draft.getLines()) {
            index.put(normalize(line.getProductName()), line);
            if (missingNames.contains(normalize(line.getProductName()))) {
                missingLines.add(line);
            }
        }
        for (String row : text.split("\\R")) {
            if (row.isBlank() || row.toLowerCase(Locale.ROOT).contains("produit")
                    || row.toLowerCase(Locale.ROOT).contains("numero")) {
                continue;
            }
            if (row.contains("|")) {
                String[] cells = row.replaceAll("^\\|", "").replaceAll("\\|$", "").split("\\|");
                if (cells.length < 2) {
                    continue;
                }
                InvoiceLine line;
                int numberedIndex = parseIndex(cells[0].trim()) - 1;
                if (numberedIndex >= 0 && numberedIndex < missingLines.size()) {
                    line = missingLines.get(numberedIndex);
                } else {
                    line = index.get(normalize(cells[0].trim()));
                }
                if (line != null) {
                    if (cells.length >= 4) {
                        line.setSalePriceQmin(parseAmount(cells[1]));
                        line.setSalePriceQmax(parseAmount(cells[2]));
                        line.setSalePrice(parseAmount(cells[3]));
                        if (cells.length >= 5) {
                            line.setSaleCurrency(cells[4].trim());
                        }
                    } else {
                        line.setSalePrice(parseAmount(cells[1]));
                        if (cells.length >= 3) {
                            line.setSaleCurrency(cells[2].trim());
                        }
                        line.setSalePriceQmin(1d);
                        line.setSalePriceQmax(999999d);
                    }
                }
            } else {
                String[] cells = row.split(",");
                if (cells.length < 3) {
                    continue;
                }
                int itemIndex = parseIndex(cells[0].trim()) - 1;
                if (itemIndex >= 0 && itemIndex < missingLines.size()) {
                    InvoiceLine line = missingLines.get(itemIndex);
                    if (cells.length >= 5) {
                        line.setSalePriceQmin(parseAmount(cells[1]));
                        line.setSalePriceQmax(parseAmount(cells[2]));
                        line.setSalePrice(parseAmount(cells[3]));
                        line.setSaleCurrency(cells[4].trim());
                    } else {
                        line.setSalePriceQmin(1d);
                        line.setSalePriceQmax(999999d);
                        line.setSalePrice(parseAmount(cells[1]));
                        line.setSaleCurrency(cells[2].trim());
                    }
                }
            }
        }
    }

    private void applySalePriceCorrections(String text, InvoiceDraft draft) {
        if (draft == null || draft.getLines() == null) {
            return;
        }
        for (String row : text.split("\\R")) {
            if (row.isBlank() || row.toLowerCase(Locale.ROOT).contains("numero")) {
                continue;
            }
            String[] cells = row.split(",");
            if (cells.length < 2) {
                continue;
            }
            int itemIndex = parseIndex(cells[0].trim()) - 1;
            if (itemIndex >= 0 && itemIndex < draft.getLines().size()) {
                InvoiceLine line = draft.getLines().get(itemIndex);
                double newPrice = parseAmount(cells[1]);
                if (newPrice > purchaseUnitCost(line)) {
                    line.setSalePrice(newPrice);
                    if (cells.length >= 3) {
                        line.setSaleCurrency(cells[2].trim());
                    }
                }
            }
        }
    }

    private void applyLotDetails(String text, InvoiceDraft draft) {
        Map<String, InvoiceLine> index = new java.util.HashMap<>();
        List<InvoiceLine> missingLines = new ArrayList<>();
        for (InvoiceLine line : draft.getLines()) {
            index.put(normalize(line.getProductName()), line);
            if (line.getLotNumber() == null || line.getLotNumber().isBlank()
                    || line.getExpiryDate() == null || line.getExpiryDate().isBlank()) {
                missingLines.add(line);
            }
        }
        // Formats acceptes: | Produit | numlot | dateExpiry | ou numero, numlot, dateExpiry.
        for (String row : text.split("\\R")) {
            if (row.toLowerCase(Locale.ROOT).contains("produit")) {
                continue;
            }
            if (row.contains("|")) {
                String[] cells = row.replaceAll("^\\|", "").replaceAll("\\|$", "").split("\\|");
                if (cells.length < 3) {
                    continue;
                }
                InvoiceLine line = index.get(normalize(cells[0].trim()));
                if (line != null) {
                    line.setLotNumber(cells[1].trim());
                    line.setExpiryDate(cells[2].trim());
                }
            } else {
                String[] cells = row.split(",");
                if (cells.length < 3) {
                    continue;
                }
                int itemIndex = parseIndex(cells[0].trim()) - 1;
                if (itemIndex >= 0 && itemIndex < missingLines.size()) {
                    InvoiceLine line = missingLines.get(itemIndex);
                    line.setLotNumber(cells[1].trim());
                    line.setExpiryDate(cells[2].trim());
                }
            }
        }
    }

    private Content imageContent(File file) throws Exception {
        if (file == null || !file.isFile() || !isImage(file)) {
            return null;
        }
        String mime = Files.probeContentType(file.toPath());
        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        Image image = Image.builder().base64Data(base64).mimeType(mime).build();
        return ImageContent.from(image);
    }

    private boolean isImage(File file) {
        try {
            String mime = Files.probeContentType(file.toPath());
            return mime != null && mime.toLowerCase(Locale.ROOT).startsWith("image/");
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isConfirmation(String question) {
        String value = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        return value.equals("oui") || value.equals("ok") || value.equals("confirme") || value.equals("j'accepte");
    }

    private boolean isCancellation(String question) {
        String value = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        return value.equals("non, annule")
                || value.equals("annule")
                || value.equals("annuler")
                || value.equals("cancel")
                || value.equals("abandonne");
    }

    private boolean looksLikePriceTable(String question) {
        String value = question == null ? "" : question.toLowerCase(Locale.ROOT);
        return value.contains("prix")
                || (value.contains("|") && (value.contains("qmin") || value.contains("prixunitaire")))
                || value.matches("(?s).*\\b\\d+\\s*,\\s*[^,]+\\s*,\\s*[^,\\n]+.*");
    }

    private boolean looksLikeLotTable(String question) {
        String value = question == null ? "" : question.toLowerCase(Locale.ROOT);
        return value.contains("|")
                || value.contains("numlot")
                || value.contains("dateexpiry")
                || value.contains("lot")
                || value.matches("(?s).*\\b\\d+\\s*,\\s*[^,]+\\s*,\\s*[^,\\n]+.*");
    }

    private void applyPaymentAnswer(String question, InvoiceDraft draft) {
        String value = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        double total = invoiceTotal(draft);
        if (isConfirmation(question) || value.contains("tout") || value.contains("totalement")
                || value.contains("entierement") || value.contains("entièrement")) {
            draft.setPayed(total);
            return;
        }
        double amount = parseAmount(question);
        draft.setPayed(Math.min(Math.max(amount, 0d), total));
    }

    private void applyPaymentAmount(String question, InvoiceDraft draft) {
        double total = invoiceTotal(draft);
        double amount = parseAmount(question);
        draft.setPayed(Math.min(Math.max(amount, 0d), total));
    }

    private boolean isNegativePaymentAnswer(String question) {
        String value = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        return value.equals("non")
                || value.equals("no")
                || value.contains("pas tout")
                || value.contains("pas totalement")
                || value.contains("pas entierement")
                || value.contains("pas entièrement") || value.contains("apana");
    }

    private String continueAfterPayment() {
        // Apres le paiement, on complete d'abord les champs obligatoires de Recquisition.
        if (!tools.findMissingLotDetails(pendingDraft).isEmpty()) {
            awaitingLotDetails = true;
            return paymentSummary(pendingDraft) + "\n\n" + tools.missingLotTemplate(pendingDraft);
        }
        return continueAfterLotDetails();
    }

    private String continueAfterLotDetails() {
        List<String> missing = tools.findMissingSalePrices(pendingDraft);
        pendingDraft.setMissingSalePrices(missing);
        if (!missing.isEmpty()) {
            return paymentSummary(pendingDraft) + "\n\n" + tools.missingPriceTemplate(pendingDraft);
        }
        return paymentSummary(pendingDraft) + "\n\n" + continueAfterSalePrices();
    }

    private String continueAfterSalePrices() {
        String warnings = tools.salePriceWarnings(pendingDraft);
        if (!warnings.isBlank()) {
            awaitingSalePriceCorrectionChoice = true;
            return warnings
                    + "\nVoulez-vous rectifier le prix de vente avant l'enregistrement ? Répondez *oui* pour corriger ou *non* pour garder ce prix.";
        }
        awaitingInsertConfirmation = true;
        return "Prix de vente reçus. Répondez *oui* pour insérer la livraison et les réquisitions en base.";
    }

    private String salePriceCorrectionTemplate(InvoiceDraft draft) {
        StringBuilder builder = new StringBuilder();
        builder.append("Entrez le nouveau prix de vente, supérieur au prix d'achat, avec le format:\n\n");
        builder.append("numero, nouveau prix, devise\n\n");
        builder.append("Produits concernés:\n");
        for (int i = 0; i < draft.getLines().size(); i++) {
            InvoiceLine line = draft.getLines().get(i);
            if (line.getSalePrice() != null && line.getSalePrice() <= purchaseUnitCost(line)) {
                builder.append(i + 1).append(". ")
                        .append(nullToDash(line.getProductName()))
                        .append(" | prix achat: ").append(purchaseUnitCost(line))
                        .append(" | prix saisi: ").append(line.getSalePrice())
                        .append("\n");
            }
        }
        builder.append("\nExemple:\n1, 350, ").append(invoiceCurrency(draft));
        return builder.toString();
    }

    private String paymentSummary(InvoiceDraft draft) {
        double total = invoiceTotal(draft);
        double payed = draft.getPayed() == null ? 0d : Math.max(0d, draft.getPayed());
        double reduction = draft.getReduction() == null ? 0d : Math.max(0d, draft.getReduction());
        double remained = Math.max(0d, total - payed);
        return "*Paiement fournisseur confirmé*\n\n"
                + "Total facture: " + total + "\n"
                + "Payé réellement: " + payed + "\n"
                + "Réduction: " + reduction + "\n"
                + "Dette fournisseur restante: " + remained;
    }

    private double invoiceTotal(InvoiceDraft draft) {
        return draft.getLines().stream()
                .mapToDouble(line -> line.getTotal() > 0
                ? line.getTotal()
                : Math.max(0d, line.getQuantity()) * Math.max(0d, line.getPurchaseUnitPrice()))
                .sum();
    }

    private double purchaseUnitCost(InvoiceLine line) {
        if (line == null) {
            return 0d;
        }
        if (line.getPurchaseUnitPrice() > 0) {
            return line.getPurchaseUnitPrice();
        }
        double quantity = line.getQuantity() <= 0 ? 1d : line.getQuantity();
        return line.getTotal() > 0 ? line.getTotal() / quantity : 0d;
    }

    private String invoiceCurrency(InvoiceDraft draft) {
        return draft == null || draft.getCurrency() == null || draft.getCurrency().isBlank()
                ? "USD"
                : draft.getCurrency();
    }

    private void clearPendingWorkflow() {
        pendingDraft = null;
        awaitingSaveConfirmation = false;
        awaitingPaymentAnswer = false;
        awaitingPaymentAmount = false;
        awaitingLotDetails = false;
        awaitingInsertConfirmation = false;
        awaitingSalePriceCorrectionChoice = false;
        awaitingSalePriceCorrection = false;
    }

    private String extractJson(String answer) {
        if (answer == null) {
            return "{}";
        }
        int start = answer.indexOf('{');
        int end = answer.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return answer.substring(start, end + 1);
        }
        return answer;
    }

    private double parseAmount(String value) {
        if (value == null) {
            return 0d;
        }
        String cleaned = value.replaceAll("[^0-9,.-]", "").replace(",", ".");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

    private int parseIndex(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public static class InvoiceAgentState extends AgentState {

        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                "step", Channels.appender(ArrayList::new)
        );

        public InvoiceAgentState(Map<String, Object> initData) {
            super(initData);
        }
    }
}
