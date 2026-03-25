/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import static com.endeleya.kazisafex.StoreformController.MAX_RETRY;
import delegates.DestockerDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RecquisitionDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import data.Destocker;
import data.Entreprise;
import data.Fournisseur;
import data.Livraison;
import data.Livraison;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.ProduitHelper;
import data.Recquisition;
import data.Recquisition;
import data.Stocker;
import data.core.KazisafeServiceFactory;
import tools.ComboBoxAutoCompletion;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import tools.Constants;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.LivraisonDelegate;
import delegates.StockerDelegate;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import retrofit2.Response;
import tools.FileUtils;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class RecqController implements Initializable {

    Preferences pref;

    @FXML
    private Pane pricepane;

    @FXML
    private ComboBox<Destocker> cbx_ref_req;
    @FXML
    private TextField tf_prod_req;
    @FXML
    private TextField tf_quant_req;
    @FXML
    private ComboBox<Mesure> cbx_mesure_req;
    @FXML
    private DatePicker dpk_date_req;
    @FXML
    private DatePicker dpk_date_expiry_req;
    @FXML
    private TextField tf_alerte_req;
    @FXML
    private TextField tf_numlot_req, tf_cout_achat_req;
    @FXML
    private ComboBox<Produit> cbx_choose_produit_req;
    @FXML
    private TextField search_produit;
    @FXML
    TilePane tilepn_prices1;
    @FXML
    private Button saveBtn;
    Label txt_equivalentCdf;

    ImageView btn_add_price;

    ResourceBundle bundle;
    ObservableList<Destocker> lsdestocker;
    ObservableList<Recquisition> obllot;
    ObservableList<Produit> lsproduit;
    Livraison chlivraisonf;

    private String action;
    private Recquisition recquisition;

    private Produit choosenPro;
    private Mesure mesureRecq;
    private Mesure mesurePv;
    private Destocker destocker;
    private double taux2change;
    ObservableList<PrixDeVente> prices;
    ObservableList<Livraison> livraizons;
    String role, region;

    private static RecqController instance;
    Kazisafe ksf;
    @FXML
    private ComboBox<Livraison> cbx_choose_livraison;
    @FXML
    private TabPane tabPanelot;
    @FXML
    private Tab lottab;
    @FXML
    private Label txt_somme_ct_lot1;
    @FXML
    private Label txt_somme_global_stk;
    @FXML
    private TableView<Recquisition> t_requisitions;
    @FXML
    private TableColumn<Recquisition, String> col_date_lot;
    @FXML
    private TableColumn<Recquisition, String> col_ref_lot;
    @FXML
    private TableColumn<Recquisition, String> col_produit_lot;
    @FXML
    private TableColumn<Recquisition, String> col_observation_lot;
    @FXML
    private TableColumn<Recquisition, String> col_numero_lot;
    @FXML
    private TableColumn<Recquisition, String> col_quant_lot;
    @FXML
    private TableColumn<Recquisition, Number> col_cout_ach_lot;
    @FXML
    private TableColumn<Recquisition, Number> col_cout_total_lot;
    @FXML
    private TableColumn<Recquisition, String> col_date_expir_lot;
    @FXML
    private Label txt_totalot;
    @FXML
    private Button btn_add_lot;
    @FXML
    private TextField searchlot;
    @FXML
    private ComboBox<Mesure> cbx_choose_mesure_vente;
    @FXML
    private TextField tf_qte_min;
    @FXML
    private TextField tf_qte_max;
    @FXML
    private TextField tf_prix_de_vente;
    @FXML
    private ComboBox<String> cbx_devise_price;
    String ref;
    @FXML
    private Label txt_equivalent_req;
    @FXML
    private Label sumlivraiz;
    @FXML
    private Label txt_ref_livr;
    @FXML
    private TextField tf_obs_req;

    public RecqController() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        instance = this;
    }

    public static RecqController getInstance() {
        if (instance == null) {
            instance = new RecqController();
        }
        return instance;
    }

    String payload;

    public void setup(Entreprise eze, String action, String payload, Livraison liv) {

        this.action = action;
        this.payload = payload;
        lsproduit = FXCollections.observableArrayList(ProduitDelegate.findProduits());
        lsdestocker = FXCollections.observableArrayList(DestockerDelegate.findDestockers());
        livraizons = FXCollections.observableArrayList();
        prices = FXCollections.observableArrayList();
        cbx_choose_livraison.setItems(livraizons);
        if (payload.equalsIgnoreCase("Entrepot")) {
            cbx_ref_req.setDisable(false);
            dpk_date_req.setDisable(false);
            cbx_choose_produit_req.setDisable(true);
            cbx_choose_livraison.setDisable(true);
//            if (cbx_choose_produit_req != null) {
//                cbx_choose_produit_req.setVisible(true);
//            }
//            if (search_produit != null) {
//                search_produit.setVisible(true);
//            }

        } else if (payload.equalsIgnoreCase("Achat")) {
            cbx_ref_req.setDisable(true);
            dpk_date_req.setDisable(true);
            cbx_choose_produit_req.setDisable(false);
            cbx_choose_livraison.setDisable(false);
            livraizons.addAll(LivraisonDelegate.findLivraisons());
            setChoosenDelivery(liv);
//            ObservableList<Destocker> filtered = FXCollections.observableArrayList();
//            for (Destocker d : lsdestocker) {
//                if (ref.equals(d.getReference())) {
//                    filtered.add(d);
//                }
//            }
//            cbx_ref_req.setItems(filter(filtered, lsproduit, region));
        }
        ksf = KazisafeServiceFactory.createService(pref.get("token", null));

        new ComboBoxAutoCompletion<>(cbx_ref_req);
        new ComboBoxAutoCompletion<>(cbx_choose_livraison);
        if (cbx_choose_produit_req != null) {
            cbx_choose_produit_req.setItems(lsproduit);
            new ComboBoxAutoCompletion<>(cbx_choose_produit_req);

        }
        if (this.action.equals(Constants.ACTION_UPDATE)) {
            saveBtn.setText(this.bundle.getString("xbtn.update"));
        }
    }

    private ObservableList<Destocker> filter(List<Destocker> lds, List<Produit> produit, String region) {
        ObservableList<Destocker> fxl = FXCollections.observableArrayList();
        for (Destocker d : lds) {
            if (d.getObservation().equalsIgnoreCase("Déclassement de stock")) {
                continue;
            }
            if (!role.equals(Role.Trader.name()) && !role.contains(Role.ALL_ACCESS.name())) {
                if (d.getDestination().equals(region)) {
                    Produit p = Util.findProduit(produit, d.getProductId().getUid());
                    d.setProductId(p);
                    fxl.add(d);
                }
            } else {
                Produit p = Util.findProduit(produit, d.getProductId().getUid());
                d.setProductId(p);
                fxl.add(d);
            }

        }
        return fxl;
    }

    public void setRecq(Recquisition req) {
        if (req != null) {
            if (action == null) {
                return;
            }
            recquisition = req;
            if (this.action.equals(Constants.ACTION_UPDATE)) {
                saveBtn.setText(this.bundle.getString("xbtn.update"));
                mesureRecq = recquisition.getMesureId();
                choosenPro = recquisition.getProductId();
                cbx_choose_produit_req.getSelectionModel().select(choosenPro);
                List<Livraison> livs = LivraisonDelegate.findByRef(recquisition.getReference());
                if (!livs.isEmpty()) {
                    Livraison l = livs.get(0);
                    cbx_choose_livraison.getSelectionModel().select(l);
                }
            }
            tf_alerte_req.setText(String.valueOf(req.getStockAlert()));
            tf_quant_req.setText(String.valueOf(req.getQuantite()));
            LocalDate dat = req.getDateExpiry();
            if (dat != null) {
                DateTimeFormatter formater = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
                dpk_date_expiry_req.setValue(dat);
            }
            obllot.add(req);
            prices = FXCollections.observableArrayList();

        }
    }

    public void config() {
        configtablot();
        cbx_ref_req.setConverter(new StringConverter<Destocker>() {
            @Override
            public String toString(Destocker object) {
                if (object == null) {
                    return null;
                }
                Produit p = ProduitDelegate.findProduit(object.getProductId().getUid());
                String numlot = object.getNumlot();
                if (numlot == null) {
                    object.setNumlot("Lot:" + object.getDateDestockage().toString());
                }
                return object.getDestination() + " " + p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + object.getNumlot() + " " + object.getDateDestockage().toString();
            }

            @Override
            public Destocker fromString(String string) {
                return cbx_ref_req.getItems()
                        .stream()
                        .filter(v -> (v.getDestination() + " " + v.getProductId().getNomProduit() + " " + v.getProductId().getMarque() + " " + v.getProductId().getModele()
                        + " " + v.getNumlot() + " " + v.getDateDestockage().toString())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(destocker);
            }
        });
        cbx_mesure_req.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure_req.getItems()
                        .stream()
                        .filter(v -> (v.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_choose_mesure_vente.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_choose_mesure_vente.getItems()
                        .stream()
                        .filter(f -> (f.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_choose_livraison.setConverter(new StringConverter<Livraison>() {
            @Override
            public String toString(Livraison object) {
                return object == null ? null : object.getFournId().getNomFourn() + ", " + object.getNumPiece() + " " + object.getDateLivr().toString();
            }

            @Override
            public Livraison fromString(String string) {
                return cbx_choose_livraison.getItems()
                        .stream()
                        .filter(f -> (f.getFournId().getNomFourn() + ", " + f.getNumPiece() + " " + f.getDateLivr().toString())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_devise_price.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_devise_price.getSelectionModel().selectFirst();
        cbx_ref_req.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Destocker> observable, Destocker oldValue, Destocker newValue) -> {
            destocker = newValue;
            if (this.action.equals(Constants.ACTION_UPDATE)) {
                choosenPro = recquisition.getProductId();
                tf_prod_req.setText(choosenPro.getNomProduit() + " " + choosenPro.getMarque() + " " + choosenPro.getModele() + " " + (choosenPro.getTaille() == null ? "" : choosenPro.getTaille()) + " " + (choosenPro.getCouleur() == null ? "" : choosenPro.getCouleur()));
                List<Mesure> lms = MesureDelegate.findMesureByProduit(choosenPro.getUid());
                cbx_mesure_req.setItems(FXCollections.observableArrayList(lms));
                Mesure m = MesureDelegate.findMesure(recquisition.getMesureId().getUid());
                cbx_mesure_req.getSelectionModel().select(m);
                cbx_choose_mesure_vente.setItems(FXCollections.observableArrayList(lms));
                tf_quant_req.setText(String.valueOf(recquisition.getQuantite()));
                if (tf_numlot_req != null) {
                    tf_numlot_req.setText(recquisition.getNumlot());
                }
                if (tf_cout_achat_req != null) {
                    tf_cout_achat_req.setText(Double.toString(recquisition.getCoutAchat()));
                }
                LocalDate exp = recquisition.getDateExpiry();
                LocalDate ldate = exp == null ? null : exp;
                dpk_date_expiry_req.setValue(ldate);

            } else if (this.action.equals(Constants.ACTION_CREATE)) {
                if (destocker == null) {
                    MainUI.notify(null, "Erreur", "Ce produit est au stock depot, et n'a jamais ete destocke! Destockez-le d'abord", 5, "error");
                    return;
                }
                choosenPro = destocker.getProductId();
                tf_prod_req.setText(choosenPro.getNomProduit() + " " + choosenPro.getMarque() + " " + choosenPro.getModele() + " " + (choosenPro.getTaille() == null ? "" : choosenPro.getTaille()) + " " + (choosenPro.getCouleur() == null ? "" : choosenPro.getCouleur()));
                List<Mesure> lms = MesureDelegate.findMesureByProduit(choosenPro.getUid());
                cbx_mesure_req.setItems(FXCollections.observableArrayList(lms));
                cbx_choose_mesure_vente.setItems(FXCollections.observableArrayList(lms));
                tf_quant_req.setText(String.valueOf(destocker.getQuantite()));
                if (tf_numlot_req != null) {
                    tf_numlot_req.setText(destocker.getNumlot());
                }
                if (tf_cout_achat_req != null) {
                    tf_cout_achat_req.setText(String.valueOf(destocker.getCoutAchat()));
                }

            }
        });
        cbx_mesure_req.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            mesureRecq = newValue;
        });
        cbx_choose_mesure_vente.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            mesurePv = newValue;
        });
        
        cbx_choose_produit_req.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null : object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_choose_produit_req.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_choose_produit_req.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) -> {
            choosenPro = newValue;
            if (choosenPro == null) {
                return;
            }
            List<Mesure> mzs = MesureDelegate.findMesureByProduit(choosenPro.getUid());
            cbx_mesure_req.setItems(FXCollections.observableArrayList(mzs));
            cbx_mesure_req.getSelectionModel().selectFirst();
            cbx_choose_mesure_vente.setItems(FXCollections.observableArrayList(mzs));
            cbx_choose_mesure_vente.getSelectionModel().selectFirst();
            List<Recquisition> proxt = RecquisitionDelegate.findDescSortedByDateForProduit(choosenPro.getUid());
            if (!proxt.isEmpty()) {
                Recquisition fromlast = proxt.get(0);
                if (lottab.isSelected()) {
                    tf_cout_achat_req.setText(String.valueOf(fromlast.getCoutAchat()));
                    tf_obs_req.setText(fromlast.getObservation());
                    dpk_date_expiry_req.setValue(fromlast.getDateExpiry());
                    tf_numlot_req.setText(fromlast.getNumlot());
                    tf_alerte_req.setText(String.valueOf(fromlast.getStockAlert()));
                    List<Mesure> lms = MesureDelegate.findMesureByProduit(choosenPro.getUid());
                    cbx_mesure_req.setItems(FXCollections.observableArrayList(lms));
                    cbx_choose_mesure_vente.setItems(FXCollections.observableArrayList(lms));
                    if (action.equals(Constants.ACTION_CREATE)) {
                        recquisition = null;
                    } else {
                        recquisition = fromlast;
                    }
                }
            }
        });

        tf_prix_de_vente.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.isEmpty()) {
                    return;
                }
                if (cbx_devise_price.getValue().equals("USD")) {
                    //eq en fc
                    txt_equivalent_req.setText(BigDecimal.valueOf((taux2change * Double.parseDouble(tf_prix_de_vente.getText()))).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " Fc");
                } else {
                    txt_equivalent_req.setText(BigDecimal.valueOf(Double.parseDouble(tf_prix_de_vente.getText()) / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " $ us");
                    //eq en usd
                }
            }
        });
        searchlot.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isBlank()) {
                    ObservableList<Recquisition> reqs = FXCollections.observableArrayList();
                    for (Recquisition r : obllot) {
                        Produit p = r.getProductId();
                        String predicat = p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + r.getNumlot() + " " + r.getObservation() + " " + r.getReference();
                        if (predicat.toUpperCase().contains(newValue.toUpperCase())) {
                            reqs.add(r);
                        }
                    }
                    t_requisitions.setItems(reqs);
                } else {
                    t_requisitions.setItems(obllot);
                }
            }
        });
        tf_quant_req.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (tf_cout_achat_req.getText().isEmpty() || newValue == null) {
                    return;
                }
                try {
                    double cu = Double.parseDouble(tf_cout_achat_req.getText());
                    double qt = Double.parseDouble(newValue);
                    txt_somme_ct_lot1.setText("Total : " + (qt * cu));
                } catch (NumberFormatException e) {

                }
            }
        });
        tf_cout_achat_req.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (tf_quant_req.getText().isEmpty() || newValue == null) {
                    return;
                }
                try {
                    double qt = Double.parseDouble(tf_quant_req.getText());
                    double cu = Double.parseDouble(newValue);
                    txt_somme_ct_lot1.setText("Total : " + (qt * cu));
                } catch (NumberFormatException e) {

                }
            }
        });
    }

    private void configtablot() {
        obllot = FXCollections.observableArrayList();
        t_requisitions.setItems(obllot);
        col_produit_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Produit px = param.getValue().getProductId();
            Produit pr = ProduitDelegate.findProduit(px.getUid());
            return new SimpleStringProperty(pr.getNomProduit() + " " + pr.getMarque() + " " + pr.getModele() + " " + pr.getCodebar());
        });
        col_date_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            LocalDateTime date = param.getValue().getDate();
            return new SimpleStringProperty(date == null ? "" : date.toString());
        });
        col_date_expir_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            LocalDate exp = param.getValue().getDateExpiry();
            return new SimpleStringProperty(exp == null ? "Non périssable" : exp.toString());
        });
        col_ref_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            return new SimpleStringProperty(param.getValue().getReference());
        });
        col_observation_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            return new SimpleStringProperty(param.getValue().getObservation());
        });
        col_numero_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            return new SimpleStringProperty(param.getValue().getNumlot());
        });
        col_quant_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, String> param) -> {
            Mesure mz = param.getValue().getMesureId();
            Produit px = param.getValue().getProductId();
            Mesure mx = MesureDelegate.findMesure(mz.getUid());
            if (mx == null) {
                List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(px.getUid());
                //nsmesure.findAllFieldAsc("produitId.uid", px.getUid(), "quantContenu");
                mx = mesures.get(0);
            }
            return new SimpleStringProperty(param.getValue().getQuantite() + " " + (mx == null ? "" : mx.getDescription()));
        });
        col_cout_ach_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getCoutAchat());
        });
        col_cout_total_lot.setCellValueFactory((TableColumn.CellDataFeatures<Recquisition, Number> param) -> {
            Recquisition l = param.getValue();
            double p = l.getQuantite() * l.getCoutAchat();
            return new SimpleDoubleProperty(p);
        });
        ContextMenu contM = new ContextMenu();
        MenuItem md = new MenuItem(bundle.getString("delete"));
        MenuItem pxs = new MenuItem("Gerer les prix de vente");
        contM.getItems().add(md);
        contM.getItems().add(pxs);
        t_requisitions.setContextMenu(contM);
        md.setOnAction((ActionEvent event) -> {
            Recquisition lot = t_requisitions.getSelectionModel().getSelectedItem();
            obllot.remove(lot);
            List<PrixDeVente> prs = PrixDeVenteDelegate.findPricesForRecq(lot.getUid());
            for (PrixDeVente pv : prs) {
                PrixDeVenteDelegate.deletePrixDeVente(pv);
            }
            RecquisitionDelegate.deleteRecquisition(lot);
            txt_totalot.setText(obllot.size() + " Lot(s)");
            txt_somme_global_stk.setText("Totaux : " + obllot.stream().mapToDouble(r -> r.getCoutAchat() * r.getQuantite()).sum());

        });
        pxs.setOnAction((ActionEvent event) -> {
            Recquisition lot = t_requisitions.getSelectionModel().getSelectedItem();
            pricepane.setVisible(true);
            prices.clear();
            tilepn_prices1.getChildren().removeAll();
            List<PrixDeVente> prs = PrixDeVenteDelegate.findPricesForRecq(lot.getUid());
            for (PrixDeVente pv : prs) {
                addPrice(pv, tilepn_prices1);
            }
        });

    }

    @FXML
    public void applySamePrice(Event evt) {
        if (destocker != null) {
            Produit prod = destocker.getProductId();
            if (prod != null) {
                List<PrixDeVente> prices = RecquisitionDelegate.findLastPrices(prod.getUid());
                for (PrixDeVente pv : prices) {
                    addPrice(pv, tilepn_prices1);
                }
            }
        }
    }

    @FXML
    public void createRecqusition(ActionEvent evt) {
        if (action.equals(tools.Constants.ACTION_UPDATE)) {
            updateRecquisition();
            recquisition = null;
            MainUI.notify(null, "Succes", "Modifié avec succès.", 4, "info");
            close(evt);
        } else {
            MainUI.notify(null, "Succes", "Enregistré avec succès.", 4, "info");
            close(evt);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        MainUI.setPattern(dpk_date_req);
        dpk_date_req.setValue(LocalDate.now());
        MainUI.setPattern(dpk_date_expiry_req);
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux2change = pref.getDouble("taux2change", 2000);
        role = pref.get("priv", null);
        region = pref.get("region", "...");
        pricepane.setVisible(false);
        config();
        // TODO
    }

    @FXML
    private void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
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
    CountDownLatch cx = new CountDownLatch(1);

    private void saveRecqusitionByHttp(Recquisition req) {

        Executors.newSingleThreadExecutor().submit(() -> {
            int attempt = 0;
            while (attempt < MAX_RETRY) {
                try {
                    if (trySaveRecquis(req)) {
                        System.out.println("Recquisition enregistré au serveur.");
                        cx.countDown();
                        break;
                    } else {
                        Produit produit = ProduitDelegate.findProduit(req.getProductId().getUid());
                        List<Mesure> mesures = MesureDelegate.findMesureByProduit(produit.getUid());
                        sendProduitIfNotExist(produit, mesures);
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'enregistrement du stock " + e.getMessage());
                }
                attempt++;
                try {
                    TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, attempt)); // Delai exponentiel
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

    }

    private void savePriceByHttp(PrixDeVente pv) {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    try {
                        cx.await();
                        int atempt = 0;
                        while (atempt < MAX_RETRY) {
                            try {
                                int code = pricefy(pv);
                                if (code == 200) {
                                    System.out.println("Price saved");
                                    break;
                                }
                                System.out.println("Price coded " + code);
                            } catch (IOException ex) {
                                Logger.getLogger(RecqController.class.getName()).log(Level.SEVERE, null, ex);
                                break;
                            }
                            atempt++;
                            try {
                                TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, atempt)); // Delai exponentiel
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RecqController.class.getName()).log(Level.SEVERE, null, ex);

                    }
                });
    }

    private int pricefy(PrixDeVente pv) throws IOException {
        Response<PrixDeVente> exe = ksf.savePrice(pv.getUid(),
                String.valueOf(pv.getQmin()),
                String.valueOf(pv.getQmax()),
                String.valueOf(pv.getPrixUnitaire()),
                pv.getDevise(), pv.getMesureId().getUid(),
                pv.getRecquisitionId().getUid(), "ok").execute();
        return exe.code();
    }

    private boolean trySaveRecquis(Recquisition req) throws IOException {
        Response<Recquisition> rool = ksf.syncRecquisition(req.getUid(), Constants.DATE_HEURE_FORMAT.format(req.getDate()), req.getObservation(),
                req.getReference(), Double.toString(req.getQuantite()), Double.toString(req.getCoutAchat()),
                req.getDateExpiry().toString(), Double.toString(req.getStockAlert()),
                req.getMesureId().getUid(), req.getProductId().getUid(), req.getRegion(), req.getNumlot()).execute();
        return rool.code() == 200;
    }

    private void sendProduitIfNotExist(Produit produit, List<Mesure> mesures) {
        byte[] imageBytes = produit.getImage();
        if (imageBytes == null) {
            imageBytes = loadDefaultImage();
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        saveProduitByHttp(produit, base64Image, mesures);
    }

    private byte[] loadDefaultImage() {
        try (InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png")) {
            return FileUtils.readAllBytes(is);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image par défaut" + e.getMessage());
            return new byte[0];
        }
    }

    private void saveProduitByHttp(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = createProduitHelper(produit, base64Image, mesures);
        try {
            Response<Produit> response = ksf.saveLite(produitHelper).execute();
            if (response.isSuccessful()) {
                System.out.println("Save synchrone Produit " + response.code());
            } else {
                System.err.println("Erreur lors de l'enregistrement du produit : " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement du produit" + e.getMessage());
        }
    }

    private ProduitHelper createProduitHelper(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = new ProduitHelper();
        produitHelper.setUid(produit.getUid());
        produitHelper.setCategoryId(produit.getCategoryId().getUid());
        produitHelper.setCodebar(produit.getCodebar());
        produitHelper.setCouleur(produit.getCouleur());
        produitHelper.setMarque(produit.getMarque());
        produitHelper.setModele(produit.getModele());
        produitHelper.setNomProduit(produit.getNomProduit());
        produitHelper.setImage("data:image/jpeg;base64," + base64Image);
        produitHelper.setTaille(produit.getTaille());
        produitHelper.setMethodeInventaire(produit.getMethodeInventaire());
        produitHelper.setMesureList(mesures);
        return produitHelper;
    }

    private void appendPrice(Recquisition savdr) {
        if (tf_qte_min.getText().isEmpty()
                || tf_qte_max.getText().isEmpty()
                || tf_prix_de_vente.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez completer tout les champs relatifs au prix de vente", 5, "error");
            return;
        }
//         String ctach = (lottab.isSelected() ? tf_cout_achlot.getText() : tf_cout_unitr_stk.getText());
        if (tf_cout_achat_req.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez completer le cout d'achat", 5, "error");
            return;
        }
        Mesure stm = cbx_mesure_req.getValue();
        mesurePv = cbx_choose_mesure_vente.getValue();
        if (mesurePv == null) {
            MainUI.notify(null, "Erreur", "Veuillez selectionner une mesure puis continuer", 5, "error");
            return;
        }
        double raps = stm.getQuantContenu() / mesurePv.getQuantContenu();
        double ppcd = Double.parseDouble(tf_cout_achat_req.getText());
        double caumv = BigDecimal.valueOf(ppcd / raps).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        double pvu = Double.parseDouble(tf_prix_de_vente.getText());
        if (caumv >= pvu) {
            MainUI.notify(null, bundle.getString("warning"), String.format(bundle.getString("xlowerprice"), caumv), 5, "warning");
            return;
        }
        PrixDeVente pv = got == null ? new PrixDeVente(DataId.generate()) : got;
        pv.setDevise(cbx_devise_price.getValue());
        pv.setPrixUnitaire(pvu);
        pv.setQmin(Double.parseDouble(tf_qte_min.getText()));
        pv.setQmax(Double.parseDouble(tf_qte_max.getText()));
        pv.setMesureId(mesurePv);
        pv.setRecquisitionId(savdr == null ? recquisition : savdr);
        if (findPrix(prices, pv) == null) {
            addPrice(pv, tilepn_prices1);
            got = null;
        } else {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("xpriceinterval"), 3, "error");
        }

    }
    int priceindex;
    PrixDeVente got;

    private void addPrice(PrixDeVente pv, TilePane tilep) {
        ContextMenu contM = new ContextMenu();
        MenuItem mi = new MenuItem("Supprimer");
        MenuItem mi2 = new MenuItem("Modifier");
        contM.getItems().add(mi2);
        contM.getItems().add(mi);
        Mesure mzr = MesureDelegate.findMesure(pv.getMesureId().getUid());
        String m = mzr.getDescription();
        String price = pv.getQmin() + m + " à " + pv.getQmax() + m + " pour " + pv.getPrixUnitaire() + " " + pv.getDevise();
        Label l = new Label();
        l.setContextMenu(contM);
        l.setPrefWidth(250);
        l.setPrefHeight(14);
        l.setId(pv.getUid());
        l.setTextAlignment(TextAlignment.CENTER);
        l.setTextFill(Color.rgb(255, 255, 255));
        l.setBackground(new Background(new BackgroundFill(Color.rgb(0x7, 0x7, 0xf, 0.3), new CornerRadii(5.0), new Insets(-5.0))));
        l.setPadding(new Insets(4, 4, 4, 4));
        l.setText(price);
        l.setTooltip(new Tooltip(price));
        tilep.getChildren().add(l);
        prices.add(pv);
        PrixDeVente pvx = PrixDeVenteDelegate.findPrixDeVente(pv.getUid());
        if (pvx != null) {
            PrixDeVenteDelegate.updatePrixDeVente(pv);
        } else {
            PrixDeVenteDelegate.savePrixDeVente(pv);
        }
        savePriceByHttp(pv);
        MainUI.notify(null, "Succes", "Prix ajouté avec succès.", 2, "info");
        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int ar = tilep.getChildren().indexOf(l);
                if (prices.isEmpty()) {
                    Node n = tilep.getChildren().get(ar);
                    tilep.getChildren().remove(ar);
                    tilep.getChildren().removeAll(n);
                    return;
                }
                PrixDeVente removed = prices.remove(ar);
                tilep.getChildren().remove(l);
                Node n = tilep.getChildren().get(ar);
                prices.remove(ar);
                tilep.getChildren().remove(ar);
                tilep.getChildren().removeAll(n);
                Recquisition r = removed.getRecquisitionId();
                if (r.equals(recquisition)) {
                    PrixDeVenteDelegate.deletePrixDeVente(removed);
                }
            }
        });
        mi2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                priceindex = tilep.getChildren().indexOf(l);
                Node n = tilep.getChildren().get(priceindex);
                got = prices.get(priceindex);
                tf_qte_min.setText(String.valueOf(got.getQmin()));
                tf_qte_max.setText(String.valueOf(got.getQmax()));
                tf_prix_de_vente.setText(String.valueOf(got.getPrixUnitaire()));
                cbx_choose_mesure_vente
                        .setItems(FXCollections.observableArrayList(MesureDelegate.findMesureByProduit(recquisition.getProductId().getUid())));
                cbx_choose_mesure_vente.getSelectionModel().select(got.getMesureId());
                cbx_devise_price.setValue(got.getDevise());
                prices.remove(priceindex);
                tilep.getChildren().remove(priceindex);
                tilep.getChildren().removeAll(n);

            }
        });

    }

    private PrixDeVente findPrix(List<PrixDeVente> lpv, PrixDeVente p) {
        for (PrixDeVente pv : lpv) {
            if (Objects.equals(pv.getQmin(), p.getQmin()) && Objects.equals(pv.getQmax(), p.getQmax())
                    && pv.getMesureId().getUid().equals(p.getMesureId().getUid())) {
                return pv;
            }
            if (Objects.equals(pv.getQmax(), p.getQmin())
                    && pv.getMesureId().getUid().equals(p.getMesureId().getUid())) {
                return pv;
            }
        }
        return null;
    }

    @FXML
    private void createNewProductIfnotExist(ActionEvent e) {
        MainuiController.getInstance().switchToProduct(e);
        MainUI.floatDialog(tools.Constants.PRODUCT_DLG, 600, 790, pref.get("token", ""), ksf, null, null);
    }

    @FXML
    private void addLot(ActionEvent event) {
        if (addRecquisition(action)) {
            pricepane.setVisible(true);
        }
    }

    @FXML
    private void appendPrix(Event e) {
        if (recquisition != null) {
            appendPrice(recquisition);
        }
    }

    @FXML
    private void clearAllprices(Event e) {
        if (!prices.isEmpty()) {
            for (PrixDeVente pv : prices) {
                PrixDeVenteDelegate.deletePrixDeVente(pv);
            }
            prices.clear();
            tilepn_prices1.getChildren().clear();
        }
    }

    public void setChoosenDelivery(Livraison livraison) {
        if (livraison == null) {
            return;
        }
        this.chlivraisonf = livraison;
        cbx_choose_livraison.getSelectionModel().select(chlivraisonf);
        this.ref = chlivraisonf.getReference();
        txt_ref_livr.setText(ref);
        sumlivraiz.setText("Total : " + chlivraisonf.getPayed() + " USD");
    }

    @FXML
    private void openNunua(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://nunua.markets"));
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(StoreformController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void closeProperly(Event e) {
        MainUI.notify(null, "Succes", "Les prix sont ajoutés avec succès.", 2, "info");
        List<PrixDeVente> pxs = PrixDeVenteDelegate.findPricesForRecq(recquisition.getUid());
        checkAllPricesIfNotExist(prices, pxs);
        closeFloatingPane(e);
    }

    private PrixDeVente checkPrice(List<PrixDeVente> tosaves, String uid) {
        for (PrixDeVente tosave : tosaves) {
            if (tosave.getUid().equals(uid)) {
                return tosave;
            }
        }
        return null;
    }

    private void checkAllPricesIfNotExist(List<PrixDeVente> tosaves, List<PrixDeVente> savd) {
        for (PrixDeVente tosave : tosaves) {
            PrixDeVente pv = checkPrice(savd, tosave.getUid());
            if (pv == null) {
                tosave.setRecquisitionId(recquisition);
                PrixDeVenteDelegate.savePrixDeVente(tosave);
            }
        }
    }

    @FXML
    public void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);

    }

    private void updateRecquisition() {
        recquisition.setMesureId(mesureRecq);
        recquisition.setRegion(region);
        recquisition.setDate(dpk_date_req.getValue().atStartOfDay());
        if (dpk_date_expiry_req.getValue() != null) {
            recquisition.setDateExpiry(dpk_date_expiry_req.getValue());
        }
        recquisition.setProductId(choosenPro);
        recquisition.setQuantite(Double.parseDouble(tf_quant_req.getText()));
        recquisition.setStockAlert(Double.valueOf(tf_alerte_req.getText()));

        if (role.equals(Role.Trader.name()) || role.equals(Role.Manager.name()) || role.equals(Role.Magazinner.name()) || role.contains(Role.ALL_ACCESS.name())) {
            Recquisition req = RecquisitionDelegate.updateRecquisition(recquisition);
            saveRecqusitionByHttp(req);
        } else {
            MainUI.notify(null, "Echec!", "La récquisition n'a pas été modifiée;\nVous n'avez pas les privilèges réquis ", 3, "error");
        }

    }

    private boolean addRecquisition(String action) {
        boolean ok = false;
        if (tf_alerte_req.getText().isEmpty()
                || tf_quant_req.getText().isEmpty() || dpk_date_req.getValue() == null || mesureRecq == null) {
            MainUI.notify(null, "Erreur", "Veuillez completez tout les elements necessaire y compris les prix de vente", 4, "error");
            return false;
        }
        if (action.equals(tools.Constants.ACTION_CREATE)) {
            recquisition = new Recquisition(DataId.generate());
            recquisition.setMesureId(mesureRecq);
            recquisition.setRegion(region);
            recquisition.setNumlot(tf_numlot_req.getText());
            if (payload.equalsIgnoreCase("Achat")) {
                recquisition.setCoutAchat(Double.parseDouble(tf_cout_achat_req.getText()));
                recquisition.setReference(chlivraisonf.getReference());
                recquisition.setObservation("Achat de Facture N : " + chlivraisonf.getNumPiece());
            } else {
                recquisition.setNumlot(destocker.getNumlot());
                recquisition.setCoutAchat(destocker.getCoutAchat());
                recquisition.setReference(destocker.getReference());
                recquisition.setObservation(destocker.getObservation());
            }
            recquisition.setDate(dpk_date_req.getValue().atStartOfDay());
            if (dpk_date_expiry_req.getValue() != null) {
                recquisition.setDateExpiry(dpk_date_expiry_req.getValue());
            }
            recquisition.setProductId(choosenPro);
            recquisition.setQuantite(Double.parseDouble(tf_quant_req.getText()));
            recquisition.setStockAlert(Double.valueOf(tf_alerte_req.getText()));
            Recquisition req;
            if (findRecquisition(recquisition.getUid()) == null) {
                req = RecquisitionDelegate.saveRecquisition(recquisition);
                obllot.add(req);
            } else {
                int index = obllot.indexOf(recquisition);
                req = RecquisitionDelegate.updateRecquisition(recquisition);
                obllot.set(index, req);
            }
            Mesure m = req.getMesureId();
            double cau = req.getCoutAchat() / m.getQuantContenu();
            RecquisitionDelegate.rectifyStock(req.getProductId(), LocalDate.now(), LocalDate.now(), region, cau);
            txt_totalot.setText(obllot.size() + " Lot(s)");
            saveRecqusitionByHttp(req);
            ok = true;
        }
        txt_somme_global_stk.setText("Totaux : " + obllot.stream().mapToDouble(r -> r.getCoutAchat() * r.getQuantite()).sum());
        return ok;
    }

    private Recquisition findRecquisition(String uid) {
        for (Recquisition ol : obllot) {
            if (ol.getUid().equals(uid)) {
                return ol;
            }
        }
        return null;
    }

}
