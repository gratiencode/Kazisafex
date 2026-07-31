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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import tools.DataId;
import tools.SyncEngine;

public class GratienProductImageWorkflow {

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
    private final Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    private final GratienTools tools;
    private InvoiceDraft pendingDraft;
    private boolean awaitingQuantity;
    private boolean awaitingSalePrices;

    public GratienProductImageWorkflow(GratienTools tools) {
        this.tools = tools == null ? new GratienTools() : tools;
    }

    public boolean shouldHandle(String question, List<File> attachments) {
        if (pendingDraft != null && (awaitingQuantity || awaitingSalePrices)) {
            return true;
        }
        String value = normalize(question);
        boolean productIntent = value.contains("produit")
                || value.contains("article")
                || value.contains("liste")
                || value.contains("photo")
                || value.contains("image");
        boolean excludedDocumentIntent = value.contains("facture")
                || value.contains("vente")
                || value.contains("sortie")
                || value.contains("depense")
                || value.contains("dépense")
                || value.contains("recu")
                || value.contains("reçu");
        return hasImage(attachments) && productIntent && !excludedDocumentIntent;
    }

    public String handle(String question, List<File> attachments) {
        if (pendingDraft != null && awaitingQuantity) {
            applyQuantities(question, pendingDraft);
            if (!missingQuantityLines(pendingDraft).isEmpty()) {
                return missingQuantityPrompt(pendingDraft);
            }
            awaitingQuantity = false;
            awaitingSalePrices = true;
            return salePricePrompt(pendingDraft);
        }
        if (pendingDraft != null && awaitingSalePrices) {
            applySalePrices(question, pendingDraft);
            if (!missingSalePriceLines(pendingDraft).isEmpty()) {
                return missingSalePricePrompt(pendingDraft);
            }
            String result = tools.insertGenericProductImageSupply(pendingDraft);
            pendingDraft = null;
            awaitingSalePrices = false;
            return result;
        }

        InvoiceDraft draft = extractProductsFromImages(attachments);
        if (draft == null || !draft.hasLines()) {
            return "Je n'ai pas pu lire une liste de produits exploitable sur cette image. Envoyez une image plus nette ou cadrez uniquement la liste.";
        }
        normalizeDraft(draft);
        pendingDraft = draft;
        awaitingQuantity = true;
        return productListPrompt(draft);
    }

    private InvoiceDraft extractProductsFromImages(List<File> attachments) {
        try {
            List<Content> contents = new ArrayList<>();
            contents.add(TextContent.from("""
                    Lis cette image comme une liste de produits à approvisionner génériquement.
                    Retourne uniquement un JSON valide:
                    {
                      "currency": "USD ou CDF",
                      "lines": [
                        {
                          "productName": "nom produit lisible",
                          "category": "catégorie commerciale probable",
                          "measureName": "Pièce",
                          "quantity": 0,
                          "purchaseUnitPrice": 0,
                          "total": 0
                        }
                      ]
                    }
                    Règles:
                    - Ne retourne que les produits clairement lisibles.
                    - Ne déduis pas les quantités si elles ne sont pas évidentes: mets 0.
                    - Ne déduis pas le prix de vente: il sera demandé à l'utilisateur.
                    - Si la mesure n'est pas visible, mets "Pièce".
                    - La catégorie doit être courte; si incertain, mets "Divers".
                    """));
            if (attachments != null) {
                for (File file : attachments) {
                    Content image = imageContent(file);
                    if (image != null) {
                        contents.add(image);
                    }
                }
            }
            ChatRequest request = ChatRequest.builder().messages(UserMessage.from(contents)).build();
            String answer = model.chat(request).aiMessage().text();
            return mapper.readValue(extractJson(answer), InvoiceDraft.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private void normalizeDraft(InvoiceDraft draft) {
        draft.setSupplier("Entreprise connectée");
        draft.setReference("APPRO-IMAGE-" + DataId.generate().substring(0, 8));
        draft.setInvoiceDate(LocalDate.now().toString());
        draft.setCurrency(safe(draft.getCurrency(), "USD"));
        draft.setPayed(0d);
        draft.setReduction(0d);
        String lot = "IMG-" + LocalDate.now() + "-" + pref.get("region", "Goma").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-");
        for (InvoiceLine line : draft.getLines()) {
            line.setMeasureName(safe(line.getMeasureName(), "Pièce"));
            line.setCategory(safe(line.getCategory(), "Divers"));
            line.setPurchaseUnitPrice(0d);
            line.setTotal(0d);
            line.setLotNumber(lot);
            line.setExpiryDate("none");
            line.setSalePriceQmin(1d);
            line.setSalePriceQmax(999999d);
            line.setSaleCurrency(draft.getCurrency());
        }
    }

    private String productListPrompt(InvoiceDraft draft) {
        StringBuilder builder = new StringBuilder("Produits lus sur l'image:\n\n");
        builder.append("|numero|produit|catégorie|mesure|\n");
        builder.append("|---:|---|---|---|\n");
        for (int i = 0; i < draft.getLines().size(); i++) {
            InvoiceLine line = draft.getLines().get(i);
            builder.append("|").append(i + 1)
                    .append("|").append(tableCell(line.getProductName()))
                    .append("|").append(tableCell(line.getCategory()))
                    .append("|").append(tableCell(line.getMeasureName()))
                    .append("|\n");
        }
        builder.append("""

                Donnez maintenant les quantités d'approvisionnement au format:

                numero, quantité

                Vous pouvez envoyer plusieurs lignes à la fois.
                Exemple:
                1, 5
                2, 10
                """);
        return builder.toString();
    }

    private String missingQuantityPrompt(InvoiceDraft draft) {
        StringBuilder builder = new StringBuilder("Il manque encore la quantité d'approvisionnement pour certaines lignes:\n\n");
        for (String missing : missingQuantityLines(draft)) {
            builder.append("- ").append(missing).append("\n");
        }
        builder.append("""

                Répondez avec:
                numero, quantité
                """);
        return builder.toString();
    }

    private String salePricePrompt(InvoiceDraft draft) {
        StringBuilder builder = new StringBuilder("""
                Quantités reçues.

                Donnez maintenant les prix de vente avec le même format que le workflow facture approvisionnement:

                numero, quantite min, quantite max, prix vente, devise

                Produits concernés:
                """);
        for (int i = 0; i < draft.getLines().size(); i++) {
            InvoiceLine line = draft.getLines().get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(safe(line.getProductName(), "Produit"))
                    .append(" | quantité approvisionnée: ")
                    .append(line.getQuantity())
                    .append("\n");
        }
        builder.append("""

                Exemple:
                1, 1, 999999, 25, """).append(safe(draft.getCurrency(), "USD")).append("""

                Vous pouvez envoyer plusieurs lignes à la fois.
                """);
        return builder.toString();
    }

    private String missingSalePricePrompt(InvoiceDraft draft) {
        StringBuilder builder = new StringBuilder("""
                Certains produits n'ont pas encore de prix de vente.
                Répondez ligne par ligne avec le format:

                numero, quantite min, quantite max, prix vente, devise

                Produits concernés:
                """);
        for (String missing : missingSalePriceLines(draft)) {
            builder.append("- ").append(missing).append("\n");
        }
        return builder.toString();
    }

    private List<String> missingQuantityLines(InvoiceDraft draft) {
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < draft.getLines().size(); i++) {
            InvoiceLine line = draft.getLines().get(i);
            if (line.getQuantity() <= 0) {
                missing.add((i + 1) + ". " + safe(line.getProductName(), "Produit"));
            }
        }
        return missing;
    }

    private List<String> missingSalePriceLines(InvoiceDraft draft) {
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < draft.getLines().size(); i++) {
            InvoiceLine line = draft.getLines().get(i);
            if (line.getSalePrice() == null || line.getSalePrice() <= 0) {
                missing.add((i + 1) + ". " + safe(line.getProductName(), "Produit"));
            }
        }
        return missing;
    }

    private void applyQuantities(String text, InvoiceDraft draft) {
        if (text == null || text.isBlank() || draft == null || draft.getLines() == null) {
            return;
        }
        for (String row : text.split("\\R")) {
            String cleaned = row.trim();
            if (cleaned.isBlank() || cleaned.toLowerCase(Locale.ROOT).contains("numero")) {
                continue;
            }
            String[] cells = cleaned.split("[,;|]");
            if (cells.length < 2) {
                continue;
            }
            int index = (int) parseDouble(cells[0], 0d) - 1;
            if (index < 0 || index >= draft.getLines().size()) {
                continue;
            }
            InvoiceLine line = draft.getLines().get(index);
            line.setQuantity(parseDouble(cells[1], line.getQuantity()));
        }
    }

    private void applySalePrices(String text, InvoiceDraft draft) {
        if (text == null || text.isBlank() || draft == null || draft.getLines() == null) {
            return;
        }
        for (String row : text.split("\\R")) {
            String cleaned = row.trim();
            if (cleaned.isBlank() || cleaned.toLowerCase(Locale.ROOT).contains("numero")) {
                continue;
            }
            String[] cells = cleaned.split("[,;|]");
            if (cells.length >= 4) {
                int index = (int) parseDouble(cells[0], 0d) - 1;
                if (index >= 0 && index < draft.getLines().size()) {
                    InvoiceLine line = draft.getLines().get(index);
                    line.setSalePriceQmin(parseDouble(cells[1], 1d));
                    line.setSalePriceQmax(parseDouble(cells[2], 999999d));
                    line.setSalePrice(parseDouble(cells[3], line.getSalePrice() == null ? 0d : line.getSalePrice()));
                    if (cells.length >= 5 && !cells[4].trim().isBlank()) {
                        line.setSaleCurrency(cells[4].trim().toUpperCase(Locale.ROOT));
                    }
                }
                continue;
            }
            GratienTools.NaturalLanguagePriceResult nl = GratienTools.parseNaturalLanguagePrice(cleaned);
            if (nl != null && nl.index >= 0 && nl.index < draft.getLines().size()) {
                InvoiceLine line = draft.getLines().get(nl.index);
                line.setSalePriceQmin(nl.qmin);
                line.setSalePriceQmax(nl.qmax);
                line.setSalePrice(nl.price);
                line.setSaleCurrency(nl.currency);
            }
        }
    }

    private boolean hasImage(List<File> attachments) {
        return attachments != null && attachments.stream().anyMatch(this::isImage);
    }

    private boolean isImage(File file) {
        String name = file == null ? "" : file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp") || name.endsWith(".bmp");
    }

    private Content imageContent(File file) throws Exception {
        if (!isImage(file)) {
            return null;
        }
        String lower = file.getName().toLowerCase(Locale.ROOT);
        String mime = lower.endsWith(".png") ? "image/png" : lower.endsWith(".webp") ? "image/webp" : "image/jpeg";
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

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String tableCell(String value) {
        return safe(value, "-").replace("|", "\\|").replace("\n", " ");
    }
}
