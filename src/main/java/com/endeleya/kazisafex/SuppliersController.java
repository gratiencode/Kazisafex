/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.FournisseurDelegate;
import delegates.LivraisonDelegate;
import data.core.KazisafeServiceFactory;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
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
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.time.LocalDate;
import data.network.Kazisafe;
import data.Entreprise;
import data.Fournisseur;
import data.Livraison;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
public class SuppliersController implements Initializable {

    public static SuppliersController getInstance() {
        if (instance == null) {
            instance = new SuppliersController();
        }
        return instance;
    }

    /**
     * Initializes the controller class.
     */
    Preferences pref;
    double taux;
    Kazisafe ksf;
    @FXML
    Pane pane_progress_fourn;
    @FXML
    ImageView btn_export_pdf;

    @FXML
    private TextField tf_nom_fourn;
    @FXML
    private TextField tf_phone_fourn;
    @FXML
    private TextField tf_identif_fourn;
    @FXML
    TextField tf_search2cloud;
    @FXML
    Label txt_somme_values;
    @FXML
    private TextField tf_adresse_fourn, tf_search_livraison;
    @FXML
    private ListView<Fournisseur> listvu_list_fournisseurs;

    // Debt Management Tab
    @FXML
    private TextField tf_filter_supplier_debt;
    @FXML
    private ListView<Fournisseur> listvu_suppliers_debt;
    @FXML
    private TableView<Livraison> tb_debt_details;
    @FXML
    private TableColumn<Livraison, LocalDate> col_date;
    @FXML
    private TableColumn<Livraison, String> col_numpiece;
    @FXML
    private TableColumn<Livraison, String> col_libelle;
    @FXML
    private TableColumn<Livraison, Double> col_topay;
    @FXML
    private TableColumn<Livraison, Double> col_payed;
    @FXML
    private TableColumn<Livraison, Double> col_remained;
    @FXML
    private Label lbl_total_debt;
    @FXML
    private CheckBox chk_show_unpaid_only;

    Fournisseur choosenf;
    Livraison choosenLiv;
    String token;
    String action = "create";

    Entreprise entreprise;
    ObservableList<Fournisseur> lisfournisseur;
    ObservableList<Livraison> lisvraison;

    private static SuppliersController instance;

    public SuppliersController() {
        // store=JpaStorage.getInstance();
        lisfournisseur = FXCollections.observableArrayList();
    }

    public void addSupplier(Fournisseur ff) {
        if (ff == null) {
            return;
        }
        Fournisseur f = FournisseurDelegate.findFournisseur(ff.getUid());
        if (f == null) {
            FournisseurDelegate.saveFournisseur(ff);
        }
    }

    public void setDataSource(Entreprise eze, Fournisseur fourn) {
        this.token = pref.get("token", null);
        this.choosenf = fourn;
        this.entreprise = eze;
        ksf = KazisafeServiceFactory.createService(this.token);
        lisvraison = FXCollections.observableArrayList(LivraisonDelegate.findLivraisons());
        lisfournisseur = FXCollections.observableArrayList(FournisseurDelegate.findFournisseurs());
        listvu_list_fournisseurs.setItems(lisfournisseur);
        if (choosenf != null) {
            tf_nom_fourn.setText(choosenf.getNomFourn());
            tf_phone_fourn.setText(choosenf.getPhone());
            tf_identif_fourn.setText(choosenf.getIdentification());
            tf_adresse_fourn.setText(choosenf.getAdresse());
            this.action = "update";
        }
        listvu_list_fournisseurs.setCellFactory((ListView<Fournisseur> param) -> new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getNomFourn() + " " + item.getAdresse() + " phone :" + item.getPhone());
                        }
                    });
                }
            }

        });
        listvu_list_fournisseurs.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Fournisseur>() {
                    @Override
                    public void changed(ObservableValue<? extends Fournisseur> observable, Fournisseur oldValue,
                            Fournisseur newValue) {
                        if (newValue != null) {
                            choosenf = newValue;
                            tf_nom_fourn.setText(choosenf.getNomFourn());
                            tf_phone_fourn.setText(choosenf.getPhone());
                            tf_identif_fourn.setText(choosenf.getIdentification());
                            tf_adresse_fourn.setText(choosenf.getAdresse());
                        }
                    }
                });

        // Debt management initialization
        listvu_suppliers_debt.setItems(lisfournisseur);
        listvu_suppliers_debt.setCellFactory(listvu_list_fournisseurs.getCellFactory());
        listvu_suppliers_debt.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showDebtDetails(newVal);
            }
        });

        tf_filter_supplier_debt.textProperty().addListener((obs, oldVal, newValue) -> {
            if (!newValue.isEmpty()) {
                List<Fournisseur> lf = Util.findFournisseur(FournisseurDelegate.findFournisseurs(), newValue);
                listvu_suppliers_debt.setItems(FXCollections.observableArrayList(lf));
            } else {
                listvu_suppliers_debt.setItems(lisfournisseur);
            }
        });

        col_date.setCellValueFactory(new PropertyValueFactory<>("dateLivr"));
        col_date.setPrefWidth(120);

        col_numpiece.setCellValueFactory(new PropertyValueFactory<>("numPiece"));
        col_numpiece.setPrefWidth(150);

        col_libelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        col_libelle.setPrefWidth(200);

        col_topay.setCellValueFactory(new PropertyValueFactory<>("topay"));
        col_topay.setPrefWidth(100);

        col_payed.setCellValueFactory(new PropertyValueFactory<>("payed"));
        col_payed.setPrefWidth(100);

        col_remained.setCellValueFactory(new PropertyValueFactory<>("remained"));
        col_remained.setPrefWidth(100);

        // Context menu for debt payment
        ContextMenu debtCm = new ContextMenu();
        MenuItem payItem = new MenuItem("Paiement de la dette");
        debtCm.getItems().add(payItem);
        tb_debt_details.setContextMenu(debtCm);

        payItem.setOnAction(e -> {
            Livraison selected = tb_debt_details.getSelectionModel().getSelectedItem();
            if (selected != null) {
                MainuiController.getInstance().switchScreens(Constants.CAISSE_VIEW, Constants.CAISSES, "Trésorerie",
                        "cashier.png", null, selected);
            }
        });

        // Ajout du listener pour la case à cocher de filtre
        if (chk_show_unpaid_only != null) {
            chk_show_unpaid_only.setOnAction(e -> {
                Fournisseur selectedSupplier = listvu_suppliers_debt.getSelectionModel().getSelectedItem();
                if (selectedSupplier != null) {
                    showDebtDetails(selectedSupplier);
                }
            });
        }
    }

    private void showDebtDetails(Fournisseur f) {
        List<Livraison> debts = LivraisonDelegate.findBySupplier(f.getUid());
        ObservableList<Livraison> obsDebts = FXCollections.observableArrayList();
        double total = 0;
        if (debts != null) {
            for (Livraison l : debts) {
                // Calcul du remained = toPay - payed
                double remained = l.getTopay() - l.getPayed();
                l.setRemained(remained);

                // Si la case est cochée, n'afficher que les dettes non soldées
                if (chk_show_unpaid_only != null && chk_show_unpaid_only.isSelected()) {
                    if (remained > 0) {
                        obsDebts.add(l);
                        total += remained;
                    }
                } else {
                    // Sinon afficher toutes les livraisons
                    obsDebts.add(l);
                    if (remained > 0) {
                        total += remained;
                    }
                }
            }
        }
        tb_debt_details.setItems(obsDebts);
        lbl_total_debt.setText(String.format("%.2f USD", total));
    }

    @FXML
    public void searchSupplyOnCloud(ActionEvent e) {
        String value = tf_search2cloud.getText();
        if (value.isEmpty()) {
            listvu_list_fournisseurs.setItems(lisfournisseur);
            return;
        }
        pane_progress_fourn.setVisible(true);
        ksf.showSuppliersByName(value)
                .enqueue(new Callback<List<Fournisseur>>() {
                    @Override
                    public void onResponse(Call<List<Fournisseur>> call, Response<List<Fournisseur>> rspns) {
                        pane_progress_fourn.setVisible(false);
                        if (rspns.isSuccessful()) {
                            List<Fournisseur> fs = rspns.body();
                            listvu_list_fournisseurs.setItems(FXCollections.observableArrayList(fs));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Fournisseur>> call, Throwable thrwbl) {
                        pane_progress_fourn.setVisible(false);
                        System.out.println("Erreur " + thrwbl.getMessage());
                    }
                });
    }

    @FXML
    public void saveSupplier(Event e) {
        if (tf_adresse_fourn.getText().isEmpty() || tf_nom_fourn.getText().isEmpty()
                || tf_phone_fourn.getText().isEmpty() || tf_identif_fourn.getText().isEmpty()) {
            MainUI.notify(null, "Error", "Completez tout les champs", 4, "error");
            return;
        }

        if (this.action.equals("create")) {
            List<Fournisseur> fext = FournisseurDelegate.findByPhone(tf_phone_fourn.getText());
            if (!fext.isEmpty()) {
                MainUI.notify(null, "Error", "Il semble que ce fournisseur existe deja", 4, "error");
                return;
            }
            Fournisseur f = new Fournisseur(DataId.generate());
            f.setAdresse(tf_adresse_fourn.getText());
            f.setIdentification(tf_identif_fourn.getText());
            f.setNomFourn(tf_nom_fourn.getText());
            f.setPhone(tf_phone_fourn.getText());
            Fournisseur frs = FournisseurDelegate.saveFournisseur(f);

            saveSupplierByHttp(f);

            if (frs != null) {
                DeliveryController.getInstance().addSupplier(f);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(frs, Constants.ACTION_CREATE, Tables.FOURNISSEUR);
                        });
                GoodstorageController.getInstance().populateSupplier(Constants.ACTION_CREATE, frs);
                MainUI.notify(null, "Succes", "Fournisseur enregistre avec succes", 4, "Info");
            }
        } else {
            if (choosenf != null) {
                choosenf.setAdresse(tf_adresse_fourn.getText());
                choosenf.setIdentification(tf_identif_fourn.getText());
                choosenf.setNomFourn(tf_nom_fourn.getText());
                choosenf.setPhone(tf_phone_fourn.getText());
                Fournisseur f = FournisseurDelegate.updateFournisseur(choosenf);
                if (f != null) {
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                Util.sync(f, Constants.ACTION_UPDATE, Tables.FOURNISSEUR);
                            });
                    GoodstorageController.getInstance().populateSupplier(Constants.ACTION_UPDATE, f);
                    MainUI.notify(null, "Succès", "Fournisseur modifié avec succes", 4, "Info");
                }
            }
        }
        choosenf = null;
    }

    @FXML
    private void exportSuppliersDebt(MouseEvent event) {
        Fournisseur selected = listvu_suppliers_debt.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // If a specific supplier is selected, export their detailed statement
            Util.exportXlsSupplierStatement(selected, tb_debt_details.getItems());
        } else {
            // Otherwise export all suppliers with debts as before
            Util.exportXlsSuppliersDebt(lisfournisseur);
        }
    }

    @FXML
    private void exportSuppliersDebtPdf(MouseEvent event) {
        if (choosenf == null) {
            MainUI.notify(null, "Attention", "Veuillez selectionner un fournisseur", 2, "warning");
            return;
        }
        Executors.newSingleThreadExecutor()
                .submit(() -> { 
                    File f = Util.exportPdfSupplierStatement(choosenf, tb_debt_details.getItems(), entreprise);
                    if (f != null) {
                        MainUI.notify(null, "Succès", "L'état de dette a été exporté en PDF", 4, "info");
                        try {
                            Desktop.getDesktop().open(f);
                        } catch (IOException ex) {
                            Logger.getLogger(SuppliersController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        MainUI.notify(null, "Erreur", "Echec de l'exportation PDF", 4, "error");
                    }
                });

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        pref = Preferences.userNodeForPackage(SyncEngine.class);

        taux = pref.getDouble("taux2change", 2000);
        // store=JpaStorage.getInstance();
        // Configuration de l'icône PDF
        btn_export_pdf.setImage(new Image(SuppliersController.class.getResourceAsStream("/icons/download-pdf.png")));
        btn_export_pdf.setFitHeight(32);
        btn_export_pdf.setFitWidth(32);
        btn_export_pdf.setPreserveRatio(true);
        Tooltip.install(btn_export_pdf, new Tooltip("Exporter en PDF"));
        btn_export_pdf.setOnMouseClicked(this::exportSuppliersDebtPdf);
        ContextMenu cm = new ContextMenu();
        MenuItem m2 = new MenuItem("Supprimer");
        cm.getItems().add(m2);
        listvu_list_fournisseurs.setContextMenu(cm);
        m2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenf != null) {
                    // store.delete("uid", choosenf.getUid());
                    Fournisseur lsf = FournisseurDelegate.findFournisseur(choosenf.getUid());
                    FournisseurDelegate.deleteFournisseur(lsf);// store.delete(lsf);
                    lisfournisseur.remove(choosenf);
                }
            }
        });

        tf_search2cloud.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isEmpty()) {
                    List<Fournisseur> lf = Util.findFournisseur(FournisseurDelegate.findFournisseurs(), newValue);
                    listvu_list_fournisseurs.setItems(FXCollections.observableArrayList(lf));
                } else {
                    listvu_list_fournisseurs.setItems(lisfournisseur);
                }
            }
        });

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
    private void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    private void saveSupplierByHttp(Fournisseur f) {
        ksf.saveSupplier(f).enqueue(new Callback<Fournisseur>() {
            @Override
            public void onResponse(Call<Fournisseur> call, Response<Fournisseur> rspns) {
                System.out.println("Fournisseur " + rspns.message());
            }

            @Override
            public void onFailure(Call<Fournisseur> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

}
