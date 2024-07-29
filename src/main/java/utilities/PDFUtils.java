/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 *
 * @author eroot
 */
public class PDFUtils {

    PDDocument document;
    PDPageContentStream contentStream;
    private int[] rightAlignedCols;
    int[] colWidths;
    int cellHeight;
    int xPos;
    int yPos;
    int xInitPos;
    int colPos = 0;
    PDFont font;
    float fontSize;
    Color color;

    public PDFUtils(PDDocument document, PDPageContentStream contentStream) {
        this.document = document;
        this.contentStream = contentStream;
    }

    public PDFUtils() {
    }

    public void addTextLine(String text, int xPos, int yPos, PDFont font, float fontSize, Color color) {
        try {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.setNonStrokingColor(color); 
            contentStream.newLineAtOffset(xPos, yPos);
            contentStream.showText(text);
            contentStream.endText();
            contentStream.moveTo(0, 0);
           
        } catch (IOException ex) {
            Logger.getLogger(PDFUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addTextLine(String[] texts, float leading, int xPos, int yPos, PDFont font, float fontSize, Color color) {
        try {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.setNonStrokingColor(color);
            contentStream.setLeading(leading);
            contentStream.newLineAtOffset(xPos, yPos);
            for (String text : texts) {
                contentStream.showText(text);
                contentStream.newLine();
            }
            contentStream.endText();
            contentStream.moveTo(0, 0);
        } catch (IOException ex) {
            Logger.getLogger(PDFUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
        return font.getStringWidth(text) / 10000 * fontSize;
    }

    public void addTable(int[] colWidths, int cellHeight, int xPos, int yPos) {
        this.cellHeight = cellHeight;
        this.colWidths = colWidths;
        this.xPos = xPos;
        this.yPos = yPos;
        this.xInitPos = xPos;
    }

    public void setFont(PDFont font, float fontSize, Color fontColor) {
        this.font = font;
        this.fontSize = fontSize;
        this.color = fontColor;
    }

    public void addCell(String text, Color fillColor) {
        try {
            contentStream.setStrokingColor(1f);
            if (fillColor != null) {
                contentStream.setNonStrokingColor(fillColor);
            }
            contentStream.addRect(xPos, yPos, colWidths[colPos], cellHeight);
            if (fillColor == null) {
                contentStream.stroke();
            } else {
                contentStream.fillAndStroke();
            }

            contentStream.beginText();
            contentStream.setNonStrokingColor(color);
            if (isRightAligned(colPos)) {
                float fwidth = font.getStringWidth(text) / 1000 * fontSize;
                contentStream.newLineAtOffset(xPos + colWidths[colPos] - 20 - fwidth, yPos + 10);
            } else {
                contentStream.newLineAtOffset(xPos + 20, yPos + 10);
            }
            contentStream.showText(text);
            contentStream.endText();
            xPos = xPos + colWidths[colPos];
            colPos++;
            if(colPos==colWidths.length){
                colPos=0;
                xPos=xInitPos;
                yPos-=cellHeight;
            }
        } catch (IOException ex) {
            Logger.getLogger(PDFUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Put columns position that concern right alignment text start by 0 as
     * first column have to be invoked before invoking addCell method
     *
     * @param rightAlignedColsPos
     */
    public void setRightAlignedColumns(int[] rightAlignedColsPos) {
        this.rightAlignedCols = rightAlignedColsPos;
    }

    private boolean isRightAligned(int colPos) {
        for (int rightAlignedCol : rightAlignedCols) {
            if (rightAlignedCol == colPos) {
                return true;
            }
        }
        return false;
    }
}
