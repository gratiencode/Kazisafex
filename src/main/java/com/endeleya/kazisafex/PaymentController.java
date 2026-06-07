package com.endeleya.kazisafex;

import com.endeleya.kazisafex.BluetoothPrinterManager;
import com.endeleya.kazisafex.MainuiController;
import com.endeleya.kazisafex.PosController;
import com.endeleya.kazisafex.ProduitsController;
import com.endeleya.kazisafex.SerialPrinterManager;
import com.endeleya.kazisafex.tools.BluetoothPrintService;
import com.endeleya.kazisafex.tools.SerialPrintService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fazecast.jSerialComm.SerialPort;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.BarCodeWrapperInterface;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImage;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.ImageWrapperInterface;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.output.PrinterOutputStream;
import data.Abonnement;
import data.BaseModel;
import data.Category;
import data.Client;
import data.CompteTresor;
import data.Entreprise;
import data.LigneVente;
import data.Mesure;
import data.Produit;
import data.ProduitHelper;
import data.Recquisition;
import data.SaleAgregate;
import data.Traisorerie;
import data.Vente;
import data.VenteHelper;
import data.core.KazisafeServiceFactory;
import data.helpers.Mouvment;
import data.helpers.TypeTraisorerie;
import data.network.Kazisafe;
import delegates.CategoryDelegate;
import delegates.ClientDelegate;
import delegates.CompteTresorDelegate;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RepportDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
import javax.bluetooth.RemoteDevice;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
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
import java.util.stream.Collectors;

public class PaymentController
        implements Initializable {

    @FXML
    private Label txt_eval_sum_usd;
    @FXML
    private Label txt_bill_num_facture;
    @FXML
    private Label txt_bill_companyname;
    @FXML
    private Label txt_comp_adresse;
    @FXML
    private Label txt_comp_adresse_tel;
    @FXML
    private Label txt_bill_user;
    @FXML
    private Label txt_lbl_credit;
    @FXML
    private Label txt_bill_somme_credit;
    @FXML
    private Label txt_eval_sum_cdf;
    @FXML
    private Label txt_bill_date_vente;
    @FXML
    private Label txt_bill_somme_facture;
    @FXML
    private Label txt_bill_contact_entreprise;
    @FXML
    private Label txt_bill_cash_paid;
    @FXML
    private Label txt_reduction;
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
    private CheckBox chbx_print_receipt;
    @FXML
    private CheckBox chbx_print_thermal;
    @FXML
    private Label txt_reference_vente;
    @FXML
    private Label txt_print_status;
    @FXML
    private Label txt_nom_client;
    @FXML
    private Label txt_nom_client1;
    @FXML
    private Label txt_bill_comp_id;
    @FXML
    private Label txt_bill_comp_idnat;
    @FXML
    private Label txt_client_selected_pay;
    @FXML
    private Label captionusd;
    @FXML
    private Label captioncdf;
    @FXML
    private Label lbl_bt_count;
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
    double sumCopy = 0.0;
    double cdf = 0.0;
    double revertCdf;
    double ff;
    double fd;
    double usd = 0.0;
    double revertUsd;
    double dt;
    String user;
    String typecli;
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
    private int title_s;
    private int identite_s;
    private int body_s;
    private int footer_s;
    private int line_dashes;
    private double restCdf;
    private double debt;
    private double inCdf;
    private static PaymentController instance;
    private Entreprise entreprise;
    private PriceMaker maker;
    String entrepName;
    String idNat;
    String phonez;
    String adresse;
    String email;
    String nif;
    String rccm;
    String region;
    String role;
    int count_logic = 0;
    int copies = 1;
    @FXML
    private Label txt_print_status1;
    @FXML
    private Label txt_ebonus_remained;
    int WIDTH;
    int compteur = 0;
    Traisorerie svincss;
    CountDownLatch cdl = new CountDownLatch(1);
    @FXML
    private DatePicker dpk_date_vente;

    public PaymentController() {
        this.maker = new PriceMaker();
        instance = this;
    }

    public static PaymentController getInstance() {
        return instance;
    }

    private void initPref() {
        this.pref = Preferences.userNodeForPackage(SyncEngine.class);
        if (this.role == null) {
            this.role = this.pref.get("role", "Trader");
        }
        if (this.region == null) {
            this.region = this.pref.get("region", "Goma");
        }
        this.WIDTH = this.pref.getInt("print-lines-dashcount", 48);
        this.title_s = this.pref.getInt("print-title-size", 1);
        this.body_s = this.pref.getInt("print-body-size", 1);
        this.footer_s = this.pref.getInt("print-footer-size", 1);
        this.identite_s = this.pref.getInt("print-identite-size", 1);
        this.line_dashes = this.WIDTH;
        this.count_logic = this.pref.getInt("count-logic", 0);
        this.copies = this.pref.getInt("bill-copy", 1);
        this.entrepName = this.pref.get("ent_name", "unknown");
        this.rccm = this.pref.get("ent_ID", "Aucun");
        this.adresse = this.pref.get("ent_adresse", "aucune");
        this.email = this.pref.get("ent_email", "");
        this.idNat = this.pref.get("ent_idnat", "Aucun");
        this.nif = this.pref.get("ent_impot", "Aucun");
        this.phonez = this.pref.get("ent_phones", "");
        this.maker.setMainCurrency(this.pref.get("mainCur", "USD"));
        this.taux2change = this.pref.getDouble("taux2change", 2800.0);
        this.print = this.pref.getBoolean("print", true);
        this.role = this.pref.get("priv", null);
        this.region = this.pref.get("region", "...");
    }

    public void printReceipt(String printerName, String storeName, String rccm,
            String invoiceNumber, List<LigneVente> itemx, double amountPaid, String customerName,
            String customerPhone, String currency, double rate) {
        Executors.newSingleThreadExecutor().submit(() -> {
            PrinterOutputStream pos = null;
            try {
                if (printerName == null) {
                    System.out.println("impirmente name " + printerName);
                    return;
                }
                List<LigneVente> items = new ArrayList<>(itemx);
                PrintService ps = PrinterOutputStream.getPrintServiceByName(printerName);
                pos = new PrinterOutputStream(ps);
                try (EscPos printer = new EscPos(pos)) {
                    printer.setCharacterCodeTable(EscPos.CharacterCodeTable.CP863_Canadian_French);
                    Style title = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(this.title_s == 1 ? Style.FontSize._1 : (this.title_s == 2 ? Style.FontSize._2 : Style.FontSize._3), this.title_s == 1 ? Style.FontSize._1 : (this.title_s == 2 ? Style.FontSize._2 : Style.FontSize._3));
                    Style identite = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(this.identite_s == 1 ? Style.FontSize._1 : (this.identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3), this.identite_s == 1 ? Style.FontSize._1 : (this.identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3));
                    Style body = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(this.body_s == 1 ? Style.FontSize._1 : (this.body_s == 2 ? Style.FontSize._2 : Style.FontSize._3), this.body_s == 1 ? Style.FontSize._1 : (this.body_s == 2 ? Style.FontSize._2 : Style.FontSize._3));
                    Style pied = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(this.footer_s == 1 ? Style.FontSize._1 : (this.footer_s == 2 ? Style.FontSize._2 : Style.FontSize._3), this.footer_s == 1 ? Style.FontSize._1 : (this.footer_s == 2 ? Style.FontSize._2 : Style.FontSize._3));
                    Style ephone = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(Style.FontSize._1, Style.FontSize._1);
                    Style customer = new Style(printer.getStyle()).setBold(true).setUnderline(Style.Underline.OneDotThick);
                    Style right = new Style(printer.getStyle()).setJustification(EscPosConst.Justification.Right);
                    Style centerbold = new Style().setJustification(EscPosConst.Justification.Center).setBold(true);
                    if (this.f != null) {
                        RasterBitImageWrapper imgWrapper = new RasterBitImageWrapper();
                        imgWrapper.setJustification(EscPosConst.Justification.Center);
                        printer.feed(1);
                        BufferedImage bimg = ImageIO.read(this.f);
                        BitonalThreshold bitonal = new BitonalThreshold(100);
                        EscPosImage posimg = new EscPosImage((CoffeeImage) new CoffeeImageImpl(bimg), (Bitonal) bitonal);
                        try {
                            printer.write((ImageWrapperInterface) imgWrapper, posimg);
                        } catch (Exception e) {
                            MainUI.notify(null, (String) "Attention", (String) "Veuillez mettre un bon logo (125X125px) au moins, pour votre facture", (long) 3L, (String) "warning");
                        }
                    }
                    printer.feed(1);
                    printer.writeLF(title, this.entreprise.getNomEntreprise() == null ? this.entrepName : this.entreprise.getNomEntreprise());
                    String idnat = this.entreprise.getIdNat() == null ? this.idNat : this.entreprise.getIdNat();
                    String impot = this.entreprise.getNumeroImpot() == null ? this.nif : this.entreprise.getNumeroImpot();
                    String phones = this.entreprise.getPhones() == null ? this.phonez : this.entreprise.getPhones();
                    String stateId = "RCCM." + this.entreprise.getIdentification() + " " + (String) (idnat == null ? "" : "ID NAT." + idnat) + (String) (impot == null ? "" : " NIF." + impot + "\nAdresse : " + this.entreprise.getAdresse() + "\n" + (String) (phones == null || phones.equals("-") ? "" : "Tel :" + phones));
                    printer.writeLF(centerbold, stateId);
                    if (this.entreprise.getWebsite() != null) {
                        printer.writeLF(identite, this.entreprise.getWebsite());
                    }
                    printer.writeLF(right, " Facture N.: " + this.vente4save.getReference());
                    LocalDateTime dv = this.vente4save.getDateVente();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    printer.writeLF(right, dv == null ? dtf.format(LocalDateTime.now()) : dtf.format(dv));
                    printer.write("Client : ");
                    printer.writeLF(customer, this.cliname.getText().isBlank() ? "Anonyme" : this.cliname.getText());
                    System.out.println("-".repeat(this.WIDTH));
                    boolean isUSD = "USD".equalsIgnoreCase(currency);
                    DecimalFormat moneyFormat = isUSD ? new DecimalFormat("0.00") : new DecimalFormat("#,##0.00");
                    printer.writeLF("-".repeat(this.WIDTH));
                    double grandTotal = 0.0;
                    for (LigneVente item : items) {
                        Produit p = ProduitDelegate.findProduit((String) item.getProductId().getUid());
                        List<String> nameLines = PaymentController.wrapText(p.getNomProduit() + " " + p.getModele() + " " + p.getTaille() + " " + p.getMarque(), this.WIDTH);
                        for (int i = 0; i < nameLines.size(); ++i) {
                            String lineName = nameLines.get(i);
                            printer.writeLF(lineName);
                            System.out.println(lineName);
                            if (i != nameLines.size() - 1) {
                                continue;
                            }
                            double unitPrice = item.getPrixUnit();
                            double lineTotal = item.getMontantUsd();
                            Mesure m = item.getMesureId();
                            String priceStr = moneyFormat.format(unitPrice);
                            String qtyStr = "x" + item.getQuantite() + " " + m.getDescription();
                            String totalStr = moneyFormat.format(lineTotal);
                            int leftLen = this.WIDTH - (priceStr.length() + qtyStr.length() + totalStr.length() + 2);
                            String leftPad = " ".repeat(Math.max(0, leftLen));
                            String line = leftPad + priceStr + " " + qtyStr + " " + totalStr;
                            System.out.println(line);
                            printer.writeLF(body, line);
                        }
                        grandTotal += item.getMontantUsd();
                    }
                    double convertedTotal = grandTotal;
                    double convertedPaid = amountPaid;
                    double convertedReste = convertedTotal - convertedPaid;
                    printer.writeLF("-".repeat(this.WIDTH));
                    printer.writeLF(pied, this.printLine("TOTAL:", convertedTotal, currency, moneyFormat));
                    if (isUSD) {
                        printer.writeLF(pied, this.printLine("PAY\u00c9:", convertedPaid, currency, moneyFormat));
                        if (convertedReste > 0.01) {
                            printer.writeLF(pied, this.printLine("RESTE \u00c0 PAYER:", convertedReste, currency, moneyFormat));
                        } else if (convertedReste < -0.01) {
                            printer.writeLF(pied, this.printLine("TROP PAY\u00c9:", -convertedReste, currency, moneyFormat));
                        }
                    }
                    printer.writeLF(pied, "Operateur: " + this.user);
                    boolean isavert = this.pref.getBoolean("averti", true);
                    if (isavert) {
                        printer.feed(1);
                        String text = "CHER CLIENT S.V.P,VEUILLEZ VERIFIER VOS PRODUITS A LA RECEPTION PAS DES RECLAMATIONS APRES";
                        printer.writeLF(centerbold, this.pref.get("ads_mesg", text));
                    }
                    printer.writeLF(ephone, this.messageForCustomer);
                    QRCode qrcode = new QRCode();
                    printer.feed(1);
                    qrcode.setSize(2);
                    qrcode.setJustification(EscPosConst.Justification.Center);
                    printer.write((BarCodeWrapperInterface) qrcode, "https://www.endeleya.com");
                    printer.feed(2);
                    printer.cut(EscPos.CutMode.FULL);
                }
            } catch (Exception exception) {
                // empty catch block
            }
        });
    }

    public static List<String> wrapText(String text, int width) {
        ArrayList<String> lines = new ArrayList<String>();
        while (text.length() > width) {
            int breakPoint = text.lastIndexOf(32, width);
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
        int pad = (this.WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + text;
    }

    public String printLine(String label, double amount, String currency, DecimalFormat formatter) {
        String amountStr = formatter.format(currency.equals("USD") ? amount : (double) Math.round(amount)) + " " + currency;
        int spaces = this.WIDTH - label.length() - amountStr.length();
        String line = label + " ".repeat(Math.max(0, spaces)) + amountStr;
        System.out.println(line);
        return line;
    }

    public void printQrCodeAscii(String data) {
        System.out.println("QRCODE ICI\n" + data);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.initPref();
        MainUI.setPattern(dpk_date_vente);
        this.cbx_payment_mode.setItems(FXCollections.observableArrayList(new String[]{TypeTraisorerie.CAISSE.name(), TypeTraisorerie.BANQUE.name(), "MOBILE MONEY", Mouvment.CREDIT.name(), Mouvment.CREDIT.name() + "+" + Mouvment.CASH.name()}));
        this.cbx_payment_mode.getSelectionModel().selectFirst();
        dpk_date_vente.setValue(LocalDate.now());
        this.maker.setTaux(this.taux2change);
        this.captioncdf.setText(this.maker.getInverseCurrencyCode());
        this.captionusd.setText(this.maker.getMainCurrency());
        this.refreshSerialPrinters();
    }

    public void setEntreprise(Entreprise e) {
        String idnat;
        this.entreprise = e;
        if (this.entreprise == null) {
            return;
        }
        this.client = ClientDelegate.findAnonymousClient();
        this.txt_bill_companyname.setText(this.entreprise.getNomEntreprise());
        this.txt_comp_adresse.setText(this.entreprise.getAdresse());
        this.txt_bill_comp_id.setText("RCCM : " + this.entreprise.getIdentification());
        this.txt_bill_contact_entreprise.setText(this.entreprise.getEmail());
        String imp = this.entreprise.getNumeroImpot() == null ? "Aucun" : this.entreprise.getNumeroImpot();
        String string = idnat = this.entreprise.getIdNat() == null ? "Aucun" : this.entreprise.getIdNat();
        this.txt_bill_comp_idnat.setText((String) (!imp.equals("Aucun") && !idnat.equals("Aucun") ? "Imp\u00f4t:" + imp + " , IdNat:" + idnat : (!imp.equals("Aucun") && idnat.equals("Aucun") ? "Imp\u00f4t:" + imp : (imp.equals("Aucun") && !idnat.equals("Aucun") ? "IdNat:" + idnat : " "))));
        this.txt_comp_adresse_tel.setText("Tel:" + this.entreprise.getPhones());
        this.clients = FXCollections.observableArrayList((Collection) ClientDelegate.findClients());
        this.comptes = FXCollections.observableArrayList((Collection) CompteTresorDelegate.findCompteTresors((String) this.region));
        this.cbx_clients.setItems(this.clients);
        this.cbx_comptes.setItems(this.comptes);
        this.f = FileUtils.pointFile((String) (this.entreprise.getUid() + ".png"));
        if (!this.f.exists()) {
            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            FileUtils.streamTofile((InputStream) is);
        }
        Image image = null;
        try {
            image = new Image((InputStream) new FileInputStream(this.f));
            this.img_vu_logo.setImage(image);
            Util.centerImage((ImageView) this.img_vu_logo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ComboBoxAutoCompletion(this.cbx_clients);
        new ComboBoxAutoCompletion(this.cbx_comptes);
    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList rst = FXCollections.observableArrayList();
        for (Printer p : osp) {
            rst.add(p);
        }
        return rst;
    }

    public void setClient(Client c) {
        this.client = c == null ? ClientDelegate.findAnonymousClient() : c;
        this.tf_phone_client.setText(this.client.getPhone());
        this.txt_client_selected_pay.setText(this.client.getNomClient());
        this.cbx_clients.setValue(this.client);
    }

    @FXML
    private void pickClient(Event evt) {
        String token = this.pref.get("token", null);
        MainUI.floatDialog((String) "client.fxml", (int) 1090, (int) 537, (String) token, (Kazisafe) this.kazisafe, (Object[]) new Object[]{this.entreprise, this.region});
    }

    public void setLines(List<LigneVente> lig, Vente invoice) {
        Vente invoices;
        String token = this.pref.get("token", null);
        this.messageForCustomer = this.pref.get("mesc", "Les marchandises vendues ne sont ni reprises ni echangees");
        this.kazisafe = KazisafeServiceFactory.createService((String) token);
        this.venteItems = new ArrayList<LigneVente>();
        Vente invoiceId = null;
        if (invoice != null && (invoices = VenteDelegate.findVente((int) invoice.getUid())) != null) {
            invoiceId = invoices;
        }
        List<LigneVente> lgvt = invoiceId == null ? lig : LigneVenteDelegate.findByReference(invoiceId.getUid());
        List<LigneVente> lignes = this.sortByPriceLength(lgvt);
        if (invoiceId != null && invoiceId.getObservation().equals("Drafted")) {
            lignes = lig;
        }
        double sommed = lignes.stream().mapToDouble(l -> l.getMontantUsd()).sum();
        double sommef = lignes.stream().mapToDouble(l -> l.getMontantCdf()).sum();
        final double tot = lignes.stream().mapToDouble(l -> l.getMontantUsd()).sum();
        if (this.maker.isCdf()) {
            this.tf_nominal_recu_cdf.setVisible(false);
            this.txt_eval_sum_cdf.setVisible(false);
            this.tf_arembourser_cdf.setVisible(false);
        } else {
            this.tf_nominal_recu_cdf.setVisible(true);
            this.txt_eval_sum_cdf.setVisible(true);
            this.tf_arembourser_cdf.setVisible(true);
        }
        this.cdf = sommef;
        this.revertUsd = this.usd = sommed;
        this.revertCdf = this.cdf;
        this.txt_bill_somme_facture.setText("CDF : " + Math.round(sommef));
        this.txt_bill_somme_credit.setText("CDF : " + Math.round(sommef));
        this.sumCopy = sommed;
        this.txt_eval_sum_usd.setText(String.valueOf(BigDecimal.valueOf(sommed).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
        this.txt_eval_sum_cdf.setText(String.valueOf(Math.round(sommef)));
        if (this.maker.isUsd()) {
            this.cdf = sommed * this.taux2change;
            this.revertUsd = this.usd = sommed;
            this.revertCdf = this.cdf;
            this.txt_bill_somme_facture.setText("USD : " + tot);
            this.txt_bill_somme_credit.setText("USD : " + tot);
            this.sumCopy = sommed;
            this.txt_eval_sum_usd.setText(String.valueOf(sommed));
            this.txt_eval_sum_cdf.setText(String.valueOf(BigDecimal.valueOf(this.cdf).setScale(0, RoundingMode.HALF_EVEN).doubleValue()));
        }
        this.venteItems.clear();
        this.venteItems.addAll(lignes);
        int ref = 0;
        if (invoiceId == null) {
            String time = String.valueOf(System.currentTimeMillis());
            String lvid = String.valueOf(DataId.generateInt()).concat(time.substring(time.length() - 2, time.length()));
            ref = Integer.parseInt(lvid);
            this.pane_bill_cash_paid.setVisible(false);
            this.txt_bill_cash_paid.setVisible(false);
        } else {
            if (!invoiceId.getObservation().equals("Drafted")) {
                this.tf_nominal_recu_usd.setText(String.valueOf(invoiceId.getMontantUsd()));
                this.tf_nominal_recu_cdf.setText(String.valueOf(Math.round(invoiceId.getMontantCdf())));
            }
            if (invoiceId.getPayment().toUpperCase().contains("credit partiel".toUpperCase())) {
                this.pane_bill_cash_paid.setVisible(true);
                this.txt_bill_cash_paid.setVisible(true);
                this.txt_bill_somme_credit.setText(String.valueOf(invoiceId.getMontantDette()));
                this.txt_bill_somme_facture.setText(String.valueOf(invoiceId.getMontantUsd() + invoiceId.getMontantCdf() / this.taux2change));
                this.pane_bill_sum_credit.setVisible(true);
                this.txt_lbl_credit.setVisible(true);
            } else if (invoiceId.getPayment().contains("Cash") | invoiceId.getPayment().contains(TypeTraisorerie.ELECTRONIQUE.name()) | invoiceId.getPayment().contains("Banque")) {
                this.pane_bill_cash_paid.setVisible(true);
                this.txt_bill_cash_paid.setVisible(true);
                this.pane_bill_sum_credit.setVisible(false);
                this.txt_lbl_credit.setVisible(false);
            } else {
                this.pane_bill_cash_paid.setVisible(false);
                this.txt_bill_cash_paid.setVisible(false);
                this.txt_bill_somme_credit.setText(String.valueOf(invoiceId.getMontantDette()));
                this.pane_bill_sum_credit.setVisible(true);
                this.txt_lbl_credit.setVisible(true);
            }
            ref = invoiceId.getUid();
            Client clt = ClientDelegate.findClient((String) invoiceId.getClientId().getUid());
            this.txt_nom_client1.setText("Tel : " + (clt.getPhone().length() < 8 ? "..." : clt.getPhone()));
            this.txt_nom_client.setText("Client : " + clt.getNomClient());
        }
        this.vente4save = new Vente(ref);
        int tbil = this.pref.getInt("tranzit_bill", -100);
        this.tf_nominal_recu_usd.textProperty().addListener((observable, oldValue, newValue) -> {
            double in_usd = Double.parseDouble(newValue.isEmpty() ? "0" : newValue);
            if (newValue.isEmpty() && this.tf_nominal_recu_cdf.getText().isEmpty()) {
                this.dt = this.usd;
                this.ff = 0.0;
                this.fd = 0.0;
                this.txt_eval_sum_usd.setText(String.valueOf(this.usd));
                this.txt_eval_sum_cdf.setText(String.valueOf(this.cdf));
                this.tf_arembourser_usd.setText("0");
                this.tf_arembourser_cdf.setText("0");
                this.vente4save.setMontantUsd(0.0);
                this.vente4save.setMontantCdf(0.0);
            } else if (!newValue.isEmpty() && this.tf_nominal_recu_cdf.getText().isEmpty()) {
                double restUsd = new BigDecimal(this.usd - in_usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                double d = restCdf = this.maker.isUsd() ? this.maker.usdToCdf(restUsd) : this.maker.cdfToUsd(restUsd);
                if (restUsd >= 0.0) {
                    this.txt_eval_sum_usd.setText(String.valueOf(restUsd));
                    this.txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                    this.dt = restUsd;
                    this.fd = in_usd;
                    this.ff = 0.0;
                    this.tf_arembourser_cdf.setText("0");
                    this.tf_arembourser_usd.setText("0");
                } else {
                    double retour = Math.abs(restUsd);
                    this.fd = in_usd - retour;
                    this.dt = 0.0;
                    this.ff = 0.0;
                    this.txt_eval_sum_usd.setText("0");
                    this.txt_eval_sum_cdf.setText("0.0");
                    this.tf_arembourser_usd.setText("" + retour);
                    this.tf_arembourser_cdf.setText("" + new BigDecimal(Math.round(retour * this.taux2change)).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                }
                if (this.maker.isUsd()) {
                    this.vente4save.setMontantCdf(0.0);
                    this.vente4save.setMontantUsd(this.fd);
                } else if (this.maker.isCdf()) {
                    this.vente4save.setMontantCdf(this.fd);
                    this.vente4save.setMontantUsd(0.0);
                }
            } else if (newValue.isEmpty() && !this.tf_nominal_recu_cdf.getText().isEmpty()) {
                double restUsd;
                if (!StringUtils.isNumeric((CharSequence) this.tf_nominal_recu_cdf.getText())) {
                    return;
                }
                inCdf = Double.parseDouble(this.tf_nominal_recu_cdf.getText());
                restCdf = new BigDecimal(this.cdf - inCdf).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                double d = restUsd = this.maker.isUsd() ? this.maker.cdfToUsd(restCdf) : this.maker.usdToCdf(restCdf);
                if (restCdf >= 0.0) {
                    this.txt_eval_sum_usd.setText(String.valueOf(restUsd));
                    this.txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                    this.dt = restUsd;
                    this.fd = in_usd;
                    this.ff = inCdf;
                    this.tf_arembourser_cdf.setText("0.0");
                    this.tf_arembourser_usd.setText("0.0");
                } else {
                    double retour = Math.abs(restCdf);
                    this.fd = 0.0;
                    this.dt = 0.0;
                    this.ff = inCdf - retour;
                    this.txt_eval_sum_usd.setText("0.0");
                    this.txt_eval_sum_cdf.setText("0.0");
                    this.tf_arembourser_cdf.setText("" + retour);
                    this.tf_arembourser_usd.setText("" + new BigDecimal(retour / this.taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                }
                if (this.maker.isUsd()) {
                    this.vente4save.setMontantCdf((double) Math.round(this.ff));
                    this.vente4save.setMontantUsd(0.0);
                } else if (this.maker.isCdf()) {
                    this.vente4save.setMontantCdf(0.0);
                    this.vente4save.setMontantUsd(this.ff);
                }
            } else {
                double restCdf;
                if (!StringUtils.isNumeric((CharSequence) this.tf_nominal_recu_cdf.getText())) {
                    return;
                }
                inCdf = Double.parseDouble(this.tf_nominal_recu_cdf.getText());
                double converted = this.maker.isUsd() ? this.maker.cdfToUsd(inCdf) : this.maker.usdToCdf(inCdf);
                double nwInUsd = in_usd + converted;
                double restUsd = new BigDecimal(this.usd - nwInUsd).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                double d = restCdf = this.maker.isUsd() ? this.maker.usdToCdf(restUsd) : this.maker.cdfToUsd(restUsd);
                if (restUsd >= 0.0) {
                    this.txt_eval_sum_usd.setText(String.valueOf(Math.round(restUsd)));
                    this.txt_eval_sum_cdf.setText(String.valueOf(Math.round(restCdf)));
                    this.dt = restUsd;
                    this.fd = in_usd;
                    this.ff = inCdf;
                    this.tf_arembourser_cdf.setText("");
                    this.tf_arembourser_usd.setText("");
                } else {
                    double retour = Math.abs(restUsd);
                    this.fd = nwInUsd - retour;
                    this.dt = 0.0;
                    this.ff = 0.0;
                    this.txt_eval_sum_usd.setText("0.0");
                    this.txt_eval_sum_cdf.setText("0.0");
                    this.tf_arembourser_usd.setText("" + retour);
                    this.tf_arembourser_cdf.setText("" + (this.maker.isUsd() ? this.maker.usdToCdf(retour) : this.maker.cdfToUsd(retour)));
                }
                if (this.maker.isUsd()) {
                    this.vente4save.setMontantUsd(this.fd);
                } else if (this.maker.isCdf()) {
                    this.vente4save.setMontantCdf(this.fd);
                }
            }
            double debt = Double.parseDouble(this.txt_eval_sum_usd.getText());
            this.vente4save.setDeviseDette("USD");
            this.vente4save.setMontantDette(Double.valueOf(this.maker.isUsd() ? debt : debt / this.taux2change));
            if (debt > 0.0) {
                if (this.tf_nominal_recu_usd.getText().isEmpty()) {
                    this.cbx_payment_mode.getSelectionModel().select(3);
                    this.pane_bill_cash_paid.setVisible(false);
                    this.txt_bill_cash_paid.setVisible(false);
                } else {
                    double sin = Double.parseDouble(this.tf_nominal_recu_usd.getText());
                    this.txt_bill_somme_facture.setText(sin > tot ? String.valueOf(tot) : String.valueOf(sin));
                    this.pane_bill_cash_paid.setVisible(true);
                    this.txt_bill_cash_paid.setVisible(true);
                    this.cbx_payment_mode.getSelectionModel().select(4);
                }
                this.dpk_echeance_debt.setDisable(false);
                this.txt_bill_somme_credit.setText(String.valueOf(debt));
                this.pane_bill_sum_credit.setVisible(true);
                this.txt_lbl_credit.setVisible(true);
            } else {
                this.txt_bill_somme_facture.setText(String.valueOf(tot));
                this.dpk_echeance_debt.setDisable(true);
                this.cbx_payment_mode.getSelectionModel().select(0);
                this.pane_bill_sum_credit.setVisible(false);
                this.txt_lbl_credit.setVisible(false);
            }
        });
        this.tf_nominal_recu_cdf.textProperty().addListener((observable, oldValue, newValue) -> {
            double in_cdf = Double.parseDouble(newValue.isEmpty() ? "0" : newValue);
            if (newValue.isEmpty() && this.tf_nominal_recu_usd.getText().isEmpty()) {
                this.txt_eval_sum_cdf.setText(String.valueOf(new BigDecimal(Math.round(this.cdf)).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                this.txt_eval_sum_usd.setText(String.valueOf(new BigDecimal(this.usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue()));
                this.dt = this.usd;
                this.ff = 0.0;
                this.fd = 0.0;
                this.tf_arembourser_usd.setText("0.0");
                this.tf_arembourser_cdf.setText("0.0");
                this.vente4save.setMontantCdf(0.0);
                this.vente4save.setMontantUsd(0.0);
            } else if (!newValue.isEmpty() && this.tf_nominal_recu_usd.getText().isEmpty()) {
                double restUsd;
                double restCdf = new BigDecimal(Math.round(this.cdf - in_cdf)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                double d = restUsd = this.maker.isUsd() ? this.maker.cdfToUsd(restCdf) : this.maker.usdToCdf(restCdf);
                if (restCdf >= 0.0) {
                    this.txt_eval_sum_usd.setText(String.valueOf(restUsd));
                    this.txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                    this.dt = restUsd;
                    this.fd = 0.0;
                    this.ff = in_cdf;
                    this.tf_arembourser_cdf.setText("");
                    this.tf_arembourser_usd.setText("");
                } else {
                    double retour = Math.abs(restCdf);
                    this.fd = 0.0;
                    this.dt = 0.0;
                    this.ff = in_cdf - retour;
                    this.txt_eval_sum_usd.setText("0.0");
                    this.txt_eval_sum_cdf.setText("0.0");
                    this.tf_arembourser_cdf.setText("" + retour);
                    this.tf_arembourser_usd.setText("" + new BigDecimal(retour / this.taux2change).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                }
                if (this.maker.isUsd()) {
                    this.vente4save.setMontantCdf(this.ff);
                    this.vente4save.setMontantUsd(0.0);
                } else if (this.maker.isCdf()) {
                    this.vente4save.setMontantCdf(0.0);
                    this.vente4save.setMontantUsd(this.ff);
                }
            } else if (newValue.isEmpty() && !this.tf_nominal_recu_usd.getText().isEmpty()) {
                double restCdf;
                if (!StringUtils.isNumeric((CharSequence) this.tf_nominal_recu_usd.getText())) {
                    return;
                }
                double in_usd = this.maker.isCdf() ? Double.parseDouble(this.tf_nominal_recu_cdf.getText()) : Double.parseDouble(this.tf_nominal_recu_usd.getText());
                double restUsd = new BigDecimal(this.usd - in_usd).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                double d = restCdf = this.maker.isCdf() ? restUsd : (double) Math.round(this.maker.usdToCdf(restUsd));
                if (restUsd >= 0.0) {
                    this.txt_eval_sum_usd.setText(String.valueOf(restUsd));
                    this.txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                    this.dt = restUsd;
                    this.fd = in_usd;
                    this.ff = 0.0;
                    this.tf_arembourser_cdf.setText("");
                    this.tf_arembourser_usd.setText("");
                } else {
                    double retour = Math.abs(restUsd);
                    this.fd = in_usd - retour;
                    this.dt = 0.0;
                    this.ff = 0.0;
                    this.txt_eval_sum_usd.setText("0.0");
                    this.txt_eval_sum_cdf.setText("0.0");
                    this.tf_arembourser_usd.setText("" + retour);
                    this.tf_arembourser_cdf.setText("" + (this.maker.isUsd() ? this.maker.usdToCdf(retour) : this.maker.cdfToUsd(retour)));
                }
                if (this.maker.isUsd()) {
                    this.vente4save.setMontantCdf(0.0);
                    this.vente4save.setMontantUsd(this.fd);
                } else if (this.maker.isCdf()) {
                    this.vente4save.setMontantCdf(this.fd);
                    this.vente4save.setMontantUsd(0.0);
                }
            } else {
                double restUsd;
                if (!StringUtils.isNumeric((CharSequence) this.tf_nominal_recu_usd.getText())) {
                    return;
                }
                double inUsd = Double.parseDouble(this.tf_nominal_recu_usd.getText());
                double converted = this.maker.isUsd() ? this.maker.usdToCdf(inUsd) : this.maker.cdfToUsd(inUsd);
                double nwInCdf = in_cdf + converted;
                double restCdf = new BigDecimal(this.cdf - nwInCdf).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                double d = restUsd = this.maker.isUsd() ? this.maker.cdfToUsd(restCdf) : this.maker.usdToCdf(restCdf);
                if (restCdf >= 0.0) {
                    this.txt_eval_sum_usd.setText(String.valueOf(restUsd));
                    this.txt_eval_sum_cdf.setText(String.valueOf(restCdf));
                    this.dt = restUsd;
                    this.ff = in_cdf;
                    this.fd = inUsd;
                    this.tf_arembourser_cdf.setText("0.0");
                    this.tf_arembourser_usd.setText("0.0");
                } else {
                    double retour = Math.abs(restCdf);
                    this.fd = 0.0;
                    this.dt = 0.0;
                    this.ff = nwInCdf - retour;
                    this.txt_eval_sum_usd.setText("0.0");
                    this.txt_eval_sum_cdf.setText("0.0");
                    this.tf_arembourser_cdf.setText("" + retour);
                    this.tf_arembourser_usd.setText("" + (this.maker.isUsd() ? this.maker.cdfToUsd(retour) : this.maker.usdToCdf(retour)));
                }
                if (this.maker.isUsd()) {
                    this.vente4save.setMontantCdf(this.ff);
                } else if (this.maker.isCdf()) {
                    this.vente4save.setMontantUsd(this.ff);
                }
            }
            String dev = this.pref.get("mainCur", "USD");
            double debt = this.maker.isUsd() ? Double.parseDouble(this.txt_eval_sum_usd.getText()) : Double.parseDouble(this.txt_eval_sum_cdf.getText());
            this.vente4save.setDeviseDette(dev);
            this.vente4save.setMontantDette(Double.valueOf(debt));
            if (debt > 0.0) {
                if (this.tf_nominal_recu_cdf.getText().isEmpty()) {
                    this.cbx_payment_mode.getSelectionModel().select(3);
                    this.pane_bill_cash_paid.setVisible(false);
                    this.txt_bill_cash_paid.setVisible(false);
                } else {
                    double sin = Double.parseDouble(this.tf_nominal_recu_cdf.getText());
                    this.txt_bill_somme_facture.setText(BigDecimal.valueOf(sin / this.taux2change).setScale(2, 6).doubleValue() > tot ? String.valueOf(tot) : String.valueOf(BigDecimal.valueOf(sin / this.taux2change).setScale(2, 6).doubleValue()));
                    this.cbx_payment_mode.getSelectionModel().select(4);
                    this.pane_bill_cash_paid.setVisible(true);
                    this.txt_bill_cash_paid.setVisible(true);
                }
                this.dpk_echeance_debt.setDisable(false);
                this.txt_bill_somme_credit.setText(String.valueOf(debt));
                this.pane_bill_sum_credit.setVisible(true);
                this.txt_lbl_credit.setVisible(true);
            } else {
                this.dpk_echeance_debt.setDisable(true);
                this.txt_bill_somme_facture.setText(String.valueOf(tot));
                this.cbx_payment_mode.getSelectionModel().select(0);
                this.pane_bill_sum_credit.setVisible(false);
                this.txt_lbl_credit.setVisible(false);
            }
        });
        String reference = switch (this.count_logic) {
            case 1 -> {
                String leo = Constants.DATE_ONLY_FORMAT.format(new Date());
                String conu = this.pref.get("_time_bill", "-1");
                if (conu.equals(leo)) {
                    this.compteur = this.pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        ++this.compteur;
                    }
                } else {
                    this.pref.put("_time_bill", leo);
                    if (tbil == -100) {
                        this.compteur = 1;
                    }
                }
                yield String.format("%06d", this.compteur);
            }
            case 2 -> {
                String mois = Constants.YEAR_AND_MONTH_FORMAT.format(new Date());
                String conu = this.pref.get("_time_bill", "-1");
                if (conu.equals(mois)) {
                    this.compteur = this.pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        ++this.compteur;
                    }
                } else {
                    this.pref.put("_time_bill", mois);
                    if (tbil == -100) {
                        this.compteur = 1;
                    }
                }
                yield String.format("%06d", this.compteur);
            }
            case 3 -> {
                String mois = Constants.YEAR_ONLY_FORMAT.format(new Date());
                String conu = this.pref.get("_time_bill", "-1");
                if (conu.equals(mois)) {
                    this.compteur = this.pref.getInt("_bill_counter_", 0);
                    if (tbil == -100) {
                        ++this.compteur;
                    }
                } else {
                    this.pref.put("_time_bill", mois);
                    if (tbil == -100) {
                        this.compteur = 1;
                    }
                }
                yield String.format("%06d", this.compteur);
            }
            case 4 -> {
                this.compteur = this.pref.getInt("_bill_counter_", 0);
                if (tbil == -100) {
                    ++this.compteur;
                }
                yield String.format("%08d", this.compteur);
            }
            default ->
                String.valueOf(ref);
        };
        this.txt_reference_vente.setText(invoiceId == null ? "#" + reference : "#" + invoiceId.getReference());
        this.vente4save.setReference(reference);
        this.txt_bill_num_facture.setText(invoiceId == null ? "#" + reference : "#" + invoiceId.getReference());
        this.user = this.pref.get("operator", "User");
        this.txt_bill_user.setText("Agent : " + this.user);
        this.vente4save.setPayment("Paiement Cash");
        this.chbx_print_receipt.setSelected(this.print);
        this.chbx_print_receipt.selectedProperty().addListener((ChangeListener) new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                PaymentController.this.pref.putBoolean("print", newValue);
            }
        });
        this.configBillTable();
        this.cbx_payment_mode.getSelectionModel().selectedItemProperty().addListener((ChangeListener) new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.contains("CAISSE")) {
                    vente4save.setPayment("Paiement Cash");
                    dpk_echeance_debt.setDisable(true);
                    if (!txt_eval_sum_usd.getText().equals("0.0") && !txt_eval_sum_cdf.getText().equals("0.0")) {
                        txt_reduction.setVisible(true);
                        double dette = Double.parseDouble(txt_eval_sum_usd.getText());
                        double pred = BigDecimal.valueOf(dette / tot * 100.0).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                        txt_lbl_credit.setText("R\u00e9duction : (" + pred + "%)");
                        txt_reduction.setText("R\u00e9duction : (" + pred + "%)");
                        vente4save.setMontantDette(Double.valueOf(0.0));
                    }
                } else if (newValue.contains("BANQUE")) {
                    vente4save.setPayment("Paiement par Banque");
                    dpk_echeance_debt.setDisable(true);
                    if (!txt_eval_sum_usd.getText().equals("0.0") && !txt_eval_sum_cdf.getText().equals("0.0")) {
                        txt_reduction.setVisible(true);
                        double dette = Double.parseDouble(txt_eval_sum_usd.getText());
                        double pred = BigDecimal.valueOf(dette / tot * 100.0).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                        txt_lbl_credit.setText("R\u00e9duction : (" + pred + "%)");
                        txt_reduction.setText("R\u00e9duction : (" + pred + "%)");
                        vente4save.setMontantDette(Double.valueOf(0.0));
                    }
                } else if (newValue.equals("CREDIT")) {
                    vente4save.setPayment("Paiement a credit");
                    dpk_echeance_debt.setDisable(false);
                    txt_reduction.setVisible(false);
                    txt_lbl_credit.setText("Reste \u00e0 payer : ");
                    double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                    vente4save.setMontantDette(Double.valueOf(debt));
                } else if (newValue.equals("MOBILE MONEY")) {
                    dpk_echeance_debt.setDisable(true);
                    vente4save.setPayment(TypeTraisorerie.ELECTRONIQUE.name());
                    if (!txt_eval_sum_usd.getText().equals("0.0") && !txt_eval_sum_cdf.getText().equals("0.0")) {
                        txt_reduction.setVisible(true);
                        double dette = Double.parseDouble(txt_eval_sum_usd.getText());
                        double pred = BigDecimal.valueOf(dette / tot * 100.0).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                        txt_lbl_credit.setText("R\u00e9duction : (" + pred + "%)");
                        txt_reduction.setText("R\u00e9duction : (" + pred + "%)");
                        vente4save.setMontantDette(Double.valueOf(0.0));
                    }
                } else if (newValue.equals("CREDIT+CASH")) {
                    vente4save.setPayment("Paiement Credit partiel");
                    dpk_echeance_debt.setDisable(false);
                    txt_reduction.setVisible(false);
                    txt_lbl_credit.setText("Reste \u00e0 payer : ");
                    double debt = Double.parseDouble(txt_eval_sum_usd.getText());
                    vente4save.setMontantDette(Double.valueOf(debt));
                }
            }
        });
        this.tf_phone_client.textProperty().addListener((ChangeListener) new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                PaymentController.this.txt_nom_client1.setText("Tel : " + (newValue.isEmpty() ? "..." : (newValue.length() < 7 ? "NA" : newValue)));
            }
        });
        this.cliname.textProperty().addListener((ChangeListener) new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                PaymentController.this.txt_nom_client.setText("Client : " + (newValue.isEmpty() ? "..." : newValue));
            }
        });
        this.cbx_clients.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.client = newValue;
                this.cliname.setText(this.client.getNomClient());
                this.tf_phone_client.setText(this.client.getPhone().length() > 7 ? this.client.getPhone() : "");
                this.typecli = this.client.getTypeClient();
                if (this.client.getTypeClient().equalsIgnoreCase("#3")) {
                    this.tflibelle.setPromptText("Entrer le numero de bon");
                    this.tf_nominal_recu_cdf.clear();
                    this.tf_nominal_recu_usd.clear();
                }
            }
        });
        if (lignes == null) {
            return;
        }
        this.venteItems.clear();
        this.venteItems.addAll(lignes);
        this.tbl_bill_products.setItems(FXCollections.observableArrayList((Collection) lignes));
        this.tbl_bill_products.setFixedCellSize(25.0);
        this.tbl_bill_products.prefHeightProperty().bind((ObservableValue) this.tbl_bill_products.fixedCellSizeProperty().multiply((ObservableNumberValue) Bindings.size((ObservableList) this.tbl_bill_products.getItems()).add(1.01)));
        this.tbl_bill_products.minHeightProperty().bind((ObservableValue) this.tbl_bill_products.prefHeightProperty());
        this.tbl_bill_products.maxHeightProperty().bind((ObservableValue) this.tbl_bill_products.prefHeightProperty());
        if (invoiceId == null) {
            this.txt_bill_date_vente.setText(LocalDateTime.now().toString());
        } else {
            this.txt_bill_date_vente.setText(invoiceId.getDateVente().toString());
        }
        this.txt_bill_num_facture.setText("Facture #" + (invoiceId == null ? this.vente4save.getReference() : invoiceId.getReference()));
        this.pane_invoiced.prefHeightProperty().bind((ObservableValue) this.tbl_bill_products.maxHeightProperty().add((ObservableNumberValue) Bindings.size((ObservableList) this.tbl_bill_products.getItems()).add(300)));
        this.pane_invoiced.minHeightProperty().bind((ObservableValue) this.pane_invoiced.prefHeightProperty());
        this.pane_invoiced.maxHeightProperty().bind((ObservableValue) this.pane_invoiced.prefHeightProperty());
        this.billbed.prefHeightProperty().bind((ObservableValue) this.pane_invoiced.maxHeightProperty().add(89));
        this.billbed.minHeightProperty().bind((ObservableValue) this.billbed.prefHeightProperty());
        this.billbed.maxHeightProperty().bind((ObservableValue) this.billbed.prefHeightProperty());
        MainUI.setPattern((DatePicker) this.dpk_echeance_debt);
        this.cbx_clients.setConverter((StringConverter) new StringConverter<Client>() {

            @Override
            public String toString(Client object) {
                return object == null ? null : object.getNomClient() + " " + (object.getPhone() == null ? "" : (object.getPhone().length() < 8 ? "" : object.getPhone()));
            }

            @Override
            public Client fromString(String string) {
                return PaymentController.this.cbx_clients.getItems().stream().filter(v -> (v.getNomClient() + " " + v.getPhone()).equalsIgnoreCase(string)).findFirst().orElse(null);
            }
        });
        this.cbx_printers.setConverter((StringConverter) new StringConverter<Printer>() {

            public String toString(Printer object) {
                return object == null ? null : object.getName();
            }

            public Printer fromString(String string) {
                return PaymentController.this.cbx_printers.getItems().stream().filter(v -> v.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
            }
        });
        this.cbx_comptes.setConverter((StringConverter) new StringConverter<CompteTresor>() {

            public String toString(CompteTresor object) {
                return object == null ? null : object.getTypeCompte() + " " + object.getBankName() + " " + object.getNumeroCompte();
            }

            public CompteTresor fromString(String string) {
                return PaymentController.this.cbx_comptes.getItems().stream().filter(obj -> (obj.getTypeCompte() + " " + obj.getBankName() + " " + obj.getNumeroCompte()).equalsIgnoreCase(string)).findFirst().orElse(null);
            }
        });
        this.cbx_comptes.getSelectionModel().select(0);
        this.cbx_comptes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.choosenComptTr = newValue;
            }
        });
        this.choosenComptTr = (CompteTresor) this.cbx_comptes.getValue();
        ObservableSet osp = Printer.getAllPrinters();
        System.out.println("Printewrs count " + osp.size());
        this.cbx_printers.setItems(this.setToList((ObservableSet<Printer>) osp));
        String prn = this.pref.get("def-printer", null);
        this.defaultPrinter = this.getPrinterByName(prn);
        if (this.defaultPrinter == null) {
            this.defaultPrinter = Printer.getDefaultPrinter();
        }
        this.cbx_printers.getSelectionModel().select(this.defaultPrinter);
        this.cbx_printers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.defaultPrinter = newVal;
                this.pref.put("def-printer", this.defaultPrinter.getName());
            }
        });
        this.kazisafe.getAbonnements().enqueue((Callback) new Callback<List<Abonnement>>() {

            public void onResponse(Call<List<Abonnement>> call, Response<List<Abonnement>> rspns) {
                if (rspns.isSuccessful()) {
                    List<Abonnement> abns = (List) rspns.body();
                    block10:
                    for (Abonnement abn : abns) {
                        String typeAb;
                        String etat = abn.getEtat();
                        switch (typeAb = abn.getTypeAbonnement()) {
                            case "Gold", "Metal", "Super gold" -> {
                                PaymentController.this.pref.put("type-sub", typeAb);
                                String status = SubscriptionUtil.computeStatus((Abonnement) abn);
                                Duration time = SubscriptionUtil.remainingDuration((Abonnement) abn);
                                if (time.minusDays(7L).isZero()) {
                                    MainUI.notify(null, (String) "Attention", (String) "Le cr\u00e9dit Kazisafe (Record) expire bient\u00f4t, pensez \u00e0 le renouveller", (long) 5L, (String) "warning");
                                }
                                long max = SubscriptionUtil.nextSubscriptionMillis((Abonnement) abn);
                                System.err.println("Abonnement total " + max + " rest " + time.toMillis());
                                PaymentController.this.pref.putDouble("sub", max);
                                PaymentController.this.pref.put("etat-sub", etat);
                                if (status.equals("Expiree")) {
                                    continue block10;
                                }
                                MainUI.notifySync((String) "Kazisafe-Abonnement", (String) ("Activation souscription " + typeAb + " faite avec succes"), (String) "Notification de souscription au service kazisafe");
                            }
                            case "PRO" -> {
                                double nombreOper = abn.getNombreOperation();
                                PaymentController.this.pref.put("pro-sub", typeAb);
                                PaymentController.this.pref.putDouble("subscripro", nombreOper);
                                PaymentController.this.pref.put("pro-etat", etat);
                                Platform.runLater(() -> PaymentController.this.txt_ebonus_remained.setText("eBonus restant: " + nombreOper + " clients"));
                            }
                        }
                    }
                }
            }

            public void onFailure(Call<List<Abonnement>> call, Throwable thrwbl) {
                System.err.println("No network");
            }
        });
    }

    private Printer getPrinterByName(String name) {
        if (name == null) {
            return null;
        }
        ObservableSet<Printer> printers = Printer.getAllPrinters();
        for (Printer printer : printers) {
            if (!printer.getName().equals(name)) {
                continue;
            }
            return printer;
        }
        return null;
    }

    private List<LigneVente> sortByPriceLength(List<LigneVente> lvx) {
        LigneVente[] lvs = this.ligneVenteToArray(lvx);
        for (int i = 0; i < lvs.length; ++i) {
            String p1 = String.valueOf(lvs[i].getPrixUnit());
            for (int x = 0; x < lvs.length; ++x) {
                String p2 = String.valueOf(lvs[x].getPrixUnit());
                if (p1.length() <= p2.length()) {
                    continue;
                }
                LigneVente tmp = lvs[i];
                lvs[i] = lvs[x];
                lvs[x] = tmp;
            }
        }
        return Arrays.asList(lvs);
    }

    public void configBillTable() {
        this.col_bill_designation.setCellValueFactory(param -> {
            LigneVente r = (LigneVente) param.getValue();
            Produit pr = ProduitDelegate.findProduit((String) r.getProductId().getUid());
            return new SimpleStringProperty(pr.getNomProduit() + " " + (pr.getMarque() == null ? "" : pr.getMarque()) + " " + (pr.getModele() == null ? "" : pr.getModele()) + " " + (pr.getTaille() == null ? "" : pr.getTaille()) + " " + (pr.getCouleur() == null ? "" : pr.getCouleur()));
        });
        this.col_bill_qte.setCellValueFactory(param -> {
            LigneVente r = (LigneVente) param.getValue();
            Mesure m = r.getMesureId();
            Mesure mzr = MesureDelegate.findMesure((String) m.getUid());
            return new SimpleStringProperty(r.getQuantite() + " " + mzr.getDescription());
        });
        this.col_bill_pu.setCellValueFactory(param -> {
            LigneVente r = (LigneVente) param.getValue();
            return new SimpleDoubleProperty(r.getPrixUnit().doubleValue());
        });
        this.col_bill_prix_unit.setCellValueFactory(param -> {
            LigneVente r = (LigneVente) param.getValue();
            return new SimpleDoubleProperty(r.getMontantUsd());
        });
    }

    @FXML
    public void printInvoice(Event e) {
        Vente vx = VenteDelegate.findVente((int) this.vente4save.getUid());
        if (vx == null && this.copies == 1) {
            MainUI.notify(null, (String) "Erreur", (String) "Impossible d'imprimer une vente non enregistr\u00e9e", (long) 4L, (String) "error");
            return;
        }
        if (this.chbx_print_thermal.isSelected()) {
            this.print();
        } else {
            List items = LigneVenteDelegate.findByReference(vx.getUid());
            if (this.defaultPrinter != null) {
                double paid = this.maker.isUsd() ? this.toUsd() : this.toCdf();
                Client cl = vx.getClientId();
                System.out.println("Printing...");
                String nomCl = this.cliname.getText().isEmpty() ? cl.getNomClient() : this.cliname.getText();
                this.printReceipt(this.defaultPrinter.getName(), this.entrepName, this.rccm, vx.getReference(), items, paid, nomCl, cl.getPhone(), this.maker.getMainCurrency(), this.taux2change);
            }
        }
    }

    private double toUsd() {
        double paidus = 0.0;
        double paidcd = 0.0;
        if (!this.tf_nominal_recu_usd.getText().isBlank()) {
            paidus = Double.parseDouble(this.tf_nominal_recu_usd.getText());
        }
        if (!this.tf_nominal_recu_cdf.getText().isBlank()) {
            paidcd = Double.parseDouble(this.tf_nominal_recu_cdf.getText()) / this.taux2change;
        }
        return paidus + paidcd;
    }

    private double toCdf() {
        double paidus = 0.0;
        double paidcd = 0.0;
        if (!this.tf_nominal_recu_usd.getText().isBlank()) {
            paidus = Double.parseDouble(this.tf_nominal_recu_usd.getText()) * this.taux2change;
        }
        if (!this.tf_nominal_recu_cdf.getText().isBlank()) {
            paidcd = Double.parseDouble(this.tf_nominal_recu_cdf.getText());
        }
        return Math.round(paidus + paidcd);
    }

    private void print() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> this.createPdfBill(this.entreprise, this.vente4save, this.client));
        exec.shutdown();
    }

    @FXML
    public void saveVente(Event et) {
        if (!((String) this.cbx_payment_mode.getValue()).equals("CREDIT") && this.choosenComptTr == null) {
            MainUI.notify(null, (String) "Erreur", (String) "Veuillez selectionner le compte de tresorerie puis continuer", (long) 4L, (String) "error");
            return;
        }
        List vxs = VenteDelegate.findByRef((String) this.vente4save.getReference(), (LocalDate) LocalDate.now());
        if (!vxs.isEmpty() && !((Vente) vxs.get(0)).getObservation().equals("Drafted")) {
            MainUI.notify(null, (String) "Erreur", (String) "La vente ayant le m\u00eame num\u00e9ro de facture existe d\u00e9j\u00e0", (long) 4L, (String) "error");
            return;
        }
        if (this.typecli != null && this.typecli.equals("#3") && this.tflibelle.getText().isEmpty()) {
            MainUI.notify(null, (String) "Erreur", (String) "Veuillez entrer le numero de bon de l'abonne puis reesayer", (long) 4L, (String) "error");
            return;
        }
        String type = this.pref.get("type-sub", " ");
        String dev = this.pref.get("mainCur", "USD");
        String pm = this.vente4save.getPayment();
        if (this.tf_nominal_recu_usd.getText().isEmpty() && this.tf_nominal_recu_cdf.getText().isEmpty()) {
            this.dpk_echeance_debt.setDisable(false);
            this.cbx_payment_mode.getSelectionModel().select(3);
        }
        if (this.dpk_echeance_debt.getValue() == null && ((String) this.cbx_payment_mode.getValue()).contains("CREDIT") && this.tf_nominal_recu_usd.getText().isEmpty() && this.tf_nominal_recu_cdf.getText().isEmpty()) {
            this.dpk_echeance_debt.setDisable(false);
            MainUI.notify(null, (String) "Erreur", (String) "Entrer la date valide de l'echeance de la dette", (long) 4L, (String) "erreur");
            return;
        }
        this.vente4save.setRegion(this.region);
        this.vente4save.setDeviseDette(this.maker.getMainCurrency());
        if (type.equalsIgnoreCase("Gold") || type.equalsIgnoreCase("Super Gold")) {
            this.vente4save.setObservation("YES");
        } else {
            this.vente4save.setObservation("NON");
        }
        if (((String) this.cbx_payment_mode.getValue()).equals("CREDIT+CASH")) {
            if (this.dpk_echeance_debt.getValue() == null || this.tf_phone_client.getText().isEmpty() || this.cliname.getText().isEmpty()) {
                MainUI.notify(null, (String) "Erreur", (String) "La date de l'\u00e9ch\u00e9ance, le t\u00e9l\u00e9phone du client et son nom sont obligatoires", (long) 4L, (String) "error");
                return;
            }
            debt = Double.parseDouble(this.txt_eval_sum_usd.getText());
            this.vente4save.setDeviseDette(this.maker.getMainCurrency());
            this.vente4save.setMontantDette(Double.valueOf(debt));
            this.vente4save.setPayment("Paiement Credit partiel");
            this.vente4save.setEcheance((LocalDate) this.dpk_echeance_debt.getValue());
        } else if (((String) this.cbx_payment_mode.getValue()).equals("CREDIT")) {
            if (this.dpk_echeance_debt.getValue() == null || this.tf_phone_client.getText().isEmpty() || this.cliname.getText().isEmpty()) {
                MainUI.notify(null, (String) "Erreur", (String) "La date de l'\u00e9ch\u00e9ance, le t\u00e9l\u00e9phone du client et son nom sont obligatoires", (long) 4L, (String) "error");
                return;
            }
            debt = Double.parseDouble(this.txt_eval_sum_usd.getText());
            this.vente4save.setDeviseDette(this.maker.getMainCurrency());
            this.vente4save.setMontantDette(Double.valueOf(debt));
            this.vente4save.setPayment("Paiement a credit");
            this.vente4save.setMontantCdf(0.0);
            this.vente4save.setMontantUsd(0.0);
            this.vente4save.setEcheance((LocalDate) this.dpk_echeance_debt.getValue());
        }
        if (!(this.dpk_echeance_debt.isDisabled() || 
                !this.tf_phone_client.getText().isEmpty() && 
                StringUtils.isNumeric((CharSequence) this.tf_phone_client.getText()))) {
            MainUI.notify(null, (String) "Erreur", (String) "Le num\u00e9ro de t\u00e9l\u00e9phone du client est obligatoire,c'est pour lui rapeller par SMS de vous payer dans le d\u00e9lai", (long) 4L, (String) "error");
            return;
        }
        if (!this.tf_phone_client.getText().isEmpty() || !this.cliname.getText().isEmpty()) {
            String phon = this.tf_phone_client.getText().isEmpty() ? String.valueOf((int) (Math.random() * 10000.0)) : this.tf_phone_client.getText();
            String namecli = this.cliname.getText().isEmpty() ? "Unknown" : this.cliname.getText();
            List clts = ClientDelegate.findClientByPhone((String) (phon.contains("\\+243") ? phon.replaceAll("\\+243", "0") : phon));
            if (clts.isEmpty()) {
                if (StringUtils.isNumeric((CharSequence) phon)) {
                    this.client = new Client(DataId.generate());
                    this.client.setPhone(phon.contains("\\+243") ? phon.replaceAll("\\+243", "0") : phon);
                    this.client.setNomClient(namecli);
                    this.client.setAdresse("Unknown");
                    this.client.setEmail("Unknown");
                    this.client.setTypeClient("Consommateur");
                    this.client.setParentId(ClientDelegate.findAnonymousClient());
                    Client customer = ClientDelegate.saveClient((Client) this.client);
                    Executors.newCachedThreadPool().submit(() -> {
                        try {
                            this.saveClientByHttp(customer);
                        } catch (IOException ex) {
                            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }
            } else {
                this.client = (Client) clts.get(0);
            }
        }
        this.vente4save.setDateVente(dpk_date_vente.getValue().atTime(LocalTime.now()));
        this.vente4save.setRegion(this.region);
        this.vente4save.setLibelle((String) (this.tflibelle.getText().isEmpty() ? "Vente - Ref  " + this.vente4save.getReference() : this.tflibelle.getText()));
        this.vente4save.setLatitude(0.0);
        this.vente4save.setLongitude(0.0);
//        
        if (this.vente4save != null) {
            Vente vtx;
            this.vente4save.setClientId(this.client);
            Map<String, Double> lotAvailablePiecesCache = new HashMap<>();
            if (!this.clients.contains(this.client) && this.save2favorite.isSelected()) {
                this.clients.add(this.client);
            }
            if ((vtx = VenteDelegate.findVente(this.vente4save.getUid())) == null) {
                Vente vent = VenteDelegate.saveVente((Vente) this.vente4save);
                for (LigneVente ligneVente : this.venteItems) {
                    this.saveLigneVenteWithLotSplitting(ligneVente, vent, dev, lotAvailablePiecesCache);
                }

            } else {
                List<LigneVente> lvs = LigneVenteDelegate.findByReference(this.vente4save.getUid());
                System.out.println("Venty " + this.vente4save.getLibelle());
                Vente vent = VenteDelegate.updateVente((Vente) this.vente4save);
                if (!lvs.isEmpty()) {
                    for (LigneVente lv : lvs) {
                        LigneVenteDelegate.deleteLigneVente(lv);
                    }
                    for (LigneVente i : this.venteItems) {
                        this.saveLigneVenteWithLotSplitting(i, vent, dev, lotAvailablePiecesCache);
                    }

                }
            }
            System.out.println("Ventitem count : " + this.venteItems.size());
            this.vente4save.setLigneVenteList(this.venteItems);
            this.pref.putInt("_bill_counter_", this.compteur);
            this.pref.putInt("tranzit_bill", -100);
            if (!((String) this.cbx_payment_mode.getValue()).equals("CREDIT")) {
                LocalDate ldt = this.vente4save.getDateVente().toLocalDate();
                Traisorerie trzr = TraisorerieDelegate.findExistingOf((String) ("BE" + Constants.dateTodayRef((LocalDate) ldt)), (LocalDate) ldt, (String) this.choosenComptTr.getUid(), (String) this.region);
                double d = VenteDelegate.sumUsdSaleOf((LocalDate) ldt, (LocalDate) ldt, (String) this.region);
                double sumCdf = VenteDelegate.sumCdfSaleOf((LocalDate) ldt, (LocalDate) ldt, (String) this.region);
                double balcdf = TraisorerieDelegate.findCurrentBalanceCdf((String) this.choosenComptTr.getUid(), (LocalDate) ldt, (LocalDate) ldt, (String) this.region);
                double balusd = TraisorerieDelegate.findCurrentBalanceUsd((String) this.choosenComptTr.getUid(), (LocalDate) ldt, (LocalDate) ldt, (String) this.region);
                System.err.println("comptresor " + ((CompteTresor) this.cbx_comptes.getValue()).getIntitule() + " " + balcdf + " usdb " + balusd);
                if (trzr == null) {
                    trzr = new Traisorerie(DataId.generate());
                    trzr.setDate(LocalDateTime.now());
                    trzr.setLibelle("Ventes journalier");
                    trzr.setMontantCdf(sumCdf);
                    trzr.setMontantUsd(d);
                    trzr.setMouvement(Mouvment.AUGMENTATION.name());
                    trzr.setReference("BE" + Constants.dateTodayRef((LocalDate) ldt));
                    trzr.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
                    trzr.setRegion(this.region);
                    trzr.setTresorId(this.choosenComptTr);
                    trzr.setSoldeCdf(Double.valueOf(balcdf + this.vente4save.getMontantCdf()));
                    trzr.setSoldeUsd(Double.valueOf(balusd + this.vente4save.getMontantUsd()));
                    this.svincss = TraisorerieDelegate.saveTraisorerie((Traisorerie) trzr);
                } else {
                    trzr.setLibelle("Ventes journalier ");
                    trzr.setMontantCdf(sumCdf);
                    trzr.setMontantUsd(d);
                    trzr.setLibelle("Ventes journalier");
                    trzr.setDate(LocalDateTime.now());
                    trzr.setMouvement(Mouvment.AUGMENTATION.name());
                    trzr.setTresorId(this.choosenComptTr);
                    trzr.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
                    trzr.setSoldeCdf(Double.valueOf(balcdf + this.vente4save.getMontantCdf()));
                    trzr.setSoldeUsd(Double.valueOf(balusd + this.vente4save.getMontantUsd()));
                    this.svincss = TraisorerieDelegate.updateTraisorerie((Traisorerie) trzr);
                }
            }
            System.out.println("SERVICE TX SAVED " + this.svincss.getSoldeUsd());

            if (this.chbx_print_receipt.isSelected()) {
                if (this.chbx_print_thermal.isSelected()) {
                    this.print();
                }
                if (this.defaultPrinter != null) {
                    for (int i = 0; i < this.copies; ++i) {
                        System.out.println("Printing on thermal... " + i);
                        double paid = this.maker.isUsd() ? this.toUsd() : this.toCdf();
                        Client cl = this.vente4save.getClientId();
                        System.out.println("Printing...");
                        this.printReceipt(this.defaultPrinter.getName(), this.entrepName,
                                this.rccm, this.vente4save.getReference(), this.venteItems, paid,
                                cl.getNomClient(), cl.getPhone(), this.maker.getMainCurrency(), this.taux2change);
                    }
                }
            }
            Executors.newCachedThreadPool().submit(() -> {
                if (this.svincss != null) {
                    boolean savecsh = this.saveCashByHttp(this.svincss);
                    System.out.println("Tresorerie http saved is " + savecsh);
                }
            });
            MainUI.notify(null, (String) "Info", (String) "Vente enregistree avec succes", (long) 4L, (String) "info");
            this.cdl.countDown();
            this.tryToSaveSale(this.svincss == null ? null : this.svincss.getUid(), this.choosenComptTr, this.client, this.vente4save, this.vente4save.getLigneVenteList(), et);
        }
//        
    }

    private void tryToSaveSale(String transaction, CompteTresor ct, Client client, Vente vente, List<LigneVente> lignes, Event et) {
        Executors.newCachedThreadPool().submit(() -> {
            if (!Util.isInternetAndBaseApiReachable()) {
                Platform.runLater(() -> {
                    PosController.getInstance().clearCart();
                    try {
                        if (PosController.getInstance().savedCarts != null) {
                            PosController.getInstance().savedCarts.removeIf(v -> v.getUid() == vente.getUid());
                        }
                    } catch (Exception exception) {
                        // empty catch block
                    }
                    PosController.getInstance().refreshPos(et);
                    this.venteItems.clear();
                    this.close(et);
                });
                return;
            }
            try {
                this.cdl.await();
            } catch (InterruptedException interruptedException) {
                // empty catch block
            }

            int retries = 0;
            block12:
            while (retries < MAX_SALE_RETRY) {
                try {
                    vente.setClientId(client);
                    System.out.println("Client phone : " + client.getPhone());
                    Response<Vente> rep = this.saveVenteByHttp(vente, client, ct, transaction, lignes);
                    if (rep == null) {
                        System.out.println("Reponse save vente by http est NULL" + String.valueOf(rep));
                        continue;
                    }
                    int reponse = rep.code();
                    System.out.println("Reponse http code - de vente " + reponse);
                    switch (reponse) {
                        case 417: {
                            System.out.println("T3 Client " + reponse + " " + client.getPhone());
                            List cs = ClientDelegate.findClientByPhone((String) client.getPhone());
                            if (!cs.isEmpty()) {
                                System.err.println("Clients is Empty");
                                Client c = (Client) cs.get(0);
                                boolean client_saved = this.saveClientByHttp(c);
                                System.out.println("Client enregistre : " + (client_saved ? "OK" : "OOps! error"));
                                break;
                            }
                            Client sc = ClientDelegate.saveClient((Client) client);
                            System.out.println("Save clt " + sc.getPhone());
                            break;
                        }
                        case 412: {
                            System.out.println("T3 Compte Tresor " + reponse);
                            List comptes = CompteTresorDelegate.findByNumeroCompte((String) ct.getNumeroCompte());
                            if (comptes.isEmpty()) {
                                break;
                            }
                            System.err.println("After if compte tres");
                            CompteTresor compte = (CompteTresor) comptes.get(0);
                            this.saveCompte(compte);
                            Executors.newCachedThreadPool().submit(() -> Util.sync((BaseModel) compte, (String) "create", (Tables) Tables.COMPTETRESOR));
                            break;
                        }
                        case 400: {
                            for (LigneVente ligne : lignes) {
                                Produit produit = ProduitDelegate.findProduit((String) ligne.getProductId().getUid());
                                List mesures = MesureDelegate.findMesureByProduit((String) produit.getUid());
                                this.sendProduitIfNotExist(produit, mesures);
                            }
                            break;
                        }
                        case 200: {
                            System.out.println("Vente enregistree au serveur avec succes");
                            break block12;
                        }
                        default: {
                            System.out.println("Reponse par defaut " + reponse);
                        }
                    }
                    ++retries;
                    try {
                        TimeUnit.MILLISECONDS.sleep(200L * (long) Math.pow(2.0, retries));
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
                PosController.getInstance().refreshPos(et);
                this.venteItems.clear();
                this.close(et);
            });
        });
    }

    private void createPdfBill(Entreprise entrep, Vente vt, Client ff) {
        if (ff == null) {
            MainUI.notify(null, (String) "Erreur", (String) "Tu peux aussi preciser un client si besoin", (long) 3L, (String) "error");
        }
        try {
            PDDocument document = new PDDocument();
            PDPage fPage = new PDPage(PDRectangle.A4);
            document.addPage(fPage);
            int pageW = (int) PDRectangle.A4.getWidth();
            int pageH = (int) PDRectangle.A4.getHeight();
            PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
            PDFUtils pdf = new PDFUtils(document, contentStream);
            PDType1Font hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            File f = FileUtils.pointFile((String) (entrep.getUid() + ".png"));
            if (!f.exists()) {
                InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                f = FileUtils.streamTofile((InputStream) is);
            }
            Color endeleya = new Color(68, 206, 245);
            Color egray = new Color(218, 218, 219);
            PDImageXObject logo = PDImageXObject.createFromFile((String) f.getPath(), (PDDocument) document);
            contentStream.drawImage(logo, (float) (pageW - 114), (float) (pageH - 114), 84.0f, 84.0f);
            pdf.addTextLine("Facture", 25, pageH - 98, (PDFont) hbold, 40.0f, Color.DARK_GRAY);
            contentStream.setStrokingColor(endeleya);
            contentStream.setLineWidth(2.0f);
            contentStream.moveTo(25.0f, 700.0f);
            contentStream.lineTo((float) (pageW - 25), 700.0f);
            contentStream.stroke();
            pdf.addTextLine(entrep.getNomEntreprise(), 25, pageH - 180, (PDFont) hnormal, 18.0f, Color.BLACK);
            pdf.addTextLine(new String[]{"Adresse : " + entrep.getAdresse(), "RCCM : " + entrep.getIdentification(), entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(), entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot()}, 15.0f, 25, pageH - 192, (PDFont) hnormal, 14.0f, Color.BLACK);
            String idf = ff.getPhone();
            pdf.addTextLine(ff.getNomClient(), (int) ((float) pageW - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "Tel : " + idf) / 1000.0f * 15.0f - 92.0f), pageH - 180, (PDFont) hnormal, 18.0f, Color.BLACK);
            pdf.addTextLine(new String[]{"Adresse : " + ff.getAdresse(), idf == null ? "" : "RCCM : " + idf, "Tel : " + ff.getPhone()}, 15.0f, (int) ((float) pageW - hnormal.getStringWidth(idf == null ? "Adresse : " + ff.getAdresse() : "RCCM : " + idf) / 1000.0f * 15.0f - 92.0f), pageH - 192, (PDFont) hnormal, 14.0f, Color.BLACK);
            String date = "Date : " + String.valueOf(LocalDateTime.now());
            pdf.addTextLine(new String[]{date, "Facture N# : " + vt.getReference()}, 15.0f, (int) ((float) pageW - hnormal.getStringWidth(date) / 1000.0f * 15.0f - 32.0f), pageH - 260, (PDFont) hnormal, 14.0f, Color.BLACK);
            int[] table = new int[]{55, 100, 230, 65, 90};
            pdf.addTable(table, 30, 25, pageH - 400);
            pdf.setFont((PDFont) hnormal, 11.0f, Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{0, 2});
            pdf.addCell("N#", endeleya);
            pdf.addCell("Quantit\u00e9", endeleya);
            pdf.addCell("D\u00e9signation", endeleya);
            pdf.addCell("P.U.", endeleya);
            pdf.addCell("P. total", endeleya);
            pdf.setFont((PDFont) hnormal, 10.0f, Color.BLACK);
            contentStream.setFont((PDFont) hnormal, 10.0f);
            int i = 0;
            double somme = 0.0;
            int ln = 0;
            int lpp = 26;
            String dev = this.pref.get("mainCur", "USD");
            for (LigneVente rupture : this.venteItems) {
                if (++i > 13 && i == 14 | ++ln == lpp) {
                    contentStream.close();
                    PDPage fPage2 = new PDPage(PDRectangle.A4);
                    document.addPage(fPage2);
                    contentStream = new PDPageContentStream(document, fPage2);
                    pdf = new PDFUtils(document, contentStream);
                    int[] tablex = new int[]{55, 230, 100, 65, 90};
                    pdf.addTable(tablex, 30, 25, pageH - 68);
                    pdf.setFont((PDFont) hnormal, 10.0f, Color.BLACK);
                    pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                    contentStream.setFont((PDFont) hnormal, 10.0f);
                    if (ln == lpp || i == 14) {
                        ln = 0;
                    }
                }
                Produit x = rupture.getProductId();
                pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                pdf.addCell(i + ".", egray);
                pdf.addCell(rupture.getQuantite() + " " + rupture.getMesureId().getDescription(), egray);
                pdf.addCell(x.getNomProduit() + " " + x.getMarque() + " " + x.getModele() + " " + (x.getTaille() == null ? "" : x.getTaille()) + " " + (x.getCouleur() == null ? "" : x.getCouleur()), egray);
                pdf.addCell(rupture.getPrixUnit() * this.taux2change + " ", egray);
                double stot = rupture.getQuantite() * rupture.getPrixUnit();
                somme += stot;
                pdf.addCell(Math.round(BigDecimal.valueOf(stot).setScale(2, RoundingMode.HALF_EVEN).doubleValue() * this.taux2change) + " FC", egray);
            }
            if (ln == lpp - 1 || ln == 0) {
                contentStream.close();
                PDPage fPage2 = new PDPage(PDRectangle.A4);
                document.addPage(fPage2);
                contentStream = new PDPageContentStream(document, fPage2);
                pdf = new PDFUtils(document, contentStream);
                int[] tablex = new int[]{55, 230, 100, 65, 90};
                pdf.addTable(tablex, 30, 25, pageH - 68);
                pdf.setFont((PDFont) hnormal, 10.0f, Color.BLACK);
                pdf.setRightAlignedColumns(new int[]{2, 3, 4});
                contentStream.setFont((PDFont) hnormal, 10.0f);
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
            File bcmd = FileUtils.pointFile((String) (System.currentTimeMillis() + ".pdf"));
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
        MainUI.setShadowEffect((Node) img);
    }

    @FXML
    private void onOutHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.removeShaddowEffect((Node) img);
    }

    private LigneVente[] ligneVenteToArray(List<LigneVente> lvx) {
        LigneVente[] lx = new LigneVente[lvx.size()];
        for (int y = 0; y < lvx.size(); ++y) {
            LigneVente lv;
            lx[y] = lv = lvx.get(y);
        }
        return lx;
    }

    @FXML
    private void onRefreshAcounts(MouseEvent evt) {
        this.comptes.addAll((Collection) CompteTresorDelegate.findCompteTresors());
    }

    private boolean saveClientByHttp(Client clt) throws IOException {
        if (!Util.isInternetAndBaseApiReachable()) {
            return false;
        }
        Response exec = this.kazisafe.saveByForm(clt.getUid(), clt.getNomClient(), clt.getPhone(), clt.getTypeClient(), clt.getEmail(), clt.getAdresse(), clt.getParentId().getUid()).execute();
        return exec.code() == 200;
    }

    private boolean saveCashByHttp(Traisorerie tr) {
        try {
            if (!Util.isInternetAndBaseApiReachable()) {
                return false;
            }
            Response excuted = this.kazisafe.saveCash(tr).execute();
            return excuted.isSuccessful();
        } catch (IOException ex) {
            return false;
        }
    }

    private Response<Vente> saveVenteByHttp(Vente vente, Client client, CompteTresor tresor, String transaction, List<LigneVente> venteItems) throws IOException {
        try {
            if (!Util.isInternetAndBaseApiReachable()) {
                return null;
            }
            VenteHelper hlp = new VenteHelper();
            hlp.setTransactionId(transaction);
            hlp.setTresor(tresor);
            hlp.setClient(client);
            hlp.setLigneVentes(venteItems);
            hlp.setVente(vente);
            Response exe = this.kazisafe.syncSale(hlp).execute();
            System.out.println("Vente response Http : " + String.valueOf(exe));
            return exe;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<SaleItemHelper> toSaleItemHelper(List<LigneVente> lvs) {
        ArrayList<SaleItemHelper> result = new ArrayList<>();
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
        Response exec = this.kazisafe.saveCompteTresorByForm(tr.getUid(), tr.getBankName(), tr.getIntitule(), tr.getSoldeMinimum().doubleValue(), tr.getNumeroCompte(), tr.getRegion(), tr.getTypeCompte()).execute();
        System.err.println("Reponse exec " + exec.code());
        return exec.code() == 200;
    }

    private void sendProduitIfNotExist(Produit produit, List<Mesure> mesures) {
        byte[] imageBytes = produit.getImage();
        if (imageBytes == null) {
            imageBytes = this.loadDefaultImage();
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        this.saveProduitByHttp(produit, base64Image, mesures);
    }

    private byte[] loadDefaultImage() {
        byte[] byArray;
        block8:
        {
            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            try {
                byArray = FileUtils.readAllBytes((InputStream) is);
                if (is == null) {
                    break block8;
                }
            } catch (Throwable throwable) {
                try {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException e) {
                    System.err.println("Erreur lors du chargement de l'image par d\u00e9faut" + e.getMessage());
                    return new byte[0];
                }
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return byArray;
    }

    private void saveProduitByHttp(Produit produit, String base64Image, List<Mesure> mesures) {
        if (!Util.isInternetAndBaseApiReachable()) {
            return;
        }
        ProduitHelper produitHelper = this.createProduitHelper(produit, base64Image, mesures);
        try {
            Response response = this.kazisafe.saveLite(produitHelper).execute();
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

    @FXML
    public void onBluetoothSearch() {
        if (!this.chbx_bt_search.isSelected()) {
            this.pgi_bt_search.setVisible(false);
            this.lbl_bt_count.setVisible(false);
            return;
        }
        this.pgi_bt_search.setVisible(true);
        this.lbl_bt_count.setVisible(true);
        this.lbl_bt_count.setText("Recherche...");
        CompletableFuture.runAsync(() -> {
            try {
                List<RemoteDevice> devices = BluetoothPrinterManager.findPrinters();
                int count = 0;
                for (RemoteDevice device : devices) {
                    try {
                        String name = device.getFriendlyName(false);
                        String url = BluetoothPrinterManager.getServiceUrl((RemoteDevice) device);
                        if (url == null) {
                            continue;
                        }
                        BluetoothPrintService bts = new BluetoothPrintService(name, url);
                        PrintServiceLookup.registerService((PrintService) bts);
                        int currentCount = ++count;
                        Platform.runLater(() -> {
                            ObservableSet osp = Printer.getAllPrinters();
                            this.cbx_printers.setItems(this.setToList((ObservableSet<Printer>) osp));
                            this.lbl_bt_count.setText(currentCount + " trouv\u00e9(s)");
                        });
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Error discovering device: " + e.getMessage());
                    }
                }
                int finalCount = count;
                Platform.runLater(() -> {
                    this.pgi_bt_search.setVisible(false);
                    this.lbl_bt_count.setText(finalCount + " trouv\u00e9(s)");
                    this.chbx_bt_search.setSelected(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    this.pgi_bt_search.setVisible(false);
                    this.lbl_bt_count.setText("Erreur: " + e.getMessage());
                    this.chbx_bt_search.setSelected(false);
                });
            }
        });
    }

    public void refreshSerialPrinters() {
        try {
            List<SerialPort> ports = SerialPrinterManager.findSerialPrinters();
            for (SerialPort port : ports) {
                String name = port.getDescriptivePortName();
                String systemName = port.getSystemPortName();
                SerialPrintService sps = new SerialPrintService(name, systemName);
                PrintServiceLookup.registerService((PrintService) sps);
            }
            ObservableSet<Printer> osp = Printer.getAllPrinters();
            this.cbx_printers.setItems(this.setToList(osp));
        } catch (Exception e) {
            System.err.println("Error discovering serial ports: " + e.getMessage());
        }
    }

    public void printBillViaBluetooth() {
        if (this.defaultPrinter != null && this.defaultPrinter.getName().startsWith("BT:")) {
            if (this.txt_print_status != null) {
                this.txt_print_status.setText("Printing via BT...");
            }
            this.printReceipt(this.defaultPrinter.getName(), this.entrepName, this.rccm, this.vente4save.getReference(), this.venteItems, this.ff + this.fd, this.cliname.getText(), this.tf_phone_client.getText(), this.maker.getMainCurrency(), this.taux2change);
        }
    }

    public void printBillViaSerial() {
        if (this.defaultPrinter != null && this.defaultPrinter.getName().startsWith("COM:")) {
            if (this.txt_print_status != null) {
                this.txt_print_status.setText("Printing via Serial...");
            }
            this.printReceipt(this.defaultPrinter.getName(), this.entrepName, this.rccm, this.vente4save.getReference(), this.venteItems, this.ff + this.fd, this.cliname.getText(), this.tf_phone_client.getText(), this.maker.getMainCurrency(), this.taux2change);
        }
    }

    private void saveLigneVenteWithLotSplitting(LigneVente ligneVente, Vente vente, String dev, Map<String, Double> lotAvailablePiecesCache) {
        String meth = this.pref.get("meth", "fifo");
        Produit prod = ligneVente.getProductId();
        Mesure mes = ligneVente.getMesureId();
        double factor = (mes != null && mes.getQuantContenu() != null && mes.getQuantContenu() > 0) ? mes.getQuantContenu() : 1.0;

        // On travaille en pièces (unité de base) pour les calculs de stock
        double requestedPieces = ligneVente.getQuantite() * factor;
        double remainingPieces = requestedPieces;
        String priorityLot = ligneVente.getNumlot();

        // 1. Priorité au lot venant avec la ligne (si reSplittnseigné et a du stock)
        if (priorityLot != null && !priorityLot.isBlank()) {
            Available availablePieces = getAvailablePiecesForLot(prod, priorityLot, lotAvailablePiecesCache);
            if (availablePieces.availablePiece() > 0) {
                double takePieces = Math.min(remainingPieces, availablePieces.availablePiece());
                recordSplittedPart(ligneVente, availablePieces.coutAchat(), vente, priorityLot, takePieces / factor, dev);
                consumeFromLotCache(prod, priorityLot, takePieces, lotAvailablePiecesCache);
                remainingPieces -= takePieces;
            }
        }

        // 2. Parcourir les autres lots si nécessaire
        if (remainingPieces > 0) {
            List<Recquisition> orderedLotsEntries = switch (meth) {
                case "ppps" ->
                    RecquisitionDelegate.toFefoOrdering(prod.getUid(), region);
                case "fifo" ->
                    RecquisitionDelegate.toFifoOrdering(prod.getUid(), region);
                case "lifo" ->
                    RecquisitionDelegate.toLifoOrdering(prod.getUid(), region);
                default ->
                    RecquisitionDelegate.toFifoOrdering(prod.getUid(), region);
            };

            List<String> distinctOtherLots = orderedLotsEntries.stream()
                    .map(Recquisition::getNumlot)
                    .filter(l -> l != null && !l.equals(priorityLot))
                    .distinct()
                    .collect(Collectors.toList());

            for (String lot : distinctOtherLots) {
                if (remainingPieces <= 0) {
                    break;
                }
                Available availablePieces = getAvailablePiecesForLot(prod, lot, lotAvailablePiecesCache);
                if (availablePieces.availablePiece() > 0) {
                    double takePieces = Math.min(remainingPieces, availablePieces.availablePiece());
                    recordSplittedPart(ligneVente, availablePieces.coutAchat(), vente, lot, takePieces / factor, dev);
                    consumeFromLotCache(prod, lot, takePieces, lotAvailablePiecesCache);
                    remainingPieces -= takePieces;
                }
            }

            // Fallback pour la quantité restante
            if (remainingPieces > 0) {
                String fallbackLot = (priorityLot != null && !priorityLot.isBlank())
                        ? priorityLot : "INCONNU";
                double caByPiece = ligneVente.getCoutAchat() / factor;
                recordSplittedPart(ligneVente, caByPiece, vente, fallbackLot, remainingPieces / factor, dev);
            }
        }
//    
    }

    private String lotCacheKey(Produit produit, String lot) {
        String p = (produit == null || produit.getUid() == null) ? "UNKNOWN_PROD" : produit.getUid();
        String l = (lot == null || lot.isBlank()) ? "INCONNU" : lot;
        return p + "::" + l;
    }

    private Available getAvailablePiecesForLot(Produit produit, String lot, Map<String, Double> lotAvailablePiecesCache) {
        data.StockAgregate sa = delegates.RepportDelegate.findCurrentStock(produit,
                lot, LocalDate.now(),
                LocalDate.now(), region);
        String key = lotCacheKey(produit, lot);
        Double cached = lotAvailablePiecesCache.get(key);
        if (cached != null) {
            return new Available(Math.max(0, cached), sa.getCoutAchat());
        }
        double availablePieces = (sa == null || sa.getFinalQuantity() < 0) ? 0 : sa.getFinalQuantity();
        lotAvailablePiecesCache.put(key, availablePieces);
        return new Available(availablePieces, sa==null?0:sa.getCoutAchat());
    }

    private record Available(double availablePiece, double coutAchat) {

    }

    private void consumeFromLotCache(Produit produit, String lot, double consumedPieces, Map<String, Double> lotAvailablePiecesCache) {
        String key = lotCacheKey(produit, lot);
        double current = lotAvailablePiecesCache.getOrDefault(key, 0.0);
        double remaining = Math.max(0, current - consumedPieces);
        lotAvailablePiecesCache.put(key, remaining);
        String productName = (produit != null) ? produit.getNomProduit() : "PRODUIT";
        String lotLabel = (lot == null || lot.isBlank()) ? "INCONNU" : lot;
        System.out.println("Stock restant lot " + lotLabel + " (" + productName + ") = " + remaining + " pieces");
    }

    private void recordSplittedPart(LigneVente original, double coutAchat, Vente vente, String lot, double qty, String dev) {
        LigneVente part = new LigneVente();
        part.setUid(DataId.generateLong());
        // Utiliser des références minimales (id uniquement) évite de propager
        // un graphe d'objets complet lors du persist/merge de la ligne de vente.
        Produit productRef = new Produit(original.getProductId().getUid());
        Mesure mesureRef = new Mesure(original.getMesureId().getUid());
        Vente venteRef = new Vente(vente.getUid());
        part.setProductId(productRef);
        part.setMesureId(mesureRef);
        part.setPrixUnit(original.getPrixUnit());
        part.setQuantite(qty);
        part.setNumlot(lot);
        part.setClientId(this.client != null ? this.client.getPhone() : original.getClientId());
        part.setReference(venteRef);

        double ratio = original.getQuantite() > 0 ? (qty / original.getQuantite()) : 1.0;
        part.setMontantUsd(original.getMontantUsd() * ratio);
        part.setMontantCdf(original.getMontantCdf() * ratio);

        // Récupérer le coût d'achat pour ce lot
//        List<Recquisition> recs = RecquisitionDelegate.findRecquisitionByProduit(original.getProductId().getUid(), lot, region);
//        if (recs != null && !recs.isEmpty()) {
//            part.setCoutAchat(recs.get(0).getCoutAchat());
//        } else {
        part.setCoutAchat(coutAchat);
//        }
        LigneVenteDelegate.saveLigneVente(part);

        // Ne pas déclencher de rectification stock ici pour éviter tout effet de bord
        // sur les entrées (recquisition) pendant l'enregistrement d'une vente.
        LocalDate leo = LocalDate.now();
        RecquisitionDelegate.fixUndesiredRecqusitionOf(leo, leo, region);
        RecquisitionDelegate.rectifyStock(productRef, leo, leo, region, part.getNumlot());
        // Métrique de vente
        SaleAgregate sa = new SaleAgregate();
        sa.setUid(DataId.generate());
        if (original.getProductId().getCategoryId() != null) {
            sa.setCategoryId(new Category(original.getProductId().getCategoryId().getUid()));
        }
        sa.setProductId(productRef);
        sa.setCoutAchatTotal((part.getCoutAchat() == null ? 0.0 : part.getCoutAchat()) * part.getQuantite());
        sa.setDate(vente.getDateVente());
        sa.setMesureId(mesureRef);
        sa.setQuantite(part.getQuantite());
        sa.setRegion(this.region);
        sa.setTotalSaleUsd(dev.equalsIgnoreCase("USD") ? part.getMontantUsd() : part.getMontantCdf());
        RepportDelegate.createMetric(sa);
    }

}
