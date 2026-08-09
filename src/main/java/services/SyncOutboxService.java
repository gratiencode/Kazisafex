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

    /**
     * Rétention des enregistrements APPLIED : un enregistrement appliqué n'est
     * supprimé qu'après ce nombre de jours. En dessous de ce seuil, l'historique
     * APPLIED est conservé (utile pour le diagnostic et pour que le backfill ne
     * recrée pas des mutations déjà envoyées). Un APPLIED n'est jamais re-sélectionné
     * par l'upsync (seuls PENDING/UNSYNCED le sont), la rétention n'alourdit donc
     * pas les batches envoyés.
     */
    public static final int APPLIED_RETENTION_DAYS = 10;

    /**
     * Récupère toutes les outbox PENDING/UNSYNCED à remonter (synchronisation
     * complète et cycle de fond). Aucun filtre d'ancienneté ni de plafond de
     * retries : un enregistrement rejeté par le serveur n'est jamais abandonné
     * définitivement — il repasse {@code UNSYNCED} (compteur incrémenté) et reste
     * éligible aux cycles suivants. Les {@code FAILED} résiduels (ancien
     * plafond) sont re-inclus pour être retentés une dernière fois. La
     * terminaison de chaque cycle est garantie par la déduplication des UID déjà
     * tentés dans {@code BackgroundSyncService} et par le recul (backoff) quand
     * aucun enregistrement ne progresse.
     */
    public static List<SyncOutbox> fetchAllPendingOutbox() {
        String jpql = "SELECT s FROM SyncOutbox s WHERE s.status IN ('PENDING','UNSYNCED','FAILED') ORDER BY s.createdAt ASC";
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, SyncOutbox.class)
                        .getResultList());
            }
            return ManagedSessionFactory.getEntityManager()
                    .createQuery(jpql, SyncOutbox.class)
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

    /**
     * Marque un enregistrement comme {@code UNSYNCED} pour nouvel essai. Le
     * compteur {@code retryCount} est incrémenté à titre informatif (utile au
     * diagnostic) mais n'entraîne <strong>jamais</strong> de passage en
     * {@code FAILED} : une mutation rejetée par le serveur reste éligible aux
     * cycles suivants jusqu'à être acceptée.
     */
    public static void markAsUnsynced(List<String> uids) {
        if (uids == null || uids.isEmpty()) return;
        submitSyncWrite(em -> {
            for (String uid : uids) {
                SyncOutbox record = em.find(SyncOutbox.class, uid);
                if (record != null) {
                    record.setRetryCount(record.getRetryCount() + 1);
                    record.setStatus("UNSYNCED");
                }
            }
            return null;
        });
    }

    /**
     * Remet toute l'outbox en état de re-synchronisation complète (bouton
     * « Réinitialiser la synchronisation ») : les mutations déjà appliquées ou
     * en échec repassent {@code UNSYNCED} avec un compteur de tentatives vierge,
     * pour être re-téléversées depuis le début. Les enregistrements
     * {@code DOWNSYNCED} (provenance serveur) ne sont volontairement pas
     * remontés : le rattrapage {@code since=0} les re-téléchargera de toute façon.
     */
    public static void resetForFullResync() {
        submitSyncWrite(em -> {
            int updated = em.createQuery(
                    "UPDATE SyncOutbox s SET s.status = 'UNSYNCED', s.retryCount = 0 "
                            + "WHERE s.status IN ('APPLIED','FAILED')")
                    .executeUpdate();
            System.out.println("[SYNC-OUTBOX] Reinitialisation pour full resync : " + updated + " enregistrement(s) a re-televerser.");
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

    /**
     * Supprime les enregistrements APPLIED de plus de
     * {@link #APPLIED_RETENTION_DAYS} jours : la suppression n'intervient que
     * sur les données de rétention, les APPLIED récents restent conservés.
     */
    public static void cleanupAppliedRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(APPLIED_RETENTION_DAYS);
        submitSyncWrite(em -> {
            em.createQuery("DELETE FROM SyncOutbox s WHERE s.status = 'APPLIED' AND s.createdAt < :cutoff")
                    .setParameter("cutoff", cutoff)
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
            } catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new RuntimeException("SyncOutbox write failed", cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("SyncOutbox write interrupted", e);
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
            String regionKey = record.getRegion() == null ? "" : record.getRegion();
            String keyBase = record.getTableName() + "|" + record.getEntityId() + "|" + regionKey;

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
        // Le backfill est une réconciliation idempotente : pour chaque entité, on
        // compare le payload stocké dans l'outbox avec l'objet réel (tous champs,
        // y compris les dates) et on ne crée/met à jour l'outbox que lorsque
        // l'objet a vraiment muté. Aucun enregistrement n'est recréé pour des
        // données inchangées : le timestamp « since » reste donc opérant.
        System.out.println("[BACKFILL] Scanning all tables for missing or stale outbox records (eUid=" + eUid + ")...");

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

        // Nettoyage: aucun enregistrement d'outbox avec payload null ne doit subsister.
        deleteNullPayloadRecords();

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

        // Charger en une seule requête les enregistrements d'outbox du table+region.
        // On retient par entité le dernier enregistrement upsync (PENDING/UNSYNCED/
        // APPLIED/FAILED) pour comparer son payload avec l'objet réel, et on
        // mémorise les entités couvertes UNIQUEMENT par du downsync (elles viennent
        // du serveur : rien à remonter tant qu'elles ne sont pas modifiées).
        Map<String, SyncOutbox> latestByEntity = new HashMap<>();
        Set<String> downsyncOnlyEntities = new HashSet<>();
        try {
            List<SyncOutbox> existing = ManagedSessionFactory.executeRead(em -> em
                    .createQuery("SELECT s FROM SyncOutbox s WHERE s.tableName = :tableName AND s.region = :region",
                            SyncOutbox.class)
                    .setParameter("tableName", table.name())
                    .setParameter("region", region)
                    .getResultList());
            Map<String, Boolean> hasUpsync = new HashMap<>();
            Map<String, Boolean> hasDownsync = new HashMap<>();
            for (SyncOutbox record : existing) {
                String entityId = record.getEntityId();
                if (entityId == null || entityId.isBlank()) {
                    continue;
                }
                if ("DOWNSYNCED".equals(record.getStatus())) {
                    hasDownsync.put(entityId, true);
                } else {
                    hasUpsync.put(entityId, true);
                    SyncOutbox prev = latestByEntity.get(entityId);
                    if (prev == null || isNewer(record, prev)) {
                        latestByEntity.put(entityId, record);
                    }
                }
            }
            for (String entityId : hasDownsync.keySet()) {
                if (!hasUpsync.containsKey(entityId)) {
                    downsyncOnlyEntities.add(entityId);
                }
            }
        } catch (Exception e) {
            System.err.println("[BACKFILL] Failed to load existing outbox for " + table.name() + ": " + e.getMessage());
        }

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

        int batchSize = 100;
        int created = 0;
        int updated = 0;
        for (int i = 0; i < allEntityIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allEntityIds.size());
            List<String> batch = allEntityIds.subList(i, end);
            try {
                int[] counts = backfillBatch(table, entityClass, batch, eUid, region,
                        latestByEntity, downsyncOnlyEntities);
                created += counts[0];
                updated += counts[1];
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("[BACKFILL] Error backfilling batch for " + table.name() + ": " + e.getMessage());
            }
        }

        System.out.println("[BACKFILL] Table " + table.name() + ": done (" + created + " created, " + updated
                + " updated, " + allEntityIds.size() + " total).");
    }

    /**
     * Compare deux enregistrements d'outbox : le plus récent est celui dont le
     * {@code createdAt} est le plus grand.
     */
    private static boolean isNewer(SyncOutbox candidate, SyncOutbox current) {
        LocalDateTime c1 = candidate.getCreatedAt();
        LocalDateTime c2 = current.getCreatedAt();
        if (c1 == null) {
            return false;
        }
        if (c2 == null) {
            return true;
        }
        return c1.isAfter(c2);
    }

    /**
     * Détermine si l'objet a vraiment muté par rapport à l'enregistrement
     * d'outbox stocké :
     * <ul>
     * <li>payload stocké nul/vide : toujours considéré comme muté ;</li>
     * <li>le createdAt/updatedAt de l'outbox est comparé à l'updatedAt de
     * l'objet : si l'objet a été modifié après la création/dernière mise à jour
     * de l'outbox, le payload stocké est périmé ;</li>
     * <li>comparaison de tous les champs : payload complet du stocké vs celui de
     * l'objet actuel.</li>
     * </ul>
     */
    private static boolean hasMutated(SyncOutbox record, String currentPayload, Object entity) {
        String storedPayload = record.getPayload();
        if (storedPayload == null || storedPayload.isBlank()) {
            return true;
        }
        LocalDateTime objUpdatedAt = getRealUpdatedAt(entity);
        if (objUpdatedAt != null) {
            LocalDateTime recUpdatedAt = record.getUpdatedAt();
            if (recUpdatedAt == null || objUpdatedAt.isAfter(recUpdatedAt)) {
                return true;
            }
            LocalDateTime recCreatedAt = record.getCreatedAt();
            if (recCreatedAt != null && objUpdatedAt.isAfter(recCreatedAt)) {
                return true;
            }
        }
        return !storedPayload.equals(currentPayload);
    }

    private static LocalDateTime getRealUpdatedAt(Object entity) {
        try {
            Method m = entity.getClass().getMethod("getUpdatedAt");
            Object res = m.invoke(entity);
            if (res instanceof LocalDateTime) {
                return (LocalDateTime) res;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int[] backfillBatch(Tables table, Class<?> entityClass, List<String> idsToBackfill, String eUid,
            String region, Map<String, SyncOutbox> latestByEntity, Set<String> downsyncOnlyEntities) {
        List<?> entities = ManagedSessionFactory.executeRead(em -> em
                .createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.uid IN :ids", entityClass)
                .setParameter("ids", idsToBackfill)
                .getResultList());

        if (entities == null || entities.isEmpty())
            return new int[] { 0, 0 };

        List<SyncOutbox> toCreate = new ArrayList<>();
        List<SyncOutbox> toUpdate = new ArrayList<>();
        int created = 0;
        int updated = 0;
        for (Object entity : entities) {
            try {
                String entityId = getEntityId(entity);
                if (entityId == null || entityId.isBlank())
                    continue;

                jakarta.json.JsonObject jsonObj = tools.JsonUtil.jsonify(entity);
                if (jsonObj == null) {
                    System.err.println("[BACKFILL] Skipping entity without serializable payload: " + entityId);
                    continue;
                }
                String payload = jsonObj.toString();
                if (payload.isBlank()) {
                    System.err.println("[BACKFILL] Skipping entity with empty payload: " + entityId);
                    continue;
                }

                SyncOutbox existing = latestByEntity.get(entityId);
                if (existing == null) {
                    // Entité couverte uniquement par du downsync : elle vient du
                    // serveur, rien à remonter tant qu'elle n'est pas modifiée.
                    if (downsyncOnlyEntities.contains(entityId)) {
                        continue;
                    }
                    // Aucun enregistrement d'outbox : création directe.
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
                    toCreate.add(outbox);
                    created++;
                } else if (hasMutated(existing, payload, entity)) {
                    // L'objet a vraiment muté : on rafraîchit l'enregistrement
                    // existant (payload + updatedAt) au lieu d'en créer un doublon.
                    existing.setPayload(payload);
                    existing.setUpdatedAt(getUpdatedAt(entity));
                    if ("APPLIED".equals(existing.getStatus()) || "FAILED".equals(existing.getStatus())) {
                        existing.setStatus("PENDING");
                    }
                    if ("REMOVE".equalsIgnoreCase(existing.getAction())) {
                        existing.setAction("PERSIST");
                    }
                    toUpdate.add(existing);
                    updated++;
                }
                // sinon : payload identique et dates cohérentes, rien à faire.
            } catch (Exception e) {
                System.err.println("[BACKFILL] Failed to process entity: " + e.getMessage());
            }
        }

        if (!toCreate.isEmpty() || !toUpdate.isEmpty()) {
            submitSyncWrite(em -> {
                for (SyncOutbox outbox : toCreate) {
                    em.persist(outbox);
                }
                for (SyncOutbox outbox : toUpdate) {
                    em.merge(outbox);
                }
                return null;
            });
        }
        return new int[] { created, updated };
    }

    // ── Downsync materialization ────────────────────────────────────────────

    /**
     * Supprime tous les enregistrements d'outbox dont le payload est null ou vide:
     * ils ne peuvent pas etre materialises ni synchronises. Aucune exception,
     * y compris pour les actions REMOVE/DELETE.
     */
    public static void deleteNullPayloadRecords() {
        submitSyncWrite(em -> {
            int deleted = em.createQuery(
                    "DELETE FROM SyncOutbox s WHERE s.payload IS NULL OR s.payload = ''")
                    .executeUpdate();
            if (deleted > 0) {
                System.out.println("[SYNC-OUTBOX] Removed " + deleted
                        + " outbox record(s) with null/empty payload.");
            }
            return null;
        });
    }

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
            if (isMissingParentException(e)) {
                // Parent (FK) pas encore matérialisé : condition transitoire et
                // auto-cicatrisante. On retente au cycle suivant sans loguer
                // d'erreur fatale en télémetrie.
                System.out.println("[DOWNSYNC] " + record.getTableName() + " " + record.getEntityId()
                        + " : parent non encore matérialisé, retenté au prochain cycle.");
                return false;
            }
            System.err.println("[DOWNSYNC] Failed to materialize " + record.getTableName()
                    + " " + record.getEntityId() + ": " + e.getMessage());
            SyncLogger.getInstance().log(e, "Downsync materialization failed",
                    record.getTableName(), record.getEntityId());
            return false;
        }
    }

    /**
     * Indique si l'échec provient d'un parent (FK) absent ou non matérialisé
     * localement, quel que soit le type d'entité. Hibernate lève différentes
     * exceptions selon l'entité et la nature de la référence :
     * <ul>
     * <li>{@code EntityNotFoundException} / {@code ObjectNotFoundException} :
     * référence par identifiant vers une ligne absente (merge de Mesure,
     * Stocker, LigneVente, PrixDeVente, ...) ;</li>
     * <li>{@code PropertyValueException} : propriété non-null référençant une
     * valeur nulle ou transiente ;</li>
     * <li>{@code TransientObjectException} : référence vers une instance
     * non persistée (« unsaved transient instance »).</li>
     * </ul>
     * Ces conditions sont transitoires : le cycle de synchronisation suivant
     * matérialisera le parent et l'enregistrement sera retenté.
     */
    private static boolean isMissingParentException(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof jakarta.persistence.EntityNotFoundException
                    || t instanceof org.hibernate.PropertyValueException
                    || t instanceof org.hibernate.TransientObjectException) {
                return true;
            }
        }
        return false;
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
                    if (entity instanceof data.Produit produit) {
                        resolveProduitCategory(em, produit);
                    }
                    em.merge(entity);
                }
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply downsync mutation for " + entityType, e);
        }
    }

    /**
     * Un produit sans catégorie se voit attribuer la catégorie par défaut
     * "Divers" (on vérifie d'abord si elle existe ; sinon on la crée puis on
     * l'attribue). Une catégorie *référencée* mais non encore matérialisée est
     * laissée telle quelle : l'ordre de matérialisation (parents avant enfants)
     * la livrera dans une passe ultérieure.
     */
    private static void resolveProduitCategory(EntityManager em, data.Produit produit) {
        data.Category category = produit.getCategoryId();
        if (category == null || category.getUid() == null || category.getUid().isBlank()) {
            produit.setCategoryId(findOrCreateDiversCategory(em));
        }
    }

    /** UID déterministe de la catégorie "Divers" de repli (idempotent). */
    static final String DIVERS_CATEGORY_UID = UUID
            .nameUUIDFromBytes("kazisafe-default-divers".getBytes())
            .toString()
            .replaceAll("-", "");

    private static data.Category findOrCreateDiversCategory(EntityManager em) {
        try {
            List<data.Category> cats = em.createQuery(
                    "SELECT c FROM Category c WHERE LOWER(c.descritption) = LOWER(:name)",
                    data.Category.class)
                    .setParameter("name", "Divers")
                    .getResultList();
            if (!cats.isEmpty()) {
                return cats.get(0);
            }
        } catch (RuntimeException ex) {
            // Contexte léger (EM incomplet) : on retombe sur l'uid déterministe.
        }
        data.Category existing = em.find(data.Category.class, DIVERS_CATEGORY_UID);
        if (existing != null) {
            return existing;
        }
        data.Category divers = new data.Category(DIVERS_CATEGORY_UID, "Divers");
        data.Category managed = em.merge(divers);
        return managed != null ? managed : divers;
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

        // Aucun enregistrement d'outbox n'est cree avec un payload null ou vide:
        // un tel enregistrement ne peut pas etre materialise et polluerait la file.
        if (dto.payload == null || dto.payload.isBlank()) {
            System.out.println("[SYNC-OUTBOX] Skipping downsync record with null/empty payload for "
                    + tableName + " " + entityId);
            return;
        }

        // UPSERT : miroir de SyncOutboxWriter côté serveur. Aucun doublon local
        // pour le triplet (tableName, entityId, region) : si un enregistrement
        // DOWNSYNCED existe déjà (mutation re-téléchargée lors d'un full resync
        // ou d'une fenêtre de rattrapage chevauchante), on met à jour action et
        // payload avec l'état le plus récent au lieu d'insérer une nouvelle ligne.
        submitSyncWrite(em -> {
            // Pas de setMaxResults : le triplet (tableName, entityId, region)
            // est unique pour le statut DOWNSYNCED (upsert), et SQLite ne gère
            // pas la syntaxe ANSI "fetch first ? rows only" générée par le
            // dialecte générique de Hibernate.
            List<SyncOutbox> existing = em.createQuery(
                    "SELECT s FROM SyncOutbox s WHERE s.status = 'DOWNSYNCED' AND s.tableName = :tableName AND s.entityId = :entityId AND s.region = :region",
                    SyncOutbox.class)
                    .setParameter("tableName", tableName)
                    .setParameter("entityId", entityId)
                    .setParameter("region", region)
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
