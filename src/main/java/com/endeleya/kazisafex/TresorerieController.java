/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.CompteTresorDelegate;
import delegates.DepenseDelegate;
import delegates.FactureDelegate;
import delegates.OperationDelegate;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
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
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import data.Client;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Entreprise;
import data.Facture;
import data.Operation;
import data.Refresher;
import data.Traisorerie;
import data.Vente;
import data.helpers.Mouvment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.ComboBoxAutoCompletion;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Transaction;
import tools.Util;
import data.helpers.Role;
import data.helpers.TypeTraisorerie;
import data.network.Kazisafe;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class TresorerieController implements Initializable {

    public static TresorerieController getInstance() {
        if (instance == null) {
            instance = new TresorerieController();
        }
        return instance;
    }
    @FXML
    ImageView imgbtn_clean;
    @FXML
    ProgressIndicator progress_cloud_search;
    @FXML
    private Tab tab_ecriture_trans;
    @FXML
    TabPane tab_pn_tresor;
    @FXML
    private TextField tf_montant_usd_trans;
    @FXML
    private RadioButton rdbtn_encaiss_trans;
    @FXML
    private RadioButton rdbtn_decaiss_trans;
    @FXML
    private TextField tf_montant_cdf_trans;
    @FXML
    private Label txt_reference_trans;
    @FXML
    private TextArea txtArea_motif_trans;
    @FXML
    private DatePicker dpk_date_trans;
    @FXML
    private TextField tf_obs_trans;
    @FXML
    private ComboBox<CompteTresor> cbx_compte_trans;
    @FXML
    private ComboBox<String> cbx_region;
    @FXML
    private Tab tab_depanse_trans;
    @FXML
    private TextField tf_montant_usd_dep;
    @FXML
    private TextField tf_montant_cdf_dep;
    @FXML
    private Label txt_reference_dep;
    @FXML
    private TextArea txtArea_motif_dep;
    @FXML
    private DatePicker dpk_date_dep;
    @FXML
    private TextField tf_obs_dep;
    @FXML
    private ComboBox<CompteTresor> cbx_compte_dep;
    @FXML
    ComboBox<Depense> cbx_depenses;
    @FXML
    private ComboBox<String> cbx_fonction_imput_dep;
    @FXML
    private Label txt_reference_trans11;
//    @FXML
//    private ListView<SMSNotification> list_sms_recov;
    @FXML
    private Tab tab_recov_trans;
    @FXML
    private Pane pane_delete;
    @FXML
    Pane comptePane;
    @FXML
    Pane depensePane;
    @FXML
    private TextField tf_montant_usd_recov;
    @FXML
    private TextField tf_montant_cdf_recov;
    @FXML
    private DatePicker dpk_date_recov;
    @FXML
    private DatePicker dpk_date_ech_recov;
    @FXML
    private TextField tf_obs_dep1;
    @FXML
    private ComboBox<CompteTresor> cbx_compte_recov;
    @FXML
    private TextField tf_reference_recov;
    @FXML
    private Label txt_eval_usd_recov;
    @FXML
    private Label txt_eval_cdf_recov;
    @FXML
    private Label txt_choosen_account_trans12;
    @FXML
    private Label txt_arembourser_usd_recov;
    @FXML
    private Label txt_arembourser_cdf_recov;
    @FXML
    private Label txt_client_recov;
    @FXML
    private Label txt_sum_arecov_recov;
    @FXML
    private Label txt_recoved_sum_recov;
    @FXML
    private Label txt_client_count;
    @FXML
    private TableView<Transaction> tbl_transaction;
    @FXML
    private TableColumn<Transaction, String> col_date_trans;
    @FXML
    private TableColumn<Transaction, String> col_libelle_trans, col_reference;
    @FXML
    private TableColumn<Transaction, Number> col_debit_usd_trans;
    @FXML
    private TableColumn<Transaction, Number> col_debit_cdf_trans;
    @FXML
    private TableColumn<Transaction, Number> col_credit_usd_trans;
    @FXML
    private TableColumn<Transaction, Number> col_credit_cdf_trans;
    @FXML
    private TableColumn<Transaction, Number> col_solde_usd_trans;
    @FXML
    private TableColumn<Transaction, Number> col_solde_cdf_trans;
    @FXML
    private Label txt_choosen_account_trans, txt_count_rappel;
    @FXML
    private DatePicker dpk_date_debut_trans;
    @FXML
    private DatePicker dpk_fin_trans;
    @FXML
    TextField intitule_copmpte;
    @FXML
    TextField numero_compte;
    @FXML
    TextField bankname;
    @FXML
    TextField soldeMin;
    @FXML
    TextField search_compte;
    @FXML
    ComboBox<String> cbx_type_compte;
    @FXML
    ComboBox<String> cbx_region_compte;

    @FXML
    ComboBox<String> cbx_rgion_deps;
    @FXML
    TextField depensename;
    @FXML
    TextField searchDeps;
    @FXML
    ListView<Depense> ls_depense;

    @FXML
    Label count_compte;
    @FXML
    Label count_depense;
    @FXML
    ListView<CompteTresor> tresors;
    Facture fact;

    Preferences pref;
    //JpaStorage database;

    ObservableList<Transaction> lstransaction;
//    ObservableList<SMSNotification> listSMS;
    ObservableList<String> regions;
    ObservableList<CompteTresor> comptes;
    ObservableList<Depense> depenses;
    List<Traisorerie> lstrz;
    List<Vente> lvente;
    Traisorerie trx;
    Vente vtx;
    double taux2change;
    double somme = 0;
    double cdf = 0, revertCdf, ff, fd;
    double usd = 0, revertUsd, dt;
    static double soldecdf, soldeusd;
    String math;
    Kazisafe kazisafe;
//    SMSNotification choosen;
    String token, region, role, entr;
    Entreprise entreprise;
    CompteTresor choosenComptetr;
    Depense choosenDepense;

    Depense d;

    private static TresorerieController instance;
    private ResourceBundle bundle;

    @FXML
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
        pref.putInt("tranzit_bill", -100);

    }

    public TresorerieController() {
        // database = JpaStorage.getInstance();
        lstransaction = FXCollections.observableArrayList();
        lstrz = new ArrayList<>();
        instance = this;
    }

    public void addTraisorerie(Traisorerie liv) {
        Traisorerie l = TraisorerieDelegate.findTraisorerie(liv.getUid());// database.findByUid(Traisorerie.class, liv.getUid());
        if (l == null) {
            l = TraisorerieDelegate.saveTraisorerie(liv);//database.insertOnly(liv);
        } else {
            l = TraisorerieDelegate.updateTraisorerie(liv);//database.updateOnly(liv);
        }
        lstrz.add(l);
        fillTransactions(lstrz);
    }

    public void addOperation(Operation liv) {
        Operation l = OperationDelegate.findOperation(liv.getUid());//database.findByUid(Operation.class, liv.getUid());
        if (l == null) {
            OperationDelegate.saveOperation(liv);//database.insertOnly(liv);
        } else {
            OperationDelegate.updateOperation(liv);//database.updateOnly(liv);
        }
    }

    public void addAccount(CompteTresor cpt) {
        CompteTresor cp = CompteTresorDelegate.findCompteTresor(cpt.getUid());//database.findByUid(CompteTresor.class, cpt.getUid());
        if (cp == null) {
            CompteTresorDelegate.saveCompteTresor(cpt);//database.insertOnly(cpt);
        } else {
            CompteTresorDelegate.updateCompteTresor(cpt);//database.updateOnly(cpt);
        }
    }

    public void addDepense(Depense d) {
        Depense cp = DepenseDelegate.findDepense(d.getUid());//database.findByUid(Depense.class, d.getUid());
        if (cp == null) {
            DepenseDelegate.saveDepense(d);//database.insertOnly(d);
        } else {
            DepenseDelegate.updateDepense(d);//database.updateOnly(d);
        }
    }

    @FXML
    public void saveCompteTr(Event e) {
        if (intitule_copmpte.getText().isEmpty()
                || numero_compte.getText().isEmpty()
                || bankname.getText().isEmpty()
                || cbx_type_compte.getValue() == null) {
            MainUI.notify(null, "Erreur", "Veuillez completer les champs obligatoires svp", 3, "error");
            return;
        }
        CompteTresor cpt;
        if (choosenComptetr == null) {
            cpt = new CompteTresor();
        } else {
            cpt = choosenComptetr;
        }
        cpt.setBankName(bankname.getText());
        cpt.setIntitule(intitule_copmpte.getText());
        cpt.setNumeroCompte(numero_compte.getText());
        cpt.setRegion(cbx_region_compte.getValue());
        cpt.setTypeCompte(cbx_type_compte.getValue());
        try {
            cpt.setSoldeMinimum(soldeMin.getText().isEmpty() ? 0 : Double.parseDouble(soldeMin.getText()));
        } catch (NumberFormatException ex) {
            MainUI.notify(null, "Erreur", "Le solde doit etre en chiffre svp", 3, "error");
            return;
        }
        if (choosenComptetr == null) {
            CompteTresor compte = CompteTresorDelegate.saveCompteTresor(cpt);//database.insertAndSync(cpt);
            if (compte != null) {
                comptes.add(compte);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(compte, Constants.ACTION_CREATE, Tables.COMPTETRESOR);
                        });
                MainUI.notify(null, "Succes", "Compte cree avec succes", 3, "info");

            }
        } else {
            CompteTresor compte = CompteTresorDelegate.updateCompteTresor(cpt);//database.update(cpt);
            if (compte != null) {
                comptes.set(comptes.indexOf(choosenComptetr), compte);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(compte, Constants.ACTION_UPDATE, Tables.COMPTETRESOR);
                        });
                MainUI.notify(null, "Succes", "Compte modifie avec succes", 3, "info");
            }
        }
        saveCompteTresorByHttp(cpt);
        choosenComptetr = null;
        bankname.clear();
        intitule_copmpte.clear();
        numero_compte.clear();
        soldeMin.clear();

    }

    @FXML
    public void saveNewDepense(Event e) {
        if (depensename.getText().isEmpty()
                || cbx_rgion_deps.getValue() == null) {
            MainUI.notify(null, "Erreur", "Veuillez completer les champs obligatoires svp", 3, "error");
            return;
        }
        Depense d;
        if (choosenDepense == null) {
            d = new Depense(DataId.generate());
        } else {
            d = choosenDepense;
        }
        d.setNomDepense(depensename.getText());
        d.setRegion(cbx_rgion_deps.getValue());
        if (choosenDepense == null) {
            Depense dx = DepenseDelegate.saveDepense(d);//database.insertAndSync(d);
            if (dx != null) {
                MainUI.notify(null, "Succes", "Depense creee avec succes", 3, "info");
                depenses.add(dx);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(dx, Constants.ACTION_CREATE, Tables.DEPENSE);
                        });
            }
        } else {
            Depense dx = DepenseDelegate.updateDepense(d);//database.update(d);
            if (dx != null) {
                MainUI.notify(null, "Succes", "Depense modifiee avec succes", 3, "info");
                depenses.set(depenses.indexOf(choosenDepense), dx);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(dx, Constants.ACTION_UPDATE, Tables.DEPENSE);
                        });
            }
        }
        saveDepenseByHttp(d);
        choosenDepense = null;
        depensename.clear();
    }

    private void configtab() {
        col_credit_cdf_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getCredit_cdf());
        });
        col_credit_usd_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getCredit_usd());
        });
        col_debit_cdf_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getDebit_cdf());
        });
        col_debit_usd_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getDebit_usd());
        });
        col_solde_cdf_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getSolde_cdf());
        });
        col_solde_usd_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Number> param) -> {
            return new SimpleDoubleProperty(param.getValue().getSolde_usd());
        });
        col_date_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, String> param) -> {
            return new SimpleStringProperty(Constants.DATE_HEURE_FORMAT.format(param.getValue().getDate()));
        });
        col_reference.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, String> param) -> {
            return new SimpleStringProperty(param.getValue().getReference());
        });
        col_libelle_trans.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, String> param) -> {
            return new SimpleStringProperty(param.getValue().getLibelle());
        });

        cbx_compte_trans.setConverter(new StringConverter<CompteTresor>() {
            @Override
            public String toString(CompteTresor item) {
                return item == null ? null : item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule();
            }

            @Override
            public CompteTresor fromString(String string) {
                return cbx_compte_trans.getItems()
                        .stream()
                        .filter(item -> (item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_compte_dep.setConverter(new StringConverter<CompteTresor>() {
            @Override
            public String toString(CompteTresor item) {
                return item == null ? null : item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule();
            }

            @Override
            public CompteTresor fromString(String string) {
                return cbx_compte_dep.getItems()
                        .stream()
                        .filter(item -> (item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_compte_recov.setConverter(new StringConverter<CompteTresor>() {
            @Override
            public String toString(CompteTresor item) {
                return item == null ? null : item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule();
            }

            @Override
            public CompteTresor fromString(String string) {
                return cbx_compte_recov.getItems()
                        .stream()
                        .filter(item -> (item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_depenses.setConverter(new StringConverter<Depense>() {
            @Override
            public String toString(Depense item) {
                return item == null ? null : item.getNomDepense();
            }

            @Override
            public Depense fromString(String string) {
                return cbx_depenses.getItems()
                        .stream()
                        .filter(item -> (item.getNomDepense())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        tresors.setCellFactory((ListView<CompteTresor> param) -> new ListCell<CompteTresor>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(CompteTresor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String com = item.getBankName() + " " + item.getTypeCompte() + ", " + item.getNumeroCompte() + " " + item.getIntitule();
                    setText(com);
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(new Image(TresorerieController.class.getResourceAsStream("/icons/revenu.png")));
                    setGraphic(imageView);
                }
            }

        });

        ls_depense.setCellFactory((ListView<Depense> param) -> new ListCell<Depense>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Depense item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String com = item.getNomDepense() + " " + item.getRegion();
                    setText(com);
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(new Image(TresorerieController.class.getResourceAsStream("/icons/depenses.png")));
                    setGraphic(imageView);
                }
            }

        });

    }

    @FXML
    public void showAccountForm(Event e) {
        tab_pn_tresor.getSelectionModel().select(tab_ecriture_trans);
        comptePane.setVisible(true);
    }

    @FXML
    public void showAccount2Form(Event e) {
        depensePane.setVisible(true);
    }

    @FXML
    private void goToClient(Event e) {
        token = pref.get("token", null);
        MainUI.floatDialog(tools.Constants.CLIENT_DLG, 521, 635, token, kazisafe, entreprise, region);
    }

    public void setUp(Entreprise eze, Vente v, Facture f) {
        token = pref.get("token", null);
        kazisafe = KazisafeServiceFactory.createService(token);
        this.entreprise = eze;
        regions = FXCollections.observableArrayList();
        comptes = FXCollections.observableArrayList();
        depenses = FXCollections.observableArrayList();
        cbx_region.setItems(regions);
        cbx_region_compte.setItems(regions);
        cbx_rgion_deps.setItems(regions);
        cbx_type_compte.setItems(FXCollections.observableArrayList(TypeTraisorerie.CAISSE.name(), TypeTraisorerie.BANQUE.name(), TypeTraisorerie.ELECTRONIQUE.name()));
        tresors.setItems(comptes);
        ls_depense.setItems(depenses);
        cbx_depenses.setItems(depenses);
        lstrz = new ArrayList<>();
        if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
            cbx_region.setVisible(true);
            lstrz.addAll(TraisorerieDelegate.findTraisoreries());
            comptes.addAll(CompteTresorDelegate.findCompteTresors());
            depenses.addAll(DepenseDelegate.findDepenses());
        } else {
            cbx_region.setVisible(false);
            lstrz.addAll(TraisorerieDelegate.findTraisoreries(region));//.fidatabase.findAllByRegion(Traisorerie.class, region));
            comptes.addAll(CompteTresorDelegate.findCompteTresors(region));//database.findAllByRegion(CompteTresor.class, region));
            depenses.addAll(DepenseDelegate.findDepenses(region));//database.findAllByRegion(Depense.class, region));
        }

        Platform.runLater(() -> {
            count_compte.setText(String.format(bundle.getString("xitems"), comptes.size()));
        });
        Platform.runLater(() -> {
            count_depense.setText(String.format(bundle.getString("xitems"), depenses.size()));
        });
        fillTransactions(lstrz);
//        listSMS = FXCollections.observableArrayList();
        math = String.valueOf(((int) (Math.random() * 100001)));
        txt_reference_trans.setText("Reference: " + math);
        txt_reference_dep.setText("Reference: " + math);
        kazisafe.getRegions(entr).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        if (reg != null) {
                            pref.put("region" + (++i), reg);
                        }
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
        cbx_region_compte.getSelectionModel().selectFirst();
        cbx_type_compte.getSelectionModel().selectFirst();
        tbl_transaction.setItems(lstransaction);
//        list_sms_recov.setItems(listSMS);
        ToggleGroup tg = new ToggleGroup();
        rdbtn_decaiss_trans.setToggleGroup(tg);
        rdbtn_encaiss_trans.setToggleGroup(tg);
        cbx_compte_dep.setItems(comptes);
        cbx_compte_recov.setItems(comptes);
        cbx_compte_trans.setItems(comptes);
        cbx_fonction_imput_dep.setItems(FXCollections.observableArrayList("Distribution", "Approvisionnement", "Admin. et Finance", "Production"));
        //new ComboBoxAutoCompletion<>(cbx_compte_trans);
        txt_sum_arecov_recov.setText("A recouvrer : " + calculDette());
        txt_recoved_sum_recov.setText("Déjà récouvré : " + Util.sumAllCurency(Util.collectPaidDebt(lstrz), taux2change));
        if (f != null) {
            tab_pn_tresor.getSelectionModel().select(tab_recov_trans);
            Double pd = TraisorerieDelegate.sumByReference(f.getNumero(), taux2change);

            somme = f.getTotalamount() - (pd == null ? 0 : pd);
            cdf = somme * taux2change;
            usd = somme;
            revertUsd = usd;
            revertCdf = cdf;
            tf_reference_recov.setText(String.valueOf(math));
            trx = new Traisorerie(DataId.generate());
            trx.setReference(f.getNumero());
            fact = f;
            ClientOrganisation clt = f.getOrganisId();
            System.out.println("Client recov " + clt);
            txt_client_recov.setText("Client : " + clt.getPhoneOrganisation() + " (" + f.getNumero() + ")");
            txt_eval_usd_recov.setText(String.valueOf(somme));
            txt_eval_cdf_recov.setText(String.valueOf(BigDecimal.valueOf(cdf).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
            tf_montant_usd_recov.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    double in_usd = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                    if (newValue.isEmpty() && tf_montant_cdf_recov.getText().isEmpty()) {
                        dt = usd;
                        ff = 0;
                        fd = 0;
                        txt_eval_usd_recov.setText(String.valueOf((new BigDecimal(usd).setScale(2, RoundingMode.HALF_UP).doubleValue())));
                        txt_eval_cdf_recov.setText(String.valueOf((new BigDecimal(cdf).setScale(2, RoundingMode.HALF_UP).doubleValue())));
                        txt_arembourser_usd_recov.setText("0");
                        txt_arembourser_cdf_recov.setText("0");
                    } else if (!newValue.isEmpty() && tf_montant_cdf_recov.getText().isEmpty()) {
                        double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restUsd >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = 0;
                            txt_arembourser_cdf_recov.setText("0");
                            txt_arembourser_usd_recov.setText("0");
                        } else {
                            double retour = Math.abs(restUsd);
                            fd = in_usd - retour;
                            dt = 0;
                            ff = 0;
                            txt_eval_usd_recov.setText("0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("" + retour);
                            txt_arembourser_cdf_recov.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }

                        trx.setMontantUsd(fd);
                    } else if (newValue.isEmpty() && !tf_montant_cdf_recov.getText().isEmpty()) {
                        double inCdf = Double.parseDouble(tf_montant_cdf_recov.getText().toString());
                        double restCdf = new BigDecimal(cdf - inCdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restCdf >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = inCdf;
                            txt_arembourser_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("0.0");
                        } else {
                            double retour = Math.abs(restCdf);
                            fd = 0;
                            dt = 0;
                            ff = inCdf - retour;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_cdf_recov.setText("" + retour);
                            txt_arembourser_usd_recov.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantCdf(ff);
                    } else {
                        double inCdf = Double.parseDouble(tf_montant_cdf_recov.getText().toString());
                        double converted = new BigDecimal(inCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double nwInUsd = (in_usd + converted);
                        double restUsd = new BigDecimal(usd - nwInUsd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restUsd >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = inCdf;
                            txt_arembourser_cdf_recov.setText("");
                            txt_arembourser_usd_recov.setText("");
                        } else {
                            double retour = Math.abs(restUsd);
                            fd = nwInUsd - retour;
                            dt = 0;
                            ff = 0;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("" + retour);
                            txt_arembourser_cdf_recov.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantUsd(fd);
                    }
                    dpk_date_ech_recov.setDisable(true);
                }
            });
            tf_montant_cdf_recov.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    double in_cdf = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                    if (newValue.isEmpty() && tf_montant_usd_recov.getText().isEmpty()) {
                        txt_eval_cdf_recov.setText(String.valueOf(new BigDecimal(cdf).setScale(2, RoundingMode.HALF_UP).doubleValue()));
                        txt_eval_usd_recov.setText(String.valueOf(new BigDecimal(usd).setScale(2, RoundingMode.HALF_UP).doubleValue()));
                        dt = usd;
                        ff = 0;
                        fd = 0;
                        txt_arembourser_usd_recov.setText("0.0");
                        txt_arembourser_cdf_recov.setText("0.0");
                    } else if (!newValue.isEmpty() && tf_montant_usd_recov.getText().isEmpty()) {
//                    double in_usd = Double.parseDouble((editable.toString().isEmpty() ? "0" : editable.toString()));
                        double restCdf = new BigDecimal(cdf - in_cdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restCdf >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = 0;
                            ff = in_cdf;
                            txt_arembourser_cdf_recov.setText("");
                            txt_arembourser_usd_recov.setText("");
                        } else {
                            double retour = Math.abs(restCdf);
                            fd = 0;
                            dt = 0;
                            ff = in_cdf - retour;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_cdf_recov.setText("" + retour);
                            txt_arembourser_usd_recov.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantCdf(ff);
                    } else if (newValue.isEmpty() && !tf_montant_usd_recov.getText().isEmpty()) {
                        double in_usd = Double.parseDouble(tf_montant_usd_recov.getText());
                        double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restUsd >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = 0;
                            txt_arembourser_cdf_recov.setText("");
                            txt_arembourser_usd_recov.setText("");
                        } else {
                            double retour = Math.abs(restUsd);
                            fd = in_usd - retour;
                            dt = 0;
                            ff = 0;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("" + retour);
                            txt_arembourser_cdf_recov.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantUsd(fd);
                    } else {
                        double inUsd = Double.parseDouble(tf_montant_usd_recov.getText().toString());
                        double converted = new BigDecimal(inUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double nwInCdf = (in_cdf + converted);
                        double restCdf = new BigDecimal(cdf - nwInCdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restCdf >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            ff = in_cdf;
                            fd = inUsd;
                            txt_arembourser_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("0.0");
                        } else {
                            double retour = Math.abs(restCdf);
                            fd = 0;
                            dt = 0;
                            ff = nwInCdf - retour;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_cdf_recov.setText("" + retour);
                            txt_arembourser_usd_recov.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantCdf(ff);
                    }
                    dpk_date_ech_recov.setDisable(true);
                }
            });
        }

        if (v != null) {
            tab_pn_tresor.getSelectionModel().select(tab_recov_trans);
            Double pd = TraisorerieDelegate.sumByReference(v.getUid() + "-" + v.getReference(), taux2change);//database.sumRecoveredByVente(v.getUid(), taux2change);
            // Util.sumAllCurency(collectPaidDebt(lstrz, "BIL"+v.getReference()), taux2change);
            somme = v.getMontantDette() - (pd == null ? 0 : pd);
            cdf = somme * taux2change;
            usd = somme;
            revertUsd = usd;
            revertCdf = cdf;
            tf_reference_recov.setText(String.valueOf(math));
            trx = new Traisorerie(DataId.generate());
            trx.setReference(v.getUid() + "-" + v.getReference());
            vtx = v;
            Client clt = v.getClientId();
            System.out.println("Client recov " + clt);
            txt_client_recov.setText("Client : " + clt.getPhone() + " (" + v.getReference() + ")");
            txt_eval_usd_recov.setText(String.valueOf((somme)));
            txt_eval_cdf_recov.setText(String.valueOf(BigDecimal.valueOf(cdf).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
            tf_montant_usd_recov.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    double in_usd = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                    if (newValue.isEmpty() && tf_montant_cdf_recov.getText().isEmpty()) {
                        dt = usd;
                        ff = 0;
                        fd = 0;
                        txt_eval_usd_recov.setText(String.valueOf((new BigDecimal(usd).setScale(2, RoundingMode.HALF_UP).doubleValue())));
                        txt_eval_cdf_recov.setText(String.valueOf((new BigDecimal(cdf).setScale(2, RoundingMode.HALF_UP).doubleValue())));
                        txt_arembourser_usd_recov.setText("0");
                        txt_arembourser_cdf_recov.setText("0");
                    } else if (!newValue.isEmpty() && tf_montant_cdf_recov.getText().isEmpty()) {
                        double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restUsd >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = 0;
                            txt_arembourser_cdf_recov.setText("0");
                            txt_arembourser_usd_recov.setText("0");
                        } else {
                            double retour = Math.abs(restUsd);
                            fd = in_usd - retour;
                            dt = 0;
                            ff = 0;
                            txt_eval_usd_recov.setText("0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("" + retour);
                            txt_arembourser_cdf_recov.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }

                        trx.setMontantUsd(fd);
                    } else if (newValue.isEmpty() && !tf_montant_cdf_recov.getText().isEmpty()) {
                        double inCdf = Double.parseDouble(tf_montant_cdf_recov.getText().toString());
                        double restCdf = new BigDecimal(cdf - inCdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restCdf >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = inCdf;
                            txt_arembourser_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("0.0");
                        } else {
                            double retour = Math.abs(restCdf);
                            fd = 0;
                            dt = 0;
                            ff = inCdf - retour;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_cdf_recov.setText("" + retour);
                            txt_arembourser_usd_recov.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantCdf(ff);
                    } else {
                        double inCdf = Double.parseDouble(tf_montant_cdf_recov.getText().toString());
                        double converted = new BigDecimal(inCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double nwInUsd = (in_usd + converted);
                        double restUsd = new BigDecimal(usd - nwInUsd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restUsd >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = inCdf;
                            txt_arembourser_cdf_recov.setText("");
                            txt_arembourser_usd_recov.setText("");
                        } else {
                            double retour = Math.abs(restUsd);
                            fd = nwInUsd - retour;
                            dt = 0;
                            ff = 0;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("" + retour);
                            txt_arembourser_cdf_recov.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }

                        trx.setMontantUsd(fd);
                    }
                    double debt = Double.parseDouble(txt_eval_usd_recov.getText());
                    if (debt > 0) {
                        if (!tf_montant_usd_recov.getText().isEmpty()) {
                            dpk_date_ech_recov.setDisable(false);
                        }
                    } else {
                        dpk_date_ech_recov.setDisable(true);
                    }
                }
            });
            tf_montant_cdf_recov.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    double in_cdf = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                    if (newValue.isEmpty() && tf_montant_usd_recov.getText().isEmpty()) {
                        txt_eval_cdf_recov.setText(String.valueOf(new BigDecimal(cdf).setScale(2, RoundingMode.HALF_UP).doubleValue()));
                        txt_eval_usd_recov.setText(String.valueOf(new BigDecimal(usd).setScale(2, RoundingMode.HALF_UP).doubleValue()));
                        dt = usd;
                        ff = 0;
                        fd = 0;
                        txt_arembourser_usd_recov.setText("0.0");
                        txt_arembourser_cdf_recov.setText("0.0");
                    } else if (!newValue.isEmpty() && tf_montant_usd_recov.getText().isEmpty()) {
//                    double in_usd = Double.parseDouble((editable.toString().isEmpty() ? "0" : editable.toString()));
                        double restCdf = new BigDecimal(cdf - in_cdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restCdf >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = 0;
                            ff = in_cdf;
                            txt_arembourser_cdf_recov.setText("");
                            txt_arembourser_usd_recov.setText("");
                        } else {
                            double retour = Math.abs(restCdf);
                            fd = 0;
                            dt = 0;
                            ff = in_cdf - retour;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_cdf_recov.setText("" + retour);
                            txt_arembourser_usd_recov.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantCdf(ff);
                    } else if (newValue.isEmpty() && !tf_montant_usd_recov.getText().toString().isEmpty()) {
                        double in_usd = Double.parseDouble(tf_montant_usd_recov.getText().toString());
                        double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restUsd >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            fd = in_usd;
                            ff = 0;
                            txt_arembourser_cdf_recov.setText("");
                            txt_arembourser_usd_recov.setText("");
                        } else {
                            double retour = Math.abs(restUsd);
                            fd = in_usd - retour;
                            dt = 0;
                            ff = 0;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("" + retour);
                            txt_arembourser_cdf_recov.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantUsd(fd);
                    } else {
                        double inUsd = Double.parseDouble(tf_montant_usd_recov.getText());
                        double converted = new BigDecimal(inUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double nwInCdf = (in_cdf + converted);
                        double restCdf = new BigDecimal(cdf - nwInCdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        if (restCdf >= 0) {
                            txt_eval_usd_recov.setText(String.valueOf(restUsd));
                            txt_eval_cdf_recov.setText(String.valueOf(restCdf));
                            dt = restUsd;
                            ff = in_cdf;
                            fd = inUsd;
                            txt_arembourser_cdf_recov.setText("0.0");
                            txt_arembourser_usd_recov.setText("0.0");
                        } else {
                            double retour = Math.abs(restCdf);
                            fd = 0;
                            dt = 0;
                            ff = nwInCdf - retour;
                            txt_eval_usd_recov.setText("0.0");
                            txt_eval_cdf_recov.setText("0.0");
                            txt_arembourser_cdf_recov.setText("" + retour);
                            txt_arembourser_usd_recov.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        }
                        trx.setMontantCdf(ff);
                    }
                    double debt = Double.parseDouble(txt_eval_usd_recov.getText());
                    if (debt > 0) {
                        if (!tf_montant_cdf_recov.getText().isEmpty()) {
                            dpk_date_ech_recov.setDisable(false);
                        }
                    } else {
                        dpk_date_ech_recov.setDisable(true);
                    }
                }
            });

        }
//        list_sms_recov.setCellFactory((ListView<SMSNotification> param) -> new ListCell<SMSNotification>() {
//            private ImageView imageView = new ImageView();
//
//            @Override
//            protected void updateItem(SMSNotification item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                    setGraphic(null);
//                } else {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            setText("Rappel à : " + item.getAdressNumber() + ", le " + item.getDate());
//                            imageView.setFitHeight(24);
//                            imageView.setFitWidth(24);
//                            imageView.setPreserveRatio(true);
//                            imageView.setImage(new Image(AgentController.class.getResourceAsStream("/icons/smartphone32.png")));
//                            setGraphic(imageView);
//                        }
//                    });
//
//                }
//            }
//
//        });

//        ContextMenu cm = new ContextMenu();
//        MenuItem mi = new MenuItem("Voir le contenu");
//        cm.getItems().add(mi);
//        mi.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                if (choosen != null) {
//                    MainUI.notify(null, "Message", choosen.getMessage(), 9, "info");
//                }
//            }
//        });
//        list_sms_recov.setContextMenu(cm);
//        list_sms_recov.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SMSNotification>() {
//            @Override
//            public void changed(ObservableValue<? extends SMSNotification> observable, SMSNotification oldValue, SMSNotification newValue) {
//                if (newValue != null) {
//                    choosen = newValue;
//                }
//            }
//        });
        tbl_transaction.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Transaction>() {
            @Override
            public void changed(ObservableValue<? extends Transaction> observable, Transaction oldValue, Transaction newValue) {
                if (newValue != null) {
                    trx = TraisorerieDelegate.findTraisorerie(newValue.getUid());//database.findByUid(Traisorerie.class, newValue.getUid());
                }
            }
        });
        tresors.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompteTresor>() {
            @Override
            public void changed(ObservableValue<? extends CompteTresor> observable, CompteTresor oldValue, CompteTresor newValue) {
                if (newValue != null) {
                    choosenComptetr = newValue;
                    bankname.setText(choosenComptetr.getBankName());
                    intitule_copmpte.setText(choosenComptetr.getIntitule());
                    numero_compte.setText(choosenComptetr.getNumeroCompte());
                    cbx_region_compte.getSelectionModel().select(choosenComptetr.getRegion());
                    cbx_type_compte.getSelectionModel().select(choosenComptetr.getTypeCompte());
                }
            }
        });
        ls_depense.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Depense>() {
            @Override
            public void changed(ObservableValue<? extends Depense> observable, Depense oldValue, Depense newValue) {
                if (newValue != null) {
                    choosenDepense = newValue;
                    depensename.setText(choosenDepense.getNomDepense());
                    cbx_rgion_deps.getSelectionModel().select(choosenDepense.getRegion());
                }
            }
        });
        cbx_depenses.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Depense>() {
            @Override
            public void changed(ObservableValue<? extends Depense> observable, Depense oldValue, Depense newValue) {
                if (newValue != null) {
                    choosenDepense = newValue;
                }
            }
        });
        search_compte.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    ObservableList<CompteTresor> rslt = FXCollections.observableArrayList();
                    for (CompteTresor compte : comptes) {
                        String pr = compte.getBankName() + " " + compte.getIntitule() + " " + compte.getNumeroCompte() + " " + compte.getRegion() + " " + compte.getTypeCompte() + " " + compte.getSoldeMinimum();
                        if (pr.toUpperCase().contains(newValue.toLowerCase())) {
                            rslt.add(compte);
                        }
                    }
                    tresors.setItems(rslt);
                } else {
                    tresors.setItems(comptes);
                }
                Platform.runLater(() -> {
                    count_compte.setText(String.format(bundle.getString("xitems"), tresors.getItems().size()));
                });
            }
        });
        searchDeps.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    ObservableList<Depense> rslt = FXCollections.observableArrayList();
                    for (Depense d : depenses) {
                        String pr = d.getNomDepense() + " " + d.getRegion();
                        if (pr.toUpperCase().contains(newValue.toUpperCase())) {
                            rslt.add(d);
                        }
                    }
                    ls_depense.setItems(rslt);
                } else {
                    ls_depense.setItems(depenses);
                }
                Platform.runLater(() -> {
                    count_depense.setText(String.format(bundle.getString("xitems"), ls_depense.getItems().size()));
                });
            }
        });
        //  new ComboBoxAutoCompletion<>(cbx_compte_trans);

    }

    @FXML
    public void searchDette(Event e) {
        if (!tf_reference_recov.getText().isEmpty()) {
            progress_cloud_search.setVisible(true);
        }
    }

    private List<String> regKeys() {
        List<String> result = new ArrayList<>();
        try {

            for (String key : pref.keys()) {
                if (key.startsWith("region")) {
                    if (!result.contains(key)) {
                        result.add(key);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(DestockController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void search(String query) {
        List<Traisorerie> result = new ArrayList<>();
        for (Traisorerie t : lstrz) {
            String value = t.getLibelle() + " " + t.getReference() + " "
                    + t.getTypeTresorerie() + " " + t.getMouvement() + ""
                    + " " + Constants.DATE_ONLY_FORMAT.format(t.getDate());
            if (value.toUpperCase().contains(query.toUpperCase())) {
                result.add(t);
            }
        }
        fillTransactions(result);
    }

    private void configCbx() {
        cbx_compte_trans.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompteTresor>() {
            @Override
            public void changed(ObservableValue<? extends CompteTresor> observable, CompteTresor oldValue, CompteTresor newValue) {
                if (newValue != null) {
                    choosenComptetr = newValue;
                    txt_choosen_account_trans.setText(newValue.getTypeCompte() + " " + newValue.getBankName());
                    List<Traisorerie> lts = TraisorerieDelegate.findTraisorByCompteTresor(newValue.getUid(), newValue.getTypeCompte());//database.findWithAndClause(Traisorerie.class, new String[]{"typeTresorerie", "tresor_id"}, new String[]{newValue.getTypeCompte(), newValue.getUid()});
                    if (lts.isEmpty()) {
                        lts = TraisorerieDelegate.findTraisorByCompteTresor(newValue.getUid(), newValue.getTypeCompte());// database.findWithOrClause(Traisorerie.class, new String[]{"typeTresorerie", "tresor_id"}, new String[]{newValue.getTypeCompte(), newValue.getUid()});
                    }
                    fillTransactions(lts);
                }
            }
        });
        cbx_compte_dep.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompteTresor>() {
            @Override
            public void changed(ObservableValue<? extends CompteTresor> observable, CompteTresor oldValue, CompteTresor newValue) {
                if (newValue != null) {
                    choosenComptetr = newValue;
                    txt_choosen_account_trans.setText(newValue.getTypeCompte() + " " + newValue.getBankName());
                    List<Traisorerie> lts = TraisorerieDelegate.findTraisorByCompteTresor(newValue.getUid(), newValue.getTypeCompte());//database.findWithAndClause(Traisorerie.class, new String[]{"typeTresorerie", "tresor_id"}, new String[]{newValue.getTypeCompte(), newValue.getUid()});
                    if (lts.isEmpty()) {
                        lts = TraisorerieDelegate.findTraisorByCompteTresor(newValue.getUid(), newValue.getTypeCompte());// database.findWithOrClause(Traisorerie.class, new String[]{"typeTresorerie", "tresor_id"}, new String[]{newValue.getTypeCompte(), newValue.getUid()});
                    }
                    fillTransactions(lts);
                }
            }
        });
        cbx_compte_recov.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompteTresor>() {
            @Override
            public void changed(ObservableValue<? extends CompteTresor> observable, CompteTresor oldValue, CompteTresor newValue) {
                if (newValue != null) {
                    choosenComptetr = newValue;
                    txt_choosen_account_trans.setText(newValue.getTypeCompte() + " " + newValue.getBankName());
                    List<Traisorerie> lts = TraisorerieDelegate.findTraisorByCompteTresor(newValue.getUid(), newValue.getTypeCompte());//database.findWithAndClause(Traisorerie.class, new String[]{"typeTresorerie", "tresor_id"}, new String[]{newValue.getTypeCompte(), newValue.getUid()});
                    if (lts.isEmpty()) {
                        lts = TraisorerieDelegate.findTraisorByCompteTresor(newValue.getUid(), newValue.getTypeCompte());//database.findWithOrClause(Traisorerie.class, new String[]{"typeTresorerie", "tresor_id"}, new String[]{newValue.getTypeCompte(), newValue.getUid()});
                    }
                    fillTransactions(lts);
                }
            }
        });
        cbx_region.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    fillTransactions(TraisorerieDelegate.findTraisoreries(newValue));
                    System.out.println("Solde usd " + soldeusd + " solde cdf " + soldecdf);
                } else {
                    fillTransactions(lstrz);
                }
            }
        });
    }

    @FXML
    private void filterByDate(Event evt) {
        if (dpk_date_debut_trans.getValue() == null || dpk_fin_trans.getValue() == null) {
            return;
        }
        List<Traisorerie> trzls = Util.filterTransactionByDate(lstrz, Constants.Datetime.toUtilDate(dpk_date_debut_trans.getValue()),
                Constants.Datetime.toUtilDate(dpk_fin_trans.getValue()));
        fillTransactions(trzls);

    }

    public void fillTransactions(List<Traisorerie> trs) {
        List<Transaction> ltx = new ArrayList<>();
        lstransaction.clear();
        List<Transaction> calc = new ArrayList<>();
        for (Traisorerie tr : trs) {
            Transaction t = new Transaction();
            t.setUid(tr.getUid());
            t.setDate(tr.getDate());
            t.setLibelle(tr.getLibelle());
            t.setReference(tr.getReference());
            if (tr.getMouvement().equals(Mouvment.AUGMENTATION.name())) {
                t.setDebit_usd(tr.getMontantUsd());
                t.setDebit_cdf(tr.getMontantCdf());
            } else if (tr.getMouvement().equals(Mouvment.DIMINUTION.name())) {
                t.setCredit_usd(tr.getMontantUsd());
                t.setCredit_cdf(tr.getMontantCdf());
            }
            ltx.add(t);

        }
        Collections.sort(ltx, new Transaction());
        soldeusd = 0;
        soldecdf = 0;
        for (Transaction tx : ltx) {
            calc.add(tx);
            double debitUsd = sumDebitUsd(calc);
            double debitCdf = sumDebitCdf(calc);
            double creditUsd = sumCreditUsd(calc);
            double creditCdf = sumCreditCdf(calc);
            double soldeUsd = debitUsd - creditUsd;
            double soldeCdf = debitCdf - creditCdf;
            soldecdf = soldeCdf;
            soldeusd = soldeUsd;
            tx.setSolde_usd(soldeUsd);
            tx.setSolde_cdf(soldeCdf);
            lstransaction.add(tx);
        }
        Collections.reverse(lstransaction);

    }

    public boolean isExist(List<Transaction> ltr, String ref) {
        for (Transaction t : ltr) {
            if (t.getReference().equals(ref)) {
                return true;
            }
        }
        return false;
    }

    private double sumDebitUsd(List<Transaction> lts) {
        double sum = 0;
        for (Transaction t : lts) {
            sum += t.getDebit_usd();
        }
        return sum;
    }

    private double sumDebitCdf(List<Transaction> lts) {
        double sum = 0;
        for (Transaction t : lts) {
            sum += t.getDebit_cdf();
        }
        return sum;
    }

    private double sumCreditUsd(List<Transaction> lts) {
        double sum = 0;
        for (Transaction t : lts) {
            sum += t.getCredit_usd();
        }
        return sum;
    }

    private double sumCreditCdf(List<Transaction> lts) {
        double sum = 0;
        for (Transaction t : lts) {
            sum += t.getCredit_cdf();
        }
        return sum;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        configtab();
        configCbx();
        MainUI.setPattern(dpk_date_dep);
        MainUI.setPattern(dpk_date_trans);
        MainUI.setPattern(dpk_date_debut_trans);
        MainUI.setPattern(dpk_fin_trans);
        MainUI.setPattern(dpk_date_recov);
        MainUI.setPattern(dpk_date_ech_recov);
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux2change = pref.getDouble("taux2change", 2000);
        dpk_date_trans.setValue(LocalDate.now());
        dpk_date_dep.setValue(LocalDate.now());
        dpk_date_recov.setValue(LocalDate.now());
        dpk_date_ech_recov.setDisable(true);
        token = pref.get("token", null);
        role = pref.get("priv", null);
        region = pref.get("region", "...");
        entr = pref.get("eUid", "unknown");
        ComboBoxAutoCompletion<CompteTresor> comboBoxAutoCompletion = new ComboBoxAutoCompletion<>(cbx_compte_dep);
        ComboBoxAutoCompletion<CompteTresor> comboBoxAutoCompletion1 = new ComboBoxAutoCompletion<>(cbx_compte_recov);
        ComboBoxAutoCompletion<Depense> comboBoxAutoCompletion2 = new ComboBoxAutoCompletion<>(cbx_depenses);
        Util.installTooltip(imgbtn_clean, "Supprimer toute les notifications");
        Tooltip.install(pane_delete, new Tooltip("Supprimer l'une de mes transactions"));
        ContextMenu cmenu = new ContextMenu();
        MenuItem deleteCpt = new MenuItem("Supprimer");
        cmenu.getItems().add(deleteCpt);
        tresors.setContextMenu(cmenu);

        deleteCpt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenComptetr != null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez vous supprimer le compte ?, les transactions de ce compte risque d'etre perdue", ButtonType.YES, ButtonType.CANCEL);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    Optional<ButtonType> clkbtn = alert.showAndWait();
                    if (clkbtn.get() == ButtonType.YES) {
                        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                            List<Traisorerie> transx = TraisorerieDelegate.findTraisorByCompteTresor(choosenComptetr.getUid());
                            if (transx == null ? true : transx.isEmpty()) {
                                CompteTresorDelegate.deleteCompteTresor(choosenComptetr);//database.delete(choosenComptetr);
                                MainUI.notify(null, "Succes", "Compte supprime avec succes", 3, "info");
                                comptes.remove(choosenComptetr);
                            }
                        } else {
                            MainUI.notify(null, "Erreur", "Vous n'avez pas des privileges necessaire pour supprimer les elements", 3, "error");
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void createTransaction(ActionEvent event) {
        if (tab_ecriture_trans.isSelected()) {
            if ((tf_montant_usd_trans.getText().isEmpty()
                    && tf_montant_cdf_trans.getText().isEmpty())
                    || txtArea_motif_trans.getText().isEmpty() || choosenComptetr == null) {
                MainUI.notify(null, "Erreur", "Completer au moins un champs", 4, "error");
                return;
            }
            Traisorerie tr = new Traisorerie(DataId.generate());
            tr.setDate(Constants.Datetime.toUtilDate(dpk_date_trans.getValue()));
            tr.setLibelle(txtArea_motif_trans.getText());
            if (!tf_montant_cdf_trans.getText().isEmpty()) {
                tr.setMontantCdf(Double.parseDouble(tf_montant_cdf_trans.getText()));
            }
            if (!tf_montant_usd_trans.getText().isEmpty()) {
                tr.setMontantUsd(Double.parseDouble(tf_montant_usd_trans.getText()));
            }
            if (rdbtn_decaiss_trans.isSelected()) {
                if (tr.getMontantUsd() > soldeusd) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Le montant USD solicité au decaissement est superieur au solde actuel, continuer malgré tout ?", ButtonType.YES, ButtonType.CANCEL);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    Optional<ButtonType> clkbtn = alert.showAndWait();
                    if (clkbtn.get() == ButtonType.YES) {
                    } else {
                        return;
                    }
                }
                if (tr.getMontantCdf() > soldecdf) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Le montant CDF solicité au decaissement est superieur au solde actuel, continuer malgré tout ?", ButtonType.YES, ButtonType.CANCEL);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    Optional<ButtonType> clkbtn = alert.showAndWait();
                    if (clkbtn.get() == ButtonType.YES) {
                    } else {
                        return;
                    }
                }
                tr.setMouvement(Mouvment.DIMINUTION.name());
            }
            if (rdbtn_encaiss_trans.isSelected()) {
                tr.setMouvement(Mouvment.AUGMENTATION.name());
            }
            tr.setReference(txt_reference_trans.getText().split(" ")[1]);
            tr.setTypeTresorerie(cbx_compte_trans.getValue().getTypeCompte());
            tr.setRegion(region);
            tr.setTresorId(choosenComptetr);
            Traisorerie trz = TraisorerieDelegate.saveTraisorerie(tr);//database.insertAndSync(tr);
            if (trz != null) {
                lstrz.add(trz);
                fillTransactions(lstrz);
                tf_montant_cdf_trans.clear();
                tf_montant_usd_trans.clear();
                txtArea_motif_trans.clear();
                math = String.valueOf(((int) (Math.random() * 100001)));
                txt_reference_trans.setText("Reference: " + math);
                txt_reference_dep.setText("Reference: " + math);
                MainUI.notify(null, "Succès", "Transaction enregistrée avec succès", 4, "info");
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(trz, Constants.ACTION_CREATE, Tables.TRAISORERIE);
                        });
            }
        } else if (tab_depanse_trans.isSelected()) {
            if ((tf_montant_usd_dep.getText().isEmpty()
                    && tf_montant_cdf_dep.getText().isEmpty())
                    || txtArea_motif_dep.getText().isEmpty() || choosenDepense == null) {
                MainUI.notify(null, "Erreur", "Completer au moins un champs", 4, "error");
                return;
            }
            Traisorerie tr = new Traisorerie(DataId.generate());
            tr.setDate(Constants.Datetime.toUtilDate(dpk_date_dep.getValue()));
            tr.setLibelle(txtArea_motif_dep.getText());
            if (!tf_montant_cdf_dep.getText().isEmpty()) {
                tr.setMontantCdf(Double.parseDouble(tf_montant_cdf_dep.getText()));
            }
            if (!tf_montant_usd_dep.getText().isEmpty()) {
                tr.setMontantUsd(Double.parseDouble(tf_montant_usd_dep.getText()));
            }
            tr.setMouvement(Mouvment.DIMINUTION.name());
            tr.setReference(txt_reference_dep.getText().split(" ")[1]);
            tr.setTypeTresorerie(cbx_compte_dep.getValue().getTypeCompte());
            tr.setRegion(region);
            tr.setTresorId(choosenComptetr);
            Traisorerie trz = TraisorerieDelegate.saveTraisorerie(tr);//database.insertAndSync(tr);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(trz, Constants.ACTION_CREATE, Tables.TRAISORERIE);
                    });
            if (trz != null) {
                lstrz.add(trz);
                fillTransactions(lstrz);
                Operation ops = new Operation(DataId.generate());
                ops.setCaisseOpId(trz);
                ops.setDate(trz.getDate());
                ops.setImputation(cbx_fonction_imput_dep.getValue());
                ops.setLibelle(trz.getLibelle());
                ops.setMontantCdf(trz.getMontantCdf());
                ops.setMontantUsd(trz.getMontantUsd());
                ops.setMouvement(Mouvment.AUGMENTATION.name());
                ops.setReferenceOp(trz.getReference());
                ops.setRegion(region);
                ops.setTresorId(choosenComptetr);
                ops.setDepenseId(choosenDepense);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(OperationDelegate.saveOperation(ops), Constants.ACTION_CREATE, Tables.OPERATION);
                        });
                //database.insertAndSync(ops);
                tf_montant_cdf_dep.clear();
                tf_montant_usd_dep.clear();
                txtArea_motif_dep.clear();
                math = String.valueOf(((int) (Math.random() * 100001)));
                txt_reference_trans.setText("Reference: " + math);
                txt_reference_dep.setText("Reference: " + math);
                MainUI.notify(null, "Succès", "Depense enregistrée avec succès", 4, "info");
            }
        } else if (tab_recov_trans.isSelected()) {
            if ((tf_montant_usd_recov.getText().isEmpty()
                    && tf_montant_cdf_recov.getText().isEmpty()) || choosenComptetr == null) {
                MainUI.notify(null, "Erreur", "Completer au moins un champs", 4, "error");
                return;
            }
            if (trx == null) {
                MainUI.notify(null, "Erreur", "Selectionner une vente à crédit, puis continuer", 4, "error");
                return;
            }
            trx.setDate(Constants.Datetime.toUtilDate(dpk_date_recov.getValue()));
            trx.setLibelle("Recouvrement dette " + ((vtx == null) ? fact.getNumero() : (vtx.getReference() + " - " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(vtx.getDateVente()))));
            trx.setMouvement(Mouvment.AUGMENTATION.name());

            trx.setRegion(region);
            trx.setTresorId(choosenComptetr);
            trx.setTypeTresorerie(cbx_compte_recov.getValue().getTypeCompte());
            Traisorerie trz = TraisorerieDelegate.saveTraisorerie(trx);//database.insertAndSync(trx);
            if (trz != null) {
                lstrz.add(trz);
                fillTransactions(lstrz);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(trz, Constants.ACTION_CREATE, Tables.TRAISORERIE);
                        });
                math = String.valueOf(((int) (Math.random() * 100001)));
//                Vente vt=new Vente(Integer.parseInt(tf_reference_recov.getText()));
                if (dpk_date_ech_recov.getValue() != null) {
                    if (vtx != null) {
                        vtx.setEcheance(Constants.Datetime.toUtilDate(dpk_date_ech_recov.getValue()));//tf_reference_recov.getText()
                        Vente updrst = VenteDelegate.updateVente(vtx);//database.update(vtx);
                        Executors.newCachedThreadPool()
                                .submit(() -> {
                                    Util.sync(updrst, Constants.ACTION_UPDATE, Tables.VENTE);
                                });
                        System.err.println("Upd result local " + updrst.getReference());

                    }
                }
                if (fact != null) {
                    double past = fact.getPayedamount();
                    double payed = trz.getMontantUsd() + (trz.getMontantCdf() / taux2change);
                    double am = past + payed;
                    double rest = (fact.getTotalamount() - am);
                    fact.setPayedamount(rest < 0 ? fact.getTotalamount() : am);
                    fact.setStatus(rest <= 0 ? Constants.BILL_STATUS_PAID : rest > 0 ? Constants.BILL_STATUS_INPAYMENT : Constants.BILL_STATUS_UNPAID);
                    Facture ff = FactureDelegate.updateFacture(fact);//database.update(fact);
                    if (ff != null) {
                        Executors.newCachedThreadPool()
                                .submit(() -> {
                                    Util.sync(ff, Constants.ACTION_UPDATE, Tables.FACTURE);
                                });
                        System.out.println("Payment saved");
                    }

                }
                tf_montant_cdf_recov.clear();
                tf_montant_usd_recov.clear();
                MainUI.notify(null, "Succès", "Recouvrement enregistré avec succès", 4, "info");
            }
        }

    }

    private double calculDette() {
        lvente = VenteDelegate.findVentes();//database.findAll(Vente.class);
        if (lvente == null) {
            return 0;
        }
        double sdette = 0;
        int countF = 0;
        for (Vente vs : lvente) {
            if (vs.getMontantDette() != null) {
                if (vs.getMontantDette() > 0) {
                    sdette += vs.getMontantDette();
                    countF++;
                }
            }
        }
        txt_client_count.setText("Repartie sur " + countF + " factures");
        return sdette;
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
    private void refreshSales(MouseEvent event) {
        if (cbx_compte_trans.getValue() == null) {
            return;
        }
        SyncEngine.getInstance().shutdown();
        List<Traisorerie> lts = new ArrayList<>();
        if (tab_ecriture_trans.isSelected()) {
            lts.addAll(TraisorerieDelegate.findTraisorByCompteTresor(cbx_compte_trans.getValue().getUid(), cbx_compte_trans.getValue().getTypeCompte()));
            if (lts.isEmpty()) {
                lts.addAll(TraisorerieDelegate.findTraisorByCompteTresOR(cbx_compte_trans.getValue().getUid(), cbx_compte_trans.getValue().getTypeCompte()));
            }
        } else if (tab_depanse_trans.isSelected()) {
            lts.addAll(TraisorerieDelegate.findTraisorByCompteTresor(cbx_compte_dep.getValue().getUid(), cbx_compte_dep.getValue().getTypeCompte()));

            if (lts.isEmpty()) {
                lts.addAll(TraisorerieDelegate.findTraisorByCompteTresOR(cbx_compte_dep.getValue().getUid(), cbx_compte_dep.getValue().getTypeCompte()));
            }

        } else if (tab_recov_trans.isSelected()) {
            lts.addAll(TraisorerieDelegate.findTraisorByCompteTresor(cbx_compte_recov.getValue().getUid(), cbx_compte_recov.getValue().getTypeCompte()));

            if (lts.isEmpty()) {
                lts.addAll(TraisorerieDelegate.findTraisorByCompteTresOR(cbx_compte_recov.getValue().getUid(), cbx_compte_recov.getValue().getTypeCompte()));
            }

        }
        fillTransactions(lts);
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    Refresher rfr = new Refresher("OTHERS");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(rfr, "read", Tables.REFRESH);
                            });
                });
    }

    @FXML
    private void deleteTransaction(MouseEvent e) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez vous vraiment supprimer la transaction séléctionnée", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        Optional<ButtonType> clkbtn = alert.showAndWait();
        if (clkbtn.get() == ButtonType.YES) {
            if (trx != null) {
                if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                    TraisorerieDelegate.deleteTraisorerie(trx);
                    //database.delete(trx);
                    MainUI.notify(null, "Succes", "Transaction supprimee avec succes", 3, "info");
                } else {
                    MainUI.notify(null, "Erreur", "Vous n'avez pas les privileges necessaire pour effectuer une supression", 3, "error");
                }
            }
        }
    }

    @FXML
    private void exportTransactions(MouseEvent event) {
        if (tbl_transaction.getItems().isEmpty()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                File xlsInv = Util.exportXlsTransactions(tbl_transaction.getItems());
                try {
                    Desktop.getDesktop().open(xlsInv);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void saveTresorerieByHttp(Traisorerie tr) {
        kazisafe.saveTraisorerie(tr).enqueue(new Callback<Traisorerie>() {
            @Override
            public void onResponse(Call<Traisorerie> call, Response<Traisorerie> rspns) {
                System.out.println("Tresorerie " + rspns.message());
                if (rspns.isSuccessful()) {
                    System.out.println("Tresorerie saved on server");
                }
            }

            @Override
            public void onFailure(Call<Traisorerie> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private void saveCompteTresorByHttp(CompteTresor ctr) {
        kazisafe.saveCompteTesor(ctr.getUid(),ctr).enqueue(new Callback<CompteTresor>() {
            @Override
            public void onResponse(Call<CompteTresor> call, Response<CompteTresor> rspns) {
                System.out.println("Compte Tresor " + rspns.message());
                if (rspns.isSuccessful()) {
                    System.out.println("Tresorerie saved on server");
                }
            }

            @Override
            public void onFailure(Call<CompteTresor> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private void saveDepenseByHttp(Depense d) {
        kazisafe.saveDepense(d.getUid(),d).enqueue(new Callback<Depense>() {
            @Override
            public void onResponse(Call<Depense> call, Response<Depense> rspns) {
                System.out.println("Depense " + rspns.message());
                if (rspns.isSuccessful()) {
                    System.out.println("Tresorerie saved on server");
                }
            }

            @Override
            public void onFailure(Call<Depense> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

}
