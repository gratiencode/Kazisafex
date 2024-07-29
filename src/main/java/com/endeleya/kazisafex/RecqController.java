/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.DestockerDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
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
import javafx.scene.control.Button;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class RecqController implements Initializable {

    Preferences pref;

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
    private TextField tf_prix_vent_req;
    @FXML
    private TextField tf_quant_max_req;
    @FXML
    private TextField tf_quant_min_req;
    @FXML
    private ComboBox<String> cbx_devise_req;
    @FXML
    private ListView<PrixDeVente> list_prix_devente_req;
    @FXML
    private Label txt_equivalent_req;
    @FXML
    private Label txt_lot_req, txt_lot_req1;
    @FXML
    private ComboBox<Mesure> cbx_mesure_vente_req;
    @FXML
    private Button saveBtn;

    ResourceBundle bundle;
    ObservableList<PrixDeVente> lsprices;
    ObservableList<Mesure> lsmesureq;
    ObservableList<Destocker> lsdestocker;
    ObservableList<Produit> lsproduit;

    private String action;
    private Recquisition recquisition;
    PrixDeVente Pv;
    private Produit p;
    private Mesure mesureRecq;
    private Mesure mesurePv;
    private Destocker destocker;
    private double taux2change;
    ObservableList<PrixDeVente> choosenPrices;
    String role, region;

    private static RecqController instance;
    Kazisafe ksf;

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

    public void setup(Entreprise eze, String action) {
        this.action = action;
        lsproduit = FXCollections.observableArrayList(ProduitDelegate.findProduits());
        lsdestocker = FXCollections.observableArrayList(DestockerDelegate.findDestockers());
        lsmesureq = FXCollections.observableArrayList(MesureDelegate.findMesures());
        lsprices = FXCollections.observableArrayList();
        list_prix_devente_req.setItems(lsprices);
        cbx_ref_req.setItems(filter(lsdestocker, lsproduit, region));
        ksf = KazisafeServiceFactory.createService(pref.get("token", null));
        if (this.action.equals(Constants.ACTION_UPDATE)) {
            saveBtn.setText(this.bundle.getString("xbtn.update"));
        }
        ContextMenu cm = new ContextMenu();
        MenuItem mi = new MenuItem("Supprimer");
        cm.getItems().add(mi);
        list_prix_devente_req.setContextMenu(cm);
        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (Pv == null) {
                    return;
                }
                lsprices.remove(Pv);
                PrixDeVenteDelegate.deletePrixDeVente(Pv);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(Pv, tools.Constants.ACTION_DELETE, Tables.PRIXDEVENTE);
                        });
            }
        });
        new ComboBoxAutoCompletion<>(cbx_ref_req);
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
            recquisition = req;
            List<Destocker> dstks = DestockerDelegate.findByReference(recquisition.getReference());
            if (dstks.isEmpty()) {
                return;
            }
            Destocker detk = dstks.get(0);
            cbx_ref_req.setValue(detk);
            tf_alerte_req.setText(String.valueOf(req.getStockAlert()));
            tf_quant_req.setText(String.valueOf(req.getQuantite()));
            if (action != null) {
                if (this.action.equals(Constants.ACTION_UPDATE)) {
                    saveBtn.setText(this.bundle.getString("xbtn.update"));
                }
            }
            Date dat = req.getDateExpiry();
            if (dat != null) {
                DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
                dpk_date_expiry_req.setValue(Constants.Datetime.toLocalDate(dat));
            }
            choosenPrices = FXCollections.observableArrayList(PrixDeVenteDelegate.findPricesForRecq(req.getUid()));
            System.out.println("Choosen rq price " + choosenPrices.size());
            lsprices.addAll(choosenPrices);
            list_prix_devente_req.setItems(lsprices);
        }
    }

    public void config() {
        cbx_ref_req.setConverter(new StringConverter<Destocker>() {
            @Override
            public String toString(Destocker object) {
                if (object == null) {
                    return null;
                }
                Produit p = Util.findProduit(lsproduit, object.getProductId().getUid());
                String numlot = object.getNumlot();
                if (numlot == null) {
                    object.setNumlot("Lot:" + tools.Constants.TIMESTAMPED_FORMAT.format(object.getDateDestockage()));
                }
                return object.getDestination() + " " + p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + object.getNumlot() + " " + Constants.Datetime.format(object.getDateDestockage());
            }

            @Override
            public Destocker fromString(String string) {
                return cbx_ref_req.getItems()
                        .stream()
                        .filter(v -> (v.getDestination() + " " + v.getProductId().getNomProduit() + " " + v.getProductId().getMarque() + " " + v.getProductId().getModele()
                        + " " + v.getNumlot() + " " + Constants.dateFormater.format(v.getDateDestockage()))
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
        cbx_mesure_vente_req.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure_vente_req.getItems()
                        .stream()
                        .filter(v -> (v.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_devise_req.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_devise_req.getSelectionModel().selectFirst();
        list_prix_devente_req.setCellFactory((ListView<PrixDeVente> param) -> new ListCell<PrixDeVente>() {
            @Override
            protected void updateItem(PrixDeVente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Mesure mzr = MesureDelegate.findMesure(item.getMesureId().getUid());
                    String mzdesc = mzr.getDescription();
                    setText("De " + item.getQmin() + " à " + item.getQmax() + " " + mzdesc + " : " + item.getPrixUnitaire() + " " + item.getDevise());
                }
            }

        });

        cbx_ref_req.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Destocker> observable, Destocker oldValue, Destocker newValue) -> {
            destocker = newValue;
            if (destocker == null) {
                MainUI.notify(null, "Erreur", "Ce produit est au stock depot, et n'a jamais ete destocke! Destockez-le d'abord", 5, "error");
            }
            p = Util.findProduit(lsproduit, destocker.getProductId().getUid());
            List<Recquisition> reqs = RecquisitionDelegate.findByReference(p.getUid(), destocker.getReference());
            if (!reqs.isEmpty()) {
                recquisition = reqs.get(0);
                action = Constants.ACTION_UPDATE;
            }
            tf_prod_req.setText(p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()) + " " + (p.getCouleur() == null ? "" : p.getCouleur()));
            cbx_mesure_req.setItems(FXCollections.observableArrayList(Util.findMesureForProduitWithId(lsmesureq, p.getUid())));
            cbx_mesure_vente_req.setItems(FXCollections.observableArrayList(Util.findMesureForProduitWithId(lsmesureq, p.getUid())));
            tf_quant_req.setText(String.valueOf(destocker.getQuantite()));
            txt_lot_req.setText(destocker.getNumlot());
            txt_lot_req1.setText(String.valueOf(destocker.getCoutAchat()));
            List<Stocker> sts = StockerDelegate.findStockerByProduitLot(p.getUid(), destocker.getNumlot());
            if (sts.isEmpty()) {

                sts = StockerDelegate.findAscSortedByDateStock(p.getUid());
            }
            if (sts.isEmpty()) {
                List<Destocker> lsdk = DestockerDelegate.findAscSortedByDate(p.getUid());
                sts = StockerDelegate.findAscSortedByDateStock(p.getUid());
//                sts = db.findWithAndClause(Stocker.class, new String[]{"dateStocker", "product_id"}, new String[]{Constants.dateFormater.format(lsdk.get(0).getDateDestockage()), p.getUid()});
            }

            List<Stocker> unull = unullifyLot(sts);
            if (unull.isEmpty()) {
                return;
            }

            Stocker st = unull.get(0);
            Date exp = st.getDateExpir();
            LocalDate ldate = exp == null ? null : Constants.Datetime.toLocalDate(exp);
            dpk_date_expiry_req.setValue(ldate);
            Mesure m = Util.findMesure(lsmesureq, destocker.getMesureId());
            cbx_mesure_req.getSelectionModel().select(m);
        });
        cbx_mesure_req.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            mesureRecq = newValue;
        });
        cbx_mesure_vente_req.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            mesurePv = newValue;
        });
        tf_prix_vent_req.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.isEmpty()) {
                    return;
                }
                if (cbx_devise_req.getValue().equals("USD")) {
                    //eq en fc
                    txt_equivalent_req.setText(BigDecimal.valueOf((taux2change * Double.parseDouble(tf_prix_vent_req.getText()))).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " Fc");
                } else {
                    txt_equivalent_req.setText(BigDecimal.valueOf(Double.parseDouble(tf_prix_vent_req.getText()) / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " $ us");
                    //eq en usd
                }
            }
        });
        list_prix_devente_req.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PrixDeVente>() {
            @Override
            public void changed(ObservableValue<? extends PrixDeVente> observable, PrixDeVente oldValue, PrixDeVente newValue) {
                Pv = newValue;
                if (Pv != null) {
                    tf_quant_min_req.setText(String.valueOf(Pv.getQmin()));
                    tf_quant_max_req.setText(String.valueOf(Pv.getQmax()));
                    tf_prix_vent_req.setText(String.valueOf(Pv.getPrixUnitaire()));
                    Mesure mzr = MesureDelegate.findMesure(Pv.getMesureId().getUid());
                    cbx_mesure_vente_req.setValue(mzr);
                }
            }
        });
    }

    private List<Stocker> unullifyLot(List<Stocker> nlstoks) {
        List<Stocker> result = new ArrayList<>();
        for (Stocker nlstok : nlstoks) {
            if (nlstok.getNumlot() == null) {
                nlstok.setNumlot("Lot:" + tools.Constants.TIMESTAMPED_FORMAT.format(nlstok.getDateStocker()));
            }
            result.add(nlstok);
        }
        return result;
    }

    @FXML
    public void createPrice(ActionEvent evt) {
        if (tf_quant_min_req.getText().isEmpty()
                || tf_quant_max_req.getText().isEmpty()
                || tf_prix_vent_req.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Veuillez completer tout les chmaps relatifs au a la configuration \n des prix de vente", 3, "error");
            return;
        }

        Mesure mzd = Util.findMesure(lsmesureq, destocker.getMesureId());

        double ppcd = (destocker.getCoutAchat() / mzd.getQuantContenu());
        double qdstpc = mzd.getQuantContenu() * destocker.getQuantite();
        double pcin = (mesureRecq.getQuantContenu() * Double.parseDouble(tf_quant_req.getText()));
        if (pcin > qdstpc) {
            MainUI.notify(null, "Erreur", "Quantité récquisitionnée superieur à celle destockée", 5, "error");
            return;
        }
        if (mesurePv == null) {
            MainUI.notify(null, "Erreur", "Veuillez selectionner une mesure puis continuer", 5, "error");
        }
        double pvmez = mesurePv.getQuantContenu();
        double pvu = Double.parseDouble(tf_prix_vent_req.getText());
        double ppcr = (pvu / pvmez);
        if (ppcr <= ppcd) {
            MainUI.notify(null, "Attention!", "Le prix de vente proposé est inferieur au coût d'achat de (" + ppcd + " la piece )", 5, "warning");
            return;
        }

        String pid = Pv == null ? DataId.generate() : Pv.getUid();
        System.out.println("Action intake " + mzd + " pid " + pid);
        PrixDeVente pv = new PrixDeVente(pid);
        pv.setDevise(cbx_devise_req.getValue());
        pv.setPrixUnitaire(pvu);
        pv.setQmin(Double.parseDouble(tf_quant_min_req.getText()));
        pv.setQmax(Double.parseDouble(tf_quant_max_req.getText()));
        pv.setMesureId(mesurePv);
        PrixDeVente px = Util.findPrice(lsprices, pv.getUid());

        if (px != null) {
            int index = Util.getIndex(lsprices, px.getUid());
            System.out.println("Action intake index " + index + " pid " + pid + " " + px);
            lsprices.add(pv);
            lsprices.remove(index);

            return;
        }
        lsprices.add(pv);
    }

    @FXML
    public void applySamePrice(Event evt) {
        if (destocker != null) {
            Produit prod = destocker.getProductId();
            if (prod != null) {
                Recquisition req = RecquisitionDelegate.findRecquisitionByProduit(p.getUid(), destocker.getNumlot()).get(0);
                List<PrixDeVente> prices = PrixDeVenteDelegate.findPricesForRecq(req.getUid());
                for (PrixDeVente pv : prices) {
                    lsprices.add(pv);
                }
            }
        }

    }

    @FXML
    public void createRecqusition(ActionEvent evt) {

        if (tf_alerte_req.getText().isEmpty() || tf_prod_req.getText().isEmpty() || lsprices.isEmpty()
                || tf_quant_req.getText().isEmpty() || dpk_date_req.getValue() == null || mesureRecq == null) {
            MainUI.notify(null, "Erreur", "Veuillez completez tout les elements necessaire y compris les prix de vente", 4, "error");
            return;
        }
        if (this.action.equals(tools.Constants.ACTION_CREATE)) {
            recquisition = new Recquisition(DataId.generate());
        }

        recquisition.setMesureId(mesureRecq);
        recquisition.setRegion(region);
        recquisition.setNumlot(destocker.getNumlot());
        recquisition.setCoutAchat(destocker.getCoutAchat());
        recquisition.setDate(tools.Constants.Datetime.toUtilDate(dpk_date_req.getValue()));
        if (dpk_date_expiry_req.getValue() != null) {
            recquisition.setDateExpiry(tools.Constants.Datetime.toUtilDate(dpk_date_expiry_req.getValue()));
        }
        recquisition.setProductId(p);
        recquisition.setQuantite(Double.parseDouble(tf_quant_req.getText()));
        recquisition.setReference(destocker.getReference());
        recquisition.setStockAlert(Double.parseDouble(tf_alerte_req.getText()));
        recquisition.setObservation(destocker.getObservation());
        Recquisition req;
        if (this.action.equals(tools.Constants.ACTION_CREATE)) {
            req = RecquisitionDelegate.saveRecquisition(recquisition);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(req, action, Tables.RECQUISITION);
                    });
            saveRecqusitionByHttp(req);
            if (req != null) {
                for (PrixDeVente pvu : lsprices) {
                    PrixDeVente pv = PrixDeVenteDelegate.findPrixDeVente(pvu.getUid());
                    if (pv == null) {
                        pvu.setRecquisitionId(recquisition);
                        PrixDeVente spv = PrixDeVenteDelegate.savePrixDeVente(pvu);
                        Executors.newCachedThreadPool()
                                .submit(() -> {
                                    Util.sync(spv, action, Tables.PRIXDEVENTE);
                                });
                    } else {
                        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                            pvu.setRecquisitionId(recquisition);
                            PrixDeVente upv = PrixDeVenteDelegate.updatePrixDeVente(pvu);
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(upv, tools.Constants.ACTION_UPDATE, Tables.PRIXDEVENTE);
                                    });
                        }
                    }
                    savePriceByHttp(pv);
                }
                lsprices.clear();
                MainuiController.getInstance().switchToPos(evt);
                MainUI.notify(null, "Succes!", "Récquisition enregistrée avec succès", 3, "Info");
               
            }
        } else {
            if (role.equals(Role.Trader.name()) || role.equals(Role.Manager.name()) || role.equals(Role.Magazinner.name()) || role.contains(Role.ALL_ACCESS.name())) {
                req = RecquisitionDelegate.updateRecquisition(recquisition);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(req, tools.Constants.ACTION_UPDATE, Tables.RECQUISITION);
                        });
                if (req != null) {
                    for (PrixDeVente pvu : lsprices) {
                        PrixDeVente pv = PrixDeVenteDelegate.findPrixDeVente(pvu.getUid());
                        if (pv == null) {
                            pvu.setRecquisitionId(recquisition);
                            PrixDeVente pvx = PrixDeVenteDelegate.savePrixDeVente(pvu);
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(pvx,
                                                tools.Constants.ACTION_CREATE, Tables.PRIXDEVENTE);
                                    });
                        } else {
                            pv.setRecquisitionId(recquisition);
                            PrixDeVente pvx = PrixDeVenteDelegate.updatePrixDeVente(pv);
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(pvx, tools.Constants.ACTION_UPDATE, Tables.PRIXDEVENTE);
                                    });
                        }
                    }
                    lsprices.clear();
                    MainUI.notify(null, "Succes!", "Récquisition modifiée avec succès", 3, "Info");
                    // MainuiController.getInstance().startSync();
                }
            } else {
                MainUI.notify(null, "Echec!", "La récquisition n'a pas été modifiée;\nVous n'avez pas les privilèges réquis ", 3, "error");
            }
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

    private void saveRecqusitionByHttp(Recquisition r) {
        ksf.saveRecquisition(r).enqueue(new Callback<Recquisition>() {
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

}
