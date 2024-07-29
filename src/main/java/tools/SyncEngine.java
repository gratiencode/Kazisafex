package tools;

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
import data.core.KazisafeServiceFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import data.network.Kazisafe;
import data.Aretirer;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Entreprise;
import data.Facture;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Module;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import java.time.Duration;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import retrofit2.Response;

/**
 *
 * @author eroot
 */
public class SyncEngine implements Callable<Boolean> {

    private Kazisafe kazisafe;
    SafeSession session;

    Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    Entreprise eze;
    ScheduledExecutorService ses;
    private static final int SLEEP=300;

    private OnUpdateVersionListener onUpdateVersionListener;
    SubmissionPublisher<List> publisher = new SubmissionPublisher<>();

    private static SyncEngine instance;

    public SyncEngine setup(String token, Entreprise entr) {
        this.kazisafe = KazisafeServiceFactory.createService(token);
        this.eze = entr;
        session=SafeSession.getInstance();
        return this;
    }
    private final Flow.Subscriber<List> subs = new Flow.Subscriber<List>() {
        private Flow.Subscription sub;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.sub = subscription;
            this.sub.request(1);
        }

        @Override
        public void onNext(List item) {
            Util.syncList(item);
            this.sub.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Sync de list terminee avec succes");
        }
    };

    private SyncEngine() {
        publisher.subscribe(subs);
        ses = Executors.newSingleThreadScheduledExecutor();
        int cpus = Runtime.getRuntime().availableProcessors();
        session=SafeSession.getInstance();
        System.out.println("CPUS : " + cpus);
    }

    public static SyncEngine getInstance() {
        if (instance == null) {
            instance = new SyncEngine();
        }
        return instance;
    }

    public String syncInBackground() {
        ExecutorService esvc = Executors.newSingleThreadExecutor();
        Future<Boolean> result = esvc.submit(this);
        try {
            boolean isfinish = result.get();
            if (result.isDone()) {
                if (isfinish) {
                    System.out.println("Synchronization invoked finish: ");
                    return "finish";
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private boolean syncup() {
        int timeout = pref.getInt("sync-freq", 300);
        ses.scheduleAtFixedRate(() -> {
            try {
                List<Category> cats = session.findCategories();
                List<Category> lcat = new ArrayList<>();
                for (Category f : cats) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setProduitList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.CATEGORY.name());
                        f.setAction(Constants.ACTION_CREATE);
                        lcat.add(f);
                    }
                }
                if (!lcat.isEmpty()) {
                    List<List<Category>> categories = Util.partitions(lcat, 6);
                    for (List<Category> categs : categories) {
                        publisher.submit(categs);
                    }
                    
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Produit> pros = session.findProduits();
                List<Produit> lpo = new ArrayList<>();
                for (Produit f : pros) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        Category c = f.getCategoryId();
                        f.setCategoryId(new Category(c.getUid()));
                        f.setImage(null);
                        f.setDestockerList(null);
                        f.setLigneVenteList(null);
                        f.setMesureList(null);
                        f.setRecquisitionList(null);
                        f.setStockerList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.PRODUIT.name());
                        f.setAction(Constants.ACTION_CREATE);
                        lpo.add(f);
                    }
                }
                if (!lpo.isEmpty()) {
                    List<List<Produit>> partitions = Util.partitions(lpo, 16);
                    for (List<Produit> partition : partitions) {
                         publisher.submit(partition);
                    }
                   
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Mesure> mesures = session.findMesures();
                List<Mesure> rmesures = new ArrayList<>();
                for (Mesure f : mesures) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        Produit p = f.getProduitId();
                        f.setProduitId(new Produit(p.getUid()));
                        f.setDestockerList(null);
                        f.setLigneVenteList(null);
                        f.setPrixDeVenteList(null);
                        f.setRecquisitionList(null);
                        f.setStockerList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.MESURE.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rmesures.add(f);
                    }
                }
                if (!rmesures.isEmpty()) {
                    List<List<Mesure>> partitions = Util.partitions(rmesures, 18);
                    for (List<Mesure> partition : partitions) {
                        publisher.submit(partition);
                    }
                    
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Fournisseur> fssr = session.findFournisseurs();
                List<Fournisseur> fss = new ArrayList<>();
                for (Fournisseur f : fssr) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setLivraisonList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.FOURNISSEUR.name());
                        f.setAction(Constants.ACTION_CREATE);
                        fss.add(f);
                    }
                }
                if (!fss.isEmpty()) {
                    List<List<Fournisseur>> partitions = Util.partitions(fss, 10);
                    for (List<Fournisseur> partition : partitions) {
                        publisher.submit(partition);
                    }
                    
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Livraison> livres = session.findLivraisons();
                List<Livraison> livraizone = new ArrayList<>();
                for (Livraison f : livres) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setFournId(new Fournisseur(f.getFournId().getUid()));
                        f.setStockerList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.LIVRAISON.name());
                        f.setAction(Constants.ACTION_CREATE);
                        livraizone.add(f);
                    }
                }
                if (!livraizone.isEmpty()) {
                    List<List<Livraison>> partitions = Util.partitions(livraizone, 12);
                    for (List<Livraison> partition : partitions) {
                        publisher.submit(partition);
                    }
                    
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Stocker> stockers = session.findStockers();
                List<Stocker> stockerss = new ArrayList<>();
                for (Stocker f : stockers) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setProductId(new Produit(f.getProductId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setLivraisId(new Livraison(f.getLivraisId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.STOCKER.name());
                        f.setAction(Constants.ACTION_CREATE);
                        stockerss.add(f);
                    }
                }
                if (!stockerss.isEmpty()) {
                   List<List<Stocker>> partitions = Util.partitions(stockerss, 12);
                    for (List<Stocker> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Destocker> destockers = session.findDestockers();
                List<Destocker> destoks = new ArrayList<>();
                for (Destocker f : destockers) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setProductId(new Produit(f.getProductId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.DESTOCKER.name());
                        f.setAction(Constants.ACTION_CREATE);
                        destoks.add(f);
                    }
                }
                if (!destoks.isEmpty()) {
                    List<List<Destocker>> partitions = Util.partitions(destoks, 12);
                    for (List<Destocker> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Recquisition> recquis = session.findRecquisitions();
                List<Recquisition> rrecquis = new ArrayList<>();
                for (Recquisition f : recquis) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setProductId(new Produit(f.getProductId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setPrixDeVenteList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.RECQUISITION.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rrecquis.add(f);
                    }
                }
                if (!rrecquis.isEmpty()) {
                    List<List<Recquisition>> partitions = Util.partitions(rrecquis, 12);
                    for (List<Recquisition> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<PrixDeVente> prices = session.findPrixDeVentes();
                List<PrixDeVente> rprices = new ArrayList<>();
                for (PrixDeVente f : prices) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setRecquisitionId(new Recquisition(f.getRecquisitionId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.PRIXDEVENTE.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rprices.add(f);
                    }
                }
                if (!rprices.isEmpty()) {
                    List<List<PrixDeVente>> partitions = Util.partitions(rprices, 12);
                    for (List<PrixDeVente> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Client> clients = session.findClients();
                List<Client> rclients = new ArrayList<>();
                for (Client f : clients) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setVenteList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.CLIENT.name());
                        f.setAretirerList(null);
                        f.setClientAppartenirList(null);
                        f.setRetourMagasinList(null);
                        f.setAction(Constants.ACTION_CREATE);
                        rclients.add(f);
                    }
                }
                if (!rclients.isEmpty()) {
                    List<List<Client>> partitions = Util.partitions(rclients, 12);
                    for (List<Client> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Vente> vs = session.findVentes();
                List<Vente> vss = new ArrayList<>();
                for (Vente v : vs) {
                    if (v == null) {
                        continue;
                    }
                    boolean yes = JournalDelegate.isDataSynced(v.getUid());
                    if (!yes) {
                        v.setClientId(new Client(v.getClientId().getUid()));
                        v.setFrom(pref.get("userid", ""));
                        v.setLigneVenteList(null);
                        v.setTaxerList(null);
                        v.setType(Tables.VENTE.name());
                        v.setAction(Constants.ACTION_CREATE);
                        vss.add(v);
                    }
                }
                if (!vss.isEmpty()) {
                    List<List<Vente>> partitions = Util.partitions(vss, 16);
                    for (List<Vente> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(1000));
                List<LigneVente> lignes = session.findLigneVentes();
                List<LigneVente> rlignes = new ArrayList<>();
                for (LigneVente f : lignes) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setProductId(new Produit(f.getProductId().getUid()));
                        f.setReference(new Vente(f.getReference().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.LIGNEVENTE.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rlignes.add(f);
                    }
                }
                if (!rlignes.isEmpty()) {
                    List<List<LigneVente>> partitions = Util.partitions(rlignes, 16);
                    for (List<LigneVente> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Aretirer> aretirs = session.findAretirer();
                List<Aretirer> aretires = new ArrayList<>();
                for (Aretirer f : aretirs) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setClientId(new Client(f.getClientId().getUid()));
                        f.setLigneVenteId(new LigneVente(f.getLigneVenteId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.ARETIRER.name());
                        f.setAction(Constants.ACTION_CREATE);
                        aretires.add(f);
                    }
                }
                if (!aretires.isEmpty()) {
                    List<List<Aretirer>> partitions = Util.partitions(aretires, 12);
                    for (List<Aretirer> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<CompteTresor> comptes = session.findCompteTresors();
                List<CompteTresor> rcomptes = new ArrayList<>();
                for (CompteTresor f : comptes) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setOperationList(null);
                        f.setTraisorerieList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.COMPTETRESOR.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rcomptes.add(f);
                    }
                }
                if (!rcomptes.isEmpty()) {
                    List<List<CompteTresor>> partitions = Util.partitions(rcomptes, 12);
                    for (List<CompteTresor> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Traisorerie> trais = session.findTraisoreries();
                List<Traisorerie> tresor = new ArrayList<>();
                for (Traisorerie f : trais) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setTresorId(new CompteTresor(f.getTresorId().getUid()));
                        f.setOperationList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.TRAISORERIE.name());
                        f.setAction(Constants.ACTION_CREATE);
                        tresor.add(f);
                    }
                }
                if (!tresor.isEmpty()) {
                   List<List<Traisorerie>> partitions = Util.partitions(tresor,16);
                    for (List<Traisorerie> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Depense> depenses = session.findDepenses();
                List<Depense> rdepense = new ArrayList<>();
                for (Depense f : depenses) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setOperationList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.DEPENSE.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rdepense.add(f);
                    }
                }
                if (!rdepense.isEmpty()) {
                   List<List<Depense>> partitions = Util.partitions(rdepense, 12);
                    for (List<Depense> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Operation> operations = session.findOperations();
                List<Operation> ropers = new ArrayList<>();
                for (Operation f : operations) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setDepenseId(new Depense(f.getDepenseId().getUid()));
                        f.setCaisseOpId(new Traisorerie(f.getCaisseOpId().getUid()));
                        f.setTresorId(new CompteTresor(f.getTresorId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.OPERATION.name());
                        f.setAction(Constants.ACTION_CREATE);
                        ropers.add(f);
                    }
                }
                if (!ropers.isEmpty()) {
                   List<List<Operation>> partitions = Util.partitions(ropers, 12);
                    for (List<Operation> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<RetourMagasin> retour = session.findRetourMagasins();
                List<RetourMagasin> retourm = new ArrayList<>();
                for (RetourMagasin f : retour) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setClientId(new Client(f.getClientId().getUid()));
                        f.setLigneVenteId(new LigneVente(f.getLigneVenteId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.RETOURMAGASIN.name());
                        f.setAction(Constants.ACTION_CREATE);
                        retourm.add(f);
                    }
                }
                if (!retourm.isEmpty()) {
                    List<List<RetourMagasin>> partitions = Util.partitions(retourm, 12);
                    for (List<RetourMagasin> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<RetourDepot> retourd = session.findRetourDepots();
                List<RetourDepot> rretourd = new ArrayList<>();
                for (RetourDepot f : retourd) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setDestockerId(new Destocker(f.getDestockerId().getUid()));
                        f.setMesureId(new Mesure(f.getMesureId().getUid()));
                        f.setRecquisitionId(new Recquisition(f.getRecquisitionId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.RETOURDEPOT.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rretourd.add(f);
                    }
                }
                if (!rretourd.isEmpty()) {
                   List<List<RetourDepot>> partitions = Util.partitions(rretourd, 12);
                    for (List<RetourDepot> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<ClientOrganisation> clientorgs = session.findClientOrganisations();
                List<ClientOrganisation> cleintorgas = new ArrayList<>();
                for (ClientOrganisation f : clientorgs) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setClientAppartenirList(null);
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.CLIENTORGANISATION.name());
                        f.setAction(Constants.ACTION_CREATE);
                        cleintorgas.add(f);
                    }
                }
                if (!cleintorgas.isEmpty()) {
                    List<List<ClientOrganisation>> partitions = Util.partitions(cleintorgas, 12);
                    for (List<ClientOrganisation> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<ClientAppartenir> clientAppart = session.findClientAppartenirs();
                List<ClientAppartenir> clientApar = new ArrayList<>();
                for (ClientAppartenir f : clientAppart) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setClientOrganisationId(new ClientOrganisation(f.getClientOrganisationId().getUid()));
                        f.setClientId(new Client(f.getClientId().getUid()));
                        f.setFrom(pref.get("userid", "")); 
                        f.setType(Tables.CLIENTAPPARTENIR.name());
                        f.setAction(Constants.ACTION_CREATE);
                        clientApar.add(f);
                    }
                }
                if (!clientApar.isEmpty()) {
                   List<List<ClientAppartenir>> partitions = Util.partitions(clientApar, 12);
                    for (List<ClientAppartenir> partition : partitions) {
                        publisher.submit(partition);
                    }
                }
                Thread.sleep(Duration.ofMillis(SLEEP));
                List<Facture> factures = session.findFactures();
                List<Facture> rfactures = new ArrayList<>();
                for (Facture f : factures) {
                    boolean synced = JournalDelegate.isDataSynced(f.getUid());
                    if (!synced) {
                        f.setOrganisId(new ClientOrganisation(f.getOrganisId().getUid()));
                        f.setFrom(pref.get("userid", ""));
                        f.setType(Tables.FACTURE.name());
                        f.setAction(Constants.ACTION_CREATE);
                        rfactures.add(f);
                    }
                }
                if (!rfactures.isEmpty()) {
                   List<List<Facture>> partitions = Util.partitions(rfactures, 12);
                    for (List<Facture> partition : partitions) {
                        publisher.submit(partition);
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, 4, 30, TimeUnit.SECONDS);
        return ses.isTerminated();
    }

    public void startChecking() {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    private void checkUpdate() {
        try {
            String version = pref.get("ksf_version", null);
            if (version == null) {
                pref.put("ksf_version", Constants.APP_VERSION);
                version = Constants.APP_VERSION;
            }
            Response<Module> update = kazisafe.checkUpdates().execute();
            if (update.isSuccessful()) {
                Module mod = update.body();
                String modver = mod.getVersion();
                if (!modver.equalsIgnoreCase(version)) {
                    notifyNewUpate(mod);
                } else {
                    notifySameUpate(mod);
                }
            }

        } catch (IOException ex) {
            //Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        syncup();
    }

    public void restart() {
        syncup();
    }

    public void shutdown() {
        ses.shutdown();
    }

    public void setOnUpdateVersionListener(OnUpdateVersionListener onUpdateVersionListener) {
        this.onUpdateVersionListener = onUpdateVersionListener;
    }

    private void notifyNewUpate(Module m) {
        if (onUpdateVersionListener != null) {
            onUpdateVersionListener.onNewUpdate(m);
        }
    }

    private void notifySameUpate(Module m) {
        if (onUpdateVersionListener != null) {
            onUpdateVersionListener.onSameUpdate(m);
        }
    }

    @Override
    public Boolean call() throws Exception {
        try {
            List<Category> cats = CategoryDelegate.findCategories();
            List<Category> lcat = new ArrayList<>();
            for (Category f : cats) {

                f.setProduitList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.CATEGORY.name());
                f.setAction(Constants.ACTION_CREATE);
                lcat.add(f);

            }
            if (!lcat.isEmpty()) {
                publisher.submit(lcat);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Produit> pros = ProduitDelegate.findProduits();
            List<Produit> lpo = new ArrayList<>();
            for (Produit f : pros) {

                Category c = f.getCategoryId();
                f.setCategoryId(new Category(c.getUid()));
                f.setImage(null);
                f.setDestockerList(null);
                f.setLigneVenteList(null);
                f.setMesureList(null);
                f.setRecquisitionList(null);
                f.setStockerList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.PRODUIT.name());
                f.setAction(Constants.ACTION_CREATE);
                lpo.add(f);

            }
            if (!lpo.isEmpty()) {
                publisher.submit(lpo);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Mesure> mesures = MesureDelegate.findMesures();
            List<Mesure> rmesures = new ArrayList<>();
            for (Mesure f : mesures) {

                Produit p = f.getProduitId();
                f.setProduitId(new Produit(p.getUid()));
                f.setDestockerList(null);
                f.setLigneVenteList(null);
                f.setPrixDeVenteList(null);
                f.setRecquisitionList(null);
                f.setStockerList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.MESURE.name());
                f.setAction(Constants.ACTION_CREATE);
                rmesures.add(f);

            }
            if (!rmesures.isEmpty()) {
                publisher.submit(rmesures);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Fournisseur> fssr = FournisseurDelegate.findFournisseurs();
            List<Fournisseur> fss = new ArrayList<>();
            for (Fournisseur f : fssr) {

                f.setLivraisonList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.FOURNISSEUR.name());
                f.setAction(Constants.ACTION_CREATE);
                fss.add(f);

            }
            if (!fss.isEmpty()) {
                publisher.submit(fss);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Livraison> livres = LivraisonDelegate.findLivraisons();
            List<Livraison> livraizone = new ArrayList<>();
            for (Livraison f : livres) {

                f.setFournId(new Fournisseur(f.getFournId().getUid()));
                f.setStockerList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.LIVRAISON.name());
                f.setAction(Constants.ACTION_CREATE);
                livraizone.add(f);

            }
            if (!livraizone.isEmpty()) {
                publisher.submit(livraizone);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Stocker> stockers = StockerDelegate.findStockers();
            List<Stocker> stockerss = new ArrayList<>();
            for (Stocker f : stockers) {

                f.setProductId(new Produit(f.getProductId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setLivraisId(new Livraison(f.getLivraisId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.STOCKER.name());
                f.setAction(Constants.ACTION_CREATE);
                stockerss.add(f);

            }
            if (!stockerss.isEmpty()) {
                publisher.submit(stockerss);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Destocker> destockers = DestockerDelegate.findDestockers();
            List<Destocker> destoks = new ArrayList<>();
            for (Destocker f : destockers) {

                f.setProductId(new Produit(f.getProductId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.DESTOCKER.name());
                f.setAction(Constants.ACTION_CREATE);
                destoks.add(f);

            }
            if (!destoks.isEmpty()) {
                publisher.submit(destoks);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Recquisition> recquis = RecquisitionDelegate.findRecquisitions();
            List<Recquisition> rrecquis = new ArrayList<>();
            for (Recquisition f : recquis) {

                f.setProductId(new Produit(f.getProductId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setPrixDeVenteList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.RECQUISITION.name());
                f.setAction(Constants.ACTION_CREATE);
                rrecquis.add(f);

            }
            if (!rrecquis.isEmpty()) {
                publisher.submit(rrecquis);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<PrixDeVente> prices = PrixDeVenteDelegate.findPrixDeVentes();
            List<PrixDeVente> rprices = new ArrayList<>();
            for (PrixDeVente f : prices) {

                f.setRecquisitionId(new Recquisition(f.getRecquisitionId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.PRIXDEVENTE.name());
                f.setAction(Constants.ACTION_CREATE);
                rprices.add(f);

            }
            if (!rprices.isEmpty()) {
                publisher.submit(rprices);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Client> clients = ClientDelegate.findClients();
            List<Client> rclients = new ArrayList<>();
            for (Client f : clients) {
                f.setVenteList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.CLIENT.name());
                f.setAretirerList(null);
                f.setClientAppartenirList(null);
                f.setRetourMagasinList(null);
                f.setAction(Constants.ACTION_CREATE);
                rclients.add(f);

            }
            if (!rclients.isEmpty()) {
                publisher.submit(rclients);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Vente> vs = VenteDelegate.findVentes();
            System.out.println("VENTES " + vs);
            List<Vente> vss = new ArrayList<>();
            for (Vente v : vs) {
                v.setClientId(new Client(v.getClientId().getUid()));
                v.setFrom(pref.get("userid", ""));
                v.setLigneVenteList(null);
                v.setTaxerList(null);
                v.setType(Tables.VENTE.name());
                v.setAction(Constants.ACTION_CREATE);
                vss.add(v);

            }

            if (!vs.isEmpty()) {
                publisher.submit(vs);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<LigneVente> lignes = LigneVenteDelegate.findLigneVentes();
            List<LigneVente> rlignes = new ArrayList<>();
            for (LigneVente f : lignes) {
                f.setProductId(new Produit(f.getProductId().getUid()));
                f.setReference(new Vente(f.getReference().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.LIGNEVENTE.name());
                f.setAction(Constants.ACTION_CREATE);
                rlignes.add(f);

            }
            if (!rlignes.isEmpty()) {
                publisher.submit(rlignes);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Aretirer> aretirs = AretirerDelegate.findAretirers();
            List<Aretirer> aretires = new ArrayList<>();
            for (Aretirer f : aretirs) {
                f.setClientId(new Client(f.getClientId().getUid()));
                f.setLigneVenteId(new LigneVente(f.getLigneVenteId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.ARETIRER.name());
                f.setAction(Constants.ACTION_CREATE);
                aretires.add(f);

            }
            if (!aretires.isEmpty()) {
                publisher.submit(aretires);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<CompteTresor> comptes = CompteTresorDelegate.findCompteTresors();
            List<CompteTresor> rcomptes = new ArrayList<>();
            for (CompteTresor f : comptes) {
                f.setOperationList(null);
                f.setTraisorerieList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.COMPTETRESOR.name());
                f.setAction(Constants.ACTION_CREATE);
                rcomptes.add(f);

            }
            if (!rcomptes.isEmpty()) {
                publisher.submit(rcomptes);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Traisorerie> trais = TraisorerieDelegate.findTraisoreries();
            List<Traisorerie> tresor = new ArrayList<>();
            for (Traisorerie f : trais) {
                f.setTresorId(new CompteTresor(f.getTresorId().getUid()));
                f.setOperationList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.TRAISORERIE.name());
                f.setAction(Constants.ACTION_CREATE);
                tresor.add(f);

            }
            if (!tresor.isEmpty()) {
                publisher.submit(tresor);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Depense> depenses = DepenseDelegate.findDepenses();
            List<Depense> rdepense = new ArrayList<>();
            for (Depense f : depenses) {
                f.setOperationList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.DEPENSE.name());
                f.setAction(Constants.ACTION_CREATE);
                rdepense.add(f);

            }
            if (!rdepense.isEmpty()) {
                publisher.submit(rdepense);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Operation> operations = OperationDelegate.findOperations();
            List<Operation> ropers = new ArrayList<>();
            for (Operation f : operations) {
                f.setDepenseId(new Depense(f.getDepenseId().getUid()));
                f.setCaisseOpId(new Traisorerie(f.getCaisseOpId().getUid()));
                f.setTresorId(new CompteTresor(f.getTresorId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.OPERATION.name());
                f.setAction(Constants.ACTION_CREATE);
                ropers.add(f);

            }
            if (!ropers.isEmpty()) {
                publisher.submit(ropers);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<RetourMagasin> retour = RetourMagasinDelegate.findRetourMagasins();
            List<RetourMagasin> retourm = new ArrayList<>();
            for (RetourMagasin f : retour) {
                f.setClientId(new Client(f.getClientId().getUid()));
                f.setLigneVenteId(new LigneVente(f.getLigneVenteId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.RETOURMAGASIN.name());
                f.setAction(Constants.ACTION_CREATE);
                retourm.add(f);

            }
            if (!retourm.isEmpty()) {
                publisher.submit(retourm);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<RetourDepot> retourd = RetourDepotDelegate.findRetourDepots();
            List<RetourDepot> rretourd = new ArrayList<>();
            for (RetourDepot f : retourd) {
                f.setDestockerId(new Destocker(f.getDestockerId().getUid()));
                f.setMesureId(new Mesure(f.getMesureId().getUid()));
                f.setRecquisitionId(new Recquisition(f.getRecquisitionId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.RETOURDEPOT.name());
                f.setAction(Constants.ACTION_CREATE);
                rretourd.add(f);

            }
            if (!rretourd.isEmpty()) {
                publisher.submit(rretourd);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<ClientOrganisation> clientorgs = ClientOrganisationDelegate.findClientOrganisations();
            List<ClientOrganisation> cleintorgas = new ArrayList<>();
            for (ClientOrganisation f : clientorgs) {
                f.setClientAppartenirList(null);
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.CLIENTORGANISATION.name());
                f.setAction(Constants.ACTION_CREATE);
                cleintorgas.add(f);

            }
            if (!cleintorgas.isEmpty()) {
                publisher.submit(cleintorgas);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<ClientAppartenir> clientAppart = ClientAppartenirDelegate.findClientAppartenirs();
            List<ClientAppartenir> clientApar = new ArrayList<>();
            for (ClientAppartenir f : clientAppart) {
                f.setClientOrganisationId(new ClientOrganisation(f.getClientOrganisationId().getUid()));
                f.setClientId(new Client(f.getClientId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.CLIENTAPPARTENIR.name());
                f.setAction(Constants.ACTION_CREATE);
                clientApar.add(f);

            }
            if (!clientApar.isEmpty()) {
                publisher.submit(clientApar);
            }
            Thread.sleep(Duration.ofMillis(SLEEP));
            List<Facture> factures = FactureDelegate.findFactures();
            List<Facture> rfactures = new ArrayList<>();
            for (Facture f : factures) {
                f.setOrganisId(new ClientOrganisation(f.getOrganisId().getUid()));
                f.setFrom(pref.get("userid", ""));
                f.setType(Tables.FACTURE.name());
                f.setAction(Constants.ACTION_CREATE);
                rfactures.add(f);

            }
            if (!rfactures.isEmpty()) {
                publisher.submit(rfactures);
            }
            return true;
        } catch (InterruptedException ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
