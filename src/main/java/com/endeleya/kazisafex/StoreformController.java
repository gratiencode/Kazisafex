/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.DestockerDelegate;
import delegates.FournisseurDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
import data.core.KazisafeServiceFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import data.network.Kazisafe;
import data.Destocker;
import data.Entreprise;
import data.Fournisseur;
import data.Livraison;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.ComboBoxAutoCompletion;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class StoreformController implements Initializable {

    @FXML
    private Label txt_somme_global_stk;
    @FXML
    private Label txt_ref_livr, txt_totalot;

    @FXML
    private DatePicker dpk_expir_lot;
    @FXML
    private DatePicker dpk_date_expir_stk;
    @FXML
    private ComboBox<Produit> cbx_choose_produit_stk;
    @FXML
    private ComboBox<Mesure> cbx_choose_mesurelot;
    @FXML
    private ComboBox<Mesure> cbx_choose_mesure_stk;
    @FXML
    private ComboBox<Livraison> cbx_choose_livraison;
    @FXML
    ComboBox<Mesure> cbx_choose_mesure_vente;
    @FXML
    ComboBox<String> cbx_devise_price;

    @FXML
    private TextField tf_quantite_stk;
    @FXML
    private TextField tf_cout_unitr_stk;
    @FXML
    private Label txt_somme_ct_stk, txt_somme_ct_lot1;
    @FXML
    Tab lottab, standtab;
    @FXML
    TabPane tabPanelot;
    @FXML
    private ImageView img_btn_apply_price;
    @FXML
    private Label txt_count_stk;
    @FXML
    private Label sumlivraiz;
    @FXML
    ComboBox<String> cbx_regions;

    @FXML
    TableView<Stocker> t_stocker;
    @FXML
    private TableColumn<Stocker, String> col_quant_lot;
    @FXML
    private TableColumn<Stocker, Number> col_cout_ach_lot;
    @FXML
    private TableColumn<Stocker, String> col_numero_lot;
    @FXML
    private TableColumn<Stocker, Number> col_cout_total_lot;
    @FXML
    private TableColumn<Stocker, String> col_ref_lot;
    @FXML
    private TableColumn<Stocker, String> col_date_lot;
    @FXML
    private TableColumn<Stocker, String> col_localisation_lot;
    @FXML
    private TableColumn<Stocker, String> col_produit_lot;
    @FXML
    private TableColumn<Stocker, String> col_date_expir_lot;

    @FXML
    private TextField tf_localisation_stk;
    @FXML
    private TextField tf_stock_alerte_stk, tf_stock_allot;
    @FXML
    private TextField tf_numlot;
    @FXML
    private TextField tf_quant_lot;
    @FXML
    private TextField tf_localisalot;
    @FXML
    private TextField tf_cout_achlot;
    @FXML
    private Pane pricepane;
    @FXML
    Label txt_equivalentCdf;

    @FXML
    ImageView btn_add_price;

    @FXML
    private Button btn_add_lot;

    ObservableList<Produit> lisproduit;
    ObservableList<Livraison> lisvrezon;
    ObservableList<Fournisseur> lisfournisseur;
    ObservableList<Mesure> lismesure;
    ObservableList<Fournisseur> cbxFssearch;
    ObservableList<Stocker> obllot;
    ObservableList<PrixDeVente> prices;
    ObservableList<String> regions;
    List<Stocker> stk_perm;
    List<Destocker> lisdestocker;
    List<Recquisition> listrecquis;
    ResourceBundle bundle;
    String lotaction = Constants.ACTION_CREATE;
    Destocker destock;
    Recquisition recq;
    Stocker choosenStock;
    Produit choosenPro;
    Recquisition choosenreq;
    Destocker choosendestock;
    Mesure choosenM;
    String action = Constants.ACTION_CREATE, token;
    double cglobal = 0;
    double coutLigne = 0;
    Livraison chlivraisonf;
    Preferences pref;
    double taux, toreceive;
    String region;
    String role;
    Kazisafe ksf;
    @FXML
    TextField tf_qte_min;
    @FXML
    TextField tf_qte_max;
    @FXML
    TextField tf_prix_de_vente;
    @FXML
    TilePane tilepn_prices1;

    private static StoreformController instance;

    public StoreformController() {
        instance = this;
    }

    public static StoreformController getInstance() {
        return instance;
    }

    Set<Stocker> loxs;

    Entreprise entreprise;

    @FXML
    TextField searchlot;

    @FXML
    Button save;
    String ref, destination;

    // JpaStorage database;
    public void setChoosenDelivery(Livraison livraison) {
        this.chlivraisonf = livraison;
        cbx_choose_livraison.setValue(chlivraisonf);
        this.ref = chlivraisonf.getReference();
        txt_ref_livr.setText(ref);
        sumlivraiz.setText("Total : " + chlivraisonf.getPayed() + " USD");

    }

    public void addProduit(Produit p) {
        lisproduit.add(p);
    }

    public void showPricepane(Event e) {
        if (cbx_regions.getItems().size() > 1) {
            String vla = cbx_regions.getValue();
            if (vla == null) {
                MainUI.notify(null, "Erreur", "Chosissez une region avant de continuer", 3, "error");
                return;
            }
        }
        if (addStocker()) {
            pricepane.setVisible(true);
        }
    }

    public void setDatabase(Entreprise eze) {
        ksf = KazisafeServiceFactory.createService(pref.get("token", null));
        this.entreprise = eze;

        lisfournisseur = FXCollections.observableArrayList(FournisseurDelegate.findFournisseurs());
        lismesure = FXCollections.observableArrayList();
        prices = FXCollections.observableArrayList();
        lisdestocker = new ArrayList<>();
        listrecquis = new ArrayList<>();
        stk_perm = new ArrayList<>();
        loxs = new HashSet<>();
        lisproduit = FXCollections.observableArrayList(ProduitDelegate.findProduits());
        obllot = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();

        t_stocker.setItems(obllot);
        cbx_regions.setItems(regions);
        cbx_choose_livraison.setItems(lisvrezon);
        cbx_choose_produit_stk.setItems(lisproduit);
        cbx_choose_mesure_stk.setItems(lismesure);
        cbx_choose_mesurelot.setItems(lismesure);
        cbx_choose_mesure_vente.setItems(lismesure);
        cbx_choose_mesure_stk.getSelectionModel().selectFirst();
        cbx_choose_mesure_vente.getSelectionModel().selectFirst();
        cbxFssearch = FXCollections.observableArrayList();

        cbx_regions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    destination = newValue;
                }
            }
        });
        cbx_regions.getSelectionModel().selectFirst();
//        new ComboBoxAutoCompletion<>(cbx_choose_livraison); 
        String meth = pref.get("meth", "fifo");
        if (meth.equalsIgnoreCase("ppps")) {
            tabPanelot.getSelectionModel().select(lottab);
        }
        cbx_devise_price.setItems(FXCollections.observableArrayList("USD", "CDF"));
        String maindev = pref.get("mainCur", "USD");
        cbx_devise_price.getSelectionModel().select(maindev);
        ksf.getRegions(entreprise.getUid()).enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        pref.put("region" + (++i), reg);
                    }
                    System.err.println("Agent regions " + lreg.size());
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

        ComboBoxAutoCompletion<Produit> comx = new ComboBoxAutoCompletion<>(cbx_choose_produit_stk);

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

    private void configtablot() {
        col_produit_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            Produit px = param.getValue().getProductId();
            Produit pr = ProduitDelegate.findProduit(px.getUid());
            return new SimpleStringProperty(pr.getNomProduit() + " " + pr.getMarque() + " " + pr.getModele() + " " + pr.getCodebar());
        });
        col_date_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            Date date = param.getValue().getDateStocker();
            return new SimpleStringProperty(tools.Constants.DATE_HEURE_FORMAT.format(date));
        });
        col_date_expir_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            Date exp = param.getValue().getDateExpir();
            return new SimpleStringProperty(exp == null ? "Non périssable" : tools.Constants.DATE_HEURE_FORMAT.format(exp));
        });
        col_ref_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            return new SimpleStringProperty(param.getValue().getLivraisId().getReference());
        });
        col_localisation_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            return new SimpleStringProperty(param.getValue().getLocalisation());
        });
        col_numero_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
            return new SimpleStringProperty(param.getValue().getNumlot());
        });
        col_quant_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, String> param) -> {
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
        col_cout_ach_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getCoutAchat());
        });
        col_cout_total_lot.setCellValueFactory((TableColumn.CellDataFeatures<Stocker, Number> param) -> {
            Stocker l = param.getValue();
            double p = l.getQuantite() * l.getCoutAchat();
            return new SimpleDoubleProperty(p);
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
    HashMap<String, PrixDeVente> coupleLotPv = new HashMap<>();

    @FXML
    public void createNewProductIfnotExist(Event e) {
        MainuiController.getInstance().switchToProduct(e);
        MainUI.floatDialog(tools.Constants.PRODUCT_DLG, 600, 790, token, ksf, this.entreprise, null);
    }

    public void addPriceIfNotExist(Event e) {
        if (choosenStock == null) {
            MainUI.notify(null, "Erreur", "Ajouter d'abord un stock avant de continuer", 3, "error");
            return;
        }

        choosendestock = DestockerDelegate.findCustomised(choosenStock.getProductId().getUid(),
                choosenStock.getNumlot(), this.ref, choosenStock.getDateStocker());
        if (choosendestock == null) {
            choosendestock = new Destocker(DataId.generate());
        }
        choosendestock.setCoutAchat(choosenStock.getCoutAchat());
        choosendestock.setDateDestockage(choosenStock.getDateStocker());
        choosendestock.setDestination(destination == null ? region : destination);
        choosendestock.setLibelle(choosenStock.getLibelle());
        choosendestock.setMesureId(choosenStock.getMesureId());
        choosendestock.setNumlot(choosenStock.getNumlot());
        choosendestock.setObservation(choosenStock.getObservation());
        choosendestock.setProductId(choosenStock.getProductId());
        choosendestock.setQuantite(choosenStock.getQuantite());
        choosendestock.setReference(this.ref);
        choosendestock.setRegion(destination == null ? region : destination);
        choosenreq = RecquisitionDelegate.findCustomized(choosenStock.getProductId().getUid(),
                choosenStock.getNumlot(), this.ref, choosenStock.getDateStocker());
        if (choosenreq == null) {
            choosenreq = new Recquisition(DataId.generate());
        }
        choosenreq.setCoutAchat(choosenStock.getCoutAchat());
        choosenreq.setDate(choosenStock.getDateStocker());
        choosenreq.setDateExpiry(choosenStock.getDateExpir());
        choosenreq.setMesureId(choosenStock.getMesureId());
        choosenreq.setNumlot(choosenStock.getNumlot());
        choosenreq.setObservation(choosenStock.getObservation());
        choosenreq.setProductId(choosenStock.getProductId());
        choosenreq.setQuantite(choosenStock.getQuantite());
        choosenreq.setReference(this.ref);
        choosenreq.setRegion(destination == null ? region : destination);
        choosenreq.setStockAlert(choosenStock.getStockAlerte());

        Destocker foundd = DestockerDelegate.findDestocker(choosendestock.getUid());
        if (foundd == null) {
            Destocker dt = DestockerDelegate.saveDestocker(choosendestock);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(dt, Constants.ACTION_CREATE, Tables.DESTOCKER);
                    });
            saveDestockByHttp(choosendestock);
        }
        Recquisition foundr = RecquisitionDelegate.findRecquisition(choosenreq.getUid());
        if (foundr == null) {
            foundr = RecquisitionDelegate.saveRecquisition(choosenreq);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(choosenreq, Constants.ACTION_CREATE, Tables.RECQUISITION);
                    });
            saveRecqusitionByHttp(choosenreq);
        }
        appendPrice(foundr);

    }

    @FXML
    public void closepricepane(Event e) {
        dpk_date_expir_stk.setValue(null);
        tf_cout_unitr_stk.clear();
        tf_localisation_stk.clear();
        tf_quantite_stk.clear();
        tf_stock_alerte_stk.clear();
        dpk_expir_lot.setValue(null);
        tf_cout_achlot.clear();
        tf_localisalot.clear();
        tilepn_prices1.getChildren().clear();
        tf_prix_de_vente.clear();
        cbx_choose_mesure_vente.setValue(null);
        tf_numlot.clear();
        tf_quant_lot.clear();
        tf_stock_allot.clear();
        choosenStock = null;
        closeFloatingPane(e);
    }

    private void appendPrice(Recquisition savdr) {
        if (tf_qte_min.getText().isEmpty()
                || tf_qte_max.getText().isEmpty()
                || tf_prix_de_vente.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez completer tout les champs relatifs au prix de vente", 5, "error");
            return;
        }
        String ctach = (lottab.isSelected() ? tf_cout_achlot.getText() : tf_cout_unitr_stk.getText());
        if (ctach.isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez completer le cout d'achat", 5, "error");
            return;
        }
        Mesure stm = cbx_choose_mesure_stk.getValue();
        Mesure mesurePv = cbx_choose_mesure_vente.getValue();
        double raps = stm.getQuantContenu() / mesurePv.getQuantContenu();
        if (mesurePv == null) {
            MainUI.notify(null, "Erreur", "Veuillez selectionner une mesure puis continuer", 5, "error");
            return;
        }
        double ppcd = Double.parseDouble(lottab.isSelected() ? tf_cout_achlot.getText() : tf_cout_unitr_stk.getText());
        double caupc = BigDecimal.valueOf(ppcd / stm.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        double pvu = Double.parseDouble(tf_prix_de_vente.getText());
        double rapv = (ppcd / pvu);
        if (rapv >= raps) {
            MainUI.notify(null, bundle.getString("warning"), String.format(bundle.getString("xlowerprice"), caupc), 5, "warning");
            return;
        }
        PrixDeVente pv = new PrixDeVente(DataId.generate());
        pv.setDevise(cbx_devise_price.getValue());
        pv.setPrixUnitaire(pvu);
        pv.setQmin(Double.valueOf(tf_qte_min.getText()));
        pv.setQmax(Double.valueOf(tf_qte_max.getText()));
        pv.setMesureId(mesurePv);
        pv.setRecquisitionId(savdr == null ? choosenreq : savdr);
        if (findPrix(prices, pv) == null) {
            addPrice(pv, tilepn_prices1);
        } else {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("xpriceinterval"), 3, "error");
        }

    }

    private void addPrice(PrixDeVente pv, TilePane tilep) {
        ContextMenu contM = new ContextMenu();
        MenuItem mi = new MenuItem("Supprimer");
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
        List<PrixDeVente> lpvs = PrixDeVenteDelegate.findPrixDeVente(pv.getQmin(),
                pv.getMesureId().getUid(),
                pv.getRecquisitionId().getUid());
        if (!lpvs.isEmpty()) {
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Un prix de vente de la meme description existe deja, voulez vous le remplacer ? ", ButtonType.YES, ButtonType.CANCEL);
//            alert.setTitle("Attention!");
//            alert.setHeaderText(null);
//            Optional<ButtonType> showAndWait = alert.showAndWait();
//            if (showAndWait.get() == ButtonType.YES) {
            PrixDeVente lpv = lpvs.get(0);
            PrixDeVenteDelegate.deletePrixDeVente(lpv);
//                Executors.newCachedThreadPool()
//                        .submit(() -> {
//                            Util.sync(lpv, Constants.ACTION_DELETE, Tables.PRIXDEVENTE);
//                        });
//            }
        }
        PrixDeVente pvx = PrixDeVenteDelegate.savePrixDeVente(pv);
        savePriceByHttp(pv);
//        Executors.newCachedThreadPool()
//                .submit(() -> {
//                    Util.sync(pvx, Constants.ACTION_CREATE, Tables.PRIXDEVENTE);
//                });
        MainUI.notify(null, "Succes", "Prix ajouté avec succès.", 2, "info");

        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int ar = tilep.getChildren().indexOf(l);
                PrixDeVente removed = prices.remove(ar);
                tilep.getChildren().remove(l);
                PrixDeVenteDelegate.deletePrixDeVente(removed);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(removed, Constants.ACTION_DELETE, Tables.PRIXDEVENTE);
                        });
                if (tilep.getChildren().isEmpty()) {
                    DestockerDelegate.deleteDestocker(choosendestock);
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(choosendestock, Constants.ACTION_DELETE, Tables.DESTOCKER);
                            });
                    RecquisitionDelegate.deleteRecquisition(choosenreq);
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(choosenreq, Constants.ACTION_DELETE, Tables.RECQUISITION);
                            });
                }
            }
        });
    }

    @FXML
    public void clearPrices(Event e) {
        if (!tilepn_prices1.getChildren().isEmpty() && !prices.isEmpty()) {
            tilepn_prices1.getChildren().clear();
            for (PrixDeVente price : prices) {
                PrixDeVenteDelegate.deletePrixDeVente(price);
            }
            DestockerDelegate.deleteDestocker(choosendestock);
            RecquisitionDelegate.deleteRecquisition(choosenreq);
        }
    }

    @FXML
    public void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
        choosendestock = null;
        choosenreq = null;
        tf_qte_max.clear();
        tf_qte_min.clear();
        tf_prix_de_vente.clear();
        cbx_choose_mesure_vente.setValue(null);
        tilepn_prices1.getChildren().clear();
    }

    public Recquisition getLastActiveRecquisition(Produit prod) {
        List<Recquisition> lsks = RecquisitionDelegate.findDescSortedByDateForProduit(prod.getUid());
        if (!lsks.isEmpty()) {
            return lsks.get(0);
        }
        return null;
    }

    @FXML
    public void gotoProduct(Event e) {
        MainuiController.getInstance().switchToProduct((MouseEvent) e);
    }

    public void setStock(Stocker stock) {
        if (stock != null) {
            this.choosenStock = stock;
            setChoosenDelivery(choosenStock.getLivraisId());
            obllot.clear();
            obllot.add(choosenStock);
            txt_somme_global_stk.setText("Totaux : " + choosenStock.getPrixAchatTotal());
            choosenPro = choosenStock.getProductId();
            cbx_choose_produit_stk.setValue(choosenStock.getProductId());
            if (lottab.isSelected()) {
                tf_cout_achlot.setText(String.valueOf(choosenStock.getCoutAchat()));
                tf_quant_lot.setText(String.valueOf(choosenStock.getQuantite()));
                tf_numlot.setText(choosenStock.getNumlot());
                tf_localisalot.setText(choosenStock.getLocalisation());
                tf_stock_allot.setText(String.valueOf(choosenStock.getStockAlerte()));
                cbx_choose_mesurelot.setValue(choosenStock.getMesureId());
                if (choosenStock.getDateExpir() != null) {
                    System.out.println("DAtex " + choosenStock.getDateExpir());
                    dpk_expir_lot.setValue(Constants.Datetime.toLocalDate(choosenStock.getDateExpir()));
                }
            } else {
                tf_cout_unitr_stk.setText(String.valueOf(choosenStock.getCoutAchat()));
                tf_quantite_stk.setText(String.valueOf(choosenStock.getQuantite()));
                tf_localisation_stk.setText(choosenStock.getLocalisation());
                tf_stock_alerte_stk.setText(String.valueOf(choosenStock.getStockAlerte()));
                cbx_choose_mesure_stk.setValue(choosenStock.getMesureId());
                if (choosenStock.getDateExpir() != null) {
                    dpk_date_expir_stk.setValue(Constants.Datetime.toLocalDate(choosenStock.getDateExpir()));
                }
            }
        }
    }

    @FXML
    public void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    @FXML
    private void showSupplyAndDeliveryBox(Event e) {
        MainUI.floatDialog(tools.Constants.FOURNISSEUR_DLG, 1090, 630, null, ksf, entreprise);
    }

    public void setAction(String action) {
        this.action = action;
        if (action.equals(Constants.ACTION_CREATE)) {
            save.setText(bundle.getString("xbtn.save"));
        } else if (action.equals(Constants.ACTION_UPDATE)) {
            save.setText(bundle.getString("xbtn.update"));
        }
        System.out.println("Action entrante " + action);
    }

    private Stocker affecteStockerx() {
        boolean islot = lottab.isSelected();
        double qsomlot = 0;
        if (islot) {
            if (!obllot.isEmpty()) {

            }
        }
        return null;
    }

    @FXML
    public void saveStock(ActionEvent e) {

        if (this.action.equals(tools.Constants.ACTION_CREATE)) {
            if (!obllot.isEmpty()) {
                choosenreq = null;
                choosendestock = null;
                MainUI.notify(null, "Succes", "Stock enregistre avec succes", 3, "info");
            } else {
                MainUI.notify(null, "Erreur", "Rien n'a ete sasi, rien n'a ete enregistre", 3, "error");
            }

        } else if (this.action.equals(tools.Constants.ACTION_UPDATE)) {
            if (lottab.isSelected()) {
                if (tf_cout_achlot.getText().isEmpty()) {
                    MainUI.notify(null, "Erreur", "Les champs sont vides, rien n'a ete modifie", 3, "error");
                    return;
                }
                choosenStock.setCoutAchat(Double.parseDouble(tf_cout_achlot.getText()));
                LocalDate expir = dpk_expir_lot.getValue();
                choosenStock.setDateExpir(expir == null ? null : Constants.Datetime.toUtilDate(expir));
                choosenStock.setDateStocker(Constants.Datetime.toUtilDate(chlivraisonf.getDateLivr()));
                choosenStock.setLibelle(chlivraisonf.getLibelle());
                choosenStock.setLivraisId(chlivraisonf);
                choosenStock.setLocalisation(tf_localisalot.getText());
                choosenStock.setProductId(cbx_choose_produit_stk.getValue());
                choosenStock.setNumlot(tf_numlot.getText());
                choosenStock.setQuantite(Double.parseDouble(tf_quant_lot.getText()));
                choosenStock.setMesureId(cbx_choose_mesurelot.getValue());
                choosenStock.setObservation(chlivraisonf.getObservation());
                double totalot = choosenStock.getCoutAchat() * choosenStock.getQuantite();
                choosenStock.setPrixAchatTotal(totalot);
                choosenStock.setStockAlerte(Double.parseDouble(tf_stock_allot.getText()));
                choosenStock.setReduction(0);
                choosenStock.setRegion(region);
            } else {
                choosenStock.setCoutAchat(Double.parseDouble(tf_cout_unitr_stk.getText()));
                LocalDate expir = dpk_date_expir_stk.getValue();
                choosenStock.setDateExpir(expir == null ? null : Constants.Datetime.toUtilDate(expir));
                choosenStock.setDateStocker(Constants.Datetime.toUtilDate(chlivraisonf.getDateLivr()));
                choosenStock.setLibelle(chlivraisonf.getLibelle());
                choosenStock.setLivraisId(chlivraisonf);
                choosenStock.setLocalisation(tf_localisation_stk.getText());
                choosenStock.setProductId(cbx_choose_produit_stk.getValue());
                choosenStock.setNumlot(Constants.TIMESTAMPED_FORMAT.format(new Date()));
                choosenStock.setQuantite(Double.parseDouble(tf_quantite_stk.getText()));
                choosenStock.setMesureId(cbx_choose_mesure_stk.getValue());
                choosenStock.setObservation(chlivraisonf.getObservation());
                double totalot = choosenStock.getCoutAchat() * choosenStock.getQuantite();
                choosenStock.setPrixAchatTotal(totalot);
                choosenStock.setStockAlerte(Double.parseDouble(tf_stock_alerte_stk.getText()));
                choosenStock.setReduction(0);
                choosenStock.setRegion(destination == null ? region : destination);
            }
            Stocker upd = StockerDelegate.updateStocker(choosenStock);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(upd, Constants.ACTION_UPDATE, Tables.STOCKER);
                    });
            List<Destocker> dtks = DestockerDelegate.findByReferenceAndProduit(choosenPro.getUid(), ref);
            if (!dtks.isEmpty()) {
                choosendestock = dtks.get(0);
                choosendestock.setCoutAchat(choosenStock.getCoutAchat());
                choosendestock.setDateDestockage(choosenStock.getDateStocker());
                choosendestock.setDestination(destination == null ? region : destination);
                choosendestock.setLibelle(choosenStock.getLibelle());
                choosendestock.setMesureId(choosenStock.getMesureId());
                choosendestock.setNumlot(choosenStock.getNumlot());
                choosendestock.setObservation(choosenStock.getObservation());
                choosendestock.setProductId(choosenStock.getProductId());
                choosendestock.setQuantite(choosenStock.getQuantite());
                choosendestock.setReference(this.ref);
                choosendestock.setRegion(destination == null ? region : destination);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(DestockerDelegate.updateDestocker(choosendestock), Constants.ACTION_UPDATE, Tables.DESTOCKER);
                        });
            }
            List<Recquisition> rqs = RecquisitionDelegate.findByReference(choosenPro.getUid(), ref);
            if (!rqs.isEmpty()) {
                choosenreq = rqs.get(0);
                choosenreq.setCoutAchat(choosenStock.getCoutAchat());
                choosenreq.setDate(choosenStock.getDateStocker());
                choosenreq.setDateExpiry(choosenStock.getDateExpir());
                choosenreq.setMesureId(choosenStock.getMesureId());
                choosenreq.setNumlot(choosenStock.getNumlot());
                choosenreq.setObservation(choosenStock.getObservation());
                choosenreq.setProductId(choosenStock.getProductId());
                choosenreq.setQuantite(choosenStock.getQuantite());
                choosenreq.setReference(this.ref);
                choosenreq.setRegion(destination == null ? region : destination);
                choosenreq.setStockAlert(choosenStock.getStockAlerte());
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(RecquisitionDelegate.updateRecquisition(choosenreq), Constants.ACTION_UPDATE, Tables.RECQUISITION);
                        });
            }
            GoodstorageController.getInstance().populateStocker(action, upd);
            MainUI.notify(null, "Succes", "Le stock a ete modifie avec succes", 3, "info");
        }

        close(e);
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
    public void addLot(Event e) {
        addStocker();
    }

    private boolean addStocker() {
        boolean ok = false;
        if (lottab.isSelected()) {
            if (txt_ref_livr.getText().isEmpty() || tf_cout_achlot.getText().isEmpty()
                    || tf_localisalot.getText().isEmpty() || tf_numlot.getText().isEmpty() || tf_quant_lot.getText().isEmpty()
                    || tf_stock_allot.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "Veuillez completer tout les champs non facultatif", 4, "error");
                return false;
            }
            String meth = pref.get("meth", "fifo");
            if (meth.equals("ppps") && dpk_expir_lot.getValue() == null) {
                MainUI.notify(null, "Erreur", "La date de peremption est obligatoire avec la methode PPPS", 4, "error");
                return false;
            }
            if (cbx_choose_mesurelot.getValue() == null) {
                MainUI.notify(null, "Erreur", "Veuillez choisir les mesures puis continuer", 4, "error");
                return false;
            }
            if (cbx_choose_produit_stk.getValue() == null) {
                MainUI.notify(null, "Erreur", "Veuillez completer le produit puis continuer", 4, "error");
                return false;
            }
            if (action.equals(Constants.ACTION_CREATE)) {
                choosenStock = new Stocker(DataId.generate());
            }
            choosenStock.setCoutAchat(Double.parseDouble(tf_cout_achlot.getText()));
            LocalDate expir = dpk_expir_lot.getValue();
            choosenStock.setDateExpir(expir == null ? null : Constants.Datetime.toUtilDate(expir));
            choosenStock.setDateStocker(new Date());
            choosenStock.setLibelle(chlivraisonf.getLibelle());
            choosenStock.setLivraisId(chlivraisonf);
            choosenStock.setLocalisation(tf_localisalot.getText());
            choosenStock.setProductId(cbx_choose_produit_stk.getValue());
            choosenStock.setNumlot(tf_numlot.getText());
            choosenStock.setQuantite(Double.parseDouble(tf_quant_lot.getText()));
            choosenStock.setMesureId(cbx_choose_mesurelot.getValue());
            choosenStock.setObservation(chlivraisonf.getObservation());
            double totalot = choosenStock.getCoutAchat() * choosenStock.getQuantite();
            choosenStock.setPrixAchatTotal(totalot);
            choosenStock.setStockAlerte(Double.parseDouble(tf_stock_allot.getText()));
            choosenStock.setReduction(0);
            choosenStock.setRegion(destination == null ? region : destination);

            if (findStocker(choosenStock.getNumlot()) == null) {
                if (obllot.add(choosenStock)) {
                    Stocker stk = StockerDelegate.saveStocker(choosenStock);
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(stk, Constants.ACTION_CREATE, Tables.STOCKER);
                            });

                    choosendestock = new Destocker(DataId.generate());
                    choosendestock.setCoutAchat(choosenStock.getCoutAchat());
                    choosendestock.setDateDestockage(choosenStock.getDateStocker());
                    choosendestock.setDestination(destination == null ? region : destination);
                    choosendestock.setLibelle(choosenStock.getLibelle());
                    choosendestock.setMesureId(choosenStock.getMesureId());
                    choosendestock.setNumlot(choosenStock.getNumlot());
                    choosendestock.setObservation(choosenStock.getObservation());
                    choosendestock.setProductId(choosenStock.getProductId());
                    choosendestock.setQuantite(choosenStock.getQuantite());
                    choosendestock.setReference(this.ref);
                    choosendestock.setRegion(destination == null ? region : destination);

                    choosenreq = new Recquisition(DataId.generate());
                    choosenreq.setCoutAchat(choosenStock.getCoutAchat());
                    choosenreq.setDate(choosenStock.getDateStocker());
                    choosenreq.setDateExpiry(choosenStock.getDateExpir());
                    choosenreq.setMesureId(choosenStock.getMesureId());
                    choosenreq.setNumlot(choosenStock.getNumlot());
                    choosenreq.setObservation(choosenStock.getObservation());
                    choosenreq.setProductId(choosenStock.getProductId());
                    choosenreq.setQuantite(choosenStock.getQuantite());
                    choosenreq.setReference(this.ref);
                    choosenreq.setRegion(destination == null ? region : destination);
                    choosenreq.setStockAlert(choosenStock.getStockAlerte());
                    ok = true;
                    //we don't save as long as we haven't yet saved any sale price
                    Platform.runLater(() -> {
                        txt_totalot.setText(String.format(bundle.getString("xnlot"), obllot.size()));
                    });
                }
            } else {

                choosendestock = new Destocker(DataId.generate());
                choosendestock.setCoutAchat(choosenStock.getCoutAchat());
                choosendestock.setDateDestockage(choosenStock.getDateStocker());
                choosendestock.setDestination(destination == null ? region : destination);
                choosendestock.setLibelle(choosenStock.getLibelle());
                choosendestock.setMesureId(choosenStock.getMesureId());
                choosendestock.setNumlot(choosenStock.getNumlot());
                choosendestock.setObservation(choosenStock.getObservation());
                choosendestock.setProductId(choosenStock.getProductId());
                choosendestock.setQuantite(choosenStock.getQuantite());
                choosendestock.setReference(this.ref);
                choosendestock.setRegion(destination == null ? region : destination);

                choosenreq = new Recquisition(DataId.generate());
                choosenreq.setCoutAchat(choosenStock.getCoutAchat());
                choosenreq.setDate(choosenStock.getDateStocker());
                choosenreq.setDateExpiry(choosenStock.getDateExpir());
                choosenreq.setMesureId(choosenStock.getMesureId());
                choosenreq.setNumlot(choosenStock.getNumlot());
                choosenreq.setObservation(choosenStock.getObservation());
                choosenreq.setProductId(choosenStock.getProductId());
                choosenreq.setQuantite(choosenStock.getQuantite());
                choosenreq.setReference(this.ref);
                choosenreq.setRegion(destination == null ? region : destination);
                choosenreq.setStockAlert(choosenStock.getStockAlerte());
                ok = true;

            }

        } else {
            if (txt_ref_livr.getText().isEmpty() || tf_cout_unitr_stk.getText().isEmpty()
                    || tf_localisation_stk.getText().isEmpty() || tf_quantite_stk.getText().isEmpty()
                    || tf_stock_alerte_stk.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "Veuillez completer tout les champs non facultatif", 4, "error");
                return false;
            }

            if (cbx_choose_mesure_stk.getValue() == null) {
                MainUI.notify(null, "Erreur", "Veuillez choisir les mesures puis continuer", 4, "error");
                return false;
            }
            if (cbx_choose_produit_stk.getValue() == null) {
                MainUI.notify(null, "Erreur", "Veuillez completer le produit puis continuer", 4, "error");
                return false;
            }
            if (action.equals(Constants.ACTION_CREATE)) {
                choosenStock = new Stocker(DataId.generate());
            }
            choosenStock.setCoutAchat(Double.parseDouble(tf_cout_unitr_stk.getText()));
            LocalDate expir = dpk_date_expir_stk.getValue();
            choosenStock.setDateExpir(expir == null ? null : Constants.Datetime.toUtilDate(expir));
            choosenStock.setDateStocker(Constants.Datetime.toUtilDate(chlivraisonf.getDateLivr()));
            choosenStock.setLibelle(chlivraisonf.getLibelle());
            choosenStock.setLivraisId(chlivraisonf);
            choosenStock.setLocalisation(tf_localisation_stk.getText());
            choosenStock.setProductId(cbx_choose_produit_stk.getValue());
            choosenStock.setNumlot(Constants.TIMESTAMPED_FORMAT.format(new Date()));
            choosenStock.setQuantite(Double.parseDouble(tf_quantite_stk.getText()));
            choosenStock.setMesureId(cbx_choose_mesure_stk.getValue());
            choosenStock.setObservation(chlivraisonf.getObservation());
            double totalot = choosenStock.getCoutAchat() * choosenStock.getQuantite();
            choosenStock.setPrixAchatTotal(totalot);
            choosenStock.setStockAlerte(Double.parseDouble(tf_stock_alerte_stk.getText()));
            choosenStock.setReduction(0);
            choosenStock.setRegion(destination == null ? region : destination);
            if (findStocker2(choosenStock.getProductId().getUid()) == null) {
                if (obllot.add(choosenStock)) {
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(StockerDelegate.saveStocker(choosenStock), Constants.ACTION_CREATE, Tables.STOCKER);
                            });
                    List<Destocker> ld = DestockerDelegate.findByReference(ref, choosenStock.getProductId().getUid(), choosenStock.getNumlot());
                    if (ld.isEmpty()) {
                        choosendestock = new Destocker(DataId.generate());
                        choosendestock.setCoutAchat(choosenStock.getCoutAchat());
                        choosendestock.setDateDestockage(choosenStock.getDateStocker());
                        choosendestock.setDestination(destination == null ? region : destination);
                        choosendestock.setLibelle(choosenStock.getLibelle());
                        choosendestock.setMesureId(choosenStock.getMesureId());
                        choosendestock.setNumlot(choosenStock.getNumlot());
                        choosendestock.setObservation(choosenStock.getObservation());
                        choosendestock.setProductId(choosenStock.getProductId());
                        choosendestock.setQuantite(choosenStock.getQuantite());
                        choosendestock.setReference(this.ref);
                        choosendestock.setRegion(destination == null ? region : destination);
                    } else {
                        choosendestock = ld.get(0);
                    }
                    List<Recquisition> lr = RecquisitionDelegate.findByReference(ref, choosenStock.getProductId().getUid(), choosenStock.getNumlot());
                    if (lr.isEmpty()) {
                        choosenreq = new Recquisition(DataId.generate());
                        choosenreq.setCoutAchat(choosenStock.getCoutAchat());
                        choosenreq.setDate(choosenStock.getDateStocker());
                        choosenreq.setDateExpiry(choosenStock.getDateExpir());
                        choosenreq.setMesureId(choosenStock.getMesureId());
                        choosenreq.setNumlot(choosenStock.getNumlot());
                        choosenreq.setObservation(choosenStock.getObservation());
                        choosenreq.setProductId(choosenStock.getProductId());
                        choosenreq.setQuantite(choosenStock.getQuantite());
                        choosenreq.setReference(this.ref);
                        choosenreq.setRegion(destination == null ? region : destination);
                        choosenreq.setStockAlert(choosenStock.getStockAlerte());
                        ok = true;
                        //we don't save as long as we haven't yet saved any sale price
                        Platform.runLater(() -> {
                            txt_totalot.setText(String.format(bundle.getString("xnlot"), obllot.size()));
                        });
                    } else {
                        choosenreq = lr.get(0);
                    }
                }
            } else {
                MainUI.notify(null, "Erreur", "Le stock de ce produit existe deja", 4, "error");
            }

        }
        saveStockByHttp(choosenStock);
        cglobal += choosenStock.getPrixAchatTotal();
        txt_somme_global_stk.setText("Totaux : " + cglobal);
        return ok;
    }

    private void saveStockByHttp(Stocker stocker) {
        /**
         * @Field("uid") String uid,
            @Field("datestok") String date,
            @Field("coutAchat") String coutAch,
            @Field("dateExp") String dateExp,
            @Field("stockAlert") String alerte,
            @Field("quantite") String quantite,
            @Field("libelle") String libelle,
            @Field("localisation") String local,
            @Field("region") String region,
            @Field("prixAchatTot") String prixAchTot,
            @Field("observation") String observation,
            @Field("livraisonId") String livraisonId,
            @Field("mesureId") String mesureId,
            @Field("productId") String productId,
            @Field("numlot") String numlo
         */
        Date d=stocker.getDateExpir();
        ksf.syncStockage(stocker.getUid(),Constants.DATE_HEURE_FORMAT.format(stocker.getDateStocker()), 
                Double.toString(stocker.getCoutAchat()),Constants.DATE_ONLY_FORMAT.format(d), 
                Double.toString(stocker.getStockAlerte()),Double.toString(stocker.getQuantite()), stocker.getLibelle(), 
                stocker.getLocalisation(), stocker.getRegion(), Double.toString(stocker.getPrixAchatTotal()),
                stocker.getObservation(),stocker.getLivraisId().getUid(), stocker.getMesureId().getUid(),
                stocker.getProductId().getUid(), stocker.getNumlot()).enqueue(new Callback<Stocker>() {
            @Override
            public void onResponse(Call<Stocker> call, Response<Stocker> rspns) {
                System.out.println("Stock " + rspns.code());
                if (rspns.isSuccessful()) {
                    System.out.println("Stock Save to server");
                }
            }

            @Override
            public void onFailure(Call<Stocker> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private void saveDestockByHttp(Destocker ds) {
        /**
         * @Field("uid") String uid,
            @Field("datedestok") String date,
            @Field("reference") String reference,
            @Field("destination") String destination,
            @Field("region") String region,
            @Field("coutAch") String coutAch,
            @Field("quantite") String quantite,
            @Field("libelle") String libelle,
            @Field("observation") String observation,
            @Field("mesureId") String mesureId,
            @Field("productId") String productId,
            @Field("numlot") String numlot
         */
        ksf.syncDestocker(ds.getUid(), Constants.DATE_HEURE_FORMAT.format(ds.getDateDestockage()),ds.getReference(),
                ds.getDestination(), ds.getRegion(), Double.toString(ds.getCoutAchat()), Double.toString(ds.getQuantite()),
                ds.getLibelle(), ds.getObservation(), ds.getMesureId().getUid(), ds.getProductId().getUid(), ds.getNumlot())
                .enqueue(new Callback<Destocker>() {
            @Override
            public void onResponse(Call<Destocker> call, Response<Destocker> rspns) {
                System.out.println("Destock " + rspns.message());
                if (rspns.isSuccessful()) {
                    System.out.println("Destocker Save to server");
                }
            }

            @Override
            public void onFailure(Call<Destocker> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private void saveRecqusitionByHttp(Recquisition req) {
        /**
         * @Field("uid") String uid,
            @Field("dateReq") String dateReq,
            @Field("observation") String observation,
            @Field("reference") String reference,
            @Field("quantite") String quantite,
            @Field("coutAch") String coutAchat,
            @Field("dateExp") String dateExp,
            @Field("alerte") String alerte,
            @Field("mesureId") String mesureId,
            @Field("productId") String productId,
            @Field("region") String region,
            @Field("numlot") String numlot
         */
        ksf.syncRecquisition(req.getUid(), Constants.DATE_HEURE_FORMAT.format(req.getDate()), req.getObservation(),
                req.getReference(), Double.toString(req.getQuantite()), Double.toString(req.getCoutAchat()),
                Constants.DATE_ONLY_FORMAT.format(req.getDateExpiry()), Double.toString(req.getStockAlert()), 
                req.getMesureId().getUid(), req.getProductId().getUid(), req.getRegion(), req.getNumlot())
                .enqueue(new Callback<Recquisition>() {
            @Override
            public void onResponse(Call<Recquisition> call, Response<Recquisition> rspns) {
                System.out.println("Recquis " + rspns.message());
                if (rspns.isSuccessful()) {
                    System.out.println("Recq Save to server");
                }
            }

            @Override
            public void onFailure(Call<Recquisition> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private void savePriceByHttp(PrixDeVente pv) {
        ksf.savePrice(pv).enqueue(new Callback<PrixDeVente>() {
            @Override
            public void onResponse(Call<PrixDeVente> call, Response<PrixDeVente> rspns) {
                System.out.println("Price " + rspns.message());
                if (rspns.isSuccessful()) {
                    System.out.println("Price Save to server");
                }
            }

            @Override
            public void onFailure(Call<PrixDeVente> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private Stocker findStocker(String numlot) {
        for (Stocker ol : obllot) {
            if (ol.getNumlot().equals(numlot)) {
                return ol;
            }
        }
        return null;
    }

    private Stocker findStocker2(String pid) {
        for (Stocker ol : obllot) {
            if (ol.getProductId().getUid().equals(pid)) {
                return ol;
            }
        }
        return null;
    }

    private void configcbx() {

        cbx_choose_mesure_stk.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_choose_mesure_stk.getItems()
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
                        .filter(f -> (f.getFournId().getNomFourn() + ", " + f.getNumPiece() + " " +f.getDateLivr().toString())
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

        cbx_choose_mesurelot.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_choose_mesurelot.getItems()
                        .stream()
                        .filter(f -> (f.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_choose_mesure_stk.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            choosenM = newValue;
        });
        cbx_choose_mesurelot.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            choosenM = newValue;
        });
        cbx_choose_produit_stk.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null : object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_choose_produit_stk.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_choose_produit_stk.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) -> {
            choosenPro = newValue;
            if (choosenPro == null) {
                return;
            }
            List<Mesure> mzs = MesureDelegate.findMesureByProduit(choosenPro.getUid());
            //nsmesure.findAllEquals("produitId.uid", choosenPro.getUid());
            // Util.findMesureForProduitWithId(nsmesure.findAll(), choosenPro.getUid());
            lismesure.setAll(mzs);
            cbx_choose_mesure_stk.getSelectionModel().selectFirst();
            cbx_choose_mesurelot.getSelectionModel().selectFirst();
            cbx_choose_mesure_vente.getSelectionModel().selectFirst();
            List<Stocker> proxt = StockerDelegate.findDescSortedByDateStock(choosenPro.getUid());

            if (!proxt.isEmpty()) {
                Stocker fromlast = proxt.get(0);
                if (lottab.isSelected()) {
                    tf_cout_achlot.setText(String.valueOf(fromlast.getCoutAchat()));
                    tf_localisalot.setText(fromlast.getLocalisation());
                    dpk_expir_lot.setValue(Constants.Datetime.toLocalDate(fromlast.getDateExpir()));
                    tf_numlot.setText(fromlast.getNumlot());
                    tf_stock_allot.setText(String.valueOf(fromlast.getStockAlerte()));
                } else {
                    tf_cout_unitr_stk.setText(String.valueOf(fromlast.getCoutAchat()));
                    tf_localisation_stk.setText(fromlast.getLocalisation());
                    tf_stock_alerte_stk.setText(String.valueOf(fromlast.getStockAlerte()));
                    if (fromlast.getDateExpir() != null) {
                        dpk_date_expir_stk.setValue(Constants.Datetime.toLocalDate(fromlast.getDateExpir()));
                    }
                }
            }

        });

        tf_quantite_stk.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (tf_cout_unitr_stk.getText().isEmpty() || newValue == null) {
                    return;
                }
                try {
                    double cu = Double.parseDouble(tf_cout_unitr_stk.getText());
                    double qt = Double.parseDouble(newValue);
                    coutLigne = qt * cu;
                    txt_somme_ct_stk.setText("Total : " + coutLigne);
                } catch (NumberFormatException e) {

                }
            }
        });
        tf_cout_unitr_stk.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (tf_quantite_stk.getText().isEmpty() || newValue == null) {
                return;
            }
            try {
                double qt = Double.parseDouble(tf_quantite_stk.getText());
                double cu = Double.parseDouble(newValue);
                coutLigne = qt * cu;
                txt_somme_ct_stk.setText("Total : " + coutLigne);
            } catch (NumberFormatException e) {
//                    e.printStackTrace();
            }
        });

        tf_quant_lot.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (tf_cout_achlot.getText().isEmpty() || newValue == null) {
                    return;
                }
                try {
                    double cu = Double.parseDouble(tf_cout_achlot.getText());
                    double qt = Double.parseDouble(newValue);
                    coutLigne = qt * cu;
                    txt_somme_ct_lot1.setText("Total : " + coutLigne);
                } catch (NumberFormatException e) {

                }
            }
        });
        tf_cout_achlot.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (tf_quant_lot.getText().isEmpty() || newValue == null) {
                    return;
                }
                try {
                    double qt = Double.parseDouble(tf_quant_lot.getText());
                    double cu = Double.parseDouble(newValue);
                    coutLigne = qt * cu;
                    txt_somme_ct_lot1.setText("Total : " + coutLigne);
                } catch (NumberFormatException e) {
//                    e.printStackTrace();
                }
            }
        });
        tf_prix_de_vente.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.isEmpty() || newValue == null) {
                return;
            }
            try {
                if (cbx_devise_price.getValue().equals("USD")) {
                    double data = Double.parseDouble(newValue);
                    double enCdf = BigDecimal.valueOf(data * taux).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    txt_equivalentCdf.setText(enCdf + " Fc");
                } else {
                    double data = Double.parseDouble(newValue);
                    double enCdf = BigDecimal.valueOf(data / taux).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    txt_equivalentCdf.setText(enCdf + " Usd");
                }
            } catch (NumberFormatException e) {
                MainUI.notify(null, "Erreur", "Entrer les chiffres uniquement", 3, "error");
            }
        });
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
        lisvrezon = FXCollections.observableArrayList();
        configcbx();
        configtablot();
        MainUI.setPattern(dpk_date_expir_stk);
        MainUI.setPattern(dpk_expir_lot);
        pricepane.setVisible(false);
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux = pref.getDouble("taux2change", 2000);
        region = pref.get("region", null);
        token = pref.get("token", null);
        role = pref.get("priv", null);
        List<Livraison> livrs = LivraisonDelegate.findLivraisons();
        lisvrezon.addAll(livrs);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Enlever de la liste");
        // add menu items to menu
        MenuItem mi1 = new MenuItem("Modifier");
        contextMenu.getItems().add(mi1);
        contextMenu.getItems().add(menuItem1);
        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });
        mi1.setOnAction((ActionEvent event) -> {

        });
        searchlot.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    ObservableList<Stocker> rst = FXCollections.observableArrayList();
                    for (Stocker stk : obllot) {
                        String prod = stk.getProductId().getNomProduit() + " " + stk.getProductId().getMarque() + " "
                                + "" + stk.getProductId().getCodebar() + " " + stk.getProductId().getCouleur() + " "
                                + "" + stk.getProductId().getModele() + " " + stk.getProductId().getTaille();
                        String pred = stk.getNumlot() + " " + stk.getLocalisation() + " " + prod + " " + stk.getLibelle() + ""
                                + " " + Constants.USER_READABLE_FORMAT.format(stk.getDateStocker());
                        if (pred.toUpperCase().contains(newValue.toUpperCase())) {
                            rst.add(stk);
                        }
                    }
                    t_stocker.setItems(rst);
                } else {
                    t_stocker.setItems(obllot);
                }
            }
        });

        ContextMenu contM = new ContextMenu();
        MenuItem mi = new MenuItem(bundle.getString("clean"));
        MenuItem md = new MenuItem(bundle.getString("delete"));
        contM.getItems().add(mi);
        contM.getItems().add(md);
        t_stocker.setContextMenu(contM);
        mi.setOnAction((ActionEvent event) -> {
            Stocker lot = t_stocker.getSelectionModel().getSelectedItem();
            obllot.remove(lot);
            stk_perm.remove(lot);
            Recquisition r = getRecByLot(lot.getNumlot());
            Destocker d = getDestByLot(lot.getNumlot());
            lisdestocker.remove(d);
            listrecquis.remove(r);
            txt_totalot.setText(obllot.size() + " Lot(s)");
        });

        Tooltip.install(img_btn_apply_price, new Tooltip("Appliquer les prix actuels"));
    }

    private Recquisition getRecByLot(String lot) {
        for (Recquisition req : listrecquis) {
            if (req.getNumlot().equals(lot)) {
                return req;
            }
        }
        return null;
    }

    private Destocker getDestByLot(String lot) {
        for (Destocker destocker : lisdestocker) {
            if (destocker.getNumlot().equals(lot)) {
                return destocker;
            }
        }
        return null;
    }
}
