/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import data.Category;
import delegates.DestockerDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import data.core.KazisafeServiceFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.collections.transformation.FilteredList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import data.Destocker;
import data.Entreprise;
import data.Fournisseur;
import data.LigneVente;
import services.utils.RegionRegistry;
import data.Mesure;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.StockDepotAgregate;
import data.Livraison;
import data.PrixDeVente;
import data.Refresher;
import data.Vente;
import data.PermitTo;
import services.StockDepotAgregateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.InventoryItem;
import tools.JsonUtil;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import tools.NotificationHandler;
import tools.Constants;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.CategoryDelegate;
import delegates.PermissionDelegate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import tools.DataId;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class GoodstorageController implements Initializable {

    private static GoodstorageController instance;

    public static GoodstorageController getInstance() {
        if (instance == null) {
            instance = new GoodstorageController();
        }
        return instance;
    }

    @FXML
    Tab tab_invent;
    @FXML
    Tab tab_destock;
    @FXML
    Tab tab_livraison;
    @FXML
    Tab tab_livraiz;
    @FXML
    Tab tab_fournisseur;
    @FXML
    TabPane tabpane_livrz;
    @FXML
    TabPane tabs_gs;
    @FXML
    AnchorPane pane_wait_import;
    @FXML
    ImageView newdelivery;
    @FXML
    ImageView newitems;
    @FXML
    ImageView newsuplier;

    @FXML
    private Pagination pagination;
    @FXML
    private TableView<Stocker> table_stockage;
    @FXML
    private TableColumn<Stocker, String> col_date_Stocker;
    @FXML
    private TableColumn<Stocker, String> col_ref_Stocker;
    @FXML
    private TableColumn<Stocker, String> col_nom_produit_stocker;
    @FXML
    private TableColumn<Stocker, String> col_numlot_stocker;
    @FXML
    private TableColumn<Stocker, String> col_quant_stocker;
    @FXML
    private TableColumn<Stocker, Number> col_cout_unit_stocker;
    @FXML
    private TableColumn<Stocker, Number> col_cout_total_stocker;
    @FXML
    private TableColumn<Stocker, String> col_date_expir_stocker;
    @FXML
    private TableColumn<Stocker, String> col_region_stocker;
    @FXML
    private TableColumn<Stocker, String> col_observ_stocker;
    @FXML
    private ComboBox<Integer> rowPP;
    @FXML
    private Label count, valSocks;
    @FXML
    private DatePicker dpk_debut_stk;
    @FXML
    private DatePicker dpk_fin_stk;
    @FXML
    private Pagination pagination1;
    @FXML
    private TableView<Destocker> table1;
    @FXML
    private TableColumn<Destocker, String> col_date_destk;
    @FXML
    private TableColumn<Destocker, String> col_ref_destk;
    @FXML
    private TableColumn<Destocker, String> col_codebar_destk;
    @FXML
    private TableColumn<Destocker, String> col_nom_prod8_dstk;
    @FXML
    private TableColumn<Destocker, String> col_quant_dstk;
    @FXML
    private TableColumn<Destocker, Number> col_cout_ach_unit_dstk;
    @FXML
    private TableColumn<Destocker, String> col_libelle_dstk;
    @FXML
    private TableColumn<Destocker, String> col_destination_dstk;
    @FXML
    private TableColumn<Destocker, String> col_numlot_dstk;
    @FXML
    private TableColumn<Destocker, Number> col_coutotal_dstk;
    @FXML
    private TableColumn<Destocker, String> col_region_dstk;
    @FXML
    private ComboBox<Integer> rowPP1;
    @FXML
    private Label count1, global_achat;
    @FXML
    private Pagination pagination11;
    @FXML
    private TableView<InventoryItem> table11;
    @FXML
    private TableColumn<InventoryItem, String> col_codebar_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_nom_produit_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_stock_initial_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_quant_in_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_quant_out_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_remain_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_cout_achat_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_valeur_rem_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_alerte_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_numlot_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_date_expir_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_localisation_inv;
    @FXML
    private TableColumn<InventoryItem, String> col_region_inv;
    @FXML
    private ComboBox<Integer> rowPP11;
    @FXML
    private ComboBox<String> cbx_regions;
    @FXML
    private Label count11;
    @FXML
    CheckBox chk_box_inv_mag_depot;
    @FXML
    Label libelle_inv;
    @FXML
    private TextField input_txt_criteres_mens;
    @FXML
    private ImageView btn_refresh, btn_refresh_d, btn_refresh_inv;
    @FXML
    private ImageView btn_delete, btn_delete_d;
    @FXML
    private ImageView btn_update, btn_update_d, btn_export_i;
    @FXML
    private ComboBox<String> cbx_choose_critere_de_selection;
    @FXML
    private ComboBox<String> cbx_filter_color_inv_depot;
    @FXML
    private CheckBox chbx_filter_stock_fini_depot;
    @FXML
    private CheckBox chbx_filter_stock_non_fini_depot;
    @FXML
    private Button btn_declasser_inv_depot;
    @FXML
    CheckBox chbx_filter;
    @FXML
    TextField search_livraiz;
    @FXML
    TextField search_supplier;
    @FXML
    ListView<Livraison> list_livraison;
    @FXML
    ListView<Fournisseur> list_supplier;
    @FXML
    Label count_livraizon;
    @FXML
    Label count_fournisseur;
    @FXML
    Label curent_path;

    ObservableList<Livraison> listlivr;
    ObservableList<Fournisseur> listfourn;
    ObservableList<Stocker> list_stockers;
    ObservableList<Destocker> lisdestocker;
    ObservableList<InventoryItem> lisinvent;
    private FilteredList<InventoryItem> filteredDepotInventory;
    ObservableList<String> regions;
    StockDepotAgregateService stockDepotService;
    List<Produit> products;
    List<Stocker> stox;
    List<Destocker> destox;
    Entreprise entreprise;
    private int starting = 0;
    private int rowsDataCount = 20;
    private int rowsDataCount1 = 20;
    private int rowsDataCount11 = 20;
    double valStock = 0;
    String choosen_criteria = "";
    String action;
    Stocker chstocker;
    Destocker chdestocker;
    Livraison livraison;
    Fournisseur choosenSupply;
    Preferences pref;
    Kazisafe kazisafe;
    String region, role, token, entr;
    ResourceBundle bundle;
    private static final DateTimeFormatter INVENTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GoodstorageController() {
        listfourn = FXCollections.observableArrayList();
        listlivr = FXCollections.observableArrayList();
        lisdestocker = FXCollections.observableArrayList();
        instance = this;
    }

    public void populateDelivery(String action, Livraison livr) {
        if (listlivr.contains(livr)) {
            return;
        }
        if (action.equals(tools.Constants.ACTION_CREATE)) {
            listlivr.add(livr);
        } else if (action.equals(tools.Constants.ACTION_UPDATE)) {
            listlivr.set(listlivr.indexOf(livr), livr);
        } else if (action.equals(tools.Constants.ACTION_DELETE)) {
            listlivr.remove(livr);
        }
        Platform.runLater(() -> {
            if (count_livraizon != null) {
                count_livraizon.setText(String.format(bundle.getString("xitems"), listlivr.size()));
            }
        });
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
            listlivr.add(l);
        } else {
            l = LivraisonDelegate.updateLivraison(liv);
            int index = listlivr.indexOf(l);
            if (index != -1) {
                listlivr.set(index, liv);
            } else {
                listlivr.add(l);
            }
        }

    }

    public void addStocker(Stocker liv) {
        Stocker l = StockerDelegate.findStocker(liv.getUid());// strongDb.findByUid(Stocker.class, liv.getUid());
        if (l == null) {
            Livraison z = LivraisonDelegate.findLivraison(liv.getLivraisId().getUid());// strongDb.findByUid(Livraison.class,
                                                                                       // liv.getLivraisId().getUid());
            if (z != null) {
                StockerDelegate.saveStocker(liv);// strongDb.insertOnly(liv);
            }
        } else {
            Livraison z = LivraisonDelegate.findLivraison(liv.getLivraisId().getUid());// strongDb.findByUid(Livraison.class,
                                                                                       // l.getLivraisId().getUid());
            if (z != null) {
                StockerDelegate.updateStocker(liv);// strongDb.updateOnly(liv);
            }
        }
    }

    public void populateStocker(String action, Stocker s) {
        if (list_stockers != null) {
            if (action.equals(tools.Constants.ACTION_CREATE)) {

                list_stockers.add(s);
            } else if (action.equals(tools.Constants.ACTION_UPDATE)) {
                if (list_stockers.size() <= 1) {
                    list_stockers.clear();
                    list_stockers.add(s);
                } else {
                    Optional<Stocker> match = list_stockers.stream().filter(p -> p.getUid().equals(s.getUid()))
                            .findFirst();
                    if (match.isPresent()) {
                        list_stockers.set(list_stockers.indexOf(s), s);
                    }
                }

            } else if (action.equals(tools.Constants.ACTION_DELETE)) {
                list_stockers.remove(s);
            }
            Platform.runLater(() -> {
                count.setText(String.format(bundle.getString("xitems"), table_stockage.getItems().size()));
            });
        }
    }

    public void addDestockerx(Destocker liv) {
        if (liv == null) {
            return;
        }
        Destocker l = DestockerDelegate.findDestocker(liv.getUid());
        if (l == null) {
            l = DestockerDelegate.saveDestocker(liv);
        } else {
            l = DestockerDelegate.updateDestocker(liv);
        }
        lisdestocker.add(l);
    }

    private void initPref() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        if (role == null) {
            role = pref.get("role", "Trader");
        }
        if (region == null) {
            region = pref.get("region", "Goma");
        }
    }

    public void populate() {
        list_stockers.clear();
        listfourn.clear();
        listlivr.clear();
        initPref();
        DestockerDelegate.removeOrphans();
        if (role.equals(Role.Trader.name())
                || role.equals(Role.Magazinner_ALL_ACCESS.name())
                || role.equals(Role.Manager_ALL_ACCESS.name())) {
            long sized = ProduitDelegate.getCount();
            int offsetd = 0;
            Long limis = Math.min(offsetd + rowsDataCount1, sized);
            destox = DestockerDelegate.findDescSortedByDate(offsetd, limis.intValue());
            if(destox==null)return;
            List<Destocker> filteredDestox = new ArrayList<>();
            for(Destocker d : destox) {
                if(d.getProductId() != null && ProduitDelegate.findProduit(d.getProductId().getUid()) != null) {
                    filteredDestox.add(d);
                }
            }
            destox = filteredDestox;
            lisdestocker.addAll(destox);
            table1.setItems(lisdestocker);

            long size = StockerDelegate.getCount();
            int offset = 0;
            Long limix = Math.min(offset + rowsDataCount, size);
            stox = StockerDelegate.findStockers();
            list_stockers.addAll(stox.subList(offset, limix.intValue()));
            table_stockage.setItems(list_stockers);
            List<Livraison> foundl = LivraisonDelegate.findDescSortedByDate();
            listlivr.addAll(foundl);
            list_livraison.setItems(listlivr);
            count_livraizon.setText(String.format(bundle.getString("xitems"), listlivr.size()));
            List<Fournisseur> lfs = FournisseurDelegate.findFournisseurs();
            listfourn.addAll(lfs);
            list_supplier.setItems(listfourn);
            count_fournisseur.setText(String.format(bundle.getString("xitems"), listfourn.size()));
        } else {
            long sized = DestockerDelegate.getCount();
            int offsetd = 0;
            Long limis = Math.min(offsetd + rowsDataCount1, sized);
            destox = DestockerDelegate.findDescSortedByDate(region, offsetd, limis.intValue());
            if(destox==null)return;
            List<Destocker> filteredDestox = new ArrayList<>();
            for(Destocker d : destox) {
                if(d.getProductId() != null && ProduitDelegate.findProduit(d.getProductId().getUid()) != null) {
                    filteredDestox.add(d);
                }
            }
            destox = filteredDestox;
            lisdestocker = FXCollections.observableArrayList(destox);
            table1.setItems(lisdestocker);
            long size = LivraisonDelegate.getCount();
            int offset = 0;
            Long limix = Math.min(offset + rowsDataCount, size);
            stox = StockerDelegate.findStockers(region);
            int lim = limix.intValue();
            int vlim = Math.min(stox.size(), lim);
            list_stockers.addAll(stox.subList(offset, vlim));
            table_stockage.setItems(list_stockers);
            List<Livraison> foundl = LivraisonDelegate.findDescSortedByDate(region);
            listlivr.addAll(foundl);
            list_livraison.setItems(listlivr);
            count_livraizon.setText(String.format(bundle.getString("xitems"), listlivr.size()));
            List<Fournisseur> lfs = FournisseurDelegate.findFournisseurs();
            listfourn.addAll(lfs);
            list_supplier.setItems(listfourn);
            count_fournisseur.setText(String.format(bundle.getString("xitems"), listfourn.size()));
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                count.setText(table_stockage.getItems().size() + " éléments");
            }
        });
    }

    public void setDatabase(String action) {
        this.action = action;
        kazisafe = KazisafeServiceFactory.createService(token);
        // Initialize StockDepotAgregateService for aggregate stock management
        stockDepotService = new StockDepotAgregateService();
        regions = FXCollections.observableArrayList();
        cbx_regions.setItems(regions);
        cbx_regions.setVisible(false);
        stox = new ArrayList<>();
        destox = new ArrayList<>();
        products = ProduitDelegate.findProduits();
        initPref();
        if (role.equals(Role.Trader.name()) || role.toUpperCase().contains(Role.ALL_ACCESS.name().toUpperCase())) {
            cbx_regions.setVisible(true);
        }
        populate();
        list_livraison.setCellFactory((ListView<Livraison> param) -> new ListCell<Livraison>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Livraison item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getNumPiece() + ", " + item.getReference() + " " + item.getTopay() + " "
                                    + item.getDateLivr().toString());
                            imageView.setFitHeight(28);
                            imageView.setFitWidth(28);
                            imageView.setPreserveRatio(true);
                            imageView.setImage(
                                    new Image(AgentController.class.getResourceAsStream("/icons/add-product(1).png")));
                            setGraphic(imageView);
                        }
                    });

                }
            }

        });

        list_supplier.setCellFactory((ListView<Fournisseur> param) -> new ListCell<Fournisseur>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getNomFourn() + ", " + item.getPhone() + " " + item.getAdresse());
                            imageView.setFitHeight(28);
                            imageView.setFitWidth(28);
                            imageView.setPreserveRatio(true);
                            imageView.setImage(
                                    new Image(AgentController.class.getResourceAsStream("/icons/add-user32.png")));
                            setGraphic(imageView);
                        }
                    });

                }
            }

        });

        search_livraiz.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.isEmpty()) {
                    list_livraison.setItems(listlivr);
                    count_livraizon.setText(String.format(bundle.getString("xitems"), listlivr.size()));
                } else {
                    ObservableList<Livraison> rst = FXCollections.observableArrayList();
                    for (Livraison livr : listlivr) {
                        String term = livr.getNumPiece() + " " + livr.getFournId().getNomFourn() + " "
                                + livr.getLibelle()
                                + " " + livr.getDateLivr().toString();
                        if (term.toUpperCase().contains(newValue.toUpperCase())) {
                            rst.add(livr);
                        }
                    }
                    list_livraison.setItems(rst);
                    count_livraizon.setText(String.format(bundle.getString("xitems"), rst.size()));
                }
            }
        });
        search_supplier.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.isEmpty()) {
                    list_supplier.setItems(listfourn);
                    count_fournisseur.setText(String.format(bundle.getString("xitems"), listfourn.size()));
                } else {
                    ObservableList<Fournisseur> rst = FXCollections.observableArrayList();
                    for (Fournisseur livr : listfourn) {
                        String term = livr.getNomFourn() + " " + livr.getIdentification() + " " + livr.getAdresse()
                                + " " + livr.getPhone();
                        if (term.toUpperCase().contains(newValue.toUpperCase())) {
                            rst.add(livr);
                        }
                    }
                    list_supplier.setItems(rst);
                    count_fournisseur.setText(String.format(bundle.getString("xitems"), rst.size()));
                }
            }
        });
        chbx_filter.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    populate();
                }
            }
        });

        lisinvent = FXCollections.observableArrayList();

        loadInv();
        tab_livraison.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                }

            }
        });
        tab_destock.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    table1.setItems(lisdestocker);
                }

            }
        });
        tab_invent.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    loadInv();
                }
            }
        });

        rowPP.setItems(FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000)));
        rowPP1.setItems(FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000)));
        rowPP11.setItems(FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000)));
        rowPP.getSelectionModel().selectFirst();
        rowPP1.getSelectionModel().selectFirst();
        rowPP11.getSelectionModel().selectFirst();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                count.setText(String.format(bundle.getString("xitems"), table_stockage.getItems().size()));
                count1.setText(String.format(bundle.getString("xitems"), table1.getItems().size()));
                count11.setText(String.format(bundle.getString("xitems"), table11.getItems().size()));
            }
        });

        RegionRegistry.loadAndSync(pref, kazisafe, regions);
        RegionRegistry.selectSavedRegion(pref, cbx_regions);
        cbx_regions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {

                    try {
                        loadInventaireDepot(products, newValue);
                    } catch (Exception ex) {
                        Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    public ObservableList<String> accessregion() {
        return regions;
    }

    public void gotoSuplier(Event e) {
        MainUI.floatDialog(tools.Constants.FOURNISSEUR_DLG, 1090, 508, null, kazisafe, entreprise, null);
    }

    public void gotoDelivery(Event e) {
        MainUI.floatDialog(tools.Constants.DELIVERY_DLG, 600, 468, null, kazisafe, entreprise, null);
    }

    private void loadInv() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
                    try {
                        loadInventaireDepot(Util.filterNoNullMesure(products), region);
                    } catch (Exception ex) {
                        Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        String selectedRegion = cbx_regions == null ? null : cbx_regions.getValue();
                        loadInventaireDepot(Util.filterNoNullMesure(products),
                                selectedRegion == null || selectedRegion.isBlank() ? region : selectedRegion);
                    } catch (Exception ex) {
                        Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public void populateSupplier(String action, Fournisseur ff) {
        try {
            if (listfourn.contains(ff)) {
                return;
            }
            if (action.equals(tools.Constants.ACTION_CREATE)) {
                listfourn.add(ff);
            } else if (action.equals(tools.Constants.ACTION_UPDATE)) {
                listfourn.set(listfourn.indexOf(ff), ff);
            } else if (action.equals(tools.Constants.ACTION_DELETE)) {
                listfourn.remove(ff);
            }
            Platform.runLater(() -> {
                if (count_fournisseur != null) {
                    count_fournisseur.setText(String.format(bundle.getString("xitems"), listfourn.size()));
                }
            });
        } catch (java.lang.NullPointerException e) {

        }
    }

    public void setSupplier(Fournisseur old, Fournisseur newf) {
        listfourn.set(listfourn.indexOf(old), newf);
    }

    private void configTables() {
        configLivCbx();
        col_date_Stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            Stocker liv = param.getValue();
            String dt = null;
            if (liv != null) {
                LocalDateTime dat = liv.getDateStocker();
                if (dat != null) {
                    dt = dat.toString();
                }
            }
            return new SimpleStringProperty(dt == null ? "-" : dt);
        });
        col_ref_Stocker
                .setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getLivraisId().getNumPiece()));
        col_nom_produit_stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            Stocker l = param.getValue();
            Produit p = ProduitDelegate.findProduit(l.getProductId().getUid());
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " "
                    + (p.getTaille() == null ? "" : p.getTaille()));
        });
        col_numlot_stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            // Fournisseur fss = strongDb.findByUid(Fournisseur.class,
            // param.getValue().getFournId().getUid());
            Stocker l = param.getValue();
            return new SimpleStringProperty(l.getNumlot());
        });
        col_quant_stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            // Fournisseur fss = strongDb.findByUid(Fournisseur.class,
            // param.getValue().getFournId().getUid());
            Stocker l = param.getValue();
            Mesure fss = l.getMesureId();
            double qt = l.getQuantite();
            return new SimpleStringProperty(qt + " " + fss.getDescription());
        });

        col_cout_unit_stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, Number> param) -> {
            Double t = param.getValue().getCoutAchat();
            return new SimpleDoubleProperty(t == null ? 0 : t);
        });
        col_cout_total_stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, Number> param) -> {
            Stocker s = param.getValue();
            return new SimpleDoubleProperty(s.getCoutAchat() * s.getQuantite());
        });

        col_date_expir_stocker.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            Stocker lst = param.getValue();// strongDb.findStockerByLivr(param.getValue().getUid());
            String dat;
            if (lst.getDateExpir() == null) {
                dat = bundle.getString("noperish");
            } else {
                dat = lst.getDateExpir().toString();
            }
            return new SimpleStringProperty(dat);
        });
        col_region_stocker
                .setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getRegion()));
        col_observ_stocker
                .setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getObservation()));
        // destockage
        col_date_destk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getDateDestockage().toString()));
        col_ref_destk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getReference()));
        col_codebar_destk.setCellValueFactory((TableColumn.CellDataFeatures<Destocker, String> param) -> {
            Produit p = Util.findProduit(products, param.getValue().getProductId().getUid());
            if (p == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(p == null ? "" : p.getCodebar());
        });
        col_nom_prod8_dstk.setCellValueFactory((TableColumn.CellDataFeatures<Destocker, String> param) -> {
            Produit p = Util.findProduit(products, param.getValue().getProductId().getUid());
            if (p == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(p == null ? ""
                    : (p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + p.getTaille()));
        });
        col_quant_dstk.setCellValueFactory((TableColumn.CellDataFeatures<Destocker, String> param) -> {
            Mesure mx = param.getValue().getMesureId();
            // Mesure m =
            // MesureDelegate.findMesure(mx.getUid());//strongDb.findByUid(Mesure.class,
            // mx.getUid());//Util.findMesure(strongDb.findAll(), mx);
            Destocker ds = param.getValue();
            return new SimpleStringProperty(
                    (ds != null ? ds.getQuantite() : 0) + " " + (mx == null ? "" : mx.getDescription()));
        });
        col_cout_ach_unit_dstk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, Number> param) -> new SimpleDoubleProperty(
                        param.getValue().getCoutAchat()));
        col_libelle_dstk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getLibelle()));
        col_destination_dstk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getDestination()));
        col_numlot_dstk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getNumlot()));
        col_coutotal_dstk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, Number> param) -> new SimpleDoubleProperty(
                        BigDecimal.valueOf(param.getValue().getCoutAchat() * param.getValue().getQuantite())
                                .setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        col_region_dstk.setCellValueFactory(
                (TableColumn.CellDataFeatures<Destocker, String> param) -> new SimpleStringProperty(
                        param.getValue().getRegion()));
        // inventaire
        col_codebar_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            Produit p = param.getValue().getProduit() == null ? null
                    : Util.findProduit(products, param.getValue().getProduit().getUid());
            if (p == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(p == null ? "Not synced" : p.getCodebar());
        });
        col_nom_produit_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            Produit p = param.getValue().getProduit() == null ? null
                    : Util.findProduit(products, param.getValue().getProduit().getUid());
            if (p == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(p == null ? "Not synced"
                    : (p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " "
                    + (p.getTaille() == null ? "" : p.getTaille()) + " "
                    + (p.getCouleur() == null ? "" : p.getCouleur())).trim());
        });
        col_stock_initial_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(formatInventoryQuantity(param.getValue().getStockInitial(), param.getValue()));
        });
        col_quant_in_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getQuantEntree());
        });
        col_quant_out_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getQuantSortie());
        });
        col_remain_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getQuantRest());
        });
        col_cout_achat_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(String.format("%.2f USD", param.getValue().getCoutAchat()));
        });
        col_valeur_rem_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getValeurStock());
        });
        col_alerte_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getStockAlerte());
        });
        col_numlot_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getNumlot() == null ? "" : param.getValue().getNumlot());
        });
        col_date_expir_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            String d = param.getValue().getDateExpir();
            if (d == null && param.getValue().getLastStocker() != null) {
                LocalDate ld = param.getValue().getLastStocker().getDateExpir();
                d = ld == null ? "-" : ld.format(INVENTORY_DATE_FORMAT);
            }
            return new SimpleStringProperty(d == null ? "-" : d);
        });
        col_localisation_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            String loc = param.getValue().getLocalisation();
            if (loc == null && param.getValue().getLastStocker() != null) {
                loc = param.getValue().getLastStocker().getLocalisation();
            }
            return new SimpleStringProperty(loc == null ? "" : loc);
        });
        col_region_inv.setCellValueFactory((TableColumn.CellDataFeatures<InventoryItem, String> param) -> {
            return new SimpleStringProperty(param.getValue().getRegion() == null ? "" : param.getValue().getRegion());
        });
        table11.setRowFactory(tv -> new TableRow<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty || item.getExpiryDate() == null) {
                    setStyle("");
                    return;
                }
                int exp = isStockExpired(item.getExpiryDate());
                switch (exp) {
                    case -1 ->
                        setStyle("-fx-background-color: #f58282;");
                    case 1 ->
                        setStyle("-fx-background-color: #c46506;");
                    case 3 ->
                        setStyle("-fx-background-color: #db8109;");
                    case 6 ->
                        setStyle("-fx-background-color: #f7fa61;");
                    case 12 ->
                        setStyle("-fx-background-color: #c5e6b3;");
                    default ->
                        setStyle("");
                }
            }
        });
        
        list_livraison.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Livraison>() {
            @Override
            public void changed(ObservableValue<? extends Livraison> observable, Livraison oldValue,
                    Livraison newValue) {
                livraison = newValue;
                if (livraison != null) {
                    ObservableList<Stocker> stockrs = FXCollections
                            .observableArrayList(StockerDelegate.findStockerByLivraison(livraison.getUid()));
                    curent_path.setText(livraison.getFournId().getNomFourn() + " : " + livraison.getNumPiece() + ", "
                            + livraison.getDateLivr().toString() + " (" + stockrs.size() + " articles)");
                    table_stockage.setItems(stockrs);
                    count.setText(String.format(bundle.getString("xitems"), stockrs.size()));
                    global_achat.setText("Total : USD " + livraison.getPayed());
                    chbx_filter.setSelected(true);
                }
            }
        });
       
        table1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Destocker>() {
            @Override
            public void changed(ObservableValue<? extends Destocker> observable, Destocker oldValue,
                    Destocker newValue) {
                chdestocker = newValue;
            }
        });
        table_stockage.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Stocker>() {
            @Override
            public void changed(ObservableValue<? extends Stocker> observable, Stocker oldValue, Stocker newValue) {
                if (newValue != null) {
                    chstocker = newValue;
                }
            }
        });

        table11.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<InventoryItem>() {
            @Override
            public void changed(ObservableValue<? extends InventoryItem> observable, InventoryItem oldValue,
                    InventoryItem newValue) {
                if (newValue != null) {
                    chdestocker = newValue.getLastDestocker();
                    chstocker = newValue.getLastStocker();
                }
            }
        });
    }

    /**
     * Load inventory data from StockDepotAgregate table
     * This is the new method that uses StockDepotAgregate as the source of truth
     */
    public void loadInventoryFromStockDepotAgregate(List<Produit> lp, String regionFilter) {
        String effectiveRegion = regionFilter != null ? regionFilter : (region != null ? region : "DEFAULT");

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            List<InventoryItem> loadedItems = new ArrayList<>();
            double totalValue = 0d;
            for (Produit p : lp) {
                List<StockDepotAgregate> lotAggregates = StockerDelegate.findLatestLotDepotStockAggregates(p.getUid(), effectiveRegion);
                if (lotAggregates == null || lotAggregates.isEmpty()) {
                    continue;
                }

                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());
                if (mesures == null || mesures.isEmpty()) {
                    continue;
                }
                Mesure displayMesure = mesures.get(0);
                double displayContent = displayMesure.getQuantContenu() != null && displayMesure.getQuantContenu() > 0
                        ? displayMesure.getQuantContenu() : 1d;

                for (StockDepotAgregate aggregate : lotAggregates) {
                    InventoryItem item = buildInventoryItemFromAggregate(p, aggregate, effectiveRegion, displayMesure, displayContent);
                    if (item == null) {
                        continue;
                    }
                    loadedItems.add(item);
                    totalValue += item.getValeurStockValue();
                }
            }

            final double totalValueFinal = totalValue;
            Platform.runLater(() -> {
                lisinvent.setAll(loadedItems);
                valStock = totalValueFinal;
                applyDepotInventoryFilters();
            });
        });
        ex.shutdown();
    }


    public void loadInventaireDepot(List<Produit> lp, String region) {
        // Call the new method that uses StockDepotAgregate as source
        loadInventoryFromStockDepotAgregate(lp, region);
    }

    private InventoryItem buildInventoryItemFromAggregate(Produit produit, StockDepotAgregate aggregate, String effectiveRegion,
            Mesure displayMesure, double displayContent) {
        if (aggregate == null || aggregate.getNumlot() == null || aggregate.getNumlot().isBlank()
                || aggregate.getQuantite() <= 0) {
            return null;
        }

        List<Stocker> filteredStockers = new ArrayList<>();
        List<Stocker> lotStockers = StockerDelegate.findStockerByProduitLot(produit.getUid(), aggregate.getNumlot());
        if (lotStockers != null) {
            for (Stocker stocker : lotStockers) {
                if (stocker != null && effectiveRegion.equals(stocker.getRegion())) {
                    filteredStockers.add(stocker);
                }
            }
        }

        List<Destocker> filteredDestockers = new ArrayList<>();
        List<Destocker> lotDestockers = DestockerDelegate.findByProduitLot(produit.getUid(), aggregate.getNumlot());
        if (lotDestockers != null) {
            for (Destocker destocker : lotDestockers) {
                if (destocker != null && effectiveRegion.equals(destocker.getRegion())) {
                    filteredDestockers.add(destocker);
                }
            }
        }

        Stocker earliestStocker = null;
        Stocker latestStocker = null;
        double totalEntriesPc = 0d;
        for (Stocker stocker : filteredStockers) {
            totalEntriesPc += toPieces(stocker);
            if (earliestStocker == null || (stocker.getDateStocker() != null && earliestStocker.getDateStocker() != null
                    && stocker.getDateStocker().isBefore(earliestStocker.getDateStocker()))) {
                earliestStocker = stocker;
            }
            if (latestStocker == null || (stocker.getDateStocker() != null && latestStocker.getDateStocker() != null
                    && stocker.getDateStocker().isAfter(latestStocker.getDateStocker()))) {
                latestStocker = stocker;
            }
        }

        Destocker latestDestocker = null;
        double totalSortiesPc = 0d;
        for (Destocker destocker : filteredDestockers) {
            totalSortiesPc += toPieces(destocker);
            if (latestDestocker == null || (destocker.getDateDestockage() != null && latestDestocker.getDateDestockage() != null
                    && destocker.getDateDestockage().isAfter(latestDestocker.getDateDestockage()))) {
                latestDestocker = destocker;
            }
        }

        double stockInitialPc = earliestStocker == null ? 0d : toPieces(earliestStocker);
        double entreesPc = Math.max(0d, totalEntriesPc - stockInitialPc);
        double stockRestPc = aggregate.getQuantite();
        double valeur = aggregate.getValeurStock() > 0 ? aggregate.getValeurStock() : stockRestPc * aggregate.getCoutAchat();
        double alertPc = 0d;
        if (latestStocker != null) {
            Mesure alerteMesure = latestStocker.getMesureId() != null
                    ? MesureDelegate.findMesure(latestStocker.getMesureId().getUid())
                    : null;
            double alertContent = alerteMesure != null && alerteMesure.getQuantContenu() != null && alerteMesure.getQuantContenu() > 0
                    ? alerteMesure.getQuantContenu() : 1d;
            alertPc = latestStocker.getStockAlerte() * alertContent;
        }

        InventoryItem item = new InventoryItem();
        item.setProduit(produit);
        item.setProductId(produit.getUid());
        item.setProductName(produit.getNomProduit());
        item.setRegion(aggregate.getRegion());
        item.setDate(aggregate.getDate());
        item.setCoutAchat(aggregate.getCoutAchat());
        item.setQuantite(stockRestPc);
        item.setNumlot(aggregate.getNumlot());
        item.setMesureLabel(displayMesure.getDescription());
        item.setQuantContenu(displayContent);
        item.setExpiryDate(aggregate.getDateExpiration());
        item.setDateExpir(aggregate.getDateExpiration() == null ? "-" : aggregate.getDateExpiration().format(INVENTORY_DATE_FORMAT));
        item.setLastStocker(latestStocker);
        item.setLastDestocker(latestDestocker);
        item.setLocalisation(latestStocker != null && latestStocker.getLocalisation() != null ? latestStocker.getLocalisation() : "");

        item.setStockInitial(stockInitialPc);
        item.setQuantEntreeValue(entreesPc);
        item.setQuantSortieValue(totalSortiesPc);
        item.setQuantRestValue(stockRestPc);
        item.setValeurStockValue(valeur);
        item.setStockAlerteValue(alertPc);

        item.setQuantEntree(formatInventoryQuantity(entreesPc, item));
        item.setQuantSortie(formatInventoryQuantity(totalSortiesPc, item));
        item.setQuantRest(formatInventoryQuantity(stockRestPc, item));
        item.setValeurStock(String.format("%.2f USD", valeur));
        item.setStockAlerte(formatInventoryQuantity(alertPc, item));
        return item;
    }

    private double toPieces(Stocker stocker) {
        if (stocker == null) {
            return 0d;
        }
        Mesure mesure = stocker.getMesureId() != null ? MesureDelegate.findMesure(stocker.getMesureId().getUid()) : null;
        double content = mesure != null && mesure.getQuantContenu() != null && mesure.getQuantContenu() > 0
                ? mesure.getQuantContenu() : 1d;
        return stocker.getQuantite() * content;
    }

    private double toPieces(Destocker destocker) {
        if (destocker == null) {
            return 0d;
        }
        Mesure mesure = destocker.getMesureId() != null ? MesureDelegate.findMesure(destocker.getMesureId().getUid()) : null;
        double content = mesure != null && mesure.getQuantContenu() != null && mesure.getQuantContenu() > 0
                ? mesure.getQuantContenu() : 1d;
        return destocker.getQuantite() * content;
    }

    private String formatInventoryQuantity(double pieces, InventoryItem item) {
        double content = item == null || item.getQuantContenu() <= 0 ? 1d : item.getQuantContenu();
        String label = item == null || item.getMesureLabel() == null ? "" : " " + item.getMesureLabel();
        return String.format("%.2f%s", pieces / content, label);
    }

    private void setupDepotInventoryFilters() {
        cbx_filter_color_inv_depot.getItems().setAll(
                "Tous les lots",
                "(Rouge) Déjà expiré",
                "(Orange foncé) Expire dans 1 mois",
                "(Orange) Expire dans 3 mois",
                "(Jaune) Expire dans 6 mois",
                "(Vert) Expire dans 12 mois");
        cbx_filter_color_inv_depot.getSelectionModel().selectFirst();

        filteredDepotInventory = new FilteredList<>(lisinvent, p -> true);

        ChangeListener<Object> filterListener = (obs, oldVal, newVal) -> applyDepotInventoryFilters();
        cbx_filter_color_inv_depot.getSelectionModel().selectedItemProperty().addListener(filterListener);
        chbx_filter_stock_fini_depot.selectedProperty().addListener(filterListener);
        chbx_filter_stock_non_fini_depot.selectedProperty().addListener(filterListener);
        cbx_choose_critere_de_selection.getSelectionModel().selectedItemProperty().addListener(filterListener);
        input_txt_criteres_mens.textProperty().addListener(filterListener);
    }

    private void applyDepotInventoryFilters() {
        if (filteredDepotInventory == null) {
            return;
        }
        String colorFilter = cbx_filter_color_inv_depot.getValue();
        boolean filterFini = chbx_filter_stock_fini_depot.isSelected();
        boolean filterNonFini = chbx_filter_stock_non_fini_depot.isSelected();
        String criteria = cbx_choose_critere_de_selection.getValue();
        int monthsFilter = parseMonthsFilter();

        filteredDepotInventory.setPredicate(item -> {
            if (item == null) {
                return false;
            }
            if (filterFini && !filterNonFini && item.getQuantRestValue() > 0) {
                return false;
            }
            if (!filterFini && filterNonFini && item.getQuantRestValue() <= 0) {
                return false;
            }

            int status = isStockExpired(item.getExpiryDate());
            if (colorFilter != null && !"Tous les lots".equals(colorFilter)) {
                if (colorFilter.contains("Rouge") && status != -1) {
                    return false;
                }
                if (colorFilter.contains("Orange foncé") && status != 1) {
                    return false;
                }
                if (colorFilter.contains("Orange") && !colorFilter.contains("foncé") && status != 3) {
                    return false;
                }
                if (colorFilter.contains("Jaune") && status != 6) {
                    return false;
                }
                if (colorFilter.contains("Vert") && status != 12) {
                    return false;
                }
            }

            if (criteria != null) {
                switch (criteria) {
                    case "Sans destockage" -> {
                        if (item.getQuantSortieValue() > 0) {
                            return false;
                        }
                    }
                    case "Stock en Alerte" -> {
                        if (item.getQuantRestValue() > item.getStockAlerteValue()) {
                            return false;
                        }
                    }
                    case "Expirés" -> {
                        if (status != -1) {
                            return false;
                        }
                    }
                    case "Stock à expirer dans :" -> {
                        if (monthsFilter <= 0 || item.getExpiryDate() == null) {
                            return false;
                        }
                        long remain = Constants.Datetime.dateInMillis(item.getExpiryDate()) - System.currentTimeMillis();
                        if (remain < 0 || remain > ((long) monthsFilter * Constants.UN_MOIS)) {
                            return false;
                        }
                    }
                    default -> {
                    }
                }
            }
            return true;
        });

        refreshDepotInventoryTable();
        updateDepotInventoryTotalLabel();
    }

    private int parseMonthsFilter() {
        try {
            return Integer.parseInt(input_txt_criteres_mens.getText() == null ? "0" : input_txt_criteres_mens.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void refreshDepotInventoryTable() {
        List<InventoryItem> source = currentDepotInventoryItems();
        int pageSize = rowsDataCount11 <= 0 ? 20 : rowsDataCount11;
        int pageCount = Math.max(1, (int) Math.ceil((double) source.size() / pageSize));
        pagination11.setPageCount(pageCount);

        int currentIndex = Math.min(pagination11.getCurrentPageIndex(), Math.max(0, pageCount - 1));
        int offset = currentIndex * pageSize;
        int limit = Math.min(offset + pageSize, source.size());
        if (source.isEmpty()) {
            table11.setItems(FXCollections.observableArrayList());
        } else {
            table11.setItems(FXCollections.observableArrayList(source.subList(offset, limit)));
        }
        count11.setText(String.format(bundle.getString("xitems"), source.size()));
    }

    private List<InventoryItem> currentDepotInventoryItems() {
        return filteredDepotInventory == null ? new ArrayList<>(lisinvent) : new ArrayList<>(filteredDepotInventory);
    }

    private void updateDepotInventoryTotalLabel() {
        double total = 0d;
        for (InventoryItem item : currentDepotInventoryItems()) {
            total += item.getValeurStockValue();
        }
        valStock = total;
        valSocks.setText("Valeur du stock : "
                + BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");

        boolean isRouge = cbx_filter_color_inv_depot.getValue() != null
                && cbx_filter_color_inv_depot.getValue().contains("Rouge");
        boolean criteriaExpired = "Expirés".equals(cbx_choose_critere_de_selection.getValue());
        btn_declasser_inv_depot.setDisable(currentDepotInventoryItems().isEmpty() || !(isRouge || criteriaExpired));
    }

    private int isStockExpired(LocalDate dateExpiration) {
        long now = System.currentTimeMillis();
        if (dateExpiration == null) {
            return 555;
        }
        long exp = Constants.Datetime.dateInMillis(dateExpiration);
        long interval = exp - now;
        if (interval <= 0) {
            return -1;
        }
        if (interval <= Constants.UN_MOIS) {
            return 1;
        }
        if (interval <= Constants.UN_MOIS * 3) {
            return 3;
        }
        if (interval <= Constants.UN_MOIS * 6) {
            return 6;
        }
        if (interval <= Constants.UN_MOIS * 12) {
            return 12;
        }
        return 555;
    }

    double sumLotPc(List<Destocker> ls, String numlot) {
        double s_qlot = 0;
        for (Destocker l : ls) {
            if (l.getNumlot() == null) {
                continue;
            }
            if (l.getNumlot().equals(numlot)) {
                Mesure m = l.getMesureId();
                Mesure mreel = MesureDelegate.findMesure(m.getUid());// strongDb.findByUid(Mesure.class, m.getUid());
                if (mreel == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(l.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                     // l.getProductId().getUid(),
                                                                                                                     // "quantContenu");
                    // strongDb.findByProduitAsc(Mesure.class,p.getUid(),"quantContenu");,
                    // p.getUid(), "quantContenu");
                    mreel = mesures.get(0);
                }
                s_qlot += l.getQuantite() * (mreel.getQuantContenu());
            }
        }
        return s_qlot;
    }

    public void loadInventaireDepotInInterval(List<Produit> lp, LocalDate date1, LocalDate date2) {

        valStock = 0;
        lisinvent.clear();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (Produit p : lp) {
                InventoryItem invent = new InventoryItem();

                List<Stocker> lst;
                List<Destocker> lsd;
                if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {

                    lst = StockerDelegate.findByDateIntervale(date1, date2);// strongDb.findAllByDateInterval(Stocker.class,
                                                                            // tools.Constants.Datetime.toUtilDate(date1),
                                                                            // tools.Constants.Datetime.toUtilDate(date2));
                    // .findWhereDateBetween("dateStocker",
                    // ,
                    // ,
                    // "productId.uid", p.getUid());
                    // .findAllEquals("productId.uid", p.getUid());
                    // Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());

                    lsd = DestockerDelegate.findByDateIntervale(date1, date2);// strongDb.findAllByDateInterval(Destocker.class,
                                                                              // tools.Constants.Datetime.toUtilDate(date1),
                                                                              // tools.Constants.Datetime.toUtilDate(date2));
                    // strongDb.findWhereDateBetween("dateDestockage",
                    // tools.Constants.Datetime.dateInMillis(date1),
                    // tools.Constants.Datetime.dateInMillis(date2),
                    // "productId.uid", p.getUid());
                    // strongDb.findAllEquals("productId.uid", p.getUid());
                    // Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
                } else {
                    lst = StockerDelegate.findByDateIntervale(date1, date2, region);// strongDb.findAllByDateIntervalInRegion(Stocker.class,
                                                                                    // tools.Constants.Datetime.toUtilDate(date1),
                    // tools.Constants.Datetime.toUtilDate(date2), region);
                    // strongDb.findWhereDateBetween("dateStocker",
                    // tools.Constants.Datetime.dateInMillis(date1),
                    // tools.Constants.Datetime.dateInMillis(date2),
                    // "productId.uid", p.getUid(), "region", region);
                    // .findAllEquals("productId.uid", p.getUid());
                    // Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());

                    lsd = DestockerDelegate.findByDateIntervale(date1, date2, region);// strongDb.findAllByDateIntervalInRegion(Destocker.class,
                                                                                      // tools.Constants.Datetime.toUtilDate(date1),
                    // tools.Constants.Datetime.toUtilDate(date2), region);
                    // strongDb.findWhereDateBetween("dateDestockage",
                    // tools.Constants.Datetime.dateInMillis(date1),
                    // tools.Constants.Datetime.dateInMillis(date2),
                    // "productId.uid", p.getUid(), "region", region);
                }
                // if (role.equals(Role.Trader.name())) {
                // lst = Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid(),
                // d1, d2);
                // lsd = Util.findDestockersForProduit(Util.filterNoNullMesure(destox),
                // p.getUid(), d1, d2);
                // } else {
                // lst = Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid(),
                // d1, d2);
                // lsd = Util.findDestockersForProduit(Util.filterNoNullMesure(destox),
                // p.getUid(), d1, d2);
                // }

                invent.setProduit(p);
                if (lst.isEmpty()) {
                    continue;
                }
                Stocker lastStk = lst.get(lst.size() - 1);
                int i = lsd.size() - 1;
                Destocker lastDstk = i <= 0 ? new Destocker() : lsd.get(i);
                Mesure mx = MesureDelegate.findMaxMesureByProduit(p.getUid());// strongDb.findMaxMesure(Mesure.class,
                                                                              // "quantContenu", p.getUid());
                // strongDb.findMaxMesure(Mesure.class,"quantContenu", p.getUid());
                invent.setLastStocker(lastStk);
                invent.setLastDestocker(lastDstk);
                Mesure mi = lastStk.getMesureId();
                Mesure mxr = MesureDelegate.findMesure(mi.getUid());// strongDb.findByUid(Mesure.class, mi.getUid());
                if (mxr == null) {
                    List<Mesure> mesures = MesureDelegate
                            .findAscSortedByQuantWithProduit(lastStk.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                              // lastStk.getProductId().getUid(),
                                                                                              // "quantContenu");
                    mxr = mesures.get(0);
                }
                invent.setStockAlerte((lastStk.getStockAlerte()) + " " + mxr.getDescription());
                // System.err.println(p.getMarque()+" "+mx.getDescription()+"
                // "+mx.getQuantContenu());
                double somVal = 0;
                double in = 0, out = 0;
                for (Stocker s : lst) {
                    Mesure mir = s.getMesureId();
                    Mesure m = MesureDelegate.findMesure(mir.getUid());// strongDb.findByUid(Mesure.class,
                                                                       // mir.getUid());
                    if (m == null) {
                        List<Mesure> mesures = MesureDelegate
                                .findAscSortedByQuantWithProduit(s.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                            // s.getProductId().getUid(),
                                                                                            // "quantContenu");
                        m = mesures.get(0);
                    }
                    double epcx = s.getQuantite() * m.getQuantContenu();
                    in += epcx;
                    double sumqsort = sumLotPc(lsd, s.getNumlot());
                    double rst = epcx - sumqsort;
                    double capc = s.getCoutAchat() / m.getQuantContenu();
                    somVal += rst * capc;
                }

                for (Destocker d : lsd) {
                    Mesure mire = d.getMesureId();
                    Mesure m = MesureDelegate.findMesure(mire.getUid());// strongDb.findByUid(Mesure.class,
                                                                        // mire.getUid());
                    if (m == null) {
                        List<Mesure> mesures = MesureDelegate
                                .findAscSortedByQuantWithProduit(d.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                            // d.getProductId().getUid(),
                                                                                            // "quantContenu");
                        m = mesures.get(0);
                    }
                    out += (d.getQuantite() * m.getQuantContenu());
                }
                double rst = in - out;
                if (mx == null) {
                    continue;
                }
                invent.setQuantEntree((in / mx.getQuantContenu()) + " " + mx.getDescription());
                invent.setQuantSortie((out / mx.getQuantContenu()) + " " + mx.getDescription());
                invent.setQuantRest((rst / mx.getQuantContenu()) + " " + mx.getDescription());
                invent.setValeurStock(
                        BigDecimal.valueOf(somVal).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
                lisinvent.add(invent);
                valStock += somVal;
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                valSocks.setText(bundle.getString("xvaleur_stock_dispo") + " : "
                        + BigDecimal.valueOf(valStock).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            }
        });
    }

    List<Stocker> findExpiry(Produit p, int mois) {
        List<Stocker> result = new ArrayList<>();
        List<Stocker> ss = StockerDelegate.findAscSortedByDateExpir(p.getUid());// strongDb.findByProduitAsc(Stocker.class,
                                                                                // p.getUid(), "dateExpir");
        // strongDb.findFullEqualAsc(new String[]{"productId.uid"}, new
        // String[]{p.getUid()}, "dateExpir");

        System.out.println("Time now timex");

        for (Stocker s : ss) {

            LocalDate datex = s.getDateExpir();
            if (datex == null) {
                continue;
            }
            long timex = Constants.Datetime.dateInMillis(datex);
            long remain = mois * tools.Constants.UN_MOIS;
            long time = System.currentTimeMillis();
            long diff = timex - time;
            System.out.println("Time now " + time + " mois " + remain + " diff " + diff + " time expire " + timex);
            if (diff <= remain) {
                result.add(s);
            }
        }
        return result;
    }

    List<Stocker> findExpiry(Produit p, String region, int mois) {
        List<Stocker> result = new ArrayList<>();
        List<Stocker> ss = StockerDelegate.findAscSortedByDateExpir(p.getUid(), region);// strongDb.findByProduitAsc(Stocker.class,
                                                                                        // p.getUid(), "dateExpir",
                                                                                        // region);
        // strongDb.findFullEqualAsc(new String[]{"productId.uid"}, new
        // String[]{p.getUid()}, "dateExpir");

        for (Stocker s : ss) {

            LocalDate datex = s.getDateExpir();
            if (datex == null) {
                continue;
            }

            long timex = Constants.Datetime.dateInMillis(datex);
            long inter = mois * tools.Constants.UN_MOIS;
            long time = System.currentTimeMillis();
            long comp = timex - time;
            if (comp <= inter) {
                result.add(s);
            }
        }
        return result;
    }

    public void loadInventaireExpirDepot(List<Produit> lp, int dureeExp, String region) {
        ObservableList<InventoryItem> result = FXCollections.observableArrayList();
        Calendar cExp = Calendar.getInstance();

        valStock = 0;
        for (Produit p : lp) {
            InventoryItem invent = new InventoryItem();
            List<Stocker> lst;
            List<Destocker> lsd;
            if (region == null) {

                lst = findExpiry(p, dureeExp);
                System.out.println("taille azo " + lst.size());
                // strongDb.findAllEquals("productId.uid", p.getUid());
                // Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
                lsd = DestockerDelegate.findByProduit(p.getUid());// strongDb.findByProduit(Destocker.class,
                                                                  // p.getUid());
                // Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            } else {

                lst = findExpiry(p, region, dureeExp);
                // strongDb.findFullEqual(new String[]{"productId.uid", "region"}, new
                // String[]{p.getUid(), region});
                // Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
                lsd = DestockerDelegate.findByProduit(p.getUid(), region);// strongDb.findByProduit(Destocker.class,
                                                                          // p.getUid(), region);
                // strongDb.findFullEqual(new String[]{"productId.uid", "region"}, new
                // String[]{p.getUid(), region});
                // Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            }
            // if (role.equals(Role.Trader.name())) {
            // lst =
            // Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
            // lsd =
            // Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            // } else {
            // lst =
            // Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
            // lsd =
            // Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            // }
            invent.setProduit(p);
            if (lst.isEmpty()) {
                continue;
            }
            Stocker lastStk = lst.get(lst.size() - 1);
            int i = lsd.size() - 1;
            Destocker lastDstk = i <= 0 ? new Destocker() : lsd.get(i);
            Mesure mx = MesureDelegate.findMaxMesureByProduit(p.getUid());// strongDb.findMaxMesure(Mesure.class,
                                                                          // "quantContenu", p.getUid());
            invent.setLastStocker(lastStk);
            invent.setLastDestocker(lastDstk);
            Mesure mxri = lastStk.getMesureId();
            Mesure mxr = MesureDelegate.findMesure(mxri.getUid());// strongDb.findByUid(Mesure.class, mxri.getUid());
            if (mxr == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(lastDstk.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                        // lastDstk.getProductId().getUid(),
                                                                                                                        // "quantContenu");
                mxr = mesures.get(0);
            }
            invent.setStockAlerte((lastStk.getStockAlerte()) + " " + mxr.getDescription());
            // System.err.println(p.getMarque()+" "+mx.getDescription()+"
            // "+mx.getQuantContenu());
            double somVal = 0;
            double in = 0, out = 0;
            for (Stocker s : lst) {
                Mesure mi = s.getMesureId();
                Mesure m = MesureDelegate.findMesure(mi.getUid());// strongDb.findByUid(Mesure.class, mi.getUid());
                if (m == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(s.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                     // s.getProductId().getUid(),
                                                                                                                     // "quantContenu");
                    m = mesures.get(0);
                }
                double epcx = s.getQuantite() * m.getQuantContenu();
                in += epcx;
                double sumqsort = sumLotPc(lsd, s.getNumlot());
                double rst = epcx - sumqsort;
                double capc = s.getCoutAchat() / m.getQuantContenu();
                somVal += rst * capc;
            }

            for (Destocker d : lsd) {
                Mesure mi = d.getMesureId();
                Mesure m = MesureDelegate.findMesure(mi.getUid());// strongDb.findByUid(Mesure.class, mi.getUid());
                if (m == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(d.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                     // d.getProductId().getUid(),
                                                                                                                     // "quantContenu");
                    m = mesures.get(0);
                }
                out += (d.getQuantite() * m.getQuantContenu());
            }
            double rst = in - out;
            if (mx == null) {
                continue;
            }
            invent.setQuantEntree((in / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantSortie((out / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantRest((rst / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setValeurStock(
                    BigDecimal.valueOf(somVal).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            lisinvent.add(invent);
            valStock += somVal;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                valSocks.setText(bundle.getString("xvaleur_stock_dispo") + " : "
                        + BigDecimal.valueOf(valStock).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            }
        });
        table11.setItems(result);
    }

    public void loadInventaireDejaExpirDepot(List<Produit> lp) {
        ObservableList<InventoryItem> result = FXCollections.observableArrayList();

        valStock = 0;
        for (Produit p : lp) {
            InventoryItem invent = new InventoryItem();
            List<Stocker> lst;
            List<Destocker> lsd;
            if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                lst = Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
                lsd = Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            } else {
                lst = Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
                lsd = Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            }
            invent.setProduit(p);
            if (lst.isEmpty()) {
                continue;
            }
            Stocker lastStk = lst.get(lst.size() - 1);
            int i = lsd.size() - 1;
            Destocker lastDstk = i <= 0 ? new Destocker() : lsd.get(i);
            Mesure mx = MesureDelegate.findMaxMesureByProduit(p.getUid());// strongDb.findMaxMesure(Mesure.class,
                                                                          // "quantContenu", p.getUid());
            // strongDb.findMaxMesure(Mesure.class,"quantContenu", p.getUid());
            LocalDate dt = lastStk.getDateExpir();
            if (dt != null) {

            }
            invent.setLastStocker(lastStk);
            invent.setLastDestocker(lastDstk);
            Mesure mxri = lastStk.getMesureId();
            Mesure mxr = MesureDelegate.findMesure(mxri.getUid());// strongDb.findByUid(Mesure.class, mxri.getUid());
            if (mxr == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(lastStk.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                       // lastStk.getProductId().getUid(),
                                                                                                                       // "quantContenu");
                mxr = mesures.get(0);
            }
            invent.setStockAlerte((lastStk.getStockAlerte()) + " " + mxr.getDescription());
            // System.err.println(p.getMarque()+" "+mx.getDescription()+"
            // "+mx.getQuantContenu());
            double somVal = 0, somQ = 0;
            double in = 0, out = 0;
            for (Stocker s : lst) {
                Mesure m = s.getMesureId();
                Mesure rell = MesureDelegate.findMesure(m.getUid());// strongDb.findByUid(Mesure.class, m.getUid());
                if (rell == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(s.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                     // s.getProductId().getUid(),
                                                                                                                     // "quantContenu");
                    rell = mesures.get(0);
                }
                in += (s.getQuantite() * rell.getQuantContenu());
                somVal += s.getPrixAchatTotal();
                somQ += s.getQuantite();
            }
            double cu = somVal / somQ;
            for (Destocker d : lsd) {
                Mesure m = d.getMesureId();
                Mesure mr = MesureDelegate.findMesure(m.getUid());// strongDb.findByUid(Mesure.class, m.getUid());
                if (mr == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(d.getProductId().getUid());
                    mr = mesures.get(0);
                }
                out += (d.getQuantite() * mr.getQuantContenu());
            }
            double rst = in - out;
            invent.setQuantEntree((in / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantSortie((out / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantRest((rst / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setValeurStock(
                    BigDecimal.valueOf(rst * cu).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            long cts = System.currentTimeMillis();
            long exp = dt == null ? 0 : Constants.Datetime.dateInMillis(dt);
            long comp = exp - cts;
            if (comp <= 0 && dt != null) {
                result.add(invent);
            }

            valStock += (rst * cu);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                valSocks.setText(bundle.getString("xvaleur_stock_dispo") + " : "
                        + BigDecimal.valueOf(valStock).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            }
        });
        table11.setItems(result);
    }

    public void loadInventaireAlerte(List<Produit> lp) {
        ObservableList<InventoryItem> result = FXCollections.observableArrayList();
        valStock = 0;
        for (Produit p : lp) {
            InventoryItem invent = new InventoryItem();
            List<Stocker> lst;
            List<Destocker> lsd;
            if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                lst = Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
                lsd = Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            } else {
                lst = Util.findStockersForProduit(Util.filterNoNullMesure(stox), p.getUid());
                lsd = Util.findDestockersForProduit(Util.filterNoNullMesure(destox), p.getUid());
            }
            invent.setProduit(p);
            if (lst.isEmpty()) {
                continue;
            }
            Stocker lastStk = lst.get(lst.size() - 1);
            int i = lsd.size() - 1;
            Destocker lastDstk = i <= 0 ? new Destocker() : lsd.get(i);
            Mesure mx = MesureDelegate.findMaxMesureByProduit(p.getUid());// strongDb.findMaxMesure(Mesure.class,
                                                                          // "quantContenu", p.getUid());
            invent.setLastStocker(lastStk);
            invent.setLastDestocker(lastDstk);
            Mesure mxir = lastStk.getMesureId();
            Mesure mxr = MesureDelegate.findMesure(mxir.getUid());// strongDb.findByUid(Mesure.class, mxir.getUid());
            if (mxr == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                  // p.getUid(),
                                                                                                  // "quantContenu");
                mxr = mesures.get(0);
            }
            invent.setStockAlerte((lastStk.getStockAlerte()) + " " + mxr.getDescription());
            // System.err.println(p.getMarque()+" "+mx.getDescription()+"
            // "+mx.getQuantContenu());
            double somVal = 0, somQ = 0;
            double in = 0, out = 0;
            for (Stocker s : lst) {
                Mesure mi = s.getMesureId();
                Mesure m = MesureDelegate.findMesure(mi.getUid());// strongDb.findByUid(Mesure.class, mi.getUid());
                if (m == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(s.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                     // s.getProductId().getUid(),
                                                                                                                     // "quantContenu");
                    m = mesures.get(0);
                }
                in += (s.getQuantite() * m.getQuantContenu());
                somVal += s.getPrixAchatTotal();
                somQ += s.getQuantite();
            }
            double cu = somVal / somQ;
            for (Destocker d : lsd) {
                Mesure mi = d.getMesureId();
                Mesure m = MesureDelegate.findMesure(mi.getUid());// strongDb.findByUid(Mesure.class, mi.getUid());
                if (m == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(d.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                     // d.getProductId().getUid(),
                                                                                                                     // "quantContenu");
                    m = mesures.get(0);
                }
                out += (d.getQuantite() * m.getQuantContenu());
            }
            double rst = in - out;
            invent.setQuantEntree((in / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantSortie((out / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantRest((rst / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setValeurStock(
                    BigDecimal.valueOf(rst * cu).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            double alert = lastStk.getStockAlerte();
            if (rst <= alert) {
                result.add(invent);
            }
            valStock += (rst * cu);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                valSocks.setText(bundle.getString("xvaleur_stock_dispo") + " : "
                        + BigDecimal.valueOf(valStock).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            }
        });
        table11.setItems(result);
    }

    public void loadInventaire100Destockage(List<Produit> lp) {
        ObservableList<InventoryItem> result = FXCollections.observableArrayList();
        valStock = 0;
        for (Produit p : lp) {
            InventoryItem invent = new InventoryItem();
            List<Stocker> lst;
            List<Destocker> lsd;
            if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                lst = requisitionToStocker(RecquisitionDelegate.findRecquisitionByProduit(p.getUid()));// strongDb.findByProduit(Recquisition.class,
                                                                                                       // p.getUid()));
                lsd = ligneVenteToDestocker(LigneVenteDelegate.findByProduit(p.getUid()));// strongDb.findByProduit(LigneVente.class,
                                                                                          // p.getUid()));
            } else {
                lst = requisitionToStocker(RecquisitionDelegate.findRecquisitionByProduitRegion(p.getUid(), region));
                lsd = ligneVenteToDestocker(LigneVenteDelegate.findByProduitRegion(p.getUid(), region));
            }
            invent.setProduit(p);
            if (lst.isEmpty()) {
                continue;
            }
            Stocker lastStk = lst.get(lst.size() - 1);
            int i = lsd.size() - 1;
            Destocker lastDstk = i <= 0 ? new Destocker() : lsd.get(i);
            Mesure mx = MesureDelegate.findMaxMesureByProduit(p.getUid());// strongDb.findMaxMesure(Mesure.class,
                                                                          // "quantContenu", p.getUid());
            double sa = lastStk.getStockAlerte();
            lastStk.setStockAlerte((sa / mx.getQuantContenu()));
            invent.setLastStocker(lastStk);
            invent.setLastDestocker(lastDstk);
            double somVal = 0, somQ = 0;
            // System.err.println(p.getMarque()+" "+mx.getDescription()+"
            // "+mx.getQuantContenu());
            double in = 0, out = 0;
            for (Stocker s : lst) {
                Mesure m = s.getMesureId();
                Mesure mz = MesureDelegate.findMesure(m.getUid());// strongDb.findByUid(Mesure.class, m.getUid());
                if (mz == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                      // p.getUid(),
                                                                                                      // "quantContenu");
                    mz = mesures.get(0);
                }
                in += (s.getQuantite() * mz.getQuantContenu());
                somVal += s.getPrixAchatTotal();
                somQ += s.getQuantite();
            }
            double cu = somVal / somQ;
            for (Destocker d : lsd) {
                Mesure m = d.getMesureId();
                Mesure mr = MesureDelegate.findMesure(m.getUid());// strongDb.findByUid(Mesure.class, m.getUid());
                if (mr == null) {
                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(p.getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                      // p.getUid(),
                                                                                                      // "quantContenu");
                    mr = mesures.get(0);
                }
                out += (d.getQuantite() * mr.getQuantContenu());
            }
            double rst = in - out;
            invent.setQuantEntree((in / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantSortie((out / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setQuantRest((rst / mx.getQuantContenu()) + " " + mx.getDescription());
            invent.setValeurStock(
                    BigDecimal.valueOf(rst * cu).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            if (out == 0) {
                result.add(invent);
            }
            valStock += (rst * cu);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                valSocks.setText(bundle.getString("xvaleur_stock_dispo") + " : "
                        + BigDecimal.valueOf(valStock).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            }
        });
        table11.setItems(result);

    }

    private List<Stocker> requisitionToStocker(List<Recquisition> reqs) {
        List<Stocker> lst = new ArrayList<>();
        for (Recquisition r : reqs) {
            Stocker s = new Stocker();
            s.setCoutAchat(r.getCoutAchat());
            s.setDateExpir(r.getDateExpiry());
            s.setDateStocker(r.getDate());
            s.setQuantite(r.getQuantite());
            s.setStockAlerte(r.getStockAlert());
            s.setProductId(r.getProductId());
            Mesure mzr = MesureDelegate.findMesure(r.getMesureId().getUid());
            if (mzr == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(r.getProductId().getUid());// strongDb.findByProduitAsc(Mesure.class,
                                                                                                                 // r.getProductId().getUid(),
                                                                                                                 // "quantContenu");
                mzr = mesures.get(0);
            }
            s.setMesureId(mzr);
            s.setRegion(r.getRegion());
            s.setObservation(r.getObservation());
            lst.add(s);
        }
        return lst;
    }

    private List<Destocker> ligneVenteToDestocker(List<LigneVente> lvs) {
        List<Destocker> lst = new ArrayList<>();
        for (LigneVente r : lvs) {
            Destocker s = new Destocker();
            Vente v = VenteDelegate.findVente(r.getReference().getUid());// strongDb.findByUid(Vente.class,
                                                                         // r.getReference().getUid());
            if (v == null) {
                continue;
            }
            s.setDateDestockage(v.getDateVente());
            s.setQuantite(r.getQuantite());
            s.setProductId(r.getProductId());
            Mesure mzr = MesureDelegate.findMesure(r.getMesureId().getUid());// strongDb.findByUid(Mesure.class,
                                                                             // r.getMesureId().getUid());
            if (mzr == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(s.getProductId().getUid());
                mzr = mesures.get(0);
            }
            s.setMesureId(mzr);
            s.setRegion(v.getRegion());
            s.setDestination(v.getRegion());
            lst.add(s);
        }
        return lst;
    }

    public void search(String query) {
        if (tab_livraison.isSelected()) {
            if (query == null) {
                table_stockage.setItems(list_stockers);
                return;
            }
            if (listlivr == null) {
                return;
            }
            ObservableList<Stocker> rst = FXCollections.observableArrayList();

            double som = 0;
            for (Stocker liv : stox) {
                Produit p = liv.getProductId();
                if (p == null) {
                    continue;
                }
                Produit f = ProduitDelegate.findProduit(p.getUid());
                if (f == null) {

                    continue;
                }
                String comp = f.getNomProduit() + " " + f.getMarque() + " " + f.getModele() + " " + f.getTaille()
                        + " " + liv.getNumlot() + " " + liv.getLocalisation() + " " + liv.getLivraisId().getReference()
                        + " " + liv.getStockAlerte() + " " + liv.getLibelle() + " " + liv.getLocalisation() + " "
                        + liv.getDateStocker().toString();
                if (comp.toUpperCase().contains(query.toUpperCase())) {
                    rst.add(liv);
                    som += liv.getPrixAchatTotal();
                }
            }
            table_stockage.setItems(rst);
            if (role.equals(Role.Trader.name()) || role.equals(Role.Manager.name())
                    || role.equals(Role.Finance.name()) || role.equals(Role.Finance_ALL_ACCESS)) {
                global_achat.setText("Total : " + som + " USD");
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    count.setText(table_stockage.getItems().size() + " éléments");
                }
            });
        } else if (tab_destock.isSelected()) {
            if (query == null) {
                table1.setItems(lisdestocker);
                return;
            }
            ObservableList<Destocker> rst = FXCollections.observableArrayList();
            for (Destocker d : lisdestocker) {
                Produit p = Util.findProduit(products, d.getProductId().getUid());
                if (p == null) {
                    continue;
                }
                String s = d.getDestination() + " " + p.getCodebar() + " " + p.getMarque() + " " + p.getModele() + " "
                        + p.getNomProduit()
                        + " " + p.getTaille() + " " + p.getCouleur() + " " + d.getReference() + " " + d.getRegion()
                        + " " + d.getLibelle();
                if (s.toUpperCase().contains(query.toUpperCase())) {
                    rst.add(d);
                }
            }
            table1.setItems(rst);
        } else if (tab_invent.isSelected()) {
            if (query == null) {
                refreshDepotInventoryTable();
                return;
            }
            ObservableList<InventoryItem> invs = FXCollections.observableArrayList(currentDepotInventoryItems());
            ObservableList<InventoryItem> result = FXCollections.observableArrayList();
            for (InventoryItem item : invs) {
                Produit p = item.getProduit() == null ? null : Util.findProduit(products, item.getProduit().getUid());
                if (p == null) {
                    continue;
                }
                Destocker lastDestocker = item.getLastDestocker();
                Stocker lastStocker = item.getLastStocker();
                String destination = lastDestocker == null || lastDestocker.getDestination() == null ? "" : lastDestocker.getDestination();
                String reference = lastDestocker == null || lastDestocker.getReference() == null ? "" : lastDestocker.getReference();
                String movementRegion = lastDestocker == null || lastDestocker.getRegion() == null ? "" : lastDestocker.getRegion();
                String location = lastStocker == null || lastStocker.getLocalisation() == null ? "" : lastStocker.getLocalisation();
                String e = p.getCodebar() + " " + p.getNomProduit() + " " + p.getMarque()
                        + " " + p.getModele() + " " + p.getCouleur() + " " + p.getTaille()
                        + " " + item.getNumlot()
                        + " " + destination
                        + " " + reference + " " + movementRegion
                        + " " + location;
                if (e.toUpperCase().contains(query.toUpperCase())) {
                    result.add(item);
                }
            }
            table11.setItems(result);
        }
    }

    private void configLivCbx() {

        cbx_choose_critere_de_selection.setItems(FXCollections.observableArrayList("Sans destockage", "Stock en Alerte",
                "Stock à expirer dans :", "Expirés"));
        cbx_choose_critere_de_selection.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue,
                            String newValue) {
                        choosen_criteria = newValue;
                        if (filteredDepotInventory != null) {
                            applyDepotInventoryFilters();
                        }
                    }
                });
        list_supplier.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Fournisseur>() {
            @Override
            public void changed(ObservableValue<? extends Fournisseur> observable, Fournisseur oldValue,
                    Fournisseur newValue) {
                if (newValue != null) {
                    choosenSupply = newValue;
                    Double somm = LivraisonDelegate.sumBySupplier(choosenSupply.getUid());
                    global_achat.setText("Total achat : " + somm);
                    List<Livraison> livrs = LivraisonDelegate.findBySupplier(choosenSupply.getUid());
                    curent_path.setText(choosenSupply.getNomFourn() + ", " + choosenSupply.getAdresse() + " "
                            + choosenSupply.getPhone() + "/ " + livrs.size() + " Livraisons");

                }
            }
        });
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        // TODO
        configTables();
        MainUI.setPattern(dpk_fin_stk);
        MainUI.setPattern(dpk_debut_stk);
        // MainUI.setPattern(dpk_fin_inv);
        // MainUI.setPattern(dpk_debut_inv);
        list_stockers = FXCollections.observableArrayList();
        listlivr = FXCollections.observableArrayList();
        listfourn = FXCollections.observableArrayList();
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        region = pref.get("region", "...");
        role = pref.get("priv", null);
        token = pref.get("token", null);
        entr = pref.get("eUid", "");
        applyPermissionUiState();
        pane_wait_import.setVisible(false);
        pagination.setPageFactory(this::createDataPage);
        pagination1.setPageFactory(this::createDataPage);
        pagination11.setPageFactory(this::createDataPage);
        setupDepotInventoryFilters();

        ContextMenu contextMenut11 = new ContextMenu();
        MenuItem menuItem1 = new MenuItem(bundle.getString("stockfolio"));
        MenuItem menuItem2 = new MenuItem(bundle.getString("savestore"));
        MenuItem menuItem3 = new MenuItem(bundle.getString("saveunstore"));
        // add menu items to menu
        contextMenut11.getItems().add(menuItem1);
        contextMenut11.getItems().add(menuItem2);
        contextMenut11.getItems().add(menuItem3);
        table11.setContextMenu(contextMenut11);
        menuItem2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tabs_gs.getSelectionModel().select(tab_livraison);
                MainUI.floatDialog(tools.Constants.STOCKAGE_DLG, 716, 746, null, kazisafe, chstocker,
                        tools.Constants.ACTION_CREATE, entreprise, null);
            }
        });
        menuItem3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tabs_gs.getSelectionModel().select(tab_destock);
                MainUI.floatDialog(tools.Constants.DESTOCKAGE_DLG, 600, 723, null, kazisafe, chdestocker,
                        tools.Constants.ACTION_CREATE, entreprise);

            }
        });
        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // generation de la fiche de stock
                InventoryItem ii = table11.getSelectionModel().getSelectedItem();
                Produit p = ii.getProduit();
                MainUI.floatDialog(tools.Constants.FICHESTOCK_DLG, 1165, 665, null, kazisafe, p, entreprise);
            }
        });

        ContextMenu ctxtMliv = new ContextMenu();
        MenuItem menuItemliv = new MenuItem(bundle.getString("delete"));
        MenuItem updateLiv = new MenuItem(bundle.getString("xbtn.update"));
        ctxtMliv.getItems().add(updateLiv);
        ctxtMliv.getItems().add(menuItemliv);
        table_stockage.setContextMenu(ctxtMliv);
        menuItemliv.setOnAction((ActionEvent event) -> {
            ctxtMliv.hide();
            if (chstocker != null) {
                Livraison lv = chstocker.getLivraisId();
                String ref = lv.getReference();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Voulez vous vraiment supprimer le stock de " + chstocker.getProductId().getNomProduit()
                                + " ? \nCeci suprimera toutes les transactions de stock ayant cette reference :" + ref,
                        ButtonType.YES, ButtonType.CANCEL);
                alert.setTitle("Attention!");
                alert.setHeaderText(null);
                Optional<ButtonType> showAndWait = alert.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    if (role.equals(Role.Trader.name())
                            | role.equals(Role.Manager.name())
                            | role.equals(Role.Magazinner.name())
                            | role.contains(Role.ALL_ACCESS.name())) {
                        StockerDelegate.deleteStocker(chstocker);
                        Produit prod = chstocker.getProductId();
                        list_stockers.remove(chstocker);
                        List<Destocker> dtks = DestockerDelegate.findByReferenceAndProduit(prod.getUid(), ref);
                        if (!dtks.isEmpty()) {
                            for (Destocker dtk : dtks) {
                                DestockerDelegate.deleteDestocker(dtk);
                            }
                        }
                        List<Recquisition> rqs = RecquisitionDelegate.findByReference(prod.getUid(), ref);
                        if (!rqs.isEmpty()) {
                            for (Recquisition rq : rqs) {
                                RecquisitionDelegate.deleteRecquisition(rq);
                                List<PrixDeVente> prices = PrixDeVenteDelegate.findPricesForRecq(rq.getUid());
                                for (PrixDeVente price : prices) {
                                    PrixDeVenteDelegate.deletePrixDeVente(price);
                                }
                            }
                        }
                    }
                }
            }
        });
        updateLiv.setOnAction((ActionEvent event) -> {
            updateStock();
            ctxtMliv.hide();
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
        list_supplier.setContextMenu(ctxt);
        updatef.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenSupply != null) {
                    MainUI.floatDialog(tools.Constants.FOURNISSEUR_DLG, 1090, 508, null, kazisafe, entreprise,
                            choosenSupply);
                }
            }
        });
        deletef.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
                            listfourn.remove(choosenSupply);
                            count_fournisseur.setText(String.format(bundle.getString("xitems"), listfourn.size()));
                            MainUI.notify(null, "Succes", "Fournisseur supprime avec success", 3, "info");
                        } else {
                            MainUI.notify(null, "Attention",
                                    "Vous n'avez pas assez de privileges pour effectuer cette action", 3, "warning");
                        }
                    }
                }
            }
        });
        livraizf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenSupply != null) {
                    List<Livraison> livrs = LivraisonDelegate.findBySupplier(choosenSupply.getUid());
                    // list_livraison.setItems(FXCollections.observableArrayList(livrs));
                    listlivr.clear();
                    listlivr.addAll(livrs);
                    count_livraizon.setText(String.format(bundle.getString("xitems"), livrs.size()));
                    tabpane_livrz.getSelectionModel().select(tab_livraiz);
                    chbx_filter.setSelected(true);
                    if (livrs.isEmpty()) {
                        return;
                    }
                    Livraison livr = livrs.get(0);
                    List<Stocker> stks = StockerDelegate.findStockerByLivraison(livr.getUid());
                    table_stockage.setItems(FXCollections.observableArrayList(stks));
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

        ContextMenu ctxtl = new ContextMenu();
        MenuItem updatel = new MenuItem(bundle.getString("updateliv"));
        MenuItem deletel = new MenuItem(bundle.getString("delete"));
        MenuItem additems = new MenuItem(bundle.getString("xaddingitem"));
        MenuItem settleThisDeliveryDebt = new MenuItem("Regler cette dette fournisseur");
        ctxtl.getItems().add(updatel);
        ctxtl.getItems().add(deletel);
        ctxtl.getItems().add(additems);
        ctxtl.getItems().add(settleThisDeliveryDebt);
        list_livraison.setContextMenu(ctxtl);
        updatel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (livraison != null) {
                    MainUI.floatDialog(tools.Constants.DELIVERY_DLG, 600, 468, null, kazisafe, entreprise, livraison,Constants.STORAGE);
                }
            }
        });
        deletel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (livraison != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Voulez vous vraiment supprimer la livraison " + livraison.getNumPiece() + " ?",
                            ButtonType.YES, ButtonType.CANCEL);
                    alert.setTitle("Attention!");
                    alert.setHeaderText(null);
                    Optional<ButtonType> showAndWait = alert.showAndWait();
                    if (showAndWait.get() == ButtonType.YES) {
                        if (role.equals(Role.Trader.name())
                                | role.equals(Role.Manager.name())
                                | role.equals(Role.Magazinner.name())
                                | role.contains(Role.ALL_ACCESS.name())) {
                            LivraisonDelegate.deleteLivraison(livraison);
                            listlivr.remove(livraison);
                            count_livraizon.setText(String.format(bundle.getString("xitems"), listlivr.size()));
                            MainUI.notify(null, "Succes", "Livraison supprimee avec success", 3, "info");
                        } else {
                            MainUI.notify(null, "Attention",
                                    "Vous n'avez pas assez de privileges pour effectuer cette action", 3, "warning");
                        }
                    }
                }
            }
        });
        additems.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (livraison != null) {
                    MainUI.floatDialog(tools.Constants.STOCKAGE_DLG, 716, 746, null, kazisafe, livraison,
                            tools.Constants.ACTION_CREATE, entreprise, null);
                }
            }
        });
        settleThisDeliveryDebt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (livraison == null) {
                    MainUI.notify(null, "Selection", "Selectionnez d'abord une livraison", 3, "warning");
                    return;
                }
                settleSupplierDebt(livraison);
            }
        });

        Tooltip.install(btn_refresh, new Tooltip("Actualiser les données"));
        Tooltip.install(btn_delete, new Tooltip("Supprimer les données"));
        Tooltip.install(btn_update, new Tooltip("Modifier les données"));

        Tooltip.install(btn_refresh_d, new Tooltip("Actualiser les données"));
        Tooltip.install(btn_delete_d, new Tooltip("Supprimer les données"));
        Tooltip.install(btn_update_d, new Tooltip("Modifier les données"));

        Tooltip.install(btn_refresh_inv, new Tooltip("Actualiser les données d'inventaire"));
        Tooltip.install(btn_delete, new Tooltip("Supprimer les données"));
        Tooltip.install(btn_export_i, new Tooltip("Exporter l'inventaire sous excel"));
        Tooltip.install(valSocks, new Tooltip("valeur du stock selon " + (pref.get("meth", "fifo"))));
        Tooltip.install(newdelivery, new Tooltip(bundle.getString("xtooltip.newdelivery")));
        Tooltip.install(newitems, new Tooltip(bundle.getString("xtooltip.newitems")));
        Tooltip.install(newsuplier, new Tooltip(bundle.getString("xtooltip.newsupplier")));
        applyPermissionUiState();
        NotificationHandler.setOnDataSyncListener((model) -> {
            if (model instanceof Stocker stocker) {
                if (list_stockers == null)
                    return;
                int index = list_stockers.indexOf(stocker);
                if (index == -1) {
                    list_stockers.add(stocker);
                } else {
                    list_stockers.set(index, stocker);
                }
            } else if (model instanceof Fournisseur fr) {
                if (listfourn == null)
                    return;
                int index = listfourn.indexOf(fr);
                if (index == -1) {
                    listfourn.add(fr);
                } else {
                    listfourn.set(index, fr);
                }
            } else if (model instanceof Destocker destocker) {
                if (lisdestocker == null)
                    return;
                int index = lisdestocker.indexOf(destocker);
                if (index == -1) {
                    lisdestocker.add(destocker);
                } else {
                    lisdestocker.set(index, destocker);
                }
            } else if (model instanceof Livraison livr) {
                if (listlivr == null)
                    return;
                int index = listlivr.indexOf(livr);
                if (index == -1) {
                    listlivr.add(livr);
                } else {
                    listlivr.set(index, livr);
                }
            }
        });
    }

    private void updateStock() {
        if (!hasPermission(PermitTo.UPDATE_STOCKER)) {
            MainUI.notify(null, "Permission", "Vous n'avez pas la permission de modifier le stockage", 3, "warning");
            return;
        }
        if (chstocker == null) {
            return;
        }
        tabs_gs.getSelectionModel().select(tab_livraison);
        Livraison liv = chstocker.getLivraisId();
        MainUI.floatDialog(tools.Constants.STOCKAGE_DLG, 716, 746, null, kazisafe, liv, tools.Constants.ACTION_UPDATE,
                entreprise, chstocker);
    }

    @FXML
    private void addItems2Livr(MouseEvent event) {
        if (livraison != null) {
            MainUI.floatDialog(tools.Constants.STOCKAGE_DLG, 716, 746, null, kazisafe, livraison,
                    tools.Constants.ACTION_CREATE, entreprise, null);
        } else {
            MainUI.notify(null, "Erreur", "Selectionnez d'abord une livraison puis reessayer", 3, "error");
        }
    }

    @FXML
    private void exportInventaitaire(Event evt) {
        if (table11.getItems().isEmpty()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsInv = Util.exportXlsInventory(table11.getItems());
                try {
                    Desktop.getDesktop().open(xlsInv);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @FXML
    private void updateStocker(Event e) {
        updateStock();
    }

    @FXML
    private void deleteStocker(Event e) {
        if (!hasPermission(PermitTo.DELETE_STOCKER)) {
            MainUI.notify(null, "Permission", "Vous n'avez pas la permission de supprimer ce stockage", 3, "warning");
            return;
        }
        if (chstocker == null) {
            MainUI.notify(null, "Erreur", "Veuillez selectionner un element puis reesayer", 3, "error");
            return;
        }
        Livraison lv = chstocker.getLivraisId();
        String ref = lv.getReference();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez vous vraiment supprimer le stock de " + chstocker.getProductId().getNomProduit()
                        + " ? \nCeci suprimera toutes les transactions de stock ayant cette reference :" + ref,
                ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            StockerDelegate.deleteStocker(chstocker);
            List<Destocker> dtks = DestockerDelegate.findByReference(ref);
            if (!dtks.isEmpty()) {
                for (Destocker dtk : dtks) {
                    DestockerDelegate.deleteDestocker(dtk);
                }
            }
            List<Recquisition> rqs = RecquisitionDelegate.findByReference(ref);
            if (!rqs.isEmpty()) {
                for (Recquisition rq : rqs) {
                    RecquisitionDelegate.deleteRecquisition(rq);
                    List<PrixDeVente> prices = PrixDeVenteDelegate.findPricesForRecq(rq.getUid());
                    for (PrixDeVente price : prices) {
                        PrixDeVenteDelegate.deletePrixDeVente(price);
                    }
                }
            }
        }
    }

    @FXML
    public void viewStockForm(ActionEvent evt) {
        MainUI.floatDialog(tools.Constants.STOCKAGE_DLG, 716, 746, null, kazisafe, chstocker,
                tools.Constants.ACTION_CREATE, entreprise, null);
    }

    @FXML
    public void viewDeStockForm(ActionEvent evt) {
        MainUI.floatDialog(tools.Constants.DESTOCKAGE_DLG, 600, 723, null, kazisafe, chdestocker,
                tools.Constants.ACTION_CREATE, entreprise);
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
    private void deleteFinAccount(MouseEvent event) {
        if (!hasPermission(PermitTo.DELETE_DESTOCKER)) {
            MainUI.notify(null, "Permission", "Vous n'avez pas la permission de supprimer ce destockage", 3, "warning");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez vous vraiment suprimmer ce destockage ?",
                ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            if (chdestocker != null) {
                DestockerDelegate.deleteDestocker(chdestocker);// strongDb.delete(chdestocker);
                MainUI.notify(null, "Suppression", "Destockage suprimé avec succès", 4, "info");
                MainuiController.getInstance().switchToStock(event);
                if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                    kazisafe.deleteDestockage(chdestocker.getUid()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> rspns) {
                            System.err.println("Destockage Deletion response " + rspns.message());
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable thrwbl) {
                            System.err.println("DeSTk deletion Error " + thrwbl.getMessage());
                        }
                    });
                }
            } else {
                MainUI.notify(null, "Erreur de suppression", "Aucune ligne n'a été séléctionnée", 4, "error");
            }
        }
    }

    @FXML
    private void updateFinAccount(MouseEvent event) {
        if (!hasPermission(PermitTo.UPDATE_DESTOCKER)) {
            MainUI.notify(null, "Permission", "Vous n'avez pas la permission de modifier ce destockage", 3, "warning");
            return;
        }
        MainUI.floatDialog(tools.Constants.DESTOCKAGE_DLG, 600, 723, null, kazisafe, chdestocker,
                tools.Constants.ACTION_UPDATE, entreprise);
    }

    private void applyPermissionUiState() {
        boolean canUpdateStocker = hasPermission(PermitTo.UPDATE_STOCKER);
        boolean canDeleteStocker = hasPermission(PermitTo.DELETE_STOCKER);
        boolean canUpdateDestocker = hasPermission(PermitTo.UPDATE_DESTOCKER);
        boolean canDeleteDestocker = hasPermission(PermitTo.DELETE_DESTOCKER);

        btn_update.setDisable(!canUpdateStocker);
        btn_update.setOpacity(canUpdateStocker ? 1d : 0.5d);
        btn_update.setMouseTransparent(!canUpdateStocker);

        btn_delete.setDisable(!canDeleteStocker);
        btn_delete.setOpacity(canDeleteStocker ? 1d : 0.5d);
        btn_delete.setMouseTransparent(!canDeleteStocker);

        btn_update_d.setDisable(!canUpdateDestocker);
        btn_update_d.setOpacity(canUpdateDestocker ? 1d : 0.5d);
        btn_update_d.setMouseTransparent(!canUpdateDestocker);

        btn_delete_d.setDisable(!canDeleteDestocker);
        btn_delete_d.setOpacity(canDeleteDestocker ? 1d : 0.5d);
        btn_delete_d.setMouseTransparent(!canDeleteDestocker);
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
        target.setToreceive(left);
        String baseObs = target.getObservation() == null ? "" : target.getObservation();
        String line = " | RECU_FOURNISSEUR=" + recu + " ; MONTANT=" + applied + " ; DATE=" + LocalDateTime.now();
        target.setObservation((baseObs + line).trim());
        Livraison saved = LivraisonDelegate.updateLivraison(target);
        if (saved == null) {
            MainUI.notify(null, "Erreur", "Echec d'enregistrement local du reglement", 3, "error");
            return;
        }
        Util.sync(saved, Constants.ACTION_UPDATE, Tables.LIVRAISON);
        syncLivraisonByHttp(saved);
        int index = listlivr.indexOf(saved);
        if (index >= 0) {
            listlivr.set(index, saved);
        }
        if (livraison != null && livraison.getUid().equals(saved.getUid())) {
            livraison = saved;
            global_achat.setText("Total : USD " + safeAmount(saved.getPayed()));
        }
        MainUI.notify(null, "Succes", "Reglement fournisseur enregistre (recu: " + recu + ")", 4, "info");
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

    @FXML
    private void refreshFinAccount(MouseEvent event) {
        if (tab_destock.isSelected()) {
            lisdestocker.setAll(Util.filterNoNullMesure(destox));
            table1.setItems(lisdestocker);
        } else if (tab_invent.isSelected()) {
            loadInv();
        } else {
            table_stockage.setItems(list_stockers);
        }
        Executors.newCachedThreadPool()
                .execute(() -> {
                    Refresher rfr = new Refresher("STOCKS");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Util.sync(rfr, "read", Tables.REFRESH);
                });
    }

    @FXML
    private void lstockage(ActionEvent e) {
        if (dpk_debut_stk.getValue() == null || dpk_fin_stk.getValue() == null) {
            return;
        }
        List<Stocker> fs = StockerDelegate.findByDateIntervale(dpk_debut_stk.getValue(), dpk_fin_stk.getValue());
        ObservableList<Stocker> obl = FXCollections.observableArrayList(fs);
        table_stockage.setItems(obl);
    }

    @FXML
    private void selectRowPerPage(ActionEvent evt) {

        if (tab_livraison.isSelected()) {
            ComboBox cbx = (ComboBox) evt.getSource();
            rowsDataCount = (int) cbx.getSelectionModel().getSelectedItem();
            pagination.setPageFactory(this::createDataPage);
            System.out.println("Row set to " + rowsDataCount);
        } else if (tab_destock.isSelected()) {
            ComboBox cbx = (ComboBox) evt.getSource();
            rowsDataCount1 = (int) cbx.getSelectionModel().getSelectedItem();
            pagination1.setPageFactory(this::createDataPage);
            System.out.println("Row set to " + rowsDataCount1);
        } else {
            ComboBox cbx = (ComboBox) evt.getSource();
            rowsDataCount11 = (int) cbx.getSelectionModel().getSelectedItem();
            refreshDepotInventoryTable();
            System.out.println("Row set to " + rowsDataCount11);
        }
    }

    private Node createDataPage(int pgindex) {

        if (tab_livraison.isSelected()) {
            // pagination.setPageCount((int) (listlivraiz.size() / rowsDataCount));
            try {
                long size = LivraisonDelegate.getCount();// strongDb.findCount(Livraison.class);

                int offset = pgindex * rowsDataCount;
                Long limit = Math.min(offset + rowsDataCount, size);

                List<Stocker> find = StockerDelegate.findStockers(offset, limit.intValue());// strongDb.findAllByAscOrdering(Livraison.class,
                                                                                            // "dateLivr", offset,
                                                                                            // Integer.valueOf(String.valueOf(limit)));
                table_stockage.setItems(FXCollections.observableArrayList(
                        find));
            } catch (Exception e) {
                pagination.setPageCount(pgindex);
                System.out.println("Page suivante non disponible");
            }
            return table_stockage;
        } else if (tab_destock.isSelected()) {
            try {
                long size = DestockerDelegate.getCount();// strongDb.findCount(Destocker.class);
                int offset = pgindex * rowsDataCount1;
                Long limit = Math.min(offset + rowsDataCount1, size);
                List<Destocker> find = DestockerDelegate.findDescSortedByDate(offset, limit.intValue());// strongDb.findAllByDescOrdering(Destocker.class,
                                                                                                        // "dateDestockage",
                                                                                                        // offset,
                                                                                                        // Integer.valueOf(String.valueOf(limit)));
                List<Destocker> filteredFind = new ArrayList<>();
                for(Destocker d : find) {
                    if(d.getProductId() != null && ProduitDelegate.findProduit(d.getProductId().getUid()) != null) {
                        filteredFind.add(d);
                    }
                }
                find = filteredFind;
                table1.setItems(FXCollections.observableArrayList(find));
            } catch (Exception e) {
                pagination1.setPageCount(pgindex);
                System.out.println("Page suivante non disponible");
            }
            return table1;
        } else {
            try {
                List<InventoryItem> source = currentDepotInventoryItems();
                int offset = pgindex * rowsDataCount11;
                int limit = Math.min(offset + rowsDataCount11, source.size());
                if (source.isEmpty()) {
                    table11.setItems(FXCollections.observableArrayList());
                } else {
                    table11.setItems(FXCollections.observableArrayList(source.subList(offset, limit)));
                }
            } catch (java.lang.IllegalArgumentException e) {
                pagination11.setPageCount(pgindex);
                System.out.println("Page suivante non disponible");
            }
            return table11;
        }
    }

    @FXML
    public void refreshLivrByHttp(Event evt) {
        pane_wait_import.setVisible(true);
        refreshLivraisonByHttp();
    }

    @FXML
    public void refreshDestHttp(Event evt) {
        refreshDestockerByHttp();
    }

    private void refreshLivraisonByHttp() {
        Executors.newCachedThreadPool()
                .submit(() -> {
                    try {
                        Response<List<Livraison>> rsts = kazisafe.refreshLivraison().execute();
                        System.out.println("Livraiz resp " + rsts);
                        if (rsts.isSuccessful()) {
                            List<Livraison> livs = rsts.body();
                            for (Livraison liv : livs) {
                                Fournisseur f = liv.getFournId();
                                System.out.println("incoming supplier " + f);

                                Livraison lf = LivraisonDelegate.findLivraison(liv.getUid());
                                if (lf == null) {
                                    Response<Fournisseur> rfs = kazisafe.showSupplier(f.getUid()).execute();
                                    System.out.println("Response fournisseur " + rfs);
                                    if (rfs.isSuccessful()) {
                                        Fournisseur ff = rfs.body();
                                        List<Fournisseur> found = FournisseurDelegate.findByPhone(ff.getPhone());
                                        if (found.isEmpty()) {
                                            FournisseurDelegate.saveFournisseur(ff);
                                        } else {
                                            FournisseurDelegate.updateFournisseur(ff);
                                        }
                                        liv.setFournId(ff);
                                        LivraisonDelegate.saveLivraison(liv);

                                    }
                                    // saveLivraisonForcely(liv, found);

                                } else {
                                    LivraisonDelegate.updateLivraison(liv);
                                }
                                Response<List<Stocker>> repstock = kazisafe.refreshStocksFor(liv.getUid()).execute();
                                if (repstock.isSuccessful()) {
                                    List<Stocker> stoxs = repstock.body();
                                    for (Stocker stox1 : stoxs) {
                                        Produit p = stox1.getProductId();
                                        Produit fpro = ProduitDelegate.findProduit(p.getUid());
                                        if (fpro == null) {
                                            Response<Produit> rpro = kazisafe.refreshProduit(p.getUid()).execute();
                                            if (rpro.isSuccessful()) {
                                                Produit prod = rpro.body();
                                                Category c = prod.getCategoryId();
                                                Category cat = CategoryDelegate.findCategory(c.getUid());
                                                if (cat == null) {
                                                    CategoryDelegate.saveCategory(c);
                                                    prod.setCategoryId(c);
                                                } else {
                                                    prod.setCategoryId(cat);
                                                }
                                                ProduitDelegate.saveProduit(prod);

                                                Response<List<Mesure>> lmr = kazisafe.refreshMesureFor(prod.getUid())
                                                        .execute();
                                                if (lmr.isSuccessful()) {
                                                    List<Mesure> ms = lmr.body();
                                                    for (Mesure m : ms) {
                                                        Mesure mf = MesureDelegate.findMesure(m.getUid());
                                                        if (mf == null) {
                                                            m.setProduitId(prod);
                                                            MesureDelegate.saveMesure(m);
                                                        }
                                                    }
                                                }
                                                stox1.setProductId(prod);
                                            }
                                        } else {
                                            Category c = p.getCategoryId();
                                            System.out.println("Categox stokage 2 " + c);
                                            if (c != null) {
                                                Response<Category> rcat = kazisafe.showCategory(c.getUid()).execute();
                                                if (rcat.isSuccessful()) {
                                                    Category categ = rcat.body();
                                                    Category cat = CategoryDelegate.findCategory(categ.getUid());
                                                    if (cat == null) {
                                                        CategoryDelegate.saveCategory(c);
                                                        p.setCategoryId(c);
                                                    } else {
                                                        p.setCategoryId(cat);
                                                    }
                                                }
                                            } else {
                                                List<Category> lcat = CategoryDelegate.findCategories("Divers");
                                                if (lcat.isEmpty()) {
                                                    Category ca = new Category(DataId.generate());
                                                    ca.setDescritption("Divers");
                                                    CategoryDelegate.saveCategory(ca);
                                                    p.setCategoryId(ca);
                                                } else {
                                                    p.setCategoryId(lcat.get(0));
                                                }
                                            }
                                            ProduitDelegate.updateProduit(p);
                                            stox1.setProductId(p);
                                        }
                                        Mesure mx = stox1.getMesureId();
                                        Mesure fm = MesureDelegate.findMesure(mx.getUid());
                                        if (fm == null || fm.getDescription() == null) {
                                            Response<Mesure> mez = kazisafe.showMesure(mx.getUid()).execute();
                                            System.out.println("Mesure out 2 " + mez);
                                            if (mez.isSuccessful()) {
                                                Mesure m = mez.body();
                                                Mesure mm = new Mesure(m.getUid());
                                                mm.setDescription(m.getDescription());
                                                mm.setProduitId(p);
                                                mm.setQuantContenu(m.getQuantContenu());
                                                Mesure fm2 = MesureDelegate.findMesure(mx.getUid());
                                                if (fm2 == null) {
                                                    MesureDelegate.saveMesure(mm);
                                                } else {
                                                    MesureDelegate.updateMesure(mm);
                                                }
                                                stox1.setMesureId(mm);
                                            }
                                        } else {
                                            stox1.setMesureId(fm);
                                        }
                                        Stocker stk = StockerDelegate.findStocker(stox1.getUid());
                                        stox1.setLivraisId(liv);
                                        if (stk == null) {
                                            Livraison ll = stox1.getLivraisId();
                                            Response<Livraison> rliv = kazisafe.showLivraison(ll.getUid()).execute();
                                            System.out.println("Response livraison " + rliv);
                                            if (rliv.isSuccessful()) {
                                                Livraison l = rliv.body();
                                                Fournisseur fx = l.getFournId();
                                                Response<Fournisseur> rfs = kazisafe.showSupplier(fx.getUid())
                                                        .execute();
                                                System.out.println("Response fournisseur 2 " + rfs);
                                                if (rfs.isSuccessful()) {
                                                    Fournisseur ff = rfs.body();
                                                    Fournisseur found = FournisseurDelegate
                                                            .findFournisseur(ff.getUid());
                                                    if (found == null) {
                                                        found = FournisseurDelegate.saveFournisseur(ff);
                                                    } else {
                                                        found = FournisseurDelegate.updateFournisseur(ff);
                                                    }
                                                    l.setFournId(found);
                                                }
                                                Livraison fl = LivraisonDelegate.findLivraison(l.getUid());
                                                Livraison svl;
                                                if (fl == null) {
                                                    svl = LivraisonDelegate.saveLivraison(l);
                                                } else {
                                                    svl = LivraisonDelegate.updateLivraison(l);
                                                }
                                                stox1.setLivraisId(svl);
                                                StockerDelegate.saveStocker(stox1);
                                            }
                                        } else {
                                            StockerDelegate.updateStocker(stox1);
                                        }
                                    }
                                }
                            }
                            Platform.runLater(() -> {
                                pane_wait_import.setVisible(false);
                            });
                            MainUI.notify(null, "Succes", "Livraisons synchronisees", 5, "info");
                            System.out.println("Rafraishissement livraison termine");
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                        Platform.runLater(() -> {
                            pane_wait_import.setVisible(false);
                        });
                        MainUI.notify(null, "Connexion",
                                "La connection internet n'est pas stable, vous etes en mode offline ", 5, "warn");
                    }

                });
    }

    private void refreshDestockerByHttp() {
        Executors.newCachedThreadPool()
                .submit(() -> {

                    try {
                        Response<List<Destocker>> listdst = kazisafe.refreshDestockers().execute();
                        if (listdst.isSuccessful()) {
                            List<Destocker> destoxs = listdst.body();
                            for (Destocker destox : destoxs) {
                                Produit p = destox.getProductId();
                                Produit fpro = ProduitDelegate.findProduit(p.getUid());
                                if (fpro == null) {
                                    Response<Produit> rpro = kazisafe.refreshProduit(p.getUid()).execute();
                                    if (rpro.isSuccessful()) {
                                        Produit prod = rpro.body();
                                        Category c = prod.getCategoryId();
                                        Category cat = CategoryDelegate.findCategory(c.getUid());
                                        if (cat == null) {
                                            CategoryDelegate.saveCategory(c);
                                        }
                                        ProduitDelegate.saveProduit(prod);
                                        Response<List<Mesure>> lmr = kazisafe.refreshMesureFor(prod.getUid()).execute();
                                        if (lmr.isSuccessful()) {
                                            List<Mesure> ms = lmr.body();
                                            for (Mesure m : ms) {
                                                Mesure mf = MesureDelegate.findMesure(m.getUid());
                                                if (mf == null) {
                                                    m.setProduitId(prod);
                                                    MesureDelegate.saveMesure(m);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    ProduitDelegate.updateProduit(p);
                                    Response<List<Mesure>> lmr = kazisafe.refreshMesureFor(p.getUid()).execute();
                                    if (lmr.isSuccessful()) {
                                        List<Mesure> ms = lmr.body();
                                        for (Mesure m : ms) {
                                            Mesure mf = MesureDelegate.findMesure(m.getUid());
                                            if (mf == null) {
                                                m.setProduitId(p);
                                                MesureDelegate.saveMesure(m);
                                            }
                                        }
                                    }
                                }
                                Destocker dstk = DestockerDelegate.findDestocker(destox.getUid());
                                if (dstk == null) {
                                    DestockerDelegate.saveDestocker(destox);
                                } else {
                                    DestockerDelegate.updateDestocker(destox);
                                }
                            }
                            System.out.println("Destockers enregistrees");
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                });
    }

    private void saveLivraisonForcely(Livraison liv, Fournisseur f) {
        int MAX_RETRY = 5;
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                LivraisonDelegate.saveLivraison(liv);
                break;
            } catch (java.lang.IllegalStateException e) {
                FournisseurDelegate.saveFournisseur(f);
                liv.setFournId(f);
            }
            attempt++;
            try {
                TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, attempt)); // Delai exponentiel
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

    }

    private void saveStockForcely(Livraison liv, Stocker s, Fournisseur f) {
        int MAX_RETRY = 5;
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                StockerDelegate.saveStocker(s);
                break;
            } catch (java.lang.IllegalStateException e) {
                FournisseurDelegate.saveFournisseur(f);
                liv.setFournId(f);
                LivraisonDelegate.saveLivraison(liv);
                s.setLivraisId(liv);
            }
            attempt++;
            try {
                TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, attempt)); // Delai exponentiel
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

    }

    /**
     * Adjust stock based on physical inventory count
     * If physical count > stock in StockDepotAgregate → register a
     * destocker
     * (remove excess)
     * If physical count < stock in StockDepotAgregate → register a stocker
     * (add
     * missing)
     * If equal → leave unchanged
     * 
     * @param product       The product to adjust
     * @param physicalCount The physical count from inventory
     * @param mesure        The measure unit for the count
     * @param region        The region/location
     * @param date          The date of inventory
     * @param observation   Notes about the adjustment
     * @return true if adjustment was made, false if no change needed
     */
    public boolean adjustStockForPhysicalInventory(Produit product, double physicalCount,
            Mesure mesure, String region, LocalDate date, String observation) {

        if (product == null || mesure == null || region == null) {
            MainUI.notify(null, "Erreur", "Paramètres invalides pour l'ajustement d'inventaire", 4, "error");
            return false;
        }

        // Get current stock from StockDepotAgregate aggregate
        StockDepotAgregate currentStock = stockDepotService.findLatestStockDepotAgregate(product.getUid(), region);
        double aggregateCount = currentStock != null ? currentStock.getQuantite() : 0.0;

        // Convert physical count to base measure if needed
        double physicalCountInBase = physicalCount * mesure.getQuantContenu();

        double difference = physicalCountInBase - aggregateCount;
        double tolerance = 0.001; // Small tolerance for floating point comparison

        if (Math.abs(difference) < tolerance) {
            // No adjustment needed - counts match
            MainUI.notify(null, "Info", "Stock déjà correct. Aucun ajustement nécessaire.", 3, "info");
            return false;
        }

        String obs = observation != null ? observation : "Ajustement inventaire physique du " + date.toString();

        if (difference > 0) {
            // Physical count is HIGHER than aggregate
            // We need to DESTOCK (remove) the excess
            Destocker destocker = new Destocker(DataId.generate());
            destocker.setProductId(product);
            destocker.setQuantite(difference / mesure.getQuantContenu()); // Convert back to measure unit
            destocker.setMesureId(mesure);
            destocker.setCoutAchat(currentStock != null ? currentStock.getCoutAchat() : 0.0);
            destocker.setDateDestockage(date.atStartOfDay());
            destocker.setRegion(region);
            destocker.setReference("INV-ADJ-" + System.currentTimeMillis());
            destocker.setLibelle("Ajustement inventaire: excès déstocké");
            destocker.setObservation(obs);
            destocker.setDestination("Ajustement Inventaire");
            destocker.setNumlot("INV-" + date.toString());

            // Save the destocker
            Destocker saved = DestockerDelegate.saveDestocker(destocker);
            if (saved != null) {
                MainUI.notify(null, "Succès",
                        String.format("Excès de %.2f %s déstocké",
                                difference / mesure.getQuantContenu(), mesure.getDescription()),
                        4, "info");
                return true;
            }

        } else {
            // Physical count is LOWER than aggregate
            // We need to STOCK (add) the missing quantity
            Stocker stocker = new Stocker(DataId.generate());
            stocker.setProductId(product);
            stocker.setQuantite(Math.abs(difference) / mesure.getQuantContenu()); // Convert back to measure unit
            stocker.setMesureId(mesure);
            stocker.setCoutAchat(currentStock != null ? currentStock.getCoutAchat() : 0.0);
            stocker.setDateStocker(date.atStartOfDay());
            stocker.setRegion(region);
            stocker.setLocalisation("Ajustement Inventaire");
            stocker.setLibelle("Ajustement inventaire: manquant ajouté");
            stocker.setObservation(obs);
            stocker.setNumlot("INV-" + date.toString());
            stocker.setDateExpir(date.plusYears(1)); // Default 1 year expiry for adjustment
            stocker.setStockAlerte(0);

            // Save the stocker
            Stocker saved = StockerDelegate.saveStocker(stocker);
            if (saved != null) {
                MainUI.notify(null, "Succès",
                        String.format("Manquant de %.2f %s ajouté",
                                Math.abs(difference) / mesure.getQuantContenu(), mesure.getDescription()),
                        4, "info");
                return true;
            }
        }

        return false;
    }

    @FXML
    private void declasserDepotExpiredStock(ActionEvent event) {
        List<InventoryItem> items = currentDepotInventoryItems();
        if (items == null || items.isEmpty()) {
            MainUI.notify(null, "Information", "Aucun lot expiré à déclasser dans la vue courante.", 3, "info");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment déclasser les lots expirés affichés ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                processDeclasserDepot(items);
            }
        });
    }

    private void processDeclasserDepot(List<InventoryItem> items) {
        String reference = "DECL-STK-" + System.currentTimeMillis();
        int savedCount = 0;
        for (InventoryItem item : items) {
            if (item == null || item.getExpiryDate() == null || isStockExpired(item.getExpiryDate()) != -1
                    || item.getQuantRestValue() <= 0) {
                continue;
            }
            Mesure unit = MesureDelegate.findByProduitAndQuant(item.getProduit().getUid(), 1d);
            if (unit == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(item.getProduit().getUid());
                if (mesures == null || mesures.isEmpty()) {
                    continue;
                }
                unit = mesures.get(0);
            }

            Destocker destocker = new Destocker(DataId.generate());
            destocker.setProductId(item.getProduit());
            destocker.setMesureId(unit);
            destocker.setQuantite(item.getQuantRestValue() / (unit.getQuantContenu() == null || unit.getQuantContenu() <= 0 ? 1d : unit.getQuantContenu()));
            destocker.setCoutAchat(item.getCoutAchat());
            destocker.setDateDestockage(LocalDateTime.now());
            destocker.setDestination("Déclassement de stock");
            destocker.setReference(reference);
            destocker.setLibelle("Déclassement de stock expiré");
            destocker.setObservation("Déclassement automatique du lot expiré " + item.getNumlot());
            destocker.setRegion(item.getRegion());
            destocker.setNumlot(item.getNumlot());

            Destocker saved = DestockerDelegate.saveDestocker(destocker);
            if (saved != null) {
                savedCount++;
                lisdestocker.add(0, saved);
                Util.sync(saved, Constants.ACTION_CREATE, Tables.DESTOCKER);
            }
        }

        if (savedCount > 0) {
            MainUI.notify(null, "Succès",
                    savedCount + " lot(s) expiré(s) déclassé(s) avec la référence " + reference, 4, "info");
            loadInv();
        } else {
            MainUI.notify(null, "Information", "Aucun lot expiré n'a été déclassé.", 3, "info");
        }
    }

    /**
     * Get stock summary for a date range from StockDepotAgregate
     * Displays total value of stock
     * 
     * @param startDate Start of period
     * @param endDate   End of period
     * @param region    Optional region filter (null for all regions)
     */
    public void displayStockSummaryForPeriod(LocalDate startDate, LocalDate endDate, String region) {
        StockDepotAgregateService.StockSummary summary = stockDepotService.findByDateRange(startDate, endDate,
                region);

        String regionText = region != null && !region.isEmpty() ? "Région: " + region : "Toutes régions";
        String message = String.format(
                "Période: %s à %s\n%s\n\nNombre de produits: %d\nValeur totale du stock: %.2f USD",
                startDate.toString(), endDate.toString(), regionText,
                summary.getStocks().size(), summary.getTotalValue());

        MainUI.notify(null, "Résumé Stock", message, 6, "info");

        // Refresh inventory display if needed
        if (lisinvent != null && !lisinvent.isEmpty()) {
            refreshInventoryDisplay(summary.getStocks());
        }
    }

    /**
     * Refresh inventory display with StockDepotAgregate data
     */
    private void refreshInventoryDisplay(List<StockDepotAgregate> stockDepotAgregates) {
        // Convert StockDepotAgregate to InventoryItem for display
        lisinvent.clear();
        for (StockDepotAgregate sd : stockDepotAgregates) {
            InventoryItem item = new InventoryItem();
            item.setProduit(sd.getProductId());
            item.setProductId(sd.getProductId().getUid());
            item.setProductName(sd.getProductId().getNomProduit());
            item.setQuantite(sd.getQuantite());
            item.setCoutAchat(sd.getCoutAchat());
            item.setQuantRestValue(sd.getQuantite());
            item.setQuantRest(String.format("%.2f", sd.getQuantite()));
            item.setValeurStock(String.format("%.2f USD", sd.getValeurStock()));
            item.setValeurStockValue(sd.getValeurStock());
            item.setRegion(sd.getRegion());
            item.setDate(sd.getDate());
            item.setNumlot(sd.getNumlot());
            item.setExpiryDate(sd.getDateExpiration());
            item.setDateExpir(sd.getDateExpiration() == null ? "-" : sd.getDateExpiration().format(INVENTORY_DATE_FORMAT));
            lisinvent.add(item);
        }
        applyDepotInventoryFilters();
    }

}
