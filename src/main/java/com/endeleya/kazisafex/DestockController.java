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
import delegates.PrixDeVenteDelegate;
import delegates.PermissionDelegate;
import data.PermitTo;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import data.Production;
import data.Produit;
import data.Recquisition;
import data.StockDepotAgregate;
import data.Stocker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.ComboBoxAutoCompletion;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.ComboBoxAutoCompletion;
import tools.Tables;
import tools.Util;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.ProductionDelegate;
import services.StockDepotAgregateService;

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
    private Label lbl_cout_achat;
    @FXML
    private Label lbl_prix_vente;
    @FXML
    private TextField tf_quantite_dstk, tf_quant_disponible;
    @FXML
    private TextField tf_cout_unitr_cump_dstk, tf_observation;
    @FXML
    private ComboBox<Mesure> cbx_choose_mesure_dstk;
    @FXML
    private Label txt_stk_alerte, alveol, value_stock;
    @FXML
    private DatePicker dpk_date_dstk;
    @FXML
    private ComboBox<String> cbx_destination_dstk;
    @FXML
    private CheckBox save_req;
    @FXML
    private Button btn_add_destock;
    @FXML
    private TableView<Destocker> tb_destock_list;
    @FXML
    private Label lbl_total_cout_achat;
    @FXML
    private Label lbl_total_prix_vente;
    @FXML
    private ComboBox<Stocker> cbx_stock_lots;
    @FXML
    private Label txt_date_expiry, methode;
    @FXML
    private TableColumn<Destocker, String> col_reference;
    @FXML
    private TableColumn<Destocker, String> col_produit;
    @FXML
    private TableColumn<Destocker, String> col_lot;
    @FXML
    private TableColumn<Destocker, String> col_date_peremption;
    @FXML
    private TableColumn<Destocker, Double> col_cout_achat;
    @FXML
    private TableColumn<Destocker, Double> col_prix_vente;
    @FXML
    private TableColumn<Destocker, String> col_quantite_mesure;
    @FXML
    private TableColumn<Destocker, Double> col_cout_total;
    @FXML
    private TableColumn<Destocker, Double> col_prix_total;
    @FXML
    private TableColumn<Destocker, Double> col_marge;
    @FXML
    private TableColumn<Destocker, String> col_region;
    @FXML
    private Label txt_somme_global_dstk;
    @FXML
    private Label txt_count_dstk;
    @FXML
    private Label txt_somme_ct_dstk;

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
    double totalCoutAchat = 0;
    double totalPrixVente = 0;
    String action, token, entr;
    Destocker choosenDestocker;
    Mesure choosenM;
    Produit choosenProduct;
    Stocker lastock;
    Preferences pref;
    Stocker choosenStockLot;
    StockDepotAgregateService stockDepotService;

    ObservableList<String> regions;
    ObservableList<Produit> lisproduit;
    ObservableList<Stocker> lisstocker;
    ObservableList<Destocker> lisdestocker, lsdin;
    ObservableList<Mesure> lismesure;

    private static DestockController instance;
    private Kazisafe kazisafe;
    String region, role, meth;
    ResourceBundle bundle;
    Recquisition req;
    List<PrixDeVente> prices;
    double taux;

    public DestockController() {
        instance = this;
    }

    public void setDatabase(Entreprise eze) {
        kazisafe = KazisafeServiceFactory.createService(token);
        lisdestocker = FXCollections.observableArrayList(DestockerDelegate.findDestockers());
        lismesure = FXCollections.observableArrayList();
        lsdin = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        cbx_destination_dstk.setItems(regions);
        lisproduit = FXCollections.observableArrayList(ProduitDelegate.findProduits());
        cbx_choose_product_dstk.setItems(lisproduit);
        new ComboBoxAutoCompletion<>(cbx_choose_product_dstk);
        lisstocker = FXCollections.observableArrayList();

        if (kazisafe != null) {
            kazisafe.getRegions().enqueue(new retrofit2.Callback<List<String>>() {
                @Override
                public void onResponse(retrofit2.Call<List<String>> call, retrofit2.Response<List<String>> rspns) {
                    if (rspns.isSuccessful() && rspns.body() != null) {
                        javafx.application.Platform.runLater(() -> {
                            regions.clear();
                            regions.addAll(rspns.body());
                        });
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<List<String>> call, Throwable thrwbl) {
                }
            });
        }
        cbx_choose_mesure_dstk.setItems(lismesure);
        txt_reference_dstk.setText("DST" + ((int) (Math.random() * 100000)) + "K");
        tb_destock_list.setItems(lsdin);
        this.action = Constants.ACTION_CREATE;

        stockDepotService = new StockDepotAgregateService();

        col_reference.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getReference()));
        col_produit.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getProductId() != null ? cellData.getValue().getProductId().getNomProduit() : ""));
        col_lot.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNumlot()));
        col_date_peremption.setCellValueFactory(cellData -> {
            Destocker d = cellData.getValue();
            String dateExp = "";
            List<Stocker> stockers = StockerDelegate.findStockerByProduitLot(d.getProductId().getUid(), d.getNumlot());
            if (!stockers.isEmpty() && stockers.get(0).getDateExpir() != null) {
                dateExp = stockers.get(0).getDateExpir().toString();
            }
            return new javafx.beans.property.SimpleStringProperty(dateExp);
        });
        col_cout_achat.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getCoutAchat()));
        col_prix_vente.setCellValueFactory(cellData -> {
            Destocker d = cellData.getValue();
            double prixVente = 0.0;
            List<Stocker> stockers = StockerDelegate.findStockerByProduitLot(d.getProductId().getUid(), d.getNumlot());
            if (!stockers.isEmpty()) {
                prixVente = stockers.get(0).getPrixVenteEstime();
            }
            return new javafx.beans.property.SimpleObjectProperty<>(prixVente);
        });
        col_quantite_mesure.setCellValueFactory(cellData -> {
            Destocker d = cellData.getValue();
            String mesure = d.getMesureId() != null ? d.getMesureId().getDescription() : "";
            return new javafx.beans.property.SimpleStringProperty(d.getQuantite() + " " + mesure);
        });
        col_cout_total.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getCoutAchat() * cellData.getValue().getQuantite()));
        col_prix_total.setCellValueFactory(cellData -> {
            Destocker d = cellData.getValue();
            double prixVente = 0.0;
            List<Stocker> stockers = StockerDelegate.findStockerByProduitLot(d.getProductId().getUid(), d.getNumlot());
            if (!stockers.isEmpty()) {
                prixVente = stockers.get(0).getPrixVenteEstime();
            }
            return new javafx.beans.property.SimpleObjectProperty<>(prixVente * d.getQuantite());
        });
        col_marge.setCellValueFactory(cellData -> {
            Destocker d = cellData.getValue();
            double prixVente = 0.0;
            List<Stocker> stockers = StockerDelegate.findStockerByProduitLot(d.getProductId().getUid(), d.getNumlot());
            if (!stockers.isEmpty()) {
                prixVente = stockers.get(0).getPrixVenteEstime();
            }
            double marge = prixVente - d.getCoutAchat();
            return new javafx.beans.property.SimpleObjectProperty<>(marge);
        });
        col_region.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getRegion()));
        cbx_stock_lots.setItems(lisstocker);
        cbx_devise_req1.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_devise_req1.getSelectionModel().selectFirst();
        prices = new ArrayList<>();
        regions.add("Déclassement de stock");
        kazisafe.getRegions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.setAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        pref.put("region" + (++i), reg);
                    }
                    regions.add("Déclassement de stock");

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
                        MainUI.notify(null, bundle.getString("error"),
                                "Completer la quantite et la destination puis continuer", 3, "error");
                        save_req.setSelected(false);
                        prices.clear();
                        return;
                    }
                    pricepane.setVisible(true);
                }
            }
        });

        tf_prix_de_vente.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue.isEmpty() || newValue == null) {
                        return;
                    }
                    try {
                        if (cbx_devise_req1.getValue().equals("USD")) {
                            double data = Double.parseDouble(newValue);
                            double enCdf = BigDecimal.valueOf(data * taux).setScale(2, RoundingMode.HALF_EVEN)
                                    .doubleValue();
                            txt_equivalentCdf.setText(enCdf + " Fc");
                        } else {
                            double data = Double.parseDouble(newValue);
                            double enCdf = BigDecimal.valueOf(data / taux).setScale(2, RoundingMode.HALF_EVEN)
                                    .doubleValue();
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
                    LocalDate date = choosenStockLot.getDateExpir();
                    txt_date_expiry.setText(date == null ? bundle.getString("noperish") : "Exp : " + date.toString());

                    txt_date_expiry.setBackground(new Background(
                            new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
                    if (date == null) {
                        return;
                    }

                    Mesure mz = choosenStockLot.getMesureId();
                    List<Destocker> lsdx = DestockerDelegate.findByProduitLot(choosenProduct.getUid(),
                            choosenStockLot.getNumlot());
                    List<Destocker> lsd = fullMesureRecqs(lsdx);
                    double sortie = Util.sumDestockerQuantInPc(lsd);
                    Mesure mreel = MesureDelegate.findMesure(mz.getUid());
                    double entree = choosenStockLot.getQuantite() * mreel.getQuantContenu();
                    double dispo = entree - sortie;

                    double stalertx = (choosenStockLot.getStockAlerte() * mreel.getQuantContenu());
                    double choose = stalertx / choosenM.getQuantContenu();
                    txt_stk_alerte.setText("Alert : " + choose);

                    double converted = dispo / choosenM.getQuantContenu();
                    lbl_cout_achat.setText(String.format("%.2f USD", choosenStockLot.getCoutAchat()));
                    tf_cout_unitr_cump_dstk.setText(String.valueOf(choosenStockLot.getCoutAchat()));
                    
                    updateGlobalStockDisplay();

                    List<Recquisition> reqs = RecquisitionDelegate.findByReference(choosenProduct.getUid(),
                            choosenStockLot.getLivraisId().getReference());
                    if (!reqs.isEmpty()) {
                        Recquisition req = reqs.get(0);
                        List<PrixDeVente> prix = PrixDeVenteDelegate.findPrixDeVentes(0.0, mz.getUid(), req.getUid());
                        if (!prix.isEmpty()) {
                            double prixVente = prix.get(0).getPrixUnitaire();
                            lbl_prix_vente.setText(String.format("%.2f %s", prixVente, prix.get(0).getDevise()));
                        } else {
                            lbl_prix_vente.setText("0.0 USD");
                        }
                    } else {
                        lbl_prix_vente.setText("0.0 USD");
                    }

                    int exp = isStockExpired(choosenStockLot);
                    if (exp == -1) {
                        txt_date_expiry.setBackground(new Background(
                                new BackgroundFill(Color.web("#f58282"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 3) {
                        txt_date_expiry.setBackground(new Background(
                                new BackgroundFill(Color.web("#c46506"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 6) {
                        txt_date_expiry.setBackground(new Background(
                                new BackgroundFill(Color.web("#f7fa61"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 12) {
                        txt_date_expiry.setBackground(new Background(
                                new BackgroundFill(Color.web("#c5e6b3"), new CornerRadii(20), new Insets(4))));
                    } else {
                        txt_date_expiry.setBackground(new Background(
                                new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
                    }
                    txt_date_expiry.setStyle(
                            "-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 2;");
                }
            }
        });
        cbx_choose_product_dstk.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Produit>() {
            @Override
            public void changed(ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) {
                if (newValue != null) {
                    choosenProduct = newValue;
                    lbl_cout_achat.setText("0.0 USD");
                    lbl_prix_vente.setText("0.0 USD");
                    tf_quantite_dstk.clear();
                    tf_quant_disponible.clear();
                    cbx_choose_mesure_dstk.setValue(null);
                    txt_stk_alerte.setText("Alert: 0");

                    List<Stocker> stocks = StockerDelegate.findStockerByProduit(newValue.getUid());
                    lisstocker.clear();
                    lisstocker.addAll(stocks);

                    List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(newValue.getUid());
                    lismesure.clear();
                    lismesure.addAll(mesures);
                    cbx_choose_mesure_dstk.setItems(lismesure);
                    
                    updateGlobalStockDisplay();
                }
            }
        });
    }

    private void updateGlobalStockDisplay() {
        if (choosenProduct == null) {
            tf_quant_disponible.clear();
            return;
        }

        double dispoGlobalEnPc = stockDepotService.getAvailableStock(choosenProduct.getUid(), region);

        // Soustraire lsdin local
        for (Destocker pending : lsdin) {
            if (pending.getProductId().getUid().equals(choosenProduct.getUid())) {
                Mesure pm = MesureDelegate.findMesure(pending.getMesureId().getUid());
                dispoGlobalEnPc -= pending.getQuantite() * pm.getQuantContenu();
            }
        }

        if (choosenM != null && choosenM.getQuantContenu() != null && choosenM.getQuantContenu() > 0) {
            double converted = dispoGlobalEnPc / choosenM.getQuantContenu();
            tf_quant_disponible.setText(String.format(java.util.Locale.US, "%.2f", converted));

            // Value
            double cumpx = choosenStockLot != null ? choosenStockLot.getCoutAchat() : 0.0;
            value_stock.setText(String.format(java.util.Locale.US, "%.2f", converted * cumpx));
        } else {
            tf_quant_disponible.setText(String.format(java.util.Locale.US, "%.2f (en Pc)", dispoGlobalEnPc));
        }
    }

    @FXML
    public void clearPrices(Event e) {
        tilepn_prices1.getChildren().clear();
        prices.clear();
    }

    @FXML
    public void addRecquisit(Event e) {
        List<PrixDeVente> lps = new ArrayList<>();
        Recquisition reqx = new Recquisition(DataId.generate());
        reqx.setCoutAchat(Double.parseDouble(tf_cout_unitr_cump_dstk.getText()));
        reqx.setDate(dpk_date_dstk.getValue().atStartOfDay());
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
        closeFloatingPane(e);
        prices.clear();
        save_req.setSelected(false);
    }

    @FXML
    public void applyOldPrice(Event e) {
        // Implementation logic for applying old price
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
        double ppcd = Double.parseDouble(tf_cout_unitr_cump_dstk.getText());
        double caupc = BigDecimal.valueOf(ppcd / stm.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN)
                .doubleValue();
        double pvu = Double.parseDouble(tf_prix_de_vente.getText());
        double rapv = (ppcd / pvu);
        if (rapv >= raps) {
            MainUI.notify(null, bundle.getString("warning"), String.format(bundle.getString("xlowerprice"), caupc), 5,
                    "warning");
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
        String price = pv.getQmin() + m + " à " + pv.getQmax() + m + " pour " + pv.getPrixUnitaire() + " "
                + pv.getDevise();
        Label l = new Label();
        l.setContextMenu(contM);
        l.setPrefWidth(250);
        l.setPrefHeight(14);
        l.setId(pv.getUid());
        l.setTextAlignment(TextAlignment.CENTER);
        l.setTextFill(Color.rgb(255, 255, 255));
        l.setBackground(new Background(
                new BackgroundFill(Color.rgb(0x7, 0x7, 0xf, 0.3), new CornerRadii(5.0), new Insets(-5.0))));
        l.setPadding(new Insets(4, 4, 4, 4));
        l.setText(price);
        l.setTooltip(new Tooltip(price));
        tilep.getChildren().add(l);
        prices.add(pv);
        MainUI.notify(null, "Succes", "Prix ajouté avec succès.", 2, "info");

        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                PrixDeVente p = find(l.getId());
                prices.remove(p);
                tilep.getChildren().remove(l);
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
                Recquisition rec = (Recquisition) obj;
                Mesure m = MesureDelegate.findMesure(rec.getMesureId().getUid());
                rec.setMesureId(m);
                result.add((T) rec);
            } else if (obj instanceof LigneVente) {
                LigneVente lv = (LigneVente) obj;
                Mesure m = MesureDelegate.findMesure(lv.getMesureId().getUid());
                lv.setMesureId(m);
                result.add((T) lv);
            } else if (obj instanceof Destocker) {
                Destocker ds = (Destocker) obj;
                Mesure m = MesureDelegate.findMesure(ds.getMesureId().getUid());
                ds.setMesureId(m);
                result.add((T) ds);
            } else if (obj instanceof Stocker) {
                Stocker st = (Stocker) obj;
                Mesure m = MesureDelegate.findMesure(st.getMesureId().getUid());
                st.setMesureId(m);
                result.add((T) st);
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        configcbx();
        MainUI.setPattern(dpk_date_dstk);
        dpk_date_dstk.setValue(LocalDate.now());

        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Supprimer");
        MenuItem menuItem2 = new MenuItem("Modifier");
        pricepane.setVisible(false);
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        tb_destock_list.setContextMenu(contextMenu);

        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!PermissionDelegate.hasPermission(PermitTo.DELETE_DESTOCKER) && !role.contains("ALL_ACCESS")) {
                    MainUI.notify(null, "Accès refusé", "Vous n'avez pas la permission de supprimer un destockage.", 3, "error");
                    return;
                }
                Destocker selected = tb_destock_list.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous supprimer ce destockage ?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            DestockerDelegate.deleteDestocker(selected);
                            StockerDelegate.rectifyStockDepot(selected.getProductId(), selected.getDateDestockage().toLocalDate(), selected.getRegion(), selected.getCoutAchat());
                            StockDepotAgregateService agService = new StockDepotAgregateService();
                            agService.addStock(selected);

                            lisdestocker.remove(selected);
                            Util.sync(selected, Constants.ACTION_DELETE, Tables.DESTOCKER);
                            MainUI.notify(null, "Succès", "Destockage supprimé.", 2, "info");
                        }
                    });
                }
            }
        });

        menuItem2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!PermissionDelegate.hasPermission(PermitTo.UPDATE_DESTOCKER) && !role.contains("ALL_ACCESS")) {
                    MainUI.notify(null, "Accès refusé", "Vous n'avez pas la permission de modifier un destockage.", 3, "error");
                    return;
                }
                Destocker selected = tb_destock_list.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    setDestocker(selected);
                    action = Constants.ACTION_UPDATE;
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
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
    }

    @FXML
    public void addDestocker(ActionEvent event) {
        String reg = cbx_destination_dstk.getValue();
        if (dpk_date_dstk.getValue() == null || choosenProduct == null
                || tf_quantite_dstk.getText().isEmpty() || cbx_destination_dstk.getValue() == null) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("fillnoptional"), 5, "error");
            return;
        }

        double qte;
        try {
            qte = Double.parseDouble(tf_quantite_dstk.getText());
        } catch (NumberFormatException ex) {
            MainUI.notify(null, bundle.getString("error"), "Quantité invalide", 3, "error");
            return;
        }
        if (qte <= 0) {
            MainUI.notify(null, bundle.getString("error"), "La quantité doit être supérieure à zéro", 3, "error");
            return;
        }
        if (choosenM == null) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("choosemez"), 3, "error");
            return;
        }

        if (choosenStockLot == null) {
            MainUI.notify(null, "Erreur", "Veuillez sélectionner un lot", 3, "error");
            return;
        }

        // --- Stock Availability Check ---
        double dispoGlobalEnPc = stockDepotService.getAvailableStock(choosenProduct.getUid(), region);
        if (dispoGlobalEnPc <= 0) {
            StockDepotAgregate latest = stockDepotService.findLatestStockDepotAgregate(choosenProduct.getUid(), region);
            if (latest == null) {
                // Fallsback to raw tables
                double entree = StockerDelegate.sum(choosenProduct.getUid());
                double sortie = DestockerDelegate.sum(choosenProduct.getUid());
                dispoGlobalEnPc = entree - sortie;
            }
        }

        // Account for items already in the list to avoid double counting
        for (Destocker pending : lsdin) {
            if (pending.getProductId().getUid().equals(choosenProduct.getUid())) {
                Mesure pm = MesureDelegate.findMesure(pending.getMesureId().getUid());
                dispoGlobalEnPc -= pending.getQuantite() * pm.getQuantContenu();
            }
        }

        double qteEnPc = qte * choosenM.getQuantContenu();
        if (qteEnPc > dispoGlobalEnPc) {
            double maxDispo = dispoGlobalEnPc / (choosenM.getQuantContenu() != null ? choosenM.getQuantContenu() : 1.0);
            MainUI.notify(null, "Stock insuffisant",
                    String.format("Stock global disponible : %.2f %s", maxDispo, choosenM.getDescription()),
                    5, "error");
            return;
        }

        if (this.action.equals(Constants.ACTION_CREATE)) {
            Destocker s = new Destocker(DataId.generate());
            s.setCoutAchat(choosenStockLot.getCoutAchat());
            s.setDateDestockage(dpk_date_dstk.getValue().atStartOfDay());
            s.setQuantite(qte);
            s.setDestination(reg);
            s.setMesureId(choosenM);
            s.setNumlot(choosenStockLot.getNumlot());
            s.setProductId(choosenProduct);
            s.setReference(txt_reference_dstk.getText());
            String obs = tf_observation.getText();
            s.setLibelle(obs.isEmpty() ? bundle.getString("unstockingof") + " : " + s.getDateDestockage() : obs);
            s.setObservation(obs.isEmpty() ? "R.A.S" : obs);
            s.setRegion(region);

            // SAVE AND RECORD IMMEDIATELY
            Destocker saved = DestockerDelegate.saveDestocker(s);
            if (saved != null) {
                // Update local list for display
                lsdin.add(0, saved);
                
                // Synchronize via HTTP directly
                saveDestockerWithRetry(saved);
                
                // Update StockDepotAgregate and legacy stock tables
                stockDepotService.removeStock(saved);
                StockerDelegate.rectifyStockDepot(saved.getProductId(), saved.getDateDestockage().toLocalDate(), saved.getRegion(), saved.getCoutAchat());
                
                // Update UI Sum
                double total = 0;
                for (Destocker d : lsdin) {
                    total += d.getCoutAchat() * d.getQuantite();
                }
                cglobal = total; // Sync internal field
                txt_somme_global_dstk.setText(String.format("Total : %.2f", total));
                txt_count_dstk.setText(String.valueOf(lsdin.size()) + " article(s)");
                
                MainUI.notify(null, "Succès", "Destockage enregistré avec succès", 4, "info");
                
                // Clear inputs for next entry
                tf_quantite_dstk.clear();
                tf_observation.clear();
            }
        } else if (this.action.equals(Constants.ACTION_UPDATE)) {
            if (choosenDestocker == null) {
                return;
            }

            // Correction: put back old stock before validating/applying new one
            stockDepotService.addStock(choosenDestocker);
            
            choosenDestocker.setDateDestockage(dpk_date_dstk.getValue().atStartOfDay());
            choosenDestocker.setQuantite(qte);
            choosenDestocker.setDestination(reg);
            choosenDestocker.setMesureId(choosenM);
            choosenDestocker.setObservation(tf_observation.getText());
            choosenDestocker.setCoutAchat(Double.parseDouble(tf_cout_unitr_cump_dstk.getText()));

            Destocker updated = DestockerDelegate.updateDestocker(choosenDestocker);
            if (updated != null) {
                StockerDelegate.rectifyStockDepot(updated.getProductId(), updated.getDateDestockage().toLocalDate(), updated.getRegion(), updated.getCoutAchat());
                stockDepotService.removeStock(updated);
                saveDestockerWithRetry(updated);
                
                int index = lsdin.indexOf(choosenDestocker);
                if (index != -1) {
                    lsdin.set(index, updated);
                }
                
                double total = 0;
                for (Destocker d : lsdin) {
                    total += d.getCoutAchat() * d.getQuantite();
                }
                cglobal = total;
                txt_somme_global_dstk.setText(String.format("Total : %.2f", total));
                
                MainUI.notify(null, "Succès", "Destockage mis à jour.", 2, "info");
            }
        }
        resetFields();
    }

    /**
     * Sauvegarde tous les destockers ajoutés dans la liste locale (lsdin) en BD.
     * Inspiré du pattern saveStock de StoreformController.
     */
    @FXML
    public void saveAllDestockers(ActionEvent event) {
        if (lsdin.isEmpty()) {
            MainUI.notify(null, "Erreur", "Rien n'a été saisi, rien n'a été enregistré", 3, "error");
            return;
        }

        for (Destocker s : lsdin) {
            Destocker saved = DestockerDelegate.saveDestocker(s);
            StockerDelegate.rectifyStockDepot(saved.getProductId(), saved.getDateDestockage().toLocalDate(), saved.getRegion(), saved.getCoutAchat());
            stockDepotService.removeStock(saved);
            saveDestockerWithRetry(saved);
            Util.sync(saved, Constants.ACTION_CREATE, Tables.DESTOCKER);
            lisdestocker.add(0, saved);
        }

        MainUI.notify(null, "Succès", lsdin.size() + " destockage(s) enregistré(s) avec succès.", 3, "info");
        lsdin.clear();
        cglobal = 0;
        txt_somme_global_dstk.setText("Total : 0.00");
        txt_count_dstk.setText("0 article(s)");
        txt_reference_dstk.setText("DST" + ((int) (Math.random() * 100000)) + "K");
    }

    private void resetFields() {
        this.action = Constants.ACTION_CREATE;
        this.choosenDestocker = null;
        tf_quantite_dstk.clear();
        tf_observation.clear();
        txt_reference_dstk.setText("DST" + ((int) (Math.random() * 100000)) + "K");
        cbx_choose_product_dstk.getSelectionModel().clearSelection();
        cbx_stock_lots.getSelectionModel().clearSelection();
        tf_quant_disponible.clear();
        value_stock.setText("0");
    }

    private List<Production> findProduction(Destocker d) {
        Produit p = d.getProductId();
        String lot = d.getNumlot();
        List<Production> pr = ProductionDelegate.findProductionByProduitLot(lot, p.getUid());
        return pr.isEmpty() ? null : pr;
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
                return cbx_choose_mesure_dstk.getItems().stream()
                        .filter(f -> f.getDescription().equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_destination_dstk.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            tf_observation.setPromptText(newV.equalsIgnoreCase("Déclassement de stock") ? bundle.getString("xcol.observation") : bundle.getString("xtxtf.prompt.observation"));
        });
        cbx_choose_mesure_dstk.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            choosenM = newValue;
            if (choosenM != null && choosenProduct != null && choosenStockLot != null) {
                double cumpx = choosenStockLot.getCoutAchat();
                Mesure reel = MesureDelegate.findMesure(choosenStockLot.getMesureId().getUid());
                List<Destocker> lsdx = DestockerDelegate.findByProduitLot(choosenProduct.getUid(), choosenStockLot.getNumlot());
                updateGlobalStockDisplay();
                txt_stk_alerte.setText("Alert : " + (choosenStockLot.getStockAlerte() * reel.getQuantContenu() / choosenM.getQuantContenu()));
            }
        });
        cbx_choose_product_dstk.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit o) {
                return o == null ? null : o.getNomProduit() + " " + o.getMarque() + " " + o.getModele() + " " + (o.getTaille() == null ? "" : o.getTaille()) + " " + (o.getCouleur() == null ? "" : o.getCouleur()) + " " + o.getCodebar();
            }

            @Override
            public Produit fromString(String s) {
                return cbx_choose_product_dstk.getItems().stream().filter(o -> (o.getNomProduit() + " " + o.getMarque() + " " + o.getModele() + " " + (o.getTaille() == null ? "" : o.getTaille()) + " " + (o.getCouleur() == null ? "" : o.getCouleur()) + " " + o.getCodebar()).equalsIgnoreCase(s)).findFirst().orElse(null);
            }
        });
        cbx_choose_product_dstk.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            choosenProduct = newValue;
            if (choosenProduct == null) {
                return;
            }
            lismesure.setAll(MesureDelegate.findMesureByProduit(choosenProduct.getUid()));
            lisstocker.clear();
            List<Stocker> lsks = meth.equals("ppps") ? StockerDelegate.toFefoOrdering(choosenProduct.getUid()) : (meth.equals("fifo") ? StockerDelegate.toFifoOrdering(choosenProduct.getUid()) : StockerDelegate.toLifoOrdering(choosenProduct.getUid()));
            for (Stocker lsk : lsks) {
                if (lsk.getNumlot() == null) {
                    lsk.setNumlot("Lot:" + Constants.TIMESTAMPED_FORMAT.format(lsk.getDateStocker()));
                }
                lisstocker.add(lsk);
            }
            cbx_choose_mesure_dstk.getSelectionModel().selectFirst();
            cbx_stock_lots.getSelectionModel().selectFirst();
            if (lisstocker.isEmpty()) {
                MainUI.notify(null, bundle.getString("error"), bundle.getString("nostockmsg"), 4, "error");
                return;
            }
            choosenStockLot = lisstocker.get(0);
            double sortie = DestockerDelegate.sum(choosenProduct.getUid());
            double entree = StockerDelegate.sum(choosenProduct.getUid());
            double dispo = entree - sortie;
            double converted = dispo / (choosenM != null ? choosenM.getQuantContenu() : 1);
            tf_cout_unitr_cump_dstk.setText(String.valueOf(choosenStockLot.getCoutAchat()));
            tf_quant_disponible.setText(String.valueOf(converted));
            value_stock.setText(String.valueOf(BigDecimal.valueOf(converted * choosenStockLot.getCoutAchat()).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        });
        cbx_stock_lots.setConverter(new StringConverter<Stocker>() {
            @Override
            public String toString(Stocker o) {
                return o == null ? null : o.getNumlot();
            }

            @Override
            public Stocker fromString(String s) {
                return cbx_stock_lots.getItems().stream().filter(o -> o.getNumlot().equalsIgnoreCase(s)).findFirst().orElse(null);
            }
        });
    }

    private int isStockExpired(Stocker e) {
        long now = System.currentTimeMillis();
        long exp = Constants.Datetime.dateInMillis(e.getDateExpir());
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

    public void setAction(String actionx) {
        this.action = actionx;
    }

    public void setDestocker(Destocker dx) {
        if (dx == null) {
            return;
        }
        this.choosenDestocker = dx;
        choosenProduct = dx.getProductId();
        cbx_choose_product_dstk.setValue(choosenProduct);
        tf_cout_unitr_cump_dstk.setText(String.valueOf(dx.getCoutAchat()));
        txt_reference_dstk.setText(dx.getReference());
        lismesure.setAll(MesureDelegate.findMesureByProduit(choosenProduct.getUid()));
    }

    private void saveDestockerWithRetry(Destocker destocker) {
        Executors.newSingleThreadExecutor().submit(() -> {
            int attempt = 0;
            while (attempt < MAX_RETRY) {
                try {
                    if (trySaveDestocker(destocker)) {
                        break;
                    }
                    sendProduitIfNotExist(ProduitDelegate.findProduit(destocker.getProductId().getUid()), MesureDelegate.findMesureByProduit(destocker.getProductId().getUid()));
                } catch (IOException e) {
                    System.err.println("Erreur: " + e.getMessage());
                }
                attempt++;
                try {
                    TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, attempt));
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    private boolean trySaveDestocker(Destocker ds) throws IOException {
        return kazisafe.syncDestocker(ds.getUid(), ds.getDateDestockage().toString(), ds.getReference(), ds.getDestination(), ds.getRegion(), Double.toString(ds.getCoutAchat()), Double.toString(ds.getQuantite()), ds.getLibelle(), ds.getObservation(), ds.getMesureId().getUid(), ds.getProductId().getUid(), ds.getNumlot()).execute().code() == 200;
    }

    private void sendProduitIfNotExist(Produit p, List<Mesure> m) {
        String base64 = Base64.getEncoder().encodeToString(p.getImage() != null ? p.getImage() : loadDefaultImage());
        saveProduitByHttp(p, base64, m);
    }

    private byte[] loadDefaultImage() {
        try (InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png")) {
            return is != null ? is.readAllBytes() : new byte[0];
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private void saveProduitByHttp(Produit p, String img, List<Mesure> m) {
        try {
            data.ProduitHelper h = new data.ProduitHelper();
            h.setUid(p.getUid());
            h.setCategoryId(p.getCategoryId() != null ? p.getCategoryId().getUid() : null);
            h.setCodebar(p.getCodebar());
            h.setNomProduit(p.getNomProduit());
            h.setMarque(p.getMarque());
            h.setModele(p.getModele());
            h.setImage("data:image/jpeg;base64," + img);
            h.setMesureList(m);
            kazisafe.saveLite(h).execute();
        } catch (IOException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static final int MAX_RETRY = 3;
}
