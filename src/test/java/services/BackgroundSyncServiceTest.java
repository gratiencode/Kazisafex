package services;

import data.SyncOutbox;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de non-régression de l'ordre de matérialisation downsync.
 *
 * Garantit que {@link BackgroundSyncService#getTablePriority} respecte les
 * dépendances FK : un parent (ex. Produit, Vente, Inventaire) est toujours
 * dans un niveau inférieur à ses dépendants (ex. Mesure, LigneVente, Compter).
 */
@DisplayName("BackgroundSyncService.getTablePriority — Ordre de dépendance FK")
class BackgroundSyncServiceTest {

    private static int p(String table) {
        return BackgroundSyncService.getTablePriority(table);
    }

    @Test
    @DisplayName("Chaque parent est prioritaire avant son dépendant")
    void testParentsBeforeChildren() {
        assertLess(p("CATEGORY"), p("PRODUIT"));
        assertLess(p("PRODUIT"), p("MESURE"));
        assertLess(p("VENTE"), p("LIGNEVENTE"));
        assertLess(p("INVENTORY"), p("COMPTER"));
        assertLess(p("CLIENT"), p("VENTE"));
        assertLess(p("MATIERE"), p("MATIERESKU"));
        assertLess(p("FOURNISSEUR"), p("LIVRAISON"));
        assertLess(p("COMPTETRESOR"), p("TRAISORERIE"));
        assertLess(p("DEPENSE"), p("OPERATION"));
        assertLess(p("TRAISORERIE"), p("OPERATION"));
        assertLess(p("PRODUIT"), p("STOCKER"));
        assertLess(p("RECQUISITION"), p("PRIXDEVENTE"));
        assertLess(p("PRODUCTION"), p("REPARTIR"));
        assertLess(p("REPARTIR"), p("IMPUTER"));
        assertLess(p("MESURE"), p("RETOURDEPOT"));
        assertLess(p("TAXE"), p("TAXER"));
        assertLess(p("COMMANDE"), p("COMMANDELIST"));
        assertLess(p("PRODUIT"), p("PERIODE"));
        assertLess(p("CLIENT"), p("ARETIRER"));
        assertLess(p("LIVRAISON"), p("ENTREPOSER"));
    }

    @Test
    @DisplayName("Assignation exacte des niveaux et inconnu -> 99")
    void testExactPhaseAssignment() {
        assertEquals(0, p("CATEGORY"));
        assertEquals(1, p("PRODUIT"));
        assertEquals(2, p("MESURE"));
        assertEquals(3, p("PRIXDEVENTE"));
        assertEquals(4, p("ENTREPOSER"));
        assertEquals(0, p("PRESENCE"));
        assertEquals(5, p("FACTURE"));
        assertEquals(99, p("UNKNOWN_TABLE"));
    }

    @Test
    @DisplayName("Les mutations strictement identiques sont coalescées avant envoi")
    void testCoalesceExactDuplicates() {
        SyncOutbox first = record("uid-1", "same-payload");
        SyncOutbox duplicate = record("uid-2", "same-payload");
        SyncOutbox different = record("uid-3", "other-payload");

        List<SyncOutbox> unique = BackgroundSyncService.coalesceExactDuplicates(
                List.of(first, duplicate, different));

        assertEquals(2, unique.size());
        assertSame(first, unique.get(0));
        assertSame(different, unique.get(1));
    }

    private static SyncOutbox record(String uid, String payload) {
        SyncOutbox r = new SyncOutbox();
        r.setUid(uid);
        r.setTableName("PRODUIT");
        r.setEntityId("prod-1");
        r.setAction("UPDATE");
        r.setPayload(payload);
        r.setRegion("Goma");
        return r;
    }

    private static void assertLess(int a, int b) {
        assertTrue(a < b, "attendu " + a + " < " + b + " mais " + a + " >= " + b);
    }
}
