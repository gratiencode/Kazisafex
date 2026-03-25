/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.CategoryDelegate;
import delegates.ClientDelegate;
import delegates.DestockerDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RetourMagasinDelegate;
import delegates.StockerDelegate;
import delegates.VenteDelegate;
import delegates.InventaireDelegate;
import delegates.CompterDelegate;
import data.core.KazisafeServiceFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import data.*;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TabPane;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import data.Category;
import data.Client;
import data.CompteTresor;
import data.Destocker;
import data.Entreprise;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.PermitTo;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Refresher;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import data.Inventaire;
import data.Compter;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.Cart;
import tools.Constants;
import static tools.Constants.CAISSES;
import tools.DataId;
import tools.FileUtils;
import tools.InventoryMagasin;
import tools.ListViewItem;
import tools.MainUI;
import tools.PhysicalInventoryLine;
import tools.Quintuplet;
import tools.Rupture;
import tools.SaleItem;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import utilities.PDFUtils;
import utilities.Peremption;
import data.helpers.Role;
import data.network.Kazisafe;
import data.helpers.TypeTraisorerie;
import delegates.PermissionDelegate;
import delegates.TraisorerieDelegate;
import java.math.BigInteger;
import java.util.Base64;
import tools.NotificationHandler;
import java.util.concurrent.ExecutorService;
import tools.ComptageItem;
import tools.NetLoockup;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import tools.ComboBoxAutoCompletion;
import tools.JsonUtil;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class PosController implements Initializable {

    @FXML
    private Label txt_panier_count;
    @FXML
    private ListView<LigneVente> panier_list;
    @FXML
    private Label txt_panier_total;
    @FXML
    Label rupt_count;
    @FXML
    Label progs;
//    @FXML
//    private TabPane tabpane_pos;
    @FXML
    private Tab tab_pos;
    @FXML
    private Tab tabcarts;
    @FXML
    private Tab tab_axsoir;

    @FXML
    Pane pane_wait_import;
    @FXML
    private TilePane tile_pane;
//    @FXML
//    private Tab tab_requisition;
    @FXML
    Tab tab_rupture;
    @FXML
    ImageView btn_view_mode;
    @FXML
    private Pagination pagination_req;
    @FXML
    Pane load_history;
    @FXML
    private CheckBox chkbx_declasser_panier;
    @FXML
    private TextField quant_retour;
    @FXML
    private ComboBox<Mesure> mesure_retour;
    @FXML
    private ComboBox<LigneVente> cbx_lgnvt_retour;
    @FXML
    private ComboBox<Recquisition> cbx_produit_retour_depot;
    @FXML
    private ComboBox<Fournisseur> cbx_forunisseur_cmd;
    @FXML
    private TextField tf_quant_retour_depot;
    @FXML
    private ComboBox<Mesure> cbx_mesure_retour_depot;
    @FXML
    private ComboBox<String> cbx_region_retour_depot;
    @FXML
    private ListView<InventoryMagasin> listvu_choosen4_retour_depot;
    @FXML
    private Pane pane_retour;
    @FXML
    Pane saveCartPane;
    @FXML
    private TextArea comment_retour;
    @FXML
    ProgressIndicator syncIndicator;
    Fournisseur choosenSupply;
    //rupture
    ObservableList<Rupture> obl_rupture_list;
    @FXML
    private CheckBox chbx_xall;
    @FXML
    private TableView<Rupture> tbl_rupture;
    @FXML
    private TableColumn<Rupture, String> tbl_rupt_date;
    @FXML
    private TableColumn<Rupture, String> tbl_rupt_produit;
    @FXML
    private TableColumn<Rupture, String> tbl_rupt_localisation;
    @FXML
    private TableColumn<Rupture, String> tbl_rupt_region;
    @FXML
    private TableColumn<Rupture, String> tbl_rupt_mesure;
    @FXML
    private TableColumn<Rupture, Number> tbl_rupt_uprice;
    @FXML
    private TableColumn<Rupture, Number> tbl_rupt_tprice;
    @FXML
    private TableColumn<Rupture, Number> tbl_rupt_quant;
    @FXML
    private TableColumn<Rupture, Boolean> tbl_rupt_select;
    @FXML
    private TableColumn<Rupture, Number> tbl_rupt_alert;
    //end rupture

    @FXML
    private TableView<Recquisition> table_req;
    @FXML
    private TableColumn<Recquisition, String> col_date_req;
    @FXML
    private TableColumn<Recquisition, String> col_ref_req;
    @FXML
    private TableColumn<Recquisition, String> col_prod_req;
    @FXML
    private TableColumn<Recquisition, String> col_quant_req;
    @FXML
    private TableColumn<Recquisition, Number> col_coutunit_req;
    @FXML
    private TableColumn<Recquisition, Number> col_coutotal_req;
    @FXML
    private TableColumn<Recquisition, String> col_numlot_req;
    @FXML
    private TableColumn<Recquisition, String> col_alerte_req;
    @FXML
    private TableColumn<Recquisition, String> col_expiry_req;
    @FXML
    private TableColumn<Recquisition, String> col_oberv_req;
    @FXML
    private DatePicker dpk_debut_req;

    @FXML
    private DatePicker dpk_fin_req;
    @FXML
    private ComboBox<String> cbx_provenance_req;
    @FXML
    private TabPane tabpane_req_left;
    @FXML
    private Tab tab_requisition;
    @FXML
    private TextField search_livraiz_req;
    @FXML
    private ListView<Livraison> list_livraison_req;
    @FXML
    private TextField search_supplier_req;
    @FXML
    private ListView<Fournisseur> list_supplier_req;
    ObservableList<Livraison> obl_livraisons_req;
    ListViewItem newValue;

    @FXML
    private DatePicker dpk_debut_inv_mag;
    @FXML
    private DatePicker dpk_fin_inv_mag;
    @FXML
    private Label txt_table_req_count;
    @FXML
    private ComboBox<Integer> rowPP;
    @FXML
    private Label txt_table_req_count1;

    @FXML
    private Tab tab_history;
    @FXML
    Tab tab_mag_inv;
    @FXML
    private Pagination pagination1;
    @FXML
    private Button btn_pay_now;
    @FXML
    private DatePicker dpk_debut_hyst;
    @FXML
    private DatePicker dpk_fin_hyst;
    @FXML
    private Label txt_table_hyst_count;
    @FXML
    private ComboBox<Integer> rowPP1, rowPP2;
    @FXML
    ComboBox<String> cbx_region_rupture;
    @FXML
    private ComboBox<String> cbx_region_maginv;
    @FXML
    private ComboBox<String> cbx_region_venthist;
    @FXML
    private Label txt_compact_somme_usd;
    @FXML
    private Label txt_compact_somme_cdf;
    @FXML
    private Label txt_compact_somme_cash;
    @FXML
    private Label txt_compact_somme_credit;
    @FXML
    private Label txt_compact_retour_cdf;
    @FXML
    private Label txt_compact_reste_recu_usd;
    @FXML
    private Label txt_compact_reste_recu_cdf;
    @FXML
    private Label txt_compact_retour_usd;
    @FXML
    private Label txt_compact_credit_client;
    @FXML
    private Label txt_compact_volume_achat;
    @FXML
    private ComboBox<Produit> cbx_compact_produit;
    @FXML
    private ComboBox<Client> cbx_compact_clients;
    @FXML
    private ComboBox<CompteTresor> cbx_compact_compte_tr;
    @FXML
    private CheckBox chbx_compact_auto_add;
    @FXML
    private CheckBox chbx_compact_print_bill_whensaving;
    @FXML
    private Spinner<Integer> spnr_compact_quantite;
    @FXML
    private CheckBox chbx_compact_aretirer;
    @FXML
    private DatePicker dpk_compact_echeance;
    @FXML
    private TextField tf_compact_codebar;
    @FXML
    private TextField tf_compact_nom_client;
    @FXML
    private TextField tf_compact_phone_client;
    @FXML
    private TextField tf_compact_recu_cdf;
    @FXML
    private TextField tf_compact_recu_usd;
    @FXML
    private TextField tf_compact_libelle_vente;
    @FXML
    private TextField tf_compact_ettiquete_panier;
    @FXML
    private ImageView img_vu_compact_historique;
    @FXML
    private ImageView img_vu_compact_artirer;
    @FXML
    private ImageView img_vu_compact_clean_cart;
    @FXML
    private ComboBox<Mesure> cbx_compact_mesure;
    @FXML
    private ImageView img_vu_compact_save_cart;
    @FXML
    private ImageView img_vu_compact_saved_cart;
    Pane pn_compact_save_panier;
    ImageView img_vu_compact_codebarr_show;
    ImageView img_vu_compact_print_codebar;

    @FXML
    private TableView<LigneVente> tbl_compact_panier;

    Livraison livraison;

    @FXML
    private Tab peremption;
    @FXML
    private DatePicker datePremption;
    @FXML
    private Label countPeremption;
    @FXML
    Label savecartsum;
    @FXML
    private Label valeurPremption;
    @FXML
    private TableView<Peremption> tablePeremption;
    @FXML
    private TableColumn<Peremption, String> col_codebar_exp;
    @FXML
    private TableColumn<Peremption, String> col_produit_exp;
    @FXML
    private TableColumn<Peremption, String> col_quantite_exp;
    @FXML
    private TableColumn<Peremption, Number> col_coutachat_exp;
    @FXML
    private TableColumn<Peremption, Number> col_valeur_exp;
    @FXML
    private TableColumn<Peremption, String> col_lot_exp;
    @FXML
    private TableColumn<Peremption, String> col_localisation_exp;
    @FXML
    private TableColumn<Peremption, String> col_date_exp;
    @FXML
    private TableColumn<Peremption, String> col_region_exp;
    @FXML
    private Pane list_mode;
    @FXML
    TableView<ListViewItem> tbl_list_pro;
    @FXML
    private TableColumn<ListViewItem, String> col_codebar_view;
    @FXML
    private TableColumn<ListViewItem, String> col_numlot_view;
    @FXML
    private TableColumn<ListViewItem, String> col_nomprod_view;
    @FXML
    private TableColumn<ListViewItem, String> col_marque_view;
    @FXML
    private TableColumn<ListViewItem, String> col_modele_view;
    @FXML
    private TableColumn<ListViewItem, String> col_taille_view;
    @FXML
    private TableColumn<ListViewItem, String> col_quanite_view;
    @FXML
    private TableColumn<ListViewItem, String> col_saleprice_view;
    @FXML
    private TableColumn<ListViewItem, String> col_saleprice_detail;
    @FXML
    private TableColumn<ListViewItem, String> col_purchaseprice_view;
    @FXML
    private TableColumn<ListViewItem, String> col_date_expir_view;
    @FXML
    private TreeTableView<SaleItem> ttable_ventes_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_date_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_facture_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_produits_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_quants_hyst;
    @FXML
    private TreeTableColumn<SaleItem, Number> trcol_pu_hyst;
    @FXML
    private TreeTableColumn<SaleItem, Number> trcol_totalusd_hyst;
    @FXML
    private TreeTableColumn<SaleItem, Number> trcol_totalcdf_hyst;
    @FXML
    private TreeTableColumn<SaleItem, Number> trcol_dette_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_client_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_echeance_hyst;
    @FXML
    private TreeTableColumn<SaleItem, String> trcol_libelle_hyst;
    @FXML
    private Pagination pagination2;
    @FXML
    private CheckBox chbx_dettes_only;
    @FXML
    private ImageView retourDepot;
    @FXML
    private ImageView btn_stock_rupture;
    @FXML
    private ImageView img_vu_clear_cart, btn_export_req, btn_refresh_req, btn_refresh_h, btn_export_h;
    @FXML
    private Label txt_total_chiffre_affaire, txt_inv_mag;
    @FXML
    private Label txt_tbl_inv_mag_count, tbl_pro_count;
    @FXML
    CheckBox chbx_scancbar;

    @FXML
    TableView<Vente> tbcarts;
    @FXML
    TableColumn<Vente, String> cart_date;
    @FXML
    TableColumn<Vente, String> cart_montant_usd;
    @FXML
    TableColumn<Vente, String> cart_montant_cdf;
    @FXML
    TableColumn<Vente, String> cart_reference;
    @FXML
    TableColumn<Vente, String> cart_libelle;
    @FXML
    TableColumn<Vente, String> cart_produx;

    @FXML
    TableView<InventoryMagasin> table_inv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_codebar_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_prod_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_entree_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_sortie_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_stock_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_val_stock_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_alert_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_lot_tInv_mag;
    @FXML
    TableColumn<InventoryMagasin, String> col_expiry_tInv_mag;
    @FXML
    ScrollPane scrollPos;
    @FXML
    Pane cmdfss;
    ImageView imgvu_mode_vupos;
    @FXML
    ImageView refreshRupture;
    @FXML
    ImageView cmdRupture;
    @FXML
    ImageView syncSales;
    @FXML
    ProgressIndicator pgsIndicator;
    @FXML
    ImageView importInventory;
    @FXML
    Label valeur_tot;
    @FXML
    ComboBox<String> cbx_devise;
    @FXML
    Pane motif_declass;
    @FXML
    TextField tf_motif_declass;
    @FXML
    private ComboBox<String> cbx_modapay;
    @FXML
    private ComboBox<String> cbx_paymode;
    @FXML
    TextField tf_adresse_livr;
    @FXML
    TextField tf_conta_perso;
    @FXML
    TextField tf_phone_conta_perso;
    @FXML
    Pane pane_retour_depot;
    int count_logic = 0;
    int pospg = 0;
    //JpaStorage db;

    ObservableList<Cart> cart;
    ObservableList<Recquisition> lsreq;
    ObservableList<Rupture> resultRuptureSearch;
    List<Recquisition> calcreq;
    Set<Recquisition> listDeRetourDepot;
    List<LigneVente> calclv;
    List<Vente> calcvente;
    ObservableList<Vente> savedCarts;
    ObservableList<Produit> lisprod;
    ObservableList<LigneVente> lslgnventes;
    ObservableList<InventoryMagasin> lsinventaire;
    ObservableList<LigneVente> ols_ligvt_retour;
    ObservableList<Mesure> ols_mesure_retour_depot;
    ObservableList<Recquisition> ols_recquis_retour_depot;
    ObservableList<String> ols_region_retour_depot;
    ObservableList<Mesure> ols_mesure_retour;
    ObservableList<Peremption> ols_peremption;
    ObservableList<String> regions;

    ObservableList<InventoryMagasin> ols_retours_invent_items;
    ObservableList<Inventaire> obl_inventaires;
    ObservableList<ComptageItem> obl_comptages;
    ObservableList<String> states;
    private SimpleBooleanProperty canCreateInventory = new SimpleBooleanProperty(false);
    TreeItem<SaleItem> rootView;
    ObservableList<TreeItem<SaleItem>> treeSaleItems;
    ObservableList<Category> categs;
    ObservableList<Fournisseur> obl_fournisseurs;
    ObservableList<ListViewItem> list_mode_ls;
    Set<Vente> selectedAvedCarts = new HashSet<>();
    List<Produit> prodx;
    LigneVente choosenLv;
    Recquisition choosenReq;
    Vente choosenVente;
    private Vente selectedCart;
    Kazisafe kazisafe;
    double valTotStock = 0;
    double valTotRupt = 0;
    int rowsDataCount = 20, rowsDataCount1 = 20, rowsDataCount2 = 20;
    int dataLoded = 20;
    int tdataLoded = 20;
    int origin = 0;
    @FXML
    ComboBox<Category> cbx_categofilter;
    Mesure choosenMesure4Retour;

    private Entreprise entreprise;
    Preferences pref;
    double taux2change;
    String region, role, token, entr;
    private ResourceBundle bundle;
    @FXML
    TextField tf_cart_label;
    @FXML
    private TabPane tabpane_main_pos;
    @FXML
    private Tab tab_main_pos;
    @FXML
    private TabPane tabpane_pos;
    @FXML
    private TableColumn<?, ?> col_numlot_view111;
    @FXML
    private ImageView btn_delete_inv_req;
    @FXML
    private Label lbl_livrez_recq;
    @FXML
    private Tab tab_invteorik;
    @FXML
    private TableColumn<?, ?> col_stok_init_tInv_mag;
    @FXML
    private Label txt_table_req_count1211;
    @FXML
    private Label txt_table_req_count111;
    @FXML
    private CheckBox chbx_perempt;
    @FXML
    private ProgressIndicator pgsIndicator_load_inv;
    @FXML
    private Tab tab_invphys;
    @FXML
    private ComboBox<Inventaire> cbx_inventaire_compter;
    @FXML
    private DatePicker dpk_date_today_compter;
    @FXML
    private DatePicker dpk_date_exp_compter;
    @FXML
    private ComboBox<Produit> cbx_produit_compter;
    @FXML
    private ComboBox<Mesure> cbx_mesure_compter;
    @FXML
    private TextField tf_numlot_compter;
    @FXML
    private TextField tf_coutachat_compter;
    @FXML
    private DatePicker dpk_datefin_inv_compter;
    @FXML
    private TextField tf_code_inv_compter;
    @FXML
    private DatePicker dpk_datedebut_inv_compter;
    @FXML
    private ComboBox<String> cbx_etat_inv_compter;
    @FXML
    private ComboBox<String> cbx_region_inv_compter;
    @FXML
    private TextField tf_comment_inv_compter;
    @FXML
    private TableView<ComptageItem> tbl_comptage_inv;
    @FXML
    private TableColumn<ComptageItem, String> col_date_today_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_code_inv_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_etat_inv_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_debut_date_inv_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_produit_compter;
    @FXML
    private TableColumn<ComptageItem, Number> col_stk_theorik_compter;
    @FXML
    private TableColumn<ComptageItem, Number> col_quantite_compter;
    @FXML
    private TableColumn<ComptageItem, Number> col_quant_ecart_compter;
    @FXML
    private TableColumn<ComptageItem, Number> col_valeur_unit_compter;
    @FXML
    private TableColumn<ComptageItem, Number> col_valeur_tot_compter;
    @FXML
    private TableColumn<ComptageItem, Number> col_val_ecart_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_lot_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_date_perem_compter;
    @FXML
    private TableColumn<ComptageItem, String> col_ecart_obs_compter;
    @FXML
    private Label txt_cloture_message_compter;
    @FXML
    private ProgressIndicator progress_cloture_inv_compter;
    @FXML
    private Label txt_valeur_global_compter;
    @FXML
    private ImageView imgvu_downlod_excel_compter;
    @FXML
    private TextField tf_quantite_compter;
    @FXML
    private ImageView imgvu_sync_compter;
    @FXML
    private Label txt_stkheorik_comptage;
    @FXML
    private Label txt_ecart_comptage;
    @FXML
    private TextField input_observ_comptage;
    @FXML
    private Label txt_valeurtotal_ecart_compter;
    @FXML
    private Label txt_table_req_count11;
    @FXML
    private Label txt_table_req_count121;
    @FXML
    private DatePicker datePremption1;
    @FXML
    private ProgressIndicator pgrs_trashing;
    @FXML
    private Label lbl_trashing_msg;
    @FXML
    private RadioButton rb_trashed;
    @FXML
    private RadioButton rb_untrashed;
    @FXML
    private ImageView btn_export_h1;
    @FXML
    private TableColumn<?, ?> tbl_rupt_prixvente;
    @FXML
    private Tab tab_retd;
    @FXML
    private ComboBox<?> cbx_produit_retd;
    @FXML
    private Label txt_available_retd;
    @FXML
    private TextField tf_quant_tran_retd;
    @FXML
    private TextArea txtarea_motif_retd;
    @FXML
    private TableView<?> tbl_info_retd;
    @FXML
    private TableColumn<?, ?> col_date_retd;
    @FXML
    private TableColumn<?, ?> cpl_produit_retd;
    @FXML
    private TableColumn<?, ?> col_lot_retd;
    @FXML
    private TableColumn<?, ?> col_qte_retd;
    @FXML
    private TableColumn<?, ?> col_coutach_retd;
    @FXML
    private TableColumn<?, ?> col_total_retd;
    @FXML
    private TableColumn<?, ?> col_motif_retd;
    @FXML
    private TableColumn<?, ?> col_prov_retd;
    @FXML
    private TableColumn<?, ?> col_dest_retd;
    @FXML
    private Label txt_count_retd;
    @FXML
    private ComboBox<?> cbx_mesure_retd;
    @FXML
    private Label txt_total_val_retd;
    @FXML
    private ComboBox<?> cbx_region_dest_retd;
    @FXML
    private ComboBox<?> cbx_region_prov_retd;
    @FXML
    private ComboBox<?> cbx_numlot_retd;
    @FXML
    private Tab tab_compact;
    @FXML
    private TableColumn<?, ?> col_codebar_compact;
    @FXML
    private TableColumn<?, ?> col_nomprod_compact;
    @FXML
    private TableColumn<?, ?> col_marque_compact;
    @FXML
    private TableColumn<?, ?> col_model_compact;
    @FXML
    private TableColumn<?, ?> col_tail_compact;
    @FXML
    private TableColumn<?, ?> col_color_compact;
    @FXML
    private TableColumn<?, ?> col_quant_compact;
    @FXML
    private TableColumn<?, ?> col_mesure_compact;
    @FXML
    private TableColumn<?, ?> col_prix_compact;
    @FXML
    private TableColumn<?, ?> col_prix_tot_compact;
    @FXML
    private TableColumn<?, ?> col_numlot_compact;
    @FXML
    private TableColumn<?, ?> col_region_compact;
    @FXML
    private Pane pane_etkt_panier_compact;
    @FXML
    private Label lbl_stock_dispo_compact;
    @FXML
    private ComboBox<?> cbx_compact_printers;
    @FXML
    private CheckBox chbx_compact_reduxion;

    Compter choosenCompter;

    private void configcbx() {
        cbx_categofilter.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category object) {
                return object == null ? null : object.getDescritption();
            }

            @Override
            public Category fromString(String string) {
                return cbx_categofilter.getItems()
                        .stream()
                        .filter(f -> (f.getDescritption())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_forunisseur_cmd.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur object) {
                return object == null ? null : object.getNomFourn() + " " + (object.getAdresse() == null ? "" : object.getAdresse()) + ", Tel :" + object.getPhone();
            }

            @Override
            public Fournisseur fromString(String string) {
                return cbx_forunisseur_cmd.getItems()
                        .stream()
                        .filter(f -> (f.getNomFourn() + " " + (f.getAdresse() == null ? "" : f.getAdresse()) + ", Tel :" + f.getPhone())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

    }

    public void addRecquisition(Recquisition liv) {
        Recquisition l = RecquisitionDelegate.findRecquisition(liv.getUid());
        if (l == null) {
            l = RecquisitionDelegate.saveRecquisition(liv);//db.insertOnly(liv);
        } else {
            l = RecquisitionDelegate.updateRecquisition(liv);//db.updateOnly(liv);
        }
        lsreq.add(l);
    }

    public void addPrixDeVente(PrixDeVente liv) {
        PrixDeVente l = PrixDeVenteDelegate.findPrixDeVente(liv.getUid());
        if (l == null) {
            PrixDeVenteDelegate.savePrixDeVente(liv);//db.insertOnly(liv);
        } else {
            l = PrixDeVenteDelegate.updatePrixDeVente(liv);//db.updateOnly(liv);
        }
    }

    @FXML
    public void addVente(Event e) {

    }

    public void addVentex(Vente liv) {
        Vente l = VenteDelegate.findVente(liv.getUid());//db.findByUid(Vente.class, liv.getUid());
        if (l == null) {
            l = VenteDelegate.saveVente(liv);//db.insertOnly(liv);
        } else {
            l = VenteDelegate.updateVente(liv);//db.updateOnly(liv);
        }

    }

    @FXML
    public void showSaveCartPane(Event e) {
        saveCartPane.setVisible(true);
    }
    int compteur = 0;
    double cmdvalue = 0;
    //Vente cartsv;

    @FXML
    public void saveCart(Event e) {
        if (tf_cart_label.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), "Complete cart label", 3, "error");
            return;
        }

        int tranzit;
        if (selectedCart == null) {
            tranzit = ((int) (Math.random() * 243350));
        } else {
            tranzit = selectedCart.getUid();
        }
        String reference;
        switch (count_logic) {
            case 1: {
                String leo = Constants.DATE_ONLY_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(leo)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (selectedCart == null) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", leo);
                    if (selectedCart == null) {
                        compteur = 1;
                    }
                }
                reference = String.format("%06d", compteur);
                break;
            }
            case 2: {
                String mois = Constants.YEAR_AND_MONTH_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(mois)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (selectedCart == null) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", mois);
                    if (selectedCart == null) {
                        compteur = 1;
                    }
                }
                reference = String.format("%06d", compteur);
                break;
            }
            case 3: {
                String mois = Constants.YEAR_ONLY_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(mois)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (selectedCart == null) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", mois);
                    if (selectedCart == null) {
                        compteur = 1;
                    }
                }       //            pref.putInt("_bill_counter_", compteur);
                reference = String.format("%06d", compteur);
                break;
            }
            case 4:
                compteur = pref.getInt("_bill_counter_", 0);
                if (selectedCart == null) {
                    compteur++;
                }   //            pref.putInt("_bill_counter_", compteur);
                reference = String.format("%08d", compteur);
                break;
            default:
                reference = String.valueOf(tranzit);
                break;
        }

        final Vente theCart = new Vente(tranzit);
        Client anonym = ClientDelegate.findAnonymousClient();//db.getAnonymousClient();
        theCart.setLibelle(tf_cart_label.getText());
        theCart.setObservation("Drafted");
        theCart.setClientId(anonym);
        theCart.setDateVente(LocalDateTime.now());
        theCart.setDeviseDette("USD");
        theCart.setLatitude(0d);
        theCart.setLongitude(0d);
        String dev = pref.get("mainCur", "USD");
        if (dev.equals("USD")) {
            theCart.setMontantCdf(savedSum * taux2change);
            theCart.setMontantUsd(savedSum);
        } else {
            theCart.setMontantCdf(savedSum);
            theCart.setMontantUsd(BigDecimal.valueOf(savedSum / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        }

        theCart.setMontantDette(0d);
        theCart.setPayment(Constants.PAYMENT_CASH);
        theCart.setReference(reference);
        theCart.setRegion(region);
        Vente v = VenteDelegate.findVente(theCart.getUid());
        if (theCart != null) {
            List<LigneVente> ligvs = LigneVenteDelegate.findByReference(theCart.getUid());
            ligvs.stream().map((ligv) -> {
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(ligv, Constants.ACTION_DELETE, Tables.LIGNEVENTE);
                        });
                return ligv;
            }).forEachOrdered((ligv) -> {
                List<RetourMagasin> rtrs = RetourMagasinDelegate.findByLigneVente(ligv.getUid());
                for (RetourMagasin rtr : rtrs) {
                    RetourMagasinDelegate.deleteRetourMagasin(rtr);
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(rtr, Constants.ACTION_DELETE, Tables.RETOURMAGASIN);
                            });

                }
                LigneVenteDelegate.deleteLigneVente(ligv);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(ligv, Constants.ACTION_DELETE, Tables.LIGNEVENTE);
                        });
            });

            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(theCart, Constants.ACTION_DELETE, Tables.VENTE);
                    });
            VenteDelegate.deleteVente(theCart);
        }

        final Vente vt = VenteDelegate.saveVente(theCart);//db.insertAndSync(theCart);
        Executors.newCachedThreadPool()
                .submit(() -> {
                    Util.sync(vt, Constants.ACTION_CREATE, Tables.VENTE);
                });

        for (LigneVente lv : panier_list.getItems()) {
            lv.setReference(theCart);
            LigneVente l = LigneVenteDelegate.findLigneVente(lv.getUid());
            Util.sync(LigneVenteDelegate.saveLigneVente(lv), Constants.ACTION_CREATE, Tables.LIGNEVENTE);
        }

        savedCarts.add(vt);
        MainUI.notify(null, bundle.getString("success"), bundle.getString("xcartsavedsuccess"), 3, "info");
        pref.putInt("tranzit_bill", -100);
        pref.putInt("_bill_counter_", compteur);

        closeFloatingPane(e);
    }

    public void addLigneVente(LigneVente liv) {
        LigneVente l = LigneVenteDelegate.findLigneVente(liv.getUid());
        if (l == null) {
            LigneVenteDelegate.saveLigneVente(liv);//db.insertOnly(liv);
        }
    }

    private static PosController instance;

    public PosController() {
        //db = JpaStorage.getInstance();
        lsreq = FXCollections.observableArrayList();
        instance = this;
    }

    public static PosController getInstance() {
        if (instance == null) {
            instance = new PosController();
        }
        return instance;
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
        taux2change = pref.getDouble("taux2change", 2000);
        tile_pane.setHgap(4);
        tile_pane.setVgap(4);
        tile_pane.setPadding(new Insets(2, 2, 2, 2));
        region = pref.get("region", "Goma");
        count_logic = pref.getInt("count-logic", 0);
        role = pref.get("priv", null);
        token = pref.get("token", null);
        entr = pref.get("eUid", "");
        rootView = new TreeItem<>(new SaleItem());
        ttable_ventes_hyst.setRoot(rootView);
        ttable_ventes_hyst.setShowRoot(false);
        motif_declass.setVisible(false);
        // db = JpaStorage.getInstance();
        listDeRetourDepot = new HashSet<>();
        lisprod = FXCollections.observableArrayList();
        obl_rupture_list = FXCollections.observableArrayList();
        obl_fournisseurs = FXCollections.observableArrayList();
        resultRuptureSearch = FXCollections.observableArrayList();
        lsinventaire = FXCollections.observableArrayList();
        table_inv_mag.setItems(lsinventaire);
        MainUI.setPattern(dpk_fin_req);
        MainUI.setPattern(dpk_debut_hyst);
        MainUI.setPattern(dpk_fin_hyst);
        MainUI.setPattern(datePremption);
        MainUI.setPattern(dpk_debut_req);
        MainUI.setPattern(dpk_debut_inv_mag);
        MainUI.setPattern(dpk_fin_inv_mag);
        pane_wait_import.setVisible(false);
        pane_retour_depot.setVisible(false);
        choosenVente = null;
        ContextMenu cm = new ContextMenu();
        MenuItem menuItem = new MenuItem("Voir la facture");
        MenuItem menuItem2 = new MenuItem("Récouvrer cette dette");
        MenuItem menuRetour = new MenuItem("Retour de Marchandise");
        MenuItem menuItem3 = new MenuItem("Supprimer la vente");

        cm.getItems().add(menuItem);
        cm.getItems().add(menuItem2);
        cm.getItems().add(menuRetour);
        cm.getItems().add(menuItem3);
        menuItem.setOnAction((ActionEvent event) -> {
            if (choosenVente != null) {
                MainUI.floatDialog(tools.Constants.PAYMENT_DLG, 1088, 678, null, kazisafe, choosenVente.getLigneVenteList(), choosenVente, entreprise, choosenVente.getClientId());
            }
        });
        menuRetour.setOnAction((ActionEvent event) -> {
            //To change body of generated methods, choose Tools | Templates.
            if (choosenVente != null) {
                List<LigneVente> lgvs = LigneVenteDelegate.findByReference(choosenVente.getUid());
                if (lgvs.isEmpty()) {
                    return;
                }
                ols_ligvt_retour.addAll(lgvs);
                cbx_lgnvt_retour.getSelectionModel().selectFirst();
                pane_retour.setVisible(true);
            } else {
                MainUI.notify(null, "Erreur", "Veuillez selectionner une facture valide puis reessayer", 4, "error");
            }
        });
        menuItem2.setOnAction((ActionEvent event) -> {
            if (choosenVente != null) {
                // List<Vente> lvts = VenteDelegate.findCreditSaleByRef(choosenVente.getReference());
                Vente vnt = VenteDelegate.findVente(choosenVente.getUid());
                System.out.println(" Ve-t " + vnt.getMontantDette());
                if (vnt.getMontantDette() > 0) {
                    //go to tresorerie>recouvrement
                    MainuiController.getInstance().switchScreens(tools.Constants.CAISSE_VIEW, CAISSES, "Trésorerie", "cashier.png", vnt, null);
                }
            }
        });
        menuItem3.setOnAction((ActionEvent event) -> {
            if (choosenVente != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer cette vente", ButtonType.YES, ButtonType.CANCEL);
                alert.setTitle("Attention!");
                alert.setHeaderText(null);
                Optional<ButtonType> showAndWait = alert.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    if (role.equals(Role.Trader.name()) | role.equals(Role.Manager_ALL_ACCESS.name())) {
                        List<LigneVente> ligvs = LigneVenteDelegate.findByReference(choosenVente.getUid());
                        ligvs.stream().map((ligv) -> {
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(ligv, Constants.ACTION_DELETE, Tables.LIGNEVENTE);
                                    });
                            return ligv;
                        }).forEachOrdered((ligv) -> {
                            List<RetourMagasin> rtrs = RetourMagasinDelegate.findByLigneVente(ligv.getUid());
                            for (RetourMagasin rtr : rtrs) {
                                RetourMagasinDelegate.deleteRetourMagasin(rtr);
                                Executors.newCachedThreadPool()
                                        .submit(() -> {
                                            Util.sync(rtr, Constants.ACTION_DELETE, Tables.RETOURMAGASIN);
                                        });

                            }
                            LigneVenteDelegate.deleteLigneVente(ligv);
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(ligv, Constants.ACTION_DELETE, Tables.LIGNEVENTE);
                                    });
                        });

                        Executors.newCachedThreadPool()
                                .submit(() -> {
                                    Util.sync(choosenVente, Constants.ACTION_DELETE, Tables.VENTE);
                                });
                        VenteDelegate.deleteVente(choosenVente);
                        MainUI.notify(null, "Success", "Suppression faite avec succès", 2, "info");
                    } else {
                        MainUI.notify(null, "Impossible de supprimer", "Vous n'avez pas les privileges necessaire pour effectuer la suppression", 2, "warn");
                    }
                }
            }
        });
        configcbx();

        ttable_ventes_hyst.setContextMenu(cm);
        lsreq = FXCollections.observableArrayList();
        ObservableList<Integer> rows = FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000));
        rowPP.setItems(rows);
        rowPP.getSelectionModel().selectFirst();
        // pagination_pos.setPageFactory(this::createPosView);
        // pagination_req.setPageFactory(this::createDataPage);
        // pagination_sale.setPageFactory(this::createDataPage1);
        // pagination_inv.setPageFactory(this::createDataPage2);
        ObservableList<Integer> rows1 = FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000));
        rowPP1.setItems(rows1);
        rowPP1.getSelectionModel().selectFirst();
        //pagination_sale.setPageFactory(this::createDataPage1);
        ObservableList<Integer> rows2 = FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000));
        rowPP2.setItems(rows2);
        rowPP2.getSelectionModel().selectFirst();
        list_mode_ls = FXCollections.observableArrayList();
        tbl_list_pro.setItems(list_mode_ls);
        cbx_devise.setItems(FXCollections.observableArrayList("$", "Fc", "FF", "Fb", "€", "£"));

        cbx_forunisseur_cmd.getSelectionModel().selectFirst();
        // pagination_inv.setPageFactory(this::createDataPage2);
        cbx_forunisseur_cmd.setItems(obl_fournisseurs);
        ttable_ventes_hyst.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<SaleItem>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<SaleItem>> observable, TreeItem<SaleItem> oldValue, TreeItem<SaleItem> newValue) {
                if (newValue != null) {
                    choosenVente = VenteDelegate.findVente(newValue.getValue().getIdVente());
                }
            }
        });
        ttable_ventes_hyst.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

            }
        });
        Util.installTooltip(img_vu_clear_cart, "Vider le panier");
        table_req.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Recquisition>() {
            @Override
            public void changed(ObservableValue<? extends Recquisition> observable, Recquisition oldValue, Recquisition newValue) {
                if (newValue != null) {
                    choosenReq = newValue;
                }
            }
        });
        ContextMenu cmrq = new ContextMenu();
        MenuItem modif = new MenuItem("Modifier");
        MenuItem del = new MenuItem("Supprimer");
        cmrq.getItems().add(modif);
        cmrq.getItems().add(del);
        table_req.setContextMenu(cmrq);
        scrollPos.hvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() == scrollPos.getHmax()) {
                    int or = dataLoded;
                    dataLoded += 6;
                    int limit = Math.min(dataLoded, prodx.size());
                    if (or > limit) {
                        return;
                    }
                    List<Produit> prod = prodx.subList(or, limit);
                    lisprod.addAll(prod);
                    fillProducts(true, lisprod);
                }
            }
        });

        modif.setOnAction((ActionEvent event) -> {
            if (choosenReq != null) {
                MainUI.floatDialog(tools.Constants.RECQ_DLG, 716, 746, null, kazisafe, tools.Constants.ACTION_UPDATE, choosenReq, entreprise, cbx_provenance_req.getValue());
            }
        });
        del.setOnAction((ActionEvent event) -> {
            if (choosenReq != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer la recquisition selectionnée", ButtonType.YES, ButtonType.CANCEL);
                alert.setTitle("Attention!");
                alert.setHeaderText(null);
                Optional<ButtonType> showAndWait = alert.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                        List<PrixDeVente> prcs = PrixDeVenteDelegate.findPricesForRecq(choosenReq.getUid());
                        prcs.forEach((prc) -> {
                            PrixDeVenteDelegate.deletePrixDeVente(prc);
                        });
                        RecquisitionDelegate.deleteRecquisition(choosenReq);
                        lsreq.remove(choosenReq);
                        MainUI.notify(null, "Succès", "Recquisition supprimée avec succès", 3, "Info");
                    }
                }
            }
        });

        cnft();
        conf();
        config();
        configList();
        confDraft();
        tbl_rupture.setItems(obl_rupture_list);
        initRequisitionTab();
        tab_rupture.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                List<Rupture> ruptures;
                                if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                                    ruptures = RecquisitionDelegate.findStockEnRupture();
                                } else {
                                    ruptures = RecquisitionDelegate.findStockEnRupture(region);
                                }
                                obl_rupture_list.setAll(ruptures);
                                Platform.runLater(() -> {
                                    rupt_count.setText(String.format(bundle.getString("xitems"), obl_rupture_list.size()));
                                });
                                chbx_xall.setSelected(false);
                            });

                }
            }
        });
        ContextMenu cmsc = new ContextMenu();
        MenuItem supp = new MenuItem(bundle.getString("delete"));
        MenuItem voirp = new MenuItem(bundle.getString("xshowcartcontent"));
        cmsc.getItems().add(voirp);
        cmsc.getItems().add(supp);
        tbcarts.setContextMenu(cmsc);
        voirp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (getSelectedCart() != null) {
                    pref.putInt("tranzit_bill", getSelectedCart().getUid());
                    System.out.println("VENte " + getSelectedCart().getMontantUsd());
                    savedSum = getSelectedCart().getMontantUsd();
                    tf_cart_label.setText(getSelectedCart().getLibelle());
                    lslgnventes.clear();
                    List<LigneVente> content = LigneVenteDelegate.findByReference(getSelectedCart().getUid());
                    for (LigneVente lvs : content) {
                        addCartItem(lvs);
                    }
                }
            }
        });
        supp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (getSelectedCart() != null) {
                    cleanSavedCart(event);
                    selectedCart = null;
                }
            }
        });

        savedCarts = FXCollections.observableArrayList();
        tbcarts.setItems(savedCarts);
        cbx_paymode.setItems(FXCollections.observableArrayList("Par Banque/Cheque", "Au comptant", "En temperament"));
        cbx_modapay.setItems(FXCollections.observableArrayList("30 jours", "A la livraison", "Périodique"));
//        pagination_pos.setPageFactory(this::createPosView);
        pagination_req.setPageFactory(this::createDataPage);
//        pagination_sale.setPageFactory(this::createDataPage1);
//        pagination_inv.setPageFactory(this::createDataPage2);
        tab_axsoir.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    double somme = 0;
                    savedCarts.clear();
                    List<Vente> cartx;
                    if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                        cartx = VenteDelegate.findDraftedCarts();
                    } else {
                        cartx = VenteDelegate.findDraftedCarts(region);
                    }
                    for (Vente vente : cartx) {
                        savedCarts.add(vente);
                        somme += vente.getMontantUsd();
                    }
                    savecartsum.setText(bundle.getString("xtotal") + " : " + somme + " USD or " + (somme * taux2change) + " CDF");
                }
            }
        });
        SpinnerValueFactory<Integer> values = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        values.setValue(1);
        spnr_compact_quantite.setValueFactory(values);
        spnr_compact_quantite.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (newValue != null) {
                    pref.putInt("bill-copy", newValue);
                }
            }
        });
//        Tooltip.install(btn_refresh, new Tooltip("Actualiser les données"));
//        Tooltip.install(btn_delete, new Tooltip("Supprimer les données"));
//        Tooltip.install(btn_export, new Tooltip("Exporter vers excel"));
        Tooltip.install(btn_refresh_h, new Tooltip("Actualiser l'historique"));
        Tooltip.install(btn_export_h, new Tooltip("Exporter vers excel"));
        Tooltip.install(txt_inv_mag, new Tooltip("Valeur du stock"));
        Tooltip.install(cmdRupture, new Tooltip("Télécharger un bon de commande"));
        Tooltip.install(refreshRupture, new Tooltip("Actualiser"));
        Tooltip.install(retourDepot, new Tooltip("Retourner un article au dépot"));
        Tooltip.install(btn_view_mode, new Tooltip("Afficher les elements dans un tableau"));
        Tooltip.install(syncSales, new Tooltip("Synchoniser les ventes d'aujourd'hui"));
        Tooltip.install(importInventory, new Tooltip("Importer l'inventaire du magasin pour la correction de stock"));
        Tooltip.install(btn_stock_rupture, new Tooltip("Sortir la liste de produit en rupture de stock"));
        pref.putInt("tranzit_bill", -100);
        cmdfss.setVisible(false);
        pgsIndicator.setVisible(false);
        progs.setVisible(false);
        initInventoryTab();
    }

    @FXML
    public void showSupllierOrderPane(Event e) {
        cbx_modapay.getSelectionModel().selectFirst();
        cbx_paymode.getSelectionModel().selectFirst();
        cbx_forunisseur_cmd.getSelectionModel().selectFirst();
        cbx_devise.getSelectionModel().selectFirst();
        cmdfss.setVisible(true);
    }

    @FXML
    public void generateOrderNow(Event e) {
        createOrder(entreprise, cbx_forunisseur_cmd.getValue());
    }

    @FXML
    public void getFinishedProducts(Event e) {
        createEndedList(entreprise);
    }

    private void createOrder(Entreprise entrep, Fournisseur ff) {
        if (ff == null) {
            MainUI.notify(null, "Erreur", "Choisir un fournisseur puis réesayer", 3, "error");
            return;
        }
        if (cbx_modapay.getValue() == null) {
            MainUI.notify(null, "Erreur", "Choisir une modalité de paiement puis réesayer", 3, "error");
            return;
        }
        if (cbx_paymode.getValue() == null) {
            MainUI.notify(null, "Erreur", "Choisir un mode de paiement puis réesayer", 3, "error");
            return;
        }
        if (commandelist.isEmpty()) {
            MainUI.notify(null, "Erreur", "Entrer la liste des produits avec des valeurs valides puis réesayer", 3, "error");
            return;
        }

        try {
            final File bcmd;
            try (PDDocument document = new PDDocument()) {
                PDPage fPage = new PDPage(PDRectangle.A4);
                document.addPage(fPage);
                int pageW = (int) PDRectangle.A4.getWidth();//fPage.getTrimBox().getWidth();
                int pageH = (int) PDRectangle.A4.getHeight();//fPage.getTrimBox().getHeight();
                PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
                PDFUtils pdf = new PDFUtils(document, contentStream);
                //PDFont normalbold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
                // PDFont normal = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
                PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                File f = FileUtils.pointFile(entrep.getUid() + ".png");
                InputStream is;
                if (!f.exists()) {
                    is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                    f = FileUtils.streamTofile(is);
                }
                java.awt.Color endeleya = new java.awt.Color(68, 206, 245);
                java.awt.Color egray = new java.awt.Color(218, 218, 219);
                String path = f.getPath();
                System.out.println("path ->>>>>>XXXXX " + path);
                if (new File(path).exists()) {
                    try {
                        PDImageXObject logo = PDImageXObject.createFromFile(path, document);
                        contentStream.drawImage(logo, pageW - 114, pageH - 114, 84, 84);
                    } catch (Exception e) {
                        Throwable cause = e.getCause();
                        cause.printStackTrace();
                    }
                }
                pdf.addTextLine("Bon de commande", 25, pageH - 98, hbold, 40, java.awt.Color.DARK_GRAY);
                contentStream.setStrokingColor(endeleya);
                contentStream.setLineWidth(2);
                contentStream.moveTo(25, 700);
                contentStream.lineTo(pageW - 25, 700);
                contentStream.stroke();
                pdf.addTextLine(entrep.getNomEntreprise(), 25, pageH - 180, hnormal, 18, java.awt.Color.BLACK);
                pdf.addTextLine(new String[]{"Adresse : " + entrep.getAdresse(),
                    "RCCM : " + entrep.getIdentification(), entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(), entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot()}, 15, 25, pageH - 192, hnormal, 14, java.awt.Color.BLACK);
                String idf = ff.getIdentification();
                pdf.addTextLine(ff.getNomFourn(), ((int) (pageW - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "RCCM : " + idf) / 1000 * 15 - 92)), pageH - 180, hnormal, 18, java.awt.Color.BLACK);
                pdf.addTextLine(new String[]{"Adresse : " + ff.getAdresse(), idf == null ? ""
                    : "RCCM : " + idf,
                    "Tel : " + ff.getPhone()}, 15, ((int) (pageW - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "RCCM : " + idf) / 1000 * 15 - 92)), pageH - 192, hnormal, 14, java.awt.Color.BLACK);
                String date = "Date : " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date());
                pdf.addTextLine(new String[]{date,
                    "Bon de commande N# : " + (int) (Math.random() * 100000)}, 15, ((int) (pageW - hnormal.getStringWidth(date) / 1000 * 15 - 32)), pageH - 260, hnormal, 14, java.awt.Color.BLACK);
                String tfAdl = tf_adresse_livr.getText();
                String perso = tf_conta_perso.getText();
                String perso_phone = tf_phone_conta_perso.getText();
                pdf.addTextLine(new String[]{"Modalité de paiement : " + cbx_modapay.getValue(),
                    "Mode de paiement : " + cbx_paymode.getValue(), "Adresse de livraison : " + (tfAdl.isEmpty() ? entrep.getAdresse() : tfAdl), (perso.isEmpty() ? "" : "Personne de contact : " + perso), (perso_phone.isEmpty() ? "" : "Contact : " + perso_phone)}, 15, 25, pageH - 300, hnormal, 14, java.awt.Color.BLACK);
                //Tableau items
                int table[] = {55, 230, 100, 65, 90};
                pdf.addTable(table, 30, 25, pageH - 400);
                pdf.setFont(hnormal, 11, java.awt.Color.WHITE);
                pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                pdf.addCell("N#", endeleya);
                pdf.addCell("Désignation", endeleya);
                pdf.addCell("Quantité", endeleya);
                pdf.addCell("P.U.", endeleya);
                pdf.addCell("P. total", endeleya);
                pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
                contentStream.setFont(hnormal, 10);
                int i = 0;
                double somme = 0;
                int ln = 0;
                int lpp = 26;
                for (Rupture rupture : commandelist) {
                    i++;
                    ln++;
                    if (i > 13) {
                        if (i == 14 | ln == lpp) {
                            contentStream.close();
                            PDPage fPage2 = new PDPage(PDRectangle.A4);
                            document.addPage(fPage2);
                            contentStream = new PDPageContentStream(document, fPage2);
                            pdf = new PDFUtils(document, contentStream);
                            int tablex[] = {55, 230, 100, 65, 90};

                            pdf.addTable(tablex, 30, 25, pageH - 68);
                            pdf.setFont(hnormal, 10, java.awt.Color.BLACK);

                            pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                            contentStream.setFont(hnormal, 10);
                            if (ln == lpp || i == 14) {
                                ln = 0;
                            }
                        }
//
                    }

                    Produit x = rupture.getProduit();
                    pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                    pdf.addCell(i + ".", egray);
                    pdf.addCell(x.getNomProduit() + " "
                            + "" + x.getMarque() + " " + x.getModele() + " " + (x.getTaille() == null ? "" : x.getTaille()) + " " + (x.getCouleur() == null ? "" : x.getCouleur()), egray);
                    pdf.addCell(rupture.getQuant() + " " + rupture.getMesure().getDescription(), egray);
                    pdf.addCell(rupture.getUnitprice() + " " + cbx_devise.getValue(), egray);
                    somme += rupture.getQuant() * rupture.getUnitprice();
                    pdf.addCell(BigDecimal.valueOf(rupture.getQuant() * rupture.getUnitprice()).setScale(2, RoundingMode.HALF_EVEN) + " " + cbx_devise.getValue(), egray);

                }
                if (ln == lpp - 1 || ln == 0) {
                    contentStream.close();
                    PDPage fPage2 = new PDPage(PDRectangle.A4);
                    document.addPage(fPage2);
                    contentStream = new PDPageContentStream(document, fPage2);
                    pdf = new PDFUtils(document, contentStream);
                    int tablex[] = {55, 230, 100, 65, 90};
                    pdf.addTable(tablex, 30, 25, pageH - 68);
                    pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
                    pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                    contentStream.setFont(hnormal, 10);
                }
                pdf.addCell("", null);
                pdf.addCell("", null);
                pdf.addCell("", null);
                pdf.addCell("", null);
                pdf.addCell("", null);
                pdf.addCell("", null);
                pdf.addCell("", null);
                pdf.addCell("Total", egray);
                pdf.addCell("", egray);
                pdf.addCell(BigDecimal.valueOf(somme).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " " + cbx_devise.getValue(), egray);
                contentStream.close();
                bcmd = FileUtils.pointFile(System.currentTimeMillis() + ".pdf");
                document.save(bcmd);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Desktop.getDesktop().open(bcmd);
                    } catch (IOException ex) {
                        Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }).start();
        } catch (IOException ex) {
            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createEndedList(Entreprise entrep) {

        try {
            PDDocument document = new PDDocument();
            PDPage fPage = new PDPage(PDRectangle.A4);
            document.addPage(fPage);

            int pageW = (int) PDRectangle.A4.getWidth();//fPage.getTrimBox().getWidth();
            int pageH = (int) PDRectangle.A4.getHeight();//fPage.getTrimBox().getHeight();

            PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
            PDFUtils pdf = new PDFUtils(document, contentStream);

            //PDFont normalbold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            // PDFont normal = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
            PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            File f = FileUtils.pointFile(entrep.getUid() + ".png");
            InputStream is;
            if (!f.exists()) {
                is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                f = FileUtils.streamTofile(is);
            }

            java.awt.Color endeleya = new java.awt.Color(68, 206, 245);
            java.awt.Color egray = new java.awt.Color(218, 218, 219);
            PDImageXObject logo = PDImageXObject.createFromFile(f.getPath(), document);
            contentStream.drawImage(logo, 25, pageH - 97, 72, 72);
            //
            pdf.addTextLine(entrep.getNomEntreprise(), 125, pageH - 38, hnormal, 18, java.awt.Color.BLACK);
            pdf.addTextLine(new String[]{"Adresse : " + entrep.getAdresse(),
                "RCCM : " + entrep.getIdentification(), entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(),
                entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot()}, 15, 125, pageH - 52, hnormal, 14, java.awt.Color.BLACK);

            contentStream.setStrokingColor(endeleya);
            contentStream.setLineWidth(2);
            contentStream.moveTo(25, 700);
            contentStream.lineTo(pageW - 25, 700);
            contentStream.stroke();

            pdf.addTextLine("Liste de produit en rupture de stock ", 25, pageH - 320, hbold, 32, java.awt.Color.DARK_GRAY);

            String date = "Date : " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date());
            pdf.addTextLine(date, ((int) (pageW - hnormal.getStringWidth(date) / 1000 * 15 - 32)), pageH - 260, hnormal, 14, java.awt.Color.DARK_GRAY);

            //Tableau items
            int table[] = {55, 230, 100, 65, 90};

            pdf.addTable(table, 30, 25, pageH - 400);
            pdf.setFont(hnormal, 11, java.awt.Color.WHITE);

            pdf.setRightAlignedColumns(new int[]{2, 3, 4});

            pdf.addCell("N#", endeleya);
            pdf.addCell("Désignation", endeleya);
            pdf.addCell("Quantité", endeleya);
            pdf.addCell("P.U.", endeleya);
            pdf.addCell("Localisation", endeleya);
            pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
            contentStream.setFont(hnormal, 10);
            int i = 0;

            int ln = 0;
            int lpp = 26;
            for (Rupture rupture : tbl_rupture.getItems()) {
                i++;
                ln++;
                if (i > 13) {
                    if (i == 14 | ln == lpp) {
                        contentStream.close();
                        PDPage fPage2 = new PDPage(PDRectangle.A4);
                        document.addPage(fPage2);
                        contentStream = new PDPageContentStream(document, fPage2);
                        pdf = new PDFUtils(document, contentStream);
                        int tablex[] = {55, 230, 100, 65, 90};

                        pdf.addTable(tablex, 30, 25, pageH - 68);
                        pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
                        pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                        contentStream.setFont(hnormal, 10);
                        if (ln == lpp || i == 14) {
                            ln = 0;
                        }
                    }
//                 
                }

                Produit x = rupture.getProduit();
                pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                pdf.addCell(i + ".", egray);
                pdf.addCell(x.getNomProduit() + " "
                        + "" + x.getMarque() + " " + x.getModele() + " " + (x.getTaille() == null ? "" : x.getTaille()) + " " + (x.getCouleur() == null ? "" : x.getCouleur()), egray);
                pdf.addCell(rupture.getQuant() + " " + rupture.getMesure().getDescription(), egray);
                pdf.addCell(rupture.getUnitprice() + " $", egray);
                pdf.addCell(rupture.getLocalisation(), egray);
            }

            contentStream.close();

            final File bcmd = FileUtils.pointFile(System.currentTimeMillis() + ".pdf");
            document.save(bcmd);
            document.close();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Desktop.getDesktop().open(bcmd);
                    } catch (IOException ex) {
                        Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }).start();
        } catch (IOException ex) {
            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void raprocherStock(Event e) {
        Node node = (Node) e.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Correction des stocks depuis un fichier excel");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier excel", "*.xls", "*.xlsx"));
        final File choosenFile = fileChooser.showOpenDialog(thisStage);
        if (choosenFile != null) {
            pgsIndicator.setVisible(true);
            progs.setVisible(true);
            try {
                Executors.newSingleThreadExecutor()
                        .submit(() -> {
                            try {
                                System.out.println("Raprochement.....");
                                List<PhysicalInventoryLine> datas = Util.importInventoryFromExcelFile(choosenFile, cbx_region_maginv.getValue() == null ? region : cbx_region_maginv.getValue());
                                if (datas == null) {
                                    progs.setVisible(false);
                                    pgsIndicator.setVisible(false);
                                    return;
                                }
                                int size = datas.size();
                                Platform.runLater(() -> {
                                    progs.setText("0/" + size + " lignes");
                                });
                                System.out.println("Size pass datas if " + datas.size());
                                if (datas.isEmpty()) {
                                    MainUI.notify(null, "Error", "Ce fichier ne comporte pas les donnees valide d'inventaire de stock", 3, "error");
                                    pgsIndicator.setVisible(false);
                                    progs.setVisible(false);
                                    return;
                                }
                                System.out.println("Empty data ? " + datas.isEmpty());
                                DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                                String ref = "INV-#" + df.format(new Date());
                                List<LigneVente> lvs = new ArrayList<>();
                                Vente v = new Vente(DataId.generateInt());
                                v.setClientId(ClientDelegate.findImporterClient());
                                v.setDateVente(LocalDateTime.now());
                                v.setDeviseDette("USD");
                                v.setReference(ref);
                                v.setLatitude(0d);
                                v.setLongitude(0d);
                                v.setLibelle("Correction Inventaire");
                                v.setMontantUsd(0);
                                v.setMontantCdf(0);
                                v.setMontantDette(0d);
                                v.setObservation("CORRECTION");

                                for (PhysicalInventoryLine data : datas) {
                                    List<Produit> produits = ProduitDelegate.findAllByCodebar(data.getCodebarr());
                                    Produit produit;
                                    if (produits.isEmpty()) {
                                        produit = new Produit(DataId.generate());
                                        List<Category> cats = CategoryDelegate.findCategories("Divers");
                                        Category cat;
                                        if (cats.isEmpty()) {
                                            cat = CategoryDelegate.saveCategory(new Category(DataId.generate(), "Divers"));
                                        } else {
                                            cat = cats.get(0);
                                        }
                                        produit.setCategoryId(cat);
                                        produit.setNomProduit(data.getNomProduit());
                                        produit.setMarque(data.getMarqueProduit());
                                        produit.setModele(data.getModeleProduit());
                                        produit.setTaille(data.getTailleProduit());
                                        produit.setMethodeInventaire("FIFO");
                                        produit.setCodebar(data.getCodebarr());
                                        produit.setDateCreation(LocalDateTime.now());
                                        ProduitDelegate.saveProduit(produit);
                                    } else {
                                        produit = produits.get(0);
                                        List<Category> cats = CategoryDelegate.findCategories("Divers");
                                        Category cat;
                                        if (cats.isEmpty()) {
                                            cat = CategoryDelegate.saveCategory(new Category(DataId.generate(), "Divers"));
                                        } else {
                                            cat = cats.get(0);
                                        }
                                        produit.setCategoryId(cat);
                                        produit.setMarque(data.getMarqueProduit());
                                        produit.setModele(data.getModeleProduit());
                                        produit.setNomProduit(data.getNomProduit());
                                        produit.setTaille(data.getTailleProduit());
                                        produit.setMethodeInventaire("FIFO");
                                        produit.setCodebar(data.getCodebarr());
                                        produit.setDateCreation(LocalDateTime.now());
                                        ProduitDelegate.updateProduit(produit);

                                    }
                                    String mesure = data.getMesure();
                                    String descr;
                                    double quantM;
                                    if (mesure.contains(":")) {
                                        descr = mesure.split(":")[0];
                                        quantM = Double.parseDouble(mesure.split(":")[1]);
                                    } else {
                                        MainUI.notify(null, "Erreur", "La mesure du produit " + data.getNomProduit() + "  a la ligne " + data.getLigne() + " est mal notee", 3, "error");
                                        progs.setVisible(false);
                                        pgsIndicator.setVisible(false);
                                        return;
                                    }
                                    List<Mesure> mzs = MesureDelegate.findMesureByProduit(produit.getUid(), descr);
                                    Mesure mz;
                                    if (mzs.isEmpty()) {
                                        mz = new Mesure(DataId.generate());
                                        mz.setDescription(descr);
                                        mz.setQuantContenu(quantM);
                                        mz.setProduitId(produit);
                                        MesureDelegate.saveMesure(mz);
                                    } else {
                                        mz = mzs.get(0);
                                    }
                                    double rem;
                                    if (data.isMultiBatch()) {
                                        rem = RecquisitionDelegate.findRemainedInMagasinForBatched(produit.getUid(), data.getNumlot());
                                    } else {
                                        rem = RecquisitionDelegate.findRemainedInMagasinFor(produit.getUid());
                                    }
                                    double phys = data.getStockPhysique();
                                    double ecart = phys - (rem / quantM);
                                    System.out.println("Ecart " + ecart);
                                    if (ecart > 0) {
                                        //Nouveau Recquis
                                        List<Recquisition> rqs = RecquisitionDelegate.findDescSortedByDateForProduit(produit.getUid());
                                        Recquisition req = new Recquisition(DataId.generate());
                                        req.setCoutAchat(data.getCoutAchat());
                                        req.setDate(LocalDateTime.now());
                                        req.setQuantite(ecart);
                                        req.setRegion(data.getRegion());
                                        req.setDateExpiry(data.getDateExpiration());
                                        req.setMesureId(mz);
                                        req.setProductId(produit);
                                        req.setStockAlert(data.getStockAlerte());
                                        req.setNumlot(data.getNumlot());
                                        req.setObservation("Importation Inventaire");
                                        req.setReference(ref);
                                        RecquisitionDelegate.saveRecquisition(req);
                                        if (rqs.isEmpty()) {
                                            String pvs = data.getPrixDeVente();
                                            if (pvs != null) {
                                                if (!pvs.isEmpty()) {
                                                    if (pvs.startsWith("[") || !StringUtils.isNumeric(pvs)) {
                                                        if (pvs.contains(";") && pvs.contains("]")) {
                                                            pvs = pvs.replace("[", "");
                                                            pvs = pvs.replace("]", "");
                                                            String[] pvx = pvs.split(";");
                                                            for (String pv : pvx) {
                                                                if (pv.contains("-")) {
                                                                    String min = pv.split("-")[0];
                                                                    String max = pv.split("-")[1].split(":")[0];
                                                                    String px = pv.split("-")[1].split(":")[1];
                                                                    if (StringUtils.isNumeric(min) && StringUtils.isNumeric(max) && StringUtils.isNumeric(px)) {
                                                                        PrixDeVente prix = new PrixDeVente(DataId.generate());
                                                                        prix.setQmin(Double.valueOf(min));
                                                                        prix.setQmax(Double.valueOf(max));
                                                                        prix.setPrixUnitaire(Double.valueOf(px));
                                                                        prix.setDevise("USD");
                                                                        prix.setMesureId(mz);
                                                                        prix.setPourcentParCunit(0d);
                                                                        prix.setRecquisitionId(req);
                                                                        PrixDeVenteDelegate.savePrixDeVente(prix);
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            PrixDeVente prix = new PrixDeVente(DataId.generate());
                                                            prix.setQmin(0.1);
                                                            prix.setQmax(1000000d);
                                                            prix.setPrixUnitaire(Double.valueOf(pvs));
                                                            prix.setDevise("USD");
                                                            prix.setMesureId(mz);
                                                            prix.setPourcentParCunit(0d);
                                                            prix.setRecquisitionId(req);
                                                            PrixDeVenteDelegate.savePrixDeVente(prix);
                                                        }
                                                    } else {
                                                        MainUI.notify(null, "Erreur", "Le prix du produit " + data.getNomProduit() + "  a la ligne " + data.getLigne() + " est mal notee", 4, "error");
                                                        progs.setVisible(false);
                                                        pgsIndicator.setVisible(false);
                                                        return;
                                                    }
                                                }
                                            }
                                        } else {
                                            List<PrixDeVente> prices = PrixDeVenteDelegate.findPricesForRecq(rqs.get(0).getUid());
                                            for (PrixDeVente price : prices) {
                                                PrixDeVente prix = new PrixDeVente(DataId.generate());
                                                prix.setQmin(price.getQmin());
                                                prix.setQmax(price.getQmax());
                                                prix.setPrixUnitaire(price.getPrixUnitaire());
                                                prix.setDevise("USD");
                                                prix.setMesureId(mz);
                                                prix.setPourcentParCunit(0d);
                                                prix.setRecquisitionId(req);
                                                PrixDeVenteDelegate.savePrixDeVente(prix);
                                            }
                                        }
                                    } else if (ecart < 0) {
                                        //Nouvelle vente
                                        v.setRegion(data.getRegion());
                                        double q = Math.abs(ecart);
                                        LigneVente lv = new LigneVente(DataId.generateLong());
                                        lv.setClientId("-");
                                        lv.setMesureId(mz);
                                        lv.setMontantCdf(0d);
                                        lv.setMontantUsd(0);
                                        lv.setNumlot(data.getNumlot());
                                        lv.setPrixUnit(0d);
                                        lv.setProductId(produit);
                                        lv.setQuantite(q);
                                        lvs.add(lv);

                                        LigneVenteDelegate.saveLigneVente(lv, v);
                                    }
                                    Platform.runLater(() -> {
                                        progs.setText(data.getLigne() + "/" + (size + 12) + " lignes");
                                    });
                                    System.out.println("Ligne INV " + data.getLigne() + " enregistreee");
                                }

//                                createSafeVente(v);
                                progs.setVisible(false);
                                pgsIndicator.setVisible(false);
                                MainUI.notify(null, "Error", "Importation de l'inventaire terminée avec succès ", 3, "info");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                System.err.println("Exception dans l'importation inventaire " + ex.getMessage());
                            }
                        });
            } catch (RejectedExecutionException ez) {
                ez.printStackTrace();
            }

        }

    }

    private void createSafeVente(Vente v) {

//        ManagedSessionFactory.beginTransaction();
//        ManagedSessionFactory.getEntityManager().persist(v);
//        ManagedSessionFactory.commit();
    }

    @FXML
    public void refreshRupture(Event e) {
        chbx_xall.setSelected(false);
        Executors.newCachedThreadPool()
                .submit(() -> {
                    List<Rupture> ruptures;
                    if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                        ruptures = RecquisitionDelegate.findStockEnRupture();
                    } else {
                        ruptures = RecquisitionDelegate.findStockEnRupture(region);
                    }
                    obl_rupture_list.setAll(ruptures);
                    Platform.runLater(() -> {
                        rupt_count.setText(String.format(bundle.getString("xitems"), obl_rupture_list.size()));
                    });
                });
    }

    Set<Rupture> commandelist = new HashSet<>();

    @FXML
    public void saveRetourMarchandise(Event evt) {
        if (choosenVente != null) {
            String ref = choosenVente.getReference() + "-" + choosenVente.getUid();
            String obs = comment_retour.getText();
            String valueobs = (obs.isEmpty() ? "Retour de marchandise" : obs);
            double vtotret = 0;

            LigneVente elm = cbx_lgnvt_retour.getValue();

            double pv = elm.getPrixUnit();
            if (quant_retour.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "Veuillez entrer d'abord la quantité qui a été retournée puis réessayer", 4, "error");
                return;
            }
            if (!elm.getReference().equals(choosenVente)) {
                MainUI.notify(null, "Erreur", "Nous n'avons pas pu trouver, sur la facture, le produit selectionne", 4, "error");
                return;
            }
            double quant = Double.parseDouble(quant_retour.getText());
            vtotret += (pv * quant);
            List<Recquisition> rss = RecquisitionDelegate.findRecquisitionByProduit(elm.getProductId().getUid(), elm.getNumlot());//db.findByLot(Recquisition.class, elm.getNumlot());
//            if (!rss.isEmpty()) {
            Recquisition r = rss.get(0);
            Mesure rme = MesureDelegate.findMesure(r.getMesureId().getUid());
            double rqdiv = rme.getQuantContenu();

            double retqpc = quant * choosenMesure4Retour.getQuantContenu();
            RetourMagasin rmg = new RetourMagasin(DataId.generate());
            rmg.setClientId(choosenVente.getClientId());
            rmg.setDate(LocalDateTime.now());
            rmg.setLigneVenteId(elm);
            rmg.setMesureId(choosenMesure4Retour);
            rmg.setMotif(valueobs);
            rmg.setPrixVente(elm.getPrixUnit());
            rmg.setQuantite(quant);
            rmg.setReferenceVente(ref);
            rmg.setRegion(region);
            RetourMagasin rtr = RetourMagasinDelegate.saveRetourMagasin(rmg);
            if (rtr != null) {
                MainUI.notify(null, "Succes", "Retour de marchandise au magasin enregistré avec succes", 4, "info");
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(rmg, Constants.ACTION_CREATE, Tables.RETOURMAGASIN);
                        });
                double sum = elm.getPrixUnit() * quant;
                if (choosenVente.getPayment().equals(Constants.PAYEMENT_CREDIT)
                        || choosenVente.getPayment().equals(Constants.PAYMENT_CREDIT_CASH)) {
                    double vsum = choosenVente.getMontantDette() - sum;
                    if (vsum < 0) {
                        choosenVente.setMontantDette(0d);
                        double nvsum = choosenVente.getMontantUsd() - Math.abs(vsum);
                        if (nvsum < 0) {
                            choosenVente.setMontantUsd(0d);
                            choosenVente.setMontantCdf(choosenVente.getMontantCdf() - (Math.abs(nvsum) * taux2change));
                        } else {
                            choosenVente.setMontantUsd(nvsum);
                        }
                    } else {
                        choosenVente.setMontantDette(vsum);
                    }

                } else {
                    double vsum = choosenVente.getMontantUsd() - sum;
                    choosenVente.setMontantUsd(vsum);
                }
                long count = RetourMagasinDelegate.getCountForVente(ref);
                choosenVente.setLibelle("RTR (" + count + ")");
                Vente upd = VenteDelegate.updateVente(choosenVente);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(upd, Constants.ACTION_UPDATE, Tables.VENTE);
                        });
            }
//                Recquisition rretour = new Recquisition(DataId.generate());
//                rretour.setCoutAchat(r.getCoutAchat());
//                rretour.setDate(new Date());
//                rretour.setDateExpiry(r.getDateExpiry());
//                System.out.println("Mesure on recquisition " + rme + " quant " + elm.getQuantite());
//                rretour.setMesureId(rme);
//                rretour.setNumlot(elm.getNumlot());
//                rretour.setObservation(valueobs);
//                rretour.setProductId(elm.getProductId());
//                rretour.setQuantite(BigDecimal.valueOf(retqpc / rqdiv).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
//                rretour.setReference(ref);
//                rretour.setRegion(region);
//                rretour.setStockAlert(r.getStockAlert());
//                Executors.newCachedThreadPool()
//                        .submit(() -> {
//                            Util.sync(RecquisitionDelegate.saveRecquisition(rretour), Constants.ACTION_CREATE, Tables.RECQUISITION);
//                        });
            //db.insertAndSync(rretour);
//            }

        }
    }

    private double consolidate(Vente v) {
        double dt = v.getMontantDette() == null ? 0 : v.getMontantDette();
        return v.getMontantUsd() + (v.getMontantCdf() / taux2change) + dt;
    }

    private TypeTraisorerie mapPayment(String payment) {
        switch (payment) {
            case Constants.PAYEMENT_BANQUE:
                return TypeTraisorerie.BANQUE;
            case Constants.PAYMENT_CASH:
                return TypeTraisorerie.CAISSE;
            case Constants.PAYEMENT_CREDIT:
                return null;
            default:
                return TypeTraisorerie.ELECTRONIQUE;
        }
    }
    double valeurs = 0;

    private void enPeremption(Date darg) {
        Calendar leo = Calendar.getInstance();
        leo.setTime(new Date());
        Calendar cexp = Calendar.getInstance();
        cexp.set(Calendar.DAY_OF_MONTH, 31);
        cexp.set(Calendar.MONTH, Calendar.DECEMBER);
        cexp.set(Calendar.YEAR, leo.get(Calendar.YEAR));
        cexp.set(Calendar.HOUR, 23);
        cexp.set(Calendar.MINUTE, 59);
        cexp.set(Calendar.SECOND, 59);
        cexp.set(Calendar.MILLISECOND, 0);
        List<Stocker> stks = StockerDelegate.findByDateExpInterval(Constants.Datetime.toLocalDate(leo.getTime()), Constants.Datetime.toLocalDate(darg == null ? cexp.getTime() : darg));
        List<Recquisition> reks = RecquisitionDelegate.findByDateExpInterval(Constants.Datetime.toLocalDate(leo.getTime()), Constants.Datetime.toLocalDate(darg == null ? cexp.getTime() : darg));
        ols_peremption.clear();
        valeurs = 0;
        for (Stocker stk : stks) {
            List<Destocker> dsks = DestockerDelegate.findByProduitLot(stk.getProductId().getUid(), stk.getNumlot());
            double sommedqpc = 0;
            for (Destocker dsk : dsks) {
                Mesure mz = MesureDelegate.findMesure(dsk.getMesureId().getUid());
                if (mz == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(dsk.getProductId().getUid());
                    mz = mesures.get(0);
                }
                sommedqpc += dsk.getQuantite() * mz.getQuantContenu();
            }
            Mesure mz = MesureDelegate.findMesure(stk.getMesureId().getUid());//db.findByUid(Mesure.class, stk.getMesureId().getUid());
            if (mz == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(stk.getProductId().getUid());// db.findByProduitAsc(Mesure.class, stk.getProductId().getUid(), "quantContenu");
                mz = mesures.get(0);
            }
            double qc = mz.getQuantContenu();
            double sqpc = stk.getQuantite() * qc;
            double reste = sqpc - sommedqpc;
            if (reste > 0) {
                Peremption per = new Peremption();
                Produit pr = ProduitDelegate.findProduit(stk.getProductId().getUid());
                per.setCodebar(pr.getCodebar());
                per.setProduit(pr.getNomProduit() + " " + pr.getModele() + " "
                        + pr.getMarque() + " " + pr.getTaille() + " " + pr.getCouleur());
                per.setCoutAchat(stk.getCoutAchat());
                per.setLocalisation("Depot : " + stk.getLocalisation());
                per.setMesure(mz.getDescription());
                per.setQuantite(reste / qc);
                per.setRegion(stk.getRegion());
                per.setDateExpiry(stk.getDateExpir());
                per.setValeur(per.getQuantite() * stk.getCoutAchat());
                per.setLot(stk.getNumlot());
                valeurs += per.getValeur();
                ols_peremption.add(per);
            }
        }
        for (Recquisition rek : reks) {
            List<LigneVente> lvs = LigneVenteDelegate.findByProduitWithLot(rek.getProductId().getUid(), rek.getNumlot());//db.findByLot(LigneVente.class, rek.getNumlot());
            double sommedqpc = 0;
            for (LigneVente lv : lvs) {
                Mesure mz = MesureDelegate.findMesure(lv.getMesureId().getUid());
                if (mz == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(lv.getProductId().getUid());//db.findByProduitAsc(Mesure.class, lv.getProductId().getUid(), "quantContenu");
                    mz = mesures.get(0);
                }
                sommedqpc += lv.getQuantite() * mz.getQuantContenu();
            }
            Mesure mz = MesureDelegate.findMesure(rek.getMesureId().getUid());
            if (mz == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(rek.getProductId().getUid());// db.findByProduitAsc(Mesure.class, rek.getProductId().getUid(), "quantContenu");
                mz = mesures.get(0);
            }
            double qc = mz.getQuantContenu();
            double sqpc = rek.getQuantite() * qc;
            double reste = sqpc - sommedqpc;
            if (reste > 0) {
                Peremption per = new Peremption();
                Produit pr = ProduitDelegate.findProduit(rek.getProductId().getUid());
                per.setCodebar(pr.getCodebar());
                per.setProduit(pr.getNomProduit() + " " + pr.getModele() + " "
                        + pr.getMarque() + " " + pr.getTaille() + " " + pr.getCouleur());
                per.setCoutAchat(rek.getCoutAchat());
                per.setLocalisation("Point de Vente : " + rek.getRegion());
                per.setMesure(mz.getDescription());
                per.setQuantite(reste / qc);
                per.setRegion(rek.getRegion());
                per.setDateExpiry(rek.getDateExpiry());
                per.setValeur(per.getQuantite() * rek.getCoutAchat());
                per.setLot(rek.getNumlot());
                valeurs += per.getValeur();
                ols_peremption.add(per);
            }
        }

        Platform.runLater(() -> {
            countPeremption.setText(ols_peremption.size() + " elements");
            valeurPremption.setText("Valeur total du stock : " + valeurs + " USD");
        });
    }

    @FXML
    private void syncdown(Event e) {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    Refresher rfr = new Refresher("SALES");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Util.sync(rfr, "read", Tables.REFRESH);
                });
    }

    @FXML
    private void searchByDate(Event e) {
        Date dateCh = Constants.Datetime.toUtilDate(datePremption.getValue());
        enPeremption(dateCh);
    }

    @FXML
    private void exportSales(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsInv = Util.exportXlsSales(ttable_ventes_hyst.getRoot().getChildren());
                try {
                    Desktop.getDesktop().open(xlsInv);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    HashMap<String, String> hmap = new HashMap<String, String>();

    @FXML
    private void exportInventoryMag(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                hmap.put("entrep", pref.get("ent_name", entreprise.getNomEntreprise()));
                hmap.put("rccm", entreprise.getIdentification());
                hmap.put("eUid", pref.get("eUid", entreprise.getUid()));
                String reg = cbx_region_maginv.getValue();
                hmap.put("region", reg == null ? "Tout" : reg);
                LocalDate ldd = dpk_debut_inv_mag.getValue();
                LocalDate ldf = dpk_fin_inv_mag.getValue();
                hmap.put("debut", ldd == null ? "Depuis l'installation"
                        : Constants.USER_READABLE_FORMAT.format(Constants.Datetime.toUtilDate(ldd)));
                hmap.put("fin", ldf == null ? "Aujourd'hui"
                        : Constants.USER_READABLE_FORMAT.format(Constants.Datetime.toUtilDate(ldf)));
                hmap.put("operateur", pref.get("operator", "Uknown"));

                File xlsInv = Util.exportXlsInventoryMagasin(hmap, table_inv_mag.getItems(), pref.get("mainCur", "USD"));
                try {
                    Desktop.getDesktop().open(xlsInv);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @FXML
    private void goToClient(Event e) {
        String token = pref.get("token", null);
        MainUI.floatDialog(tools.Constants.CLIENT_DLG, 1090, 489, token, kazisafe, entreprise, region);
    }

    private ScrollBar getVScrollbar(TableView<ListViewItem> table) {
        ScrollBar result = null;
        for (Node n : table.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }
        return result;
    }

    @FXML
    private void exportRecquisition(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsInv = Util.exportXlsRecquisition(table_req.getItems());
                try {
                    Desktop.getDesktop().open(xlsInv);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void configList() {
        panier_list.setCellFactory((ListView<LigneVente> param) -> new ListCell<LigneVente>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(LigneVente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Produit p = ProduitDelegate.findProduit(item.getProductId().getUid());
                    setText(p.getNomProduit() + " " + (p.getMarque() == null ? "" : p.getMarque()) + "-" + (p.getModele() == null ? "" : p.getModele()) + " " + item.getQuantite() + ""
                            + " " + item.getMesureId().getDescription() + " à " + item.getPrixUnit() + " USD : " + item.getMontantUsd());
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    InputStream is;
                    try {
                        is = FileUtils.fileToStream(p.getUid() + ".jpeg");
                    } catch (FileNotFoundException ex) {
                        is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                    }
                    imageView.setImage(new Image(is));
                    setGraphic(imageView);
                }
            }

        });
        ContextMenu context = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Modifier");
        MenuItem menuItem2 = new MenuItem("Supprimer");
        panier_list.setContextMenu(context);
        context.getItems().add(menuItem1);
        context.getItems().add(menuItem2);
        menuItem1.setOnAction((ActionEvent event) -> {
            Produit p = choosenLv.getProductId();
            MainUI.floatDialog(tools.Constants.PANIER_DLG, 430, 497, null, kazisafe, p, entreprise, "Modif", choosenLv.getUid());
            lslgnventes.remove(choosenLv);
            String dev = pref.get("mainCur", "USD");
            savedSum = Util.sumCart(lslgnventes, dev);
            if (dev.equals("CDF")) {
                txt_panier_total.setText("Total : " + BigDecimal.valueOf(savedSum).setScale(0, RoundingMode.HALF_EVEN).doubleValue() + "  CDF");
            } else {
                txt_panier_total.setText("Total : " + savedSum + "  USD");
            }

        });
        menuItem2.setOnAction((ActionEvent event) -> {
            lslgnventes.remove(choosenLv);
            String dev = pref.get("mainCur", "USD");
            savedSum = Util.sumCart(lslgnventes, dev);
            if (dev.equals("CDF")) {
                txt_panier_total.setText("Total : " + BigDecimal.valueOf(savedSum).setScale(0, RoundingMode.HALF_EVEN).doubleValue() + "  CDF");
            } else {
                txt_panier_total.setText("Total : " + savedSum + "  USD");
            }
            btn_pay_now.setDisable(lslgnventes.isEmpty());
        });
        panier_list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LigneVente>() {
            @Override
            public void changed(ObservableValue<? extends LigneVente> observable, LigneVente oldValue, LigneVente newValue) {
                choosenLv = newValue;
            }
        });

    }

    private SimpleStringProperty getValues(String s) {
        return new SimpleStringProperty(s == null ? "" : s);
    }

    private void configViewlist() {
        col_codebar_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getProduit().getCodebar());

        });
        col_nomprod_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getProduit().getNomProduit());

        });
        col_marque_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getProduit().getMarque());

        });
        col_modele_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getProduit().getModele());
        });
        col_date_expir_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getPeremption() == null ? "Non perissable" : r.getPeremption().toString());
        });
        col_taille_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getProduit().getTaille());
        });
        col_quanite_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            double rest = r.getQuantiteRestant();
//            if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
//                rest = RecquisitionDelegate.findRemainedInMagasinFor(r.getProduit().getUid());
//            } else {
//                rest = RecquisitionDelegate.findRemainedInMagasinFor(r.getProduit().getUid(), region);
//            }
            ////            r.getQuantiteRestant();
            Mesure m = r.getMesureAchat();
            return new SimpleStringProperty(rest + " " + m.getDescription());
        });
        col_purchaseprice_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            double rest = r.getPurchasePrice();
            Mesure m = r.getMesureAchat();
            return new SimpleStringProperty(rest + "/" + m.getDescription());
        });

        col_saleprice_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            Mesure m = r.getMesureGros();
            return new SimpleStringProperty(r.getSalePrice() + "/" + m.getDescription());
        });

        col_saleprice_detail.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            if (r == null) {
                return null;
            }
            Double det = r.getDetailPrice();
            Mesure m = r.getMesureDetail();
            return new SimpleStringProperty((det == null ? 0 : det) + "/" + m.getDescription());
        });

        col_numlot_view.setCellValueFactory((TableColumn.CellDataFeatures<ListViewItem, String> param) -> {
            ListViewItem r = param.getValue();
            return new SimpleStringProperty(r.getNumlot());
        });
        chkbx_declasser_panier.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    btn_pay_now.setText("DECLASSER MAINTENANT");
                } else {
                    btn_pay_now.setText("PAYER MAINTENANT");
                }
            }
        });

    }

    private void config() {
        col_alerte_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            Mesure mz = r.getMesureId();
//            Mesure mz = MesureDelegate.findMesure(mz.getUid());
            double sa = r.getStockAlert() == null ? 0 : r.getStockAlert();
            Double qc = mz == null ? mz.getQuantContenu() : mz.getQuantContenu();
            Mesure mm;
            if (qc == null) {
                mm = MesureDelegate.findMesure(mz.getUid());
                qc = mm.getQuantContenu();
            }
            SimpleStringProperty ex = getValues((sa / qc) + " " + (mz == null ? mz.getDescription() : mz.getDescription()));
            return ex;

        });
        col_quant_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            Mesure mx = r.getMesureId();
//            Mesure m = MesureDelegate.findMesure(mx.getUid());
            return new SimpleStringProperty(r.getQuantite() + " " + (mx == null ? "" : mx.getDescription()));
        });
        col_date_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            return new SimpleStringProperty(param.getValue().getDate().toString());
        });
        col_expiry_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            return new SimpleStringProperty(r.getDateExpiry() == null ? "" : r.getDateExpiry().toString());
        });
        col_prod_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            Produit x = r.getProductId();
            if (x == null) {
                return null;
            }
            Produit p = ProduitDelegate.findProduit(x.getUid());
            if (p == null) {
                return null;
            }
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " "
                    + (p.getTaille() == null ? "" : p.getTaille()) + " " + (p.getCouleur() == null ? "" : p.getCouleur()));
        });
        col_ref_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            return new SimpleStringProperty(r.getReference());
        });
        col_coutunit_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, Number> param) -> {
            Recquisition r = param.getValue();
            return new SimpleDoubleProperty(r.getCoutAchat());
        });
        col_coutotal_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, Number> param) -> {
            Recquisition r = param.getValue();
            return new SimpleDoubleProperty(BigDecimal.valueOf(r.getCoutAchat() * r.getQuantite()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        });
        col_numlot_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            return new SimpleStringProperty(r.getNumlot());
        });
        col_oberv_req.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Recquisition r = param.getValue();
            return new SimpleStringProperty(r.getObservation());
        });

        //Peremption
        col_codebar_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getCodebar());
        });
        col_produit_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getProduit());
        });
        col_quantite_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getQuantite() + " " + p.getMesure());
        });
        col_lot_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getLot());
        });
        col_date_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getDateExpiry().toString());
        });
        col_localisation_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getLocalisation());
        });
        col_region_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, String> param) -> {
            Peremption p = param.getValue();
            return new SimpleStringProperty(p.getRegion());
        });
        col_coutachat_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, Number> param) -> {
            Peremption p = param.getValue();
            return new SimpleDoubleProperty(p.getCoutAchat());
        });
        col_valeur_exp.setCellValueFactory((TableColumn.CellDataFeatures<Peremption, Number> param) -> {
            Peremption p = param.getValue();
            return new SimpleDoubleProperty(p.getValeur());
        });

        //#Cart saving
        //+ Rupture
        tbl_rupt_date.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, String> param) -> {
            Rupture p = param.getValue();
            return new SimpleStringProperty(p.getDate());
        });
        tbl_rupt_produit.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, String> param) -> {
            Rupture r = param.getValue();
            Produit p = r.getProduit();
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getMarque() + " "
                    + "" + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()) + " "
                    + "" + (p.getCouleur() == null ? "" : p.getCouleur()));

        });
        tbl_rupt_localisation.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, String> param) -> {
            Rupture p = param.getValue();
            return new SimpleStringProperty(p.getLocalisation());
        });
        tbl_rupt_mesure.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, String> param) -> {
            Rupture p = param.getValue();
            Mesure m = p.getMesure();
            return new SimpleStringProperty(m.getDescription());
        });
        tbl_rupt_region.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, String> param) -> {
            Rupture p = param.getValue();
            return new SimpleStringProperty(p.getRegion());
        });
        tbl_rupt_quant.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, Number> param) -> {
            Rupture p = param.getValue();
            return new SimpleDoubleProperty(p.getQuant());
        });
        tbl_rupt_quant.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        tbl_rupt_uprice.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, Number> param) -> {
            Rupture p = param.getValue();
            return new SimpleDoubleProperty(p.getUnitprice());
        });
        tbl_rupt_uprice.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        tbl_rupt_quant.setOnEditCommit((TableColumn.CellEditEvent<Rupture, Number> event) -> {
            Rupture vl = event.getRowValue();
            vl.setQuant(event.getNewValue().doubleValue());
            cmdvalue += (vl.getUnitprice() * vl.getQuant());
            valeur_tot.setText("Total :" + cmdvalue + " USD");
        });
        tbl_rupt_uprice.setOnEditCommit((TableColumn.CellEditEvent<Rupture, Number> event) -> {
            Rupture vl = event.getRowValue();
            vl.setUnitprice(event.getNewValue().doubleValue());
            cmdvalue += (vl.getUnitprice() * vl.getQuant());
            valeur_tot.setText("Total : " + cmdvalue + " USD");
        });

        tbl_rupt_tprice.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, Number> param) -> {
            Rupture p = param.getValue();
            return new SimpleDoubleProperty(p.getQuant() * p.getUnitprice());
        });

        tbl_rupt_alert.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, Number> param) -> {
            Rupture p = param.getValue();
            return new SimpleDoubleProperty(p.getAlert());
        });
        tbl_rupt_select.setCellValueFactory((TableColumn.CellDataFeatures<Rupture, Boolean> param) -> {
            Rupture p = param.getValue();
            SimpleBooleanProperty sbp = new SimpleBooleanProperty(p.isSelect());
            sbp.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    p.setSelect(newValue);
                    if (newValue) {
                        commandelist.add(p);
                    } else {
                        commandelist.remove(p);
                    }
                }
            });
            return sbp;
        });
        tbl_rupt_select.setCellFactory((TableColumn<Rupture, Boolean> param) -> {
            CheckBoxTableCell<Rupture, Boolean> slx = new CheckBoxTableCell<>();
            slx.setAlignment(Pos.CENTER);
            return slx;
        });

        //- Rupture
    }

    private void confDraft() {
        cart_reference.setCellValueFactory((TableColumn.CellDataFeatures<Vente, String> param) -> {
            Vente r = param.getValue();
            return new SimpleStringProperty(r.getReference());
        });

        cart_produx.setCellValueFactory((TableColumn.CellDataFeatures<Vente, String> param) -> {
            Vente r = param.getValue();
            List<LigneVente> content = LigneVenteDelegate.findByReference(r.getUid());
            return new SimpleStringProperty(String.format(bundle.getString("xitems"), content.size()));
        });
        cart_montant_cdf.setCellValueFactory((TableColumn.CellDataFeatures<Vente, String> param) -> {
            Vente v = param.getValue();
            return new SimpleStringProperty(v.getMontantCdf() + " CDF");
        });
        cart_montant_usd.setCellValueFactory((TableColumn.CellDataFeatures<Vente, String> param) -> {
            Vente r = param.getValue();
            return new SimpleStringProperty(r.getMontantUsd() + " USD");
        });
        cart_libelle.setCellValueFactory((TableColumn.CellDataFeatures<Vente, String> param) -> {
            Vente r = param.getValue();
            return new SimpleStringProperty(r.getLibelle());
        });
        cart_date.setCellValueFactory((TableColumn.CellDataFeatures<Vente, String> param) -> {
            Vente r = param.getValue();
            return new SimpleStringProperty(r.getDateVente().toString());
        });
    }

    private void conf() {
        col_codebar_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            return new SimpleStringProperty(im.getProduit().getCodebar());
        });
        col_prod_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            Produit p = im.getProduit();
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + p.getTaille() + " " + p.getCouleur());
        });
        col_entree_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            Mesure mx = im.getMesure();
            Mesure mes = MesureDelegate.findMesure(mx.getUid());
            if (mes == null) {
                mes = mx;
            }
            return new SimpleStringProperty((im.getQuantEntree() / mes.getQuantContenu()) + " " + mes.getDescription());
        });
        col_sortie_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            Mesure mx = im.getMesure();
            Mesure mes = MesureDelegate.findMesure(mx.getUid());
            //Util.findMesure(db.findAll(), mx);
            if (mes == null) {
                mes = mx;
            }
            return new SimpleStringProperty((im.getQuantSortie() / mes.getQuantContenu()) + " " + mes.getDescription());
        });
        col_stock_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            Mesure mx = im.getMesure();
            Mesure mes = MesureDelegate.findMesure(mx.getUid());
            // Util.findMesure(db.findAll(), mx);
            if (mes == null) {
                mes = mx;
            }
            return new SimpleStringProperty((im.getQuantStock() / mes.getQuantContenu()) + " " + mes.getDescription());
        });
        col_val_stock_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            return new SimpleStringProperty(im.getValeurStock() + " USD");
        });
        col_alert_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            Mesure mx = im.getMesure();
            Mesure mes = MesureDelegate.findMesure(mx.getUid());
            //Util.findMesure(db.findAll(), mx);
            if (mes == null) {
                mes = mx;
            }
            return new SimpleStringProperty((im.getAlerte() / mes.getQuantContenu()) + " " + mes.getDescription());
        });
        col_lot_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            return new SimpleStringProperty(im.getLot());
        });
        col_expiry_tInv_mag.setCellValueFactory((TableColumn.CellDataFeatures<InventoryMagasin, String> param) -> {
            InventoryMagasin im = param.getValue();
            LocalDate dex = im.getExpiry();
            return new SimpleStringProperty(dex == null ? "" : Constants.USER_READABLE_FORMAT.format(Constants.Datetime.toUtilDate(dex)));
        });
    }

    private void cnft() {
        trcol_pu_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, Number> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            return new SimpleDoubleProperty(value.getUnitPrice());
        });
        trcol_quants_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            double q = value.getQuantite();
            Mesure mx = value.getMesureObj();
            return new SimpleStringProperty(q + " " + ((mx == null) ? "" : mx.getDescription()));
        });
        trcol_facture_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            return new SimpleStringProperty(value.getFacture());
        });
        trcol_produits_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            Produit p = value.getProduit();
            return new SimpleStringProperty(p == null && value.getItems() == null ? "+ des factures" : p == null && value.getItems() != null ? value.getItems().size() + " produit(s)" : p.getNomProduit() + " " + p.getMarque() + ""
                    + " " + p.getModele() + " " + p.getCouleur() + " " + p.getTaille());
        });

        trcol_date_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem vls = param.getValue().getValue();
            if (vls == null) {
                return null;
            }
            Date date = vls.getDate();
            return new SimpleStringProperty(date == null ? "" : tools.Constants.DATE_HEURE_USER_READABLE_FORMAT.format(date));
        });

        trcol_client_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            Client c = value.getClient();
            return new SimpleStringProperty(c == null ? "" : (c.getPhone().length() < 8
                    ? c.getNomClient() : c.getPhone().equals("09000") ? "Anonyme" : c.getNomClient() + ",Tel:" + c.getPhone()));
        });
        trcol_totalusd_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, Number> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            return new SimpleDoubleProperty(value.getSaleAmountUsd());
        });
        trcol_totalcdf_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, Number> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            return new SimpleDoubleProperty(value.getSaleAmountCdf());
        });
        trcol_dette_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, Number> param) -> {
            SaleItem value = param.getValue().getValue();
            if (value == null) {
                return null;
            }
            return new SimpleDoubleProperty(value.getSaleAmountCredit());
        });
        trcol_echeance_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem vls = param.getValue().getValue();
            if (vls == null) {
                return null;
            }
            LocalDate date = vls.getDatEcheance();
            return new SimpleStringProperty(date == null ? "" : date.toString());
        });

        trcol_libelle_hyst.setCellValueFactory((TreeTableColumn.CellDataFeatures<SaleItem, String> param) -> {
            SaleItem vls = param.getValue().getValue();
            if (vls == null) {
                return null;
            }
            return new SimpleStringProperty(vls.getLibelle());
        });

        ttable_ventes_hyst.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<SaleItem>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<SaleItem>> observable, TreeItem<SaleItem> oldValue, TreeItem<SaleItem> newValue) {
                if (newValue == null) {
                    return;
                }
                SaleItem si = newValue.getValue();

                choosenVente = VenteDelegate.findVente(si.getIdVente());

            }
        });
        cbx_lgnvt_retour.setConverter(new StringConverter<LigneVente>() {
            @Override
            public String toString(LigneVente object) {
                if (object == null) {
                    return null;
                }
                Produit p = ProduitDelegate.findProduit(object.getProductId().getUid());
                return p == null ? null : p.getNomProduit() + " " + (p.getMarque() == null ? "" : p.getMarque()) + " " + (p.getModele() == null ? "" : p.getModele())
                        + " " + (p.getTaille() == null ? "" : p.getTaille() + " ") + p.getCodebar();
            }

            @Override
            public LigneVente fromString(String string) {
                return cbx_lgnvt_retour.getItems()
                        .stream()
                        .filter(p -> (p.getProductId().getNomProduit() + " " + (p.getProductId().getMarque() == null ? "" : p.getProductId().getMarque()) + " " + (p.getProductId().getModele() == null ? "" : p.getProductId().getModele())
                        + " " + (p.getProductId().getTaille() == null ? "" : p.getProductId().getTaille() + " ") + p.getProductId().getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_lgnvt_retour.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<LigneVente>() {
                    @Override
                    public void changed(ObservableValue<? extends LigneVente> observable, LigneVente oldValue, LigneVente newValue) {
                        if (newValue != null) {
                            quant_retour.setText(String.valueOf(newValue.getQuantite()));
                            List<Mesure> lmr = MesureDelegate.findMesureByProduit(newValue.getProductId().getUid());
                            ols_mesure_retour.addAll(lmr);
                            mesure_retour.getSelectionModel().select(newValue.getMesureId());
                        }
                    }
                });
        mesure_retour.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return mesure_retour.getItems()
                        .stream()
                        .filter(p -> (p.getDescription().equalsIgnoreCase(string)))
                        .findFirst().orElse(null);
            }
        });

        cbx_produit_retour_depot.setConverter(new StringConverter<Recquisition>() {
            @Override
            public String toString(Recquisition object) {
                Produit p = null;
                if (object != null) {
                    p = ProduitDelegate.findProduit(object.getProductId().getUid());
                }
                return p == null ? null : p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + p.getTaille() + " " + p.getCouleur() + " " + object.getNumlot();
            }

            @Override
            public Recquisition fromString(String string) {
                for (Recquisition r : cbx_produit_retour_depot.getItems()) {
                    Produit x = ProduitDelegate.findProduit(r.getProductId().getUid());
                    if ((x.getNomProduit() + " " + x.getMarque()
                            + " " + x.getModele() + " " + x.getTaille()
                            + " " + x.getCouleur() + " " + r.getNumlot()).equalsIgnoreCase(string)) {
                        return r;
                    }
                }
                return null;
            }
        });

        mesure_retour.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Mesure>() {
                    @Override
                    public void changed(ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) {
                        if (newValue != null) {
                            choosenMesure4Retour = newValue;
                        }
                    }
                });
        listvu_choosen4_retour_depot.setCellFactory((ListView<InventoryMagasin> param) -> new ListCell<InventoryMagasin>() {

            @Override
            protected void updateItem(InventoryMagasin item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Produit p = ProduitDelegate.findProduit(item.getProduit().getUid());
                    Mesure mes = MesureDelegate.findMesure(item.getMesure().getUid());
                    setText(p.getNomProduit() + " " + p.getMarque() + "-" + p.getModele() + " " + p.getTaille() + " " + p.getCouleur() + " : " + item.getQuantStock() + " " + mes.getDescription() + " valant " + item.getValeurStock() + " USD ");
                }
            }
        });

        ContextMenu cm = new ContextMenu();
        MenuItem midel = new MenuItem("Supprimer");
        cm.getItems().add(midel);
        midel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ols_retours_invent_items.remove(listvu_choosen4_retour_depot.getSelectionModel().getSelectedItem());
            }
        });
        listvu_choosen4_retour_depot.setContextMenu(cm);
        cbx_produit_retour_depot.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Recquisition>() {
                    @Override
                    public void changed(ObservableValue<? extends Recquisition> observable, Recquisition oldValue, Recquisition newValue) {
                        if (newValue != null) {
                            Mesure rm = newValue.getMesureId();
                            List<Mesure> lms = MesureDelegate.findMesureByProduit(newValue.getProductId().getUid());
                            ols_mesure_retour_depot.addAll(lms);
                            double qrest = lotDiff(newValue.getProductId().getUid(), newValue.getNumlot());
                            final Mesure cmz = MesureDelegate.findMesure(rm.getUid());
                            Mesure mx = cmz == null ? Util.findMesure(lms, rm) : cmz;

                            double qimz = (qrest / (mx == null ? 1 : mx.getQuantContenu()));

                            Platform.runLater(() -> {
                                cbx_mesure_retour_depot.getSelectionModel().select(cmz);
                                tf_quant_retour_depot.setText(Double.toString(qimz));
                            });
                        }
                    }
                });

        cbx_mesure_retour_depot.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return mesure_retour.getItems()
                        .stream()
                        .filter(p -> (p.getDescription().equalsIgnoreCase(string)))
                        .findFirst().orElse(null);
            }
        });
    }
    double sommeRtr = 0;

    @FXML
    public void addRetourMarchandise(Event e) {
        Recquisition chosenReqRet = cbx_produit_retour_depot.getValue();
        if (chosenReqRet != null) {
            Mesure mr = cbx_mesure_retour_depot.getValue();
            Produit p = ProduitDelegate.findProduit(chosenReqRet.getProductId().getUid());
            InventoryMagasin invm = new InventoryMagasin();
            invm.setProduit(p);
            invm.setMesure(mr);
            double qt = Double.parseDouble(tf_quant_retour_depot.getText());
            invm.setQuantStock(qt);
            double ctach = chosenReqRet.getCoutAchat();
            Mesure mreq = MesureDelegate.findMesure(chosenReqRet.getMesureId().getUid());
            double caparpc = ctach / mreq.getQuantContenu();
            double pctot = (qt * mr.getQuantContenu());
            double patpc = pctot * caparpc;
            invm.setValeurStock(patpc);
            Recquisition newr = new Recquisition(DataId.generate());
            newr.setCoutAchat(caparpc * mr.getQuantContenu());
            newr.setDate(LocalDateTime.now());
            newr.setDateExpiry(chosenReqRet.getDateExpiry());
            newr.setMesureId(mr);
            newr.setNumlot(chosenReqRet.getNumlot());
            newr.setObservation("Retour au depot");
            newr.setProductId(p);
            newr.setQuantite(qt);
            newr.setReference("RRTR");
            newr.setRegion(region);
            newr.setStockAlert(chosenReqRet.getStockAlert());
            listDeRetourDepot.add(newr);
            ols_retours_invent_items.add(invm);
            sommeRtr += patpc;
        }
    }

    @FXML
    public void saveRetourAuDepot(Event e) {
        if (!listDeRetourDepot.isEmpty()) {
            String ref = "DRTR" + ((int) (Math.random() * 1000000));
            Livraison livr = new Livraison(DataId.generate());
            livr.setDateLivr(LocalDate.now());
            livr.setLibelle("Retour de stock");
            livr.setNumPiece(ref);
            livr.setObservation("Non confirme");
            livr.setPayed(sommeRtr);
            livr.setReduction(0d);
            livr.setReference(ref);
            livr.setRegion(cbx_region_retour_depot.getValue());
            livr.setRemained(0d);
            livr.setTopay(sommeRtr);
            livr.setToreceive(0d);
            Fournisseur f = new Fournisseur(this.entreprise.getUid());
            f.setAdresse(this.entreprise.getAdresse());
            f.setIdentification(this.entreprise.getIdentification());
            f.setNomFourn(this.entreprise.getNomEntreprise());
            f.setPhone(this.entreprise.getPhones());
            Fournisseur fins = FournisseurDelegate.saveFournisseur(f);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(fins, Constants.ACTION_CREATE, Tables.FOURNISSEUR);
                    });
            livr.setFournId(fins);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(LivraisonDelegate.saveLivraison(livr), Constants.ACTION_CREATE, Tables.LIVRAISON);
                    });
            //db.insertAndSync(livr);
            for (Recquisition rec : listDeRetourDepot) {
                Stocker s = new Stocker(DataId.generate());
                s.setCoutAchat(rec.getCoutAchat());
                s.setDateExpir(rec.getDateExpiry());
                s.setDateStocker(rec.getDate());
                s.setLibelle("Retour de stock");
                s.setLivraisId(livr);
                s.setLocalisation("depot");
                Mesure mzr = MesureDelegate.findMesure(rec.getMesureId().getUid());
                s.setMesureId(mzr);
                s.setNumlot(rec.getNumlot());
                s.setObservation(rec.getObservation());
                rec.setReference(ref);
                double q = rec.getQuantite();
                s.setQuantite(rec.getQuantite());
                q = q * -1;
                rec.setQuantite(q);
                s.setPrixAchatTotal(s.getQuantite() * s.getCoutAchat());
                s.setProductId(rec.getProductId());
                s.setReduction(0d);
                s.setRegion(cbx_region_retour_depot.getValue());
                s.setStockAlerte(rec.getStockAlert());
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(RecquisitionDelegate.saveRecquisition(rec), Constants.ACTION_CREATE, Tables.RECQUISITION);//db.insertAndSync(rec);
                            Util.sync(StockerDelegate.saveStocker(s), Constants.ACTION_CREATE, Tables.STOCKER);//db.insertAndSync(s);
                        });
            }
            MainUI.notify(null, "Succes", "Stock retourne au depot avec success", 4, "info");
            sommeRtr = 0;
            closeFloatingPane(e);
        }
    }

    private String getLocation(String idpro) {
        List<Stocker> loc = StockerDelegate.findDescSortedByDateStock(idpro);
        if (loc.isEmpty()) {
            return region;
        }
        return loc.get(0).getLocalisation();
    }

    private double getQuant(String idpro, boolean entreOuSorti) {
        double result = 0;
        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
            if (entreOuSorti) {
                result = RecquisitionDelegate.sumByProduit(idpro);
            } else {
                result = LigneVenteDelegate.sumByProduit(idpro);
            }
        } else {
            if (entreOuSorti) {
                result = RecquisitionDelegate.sumByProduit(idpro, region);
            } else {
                result = LigneVenteDelegate.sumByProduit(idpro, region);
            }
        }
        return result;
    }

    private double getQuant(String idpro, Date d1, Date d2, boolean entreOuSorti) {
        LocalDate ld1 = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ld2 = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        double result = 0;
        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
            if (entreOuSorti) {
                result = RecquisitionDelegate.sumByProduit(idpro, ld1, ld2);
            } else {
                result = LigneVenteDelegate.sumByProduit(idpro, ld1, ld2);
            }
        } else {
            if (entreOuSorti) {
                result = RecquisitionDelegate.sumByProduit(idpro, ld1, ld2, region);
            } else {
                result = LigneVenteDelegate.sumByProduit(idpro, ld1, ld2, region);
            }
        }
        return result;
    }

//    private ObservableList<ListViewItem> toListMode(String query, String cat) {
//        ObservableList<ListViewItem> result = FXCollections.observableArrayList();
//        List<Object[]> goods;
//        if (cat.equals("All")) {
//            if (role.equals(Role.Trader.name()) | role.equals(Role.ALL_ACCESS.name())) {
//                goods = db.findGoods();
//            } else {
//                goods = db.findGoodsOnRegion(region);
//            }
//        } else {
//            if (role.equals(Role.Trader.name()) | role.equals(Role.ALL_ACCESS.name())) {
//                goods = db.searchGoods(query);
//            } else {
//                goods = db.searchGoods(query, region);
//            }
//        }
//        for (Object[] good : goods) {
//            ListViewItem lvi = new ListViewItem();
//            Double ca = Double.valueOf(String.valueOf(good[9]));
//            lvi.setCoutAchat(ca);
//            lvi.setPurchasePrice(ca);
//            lvi.setNumlot(String.valueOf(good[10]));
//            Produit pro = db.findByUid(Produit.class, String.valueOf(good[1]));
//            lvi.setProduit(pro);
//            lvi.setAchatQuantity(String.valueOf(good[7]) + " " + String.valueOf(good[8]));
//
//            if (!Objects.isNull(good[11])) {
//                try {
//                    String exp = String.valueOf(good[11]);
    ////                    if (StringUtils.isNumeric(exp)) {
//                    Date ex = Constants.DATE_ONLY_FORMAT.parse(String.valueOf(exp));
//                    lvi.setPeremption(ex);
////                    }
//                } catch (ParseException ex) {
//                    Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            String req = String.valueOf(good[0]);
//            Mesure mez = db.findByUid(Mesure.class, String.valueOf(good[2]));
//            lvi.setMesureAchat(mez);
//            List<Mesure> mesures = db.findByProduitAsc(Mesure.class, pro.getUid(), "quantcontenu");
//            if (mesures.isEmpty()) {
//                continue;
//            }
//            Mesure mesure = mesures.get(0);
//            List<PrixDeVente> pvx = db.findWithAndClauseDesc(PrixDeVente.class, new String[]{"recquisition_id", "mesureid_uid"}, new String[]{req, mez.getUid()}, "prix_Unitaire");
//            if (!pvx.isEmpty()) {
//                PrixDeVente pv = pvx.get(0);
//                Mesure mzr = db.findByUid(Mesure.class, pv.getMesureId().getUid());
//                lvi.setMesureDetail(mzr);
//                lvi.setDetailPrice(pv.getPrixUnitaire());
//            } else {
//                Double d = mesure.getQuantContenu();
//                PrixDeVente pr = findPrice(req);
//                if (pr == null) {
//                    continue;
//                }
//                Double p = pr.getPrixUnitaire();
//                Mesure qrz = pr.getMesureId();
//                Mesure qr = db.findByUid(Mesure.class, qrz.getUid());
//                double ppc = (p / qr.getQuantContenu());
//                double ppd = BigDecimal.valueOf(d * ppc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
//                lvi.setMesureDetail(mesure);
//                lvi.setDetailPrice(ppd);
//            }
//            mesure = mesures.get(mesures.size() - 1);
//            List<PrixDeVente> pvs = db.findWithAndClauseDesc(PrixDeVente.class, new String[]{"recquisition_id", "mesureid_uid"}, new String[]{req, mesure.getUid()}, "prix_Unitaire");
//            if (!pvs.isEmpty()) {
//                PrixDeVente pv = pvs.get(0);
//                Mesure mzr = db.findByUid(Mesure.class, pv.getMesureId().getUid());
//                lvi.setMesureGros(mzr);
//                lvi.setSalePrice(pv.getPrixUnitaire());
//            } else {
//                Double d = mesure.getQuantContenu();
//                PrixDeVente pr = findPrice(req);
//                Double p = pr.getPrixUnitaire();
//                Mesure qrz = pr.getMesureId();
//                Mesure qr = db.findByUid(Mesure.class, qrz.getUid());
//                double ppc = (p / qr.getQuantContenu());
//                double ppd = BigDecimal.valueOf(d * ppc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
//                lvi.setMesureGros(mesure);
//                lvi.setSalePrice(ppd);
//            }
//            double rest = Double.valueOf(String.valueOf(good[7]));
//            lvi.setQuantiteRestant(rest);
//            result.add(lvi);
//
//        }
//        return result;
//    }
    private PrixDeVente findPrice(String req) {
        List<PrixDeVente> pvs = PrixDeVenteDelegate.findDescOrderdByPriceForRecq(req);
        if (pvs.isEmpty()) {
            return null;
        }
        return pvs.get(0);
    }

    @FXML
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
        pref.putInt("tranzit_bill", -100);

    }

    double sommeUsd = 0, sommeCdf = 0, sommeDette = 0;

    private void refreshHistory(List<SaleItem> lsi, List<LocalDate> dates) {
        new Thread(() -> {
            sommeUsd = 0;
            sommeCdf = 0;
            sommeDette = 0;
            for (LocalDate date : dates) {
                Quintuplet<LocalDate, List<SaleItem>, Double, Double, Double> rst = Util.findByDate(lsi, date);
                SaleItem parent = new SaleItem();
                parent.setDate(date.atStartOfDay());
                parent.setSaleAmountCdf(rst.getY());
                parent.setSaleAmountUsd(rst.getX());
                parent.setSaleAmountCredit(rst.getZ());
                List<SaleItem> lv = rst.getW();
                sommeUsd += parent.getSaleAmountUsd();
                sommeCdf += parent.getSaleAmountCdf();
                sommeDette += parent.getSaleAmountCredit();
                if (lv.isEmpty()) {
                    continue;
                }
                parent.setFacture(lv.size() + " factures");
                TreeItem<SaleItem> tip = new TreeItem<>(parent);
                for (SaleItem si : lv) {
                    TreeItem<SaleItem> tsi = new TreeItem<>(si);
                    tip.getChildren().add(tsi);
                    List<LigneVente> sitems = si.getItems();
                    if (sitems == null) {
                        continue;
                    }
                    for (LigneVente item : sitems) {
                        SaleItem silv = new SaleItem();
                        silv.setClient(si.getClient());
                        Produit pt = ProduitDelegate.findProduit(item.getProductId().getUid());
                        silv.setProduit(pt);
                        silv.setQuantite(item.getQuantite());
                        Mesure mm = item.getMesureId();
                        if (mm == null) {
                            continue;
                        }
                        Mesure mzr = MesureDelegate.findMesure(mm.getUid());
                        silv.setMesure(mzr);
                        silv.setUnitPrice(item.getPrixUnit());
                        silv.setTotalCost(item.getMontantUsd());
                        silv.setSaleAmountCdf(item.getMontantCdf());
                        silv.setSaleAmountUsd(item.getMontantUsd());
                        TreeItem<SaleItem> tlv = new TreeItem<>(silv);
                        tsi.getChildren().add(tlv);
                    }

                }
                load_history.setVisible(false);
                //if (!treeSaleItems.contains(tip)) {
                treeSaleItems.add(tip);
                // }
            }

            rootView.getChildren().setAll(treeSaleItems);

            Platform.runLater(() -> {
                txt_table_hyst_count.setText(rootView.getChildren().size() + " elements");
                txt_total_chiffre_affaire.setText("Total cash usd " + BigDecimal.valueOf(sommeUsd).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + ", cdf "
                        + "" + BigDecimal.valueOf(sommeCdf).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + ". Dette USD "
                        + "" + BigDecimal.valueOf(sommeDette).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            });
            Util.installTooltip(txt_total_chiffre_affaire, txt_total_chiffre_affaire.getText());
        }).start();
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
    private void gotoCloture(MouseEvent event) {
        // TODO: Implement Cloture functionality
        MainUI.notify(null, "Info", "Fonctionnalité de clôture en cours de développement", 1, "info");
    }

    public void clearTableSelection() {
        tbl_list_pro.getSelectionModel().clearSelection();
    }

    private void initRequisitionTab() {
        obl_livraisons_req = FXCollections.observableArrayList();
        if (obl_fournisseurs == null) {
            obl_fournisseurs = FXCollections.observableArrayList();
        }
        list_livraison_req.setItems(obl_livraisons_req);
        list_supplier_req.setItems(obl_fournisseurs);

        cbx_provenance_req.setItems(FXCollections.observableArrayList("Achat", "Entrepot"));
        cbx_provenance_req.getSelectionModel().selectFirst();

        list_livraison_req.setCellFactory(param -> new ListCell<Livraison>() {
            @Override
            protected void updateItem(Livraison item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getReference() + " - " + (item.getFournId() != null ? item.getFournId().getNomFourn() : "") + " le " + item.getDateLivr().toString());
                }
            }
        });

        list_supplier_req.setCellFactory(param -> new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomFourn() + " - " + item.getPhone());
                }
            }
        });

        search_livraiz_req.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                list_livraison_req.setItems(obl_livraisons_req);
            } else {
                ObservableList<Livraison> filtered = FXCollections.observableArrayList();
                for (Livraison l : obl_livraisons_req) {
                    if (l.getReference().toLowerCase().contains(newValue.toLowerCase()) || (l.getFournId() != null && l.getFournId().getNomFourn().toLowerCase().contains(newValue.toLowerCase()))) {
                        filtered.add(l);
                    }
                }
                list_livraison_req.setItems(filtered);
            }
        });

        search_supplier_req.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                list_supplier_req.setItems(obl_fournisseurs);
            } else {
                ObservableList<Fournisseur> filtered = FXCollections.observableArrayList();
                for (Fournisseur f : obl_fournisseurs) {
                    if (f.getNomFourn() != null && f.getNomFourn().toLowerCase().contains(newValue.toLowerCase())) {
                        filtered.add(f);
                    }
                }
                list_supplier_req.setItems(filtered);
            }
        });

        list_supplier_req.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Fournisseur>() {
            @Override
            public void changed(ObservableValue<? extends Fournisseur> observable, Fournisseur oldValue,
                    Fournisseur newValue) {
                if (newValue != null) {
                    choosenSupply = newValue;
                    Double somm = LivraisonDelegate.sumBySupplier(choosenSupply.getUid());
                    List<Livraison> livrs = LivraisonDelegate.findBySupplier(choosenSupply.getUid());
                    lbl_livrez_recq.setText(choosenSupply.getNomFourn() + ", " + choosenSupply.getAdresse() + " "
                            + choosenSupply.getPhone() + "/ " + livrs.size() + " Livraisons, Total achat : " + somm);
                    obl_livraisons_req.setAll(livrs);
                }
            }
        });

        ContextMenu ctxt = new ContextMenu();
        MenuItem updatef = new MenuItem(bundle.getString("xbtn.update"));
        MenuItem deletef = new MenuItem(bundle.getString("delete"));
        MenuItem livraizf = new MenuItem(bundle.getString("selectdeliv"));
        MenuItem settleSupplierDebt = new MenuItem("Regler dette fournisseur");
        MenuItem supplierDebtReport = new MenuItem("Releve dettes fournisseur");
        ctxt.getItems().add(updatef);
        ctxt.getItems().add(deletef);
        ctxt.getItems().add(livraizf);
        ctxt.getItems().add(settleSupplierDebt);
        ctxt.getItems().add(supplierDebtReport);
        list_supplier_req.setContextMenu(ctxt);
        updatef.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenSupply != null) {
                    MainUI.floatDialog(tools.Constants.FOURNISSEUR_DLG, 1090, 508, null, kazisafe, entreprise,
                            choosenSupply);
                }
            }
        });
        deletef.setOnAction((ActionEvent event) -> {
            if (choosenSupply != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Voulez vous vraiment supprimer le fournisseur " + choosenSupply.getNomFourn() + " ?",
                        ButtonType.YES, ButtonType.CANCEL);
                alert.setTitle("Attention!");
                alert.setHeaderText(null);
                Optional<ButtonType> showAndWait = alert.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    if (role.equals(Role.Trader.name())
                            | role.equals(Role.Manager.name())
                            | role.equals(Role.Magazinner.name())
                            | role.contains(Role.ALL_ACCESS.name())) {
                        FournisseurDelegate.deleteFournisseur(choosenSupply);
                        obl_fournisseurs.remove(choosenSupply);
                        // count_fournisseur.setText(String.format(bundle.getString("xitems"), list_supplier_req.size()));
                        MainUI.notify(null, "Succes", "Fournisseur supprime avec success", 3, "info");
                    } else {
                        MainUI.notify(null, "Attention",
                                "Vous n'avez pas assez de privileges pour effectuer cette action", 3, "warning");
                    }
                }
            }
        });
        livraizf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenSupply != null) {
                    showSupplierDeliveries();
                }
            }
        });
        settleSupplierDebt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenSupply == null) {
                    MainUI.notify(null, "Selection", "Selectionnez d'abord un fournisseur", 3, "warning");
                    return;
                }
                List<Livraison> livrs = LivraisonDelegate.findBySupplier(choosenSupply.getUid());
                Livraison target = null;
                for (Livraison liv : livrs) {
                    if (safeAmount(liv.getRemained()) > 0d) {
                        target = liv;
                        break;
                    }
                }
                if (target == null) {
                    MainUI.notify(null, "Info", "Ce fournisseur n'a plus de dette restante", 3, "info");
                    return;
                }
                settleSupplierDebt(target);
            }
        });
        supplierDebtReport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                exportSupplierDebtStatement();
            }
        });

    }

    private void exportSupplierDebtStatement() {
        if (choosenSupply == null) {
            MainUI.notify(null, "Selection", "Selectionnez d'abord un fournisseur", 3, "warning");
            return;
        }
        List<Livraison> livrs = LivraisonDelegate.findBySupplier(choosenSupply.getUid());
        if (livrs == null || livrs.isEmpty()) {
            MainUI.notify(null, "Info", "Aucune livraison pour ce fournisseur", 3, "info");
            return;
        }
        new Thread(() -> {
            File report = Util.exportXlsLivraison(TraisorerieDelegate.findTraisoreries(), livrs, 1d);
            if (report == null) {
                MainUI.notify(null, "Erreur", "Echec de generation du releve", 3, "error");
                return;
            }
            try {
                Desktop.getDesktop().open(report);
            } catch (IOException ex) {
                Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    @FXML
    private void addRequest(ActionEvent event) {
        String prov = cbx_provenance_req.getValue();
        if ("Achat".equals(prov)) {
            //on affiche le dialogue de DELIVERY_DLG
            MainUI.floatDialog(tools.Constants.DELIVERY_DLG, 600, 468, null, kazisafe, entreprise, null, Constants.POS);
        } else {
            //on affiche le RECQ_DLG
            addRequestItem(event);
        }
    }

    private boolean hasPermission(PermitTo permit) {
        return role != null && role.toUpperCase().contains(Role.ALL_ACCESS.name())
                || role.toUpperCase().equals(Role.Trader.name()) || PermissionDelegate.hasPermission(permit);
    }

    private void settleSupplierDebt(Livraison target) {
        if (!hasPermission(PermitTo.UPDATE_LIVRAISON)) {
            MainUI.notify(null, "Permission", "Vous n'avez pas la permission de regler cette dette", 3, "warning");
            return;
        }
        double remained = safeAmount(target.getRemained());
        if (remained <= 0d) {
            MainUI.notify(null, "Info", "Cette livraison est deja reglee", 3, "info");
            return;
        }
        TextInputDialog amountDialog = new TextInputDialog(String.valueOf(remained));
        amountDialog.setTitle("Reglement dette fournisseur");
        amountDialog.setHeaderText("Livraison " + target.getNumPiece() + " - dette restante USD " + remained);
        amountDialog.setContentText("Montant a regler (USD):");
        Optional<String> amountInput = amountDialog.showAndWait();
        if (amountInput.isEmpty()) {
            return;
        }
        double amount = safeAmount(amountInput.get());
        if (amount <= 0d) {
            MainUI.notify(null, "Validation", "Le montant doit etre superieur a zero", 3, "warning");
            return;
        }
        TextInputDialog recuDialog = new TextInputDialog();
        recuDialog.setTitle("Numero recu fournisseur");
        recuDialog.setHeaderText("Enregistrer le numero de recu du fournisseur");
        recuDialog.setContentText("Numero recu:");
        Optional<String> recuInput = recuDialog.showAndWait();
        if (recuInput.isEmpty()) {
            return;
        }
        String recu = recuInput.get().trim();
        if (recu.isBlank()) {
            MainUI.notify(null, "Validation", "Le numero de recu est obligatoire", 3, "warning");
            return;
        }
        double applied = Math.min(amount, remained);
        target.setPayed(safeAmount(target.getPayed()) + applied);
        double left = Math.max(0d, remained - applied);
        target.setRemained(left);
        target.setToreceive(Double.valueOf(Math.max(0d, applied - remained)));
        String baseObs = target.getObservation() == null ? "" : target.getObservation();
        String line = " | RECU_FOURNISSEUR=" + recu + " ; MONTANT=" + applied + " ; DATE=" + LocalDateTime.now();
        target.setObservation((baseObs + line).trim());
        Livraison saved = LivraisonDelegate.updateLivraison(target);
        if (saved == null) {
            MainUI.notify(null, "Erreur", "Echec d'enregistrement local du reglement", 3, "error");
            return;
        }
//        Util.sync(saved, Constants.ACTION_UPDATE, Tables.LIVRAISON);
        syncLivraisonByHttp(saved);
//        int index = obl_livraisons_req.indexOf(saved);
//        if (index >= 0) {
//            obl_livraisons_req.set(index, saved);
//        }
//        if (livraison != null && livraison.getUid().equals(saved.getUid())) {
//            livraison = saved;
//            global_achat.setText("Total : USD " + safeAmount(saved.getPayed()));
//        }
        MainUI.notify(null, "Succes", "Reglement fournisseur enregistre (recu: " + recu + ")", 4, "info");
    }

    private void syncLivraisonByHttp(Livraison l) {
        if (kazisafe == null || l == null || l.getFournId() == null) {
            return;
        }
        kazisafe.syncDelivery(l.getUid(),
                l.getNumPiece(),
                l.getDateLivr() == null ? LocalDate.now().toString() : l.getDateLivr().toString(),
                l.getReference(),
                l.getLibelle(),
                String.valueOf(safeAmount(l.getReduction())),
                l.getObservation(),
                String.valueOf(safeAmount(l.getTopay())),
                String.valueOf(safeAmount(l.getPayed())),
                String.valueOf(safeAmount(l.getRemained())),
                String.valueOf(safeAmount(l.getToreceive())),
                l.getFournId().getUid())
                .enqueue(new Callback<Livraison>() {
                    @Override
                    public void onResponse(Call<Livraison> call, Response<Livraison> rspns) {
                        System.out.println("Livraison sync update " + rspns.code());
                    }

                    @Override
                    public void onFailure(Call<Livraison> call, Throwable thrwbl) {
                        System.err.println("Livraison sync update failed " + thrwbl.getMessage());
                    }
                });
    }

    private double safeAmount(Double value) {
        return value == null ? 0d : value;
    }

    private double safeAmount(String value) {
        try {
            return Double.parseDouble(value == null ? "0" : value.trim());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private void showSupplierDeliveries() {

        if (tabpane_req_left.getSelectionModel().getSelectedIndex() == 1) {
            choosenSupply = list_supplier_req.getSelectionModel().getSelectedItem();
            if (choosenSupply == null) {
                MainUI.notify(null, "Erreur", "Veuillez selectionner un fournisseur dans l'onglet Fournisseurs", 3, "error");
                return;
            }
            List<Livraison> livs = LivraisonDelegate.findBySupplier(choosenSupply.getUid());
            obl_livraisons_req.setAll(livs);
            tabpane_req_left.getSelectionModel().select(0);
            lbl_livrez_recq.setText(choosenSupply.getNomFourn() + ", " + choosenSupply.getAdresse() + " "
                    + choosenSupply.getPhone() + "/ " + livs.size() + " Livraisons, Total achat : " + livs.stream().mapToDouble(l -> l.getTopay()).sum());

        } else {
            MainUI.notify(null, "Erreur", "Veuillez selectionner un fournisseur dans l'onglet Fournisseurs", 3, "error");
            return;
        }
    }

    boolean isCard = true;

    public void refreshPosUi() {
        String mode = pref.get("view-mode", "card");
        if (!mode.equals("card")) {
            tbl_list_pro.getSelectionModel().clearSelection();
            scrollPos.setVisible(false);
            list_mode.setVisible(true);
            fillProductInTable("All");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    tbl_pro_count.setText(tbl_list_pro.getItems().size() + " elements");
                }
            });
        } else {
            list_mode.setVisible(false);
            scrollPos.setVisible(true);
            fillProducts(true, lisprod);
        }

    }

    @FXML
    public void refreshPos(Event e) {
        refreshPosUi();
    }

    @FXML
    public void switchModeView(Event e) {
        Node n = (Node) e.getSource();
        ImageView nx = (ImageView) n;
        String mode = pref.get("view-mode", "card");
        if (mode.equals("card")) {
            Util.setResourceImage(btn_view_mode, "bloccard.png");
            pref.put("view-mode", "list");
            Tooltip.install(nx, new Tooltip("Afficher les elements sur des cartes"));

            scrollPos.setVisible(false);
            list_mode.setVisible(true);
            fillProductInTable("All");

            if (prodx.size() > 20) {
                ScrollBar vbar = getVScrollbar(tbl_list_pro);
                vbar.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (newValue.doubleValue() == vbar.getMax()) {

                            int or = tdataLoded;
                            tdataLoded += 10;
                            int limit = Math.min(tdataLoded, prodx.size());
                            if (or > limit) {
                                return;
                            }
                            List<Produit> prod = prodx.subList(or, limit);
                            fillProductInTable("All");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    tbl_pro_count.setText(tbl_list_pro.getItems().size() + " elements");
                                }
                            });
                        }
                    }
                });
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tbl_pro_count.setText(tbl_list_pro.getItems().size() + " elements");
                    }
                });
            }
        } else {
            Tooltip.install(n, new Tooltip("Affecher les elements dans un tableau"));
            Util.setResourceImage(btn_view_mode, "list.png");
            list_mode.setVisible(false);
            scrollPos.setVisible(true);
            pref.put("view-mode", "card");
            fillProducts(true, lisprod);
        }
    }

    public void tryit(String id) {
        ObservableList<Node> children = tile_pane.getChildren();
        for (Node node : children) {
            ObservableList<Node> elt = ((Pane) node).getChildren();
            for (Node node1 : elt) {
                if (node1 instanceof ImageView) {
                    ImageView imagev = (ImageView) node1;
                    if (imagev.getId().equals(node1)) {
                        Util.installPicture(imagev, id + ".jpeg");
                    }
                }
            }
        }
    }

    private Node addProduit(final Produit p) {
        Pane pane = new Pane();
        pane.setId(p.getUid());
        pane.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        pane.setPadding(new Insets(0, 2, 2, 2));
        DropShadow dse = new DropShadow();
        pane.setEffect(dse);
        pane.setPrefWidth(149);
        pane.setPrefHeight(142);
        ImageView imagev = new ImageView();
        imagev.setId(p.getUid());
        Label l = new Label();
        l.setPrefWidth(139);
        l.setPrefHeight(16);
        l.setLayoutY(118);
        l.setLayoutX(5);
        l.setTextFill(Color.rgb(255, 255, 255));
        l.setBackground(new Background(new BackgroundFill(Color.rgb(0x7, 0x7, 0xf, 0.3), new CornerRadii(3.0), new Insets(-5.0))));
        l.setPadding(new Insets(0, 4, 4, 4));
        imagev.setFitWidth(149);
        imagev.setFitHeight(122);
        imagev.setScaleX(1);
        imagev.setScaleY(1);
        imagev.setScaleZ(1);
        imagev.setPreserveRatio(true);
        imagev.setCursor(Cursor.HAND);
        List<Recquisition> lr;
        List<LigneVente> ll;
        if (role.equals(Role.Trader.name()) | role.equals(Role.ALL_ACCESS.name())) {
            lr = RecquisitionDelegate.findRecquisitionByProduit(p.getUid());
            ll = LigneVenteDelegate.findByProduit(p.getUid());
        } else {
            lr = RecquisitionDelegate.findRecquisitionByProduitRegion(p.getUid(), region);
            ll = LigneVenteDelegate.findByProduitRegion(p.getUid(), region);
        }

        Recquisition r = getHeaderRecq(p);
        double rest = getRestPCFor(lr, ll);
        if (rest == 0) {
            return null;
        }
        if (r == null) {
            return null;
        }
        Mesure mz = r.getMesureId();
        Mesure m = MesureDelegate.findMesure(mz.getUid());
        if (m == null) {
            List<Mesure> lm = MesureDelegate.findMesureByProduit(p.getUid());
            if (lm.isEmpty()) {
                return null;
            }
            m = lm.get(0);
        }
        l.setText(p.getMarque() + " " + p.getModele() + " " + p.getTaille() + "(" + (r.getQuantite() / m.getQuantContenu()) + " " + m.getDescription() + ")");
        Util.installTooltip(l, l.getText());
        Util.installPicture(imagev, p.getUid() + ".jpeg");
        pane.getChildren().add(imagev);
        pane.getChildren().add(l);

        pane.setOnMouseClicked((MouseEvent event) -> {
            MainUI.floatDialog(tools.Constants.PANIER_DLG, 430, 497, null, kazisafe, p, entreprise, "Create", -1);
        });
        imagev.setOnMouseEntered((MouseEvent event) -> {
            onHoverHome(event);
        });
        imagev.setOnMouseExited((MouseEvent event) -> {
            onOutHome(event);
        });
        return pane;
    }

    public void addProductItem(Produit good) {
        if (good != null) {
            Platform.runLater(() -> {
                Node pane = addProduit(good);
                if (pane != null) {
                    tile_pane.getChildren().add(pane);
                }
            });
        }
    }

    private void fillProducts(boolean reinit, Collection<Produit> ps) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        if (reinit) {
            Platform.runLater(tile_pane.getChildren()::clear);
        }
        //List<Object[]> goods = db.searchGoods();
        for (Produit good : ps) {

            Platform.runLater(() -> {
                //  Produit pr=db.findByUid(Produit.class, String.valueOf(good[1]));
                Node pane = addProduit(good);
//                        boolean isnodexist=findNode(tile_pane.getChildren(), pane)!=null;
//                            if (!isnodexist) {
                if (pane != null) {
                    tile_pane.getChildren().add(pane);
                }
            });
        }

//            }
//        }).start();
    }

    public Node findNode(List<Node> nodes, Node id) {
        for (Node node : nodes) {
            if (node.getId().equals(id.getId())) {
                return node;
            }
        }
        return null;
    }

    @FXML
    private void selectRowPerPage(ActionEvent evt) {
        ComboBox cbx = (ComboBox) evt.getSource();
        rowsDataCount = (int) cbx.getValue();
        pagination_req.setPageFactory(this::createDataPage);
        System.out.println("Row set to " + rowsDataCount);
    }

    @FXML
    private void selectRowPerPage1(ActionEvent evt) {
        ComboBox cbx = (ComboBox) evt.getSource();
        rowsDataCount1 = (int) cbx.getValue();
//        pagination_sale.setPageFactory(this::createDataPage1);
        System.out.println("Row set to " + rowsDataCount1);
    }

    @FXML
    private void selectRowPerPage2(ActionEvent evt) {
        ComboBox cbx = (ComboBox) evt.getSource();
        rowsDataCount2 = (int) cbx.getValue();
//        pagination_inv.setPageFactory(this::createDataPage2);
        System.out.println("Row set to " + rowsDataCount2);
    }

    private Node createDataPage(int pgindex) {
        try {
            int offset = pgindex * rowsDataCount;
            int limit = Math.min(offset + rowsDataCount, lsreq.size());
            table_req.setItems(FXCollections.observableArrayList(lsreq.subList(offset, limit)));
            txt_table_req_count.setText(lsreq.size() + " elements");
        } catch (java.lang.IllegalArgumentException e) {
            pagination_req.setPageCount(pgindex);
            System.out.println("Page suivante non disponible");
        }
        return table_req;
    }

    private Node createDataPage1(int pgindex) {
        try {
            if (treeSaleItems != null) {
                int offset = pgindex * rowsDataCount1;
                int limit = Math.min(offset + rowsDataCount1, treeSaleItems.size());
                rootView.getChildren().setAll(FXCollections.observableArrayList(treeSaleItems.subList(offset, limit)));
                ttable_ventes_hyst.setRoot(rootView);
                txt_table_hyst_count.setText(rootView.getChildren().size() + " elements");
            }
        } catch (java.lang.IllegalArgumentException e) {
//            pagination_sale.setPageCount(pgindex);
            System.out.println("Page suivante non disponible");
        }
        return ttable_ventes_hyst;
    }

    private Node createDataPage2(int pgindex) {
        try {
            int offset = pgindex * rowsDataCount2;
            int limit = Math.min(offset + rowsDataCount2, lsinventaire.size());
            table_inv_mag.setItems(FXCollections.observableArrayList(lsinventaire.subList(offset, limit)));

        } catch (java.lang.IllegalArgumentException e) {
//            pagination_inv.setPageCount(pgindex);
            System.out.println("Page suivante non disponible");
        }
        return table_inv_mag;
    }

    @FXML
    public void payNow(Event e) {
        if (chkbx_declasser_panier.isSelected()) {
            motif_declass.setVisible(true);
        } else {
            if (choosenVente != null) {
                MainUI.floatDialog(tools.Constants.PAYMENT_DLG, 1088, 678, null, kazisafe, lslgnventes, choosenVente, entreprise, choosenVente.getClientId());
            } else {
                MainUI.floatDialog(tools.Constants.PAYMENT_DLG, 1088, 678, null, kazisafe, lslgnventes, selectedCart == null ? null : selectedCart, entreprise, selectedCart == null ? null : selectedCart.getClientId());
            }
        }
    }

    @FXML
    public void saveDeclasser(Event e) {
        if (!tf_motif_declass.getText().isEmpty()) {
            //declasser now
            Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment déclasser les éléments séléctionnés", ButtonType.YES, ButtonType.CANCEL);
            alertdlg.setTitle("Attention!");
            alertdlg.setHeaderText(null);

            Optional<ButtonType> showAndWait = alertdlg.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                int r = (int) (Math.random() * 100000);
                Vente v = new Vente(r);
                Client clt = ClientDelegate.findAnonymousClient();//db.findWithAndClause(Client.class, new String[]{"phone"}, new String[]{"0000"});
                v.setClientId(clt);
                v.setDateVente(LocalDateTime.now());
                v.setDeviseDette("USD");
                v.setLatitude(0d);
                v.setLibelle("Déclassement de stock");
                v.setLongitude(0d);
                v.setMontantCdf(0);
                v.setMontantDette(0d);
                v.setMontantUsd(0);
                v.setObservation("Déclassement de stock");
                v.setPayment("NA");
                v.setReference("DEC" + r);
                v.setRegion(region);
                Vente ventura = VenteDelegate.saveVente(v);//db.insertAndSync(v);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(ventura, Constants.ACTION_CREATE, Tables.VENTE);
                        });
                for (LigneVente lv : lslgnventes) {
                    lv.setReference(v);
                    lv.setClientId("RABBISH");
//                    System.out.println("VENTE INTSERTE "+ventura);

                    System.out.println("Ligne vente " + lv.getNumlot());
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(LigneVenteDelegate.saveLigneVente(lv), Constants.ACTION_CREATE, Tables.LIGNEVENTE);//db.insertAndSync(lv);
                            });
                }

                MainUI.notify(null, "Succès!", "Tout le panier a été déclassé avec succès", 3, "info");
                closeFloatingPane(e);
                clearCart();
            }
        } else {
            //
            MainUI.notify(null, "Erreur", "Veuillez preciser la raison de declassement", 3, "error");
        }
    }

    @FXML
    public void clearCart() {
        lslgnventes.clear();
        btn_pay_now.setDisable(true);
        txt_panier_total.setText("Total : 0");
    }

    public boolean isInScanMode() {
        return chbx_scancbar.isSelected();
    }

    public void clearCart(Event e) {
        lslgnventes.clear();
        btn_pay_now.setDisable(lslgnventes.isEmpty());
    }

    public void fillProductInTable(final String cat) {
        new Thread(() -> {
            List<ListViewItem> datalist
                    = RecquisitionDelegate.populate();
            Platform.runLater(() -> {
                list_mode_ls.setAll(datalist);
                tbl_list_pro.setItems(list_mode_ls);
            });
        }).start();
    }

    List<ListViewItem> searcher = null;

    @FXML
    public void selectProduct(Event evt) {

        newValue = tbl_list_pro.getSelectionModel().getSelectedItem();
        if (newValue == null) {
            return;
        }
        Produit p = newValue.getProduit();
        MainUI.floatDialog(tools.Constants.PANIER_DLG, 430, 497, null, kazisafe, p, entreprise, "Create", -1);
        System.out.println("Test " + p.getCodebar());
        tbl_list_pro.getSelectionModel().clearSelection();

    }

    public void search(final String query) {
        searcher = new ArrayList<>();
        System.out.println("Searcching cb");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // ObservableList<Produit> produits = FXCollections.observableArrayList();
//                tbl_list_pro.getSelectionModel().clearSelection();
                if (tab_pos.isSelected()) {
                    //tile_pane.getChildren().clear();
                    List<ListViewItem> result = new ArrayList<>();
                    if (chbx_scancbar.isSelected()) {
                        Produit p = ProduitDelegate.findByCodebar(query);
                        //Util.findProduitByCodebar(distinctProduct(lisprod), query);
                        System.out.println("Produit by CB -XX " + p);
                        if (p != null) {
                            Platform.runLater(() -> {
                                MainUI.floatDialog(tools.Constants.PANIER_DLG, 430, 497, null, kazisafe, p, entreprise, "Create", -1);
                            });
                        }

                    }

                    String vu = pref.get("view-mode", "card");
                    if (query.isEmpty()) {
                        if (vu.equals("card")) {
                            fillProducts(true, lisprod);
                        } else {
                            tbl_list_pro.getSelectionModel().clearSelection();
                            tbl_list_pro.setItems(list_mode_ls);
                        }
                    } else {
                        if (!vu.equals("card")) {
                            result.clear();
                            searcher.clear();
                            for (ListViewItem p : list_mode_ls) {
                                String pred = (p.getProduit().getCodebar() + " " + p.getProduit().getCouleur() + ""
                                        + " " + p.getProduit().getMarque() + " " + p.getProduit().getModele() + " "
                                        + p.getProduit().getNomProduit() + " " + p.getProduit().getTaille());
                                if (pred.toUpperCase().contains(query.toUpperCase())) {
                                    result.add(p);
                                }
                            }
                            Platform.runLater(() -> {
                                tbl_list_pro.setItems(FXCollections.observableArrayList(result));
                                tbl_pro_count.setText(String.format(bundle.getString("xitems"), result.size()));
                            });
                        } else {
                            List<Produit> resulto = new ArrayList<>();
                            for (Produit p : lisprod) {
                                String pred = (p.getCodebar() + " " + p.getCouleur() + ""
                                        + " " + p.getMarque() + " " + p.getModele() + " "
                                        + p.getNomProduit() + " " + p.getTaille());
                                if (pred.toUpperCase().contains(query.toUpperCase())) {
                                    resulto.add(p);
                                }
                            }
                            if (resulto.isEmpty()) {
                                List<Produit> inj = ProduitDelegate.findProduitByName(query);//searchInDatabase(dbase, query);
                                resulto.addAll(inj);
                            }
                            System.err.println("pos pos pos " + resulto.size());
                            if (vu.equals("card")) {
                                fillProducts(true, resulto);
                            }
                        }
                    }

                } else if (tab_requisition.isSelected()) {
                    if (query.isEmpty()) {
                        refreshRecqs(null);
                        return;
                    }
                    ObservableList<Recquisition> result = FXCollections.observableArrayList();
                    table_req.setItems(result);
                    for (Recquisition req : lsreq) {
                        Produit p = req.getProductId();
                        String pred = p.getCodebar() + " " + p.getCouleur() + ""
                                + " " + p.getMarque() + " " + p.getModele() + " "
                                + p.getNomProduit() + " " + p.getTaille() + " " + req.getReference() + " " + req.getObservation();
                        if (pred.toUpperCase().contains(query.toUpperCase())) {
                            result.add(req);
                        }
                    }

                } else if (tab_history.isSelected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (query.isEmpty()) {
                                fillSaleHistory();
                                return;
                            }

                            List<Vente> ventes = VenteDelegate.findVentes();
                            List<Vente> lrst = new ArrayList<>();
                            for (Vente v : ventes) {
                                Client c = ClientDelegate.findClient(v.getClientId().getUid());
                                String bill = v.getReference() + " " + (Constants.DATE_HEURE_FORMAT.format(v.getDateVente())) + " "
                                        + "" + (c == null ? "Anonyme" : (c.getNomClient() + " " + c.getPhone())) + " " + c.getEmail();
                                List<LigneVente> lgv = v.getLigneVenteList();
                                List<LigneVente> lgvs = lgv == null ? LigneVenteDelegate.findByReference(v.getUid()) : lgv;
                                for (LigneVente lv : lgvs) {
                                    Produit px = lv.getProductId();
                                    Produit p = ProduitDelegate.findProduit(px.getUid());
                                    String pred = (p.getCodebar() + " " + p.getCouleur() + ""
                                            + " " + p.getMarque() + " " + p.getModele() + " "
                                            + p.getNomProduit() + " " + p.getTaille()).toUpperCase();
                                    if ((bill + " " + pred).toUpperCase().contains(query.toUpperCase())) {
                                        lrst.add(v);
                                    }
                                }
                            }
                            fillSaleHistory();

                        }
                    }).start();
                } else if (tab_mag_inv.isSelected()) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                    ObservableList<InventoryMagasin> linvm = FXCollections.observableArrayList();
                    for (InventoryMagasin im : lsinventaire) {
                        Produit p = im.getProduit();
                        if (p == null) {
                            continue;
                        }
                        String pred = (p.getCodebar() + " " + p.getCouleur() + ""
                                + " " + p.getMarque() + " " + p.getModele() + " "
                                + p.getNomProduit() + " " + p.getTaille()).toUpperCase();
                        if (pred.contains(query.toUpperCase())) {
                            linvm.add(im);
                        }
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            table_inv_mag.setItems(linvm);
                        }
                    });

//                        }
//                    }).start();
                } else if (peremption.isSelected()) {
                    Executors.newCachedThreadPool()
                            .execute(() -> {
                                if (query.isEmpty()) {
                                    tablePeremption.setItems(ols_peremption);
                                    enPeremption(null);
                                } else {
                                    ObservableList<Peremption> result = FXCollections.observableArrayList();
                                    valeurs = 0;
                                    for (Peremption perams : ols_peremption) {
                                        String pred = perams.getCodebar() + " " + perams.getProduit()
                                                + " " + perams.getLocalisation() + " " + perams.getLot() + ""
                                                + " " + perams.getLot() + " " + perams.getRegion() + " " + perams.getQuantite()
                                                + " " + Constants.USER_READABLE_FORMAT.format(perams.getDateExpiry()) + " "
                                                + perams.getCoutAchat();
                                        if (pred.toUpperCase().contains(query.toUpperCase())) {
                                            result.add(perams);
                                            valeurs += perams.getValeur();
                                        }
                                    }
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            tablePeremption.setItems(result);
                                            countPeremption.setText(result.size() + " elements");
                                            valeurPremption.setText("Valeur total : " + valeurs + " USD");
                                        }
                                    });
                                }
                            });

                } else if (tab_rupture.isSelected()) {
//                    Executors.newSingleThreadExecutor().submit(() -> {
                    if (query.isEmpty()) {
                        tbl_rupture.setItems(obl_rupture_list);
                    } else {
                        List<Rupture> result = new ArrayList<>();
                        for (Rupture rup : obl_rupture_list) {
                            Produit p = rup.getProduit();
                            String pr = rup.getDate() + " " + rup.getLocalisation() + " " + rup.getRegion() + " " + p.getCodebar()
                                    + " " + p.getCouleur() + " " + p.getMarque() + " " + p.getModele() + " " + p.getNomProduit() + " " + p.getTaille();
                            if (pr.toUpperCase().contains(query.toUpperCase())) {
                                result.add(rup);
                            }
                        }
                        Platform.runLater(() -> {
                            tbl_rupture.setItems(FXCollections.observableArrayList(result));
                            rupt_count.setText(String.format(bundle.getString("xitems"), result.size()));
                        });
                    }
//                    });
                }
            }
        }).start();
    }
    int s = 0;

    public double getRest(Produit p) {
        List<LigneVente> ll;
        List<Recquisition> lr;
        if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
            lr = RecquisitionDelegate.findRecquisitionByProduitRegion(p.getUid(), region);
            ll = LigneVenteDelegate.findByProduitRegion(p.getUid(), region);
        } else {
            lr = RecquisitionDelegate.findRecquisitionByProduit(p.getUid());
            ll = LigneVenteDelegate.findByProduit(p.getUid());
        }
        return getRestPCFor(lr, ll);
    }

    @FXML
    private void searchVenteByDate(Event e) {
        if (dpk_debut_hyst.getValue() != null && dpk_fin_hyst.getValue() != null) {

            List<Vente> vts;
            if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                vts = VenteDelegate.findAllByDateInterval(dpk_debut_hyst.getValue(), dpk_fin_hyst.getValue());
            } else {

                vts = VenteDelegate.findAllByDateInterval(dpk_debut_hyst.getValue(), dpk_fin_hyst.getValue(), region);
            }

            fillSaleHistory(vts);
        } else {
            fillSaleHistory();
        }

    }

    @FXML
    private void searchRecqByDate(Event evt) {
        if (dpk_debut_req.getValue() != null && dpk_fin_req.getValue() != null) {
            ObservableList<Recquisition> result = FXCollections.observableArrayList();
            for (Recquisition e : lsreq) {
                if (Util.isDateBetween(dpk_debut_req.getValue(), dpk_fin_req.getValue(), e.getDate().toLocalDate())) {
                    result.add(e);
                }
            }
            table_req.setItems(result);
        } else {
            refreshRecqs(null);
        }

    }

    public boolean pass(double quantDem, Recquisition req, Produit choosenPro, Mesure choosenMesure) {
        // Check available stock from StockDepotAgregate aggregate table
        if (newValue == null) {
            return false;
        }
        // String regionStock = req.getRegion() != null ? req.getRegion() : region;
        Mesure mx = newValue.getMesureAchat();
        double availableStock = newValue.getQuantiteRestant() * mx.getQuantContenu();

        // Also check what's already in the current list to prevent double counting
        double alreadyQueued = lslgnventes.stream()
                .filter(d -> d.getProductId().getUid().equals(choosenPro.getUid()))
                .mapToDouble(d -> d.getQuantite() * d.getMesureId().getQuantContenu()).sum();

        double netAvailable = availableStock - alreadyQueued;

        // Récupérer la mesure réelle du stocker pour les calculs de conversion
        Mesure reel = MesureDelegate.findMesure(choosenMesure.getUid());
        double qco = reel.getQuantContenu();

        double alert = req.getStockAlert() * qco;
        if (netAvailable <= alert) {
            MainUI.notify(null, bundle.getString("warning"), bundle.getString("alertmess") + " de "
                    + req.getStockAlert() + " " + reel.getDescription(), 5, "warning");
        }
        // Also calculate based on direct stocker-destocker difference for backward
        // compatibility

        double qinpc = quantDem * choosenMesure.getQuantContenu();
        double dispo = netAvailable - qinpc;
        double stab = alreadyQueued / choosenMesure.getQuantContenu();
        double stockRestant = netAvailable / choosenMesure.getQuantContenu();

        if (dispo < 0) {
            MainUI.notify(null, bundle.getString("error"),
                    String.format("Stock insuffisant. Disponible: %.2f %s (déjà dans la liste: %.2f)",
                            stockRestant, choosenMesure.getDescription(), stab),
                    5, "error");
            return false;
        }
        return true;
    }
    double savedSum = 0;

    public void addCartItem(LigneVente lv) {
        lslgnventes.add(lv);
        String dev = pref.get("mainCur", "USD");
        savedSum = Util.sumCart(lslgnventes, dev);
        if (dev.equals("CDF")) {
            txt_panier_total.setText("Total : " + BigDecimal.valueOf(savedSum).setScale(0, RoundingMode.HALF_EVEN).doubleValue() + "  CDF");
        } else {
            txt_panier_total.setText("Total : " + savedSum + "  USD");
        }
        tbl_list_pro.getSelectionModel().clearSelection();
        btn_pay_now.setDisable(lslgnventes.isEmpty());
    }

    public List<Recquisition> loadReqs(List<Produit> lps) {
        List<Recquisition> result = new ArrayList<>();
        for (Produit lp : lps) {
            List<Recquisition> rqs = RecquisitionDelegate.findRecquisitionByProduit(lp.getUid());
            //List<Recquisition> tofills = distinctBylotRecq(rqs);
            result.addAll(rqs
            // tofills
            );
        }
        return result;
    }

    public void addDelivery(Livraison l) {
        obl_livraisons_req.add(l);
    }

    public void addLivraison(Livraison liv) {
        Livraison l = LivraisonDelegate.findLivraison(liv.getUid());
        Fournisseur f = FournisseurDelegate.findFournisseur(liv.getFournId().getUid());
        if (f != null) {
            System.out.println("Fournisiiseur " + JsonUtil.jsonify(f).toString());
        } else {
            SuppliersController sup = SuppliersController.getInstance();
            sup.addSupplier(f);
        }
        if (l == null) {
            l = LivraisonDelegate.saveLivraison(liv);
            addDelivery(l);
        } else {
            l = LivraisonDelegate.updateLivraison(liv);
            int index = obl_livraisons_req.indexOf(l);
            if (index != -1) {
                obl_livraisons_req.set(index, liv);
            } else {
                addDelivery(l);
            }
        }

    }

    public void loadRegionsLocally() {
        for (String key : regKeys()) {
            String r = pref.get(key, "...");
            if (!regions.contains(r)) {
                regions.add(r);
            }
        }
    }

    public void setDatabase() {

        kazisafe = KazisafeServiceFactory.createService(token);
        lslgnventes = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        list_mode_ls = FXCollections.observableArrayList();
        ols_ligvt_retour = FXCollections.observableArrayList();
        ols_peremption = FXCollections.observableArrayList();
        categs = FXCollections.observableArrayList();
        lsreq = FXCollections.observableArrayList();
        obl_livraisons_req = FXCollections.observableArrayList();
        cbx_categofilter.setItems(categs);
        tablePeremption.setItems(ols_peremption);
        cbx_lgnvt_retour.setItems(ols_ligvt_retour);
        ols_mesure_retour = FXCollections.observableArrayList();
        ols_mesure_retour_depot = FXCollections.observableArrayList();
        mesure_retour.setItems(ols_mesure_retour);
        ols_retours_invent_items = FXCollections.observableArrayList();
        listvu_choosen4_retour_depot.setItems(ols_retours_invent_items);
        ols_recquis_retour_depot = FXCollections.observableArrayList();
        cbx_produit_retour_depot.setItems(ols_recquis_retour_depot);
        cbx_mesure_retour_depot.setItems(ols_mesure_retour_depot);
        cbx_region_retour_depot.setItems(regions);
        cbx_region_rupture.setItems(regions);
        prodx = FXCollections.observableArrayList();
        String meth = pref.get("meth", "FIFO");
        loadRegionsLocally();
//        Executors.newSingleThreadExecutor()
//                .execute(() -> {
//        JpaStorage dbase = JpaStorage.getInstance();
        obl_fournisseurs.setAll(FournisseurDelegate.findFournisseurs());
        categs.addAll(CategoryDelegate.findCategories());
        List<Livraison> foundl = LivraisonDelegate.findDescSortedByDate();
        obl_livraisons_req.addAll(foundl);
        list_livraison_req.setItems(obl_livraisons_req);
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            List<Recquisition> lsr = (List<Recquisition>) MainuiController.dataCache.get("pos-recq-" + role);
            if (lsr == null) {
                List<Recquisition> ls = RecquisitionDelegate.findRecquisitions();
                lsreq.setAll(ls);
                MainuiController.dataCache.put("pos-recq-" + role, ls);
            } else {
                lsreq.setAll(lsr);
            }
            table_req.setItems(lsreq);
            calcreq = RecquisitionDelegate.findRecquisitions();
            calclv = LigneVenteDelegate.findLigneVentes();
            calcvente = VenteDelegate.findVentes();
        } else {
            List<Recquisition> lsr = (List<Recquisition>) MainuiController.dataCache.get("pos-recq-" + role);
            if (lsr == null) {
                List<Recquisition> ls = RecquisitionDelegate.findRecquisitions(region);
                lsreq.setAll(ls);
                MainuiController.dataCache.put("pos-recq-" + role, ls);
            } else {
                lsreq.setAll(lsr);
            }
            table_req.setItems(lsreq);
            calcreq = RecquisitionDelegate.findRecquisitions(region);
            calclv = LigneVenteDelegate.findLigneVentes();
            calcvente = VenteDelegate.findVentes(region);
        }

//                });
        new Thread(() -> {
            List<Object[]> gds = RecquisitionDelegate.findGoods();//db.findGoods();
            for (Object[] gd : gds) {
                Produit pro = ProduitDelegate.findProduit(String.valueOf(gd[1]));
                prodx.add(pro);
            }
            // filterNullRecquisitionProduct();
            List<Recquisition> loadReqs = loadReqs(prodx);
            ols_recquis_retour_depot.addAll(loadReqs);
            if (cbx_produit_retour_depot != null && !loadReqs.isEmpty()) {
                cbx_produit_retour_depot.getSelectionModel().selectFirst();
            }
            int limit = Math.min(dataLoded, prodx.size());
            lisprod = FXCollections.observableArrayList(prodx.subList(origin, limit));
            String modec = pref.get("view-mode", "card");
            if (modec.equals("card")) {
                fillProducts(false, lisprod);
                scrollPos.setVisible(true);
                list_mode.setVisible(false);
                Util.setResourceImage(btn_view_mode, "list.png");
            } else {
                scrollPos.setVisible(false);
                list_mode.setVisible(true);
                fillProductInTable("All");
                Util.setResourceImage(btn_view_mode, "bloccard.png");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tbl_pro_count.setText(tbl_list_pro.getItems().size() + " elements");
                    }
                });
            }
            cbx_region_maginv.setItems(regions);
            cbx_region_venthist.setItems(regions);
            if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                cbx_region_maginv.setVisible(true);
                cbx_region_venthist.setVisible(true);
                cbx_region_rupture.setVisible(true);
            } else {
                cbx_region_maginv.setVisible(false);
                cbx_region_venthist.setVisible(false);
                cbx_region_rupture.setVisible(false);
            }
            treeSaleItems = FXCollections.observableArrayList();
            panier_list.setItems(lslgnventes);
            btn_pay_now.setDisable(lslgnventes.isEmpty());
            tab_history.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        refreshSales(null);
                    }
                }
            });
            tab_mag_inv.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {

                    }
                }
            });
            tab_requisition.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        // refreshRecqs(null);
                    }
                }
            });

            list_livraison_req.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Livraison>() {
                @Override
                public void changed(ObservableValue<? extends Livraison> observable, Livraison oldValue,
                        Livraison newValue) {
                    livraison = newValue;
                    if (livraison != null) {
                        ObservableList<Recquisition> stockrs = FXCollections
                                .observableArrayList(RecquisitionDelegate.findByReference(livraison.getReference()));
                        lbl_livrez_recq.setText(livraison.getFournId().getNomFourn() + " : " + livraison.getNumPiece() + ", "
                                + livraison.getDateLivr().toString() + " (" + stockrs.size() + " articles), Total : " + stockrs.stream()
                                .mapToDouble(r -> r.getCoutAchat() * r.getQuantite()).sum());
                        table_req.setItems(stockrs);
                        txt_table_req_count.setText(String.format(bundle.getString("xitems"), stockrs.size()));

                    }
                }
            });
            ContextMenu ctx = new ContextMenu();
            MenuItem addArt = new MenuItem("Ajouter un article");
            ctx.getItems().add(addArt);
            addArt.setOnAction((ActionEvent event) -> {
                Livraison liv = list_livraison_req.getSelectionModel().getSelectedItem();
                if (liv == null) {
                    MainUI.notify(null, "Erreur", "Veuillez selectionner une livraison", 3, "error");
                    return;
                }
                String prov = cbx_provenance_req.getValue();
                MainUI.floatDialog(tools.Constants.RECQ_DLG, 716, 746, null, kazisafe, new Object[]{tools.Constants.ACTION_CREATE, null, entreprise, prov, liv});
            });
            list_livraison_req.setContextMenu(ctx);
            tbcarts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tbcarts.getSelectionModel().selectedItemProperty()
                    .addListener(new ChangeListener<Vente>() {
                        @Override
                        public void changed(ObservableValue<? extends Vente> observable, Vente oldValue, Vente newValue) {
                            selectedCart = newValue;
                            selectedAvedCarts.add(newValue);
                        }
                    });
            if (prodx.size() > 20) {
                ScrollBar vbar = getVScrollbar(tbl_list_pro);
                if (vbar != null) {
                    vbar.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                            if (newValue.doubleValue() == vbar.getMax()) {
                                int or = tdataLoded;
                                tdataLoded += 10;
                                int limit = Math.min(tdataLoded, prodx.size());
                                if (or > limit) {
                                    return;
                                }
                                //List<Produit> prod = prodx.subList(or, limit);
                                fillProductInTable("All");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tbl_pro_count.setText(String.format(bundle.getString("xitems"), tbl_list_pro.getItems().size()));
                                    }
                                });
                            }
                        }
                    });
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tbl_pro_count.setText(tbl_list_pro.getItems().size() + " elements");
                    }
                });
            }
//                if (prodx.size() > 20) {
//                    ScrollBar vbar = getVScrollbar(tbl_list_pro);
//                    if (vbar != null) {
//                        vbar.valueProperty().addListener(new ChangeListener<Number>() {
//                            @Override
//                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                                if (newValue.doubleValue() == vbar.getMax()) {
//
//                                    int or = tdataLoded;
//                                    tdataLoded += 10;
//                                    int limit = Math.min(tdataLoded, prodx.size());
//                                    if (or > limit) {
//                                        return;
//                                    }
//                                    List<Produit> prod = prodx.subList(or, limit);
//                                    addProductInTable(prod);

        
        ////                            lisprod.addAll(prod);
////                            fillProducts(true, lisprod);
//                                    Platform.runLater(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            tbl_pro_count.setText(tbl_list_pro.getItems().size() + " elements");
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    }
//                }
        }).start();
        configViewlist();
        chbx_dettes_only.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    load_history.setVisible(true);
                    Executors.newSingleThreadExecutor()
                            .submit(new Runnable() {
                                @Override
                                public void run() {
                                    List<Vente> loop;
                                    if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
                                        loop = VenteDelegate.findCreditSalesFromRegion(region);
                                    } else {
                                        loop = VenteDelegate.findCreditSales();//db.findVenteCredit();
                                    }
                                    fillSaleHistory();
                                }
                            });

//                    for (Vente vs : db.findAll()) {
//                        double dette = vs.getMontantDette() == null ? 0 : vs.getMontantDette();
//                        if (dette > 0) {
//                            result.add(vs);
//                        }
//                    }
//                    if (!role.equals(Role.Trader.name())) {
//                        fillSaleHistory(result, region);
//                    } else {
//                       
//                    }
                } else {
                    fillSaleHistory();
                }
            }
        });
        if (meth.equals("ppps")) {
            peremption.setDisable(false);
        } else {
            peremption.setDisable(true);
        }

        peremption.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    enPeremption(null);
                }
            }
        });

        kazisafe.getRegions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            cbx_region_retour_depot.getSelectionModel().selectFirst();
                        }
                    });

                    int i = 0;
                    for (String reg : lreg) {
                        if (reg != null) {

                            pref.put("region" + (++i), reg);
                        }
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
        cbx_region_rupture.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                chbx_xall.setSelected(false);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            if (!newValue.isEmpty()) {
                                obl_rupture_list.setAll(RecquisitionDelegate.findStockEnRupture(newValue));
                            } else {
                                if (role.contains(Role.ALL_ACCESS.name()) | role.equals(Role.Trader.name())) {
                                    obl_rupture_list.setAll(RecquisitionDelegate.findStockEnRupture());
                                }
                            }
                            Platform.runLater(() -> {
                                rupt_count.setText(String.format(bundle.getString("xitems"), obl_rupture_list.size()));
                            });

                        });

            }
        });
        chbx_xall.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                ObservableList<Rupture> rup = tbl_rupture.getItems();
                if (newValue) {
//                    obl_rupture_list.forEach((rupture) -> {
//                        rupture.setSelect(true);
//                        obl_rupture_list.set(obl_rupture_list.indexOf(rupture), rupture);
//                    });

                    rup.forEach((rupture) -> {
                        rupture.setSelect(true);
                        rup.set(rup.indexOf(rupture), rupture);
                        valTotRupt += rupture.getQuant() * rupture.getUnitprice();
                    });
                    commandelist.addAll(rup);
                    valeur_tot.setText("Total : " + valTotRupt + " USD");
                } else {
                    rup.forEach((rupture) -> {
                        rupture.setSelect(false);
                        rup.set(rup.indexOf(rupture), rupture);
                    });
                    commandelist.clear();
                    valeur_tot.setText("Total : 0.0 USD");
                }
                Platform.runLater(() -> {
                    rupt_count.setText(String.format(bundle.getString("xitems"), rup.size()));
                });
            }
        });

        cbx_region_maginv.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    region = newValue;
                    populateInv(newValue, null, null);

                }
            }
        });
        cbx_region_venthist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    region = newValue;
                    fillSaleHistory();
                }
            }
        });

//        tbl_list_pro.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends ListViewItem> observable, ListViewItem oldValue, ListViewItem newValue) -> {
//            if (newValue != null) {
//
//                Produit p = newValue.getProduit();
//                MainUI.floatDialog(tools.Constants.PANIER_DLG, 430, 497, null, kazisafe, p, entreprise, "Create", -1);
//                System.out.println("Test " + p.getCodebar());
//                tbl_list_pro.getSelectionModel().clearSelection();
//            }
//        });
        tbl_list_pro.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {

                newValue = tbl_list_pro.getSelectionModel().getSelectedItem();
                if (newValue != null) {
                    Produit p = newValue.getProduit();
                    MainUI.floatDialog(tools.Constants.PANIER_DLG, 430, 497, null, kazisafe, p, entreprise, "Create", -1);
                    System.out.println("Test " + p.getCodebar());
                }
            }
        });

        cbx_categofilter.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Category> observable, Category oldValue, Category newValue) -> {
                    if (newValue != null) {
                        if (role.equals(Role.Trader.name()) | role.equals(Role.ALL_ACCESS.name())) {
                            fillProductInTable(newValue.getUid());
                        } else {
                            fillProductInTable(newValue.getUid());
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
            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public Recquisition getHeaderRecq(Produit prod) {
        String meth = pref.get("meth", "fifo");
        List<Recquisition> lsks = new ArrayList<>();
        if (meth.equals("ppps")) {
            lsks.addAll(RecquisitionDelegate.toFefoOrdering(prod.getUid()));
        } else if (meth.equals("fifo")) {
            lsks.addAll(RecquisitionDelegate.toFifoOrdering(prod.getUid()));
            //db.findByProduitOrderByDateAsc(Recquisition.class, prod.getUid());
        } else if (meth.equals("lifo")) {
            lsks.addAll(RecquisitionDelegate.toLifoOrdering(prod.getUid()));
        }
        //System.out.println("Produit XXXXXXXXXXXX " + prod.getNomProduit() + " size " + lsks.size());
        return lsks.isEmpty() ? null : lsks.get(0);
    }

    private List<Produit> distinctProduct(Collection<Produit> ls) {
        List<Produit> lps = new ArrayList<>();
        for (Produit l : ls) {
            Produit p = Util.findProduitByCodebar(lps, l.getCodebar());
            if (p == null) {
                lps.add(l);
            }
        }
        return lps;
    }

    @FXML
    public void showRetourDepotPane(Event e) {
        pane_retour_depot.setVisible(true);
    }

    @FXML
    private void refreshRecqs(Event et) {
        if (role.equals(Role.Trader.name())) {
            lsreq.setAll(RecquisitionDelegate.findRecquisitions());
            table_req.setItems(lsreq);
        } else {
            lsreq = FXCollections.observableArrayList(RecquisitionDelegate.findRecquisitions(region));//db.findAllByRegion(Recquisition.class, region));
            table_req.setItems(lsreq);
        }

        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    Refresher rfr = new Refresher("SALES");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Util.sync(rfr, "read", Tables.REFRESH);
                });

    }

    @FXML
    private void deleteRecqs(Event et) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez vous vraiment suprimmer cette recquisition ?", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            if (choosenReq != null) {
                if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                    // db.delete(choosenReq);
                    RecquisitionDelegate.deleteRecquisition(choosenReq);
                    MainUI.notify(null, "Suppression", "Récquisition suprimé avec succès", 4, "info");
                }
            } else {
                MainUI.notify(null, "Erreur de suppression", "Aucune ligne n'a été séléctionnée", 4, "error");
            }
        }
    }

    @FXML
    public void cleanSavedCart(Event e) {
        if (!selectedAvedCarts.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez vous vraiment suprimmer les element selectiones ?", ButtonType.YES, ButtonType.CANCEL);
            alert.setTitle("Attention!");
            alert.setHeaderText(null);
            Optional<ButtonType> showAndWait = alert.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                List<LigneVente> lv = LigneVenteDelegate.findByReference(selectedCart.getUid());
                for (LigneVente l : lv) {
                    LigneVenteDelegate.deleteLigneVente(l);
                    //db.delete(l);
                }
                VenteDelegate.deleteVente(selectedCart);
                //db.delete(choosenVente);
//                }

                savedCarts.removeAll(selectedAvedCarts);
                selectedAvedCarts.clear();
                MainUI.notify(null, bundle.getString("success"), "Elements supprimes avec succes", 3, "info");
                selectedCart = null;
            }

        } else {
            MainUI.notify(null, bundle.getString("error"), "Selectionnez au moins un element puis continuer", 3, "error");
        }
    }

    @FXML
    public void uploadToCloud(Event e) {
        syncIndicator.setVisible(true);
        SyncEngine se = SyncEngine.getInstance();
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit((Runnable) () -> {
            String finisf = SyncEngine.getInstance().syncInBackground();
            if (finisf.contains("finish")) {
                syncIndicator.setVisible(false);
                String op = pref.get("operator", "Un utilisateur");
                Refresher ref = new Refresher(op + " a fini la synchronisation montante avec le serveur");
                Util.sync(ref, "read", Tables.REFRESH);
            }
        });
//        Future<Boolean> finish = exec.submit(se);
//        try {
//            
//        } catch (InterruptedException ex) {
//            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ExecutionException ex) {
//            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    @FXML
    private void refreshSales(Event e) {
        load_history.setVisible(true);
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    treeSaleItems.clear();
                    region = pref.get("region", "...");
                    if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                        fillSaleHistory();
                    } else {
                        List<Vente> lvts = VenteDelegate.findVentes(region);
                        fillSaleHistory(lvts);
                    }
                });
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    Refresher rfr = new Refresher("SALES");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Util.sync(rfr, "read", Tables.REFRESH);
                });

    }

    private List<Traisorerie> getPaidByRef(List<Traisorerie> ltr, String ref) {
        List<Traisorerie> rst = new ArrayList<>();
        for (Traisorerie t : ltr) {
            String r = "Recouvrement dette " + ref;
            if (t.getLibelle().equals(r)) {
                rst.add(t);
            }
        }
        return rst;
    }

    private void populate(List<Vente> sold, String region) {
        //SyncEngine.getInstance().shutdown();
        treeSaleItems.clear();
        List<SaleItem> lsi = new ArrayList<>();
        for (Vente v : sold) {
            if (v.getObservation().equals("Drafted")) {
                continue;
            }
            if (region == null) {
                List<LigneVente> lvs = LigneVenteDelegate.findByReference(v.getUid());
                System.out.println("Today - " + v.getDateVente() + " : " + lvs.size());
                SaleItem si = new SaleItem();
                si.setClient(v.getClientId());
                si.setDate(v.getDateVente());
                si.setFacture(v.getReference());
                si.setIdVente(v.getUid());
                si.setDateEcheance(v.getEcheance());
                si.setSaleAmountCdf(v.getMontantCdf());
                si.setSaleAmountUsd(v.getMontantUsd());
                si.setLibelle(v.getLibelle());
                double paym = sumAllCurrency(v.getUid());
                si.setSaleAmountCredit(v.getMontantDette());
//                if (lvs != null) {
                si.setItems(lvs);
//                } else {
//                    List<LigneVente> lvt = LigneVenteDelegate.findByReference(v.getUid());
//                    //  Util.getLigneVenteForVente(jpas.findAll(), v.getUid());
//                    si.setItems(lvt);
//                }
                if (si.getItems().isEmpty()) {
                    List<LigneVente> items = getItemsForVente(v.getUid());
                    // System.err.println("Items value " + items.size());
                    if (items != null) {
                        si.setItems(items);
                    }

                }
                lsi.add(si);
            } else {
                if (v.getRegion() == null) {
                    continue;
                }
                if (v.getRegion().equals(region)) {
                    List<LigneVente> lvs = LigneVenteDelegate.findByReference(v.getUid());
                    SaleItem si = new SaleItem();
                    si.setClient(v.getClientId());
                    si.setDate(v.getDateVente());
                    si.setFacture(v.getReference());
                    si.setIdVente(v.getUid());
                    si.setDateEcheance(v.getEcheance());
                    si.setSaleAmountCdf(v.getMontantCdf());
                    si.setSaleAmountUsd(v.getMontantUsd());
                    si.setLibelle(v.getLibelle());
                    double paym = sumAllCurrency(v.getUid());
                    System.out.println("Dette OKKK " + v.getMontantDette() + " " + v.getReference());
                    si.setSaleAmountCredit(v.getMontantDette());
                    if (lvs != null) {
                        si.setItems(lvs);
                    } else {
                        List<LigneVente> lvt = LigneVenteDelegate.findByReference(v.getUid());
                        si.setItems(lvt);
                    }

                    if (si.getItems().isEmpty()) {
                        List<LigneVente> items = getItemsForVente(v.getUid());
                        System.err.println("Items value byref size " + items.size());
                        si.setItems(items);
                    }
                    lsi.add(si);
                }
            }
        }
        refreshHistory(lsi, Util.extractDates(VenteDelegate.findVentes(), region));
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    Refresher rfr = new Refresher("SALES");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Util.sync(rfr, "read", Tables.REFRESH);
                });
        // SyncEngine.getInstance().restart();
    }

    private List<LigneVente> getItemsForVente(int vid) {
        List<LigneVente> lvs = LigneVenteDelegate.findByReference(vid);//db.findByRef(LigneVente.class, vid);
        return lvs;
    }

    private void fillSaleHistory() {
        rootView.getChildren().clear();
        List<Vente> ventes;
        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
            if (chbx_dettes_only.isSelected()) {
                ventes = VenteDelegate.findCreditSales();
            } else {
                ventes = VenteDelegate.findVentes();
            }

        } else {
            if (chbx_dettes_only.isSelected()) {
                ventes = VenteDelegate.findCreditSalesFromRegion(region);
            } else {
                ventes = VenteDelegate.findVentes(region);
            }
        }
        populate(ventes, region);
    }

    private void fillSaleHistory(List<Vente> ventes) {
        rootView.getChildren().clear();
        populate(ventes, region);
    }

    private boolean isSameLotExistInRecqs(List<Recquisition> rqs, String numlot) {
        for (Recquisition recquisition : rqs) {
            if (recquisition.getReference().startsWith("RTR")) {
                continue;
            }
            String lot = recquisition.getNumlot();
            if (lot == null) {
                continue;
            }
            if (recquisition.getNumlot().equals(numlot)) {
                return true;
            }
        }
        return false;
    }

    private List<Recquisition> distinctBylotRecq(List<Recquisition> reqs) {
        List<Recquisition> result = new ArrayList<>();
        for (Recquisition req : reqs) {
            if (req.getReference().startsWith("RTR")) {
                continue;
            }
            if (!isSameLotExistInRecqs(result, req.getNumlot())) {
                result.add(req);
            }
        }
        return result;
    }

    private double sumAllRecqInPcsWithLot(String idpro, String lot) {
        double result
                = RecquisitionDelegate.sumByProduitWithLotInUnit(idpro, lot);
        return result;
    }

    private double sumAllLigneVenteInPcsWithLot(String idpro, String lot) {
        double result = LigneVenteDelegate.sumByProduitWithLotInUnit(idpro, lot);
        return result;
    }

    private double lotDiff(String pro, String lot) {
        double in = sumAllRecqInPcsWithLot(pro, lot);
        double out = sumAllLigneVenteInPcsWithLot(pro, lot);
        return in - out;
    }

    @FXML
    private void refreshInvMag(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    populateInv();
                });
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Refresher rfr = new Refresher("SALES");
                            rfr.setAction("read");
                            rfr.setCount(1);
                            rfr.setCounter(1);
                            Util.sync(rfr, "read", Tables.REFRESH);
                        });
            }
        }).start();

    }

    @FXML
    public void inventoryMagInInterval(Event e) {
        if (dpk_debut_inv_mag.getValue() != null && dpk_fin_inv_mag.getValue() != null) {
            Executors.newSingleThreadExecutor()
                    .execute(() -> {
                        populateInv(region, Constants.Datetime.toUtilDate(dpk_debut_inv_mag.getValue()), Constants.Datetime.toUtilDate(dpk_fin_inv_mag.getValue()));
                        table_inv_mag.setItems(FXCollections.observableArrayList(lsinventaire));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                txt_tbl_inv_mag_count.textProperty().setValue(lsinventaire.size() + " elements");
                            }
                        });
                    });
        }
    }

    private void populateInv() {
        populateInv(region, null, null);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txt_tbl_inv_mag_count.textProperty().setValue(lsinventaire.size() + " elements");
            }
        });
    }

    private void populateInv(String region, Date d1, Date d2) {
        lsinventaire.clear();
        List<Object[]> goods = RecquisitionDelegate.findGoodsFromRegion(region);
        this.region = region;
        valTotStock = 0;
        for (Object[] good : goods) {
            InventoryMagasin im = new InventoryMagasin();
            Double ca = Double.valueOf(String.valueOf(good[9]));
            im.setCoutAchat(ca);
            im.setLot(String.valueOf(good[10]));
            Produit pro = ProduitDelegate.findProduit(String.valueOf(good[1]));
            im.setProduit(pro);
            if (d1 == null && d2 == null) {
                im.setQuantEntree(getQuant(pro.getUid(), true));
                im.setQuantSortie(getQuant(pro.getUid(), false));
            } else {
                im.setQuantEntree(getQuant(pro.getUid(), d1, d2, true));
                im.setQuantSortie(getQuant(pro.getUid(), d1, d2, false));
            }
            if (!Objects.isNull(good[11])) {
                try {
                    String exp = String.valueOf(good[11]);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date ex = sdf.parse(String.valueOf(exp));
                    im.setExpiry(Constants.Datetime.toLocalDate(ex));
                } catch (ParseException ex) {
                    Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String req = String.valueOf(good[0]);
            Recquisition reqz = RecquisitionDelegate.findRecquisition(req);
            Mesure mez = MesureDelegate.findMesure(String.valueOf(good[2]));
            im.setMesure(mez);
            im.setAlerte(reqz.getStockAlert());
            List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(pro.getUid());
            if (mesures.isEmpty()) {
                continue;
            }
            Set<PrixDeVente> prices = PrixDeVenteDelegate.findPricesSetForRecq(req);
            String px = stringfyprices(prices);
            im.setPrixDeVente(px);
            double rest = Double.parseDouble(String.valueOf(good[7]));
            im.setLocalisation(getLocation(pro.getUid()));
            im.setQuantStock(rest);
            double somVal = rest * im.getCoutAchat();
            im.setValeurStock(somVal);
            lsinventaire.add(im);
            valTotStock += somVal;
        }
        table_inv_mag.setItems(lsinventaire);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txt_tbl_inv_mag_count.textProperty().setValue(lsinventaire.size() + " elements");
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txt_inv_mag.setText("Valeur du stock : " + BigDecimal.valueOf(valTotStock).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            }
        });
    }

    private String stringfyprices(Set<PrixDeVente> prices) {
        if (prices.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (PrixDeVente price : prices) {
            sb.append(price.getQmin())
                    .append("-")
                    .append(price.getQmax())
                    .append(":")
                    .append(price.getPrixUnitaire())
                    .append(",");
        }
        if (sb.toString().endsWith(",")) {
            sb.replace(sb.length() - 1, sb.length(), "");
        }
        sb.append("]");
        return sb.toString();
    }

    private double sumInUsdVente(Vente vente) {
        return vente.getMontantUsd() + (vente.getMontantCdf() / taux2change) + vente.getMontantDette();
    }

    public double sumAllCurrency(int uid) {
        Vente v = VenteDelegate.findVente(uid);
        if (v != null) {
            if (v.getEcheance() != null) {
                Double rst = VenteDelegate.sumPayedCredit("F-" + v.getReference(), taux2change);
                return rst == null ? 0 : rst;
            }
        }
        return 0;
    }

    double sumLotPc(List<LigneVente> ls, String numlot) {
        double s_qlot = 0;
        for (LigneVente l : ls) {
            if (l.getNumlot().equals(numlot)) {
                Mesure mz = l.getMesureId();
                Mesure m = MesureDelegate.findMesure(mz.getUid());
                s_qlot += l.getQuantite() * m.getQuantContenu();
            }
        }
        return s_qlot;
    }

    private Integer[] getVenteIdValues(List<Vente> lvid) {
        Integer[] vids = new Integer[lvid.size()];
        for (int i = 0; i < lvid.size(); i++) {
            vids[i] = lvid.get(i).getUid();
        }
        return vids;
    }

    public boolean isAlertReached(String idPro) {
        List<Recquisition> entrees;
        if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
            entrees = RecquisitionDelegate.findRecquisitionByProduitRegion(idPro, region);
        } else {
            entrees = RecquisitionDelegate.findRecquisitionByProduit(idPro);
        }
        Recquisition last = RecquisitionDelegate.findDescSortedByDateForProduit(idPro).get(0);
        //Util.findLastRecquisitionFor(entrees, idPro);

        List<LigneVente> sorties;
        if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
            sorties = LigneVenteDelegate.findByProduitRegion(idPro, region);//Util.findLigneVenteForProduit(db.findAll(),idPro, region);
        } else {
            sorties = LigneVenteDelegate.findByProduit(idPro);
        }
        //en piecement
        double epc = 0, spc = 0;
        for (Recquisition r : entrees) {
            Mesure mz = r.getMesureId();
            Mesure m = MesureDelegate.findMesure(mz.getUid());
            if (m == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(idPro);
                m = mesures.get(0);
            }
            epc += r.getQuantite() * m.getQuantContenu();
        }
        for (LigneVente lv : sorties) {
            Mesure mz = lv.getMesureId();

            Mesure m = (mz == null) ? mesure1(idPro) : MesureDelegate.findMesure(mz.getUid());
            if (m == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(idPro);//db.findByProduitAsc(Mesure.class, idPro, "quantContenu");
                m = mesures.get(0);
            }
            spc += lv.getQuantite() * m.getQuantContenu();
        }
        if (last == null) {
            return false;
        }
        Mesure mz = last.getMesureId();
        Mesure m = MesureDelegate.findMesure(mz.getUid());
        if (m == null) {
            List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(idPro);//db.findByProduitAsc(Mesure.class, idPro, "quantContenu");
            m = mesures.get(0);
        }
        double alert = (last.getStockAlert() == null ? 0 : last.getStockAlert()) / (m.getQuantContenu() == null ? 1 : m.getQuantContenu());
        double rest = epc - spc;
        return rest <= alert;
    }

    private Mesure mesure1(String idpro) {
        List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(idpro);//db.findByProduitAsc(Mesure.class, idPro, "quantContenu");
        return (mesures.isEmpty()) ? null : mesures.get(0);

    }

    /**
     * Cette fonction test si un produit sera expiree dans 6 mois ou pas
     *
     * @param idPro l'id du produit dont on va tester la peremption
     * @return true si le produit expire dans 6 mois et false dans le cas
     * contraire
     */
    public boolean isExpiredSoon(String idPro) {
        List<Recquisition> entrees;
        if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
            entrees = RecquisitionDelegate.findRecquisitionByProduitRegion(idPro, region);//db.findByProduit(Recquisition.class, idPro, region);//Util.findRequisitionForProduit(db.findByRegion(region), idPro);
        } else {
            entrees = RecquisitionDelegate.findRecquisitionByProduit(idPro);//db.findByProduit(Recquisition.class, idPro);
        }
        Recquisition last = Util.findLastRecquisitionFor(entrees, idPro);
        if (last == null) {
            return false;
        }
        LocalDate expDate = last.getDateExpiry();
        if (expDate == null) {
            return false;
        }
        long time = Constants.Datetime.dateInMillis(expDate);
        long now = System.currentTimeMillis();
        long sixMonth = 3600000 * 24 * 30 * 6;
        long timeDiff = time - now;
        return timeDiff <= sixMonth;
    }

    public double getRestPCFor(List<Recquisition> lr, List<LigneVente> llv) {
        // List<Recquisition> lrq = fullMesureRecqs(lr);
        // List<LigneVente> ll = fullMesureRecqs(llv);
        double qe = Util.sumQuantInPc(lr);
        System.out.println("QER " + qe);
        double so = Util.sumQuantInPc(llv);
        System.out.println("QSR " + so);
        double rest = qe - so;
        return rest;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;

    }

    public Vente getSelectedCart() {
        return selectedCart;
    }

    @FXML
    private void refreshRecquisition(Event et) {
        refreshRecqs(et);
    }

    private void saveRetourDepot(ActionEvent et) {
        saveRetourAuDepot(et);
    }

    @FXML
    private void clearArticles(MouseEvent et) {
//        oblcart.clear();
//        table_cart.setItems(oblcart);
    }

    @FXML
    private void goToAccessories(MouseEvent et) {
//        MainUI.getPage(kazisafe, "accessories.fxml", null);
    }

    @FXML
    private void goToHistory(MouseEvent et) {
//        MainUI.getPage(kazisafe, "history.fxml", null);
    }

    @FXML
    private void refreshVentesHttp(MouseEvent et) {
        refreshSales(et);
    }

    @FXML
    private void saleHere(ActionEvent et) {
        // Placeholder
    }

    @FXML
    private void saveCompter(ActionEvent et) {
        Inventaire inv = cbx_inventaire_compter.getValue();
        if (inv == null) {
            MainUI.notify(null, "Erreur", "Veuillez d'abord sélectionner un inventaire.", 3, "error");
            return;
        }
        if (!"en cours...".equalsIgnoreCase(inv.getEtat())) {
            MainUI.notify(null, "Action impossible", "L'inventaire n'est pas en état 'en cours...'.", 3, "error");
            return;
        }

        Produit p = cbx_produit_compter.getValue();
        Mesure m = cbx_mesure_compter.getValue();
        if (p == null || m == null || tf_quantite_compter.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez remplir les champs obligatoires (Produit, Mesure, Quantité).", 3, "error");
            return;
        }

        try {
            Compter c = new Compter();
            c.setUid(DataId.generate());
            c.setInventaireId(inv);
            c.setProductId(p);
            c.setMesureId(m);
            c.setQuantite(Double.parseDouble(tf_quantite_compter.getText()));
            c.setNumlot(tf_numlot_compter.getText());
            c.setRegion(region);
            c.setDateCount(LocalDateTime.now());
            c.setObservation(input_observ_comptage.getText());
            c.setCoutAchat(tf_coutachat_compter.getText().isEmpty() ? 0 : Double.parseDouble(tf_coutachat_compter.getText()));
            if (dpk_date_exp_compter.getValue() != null) {
                c.setDateExpiration(dpk_date_exp_compter.getValue());
            }

            // Calcul theorique tire de StockAgregate
            data.StockAgregate sa = delegates.RepportDelegate.findCurrentStock(p, inv.getRegion(), LocalDate.now(), LocalDate.now());

            double theorik = (sa != null && sa.getFinalQuantity() != null) ? sa.getFinalQuantity() : 0.0;
            c.setQuantiteTheorik(theorik);

            double ecart = c.getQuantite() - (theorik / m.getQuantContenu());
            c.setEcart(ecart);

            // Mettre a jour le total ecart en valeur
            double existingVal = inv.getValeurTotalEcart() != null ? inv.getValeurTotalEcart() : 0.0;
            inv.setValeurTotalEcart(existingVal + (ecart * c.getCoutAchat()));
            delegates.InventaireDelegate.updateInventaire(inv);
            saveInventaireByHttp(inv);

            Compter saved = CompterDelegate.createCompter(c);
            obl_comptages.add(0, comptageRender(saved));
            Mesure mz = saved.getMesureId();
            double cau = saved.getCoutAchat() / mz.getQuantContenu();
            RecquisitionDelegate.rectifyStock(saved.getProductId(), LocalDate.now(), LocalDate.now(), region, cau);
            MainUI.notify(null, "Succès", "Comptage enregistré.", 2, "info");

            // Clear inputs
            tf_quantite_compter.clear();
            input_observ_comptage.clear();
        } catch (NumberFormatException ex) {
            MainUI.notify(null, "Erreur format", "Veuillez vérifier les valeurs numériques.", 3, "error");
        }
    }

    @FXML
    private void saveInventaire(ActionEvent et) {
        if (!canCreateInventory.get()) {
            MainUI.notify(null, "Accès refusé", "Vous n'avez pas la permission de créer ou modifier un inventaire.", 3, "error");
            return;
        }

        if (tf_code_inv_compter.getText().isEmpty() || cbx_etat_inv_compter.getValue() == null) {
            MainUI.notify(null, "Erreur", "Le code et l'état sont obligatoires.", 3, "error");
            return;
        }

        Inventaire inv = cbx_inventaire_compter.getValue();
        boolean isUpdate = inv != null && inv.getUid() != null;

        if (!isUpdate) {
            inv = new Inventaire();
            inv.setUid(DataId.generate());
        }

        inv.setCodeInventaire(tf_code_inv_compter.getText());
        inv.setEtat(cbx_etat_inv_compter.getValue());
        inv.setRegion(cbx_region_inv_compter.getValue() == null ? region : cbx_region_inv_compter.getValue());
        inv.setComment(tf_comment_inv_compter.getText());
        inv.setDateDebut(dpk_datedebut_inv_compter.getValue());
        inv.setDateFin(dpk_datefin_inv_compter.getValue());

        if (isUpdate) {
            InventaireDelegate.updateInventaire(inv);
            saveInventaireByHttp(inv);
            MainUI.notify(null, "Succès", "Inventaire mis à jour.", 2, "info");
        } else {
            Inventaire saved = InventaireDelegate.createInventaire(inv);
            obl_inventaires.add(0, saved);
            saveInventaireByHttp(saved);
            cbx_inventaire_compter.getSelectionModel().select(saved);
            MainUI.notify(null, "Succès", "Inventaire créé.", 2, "info");
        }
    }

    private void initInventoryTab() {
        obl_inventaires = FXCollections.observableArrayList();
        cbx_inventaire_compter.setItems(obl_inventaires);
        obl_comptages = FXCollections.observableArrayList();
        states = FXCollections.observableArrayList("Non commencé", "En cours...", "Terminé");

        canCreateInventory.set(PermissionDelegate.hasPermission(PermitTo.CREATE_INVENTORY)
                || role.contains("ALL_ACCESS") || role.equals(Role.Trader.name()));

        cbx_etat_inv_compter.setItems(states);
        cbx_region_inv_compter.setItems(regions);

        MainUI.setPattern(dpk_date_today_compter);
        MainUI.setPattern(dpk_date_exp_compter);
        MainUI.setPattern(dpk_datefin_inv_compter);
        MainUI.setPattern(dpk_datedebut_inv_compter);
        dpk_date_today_compter.setValue(LocalDate.now());

        // Setup table
        tbl_comptage_inv.setItems(obl_comptages);
        col_code_inv_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodeInventaire()));
        col_etat_inv_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEtatInventaire()));
        col_debut_date_inv_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDebutInventair().toString()));
        col_date_today_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateCompter().toString()));
        col_produit_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduit().getNomProduit()));
        col_stk_theorik_compter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantiteTheorik()));
        col_quantite_compter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantite()));
        col_quant_ecart_compter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getEcart()));
        col_valeur_unit_compter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCoutAchat()));

        col_valeur_tot_compter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantite() * cellData.getValue().getCoutAchat()));
        col_val_ecart_compter.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getEcart() * cellData.getValue().getCoutAchat()));
        col_lot_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumlot()));
        col_date_perem_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateExpiration() != null ? cellData.getValue().getDateExpiration().toString() : ""));
        col_ecart_obs_compter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObservation()));
        this.tbl_comptage_inv.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            if (t1 != null) {
                Inventaire inv = InventaireDelegate.findInventaireByCode((String) t1.getCodeInventaire());
                this.tf_code_inv_compter.setText(t1.getCodeInventaire());
                this.cbx_etat_inv_compter.setValue(t1.getEtatInventaire());
                this.cbx_region_inv_compter.setValue(inv.getRegion());
                this.dpk_datedebut_inv_compter.setValue(inv.getDateDebut());
                this.dpk_datefin_inv_compter.setValue(inv.getDateFin());
                this.tf_comment_inv_compter.setText(inv.getComment());
                this.cbx_inventaire_compter.setValue(inv);
                this.dpk_date_today_compter.setValue(t1.getDateCompter().toLocalDate());
                this.dpk_date_exp_compter.setValue(t1.getDateExpiration());
                this.cbx_produit_compter.setValue(t1.getProduit());
                this.cbx_mesure_compter.setValue(t1.getMesure());
                this.tf_coutachat_compter.setText(Double.toString(t1.getCoutAchat()));
                this.tf_numlot_compter.setText(t1.getNumlot());
                this.tf_quantite_compter.setText(Double.toString(t1.getQuantite()));
                this.choosenCompter = CompterDelegate.findCompterByInventaireAndProduit(inv.getUid(), t1.getProduit().getUid());
            }
        });

        // Converters
        cbx_inventaire_compter.setConverter(new StringConverter<Inventaire>() {
            @Override
            public String toString(Inventaire o) {
                return o == null ? "" : o.getCodeInventaire() + " du " + o.getDateDebut().toString() + " (" + o.getEtat() + ")".toUpperCase();
            }

            @Override
            public Inventaire fromString(String s) {
                return cbx_inventaire_compter.getItems()
                        .stream()
                        .filter(o -> (o != null && (o.getCodeInventaire() + " du " + o.getDateDebut().toString() + " (" + o.getEtat() + ")").toUpperCase()
                        .equalsIgnoreCase(s.toUpperCase())))
                        .findFirst().orElse(null);
            }
        });
        cbx_produit_compter.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null : object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_produit_compter.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_mesure_compter.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure_compter.getItems()
                        .stream()
                        .filter(v -> (v.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        // Listeners
        cbx_inventaire_compter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tf_code_inv_compter.setText(newVal.getCodeInventaire());
                cbx_etat_inv_compter.setValue(newVal.getEtat());
                cbx_region_inv_compter.setValue(newVal.getRegion());
                tf_comment_inv_compter.setText(newVal.getComment());
                dpk_datedebut_inv_compter.setValue(newVal.getDateDebut());
                dpk_datefin_inv_compter.setValue(newVal.getDateFin());
                // Load associated counts
                List<Compter> lcs = CompterDelegate.findCompterBYInventaire(newVal.getUid());
                obl_comptages.setAll(comptageRender(lcs, newVal));
                txt_valeur_global_compter.setText("Valeur comptee : " + newVal.getValeurTotal());
                txt_valeurtotal_ecart_compter.setText("Ecart en valeur : " + newVal.getValeurTotalEcart());
            }
        });

        cbx_produit_compter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cbx_mesure_compter.setItems(FXCollections.observableArrayList(MesureDelegate.findAscSortedByQuantWithProduit(newVal.getUid())));
                cbx_mesure_compter.getSelectionModel().selectFirst();
                String meth = pref.get("meth", "FIFO");
                Recquisition rq = RecquisitionDelegate.getLastEntry(meth, newVal, region);
                tf_numlot_compter.setText(rq.getNumlot());
                tf_coutachat_compter.setText(String.valueOf(rq.getCoutAchat()));
                if (rq.getDateExpiry() != null) {
                    dpk_date_exp_compter.setValue(rq.getDateExpiry());
                }
                // Update theoretical stock label from StockAgregate
                data.StockAgregate sa = delegates.RepportDelegate.findCurrentStock(newVal, region, LocalDate.now(), LocalDate.now());
                double theorik = (sa != null && sa.getFinalQuantity() != null) ? sa.getFinalQuantity() : 0.0;
                txt_stkheorik_comptage.setText("Theor.: " + theorik);
                updateEcartLive();
            }
        });

        tf_quantite_compter.textProperty().addListener((obs, oldV, newV) -> updateEcartLive());
        cbx_mesure_compter.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateEcartLive());
        NotificationHandler.setOnDataSyncListener(basemodel -> {
            int index = -2;
            if (basemodel instanceof Inventaire) {
                Inventaire inventaire = (Inventaire) basemodel;
                if (this.obl_inventaires == null) {
                    return;
                }
                index = this.obl_inventaires.indexOf(inventaire);
                if (index == -1) {
                    this.obl_inventaires.add(inventaire);
                } else {
                    this.obl_inventaires.set(index, inventaire);
                }
                if (inventaire.getEtat().equalsIgnoreCase("Terminé")) {
                    CompterDelegate.closeInventoryByFixingNoCounts(inventaire);
                }
            } else if (basemodel instanceof Compter) {
                Compter compter = (Compter) basemodel;

                if (this.obl_comptages == null) {
                    return;
                }
                ComptageItem ci = this.comptageRender(compter);
                index = this.obl_comptages.indexOf(ci);
                if (index == -1) {
                    this.obl_comptages.add(ci);
                } else {
                    this.obl_comptages.set(index, ci);
                }
                System.out.println("incoming expiry " + String.valueOf(compter.getDateExpiration()));
            } else if (basemodel instanceof Recquisition) {
                Recquisition req = (Recquisition) basemodel;
                if (!PrixDeVenteDelegate.findPricesForRecq((String) req.getUid()).isEmpty()) {
                    this.refreshPosUi();
                }
            } else if (basemodel instanceof PrixDeVente) {
                PrixDeVente pv = (PrixDeVente) basemodel;
                if (RecquisitionDelegate.findRecquisition((String) pv.getRecquisitionId().getUid()) != null) {
                    this.refreshPosUi();
                }
            } else if (basemodel instanceof LigneVente) {
                LigneVente i = (LigneVente) basemodel;
                this.refreshPosUi();
            }
            System.out.println(">>>>>>>>>>> index " + index + ">>>>>>>>>>>>>>>>uid enregirte venant de synchronization = " + basemodel.getType());
        });

        // Load data
        obl_inventaires.setAll(InventaireDelegate.findInventaires(region));
        obl_inventaires.add(null);
//        cbx_inventaire_compter.setItems(obl_inventaires);
        cbx_produit_compter.setItems(FXCollections.observableArrayList(ProduitDelegate.findProduits()));
        new ComboBoxAutoCompletion<>(cbx_produit_compter);
    }

    private void updateEcartLive() {
        if (txt_stkheorik_comptage.getText() == null || !txt_stkheorik_comptage.getText().contains(":")) {
            return;
        }
        try {
            double theorik = Double.parseDouble(txt_stkheorik_comptage.getText().split(":")[1].trim());
            double qte = tf_quantite_compter.getText().isEmpty() ? 0 : Double.parseDouble(tf_quantite_compter.getText());
            Mesure m = cbx_mesure_compter.getValue();
            if (m != null && m.getQuantContenu() != null && m.getQuantContenu() > 0) {
                double ecart = qte - (theorik / m.getQuantContenu());
                txt_ecart_comptage.setText(String.format("Ecart: %.2f", ecart));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void saveInventaireByHttp(data.Inventaire inv) {
        if (kazisafe == null) {
            return;
        }
        java.util.concurrent.Executors.newSingleThreadExecutor().submit(() -> {
            int attempt = 0;
            while (attempt < 5) {
                try {
                    retrofit2.Response<data.Inventaire> response = kazisafe.createInventair(inv).execute();
                    if (response.code() == 200 || response.code() == 201) {
                        break;
                    }
                } catch (java.io.IOException e) {
                }
                attempt++;
                try {
                    java.util.concurrent.TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, attempt));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    @FXML
    public void saveCompter(Event e) {
        if (cbx_inventaire_compter.getValue() == null || this.tf_coutachat_compter.getText().isBlank() || this.tf_quantite_compter.getText().isBlank() || this.tf_numlot_compter.getText().isBlank()) {
            MainUI.notify(null, (String) "", (String) "Completer les champs non facultatives du cote comptage puis reessayer", (long) 5L, (String) "error");
            return;
        }
        if (cbx_inventaire_compter.getValue().getEtat().equals("Terminé")) {
            MainUI.notify(null, (String) "", (String) "Impossible d'ajouter un pointage sur un inventaire cloture", (long) 4L, (String) "error");
            return;
        }
        if (cbx_inventaire_compter.getValue().getEtat().equalsIgnoreCase("Non commencé")) {
            cbx_inventaire_compter.getValue().setEtat("En cours...");
            InventaireDelegate.updateInventaire(cbx_inventaire_compter.getValue());
            this.syncInventaireHttp(cbx_inventaire_compter.getValue());
            MainUI.notify(null, "", "Inventaire physique marque comme en cours...", 4L, "info");
        }
        Inventaire inv = this.cbx_inventaire_compter.getValue();
        Mesure m = this.cbx_mesure_compter.getValue();
        Produit p = this.cbx_produit_compter.getValue();
        if (this.choosenCompter == null) {
            this.choosenCompter = new Compter();
            this.choosenCompter.setCoutAchat(Double.parseDouble(this.tf_coutachat_compter.getText()));
            this.choosenCompter.setDateCount(LocalDateTime.now());
            this.choosenCompter.setDateExpiration((LocalDate) this.dpk_date_exp_compter.getValue());
            this.choosenCompter.setInventaireId(inv);
            this.choosenCompter.setMesureId(m);
            this.choosenCompter.setQuantite(Double.parseDouble(this.tf_quantite_compter.getText()));
            if (dpk_date_exp_compter.getValue() != null) {
                choosenCompter.setDateExpiration(dpk_date_exp_compter.getValue());
            }
            // Calcul theorique tire de StockAgregate
            StockAgregate sa = delegates.RepportDelegate.findCurrentStock(p, cbx_inventaire_compter.getValue().getRegion(), LocalDate.now(), LocalDate.now());

            double theorik = (sa == null) ? 0.0 : (sa.getFinalQuantity() == null) ? 0.0 : sa.getFinalQuantity();
            double th = (theorik / m.getQuantContenu());
            double ecart = choosenCompter.getQuantite() - th;
            choosenCompter.setEcart(ecart);

            // Mettre a jour le total ecart en valeur
            double existingVal = cbx_inventaire_compter.getValue().getValeurTotalEcart() != null ? inv.getValeurTotalEcart() : 0.0;
            cbx_inventaire_compter.getValue().setValeurTotalEcart(existingVal + (ecart * choosenCompter.getCoutAchat()));
            InventaireDelegate.updateInventaire(cbx_inventaire_compter.getValue());
            saveInventaireByHttp(cbx_inventaire_compter.getValue());
            this.choosenCompter.setQuantiteTheorik(th);
            this.choosenCompter.setObservation(this.input_observ_comptage.getText());
            this.choosenCompter.setNumlot(this.tf_numlot_compter.getText());
            this.choosenCompter.setProductId(p);
            this.choosenCompter.setRegion(this.cbx_region_inv_compter.getValue());
            this.choosenCompter.setTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
            List<Compter> cpts = CompterDelegate.findCompterForProduct(p.getUid(), inv.getUid());

            Compter created = CompterDelegate.createCompter(choosenCompter);
            if (created != null) {
                ComptageItem crender = this.comptageRender(created);
                if (crender == null) {
                    return;
                }
                this.obl_comptages.add(0, crender);
            }
            for (int i = 0; i < cpts.size(); ++i) {
                Compter get = (Compter) cpts.get(i);
                if (i == cpts.size() - 1) {
                    get.setEcart(created.getEcart());
                } else {
                    get.setEcart(created.getEcart());
                }
                Compter updated = CompterDelegate.updateCompter((Compter) get);
                this.syncCompterHttp(updated);
            }
        } else {
            this.choosenCompter.setCoutAchat(Double.parseDouble(this.tf_coutachat_compter.getText()));
            this.choosenCompter.setDateCount(LocalDateTime.now());
            this.choosenCompter.setRegion(cbx_region_inv_compter.getValue());
            this.choosenCompter.setQuantite(Double.parseDouble(this.tf_quantite_compter.getText()));

            if (dpk_date_exp_compter.getValue() != null) {
                choosenCompter.setDateExpiration(dpk_date_exp_compter.getValue());
            }

            // Calcul theorique tire de StockAgregate
            StockAgregate sa = delegates.RepportDelegate.findCurrentStock(p,
                    choosenCompter.getRegion(),
                    LocalDate.now(),
                    LocalDate.now());

            double theorik = (sa == null) ? 0.0 : (sa.getFinalQuantity() == null) ? 0.0 : sa.getFinalQuantity();
            double th = (theorik / m.getQuantContenu());
            double ecart = choosenCompter.getQuantite() - th;
            choosenCompter.setEcart(ecart);

            // Mettre a jour le total ecart en valeur
            double existingVal = cbx_inventaire_compter.getValue().getValeurTotalEcart() != null ? inv.getValeurTotalEcart() : 0.0;
            cbx_inventaire_compter.getValue().setValeurTotalEcart(existingVal + (ecart * choosenCompter.getCoutAchat()));
            delegates.InventaireDelegate.updateInventaire(cbx_inventaire_compter.getValue());
            saveInventaireByHttp(cbx_inventaire_compter.getValue());
            this.choosenCompter.setInventaireId(inv);
            this.choosenCompter.setMesureId(m);
            this.choosenCompter.setNumlot(this.tf_numlot_compter.getText());
            this.choosenCompter.setProductId(this.cbx_produit_compter.getValue());
            this.choosenCompter.setTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
            Compter created = CompterDelegate.updateCompter(this.choosenCompter);
            if (created != null) {
                int index = obl_comptages.indexOf(choosenCompter);
                ComptageItem crender = this.comptageRender(created);
                if (crender == null) {
                    return;
                }
                this.obl_comptages.set(index, crender);
                MainUI.notify(null, "", "Inventaire enregistre avec succes", 4L, "info");
            }
            this.syncCompterHttp(created);
        }
//        this.tf_quantite_compter.clear();
//        this.tf_numlot_compter.clear();
//        double cau = choosenCompter.getCoutAchat() / m.getQuantContenu();
//        RecquisitionDelegate.rectifyStock(p, LocalDate.now(), LocalDate.now(), this.region, cau);
        String dev = this.pref.get("mainCur", "USD");
        this.txt_valeur_global_compter.setText("Valeur totale compt\u00e9e :" + this.obl_comptages.stream().mapToDouble(l -> {
            double val = BigDecimal.valueOf(l.getCoutTotal()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return val;
        }).sum() + " " + dev);
        this.txt_valeurtotal_ecart_compter.setText("Valeur totale \u00e9cart :" + this.obl_comptages.stream().mapToDouble(l -> {
            double val = BigDecimal.valueOf(l.getEcart() * l.getCoutAchat()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return val;
        }).sum() + " " + dev);
        choosenCompter = null;
    }

    public void refreshPoints(Event e) {
        Executors.newSingleThreadExecutor().submit(() -> {
            if (cbx_inventaire_compter.getValue() == null) {
                MainUI.notify(null, (String) "Erreur", (String) "Selectionner un inventaire puis reessayer!", (long) 4L, (String) "error");
                return;
            }
            this.obl_comptages.clear();
            List<Compter> lscs = CompterDelegate.findCompterBYInventaire(cbx_inventaire_compter.getValue().getUid());
            for (Compter lsc : lscs) {
                ComptageItem cit = this.comptageRender(lsc);
                this.obl_comptages.add(cit);
            }
        });
    }

    @FXML
    public void cloturerInventaire(Event e) {
        this.txt_cloture_message_compter.setVisible(true);
        this.progress_cloture_inv_compter.setVisible(true);
        Executors.newSingleThreadExecutor().submit(() -> {
            Inventaire inv = cbx_inventaire_compter.getValue();
            if (inv == null) {
                return;
            }
            if (!inv.getEtat().equalsIgnoreCase("Terminé")) {
                inv.setEtat("Terminé");
                inv.setDateFin(LocalDate.now());
                double valeurTotal = this.obl_comptages.stream().mapToDouble(l -> {
                    double val = BigDecimal.valueOf(l.getCoutTotal()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    return val;
                }).sum();
                double valeurTotalEcart = this.obl_comptages.stream().mapToDouble(l -> {
                    double val = BigDecimal.valueOf(l.getEcart() * l.getCoutAchat()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    return val;
                }).sum();
                inv.setValeurTotal(valeurTotal);
                inv.setValeurTotalEcart(valeurTotalEcart);
                InventaireDelegate.updateInventaire(inv);
                this.syncInventaireHttp(inv);
                CompterDelegate.closeInventoryByFixingNoCounts(inv);
            }
            Platform.runLater(() -> {
                this.txt_cloture_message_compter.setVisible(false);
                this.progress_cloture_inv_compter.setVisible(false);
                this.showLastInventory();
                MainUI.notify(null, "Succès", "Inventaire clôturé.", 2, "info");
            });
        });
    }

    @FXML
    public void createNewProductIfnotExist(Event e) {
        MainUI.floatDialog((String) "produit_item.fxml", (int) 600, (int) 790, (String) this.token, (Kazisafe) this.kazisafe, (Object[]) new Object[]{this.entreprise, null});
    }

    private void syncInventaireHttp(Inventaire inv) {
        Executors.newCachedThreadPool().submit(() -> this.saveInvHttp(inv));
    }

    private boolean saveInvHttp(Inventaire inv) {
        try {
            if (!NetLoockup.NETWORK_STATUS_ON) {
                return false;
            }
            Response syncinv = this.kazisafe.createInventaire(inv).execute();
            System.out.println("inventaire sync response " + String.valueOf(syncinv));
            if (syncinv.isSuccessful()) {
                System.out.println("Inventaire synced..OK");
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void syncCompterHttp(Compter compter) {
        Executors.newCachedThreadPool().submit(() -> {
            try {
                int retries = 0;
                while (retries < 5) {
                    Response syncinv = this.kazisafe.createCompter(compter).execute();
                    System.out.println("compter sync response " + String.valueOf(syncinv));
                    if (syncinv.isSuccessful()) {
                        System.out.println("Compter synced..OK");
                        break;
                    }
                    if (syncinv.code() == 417) {
                        String msg = syncinv.errorBody().string();
                        if (msg.contains("Inventaire")) {
                            this.saveInvHttp(InventaireDelegate.findInventaire((String) compter.getInventaireId().getUid()));
                        } else {
                            this.sendProduitIfNotExist(compter.getProductId(), MesureDelegate.findMesureByProduit((String) compter.getProductId().getUid()));
                        }
                    }
                    ++retries;
                    try {
                        TimeUnit.MILLISECONDS.sleep(200L * (long) Math.pow(2.0, retries));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void sendProduitIfNotExist(Produit produit, List<Mesure> mesures) {
        byte[] imageBytes = produit.getImage();
        if (imageBytes == null) {
            imageBytes = this.loadDefaultImage();
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        this.saveProductByHttpSafely(produit, base64Image, mesures);
    }

    private byte[] loadDefaultImage() {
        byte[] byArray;
        block8:
        {
            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            try {
                byArray = FileUtils.readAllBytes((InputStream) is);
                if (is == null) {
                    break block8;
                }
            } catch (Throwable throwable) {
                try {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException e) {
                    System.err.println("Erreur lors du chargement de l'image par d\u00e9faut" + e.getMessage());
                    return new byte[0];
                }
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return byArray;
    }

    @FXML
    private void deleteInventory(ActionEvent event) {
        if (!canCreateInventory.get()) {
            MainUI.notify(null, "Accès refusé", "Vous n'avez pas la permission de supprimer un inventaire.", 3, "error");
            return;
        }
        if (cbx_inventaire_compter.getValue() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer l'inventaire physique ?", new ButtonType[]{ButtonType.YES, ButtonType.CANCEL});
            alert.setTitle("Attention!");
            alert.setHeaderText(null);
            Optional showAndWait = alert.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                if (this.role.equals(Role.Trader.name())
                        | this.role.equals(Role.Manager_ALL_ACCESS.name())) {
                    List counts = CompterDelegate.findCompterBYInventaire((String) cbx_inventaire_compter.getValue().getUid());
                    if (counts.isEmpty()) {
                        InventaireDelegate.deleteInventaire((Inventaire) cbx_inventaire_compter.getValue());
                        MainUI.notify(null, (String) "Success", (String) "Suppression faite avec succ\u00e8s", (long) 4L, (String) "info");
                    } else {
                        MainUI.notify(null, (String) "Erreur", (String) "Cet inventaire n'est pas vide", (long) 4L, (String) "erreur");
                    }
                } else {
                    MainUI.notify(null, (String) "Impossible de supprimer", (String) "Vous n'avez pas les privileges necessaire pour effectuer la suppression", (long) 2L, (String) "warn");
                }
            }
        }
    }

    @FXML
    private void deleteCompter(ActionEvent event) {
        if (this.choosenCompter != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer ce comptage ?", new ButtonType[]{ButtonType.YES, ButtonType.CANCEL});
            alert.setTitle("Attention!");
            alert.setHeaderText(null);
            Optional showAndWait = alert.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                if (PermissionDelegate.hasPermission((PermitTo) PermitTo.CREATE_INVENTORY)) {
                    Inventaire parent = this.choosenCompter.getInventaireId();
                    CompterDelegate.deleteCompter((Compter) this.choosenCompter);
                    ComptageItem crender = this.comptageRender(this.choosenCompter);
                    if (crender == null) {
                        return;
                    }
                    this.obl_comptages.remove(crender);
                    double newvalue = this.obl_comptages.stream().mapToDouble(l -> l.getCoutTotal()).sum();
                    parent.setValeurTotal(newvalue);
                    InventaireDelegate.updateInventaire((Inventaire) parent);
                    this.syncInventaireHttp(parent);
                    MainUI.notify(null, (String) "Success", (String) "Suppression faite avec succ\u00e8s", (long) 4L, (String) "info");
                } else {
                    MainUI.notify(null, (String) "Impossible de supprimer", (String) "Vous n'avez pas les privileges necessaire pour effectuer la suppression", (long) 2L, (String) "warn");
                }
            }
        }
    }

    @FXML
    private void addRequestItem(Event et) {
        Livraison liv = null;
        if (tabpane_req_left.getSelectionModel().getSelectedIndex() == 0) {
            liv = list_livraison_req.getSelectionModel().getSelectedItem();
            if (liv == null) {
                MainUI.notify(null, "Erreur", "Veuillez selectionner une livraison", 3, "error");
                return;
            }
            String prov = cbx_provenance_req.getValue();
            MainUI.floatDialog(tools.Constants.RECQ_DLG, 716, 746, null, kazisafe, new Object[]{tools.Constants.ACTION_CREATE, null, entreprise, prov, liv});
        } else {
            MainUI.notify(null, "Erreur", "Veuillez selectionner une livraison dans l'onglet Livraisons", 3, "error");
        }
    }

    @FXML
    private void gotoSupplier(Event e) {
        MainUI.floatDialog(tools.Constants.FOURNISSEUR_DLG, 1090, 508, null, kazisafe, entreprise,
                choosenSupply);
    }

    @FXML
    private void createNewProductIfnotExist(ActionEvent et) {
        // Placeholder
    }

    @FXML
    private void recloturer(MouseEvent event) {
    }

    @FXML
    private void exportInventoryPhys(MouseEvent event) {
    }

    @FXML
    private void refreshComptage(MouseEvent event) {
    }

    @FXML
    private void exportxlsPerimees(MouseEvent event) {
    }

    @FXML
    private void saveRetourDepot(MouseEvent event) {
    }

    @FXML
    private void updateRetourDepot(MouseEvent event) {
    }

    @FXML
    private void deleteRetourDepot(MouseEvent event) {
    }

    @FXML
    private void showCompactCartSavePane(MouseEvent event) {
    }

    @FXML
    private void removeArticle(MouseEvent event) {
    }

    private ComptageItem comptageRender(Compter compter) {
        Produit pro = compter.getProductId();
        Mesure m = compter.getMesureId();
        Inventaire in = compter.getInventaireId();
        Produit p = ProduitDelegate.findProduit((String) pro.getUid());
        Inventaire inv = InventaireDelegate.findInventaire((String) in.getUid());
        Mesure mz = MesureDelegate.findMesure((String) m.getUid());
        ComptageItem co = new ComptageItem();
        co.setCodeInventaire(inv.getCodeInventaire());
        co.setCoutAchat(compter.getCoutAchat());
        co.setCoutTotal(BigDecimal.valueOf(compter.getCoutAchat() * compter.getQuantite()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        co.setDateCompter(compter.getDateCount());
        co.setDateExpiration(compter.getDateExpiration());
        co.setQuantiteTheorik(compter.getQuantiteTheorik().doubleValue());
        co.setEcart(compter.getEcart().doubleValue());
        co.setObservation(compter.getObservation());
        co.setDebutInventair(inv.getDateDebut());
        co.setEtatInventaire(inv.getEtat());
        co.setMesure(mz);
        co.setNomProduit(p.getNomProduit() + " " + p.getModele() + " " + p.getTaille() + " " + p.getMarque());
        co.setNumlot(compter.getNumlot());
        co.setProduit(p);
        co.setQuantite(compter.getQuantite());
        return co;
    }

    private List<ComptageItem> comptageRender(Collection<Compter> cs, Inventaire inv) {
        if (inv == null) {
            MainUI.notify(null, (String) "Error", (String) "Erreur : veuillez reselectionner l'inventaire puis reesayer", (long) 4L, (String) "error");
            return null;
        }
        ArrayList<ComptageItem> result = new ArrayList<>();
        for (Compter c : cs) {
            Produit p = c.getProductId();
            ComptageItem co = new ComptageItem();
            co.setCodeInventaire(inv.getCodeInventaire());
            co.setCoutAchat(c.getCoutAchat());
            co.setCoutTotal(BigDecimal.valueOf(c.getCoutAchat() * c.getQuantite()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            co.setDateCompter(c.getDateCount());
            co.setDateExpiration(c.getDateExpiration());
            co.setDebutInventair(inv.getDateDebut());
            co.setEtatInventaire(inv.getEtat());
            co.setQuantiteTheorik(c.getQuantiteTheorik());
            co.setEcart(c.getEcart().doubleValue());
            co.setObservation(c.getObservation());
            co.setMesure(c.getMesureId());
            co.setNomProduit(p.getNomProduit() + " " + p.getModele() + " " + p.getTaille() + " " + p.getMarque());
            co.setNumlot(c.getNumlot());
            co.setProduit(p);
            co.setQuantite(c.getQuantite());
            result.add(co);
        }
        return result;
    }

    private void showLastInventory() {
        String dev = this.pref.get("mainCur", "USD");
        Inventaire in = InventaireDelegate.findLastInventory();
        List co = CompterDelegate.findCompterBYInventaire(in.getUid());
        List<ComptageItem> crender = this.comptageRender(co, in);
        if (crender == null) {
            return;
        }
        this.obl_comptages.setAll(crender);
        this.txt_valeur_global_compter.setText("Valeur totale compt\u00e9e: " + Util.toPlain(this.obl_comptages.stream().mapToDouble(l -> {
            double val = BigDecimal.valueOf(l.getCoutTotal()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return val;
        }).sum()) + " " + dev);
        this.txt_valeurtotal_ecart_compter.setText("Val.totale ecart: " + Util.toPlain(this.obl_comptages.stream().mapToDouble(e -> {
            double val = BigDecimal.valueOf(e.getEcart() * e.getCoutAchat()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return val;
        }).sum()) + " " + dev);
    }

    private void saveProductByHttpSafely(Produit produit, String base64Image, List<Mesure> mesures) {
        int retries = 0;
        while (retries < 5) {
            try {
                Response<Produit> saveR = this.saveProduitByHttp(produit, base64Image, mesures);
                if (saveR.isSuccessful()) {
                    break;
                }
                Category c = CategoryDelegate.findCategory((String) produit.getCategoryId().getUid());
                if (this.saveCategoryByHttp(c)) {
                    ++retries;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(200L * (long) Math.pow(2.0, retries));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (IOException ex) {
                System.out.println("Instant save product error " + ex.getMessage());
                Logger.getLogger(PaymentController.class.getName()).log(Level.INFO, null, ex);
                break;
            }
        }
    }

    private Response<Produit> saveProduitByHttp(Produit produit, String base64Image, List<Mesure> mesures) throws IOException {
        ProduitHelper produitHelper = this.createProduitHelper(produit, base64Image, mesures);
        Response response = this.kazisafe.saveLite(produitHelper).execute();
        if (response.isSuccessful()) {
            System.out.println("Save synchrone Produit " + response.code());
        } else {
            System.err.println("Erreur lors de l'enregistrement du produit : " + response.code());
        }
        return response;
    }

    private ProduitHelper createProduitHelper(Produit produit, String base64Image, List<Mesure> mesures) {
        if (base64Image == null) {
            InputStream inputStream = this.getClass().getResourceAsStream("/icons/gallery.png");
        }
        ProduitHelper produitHelper = new ProduitHelper();
        produitHelper.setUid(produit.getUid());
        produitHelper.setCategoryId(produit.getCategoryId().getUid());
        produitHelper.setCodebar(produit.getCodebar());
        produitHelper.setCouleur(produit.getCouleur());
        produitHelper.setMarque(produit.getMarque());
        produitHelper.setModele(produit.getModele());
        produitHelper.setNomProduit(produit.getNomProduit());
        produitHelper.setTaille(produit.getTaille());
        produitHelper.setMethodeInventaire(produit.getMethodeInventaire());
        produitHelper.setMesureList(mesures);
        return produitHelper;
    }

    private boolean saveCategoryByHttp(Category c) throws IOException {
        Response exec = this.kazisafe.saveCategory(c).execute();
        System.out.println("Instant save Category response = " + exec.code());
        return exec.isSuccessful();
    }
}
