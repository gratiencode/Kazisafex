package services;

import data.SyncOutbox;
import data.network.dto.SyncOutboxDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de non-régression de la matérialisation downsync locale.
 *
 * Vérifie :
 *  1. createDownsyncRecord fait un UPSERT (jamais de doublon de ligne
 *     sync_outbox pour le même couple tableName + entityId).
 *  2. Le mapping des types de mutation (INSERT->PERSIST, DELETE->REMOVE,
 *     sinon UPDATE).
 *  3. La matérialisation utilise em.merge (UPSERT par uid) : rejouer les
 *     mêmes mutations ne crée aucune ligne en double.
 *  4. L'ordre parent -> enfant (Produit avant Mesure, etc.).
 *  5. REMOVE sans payload passe par find + remove (idempotent).
 *  6. Un payload sans uid est ignoré (anti-doublon).
 */
@DisplayName("SyncOutboxService — Matérialisation downsync sans doublon")
class SyncOutboxServiceTest {

    @Test
    @DisplayName("createDownsyncRecord: première fois insère, re-téléchargement met à jour (UPSERT)")
    void testCreateDownsyncRecordUpsert() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            SyncOutboxDto dto = dto("PRODUIT", "prod-1", "INSERT", 1000L);
            SyncOutboxService.createDownsyncRecord(dto);

            // même mutation re-téléchargée (ex. full resync since=0)
            dto.mutationType = "UPDATE";
            SyncOutboxService.createDownsyncRecord(dto);

            assertEquals(1, db.persistedEntities.size(),
                    "Aucune seconde ligne pour la même entité");
            SyncOutbox row = (SyncOutbox) db.persistedEntities.get(0);
            assertEquals("UPDATE", row.getAction(), "La ligne existante est mise à jour, pas ré-insérée");
            assertEquals("DOWNSYNCED", row.getStatus());
            assertEquals("prod-1", row.getEntityId());
            assertEquals("PRODUIT", row.getTableName());
            assertTrue(row.getRegion() != null && !row.getRegion().isBlank(),
                    "Le record porte la région : la clé d'UPSERT est (tableName, entityId, region)");
        }
    }

    @Test
    @DisplayName("createDownsyncRecord: une entité différente crée bien sa propre ligne")
    void testCreateDownsyncRecordDifferentEntityInserts() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            SyncOutboxService.createDownsyncRecord(dto("PRODUIT", "prod-1", "INSERT", 1L));
            SyncOutboxService.createDownsyncRecord(dto("PRODUIT", "prod-2", "INSERT", 2L));
            SyncOutboxService.createDownsyncRecord(dto("MESURE", "mes-1", "INSERT", 3L));

            assertEquals(3, db.persistedEntities.size());
        }
    }

    @Test
    @DisplayName("createDownsyncRecord: mapping INSERT->PERSIST, UPDATE->UPDATE, DELETE->REMOVE")
    void testCreateDownsyncRecordActionMapping() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            SyncOutboxService.createDownsyncRecord(dto("PRODUIT", "a", "INSERT", 1L));
            SyncOutboxService.createDownsyncRecord(dto("PRODUIT", "b", "UPDATE", 2L));
            SyncOutboxService.createDownsyncRecord(dto("PRODUIT", "c", "DELETE", 3L));

            Map<String, String> byId = db.persistedEntities.stream()
                    .filter(o -> o instanceof SyncOutbox)
                    .collect(Collectors.toMap(
                            o -> ((SyncOutbox) o).getEntityId(),
                            o -> ((SyncOutbox) o).getAction()));

            assertEquals("PERSIST", byId.get("a"));
            assertEquals("UPDATE", byId.get("b"));
            assertEquals("REMOVE", byId.get("c"));
        }
    }

    @Test
    @DisplayName("materializeDownsyncRecords: parents appliqués avant enfants, jamais de doublon")
    void testMaterializeRespectsDependencyOrderAndIsIdempotent() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            // arrivée volontairement désordonnée : Mesure avant Produit/Category
            db.record("MESURE", "mes-1", "PERSIST",
                    "{\"type\":\"MESURE\",\"uid\":\"mes-1\",\"quantContenu\":1.0,\"produitId\":{\"uid\":\"prod-1\"}}");
            db.record("PRODUIT", "prod-1", "PERSIST",
                    "{\"type\":\"PRODUIT\",\"uid\":\"prod-1\",\"nomProduit\":\"Lait\"}");
            db.record("CATEGORY", "cat-1", "PERSIST",
                    "{\"type\":\"CATEGORY\",\"uid\":\"cat-1\",\"descritption\":\"Élec\"}");
            // La catégorie de repli "Divers" existe : un produit sans catégorie
            // l'obtient par recherche (uid déterministe), sans nouvelle ligne.
            db.seedEntity(new data.Category(SyncOutboxService.DIVERS_CATEGORY_UID, "Divers"));

            SyncOutboxService.materializeDownsyncRecords();

            List<String> uids = db.mergedEntities.stream()
                    .map(SyncTestDb::uidOf)
                    .collect(Collectors.toList());
            assertEquals(List.of("cat-1", "prod-1", "mes-1"), uids,
                    "Ordre de matérialisation = niveaux de dépendance (parents d'abord)");

            // merge uniquement : aucune insertion brute d'entité
            assertTrue(db.persistedEntities.isEmpty(),
                    "La matérialisation ne doit jamais em.persist() une entité métier");
            assertTrue(db.outboxStatusesAll("APPLIED"), "Tous les records doivent être marqués APPLIED");

            // rejeu : aucune entité supplémentaire matérialisée (idempotent)
            SyncOutboxService.materializeDownsyncRecords();
            assertEquals(List.of("cat-1", "prod-1", "mes-1"), uids);
            assertEquals(3, db.mergedEntities.size());
        }
    }

    @Test
    @DisplayName("materializeDownsyncRecords: REMOVE sans payload passe par find + remove (idempotent)")
    void testMaterializeRemoveWithoutPayload() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            db.seedEntity(new data.Produit("prod-5"));
            db.record("PRODUIT", "prod-5", "REMOVE", null);

            SyncOutboxService.materializeDownsyncRecords();

            assertEquals(1, db.removedEntities.size(),
                    "L'entité présente en base doit être supprimée");
            assertTrue(db.outboxStatusesAll("APPLIED"));
        }
    }

    @Test
    @DisplayName("materializeDownsyncRecords: REMOVE d'une entité absente est sans effet (idempotent)")
    void testMaterializeRemoveAbsentEntityIsHarmless() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            db.record("PRODUIT", "prod-absent", "REMOVE", null);

            SyncOutboxService.materializeDownsyncRecords();

            assertTrue(db.removedEntities.isEmpty(), "Rien à supprimer, aucune erreur");
            assertTrue(db.outboxStatusesAll("APPLIED"));
        }
    }

    @Test
    @DisplayName("materializeDownsyncRecords: un payload sans uid est ignoré (anti-doublon)")
    void testNullUidPayloadSkipped() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            db.record("PRODUIT", "prod-x", "PERSIST",
                    "{\"type\":\"PRODUIT\",\"nomProduit\":\"Lait\"}");

            SyncOutboxService.materializeDownsyncRecords();

            assertTrue(db.mergedEntities.isEmpty(),
                    "merge ne doit pas être appelé sans uid (sinon doublon)");
            assertTrue(db.outboxStatusesAll("APPLIED"),
                    "Payload sans uid = définitivement ignoré, pas de retry inutile");
        }
    }

    private static SyncOutboxDto dto(String type, String entityId, String mutationType, long ts) {
        SyncOutboxDto d = new SyncOutboxDto();
        d.entityType = type;
        d.entityId = entityId;
        d.mutationType = mutationType;
        d.mutationTs = ts;
        d.payload = "{\"type\":\"" + type + "\",\"uid\":\"" + entityId + "\"}";
        return d;
    }

    // ── Upsync : fetchAllPendingOutbox (plus de filtre d'ancienneté ni de plafond) ──

    private static SyncOutbox addRecord(
            SyncTestDb db,
            String uid,
            LocalDateTime createdAt,
            String status
    ) {
        SyncOutbox r = new SyncOutbox();
        r.setUid(uid);
        r.setTableName("PRODUIT");
        r.setEntityId("ent-" + uid);
        r.setAction("PERSIST");
        r.setPayload("{}");
        r.setStatus(status);
        r.setCreatedAt(createdAt);
        db.records.put(uid, r);
        return r;
    }

    private static void assertUids(List<SyncOutbox> result, String... expected) {
        List<String> uids = result.stream()
                .map(SyncOutbox::getUid)
                .collect(Collectors.toList());
        assertEquals(List.of(expected), uids,
                "Seuls les statuts à remonter, triés par createdAt (résultat=" + uids + ")");
    }

    @Test
    @DisplayName("fetchAllPendingOutbox: seuls PENDING/UNSYNCED/FAILED, triés par createdAt")
    void testFetchAllPendingOutbox() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            LocalDateTime t1 = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime t2 = LocalDateTime.of(2024, 1, 2, 10, 0);
            LocalDateTime t3 = LocalDateTime.of(2024, 1, 3, 10, 0);
            addRecord(db, "applied", t1, "APPLIED");
            addRecord(db, "downsynced", t1, "DOWNSYNCED");
            addRecord(db, "p1", t2, "PENDING");
            addRecord(db, "u1", t3, "UNSYNCED");
            addRecord(db, "f1", t3, "FAILED");

            List<SyncOutbox> result = SyncOutboxService.fetchAllPendingOutbox();

            assertUids(result, "p1", "u1", "f1");
        }
    }

    // ── Règle de fraîcheur : un payload réseau plus ancien que la version locale ──

    private static final String SERVER_UPDATED_AT = "2024-05-01T10:00:00";
    private static final LocalDateTime SERVER_TIME =
            LocalDateTime.parse(SERVER_UPDATED_AT);

    private static String produitPayload(String uid, String updatedAt) {
        return "{\"type\":\"PRODUIT\",\"uid\":\"" + uid
                + "\",\"nomProduit\":\"Lait\",\"updatedAt\":\"" + updatedAt + "\"}";
    }

    private static data.Produit localProduit(String uid, LocalDateTime updatedAt) {
        data.Produit p = new data.Produit(uid);
        p.setUpdatedAt(updatedAt);
        return p;
    }

    @Test
    @DisplayName("matérialisation: la version réseau plus récente est appliquée et garde son updatedAt serveur")
    void testMaterializeAppliesNewerNetworkVersionAndPreservesUpdatedAt() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            db.record("PRODUIT", "prod-9", "PERSIST",
                    produitPayload("prod-9", SERVER_UPDATED_AT));

            SyncOutboxService.materializeDownsyncRecords();

            // La valeur serveur est ré-appliquée par bulk UPDATE après le flush :
            // l'updatedAt de l'objet local n'est pas « maintenant », donc le
            // backfill ne peut pas croire à une mutation locale (pas de boucle).
            assertTrue(db.executedUpdates.stream().anyMatch(q ->
                            q.contains("UPDATE Produit e SET e.updatedAt")),
                    "Le bulk UPDATE de préservation de l'updatedAt serveur doit être émis, "
                            + "exécutés=" + db.executedUpdates);
            assertEquals(SERVER_TIME, db.updateParams.get("serverUpdatedAt"),
                    "Le timestamp serveur du payload est la valeur de référence, pas now()");

            // Aucun PENDING recréé : le listener est supprimé pendant la
            // matérialisation, aucun enregistrement supplémentaire n'apparaît.
            assertEquals(1, db.records.size(),
                    "Aucun enregistrement d'outbox ne doit être ajouté par la matérialisation");
            assertTrue(db.outboxStatusesAll("APPLIED"),
                    "Tous les records sont appliqués, aucun repassé en PENDING");
        }
    }

    @Test
    @DisplayName("matérialisation: un payload réseau périmé (updatedAt <= local) est ignoré, la version locale prévaut")
    void testMaterializeIgnoresStaleNetworkPayload() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            // La version locale est plus récente que celle du réseau.
            db.seedEntity(localProduit("prod-9",
                    SERVER_TIME.plusDays(30)));
            db.record("PRODUIT", "prod-9", "PERSIST",
                    produitPayload("prod-9", SERVER_UPDATED_AT));

            SyncOutboxService.materializeDownsyncRecords();

            assertTrue(db.mergedEntities.stream()
                            .noneMatch(o -> "prod-9".equals(SyncTestDb.uidOf(o))),
                    "merge ne doit pas écraser la version locale plus récente");
            assertTrue(db.outboxStatusesAll("APPLIED"),
                    "Le record est traité (APPLIED) même si la matérialisation est ignorée");
        }
    }
}
