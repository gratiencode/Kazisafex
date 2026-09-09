/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import data.*;
import data.core.KazisafeServiceFactory;
import data.dto.MesureUpsertDto;
import data.dto.ProduitUpsertDto;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.*;
import tools.sync.SyncMappers;
import retrofit2.Response;
import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.concurrent.CopyOnWriteArrayList;
import services.ManagedSessionFactory;
import services.utils.PermissionRegistry;
import services.utils.UserRoleRegistry;
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
    private static volatile Kazisafe kazisafeClient;

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
                if (id == null || id.equals(eid)) {
                    boolean ok = PermissionRegistry.hasGlobalAccess(pref);
                    if (!ok) {
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
                    // Fallback Jackson explicite pour tous les types si le premier parse est incomplet
                    if (obj == null || obj.getType() == null) {
                        String typeFromJson = extractJsonString(json, "type");
                        if (typeFromJson != null) {
                            try {
                                Tables ft = Tables.valueOf(typeFromJson);
                                switch (ft) {
                                    case PRODUIT -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, Produit.class);
                                    case MESURE -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, Mesure.class);
                                    case CLIENT -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, Client.class);
                                    case VENTE -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, Vente.class);
                                    case LIGNEVENTE -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, LigneVente.class);
                                    case COMPTETRESOR -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, CompteTresor.class);
                                    case TRAISORERIE -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, Traisorerie.class);
                                    case CATEGORY -> obj = data.core.KazisafeServiceFactory.mapper().readValue(json, Category.class);
                                    default -> {}
                                }
                                if (obj != null) System.out.println("[SSE] Jackson fallback OK type=" + ft + " uid=" + readUid(obj));
                            } catch (Exception e) {
                                SyncLogger.getInstance().log(e, "NotificationHandler.processMessage.jacksonFallback");
                                Logger.getLogger(NotificationHandler.class.getName()).log(Level.WARNING, "[SSE] Jackson fallback echec type=" + typeFromJson, e);
                            }
                        }
                    }
                    // Re-parse supplementaire pour TRAISORERIE si tresorId stub incomplet
                    if (obj instanceof Traisorerie trCheck && (trCheck.getTresorId() == null || trCheck.getTresorId().getUid() == null || trCheck.getTresorId().getUid().isBlank())) {
                        try {
                            Traisorerie reparsed = data.core.KazisafeServiceFactory.mapper().readValue(json, Traisorerie.class);
                            if (reparsed != null && reparsed.getTresorId() != null && reparsed.getTresorId().getUid() != null) {
                                obj = reparsed;
                                System.out.println("[SSE TRAISORERIE] reparsed tresorId=" + reparsed.getTresorId().getUid());
                            }
                        } catch (Exception e) {
                            SyncLogger.getInstance().log(e, "NotificationHandler.processMessage.reparseTraisorerie");
                            Logger.getLogger(NotificationHandler.class.getName()).log(Level.WARNING, "[SSE TRAISORERIE] reparsed echec", e);
                        }
                    }
                    // Re-parse supplementaire pour VENTE si clientId stub incomplet
                    if (obj instanceof Vente vCheck && vCheck.getClientId() != null && vCheck.getClientId().getUid() != null) {
                        Client resolvedClient = ClientDelegate.findClient(vCheck.getClientId().getUid());
                        if (resolvedClient != null) vCheck.setClientId(resolvedClient);
                    }
                    // Re-parse supplementaire pour LIGNEVENTE si references stubs incompletes
                    if (obj instanceof LigneVente lvCheck) {
                        if (lvCheck.getProductId() != null && lvCheck.getProductId().getUid() != null) {
                            Produit resolvedProd = ProduitDelegate.findProduit(lvCheck.getProductId().getUid());
                            if (resolvedProd != null) lvCheck.setProductId(resolvedProd);
                        }
                        if (lvCheck.getMesureId() != null && lvCheck.getMesureId().getUid() != null) {
                            Mesure resolvedMes = MesureDelegate.findMesure(lvCheck.getMesureId().getUid());
                            if (resolvedMes != null) lvCheck.setMesureId(resolvedMes);
                        }
                        if (lvCheck.getReference() != null && lvCheck.getReference().getUid() != null) {
                            try {
                                Vente resolvedVente = VenteDelegate.findVente(lvCheck.getReference().getUid());
                                if (resolvedVente != null) lvCheck.setReference(resolvedVente);
                            } catch (Exception ignored) {
                                SyncLogger.getInstance().log(ignored, "NotificationHandler.processMessage.resolveVenteRef");
                            }
                        }
                    }
                    // Re-parse supplementaire pour MESURE si produitId stub incomplet
                    if (obj instanceof Mesure mCheck && mCheck.getProduitId() != null && mCheck.getProduitId().getUid() != null) {
                        Produit resolvedProd2 = ProduitDelegate.findProduit(mCheck.getProduitId().getUid());
                        if (resolvedProd2 != null) mCheck.setProduitId(resolvedProd2);
                    }
                    // Re-parse supplementaire pour PRODUIT si categoryId stub incomplet
                    if (obj instanceof Produit pCheck && pCheck.getCategoryId() != null && pCheck.getCategoryId().getUid() != null) {
                        Category resolvedCat = CategoryDelegate.findCategory(pCheck.getCategoryId().getUid());
                        if (resolvedCat != null) pCheck.setCategoryId(resolvedCat);
                    }
                    if (obj == null) {
                        SyncLogger.getInstance().log(
                            null,
                            "SSE downsync - objet null ou table non supportee",
                            null,
                            id
                        );
                        return;
                    }
                    final BaseModel finalObj = obj;
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
                    // Garde tenant : si l'uid porte par le message est celui de
                    // l'entreprise locale (suffixe de la base / evenement region),
                    // ce n'est pas une entite. On ne le verifie jamais en base et
                    // on passe a l'entite suivante.
                    String entityUid = readUid(obj);
                    if (entityUid != null && isEnterpriseUid(entityUid)) {
                        SyncLogger.getInstance().log(
                            null,
                            "SSE downsync - uid = uid d'entreprise (ignore, pas une entite) ("
                                + t + ")",
                            region,
                            id
                        );
                        return;
                    }
                    DownsyncStatus[] status = {DownsyncStatus.OK};
                    boolean isDelete = obj.getAction() != null
                        && (obj.getAction().equalsIgnoreCase("delete")
                            || obj.getAction().equalsIgnoreCase("remove"));
                    // Règle de fraîcheur (identique à la matérialisation) : une
                    // version venant du réseau n'écrase la version locale que si
                    // son updatedAt est STRICTEMENT plus récent. Sinon la version
                    // locale (modifiée via l'UI) prévaut et l'objet SSE est ignoré.
                    if (!isDelete && isStaleNetworkUpdate(obj, t)) {
                        SyncLogger.getInstance().log(
                            null,
                            "SSE downsync - version locale plus recente, objet ignore ("
                                + t + " " + readUid(obj) + ")",
                            region,
                            id
                        );
                        return;
                    }
                    SyncOutboxListener.runSuppressed(() -> executeDownsyncMutation(() -> {
                            switch (t) {
                                case Tables.PRODUIT -> {
                                    Produit product = (Produit) finalObj;
                                    if (refUid(product.getCategoryId()) == null) {
                                        product.setCategoryId(
                                            findOrCreateDiversCategory()
                                        );
                                    } else if (!ensureDependency(product.getCategoryId())) {
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
                                    Category c = (Category) finalObj;
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
                                    Mesure measure = (Mesure) finalObj;
                                    // Reference produit TOUJOURS managee (issue de la
                                    // base) avant la persistance : une MESURE pointee
                                    // vers un produit transient/detached hors base fera
                                    // echouer le flush (TransientObjectException) puis
                                    // bouclera sur "dependance manquante". On re-pointe
                                    // donc vers le produit local, materialise si besoin
                                    // depuis son payload (comme cote Android).
                                    if (measure.getProduitId() != null
                                            && measure.getProduitId().getUid() != null) {
                                        Produit prodRef = ProduitDelegate.findProduit(
                                            measure.getProduitId().getUid()
                                        );
                                        if (prodRef == null
                                                && !ensureDependency(measure.getProduitId())) {
                                            status[0] =
                                                DownsyncStatus.DEPENDENCY_MISSING;
                                        } else {
                                            prodRef = ProduitDelegate.findProduit(
                                                measure.getProduitId().getUid()
                                            );
                                            if (prodRef == null) {
                                                status[0] =
                                                    DownsyncStatus.DEPENDENCY_MISSING;
                                            } else {
                                                measure.setProduitId(prodRef);
                                            }
                                        }
                                    }
                                    if (status[0] == DownsyncStatus.DEPENDENCY_MISSING) {
                                        // produit parent non materialisable -> retry
                                    } else {
                                        boolean mesureExists =
                                            MesureDelegate.isExists(measure.getUid());
                                        // MESURE deja presente : le stub SSE ne porte
                                        // pas toujours le produit parent. On conserve
                                        // le produit deja en base pour ne jamais
                                        // ecraser la FK avec null (not-null).
                                        if (measure.getProduitId() == null
                                                && mesureExists) {
                                            Mesure local =
                                                MesureDelegate.findMesure(
                                                    measure.getUid()
                                                );
                                            if (local != null
                                                    && local.getProduitId() != null) {
                                                measure.setProduitId(
                                                    local.getProduitId()
                                                );
                                            }
                                        }
                                        if (measure.getProduitId() == null) {
                                            // Stub MESURE sans parent produit
                                            // persistant : on ne peut pas la persister
                                            // en l'etat (FK produit_id not-null) ->
                                            // full-downsync requis.
                                            status[0] =
                                                DownsyncStatus.DEPENDENCY_MISSING;
                                        } else {
                                            Mesure result;
                                            if (!mesureExists) {
                                                result =
                                                    MesureDelegate.saveMesure(
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
                                }
                                case Tables.FOURNISSEUR -> {
                                    Fournisseur supplier = (Fournisseur) finalObj;
                                    Fournisseur result =
                                        FournisseurDelegate.syncFournisseurSafe(
                                            supplier
                                        );
                                    notifySynced(result);
                                }
                                case Tables.LIVRAISON -> {
                                    Livraison delivery = (Livraison) finalObj;
                                    if (!ensureDependency(delivery.getFournId())) {
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
                                    Stocker stocker = (Stocker) finalObj;
                                    boolean stockProdOk = ensureDependency(stocker.getProductId());
                                    boolean stockMesOk = ensureDependency(stocker.getMesureId());
                                    if (stockProdOk && !stockMesOk) {
                                        Produit sp = stocker.getProductId();
                                        if (sp != null && sp.getUid() != null) {
                                            Mesure fallback = MesureDelegate.findByProduitAndQuant(sp.getUid(), 1d);
                                            if (fallback != null) {
                                                stocker.setMesureId(fallback);
                                                stockMesOk = true;
                                            }
                                        }
                                    }
                                    if (!(ensureDependency(stocker.getLivraisId())
                                            && stockMesOk && stockProdOk)) {
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
                                        if (result != null) {
                                            StockerDelegate.rectifyStockDepotByLot(result.getProductId(), result.getNumlot(), result.getRegion(), result.getCoutAchat(), result.getDateExpir());
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.DESTOCKER -> {
                                    Destocker destocker = (Destocker) finalObj;
                                    boolean destProdOk = ensureDependency(destocker.getProductId());
                                    boolean destMesOk = ensureDependency(destocker.getMesureId());
                                    if (destProdOk && !destMesOk) {
                                        Produit dp = destocker.getProductId();
                                        if (dp != null && dp.getUid() != null) {
                                            Mesure fallback = MesureDelegate.findByProduitAndQuant(dp.getUid(), 1d);
                                            if (fallback != null) {
                                                destocker.setMesureId(fallback);
                                                destMesOk = true;
                                            }
                                        }
                                    }
                                    if (!(destMesOk && destProdOk)) {
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
                                        if (result != null) {
                                            StockerDelegate.rectifyStockDepotByLot(result.getProductId(), result.getNumlot(), result.getRegion(), result.getCoutAchat(), null);
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.RECQUISITION -> {
                                    Recquisition recquisition =
                                        (Recquisition) finalObj;
                                    boolean recqProdOk = ensureDependency(recquisition.getProductId());
                                    boolean recqMesOk = ensureDependency(recquisition.getMesureId());
                                    if (recqProdOk && !recqMesOk) {
                                        Produit rp = recquisition.getProductId();
                                        if (rp != null && rp.getUid() != null) {
                                            Mesure fallback = MesureDelegate.findByProduitAndQuant(rp.getUid(), 1d);
                                            if (fallback != null) {
                                                recquisition.setMesureId(fallback);
                                                recqMesOk = true;
                                            }
                                        }
                                    }
                                    if (!(recqProdOk && recqMesOk)) {
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
                                    PrixDeVente price = (PrixDeVente) finalObj;
                                    if (!(ensureDependency(price.getRecquisitionId())
                                            && ensureDependency(price.getMesureId()))) {
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
                                    Client client = (Client) finalObj;
                                    Client result =
                                        ClientDelegate.syncClientSafe(client);
                                    notifySynced(result);
                                }
                                case Tables.COMPTETRESOR -> {
                                    CompteTresor account = (CompteTresor) finalObj;
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
                                    Vente vente = (Vente) finalObj;
                                    // Fallback Jackson + resolution client complet comme TRAISORERIE
                                    if (vente.getClientId() != null && vente.getClientId().getUid() != null) {
                                        Client fullClient = ClientDelegate.findClient(vente.getClientId().getUid());
                                        if (fullClient != null) vente.setClientId(fullClient);
                                    }
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
                                    if (!ensureDependency(vente.getClientId())) {
                                        // Client reference introuvable localement :
                                        // on bascule sur le client "Anonyme" deja en
                                        // base pour ne pas bloquer la vente en boucle
                                        // de retry (la FK clientId est nullable et le
                                        // client Anonyme est le compte de repli).
                                        Client anon = ClientDelegate.findAnonymousClient();
                                        if (anon != null) {
                                            vente.setClientId(anon);
                                        } else {
                                            status[0] =
                                                DownsyncStatus.DEPENDENCY_MISSING;
                                        }
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
                                            // Venant du SSE, on compare par uid +
                                            // observation. Tant que la vente entrante
                                            // est toujours un brouillon (Drafted), on
                                            // conserve les lignes locales (actives +
                                            // soft-deleted) telles quelles. Des que le
                                            // terminal d'origine a finalise le brouillon
                                            // (Drafted -> vente normale, observation !=
                                            // Drafted), les anciennes lignes du brouillon
                                            // sont retirees de la liste : on les hard-delete
                                            // (qu'elles soient soft-deleted ou non) pour ne
                                            // garder que ce qui sera re-delivre comme
                                            // LIGNEVENTE finalisee.
                                            if (vente.getObservation() == null
                                                    || !vente.getObservation().equals("Drafted")) {
                                                removeOldLigneVente(vente);
                                                LigneVenteDelegate
                                                    .hardDeleteSoftDeletedByReference(
                                                        vente.getUid()
                                                    );
                                            }
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.LIGNEVENTE -> {
                                    LigneVente saleitem = (LigneVente) finalObj;
                                    if (isDelete) {
                                        LigneVenteDelegate.deleteLigneVente(
                                            saleitem
                                        );
                                        notifySynced(saleitem);
                                        return;
                                    }
                                    // On prend la reference de la ligne entrante pour
                                    // selectionner la vente y relative, puis on verifie
                                    // le champ observation afin de savoir si la vente est
                                    // toujours un brouillon (Drafted) ou si elle a ete
                                    // finalisee (Drafted -> vente normale).
                                    Vente refVente = saleitem.getReference();
                                    Integer refUid = refVente != null ? refVente.getUid() : null;
                                    boolean stillDrafted = false;
                                    if (refUid != null) {
                                        Vente localVente = VenteDelegate.findVente(refUid);
                                        String obs = refVente.getObservation() != null
                                                ? refVente.getObservation()
                                                : (localVente != null ? localVente.getObservation() : null);
                                        stillDrafted = obs != null && obs.equals("Drafted");
                                    }
                                    // Copie cross-tenant : la ligne diffusee peut porter
                                    // l'ANCIEN uid de mesure (et le nouveau produit). On
                                    // re-point d'abord les FK sur les NOUVEAUX uids du
                                    // payload (replacement/measureUids) quand ils existent
                                    // localement, AVANT le controle de dependance, pour que
                                    // la persistance utilise les copies materialisees.
                                    remapCrossTenantLigne(saleitem);
                                    boolean lvProdOk = ensureDependency(saleitem.getProductId());
                                    boolean lvMesOk = ensureDependency(saleitem.getMesureId());
                                    boolean lvVenOk = ensureDependency(saleitem.getReference());
                                    // Copie cross-tenant : la ligne porte l'ANCIENNE mesure
                                    // (uid d'un autre tenant). Son payload la mappe vers la
                                    // NOUVELLE mesure locale (measureUids). Si cette nouvelle
                                    // mesure existe, on re-pointe la ligne dessus afin que la
                                    // persistance ne soit plus bloque par la dependance.
                                    if (!lvMesOk && saleitem.getMesureId() != null
                                            && saleitem.getMesureId().getUid() != null) {
                                        String mapped = mappedNewMesureUid(saleitem.getPayload(),
                                                saleitem.getMesureId().getUid());
                                        if (mapped != null && MesureDelegate.isExists(mapped)) {
                                            Mesure localNew = MesureDelegate.findMesure(mapped);
                                            if (localNew != null) {
                                                saleitem.setMesureId(localNew);
                                                lvMesOk = true;
                                            }
                                        }
                                    }
                                    if (lvProdOk && !lvMesOk) {
                                        Produit lp = saleitem.getProductId();
                                        if (lp != null && lp.getUid() != null) {
                                            Mesure fallback = MesureDelegate.findByProduitAndQuant(lp.getUid(), 1d);
                                            if (fallback != null) {
                                                saleitem.setMesureId(fallback);
                                                lvMesOk = true;
                                            }
                                        }
                                    }
                                    // Description de la mesure : une vente vendue depuis un
                                    // terminal Android peut porter une mesure dont la copie
                                    // cross-tenant locale a une description null. On repare
                                    // en priorite via la version locale (clone depuis
                                    // l'ancienne mesure du payload), et on ne recourt au
                                    // backend que si la description locale est aussi null.
                                    if (lvMesOk && saleitem.getMesureId() != null) {
                                        if (!ensureMesureDescriptionLocal(saleitem)) {
                                            SyncLogger.getInstance().logMessage(
                                                    "NotificationHandler.LIGNEVENTE",
                                                    "description MESURE indisponible uid="
                                                    + saleitem.getMesureId().getUid()
                                                    + " (source locale et backend sans description)");
                                        }
                                    }
                                    if (!(lvProdOk && lvMesOk && lvVenOk)) {
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
                                        // Suite de la logique selon l'observation de la
                                        // vente liee :
                                        //  - toujours Drafted -> la ligne (soft-deleted ou
                                        //    non) est conservee telle quelle localement ;
                                        //  - plus Drafted (finalisee sur le terminal
                                        //    d'origine) -> les lignes retirees de la liste
                                        //    au paiement (passees soft-deleted puis
                                        //    hard-deleted) ne figurent plus : on hard-delete
                                        //    les lignes locales soft-deleted de cette vente.
                                        if (!stillDrafted && refUid != null) {
                                            LigneVenteDelegate
                                                .hardDeleteSoftDeletedByReference(
                                                    refUid
                                                );
                                        }
                                        notifySynced(result);
                                    }
                                }
                                case Tables.TRAISORERIE -> {
                                    Traisorerie trans = (Traisorerie) finalObj;
                                    if (!ensureDependency(trans.getTresorId())) {
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
                                        System.out.println("[SSE TRAISORERIE] synced uid=" + result.getUid() + " ref=" + result.getReference() + " montantUsd=" + result.getMontantUsd() + " tresor=" + (result.getTresorId() != null ? result.getTresorId().getUid() : "null"));
                                        notifySynced(result);
                                    }
                                }
                                case Tables.DEPENSE -> {
                                    Depense depense = (Depense) finalObj;
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
                                    Operation operation = (Operation) finalObj;
                                    if (!(ensureDependency(operation.getTresorId())
                                            && ensureDependency(operation.getCaisseOpId())
                                            && ensureDependency(operation.getDepenseId()))) {
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
                                    Compter compter = (Compter) finalObj;
                                    if (!(ensureDependency(compter.getInventaireId())
                                            && ensureDependency(compter.getMesureId())
                                            && ensureDependency(compter.getProductId()))) {
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
                                    Inventaire inventory = (Inventaire) finalObj;
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
                                    Presence presence = (Presence) finalObj;
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
                        }));
                    if (status[0] == DownsyncStatus.DEPENDENCY_MISSING) {
                        // Full downsync auto-resolution : tente de recuperer
                        // l'element manquant (produit avec image+mesures, ou
                        // mesure) puis rejoue immediatement. Echec -> retry.
                        if (tryResolveMissingDependency(obj)) {
                            processMessage(json, id, region, 1);
                        } else {
                            scheduleRetry(json, id, region, attempt + 1, obj.getType());
                        }
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

    /**
     * Règle de fraîcheur pour les objets SSE : un objet réseau n'est appliqué
     * que si son {@code updatedAt} est strictement plus récent que celui de
     * l'entité locale. Sinon la version locale (modifiée via l'UI) prévaut.
     * Sans timestamp ou sans entité locale existante, l'objet est appliqué.
     */
    private boolean isStaleNetworkUpdate(BaseModel incoming, Tables table) {
        LocalDateTime incomingUpdatedAt = readUpdatedAt(incoming);
        if (incomingUpdatedAt == null) {
            return false;
        }
        Object local = findLocalEntity(table, readUid(incoming));
        if (local == null) {
            return false;
        }
        LocalDateTime localUpdatedAt = readUpdatedAt(local);
        if (localUpdatedAt == null) {
            return false;
        }
        return !incomingUpdatedAt.isAfter(localUpdatedAt);
    }

    private Object findLocalEntity(Tables t, String uid) {
        if (uid == null || uid.isBlank()) {
            return null;
        }
        try {
            switch (t) {
                case PRODUIT -> {
                    return ProduitDelegate.findProduit(uid);
                }
                case CATEGORY -> {
                    return CategoryDelegate.findCategory(uid);
                }
                case MESURE -> {
                    return MesureDelegate.findMesure(uid);
                }
                case FOURNISSEUR -> {
                    return FournisseurDelegate.findFournisseur(uid);
                }
                case LIVRAISON -> {
                    return LivraisonDelegate.findLivraison(uid);
                }
                case STOCKER -> {
                    return StockerDelegate.findStocker(uid);
                }
                case DESTOCKER -> {
                    return DestockerDelegate.findDestocker(uid);
                }
                case RECQUISITION -> {
                    return RecquisitionDelegate.findRecquisition(uid);
                }
                case PRIXDEVENTE -> {
                    return PrixDeVenteDelegate.findPrixDeVente(uid);
                }
                case CLIENT -> {
                    return ClientDelegate.findClient(uid);
                }
                case COMPTETRESOR -> {
                    return CompteTresorDelegate.findCompteTresor(uid);
                }
                case VENTE -> {
                    try {
                        return VenteDelegate.findVente(Integer.parseInt(uid));
                    } catch (NumberFormatException e) {
                        SyncLogger.getInstance().log(e, "NotificationHandler.findEntityByUid.vente");
                        return null;
                    }
                }
                case LIGNEVENTE -> {
                    try {
                        return LigneVenteDelegate.findLigneVente(Long.parseLong(uid));
                    } catch (NumberFormatException e) {
                        SyncLogger.getInstance().log(e, "NotificationHandler.findEntityByUid.ligneVente");
                        return null;
                    }
                }
                case TRAISORERIE -> {
                    return TraisorerieDelegate.findTraisorerie(uid);
                }
                case DEPENSE -> {
                    return DepenseDelegate.findDepense(uid);
                }
                case OPERATION -> {
                    return OperationDelegate.findOperation(uid);
                }
                case COMPTER -> {
                    return CompterDelegate.findCompter(uid);
                }
                case INVENTORY -> {
                    return InventaireDelegate.findInventaire(uid);
                }
                case PRESENCE -> {
                    return PresenceDelegate.findPresence(uid);
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.findEntityByUid");
            return null;
        }
    }

    private static LocalDateTime readUpdatedAt(Object entity) {
        try {
            Method m = entity.getClass().getMethod("getUpdatedAt");
            Object res = m.invoke(entity);
            if (res instanceof LocalDateTime) {
                return (LocalDateTime) res;
            }
        } catch (Exception ignored) {
            SyncLogger.getInstance().log(ignored, "NotificationHandler.readUpdatedAt");
        }
        return null;
    }

    private static String readUid(Object entity) {
        try {
            Method m = entity.getClass().getMethod("getUid");
            Object res = m.invoke(entity);
            return res == null ? null : res.toString();
        } catch (Exception ignored) {
            SyncLogger.getInstance().log(ignored, "NotificationHandler.readUid");
        }
        return null;
    }

    /**
     * Lit le champ "uid" d'un message SSE JSON. Sert au diagnostic de
     * dependance manquante : l'id d'evenement SSE est l'uid d'entreprise,
     * or on veut l'uid de l'entite (PRODUIT/MESURE/...) pour connaitre ce qui
     * manque vraiment. Retourne null si le message ne porte pas de "uid".
     */
    private static String extractUidFromMessage(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    data.core.KazisafeServiceFactory.mapper().readTree(json);
            com.fasterxml.jackson.databind.JsonNode uid = root.get("uid");
            if (uid != null && !uid.isNull() && uid.isValueNode()) {
                return uid.asText();
            }
        } catch (Exception ignored) {
            // JSON invalide : on n'affichera pas d'uid, fallback sur l'id SSE.
        }
        return null;
    }

    private static Tables tableOf(Object entity) {
        if (entity instanceof Category) {
            return Tables.CATEGORY;
        }
        if (entity instanceof Produit) {
            return Tables.PRODUIT;
        }
        if (entity instanceof Mesure) {
            return Tables.MESURE;
        }
        if (entity instanceof Fournisseur) {
            return Tables.FOURNISSEUR;
        }
        if (entity instanceof Livraison) {
            return Tables.LIVRAISON;
        }
        if (entity instanceof Stocker) {
            return Tables.STOCKER;
        }
        if (entity instanceof Destocker) {
            return Tables.DESTOCKER;
        }
        if (entity instanceof Recquisition) {
            return Tables.RECQUISITION;
        }
        if (entity instanceof PrixDeVente) {
            return Tables.PRIXDEVENTE;
        }
        if (entity instanceof Client) {
            return Tables.CLIENT;
        }
        if (entity instanceof CompteTresor) {
            return Tables.COMPTETRESOR;
        }
        if (entity instanceof Vente) {
            return Tables.VENTE;
        }
        if (entity instanceof Traisorerie) {
            return Tables.TRAISORERIE;
        }
        if (entity instanceof Depense) {
            return Tables.DEPENSE;
        }
        if (entity instanceof Operation) {
            return Tables.OPERATION;
        }
        if (entity instanceof Compter) {
            return Tables.COMPTER;
        }
        if (entity instanceof Inventaire) {
            return Tables.INVENTORY;
        }
        if (entity instanceof Presence) {
            return Tables.PRESENCE;
        }
        return null;
    }

    private static boolean isCompleteDependency(Object dependency) {
        if (dependency == null) {
            return false;
        }
        if (readUpdatedAt(dependency) != null) {
            return true;
        }
        try {
            for (Method m : dependency.getClass().getMethods()) {
                String name = m.getName();
                if (m.getParameterCount() != 0 || !name.startsWith("get")) {
                    continue;
                }
                if ("getUid".equals(name)
                        || "getType".equals(name)
                        || "getAction".equals(name)
                        || "getClass".equals(name)
                        || "getCount".equals(name)
                        || "getCounter".equals(name)
                        || "getPayload".equals(name)
                        || "getFrom".equals(name)
                        || "getPriority".equals(name)) {
                    continue;
                }
                Object value = m.invoke(dependency);
                if (value == null) {
                    continue;
                }
                if (value instanceof String s && s.isBlank()) {
                    continue;
                }
                if (value instanceof Number n && n.doubleValue() == 0.0) {
                    continue;
                }
                return true;
            }
        } catch (Exception ignored) {
            SyncLogger.getInstance().log(ignored, "NotificationHandler.isCompleteDependency");
        }
        return false;
    }

    private static boolean isExists(Tables t, String uid) {
        try {
            return switch (t) {
                case CATEGORY -> CategoryDelegate.isExists(uid);
                case PRODUIT -> ProduitDelegate.isExists(uid);
                case MESURE -> MesureDelegate.isExists(uid);
                case FOURNISSEUR -> FournisseurDelegate.isExists(uid);
                case LIVRAISON -> LivraisonDelegate.isExists(uid);
                case STOCKER -> StockerDelegate.isExists(uid);
                case DESTOCKER -> DestockerDelegate.isExists(uid);
                case RECQUISITION -> RecquisitionDelegate.isExists(uid);
                case PRIXDEVENTE -> PrixDeVenteDelegate.isExists(uid);
                case CLIENT -> ClientDelegate.isExists(uid);
                case COMPTETRESOR -> CompteTresorDelegate.isExists(uid);
                case VENTE -> VenteDelegate.isExists(Integer.parseInt(uid));
                case LIGNEVENTE -> LigneVenteDelegate.isExists(Long.parseLong(uid));
                case TRAISORERIE -> TraisorerieDelegate.isExists(uid);
                case DEPENSE -> DepenseDelegate.isExists(uid);
                case OPERATION -> OperationDelegate.isExists(uid);
                case COMPTER -> CompterDelegate.isExists(uid);
                case INVENTORY -> InventaireDelegate.isExists(uid);
                case PRESENCE -> PresenceDelegate.isExists(uid);
                default -> false;
            };
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.isExists");
            return false;
        }
    }

    private BaseModel upsertEntity(Tables t, BaseModel entity) {
        try {
            switch (t) {
                case CATEGORY -> {
                    Category c = (Category) entity;
                    if (CategoryDelegate.isExists(c.getUid())) {
                        Category cat = CategoryDelegate.findCategory(c.getUid());
                        if (cat == null) {
                            return null;
                        }
                        cat.setDescritption(c.getDescritption());
                        cat.setUpdatedAt(c.getUpdatedAt());
                        cat.setDeletedAt(c.getDeletedAt());
                        return CategoryDelegate.updateCategory(cat);
                    }
                    return CategoryDelegate.saveCategory(c);
                }
                case PRODUIT -> {
                    Produit p = (Produit) entity;
                    if (refUid(p.getCategoryId()) == null) {
                        p.setCategoryId(findOrCreateDiversCategory());
                    }
                    if (ProduitDelegate.isExists(p.getUid())) {
                        return ProduitDelegate.updateProduit(p);
                    }
                    return ProduitDelegate.saveProduit(p);
                }
                case MESURE -> {
                    Mesure m = (Mesure) entity;
                    if (MesureDelegate.isExists(m.getUid())) {
                        return MesureDelegate.updateMesure(m);
                    }
                    return MesureDelegate.saveMesure(m);
                }
                case FOURNISSEUR -> {
                    return FournisseurDelegate.syncFournisseurSafe((Fournisseur) entity);
                }
                case LIVRAISON -> {
                    Livraison l = (Livraison) entity;
                    if (LivraisonDelegate.isExists(l.getUid())) {
                        return LivraisonDelegate.updateLivraison(l);
                    }
                    return LivraisonDelegate.saveLivraison(l);
                }
                case STOCKER -> {
                    Stocker s = (Stocker) entity;
                    if (StockerDelegate.isExists(s.getUid())) {
                        return StockerDelegate.updateStocker(s);
                    }
                    return StockerDelegate.saveStocker(s);
                }
                case DESTOCKER -> {
                    Destocker d = (Destocker) entity;
                    if (DestockerDelegate.isExists(d.getUid())) {
                        return DestockerDelegate.updateDestocker(d);
                    }
                    return DestockerDelegate.saveDestocker(d);
                }
                case RECQUISITION -> {
                    Recquisition r = (Recquisition) entity;
                    if (RecquisitionDelegate.isExists(r.getUid())) {
                        return RecquisitionDelegate.updateRecquisition(r);
                    }
                    return RecquisitionDelegate.saveRecquisition(r);
                }
                case PRIXDEVENTE -> {
                    PrixDeVente price = (PrixDeVente) entity;
                    if (refUid(price.getRecquisitionId()) != null) {
                        price.setRecquisitionId(
                            RecquisitionDelegate.findRecquisition(
                                refUid(price.getRecquisitionId())
                            )
                        );
                    }
                    if (PrixDeVenteDelegate.isExists(price.getUid())) {
                        return PrixDeVenteDelegate.updatePrixDeVente(price);
                    }
                    return PrixDeVenteDelegate.savePrixDeVente(price);
                }
                case CLIENT -> {
                    return ClientDelegate.syncClientSafe((Client) entity);
                }
                case COMPTETRESOR -> {
                    CompteTresor ct = (CompteTresor) entity;
                    if (CompteTresorDelegate.isExists(ct.getUid())) {
                        return CompteTresorDelegate.updateCompteTresor(ct);
                    }
                    return CompteTresorDelegate.saveCompteTresor(ct);
                }
                case VENTE -> {
                    Vente v = (Vente) entity;
                    if (VenteDelegate.isExists(v.getUid())) {
                        return VenteDelegate.updateVente(v);
                    }
                    return VenteDelegate.saveVente(v);
                }
                case TRAISORERIE -> {
                    Traisorerie tr = (Traisorerie) entity;
                    if (TraisorerieDelegate.isExists(tr.getUid())) {
                        return TraisorerieDelegate.updateTraisorerie(tr);
                    }
                    return TraisorerieDelegate.saveTraisorerie(tr);
                }
                case DEPENSE -> {
                    Depense dep = (Depense) entity;
                    if (DepenseDelegate.isExists(dep.getUid())) {
                        return DepenseDelegate.updateDepense(dep);
                    }
                    return DepenseDelegate.saveDepense(dep);
                }
                case OPERATION -> {
                    Operation op = (Operation) entity;
                    if (OperationDelegate.isExists(op.getUid())) {
                        return OperationDelegate.updateOperation(op);
                    }
                    return OperationDelegate.saveOperation(op);
                }
                case COMPTER -> {
                    Compter cp = (Compter) entity;
                    boolean isSynced = CompterDelegate.isExists(cp.getUid());
                    if (cp.getDeletedAt() != null) {
                        if (isSynced) {
                            CompterDelegate.deleteCompter(cp);
                        }
                        return cp;
                    }
                    if (isSynced) {
                        return CompterDelegate.updateCompter(cp);
                    }
                    return CompterDelegate.createCompter(cp);
                }
                case INVENTORY -> {
                    Inventaire inv = (Inventaire) entity;
                    if (InventaireDelegate.isExists(inv.getUid())) {
                        return InventaireDelegate.updateInventaire(inv);
                    }
                    return InventaireDelegate.createInventaire(inv);
                }
                case PRESENCE -> {
                    Presence pr = (Presence) entity;
                    if (PresenceDelegate.isExists(pr.getUid())) {
                        return PresenceDelegate.updatePresence(pr);
                    }
                    return PresenceDelegate.savePresence(pr);
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.upsertEntity");
            return null;
        }
    }

    private boolean ensureDependency(Object dependency) {
        if (dependency == null) {
            return false;
        }
        String uid = refUid(dependency);
        if (uid == null || uid.isBlank()) {
            return false;
        }
        // Un uid qui est celui du tenant local n'est pas une entite : ne le
        // verifions jamais en base ni comme dependance manquante.
        if (isEnterpriseUid(uid)) {
            return true;
        }
        Tables t = tableOf(dependency);
        if (t == null) {
            return false;
        }
        if (!isCompleteDependency(dependency)) {
            return isExists(t, uid);
        }
        if (isStaleNetworkUpdate((BaseModel) dependency, t)) {
            return isExists(t, uid);
        }
        return upsertEntity(t, (BaseModel) dependency) != null;
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
        // Le "id" transmis par le SSE est l'id d'evenement (uid d'entreprise),
        // pas l'uid de l'entite manquante. On affiche donc le vrai uid de
        // l'entite (champ "uid" du message) pour un diagnostic fiable.
        String entityUid = extractUidFromMessage(json);
        String missingLabel = (type != null ? type : "entite") + " "
                + (entityUid != null ? entityUid : id);
        System.out.println(
            "SSE downsync: dependance manquante (" +
                missingLabel +
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
            missingLabel
        );
        retryExecutor.schedule(
            () -> processMessage(json, id, region, attempt),
            delaySeconds,
            java.util.concurrent.TimeUnit.SECONDS
        );
    }

    private Kazisafe getKazisafeClient() {
        if (kazisafeClient == null) {
            synchronized (NotificationHandler.class) {
                if (kazisafeClient == null) {
                    String token = pref != null ? pref.get("token", null) : null;
                    kazisafeClient = KazisafeServiceFactory.createService(token);
                }
            }
        }
        return kazisafeClient;
    }

    /**
     * Full downsync auto-resolution : selon le type de l'objet dont une
     * dependance manque, recupere le PRODUIT manquant (avec image + mesures)
     * ou la MESURE manquante via {@code x-sync/full}. Si l'element appartient
     * au tenant courant, on le persiste et on retourne true (l'appelant rejoue
     * la requete). Si absent du tenant (404), on pilote la copie cross-tenant
     * via {@code x-sync/dto} (avec migration ProductUidMigrator). Absent
     * partout -> false (l'appelant garde le retry simple + logs).
     */
    private boolean tryResolveMissingDependency(BaseModel obj) {
        try {
            if (obj instanceof LigneVente lv) {
                boolean ok = false;
                if (lv.getProductId() != null && !ProduitDelegate.isExists(lv.getProductId().getUid())) {
                    ok = tryFullDownsyncProduit(lv.getProductId().getUid());
                    if (!ok) {
                        // Secours : le full downsync de la copie echoue (404 sur
                        // x-sync/full/{uid} cote serveur). On clone localement
                        // l'ANCIEN produit encore present dans le tenant vers le
                        // NOUVEL uid fourni par le payload, sans dependre du serveur.
                        ok = tryLocalCloneLigne(lv);
                    }
                    if (ok) {
                        // Full downsync du NOUVEL objet (produit copie cross-tenant
                        // par le serveur sous un nouvel uid). Le payload de la ligne
                        // porte les ANCIENS uids (replacement + measureUids) : on
                        // re-pointe les FK locales vers les nouveaux uids avant rejeu.
                        migrateForeignKeysFromPayload(lv.getPayload(), lv.getProductId().getUid(),
                                newMesureUidFromPayload(lv));
                    }
                } else if (lv.getMesureId() != null && !MesureDelegate.isExists(lv.getMesureId().getUid())) {
                    // La ligne porte l'ANCIENNE mesure (uid d'un autre tenant) : son
                    // payload la mappe vers la NOUVELLE mesure de la copie cross-tenant.
                    // On ne doit pas chercher l'ancien uid (il n'existera jamais dans ce
                    // tenant) mais la nouvelle mesure locale ; en dernier recours on
                    // rafraichit le produit complet qui porte la nouvelle mesure.
                    String mapped = mappedNewMesureUid(lv.getPayload(), lv.getMesureId().getUid());
                    if (mapped != null) {
                        if (MesureDelegate.isExists(mapped)) {
                            ok = true;
                        } else {
                            String prodUid = lv.getProductId() != null ? lv.getProductId().getUid() : null;
                            if (prodUid != null) {
                                tryFullDownsyncProduit(prodUid);
                            }
                            ok = MesureDelegate.isExists(mapped);
                            if (ok) {
                                migrateForeignKeysFromPayload(lv.getPayload(), null, mapped);
                            }
                        }
                    } else {
                        ok = tryFullDownsyncMesure(lv.getMesureId().getUid(), lv.getMesureId().getProduitId() != null
                                ? lv.getMesureId().getProduitId().getUid() : null);
                        if (ok) {
                            // Mesure creee par copie cross-tenant : re-pointe les FK
                            // locales de l'ancienne mesure vers la nouvelle via le payload.
                            migrateForeignKeysFromPayload(lv.getPayload(), null, newMesureUidFromPayload(lv));
                        }
                    }
                } else if (lv.getReference() != null && lv.getReference().getUid() != null
                        && !VenteDelegate.isExists(lv.getReference().getUid())) {
                    // Vente (reference) de la ligne passee en stub : on downsync
                    // son objet complet depuis le backend.
                    ok = tryFullDownsyncVente(lv.getReference().getUid());
                }
                return ok;
            }
            if (obj instanceof Mesure m) {
                Produit mesProd = m.getProduitId();
                String prodUid = mesProd != null ? mesProd.getUid() : null;
                if (prodUid != null && !ProduitDelegate.isExists(prodUid)) {
                    // Parent produit manquant : essayer de le recuperer en entier.
                    return tryFullDownsyncProduit(prodUid);
                }
                if (!MesureDelegate.isExists(m.getUid())) {
                    // La mesure manque localement : la recuperer en entier.
                    return tryFullDownsyncMesure(m.getUid(), prodUid);
                }
                // La mesure existe deja localement et (le cas echeant) son parent
                // aussi : la dependance est satisfaite. Retourner true pour que
                // l'appelant reprenne la persistance au lieu de re-planifier un
                // retry inutile.
                return prodUid == null || ProduitDelegate.isExists(prodUid);
            }
            if (obj instanceof Produit p) {
                if (!ProduitDelegate.isExists(p.getUid())) {
                    return tryFullDownsyncProduit(p.getUid());
                }
                if (p.getCategoryId() != null && !CategoryDelegate.isExists(p.getCategoryId().getUid())) {
                    return tryFullDownsyncProduit(p.getUid());
                }
                // Produit present localement (et sa categorie si referencee) :
                // dependance satisfaite, on laisse l'appelant reprendre la
                // persistance.
                return true;
            }
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.tryResolveMissingDependency");
            return false;
        }
    }

    private boolean tryFullDownsyncProduit(String uid) {
        if (uid == null || uid.isBlank()) {
            return false;
        }
        try {
            Response<Produit> full = getKazisafeClient().syncProduitFull(uid).execute();
            // 200 avec corps = downsync reussi d'un produit du tenant, meme si
            // nomProduit est null (donnees minimales). Ne pas rabattre sur une
            // copie cross-tenant pour autant.
            if (full.isSuccessful() && full.body() != null) {
                persistFullProduit(full.body());
                SyncLogger.getInstance().logMessage("NotificationHandler.tryFullDownsyncProduit",
                        "full downsync PRODUIT ok uid=" + uid);
                return true;
            }
            SyncLogger.getInstance().logMessage("NotificationHandler.tryFullDownsyncProduit",
                    "full downsync PRODUIT absent/inaccessible uid=" + uid + " http=" + full.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.tryFullDownsyncProduit");
            return false;
        }
    }

    private boolean tryFullDownsyncMesure(String uid, String prodUid) {
        return tryFullDownsyncMesureObject(uid, prodUid) != null;
    }

    /**
     * Version retournant la {@link Mesure} telechargee (persistee) si le full
     * downsync reussit, sinon {@code null}. {@code tryFullDownsyncMesure} n'en
     * est qu'une facade booleenne.
     */
    private Mesure tryFullDownsyncMesureObject(String uid, String prodUid) {
        if (uid == null || uid.isBlank()) {
            return null;
        }
        try {
            Response<Mesure> full = getKazisafeClient().syncMesureFull(uid).execute();
            // Un 200 avec corps est un downsync reussi d'une mesure existante du
            // tenant, MEME si sa description est null (cas legitime : une unite
            // peut ne pas avoir de description). On ne doit pas la rabattre sur
            // une copie cross-tenant pour autant.
            if (full.isSuccessful() && full.body() != null) {
                persistFullMesure(full.body());
                SyncLogger.getInstance().logMessage("NotificationHandler.tryFullDownsyncMesure",
                        "full downsync MESURE ok uid=" + uid);
                return full.body();
            }
            SyncLogger.getInstance().logMessage("NotificationHandler.tryFullDownsyncMesure",
                    "full downsync MESURE absent/inaccessible uid=" + uid + " http=" + full.code());
            return null;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.tryFullDownsyncMesure");
            return null;
        }
    }

    /**
     * Retrouve dans le payload cross-tenant l'uid de la NOUVELLE mesure creee
     * par le serveur pour l'ancienne mesure portee par la ligne
     * ({@code measureUids}).
     */
    private String mappedNewMesureUid(String payload, String oldMesureUid) {
        if (payload == null || payload.isBlank() || oldMesureUid == null || oldMesureUid.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root = KazisafeServiceFactory.mapper().readTree(payload);
            com.fasterxml.jackson.databind.JsonNode mu = root.path("measureUids");
            if (mu.isObject()) {
                com.fasterxml.jackson.databind.JsonNode v = mu.get(oldMesureUid);
                if (v != null && v.isTextual() && !v.asText().isBlank()) {
                    return v.asText();
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.mappedNewMesureUid");
        }
        return null;
    }

    /**
     * Nouvelle mesure cible de la ligne : l'uid mappe par le payload s'il
     * existe (copie cross-tenant), sinon l'uid porte par la ligne.
     */
    private String newMesureUidFromPayload(LigneVente lv) {
        if (lv.getMesureId() == null || lv.getMesureId().getUid() == null) {
            return null;
        }
        String mapped = mappedNewMesureUid(lv.getPayload(), lv.getMesureId().getUid());
        return mapped != null ? mapped : lv.getMesureId().getUid();
    }

    /**
     * Retrouve l'ANCIEN uid de mesure dont la copie cross-tenant porte le
     * {@code targetUid} (inverse de {@code measureUids}). Sert a repartir d'une
     * source locale presente pour completer la description d'une mesure cible
     * dont la description est absente (clone local prioritaire au backend).
     */
    private String oldMesureSourceUid(String payload, String targetUid) {
        if (payload == null || payload.isBlank() || targetUid == null || targetUid.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    data.core.KazisafeServiceFactory.mapper().readTree(payload);
            com.fasterxml.jackson.databind.JsonNode mu = root.path("measureUids");
            if (mu.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> it = mu.fields();
                while (it.hasNext()) {
                    java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> en = it.next();
                    String v = en.getValue().asText();
                    if (v != null && !v.isBlank() && v.equals(targetUid)) {
                        return en.getKey();
                    }
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.oldMesureSourceUid");
        }
        return null;
    }

    /**
     * Garantit qu'une LigneVente portee par le SSE reference une mesure locale
     * avec une description exploitable. Priorite a la version locale :
     *  - la mesure referencee existe et a deja une description -> rien a faire ;
     *  - la mesure referencee existe mais sa description est null -> on clone la
     *    description depuis l'ANCIENNE mesure du payload ({@code measureUids}) si
     *    celle-ci existe localement avec une description (exactement comme on
     *    clone le produit). Recours au backend uniquement si la description est
     *    aussi absente cote source locale.
     */
    private boolean ensureMesureDescriptionLocal(LigneVente lv) {
        if (lv == null || lv.getMesureId() == null) {
            return true;
        }
        String mesUid = lv.getMesureId().getUid();
        if (mesUid == null || mesUid.isBlank()) {
            return true;
        }
        try {
            Mesure local = MesureDelegate.findMesure(mesUid);
            if (local != null && local.getDescription() != null && !local.getDescription().isBlank()) {
                return true;
            }
            // Description absente localement sur la cible : chercher l'ancienne
            // mesure du payload et cloner sa description si elle existe en local.
            String oldMes = oldMesureSourceUid(lv.getPayload(), mesUid);
            if (oldMes != null && !oldMes.equals(mesUid)) {
                Mesure source = MesureDelegate.findMesure(oldMes);
                if (source != null && source.getDescription() != null && !source.getDescription().isBlank()) {
                    if (local == null) {
                        local = new Mesure(mesUid);
                        Produit refProd = lv.getMesureId().getProduitId();
                        if (refProd != null) {
                            local.setProduitId(refProd);
                        }
                    }
                    local.setDescription(source.getDescription());
                    if (local.getQuantContenu() == null && source.getQuantContenu() != null) {
                        local.setQuantContenu(source.getQuantContenu());
                    }
                    if (MesureDelegate.isExists(mesUid)) {
                        MesureDelegate.updateMesure(local);
                    } else {
                        MesureDelegate.saveMesure(local);
                    }
                    SyncLogger.getInstance().logMessage("NotificationHandler.ensureMesureDescriptionLocal",
                            "clone description " + oldMes + " -> " + mesUid);
                    return true;
                }
            }
            // Dernier recours : la description est absente aussi cote source
            // locale. On ne fait alors appel au backend que dans ce cas.
            Mesure fetched = tryFullDownsyncMesureObject(mesUid, lv.getMesureId().getProduitId() != null
                    ? lv.getMesureId().getProduitId().getUid() : null);
            if (fetched != null && fetched.getDescription() != null && !fetched.getDescription().isBlank()) {
                Mesure persisted = MesureDelegate.findMesure(mesUid);
                if (persisted != null) {
                    lv.setMesureId(persisted);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.ensureMesureDescriptionLocal");
            return false;
        }
    }

    /**
     * Re-point les FK produit/mesure d'une LigneVente sur les uids cibles de la
     * copie cross-tenant (replacement/measureUids) quand ceux-ci existent
     * localement. A appeler AVANT le controle de dependance : la ligne diffusee
     * peut porter l'ANCIEN uid de mesure (et le nouveau produit) ; une fois les
     * copies materialisees localement, on persiste avec les NOUVEAUX uids.
     */
    private void remapCrossTenantLigne(LigneVente lv) {
        if (lv == null || lv.getPayload() == null || lv.getPayload().isBlank()) {
            return;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    data.core.KazisafeServiceFactory.mapper().readTree(lv.getPayload());
            if (lv.getProductId() != null && lv.getProductId().getUid() != null) {
                com.fasterxml.jackson.databind.JsonNode repl = root.path("replacement");
                if (repl.isObject()) {
                    com.fasterxml.jackson.databind.JsonNode target = repl.get(lv.getProductId().getUid());
                    if (target != null && target.isTextual() && !target.asText().isBlank()
                            && ProduitDelegate.isExists(target.asText())) {
                        Produit newProd = ProduitDelegate.findProduit(target.asText());
                        if (newProd != null) {
                            lv.setProductId(newProd);
                        }
                    }
                }
            }
            if (lv.getMesureId() != null && lv.getMesureId().getUid() != null) {
                com.fasterxml.jackson.databind.JsonNode mu = root.path("measureUids");
                if (mu.isObject()) {
                    com.fasterxml.jackson.databind.JsonNode target = mu.get(lv.getMesureId().getUid());
                    if (target != null && target.isTextual() && !target.asText().isBlank()
                            && MesureDelegate.isExists(target.asText())) {
                        Mesure newMes = MesureDelegate.findMesure(target.asText());
                        if (newMes != null) {
                            lv.setMesureId(newMes);
                        }
                    }
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.remapCrossTenantLigne");
        }
    }

    /**
     * Secours quand le full downsync de la copie cross-tenant echoue (404 sur
     * x-sync/full/{uid}) : clone localement l'ANCIEN produit encore present dans
     * le tenant (et ses mesures) vers les NOUVEAUX uids fournis par le payload
     * (replacement + measureUids), pour materialiser la copie cotE terminal sans
     * dependre du serveur. Le ProductUidMigrator fera ensuite le re-point + la
     * suppression des anciens. Ne fait rien si la copie existe deja.
     */
    private boolean tryLocalCloneLigne(LigneVente lv) {
        if (lv == null || lv.getPayload() == null || lv.getPayload().isBlank()) {
            return false;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    data.core.KazisafeServiceFactory.mapper().readTree(lv.getPayload());
            java.util.Map<String, String> measureUids = new java.util.HashMap<>();
            com.fasterxml.jackson.databind.JsonNode mu = root.path("measureUids");
            if (mu.isObject()) {
                mu.fields().forEachRemaining(en -> {
                    String v = en.getValue().asText();
                    if (v != null && !v.isBlank()) {
                        measureUids.put(en.getKey(), v);
                    }
                });
            }
            String newProdUid = lv.getProductId() != null ? lv.getProductId().getUid() : null;
            if (newProdUid == null || newProdUid.isBlank()) {
                return false;
            }
            if (ProduitDelegate.isExists(newProdUid)) {
                return true;
            }
            String oldProdUid = null;
            com.fasterxml.jackson.databind.JsonNode repl = root.path("replacement");
            if (repl.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> it = repl.fields();
                while (it.hasNext()) {
                    java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> en = it.next();
                    if (newProdUid.equals(en.getValue().asText())
                            || (lv.getProductId() != null
                                    && en.getKey().equals(lv.getProductId().getUid()))) {
                        oldProdUid = en.getKey();
                        break;
                    }
                }
            }
            if (oldProdUid == null || oldProdUid.isBlank()) {
                return false;
            }
            Produit origin = ProduitDelegate.findProduit(oldProdUid);
            if (origin == null) {
                SyncLogger.getInstance().logMessage("NotificationHandler.tryLocalCloneLigne",
                        "origine introuvable " + oldProdUid + " pour copier vers " + newProdUid);
                return false;
            }
            Produit copy = new Produit(newProdUid);
            copy.setCodebar(origin.getCodebar());
            copy.setCouleur(origin.getCouleur());
            copy.setDateCreation(origin.getDateCreation());
            copy.setImage(origin.getImage());
            copy.setMarque(origin.getMarque());
            copy.setMethodeInventaire(origin.getMethodeInventaire() != null
                    ? origin.getMethodeInventaire() : "ppps");
            copy.setModele(origin.getModele());
            copy.setNomProduit(origin.getNomProduit());
            copy.setTaille(origin.getTaille());
            copy.setUpdatedAt(origin.getUpdatedAt());
            if (origin.getCategoryId() != null && origin.getCategoryId().getUid() != null
                    && CategoryDelegate.isExists(origin.getCategoryId().getUid())) {
                copy.setCategoryId(origin.getCategoryId());
            }
            ProduitDelegate.saveProduit(copy);
            for (java.util.Map.Entry<String, String> e : measureUids.entrySet()) {
                String oldMes = e.getKey();
                String newMes = e.getValue();
                if (oldMes == null || oldMes.isBlank() || newMes == null || newMes.isBlank()) {
                    continue;
                }
                Mesure om = MesureDelegate.findMesure(oldMes);
                if (om == null) {
                    continue;
                }
                Mesure existing = MesureDelegate.findMesure(newMes);
                if (existing != null) {
                    // La copie existe deja localement. Si elle manque de description
                    // alors que la source en a une, on la complete (clone de la
                    // description, comme pour le produit). La priorite est a la
                    // version locale : on ne va chercher le backend que si la source
                    // locale n'a pas non plus de description.
                    String existingDesc = existing.getDescription();
                    String originDesc = om.getDescription();
                    if ((originDesc == null || originDesc.isBlank()) && existingDesc != null && !existingDesc.isBlank()) {
                        continue;
                    }
                    if (originDesc != null && !originDesc.isBlank()) {
                        existing.setDescription(originDesc);
                    }
                    if (existing.getQuantContenu() == null && om.getQuantContenu() != null) {
                        existing.setQuantContenu(om.getQuantContenu());
                    }
                    MesureDelegate.updateMesure(existing);
                    continue;
                }
                Mesure mc = new Mesure(newMes);
                mc.setDescription(om.getDescription());
                mc.setQuantContenu(om.getQuantContenu() != null ? om.getQuantContenu() : 0.0);
                mc.setProduitId(copy);
                mc.setUpdatedAt(om.getUpdatedAt());
                MesureDelegate.saveMesure(mc);
            }
            SyncLogger.getInstance().logMessage("NotificationHandler.tryLocalCloneLigne",
                    "clone local " + oldProdUid + " -> " + newProdUid + " mesures=" + measureUids.size());
            return true;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.tryLocalCloneLigne");
            return false;
        }
    }

    /**
     * Lit le payload d'une LigneVente diffusee apres une copie cross-tenant
     * (format : {@code {"replacement":{ancienProduit:nouveauProduit},
     * "measureUids":{ancienneMesure:nouvelleMesure}}}). Re-pointe les FK
     * locales de l'ANCIEN produit/mesure vers le NOUVEAU via
     * {@code ProductUidMigrator} pour que le rejeu de la ligne persiste sans
     * dependance manquante. Les nouvelles entrees (produit copie et mesures)
     * ont deja ete persistees par le full downsync du nouvel objet.
     */
    private void migrateForeignKeysFromPayload(String payload, String newProductUid, String newMesureUid) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root = KazisafeServiceFactory.mapper().readTree(payload);
            java.util.Map<String, String> measureUids = new java.util.HashMap<>();
            com.fasterxml.jackson.databind.JsonNode mu = root.path("measureUids");
            if (mu.isObject()) {
                mu.fields().forEachRemaining(en -> {
                    String v = en.getValue().asText();
                    if (v != null && !v.isBlank()) {
                        measureUids.put(en.getKey(), v);
                    }
                });
            }
            com.fasterxml.jackson.databind.JsonNode repl = root.path("replacement");
            if (repl.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> it = repl.fields();
                while (it.hasNext()) {
                    java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> en = it.next();
                    String oldProd = en.getKey();
                    String newProd = en.getValue().asText();
                    if (oldProd == null || oldProd.isBlank() || newProd == null || newProd.isBlank()) {
                        continue;
                    }
                    tools.sync.ProductUidMigrator.migrate(oldProd, newProd, measureUids);
                }
            }
            if (newMesureUid != null && !newMesureUid.isBlank()) {
                for (java.util.Map.Entry<String, String> e : measureUids.entrySet()) {
                    if (newMesureUid.equals(e.getValue())) {
                        tools.sync.ProductUidMigrator.migrateMesure(e.getKey(), e.getValue());
                    }
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.migrateForeignKeysFromPayload");
        }
    }

    /**
     * Downsync le VENTE complet depuis le backend (voir {@code ventes/{uid}/show}).
     * Utilise quand une LigneVente reference une vente passee uniquement en stub.
     */
    private boolean tryFullDownsyncVente(Integer uid) {
        if (uid == null) {
            return false;
        }
        try {
            Response<Vente> full = getKazisafeClient().showVente(String.valueOf(uid)).execute();
            if (full.isSuccessful() && full.body() != null) {
                persistFullVente(full.body());
                SyncLogger.getInstance().logMessage("NotificationHandler.tryFullDownsyncVente",
                        "full downsync VENTE ok uid=" + uid + " ref=" + full.body().getReference());
                return true;
            }
            SyncLogger.getInstance().logMessage("NotificationHandler.tryFullDownsyncVente",
                    "full downsync VENTE absent/inaccessible uid=" + uid + " http=" + full.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.tryFullDownsyncVente");
            return false;
        }
    }

    private void persistFullVente(Vente v) {
        try {
            Client cli = v != null ? v.getClientId() : null;
            if (cli != null && cli.getUid() != null && !ClientDelegate.isExists(cli.getUid())) {
                ClientDelegate.syncClientSafe(cli);
            }
            if (v == null) {
                return;
            }
            if (VenteDelegate.isExists(v.getUid())) {
                VenteDelegate.updateVente(v);
            } else {
                VenteDelegate.saveVente(v);
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.persistFullVente");
        }
    }

    private void persistFullProduit(Produit p) {
        try {
            if (p.getCategoryId() == null || p.getCategoryId().getUid() == null) {
                p.setCategoryId(findOrCreateDiversCategory());
            } else if (!CategoryDelegate.isExists(p.getCategoryId().getUid())) {
                Category cat = CategoryDelegate.findCategory(p.getCategoryId().getUid());
                if (cat == null) {
                    Category createdCat = new Category(p.getCategoryId().getUid(), "Divers");
                    CategoryDelegate.saveCategory(createdCat);
                }
            }
            if (ProduitDelegate.isExists(p.getUid())) {
                ProduitDelegate.updateProduit(p);
            } else {
                ProduitDelegate.saveProduit(p);
            }
            List<Mesure> mesures = p.getMesureList();
            if (mesures != null) {
                for (Mesure m : mesures) {
                    if (m == null || m.getUid() == null) {
                        continue;
                    }
                    m.setProduitId(p);
                    if (MesureDelegate.isExists(m.getUid())) {
                        MesureDelegate.updateMesure(m);
                    } else {
                        MesureDelegate.saveMesure(m);
                    }
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.persistFullProduit");
        }
    }

    private void persistFullMesure(Mesure m) {
        try {
            Produit prod = m.getProduitId();
            if (prod != null && prod.getUid() != null && !ProduitDelegate.isExists(prod.getUid())
                    && prod.getNomProduit() != null) {
                persistFullProduit(prod);
            }
            if (MesureDelegate.isExists(m.getUid())) {
                MesureDelegate.updateMesure(m);
            } else {
                MesureDelegate.saveMesure(m);
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.persistFullMesure");
        }
    }

    private boolean triggerCrossTenantCopyProduit(Produit origin) {
        try {
            Response<Produit> rep = getKazisafeClient()
                    .syncProduitDto(SyncMappers.toProduitDto(origin))
                    .execute();
            if (!rep.isSuccessful() || rep.body() == null) {
                SyncLogger.getInstance().logMessage("NotificationHandler.triggerCrossTenantCopyProduit",
                        "copie PRODUIT rejetee uid=" + origin.getUid() + " http=" + rep.code());
                return false;
            }
            Produit returned = rep.body();
            String oldUid = origin.getUid();
            String newUid = returned.getUid();
            if (newUid == null || newUid.equals(oldUid)) {
                return true;
            }
            if (ProduitDelegate.findProduit(newUid) == null && returned.getNomProduit() != null) {
                ProduitDelegate.saveProduit(returned);
            }
            java.util.Map<String, String> measureUids = new java.util.HashMap<>();
            String payload = returned.getPayload();
            if (payload != null && !payload.isBlank()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode root =
                            data.core.KazisafeServiceFactory.mapper().readTree(payload);
                    com.fasterxml.jackson.databind.JsonNode mu = root.path("measureUids");
                    if (mu.isObject()) {
                        mu.fields().forEachRemaining(en -> {
                            String v = en.getValue().asText();
                            if (v != null && !v.isBlank()) {
                                measureUids.put(en.getKey(), v);
                            }
                        });
                    }
                } catch (Exception ignored) {
                    SyncLogger.getInstance().log(ignored, "NotificationHandler.copyProduit.payload");
                }
            }
            tools.sync.ProductUidMigrator.migrate(oldUid, newUid, measureUids);
            SyncLogger.getInstance().logMessage("NotificationHandler.triggerCrossTenantCopyProduit",
                    "copie PRODUIT " + oldUid + " -> " + newUid + " mesures=" + measureUids.size());
            return true;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.triggerCrossTenantCopyProduit");
            return false;
        }
    }

    private boolean triggerCrossTenantCopyMesure(Mesure origin) {
        try {
            Produit prod = origin.getProduitId();
            String prodUid = prod != null ? prod.getUid() : null;
            if (prodUid != null && !ProduitDelegate.isExists(prodUid) && prod != null && prod.getNomProduit() != null) {
                persistFullProduit(prod);
            }
            Response<Mesure> rep = getKazisafeClient()
                    .syncMesureDto(SyncMappers.toMesureDto(origin))
                    .execute();
            if (!rep.isSuccessful() || rep.body() == null) {
                SyncLogger.getInstance().logMessage("NotificationHandler.triggerCrossTenantCopyMesure",
                        "copie MESURE rejetee uid=" + origin.getUid() + " http=" + rep.code());
                return false;
            }
            Mesure returned = rep.body();
            String oldUid = origin.getUid();
            String newUid = returned.getUid();
            String payload = returned.getPayload();
            if (payload != null && !payload.isBlank()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode root =
                            data.core.KazisafeServiceFactory.mapper().readTree(payload);
                    com.fasterxml.jackson.databind.JsonNode repl = root.path("replacement");
                    if (repl.isObject()) {
                        com.fasterxml.jackson.databind.JsonNode nu = repl.get(oldUid);
                        if (nu != null && nu.isTextual() && !nu.asText().isBlank()) {
                            newUid = nu.asText();
                        }
                    }
                } catch (Exception ignored) {
                    SyncLogger.getInstance().log(ignored, "NotificationHandler.triggerCrossTenantCopyMesure.payload");
                }
            }
            if (newUid == null || newUid.isBlank() || newUid.equals(oldUid)) {
                return true;
            }
            tools.sync.ProductUidMigrator.migrateMesure(oldUid, newUid);
            SyncLogger.getInstance().logMessage("NotificationHandler.triggerCrossTenantCopyMesure",
                    "copie MESURE " + oldUid + " -> " + newUid);
            return true;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "NotificationHandler.triggerCrossTenantCopyMesure");
            return false;
        }
    }

    private void executeDownsyncMutation(Runnable action) {
        if (ManagedSessionFactory.isEmbedded()) {
            action.run();
            return;
        }

        ManagedSessionFactory.runInSession(em -> {
            // Un message SSE = un contexte persistant propre : une tentative
            // precedente en erreur (rollback) laisse des entites managed dans
            // l'EntityManager partage ; leur re-flush ulterieur eclaterait en
            // TransientObjectException des le prochain isExists. On les
            // detache avant toute operation (ne touche jamais a entreprise_id
            // : la locataire est portee par la base locale elle-meme).
            em.clear();
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
                SyncLogger.getInstance().log(ex, "NotificationHandler.runInTransaction");
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
                SyncLogger.getInstance().log(ex, "NotificationHandler.beep");
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
                    SyncLogger.getInstance().log(ex, "NotificationHandler.broadcastDataSynced");
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
            SyncLogger.getInstance().log(e, "NotificationHandler.refUid");
            return null;
        }
    }

    /**
     * Un uid qui coincide avec l'uid d'entreprise local (suffixe du nom de la
     * base et evenement SSE de type "region") n'est jamais un identifiant
     * d'entite : il ne faut ni le verifier en base ni le traiter comme une
     * dependance manquante. On passe simplement a l'entite suivante.
     */
    private boolean isEnterpriseUid(String uid) {
        if (uid == null || uid.isBlank()) {
            return false;
        }
        try {
            String localEid = pref.get("eUid", "");
            if (localEid != null && !localEid.isBlank() && uid.equals(localEid)) {
                return true;
            }
        } catch (Exception ignored) {
            // conservateur : on ne bloque pas le flux.
        }
        return false;
    }

    private static String extractJsonString(String json, String key) {
        if (json == null || key == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return null;
        int q1 = json.indexOf('"', colon);
        if (q1 == -1) return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 == -1) return null;
        String val = json.substring(q1 + 1, q2);
        return val.isBlank() ? null : val;
    }

    private void notifySynced(BaseModel uid) {
        broadcastDataSynced(uid);
    }
}
