/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import com.endeleya.kazisafex.EntrepriseController;
import com.endeleya.kazisafex.GoodstorageController;
import com.endeleya.kazisafex.PosController;
import data.Abonnement;
import data.Aretirer;
import data.BaseModel;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Facture;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import delegates.AretirerDelegate;
import delegates.CategoryDelegate;
import delegates.ClientAppartenirDelegate;
import delegates.ClientDelegate;
import delegates.ClientOrganisationDelegate;
import delegates.CompteTresorDelegate;
import delegates.DepenseDelegate;
import delegates.DestockerDelegate;
import delegates.FactureDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RetourDepotDelegate;
import delegates.RetourMagasinDelegate;
import delegates.StockerDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.xml.bind.DatatypeConverter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.prefs.Preferences;
import utilities.ImageProduit;

/**
 *
 * @author endeleya
 */
public class WsClientEndpoint extends Endpoint {

    static Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    private OnWebsocketCloseListener onWebsocketCloseListener;
    String userid;
    Session session;
    
    @Override
    public void onOpen(Session s, EndpointConfig ec) {
        System.err.println("Session - " + s.getId());
        userid = pref.get("userid", null);
        this.session=s;
        s.addMessageHandler(BaseModel.class , (BaseModel obj) -> {
            String uid;
            if (obj instanceof Category cat) {
                String action = cat.getAction();
                if (cat.getCount() == cat.getCounter()) {
                    if (!cat.getFrom().equals(userid)) {
                        MainUI.notifySync("Kazisafe-Synchronisation", (cat.getCount() > 1 ? ("La categorie de produit " + cat.getDescritption() + " et " + (cat.getCount() - 1) + " autre categories sauvegardes ") : "La categorie " + cat.getDescritption() + " sauvegarde avec succes "), "Synchonisation");
                    }
                }
                uid = cat.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        Category exist = CategoryDelegate.findCategory(cat.getUid());
                        if (exist == null) {
                            CategoryDelegate.updateCategory(cat);
                        }
                    }
                    case Constants.ACTION_UPDATE -> CategoryDelegate.updateCategory(cat);
                    default -> CategoryDelegate.deleteCategory(cat);
                }
                System.out.println("Category object arrive dans onmessage");
                
            } else if (obj instanceof Produit p) {
                String action = p.getAction();
                Category isExist1 = CategoryDelegate.findCategory(p.getCategoryId().getUid());
                if (isExist1 == null) {
                    return;
                }
                if (p.getCount() == p.getCounter() && !p.getAction().equals(Constants.ACTION_DELETE)) {
                    if (!p.getFrom().equals(userid)) {
                        String produit = p.getNomProduit() + " " + p.getMarque() + " "
                                + "" + p.getModele() + " " + p.getTaille();
                        MainUI.notifySync("Kazisafe-Synchronisation", (p.getCount() > 1 ? ("Le Produit " + produit + " et " + (p.getCount() - 1) + " autre produits sauvegardes ") : "Le produit " + produit + " sauvegarde "), "Synchonisation");
                    }
                }
                uid = p.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Produit exist = ProduitDelegate.findProduit(p.getUid());
                        if (exist == null) {
                            ProduitDelegate.updateProduit(p);
                        }      break;
                    case Constants.ACTION_UPDATE:
                        ProduitDelegate.updateProduit(p);
                        break;
                    default:
                        ProduitDelegate.deleteProduit(p);
                        break;
                }
                
            } else if (obj instanceof Mesure m) {
                String action = m.getAction();
                Produit isExist1 = ProduitDelegate.findProduit(m.getProduitId().getUid());
                if (isExist1 == null) {
                    return;
                }
                uid = m.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Mesure exist = MesureDelegate.findMesure(m.getUid());
                        if (exist == null) {
                            MesureDelegate.updateMesure(m);
                        }      break;
                    case Constants.ACTION_UPDATE:
                        MesureDelegate.updateMesure(m);
                        break;
                    case Constants.ACTION_DELETE:
                        MesureDelegate.deleteMesure(m);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof Fournisseur four) {
                String action = four.getAction();
                if (four.getCount() == four.getCounter()) {
                    if (!four.getFrom().equals(userid)) {
                        MainUI.notifySync("Kazisafe-Synchronisation", four.getCount() + " fournisseur a ete sauvegardes ", "Synchonisation");
                    }
                }
                uid = four.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Fournisseur exist = FournisseurDelegate.findFournisseur(four.getUid());
                        if (exist == null) {
                            FournisseurDelegate.updateFournisseur(four);
                        }      break;
                    case Constants.ACTION_UPDATE:
                        FournisseurDelegate.updateFournisseur(four);
                        break;
                    case Constants.ACTION_DELETE:
                        FournisseurDelegate.deleteFournisseur(four);
                        break;
                    default:
                        break;
                }
                
                GoodstorageController.getInstance().populateSupplier(action, four);
            } else if (obj instanceof Livraison livr) {
                String action = livr.getAction();
                Fournisseur isExist1 = FournisseurDelegate.findFournisseur(livr.getFournId().getUid());
                if (isExist1 == null) {
                    return;
                }
                uid = livr.getUid();
                if (livr.getCount() == livr.getCounter()) {
                    String supplier = isExist1.getNomFourn();
                    if (!livr.getFrom().equals(userid)) {
                        MainUI.notifySync("Kazisafe-Synchronisation", livr.getCount() > 1 ? ("Une livraison de " + supplier + " et " + (livr.getCount() - 1) + " autres livraisons sauvegardes ") : "La livraison de " + supplier + " sauvegarde ", "Synchonisation");
                    }
                }
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Livraison exist = LivraisonDelegate.findLivraison(livr.getUid());
                        if (exist == null) {
                            LivraisonDelegate.updateLivraison(livr);
                        }      break;
                    case Constants.ACTION_UPDATE:
                        LivraisonDelegate.updateLivraison(livr);
                        break;
                    case Constants.ACTION_DELETE:
                        LivraisonDelegate.deleteLivraison(livr);
                        break;
                    default:
                        break;
                }
                
                GoodstorageController.getInstance().populateDelivery(action, livr);
                
            } else if (obj instanceof Stocker stock) {
                String action = stock.getAction();
                Mesure isExist1 = MesureDelegate.findMesure(stock.getMesureId().getUid());
                if (isExist1 == null) {
                    return;
                }
                uid = stock.getUid();
                Produit isExist = ProduitDelegate.findProduit(stock.getProductId().getUid());
                if (isExist == null) {
                    return;
                }
                if (stock.getCount() == stock.getCounter()) {
                    String produit = isExist.getNomProduit() + " " + isExist.getMarque() + " "
                            + "" + isExist.getModele() + " " + isExist.getTaille();
                    if (!stock.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", (stock.getCount() > 1 ? "Le stock de " + produit + " et " + (stock.getCount() - 1) + " autre produits sauvegardes " : "Stock de " + produit + " sauvegarde "), "Synchonisation");
                    }
                }
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Stocker exist = StockerDelegate.findStocker(stock.getUid());
                        if (exist == null) {
                            StockerDelegate.updateStocker(stock);
                        }      break;
                    case Constants.ACTION_UPDATE:
                        StockerDelegate.updateStocker(stock);
                        break;
                    case Constants.ACTION_DELETE:
                        StockerDelegate.deleteStocker(stock);
                        break;
                    default:
                        break;
                }
                GoodstorageController.getInstance().populateStocker(action, stock);
                
            } else if (obj instanceof Destocker destk) {
                String action = destk.getAction();
                Mesure isExist1 = MesureDelegate.findMesure(destk.getMesureId().getUid());
                if (isExist1 == null) {
                    return;
                }
                Produit isExist = ProduitDelegate.findProduit(destk.getProductId().getUid());
                if (isExist == null) {
                    return;
                }
                uid = destk.getUid();
                if (destk.getCount() == destk.getCounter()) {
                    if (!destk.getFrom().equals(userid)) {
                        
                        String produit = isExist.getNomProduit() + " " + isExist.getMarque() + " "
                                + "" + isExist.getModele() + " " + isExist.getTaille();
                        MainUI.notifySync("Kazisafe-Synchronisation", (destk.getCount() > 1 ? "Le destockage de " + produit + " et " + (destk.getCount() - 1) + " autre produits sauvegardes " : " Le destockage de " + produit + " sauvegarde "), "Synchonisation");
                    }
                }
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Destocker exist = DestockerDelegate.findDestocker(destk.getUid());
                        if (exist == null) {
                            DestockerDelegate.updateDestocker(destk);
                        }      break;
                    case Constants.ACTION_UPDATE:
                        DestockerDelegate.updateDestocker(destk);
                        break;
                    case Constants.ACTION_DELETE:
                        DestockerDelegate.deleteDestocker(destk);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof Recquisition recq) {
                String action = recq.getAction();
                Mesure isExist1 = MesureDelegate.findMesure(recq.getMesureId().getUid());
                if (isExist1 == null) {
                    return;
                }
                Produit isExist = ProduitDelegate.findProduit(recq.getProductId().getUid());
                if (isExist == null) {
                    return;
                }
                uid = recq.getUid();
                if (recq.getCount() == recq.getCounter()) {
                    if (!recq.getFrom().equals(userid)) {
                        
                        String produit = isExist.getNomProduit() + " " + isExist.getMarque() + " "
                                + "" + isExist.getModele() + " " + isExist.getTaille();
                        MainUI.notifySync("Kazisafe-Synchronisation", (recq.getCount() > 1 ? "La recquisition de " + produit + " et celles de " + (recq.getCount() - 1) + " autres produit sauvegardees " : "La recquisition de " + produit + " sauvegarde "), "Synchonisation");
                    }
                }
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        Recquisition exist = RecquisitionDelegate.findRecquisition(recq.getUid());
                        if (exist == null) {
                            RecquisitionDelegate.updateRecquisition(recq);
                        }
                    }
                    case Constants.ACTION_UPDATE -> RecquisitionDelegate.updateRecquisition(recq);
                    case Constants.ACTION_DELETE -> RecquisitionDelegate.deleteRecquisition(recq);
                    default -> {
                    }
                }
                
            } else if (obj instanceof PrixDeVente pv) {
                String action = pv.getAction();
                Recquisition isExist = RecquisitionDelegate.findRecquisition(pv.getRecquisitionId().getUid());
                if (isExist == null) {
                    return;
                }
                Mesure isExist1 = MesureDelegate.findMesure(pv.getMesureId().getUid());
                if (isExist1 == null) {
                    return;
                }
                if (pv.getCount() == pv.getCounter()) {
                    if (!pv.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", " Le prix de la requisition " + isExist.getReference() + " bien configure", "Synchonisation");
                    }
                }
                uid = pv.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        PrixDeVente exist = PrixDeVenteDelegate.findPrixDeVente(pv.getUid());
                        if (exist == null) {
                            PrixDeVenteDelegate.updatePrixDeVente(pv);
                        }
                    }
                    case Constants.ACTION_UPDATE -> PrixDeVenteDelegate.updatePrixDeVente(pv);
                    case Constants.ACTION_DELETE -> PrixDeVenteDelegate.deletePrixDeVente(pv);
                    default -> {
                    }
                }
                
            } else if (obj instanceof Client client) {
                String action = client.getAction();
                if (client.getCount() == client.getCounter()) {
                    if (!client.getFrom().equals(userid)) {
                        MainUI.notifySync("Kazisafe-Synchronisation", client.getCount() + " client enregistre", "Synchonisation");
                    }
                }
                uid = client.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Client exist = ClientDelegate.findClient(client.getUid());
                        if (exist == null) {
                            try {
                                ClientDelegate.saveClient(client);
                            } catch (Exception e) {
                                if (e instanceof jakarta.persistence.EntityExistsException ex) {
                                    
                                }
                            }
                        }   break;
                    case Constants.ACTION_UPDATE:
                        ClientDelegate.updateClient(client);
                        break;
                    case Constants.ACTION_DELETE:
                        ClientDelegate.deleteClient(client);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof ClientOrganisation client) {
                String action = client.getAction();
                uid = client.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        ClientOrganisation exist = ClientOrganisationDelegate.findClientOrganisation(client.getUid());
                        if (exist == null) {
                            ClientOrganisationDelegate.updateClientOrganisation(client);
                        }
                    }
                    case Constants.ACTION_UPDATE -> ClientOrganisationDelegate.updateClientOrganisation(client);
                    case Constants.ACTION_DELETE -> ClientOrganisationDelegate.deleteClientOrganisation(client);
                    default -> {
                    }
                }
                
            } else if (obj instanceof ClientAppartenir client) {
                String action = client.getAction();
                Client isExist = ClientDelegate.findClient(client.getClientId().getUid());
                if (isExist == null) {
                    return;
                }
                ClientOrganisation isExist1 = ClientOrganisationDelegate.findClientOrganisation(client.getClientOrganisationId().getUid());
                if (isExist1 == null) {
                    return;
                }
                uid = client.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        ClientAppartenir exist = ClientAppartenirDelegate.findClientAppartenir(client.getUid());
                        if (exist == null) {
                            ClientAppartenirDelegate.updateClientAppartenir(client);
                        }   break;
                    case Constants.ACTION_UPDATE:
                        ClientAppartenirDelegate.updateClientAppartenir(client);
                        break;
                    case Constants.ACTION_DELETE:
                        ClientAppartenirDelegate.deleteClientAppartenir(client);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof Vente vente) {
                String action = vente.getAction();
                Client isExist = ClientDelegate.findClient(vente.getClientId().getUid());
                if (isExist == null) {
                    return;
                }
                if (vente.getCount() == vente.getCounter()) {
                    MainUI.notifySync("Kazisafe-Synchronisation", vente.getCounter() + " Vente a ete enregistrer a " + vente.getRegion() + " via " + pref.get("operator", "un utilisateur"), "Synchonisation");
                }
                uid = String.valueOf(vente.getUid());
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Vente exist = VenteDelegate.findVente(vente.getUid());
                        if (exist == null) {
                            VenteDelegate.updateVente(vente);
                        }   break;
                    case Constants.ACTION_UPDATE:
                        VenteDelegate.updateVente(vente);
                        break;
                    case Constants.ACTION_DELETE:
                        VenteDelegate.deleteVente(vente);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof LigneVente ligv) {
                String action = ligv.getAction();
                Vente isExist = VenteDelegate.findVente(ligv.getReference().getUid());
                if (isExist == null) {
                    return;
                }
                Produit isExist1 = ProduitDelegate.findProduit(ligv.getProductId().getUid());
                if (isExist1 == null) {
                    return;
                }
                Mesure isExist2 = MesureDelegate.findMesure(ligv.getMesureId().getUid());
                if (isExist2 == null) {
                    return;
                }
                uid = String.valueOf(ligv.getUid());
                
                switch (action) {
                    case Constants.ACTION_CREATE:
                        LigneVente exist = LigneVenteDelegate.findLigneVente(ligv.getUid());
                        if (exist == null) {
                            LigneVenteDelegate.updateLigneVente(ligv);
                        }   break;
                    case Constants.ACTION_UPDATE:
                        LigneVenteDelegate.updateLigneVente(ligv);
                        break;
                    case Constants.ACTION_DELETE:
                        LigneVenteDelegate.deleteLigneVente(ligv);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof Traisorerie tr) {
                String action = tr.getAction();
                CompteTresor isExist = CompteTresorDelegate.findCompteTresor(tr.getTresorId().getUid());
                if (isExist == null) {
                    return;
                }
                if (tr.getCount() == tr.getCounter()) {
                    MainUI.notifySync("Kazisafe-Synchronisation", " Le " + isExist.getIntitule() + " de " + isExist.getBankName() + " a fait " + tr.getCount() + " mouvement", "Synchonisation");
                }
                uid = tr.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        Traisorerie exist = TraisorerieDelegate.findTraisorerie(tr.getUid());
                        if (exist == null) {
                            TraisorerieDelegate.updateTraisorerie(tr);
                        }   break;
                    case Constants.ACTION_UPDATE:
                        TraisorerieDelegate.updateTraisorerie(tr);
                        break;
                    case Constants.ACTION_DELETE:
                        TraisorerieDelegate.deleteTraisorerie(tr);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof Operation opera) {
                String action = opera.getAction();
                Depense isExist = DepenseDelegate.findDepense(opera.getDepenseId().getUid());
                if (isExist == null) {
                    return;
                }
                Traisorerie isExist1 = TraisorerieDelegate.findTraisorerie(opera.getCaisseOpId().getUid());
                if (isExist1 == null) {
                    return;
                }
                if (opera.getCount() == opera.getCounter()) {
                    if (!opera.getFrom().equals(userid)) {
                        MainUI.notifySync("Kazisafe-Synchronisation", " Une depense a ete effectuee chez " + opera.getAction(), "Synchonisation");
                    }
                }
                uid = opera.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        Operation exist = OperationDelegate.findOperation(opera.getUid());
                        if (exist == null) {
                            OperationDelegate.updateOperation(opera);
                        }
                    }
                    case Constants.ACTION_UPDATE -> OperationDelegate.updateOperation(opera);
                    case Constants.ACTION_DELETE -> OperationDelegate.deleteOperation(opera);
                    default -> {
                    }
                }
                
            } else if (obj instanceof Depense opera) {
                String action = opera.getAction();
                if (opera.getCount() == opera.getCounter()) {
                    if (!opera.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", " Une compte depense a ete cree a " + opera.getRegion(), "Synchonisation");
                    }
                }
                uid = opera.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        Depense exist = DepenseDelegate.findDepense(opera.getUid());
                        if (exist == null) {
                            DepenseDelegate.updateDepense(opera);
                        }
                    }
                    case Constants.ACTION_UPDATE -> DepenseDelegate.updateDepense(opera);
                    case Constants.ACTION_DELETE -> DepenseDelegate.deleteDepense(opera);
                    default -> {
                    }
                }
                
            } else if (obj instanceof CompteTresor opera) {
                String action = opera.getAction();
                if (opera.getCount() == opera.getCounter()) {
                    if (!opera.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", " Un compte de tresorerie de la region " + opera.getRegion() + " a ete cree", "Synchonisation");
                    }
                }
                uid = opera.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE:
                        CompteTresor exist = CompteTresorDelegate.findCompteTresor(opera.getUid());
                        if (exist == null) {
                            List<CompteTresor> cpts = CompteTresorDelegate.findByNumeroCompte(opera.getNumeroCompte());
                            if (cpts.isEmpty()) {
                                CompteTresorDelegate.updateCompteTresor(opera);
                            }
                            
                        }   break;
                    case Constants.ACTION_UPDATE:
                        CompteTresorDelegate.updateCompteTresor(opera);
                        break;
                    case Constants.ACTION_DELETE:
                        CompteTresorDelegate.deleteCompteTresor(opera);
                        break;
                    default:
                        break;
                }
                
            } else if (obj instanceof Facture opera) {
                String action = opera.getAction();
                ClientOrganisation isExist = ClientOrganisationDelegate.findClientOrganisation(opera.getOrganisId().getUid());
                if (isExist == null) {
                    return;
                }
                uid = opera.getUid();
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        Facture exist = FactureDelegate.findFacture(opera.getUid());
                        if (exist == null) {
                            FactureDelegate.updateFacture(opera);
                        }
                    }
                    case Constants.ACTION_UPDATE -> FactureDelegate.updateFacture(opera);
                    case Constants.ACTION_DELETE -> FactureDelegate.deleteFacture(opera);
                    default -> {
                    }
                }
                
            } else if (obj instanceof RetourDepot opera) {
                String action = opera.getAction();
                Recquisition isExist = RecquisitionDelegate.findRecquisition(opera.getRecquisitionId().getUid());
                if (isExist == null) {
                    return;
                }
                Destocker isExist1 = DestockerDelegate.findDestocker(opera.getDestockerId().getUid());
                if (isExist1 == null) {
                    return;
                }
                uid = opera.getUid();
                Mesure isExist2 = MesureDelegate.findMesure(opera.getMesureId().getUid());
                if (isExist2 == null) {
                    return;
                }
                if (opera.getCount() == opera.getCounter()) {
                    if (!opera.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", " Une marchandise recquisitionee a ete retorunee au depot depuis " + opera.getRegion(), "Synchonisation");
                    }
                }
                switch (action) {
                    case Constants.ACTION_CREATE -> {
                        RetourDepot exist = RetourDepotDelegate.findRetourDepot(opera.getUid());
                        if (exist == null) {
                            RetourDepotDelegate.updateRetourDepot(opera);
                        }
                    }
                    case Constants.ACTION_UPDATE -> RetourDepotDelegate.updateRetourDepot(opera);
                    case Constants.ACTION_DELETE -> RetourDepotDelegate.deleteRetourDepot(opera);
                    default -> {
                    }
                }
                
            } else if (obj instanceof RetourMagasin opera) {
                String action = opera.getAction();
                Mesure isExist1 = MesureDelegate.findMesure(opera.getMesureId().getUid());
                if (isExist1 == null) {
                    return;
                }
                LigneVente isExist = LigneVenteDelegate.findLigneVente(opera.getLigneVenteId().getUid());
                if (isExist == null) {
                    return;
                }
                Client isExist2 = ClientDelegate.findClient(opera.getClientId().getUid());
                if (isExist2 == null) {
                    return;
                }
                if (opera.getCount() == opera.getCounter()) {
                    if (!opera.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", " Une Marchandise vendue a ete retournee au magasin " + opera.getRegion(), "Synchonisation");
                    }
                }
                uid = opera.getUid();
                if (action.equals(Constants.ACTION_CREATE)) {
                    RetourMagasin exist = RetourMagasinDelegate.findRetourMagasin(opera.getUid());
                    if (exist == null) {
                        RetourMagasinDelegate.updateRetourMagasin(opera);
                    }
                } else if (action.equals(Constants.ACTION_UPDATE)) {
                    RetourMagasinDelegate.updateRetourMagasin(opera);
                } else if (action.equals(Constants.ACTION_DELETE)) {
                    RetourMagasinDelegate.deleteRetourMagasin(opera);
                }
                
            } else if (obj instanceof Aretirer) {
                Aretirer opera = (Aretirer) obj;
                String action = opera.getAction();
                Mesure isExist1 = MesureDelegate.findMesure(opera.getMesureId().getUid());
                if (isExist1 == null) {
                    return;
                }
                LigneVente isExist = LigneVenteDelegate.findLigneVente(opera.getLigneVenteId().getUid());
                if (isExist == null) {
                    return;
                }
                Client isExist2 = ClientDelegate.findClient(opera.getClientId().getUid());
                if (isExist2 == null) {
                    return;
                }
                if (opera.getCount() == opera.getCounter()) {
                    if (!opera.getFrom().equals(userid)) {
                        
                        MainUI.notifySync("Kazisafe-Synchronisation", " Une facture a ete marquee comme a retirer ", "Synchonisation");
                    }
                }
                uid = opera.getUid();
                if (action.equals(Constants.ACTION_CREATE)) {
                    Aretirer exist = AretirerDelegate.findAretirer(opera.getUid());
                    if (exist == null) {
                        AretirerDelegate.updateAretirer(opera);
                    }
                } else if (action.equals(Constants.ACTION_UPDATE)) {
                    AretirerDelegate.updateAretirer(opera);
                } else if (action.equals(Constants.ACTION_DELETE)) {
                    AretirerDelegate.deleteAretirer(opera);
                }
                
            } else if (obj instanceof ImageProduit) {
                ImageProduit p = (ImageProduit) obj;
                String photoB64 = p.getImageBase64();
                byte[] pixcha = DatatypeConverter.parseBase64Binary(photoB64);
                String idp = p.getIdProduit();
                FileUtils.byteToFile(idp, pixcha);
                PosController inst = PosController.getInstance();
                if (inst != null) {
                    inst.getInstance().tryit(idp);
                }
            } else if (obj instanceof Abonnement) {
                pref = Preferences.userNodeForPackage(SyncEngine.class);
                Abonnement abn = (Abonnement) obj;
                String typeAb = abn.getTypeAbonnement();
                String etat = abn.getEtat();
                LocalDateTime date = abn.getDateAbonnement();
                double nombreOper = abn.getNombreOperation();
                System.err.println("Abx " + etat + " " + date.toString() + " " + userid + " " + abn.getFrom());
                pref.put("type-sub", typeAb);
                userid = pref.get("userid", "na");
                if (!abn.getFrom().equals(userid)) {
                    MainUI.notifyConnect(null, "Connexion a Kazisafe", abn.getAgent() + " vient de se connecter à son compte Kazisafe", 6);
                }
                if (typeAb.equalsIgnoreCase("Gold")) {
                    pref.putDouble("sub", nombreOper);
                    MainUI.notifySync("Kazisafe-Abonnement", "Abonnement Bonus " + typeAb + " active", "Notification de souscription au service kazisafe");
                } else {
                    double d1 = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    double d2 = System.currentTimeMillis();
                    double max = d1 + nombreOper;
                    double remained = max - d2;
                    long week = Constants.MILLSECONDS_JOURN * 7;
                    System.err.println("Abonnement total " + max + " rest " + remained + " now " + d2);
                    double sous = pref.getDouble("sub", 0);
                    
                    if (Math.abs(remained) <= week) {
                        MainUI.notify(null, "Attention", "Le crédit Kazisafe (Record) expire bientôt, pensez à le renouveller", 5, "warning");
                    }
                    String abx = pref.get("etat-sub", "-");
                    if (!abx.equals("Expiree") && (sous <= 1 || remained <= 0)) {
                        MainUI.notifySync("Kazisafe-Abonnement", "Activation souscription " + typeAb + " faite avec succes", "Notification de souscription au service kazisafe");
                    }
                    String abo = abn.getAgent();
                    pref.putDouble("sub", max);
                    pref.put("etat-sub", etat);
                    if (!abn.getFrom().equals(userid)) {
                        MainUI.notifySync("Connexion a Kazisafe", abo + " vient de se connecter à son compte Kazisafe", "Notification de connexion kazisafe");
                    }
                    
                }
//            pref.putLong(tools.Constants.LAST_SESSION_ENDS, System.currentTimeMillis());
EntrepriseController.getInstance().go();

            } 
        });
        
    }

    @Override
    public void onError(Session session, Throwable err) {
        super.onError(session, err); 
        System.err.println("Erreur client endpoint " + err.getMessage());
        err.printStackTrace();
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason); 
        if(onWebsocketCloseListener!=null){
            onWebsocketCloseListener.onWebSocketClose(session, !session.isOpen());
        }
    }

    public void setOnWebsocketCloseListener(OnWebsocketCloseListener onWebsocketCloseListener) {
        this.onWebsocketCloseListener = onWebsocketCloseListener;
    }
    
    

    

}
