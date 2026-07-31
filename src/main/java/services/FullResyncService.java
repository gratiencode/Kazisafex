package services;

import data.*;
import data.network.Kazisafe;
import data.network.dto.BatchMutationDto;
import data.network.dto.BatchResultDto;
import delegates.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import retrofit2.Response;

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
        new Thread(() -> {
            try {
                statusUpdater.accept(
                    "Désynchronisation détectée. Lancement du resync complet..."
                );

                // 1. Complete upsync of local offline mutations
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
                        List<SyncOutbox> chunk = SyncOutboxService.fetchOutboxChunk(batchSize);
                        if (chunk == null || chunk.isEmpty()) {
                            hasMoreOutbox = false;
                            break;
                        }

                        // Hierarchical Sort (Dependency Level, then Timestamp)
                        chunk.sort((r1, r2) -> {
                            int p1 = BackgroundSyncService.getTablePriority(r1.getTableName());
                            int p2 = BackgroundSyncService.getTablePriority(r2.getTableName());
                            if (p1 != p2) {
                                return Integer.compare(p1, p2);
                            }
                            return r1.getUpdatedAt() != null
                                    ? r1.getUpdatedAt().compareTo(r2.getUpdatedAt() != null ? r2.getUpdatedAt() : LocalDateTime.MIN)
                                    : r2.getUpdatedAt() != null ? -1 : 0;
                        });

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
                            break;
                        }

                        logger.info("[FullResync] Sending batch of " + mutations.size() + " mutations.");
                        Response<BatchResultDto> response = api.adaptiveUpsync(mutations).execute();

                        if (response.isSuccessful() && response.body() != null) {
                            BatchResultDto result = response.body();
                            // Use mutable list so the infinite-loop guard works correctly
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
                                // Server accepted entire batch with no explicit per-item breakdown → treat all as success
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

                            // If we didn't make any progress (no successes), break to avoid infinite loop
                            if (successUids.isEmpty()) {
                                logger.warning("[FullResync] No mutations applied in this batch. Stopping upsync to prevent infinite loop.");
                                break;
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
                    statusUpdater.accept("Envoi des données locales terminé.");
                } catch (Exception e) {
                    logger.severe("[FullResync] Error during upsync phase: " + e.getMessage());
                    statusUpdater.accept("Avertissement: Échec lors de la synchronisation des modifications locales.");
                }

                // 2. Download and upsert all entities via merge (update* delegates use em.merge()
                //    which is idempotent: inserts when absent, updates when present, and correctly
                //    handles transient @ManyToOne FK references that would cause PropertyValueException
                //    with em.persist(). No isExists check needed — merge does it in one round-trip.)
                String epoch = "1970-01-01T00:00:00";
                statusUpdater.accept("Téléchargement des données...");

                // ── Master / reference data (no FK deps) ────────────────────────────────

                Response<List<Category>> catResp = api.syncMissedCategories(epoch).execute();
                if (catResp.isSuccessful() && catResp.body() != null) {
                    catResp.body().forEach(CategoryDelegate::updateCategory);
                    statusUpdater.accept("Catégories récupérées.");
                }

                Response<List<Fournisseur>> supResp = api.syncMissedSuppliers(epoch).execute();
                if (supResp.isSuccessful() && supResp.body() != null) {
                    supResp.body().forEach(FournisseurDelegate::updateFournisseur);
                    statusUpdater.accept("Fournisseurs récupérés.");
                }

                Response<List<Client>> cliResp = api.syncMissedClients(epoch).execute();
                if (cliResp.isSuccessful() && cliResp.body() != null) {
                    cliResp.body().forEach(ClientDelegate::updateClient);
                    statusUpdater.accept("Clients récupérés.");
                }

                Response<List<CompteTresor>> cpteResp = api.syncMissedAccounts(epoch).execute();
                if (cpteResp.isSuccessful() && cpteResp.body() != null) {
                    cpteResp.body().forEach(CompteTresorDelegate::updateCompteTresor);
                    statusUpdater.accept("Comptes trésorerie récupérés.");
                }

                Response<List<Matiere>> matResp = api.syncMissedMatieres(epoch).execute();
                if (matResp.isSuccessful() && matResp.body() != null) {
                    matResp.body().forEach(MatiereDelegate::updateMatiere);
                    statusUpdater.accept("Matières récupérées.");
                }

                Response<List<Depot>> depotResp = api.syncMissedDepots(epoch).execute();
                if (depotResp.isSuccessful() && depotResp.body() != null) {
                    depotResp.body().forEach(DepotDelegate::updateDepot);
                    statusUpdater.accept("Dépôts récupérés.");
                }

                // ── Products & measures (depend on Category) ────────────────────────────

                Response<List<Produit>> prodItemResp = api.syncMissedProducts(epoch).execute();
                if (prodItemResp.isSuccessful() && prodItemResp.body() != null) {
                    prodItemResp.body().forEach(ProduitDelegate::updateProduit);
                    statusUpdater.accept("Produits récupérés.");
                }

                Response<List<Mesure>> mesResp = api.syncMissedMesures(epoch).execute();
                if (mesResp.isSuccessful() && mesResp.body() != null) {
                    mesResp.body().forEach(MesureDelegate::updateMesure);
                    statusUpdater.accept("Mesures récupérées.");
                }

                Response<List<MatiereSku>> skuResp = api.syncMissedMatiereSkus(epoch).execute();
                if (skuResp.isSuccessful() && skuResp.body() != null) {
                    skuResp.body().forEach(MatiereSkuDelegate::updateMatiereSku);
                    statusUpdater.accept("Références matières récupérées.");
                }

                // ── Supply chain ────────────────────────────────────────────────────────

                Response<List<Livraison>> livrResp = api.syncMissedDeliveries(epoch).execute();
                if (livrResp.isSuccessful() && livrResp.body() != null) {
                    livrResp.body().forEach(LivraisonDelegate::updateLivraison);
                    statusUpdater.accept("Livraisons récupérées.");
                }

                Response<List<Commande>> cmdResp = api.syncMissedCommandes(epoch).execute();
                if (cmdResp.isSuccessful() && cmdResp.body() != null) {
                    cmdResp.body().forEach(CommandeDelegate::updateCommande);
                    statusUpdater.accept("Commandes récupérées.");
                }

                Response<List<CommandeLister>> cmdLstResp = api.syncMissedCommandListers(epoch).execute();
                if (cmdLstResp.isSuccessful() && cmdLstResp.body() != null) {
                    cmdLstResp.body().forEach(CommandeListerDelegate::updateCommandeLister);
                    statusUpdater.accept("Lignes de commandes récupérées.");
                }

                // ── Industrial / stock ──────────────────────────────────────────────────

                Response<List<Entreposer>> entResp = api.syncMissedEntreposages(epoch).execute();
                if (entResp.isSuccessful() && entResp.body() != null) {
                    entResp.body().forEach(EntreposerDelegate::updateEntreposer);
                    statusUpdater.accept("Entreposages récupérés.");
                }

                Response<List<Production>> prodResp = api.syncMissedProductions(epoch).execute();
                if (prodResp.isSuccessful() && prodResp.body() != null) {
                    prodResp.body().forEach(ProductionDelegate::updateProduction);
                    statusUpdater.accept("Productions récupérées.");
                }

                Response<List<Repartir>> repartirResp = api.syncMissedRepartirs(epoch).execute();
                if (repartirResp.isSuccessful() && repartirResp.body() != null) {
                    repartirResp.body().forEach(RepartirDelegate::updateRepartir);
                    statusUpdater.accept("Répartitions récupérées.");
                }

                Response<List<Imputer>> imputerResp = api.syncMissedImputers(epoch).execute();
                if (imputerResp.isSuccessful() && imputerResp.body() != null) {
                    imputerResp.body().forEach(ImputerDelegate::updateImputer);
                    statusUpdater.accept("Imputations récupérées.");
                }

                Response<List<Stocker>> stockResp = api.syncMissedStocks(epoch).execute();
                if (stockResp.isSuccessful() && stockResp.body() != null) {
                    stockResp.body().forEach(StockerDelegate::updateStocker);
                    statusUpdater.accept("Stocks récupérés.");
                }

                Response<List<Destocker>> destResp = api.syncMissedDestokers(epoch).execute();
                if (destResp.isSuccessful() && destResp.body() != null) {
                    destResp.body().forEach(DestockerDelegate::updateDestocker);
                    statusUpdater.accept("Destockages récupérés.");
                }

                Response<List<Recquisition>> recqResp = api.syncMissedRecquisitions(epoch).execute();
                if (recqResp.isSuccessful() && recqResp.body() != null) {
                    recqResp.body().forEach(RecquisitionDelegate::updateRecquisition);
                    statusUpdater.accept("Réquisitions récupérées.");
                }

                Response<List<PrixDeVente>> priceResp = api.syncMissedPrices(epoch).execute();
                if (priceResp.isSuccessful() && priceResp.body() != null) {
                    priceResp.body().forEach(PrixDeVenteDelegate::updatePrixDeVente);
                    statusUpdater.accept("Tarifs récupérés.");
                }

                // ── Inventory ───────────────────────────────────────────────────────────

                Response<List<Inventaire>> invResp = api.syncMissedInventaires(epoch).execute();
                if (invResp.isSuccessful() && invResp.body() != null) {
                    invResp.body().forEach(InventaireDelegate::updateInventaire);
                    statusUpdater.accept("Inventaires récupérés.");
                }

                // Compter depends on Inventaire — must come after Inventaire merge
                Response<List<Compter>> compterResp = api.syncMissedCounts(epoch).execute();
                if (compterResp.isSuccessful() && compterResp.body() != null) {
                    compterResp.body().forEach(CompterDelegate::updateCompter);
                    statusUpdater.accept("Comptages récupérés.");
                }

                // ── Sales ───────────────────────────────────────────────────────────────

                Response<List<Vente>> venteResp = api.syncMissedSales(epoch).execute();
                if (venteResp.isSuccessful() && venteResp.body() != null) {
                    venteResp.body().forEach(VenteDelegate::updateVente);
                    statusUpdater.accept("Ventes récupérées.");
                }

                Response<List<LigneVente>> lineResp = api.syncMissedSaleItems(epoch).execute();
                if (lineResp.isSuccessful() && lineResp.body() != null) {
                    lineResp.body().forEach(LigneVenteDelegate::updateLigneVente);
                    statusUpdater.accept("Lignes de vente récupérées.");
                }

                // ── Finance ─────────────────────────────────────────────────────────────

                Response<List<Traisorerie>> trasResp = api.syncMissedTransactions(epoch).execute();
                if (trasResp.isSuccessful() && trasResp.body() != null) {
                    trasResp.body().forEach(TraisorerieDelegate::updateTraisorerie);
                    statusUpdater.accept("Transactions de trésorerie récupérées.");
                }

                Response<List<Depense>> depResp = api.syncMissedDepenses(epoch).execute();
                if (depResp.isSuccessful() && depResp.body() != null) {
                    depResp.body().forEach(DepenseDelegate::updateDepense);
                    statusUpdater.accept("Dépenses récupérées.");
                }

                Response<List<Operation>> opResp = api.syncMissedOperations(epoch).execute();
                if (opResp.isSuccessful() && opResp.body() != null) {
                    opResp.body().forEach(OperationDelegate::updateOperation);
                    statusUpdater.accept("Opérations récupérées.");
                }
                statusUpdater.accept(
                    "Resynchronisation complète terminée avec succès."
                );
            } catch (IOException e) {
                e.printStackTrace();
                logger.severe("Erreur lors du full resync: " + e.getMessage());
                statusUpdater.accept(
                    "Erreur lors de la resynchronisation complète."
                );
            }
        }).start();
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
