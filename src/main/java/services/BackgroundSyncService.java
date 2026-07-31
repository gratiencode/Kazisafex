package services;

import data.SyncOutbox;
import data.network.Kazisafe;
import data.network.dto.BatchMutationDto;
import data.network.dto.BatchResultDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import retrofit2.Response;
import tools.SyncLogger;
import tools.Tables;
import tools.Util;

public class BackgroundSyncService extends Service<Void> {

    private final Kazisafe kazisafe;
    private final int pollIntervalSeconds;
    private final Consumer<String> uiStatusUpdater;
    private boolean isPaused = false;
    private static BackgroundSyncService instance;

    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int MIN_BATCH_SIZE = 5;
    private static final int MAX_BATCH_SIZE = 200;
    private int adaptiveBatchSize = DEFAULT_BATCH_SIZE;
    private long adaptivePauseUntilEpochMs = 0L;

    public BackgroundSyncService(Kazisafe kazisafe, int pollIntervalSeconds) {
        this(kazisafe, pollIntervalSeconds, null);
    }

    public BackgroundSyncService(
            Kazisafe kazisafe,
            int pollIntervalSeconds,
            Consumer<String> uiStatusUpdater
    ) {
        this.kazisafe = kazisafe;
        this.pollIntervalSeconds = pollIntervalSeconds;
        this.uiStatusUpdater = uiStatusUpdater;
        instance = this;
    }

    public static BackgroundSyncService getInstance() {
        return instance;
    }

    public synchronized void pauseSync() {
        if (!isPaused) {
            isPaused = true;
            System.out.println(
                    "BackgroundSyncService: Paused due to network/connectivity changes."
            );
        }
    }

    public synchronized void resumeSync() {
        if (isPaused) {
            isPaused = false;
            System.out.println(
                    "BackgroundSyncService: Resumed sync operations."
            );
        }
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    if (isPaused()) {
                        publishStatus("Sync paused.");
                        safeSleepSeconds(5);
                        continue;
                    }

                    if (!Util.isInternetAndBaseApiReachable()) {
                        publishStatus(
                                "Connection lost... waiting to reconnect."
                        );
                        safeSleepSeconds(5);
                        continue;
                    }

                    try {
                        java.util.prefs.Preferences pref
                                = java.util.prefs.Preferences.userNodeForPackage(
                                        tools.SyncEngine.class
                                );
                        String enterpriseId = pref.get("eUid", null);
                        String regionId = pref.get("region", "Goma");
                        if (enterpriseId != null) {
                            services.StateValidationService.ValidationResult res
                                    = services.StateValidationService.validateState(
                                            kazisafe,
                                            enterpriseId,
                                            regionId
                                    );
                            if (!res.isSynchronized) {
                                publishStatus("Désynchronisation détectée. Réparation en cours...");
                                services.FullResyncService.performFullResync(
                                        kazisafe,
                                        enterpriseId,
                                        regionId,
                                        BackgroundSyncService.this::publishStatus
                                );
                                safeSleepSeconds(10);
                            }
                        }
                    } catch (Exception e) {
                        SyncLogger.getInstance().log(
                                e,
                                "State validation failed",
                                null,
                                null
                        );
                    }

                    long now = System.currentTimeMillis();
                    if (adaptivePauseUntilEpochMs > now) {
                        long remainMs = adaptivePauseUntilEpochMs - now;
                        publishStatus(
                                "Server busy - slowing down sync rate..."
                        );
                        safeSleepMillis(Math.min(remainMs, 5000L));
                        continue;
                    }

                    try {
                        // Clear any stale deferred rectifications from a previous interrupted cycle
                        SyncEpochManager.discardAll();

                        // Correct and clean outbox data before syncing
                        SyncOutboxService.correctAndCleanOutboxData();
                        
                        boolean hasMoreOutbox = true;
                        while (hasMoreOutbox && !isCancelled()) {
                            List<SyncOutbox> chunk= SyncOutboxService.fetchOutboxChunk(adaptiveBatchSize);
                            if (chunk == null || chunk.isEmpty()) {
                                hasMoreOutbox = false;
                                maybeIncreaseBatchSize();
                                break;
                            }

                            SyncOutcome outcome = processUpsyncChunk(chunk);
                            if (outcome.rateLimited) {
                                handleRateLimit(outcome);
                                break;
                            }
                            if (!outcome.success) {
                                break;
                            }

                            maybeIncreaseBatchSize();
                        }

                        // Materialize downsync records
                        SyncOutboxService.materializeDownsyncRecords();

                        // Flush deferred stock rectifications accumulated during downsync
                        SyncEpochManager.flush();

                        // Clean up applied records
                        SyncOutboxService.cleanupAppliedRecords();
                    } catch (Exception e) {
                        SyncLogger.getInstance().log(
                                e,
                                "BackgroundSyncService: Error during sync cycle",
                                null,
                                null
                        );
                    }

                    safeSleepSeconds(pollIntervalSeconds);
                }
                return null;
            }
        };
    }

    private SyncOutcome processUpsyncChunk(List<SyncOutbox> chunk) {
        // 1. Hierarchical Sort (Dependency Level, then Timestamp)
        chunk.sort((r1, r2) -> {
            int p1 = getTablePriority(r1.getTableName());
            int p2 = getTablePriority(r2.getTableName());
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
            return r1.getUpdatedAt() != null
                    ? r1.getUpdatedAt().compareTo(r2.getUpdatedAt() != null? r2.getUpdatedAt(): LocalDateTime.MIN)
                    : r2.getUpdatedAt() != null? -1: 0;
        });

        // 2. Convert to BatchMutationDto
        List<BatchMutationDto> mutations = chunk
                .stream()
                .map(r -> {
                    BatchMutationDto dto = new BatchMutationDto();
                    dto.entityId = r.getEntityId();
                    dto.entityType = r.getTableName();
                    dto.payload = r.getPayload();
                    dto.updatedAt = r.getUpdatedAt();
                    dto.mutationType = r.getAction().replace("PERSIST", "INSERT");
                    dto.entrepriseId = r.getEntrepriseId();
                    dto.region = r.getRegion();
                    return dto;
                })
                .collect(Collectors.toList());

        if (mutations.isEmpty()) {
            return SyncOutcome.success();
        }

        try {
            System.out.println("[SYNC-UPSYNC] Sending batch of "+ mutations.size()+ " mutations to adaptive endpoint.");
            Response<BatchResultDto> response = kazisafe
                    .adaptiveUpsync(mutations)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                BatchResultDto result = response.body();

                // Handle Successes
                if (result.successes != null) {
                    List<String> successUids = chunk
                            .stream()
                            .filter(r-> result.successes.stream().anyMatch(s-> s.entityId.equals(r.getEntityId())
                                            && s.entityType.equals(r.getTableName())))
                            .map(SyncOutbox::getUid)
                            .collect(Collectors.toList());

                    if (!successUids.isEmpty()) {
                        System.out.println("[SYNC-UPSYNC] Successfully synced "+ successUids.size()+ "/"+mutations.size()+" records.");
                        SyncOutboxService.markAsApplied(successUids);
                    }
                }

                // Handle Failures: mark as UNSYNCED (will be retried, up to 5 times)
                if (result.failures != null) {
                    List<String> failedUids = chunk
                            .stream()
                            .filter(r-> result.failures.stream().anyMatch(s-> s.entityId.equals(r.getEntityId())
                                            && s.entityType.equals(r.getTableName())))
                            .map(SyncOutbox::getUid)
                            .collect(Collectors.toList());

                    if (!failedUids.isEmpty()) {
                        System.out.println("[SYNC-UPSYNC] "+ failedUids.size()+ " mutations failed, marking as UNSYNCED for retry.");
                        SyncOutboxService.markAsUnsynced(failedUids);
                    }
                }

                return SyncOutcome.success();
            } else if (response.code() == 429) {
                String retryBatchHeader = response.headers().get("Retry-With-Batch-Size"); 
                String retryAfterHeader = response.headers().get("Retry-After"); 
                System.out.println("[SYNC-UPSYNC] HTTP 429 Rate Limit. Reducing batch size.");
                return SyncOutcome.rateLimited(parsePositiveInt(retryBatchHeader,Math.max(MIN_BATCH_SIZE, adaptiveBatchSize / 2)),
                        parsePositiveInt(retryAfterHeader, 5));
            } else {
                System.err.println("[SYNC-UPSYNC] Server error: " + response.code());
                // Mark entire chunk as UNSYNCED for retry
                List<String> allUids = chunk.stream().map(SyncOutbox::getUid).collect(Collectors.toList());
                SyncOutboxService.markAsUnsynced(allUids);
                return SyncOutcome.failed();
            }
        } catch (Exception e) {
            // Check if this is an interruption-related exception
            if (e instanceof InterruptedException
                    || e instanceof java.io.InterruptedIOException
                    || (e.getCause() != null
                    && (e.getCause() instanceof InterruptedException
                    || e.getCause() instanceof java.io.InterruptedIOException))) {
                System.out.println(
                        "[SYNC-UPSYNC] Interrupted - stopping this batch"
                );
                // Re-interrupt the thread to preserve interrupted status
                Thread.currentThread().interrupt();
                return SyncOutcome.failed();
            }

            SyncLogger.getInstance().log(
                    e,
                    "BackgroundSyncService: adaptiveUpsync failed",
                    "UPSYNC",
                    null
            );
            return SyncOutcome.failed();
        }
    }

    private void handleRateLimit(SyncOutcome outcome) {
        adaptiveBatchSize = Math.max(
                MIN_BATCH_SIZE,
                Math.min(MAX_BATCH_SIZE, outcome.suggestedBatchSize)
        );
        int retryAfterSeconds = Math.max(2, outcome.retryAfterSeconds);
        adaptivePauseUntilEpochMs
                = System.currentTimeMillis() + retryAfterSeconds * 1000L;
        publishStatus(
                "Server busy - slowing down sync rate... (batch="
                + adaptiveBatchSize
                + ", pause="
                + retryAfterSeconds
                + "s)"
        );
    }

    private void maybeIncreaseBatchSize() {
        if (adaptiveBatchSize < DEFAULT_BATCH_SIZE) {
            adaptiveBatchSize = Math.min(
                    DEFAULT_BATCH_SIZE,
                    adaptiveBatchSize + 5
            );
        } else if (adaptiveBatchSize < MAX_BATCH_SIZE) {
            adaptiveBatchSize = Math.min(MAX_BATCH_SIZE, adaptiveBatchSize + 2);
        }
    }

    private void safeSleepSeconds(int seconds) {
        safeSleepMillis(seconds * 1000L);
    }

    private void safeSleepMillis(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(Math.max(1L, millis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void publishStatus(String message) {
        if (uiStatusUpdater != null) {
            Platform.runLater(() -> uiStatusUpdater.accept(message));
        }
    }

    private int parsePositiveInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static final class SyncOutcome {

        private final boolean success;
        private final boolean rateLimited;
        private final int suggestedBatchSize;
        private final int retryAfterSeconds;

        private SyncOutcome(
                boolean success,
                boolean rateLimited,
                int suggestedBatchSize,
                int retryAfterSeconds
        ) {
            this.success = success;
            this.rateLimited = rateLimited;
            this.suggestedBatchSize = suggestedBatchSize;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        private static SyncOutcome success() {
            return new SyncOutcome(true, false, 0, 0);
        }

        private static SyncOutcome failed() {
            return new SyncOutcome(false, false, 0, 0);
        }

        private static SyncOutcome rateLimited(
                int suggestedBatchSize,
                int retryAfterSeconds
        ) {
            return new SyncOutcome(
                    false,
                    true,
                    suggestedBatchSize,
                    retryAfterSeconds
            );
        }
    }

    static int getTablePriority(String tableName) {
        List<List<Tables>> phases = List.of(
                List.of(
                        Tables.CATEGORY,
                        Tables.FOURNISSEUR,
                        Tables.CLIENT,
                        Tables.COMPTETRESOR,
                        Tables.MATIERE,
                        Tables.DEPOT,
                        Tables.INVENTORY,
                        Tables.IMMOBILISATION
                ),
                List.of(
                        Tables.PRODUIT,
                        Tables.LIVRAISON,
                        Tables.VENTE,
                        Tables.TRAISORERIE,
                        Tables.DEPENSE
                ),
                List.of(
                        Tables.MESURE,
                        Tables.STOCKER,
                        Tables.DESTOCKER,
                        Tables.RECQUISITION,
                        Tables.LIGNEVENTE,
                        Tables.OPERATION,
                        Tables.MATIERESKU,
                        Tables.PRODUCTION,
                        Tables.COMPTER
                ),
                List.of(Tables.PRIXDEVENTE, Tables.REPARTIR),
                List.of(Tables.IMPUTER, Tables.ENTREPOSER),
                List.of(Tables.PRESENCE)
        );

        for (int i = 0; i < phases.size(); i++) {
            for (Tables t : phases.get(i)) {
                if (t.name().equalsIgnoreCase(tableName)) {
                    return i;
                }
            }
        }
        return 99;
    }


}
