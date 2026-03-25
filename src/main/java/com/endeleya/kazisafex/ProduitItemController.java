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
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;

import delegates.CategoryDelegate;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import data.core.KazisafeServiceFactory;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import jakarta.xml.bind.DatatypeConverter;
import data.network.Kazisafe;
import data.Category;
import data.Entreprise;
import data.Mesure;
import data.Produit;
import data.ProduitHelper;
import java.time.LocalDateTime;
import java.util.Base64;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.math.NumberUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.PlatformUtil;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;
import tools.FileUtils;
import utilities.ImageProduit;
import tools.Tables;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class ProduitItemController implements Initializable {

    public static ProduitItemController getInstance() {
        return instance;
    }

    static boolean OK = true;

    @FXML
    private ComboBox<Category> cbx_choose_category;
    @FXML
    private Pane categorie_pane;
    @FXML
    private Pane pane_print;
    @FXML
    private TextField tf_input_category;
    @FXML
    private TextField tf_input_nomproduit;
    @FXML
    private ImageView imgvu_product, img_codebar;
    @FXML
    private ImageView imgvu_empty_avatar;
    @FXML
    private TextField tf_input_markproduit;
    @FXML
    private TextField tf_input_modelproduit;
    @FXML
    private TextField tf_input_codebarproduit;
    @FXML
    private TextField tf_input_tailleproduit;
    @FXML
    private RadioButton thermal, jet;
    @FXML
    private TextField tf_input_couleurproduit;
    @FXML
    private ComboBox<String> cbx_descr_mzr;
    @FXML
    private ComboBox<Printer> cbx_printers;
    @FXML
    private TextField tf_quant_mzr;
    @FXML
    private TilePane tile_pn_mesures;
    @FXML
    private ImageView img_vu_edit, img_btn_search_img_google, img_btn_ordimage, img_btn_search_img_google1;

    private Entreprise entreprise;
    Category choosenCat;
    Produit produit;
    Node choosenMez;
    Printer defaultPrinter;
    ToggleGroup printerGroup;

    //JpaStorage jpas;
    private static ProduitItemController instance;

    public ProduitItemController() {
        //jpas=JpaStorage.getInstance();
        instance = this;
    }

    Kazisafe kazisafe;
    ObservableList<Category> categoriesx;
    List<String> lss = new ArrayList<>();

    File choosenFile;
    String meth;
    private String token;

    Preferences pref;

    @FXML
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        n.getParent().setVisible(false);
    }

    @FXML
    private void showFloatingPane(Event evt) {
        pane_print.setVisible(true);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        Tooltip editbtn = new Tooltip("Modifier le produit selectionne");
        Tooltip.install(img_vu_edit, editbtn);
        tile_pn_mesures.setHgap(10);
        tile_pn_mesures.setOrientation(Orientation.VERTICAL);
        tile_pn_mesures.setAlignment(Pos.CENTER);
        kazisafe = KazisafeServiceFactory.createService(pref.get("token", null));
        Tooltip.install(img_btn_search_img_google, new Tooltip("Rechercher l'image sur Google"));
        Tooltip.install(img_btn_ordimage, new Tooltip("Rechercher l'image dans mon ordinateur"));
        Tooltip.install(tf_quant_mzr, new Tooltip("Entrer le nombre de pièce contenant dans la mesure sélectionée"));
        Tooltip.install(cbx_descr_mzr, new Tooltip("Entrer la description d'un tas de pieces vendues et ou achetées ensemble"));
        Tooltip.install(img_btn_search_img_google1, new Tooltip("Imprimer le codebar"));
        printerGroup = new ToggleGroup();
        thermal.setToggleGroup(printerGroup);
        jet.setToggleGroup(printerGroup);

        // TODO 
        meth = pref.get("meth", "fifo");
    }

    @FXML
    public void serchImgOnGoogle(Event e) {
        if (tf_input_markproduit.getText().isEmpty() && tf_input_nomproduit.getText().isEmpty() && tf_input_markproduit.getText().isEmpty() && tf_input_modelproduit.getText().isEmpty()) {
            MainUI.notify(null, "Erreur", "Completer au moins un champs de description du produit, puis continuer", 3, "error");
            return;
        }
        selectAuto();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String q = tf_input_nomproduit.getText() + "+" + tf_input_markproduit.getText()
                            + "+" + tf_input_modelproduit.getText();
                    if (q.startsWith("+")) {
                        q = q.replaceFirst("\\+", "");
                    }
                    if (q.endsWith("+")) {
                        q = replaceLast(q, "\\+", "");
                    }
                    q = q.replace(" ", "+");
                    Desktop.getDesktop().browse(URI.create("https://www.google.com/search?q=" + q + "&source=lnms&tbm=isch"));
                } catch (Exception e) {

                }
            }
        }).start();
    }

    private String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length());
        } else {
            return string;
        }
    }

    private void perfom(Event e) {
        try {
            if (cbx_descr_mzr.getValue().isEmpty() || tf_quant_mzr.getText().isEmpty()) {
                MainUI.notify(null, "Erreur", "Veuillez remplir les deux champs de mesure, puis continuer", 4, "error");
                return;
            }
            Double.valueOf(tf_quant_mzr.getText());
            String mes = cbx_descr_mzr.getValue() + ":" + tf_quant_mzr.getText();
            addMesure(mes);
        } catch (NumberFormatException ex) {
            MainUI.notify(null, "Erreur", "Entrer la quantite valide (des chiffres)", 4, "error");
        }
    }

    @FXML
    public void printbarcode(Event e) {
        if (thermal.isSelected()) {
            printBCWithThermal(defaultPrinter.getName(), tf_input_codebarproduit.getText());
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

    private void showcodebar(String text) {
        InputStream is;
        try {
            if (NumberUtils.isDigits(text) && (text.length() == 12 | text.length() == 13)) {
                EAN13Writer writer = new EAN13Writer();
//                Code128Writer w128=new Code128Writer();
                BitMatrix matrix = writer.encode(text.trim(), BarcodeFormat.EAN_13, 250, 150);
                BufferedImage bimage = MatrixToImageWriter.toBufferedImage(matrix);
                try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                    ImageIO.write(bimage, "jpg", stream);
                    is = new ByteArrayInputStream(stream.toByteArray());
                }
                is.close();
            } else {
                ByteArrayOutputStream stream = createQR(text, "UTF-8", 250, 250);
                is = new ByteArrayInputStream(stream.toByteArray());
            }
            img_codebar.setImage(new Image(is));
        } catch (IOException | java.lang.IllegalArgumentException ex) {
            if (ex instanceof java.lang.IllegalArgumentException) {
                ByteArrayOutputStream stream = createQR(text, "UTF-8", 250, 250);
                is = new ByteArrayInputStream(stream.toByteArray());
                img_codebar.setImage(new Image(is));
            }

        }
    }

    public ByteArrayOutputStream createQR(String data, String charset, int height, int width) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(data.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, width, height);
            BufferedImage bim = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "jpg", baos);
            baos.flush();
            return baos;
        } catch (WriterException ex) {
            Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
    private void addMesures(Event evt) {
        perfom(evt);
    }

    private void addMesure(String mesure) {
        ContextMenu contM = new ContextMenu();
        MenuItem mi = new MenuItem("Supprimer");
        contM.getItems().add(mi);
        Label l = new Label();
        l.setPrefWidth(64);
        l.setPrefHeight(16);
        l.setTextAlignment(TextAlignment.CENTER);
        l.setTextFill(Color.rgb(255, 255, 255));
        l.setBackground(new Background(new BackgroundFill(Color.rgb(0x7, 0x7, 0xf, 0.3), new CornerRadii(5.0), new Insets(-5.0))));
        l.setPadding(new Insets(4, 4, 4, 4));
        l.setText(mesure);
        l.setTooltip(new Tooltip(mesure));
        l.setContextMenu(contM);

        if (isMesureExist(tile_pn_mesures.getChildren(), mesure)) {
            MainUI.notify(null, "Erreur", "La mesure ayant les infos fournies existe deja", 3, "error");
            return;
        }
        double qq = Double.parseDouble(mesure.split(":")[1]);
        if (isMesureExist(tile_pn_mesures.getChildren(), qq)) {
            MainUI.notify(null, "Erreur", "La mesure ayant les infos fournies existe deja", 3, "error");
            return;
        }

        tile_pn_mesures.getChildren().add(l);
        cbx_descr_mzr.getSelectionModel().select("");
        tf_quant_mzr.clear();
        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tile_pn_mesures.getChildren().remove(l);
            }
        });
    }

    private boolean isMesureExist(List<Node> lmz, String decPlusCount) {
        for (Node n : lmz) {
            Label mz = (Label) n;
            String pc = mz.getText().split(":")[0];
            if (pc.equalsIgnoreCase(decPlusCount.split(":")[0])) {
                return true;
            }
        }
        return false;
    }

    private boolean isMesureExist(List<Node> lmz, double q) {
        for (Node n : lmz) {
            Label mz = (Label) n;
            String qt = mz.getText().split(":")[1];
            double quant = Double.parseDouble(qt);
            if (quant == q) {
                return true;
            }
        }
        return false;
    }

    @FXML
    public void addMZ(KeyEvent kevt) {
        if (kevt.getCode() == KeyCode.ENTER) {
            perfom(kevt);
        }
    }

    public List<String> toStringList(List<Mesure> lm) {
        List<String> result = new ArrayList<>();
        for (Mesure mesure : lm) {
            result.add(mesure.getDescription() + ":" + mesure.getQuantContenu());
        }
        return result;
    }

    public void setProduct(Produit prod) {
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();
        cbx_printers.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) -> {
            defaultPrinter = newValue;
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        tf_input_codebarproduit.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }
            showcodebar(newValue);
        });
        if (prod == null) {
            imgvu_empty_avatar.setVisible(true);
            return;
        }
        categoriesx = FXCollections.observableArrayList();
        List<Category> cats = CategoryDelegate.findCategories();//jpas.findAll(Category.class);
        System.out.println("Categories ----- XXXXXX SIZE " + cats.size());
        cbx_choose_category.setItems(categoriesx);
        cats.forEach(k -> {
            categoriesx.add(k);
        });
        this.produit = prod;
        imgvu_empty_avatar.setVisible(false);
        tf_input_nomproduit.setText(this.produit.getNomProduit());
        tf_input_markproduit.setText(this.produit.getMarque());
        tf_input_tailleproduit.setText(this.produit.getTaille());
        tf_input_codebarproduit.setText(this.produit.getCodebar());
        tf_input_modelproduit.setText(this.produit.getModele());
        tf_input_couleurproduit.setText(this.produit.getCouleur());
        if (!tf_input_codebarproduit.getText().isEmpty()) {
            showcodebar(tf_input_codebarproduit.getText());
        }

        Category cx = this.produit.getCategoryId();
        Category c;
        if (cx != null) {
            c = CategoryDelegate.findCategory(cx.getUid());//jpas.findByUid(Category.class,cx.getUid());
            cbx_choose_category.setValue(c);
            tf_input_category.setText(c == null ? "" : c.getDescritption());
        }
        List<Mesure> mesr = MesureDelegate.findMesureByProduit(produit.getUid());//jpas.findByProduit(Mesure.class, this.produit.getUid());
        //Util.findMesureForProduitWithId(nsmesure.findAll(), );
        List<String> mznoms = toStringList(mesr);
        for (String mznom : mznoms) {
            addMesure(mznom);
        }
        File f = FileUtils.pointFile(produit.getUid() + ".jpeg");
        InputStream is;
        if (!f.exists()) {
            is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            f = FileUtils.streamTofile(is);
        }
        Image image = null;
        try {
            image = new Image(new FileInputStream(f));
            imgvu_product.setImage(image);
            Util.centerImage(imgvu_product);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        setInitData(null);

    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        for (Printer p : osp) {
            rst.add(p);
        }
        return rst;
    }

    @FXML
    private void modifierSelectedProduit(Event e) {
        if (produit == null || cbx_choose_category.getValue() == null) {
            MainUI.notify(null, "Erreur", "Selectionner un produit et une categorie puis continuer", 4, "error");
            return;
        }

        new Thread(() -> {
            try {
                byte[] pixa = null;
                if (choosenFile != null) {
                    pixa = FileUtils.readFromFile(choosenFile);
                    ImageProduit imgp = new ImageProduit();
                    imgp.setIdProduit(produit.getUid());
                    String base64 = DatatypeConverter.printBase64Binary(pixa);
                    imgp.setImageBase64(base64);
//                    Util.syncImage(imgp);
                }
            } catch (IOException ex) {
                Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

        Produit p = new Produit(produit.getUid());

        try {
            byte[] pixa = null;
            if (choosenFile != null) {
                pixa = FileUtils.readFromFile(choosenFile);
                FileUtils.byteToFile(p.getUid(), pixa);
                p.setImage(pixa);
            }
        } catch (IOException ex) {
            Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.setCategoryId(cbx_choose_category.getValue());
        p.setDateCreation(LocalDateTime.now());
        String value = tf_input_codebarproduit.getText();
        p.setCodebar(value == null ? produit.getCodebar() : value);
        value = tf_input_nomproduit.getText();
        p.setNomProduit(value == null ? produit.getNomProduit() : value);
        value = tf_input_markproduit.getText();
        p.setMarque(value == null ? produit.getMarque() : value);
        p.setMethodeInventaire(meth);
        value = tf_input_modelproduit.getText();
        p.setModele(value == null ? produit.getModele() : value);
        value = tf_input_couleurproduit.getText();
        p.setCouleur(value == null ? produit.getCouleur() : value);
        value = tf_input_tailleproduit.getText();
        p.setTaille(value == null ? produit.getTaille() : value);
        Produit prodi = ProduitDelegate.updateProduit(p);//jpas.update(p);

        if (prodi != null) {
            ProduitsController.getInstance().replaceElemnt(produit, prodi);
            List<Mesure> mzs = MesureDelegate.findMesureByProduit(prodi.getUid());
            sendProduitIfNotExist(p, mzs);
            mzs.forEach(m->saveMesureByHttp(m));
            MainUI.notify(null, "INFO", "La modification du produit faite avec succes", 3, "info");
        }

    }

    public void setInitData(String token) {
        this.token = token;

        kazisafe = KazisafeServiceFactory.createService(token);
        //jpas=JpaStorage.getInstance();

        cbx_descr_mzr.setItems(FXCollections.observableArrayList("CTN", "DZN", "DiZN", "SZN", "Pqt", "PCs"));
        conf();
        List<Category> cats = CategoryDelegate.findCategories();//jpas.findAll(Category.class);
        categoriesx = FXCollections.observableArrayList(cats);
        cbx_choose_category.setItems(categoriesx);
        cats.forEach(k -> {
            categoriesx.add(k);
        });
//         categoriesx.addAll(cats);
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        System.out.println("Printewrs count XX " + osp.size());
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();
        cbx_printers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Printer>() {
            @Override
            public void changed(ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) {
                defaultPrinter = newValue;
            }
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        tf_input_codebarproduit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.isEmpty()) {
                    return;
                }
                showcodebar(newValue);
            }
        });
        tile_pn_mesures.clipProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
                choosenMez = newValue;
            }
        });
        cbx_descr_mzr.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) {
                    return;
                }
                switch (newValue) {
                    case "DZN":
                        tf_quant_mzr.setText("12");
                        break;
                    case "DiZN":
                        tf_quant_mzr.setText("10");
                        break;
                    case "SZN":
                        tf_quant_mzr.setText("6");
                        break;
                    case "PCs":
                        tf_quant_mzr.setText("1");
                        break;
                    default:
                        tf_quant_mzr.setText("");
                }
            }
        });
    }

    private void conf() {
        cbx_choose_category.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category object) {
                return object == null ? null : object.getDescritption();
            }

            @Override
            public Category fromString(String string) {
                return cbx_choose_category.getItems()
                        .stream()
                        .filter(v -> (v.getDescritption())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_choose_category.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Category> observable, Category oldValue, Category newValue) -> {
            choosenCat = newValue;

        });
    }

    @FXML
    private void showCategoryDlg(ActionEvent evt) {
        categorie_pane.setVisible(true);
    }

    @FXML
    private void browseFiles(Event evt) {
        Node node = (Node) evt.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selectionner une photo du produit");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Photos seulements", "*.jpg", "*.png", "*.jpeg", "*.jfif"));
        choosenFile = fileChooser.showOpenDialog(thisStage);
        if (choosenFile != null && choosenFile.length() < 5000000) {
            InputStream fis = null;
            try {
                OK = false;
                imgvu_empty_avatar.setVisible(false);
                fis = new FileInputStream(choosenFile);
                Image image = new Image(fis);
                imgvu_product.setImage(image);
                centerImage(imgvu_product);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void selectAuto() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OK = true;
                choosenFile = platformize("kazisafe-produit.jpeg");
                if (choosenFile.exists()) {
                    choosenFile.delete();
                }
                while (OK) {
                    FileInputStream fis = null;
                    try {
                        choosenFile = platformize("kazisafe-produit.jpeg");
                        System.err.println("tik task");
                        if (choosenFile.exists()) {
                            OK = false;
                            System.out.println("Fichier trouveeee");
                            imgvu_empty_avatar.setVisible(false);
                            fis = new FileInputStream(choosenFile);
                            Image image = new Image(fis);
                            imgvu_product.setImage(image);
                            centerImage(imgvu_product);
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }).start();
    }

    private File platformize(String filename) {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path = "C:" + File.separator + "Users" + File.separator + System.getProperty("user.name") + File.separator + "Downloads"
                    + "" + File.separator + filename;
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Downloads" + File.separator + filename;
        } else if (PlatformUtil.isMac()) {
            path = "/Users/" + System.getProperty("user.name") + "/Downloads" + File.separator + filename;
        }
        return new File(path);
    }

    @FXML
    private void saveProduct(ActionEvent et) {
        if (tf_input_nomproduit.getText().isEmpty()
                || tf_input_modelproduit.getText().isEmpty()
                || tf_input_markproduit.getText().isEmpty()
                || tf_input_codebarproduit.getText().isEmpty()
                || tile_pn_mesures.getChildren().isEmpty() || cbx_choose_category.getValue() == null) {
            MainUI.notify(null, "Erreur", "Completez au moins tous les 4 premiers champs et une mesure", 4, "error");
            return;
        }
        try {
            byte[] pixa = null;
            Produit p = new Produit(DataId.generate());
            if (choosenFile != null) {
                pixa = FileUtils.readFromFile(choosenFile);
                FileUtils.byteToFile(p.getUid(), pixa);
                p.setImage(pixa);
            } else {
                InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                pixa = FileUtils.readAllBytes(is);
            }
            p.setCategoryId(cbx_choose_category.getValue());
            p.setDateCreation(LocalDateTime.now());
            p.setCodebar(tf_input_codebarproduit.getText());
            p.setNomProduit(tf_input_nomproduit.getText());
            p.setMarque(tf_input_markproduit.getText());
            p.setMethodeInventaire(meth);
            p.setModele(tf_input_modelproduit.getText());
            p.setCouleur(tf_input_couleurproduit.getText());
            p.setTaille(tf_input_tailleproduit.getText());
            Produit pr = ProduitDelegate.findByCodebar(p.getCodebar());//jpas.findWithAndClause(Produit.class,new String[]{"codebar"}, new String[]{p.getCodebar()});
            if (pr != null) {
                MainUI.notify(null, "Erreur", "Le produit ayant le meme codebar existe deja", 4, "error");
                return;
            }
            List<Produit> pros = ProduitDelegate.findByDescription(p.getNomProduit(), p.getMarque(),
                    p.getModele(), p.getTaille());
            if (!pros.isEmpty()) {
                MainUI.notify(null, "Erreur", "Le produit ayant la meme description existe deja", 4, "error");
                return;
            }

            Produit prodi = ProduitDelegate.saveProduit(p);//jpas.insertAndSync(p);

            if (prodi != null) {

                tf_input_codebarproduit.clear();
                tf_input_markproduit.clear();
                ProduitsController pc = ProduitsController.getInstance();
                if (pc != null) {
                    pc.addElemnt(p);
                }
                List<Mesure> mesureSaved = new ArrayList<>();
                for (Node n : tile_pn_mesures.getChildren()) {
                    Label l = (Label) n;
                    String text = l.getText();
                    Mesure mez = new Mesure(DataId.generate());
                    mez.setDescription(text.split(":")[0]);
                    p.setImage(null);
                    mez.setProduitId(new Produit(prodi.getUid()));
                    mez.setQuantContenu(Double.valueOf(text.split(":")[1]));
                    Mesure sm = MesureDelegate.saveMesure(mez);
                    mesureSaved.add(sm);
                    
                }

                sendProduitIfNotExist(prodi, mesureSaved);
                mesureSaved.forEach(m->saveMesureByHttp(m));
                MainUI.notify(null, "Succes", "Produit enregistre avec succes", 6, "Info");

                StoreformController sfc = StoreformController.getInstance();
                if (sfc != null) {
                    sfc.addProduit(prodi);
                }
                PosController pos = PosController.getInstance();
                if (pos != null) {
                    pos.addProductItem(prodi);
                }
                if (pc != null) {
                    MainuiController.getInstance().getInstance().switchToProduct(et);
                }
            }
            tile_pn_mesures.getChildren().clear();
        } catch (IOException ex) {
            Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void deleteCategory(Event e) {
        if (choosenCat == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "La selection est vide");
            alert.setTitle("Selectionez un element!");
            alert.setHeaderText(null);
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer la categorie selectionnées", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);

        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            CategoryDelegate.deleteCategory(choosenCat);
            Executors.newCachedThreadPool()
                    .submit(() -> {

                        Util.sync(choosenCat, Constants.ACTION_DELETE, Tables.CATEGORY);
                    });
            categoriesx.remove(choosenCat);
            MainUI.notify(null, "Succes", "Categorie suprimee avec succes", 4, "Info");
        }
    }

    public void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;

            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if (ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            imageView.setX((imageView.getFitWidth() - w) / 2);
            imageView.setY((imageView.getFitHeight() - h) / 2);

        }
    }

    @FXML
    private void hideCategoryDlg(ActionEvent evt) {
        categorie_pane.setVisible(false);
    }

    @FXML
    private void saveCategoryDlg(ActionEvent evt) {
        if (tf_input_category.getText().isEmpty()) {
            return;
        }
        Category c = new Category(DataId.generate());
        c.setDescritption(tf_input_category.getText());
        List<Category> lscat = CategoryDelegate.findCategories(tf_input_category.getText());
        if (!lscat.isEmpty()) {
            MainUI.notify(null, "Succes", "La categorie existe deja", 6, "Info");
            return;
        }
        Category catrst = CategoryDelegate.saveCategory(c);
        saveCategoryByHttp(c);
        if (catrst != null) {
            categoriesx.add(catrst);
            MainUI.notify(null, "Succes", "La categorie a ete enregistree avec succes", 6, "Info");
            tf_input_category.setText("");
            catrst.setType(Tables.CATEGORY.name());
            catrst.setAction(Constants.ACTION_CREATE);
            catrst.setCount(1);
            catrst.setCounter(1);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.syncModel(catrst, Constants.ACTION_CREATE);
                    });
        }

    }

    @FXML
    private void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
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

    public void setEntreprise(Entreprise entreprise) {
        conf();
        categoriesx = FXCollections.observableArrayList();
        List<Category> cats = CategoryDelegate.findCategories();//jpas.findAll(Category.class);
        System.out.println("Categories ----- XXXXXX SIZE " + cats.size());
        cbx_choose_category.setItems(categoriesx);
        cats.forEach(k -> {
            categoriesx.add(k);
        });
    }

    private void saveMesureByHttp(Mesure c) {
        kazisafe.saveMesure(c).enqueue(new Callback<Mesure>() {
            @Override
            public void onResponse(Call<Mesure> call, Response<Mesure> response) {
                System.out.println("http mesure " + response);
                if (response.isSuccessful()) {
                    System.out.println("mesure ok");
                }
            }

            @Override
            public void onFailure(Call<Mesure> call, Throwable t) {
                System.err.println("erreur http msure " + t.getMessage());
            }
        });
    }

    private void saveCategoryByHttp(Category c) {
        kazisafe.saveCategory(c).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rspns) {
                System.out.println("Categ " + rspns.code());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });
    }

    private void sendProduitIfNotExist(Produit produit, List<Mesure> mesures) {
        byte[] imageBytes = produit.getImage();
        if (choosenFile != null) {
            try {
                imageBytes = FileUtils.readFromFile(choosenFile);
            } catch (IOException ex) {
                Logger.getLogger(ProduitItemController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (imageBytes == null) {
            imageBytes = loadDefaultImage();
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        saveProduitByHttps(produit, base64Image, mesures);
    }

    private byte[] loadDefaultImage() {
        try (InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png")) {
            return FileUtils.readAllBytes(is);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image par défaut" + e.getMessage());
            return new byte[0];
        }
    }

    private void saveProduitByHttps(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = createProduitHelper(produit, base64Image, mesures);
        try {
            Response<Produit> response = kazisafe.saveLite(produitHelper).execute();
            if (response.isSuccessful()) {
                System.out.println("Save synchrone Produit " + response.code());
            } else {
                System.err.println("Erreur lors de l'enregistrement du produit : " + response.code());
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

}
