package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data.LigneVente;
import data.PrixDeVente;
import data.Recquisition;
import data.Vente;
import java.util.List;
import org.junit.jupiter.api.Test;

class UtilTest {

    @Test
    void findPricesForReqComparesTheRecquisitionUid() {
        Recquisition r1 = new Recquisition("req-1");
        Recquisition r2 = new Recquisition("req-2");
        PrixDeVente first = new PrixDeVente();
        first.setRecquisitionId(r1);
        PrixDeVente second = new PrixDeVente();
        second.setRecquisitionId(r2);
        PrixDeVente orphan = new PrixDeVente();

        List<PrixDeVente> result = Util.findPricesForReq(List.of(first, second, orphan), "req-1");

        assertEquals(1, result.size());
        assertSame(first, result.get(0));
    }

    @Test
    void getVenteUsesIntegerValueEquality() {
        Vente sale = new Vente(1000);
        assertSame(sale, Util.getVente(List.of(sale), 1000));
        assertNull(Util.getVente(List.of(sale), 1001));
    }

    @Test
    void findSaleItemsForUsesIntegerValueEquality() {
        Vente sale = new Vente(1000);
        LigneVente line = new LigneVente();
        line.setReference(sale);

        List<LigneVente> result = Util.findSaleItemsFor(List.of(line), 1000);

        assertEquals(1, result.size());
        assertTrue(result.contains(line));
    }
}
