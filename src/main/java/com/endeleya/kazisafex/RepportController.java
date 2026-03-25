/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
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
import javafx.scene.chart.LineChart;
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
import javafx.scene.layout.AnchorPane;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.stage.Stage;

import data.Client;
import data.Entreprise;
import data.Immobilisation;
import data.Operation;
import data.Periode;
import tools.RecentSale;
import data.Traisorerie;
import data.Vente;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.ChartItem;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import tools.VenteReporter;
import data.helpers.Role;
import data.network.Kazisafe;
import data.core.KazisafeServiceFactory;
import data.finance.BilanReport;
import data.finance.CompteResultatReport;
import delegates.PeriodeDelegate;
import delegates.RepportDelegate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import javafx.scene.image.Image;
import tools.SaleReport;

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
    TextField searchRelv2;// search rep per prod
    @FXML
    TextField searchRelv; // search per cleint
    @FXML
    TextField searchRelv1;// search per cat

    @FXML
    private DatePicker dpk_debut_report;
    @FXML
    private DatePicker dpk_fin_report;

    Periode choosenPeriod;

    @FXML
    private Label txt_vente_report;
    // @FXML
    // private ComboBox<String> cbx_agreggation_report;
    @FXML
    private LineChart<String, Number> lnchart_data_report;

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
    String role, region, devise;
    ResourceBundle bundle;

    @FXML
    Tab sales_produx;
    @FXML
    Tab sales;
    @FXML
    Tab tab_immobilisation;
    Tab overview;
    @FXML
    Label totalSalePerPro;
    @FXML
    Label totalSaleperCli;
    @FXML
    Label totalSaleperCat;

    private BilanReport currentBilan;
    private CompteResultatReport currentCR;

    private static RepportController instance;
    Entreprise entreprise;
    @FXML
    TableView<SaleReport> tbreport;
    @FXML
    TableColumn<SaleReport, String> codebar;
    @FXML
    TableColumn<SaleReport, String> produit;
    @FXML
    TableColumn<SaleReport, String> quantite;
    @FXML
    TableColumn<SaleReport, String> coutachat;
    @FXML
    TableColumn<SaleReport, Number> chiffreAffaire;
    @FXML
    TableColumn<SaleReport, String> percent;
    @FXML
    private TableColumn<SaleReport, String> col_marge_preport;

    // categrory
    @FXML
    TableView<SaleReport> tb_cat_report;
    @FXML
    TableColumn<SaleReport, String> col_category;
    @FXML
    TableColumn<SaleReport, Number> cat_chiffreAffaire;
    @FXML
    TableColumn<SaleReport, String> cat_percent;

    // per client
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
    ObservableList<SaleReport> ventePr;
    ObservableList<SaleReport> ventePerCategory;
    ObservableList<VenteReporter> ventePerClient;
    List<List<ChartItem>> cis;

    ObservableList<String> regions;
    Label depense_proportion;
    // @FXML
    // private ComboBox<Periode> cbx_periode_rapport;
    @FXML
    private TableColumn<VenteReporter, String> col_si_preport;
    @FXML
    private TableColumn<VenteReporter, String> col_entree_preport;
    @FXML
    private TableColumn<VenteReporter, String> col_sf_preport;
    @FXML
    private TableColumn<VenteReporter, String> col_ecart_preport;
    @FXML
    private TableColumn<VenteReporter, String> col_retour_stk_preport;

    @FXML
    private TableView<RecentSale> recentSales;
    @FXML
    private TableColumn<RecentSale, String> col_facture_recent;
    @FXML
    private TableColumn<RecentSale, String> col_produit_recent;
    @FXML
    private TableColumn<RecentSale, String> col_quantite_recent;
    @FXML
    private TableColumn<RecentSale, String> col_total_recent;
    @FXML
    private ComboBox<String> cbx_periodicity;
    @FXML
    private Label lbl_comment_CA;
    @FXML
    private ImageView img_indic_CA;
    @FXML
    private Label lbl_comment_CV;
    @FXML
    private ImageView img_indic_CV;
    @FXML
    private Label lbl_comment_MARGE;
    @FXML
    private ImageView img_indic_MARGE;
    @FXML
    private TableView<Immobilisation> tb_immobilisations;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_libelle;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_cat;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_region;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_date;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_valeur;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_dotation;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_cumul;
    @FXML
    private TableColumn<Immobilisation, String> col_imo_vnc;
    @FXML
    private TextField search_imo;
    @FXML
    private Label lbl_imo_status;
    @FXML
    private Label lbl_bilan_immobilise;
    @FXML
    private Label lbl_bilan_circulant;
    @FXML
    private Label lbl_bilan_dispo;
    @FXML
    private Label lbl_bilan_total_actif;
    @FXML
    private Label lbl_bilan_capitaux;
    @FXML
    private Label lbl_bilan_dettes;
    @FXML
    private Label lbl_bilan_total_passif;
    @FXML
    private Label lbl_cr_ca;
    @FXML
    private Label lbl_cr_cout_vente;
    @FXML
    private Label lbl_cr_marge;
    @FXML
    private Label lbl_cr_dep_ops;
    @FXML
    private Label lbl_cr_amort;
    @FXML
    private Label lbl_cr_var_stock;
    @FXML
    private Label lbl_cr_result_expl;
    @FXML
    private Label lbl_cr_impot;
    @FXML
    private Label lbl_cr_result_net;
    @FXML
    private Label lbl_cr_industriel;
    @FXML
    private Label lbl_flux_encaiss;
    @FXML
    private Label lbl_flux_decaiss;
    @FXML
    private Label lbl_flux_net;
    @FXML
    private AnchorPane pane_financial_states;

    private ObservableList<Immobilisation> immobilisations;

    public RepportController() {
        instance = this;
        regions = FXCollections.observableArrayList();
        cis = new ArrayList<>();
    }

    public void configTableVentePerProd() {
        codebar.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            return new SimpleStringProperty(im.codebar());
        });
        produit.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            return new SimpleStringProperty(im.produit());
        });
        quantite.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            double qu = im.quantite();
            double wx = BigDecimal.valueOf(qu).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleStringProperty(wx + " " + im.unite());
        });
        coutachat.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            double cout = im.coutAchat();
            double w = BigDecimal.valueOf(cout).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleStringProperty(w + im.devise());
        });
        col_marge_preport.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            double qu = im.marge();
            double w = BigDecimal.valueOf(qu).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleStringProperty(w + im.devise());
        });
        chiffreAffaire.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, Number> param) -> {
            SaleReport im = param.getValue();
            return new SimpleDoubleProperty(im.vente());
        });
        percent.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            double pr = im.percentMarge();
            return new SimpleStringProperty(
                    BigDecimal.valueOf(pr).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + "%");
        });
        // col_si_preport.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter,
        // String> param) -> {
        // VenteReporter im = param.getValue();
        // Mesure m = im.getMesure();
        // double qu = im.getStockInitial();
        // double w = BigDecimal.valueOf(qu).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue();
        // return new SimpleStringProperty(w + " " + m.getDescription());
        // });
        // col_sf_preport.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter,
        // String> param) -> {
        // VenteReporter im = param.getValue();
        // Mesure m = im.getMesure();
        // double qu = (im.getStockFinal());
        // double w = BigDecimal.valueOf(qu).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue();
        // return new SimpleStringProperty(w + " " + m.getDescription());
        // });
        // col_entree_preport.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter,
        // String> param) -> {
        // VenteReporter im = param.getValue();
        // Mesure m = im.getMesure();
        // double qu = (im.getEntrees());
        // double w = BigDecimal.valueOf(qu).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue();
        // return new SimpleStringProperty(w + " " + m.getDescription());
        // });
        // col_ecart_preport.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter,
        // String> param) -> {
        // VenteReporter im = param.getValue();
        // Mesure m = im.getMesure();
        // double qu = (im.getEcart());
        // double w = BigDecimal.valueOf(qu).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue();
        // return new SimpleStringProperty(w + " " + m.getDescription());
        // });
        // col_retour_stk_preport.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter,
        // String> param) -> {
        // VenteReporter im = param.getValue();
        // Mesure m = im.getMesure();
        // double qu = (im.getRetour());
        // double w = BigDecimal.valueOf(qu).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue();
        // return new SimpleStringProperty(w + " " + m.getDescription());
        // });
        // col_ca_preport.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter,
        // String> param) -> {
        // VenteReporter im = param.getValue();
        // double qu = im.getCoutAchat();
        // double w = BigDecimal.valueOf(qu).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue();
        // return new SimpleStringProperty(w + " $");
        // });
        //

        // per category
        col_category.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            return new SimpleStringProperty(im.category());
        });
        cat_chiffreAffaire.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, Number> param) -> {
            SaleReport im = param.getValue();
            return new SimpleDoubleProperty(im.vente());
        });
        cat_percent.setCellValueFactory((TableColumn.CellDataFeatures<SaleReport, String> param) -> {
            SaleReport im = param.getValue();
            double pr = im.percentMarge();
            return new SimpleStringProperty(
                    BigDecimal.valueOf(pr).setScale(1, RoundingMode.HALF_EVEN).doubleValue() + "%");
        });

        // per client
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
                                    : c.getTypeClient().equals("#3") ? bundle.getString("subscriber")
                                            : bundle.getString("consumer");
            return new SimpleStringProperty(typecli);
        });
        clt_chiffreAffaire.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, Number> param) -> {
            VenteReporter im = param.getValue();
            return new SimpleDoubleProperty(im.getChiffre());
        });
        clt_percent.setCellValueFactory((TableColumn.CellDataFeatures<VenteReporter, String> param) -> {
            VenteReporter im = param.getValue();
            double pr = Double.isNaN(im.getMarge() / im.getChiffre()) ? 0 : (im.getMarge() / im.getChiffre()) * 100;
            return new SimpleStringProperty(
                    BigDecimal.valueOf(pr).setScale(1, RoundingMode.HALF_EVEN).doubleValue() + "%");
        });
        // cbx_periode_rapport.setConverter(new StringConverter<Periode>() {
        // @Override
        // public String toString(Periode object) {
        // String dt = Constants.DATE_ONLY_FORMAT.format(object.getDateDebut()) + " au "
        // + Constants.DATE_ONLY_FORMAT.format(object.getDateFin());
        // String et = object.getMouvement();
        // return object == null ? null : (object.getComment() + " du " + dt + " - " +
        // et);
        // }
        //
        // @Override
        // public Periode fromString(String string) {
        // return cbx_periode_rapport.getItems()
        // .stream()
        // .filter(f -> (f.getComment() + " du " +
        // Constants.DATE_ONLY_FORMAT.format(f.getDateDebut()) + " au " +
        // Constants.DATE_ONLY_FORMAT.format(f.getDateFin())
        // + " - " + f.getMouvement())
        // .equalsIgnoreCase(string))
        // .findFirst().orElse(null);
        // }
        // });
        // cbx_periode_rapport.setItems(FXCollections.observableArrayList(loadPeriode()));
        cbx_periodicity.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        if (t1 == null) {
                            return;
                        }
                        lnchart_data_report.getData().clear();
                        RepportDelegate.metrify(lnchart_data_report, bundle.getString("xgraph.seri1_vente").trim(),
                                bundle.getString("xgraph.seri2_depens").trim(),
                                bundle.getString("xgraph.seri3_marg").trim(),
                                dpk_debut_report.getValue(), dpk_fin_report.getValue(), role, region, t1);

                    }
                });
        // recent vente
        col_facture_recent.setCellValueFactory((TableColumn.CellDataFeatures<RecentSale, String> param) -> {
            RecentSale rs = param.getValue();
            return new SimpleStringProperty(rs.facture());
        });
        col_produit_recent.setCellValueFactory((TableColumn.CellDataFeatures<RecentSale, String> param) -> {
            RecentSale rs = param.getValue();
            return new SimpleStringProperty(rs.produit());
        });
        col_quantite_recent.setCellValueFactory((TableColumn.CellDataFeatures<RecentSale, String> param) -> {
            RecentSale rs = param.getValue();
            return new SimpleStringProperty(rs.quantiteLine() + " " + rs.unite());
        });
        col_total_recent.setCellValueFactory((TableColumn.CellDataFeatures<RecentSale, String> param) -> {
            RecentSale rs = param.getValue();
            return new SimpleStringProperty(rs.totalLine() + " " + devise);
        });

        col_imo_libelle.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLibelle()));
        col_imo_cat.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCategorie()));
        col_imo_region.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRegion()));
        col_imo_date.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getDateAcquisition() == null ? "-"
                        : param.getValue().getDateAcquisition().toString()));
        col_imo_valeur.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().getValeurOrigineUsd())) + " USD"));
        col_imo_dotation.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().dotationMensuelleUsd())) + " USD"));
        col_imo_cumul.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().amortissementCumulUsd(LocalDate.now()))) + " USD"));
        col_imo_vnc.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().valeurNetteUsd(LocalDate.now()))) + " USD"));
    }

    public void setup(Entreprise entr, Kazisafe kazi) {
        this.kazisafe = kazi;
        this.entreprise = entr;
        taux = pref.getDouble("taux2change", 2000);
        ventePr = FXCollections.observableArrayList();
        ventePerCategory = FXCollections.observableArrayList();
        ventePerClient = FXCollections.observableArrayList();
        immobilisations = FXCollections.observableArrayList();
        lsoperations = FXCollections.observableArrayList();
        ltxt_result_reporterie = FXCollections.observableArrayList();
        lsventes = FXCollections.observableArrayList();
        cbx_duration_report
                .setItems(FXCollections.observableArrayList("Ponctuel", "Par jours", "Par mois", "Par année"));
        cbx_periodicity.setItems(FXCollections.observableArrayList("Mensuel", "Annuel"));
        cbx_periodicity.getSelectionModel().selectFirst();
        cbx_regions.setItems(regions);
        tbreport.setItems(ventePr);
        tb_cat_report.setItems(ventePerCategory);
        clt_tbreport.setItems(ventePerClient);
        tb_immobilisations.setItems(immobilisations);

        searchRelv2.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (!newValue.isEmpty()) {
                        ObservableList<SaleReport> rsult = FXCollections.observableArrayList();
                        for (SaleReport vpr : ventePr) {
                            String q = vpr.codebar() + " " + vpr.produit();
                            if (q.toUpperCase().contains(newValue.toUpperCase())) {
                                rsult.add(vpr);
                            }
                        }
                        tbreport.setItems(rsult);
                    } else {
                        tbreport.setItems(ventePr);
                    }
                });

        searchRelv.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (!newValue.isEmpty()) {
                        ObservableList<VenteReporter> rsult = FXCollections.observableArrayList();
                        for (VenteReporter vpr : ventePerClient) {
                            Client c = vpr.getClient();
                            String typecli = c.getTypeClient().equals("#0") ? bundle.getString("consumer")
                                    : c.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                                            : c.getTypeClient().equals("#2") ? bundle.getString("detailor")
                                                    : c.getTypeClient().equals("#3") ? bundle.getString("subscriber")
                                                            : bundle.getString("consumer");
                            String q = c.getNomClient() + " " + typecli + " " + c.getAdresse() + " " + c.getPhone();
                            if (q.toUpperCase().contains(newValue.toUpperCase())) {
                                rsult.add(vpr);
                            }
                        }
                        clt_tbreport.setItems(rsult);
                    } else {
                        clt_tbreport.setItems(ventePerClient);
                    }
                });
        searchRelv1.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (!newValue.isEmpty()) {
                        ObservableList<SaleReport> rsult = FXCollections.observableArrayList();
                        for (SaleReport vpr : ventePerCategory) {
                            String q = vpr.category();
                            if (q.toUpperCase().contains(newValue.toUpperCase())) {
                                rsult.add(vpr);
                            }
                        }
                        tb_cat_report.setItems(rsult);
                    } else {
                        tb_cat_report.setItems(ventePerCategory);
                    }
                });
        search_imo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                tb_immobilisations.setItems(immobilisations);
                return;
            }
            ObservableList<Immobilisation> filtered = FXCollections.observableArrayList();
            for (Immobilisation imo : immobilisations) {
                String query = (imo.getLibelle() + " " + imo.getCategorie() + " " + imo.getRegion()).toUpperCase();
                if (query.contains(newValue.toUpperCase())) {
                    filtered.add(imo);
                }
            }
            tb_immobilisations.setItems(filtered);
        });
        cbx_regions.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue == null) {
                        return;
                    }
                    region = newValue;
                    Executors.newSingleThreadExecutor()
                            .submit(() -> {
                                List<SaleReport> rps = rapporterParProduit(dpk_debut_report.getValue(),
                                        dpk_fin_report.getValue(), newValue);
                                Platform.runLater(() -> {
                                    tbreport.setItems(FXCollections.observableArrayList(rps));
                                });
                            });
                });
        dpk_debut_report.valueProperty().addListener(
                (ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
                    evaluate();
                    summarise();
                    loadFinancialStatements();
                });
        dpk_fin_report.valueProperty().addListener(
                (ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
                    evaluate();
                    summarise();
                    loadFinancialStatements();
                });
        dpk_debut_report.setValue(LocalDate.now().minusMonths(LocalDate.now().getMonthValue() - 1));
        dpk_fin_report.setValue(LocalDate.now().plusMonths((12 - LocalDate.now().getMonthValue())));
        cbx_duration_report.getSelectionModel().selectFirst();
        evaluate();
        ponctuel();
        summarise();
        loadImmobilisations();
        loadFinancialStatements();
        if (entr == null) {
            MainUI.notify(null, "Info",
                    "Certains elements seront visibles apres le chargement complet des info de l'entreprise", 3,
                    "warning");
            return;
        }
        kazisafe.getRegions().enqueue(new Callback<List<String>>() {
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
    private void genPerCategory(Event e) {
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
    private void genPerClient(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsrep = Util.exportXlsSalePerClient(ventePerClient, bundle);
                try {
                    Desktop.getDesktop().open(xlsrep);
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    double marg = 0;

    private List<SaleReport> rapporterParProduit(LocalDate debut, LocalDate fin, String region) {
        List<SaleReport> reports = RepportDelegate.findSaleReportPerProduct(debut, fin,
                region == null ? detectRegion(role) : region);
        return reports;
    }

    private boolean isPeriodInList(List<Periode> listp, Periode p) {
        for (Periode periode : listp) {
            if (periode.getComment().equals(p.getComment())
                    && periode.getDateDebut().equals(p.getDateDebut())
                    && periode.getDateFin().equals(p.getDateFin())) {
                return true;
            }
        }
        return false;
    }

    private List<Periode> loadPeriode() {
        List<Periode> result = new ArrayList<>();
        List<Periode> periodes = PeriodeDelegate.findPeriodes();
        for (Periode periode : periodes) {
            if (!isPeriodInList(result, periode)) {
                result.add(periode);
            }
        }
        return result;
    }

    private void evaluate() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    ventePr.clear();
                    ventePerCategory.clear();
                    ventePerClient.clear();

                    LocalDate date1 = dpk_debut_report.getValue();
                    LocalDate date2 = dpk_fin_report.getValue();
                    double venteLeo = RepportDelegate.chiffreDaffaire(date1, date2, detectRegion(role));
                    double chargeLeo = RepportDelegate.chargeVariable(date1, date2, detectRegion(role));
                    double sum = BigDecimal.valueOf(venteLeo).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    LocalDate[] dateCA = previousPeriodOf(date1, date2);
                    double venteJana = RepportDelegate.chiffreDaffaire(dateCA[0], dateCA[1], detectRegion(role));
                    double progres = ((venteLeo - venteJana) / venteJana) * 100;

                    double chargeJana = RepportDelegate.chargeVariable(dateCA[0], dateCA[1], detectRegion(role));
                    double progresCV = ((chargeLeo - chargeJana) / chargeJana) * 100;

                    double resultLeo = (venteLeo - chargeLeo);
                    double resultJana = (venteJana - chargeJana);
                    double progresLeo = ((resultLeo - resultJana) / resultJana) * 100;

                    List<SaleReport> reports = RepportDelegate.findSaleReportPerProduct(date1, date2,
                            detectRegion(role));
                    List<SaleReport> repcats = RepportDelegate.findSaleReportPerCategory(date1, date2,
                            detectRegion(role));

                    Platform.runLater(() -> {
                        comment(lbl_comment_CA, img_indic_CA, progres);
                        comment(lbl_comment_MARGE, img_indic_MARGE, progresLeo);
                        comment(lbl_comment_CV, img_indic_CV, progresCV);
                        ventePr.setAll(reports);
                        ventePerCategory.setAll(repcats);
                        totalSalePerPro.setText(" Vente :" + Util.toPlain(sum) + " " + devise + ","
                                + " Cout :" + Util.toPlain(BigDecimal.valueOf(chargeLeo)
                                        .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                                + " "
                                + "" + devise + ", Marge :" + Util.toPlain(BigDecimal.valueOf(resultLeo)
                                        .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                                + " " + devise);
                        totalSaleperCat.setText(" Vente :" + Util.toPlain(sum) + " " + devise + ","
                                + " Cout :" + Util.toPlain(BigDecimal.valueOf(chargeLeo)
                                        .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                                + " "
                                + "" + devise + ", Marge :" + Util.toPlain(BigDecimal.valueOf(resultLeo)
                                        .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                                + " " + devise);
                        totalSalePerPro.setText(" Vente :" + Util.toPlain(sum) + " " + devise + ","
                                + " Cout :" + Util.toPlain(BigDecimal.valueOf(chargeLeo)
                                        .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                                + " "
                                + "" + devise + ", Marge :" + Util.toPlain(BigDecimal.valueOf(resultLeo)
                                        .setScale(2, RoundingMode.HALF_EVEN).doubleValue())
                                + " " + devise);
                    });
                });
    }

    private void comment(Label lbl, ImageView img, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            img.setImage(new Image(getClass().getResource("/icons/history32.png").toExternalForm()));
            lbl.setText("Aucune donnee aujourd'hui");
            return;
        }
        double val = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        if (value > 0) {
            img.setImage(new Image(getClass().getResource("/icons/arrow.png").toExternalForm()));
            lbl.setText("(" + val + "%) vs periode precedente");
        } else if (value < 0) {
            img.setImage(new Image(getClass().getResource("/icons/chart-down.png").toExternalForm()));
            lbl.setText("(" + val + "%) vs periode precedente");
        } else {
            img.setImage(new Image(getClass().getResource("/icons/equal.png").toExternalForm()));
            lbl.setText("(" + val + "%) stagnation");
        }
    }

    private LocalDate[] previousPeriodOf(LocalDate date1, LocalDate date2) {
        long days = ChronoUnit.DAYS.between(date1, date2) + 1;
        LocalDate yesterday = date1.minusDays(1);
        LocalDate begin = yesterday.minusDays(days);
        return new LocalDate[] { begin, yesterday };
    }

    public void summarise() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    System.out.println("is summary called");
                    dashCardVente();
                    dashCardDepense();
                    dashCardResult();
                    creanceToday();
                });
        loadSaleChart();
    }

    private String detectRegion(String role) {
        return role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name()) ? "%" : region;
    }

    private void ponctuel() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    List<RecentSale> recents = RepportDelegate.findRecentSales(detectRegion(role));
                    Platform.runLater(() -> {
                        recentSales.setItems(FXCollections.observableArrayList(recents));
                    });
                });
    }

    @FXML
    public void showCurrentInTable(ActionEvent evt) {
        if (dpk_debut_report.getValue() != null && dpk_fin_report.getValue() != null) {
            if (sales_produx.isSelected()) {

            } else if (overview.isSelected()) {

            }
            evaluate();
            summarise();
            loadFinancialStatements();
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
                // File xlsrep = Util.exportXlsReports(cis, cbx_agreggation_report.getValue(),
                // cbx_duration_report.getValue());
                // try {
                // Desktop.getDesktop().open(xlsrep);
                // } catch (IOException ex) {
                // Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null,
                // ex);
                // }
            }
        }).start();
    }

    @FXML
    public void exportSalePerProducReport(Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                choosenPeriod = new Periode();
                choosenPeriod.setComment(entreprise.getNomEntreprise());
                choosenPeriod.setRegion(detectRegion(role).equals("%") ? "Toute succursale" : detectRegion(role));
                choosenPeriod.setDateDebut(dpk_debut_report.getValue());
                choosenPeriod.setDateFin(dpk_fin_report.getValue());
                File xlsrep = Util.exportXlsSalePerProductReports(tbreport.getItems(), choosenPeriod);
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
                // File pdfrep;
                // if (role.equals(Role.Trader.name())) {
                // pdfrep =
                // Util.exportPDFicheDebiteurs(Util.getDebts(store.findAll(Vente.class),
                // store.findAll(Traisorerie.class), taux));
                // } else {
                // pdfrep =
                // Util.exportPDFicheDebiteurs(Util.getDebts(store.findAllByRegion(Vente.class,
                // region), store.findAllByRegion(Traisorerie.class, region), taux));
                // }
                // try {
                // Desktop.getDesktop().open(pdfrep);
                // } catch (IOException ex) {
                // MainUI.notify(null, bundle.getString("error"), "Erruer du soit a aucun
                // program associe au fomat PDF trouve sur votre ordianteur", 3, "error");
                // Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null,
                // ex);
                // }
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
        MainUI.setPattern(dpk_fin_report);
        MainUI.setPattern(dpk_debut_report);
        dpk_debut_report.setValue(LocalDate.now());
        dpk_fin_report.setValue(LocalDate.now().minusMonths(1));
        configTableVentePerProd();
        devise = pref.get("mainCur", "USD");
        region = pref.get("region", "...");
        role = pref.get("priv", null);
    }

    // public List<Vente> getVentes(LocalDate date, LocalDate date2, String region)
    // {
    // List<Vente> vts = store.findVenteCreditByLocalDateInterval(date, date2,
    // region);
    // return vts;
    // }
    public void dashCardVente() {
        LocalDate kesho = dpk_fin_report.getValue();
        LocalDate d1 = dpk_debut_report.getValue();
        double sumSales = RepportDelegate.chiffreDaffaire(d1, kesho, region == null ? "%" : region);
        Platform.runLater(() -> {
            txt_vente_report.setText(devise + " "
                    + formatNumber(BigDecimal.valueOf(sumSales).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        });
    }

    public void creanceToday() {
        // if (role.equals(Role.Trader.name())) {
        // List<Vente> ventes = getVentesDebt(LocalDate.now());
        // double sumSales = Util.sumCreditSales(ventes, taux);
        // txt_creance_report.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue());
        // } else {
        // List<Vente> ventes = getVentesDebt(new Date(), region);
        // double sumSales = Util.sumCreditSales(ventes, taux);
        // txt_creance_report.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2,
        // RoundingMode.HALF_EVEN).doubleValue());
        // }
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

    public void dashCardResult() {
        LocalDate kesho = dpk_fin_report.getValue();
        LocalDate d1 = dpk_debut_report.getValue();
        double ca = RepportDelegate.chiffreDaffaire(d1, kesho, region == null ? "%" : region);
        double cv = RepportDelegate.chargeVariable(d1, kesho, region == null ? "%" : region);
        double result = ca - cv;
        Platform.runLater(() -> {
            txt_result_report.setText(devise + " "
                    + formatNumber(BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        });
    }

    public void dashCardDepense() {
        LocalDate kesho = dpk_fin_report.getValue();
        LocalDate d1 = dpk_debut_report.getValue();
        double exp = RepportDelegate.chargeVariable(d1, kesho, region == null ? "%" : region);
        Platform.runLater(() -> {
            txt_depense_report.setText(devise + " "
                    + formatNumber(BigDecimal.valueOf(exp).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        });
    }

    public void focusOnImmobilisation() {
        if (tab_immobilisation != null) {
            tab_immobilisation.getTabPane().getSelectionModel().select(tab_immobilisation);
        }
        loadImmobilisations();
    }

    @FXML
    private void refreshImmobilisations() {
        loadImmobilisations();
        loadFinancialStatements();
    }

    private void loadImmobilisations() {
        if (immobilisations == null) {
            immobilisations = FXCollections.observableArrayList();
        }
        if (kazisafe == null) {
            loadImmobilisationsCache();
            return;
        }
        String usedRegion = role != null && role.contains(Role.ALL_ACCESS.name()) ? null : region;
        kazisafe.getImmobilisations(usedRegion).enqueue(new Callback<List<Immobilisation>>() {
            @Override
            public void onResponse(Call<List<Immobilisation>> call, Response<List<Immobilisation>> rspns) {
                if (rspns.isSuccessful() && rspns.body() != null) {
                    List<Immobilisation> data = rspns.body();
                    Platform.runLater(() -> {
                        immobilisations.setAll(data);
                        tb_immobilisations.setItems(immobilisations);
                        lbl_imo_status.setText(data.size() + " immobilisation(s)");
                    });
                    saveImmobilisationsCache(data);
                    return;
                }
                loadImmobilisationsCache();
            }

            @Override
            public void onFailure(Call<List<Immobilisation>> call, Throwable thrwbl) {
                loadImmobilisationsCache();
            }
        });
    }

    private void saveImmobilisationsCache(List<Immobilisation> items) {
        try {
            Path root = Path.of(MainUI.rootPath(), "cache");
            Files.createDirectories(root);
            Path file = root.resolve("immobilisations.json");
            byte[] data = KazisafeServiceFactory.mapper().writeValueAsBytes(items);
            Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException ex) {
            Logger.getLogger(RepportController.class.getName()).log(Level.FINE, ex.getMessage(), ex);
        }
    }

    private void loadImmobilisationsCache() {
        try {
            Path file = Path.of(MainUI.rootPath(), "cache", "immobilisations.json");
            if (!Files.exists(file)) {
                Platform.runLater(() -> lbl_imo_status.setText("Aucune donnee immobilisation hors ligne"));
                return;
            }
            List<Immobilisation> cached = KazisafeServiceFactory.mapper().readValue(Files.readAllBytes(file),
                    KazisafeServiceFactory.mapper().getTypeFactory().constructCollectionType(List.class,
                            Immobilisation.class));
            Platform.runLater(() -> {
                immobilisations.setAll(cached);
                tb_immobilisations.setItems(immobilisations);
                lbl_imo_status.setText("Mode hors ligne: " + cached.size() + " element(s)");
            });
        } catch (IOException ex) {
            Logger.getLogger(RepportController.class.getName()).log(Level.FINE, ex.getMessage(), ex);
            Platform.runLater(() -> lbl_imo_status.setText("Echec chargement hors ligne"));
        }
    }

    @FXML
    private void exportImmobilisations() {
        Util.exportXlsAmortissement(immobilisations);
    }

    @FXML
    private void exportExpiredStock(ActionEvent event) {
        LocalDate d1 = dpk_debut_report.getValue() == null ? LocalDate.now() : dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue() == null ? LocalDate.now() : dpk_fin_report.getValue();
        String usedRegion = detectRegion(role);
        List<utilities.Peremption> expiredItems = delegates.RecquisitionDelegate.showExpiredAtInterval(d1, d2,
                usedRegion);
        if (expiredItems == null || expiredItems.isEmpty()) {
            MainUI.notify(null, "Info", "Aucun produit expiré trouvé sur cette période", 3, "info");
            return;
        }
        Util.exportXlsExpiredStock(expiredItems);
    }

    @FXML
    private void exportFinancialStates(MouseEvent event) {
        if (currentBilan == null || currentCR == null) {
            MainUI.notify(null, "Ereur", "Veuillez d'abord charger les états financiers", 3, "error");
            return;
        }
        double chargesInd = RepportDelegate.industrialExpenseOf(dpk_debut_report.getValue(), dpk_fin_report.getValue(),
                detectRegion(role));
        Util.exportXlsFinancialStates(currentBilan, currentCR, chargesInd,
                entreprise != null ? entreprise.getNomEntreprise() : "Enterprise");
    }

    private double scale(Double value) {
        return BigDecimal.valueOf(value == null ? 0d : value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    @FXML
    private void refreshFinancialStates() {
        loadFinancialStatements();
    }

    @FXML
    private void printFinancialStates(MouseEvent event) {
        if (pane_financial_states == null) {
            return;
        }
        Printer printer = Printer.getDefaultPrinter();
        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job == null) {
            MainUI.notify(null, "Impression", "Aucune imprimante detectee", 3, "warning");
            return;
        }
        Stage stage = (Stage) pane_financial_states.getScene().getWindow();
        boolean proceed = job.showPrintDialog(stage);
        if (!proceed) {
            return;
        }
        boolean printed = job.printPage(pane_financial_states);
        if (printed) {
            job.endJob();
            MainUI.notify(null, "Impression", "Etats financiers envoyes a l'imprimante", 3, "info");
        } else {
            MainUI.notify(null, "Impression", "Echec de l'impression des etats financiers", 3, "error");
        }
    }

    private void loadFinancialStatements() {
        LocalDate d1 = dpk_debut_report.getValue() == null ? LocalDate.now().withDayOfMonth(1)
                : dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue() == null ? LocalDate.now() : dpk_fin_report.getValue();
        if (kazisafe != null) {
            String usedRegion = role != null && role.contains(Role.ALL_ACCESS.name()) ? null : region;
            kazisafe.getBilan(d1.toString(), d2.toString(), usedRegion).enqueue(new Callback<BilanReport>() {
                @Override
                public void onResponse(Call<BilanReport> call, Response<BilanReport> rspns) {
                    if (rspns.isSuccessful() && rspns.body() != null) {
                        Platform.runLater(() -> renderBilan(rspns.body()));
                    } else {
                        computeFinancialOffline(d1, d2);
                    }
                }

                @Override
                public void onFailure(Call<BilanReport> call, Throwable thrwbl) {
                    computeFinancialOffline(d1, d2);
                }
            });
            kazisafe.getCompteResultat(d1.toString(), d2.toString(), usedRegion)
                    .enqueue(new Callback<CompteResultatReport>() {
                        @Override
                        public void onResponse(Call<CompteResultatReport> call, Response<CompteResultatReport> rspns) {
                            if (rspns.isSuccessful() && rspns.body() != null) {
                                Platform.runLater(() -> renderCompteResultat(rspns.body(), d1, d2));
                            } else {
                                computeFinancialOffline(d1, d2);
                            }
                        }

                        @Override
                        public void onFailure(Call<CompteResultatReport> call, Throwable thrwbl) {
                            computeFinancialOffline(d1, d2);
                        }
                    });
            return;
        }
        computeFinancialOffline(d1, d2);
    }

    private void computeFinancialOffline(LocalDate d1, LocalDate d2) {
        String usedRegion = detectRegion(role);
        double stockValue = RepportDelegate.findStockValue(d1, d2, usedRegion,
                d1.equals(d2) ? "Journalier du " + d1 : "Intervale du " + d1 + " au " + d2);
        double ca = RepportDelegate.chiffreDaffaire(d1, d2, usedRegion);
        double coutVente = RepportDelegate.chargeVariable(d1, d2, usedRegion);
        double depensesOps = RepportDelegate.operationExpenseOf(d1, d2, usedRegion);
        double chargesInd = RepportDelegate.industrialExpenseOf(d1, d2, usedRegion);
        double amort = immobilisations == null ? 0d
                : immobilisations.stream().mapToDouble(i -> i.dotationMensuelleUsd() * i.moisEcoules(d2)).sum();
        double immobilise = immobilisations == null ? 0d
                : immobilisations.stream().mapToDouble(i -> i.valeurNetteUsd(d2)).sum();

        // Calcul des actifs (Assets)
        double receivables = delegates.ClientDelegate.getTotalDebt();
        double available = delegates.TraisorerieDelegate.soldeUsdOnPeriod(null, d1, d2, usedRegion);
        double actifCourant = stockValue + receivables + available;
        double totalActif = actifCourant + immobilise;

        // Calcul des passifs (Liabilities)
        double shortTermDebt = delegates.FournisseurDelegate.getTotalDebt();
        double longTermDebt = delegates.TraisorerieDelegate.getTotalBankDebt();
        double otherPassif = Math.max(0d, depensesOps - chargesInd) + chargesInd;

        double margeBrute = ca - coutVente;
        double resultExpl = margeBrute - depensesOps - amort;
        double impots = Math.max(0d, resultExpl * 0.3);
        double resultNet = resultExpl - impots;

        // Le capital est la contrepartie pour equilibrer (Assets - Other Liabilities)
        double capitaux = totalActif - shortTermDebt - longTermDebt - otherPassif;

        BilanReport bilan = new BilanReport();
        bilan.setActifNonCourant(immobilise);
        bilan.setActifCourant(actifCourant);
        bilan.setTotalActif(totalActif);
        bilan.setPassifCourant(shortTermDebt);
        bilan.setPassifNonCourant(longTermDebt + otherPassif);
        bilan.setCapitauxPropres(capitaux);
        bilan.setTotalPassif(totalActif); // Toujours equilibre

        CompteResultatReport cr = new CompteResultatReport();
        cr.setChiffreAffaires(ca);
        cr.setCoutDesVentes(coutVente);
        cr.setMargeBrute(margeBrute);
        cr.setDepensesOperationnelles(depensesOps);
        cr.setAmortissements(amort);
        cr.setVariationStock(stockValue);
        cr.setResultatExploitation(resultExpl);
        cr.setImpotsEstimes(impots);
        cr.setResultatNet(resultNet);

        Platform.runLater(() -> {
            renderBilan(bilan);
            renderCompteResultat(cr, d1, d2);
            lbl_cr_industriel.setText(formatMonnaie(chargesInd));
            renderCashFlow(cr.getChiffreAffaires(), cr.getCoutDesVentes(), cr.getDepensesOperationnelles(),
                    cr.getAmortissements(), chargesInd);
        });
    }

    private void renderBilan(BilanReport bilan) {
        this.currentBilan = bilan;
        double immobilise = bilan.getActifNonCourant();
        double circulant = Math.max(0d, bilan.getActifCourant());
        double dispo = Math.max(0d,
                circulant - RepportDelegate.findStockValue(dpk_debut_report.getValue(), dpk_fin_report.getValue(),
                        detectRegion(role),
                        dpk_debut_report.getValue().equals(dpk_fin_report.getValue())
                                ? "Journalier du " + dpk_debut_report.getValue()
                                : "Intervale du " + dpk_debut_report.getValue() + " au " + dpk_fin_report.getValue()));
        lbl_bilan_immobilise.setText(formatMonnaie(immobilise));
        lbl_bilan_circulant.setText(formatMonnaie(circulant));
        lbl_bilan_dispo.setText(formatMonnaie(dispo));
        lbl_bilan_total_actif.setText(formatMonnaie(bilan.getTotalActif()));

        lbl_bilan_capitaux.setText(formatMonnaie(bilan.getCapitauxPropres()));
        lbl_bilan_dettes.setText(formatMonnaie(bilan.getPassifCourant() + bilan.getPassifNonCourant()));
        lbl_bilan_total_passif.setText(formatMonnaie(bilan.getTotalPassif()));
    }

    private void renderCompteResultat(CompteResultatReport cr, LocalDate d1, LocalDate d2) {
        this.currentCR = cr;
        lbl_cr_ca.setText(formatMonnaie(cr.getChiffreAffaires()));
        lbl_cr_cout_vente.setText(formatMonnaie(cr.getCoutDesVentes()));
        lbl_cr_marge.setText(formatMonnaie(cr.getMargeBrute()));
        lbl_cr_dep_ops.setText(formatMonnaie(cr.getDepensesOperationnelles()));
        lbl_cr_amort.setText(formatMonnaie(cr.getAmortissements()));
        lbl_cr_var_stock.setText(formatMonnaie(cr.getVariationStock()));
        lbl_cr_result_expl.setText(formatMonnaie(cr.getResultatExploitation()));
        lbl_cr_impot.setText(formatMonnaie(cr.getImpotsEstimes()));
        lbl_cr_result_net.setText(formatMonnaie(cr.getResultatNet()));
        if (lbl_cr_industriel.getText() == null || lbl_cr_industriel.getText().isBlank()) {
            String usedRegion = detectRegion(role);
            lbl_cr_industriel.setText(formatMonnaie(RepportDelegate.industrialExpenseOf(d1, d2, usedRegion)));
        }
        double chargesInd = RepportDelegate.industrialExpenseOf(d1, d2, detectRegion(role));
        renderCashFlow(cr.getChiffreAffaires(), cr.getCoutDesVentes(), cr.getDepensesOperationnelles(),
                cr.getAmortissements(), chargesInd);
    }

    private String formatMonnaie(double amount) {
        return devise + " " + Util.toPlain(scale(amount));
    }

    private void renderCashFlow(double encaissements, double coutVentes, double depensesOps, double amortissements,
            double chargesIndustrielles) {
        double decaissements = Math.max(0d, coutVentes) + Math.max(0d, depensesOps) + Math.max(0d, amortissements)
                + Math.max(0d, chargesIndustrielles);
        double fluxNet = encaissements - decaissements;
        lbl_flux_encaiss.setText(formatMonnaie(encaissements));
        lbl_flux_decaiss.setText(formatMonnaie(decaissements));
        lbl_flux_net.setText(formatMonnaie(fluxNet));
    }

    private void loadSaleChart() {
        RepportDelegate.metrify(lnchart_data_report, bundle.getString("xgraph.seri1_vente").trim(),
                bundle.getString("xgraph.seri2_depens").trim(), bundle.getString("xgraph.seri3_marg").trim(),
                dpk_debut_report.getValue(), dpk_fin_report.getValue(), role, region, cbx_periodicity.getValue());
    }

    // public List<Operation> getOps(Date date) {
    // List<Operation> vts = store.findAllByLocalDateInterval(Operation.class,
    // dpk_debut_report.getValue(), dpk_fin_report.getValue());
    // return vts;
    // }
    //
    // public List<Operation> getOps(Date date, String region) {
    // List<Operation> vts =
    // store.findAllByLocalDateIntervalInRegion(Operation.class,
    // dpk_debut_report.getValue(), dpk_fin_report.getValue(), region);
    // return vts;
    // }
}
