package services;

import data.*;
import data.network.Kazisafe;
import data.network.dto.BatchMutationDto;
import data.network.dto.BatchResultDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import retrofit2.Response;
import services.sync.DownsyncCatchupService;

public class FullResyncService {

    private static final Logger logger = Logger.getLogger(
        FullResyncService.class.getName()
    );

    public static void performFullResync(
        Kazisafe api,
        String enterpriseId,
        String regionId,
        Consumer<String> statusUpdater
    ) {
        new Thread(() -> performFullResyncSync(api, enterpriseId, regionId, statusUpdater), "Kazisafe-FullResync").start();
    }

    public static void performFullResyncSync(
        Kazisafe api,
        String enterpriseId,
        String regionId,
        Consumer<String> statusUpdater
    ) {
        BackgroundSyncService.acquireSyncLock();
        try {
            statusUpdater.accept(
                "Désynchronisation détectée. Lancement du resync complet..."
            );

            // 1. Complete upsync of local offline mutations ordered strictly by entity priority levels
            statusUpdater.accept(
                "Envoi des données locales non synchronisées..."
            );
            
            try {
                // Backfill: ensure all entities from tables are present in SyncOutbox 
                SyncOutboxService.backfillMissingEntities();
                
                // Correct and clean outbox data before syncing
                SyncOutboxService.correctAndCleanOutboxData();
                
                boolean hasMoreOutbox = true;
                int batchSize = 50;
                while (hasMoreOutbox) {
                    List<SyncOutbox> pendingList = SyncOutboxService.fetchAllPendingOutbox();
                    if (pendingList == null || pendingList.isEmpty()) {
                        hasMoreOutbox = false;
                        break;
                    }

                    // Tri hiérarchique strict : Niveau de priorité des entités (0 à 5), puis Date
                    pendingList.sort((r1, r2) -> {
                        int p1 = BackgroundSyncService.getTablePriority(r1.getTableName());
                        int p2 = BackgroundSyncService.getTablePriority(r2.getTableName());
                        if (p1 != p2) {
                            return Integer.compare(p1, p2);
                        }
                        LocalDateTime t1 = r1.getCreatedAt() != null ? r1.getCreatedAt() : r1.getUpdatedAt();
                        LocalDateTime t2 = r2.getCreatedAt() != null ? r2.getCreatedAt() : r2.getUpdatedAt();
                        if (t1 != null && t2 != null) {
                            return t1.compareTo(t2);
                        }
                        return t1 != null ? 1 : (t2 != null ? -1 : 0);
                    });

                    int total = pendingList.size();
                    int processedInLoop = 0;
                    for (int i = 0; i < total; i += batchSize) {
                        int end = Math.min(i + batchSize, total);
                        List<SyncOutbox> chunk = pendingList.subList(i, end);

                        // Convert to BatchMutationDto
                        List<BatchMutationDto> mutations = chunk.stream().map(r -> {
                            BatchMutationDto dto = new BatchMutationDto();
                            dto.entityId = r.getEntityId();
                            dto.entityType = r.getTableName();
                            dto.payload = r.getPayload();
                            dto.updatedAt = r.getUpdatedAt();
                            dto.mutationType = r.getAction().replace("PERSIST", "INSERT");
                            dto.entrepriseId = r.getEntrepriseId();
                            dto.region = r.getRegion();
                            return dto;
                        }).collect(Collectors.toList());

                        if (mutations.isEmpty()) {
                            continue;
                        }

                        logger.info("[FullResync] Sending batch of " + mutations.size() + " mutations.");
                        Response<BatchResultDto> response = api.adaptiveUpsync(mutations).execute();

                        if (response.isSuccessful() && response.body() != null) {
                            BatchResultDto result = response.body();
                            List<String> successUids = new java.util.ArrayList<>();

                            // Handle Successes
                            if (result.successes != null && !result.successes.isEmpty()) {
                                successUids = chunk.stream()
                                        .filter(r -> result.successes.stream().anyMatch(s -> s.entityId.equals(r.getEntityId())
                                                && s.entityType.equals(r.getTableName())))
                                        .map(SyncOutbox::getUid)
                                        .collect(Collectors.toList());

                                if (!successUids.isEmpty()) {
                                    logger.info("[FullResync] Successfully synced " + successUids.size() + "/" + mutations.size() + " records.");
                                    SyncOutboxService.markAsApplied(successUids);
                                }
                            } else if (result.failures == null || result.failures.isEmpty()) {
                                successUids = chunk.stream().map(SyncOutbox::getUid).collect(Collectors.toList());
                                logger.info("[FullResync] Batch fully accepted (no item breakdown). Marking " + successUids.size() + " outbox records as APPLIED.");
                                SyncOutboxService.markAsApplied(successUids);
                            }

                            // Handle Failures: mark as UNSYNCED for retry
                            if (result.failures != null && !result.failures.isEmpty()) {
                                List<String> failedUids = chunk.stream()
                                        .filter(r -> result.failures.stream().anyMatch(s -> s.entityId.equals(r.getEntityId())
                                                && s.entityType.equals(r.getTableName())))
                                        .map(SyncOutbox::getUid)
                                        .collect(Collectors.toList());

                                if (!failedUids.isEmpty()) {
                                    logger.warning("[FullResync] " + failedUids.size() + " mutations failed, marking as UNSYNCED for retry.");
                                    SyncOutboxService.markAsUnsynced(failedUids);
                                }
                            }

                            if (successUids.isEmpty()) {
                                logger.warning("[FullResync] No mutations applied in this batch. Stopping upsync pass.");
                                break;
                            } else {
                                processedInLoop += successUids.size();
                            }
                        } else if (response.code() == 429) {
                            String retryAfterHeader = response.headers().get("Retry-After");
                            int retryAfterSeconds = 5;
                            if (retryAfterHeader != null && !retryAfterHeader.isBlank()) {
                                try {
                                    retryAfterSeconds = Integer.parseInt(retryAfterHeader.trim());
                                } catch (NumberFormatException ignored) {}
                            }
                            statusUpdater.accept("Serveur surchargé (HTTP 429). Pause de " + retryAfterSeconds + "s...");
                            try {
                                java.util.concurrent.TimeUnit.SECONDS.sleep(retryAfterSeconds);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        } else {
                            logger.severe("[FullResync] Server error during upsync: " + response.code());
                            statusUpdater.accept("Erreur de synchronisation locale (HTTP " + response.code() + ").");
                            break;
                        }
                    }

                    if (processedInLoop == 0) {
                        break;
                    }
                }
                statusUpdater.accept("Envoi des données locales terminé.");
            } catch (Exception e) {
                logger.severe("[FullResync] Error during upsync phase: " + e.getMessage());
                statusUpdater.accept("Avertissement: Échec lors de la synchronisation des modifications locales.");
            }

            // 2. Downsync : téléchargement de toutes les mutations manquées via
            //    l'outbox serveur (getMissedMutations) puis matérialisation en base
            //    locale (MySQL ou SQLite selon la base active). Le tri hiérarchique
            //    par niveau de priorité (BackgroundSyncService.getTablePriority)
            //    garantit que les parents (Inventaire, Vente, ...) sont matérialisés
            //    avant leurs dépendants (Compter, LigneVente, ...).
            DownsyncCatchupService.catchUpFull(api, statusUpdater);

            statusUpdater.accept(
                "Resynchronisation complète terminée avec succès."
            );
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Erreur lors du full resync: " + e.getMessage());
            statusUpdater.accept(
                "Erreur lors de la resynchronisation complète."
            );
        } finally {
            BackgroundSyncService.releaseSyncLock();
        }
    }

    private static void clearRegionalData(String regionId) {
        ManagedSessionFactory.executeWrite(em -> {
            String[] tables = {
                "Production",
                "Commande",
                "Entreposer",
                "Stocker",
                "Destocker",
                "Recquisition",
                "LigneVente",
                "Operation",
                "Traisorerie",
                "Depense",
            };
            for (String table : tables) {
                em.createQuery(
                    "DELETE FROM " + table + " e WHERE e.region = :region"
                )
                    .setParameter("region", regionId)
                    .executeUpdate();
            }
            return null;
        });
    }
}
