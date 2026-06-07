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
            
            // Build dynamic table column widths
            boolean includeImmobilisationColumns = hasImmobilisationColumns(rows);
            int dataColumnCount = dataHeaders.size() + (includeImmobilisationColumns ? 3 : 0);
            int[] table;
            if (includeImmobilisationColumns && dataHeaders.size() == 5) {
                table = new int[]{32, 130, 110, 48, 48, 48, 48, 48, 55, 55, 55};
            } else if (includeImmobilisationColumns) {
                table = new int[]{35, 155, 125, 55, 55, 55, 55, 55, 55, 55};
            } else if (dataHeaders.size() == 5) {
                table = new int[]{40, 210, 160, 65, 65, 65, 65, 65};
            } else if (dataHeaders.size() == 4) {
                table = new int[]{40, 220, 200, 65, 65, 65, 65};
            } else {
                table = new int[]{40, 230, 210, 80, 80, 80};
            }

            int[] rightAligned = new int[dataColumnCount];
            for (int i = 0; i < dataColumnCount; i++) {
                rightAligned[i] = 3 + i;
            }

            pdf.addTable(table, 24, 25, pageH - 240);
            pdf.setFont(normal, 9, Color.WHITE);
            pdf.setRightAlignedColumns(rightAligned);
            pdf.addCell("Code", primary);
            pdf.addCell("Rubrique", primary);
            pdf.addCell("Nature", primary);
            for (String header : dataHeaders) {
                pdf.addCell(header, primary);
            }
            if (includeImmobilisationColumns) {
                pdf.addCell("Brut immo", primary);
                pdf.addCell("Amort.", primary);
                pdf.addCell("Net immo", primary);
            }

            pdf.setFont(normal, 8, Color.BLACK);
            int lines = 0;
            int linePerPage = 18;
            for (FinancialStatementRow row : rows) {
                if (lines == linePerPage) {
                    contentStream.close();
                    page = new PDPage(landscape);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    pdf = new PDFUtils(document, contentStream);
                    drawHeader(document, contentStream, pdf, entreprise, title, subtitle, normal, bold, primary, pageW,
                            pageH);
                    pdf.addTable(table, 24, 25, pageH - 240);
                    pdf.setFont(normal, 9, Color.WHITE);
                    pdf.setRightAlignedColumns(rightAligned);
                    pdf.addCell("Code", primary);
                    pdf.addCell("Rubrique", primary);
                    pdf.addCell("Nature", primary);
                    for (String header : dataHeaders) {
                        pdf.addCell(header, primary);
                    }
                    if (includeImmobilisationColumns) {
                        pdf.addCell("Brut immo", primary);
                        pdf.addCell("Amort.", primary);
                        pdf.addCell("Net immo", primary);
                    }
                    pdf.setFont(normal, 8, Color.BLACK);
                    lines = 0;
                }

                Color fill = row.isSectionHeader() ? new Color(220, 244, 252) : gray;
                if (row.isTotalLine()) {
                    fill = new Color(210, 232, 240);
                }
                pdf.setRightAlignedColumns(rightAligned);
                pdf.addCell(fit(row.getCode(), bold, 8, 30), fill);
                pdf.addCell(fit(row.getRubrique(), row.isTotalLine() || row.isSectionHeader() ? bold : normal, 8, 210), fill);
                pdf.addCell(fit(row.getNature(), normal, 8, 200), fill);
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
