/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.barcode.BarCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import delegates.LigneVenteDelegate;
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
import data.Stocker;
import org.apache.commons.lang3.StringUtils;
import tools.Constants;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import data.helpers.Role; import data.network.Kazisafe;

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
    private ComboBox<Recquisition> cbx_lot_recquisitionee;
    @FXML
    private Label txt_peremption;
    @FXML
    private Label txt_alerte_pa;
    @FXML
    ImageView img_codebar;

    //JpaStorage db;
    List<PrixDeVente> prices;
    List<Mesure> mesures;

    ObservableList<Recquisition> lisrecquisition;

    Produit prod;

    Mesure choosenmez;
    Recquisition choosenRecquisition;
    Preferences pref;
    double taux2change;
    double quant = 1, reste;
    String region, role, meth, action;
    ResourceBundle bundle;
    long id;
    double resteEnPiece;
    @FXML
    private RadioButton thermal, jet;
    @FXML
    private Pane pane_print;
    Printer defaultPrinter;
    ToggleGroup printerGroup;
    String dev;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux2change = pref.getDouble("taux2change", 2000);
        region = pref.get("region", "...");
        role = pref.get("priv", null);
        meth = pref.get("meth", "fifo");
        printerGroup = new ToggleGroup();
        thermal.setToggleGroup(printerGroup);
        jet.setToggleGroup(printerGroup);
        pane_print.setVisible(false);
        config();
        cbx_mesure_selected.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) -> {
            choosenmez = newValue;
            if (choosenRecquisition == null) {
                return;
            }
            String numlotx = choosenRecquisition.getNumlot();
            if (role.equals(Role.Trader.name()) | role.equals(Role.ALL_ACCESS.name())) {
                resteEnPiece = RecquisitionDelegate.findRemainedInMagasinFor(prod.getUid());
            } else {
                resteEnPiece = RecquisitionDelegate.findRemainedInMagasinFor(prod.getUid(), region);
            }
            prices = PrixDeVenteDelegate.findPricesForRecq(choosenRecquisition.getUid());

            reste = (resteEnPiece / choosenmez.getQuantContenu());
            txt_available_quant.setText(reste + " " + choosenmez.getDescription());
            if (!tf_input_quant.getText().isEmpty() && StringUtils.isNumeric(tf_input_quant.getText())) {
                Double d = Double.valueOf(tf_input_quant.getText());
                List<PrixDeVente> pvds = PrixDeVenteDelegate.findSpecificByQuant(choosenRecquisition, choosenmez, d);
                if (pvds.isEmpty()) {
                    applyPrices(prices);
                }
                PrixDeVente pvd = pvds.get(0);
                dev = pvd.getDevise();
                if (dev.equals("CDF")) {
                    tf_prix_unitr_cdf.setText(String.valueOf(pvd.getPrixUnitaire()));
                    tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pvd.getPrixUnitaire() / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                    double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
                    double cdf = Double.parseDouble(tf_prix_unitr_cdf.getText());
                    double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    double tcfd = BigDecimal.valueOf(d * cdf).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    txt_total_cdf.setText(String.valueOf(tcfd));
                    txt_total_usd.setText(String.valueOf(tusd));
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

    public void setProduit(Entreprise eze, Kazisafe ksf, Produit produit, String action, long id) {
        this.prod = produit;
        this.action = action;
        this.id = id;
        lisrecquisition = FXCollections.observableArrayList();
        // this.db = JpaStorage.getInstance();
        Util.installPicture(img_vu_pixa_prod, prod.getUid() + ".jpeg");
        txt_codebar.setText(prod.getCodebar());
        System.out.println("selected pro " + prod.getNomProduit() + " From " + eze);

        mesures = MesureDelegate.findAscSortedByQuantWithProduit(prod.getUid());
        Mesure minMesure = mesures.get(0);
        choosenmez = minMesure;
        ObservableList<Mesure> lmez = FXCollections.observableArrayList(mesures);
        cbx_mesure_selected.setItems(lmez);
        cbx_lot_recquisitionee.setItems(lisrecquisition);
        cbx_mesure_selected.getSelectionModel().selectFirst();
        if (minMesure == null) {
            choosenmez = cbx_mesure_selected.getValue();
        }
         ObservableSet<Printer> osp = Printer.getAllPrinters();
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();
        cbx_printers.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) -> {
            defaultPrinter = newValue;
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        txt_product_name.setText(prod.getNomProduit() + " " + (prod.getMarque() == null ? "" : prod.getMarque()) + " " + (prod.getModele() == null ? "" : prod.getModele()) + ""
                + " " + (prod.getTaille() == null ? "" : prod.getTaille()) + " " + (prod.getCouleur() == null ? "" : prod.getCouleur()));
//        applyPrice(prices);
        if (meth.equals("ppps")) {
            List<Recquisition> lsks = RecquisitionDelegate.toFefoOrdering(prod.getUid());
            //db.findByProduitOrderByExp(Recquisition.class, prod.getUid());
            for (Recquisition lsk : lsks) {
                if (lsk.getNumlot() == null) {
                    lsk.setNumlot(Constants.TIMESTAMPED_FORMAT.format(lsk.getDate()));
                }
                if (!isSameLotExistInRecqs(lsk.getNumlot())) {
                    lisrecquisition.add(lsk);
                }
            }
        } else if (meth.equals("fifo")) {
            List<Recquisition> lsks = RecquisitionDelegate.toFifoOrdering(prod.getUid());
            //db.findByProduitOrderByDateAsc(Recquisition.class, prod.getUid());
            for (Recquisition lsk : lsks) {
                if (lsk.getNumlot() == null) {
                    lsk.setNumlot(Constants.TIMESTAMPED_FORMAT.format(lsk.getDate()));
                }
                if (!isSameLotExistInRecqs(lsk.getNumlot())) {
                    lisrecquisition.add(lsk);
                }
            }
        } else if (meth.equals("lifo")) {
            List<Recquisition> lsks = RecquisitionDelegate.toLifoOrdering(prod.getUid());
            //db.findByProduitOrderByDateDesc(Recquisition.class, prod.getUid());
            for (Recquisition lsk : lsks) {
                if (lsk.getNumlot() == null) {
                    lsk.setNumlot(Constants.TIMESTAMPED_FORMAT.format(lsk.getDate()));
                }
                if (!isSameLotExistInRecqs(lsk.getNumlot())) {
                    lisrecquisition.add(lsk);
                }
            }
        }
        System.out.println("Recqusis met size " + lisrecquisition.size());
        Recquisition choosen = chooseValideRecquisition(lisrecquisition);
        cbx_lot_recquisitionee.getSelectionModel().select(choosen);
        if (choosen == null) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("choosemeth"), 4, "error");
            return;
        }
        List<Stocker> locals = StockerDelegate.findStockerByProduitLot(prod.getUid(), choosen.getNumlot());
        Stocker local = locals.isEmpty() ? null : locals.get(0);
        localisabel.setText(local == null ? "" : local.getLocalisation());
        choosenRecquisition = cbx_lot_recquisitionee.getValue();
        System.out.println("Recs size " + lisrecquisition.size());
        Recquisition headerRecq = PosController.getInstance().getHeaderRecq(prod);
        String numlot = choosenRecquisition == null ? headerRecq.getNumlot() : choosenRecquisition.getNumlot();

        int expir = isStockExpired(choosenRecquisition);
        if (expir == -1) {
            txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f58282"), new CornerRadii(20), new Insets(4))));
        } else if (expir == 3) {
            txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c46506"), new CornerRadii(20), new Insets(4))));
        } else if (expir == 6) {
            txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f7fa61"), new CornerRadii(20), new Insets(4))));
        } else if (expir == 12) {
            txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c5e6b3"), new CornerRadii(20), new Insets(4))));
        } else {
            txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
        }
        txt_peremption.setStyle("-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 6;");
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            resteEnPiece = RecquisitionDelegate.findRemainedInMagasinFor(prod.getUid());
        } else {
            resteEnPiece = RecquisitionDelegate.findRemainedInMagasinFor(prod.getUid(), region);
        }
        prices = PrixDeVenteDelegate.findPricesForRecq(choosenRecquisition.getUid());
        txt_alerte_pa.setText(choosenRecquisition.getStockAlert() + " " + choosenmez.getDescription());

        txt_available_quant.setText((resteEnPiece / choosenmez.getQuantContenu()) + " " + choosenmez.getDescription());
        List<PrixDeVente> pvxs = PrixDeVenteDelegate.findSpecificByQuant(choosenRecquisition, choosenmez, 1);
        if (!pvxs.isEmpty()) {
            PrixDeVente pvx = pvxs.get(0);
            String dev = pvx.getDevise();
//                    pref.get("mainCur", "USD");
            if (dev.equals("CDF")) {
                tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire()));
                tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pvx.getPrixUnitaire() / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                double pv = Double.parseDouble(tf_prix_unitr_cdf.getText());
                double tcdf = BigDecimal.valueOf(1 * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                double tusd = BigDecimal.valueOf(tcdf / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                txt_total_cdf.setText(String.valueOf(tcdf));
                txt_total_usd.setText(String.valueOf(tusd));
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
        String text = "X" + prod.getUid() + "-" + choosenmez.getQuantContenu() + "-" + tf_input_quant.getText() + "-"
                + "" + choosenRecquisition.getNumlot() + "-" + dev;
        try {
            writeCartItem(text.trim());
        } catch (IOException ex) {
            Logger.getLogger(PanierappenderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        tf_input_quant.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    double d = Double.parseDouble(newValue);
                    List<PrixDeVente> pvxs = PrixDeVenteDelegate.findSpecificByQuant(choosenRecquisition, choosenmez, d);

                    if (pvxs.isEmpty()) {
                        return;
                    }

                    PrixDeVente pvx = pvxs.get(0);
                    String dev = pvx.getDevise();
                    if (dev.equals("CDF")) {
                        tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pvx.getPrixUnitaire() / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                        tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire()));
                        double fc = Double.parseDouble(tf_prix_unitr_cdf.getText());
                        double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
                        double tusd = BigDecimal.valueOf(d * pv).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        double tcfd = BigDecimal.valueOf(d * fc).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                        txt_total_cdf.setText(String.valueOf(tcfd));
                        txt_total_usd.setText(String.valueOf(tusd));
                    } else {
                        tf_prix_unitr_usd.setText(String.valueOf(pvx.getPrixUnitaire()));
                        tf_prix_unitr_cdf.setText(String.valueOf(pvx.getPrixUnitaire() * taux2change));
                        double pv = Double.parseDouble(tf_prix_unitr_usd.getText());
                        double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                        double tcfd = BigDecimal.valueOf(tusd * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                        txt_total_cdf.setText(String.valueOf(tcfd));
                        txt_total_usd.setText(String.valueOf(tusd));
                    }
                    String text = "X" + prod.getUid() + "-" + choosenmez.getQuantContenu() + "-" + tf_input_quant.getText() + "-"
                            + "" + choosenRecquisition.getNumlot() + "-" + dev;
                    writeCartItem(text);
                } catch (NumberFormatException e) {

                } catch (IOException ex) {
                    Logger.getLogger(PanierappenderController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

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
                    pv = (pv / taux2change);
                    if (tf_prix_unitr_cdf.isFocused()) {
                        tf_prix_unitr_usd.setText(String.valueOf(BigDecimal.valueOf(pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue()));
                    }
                    double tusd = BigDecimal.valueOf(d * pv).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    double tcfd = BigDecimal.valueOf(tusd * taux2change).setScale(3, RoundingMode.HALF_EVEN).doubleValue();

                    txt_total_cdf.setText(String.valueOf(tcfd));
                    txt_total_usd.setText(String.valueOf(tusd));
                } catch (NumberFormatException e) {

                }
            }
        });

        cbx_lot_recquisitionee.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Recquisition>() {
            @Override
            public void changed(ObservableValue<? extends Recquisition> observable, Recquisition oldValue, Recquisition newValue) {
                if (newValue != null) {
                    choosenRecquisition = newValue;
                    Date date = choosenRecquisition.getDateExpiry();
                    txt_peremption.setText(date == null ? bundle.getString("noperish") : "  Exp : " + Constants.USER_READABLE_FORMAT.format(date));
                    txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
//                    if (date == null) {
//                        return;
//                    }

                    Mesure mzr = choosenRecquisition.getMesureId();
                    Mesure mz = MesureDelegate.findMesure(mzr.getUid());
                    if (mz == null) {
                        List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(choosenRecquisition.getProductId().getUid());
                        mz = mesures.get(0);
                    }

                    double dispo = choosenRecquisition.getQuantite();
                    double stalertpc = (choosenRecquisition.getStockAlert() * mz.getQuantContenu());
                    double alerte = stalertpc / choosenmez.getQuantContenu();
                    txt_alerte_pa.setText("Alt:" + alerte + " " + choosenmez.getDescription());
                    if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                        resteEnPiece = RecquisitionDelegate.findRemainedInMagasinFor(prod.getUid());
                    } else {
                        resteEnPiece = RecquisitionDelegate.findRemainedInMagasinFor(prod.getUid(), region);
                    }
                    reste = resteEnPiece / choosenmez.getQuantContenu();
                    txt_available_quant.setText(reste + " " + choosenmez.getDescription());
                    List<Stocker> lstk = StockerDelegate.findStockerByProduitLot(choosenRecquisition.getProductId().getUid(), choosenRecquisition.getNumlot());
                    if (lstk.isEmpty()) {
                        return;
                    }
                    localisabel.setText(lstk.get(0).getLocalisation());
                    int exp = isStockExpired(choosenRecquisition);
                    if (exp == -1) {
                        txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f58282"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 3) {
                        txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c46506"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 6) {
                        txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f7fa61"), new CornerRadii(20), new Insets(4))));
                    } else if (exp == 12) {
                        txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c5e6b3"), new CornerRadii(20), new Insets(4))));
                    } else {
                        txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
                    }
                    txt_peremption.setStyle("-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 6;");
                }
            }
        });

        Date date = choosenRecquisition.getDateExpiry();
        txt_peremption.setText(date == null ? bundle.getString("noperish") : "  Exp : " + Constants.USER_READABLE_FORMAT.format(date));
        txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
        if (txt_peremption.getText().trim().equals(bundle.getString("noperish"))) {
            return;
        }
        int exp = isStockExpired(choosenRecquisition);
        switch (exp) {
            case -1:
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f58282"), new CornerRadii(20), new Insets(4))));
                break;
            case 3:
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c46506"), new CornerRadii(20), new Insets(4))));
                break;
            case 6:
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#f7fa61"), new CornerRadii(20), new Insets(4))));
                break;
            case 12:
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#c5e6b3"), new CornerRadii(20), new Insets(4))));
                break;
            default:
                txt_peremption.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(20), new Insets(4))));
                break;
        }
        txt_peremption.setStyle("-fx-border-color: #44cef5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-label-padding: 6;");
        tf_input_quant.requestFocus();
    }
    
    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        osp.forEach((p) -> {
            rst.add(p);
        });
        return rst;
    }

    private boolean isSameLotExistInRecqs(String numlot) {
        for (Recquisition recquisition : lisrecquisition) {
            if (recquisition.getNumlot().equals(numlot)) {
                return true;
            }
        }
        return false;
    }
    
    

    private void printBCWithThermal(String printerName, String bcode) {
        PrinterOutputStream pos = null;
        try {
            if (bcode.isEmpty()) {
                return;
            }
            PrintService ps = PrinterOutputStream.getPrintServiceByName(printerName);
            pos = new PrinterOutputStream(ps);
            EscPos printer = new EscPos(pos);
            BarCode bc = new BarCode();
            bc.setBarCodeSize(12, 6).setJustification(EscPosConst.Justification.Center)
                    .getBytes(bcode);
            printer.feed(2);
            printer.cut(EscPos.CutMode.FULL);
            printer.close();
        } catch (IOException ex) {
            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                pos.close();
            } catch (IOException ex) {
                Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    public void printbarcode(Event e) {
        if (thermal.isSelected()) {
            String text = "ENCODE:" + prod.getUid() + "-" + choosenmez.getUid() + "-" + tf_input_quant.getText() + "-"
                    + "" + choosenRecquisition.getNumlot() + "-" + dev;
            printBCWithThermal(defaultPrinter.getName(), text);
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

    private Recquisition chooseValideRecquisition(List<Recquisition> reqs) {
        List<Recquisition> lrq;
        List<LigneVente> llv;
        for (Recquisition req : reqs) {
            String numlot = req.getNumlot();
            if (role.equals(Role.Trader.name()) || role.contains(Role.ALL_ACCESS.name())) {
                lrq = RecquisitionDelegate.findRecquisitionByProduit(prod.getUid(), numlot);
                llv = LigneVenteDelegate.findByProduitWithLot(prod.getUid(), numlot);
            } else {
                llv = LigneVenteDelegate.findByProduitWithLot(prod.getUid(), numlot, region);
                lrq = RecquisitionDelegate.findRecquisitionByProduit(prod.getUid(), numlot, region);
            }
//            List<Recquisition> lr = fullMesureRecqs(lrq);
//            List<LigneVente> ll = fullMesureRecqs(llv);
            double entree = Util.sumQuantInPc(lrq);
            double sortie = Util.sumQuantInPc(llv);
            reste = (entree - sortie);
            if (reste > 0) {
                return req;
            }
        }
        if (reqs.isEmpty()) {
            return null;
        }
        return reqs.get(0);
    }

    private int isStockExpired(Recquisition e) {
        long now = System.currentTimeMillis();
        if (e.getDateExpiry() == null) {
            return 555;
        }
        long exp = e.getDateExpiry().getTime();
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
                    tcdf = BigDecimal.valueOf(d * pv.getPrixUnitaire()).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                    tusd = BigDecimal.valueOf(tcdf / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
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
                double pvs = pvx.getPrixUnitaire() == null ? 0 : pvx.getPrixUnitaire();
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
                } else {
                    tusd = BigDecimal.valueOf(d * pup).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    tcdf = BigDecimal.valueOf(tusd * taux2change).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
                }
                txt_total_cdf.setText(String.valueOf(tcdf));
                txt_total_usd.setText(String.valueOf(tusd));
            }
        } catch (NumberFormatException e) {
        }
        tf_input_quant.requestFocus();
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
        cbx_lot_recquisitionee.setConverter(new StringConverter<Recquisition>() {
            @Override
            public String toString(Recquisition object) {
                return object == null ? null : object.getNumlot();
            }

            @Override
            public Recquisition fromString(String string) {
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
    }

    @FXML
    public void addToCart(Event ebt) {
        if (tf_prix_unitr_usd.getText().isEmpty() || tf_prix_unitr_cdf.getText().isEmpty()) {
            return;
        } 
        double qr = Double.parseDouble(tf_input_quant.getText());
        double pu = Double.parseDouble(tf_prix_unitr_usd.getText());
        double usd = Double.parseDouble(txt_total_usd.getText());
        double cdf = Double.parseDouble(txt_total_cdf.getText());
        double rest = PosController.getInstance().getRest(prod);
        double kms = rest;
        // choosenmez.getQuantContenu();
        double qpc = qr
                * choosenmez.getQuantContenu();
        Recquisition last = cbx_lot_recquisitionee.getValue();
        Mesure mi = last.getMesureId();
        Mesure m = MesureDelegate.findMesure(mi.getUid());
        if (m == null) {
            List<Mesure> mesures = MesureDelegate.findAscSortedByQuantWithProduit(prod.getUid());
            m = mesures.get(0);
        }
        Double oal = last.getStockAlert();
        double alt = ((oal == null ? 0 : oal) * m.getQuantContenu()) / choosenmez.getQuantContenu();
        double rst = resteEnPiece - qpc;
        if (PosController.getInstance().isAlertReached(prod.getUid())) {
            if (rst <= 0) {
                Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, String.format(bundle.getString("quantremn"), kms, choosenmez.getDescription(), choosenRecquisition.getNumlot()), ButtonType.YES, ButtonType.CANCEL);
                alertdlg.setTitle(bundle.getString("warning"));
                alertdlg.setHeaderText(null);
                Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    qr = kms;
                    usd = BigDecimal.valueOf(qr * pu).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
                    cdf = usd * taux2change;
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
        if (rst <= -1) {
            MainUI.notify(null, bundle.getString("warning"), bundle.getString("quantexceed"), 4, "warning");
            return;
        }
        String time = String.valueOf(System.currentTimeMillis());
        String lvid = String.valueOf((long) (Math.random() * 1000000)).concat(time.substring(time.length() - 2, time.length()));
        LigneVente lv = new LigneVente(action.equals("Modif") ? this.id : Long.valueOf(lvid));
        lv.setMesureId(choosenmez);
        lv.setMontantCdf(cdf);
        lv.setMontantUsd(usd);
        lv.setPrixUnit(pu);
        lv.setNumlot(cbx_lot_recquisitionee.getValue().getNumlot());
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
InputStream is;
//            EAN13Writer writer = new EAN13Writer();
Code128Writer w128 = new Code128Writer();
//            Code128Writer c39 = new Code39Writer();
BitMatrix matrix = w128.encode(texte, BarcodeFormat.CODE_128, 256, 150);
BufferedImage bimage = MatrixToImageWriter.toBufferedImage(matrix);
try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
    ImageIO.write(bimage, "jpg", stream);
    is = new ByteArrayInputStream(stream.toByteArray());
}
is.close();
// } else {
//            ByteArrayOutputStream stream = net.glxn.qrgen.QRCode
//                    .from(texte)
//                    .withSize(250, 250)
//                    .stream();
//            is = new ByteArrayInputStream(stream.toByteArray());
// }
img_codebar.setImage(new Image(is));

    }

    @FXML
    public void decrease(ActionEvent evt) {
        if (quant >= 2) {
            quant--;
            tf_input_quant.setText(String.valueOf(quant));
        } else {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("emptinesswarn"), 3, "error");
        }
    }
}
