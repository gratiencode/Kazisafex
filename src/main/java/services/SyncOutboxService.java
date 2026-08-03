package services;

import data.SyncOutbox;
import data.SyncOutboxListener;
import data.network.dto.SyncOutboxDto;
import jakarta.persistence.EntityManager;
import jakarta.json.JsonObject;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.prefs.Preferences;
import tools.JsonUtil;
import tools.SyncEngine;
import tools.SyncLogger;
import tools.Tables;

public class SyncOutboxService {

    private static final int MAX_RETRY_COUNT = 5;

    public static List<SyncOutbox> fetchAllPendingOutbox() {
        String jpql = "SELECT s FROM SyncOutbox s WHERE s.status IN ('PENDING','UNSYNCED') AND (s.retryCount IS NULL OR s.retryCount < :maxRetry)";
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, SyncOutbox.class)
                        .setParameter("maxRetry", MAX_RETRY_COUNT)
                        .getResultList());
            }
            return ManagedSessionFactory.getEntityManager()
                    .createQuery(jpql, SyncOutbox.class)
                    .setParameter("maxRetry", MAX_RETRY_COUNT)
                    .getResultList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public static List<SyncOutbox> fetchOutboxChunk(int chunkSize) {
        String jpql = "SELECT s FROM SyncOutbox s WHERE s.status IN ('PENDING','UNSYNCED') ORDER BY s.createdAt ASC";
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, SyncOutbox.class)
                        .setMaxResults(chunkSize).getResultList());
            }
            return ManagedSessionFactory.getEntityManager()
                    .createQuery(jpql, SyncOutbox.class)
                    .setMaxResults(chunkSize)
                    .getResultList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public static void markAsApplied(List<String> uids) {
        if (uids == null || uids.isEmpty()) return;
        submitSyncWrite(em -> {
            for (String uid : uids) {
                SyncOutbox record = em.find(SyncOutbox.class, uid);
                if (record != null) {
                    record.setStatus("APPLIED");
                    record.setRetryCount(0);
                }
            }
            return null;
        });
    }

    public static void markAsFailed(List<String> uids) {
        if (uids == null || uids.isEmpty()) return;
        submitSyncWrite(em -> {
            for (String uid : uids) {
                SyncOutbox record = em.find(SyncOutbox.class, uid);
                if (record != null) {
                    record.setStatus("FAILED");
                }
            }
            return null;
        });
    }

    public static void markAsUnsynced(List<String> uids) {
        if (uids == null || uids.isEmpty()) return;
        submitSyncWrite(em -> {
            for (String uid : uids) {
                SyncOutbox record = em.find(SyncOutbox.class, uid);
                if (record != null) {
                    int retry = record.getRetryCount() + 1;
                    record.setRetryCount(retry);
                    if (retry >= MAX_RETRY_COUNT) {
                        record.setStatus("FAILED");
                    } else {
                        record.setStatus("UNSYNCED");
                    }
                }
            }
            return null;
        });
    }

    public static void deleteAppliedRecords(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        submitSyncWrite(em -> {
            for (String uid : uids) {
                SyncOutbox record = em.find(SyncOutbox.class, uid);
                if (record != null && "APPLIED".equals(record.getStatus())) {
                    em.remove(record);
                }
            }
            return null;
        });
    }

    public static void cleanupAppliedRecords() {
        submitSyncWrite(em -> {
            em.createQuery("DELETE FROM SyncOutbox s WHERE s.status = 'APPLIED'")
                    .executeUpdate();
            return null;
        });
    }

    public static void purgeOldRecords(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        submitSyncWrite(em -> {
            em.createQuery("DELETE FROM SyncOutbox s WHERE s.createdAt < :cutoff AND s.status = 'APPLIED'")
                    .setParameter("cutoff", cutoff)
                    .executeUpdate();
            return null;
        });
    }

    /**
     * Routes SyncOutbox writes through the WriteQueueManager for SQLite
     * (serializing with SyncOutboxListener writes), or through executeWrite
     * for MySQL (each write gets its own EM).
     */
    private static <T> T submitSyncWrite(java.util.function.Function<EntityManager, T> action) {
        if (ManagedSessionFactory.isEmbedded()) {
            try {
                return ManagedSessionFactory.submitWrite(action).get();
            } catch (Exception e) {
                throw new RuntimeException("SyncOutbox write failed", e);
            }
        }
        return ManagedSessionFactory.executeWrite(action);
    }

    public static void correctAndCleanOutboxData() {
        Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
        String defaultEntrepriseId = pref.get("eUid", null);
        String defaultRegion = pref.get("region", "Goma");
        submitSyncWrite(em -> {
            performCorrection(em, defaultEntrepriseId, defaultRegion);
            return null;
        });
    }

    private static void performCorrection(EntityManager em, String defaultEntrepriseId, String defaultRegion) {
        String jpql = "SELECT s FROM SyncOutbox s ORDER BY s.createdAt ASC, s.updatedAt ASC";
        List<SyncOutbox> allRecords = em.createQuery(jpql, SyncOutbox.class).getResultList();

        Map<String, SyncOutbox> persistDuplicates = new HashMap<>();
        Map<String, SyncOutbox> updateDuplicates = new HashMap<>();
        Map<String, SyncOutbox> removeDuplicates = new HashMap<>();
        Set<String> removedEntityKeys = new HashSet<>();
        List<String> recordsToDelete = new ArrayList<>();

        for (SyncOutbox record : allRecords) {
            boolean valid = true;

            if (record.getEntityId() == null || record.getEntityId().isBlank() ||
                    record.getTableName() == null || record.getTableName().isBlank()) {
                valid = false;
            }

            // REMOVE actions intentionally have null payloads (entity already deleted from DB)
            if (valid && !"REMOVE".equalsIgnoreCase(record.getAction())
                    && (record.getPayload() == null || record.getPayload().isBlank())) {
                Object entity = findEntityInDb(em, record.getTableName(), record.getEntityId());
                if (entity != null) {
                    try {
                        JsonObject json = JsonUtil.jsonify(entity);
                        record.setPayload(json.toString());
                    } catch (Exception e) {
                        valid = false;
                    }
                } else {
                    valid = false;
                }
            }

            if (valid) {
                if (record.getEntrepriseId() == null || record.getEntrepriseId().isBlank()) {
                    record.setEntrepriseId(defaultEntrepriseId);
                }
                if (record.getRegion() == null || record.getRegion().isBlank()) {
                    record.setRegion(defaultRegion);
                }
            }

            if (!valid) {
                recordsToDelete.add(record.getUid());
                continue;
            }

            String action = record.getAction();
            String keyBase = record.getTableName() + "|" + record.getEntityId();

            if ("REMOVE".equalsIgnoreCase(action)) {
                // Track that this entity has a pending deletion
                removedEntityKeys.add(keyBase);
                // Dedup multiple REMOVEs for the same entity (keep latest)
                if (record.getUpdatedAt() != null) {
                    String key = keyBase + "|" + record.getUpdatedAt().toLocalDate();
                    if (removeDuplicates.containsKey(key)) {
                        SyncOutbox older = removeDuplicates.get(key);
                        recordsToDelete.add(older.getUid());
                        removeDuplicates.put(key, record);
                    } else {
                        removeDuplicates.put(key, record);
                    }
                }
                continue;
            }

            // If a REMOVE exists for this entity, the PERSIST/UPDATE is pointless
            if (removedEntityKeys.contains(keyBase)) {
                recordsToDelete.add(record.getUid());
                continue;
            }

            if ("PERSIST".equalsIgnoreCase(action) && record.getCreatedAt() != null) {
                LocalDate date = record.getCreatedAt().toLocalDate();
                String key = keyBase + "|" + date;
                if (persistDuplicates.containsKey(key)) {
                    SyncOutbox older = persistDuplicates.get(key);
                    recordsToDelete.add(older.getUid());
                    persistDuplicates.put(key, record);
                } else {
                    persistDuplicates.put(key, record);
                }
            } else if ("UPDATE".equalsIgnoreCase(action) && record.getUpdatedAt() != null) {
                LocalDate date = record.getUpdatedAt().toLocalDate();
                String key = keyBase + "|" + date;
                if (updateDuplicates.containsKey(key)) {
                    SyncOutbox older = updateDuplicates.get(key);
                    recordsToDelete.add(older.getUid());
                    updateDuplicates.put(key, record);
                } else {
                    updateDuplicates.put(key, record);
                }
            }
        }

        for (String uid : recordsToDelete) {
            SyncOutbox record = em.find(SyncOutbox.class, uid);
            if (record != null) {
                em.remove(record);
            }
        }
    }

    private static Object findEntityInDb(EntityManager em, String tableName, String entityId) {
        try {
            System.out.println("Finding entity in db: " + tableName + " " + entityId);
            Tables table = Tables.valueOf(tableName.toUpperCase());
            Class<?> entityClass = getEntityClass(table);
            if (entityClass != null) {
                // Detect the PK type from the entity class to avoid ClassCastException
                Object typedId = castToEntityIdType(entityClass, entityId);
                return em.find(entityClass, typedId);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Caste l'entityId String vers le type de PK réel de la classe (@Id).
     * Integer pour Vente, Long pour LigneVente, String pour toutes les autres.
     */
    private static Object castToEntityIdType(Class<?> entityClass, String entityId) {
        if (entityId == null) return null;
        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    Class<?> type = field.getType();
                    try {
                        if (type == Integer.class || type == int.class) {
                            return Integer.valueOf(entityId);
                        } else if (type == Long.class || type == long.class) {
                            return Long.valueOf(entityId);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("[DOWNSYNC] Cannot cast entityId=" + entityId + " to " + type.getSimpleName());
                    }
                    return entityId;
                }
            }
        }
        return entityId;
    }

    private static Class<?> getEntityClass(Tables table) {
        try {
            switch (table) {
                case FOURNISSEUR:
                    return data.Fournisseur.class;
                case CLIENT:
                    return data.Client.class;
                case TAXE:
                    return data.Taxe.class;
                case CATEGORY:
                    return data.Category.class;
                case PRODUIT:
                    return data.Produit.class;
                case MESURE:
                    return data.Mesure.class;
                case LIVRAISON:
                    return data.Livraison.class;
                case STOCKER:
                    return data.Stocker.class;
                case DESTOCKER:
                    return data.Destocker.class;
                case RECQUISITION:
                    return data.Recquisition.class;
                case PRIXDEVENTE:
                    return data.PrixDeVente.class;
                case COMPTETRESOR:
                    return data.CompteTresor.class;
                case VENTE:
                    return data.Vente.class;
                case LIGNEVENTE:
                    return data.LigneVente.class;
                case TAXER:
                    return data.Taxer.class;
                case TRAISORERIE:
                    return data.Traisorerie.class;
                case DEPENSE:
                    return data.Depense.class;
                case OPERATION:
                    return data.Operation.class;
                case INVENTORY:
                    return data.Inventaire.class;
                case COMPTER:
                    return data.Compter.class;
                case CLIENTORGANISATION:
                    return data.ClientOrganisation.class;
                case CLIENTAPPARTENIR:
                    return data.ClientAppartenir.class;
                case RETOURMAGASIN:
                    return data.RetourMagasin.class;
                case RETOURDEPOT:
                    return data.RetourDepot.class;
                case FACTURE:
                    return data.Facture.class;
                case ARETIRER:
                    return data.Aretirer.class;
                case ABONNEMENT:
                    return data.Abonnement.class;
                case PERIODE:
                    return data.Periode.class;
                case DEPOT:
                    return data.Depot.class;
                case COMMANDE:
                    return data.Commande.class;
                case COMMANDELIST:
                    return data.CommandeLister.class;
                case MATIERE:
                    return data.Matiere.class;
                case MATIERESKU:
                    return data.MatiereSku.class;
                case PRODUCTION:
                    return data.Production.class;
                case REPARTIR:
                    return data.Repartir.class;
                case IMPUTER:
                    return data.Imputer.class;
                case ENTREPOSER:
                    return data.Entreposer.class;
                case SATISFAIRE:
                    return data.Satisfaire.class;
                case PERMISSION:
                    return data.Permission.class;
                case IMMOBILISATION:
                    return data.Immobilisation.class;
                case PRESENCE:
                    return data.Presence.class;
                case FINGERPRINTMAPPING:
                    return data.FingerprintMapping.class;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ── Backfill ────────────────────────────────────────────────────────────

    public static void startBackfillingBackground() {
        Thread backfillThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
                backfillMissingEntities();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[BACKFILL] Error in backfill daemon: " + e.getMessage());
                e.printStackTrace();
            }
        }, "Kazisafe-SyncOutbox-Backfiller");
        backfillThread.setDaemon(true);
        backfillThread.start();
    }

    public static void backfillMissingEntities() {
        Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
        String eUid = pref.get("eUid", null);
        String region = pref.get("region", "Goma");
        if (eUid == null || eUid.isBlank()) {
            System.err.println("[BACKFILL] eUid not set, skipping backfill.");
            return;
        }
        System.out.println("[BACKFILL] Scanning all tables for missing outbox records (eUid=" + eUid + ")...");

        List<List<Tables>> phases = List.of(
                List.of(Tables.CATEGORY, Tables.FOURNISSEUR, Tables.CLIENT, Tables.COMPTETRESOR, Tables.MATIERE,
                        Tables.DEPOT, Tables.INVENTORY, Tables.IMMOBILISATION),
                List.of(Tables.PRODUIT, Tables.LIVRAISON, Tables.VENTE, Tables.TRAISORERIE, Tables.DEPENSE),
                List.of(Tables.MESURE, Tables.STOCKER, Tables.DESTOCKER, Tables.RECQUISITION, Tables.LIGNEVENTE,
                        Tables.OPERATION, Tables.MATIERESKU, Tables.PRODUCTION, Tables.COMPTER),
                List.of(Tables.PRIXDEVENTE, Tables.REPARTIR),
                List.of(Tables.IMPUTER, Tables.ENTREPOSER),
                List.of(Tables.PRESENCE));

        Set<Tables> phased = new HashSet<>();
        for (List<Tables> phase : phases) {
            for (Tables table : phase) {
                phased.add(table);
                backfillSingleTable(table, eUid, region);
            }
        }

        for (Tables table : Tables.values()) {
            if (!phased.contains(table)) {
                if(table.equals(Tables.PERMISSION)|table.equals(Tables.FINGERPRINTMAPPING))continue;
                backfillSingleTable(table, eUid, region);
            }
        }

        System.out.println("[BACKFILL] Backfill complete.");
    }

    private static void backfillSingleTable(Tables table, String eUid, String region) {
        Class<?> entityClass = getEntityClass(table);
        if (entityClass == null)
            return;
        try {
            backfillTable(table, entityClass, eUid, region);
        } catch (Exception e) {
            System.err.println("[BACKFILL] Failed backfilling table " + table.name() + ": " + e.getMessage());
        }
    }

    private static void backfillTable(Tables table, Class<?> entityClass, String eUid, String region) {
        System.out.println("[BACKFILL] Scanning table: " + table.name());

        Set<String> existingSet = new HashSet<>(ManagedSessionFactory.executeRead(
                em -> em.createQuery("SELECT s.entityId FROM SyncOutbox s WHERE s.tableName = :tableName", String.class)
                        .setParameter("tableName", table.name())
                        .getResultList()));

        List<String> allEntityIds = ManagedSessionFactory.executeRead(em -> {
            try {
                List<?> rawIds = em.createQuery("SELECT e.uid FROM " + entityClass.getSimpleName() + " e")
                        .getResultList();
                List<String> strIds = new ArrayList<>(rawIds.size());
                for (Object id : rawIds) {
                    strIds.add(id != null ? id.toString() : null);
                }
                return strIds;
            } catch (Exception e) {
                System.err.println(
                        "[BACKFILL] Failed to fetch UIDs for " + entityClass.getSimpleName() + ": " + e.getMessage());
                return java.util.Collections.<String>emptyList();
            }
        });

        List<String> missingIds = new ArrayList<>();
        for (String id : allEntityIds) {
            if (id != null && !existingSet.contains(id)) {
                missingIds.add(id);
            }
        }

        if (missingIds.isEmpty()) {
            System.out.println("[BACKFILL] Table " + table.name() + ": up to date.");
            return;
        }

        System.out.println("[BACKFILL] Table " + table.name() + ": " + missingIds.size()
                + " missing records. Creating mutations...");

        int batchSize = 100;
        for (int i = 0; i < missingIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, missingIds.size());
            List<String> batch = missingIds.subList(i, end);
            try {
                backfillBatch(table, entityClass, batch, eUid, region);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("[BACKFILL] Error backfilling batch for " + table.name() + ": " + e.getMessage());
            }
        }

        System.out.println("[BACKFILL] Table " + table.name() + ": done.");
    }

    private static void backfillBatch(Tables table, Class<?> entityClass, List<String> idsToBackfill, String eUid,
            String region) {
        List<?> entities = ManagedSessionFactory.executeRead(em -> em
                .createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.uid IN :ids", entityClass)
                .setParameter("ids", idsToBackfill)
                .getResultList());

        if (entities == null || entities.isEmpty())
            return;

        List<SyncOutbox> outboxRecords = new ArrayList<>();
        for (Object entity : entities) {
            try {
                String entityId = getEntityId(entity);
                if (entityId == null || entityId.isBlank())
                    continue;

                jakarta.json.JsonObject jsonObj = tools.JsonUtil.jsonify(entity);
                String payload = jsonObj != null ? jsonObj.toString() : null;

                SyncOutbox outbox = new SyncOutbox();
                outbox.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
                outbox.setTableName(table.name());
                outbox.setEntityId(entityId);
                outbox.setAction("PERSIST");
                outbox.setPayload(payload);
                outbox.setCreatedAt(LocalDateTime.now());
                outbox.setUpdatedAt(getUpdatedAt(entity));
                outbox.setEntrepriseId(eUid);
                outbox.setRegion(region);
                outbox.setStatus("PENDING");
                outboxRecords.add(outbox);
            } catch (Exception e) {
                System.err.println("[BACKFILL] Failed to create outbox record: " + e.getMessage());
            }
        }

        if (outboxRecords.isEmpty())
            return;

        submitSyncWrite(em -> {
            for (SyncOutbox outbox : outboxRecords) {
                em.persist(outbox);
            }
            return null;
        });
    }

    // ── Downsync materialization ────────────────────────────────────────────

    /**
     * Matérialise en base locale les enregistrements downsync en respectant
     * strictement l'ordre de dépendance entre les entités : les parents (FK)
     * sont toujours insérés avant leurs enfants (ex. Produit avant Mesure,
     * Vente avant LigneVente, Inventaire avant Compter). L'ordre est obtenu
     * en traitant les enregistrements niveau de priorité par niveau
     * (BackgroundSyncService.getTablePriority), indépendamment de leur ordre
     * d'arrivée / createdAt. Les échecs (parent absent de l'outbox) sont
     * retentés sur quelques passes puis laissés pour le cycle suivant.
     */
    public static void materializeDownsyncRecords() {
        int maxPasses = 3;
        for (int pass = 0; pass < maxPasses; pass++) {
            boolean anyApplied = false;
            List<String> appliedUids = new ArrayList<>();
            for (int phase = 0; phase <= 5; phase++) {
                List<SyncOutbox> phaseRecords = fetchDownsyncPhase(phase);
                if (phaseRecords.isEmpty()) {
                    continue;
                }
                // Au sein d'un même niveau, ordre FIFO (dépendances intra-niveau
                // comme Client.parentId ou Periode.previousPeriod)
                phaseRecords.sort((a, b) -> {
                    LocalDateTime t1 = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime t2 = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return t1.compareTo(t2);
                });
                for (SyncOutbox record : phaseRecords) {
                    if (applyDownsyncRecord(record)) {
                        appliedUids.add(record.getUid());
                        anyApplied = true;
                    }
                }
            }
            if (!appliedUids.isEmpty()) {
                markAsApplied(appliedUids);
            }
            if (!anyApplied) {
                break;
            }
        }
    }

    /**
     * Récupère les enregistrements downsync d'un niveau de priorité donné
     * (ordre de dépendance). Ne charge que les entités de ce niveau pour
     * limiter l'empreinte mémoire.
     */
    private static List<SyncOutbox> fetchDownsyncPhase(int phase) {
        List<String> tables = new ArrayList<>();
        for (Tables t : Tables.values()) {
            if (BackgroundSyncService.getTablePriority(t.name()) == phase) {
                tables.add(t.name());
            }
        }
        if (tables.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String jpql = "SELECT s FROM SyncOutbox s WHERE s.status = 'DOWNSYNCED' AND s.tableName IN :tables";
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, SyncOutbox.class)
                        .setParameter("tables", tables)
                        .getResultList());
            }
            return ManagedSessionFactory.getEntityManager()
                    .createQuery(jpql, SyncOutbox.class)
                    .setParameter("tables", tables)
                    .getResultList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private static boolean applyDownsyncRecord(SyncOutbox record) {
        try {
            String entityType = record.getTableName();
            String payload = record.getPayload();
            String action = record.getAction();
            if (payload == null || payload.isBlank()) {
                if ("REMOVE".equalsIgnoreCase(action) || "DELETE".equalsIgnoreCase(action)) {
                    SyncOutboxListener.runSuppressed(() -> {
                        applyRemoveMutation(entityType, record.getEntityId());
                    });
                    System.out.println("[DOWNSYNC] Removed " + entityType + " " + record.getEntityId());
                    return true;
                }
                System.err.println("[DOWNSYNC] Empty payload for " + entityType + " " + record.getEntityId());
                return false;
            }

            SyncOutboxListener.runSuppressed(() -> {
                applyDownsyncMutation(entityType, record.getAction(), payload);
            });

            System.out.println("[DOWNSYNC] Materialized " + entityType + " " + record.getEntityId());
            return true;
        } catch (Exception e) {
            System.err.println("[DOWNSYNC] Failed to materialize " + record.getTableName()
                    + " " + record.getEntityId() + ": " + e.getMessage());
            SyncLogger.getInstance().log(e, "Downsync materialization failed",
                    record.getTableName(), record.getEntityId());
            return false;
        }
    }

    private static void applyDownsyncMutation(String entityType, String action, String payload) {
        Tables table;
        try {
            table = Tables.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[DOWNSYNC] Unknown table type: " + entityType);
            return;
        }

        Class<?> entityClass = getEntityClass(table);
        if (entityClass == null) return;

        try {
            Object entity = JsonUtil.objectify(payload);
            if (entity == null) return;

            // Garde anti-doublon : un payload sans uid ne peut pas être rattaché à
            // une ligne existante. merge() insérerait alors une nouvelle ligne à
            // chaque matérialisation. On ignore donc la mutation (le payload serveur
            // contient toujours le uid via toSafePayload).
            Object entityUid = getEntityId(entity);
            if (entityUid == null) {
                System.err.println("[DOWNSYNC] Skipping " + entityType + " with null uid (merge would duplicate).");
                return;
            }

            submitSyncWrite(em -> {
                if ("REMOVE".equalsIgnoreCase(action) || "DELETE".equalsIgnoreCase(action)) {
                    // Cast PK to the correct type to avoid ClassCastException with Integer/Long keys
                    Object typedId = getTypedEntityId(entity, table);
                    Object managed = em.find(entityClass, typedId != null ? typedId : getEntityId(entity));
                    if (managed != null) {
                        em.remove(managed);
                    }
                } else {
                    em.merge(entity);
                }
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply downsync mutation for " + entityType, e);
        }
    }

    /**
     * Supprime localement une entite a partir de son type et de son uid.
     * Utilise pour les mutations REMOVE dont le payload est null (comme le JavaFX)
     * et qui ne peuvent donc pas etre deserialisees.
     */
    private static void applyRemoveMutation(String entityType, String entityId) {
        if (entityId == null || entityId.isBlank()) return;
        Tables table;
        try {
            table = Tables.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[DOWNSYNC] Unknown table type: " + entityType);
            return;
        }

        Class<?> entityClass = getEntityClass(table);
        if (entityClass == null) return;

        submitSyncWrite(em -> {
            Object managed = findEntityById(em, entityClass, entityId);
            if (managed != null) {
                em.remove(managed);
            }
            return null;
        });
    }

    private static Object findEntityById(EntityManager em, Class<?> entityClass, String entityId) {
        try {
            Object managed = em.find(entityClass, entityId);
            if (managed != null) return managed;
        } catch (Exception ignored) {
        }
        try {
            Object managed = em.find(entityClass, Integer.valueOf(entityId));
            if (managed != null) return managed;
        } catch (Exception ignored) {
        }
        try {
            Object managed = em.find(entityClass, Long.valueOf(entityId));
            if (managed != null) return managed;
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Retourne l'identifiant de l'entité avec son type correct (Integer pour Vente, Long pour LigneVente, String sinon).
     * Evite les ClassCastException lors du em.find() sur des entités à uid non-String.
     */
    private static Object getTypedEntityId(Object entity, Tables table) {
        try {
            Method getUid = entity.getClass().getMethod("getUid");
            Object val = getUid.invoke(entity);
            if (val == null) return null;
            // Retourner directement la valeur typée : Integer, Long ou String
            return val;
        } catch (Exception ignored) {
        }
        return null;
    }

    // ── Downsync record creation ────────────────────────────────────────────

    public static void createDownsyncRecord(SyncOutboxDto dto) {
        Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
        String eUid = pref.get("eUid", null);
        String region = pref.get("region", "Goma");

        String tableName = dto.entityType.toUpperCase();
        String entityId = dto.entityId;

        // UPSERT : miroir de SyncOutboxWriter côté serveur. Aucun doublon local
        // pour le même couple (tableName, entityId) : si un enregistrement
        // DOWNSYNCED existe déjà (mutation re-téléchargée lors d'un full resync
        // ou d'une fenêtre de rattrapage chevauchante), on met à jour action et
        // payload avec l'état le plus récent au lieu d'insérer une nouvelle ligne.
        submitSyncWrite(em -> {
            List<SyncOutbox> existing = em.createQuery(
                    "SELECT s FROM SyncOutbox s WHERE s.status = 'DOWNSYNCED' AND s.tableName = :tableName AND s.entityId = :entityId",
                    SyncOutbox.class)
                    .setParameter("tableName", tableName)
                    .setParameter("entityId", entityId)
                    .setMaxResults(1)
                    .getResultList();
            if (!existing.isEmpty()) {
                SyncOutbox row = existing.get(0);
                row.setAction(mapMutationType(dto.mutationType));
                row.setPayload(dto.payload);
                row.setUpdatedAt(LocalDateTime.now());
                return null;
            }

            SyncOutbox outbox = new SyncOutbox();
            outbox.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
            outbox.setTableName(tableName);
            outbox.setEntityId(entityId);
            outbox.setAction(mapMutationType(dto.mutationType));
            outbox.setPayload(dto.payload);
            outbox.setCreatedAt(LocalDateTime.now());
            outbox.setUpdatedAt(LocalDateTime.now());
            outbox.setEntrepriseId(eUid);
            outbox.setRegion(region);
            outbox.setStatus("DOWNSYNCED");

            em.persist(outbox);
            return null;
        });
    }

    private static String mapMutationType(String mutationType) {
        if ("INSERT".equalsIgnoreCase(mutationType)) return "PERSIST";
        if ("DELETE".equalsIgnoreCase(mutationType)) return "REMOVE";
        return "UPDATE";
    }

    private static String getEntityId(Object entity) {
        try {
            Method getUid = entity.getClass().getMethod("getUid");
            Object val = getUid.invoke(entity);
            if (val != null) {
                return val.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static LocalDateTime getUpdatedAt(Object entity) {
        try {
            Method m = entity.getClass().getMethod("getUpdatedAt");
            Object res = m.invoke(entity);
            if (res instanceof LocalDateTime) {
                return (LocalDateTime) res;
            }
        } catch (Exception ignored) {
        }
        return LocalDateTime.now();
    }
}
