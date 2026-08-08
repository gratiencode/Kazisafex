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
import tools.MemoryGuard;
import tools.SyncEngine;
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

    /**
     * Moniteur de réveil : permet de demander un nouveau cycle sans annuler
     * celui en cours. Un cycle de sync (upsync + downsync) doit se terminer
     * complètement avant que le suivant démarre.
     */
    private final Object wakeMonitor = new Object();
    private boolean cycleRequested = false;

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

    /**
     * Demande qu'un nouveau cycle de synchronisation démarre dès que le cycle
     * en cours (upsync + downsync) est complètement terminé. Ne cancelle rien.
     */
    public void requestCycle() {
        synchronized (wakeMonitor) {
            cycleRequested = true;
            wakeMonitor.notifyAll();
        }
    }

    /**
     * Consomme et retourne l'éventuelle demande de cycle immédiat.
     */
    private boolean consumeCycleRequest() {
        synchronized (wakeMonitor) {
            boolean requested = cycleRequested;
            cycleRequested = false;
            return requested;
        }
    }

    /**
     * Sommeil interruptible par {@link #requestCycle()} : un cycle demandé
     * pendant l'attente démarre immédiatement au lieu d'attendre le polling.
     */
    private void sleepWithWake(long millis) {
        synchronized (wakeMonitor) {
            try {
                wakeMonitor.wait(Math.max(1L, millis));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final java.util.concurrent.locks.ReentrantLock SYNC_CYCLE_LOCK = new java.util.concurrent.locks.ReentrantLock();

    public static boolean tryAcquireSyncLock() {
        return SYNC_CYCLE_LOCK.tryLock();
    }

    public static void acquireSyncLock() {
        SYNC_CYCLE_LOCK.lock();
    }

    public static void releaseSyncLock() {
        if (SYNC_CYCLE_LOCK.isHeldByCurrentThread()) {
            SYNC_CYCLE_LOCK.unlock();
        }
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

                    // Vérification mémoire avant tout traitement lourd
                    if (!MemoryGuard.hasEnoughMemory()) {
                        MemoryGuard.logMemoryState();
                        publishStatus("Mémoire insuffisante – cycle de sync suspendu 10s...");
                        safeSleepSeconds(10);
                        continue;
                    }

                    if (!tryAcquireSyncLock()) {
                        requestCycle();
                        safeSleepSeconds(1);
                        continue;
                    }

                    boolean cycleCompleted = false;
                    try {
                        // 1. Validation d'état & Full Resync synchrone
                        try {
                            java.util.prefs.Preferences pref
                                    = java.util.prefs.Preferences.userNodeForPackage(
                                            tools.SyncEngine.class
                                    );
                            String enterpriseId = pref.get("eUid", pref.get("enterpriseId", pref.get("entrepriseId", null)));
                            String regionId = pref.get("region", "Goma");
                            if (enterpriseId != null) {
                                services.StateValidationService.ValidationResult res
                                        = services.StateValidationService.validateState(
                                                kazisafe,
                                                enterpriseId,
                                                regionId
                                        );
                                if (!res.isSynchronized) {
                                    publishStatus("Désynchronisation détectée. Réparation synchrone en cours...");
                                    services.FullResyncService.performFullResyncSync(
                                            kazisafe,
                                            enterpriseId,
                                            regionId,
                                            BackgroundSyncService.this::publishStatus
                                    );
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
                        } else {
                            // Timestamp de fin de la dernière synchro : l'upsync
                            // ne remonte que les outbox créées après ce timestamp
                            // (ancienneté), en conservant les retries en cours.
                            long cycleSince = SyncEngine.getLastSyncTimestamp();

                            // 2. Nettoyage et correction outbox
                            SyncOutboxService.correctAndCleanOutboxData();

                            // 3. Upsync complet jusqu'au dernier niveau de priorité (Level 0 à Level 5)
                            boolean hasMoreOutbox = true;
                            while (hasMoreOutbox && !isCancelled()) {
                                List<SyncOutbox> pendingList = SyncOutboxService.fetchPendingOutboxSince(cycleSince);
                                if (pendingList == null || pendingList.isEmpty()) {
                                    hasMoreOutbox = false;
                                    break;
                                }

                                // Tri hiérarchique strict : Niveau de priorité des entités (0 à 5), puis Date
                                pendingList.sort((r1, r2) -> {
                                    int p1 = getTablePriority(r1.getTableName());
                                    int p2 = getTablePriority(r2.getTableName());
                                    if (p1 != p2) {
                                        return Integer.compare(p1, p2);
                                    }
                                    return r1.getCreatedAt() != null
                                            ? r1.getCreatedAt().compareTo(r2.getCreatedAt() != null ? r2.getCreatedAt() : LocalDateTime.MIN)
                                            : (r2.getCreatedAt() != null ? -1 : 0);
                                });

                                int total = pendingList.size();
                                int processedCount = 0;
                                for (int i = 0; i < total && !isCancelled(); i += adaptiveBatchSize) {
                                    int end = Math.min(i + adaptiveBatchSize, total);
                                    List<SyncOutbox> chunk = pendingList.subList(i, end);

                                    SyncOutcome outcome = processUpsyncChunk(chunk);
                                    if (outcome.rateLimited) {
                                        handleRateLimit(outcome);
                                        long pauseRemaining = adaptivePauseUntilEpochMs - System.currentTimeMillis();
                                        if (pauseRemaining > 0) {
                                            safeSleepMillis(pauseRemaining);
                                        }
                                    } else if (!outcome.success) {
                                        hasMoreOutbox = false;
                                        break;
                                    } else {
                                        maybeIncreaseBatchSize();
                                        processedCount += chunk.size();
                                    }
                                }

                                if (processedCount == 0) {
                                    break;
                                }
                            }

                            // 4. Downsync : pull incrémental depuis le dernier
                            //    timestamp de mutation reçu (ancienneté), puis
                            //    matérialisation complète de toutes les entités
                            //    downsync jusqu'au dernier niveau.
                            services.sync.DownsyncCatchupService.catchUp(kazisafe);
                            SyncOutboxService.materializeDownsyncRecords();

                            // 5. Nettoyage des enregistrements appliqués
                            SyncOutboxService.cleanupAppliedRecords();

                            cycleCompleted = true;
                        }
                    } catch (Exception e) {
                        SyncLogger.getInstance().log(
                                e,
                                "BackgroundSyncService: Error during sync cycle",
                                null,
                                null
                        );
                    } finally {
                        releaseSyncLock();
                    }

                    // Timestamp de fin de synchronisation : capturé uniquement
                    // si le cycle s'est terminé sans exception, pour ne jamais
                    // « vieillir » des mutations restées non synchronisées.
                    if (cycleCompleted) {
                        SyncEngine.setLastSyncTimestamp(
                                System.currentTimeMillis()
                        );
                    }

                    // Cycle terminé. Si un nouveau cycle a été demandé pendant celui-ci,
                    // il démarre immédiatement en séquence ; sinon on attend le prochain polling.
                    if (!consumeCycleRequest()) {
                        sleepWithWake(pollIntervalSeconds * 1000L);
                    } else {
                        publishStatus("Synchronisation demandée - nouveau cycle...");
                    }
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
            // Marquer le lot comme UNSYNCED (retryCount++) pour que le retry
            // reste actif même si le timestamp de fin de synchro avance.
            List<String> allUids = chunk.stream()
                    .map(SyncOutbox::getUid)
                    .collect(Collectors.toList());
            try {
                SyncOutboxService.markAsUnsynced(allUids);
            } catch (Exception ignored) {
            }
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

    /**
     * Niveau de priorité d'une entité = ordre de dépendance. Les parents (FK)
     * sont toujours dans un niveau inférieur à leurs dépendants, afin que la
     * matérialisation locale (downsync) insère les parents avant les enfants.
     * Niveau 0 = racines (aucune FK obligatoire).
     */
    static int getTablePriority(String tableName) {
        List<List<Tables>> phases = List.of(
                // Niveau 0 — racines : aucune dépendance FK obligatoire
                List.of(
                        Tables.CATEGORY,
                        Tables.FOURNISSEUR,
                        Tables.CLIENT,
                        Tables.COMPTETRESOR,
                        Tables.MATIERE,
                        Tables.DEPOT,
                        Tables.INVENTORY,
                        Tables.IMMOBILISATION,
                        Tables.TAXE,
                        Tables.CLIENTORGANISATION,
                        Tables.DEPENSE,
                        Tables.PRESENCE
                ),
                // Niveau 1 — dépendent uniquement du niveau 0
                List.of(
                        Tables.PRODUIT,
                        Tables.LIVRAISON,
                        Tables.VENTE,
                        Tables.TRAISORERIE,
                        Tables.MATIERESKU,
                        Tables.COMMANDE
                ),
                // Niveau 2 — dépendent des niveaux 0-1 (ex : Mesure -> Produit)
                List.of(
                        Tables.MESURE,
                        Tables.STOCKER,
                        Tables.DESTOCKER,
                        Tables.RECQUISITION,
                        Tables.PRODUCTION,
                        Tables.COMPTER,
                        Tables.LIGNEVENTE,
                        Tables.OPERATION,
                        Tables.COMMANDELIST,
                        Tables.PERIODE,
                        Tables.TAXER,
                        Tables.SATISFAIRE,
                        Tables.CLIENTAPPARTENIR
                ),
                // Niveau 3 — dépendent des niveaux 0-2
                List.of(
                        Tables.PRIXDEVENTE,
                        Tables.REPARTIR,
                        Tables.ARETIRER,
                        Tables.RETOURDEPOT,
                        Tables.RETOURMAGASIN
                ),
                // Niveau 4 — dépendent des niveaux 0-3
                List.of(
                        Tables.IMPUTER,
                        Tables.ENTREPOSER
                ),
                // Niveau 5 — dernier niveau
                List.of(
                        Tables.FACTURE,
                        Tables.ABONNEMENT,
                        Tables.BULKMODEL,
                        Tables.REFRESH,
                        Tables.PERMISSION,
                        Tables.REPPORTING,
                        Tables.IMMOBILISATION_AGREGATE,
                        Tables.FINGERPRINTMAPPING
                )
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
