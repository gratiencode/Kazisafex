/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.DestockerDelegate;
import delegates.MesureDelegate;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
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

import data.Destocker;
import data.Entreprise;
import data.LigneVente;
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
import data.helpers.Role;
import data.network.Kazisafe;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class DestockController implements Initializable {

    public static DestockController getInstance() {
        return instance;
    }

    @FXML
    private Label txt_reference_dstk;
    @FXML
    private ComboBox<Produit> cbx_choose_product_dstk;
    @FXML
    private ComboBox<Mesure> cbx_choose_mesure_dstk;
    @FXML
    private TextField tf_quantite_dstk, tf_quant_disponible;
    @FXML
    private TextField tf_cout_unitr_cump_dstk, tf_observation;
    @FXML
    private Label txt_somme_ct_dstk;
    @FXML
    private ListView<Destocker> listview_dstks;
    @FXML
    private ListView<Stocker> listview_stks;
    @FXML
    private Label txt_somme_global_dstk;
    @FXML
    private Label txt_count_dstk;
    @FXML
    private DatePicker dpk_date_dstk;
    @FXML
    private ComboBox<String> cbx_destination_dstk;
    @FXML
    CheckBox save_req;

    @FXML
    private ComboBox<Stocker> cbx_stock_lots;
    @FXML
    private Label txt_stk_alerte, alveol, value_stock;
    @FXML
    private Label txt_date_expiry, methode;

    @FXML
    TilePane tilepn_prices1;
    @FXML
    ComboBox<String> cbx_devise_req1;
    @FXML
    ComboBox<Mesure> cbx_choose_mesure_vente;
    @FXML
    TextField tf_qte_min;
    @FXML
    TextField tf_qte_max;
    @FXML
    TextField tf_prix_de_vente;
    @FXML
    Label txt_equivalentCdf;
    @FXML
    Pane pricepane;

    double coutLigne = 0;
    double cglobal = 0;
    String action, token, entr;

//    JpaStorage store;
    Destocker choosenDestocker;
    Mesure choosenM;
    Produit choosenProduct;
    Stocker lastock;
    Preferences pref;
    Stocker choosenStockLot;

    ObservableList<String> regions;
    ObservableList<Produit> lisproduit;
    ObservableList<Stocker> lisstocker;
//    ObservableList<Stocker> lisstocker;
    ObservableList<Destocker> lisdestocker, lsdin;
    ObservableList<Mesure> lismesure;

    private static DestockController instance;
    private Kazisafe kazisafe;
    String region, role, meth;
    ResourceBundle bundle;
    RecquisitionManager reqmanager;
    Recquisition req;
    List<PrixDeVente> prices;
    double taux;

    public DestockController() {
        instance = this;
    }

    public void setDatabase(Entreprise eze) {
        //store = JpaStorage.getInstance();
        kazisafe = KazisafeServiceFactory.createService(token);
        lisdestocker = FXCollections.observableArrayList(DestockerDelegate.findDestockers());
        lismesure = FXCollections.observableArrayList();
        lsdin = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        cbx_destination_dstk.setItems(regions);
        lisproduit = FXCollections.observableArrayList(ProduitDelegate.findProduits());
        cbx_choose_product_dstk.setItems(lisproduit);
        lisstocker = FXCollections.observableArrayList();
        cbx_choose_mesure_dstk.setItems(lismesure);
        txt_reference_dstk.setText("DST" + ((int) (Math.random() * 100000)) + "K");
        listview_dstks.setItems(lsdin);
        cbx_stock_lots.setItems(lisstocker);
        reqmanager = new RecquisitionManager();
        cbx_devise_req1.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_devise_req1.getSelectionModel().selectFirst();
        prices = new ArrayList<>();
        regions.add("Déclassement de stock");
        kazisafe.getRegions(entr).enqueue(new Callback<List<String>>() {
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
        regions.addAll(GoodstorageController.getInstance().accessregion());
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

        cbx_choose_mesure_vente.setItems(lismesure);

        save_req.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    if (tf_quantite_dstk.getText().isEmpty() || cbx_destination_dstk.getValue() == null) {
                        MainUI.notify(null, bundle.getString("error"), "Completer la quantite et la destination puis continuer", 3, "error");
                        save_req.setSelected(false);
                        prices.clear();
                        return;
                    }
                    pricepane.setVisible(true);
                }
            }
        });

        tf_prix_de_vente.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.isEmpty() || newValue == null) {
                return;
            }
            try {
                if (cbx_devise_req1.getValue().equals("USD")) {
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

        cbx_stock_lots.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Stocker>() {
            @Override
            public void changed(ObservableValue<? extends Stocker> observable, Stocker oldValue, Stocker newValue) {
                if (newValue != null) {
                    choosenStockLot = newValue;
                    alveol.setText(choosenStockLot.getLocalisation());
                    Date date = choosenStockLot.getDateExpir();
                    txt_date_expiry.setText(date == null ? bundle.getString("noperish") : "Exp : " + Constants.USER_READABLE_FORMAT.format(date));

                    txt_date_expiry.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
                    if (date == null) {
                        return;
                    }

                    Mesure mz = choosenStockLot.getMesureId();
                    List<Destocker> lsdx = DestockerDelegate.findByProduitLot(choosenProduct.getUid(), choosenStockLot.getNumlot());
                    List<Destocker> lsd = fullMesureRecqs(lsdx);
                    double sortie = Util.sumDestockerQuantInPc(lsd);
                    Mesure mreel = MesureDelegate.findMesure(mz.getUid());
                    double entree = choosenStockLot.getQuantite() * mreel.getQuantContenu();
                    double dispo = entree - sortie;

                    double stalertx = (choosenStockLot.getStockAlerte() * mreel.getQuantContenu());
                    double choose = stalertx / choosenM.getQuantContenu();
                    txt_stk_alerte.setText("Alert : " + choose);

                    double converted = dispo / choosenM.getQuantContenu();
                    tf_cout_unitr_cump_dstk.setText(String.valueOf(choosenStockLot.getCoutAchat()));
                    tf_quant_disponible.setText(String.valueOf(converted));
                    value_stock.setText(String.valueOf(BigDecimal.valueOf(converted * choosenStockLot.getCoutAchat()).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));

                    int exp = isStockExpired(choosenStockLot);
                    if (exp == -1) {
                        txt_date_expiry.setBackground(new Background(new BackgroundFill(Color.web("#f58282"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 3) {
                        txt_date_expiry.setBackground(new Background(new BackgroundFill(Color.web("#c46506"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 6) {
                        txt_date_expiry.setBackground(new Background(new BackgroundFill(Color.web("#f7fa61"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 12) {
                        txt_date_expiry.setBackground(new Background(new BackgroundFill(Color.web("#c5e6b3"), new CornerRadii(20), new Insets(4))));
                    } else {
                        txt_date_expiry.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
                    }
                    txt_date_expiry.setStyle("-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 2;");
                }
            }
        });

        new ComboBoxAutoCompletion<>(cbx_choose_product_dstk);
    }

    @FXML
    public void clearPrices(Event e) {
        tilepn_prices1.getChildren().clear();
        prices.clear();
        //req.setPrixDeVenteList(prices);
    }

    @FXML
    public void addRecquisit(Event e) {
        List<PrixDeVente> lps = new ArrayList<>();
        Recquisition reqx = new Recquisition(DataId.generate());
        reqx.setCoutAchat(Double.parseDouble(tf_cout_unitr_cump_dstk.getText()));
        reqx.setDate(Constants.Datetime.toUtilDate(dpk_date_dstk.getValue()));
        double qte = Double.parseDouble(tf_quantite_dstk.getText());
        reqx.setQuantite(qte);
        reqx.setMesureId(choosenM);
        reqx.setNumlot(choosenStockLot.getNumlot());
        reqx.setProductId(choosenProduct);
        reqx.setReference(txt_reference_dstk.getText());
        String obs = tf_observation.getText();
        reqx.setObservation(obs.isEmpty() ? "R.A.S" : obs);
        reqx.setRegion(cbx_destination_dstk.getValue());
        List<Stocker> stks = StockerDelegate.findStockerByProduitLot(choosenProduct.getUid(), reqx.getNumlot());
        Stocker s = stks.get(0);
        reqx.setDateExpiry(s.getDateExpir());
        for (PrixDeVente price : prices) {
            price.setRecquisitionId(reqx);
            lps.add(price);
        }
        reqx.setPrixDeVenteList(lps);
        reqmanager.addRecqusition(reqx);
        closeFloatingPane(e);
        prices.clear();
        save_req.setSelected(false);

    }

    @FXML
    public void applyOldPrice(Event e) {
        if (choosenProduct != null) {
//           
//            Recquisition req = getLastActiveRecquisition(choosenPro);
//            if (req == null) {
//                return;
//            }
//            List<PrixDeVente> prices = database.findWithAndClause(PrixDeVente.class, new String[]{"recquisition_id"}, new String[]{req.getUid()});
//            System.out.println("Pricess OKK " + prices.size());
//            if (standtab.isSelected()) {
//                for (PrixDeVente pvx : prices) {
//                    PrixDeVente pv = new PrixDeVente(DataId.generate());
//                    pv.setDevise(pvx.getDevise());
//                    pv.setPrixUnitaire(pvx.getPrixUnitaire());
//                    pv.setQmin(pvx.getQmin());
//                    pv.setQmax(pvx.getQmax());
//                    pv.setMesureId(pvx.getMesureId());
//                    if (findPrix(listprices, pv) == null) {
//                        addPrice(pv, tilepn_prices, false);
//                    }
//                    //addPrice(pv, tilepn_prices, false,true);
//                }
//            }
//            if (lottab.isSelected()) {
//                for (PrixDeVente pvx : prices) {
//                    PrixDeVente pv = new PrixDeVente(DataId.generate());
//                    pv.setDevise(pvx.getDevise());
//                    pv.setPrixUnitaire(pvx.getPrixUnitaire());
//                    pv.setQmin(pvx.getQmin());
//                    pv.setQmax(pvx.getQmax());
//                    pv.setMesureId(pvx.getMesureId());
//                    if (findPrix(listpricelot, pv) == null) {
//                        addPrice(pv, tilepn_prices1, true);
//                    }
//
//                    //titlot.setText("Prix de ventes (" + tilepn_prices1.getChildren().size() + ")");
//                }
//            }
//            add_stocker.setDisable(false);
        }
    }

    @FXML
    public void addPriceIfPossible(Event e) {
        if (tf_qte_min.getText().isEmpty()
                || tf_qte_max.getText().isEmpty()
                || tf_prix_de_vente.getText().isEmpty()
                || tf_cout_unitr_cump_dstk.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez completer tout les champs relatifs au prix de vente", 5, "error");
            return;
        }
        Mesure stm = cbx_choose_mesure_dstk.getValue();
        Mesure mesurePv = cbx_choose_mesure_vente.getValue();
        if (mesurePv == null) {
            MainUI.notify(null, "Erreur", "Veuillez completer la mesure de la vente", 5, "error");
            return;
        }
        double raps = stm.getQuantContenu() / mesurePv.getQuantContenu();
        if (mesurePv == null) {
            MainUI.notify(null, "Erreur", "Veuillez selectionner une mesure puis continuer", 5, "error");
            return;
        }
        double ppcd = Double.parseDouble(tf_cout_unitr_cump_dstk.getText());
        double caupc = BigDecimal.valueOf(ppcd / stm.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        double pvu = Double.parseDouble(tf_prix_de_vente.getText());
        double rapv = (ppcd / pvu);
        if (rapv >= raps) {
            MainUI.notify(null, bundle.getString("warning"), String.format(bundle.getString("xlowerprice"), caupc), 5, "warning");
            return;
        }
        PrixDeVente pv = new PrixDeVente(DataId.generate());
        pv.setDevise(cbx_devise_req1.getValue());
        pv.setPrixUnitaire(pvu);
        pv.setQmin(Double.valueOf(tf_qte_min.getText()));  
        pv.setQmax(Double.valueOf(tf_qte_max.getText()));
        pv.setMesureId(mesurePv);
        if (findPrix(prices, pv) == null) {
            addPrice(pv, tilepn_prices1, true);
        } else {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("xpriceinterval"), 3, "error");
        }
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

    private void addPrice(PrixDeVente pv, TilePane tilep, boolean islot) {
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
        // req.setPrixDeVenteList(prices);
        MainUI.notify(null, "Succes", "Prix ajouté avec succès.", 2, "info");

        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int ar = tilep.getChildren().indexOf(l);
                PrixDeVente p = find(l.getId());
                prices.remove(p);
                tilep.getChildren().remove(l);
                req.setPrixDeVenteList(prices);
            }
        });
    }

    private PrixDeVente find(String pvid) {
        for (PrixDeVente price : prices) {
            if (price.getUid().equals(pvid)) {
                return price;
            }
        }
        return null;
    }

    private <T> List<T> fullMesureRecqs(List<T> reqs) {
        List<T> result = new ArrayList<>();
        for (T obj : reqs) {
            if (obj instanceof Recquisition) {
                Recquisition req = (Recquisition) obj;
                Mesure m = MesureDelegate.findMesure(req.getMesureId().getUid());
                System.out.println("Mesurex " + m.getDescription());
                req.setMesureId(m);
                result.add((T) req);
            } else if (obj instanceof LigneVente) {
                LigneVente req = (LigneVente) obj;
                Mesure m = MesureDelegate.findMesure(req.getMesureId().getUid());
                System.out.println("Mesurex lv " + m.getDescription());
                req.setMesureId(m);
                result.add((T) req);
            } else if (obj instanceof Destocker) {
                Destocker req = (Destocker) obj;
                Mesure m = MesureDelegate.findMesure(req.getMesureId().getUid());
                System.out.println("Mesurex DSTK " + m.getDescription());
                req.setMesureId(m);
                result.add((T) req);
            } else if (obj instanceof Stocker) {
                Stocker req = (Stocker) obj;
                Mesure m = MesureDelegate.findMesure(req.getMesureId().getUid());
                System.out.println("Mesurex STK " + m.getDescription());
                req.setMesureId(m);
                result.add((T) req);
            }
        }

        return result;
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

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        configcbx();
        MainUI.setPattern(dpk_date_dstk);
        dpk_date_dstk.setValue(LocalDate.now());

        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem(bundle.getString("removefromlist"));
        pricepane.setVisible(false);
        // add menu items to menu
        contextMenu.getItems().add(menuItem1);
        listview_dstks.setContextMenu(contextMenu);
        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenDestocker != null) {
                    lsdin.remove(choosenDestocker);
                    cglobal -= (choosenDestocker.getCoutAchat() * choosenDestocker.getQuantite());
                    txt_somme_global_dstk.setText(cglobal + "$");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            txt_count_dstk.setText(String.format(bundle.getString("xitems"), lisdestocker.size()));
                        }
                    });
                }
            }
        });
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        token = pref.get("token", null);
        entr = pref.get("eUid", "f3d81978a5524681bf1090d1d41edb15");
        region = pref.get("region", "...");
        role = pref.get("priv", null);
        meth = pref.get("meth", "fifo");
        taux = pref.getDouble("taux2change", 2000);
        txt_date_expiry.setStyle("-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 2;");
        methode.setText(bundle.getString("inventory") + " : " + meth);
        methode.setTooltip(new Tooltip(bundle.getString("go2param4choice")));

    }

    @FXML
    public void addDestocker(ActionEvent event) {
        String reg = cbx_destination_dstk.getValue();
        if (dpk_date_dstk.getValue() == null || tf_cout_unitr_cump_dstk.getText().isEmpty() || choosenProduct == null
                || tf_quantite_dstk.getText().isEmpty() || cbx_destination_dstk.getValue() == null) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("fillnoptional"), 5, "error");
            return;
        }
        if (reg.equalsIgnoreCase("Déclassement de stock") && tf_observation.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("fillmotif2class"), 5, "error");
            return;
        }

        double qte = Double.parseDouble(tf_quantite_dstk.getText());
        if (choosenM == null) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("choosemez"), 3, "error");
            return;
        }
        // double rqte = Util.getQRest(lisstocker, lisdestocker, choosenM, choosenProduct);

        Mesure mz = choosenStockLot.getMesureId();
        Mesure reel = MesureDelegate.findMesure(mz.getUid());
        double qco = reel.getQuantContenu();
        double alert = choosenStockLot.getStockAlerte() * qco;
        List<Destocker> lsdx = DestockerDelegate.findByProduitLot(choosenProduct.getUid(), choosenStockLot.getNumlot());
        List<Destocker> lsd = fullMesureRecqs(lsdx);
        double sortie = Util.sumQuantInPc(lsd);
        double entree = choosenStockLot.getQuantite() * qco;
        double dispo = entree - sortie;
        double qinpc = qte * choosenM.getQuantContenu();
        double converted = dispo / choosenM.getQuantContenu();
        double stab = (converted - qinpc);

        if (alert != 0 && stab <= alert) {
            //notify
            MainUI.notify(null, bundle.getString("warning"), bundle.getString("alertmess"), 4, "warning");
            if (qte >= converted) {
                Alert alertd = new Alert(Alert.AlertType.WARNING, String.format(bundle.getString("alertyesmessage"), converted, choosenM.getDescription(), qte, choosenM.getDescription()), ButtonType.YES, ButtonType.CANCEL);
                alertd.setTitle(bundle.getString("warning"));
                alertd.setHeaderText(null);
                Optional<ButtonType> showAndWait = alertd.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    qte = converted;
                }
            }
            if (converted <= 0) {
                Alert alertd = new Alert(Alert.AlertType.WARNING, bundle.getString("notsatisfyreq"), ButtonType.YES, ButtonType.CANCEL);
                alertd.setTitle(bundle.getString("warning"));
                alertd.setHeaderText(null);
                alertd.show();
                return;
            }
        }
        Destocker s = new Destocker(DataId.generate());
        s.setCoutAchat(Double.parseDouble(tf_cout_unitr_cump_dstk.getText()));
        s.setDateDestockage(Constants.Datetime.toUtilDate(dpk_date_dstk.getValue()));
        s.setQuantite(qte);
        s.setDestination(cbx_destination_dstk.getValue());
        s.setMesureId(choosenM);
        s.setNumlot(choosenStockLot.getNumlot());
        s.setProductId(choosenProduct);
        s.setReference(txt_reference_dstk.getText());
        String obs = tf_observation.getText();
        s.setLibelle(obs.isEmpty() ? bundle.getString("unstockingof") + " : " + s.getDateDestockage() : obs);
        s.setObservation(obs.isEmpty() ? "R.A.S" : obs);
        s.setRegion(region);

        cglobal += coutLigne;
        txt_somme_global_dstk.setText(cglobal + "$");
        cbx_choose_product_dstk.getSelectionModel().select(null);
        cbx_choose_mesure_dstk.getSelectionModel().select(null);
        tf_quantite_dstk.setText("");
        tf_quant_disponible.clear();
        value_stock.setText("0");
        tf_cout_unitr_cump_dstk.setText("");
        tf_observation.clear();
        cbx_destination_dstk.getSelectionModel().clearSelection();
        lsdin.add(s);
        save_req.setSelected(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txt_count_dstk.setText(String.format(bundle.getString("xitems"), lsdin.size()));
            }
        });
    }

    @FXML
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);

    }

    @FXML
    public void saveDestock(ActionEvent event) {
        if (this.action.equals(tools.Constants.ACTION_CREATE)) {
            for (Destocker d : lsdin) {
                DestockerDelegate.saveDestocker(d);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(d, Constants.ACTION_CREATE, Tables.DESTOCKER);
                        });
                if (!reqmanager.isEmpty()) {
                    Recquisition r = reqmanager.findbyLotAndProduit(d.getProductId().getUid(), d.getNumlot());
                    if (r != null) {
                        //store.insertAndSync(r);
                        Recquisition rk = RecquisitionDelegate.saveRecquisition(r);
                        Executors.newCachedThreadPool()
                                .submit(() -> {
                                    Util.sync(rk, Constants.ACTION_CREATE, Tables.RECQUISITION);
                                });
                        List<PrixDeVente> prix = r.getPrixDeVenteList();
                        for (PrixDeVente pv : prix) {
                            pv.setRecquisitionId(r);
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(pv, Constants.ACTION_CREATE, Tables.PRIXDEVENTE);
                                    });
                        }
                    }
                }
            }

//                        for (Destocker d : lsdin) {
//                            Util.sync(d, Constants.ACTION_CREATE, Tables.DESTOCKER);
//                            if (!reqmanager.isEmpty()) {
//                                Recquisition r = reqmanager.findbyLotAndProduit(d.getProductId().getUid(), d.getNumlot());
//                                if (r != null) {
//                                    //store.insertAndSync(r);
//                                    Util.sync(r, Constants.ACTION_CREATE, Tables.RECQUISITION);
//                                    System.out.println("Recqusiit === " + JsonUtil.jsonify(r).toString());
//                                    List<PrixDeVente> prix = r.getPrixDeVenteList();
//                                    for (PrixDeVente pv : prix) {
//                                        pv.setRecquisitionId(r);
//
//                                        Util.sync(pv, Constants.ACTION_CREATE, Tables.PRIXDEVENTE);
//
//                                    }
//                                }
//                            }
//                        }
//                    });
            MainUI.notify(null, bundle.getString("success"), bundle.getString("unstocksaved"), 3, "info");
            MainuiController.getInstance().switchToStock(event);
            reqmanager.clean();
            lsdin.clear();
        } else if (this.action.equals(tools.Constants.ACTION_UPDATE)) {
            if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name()) | role.equals(Role.Magazinner.name())) {
                if (choosenDestocker == null || choosenM == null) {
                    MainUI.notify(null, bundle.getString("error"), "Le destockage a modifier n'a pas bien ete choisit", 3, "error");
                    return;
                }
                choosenDestocker.setCoutAchat(Double.parseDouble(tf_cout_unitr_cump_dstk.getText()));
                choosenDestocker.setDateDestockage(Constants.Datetime.toUtilDate(dpk_date_dstk.getValue()));
                choosenDestocker.setQuantite(Double.parseDouble(tf_quantite_dstk.getText()));
                choosenDestocker.setDestination(cbx_destination_dstk.getValue() == null ? choosenDestocker.getDestination() : cbx_destination_dstk.getValue());
                choosenDestocker.setMesureId(choosenM);
                choosenDestocker.setProductId(choosenProduct);
                choosenDestocker.setReference(choosenDestocker.getReference());
                String obs = tf_observation.getText();
                choosenDestocker.setLibelle(obs.isEmpty() ? "Last update : " + Constants.Datetime.format(new Date()) : obs);
                choosenDestocker.setObservation(obs.isEmpty() ? "R.A.S" : obs);
                choosenDestocker.setRegion(region);
                Destocker tos = DestockerDelegate.updateDestocker(choosenDestocker); //store.update(d);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(tos, Constants.ACTION_UPDATE, Tables.DESTOCKER);
                        });

                MainUI.notify(null, bundle.getString("success"), bundle.getString("unstocksaved"), 3, "info");
            } else {
                MainUI.notify(null, bundle.getString("error"), bundle.getString("priv_msg"), 3, "error");
            }
//            Executors.newCachedThreadPool()
//                    .submit(() -> {
//                        lsdin.forEach((d) -> {
//                            Util.sync(d, Constants.ACTION_UPDATE, Tables.DESTOCKER);
//                        });
//                    });
        }
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

    private void configcbx() {
        cbx_choose_mesure_dstk.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_choose_mesure_dstk.getItems()
                        .stream()
                        .filter(f -> (f.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_destination_dstk.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) {
                    return;
                }
                if (newValue.equalsIgnoreCase("Déclassement de stock")) {
                    tf_observation.setPromptText(bundle.getString("xcol.observation"));
                } else {
                    tf_observation.setPromptText(bundle.getString("xtxtf.prompt.observation"));
                }
            }
        });
        cbx_choose_mesure_dstk.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            choosenM = newValue;
            if (choosenM != null && choosenProduct != null && choosenStockLot != null) {
                double cumpx = choosenStockLot.getCoutAchat();
                Mesure mz = choosenStockLot.getMesureId();
                Mesure reel = MesureDelegate.findMesure(mz.getUid());
                List<Destocker> lsdx = DestockerDelegate.findByProduitLot(choosenProduct.getUid(), choosenStockLot.getNumlot());
                List<Destocker> lsd = fullMesureRecqs(lsdx);
                double sortie = Util.sumDestockerQuantInPc(lsd);
                double entree = choosenStockLot.getQuantite() * reel.getQuantContenu();
                double dispo = entree - sortie;
                double converted = dispo / choosenM.getQuantContenu();
                tf_cout_unitr_cump_dstk.setText(String.valueOf(choosenStockLot.getCoutAchat()));
                tf_quant_disponible.setText(String.valueOf(converted));
                value_stock.setText(String.valueOf(BigDecimal.valueOf(converted * cumpx).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                double stalertx = (choosenStockLot.getStockAlerte() * reel.getQuantContenu());
                double choose = stalertx / choosenM.getQuantContenu();
                txt_stk_alerte.setText("Alert : " + choose);
            }
        });
        cbx_choose_product_dstk.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null : object.getNomProduit() + " " + object.getMarque() + " " + object.getModele() + " "
                        + (object.getTaille() == null ? "" : object.getTaille()) + " " + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_choose_product_dstk.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " " + object.getMarque() + " " + object.getModele() + " "
                        + (object.getTaille() == null ? "" : object.getTaille()) + " " + (object.getCouleur() == null ? "" : object.getCouleur())
                        + " " + object.getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_choose_product_dstk.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) -> {
            choosenProduct = newValue;
            if (choosenProduct == null) {
                return;
            }
            List<Mesure> mzs = MesureDelegate.findMesureByProduit(choosenProduct.getUid());
            //Util.findMesureForProduitWithId(store.findAll(), choosenProduct.getUid());
            if (lismesure != null) {
                lismesure.setAll(mzs);
            }
            if (lisstocker != null) {
                lisstocker.clear();
            }
            if (meth.equals("ppps")) {

                List<Stocker> lsks = StockerDelegate.toFefoOrdering(choosenProduct.getUid());
                for (Stocker lsk : lsks) {
                    if (lsk.getNumlot() == null) {
                        lsk.setNumlot("Lot:" + Constants.TIMESTAMPED_FORMAT.format(lsk.getDateStocker()));
                    }
                    lisstocker.add(lsk);
                }
            } else if (meth.equals("fifo")) {
                List<Stocker> lsks = StockerDelegate.toFifoOrdering(choosenProduct.getUid());
                for (Stocker lsk : lsks) {
                    if (lsk.getNumlot() == null) {
                        lsk.setNumlot("Lot:" + Constants.TIMESTAMPED_FORMAT.format(lsk.getDateStocker()));
                    }
                    lisstocker.add(lsk);
                }
            } else if (meth.equals("lifo")) {
                List<Stocker> lsks = StockerDelegate.toLifoOrdering(choosenProduct.getUid());
                for (Stocker lsk : lsks) {
                    if (lsk.getNumlot() == null) {
                        lsk.setNumlot("Lot:" + Constants.TIMESTAMPED_FORMAT.format(lsk.getDateStocker()));
                    }
                    lisstocker.add(lsk);
                }
            }

            cbx_choose_mesure_dstk.getSelectionModel().selectFirst();
            cbx_stock_lots.getSelectionModel().selectFirst();
            choosenM = cbx_choose_mesure_dstk.getValue();
            if (choosenStockLot == null) {
                MainUI.notify(null, bundle.getString("error"), bundle.getString("nostockmsg"), 4, "error");
            }
            if (choosenM == null) {
                MainUI.notify(null, bundle.getString("error"), bundle.getString("pleaseselectmez"), 4, "error");
                return;
            }
            choosenStockLot = lisstocker.get(0);
            if (choosenStockLot == null) {
                return;
            }

            double cumpx = choosenStockLot.getCoutAchat();
            Mesure mz = choosenStockLot.getMesureId();
            Mesure reel = MesureDelegate.findMesure(mz.getUid());
            List<Destocker> lsdx = DestockerDelegate.findByProduitLot(choosenProduct.getUid(), choosenStockLot.getNumlot());
            List<Destocker> lsd = fullMesureRecqs(lsdx);

            double sortie = DestockerDelegate.sum(choosenProduct.getUid());//Util.sumDestockerQuantInPc(lsd);
            double entree = StockerDelegate.sum(choosenProduct.getUid());
            //choosenStockLot.getQuantite() * reel.getQuantContenu();
            double dispo = entree - sortie;
            double converted = dispo / choosenM.getQuantContenu();
            tf_cout_unitr_cump_dstk.setText(String.valueOf(choosenStockLot.getCoutAchat()));
            tf_quant_disponible.setText(String.valueOf(converted));
            value_stock.setText(String.valueOf(BigDecimal.valueOf(converted * cumpx).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
            double stalertx = (choosenStockLot.getStockAlerte() * reel.getQuantContenu());
            double choose = stalertx / choosenM.getQuantContenu();
            txt_stk_alerte.setText("Alert : " + choose);
        });

        listview_dstks.setCellFactory((ListView<Destocker> param) -> new ListCell<Destocker>() {
            @Override
            protected void updateItem(Destocker item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Produit p = Util.findProduit(lisproduit, item.getProductId().getUid());
                            Mesure m = MesureDelegate.findMesure(item.getMesureId().getUid());
                            setText(p.getNomProduit() + " " + p.getMarque() + " " + item.getQuantite() + " " + m.getDescription()
                                    + " Dest : " + item.getDestination() + bundle.getString("xlibelle") + " : " + item.getLibelle());
                        }
                    });
                }
            }

        });
        listview_dstks.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Destocker>() {
            @Override
            public void changed(ObservableValue<? extends Destocker> observable, Destocker oldValue, Destocker newValue) {
//                setStockToUpdate(newValue);
            }
        });
        tf_quantite_dstk.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (tf_cout_unitr_cump_dstk.getText().isEmpty() || newValue == null) {
                    return;
                }
                try {
                    double cu = Double.parseDouble(tf_cout_unitr_cump_dstk.getText());
                    double qt = Double.parseDouble(newValue);
                    coutLigne = qt * cu;
                    txt_somme_ct_dstk.setText("Total : " + coutLigne);
                } catch (NumberFormatException e) {

                }
            }
        });
        cbx_stock_lots.getSelectionModel().selectFirst();
        cbx_stock_lots.setConverter(new StringConverter<Stocker>() {
            @Override
            public String toString(Stocker object) {
                return object == null ? null : object.getNumlot();
            }

            @Override
            public Stocker fromString(String string) {
                return cbx_stock_lots.getItems()
                        .stream()
                        .filter(object -> (object.getNumlot())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

    }

    private int isStockExpired(Stocker e) {
        long now = System.currentTimeMillis();
        long exp = e.getDateExpir().getTime();
        long un_mois = Constants.UN_MOIS;
        long interval = exp - now;
        long mois3 = (un_mois * 3);
        long mois6 = (un_mois * 6);
        long mois12 = (un_mois * 12);
        if (interval <= 0) {
            System.out.println("0");
            return -1;
        } else if (interval <= un_mois) {
            System.out.println("1");
            return 1;
        } else if (interval <= mois3) {
            System.out.println("3");
            return 3;
        } else if (interval <= mois6) {
            System.out.println("6");
            return 6;
        } else if (interval <= mois12) {
            System.out.println("12");
            return 12;
        } else {
            return 555;
        }
    }

    public void setAction(String actionx) {
        this.action = actionx;

    }

    public void setDestocker(Destocker dx) {
        if (dx == null) {
            return;
        }

        lismesure = FXCollections.observableArrayList();
        this.choosenDestocker = dx;
        choosenProduct = dx.getProductId();
        cbx_choose_product_dstk.setValue(choosenProduct);
        tf_cout_unitr_cump_dstk.setText(String.valueOf(dx.getCoutAchat()));
        txt_reference_dstk.setText(dx.getReference());
        List<Mesure> mzs = MesureDelegate.findMesureByProduit(choosenProduct.getUid());
        //Util.findMesureForProduitWithId(store.findAll(), choosenProduct.getUid());
        if (lismesure != null) {
            lismesure.setAll(mzs);
        }

        //To change body of generated methods, choose Tools | Templates.
    }

    private static class RecquisitionManager {

        Set<Recquisition> recqusition;

        public RecquisitionManager() {
            recqusition = new HashSet<>();
        }

        public boolean addRecqusition(Recquisition r) {
            if (findbyLotAndProduit(r.getProductId().getUid(), r.getNumlot()) == null) {
                return recqusition.add(r);
            }
            return false;
        }

        public boolean removeRecquisition(Recquisition r) {
            return recqusition.remove(r);
        }

        public Recquisition findbyLotAndProduit(String pro, String lot) {
            for (Recquisition rq : recqusition) {
                if (rq.getNumlot().equals(lot) && rq.getProductId().getUid().equals(pro)) {
                    return rq;
                }
            }
            return null;
        }

        public List<Recquisition> findForProduit(String prod) {
            List<Recquisition> rst = new ArrayList<>();
            for (Recquisition r : rst) {
                if (r.getProductId().getUid().equals(prod)) {
                    rst.add(r);
                }
            }
            return rst;
        }

        private boolean isEmpty() {
            return recqusition.isEmpty();
        }

        private void clean() {
            recqusition.clear();
        }

    }

}
