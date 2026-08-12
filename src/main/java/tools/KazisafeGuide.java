package tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Gestion des guides d'utilisation Kazisafe.
 *
 * <p>Les fichiers {@code GUIDE_XX.md} vivent en ressources ({@code /guides/}) et sont
 * copies au premier lancement dans {@code Media/ia/gratien}. Ils servent a deux fins:
 * source du PDF ouvert par le menu Aide, et base de connaissance de l'assistant
 * Gratien pour repondre aux questions d'utilisation, en detectant la langue de la
 * question (fallback: langue courante de l'application puis francais).</p>
 */
public final class KazisafeGuide {

    /** Dossier partage avec l'assistant Gratien (AGENT.md / USER.md). */
    public static final String GUIDE_DIR = MainUI.cPath("/Media/ia/gratien");

    /** Langues pour lesquelles un guide existe (identiques aux bundles de l'app). */
    public static final List<String> SUPPORTED = List.of("fr", "en", "sw", "ln", "rw", "ar", "hi");

    /** Taille maximale de guide injectee dans le contexte de Gratien. */
    private static final int MAX_KNOWLEDGE_CHARS = 9000;

    private static final Map<String, List<String>> MARKERS = Map.of(
            "fr", List.of("comment", "comment faire", "utiliser", "utilisation", "guide",
                    "aide", "manuel", "tutoriel", "fonctionne", "fonctionnement",
                    "etape", "etapes", "creer", "enregistrer"),
            "en", List.of("how", "how to", "guide", "help", "use", "using", "usage",
                    "manual", "tutorial", "works", "step", "steps", "create"),
            "sw", List.of("jinsi", "kutumia", "matumizi", "mwongozo", "usaidizi",
                    "msaada", "kufanya"),
            "ln", List.of("ndenge", "kosalela", "moyebi", "lisalisi", "losakola", "buku"),
            "rw", List.of("uburyo", "gukoresha", "umuyoboro", "uko", "ifashayigisha"),
            "ar", List.of("كيف", "استخدام", "دليل", "مساعدة", "طريقة", "تشغيل", "التعليمات"),
            "hi", List.of("कैसे", "उपयोग", "गाइड", "मदद", "करें", "करना", "जानकारी"));

    /** Salutations pures, a ne pas confondre avec une question d'utilisation. */
    private static final List<String> GREETINGS = List.of(
            "comment allez", "comment vas", "how are you", "salut", "bonjour", "bonsoir",
            "bonne nuit", "hello", "hey", "hi", "jambo", "hujambo", "mbote", "mwaramutse",
            "bienvenue", "مرحبا", "السلام عليكم", "اهلا", "नमस्ते", "नमस्कार");

    private static final List<String> STOPWORDS = List.of(
            "le", "la", "les", "un", "une", "des", "du", "de", "et", "est", "que", "qui",
            "pour", "dans", "avec", "sur", "par", "au", "aux", "ce", "cette", "ces", "je",
            "tu", "il", "elle", "on", "nous", "vous", "ils", "elles", "mon", "ma", "mes",
            "ton", "ta", "tes", "son", "sa", "ses", "notre", "votre", "leur", "leurs",
            "the", "a", "an", "and", "of", "to", "in", "for", "on", "with", "at", "by",
            "from", "as", "is", "are", "was", "were", "it", "this", "that", "these",
            "those", "my", "your", "his", "her", "our", "their", "i", "you", "he", "she",
            "we", "they", "me", "him", "us", "them", "ni", "ya", "wa", "na", "cha", "kwa",
            "za", "kutoka", "katika", "au", "hiyo", "hii", "li", "ma", "ba", "mu", "ko",
            "ku", "no", "mo", "yo", "ne", "ue", "cya", "cyo", "في", "من", "على", "و",
            "أو", "مع", "إلى", "أن", "هذا", "هذه", "उस", "एक", "और", "है", "हैं", "में",
            "से", "के", "को", "की", "का", "पर", "लिए", "यह");

    private KazisafeGuide() {
    }

    public static boolean isSupported(String lang) {
        return lang != null && SUPPORTED.contains(lang);
    }

    /** Langue courante de l'application, ramenee a une langue guidee. */
    public static String currentLang() {
        String lang = Preferences.userNodeForPackage(SyncEngine.class).get("lang", "fr");
        return isSupported(lang) ? lang : "fr";
    }

    /** Langue ramenee en minuscule si supportee, sinon "fr". */
    private static String normalizedLang(String lang) {
        return isSupported(lang) ? lang : "fr";
    }

    /** Code langue en majuscules (les ressources et fichiers disque utilisent GUIDE_XX.md). */
    private static String langUpper(String lang) {
        return normalizedLang(lang).toUpperCase(Locale.ROOT);
    }

    /** Emplacement du guide Markdown d'une langue dans Media/ia/gratien. */
    public static File guideFile(String lang) {
        return new File(GUIDE_DIR, "GUIDE_" + langUpper(lang) + ".md");
    }

    /**
     * Regeneres les guides {@code GUIDE_XX.md} dans Media/ia/gratien quand ils manquent
     * (par exemple apres suppression manuelle). Appele au demarrage.
     */
    public static void ensureGuides() {
        try {
            File dir = new File(GUIDE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (String l : SUPPORTED) {
                File target = guideFile(l);
                if (target.exists()) {
                    continue;
                }
                try (InputStream in = KazisafeGuide.class
                        .getResourceAsStream("/guides/GUIDE_" + l.toUpperCase(Locale.ROOT) + ".md")) {
                    if (in != null) {
                        Files.copy(in, target.toPath());
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    /** Contenu du guide d'une langue (depuis le disque puis les ressources). */
    public static String loadGuide(String lang) {
        String l = normalizedLang(lang);
        File f = guideFile(l);
        try {
            if (f.exists()) {
                return Files.readString(f.toPath(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
        try (InputStream in = KazisafeGuide.class
                .getResourceAsStream("/guides/GUIDE_" + l.toUpperCase(Locale.ROOT) + ".md")) {
            return in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * Detecte la langue d'un texte (parmi les langues guidees) par mots-cles.
     * Retourne {@code null} si aucune langue ne se degage.
     */
    public static String detectLanguage(String text) {
        if (text == null) {
            return null;
        }
        String low = text.toLowerCase(Locale.ROOT);
        String best = null;
        int bestScore = 0;
        for (Map.Entry<String, List<String>> e : MARKERS.entrySet()) {
            int score = 0;
            for (String m : e.getValue()) {
                if (low.contains(m)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = e.getKey();
            }
        }
        return best;
    }

    /** Une question porte-t-elle sur l'utilisation du logiciel ? */
    public static boolean isUsageQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String norm = normalize(question);
        for (String g : GREETINGS) {
            if (norm.equals(g)) {
                return false;
            }
            if (norm.startsWith(g)) {
                String rest = norm.substring(g.length()).strip();
                if (rest.isEmpty() || rest.matches("[,!.?]+|(vous|tu|wewe|ko|koko)?")) {
                    return false;
                }
            }
        }
        String low = question.toLowerCase(Locale.ROOT);
        for (List<String> list : MARKERS.values()) {
            for (String m : list) {
                if (low.contains(m)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Connaissance "guide d'utilisation" pour Gratien, dans la langue de la question
     * (repli: langue courante). Retourne une chaine vide si la question n'est pas une
     * question d'utilisation. Le contenu renvoye est limite a la section la plus
     * pertinente quand elle est identifiable, sinon au debut du guide.
     */
    public static String usageKnowledge(String question) {
        if (!isUsageQuestion(question)) {
            return "";
        }
        String lang = detectLanguage(question);
        if (!isSupported(lang)) {
            lang = currentLang();
        }
        String md = loadGuide(lang);
        if (md.isBlank()) {
            return "";
        }
        String section = relevantSection(question, md);
        String body = section.isEmpty() ? md : section;
        if (body.length() > MAX_KNOWLEDGE_CHARS) {
            body = body.substring(0, MAX_KNOWLEDGE_CHARS);
        }
        return "GUIDE D'UTILISATION KAZISAFE (" + lang.toUpperCase(Locale.ROOT) + "):\n"
                + "Reponds en te basant sur ce guide quand l'information s'y trouve.\n"
                + "---\n" + body;
    }

    /** Genere le PDF d'une langue depuis son guide Markdown. */
    public static File generatePdf(String lang) throws IOException {
        String l = isSupported(lang) ? lang : "fr";
        ensureGuides();
        File out = new File(GUIDE_DIR, "Kazisafe_Guide_" + l + ".pdf");
        GuidePdfRenderer.render(l, loadGuide(l), out);
        return out;
    }

    /** Genere puis ouvre le PDF du guide dans la langue courante. */
    public static void openGuidePdf() throws IOException {
        File pdf = generatePdf(currentLang());
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Ouverture du PDF non supportee sur cette plateforme: " + pdf);
        }
        Desktop.getDesktop().open(pdf);
    }

    // ------------------------------------------------------------------
    // Recherche de section pertinente
    // ------------------------------------------------------------------

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .strip();
    }

    private static List<String> tokenize(String norm) {
        List<String> out = new ArrayList<>();
        for (String w : norm.split(" ")) {
            if (w.length() >= 3 && !STOPWORDS.contains(w)) {
                out.add(w);
            }
        }
        return out;
    }

    private static List<String> splitSections(String md) {
        List<String> sections = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String line : md.split("\n")) {
            if (line.startsWith("## ")) {
                if (cur.length() > 0) {
                    sections.add(cur.toString());
                }
                cur = new StringBuilder(line).append('\n');
            } else {
                cur.append(line).append('\n');
            }
        }
        if (cur.length() > 0) {
            sections.add(cur.toString());
        }
        return sections;
    }

    private static String firstLine(String s) {
        int i = s.indexOf('\n');
        return i < 0 ? s : s.substring(0, i);
    }

    private static String bodyOf(String s) {
        int i = s.indexOf('\n');
        return i < 0 ? "" : s.substring(i + 1);
    }

    /** Section du guide dont le titre/corps correspond le mieux a la question. */
    private static String relevantSection(String question, String md) {
        List<String> words = tokenize(normalize(question));
        if (words.isEmpty()) {
            return "";
        }
        String best = "";
        int bestScore = 0;
        for (String s : splitSections(md)) {
            String head = firstLine(s).toLowerCase(Locale.ROOT);
            String body = bodyOf(s);
            String bodyLow = body.toLowerCase(Locale.ROOT);
            int score = 0;
            for (String w : words) {
                if (head.contains(w)) {
                    score += 2;
                } else if (bodyLow.contains(w)) {
                    score += 1;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = s;
            }
        }
        return bestScore > 0 ? best.strip() : "";
    }
}
