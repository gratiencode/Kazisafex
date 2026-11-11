package services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import data.Compter;
import data.Inventaire;
import data.Produit;
import org.junit.jupiter.api.Test;

class CompterServiceValidationTest {

    @Test
    void rejectsNegativeOrMissingFields() {
        Compter c = new Compter();
        c.setInventaireId(new Inventaire("inv-1"));
        c.setProductId(new Produit("prod-1"));
        c.setQuantite(-1);

        assertThrows(IllegalArgumentException.class, () -> CompterService.validateCompter(c));

        c.setQuantite(2);
        assertDoesNotThrow(() -> CompterService.validateCompter(c));

        c.setProductId(null);
        assertThrows(IllegalArgumentException.class, () -> CompterService.validateCompter(c));
    }
}
