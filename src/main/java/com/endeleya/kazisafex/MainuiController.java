/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

//import com.launchdarkly.eventsource.EventHandler;
//import com.launchdarkly.eventsource.EventSource;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.StockerDelegate;
import delegates.VenteDelegate;
import data.core.KazisafeServiceFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

import data.Abonnement;
import data.BaseModel;
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
import data.LoginResult;
import data.Mesure;
import data.Module;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Refresher;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.User;
import data.Vente;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.time.DateUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static tools.Constants.CAISSES;
import static tools.Constants.PARAMETRES;
import static tools.Constants.POS;
import static tools.Constants.REPPORTS;
import static tools.Constants.STORAGE;
import tools.FileUtils;
import services.PlatformUtil;
import tools.MainUI;
import tools.NetLoockup;
import tools.OnUpdateVersionListener;
import tools.SyncEndpoint;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import tools.Constants;
import data.helpers.Role;
import data.network.Kazisafe;
import data.helpers.Token;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import services.SafeConnectionFactory;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class MainuiController implements Initializable {

    Flow.Subscriber<Set<BaseModel>> saver = new Flow.Subscriber<>() {
        private Flow.Subscription abonnement;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            System.out.println("Final souscriber onSubc");
            this.abonnement = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Set<BaseModel> items) {

            save(items);
            System.out.println("fin fonction request now");
            abonnement.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Teminaison de la conso");
        }
    };

    public static MainuiController getInstance() {
        if (instance == null) {
            instance = new MainuiController();
        }
        return instance;
    }

    public static ConcurrentHashMap<String, Object> dataCache;
    private static final double LAYOUTY = 59.0;
    private static final double LAYOUTX = 30.0;
    @FXML
    private Pane update_pane;

    @FXML
    private ImageView img_profile, app_image;

    @FXML
    private TextField searchField;
    @FXML
    private Label user_connected;
    @FXML
    private ImageView home;
    @FXML
    private ImageView stockage;
    @FXML
    private ImageView pos;
    @FXML
    private ImageView agents;
    @FXML
    private ImageView rapport;
    @FXML
    private ImageView compagnie;
    @FXML
    private ImageView products_gate;
    @FXML
    private ImageView caisse, img_iconify, img_close, agrandir;
    SyncEngine se;

    @FXML
    private ImageView image_title, download_imgbtn;

    @FXML
    private ImageView parametre, aide;
    @FXML
    private Label pane_title, label_version, label_status;
    @FXML
    private AnchorPane mainpane;
    @FXML
    private AnchorPane showPane;
    @FXML
    private AreaChart<?, ?> venteChart;
    @FXML
    private Label svente;
    @FXML
    private Label stresor;
    @FXML
    private PieChart piepane;
    @FXML
    private Label sdepense;
    @FXML
    private Label screance;
    @FXML
    private Label entrep_name;
    @FXML
    private Label depense_proportion;
    @FXML
    Label txt_region, appName;
    @FXML
    VBox vbox_menu;
    @FXML
    Hyperlink install_update_link;
    @FXML
    private ProgressBar download_update_pgb;
    @FXML
    private ProgressIndicator douwnload_update_pgi;
    ResourceBundle bundle;

    String token;
    private String phone;
    private String region, role, rccm;
    private String entrepiseId;
    private LoginResult loginResult;
    private Module newModule;
    Preferences pref;
    double taux;
    int itemPerPage = 15;
    String localPath;
    Kazisafe kazisafe;
    User user;
    Entreprise entreprisex;

    private String CURRENT_VIEW = tools.Constants.MAIN;
    boolean isConnected = false;

    private static MainuiController instance;
    private static final int BATCH_SIZE = 10;
    private Set<BaseModel> buffer;

    NetLoockup network;

    public MainuiController() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        instance = this;
        buffer = new HashSet<>();
        dataCache = new ConcurrentHashMap<>();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);

        appName.setText("Kazisafe");
        role = pref.get("priv", "Non disponible");
        installTooltips();
        Tooltip.install(img_profile, new Tooltip(role));
        Tooltip.install(img_iconify, new Tooltip(bundle.getString("xtooltip.redu_ire")));
        Tooltip.install(agrandir, new Tooltip(bundle.getString("xtooltip.agran_dir")));
        Tooltip.install(img_close, new Tooltip(bundle.getString("xtooltip.fer_me")));
        Tooltip.install(download_imgbtn, new Tooltip("Télécharger les mises à jours"));
        Tooltip.install(aide, new Tooltip("Ouvrir le fichier d'aide"));
        Tooltip.install(app_image, new Tooltip("© Endeleya Corp. Kazisafe v" + pref.get("ksf_version", tools.Constants.APP_VERSION)));
        douwnload_update_pgi.setVisible(false);
        download_update_pgb.setVisible(false);
        install_update_link.setVisible(false);
        localPath = MainUI.cPath("/Media/Update");
        pref.put("ksf_version", tools.Constants.APP_VERSION);
        taux = pref.getDouble("taux2change", 2300);
        network = new NetLoockup();

        // TODO
    }

    SyncEndpoint sep;
    String URL;

    public void setToken(String token) {
        this.token = token;
        kazisafe = KazisafeServiceFactory.createService(token);

        KazisafeServiceFactory.setOnTokenRefreshCallback((Token var1) -> {
            MainuiController.this.token = var1.getToken();
            System.err.println("Nouveau Token "+var1.getToken());
            pref.put("token", var1.getToken());
        });

        network.setOnNetworkStateChangeListener((boolean isReachable) -> {
            System.err.println("NET_LOOK : " + isReachable);
            if (isReachable) {
                System.err.println("NET_LOOK - REACABLE: " + isReachable);
                if (!isConnected) {
                    isConnected = true;
                    System.err.println("MAINUIC_NETWORK_IS_ON : Good states websocket can start");
                    kazisafe = KazisafeServiceFactory.createService(token);
                    KazisafeServiceFactory.setOnTokenRefreshCallback((Token var1) -> {
                        MainuiController.this.token = var1.getToken();
                        pref.put("token", var1.getToken());
                    });
                }
            } else {
                System.err.println("MAINUIC_NETWORK_IS_OFF : Bad states websocket can't start");
                isConnected = false;
            }
            pref.putBoolean(NetLoockup.NETWORK_STATUS, isReachable);
        });
        searchField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if(t1){
                    if(searchField.getText().isEmpty()){
                        searchField.selectAll();
                    }
                }
            }
        });
    }

    @FXML
    public void iconify(Event evt) {
        MainUI.minimize();
    }

    public void cleanSearchBar() {
        searchField.clear();
        searchField.requestFocus();
    }

    public void reinitSearchBar() {
        searchField.selectAll();
    }

    @FXML
    public void sync(Event e) {
        if (se != null) {
            se.start();
        } else {
            MainUI.notify(null, "Erreur", "Echec de lancement de la synchronisation, verifiez la connection internet", 4, "error");
        }
    }

    public List<Vente> getVentes(Date date) {
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        List<Vente> vts = VenteDelegate.findAllByDateInterval(d1, kesho);//db.findAllByDateInterval(Vente.class, d1, kesho);
        if (vts == null) {
            return null;
        }
        return vts;
    }

    public List<Vente> getVentesDebt(Date date) {
        Calendar cexp = Calendar.getInstance();
        cexp.setTime(date);
        cexp.set(Calendar.HOUR, 0);
        cexp.set(Calendar.MINUTE, 59);
        cexp.set(Calendar.SECOND, 59);
        cexp.set(Calendar.MILLISECOND, 0);
        Date date1 = DateUtils.addDays(date, 1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(cexp.getTime(), date1);
        //Util.getByDay(db.findAll(), new Date());

        return vts;
    }

    public List<Vente> getVentesDebt(Date date, String region) {
        Calendar cexp = Calendar.getInstance();
        cexp.setTime(date);
        cexp.set(Calendar.HOUR, 0);
        cexp.set(Calendar.MINUTE, 59);
        cexp.set(Calendar.SECOND, 59);
        cexp.set(Calendar.MILLISECOND, 0);
        Date date1 = DateUtils.addDays(date, 1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(cexp.getTime(), date1, region);
        //Util.getByDay(db.findAll(), new Date());

        return vts;
    }

    public List<Vente> getVentesInMoth(String month) {
        List<Vente> result = new ArrayList<>();
        List<Vente> vts = VenteDelegate.findVentes();//db.findAll(Vente.class);
        for (Vente vt : vts) {
            String dv = tools.Constants.YEAR_AND_MONTH_FORMAT.format(vt.getDateVente());
            if (dv.equals(month)) {
                result.add(vt);
            }
        }
        return result;
    }

    public List<Operation> getOpsInMonth(String month) {
        List<Operation> result = new ArrayList<>();
        List<Operation> vts = OperationDelegate.findOperations();//db.findAll(Operation.class);
        if (vts != null) {
            for (Operation vt : vts) {
                String dv = tools.Constants.YEAR_AND_MONTH_FORMAT.format(vt.getDate());
                if (dv.equals(month)) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Vente> getVentesInMoth(String month, String region) {
        List<Vente> result = new ArrayList<>();
        List<Vente> vts = VenteDelegate.findVentes(region);// db.findAllByRegion(Vente.class, region);
        if (vts != null) {
            for (Vente vt : vts) {
                String dv = tools.Constants.YEAR_AND_MONTH_FORMAT.format(vt.getDateVente());
                if (dv.equals(month)) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Operation> getOpsInMonth(String month, String region) {
        List<Operation> result = new ArrayList<>();
        List<Operation> vts = OperationDelegate.findOperations(region);//db.findAllByRegion(Operation.class, region);
        if (vts != null) {
            for (Operation vt : vts) {
                String dv = tools.Constants.YEAR_AND_MONTH_FORMAT.format(vt.getDate());
                if (dv.equals(month) && vt.getRegion().equalsIgnoreCase(region)) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Operation> getOps(Date date) {
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        List<Operation> vts = OperationDelegate.findByDateInterval(d1, kesho);
        return vts;
    }

    public List<Operation> getOps(Date date, String region) {
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        List<Operation> vts = OperationDelegate.findByDateInterval(d1, kesho, region);
        return vts;
    }

    public List<Vente> getVentes(Date date, String region) {
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        List<Vente> vts = VenteDelegate.findAllByDateInterval(d1, kesho, region);//db.findAllByDateIntervalInRegion(Vente.class, d1, kesho, region);
        return vts;
    }

    @FXML
    public void enlarge(Event e) {
        MainUI.enlarge();
    }

    public void dashCardVente() {
        Date date = new Date();
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            double sumSales = VenteDelegate.sumVente(d1, kesho, taux);
            //Util.sumSales(ventes, taux);
            System.out.println("Sum sale " + sumSales);
            svente.setText("$ " + BigDecimal.valueOf(sumSales).setScale(1, RoundingMode.FLOOR).doubleValue());
        } else {
            // List<Vente> ventes = getVentes(new Date(), region);
            double sumSales = VenteDelegate.sumVente(d1, kesho, region, taux);
            svente.setText("$ " + BigDecimal.valueOf(sumSales).setScale(1, RoundingMode.FLOOR).doubleValue());
        }
    }

    public void creanceToday() {
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            List<Vente> ventes = getVentesDebt(new Date());
            double sumSales = Util.sumCreditSales(ventes, taux);
            screance.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        } else {
            List<Vente> ventes = getVentesDebt(new Date(), region);
            double sumSales = Util.sumCreditSales(ventes, taux);
            screance.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        }
    }

    public void dashCardResult() {
        Date date = new Date();
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            double sumSales = VenteDelegate.sumVente(d1, kesho, taux);
            double achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho, null);
            double sum = VenteDelegate.sumExpenses(d1, kesho, taux);
            double exp = achat + sum;
            double result = sumSales - exp;
            stresor.setText("$ " + BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN));
        } else {
            double sumSales = VenteDelegate.sumVente(d1, kesho, region, taux);
            double achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho, region);
            double sum = VenteDelegate.sumExpenses(d1, kesho, region, taux);
            double exp = achat + sum;
            double result = sumSales - exp;
            stresor.setText("$ " + BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN));
        }
    }

    public double dashAchat(List<Vente> vents) {
        double sB = 0;
        for (Vente vent : vents) {
            List<LigneVente> lvs = LigneVenteDelegate.findByReference(vent.getUid());
            //db.findAll("reference.uid", vent.getUid());
            double sm = 0;
            if (lvs == null) {
                continue;
            }
            for (LigneVente lv : lvs) {
                if (lv.getNumlot() == null) {
                    continue;
                }
                Vente vref = lv.getReference();
                if (vref == null) {
                    continue;
                }
                Vente v = VenteDelegate.findVente(vref.getUid());
                double smv = v.getMontantCdf() + v.getMontantUsd() + (v.getMontantDette() == null
                        ? 0 : v.getMontantDette());
                if (smv == 0) {
                    continue;
                }
                Produit p = lv.getProductId();
                List<Stocker> stocks = StockerDelegate.findDescSortedByDateStock(p.getUid());
                if (stocks.isEmpty()) {
                    continue;
                }
                Stocker s = stocks.get(0);
                Mesure mstok = s.getMesureId();

                Mesure mreel = MesureDelegate.findMesure(mstok.getUid());
                if (mreel == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());
                    mreel = mesures.get(0);
                }
                double qpcsti = mreel.getQuantContenu();
                double qpcst = qpcsti == 0 ? 1 : mreel.getQuantContenu();
                double coutAchat = s.getCoutAchat();
                double pupc = coutAchat / qpcst;
                Mesure mvndu = lv.getMesureId();
                Mesure mreelv = MesureDelegate.findMesure(mvndu.getUid());
                if (mreelv == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());
                    mreelv = mesures.get(0);
                }
                double vndu = mreelv.getQuantContenu();
                double qtvndupc = vndu * lv.getQuantite();
                double sv = pupc * qtvndupc;
                sm += sv;
            }
            sB += sm;
        }
        System.out.println("Achat - " + sB);
        return sB;
    }

    public double dashAchat(List<Vente> vents, String region) {
        double sB = 0;
        for (Vente vent : vents) {
            List<LigneVente> lvs = LigneVenteDelegate.findByReference(vent.getUid());
            double sm = 0;
            if (lvs == null) {
                continue;
            }
            for (LigneVente lv : lvs) {
                if (lv.getNumlot() == null) {
                    continue;
                }
                Vente v = VenteDelegate.findVente(lv.getReference().getUid());
                double smv = v.getMontantCdf() + v.getMontantUsd() + (v.getMontantDette() == null
                        ? 0 : v.getMontantDette());
                if (smv == 0) {
                    continue;
                }
                Produit p = lv.getProductId();
                List<Stocker> stocks = StockerDelegate.findDescSortedByDateStock(p.getUid(), region);
                if (stocks.isEmpty()) {
                    continue;
                }
                Stocker s = stocks.get(0);
                Mesure mstok = s.getMesureId();
                Mesure mzr = MesureDelegate.findMesure(mstok.getUid());
                if (mzr == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());
                    mzr = mesures.get(0);
                }
                double qpcst = mzr.getQuantContenu();
                double coutAchat = s.getCoutAchat();
                double pupc = coutAchat / qpcst;
                Mesure mvndu = lv.getMesureId();
                Mesure mzir = MesureDelegate.findMesure(mvndu.getUid());
                if (mzir == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());
                    mzir = mesures.get(0);
                }
                double vndu = mzir.getQuantContenu();
                double qtvndupc = vndu * lv.getQuantite();
                double sv = pupc * qtvndupc;
                sm += sv;
            }
            sB += sm;
        }
        return sB;
    }

    public void dashCardDepense() {
        Date date = new Date();
        Date kesho = DateUtils.addDays(date, 1);
        Calendar leo = Calendar.getInstance();
        leo.setTime(date);
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);
        Date d1 = leo.getTime();
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            double achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho, null);
            double sum = VenteDelegate.sumExpenses(d1, kesho, taux);
            double exp = achat + sum;
            sdepense.setText("$ " + BigDecimal.valueOf(exp).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            if (exp != 0) {
                double p = (sum / exp) * 100;
                p = BigDecimal.valueOf(p).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), p, "%", sum));
            } else {
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), "0.0", "%", 0.0));
            }
        } else {
            double achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho, region);
            double sum = VenteDelegate.sumExpenses(d1, kesho, region, taux);
            double exp = achat + sum;
            sdepense.setText("$ " + BigDecimal.valueOf(exp).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            if (exp != 0) {
                double p = (sum / exp) * 100;
                p = BigDecimal.valueOf(p).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), p, "%", sum));
            } else {
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), "0.0", "%", 0.0));
            }
        }
    }

    public void summarise() {

        System.out.println("is summary called");
        dashCardVente();
        dashCardDepense();
        dashCardResult();
        loadSaleChart();
        loadProChart();
        creanceToday();

    }
    String reg = null;

    public void metrify() {
        venteChart.setLegendVisible(true);
        XYChart.Series serie_vente = new XYChart.Series();
        XYChart.Series serie_prixderevient = new XYChart.Series();
        XYChart.Series serie_resultat = new XYChart.Series();
        serie_vente.setName(bundle.getString("xgraph.seri1_vente").trim());
        serie_prixderevient.setName(bundle.getString("xgraph.seri2_depens").trim());
        serie_resultat.setName(bundle.getString("xgraph.seri3_marg").trim());

        Calendar leo = Calendar.getInstance();
        leo.setTime(new Date());
        leo.set(Calendar.HOUR, 0);
        leo.set(Calendar.MINUTE, 0);
        leo.set(Calendar.SECOND, 0);
        leo.set(Calendar.MILLISECOND, 0);

        int month = leo.get(Calendar.MONTH) + 1;

//        Date mwanzo = DateUtils.addMonths(d1, -month);
//        if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
//            reg = region;
//        }
//        HashMap<String, Double> datax = db.getSalesInPerod(mwanzo, d1, null, taux);
//        System.out.println("Dataxsf " + datax.size());
//        datax.forEach((x, y) -> {
//            System.out.println(">>>>>>>>>>>> Value Mois " + x + " : " + y);
//            serie_vente.getData().add(new XYChart.Data<>(x, y));
//            Double mca = VenteDelegate.sumCoutAchatArticleVendu(x, null);
//            double dep = VenteDelegate.sumExpenses(x, null, taux);
//            double prixrevient = mca + dep;
//            serie_prixderevient.getData().add(new XYChart.Data<>(x, prixrevient));
//            double result = y - prixrevient;//resultat
//            serie_resultat.getData().add(new XYChart.Data<>(x, result));
//        });
        System.out.println("mois en cours " + month);
        for (int i = 1; i <= month; i++) {
            try {
                String suffix = String.format("%02d", i);
                String firstday = leo.get(Calendar.YEAR) + "-" + suffix + "-01";
                Date date1 = Constants.dateFormater.parse(firstday);
                Calendar d2 = Calendar.getInstance();
                d2.setTime(date1);
                int maxday = d2.getActualMaximum(Calendar.DAY_OF_MONTH);
                String lastday = leo.get(Calendar.YEAR) + "-" + suffix + "-" + maxday;
                Date date2 = Constants.dateFormater.parse(lastday);

                String moix = getMonthName(lastday.substring(0, lastday.lastIndexOf("-")));
                System.out.println("lonog " + moix);
                if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                    System.out.println("vente ajour " + lastday);
                    double sumSales = VenteDelegate.sumVente(date1, date2, taux);

                    double achat = VenteDelegate.sumCoutAchatArticleVendu(date1, date2, null);
                    double sum = VenteDelegate.sumExpenses(date1, date2, taux);
                    double prixrevient = achat + sum;
                    double result = sumSales - prixrevient;
                    serie_vente.getData().add(new XYChart.Data<>(moix, sumSales));
                    serie_prixderevient.getData().add(new XYChart.Data<>(moix, prixrevient));
                    serie_resultat.getData().add(new XYChart.Data<>(moix, result));
                } else {
                    System.out.println("vente ajour reg" + lastday);
                    double sumSales = VenteDelegate.sumVente(date1, date2, region, taux);
                    serie_vente.getData().add(new XYChart.Data<>(moix, sumSales));
                    double achat = VenteDelegate.sumCoutAchatArticleVendu(date1, date2, region);
                    double sum = VenteDelegate.sumExpenses(date1, date2, region, taux);
                    double prixrevient = achat + sum;
                    double result = sumSales - prixrevient;
                    serie_prixderevient.getData().add(new XYChart.Data<>(moix, prixrevient));
                    // double result = sumSales - prixrevient;//resultat
                    serie_resultat.getData().add(new XYChart.Data<>(moix, result));
                }
            } catch (ParseException ex) {
                Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        venteChart.getData().add(serie_vente);
        venteChart.getData().add(serie_prixderevient);
        venteChart.getData().add(serie_resultat);
        venteChart.setLegendSide(Side.BOTTOM);

    }

    private Map<Long, String> sortDesc(Map<Long, String> values) {
        return new TreeMap(values).descendingMap();
    }

    private void loadProChart() {

        piepane.setPrefSize(351, 318);
        piepane.setLabelsVisible(true);
        //piepane.setLegendVisible(true);

        HashMap<Long, String> entries;
        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
            entries = VenteDelegate.getTop10ProductDesc();
        } else {
            entries = VenteDelegate.getTop10ProductDesc(region);
        }
        System.out.println("entree " + entries.size());
        for (Map.Entry<Long, String> entry : entries.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getValue(), entry.getKey());
            piepane.setAnimated(true);

            if (!existPie(piepane.getData(), data.getName())) {
                piepane.getData().add(data);
            }
        }

    }

    private void loadSaleChart() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                venteChart.getData().clear();
                metrify();
            }
        });

    }

    public boolean existPie(ObservableList<PieChart.Data> data, String name) {
        for (PieChart.Data d : data) {
            if (d.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getMonthName(String numRepres) {
        if (numRepres.endsWith("01")) {
            return bundle.getString("xmwezi_1");
        } else if (numRepres.endsWith("02")) {
            return bundle.getString("xmwezi_2");
        } else if (numRepres.endsWith("03") || numRepres.endsWith("3")) {
            return bundle.getString("xmwezi_3");
        } else if (numRepres.endsWith("04") || numRepres.endsWith("4")) {
            return bundle.getString("xmwezi_4");
        } else if (numRepres.endsWith("05") || numRepres.endsWith("5")) {
            return bundle.getString("xmwezi_5");
        } else if (numRepres.endsWith("06") || numRepres.endsWith("6")) {
            return bundle.getString("xmwezi_6");
        } else if (numRepres.endsWith("07") || numRepres.endsWith("7")) {
            return bundle.getString("xmwezi_7");
        } else if (numRepres.endsWith("08") || numRepres.endsWith("8")) {
            return bundle.getString("xmwezi_8");
        } else if (numRepres.endsWith("09") || numRepres.endsWith("9")) {
            return bundle.getString("xmwezi_9");
        } else if (numRepres.endsWith("10")) {
            return bundle.getString("xmwezi_10");
        } else if (numRepres.endsWith("11")) {
            return bundle.getString("xmwezi_11");
        } else if (numRepres.endsWith("12")) {
            return bundle.getString("xmwezi_12");
        }
        return "";
    }

    public void setUserPhone(String phone) {
        this.phone = phone;
        setPhone(phone);
    }

    @FXML
    private void switchToDashBoard(MouseEvent event) {
        mainpane.getChildren().remove(0);
        mainpane.getChildren().add(showPane);
        pane_title.setText("Tableau de bord");
        image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/dashboard(2).png")));
        summarise();
        CURRENT_VIEW = "DASHBOARD";
    }

    @FXML
    public void switchToStock(Event event) {
        if (go()) {
            switchSimpleScreens(tools.Constants.STORAGE_VIEW, STORAGE, "Stockage", "warehouse(1).png");
        }

    }

    @FXML
    private void switchToSettings(MouseEvent event) {
        switchSimpleScreens(tools.Constants.PARAMETRE_VIEW, PARAMETRES, "Paramètres", "speedometer(2).png");
    }

    public void switchSimpleScreens(String xml, String viewName, String title, String iconName) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(viewName)) {
                    AnchorPane p = MainUI.getPage(MainuiController.this, xml, token, entreprisex, kazisafe);
                    if (p == null) {
                        return;
                    }
                    p.setLayoutY(LAYOUTY);
                    p.setLayoutX(LAYOUTX);
                    mainpane.getChildren().remove(0);
                    mainpane.getChildren().add(p);
                    pane_title.setText(title);
                    image_title.setImage(new Image(MainuiController.this.getClass().getResourceAsStream("/icons/" + iconName)));
                    CURRENT_VIEW = viewName;
                }
            }
        });

    }

    public void switchScreens(String xml, String viewName, String title, String iconName, Vente v, Facture liv) {
        if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(viewName)) {
            AnchorPane p = MainUI.getPage(this, xml, token, entreprisex, v, liv);
            if (p == null) {
                return;
            }
            p.setLayoutY(LAYOUTY);
            p.setLayoutX(LAYOUTX);
            mainpane.getChildren().remove(0);
            mainpane.getChildren().add(p);
            pane_title.setText(title);
            image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/" + iconName)));
            CURRENT_VIEW = viewName;
        }
    }

    @FXML
    public void switchToPos(Event event) {
        if (go()) {
            switchSimpleScreens(tools.Constants.POS_VIEW, POS, "Ventes & Recquisitions", "shopping-cart(1).png");
        }

    }

    @FXML
    public void switchToTresorerie(Event event) {
        if (go()) {
            switchScreens(tools.Constants.CAISSE_VIEW, CAISSES, "Trésorerie", "cashier.png", null, null);
        }

    }

    @FXML
    private void switchToAgents(MouseEvent event) {
        if (go()) {
            if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(tools.Constants.AGENTS)) {
                AnchorPane p = MainUI.getPage(this, tools.Constants.AGENTS_VIEW, token, entreprisex, kazisafe);
                p.setLayoutY(LAYOUTY);
                p.setLayoutX(LAYOUTX);
                mainpane.getChildren().remove(0);
                mainpane.getChildren().add(p);
                pane_title.setText("Agents");
                image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/hosting-services.png")));
                CURRENT_VIEW = tools.Constants.AGENTS;
            }
        }

    }

    @FXML
    private void switchToCompany(MouseEvent event) {

        AnchorPane p = MainUI.getPage(this, tools.Constants.ENTREPRISE_VIEW, token, entreprisex, kazisafe, user);
        p.setLayoutY(LAYOUTY);
        p.setLayoutX(LAYOUTX);
        mainpane.getChildren().remove(0);
        mainpane.getChildren().add(p);
        pane_title.setText("Entreprise");
        image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/office-building.png")));
        CURRENT_VIEW = tools.Constants.ENTREPRISE;
    }

    public void startSMSreporting() {
        if (go()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    EventHandler evh = new NotificationHandler();
//                    String url = KazisafeServiceFactory.BASE_URL + "notification/smsrepport";
//                    EventSource.Builder evb = new EventSource.Builder(evh, URI.create(url))
//                            .reconnectTimeMs(3000);
//                    try (EventSource evs = evb.build()) {
//                        evs.start();
//                        TimeUnit.MINUTES.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }).start();
        }
    }

    @FXML
    public void switchToProduct(Event event) {
        if (go()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(tools.Constants.PRODUIT)) {
                        AnchorPane p = MainUI.getPage(MainuiController.this, tools.Constants.PRODUITS_VIEW, token, null, entreprisex);

                        p.setLayoutY(LAYOUTY);
                        p.setLayoutX(LAYOUTX);
                        mainpane.getChildren().remove(0);
                        mainpane.getChildren().add(p);
                        pane_title.setText("Produits");
                        image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/boxes.png")));
                        CURRENT_VIEW = tools.Constants.PRODUIT;
                    }
                }
            });

        }

    }

    @FXML
    public void switchToRepport(Event event) {
        if (go()) {
            switchSimpleScreens(tools.Constants.REPPORT_VIEW, REPPORTS, "Rapports", "report.png");
        }

    }

    @FXML
    private void exit(Event event) {
        pref.putInt("exit", 1);
        if (sep != null) {
            sep.closeSession();
        }
        System.exit(0);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (session != null) {
//                        
//                        session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Bye bye all"));
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }).start();
        //   pref.putLong(tools.Constants.LAST_SESSION_ENDS, System.currentTimeMillis());
    }

    @FXML
    private void onHoverHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.setShadowEffect(img);
    }

    @FXML
    private void onExitOverlay(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.setShadowAlertEffect(img);
    }

    @FXML
    private void onOutHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.removeShaddowEffect(img);
    }

    public void setPhone(String phone) {
        this.phone = phone;
        kazisafe.showUserByPhoneSecurely(phone)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> rspns) {
                        System.out.println("loading usr rspn " + rspns.message());
                        if (rspns.isSuccessful()) {
                            user = rspns.body();
                            pref.put("operator", user.getNom() + " " + user.getPrenom());
                            pref.put("userid", user.getUid());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    user_connected.setText(user.getPrenom() + " " + user.getNom());
                                }
                            });
                            kazisafe.downloadUserPhotoSecurely(user.getUid())
                                    .enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rspns) {
                                            if (rspns.isSuccessful()) {
                                                InputStream is = rspns.body().byteStream();
                                                Image image = new Image(is, 90, 94, true, true);
                                                img_profile.setImage(MainUI.makeTransparent(image));
                                                Circle clip = new Circle(16);
                                                clip.setStrokeType(StrokeType.CENTERED);
                                                clip.setStroke(Color.valueOf("#44cef5"));
                                                clip.setStrokeWidth(3);
                                                clip.setCenterX(img_profile.getFitWidth() / 2);
                                                clip.setCenterY(img_profile.getFitHeight() / 2);
                                                img_profile.setClip(clip);
                                                centerImage(img_profile);

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable thrwbl) {
                                            System.err.println("Erreur image profile " + thrwbl.getMessage());
                                        }
                                    });
                            img_profile.imageProperty().addListener(new ChangeListener<Image>() {
                                @Override
                                public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable thrwbl) {
                        thrwbl.printStackTrace();
                    }
                });

    }

    public void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;

            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if (ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            imageView.setX((imageView.getFitWidth() - w) / 2);
            imageView.setY((imageView.getFitHeight() - h) / 2);

        }
    }

    public void setEntrepiseId(String entrepiseId) {
        this.entrepiseId = entrepiseId;

    }

    Abonnement ab = null;

    public void setLoginResult(String token, String rccm, LoginResult loginResult) {
        setToken(token);
        this.rccm = rccm;
        this.loginResult = loginResult;
        final String ezi = loginResult.getEntreprise();
        role = loginResult.getRole();
        System.out.println("Role " + role);
        region = loginResult.getRegion();
        if (role.equals(Role.Saler.name())) {
            vbox_menu.getChildren().remove(caisse);
            vbox_menu.getChildren().remove(stockage);
            vbox_menu.getChildren().remove(agents);
            vbox_menu.getChildren().remove(rapport);
        } else if (role.equals(Role.Magazinner.name())) {
            vbox_menu.getChildren().remove(caisse);
            vbox_menu.getChildren().remove(agents);
            vbox_menu.getChildren().remove(rapport);
        } else if (role.equals(Role.Finance.name())) {
            vbox_menu.getChildren().remove(stockage);
            vbox_menu.getChildren().remove(agents);
            vbox_menu.getChildren().remove(rapport);
        } else if (role.equals(Role.Manager.name()) || role.contains(Role.ALL_ACCESS.name())) {
            vbox_menu.getChildren().remove(agents);
        }
      
        searchField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue == null) {
                return;
            }
            switch (CURRENT_VIEW) {
                case tools.Constants.PRODUIT:
                    ProduitsController pc = ProduitsController.getInstance();
                    pc.searchProduit(newValue);
                    break;
                case tools.Constants.STORAGE:
                    GoodstorageController gc = GoodstorageController.getInstance();
                    gc.search(newValue);
                    break;
                case tools.Constants.POS:
                    PosController poc = PosController.getInstance();
                    poc.search(newValue);
                    break;
                case tools.Constants.CAISSES:
                    TresorerieController.getInstance().search(newValue);
                    break;
                case tools.Constants.AGENTS:
                    AgentController.getInstance().search(newValue);
                    break;
                default:
                    break;
            }
        });

        try {

            if (ezi == null) {
                return;
            }
            System.err.println(" e: 0x" + ezi + " rccm " + rccm);
            Response<Entreprise> rent
                    = kazisafe.getEntrepriseInfo(rccm)
                            .execute();
            System.err.println("Reponse search entreprise " + rent.message());
            if (rent.isSuccessful()) {
                entreprisex = rent.body();
                System.out.println("Affiche info entreprise " + entreprisex);
                pref.put("ent_name", entreprisex.getNomEntreprise());
                pref.put("ent_ID", entreprisex.getIdentification());
                pref.put("ent_adresse", entreprisex.getAdresse());
                pref.put("ent_email", entreprisex.getEmail());
                pref.put("ent_idnat", entreprisex.getIdNat() == null ? "Aucun" : entreprisex.getIdNat());
                pref.put("ent_impot", entreprisex.getNumeroImpot() == null ? "Aucun" : entreprisex.getNumeroImpot());
                pref.put("ent_phones", entreprisex.getPhones() == null ? "Aucun" : entreprisex.getPhones());
                txt_region.setText(region);
                Platform.runLater(() -> {
                    entrep_name.setText(entreprisex.getNomEntreprise());
                });//;localhost:8080
                URL = KazisafeServiceFactory.WEBSOCKET + "/wssync/ez/" + ezi + "/" + token;
                sep = new SyncEndpoint(URL);

                pref.putInt("exit", 0);
                kazisafe.downloadLogo(ezi)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rspns) {
                                if (rspns.isSuccessful()) {
                                    try {
                                        InputStream is = rspns.body().byteStream();
                                        byte[] pixa = data.helpers.FileUtils.readFromFile(FileUtils.streamTofile(is));
                                        FileUtils.byteToFile(ezi, pixa, "png");
                                    } catch (IOException ex) {
                                        Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable thrwbl) {

                            }
                        });

                kazisafe.getRegions(entreprisex.getUid()).enqueue(new retrofit2.Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                        if (rspns.isSuccessful()) {
                            List<String> lreg = rspns.body();
                            int i = 0;
                            for (String reg : lreg) {
                                pref.put("region" + (++i), reg);
                            }
                            System.err.println("Agent regions " + lreg.size());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable thrwbl) {

                    }
                });

                se = SyncEngine.getInstance();

                se.setup(token, entreprisex);
                if (go()) {
                    se.start();
                } else {
                    return;
                }
                se.startChecking();
                se.setOnUpdateVersionListener(new OnUpdateVersionListener() {
                    @Override
                    public void onNewUpdate(Module module) {
                        newModule = module;
                        update_pane.setVisible(true);
                        Platform.runLater(() -> {
                            label_version.setText("Version : " + module.getVersion());
                        });

                    }

                    @Override
                    public void onSameUpdate(Module module) {
                        update_pane.setVisible(false);
                    }
                });
                sep.getPublisher().subscribe(saver);
            }
            summarise();
        } catch (IOException ex) {
            boolean open = pref.getBoolean("open-session", true);
            if (open) {
                String eName = pref.get("ent_name", null);
                String id = pref.get("ent_ID", null);
                String adresse = pref.get("ent_adresse", null);
                String email = pref.get("ent_email", null);
                String idnat = pref.get("ent_idnat", null);
                String impot = pref.get("ent_impot", null);
                String phonez = pref.get("ent_phones", null);
                token = pref.get("token", null);
                region = pref.get("region", "...");
                txt_region.setText(region);
                String user = pref.get("operator", "Chargement...");
                entreprisex = new Entreprise(ezi);
                if (eName == null) {
                    return;
                }
                entreprisex.setNomEntreprise(eName);
                if (adresse == null) {
                    return;
                }
                entreprisex.setAdresse(adresse);
                if (idnat == null) {
                    return;
                }
                entreprisex.setIdNat(idnat == null ? " " : idnat);
                entreprisex.setNumeroImpot(impot == null ? " " : impot);
                entreprisex.setPhones(phonez == null ? " " : phonez);

                if (id == null) {
                    return;
                }
                entreprisex.setIdentification(id);
                if (email == null) {
                    return;
                }
                entrep_name.setText(entreprisex.getNomEntreprise());
                user_connected.setText(user);
                entreprisex.setEmail(email);
                se = SyncEngine.getInstance();
                se.setup(token, entreprisex);
                if (go()) {

                    se.start();
                }
                se.startChecking();
                se.setOnUpdateVersionListener(new OnUpdateVersionListener() {
                    @Override
                    public void onNewUpdate(Module module) {
                        newModule = module;
                        update_pane.setVisible(true);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                label_version.setText("Version : " + module.getVersion());
                            }
                        });
                    }

                    @Override
                    public void onSameUpdate(Module module) {
                        update_pane.setVisible(false);
                    }
                });
            }
            //  Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (role.equals(Role.Saler.name())) {
            vbox_menu.getChildren().remove(caisse);
            vbox_menu.getChildren().remove(stockage);
            vbox_menu.getChildren().remove(agents);
            vbox_menu.getChildren().remove(rapport);
        } else if (role.equals(Role.Magazinner.name())) {
            vbox_menu.getChildren().remove(caisse);
            vbox_menu.getChildren().remove(agents);
            vbox_menu.getChildren().remove(rapport);
        } else if (role.equals(Role.Finance.name())) {
            vbox_menu.getChildren().remove(stockage);
            vbox_menu.getChildren().remove(agents);
            vbox_menu.getChildren().remove(rapport);
        } else if (role.equals(Role.Manager.name())) {
            vbox_menu.getChildren().remove(agents);
        }

        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) {
                    return;
                }
                if (CURRENT_VIEW.equals(tools.Constants.PRODUIT)) {
                    ProduitsController pc = ProduitsController.getInstance();
                    pc.searchProduit(newValue);
                } else if (CURRENT_VIEW.equals(tools.Constants.STORAGE)) {
                    GoodstorageController gc = GoodstorageController.getInstance();
                    gc.search(newValue);
                } else if (CURRENT_VIEW.equals(tools.Constants.POS)) {

                    PosController poc = PosController.getInstance();
                    poc.search(newValue);

                } else if (CURRENT_VIEW.equals(tools.Constants.CAISSES)) {
                    TresorerieController.getInstance().search(newValue);
                } else if (CURRENT_VIEW.equals(tools.Constants.AGENTS)) {
                    AgentController.getInstance().search(newValue);
                }
            }
        });
        //summarise();
        startSMSreporting();

    }

    private void subscribe() {
//        Executors.newSingleThreadExecutor()
//                .execute(() -> {
//                    boolean network_on = pref.getBoolean(NetLoockup.NETWORK_STATUS, NetLoockup.NETWORK_STATUS_DEFAULT);
//                    if (network_on) {
        Executors.newCachedThreadPool()
                .submit(() -> {
                    Refresher ref = new Refresher();
                    ref.setTarget("CHK_SUB");
                    Util.sync(ref, "read", Tables.REFRESH);
                });
//                    }
//                });
    }

    @FXML
    public void search(Event e) {
        if (!searchField.getText().isEmpty()) {
            if (searchField.getText() == null) {
                return;
            }
            if (CURRENT_VIEW.equals(tools.Constants.PRODUIT)) {
                ProduitsController pc = ProduitsController.getInstance();
                pc.searchProduit(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.STORAGE)) {
                GoodstorageController gc = GoodstorageController.getInstance();
                gc.search(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.POS)) {
                PosController poc = PosController.getInstance();
                poc.search(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.CAISSES)) {
                TresorerieController.getInstance().search(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.AGENTS)) {
                AgentController.getInstance().search(searchField.getText());
            }
        }
    }

    private boolean go() {
        if (entrepiseId != null) {
            subscribe();
        }
        double max = pref.getDouble("sub", 0);
        String type = pref.get("type-sub", "Gold");
        if (type.equalsIgnoreCase("Gold")) {
            if (max > 0) {
                return true;
            } else {
                MainUI.notify(null, "Attention", "Vous n'avez plus de crédit Kazisafe (record) dans votre compte, veuillez recharger votre compte", 5, "warning");
                return false;
            }
        } else {
            long d2 = System.currentTimeMillis();
            long jrs = BigDecimal.valueOf(max).setScale(9).longValue();
            long remaind = jrs - d2;
            long week = tools.Constants.MILLSECONDS_JOURN * 7;
            System.err.println("Remained " + remaind + " week " + week);

            if (remaind <= 0) {
                MainUI.notify(null, "Attention", "Vous n'avez plus de crédit Kazisafe (record) dans votre compte, veuillez récharger votre compte", 5, "warning");
                return false;
            }
            if (Math.abs(remaind) <= week) {
                MainUI.notify(null, "Attention", "Votre crédit Kazisafe expire bientôt, pensez à le renouveller", 5, "warning");
                return true;
            }
            return true;
        }

    }

    @FXML
    private void runhelp(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Desktop.getDesktop().open(new File("./Kazisafe guide d'utilisation.pdf"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (java.lang.IllegalArgumentException ex) {
                    MainUI.notify(null, "Erreur", "Le fichier d'aide n'existe plus ou son nom \"Kazisafe guide d'utilisation.pdf\" d'origine a été modifié", 4, "error");
                }
            }

        }).start();
    }

    @FXML
    private void installUpdate(Event e) {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Cet action va fermer Kazisafe pour installer le mise a jour\nvoulez vous continuer ?", ButtonType.YES, ButtonType.CANCEL);
            alert.setTitle("Attention!");
            alert.setHeaderText(null);
            Optional<ButtonType> showAndWait = alert.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                Runtime.getRuntime().exec(localPath + File.separator + (PlatformUtil.isWindows() ? newModule.getNomModule() : PlatformUtil.isMac() ? "Kazisafe-MacOS.zip" : "Kazisafe-Linux.zip"));
                pref.put("ksf_version", newModule.getVersion());
                exit(e);
            }

        } catch (IOException ex) {
            Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void closeUpdate(Event e) {
        update_pane.setVisible(false);
    }

    @FXML
    private void downloadUpdate(Event e) {
        if (newModule != null) {
            if (PlatformUtil.isWindows()) {
                Task<Void> downTask = new DownloadTask(newModule.getNomModule(), localPath);
                downTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                        if (downTask.getState() == newValue.SUCCEEDED) {
                            install_update_link.setVisible(true);
                        } else if (downTask.getState() == newValue.FAILED) {
                            MainUI.notify(null, "Erreur", "Le téléchargemement a été interrompu", 4, "error");
                        }
                    }
                });
                download_update_pgb.setVisible(true);
                douwnload_update_pgi.setVisible(true);
                download_update_pgb.progressProperty().bind(downTask.progressProperty());
                douwnload_update_pgi.progressProperty().bind(downTask.progressProperty());
                Thread kazi = new Thread(downTask);
                kazi.setDaemon(true);
                kazi.start();
            } else if (PlatformUtil.isMac()) {
                Task<Void> downTask = new DownloadTask("Kazisafe-MacOS.zip", localPath);
                downTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                        if (downTask.getState() == newValue.SUCCEEDED) {
                            install_update_link.setVisible(true);
                        } else if (downTask.getState() == newValue.FAILED) {
                            MainUI.notify(null, "Erreur", "Le téléchargemement a été interrompu", 4, "error");
                        }
                    }
                });
                download_update_pgb.setVisible(true);
                douwnload_update_pgi.setVisible(true);
                download_update_pgb.progressProperty().bind(downTask.progressProperty());
                douwnload_update_pgi.progressProperty().bind(downTask.progressProperty());
                Thread kazi = new Thread(downTask);
                kazi.setDaemon(true);
                kazi.start();
            }
        } else {
            MainUI.notify(null, "Erreur", "erreur interne verifiez votre connection internet", 4, "error");
        }

    }

    private void installTooltips() {
        Tooltip thome = new Tooltip();
        thome.setText(bundle.getString("xlbel.curen_tview"));
        thome.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(home, thome);

        Tooltip tclient = new Tooltip();
        tclient.setText(bundle.getString("xtooltip.fina_nce"));
        tclient.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(caisse, tclient);

        Tooltip tvente = new Tooltip();
        tvente.setText(bundle.getString("xtooltip.vente_recquis"));
        tvente.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(pos, tvente);

        Tooltip tdepot = new Tooltip();
        tdepot.setText(bundle.getString("xtooltip.entr_epot"));
        tdepot.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(stockage, tdepot);

        Tooltip tproducts = new Tooltip();
        tproducts.setText(bundle.getString("xtooltip.prodwi"));
        tproducts.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(products_gate, tproducts);

        Tooltip tagents = new Tooltip();
        tagents.setText(bundle.getString("xtooltip.ag_ents"));
        tagents.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(agents, tagents);

        Tooltip company = new Tooltip();
        company.setText(bundle.getString("xtooltip.com_panie"));
        company.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(compagnie, company);

        Tooltip trapport = new Tooltip();
        trapport.setText(bundle.getString("xtooltip.rap_poro"));
        trapport.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(rapport, trapport);

        Tooltip perf = new Tooltip();
        perf.setText(bundle.getString("xtooltip.param_ettre"));
        perf.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(parametre, perf);
    }

    private static class DownloadTask extends Task<Void> {

        String url;
        String localPath;

        public DownloadTask(String url, String localPath) {
            this.url = url;
            this.localPath = localPath;
        }

        @Override
        protected Void call() throws Exception {
            URLConnection connexion = new URL("https://www.kazisafe.com/download/" + this.url).openConnection();
            long taille = connexion.getContentLengthLong();
            try (InputStream is = connexion.getInputStream(); OutputStream os = Files.newOutputStream(Paths.get(localPath + "/" + url))) {
                long nread = 0L;
                byte[] buffer = new byte[8192];
                int n;
                while ((n = is.read(buffer)) > 0) {
                    os.write(buffer, 0, n);
                    nread += n;
                    updateProgress(nread, taille);
                }
            }
            return null;
        }

        @Override
        protected void failed() {
            super.failed();
        }

        @Override
        protected void succeeded() {
            super.succeeded();
        }

    }

    private void save(Set<BaseModel> models) {
        int retries = 0;
//        boolean success = false;                

//        while (retries < 5 && !success) {
        EntityManager em = SafeConnectionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            for (BaseModel model : models) {
                String type = model.getType();
                System.out.println("Pret a enregistrer un " + type);
                switch (type) {
                    case "CATEGORY" -> {
                        Category c = (Category) model;
                        Category ct = em.find(Category.class, c.getUid());
                        if (ct != null) {
                            em.merge(c);
                        } else {
                            em.merge(c);
                        }
                        em.merge(Util.createJournal(c.getUid(), c, true));
                    }
                    case "PRODUIT" -> {
                        Produit mz = (Produit) model;
                        Produit pt = em.find(Produit.class, mz.getUid());
                        String idcat = extractId(mz.getCategoryId().getUid());
                        System.out.println("ID CAT = " + idcat);
                        Category categ = em.find(Category.class, idcat);
                        System.out.println("Category P trouvee " + categ);
                        if (categ != null) {
                            mz.setCategoryId(new Category(idcat));
                            if (pt == null) {
                                em.merge(mz);
//                                    em.merge(Util.createJournal(p.getUid(), p, true));
                            } else {
                                em.merge(mz);
//                                    em.merge(Util.createJournal(p.getUid(), p, true));
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "MESURE" -> {

                        Mesure mz = (Mesure) model;
                        String idcat = extractId(mz.getProduitId().getUid());
                        // Vérifier l'existence du parent (Department)
                        Produit pr = em.find(Produit.class, idcat);
                        if (pr != null) {
                            Mesure mex = em.find(Mesure.class, mz.getUid());
                            mz.setProduitId(new Produit(idcat));
                            if (mex != null) {
                                em.merge(mz);
//                                    em.merge(Util.createJournal(mz.getUid(), mz, true));
                            } else {
                                em.merge(mz);
//                                    em.merge(Util.createJournal(mz.getUid(), mz, true));
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "FOURNISSEUR" -> {

                        Fournisseur mz = (Fournisseur) model;

                        Fournisseur exist = em.find(Fournisseur.class, mz.getUid());
                        if (exist != null) {
                            em.merge(mz);
                        } else {
                            em.merge(mz);
                        }
                        em.merge(Util.createJournal(mz.getUid(), mz, true));
                    }
                    case "LIVRAISON" -> {

                        Livraison mz = (Livraison) model;
                        // Vérifier l'existence du parent (Department)
                        System.out.println("Livraison -- FRN " + mz.getFournId().getUid());
                        String idcat = extractId(mz.getFournId().getUid());
                        Fournisseur fr = em.find(Fournisseur.class, idcat);
                        if (fr != null) {
                            Livraison exist = em.find(Livraison.class, mz.getUid());
                            mz.setFournId(new Fournisseur(idcat));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "STOCKER" -> {

                        Stocker mz = (Stocker) model;

                        // Vérifier l'existence du parent (Department)
                        String idp = extractId(mz.getProductId().getUid());
                        Produit pr = em.find(Produit.class, idp);
                        String idm = extractId(mz.getMesureId().getUid());
                        Mesure m = em.find(Mesure.class, idm);
                        String idl = extractId(mz.getLivraisId().getUid());
                        Livraison l = em.find(Livraison.class, idl);
                        if (pr != null && m != null && l != null) {
                            Stocker exist = em.find(Stocker.class, mz.getUid());
                            mz.setLivraisId(new Livraison(idl));
                            mz.setMesureId(new Mesure(idm));
                            mz.setProductId(new Produit(idp));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "DESTOCKER" -> {

                        Destocker mz = (Destocker) model;

                        // Vérifier l'existence du parent (Department)
                        String idp = extractId(mz.getProductId().getUid());
                        Produit pr = em.find(Produit.class, idp);
                        String idm = extractId(mz.getMesureId().getUid());
                        Mesure m = em.find(Mesure.class, idm);
                        if (pr != null && m != null) {
                            Destocker exist = em.find(Destocker.class, mz.getUid());
                            mz.setMesureId(new Mesure(idm));
                            mz.setProductId(new Produit(idp));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "RECQUISITION" -> {
                        Recquisition mz = (Recquisition) model;
                        // Vérifier l'existence du parent (Department)
                        String idp = extractId(mz.getProductId().getUid());
                        Produit pr = em.find(Produit.class, idp);
                        String idm = extractId(mz.getMesureId().getUid());
                        Mesure m = em.find(Mesure.class, idm);
                        if (pr != null && m != null) {
                            Recquisition exist = em.find(Recquisition.class, mz.getUid());
                            mz.setMesureId(new Mesure(idm));
                            mz.setProductId(new Produit(idp));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }
                    }
                    case "PRIXDEVENTE" -> {
                        PrixDeVente mz = (PrixDeVente) model;
                        // Vérifier l'existence du parent (Department)
                        String idr = extractId(mz.getRecquisitionId().getUid());
                        Recquisition pr = em.find(Recquisition.class, idr);
                        String idm = extractId(mz.getMesureId().getUid());
                        Mesure m = em.find(Mesure.class, idm);
                        if (pr != null && m != null) {
                            PrixDeVente exist = em.find(PrixDeVente.class, mz.getUid());
                            mz.setMesureId(new Mesure(idm));
                            mz.setRecquisitionId(new Recquisition(idr));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }
                    }
                    case "CLIENT" -> {

                        Client mz = (Client) model;
                        if (mz.getUid() != null) {
                            Client parent = mz.getParentId();
                            if (parent != null) {
                                Client mere = em.find(Client.class, parent.getUid());
                                if (mere == null) {
                                    mz.setParentId(mz);
                                }
                                Client exist = em.find(Client.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.merge(mz);
                                }
                                em.merge(Util.createJournal(mz.getUid(), mz, true));

                            }

                        }
                    }
                    case "CLIENTORGANISATION" -> {

                        ClientOrganisation mz = (ClientOrganisation) model;

                        ClientOrganisation exist = em.find(ClientOrganisation.class, mz.getUid());
                        if (exist != null) {
                            em.merge(mz);
                        } else {
                            em.merge(mz);
                        }
                        em.merge(Util.createJournal(mz.getUid(), mz, true));

                    }
                    case "CLIENTAPPARTENIR" -> {

                        ClientAppartenir mz = (ClientAppartenir) model;

                        // Vérifier l'existence du parent (Department)
                        String ido = extractId(mz.getClientOrganisationId().getUid());
                        ClientOrganisation pr = em.find(ClientOrganisation.class, ido);
                        String idc = extractId(mz.getClientId().getUid());
                        Client m = em.find(Client.class, idc);
                        if (pr != null && m != null) {
                            ClientAppartenir exist = em.find(ClientAppartenir.class, mz.getUid());
                            mz.setClientId(new Client(idc));
                            mz.setClientOrganisationId(new ClientOrganisation(ido));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "VENTE" -> {

                        Vente mz = (Vente) model;

                        // Vérifier l'existence du parent (Department)
                        String idc = extractId(mz.getClientId().getUid());
                        Client pr = em.find(Client.class, idc);
                        if (pr != null) {
                            Vente exist = em.find(Vente.class, mz.getUid());
                            mz.setClientId(new Client(idc));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "LIGNEVENTE" -> {

                        LigneVente mz = (LigneVente) model;

                        // Vérifier l'existence du parent (Department)
                        String idp = extractId(mz.getProductId().getUid());
                        Produit pr = em.find(Produit.class, idp);
                        String idm = extractId(mz.getMesureId().getUid());
                        Mesure m = em.find(Mesure.class, idm);
                        String idv = extractId(mz.getReference().getUid());
                        int uidv = Integer.parseInt(idv);
                        Vente v = em.find(Vente.class, uidv);
                        if (pr != null && m != null && v != null) {
                            LigneVente exist = em.find(LigneVente.class, mz.getUid());
                            mz.setMesureId(new Mesure(idm));
                            mz.setReference(new Vente(uidv));
                            mz.setProductId(new Produit(idp));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "COMPTETRESOR" -> {
                        CompteTresor mz = (CompteTresor) model;
                        CompteTresor exist = em.find(CompteTresor.class, mz.getUid());
                        if (exist != null) {
                            em.merge(mz);
                        } else {
                            em.merge(mz);
                        }
                        em.merge(Util.createJournal(mz.getUid(), mz, true));
                    }
                    case "TRAISORERIE" -> {

                        Traisorerie mz = (Traisorerie) model;
                        // Vérifier l'existence du parent (Department)
                        String idcp = extractId(mz.getTresorId().getUid());
                        CompteTresor pr = em.find(CompteTresor.class, idcp);
                        if (pr != null) {
                            Traisorerie exist = em.find(Traisorerie.class, mz.getUid());
                            mz.setTresorId(new CompteTresor(idcp));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "DEPENSE" -> {
                        Depense mz = (Depense) model;
                        Depense exist = em.find(Depense.class, mz.getUid());
                        if (exist != null) {
                            em.merge(mz);
                        } else {
                            em.merge(mz);
                        }
                        em.merge(Util.createJournal(mz.getUid(), mz, true));

                    }
                    case "OPERATION" -> {
                        Operation mz = (Operation) model;
                        // Vérifier l'existence du parent (Department)
                        String idd = extractId(mz.getDepenseId().getUid());
                        Depense pr = em.find(Depense.class, mz.getDepenseId().getUid());
                        String idc = extractId(mz.getTresorId().getUid());
                        CompteTresor ct = em.find(CompteTresor.class, idc);
                        String idt = extractId(mz.getCaisseOpId().getUid());
                        Traisorerie tr = em.find(Traisorerie.class, idt);
                        if (pr != null && ct != null && tr != null) {
                            Operation exist = em.find(Operation.class, mz.getUid());
                            mz.setCaisseOpId(new Traisorerie(idt));
                            mz.setDepenseId(new Depense(idd));
                            mz.setTresorId(new CompteTresor(idc));
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.merge(mz);
                            }
                            em.merge(Util.createJournal(mz.getUid(), mz, true));
                        }

                    }
                    case "RETOURDEPOT" -> {

                    }
                    case "RETOURMAGASIN" -> {
                        RetourMagasin mz = (RetourMagasin) model;
                        RetourMagasin exist = em.find(RetourMagasin.class, mz.getUid());
                        if (exist != null) {
                            em.merge(mz);
                        } else {
                            em.merge(mz);
                        }
                        em.merge(Util.createJournal(mz.getUid(), mz, true));
                    }
                    case "ARETIRER" -> {

                    }
                    case "FACTURE" -> {

                    }
                    case "REFRESH" -> {

                    }
                }
            }
            tx.commit();
            em.clear();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            if (isDeadlockException(e)) {
                System.out.println("Dead lock detecte " + retries + " fois, reessaie en cours.... ");
            } else if (isUniqueConstraintViolation(e)) {
                System.out.println("Doublon dans les donnees saut executed");
            } else if (isIllegalStateException(e)) {
                e.printStackTrace();
            } else {
                e.printStackTrace();
            }
        }
    }

    private boolean isDeadlockException(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage().contains("Deadlock found when trying to get lock")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private boolean isEntityExistException(Exception e) {
        Throwable x = e;
        while (x != null) {
            if (x instanceof EntityExistsException) {
                return true;
            }
            x = x.getCause();
        }
        return false;
    }

    private boolean isEntityNotFoundException(Exception e) {
        Throwable x = e;
        while (x != null) {
            if (x instanceof jakarta.persistence.EntityNotFoundException) {
                return true;
            }
            x = x.getCause();
        }
        return false;
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private boolean isIllegalStateException(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof IllegalStateException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private String extractId(Object obj) {
        System.out.println("ID OBJKT " + obj);

        String s = String.valueOf(obj);
        String result;
        if (s.contains("{") || s.contains("}")) {
            result = s.replace("{", "").replace("}", "").split(",")[0].split("=")[1];
        } else {
            result = s;
        }
        return result;
    }
}
