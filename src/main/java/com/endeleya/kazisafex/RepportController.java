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
import java.util.Map;
import java.util.stream.Collectors;
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
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
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
import services.utils.RegionRegistry;
import services.utils.UserRoleRegistry;
import tools.ChartItem;
import tools.MainUI;
import tools.SyncEngine;
import tools.FinancialStatementPdfExporter;
import tools.FinancialStatementRow;
import tools.Util;
import tools.VenteReporter;
import data.helpers.Role;
import data.network.Kazisafe;
import data.core.KazisafeServiceFactory;
import delegates.PeriodeDelegate;
import delegates.RepportDelegate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import javafx.scene.image.Image;
import services.FinancialStatementAgregateService;
import services.FinancialStatementSyncService;
import tools.FinancialRowModel;
import tools.FinancialTableBinder;
import tools.DataCache;
import tools.SaleReport;
import tools.PurchaseBySupplier;
import tools.PurchaseByProduct;
import tools.PurchaseByMonth;
import tools.ExpenseByImputation;
import javafx.scene.control.RadioButton;

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
    private Label txt_amort_report;
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

    private final FinancialStatementAgregateService financialStatementService = new FinancialStatementAgregateService();
    private final FinancialStatementSyncService financialSyncService = new FinancialStatementSyncService();

    private static RepportController instance;
    Entreprise entreprise;

    /**
     * Rebind virtualized controls to the master ObservableLists after the
     * cached page is reattached. Does not reload from the database.
     */
    public void onCachedPageShown() {
        // No longer needed — views are rebuilt fresh each time.
    }
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

    // achats per fournisseur
    @FXML
    TableView<PurchaseBySupplier> tb_ach_fourn;
    @FXML
    TableColumn<PurchaseBySupplier, String> col_ach_fourn_nom;
    @FXML
    TableColumn<PurchaseBySupplier, String> col_ach_fourn_adresse;
    @FXML
    TableColumn<PurchaseBySupplier, String> col_ach_fourn_phone;
    @FXML
    TableColumn<PurchaseBySupplier, Number> col_ach_fourn_nb;
    @FXML
    TableColumn<PurchaseBySupplier, String> col_ach_fourn_montant;
    @FXML
    TextField searchAchFourn;
    @FXML
    Label totalAchFourn;

    // achats per produit
    @FXML
    TableView<PurchaseByProduct> tb_ach_prod;
    @FXML
    TableColumn<PurchaseByProduct, String> col_ach_prod_codebar;
    @FXML
    TableColumn<PurchaseByProduct, String> col_ach_prod_produit;
    @FXML
    TableColumn<PurchaseByProduct, String> col_ach_prod_quantite;
    @FXML
    TableColumn<PurchaseByProduct, String> col_ach_prod_unite;
    @FXML
    TableColumn<PurchaseByProduct, String> col_ach_prod_montant;
    @FXML
    TextField searchAchProd;
    @FXML
    Label totalAchProd;

    // achats per mois
    @FXML
    TableView<PurchaseByMonth> tb_ach_mois;
    @FXML
    TableColumn<PurchaseByMonth, String> col_ach_mois_periode;
    @FXML
    TableColumn<PurchaseByMonth, Number> col_ach_mois_nb;
    @FXML
    TableColumn<PurchaseByMonth, String> col_ach_mois_montant;
    @FXML
    TextField searchAchMois;
    @FXML
    Label totalAchMois;

    // depenses par imputation
    @FXML
    TableView<ExpenseByImputation> tb_depenses;
    @FXML
    TableColumn<ExpenseByImputation, String> col_dep_imputation;
    @FXML
    TableColumn<ExpenseByImputation, Number> col_dep_usd;
    @FXML
    TableColumn<ExpenseByImputation, Number> col_dep_cdf;
    @FXML
    TextField searchDep;
    @FXML
    Label totalDepenses;

    double taux;

    ObservableList<Vente> lsventes;
    ObservableList<Operation> lsoperations;
    ObservableList<Traisorerie> ltxt_result_reporterie;
    ObservableList<SaleReport> ventePr;
    ObservableList<SaleReport> ventePerCategory;
    ObservableList<VenteReporter> ventePerClient;
    ObservableList<PurchaseBySupplier> achatFournisseur;
    ObservableList<PurchaseByProduct> achatProduit;
    ObservableList<PurchaseByMonth> achatMois;
    ObservableList<ExpenseByImputation> depensesParImputation;
    List<List<ChartItem>> cis;

    private boolean achatSourceDepot = true;
    @FXML
    private RadioButton rbAchDepot;
    @FXML
    private RadioButton rbAchPdv;

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
    private ComboBox<String> cbx_financial_history_span;
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
    private AnchorPane pane_financial_states;
    @FXML
    private TabPane tab_financial_statements;
    @FXML
    private TableView<FinancialStatementRow> tb_fin_bilan;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_code;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_rubrique;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_nature;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_n;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_n1;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_n2;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_n3;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_bilan_n4;
    private final TableColumn<FinancialStatementRow, String> col_fin_bilan_gross = new TableColumn<>("Valeur brute immobilisation");
    private final TableColumn<FinancialStatementRow, String> col_fin_bilan_amortization = new TableColumn<>("Amortissement");
    private final TableColumn<FinancialStatementRow, String> col_fin_bilan_net = new TableColumn<>("Valeur nette immobilisation");
    @FXML
    private TableView<FinancialStatementRow> tb_fin_cr;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_code;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_rubrique;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_nature;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_n;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_n1;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_n2;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_n3;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_cr_n4;
    @FXML
    private TableView<FinancialStatementRow> tb_fin_flux;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_code;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_rubrique;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_nature;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_n;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_n1;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_n2;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_n3;
    @FXML
    private TableColumn<FinancialStatementRow, String> col_fin_flux_n4;
    @FXML
    private TableView<FinancialRowModel> tb_fin_pivot_bilan;
    @FXML
    private TableView<FinancialRowModel> tb_fin_pivot_cr;
    @FXML
    private TableView<FinancialRowModel> tb_fin_pivot_flux;

    private ObservableList<Immobilisation> immobilisations;
    private final ObservableList<FinancialStatementRow> bilanRows = FXCollections.observableArrayList();
    private final ObservableList<FinancialStatementRow> compteResultatRows = FXCollections.observableArrayList();
    private final ObservableList<FinancialStatementRow> fluxRows = FXCollections.observableArrayList();
    private static final int FINANCIAL_PERIOD_EXACT = 1;
    private int financialHistorySpan = FINANCIAL_PERIOD_EXACT;

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
            double pr = im.getSommeTotal() <= 0 ? 0 : (im.getChiffre() / im.getSommeTotal()) * 100;
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

    private void configTableAchats() {
        if (col_ach_fourn_nom == null || col_ach_prod_produit == null || col_ach_mois_periode == null) {
            return;
        }
        col_ach_fourn_nom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().nom()));
        col_ach_fourn_adresse.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().adresse()));
        col_ach_fourn_phone.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().phone()));
        col_ach_fourn_nb.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().nbLivraisons()));
        col_ach_fourn_montant.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().montant())) + " " + devise));

        col_ach_prod_codebar.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().codebar()));
        col_ach_prod_produit.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().produit()));
        col_ach_prod_quantite.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().quantite())) + " " + param.getValue().unite()));
        col_ach_prod_unite.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().unite()));
        col_ach_prod_montant.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().montant())) + " " + devise));

        col_ach_mois_periode.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().periode()));
        col_ach_mois_nb.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().nbLivraisons()));
        col_ach_mois_montant.setCellValueFactory(param -> new SimpleStringProperty(
                Util.toPlain(scale(param.getValue().montant())) + " " + devise));

        searchAchFourn.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue == null || newValue.isBlank()) {
                        tb_ach_fourn.setItems(achatFournisseur);
                        return;
                    }
                    ObservableList<PurchaseBySupplier> rsult = FXCollections.observableArrayList();
                    for (PurchaseBySupplier p : achatFournisseur) {
                        String q = p.nom() + " " + p.adresse() + " " + p.phone();
                        if (q.toUpperCase().contains(newValue.toUpperCase())) {
                            rsult.add(p);
                        }
                    }
                    tb_ach_fourn.setItems(rsult);
                });
        searchAchProd.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue == null || newValue.isBlank()) {
                        tb_ach_prod.setItems(achatProduit);
                        return;
                    }
                    ObservableList<PurchaseByProduct> rsult = FXCollections.observableArrayList();
                    for (PurchaseByProduct p : achatProduit) {
                        String q = p.codebar() + " " + p.produit() + " " + p.unite();
                        if (q.toUpperCase().contains(newValue.toUpperCase())) {
                            rsult.add(p);
                        }
                    }
                    tb_ach_prod.setItems(rsult);
                });
        searchAchMois.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue == null || newValue.isBlank()) {
                        tb_ach_mois.setItems(achatMois);
                        return;
                    }
                    ObservableList<PurchaseByMonth> rsult = FXCollections.observableArrayList();
                    for (PurchaseByMonth p : achatMois) {
                        String q = p.periode();
                        if (q.toUpperCase().contains(newValue.toUpperCase())) {
                            rsult.add(p);
                        }
                    }
                    tb_ach_mois.setItems(rsult);
                });

        if (rbAchDepot != null) {
            rbAchDepot.selectedProperty()
                    .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        achatSourceDepot = newValue != null && newValue;
                        loadPurchaseReports();
                    });
        }
    }

    private void configTableDepenses() {
        if (col_dep_imputation == null || col_dep_usd == null || col_dep_cdf == null) {
            return;
        }
        col_dep_imputation.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().imputation()));
        col_dep_usd.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().montantUsd()));
        col_dep_cdf.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().montantCdf()));

        if (searchDep != null) {
            searchDep.textProperty()
                    .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        if (newValue == null || newValue.isBlank()) {
                            tb_depenses.setItems(depensesParImputation);
                            return;
                        }
                        ObservableList<ExpenseByImputation> rsult = FXCollections.observableArrayList();
                        for (ExpenseByImputation d : depensesParImputation) {
                            String q = d.imputation();
                            if (q.toUpperCase().contains(newValue.toUpperCase())) {
                                rsult.add(d);
                            }
                        }
                        tb_depenses.setItems(rsult);
                    });
        }
    }

    private void loadExpenseReports() {
        loadExpenseReports(detectRegion(role));
    }

    private void loadExpenseReports(String region) {
        LocalDate d1 = dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue();
        if (d1 == null || d2 == null) {
            return;
        }
        String usedRegion = (region == null || region.isBlank()) ? "%" : region;
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    List<ExpenseByImputation> items = DataCache.getOrLoad(
                            "report-expense-imputation-" + d1 + "-" + d2 + "-" + usedRegion,
                            () -> RepportDelegate.findExpenseReportByImputation(d1, d2, usedRegion));
                    Platform.runLater(() -> {
                        if (depensesParImputation != null) {
                            depensesParImputation.setAll(items == null ? List.of() : items);
                        }
                        updateExpenseTotals();
                    });
                });
    }

    private void updateExpenseTotals() {
        if (totalDepenses == null) {
            return;
        }
        double totalUsd = 0;
        double totalCdf = 0;
        for (ExpenseByImputation d : depensesParImputation) {
            totalUsd += d.montantUsd();
            totalCdf += d.montantCdf();
        }
        totalDepenses.setText("Depenses total : " + Util.toPlain(scale(totalUsd)) + " USD, "
                + Util.toPlain(scale(totalCdf)) + " CDF");
    }

    private String expenseReportTitle(String libelle) {
        String r = detectRegion(role).equals("%") ? "Toute succursale" : detectRegion(role);
        LocalDate d1 = dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue();
        return libelle + " - " + r + " du " + d1 + " au " + d2;
    }

    @FXML
    public void exportExpenses(Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ExpenseByImputation> items = new ArrayList<>(tb_depenses.getItems());
                    if (items.isEmpty()) {
                        Platform.runLater(() -> MainUI.notify(null, "Export", "Aucune donnée à exporter", 3, "error"));
                        return;
                    }
                    File xlsrep = Util.exportXlsExpenseByImputation(items,
                            expenseReportTitle("Rapport des depenses par imputation"));
                    if (xlsrep != null) {
                        Desktop.getDesktop().open(xlsrep);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void loadPurchaseReports() {
        loadPurchaseReports(detectRegion(role));
    }

    private void loadPurchaseReports(String region) {
        LocalDate d1 = dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue();
        if (d1 == null || d2 == null) {
            return;
        }
        String usedRegion = (region == null || region.isBlank()) ? "%" : region;
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    List<PurchaseBySupplier> fourn = DataCache.getOrLoad(
                            "report-purchase-fourn-" + d1 + "-" + d2 + "-" + usedRegion,
                            () -> RepportDelegate.findPurchasesBySupplier(d1, d2, usedRegion));
                    List<PurchaseByProduct> prods = DataCache.getOrLoad(
                            "report-purchase-prod-" + (achatSourceDepot ? "depot" : "pdv") + "-" + d1 + "-" + d2 + "-" + usedRegion,
                            () -> achatSourceDepot
                                    ? RepportDelegate.findPurchasesByProduct(d1, d2, usedRegion)
                                    : RepportDelegate.findRequisitionPurchasesByProduct(d1, d2, usedRegion));
                    List<PurchaseByMonth> mois = DataCache.getOrLoad(
                            "report-purchase-mois-" + d1 + "-" + d2 + "-" + usedRegion,
                            () -> RepportDelegate.findPurchasesByMonth(d1, d2, usedRegion));
                    Platform.runLater(() -> {
                        if (achatFournisseur != null) {
                            achatFournisseur.setAll(fourn == null ? List.of() : fourn);
                        }
                        if (achatProduit != null) {
                            achatProduit.setAll(prods == null ? List.of() : prods);
                        }
                        if (achatMois != null) {
                            achatMois.setAll(mois == null ? List.of() : mois);
                        }
                        updatePurchaseTotals();
                    });
                });
    }

    private void updatePurchaseTotals() {
        double totalFourn = 0;
        for (PurchaseBySupplier p : achatFournisseur) {
            totalFourn += p.montant();
        }
        if (totalAchFourn != null) {
            totalAchFourn.setText("Achats total : " + Util.toPlain(scale(totalFourn)) + " " + devise);
        }
        double totalProd = 0;
        for (PurchaseByProduct p : achatProduit) {
            totalProd += p.montant();
        }
        if (totalAchProd != null) {
            totalAchProd.setText("Achats total : " + Util.toPlain(scale(totalProd)) + " " + devise);
        }
        double totalMois = 0;
        for (PurchaseByMonth p : achatMois) {
            totalMois += p.montant();
        }
        if (totalAchMois != null) {
            totalAchMois.setText("Achats total : " + Util.toPlain(scale(totalMois)) + " " + devise);
        }
    }

    private String purchaseReportTitle(String libelle) {
        String r = detectRegion(role).equals("%") ? "Toute succursale" : detectRegion(role);
        LocalDate d1 = dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue();
        return libelle + " - " + r + " du " + d1 + " au " + d2;
    }

    @FXML
    public void exportAchFourn(Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PurchaseBySupplier> items = new ArrayList<>(tb_ach_fourn.getItems());
                    if (items.isEmpty()) {
                        Platform.runLater(() -> MainUI.notify(null, "Export", "Aucune donnée à exporter", 3, "error"));
                        return;
                    }
                    File xlsrep = Util.exportXlsPurchasesBySupplier(items,
                            purchaseReportTitle("Rapport des achats par fournisseur"));
                    if (xlsrep != null) {
                        Desktop.getDesktop().open(xlsrep);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @FXML
    public void exportAchProd(Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PurchaseByProduct> items = new ArrayList<>(tb_ach_prod.getItems());
                    if (items.isEmpty()) {
                        Platform.runLater(() -> MainUI.notify(null, "Export", "Aucune donnée à exporter", 3, "error"));
                        return;
                    }
                    File xlsrep = Util.exportXlsPurchasesByProduct(items,
                            purchaseReportTitle("Rapport des achats par produit (" + (achatSourceDepot ? "Dépôt" : "Point de vente") + ")"));
                    if (xlsrep != null) {
                        Desktop.getDesktop().open(xlsrep);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @FXML
    public void exportAchMois(Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PurchaseByMonth> items = new ArrayList<>(tb_ach_mois.getItems());
                    if (items.isEmpty()) {
                        Platform.runLater(() -> MainUI.notify(null, "Export", "Aucune donnée à exporter", 3, "error"));
                        return;
                    }
                    File xlsrep = Util.exportXlsPurchasesByMonth(items,
                            purchaseReportTitle("Rapport des achats par mois"));
                    if (xlsrep != null) {
                        Desktop.getDesktop().open(xlsrep);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public void setup(Entreprise entr, Kazisafe kazi) {
        this.kazisafe = kazi;
        this.entreprise = entr;
        taux = pref.getDouble("taux2change", 2000);
        ventePr = FXCollections.observableArrayList();
        ventePerCategory = FXCollections.observableArrayList();
        ventePerClient = FXCollections.observableArrayList();
        achatFournisseur = FXCollections.observableArrayList();
        achatProduit = FXCollections.observableArrayList();
        achatMois = FXCollections.observableArrayList();
        depensesParImputation = FXCollections.observableArrayList();
        immobilisations = FXCollections.observableArrayList();
        lsoperations = FXCollections.observableArrayList();
        ltxt_result_reporterie = FXCollections.observableArrayList();
        lsventes = FXCollections.observableArrayList();
        cbx_duration_report
                .setItems(FXCollections.observableArrayList("Ponctuel", "Par jours", "Par mois", "Par trimestre", "Par année"));
        cbx_periodicity.setItems(FXCollections.observableArrayList("Mensuel", "Trimestriel", "Annuel"));
        cbx_periodicity.getSelectionModel().selectFirst();
        cbx_regions.setItems(regions);
        tbreport.setItems(ventePr);
        tb_cat_report.setItems(ventePerCategory);
        clt_tbreport.setItems(ventePerClient);
        tb_immobilisations.setItems(immobilisations);
        if (tb_ach_fourn != null) {
            tb_ach_fourn.setItems(achatFournisseur);
        }
        if (tb_ach_prod != null) {
            tb_ach_prod.setItems(achatProduit);
        }
        if (tb_ach_mois != null) {
            tb_ach_mois.setItems(achatMois);
        }
        if (tb_depenses != null) {
            tb_depenses.setItems(depensesParImputation);
        }
        configTableAchats();
        configTableDepenses();

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
                    LocalDate rDebut = dpk_debut_report.getValue();
                    LocalDate rFin = dpk_fin_report.getValue();
                    loadFinancialStatements();
                    Executors.newSingleThreadExecutor()
                            .submit(() -> {
                                List<SaleReport> rps = rapporterParProduit(rDebut, rFin, newValue);
                                List<SaleReport> rcs = DataCache.getOrLoad(
                                        "report-sale-per-category-" + rDebut + "-" + rFin + "-" + newValue,
                                        () -> RepportDelegate.findSaleReportPerCategory(rDebut, rFin, newValue));
                                List<VenteReporter> rcsCli = rapporterParClient(rDebut, rFin, newValue);
                                List<RecentSale> recents = DataCache.getOrLoad(
                                        "report-recent-sales-" + newValue,
                                        () -> RepportDelegate.findRecentSales(newValue));
                                Platform.runLater(() -> {
                                    ventePr.setAll(rps);
                                    ventePerCategory.setAll(rcs);
                                    if (rcsCli != null) {
                                        ventePerClient.setAll(rcsCli);
                                    } else {
                                        ventePerClient.clear();
                                    }
                                     updateTotalSaleperCli();
                                    recentSales.setItems(FXCollections.observableArrayList(recents));
                                });
                            });
                    loadPurchaseReports(newValue);
                    loadExpenseReports(newValue);
                    summarise();
                });
        dpk_debut_report.valueProperty().addListener(
                (ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
                    evaluate();
                    summarise();
                    refreshFinancialColumnHeaders();
                    loadFinancialStatements();
                    loadPurchaseReports();
                    loadExpenseReports();
                });
        dpk_fin_report.valueProperty().addListener(
                (ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
                    evaluate();
                    summarise();
                    refreshFinancialColumnHeaders();
                    loadFinancialStatements();
                    loadPurchaseReports();
                    loadExpenseReports();
                });
        dpk_debut_report.setValue(LocalDate.now().withDayOfYear(1));
        dpk_fin_report.setValue(LocalDate.of(LocalDate.now().getYear(), 12, 31));
        cbx_duration_report.getSelectionModel().selectFirst();
        evaluate();
        ponctuel();
        summarise();
        loadPurchaseReports();
        loadExpenseReports();
        loadImmobilisations();
        loadFinancialStatements();
        if (entr == null) {
            MainUI.notify(null, "Info",
                    "Certains elements seront visibles apres le chargement complet des info de l'entreprise", 3,
                    "warning");
            return;
        }
        RegionRegistry.loadAndSync(pref, kazisafe, regions);
        RegionRegistry.selectSavedRegion(pref, cbx_regions);

    }

    private void configureFinancialTables() {
        if (tb_fin_bilan != null) {
            configureFinancialTable(tb_fin_bilan, col_fin_bilan_code, col_fin_bilan_rubrique, col_fin_bilan_nature,
                    col_fin_bilan_n, col_fin_bilan_n1, col_fin_bilan_n2, col_fin_bilan_n3, col_fin_bilan_n4);
            configureBilanImmobilisationColumns();
            tb_fin_bilan.setItems(bilanRows);
        }
        if (tb_fin_cr != null) {
            configureFinancialTable(tb_fin_cr, col_fin_cr_code, col_fin_cr_rubrique, col_fin_cr_nature, col_fin_cr_n,
                    col_fin_cr_n1, col_fin_cr_n2, col_fin_cr_n3, col_fin_cr_n4);
            tb_fin_cr.setItems(compteResultatRows);
        }
        if (tb_fin_flux != null) {
            configureFinancialTable(tb_fin_flux, col_fin_flux_code, col_fin_flux_rubrique, col_fin_flux_nature,
                    col_fin_flux_n, col_fin_flux_n1, col_fin_flux_n2, col_fin_flux_n3, col_fin_flux_n4);
            tb_fin_flux.setItems(fluxRows);
        }
        if (tb_fin_pivot_bilan != null) {
            FinancialTableBinder.bind(tb_fin_pivot_bilan, List.of(), List.of());
        }
        if (tb_fin_pivot_cr != null) {
            FinancialTableBinder.bind(tb_fin_pivot_cr, List.of(), List.of());
        }
        if (tb_fin_pivot_flux != null) {
            FinancialTableBinder.bind(tb_fin_pivot_flux, List.of(), List.of());
        }
        configureFinancialHistorySelector();
    }

    private void configureBilanImmobilisationColumns() {
        if (tb_fin_bilan == null) {
            return;
        }
        col_fin_bilan_gross.setPrefWidth(120);
        col_fin_bilan_amortization.setPrefWidth(120);
        col_fin_bilan_net.setPrefWidth(120);
        col_fin_bilan_gross.setCellValueFactory(param ->
                new SimpleStringProperty(formatFinancialAmount(param.getValue().getGrossAmount())));
        col_fin_bilan_amortization.setCellValueFactory(param ->
                new SimpleStringProperty(formatFinancialAmount(param.getValue().getAmortizationAmount())));
        col_fin_bilan_net.setCellValueFactory(param ->
                new SimpleStringProperty(formatFinancialAmount(param.getValue().getNetAmount())));
        if (!tb_fin_bilan.getColumns().contains(col_fin_bilan_gross)) {
            tb_fin_bilan.getColumns().addAll(col_fin_bilan_gross, col_fin_bilan_amortization, col_fin_bilan_net);
        }
    }

    private void configureFinancialTable(TableView<FinancialStatementRow> table,
            TableColumn<FinancialStatementRow, String> codeCol,
            TableColumn<FinancialStatementRow, String> rubriqueCol,
            TableColumn<FinancialStatementRow, String> natureCol,
            TableColumn<FinancialStatementRow, String> nCol,
            TableColumn<FinancialStatementRow, String> n1Col,
            TableColumn<FinancialStatementRow, String> n2Col,
            TableColumn<FinancialStatementRow, String> n3Col,
            TableColumn<FinancialStatementRow, String> n4Col) {
        codeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCode()));
        rubriqueCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRubrique()));
        natureCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getNature()));
        nCol.setCellValueFactory(param -> new SimpleStringProperty(formatFinancialAmount(param.getValue().getAmountN())));
        n1Col.setCellValueFactory(param -> new SimpleStringProperty(formatFinancialAmount(param.getValue().getAmountN1())));
        n2Col.setCellValueFactory(param -> new SimpleStringProperty(formatFinancialAmount(param.getValue().getAmountN2())));
        n3Col.setCellValueFactory(param -> new SimpleStringProperty(formatFinancialAmount(param.getValue().getAmountN3())));
        n4Col.setCellValueFactory(param -> new SimpleStringProperty(formatFinancialAmount(param.getValue().getAmountN4())));
        
        table.getColumns().setAll(codeCol, rubriqueCol, natureCol, nCol, n1Col, n2Col, n3Col, n4Col);
        
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(FinancialStatementRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }
                if (item.isTotalLine()) {
                    setStyle("-fx-font-weight: bold; -fx-background-color: #d2e8f0;");
                } else if (item.isSectionHeader()) {
                    setStyle("-fx-font-weight: bold; -fx-background-color: #dcf4fc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configureFinancialHistorySelector() {
        if (cbx_financial_history_span == null) {
            return;
        }
        cbx_financial_history_span.setItems(FXCollections.observableArrayList("Période choisie", "4 trimestres", "3 ans", "5 ans"));
        cbx_financial_history_span.getSelectionModel().select("Période choisie");
        cbx_financial_history_span.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            financialHistorySpan = newValue.startsWith("P") ? FINANCIAL_PERIOD_EXACT
                    : (newValue.startsWith("4") ? 4 : (newValue.startsWith("3") ? 3 : 5));
            refreshFinancialColumnHeaders();
            loadFinancialStatements();
        });
        refreshFinancialColumnHeaders();
    }

    private void refreshFinancialColumnHeaders() {
        if (col_fin_bilan_n == null || col_fin_cr_n == null || col_fin_flux_n == null) {
            return;
        }
        boolean exactPeriod = financialHistorySpan == FINANCIAL_PERIOD_EXACT;
        if (exactPeriod) {
            col_fin_bilan_n.setText("Période");
            col_fin_bilan_n1.setText("Période précédente 1");
            col_fin_bilan_n2.setText("Période précédente 2");
            col_fin_bilan_n3.setText("Période précédente 3");
            col_fin_bilan_n4.setText("");
            col_fin_cr_n.setText("Période");
            col_fin_cr_n1.setText("Période précédente 1");
            col_fin_cr_n2.setText("Période précédente 2");
            col_fin_cr_n3.setText("Période précédente 3");
            col_fin_cr_n4.setText("");
            col_fin_flux_n.setText("Période");
            col_fin_flux_n1.setText("Période précédente 1");
            col_fin_flux_n2.setText("Période précédente 2");
            col_fin_flux_n3.setText("Période précédente 3");
            col_fin_flux_n4.setText("");
            col_fin_bilan_n3.setVisible(true);
            col_fin_bilan_n4.setVisible(false);
            col_fin_cr_n3.setVisible(true);
            col_fin_cr_n4.setVisible(false);
            col_fin_flux_n3.setVisible(true);
            col_fin_flux_n4.setVisible(false);
            return;
        }
        LocalDate endDate = dpk_fin_report != null && dpk_fin_report.getValue() != null ? dpk_fin_report.getValue() : LocalDate.now();
        if (financialHistorySpan == 4) {
            applyQuarterHeaders(col_fin_bilan_n, col_fin_bilan_n1, col_fin_bilan_n2, col_fin_bilan_n3, col_fin_bilan_n4, endDate);
            applyQuarterHeaders(col_fin_cr_n, col_fin_cr_n1, col_fin_cr_n2, col_fin_cr_n3, col_fin_cr_n4, endDate);
            applyQuarterHeaders(col_fin_flux_n, col_fin_flux_n1, col_fin_flux_n2, col_fin_flux_n3, col_fin_flux_n4, endDate);
        } else {
            int endYear = endDate.getYear();
            applyYearHeaders(col_fin_bilan_n, col_fin_bilan_n1, col_fin_bilan_n2, col_fin_bilan_n3, col_fin_bilan_n4, endYear);
            applyYearHeaders(col_fin_cr_n, col_fin_cr_n1, col_fin_cr_n2, col_fin_cr_n3, col_fin_cr_n4, endYear);
            applyYearHeaders(col_fin_flux_n, col_fin_flux_n1, col_fin_flux_n2, col_fin_flux_n3, col_fin_flux_n4, endYear);
        }
        boolean showFiveYears = financialHistorySpan == 5;
        boolean showFourQuarters = financialHistorySpan == 4;
        col_fin_bilan_n3.setVisible(showFiveYears || showFourQuarters);
        col_fin_bilan_n4.setVisible(showFiveYears);
        col_fin_cr_n3.setVisible(showFiveYears || showFourQuarters);
        col_fin_cr_n4.setVisible(showFiveYears);
        col_fin_flux_n3.setVisible(showFiveYears || showFourQuarters);
        col_fin_flux_n4.setVisible(showFiveYears);
    }

    private void applyYearHeaders(TableColumn<FinancialStatementRow, String> nCol,
            TableColumn<FinancialStatementRow, String> n1Col,
            TableColumn<FinancialStatementRow, String> n2Col,
            TableColumn<FinancialStatementRow, String> n3Col,
            TableColumn<FinancialStatementRow, String> n4Col,
            int endYear) {
        nCol.setText(String.valueOf(endYear));
        n1Col.setText(String.valueOf(endYear - 1));
        n2Col.setText(String.valueOf(endYear - 2));
        n3Col.setText(String.valueOf(endYear - 3));
        n4Col.setText(String.valueOf(endYear - 4));
    }

    private void applyQuarterHeaders(TableColumn<FinancialStatementRow, String> nCol,
            TableColumn<FinancialStatementRow, String> n1Col,
            TableColumn<FinancialStatementRow, String> n2Col,
            TableColumn<FinancialStatementRow, String> n3Col,
            TableColumn<FinancialStatementRow, String> n4Col,
            LocalDate endDate) {
        int year = endDate.getYear();
        nCol.setText("T1 " + year);
        n1Col.setText("T2 " + year);
        n2Col.setText("T3 " + year);
        n3Col.setText("T4 " + year);
        n4Col.setText("");
    }

    private String formatFinancialAmount(Double amount) {
        if (amount == null) {
            return "0";
        }
        return Util.toPlain(scale(amount));
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
        String usedRegion = region == null ? detectRegion(role) : region;
        return DataCache.getOrLoad(
                "report-sale-per-product-" + debut + "-" + fin + "-" + usedRegion,
                () -> RepportDelegate.findSaleReportPerProduct(debut, fin, usedRegion));
    }

    private List<VenteReporter> rapporterParClient(LocalDate debut, LocalDate fin, String region) {
        String usedRegion = region == null ? detectRegion(role) : region;
        return DataCache.getOrLoad(
                "report-sale-per-client-" + debut + "-" + fin + "-" + usedRegion,
                () -> RepportDelegate.findReportSaleByClient(debut, fin, usedRegion, devise));
    }

    private void updateTotalSaleperCli() {
        double total = 0;
        for (VenteReporter vpr : ventePerClient) {
            total += vpr.getChiffre();
        }
        totalSaleperCli.setText(" Vente :" + Util.toPlain(BigDecimal.valueOf(total)
                .setScale(2, RoundingMode.HALF_EVEN).doubleValue()) + " " + devise);
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
                    String regionKey = detectRegion(role);
                    double venteLeo = DataCache.getOrLoad(
                            "report-ca-" + date1 + "-" + date2 + "-" + regionKey,
                            () -> RepportDelegate.chiffreDaffaire(date1, date2, regionKey));
                    double chargeLeo = DataCache.getOrLoad(
                            "report-cv-" + date1 + "-" + date2 + "-" + regionKey,
                            () -> RepportDelegate.chargeVariable(date1, date2, regionKey));
                    double sum = BigDecimal.valueOf(venteLeo).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    LocalDate[] dateCA = previousPeriodOf(date1, date2);
                    double venteJana = DataCache.getOrLoad(
                            "report-ca-" + dateCA[0] + "-" + dateCA[1] + "-" + regionKey,
                            () -> RepportDelegate.chiffreDaffaire(dateCA[0], dateCA[1], regionKey));
                    double progres = ((venteLeo - venteJana) / venteJana) * 100;

                    double chargeJana = DataCache.getOrLoad(
                            "report-cv-" + dateCA[0] + "-" + dateCA[1] + "-" + regionKey,
                            () -> RepportDelegate.chargeVariable(dateCA[0], dateCA[1], regionKey));
                    double progresCV = ((chargeLeo - chargeJana) / chargeJana) * 100;

                    double resultLeo = (venteLeo - chargeLeo);
                    double resultJana = (venteJana - chargeJana);
                    double progresLeo = ((resultLeo - resultJana) / resultJana) * 100;

                    List<SaleReport> reports = DataCache.getOrLoad(
                            "report-sale-per-product-" + date1 + "-" + date2 + "-" + regionKey,
                            () -> RepportDelegate.findSaleReportPerProduct(date1, date2, regionKey));
                    List<SaleReport> repcats = DataCache.getOrLoad(
                            "report-sale-per-category-" + date1 + "-" + date2 + "-" + regionKey,
                            () -> RepportDelegate.findSaleReportPerCategory(date1, date2, regionKey));
                    List<VenteReporter> repclis = DataCache.getOrLoad(
                            "report-sale-per-client-" + date1 + "-" + date2 + "-" + regionKey,
                            () -> RepportDelegate.findReportSaleByClient(date1, date2, regionKey, devise));

                    Platform.runLater(() -> {
                        comment(lbl_comment_CA, img_indic_CA, progres);
                        comment(lbl_comment_MARGE, img_indic_MARGE, progresLeo);
                        comment(lbl_comment_CV, img_indic_CV, progresCV);
                        ventePr.setAll(reports);
                        ventePerCategory.setAll(repcats);
                        if (repclis != null) {
                            ventePerClient.setAll(repclis);
                        } else {
                            ventePerClient.clear();
                        }
                        updateTotalSaleperCli();
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
                    dashCardAmort();
                    dashCardResult();
                    creanceToday();
                });
        loadSaleChart();
    }

    private String detectRegion(String role) {
        return UserRoleRegistry.hasAllAccess(pref) || UserRoleRegistry.isTrader(pref) ? "%" : region;
    }

    private String selectedFinancialRegion() {
        boolean globalAccess = UserRoleRegistry.hasAllAccess(pref) || UserRoleRegistry.isTrader(pref);
        String selected = cbx_regions == null ? null : cbx_regions.getSelectionModel().getSelectedItem();
        if (globalAccess && selected != null && !selected.isBlank()) {
            return selected.trim();
        }
        if (globalAccess) {
            return "%";
        }
        return region == null || region.isBlank() ? pref.get("region", "%") : region;
    }

    private void ponctuel() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    List<RecentSale> recents = DataCache.getOrLoad(
                            "report-recent-sales-" + detectRegion(role),
                            () -> RepportDelegate.findRecentSales(detectRegion(role)));
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
            refreshFinancialColumnHeaders();
            loadFinancialStatements();
            loadPurchaseReports();
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
        dpk_debut_report.setValue(LocalDate.now().withDayOfYear(1));
        dpk_fin_report.setValue(LocalDate.of(LocalDate.now().getYear(), 12, 31));
        configureFinancialTables();
        configTableVentePerProd();
        devise = pref.get("mainCur", "USD");
        region = pref.get("region", "...");
        role = UserRoleRegistry.getRole(pref);
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
        double sumSales = DataCache.getOrLoad(
                "report-ca-" + d1 + "-" + kesho + "-" + region,
                () -> RepportDelegate.chiffreDaffaire(d1, kesho, region == null ? "%" : region));
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
        String usedDashRegion = region == null ? "%" : region;
        double ca = DataCache.getOrLoad(
                "report-ca-" + d1 + "-" + kesho + "-" + usedDashRegion,
                () -> RepportDelegate.chiffreDaffaire(d1, kesho, usedDashRegion));
        double cv = DataCache.getOrLoad(
                "report-cv-" + d1 + "-" + kesho + "-" + usedDashRegion,
                () -> RepportDelegate.chargeVariable(d1, kesho, usedDashRegion));
        double result = ca - cv;
        Platform.runLater(() -> {
            txt_result_report.setText(devise + " "
                    + formatNumber(BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        });
    }

    public void dashCardDepense() {
        LocalDate kesho = dpk_fin_report.getValue();
        LocalDate d1 = dpk_debut_report.getValue();
        double exp = DataCache.getOrLoad(
                "report-cv-" + d1 + "-" + kesho + "-" + (region == null ? "%" : region),
                () -> RepportDelegate.chargeVariable(d1, kesho, region == null ? "%" : region));
        Platform.runLater(() -> {
            txt_depense_report.setText(devise + " "
                    + formatNumber(BigDecimal.valueOf(exp).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        });
    }

    public void dashCardAmort() {
        LocalDate kesho = dpk_fin_report.getValue();
        LocalDate d1 = dpk_debut_report.getValue();
        double amort = DataCache.getOrLoad(
                "report-amort-" + d1 + "-" + kesho + "-" + (region == null ? "%" : region),
                () -> RepportDelegate.aggregatedAmortizationOf(d1, kesho, region == null ? "%" : region));
        Platform.runLater(() -> {
            if (txt_amort_report != null) {
                txt_amort_report.setText(devise + " "
                        + formatNumber(BigDecimal.valueOf(amort).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
            }
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
        String usedRegion = UserRoleRegistry.hasAllAccess(pref) || UserRoleRegistry.isTrader(pref) ? null : region;
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
        MainUI.notify(null, "Info", "Utilisez les icônes PDF de chaque état pour générer le document voulu.", 3,
                "info");
    }

    private double scale(Double value) {
        return BigDecimal.valueOf(value == null ? 0d : value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    @FXML
    private void refreshFinancialStates() {
        loadFinancialStatements(true);
    }

    @FXML
    private void recalculateFinancialStates(ActionEvent event) {
        String usedRegion = selectedFinancialRegion();
        MainUI.notify(null, "Etats financiers",
                "Recalcul des agrégats financiers lancé pour "
                + ("%".equals(usedRegion) ? "toutes les régions autorisées" : usedRegion) + ".", 3, "info");
        loadFinancialStatements(true);
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
        loadFinancialStatements(false);
    }

    private void loadFinancialStatements(boolean force) {
        LocalDate d1 = dpk_debut_report.getValue() == null ? LocalDate.now().withDayOfMonth(1)
                : dpk_debut_report.getValue();
        LocalDate d2 = dpk_fin_report.getValue() == null ? LocalDate.now() : dpk_fin_report.getValue();
        if (d1.isAfter(d2)) {
            LocalDate tmp = d1;
            d1 = d2;
            d2 = tmp;
        }
        final LocalDate statementStart = d1;
        final LocalDate statementEnd = d2;
        String usedRegion = selectedFinancialRegion();
        int span = financialHistorySpan;
        String cacheKey = "report-financial-" + span + "-" + statementStart + "-" + statementEnd + "-" + usedRegion;
        if (!force) {
            FinancialReportCache cached = DataCache.get(cacheKey);
            if (cached != null) {
                applyFinancialData(cached);
                return;
            }
        }
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<FinancialStatementRow> bilan;
                List<FinancialStatementRow> compte;
                List<FinancialStatementRow> flux;
                List<String> pivotHeaders = financialDynamicHeaders(statementEnd, span);
                if (span == FINANCIAL_PERIOD_EXACT) {
                    financialStatementService.rebuildStatements(statementStart, statementEnd, usedRegion);
                    bilan = financialStatementService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_BILAN, statementStart, statementEnd, usedRegion);
                    compte = financialStatementService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, statementStart, statementEnd, usedRegion);
                    flux = financialStatementService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, statementStart, statementEnd, usedRegion);
                } else if (span == 4) {
                    financialStatementService.ensureQuarterlyStatements(statementEnd, span, usedRegion);
                    bilan = financialStatementService.loadStatementRowsQuarterly(
                            FinancialStatementAgregateService.STATEMENT_BILAN, statementEnd, span, usedRegion);
                    compte = financialStatementService.loadStatementRowsQuarterly(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, statementEnd, span, usedRegion);
                    flux = financialStatementService.loadStatementRowsQuarterly(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, statementEnd, span, usedRegion);
                } else {
                    int anchorYear = statementEnd.getYear();
                    financialStatementService.ensureYearlyStatements(anchorYear, span, usedRegion);
                    bilan = financialStatementService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_BILAN, anchorYear, span, usedRegion);
                    compte = financialStatementService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, anchorYear, span, usedRegion);
                    flux = financialStatementService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, anchorYear, span, usedRegion);
                }

                FinancialReportCache data = new FinancialReportCache(bilan, compte, flux, pivotHeaders);
                DataCache.put(cacheKey, data);
                applyFinancialData(data);
            } catch (Exception ex) {
                Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE,
                        "Erreur de chargement des états financiers", ex);
                Platform.runLater(() -> MainUI.notify(null, "Erreur",
                        "Impossible de générer les états financiers sur cette période", 4, "error"));
            }
        });
    }

    private void applyFinancialData(FinancialReportCache data) {
        List<FinancialRowModel> pivotBilanRows = buildDynamicFinancialRows("Bilan", data.bilan, data.pivotHeaders.size());
        List<FinancialRowModel> pivotCompteRows = buildDynamicFinancialRows("Compte de résultat", data.compte, data.pivotHeaders.size());
        List<FinancialRowModel> pivotFluxRows = buildDynamicFinancialRows("Flux de trésorerie", data.flux, data.pivotHeaders.size());
        Platform.runLater(() -> {
            bilanRows.setAll(data.bilan);
            compteResultatRows.setAll(data.compte);
            fluxRows.setAll(data.flux);
            if (tb_fin_pivot_bilan != null) {
                FinancialTableBinder.bindWithHeaders(tb_fin_pivot_bilan, data.pivotHeaders, pivotBilanRows);
            }
            if (tb_fin_pivot_cr != null) {
                FinancialTableBinder.bindWithHeaders(tb_fin_pivot_cr, data.pivotHeaders, pivotCompteRows);
            }
            if (tb_fin_pivot_flux != null) {
                FinancialTableBinder.bindWithHeaders(tb_fin_pivot_flux, data.pivotHeaders, pivotFluxRows);
            }
        });
    }

    private static final class FinancialReportCache {
        final List<FinancialStatementRow> bilan;
        final List<FinancialStatementRow> compte;
        final List<FinancialStatementRow> flux;
        final List<String> pivotHeaders;

        FinancialReportCache(List<FinancialStatementRow> bilan, List<FinancialStatementRow> compte,
                List<FinancialStatementRow> flux, List<String> pivotHeaders) {
            this.bilan = bilan;
            this.compte = compte;
            this.flux = flux;
            this.pivotHeaders = pivotHeaders;
        }
    }

    private List<Integer> financialPivotYears(LocalDate statementEnd, int span) {
        if (span != 3 && span != 5) {
            return List.of();
        }
        int anchorYear = statementEnd == null ? LocalDate.now().getYear() : statementEnd.getYear();
        List<Integer> years = new ArrayList<>();
        for (int year = anchorYear - span + 1; year <= anchorYear; year++) {
            years.add(year);
        }
        return years;
    }

    private List<String> financialDynamicHeaders(LocalDate statementEnd, int span) {
        int anchorYear = statementEnd == null ? LocalDate.now().getYear() : statementEnd.getYear();
        if (span == 4) {
            return List.of("T1-" + anchorYear, "T2-" + anchorYear, "T3-" + anchorYear, "T4-" + anchorYear);
        }
        if (span == 3 || span == 5) {
            List<String> headers = new ArrayList<>();
            for (int year = anchorYear; year >= anchorYear - span + 1; year--) {
                headers.add(String.valueOf(year));
            }
            return headers;
        }
        return List.of("Période", "Période précédente 1", "Période précédente 2", "Période précédente 3");
    }

    private List<FinancialRowModel> buildDynamicFinancialRows(String statementType,
            List<FinancialStatementRow> source, int valueCount) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return java.util.stream.IntStream.range(0, source.size())
                .mapToObj(index -> toFinancialRowModel(statementType, index + 1, source.get(index), valueCount))
                .collect(Collectors.toList());
    }

    private List<FinancialRowModel> filterDynamicRowsByType(List<FinancialRowModel> rows, String statementType) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<FinancialRowModel> filtered = new ArrayList<>();
        for (FinancialRowModel row : rows) {
            if (statementType.equals(row.getStatementType())) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private void appendDynamicFinancialRows(List<FinancialRowModel> target, String statementType,
            List<FinancialStatementRow> source, int valueCount) {
        if (source == null || source.isEmpty()) {
            return;
        }
        int order = target.size() + 1;
        for (FinancialStatementRow sourceRow : source) {
            FinancialRowModel row = new FinancialRowModel();
            row.setSortOrder(order++);
            row.setStatementType(statementType);
            row.setLineCode(sourceRow.getCode());
            row.setRubrique(sourceRow.getRubrique());
            row.setNature(sourceRow.getNature());
            row.setTotal(sourceRow.isTotalLine());
            row.setSectionHeader(sourceRow.isSectionHeader());
            setDynamicValue(row, 0, sourceRow.getAmountN(), valueCount);
            setDynamicValue(row, 1, sourceRow.getAmountN1(), valueCount);
            setDynamicValue(row, 2, sourceRow.getAmountN2(), valueCount);
            setDynamicValue(row, 3, sourceRow.getAmountN3(), valueCount);
            setDynamicValue(row, 4, sourceRow.getAmountN4(), valueCount);
            target.add(row);
        }
    }

    private FinancialRowModel toFinancialRowModel(String statementType, int order, FinancialStatementRow sourceRow,
            int valueCount) {
        FinancialRowModel row = new FinancialRowModel();
        row.setSortOrder(order);
        row.setStatementType(statementType);
        row.setLineCode(sourceRow.getCode());
        row.setRubrique(sourceRow.getRubrique());
        row.setNature(sourceRow.getNature());
        row.setTotal(sourceRow.isTotalLine());
        row.setSectionHeader(sourceRow.isSectionHeader());
        List<Double> values = new ArrayList<>();
        values.add(sourceRow.getAmountN());
        values.add(sourceRow.getAmountN1());
        values.add(sourceRow.getAmountN2());
        values.add(sourceRow.getAmountN3());
        values.add(sourceRow.getAmountN4());
        Map<Integer, Double> valuesByColumn = java.util.stream.IntStream.range(0, Math.min(valueCount, values.size()))
                .boxed()
                .collect(Collectors.toMap(
                        index -> index,
                        index -> values.get(index) == null ? 0d : values.get(index),
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new));
        valuesByColumn.forEach(row::setValueForYear);
        return row;
    }

    private void setDynamicValue(FinancialRowModel row, int index, Double value, int valueCount) {
        if (index < valueCount) {
            row.setValueForYear(index, value == null ? 0d : value);
        }
    }

    @FXML
    private void exportBilanPdf(MouseEvent event) {
        exportFinancialPdf("Bilan Comptable Financier", bilanRows);
    }

    @FXML
    private void exportCompteResultatPdf(MouseEvent event) {
        exportFinancialPdf("Compte de Résultat Standard", compteResultatRows);
    }

    @FXML
    private void exportFluxPdf(MouseEvent event) {
        exportFinancialPdf("Tableau de Flux de Trésorerie", fluxRows);
    }

    private String formatMonnaie(double amount) {
        return devise + " " + Util.toPlain(scale(amount));
    }

    private void exportFinancialPdf(String title, List<FinancialStatementRow> rows) {
        if (rows == null || rows.isEmpty()) {
            MainUI.notify(null, "Erreur", "Aucune donnée financière à exporter pour cette période", 3, "error");
            return;
        }
        LocalDate start = dpk_debut_report.getValue() == null ? LocalDate.now() : dpk_debut_report.getValue();
        LocalDate end = dpk_fin_report.getValue() == null ? LocalDate.now() : dpk_fin_report.getValue();
        
        List<String> dataHeaders = financialDynamicHeaders(end, financialHistorySpan);

        new Thread(() -> {
            try {
                File file = FinancialStatementPdfExporter.export(entreprise, title, start, end, rows, dataHeaders);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                Platform.runLater(() -> MainUI.notify(null, "PDF",
                        "Rapport financier généré : " + file.getName(), 4, "info"));
            } catch (Exception ex) {
                Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> MainUI.notify(null, "Erreur", "Echec de génération du PDF demandé", 4,
                        "error"));
            }
        }).start();
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
    //     List<Operation> vts =
    //             store.findAllByLocalDateIntervalInRegion(Operation.class,
    //             dpk_debut_report.getValue(), dpk_fin_report.getValue(), region);
    //     return vts;
    // }

}
