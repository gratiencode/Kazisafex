/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

//
import data.core.KazisafeServiceFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import jakarta.persistence.EntityManager;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import data.network.Kazisafe;
import data.helpers.LoginWebResult;
import org.apache.commons.io.IOUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.PlatformUtil;
import tools.Constants;
import tools.MainUI;
import tools.NetLoockup;
import tools.SyncEngine;
import tools.Util;
import data.helpers.Credentials;

/**
 *
 * @author eroot
 */
public class EntryPointController implements Initializable {

    @FXML
    ComboBox<String> langs;
    @FXML
    ImageView flag_lang;
    @FXML
    Label txt_version, appName;
    @FXML
    CheckBox showpswd;
    @FXML
    Pane portParam;
    @FXML
    TextField inputPort;

    // private static Nitrite initDatabase(final String dbname) {
    // String path = null, fpath = null, secpath;
    // if (PlatformUtil.isWindows()) {
    // path = "C:" + File.separator + "Kazisafe" + File.separator + "datastore";
    // fpath = path + File.separator + dbname + ".db";
    // } else if (PlatformUtil.isLinux()) {
    // path = "/home/" + System.getProperty("user.name") + "/Kazisafe/datastore";
    // fpath = path + File.separator + dbname + ".db";
    // } else if (PlatformUtil.isMac()) {
    // path = "/Users" + File.separator + System.getProperty("user.name") +
    // File.separator + "Kazisafe" + File.separator + "datastore";
    // fpath = path + File.separator + dbname + ".db";
    // }
    // File folder = new File(path);
    // File file = null;
    // boolean dir = folder.exists();
    // if (!dir) {
    // dir = folder.mkdir();
    // }
    // System.out.println("Droit Folder " + dir);
    // if (dir) {
    // file = new File(fpath);
    // // System.out.println("Droit "+file.canWrite());
    // if (!file.exists()) {
    // try {
    // file.createNewFile();
    // } catch (IOException ex) {
    // Logger.getLogger(Kazisafex.class.getName()).log(Level.SEVERE, null, ex);
    // }
    // }
    // }
    // try {
    //
    // Nitrite nitrodb = Nitrite.builder()
    // .filePath(fpath)
    // .compressed()
    // .openOrCreate();
    //
    // return nitrodb;
    // } catch (org.dizitart.no2.exceptions.NitriteIOException ex) {
    // if (ex.getMessage().toUpperCase().contains("NO2.2001: database is already
    // opened in other process".toUpperCase())) {
    //
    // }
    // System.err.println("Erruer " + ex.getMessage());
    // return getLogDatabaseInstance();
    // }
    //
    // }
    //
    @FXML
    public void showPortPane(Event e) {
        portParam.setVisible(true);
    }

    @FXML
    public void createPort(Event e) {
        try {
            pref.putInt("default_mysql_port", Integer.parseInt(inputPort.getText()));
            MainUI.notify(null, "Succes", "Port configuré avec succès", 4, "info");
        } catch (Exception ex) {
            MainUI.notify(null, "Erreur", "Mettez les chiffre uniquement!", 4, "error");
        }
        portParam.setVisible(false);
    }
//
//    public static EntityManager initObjectDatabase(final String dbname) {
//        String path = null, fpath = null, secpath = null, secfile;
//
//        InputStream is = EntryPointController.class.getResourceAsStream("/sec/cacerts.jks");
//        if (PlatformUtil.isWindows()) {
//            path = System.getenv("ProgramData") + File.separator + "Kazisafe" + File.separator + "datastore";
//            secpath = System.getenv("ProgramData") + File.separator + "Kazisafe" + File.separator + ".security";
//            fpath = path + File.separator + dbname + ".odb";
//        } else if (PlatformUtil.isLinux()) {
//            path = "/home/" + System.getProperty("user.name") + "/Kazisafe/datastore";
//            secpath = "/home/" + System.getProperty("user.name") + File.separator + "Kazisafe" + File.separator
//                    + ".security";
//            fpath = path + File.separator + dbname + ".odb";
//        } else if (PlatformUtil.isMac()) {
//
//            path = "/Users" + File.separator + System.getProperty("user.name") + File.separator + "Kazisafe"
//                    + File.separator + "datastore";
//            secpath = "/Users/" + System.getProperty("user.name") + File.separator + "Kazisafe" + File.separator
//                    + ".security";
//            fpath = path + File.separator + dbname + ".odb";
//        }
//        secfile = secpath + File.separator + "cacerts.jks";
//        File folder = new File(path);
//        File secfolder = new File(secpath);
//        File file = null;
//        boolean dir = folder.exists();
//        boolean secdir = secfolder.exists();
//        MainUI.cPath(path);
//        if (!dir) {
//            dir = folder.mkdirs();
//            secdir = secfolder.mkdirs();
//        }
//        System.out.println("Droit Folder " + dir);
//
//        if (dir) {
//            file = new File(fpath);
//            // System.out.println("Droit "+file.canWrite());
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException ex) {
//                    Logger.getLogger(Kazisafex.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            File sec = new File(secfile);
//            try {
//                FileOutputStream fos = new FileOutputStream(sec);
//                // byte[] buffer = FileUtils.readAllBytes(is);
//                IOUtils.copyLarge(is, fos);
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(EntryPointController.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(EntryPointController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            System.setProperty("derby.system.home", folder.getAbsolutePath());
//        }
//
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory(fpath);
//        EntityManager em = emf.createEntityManager();
//
//        return em;
//    }

    @FXML
    TextField pswd_field;
    @FXML
    TextField username_field;
    @FXML
    TextField showpswd_field;
    @FXML
    TextField identification;
    @FXML
    Pane pane_progress;
    Kazisafe kazisafe;
    Preferences pref;
    LoginWebResult genResult = new LoginWebResult();

    ResourceBundle bundle;

    private static EntryPointController instance;

    public static EntryPointController getInstance() {
        return instance;
    }

    public EntryPointController() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        instance = this;
    }

    private void tryAutoLoginWithSavedSession() {
        boolean keepSession = pref.getBoolean("session", false);
        if (!keepSession) {
            return;
        }
        String token = pref.get("token", null);
        String euid = pref.get("eUid", null);
        if (token == null || token.isBlank() || euid == null || euid.isBlank()) {
            return;
        }
        String region = pref.get("region", "Lubumbashi");
        String rl = pref.get("priv", "Trader");
        String uname = pref.get("uname", "");
        genResult.setToken(token);
        genResult.setEntrepriseId(euid);
        genResult.setRegion(region);
        genResult.setRole(rl);
        genResult.setPhone(uname);
        Platform.runLater(() -> {
            Screen scr = Screen.getPrimary();
            double height = scr.getVisualBounds().getHeight();
            double width = scr.getVisualBounds().getWidth();
            MainUI.loadMainView(this.getClass(), "mainuix.fxml", height, width, genResult);
            Kazisafex.stagex.close();
        });
    }

    private void synchronousLogin(Credentials creds) {
        pane_progress.setVisible(true);
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    try {
                        final boolean[] loginSucceeded = new boolean[]{false};
                        Response<LoginWebResult> reponse = kazisafe.desktopSignin(creds).execute();
                        if (reponse.isSuccessful()) {
                            LoginWebResult body = reponse.body();
                            if (body == null) {
                                MainUI.notify(null, bundle.getString("error"), bundle.getString("networkerror"), 3,
                                        "error");
                            } else {
                                Object obj = body;
                                switch (obj) {
                                    case String message -> {
                                        switch (message) {
                                            case "Utilisateur introuvable" ->
                                                MainUI.notify(null, bundle.getString("error"),
                                                        bundle.getString("login.userincorect"), 4, "error");
                                            case "Entreprise introuvable" ->
                                                MainUI.notify(null, bundle.getString("error"),
                                                        bundle.getString("login.compnotfound"), 12, "error");
                                            case "Entreprise suspendue" ->
                                                MainUI.notify(null, bundle.getString("error"),
                                                        bundle.getString("login.suspended"), 16, "Info");
                                            case "Engagement introuvable" ->
                                                MainUI.notify(null, bundle.getString("error"),
                                                        bundle.getString("login.engageinc"), 4, "error");
                                            default -> {
                                            }
                                        }
                                    }
                                    case LoginWebResult lr -> {
                                        if (lr == null || lr.getToken() == null || lr.getToken().isBlank()) {
                                            MainUI.notify(null, bundle.getString("error"),
                                                    bundle.getString("networkerror"), 3, "error");
                                            break;
                                        }
                                        pref.put("eUid", lr.getEntrepriseId());
                                        pref.put("uname", lr.getPhone());
                                        pref.put("region", lr.getRegion());
                                        pref.put("token", lr.getToken());
                                        pref.put("ucontract", lr.getUserContract());
                                        genResult = lr;
                                        loginSucceeded[0] = true;
                                    }
                                    default -> {
                                    }
                                }
                            }
                        } else {
                            MainUI.notify(null, bundle.getString("error"),
                                    "Erreur interne, HTTP: [" + reponse.code() + "]", 15, "error");
                        }
                        Platform.runLater(() -> {
                            pane_progress.setVisible(false);
                            if (loginSucceeded[0]) {
                                Screen scr = Screen.getPrimary();
                                double height = scr.getVisualBounds().getHeight() * 1;
                                double width = scr.getVisualBounds().getWidth() * 1;
                                MainUI.loadMainView(this.getClass(), "mainuix.fxml", height, width, genResult);
                                Kazisafex.stagex.close();
                            }
                        });
                    } catch (IOException ex) {

                        Platform.runLater(() -> {
                            pane_progress.setVisible(false);
                        });
                        pref.putBoolean(NetLoockup.NETWORK_STATUS, NetLoockup.NETWORK_STATUS_DEFAULT);
                        System.out.println("DEMOSTRATION-MODE : " + ex.getMessage());
                        boolean sopen = pref.getBoolean("session", true);
                        if (sopen) {
                            String euid = pref.get("eUid", "__Generic_");
                            String token = pref.get("token", null);
                            String rl = pref.get("priv", "Trader");
                            String region = pref.get("region", "Lubumbashi");
                            if (token == null || token.isBlank()) {
                                MainUI.notify(null, bundle.getString("error"), bundle.getString("networkerror"), 4,
                                        "error");
                                return;
                            }
                            genResult.setRegion(region);
                            genResult.setToken(token);
                            genResult.setEntrepriseId(euid);
                            genResult.setRole(rl);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Screen scr = Screen.getPrimary();
                                    double height = scr.getVisualBounds().getHeight() * 1;
                                    double width = scr.getVisualBounds().getWidth() * 1;
                                    MainUI.loadMainView(this.getClass(), "mainuix.fxml", height, width, genResult);
                                    Kazisafex.stagex.close();
                                }
                            });
                        } else {
                            MainUI.notify(null, bundle.getString("error"),
                                    "Kazisafe n'a pas pu se connecter à l'internet,\n veuillez verifier l'etat de votre connection internet",
                                    4, "error");
                        }
                    }
                });
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
        kazisafe = KazisafeServiceFactory.createService(null);
        if (pswd_field.getText().isEmpty() || username_field.getText().isEmpty()
                || identification.getText().isEmpty()) {
            return;
        }
        Credentials cred = new Credentials(username_field.getText(), pswd_field.getText(), identification.getText());
        synchronousLogin(cred);
    }

    @FXML
    private void exit(MouseEvent event) {
        System.exit(0);
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        txt_version.setText("Version : " + Constants.APP_VERSION);
        int proc = Runtime.getRuntime().availableProcessors();
        System.err.println("How many processors = " + proc);
        // "Swahili", "Lingala", , "Hindi", "Kinyarwanda", "Arabe"
        langs.setItems(FXCollections.observableArrayList("Français", "English"));
        String langz = pref.get("lang", "fr");
        appName.setText("Kazisafe");

        // double x = 10.01;
        // double y = 20.30;
        // String o="-";
        // String xy=FMT."Description Width Area \n %-12s\{langz} %7f\{x} %7f\{y} \n
        // \{o.repeat(21)}";
        //
        // String result = FMT."0x%04x\{x} + 0x%04x\{y} = 0x%04x\{x + y}";
        // System.out.println("Result \n "+xy);
        switch (langz) {
            case "fr" -> {
                langs.getSelectionModel().select("Français");
                Util.setImageResourceOn(flag_lang, "fr.png");
            }
            case "sw" -> {
                langs.getSelectionModel().select("Swahili");
                Util.setImageResourceOn(flag_lang, "sw.png");
            }
            case "ln" -> {
                langs.getSelectionModel().select("Lingala");
                Util.setImageResourceOn(flag_lang, "ln.png");
            }
            case "en" -> {
                langs.getSelectionModel().select("English");
                Util.setImageResourceOn(flag_lang, "en.png");
            }
            case "hi" -> {
                langs.getSelectionModel().select("Hindi");
                Util.setImageResourceOn(flag_lang, "hi.png");
            }
            case "rw" -> {
                langs.getSelectionModel().select("Kinyarwanda");
                Util.setImageResourceOn(flag_lang, "rw.png");
            }
            case "ar" -> {
                langs.getSelectionModel().select("Arabe");
                Util.setImageResourceOn(flag_lang, "ar.png");
            }

        }
        langs.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    switch (newValue) {
                        case "Français" -> {
                            pref.put("lang", "fr");
                            Util.setImageResourceOn(flag_lang, "fr.png");
                        }
                        case "Swahili" -> {
                            pref.put("lang", "sw");
                            Util.setImageResourceOn(flag_lang, "sw.png");
                        }
                        case "Lingala" -> {
                            pref.put("lang", "ln");
                            Util.setImageResourceOn(flag_lang, "ln.png");
                        }
                        case "English" -> {
                            pref.put("lang", "en");
                            Util.setImageResourceOn(flag_lang, "en.png");
                        }
                        case "Hindi" -> {
                            pref.put("lang", "hi");
                            Util.setImageResourceOn(flag_lang, "hi.png");
                        }
                        case "Kinyarwanda" -> {
                            pref.put("lang", "rw");
                            Util.setImageResourceOn(flag_lang, "rw.png");
                        }
                        case "Arabe" -> {
                            pref.put("lang", "ar");
                            Util.setImageResourceOn(flag_lang, "ar.png");
                        }
                    }
                });
        pswd_field.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue != null) {
                        if (!showpswd_field.isFocused()) {
                            showpswd_field.setText(newValue);
                        }
                    }
                });
        showpswd_field.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue != null) {
                        if (!pswd_field.isFocused()) {
                            pswd_field.setText(newValue);
                        }
                    }
                });
        showpswd.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pswd_field.setVisible(false);
                    showpswd_field.setVisible(true);
                } else {
                    pswd_field.setVisible(true);
                    showpswd_field.setVisible(false);
                }
            }
        });
        tryAutoLoginWithSavedSession();
    }

    @FXML
    public void gotoEndeleya(Event evt) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://www.endeleya.com"));
            } catch (IOException e) {

            }
        }).start();

    }

    @FXML
    public void createNewAccount(Event e) {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    try {
                        Desktop.getDesktop().browse(URI.create("https://cloud.kazisafe.com/signup"));
                    } catch (IOException ex) {
                        Logger.getLogger(EntryPointController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                });

    }

    @FXML
    public void recoverPassword(Event e) {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
                    try {
                        // https://app.kazisafe.com/reset-password
                        Desktop.getDesktop().browse(new URI("https://cloud.kazisafe.com/recover-password"));
                    } catch (URISyntaxException | IOException ex) {
                        Logger.getLogger(EntryPointController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
    }
}
