/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import data.Destocker;
import data.Entreprise;
import data.Mesure;
import data.Produit;
import data.Stocker;
import tools.FicheItem;
import services.RepportService;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import tools.Constants;
import data.helpers.Role;
import data.network.Kazisafe;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class FichedestockController implements Initializable {

    private static FichedestockController instance;

    @FXML
    private Label txt_produit_id;
    @FXML
    private ComboBox<Mesure> cbx_choose_mesure;
    @FXML
    private ComboBox<String> cbx_regions;
    @FXML
    private TableView<FicheItem> table_fiche_stock;
    @FXML
    private TableColumn<FicheItem, String> col_date_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_libelle_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_quant_in_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_price_in_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_totprice_in_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_quant_out_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_coutinit_out_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_coutotal_out_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_restant_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_cump_restant_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_couttot_restant_fiche;
    @FXML
    private TableColumn<FicheItem, String> col_destination_fiche;
    @FXML
    private DatePicker dpk_debut_fiche;
    @FXML
    private DatePicker dpk_fin_fiche;
    @FXML
    private Label txt_count;

    Produit produit;
    Mesure choosenM;
    private FicheItem selectedItem;
    RepportService db;
    ObservableList<FicheItem> ficheItems;
    ObservableList<Mesure> mzrs;
    ObservableList<String> regions;
    Preferences pref;
    private String role;
    private String region;
    ResourceBundle bundle;
    

    public FichedestockController() {
        instance = this;
    }

    public static FichedestockController getInstance() {
        if(instance==null){
            instance=new FichedestockController();
        }
        return instance;
    }

    public void setDatabase(Entreprise eze,Kazisafe kazisafe, Produit p) {
        this.produit = p;
        txt_produit_id.setText(p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()) + " " + (p.getCouleur() == null ? "" : p.getCouleur()));
      
        ficheItems = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        cbx_regions.setItems(regions); 
        for (String key : regKeys()) {
            String r = pref.get(key, "...");
            if (!regions.contains(r)) {
                regions.add(r);
            }
        }
        List<FicheItem> fiche;
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
          
        } else {
           
                  
        }
//        ficheItems.setAll(fiche);
        table_fiche_stock.setItems(ficheItems);
        mzrs = FXCollections.observableArrayList();
        cbx_choose_mesure.setItems(mzrs);
        cbx_choose_mesure.getSelectionModel().selectFirst();
        txt_count.setText(String.format(bundle.getString("xitems"), ficheItems.size()));
        cbx_regions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                   
//                    ficheItems.setAll(fichel);
                    table_fiche_stock.setItems(ficheItems);
                }
            }
        });
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle=rb;
        MainUI.setPattern(dpk_fin_fiche);
        MainUI.setPattern(dpk_debut_fiche);
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        role = pref.get("priv", null);
        region = pref.get("regon", "...");
        configs();
        // TODO
    }

    private void configs() {
        cbx_choose_mesure.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_choose_mesure.getItems()
                        .stream()
                        .filter(f -> (f.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_choose_mesure.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            choosenM = newValue;
            if (choosenM != null) {
               
//                ficheItems.setAll(fiche);
            }
        });
        col_destination_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> new SimpleStringProperty(param.getValue().getDestination()));
        col_libelle_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> new SimpleStringProperty(param.getValue().getLibelles()));
        col_date_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            return new SimpleStringProperty(Constants.dateFormater.format(param.getValue().getDate()));

        });

        col_quant_in_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getQuantiteEntree();
            return new SimpleStringProperty(d == 0 ? "" : String.valueOf(d));

        });
        col_price_in_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getPrixUnitEntree();
            return new SimpleStringProperty(d == 0 ? "" : String.valueOf(d));

        });
        col_totprice_in_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getCoutTotalEntree();
            return new SimpleStringProperty(d == 0 ? "" : String.valueOf(d));

        });
        col_quant_out_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getQuantiteSortie();
            return new SimpleStringProperty(d == 0.0 ? "" : String.valueOf(d));

        });
        col_coutinit_out_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getCoutUnitaireSortie();
            return new SimpleStringProperty(d == 0 ? null : String.valueOf(d));

        });
        col_coutotal_out_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getCoutTotalSortie();
            return new SimpleStringProperty(d == 0 ? null : Double.toString(d));

        });
        col_restant_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getQuantiteRestant();
            return new SimpleStringProperty(d == 0 ? "" : String.valueOf(d));

        });
        col_cump_restant_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getCoutUnitRestant();
            return new SimpleStringProperty(d == 0 ? "" : String.valueOf(d));

        });
        col_couttot_restant_fiche.setCellValueFactory((TableColumn.CellDataFeatures<FicheItem, String> param) -> {
            double d = param.getValue().getCoutTotalRestant();
            return new SimpleStringProperty(d == 0 ? "" : String.valueOf(d));

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

    @FXML
    private void chooseByDate(ActionEvent evt) {
        if (dpk_debut_fiche.getValue() != null && dpk_fin_fiche.getValue() != null) {
            long debut = tools.Constants.Datetime.dateInMillis(dpk_debut_fiche.getValue());
            long fin = tools.Constants.Datetime.dateInMillis(dpk_fin_fiche.getValue());
            
           
//                    produit, debut, fin);
//            ficheItems.setAll(fiche);
            txt_count.setText(String.format(bundle.getString("xitems"), ficheItems.size() ));
        }
    }

    @FXML
    private void refresh(Event e) {
//        List<FicheItem> fiche = Util.findFicheDeStock(choosenM, db.findByProduit(Mesure.class, produit.getUid()),
//                db.findByProduit(Stocker.class, produit.getUid()),
//                db.findByProduit(Destocker.class, produit.getUid()), produit);
//        ficheItems.setAll(fiche);
    }

    @FXML
    private void downloadPdf(MouseEvent event) {
        new Thread(new Runnable() {
            private File fichedestock;

            @Override
            public void run() {
                if (dpk_debut_fiche.getValue() != null && dpk_debut_fiche.getValue() != null) {
                    long debut = tools.Constants.Datetime.dateInMillis(dpk_debut_fiche.getValue());
//                    long fin = tools.Constants.Datetime.dateInMillis(dpk_fin_fiche.getValue());
//                    List<FicheItem> fiche = Util.findFicheDeStock(choosenM, db.findByProduit(Mesure.class, produit.getUid()),
//                            db.findByProduit(Stocker.class, produit.getUid()),
//                            db.findByProduit(Destocker.class, produit.getUid()), produit, debut, fin);
//                    fichedestock = Util.exportPDFicheStock(fiche, choosenM, produit);
                } else {
//                    List<FicheItem> fiche = Util.findFicheDeStock(choosenM, db.findByProduit(Mesure.class, produit.getUid()),
//                           db.findByProduit(Stocker.class, produit.getUid()),
//                           db.findByProduit(Destocker.class, produit.getUid()), produit);
//                    fichedestock = Util.exportPDFicheStock(fiche, choosenM, produit);
                }
                try {

                    Desktop.getDesktop().open(fichedestock);
                } catch (IOException ex) {
                    Logger.getLogger(FichedestockController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public void setSelectedItem(FicheItem selectedItem) {
        this.selectedItem = selectedItem;
    }

}
