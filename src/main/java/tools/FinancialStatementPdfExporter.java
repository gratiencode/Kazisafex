package tools;

import com.endeleya.kazisafex.MainuiController;
import data.Entreprise;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import utilities.PDFUtils;

public final class FinancialStatementPdfExporter {

    private FinancialStatementPdfExporter() {
    }

    public static File export(Entreprise entreprise, String title, LocalDate start, LocalDate end,
            List<FinancialStatementRow> rows, List<String> dataHeaders) throws IOException {
        dataHeaders = dataHeaders == null ? List.of() : dataHeaders;
        File output;
        try (PDDocument document = new PDDocument()) {
            PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            Color primary = new Color(68, 206, 245);
            Color gray = new Color(238, 238, 238);
            DateTimeFormatter periodFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String subtitle = "Période : " + periodFormat.format(start) + " au " + periodFormat.format(end);
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());

            PDPage page = new PDPage(landscape);
            document.addPage(page);
            int pageW = (int) landscape.getWidth();
            int pageH = (int) landscape.getHeight();
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDFUtils pdf = new PDFUtils(document, contentStream);

            drawHeader(document, contentStream, pdf, entreprise, title, subtitle, normal, bold, primary, pageW, pageH);
            drawFooter(contentStream, pdf, normal, primary, pageW, pageH);

            boolean includeImmobilisationColumns = hasImmobilisationColumns(rows);
            int dataColumnCount = dataHeaders.size() + (includeImmobilisationColumns ? 3 : 0);
            int[] table = tableWidths(dataHeaders.size(), includeImmobilisationColumns);
            float bodyFontSize = fittingBodyFontSize(normal, table, rows, dataHeaders.size(),
                    includeImmobilisationColumns);
            float headerFontSize = bodyFontSize;

            int[] rightAligned = new int[dataColumnCount];
            for (int i = 0; i < dataColumnCount; i++) {
                rightAligned[i] = 2 + i;
            }

            pdf.addTable(table, 30, 25, pageH - 240);
            pdf.setFont(normal, headerFontSize, Color.WHITE);
            pdf.setRightAlignedColumns(rightAligned);
            addHeader(pdf, primary, dataHeaders, includeImmobilisationColumns);

            pdf.setFont(normal, bodyFontSize, Color.BLACK);
            int lines = 0;
            int linePerPage = 10;
            for (FinancialStatementRow row : rows) {
                if (lines == linePerPage) {
                    contentStream.close();
                    page = new PDPage(landscape);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    pdf = new PDFUtils(document, contentStream);
                    drawHeader(document, contentStream, pdf, entreprise, title, subtitle, normal, bold, primary, pageW,
                            pageH);
                    drawFooter(contentStream, pdf, normal, primary, pageW, pageH);
                    pdf.addTable(table, 30, 25, pageH - 240);
                    pdf.setFont(normal, headerFontSize, Color.WHITE);
                    pdf.setRightAlignedColumns(rightAligned);
                    addHeader(pdf, primary, dataHeaders, includeImmobilisationColumns);
                    pdf.setFont(normal, bodyFontSize, Color.BLACK);
                    lines = 0;
                }

                Color fill = row.isSectionHeader() ? new Color(220, 244, 252) : gray;
                if (row.isTotalLine()) {
                    fill = new Color(210, 232, 240);
                }
                pdf.setRightAlignedColumns(rightAligned);
                pdf.addCell(fit(row.getCode(), bold, bodyFontSize, table[0] - 10), fill);
                pdf.addCell(wrapText(row.getRubrique(), row.isTotalLine() || row.isSectionHeader() ? bold : normal,
                        bodyFontSize, table[1] - 20, 2), fill);
                pdf.addCell(formatAmount(row.getAmountN()), fill);
                pdf.addCell(formatAmount(row.getAmountN1()), fill);
                pdf.addCell(formatAmount(row.getAmountN2()), fill);
                if (dataHeaders.size() >= 4) {
                    pdf.addCell(formatAmount(row.getAmountN3()), fill);
                }
                if (dataHeaders.size() >= 5) {
                    pdf.addCell(formatAmount(row.getAmountN4()), fill);
                }
                if (includeImmobilisationColumns) {
                    pdf.addCell(formatAmount(row.getGrossAmount()), fill);
                    pdf.addCell(formatAmount(row.getAmortizationAmount()), fill);
                    pdf.addCell(formatAmount(row.getNetAmount()), fill);
                }
                lines++;
            }

            contentStream.close();
            output = FileUtils.pointFile("financial-" + title.replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase()
                    + "-" + Constants.TIMESTAMPED_FORMAT.format(new java.util.Date()) + ".pdf");
            document.save(output);
        }
        return output;
    }

    private static void addHeader(PDFUtils pdf, Color primary, List<String> dataHeaders,
            boolean includeImmobilisationColumns) {
        pdf.addCell("Code", primary);
        pdf.addCell("Rubrique", primary);
        for (String header : dataHeaders) {
            pdf.addCell(header, primary);
        }
        if (includeImmobilisationColumns) {
            pdf.addCell("Brut immo", primary);
            pdf.addCell("Amort.", primary);
            pdf.addCell("Net immo", primary);
        }
    }

    private static int[] tableWidths(int headerCount, boolean includeImmobilisationColumns) {
        int dataColumnCount = headerCount + (includeImmobilisationColumns ? 3 : 0);
        int codeWidth = 50;
        int rubriqueWidth = includeImmobilisationColumns
                ? (headerCount >= 5 ? 250 : 285)
                : (headerCount >= 5 ? 360 : 390);
        int availableWidth = 792 - codeWidth - rubriqueWidth;
        int amountWidth = dataColumnCount == 0 ? availableWidth : Math.max(52, availableWidth / dataColumnCount);
        int[] table = new int[2 + dataColumnCount];
        table[0] = codeWidth;
        table[1] = rubriqueWidth;
        for (int i = 2; i < table.length; i++) {
            table[i] = amountWidth;
        }
        return table;
    }

    private static float fittingBodyFontSize(PDFont font, int[] table, List<FinancialStatementRow> rows,
            int headerCount, boolean includeImmobilisationColumns) throws IOException {
        float size = 10f;
        float minSize = 10f;
        for (FinancialStatementRow row : rows) {
            List<String> amounts = amountTexts(row, headerCount, includeImmobilisationColumns);
            for (int i = 0; i < amounts.size(); i++) {
                int columnIndex = 2 + i;
                if (columnIndex >= table.length) {
                    break;
                }
                float available = Math.max(20, table[columnIndex] - 24);
                float width = font.getStringWidth(amounts.get(i)) / 1000 * size;
                while (width > available && size > minSize) {
                    size -= 0.25f;
                    width = font.getStringWidth(amounts.get(i)) / 1000 * size;
                }
            }
        }
        return Math.max(minSize, size);
    }

    private static List<String> amountTexts(FinancialStatementRow row, int headerCount,
            boolean includeImmobilisationColumns) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Double> periodAmounts = new java.util.ArrayList<>();
        periodAmounts.add(row.getAmountN());
        periodAmounts.add(row.getAmountN1());
        periodAmounts.add(row.getAmountN2());
        periodAmounts.add(row.getAmountN3());
        periodAmounts.add(row.getAmountN4());
        for (int i = 0; i < headerCount && i < periodAmounts.size(); i++) {
            values.add(formatAmount(periodAmounts.get(i)));
        }
        if (includeImmobilisationColumns) {
            values.add(formatAmount(row.getGrossAmount()));
            values.add(formatAmount(row.getAmortizationAmount()));
            values.add(formatAmount(row.getNetAmount()));
        }
        return values;
    }

    private static void drawHeader(PDDocument document, PDPageContentStream contentStream, PDFUtils pdf,
            Entreprise entreprise, String title, String subtitle, PDFont normal, PDFont bold, Color primary, int pageW,
            int pageH) throws IOException {
        File logoFile = FileUtils.pointFile(entreprise == null ? "company.png" : entreprise.getUid() + ".png");
        InputStream fallback = null;
        if (!logoFile.exists()) {
            fallback = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            if (fallback != null) {
                logoFile = FileUtils.streamTofile(fallback);
            }
        }
        if (logoFile != null && logoFile.exists()) {
            try {
                PDImageXObject logo = PDImageXObject.createFromFile(logoFile.getPath(), document);
                contentStream.drawImage(logo, pageW - 110, pageH - 95, 70, 70);
            } catch (Exception ignored) {
            }
        }

        pdf.addTextLine(title, 25, pageH - 70, bold, 28, Color.DARK_GRAY);
        contentStream.setStrokingColor(primary);
        contentStream.setLineWidth(2);
        contentStream.moveTo(25, pageH - 86);
        contentStream.lineTo(pageW - 25, pageH - 86);
        contentStream.stroke();

        if (entreprise != null) {
            pdf.addTextLine(entreprise.getNomEntreprise(), 25, pageH - 125, normal, 16, Color.BLACK);
            pdf.addTextLine(new String[]{
                "Adresse : " + safe(entreprise.getAdresse()),
                "RCCM : " + safe(entreprise.getIdentification()),
                entreprise.getIdNat() == null ? "" : "ID-NAT : " + entreprise.getIdNat(),
                entreprise.getNumeroImpot() == null ? "" : "NIF : " + entreprise.getNumeroImpot()
            }, 14, 25, pageH - 138, normal, 12, Color.BLACK);
        }

        String generated = "Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        pdf.addTextLine(generated,
                (int) (pageW - normal.getStringWidth(generated) / 1000 * 12 - 32), pageH - 125, normal, 12,
                Color.BLACK);
        pdf.addTextLine(subtitle, 25, pageH - 200, bold, 12, Color.BLACK);
    }

    private static String fit(String text, PDFont font, float fontSize, int maxWidth) throws IOException {
        if (text == null) {
            return "";
        }
        String value = text.replace('\n', ' ').trim();
        if (font.getStringWidth(value) / 1000 * fontSize <= maxWidth) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            String candidate = builder + String.valueOf(c) + "...";
            if (font.getStringWidth(candidate) / 1000 * fontSize > maxWidth) {
                break;
            }
            builder.append(c);
        }
        return builder + "...";
    }

    private static void drawFooter(PDPageContentStream contentStream, PDFUtils pdf, PDFont normal, Color primary,
            int pageW, int pageH) throws IOException {
        Preferences preferences = Preferences.userNodeForPackage(SyncEngine.class);
        String userName = preferences.get("operator", preferences.get("uname", "Utilisateur"));
        String generated = "Établi par : " + PDFUtils.safeText(userName == null ? "" : userName) + "    le "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        pdf.addTextLine(generated, 25, 55, normal, 10, Color.DARK_GRAY);
        contentStream.setStrokingColor(primary);
        contentStream.setLineWidth(1);
        contentStream.moveTo(25, 42);
        contentStream.lineTo(300, 42);
        contentStream.stroke();
        pdf.addTextLine("Signature :", 25, 30, normal, 10, Color.DARK_GRAY);
    }

    private static String wrapText(String text, PDFont font, float fontSize, int maxWidth, int maxLines)
            throws IOException {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] words = text.replace('\n', ' ').trim().split("\\s+");
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (font.getStringWidth(candidate) / 1000 * fontSize <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if (current.length() > 0) {
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            } else {
                lines.add(fit(word, font, fontSize, maxWidth));
            }
            if (lines.size() == maxLines) {
                break;
            }
        }
        if (current.length() > 0 && lines.size() < maxLines) {
            lines.add(current.toString());
        }
        if (lines.size() > maxLines) {
            lines = new java.util.ArrayList<>(lines.subList(0, maxLines));
        }
        if (lines.isEmpty()) {
            return "";
        }
        if (lines.size() == maxLines && words.length > String.join(" ", lines).split("\\s+").length) {
            int last = lines.size() - 1;
            lines.set(last, fit(lines.get(last) + "...", font, fontSize, maxWidth));
        }
        return String.join("\n", lines);
    }

    private static String formatAmount(Double amount) {
        if (amount == null) {
            return "";
        }
        return Util.toPlain(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
    }

    private static boolean hasImmobilisationColumns(List<FinancialStatementRow> rows) {
        return rows.stream().anyMatch(row -> row.getGrossAmount() != null
                || row.getAmortizationAmount() != null || row.getNetAmount() != null);
    }

    private static String safe(String value) {
        return value == null ? "-" : value;
    }
}
