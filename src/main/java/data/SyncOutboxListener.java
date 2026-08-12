package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import services.ManagedSessionFactory;
import tools.MemoryGuard;
import tools.SyncLogger;
import tools.Tables;

public class SyncOutboxListener {

    private static final int MAX_POOL_RETRY = 3;
    private static final long RETRY_DELAY_MS = 250L;

    private static final java.util.concurrent.ExecutorService outboxExecutor =
        MemoryGuard.newSingleThreadExecutor("Kazisafe-SyncOutbox-Logger");

    private static final ThreadLocal<Boolean> suppressListener =
        ThreadLocal.withInitial(() -> false);

    public static boolean isSuppressed() {
        return suppressListener.get();
    }

    public static void setSuppressed(boolean value) {
        suppressListener.set(value);
    }

    public static void runSuppressed(Runnable action) {
        boolean previous = suppressListener.get();
        suppressListener.set(true);
        try {
            action.run();
        } finally {
            suppressListener.set(previous);
        }
    }

    @PrePersist
    public void onPrePersist(Object entity) {
        if (suppressListener.get()) return;
        logMutation(entity, "PERSIST");
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        if (suppressListener.get()) return;
        logMutation(entity, "UPDATE");
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        if (suppressListener.get()) return;
        logMutation(entity, "REMOVE");
    }

    private void logMutation(Object entity, String action) {
        if (entity instanceof SyncOutbox) {
            return;
        }
        if (!(entity instanceof BaseModel)) {
            return;
        }
        BaseModel baseModel = (BaseModel) entity;
        String type = baseModel.getType();
        if (type == null) {
            return;
        }

        // Validate table
        try {
            Tables.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        String entityId = getEntityId(baseModel);
        if (entityId == null) {
            return;
        }

        String payload = null;
        if (!"REMOVE".equals(action)) {
            try {
                jakarta.json.JsonObject jsonObj = tools.JsonUtil.jsonify(
                    baseModel
                );
                if (jsonObj != null) {
                    payload = jsonObj.toString();
                }
            } catch (Exception e) {
                SyncLogger.getInstance().log(
                    e,
                    "Failed to jsonify entity: " + type,
                    type,
                    entityId
                );
            }
        }

        final String finalType = type.toUpperCase();
        final String finalEntityId = entityId;
        final String finalPayload = payload;
        System.out.println("========================= L'etat de final payload "+finalPayload+"===============================");
        outboxExecutor.submit(() -> {
            int attempt = 0;
            while (true) {
                try {
                    persistOutboxRecord(
                        action,
                        finalType,
                        finalEntityId,
                        finalPayload,
                        entity
                    );
                    return;
                } catch (Exception ex) {
                    attempt++;
                    boolean retryable =
                        isPoolExhaustion(ex) && attempt <= MAX_POOL_RETRY;
                    if (!retryable) {
                        SyncLogger.getInstance().log(
                            ex,
                            "SyncOutboxListener: failed to persist outbox record",
                            finalType,
                            finalEntityId
                        );
                        return;
                    }
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        SyncLogger.getInstance().log(
                            ex,
                            "SyncOutboxListener: interrupted while retrying outbox persistence",
                            finalType,
                            finalEntityId
                        );
                        return;
                    }
                }
            }
        });

        // Push local mutations to open UIs immediately (SSE already calls notifySynced)
        try {
            tools.NotificationHandler.broadcastDataSynced(baseModel);
        } catch (Throwable ignored) {
            // UI layer may be unavailable during early bootstrap / headless tests
        }
    }

    private void persistOutboxRecord(
        String action,
        String finalType,
        String finalEntityId,
        String finalPayload,
        Object entity
    ) throws Exception {
        // La région et l'entreprise sont lues sur le MÊME nœud de préférences que
        // le backfill et la synchronisation (tools.SyncEngine) : l'ancienne lecture
        // sur le nœud data.SyncOutboxListener ne contenait jamais la région écrite
        // par l'UI et retombait sur "Unknown", rendant les enregistrements de
        // l'outbox invisibles au backfill (filtre région "Goma") → doublons.
        String eUid = java.util.prefs.Preferences.userNodeForPackage(
            tools.SyncEngine.class
        ).get("eUid", null);
        String region = java.util.prefs.Preferences.userNodeForPackage(
            tools.SyncEngine.class
        ).get("region", "Goma");

        LocalDateTime updatedAt = getUpdatedAt(entity);

        // Upsert : on réutilise le dernier enregistrement PENDING/UNSYNCED/FAILED
        // de la même entité au lieu d'en insérer un nouveau à chaque mutation.
        // Sans cela, chaque modification créait un doublon (PERSIST + UPDATE +
        // UPDATE...) jamais consolidé par le backfill.
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                SyncOutbox outbox = findPendingOutbox(em, finalType, finalEntityId);
                if (outbox == null) {
                    outbox = new SyncOutbox();
                    outbox.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
                    outbox.setTableName(finalType);
                    outbox.setEntityId(finalEntityId);
                    outbox.setCreatedAt(LocalDateTime.now());
                    outbox.setEntrepriseId(eUid);
                    outbox.setStatus("PENDING");
                    outbox.setRetryCount(0);
                    em.persist(outbox);
                }
                outbox.setAction(action);
                outbox.setPayload(finalPayload);
                outbox.setUpdatedAt(updatedAt);
                outbox.setRegion(region);
                return null;
            }).get(); // Block the outbox executor thread to preserve sequence
            return;
        }

        ManagedSessionFactory.runInSession(em -> {
            jakarta.persistence.EntityTransaction tx = em.getTransaction();
            boolean activeTx = tx.isActive();
            if (!activeTx) {
                tx.begin();
            }
            try {
                SyncOutbox outbox = findPendingOutbox(em, finalType, finalEntityId);
                if (outbox == null) {
                    outbox = new SyncOutbox();
                    outbox.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
                    outbox.setTableName(finalType);
                    outbox.setEntityId(finalEntityId);
                    outbox.setCreatedAt(LocalDateTime.now());
                    outbox.setEntrepriseId(eUid);
                    outbox.setStatus("PENDING");
                    outbox.setRetryCount(0);
                    em.persist(outbox);
                }
                outbox.setAction(action);
                outbox.setPayload(finalPayload);
                outbox.setUpdatedAt(updatedAt);
                outbox.setRegion(region);
                if (!activeTx) {
                    tx.commit();
                }
            } catch (Exception e) {
                if (!activeTx && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        });
    }

    private SyncOutbox findPendingOutbox(
        EntityManager em,
        String tableName,
        String entityId
    ) {
        try {
            List<SyncOutbox> results = em
                .createQuery(
                    "SELECT s FROM SyncOutbox s WHERE s.tableName = :tableName AND s.entityId = :entityId AND s.status IN ('PENDING','UNSYNCED','FAILED') ORDER BY s.createdAt DESC",
                    SyncOutbox.class
                )
                .setParameter("tableName", tableName)
                .setParameter("entityId", entityId)
                .setMaxResults(1)
                .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            SyncLogger
                .getInstance()
                .log(
                    e,
                    "SyncOutboxListener: failed to find pending outbox record",
                    tableName,
                    entityId
                );
            return null;
        }
    }

    private LocalDateTime getUpdatedAt(Object entity) {
        try {
            java.lang.reflect.Method m = entity
                .getClass()
                .getMethod("getUpdatedAt");
            Object res = m.invoke(entity);
            if (res instanceof LocalDateTime) {
                return (LocalDateTime) res;
            }
        } catch (Exception ignored) {}
        return LocalDateTime.now();
    }

    private boolean isPoolExhaustion(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (
                    lower.contains(
                        "connection pool has reached its maximum size"
                    ) ||
                    lower.contains("no connection is currently available") ||
                    lower.contains("unable to acquire jdbc connection") ||
                    lower.contains("connection is not available")
                ) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String getEntityId(BaseModel entity) {
        try {
            java.lang.reflect.Method getUid = entity
                .getClass()
                .getMethod("getUid");
            Object val = getUid.invoke(entity);
            if (val != null) {
                return val.toString();
            }
            // Generate UUID if null (e.g. pre-persist)
            java.lang.reflect.Method setUid = entity
                .getClass()
                .getMethod("setUid", String.class);
            String newUid = UUID.randomUUID()
                .toString()
                .toLowerCase()
                .replaceAll("-", "");
            setUid.invoke(entity, newUid);
            return newUid;
        } catch (Exception e) {
            // fallback
        }
        return null;
    }
}
