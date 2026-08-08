package services;

import data.SyncOutbox;
import data.network.dto.SyncOutboxDto;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    // ── Upsync : filtre d'ancienneté (createdAt > lastSyncTimestamp) ─────────

    private static SyncOutbox addPending(
            SyncTestDb db,
            String uid,
            LocalDateTime createdAt,
            Integer retryCount
    ) {
        SyncOutbox r = new SyncOutbox();
        r.setUid(uid);
        r.setTableName("PRODUIT");
        r.setEntityId("ent-" + uid);
        r.setAction("PERSIST");
        r.setPayload("{}");
        r.setStatus(retryCount != null && retryCount > 0 ? "UNSYNCED" : "PENDING");
        r.setCreatedAt(createdAt);
        r.setRetryCount(retryCount);
        db.records.put(uid, r);
        return r;
    }

    private static void assertFiltered(List<SyncOutbox> result, String... expected) {
        List<String> uids = result.stream()
                .map(SyncOutbox::getUid)
                .collect(Collectors.toList());
        for (String uid : expected) {
            assertTrue(uids.contains(uid), "attendu dans le résultat: " + uid + " (résultat=" + uids + ")");
        }
        assertEquals(expected.length, uids.size(),
                "aucun enregistrement supplémentaire (résultat=" + uids + ")");
    }

    @Test
    @DisplayName("fetchPendingOutboxSince (MySQL): ancienneté + retries préservés")
    void testFetchPendingOutboxSinceFiltersMySQL() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            LocalDateTime old = LocalDateTime.now().minusDays(2).withNano(0);
            LocalDateTime fresh = LocalDateTime.now();
            addPending(db, "p1", old, null); // ancien, jamais tenté -> exclu
            addPending(db, "p2", fresh, null); // récent -> inclus
            addPending(db, "p3", old, 2); // ancien en retry -> inclus
            addPending(db, "p4", fresh, 5); // retryCount >= max -> exclu
            addPending(db, "p5", old, 0); // ancien, retry 0 -> exclu

            long since = old.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<SyncOutbox> result =
                    SyncOutboxService.fetchPendingOutboxSince(since);

            assertFiltered(result, "p2", "p3");
        }
    }

    @Test
    @DisplayName("fetchPendingOutboxSince (SQLite): même filtre via executeRead")
    void testFetchPendingOutboxSinceFiltersSqlite() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            db.msf.when(ManagedSessionFactory::isEmbedded).thenReturn(true);
            LocalDateTime old = LocalDateTime.now().minusDays(2).withNano(0);
            LocalDateTime fresh = LocalDateTime.now();
            addPending(db, "s1", old, null);
            addPending(db, "s2", fresh, null);
            addPending(db, "s3", old, 3);

            long since = old.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<SyncOutbox> result =
                    SyncOutboxService.fetchPendingOutboxSince(since);

            assertFiltered(result, "s2", "s3");
        }
    }

    @Test
    @DisplayName("fetchPendingOutboxSince: since=0 (full sync) retourne tout")
    void testFetchPendingOutboxSinceFullSync() throws Exception {
        try (SyncTestDb db = new SyncTestDb()) {
            LocalDateTime old = LocalDateTime.now().minusDays(30).withNano(0);
            addPending(db, "f1", old, null);
            addPending(db, "f2", old, 1);

            List<SyncOutbox> result = SyncOutboxService.fetchPendingOutboxSince(0L);

            assertFiltered(result, "f1", "f2");
        }
    }
}
