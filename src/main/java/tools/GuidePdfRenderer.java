package tools;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Rend simple Markdown vers un PDF respectant la charte graphique Kazisafe
 * (couleur principale {@code #44cef5}). Utilise PDFBox 3 et des polices
 * embarquees (DejaVu pour les langues latines, KacstOne pour l'arabe,
 * Lohit-Devanagari pour l'hindi) avec repli sur Helvetica.
 */
public final class GuidePdfRenderer {

    /** Couleur principale de la charte Kazisafe. */
    public static final Color PRIMARY = new Color(0x44, 0xCE, 0xF5);
    private static final Color TEXT_DARK = new Color(0x33, 0x33, 0x33);
    private static final Color TEXT_GRAY = new Color(0x5A, 0x5A, 0x5A);
    private static final Color WHITE = Color.WHITE;
    private static final Color CODE_BG = new Color(0xF0, 0xF4, 0xF7);
    private static final Color GRID = new Color(0xBB, 0xD6, 0xE0);

    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float LEFT = 48f;
    private static final float RIGHT = 48f;
    private static final float TOP = 50f;
    private static final float BOTTOM = 42f;
    private static final float CONTENT_W = PAGE_W - LEFT - RIGHT;

    private static final Pattern INLINE = Pattern.compile(
            "(\\*\\*.+?\\*\\*|\\*[^*\\s][^*]*\\*|`[^`]+`)");
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern RULE = Pattern.compile("^(-{3,}|\\*{3,}|_{3,})\\s*$");
    private static final Pattern BULLET = Pattern.compile("^(?:(\\d+)\\.|[-*])\\s+(.*)$");
    private static final Pattern TABLE_SEP = Pattern.compile("^\\s*\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)*\\|?\\s*$");

    private GuidePdfRenderer() {
    }

    enum Style { NORMAL, BOLD, ITALIC, BOLD_ITALIC, CODE }

    static final class Seg {
        final String text;
        final Style style;
        Seg(String text, Style style) {
            this.text = text;
            this.style = style;
        }
    }

    static final class Word {
        final String text;
        final Style style;
        Word(String text, Style style) {
            this.text = text;
            this.style = style;
        }
    }

    static final class WrappedLine {
        final List<Word> words;
        WrappedLine(List<Word> words) {
            this.words = words;
        }
    }

    sealed interface Block {}
    record Heading(int level, String text) implements Block {}
    record Para(List<Seg> segs) implements Block {}
    record Bullet(boolean ordered, List<List<Seg>> items) implements Block {}
    record Table(List<List<List<Seg>>> rows) implements Block {}
    record Hr() implements Block {}
    record Quote(List<Seg> segs) implements Block {}
    record CodeBlock(List<String> lines) implements Block {}

    static final class Fonts {
        final PDFont normal;
        final PDFont bold;
        final PDFont italic;
        final PDFont mono;
        final PDFont fallback;
        Fonts(PDFont normal, PDFont bold, PDFont italic, PDFont mono, PDFont fallback) {
            this.normal = normal;
            this.bold = bold;
            this.italic = italic;
            this.mono = mono;
            this.fallback = fallback == null ? normal : fallback;
        }
        PDFont fontFor(Style s) {
            return switch (s) {
                case BOLD -> bold;
                case ITALIC, BOLD_ITALIC -> italic;
                case CODE -> mono;
                default -> normal;
            };
        }
        /** Police de style si elle contient le caractere, sinon police de repli (ar/hi). */
        PDFont fontForChar(Style s, char c) {
            PDFont f = fontFor(s);
            return canEncode(f, c) ? f : fallback;
        }
        private static boolean canEncode(PDFont f, char c) {
            try {
                f.encode(String.valueOf(c));
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        float width(Style s, float size, String text) throws IOException {
            float w = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                w += fontForChar(s, c).getStringWidth(String.valueOf(c)) / 1000f * size;
            }
            return w;
        }
    }

    static final class PageWriter implements AutoCloseable {
        final PDDocument doc;
        final Fonts fonts;
        final String lang;
        PDPage page;
        PDPageContentStream cs;
        float y;
        int pageNumber;

        PageWriter(PDDocument doc, Fonts fonts, String lang) {
            this.doc = doc;
            this.fonts = fonts;
            this.lang = lang;
        }

        void newPage() throws IOException {
            if (cs != null) {
                cs.close();
            }
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            pageNumber++;
            y = PAGE_H - TOP;
            drawBand();
        }

        private void drawBand() throws IOException {
            cs.addRect(0, PAGE_H - 36, PAGE_W, 36);
            cs.fill();
            drawText(LEFT, PAGE_H - 25, "Kazisafe - " + titleFor(lang), Style.NORMAL, 13f, WHITE);
            String pageNum = String.valueOf(pageNumber);
            drawText(PAGE_W - RIGHT - fonts.width(Style.NORMAL, 9f, pageNum), PAGE_H - 25, pageNum, Style.NORMAL, 9f, WHITE);
            cs.setNonStrokingColor(PRIMARY);
            cs.addRect(0, PAGE_H - 40, PAGE_W, 4);
            cs.fill();
        }

        void footer() throws IOException {
            drawText(LEFT, 24, "Kazisafe - " + titleFor(lang), Style.NORMAL, 8f, TEXT_GRAY);
            String pageNum = pageNumber + " / " + pageNumber;
            drawText(PAGE_W - RIGHT - fonts.width(Style.NORMAL, 8f, pageNum), 24, pageNum, Style.NORMAL, 8f, TEXT_GRAY);
        }

        void ensure(float needed) throws IOException {
            if (y - needed < BOTTOM) {
                footer();
                newPage();
            }
        }

        /** Dessine un texte en basculant de police par caractere (repli ar/hi). */
        void drawText(float x, float baseline, String text, Style style, float size, Color color) throws IOException {
            cs.beginText();
            cs.newLineAtOffset(x, baseline);
            int i = 0;
            while (i < text.length()) {
                char c = text.charAt(i);
                PDFont f = fonts.fontForChar(style, c);
                StringBuilder run = new StringBuilder();
                while (i < text.length() && fonts.fontForChar(style, text.charAt(i)) == f) {
                    run.append(text.charAt(i));
                    i++;
                }
                cs.setFont(f, size);
                cs.setNonStrokingColor(style == Style.CODE ? TEXT_DARK : color);
                cs.showText(run.toString());
            }
            cs.endText();
        }

        void drawLine(float x, float yBaseline, WrappedLine line, float size, Color color) throws IOException {
            float cx = x;
            float space = fonts.width(Style.NORMAL, size, " ");
            for (Word w : line.words) {
                drawText(cx, yBaseline, w.text, w.style, size, color);
                cx += fonts.width(w.style, size, w.text);
                cx += space;
            }
        }

        @Override
        public void close() throws IOException {
            if (cs != null) {
                footer();
                cs.close();
                cs = null;
            }
        }
    }

    // ------------------------------------------------------------------
    // API publique
    // ------------------------------------------------------------------

    public static void render(String lang, String markdown, File out) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            Fonts fonts = loadFonts(doc, lang);
            List<Block> blocks = parse(markdown);
            try (PageWriter pw = new PageWriter(doc, fonts, lang)) {
                pw.newPage();
                for (Block b : blocks) {
                    renderBlock(pw, b);
                }
            }
            doc.save(out);
        }
    }

    // ------------------------------------------------------------------
    // Police
    // ------------------------------------------------------------------

    private static Fonts loadFonts(PDDocument doc, String lang) {
        boolean script = "ar".equals(lang) || "hi".equals(lang);
        PDFont normal = loadTtf(doc, resourceFor(lang));
        PDFont bold = script ? normal : loadTtf(doc, "/fonts/DejaVuSans-Bold.ttf");
        PDFont mono = loadTtf(doc, "/fonts/DejaVuSansMono.ttf");
        PDFont fallback = script ? loadTtf(doc, "/fonts/DejaVuSans.ttf") : normal;
        if (normal == null) {
            return new Fonts(new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                    new PDType1Font(Standard14Fonts.FontName.COURIER), null);
        }
        return new Fonts(normal, bold == null ? normal : bold, normal, mono == null ? normal : mono, fallback);
    }

    private static String resourceFor(String lang) {
        return switch (lang == null ? "fr" : lang) {
            case "ar" -> "/fonts/KacstOne.ttf";
            case "hi" -> "/fonts/LohitDevanagari.ttf";
            default -> "/fonts/DejaVuSans.ttf";
        };
    }

    private static PDFont loadTtf(PDDocument doc, String resource) {
        if (resource == null) {
            return null;
        }
        try (InputStream in = GuidePdfRenderer.class.getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }
            return PDType0Font.load(doc, in, true);
        } catch (IOException ex) {
            return null;
        }
    }

    private static String titleFor(String lang) {
        return switch (lang == null ? "fr" : lang) {
            case "en" -> "User Guide";
            case "sw" -> "Mwongozo wa Matumizi";
            case "ln" -> "Buku ya kosalela";
            case "rw" -> "Umuyoboro w'Igikoresho";
            case "ar" -> "دليل الاستخدام";
            case "hi" -> "उपयोग गाइड";
            default -> "Guide d'utilisation";
        };
    }

    // ------------------------------------------------------------------
    // Analyse du Markdown
    // ------------------------------------------------------------------

    static List<Block> parse(String md) {
        List<Block> blocks = new ArrayList<>();
        String[] raw = md.split("\r?\n");
        int i = 0;
        List<String> para = new ArrayList<>();
        while (i < raw.length) {
            String t = raw[i].strip();
            if (t.isEmpty()) {
                i++;
                continue;
            }
            if (t.startsWith("|") && i + 1 < raw.length && TABLE_SEP.matcher(raw[i + 1].strip()).matches()) {
                flushPara(blocks, para);
                List<List<String>> rows = new ArrayList<>();
                rows.add(splitRow(t));
                i += 2;
                while (i < raw.length && raw[i].strip().startsWith("|")) {
                    rows.add(splitRow(raw[i].strip()));
                    i++;
                }
                blocks.add(toTable(rows));
                continue;
            }
            Matcher h = HEADING.matcher(t);
            if (h.matches()) {
                flushPara(blocks, para);
                blocks.add(new Heading(h.group(1).length(), h.group(2).strip()));
                i++;
                continue;
            }
            if (RULE.matcher(t).matches()) {
                flushPara(blocks, para);
                blocks.add(new Hr());
                i++;
                continue;
            }
            if (t.startsWith("```")) {
                flushPara(blocks, para);
                List<String> code = new ArrayList<>();
                i++;
                while (i < raw.length && !raw[i].strip().startsWith("```")) {
                    code.add(raw[i]);
                    i++;
                }
                i++;
                blocks.add(new CodeBlock(code));
                continue;
            }
            if (t.startsWith(">")) {
                flushPara(blocks, para);
                List<String> q = new ArrayList<>();
                while (i < raw.length && raw[i].strip().startsWith(">")) {
                    q.add(raw[i].strip().substring(1).strip());
                    i++;
                }
                blocks.add(new Quote(parseInline(String.join(" ", q))));
                continue;
            }
            Matcher b = BULLET.matcher(t);
            if (b.matches()) {
                flushPara(blocks, para);
                boolean ordered = b.group(1) != null;
                List<List<Seg>> items = new ArrayList<>();
                while (i < raw.length) {
                    String lt = raw[i].strip();
                    if (lt.isEmpty()) {
                        i++;
                        continue;
                    }
                    Matcher b2 = BULLET.matcher(lt);
                    if (b2.matches()) {
                        items.add(parseInline(b2.group(2)));
                        i++;
                    } else {
                        break;
                    }
                }
                blocks.add(new Bullet(ordered, items));
                continue;
            }
            para.add(t);
            i++;
        }
        flushPara(blocks, para);
        return blocks;
    }

    private static void flushPara(List<Block> blocks, List<String> para) {
        if (!para.isEmpty()) {
            blocks.add(new Para(parseInline(String.join(" ", para))));
            para.clear();
        }
    }

    private static List<String> splitRow(String row) {
        String s = row.strip();
        if (s.startsWith("|")) {
            s = s.substring(1);
        }
        if (s.endsWith("|")) {
            s = s.substring(0, s.length() - 1);
        }
        List<String> cells = new ArrayList<>();
        for (String c : s.split("\\|")) {
            cells.add(c.strip());
        }
        return cells;
    }

    private static Table toTable(List<List<String>> rows) {
        List<List<List<Seg>>> out = new ArrayList<>();
        for (List<String> row : rows) {
            List<List<Seg>> cells = new ArrayList<>();
            for (String cell : row) {
                cells.add(parseInline(cell));
            }
            out.add(cells);
        }
        return new Table(out);
    }

    static List<Seg> parseInline(String text) {
        List<Seg> out = new ArrayList<>();
        Matcher m = INLINE.matcher(text);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                out.add(new Seg(text.substring(last, m.start()), Style.NORMAL));
            }
            String tok = m.group(1);
            if (tok.startsWith("**")) {
                out.add(new Seg(tok.substring(2, tok.length() - 2), Style.BOLD));
            } else if (tok.startsWith("`")) {
                out.add(new Seg(tok.substring(1, tok.length() - 1), Style.CODE));
            } else {
                out.add(new Seg(tok.substring(1, tok.length() - 1), Style.ITALIC));
            }
            last = m.end();
        }
        if (last < text.length()) {
            out.add(new Seg(text.substring(last), Style.NORMAL));
        }
        return out;
    }

    static List<WrappedLine> wrapLines(List<Seg> segs, Fonts fonts, float size, float maxWidth) throws IOException {
        List<Word> words = new ArrayList<>();
        for (Seg s : segs) {
            String[] parts = s.text.split("\\s+");
            for (String p : parts) {
                if (!p.isEmpty()) {
                    words.add(new Word(p, s.style));
                }
            }
        }
        List<WrappedLine> lines = new ArrayList<>();
        List<Word> cur = new ArrayList<>();
        float curW = 0;
        float space = fonts.width(Style.NORMAL, size, " ");
        for (Word w : words) {
            float ww = fonts.width(w.style, size, w.text);
            float add = cur.isEmpty() ? 0 : space;
            if (curW + add + ww > maxWidth && !cur.isEmpty()) {
                lines.add(new WrappedLine(cur));
                cur = new ArrayList<>();
                curW = 0;
            }
            curW += add + ww;
            cur.add(w);
        }
        if (!cur.isEmpty()) {
            lines.add(new WrappedLine(cur));
        }
        if (lines.isEmpty()) {
            lines.add(new WrappedLine(List.of()));
        }
        return lines;
    }

    // ------------------------------------------------------------------
    // Rendu des blocs
    // ------------------------------------------------------------------

    private static float lineHeight(float size) {
        return size * 1.4f;
    }

    private static void renderBlock(PageWriter pw, Block b) throws IOException {
        if (b instanceof Heading h) {
            renderHeading(pw, h);
        } else if (b instanceof Para p) {
            renderPara(pw, p.segs, 10.5f, TEXT_DARK, 6f, 4f);
        } else if (b instanceof Bullet bl) {
            renderBullet(pw, bl);
        } else if (b instanceof Table tb) {
            renderTable(pw, tb);
        } else if (b instanceof Hr) {
            pw.ensure(16);
            pw.y -= 8;
            pw.cs.setStrokingColor(PRIMARY);
            pw.cs.setLineWidth(1.2f);
            pw.cs.moveTo(LEFT, pw.y);
            pw.cs.lineTo(PAGE_W - RIGHT, pw.y);
            pw.cs.stroke();
            pw.y -= 8;
        } else if (b instanceof Quote q) {
            renderQuote(pw, q);
        } else if (b instanceof CodeBlock cb) {
            renderCode(pw, cb);
        }
    }

    private static void renderHeading(PageWriter pw, Heading h) throws IOException {
        float size = switch (h.level) {
            case 1 -> 19f;
            case 2 -> 14.5f;
            case 3 -> 12f;
            case 4 -> 11f;
            case 5 -> 10.5f;
            default -> 10f;
        };
        float before = h.level == 1 ? 18f : h.level == 2 ? 14f : 9f;
        float after = h.level == 1 ? 8f : h.level == 2 ? 6f : 4f;
        pw.ensure(before + size * 1.3f + after + (h.level <= 2 ? 6f : 0));
        pw.y -= before;
        List<WrappedLine> lines = wrapLines(List.of(new Seg(h.text, h.level <= 2 ? Style.BOLD : Style.BOLD)), pw.fonts, size, CONTENT_W);
        float lh = lineHeight(size);
        for (WrappedLine l : lines) {
            pw.drawLine(LEFT, pw.y, l, size, PRIMARY);
            pw.y -= lh;
        }
        pw.y += lh;
        if (h.level <= 2) {
            pw.cs.setNonStrokingColor(PRIMARY);
            pw.cs.addRect(LEFT, pw.y, h.level == 1 ? CONTENT_W : Math.min(220f, CONTENT_W), 1.6f);
            pw.cs.fill();
            pw.y -= 5f;
        }
        pw.y -= after;
    }

    private static void renderPara(PageWriter pw, List<Seg> segs, float size, Color color, float before, float after) throws IOException {
        List<WrappedLine> lines = wrapLines(segs, pw.fonts, size, CONTENT_W);
        float lh = lineHeight(size);
        pw.ensure(before + lines.size() * lh + after);
        pw.y -= before;
        for (WrappedLine l : lines) {
            pw.drawLine(LEFT, pw.y, l, size, color);
            pw.y -= lh;
        }
        pw.y -= after;
    }

    private static void renderBullet(PageWriter pw, Bullet b) throws IOException {
        float size = 10.5f;
        float lh = lineHeight(size);
        float markerW = 16f;
        int idx = 1;
        for (List<Seg> item : b.items) {
            List<WrappedLine> lines = wrapLines(item, pw.fonts, size, CONTENT_W - markerW);
            pw.ensure(lh);
            pw.y -= 3f;
            float firstBase = pw.y;
            if (b.ordered) {
                String num = idx + ".";
                float numW = pw.fonts.width(Style.BOLD, size, num);
                pw.drawText(LEFT, firstBase, num, Style.BOLD, size, PRIMARY);
                pw.drawLine(LEFT + numW + 4f, firstBase, lines.get(0), size, TEXT_DARK);
            } else {
                pw.cs.setNonStrokingColor(PRIMARY);
                pw.cs.addRect(LEFT + 2f, firstBase - size * 0.30f, 5f, 5f);
                pw.cs.fill();
                pw.drawLine(LEFT + markerW, firstBase, lines.get(0), size, TEXT_DARK);
            }
            pw.y = firstBase;
            pw.y -= lh;
            for (int li = 1; li < lines.size(); li++) {
                pw.drawLine(LEFT + markerW, pw.y, lines.get(li), size, TEXT_DARK);
                pw.y -= lh;
            }
            idx++;
        }
        pw.y -= 4f;
    }

    private static void renderQuote(PageWriter pw, Quote q) throws IOException {
        float size = 10.5f;
        List<WrappedLine> lines = wrapLines(q.segs, pw.fonts, size, CONTENT_W - 16f);
        float lh = lineHeight(size);
        float h = lines.size() * lh + 8f;
        pw.ensure(h);
        pw.y -= 4f;
        float top = pw.y;
        float bottom = top - h + 8f;
        pw.cs.setNonStrokingColor(PRIMARY);
        pw.cs.addRect(LEFT, bottom, 3f, top - bottom);
        pw.cs.fill();
        for (WrappedLine l : lines) {
            pw.drawLine(LEFT + 12f, pw.y, l, size, TEXT_GRAY);
            pw.y -= lh;
        }
        pw.y -= 4f;
    }

    private static void renderCode(PageWriter pw, CodeBlock cb) throws IOException {
        float size = 9f;
        float lh = lineHeight(size);
        float h = cb.lines.size() * lh + 12f;
        pw.ensure(h);
        pw.y -= 6f;
        float top = pw.y;
        pw.cs.setNonStrokingColor(CODE_BG);
        pw.cs.addRect(LEFT, top - h, CONTENT_W, h);
        pw.cs.fill();
        pw.y = top - 6f;
        for (String codeLine : cb.lines) {
            pw.cs.setFont(pw.fonts.mono, size);
            pw.cs.setNonStrokingColor(TEXT_DARK);
            pw.cs.beginText();
            pw.cs.newLineAtOffset(LEFT + 8f, pw.y);
            pw.cs.showText(codeLine.isBlank() ? " " : codeLine);
            pw.cs.endText();
            pw.y -= lh;
        }
        pw.y -= 6f;
    }

    private static void renderTable(PageWriter pw, Table tb) throws IOException {
        if (tb.rows().isEmpty()) {
            return;
        }
        int cols = tb.rows().get(0).size();
        if (cols == 0) {
            return;
        }
        float[] colW = columnWidths(pw, tb, cols);
        float pad = 5f;
        float size = 9.5f;
        float lh = lineHeight(size);
        float total = CONTENT_W;
        float top = pw.y - 8f;
        pw.ensure(10f + tableHeight(pw, tb, colW, pad, size, lh));
        pw.y = top;
        float rowTop = pw.y;
        for (int r = 0; r < tb.rows().size(); r++) {
            List<List<Seg>> row = tb.rows().get(r);
            float h = rowHeight(pw, row, colW, pad, size, lh);
            if (pw.y - h < BOTTOM) {
                pw.newPage();
                rowTop = pw.y;
            }
            boolean header = r == 0;
            if (header) {
                pw.cs.setNonStrokingColor(PRIMARY);
                pw.cs.addRect(LEFT, pw.y - h, total, h);
                pw.cs.fill();
            }
            float cellX = LEFT;
            for (int c = 0; c < row.size(); c++) {
                List<WrappedLine> cellLines = wrapLines(row.get(c), pw.fonts, size, colW[Math.min(c, colW.length - 1)] - 2 * pad);
                Color color = header ? WHITE : TEXT_DARK;
                float yy = pw.y - pad - lh;
                for (WrappedLine l : cellLines) {
                    if (yy > pw.y - h) {
                        pw.drawLine(cellX + pad, yy, l, size, color);
                    }
                    yy -= lh;
                }
                cellX += colW[Math.min(c, colW.length - 1)];
            }
            pw.cs.setStrokingColor(header ? PRIMARY : GRID);
            pw.cs.setLineWidth(header ? 1.2f : 0.7f);
            pw.cs.moveTo(LEFT, pw.y);
            pw.cs.lineTo(LEFT + total, pw.y);
            pw.cs.stroke();
            pw.y -= h;
        }
        pw.cs.setStrokingColor(GRID);
        pw.cs.setLineWidth(0.7f);
        float bx = LEFT;
        for (int c = 0; c <= cols; c++) {
            pw.cs.moveTo(bx, rowTop);
            pw.cs.lineTo(bx, pw.y);
            pw.cs.stroke();
            bx += (c < colW.length ? colW[c] : 0);
        }
        pw.y -= 8f;
    }

    private static float[] columnWidths(PageWriter pw, Table tb, int cols) throws IOException {
        float[] maxes = new float[cols];
        for (List<List<Seg>> row : tb.rows()) {
            for (int c = 0; c < Math.min(row.size(), cols); c++) {
                String plain = row.get(c).stream().map(s -> s.text).reduce("", (a, b) -> a + b);
                float w = pw.fonts.width(Style.NORMAL, 9.5f, plain) + 12f;
                if (w > maxes[c]) {
                    maxes[c] = w;
                }
            }
        }
        float sum = 0;
        for (float m : maxes) {
            sum += m;
        }
        float[] out = new float[cols];
        if (sum <= 0) {
            for (int c = 0; c < cols; c++) {
                out[c] = CONTENT_W / cols;
            }
            return out;
        }
        for (int c = 0; c < cols; c++) {
            out[c] = Math.max(40f, maxes[c] / sum * CONTENT_W);
        }
        return out;
    }

    private static float rowHeight(PageWriter pw, List<List<Seg>> row, float[] colW, float pad, float size, float lh) throws IOException {
        float max = 0;
        for (int c = 0; c < row.size(); c++) {
            int n = wrapLines(row.get(c), pw.fonts, size, colW[Math.min(c, colW.length - 1)] - 2 * pad).size();
            max = Math.max(max, n * lh + 2 * pad);
        }
        return Math.max(max, lh + 2 * pad);
    }

    private static float tableHeight(PageWriter pw, Table tb, float[] colW, float pad, float size, float lh) throws IOException {
        float h = 0;
        for (List<List<Seg>> row : tb.rows()) {
            h += rowHeight(pw, row, colW, pad, size, lh);
        }
        return h;
    }
}
