/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.ProduitDelegate;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import data.Category;
import data.Client;
import data.Entreprise;
import data.LigneVente;
import data.Mesure;
import data.Operation;
import data.Produit;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import org.apache.commons.lang3.time.DateUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.ChartItem;
import tools.Constants;
import services.JpaStorage;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import tools.VenteReporter;
import data.helpers.Role; import data.network.Kazisafe; 

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class RepportController implements Initializable {

    public static RepportController getInstance() {
        return instance;
    }

    Kazisafe kazisafe;

    @FXML
    TextField searchRelv2;//search rep per prod
    @FXML
    TextField searchRelv; //search per cleint
    @FXML
    TextField searchRelv1;//search per cat

    @FXML
    private DatePicker dpk_debut_report;
    @FXML
    private DatePicker dpk_fin_report;

    @FXML
    private Label txt_vente_report;
//    @FXML
//    private ComboBox<String> cbx_agreggation_report;
    @FXML
    private LineChart<?, ?> lnchart_data_report;

    @FXML
    private Label txt_depense_report;
    @FXML
    private Label txt_creance_report;

    @FXML
    private Label txt_result_report;
    @FXML
    private ComboBox<String> cbx_duration_report, cbx_regions;
    ToggleGroup rbtngroup;
    Preferences pref;
    String role, region;
    JpaStorage store;
    ResourceBundle bundle;

    @FXML
    Tab sales_produx;
    @FXML
    Tab sales;
    @FXML
    Tab overview;
    @FXML
    Label totalSalePerPro;
    @FXML Label totalSaleperCli;
    @FXML Label totalSaleperCat;

    private static RepportController instance;
    Entreprise entreprise;
    @FXML
    TableView<VenteReporter> tbreport;
    @FXML
    TableColumn<VenteReporter, String> codebar;
    @FXML
    TableColumn<VenteReporter, String> produit;
    @FXML
    TableColumn<VenteReporter, String> quantite;
    @FXML
    TableColumn<VenteReporter, Number> chiffreAffaire;
    @FXML
    TableColumn<VenteReporter, String> percent;

    //categrory
    @FXML
    TableView<VenteReporter> tb_cat_report;
    @FXML
    TableColumn<VenteReporter, String> col_category;
    @FXML
    TableColumn<VenteReporter, Number> cat_chiffreAffaire;
    @FXML
    TableColumn<VenteReporter, String> cat_percent;

    //per client
    @FXML
    TableView<VenteReporter> clt_tbreport;
    @FXML
    TableColumn<VenteReporter, String> clt_phone;
    @FXML
    TableColumn<VenteReporter, String> clt_name;
    @FXML
    TableColumn<VenteReporter, String> clt_type;
    @FXML
    TableColumn<VenteReporter, Number> clt_chiffreAffaire;
    @FXML
    TableColumn<VenteReporter, String> clt_percent;

    double taux;

    ObservableList<Vente> lsventes;
    ObservableList<Operation> lsoperations;
    ObservableList<Traisorerie> ltxt_result_reporterie;
    ObservableList<VenteReporter> ventePr;
    ObservableList<VenteReporter> ventePerCategory;
    ObservableList<VenteReporter> ventePerClient;
    List<List<ChartItem>> cis;

    ObservableList<String> regions;
    @FXML
    Label depense_proportion;

    public RepportController() {
        instance = this;
        regions = FXCollections.observableArrayList();
        cis = new ArrayList<>();
    }

    public void configTableVentePerProd() {
        codebar.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            return new SimpleStringProperty(im.getCodebar());
        });
        produit.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            String pro=im.getProduit();
            if(pro==null){
                Produit pr=ProduitDelegate.findByCodebar(im.getCodebar());
                pro=pr.getNomProduit()+" "+pr.getMarque()+" "+pr.getModele()+" "+(pr.getTaille()==null?"":pr.getTaille());
            }
            return new SimpleStringProperty(pro);
        });
        quantite.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            Mesure m = im.getMesure();
            double qu = (im.getQuantite() / m.getQuantContenu());
            double w = BigDecimal.valueOf(qu).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleStringProperty(w + " " + m.getDescription());
        });
        chiffreAffaire.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, Number> param) -> {
            VenteReporter im = param.getValue();
            return new SimpleDoubleProperty(im.getChiffre());
        });
        percent.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            double pr = (im.getChiffre() / im.getSommeTotal())*100;
            return new SimpleStringProperty(BigDecimal.valueOf(pr).setScale(1, RoundingMode.HALF_EVEN).doubleValue() + "%");
        });
        //per category

        col_category.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            Category c = im.getCategory();
            return new SimpleStringProperty(c.getDescritption());
        });
        cat_chiffreAffaire.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, Number> param) -> {
            VenteReporter im = param.getValue();
            return new SimpleDoubleProperty(im.getChiffre());
        });
        cat_percent.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            double pr = (im.getChiffre() / im.getSommeTotal())*100;
            return new SimpleStringProperty(BigDecimal.valueOf(pr).setScale(1, RoundingMode.HALF_EVEN).doubleValue() + "%");
        });

        //per client
        clt_phone.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            Client c = im.getClient();
            return new SimpleStringProperty(c.getPhone());
        });
        clt_name.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            Client c = im.getClient();
            return new SimpleStringProperty(c.getNomClient());
        });
        clt_type.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            Client c = im.getClient();
            String typecli = c.getTypeClient().equals("#0") ? bundle.getString("consumer")
                    : c.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                    : c.getTypeClient().equals("#2") ? bundle.getString("detailor")
                    : c.getTypeClient().equals("#3") ? bundle.getString("subscriber") : bundle.getString("consumer");
            return new SimpleStringProperty(typecli);
        });
        clt_chiffreAffaire.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, Number> param) -> {
            VenteReporter im = param.getValue();
            return new SimpleDoubleProperty(im.getChiffre());
        });
        clt_percent.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            double pr = (im.getChiffre() / im.getSommeTotal())*100;
            return new SimpleStringProperty(BigDecimal.valueOf(pr).setScale(1, RoundingMode.HALF_EVEN).doubleValue() + "%");
        });
    }

    public void setup(Entreprise entr, Kazisafe kazi) {
        store = JpaStorage.getInstance();
        this.kazisafe = kazi;
        this.entreprise = entr;
        taux = pref.getDouble("taux2change", 2000);
        ventePr = FXCollections.observableArrayList();
        ventePerCategory = FXCollections.observableArrayList();
        ventePerClient = FXCollections.observableArrayList();
        lsoperations = FXCollections.observableArrayList();
        ltxt_result_reporterie = FXCollections.observableArrayList();
        lsventes = FXCollections.observableArrayList();
        cbx_duration_report.setItems(FXCollections.observableArrayList("Ponctuel", "Par jours", "Par mois", "Par année"));
        cbx_regions.setItems(regions);
        tbreport.setItems(ventePr);
        tb_cat_report.setItems(ventePerCategory);
        clt_tbreport.setItems(ventePerClient);

        searchRelv2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isEmpty()) {
                    ObservableList<VenteReporter> rsult = FXCollections.observableArrayList();
                    for (VenteReporter vpr : ventePr) {
                        String q = vpr.getCodebar() + " " + vpr.getProduit() + " " + vpr.getMesure() + " " + Constants.USER_READABLE_FORMAT.format(vpr.getDate());
                        if (q.toUpperCase().contains(newValue.toUpperCase())) {
                            rsult.add(vpr);
                        }
                    }
                    tbreport.setItems(rsult);
                } else {
                    tbreport.setItems(ventePr);
                }
            }
        });

        searchRelv.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isEmpty()) {
                    ObservableList<VenteReporter> rsult = FXCollections.observableArrayList();
                    for (VenteReporter vpr : ventePerClient) {
                        Client c = vpr.getClient();
                        String typecli = c.getTypeClient().equals("#0") ? bundle.getString("consumer")
                                : c.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                                : c.getTypeClient().equals("#2") ? bundle.getString("detailor")
                                : c.getTypeClient().equals("#3") ? bundle.getString("subscriber") : bundle.getString("consumer");
                        String q = c.getNomClient() + " " + typecli + " " + c.getAdresse() + " " + c.getPhone();
                        if (q.toUpperCase().contains(newValue.toUpperCase())) {
                            rsult.add(vpr);
                        }
                    }
                    clt_tbreport.setItems(rsult);
                } else {
                    clt_tbreport.setItems(ventePerClient);
                }
            }
        });
        searchRelv1.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isEmpty()) {
                    ObservableList<VenteReporter> rsult = FXCollections.observableArrayList();
                    for (VenteReporter vpr : ventePerCategory) {
                        Category c = vpr.getCategory();
                        String q = c.getDescritption();
                        if (q.toUpperCase().contains(newValue.toUpperCase())) {
                            rsult.add(vpr);
                        }
                    }
                    tb_cat_report.setItems(rsult);
                } else {
                    tb_cat_report.setItems(ventePerCategory);
                }
            }
        });
        cbx_regions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) {
                    return;
                }

                evaluate(newValue);
            }
        });
        dpk_debut_report.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                evaluate();
            }
        });
        dpk_debut_report.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                evaluate();
            }
        });
        dpk_debut_report.setValue(LocalDate.now().minusMonths(LocalDate.now().getMonthValue() - 1));
        dpk_fin_report.setValue(LocalDate.now().plusMonths((12 - LocalDate.now().getMonthValue())));
        cbx_duration_report.getSelectionModel().selectFirst();
        evaluate();
        ponctuel();
        summarise();
        if(entr==null){
            MainUI.notify(null, "Info", "Certains elements seront visibles apres le chargement complet des info de l'entreprise", 3, "warning");
            return;
        }
        kazisafe.getRegions(entr.getUid()).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        pref.put("region" + (++i), reg);
                    }
                    System.err.println("Rapport regions " + lreg.size());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable thrwbl) {
                for (String key : regKeys()) {
                    String r = pref.get(key, "...");
                    if (!regions.contains(r)) {
                        regions.add(r);
                    }
                }
            }
        });
        cbx_duration_report.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.equals("Ponctuel")) {
                    ponctuel();
                } else {
                    evaluate();
                }

            }
        });
    }

    private List<String> regKeys() {
        List<String> result = new ArrayList<>();
        try {

            for (String key : pref.keys()) {
                if (key.startsWith("region")) {
                    result.add(key);
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(DestockController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    @FXML
    private void genPerCategory(Event e){
         new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsrep = Util.exportXlsSalePerCategory(ventePerCategory);
                try {
                    Desktop.getDesktop().open(xlsrep);
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    
     @FXML
    private void genPerClient(Event e){
         new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsrep = Util.exportXlsSalePerClient(ventePerClient,bundle);
                try {
                    Desktop.getDesktop().open(xlsrep);
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void evaluate() {
        ventePr.clear();
        ventePerCategory.clear();
        ventePerClient.clear();
        lnchart_data_report.getData().clear();
        Date date1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        Date date2 = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        List<VenteReporter> fxv = store.findReportSaleByProduct(date1, date2);
        Double somv = store.sumVente(date1, date2, taux);
        double sumPpro = 0;
        for (VenteReporter vpp : fxv) {
            vpp.setSommeTotal(somv);
            ventePr.add(vpp);
            sumPpro += vpp.getChiffre();
        }
        
        totalSalePerPro.setText("Global sale amount :" + sumPpro + " USD");
        List<VenteReporter> cat = store.findReportSaleByCategory(date1, date2);
        sumPpro = 0;
        for (VenteReporter vpp : cat) {
            vpp.setSommeTotal(somv);
            ventePerCategory.add(vpp);
            sumPpro += vpp.getChiffre();
        }
        totalSaleperCat.setText("Global sale amount :" + sumPpro + " USD");
        List<VenteReporter> clts = store.findReportSaleByClient(date1, date2,taux);
        sumPpro = 0;
        for (VenteReporter vpp : clts) {
            vpp.setSommeTotal(somv);
            ventePerClient.add(vpp);
            sumPpro += vpp.getChiffre();
        }
        totalSaleperCli.setText("Global sale amount :" + sumPpro + " USD");
        summarise();
    }
     private void evaluate(String region) {
        ventePr.clear();
        ventePerCategory.clear();
        ventePerClient.clear();
        lnchart_data_report.getData().clear();
        Date date1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        Date date2 = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        List<VenteReporter> fxv = store.findReportSaleByProduct(date1, date2,region);
        Double somv = store.sumVente(date1, date2, taux);
        double sumPpro = 0;
        for (VenteReporter vpp : fxv) {
            vpp.setSommeTotal(somv);
            ventePr.add(vpp);
            sumPpro += vpp.getChiffre();
        }
        
        totalSalePerPro.setText("Global sale amount :" + sumPpro + " USD");
        List<VenteReporter> cat = store.findReportSaleByCategory(date1, date2,region);
        sumPpro = 0;
        for (VenteReporter vpp : cat) {
            vpp.setSommeTotal(somv);
            ventePerCategory.add(vpp);
            sumPpro += vpp.getChiffre();
        }
        totalSaleperCat.setText("Global sale amount :" + sumPpro + " USD");
        List<VenteReporter> clts = store.findReportSaleByClient(date1, date2,region,taux);
        sumPpro = 0;
        for (VenteReporter vpp : clts) {
            vpp.setSommeTotal(somv);
            ventePerClient.add(vpp);
            sumPpro += vpp.getChiffre();
        }
        totalSaleperCli.setText("Global sale amount :" + sumPpro + " USD");
        summarise();
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

    public void summarise() {
        System.out.println("is summary called");
        dashCardVente();
        dashCardDepense();
        dashCardResult();
        loadSaleChart();
        creanceToday();

    }

    private void ponctuel() {
        lnchart_data_report.getData().clear();
        // summarise();
//        String group = cbx_agreggation_report.getValue();
        metrify();
    }

    @FXML
    public void showCurrentInTable(ActionEvent evt) {
        if (dpk_debut_report.getValue() != null && dpk_fin_report.getValue() != null) {
            if (sales_produx.isSelected()) {

            } else if (overview.isSelected()) {

            }
            evaluate();
        }
    }

    @FXML
    private void onHoverHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.setShadowEffect(img);
    }

    @FXML
    private void onOutHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.removeShaddowEffect(img);
    }

    @FXML
    private void exportReport(MouseEvent event) {
        if (cis.isEmpty()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
//                File xlsrep = Util.exportXlsReports(cis, cbx_agreggation_report.getValue(), cbx_duration_report.getValue());
//                try {
//                    Desktop.getDesktop().open(xlsrep);
//                } catch (IOException ex) {
//                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }).start();
    }

    @FXML
    private void exportSalePerProducReport(Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsrep = Util.exportXlsSalePerProductReports(ventePr);
                try {
                    Desktop.getDesktop().open(xlsrep);
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @FXML
    private void exportPdfReport(MouseEvent event) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                File pdfrep;
                if (role.equals(Role.Trader.name())) {
                    pdfrep = Util.exportPDFicheDebiteurs(Util.getDebts(store.findAll(Vente.class), store.findAll(Traisorerie.class), taux));
                } else {
                    pdfrep = Util.exportPDFicheDebiteurs(Util.getDebts(store.findAllByRegion(Vente.class, region), store.findAllByRegion(Traisorerie.class, region), taux));
                }
                try {
                    Desktop.getDesktop().open(pdfrep);
                } catch (IOException ex) {
                    MainUI.notify(null, bundle.getString("error"), "Erruer du soit a aucun program associe au fomat PDF trouve sur votre ordianteur", 3, "error");
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        rbtngroup = new ToggleGroup();
//        rdbtn_vente_report.setToggleGroup(rbtngroup);
//        rdbtn_result_report.setToggleGroup(rbtngroup);
//        rdbtn_depense_report.setToggleGroup(rbtngroup);
        MainUI.setPattern(dpk_fin_report);
        MainUI.setPattern(dpk_debut_report);
        dpk_debut_report.setValue(LocalDate.now());
        dpk_fin_report.setValue(LocalDate.now().minusMonths(1));
        configTableVentePerProd();

        region = pref.get("region", "...");
        role = pref.get("priv", null);
    }

    public List<Vente> getVentes(Date date, Date date2, String region) {
        List<Vente> vts = store.findVenteCreditByDateInterval(date, date2, region);
        return vts;
    }

    public void dashCardVente() {
        Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        if (role.equals(Role.Trader.name())) {
            double sumSales = store.sumVente(d1, kesho, taux);
            //Util.sumSales(ventes, taux);
            System.out.println("Sum sale " + sumSales);
            txt_vente_report.setText("$ " + BigDecimal.valueOf(sumSales).setScale(1, RoundingMode.FLOOR).doubleValue());
        } else {
            // List<Vente> ventes = getVentes(new Date(), region);
            double sumSales = store.sumVente(d1, kesho, region, taux);
            txt_vente_report.setText("$ " + BigDecimal.valueOf(sumSales).setScale(1, RoundingMode.FLOOR).doubleValue());
        }
    }

    public void creanceToday() {
        if (role.equals(Role.Trader.name())) {
            List<Vente> ventes = getVentesDebt(new Date());
            double sumSales = Util.sumCreditSales(ventes, taux);
            txt_creance_report.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        } else {
            List<Vente> ventes = getVentesDebt(new Date(), region);
            double sumSales = Util.sumCreditSales(ventes, taux);
            txt_creance_report.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        }
    }

    public void dashCardResult() {

        Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());

        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        if (role.equals(Role.Trader.name())) {
            double sumSales = store.sumVente(d1, kesho, taux);
            double achat = store.sumCoutAchatArticleVendu(d1, kesho, null);
            double sum = store.sumExpenses(d1, kesho, taux);
            double exp = achat + sum;
            double result = sumSales - exp;
            txt_result_report.setText("$ " + BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN));
        } else {
            double sumSales = store.sumVente(d1, kesho, region, taux);
            double achat = store.sumCoutAchatArticleVendu(d1, kesho, region);
            double sum = store.sumExpenses(d1, kesho, region, taux);
            double exp = achat + sum;
            double result = sumSales - exp;
            txt_result_report.setText("$ " + BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN));
        }
    }

    public double dashAchat(List<Vente> vents) {
        double sB = 0;
        for (Vente vent : vents) {
            List<LigneVente> lvs = store.findByRef(LigneVente.class, vent.getUid());
            //store.findAll("reference.uid", vent.getUid());
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
                Vente v = store.findByUid(Vente.class, vref.getUid());
                double smv = v.getMontantCdf() + v.getMontantUsd() + (v.getMontantDette() == null
                        ? 0 : v.getMontantDette());
                if (smv == 0) {
                    continue;
                }
                Produit p = lv.getProductId();
                List<Stocker> stocks = store.findByProduitWithLotDesc(Stocker.class, p.getUid(), lv.getNumlot(), "dateStocker");
                if (stocks.isEmpty()) {
                    continue;
                }
                Stocker s = stocks.get(0);
                Mesure mstok = s.getMesureId();

                Mesure mreel = store.findByUid(Mesure.class, mstok.getUid());
                if (mreel == null) {
                    List<Mesure> mesures = store.findByProduitAsc(Mesure.class, p.getUid(), "quantContenu");
                    mreel = mesures.get(0);
                }
                double qpcsti = mreel.getQuantContenu();
                double qpcst = qpcsti == 0 ? 1 : mreel.getQuantContenu();
                double coutAchat = s.getCoutAchat();
                double pupc = coutAchat / qpcst;
                Mesure mvndu = lv.getMesureId();
                Mesure mreelv = store.findByUid(Mesure.class, mvndu.getUid());
                if (mreelv == null) {
                    List<Mesure> mesures = store.findByProduitAsc(Mesure.class, p.getUid(), "quantContenu");
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
            List<LigneVente> lvs = store.findByRef(LigneVente.class, vent.getUid());
            double sm = 0;
            if (lvs == null) {
                continue;
            }
            for (LigneVente lv : lvs) {
                if (lv.getNumlot() == null) {
                    continue;
                }
                Vente v = store.findByUid(Vente.class, lv.getReference().getUid());
                double smv = v.getMontantCdf() + v.getMontantUsd() + (v.getMontantDette() == null
                        ? 0 : v.getMontantDette());
                if (smv == 0) {
                    continue;
                }
                Produit p = lv.getProductId();
                List<Stocker> stocks = store.findByProduitWithLotDesc(Stocker.class, p.getUid(), lv.getNumlot(), "dateStocker", region);
                if (stocks.isEmpty()) {
                    continue;
                }
                Stocker s = stocks.get(0);
                Mesure mstok = s.getMesureId();
                Mesure mzr = store.findByUid(Mesure.class, mstok.getUid());
                if (mzr == null) {
                    List<Mesure> mesures = store.findByProduitAsc(Mesure.class, p.getUid(), "quantContenu");
                    mzr = mesures.get(0);
                }
                double qpcst = mzr.getQuantContenu();
                double coutAchat = s.getCoutAchat();
                double pupc = coutAchat / qpcst;
                Mesure mvndu = lv.getMesureId();
                Mesure mzir = store.findByUid(Mesure.class, mvndu.getUid());
                if (mzir == null) {
                    List<Mesure> mesures = store.findByProduitAsc(Mesure.class, p.getUid(), "quantContenu");
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
        Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        if (role.equals(Role.Trader.name())) {
            double achat = store.sumCoutAchatArticleVendu(d1, kesho, null);
            double sum = store.sumExpenses(d1, kesho, taux);
            double exp = achat + sum;
            txt_depense_report.setText("$ " + BigDecimal.valueOf(exp).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            if (exp != 0) {
                double p = (sum / exp) * 100;
                p = BigDecimal.valueOf(p).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), p, "%", sum));
            } else {
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), "0.0", "%", 0.0));
            }
        } else {
            double achat = store.sumCoutAchatArticleVendu(d1, kesho, region);
            double sum = store.sumExpenses(d1, kesho, region, taux);
            double exp = achat + sum;
            txt_depense_report.setText("$ " + BigDecimal.valueOf(exp).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            if (exp != 0) {
                double p = (sum / exp) * 100;
                p = BigDecimal.valueOf(p).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), p, "%", sum));
            } else {
                depense_proportion.setText(String.format(bundle.getString("xlbel.depense_proport"), "0.0", "%", 0.0));
            }
        }
    }

    public List<Vente> getVentesDebt(Date date) {
        Calendar cexp = Calendar.getInstance();
        cexp.setTime(date);
        cexp.set(Calendar.HOUR, 0);
        cexp.set(Calendar.MINUTE, 59);
        cexp.set(Calendar.SECOND, 59);
        cexp.set(Calendar.MILLISECOND, 0);
        Date date1 = DateUtils.addDays(date, 1);
        List<Vente> vts = store.findVenteCreditByDateInterval(cexp.getTime(), date1);
        //Util.getByDay(store.findAll(), new Date());

        return vts;
    }

    public List<Vente> getVentesDebt(Date date, String region) {
        Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        List<Vente> vts = store.findVenteCreditByDateInterval(d1, kesho, region);

        return vts;
    }

    private void loadSaleChart() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lnchart_data_report.getData().clear();
                metrify();
            }
        });

    }

    String reg = null;

    public List<Operation> getOps(Date date) {
        Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        List<Operation> vts = store.findAllByDateInterval(Operation.class, d1, kesho);
        return vts;
    }

    public List<Operation> getOps(Date date, String region) {
        Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());
        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        List<Operation> vts = store.findAllByDateIntervalInRegion(Operation.class, d1, kesho, region);
        return vts;
    }

    public void metrify() {
        lnchart_data_report.setLegendVisible(true);
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
//         Date kesho = Constants.Datetime.toUtilDate(dpk_fin_report.getValue());

//        Date d1 = Constants.Datetime.toUtilDate(dpk_debut_report.getValue());
        int month = leo.get(Calendar.MONTH) + 1;
        for (int i = 1; i <= month; i++) {
            try {
                String suffix = String.format("%02d", i);
                String firstday = leo.get(Calendar.YEAR) + "-" + suffix + "-01";
                Date date1 = tools.Constants.dateFormater.parse(firstday);
                Calendar d2 = Calendar.getInstance();
                d2.setTime(date1);
                int maxday = d2.getActualMaximum(Calendar.DAY_OF_MONTH);
                String lastday = leo.get(Calendar.YEAR) + "-" + suffix + "-" + maxday;
                Date date2 = tools.Constants.dateFormater.parse(lastday);
                String moix = getMonthName(firstday.substring(0, firstday.lastIndexOf("-")));
                if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                    System.out.println("vente ajour " + lastday);
                    double sumSales = store.sumVente(date1, date2, taux);

                    double achat = store.sumCoutAchatArticleVendu(date1, date2, null);
                    double sum = store.sumExpenses(date1, date2, taux);
                    double prixrevient = achat + sum;
                    double result = sumSales - prixrevient;
                    serie_vente.getData().add(new XYChart.Data<>(moix, sumSales));
                    serie_prixderevient.getData().add(new XYChart.Data<>(moix, prixrevient));
                    serie_resultat.getData().add(new XYChart.Data<>(moix, result));
                } else {
                    System.out.println("vente ajour reg" + lastday);
                    double sumSales = store.sumVente(date1, date2, region, taux);
                    serie_vente.getData().add(new XYChart.Data<>(moix, sumSales));
                    double achat = store.sumCoutAchatArticleVendu(date1, date2, region);
                    double sum = store.sumExpenses(date1, date2, region, taux);
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
        lnchart_data_report.getData().add(serie_vente);
        lnchart_data_report.getData().add(serie_prixderevient);
        lnchart_data_report.getData().add(serie_resultat);
        lnchart_data_report.setLegendSide(Side.BOTTOM);

    }

}
