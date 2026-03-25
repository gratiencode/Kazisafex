package com.endeleya.kazisafex;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tools.MainUI;
import tools.SyncEngine;
import tools.ThemeStyler;


/**
 * JavaFX Kazisafex
 */
public class Kazisafex extends Application {
    public static final String DARK_THEME_PREF = "dark_theme_enabled";
    public static Stage stagex;
    public static Parent rootx;
    private double xOffset = 0;
    private double yOffset = 0;
    private static Kazisafex instance;
    private Preferences pref;

    public Kazisafex() {
        instance=this;
        pref=Preferences.userNodeForPackage(SyncEngine.class);
        System.out.println("Time now: "+System.currentTimeMillis());
    }
    
    public static Kazisafex getInstance(){
        return instance;
    }
    private ResourceBundle langageBundle;
    
    
    
    @Override
    public void start(Stage stage) throws Exception {
//          System.setProperty("javax.net.debug", "ssl,handshake");
//
//            // Spécifier les versions de protocoles SSL/TLS
//            System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        String lang = pref.get("lang", "fr");
        Locale locale = new Locale.Builder().setLanguage(lang).build();
        langageBundle= ResourceBundle.getBundle("bundles." +lang, locale);
                
        Parent root = FXMLLoader.load(getClass().getResource("/guis/FXMLDocument.fxml"),langageBundle);
        Scene scene = new Scene(root);
        applyTheme(scene);
        stage.setScene(scene);
        stagex = stage;
        rootx = root;
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);
        stage.getIcons().add(new Image(Kazisafex.class.getResourceAsStream("/icons/icone_ksf.png")));

        root.setOnMousePressed((javafx.scene.input.MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged((javafx.scene.input.MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        MainUI.rootPath();
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public ResourceBundle getLangageBundle() {
        return langageBundle;
    }
    
    
    public ResourceBundle getLanguageBundle(String lang){
        String langn = pref.get("lang", lang);
        Locale locale = new Locale.Builder().setLanguage(langn).build();
        langageBundle= ResourceBundle.getBundle("bundles." + langn, locale);
        return langageBundle;
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) {
            return;
        }
        Preferences preferences = Preferences.userNodeForPackage(SyncEngine.class);
        String darkCss = Kazisafex.class.getResource("/styles/dark-theme.css").toExternalForm();
        String lightCss = Kazisafex.class.getResource("/styles/light-theme.css").toExternalForm();
        scene.getStylesheets().remove(darkCss);
        scene.getStylesheets().remove(lightCss);
        scene.getStylesheets().add(lightCss);
        boolean darkEnabled = preferences.getBoolean(DARK_THEME_PREF, false);
        if (darkEnabled) {
            scene.getStylesheets().add(darkCss);
        }
        ThemeStyler.apply(scene.getRoot(), darkEnabled);
    }
    
    
}
