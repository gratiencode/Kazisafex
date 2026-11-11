package services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import data.Inventaire;
import org.junit.jupiter.api.Test;

class CompterServiceInventoryGuardTest {

    @Test
    void rejectsUpdateOnClosedInventory() {
        Inventaire closed = new Inventaire();
        closed.setEtat("Terminé");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> CompterService.requireInventoryOpen(closed));
        assertEquals("Inventaire clôturé : la modification des comptages est interdite",
                error.getMessage());
    }

    @Test
    void allowsUpdateOnOpenOrNullInventory() {
        Inventaire open = new Inventaire();
        open.setEtat("En cours...");

        assertDoesNotThrow(() -> CompterService.requireInventoryOpen(open));
        assertDoesNotThrow(() -> CompterService.requireInventoryOpen(null));
    }
}
