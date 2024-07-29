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
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import data.network.Kazisafe;

import data.Entreprise;
import data.Fournisseur;
import data.Livraison;
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
public class DeliveryController implements Initializable {

    private static DeliveryController instance;
    Kazisafe kazisafe;
    Preferences pref;
    @FXML
    ComboBox<Fournisseur> cbx_choose_fourniss_lic;
    @FXML
    private TextField tf_numpiece;
    @FXML
    private TextField tf_libelle;
    @FXML
    private TextField tf_montantfss;
    @FXML
    private TextField tf_montantpaye;
    @FXML
    private TextField tf_reduction;
    @FXML
    private TextField tf_observation;
    @FXML
    DatePicker dpk_dateLivr;
    ResourceBundle RB;
    @FXML Button bntsave;

    public static DeliveryController getInstance() {
        if (instance == null) {
            instance = new DeliveryController();
        }
        return instance;
    }
    Livraison choosenLivraison;
    Fournisseur choosenSupplier;
    Entreprise ent;
    String region, role, token, entr, action = "create";
    ObservableList<Fournisseur> list_fourn;

    public DeliveryController() {
        instance = this;
    }

    public void gotoSuplier(Event e) {
        MainUI.floatDialog(tools.Constants.FOURNISSEUR_DLG, 1090, 355, null, kazisafe, ent, null);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.RB=rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        region = pref.get("region", "...");
        role = pref.get("priv", null);
        token = pref.get("token", null);
        entr = pref.get("eUid", "");
        bntsave.setText(RB.getString("xbtn.save")+" >>");
        MainUI.setPattern(dpk_dateLivr);
        dpk_dateLivr.setValue(LocalDate.now());
        kazisafe = KazisafeServiceFactory.createService(token);
        cbx_choose_fourniss_lic.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur object) {
                return object == null ? null : object.getNomFourn() + ", " + object.getAdresse() + " " + object.getPhone();
            }

            @Override
            public Fournisseur fromString(String string) {
                return cbx_choose_fourniss_lic.getItems()
                        .stream()
                        .filter(f -> (f.getNomFourn() + ", " + f.getAdresse() + " " + f.getPhone())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            } 
        });
        cbx_choose_fourniss_lic.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Fournisseur>() {
            @Override
            public void changed(ObservableValue<? extends Fournisseur> observable, Fournisseur oldValue, Fournisseur newValue) {
                if (newValue != null) {
                    choosenSupplier = newValue;
                }
            }
        });
    }

    public void setUp(Entreprise entreprise, Livraison livraison) {
        this.ent = entreprise;
        if (livraison != null) {
            this.choosenLivraison = livraison;
            tf_libelle.setText(livraison.getLibelle());
            tf_montantfss.setText(String.valueOf(livraison.getTopay()));
            tf_montantpaye.setText(String.valueOf(livraison.getPayed()));
            tf_numpiece.setText(livraison.getNumPiece());
            tf_observation.setText(livraison.getObservation());
            tf_reduction.setText(String.valueOf(livraison.getReduction()));
            action = "update";
        }
        List<Fournisseur> fss = FournisseurDelegate.findFournisseurs();
        list_fourn = FXCollections.observableArrayList();
        list_fourn.addAll(fss);
        cbx_choose_fourniss_lic.setItems(list_fourn);
        cbx_choose_fourniss_lic.getSelectionModel().selectFirst();
        ComboBoxAutoCompletion<Fournisseur> com = new ComboBoxAutoCompletion<>(cbx_choose_fourniss_lic);

    }

    @FXML
    public void saveLivraison(Event e) {
        if (action.equals("create")) {
            if (tf_numpiece.getText().isEmpty()
                    || tf_libelle.getText().isEmpty()
                    || tf_montantfss.getText().isEmpty() || choosenSupplier == null) {
                MainUI.notify(null, "Erreur", "Completer tout les non facultatif", 3, "error");
                return;
            }
            Livraison newl = new Livraison(DataId.generate());
            newl.setDateLivr(dpk_dateLivr.getValue());
            newl.setFournId(choosenSupplier);
            newl.setLibelle(tf_libelle.getText());
            newl.setNumPiece(tf_numpiece.getText());
            newl.setObservation(tf_observation.getText());
            double pyd = Double.valueOf(tf_montantpaye.getText().isEmpty() ? "0" : tf_montantpaye.getText());
            newl.setPayed(pyd);
            String dt = Constants.TIMESTAMPED_FORMAT.format(new Date());
            dt = dt.substring(dt.length() - 8, dt.length());
            newl.setReference("TXN" + ((int) (Math.random() * 1000)) + dt + "STK");
            newl.setRegion(region);
            double topay = Double.valueOf(tf_montantfss.getText());
            newl.setTopay(topay);
            double red;
            if (tf_reduction.getText().contains("%")) {
                String name = tf_reduction.getText().split("%")[0];
                if (name != null) {
                    double pred = Double.valueOf(name);
                    red = (topay * pred) / 100;
                } else {
                    red = 0;
                }
            } else {
                red = Double.valueOf(tf_reduction.getText().isEmpty() ? "0" : tf_reduction.getText());
            }
            newl.setReduction(red);
            newl.setRemained(((topay - pyd) - red) < 0 ? 0 : ((topay - pyd) - red));
            newl.setToreceive(0d);
            Livraison svl = LivraisonDelegate.saveLivraison(newl);
            saveLivraisonByHttp(newl);
            if (svl != null) {
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(svl, Constants.ACTION_CREATE, Tables.LIVRAISON);
                        });
                GoodstorageController.getInstance().populateDelivery(Constants.ACTION_CREATE, svl);
                MainUI.notify(null, "Succes", "Facture enregistrer avec success", 3, "info");
                MainUI.floatDialog(tools.Constants.STOCKAGE_DLG, 716, 746, null, kazisafe, svl, tools.Constants.ACTION_CREATE, ent, null);
                close(e);
            }
          } else {
            if (tf_numpiece.getText().isEmpty()
                    || tf_libelle.getText().isEmpty()
                    || tf_montantfss.getText().isEmpty() || choosenSupplier == null) {
                MainUI.notify(null, "Erreur", "Completer tout les non facultatif", 3, "error");
                return;
            }
            choosenLivraison.setDateLivr(dpk_dateLivr.getValue());
            choosenLivraison.setFournId(choosenSupplier);
            choosenLivraison.setLibelle(tf_libelle.getText());
            choosenLivraison.setNumPiece(tf_numpiece.getText());
            choosenLivraison.setObservation(tf_observation.getText());
            double pyd = Double.parseDouble(tf_montantpaye.getText().isEmpty() ? "0" : tf_montantpaye.getText());
            choosenLivraison.setPayed(pyd);
            choosenLivraison.setReference("STK" + ((int) (Math.random() * 10000)) + "L");
            choosenLivraison.setRegion(region);
            double topay = Double.parseDouble(tf_montantfss.getText());
            choosenLivraison.setTopay(topay);
            double red;
            if (tf_reduction.getText().contains("%")) {
                String name = tf_reduction.getText().split("%")[0];
                if (name != null) {
                    double pred = Double.valueOf(name);
                    red = (topay * pred) / 100;
                } else {
                    red = 0;
                }
            } else {
                red = Double.valueOf(tf_reduction.getText().isEmpty() ? "0" : tf_reduction.getText());
            }
            choosenLivraison.setReduction(red);
            choosenLivraison.setRemained(((topay - pyd) - red) < 0 ? 0 : ((topay - pyd) - red));
            choosenLivraison.setToreceive(0d);
            Livraison svl = LivraisonDelegate.updateLivraison(choosenLivraison);
            if (svl != null) {
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(svl, Constants.ACTION_UPDATE, Tables.LIVRAISON);
                        });
                GoodstorageController.getInstance().populateDelivery(Constants.ACTION_UPDATE, svl);
                MainUI.notify(null, "Succes", "Facture modifiee avec success", 3, "info");
            }
        }
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
    /**  @Field("npiece") String numPiece,
            @Field("dateLivr") String dateLivr,
            @Field("reference") String reference,
            @Field("libelle") String libelle,
            @Field("reduction") String reduction,
            @Field("observation") String observation,
            @Field("topay") String topay,
            @Field("payed") String payed,
            @Field("ramained") String remained,
            @Field("toreceive") String toreceive,
            @Field("fournId") String fournId**/
    
    private void saveLivraisonByHttp(Livraison l){
        kazisafe.syncDelivery(l.getUid(),l.getNumPiece(),l.getDateLivr().toString(),
                l.getReference(),l.getLibelle(),l.getReduction().toString(),l.getObservation(), 
                l.getTopay().toString(),l.getPayed().toString(),l.getRemained().toString(),
                l.getToreceive().toString(),l.getFournId().getUid())
                .enqueue(new Callback<Livraison>() {
            @Override
            public void onResponse(Call<Livraison> call, Response<Livraison> rspns) {
                System.out.println("Livraison "+rspns.code());
            }

            @Override
            public void onFailure(Call<Livraison> call, Throwable thrwbl) {
               thrwbl.printStackTrace();
            }
        });
    }
}
