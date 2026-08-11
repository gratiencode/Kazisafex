/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;
import javax.print.PrintService;

import data.Entreprise;
import data.LigneVente;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.StockAgregate;
import data.Stocker;
import org.apache.commons.lang3.StringUtils;
import tools.CurrencyConverter;
import tools.Constants;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import data.helpers.Role;
import data.network.Kazisafe;
import services.utils.UserRoleRegistry;
import delegates.TraisorerieDelegate;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class PanierappenderController implements Initializable {

    @FXML
    private ImageView img_vu_pixa_prod;
    @FXML
    private Label txt_codebar;
    @FXML
    private Label localisabel;
    @FXML
    private Label txt_product_name;
    @FXML
    private ComboBox<Mesure> cbx_mesure_selected;
    @FXML
    private TextField tf_input_quant;
    @FXML
    private TextField tf_prix_unitr_usd;
    @FXML
    private TextField tf_prix_unitr_cdf;
    @FXML
    private Label txt_total_usd;
    @FXML
    private Label txt_total_cdf, txt_available_quant;
    @FXML
    private Button btn_add2cart;
    @FXML
    private ComboBox<StockAgregate> cbx_lot_recquisitionee;
    @FXML
    private Label txt_peremption;
    @FXML
    private Label txt_alerte_pa;
    @FXML
    ImageView img_codebar;

    //JpaStorage db;
    List<PrixDeVente> prices;
    List<Mesure> mesures;

    ObservableList<StockAgregate> lisrecquisition;

    Produit prod;

    Mesure choosenmez;
    Recquisition choosenRecquisition;
    StockAgregate choosenLotStock;
    Preferences pref;
    double taux2change;
    double quant = 1, reste;
    String region, role, meth, action;
    ResourceBundle bundle;
    long id;
    double resteEnPiece;
    private final Map<String, Double> lotRemainingPiecesCache = new HashMap<>();
    private final Map<String, Recquisition> lotMetadataCache = new HashMap<>();
    @FXML
    private RadioButton thermal, jet;
    @FXML
    private Pane pane_print;
    Printer defaultPrinter;
    ToggleGroup printerGroup;
    String dev;
    @FXML
    private Label txt_product_name11;
    @FXML
    private Label txt_alerte_pa1;
    @FXML
    private Label txt_qte_lot;
    @FXML
    private Label lbl_usd_part;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux2change = CurrencyConverter.activeRate();
        region = pref.get("region", "...");
        role = UserRoleRegistry.getRole(pref);
        meth = pref.get("meth", "fifo");
        printerGroup = new ToggleGroup();
        thermal.setToggleGroup(printerGroup);
        jet.setToggleGroup(printerGroup);
        pane_print.setVisible(false);
        config();
        cbx_lot_recquisitionee.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            applySelectedLot(newValue);
        });
        cbx_mesure_selected.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            choosenmez = newValue;
            if (choosenRecquisition == null) {
                return;
            }
            prices = getRecentPrice(prod.getUid()).getValue();//PrixDeVenteDelegate.findPricesForRecq(choosenRecquisition.getUid());
            refreshAvailableQuantitiesLabels();
            if (!tf_input_quant.getText().isEmpty() && StringUtils.isNumeric(tf_input_quant.getText())) {
                Double d = Double.valueOf(tf_input_quant.getText());
                List<PrixDeVente> pvds = PrixDeVenteDelegate.findSpecificByQuant(choosenRecquisition, choosenmez, d);
                if (pvds.isEmpty()) {
                    applyPrices(prices);
                    return;
                }
                PrixDeVente pvd = pvds.get(0);
                dev = pvd.getDevise();
                if (dev.equals("CDF")) {
                    tf_prix_unitr_cdf.setText(String.valueOf(pvd.getPrixUnitaire()));
                    tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pvd.getPrixUnitaire()).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                    double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
                    double cdf = Double.parseDouble(tf_prix_unitr_cdf.getText());
                    double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
//                    double tcfd = BigDecimal.valueOf(d * cdf).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    txt_total_cdf.setText(String.valueOf(Math.round(d * cdf)));
                    txt_total_usd.setText(String.valueOf(tusd));
                    // Les champs USD restent toujours visibles quelle que soit la devise.
                } else {
                    tf_prix_unitr_usd.setText(String.valueOf(pvd.getPrixUnitaire()));
                    double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
                    double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    double tcfd = BigDecimal.valueOf(tusd * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    txt_total_cdf.setText(String.valueOf(tcfd));
                    txt_total_usd.setText(String.valueOf(tusd));
                }

            }
            applyPrices(prices);
        });
        tf_input_quant.requestFocus();
        tf_input_quant.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                System.out.println("Execution carret on quant");
                resolve(newValue);
            } catch (NumberFormatException e) {
                System.err.println("NFE error " + e.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(PanierappenderController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(PanierappenderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @FXML
    private void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    @FXML
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        n.getParent().setVisible(false);
    }

    @FXML
    private void showFloatingPane(Event evt) {
        pane_print.setVisible(true);
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
    ComboBox<Printer> cbx_printers;
//appel initial
    public void setProduit(Entreprise eze, Kazisafe ksf, Produit produit, String action, long id) {
        taux2change = CurrencyConverter.activeRate();
        if (produit == null) {
            return;
        }
        this.prod = produit;
        this.action = action;
        this.id = id;
        lisrecquisition = FXCollections.observableArrayList();
        // this.db = JpaStorage.getInstance();
        Util.installPicture(img_vu_pixa_prod, prod.getUid() + ".jpeg");
        txt_codebar.setText(prod.getCodebar());
        System.out.println("selected pro " + prod.getNomProduit() + " From " + eze);
        mesures = MesureDelegate.findAscSortedByQuantWithProduit(prod.getUid());
        ObservableList<Mesure> lmez = FXCollections.observableArrayList(mesures);
        cbx_mesure_selected.setItems(lmez);
        cbx_lot_recquisitionee.setItems(lisrecquisition);
        cbx_mesure_selected.getSelectionModel().selectFirst();
        choosenmez = cbx_mesure_selected.getValue();
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();
        cbx_printers.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) -> {
            defaultPrinter = newValue;
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        txt_product_name.setText(prod.getNomProduit() + " " + (prod.getMarque() == null ? "" : prod.getMarque()) + " " + (prod.getModele() == null ? "" : prod.getModele()) + ""
                + " " + (prod.getTaille() == null ? "" : prod.getTaille()) + " " + (prod.getCouleur() == null ? "" : prod.getCouleur()));
        refreshRecquisitionLots();
        System.out.println("Recqusis met size " + lisrecquisition.size());
        StockAgregate preferred = pickPreferredLotForSelection();
        if (preferred != null) {
            cbx_lot_recquisitionee.getSelectionModel().select(preferred);
        } else {
            cbx_lot_recquisitionee.getSelectionModel().clearSelection();
        }
        choosenLotStock = cbx_lot_recquisitionee.getValue();
        choosenRecquisition = resolveRecquisitionMetadata(choosenLotStock);

        Map.Entry<Recquisition, List<PrixDeVente>> recent = getRecentPrice(prod.getUid());
        if (choosenLotStock == null) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("choosemeth"), 4, "error");
            StockAgregate fallbackLot = buildFallbackLotStock(recent.getKey());
            if (fallbackLot != null) {
                boolean alreadyListed = lisrecquisition.stream()
                        .anyMatch(r -> Objects.equals(r.getNumlot(), fallbackLot.getNumlot()));
                if (!alreadyListed) {
                    lisrecquisition.add(fallbackLot);
                }
                String fallbackLotNum = fallbackLot.getNumlot();
                if (fallbackLotNum != null && !fallbackLotNum.isBlank() && !lotRemainingPiecesCache.containsKey(fallbackLotNum)) {
                    lotRemainingPiecesCache.put(fallbackLotNum, getCurrentRemainingPiecesForLot(fallbackLotNum));
                }
                cbx_lot_recquisitionee.getSelectionModel().select(fallbackLot);
                choosenLotStock = fallbackLot;
                choosenRecquisition = resolveRecquisitionMetadata(fallbackLot);
            }
        }
        if (choosenLotStock == null || choosenRecquisition == null) {
            MainUI.notify(null, bundle.getString("error"), "Aucun lot disponible pour ce produit.", 4, "error");
            return;
        }
        applySelectedLot(choosenLotStock);
        List<Stocker> locals = StockerDelegate.findStockerByProduitLot(prod.getUid(), choosenLotStock.getNumlot());
        Stocker local = locals.isEmpty() ? null : locals.get(0);
        localisabel.setText(local == null ? "" : local.getLocalisation());
        System.out.println("Recs size " + lisrecquisition.size());
//        Recquisition headerRecq = PosController.getInstance().getHeaderRecq(prod);
//        String numlot = choosenRecquisition == null ? headerRecq.getNumlot() : choosenRecquisition.getNumlot();

//        prices = PrixDeVenteDelegate.findPricesForRecq(choosenRecquisition.getUid());
        if (prices == null) {
            prices = recent.getValue() == null ? new ArrayList<>() : new ArrayList<>(recent.getValue());
        }
        updateAlertLabel();
        if (prices.isEmpty()) {
            for (Recquisition rx : RecquisitionDelegate.toLifoOrdering(prod.getUid())) {
                List<PrixDeVente> pricez = PrixDeVenteDelegate.findPricesForRecq(rx.getUid());
                if (!pricez.isEmpty()) {
                    for (PrixDeVente prixDeVente : pricez) {
                        if (prixDeVente.getPrixUnitaire() > 0) {
                            prices.add(prixDeVente);
                        }
                    }
                    if (!prices.isEmpty()) {
                        break;
                    }
                }
            }
        }
        refreshAvailableQuantitiesLabels();
        List<PrixDeVente> pvxs = pickPrice(prices, choosenmez, 1);
        if (!pvxs.isEmpty()) {
            PrixDeVente pvx = pvxs.get(0);
            String dev = pvx.getDevise();
//                   
            if (dev.equals("CDF")) {
                tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire()));
                tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pvx.getPrixUnitaire() / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                double pv = Double.parseDouble(tf_prix_unitr_cdf.getText());
                double tcdf = BigDecimal.valueOf(1 * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                double tusd = BigDecimal.valueOf(tcdf / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                txt_total_cdf.setText(String.valueOf(tcdf));
                txt_total_usd.setText(String.valueOf(tusd));
                // Les champs USD restent toujours visibles quelle que soit la devise.
            } else {
                tf_prix_unitr_usd.setText(String.valueOf(pvx.getPrixUnitaire()));
                tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire() * taux2change));
                double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
                double tusd = BigDecimal.valueOf(1 * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                double tcfd = BigDecimal.valueOf(tusd * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                txt_total_cdf.setText(String.valueOf(tcfd));
                txt_total_usd.setText(String.valueOf(tusd));
            }

        } else {
            applyPrices(prices);
        }
        String text = "#" + prod.getUid() + "-" + choosenmez.getQuantContenu() + "-" + tf_input_quant.getText() + "-"
                + "" + choosenLotStock.getNumlot() + "-" + dev;
        try {
            writeCartItem(text.trim());
        } catch (IOException ex) {
            Logger.getLogger(PanierappenderController.class.getName()).log(Level.SEVERE, null, ex);
        }

        tf_prix_unitr_usd.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    double d = Double.parseDouble(tf_input_quant.getText());
                    double pv = Double.parseDouble(newValue);
                    if (tf_prix_unitr_usd.isFocused()) {
                        tf_prix_unitr_cdf.setText(String.valueOf(BigDecimal.valueOf(taux2change * pv).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                    }
                    double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    double tcfd = BigDecimal.valueOf(tusd * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    txt_total_cdf.setText(String.valueOf(tcfd));

                    txt_total_usd.setText(String.valueOf(tusd));

                } catch (NumberFormatException e) {

                }
            }
        });
        tf_prix_unitr_cdf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    double d = Double.parseDouble(tf_input_quant.getText());
                    double pv = Double.parseDouble(newValue);
                    String dev = pref.get("mainCur", "USD");
                    pv = "USD".equals(dev) ? (pv / taux2change) : pv;
                    if (tf_prix_unitr_cdf.isFocused()) {
                        tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue()));
                    }
                    double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    double tcfd = "USD".equals(dev) ? BigDecimal.valueOf(tusd * taux2change)
                            .setScale(3, RoundingMode.HALF_EVEN).doubleValue() : tusd;

                    txt_total_cdf.setText(String.valueOf(tcfd));
                    txt_total_usd.setText(String.valueOf(tusd));
                } catch (NumberFormatException e) {

                }
            }
        });

        updatePeremptionLabel(choosenLotStock);
        tf_input_quant.requestFocus();
    }
    
    private void applySelectedLot(StockAgregate lotStock) {
        choosenLotStock = lotStock;
        choosenRecquisition = resolveRecquisitionMetadata(lotStock);
        updatePeremptionLabel(lotStock);
        updateAlertLabel();
        refreshAvailableQuantitiesLabels();
        updateLotLocationLabel();
    }

    private void updatePeremptionLabel(StockAgregate lotStock) {
        LocalDate date = lotStock == null ? null : lotStock.getDateExpiration();
        txt_peremption.setText(date == null ? bundle.getString("noperish") : "  Exp : " + date);
        applyBgColorIfExpired(date);
    }

    private void updateAlertLabel() {
        if (txt_alerte_pa == null || choosenmez == null) {
            return;
        }
        if (choosenRecquisition == null) {
            txt_alerte_pa.setText("Alt:0 " + choosenmez.getDescription());
            return;
        }
        Mesure mzr = choosenRecquisition.getMesureId();
        Mesure mz = mzr == null ? null : MesureDelegate.findMesure(mzr.getUid());
        if (mz == null && choosenLotStock != null && choosenLotStock.getMesureId() != null) {
            mz = choosenLotStock.getMesureId();
        }
        if (mz == null && prod != null) {
            List<Mesure> mesuresProduit = MesureDelegate.findAscSortedByQuantWithProduit(prod.getUid());
            mz = mesuresProduit.isEmpty() ? null : mesuresProduit.get(0);
        }
        double stockAlert = choosenRecquisition.getStockAlert() == null ? 0 : choosenRecquisition.getStockAlert();
        double quantContenu = mz == null || mz.getQuantContenu() == null || mz.getQuantContenu() == 0 ? 1 : mz.getQuantContenu();
        double alerte = (stockAlert * quantContenu) / choosenmez.getQuantContenu();
        txt_alerte_pa.setText("Alt:" + BigDecimal.valueOf(alerte).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " " + choosenmez.getDescription());
    }

    private void updateLotLocationLabel() {
        if (localisabel == null) {
            return;
        }
        if (prod == null || choosenLotStock == null || choosenLotStock.getNumlot() == null) {
            localisabel.setText("");
            return;
        }
        List<Stocker> lots = StockerDelegate.findStockerByProduitLot(prod.getUid(), choosenLotStock.getNumlot());
        if (lots.isEmpty()) {
            localisabel.setText("");
            return;
        }
        localisabel.setText(lots.get(0).getLocalisation());
    }

    private Recquisition resolveRecquisitionMetadata(StockAgregate lotStock) {
        if (lotStock == null) {
            return null;
        }
        String numlot = lotStock.getNumlot();
        if (numlot == null || numlot.isBlank()) {
            return synthesizeRecquisitionFromLotStock(lotStock);
        }
        if (lotMetadataCache.containsKey(numlot)) {
            return lotMetadataCache.get(numlot);
        }
        List<Recquisition> recquisitions = hasGlobalStockAccess()
                ? RecquisitionDelegate.findRecquisitionByProduit(prod.getUid(), numlot)
                : RecquisitionDelegate.findRecquisitionByProduit(prod.getUid(), numlot, region);
        Recquisition metadata = recquisitions == null ? null : recquisitions.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparing(Recquisition::getDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        if (metadata == null) {
            metadata = synthesizeRecquisitionFromLotStock(lotStock);
        } else {
            enrichRecquisitionFromLotStock(metadata, lotStock);
        }
        lotMetadataCache.put(numlot, metadata);
        return metadata;
    }

    private Recquisition synthesizeRecquisitionFromLotStock(StockAgregate lotStock) {
        if (lotStock == null) {
            return null;
        }
        Recquisition recquisition = new Recquisition(lotStock.getUid());
        enrichRecquisitionFromLotStock(recquisition, lotStock);
        if (recquisition.getStockAlert() == null) {
            recquisition.setStockAlert(0d);
        }
        return recquisition;
    }

    private void enrichRecquisitionFromLotStock(Recquisition recquisition, StockAgregate lotStock) {
        if (recquisition == null || lotStock == null) {
            return;
        }
        recquisition.setNumlot(lotStock.getNumlot());
        recquisition.setDate(lotStock.getDate().atStartOfDay());
        if (recquisition.getDateExpiry() == null) {
            recquisition.setDateExpiry(lotStock.getDateExpiration());
        }
        if (recquisition.getMesureId() == null) {
            recquisition.setMesureId(lotStock.getMesureId());
        }
        if (recquisition.getProductId() == null) {
            recquisition.setProductId(lotStock.getProductId());
        }
        if (recquisition.getCoutAchat() <= 0 && lotStock.getCoutAchat() != null) {
            recquisition.setCoutAchat(lotStock.getCoutAchat());
        }
    }

    private StockAgregate buildFallbackLotStock(Recquisition recquisition) {
        if (recquisition == null || recquisition.getNumlot() == null || recquisition.getNumlot().isBlank()) {
            return null;
        }
        double lotRemainingPieces = getCurrentRemainingPiecesForLot(recquisition.getNumlot());
        if (lotRemainingPieces <= 0) {
            return null;
        }
        StockAgregate fallback = new StockAgregate(recquisition.getUid());
        fallback.setNumlot(recquisition.getNumlot());
        fallback.setDate(recquisition.getDate().toLocalDate());
        fallback.setDateExpiration(recquisition.getDateExpiry());
        fallback.setMesureId(recquisition.getMesureId());
        fallback.setProductId(recquisition.getProductId());
        fallback.setCoutAchat(recquisition.getCoutAchat());
        fallback.setFinalQuantity(lotRemainingPieces);
        return fallback;
    }

    private void applyBgColorIfExpired(LocalDate dateExpiration) {
        int exp = isStockExpired(dateExpiration);
        switch (exp) {
            case -1 -> // Expired
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f58282"), new CornerRadii(20), new Insets(4))));
            case 1 -> // 1 Month
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c46506"), new CornerRadii(20), new Insets(4))));
            case 3 -> // 3 Months
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#db8109"), new CornerRadii(20), new Insets(4))));
            case 6 -> // 6 Months
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f7fa61"), new CornerRadii(20), new Insets(4))));
            case 12 -> // 12 Months
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c5e6b3"), new CornerRadii(20), new Insets(4))));
            default -> // Non-perishable or long term
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
        }
        txt_peremption.setStyle("-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 6;");
    }

    private PrixDeVente findPriceWithConversion(List<PrixDeVente> pvxs,
            Mesure mesureChoisie,
            double quantiteChoisie) {

        PrixDeVente exact = pvxs.stream()
                .filter(p -> sameMesure(p, mesureChoisie))
                .filter(p -> inInterval(p, quantiteChoisie))
                .findFirst()
                .orElse(null);

        if (exact != null) {
            return exact;
        }

        return pvxs.stream()
                .filter(p -> p.getMesureId() != null)
                .map(p -> convertIfPossible(p, mesureChoisie, quantiteChoisie))
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(PrixDeVente::getQmin)) // qmin le plus bas
                .orElse(null);
    }

    private boolean sameMesure(PrixDeVente p, Mesure m) {
        return p.getMesureId().getUid().equals(m.getUid());
    }

    private boolean inInterval(PrixDeVente p, double q) {
        return p.getQmin() <= q && q <= p.getQmax();
    }

    private PrixDeVente convertIfPossible(PrixDeVente prix,
            Mesure mesureChoisie,
            double quantiteChoisie) {

        Mesure mesurePrix = prix.getMesureId();
        // facteur de conversion
        double facteur = mesureChoisie.getQuantContenu() / mesurePrix.getQuantContenu();

        // quantité équivalente dans la mesure du prix
        double quantiteConvertie = quantiteChoisie * facteur;

        // respect des bornes
        if (!inInterval(prix, quantiteConvertie)) {
            return null;
        }

        double prixUnitaire = prix.getPrixUnitaire()/ mesurePrix.getQuantContenu();
        double prixFinal = prixUnitaire * mesureChoisie.getQuantContenu();

        PrixDeVente approx = new PrixDeVente(prix.getUid());
        approx.setPrixUnitaire(prixFinal);
        approx.setQmin(prix.getQmin() / facteur);
        approx.setQmax(prix.getQmax() / facteur);
        approx.setDevise(prix.getDevise());
        approx.setRecquisitionId(prix.getRecquisitionId());
        approx.setMesureId(mesureChoisie);
        return approx;
    }

    private void resolve(String newValue) throws Exception {
        if (newValue == null || newValue.isBlank() || prod == null || choosenmez == null || choosenRecquisition == null) {
            return;
        }
        double d = Double.parseDouble(newValue);
        if (d <= 0) {
            return;
        }
        List<PrixDeVente> pvxs
                = //PrixDeVenteDelegate.findSpecificByQuant(choosenRecquisition, choosenmez, d);
                getRecentPrice(prod.getUid()).getValue();
        PrixDeVente pvx = findPriceWithConversion(pvxs, choosenmez, d);
        if (pvx == null) {
            return;
        }
        System.out.println("PRIXO");
        String divize = pvx.getDevise();
        if (divize.equals("CDF")) {
            tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pvx.getPrixUnitaire() / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
            tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire()));
            double fc = Double.parseDouble(tf_prix_unitr_cdf.getText());
            double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
            double tusd = BigDecimal.valueOf(d * pv).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            double tcfd = BigDecimal.valueOf(d * fc).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
            txt_total_cdf.setText(String.valueOf(tcfd));
            txt_total_usd.setText(String.valueOf(tusd));
            // Les champs USD restent toujours visibles quelle que soit la devise.
        } else {
            tf_prix_unitr_usd.setText(String.valueOf(pvx.getPrixUnitaire()));
            tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire() * taux2change));
            double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
            double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
            double tcfd = BigDecimal.valueOf(tusd * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
            txt_total_cdf.setText(String.valueOf(tcfd));
            txt_total_usd.setText(String.valueOf(tusd));
        }
        String text = "#" + prod.getUid() + "-" + choosenmez.getQuantContenu() + "-" + tf_input_quant.getText() + "-"
                + "" + choosenRecquisition.getNumlot() + "-" + divize;
        writeCartItem(text);
    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        osp.forEach((p) -> {
            rst.add(p);
        });
        return rst;
    }

    private void refreshRecquisitionLots() {
        lisrecquisition.clear();
        lotRemainingPiecesCache.clear();
        lotMetadataCache.clear();
        if (prod == null) {
            return;
        }
        List<StockAgregate> groupedLots = hasGlobalStockAccess()
                ? RecquisitionDelegate.findLatestLotStockAgregates(prod.getUid())
                : RecquisitionDelegate.findLatestLotStockAgregates(prod.getUid(), region);
        if (groupedLots == null || groupedLots.isEmpty()) {
            return;
        }
        Map<String, StockAgregate> uniqueLots = new HashMap<>();
        for (StockAgregate recquisition : groupedLots) {
            if (recquisition == null || recquisition.getProductId() == null) {
                continue;
            }
            if (!prod.getUid().equals(recquisition.getProductId().getUid())) {
                continue;
            }
            String lot = recquisition.getNumlot();
            if (lot == null || lot.isBlank()) {
                lot = recquisition.getDate() == null ? null : recquisition.getDate().toString();
                recquisition.setNumlot(lot);
            }
            if (lot == null || lot.isBlank()) {
                continue;
            }
            double lotRemainingPieces = recquisition.getFinalQuantity() == null
                    ? getCurrentRemainingPiecesForLot(lot)
                    : Math.max(0, recquisition.getFinalQuantity());
            if (lotRemainingPieces <= 0) {
                continue;
            }
            recquisition.setFinalQuantity(lotRemainingPieces);
            lotRemainingPiecesCache.put(lot, lotRemainingPieces);
            StockAgregate existing = uniqueLots.get(lot);
            if (existing == null || isMoreRecent(recquisition, existing)) {
                uniqueLots.put(lot, recquisition);
            }
        }
        List<StockAgregate> lots = new ArrayList<>(uniqueLots.values());
        sortLotsByInventoryMethod(lots);
        lisrecquisition.setAll(lots);
    }

    private StockAgregate pickPreferredLotForSelection() {
        if (prod == null || lisrecquisition.isEmpty()) {
            return null;
        }
        Recquisition preferred = hasGlobalStockAccess()
                ? RecquisitionDelegate.getHeaderRecq(meth, prod)
                : RecquisitionDelegate.getHeaderRecq(meth, prod, region);
        if (preferred == null || preferred.getNumlot() == null) {
            return lisrecquisition.get(0);
        }
        for (StockAgregate lot : lisrecquisition) {
            if (Objects.equals(preferred.getNumlot(), lot.getNumlot())) {
                return lot;
            }
        }
        return lisrecquisition.get(0);
    }

    private void sortLotsByInventoryMethod(List<StockAgregate> lots) {
        if (lots == null || lots.size() < 2) {
            return;
        }
        Comparator<StockAgregate> comparator;
        switch (meth) {
            case "ppps":
                comparator = Comparator.comparing(StockAgregate::getDateExpiration, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(StockAgregate::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "lifo":
                comparator = Comparator.comparing(StockAgregate::getDate, Comparator.nullsLast(Comparator.reverseOrder()));
                break;
            case "fifo":
            default:
                comparator = Comparator.comparing(StockAgregate::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }
        lots.sort(comparator);
    }

    private boolean isMoreRecent(StockAgregate candidate, StockAgregate current) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }
        if (candidate.getDate() == null) {
            return false;
        }
        if (current.getDate() == null) {
            return true;
        }
        return candidate.getDate().isAfter(current.getDate());
    }

    private void refreshAvailableQuantitiesLabels() {
        if (choosenmez == null || prod == null) {
            return;
        }
        resteEnPiece = getCurrentRemainingPieces();
        reste = toMesureQuantity(resteEnPiece, choosenmez);
        txt_available_quant.setText(reste + " " + choosenmez.getDescription());
        if (txt_qte_lot == null) {
            return;
        }
        if (choosenLotStock == null || choosenLotStock.getNumlot() == null) {
            txt_qte_lot.setText(0 + " " + choosenmez.getDescription());
            return;
        }
        String selectedLot = choosenLotStock.getNumlot();
        double lotPieces = choosenLotStock.getFinalQuantity() == null
                ? (lotRemainingPiecesCache.containsKey(selectedLot)
                        ? lotRemainingPiecesCache.get(selectedLot)
                        : getCurrentRemainingPiecesForLot(selectedLot))
                : Math.max(0, choosenLotStock.getFinalQuantity());
        lotRemainingPiecesCache.put(selectedLot, lotPieces);
        txt_qte_lot.setText(toMesureQuantity(lotPieces, choosenmez) + " " + choosenmez.getDescription());
    }

    private double toMesureQuantity(double pieces, Mesure mesure) {
        if (mesure == null || mesure.getQuantContenu() == null || mesure.getQuantContenu() <= 0) {
            return 0;
        }
        return BigDecimal.valueOf(pieces / mesure.getQuantContenu())
                .setScale(3, RoundingMode.HALF_EVEN)
                .doubleValue();
    }

    private double getCurrentRemainingPiecesForLot(String numlot) {
        if (prod == null || numlot == null || numlot.isBlank()) {
            return 0;
        }
        // Quantite par lot provenant directement de stock_agregate
        double pieces = hasGlobalStockAccess()
                ? RecquisitionDelegate.sumLatestLotFinalQuantityFromStockAggregate(prod.getUid(), numlot, "%")
                : RecquisitionDelegate.sumLatestLotFinalQuantityFromStockAggregate(prod.getUid(), numlot, region);
        return Math.max(0, pieces);
    }

//    private void printBCWithThermal(String printerName, String bcode) {
//        PrinterOutputStream pos = null;
//        try {
//            if (bcode.isEmpty()) {
//                return;
//            }
//            PrintService ps = PrinterOutputStream.getPrintServiceByName(printerName);
//            pos = new PrinterOutputStream(ps);
//            EscPos printer = new EscPos(pos);
//            BarCode bc = new BarCode();
//            bc.setBarCodeSize(12, 6).setJustification(EscPosConst.Justification.Center)
//                    .getBytes(bcode);
//            printer.feed(2);
//            printer.cut(EscPos.CutMode.FULL);
//            printer.close();
//        } catch (IOException ex) {
//            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            if (pos != null) {
//                try {
//                    pos.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//
//    }

    @FXML
    public void printbarcode(Event e) {
        if (prod == null || choosenmez == null || choosenRecquisition == null) {
            MainUI.notify(null, bundle.getString("error"), "Sélection incomplète pour impression du code-barres.", 4, "error");
            return;
        }
        if (thermal.isSelected()) {
            if (defaultPrinter == null) {
                MainUI.notify(null, bundle.getString("error"), "Aucune imprimante sélectionnée.", 4, "error");
                return;
            }
//            String text = "ENCODE:" + prod.getUid() + "-" + choosenmez.getUid() + "-" + tf_input_quant.getText() + "-"
//                    + "" + choosenRecquisition.getNumlot() + "-" + dev;
//            printBCWithThermal(defaultPrinter.getName(), text);
        } else if (jet.isSelected()) {
            print();
        }
        pane_print.setVisible(false);
    }

    private void print() {
        PrinterJob pj = PrinterJob.createPrinterJob(defaultPrinter);
        if (pj == null) {
            return;
        }

        Window s = pane_print.getScene().getWindow();
        pj.showPageSetupDialog(s);
        boolean proceed = pj.showPrintDialog((Stage) s);
        if (proceed) {
            printCodebar(pj, img_codebar);
        }

    }

    private void printCodebar(PrinterJob pj, Node node) {
        boolean pr = pj.printPage(node);
        if (pr) {
            pj.endJob();
        }

    }

    private int isStockExpired(LocalDate dateExpiration) {
        long now = System.currentTimeMillis();
        if (dateExpiration == null) {
            return 555;
        }
        long exp = Constants.Datetime.dateInMillis(dateExpiration);
        long un_mois = Constants.UN_MOIS;
        long interval = exp - now;
        long mois3 = (un_mois * 3);
        long mois6 = (un_mois * 6);
        long mois12 = (un_mois * 12);
        if (interval <= 0) {
            System.out.println("0");
            return -1;
        } else if (interval <= un_mois) {
            System.out.println("1");
            return 1;
        } else if (interval <= mois3) {
            System.out.println("3");
            return 3;
        } else if (interval <= mois6) {
            System.out.println("6");
            return 6;
        } else if (interval <= mois12) {
            System.out.println("12");
            return 12;
        } else {
            return 555;
        }
    }

    private List<PrixDeVente> pickPrice(List<PrixDeVente> prices, Mesure m, double quant) {
        List<PrixDeVente> rst = new ArrayList<>();
        for (PrixDeVente price : prices) {
            if (price.getMesureId().equals(m)
                    && (price.getQmin() <= quant
                    && price.getQmax() > quant)) {
                rst.add(price);
            }
        }
        return rst;
    }

    private void applyPrices(List<PrixDeVente> prices) {
        try {
            double d = Double.parseDouble(tf_input_quant.getText());
            PrixDeVente pv = Util.findPrice(prices, choosenmez, d);
            if (pv != null) {
                String dvz = pv.getDevise();
                tf_prix_unitr_cdf.setText(String.valueOf(String.valueOf(dvz.equals("CDF") ? pv.getPrixUnitaire()
                        : BigDecimal.valueOf(pv.getPrixUnitaire() * taux2change).setScale(3, RoundingMode.HALF_EVEN))));
                tf_prix_unitr_usd.setText(String.valueOf(dvz.equals("USD") ? pv.getPrixUnitaire()
                        : BigDecimal.valueOf(pv.getPrixUnitaire() / taux2change).setScale(2, RoundingMode.HALF_EVEN)));
                double tcdf;
                double tusd;
                if (dvz.equals("CDF")) {
                    tcdf = BigDecimal.valueOf(d * pv.getPrixUnitaire())
                            .setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                    tusd = BigDecimal.valueOf(tcdf / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    // Les champs USD restent toujours visibles quelle que soit la devise.
                } else {
                    tusd = BigDecimal.valueOf(d * pv.getPrixUnitaire()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    tcdf = BigDecimal.valueOf(tusd * taux2change).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                }
                txt_total_cdf.setText(String.valueOf(tcdf));
                txt_total_usd.setText(String.valueOf(tusd));
            } else {
                PrixDeVente pvx = Util.findAvailablePrice(prices);
                if (pvx == null) {
                    return;
                }

                Mesure pvm = pvx.getMesureId();
                Mesure pvmz = MesureDelegate.findMesure(pvm.getUid());
                if (choosenmez == null) {
                    return;
                }
                double raport = choosenmez.getQuantContenu() / pvmz.getQuantContenu();
                double pvs = pvx.getPrixUnitaire();
                double pup = BigDecimal.valueOf(pvs * raport).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                String dvz = pvx.getDevise();
                tf_prix_unitr_cdf.setText(String.valueOf(dvz.equals("CDF") ? pup
                        : BigDecimal.valueOf(pup * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue()));
                tf_prix_unitr_usd.setText(String.valueOf(dvz.equals("USD") ? pup
                        : BigDecimal.valueOf(pup / taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue()));
                double tcdf;
                double tusd;
                if (dvz.equals("CDF")) {
                    tcdf = BigDecimal.valueOf(d * pup).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                    tusd = BigDecimal.valueOf(tcdf / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    // Les champs USD restent toujours visibles quelle que soit la devise.
                    txt_total_cdf.setText(String.valueOf(tcdf));
                    txt_total_usd.setText(String.valueOf(tusd));
                } else {
                    tusd = BigDecimal.valueOf(d * pup).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    tcdf = BigDecimal.valueOf(tusd * taux2change).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                    txt_total_cdf.setText(String.valueOf(tcdf));
                    txt_total_usd.setText(String.valueOf(tusd));
                }

            }
        } catch (NumberFormatException e) {
        }
        tf_input_quant.requestFocus();
    }

    private Map.Entry<Recquisition, List<PrixDeVente>> getRecentPrice(String produit) {
//        List<Recquisition> lasts = RecquisitionDelegate.findDescSortedByDateForProduit(produit);
        List<PrixDeVente> pxs = RecquisitionDelegate.findLastPrices(produit);
        if (pxs == null || pxs.isEmpty()) {
            return new AbstractMap.SimpleEntry<>(choosenRecquisition, new ArrayList<>());
        }
        Recquisition rec = pxs.get(0).getRecquisitionId();
        if (rec == null) {
            rec = choosenRecquisition;
        }
        return new AbstractMap.SimpleEntry<>(rec, new ArrayList<>(pxs));

    }

    private void config() {
        cbx_mesure_selected.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure_selected.getItems()
                        .stream()
                        .filter(v -> (v.getDescription())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_lot_recquisitionee.setConverter(new StringConverter<StockAgregate>() {
            @Override
            public String toString(StockAgregate object) {
                return object == null ? null : object.getNumlot();
            }

            @Override
            public StockAgregate fromString(String string) {
                return cbx_lot_recquisitionee.getItems()
                        .stream()
                        .filter(object -> (object.getNumlot())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

    }

    @FXML
    public void increase(ActionEvent evt) {
        quant++;
        tf_input_quant.setText(String.valueOf(quant));
//        try {
//            resolve(String.valueOf(quant));
//        } catch (Exception e) {
//
//        }
    }

    @FXML
    public void addToCart(Event ebt) {
        if (prod == null || choosenmez == null || choosenRecquisition == null || choosenLotStock == null) {
            MainUI.notify(null, bundle.getString("error"), "Produit, lot ou mesure manquant.", 4, "error");
            return;
        }
        if (tf_prix_unitr_usd.getText().isEmpty() || tf_prix_unitr_cdf.getText().isEmpty() || tf_input_quant.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), "Prix ou quantité invalide.", 4, "error");
            return;
        }
        String dex = CurrencyConverter.mainCurrency();
        double qr = parseDoubleOrDefault(tf_input_quant.getText(), 0);
        if (qr <= 0) {
            MainUI.notify(null, bundle.getString("error"), "La quantité doit être supérieure à 0.", 4, "error");
            return;
        }
        double prixUnitUsd = parseDoubleOrDefault(tf_prix_unitr_usd.getText(), 0);
        double prixUnitCdf = parseDoubleOrDefault(tf_prix_unitr_cdf.getText(), 0);
        if (prixUnitUsd <= 0 && prixUnitCdf <= 0) {
            MainUI.notify(null, bundle.getString("error"), "Prix unitaire invalide.", 4, "error");
            return;
        }
        double valeurTotalUsd = parseDoubleOrDefault(txt_total_usd.getText(), qr * prixUnitUsd);
        double valeurTotalCdf = parseDoubleOrDefault(txt_total_cdf.getText(), qr * prixUnitCdf);
        if (CurrencyConverter.USD.equals(dex)) {
            valeurTotalCdf = CurrencyConverter.fromUsd(valeurTotalUsd, CurrencyConverter.CDF);
        } else {
            valeurTotalUsd = CurrencyConverter.toUsd(valeurTotalCdf, CurrencyConverter.CDF);
        }

        // Reprendre la quantite courante juste avant validation pour eviter un stale state.
        resteEnPiece = getCurrentRemainingPieces();
        double rest = PosController.getInstance().getRest(prod);
        double kms = rest;
        // choosenmez.getQuantContenu();
        double qpc = qr
                * choosenmez.getQuantContenu();
        Recquisition last = choosenRecquisition;
        Mesure mi = last.getMesureId();
        Mesure m = mi == null ? null : MesureDelegate.findMesure(mi.getUid());
        if (m == null) {
            List<Mesure> mesurs = MesureDelegate.findAscSortedByQuantWithProduit(prod.getUid());
            m = mesurs.isEmpty() ? null : mesures.get(0);
        }
        Double oal = last.getStockAlert();
        double alt = ((oal == null ? 0 : oal) * (m == null ? 1 : m.getQuantContenu())) / choosenmez.getQuantContenu();
        double rst = rest - qpc;
        if (PosController.getInstance().isAlertReached(prod.getUid())) {
            if (rst <= 0) {
                Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, String.format(bundle.getString("quantremn"), kms, choosenmez.getDescription(), choosenRecquisition.getNumlot()), ButtonType.YES, ButtonType.CANCEL);
                alertdlg.setTitle(bundle.getString("warning"));
                alertdlg.setHeaderText(null);
                Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    qr = kms;
                    valeurTotalUsd = BigDecimal.valueOf(qr * prixUnitUsd)
                            .setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    valeurTotalCdf = BigDecimal.valueOf(valeurTotalUsd * taux2change)
                            .setScale(0, RoundingMode.HALF_EVEN).doubleValue();
//                    cdf = dex.equals("USD") ? usd * taux2change : cdf;
                } else if (showAndWait.get() == ButtonType.CANCEL) {
                    return;
                }
            } else {
                Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, String.format(bundle.getString("stockalertok"), alt, choosenmez.getDescription(), choosenRecquisition.getNumlot()), ButtonType.YES, ButtonType.CANCEL);
                alertdlg.setTitle(bundle.getString("warning"));
                alertdlg.setHeaderText(null);
                Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                if (showAndWait.get() == ButtonType.CANCEL) {
                    return;
                }
            }
        }
//        else {
//            usd = BigDecimal.valueOf(qr * prixEnQuestion)
//                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue();
//        }
        if (rst <= -1) {
            MainUI.notify(null, bundle.getString("warning"), bundle.getString("quantexceed"), 4, "warning");
            return;
        }
        double coutAchat = choosenRecquisition.getCoutAchat();
        if (coutAchat <= 0) {
            List<Recquisition> order = RecquisitionDelegate.toLifoOrdering(prod.getUid());
            if (!order.isEmpty()) {
                coutAchat = order.get(0).getCoutAchat();
            }
        }
        Mesure chrmez = choosenLotStock.getMesureId() == null ? last.getMesureId() : choosenLotStock.getMesureId();
        if (chrmez == null || chrmez.getQuantContenu() == 0) {
            MainUI.notify(null, bundle.getString("error"), "Mesure de lot invalide.", 4, "error");
            return;
        }
        if(!PosController.getInstance().pass(qr, last, prod, choosenmez))return;
        double ctpc = coutAchat / chrmez.getQuantContenu();
        double chznpc = choosenmez.getQuantContenu();
        String time = String.valueOf(System.currentTimeMillis());
        String lvid = String.valueOf((long) (Math.random() * 1000000)).concat(time.substring(time.length() - 2, time.length()));
        LigneVente lv = new LigneVente(action.equals("Modif") ? this.id : Long.valueOf(lvid));
        lv.setMesureId(choosenmez);
        lv.setMontantCdf(valeurTotalCdf);
        lv.setCoutAchat((ctpc * chznpc));
        lv.setMontantUsd(valeurTotalUsd);
        lv.setPrixUnit(prixUnitUsd);
        lv.setNumlot(choosenLotStock.getNumlot());
        lv.setProductId(prod);
        lv.setQuantite(qr);
//        if (PosController.getInstance().isInScanMode()) { 
        MainuiController.getInstance().reinitSearchBar();
//        }
        PosController.getInstance().addCartItem(lv);
        close(ebt);
//       PosController.getInstance().fillProductInTable("All");
//       
//        MainuiController.getInstance().cleanSearchBar();
//         PosController.getInstance().refreshPosUi();
    }

    private void writeCartItem(String texte) throws IOException {
        //        Produit pr=ProduitDelegate.findProduit(tokens[0]);
//                            Mesure mz=MesureDelegate.findMesure(tokens[1]);
//                            double quant=Double.valueOf(tokens[2]);
//                            double unitPrice=Double.valueOf(tokens[3]);
//                            String batch=tokens[4];
//                            String dev=tokens[5];
        ////            EAN13Writer writer = new EAN13Writer();
        Code128Writer w128 = new Code128Writer();
//            Code128Writer c39 = new Code39Writer();
        BitMatrix matrix = w128.encode(texte, BarcodeFormat.CODE_128, 256, 150);
        BufferedImage bimage = MatrixToImageWriter.toBufferedImage(matrix);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(bimage, "jpg", stream);
            img_codebar.setImage(new Image(new ByteArrayInputStream(stream.toByteArray())));
        }
    }

    @FXML
    public void decrease(ActionEvent evt) {
        if (quant >= 2) {
            quant--;
            tf_input_quant.setText(String.valueOf(quant));
//            try {
//                resolve(String.valueOf(quant));
//            } catch (IOException e) {
//
//            }
        } else {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("emptinesswarn"), 3, "error");
        }
    }

    private boolean hasGlobalStockAccess() {
        return UserRoleRegistry.isTrader(pref) || UserRoleRegistry.hasAllAccess(pref);
    }

    private double getCurrentRemainingPieces() {
        if (prod == null) {
            return 0;
        }
        // Quantite POS/panier en pieces: somme des final_quantity depuis stock_agregate.
        double pieces = hasGlobalStockAccess()
                ? RecquisitionDelegate.sumLatestLotFinalQuantityFromStockAggregate(prod.getUid())
                : RecquisitionDelegate.sumLatestLotFinalQuantityFromStockAggregate(prod.getUid(), region);
        return Math.max(0, pieces);
    }

    private double parseDoubleOrDefault(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
