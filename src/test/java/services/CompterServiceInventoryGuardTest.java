package services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

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
