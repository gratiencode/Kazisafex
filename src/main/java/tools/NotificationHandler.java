/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import data.*;
import delegates.*;
import data.helpers.Role;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author eroot
 */
public class NotificationHandler
        implements EventHandler {

    Preferences pref;
    private static OnDataSyncListener onDataSyncListener;

    @Override
    public void onOpen() throws Exception {
        System.out.println("Encours d'ecoute sur " + System.getProperty("os.name") + " u= "
                + System.getProperty("user.name") + "...");
        pref = Preferences.userNodeForPackage(SyncEngine.class);
    }

    @Override
    public void onClosed() throws Exception {
        System.out.println("close");
    }

    @Override
    public void onMessage(String string, MessageEvent me) throws Exception {
        String json = me.getData();
        String id = me.getLastEventId();
        String region = me.getEventName();
        String eid = pref.get("eUid", "");
        String reg = pref.get("region", "");
        String role = pref.get("priv", null);
        System.out.println("Reception : Ping Connected (v):" + string);
        if (!json.equals("Connected!") && !json.equals("ping")) {
            if (id.equals(eid)) {
                boolean ok = false;
                if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                    ok = true;
                } else {
                    if (reg.equals(region) || region.equals("*")) {
                        ok = true;
                    }
                }
                if (ok) {
                    MainUI.notifySync("Sync", "Un element a ete synchronise", region);
                    BaseModel obj = JsonUtil.toBaseModelObject(json);
                    Tables t = Tables.valueOf(obj.getType());
                    switch (t) {

                        case Tables.PRODUIT -> {
                            Produit product = (Produit) obj;
                            boolean exist = CategoryDelegate.isExists(product.getCategoryId().getUid());
                            if (exist) {
                                boolean isSynced = ProduitDelegate.isExists(product.getUid());
                                Produit result;
                                if (!isSynced) {
                                    result = ProduitDelegate.saveProduit(product);
                                } else {
                                    result = ProduitDelegate.updateProduit(product);
                                }
                                notifySynced(result);
                            }
                        }
                        case Tables.CATEGORY -> {
                            Category c = (Category) obj;
                            boolean isSynced = CategoryDelegate.isExists(c.getUid());
                            Category result;
                            if (!isSynced) {
                                result = CategoryDelegate.saveCategory(c);
                            } else {
                                Category cat = CategoryDelegate.findCategory(c.getUid());
                                cat.setDescritption(c.getDescritption());
                                cat.setUpdatedAt(c.getUpdatedAt());
                                cat.setDeletedAt(c.getDeletedAt());
                                result = CategoryDelegate.updateCategory(cat);
                            }
                            notifySynced(result);
                        }
                        case Tables.MESURE -> {
                            Mesure measure = (Mesure) obj;
                            boolean exists = ProduitDelegate.isExists(measure.getProduitId().getUid());
                            if (exists) {
                                boolean isSynced = MesureDelegate.isExists(measure.getUid());
                                Mesure result;
                                if (!isSynced) {
                                    result = MesureDelegate.saveMesure(measure);
                                } else {
                                    result = MesureDelegate.updateMesure(measure);
                                }
                                notifySynced(result);
                            }

                        }
                        case Tables.FOURNISSEUR -> {
                            Fournisseur supplier = (Fournisseur) obj;
                            boolean isSynced = FournisseurDelegate.isExists(supplier.getUid());
                            Fournisseur result;
                            if (!isSynced) {
                                result = FournisseurDelegate.saveFournisseur(supplier);
                            } else {
                                result = FournisseurDelegate.updateFournisseur(supplier);
                            }
                            notifySynced(result);
                        }
                        case Tables.LIVRAISON -> {
                            Livraison delivery = (Livraison) obj;
                            boolean exists = FournisseurDelegate.isExists(delivery.getFournId().getUid());
                            if (exists) {
                                boolean isSynced = LivraisonDelegate.isExists(delivery.getUid());
                                System.out.println("after livraison exist- ");
                                Livraison result;
                                if (!isSynced) {
                                    result = LivraisonDelegate.saveLivraison(delivery);
                                } else {
                                    result = LivraisonDelegate.updateLivraison(delivery);
                                }
                                notifySynced(result);
                            }
                        }
                        case Tables.STOCKER -> {
                            Stocker stocker = (Stocker) obj;
                            boolean exists = LivraisonDelegate.isExists(stocker.getLivraisId().getUid());
                            boolean exist1 = MesureDelegate.isExists(stocker.getMesureId().getUid());
                            boolean exist2 = ProduitDelegate.isExists(stocker.getProductId().getUid());
                            if (exists && exist1 && exist2) {
                                boolean isSynced = StockerDelegate.isExists(stocker.getUid());
                                Stocker result;
                                if (!isSynced) {
                                    result = StockerDelegate.saveStocker(stocker);
                                } else {
                                    result = StockerDelegate.updateStocker(stocker);
                                }
                                Produit p = ProduitDelegate.findProduit(stocker.getProductId().getUid());
                                StockerDelegate.rectifyStockDepot(p, stocker.getDateStocker().toLocalDate(), stocker.getRegion(), stocker.getCoutAchat());
                                notifySynced(result);
                            }
                        }
                        case Tables.DESTOCKER -> {
                            Destocker destocker = (Destocker) obj;
                            boolean exists = MesureDelegate.isExists(destocker.getMesureId().getUid());
                            boolean exist2 = ProduitDelegate.isExists(destocker.getProductId().getUid());
                            if (exists && exist2) {
                                boolean isSynced = DestockerDelegate.isExists(destocker.getUid());
                                Destocker result;
                                if (!isSynced) {
                                    result = DestockerDelegate.saveDestocker(destocker);
                                } else {
                                    result = DestockerDelegate.updateDestocker(destocker);
                                }
                                Produit p = ProduitDelegate.findProduit(destocker.getProductId().getUid());
                                StockerDelegate.rectifyStockDepot(p, destocker.getDateDestockage().toLocalDate(), destocker.getRegion(), destocker.getCoutAchat());
                                notifySynced(result);
                            }
                        }
                        case Tables.RECQUISITION -> {
                            Recquisition recquisition = (Recquisition) obj;
                            boolean exists = ProduitDelegate.isExists(recquisition.getProductId().getUid());
                            boolean exist1 = MesureDelegate.isExists(recquisition.getMesureId().getUid());
                            if (exists && exist1) {
                                boolean isSynced = RecquisitionDelegate.isExists(recquisition.getUid());

                                Recquisition result;
                                if (!isSynced) {
                                    result = RecquisitionDelegate.saveRecquisition(recquisition);
                                } else {
                                    result = RecquisitionDelegate.updateRecquisition(recquisition);
                                }
                                Mesure m = MesureDelegate.findMesure(recquisition.getMesureId().getUid());
                                Produit p = ProduitDelegate.findProduit(recquisition.getProductId().getUid());
                                double cau = recquisition.getCoutAchat() / m.getQuantContenu();
                                RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), recquisition.getRegion(), cau);
                                notifySynced(result);
                            }
                        }
                        case Tables.PRIXDEVENTE -> {
                            PrixDeVente price = (PrixDeVente) obj;
                            boolean exists = RecquisitionDelegate.isExists(price.getRecquisitionId().getUid());
                            boolean exist1 = MesureDelegate.isExists(price.getMesureId().getUid());
                            if (exists && exist1) {
                                boolean isSynced = PrixDeVenteDelegate.isExists(price.getUid());

                                PrixDeVente result;
                                if (!isSynced) {
                                    price.setRecquisitionId(
                                            RecquisitionDelegate.findRecquisition(price.getRecquisitionId().getUid()));
                                    result = PrixDeVenteDelegate.savePrixDeVente(price);
                                } else {
                                    result = PrixDeVenteDelegate.updatePrixDeVente(price);
                                }
                                notifySynced(result);
                            }
                        }
                        case Tables.CLIENT -> {
                            Client client = (Client) obj;
                            boolean isSynced = ClientDelegate.isExists(client.getUid());

                            Client result;
                            if (!isSynced) {
                                result = ClientDelegate.saveClient(client);
                            } else {
                                result = ClientDelegate.updateClient(client);
                            }
                            notifySynced(result);
                        }
                        case Tables.COMPTETRESOR -> {
                            CompteTresor account = (CompteTresor) obj;
                            boolean isSynced = CompteTresorDelegate.isExists(account.getUid());

                            CompteTresor result;
                            if (!isSynced) {
                                result = CompteTresorDelegate.saveCompteTresor(account);
                            } else {
                                result = CompteTresorDelegate.updateCompteTresor(account);
                            }
                            notifySynced(result);
                        }
                        case Tables.VENTE -> {
                            Vente vente = (Vente) obj;
                            boolean exists = ClientDelegate.isExists(vente.getClientId().getUid());
                            if (exists) {
                                boolean isSynced = VenteDelegate.isExists(vente.getUid());
                                Vente result;
                                if (!isSynced) {
                                    result = VenteDelegate.saveVente(vente);
                                } else {
                                    result = VenteDelegate.updateVente(vente);
                                    removeOldLigneVente(vente);
                                }
                                notifySynced(result);
                            }
                        }
                        case Tables.LIGNEVENTE -> {
                            LigneVente saleitem = (LigneVente) obj;
                            boolean exists = ProduitDelegate.isExists(saleitem.getProductId().getUid());
                            boolean exist1 = MesureDelegate.isExists(saleitem.getMesureId().getUid());
                            boolean exist2 = VenteDelegate.isExists(saleitem.getReference().getUid());
                            if (exists && exist1 && exist2) {
                                boolean isSynced = LigneVenteDelegate.isExists(saleitem.getUid());
                                LigneVente result;
                                if (!isSynced) {
                                    result = LigneVenteDelegate.saveLigneVente(saleitem);
                                } else {
                                    result = LigneVenteDelegate.updateLigneVente(saleitem);
                                }
                                Mesure m = MesureDelegate.findMesure(saleitem.getMesureId().getUid());
                                Produit p = ProduitDelegate.findProduit(saleitem.getProductId().getUid());
                                Vente vr = VenteDelegate.findVente(saleitem.getReference().getUid());
                                double cau = saleitem.getCoutAchat() / m.getQuantContenu();
                                RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), vr.getRegion(), cau);
                                notifySynced(result);
                            }
                        }
                        case Tables.TRAISORERIE -> {
                            Traisorerie trans = (Traisorerie) obj;
                            boolean exists = CompteTresorDelegate.isExists(trans.getTresorId().getUid());
                            if (exists) {
                                boolean isSynced = TraisorerieDelegate.isExists(trans.getUid());
                                Traisorerie result;
                                if (!isSynced) {
                                    result = TraisorerieDelegate.saveTraisorerie(trans);
                                } else {
                                    result = TraisorerieDelegate.updateTraisorerie(trans);
                                }
                                notifySynced(result);
                            }
                        }
                        case Tables.DEPENSE -> {
                            Depense depense = (Depense) obj;
                            boolean isSynced = DepenseDelegate.isExists(depense.getUid());
                            Depense result;
                            if (!isSynced) {
                                result = DepenseDelegate.saveDepense(depense);
                            } else {
                                result = DepenseDelegate.updateDepense(depense);
                            }
                            notifySynced(result);
                        }
                        case Tables.OPERATION -> {
                            Operation operation = (Operation) obj;
                            boolean exists = CompteTresorDelegate.isExists(operation.getTresorId().getUid());
                            boolean exist2 = TraisorerieDelegate.isExists(operation.getCaisseOpId().getUid());
                            boolean exist3 = DepenseDelegate.isExists(operation.getDepenseId().getUid());
                            if (exists && exist2 && exist3) {
                                boolean isSynced = OperationDelegate.isExists(operation.getUid());
                                Operation result;
                                if (!isSynced) {
                                    result = OperationDelegate.saveOperation(operation);
                                } else {
                                    result = OperationDelegate.updateOperation(operation);
                                }
                                Depense dep = DepenseDelegate.findDepense(operation.getDepenseId().getUid());
                                DepenseAgregateDelegate.aggregateDepense(operation.getDate(), operation.getImputation(), operation.getMontantUsd(), operation.getMontantCdf(), dep);
                                notifySynced(result);
                            }
                        }
                        case Tables.COMPTER -> {
                            Compter compter = (Compter) obj;
                            boolean exists = InventaireDelegate.isExists(compter.getInventaireId().getUid());
                            boolean exist1 = MesureDelegate.isExists(compter.getMesureId().getUid());
                            boolean exist2 = ProduitDelegate.isExists(compter.getProductId().getUid());
                            if (exists && exist1 && exist2) {
                                boolean isSynced = CompterDelegate.isExists(compter.getUid());
                                Compter result;
                                if (!isSynced) {
                                    System.out.println("new compter");
                                    result = CompterDelegate.createCompter(compter);
                                } else {
                                    result = CompterDelegate.updateCompter(compter);
                                    System.out.println("edit compter");
                                }
//                                Mesure m = MesureDelegate.findMesure(compter.getMesureId().getUid());
//                                Produit p = ProduitDelegate.findProduit(compter.getProductId().getUid());
//                                double cau = compter.getCoutAchat() / m.getQuantContenu();
//                                RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), compter.getRegion(), cau);
                                notifySynced(result);
                            }
                        }
                        case Tables.INVENTORY -> {
                            Inventaire inventory = (Inventaire) obj;
                            boolean isSynced = InventaireDelegate.isExists(inventory.getUid());
                            Inventaire result;
                            if (!isSynced) {
                                result = InventaireDelegate.createInventaire(inventory);
                            } else {
                                result = InventaireDelegate.updateInventaire(inventory);
                            }
                            notifySynced(result);
                        }
                        case Tables.PRESENCE -> {
                            Presence presence = (Presence) obj;
                            boolean isSynced = PresenceDelegate.isExists(presence.getUid());
                            Presence result;
                            if (!isSynced) {
                                result = PresenceDelegate.savePresence(presence);
                            } else {
                                result = PresenceDelegate.updatePresence(presence);
                            }
                            notifySynced(result);
                        }
                        default -> {
                        }
                    }

                }
            }
        }
    }

    @Override
    public void onComment(String string) throws Exception {
        System.out.println("Commentaire " + string);
    }

    private com.launchdarkly.eventsource.EventSource eventSource;

    public void setEventSource(com.launchdarkly.eventsource.EventSource eventSource) {
        this.eventSource = eventSource;
    }

    @Override
    public void onError(Throwable thrwbl) {
        System.out.println("SSE Error " + thrwbl.getMessage());
        if (thrwbl.getMessage() != null && thrwbl.getMessage().contains("401")) {
            System.err.println("Closing SSE Stream due to persistent 401 error.");
            if (this.eventSource != null) {
                this.eventSource.close();
            }
        }
    }

    private void removeOldLigneVente(Vente vente) {
        List<LigneVente> ls = LigneVenteDelegate.findByReference(vente.getUid());
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
                Logger.getLogger(NotificationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void setOnDataSyncListener(OnDataSyncListener listener) {
        onDataSyncListener = listener;
    }

    private void notifySynced(BaseModel uid) {
        if (onDataSyncListener != null) {
            onDataSyncListener.onDataSynced(uid);
        }
    }

}
