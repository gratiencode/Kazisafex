/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import data.Abonnement;
import javafx.scene.control.ProgressIndicator;
import data.Category;
import delegates.ClientDelegate;
import delegates.CategoryDelegate;
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
import data.SaleAgregate;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import data.ProduitHelper;
import data.Traisorerie;
import data.Vente;
import data.VenteHelper;
import data.helpers.Mouvment;
import data.helpers.TypeTraisorerie;
import delegates.RepportDelegate;
import delegates.RecquisitionDelegate;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
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
import tools.NetLoockup;
import tools.PriceMaker;
import tools.SaleItemHelper;
import tools.SubscriptionUtil;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import utilities.PDFUtils;

import javax.bluetooth.RemoteDevice;
import javax.print.PrintServiceLookup;
import com.endeleya.kazisafex.tools.BluetoothPrintService;
import com.endeleya.kazisafex.BluetoothPrinterManager;
import com.endeleya.kazisafex.tools.SerialPrintService;
import com.endeleya.kazisafex.SerialPrinterManager;
import com.fazecast.jSerialComm.SerialPort;
import java.util.concurrent.CompletableFuture;
import javafx.beans.property.SimpleListProperty;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class PaymentController implements Initializable {

    @FXML
    private Label txt_eval_sum_usd, txt_bill_num_facture, txt_bill_companyname, txt_comp_adresse, txt_comp_adresse_tel,
            txt_bill_user, txt_lbl_credit, txt_bill_somme_credit;
    @FXML
    private Label txt_eval_sum_cdf, txt_bill_date_vente, txt_bill_somme_facture, txt_bill_contact_entreprise,
            txt_bill_cash_paid, txt_reduction;
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
    private Label captionusd;
    @FXML
    private Label captioncdf, lbl_bt_count;
    @FXML
    private CheckBox chbx_bt_search;
    @FXML
    private ProgressIndicator pgi_bt_search;
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

    private static int MAX_SALE_RETRY = 9;

    private int title_s, identite_s, body_s, line_dashes;
    private static PaymentController instance;
    private Entreprise entreprise;
    private PriceMaker maker;

    String entrepName, idNat, phonez, adresse, email, nif, rccm;
    String region, role;
    int count_logic = 0;
    int copies = 1;
    @FXML
    private Label txt_print_status1;
    @FXML
    private Label txt_ebonus_remained;

    public PaymentController() {
        maker = new PriceMaker();
        instance = this;
    }

    public static PaymentController getInstance() {
        return instance;
    }

    private void initPref() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        if (role == null) {
            role = pref.get("role", "Trader");
        }
        if (region == null) {
            region = pref.get("region", "Goma");
        }
        WIDTH = pref.getInt("paper_width", 48);
    }

    int WIDTH;

    public void printReceipt(String printerName,
            String storeName,
            String rccm,
            String invoiceNumber,
            List<LigneVente> items,
            double amountPaid,
            String customerName,
            String customerPhone,
            String currency,
            double rate) {
        PrinterOutputStream pos = null;
        try {
            if (printerName == null) {
                return;
            }
            PrintService ps = PrinterOutputStream.getPrintServiceByName(printerName);
            pos = new PrinterOutputStream(ps);
            EscPos printer = new EscPos(pos);
            printer.setCharacterCodeTable(EscPos.CharacterCodeTable.CP863_Canadian_French);
            Style title = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(
                    title_s == 1 ? Style.FontSize._1 : title_s == 2 ? Style.FontSize._2 : Style.FontSize._3,
                    title_s == 1 ? Style.FontSize._1 : title_s == 2 ? Style.FontSize._2 : Style.FontSize._3);
            Style identite = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(
                    identite_s == 1 ? Style.FontSize._1 : identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3,
                    identite_s == 1 ? Style.FontSize._1 : identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3);
            Style ephone = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setFontSize(Style.FontSize._1, Style.FontSize._1);
            Style client = new Style(printer.getStyle()).setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);
            // Style bold = new
            // Style(printer.getStyle()).setJustification(EscPosConst.Justification.Left_Default).setBold(true);
            // Style gras = new Style(printer.getStyle())
            // .setJustification(EscPosConst.Justification.Right)
            // .setBold(true);JustificationJustifiJustificationJustificationcation
            Style right = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Right);
            // Style left = new Style(printer.getStyle())
            // .setJustification(EscPosConst.Justification.Left_Default);
            Style centerbold = new Style().setJustification(EscPosConst.Justification.Center).setBold(true);
            // le fichier charge a la ligne 1674 et 534
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
                    MainUI.notify(null, "Attention",
                            "Veuillez mettre un bon logo (125X125px) au moins, pour votre facture", 3, "warning");
                }

            }
            printer.feed(1);
            printer.writeLF(title, entreprise.getNomEntreprise() == null ? entrepName : entreprise.getNomEntreprise());
            String idnat = entreprise.getIdNat() == null ? idNat : entreprise.getIdNat();
            String impot = entreprise.getNumeroImpot() == null ? nif : entreprise.getNumeroImpot();
            String phones = entreprise.getPhones() == null ? phonez : entreprise.getPhones();
            String stateId = "RCCM." + entreprise.getIdentification() + " " + (idnat == null ? "" : "ID NAT." + idnat)
                    + (impot == null ? ""
                            : " NIF." + impot
                                    + "\nAdresse : " + entreprise.getAdresse() + "\n"
                                    + (phones == null || phones.equals("-") ? "" : "Tel :" + phones));
            printer.writeLF(centerbold, stateId);
            if (entreprise.getWebsite() != null) {
                printer.writeLF(identite, entreprise.getWebsite());
            }

            printer.writeLF(right, " Facture N.: " + vente4save.getReference());
            LocalDateTime dv = vente4save.getDateVente();
            printer.writeLF(right, dv == null ? LocalDateTime.now().toString() : dv.toString());

            printer.write("Client : ");
            printer.writeLF(client, cliname.getText().isBlank() ? "Anonyme" : cliname.getText());
            System.out.println("-".repeat(WIDTH));
            boolean isUSD = "USD".equalsIgnoreCase(currency);
            DecimalFormat moneyFormat = isUSD ? new DecimalFormat("0.00") : new DecimalFormat("#,##0.00");
            printer.writeLF("-".repeat(WIDTH));
            double grandTotal = 0;
            for (LigneVente item : items) {
                Produit p = ProduitDelegate.findProduit(item.getProductId().getUid());
                List<String> nameLines = wrapText(
                        p.getNomProduit() + " " + p.getModele() + " " + p.getTaille() + " " + p.getMarque(), WIDTH);
                for (int i = 0; i < nameLines.size(); i++) {
                    printer.writeLF(nameLines.get(i));
                    if (i == nameLines.size() - 1) {
                        double unitPrice = item.getPrixUnit();
                        double lineTotal = item.getMontantUsd();
                        Mesure m = item.getMesureId();
                        String priceStr = moneyFormat.format(unitPrice);
                        String qtyStr = "x" + item.getQuantite() + " " + m.getDescription();
                        String totalStr = moneyFormat.format(lineTotal);

                        int leftLen = WIDTH - (priceStr.length() + qtyStr.length() + totalStr.length() + 2);
                        String leftPad = " ".repeat(Math.max(0, leftLen));
                        String line = leftPad + priceStr + " " + qtyStr + " " + totalStr;
                        System.out.println(line);
                        printer.writeLF(line);

                    }
                }
                grandTotal += item.getMontantUsd();
            }

            double convertedTotal = grandTotal;
            double convertedPaid = amountPaid;
            double convertedReste = convertedTotal - convertedPaid;

            printer.writeLF("-".repeat(WIDTH));
            printer.writeLF(printLine("TOTAL:", convertedTotal, currency, moneyFormat));
            if (isUSD) {
                printer.writeLF(printLine("PAYÉ:", convertedPaid, currency, moneyFormat));

                if (convertedReste > 0.01) {
                    printer.writeLF(printLine("RESTE À PAYER:", convertedReste, currency, moneyFormat));
                } else if (convertedReste < -0.01) {
                    printer.writeLF(printLine("TROP PAYÉ:", -convertedReste, currency, moneyFormat));
                }
            }
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

        } catch (Exception exception) {

        }
        // boolean isUSD = "USD".equalsIgnoreCase(currency);
        // DecimalFormat moneyFormat = isUSD ? new DecimalFormat("0.00") : new
        // DecimalFormat("#,##0.00");
        //
        // printCentered(storeName);
        // printCentered("RCCM: " + rccm);
        //
        // String date = LocalDate.now().toString();

        //// System.out.printf("Date: %-15s Facture: %s%n", date, invoiceNumber);
        // printCentered("Date: " + date + " " + invoiceNumber);
        //
        // System.out.println("Client: " + customerName);
        // if (customerPhone != null && !customerPhone.isBlank()) {
        // System.out.println("Téléphone: " + customerPhone);
        // }
        //
        // System.out.println("-".repeat(WIDTH));
        //
        // double grandTotal = 0;
        // for (LigneVente item : items) {
        // Produit p = ProduitDelegate.findProduit(item.getProductId().getUid());
        // List<String> nameLines = wrapText(p.getNomProduit() + " " + p.getModele() + "
        // " + p.getTaille() + " " + p.getMarque(), WIDTH);
        // for (int i = 0; i < nameLines.size(); i++) {
        // System.out.println(nameLines.get(i));
        // printer.write(left, " ".repeat(dif));
        // if (i == nameLines.size() - 1) {
        // double unitPrice = isUSD ? item.getPrixUnit() : item.getPrixUnit() * rate;
        // double lineTotal = isUSD ? item.getMontantUsd() : item.getMontantUsd() *
        // rate;
        // Mesure m = item.getMesureId();
        // String priceStr = moneyFormat.format(unitPrice);
        // String qtyStr = "x" + item.getQuantite() + " " + m.getDescription();
        // String totalStr = moneyFormat.format(lineTotal);
        //
        // int leftLen = WIDTH - (priceStr.length() + qtyStr.length() +
        // totalStr.length() + 2);
        // String leftPad = " ".repeat(Math.max(0, leftLen));
        // System.out.println(leftPad + priceStr + " " + qtyStr + " " + totalStr);
        // }
        // }
        // grandTotal += item.getMontantUsd();
        // }
        //
        // double convertedTotal = isUSD ? grandTotal : grandTotal * rate;
        // double convertedPaid = amountPaid;
        // double convertedReste = convertedTotal - convertedPaid;
        //
        // System.out.println("-".repeat(WIDTH));
        // printLine("TOTAL:", convertedTotal, currency, moneyFormat);
        // printLine("PAYÉ:", convertedPaid, currency, moneyFormat);
        //
        // if (convertedReste > 0.01) {
        // printLine("RESTE À PAYER:", convertedReste, currency, moneyFormat);
        // } else if (convertedReste < -0.01) {
        // printLine("TROP PAYÉ:", -convertedReste, currency, moneyFormat);
        // }
        //
        // printCentered("Merci pour votre paiement !");
        // printQrCodeAscii("Facture: " + invoiceNumber + ", Client: " + customerName);
    }

    public static List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        while (text.length() > width) {
            int breakPoint = text.lastIndexOf(' ', width);
            if (breakPoint == -1) {
                breakPoint = width;
            }
            lines.add(text.substring(0, breakPoint));
            text = text.substring(breakPoint).stripLeading();
        }
        lines.add(text);
        return lines;
    }

    public String printCentered(String text) {
        int pad = (WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + text;
    }

    public String printLine(String label, double amount, String currency, DecimalFormat formatter) {
        String amountStr = formatter.format(currency.equals("USD") ? amount : Math.round(amount)) + " " + currency;
        int spaces = WIDTH - label.length() - amountStr.length();
        String line = label + " ".repeat(Math.max(0, spaces)) + amountStr;
        System.out.println(line);
        return line;
    }

    public void printQrCodeAscii(String data) {
        // Simplification ASCII fixe — à remplacer par une vraie lib QR si imprimante
        // graphique
        System.out.println("QRCODE ICI\n" + data);
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initPref();
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux2change = pref.getDouble("taux2change", 2800);
        print = pref.getBoolean("print", true);
        role = pref.get("priv", null);

        region = pref.get("region", "...");
        cbx_payment_mode.setItems(
                FXCollections.observableArrayList(TypeTraisorerie.CAISSE.name(), TypeTraisorerie.BANQUE.name(),
                        "MOBILE MONEY", Mouvment.CREDIT.name(), Mouvment.CREDIT.name() + "+" + Mouvment.CASH.name()));
        cbx_payment_mode.getSelectionModel().selectFirst();

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
        maker.setMainCurrency(pref.get("mainCur", "USD"));
        maker.setTaux(taux2change);
        captioncdf.setText(maker.getInverseCurrencyCode());
        captionusd.setText(maker.getMainCurrency());

        // Discover and register Serial printers on startup
        refreshSerialPrinters();
    }

    public void setEntreprise(Entreprise e) {
        this.entreprise = e;
        if (entreprise == null) {
            return;
        }
        // db = JpaStorage.getInstance();
        client = ClientDelegate.findAnonymousClient();// db.getAnonymousClient();
        txt_bill_companyname.setText(this.entreprise.getNomEntreprise());
        txt_comp_adresse.setText(this.entreprise.getAdresse());
        txt_bill_comp_id.setText("RCCM : " + this.entreprise.getIdentification());
        txt_bill_contact_entreprise.setText(this.entreprise.getEmail());
        String imp = this.entreprise.getNumeroImpot() == null ? "Aucun" : this.entreprise.getNumeroImpot(),
                idnat = this.entreprise.getIdNat() == null ? "Aucun" : this.entreprise.getIdNat();
        txt_bill_comp_idnat
                .setText((!imp.equals("Aucun") && !idnat.equals("Aucun")) ? "Impôt:" + imp + " , IdNat:" + idnat
                        : (!imp.equals("Aucun") && idnat.equals("Aucun")) ? "Impôt:" + imp
                                : (imp.equals("Aucun") && !idnat.equals("Aucun")) ? "IdNat:" + idnat : " ");
        txt_comp_adresse_tel.setText("Tel:" + entreprise.getPhones());
        clients = FXCollections.observableArrayList(ClientDelegate.findClients());
        comptes = FXCollections.observableArrayList(CompteTresorDelegate.findCompteTresors(region));
        cbx_clients.setItems(clients);
        cbx_comptes.setItems(comptes);
        f = FileUtils.pointFile(entreprise.getUid() + ".png");
        InputStream is;
        if (!f.exists()) {
            is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            FileUtils.streamTofile(is);
        }
        Image image = null;
        try {
            image = new Image(new FileInputStream(f));
            img_vu_logo.setImage(image);
            Util.centerImage(img_vu_logo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ComboBoxAutoCompletion<Client>(cbx_clients);
        new ComboBoxAutoCompletion<CompteTresor>(cbx_comptes);
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
            this.client = ClientDelegate.findAnonymousClient();
        } else {
            this.client = c;
        }
        tf_phone_client.setText(this.client.getPhone());
        txt_client_selected_pay.setText(this.client.getNomClient());
        cbx_clients.setValue(this.client);
    }

    @FXML
    private void pickClient(Event evt) {
        String token = pref.get("token", null);
        MainUI.floatDialog(tools.Constants.CLIENT_DLG, 1090, 537, token, kazisafe, entreprise, region);
    }

    int compteur = 0;

    public void setLines(List<LigneVente> lig, Vente invoice) {

        String token = pref.get("token", null);
        messageForCustomer = pref.get("mesc", "Les marchandises vendues ne sont ni reprises ni echangees");
        System.out.println("Message pour customers " + messageForCustomer);
        kazisafe = KazisafeServiceFactory.createService(token);

        venteItems = new ArrayList<>();
        // db = JpaStorage.getInstance();

        Vente invoiceId = null;
        if (invoice != null) {
            Vente invoices = VenteDelegate.findVente(invoice.getUid());// db.findByUid(Vente.class, invoice.getUid());
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
        // String dev = pref.get("mainCur", "USD");
        double sommed = lignes.stream().mapToDouble(l -> l.getMontantUsd()).sum();
        double sommef = lignes.stream().mapToDouble(l -> l.getMontantCdf()).sum();
        final double tot = lignes.stream().mapToDouble(l -> l.getMontantUsd()).sum();
        if (maker.isCdf()) {
            tf_nominal_recu_cdf.setVisible(false);
            txt_eval_sum_cdf.setVisible(false);
            tf_arembourser_cdf.setVisible(false);
        } else {
            tf_nominal_recu_cdf.setVisible(true);
            txt_eval_sum_cdf.setVisible(true);
            tf_arembourser_cdf.setVisible(true);
        }
        cdf = sommef;
        usd = sommed;
        revertUsd = usd;
        revertCdf = cdf;

        txt_bill_somme_facture.setText("CDF : " + Math.round(sommef));
        txt_bill_somme_credit.setText("CDF : " + Math.round(sommef));
        sumCopy = sommed;
        txt_eval_sum_usd.setText(String.valueOf(BigDecimal.valueOf(sommed)
                .setScale(2, RoundingMode.HALF_EVEN).doubleValue()));

        txt_eval_sum_cdf.setText(String.valueOf(Math.round(sommef)));
        if (maker.isUsd()) {
            cdf = sommed * taux2change;
            usd = sommed;
            revertUsd = usd;
            revertCdf = cdf;
            // tot = lignes.stream().mapToDouble(l -> l.getMontantUsd()).sum();
            txt_bill_somme_facture.setText("USD : " + tot);
            txt_bill_somme_credit.setText("USD : " + tot);
            sumCopy = sommed;
            txt_eval_sum_usd.setText(String.valueOf(sommed));
            txt_eval_sum_cdf
                    .setText(String.valueOf(BigDecimal.valueOf(cdf).setScale(0, RoundingMode.HALF_EVEN).doubleValue()));
        }

        venteItems.clear();
        venteItems.addAll(lignes);
        //

        int ref = 0;
        if (invoiceId == null) {
            String time = String.valueOf(System.currentTimeMillis());
            String lvid = String.valueOf(DataId.generateInt()).concat(time.substring(time.length() - 2, time.length()));
            ref = Integer.parseInt(lvid);
            pane_bill_cash_paid.setVisible(false);
            txt_bill_cash_paid.setVisible(false);
        } else {
            if (!invoiceId.getObservation().equals("Drafted")) {
                tf_nominal_recu_usd.setText(String.valueOf(invoiceId.getMontantUsd()));
                tf_nominal_recu_cdf.setText(String.valueOf(Math.round(invoiceId.getMontantCdf())));
            }
            if (invoiceId.getPayment().toUpperCase().contains("credit partiel".toUpperCase())) {
                pane_bill_cash_paid.setVisible(true);
                txt_bill_cash_paid.setVisible(true);
                txt_bill_somme_credit.setText(String.valueOf(invoiceId.getMontantDette()));
                txt_bill_somme_facture.setText(
                        String.valueOf((invoiceId.getMontantUsd() + (invoiceId.getMontantCdf() / taux2change))));
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

            // StringUtils.isNumeric(invoiceId.getReference()) ?
            // Integer.parseInt(invoiceId.getReference()) : invoice.getUid();
        }

        vente4save = new Vente(ref);
        int tbil = pref.getInt("tranzit_bill", -100);

        tf_nominal_recu_usd.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    double in_usd = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                    if (newValue.isEmpty() && tf_nominal_recu_cdf.getText().isEmpty()) {
                        dt = usd;
                        ff = 0;
                        fd = 0;
                        txt_eval_sum_usd.setText(String.valueOf(usd));
                        txt_eval_sum_cdf.setText(String.valueOf(cdf));
                        tf_arembourser_usd.setText("0");
                        tf_arembourser_cdf.setText("0");
                        vente4save.setMontantUsd(0);
                        vente4save.setMontantCdf(0);
                    } else if (!newValue.isEmpty() && tf_nominal_recu_cdf.getText().isEmpty()) {

                        double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        // maker.setPrix(restUsd);
                        double restCdf = maker.isUsd() ? maker.usdToCdf(restUsd) : maker.cdfToUsd(restUsd);
                        // new BigDecimal().setScale(2, RoundingMode.HALF_EVEN).doubleValue();
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
                            tf_arembourser_cdf.setText("" + new BigDecimal(Math.round(retour * taux2change))
                                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        }

                        if (maker.isUsd()) {
                            vente4save.setMontantCdf(0);
                            vente4save.setMontantUsd(fd);
                        } else if (maker.isCdf()) {
                            vente4save.setMontantCdf(fd);
                            vente4save.setMontantUsd(0);
                        }

                    } else if (newValue.isEmpty() && !tf_nominal_recu_cdf.getText().isEmpty()) {
                        if (!StringUtils.isNumeric(tf_nominal_recu_cdf.getText())) {
                            return;
                        }
                        double inCdf = Double.parseDouble(tf_nominal_recu_cdf.getText());
                        double restCdf = new BigDecimal(cdf - inCdf).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        double restUsd = maker.isUsd() ? maker.cdfToUsd(restCdf) : maker.usdToCdf(restCdf);
                        // new BigDecimal(restCdf / taux2change).setScale(2,
                        // RoundingMode.HALF_EVEN).doubleValue();
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
                            tf_arembourser_usd.setText("" + new BigDecimal(retour / taux2change)
                                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        }

                        if (maker.isUsd()) {
                            vente4save.setMontantCdf(Math.round(ff));
                            vente4save.setMontantUsd(0);
                        } else if (maker.isCdf()) {
                            vente4save.setMontantCdf(0);
                            vente4save.setMontantUsd(ff);
                        }
                    } else {
                        if (!StringUtils.isNumeric(tf_nominal_recu_cdf.getText())) {
                            return;
                        }
                        double inCdf = Double.parseDouble(tf_nominal_recu_cdf.getText());
                        double converted = maker.isUsd() ? maker.cdfToUsd(inCdf) : maker.usdToCdf(inCdf);
                        double nwInUsd = (in_usd + converted);
                        double restUsd = new BigDecimal(usd - nwInUsd).setScale(2, RoundingMode.HALF_EVEN)
                                .doubleValue();
                        double restCdf = maker.isUsd() ? maker.usdToCdf(restUsd) : maker.cdfToUsd(restUsd);
                        if (restUsd >= 0) {
                            txt_eval_sum_usd.setText(String.valueOf(Math.round(restUsd)));
                            txt_eval_sum_cdf.setText(String.valueOf(Math.round(restCdf)));
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
                            tf_arembourser_cdf
                                    .setText("" + (maker.isUsd() ? maker.usdToCdf(retour) : maker.cdfToUsd(retour)));
                        }
                        if (maker.isUsd()) {
                            vente4save.setMontantUsd(fd);
                        } else if (maker.isCdf()) {
                            vente4save.setMontantCdf(fd);
                        }

                    }
                    double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                    vente4save.setDeviseDette("USD");
                    vente4save.setMontantDette(maker.isUsd() ? debt : (debt / taux2change));
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
                });

        tf_nominal_recu_cdf.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    double in_cdf = Double.parseDouble((newValue.isEmpty() ? "0" : newValue));
                    if (newValue.isEmpty() && tf_nominal_recu_usd.getText().isEmpty()) {
                        txt_eval_sum_cdf.setText(String.valueOf(
                                new BigDecimal(Math.round(cdf)).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                        txt_eval_sum_usd.setText(
                                String.valueOf(new BigDecimal(usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                        dt = usd;
                        ff = 0;
                        fd = 0;
                        tf_arembourser_usd.setText("0.0");
                        tf_arembourser_cdf.setText("0.0");
                        vente4save.setMontantCdf(0);
                        vente4save.setMontantUsd(0);
                    } else if (!newValue.isEmpty() && tf_nominal_recu_usd.getText().isEmpty()) {
                        double restCdf = new BigDecimal(Math.round(cdf - in_cdf)).setScale(2, RoundingMode.HALF_EVEN)
                                .doubleValue();
                        double restUsd = maker.isUsd() ? maker.cdfToUsd(restCdf) : maker.usdToCdf(restCdf);
                        // maker.setPrix(restCdf);
                        // new BigDecimal(restCdf / taux2change).setScale(2,
                        // RoundingMode.HALF_EVEN).doubleValue();
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
                            tf_arembourser_usd.setText("" + new BigDecimal(retour / taux2change)
                                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        }
                        // vente4save.setMontantUsd(0);
                        // vente4save.setMontantCdf(ff);
                        if (maker.isUsd()) {
                            vente4save.setMontantCdf(ff);
                            vente4save.setMontantUsd(0);
                        } else if (maker.isCdf()) {
                            vente4save.setMontantCdf(0);
                            vente4save.setMontantUsd(ff);
                        }
                    } else if (newValue.isEmpty() && !tf_nominal_recu_usd.getText().isEmpty()) {
                        if (!StringUtils.isNumeric(tf_nominal_recu_usd.getText())) {
                            return;
                        }
                        double in_usd = maker.isCdf() ? Double.parseDouble(tf_nominal_recu_cdf.getText())
                                : Double.parseDouble(tf_nominal_recu_usd.getText());
                        double restUsd = new BigDecimal(usd - in_usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        double restCdf = maker.isCdf() ? restUsd : Math.round(maker.usdToCdf(restUsd));
                        // new BigDecimal(restUsd * taux2change).setScale(2,
                        // RoundingMode.HALF_EVEN).doubleValue();
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
                            tf_arembourser_cdf
                                    .setText("" + (maker.isUsd() ? maker.usdToCdf(retour) : maker.cdfToUsd(retour)));
                        }
                        if (maker.isUsd()) {
                            vente4save.setMontantCdf(0);
                            vente4save.setMontantUsd(fd);
                        } else if (maker.isCdf()) {
                            vente4save.setMontantCdf(fd);
                            vente4save.setMontantUsd(0);
                        }
                    } else {
                        if (!StringUtils.isNumeric(tf_nominal_recu_usd.getText())) {
                            return;
                        }
                        double inUsd = Double.parseDouble(tf_nominal_recu_usd.getText());
                        double converted = maker.isUsd() ? maker.usdToCdf(inUsd) : maker.cdfToUsd(inUsd);
                        double nwInCdf = (in_cdf + converted);
                        double restCdf = new BigDecimal(cdf - nwInCdf).setScale(2, RoundingMode.HALF_EVEN)
                                .doubleValue();
                        double restUsd = maker.isUsd() ? maker.cdfToUsd(restCdf) : maker.usdToCdf(restCdf);
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
                            tf_arembourser_usd
                                    .setText("" + (maker.isUsd() ? maker.cdfToUsd(retour) : maker.usdToCdf(retour)));
                        }
                        // vente4save.setMontantCdf(ff);
                        if (maker.isUsd()) {
                            vente4save.setMontantCdf(ff);
                        } else if (maker.isCdf()) {
                            vente4save.setMontantUsd(ff);
                        }
                    }
                    String dev = pref.get("mainCur", "USD");
                    double debt = maker.isUsd() ? Double.parseDouble(txt_eval_sum_usd.getText())
                            : Double.parseDouble(txt_eval_sum_cdf.getText());
                    vente4save.setDeviseDette(dev);
                    vente4save.setMontantDette(debt);
                    if (debt > 0) {
                        if (tf_nominal_recu_cdf.getText().isEmpty()) {
                            cbx_payment_mode.getSelectionModel().select(3);
                            pane_bill_cash_paid.setVisible(false);
                            txt_bill_cash_paid.setVisible(false);
                        } else {
                            double sin = Double.parseDouble(tf_nominal_recu_cdf.getText());
                            txt_bill_somme_facture.setText(BigDecimal.valueOf(sin / taux2change)
                                    .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() > tot ? String.valueOf(tot)
                                            : String.valueOf(BigDecimal.valueOf(sin / taux2change)
                                                    .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
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
                } // pref.putInt("_bill_counter_", compteur);
                reference = String.format("%06d", compteur);
                break;
            }
            case 4:
                compteur = pref.getInt("_bill_counter_", 0);
                if (tbil == -100) {
                    compteur++;
                } // pref.putInt("_bill_counter_", compteur);
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
                        double pred = BigDecimal.valueOf((dette / tot) * 100).setScale(1, RoundingMode.HALF_EVEN)
                                .doubleValue();
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
                        double pred = BigDecimal.valueOf((dette / tot) * 100).setScale(1, RoundingMode.HALF_EVEN)
                                .doubleValue();
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
                        double pred = BigDecimal.valueOf((dette / tot) * 100).setScale(1, RoundingMode.HALF_EVEN)
                                .doubleValue();
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
                txt_nom_client1
                        .setText("Tel : " + (newValue.isEmpty() ? "..." : newValue.length() < 7 ? "NA" : newValue));
            }
        });
        cliname.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                txt_nom_client.setText("Client : " + (newValue.isEmpty() ? "..." : newValue));
            }
        });
        cbx_clients.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Client> observable, Client oldValue, Client newValue) -> {
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
                });

        if (lignes == null) {
            return;
        }
        venteItems.clear();
        venteItems.addAll(lignes);
        tbl_bill_products.setItems(FXCollections.observableArrayList(lignes));
        tbl_bill_products.setFixedCellSize(25);
        tbl_bill_products.prefHeightProperty().bind(tbl_bill_products.fixedCellSizeProperty()
                .multiply(Bindings.size(tbl_bill_products.getItems()).add(1.01)));
        tbl_bill_products.minHeightProperty().bind(tbl_bill_products.prefHeightProperty());
        tbl_bill_products.maxHeightProperty().bind(tbl_bill_products.prefHeightProperty());
        if (invoiceId == null) {
            txt_bill_date_vente.setText(LocalDateTime.now().toString());
        } else {
            txt_bill_date_vente.setText(invoiceId.getDateVente().toString());
        }
        txt_bill_num_facture
                .setText("Facture #" + ((invoiceId == null) ? vente4save.getReference() : invoiceId.getReference()));
        pane_invoiced.prefHeightProperty()
                .bind(tbl_bill_products.maxHeightProperty().add(Bindings.size(tbl_bill_products.getItems()).add(300)));
        pane_invoiced.minHeightProperty().bind(pane_invoiced.prefHeightProperty());
        pane_invoiced.maxHeightProperty().bind(pane_invoiced.prefHeightProperty());
        billbed.prefHeightProperty().bind(pane_invoiced.maxHeightProperty().add(89));
        billbed.minHeightProperty().bind(billbed.prefHeightProperty());
        billbed.maxHeightProperty().bind(billbed.prefHeightProperty());
        MainUI.setPattern(dpk_echeance_debt);
        cbx_clients.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client object) {
                return object == null ? null
                        : object.getNomClient() + " " + (object.getPhone() == null ? ""
                                : (object.getPhone().length() < 8 ? "" : object.getPhone()));
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
        cbx_printers.setConverter(new StringConverter<Printer>() {
            @Override
            public String toString(Printer object) {
                return object == null ? null : object.getName();
            }

            @Override
            public Printer fromString(String string) {
                return cbx_printers.getItems()
                        .stream()
                        .filter(v -> (v.getName())
                                .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_comptes.setConverter(new StringConverter<CompteTresor>() {
            @Override
            public String toString(CompteTresor object) {
                return object == null ? null
                        : object.getTypeCompte() + " " + object.getBankName() + " " + object.getNumeroCompte();
            }

            @Override
            public CompteTresor fromString(String string) {
                return cbx_comptes.getItems()
                        .stream()
                        .filter(obj -> (obj.getTypeCompte() + " " + obj.getBankName() + " " + obj.getNumeroCompte())
                                .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_comptes.getSelectionModel().select(0);
        cbx_comptes.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends CompteTresor> observable, CompteTresor oldValue,
                        CompteTresor newValue) -> {
                    if (newValue != null) {
                        choosenComptTr = newValue;
                    }
                });
        choosenComptTr = cbx_comptes.getValue();
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        System.out.println("Printewrs count " + osp.size());
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();

        cbx_printers.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) -> {
                    defaultPrinter = newValue;
                });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        // Ensure defaultPrinter is updated when selection changes
        cbx_printers.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        defaultPrinter = newVal;
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
                                    MainUI.notify(null, "Attention",
                                            "Le crédit Kazisafe (Record) expire bientôt, pensez à le renouveller", 5,
                                            "warning");
                                }

                                long max = SubscriptionUtil.nextSubscriptionMillis(abn);
                                System.err.println("Abonnement total " + max + " rest " + time.toMillis());
                                pref.putDouble("sub", Double.valueOf(max));
                                pref.put("etat-sub", etat);
                                if (!status.equals(Constants.ETAT_SUBSCRIPTION_EXPIRY)) {
                                    MainUI.notifySync("Kazisafe-Abonnement",
                                            "Activation souscription " + typeAb + " faite avec succes",
                                            "Notification de souscription au service kazisafe");
                                }
                            }
                            case "PRO" -> {
                                double nombreOper = abn.getNombreOperation();
                                pref.put("pro-sub", typeAb);
                                pref.putDouble("subscripro", nombreOper);
                                pref.put("pro-etat", etat);
                                Platform.runLater(() -> {
                                    txt_ebonus_remained.setText("eBonus restant: " + nombreOper + " clients");
                                });
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

    public void configBillTable() {
        col_bill_designation.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente r = param.getValue();
            Produit pr = ProduitDelegate.findProduit(r.getProductId().getUid());
            return new SimpleStringProperty(
                    pr.getNomProduit() + " " + (pr.getMarque() == null ? "" : pr.getMarque()) + " "
                            + "" + (pr.getModele() == null ? "" : pr.getModele()) + " "
                            + (pr.getTaille() == null ? "" : pr.getTaille()) + " "
                            + (pr.getCouleur() == null ? "" : pr.getCouleur()));
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
            List<LigneVente> items = LigneVenteDelegate.findByReference(vx.getUid());

            if (defaultPrinter != null) {
                // printWithThermal(defaultPrinter.getName(), items);
                double paid = maker.isUsd() ? toUsd() : toCdf();
                Client cl = vx.getClientId();
                String nomCl = cliname.getText().isEmpty() ? cl.getNomClient() : cliname.getText();
                printReceipt(defaultPrinter.getName(), entrepName, rccm, vx.getReference(), items, paid, nomCl,
                        cl.getPhone(), maker.getMainCurrency(), taux2change);
            }
        }
    }

    private double toUsd() {
        double paidus = 0;
        double paidcd = 0;
        if (!tf_nominal_recu_usd.getText().isBlank()) {
            paidus = Double.parseDouble(tf_nominal_recu_usd.getText());// :
                                                                       // Double.parseDouble(tf_nominal_recu_usd.getText())
                                                                       // * taux2change;
        }
        if (!tf_nominal_recu_cdf.getText().isBlank()) {
            paidcd = Double.parseDouble(tf_nominal_recu_cdf.getText()) / taux2change;// Double.parseDouble(tf_nominal_recu_usd.getText());
        }
        return paidus + paidcd;
    }

    private double toCdf() {
        double paidus = 0;
        double paidcd = 0;
        if (!tf_nominal_recu_usd.getText().isBlank()) {
            paidus = Double.parseDouble(tf_nominal_recu_usd.getText()) * taux2change;
        }
        if (!tf_nominal_recu_cdf.getText().isBlank()) {
            paidcd = Double.parseDouble(tf_nominal_recu_cdf.getText());// Double.parseDouble(tf_nominal_recu_usd.getText());
        }
        return Math.round(paidus + paidcd);
    }

    private void print() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> {
            createPdfBill(entreprise, vente4save, client);
        });
        exec.shutdown();
    }

    // private void printBills(PrinterJob pj, Node node) {
    // txt_print_status.textProperty().bind(pj.jobStatusProperty().asString());
    // boolean pr = pj.printPage(node);
    // if (pr) {
    // pj.endJob();
    // }
    // }
    Traisorerie svincss;
    CountDownLatch cdl = new CountDownLatch(1);

    @FXML
    public void saveVente(Event et) {
        if (!cbx_payment_mode.getValue().equals("CREDIT") && choosenComptTr == null) {

            MainUI.notify(null, "Erreur", "Veuillez selectionner le compte de tresorerie puis continuer", 4, "error");
            return;
        }
        List<Vente> vxs = VenteDelegate.findByRef(vente4save.getReference(), LocalDate.now());
        if (!vxs.isEmpty()) {
            if (!vxs.get(0).getObservation().equals("Drafted")) {
                MainUI.notify(null, "Erreur", "La vente ayant le même numéro de facture existe déjà", 4, "error");
                return;
            }
        }
        if (typecli != null) {
            if (typecli.equals("#3")) {
                if (tflibelle.getText().isEmpty()) {
                    MainUI.notify(null, "Erreur", "Veuillez entrer le numero de bon de l'abonne puis reesayer", 4,
                            "error");
                    return;
                }
            }
        }
        String type = pref.get("type-sub", " ");
        String dev = pref.get("mainCur", "USD");
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
        vente4save.setRegion(region);
        vente4save.setDeviseDette(maker.getMainCurrency());
        if (type.equalsIgnoreCase("Gold") || type.equalsIgnoreCase("Super Gold")) {
            vente4save.setObservation("YES");
        } else {
            vente4save.setObservation("NON");
        }
        if (cbx_payment_mode.getValue().equals("CREDIT+CASH")) {
            if (dpk_echeance_debt.getValue() == null || tf_phone_client.getText().isEmpty()
                    || cliname.getText().isEmpty()) {
                MainUI.notify(null, "Erreur",
                        "La date de l'échéance, le téléphone du client et son nom sont obligatoires", 4, "error");
                return;
            }
            double debt = Double.parseDouble(txt_eval_sum_usd.getText());
            vente4save.setDeviseDette(maker.getMainCurrency());
            vente4save.setMontantDette(debt);
            vente4save.setPayment(Constants.PAYMENT_CREDIT_CASH);
            vente4save.setEcheance(dpk_echeance_debt.getValue());
        } else if (cbx_payment_mode.getValue().equals("CREDIT")) {
            if (dpk_echeance_debt.getValue() == null || tf_phone_client.getText().isEmpty()
                    || cliname.getText().isEmpty()) {
                MainUI.notify(null, "Erreur",
                        "La date de l'échéance, le téléphone du client et son nom sont obligatoires", 4, "error");
                return;
            }
            double debt = Double.parseDouble(txt_eval_sum_usd.getText());
            vente4save.setDeviseDette(maker.getMainCurrency());
            vente4save.setMontantDette(debt);
            vente4save.setPayment(Constants.PAYEMENT_CREDIT);
            vente4save.setMontantCdf(0d);
            vente4save.setMontantUsd(0d);
            vente4save.setEcheance(dpk_echeance_debt.getValue());
        }
        if (!dpk_echeance_debt.isDisabled()) {
            if (tf_phone_client.getText().isEmpty() || !StringUtils.isNumeric(tf_phone_client.getText())) {
                MainUI.notify(null, "Erreur", "Le numéro de téléphone du client est obligatoire,"
                        + "c'est pour lui rapeller par SMS de vous payer dans le délai", 4, "error");
                return;
            }
        }

        if (!tf_phone_client.getText().isEmpty() || !cliname.getText().isEmpty()) {
            String phon = tf_phone_client.getText().isEmpty() ? String.valueOf(((int) (Math.random() * 10000)))
                    : tf_phone_client.getText();
            String namecli = cliname.getText().isEmpty() ? "Unknown" : cliname.getText();
            List<Client> clts = ClientDelegate
                    .findClientByPhone(phon.contains("\\+243") ? phon.replaceAll("\\+243", "0") : phon);
            if (clts.isEmpty()) {
                if (StringUtils.isNumeric(phon)) {
                    client = new Client(DataId.generate());
                    client.setPhone(phon.contains("\\+243") ? phon.replaceAll("\\+243", "0") : phon);
                    client.setNomClient(namecli);
                    client.setAdresse("Unknown");
                    client.setEmail("Unknown");
                    client.setTypeClient("Consommateur");
                    client.setParentId(ClientDelegate.findAnonymousClient());
                    Client sc = ClientDelegate.saveClient(client);
                    Executors.newCachedThreadPool()
                            .submit(() -> {
                                try {
                                    saveClientByHttp(sc);
                                } catch (IOException ex) {
                                    Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            });
                }
            } else {
                client = clts.get(0);
            }
        }
        vente4save.setDateVente(LocalDateTime.now());
        vente4save.setRegion(region);
        vente4save.setLibelle(
                tflibelle.getText().isEmpty() ? "Vente - Ref  " + vente4save.getReference() : tflibelle.getText());
        vente4save.setLatitude(0d);
        vente4save.setLongitude(0d);
        if (vente4save != null) {
            vente4save.setClientId(client);
            if (!clients.contains(client) && save2favorite.isSelected()) {
                clients.add(client);
            }

            Vente vtx = VenteDelegate.findVente(vente4save.getUid());
            if (vtx == null) {
                Vente vent = VenteDelegate.saveVente(vente4save);
                for (LigneVente i : venteItems) {
                    System.out.println("enregistremnt de "+i.getNumlot());
                    i.setClientId(client.getPhone());
                    i.setReference(vent);
                    LigneVenteDelegate.saveLigneVente(i);
                    Mesure m = i.getMesureId();
                    double cau = i.getCoutAchat() / m.getQuantContenu();
                    RecquisitionDelegate.rectifyStock(i.getProductId(), LocalDate.now(), LocalDate.now(), region, cau);
                    Category c = CategoryDelegate.findCategory(i.getProductId().getCategoryId().getUid());
                    SaleAgregate sa = new SaleAgregate();
                    sa.setUid(DataId.generate());
                    sa.setCategoryId(c);
                    sa.setProductId(i.getProductId());
                    sa.setCoutAchatTotal(i.getCoutAchat() * i.getQuantite());
                    sa.setDate(LocalDateTime.now());
                    sa.setMesureId(m);
                    sa.setQuantite(i.getQuantite());
                    sa.setRegion(region);
                    sa.setTotalSaleUsd(dev.equalsIgnoreCase("USD")
                            ? i.getMontantUsd()
                            : i.getMontantCdf());
                    RepportDelegate.createMetric(sa);
                }
            } else {
                List<LigneVente> lvs = LigneVenteDelegate.findByReference(vente4save.getUid());
                System.out.println("Venty " + vente4save.getLibelle());
                Vente vent = VenteDelegate.updateVente(vente4save);
                if (!lvs.isEmpty()) {
                    for (LigneVente lv : lvs) {
                        LigneVenteDelegate.deleteLigneVente(lv);
                    }
                    for (LigneVente i : venteItems) {
                        i.setReference(vente4save);
                        i.setClientId(client.getPhone());
                        LigneVenteDelegate.saveLigneVente(i);
                        Mesure m = i.getMesureId();
                        double cau = i.getCoutAchat() / m.getQuantContenu();
                        RecquisitionDelegate.rectifyStock(i.getProductId(), LocalDate.now(), LocalDate.now(),
                                region, cau);
                        Category c = CategoryDelegate.findCategory(i.getProductId().getCategoryId().getUid());
                        SaleAgregate sa = new SaleAgregate();
                        sa.setUid(DataId.generate());
                        sa.setCategoryId(c);
                        sa.setProductId(i.getProductId());
                        sa.setCoutAchatTotal(i.getCoutAchat() * i.getQuantite());
                        sa.setDate(LocalDateTime.now());
                        sa.setMesureId(m);
                        sa.setQuantite(i.getQuantite());
                        sa.setRegion(region);
                        sa.setTotalSaleUsd(dev.equalsIgnoreCase("USD") ? i.getMontantUsd()
                                : i.getMontantCdf());
                        RepportDelegate.createMetric(sa);
                    }
                }
            }
            System.out.println("Ventitem count : "+venteItems.size());
            vente4save.setLigneVenteList(venteItems);
            pref.putInt("_bill_counter_", compteur);
            pref.putInt("tranzit_bill", -100);
            if (!cbx_payment_mode.getValue().equals("CREDIT")) {
                LocalDate ldt = vente4save.getDateVente().toLocalDate();
                Traisorerie trzr = TraisorerieDelegate.findExistingOf("BE" + tools.Constants.dateTodayRef(ldt), ldt,
                        choosenComptTr.getUid(), region);
                double sumUsd = VenteDelegate.sumUsdSaleOf(ldt, ldt, region);
                double sumCdf = VenteDelegate.sumCdfSaleOf(ldt, ldt, region);
                double balcdf = TraisorerieDelegate.findCurrentBalanceCdf(choosenComptTr.getUid(), ldt, ldt, region);
                double balusd = TraisorerieDelegate.findCurrentBalanceUsd(choosenComptTr.getUid(), ldt, ldt, region);
                System.err.println(
                        "comptresor " + cbx_comptes.getValue().getIntitule() + " " + balcdf + " usdb " + balusd);
                if (trzr == null) {
                    trzr = new Traisorerie(DataId.generate());
                    trzr.setDate(LocalDateTime.now());
                    trzr.setLibelle("Ventes journalier");
                    trzr.setMontantCdf(sumCdf);
                    trzr.setMontantUsd(sumUsd);
                    trzr.setMouvement(Mouvment.AUGMENTATION.name());
                    trzr.setReference("BE" + tools.Constants.dateTodayRef(ldt));
                    trzr.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
                    trzr.setRegion(region);
                    trzr.setTresorId(choosenComptTr);
                    trzr.setSoldeCdf(balcdf + vente4save.getMontantCdf());
                    trzr.setSoldeUsd(balusd + vente4save.getMontantUsd());
                    svincss = TraisorerieDelegate.saveTraisorerie(trzr);
                } else {
                    trzr.setLibelle("Ventes journalier ");
                    trzr.setMontantCdf(sumCdf);
                    trzr.setMontantUsd(sumUsd);
                    trzr.setLibelle("Ventes journalier");
                    trzr.setDate(LocalDateTime.now());
                    trzr.setMouvement(Mouvment.AUGMENTATION.name());
                    trzr.setTresorId(choosenComptTr);
                    trzr.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
                    trzr.setSoldeCdf(balcdf + vente4save.getMontantCdf());
                    trzr.setSoldeUsd(balusd + vente4save.getMontantUsd());
                    svincss = TraisorerieDelegate.updateTraisorerie(trzr);
                }
            }
            System.out.println("SERVICE TX SAVED " + svincss.getSoldeUsd());
            if (chbx_print_receipt.isSelected()) {
                // if (PermissionDelegate.hasPermission(PermitTo.PRINT_INVOICE)) {

                if (chbx_print_thermal.isSelected()) {
                    print();
                } else {
                    if (defaultPrinter != null) {
                        for (int i = 0; i < copies; i++) {
                            System.out.println("Printing on thermal... " + i);
                            double paid = maker.isUsd() ? toUsd() : toCdf();
                            Client cl = vente4save.getClientId();
                            printReceipt(defaultPrinter.getName(), entrepName, rccm, vente4save.getReference(),
                                    venteItems, paid, cl.getNomClient(), cl.getPhone(), maker.getMainCurrency(),
                                    taux2change);
                            // printWithThermal(defaultPrinter.getName(), venteItems);
                        }
                    }
                }
                // }
            }
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        if (svincss != null) {
                            // Util.sync(svincss, Constants.ACTION_CREATE, Tables.TRAISORERIE);
                            boolean savecsh = saveCashByHttp(svincss);
                            System.out.println("Tresorerie http saved is " + savecsh);
                        }
                    });
            MainUI.notify(null, "Info", "Vente enregistree avec succes", 4, "info");
            cdl.countDown();
            tryToSaveSale(svincss == null ? null : svincss.getUid(), choosenComptTr, client, vente4save,
                    vente4save.getLigneVenteList(), et);

        }
    }

    private void tryToSaveSale(final String transaction,
            final CompteTresor ct,
            final Client client,
            final Vente vente,
            final List<LigneVente> lignes, Event et) {
        Executors.newCachedThreadPool()
                .submit(() -> {
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                    }
                    if (!NetLoockup.NETWORK_STATUS_ON) {
                        Platform.runLater(() -> {
                            PosController.getInstance().clearCart();
                            try {
                                if (PosController.getInstance().savedCarts != null) {
                                    PosController.getInstance().savedCarts.removeIf(v -> v.getUid() == vente.getUid());
                                }
                            } catch (Exception ex) {
                            }
                            PosController.getInstance().refreshPos(et);
                            venteItems.clear();
                            close(et);
                        });
                        return;
                    }
                    int retries = 0;
                    OUTER: while (retries < MAX_SALE_RETRY) {
                        try {
                            vente.setClientId(client);
                            System.out.println("Client phone : " + client.getPhone());
                            Response<Vente> rep = saveVenteByHttp(vente, client, ct, transaction, lignes);
                            if (rep == null) {
                                System.out.println("Reponse save vente by http est NULL" + rep);
                                continue;
                            }
                            int reponse = rep.code();
                            System.out.println("Reponse http code - de vente " + reponse);
                            switch (reponse) {
                                case 417 -> {
                                    // client
                                    System.out.println("T3 Client " + reponse + " " + client.getPhone());
                                    List<Client> cs = ClientDelegate.findClientByPhone(client.getPhone());
                                    if (!cs.isEmpty()) {
                                        System.err.println("Clients is Empty");
                                        Client c = cs.get(0);
                                        boolean client_saved = saveClientByHttp(c);
                                        System.out.println(
                                                "Client enregistre : " + (client_saved ? "OK" : "OOps! error"));
                                    } else {
                                        Client sc = ClientDelegate.saveClient(client);
                                        System.out.println("Save clt " + sc.getPhone());
                                    }
                                }
                                case 412 -> {
                                    // compte tresor
                                    System.out.println("T3 Compte Tresor " + reponse);
                                    List<CompteTresor> comptes = CompteTresorDelegate
                                            .findByNumeroCompte(ct.getNumeroCompte());
                                    if (!comptes.isEmpty()) {
                                        System.err.println("After if compte tres");
                                        CompteTresor compte = comptes.get(0);
                                        saveCompte(compte);
                                        Executors.newCachedThreadPool()
                                                .submit(() -> {
                                                    Util.sync(compte, Constants.ACTION_CREATE, Tables.COMPTETRESOR);
                                                });
                                    }
                                }
                                case 400 -> {
                                    for (LigneVente ligne : lignes) {
                                        Produit produit = ProduitDelegate.findProduit(ligne.getProductId().getUid());
                                        List<Mesure> mesures = MesureDelegate.findMesureByProduit(produit.getUid());
                                        sendProduitIfNotExist(produit, mesures);
                                    }
                                }
                                case 200 -> {
                                    System.out.println("Vente enregistree au serveur avec succes");
                                    break OUTER;
                                }
                                default ->
                                    System.out.println("Reponse par defaut " + reponse);
                            }
                            retries++;
                            try {
                                TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, retries)); // Delai exponentiel
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } catch (IOException ex) {
                            System.out.println("T3 ERROR " + ex.getMessage());
                            Logger.getLogger(PaymentController.class.getName()).log(Level.INFO, null, ex);
                            break;
                        }
                    }
                    Platform.runLater(() -> {
                        PosController.getInstance().clearCart();
                        // try {
                        // if (PosController.getInstance().savedCarts != null) {
                        // PosController.getInstance().savedCarts.removeIf(v -> v.getUid() ==
                        // vente.getUid());
                        // }
                        // } catch (Exception ex) {
                        // }
                        PosController.getInstance().refreshPos(et);
                        venteItems.clear();
                        close(et);
                    });
                });
    }

    private void createPdfBill(Entreprise entrep, Vente vt, Client ff) {
        if (ff == null) {
            MainUI.notify(null, "Erreur", "Tu peux aussi preciser un client si besoin", 3, "error");
        }

        try {
            PDDocument document = new PDDocument();
            PDPage fPage = new PDPage(PDRectangle.A4);
            document.addPage(fPage);

            int pageW = (int) PDRectangle.A4.getWidth();// fPage.getTrimBox().getWidth();
            int pageH = (int) PDRectangle.A4.getHeight();// fPage.getTrimBox().getHeight();

            PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
            PDFUtils pdf = new PDFUtils(document, contentStream);

            // PDFont normalbold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
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
            pdf.addTextLine(new String[] { "Adresse : " + entrep.getAdresse(),
                    "RCCM : " + entrep.getIdentification(),
                    entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(),
                    entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot() }, 15, 25, pageH - 192,
                    hnormal, 14, java.awt.Color.BLACK);
            String idf = ff.getPhone();

            pdf.addTextLine(ff.getNomClient(), ((int) (pageW
                    - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "Tel : " + idf) / 1000 * 15
                    - 92)), pageH - 180, hnormal, 18, java.awt.Color.BLACK);
            pdf.addTextLine(new String[] { "Adresse : " + ff.getAdresse(), idf == null ? ""
                    : "RCCM : " + idf,
                    "Tel : " + ff.getPhone() }, 15,
                    ((int) (pageW
                            - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "RCCM : " + idf)
                                    / 1000 * 15
                            - 92)),
                    pageH - 192, hnormal, 14, java.awt.Color.BLACK);

            String date = "Date : " + LocalDateTime.now();
            pdf.addTextLine(new String[] { date,
                    "Facture N# : " + vt.getReference() }, 15,
                    ((int) (pageW - hnormal.getStringWidth(date) / 1000 * 15 - 32)), pageH - 260, hnormal, 14,
                    java.awt.Color.BLACK);
            // Tableau items
            int table[] = { 55, 100, 230, 65, 90 };

            pdf.addTable(table, 30, 25, pageH - 400);
            pdf.setFont(hnormal, 11, java.awt.Color.WHITE);

            pdf.setRightAlignedColumns(new int[] { 0, 2 });

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
            // double total;
            // double actuel;
            // double dette;
            // double pred;
            // if (dev.equals("USD")) {
            // total = sumFact(venteItems, dev);
            // actuel = vente4save.getMontantUsd() + (vente4save.getMontantCdf() /
            // taux2change);
            // dette = Double.parseDouble(txt_eval_sum_usd.getText());
            // } else {
            // total = sumFact(venteItems, dev);
            // actuel = vente4save.getMontantCdf() + (vente4save.getMontantUsd() *
            // taux2change);
            // dette = Double.parseDouble(txt_eval_sum_cdf.getText());
            // }
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
                        int tablex[] = { 55, 230, 100, 65, 90 };
                        pdf.addTable(tablex, 30, 25, pageH - 68);
                        pdf.setFont(hnormal, 10, java.awt.Color.BLACK);

                        pdf.setRightAlignedColumns(new int[] { 2, 3, 4 });
                        contentStream.setFont(hnormal, 10);
                        if (ln == lpp || i == 14) {
                            ln = 0;
                        }
                    }
                    //
                }

                Produit x = rupture.getProductId();
                pdf.setRightAlignedColumns(new int[] { 2, 3, 4 });
                pdf.addCell(i + ".", egray);
                pdf.addCell(rupture.getQuantite() + " " + rupture.getMesureId().getDescription(), egray);
                pdf.addCell(x.getNomProduit() + " "
                        + "" + x.getMarque() + " " + x.getModele() + " " + (x.getTaille() == null ? "" : x.getTaille())
                        + " " + (x.getCouleur() == null ? "" : x.getCouleur()), egray);

                pdf.addCell((rupture.getPrixUnit() * taux2change) + " ", egray);
                double stot = rupture.getQuantite() * rupture.getPrixUnit();
                somme += stot;
                pdf.addCell(Math.round(
                        (BigDecimal.valueOf(stot).setScale(2, RoundingMode.HALF_EVEN).doubleValue() * taux2change))
                        + " FC", egray);

            }

            if (ln == lpp - 1 || ln == 0) {
                contentStream.close();
                PDPage fPage2 = new PDPage(PDRectangle.A4);
                document.addPage(fPage2);
                contentStream = new PDPageContentStream(document, fPage2);
                pdf = new PDFUtils(document, contentStream);
                int tablex[] = { 55, 230, 100, 65, 90 };
                pdf.addTable(tablex, 30, 25, pageH - 68);
                pdf.setFont(hnormal, 10, java.awt.Color.BLACK);
                pdf.setRightAlignedColumns(new int[] { 2, 3, 4 });
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

    private boolean saveClientByHttp(Client clt) throws IOException {
        Response<Client> exec = kazisafe.saveByForm(clt.getUid(), clt.getNomClient(), clt.getPhone(),
                clt.getTypeClient(), clt.getEmail(), clt.getAdresse(), clt.getParentId().getUid())
                .execute();
        return exec.code() == 200;
    }

    private boolean saveCashByHttp(Traisorerie tr) {
        try {
            Response<Traisorerie> excuted = kazisafe.saveCash(tr).execute();
            return excuted.isSuccessful();
        } catch (IOException ex) {
            return false;
        }
    }

    private Response<Vente> saveVenteByHttp(Vente vente, Client client, CompteTresor tresor, String transaction,
            List<LigneVente> venteItems) throws IOException {
        try {

            VenteHelper hlp = new VenteHelper();
            hlp.setTransactionId(transaction);
            hlp.setTresor(tresor);
            hlp.setClient(client);
            hlp.setLigneVentes(venteItems);
            hlp.setVente(vente);
            Response<Vente> exe = kazisafe.syncSale(hlp).execute();
            System.out.println("Vente response Http : " + exe);
            return exe;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<SaleItemHelper> toSaleItemHelper(List<LigneVente> lvs) {
        List<SaleItemHelper> result = new ArrayList<>();
        for (LigneVente lv : lvs) {
            SaleItemHelper sih = new SaleItemHelper();
            sih.setClientId(lv.getClientId());
            sih.setMesureId(lv.getMesureId().getUid());
            sih.setMontantCdf(lv.getMontantCdf());
            sih.setMontantUsd(lv.getMontantUsd());
            sih.setNumlot(lv.getNumlot());
            sih.setProductId(lv.getProductId().getUid());
            sih.setQuantite(lv.getQuantite());
            sih.setSalePrice(lv.getPrixUnit());
            sih.setUid(lv.getUid());
            sih.setVenteId(lv.getReference().getUid());
            result.add(sih);
        }
        return result;
    }

    private boolean saveCompte(CompteTresor tr) throws IOException {
        Response<CompteTresor> exec = kazisafe.saveCompteTresorByForm(tr.getUid(),
                tr.getBankName(), tr.getIntitule(),
                tr.getSoldeMinimum(), tr.getNumeroCompte(),
                tr.getRegion(),
                tr.getTypeCompte()).execute();
        System.err.println("Reponse exec " + exec.code());
        return exec.code() == 200;
    }

    private void sendProduitIfNotExist(Produit produit, List<Mesure> mesures) {
        byte[] imageBytes = produit.getImage();
        if (imageBytes == null) {
            imageBytes = loadDefaultImage();
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        saveProduitByHttp(produit, base64Image, mesures);
    }

    private byte[] loadDefaultImage() {
        try (InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png")) {
            return FileUtils.readAllBytes(is);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image par défaut" + e.getMessage());
            return new byte[0];
        }
    }

    private void saveProduitByHttp(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = createProduitHelper(produit, base64Image, mesures);
        try {
            Response<Produit> response = kazisafe.saveLite(produitHelper).execute();
            if (response.isSuccessful()) {
                System.out.println("Save synchrone Produit " + response.code());
            } else {
                System.err.println("Echec d'enregistrement du produit code : " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement du produit" + e.getMessage());
        }
    }

    private ProduitHelper createProduitHelper(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = new ProduitHelper();
        produitHelper.setUid(produit.getUid());
        produitHelper.setCategoryId(produit.getCategoryId().getUid());
        produitHelper.setCodebar(produit.getCodebar());
        produitHelper.setCouleur(produit.getCouleur());
        produitHelper.setMarque(produit.getMarque());
        produitHelper.setModele(produit.getModele());
        produitHelper.setNomProduit(produit.getNomProduit());
        produitHelper.setImage("data:image/jpeg;base64," + base64Image);
        produitHelper.setTaille(produit.getTaille());
        produitHelper.setMethodeInventaire(produit.getMethodeInventaire());
        produitHelper.setMesureList(mesures);
        return produitHelper;
    }

    /**
     * Finds bluetooth thermal printers and registers them as PrintServices.
     * They will appear in the printer list if the system supports it.
     */
    @FXML
    public void onBluetoothSearch() {
        if (!chbx_bt_search.isSelected()) {
            pgi_bt_search.setVisible(false);
            lbl_bt_count.setVisible(false);
            return;
        }

        pgi_bt_search.setVisible(true);
        lbl_bt_count.setVisible(true);
        lbl_bt_count.setText("Recherche...");
        
        CompletableFuture.runAsync(() -> {
            try {
                List<RemoteDevice> devices = BluetoothPrinterManager.findPrinters();
                int count = 0;
                for (RemoteDevice device : devices) {
                    try {
                        String name = device.getFriendlyName(false);
                        String url = BluetoothPrinterManager.getServiceUrl(device);
                        if (url != null) {
                            BluetoothPrintService bts = new BluetoothPrintService(name, url);
                            PrintServiceLookup.registerService(bts);
                            count++;
                            final int currentCount = count;
                            Platform.runLater(() -> {
                                // Refresh the printer list
                                ObservableSet<Printer> osp = Printer.getAllPrinters();
                                cbx_printers.setItems(setToList(osp));
                                lbl_bt_count.setText(currentCount + " trouvé(s)");
                            });
                        }
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Error discovering device: " + e.getMessage());
                    }
                }
                final int finalCount = count;
                Platform.runLater(() -> {
                    pgi_bt_search.setVisible(false);
                    lbl_bt_count.setText(finalCount + " trouvé(s)");
                    chbx_bt_search.setSelected(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    pgi_bt_search.setVisible(false);
                    lbl_bt_count.setText("Erreur: " + e.getMessage());
                    chbx_bt_search.setSelected(false);
                });
            }
        });
    }

    /**
     * Finds serial printers (COM ports) and registers them as PrintServices.
     */
    public void refreshSerialPrinters() {
        try {
            List<SerialPort> ports = SerialPrinterManager.findSerialPrinters();
            for (SerialPort port : ports) {
                String name = port.getDescriptivePortName();
                String systemName = port.getSystemPortName();
                SerialPrintService sps = new SerialPrintService(name, systemName);
                PrintServiceLookup.registerService(sps);
            }
            
            // Refresh the printer list in cbx_printers
            ObservableSet<Printer> osp = Printer.getAllPrinters();
            cbx_printers.setItems(setToList(osp));
        } catch (Exception e) {
            System.err.println("Error discovering serial ports: " + e.getMessage());
        }
    }

    /**
     * Launch printing of the bill receipt via Bluetooth if it's the selected printer.
     */
    public void printBillViaBluetooth() {
        if (defaultPrinter != null && defaultPrinter.getName().startsWith("BT:")) {
            if (txt_print_status != null) {
                txt_print_status.setText("Printing via BT...");
            }
            printReceipt(defaultPrinter.getName(), entrepName, rccm, vente4save.getReference(), venteItems, ff + fd, cliname.getText(), tf_phone_client.getText(), maker.getMainCurrency(), taux2change);
        }
    }

    /**
     * Launch printing of the bill receipt via Serial if it's the selected printer.
     */
    public void printBillViaSerial() {
        if (defaultPrinter != null && defaultPrinter.getName().startsWith("COM:")) {
            if (txt_print_status != null) {
                txt_print_status.setText("Printing via Serial...");
            }
            printReceipt(defaultPrinter.getName(), entrepName, rccm, vente4save.getReference(), venteItems, ff + fd, cliname.getText(), tf_phone_client.getText(), maker.getMainCurrency(), taux2change);
        }
    }
}

