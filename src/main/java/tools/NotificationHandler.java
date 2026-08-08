/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import data.*;
import data.helpers.Role;
import delegates.*;
import java.awt.Toolkit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.concurrent.CopyOnWriteArrayList;
import services.ManagedSessionFactory;
import tools.MemoryGuard;

/**
 *
 * @author eroot
 */
public class NotificationHandler implements EventHandler {

    Preferences pref;
    /**
     * Strong refs required: controllers register lambdas/method refs that are not
     * stored elsewhere. WeakReference let the GC drop them while the cached
     * controller stayed alive — UI then stopped updating after the first load.
     */
    private static final CopyOnWriteArrayList<OnDataSyncListener> onDataSyncListeners =
        new CopyOnWriteArrayList<>();
    private static final java.util.concurrent.ExecutorService sseExecutor =
        MemoryGuard.newSingleThreadExecutor("Kazisafe-SSE-Downsync-Worker");
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long[] RETRY_DELAYS_SECONDS = {5L, 15L, 30L, 60L, 120L};
    private static final java.util.concurrent.ScheduledExecutorService retryExecutor =
        MemoryGuard.newSingleThreadScheduledExecutor("Kazisafe-SSE-Retry-Worker");

    private enum DownsyncStatus {
        OK,
        DEPENDENCY_MISSING,
    }

    @Override
    public void onOpen() throws Exception {
        System.out.println(
            "Encours d'ecoute sur " +
                System.getProperty("os.name") +
                " u= " +
                System.getProperty("user.name") +
                "..."
        );
        pref = Preferences.userNodeForPackage(SyncEngine.class);
    }

    @Override
    public void onClosed() throws Exception {
        System.out.println("close");
    }

    @Override
    public void onMessage(String string, MessageEvent me) throws Exception {
        final String json = me.getData();
        final String id = me.getLastEventId();
        final String region = me.getEventName();
        System.out.println("Reception : Ping Connected (v):" + string);
        if (
            json != null && !json.equals("Connected!") && !json.equals("ping")
        ) {
            processMessage(json, id, region, 1);
        }
    }

    private void processMessage(
        String json,
        String id,
        String region,
        int attempt
    ) {
        sseExecutor.submit(() -> {
            try {
                final String eid = pref.get("eUid", "");
                final String reg = pref.get("region", "");
                final String role = pref.get("priv", null);
                if (id != null && id.equals(eid)) {
                    boolean ok = false;
                    if (
                        Role.Trader.name().equals(role) ||
                        (role != null &&
                            role.contains(Role.ALL_ACCESS.name()))
                    ) {
                        ok = true;
                    } else {
                        if (reg.equals(region) || region.equals("*")) {
                            ok = true;
                        }
                    }
                    if (!ok) {
                        SyncLogger.getInstance().log(
                            null,
                            "SSE downsync - message d'une autre region (ignore)",
                            region,
                            id
                        );
                        return;
                    }
                    if (attempt == 1) {
                        javafx.application.Platform.runLater(() -> {
                            MainUI.notifySync(
                                "Sync",
                                "Un element a ete synchronise",
                                region
                            );
                        });
                    }
                    BaseModel obj = JsonUtil.toBaseModelObject(json);
                    if (obj == null) {
                        SyncLogger.getInstance().log(
                            null,
                            "SSE downsync - objet null ou table non supportee",
                            null,
                            id
                        );
                        return;
                    }
                    Tables t;
                    try {
                        t = Tables.valueOf(obj.getType());
                    } catch (IllegalArgumentException e) {
                        SyncLogger.getInstance().log(
                            e,
                            "SSE downsync - type de table inconnu",
                            obj.getType(),
                            id
                        );
                        return;
                    }
                    DownsyncStatus[] status = {DownsyncStatus.OK};
                    boolean isDelete = obj.getAction() != null
                        && (obj.getAction().equalsIgnoreCase("delete")
                            || obj.getAction().equalsIgnoreCase("remove"));
                    executeDownsyncMutation(() -> {
                            switch (t) {
                                case Tables.PRODUIT -> {
                                    Produit product = (Produit) obj;
                                    String categoryUid = refUid(
                                        product.getCategoryId()
                                    );
                                    // Produit sans catégorie : on attribue la
                                    // catégorie par défaut "Divers" — on vérifie
                                    // d'abord si elle existe, sinon on la crée,
                                    // puis on l'attribue au produit.
                                    if (categoryUid == null) {
                                        product.setCategoryId(
                                            findOrCreateDiversCategory()
                                        );
                                        categoryUid = refUid(
                                            product.getCategoryId()
                                        );
                                    }
                                    boolean exist =
                                        categoryUid != null &&
                                        CategoryDelegate.isExists(categoryUid);
                                    if (!exist) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            ProduitDelegate.isExists(
                                                product.getUid()
                                            );
                                        Produit result;
                                        if (!isSynced) {
                                            result =
                                                ProduitDelegate.saveProduit(
                                                    product
                                                );
                                        } else {
                                            result =
                                                ProduitDelegate.updateProduit(
                                                    product
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.CATEGORY -> {
                                    Category c = (Category) obj;
                                    boolean isSynced =
                                        CategoryDelegate.isExists(c.getUid());
                                    Category result;
                                    if (!isSynced) {
                                        result = CategoryDelegate.saveCategory(
                                            c
                                        );
                                    } else {
                                        Category cat =
                                            CategoryDelegate.findCategory(
                                                c.getUid()
                                            );
                                        cat.setDescritption(
                                            c.getDescritption()
                                        );
                                        cat.setUpdatedAt(c.getUpdatedAt());
                                        cat.setDeletedAt(c.getDeletedAt());
                                        result =
                                            CategoryDelegate.updateCategory(
                                                cat
                                            );
                                    }
                                    notifySynced(result);
                                }
                                case Tables.MESURE -> {
                                    Mesure measure = (Mesure) obj;
                                    String produitUid = refUid(
                                        measure.getProduitId()
                                    );
                                    boolean exists =
                                        produitUid != null &&
                                        ProduitDelegate.isExists(produitUid);
                                    if (!exists) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            MesureDelegate.isExists(
                                                measure.getUid()
                                            );
                                        Mesure result;
                                        if (!isSynced) {
                                            result = MesureDelegate.saveMesure(
                                                measure
                                            );
                                        } else {
                                            result =
                                                MesureDelegate.updateMesure(
                                                    measure
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.FOURNISSEUR -> {
                                    Fournisseur supplier = (Fournisseur) obj;
                                    Fournisseur result =
                                        FournisseurDelegate.syncFournisseurSafe(
                                            supplier
                                        );
                                    notifySynced(result);
                                }
                                case Tables.LIVRAISON -> {
                                    Livraison delivery = (Livraison) obj;
                                    String fournisseurUid = refUid(
                                        delivery.getFournId()
                                    );
                                    boolean exists =
                                        fournisseurUid != null &&
                                        FournisseurDelegate.isExists(
                                            fournisseurUid
                                        );
                                    if (!exists) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            LivraisonDelegate.isExists(
                                                delivery.getUid()
                                            );
                                        System.out.println(
                                            "after livraison exist- "
                                        );
                                        Livraison result;
                                        if (!isSynced) {
                                            result =
                                                LivraisonDelegate.saveLivraison(
                                                    delivery
                                                );
                                        } else {
                                            result =
                                                LivraisonDelegate.updateLivraison(
                                                    delivery
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.STOCKER -> {
                                    Stocker stocker = (Stocker) obj;
                                    String livraisonUid = refUid(
                                        stocker.getLivraisId()
                                    );
                                    String mesureUid = refUid(
                                        stocker.getMesureId()
                                    );
                                    String productUid = refUid(
                                        stocker.getProductId()
                                    );
                                    boolean exists =
                                        livraisonUid != null &&
                                        LivraisonDelegate.isExists(livraisonUid);
                                    boolean exist1 =
                                        mesureUid != null &&
                                        MesureDelegate.isExists(mesureUid);
                                    boolean exist2 =
                                        productUid != null &&
                                        ProduitDelegate.isExists(productUid);
                                    if (!(exists && exist1 && exist2)) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            StockerDelegate.isExists(
                                                stocker.getUid()
                                            );
                                        Stocker result;
                                        if (!isSynced) {
                                            result =
                                                StockerDelegate.saveStocker(
                                                    stocker
                                                );
                                        } else {
                                            result =
                                                StockerDelegate.updateStocker(
                                                    stocker
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.DESTOCKER -> {
                                    Destocker destocker = (Destocker) obj;
                                    String mesureUid = refUid(
                                        destocker.getMesureId()
                                    );
                                    String productUid = refUid(
                                        destocker.getProductId()
                                    );
                                    boolean exists =
                                        mesureUid != null &&
                                        MesureDelegate.isExists(mesureUid);
                                    boolean exist2 =
                                        productUid != null &&
                                        ProduitDelegate.isExists(productUid);
                                    if (!(exists && exist2)) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            DestockerDelegate.isExists(
                                                destocker.getUid()
                                            );
                                        Destocker result;
                                        if (!isSynced) {
                                            result =
                                                DestockerDelegate.saveDestocker(
                                                    destocker
                                                );
                                        } else {
                                            result =
                                                DestockerDelegate.updateDestocker(
                                                    destocker
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.RECQUISITION -> {
                                    Recquisition recquisition =
                                        (Recquisition) obj;
                                    String productUid = refUid(
                                        recquisition.getProductId()
                                    );
                                    String mesureUid = refUid(
                                        recquisition.getMesureId()
                                    );
                                    boolean exists =
                                        productUid != null &&
                                        ProduitDelegate.isExists(productUid);
                                    boolean exist1 =
                                        mesureUid != null &&
                                        MesureDelegate.isExists(mesureUid);
                                    if (!(exists && exist1)) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            RecquisitionDelegate.isExists(
                                                recquisition.getUid()
                                            );

                                        Recquisition result;
                                        if (!isSynced) {
                                            result =
                                                RecquisitionDelegate.saveRecquisition(
                                                    recquisition
                                                );
                                        } else {
                                            result =
                                                RecquisitionDelegate.updateRecquisition(
                                                    recquisition
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.PRIXDEVENTE -> {
                                    PrixDeVente price = (PrixDeVente) obj;
                                    String recquisitionUid = refUid(
                                        price.getRecquisitionId()
                                    );
                                    String mesureUid = refUid(
                                        price.getMesureId()
                                    );
                                    boolean exists =
                                        recquisitionUid != null &&
                                        RecquisitionDelegate.isExists(
                                            recquisitionUid
                                        );
                                    boolean exist1 =
                                        mesureUid != null &&
                                        MesureDelegate.isExists(mesureUid);
                                    if (!(exists && exist1)) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            PrixDeVenteDelegate.isExists(
                                                price.getUid()
                                            );

                                        PrixDeVente result;
                                        if (!isSynced) {
                                            price.setRecquisitionId(
                                                RecquisitionDelegate.findRecquisition(
                                                    price
                                                        .getRecquisitionId()
                                                        .getUid()
                                                )
                                            );
                                            result =
                                                PrixDeVenteDelegate.savePrixDeVente(
                                                    price
                                                );
                                        } else {
                                            result =
                                                PrixDeVenteDelegate.updatePrixDeVente(
                                                    price
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.CLIENT -> {
                                    Client client = (Client) obj;
                                    Client result =
                                        ClientDelegate.syncClientSafe(client);
                                    notifySynced(result);
                                }
                                case Tables.COMPTETRESOR -> {
                                    CompteTresor account = (CompteTresor) obj;
                                    boolean isSynced =
                                        CompteTresorDelegate.isExists(
                                            account.getUid()
                                        );

                                    CompteTresor result;
                                    if (!isSynced) {
                                        result =
                                            CompteTresorDelegate.saveCompteTresor(
                                                account
                                            );
                                    } else {
                                        result =
                                            CompteTresorDelegate.updateCompteTresor(
                                                account
                                            );
                                    }
                                    notifySynced(result);
                                }
                                case Tables.VENTE -> {
                                    Vente vente = (Vente) obj;
                                    if (isDelete) {
                                        Integer vuid = vente.getUid();
                                        if (vuid != null) {
                                            Vente existingV = VenteDelegate.findVente(
                                                vuid
                                            );
                                            if (existingV != null) {
                                                for (
                                                    LigneVente lv :
                                                        LigneVenteDelegate.findByReference(
                                                            existingV.getUid()
                                                        )
                                                ) {
                                                    LigneVenteDelegate.deleteLigneVente(
                                                        lv
                                                    );
                                                }
                                                VenteDelegate.deleteVente(existingV);
                                            }
                                        }
                                        notifySynced(vente);
                                        return;
                                    }
                                    String clientUid = refUid(
                                        vente.getClientId()
                                    );
                                    boolean exists =
                                        clientUid != null &&
                                        ClientDelegate.isExists(clientUid);
                                    if (!exists) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            VenteDelegate.isExists(
                                                vente.getUid()
                                            );
                                        Vente result;
                                        if (!isSynced) {
                                            result = VenteDelegate.saveVente(
                                                vente
                                            );
                                        } else {
                                            result = VenteDelegate.updateVente(
                                                vente
                                            );
                                            removeOldLigneVente(vente);
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.LIGNEVENTE -> {
                                    LigneVente saleitem = (LigneVente) obj;
                                    if (isDelete) {
                                        LigneVenteDelegate.deleteLigneVente(
                                            saleitem
                                        );
                                        notifySynced(saleitem);
                                        return;
                                    }
                                    boolean exists = saleitem.getProductId() != null && saleitem.getProductId().getUid() != null
                                            && ProduitDelegate.isExists(saleitem.getProductId().getUid());
                                    boolean exist1 = saleitem.getMesureId() != null && saleitem.getMesureId().getUid() != null
                                            && MesureDelegate.isExists(saleitem.getMesureId().getUid());
                                    boolean exist2 = saleitem.getReference() != null && saleitem.getReference().getUid() != null
                                            && VenteDelegate.isExists(saleitem.getReference().getUid());
                                    if (!(exists && exist1 && exist2)) {
                                        status[0] = DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            LigneVenteDelegate.isExists(
                                                saleitem.getUid()
                                            );
                                        LigneVente result;
                                        if (!isSynced) {
                                            result =
                                                LigneVenteDelegate.saveLigneVente(
                                                    saleitem
                                                );
                                        } else {
                                            result =
                                                LigneVenteDelegate.updateLigneVente(
                                                    saleitem
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.TRAISORERIE -> {
                                    Traisorerie trans = (Traisorerie) obj;
                                    String tresorUid = refUid(
                                        trans.getTresorId()
                                    );
                                    boolean exists =
                                        tresorUid != null &&
                                        CompteTresorDelegate.isExists(tresorUid);
                                    if (!exists) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            TraisorerieDelegate.isExists(
                                                trans.getUid()
                                            );
                                        Traisorerie result;
                                        if (!isSynced) {
                                            result =
                                                TraisorerieDelegate.saveTraisorerie(
                                                    trans
                                                );
                                        } else {
                                            result =
                                                TraisorerieDelegate.updateTraisorerie(
                                                    trans
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.DEPENSE -> {
                                    Depense depense = (Depense) obj;
                                    boolean isSynced = DepenseDelegate.isExists(
                                        depense.getUid()
                                    );
                                    Depense result;
                                    if (!isSynced) {
                                        result = DepenseDelegate.saveDepense(
                                            depense
                                        );
                                    } else {
                                        result = DepenseDelegate.updateDepense(
                                            depense
                                        );
                                    }
                                    notifySynced(result);
                                }
                                case Tables.OPERATION -> {
                                    Operation operation = (Operation) obj;
                                    String tresorUid = refUid(
                                        operation.getTresorId()
                                    );
                                    String caisseUid = refUid(
                                        operation.getCaisseOpId()
                                    );
                                    String depenseUid = refUid(
                                        operation.getDepenseId()
                                    );
                                    boolean exists =
                                        tresorUid != null &&
                                        CompteTresorDelegate.isExists(tresorUid);
                                    boolean exist2 =
                                        caisseUid != null &&
                                        TraisorerieDelegate.isExists(caisseUid);
                                    boolean exist3 =
                                        depenseUid != null &&
                                        DepenseDelegate.isExists(depenseUid);
                                    if (!(exists && exist2 && exist3)) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            OperationDelegate.isExists(
                                                operation.getUid()
                                            );
                                        Operation result;
                                        if (!isSynced) {
                                            result =
                                                OperationDelegate.saveOperation(
                                                    operation
                                                );
                                        } else {
                                            result =
                                                OperationDelegate.updateOperation(
                                                    operation
                                                );
                                        }
                                        Depense dep =
                                            DepenseDelegate.findDepense(
                                                operation
                                                    .getDepenseId()
                                                    .getUid()
                                            );
                                        DepenseAgregateDelegate.aggregateDepense(
                                            operation.getDate(),
                                            operation.getImputation(),
                                            operation.getMontantUsd(),
                                            operation.getMontantCdf(),
                                            dep
                                        );
                                        notifySynced(result);
                                    }
                                }
                                case Tables.COMPTER -> {
                                    Compter compter = (Compter) obj;
                                    String inventaireUid = refUid(
                                        compter.getInventaireId()
                                    );
                                    String mesureUid = refUid(
                                        compter.getMesureId()
                                    );
                                    String productUid = refUid(
                                        compter.getProductId()
                                    );
                                    boolean exists =
                                        inventaireUid != null &&
                                        InventaireDelegate.isExists(
                                            inventaireUid
                                        );
                                    boolean exist1 =
                                        mesureUid != null &&
                                        MesureDelegate.isExists(mesureUid);
                                    boolean exist2 =
                                        productUid != null &&
                                        ProduitDelegate.isExists(productUid);
                                    if (!(exists && exist1 && exist2)) {
                                        status[0] =
                                            DownsyncStatus.DEPENDENCY_MISSING;
                                    } else {
                                        boolean isSynced =
                                            CompterDelegate.isExists(
                                                compter.getUid()
                                            );
                                        Compter result;
                                        if (compter.getDeletedAt() != null) {
                                            if (isSynced) {
                                                CompterDelegate.deleteCompter(
                                                    compter
                                                );
                                            }
                                            result = compter;
                                        } else if (!isSynced) {
                                            System.out.println("new compter");
                                            result =
                                                CompterDelegate.createCompter(
                                                    compter
                                                );
                                        } else {
                                            result =
                                                CompterDelegate.updateCompter(
                                                    compter
                                                );
                                            System.out.println("edit compter");
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.INVENTORY -> {
                                    Inventaire inventory = (Inventaire) obj;
                                    boolean isSynced =
                                        InventaireDelegate.isExists(
                                            inventory.getUid()
                                        );
                                    Inventaire result;
                                    if (!isSynced) {
                                        result =
                                            InventaireDelegate.createInventaire(
                                                inventory
                                            );
                                    } else {
                                        result =
                                            InventaireDelegate.updateInventaire(
                                                inventory
                                            );
                                    }
                                    notifySynced(result);
                                }
                                case Tables.PRESENCE -> {
                                    Presence presence = (Presence) obj;
                                    boolean isSynced =
                                        PresenceDelegate.isExists(
                                            presence.getUid()
                                        );
                                    Presence result;
                                    if (!isSynced) {
                                        result = PresenceDelegate.savePresence(
                                            presence
                                        );
                                    } else {
                                        result =
                                            PresenceDelegate.updatePresence(
                                                presence
                                            );
                                    }
                                    notifySynced(result);
                                }
                                default -> {
                                }
                            }
                        });
                    if (status[0] == DownsyncStatus.DEPENDENCY_MISSING) {
                        scheduleRetry(json, id, region, attempt + 1, obj.getType());
                    }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SyncLogger.getInstance().log(
                        ex,
                        "SSE downsync - erreur",
                        null,
                        id
                    );
                    scheduleRetry(json, id, region, attempt + 1, null);
                }
            });
    }

    private void scheduleRetry(
        String json,
        String id,
        String region,
        int attempt,
        String type
    ) {
        if (attempt > MAX_RETRY_ATTEMPTS) {
            SyncLogger.getInstance().log(
                null,
                "SSE downsync - abandon apres " +
                    MAX_RETRY_ATTEMPTS +
                    " tentatives (dependance manquante)",
                type,
                id
            );
            return;
        }
        int idx = Math.min(attempt - 1, RETRY_DELAYS_SECONDS.length - 1);
        long delaySeconds = RETRY_DELAYS_SECONDS[idx];
        System.out.println(
            "SSE downsync: dependance manquante (" +
                type +
                " " +
                id +
                "), tentative " +
                attempt +
                "/" +
                MAX_RETRY_ATTEMPTS +
                " dans " +
                delaySeconds +
                "s"
        );
        SyncLogger.getInstance().log(
            null,
            "SSE downsync - dependance manquante, tentative " +
                attempt +
                "/" +
                MAX_RETRY_ATTEMPTS +
                " differee de " +
                delaySeconds +
                "s",
            type,
            id
        );
        retryExecutor.schedule(
            () -> processMessage(json, id, region, attempt),
            delaySeconds,
            java.util.concurrent.TimeUnit.SECONDS
        );
    }

    private void executeDownsyncMutation(Runnable action) {
        if (ManagedSessionFactory.isEmbedded()) {
            action.run();
            return;
        }

        ManagedSessionFactory.runInSession(em -> {
            jakarta.persistence.EntityTransaction tx = em.getTransaction();
            boolean started = !tx.isActive();
            if (started) {
                tx.begin();
            }
            try {
                action.run();
                if (started && tx.isActive()) {
                    tx.commit();
                }
            } catch (RuntimeException ex) {
                if (started && tx.isActive()) {
                    tx.rollback();
                }
                throw ex;
            }
        });
    }

    @Override
    public void onComment(String string) throws Exception {
        System.out.println("Commentaire " + string);
    }

    private com.launchdarkly.eventsource.EventSource eventSource;

    public void setEventSource(
        com.launchdarkly.eventsource.EventSource eventSource
    ) {
        this.eventSource = eventSource;
    }

    @Override
    public void onError(Throwable thrwbl) {
        System.out.println("SSE Error " + thrwbl.getMessage());
        if (
            thrwbl.getMessage() != null && thrwbl.getMessage().contains("401")
        ) {
            System.err.println(
                "Closing SSE Stream due to persistent 401 error."
            );
            if (this.eventSource != null) {
                this.eventSource.close();
            }
        }
    }

    private void removeOldLigneVente(Vente vente) {
        List<LigneVente> ls = LigneVenteDelegate.findByReference(
            vente.getUid()
        );
        for (LigneVente l : ls) {
            LigneVenteDelegate.deleteLigneVente(l);
        }
    }

    private void beep() {
        for (int i = 0; i < 3; i++) {
            try {
                for (int x = 0; x < 8; x++) {
                    Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(65);
                }
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(NotificationHandler.class.getName()).log(
                    Level.SEVERE,
                    null,
                    ex
                );
            }
        }
    }

    public static void setOnDataSyncListener(OnDataSyncListener listener) {
        registerOnDataSyncListener(listener);
    }

    public static void registerOnDataSyncListener(OnDataSyncListener listener) {
        if (listener == null) {
            return;
        }
        // Avoid duplicate registration if initialize() is ever called twice
        onDataSyncListeners.removeIf(existing -> existing == listener);
        onDataSyncListeners.add(listener);
    }

    public static void unregisterOnDataSyncListener(OnDataSyncListener listener) {
        if (listener == null) {
            return;
        }
        onDataSyncListeners.removeIf(existing -> existing == listener);
    }

    /**
     * Pushes a local or remote entity change to every registered UI listener.
     * Safe to call from any thread.
     */
    public static void broadcastDataSynced(BaseModel model) {
        if (model == null) {
            return;
        }
        for (OnDataSyncListener listener : onDataSyncListeners) {
            Runnable dispatch = () -> {
                try {
                    listener.onDataSynced(model);
                } catch (Exception ex) {
                    Logger.getLogger(NotificationHandler.class.getName()).log(
                        Level.WARNING,
                        "UI sync listener failure",
                        ex
                    );
                }
            };
            if (javafx.application.Platform.isFxApplicationThread()) {
                dispatch.run();
            } else {
                javafx.application.Platform.runLater(dispatch);
            }
        }
    }

    /**
     * UID d'une référence entité, sans NPE si la référence (ou son uid) est
     * absente du payload downsync.
     */
    /**
     * Recherche la catégorie "Divers" ; si elle n'existe pas encore, la crée
     * puis la retourne. Utilisée pour les produits dépourvus de catégorie.
     */
    private static Category findOrCreateDiversCategory() {
        List<Category> cats = CategoryDelegate.findCategories("Divers");
        if (!cats.isEmpty()) {
            return cats.get(0);
        }
        Category created = new Category(DataId.generate(), "Divers");
        return CategoryDelegate.saveCategory(created);
    }

    static String refUid(Object reference) {
        if (reference == null) {
            return null;
        }
        try {
            Object uid = reference.getClass().getMethod("getUid").invoke(reference);
            return uid == null ? null : uid.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void notifySynced(BaseModel uid) {
        DataCache.invalidateAll();
        broadcastDataSynced(uid);
    }
}
