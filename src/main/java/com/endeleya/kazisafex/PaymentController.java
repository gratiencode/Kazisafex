/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

//import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.output.PrinterOutputStream;
import delegates.ClientDelegate;
import delegates.CompteTresorDelegate;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import data.core.KazisafeServiceFactory;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import data.network.Kazisafe;
import data.Client;
import data.CompteTresor;
import data.Entreprise;
import data.LigneVente;
import data.Mesure;
import data.Produit;
import data.Traisorerie;
import data.Vente;
import data.VenteResult;
import data.helpers.Mouvment;
import data.helpers.TypeTraisorerie;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.ComboBoxAutoCompletion;
import tools.Constants;
import tools.DataId;
import tools.FileUtils;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import utilities.PDFUtils;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class PaymentController implements Initializable {

    @FXML
    private Label txt_eval_sum_usd, txt_bill_num_facture, txt_bill_companyname, txt_comp_adresse, txt_comp_adresse_tel, txt_bill_user, txt_lbl_credit, txt_bill_somme_credit;
    @FXML
    private Label txt_eval_sum_cdf, txt_bill_date_vente, txt_bill_somme_facture, txt_bill_contact_entreprise, txt_bill_cash_paid, txt_reduction;
    @FXML
    private TextField tf_nominal_recu_usd;
    @FXML
    private TextField tf_nominal_recu_cdf;
    @FXML
    private TextField tf_arembourser_cdf;
    @FXML
    private TextField tf_arembourser_usd;
    @FXML
    private TextField tf_phone_client;
    @FXML
    private CheckBox chbx_print_receipt, chbx_print_thermal;
    @FXML
    private Label txt_reference_vente, txt_print_status,
            txt_nom_client, txt_nom_client1, txt_bill_comp_id, txt_bill_comp_idnat, txt_client_selected_pay;
    @FXML
    private DatePicker dpk_echeance_debt;
    @FXML
    private ComboBox<String> cbx_payment_mode;
    @FXML
    private ComboBox<Client> cbx_clients;
    @FXML
    private ComboBox<CompteTresor> cbx_comptes;
    @FXML
    private ComboBox<Printer> cbx_printers;
    @FXML
    TableView<LigneVente> tbl_bill_products;
    @FXML
    TableColumn<LigneVente, String> col_bill_qte;
    @FXML
    TableColumn<LigneVente, String> col_bill_designation;
    @FXML
    TableColumn<LigneVente, Number> col_bill_pu;
    @FXML
    TableColumn<LigneVente, Number> col_bill_prix_unit;
    @FXML
    ImageView img_vu_logo;
    @FXML
    TextField cliname;
    @FXML
    TextField tflibelle;
    @FXML
    CheckBox save2favorite;
    String messageForCustomer;
    ObservableList<Client> clients;
    ObservableList<CompteTresor> comptes;
    Client client;
    CompteTresor choosenComptTr;
    Printer defaultPrinter;
    List<LigneVente> venteItems;
    File f;
    //JpaStorage db;

    Vente vente4save;
    Preferences pref;
    double taux2change;
    double sumCopy = 0;
    double cdf = 0, revertCdf, ff, fd;
    double usd = 0, revertUsd, dt;
    String user, typecli;
    boolean print;
    Kazisafe kazisafe;
    VBox vbx;
    @FXML
    private Pane pane_invoiced;
    @FXML
    private Pane pane_bill_sum_credit;
    @FXML
    private Pane pane_bill_cash_paid;
    @FXML
    AnchorPane billbed;

    //int invoiceId;
    /*
     pref.putInt("print-title-size", 3);
                        pref.putInt("print-body-size", 1);
                        pref.putInt("print-identite-size", 1);
                        pref.putInt("print-lines-dashcount", line_dashes);
     */
    private int title_s, identite_s, body_s, line_dashes;
    private static PaymentController instance;
    private Entreprise entreprise;
    String entrepName, idNat, phonez, adresse, email, nif, rccm;
    String region, role;
    int count_logic = 0;
    int copies = 1;
    @FXML
    private Label txt_print_status1;

    public PaymentController() {
        instance = this;
    }

    public static PaymentController getInstance() {
        return instance;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux2change = pref.getDouble("taux2change", 2000);
        print = pref.getBoolean("print", true);
        role = pref.get("priv", null);
        region = pref.get("region", "...");
        cbx_payment_mode.setItems(FXCollections.observableArrayList(TypeTraisorerie.CAISSE.name(), TypeTraisorerie.BANQUE.name(), "MOBILE MONEY", Mouvment.CREDIT.name(), Mouvment.CREDIT.name() + "+" + Mouvment.CASH.name()));
        cbx_payment_mode.getSelectionModel().selectFirst();
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        System.out.println("Printewrs count " + osp.size());
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();

        cbx_printers.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) -> {
            defaultPrinter = newValue;
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        title_s = pref.getInt("print-title-size", 1);
        body_s = pref.getInt("print-body-size", 1);
        identite_s = pref.getInt("print-identite-size", 1);
        line_dashes = pref.getInt("print-lines-dashcount", 48);
        count_logic = pref.getInt("count-logic", 0);
        copies = pref.getInt("bill-copy", 1);
        entrepName = pref.get("ent_name", "unknown");
        rccm = pref.get("ent_ID", "Aucun");
        adresse = pref.get("ent_adresse", "aucune");
        email = pref.get("ent_email", "");
        idNat = pref.get("ent_idnat", "Aucun");
        nif = pref.get("ent_impot", "Aucun");
        phonez = pref.get("ent_phones", "");
    }

    public void setEntreprise(Entreprise e) {
        this.entreprise = e;
        if (entreprise == null) {
            return;
        }
//        db = JpaStorage.getInstance();
        client = ClientDelegate.findAnonymousClient();//db.getAnonymousClient();
        txt_bill_companyname.setText(this.entreprise.getNomEntreprise());
        txt_comp_adresse.setText(this.entreprise.getAdresse());
        txt_bill_comp_id.setText("RCCM : " + this.entreprise.getIdentification());
        txt_bill_contact_entreprise.setText(this.entreprise.getEmail());
        String imp = this.entreprise.getNumeroImpot() == null ? "Aucun" : this.entreprise.getNumeroImpot(), idnat = this.entreprise.getIdNat() == null ? "Aucun" : this.entreprise.getIdNat();
        txt_bill_comp_idnat.setText((!imp.equals("Aucun") && !idnat.equals("Aucun")) ? "Impôt:" + imp + " , IdNat:" + idnat : (!imp.equals("Aucun") && idnat.equals("Aucun")) ? "Impôt:" + imp : (imp.equals("Aucun") && !idnat.equals("Aucun")) ? "IdNat:" + idnat : " ");
        txt_comp_adresse_tel.setText("Tel:" + entreprise.getPhones());
        clients = FXCollections.observableArrayList(ClientDelegate.findClients());
        comptes = FXCollections.observableArrayList(CompteTresorDelegate.findCompteTresors(region));
        cbx_clients.setItems(clients);
        cbx_comptes.setItems(comptes);
        f = FileUtils.pointFile(entreprise.getUid() + ".png");
        InputStream is;
        if (!f.exists()) {
            is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            f = FileUtils.streamTofile(is);
        }
        Image image = null;
        try {
            image = new Image(new FileInputStream(f));
            img_vu_logo.setImage(image);
            Util.centerImage(img_vu_logo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ComboBoxAutoCompletion<>(cbx_clients);
        new ComboBoxAutoCompletion<>(cbx_comptes);
    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        for (Printer p : osp) {
            rst.add(p);
        }
        return rst;
    }

    public void setClient(Client c) {
        if (c == null) {
            return;
        }
        this.client = c;
        tf_phone_client.setText(c.getPhone());
        txt_client_selected_pay.setText(this.client.getNomClient());
        cbx_clients.setValue(this.client);
    }

    @FXML
    private void pickClient(Event evt) {
        String token = pref.get("token", null);
        MainUI.floatDialog(tools.Constants.CLIENT_DLG, 521, 635, token, kazisafe, entreprise, region);
    }
    int compteur = 0;

    public void setLines(List<LigneVente> lig, Vente invoice) {

        String token = pref.get("token", null);
        messageForCustomer = pref.get("mesc", "Les marchandises vendues ne sont ni reprises ni echangees");
        System.out.println("Message pour customers " + messageForCustomer);
        kazisafe = KazisafeServiceFactory.createService(token);

        venteItems = new ArrayList<>();
        //db = JpaStorage.getInstance();

        Vente invoiceId = null;
        if (invoice != null) {
            Vente invoices = VenteDelegate.findVente(invoice.getUid());//db.findByUid(Vente.class, invoice.getUid());
            if (invoices != null) {
                invoiceId = invoices;
            }
        }
        List<LigneVente> lgvt = invoiceId == null ? lig : LigneVenteDelegate.findByReference(invoiceId.getUid());
        List<LigneVente> lignes = sortByPriceLength(lgvt);
        if (invoiceId != null) {
            if (invoiceId.getObservation().equals("Drafted")) {
                lignes = lig;
            }
        }
        String dev = pref.get("mainCur", "USD");
        double somme = Util.sumCart(lignes, dev);
        double tot;
        if (dev.equals("CDF")) {
            cdf = somme;
            usd = somme / taux2change;
            revertUsd = usd;
            revertCdf = cdf;
            tot = sumFact(lignes, dev);
            txt_bill_somme_facture.setText("CDF : " + tot);
            txt_bill_somme_credit.setText("CDF : " + tot);
            sumCopy = somme;
            txt_eval_sum_usd.setText(String.valueOf(BigDecimal.valueOf(usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
            txt_eval_sum_cdf.setText(String.valueOf(somme));
        } else {
            cdf = somme * taux2change;
            usd = somme;
            revertUsd = usd;
            revertCdf = cdf;
            tot = sumFact(lignes, dev);
            txt_bill_somme_facture.setText("USD : " + tot);
            txt_bill_somme_credit.setText("USD : " + tot);
            sumCopy = somme;
            txt_eval_sum_usd.setText(String.valueOf(somme));
            txt_eval_sum_cdf.setText(String.valueOf(BigDecimal.valueOf(cdf).setScale(0, RoundingMode.HALF_EVEN).doubleValue()));
        }

        venteItems.clear();
        venteItems.addAll(lignes);
//      

        int ref = 0;
        if (invoiceId == null) {
            String time = String.valueOf(System.currentTimeMillis());
            String lvid = String.valueOf((int) (Math.random() * 10000)).concat(time.substring(time.length() - 2, time.length()));
            ref = Integer.parseInt(lvid);
            pane_bill_cash_paid.setVisible(false);
            txt_bill_cash_paid.setVisible(false);
        } else {
            if (!invoiceId.getObservation().equals("Drafted")) {
                tf_nominal_recu_usd.setText(String.valueOf(invoiceId.getMontantUsd()));
                tf_nominal_recu_cdf.setText(String.valueOf(invoiceId.getMontantCdf()));
            }
            if (invoiceId.getPayment().toUpperCase().contains("credit partiel".toUpperCase())) {
                pane_bill_cash_paid.setVisible(true);
                txt_bill_cash_paid.setVisible(true);
                txt_bill_somme_credit.setText(String.valueOf(invoiceId.getMontantDette()));
                txt_bill_somme_facture.setText(String.valueOf((invoiceId.getMontantUsd() + (invoiceId.getMontantCdf() / taux2change))));
                pane_bill_sum_credit.setVisible(true);
                txt_lbl_credit.setVisible(true);
            } else if (invoiceId.getPayment().contains("Cash")
                    | invoiceId.getPayment().contains(TypeTraisorerie.ELECTRONIQUE.name())
                    | invoiceId.getPayment().contains("Banque")) {
                pane_bill_cash_paid.setVisible(true);
                txt_bill_cash_paid.setVisible(true);
                pane_bill_sum_credit.setVisible(false);
                txt_lbl_credit.setVisible(false);
            } else {
                pane_bill_cash_paid.setVisible(false);
                txt_bill_cash_paid.setVisible(false);
                txt_bill_somme_credit.setText(String.valueOf(invoiceId.getMontantDette()));
                pane_bill_sum_credit.setVisible(true);
                txt_lbl_credit.setVisible(true);
            }
            ref = invoiceId.getUid();
            Client clt = ClientDelegate.findClient(invoiceId.getClientId().getUid());
            txt_nom_client1.setText("Tel : " + (clt.getPhone().length() < 8 ? "..." : clt.getPhone()));
            txt_nom_client.setText("Client : " + clt.getNomClient());

            // StringUtils.isNumeric(invoiceId.getReference()) ? Integer.parseInt(invoiceId.getReference()) : invoice.getUid();
        }

        vente4save = new Vente(ref);
        int tbil = pref.getInt("tranzit_bill", -100);
        tf_nominal_recu_usd.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                double in_usd = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                if (newValue.isEmpty() && tf_nominal_recu_cdf.getText().isEmpty()) {
                    dt = usd;
                    ff = 0;
                    fd = 0;
                    txt_eval_sum_usd.setText(String.valueOf((new BigDecimal(usd).setScale(2, RoundingMode.HALF_UP).doubleValue())));
                    txt_eval_sum_cdf.setText(String.valueOf((new BigDecimal(cdf).setScale(2, RoundingMode.HALF_UP).doubleValue())));
                    tf_arembourser_usd.setText("0");
                    tf_arembourser_cdf.setText("0");
                    vente4save.setMontantUsd(0);
                    vente4save.setMontantCdf(0);
                } else if (!newValue.isEmpty() && tf_nominal_recu_cdf.getText().isEmpty()) {
//                    double in_usd = Double.parseDouble((editable.toString().isEmpty() ? "0" : editable.toString()));
                    double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if (restUsd >= 0) {
                        txt_eval_sum_usd.setText(String.valueOf(restUsd));
                        txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                        dt = restUsd;
                        fd = in_usd;
                        ff = 0;
                        tf_arembourser_cdf.setText("0");
                        tf_arembourser_usd.setText("0");
                    } else {
                        double retour = Math.abs(restUsd);
                        fd = in_usd - retour;
                        dt = 0;
                        ff = 0;
                        txt_eval_sum_usd.setText("0");
                        txt_eval_sum_cdf.setText("0.0");
                        tf_arembourser_usd.setText("" + retour);
                        tf_arembourser_cdf.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    vente4save.setMontantCdf(0);
                    vente4save.setMontantUsd(fd);

                } else if (newValue.isEmpty() && !tf_nominal_recu_cdf.getText().isEmpty()) {
                    double inCdf = Double.parseDouble(tf_nominal_recu_cdf.getText());
                    double restCdf = new BigDecimal(cdf - inCdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if (restCdf >= 0) {
                        txt_eval_sum_usd.setText(String.valueOf(restUsd));
                        txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                        dt = restUsd;
                        fd = in_usd;
                        ff = inCdf;
                        tf_arembourser_cdf.setText("0.0");
                        tf_arembourser_usd.setText("0.0");
                    } else {
                        double retour = Math.abs(restCdf);
                        fd = 0;
                        dt = 0;
                        ff = inCdf - retour;
                        txt_eval_sum_usd.setText("0.0");
                        txt_eval_sum_cdf.setText("0.0");
                        tf_arembourser_cdf.setText("" + retour);
                        tf_arembourser_usd.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    vente4save.setMontantUsd(0);
                    vente4save.setMontantCdf(ff);
                } else {
                    double inCdf = Double.parseDouble(tf_nominal_recu_cdf.getText());
                    double converted = new BigDecimal(inCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double nwInUsd = (in_usd + converted);
                    double restUsd = new BigDecimal(usd - nwInUsd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if (restUsd >= 0) {
                        txt_eval_sum_usd.setText(String.valueOf(restUsd));
                        txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                        dt = restUsd;
                        fd = in_usd;
                        ff = inCdf;
                        tf_arembourser_cdf.setText("");
                        tf_arembourser_usd.setText("");
                    } else {
                        double retour = Math.abs(restUsd);
                        fd = nwInUsd - retour;
                        dt = 0;
                        ff = 0;
                        txt_eval_sum_usd.setText("0.0");
                        txt_eval_sum_cdf.setText("0.0");
                        tf_arembourser_usd.setText("" + retour);
                        tf_arembourser_cdf.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    vente4save.setMontantUsd(fd);
                }
                double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                vente4save.setDeviseDette("USD");
                vente4save.setMontantDette(debt);
                if (debt > 0) {
                    if (tf_nominal_recu_usd.getText().isEmpty()) {
                        cbx_payment_mode.getSelectionModel().select(3);
                        pane_bill_cash_paid.setVisible(false);
                        txt_bill_cash_paid.setVisible(false);
                    } else {
                        double sin = Double.parseDouble(tf_nominal_recu_usd.getText());
                        txt_bill_somme_facture.setText(sin > tot ? String.valueOf(tot) : String.valueOf(sin));
                        pane_bill_cash_paid.setVisible(true);
                        txt_bill_cash_paid.setVisible(true);
                        cbx_payment_mode.getSelectionModel().select(4);
                    }
                    dpk_echeance_debt.setDisable(false);
                    txt_bill_somme_credit.setText(String.valueOf(debt));
                    pane_bill_sum_credit.setVisible(true);
                    txt_lbl_credit.setVisible(true);
                } else {
                    txt_bill_somme_facture.setText(String.valueOf(tot));
                    dpk_echeance_debt.setDisable(true);

                    cbx_payment_mode.getSelectionModel().select(0);
                    pane_bill_sum_credit.setVisible(false);
                    txt_lbl_credit.setVisible(false);
                }
            }
        });

        tf_nominal_recu_cdf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                double in_cdf = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                if (newValue.isEmpty() && tf_nominal_recu_usd.getText().isEmpty()) {
                    txt_eval_sum_cdf.setText(String.valueOf(new BigDecimal(cdf).setScale(2, RoundingMode.HALF_UP).doubleValue()));
                    txt_eval_sum_usd.setText(String.valueOf(new BigDecimal(usd).setScale(2, RoundingMode.HALF_UP).doubleValue()));
                    dt = usd;
                    ff = 0;
                    fd = 0;
                    tf_arembourser_usd.setText("0.0");
                    tf_arembourser_cdf.setText("0.0");
                    vente4save.setMontantCdf(0);
                    vente4save.setMontantUsd(0);
                } else if (!newValue.isEmpty() && tf_nominal_recu_usd.getText().isEmpty()) {
                    double restCdf = new BigDecimal(cdf - in_cdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if (restCdf >= 0) {
                        txt_eval_sum_usd.setText(String.valueOf(restUsd));
                        txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                        dt = restUsd;
                        fd = 0;
                        ff = in_cdf;
                        tf_arembourser_cdf.setText("");
                        tf_arembourser_usd.setText("");
                    } else {
                        double retour = Math.abs(restCdf);
                        fd = 0;
                        dt = 0;
                        ff = in_cdf - retour;
                        txt_eval_sum_usd.setText("0.0");
                        txt_eval_sum_cdf.setText("0.0");
                        tf_arembourser_cdf.setText("" + retour);
                        tf_arembourser_usd.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    vente4save.setMontantUsd(0);
                    vente4save.setMontantCdf(ff);
                } else if (newValue.isEmpty() && !tf_nominal_recu_usd.getText().toString().isEmpty()) {
                    double in_usd = Double.parseDouble(tf_nominal_recu_usd.getText().toString());
                    double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double restCdf = new BigDecimal(restUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if (restUsd >= 0) {
                        txt_eval_sum_usd.setText(String.valueOf(restUsd));
                        txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                        dt = restUsd;
                        fd = in_usd;
                        ff = 0;
                        tf_arembourser_cdf.setText("");
                        tf_arembourser_usd.setText("");
                    } else {
                        double retour = Math.abs(restUsd);
                        fd = in_usd - retour;
                        dt = 0;
                        ff = 0;
                        txt_eval_sum_usd.setText("0.0");
                        txt_eval_sum_cdf.setText("0.0");
                        tf_arembourser_usd.setText("" + retour);
                        tf_arembourser_cdf.setText("" + new BigDecimal(retour * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    vente4save.setMontantCdf(0);
                    vente4save.setMontantUsd(fd);
                } else {
                    double inUsd = Double.parseDouble(tf_nominal_recu_usd.getText());
                    double converted = new BigDecimal(inUsd * taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double nwInCdf = (in_cdf + converted);
                    double restCdf = new BigDecimal(cdf - nwInCdf).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double restUsd = new BigDecimal(restCdf / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if (restCdf >= 0) {
                        txt_eval_sum_usd.setText(String.valueOf(restUsd));
                        txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                        dt = restUsd;
                        ff = in_cdf;
                        fd = inUsd;
                        tf_arembourser_cdf.setText("0.0");
                        tf_arembourser_usd.setText("0.0");
                    } else {
                        double retour = Math.abs(restCdf);
                        fd = 0;
                        dt = 0;
                        ff = nwInCdf - retour;
                        txt_eval_sum_usd.setText("0.0");
                        txt_eval_sum_cdf.setText("0.0");
                        tf_arembourser_cdf.setText("" + retour);
                        tf_arembourser_usd.setText("" + new BigDecimal(retour / taux2change).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    vente4save.setMontantCdf(ff);
                }
                double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                vente4save.setDeviseDette("USD");
                vente4save.setMontantDette(debt);
                if (debt > 0) {
                    if (tf_nominal_recu_cdf.getText().isEmpty()) {
                        cbx_payment_mode.getSelectionModel().select(3);
                        pane_bill_cash_paid.setVisible(false);
                        txt_bill_cash_paid.setVisible(false);
                    } else {
                        double sin = Double.parseDouble(tf_nominal_recu_cdf.getText());
                        txt_bill_somme_facture.setText(BigDecimal.valueOf(sin / taux2change).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() > tot ? String.valueOf(tot)
                                : String.valueOf(BigDecimal.valueOf(sin / taux2change).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
                        cbx_payment_mode.getSelectionModel().select(4);
                        pane_bill_cash_paid.setVisible(true);
                        txt_bill_cash_paid.setVisible(true);
                    }
                    dpk_echeance_debt.setDisable(false);
                    txt_bill_somme_credit.setText(String.valueOf(debt));
                    pane_bill_sum_credit.setVisible(true);
                    txt_lbl_credit.setVisible(true);
                } else {
                    dpk_echeance_debt.setDisable(true);
                    txt_bill_somme_facture.setText(String.valueOf(tot));
                    cbx_payment_mode.getSelectionModel().select(0);

                    pane_bill_sum_credit.setVisible(false);
                    txt_lbl_credit.setVisible(false);
                }
            }
        });

        String reference;
        switch (count_logic) {
            case 1: {
                String leo = Constants.DATE_ONLY_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(leo)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", leo);
                    if (tbil == -100) {
                        compteur = 1;
                    }
                }
                reference = String.format("%06d", compteur);
                break;
            }
            case 2: {
                String mois = Constants.YEAR_AND_MONTH_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(mois)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", mois);
                    if (tbil == -100) {
                        compteur = 1;
                    }
                }
                reference = String.format("%06d", compteur);
                break;
            }
            case 3: {
                String mois = Constants.YEAR_ONLY_FORMAT.format(new Date());
                String conu = pref.get("_time_bill", "-1");
                if (conu.equals(mois)) {
                    compteur = pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        compteur++;
                    }
                } else {
                    pref.put("_time_bill", mois);
                    if (tbil == -100) {
                        compteur = 1;
                    }
                }       //            pref.putInt("_bill_counter_", compteur);
                reference = String.format("%06d", compteur);
                break;
            }
            case 4:
                compteur = pref.getInt("_bill_counter_", 0);
                if (tbil == -100) {
                    compteur++;
                }   //            pref.putInt("_bill_counter_", compteur);
                reference = String.format("%08d", compteur);
                break;
            default:
                reference = String.valueOf(ref);
                break;
        }
        txt_reference_vente.setText((invoiceId == null) ? "#" + reference : "#" + invoiceId.getReference());
        vente4save.setReference(reference);
        txt_bill_num_facture.setText((invoiceId == null) ? "#" + reference : "#" + invoiceId.getReference());

        user = pref.get("operator", "User");
        txt_bill_user.setText("Agent : " + user);
        vente4save.setPayment(Constants.PAYMENT_CASH);

        chbx_print_receipt.setSelected(print);
        chbx_print_receipt.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                pref.putBoolean("print", newValue);
            }
        });
        configBillTable();
        cbx_payment_mode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.contains("CAISSE")) {
                    vente4save.setPayment(Constants.PAYMENT_CASH);
                    dpk_echeance_debt.setDisable(true);
                    if (!txt_eval_sum_usd.getText().equals("0.0") && !txt_eval_sum_cdf.getText().equals("0.0")) {
                        txt_reduction.setVisible(true);
                        double dette = Double.parseDouble(txt_eval_sum_usd.getText());
                        double pred = BigDecimal.valueOf((dette / tot) * 100).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                        txt_lbl_credit.setText("Réduction : (" + pred + "%)");
                        txt_reduction.setText("Réduction : (" + pred + "%)");
                        vente4save.setMontantDette(0d);

                    }
                } else if (newValue.contains("BANQUE")) {
                    vente4save.setPayment(Constants.PAYEMENT_BANQUE);
                    dpk_echeance_debt.setDisable(true);
                    if (!txt_eval_sum_usd.getText().equals("0.0") && !txt_eval_sum_cdf.getText().equals("0.0")) {
                        txt_reduction.setVisible(true);
                        double dette = Double.parseDouble(txt_eval_sum_usd.getText());
                        double pred = BigDecimal.valueOf((dette / tot) * 100).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                        txt_lbl_credit.setText("Réduction : (" + pred + "%)");
                        txt_reduction.setText("Réduction : (" + pred + "%)");
                        vente4save.setMontantDette(0d);
                    }
                } else if (newValue.equals("CREDIT")) {
                    vente4save.setPayment(Constants.PAYEMENT_CREDIT);
                    dpk_echeance_debt.setDisable(false);
                    txt_reduction.setVisible(false);
                    txt_lbl_credit.setText("Reste à payer : ");
                    double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                    vente4save.setMontantDette(debt);
                } else if (newValue.equals("MOBILE MONEY")) {
                    dpk_echeance_debt.setDisable(true);
                    vente4save.setPayment(TypeTraisorerie.ELECTRONIQUE.name());
                    if (!txt_eval_sum_usd.getText().equals("0.0") && !txt_eval_sum_cdf.getText().equals("0.0")) {
                        txt_reduction.setVisible(true);
                        double dette = Double.parseDouble(txt_eval_sum_usd.getText());
                        double pred = BigDecimal.valueOf((dette / tot) * 100).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                        txt_lbl_credit.setText("Réduction : (" + pred + "%)");
                        txt_reduction.setText("Réduction : (" + pred + "%)");
                        vente4save.setMontantDette(0d);
                    }
                } else if (newValue.equals("CREDIT+CASH")) {
                    vente4save.setPayment(Constants.PAYMENT_CREDIT_CASH);
                    dpk_echeance_debt.setDisable(false);
                    txt_reduction.setVisible(false);
                    txt_lbl_credit.setText("Reste à payer : ");
                    double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                    vente4save.setMontantDette(debt);
                }

            }
        });

        tf_phone_client.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                txt_nom_client1.setText("Tel : " + (newValue.isEmpty() ? "..." : newValue.length() < 7 ? "NA" : newValue));
            }
        });
        cliname.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                txt_nom_client.setText("Client : " + (newValue.isEmpty() ? "..." : newValue));
            }
        });
        cbx_clients.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Client>() {
                    @Override
                    public void changed(ObservableValue<? extends Client> observable, Client oldValue, Client newValue) {
                        if (newValue != null) {
                            client = newValue;
                            cliname.setText(client.getNomClient());
                            tf_phone_client.setText(client.getPhone().length() > 7 ? client.getPhone() : "");
                            typecli = client.getTypeClient();
                            if (client.getTypeClient().equalsIgnoreCase("#3")) {
                                tflibelle.setPromptText("Entrer le numero de bon");
                                tf_nominal_recu_cdf.clear();
                                tf_nominal_recu_usd.clear();
                            }
                        }
                    }
                });

        if (lignes == null) {
            return;
        }
        venteItems.clear();
        venteItems.addAll(lignes);
        tbl_bill_products.setItems(FXCollections.observableArrayList(lignes));
        tbl_bill_products.setFixedCellSize(25);
        tbl_bill_products.prefHeightProperty().bind(tbl_bill_products.fixedCellSizeProperty().multiply(Bindings.size(tbl_bill_products.getItems()).add(1.01)));
        tbl_bill_products.minHeightProperty().bind(tbl_bill_products.prefHeightProperty());
        tbl_bill_products.maxHeightProperty().bind(tbl_bill_products.prefHeightProperty());
        if (invoiceId == null) {
            txt_bill_date_vente.setText(tools.Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date()));
        } else {
            txt_bill_date_vente.setText(tools.Constants.DATE_HEURE_USER_READABLE_FORMAT.format(invoiceId.getDateVente()));
        }
        txt_bill_num_facture.setText("Facture #" + ((invoiceId == null) ? vente4save.getReference() : invoiceId.getReference()));
        pane_invoiced.prefHeightProperty().bind(tbl_bill_products.maxHeightProperty().add(Bindings.size(tbl_bill_products.getItems()).add(300)));
        pane_invoiced.minHeightProperty().bind(pane_invoiced.prefHeightProperty());
        pane_invoiced.maxHeightProperty().bind(pane_invoiced.prefHeightProperty());
        billbed.prefHeightProperty().bind(pane_invoiced.maxHeightProperty().add(89));
        billbed.minHeightProperty().bind(billbed.prefHeightProperty());
        billbed.maxHeightProperty().bind(billbed.prefHeightProperty());
        MainUI.setPattern(dpk_echeance_debt);
        cbx_clients.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client object) {
                return object == null ? null : object.getNomClient() + " " + (object.getPhone() == null ? "" : (object.getPhone().length() < 8 ? "" : object.getPhone()));
            }

            @Override
            public Client fromString(String string) {
                return cbx_clients.getItems()
                        .stream()
                        .filter(v -> (v.getNomClient() + " " + v.getPhone())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        cbx_comptes.setConverter(new StringConverter<CompteTresor>() {
            @Override
            public String toString(CompteTresor object) {
                return object == null ? null : object.getTypeCompte() + " " + object.getBankName() + " " + object.getNumeroCompte();
            }

            @Override
            public CompteTresor fromString(String string) {
                return cbx_comptes.getItems()
                        .stream()
                        .filter(object -> (object.getTypeCompte() + " " + object.getBankName() + " " + object.getNumeroCompte())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_comptes.getSelectionModel().select(0);
        cbx_comptes.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<CompteTresor>() {
                    @Override
                    public void changed(ObservableValue<? extends CompteTresor> observable, CompteTresor oldValue, CompteTresor newValue) {
                        if (newValue != null) {
                            choosenComptTr = newValue;
                        }
                    }
                });
        choosenComptTr = cbx_comptes.getValue();

    }

    private List<LigneVente> sortByPriceLength(List<LigneVente> lvx) {
        LigneVente[] lvs = ligneVenteToArray(lvx);

        for (int i = 0; i < lvs.length; i++) {
            LigneVente tmp;
            String p1 = String.valueOf(lvs[i].getPrixUnit());
            for (int x = 0; x < lvs.length; x++) {
                String p2 = String.valueOf(lvs[x].getPrixUnit());
                if (p1.length() > p2.length()) {
                    tmp = lvs[i];

                    lvs[i] = lvs[x];
                    lvs[x] = tmp;
                }

            }
        }

        return Arrays.asList(lvs);
    }

    private double sumFact(List<LigneVente> lignes, String dev) {
        if (lignes == null) {
            return 0;
        }
        double d = 0;
        for (LigneVente l : lignes) {
            if (dev.equals("USD")) {
                d += l.getMontantUsd();
            } else {
                d += l.getMontantCdf();
            }
        }
        return d;
    }

    public void configBillTable() {
        col_bill_designation.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente r = param.getValue();
            Produit pr = ProduitDelegate.findProduit(r.getProductId().getUid());
            return new SimpleStringProperty(pr.getNomProduit() + " " + (pr.getMarque() == null ? "" : pr.getMarque()) + " "
                    + "" + (pr.getModele() == null ? "" : pr.getModele()) + " " + (pr.getTaille() == null ? "" : pr.getTaille()) + " " + (pr.getCouleur() == null ? "" : pr.getCouleur()));
        });
        col_bill_qte.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente r = param.getValue();
            Mesure m = r.getMesureId();
            Mesure mzr = MesureDelegate.findMesure(m.getUid());
            return new SimpleStringProperty(r.getQuantite() + " " + mzr.getDescription());
        });
        col_bill_pu.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, Number> param) -> {
            LigneVente r = param.getValue();
            return new SimpleDoubleProperty(r.getPrixUnit());
        });
        col_bill_prix_unit.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, Number> param) -> {
            LigneVente r = param.getValue();
            return new SimpleDoubleProperty(r.getMontantUsd());
        });
    }

    @FXML
    public void printInvoice(Event e) {
        Vente vx = VenteDelegate.findVente(vente4save.getUid());

        if (vx == null && copies == 1) {
            MainUI.notify(null, "Erreur", "Impossible d'imprimer une vente non enregistrée", 4, "error");
            return;
        }
        if (chbx_print_thermal.isSelected()) {
            print();
        } else {
            printWithThermal(defaultPrinter.getName());
        }
    }

    private void print() {
//        PrinterJob pj = PrinterJob.createPrinterJob(defaultPrinter);
//        if (pj == null) {
//            return;
//        }
//
//        Window s = pane_invoiced.getScene().getWindow();
//        pj.showPageSetupDialog(s);
//        boolean proceed = pj.showPrintDialog((Stage) s);
//        if (proceed) {
//            printBill(pj, pane_invoiced);
//        }
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> {
            createPdfBill(entreprise, vente4save, client);
        });
        exec.shutdown();

    }

    private void printBills(PrinterJob pj, Node node) {
        txt_print_status.textProperty().bind(pj.jobStatusProperty().asString());
        boolean pr = pj.printPage(node);
        if (pr) {
            pj.endJob();
        }

    }

    private void printWithThermal(String printerName) {
        PrinterOutputStream pos = null;
        try {
            if (printerName == null) {
                return;
            }
            PrintService ps = PrinterOutputStream.getPrintServiceByName(printerName);
            pos = new PrinterOutputStream(ps);
            EscPos printer = new EscPos(pos);
            printer.setCharacterCodeTable(EscPos.CharacterCodeTable.CP863_Canadian_French);
            Style title = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(title_s == 1 ? Style.FontSize._1 : title_s == 2 ? Style.FontSize._2 : Style.FontSize._3,
                    title_s == 1 ? Style.FontSize._1 : title_s == 2 ? Style.FontSize._2 : Style.FontSize._3);
            Style identite = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(identite_s == 1 ? Style.FontSize._1 : identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3,
                    identite_s == 1 ? Style.FontSize._1 : identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3);
            Style ephone = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setFontSize(Style.FontSize._1, Style.FontSize._1);
            Style client = new Style(printer.getStyle()).setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);
            Style bold = new Style(printer.getStyle()).setJustification(EscPosConst.Justification.Left_Default).setBold(true);
            Style gras = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Right)
                    .setBold(true);
            Style right = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Right);
            Style left = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Left_Default);
            Style centerbold = new Style().setJustification(EscPosConst.Justification.Center).setBold(true);
            if (f != null) {
                RasterBitImageWrapper imgWrapper = new RasterBitImageWrapper();
                imgWrapper.setJustification(EscPosConst.Justification.Center);
                printer.feed(1);
                BufferedImage bimg = ImageIO.read(f);
                Bitonal bitonal = new BitonalThreshold(100);
                EscPosImage posimg = new EscPosImage(new CoffeeImageImpl(bimg), bitonal);

                try {
                    printer.write(imgWrapper, posimg);
                } catch (Exception e) {
                    MainUI.notify(null, "Attention", "Veuillez mettre un bon logo (125X125px) au moins, pour votre facture", 3, "warning");
                }

            }

            printer.feed(1);
            printer.writeLF(title, entreprise.getNomEntreprise() == null ? entrepName : entreprise.getNomEntreprise());
            String idnat = entreprise.getIdNat() == null ? idNat : entreprise.getIdNat();
            String impot = entreprise.getNumeroImpot() == null ? nif : entreprise.getNumeroImpot();
            String phones = entreprise.getPhones() == null ? phonez : entreprise.getPhones();
            String stateId = "RCCM." + entreprise.getIdentification() + " " + (idnat == null ? "" : "ID NAT." + idnat) + (impot == null ? "" : " NIF." + impot
                    + "\nAdresse : " + entreprise.getAdresse() + "\n" + (phones == null || phones.equals("-") ? "" : "Tel :" + phones));
            printer.writeLF(centerbold, stateId);
            if (entreprise.getWebsite() != null) {
                printer.writeLF(identite, entreprise.getWebsite());
            }

            printer.writeLF(right, " Facture N.: " + vente4save.getReference());
            Date dv = vente4save.getDateVente();
            printer.writeLF(right, dv == null ? Constants.DATE_HEURE_FORMAT.format(new Date()) : Constants.DATE_HEURE_FORMAT.format(dv));

            printer.write("Client : ");
            printer.writeLF(client, this.client != null ? ((this.client.getNomClient().equalsIgnoreCase("Anonyme")
                    || this.client.getNomClient().equalsIgnoreCase("Unknown"))
                    ? (this.client.getPhone().length() < 10 ? "Anonyme" : this.client.getPhone()) : this.client.getNomClient())
                    : !tf_phone_client.getText().isEmpty() || !cliname.getText().isEmpty() ? cliname.getText() + ", " + tf_phone_client.getText() : "Anonyme");
            printer.writeLF(repeatChar("-", 48));
            printer.writeLF(left, "Designation   ");
            printer.writeLF(right, "Quantite    P.U         P.TCDF      P.TUSD      ");
            printer.writeLF(repeatChar("-", 48));
            //List<LigneVente> lignes = venteItems;
            int d = 0;
            for (LigneVente ligne : venteItems) {
                Produit pdx = ligne.getProductId();
                Produit pd = ProduitDelegate.findProduit(pdx.getUid());
                String l2 = ligne.getQuantite() + " " + ligne.getMesureId().getDescription()
                        + " " + ligne.getPrixUnit() + "$ ou " + (ligne.getPrixUnit() * taux2change) + "Fc " + ligne.getMontantUsd() + " USD ou " + (ligne.getMontantUsd() * taux2change) + " CDF";
                printer.writeLF(bold, pd.getNomProduit() + " " + (pref.getBoolean("print_mark", true) ? (pd.getMarque() == null ? "" : pd.getMarque()) : "") + " " + (pref.getBoolean("print_modele", true) ? (pd.getModele() == null ? "" : pd.getModele()) : "") + " " + (pref.getBoolean("print_tail", true) ? ((pd.getTaille() == null) ? "" : pd.getTaille()) : ""));

                printer.write(left, ((int) (ligne.getQuantite())) + " ");
                printer.write(left, ligne.getMesureId().getDescription() + " ");

                int dif = 48 - l2.length();
                int g = d - (ligne.getPrixUnit() + "$ ").length();
                if (g <= 0) {
                    printer.write(left, repeatChar(" ", dif));
                    printer.write(right, (ligne.getPrixUnit() * taux2change) + "Fc ");
                } else {
                    printer.write(left, repeatChar(" ", dif - g));
                    printer.write(right, (ligne.getPrixUnit() * taux2change) + "Fc " + repeatChar(" ", g));
                }
                d = (ligne.getPrixUnit() + "$ ").length();
                printer.writeLF(right, (ligne.getMontantUsd() * taux2change) + "CDF " + (pref.getBoolean("print_total_usd", true) ? ("ou " + ligne.getMontantUsd() + " USD") : ""));

            }
            String dev = pref.get("mainCur", "USD");
            double total;
            double actuel;
            double dette;
            double pred;
            if (dev.equals("USD")) {
                total = sumFact(venteItems, dev);
                actuel = vente4save.getMontantUsd() + (vente4save.getMontantCdf() / taux2change);
                dette = Double.parseDouble(txt_eval_sum_usd.getText());
            } else {
                total = sumFact(venteItems, dev);
                actuel = vente4save.getMontantCdf() + (vente4save.getMontantUsd() * taux2change);
                dette = Double.parseDouble(txt_eval_sum_cdf.getText());
            }

            try {
                pred = BigDecimal.valueOf(((dette / total) * 100)).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
            } catch (java.lang.NumberFormatException e) {
                pred = 0;
            }

            printer.writeLF("-".repeat(48));
            printer.writeLF(gras, "Total cash : " + BigDecimal.valueOf((total * taux2change)).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " CDF ou " + BigDecimal.valueOf(total).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " USD");
            if (dette > 0 && !cbx_payment_mode.getValue().contains("CREDIT")) {
                if (dev.equals("USD")) {
                    printer.writeLF(gras, "Reduction (" + pred + "%): -" + BigDecimal.valueOf(dette).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " USD");
                    printer.writeLF(gras, "Total cash : " + BigDecimal.valueOf((actuel * taux2change)).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " CDF ou " + BigDecimal.valueOf(actuel).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " USD");
                } else {
                    printer.writeLF(gras, "Reduction (" + pred + "%): -" + BigDecimal.valueOf(dette).setScale(3, RoundingMode.HALF_EVEN).doubleValue() + " CDF");
                    printer.writeLF(gras, "Total cash : " + (int) (BigDecimal.valueOf(actuel).setScale(3, RoundingMode.HALF_EVEN).doubleValue()) + " CDF ");
                }
            } else {
                if (dette > 0) {
                    if (dev.equals("USD")) {
                        printer.writeLF(gras, "Reste a payer: " + (int) (BigDecimal.valueOf((dette * taux2change)).setScale(0, RoundingMode.HALF_EVEN).doubleValue()) + " CDF  ou " + dette + " USD");
                        if (actuel > 0) {
                            printer.writeLF(gras, "Total cash : " + (int) (BigDecimal.valueOf((actuel * taux2change)).setScale(0, RoundingMode.HALF_EVEN).doubleValue()) + " CDF  ou " + actuel + " USD");
                        }
                    } else {
                        printer.writeLF(gras, "Reste a payer: " + dette + " CDF  ou " + BigDecimal.valueOf(dette / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
                        if (actuel > 0) {
                            printer.writeLF(gras, "Total cash : " + actuel + " CDF  ou " + BigDecimal.valueOf(actuel / taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " USD");
                        }
                    }
                }
            }
            printer.writeLF(repeatChar("-", 48));
            printer.writeLF("Operateur: " + user);
            printer.feed(1);
            boolean isavert = pref.getBoolean("averti", true);
            if (isavert) {
                String text = "CHER CLIENT S.V.P,VEUILLEZ VERIFIER VOS PRODUITS A LA RECEPTION PAS DES RECLAMATIONS APRES";
                printer.writeLF(centerbold, pref.get("ads_mesg", text));
            }
            printer.writeLF(ephone, messageForCustomer);
            printer.feed(1);
            printer.writeLF(identite, "Telecharger Kazisafe via Qr-Code suivant");
            QRCode qrcode = new QRCode();
            printer.feed(2);
            qrcode.setSize(2);
            qrcode.setJustification(EscPosConst.Justification.Center);
            printer.write(qrcode, "https://www.endeleya.com");
            printer.feed(4);
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

    private String repeatChar(String car, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(car);
        }
        return sb.toString();
    }
    Traisorerie svincss;

    @FXML
    public void saveVente(Event et) {
        if (!cbx_payment_mode.getValue().equals("CREDIT") && choosenComptTr == null) {

            MainUI.notify(null, "Erreur", "Veuillez selectionner le compte de tresorerie puis continuer", 4, "error");
            return;
        }
        List<Vente> vxs = VenteDelegate.findByRef(vente4save.getReference(), new Date());
        if (!vxs.isEmpty()) {
            if (!vxs.get(0).getObservation().equals("Drafted")) {
                MainUI.notify(null, "Erreur", "La vente ayant le même numéro de facture existe déjà", 4, "error");
                return;
            }
        }
        if (typecli != null) {
            if (typecli.equals("#3")) {
                if (tflibelle.getText().isEmpty()) {
                    MainUI.notify(null, "Erreur", "Veuillez entrer le numero de bon de l'abonne puis reesayer", 4, "error");
                    return;
                }
            }
        }
        String type = pref.get("type-sub", " ");
        String pm = vente4save.getPayment();
        if (tf_nominal_recu_usd.getText().isEmpty() && tf_nominal_recu_cdf.getText().isEmpty()) {
            dpk_echeance_debt.setDisable(false);
            cbx_payment_mode.getSelectionModel().select(3);
        }
        if (dpk_echeance_debt.getValue() == null && cbx_payment_mode.getValue().contains("CREDIT")
                && tf_nominal_recu_usd.getText().isEmpty() && tf_nominal_recu_cdf.getText().isEmpty()) {
            dpk_echeance_debt.setDisable(false);
            MainUI.notify(null, "Erreur", "Entrer la date valide de l'echeance de la dette", 4, "erreur");
            return;
        }
        vente4save.setDeviseDette("USD");
        if (type.equalsIgnoreCase("Gold") || type.equalsIgnoreCase("Super Gold")) {
            vente4save.setObservation("YES");
        } else {
            vente4save.setObservation("NON");
        }
        if (cbx_payment_mode.getValue().equals("CREDIT+CASH")) {
            if (dpk_echeance_debt.getValue() == null || tf_phone_client.getText().isEmpty() || cliname.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "La date de l'échéance, le téléphone du client et son nom sont obligatoires", 4, "error");
                return;
            }
            double debt = Double.parseDouble(txt_eval_sum_usd.getText());
            vente4save.setDeviseDette("USD");
            vente4save.setMontantDette(debt);
            vente4save.setPayment(Constants.PAYMENT_CREDIT_CASH);
            vente4save.setEcheance(tools.Constants.Datetime.toUtilDate(dpk_echeance_debt.getValue()));
        } else if (cbx_payment_mode.getValue().equals("CREDIT")) {
            if (dpk_echeance_debt.getValue() == null || tf_phone_client.getText().isEmpty() || cliname.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "La date de l'échéance, le téléphone du client et son nom sont obligatoires", 4, "error");
                return;
            }
            double debt = Double.parseDouble(txt_eval_sum_usd.getText());
            vente4save.setDeviseDette("USD");
            vente4save.setMontantDette(debt);
            vente4save.setPayment(Constants.PAYEMENT_CREDIT);
            vente4save.setMontantCdf(0d);
            vente4save.setMontantUsd(0d);
            vente4save.setEcheance(tools.Constants.Datetime.toUtilDate(dpk_echeance_debt.getValue()));
        }
        if (!dpk_echeance_debt.isDisabled()) {
            if (tf_phone_client.getText().isEmpty() || !StringUtils.isNumeric(tf_phone_client.getText())) {
                MainUI.notify(null, "Erreur", "Le numéro de téléphone du client est obligatoire,"
                        + "c'est pour lui rapeller par SMS de vous payer dans le délai", 4, "error");
                return;
            }
        }

        if (!tf_phone_client.getText().isEmpty() || !cliname.getText().isEmpty()) {
            String phon = tf_phone_client.getText().isEmpty() ? String.valueOf(((int) (Math.random() * 10000))) : tf_phone_client.getText();
            String namecli = cliname.getText().isEmpty() ? "Unknown" : cliname.getText();
            List<Client> clts = ClientDelegate.findClientByPhone(phon.contains("\\+243") ? phon.replaceAll("\\+243", "0") : phon);
            if (clts.isEmpty()) {
                if (StringUtils.isNumeric(phon)) {
                    client = new Client(DataId.generate());
                    client.setPhone(phon.contains("\\+243") ? phon.replaceAll("\\+243", "0") : phon);
                    client.setNomClient(namecli);
                    client.setAdresse("Unknown");
                    client.setEmail("Unknown");
                    client.setTypeClient("Consommateur");
                }
            }else{
                client= clts.get(0);
            }
        }
        vente4save.setDateVente(new Date());
        vente4save.setRegion(region);
        vente4save.setLibelle(tflibelle.getText().isEmpty() ? "Vente - Ref  " + vente4save.getReference() : tflibelle.getText());
        vente4save.setLatitude(0d);
        vente4save.setLongitude(0d);
        Client clt = ClientDelegate.findClient(client.getUid());
        if (clt == null) {
            Client sc = ClientDelegate.saveClient(client);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(sc, Constants.ACTION_CREATE, Tables.CLIENT);
                    });
        } else {
            client = clt;
        }
        saveClientByHttp(client);
        if (vente4save != null) {
            vente4save.setClientId(client);
            // if (!cbx_payment_mode.getValue().equals("CREDIT+CASH")) {
            if (!clients.contains(client) && save2favorite.isSelected()) {
                clients.add(client);
            }
            Vente vtx = VenteDelegate.findVente(vente4save.getUid());
            List<LigneVente> tosave = new ArrayList<>();
            if (vtx == null) {
//                Vente vent = VenteDelegate.saveVente(vente4save);

                for (LigneVente i : venteItems) {
                    i.setClientId(client.getPhone());
//                    i.setReference(vente4save);
                    LigneVenteDelegate.saveLigneVente(i,vente4save);
//                    tosave.add(i);
                }
                
               
//                Vente vent = VenteDelegate.saveVente(vente4save);
            } else {
                List<LigneVente> lvs = LigneVenteDelegate.findByReference(vente4save.getUid());
                System.out.println("Venty " + vente4save.getLibelle());
                Vente vent = VenteDelegate.updateVente(vente4save);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(vent, Constants.ACTION_UPDATE, Tables.VENTE);
                        });
                if (lvs.size() < venteItems.size()) {
                    //insert ventitems
                    for (LigneVente i : venteItems) {
                        LigneVente lv = LigneVenteDelegate.findLigneVente(i.getUid());// db.findByUid(LigneVente.class, i.getUid());
                        if (lv == null) {
//                            i.setReference(vente4save);
                            i.setClientId(client.getPhone());
                            LigneVenteDelegate.saveLigneVente(i,vente4save);
//                            LigneVenteDelegate.saveLigneVente(i); //db.insertAndSync(i);
//                            Executors.newCachedThreadPool()
//                                    .submit(() -> {
//                                        Util.sync(i, Constants.ACTION_CREATE, Tables.LIGNEVENTE);
//                                    });
                        }
                    }
                } else if (lvs.size() > venteItems.size()) {
                    for (LigneVente lv : lvs) {
                        LigneVente fl = findLv(venteItems, lv.getUid());
                        if (fl == null) {
                            LigneVenteDelegate.deleteLigneVente(lv);
                            Executors.newCachedThreadPool()
                                    .submit(() -> {
                                        Util.sync(lv, Constants.ACTION_DELETE, Tables.LIGNEVENTE);
                                    });
                            // db.delete(lv);
                        }
                    }
                }

            }
                vente4save.setLigneVenteList(venteItems);
//                saveVenteByHttp(vente4save);
//      
            MainUI.notify(null, "Info", "Vente enregistree avec succes", 4, "info");
            pref.putInt("_bill_counter_", compteur);
            pref.putInt("tranzit_bill_", -100);
            if (!cbx_payment_mode.getValue().equals("CREDIT")) {
                Traisorerie trzr = new Traisorerie();
                trzr.setDate(new Date());
                trzr.setLibelle("Paiement vente " + vente4save.getReference());
                trzr.setMontantCdf(vente4save.getMontantCdf());
                trzr.setMontantUsd(vente4save.getMontantUsd());
                trzr.setMouvement(Mouvment.AUGMENTATION.name());
                trzr.setReference(vente4save.getReference());
                trzr.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
                trzr.setRegion(region);
                trzr.setTresorId(choosenComptTr);
                svincss = TraisorerieDelegate.saveTraisorerie(trzr);//db.insertAndSync(trzr);
//                saveTresorerieByHttp(trzr);
            }
             saveVenteByHttp(vente4save,choosenComptTr,svincss.getUid());     
             saveLigneVenteByHttp(venteItems);
            if (chbx_print_receipt.isSelected()) {
                if (chbx_print_thermal.isSelected()) {
                    print();
                } else {
                    if (defaultPrinter != null) {
                        for (int i = 0; i < copies; i++) {
                            printWithThermal(defaultPrinter.getName());
                        }
                    }
                }
            }
//            Executors.newCachedThreadPool()
//                    .submit(() -> {
//                        Vente vtxo = VenteDelegate.findVente(vente4save.getUid());
//                        if (vtxo == null) {
//                            Util.sync(vente4save, Constants.ACTION_CREATE, Tables.VENTE);
//                            List<LigneVente> items = LigneVenteDelegate.findByReference(vente4save.getUid());
//                            for (LigneVente item : items) {
//                                Util.sync(item, Constants.ACTION_CREATE, Tables.LIGNEVENTE);
//                            }
//                        } else {
//                            Util.sync(vente4save, Constants.ACTION_UPDATE, Tables.VENTE);
//                            List<LigneVente> lvs = LigneVenteDelegate.findByReference(vente4save.getUid());
//                            if (lvs.size() < venteItems.size()) {
//                                //insert ventitems
//                                for (LigneVente i : venteItems) {
//                                    Util.sync(i, Constants.ACTION_CREATE, Tables.LIGNEVENTE);
//                                }
//                            } else if (lvs.size() > venteItems.size()) {
//                                for (LigneVente lv : lvs) {
//                                    LigneVente fl = findLv(venteItems, lv.getUid());
//                                    if (fl == null) {
//                                        Util.sync(lv, Constants.ACTION_DELETE, Tables.LIGNEVENTE);
//                                    }
//                                }
//                            }
//                        }
//
//                    });
//            Executors.newCachedThreadPool()
//                    .submit(() -> {
//                        List<LigneVente> items = LigneVenteDelegate.findByReference(vente4save.getUid());
//                        for (LigneVente item : items) {
//                            Util.sync(item, Constants.ACTION_CREATE, Tables.LIGNEVENTE);
//                        }
//                    });
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        if (svincss != null) {
                            Util.sync(svincss, Constants.ACTION_CREATE, Tables.TRAISORERIE);
                            System.out.println("Tresorerie saved");
                        }
                    });
            if (type.equalsIgnoreCase("Gold")) {
                double solde = pref.getDouble("sub", 2000);
                solde -= ((((vente4save.getMontantCdf() / taux2change) + (vente4save.getMontantDette() == null ? 0 : vente4save.getMontantDette()) + vente4save.getMontantUsd()) * taux2change) * 0.002);
                pref.putDouble("sub", solde);
            }
            PosController.getInstance().clearCart();
            PosController.getInstance().refreshPos(et);
            venteItems.clear();
            close(et);
        }
    }

    private void createPdfBill(Entreprise entrep, Vente vt, Client ff) {
        if (ff == null) {
            MainUI.notify(null, "Erreur", "Tu peux aussi preciser un client si besoin", 3, "error");
        }

        try {
            PDDocument document = new PDDocument();
            PDPage fPage = new PDPage(PDRectangle.A4);
            document.addPage(fPage);

            int pageW = (int) PDRectangle.A4.getWidth();//fPage.getTrimBox().getWidth();
            int pageH = (int) PDRectangle.A4.getHeight();//fPage.getTrimBox().getHeight();

            PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
            PDFUtils pdf = new PDFUtils(document, contentStream);

            //PDFont normalbold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            // PDFont normal = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
            PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            File f = FileUtils.pointFile(entrep.getUid() + ".png");
            InputStream is;
            if (!f.exists()) {
                is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                f = FileUtils.streamTofile(is);
            }
            java.awt.Color endeleya = new java.awt.Color(68, 206, 245);
            java.awt.Color egray = new java.awt.Color(218, 218, 219);
            PDImageXObject logo = PDImageXObject.createFromFile(f.getPath(), document);
            contentStream.drawImage(logo, pageW - 114, pageH - 114, 84, 84);

            pdf.addTextLine("Facture", 25, pageH - 98, hbold, 40, java.awt.Color.DARK_GRAY);

            contentStream.setStrokingColor(endeleya);
            contentStream.setLineWidth(2);
            contentStream.moveTo(25, 700);
            contentStream.lineTo(pageW - 25, 700);
            contentStream.stroke();

            pdf.addTextLine(entrep.getNomEntreprise(), 25, pageH - 180, hnormal, 18, java.awt.Color.BLACK);
            pdf.addTextLine(new String[]{"Adresse : " + entrep.getAdresse(),
                "RCCM : " + entrep.getIdentification(), entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(), entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot()}, 15, 25, pageH - 192, hnormal, 14, java.awt.Color.BLACK);
            String idf = ff.getPhone();

            pdf.addTextLine(ff.getNomClient(), ((int) (pageW - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "Tel : " + idf) / 1000 * 15 - 92)), pageH - 180, hnormal, 18, java.awt.Color.BLACK);
            pdf.addTextLine(new String[]{"Adresse : " + ff.getAdresse(), idf == null ? ""
                : "RCCM : " + idf,
                "Tel : " + ff.getPhone()}, 15, ((int) (pageW - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "RCCM : " + idf) / 1000 * 15 - 92)), pageH - 192, hnormal, 14, java.awt.Color.BLACK);

            String date = "Date : " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date());
            pdf.addTextLine(new String[]{date,
                "Facture N# : " + vt.getReference()}, 15, ((int) (pageW - hnormal.getStringWidth(date) / 1000 * 15 - 32)), pageH - 260, hnormal, 14, java.awt.Color.BLACK);
            //Tableau items
            int table[] = {55, 100, 230, 65, 90};

            pdf.addTable(table, 30, 25, pageH - 400);
            pdf.setFont(hnormal, 11, java.awt.Color.WHITE);

            pdf.setRightAlignedColumns(new int[]{0, 2});

            pdf.addCell("N#", endeleya);
            pdf.addCell("Quantité", endeleya);
            pdf.addCell("Désignation", endeleya);

            pdf.addCell("P.U.", endeleya);
            pdf.addCell("P. total", endeleya);
            pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
            contentStream.setFont(hnormal, 10);
            int i = 0;
            double somme = 0;
            int ln = 0;
            int lpp = 26;
            String dev = pref.get("mainCur", "USD");
//            double total;
//            double actuel;
//            double dette;
//            double pred;
//            if (dev.equals("USD")) {
//                total = sumFact(venteItems, dev);
//                actuel = vente4save.getMontantUsd() + (vente4save.getMontantCdf() / taux2change);
//                dette = Double.parseDouble(txt_eval_sum_usd.getText());
//            } else {
//                total = sumFact(venteItems, dev);
//                actuel = vente4save.getMontantCdf() + (vente4save.getMontantUsd() * taux2change);
//                dette = Double.parseDouble(txt_eval_sum_cdf.getText());
//            }
            for (LigneVente rupture : venteItems) {
                i++;
                ln++;
                if (i > 13) {
                    if (i == 14 | ln == lpp) {
                        contentStream.close();
                        PDPage fPage2 = new PDPage(PDRectangle.A4);
                        document.addPage(fPage2);
                        contentStream = new PDPageContentStream(document, fPage2);
                        pdf = new PDFUtils(document, contentStream);
                        int tablex[] = {55, 230, 100, 65, 90};
                        pdf.addTable(tablex, 30, 25, pageH - 68);
                        pdf.setFont(hnormal, 10, java.awt.Color.BLACK);

                        pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                        contentStream.setFont(hnormal, 10);
                        if (ln == lpp || i == 14) {
                            ln = 0;
                        }
                    }
//                 
                }

                Produit x = rupture.getProductId();
                pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                pdf.addCell(i + ".", egray);
                pdf.addCell(rupture.getQuantite() + " " + rupture.getMesureId().getDescription(), egray);
                pdf.addCell(x.getNomProduit() + " "
                        + "" + x.getMarque() + " " + x.getModele() + " " + (x.getTaille() == null ? "" : x.getTaille()) + " " + (x.getCouleur() == null ? "" : x.getCouleur()), egray);

                pdf.addCell((rupture.getPrixUnit() * taux2change) + " ", egray);
                double stot = rupture.getQuantite() * rupture.getPrixUnit();
                somme += stot;
                pdf.addCell((BigDecimal.valueOf(stot).setScale(2, RoundingMode.HALF_EVEN).doubleValue() * taux2change) + " FC", egray);

            }

            if (ln == lpp - 1 || ln == 0) {
                contentStream.close();
                PDPage fPage2 = new PDPage(PDRectangle.A4);
                document.addPage(fPage2);
                contentStream = new PDPageContentStream(document, fPage2);
                pdf = new PDFUtils(document, contentStream);
                int tablex[] = {55, 230, 100, 65, 90};
                pdf.addTable(tablex, 30, 25, pageH - 68);
                pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
                pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                contentStream.setFont(hnormal, 10);
            }

            pdf.addCell("", null);
            pdf.addCell("", null);
            pdf.addCell("", null);
            pdf.addCell("", null);
            pdf.addCell("", null);

            pdf.addCell("", null);
            pdf.addCell("", null);
            pdf.addCell("Total", egray);
            pdf.addCell("", egray);
            pdf.addCell(BigDecimal.valueOf(somme).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + " ", egray);
            contentStream.close();

            final File bcmd = FileUtils.pointFile(System.currentTimeMillis() + ".pdf");
            document.save(bcmd);
            document.close();
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(bcmd);
                } catch (IOException ex) {
                    Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        } catch (IOException ex) {
            Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private LigneVente findLv(List<LigneVente> litems, long id) {
        for (LigneVente litem : litems) {
            if (litem.getUid() == id) {
                return litem;
            }
        }
        return null;
    }

    @FXML
    private void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
        PosController.getInstance().choosenVente = null;
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

    private LigneVente[] ligneVenteToArray(List<LigneVente> lvx) {
        LigneVente lx[] = new LigneVente[lvx.size()];
        for (int y = 0; y < lvx.size(); y++) {
            LigneVente lv = lvx.get(y);
            lx[y] = lv;
        }
        return lx;
    }

    @FXML
    private void onRefreshAcounts(MouseEvent evt) {
        comptes.addAll(CompteTresorDelegate.findCompteTresors());
    }
    
    
    private void saveClientByHttp(Client clt){
        kazisafe.saveClient(clt).enqueue(new Callback<Client>() {
            @Override
            public void onResponse(Call<Client> call, Response<Client> rspns) {
                System.out.println("Client "+rspns.message());
              if(rspns.isSuccessful()){
                  System.out.println("Client saved on server");
              }
            }

            @Override
            public void onFailure(Call<Client> call, Throwable thrwbl) {
             thrwbl.printStackTrace();
            }
        });
    }
    
    private void saveVenteByHttp(Vente vente,CompteTresor tresor,String transaction){
        /**
         * @Field("uid") int uid,
            @Field("clientId") String clientId,
            @Field("reference") String reference,
            @Field("libelle") String libelle,
            @Field("observation") String observation,
            @Field("date") String dateVente,
            @Field("paymentMode") String paymentMode,
            @Field("montantCdf") double montantcdf,
            @Field("montantUsd") double montantusd,
            @Field("montantDette") double montantDette,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude,
            @Field("region") String region,
            @Field("compteCaisse") String tresorId,
            @Field("transFinId") String transactionId,
            @Field("smsBill") String smsBill
         */
        kazisafe.syncSale(vente.getUid(),vente.getClientId().getUid(),
                vente.getReference(), vente.getLibelle(), vente.getObservation(),Constants.DATE_HEURE_FORMAT.format(vente.getDateVente()),
                vente.getPayment(), vente.getMontantCdf(), vente.getMontantUsd(),vente.getMontantDette(), vente.getLatitude(),
                vente.getLongitude(), vente.getRegion(), tresor.getUid(), transaction,"Not ready")
                .enqueue(new Callback<Vente>() {
            @Override
            public void onResponse(Call<Vente> call, Response<Vente> rspns) {
              System.out.println("Vente "+rspns.code());
              if(rspns.isSuccessful()){
                  System.out.println("Vente saved on server");
              }
            } 

            @Override
            public void onFailure(Call<Vente> call, Throwable thrwbl) {
               thrwbl.printStackTrace();
            }
        });
    }
    private void saveLigneVenteByHttp(List<LigneVente> lvs){
        kazisafe.saveLigneVente(lvs).enqueue(new Callback<LigneVente>() {
            @Override
            public void onResponse(Call<LigneVente> call, Response<LigneVente> rspns) {
                System.out.println("LigneVente "+rspns.code());
              if(rspns.isSuccessful()){
                  System.out.println("Lignevente saved on server");
              }
            }

            @Override
            public void onFailure(Call<LigneVente> call, Throwable thrwbl) {
             thrwbl.printStackTrace();
            }
        });
    }
    
    private void saveTresorerieByHttp(Traisorerie tr){
        kazisafe.saveTraisorerie(tr).enqueue(new Callback<Traisorerie>() {
            @Override
            public void onResponse(Call<Traisorerie> call, Response<Traisorerie> rspns) {
                System.out.println("Tresorerie "+rspns.message());
              if(rspns.isSuccessful()){
                  System.out.println("Tresorerie saved on server");
              }
            }

            @Override
            public void onFailure(Call<Traisorerie> call, Throwable thrwbl) {
             thrwbl.printStackTrace();
            }
        });
    }
    
    
}
