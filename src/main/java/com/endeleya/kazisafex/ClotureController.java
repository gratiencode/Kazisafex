/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.endeleya.kazisafex;

import data.Entreprise;
import data.LigneVente;
import data.Periode;
import data.Mesure;
import data.Produit;
import data.Recquisition;
import data.Vente;
import data.core.KazisafeServiceFactory;
import data.helpers.CardHelper;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.ClientDelegate;
import delegates.LigneVenteDelegate;
import delegates.PeriodeDelegate;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.VenteDelegate;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import retrofit2.Call;
import retrofit2.Callback;
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
public class ClotureController implements Initializable {

    public static ClotureController getInstance() {
        return instance;
    }
    ResourceBundle bundle;
    private static ClotureController instance;
    @FXML
    private ComboBox<String> cbx_region;
    @FXML
    private DatePicker dpk_cloture_debut;
    @FXML
    private DatePicker dpk_cloture_fin;
    @FXML
    private ComboBox<Produit> cbx_cloture_produit;
    @FXML
    private Label txt_stock_final;
    @FXML
    private TextField tf_cloture_stock_initial;
    @FXML
    private ComboBox<Mesure> cbx_cloture_mesure;
    @FXML
    private Label txt_cloture_ecart;
    Kazisafe kazisafe;

    ObservableList<Produit> lsproduits;
    ObservableList<Mesure> lsmesures;
    ObservableList<String> regions;
    double stockFinal;
    Mesure mesureFinale;
    ToggleGroup mouvGroup;

    String region, role;
    Preferences pref; 
    @FXML
    private CheckBox chbx_cloture_apply2all;
    private String entr;
    @FXML
    private RadioButton rbtn_journalier;
    @FXML
    private RadioButton rbtn_annuelle;
    @FXML
    private RadioButton rbtn_mensuelle;
    @FXML
    private ProgressIndicator pgsind_progess_cloture;
    @FXML
    private Label txt_cloture_progress;
    @FXML
    private TableView<Periode> tbl_clotures_cloture;
    @FXML
    private TextField tf_cloture_recherche;
    @FXML
    private MenuButton mnubtn_save;
    @FXML
    private Label txt_cloture_element_count;
    @FXML
    private TableColumn<Periode, String> col_date;
    @FXML
    private TableColumn<Periode, String> col_type_periode;
    @FXML
    private TableColumn<Periode, String> col_date_debut;
    @FXML
    private TableColumn<Periode, String> col_date_fin;
    @FXML
    private TableColumn<Periode, String> col_produit;
    @FXML
    private TableColumn<Periode, String> col_SI;
    @FXML
    private TableColumn<Periode, String> col_SF;
    @FXML
    private TableColumn<Periode, String> col_Ec;
    @FXML
    private TableColumn<Periode, String> col_status;
    @FXML
    private TableColumn<Periode, String> col_region;
    @FXML
    private MenuItem menuaction_openperiod;
    @FXML
    private MenuItem menuaction_closeperiod;
    @FXML
    private MenuItem menuitem_cancel;

    public ClotureController() {
        instance = this;
    }

    ObservableList<Periode> olc;

    Entreprise entrep;
    Produit produit;
    Periode choosen;

    ExecutorService executor;

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
        // TODO
        mouvGroup = new ToggleGroup();
        kazisafe = KazisafeServiceFactory.createService(pref.get("token", null));
        entr = pref.get("eUid", "");
        role = pref.get("priv", null);
        region = pref.get("region", "...");
        lsmesures = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        lsproduits = FXCollections.observableArrayList(ProduitDelegate.findProduits());
        olc = FXCollections.observableArrayList();
        MainUI.setPattern(dpk_cloture_fin);
        MainUI.setPattern(dpk_cloture_debut);
        initUi();
        txt_cloture_element_count.setText("(" + olc.size() + ") Lignes");
    }

    @FXML
    public void filterByDate(Event evt) {
        if (dpk_cloture_fin.getValue() != null && dpk_cloture_debut.getValue() != null) {
            List<Periode> items = PeriodeDelegate.findByLocalDates(dpk_cloture_debut.getValue(),
                    dpk_cloture_fin.getValue());
            olc.setAll(items); 
        } else {
            olc.setAll(PeriodeDelegate.findPeriodes());
        }
        txt_cloture_element_count.setText("(" + olc.size() + ") Lignes");
    }

    private void initUi() {
        executor = Executors.newSingleThreadExecutor();
        rbtn_annuelle.setToggleGroup(mouvGroup);
        rbtn_journalier.setToggleGroup(mouvGroup);
        rbtn_mensuelle.setToggleGroup(mouvGroup);
        cbx_cloture_produit.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null : object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_cloture_produit.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                        + (object.getModele() == null ? "" : object.getModele()) + " " + (object.getTaille() == null ? "" : object.getTaille()) + " "
                        + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_cloture_produit.setItems(lsproduits);
        cbx_cloture_mesure.setItems(lsmesures);
        cbx_region.setItems(regions);
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            cbx_region.setVisible(true);
        } else {
            cbx_region.setVisible(false);
        }
        cbx_cloture_mesure.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_cloture_mesure.getItems()
                        .stream()
                        .filter(f -> (f.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_region.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
                    if (t1 != null) {
                        region = t1;
                    }
        });
        cbx_cloture_produit.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends Produit> ov, Produit t, Produit t1) -> {
                    if (t1 != null) {
                        List<Mesure> mes = MesureDelegate.findMesureByProduit(t1.getUid());
                        lsmesures.setAll(mes);
                        produit = t1;
                        CardHelper cah;
                        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                            cah = RecquisitionDelegate.makeCardFor(t1);
                        } else {
                            cah = RecquisitionDelegate.makeCardFor(t1, region);
                        }

                        if (cah == null) {
                            txt_stock_final.setText("En stock : 0");
                            tf_cloture_stock_initial.setText("0");
                            cbx_cloture_mesure.getSelectionModel().selectFirst();
                            System.out.println("CAH value " + cah);
                            MainUI.notify(null, "Votre attention", "Les stock de ce " + t1.getNomProduit() + " est soit epuise ou non approvisione", 8, "warn");
                            return;
                        }

                        stockFinal = cah.getRemainedQuantity();
                        mesureFinale = cah.getRemainedMesure();
                        cbx_cloture_mesure.setValue(mesureFinale);
                        txt_stock_final.setText("En stock : " + stockFinal);
                        tf_cloture_stock_initial.setText(String.valueOf(stockFinal));
                    }
                });
        ComboBoxAutoCompletion<Produit> comx = new ComboBoxAutoCompletion<>(cbx_cloture_produit);
        tf_cloture_stock_initial.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            if (!t1.isEmpty()) {
                try {
                    double data = Double.parseDouble(t1);
                    double ecart = data - stockFinal;
                    txt_cloture_ecart.setText("Ecart entre stock final theorique et initial physique : " + ecart);
                } catch (NumberFormatException e) {
                    MainUI.notify(null, "Votre attention", "Vous devez mettre uniquement les chiffres", 8, "error");
                }
            }
        });
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
        initTable();
        olc.setAll(PeriodeDelegate.findPeriodes());
        tf_cloture_recherche.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                if (t1.isEmpty()) {
                    tbl_clotures_cloture.setItems(olc);
                    txt_cloture_element_count.setText("(" + olc.size() + ") Lignes");
                    return;
                }
                ObservableList<Periode> result = FXCollections.observableArrayList();
                for (Periode cloture : olc) {
                    Produit p = cloture.getProductId();
                    String el = p.getNomProduit() + " " + p.getMarque() + " " + (p.getModele() == null ? "" : p.getModele()) + " " + (p.getTaille() == null ? "" : p.getTaille()) + " " + (p.getCouleur() == null ? "" : p.getCouleur());
                    el = el.concat(" " + cloture.getComment() + " " + cloture.getMouvement() + " " + cloture.getRegion());
                    if (el.toUpperCase().contains(t1.toUpperCase())) {
                        result.add(cloture);
                    }
                }
                tbl_clotures_cloture.setItems(result);
                txt_cloture_element_count.setText("(" + result.size() + ") Lignes");
            }
        });
        menuaction_openperiod.setOnAction(evt -> {
            pgsind_progess_cloture.setVisible(true);
            txt_cloture_progress.setVisible(true);
            txt_cloture_progress.setText("L'operation d'ouverture des stocks produits en cours...");
            LocalDate d1,d2;
            String context;
            if(dpk_cloture_debut.getValue()==null || dpk_cloture_fin.getValue()==null){
                d1=d2=LocalDate.now();
                context="Journalier du "+d1;
            }else{
                d1=dpk_cloture_debut.getValue();
                d2=dpk_cloture_fin.getValue();
                context="Intervale du "+d1+" au "+d2;
            }
            
            MainuiController.getInstance().cloturer(d1, d2,context);
        });
    }

    private boolean isPeriodValid(LocalDate debut, LocalDate fin, String cment) {
        LocalDate rep = debut.plusDays(1);
        LocalDate an = debut.plusDays(debut.lengthOfYear());
        LocalDate mo=debut.plusDays(debut.lengthOfMonth()); 
        if (rep.equals(fin) && cment.equals(Constants.PERIODE_JOURNALIERE)) {
            return true;
        } else if (mo.equals(fin) && cment.equals(Constants.PERIODE_MENSUELLE)) {
            return true;
        } else if (an.equals(fin) && cment.equals(Constants.PERIODE_ANNUELLE)) {
            return true;
        }
        return false;
    }

    @FXML
    public void annulerTache(Event e) {
        
    }

    private void ajusterStock(Produit prod, double ecart, Mesure m, String region) {
        System.out.println("Ecart " + ecart);
        CardHelper card;
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            card = RecquisitionDelegate.makeCardFor(prod);
        } else {
            card = RecquisitionDelegate.makeCardFor(prod, region);
        }
        if (card == null) {
            return;
        }
        String ref = "INV-#" + ((int) (Math.random() * 100000));
        Produit pr = card.getRecquisition().getProductId();
        String lot = card.getRecquisition().getNumlot();
        if (ecart > 0) {
            //Nouveau Recquis

            Recquisition req = new Recquisition(DataId.generate());
            req.setCoutAchat(card.getRecquisition().getCoutAchat());
            req.setDate(LocalDateTime.now());
            req.setQuantite(ecart);
            req.setRegion(region);
            req.setDateExpiry(card.getRecquisition().getDateExpiry());
            req.setMesureId(m);
            req.setProductId(pr);
            req.setStockAlert(card.getRecquisition().getStockAlert());
            req.setNumlot(lot);
            req.setObservation("Importation Inventaire");
            req.setReference(ref);
            RecquisitionDelegate.saveRecquisition(req);
        } else if (ecart < 0) {
            //Nouvelle vente

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
            v.setRegion(region);
            v.setObservation("CORRECTION");
            VenteDelegate.saveVente(v);

            double q = Math.abs(ecart);
            LigneVente lv = new LigneVente(DataId.generateLong());
            lv.setClientId("-");
            lv.setMesureId(m);
            lv.setMontantCdf(0d);
            lv.setMontantUsd(0);
            lv.setNumlot(lot);
            lv.setPrixUnit(0d);
            lv.setProductId(pr);
            lv.setQuantite(q);
            lv.setReference(v);
            LigneVenteDelegate.saveLigneVente(lv);
        }
    }

    @FXML
    public void onHoverHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.setShadowEffect(img);
    }

    @FXML
    public void onOutHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.removeShaddowEffect(img);
    }

    @FXML
    public void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    @FXML
    public void deletePeriode(Event evt) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez vous supprimer les periodes selectionnees ?", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        List<Periode> items = tbl_clotures_cloture.getSelectionModel().getSelectedItems();
        Optional<ButtonType> clkbtn = alert.showAndWait();
        if (clkbtn.get() == ButtonType.YES && !items.isEmpty()) {
            items.forEach(i -> {
                PeriodeDelegate.deletePeriode(i);
                tbl_clotures_cloture.getItems().removeAll(i);
            });
            MainUI.notify(null, "Succes", "Suppression des periodes faite avec succes", 8, "info");
        }
    }

    private String comment() {
        if (rbtn_annuelle.isSelected()) {
            return Constants.PERIODE_ANNUELLE;
        } else if (rbtn_journalier.isSelected()) {
            return Constants.PERIODE_JOURNALIERE;
        } else if (rbtn_mensuelle.isSelected()) {
            return Constants.PERIODE_MENSUELLE;
        }
        return null;
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

    public void setEntreprise(Entreprise entreprise) {
        this.entrep = entreprise;
        if (entr == null) {
            entr = entreprise.getUid();
        }
    }

    public void initTable() {
        col_produit.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getNomProduit() + " " + p.getMarque() + " " + (p.getModele() == null ? "" : p.getModele()) + " " + (p.getTaille() == null ? "" : p.getTaille()) + " " + (p.getCouleur() == null ? "" : p.getCouleur()));
        });
        col_date.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            return new SimpleStringProperty(lv.getNow().toString());
        });
        col_date_debut.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            return new SimpleStringProperty(lv.getDateDebut().toString());
        });
        col_date_fin.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            return new SimpleStringProperty(lv.getDateFin().toString());
        });
        col_type_periode.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            String p = lv.getComment();
            return new SimpleStringProperty(p);
        });
        col_SI.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            Double si = lv.getStockInitial();
            Mesure m = lv.getMesureId();
            return new SimpleStringProperty(si + " " + m.getDescription());
        });
        col_SF.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            Double sf = lv.getStockFinal();
            Mesure m = lv.getMesureId();
            return new SimpleStringProperty(sf + " " + m.getDescription());
        });
        col_Ec.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            Double ec = lv.getEcart();
            Mesure m = lv.getMesureId();
            return new SimpleStringProperty(ec + " " + m.getDescription());
        });
        col_status.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            var st = lv.getMouvement();
            return new SimpleStringProperty(st);
        });
        col_region.setCellValueFactory((TableColumn.CellDataFeatures<Periode, String> param) -> {
            Periode lv = param.getValue();
            var reg = lv.getRegion();
            return new SimpleStringProperty(reg);
        });
        tbl_clotures_cloture.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tbl_clotures_cloture.setItems(olc);
        tbl_clotures_cloture.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Periode> observable, Periode oldValue, Periode newValue) -> {
            if (newValue == null) {
                return;
            }
            choosen = newValue;
            String type = choosen.getComment();
            produit = choosen.getProductId();
            dpk_cloture_debut.setValue(choosen.getDateDebut());
            dpk_cloture_fin.setValue(choosen.getDateFin());
            switch (type) {
                case Constants.PERIODE_ANNUELLE ->
                    rbtn_annuelle.setSelected(true);
                case Constants.PERIODE_JOURNALIERE ->
                    rbtn_journalier.setSelected(true);
                case Constants.PERIODE_MENSUELLE ->
                    rbtn_mensuelle.setSelected(true);
                default -> {
                }
            }
            cbx_cloture_produit.getSelectionModel().select(produit);
        });
    }
}
