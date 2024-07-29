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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import data.network.Kazisafe;
import data.Entreprise;
import data.Fournisseur;
import data.Livraison;
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
    ImageView btn_export;

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

    Fournisseur choosenf;
    Livraison choosenLiv;
    String token;
    String action = "create";

    ObservableList<Fournisseur> lisfournisseur;
    ObservableList<Livraison> lisvraison;

    private static SuppliersController instance;

    public SuppliersController() {
        //store=JpaStorage.getInstance();
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
        listvu_list_fournisseurs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Fournisseur>() {
            @Override
            public void changed(ObservableValue<? extends Fournisseur> observable, Fournisseur oldValue, Fournisseur newValue) {
                if (newValue != null) {
                    choosenf = newValue;
                    tf_nom_fourn.setText(choosenf.getNomFourn());
                    tf_phone_fourn.setText(choosenf.getPhone());
                    tf_identif_fourn.setText(choosenf.getIdentification());
                    tf_adresse_fourn.setText(choosenf.getAdresse());
                }
            }
        });
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
        if (tf_adresse_fourn.getText().isEmpty() || tf_nom_fourn.getText().isEmpty() || tf_phone_fourn.getText().isEmpty() || tf_identif_fourn.getText().isEmpty()) {
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        pref = Preferences.userNodeForPackage(SyncEngine.class);

        taux = pref.getDouble("taux2change", 2000);
//        store=JpaStorage.getInstance();
        Tooltip.install(btn_export, new Tooltip("Exporter vers un fichier excel"));
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
                    FournisseurDelegate.deleteFournisseur(lsf);//store.delete(lsf);
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

    private void saveSupplierByHttp(Fournisseur f){
        ksf.saveSupplier(f).enqueue(new Callback<Fournisseur>() {
            @Override
            public void onResponse(Call<Fournisseur> call, Response<Fournisseur> rspns) {
                System.out.println("Fournisseur "+rspns.message());
            }

            @Override
            public void onFailure(Call<Fournisseur> call, Throwable thrwbl) {
              thrwbl.printStackTrace();
            }
        });
    }
    
}
