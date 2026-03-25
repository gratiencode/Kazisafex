/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.endeleya.kazisafex;

import data.Depense;
import delegates.MatiereDelegate;
import data.Depot;
import data.Entreposer;
import data.Entreprise;
import data.Imputer;
import data.Livraison;
import data.Matiere;
import data.MatiereSku;
import data.Mesure;
import data.Operation;
import data.Production;
import data.Produit;
import data.Repartir;
import data.Stocker;
import data.core.KazisafeServiceFactory;
import data.helpers.EntreposerHelper;
import data.network.Kazisafe;
import delegates.CategoryDelegate;
import delegates.DepenseDelegate;
import delegates.DepotDelegate;
import delegates.EntreposerDelegate;
import delegates.FournisseurDelegate;
import delegates.ImputerDelegate;
import delegates.LivraisonDelegate;
import delegates.MatiereSkuDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.ProductionDelegate;
import delegates.ProduitDelegate;
import delegates.RepartirDelegate;
import delegates.StockerDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import retrofit2.Call;
import retrofit2.Response;
import tools.ComboBoxAutoCompletion;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;

/**
 * FXML Controller class
 *
 * @author endeleya
 */
public class ProductionController implements Initializable {

    public static ProductionController getInstance() {
        return instance;
    }

    Kazisafe kazisafe;
    ObservableList<Entreposer> ols_entreposer;
    ObservableList<Depot> ols_entr_deposer;
    ObservableList<Matiere> ols_intra_matiere;
    ObservableList<Livraison> ols_entr_livr;
    ObservableList<MatiereSku> ols_sku_entr;
    ObservableList<MatiereSku> ols_newsku_entr;
    ObservableList<String> regions;
    ObservableList<Entreposer> ols_entreposer_stock, ols_entreposer_pfini;
    ObservableList<EntreposerHelper> ols_entreposeraff_indir;
    ObservableList<Operation> ols_operations;
    ObservableList<Depense> ols_depenses;
    ObservableList<Produit> ols_produits;
    ObservableList<Mesure> ols_mesures, ols_mesure_deprod;
    ObservableList<Production> ols_productions;
    ObservableList<Production> ols_productions_terminee;
    ObservableList<Repartir> ols_repartirs;
    ObservableList<Imputer> ols_imputers;
    private ObservableList<Imputer> ols_imputeraff_indir;

    @FXML
    private Pagination pgnation_tbl_entreposage;
    @FXML
    private TableView<Entreposer> tbl_entreposage;
    @FXML
    private TableColumn<Entreposer, String> col_date_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_livraison_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_matiere_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_type_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_quant_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_cout_unitr_intrapos;
    @FXML
    private TableColumn<Entreposer, Number> col_total_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_lot_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_peremption_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_depot_intrapos;
    @FXML
    private TableColumn<Entreposer, String> col_region_intrapos;
    @FXML
    private TableView<Entreposer> tbl_stock_entreposage;
    @FXML
    private TableColumn<Entreposer, String> col_nom_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_type_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_depot_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_entree_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_sortie_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_stock_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_cout_intrant_stock;
    @FXML
    private TableColumn<Entreposer, Number> col_total_value_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_lot_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_peremption_intrant_stock;
    @FXML
    private TableColumn<Entreposer, String> col_region_intrant_stock;
    @FXML
    private ComboBox<Livraison> cbx_livraison_intraposage;
    @FXML
    private TextField tf_quantite_intrapos;
    @FXML
    private TextField tf_coutach_intrapos;
    @FXML
    private ComboBox<MatiereSku> cbx_sku_intrapos;
    @FXML
    private ComboBox<String> cbx_devise_intrapos;
    @FXML
    private ComboBox<String> cbx_region_intraposage;
    @FXML
    private DatePicker dpk_dateexp_intrapos;
    @FXML
    private TextField tf_numlot_intrapos;
    @FXML
    private DatePicker dpk_date_intraposage;
    @FXML
    private ComboBox<Depot> cbx_depot_intrapos;
    @FXML
    private ComboBox<Matiere> cbx_matiere_intraposage;
    @FXML
    private TextArea txtarea_comment_intrapos;
    @FXML
    private Label txt_type_matiere_intrapos;
    @FXML
    private AnchorPane anchorpn_depot_intrant;
    @FXML
    private TabPane tabpn_intrant_depot;
    @FXML
    private TextField tf_nom_intrant;
    @FXML
    private ComboBox<String> cbx_type_intrant;
    @FXML
    private ComboBox<String> cbx_region_intrant;
    @FXML
    private ComboBox<String> cbx_sku_intrant;
    @FXML
    private TextField tf_quant_sku_intrant;
    @FXML
    private CheckBox chbx_perissable_intrant;
    @FXML
    private TextField tf_search_intrant;
    @FXML
    private TilePane tilepn_list_sku_intrant;
    @FXML
    private TableView<Matiere> tbl_intrant;
    @FXML
    private TableColumn<Matiere, String> col_designation_intrant;
    @FXML
    private TableColumn<Matiere, String> col_type_intrant;
    @FXML
    private TableColumn<Matiere, String> col_perissable_intrant;
    @FXML
    private TableColumn<Matiere, String> col_lessku_intrant;
    @FXML
    private TableColumn<Matiere, String> col_region_intrant;
    @FXML
    private TextField tf_nom_depot;
    @FXML
    private ComboBox<String> cbx_region_depot;
    @FXML
    private TextField tf_dimens_depot;
    @FXML
    private ComboBox<String> cbx_type_depot;
    @FXML
    private TextField tf_search_depot;
    @FXML
    private TableView<Depot> tbl_depot;
    @FXML
    private TableColumn<Depot, String> col_nom_depot;
    @FXML
    private TableColumn<Depot, String> col_type_depot;
    @FXML
    private TableColumn<Depot, String> col_dimension_depot;
    @FXML
    private TableColumn<Depot, String> col_region_depot;
    @FXML
    private Label txt_count_intrant;
    @FXML
    private Label txt_count_intraprod;
    @FXML
    private Label txt_valeur_global_intraprod;
    Preferences pref;
    String region, role, token;
    ObservableDoubleValue stockValue;
    private String position = "Donnee";
    Entreprise entreprise;

    Depot choosenDepot;
    Entreposer choosenEntreposer, choosenProdFinished;
    Entreposer currentChoosenEntreposer;
    Matiere choosenMatiere;
    MatiereSku choosenSku;
    Production choosenProduction, choosenFinishedProduct;
    ResourceBundle bundle;
    @FXML
    private TabPane tabpn_entreposer_in_stock;
    @FXML
    private DatePicker dpk_start_period_entrepo;
    @FXML
    private DatePicker dpk_end_period_entrepo;
    @FXML
    private TabPane tabpn_produktion;
    @FXML
    private ComboBox<String> cbx_region_prod;
    @FXML
    private DatePicker dpk_now_prod;
    @FXML
    private TableView<Production> tbl_productions;
    @FXML
    private TableColumn<Production, String> col_produit_prod;
    @FXML
    private TableColumn<Production, String> col_lot_produit_prod;
    @FXML
    private TableColumn<Production, String> col_exp_produit_prod;
    @FXML
    private TableColumn<Production, String> col_date_start_prod;
    @FXML
    private TableColumn<Production, String> col_date_end_prod;
    @FXML
    private TableColumn<Production, String> col_quantite_prod;
    @FXML
    private TableColumn<Production, String> col_qualite_prod;
    @FXML
    private TableColumn<Production, String> col_etat_prod;
    @FXML
    private TableColumn<Production, String> col_region_prod;
    @FXML
    private ComboBox<Matiere> cbx_matiere_to_prod;
    @FXML
    private Label txt_stock_matiere_to_prod;
    @FXML
    private TextField tf_qte_reparti_matiere_prod;
    @FXML
    private ComboBox<MatiereSku> cbx_sku_matiere_to_prod;
    @FXML
    private Label txt_sum_choosen_matiere_to_prod;
    @FXML
    private TableView<Repartir> tbl_matiere_affected_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_date_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_type_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_lot_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_expir_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_qte_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_cout_mat_aff_to_prod;
    @FXML
    private TableColumn<Repartir, String> col_tot_mat_aff_to_prod;
    @FXML
    private Label txt_total_mat_aff_to_prod;
    @FXML
    private ComboBox<Operation> cbx_charge_aff_to_prod;
    @FXML
    private Label txt_sum_charg_aff_to_prod;
    @FXML
    private TextField tf_quot_charg_aff_to_prod;
    @FXML
    private ComboBox<String> cbx_deviz_charg_aff_to_prod;
    @FXML
    private TableView<Imputer> tbl_charges_prod;
    @FXML
    private TableColumn<Imputer, String> col_date_charg_aff_to_prod;
    @FXML
    private TableColumn<Imputer, String> col_depense_charg_aff_to_prod;
    @FXML
    private TableColumn<Imputer, String> col_sumquot_charg_aff_to_prod;
    @FXML
    private TableColumn<Imputer, String> col_percent_charg_aff_to_prod;
    @FXML
    private Label txtl_total_charg_aff_to_prod;
    @FXML
    private TextField tf_lot_to_prod;
    @FXML
    private TextField tf_quant_to_prod;
    @FXML
    private ComboBox<String> cbx_state_prod;
    @FXML
    private TextArea txtArea_comment_prod;
    @FXML
    private ComboBox<String> cbx_quality_to_prod;
    @FXML
    private DatePicker dpk_start_prod;
    @FXML
    private DatePicker dpk_end_prod;
    @FXML
    private ComboBox<Mesure> cbx_mesure_to_prod;
    @FXML
    private ComboBox<Produit> cbx_produits_prod;
    @FXML
    private DatePicker dpk_expir_to_prod;
    @FXML
    private Label txt_toolbar_etat_prod;
    @FXML
    private TextField tf_search_production;
    @FXML
    private TextField tf_search_repartir;
    @FXML
    private TextField tf_search_charge;
    double taux;

    private static ProductionController instance;
    @FXML
    private ComboBox<String> cbx_avancement_deprod;
    @FXML
    private ComboBox<String> cbx_region_deprod;
    @FXML
    private Label txt_cout_tot_deprod;
    @FXML
    private TextField tf_quant_deprod;
    @FXML
    private Label txt_de_prodUnit_prevu_deprod;
    @FXML
    private ComboBox<String> cbx_region_dest_deprod;
    @FXML
    private ComboBox<Mesure> cbx_mesure_deprod;
    @FXML
    private Label txt_quant_prevu_deprod;
    @FXML
    private Label txt_produit_deprod;
    @FXML
    private Label txt_cout_proUnit_deprod;
    @FXML
    private DatePicker dpk_expiry_deprod;
    @FXML
    private Label txt_lot_deprod;
    @FXML
    private ComboBox<String> cbx_quality_deprod;
    @FXML
    private TextArea txtArea_comment_deprod;
    @FXML
    private ComboBox<Depot> cbx_depot_deprod;
    @FXML
    private CheckBox chbx_to_sale_deprod;
    @FXML
    private Label txt_matlot_to_prod;
    @FXML
    private ComboBox<Depense> cbx_charge_indir;
    @FXML
    private Label txt_sumcharge_indir;
    @FXML
    private DatePicker dpk_dujour_indir;
    @FXML
    private TableView<EntreposerHelper> tbl_affectation_indir;
    @FXML
    private Label txt_sumaffect_indir;
    @FXML
    private TableView<Imputer> tbl_savedaffefct_indir;
    @FXML
    private DatePicker dpk_end_indir;
    @FXML
    private DatePicker dpk_start_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_prodaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_lotaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_debutaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_findaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_quantaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_coutaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_coutotaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, Number> col_quotaff_indir;
    @FXML
    private TableColumn<EntreposerHelper, String> col_regionaff_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffprod_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedafflot_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffdebut_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedafffin_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffquant_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffcout_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffcoutotaff_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffquot_indir;
    @FXML
    private TableColumn<Imputer, String> col_savedaffregion_indir;
    @FXML
    private TableColumn<Imputer, String> col_savednomcharg_indir;
    @FXML
    private ComboBox<Production> cbx_production_terminees;
    @FXML
    private TableView<Entreposer> tbl_prod_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_date_prodeposee;
    @FXML
    private TableColumn<Entreposer, String> col_prod8_deposees;
    @FXML
    private TableColumn<Entreposer, String> cout_unit_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_qte_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_coutotal_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_debut_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_fin_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_etat_deposee;
    @FXML
    private TableColumn<Entreposer, String> col_lot_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_expir_deposee;
    @FXML
    private TableColumn<Entreposer, String> col_region_deposees;
    @FXML
    private TableColumn<Entreposer, String> col_depot_deposees;
    @FXML
    private AnchorPane tab_produit_fini;

    public ProductionController() {
        instance = this;
    }

    public void addProduct(Produit p) {
        ols_produits.add(p);
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
        token = pref.get("token", null);
        taux = pref.getDouble("taux2change", 2300);
        anchorpn_depot_intrant.setVisible(false);

        ols_entr_deposer = FXCollections.observableArrayList();
        ols_operations = FXCollections.observableArrayList();
        ols_mesures = FXCollections.observableArrayList();
        ols_mesure_deprod = FXCollections.observableArrayList();
        ols_produits = FXCollections.observableArrayList();
        ols_productions = FXCollections.observableArrayList();
        ols_productions_terminee = FXCollections.observableArrayList();
        ols_depenses = FXCollections.observableArrayList();
        ols_repartirs = FXCollections.observableArrayList();
        ols_imputers = FXCollections.observableArrayList();
        ols_imputeraff_indir = FXCollections.observableArrayList();
        ols_produits.addAll(ProduitDelegate.findProduits());
        cbx_mesure_to_prod.setItems(ols_mesures);
        cbx_mesure_deprod.setItems(ols_mesure_deprod);
        cbx_produits_prod.setItems(ols_produits);
        cbx_charge_aff_to_prod.setItems(ols_operations);
        cbx_production_terminees.setItems(ols_productions_terminee);
        tbl_productions.setItems(ols_productions);
        tbl_matiere_affected_to_prod.setItems(ols_repartirs);
        tbl_charges_prod.setItems(ols_imputers);
        tbl_depot.setItems(ols_entr_deposer);
        tbl_savedaffefct_indir.setItems(ols_imputeraff_indir);
        cbx_depot_intrapos.setItems(ols_entr_deposer);
        cbx_depot_deprod.setItems(ols_entr_deposer);

        ols_entr_livr = FXCollections.observableArrayList();
        ols_operations.setAll(OperationDelegate.findOperationByImputation(Constants.DEPT_PRODUCTION));

        cbx_livraison_intraposage.setItems(ols_entr_livr);
        ols_intra_matiere = FXCollections.observableArrayList();
        tbl_intrant.setItems(ols_intra_matiere);
        cbx_matiere_intraposage.setItems(ols_intra_matiere);

        cbx_matiere_to_prod.setItems(FXCollections.observableArrayList(MatiereDelegate.findMatieres()
                .stream().filter(i -> (i.getTypeMatiere().equals("Matiere premiere")
                || i.getTypeMatiere().equals("Matiere consommable")
                || i.getTypeMatiere().equals("Produit semi fini")))
                .collect(Collectors.toList())));
        ols_entreposer_stock = FXCollections.observableArrayList();
        ols_entreposer_pfini = FXCollections.observableArrayList();
        ols_entreposeraff_indir = FXCollections.observableArrayList();
        tbl_affectation_indir.setItems(ols_entreposeraff_indir);
        tbl_stock_entreposage.setItems(ols_entreposer_stock);
        tbl_prod_deposees.setItems(ols_entreposer_pfini);
        ols_entreposer = FXCollections.observableArrayList();
        tbl_entreposage.setItems(ols_entreposer);
        ols_sku_entr = FXCollections.observableArrayList();
        ols_newsku_entr = FXCollections.observableArrayList();
        cbx_sku_intrapos.setItems(ols_sku_entr);
        cbx_sku_matiere_to_prod.setItems(ols_sku_entr);
        regions = FXCollections.observableArrayList();
        cbx_region_depot.setItems(regions);
        cbx_region_intrant.setItems(regions);
        cbx_region_intraposage.setItems(regions);
        cbx_region_prod.setItems(regions);
        cbx_region_deprod.setItems(regions);
        cbx_region_dest_deprod.setItems(regions);
        cbx_region_intrant.getSelectionModel().selectFirst();
        cbx_region_intraposage.getSelectionModel().selectFirst();
        initCombos();
        initTables();
        initUpdaTable();
        initTbl();
// TODO
    }

    public void initArgs(Object... objs) {
        entreprise = (Entreprise) objs[0];
        kazisafe = KazisafeServiceFactory.createService(token);
        tbl_depot.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Depot> ov, Depot t, Depot t1) -> {
                    if (t1 != null) {
                        choosenDepot = t1;
                    }
                });
        tbl_intrant.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Matiere> ov, Matiere t, Matiere t1) -> {
                    if (t1 != null) {
                        choosenMatiere = t1;
                        tf_nom_intrant.setText(choosenMatiere.getMatiereName());
                        cbx_type_intrant.setValue(choosenMatiere.getTypeMatiere());
                        cbx_region_intrant.getSelectionModel().select(choosenMatiere.getRegion());
                        chbx_perissable_intrant.setSelected(choosenMatiere.getPerissable());
                        List<MatiereSku> lsku = MatiereSkuDelegate.findMatiereSkuFor(choosenMatiere.getUid());
                        for (MatiereSku m : lsku) {
                            addMatiereSku(m.getNomSku() + ":" + m.getQuantContenuSku());
                        }
                    }
                });
        tbl_entreposage.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Entreposer> ov, Entreposer t, Entreposer t1) -> {
                    if (t1 != null) {
                        choosenEntreposer = t1;
                        Livraison livr = LivraisonDelegate.findLivraison(choosenEntreposer.getLivraisonId().getUid());
                        cbx_livraison_intraposage.getSelectionModel().select(livr);
                        Matiere mat = MatiereDelegate.findMatiere(choosenEntreposer.getMatiereId().getUid());
                        cbx_matiere_intraposage.getSelectionModel().select(mat);
                        tf_quantite_intrapos.setText(String.valueOf(choosenEntreposer.getQuantite()));
                        tf_coutach_intrapos.setText(String.valueOf(choosenEntreposer.getCout()));
                        cbx_devise_intrapos.setValue(choosenEntreposer.getDevise());
                        Depot d = DepotDelegate.findDepot(choosenEntreposer.getDepotId().getUid());

                        cbx_depot_intrapos.getSelectionModel().select(d);
                        List<MatiereSku> ls = MatiereSkuDelegate.findMatiereSkuFor(mat.getUid());
                        ols_sku_entr.setAll(ls);
                        cbx_sku_intrapos.getSelectionModel().select(choosenEntreposer.getSkuId());
                        String cmt = choosenEntreposer.getComment();
                        txtarea_comment_intrapos.setText(cmt == null ? "" : cmt);
                        tf_numlot_intrapos.setText(choosenEntreposer.getNumlot());
                        dpk_dateexp_intrapos.setValue(choosenEntreposer.getExpiryDate());
                        cbx_region_intraposage.getSelectionModel().select(choosenEntreposer.getRegion());
                    }
                });
        cbx_matiere_intraposage.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Matiere> ov, Matiere t, Matiere t1) -> {
                    if (t1 != null) {
                        txt_type_matiere_intrapos.setText(t1.getTypeMatiere());
                        List<MatiereSku> skus = MatiereSkuDelegate.findMatiereSkuFor(t1.getUid());
                        ols_sku_entr.setAll(skus);
                    }
                });
        Platform.runLater(() -> {
            txt_count_intraprod.textProperty().bind(Bindings
                    .size(tbl_entreposage.getItems()).asString("%d Entreposages"));
        });
        tf_quant_deprod.textProperty()
                .addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
                    if (!t1.isEmpty()) {
                        if (choosenFinishedProduct == null) {
                            return;
                        }
                        try {
                            double q = Double.parseDouble(t1);
                            double chargDir = choosenFinishedProduct.getImputerList().stream().mapToDouble(i -> i.getDevise().equals("USD") ? i.getMontant()
                                    : (i.getMontant() / taux)).sum();
                            double sumMp = choosenFinishedProduct.getRepartirList().stream().mapToDouble(mp -> mp.getQuantite() * mp.getCoutAchat()).sum();
                            double ct = (chargDir + sumMp) / q;
                            txt_cout_proUnit_deprod.setText("C U. Prod : " + BigDecimal.valueOf(ct).setScale(2, RoundingMode.HALF_EVEN).toPlainString() + " USD");
                        } catch (NumberFormatException e) {
                            MainUI.notify(null, "Error", "Entrer le chiffres uniquement.", 5, "error");
                        }
                    }
                });
        kazisafe.getRegions().enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        pref.put("region" + (++i), reg);
                    }
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
        cbx_produits_prod.getSelectionModel().selectFirst();
        cbx_produits_prod.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) -> {
            Produit pr = newValue;
            List<Mesure> mzs = MesureDelegate.findMesureByProduit(pr.getUid());
            ols_mesures.setAll(mzs);
        });
        cbx_production_terminees.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Production> observable, Production oldValue, Production newValue) -> {
            if (newValue != null) {
                choosenFinishedProduct = newValue;
                List<Imputer> li = ImputerDelegate.findForProduction(newValue.getUid());
                List<Repartir> lr = RepartirDelegate.findForProduction(newValue.getUid());
                double chargDir = li.stream().mapToDouble(i -> i.getDevise().equals("USD") ? i.getMontant()
                        : (i.getMontant() / taux)).sum();
                double sumMp = lr.stream().mapToDouble(mp -> mp.getQuantite() * mp.getCoutAchat()).sum();
                txt_cout_tot_deprod.setText("Cout Prod Total: " + (chargDir + sumMp) + " USD");
                txt_quant_prevu_deprod.setText("Qte prevu : " + newValue.getQuantitePrevu() + " " + newValue.getMesureId().getDescription());
                double cpup = (chargDir + sumMp) / newValue.getQuantitePrevu();
                txt_de_prodUnit_prevu_deprod.setText(BigDecimal.valueOf(cpup).setScale(2, RoundingMode.HALF_EVEN).toPlainString() + " USD");
            }
        });
        tbl_productions.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Production> ov, Production t, Production t1) -> {
                    if (t1 != null) {
                        choosenProduction = t1;
                        txt_toolbar_etat_prod.setText(choosenProduction.getEtat());
                        showValue(t1);
                        Produit p = choosenProduction.getProduitId();
                        cbx_produits_prod.setValue(p);
                        cbx_region_prod.setValue(choosenProduction.getRegion());
                        cbx_state_prod.setValue(choosenProduction.getEtat());
                        tf_lot_to_prod.setText(choosenProduction.getNumlot());
                        cbx_quality_to_prod.setValue(choosenProduction.getQualitePrevu());
                        tf_quant_to_prod.setText(String.valueOf(choosenProduction.getQuantitePrevu()));
                        dpk_start_prod.setValue(choosenProduction.getDateDebut());
                        dpk_end_prod.setValue(choosenProduction.getDateFin());
                        dpk_expir_to_prod.setValue(choosenProduction.getDatePeremption());
                        txtArea_comment_prod.setText(choosenProduction.getComment());
                        ols_mesures.setAll(MesureDelegate.findMesureByProduit(p.getUid()));
                        cbx_mesure_to_prod.getSelectionModel().select(choosenProduction.getMesureId());
                        txt_valeur_global_intraprod.textProperty().bind(
                                Bindings.createStringBinding(() -> "CT de Prod : "
                                + sumprod(tbl_matiere_affected_to_prod.getItems()),
                                        tbl_charges_prod.getItems(), tbl_matiere_affected_to_prod.getItems()));

                    }
                });
        tf_qte_reparti_matiere_prod.textProperty()
                .addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
                    if (!t1.isEmpty()) {
                        try {
                            Double d = Double.valueOf(t1);
                            double cout = currentChoosenEntreposer.getCout();
                            double total = d * cout;
                            txt_sum_choosen_matiere_to_prod.setText(total + " " + currentChoosenEntreposer.getDevise());
                        } catch (NumberFormatException e) {
                        }
                    }
                });
        cbx_matiere_to_prod.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Matiere> ov, Matiere t, Matiere t1) -> {
                    if (t1 != null) {
                        currentChoosenEntreposer = findOngoingEntreposer(t1.getUid());
                        txt_matlot_to_prod.setText("Lot: " + currentChoosenEntreposer.getNumlot());
                        MatiereSku fss = currentChoosenEntreposer.getSkuId();
                        double qt = stockFinalMatierePremiere(fss, t1);
                        txt_stock_matiere_to_prod.setText(qt + " " + fss.getNomSku());
                        List<MatiereSku> skus = MatiereSkuDelegate.findMatiereSkuFor(t1.getUid());
                        ols_sku_entr.setAll(skus);
                    }
                });
        tf_search_charge.textProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        if (t1.isEmpty()) {
                            tbl_charges_prod.setItems(ols_imputers);
                        } else {
                            ObservableList<Imputer> result = FXCollections.observableArrayList();
                            for (Imputer imputer : ols_imputers) {
                                String mix = imputer.getOperationId().getDepenseId().getNomDepense()
                                        + " " + imputer.getOperationId().getLibelle() + " " + imputer.getRegion();
                                if (mix.toUpperCase().contains(t1.toUpperCase())) {
                                    result.add(imputer);
                                }
                            }
                            tbl_charges_prod.setItems(result);

                        }
                    }
                });

        tf_search_production.textProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        if (t1.isEmpty()) {
                            tbl_productions.setItems(ols_productions);
                        } else {
                            ObservableList<Production> result = FXCollections.observableArrayList();
                            for (Production pr : ols_productions) {
                                String mix = pr.getEtat() + "" + pr.getNumlot() + "" + pr.getDateDebut().toString()
                                        + "" + pr.getDateFin().toString() + "" + pr.getProduitId().getNomProduit()
                                        + "" + pr.getProduitId().getModele();
                                if (mix.toUpperCase().contains(t1.toUpperCase())) {
                                    result.add(pr);
                                }
                            }
                            tbl_productions.setItems(result);

                        }
                    }
                });
        tf_search_repartir.textProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        if (t1.isEmpty()) {
                            tbl_matiere_affected_to_prod.setItems(ols_repartirs);
                        } else {
                            ObservableList<Repartir> result = FXCollections.observableArrayList();
                            for (Repartir r : ols_repartirs) {
                                String mix = r.getNumlot() + "" + r.getRegion() + "" + r.getMatiereId().getMatiereName() + ""
                                        + r.getMatiereId().getTypeMatiere();
                                if (mix.toUpperCase().contains(t1.toUpperCase())) {
                                    result.add(r);
                                }
                            }
                            tbl_matiere_affected_to_prod.setItems(result);
                        }
                    }
                });
        tabpn_produktion.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Tab> ov, Tab t, Tab t1) -> {
                    if (t1.getText().equals("Production")) {

                        Platform.runLater(() -> {
                            txt_count_intraprod.textProperty().bind(Bindings
                                    .size(tbl_productions.getItems()).asString("Production: %d"));
                            txt_valeur_global_intraprod.textProperty().bind(
                                    Bindings.createStringBinding(() -> "CT de Prod : "
                                    + sumprod(tbl_matiere_affected_to_prod.getItems()),
                                            tbl_charges_prod.getItems(),
                                            tbl_matiere_affected_to_prod.getItems()));
                        });
                    } else if (t1.getText().equals("Intrants")) {
                        Platform.runLater(() -> {
                            txt_count_intraprod.textProperty().bind(Bindings
                                    .size(tbl_entreposage.getItems()).asString("%d Entreposages"));
                            txt_valeur_global_intraprod.textProperty().bind(
                                    Bindings.createStringBinding(() -> "Entrees : "
                                    + (tbl_entreposage.getItems()
                                            .stream()
                                            .mapToDouble(m -> (m.getCout() * m.getQuantite())).sum()),
                                            tbl_prod_deposees.getItems()));
                        });
                    } else if (t1.getText().equals("Produits finis")) {
                        Platform.runLater(() -> {
                            txt_count_intraprod.textProperty().bind(Bindings
                                    .size(tbl_prod_deposees.getItems()).asString("Produits fini: %d"));
                            txt_valeur_global_intraprod.textProperty().bind(
                                    Bindings.createStringBinding(() -> "Valeur stock PF : "
                                    + (tbl_prod_deposees.getItems()
                                            .stream()
                                            .mapToDouble(m -> (m.getCout() * m.getQuantite())).sum()),
                                            tbl_prod_deposees.getItems()));

                        });
                    } else if (t1.getText().equals("Charges indirectes")) {

                    }
                });

        loadData();
        new ComboBoxAutoCompletion<>(cbx_livraison_intraposage);
        new ComboBoxAutoCompletion<>(cbx_matiere_intraposage);
        new ComboBoxAutoCompletion<>(cbx_matiere_to_prod);
        new ComboBoxAutoCompletion<>(cbx_produits_prod);
        new ComboBoxAutoCompletion<>(cbx_charge_aff_to_prod);
    }

    private double sumprod(List<Repartir> lr) {
        double mp = lr.stream()
                .mapToDouble(r -> (r.getCoutAchat() * r.getQuantite()))
                .sum();
        double ch = tbl_charges_prod.getItems()
                .stream().mapToDouble(c -> c.getMontant()).sum();
        return (ch + mp);
    }

    private void showValue(Production t1) {
        List<Repartir> lrp = RepartirDelegate.findForProduction(t1.getUid());
        ols_repartirs.setAll(lrp);
        List<Imputer> limp = ImputerDelegate.findForProduction(t1.getUid());
        ols_imputers.setAll(limp);
        //txt_valeur_global_intraprod.setText("C.T de Prod : " + getCost(lrp, limp));
    }

    private double getCost(List<Repartir> lrs, List<Imputer> limp) {
        double somme = 0;
        for (Repartir lr : lrs) {
            somme += (lr.getQuantite() * lr.getCoutAchat());
        }
        for (Imputer imputer : limp) {
            somme += imputer.getMontant();
        }
        return somme;
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
    private String NON_COMMENCE = "Non commencée", LANCEE = "Lancée", EN_COURS = "En cours...", TERMINEE = "Términée", SUSPENDUE = "Suspendue", ANNULEE = "Annulée", REPRISE = "Réprise", EN_ETUDE = "En étude", EN_TEST = "En test";

    private void loadData() {
        ols_intra_matiere.addAll(MatiereDelegate.findMatieres());
        ols_entr_deposer.addAll(DepotDelegate.findDepots());
        cbx_devise_intrapos.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_deviz_charg_aff_to_prod.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_type_intrant.setItems(FXCollections.observableArrayList("Matiere premiere", "Matiere consommable", "Produit semi fini"));
        cbx_type_depot.setItems(FXCollections.observableArrayList("Chambre froide", "Entrepot simple", "Cuve", "Silo", "Contenaire", "Contenaire-frigo", "Hangar", "Entrepot sechoire", "Citerne", "Terrain", "Autre"));
        cbx_sku_intrant.setItems(FXCollections.observableArrayList("Kg", "Sac", "Pcs", "Litre", "Metre cube", "Metre", "Km", "Gramme"));
        cbx_state_prod.setItems(FXCollections.observableArrayList(NON_COMMENCE, LANCEE, EN_COURS, TERMINEE, SUSPENDUE,
                ANNULEE, REPRISE, EN_ETUDE, EN_TEST));
        cbx_avancement_deprod.setItems(FXCollections.observableArrayList("Produit Fini", "Produit semi fini"));
        cbx_quality_to_prod.setItems(FXCollections.observableArrayList("Superieur", "Moyen", "Faible"));
        cbx_quality_deprod.setItems(cbx_quality_to_prod.getItems());
        tf_quant_sku_intrant.setText("1");
        ols_entr_livr.addAll(LivraisonDelegate.findLivraisons());
        ols_entreposer.addAll(EntreposerDelegate.findEntreposers()
                .stream().filter(i -> (i.getNiveauFabrication().equals(Constants.MANUFACTURING_LEVEL_RAW_MATERIAL)
                || i.getNiveauFabrication().equals(Constants.MANUFACTURING_LEVEL_MIDDLE_END_PRODUCT))).collect(Collectors.toList()));
        ols_entreposer_stock.addAll(EntreposerDelegate.findEntreposersGroupedByIntrant());
        ols_entreposer_pfini.setAll(EntreposerDelegate.findEntreposers()
                .stream().filter(i -> (i.getNiveauFabrication().equals(Constants.MANUFACTURING_LEVEL_MADE_PRODUCT)
                || i.getNiveauFabrication().equals(Constants.MANUFACTURING_LEVEL_MIDDLE_END_PRODUCT))).collect(Collectors.toList()));
        ols_productions.addAll(ProductionDelegate.findProductions());
        ols_productions_terminee.setAll(ols_productions.stream()
                .filter(e -> e.getEtat().equals(TERMINEE)).collect(Collectors.toList()));
        ols_depenses.addAll(DepenseDelegate.findDepenses());
        cbx_charge_indir.setItems(ols_depenses);
        ols_repartirs.addAll(RepartirDelegate.findRepartirs());
        ols_imputers.addAll(ImputerDelegate.findImputers());
        ols_imputeraff_indir.addAll(ImputerDelegate.findImputers());
        cbx_sku_intrant.getSelectionModel().selectFirst();
        cbx_region_depot.getSelectionModel().selectFirst();
        cbx_region_intrant.getSelectionModel().selectFirst();
        cbx_region_intraposage.getSelectionModel().selectFirst();
        cbx_region_deprod.getSelectionModel().selectFirst();
        cbx_type_depot.getSelectionModel().selectFirst();
        cbx_type_intrant.getSelectionModel().selectFirst();
        MainUI.setPattern(dpk_dateexp_intrapos);
        MainUI.setPattern(dpk_date_intraposage);
        MainUI.setPattern(dpk_start_period_entrepo);
        MainUI.setPattern(dpk_end_period_entrepo);
        MainUI.setPattern(dpk_start_prod);
        MainUI.setPattern(dpk_end_prod);
        MainUI.setPattern(dpk_expir_to_prod);
        MainUI.setPattern(dpk_now_prod);
        MainUI.setPattern(dpk_dujour_indir);
        MainUI.setPattern(dpk_start_indir);
        MainUI.setPattern(dpk_end_indir);
        dpk_date_intraposage.setValue(LocalDate.now());
        dpk_now_prod.setValue(LocalDate.now());
        dpk_dujour_indir.setValue(LocalDate.now());
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
    private void showIntradepotAnchorpane(MouseEvent event) {
        tabpn_intrant_depot.getSelectionModel().selectFirst();
        anchorpn_depot_intrant.setVisible(true);
    }

    @FXML
    private void updateEntreposage(MouseEvent event) {
        if (choosenEntreposer == null) {
            MainUI.notify(null, "", "Selectionnez un stockage puis reesayer", 5, "error");
            return;
        }
        if (tf_quantite_intrapos.getText().isEmpty()) {
            MainUI.notify(null, "", "La quantite est obligatoire", 5, "error");
            return;
        }
        if (cbx_depot_intrapos.getValue() == null) {
            MainUI.notify(null, "", "L'entrepot de stockage est obligatoire", 5, "error");
            return;
        }
        if (cbx_livraison_intraposage.getValue() == null) {
            MainUI.notify(null, "", "Les infos de la livraison doivent etre fourni de maniere exacte", 5, "error");
            return;
        }
        if (cbx_matiere_intraposage.getValue() == null) {
            MainUI.notify(null, "", "La matiere a stocker est obligatoire", 5, "error");
            return;
        }
        if (tf_coutach_intrapos.getText().isEmpty()) {
            MainUI.notify(null, "", "Le cout d'achat est obligatoire", 5, "error");
            return;
        }

        choosenEntreposer.setComment(txtarea_comment_intrapos.getText());
        choosenEntreposer.setCout(Double.parseDouble(tf_coutach_intrapos.getText()));
        choosenEntreposer.setDate(dpk_date_intraposage.getValue().atTime(LocalTime.now()));
        choosenEntreposer.setDepotId(cbx_depot_intrapos.getValue());
        choosenEntreposer.setDevise(cbx_devise_intrapos.getValue());
        choosenEntreposer.setExpiryDate(dpk_dateexp_intrapos.getValue());
        choosenEntreposer.setLivraisonId(cbx_livraison_intraposage.getValue());
        choosenEntreposer.setMatiereId(cbx_matiere_intraposage.getValue());
        choosenEntreposer.setNiveauFabrication(Constants.MANUFACTURING_LEVEL_RAW_MATERIAL);
        choosenEntreposer.setNumlot(tf_numlot_intrapos.getText());
        choosenEntreposer.setQuantite(Double.parseDouble(tf_quantite_intrapos.getText()));
        choosenEntreposer.setRegion(cbx_region_intraposage.getValue());
        choosenEntreposer.setSkuId(cbx_sku_intrapos.getValue());
        Entreposer saved = EntreposerDelegate.updateEntreposer(choosenEntreposer);
        if (saved != null) {
            ols_entreposer.set(ols_entreposer.indexOf(choosenEntreposer), saved);
            MainUI.notify(null, "", "L'entreposage modifie avec succes", 5, "info");
        }
    }

    @FXML
    private void deleteEntreposage(MouseEvent event) {
        if (choosenEntreposer == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "La selection est vide");
            alert.setTitle("Selectionez un element!");
            alert.setHeaderText(null);
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer cet entreposage selectionné", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            EntreposerDelegate.removeEntreposer(choosenEntreposer);
            MainUI.notify(null, "", "L'entreposage supprime avec succes", 5, "info");
        }
    }

    @FXML
    private void refreshEntreposage(MouseEvent event) {
        if (position.contains("Donnee")) {
            if (dpk_start_period_entrepo.getValue() != null && dpk_end_period_entrepo.getValue() != null) {
                ols_entreposer.clear();
                ols_entreposer.addAll(EntreposerDelegate.findEntreposerByLevel(
                        Constants.MANUFACTURING_LEVEL_RAW_MATERIAL));
            }
        } else if (position.contains("Stock")) {
            if (dpk_start_period_entrepo.getValue() != null && dpk_end_period_entrepo.getValue() != null) {
                ols_entreposer_stock.clear();
                ols_entreposer_stock.addAll(EntreposerDelegate.findEntreposersGroupedByIntrant());
            }
        }
    }

    @FXML
    private void saveEntreposage(ActionEvent event) {
        if (tf_quantite_intrapos.getText().isEmpty()) {
            MainUI.notify(null, "", "La quantite est obligatoire", 5, "error");
            return;
        }
        if (cbx_depot_intrapos.getValue() == null) {
            MainUI.notify(null, "", "L'entrepot de stockage est obligatoire", 5, "error");
            return;
        }
        if (cbx_livraison_intraposage.getValue() == null) {
            MainUI.notify(null, "", "Les infos de la livraison doivent etre fourni de maniere exacte", 5, "error");
            return;
        }
        if (cbx_matiere_intraposage.getValue() == null) {
            MainUI.notify(null, "", "La matiere a stocker est obligatoire", 5, "error");
            return;
        }
        if (tf_coutach_intrapos.getText().isEmpty()) {
            MainUI.notify(null, "", "Le cout d'achat est obligatoire", 5, "error");
            return;
        }
        Entreposer ent = new Entreposer(DataId.generate());
        ent.setComment(txtarea_comment_intrapos.getText());
        ent.setCout(Double.parseDouble(tf_coutach_intrapos.getText()));
        ent.setDate(dpk_date_intraposage.getValue().atTime(LocalTime.now()));
        ent.setDepotId(cbx_depot_intrapos.getValue());
        ent.setDevise(cbx_devise_intrapos.getValue());
        ent.setExpiryDate(dpk_dateexp_intrapos.getValue());
        ent.setLivraisonId(cbx_livraison_intraposage.getValue());
        ent.setMatiereId(cbx_matiere_intraposage.getValue());
        ent.setNiveauFabrication(Constants.MANUFACTURING_LEVEL_RAW_MATERIAL);
        ent.setNumlot(tf_numlot_intrapos.getText());
        ent.setQuantite(Double.parseDouble(tf_quantite_intrapos.getText()));
        ent.setRegion(cbx_region_intraposage.getValue());
        ent.setSkuId(cbx_sku_intrapos.getValue());
        Entreposer saved = EntreposerDelegate.saveEntreposer(ent);
        if (saved != null) {
            ols_entreposer.add(saved);
            MainUI.notify(null, "", "La matiere a ete entreposee avec succes", 5, "info");
        }

    }

    @FXML
    private void manageLivraison(MouseEvent event) {
        MainUI.floatDialog(tools.Constants.DELIVERY_DLG, 600, 468, null, kazisafe, entreprise, null, Constants.PRODUCTION);
    }

    @FXML
    private void showIntradepotAnchorpane2(MouseEvent event) {
        tabpn_intrant_depot.getSelectionModel().selectLast();
        anchorpn_depot_intrant.setVisible(true);
    }

    @FXML
    private void ajouterSku(ActionEvent event) {
        perfom(event);
    }

    @FXML
    private void updateIntrant(MouseEvent event) {
        if (choosenMatiere == null) {
            MainUI.notify(null, "", "Selectionnez une matiere puis reesayer", 5, "error");
            return;
        }
        if (!tf_nom_intrant.getText().isEmpty()) {
            choosenMatiere.setMatiereName(tf_nom_intrant.getText());
        }
        if (!cbx_type_intrant.getValue().isEmpty()) {
            choosenMatiere.setTypeMatiere(cbx_type_intrant.getValue());
        }
        choosenMatiere.setRegion(cbx_region_intrant.getValue());
        choosenMatiere.setPerissable(chbx_perissable_intrant.isSelected());
        Matiere updated = MatiereDelegate.updateMatiere(choosenMatiere);
        if (updated != null) {
            ols_intra_matiere.set(ols_intra_matiere.indexOf(choosenMatiere), updated);
            MainUI.notify(null, "", "Intrant modifie avec success", 5, "info");
        }

    }

    @FXML
    private void deleteIntrant(MouseEvent event) {
        if (choosenMatiere == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "La selection est vide");
            alert.setTitle("Selectionez un element!");
            alert.setHeaderText(null);
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer l'intrant selectionné", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            MatiereDelegate.removeMatiere(choosenMatiere);
            MainUI.notify(null, "", "L'intrant a ete supprime avec succes", 5, "info");
        }
    }

    @FXML
    private void saveIntrant(ActionEvent event) {
        if (tf_nom_intrant.getText().isEmpty()) {
            MainUI.notify(null, "", "Le Nom de l'intrant est obligatoire", 5, "error");
            return;
        }
        if (cbx_type_intrant.getValue().isEmpty()) {
            MainUI.notify(null, "", "Le type d'intrant est obligatoire", 5, "error");
            return;
        }
        if (ols_newsku_entr.isEmpty()) {
            MainUI.notify(null, "", "Les unites de stockage sont obligatoire, remplissez la liste dediee, puis reesayer", 5, "error");
            return;
        }
        String uid = DataId.generate();
        Matiere m = new Matiere(uid);
        m.setMatiereName(tf_nom_intrant.getText());
        m.setPerissable(chbx_perissable_intrant.isSelected());
        m.setRegion(cbx_region_intrant.getValue());
        m.setTypeMatiere(cbx_type_intrant.getValue());
        Matiere ms = MatiereDelegate.saveMatiere(m);
        if (ms != null) {
            for (MatiereSku msku : ols_newsku_entr) {
                msku.setMatiere(ms);
                msku.setRegion(ms.getRegion());
                MatiereSkuDelegate.saveMatiereSku(msku);
            }
            if ("Produit semi fini".equals(cbx_type_intrant.getValue())) {
                Produit p = new Produit(uid);
                p.setCodebar(((int) (Math.random() * 1000000)) + "");
                p.setCategoryId(CategoryDelegate.findCategories("Divers").get(0));
                p.setDateCreation(LocalDateTime.now());
                p.setMarque(entreprise.getNomEntreprise());
                p.setMethodeInventaire("fifo");
                p.setModele(ms.getTypeMatiere());
                p.setNomProduit(ms.getMatiereName());
                Produit sp = ProduitDelegate.saveProduit(p);
                if (sp != null) {
                    for (MatiereSku msku : ols_newsku_entr) {
                        Mesure mz = new Mesure(msku.getUid());
                        mz.setProduitId(sp);
                        mz.setDescription(msku.getNomSku());
                        mz.setQuantContenu(msku.getQuantContenuSku());
                        MesureDelegate.saveMesure(mz);
                    }
                }
            }
            MainUI.notify(null, "", "Matiere enregistree avec success", 5, "info");
            ols_intra_matiere.add(ms);
            ols_newsku_entr.clear();
        }

    }

    @FXML
    private void updateSku(Event e) {
        if (cbx_sku_intrant.getValue() == null) {
            MainUI.notify(null, "", "Le champ nom de l'unite de stockage est obligatoire", 5, "error");
            return;
        }
        if (tf_quant_sku_intrant.getText().isEmpty()) {
            MainUI.notify(null, "", "Le champ de la quantite de l'unite de stockage est obligatoire", 5, "error");
            return;
        }
        choosenSku.setNomSku(cbx_sku_intrant.getValue());
        choosenSku.setQuantContenuSku(Double.parseDouble(tf_quant_sku_intrant.getText()));
        MatiereSku updated = MatiereSkuDelegate.updateMatiereSku(choosenSku);
        if (updated != null) {
            MainUI.notify(null, "", "SKU de la matiere modifiee avec success", 5, "info");
        }
    }

    @FXML
    private void saveDepot(ActionEvent event) {
        if (tf_nom_depot.getText().isEmpty()) {
            MainUI.notify(null, "", "Le champ nom du depot est obligatoire", 5, "error");
            return;
        }
        if (cbx_type_depot.getValue().isEmpty()) {
            MainUI.notify(null, "", "Le champ type du depot est obligatoire", 5, "error");
            return;
        }
        Depot d = new Depot(DataId.generate());
        d.setNomDepot(tf_nom_depot.getText());
        d.setTypeDepot(cbx_type_depot.getValue());
        d.setRegion(cbx_region_depot.getValue());
        if (!tf_dimens_depot.getText().isEmpty()) {
            d.setDimension(tf_dimens_depot.getText());
        }
        Depot rst = DepotDelegate.saveDepot(d);
        if (rst != null) {
            MainUI.notify(null, "", "Depot enregistre avec success", 5, "info");
            ols_entr_deposer.add(rst);
        }
    }

    @FXML
    private void updateDepot(MouseEvent event) {
        if (tf_nom_depot.getText().isEmpty()) {
            MainUI.notify(null, "", "Le champ nom du depot est obligatoire", 5, "error");
            return;
        }
        if (cbx_type_depot.getValue().isEmpty()) {
            MainUI.notify(null, "", "Le champ type du depot est obligatoire", 5, "error");
            return;
        }
        if (choosenDepot == null) {
            MainUI.notify(null, "", "Selectionnez d'abord un depot", 5, "error");
            return;
        }

        choosenDepot.setNomDepot(tf_nom_depot.getText());
        choosenDepot.setTypeDepot(cbx_type_depot.getValue());
        if (!tf_dimens_depot.getText().isEmpty()) {
            choosenDepot.setDimension(tf_dimens_depot.getText());
        }
        Depot rst = DepotDelegate.updateDepot(choosenDepot);
        if (rst != null) {
            ols_entr_deposer.set(ols_entr_deposer.indexOf(choosenDepot), rst);
            MainUI.notify(null, "", "Depot modifie avec success", 5, "info");
        }
    }

    @FXML
    private void deleteDepot(MouseEvent event) {
        if (choosenDepot == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "La selection est vide");
            alert.setTitle("Selectionez un element!");
            alert.setHeaderText(null);
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer le depot selectionné", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            DepotDelegate.removeDepot(choosenDepot);
            MainUI.notify(null, "", "Depot supprime avec success", 5, "info");
        }
    }

    @FXML
    private void closeIntranFloatingForm(ActionEvent event) {
        closeFloatingPane(event);
    }

    @FXML
    private void sortByDate(Event e) {
        if (position.contains("Donnee")) {
            if (dpk_start_period_entrepo.getValue() != null && dpk_end_period_entrepo.getValue() != null) {
                ols_entreposer.clear();
                ols_entreposer.addAll(EntreposerDelegate.findEntreposerByLevel(
                        dpk_start_period_entrepo.getValue(),
                        dpk_end_period_entrepo.getValue(),
                        Constants.MANUFACTURING_LEVEL_RAW_MATERIAL));
            }

        } else if (position.contains("Stock")) {
            if (dpk_start_period_entrepo.getValue() != null && dpk_end_period_entrepo.getValue() != null) {
                ols_entreposer_stock.clear();
                ols_entreposer_stock.addAll(EntreposerDelegate.findEntreposersGroupedByIntrant(
                        dpk_start_period_entrepo.getValue(),
                        dpk_end_period_entrepo.getValue()));
            }
        }
    }

    public void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        Parent p2 = p.getParent();
        Parent p3 = p2.getParent();
        p3.setVisible(false);
    }

    private void initCombos() {

        cbx_sku_intrapos.setConverter(new StringConverter<MatiereSku>() {
            @Override
            public String toString(MatiereSku object) {
                return object == null ? null : object.getNomSku();
            }

            @Override
            public MatiereSku fromString(String string) {
                return cbx_sku_intrapos.getItems()
                        .stream()
                        .filter(f -> (f.getNomSku())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_sku_matiere_to_prod.setConverter(new StringConverter<MatiereSku>() {
            @Override
            public String toString(MatiereSku object) {
                return object == null ? null : object.getNomSku();
            }

            @Override
            public MatiereSku fromString(String string) {
                return cbx_sku_intrapos.getItems()
                        .stream()
                        .filter(f -> (f.getNomSku())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_depot_intrapos.setConverter(new StringConverter<Depot>() {
            @Override
            public String toString(Depot object) {
                return object == null ? null : object.getNomDepot() + " - " + object.getTypeDepot() + " " + object.getRegion();
            }

            @Override
            public Depot fromString(String string) {
                return cbx_depot_intrapos.getItems()
                        .stream()
                        .filter(object -> (object.getNomDepot() + " - " + object.getTypeDepot() + " " + object.getRegion())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_depot_deprod.setConverter(new StringConverter<Depot>() {
            @Override
            public String toString(Depot object) {
                return object == null ? null : object.getNomDepot() + " - " + object.getTypeDepot() + " " + object.getRegion();
            }

            @Override
            public Depot fromString(String string) {
                return cbx_depot_intrapos.getItems()
                        .stream()
                        .filter(object -> (object.getNomDepot() + " - " + object.getTypeDepot() + " " + object.getRegion())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_matiere_intraposage.setConverter(new StringConverter<Matiere>() {
            @Override
            public String toString(Matiere object) {
                return object == null ? null : object.getMatiereName() + " - " + object.getTypeMatiere();
            }

            @Override
            public Matiere fromString(String string) {
                return cbx_matiere_intraposage.getItems()
                        .stream()
                        .filter(f -> (f.getMatiereName() + " - " + f.getTypeMatiere())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_matiere_to_prod.setConverter(new StringConverter<Matiere>() {
            @Override
            public String toString(Matiere object) {
                return object == null ? null : object.getMatiereName() + " " + object.getTypeMatiere();
            }

            @Override
            public Matiere fromString(String string) {

                return cbx_matiere_to_prod.getItems()
                        .stream()
                        .filter(f -> (f.getMatiereName() + " " + f.getTypeMatiere())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_mesure_to_prod.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure_to_prod.getItems()
                        .stream()
                        .filter(f -> f.getDescription()
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_mesure_deprod.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure_deprod.getItems()
                        .stream()
                        .filter(f -> f.getDescription()
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_produits_prod.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null : object.getNomProduit() + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_produits_prod.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + object.getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_charge_aff_to_prod.setConverter(new StringConverter<Operation>() {
            @Override
            public String toString(Operation object) {
                if (object == null) {
                    return null;
                }
                Depense d = DepenseDelegate.findDepense(object.getDepenseId().getUid());
                return d.getNomDepense() + "-" + object.getReferenceOp() + " " + object.getLibelle();
            }

            @Override
            public Operation fromString(String string) {
                return cbx_charge_aff_to_prod.getItems()
                        .stream()
                        .filter(object -> (object.getDepenseId().getNomDepense() + "-" + object.getReferenceOp() + " " + object.getLibelle())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_charge_indir.setConverter(new StringConverter<Depense>() {
            @Override
            public String toString(Depense object) {
                if (object == null) {
                    return null;
                }
                return object.getNomDepense() + (object.getFrequence() == null ? "" : "-" + object.getFrequence());
            }

            @Override
            public Depense fromString(String string) {
                return cbx_charge_indir.getItems()
                        .stream()
                        .filter(object -> (object.getNomDepense() + (object.getFrequence() == null ? "" : "-" + object.getFrequence()))
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_livraison_intraposage.setConverter(new StringConverter<Livraison>() {
            @Override
            public String toString(Livraison object) {
                return object == null ? null : object.getFournId().getNomFourn() + ", " + object.getNumPiece() + " " + object.getDateLivr().toString();
            }

            @Override
            public Livraison fromString(String string) {
                return cbx_livraison_intraposage.getItems()
                        .stream()
                        .filter(f -> (f.getFournId().getNomFourn() + ", " + f.getNumPiece() + " " + f.getDateLivr().toString())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_production_terminees.setConverter(new StringConverter<Production>() {
            @Override
            public String toString(Production object) {
                Produit pd = object.getProduitId();
                return pd == null ? null : pd.getNomProduit() + " " + pd.getModele() + " "
                        + "" + pd.getTaille() + " lot : " + object.getNumlot() + " " + object.getEtat();
            }

            @Override
            public Production fromString(String string) {
                return ols_productions
                        .stream()
                        .filter(pd -> (pd.getProduitId().getNomProduit() + " " + pd.getProduitId().getModele() + " "
                        + "" + pd.getProduitId().getTaille() + " lot : " + pd.getNumlot() + " " + pd.getEtat())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

    }

    private void initTables() {
        col_date_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer liv = param.getValue();
            String dt = null;
            if (liv != null) {
                LocalDateTime dat = liv.getDate();
                if (dat != null) {
                    dt = dat.toString();
                }
            }
            return new SimpleStringProperty(dt == null ? "-" : dt);
        });
        col_livraison_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> new SimpleStringProperty(param.getValue().getLivraisonId() == null ? ""
                : param.getValue().getLivraisonId().getNumPiece()));
        col_matiere_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            String nom;
            if (l.getMatiereId() != null) {
                Matiere p = MatiereDelegate.findMatiere(l.getMatiereId().getUid());
                nom = p.getMatiereName();
            } else {
                Produit p = l.getProductionId().getProduitId();
                nom = p.getNomProduit() + " " + p.getModele();
            }
            return new SimpleStringProperty(nom);
        });
        col_lot_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getNumlot());
        });
        col_quant_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Entreposer l = param.getValue();
            MatiereSku fss = l.getSkuId();
            double qt = l.getQuantite();
            return new SimpleStringProperty(qt + " " + (fss == null ? "" : fss.getNomSku()));
        });
        col_region_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getRegion());
        });
        col_cout_unitr_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Double t = param.getValue().getCout();
            String dvs = param.getValue().getDevise();
            return new SimpleStringProperty(t + " " + dvs);
        });
        col_total_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, Number> param) -> {
            Entreposer s = param.getValue();
            return new SimpleDoubleProperty(s.getCout() * s.getQuantite());
        });
        col_peremption_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer lst = param.getValue();//strongDb.findStockerByLivr(param.getValue().getUid());
            String dat;
            if (lst.getExpiryDate() == null) {
                dat = bundle.getString("noperish");
            } else {
                dat = lst.getExpiryDate().toString();
            }
            return new SimpleStringProperty(dat);
        });
        col_type_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer lst = param.getValue();//strongDb.findStockerByLivr(param.getValue().getUid());
            String nom;
            if (lst.getMatiereId() != null) {
                Matiere p = MatiereDelegate.findMatiere(lst.getMatiereId().getUid());
                nom = p.getTypeMatiere();
            } else {
                nom = "Produit semi fini";
            }
            return new SimpleStringProperty(nom);
        });
        col_depot_intrapos.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer lst = param.getValue();//strongDb.findStockerByLivr(param.getValue().getUid());
            Depot dep = DepotDelegate.findDepot(lst.getDepotId().getUid());
            return new SimpleStringProperty(dep.getNomDepot() + "-" + dep.getTypeDepot());
        });

        col_nom_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            Matiere p = MatiereDelegate.findMatiere(l.getMatiereId().getUid());
            return new SimpleStringProperty(p.getMatiereName() + " " + p.getTypeMatiere());
        });
        col_lot_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = findOngoingEntreposer(param.getValue().getMatiereId().getUid());
            return new SimpleStringProperty(l.getNumlot());
        });
        col_entree_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //somme des entree dans le cadre d'inventaire
            Entreposer l = param.getValue();
            MatiereSku fss = l.getSkuId();
            double entree = EntreposerDelegate.findSommeEntree(l.getMatiereId().getUid());
            double qt = BigDecimal.valueOf(entree / fss.getQuantContenuSku()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();

            return new SimpleStringProperty((qt) + " " + fss.getNomSku());
        });
        col_sortie_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //somme sortie dans le cadre d'inventaire
            Entreposer l = param.getValue();
            MatiereSku fss = l.getSkuId();
            double sortie = RepartirDelegate.findSommeRepartir(l.getMatiereId().getUid());
            double qt = BigDecimal.valueOf(sortie / fss.getQuantContenuSku()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleStringProperty(qt + " " + fss.getNomSku());
        });
        col_stock_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //stock restant= sommeEntree-sommesSortie
            Entreposer l = param.getValue();
            MatiereSku fss = l.getSkuId();
            double qt = stockFinalMatierePremiere(fss, l.getMatiereId());
            return new SimpleStringProperty(qt + " " + fss.getNomSku());
        });
        col_region_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getRegion());
        });
        col_cout_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //cout de l'entreposage encours de prelevement
            Entreposer e = param.getValue();
            Entreposer ongoing = findOngoingEntreposer(e.getMatiereId().getUid());
            Double t = ongoing.getCout();
            String dvs = ongoing.getDevise();
            return new SimpleStringProperty(t + " " + dvs);
        });
        col_total_value_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, Number> param) -> {
            Entreposer e = param.getValue();
            Entreposer ongoing = findOngoingEntreposer(e.getMatiereId().getUid());
            Double stock = stockFinalMatierePremiere(e.getSkuId(), e.getMatiereId());
            Double t = ongoing.getCout() * stock;
            return new SimpleDoubleProperty(t);
        });
        col_peremption_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer e = param.getValue();//strongDb.findStockerByLivr(param.getValue().getUid());
            Entreposer lst = findOngoingEntreposer(e.getMatiereId().getUid());
            String dat;
            if (lst.getExpiryDate() == null) {
                dat = bundle.getString("noperish");
            } else {
                dat = lst.getExpiryDate().toString();
            }
            return new SimpleStringProperty(dat);
        });
        col_type_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer lst = param.getValue();//strongDb.findStockerByLivr(param.getValue().getUid());
            Matiere p = MatiereDelegate.findMatiere(lst.getMatiereId().getUid());
            return new SimpleStringProperty(p.getTypeMatiere());
        });
        col_depot_intrant_stock.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer lst = param.getValue();
            Depot dep = DepotDelegate.findDepot(lst.getDepotId().getUid());
            return new SimpleStringProperty(dep.getTypeDepot() + "/" + dep.getNomDepot());
        });

        col_nom_depot.setCellValueFactory((TableColumn.CellDataFeatures<Depot, String> param) -> {
            Depot lst = param.getValue();
            return new SimpleStringProperty(lst.getNomDepot());
        });
        col_dimension_depot.setCellValueFactory((TableColumn.CellDataFeatures<Depot, String> param) -> {
            Depot lst = param.getValue();
            return new SimpleStringProperty(lst.getDimension());
        });
        col_region_depot.setCellValueFactory((TableColumn.CellDataFeatures<Depot, String> param) -> {
            Depot lst = param.getValue();
            return new SimpleStringProperty(lst.getRegion());
        });
        col_type_depot.setCellValueFactory((TableColumn.CellDataFeatures<Depot, String> param) -> {
            Depot lst = param.getValue();
            return new SimpleStringProperty(lst.getTypeDepot());
        });

        col_designation_intrant.setCellValueFactory((TableColumn.CellDataFeatures<Matiere, String> param) -> {
            Matiere lst = param.getValue();
            return new SimpleStringProperty(lst.getMatiereName());
        });
        col_type_intrant.setCellValueFactory((TableColumn.CellDataFeatures<Matiere, String> param) -> {
            Matiere lst = param.getValue();
            return new SimpleStringProperty(lst.getTypeMatiere());
        });
        col_region_intrant.setCellValueFactory((TableColumn.CellDataFeatures<Matiere, String> param) -> {
            Matiere lst = param.getValue();
            return new SimpleStringProperty(lst.getRegion());
        });
        col_perissable_intrant.setCellValueFactory((TableColumn.CellDataFeatures<Matiere, String> param) -> {
            Matiere lst = param.getValue();
            return new SimpleStringProperty(lst.getPerissable() ? "Perissable" : "Non perissable");
        });
        col_lessku_intrant.setCellValueFactory((TableColumn.CellDataFeatures<Matiere, String> param) -> {
            Matiere lst = param.getValue();
            List<MatiereSku> skus = MatiereSkuDelegate.findMatiereSkuFor(lst.getUid());
            return new SimpleStringProperty(skus.isEmpty() ? "Pas de SKU" : "Mesure (" + skus.size() + ")");
        });

        tabpn_entreposer_in_stock.getSelectionModel()
                .selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab t, Tab t1) -> {
                    position = t1.getText();
                    if (position.contains("Donnee")) {

                        DoubleBinding dbg = Bindings.createDoubleBinding(() -> {
                            double stock1 = EntreposerDelegate.sumValueStockMP();
                            return stock1;
                        }, ols_entreposer_stock);
                        Platform.runLater(() -> {
                            txt_count_intraprod.textProperty()
                                    .bind(Bindings.size(tbl_entreposage.getItems()).asString("%d entreposages"));
                            txt_valeur_global_intraprod.textProperty().bind(dbg.asString(" Entrees : %.2fUSD"));
                        });
                    } else if (position.contains("Stock")) {
                        DoubleBinding dbg = Bindings.createDoubleBinding(() -> {
                            double stock1 = 0;
                            for (Entreposer ent : ols_entreposer_stock) {
                                Entreposer wpc = findOngoingEntreposer(ent.getMatiereId().getUid());
                                double sf1 = stockFinalMatierePremiere(ent.getSkuId(), wpc.getMatiereId());
                                stock1 += (sf1 * wpc.getCout());
                            }
                            return stock1;
                        }, ols_entreposer_stock);
                        Platform.runLater(() -> {
                            txt_count_intraprod.textProperty()
                                    .bind(Bindings.size(tbl_stock_entreposage.getItems()).asString("%d Stocks"));
                            txt_valeur_global_intraprod.textProperty().bind(dbg.asString("Valeur total du stock : %.2fUSD"));
                        });
                    }
                });

        col_date_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            return new SimpleStringProperty(lst.getDate().toString());
        });
        col_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            Matiere mat = MatiereDelegate.findMatiere(lst.getMatiereId().getUid());
            return new SimpleStringProperty(mat.getMatiereName());
        });
        col_type_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            Matiere mat = MatiereDelegate.findMatiere(lst.getMatiereId().getUid());
            return new SimpleStringProperty(mat.getTypeMatiere());
        });
        col_lot_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            return new SimpleStringProperty(lst.getNumlot());
        });
        col_expir_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            Entreposer entr = findOngoingEntreposer(lst.getMatiereId().getUid());
            return new SimpleStringProperty(entr.getExpiryDate() == null ? bundle.getString("noperish") : entr.getExpiryDate().toString());
        });
        col_qte_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            double qt = lst.getQuantite();
            MatiereSku ms = lst.getSkuId();
            return new SimpleStringProperty(qt + " " + ms.getNomSku());
        });
        col_cout_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            return new SimpleStringProperty(lst.getCoutAchat() + " " + lst.getDevise());
        });
        col_tot_mat_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Repartir, String> param) -> {
            Repartir lst = param.getValue();
            double qt = lst.getQuantite();
            double cu = lst.getCoutAchat();
            return new SimpleStringProperty((qt * cu) + " " + lst.getDevise());
        });

        col_depense_charg_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            Imputer lst = param.getValue();
            Operation op = OperationDelegate.findOperation(lst.getOperationId().getUid());
            Depense dep = DepenseDelegate.findDepense(op.getDepenseId().getUid());
            return new SimpleStringProperty(dep.getNomDepense());
        });
        col_sumquot_charg_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            Imputer lst = param.getValue();
            return new SimpleStringProperty(lst.getMontant() + " " + lst.getDevise());
        });
        col_date_charg_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            Imputer lst = param.getValue();
            return new SimpleStringProperty(lst.getDate().toString());
        });
        col_percent_charg_aff_to_prod.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            Imputer lst = param.getValue();
            return new SimpleStringProperty(lst.getPercent() + "%");
        });

        col_produit_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            Produit p = ProduitDelegate.findProduit(lst.getProduitId().getUid());
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getModele());
        });
        col_lot_produit_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getNumlot());
        });
        col_exp_produit_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getDatePeremption() == null ? "Non disponible" : lst.getDatePeremption().toString());
        });
        col_date_start_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getDateDebut().toString());
        });
        col_date_end_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getDateFin().toString());
        });
        col_quantite_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            double qt = lst.getQuantitePrevu();
            Mesure mx = lst.getMesureId();
            if (mx == null) {
                return new SimpleStringProperty(qt + "");
            }
            Mesure m = MesureDelegate.findMesure(mx.getUid());
            return new SimpleStringProperty(qt + " " + m.getDescription());
        });
        col_qualite_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getQualitePrevu());
        });
        col_etat_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getEtat());
        });
        col_region_prod.setCellValueFactory((TableColumn.CellDataFeatures<Production, String> param) -> {
            Production lst = param.getValue();
            return new SimpleStringProperty(lst.getRegion());
        });
        col_coutotal_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Entreposer l = param.getValue();
            return new SimpleStringProperty((l.getCout() * l.getQuantite()) + " USD");
        });
        col_date_prodeposee.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getDate().toString());
        });
        col_prod8_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            Produit p = l.getProductionId().getProduitId();
            return new SimpleStringProperty(p.getNomProduit() + "" + p.getModele() + "" + (p.getTaille() == null ? "" : p.getTaille()));
        });
        cout_unit_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getCout() + " USD");
        });
        col_qte_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            Mesure m = l.getMesureId();
            return new SimpleStringProperty(l.getQuantite() + " " + m.getDescription());
        });
        col_debut_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getProductionId().getDateDebut().toString());
        });
        col_fin_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getProductionId().getDateFin().toString());
        });
        col_etat_deposee.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getNiveauFabrication());
        });
        col_lot_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getNumlot());
        });
        col_expir_deposee.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getExpiryDate() == null ? "" : l.getExpiryDate().toString());
        });
        col_depot_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getDepotId().getNomDepot());
        });
        col_region_deposees.setCellValueFactory((TableColumn.CellDataFeatures<Entreposer, String> param) -> {
            Entreposer l = param.getValue();
            return new SimpleStringProperty(l.getRegion());
        });
        cbx_charge_aff_to_prod.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Operation> ov, Operation t, Operation t1) -> {
                    if (t1 != null) {
                        txt_sum_charg_aff_to_prod.setText((t1.getMontantUsd() + (t1.getMontantCdf() / taux)) + "USD");
                    }
                });

    }

    private void addMatiereSku(String matsku) {
        ContextMenu contM = new ContextMenu();
        MenuItem mi = new MenuItem("Supprimer");
        contM.getItems().add(mi);
        Label l = new Label();
        l.setPrefWidth(64);
        l.setPrefHeight(16);
        l.setTextAlignment(TextAlignment.CENTER);
        l.setTextFill(Color.rgb(255, 255, 255));
        l.setBackground(new Background(new BackgroundFill(Color.rgb(0x7, 0x7, 0xf, 0.3), new CornerRadii(5.0), new Insets(-5.0))));
        l.setPadding(new Insets(4, 4, 4, 4));
        l.setText(matsku);
        l.setTooltip(new Tooltip(matsku));
        l.setContextMenu(contM);

        if (isMesureExist(tilepn_list_sku_intrant.getChildren(), matsku)) {
            MainUI.notify(null, "Erreur", "Le SKU ayant ces infos fournies existe deja", 4, "error");
            return;
        }
        double qq = Double.parseDouble(matsku.split(":")[1]);
        if (isMesureExist(tilepn_list_sku_intrant.getChildren(), qq)) {
            MainUI.notify(null, "Erreur", "Le SKU ayant cet info fournies existe deja", 4, "error");
            return;
        }

        tilepn_list_sku_intrant.getChildren().add(l);
        MatiereSku msk = new MatiereSku(DataId.generate());
        msk.setNomSku(matsku.split(":")[0]);
        msk.setQuantContenuSku(qq);
        msk.setRegion(cbx_region_intrant.getValue());
        ols_newsku_entr.add(msk);
        cbx_sku_intrant.getSelectionModel().select("");
        tf_quant_sku_intrant.clear();
        mi.setOnAction((ActionEvent event) -> {
            Label lab = l;
            String txt = lab.getText();
            if (choosenMatiere != null) {
                if (!txt.contains(":")) {
                    choosenSku = MatiereSkuDelegate.findMatiereSku(txt, choosenMatiere.getUid());
                    cbx_sku_intrant.setValue(txt);
                } else {
                    double q = Double.parseDouble(txt.split(":")[1]);
                    String name = txt.split(":")[0];
                    choosenSku = MatiereSkuDelegate.findMatiereSku(name, q, choosenMatiere.getUid());
                    cbx_sku_intrant.setValue(name);
                    tf_quant_sku_intrant.setText(String.valueOf(q));
                }
                if (choosenSku != null) {
                    MatiereSkuDelegate.removeMatiereSku(choosenSku);
                }
            }
            tilepn_list_sku_intrant.getChildren().remove(l);
        });
        l.setOnMouseClicked((MouseEvent t) -> {
            Node dx = (Node) t.getSource();
            Label lab = (Label) dx;
            String txt = lab.getText();
            if (choosenMatiere != null) {
                if (!txt.contains(":")) {
                    choosenSku = MatiereSkuDelegate.findMatiereSku(txt, choosenMatiere.getUid());
                    cbx_sku_intrant.setValue(txt);
                } else {
                    double q = Double.parseDouble(txt.split(":")[1]);
                    int lastIndex = txt.lastIndexOf(":");
                    String name = txt.substring(0, lastIndex);
                    choosenSku = MatiereSkuDelegate.findMatiereSku(name, q, choosenMatiere.getUid());
                    cbx_sku_intrant.setValue(name);
                    tf_quant_sku_intrant.setText(String.valueOf(q));
                }
            }
        });
    }

    private void perfom(Event e) {
        try {
            if (cbx_sku_intrant.getValue().isEmpty() || tf_quant_sku_intrant.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "Veuillez remplir les deux champs pour le SKU , puis continuer", 4, "error");
                return;
            }
            String mes = cbx_sku_intrant.getValue() + ":" + tf_quant_sku_intrant.getText();
            addMatiereSku(mes);
        } catch (NumberFormatException ex) {
            MainUI.notify(null, "Erreur", "Entrer la quantite valide (des chiffres) SVP", 4, "error");
        }
    }

    private boolean isMesureExist(List<Node> lmz, String decPlusCount) {
        for (Node n : lmz) {
            Label mz = (Label) n;
            String pc = mz.getText().split(":")[0];
            if (pc.equalsIgnoreCase(decPlusCount.split(":")[0])) {
                return true;
            }
        }
        return false;
    }

    private boolean isMesureExist(List<Node> lmz, double q) {
        for (Node n : lmz) {
            Label mz = (Label) n;
            String qt = mz.getText().split(":")[1];
            double quant = Double.parseDouble(qt);
            if (quant == q) {
                return true;
            }
        }
        return false;
    }

    public void search(String query) {
        if (position.contains("Donnee")) {

        } else if (position.contains("Stock")) {
        }
    }

    private Entreposer findOngoingEntreposer(String matiereId) {
        String meth = pref.get("meth", "fifo");
        List<Entreposer> entreps = null;
        if (meth.equals("ppps")) {
            entreps = EntreposerDelegate.toFefoOrdering(matiereId);
        } else if (meth.equals("fifo")) {
            entreps = EntreposerDelegate.toFifoOrdering(matiereId);
        } else if (meth.equals("lifo")) {
            entreps = EntreposerDelegate.toLifoOrdering(matiereId);
        }
        return entreps == null || entreps.isEmpty() ? null : entreps.get(0);
    }

    private Entreposer findOngoingProdEntreposer(String prod) {
        String meth = pref.get("meth", "fifo");
        List<Entreposer> entreps = null;
        if (meth.equals("ppps")) {
            entreps = EntreposerDelegate.toFefoOrderingProd(prod);
        } else if (meth.equals("fifo")) {
            entreps = EntreposerDelegate.toFifoOrderingProd(prod);
        } else if (meth.equals("lifo")) {
            entreps = EntreposerDelegate.toLifoOrderingProd(prod);
        }
        return entreps == null || entreps.isEmpty() ? null : entreps.get(0);
    }

    private double stockFinalMatierePremiere(MatiereSku fss, Matiere l) {
        double entree = EntreposerDelegate.findSommeEntree(l.getUid());
        double sortie = RepartirDelegate.findSommeRepartir(l.getUid());
        double qte = BigDecimal.valueOf(entree / fss.getQuantContenuSku()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        double qts = BigDecimal.valueOf(sortie / fss.getQuantContenuSku()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        double qt = qte - qts;
        return qt;
    }

    private List<Operation> searchOnDate(Depense dep, LocalDate date1, LocalDate date2) {
        List<Operation> ops = null;
        if (date1 != null && date2 != null) {
            ops = OperationDelegate.findByDateInterval(dep, date1, date2);
        }
        return ops;
    }

    @FXML
    private void addNewRepartir(ActionEvent event) {
        if (tf_qte_reparti_matiere_prod.getText().isEmpty()) {
            MainUI.notify(null, "INFO", "Erreur : La quantite de la matiere a incorporer est obligatoire", 6, "error");
            return;
        }
        if (cbx_matiere_to_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "Erreur : La matiere premiere est obligatoire", 6, "error");
            return;
        }
        if (cbx_sku_matiere_to_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "Erreur : L'unite de stockage de la matiere premiere est obligatoire", 6, "error");
            return;
        }
        if (choosenProduction == null) {
            MainUI.notify(null, "INFO", "Erreur : Selectionnez une production puis reessayer", 6, "error");
            return;
        }

        Repartir r = new Repartir(DataId.generate());
        r.setCoutAchat(currentChoosenEntreposer.getCout());
        r.setDate(LocalDateTime.now());
        r.setDevise(currentChoosenEntreposer.getDevise());
        Matiere mp = cbx_matiere_to_prod.getValue();
        r.setMatiereId(mp);
        r.setNumlot(currentChoosenEntreposer.getNumlot());
        r.setProductionId(choosenProduction);
        r.setQuantite(Double.valueOf(tf_qte_reparti_matiere_prod.getText()));
        r.setRegion(cbx_region_prod.getValue());
        r.setSkuId(cbx_sku_matiere_to_prod.getValue());
        Repartir rp = RepartirDelegate.saveRepartir(r);
        if (rp != null) {
            ols_repartirs.add(rp);
            showValue(choosenProduction);
            MainUI.notify(null, "INFO", "La quanite de la matiere premiere a ete bien repartie", 5, "info");
        }
    }

    @FXML
    private void addNewImputer(ActionEvent event) {
        if (tf_quot_charg_aff_to_prod.getText().isEmpty()) {
            MainUI.notify(null, "INFO", "Erreur : Le quote part de charge pour cette production est obligatoire", 6, "info");
            return;
        }
        if (cbx_charge_aff_to_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "Erreur : La charge a incorporer est obligatoire", 6, "info");
            return;
        }
        if (choosenProduction == null) {
            MainUI.notify(null, "INFO", "Erreur : Veuillez selectionner une production", 6, "info");
            return;
        }
        Operation op = cbx_charge_aff_to_prod.getValue();
        double m = Double.parseDouble(tf_quot_charg_aff_to_prod.getText());
        double totch = op.getMontantUsd() + (op.getMontantCdf() / taux);
        Imputer imp = new Imputer(DataId.generate());
        imp.setDate(LocalDate.now());
        imp.setDevise(cbx_deviz_charg_aff_to_prod.getValue());
        imp.setMontant(m);
        imp.setOperationId(op);
        imp.setPercent(BigDecimal.valueOf((m / totch) * 100).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        imp.setProductionId(choosenProduction);

        imp.setRegion(cbx_region_prod.getValue());
        Imputer i = ImputerDelegate.saveImputer(imp);
        if (i != null) {
            ols_imputers.add(i);
            showValue(choosenProduction);
            MainUI.notify(null, "INFO", "La charge a ete bien repartie, sur cette production", 5, "info");
        }
    }

    @FXML
    private void saveProduction(ActionEvent event) {
        if (cbx_produits_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "Erreur : Le produit a produire est obligatoire", 6, "info");
            return;
        }
        if (tf_lot_to_prod.getText().isEmpty()) {
            MainUI.notify(null, "INFO", "Erreur : Le numero de lot de la production est obligatoire", 6, "info");
            return;
        }
        if (tf_quant_to_prod.getText().isEmpty()) {
            MainUI.notify(null, "INFO", "Erreur : La quantite previsionnelle a produire est obligatoire", 6, "info");
            return;
        }
        if (dpk_end_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "erreur : la date de fin de programme de production est obligatoire", 6, "info");
            return;
        }
        if (dpk_now_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "erreur : le date de production est obligatoire", 6, "info");
            return;
        }
        if (dpk_start_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "erreur : le date du debut du programme de production est obligatoire", 6, "info");
            return;
        }
        if (cbx_state_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "erreur : l'etat de la production est obligatoire", 6, "info");
            return;
        }
        if (cbx_mesure_to_prod.getValue() == null) {
            MainUI.notify(null, "INFO", "erreur : l'unite de stockage du produit a produire est obligatoire", 6, "info");
            return;
        }

        Production pro = new Production(DataId.generate());
        pro.setComment(txtArea_comment_prod.getText());
        pro.setDateDebut(dpk_start_prod.getValue());
        pro.setDateFabrication(dpk_now_prod.getValue());
        pro.setDateFin(dpk_end_prod.getValue());
        pro.setDatePeremption(dpk_expir_to_prod.getValue());
        pro.setEtat(cbx_state_prod.getValue());
        pro.setMesureId(cbx_mesure_to_prod.getValue());
        pro.setNumlot(tf_lot_to_prod.getText());
        pro.setProduitId(cbx_produits_prod.getValue());
        pro.setQualitePrevu(cbx_quality_to_prod.getValue());
        pro.setQuantitePrevu(Double.valueOf(tf_quant_to_prod.getText()));
        pro.setRegion(cbx_region_prod.getValue());
        Production saved = ProductionDelegate.saveProduction(pro);
        if (saved != null) {
            ols_productions.add(saved);
            showValue(saved);
            MainUI.notify(null, "INFO", "La production a ete enregistree avec succes", 5, "info");
        }

    }

    @FXML
    private void refreshProduction(MouseEvent event) {
        ols_productions.setAll(ProductionDelegate.findProductions());
    }

    @FXML
    private void updateProduction(MouseEvent event) {
        if (choosenProduction == null) {
            MainUI.notify(null, "Error", "Erreur: selectionnez une production", 5, "error");
            return;
        }
        choosenProduction.setComment(txtArea_comment_prod.getText());
        choosenProduction.setDateDebut(dpk_start_prod.getValue());
        choosenProduction.setDateFabrication(dpk_now_prod.getValue());
        choosenProduction.setDateFin(dpk_end_prod.getValue());
        if (dpk_expir_to_prod.getValue() != null) {
            choosenProduction.setDatePeremption(dpk_expir_to_prod.getValue());
        }
        choosenProduction.setEtat(cbx_state_prod.getValue());
        choosenProduction.setMesureId(cbx_mesure_to_prod.getValue());
        choosenProduction.setNumlot(tf_lot_to_prod.getText());
        choosenProduction.setProduitId(cbx_produits_prod.getValue());
        choosenProduction.setQualitePrevu(cbx_quality_to_prod.getValue());
        choosenProduction.setQuantitePrevu(Double.valueOf(tf_quant_to_prod.getText()));
        choosenProduction.setRegion(cbx_region_prod.getValue());
        Production saved = ProductionDelegate.updateProduction(choosenProduction);
        if (saved != null) {
            ols_productions.set(ols_productions.indexOf(choosenProduction), saved);
            showValue(saved);
            MainUI.notify(null, "INFO", "La production a ete modifiee avec succes", 5, "info");
        }
    }

    @FXML
    private void deleteProduction(MouseEvent event) {

    }

    @FXML
    private void addNewProduct(ActionEvent event) {
        MainUI.floatDialog(tools.Constants.PRODUCT_DLG, 600, 790, token, kazisafe, this.entreprise, null);
    }

    public void addLivraison(Livraison svl) {
        this.ols_entr_livr.add(svl);
    }

    @FXML
    private void transfererProdtoStock(MouseEvent event) {
        if (choosenFinishedProduct != null) {
            Produit p = choosenFinishedProduct.getProduitId();
            txt_produit_deprod.setText(p.getNomProduit() + " "
                    + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()) + " " + p.getCodebar());
            ols_mesure_deprod.setAll(FXCollections.observableArrayList(MesureDelegate.findMesureByProduit(p.getUid())));
            txt_lot_deprod.setText(choosenFinishedProduct.getNumlot());
            cbx_mesure_deprod.getSelectionModel().select(choosenFinishedProduct.getMesureId());
            cbx_region_deprod.setValue(choosenFinishedProduct.getRegion());
            chbx_to_sale_deprod.setSelected(true);
        }
    }

    @FXML
    private void storePf(ActionEvent event) {
        if (tf_quant_deprod.getText().isEmpty()) {
            MainUI.notify(null, "", "La quantite reelle produite est obligatoire", 5, "error");
            return;
        }
        if (cbx_mesure_deprod.getValue() == null) {
            MainUI.notify(null, "", "L'unite de stockage de la quantite est obligatoire", 5, "error");
            return;
        }
        if (cbx_quality_deprod.getValue() == null) {
            MainUI.notify(null, "", "La qualite produite est obligatoire", 5, "error");
            return;
        }
        if (cbx_avancement_deprod.getValue() == null) {
            MainUI.notify(null, "", "L'etat d'avancement de la production est obligatoire", 5, "error");
            return;
        }
        if (cbx_region_dest_deprod.getValue() == null) {
            MainUI.notify(null, "", "La region destinatrice de la production est obligatoire", 5, "error");
            return;
        }
        if (cbx_depot_deprod.getValue() == null) {
            MainUI.notify(null, "", "Le depot de stockage de la production est obligatoire", 5, "error");
            return;
        }
        double qte = Double.parseDouble(tf_quant_deprod.getText());
        List<Imputer> li = ImputerDelegate.findForProduction(choosenFinishedProduct.getUid());
        List<Repartir> lr = RepartirDelegate.findForProduction(choosenFinishedProduct.getUid());
        double chargDir = li.stream().mapToDouble(i -> i.getDevise().equals("USD") ? i.getMontant()
                : (i.getMontant() / taux)).sum();
        double sumMp = lr.stream().mapToDouble(mp -> mp.getQuantite() * mp.getCoutAchat()).sum();
        double ct = (chargDir + sumMp) / qte;
        int value = cbx_avancement_deprod.getSelectionModel().getSelectedIndex();
        Entreposer entrpos = new Entreposer(DataId.generate());
        entrpos.setComment(txtArea_comment_deprod.getText());
        entrpos.setCout(ct);
        entrpos.setDate(LocalDateTime.now());
        entrpos.setDepotId(cbx_depot_deprod.getValue());
        entrpos.setDevise("USD");
        entrpos.setExpiryDate(dpk_expiry_deprod.getValue());
        entrpos.setMesureId(cbx_mesure_deprod.getValue());
        if (value == 0) {
            entrpos.setNiveauFabrication(Constants.MANUFACTURING_LEVEL_MADE_PRODUCT);
        } else {
            entrpos.setNiveauFabrication(Constants.MANUFACTURING_LEVEL_MIDDLE_END_PRODUCT);
            Produit pro = choosenFinishedProduct.getProduitId();
            Matiere mat = MatiereDelegate.findMatiere(pro.getUid());
            entrpos.setMatiereId(mat);
            MatiereSku ms = MatiereSkuDelegate.findMatiereSku(entrpos.getMesureId().getUid());
            entrpos.setSkuId(ms);
        }
        entrpos.setNumlot(choosenFinishedProduct.getNumlot());
        entrpos.setProductionId(choosenFinishedProduct);
        entrpos.setQualite(cbx_quality_deprod.getValue());
        entrpos.setQuantite(qte);
        entrpos.setRegion(cbx_region_deprod.getValue());
        Entreposer savedE = EntreposerDelegate.saveEntreposer(entrpos);
        Stocker savedS = null;
        if (chbx_to_sale_deprod.isSelected()) {
            Livraison liv = new Livraison(DataId.generate());
            liv.setDateLivr(LocalDate.now());
            liv.setFournId(FournisseurDelegate.findOrCreate(entreprise));
            liv.setLibelle("Stockage de PF");
            liv.setNumPiece("PT" + ((int) (Math.random() * 10000)));
            liv.setObservation("");
            liv.setPayed((chargDir + sumMp));
            liv.setReduction(0d);
            liv.setReference("TXNPROD" + ((int) (Math.random() * 100000)) + "S");
            liv.setRegion(choosenFinishedProduct.getRegion());
            liv.setRemained(0d);
            liv.setTopay((chargDir + sumMp));
            liv.setToreceive(0d);
            Livraison saved = LivraisonDelegate.saveLivraison(liv);
            Stocker st = new Stocker(DataId.generate());
            st.setCoutAchat(ct);
            st.setDateExpir(dpk_expiry_deprod.getValue());
            st.setDateStocker(LocalDateTime.now());
            st.setLibelle("Stockage de PF");
            st.setLivraisId(saved);
            st.setLocalisation(region);
            st.setMesureId(cbx_mesure_deprod.getValue());
            st.setNumlot(choosenFinishedProduct.getNumlot());
            st.setObservation("RAS");
            st.setProductId(choosenFinishedProduct.getProduitId());
            st.setQuantite(qte);
            st.setReduction(0d);
            st.setReduction(0d);
            st.setStockAlerte(2d);
            st.setRegion(region);
            st.setPrixAchatTotal((ct * qte));
            savedS = StockerDelegate.saveStocker(st);
        }

        if (savedE != null || savedS != null) {
            MainUI.notify(null, "", "Le stockage fait avec succes", 5, "info");
            ols_entreposer_pfini.add(savedE);
        }
    }

    @FXML
    private void updatePfStorage(MouseEvent event) {
    }

    @FXML
    private void deletePfStorage(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer l'entreposage de la production selectionné", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            if (choosenProdFinished != null) {
                EntreposerDelegate.removeEntreposer(choosenProdFinished);
                MainUI.notify(null, "INFO", "L'entreposage a ete supprimmé avec succes", 5, "info");
            }
        }
    }
    List<Operation> ops;
    List<Imputer> imps;
    double somTotAmont = 0;
    double diffInd = 0;

    @FXML
    private void filterToDate(ActionEvent event) {
        populate();
    }

    private List<EntreposerHelper> toEntreposerHelper(List<Entreposer> les) {
        List<EntreposerHelper> result = new ArrayList<>();
        for (Entreposer le : les) {
            EntreposerHelper el = new EntreposerHelper();
            el.setEntreposer(le);
            Production pt = ProductionDelegate.findProduction(le.getProductionId().getUid());
            el.setProduction(pt);
            Produit pro = ProduitDelegate.findProduit(pt.getProduitId().getUid());
            el.setProduit(pro);
            result.add(el);
        }
        return result;
    }

    private void initUpdaTable() {

        col_quantaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Entreposer entr = l.getEntreposer();
            Mesure mez = entr.getMesureId();
            double q = entr.getQuantite();
            return new SimpleStringProperty(q + " " + mez.getDescription());
        });
        col_prodaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Produit p = l.getProduit();
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()));
        });
        col_findaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Production p = l.getProduction();
            return new SimpleStringProperty(p.getDateFin().toString());
        });
        col_coutotaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Entreposer entr = findOngoingProdEntreposer(l.getProduit().getUid());
            return new SimpleStringProperty((entr.getCout() * l.getEntreposer().getQuantite()) + " USD");
        });
        col_coutaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Entreposer entr = findOngoingProdEntreposer(l.getProduit().getUid());
            return new SimpleStringProperty(entr.getCout() + " USD");
        });
        col_debutaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Production p = l.getProduction();
            return new SimpleStringProperty(p.getDateDebut().toString());
        });
        col_lotaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper lx = param.getValue();
            Entreposer l = lx.getEntreposer();
            return new SimpleStringProperty(l.getNumlot());
        });
        col_regionaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            EntreposerHelper l = param.getValue();
            Production p = l.getProduction();
            return new SimpleStringProperty(p.getRegion());
        });
        col_quotaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<EntreposerHelper, Number> param) -> {
            EntreposerHelper p = param.getValue();
            return new SimpleDoubleProperty(p.getQuotePart());
        });
        col_quotaff_indir.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        col_quotaff_indir.setOnEditCommit((TableColumn.CellEditEvent<EntreposerHelper, Number> event) -> {
            EntreposerHelper p = event.getRowValue();
            double ammount = (double) event.getNewValue().doubleValue();
            double oldamount = (double) event.getOldValue().doubleValue();
            // if (dpk_start_indir.getValue() != null && dpk_end_indir.getValue() != null) {
            diffInd += (oldamount - ammount);
            if (diffInd < 0) {
                p.setQuotePart(0);
                double sum = ops.stream()
                        .mapToDouble(o -> (o.getMontantUsd() + (o.getMontantCdf() / taux))).sum();
                imps = ImputerDelegate.findByDateInterval(cbx_charge_indir.getValue(),
                        dpk_start_indir.getValue(), dpk_end_indir.getValue());
                double sumAf = imps.stream().mapToDouble(Imputer::getMontant)
                        .sum();
                double cam = tbl_affectation_indir.getItems().stream().mapToDouble(e -> (e.getQuotePart()))
                        .sum();
                diffInd = (sum - sumAf) - cam;
                txt_sumcharge_indir.setText(diffInd + " USD");
                MainUI.notify(null, "", "Vous ne pouvez pas imputer le montant superieur a celui de la charge", 4, "error");
            } else {
                p.setQuotePart(ammount);
                txt_sumcharge_indir.setText(diffInd + " USD");

            }
            Platform.runLater(() -> {
                sumup();
            });

        });
    }

    private void sumup() {
        somTotAmont = tbl_affectation_indir.getItems().stream().mapToDouble(e -> ((e.getEntreposer().getCout() * e.getEntreposer().getQuantite()) + e.getQuotePart()))
                .sum();
        txt_sumaffect_indir.setText(somTotAmont + " USD");
    }

    private void populate() {
        if (dpk_start_indir.getValue() != null && dpk_end_indir.getValue() != null) {
            ops = OperationDelegate.findByDateInterval(cbx_charge_indir.getValue(),
                    dpk_start_indir.getValue(), dpk_end_indir.getValue());
            double sum = ops.stream()
                    .mapToDouble(o -> (o.getMontantUsd() + (o.getMontantCdf() / taux))).sum();
            imps = ImputerDelegate.findByDateInterval(cbx_charge_indir.getValue(),
                    dpk_start_indir.getValue(), dpk_end_indir.getValue());
            double sumAf = imps.stream().mapToDouble(Imputer::getMontant)
                    .sum();
            diffInd = sum - sumAf;
            txt_sumcharge_indir.setText(diffInd + " USD");
            List<Entreposer> tofill = EntreposerDelegate.findEntreposersGroupedByProd(dpk_start_indir.getValue(), dpk_end_indir.getValue());
            List<EntreposerHelper> ehlps = toEntreposerHelper(tofill);
            ols_entreposeraff_indir.setAll(ehlps);
            somTotAmont = tbl_affectation_indir.getItems().stream().mapToDouble(e -> ((e.getEntreposer().getCout() * e.getEntreposer().getQuantite()) - e.getQuotePart()))
                    .sum();
            txt_sumaffect_indir.setText(somTotAmont + " USD");
        }
    }

    @FXML
    private void saveAffectation(ActionEvent event) {
        ops = OperationDelegate.findByDateInterval(cbx_charge_indir.getValue(),
                dpk_start_indir.getValue(), dpk_end_indir.getValue());
        List<Entreposer> tofill = EntreposerDelegate.findEntreposersGroupedByProd(dpk_start_indir.getValue(), dpk_end_indir.getValue());
        double global = tofill.stream().mapToDouble(e -> e.getCout() * e.getQuantite()).sum();
        ObservableList<EntreposerHelper> items = tbl_affectation_indir.getItems();
        for (EntreposerHelper h : items) {
            Produit p = h.getProduit();
            double charg = h.getQuotePart();
            List<Production> lps = ProductionDelegate.findForProduct(p,
                    dpk_start_indir.getValue(), dpk_end_indir.getValue());
            for (Production lp : lps) {
                List<Entreposer> lep = EntreposerDelegate.findByProduction(lp.getUid());
                double sumprod = lep.stream().mapToDouble(e -> (e.getCout() * e.getQuantite())).sum();
                double rate = sumprod / global;
                double qp = charg * rate;
                Entreposer entr = lep.get(0);
                Imputer imp = new Imputer(DataId.generate());
                imp.setDate(LocalDate.now());
                imp.setDevise("USD");
                imp.setMontant(qp);
                imp.setOperationId(ops.get(0));
                imp.setPercent(rate);
                imp.setProductionId(lp);
                imp.setRegion(region);
                Imputer saveImputer = ImputerDelegate.saveImputer(imp);
                if (saveImputer != null) {
                    if (!ols_imputeraff_indir.contains(saveImputer)) {
                        ols_imputeraff_indir.add(saveImputer);
                    }
                }
                //le calcul du cout unitaire puis modification entreposer
                double q = entr.getQuantite();
                double chargDir = lp.getImputerList().stream().mapToDouble(i -> i.getDevise().equals("USD") ? i.getMontant()
                        : (i.getMontant() / taux)).sum();
                double sumMp = lp.getRepartirList().stream().mapToDouble(mp -> mp.getQuantite() * mp.getCoutAchat()).sum();
                double newCt = (chargDir + sumMp) / q;
                entr.setCout(newCt);
                EntreposerDelegate.updateEntreposer(entr);
            }
        }
        MainUI.notify(null, "", "Imputation faite avec ", 4, "info");

    }

    @FXML
    private void deleteSavedAffectation(MouseEvent event) {
    }

    private void initTbl() {
        col_savedaffprod_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer ll = param.getValue();
            Production l = ll.getProductionId();
            Produit p = l.getProduitId();
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()));
        });
        col_savednomcharg_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Depense dep = l.getOperationId().getDepenseId();
            return new SimpleStringProperty(dep.getNomDepense());
        });
        col_savedaffdebut_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Production pro = l.getProductionId();
            return new SimpleStringProperty(pro.getDateDebut().toString());
        });
        col_savedafffin_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Production pro = l.getProductionId();
            return new SimpleStringProperty(pro.getDateFin().toString());
        });
        col_savedafflot_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Production pro = l.getProductionId();
            return new SimpleStringProperty(pro.getNumlot());
        });
        col_savedaffquant_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Production pro = l.getProductionId();
            List<Entreposer> es = EntreposerDelegate.findByProduction(pro.getUid());
            if (es.isEmpty()) {
                return new SimpleStringProperty();
            }
            Entreposer e = es.get(0);
            Mesure m = e.getMesureId();
            return new SimpleStringProperty(e.getQuantite() + " " + m.getDescription());
        });
        col_savedaffcout_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Production pro = l.getProductionId();
            List<Entreposer> es = EntreposerDelegate.findByProduction(pro.getUid());
            if (es.isEmpty()) {
                return new SimpleStringProperty();
            }
            Entreposer e = es.get(0);
//             Mesure m=e.getMesureId();
            return new SimpleStringProperty(e.getCout() + " USD");
        });

        col_savedaffquot_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
//             Mesure m=e.getMesureId();
            return new SimpleStringProperty(l.getMontant() + " USD");
        });

        col_savedaffregion_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            return new SimpleStringProperty(l.getRegion());
        });

        col_savedaffcoutotaff_indir.setCellValueFactory((TableColumn.CellDataFeatures<Imputer, String> param) -> {
            //Fournisseur fss = strongDb.findByUid(Fournisseur.class, param.getValue().getFournId().getUid());
            Imputer l = param.getValue();
            Production pro = l.getProductionId();
            List<Entreposer> es = EntreposerDelegate.findByProduction(pro.getUid());
            if (es.isEmpty()) {
                return new SimpleStringProperty();
            }
            Entreposer e = es.get(0);
            double ct = e.getCout() * e.getQuantite();
            return new SimpleStringProperty(ct + " USD");
        });

    }

}
