package com.endeleya.kazisafex;

import data.Category;
import data.Mesure;
import data.Produit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.CurrencyConverter;
import tools.ListViewItem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("POS - rendu des ListViewItem")
class PosListViewDisplayTest {

    @Test
    @DisplayName("les cellules texte tolerent un item ou un produit absent")
    void textCells_tolerateMissingItemOrProduct() {
        assertEquals("", PosController.productText(null, Produit::getCodebar));
        assertEquals("", PosController.productText(item(null), Produit::getCodebar));
        assertEquals("", PosController.productText(item(null), null));
        assertEquals("", PosController.searchText((ListViewItem) null));
        assertEquals("", PosController.searchText((Produit) null));
        assertEquals("", PosController.quantityText(null));
    }

    @Test
    @DisplayName("les champs produit nuls ne produisent ni NPE ni texte null")
    void productFields_nullSafe() {
        Produit produit = new Produit();
        produit.setUid("p1");
        produit.setCodebar("CB-1");
        ListViewItem item = item(produit);

        assertEquals("CB-1", PosController.productText(item, Produit::getCodebar));
        assertEquals("", PosController.productText(item, Produit::getNomProduit));

        String searchable = PosController.searchText(item);
        assertTrue(searchable.contains("CB-1"));
        assertFalse(searchable.contains("null"));
    }

    @Test
    @DisplayName("quantité NaN/infinie/nulle affichée sans NumberFormatException")
    void quantityCells_nonFiniteValues() {
        assertDoesNotThrow(() -> PosController.quantityText(item(null)));
        assertEquals("0", PosController.quantityText(withQuantity(null)));
        assertEquals("0", PosController.quantityText(withQuantity(Double.NaN)));
        assertEquals("0", PosController.quantityText(withQuantity(Double.POSITIVE_INFINITY)));
        assertEquals("0", PosController.quantityText(withQuantity(Double.NEGATIVE_INFINITY)));
        assertEquals("0", PosController.formatQuantity(Double.NaN));
        assertEquals("12.346", PosController.formatQuantity(12.3456));
        assertEquals("12", PosController.formatQuantity(12.0));
    }

    @Test
    @DisplayName("quantité et prix utilisent une mesure absente sans bloquer la ligne")
    void quantityAndPrice_missingMeasure() {
        ListViewItem item = withQuantity(3.0);
        item.setMesureAchat(null);

        assertEquals("3", PosController.quantityText(item));
        assertDoesNotThrow(() -> PosController.priceText(null, null));
        assertFalse(PosController.priceText(Double.NaN, null).contains("NaN"));
        assertTrue(PosController.priceText(0.0, measure("kg", 1d)).endsWith("/kg"));
    }

    @Test
    @DisplayName("filtre catégorie tolère catégorie/produit nuls")
    void categoryFilter_nullSafe() {
        assertFalse(PosController.matchesCategory(null, "c1"));
        assertFalse(PosController.matchesCategory(item(null), "c1"));

        Produit produit = new Produit();
        produit.setUid("p1");
        ListViewItem item = item(produit);
        assertTrue(PosController.matchesCategory(item, null));
        assertTrue(PosController.matchesCategory(item, ""));
        assertTrue(PosController.matchesCategory(item, "All"));
        assertFalse(PosController.matchesCategory(item, "c1"));

        Category category = new Category();
        category.setUid("c1");
        produit.setCategoryId(category);
        assertTrue(PosController.matchesCategory(item, "c1"));
        assertFalse(PosController.matchesCategory(item, "c2"));

        category.setUid(null);
        assertFalse(PosController.matchesCategory(item, "c1"));
    }

    @Test
    @DisplayName("valeurs finies uniquement pour les calculs de stock")
    void finiteHelpers() {
        assertEquals(0d, PosController.finiteOrZero(null));
        assertEquals(0d, PosController.finiteOrZero(Double.NaN));
        assertEquals(0d, PosController.finiteOrZero(Double.POSITIVE_INFINITY));
        assertEquals(5d, PosController.finiteOrZero(5d));
        assertFalse(PosController.hasPositiveFinite(null));
        assertFalse(PosController.hasPositiveFinite(0d));
        assertFalse(PosController.hasPositiveFinite(-1d));
        assertFalse(PosController.hasPositiveFinite(Double.NaN));
        assertTrue(PosController.hasPositiveFinite(0.001d));
    }

    @Test
    @DisplayName("les montants non finis ne cassent pas le formatage")
    void amountFormatting_nonFiniteValues() {
        assertEquals(0d, CurrencyConverter.round(Double.NaN));
        assertEquals(0d, CurrencyConverter.round(Double.POSITIVE_INFINITY));
        assertDoesNotThrow(() -> CurrencyConverter.formatAmount(Double.NaN, "USD"));
        assertDoesNotThrow(() -> CurrencyConverter.formatAmount(Double.NEGATIVE_INFINITY, "CDF"));
    }

    private static ListViewItem item(Produit produit) {
        ListViewItem item = new ListViewItem();
        item.setProduit(produit);
        return item;
    }

    private static ListViewItem withQuantity(Double quantity) {
        ListViewItem item = item(null);
        item.setQuantiteRestant(quantity);
        return item;
    }

    private static Mesure measure(String description, Double quantContenu) {
        Mesure mesure = new Mesure();
        mesure.setUid("m1");
        mesure.setDescription(description);
        mesure.setQuantContenu(quantContenu);
        return mesure;
    }
}
