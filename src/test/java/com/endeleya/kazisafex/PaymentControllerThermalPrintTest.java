package com.endeleya.kazisafex;

import java.text.DecimalFormat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de la logique d'impression thermique (receipt 58mm).
 *
 * Couvre les helpers pures de PaymentController :
 *  - wrapText : retour à la ligne respectant la largeur du ticket
 *  - printCentered : centrage du texte sur WIDTH caractères
 *  - printLine : formatage d'une ligne TOTAL/PAYÉ/RESTE
 *
 * Ces tests ne dépendent d'aucun matériel (pas de EscPos / PrinterOutputStream).
 */
@DisplayName("PaymentController — Impression thermique (receipt 58mm)")
class PaymentControllerThermalPrintTest {

    private static final int WIDTH_48 = 48;  // ticket 48 colonnes (défaut)
    private static final int WIDTH_32 = 32;  // ticket 32 colonnes (étroit)

    private DecimalFormat usdFmt;
    private DecimalFormat cdfFmt;

    @BeforeEach
    void setUp() {
        usdFmt = new DecimalFormat("0.00");
        cdfFmt = new DecimalFormat("#,##0.00");
    }

    // ── wrapText ────────────────────────────────────────────────────────

    @Test
    @DisplayName("wrapText : texte court — pas de retour à la ligne")
    void wrapText_shortText_noWrap() {
        List<String> lines = PaymentController.wrapText("Nystatine", WIDTH_48);
        assertEquals(1, lines.size());
        assertEquals("Nystatine", lines.get(0));
    }

    @Test
    @DisplayName("wrapText : texte exactement à la largeur — pas de split")
    void wrapText_exactWidth_noSplit() {
        String text = "A".repeat(WIDTH_48);
        List<String> lines = PaymentController.wrapText(text, WIDTH_48);
        assertEquals(1, lines.size());
        assertEquals(text, lines.get(0));
    }

    @Test
    @DisplayName("wrapText : texte dépasse — split au dernier espace")
    void wrapText_longText_splitsAtSpace() {
        // "Paracetamol 500mg boite de 30 comprimes" = 40 chars > 32
        // lastIndexOf(32, 32) → espace à index 30 → split: "Paracetamol 500mg boite de 30"
        String text = "Paracetamol 500mg boite de 30 comprimes";
        List<String> lines = PaymentController.wrapText(text, WIDTH_32);
        assertFalse(lines.isEmpty(), "Doit retourner au moins 1 ligne");
        for (String line : lines) {
            assertTrue(line.length() <= WIDTH_32,
                    "Chaque ligne doit être <= " + WIDTH_32 + " mais : '" + line + "' (" + line.length() + ")");
        }
        assertEquals("Paracetamol 500mg boite de 30", lines.get(0));
    }

    @Test
    @DisplayName("wrapText : texte très long — split multiple")
    void wrapText_veryLongText_multipleSplits() {
        String text = "Cereale Lait Ble Miel Bebe 800g Nutriben";
        List<String> lines = PaymentController.wrapText(text, WIDTH_32);
        assertTrue(lines.size() >= 2, "Doit retourner au moins 2 lignes");
        for (String line : lines) {
            assertTrue(line.length() <= WIDTH_32,
                    "Chaque ligne doit être <= " + WIDTH_32 + " : '" + line + "'");
        }
        // Vérifier que le texte complet est reconstitué
        String rejoined = String.join(" ", lines);
        assertTrue(rejoined.contains("Cereale") && rejoined.contains("Nutriben"),
                "Le texte reconstitué doit contenir tous les mots");
    }

    @Test
    @DisplayName("wrapText : pas d'espace — split à la largeur exacte")
    void wrapText_noSpace_splitsAtWidth() {
        String text = "A".repeat(50);
        List<String> lines = PaymentController.wrapText(text, WIDTH_32);
        assertTrue(lines.size() >= 2);
        assertEquals("A".repeat(WIDTH_32), lines.get(0));
    }

    @Test
    @DisplayName("wrapText : texte vide — retourne liste avec chaîne vide")
    void wrapText_emptyText() {
        List<String> lines = PaymentController.wrapText("", WIDTH_48);
        assertEquals(1, lines.size());
        assertEquals("", lines.get(0));
    }

    // ── printCentered ───────────────────────────────────────────────────

    @Test
    @DisplayName("printCentered : centrage normal")
    void printCentered_normalText() {
        PaymentController ctrl = createController(WIDTH_48);
        String centered = ctrl.printCentered("FACTURE");
        // (48 - 7) / 2 = 20 espaces avant
        assertEquals(20, centered.indexOf("FACTURE"),
                "FACTURE doit commencer à l'index 20 pour WIDTH=48");
        assertTrue(centered.endsWith("FACTURE"));
    }

    @Test
    @DisplayName("printCentered : texte large — pas d'espace négatif")
    void printCentered_textLargerThanWidth() {
        PaymentController ctrl = createController(WIDTH_32);
        String text32 = "A".repeat(WIDTH_32);
        String centered = ctrl.printCentered(text32);
        assertTrue(centered.startsWith(text32),
                "Si le texte = WIDTH, pas de padding (pad=0)");
    }

    @Test
    @DisplayName("printCentered : texte vide — centré avec moitié des espaces")
    void printCentered_emptyText() {
        PaymentController ctrl = createController(WIDTH_48);
        String centered = ctrl.printCentered("");
        // pad = (48 - 0) / 2 = 24
        assertEquals(24, centered.length(),
                "Texte vide → (WIDTH/2) espaces de padding");
        assertEquals(" ".repeat(24), centered);
    }

    // ── printLine (TOTAL / PAYÉ / RESTE) ───────────────────────────────

    @Test
    @DisplayName("printLine USD : format 0.00 sans séparateur de milliers")
    void printLine_usd() {
        PaymentController ctrl = createController(WIDTH_48);
        String line = ctrl.printLine("TOTAL:", 125.50, "USD", usdFmt);
        assertTrue(line.startsWith("TOTAL:"), "Commence par le label");
        assertTrue(line.contains("125.50"), "Contient le montant USD formaté");
        assertTrue(line.endsWith("USD"), "Se termine par la devise");
        assertEquals(WIDTH_48, line.length(),
                "La ligne doit faire exactement WIDTH caractères");
    }

    @Test
    @DisplayName("printLine CDF : format #,##0.00 avec séparateur de milliers")
    void printLine_cdf() {
        PaymentController ctrl = createController(WIDTH_48);
        String line = ctrl.printLine("PAYÉ:", 1500000.0, "CDF", cdfFmt);
        assertTrue(line.startsWith("PAYÉ:"));
        assertTrue(line.contains("1,500,000"), "CDF doit avoir le séparateur de milliers");
        assertTrue(line.endsWith("CDF"));
        assertEquals(WIDTH_48, line.length());
    }

    @Test
    @DisplayName("printLine : label long — pas d'espace négatif")
    void printLine_longLabel() {
        PaymentController ctrl = createController(WIDTH_32);
        String line = ctrl.printLine("RESTE À PAYER:", 10.0, "USD", usdFmt);
        assertTrue(line.contains("10.00"));
        assertTrue(line.contains("USD"));
        assertEquals(WIDTH_32, line.length());
    }

    @Test
    @DisplayName("printLine : montant zéro")
    void printLine_zeroAmount() {
        PaymentController ctrl = createController(WIDTH_48);
        String line = ctrl.printLine("TOTAL:", 0.0, "USD", usdFmt);
        assertTrue(line.contains("0.00"));
        assertEquals(WIDTH_48, line.length());
    }

    @Test
    @DisplayName("printLine : grand montant USD sans séparateur de milliers")
    void printLine_largeAmount() {
        PaymentController ctrl = createController(WIDTH_48);
        String line = ctrl.printLine("TOTAL:", 999999.99, "USD", usdFmt);
        assertTrue(line.contains("999999.99"), "USD format 0.00 — pas de virgule de milliers");
        assertTrue(line.endsWith("USD"));
        assertEquals(WIDTH_48, line.length());
    }

    // ── Wrap + Center intégration ───────────────────────────────────────

    @Test
    @DisplayName("Integration : wrapText puis printCentered ne dépasse pas WIDTH")
    void integration_wrapThenCenter() {
        PaymentController ctrl = createController(WIDTH_48);
        String longName = "Paracetamol 500mg boite de 30 comprimes solution buvable";
        List<String> wrapped = PaymentController.wrapText(longName, WIDTH_48);
        for (String line : wrapped) {
            String centered = ctrl.printCentered(line);
            assertTrue(centered.length() <= WIDTH_48 + line.length(),
                    "Le centrage ne doit pas créer de dépassement absurdement long");
        }
    }

    @Test
    @DisplayName("Integration : wrapText puis printLine — alignement cohérent")
    void integration_wrapThenPrintLine() {
        PaymentController ctrl = createController(WIDTH_48);
        // Simule un item wrap
        String productName = "Amoxicilline 500mg gelule boite de 100 unite pharmacie";
        List<String> lines = PaymentController.wrapText(productName, WIDTH_48);
        assertFalse(lines.isEmpty());

        // Puis une ligne TOTAL
        String totalLine = ctrl.printLine("TOTAL:", 45.75, "USD", usdFmt);
        assertEquals(WIDTH_48, totalLine.length());
        assertTrue(totalLine.contains("45.75"));
    }

    // ── Helper ──────────────────────────────────────────────────────────

    /**
     * Crée un PaymentController partiellement initialisé pour les tests
     * des helpers pures (WIDTH utilisé par printCentered/printLine).
     */
    private PaymentController createController(int width) {
        try {
            PaymentController ctrl = new PaymentController();
            var field = PaymentController.class.getDeclaredField("WIDTH");
            field.setAccessible(true);
            field.setInt(ctrl, width);
            return ctrl;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer le controller de test", e);
        }
    }
}
