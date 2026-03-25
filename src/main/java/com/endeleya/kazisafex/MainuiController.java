/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import delegates.OperationDelegate;
import delegates.VenteDelegate;
import delegates.LigneVenteDelegate;
import delegates.RepportDelegate;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

import data.Abonnement;
import data.BaseModel;
import data.Entreprise;
import data.Facture;
import data.LigneVente;
import data.helpers.LoginWebResult;
import data.Module;
import data.Operation;
import data.Refresher;
import data.User;
import data.Vente;
import okhttp3.ResponseBody;
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
import tools.PriceMaker;
import tools.Agregator;
import tools.LocalTaskStateListener;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import tools.Constants;
import data.Permission;
import data.helpers.Role;
import data.network.Kazisafe;
import data.helpers.Token;
import jakarta.persistence.EntityExistsException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import delegates.PermissionDelegate;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.scene.control.ToolBar;
import okhttp3.Headers;
import tools.TopTen;
import tools.Droit;
import tools.Metric;
import tools.SubscriptionUtil;

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
    private ImageView immobilisation;
    @FXML
    private ImageView caisse, img_iconify, img_close, agrandir;

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
    private AreaChart<String, Number> venteChart;
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
    private Module newModule;
    Preferences pref;
    double taux;
    int itemPerPage = 15;
    String localPath;
    Kazisafe kazisafe;
    User user;
    Entreprise entreprisex;
    PriceMaker maker;

    private String CURRENT_VIEW = tools.Constants.MAIN;
    boolean isConnected = false;

    private static MainuiController instance;
    private static final int BATCH_SIZE = 10;
    private Set<BaseModel> buffer;
    Agregator ag;
    NetLoockup network;
    private ImageView activeMenuIcon;
    @FXML
    private Label sync_txt_message;
    @FXML
    private ProgressBar sync_pg_bar;
    @FXML
    private ImageView production;
    @FXML
    private ToolBar tbar_menu;
    @FXML
    private Button btn_theme_toggle;
    @FXML
    public Label txt_states_features;

    public Label getSync_txt_message() {
        return sync_txt_message;
    }

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
        Tooltip.install(app_image, new Tooltip("© " + Year.now() + " Endeleya Corp. Kazisafe v"
                + pref.get("ksf_version", tools.Constants.APP_VERSION)));
        douwnload_update_pgi.setVisible(false);
        download_update_pgb.setVisible(false);
        install_update_link.setVisible(false);
        localPath = MainUI.cPath("/Media/Update");
        MainUI.cPath(File.separator+"datastore");
        pref.put("ksf_version", tools.Constants.APP_VERSION);
        taux = pref.getDouble("taux2change", 2300);
        network = new NetLoockup();
        maker = new PriceMaker();
        maker.setMainCurrency(pref.get("mainCur", "USD"));
        txt_states_features.setVisible(true);
        txt_states_features.setText("...");
        btn_theme_toggle.setText(pref.getBoolean(Kazisafex.DARK_THEME_PREF, false)
                ? bundle.getString("xbtn.theme.light")
                : bundle.getString("xbtn.theme.dark"));
        Platform.runLater(this::refreshThemeView);
        Platform.runLater(() -> setActiveMenu(home));
        searchField.focusedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (t1) {
                if (searchField.getText().isEmpty()) {
                    searchField.selectAll();
                }
            }
        });

        cloturer(LocalDate.now(), LocalDate.now(), "Journalier du " + LocalDate.now().toString());
        // sync();
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        boolean darkEnabled = !pref.getBoolean(Kazisafex.DARK_THEME_PREF, false);
        pref.putBoolean(Kazisafex.DARK_THEME_PREF, darkEnabled);
        refreshThemeView();
    }

    private void refreshThemeView() {
        if (mainpane == null || mainpane.getScene() == null) {
            return;
        }
        Kazisafex.applyTheme(mainpane.getScene());
        boolean darkEnabled = pref.getBoolean(Kazisafex.DARK_THEME_PREF, false);
        btn_theme_toggle.setText(darkEnabled
                ? bundle.getString("xbtn.theme.light")
                : bundle.getString("xbtn.theme.dark"));
        if (darkEnabled) {
            appName.setTextFill(Color.web("#a7d8ff"));
            tbar_menu.setStyle("-fx-background-color: #111827;");
        } else {
            appName.setTextFill(Color.web("#44cef5"));
            tbar_menu.setStyle("-fx-background-color: #ffffff;");
        }
    }

    public Label getTxt_states_features() {
        return txt_states_features;
    }

    public void cloturer(LocalDate d1, LocalDate d2, String context) {
        try {
            sync_txt_message.setVisible(true);
            sync_pg_bar.setVisible(true);
            MainUI.notify(null, "", "Veuillez patientez que la cloture de stock se termine", 15, "warning");
            sync_txt_message.setText("Cloture des stocks en cours....");
            ag = Agregator.getInstance();
            ag.setLocalTaskStateListener(new LocalTaskStateListener() {
                @Override
                public void onFinish(boolean isfinished, String name) {
                    if (name.contains("stock")) {
                        sync_txt_message.setVisible(false);
                        sync_pg_bar.setVisible(false);
                        Platform.runLater(() -> {
                            summarise();
                        });
                    }
                }

                @Override
                public void onProgress(double progress, String message) {
                    Platform.runLater(() -> {
                        sync_txt_message.setText(message);
                        sync_pg_bar.setProgress(progress);
                    });
                }
            });
            ag.agregate(d1, d2, d1.equals(d2) ? "Journalier du " + d1 : context);
            ag.setOnReportSavedListener((double chiffreAffaire, double coutVariable) -> {
                Platform.runLater(() -> {
                    dashCardVente(chiffreAffaire);
                    dashCardDepense(coutVariable);
                    dashCardResult(chiffreAffaire, coutVariable);
                });
            });
            ag.reportInBackground();
        } catch (java.lang.RuntimeException e) {
        }
    }

    public void sync(Kazisafe ksf) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleWithFixedDelay(() -> {
            if (ag != null) {
                if (ag.isFinish()
                        && NetLoockup.NETWORK_STATUS_ON) {
                    System.out.println("Connected on Internet");
                    Platform.runLater(() -> {
                        SyncEngine.getInstance().syncWithHttpProtocol(sync_txt_message, ksf);
                    });
                }
            }
        }, 1, 8, TimeUnit.MINUTES);
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

    public List<Vente> getVentes(LocalDate date) {
        LocalDate kesho = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, kesho);// db.findAllByDateInterval(Vente.class, d1,
                                                                           // kesho);
        if (vts == null) {
            return null;
        }
        return vts;
    }

    public List<Vente> getVentesDebt(LocalDate date) {
        LocalDate date1 = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, date1);
        return vts;
    }

    public List<Vente> getVentesDebt(LocalDate date, String region) {
        LocalDate date1 = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, date1, region);
        return vts;
    }

    public List<Vente> getVentesInMoth(String month) {
        List<Vente> result = new ArrayList<>();
        List<Vente> vts = VenteDelegate.findVentes();// db.findAll(Vente.class);
        for (Vente vt : vts) {
            String dv = String.valueOf(vt.getDateVente().getMonthValue());
            if (dv.equals(month)) {
                result.add(vt);
            }
        }
        return result;
    }

    public List<Operation> getOpsInMonth(String month) {
        List<Operation> result = new ArrayList<>();
        List<Operation> vts = OperationDelegate.findOperations();// db.findAll(Operation.class);
        if (vts != null) {
            for (Operation vt : vts) {
                String dv = String.valueOf(vt.getDate().getMonthValue());
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
                String dv = String.valueOf(vt.getDateVente().getMonthValue());
                if (dv.equals(month)) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Operation> getOpsInMonth(String month, String region) {
        List<Operation> result = new ArrayList<>();
        List<Operation> vts = OperationDelegate.findOperations(region);// db.findAllByRegion(Operation.class, region);
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

    public List<Operation> getOps(LocalDate date) {
        LocalDate kesho = date.plusDays(1);
        List<Operation> vts = OperationDelegate.findByDateInterval(date, kesho);
        return vts;
    }

    public List<Operation> getOps(LocalDate date, String region) {
        LocalDate kesho = date.plusDays(1);
        List<Operation> vts = OperationDelegate.findByDateInterval(date, kesho, region);
        return vts;
    }

    public List<Vente> getVentes(LocalDate date, String region) {
        LocalDate kesho = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, kesho, region);// db.findAllByDateIntervalInRegion(Vente.class,
                                                                                   // d1, kesho, region);
        return vts;
    }

    @FXML
    public void enlarge(Event e) {
        MainUI.enlarge();
    }

    public void dashCardVente(double sumSales) {
        System.out.println("Sum sale " + sumSales);
        String somV = maker.isUsd() ? "$ " + formatNumber(BigDecimal.valueOf(sumSales).setScale(1, RoundingMode.FLOOR)
                .doubleValue())
                : "Fc " + formatNumber(
                        BigDecimal.valueOf(sumSales * taux).setScale(2, RoundingMode.FLOOR).doubleValue());
        svente.setText(somV);
    }

    public void creanceToday() {
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            List<Vente> ventes = getVentesDebt(LocalDate.now());
            double sumSales = Util.sumCreditSales(ventes, taux);
            String somC = maker.isUsd() ? "$ " + formatNumber(BigDecimal.valueOf(sumSales)
                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                    : "Fc " + formatNumber(BigDecimal.valueOf(sumSales * taux)
                            .setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            screance.setText(somC);
        } else {
            List<Vente> ventes = getVentesDebt(LocalDate.now(), region);
            double sumSales = Util.sumCreditSales(ventes, taux);
            String somC = maker.isUsd() ? "$ " + formatNumber(BigDecimal.valueOf(sumSales)
                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                    : "Fc " + formatNumber(BigDecimal.valueOf(sumSales * taux)
                            .setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            screance.setText(somC);
        }
    }

    public void dashCardResult(double sumSales, double achat) {
        double result = sumSales - achat;
        String r = (maker.isUsd() ? ("$ " + formatNumber(BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_EVEN).doubleValue()))
                : "Fc " + formatNumber(BigDecimal.valueOf(result * taux)
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .doubleValue()));
        stresor.setText(r);
    }

    public void dashCardDepense(double achat) {
        sdepense.setText(maker.isUsd() ? ("$ " + formatNumber(achat)) : ("Fc " + formatNumber(achat * taux)));
    }

    public void metrify() {
        venteChart.setLegendVisible(true);
        XYChart.Series<String, Number> serie_vente = new XYChart.Series();
        XYChart.Series<String, Number> serie_prixderevient = new XYChart.Series();
        XYChart.Series<String, Number> serie_resultat = new XYChart.Series();
        serie_vente.setName(bundle.getString("xgraph.seri1_vente").trim());
        serie_prixderevient.setName(bundle.getString("xgraph.seri2_depens").trim());
        serie_resultat.setName(bundle.getString("xgraph.seri3_marg").trim());
        int month = LocalDate.now().getMonthValue();
        System.out.println("mois en cours " + month);
        List<Metric> kpis;

        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            kpis = RepportDelegate.kpiValues(LocalDate.of(Year.now().getValue(), Month.JANUARY, 1),
                    LocalDate.now(),
                    "%", "Mensuel");
        } else {
            kpis = RepportDelegate.kpiValues(LocalDate.of(Year.now().getValue(), Month.JANUARY, 1),
                    LocalDate.now(),
                    region, "Mensuel");
        }
        for (Metric kpi : kpis) {
            LocalDate period = kpi.period();
            String moix = period.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRANCE);
            serie_vente.getData().add(new XYChart.Data<>(moix, kpi.chiffreAffaire()));
            serie_prixderevient.getData().add(new XYChart.Data<>(moix, kpi.coutAchat()));
            serie_resultat.getData().add(new XYChart.Data<>(moix, kpi.result()));
        }

        venteChart.getData().addAll(serie_vente, serie_prixderevient, serie_resultat);
        for (XYChart.Series<String, Number> serie : venteChart.getData()) {
            for (XYChart.Data<String, Number> data : serie.getData()) {
                String text = serie.getName() + "\n" + data.getXValue() + " : "
                        + formatNumber(data.getYValue().doubleValue()) + " " + maker.getMainCurrency();
                Tooltip tooltip = new Tooltip(text);
                Tooltip.install(data.getNode(), tooltip);
                data.getNode().setStyle("-fx-background-color: #ff6600, white; -fx-padding: 5px;");
            }
        }
        venteChart.setLegendSide(Side.BOTTOM);
    }

    private double sumCout(List<Vente> ventes) {
        double coutTotal = 0;
        for (Vente vente : ventes) {
            List<LigneVente> items = LigneVenteDelegate.findByReference(vente.getUid());
            coutTotal += items.stream()
                    .mapToDouble(l -> l.getQuantite() * (l.getCoutAchat() == null ? 0 : l.getCoutAchat())).sum();
        }
        return coutTotal;
    }

    public void summarise() {
        maker.setMainCurrency(pref.get("mainCur", "USD"));
        System.out.println("is summary called");
        creanceToday();
        loadSaleChart();
        loadProChart();

    }

    String reg = null;

    private void loadProChart() {
        piepane.setPrefSize(351, 318);
        piepane.setLabelsVisible(true);
        List<TopTen> entries;
        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
            entries = VenteDelegate.getTop10ProductDesc();
        } else {
            entries = VenteDelegate.getTop10ProductDesc(region);
        }
        System.out.println("entree " + entries.size());
        for (TopTen top : entries) {
            PieChart.Data data = new PieChart.Data(top.nomp(), top.quantite());

            piepane.setAnimated(true);
            if (!existPie(piepane.getData(), data.getName())) {
                piepane.getData().add(data);
                Tooltip bull = new Tooltip(
                        data.getName() + " : " + formatNumber(data.getPieValue()) + " " + top.mesure());
                Tooltip.install(data.getNode(), bull);
                data.pieValueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                        bull.setText(data.getName() + " : " + formatNumber(data.getPieValue()) + " " + top.mesure());
                    }
                });

            }
        }
        piepane.setLabelsVisible(true);
    }

    private String formatNumber(double value) {
        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        } else {
            return Double.toString(value);
        }
    }

    private void loadSaleChart() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            venteChart.getData().clear();
                            metrify();
                        }
                    });
                }, 2, 60, TimeUnit.SECONDS);

    }

    public boolean existPie(ObservableList<PieChart.Data> data, String name) {
        for (PieChart.Data d : data) {
            if (d.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void setActiveMenu(ImageView target) {
        List<ImageView> menus = List.of(home, products_gate, caisse, immobilisation, stockage, pos, production, agents,
                rapport, compagnie, parametre);
        for (ImageView menu : menus) {
            if (menu == null) {
                continue;
            }
            if (menu.equals(target)) {
                menu.setOpacity(1d);
                menu.setScaleX(1.08);
                menu.setScaleY(1.08);
                menu.setStyle("-fx-effect: dropshadow(three-pass-box, #44cef5, 14, 0.2, 0, 0);");
            } else {
                menu.setOpacity(0.62);
                menu.setScaleX(1d);
                menu.setScaleY(1d);
                menu.setStyle("");
                MainUI.removeShaddowEffect(menu);
            }
        }
        activeMenuIcon = target;
    }

    private ImageView menuForView(String viewName) {
        return switch (viewName) {
            case tools.Constants.MAIN -> home;
            case tools.Constants.PRODUIT -> products_gate;
            case tools.Constants.CAISSES -> caisse;
            case tools.Constants.IMMOBILISATIONS -> immobilisation;
            case tools.Constants.STORAGE -> stockage;
            case tools.Constants.POS -> pos;
            case tools.Constants.PRODUCTION -> production;
            case tools.Constants.AGENTS -> agents;
            case tools.Constants.REPPORTS -> rapport;
            case tools.Constants.ENTREPRISE -> compagnie;
            case tools.Constants.PARAMETRES -> parametre;
            default -> null;
        };
    }

    public void setUserPhone(String phone) {
        this.phone = phone;

    }

    @FXML
    private void switchToDashBoard(MouseEvent event) {
        mainpane.getChildren().remove(0);
        mainpane.getChildren().add(showPane);
        pane_title.setText("Tableau de bord");
        image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/dashboard(2).png")));
        if (ag != null) {
            ag.reportInBackground();
        }
        CURRENT_VIEW = "DASHBOARD";
        setActiveMenu(home);
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
        Executors.newCachedThreadPool()
                .submit(() -> {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(viewName)) {
                                txt_states_features.setText("...");
                                AnchorPane p = MainUI.getPage(MainuiController.this, xml, token, getEntreprisex(),
                                        kazisafe);
                                if (p == null) {
                                    return;
                                }
                                p.setLayoutY(LAYOUTY);
                                p.setLayoutX(LAYOUTX);
                                mainpane.getChildren().remove(0);
                                mainpane.getChildren().add(p);
                                pane_title.setText(title);
                                image_title.setImage(new Image(
                                        MainuiController.this.getClass().getResourceAsStream("/icons/" + iconName)));
                                CURRENT_VIEW = viewName;
                                setActiveMenu(menuForView(viewName));
                            }
                        }
                    });
                });

    }

    public void switchScreens(String xml, String viewName, String title, String iconName, Vente v, Object liv) {
        if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(viewName)) {
            txt_states_features.setText("...");
            AnchorPane p = MainUI.getPage(this, xml, token, getEntreprisex(), v, liv);
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
            setActiveMenu(menuForView(viewName));
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
                txt_states_features.setText("...");
                AnchorPane p = MainUI.getPage(this, tools.Constants.AGENTS_VIEW, token, getEntreprisex(), kazisafe);
                p.setLayoutY(LAYOUTY);
                p.setLayoutX(LAYOUTX);
                mainpane.getChildren().remove(0);
                mainpane.getChildren().add(p);
                pane_title.setText("Agents");
                image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/hosting-services.png")));
                CURRENT_VIEW = tools.Constants.AGENTS;
                setActiveMenu(agents);
            }
        }

    }

    @FXML
    private void switchToCompany(MouseEvent event) {
        txt_states_features.setText("...");
        AnchorPane p = MainUI.getPage(this, tools.Constants.ENTREPRISE_VIEW, token, getEntreprisex(), kazisafe, user);
        p.setLayoutY(LAYOUTY);
        p.setLayoutX(LAYOUTX);
        mainpane.getChildren().remove(0);
        mainpane.getChildren().add(p);
        pane_title.setText("Entreprise");
        image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/office-building.png")));
        CURRENT_VIEW = tools.Constants.ENTREPRISE;
        setActiveMenu(compagnie);
    }

    public void sseSync() {
        if (go()) {
            new Thread(() -> {
                EventHandler evh = new tools.NotificationHandler();
                String url = KazisafeServiceFactory.BASE_URL + "notification/events";
                Headers headers = new Headers.Builder()
                        .add("Authorization", "Bearer " + token).build();
                EventSource.Builder evb = new EventSource.Builder(evh, URI.create(url))
                        .headers(headers)
                        .reconnectTime(1, TimeUnit.SECONDS).readTimeout(29, TimeUnit.HOURS);
                EventSource evs = evb.build();
                if (evh instanceof tools.NotificationHandler) {
                    ((tools.NotificationHandler) evh).setEventSource(evs);
                }

                evs.start();
                // try () {
                //
                // TimeUnit.MINUTES.sleep(6);
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
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
                        txt_states_features.setText("...");
                        AnchorPane p = MainUI.getPage(MainuiController.this, tools.Constants.PRODUITS_VIEW, token,
                                entreprisex);
                        p.setLayoutY(LAYOUTY);
                        p.setLayoutX(LAYOUTX);
                        mainpane.getChildren().remove(0);
                        mainpane.getChildren().add(p);
                        pane_title.setText("Produits");
                        image_title.setImage(new Image(this.getClass().getResourceAsStream("/icons/boxes.png")));
                        CURRENT_VIEW = tools.Constants.PRODUIT;
                        setActiveMenu(products_gate);
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
    private void switchToImmobilisation(MouseEvent event) {
        if (go()) {
            switchSimpleScreens(tools.Constants.IMMOBILISATION_VIEW, tools.Constants.IMMOBILISATIONS, "Immobilisations",
                    "annual-report.png");
        }
    }

    @FXML
    private void exit(Event event) {
        pref.putInt("exit", 1);
        if (!pref.getBoolean("session", false)) {
            pref.remove("token");
        }
        // if (sep != null) {
        // sep.closeSession();
        // }

        System.exit(0);
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
        if (img == activeMenuIcon) {
            return;
        }
        MainUI.removeShaddowEffect(img);
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

    public Entreprise getEntreprisex() {
        if (entreprisex == null) {
            String ezi = pref.get("eUid", "f3d81978a5524681bf1090d1d41edb15");
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
            entreprisex.setNomEntreprise(eName);
            entreprisex.setAdresse(adresse);
            entreprisex.setIdNat(idnat == null ? " " : idnat);
            entreprisex.setNumeroImpot(impot == null ? " " : impot);
            entreprisex.setPhones(phonez == null ? " " : phonez);
            entreprisex.setIdentification(id);
            entrep_name.setText(entreprisex.getNomEntreprise());
            user_connected.setText(user);
            entreprisex.setEmail(email);
        }
        return entreprisex;
    }

    private void initializePermissions(LoginWebResult loginResult, Runnable next) {
        // System.out.println("Permix-ion "+loginResult.getJsonPermissions()+"
        // "+loginResult.getRole());
        if (loginResult.getJsonPermissions() == null) {
            next.run();
            return;
        }
        if (loginResult.getJsonPermissions().startsWith("[")) {
            try {
                ObjectMapper drx = KazisafeServiceFactory.mapper();
                region = loginResult.getRegion();
                role = loginResult.getRole();
                List<Permission> perms = drx.readValue(loginResult.getJsonPermissions(),
                        new TypeReference<List<Permission>>() {
                        });
                List<Permission> tosave = perms.stream()
                        .map(p -> {
                            p.setTablename(loginResult.getUserContract());
                            return p;
                        }).collect(Collectors.toList());
                PermissionDelegate.renewPermissions(tosave);
                pref.put("priv", loginResult.getRole());
            } catch (JsonProcessingException ex) {
                Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        next.run();
    }

    private void initializePreferencesAndGui(LoginWebResult logr, Runnable next) {
        if (logr.getNomentreprise() != null) {
            pref.put("ent_name", logr.getNomentreprise());
            entrep_name.setText(logr.getNomentreprise());
        } else {
            String eName = pref.get("ent_name", "Chargement...");
            entrep_name.setText(eName);
            logr.setNomentreprise(eName);
        }
        role = logr.getRole();
        if (role == null) {
            role = pref.get("role", "Trader");
        }
        region = logr.getRegion();
        if (region == null) {
            region = pref.get("region", "Goma");
        }
        txt_region.setText(region);
        if (logr.getRccm() != null) {
            pref.put("ent_ID", logr.getRccm());
            this.rccm = logr.getRccm();
        } else {
            String id = pref.get("ent_ID", null);
            logr.setRccm(id);
            this.rccm = id;
        }
        if (logr.getAdresseEntreprise() != null) {
            pref.put("ent_adresse", logr.getAdresseEntreprise());
        } else {
            String adresse = pref.get("ent_adresse", null);
            logr.setAdresseEntreprise(adresse);
        }
        if (logr.getEmailEntreprise() != null) {
            pref.put("ent_email", logr.getEmailEntreprise());
        } else {
            String email = pref.get("ent_email", null);
            logr.setEmailEntreprise(email);
        }
        if (logr.getIdNat() != null) {
            pref.put("ent_idnat", logr.getIdNat());
        } else {
            String idnat = pref.get("ent_idnat", null);
            logr.setIdNat(idnat);
        }
        if (logr.getNumeroImpot() != null) {
            pref.put("ent_impot", logr.getNumeroImpot());
        } else {
            String impot = pref.get("ent_impot", null);
            logr.setNumeroImpot(impot);
        }
        if (logr.getPhoneEntrerprise() != null) {
            pref.put("ent_phones", logr.getPhoneEntrerprise());
        } else {
            String phonez = pref.get("ent_phones", null);
            logr.setPhoneEntrerprise(phonez);
        }
        String u = logr.getNomUtilisateur() + " " + logr.getPrenomUtilisateur();
        if (!u.contains("null")) {
            pref.put("operator", u);
        } else {
            u = pref.get("operator", "Chargement...");
            if (u.length() == 2) {
                logr.setNomUtilisateur(u.split(" ")[0]);
                logr.setPrenomUtilisateur(u.split(" ")[1]);
            }
        }
        entreprisex = new Entreprise(logr.getEntrepriseId());
        entreprisex.setNomEntreprise(logr.getNomentreprise());
        entreprisex.setAdresse(logr.getAdresseEntreprise());
        entreprisex.setCategory(logr.getCategoryEntreprise());
        entreprisex.setEmail(logr.getEmailEntreprise());
        entreprisex.setIdNat(logr.getIdNat());
        entreprisex.setIdentification(logr.getRccm());
        entreprisex.setNumeroImpot(logr.getNumeroImpot());
        entreprisex.setPhones(logr.getPhoneEntrerprise());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String nom = logr.getNomUtilisateur(), prenom = logr.getPrenomUtilisateur();
                user_connected.setText(
                        (nom != null && prenom != null) ? (nom + " " + prenom) : pref.get("operator", "Chargement..."));
            }
        });
        next.run();
    }

    private void initializeImages(LoginWebResult logr, Runnable next) {
        if (logr.getUserId() != null && !logr.getUserId().isBlank()) {
            kazisafe.downloadUserPhotoSecurely(logr.getUserId())
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
        }
        kazisafe.downloadLogo(logr.getEntrepriseId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rspns) {
                        if (rspns.isSuccessful()) {
                            try {
                                InputStream is = rspns.body().byteStream();
                                byte[] pixa = data.helpers.FileUtils.readFromFile(FileUtils.streamTofile(is));
                                FileUtils.byteToFile(logr.getEntrepriseId(), pixa, "png");
                            } catch (IOException ex) {
                                Logger.getLogger(MainuiController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable thrwbl) {
                        System.out.println("erreur logo e " + thrwbl.getMessage());
                    }
                });
        kazisafe.getAbonnements().enqueue(new Callback<List<Abonnement>>() {
            @Override
            public void onResponse(Call<List<Abonnement>> call, Response<List<Abonnement>> rspns) {
                if (rspns.isSuccessful()) {
                    List<Abonnement> abns = rspns.body();
                    System.out.println("Souscrizise-->> " + abns.size());
                    for (Abonnement abn : abns) {
                        String etat = abn.getEtat();
                        String typeAb = abn.getTypeAbonnement();

                        switch (typeAb) {
                            case "Gold", "Metal", "Super gold" -> {
                                pref.put("type-sub", typeAb);
                                String status = SubscriptionUtil.computeStatus(abn);
                                Duration time = SubscriptionUtil.remainingDuration(abn);
                                if (time.minusDays(7).isZero()) {
                                    MainUI.notify(null, "Attention",
                                            "Le crédit Kazisafe (Record) expire bientôt, pensez à le renouveller", 5,
                                            "warning");
                                }

                                long max = SubscriptionUtil.nextSubscriptionMillis(abn);
                                System.err.println("Abonnement total " + max + " rest " + time.toMillis());
                                pref.putDouble("sub", Double.valueOf(max));
                                pref.put("etat-sub", etat);
                                if (!status.equals(Constants.ETAT_SUBSCRIPTION_EXPIRY)) {
                                    MainUI.notifySync("Kazisafe-Abonnement",
                                            "Activation souscription " + typeAb + " faite avec succes",
                                            "Notification de souscription au service kazisafe");
                                }
                            }
                            case "PRO" -> {
                                double nombreOper = abn.getNombreOperation();
                                pref.put("pro-sub", typeAb);
                                pref.putDouble("subscripro", nombreOper);
                                pref.put("pro-etat", etat);
                                MainUI.notifySync("Kazisafe-Abonnement",
                                        "Abonnement " + typeAb + " de " + formatNumber(nombreOper) + " eBonus active",
                                        "Notification de souscription au service kazisafe");
                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<List<Abonnement>> call, Throwable thrwbl) {
                System.err.println("No network");
            }
        });

    }

    public void setLoginResult(LoginWebResult loginResult) {
        token = loginResult.getToken();
        kazisafe = KazisafeServiceFactory.createService(loginResult.getToken());
        KazisafeServiceFactory.setOnTokenRefreshCallback((Token var1) -> {
            MainuiController.this.token = var1.getToken();
            pref.put("token", var1.getToken());
        });
        // URL = KazisafeServiceFactory.WEBSOCKET + "/wssync/ez/" +
        // loginResult.getEntrepriseId() ;
        // sep = new SyncEndpoint(URL,token);
        pref.putInt("exit", 0);
        pref.putLong("entretime", loginResult.getCreationTimestamp());
        // if (services.ManagedSessionFactory.isEmbedded()) {
        // if (services.ManagedSessionFactory.isBdCreated()) {
        initializePermissions(loginResult, () -> {
            initializePreferencesAndGui(loginResult, () -> {
                initializeImages(loginResult, () -> {
                    kazisafe.getRegions().enqueue(new retrofit2.Callback<List<String>>() {
                        @Override
                        public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                            System.out.println("Reponse web server " + rspns.code());
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
                            System.err.println("Error : " + thrwbl);
                        }
                    });
                    SyncEngine.getInstance().setup(token);
                });
            });
        });
        // }
        // }
        System.out.println("permix " + loginResult.getRole());
        if (role.equals(Role.Saler.name())) {
            tbar_menu.getItems().remove(caisse);
            tbar_menu.getItems().remove(immobilisation);
            tbar_menu.getItems().remove(stockage);
            tbar_menu.getItems().remove(agents);
            tbar_menu.getItems().remove(production);
        } else if (role.equals(Role.Magazinner.name())) {
            tbar_menu.getItems().remove(caisse);
            tbar_menu.getItems().remove(immobilisation);
            tbar_menu.getItems().remove(agents);
            tbar_menu.getItems().remove(rapport);
        } else if (role.equals(Role.Finance.name())) {
            tbar_menu.getItems().remove(stockage);
            tbar_menu.getItems().remove(production);
            tbar_menu.getItems().remove(agents);
            tbar_menu.getItems().remove(rapport);
        } else if (role.equals(Role.Manager.name()) || role.contains(Role.ALL_ACCESS.name())) {
            tbar_menu.getItems().remove(agents);
        } else {

        }
        SyncEngine.getInstance().startChecking();
        searchField.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
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
                        case tools.Constants.IMMOBILISATIONS:
                            ImmobilisationController ic = ImmobilisationController.getInstance();
                            if (ic != null) {
                                ic.search(newValue);
                            }
                            break;
                        default:
                            break;
                    }
                });
        sseSync();
        sync(kazisafe);
    }

    private void subscribe() {
        // Executors.newSingleThreadExecutor()
        // .execute(() -> {
        // boolean network_on = pref.getBoolean(NetLoockup.NETWORK_STATUS,
        // NetLoockup.NETWORK_STATUS_DEFAULT);
        // if (network_on) {
        Executors.newCachedThreadPool()
                .submit(() -> {
                    Refresher ref = new Refresher();
                    ref.setTarget("CHK_SUB");
                    Util.sync(ref, "read", Tables.REFRESH);
                });
        // }
        // });
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
            } else if (CURRENT_VIEW.equals(tools.Constants.IMMOBILISATIONS)) {
                ImmobilisationController ic = ImmobilisationController.getInstance();
                if (ic != null) {
                    ic.search(searchField.getText());
                }
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
                MainUI.notify(null, "Attention",
                        "Vous n'avez plus de crédit Kazisafe (record) dans votre compte, veuillez recharger votre compte",
                        5, "warning");
                return false;
            }
        } else {
            long d2 = System.currentTimeMillis();
            long jrs = BigDecimal.valueOf(max).setScale(9).longValue();
            long remaind = jrs - d2;
            long week = tools.Constants.MILLSECONDS_JOURN * 7;
            System.err.println("Remained " + remaind + " week " + week);

            if (remaind <= 0) {
                MainUI.notify(null, "Attention",
                        "Vous n'avez plus de crédit Kazisafe (record) dans votre compte, veuillez récharger votre compte",
                        5, "warning");
                return false;
            }
            if (Math.abs(remaind) <= week) {
                MainUI.notify(null, "Attention", "Votre crédit Kazisafe expire bientôt, pensez à le renouveller", 5,
                        "warning");
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
                    MainUI.notify(null, "Erreur",
                            "Le fichier d'aide n'existe plus ou son nom \"Kazisafe guide d'utilisation.pdf\" d'origine a été modifié",
                            4, "error");
                }
            }

        }).start();
    }

    @FXML
    private void installUpdate(Event e) {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Cet action va fermer Kazisafe pour installer le mise a jour\nvoulez vous continuer ?",
                    ButtonType.YES, ButtonType.CANCEL);
            alert.setTitle("Attention!");
            alert.setHeaderText(null);
            Optional<ButtonType> showAndWait = alert.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                Runtime.getRuntime()
                        .exec(localPath + File.separator + (PlatformUtil.isWindows() ? newModule.getNomModule()
                                : PlatformUtil.isMac() ? "Kazisafe-MacOS.zip" : "Kazisafe-Linux.zip"));
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
                downTask.stateProperty().addListener((ObservableValue<? extends Worker.State> observable,
                        Worker.State oldValue, Worker.State newValue) -> {
                    if (downTask.getState() == newValue.SUCCEEDED) {
                        install_update_link.setVisible(true);
                    } else if (downTask.getState() == newValue.FAILED) {
                        MainUI.notify(null, "Erreur", "Le téléchargemement a été interrompu", 4, "error");
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
                    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                            Worker.State newValue) {
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

        Tooltip timo = new Tooltip();
        timo.setText("Immobilisations");
        timo.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(immobilisation, timo);

        Tooltip tvente = new Tooltip();
        tvente.setText(bundle.getString("xtooltip.vente_recquis"));
        tvente.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(pos, tvente);

        Tooltip tproduction = new Tooltip();
        tproduction.setText("Production");
        tproduction.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(production, tproduction);

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

    @FXML
    private void synchronizeWithServer(MouseEvent event) {
        if (isConnected) {
            SyncEngine.getInstance().syncWithHttpProtocol(label_status, kazisafe);
        } else {
            System.out.println("Pas de connection");
        }
    }

    @FXML
    private void switchToProduction(MouseEvent event) {
        if (go()) {
            switchSimpleScreens(tools.Constants.PRODUCTION_VIEW, Constants.PRODUCTION, "Production",
                    "production-line.png");
        }
    }

    @FXML
    private void callAssistantDialog(MouseEvent event) {
        MainUI.showAssistantIa(tools.Constants.ASSISTANT_DLG, 663, 685, this.entreprisex,
                this.user_connected.getText());
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
            try (InputStream is = connexion.getInputStream();
                    OutputStream os = Files.newOutputStream(Paths.get(localPath + "/" + url))) {
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
