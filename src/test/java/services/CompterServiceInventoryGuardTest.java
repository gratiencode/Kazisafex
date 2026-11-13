package services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

import data.Compter;
import data.Inventaire;
import data.Produit;
import data.StockAgregate;
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

    @Test
    void inventoryReferenceDateUsesOpeningDateWithLegacyFallback() {
        Inventaire inventory = new Inventaire();
        LocalDate opening = LocalDate.of(2026, 11, 1);
        inventory.setDateDebut(opening);
        assertEquals(opening, CompterService.inventoryReferenceDate(inventory));
        assertEquals(LocalDate.now(), CompterService.inventoryReferenceDate(new Inventaire()));
        assertEquals(LocalDate.now(), CompterService.inventoryReferenceDate(null));
    }

    @Test
    void openingTheoreticalStockUsesInventoryOpeningDate() {
        LocalDate opening = LocalDate.of(2026, 11, 1);
        Inventaire inventory = new Inventaire();
        inventory.setUid("inv-opening");
        inventory.setDateDebut(opening);
        Produit product = new Produit("prod-open");

        final LocalDate[] seenDate = new LocalDate[1];
        CompterService service = new CompterService() {
            @Override
            public StockAgregate findLotStock(String pId, String lot, LocalDate date, String region) {
                seenDate[0] = date;
                StockAgregate aggregate = new StockAgregate("stock-open");
                aggregate.setFinalQuantity(42d);
                return aggregate;
            }
        };

        double quantity = service.theoreticalStockAtOpening(product, "lot-1", inventory, "Lubumbashi");

        assertEquals(42d, quantity);
        assertEquals(opening, seenDate[0]);
    }

    @Test
    void persistedSnapshotIsTheImmutableReference() {
        Inventaire inventory = new Inventaire();
        inventory.setUid("inv-snapshot");
        inventory.setDateDebut(LocalDate.of(2026, 11, 1));
        Produit product = new Produit("prod-snapshot");

        Compter line = new Compter();
        line.setTheoreticalSnapshot(15d);

        CompterService service = new CompterService();
        assertEquals(15d, service.resolveTheoreticalReference(
                line, product, "lot-snapshot", inventory, "Lubumbashi"),
                "La photographie persistée doit primer sur tout recalcul");
    }

    @Test
    void legacyLineWithoutSnapshotFallsBackToOpeningReference() {
        Inventaire inventory = new Inventaire();
        inventory.setUid("inv-legacy");
        inventory.setDateDebut(LocalDate.of(2026, 11, 1));
        Produit product = new Produit("prod-legacy");

        CompterService service = new CompterService() {
            @Override
            public StockAgregate findLotStock(String pId, String lot, LocalDate date, String region) {
                StockAgregate aggregate = new StockAgregate("stock-legacy");
                aggregate.setFinalQuantity(7d);
                return aggregate;
            }
        };

        assertEquals(7d, service.resolveTheoreticalReference(
                new Compter(), product, "lot-legacy", inventory, "Lubumbashi"));
    }

    @Test
    void closingAnAlreadyClosedInventoryIsANoOp() {
        Inventaire closed = new Inventaire();
        closed.setUid("inv-closed");
        closed.setEtat("Terminé");

        assertDoesNotThrow(() -> new CompterService().removeNoCountedProducts(closed),
                "une clôture rejouée ne doit ni recréer de comptages zéro ni ajuster le stock");
    }

    @Test
    void closedInventoryIsDetectedForIdempotentClose() {
        Inventaire closed = new Inventaire();
        closed.setEtat("Terminé");
        Inventaire open = new Inventaire();
        open.setEtat("En cours...");

        assertTrue(CompterService.isInventoryClosed(closed));
        assertFalse(CompterService.isInventoryClosed(open));
        assertFalse(CompterService.isInventoryClosed(null));
    }

    @Test
    void confirmingEmptyComptageIsIgnoredInsteadOfThrowing() {
        CompterService service = new CompterService();
        assertDoesNotThrow(() -> service.confirmerComptage(null));
        assertDoesNotThrow(() -> service.confirmerComptage(new tools.ComptageItem()));
    }

    @Test
    void theoreticalSnapshotIsExcludedFromServerPayload() throws Exception {
        Compter compter = new Compter();
        compter.setUid("compter-snapshot");
        compter.setTheoreticalSnapshot(7.5);

        String json = data.core.KazisafeServiceFactory.mapper()
                .writeValueAsString(compter);

        assertFalse(json.contains("theoreticalSnapshot"),
                "la photographie locale ne doit pas modifier le contrat serveur");
        assertFalse(json.contains("theoretical_snapshot"));
    }

    @Test
    void inventoryLocksAreSharedPerInventoryAndReentrant() {
        Inventaire first = new Inventaire();
        first.setUid("inv-lock-1");
        Inventaire same = new Inventaire();
        same.setUid("inv-lock-1");
        Inventaire other = new Inventaire();
        other.setUid("inv-lock-2");

        ReentrantLock lock = CompterService.inventoryLock(first);
        assertSame(lock, CompterService.inventoryLock(same));
        assertNotSame(lock, CompterService.inventoryLock(other));

        lock.lock();
        try {
            lock.lock();
            assertEquals(2, lock.getHoldCount(), "le verrou doit être réentrant pour la clôture");
            lock.unlock();
        } finally {
            lock.unlock();
        }
    }
}
