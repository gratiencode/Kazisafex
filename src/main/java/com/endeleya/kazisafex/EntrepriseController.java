/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import data.Abonnement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tools.SubscriptionUtil;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import data.network.Kazisafe;

import data.Entreprise;
import data.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.Constants;
import tools.FileUtils;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import static tools.Util.centerImage;
import data.helpers.Role;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import okhttp3.RequestBody;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class EntrepriseController implements Initializable {

    @FXML
    private Label txt_nom_entreprise;
    @FXML
    private Label txt_identifiant_eze;
    @FXML
    private Label txt_type_abonnement_eze;
    @FXML
    private Label txt_abonn_value_eze;
    @FXML
    private Label txt_design_balance_eze;
    @FXML
    private TextField tf_identifiant_eze, tf_idnat_eze;
    @FXML
    private TextField tf_ville_eze, tf_numero_impot;
    @FXML
    private TextField tf_adresse_eze;
    @FXML
    private TextField tf_site_web_eze;
    @FXML
    private TextField tf_nom_eze;
    @FXML
    private TextField tf_email_eze, tf_phone_eze;
    @FXML
    private ComboBox<String> cbx_domaine_eze;
    @FXML
    private ListView<String> list_sucursale_eze;
    ObservableList<String> regions;
    @FXML
    private ImageView img_logo_eze;

    @FXML
    Pane pane_progress;

    @FXML
    private Label txt_adresse_eze;
    File choosenFile;
    private static int GOLD = 0x0;

    ResourceBundle bundle;

    private static EntrepriseController instance;
    @FXML
    private Label txt_subscripro;

    public EntrepriseController() {
        instance = this;
    }

    public static EntrepriseController getInstance() {
        if (instance == null) {
            instance = new EntrepriseController();
        }
        return instance;
    }

    Entreprise eze;
    Kazisafe ksf;
    User user;
    Preferences pref;
    String role;

    public void setup(Entreprise e, Kazisafe kazisafe, User user) {
        this.eze = e;
        this.ksf = kazisafe;
        this.user = user;
        regions = FXCollections.observableArrayList();
        list_sucursale_eze.setItems(regions);
        txt_adresse_eze.setText(eze.getAdresse());
        txt_identifiant_eze.setText(eze.getIdentification());
        txt_nom_entreprise.setText(eze.getNomEntreprise());
        choosenFile = FileUtils.pointFile(eze.getUid() + ".png");
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        role = pref.get("priv", "uknown");
        InputStream is;
        if (!choosenFile.exists()) {
            is = MainuiController.class.getResourceAsStream("/icons/office-building.png");
            choosenFile = FileUtils.streamTofile(is);
        }
        Image image = null;
        try {
            image = new Image(new FileInputStream(choosenFile));
            img_logo_eze.setImage(image);
            Util.centerImage(img_logo_eze);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ksf.getRegions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.setAll(lreg);
                    System.err.println("Eze regions " + lreg.size());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable thrwbl) {

            }
        });
        kazisafe.getAbonnements().enqueue(new Callback<List<Abonnement>>() {
            @Override
            public void onResponse(Call<List<Abonnement>> call, Response<List<Abonnement>> rspns) {
                if (rspns.isSuccessful()) {
                    List<Abonnement> abns = rspns.body();
                    for (Abonnement abn : abns) {
                        String etat = abn.getEtat();
                        String typeAb = abn.getTypeAbonnement();

                        switch (typeAb) {
                            case "Gold", "Metal", "Super gold" -> {
                                pref.put("type-sub", typeAb);
                                String status = SubscriptionUtil.computeStatus(abn);
                                Duration time = SubscriptionUtil.remainingDuration(abn);
                                if (time.minusDays(7).isZero()) {
                                    MainUI.notify(null, "Attention", "Le crédit Kazisafe (Record) expire bientôt, pensez à le renouveller", 5, "warning");
                                }
                                pref.putDouble("sub", Double.valueOf(
                                        SubscriptionUtil.nextSubscriptionMillis(abn)));
                                pref.put("etat-sub", etat);
                                if (!status.equals(Constants.ETAT_SUBSCRIPTION_EXPIRY)) {
                                    MainUI.notifySync("Kazisafe-Abonnement", "Activation souscription " + typeAb + " faite avec succes", "Notification de souscription au service kazisafe");
                                }
                            }
                            case "PRO" -> {
                                double nombreOper = abn.getNombreOperation();
                                pref.put("pro-sub", typeAb);
                                pref.putDouble("subscripro", nombreOper);
                                pref.put("pro-etat", etat);
                                MainUI.notifySync("Kazisafe-Abonnement", "Abonnement " + typeAb + " de " + formatNumber(nombreOper) + " eBonus active", "Notification de souscription au service kazisafe");
                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<List<Abonnement>> call, Throwable thrwbl) {
                System.err.println("No network");
            }
        });
        go();
    }

    private String formatNumber(double val) {
        BigDecimal bd = new BigDecimal(val);
        bd = bd.setScale(2, RoundingMode.UNNECESSARY);
        double value = bd.doubleValue();
        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        } else {
            return Double.toString(value);
        }
    }

    public boolean go() {
        if (txt_type_abonnement_eze != null && txt_abonn_value_eze != null) {
            double max = pref.getDouble("sub", 0);
            double pro = pref.getDouble("subscripro", 0);
            String proetat = pref.get("pro-etat", "INVALID");
            String type = pref.get("type-sub", "Gold");
            txt_subscripro.setText("PRO: " + formatNumber(pro) + " eBonus (" + proetat + ")");
            if (type.equalsIgnoreCase("Gold")) {
                txt_abonn_value_eze.setText(String.valueOf(max));
                txt_design_balance_eze.setText("Record");
                txt_type_abonnement_eze.setText(type);
                txt_type_abonnement_eze.setTextFill(Paint.valueOf("#f7fa45"));
            } else {
                double d2 = System.currentTimeMillis();
                double remaind = max - d2;
                long week = tools.Constants.MILLSECONDS_JOURN * 7;
                if (Math.abs(remaind) <= week) {
                    MainUI.notify(null, bundle.getString("warning"), bundle.getString("subscriptionwarn"), 5, "warning");
                }
                double jrc = remaind / Constants.MILLSECONDS_JOURN;
                double jrs = BigDecimal.valueOf(jrc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                txt_abonn_value_eze.setText(String.valueOf(jrs));
                txt_design_balance_eze.setText(bundle.getString("xday"));
                txt_type_abonnement_eze.setText(type);
                txt_type_abonnement_eze.setTextFill(Paint.valueOf(String.valueOf("#dddddd")));
            }
        }
        return false;
    }

    @FXML
    public void setOnFields(Event e) {
        tf_nom_eze.setText(txt_nom_entreprise.getText());
        tf_identifiant_eze.setText(txt_identifiant_eze.getText());
        tf_adresse_eze.setText(txt_adresse_eze.getText());
        tf_email_eze.setText(this.eze.getEmail());
        tf_site_web_eze.setText(this.eze.getWebsite());
        tf_ville_eze.setText(this.eze.getAdresse().split("-")[0]);
        tf_idnat_eze.setText(this.eze.getIdNat());
        tf_phone_eze.setText(this.eze.getPhones());
        tf_numero_impot.setText(this.eze.getNumeroImpot());
    }

    @FXML
    public void chooseLogo(Event evt) {
        Node node = (Node) evt.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("selectlogo"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("pichaonly"), "*.jpg", "*.png", "*.jpeg", "*.jfif"));
        choosenFile = fileChooser.showOpenDialog(thisStage);
        if (choosenFile != null && choosenFile.length() < 5000000) {
            InputStream fis = null;
            try {
                fis = new FileInputStream(choosenFile);
                Image image = new Image(fis);
                img_logo_eze.setImage(image);
                centerImage(img_logo_eze);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void save2cloud(Entreprise ese) {
        RequestBody rNomEze = RequestBody.create(MediaType.parse("text/plain"), ese.getNomEntreprise());
        RequestBody rIdent = RequestBody.create(MediaType.parse("text/plain"), ese.getIdentification());
        RequestBody rAdress = RequestBody.create(MediaType.parse("text/plain"), ese.getAdresse());
        RequestBody rDomain = RequestBody.create(MediaType.parse("text/plain"), ese.getCategory());
        RequestBody rMail = RequestBody.create(MediaType.parse("text/plain"), ese.getEmail());
        RequestBody rTypeId = RequestBody.create(MediaType.parse("text/plain"), ese.getTypeIdentification());
        RequestBody rWeb = RequestBody.create(MediaType.parse("text/plain"), ese.getWebsite());
        RequestBody rLat = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(ese.getLatitude()));
        RequestBody rLong = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(ese.getLongitude()));
        RequestBody rDate = RequestBody.create(MediaType.parse("text/plain"), Constants.DATE_HEURE_FORMAT.format(new Date()));
        RequestBody rPriv = RequestBody.create(MediaType.parse("text/plain"), Role.Trader.name());
        RequestBody rReg = RequestBody.create(MediaType.parse("text/plain"), tf_ville_eze.getText());
        RequestBody rDescrip = RequestBody.create(MediaType.parse("text/plain"), "Fondateur");
        RequestBody rUsername = RequestBody.create(MediaType.parse("text/plain"), user.getPrenom());
        RequestBody rUserId = RequestBody.create(MediaType.parse("text/plain"), user.getUid());
        MultipartBody.Part rLogo = MultipartBody.Part.createFormData("logo", choosenFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), choosenFile));
        ksf.createEntreprise(rLogo,
                rIdent, rTypeId,
                rAdress, rDomain,
                rNomEze, rLat,
                rLong, rDate,
                rMail, rWeb,
                rUserId, rPriv,
                rReg, rDescrip,
                rUsername).enqueue(new Callback<Entreprise>() {
                    @Override
                    public void onResponse(Call<Entreprise> call, Response<Entreprise> rspns) {
                        System.out.println("Creqtion en trep result " + rspns.message());
                        if (rspns.isSuccessful()) {
                            MainUI.notify(null, bundle.getString("success"), bundle.getString("newcompsaved"), 4, "Info");
                        }
                        if (rspns.code() == 409) {
                            MainUI.notify(null, bundle.getString("error"), bundle.getString("compexist"), 4, "error");
                        }
                    }

                    @Override
                    public void onFailure(Call<Entreprise> call, Throwable thrwbl) {
                        MainUI.notify(null, bundle.getString("error"), bundle.getString("networkerror"), 4, "error");
                    }
                });
    }

    public void updateOnCloud(Entreprise ese, String uid) {
        if (!role.equals(Role.Trader.name())) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("moreprivmsg"), 3, "error");
            return;
        }
        MultipartBody.Part rLogo = MultipartBody.Part.createFormData("logo", choosenFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), choosenFile));
        RequestBody rNomEze = RequestBody.create(MediaType.parse("text/plain"), ese.getNomEntreprise());
        RequestBody rIdent = RequestBody.create(MediaType.parse("text/plain"), ese.getIdentification());
        RequestBody rAdress = RequestBody.create(MediaType.parse("text/plain"), ese.getAdresse());
        RequestBody rDomain = RequestBody.create(MediaType.parse("text/plain"), ese.getCategory());
        RequestBody rMail = RequestBody.create(MediaType.parse("text/plain"), ese.getEmail());
        RequestBody rTypeId = RequestBody.create(MediaType.parse("text/plain"), ese.getTypeIdentification() == null ? "RCCM" : ese.getTypeIdentification());
        RequestBody rWeb = RequestBody.create(MediaType.parse("text/plain"), ese.getWebsite() == null ? "-" : ese.getWebsite());
        RequestBody rLat = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(ese.getLatitude()));
        RequestBody rLong = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(ese.getLongitude()));
        RequestBody rImpot = RequestBody.create(MediaType.parse("text/plain"), ese.getNumeroImpot());
        RequestBody rIdnat = RequestBody.create(MediaType.parse("text/plain"), ese.getIdNat() == null ? "-" : ese.getIdNat());
        RequestBody rPhone = RequestBody.create(MediaType.parse("text/plain"), ese.getPhones() == null ? "-" : ese.getPhones());
        pane_progress.setVisible(true);
        ksf.updateEntreprise(rLogo, rIdent, rTypeId, rAdress, rDomain, rNomEze, rLat,
                rLong, rMail, rWeb, rImpot, rIdnat, rPhone, uid)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rspns) {
                        pane_progress.setVisible(false);
                        System.out.println("Update eze result " + rspns.message());
                        if (rspns.isSuccessful()) {
                            MainUI.notify(null, bundle.getString("success"), bundle.getString("compupdated"), 4, "INFO");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable thrwbl) {
                        MainUI.notify(null, bundle.getString("error"), bundle.getString("networkerror"), 4, "error");
                    }
                });
    }

    @FXML
    public void saveNewEntreprise(Event e) {
        if (tf_nom_eze.getText().isEmpty()
                || tf_adresse_eze.getText().isEmpty()
                || tf_email_eze.getText().isEmpty()
                || tf_identifiant_eze.getText().isEmpty()
                || tf_ville_eze.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("fillfield"), 4, "error");
            return;
        }
        Entreprise ese = new Entreprise();
        ese.setAdresse(tf_adresse_eze.getText());
        ese.setCategory(cbx_domaine_eze.getValue());
        ese.setDateCreation(LocalDateTime.now());
        ese.setEmail(tf_email_eze.getText());
        ese.setIdentification(tf_identifiant_eze.getText());
        ese.setLatitude(eze.getLatitude());
        ese.setLongitude(eze.getLongitude());
        ese.setNomEntreprise(tf_nom_eze.getText());
        ese.setTypeIdentification(eze.getTypeIdentification());
        ese.setNumeroImpot(tf_numero_impot.getText());
        ese.setIdNat(tf_idnat_eze.getText());
        ese.setPhones(tf_phone_eze.getText());
        save2cloud(ese);
    }

    @FXML
    public void updateEntreprise(Event e) {
        if (tf_nom_eze.getText().isEmpty()
                || tf_adresse_eze.getText().isEmpty()
                || tf_email_eze.getText().isEmpty()
                || tf_identifiant_eze.getText().isEmpty()
                || tf_ville_eze.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("fillfield"), 4, "error");
            return;
        }
        eze.setAdresse(tf_adresse_eze.getText());
        eze.setCategory(cbx_domaine_eze.getValue());
        eze.setDateCreation(LocalDateTime.now());
        eze.setEmail(tf_email_eze.getText());
        eze.setIdentification(tf_identifiant_eze.getText());
        eze.setNomEntreprise(tf_nom_eze.getText());
        eze.setWebsite(tf_site_web_eze.getText());
        eze.setNumeroImpot(tf_numero_impot.getText());
        eze.setIdNat(tf_idnat_eze.getText());
        eze.setPhones(tf_phone_eze.getText());
        updateOnCloud(eze, eze.getUid());
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;
        cbx_domaine_eze.setItems(FXCollections.observableArrayList(bundle.getString("comp.detailsnwhole"),
                bundle.getString("comp.detailor"), bundle.getString("comp.wholesaler"), bundle.getString("comp.industry"),
                bundle.getString("comp.service"), bundle.getString("comp.spectacle")));
        cbx_domaine_eze.getSelectionModel().selectFirst();
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
