/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import com.endeleya.kazisafex.MainuiController;
import java.util.List;
import java.util.Set;
import data.network.Kazisafe;
import data.*;
import delegates.*;
import jakarta.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.concurrent.Task;
import retrofit2.Call;
import retrofit2.Response;

/**
 *
 * @author endeleya
 */
public class HttpSyncHandler
        extends Task<Boolean> {

    Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    Kazisafe kazisafe;

    public HttpSyncHandler(Kazisafe kazisafe) {
        this.kazisafe = kazisafe;
    }

    boolean up = false, down = false;

    @Override
    protected Boolean call() throws Exception {
        try {

            long since = pref.getLong(Constants.SYNC_ELLAPSED_TIME, 0);
            System.out.println("Sync du depuis le "
                    + LocalDateTime.ofInstant(Instant.ofEpochMilli(since), ZoneId.systemDefault()));
            updateMessage("\uD83D\uDC46 Synchronisation cloud en cours...");
            sendDataToCloud(since, () -> {
                updateMessage("\uD83D\uDC47 Mise ajour de la base locale en cours...");
                up = true;
                getDataFromCloud(since, () -> {
                    long now = System.currentTimeMillis();
                    pref.putLong(Constants.SYNC_ELLAPSED_TIME, now);
                    updateMessage("\u2705 Synchronisation terminée avec succès");
                    down = true;
                    System.out.println("Sync fini le "
                            + LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault()));
                });
            });
            return true;
        } catch (Exception e) {
            updateMessage(
                    (!up ? "Synchronisation distante" : !down ? "Synchronisation locale" : "") + "  non terminée");
            System.err.println("Erreur de synchronisation : " + e.getMessage());
            throw e;
        }
    }

    private void getDataFromCloud(long lastSession, ExceptionalRunnable onComplete) throws Exception {
        String[] endpoints = {
            "syncMissedCategories",
            "syncMissedProducts",
            "syncMissedMesures",
            "syncMissedSuppliers",
            "syncMissedDeliveries",
            "syncMissedStocks",
            "syncMissedDestokers",
            "syncMissedRecquisitions",
            "syncMissedPrices",
            "syncMissedClients",
            "syncMissedSales",
            "syncMissedSaleItems",
            "syncMissedAccounts",
            "syncMissedTransactions",
            "syncMissedInventaires",
            "syncMissedCounts",
            "syncMissedDepenses",
            "syncMissedOperations",
            "syncMissedMatieres",
            "syncMissedMatiereSkus",
            "syncMissedDepots",
            "syncMissedProductions",
            "syncMissedRepartirs",
            "syncMissedImputers",
            "syncMissedEntreposages",
            "syncMissedImmobilisations",
            "syncMissedPresences"
        };
        System.out.println("lastSession " + lastSession + " prepare synchronisation");
        try {
            for (String endpoint : endpoints) {
                System.out.println("Donwload request for - " + endpoint);
                Call<?> call = downsyncCall(endpoint, lastSession);
                if (call == null) {
                    continue;
                }
                Response<?> execute = call.execute();
                System.out.println("Message " + endpoint + " " + execute.code());
                if (execute.code() == 401) {
                    throw new IllegalStateException("Session expirée (401) pendant la synchronisation distante.");
                }
                if (execute.isSuccessful()) {
                    handleSyncResponse(endpoint, execute);
                }
            }
            onComplete.run();
        } catch (Exception e) {
            System.out.println("Sync-down error : " + e.getMessage());
            throw e;
        }

    }

    private Call<?> downsyncCall(String endpoint, long lastSession) {
        String ts = String.valueOf(lastSession);
        return switch (endpoint) {
            case "syncMissedCategories" ->
                kazisafe.syncMissedCategories(ts);
            case "syncMissedProducts" ->
                kazisafe.syncMissedProducts(ts);
            case "syncMissedMesures" ->
                kazisafe.syncMissedMesures(ts);
            case "syncMissedSuppliers" ->
                kazisafe.syncMissedSuppliers(ts);
            case "syncMissedDeliveries" ->
                kazisafe.syncMissedDeliveries(ts);
            case "syncMissedStocks" ->
                kazisafe.syncMissedStocks(ts);
            case "syncMissedDestokers" ->
                kazisafe.syncMissedDestokers(ts);
            case "syncMissedRecquisitions" ->
                kazisafe.syncMissedRecquisitions(ts);
            case "syncMissedPrices" ->
                kazisafe.syncMissedPrices(ts);
            case "syncMissedClients" ->
                kazisafe.syncMissedClients(ts);
            case "syncMissedSales" ->
                kazisafe.syncMissedSales(ts);
            case "syncMissedSaleItems" ->
                kazisafe.syncMissedSaleItems(ts);
            case "syncMissedAccounts" ->
                kazisafe.syncMissedAccounts(ts);
            case "syncMissedTransactions" ->
                kazisafe.syncMissedTransactions(ts);
            case "syncMissedInventaires" ->
                kazisafe.syncMissedInventaires(ts);
            case "syncMissedCounts" ->
                kazisafe.syncMissedCounts(ts);
            case "syncMissedDepenses" ->
                kazisafe.syncMissedDepenses(ts);
            case "syncMissedOperations" ->
                kazisafe.syncMissedOperations(ts);
            case "syncMissedMatieres" ->
                kazisafe.syncMissedMatieres(ts);
            case "syncMissedMatiereSkus" ->
                kazisafe.syncMissedMatiereSkus(ts);
            case "syncMissedDepots" ->
                kazisafe.syncMissedDepots(ts);
            case "syncMissedProductions" ->
                kazisafe.syncMissedProductions(ts);
            case "syncMissedRepartirs" ->
                kazisafe.syncMissedRepartirs(ts);
            case "syncMissedImputers" ->
                kazisafe.syncMissedImputers(ts);
            case "syncMissedEntreposages" ->
                kazisafe.syncMissedEntreposages(ts);
            case "syncMissedImmobilisations" ->
                kazisafe.syncMissedImmobilisations(ts);
            case "syncMissedPresences" ->
                kazisafe.syncMissedPresences(ts);
            default ->
                null;
        };
    }

    private void handleSyncResponse(String method, Response<?> response) throws Exception {
        if ("syncMissedCategories".equals(method)) {
            List<Category> categories = (List<Category>) response.body();

            if (categories != null) {
                System.out.println("Incoming categ " + categories.size());
                if (!categories.isEmpty()) {
                    for (Category c : categories) {
                        boolean isSynced = CategoryDelegate.isExists(c.getUid());
                        if (!isSynced) {
                            CategoryDelegate.saveCategory(c);
                        } else {
                            Category cat = CategoryDelegate.findCategory(c.getUid());
                            cat.setDescritption(c.getDescritption());
                            cat.setUpdatedAt(c.getUpdatedAt());
                            cat.setDeletedAt(c.getDeletedAt());
                            CategoryDelegate.updateCategory(cat);
                        }
                    }

                }

            }
        } else if ("syncMissedProducts".equals(method)) {
            List<Produit> products = (List<Produit>) response.body();
            if (products != null) {
                for (Produit product : products) {
                    boolean exist = CategoryDelegate.isExists(product.getCategoryId().getUid());
                    if (exist) {
                        boolean isSynced = ProduitDelegate.isExists(product.getUid());
                        if (!isSynced) {
                            ProduitDelegate.saveProduit(product);
                        } else {
                            ProduitDelegate.updateProduit(product);
                        }
                    }
                }

            }
        } else if ("syncMissedMesures".equals(method)) {
            List<Mesure> measures = (List<Mesure>) response.body();
            if (measures != null) {
                for (Mesure measure : measures) {
                    boolean exists = ProduitDelegate.isExists(measure.getProduitId().getUid());
                    if (exists) {
                        boolean isSynced = MesureDelegate.isExists(measure.getUid());
                        if (!isSynced) {
                            MesureDelegate.saveMesure(measure);
                        } else {
                            MesureDelegate.updateMesure(measure);
                        }
                    }
                }

            }
        } else if ("syncMissedRecquisitions".equals(method)) {
            List<Recquisition> recquisitions = (List<Recquisition>) response.body();
            if (recquisitions != null) {
                for (Recquisition recquisition : recquisitions) {
                    boolean exists = ProduitDelegate.isExists(recquisition.getProductId().getUid());
                    boolean exist1 = MesureDelegate.isExists(recquisition.getMesureId().getUid());
                    if (exists && exist1) {
                        boolean isSynced = RecquisitionDelegate.isExists(recquisition.getUid());
                        if (!isSynced) {
                            RecquisitionDelegate.saveRecquisition(recquisition);
                        } else {
                            RecquisitionDelegate.updateRecquisition(recquisition);
                        }
                        Mesure m = MesureDelegate.findMesure(recquisition.getMesureId().getUid());
                        Produit p = ProduitDelegate.findProduit(recquisition.getProductId().getUid());
                        double cau = recquisition.getCoutAchat() / m.getQuantContenu();
                        RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), recquisition.getRegion(), cau);
                    }

                }

            }
        } else if ("syncMissedPrices".equals(method)) {
            List<PrixDeVente> prices = (List<PrixDeVente>) response.body();
            if (prices != null) {
                for (PrixDeVente price : prices) {
                    boolean exists = RecquisitionDelegate.isExists(price.getRecquisitionId().getUid());
                    boolean exist1 = MesureDelegate.isExists(price.getMesureId().getUid());
                    if (exists && exist1) {
                        boolean isSynced = PrixDeVenteDelegate.isExists(price.getUid());
                        if (!isSynced) {
                            price.setRecquisitionId(
                                    RecquisitionDelegate.findRecquisition(price.getRecquisitionId().getUid()));
                            PrixDeVenteDelegate.savePrixDeVente(price);
                        } else {
                            PrixDeVenteDelegate.updatePrixDeVente(price);
                        }
                    }
                }
            }
        } else if ("syncMissedClients".equals(method)) {
            List<Client> clients = (List<Client>) response.body();
            if (clients != null) {
                for (Client client : clients) {
                    boolean isSynced = ClientDelegate.isExists(client.getUid());
                    if (!isSynced) {
                        ClientDelegate.saveClient(client);
                    } else {
                        ClientDelegate.updateClient(client);
                    }
                }

            }
        } else if ("syncMissedSales".equals(method)) {
            List<Vente> ventes = (List<Vente>) response.body();
            if (ventes != null) {
                for (Vente vente : ventes) {
                    Client clt = vente.getClientId();
                    if (clt == null) {
                        continue;
                    }
                    boolean exists = ClientDelegate.isExists(clt.getUid());
                    if (exists) {
                        boolean isSynced = VenteDelegate.isExists(vente.getUid());
                        if (!isSynced) {
                            VenteDelegate.saveVente(vente);
                        } else {
                            VenteDelegate.updateVente(vente);
                        }
                    }

                }

            }
        } else if ("syncMissedSaleItems".equals(method)) {
            List<LigneVente> saleitems = (List<LigneVente>) response.body();
            if (saleitems != null) {
                for (LigneVente saleitem : saleitems) {
                    boolean exists = ProduitDelegate.isExists(saleitem.getProductId().getUid());
                    boolean exist1 = MesureDelegate.isExists(saleitem.getMesureId().getUid());
                    boolean exist2 = VenteDelegate.isExists(saleitem.getReference().getUid());
                    if (exists && exist1 && exist2) {
                        boolean isSynced = LigneVenteDelegate.isExists(saleitem.getUid());
                        if (!isSynced) {
                            LigneVenteDelegate.saveLigneVente(saleitem);
                        } else {
                            LigneVenteDelegate.updateLigneVente(saleitem);
                        }
                        Mesure m = MesureDelegate.findMesure(saleitem.getMesureId().getUid());
                        Produit p = ProduitDelegate.findProduit(saleitem.getProductId().getUid());
                        Vente vr = VenteDelegate.findVente(saleitem.getReference().getUid());
                        double cau = saleitem.getCoutAchat() / m.getQuantContenu();
                        RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), vr.getRegion(), cau);
                    }
                }
            }
        } else if ("syncMissedAccounts".equals(method)) {
            List<CompteTresor> accounts = (List<CompteTresor>) response.body();
            if (accounts != null) {
                for (CompteTresor account : accounts) {
                    boolean isSynced = CompteTresorDelegate.isExists(account.getUid());
                    if (!isSynced) {
                        CompteTresorDelegate.saveCompteTresor(account);
                    } else {
                        CompteTresorDelegate.updateCompteTresor(account);
                    }
                }

            }
        } else if ("syncMissedTransactions".equals(method)) {
            List<Traisorerie> transactions = (List<Traisorerie>) response.body();
            if (transactions != null) {
                for (Traisorerie trans : transactions) {
                    boolean exists = CompteTresorDelegate.isExists(trans.getTresorId().getUid());
                    if (exists) {
                        boolean isSynced = TraisorerieDelegate.isExists(trans.getUid());
                        if (!isSynced) {
                            TraisorerieDelegate.saveTraisorerie(trans);
                        } else {
                            TraisorerieDelegate.updateTraisorerie(trans);
                        }
                    }
                }

            }
        } else if ("syncMissedInventaires".equals(method)) {
            List<Inventaire> inventories = (List<Inventaire>) response.body();
            if (inventories != null) {
                for (Inventaire inventory : inventories) {
                    boolean isSynced = InventaireDelegate.isExists(inventory.getUid());
                    if (!isSynced) {
                        InventaireDelegate.createInventaire(inventory);
                    } else {
                        InventaireDelegate.updateInventaire(inventory);
                    }
                }
            }
        } else if ("syncMissedCounts".equals(method)) {
            List<Compter> compters = (List<Compter>) response.body();
            if (compters != null) {
                for (Compter compter : compters) {
                    boolean exists = InventaireDelegate.isExists(compter.getInventaireId().getUid());
                    boolean exist1 = MesureDelegate.isExists(compter.getMesureId().getUid());
                    boolean exist2 = ProduitDelegate.isExists(compter.getProductId().getUid());
                    if (exists && exist1 && exist2) {
                        boolean isSynced = CompterDelegate.isExists(compter.getUid());
                        if (!isSynced) {
                            CompterDelegate.createCompter(compter);
                        } else {
                            CompterDelegate.updateCompter(compter);
                        }
//                        Mesure m = MesureDelegate.findMesure(compter.getMesureId().getUid());
//                        Produit p = ProduitDelegate.findProduit(compter.getProductId().getUid());
//                        double cau = compter.getCoutAchat() / m.getQuantContenu();
//                        RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), compter.getRegion(), cau);
                    }
                }
            }
        } else if ("syncMissedSuppliers".equals(method)) {
            List<Fournisseur> suppliers = (List<Fournisseur>) response.body();
            if (suppliers != null) {
                for (Fournisseur supplier : suppliers) {

                    boolean isSynced = FournisseurDelegate.isExists(supplier.getUid());
                    if (!isSynced) {
                        FournisseurDelegate.saveFournisseur(supplier);
                    } else {
                        FournisseurDelegate.updateFournisseur(supplier);
                    }

                }

            }
        } else if ("syncMissedDeliveries".equals(method)) {
            List<Livraison> deliveries = (List<Livraison>) response.body();
            if (deliveries != null) {
                for (Livraison delivery : deliveries) {
                    boolean exists = FournisseurDelegate.isExists(delivery.getFournId().getUid());
                    if (exists) {
                        boolean isSynced = LivraisonDelegate.isExists(delivery.getUid());
                        if (!isSynced) {
                            LivraisonDelegate.saveLivraison(delivery);
                        } else {
                            LivraisonDelegate.updateLivraison(delivery);
                        }
                    }

                }

            }
        } else if ("syncMissedStocks".equals(method)) {
            List<Stocker> stockers = (List<Stocker>) response.body();
            if (stockers != null) {
                for (Stocker stocker : stockers) {
                    boolean exists = LivraisonDelegate.isExists(stocker.getLivraisId().getUid());
                    boolean exist1 = MesureDelegate.isExists(stocker.getMesureId().getUid());
                    boolean exist2 = ProduitDelegate.isExists(stocker.getProductId().getUid());
                    if (exists && exist1 && exist2) {
                        boolean isSynced = StockerDelegate.isExists(stocker.getUid());
                        if (!isSynced) {
                            StockerDelegate.saveStocker(stocker);
                        } else {
                            StockerDelegate.updateStocker(stocker);
                        }
                        Produit p = ProduitDelegate.findProduit(stocker.getProductId().getUid());
                        StockerDelegate.rectifyStockDepot(p, stocker.getDateStocker().toLocalDate(), stocker.getRegion(), stocker.getCoutAchat());

                    }

                }

            }
        } else if ("syncMissedDestokers".equals(method)) {
            List<Destocker> destockers = (List<Destocker>) response.body();
            if (destockers != null) {
                for (Destocker destocker : destockers) {
                    boolean exists = MesureDelegate.isExists(destocker.getMesureId().getUid());
                    boolean exist2 = ProduitDelegate.isExists(destocker.getProductId().getUid());
                    if (exists && exist2) {
                        boolean isSynced = DestockerDelegate.isExists(destocker.getUid());
                        if (!isSynced) {
                            DestockerDelegate.saveDestocker(destocker);
                        } else {
                            DestockerDelegate.updateDestocker(destocker);
                        }
                        Produit p = ProduitDelegate.findProduit(destocker.getProductId().getUid());
                        StockerDelegate.rectifyStockDepot(p, destocker.getDateDestockage().toLocalDate(), destocker.getRegion(), destocker.getCoutAchat());
                    }
                }
            }
        } else if ("syncMissedDepenses".equals(method)) {
            List<Depense> depenses = (List<Depense>) response.body();
            if (depenses != null) {
                for (Depense depense : depenses) {
                    boolean isSynced = DepenseDelegate.isExists(depense.getUid());
                    if (!isSynced) {
                        DepenseDelegate.saveDepense(depense);
                    } else {
                        DepenseDelegate.updateDepense(depense);
                    }

                }

            }
        } else if ("syncMissedOperations".equals(method)) {
            List<Operation> operations = (List<Operation>) response.body();
            if (operations != null) {
                for (Operation operation : operations) {
                    boolean exists = CompteTresorDelegate.isExists(operation.getTresorId().getUid());
                    boolean exist2 = TraisorerieDelegate.isExists(operation.getCaisseOpId().getUid());
                    boolean exist3 = DepenseDelegate.isExists(operation.getDepenseId().getUid());
                    if (exists && exist2 && exist3) {
                        boolean isSynced = OperationDelegate.isExists(operation.getUid());
                        if (!isSynced) {
                            OperationDelegate.saveOperation(operation);
                        } else {
                            OperationDelegate.updateOperation(operation);
                        }
                        Depense dep=DepenseDelegate.findDepense(operation.getDepenseId().getUid());
                        DepenseAgregateDelegate.aggregateDepense(operation.getDate(), operation.getImputation(), operation.getMontantUsd(), operation.getMontantCdf(), dep);
                    }
                }

            }
        } else if ("syncMissedMatieres".equals(method)) {
            List<Matiere> matieres = (List<Matiere>) response.body();
            if (matieres != null) {
                for (Matiere matiere : matieres) {
                    boolean isSynced = MatiereDelegate.isExists(matiere.getUid());
                    if (!isSynced) {
                        MatiereDelegate.saveMatiere(matiere);
                    } else {
                        MatiereDelegate.updateMatiere(matiere);
                    }
                }
            }
        } else if ("syncMissedMatiereSkus".equals(method)) {
            List<MatiereSku> skus = (List<MatiereSku>) response.body();
            if (skus != null) {
                for (MatiereSku sku : skus) {
                    Matiere parent = sku.getMatiere();
                    if (parent == null || MatiereDelegate.isExists(parent.getUid())) {
                        boolean isSynced = MatiereSkuDelegate.isExists(sku.getUid());
                        if (!isSynced) {
                            MatiereSkuDelegate.saveMatiereSku(sku);
                        } else {
                            MatiereSkuDelegate.updateMatiereSku(sku);
                        }
                    }
                }
            }
        } else if ("syncMissedDepots".equals(method)) {
            List<Depot> depots = (List<Depot>) response.body();
            if (depots != null) {
                for (Depot depot : depots) {
                    boolean isSynced = DepotDelegate.isExists(depot.getUid());
                    if (!isSynced) {
                        DepotDelegate.saveDepot(depot);
                    } else {
                        DepotDelegate.updateDepot(depot);
                    }
                }
            }
        } else if ("syncMissedProductions".equals(method)) {
            List<Production> productions = (List<Production>) response.body();
            if (productions != null) {
                for (Production production : productions) {
                    boolean productOk = production.getProduitId() == null
                            || ProduitDelegate.isExists(production.getProduitId().getUid());
                    boolean mesureOk = production.getMesureId() == null
                            || MesureDelegate.isExists(production.getMesureId().getUid());
                    if (productOk && mesureOk) {
                        boolean isSynced = ProductionDelegate.isExists(production.getUid());
                        if (!isSynced) {
                            ProductionDelegate.saveProduction(production);
                        } else {
                            ProductionDelegate.updateProduction(production);
                        }
                    }
                }
            }
        } else if ("syncMissedRepartirs".equals(method)) {
            List<Repartir> repartirs = (List<Repartir>) response.body();
            if (repartirs != null) {
                for (Repartir repartir : repartirs) {
                    boolean productionOk = repartir.getProductionId() != null
                            && ProductionDelegate.isExists(repartir.getProductionId().getUid());
                    boolean matiereOk = repartir.getMatiereId() != null
                            && MatiereDelegate.isExists(repartir.getMatiereId().getUid());
                    boolean skuOk = repartir.getSkuId() == null
                            || MatiereSkuDelegate.isExists(repartir.getSkuId().getUid());
                    if (productionOk && matiereOk && skuOk) {
                        boolean isSynced = RepartirDelegate.isExists(repartir.getUid());
                        if (!isSynced) {
                            RepartirDelegate.saveRepartir(repartir);
                        } else {
                            RepartirDelegate.updateRepartir(repartir);
                        }
                    }
                }
            }
        } else if ("syncMissedImputers".equals(method)) {
            List<Imputer> imputers = (List<Imputer>) response.body();
            if (imputers != null) {
                for (Imputer imputer : imputers) {
                    boolean operationOk = imputer.getOperationId() != null
                            && OperationDelegate.isExists(imputer.getOperationId().getUid());
                    boolean productionOk = imputer.getProductionId() != null
                            && ProductionDelegate.isExists(imputer.getProductionId().getUid());
                    if (operationOk && productionOk) {
                        boolean isSynced = ImputerDelegate.isExists(imputer.getUid());
                        if (!isSynced) {
                            ImputerDelegate.saveImputer(imputer);
                        } else {
                            ImputerDelegate.updateImputer(imputer);
                        }
                    }
                }
            }
        } else if ("syncMissedEntreposages".equals(method)) {
            List<Entreposer> entreposages = (List<Entreposer>) response.body();
            if (entreposages != null) {
                for (Entreposer entreposer : entreposages) {
                    boolean depotOk = entreposer.getDepotId() == null
                            || DepotDelegate.isExists(entreposer.getDepotId().getUid());
                    boolean livraisonOk = entreposer.getLivraisonId() == null
                            || LivraisonDelegate.isExists(entreposer.getLivraisonId().getUid());
                    boolean matiereOk = entreposer.getMatiereId() == null
                            || MatiereDelegate.isExists(entreposer.getMatiereId().getUid());
                    boolean skuOk = entreposer.getSkuId() == null
                            || MatiereSkuDelegate.isExists(entreposer.getSkuId().getUid());
                    boolean mesureOk = entreposer.getMesureId() == null
                            || MesureDelegate.isExists(entreposer.getMesureId().getUid());
                    boolean productionOk = entreposer.getProductionId() == null
                            || ProductionDelegate.isExists(entreposer.getProductionId().getUid());
                    if (depotOk && livraisonOk && matiereOk && skuOk && mesureOk && productionOk) {
                        boolean isSynced = EntreposerDelegate.isExists(entreposer.getUid());
                        if (!isSynced) {
                            EntreposerDelegate.saveEntreposer(entreposer);
                        } else {
                            EntreposerDelegate.updateEntreposer(entreposer);
                        }
                    }
                }
            }
        } else if ("syncMissedImmobilisations".equals(method)) {
            List<Immobilisation> immobilisations = (List<Immobilisation>) response.body();
            if (immobilisations != null) {
                for (Immobilisation immobilisation : immobilisations) {
                    boolean isSynced = ImmobilisationDelegate.isExists(immobilisation.getUid());
                    if (!isSynced) {
                        ImmobilisationDelegate.saveImmobilisation(immobilisation);
                    } else {
                        ImmobilisationDelegate.updateImmobilisation(immobilisation);
                    }
                }
            }
        } else if ("syncMissedPresences".equals(method)) {
            List<Presence> presences = (List<Presence>) response.body();
            if (presences != null) {
                for (Presence presence : presences) {
                    boolean isSynced = PresenceDelegate.isExists(presence.getUid());
                    if (!isSynced) {
                        PresenceDelegate.savePresence(presence);
                    } else {
                        PresenceDelegate.updatePresence(presence);
                    }
                }
            }
        }
    }

    private void sendDataToCloud(long lastSession, ExceptionalRunnable onComplete) throws Exception {
        try {
            for (Tables t : Tables.values()) {
                WebResult wres = handleSyncRequest(lastSession, t);
                if (wres != null) {
                    int sizeOk = wres.getSuccessResultSize();
                    int sizeNo = wres.getFailureResultSize();
                    System.out.println(sizeOk + " reussi /" + (sizeOk + sizeNo));
                    Set<RequestResult> echecs = wres.getFailureResultSet();
                    for (RequestResult echec : echecs) {
                        String suid = echec.getStringInstanceId();
                        int iuid = echec.getIntInstanceId();
                        long luid = echec.getLongInstanceId();
                        System.out.println((suid == null ? iuid == 0 ? luid : iuid : suid) + " " + echec.getTableName()
                                + " raison : " + echec.getPayload());
                    }

                    System.out.println("\n\n\n");
                }
            }
            onComplete.run();
        } catch (Exception e) {
            System.err.println("Upsync Error: " + e.getMessage());
            throw e;
        }
    }

    private WebResult handleSyncRequest(long lastSession, Tables tableName) throws Exception {
        int subsize = 50;
        int start = 0;
        System.out.println("Gonna upsync Table " + tableName);
        Call<WebResult> wres = null;
        switch (tableName) {
            case CATEGORY -> {
                List<Category> cats = CategoryDelegate.findUnSyncedCategories(lastSession);
                wres = kazisafe.syncCategories(cats);
            }
            case PRODUIT -> {
                List<Produit> cats = ProduitDelegate.findUnSyncedProduct(lastSession);
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<Produit> subList = cats.subList(start, limit);
                    wres = kazisafe.syncProduct(produitToMarshalList(subList));
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;
            }
            case MESURE -> {
                List<Mesure> cats = MesureDelegate.findUnSyncedMesure(lastSession);
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<Mesure> subList = cats.subList(start, limit);
                    wres = kazisafe.syncMesure(subList);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;
            }
            case FOURNISSEUR -> {
                List<Fournisseur> suppliers = FournisseurDelegate.findUnSyncedFournisseurs(lastSession);
                wres = kazisafe.syncFournisseur(suppliers);
            }
            case LIVRAISON -> {
                List<Livraison> cats = LivraisonDelegate.findUnSyncedLivraisons(lastSession);
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<Livraison> subList = cats.subList(start, limit);
                    wres = kazisafe.syncLivraison(subList);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;
            }
            case STOCKER -> {
                List<Stocker> stoxs = StockerDelegate.findUnSyncedStockers(lastSession);
                int psize = (int) Math.ceil((double) stoxs.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, stoxs.size());
                    List<Stocker> subList = stoxs.subList(start, limit);
                    wres = kazisafe.syncStocks(subList);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;

            }
            case DESTOCKER -> {
                List<Destocker> destx = DestockerDelegate.findUnSyncedDestockers(lastSession);
                wres = kazisafe.syncDestockage(destx);
            }
            case RECQUISITION -> {
                List<Recquisition> cats = RecquisitionDelegate.findUnSyncedRecquisitions(lastSession);
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<Recquisition> subList = cats.subList(start, limit);
                    wres = kazisafe.syncRecquisition(subList);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;

            }
            case PRIXDEVENTE -> {
                List<PrixDeVente> cats = PrixDeVenteDelegate.findUnSyncedPrixDeVentes(lastSession);
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<PrixDeVente> subList = cats.subList(start, limit);
                    wres = kazisafe.syncPrices(subList);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;
            }
            case CLIENT -> {
                List<Client> clients = ClientDelegate.findUnSyncedClients(lastSession);
                wres = kazisafe.syncClient(clients);
            }
            case VENTE -> {
                List<Vente> cats = VenteDelegate.findUnSyncedVentes(lastSession);
                // verification
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<Vente> sales = cats.subList(start, limit);
                    List<VenteHelper> helpers = new ArrayList<>();
                    for (Vente sale : sales) {
                        VenteHelper vh = new VenteHelper();
                        vh.setVente(sale);
                        Client cl = sale.getClientId();
                        vh.setClient(cl);
                        helpers.add(vh);
                    }
                    wres = kazisafe.syncVentes(helpers);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;

            }
            case LIGNEVENTE -> {
                List<LigneVente> cats = LigneVenteDelegate.findUnSyncedLigneVentes(lastSession);
                int psize = (int) Math.ceil((double) cats.size() / subsize);
                for (int i = 0; i < psize; i++) {
                    start = i * subsize;
                    int limit = Math.min(start + subsize, cats.size());
                    List<LigneVente> subList = cats.subList(start, limit);
                    wres = kazisafe.syncLigneVente(subList);
                    Response<WebResult> result = wres.execute();
                    System.err.println("Reponse serveur " + tableName.name() + "->page " + i + " : " + result.code());
                    if (result.isSuccessful()) {
                        System.out.println("--------PASS-------");
                    }
                }
                start = 0;
                wres = null;

            }
            case COMPTETRESOR -> {
                List<CompteTresor> comptes = CompteTresorDelegate.findUnSyncedCompteTresors(lastSession);
                wres = kazisafe.syncCompteTresor(comptes);
            }
            case DEPENSE -> {
                List<Depense> deps = DepenseDelegate.findUnSyncedDepenses(lastSession);
                wres = kazisafe.syncDepense(deps);
            }
            case TRAISORERIE -> {
                List<Traisorerie> traisor = TraisorerieDelegate.findUnSyncedTraisoreries(lastSession);
                wres = kazisafe.syncTraisorerie(traisor);
            }
            case OPERATION -> {
                List<Operation> ops = OperationDelegate.findUnSyncedOperations(lastSession);
                wres = kazisafe.syncOperations(ops);
            }
            case INVENTORY -> {
                List<Inventaire> invents = InventaireDelegate.findUnSyncedInventories(lastSession);
                wres = kazisafe.syncInventories(invents);
            }
            case COMPTER -> {
                List<Compter> counts = CompterDelegate.findUnSyncedCounts(lastSession);
                wres = kazisafe.syncCounts(counts);
            }
            case MATIERE -> {
                List<Matiere> matieres = MatiereDelegate.findMatieres();
                if (matieres == null || matieres.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncMatiere(matieres);
            }
            case MATIERESKU -> {
                List<MatiereSku> skus = MatiereSkuDelegate.findMatiereSkus();
                if (skus == null || skus.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncMatiereSku(skus);
            }
            case DEPOT -> {
                List<Depot> depots = DepotDelegate.findDepots();
                if (depots == null || depots.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncDepot(depots);
            }
            case ENTREPOSER -> {
                List<Entreposer> entreposers = EntreposerDelegate.findEntreposers();
                if (entreposers == null || entreposers.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncEntreposage(entreposers);
            }
            case PRODUCTION -> {
                List<Production> productions = ProductionDelegate.findProductions();
                if (productions == null || productions.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncProduction(productions);
            }
            case REPARTIR -> {
                List<Repartir> repartirs = RepartirDelegate.findRepartirs();
                if (repartirs == null || repartirs.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncRepartir(repartirs);
            }
            case IMPUTER -> {
                List<Imputer> imputers = ImputerDelegate.findImputers();
                if (imputers == null || imputers.isEmpty()) {
                    return null;
                }
                wres = kazisafe.syncImputer(imputers);
            }
            case IMMOBILISATION -> {
                List<Immobilisation> immobilisations = ImmobilisationDelegate.findUnSynced(lastSession);
                wres = kazisafe.syncImmobilisations(immobilisations);
            }
            case PRESENCE -> {
                List<Presence> presences = PresenceDelegate.findUnSyncedPresences(lastSession);
                wres = kazisafe.syncPresences(presences);
            }

        }
        System.out.println("Wres " + wres);
        // ....
        if (wres == null) {
            return null;
        }
        Response<WebResult> result = wres.execute();
        System.err.println("Reponse serveur " + tableName.name() + ": " + result.code());
        if (result.code() == 401) {
            throw new IllegalStateException("Session expirée (401) pendant l'upsync: " + tableName.name());
        }
        if (result.isSuccessful()) {
            // allUpSyncPass++;
            return result.body();
        } else {
            System.err.println("Reponse serveur " + tableName.name() + " est non 200 est : " + result.code());
            return null;
        }

    }

    private ProductMarshalAdapter produitToMarshal(Produit produit, String base64Image) {
        ProductMarshalAdapter produitHelper = new ProductMarshalAdapter();
        produitHelper.setUid(produit.getUid());
        produitHelper.setCategoryId(produit.getCategoryId().getUid());
        produitHelper.setCodebar(produit.getCodebar());
        produitHelper.setCouleur(produit.getCouleur());
        produitHelper.setMarque(produit.getMarque());
        produitHelper.setModele(produit.getModele());
        produitHelper.setNomProduit(produit.getNomProduit());
        if (base64Image != null) {
            produitHelper.setImage("data:image/jpeg;base64," + base64Image);
        }
        produitHelper.setTaille(produit.getTaille());
        produitHelper.setMethodeInventaire(produit.getMethodeInventaire());
        return produitHelper;
    }

    private List<ProductMarshalAdapter> produitToMarshalList(List<Produit> products) {
        List<ProductMarshalAdapter> result = new ArrayList<>();
        for (Produit product : products) {
            String img64 = productImageToBase64(product);
            ProductMarshalAdapter marshaled = produitToMarshal(product, img64);
            result.add(marshaled);
        }
        return result;
    }

    private String productImageToBase64(Produit produit) {
        File f = FileUtils.pointFile(produit.getUid() + ".jpeg");
        if (!f.exists()) {
            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            f = FileUtils.streamTofile(is);
        }
        if (f != null) {
            try {
                byte[] pixa = FileUtils.readFromFile(f);
                String base64 = DatatypeConverter.printBase64Binary(pixa);
                return base64;
            } catch (IOException ex) {
                Logger.getLogger(HttpSyncHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

}
