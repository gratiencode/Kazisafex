/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.FactureDelegate;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import data.ClientOrganisation;
import data.Entreprise;
import data.Facture;
import data.Mesure;
import tools.Constants;
import static tools.Constants.CAISSES;
import tools.DataId;
import tools.FileUtils;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import utilities.Relevee;
import data.helpers.Role; import data.network.Kazisafe;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class ReleveeController implements Initializable {

    static ReleveeController instance;
//    JpaStorage db;
    ResourceBundle bundle;
    Kazisafe ksf;
    Preferences pref;
    @FXML
    DatePicker dpk1;
    @FXML
    DatePicker dpk2;
    @FXML
    Tab bills;
    @FXML
    TabPane relevtabs;
    @FXML
    ImageView img_vu_logo;
    @FXML
    Label txt_bill_companyname;
    @FXML
    Label txt_comp_adresse;
    @FXML
    Label txt_bill_comp_id;
    @FXML
    Label txt_bill_contact_entreprise;
    @FXML
    Label txt_bill_comp_idnat;
    @FXML
    Label txt_comp_adresse_tel;
    @FXML
    Label txt_nom_client1;
    @FXML
    Label txt_nom_client;
    @FXML
    Label txt_comp_adresse1;
    @FXML
    Pane arrierre_pane;
    @FXML
    private Pane pane_invoiced;
    @FXML
    private Pane pane_bill_sum_credit;
    @FXML
    private Pane pane_bill_cash_paid;
    @FXML
    AnchorPane billbed;
    @FXML
    CheckBox unpaids;
    File f;
    @FXML
    Tab inv;
    @FXML
    Tab rlv;

    @FXML
    TableView<Relevee> tbreleve;
    @FXML
    TableColumn<Relevee, String> cdate;
    @FXML
    TableColumn<Relevee, String> cbon;
    @FXML
    TableColumn<Relevee, String> cclient;
    @FXML
    TableColumn<Relevee, String> cproduit;
    @FXML
    TableColumn<Relevee, String> cquantite;
    @FXML
    TableColumn<Relevee, Number> cprixunit;
    @FXML
    TableColumn<Relevee, Number> cprixtotal;
    @FXML
    TableColumn<Relevee, String> cparent;
    @FXML
    Label lbl_entreprise;
    @FXML
    TextField searchRelv;
    @FXML
    Label totalRlv;
    @FXML
    Label txt_bill_num_facture;
    @FXML
    Pane wait_insert_pane, pane_tab;

    @FXML
    TableView<Facture> tfactures;
    @FXML
    TableColumn<Facture, String> cnumero;
    @FXML
    TableColumn<Facture, String> cperiode;
    @FXML
    TableColumn<Facture, Number> cmontant;
    @FXML
    TableColumn<Facture, Number> cmontantpaye;
    @FXML
    TableColumn<Facture, Number> cmontantrestant;
    @FXML
    TableColumn<Facture, String> cetat;

    @FXML
    TableView<Facture> tbl_bill_facts;
    @FXML
    TableColumn<Facture, String> col_bill_qte;
    @FXML
    TableColumn<Facture, String> col_bill_designation;
    @FXML
    TableColumn<Facture, Number> col_bill_pu;
    @FXML
    TableColumn<Facture, Number> col_bill_prix_unit;

    @FXML
    TableView<Facture> tbl_bill_arr_facts;
    @FXML
    TableColumn<Facture, String> col_bill_arr_numero;
    @FXML
    TableColumn<Facture, String> col_bill_arr_designation;
    @FXML
    TableColumn<Facture, Number> col_bill_prix_arr_unit;

    @FXML
    Label txt_bill_date_vente;
    @FXML
    Label txt_bill_somme_credit;
    @FXML
    Label txt_bill_somme_facture;
    @FXML
    Label txt_print_status;
    Printer defaultPrinter;
    @FXML
    ComboBox<Printer> cbx_printers;

    ObservableList<Relevee> relevees;
    ObservableList<Facture> factures;
    ObservableList<Facture> fact_nonp;
    ObservableList<Facture> fact_arrier;
    ClientOrganisation orga;

    Facture choosenBill;
    Entreprise entreprise;
    int count_logic = -44;
    String role;
    String region;
    double restapayer = 0, payee = 0;

    public static ReleveeController getInstance() {
        if (instance == null) {
            instance = new ReleveeController();
        }
        return instance;
    }

    public ReleveeController() {
//        db = JpaStorage.getInstance();
        instance = this;
    }

    public void configTableRelevee() {
        cdate.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, String> param) -> {
            Relevee im = param.getValue();
            return new SimpleStringProperty(Constants.DATE_HEURE_FORMAT.format(im.getDate()));
        });
        cbon.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, String> param) -> {
            Relevee im = param.getValue();
            return new SimpleStringProperty(im.getNumeroBon());
        });
        cclient.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, String> param) -> {
            Relevee im = param.getValue();
            return new SimpleStringProperty(im.getNomClient());
        });
        cproduit.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, String> param) -> {
            Relevee im = param.getValue();
            return new SimpleStringProperty(im.getNomProduit());
        });
        cparent.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, String> param) -> {
            Relevee im = param.getValue();
            return new SimpleStringProperty(im.getParent());
        });
        cquantite.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, String> param) -> {
            Relevee im = param.getValue();
            Mesure m = im.getMesure();
            return new SimpleStringProperty(im.getQuantite() + " " + m.getDescription());
        });
        cprixunit.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, Number> param) -> {
            Relevee im = param.getValue();
            return new SimpleDoubleProperty(im.getPrixunitaire());
        });
        cprixtotal.setCellValueFactory((TableColumn.CellDataFeatures<Relevee, Number> param) -> {
            Relevee im = param.getValue();
            return new SimpleDoubleProperty(im.getQuantite() * im.getPrixunitaire());
        });

        /// factures
        cperiode.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture im = param.getValue();
            String d1 = Constants.DATE_ONLY_FORMAT.format(im.getStartDate());
            String d2 = Constants.DATE_ONLY_FORMAT.format(im.getEndDate());
            return new SimpleStringProperty(d1 + " - " + d2);
        });
        cnumero.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture im = param.getValue();
            return new SimpleStringProperty(im.getNumero());
        });
        cetat.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture im = param.getValue();
            return new SimpleStringProperty(im.getStatus());
        });
        cmontant.setCellValueFactory((TableColumn.CellDataFeatures<Facture, Number> param) -> {
            Facture im = param.getValue();
            return new SimpleDoubleProperty(im.getTotalamount());
        });
        cmontantpaye.setCellValueFactory((TableColumn.CellDataFeatures<Facture, Number> param) -> {
            Facture im = param.getValue();
            return new SimpleDoubleProperty(im.getPayedamount());
        });
        cmontantrestant.setCellValueFactory((TableColumn.CellDataFeatures<Facture, Number> param) -> {
            Facture im = param.getValue();
            return new SimpleDoubleProperty(im.getTotalamount() - im.getPayedamount());
        });

        //invoice
    }

    public void addFacture(Facture f) {
        Facture ff = FactureDelegate.findFacture(f.getUid());
        if (ff == null) {
            FactureDelegate.saveFacture(ff);
        } else {
            FactureDelegate.updateFacture(ff);
        }
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
        MainUI.setPattern(dpk1);
        MainUI.setPattern(dpk2);
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        configTableRelevee();
        configBillTable();
        configArrBillTable();
        tbreleve.setItems(relevees);
        LocalDate date = LocalDate.now();
        LocalDate start = date.minusMonths(1);
        dpk1.setValue(start);
        dpk2.setValue(date);
        count_logic = pref.getInt("count-logic", 0);
        role = pref.get("priv", null);
        region = pref.get("region", "...");
        ContextMenu recov = new ContextMenu();
        MenuItem rec = new MenuItem("Voir la facture");
        MenuItem billing = new MenuItem("Recouvrer la facture");
        MenuItem cancel = new MenuItem("Annuler la facture");
        MenuItem delete = new MenuItem("Supprimer la facture");
        recov.getItems().add(rec);
        recov.getItems().add(billing);
        recov.getItems().add(cancel);
        recov.getItems().add(delete);
        arrierre_pane.setVisible(false);
        defaultPrinter = Printer.getDefaultPrinter();
        tfactures.setContextMenu(recov);
        rec.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenBill != null) {
                    if (choosenBill.getPayedamount() < choosenBill.getTotalamount()) {
                        Facture found = FactureDelegate.findFacture(choosenBill.getUid());
                        if (found != null) {
                            relevtabs.getSelectionModel().select(bills);
                            fact_nonp.clear();
                            fact_nonp.add(found);
                            txt_nom_client1.setText("Tel : " + orga.getPhoneOrganisation());
                            txt_nom_client.setText("Client : " + orga.getNomOrganisation());
                            txt_comp_adresse1.setText(orga.getAdresse());
                        }
                    }
                }
            }
        });
        billing.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenBill != null) {
                    if (choosenBill.getPayedamount() < choosenBill.getTotalamount()) {
                        MainuiController.getInstance().switchScreens(tools.Constants.CAISSE_VIEW, CAISSES, "Trésorerie", "cashier.png", null, choosenBill);
                        Stage st = (Stage) tfactures.getScene().getWindow();
                        st.close();
                        ClientController.getInstance().closeFromOut();
                    } else {
                        MainUI.notify(null, "Info", "Cette facture est completement payee", 3, "info");
                    }
                }
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenBill != null) {
                    choosenBill.setStatus(Constants.BILL_STATUS_CANCELED);
                    Facture uf = FactureDelegate.updateFacture(choosenBill);
                    uf.setType(Tables.FACTURE.name());
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(uf, "update", Tables.FACTURE);
                            });
                }
            }
        });
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenBill != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer la facture séléctionnée ?", ButtonType.YES, ButtonType.CANCEL);
                    alert.setTitle("Attention!");
                    alert.setHeaderText(null);
                    Optional<ButtonType> showAndWait = alert.showAndWait();
                    if (showAndWait.get() == ButtonType.YES) {
                        if (role.equals(Role.Trader.name())
                                | role.toUpperCase().contains(Role.Finance.name().toUpperCase())
                                | role.toUpperCase().contains(Role.Manager.name().toUpperCase())) {
                            FactureDelegate.deleteFacture(choosenBill);
                            choosenBill.setType(Tables.FACTURE.name());
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(choosenBill, Constants.ACTION_DELETE, Tables.FACTURE);
                                    });
                            factures.remove(choosenBill);
                            MainUI.notify(null, "Succès", "Facture supprimée avec succès", 3, "Info");
                        } else {
                            MainUI.notify(null, "Erreur", "Vous n'avez pas assez des privileges pour effectuer cette action", 3, "error");
                        }

                    }
                }
            }
        });
        wait_insert_pane.setVisible(false);
    }

    @FXML
    public void printit(Event e) {
        print();
    }

    private void print() {
        PrinterJob pj = PrinterJob.createPrinterJob(defaultPrinter);
        if (pj == null) {
            return;
        }
        Window s = pane_invoiced.getScene().getWindow();
        boolean proceed = pj.showPrintDialog((Stage) s);
        if (proceed) {
            printBill(pj, pane_invoiced);
        }

    }

    private void printBill(PrinterJob pj, Node node) {
        txt_print_status.textProperty().bind(pj.jobStatusProperty().asString());
        boolean pr = pj.printPage(node);
        if (pr) {
            pj.endJob();
        }

    }

    public void setup(Kazisafe ksf, Entreprise entr, ClientOrganisation orga) {
        this.orga = orga;
        this.ksf = ksf;
        this.entreprise = entr;
        lbl_entreprise.setText(orga.getNomOrganisation() + ", " + orga.getAdresse() + ", " + orga.getPhoneOrganisation());

        txt_bill_companyname.setText(this.entreprise.getNomEntreprise());
        txt_comp_adresse.setText(this.entreprise.getAdresse());
        txt_bill_comp_id.setText("RCCM : " + this.entreprise.getIdentification());
        txt_bill_contact_entreprise.setText(this.entreprise.getEmail());
        String imp = this.entreprise.getNumeroImpot() == null ? "Aucun" : this.entreprise.getNumeroImpot(),
                idnat = this.entreprise.getIdNat() == null ? "Aucun" : this.entreprise.getIdNat();
        txt_bill_comp_idnat.setText((!imp.equals("Aucun") && !idnat.equals("Aucun")) ? "Impôt:" + imp + " , IdNat:" + idnat : (!imp.equals("Aucun") && idnat.equals("Aucun")) ? "Impôt:" + imp : (imp.equals("Aucun") && !idnat.equals("Aucun")) ? "IdNat:" + idnat : " ");
        txt_comp_adresse_tel.setText("Tel:" + entreprise.getPhones());

        relevees = FXCollections.observableArrayList();
        factures = FXCollections.observableArrayList();
        fact_nonp = FXCollections.observableArrayList();
        fact_arrier = FXCollections.observableArrayList();
        relevees.addAll(FactureDelegate.findReleveeInterval(this.orga.getUid(),
                Constants.Datetime.toUtilDate(dpk1.getValue()),
                Constants.Datetime.toUtilDate(dpk2.getValue())));

        factures.addAll(FactureDelegate.findOrgaBills(this.orga.getUid()));
        tfactures.setItems(factures);
        tbreleve.setItems(relevees);
        tbl_bill_facts.setItems(fact_nonp);
        tbl_bill_arr_facts.setItems(fact_arrier);
        totalRlv.setText("Total : " + sumRelev() + " usd");
        searchRelv.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    ObservableList<Relevee> result = FXCollections.observableArrayList();
                    for (Relevee relevee : relevees) {
                        String predic = relevee.getNomClient() + " " + relevee.getNomProduit() + " " + relevee.getNumeroBon()
                                + " " + relevee.getParent() + " " + relevee.getQuantite() + " " + Constants.DATE_ONLY_FORMAT.format(relevee.getDate());
                        if (predic.toUpperCase()
                                .contains(newValue.toUpperCase())) {
                            result.add(relevee);
                        }
                    }
                    tbreleve.setItems(result);
                } else {
                    tbreleve.setItems(relevees);
                }
                Platform.runLater(() -> {
                    totalRlv.setText("Total : " + sumRelev() + " usd");
                });
            }
        });
        tfactures.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Facture>() {
            @Override
            public void changed(ObservableValue<? extends Facture> observable, Facture oldValue, Facture newValue) {
                if (newValue != null) {
                    choosenBill = newValue;
                }
            }
        });
        f = FileUtils.pointFile(entr.getUid() + ".png");
        InputStream is;
        if (!f.exists()) {
            is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            f = FileUtils.streamTofile(is);
        }
        Image image = null;
        try {
            image = new Image(new FileInputStream(f));
            img_vu_logo.setImage(image);
            Util.centerImage(img_vu_logo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        bills.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    restapayer = 0;
                    payee = 0;
                    fact_arrier.clear();
                    List<Facture> facts = FactureDelegate.findUpaidBillsFor(orga.getUid());
                    for (Facture facture : facts) {
                        if (!facture.equals(choosenBill)) {
                            fact_arrier.add(facture);
                            payee += facture.getPayedamount();
                            restapayer += (facture.getTotalamount() - facture.getPayedamount());
                        }
                    }
                    if (choosenBill == null) {
                        return;
                    }
                    payee += choosenBill.getPayedamount();
                    restapayer += (choosenBill.getTotalamount() - choosenBill.getPayedamount());
                    txt_bill_num_facture.setText("Facture #" + choosenBill.getNumero());
                    txt_bill_date_vente.setText(tools.Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date()));

                    tbl_bill_facts.setFixedCellSize(25);
                    pane_tab.prefHeightProperty().bind(tbl_bill_facts.fixedCellSizeProperty().multiply(Bindings.size(tbl_bill_facts.getItems()).add(2)));
                    pane_tab.minHeightProperty().bind(pane_tab.prefHeightProperty());
                    pane_tab.maxHeightProperty().bind(pane_tab.prefHeightProperty());
//                   
                    tbl_bill_arr_facts.setFixedCellSize(25);
                    tbl_bill_arr_facts.prefHeightProperty().bind(tbl_bill_arr_facts.fixedCellSizeProperty().multiply(Bindings.size(tbl_bill_arr_facts.getItems()).add(1.01)));
                    tbl_bill_arr_facts.minHeightProperty().bind(tbl_bill_arr_facts.prefHeightProperty());
                    tbl_bill_arr_facts.maxHeightProperty().bind(tbl_bill_arr_facts.prefHeightProperty());
                    arrierre_pane.prefHeightProperty().bind(tbl_bill_arr_facts.maxHeightProperty().add(Bindings.size(tbl_bill_arr_facts.getItems())));
                    pane_invoiced.minHeightProperty().bind(pane_invoiced.prefHeightProperty().add(arrierre_pane.prefHeightProperty()));
                    pane_invoiced.maxHeightProperty().bind(pane_invoiced.prefHeightProperty().add(arrierre_pane.prefHeightProperty()));

//                    tbl_bill_facts.setFixedCellSize(25);
//                    tbl_bill_facts.prefHeightProperty().bind(tbl_bill_facts.fixedCellSizeProperty().multiply(Bindings.size(tbl_bill_facts.getItems()).add(1.01)));
//                    tbl_bill_facts.minHeightProperty().bind(tbl_bill_facts.prefHeightProperty());
//                    tbl_bill_facts.maxHeightProperty().bind(tbl_bill_facts.prefHeightProperty());
//                    pane_invoiced.prefHeightProperty().bind(tbl_bill_facts.maxHeightProperty().add(Bindings.size(tbl_bill_facts.getItems()).add(400)));
//                    pane_invoiced.minHeightProperty().bind(pane_invoiced.prefHeightProperty());
//                    pane_invoiced.maxHeightProperty().bind(pane_invoiced.prefHeightProperty());
                    billbed.prefHeightProperty().bind(pane_invoiced.maxHeightProperty().add(50));
                    billbed.minHeightProperty().bind(billbed.prefHeightProperty());
                    billbed.maxHeightProperty().bind(billbed.prefHeightProperty());
                    //afficher les totaux
                    Platform.runLater(() -> {
                        if (arrierre_pane.isVisible()) {
                            double dk = BigDecimal.valueOf(restapayer).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                            txt_bill_somme_credit.setText(dk + " USD");
                            double ds = BigDecimal.valueOf(payee).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                            txt_bill_somme_facture.setText(ds + " USD");
                        } else {
                            double dk = BigDecimal.valueOf((choosenBill.getTotalamount() - choosenBill.getPayedamount())).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                            txt_bill_somme_credit.setText(dk + " USD");
                            double dkt = BigDecimal.valueOf(choosenBill.getPayedamount()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                            txt_bill_somme_facture.setText(dkt + " USD");
                        }
                    });
                }
            }
        });
        unpaids.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    arrierre_pane.setVisible(true);
                    //affiche les variables
                    Platform.runLater(() -> {
                        double dk = BigDecimal.valueOf(restapayer).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        txt_bill_somme_credit.setText(dk + " USD");
                        double ds = BigDecimal.valueOf(payee).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        txt_bill_somme_facture.setText(ds + " USD");
                    });
                } else {
                    arrierre_pane.setVisible(false);
                    //affiche les totaux en cours
                    Platform.runLater(() -> {
                        double dk = BigDecimal.valueOf((choosenBill.getTotalamount() - choosenBill.getPayedamount())).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        txt_bill_somme_credit.setText(dk + " USD");
                        double dkt = BigDecimal.valueOf(choosenBill.getPayedamount()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        txt_bill_somme_facture.setText(dkt + " USD");
                    });
                }
            }
        });
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        System.out.println("Printewrs count " + osp.size());
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();
        cbx_printers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Printer>() {
            @Override
            public void changed(ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) {
                defaultPrinter = newValue;
            }
        });

        arrierre_pane.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    tbl_bill_arr_facts.setItems(fact_arrier);
                    tbl_bill_arr_facts.setFixedCellSize(25);
                    tbl_bill_arr_facts.prefHeightProperty().bind(tbl_bill_arr_facts.fixedCellSizeProperty().multiply(Bindings.size(tbl_bill_arr_facts.getItems()).add(1.01)));
                    tbl_bill_arr_facts.minHeightProperty().bind(tbl_bill_arr_facts.prefHeightProperty());
                    tbl_bill_arr_facts.maxHeightProperty().bind(tbl_bill_arr_facts.prefHeightProperty());
                    arrierre_pane.prefHeightProperty().bind(tbl_bill_arr_facts.maxHeightProperty().add(Bindings.size(tbl_bill_arr_facts.getItems()).add(100)));
                    pane_invoiced.minHeightProperty().bind(pane_invoiced.prefHeightProperty().add(arrierre_pane.prefHeightProperty()));
                    pane_invoiced.maxHeightProperty().bind(pane_invoiced.prefHeightProperty().add(arrierre_pane.prefHeightProperty()));

                }
            }
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        for (Printer p : osp) {
            rst.add(p);
        }
        return rst;
    }

    public void configBillTable() {
        col_bill_designation.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture r = param.getValue();

            return new SimpleStringProperty("Periode du "
                    + Constants.USER_READABLE_FORMAT.format(r.getStartDate()) + " - "
                    + Constants.USER_READABLE_FORMAT.format(r.getEndDate()));
        });
        col_bill_qte.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture r = param.getValue();
            return new SimpleStringProperty("1");
        });
        col_bill_pu.setCellValueFactory((TableColumn.CellDataFeatures<Facture, Number> param) -> {
            Facture r = param.getValue();
            double d = BigDecimal.valueOf(r.getTotalamount()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleDoubleProperty(d);
        });
        col_bill_prix_unit.setCellValueFactory((TableColumn.CellDataFeatures<Facture, Number> param) -> {
            Facture r = param.getValue();
            double d = BigDecimal.valueOf(r.getTotalamount()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleDoubleProperty(d);
        });
    }

    public void configArrBillTable() {
        col_bill_arr_designation.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture r = param.getValue();

            return new SimpleStringProperty("Periode du "
                    + Constants.USER_READABLE_FORMAT.format(r.getStartDate()) + " - "
                    + Constants.USER_READABLE_FORMAT.format(r.getEndDate()));
        });
        col_bill_arr_numero.setCellValueFactory((TableColumn.CellDataFeatures<Facture, String> param) -> {
            Facture r = param.getValue();
            return new SimpleStringProperty(r.getNumero());
        });

        col_bill_prix_arr_unit.setCellValueFactory((TableColumn.CellDataFeatures<Facture, Number> param) -> {
            Facture r = param.getValue();
            double d = BigDecimal.valueOf(r.getTotalamount()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            return new SimpleDoubleProperty(d);
        });
    }

    @FXML
    public void chooseperiod(Event e) {
        if (rlv.isSelected()) {
            if (dpk1.getValue() != null && dpk2.getValue() != null) {
                ObservableList<Relevee> result = FXCollections.observableArrayList(FactureDelegate.findReleveeInterval(this.orga.getUid(),
                        Constants.Datetime.toUtilDate(dpk1.getValue()),
                        Constants.Datetime.toUtilDate(dpk2.getValue())));
                tbreleve.setItems(result);
            } else {
                tbreleve.setItems(relevees);
            }
            totalRlv.setText("Total : " + sumRelev() + " usd");
        } else if (inv.isSelected()) {
            List<Facture> lsf = FactureDelegate.findBillingInInterval(Constants.Datetime.toUtilDate(dpk1.getValue()),
                    Constants.Datetime.toUtilDate(dpk2.getValue()));
            factures.clear();
            factures.addAll(lsf);
        }
    }

    private double sumRelev() {
        ObservableList<Relevee> items = tbreleve.getItems();
        double sum = 0;
        sum = items
                .stream()
                .map((item) -> (item.getPrixunitaire() * item.getQuantite()))
                .reduce(sum, (accumulator, _item) -> accumulator + _item);
        return sum;
    }

    int compteur = 0;

    private String figureOutRef(int countLogic) {
        String reference;
        int tbil = pref.getInt("tranzit_bill", -100);
        switch (countLogic) {
            case 1: {
                String leo = Constants.DATE_ONLY_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(leo)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", leo);
                    if (tbil == -100) {
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
                    if (tbil == -100) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", mois);
                    if (tbil == -100) {
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
                    if (tbil == -100) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", mois);
                    if (tbil == -100) {
                        compteur = 1;
                    }
                }           //            pref.putInt("_bill_counter_", compteur);
                reference = String.format("%06d", compteur);
                break;
            }
            case 4:
                compteur = pref.getInt("_bill_counter_", 0);
                if (tbil == -100) {
                    compteur++;
                }       //            pref.putInt("_bill_counter_", compteur);
                reference = String.format("%08d", compteur);
                break;
            default:
                reference = String.valueOf(((int) (Math.random() * 100000)));
                break;
        }
        return reference;
    }

    @FXML
    public void createBill(Event e) {
        if (dpk1.getValue() != null && dpk2.getValue() != null) {
            wait_insert_pane.setVisible(true);
            Facture f = new Facture(DataId.generate());
            f.setEndDate(Constants.Datetime.toUtilDate(dpk2.getValue()));
            SimpleDateFormat sdf = new SimpleDateFormat("yymmdd");
            f.setNumero(figureOutRef(count_logic) + sdf.format(new Date()));
            f.setOrganisId(orga);
            f.setRegion(region);
            f.setStartDate(Constants.Datetime.toUtilDate(dpk1.getValue()));
            f.setStatus(Constants.BILL_STATUS_UNPAID);
            double s = sumRelev();
            System.out.println("Somme relev " + s);
            f.setTotalamount(s);
            f.setPayedamount(0d);
            Facture factz = FactureDelegate.saveFacture(f);
            if (factz != null) {
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(factz, Constants.ACTION_CREATE, Tables.FACTURE);
                        });
                wait_insert_pane.setVisible(false);
                factures.add(factz);
                MainUI.notify(null, "Succes", "Facture generee avec succes", 3, "info");
            }
        } else {
            MainUI.notify(null, "Erreur", "Selectionner une periode de facturation puis continuer", 3, "error");
        }
    }

    @FXML
    private void exportXlsRelevee(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = Util.exportRelevee(tbreleve.getItems(), orga.getNomOrganisation() + " "
                        + orga.getAdresse(), sumRelev(), "xls");
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @FXML
    private void exportPdfRelevee(Event e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = Util.exportRelevee(tbreleve.getItems(), orga.getNomOrganisation() + " "
                        + orga.getAdresse(), sumRelev(), "pdf");
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    Logger.getLogger(GoodstorageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
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
}
