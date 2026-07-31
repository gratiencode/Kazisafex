package data;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.UUID;
import services.ManagedSessionFactory;
import tools.SyncLogger;
import tools.Tables;

public class SyncOutboxListener {

    private static final int MAX_POOL_RETRY = 3;
    private static final long RETRY_DELAY_MS = 250L;

    private static final java.util.concurrent.ExecutorService outboxExecutor =
        java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Kazisafe-SyncOutbox-Logger");
            t.setDaemon(true);
            return t;
        });

    private static final ThreadLocal<Boolean> suppressListener =
        ThreadLocal.withInitial(() -> false);

    public static void runSuppressed(Runnable action) {
        suppressListener.set(true);
        try {
            action.run();
        } finally {
            suppressListener.set(false);
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
        String eUid = java.util.prefs.Preferences.userNodeForPackage(
            SyncOutboxListener.class
        ).get("eUid", null);
        String region = java.util.prefs.Preferences.userNodeForPackage(
            SyncOutboxListener.class
        ).get("region", "Unknown");

        LocalDateTime updatedAt = getUpdatedAt(entity);

        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                SyncOutbox outbox = new SyncOutbox();
                outbox.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
                outbox.setTableName(finalType);
                outbox.setEntityId(finalEntityId);
                outbox.setAction(action);
                outbox.setPayload(finalPayload);
                outbox.setCreatedAt(LocalDateTime.now());
                outbox.setUpdatedAt(updatedAt);
                outbox.setEntrepriseId(eUid);
                outbox.setRegion(region);
                outbox.setStatus("PENDING");
                em.persist(outbox);
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
                SyncOutbox outbox = new SyncOutbox();
                outbox.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
                outbox.setTableName(finalType);
                outbox.setEntityId(finalEntityId);
                outbox.setAction(action);
                outbox.setPayload(finalPayload);
                outbox.setCreatedAt(LocalDateTime.now());
                outbox.setUpdatedAt(updatedAt);
                outbox.setEntrepriseId(eUid);
                outbox.setRegion(region);
                outbox.setStatus("PENDING");
                em.persist(outbox);
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
