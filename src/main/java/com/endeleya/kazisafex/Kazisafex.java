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


/**
 * JavaFX Kazisafex
 */
public class Kazisafex extends Application {
    public static Stage stagex;
    public static Parent rootx;
    private double xOffset = 0;
    private double yOffset = 0;
    private static Kazisafex instance;
    private Preferences pref;

    public Kazisafex() {
        instance=this;
        pref=Preferences.userNodeForPackage(SyncEngine.class);
    }
    
    public static Kazisafex getInstance(){
        return instance;
    }
    private ResourceBundle langageBundle;
    
    
    
    @Override
    public void start(Stage stage) throws Exception {
        String lang = pref.get("lang", "fr");
        Locale locale = new Locale.Builder().setLanguage(lang).build();
        langageBundle= ResourceBundle.getBundle("bundles." +lang, locale);
                
        Parent root = FXMLLoader.load(getClass().getResource("/guis/FXMLDocument.fxml"),langageBundle);
        Scene scene = new Scene(root);
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
    
    
}