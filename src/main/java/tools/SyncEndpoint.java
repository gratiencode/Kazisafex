/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.endeleya.kazisafex.EntrepriseController;
import com.endeleya.kazisafex.GoodstorageController;
import com.endeleya.kazisafex.PosController;
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
import delegates.JournalDelegate;
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
import util.decoders.ImageProductDecoder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import jakarta.persistence.EntityManagerFactory;
import jakarta.xml.bind.DatatypeConverter;
import data.Abonnement;
import data.Aretirer;
import data.BaseModel;
import data.BulkModel;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Facture;
import data.Fournisseur;
import data.Journal;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import services.JpaUtil;
import util.decoders.BaseModelDecoder;
import util.encoders.AbonnementEncoder;
import util.encoders.AretirerEncoder;
import util.encoders.CategoryEncoder;
import util.encoders.ClientAppartenirEncoder;
import util.encoders.ClientEncoder;
import util.encoders.ClientOrganisationEncoder;
import util.encoders.CompteTresorEncoder;
import util.encoders.DepenseEncoder;
import util.encoders.DestockerEncoder;
import util.encoders.FactureEncoder;
import util.encoders.FournisseurEncoder;
import util.encoders.ImageProductEncoder;
import util.encoders.LigneVenteEncoder;
import util.encoders.LivraisonEncoder;
import util.encoders.MesureEncoder;
import util.encoders.OperationEncoder;
import util.encoders.PrixDeVenteEncoder;
import util.encoders.ProduitEncoder;
import util.encoders.RecquisitionEncoder;
import util.encoders.RefresherEncoder;
import util.encoders.RetourDepotEncoder;
import util.encoders.RetourMagasinEncoder;
import util.encoders.StockerEncoder;
import util.encoders.TraisorerieEncoder;
import util.encoders.VenteEncoder;
import util.listencoders.ListEncoder;
import utilities.ImageProduit;

/**
 *
 * @author eroot
 */
@ClientEndpoint(
        decoders = {ImageProductDecoder.class, BaseModelDecoder.class},
        encoders = {AbonnementEncoder.class, AretirerEncoder.class, CategoryEncoder.class, ProduitEncoder.class, MesureEncoder.class, FournisseurEncoder.class, ListEncoder.class, RefresherEncoder.class,
            LivraisonEncoder.class, StockerEncoder.class, DestockerEncoder.class, RecquisitionEncoder.class, PrixDeVenteEncoder.class,
            ClientEncoder.class, ClientAppartenirEncoder.class, ClientOrganisationEncoder.class, VenteEncoder.class, LigneVenteEncoder.class, ImageProductEncoder.class,
            FactureEncoder.class, CompteTresorEncoder.class, TraisorerieEncoder.class, DepenseEncoder.class, OperationEncoder.class, RetourMagasinEncoder.class,
            RetourDepotEncoder.class, ListEncoder.class})
public class SyncEndpoint 
//        extends SubmissionPublisher<Set<BaseModel>> implements Flow.Processor<Set<BaseModel>, BaseModel>
{

    int NB_CORE = Runtime.getRuntime().availableProcessors();
    int NB_THREAD = NB_CORE + (NB_CORE / 2);
    private static SyncEndpoint instance;
    static Preferences pref;
    private OnWebsocketCloseListener onWebsocketCloseListener;
    private Flow.Subscription subscription;
    BlockingQueue<BaseModel> mos = new LinkedBlockingQueue<>(12);
    ExecutorService thp = Executors.newFixedThreadPool(NB_THREAD);
    HashMap<String, Set<BaseModel>> dico = new HashMap<>();
    private SubmissionPublisher<Set<BaseModel>> publisher = new SubmissionPublisher<>();
    public static SyncEndpoint getInstance() {
        return instance;
    }

    private static String url;

    private Session session;
    String userid;
 
    public SyncEndpoint() {
    }

    public SyncEndpoint(String uri) {
        super();
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        connect(uri);
        instance = this;
        
    }

    private void connect(String uri) {
        url = uri;
        WebSocketContainer wscontainer = ContainerProvider.getWebSocketContainer();
        try {

            System.out.println("URI----> " + uri);
            URI urlx = URI.create(uri);
            wscontainer.connectToServer(this, urlx);
            pref.putInt("exit", 0);
        } catch (DeploymentException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MainUI.notify(null, "Erreur", "Veuillez vérifier la qualité de votre connection internet", 6, "error");
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @OnOpen
    public void open(Session s) {
        System.err.println("Session - " + s.getId());
        userid = pref.get("userid", null);
        this.session = s;
//        publisher.subscribe(this); 
    }

//    @OnMessage
//    public void onTextMessage(String message, Session session) {
//        System.out.println("Test "+message);
//    }
    @OnMessage
    public void onObjectMessage(BaseModel obj, Session session) {
        //   System.out.println("Value " + obj.toString());
        String uid;
        if (obj instanceof Category) {
            Category cat = (Category) obj;
            String action = cat.getAction();
            if (cat.getCount() == cat.getCounter()) {
                if (!cat.getFrom().equals(userid)) {
                    MainUI.notifySync("Kazisafe-Synchronisation", (cat.getCount() > 1 ? ("La categorie de produit " + cat.getDescritption() + " et " + (cat.getCount() - 1) + " autre categories sauvegardes ") : "La categorie " + cat.getDescritption() + " sauvegarde avec succes "), "Synchonisation");
                }
            }
            uid = cat.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Category exist = CategoryDelegate.findCategory(cat.getUid());
                if (exist == null) {
                    CategoryDelegate.updateCategory(cat);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                CategoryDelegate.updateCategory(cat);
            } else {
                CategoryDelegate.deleteCategory(cat);
            }
            System.out.println("Category object arrive dans onmessage");
            journalize(cat, uid);
        } else if (obj instanceof Produit) {
            Produit p = (Produit) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                Produit exist = ProduitDelegate.findProduit(p.getUid());
                if (exist == null) {
                    ProduitDelegate.updateProduit(p);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                ProduitDelegate.updateProduit(p);
            } else {
                ProduitDelegate.deleteProduit(p);
            }
            journalize(p, uid);
        } else if (obj instanceof Mesure) {
            Mesure m = (Mesure) obj;
            String action = m.getAction();
            Produit isExist1 = ProduitDelegate.findProduit(m.getProduitId().getUid());
            if (isExist1 == null) {
                return;
            }
            uid = m.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Mesure exist = MesureDelegate.findMesure(m.getUid());
                if (exist == null) {
                    MesureDelegate.updateMesure(m);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                MesureDelegate.updateMesure(m);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                MesureDelegate.deleteMesure(m);
            }
            journalize(m, uid);
        } else if (obj instanceof Fournisseur) {
            Fournisseur four = (Fournisseur) obj;
            String action = four.getAction();
            if (four.getCount() == four.getCounter()) {
                if (!four.getFrom().equals(userid)) {
                    MainUI.notifySync("Kazisafe-Synchronisation", four.getCount() + " fournisseur a ete sauvegardes ", "Synchonisation");
                }
            }
            uid = four.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Fournisseur exist = FournisseurDelegate.findFournisseur(four.getUid());
                if (exist == null) {
                    FournisseurDelegate.updateFournisseur(four);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                FournisseurDelegate.updateFournisseur(four);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                FournisseurDelegate.deleteFournisseur(four);
            }
            journalize(four, uid);
            GoodstorageController.getInstance().populateSupplier(action, four);
        } else if (obj instanceof Livraison) {
            Livraison livr = (Livraison) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                Livraison exist = LivraisonDelegate.findLivraison(livr.getUid());
                if (exist == null) {
                    LivraisonDelegate.updateLivraison(livr);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                LivraisonDelegate.updateLivraison(livr);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                LivraisonDelegate.deleteLivraison(livr);
            }
            journalize(livr, uid);
            GoodstorageController.getInstance().populateDelivery(action, livr);

        } else if (obj instanceof Stocker) {
            Stocker stock = (Stocker) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                Stocker exist = StockerDelegate.findStocker(stock.getUid());

                if (exist == null) {
                    StockerDelegate.updateStocker(stock);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                StockerDelegate.updateStocker(stock);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                StockerDelegate.deleteStocker(stock);
            }
            GoodstorageController.getInstance().populateStocker(action, stock);
            journalize(stock, uid);
        } else if (obj instanceof Destocker) {
            Destocker destk = (Destocker) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                Destocker exist = DestockerDelegate.findDestocker(destk.getUid());
                if (exist == null) {
                    DestockerDelegate.updateDestocker(destk);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                DestockerDelegate.updateDestocker(destk);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                DestockerDelegate.deleteDestocker(destk);
            }
            journalize(destk, uid);
        } else if (obj instanceof Recquisition) {
            Recquisition recq = (Recquisition) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                Recquisition exist = RecquisitionDelegate.findRecquisition(recq.getUid());
                if (exist == null) {
                    RecquisitionDelegate.updateRecquisition(recq);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                RecquisitionDelegate.updateRecquisition(recq);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                RecquisitionDelegate.deleteRecquisition(recq);
            }
            journalize(recq, uid);
        } else if (obj instanceof PrixDeVente) {
            PrixDeVente pv = (PrixDeVente) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                PrixDeVente exist = PrixDeVenteDelegate.findPrixDeVente(pv.getUid());
                if (exist == null) {
                    PrixDeVenteDelegate.updatePrixDeVente(pv);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                PrixDeVenteDelegate.updatePrixDeVente(pv);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                PrixDeVenteDelegate.deletePrixDeVente(pv);
            }
            journalize(pv, uid);
        } else if (obj instanceof Client) {
            Client client = (Client) obj;
            String action = client.getAction();
            if (client.getCount() == client.getCounter()) {
                if (!client.getFrom().equals(userid)) {
                    MainUI.notifySync("Kazisafe-Synchronisation", client.getCount() + " client enregistre", "Synchonisation");
                }
            }
            uid = client.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Client exist = ClientDelegate.findClient(client.getUid());
                if (exist == null) {
                    try{
                    ClientDelegate.saveClient(client);
                    }catch(Exception e){
                        if(e instanceof jakarta.persistence.EntityExistsException ex){
                            
                        }
                    }
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                ClientDelegate.updateClient(client);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                ClientDelegate.deleteClient(client);
            }
            journalize(client, uid);
        } else if (obj instanceof ClientOrganisation) {
            ClientOrganisation client = (ClientOrganisation) obj;
            String action = client.getAction();
            uid = client.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                ClientOrganisation exist = ClientOrganisationDelegate.findClientOrganisation(client.getUid());
                if (exist == null) {
                    ClientOrganisationDelegate.updateClientOrganisation(client);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                ClientOrganisationDelegate.updateClientOrganisation(client);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                ClientOrganisationDelegate.deleteClientOrganisation(client);
            }
            journalize(client, uid);
        } else if (obj instanceof ClientAppartenir) {
            ClientAppartenir client = (ClientAppartenir) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                ClientAppartenir exist = ClientAppartenirDelegate.findClientAppartenir(client.getUid());
                if (exist == null) {
                    ClientAppartenirDelegate.updateClientAppartenir(client);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                ClientAppartenirDelegate.updateClientAppartenir(client);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                ClientAppartenirDelegate.deleteClientAppartenir(client);
            }
            journalize(client, uid);
        } else if (obj instanceof Vente) {
            Vente vente = (Vente) obj;
            String action = vente.getAction();
            Client isExist = ClientDelegate.findClient(vente.getClientId().getUid());
            if (isExist == null) {
                return;
            }
            if (vente.getCount() == vente.getCounter()) {
                MainUI.notifySync("Kazisafe-Synchronisation", vente.getCounter() + " Vente a ete enregistrer a " + vente.getRegion() + " via " + pref.get("operator", "un utilisateur"), "Synchonisation");
            }
            uid = String.valueOf(vente.getUid());
            if (action.equals(Constants.ACTION_CREATE)) {
                Vente exist = VenteDelegate.findVente(vente.getUid());
                if (exist == null) {
                    VenteDelegate.updateVente(vente);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                VenteDelegate.updateVente(vente);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                VenteDelegate.deleteVente(vente);
            }
            journalize(vente, uid);
        } else if (obj instanceof LigneVente) {
            LigneVente ligv = (LigneVente) obj;
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

            if (action.equals(Constants.ACTION_CREATE)) {
                LigneVente exist = LigneVenteDelegate.findLigneVente(ligv.getUid());

                if (exist == null) {
                    LigneVenteDelegate.updateLigneVente(ligv);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                LigneVenteDelegate.updateLigneVente(ligv);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                LigneVenteDelegate.deleteLigneVente(ligv);
            }
            journalize(ligv, uid);
        } else if (obj instanceof Traisorerie) {
            Traisorerie tr = (Traisorerie) obj;
            String action = tr.getAction();
            CompteTresor isExist = CompteTresorDelegate.findCompteTresor(tr.getTresorId().getUid());
            if (isExist == null) {
                return;
            }
            if (tr.getCount() == tr.getCounter()) {
                MainUI.notifySync("Kazisafe-Synchronisation", " Le " + isExist.getIntitule() + " de " + isExist.getBankName() + " a fait " + tr.getCount() + " mouvement", "Synchonisation");
            }
            uid = tr.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Traisorerie exist = TraisorerieDelegate.findTraisorerie(tr.getUid());
                if (exist == null) {
                    TraisorerieDelegate.updateTraisorerie(tr);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                TraisorerieDelegate.updateTraisorerie(tr);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                TraisorerieDelegate.deleteTraisorerie(tr);
            }
            journalize(tr, tr.getUid());
        } else if (obj instanceof Operation) {
            Operation opera = (Operation) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                Operation exist = OperationDelegate.findOperation(opera.getUid());
                if (exist == null) {
                    OperationDelegate.updateOperation(opera);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                OperationDelegate.updateOperation(opera);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                OperationDelegate.deleteOperation(opera);
            }
            journalize(opera, opera.getUid());
        } else if (obj instanceof Depense) {
            Depense opera = (Depense) obj;
            String action = opera.getAction();
            if (opera.getCount() == opera.getCounter()) {
                if (!opera.getFrom().equals(userid)) {

                    MainUI.notifySync("Kazisafe-Synchronisation", " Une compte depense a ete cree a " + opera.getRegion(), "Synchonisation");
                }
            }
            uid = opera.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Depense exist = DepenseDelegate.findDepense(opera.getUid());
                if (exist == null) {
                    DepenseDelegate.updateDepense(opera);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                DepenseDelegate.updateDepense(opera);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                DepenseDelegate.deleteDepense(opera);
            }
            journalize(opera, opera.getUid());
        } else if (obj instanceof CompteTresor) {
            CompteTresor opera = (CompteTresor) obj;
            String action = opera.getAction();
            if (opera.getCount() == opera.getCounter()) {
                if (!opera.getFrom().equals(userid)) {

                    MainUI.notifySync("Kazisafe-Synchronisation", " Un compte de tresorerie de la region " + opera.getRegion() + " a ete cree", "Synchonisation");
                }
            }
            uid = opera.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                CompteTresor exist = CompteTresorDelegate.findCompteTresor(opera.getUid());
                if (exist == null) {
                    List<CompteTresor> cpts = CompteTresorDelegate.findByNumeroCompte(opera.getNumeroCompte());
                    if (cpts.isEmpty()) {
                        CompteTresorDelegate.updateCompteTresor(opera);
                    }

                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                CompteTresorDelegate.updateCompteTresor(opera);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                CompteTresorDelegate.deleteCompteTresor(opera);
            }
            journalize(opera, opera.getUid());
        } else if (obj instanceof Facture) {
            Facture opera = (Facture) obj;
            String action = opera.getAction();
            ClientOrganisation isExist = ClientOrganisationDelegate.findClientOrganisation(opera.getOrganisId().getUid());
            if (isExist == null) {
                return;
            }
            uid = opera.getUid();
            if (action.equals(Constants.ACTION_CREATE)) {
                Facture exist = FactureDelegate.findFacture(opera.getUid());
                if (exist == null) {
                    FactureDelegate.updateFacture(opera);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                FactureDelegate.updateFacture(opera);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                FactureDelegate.deleteFacture(opera);
            }
            journalize(opera, opera.getUid());
        } else if (obj instanceof RetourDepot) {
            RetourDepot opera = (RetourDepot) obj;
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
            if (action.equals(Constants.ACTION_CREATE)) {
                RetourDepot exist = RetourDepotDelegate.findRetourDepot(opera.getUid());
                if (exist == null) {
                    RetourDepotDelegate.updateRetourDepot(opera);
                }
            } else if (action.equals(Constants.ACTION_UPDATE)) {
                RetourDepotDelegate.updateRetourDepot(opera);
            } else if (action.equals(Constants.ACTION_DELETE)) {
                RetourDepotDelegate.deleteRetourDepot(opera);
            }
            journalize(opera, opera.getUid());
        } else if (obj instanceof RetourMagasin) {
            RetourMagasin opera = (RetourMagasin) obj;
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
            journalize(opera, opera.getUid());
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
            journalize(opera, opera.getUid());
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
            Date date = abn.getDateAbonnement();
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
                double d1 = date.getTime();
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
            pref.putLong(tools.Constants.LAST_SESSION_ENDS, System.currentTimeMillis());
            EntrepriseController.getInstance().go();

        } else if (obj instanceof BulkModel) {
            BulkModel bulk = (BulkModel) obj;
            List<Object> models = bulk.getModels();
            try {
                notification(models);
                System.out.println("Taille - " + models.size());
//                dico.put(getListType(models), toBaseModelSet(models));
//                System.out.println("Dico stock " + dico.size());
                
                Set<BaseModel> data = toBaseModelSet(models);
                publisher.submit(data);
               
//                onNext(data);
//                EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
//                CountDownLatch cdl = new CountDownLatch(1);
//                ExecutorService exec = Executors.newFixedThreadPool(NB_THREAD);
//                PersisterTask task1 = new PersisterTask(emf, data, cdl);
//                LoggingTask task2 = new LoggingTask(emf, data, cdl);
//                exec.submit(task1);
//                exec.submit(task2);
//
//                exec.shutdown();

//                for (: dico.values()) {
////                    try {
//
//                        for (BaseModel bm : looper) {
//                            System.out.println("Tyep yask "+bm.getType());
//                           
//                            thp.submit(task);
//                            
//                        }
//                    } finally {
//                        // Arrêter l'ExecutorService
////                        thp.shutdown();
//                        // Attendre la fin de toutes les tâches
//                        try {
//                            if (!thp.awaitTermination(120, java.util.concurrent.TimeUnit.SECONDS)) {
//                                thp.shutdownNow();
//                            }
//                        } catch (InterruptedException e) {
//                            thp.shutdownNow();
//                        }
//                        // Fermer l'EntityManagerFactory
//                        JpaUtil.closeEntityManagerFactory();
//                    }
//                }
                // Attendre la fin de toutes les tâches
                // Fermer l'EntityManagerFactory
//                Iterator<Set<BaseModel>> it = dico.values().iterator();
//                 System.out.println("Capacite au debut " + mos.remainingCapacity());
//                while (mos.remainingCapacity() == 12) {
//                    if (it.hasNext()) {
//                        Set<BaseModel> no = it.next();
//                        int nth = NB_THREAD / 2;
//                        //for (int i = 0; i <3; i++) {
//                        thp.submit(new Producer(mos, toBaseModelSet(models)));
//                        // }
//                        //  for (int i = 0; i < 3; i++) {
//                        thp.execute(new Consumer(mos));
//
//                        //  }
//                    }
//                }
//            for (Object model : models) {
//                System.out.println("Elemnt " + model);
//                save((LinkedHashMap) model);
//            }
            } catch (ParseException ex) {
                Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
     
    
    private void notification(List<Object> objs) {
        if (objs.isEmpty()) {
            return;
        }
        int size = objs.size();
        String type = getListType(objs);
        LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) objs.get(0);
        String from = String.valueOf(obj.get("from"));
        System.out.println(size + " elements de type " + type + " bien arrives");
        if (from != null) {
            if (!from.equals(userid)) {
                MainUI.notifySync("Kazisafe-Synchronisation", size + " elements de type " + type + " synchronises", "Synchonisation");
            }
        }

    }

    private String getListType(List<Object> objs) {
        LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) objs.get(0);
        if (obj.containsKey("type")) {
            String type = String.valueOf(obj.get("type"));
            return type;
        }
        return null;
    }

    private Set<BaseModel> toBaseModelSet(List<Object> objs) throws ParseException {
        String type = getListType(objs);
        Set<BaseModel> catUpdates = new HashSet<>();
        switch (type) {
            case "CATEGORY":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Category rstc = new Category(String.valueOf(vmap.get("uid")), String.valueOf(vmap.get("descritption")));
                    rstc.setAction(String.valueOf(vmap.get("action")));
                    rstc.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    rstc.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    rstc.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    rstc.setType(String.valueOf(vmap.get("type")));
                    rstc.setPayload(String.valueOf(vmap.get("payload")));
                    rstc.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(rstc);
                }
                return catUpdates;

            case "PRODUIT":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Produit p = new Produit(String.valueOf(vmap.get("uid")));
                    p.setCodebar(String.valueOf(vmap.get("codebar")));
                    p.setNomProduit(String.valueOf(vmap.get("nomProduit")));
                    p.setMarque(String.valueOf(vmap.get("marque")));
                    p.setModele(String.valueOf(vmap.get("modele")));
                    p.setCouleur(String.valueOf(vmap.get("couleur")));
                    p.setTaille(String.valueOf(vmap.get("taille")));
                    p.setMethodeInventaire(String.valueOf(vmap.get("methodeInventaire")));
                    p.setCategoryId(new Category(String.valueOf(vmap.get("categoryId"))));

                    p.setAction(String.valueOf(vmap.get("action")));
                    p.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    p.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    p.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    p.setType(String.valueOf(vmap.get("type")));
                    p.setPayload(String.valueOf(vmap.get("payload")));
                    p.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(p);
//                    jrs.add(Util.createJournal(p.getUid(), p, true));
                }
                return catUpdates;
            case "MESURE":

                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Mesure m = new Mesure(String.valueOf(vmap.get("uid")));
                    m.setDescription(String.valueOf(vmap.get("description")));
                    m.setQuantContenu(Double.parseDouble(String.valueOf(vmap.get("quantContenu"))));
                    m.setProduitId(new Produit(String.valueOf(vmap.get("produitId"))));

                    m.setAction(String.valueOf(vmap.get("action")));
                    m.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    m.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    m.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    m.setType(String.valueOf(vmap.get("type")));
                    m.setPayload(String.valueOf(vmap.get("payload")));
                    m.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(m);
                }

                return catUpdates;
            case "FOURNISSEUR":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Fournisseur ins = new Fournisseur(String.valueOf(vmap.get("uid")));
                    ins.setAdresse(String.valueOf(vmap.get("adresse")));
                    ins.setIdentification(String.valueOf(vmap.get("identification")));
                    ins.setNomFourn(String.valueOf(vmap.get("nomFourn")));
                    ins.setPhone(String.valueOf(vmap.get("phone")));

                    ins.setAction(String.valueOf(vmap.get("action")));
                    ins.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    ins.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    ins.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    ins.setType(String.valueOf(vmap.get("type")));
                    ins.setPayload(String.valueOf(vmap.get("payload")));
                    ins.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(ins);
//                    jfs.add(Util.createJournal(ins.getUid(), ins, true));
                }
//                FournisseurDelegate.mergeSet(fs);
//                JournalDelegate.createJournals(jfs);
                return catUpdates;
            case "LIVRAISON":
//                Set<Livraison> ls = new HashSet<>();
//                Set<Journal> jls = new HashSet<>();
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Livraison livrz = new Livraison(String.valueOf(vmap.get("uid")));
                    livrz.setLibelle(String.valueOf(vmap.get("libelle")));
                    livrz.setNumPiece(String.valueOf(vmap.get("numPiece")));
                    livrz.setObservation(String.valueOf(vmap.get("observation")));
                    livrz.setPayed(Double.valueOf(String.valueOf(vmap.get("payed"))));
                    livrz.setReduction(Double.valueOf(String.valueOf(vmap.get("reduction"))));
                    livrz.setReference(String.valueOf(vmap.get("reference")));
                    livrz.setRegion(String.valueOf(vmap.get("region")));
                    livrz.setRemained(Double.valueOf(String.valueOf(vmap.get("remained"))));
                    String tpy=String.valueOf(vmap.get("topay"));
                    livrz.setTopay(Double.valueOf(tpy.equals("null")?"0":tpy));
                    String torec=String.valueOf(vmap.get("toreceive"));
                    livrz.setToreceive(Double.valueOf(torec.equals("null")?"0":torec));
//                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                       livrz.setDateLivr(LocalDate.parse(String.valueOf(vmap.get("dateLivr"))));
//                    livrz.setDateLivr(df.parse(String.valueOf(vmap.get("dateLivr"))));
                    livrz.setFournId(new Fournisseur(String.valueOf(vmap.get("fournId"))));

                    livrz.setAction(String.valueOf(vmap.get("action")));
                    livrz.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    livrz.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    livrz.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    livrz.setType(String.valueOf(vmap.get("type")));
                    livrz.setPayload(String.valueOf(vmap.get("payload")));
                    livrz.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(livrz);
//                    jls.add(Util.createJournal(livrz.getUid(), livrz, true));
                }
//                LivraisonDelegate.mergeSet(ls);
//                JournalDelegate.createJournals(jls);
                return catUpdates;
            case "STOCKER":
//                Set<Stocker> ss = new HashSet<>();
//                Set<Journal> jss = new HashSet<>();
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Stocker stok = new Stocker(String.valueOf(vmap.get("uid")));
                    stok.setLibelle(String.valueOf(vmap.get("libelle")));
                    stok.setRegion(String.valueOf(vmap.get("region")));
                    stok.setObservation(String.valueOf(vmap.get("observation")));
                    stok.setNumlot(String.valueOf(vmap.get("numlot")));
                    stok.setLocalisation(String.valueOf(vmap.get("localisation")));
                    stok.setCoutAchat(Double.parseDouble(String.valueOf(vmap.get("coutAchat"))));
                    String red=String.valueOf(vmap.get("reduction"));
                    stok.setReduction(Double.parseDouble(red.equals("null")?"0":red));
                    stok.setPrixAchatTotal(Double.parseDouble(String.valueOf(vmap.get("prixAchatTotal"))));
                    stok.setQuantite(Double.parseDouble(String.valueOf(vmap.get("quantite"))));
                    stok.setStockAlerte(Double.parseDouble(String.valueOf(vmap.get("stockAlerte"))));
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Object obz = vmap.get("dateExpir");
                    if (obz != null) {

                        stok.setDateExpir(df.parse(String.valueOf(obz)));
                    }
                    DateFormat dsf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    stok.setDateStocker(dsf.parse(String.valueOf(vmap.get("dateStocker"))));
                    stok.setLivraisId(new Livraison(String.valueOf(vmap.get("livraisId"))));
                    stok.setMesureId(new Mesure(String.valueOf(vmap.get("mesureId"))));
                    stok.setProductId(new Produit(String.valueOf(vmap.get("productId"))));

                    stok.setAction(String.valueOf(vmap.get("action")));
                    stok.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    stok.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    stok.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    stok.setType(String.valueOf(vmap.get("type")));
                    stok.setPayload(String.valueOf(vmap.get("payload")));
                    stok.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(stok);
//                    jss.add(Util.createJournal(stok.getUid(), stok, true));
                }
//                StockerDelegate.mergeSet(ss);
//                JournalDelegate.createJournals(jss);
                return catUpdates;
            case "DESTOCKER":
//                Set<Destocker> ds = new HashSet<>();
//                Set<Journal> jds = new HashSet<>();
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Destocker destok = new Destocker();
                    destok.setUid(String.valueOf(vmap.get("uid")));
                    destok.setLibelle(String.valueOf(vmap.get("libelle")));
                    destok.setCoutAchat(Double.parseDouble(String.valueOf(vmap.get("coutAchat"))));
                    destok.setObservation(String.valueOf(vmap.get("observation")));
                    destok.setNumlot(String.valueOf(vmap.get("numlot")));
                    destok.setReference(String.valueOf(vmap.get("reference")));
                    destok.setRegion(String.valueOf(vmap.get("region")));
                    destok.setQuantite(Double.parseDouble(String.valueOf(vmap.get("quantite"))));
                    destok.setDestination(String.valueOf(vmap.get("destination")));
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    destok.setDateDestockage(df.parse(String.valueOf(vmap.get("dateDestockage"))));
                    destok.setMesureId(new Mesure(String.valueOf(vmap.get("mesureId"))));
                    destok.setProductId(new Produit(String.valueOf(vmap.get("productId"))));

                    destok.setAction(String.valueOf(vmap.get("action")));
                    destok.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    destok.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    destok.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    destok.setType(String.valueOf(vmap.get("type")));
                    destok.setPayload(String.valueOf(vmap.get("payload")));
                    destok.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(destok);
//                    jds.add(Util.createJournal(destok.getUid(), destok, true));
                }
//                DestockerDelegate.mergeSet(ds);
//                JournalDelegate.createJournals(jds);
                return catUpdates;
            case "RECQUISITION":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Recquisition recq = new Recquisition(String.valueOf(vmap.get("uid")));
                    recq.setReference(String.valueOf(vmap.get("reference")));
                    recq.setObservation(String.valueOf(vmap.get("observation")));
                    recq.setNumlot(String.valueOf(vmap.get("numlot")));
                    recq.setRegion(String.valueOf(vmap.get("region")));
                    recq.setQuantite(Double.parseDouble(String.valueOf(vmap.get("quantite"))));
                    recq.setCoutAchat(Double.parseDouble(String.valueOf(vmap.get("coutAchat"))));
                    String alt=String.valueOf(vmap.get("stockAlert"));
                    recq.setStockAlert(Double.valueOf(alt.equals("null")?"0":alt)); 
                    DateFormat def = new SimpleDateFormat("yyyy-MM-dd");
                    Object de = vmap.get("dateExpiry");
                    if (de != null) {
                        recq.setDateExpiry(def.parse(String.valueOf(de)));
                    }
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    recq.setDate(df.parse(String.valueOf(vmap.get("date"))));
                    recq.setMesureId(new Mesure(String.valueOf(vmap.get("mesureId"))));
                    recq.setProductId(new Produit(String.valueOf(vmap.get("productId"))));
                    recq.setAction(String.valueOf(vmap.get("action")));
                    recq.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    recq.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    recq.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    recq.setType(String.valueOf(vmap.get("type")));
                    recq.setPayload(String.valueOf(vmap.get("payload")));
                    recq.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(recq);
                }
                return catUpdates;
            case "PRIXDEVENTE":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    PrixDeVente pxv = new PrixDeVente(String.valueOf(vmap.get("uid")));
                    pxv.setQmax(Double.valueOf(String.valueOf(vmap.get("qmax"))));
                    pxv.setQmin(Double.valueOf(String.valueOf(vmap.get("qmin"))));
                    pxv.setDevise(String.valueOf(vmap.get("devise")));
                    pxv.setPrixUnitaire(Double.valueOf(String.valueOf(vmap.get("prixUnitaire"))));
                    pxv.setMesureId(new Mesure(String.valueOf(vmap.get("mesureId"))));
                    pxv.setRecquisitionId(new Recquisition(String.valueOf(vmap.get("recquisitionId"))));

                    pxv.setAction(String.valueOf(vmap.get("action")));
                    pxv.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    pxv.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    pxv.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    pxv.setType(String.valueOf(vmap.get("type")));
                    pxv.setPayload(String.valueOf(vmap.get("payload")));
                    pxv.setFrom(String.valueOf(vmap.get("from")));

                    catUpdates.add(pxv);
                }
                return catUpdates;
            case "CLIENT":
                for (Object obj : objs) {
                    if (obj instanceof String) {
                        continue;
                    }
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Client client = new Client();
                    client.setUid(String.valueOf(vmap.get("uid")));
                    client.setAdresse(String.valueOf(vmap.get("adresse")));
                    client.setEmail(String.valueOf(vmap.get("email")));
                    client.setTypeClient(String.valueOf(vmap.get("typeClient")));
                    client.setNomClient(String.valueOf(vmap.get("nomClient")));
                    client.setParentId(new Client(String.valueOf(vmap.get("parentId"))));
                    client.setPhone(String.valueOf(vmap.get("phone")));

                    client.setAction(String.valueOf(vmap.get("action")));
                    client.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    client.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    client.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    client.setType(String.valueOf(vmap.get("type")));
                    client.setPayload(String.valueOf(vmap.get("payload")));
                    client.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(client);
                }
                return catUpdates;
            case "CLIENTORGANISATION":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    ClientOrganisation clio = new ClientOrganisation();
                    clio.setUid(String.valueOf(vmap.get("uid")));
                    clio.setRegion(String.valueOf(vmap.get("region")));
                    clio.setAdresse(String.valueOf(vmap.get("adresse")));
                    clio.setBoitePostalOrganisation(String.valueOf(vmap.get("boitePostalOrganisation")));
                    clio.setDomaineOrganisation(String.valueOf(vmap.get("domaineOrganisation")));
                    clio.setEmailOrganisation(String.valueOf(vmap.get("emailOrganisation")));
                    clio.setNomOrganisation(String.valueOf(vmap.get("nomOrganisation")));
                    clio.setPhoneOrganisation(String.valueOf(vmap.get("phoneOrganisation")));
                    clio.setRccmOrganisation(String.valueOf(vmap.get("rccmOrganisation")));
                    clio.setWebsiteOrganisation(String.valueOf(vmap.get("websiteOrganisation")));

                    clio.setAction(String.valueOf(vmap.get("action")));
                    clio.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    clio.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    clio.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    clio.setType(String.valueOf(vmap.get("type")));
                    clio.setPayload(String.valueOf(vmap.get("payload")));
                    clio.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(clio);
                }
                return catUpdates;
            case "CLIENTAPPARTENIR":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    ClientAppartenir oper = new ClientAppartenir();
                    oper.setUid(String.valueOf(vmap.get("uid")));
                    oper.setRegion(String.valueOf(vmap.get("region")));
                    oper.setDateAppartenir((Date) vmap.get("date"));
                    oper.setClientId((Client) vmap.get("clientId"));
                    oper.setClientOrganisationId((ClientOrganisation) vmap.get("clientOrganisationId"));

                    oper.setAction(String.valueOf(vmap.get("action")));
                    oper.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    oper.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    oper.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    oper.setType(String.valueOf(vmap.get("type")));
                    oper.setPayload(String.valueOf(vmap.get("payload")));
                    oper.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(oper);
                }
                return catUpdates;
            case "VENTE":
//                Set<Vente> vs = new HashSet<>();
//                Set<Journal> jvs = new HashSet<>();
                for (Object obj : objs) {
                    if (obj instanceof Integer) {
                        continue;
                    }
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Vente vente = new Vente();
                    vente.setUid(Integer.valueOf(String.valueOf(vmap.get("uid"))));
                    vente.setLibelle(String.valueOf(vmap.get("libelle")));
                    vente.setLatitude(Double.valueOf(String.valueOf(vmap.get("latitude"))));
                    vente.setObservation(String.valueOf(vmap.get("observation")));
                    vente.setLongitude(Double.valueOf(String.valueOf(vmap.get("longitude"))));
                    vente.setMontantCdf(Double.parseDouble(String.valueOf(vmap.get("montantCdf"))));
                    String dtt=String.valueOf(vmap.get("montantDette"));
                    vente.setMontantDette(Double.valueOf(dtt.equals("null")?"0":dtt));
                    vente.setRegion(String.valueOf(vmap.get("region")));
                    vente.setMontantUsd(Double.parseDouble(String.valueOf(vmap.get("montantUsd"))));
                    vente.setPayment(String.valueOf(vmap.get("payment")));
                    vente.setReference(String.valueOf(vmap.get("reference")));
                    DateFormat def = new SimpleDateFormat("yyyy-MM-dd");
                    Object ech = vmap.get("echeance");
                    if (ech != null) {
                        vente.setEcheance(def.parse(String.valueOf(ech)));
                    }
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    vente.setDateVente(df.parse(String.valueOf(vmap.get("dateVente"))));
                    vente.setDeviseDette(String.valueOf(vmap.get("deviseDette")));
                    vente.setClientId(new Client(String.valueOf(vmap.get("clientId"))));

                    vente.setAction(String.valueOf(vmap.get("action")));
                    vente.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    vente.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    vente.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    vente.setType(String.valueOf(vmap.get("type")));
                    vente.setPayload(String.valueOf(vmap.get("payload")));
                    vente.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(vente);
                }
                return catUpdates;
            case "LIGNEVENTE":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    LigneVente lignv = new LigneVente();
                    lignv.setUid(Long.valueOf(String.valueOf(vmap.get("uid"))));
                    lignv.setClientId(String.valueOf(vmap.get("clientId")));
                    lignv.setNumlot(String.valueOf(vmap.get("numlot")));
                    lignv.setPrixUnit(Double.valueOf(String.valueOf(vmap.get("prixUnit"))));
                    lignv.setQuantite(Double.parseDouble(String.valueOf(vmap.get("quantite"))));
                    lignv.setMontantCdf(Double.parseDouble(String.valueOf(vmap.get("montantCdf"))));
                    lignv.setMontantUsd(Double.parseDouble(String.valueOf(vmap.get("montantUsd"))));
                    lignv.setProductId(new Produit(String.valueOf(vmap.get("productId"))));
                    lignv.setMesureId(new Mesure(String.valueOf(vmap.get("mesureId"))));
                    LinkedHashMap<String, Object> ref = (LinkedHashMap<String, Object>) vmap.get("reference");
                    lignv.setReference(new Vente(Integer.valueOf(String.valueOf(ref.get("uid")))));

                    lignv.setAction(String.valueOf(vmap.get("action")));
                    lignv.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    lignv.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    lignv.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    lignv.setType(String.valueOf(vmap.get("type")));
                    lignv.setPayload(String.valueOf(vmap.get("payload")));
                    lignv.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(lignv);
                }

                return catUpdates;
            case "COMPTETRESOR":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    CompteTresor bill = new CompteTresor(String.valueOf(vmap.get("uid")));
                    bill.setBankName(String.valueOf(vmap.get("bankName")));
                    bill.setIntitule(String.valueOf(vmap.get("intitule")));
                    bill.setNumeroCompte(String.valueOf(vmap.get("numeroCompte")));
                    bill.setRegion(String.valueOf(vmap.get("region")));
                    bill.setSoldeMinimum((Double) vmap.get("soldeMinimum"));
                    bill.setTypeCompte(String.valueOf(vmap.get("typeCompte")));

                    bill.setAction(String.valueOf(vmap.get("action")));
                    bill.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    bill.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    bill.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    bill.setType(String.valueOf(vmap.get("type")));
                    bill.setPayload(String.valueOf(vmap.get("payload")));
                    bill.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(bill);
                }
                return catUpdates;
            case "TRAISORERIE":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Traisorerie tres = new Traisorerie();
                    tres.setUid(String.valueOf(vmap.get("uid")));
                    tres.setLibelle(String.valueOf(vmap.get("libelle")));
                    tres.setMouvement(String.valueOf(vmap.get("mouvement")));
                    tres.setTypeTresorerie(String.valueOf(vmap.get("typeTresorerie")));
                    tres.setMontantCdf(Double.parseDouble(String.valueOf(vmap.get("montantCdf"))));
                    tres.setRegion(String.valueOf(vmap.get("region")));
                    Object ctrz = vmap.get("tresorId");
                    if (ctrz != null) {
                        tres.setTresorId(new CompteTresor(String.valueOf(ctrz)));
                    }
                    tres.setMontantUsd(Double.parseDouble(String.valueOf(vmap.get("montantUsd"))));
                    tres.setReference(String.valueOf(vmap.get("reference")));
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    tres.setDate(df.parse(String.valueOf(vmap.get("date"))));
                    tres.setAction(String.valueOf(vmap.get("action")));
                    tres.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    tres.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    tres.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    tres.setType(String.valueOf(vmap.get("type")));
                    tres.setPayload(String.valueOf(vmap.get("payload")));
                    tres.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(tres);
                }
                return catUpdates;
            case "DEPENSE":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Depense dep = new Depense(String.valueOf(vmap.get("uid")));
                    dep.setNomDepense(String.valueOf(vmap.get("nomDepense")));
                    dep.setRegion(String.valueOf(vmap.get("region")));

                    dep.setAction(String.valueOf(vmap.get("action")));
                    dep.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    dep.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    dep.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    dep.setType(String.valueOf(vmap.get("type")));
                    dep.setPayload(String.valueOf(vmap.get("payload")));
                    dep.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(dep);
                }
                return catUpdates;
            case "OPERATION":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Operation operation = new Operation();
                    operation.setUid(String.valueOf(vmap.get("uid")));
                    operation.setLibelle(String.valueOf(vmap.get("libelle")));
                    operation.setMouvement(String.valueOf(vmap.get("mouvement")));
                    operation.setImputation(String.valueOf(vmap.get("imputation")));
                    operation.setMontantCdf(Double.valueOf(String.valueOf(vmap.get("montantCdf"))));
                    operation.setRegion(String.valueOf(vmap.get("region")));
                    operation.setMontantUsd(Double.valueOf(String.valueOf(vmap.get("montantUsd"))));
                    operation.setReferenceOp(String.valueOf(vmap.get("referenceOp")));
                    Object cotrz = vmap.get("tresorId");
                    if (cotrz != null) {
                        operation.setTresorId(new CompteTresor(String.valueOf(cotrz)));
                    }
                    Object deps = vmap.get("depenseId");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    if (deps != null) {
                        operation.setDepenseId(new Depense(String.valueOf(deps)));
                    }
                    operation.setDate(df.parse(String.valueOf(vmap.get("date"))));
                    operation.setCaisseOpId(new Traisorerie(String.valueOf(vmap.get("caisseOpId"))));
                    operation.setAction(String.valueOf(vmap.get("action")));
                    operation.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    operation.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    operation.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    operation.setType(String.valueOf(vmap.get("type")));
                    operation.setPayload(String.valueOf(vmap.get("payload")));
                    operation.setFrom(String.valueOf(vmap.get("from")));
                    Operation op = OperationDelegate.findOperation(operation.getUid());
                    catUpdates.add(op);
                }
                return catUpdates;
            case "RETOURMAGASIN":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    RetourMagasin rtrmag = new RetourMagasin();
                    rtrmag.setUid(String.valueOf(vmap.get("uid")));
                    rtrmag.setRegion(String.valueOf(vmap.get("region")));
                    rtrmag.setPrixVente(Double.valueOf(String.valueOf(vmap.get("prixVente"))));
                    rtrmag.setReferenceVente(String.valueOf(vmap.get("referenceVente")));
                    rtrmag.setMotif(String.valueOf(vmap.get("motif")));
                    rtrmag.setQuantite(Double.valueOf(String.valueOf(vmap.get("quantite"))));
                    rtrmag.setDate((Date) vmap.get("date"));
                    rtrmag.setLigneVenteId((LigneVente) vmap.get("ligneVenteId"));
                    rtrmag.setClientId((Client) vmap.get("recquisitionId"));
                    rtrmag.setMesureId((Mesure) vmap.get("mesureId"));

                    rtrmag.setAction(String.valueOf(vmap.get("action")));
                    rtrmag.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    rtrmag.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    rtrmag.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    rtrmag.setType(String.valueOf(vmap.get("type")));
                    rtrmag.setPayload(String.valueOf(vmap.get("payload")));
                    rtrmag.setFrom(String.valueOf(vmap.get("from")));
                    // RetourMagasin rtr = RetourMagasinDelegate.findRetourMagasin(rtrmag.getUid());
                    catUpdates.add(rtrmag);
                }
            case "RETOURDEPOT":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    RetourDepot rtrdep = new RetourDepot();
                    rtrdep.setUid(String.valueOf(vmap.get("uid")));
                    rtrdep.setRegion(String.valueOf(vmap.get("region")));
                    rtrdep.setCoutAchat(Double.valueOf(String.valueOf(vmap.get("coutAchat"))));
                    rtrdep.setLocalisation(String.valueOf(vmap.get("localisation")));
                    rtrdep.setMotif(String.valueOf(vmap.get("motif")));
                    rtrdep.setNumlot(String.valueOf(vmap.get("numlot")));
                    rtrdep.setQuantite(Double.valueOf(String.valueOf(vmap.get("quantite"))));
                    rtrdep.setRegionDest(String.valueOf(vmap.get("regionDest")));
                    rtrdep.setRegionProv(String.valueOf(vmap.get("regionProv")));
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    rtrdep.setDate(df.parse(String.valueOf(vmap.get("date"))));
                    rtrdep.setDestockerId((Destocker) vmap.get("destockerId"));
                    rtrdep.setRecquisitionId((Recquisition) vmap.get("recquisitionId"));
                    rtrdep.setMesureId((Mesure) vmap.get("mesureId"));

                    rtrdep.setAction(String.valueOf(vmap.get("action")));
                    rtrdep.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    rtrdep.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    rtrdep.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    rtrdep.setType(String.valueOf(vmap.get("type")));
                    rtrdep.setPayload(String.valueOf(vmap.get("payload")));
                    rtrdep.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(rtrdep);
                }
            case "ARETIRER":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Aretirer aretir = new Aretirer();
                    aretir.setUid(String.valueOf(vmap.get("uid")));
                    aretir.setNumlot(String.valueOf(vmap.get("numlot")));
                    aretir.setPrixVente(Double.valueOf(String.valueOf(vmap.get("prixVente"))));
                    aretir.setQuantite(Double.valueOf(String.valueOf(vmap.get("quantite"))));
                    aretir.setReferenceVente(String.valueOf(vmap.get("referenceVente")));
                    aretir.setRegion(String.valueOf(vmap.get("region")));
                    aretir.setStatus(String.valueOf(vmap.get("status")));
                    aretir.setDate((Date) vmap.get("date"));
                    aretir.setClientId((Client) vmap.get("clientId"));
                    aretir.setLigneVenteId((LigneVente) vmap.get("ligneVenteId"));
                    aretir.setMesureId((Mesure) vmap.get("mesureId"));
                    aretir.setAction(String.valueOf(vmap.get("action")));
                    aretir.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    aretir.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    aretir.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    aretir.setType(String.valueOf(vmap.get("type")));
                    aretir.setPayload(String.valueOf(vmap.get("payload")));
                    aretir.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(aretir);
                }
            case "FACTURE":
                for (Object obj : objs) {
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) obj;
                    Facture f = new Facture();
                    f.setUid(String.valueOf(vmap.get("uid")));
                    f.setStartDate((Date) vmap.get("startDate"));
                    f.setStartDate((Date) vmap.get("endDate"));

                    f.setNumero(String.valueOf(vmap.get("numero")));
                    f.setOrganisId(new ClientOrganisation(String.valueOf(vmap.get("organisId"))));
                    f.setPayedamount(Double.valueOf(String.valueOf(vmap.get("payedamount"))));
                    f.setRegion(String.valueOf(vmap.get("region")));
                    f.setStatus(String.valueOf(vmap.get("status")));
                    f.setTotalamount(Double.valueOf(String.valueOf(vmap.get("totalamount"))));
                    f.setAction(String.valueOf(vmap.get("action")));
                    f.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    f.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    f.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    f.setType(String.valueOf(vmap.get("type")));
                    f.setPayload(String.valueOf(vmap.get("payload")));
                    f.setFrom(String.valueOf(vmap.get("from")));
                    catUpdates.add(f);
                }
                return catUpdates;
        }
        return catUpdates;
    }

    private void journalize(BaseModel obj, String uid) {
        Journal jrl = Util.createJournal(uid, obj, true);
        JournalDelegate.saveJournal(jrl);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeResean) {
        pref.putLong(Constants.LAST_SESSION_ENDS, System.currentTimeMillis());
        int exit = pref.getInt("exit", 0);
        if (exit != 1) {
            connect(url);
        }
        System.out.println("Seesion " + session.getId() + " is closed  for "
                + "" + closeResean.getReasonPhrase() + " code [" + closeResean.getCloseCode() + "]");
    }

    @OnError
    public void onError(Session session, Throwable err) {
        System.err.println("Erreur client endpoint " + err.getMessage());
        err.printStackTrace();
    
    }

    public void async(BaseModel bm) {
        try {
            if (this.session != null) {
                Future<Void> asyncf = this.session.getAsyncRemote().sendObject(pref);
                if (asyncf.isDone()) {
                    Void rst = asyncf.get();
                    if (rst != null) {
                        System.err.println("ASYNCHRONIZATION FINISHES " + bm);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            if (this.session != null) {
                this.session.getBasicRemote().sendText(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendObject(BaseModel model) {
        try {
            if (this.session != null) {
                this.session.getBasicRemote().sendObject(model);
            }
        } catch (IOException e) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, e.getMessage());
        } catch (EncodeException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendListObject(List models) {
        try {
            if (this.session != null) {
                this.session.getBasicRemote().sendObject(models);
            }
        } catch (IOException e) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, e.getMessage());
        } catch (EncodeException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeSession() {
        try {
            if (session != null) {
                if (session.isOpen()) {
                    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Bye bye Fin de session!"));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setOnWebsocketCloseListener(OnWebsocketCloseListener onWebsocketCloseListener) {
        this.onWebsocketCloseListener = onWebsocketCloseListener;
    }

    private void notifyClose(Session session) {
        if (this.onWebsocketCloseListener != null) {
            this.onWebsocketCloseListener.onWebSocketClose(session, true);
        }
    }

    public SubmissionPublisher<Set<BaseModel>> getPublisher() {
        return publisher;
    }

  

}
