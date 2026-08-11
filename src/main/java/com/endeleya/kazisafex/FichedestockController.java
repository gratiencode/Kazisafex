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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import data.Destocker;
import data.Entreprise;
import data.LigneVente;
import data.Mesure;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import delegates.DestockerDelegate;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
import services.utils.RegionRegistry;
import services.utils.UserRoleRegistry;
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
    private boolean usePosMovements;
    

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
        setDatabase(eze, kazisafe, p, null);
    }

    public void setDatabase(Entreprise eze, Kazisafe kazisafe, Produit p,
            String movementSource) {
        this.produit = p;
        this.usePosMovements = "POS".equalsIgnoreCase(movementSource);
        txt_produit_id.setText(p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + (p.getTaille() == null ? "" : p.getTaille()) + " " + (p.getCouleur() == null ? "" : p.getCouleur()));
      
        ficheItems = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        cbx_regions.setItems(regions); 
        RegionRegistry.loadAndSync(pref, kazisafe, regions);
        RegionRegistry.selectSavedRegion(pref, cbx_regions);
        table_fiche_stock.setItems(ficheItems);
        mzrs = FXCollections.observableArrayList(MesureDelegate.findMesureByProduit(produit.getUid()));
        cbx_choose_mesure.setItems(mzrs);
        if (!mzrs.isEmpty()) {
            cbx_choose_mesure.getSelectionModel().selectFirst();
        } else {
            refreshFiche();
        }
        cbx_regions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    refreshFiche();
                }
            }
        });
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
        role = UserRoleRegistry.getRole(pref);
        region = pref.get("regon", "...");
        configs();
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
            refreshFiche();
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
        refreshFiche();
    }

    @FXML
    private void refresh(Event e) {
        refreshFiche();
    }

    /** Loads every incoming and outgoing movement in chronological order. */
    private void refreshFiche() {
        if (produit == null || ficheItems == null) {
            return;
        }

        List<Mesure> mesures = MesureDelegate.findMesureByProduit(produit.getUid());
        List<Stocker> entrees;
        List<Destocker> sorties;
        String selectedRegion = cbx_regions.getValue();
        boolean allRegions = UserRoleRegistry.isTrader(pref) || UserRoleRegistry.hasAllAccess(pref);
        if (allRegions || selectedRegion == null || selectedRegion.isBlank()) {
            entrees = StockerDelegate.findStockerByProduit(produit.getUid());
            sorties = DestockerDelegate.findByProduit(produit.getUid());
        } else {
            entrees = StockerDelegate.findStockerByProduit(produit.getUid(), selectedRegion);
            sorties = DestockerDelegate.findByProduit(produit.getUid(), selectedRegion);
        }

        List<FicheItem> fiche;
        if (usePosMovements) {
            List<Recquisition> recquisitions = allRegions || selectedRegion == null || selectedRegion.isBlank()
                    ? RecquisitionDelegate.findRecquisitionByProduit(produit.getUid())
                    : RecquisitionDelegate.findRecquisitionByProduitRegion(produit.getUid(), selectedRegion);
            List<LigneVente> ventes = allRegions || selectedRegion == null || selectedRegion.isBlank()
                    ? LigneVenteDelegate.findByProduit(produit.getUid())
                    : LigneVenteDelegate.findByProduitRegion(produit.getUid(), selectedRegion);
            fiche = Util.findFicheDeStockPos(choosenM, mesures, recquisitions, ventes, produit);
        } else {
            if (dpk_debut_fiche.getValue() != null && dpk_fin_fiche.getValue() != null) {
                long debut = Constants.Datetime.dateInMillis(dpk_debut_fiche.getValue());
                long fin = Constants.Datetime.dateInMillis(dpk_fin_fiche.getValue());
                fiche = Util.findFicheDeStock(choosenM, mesures, entrees, sorties, produit, debut, fin);
            } else {
                fiche = Util.findFicheDeStock(choosenM, mesures, entrees, sorties, produit);
            }
        }
        if (dpk_debut_fiche.getValue() != null && dpk_fin_fiche.getValue() != null && usePosMovements) {
            long debut = Constants.Datetime.dateInMillis(dpk_debut_fiche.getValue());
            long fin = Constants.Datetime.dateInMillis(dpk_fin_fiche.getValue());
            fiche.removeIf(item -> item.getDate().getTime() < debut || item.getDate().getTime() > fin);
        }
        ficheItems.setAll(fiche);
        txt_count.setText(String.format(bundle.getString("xitems"), ficheItems.size()));
    }

    @FXML
    private void downloadPdf(MouseEvent event) {
        exportFiche(true);
    }

    @FXML
    private void downloadExcel(MouseEvent event) {
        exportFiche(false);
    }

    private void exportFiche(boolean pdf) {
        if (produit == null || ficheItems == null || ficheItems.isEmpty()) {
            return;
        }
        List<FicheItem> data = new ArrayList<>(ficheItems);
        Mesure mesure = choosenM;
        new Thread(() -> {
            File file = pdf
                    ? Util.exportPDFicheStock(data, mesure, produit)
                    : Util.exportXlsFicheStock(data, mesure, produit);
            if (file != null && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    Logger.getLogger(FichedestockController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "stock-card-export").start();
    }

    public void setSelectedItem(FicheItem selectedItem) {
        this.selectedItem = selectedItem;
    }

}
