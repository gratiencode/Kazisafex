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
import delegates.DestockerDelegate;
import delegates.FournisseurDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List; 
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
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
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;
import javax.print.PrintService;

import data.Category;
import data.Destocker;
import data.Entreprise;
import data.Fournisseur;
import data.Livraison;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Refresher;
import data.Stocker;
import org.apache.commons.lang3.math.NumberUtils;
import retrofit2.Call;
import retrofit2.Response;
import tools.Constants;
import tools.DataId;
import tools.DataImporter; 
import tools.DownloadTask;
import tools.FileUtils;
import services.JpaStorage;
import tools.LigneImport;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import data.helpers.Role; import data.network.Kazisafe;
import java.time.LocalDate;
import static java.util.FormatProcessor.FMT;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class ProduitsController implements Initializable {

    public static ProduitsController getInstance() {
        if (instance == null) {
            instance = new ProduitsController();
        }
        return instance;
    }

    @FXML
    private Pagination pagination;
    @FXML
    private TableView<Produit> table;
    @FXML
    ProgressIndicator progress_downxls;
    @FXML
    ImageView btn_import, btn_refresh, btn_update, btn_delete;
    @FXML
    private TableColumn<Produit, String> collcodebar;
    @FXML
    private TableColumn<Produit, String> collnom_produit;
    @FXML
    private TableColumn<Produit, String> collmarque_produit;
    @FXML
    private TableColumn<Produit, String> collmodel_produit;
    @FXML
    private TableColumn<Produit, String> colltaille;
    @FXML
    private TableColumn<Produit, String> collcolor;
    @FXML
    private TableColumn<Produit, String> actions;
    @FXML
    private ComboBox<Category> cbx_catwrap;
    @FXML
    private TextField newCategory;
    @FXML
    private Pane wrapInCatPane;
    @FXML
    private ComboBox<Integer> rowPP;
    @FXML
    private Pane pane_print;
    @FXML
    ImageView imgbtn_print_bc;
    @FXML
    Label txt_nomproselected, txt_codebar_proselected;
    @FXML
    ImageView img_vu_selected_pro, import_img_btn, import_img_btn1, img_codebar;
    @FXML
    Pane pane_select_xlss, pane_4region, pane_wait_import;
    @FXML
    private ComboBox<Category> cbx_choose_category;
    @FXML
    ComboBox<String> cbx_regions;
    @FXML
    private RadioButton thermal, jet;
    ToggleGroup printerGroup;
    @FXML
    private ComboBox<Printer> cbx_printers;
    @FXML
    CheckBox chkbx_stockage;
    Printer defaultPrinter;
    @FXML
    CheckBox chbx_convert_special_codebar;
    @FXML
    private Label count;
    int rowsDataCount = 20;

    //JpaStorage database;

    private String token, role;
    Kazisafe kazisafe;
    ObservableList<Produit> produitsList;
    ObservableList<Category> catList;
    Produit produit;
    Category choosenCat;
    private Entreprise entreprise;
    Preferences pref;
    private static ProduitsController instance;
    private String region;
    private ObservableList<String> regions;
    Set<Produit> selectedproduct;
    boolean xlsvisible = false, stockageonly;
    String choosenregion;
    String localPath;
    boolean downloaded = false;
    private ResourceBundle bundle;

    public ProduitsController() {
       // database = JpaStorage.getInstance();
        produitsList = FXCollections.observableArrayList();
        catList = FXCollections.observableArrayList();
        instance = this;
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
    String euid;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        produitsList = FXCollections.observableArrayList();
        catList = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        selectedproduct = new HashSet<>();
        conf();
        ObservableList<Integer> rows = FXCollections.observableArrayList(Arrays.asList(20, 25, 50, 100, 250, 500, 1000));
        rowPP.setItems(rows);
        rowPP.getSelectionModel().select(0);
        pane_4region.setVisible(false);
        pane_wait_import.setVisible(false);
        pagination.setPageFactory(this::createDataPage);
        wrapInCatPane.setVisible(false);
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        role = pref.get("priv", "Unknown");
        region = pref.get("region", null);
        euid = pref.get("eUid", "");
        
        Tooltip.install(import_img_btn, new Tooltip("Inmporter les produits avec le prix de details seulement"));
        Tooltip.install(import_img_btn1, new Tooltip("Importer les produits avec leurs prix de gros et details"));
        Tooltip.install(btn_import, new Tooltip("Importer les produits depuis un fichier excel"));
        Tooltip.install(btn_refresh, new Tooltip("Actualiser les données"));
        Tooltip.install(btn_update, new Tooltip("Modifier un produit séléctionné"));
        Tooltip.install(btn_delete, new Tooltip("Supprimer un produit séléctionné"));
        Tooltip.install(imgbtn_print_bc, new Tooltip("Imprimer le code bar"));
        localPath = MainUI.cPath("/Media/inventories");
        printerGroup = new ToggleGroup();
        thermal.setToggleGroup(printerGroup);
        jet.setToggleGroup(printerGroup);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Produit>() {
            @Override
            public void changed(ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) {
                if (newValue != null) {
                    selectedproduct.add(newValue);
                }
            }
        });
        ContextMenu cm = new ContextMenu();
        MenuItem mi = new MenuItem(bundle.getString("gorup_incateogey"));
        cm.getItems().add(mi);
        table.setContextMenu(cm);
        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!selectedproduct.isEmpty()) {
                    wrapInCatPane.setVisible(true);
                }
            }
        });
        // TODO
    }

    public void addCategory(Category cat) {
       // if (database != null) {
            Category catx = CategoryDelegate.findCategory(cat.getUid());
                    //database.findByUid(Category.class, cat.getUid());
            if (catx == null) {
                CategoryDelegate.saveCategory(cat);//database.insertOnly(cat);
                Platform.runLater(()->{
                 catList.add(cat);
                });
               
                MainUI.notifySync("Kazisafe-Sync", "Categorie " + cat.getDescritption() + " trouve", "Synchronisation");
            }
       // }
    }
    
    public void appendProduct(String action, Produit p){
        if(action.equals(Constants.ACTION_CREATE)){
            produitsList.add(p);
        }else if(action.equals(Constants.ACTION_UPDATE)){
            Optional<Produit> fp = produitsList.stream().filter(x->x.getUid().equals(p.getUid())).findFirst();
            if(fp.isPresent()){
                produitsList.set(produitsList.indexOf(p), p);
            }
        }else if(action.equals(Constants.ACTION_DELETE)){
            produitsList.remove(p);
        }
    }

    public void addProduct(Produit p) {
      //  if (database != null) {
            Produit pr =ProduitDelegate.findProduit(p.getUid());// database.findByUid(Produit.class, p.getUid());
            if (pr == null) {
                ProduitDelegate.saveProduit(p);//database.insertOnly(p);
                Platform.runLater(()->{
                produitsList.add(p);
                });
            }
       // }
    }

    public void addMesure(Mesure m) {
      //  if (database != null) {
            Mesure ms =MesureDelegate.findMesure(m.getUid());// database.findByUid(Mesure.class, m.getUid());
            if (ms == null) {
               MesureDelegate.saveMesure(m);// database.insertOnly(m);
            }
       // }
    }

    @FXML
    public void printbarcode(Event e) {
        if (thermal.isSelected()) {
            printBCWithThermal(defaultPrinter.getName(), txt_codebar_proselected.getText());
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
        if(text.isEmpty()){
            return;
        }
        InputStream is;
        try {
            if (NumberUtils.isDigits(text) && (text.length() == 12 | text.length() == 13)) {
                EAN13Writer writer = new EAN13Writer();
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
    private void downloadProductXls(Event event) {
        File f = new File(localPath + File.separator + "Produit-inventories.xls");
        if (f.exists()) {
            progress_downxls.setVisible(true);
            downloaded = true;
        } else {
            downloaded = false;
        }
        if (!downloaded) {
            Task<Void> downTask = new DownloadTask("Produit-inventories.xls", localPath);
            downTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                    if (downTask.getState() == newValue.SUCCEEDED) {
                        MainUI.notify(null, "Succes", "Le téléchargemement a été effectué avec succès", 4, "info");
                        downloaded = true;
                    } else if (downTask.getState() == newValue.FAILED) {
                        MainUI.notify(null, "Erreur", "Le téléchargemement a été interrompu", 4, "error");
                    }
                }
            });
            progress_downxls.setVisible(true);
            progress_downxls.progressProperty().bind(downTask.progressProperty());
            Thread kazi = new Thread(downTask);
            kazi.setDaemon(true);
            kazi.start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (IOException ex) {
                        Logger.getLogger(RepportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        }
    }

    @FXML
    private void createFinAccount(ActionEvent event) {
    }

    @FXML
    public void viewProductDialog(ActionEvent evt) {
        MainUI.floatDialog(tools.Constants.PRODUCT_DLG, 600, 790, token, kazisafe, this.entreprise, null);
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
    private void deleteFinAccount(MouseEvent event) {
    }

    @FXML
    private void updateFinAccount(MouseEvent event) {
        MainUI.floatDialog(tools.Constants.PRODUCT_DLG, 600, 790, token, kazisafe, entreprise, produit);
    }

    @FXML
    private void refreshFinAccount(MouseEvent event) {
        table.setItems(produitsList);
        //MainuiController.getInstance().getInstance().switchToProduct(event);

        refreshFromCloud();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                count.setText(table.getItems().size() + " éléments");
            }
        });
        SyncEngine.getInstance().restart();
    }

    public void addElemnt(Produit p) {
        if (produitsList != null) {
            produitsList.add(p);
        }
    }

    public void replaceElemnt(Produit oldPro, Produit newPro) {
        if (produitsList != null) {
            int index = produitsList.indexOf(oldPro);
            produitsList.set(index, newPro); 
        }
    }

    @FXML
    public void groupInCategory(Event e) {
        new Thread(() -> {
            JpaStorage jpa = JpaStorage.getInstance();
            Category value = cbx_catwrap.getValue();
            if (value != null) {
                for (Produit pr : selectedproduct) {
                    pr.setCategoryId(value);
                    jpa.update(pr);
                }
            } else {
                if (!newCategory.getText().isEmpty()) {

                    Category x = new Category(DataId.generate());
                    x.setDescritption(newCategory.getText());
                    Category catx = jpa.insertAndSync(x);
                    for (Produit pr : selectedproduct) {
                        pr.setCategoryId(catx);
                        jpa.update(pr);
                    }

                }
            }
            MainUI.notify(null, bundle.getString("success"), bundle.getString("xsuccess_saved"), 3, "info");
            closeBox(e);
        }).start();
    }

    public void setToken(String token) {
        this.token = token;
        kazisafe = KazisafeServiceFactory.createService(token);
        //this.database = JpaStorage.getInstance();
        //mesureLs = mesures.findAll();
        catList.addAll(CategoryDelegate.findCategories());
        produitsList.addAll(ProduitDelegate.findProduits());
        table.setItems(produitsList);
        cbx_choose_category.setItems(catList);
        cbx_catwrap.setItems(catList);
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        System.out.println("Printewrs count " + osp.size());
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();
        cbx_printers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Printer>() {
            @Override
            public void changed(ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) {
                defaultPrinter = newValue;
            }
        });
        cbx_printers.getSelectionModel().select(defaultPrinter);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                count.setText(table.getItems().size() + " éléments");
            }
        });
        cbx_regions.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue != null) {
                            region = newValue;
                        }
                    }
                });

        kazisafe.getRegions(euid).enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        pref.put("region" + (++i), reg);
                    }
                    System.err.println("Agent regions " + lreg.size());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable thrwbl) {
                for (String key : regKeys()) {
                    String r = pref.get(key, "...");
                    if (!regions.contains(r)) {
                        regions.add(r);
                    }
                }
            }
        });
        //sync mez
//        refreshFromCloud();
        instance = this;
    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        for (Printer p : osp) {
            rst.add(p);
        }
        return rst;
    }

//    private List<Produit> getProductWithNullMesure(List<Produit> ps) {
//        List<Produit> result = new ArrayList<>();
//        for (Produit p : ps) {
//            List<Mesure> lis = Util.findMesureForProduitWithId(mesures.findAll(), p.getUid());
//            if (lis.isEmpty()) {
//                result.add(p);
//            }
//        }
//        return result;
//    }
//    private List<String> getProductIDWithNullMesure(List<Produit> ps) {
//        List<String> result = new ArrayList<>();
//        for (Produit p : ps) {
//            List<Mesure> lis = Util.findMesureForProduitWithId(mesures.findAll(), p.getUid());
//            if (lis.isEmpty()) {
//                result.add(p.getUid());
//            }
//        }
//        return result;
//    }
    private void refreshFromCloud() {
         Executors.newSingleThreadExecutor()
                .execute(() -> {
                    Refresher rfr = new Refresher("PRODUITS");
                    rfr.setAction("read");
                    rfr.setCount(1);
                    rfr.setCounter(1);
                    Util.sync(rfr, "read", Tables.REFRESH);
                });
    }

//    private List<Mesure> findMesure(List<Mesure> allMez, String produitId) {
//        List<Mesure> lmz = new ArrayList<>();
//        for (Mesure m : allMez) {
//            if (m.getProduitId().getUid().equals(produitId)) {
//                lmz.add(m);
//            }
//        }
//        return lmz;
//    }
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
        cbx_catwrap.setConverter(new StringConverter<Category>() {
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
            if (choosenCat == null) {
                table.setItems(produitsList);
            } else {
                List<Produit> pros = findProductByCategory(choosenCat.getUid());
                table.setItems(FXCollections.observableArrayList(pros));
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    count.setText(table.getItems().size() + " éléments");
                }
            });

        });
        collcodebar.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> new SimpleStringProperty(param.getValue().getCodebar()));
        collnom_produit.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> new SimpleStringProperty(param.getValue().getNomProduit()));
        collmarque_produit.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> new SimpleStringProperty(param.getValue().getMarque()));
        collmodel_produit.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> new SimpleStringProperty(param.getValue().getModele()));
        colltaille.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> new SimpleStringProperty(param.getValue().getTaille()));
        collcolor.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> new SimpleStringProperty(param.getValue().getCouleur()));
        actions.setCellValueFactory((TableColumn.CellDataFeatures<Produit, String> param) -> {
            Produit pr = param.getValue();
            System.err.println("Produit de la table de donnneee " + pr);
            List<Mesure> lm =MesureDelegate.findMesureByProduit(pr.getUid());// database.findByProduit(Mesure.class, pr.getUid());
            System.err.println("PROMEZ ---> " + lm.size() + " " + pr.getCodebar());
            String mzmsg = lm.isEmpty() ? "Pas de mesure" : "Mesures (" + lm.size() + ")";
            return new SimpleStringProperty(mzmsg);
        });
        actions.setCellFactory(new Callback<TableColumn<Produit, String>, TableCell<Produit, String>>() {
            @Override
            public TableCell<Produit, String> call(TableColumn<Produit, String> param) {
                return new TextFieldTableCell<>();
            }
        });
        ObservableList<String> actionList = FXCollections.observableArrayList("Pas de mesure", "Ajouter une mesure", "Modifier");
        actions.setCellFactory(ChoiceBoxTableCell.forTableColumn(actionList));
        actions.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Produit, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Produit, String> event) {
                String value = event.getNewValue();
                if (produit == null) {
                    return;
                }
                if (value.equals("Ajouter une mesure")) {
                    MainUI.floatDialog(tools.Constants.MESURE_DLG, 433, 490, token, kazisafe, entreprise, produit);
                } else if (value.equals("Modifier")) {
                    MainUI.floatDialog(tools.Constants.PRODUCT_DLG, 600, 790, token, kazisafe, entreprise, produit);
                } else if (value.equals("Supprimer")) {

                }
            }
        });
        table.setEditable(true);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Produit>() {
            @Override
            public void changed(ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) {
                produit = newValue;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (produit == null) {
                            return;
                        }
                        txt_codebar_proselected.setText(produit.getCodebar());
                        showcodebar(produit.getCodebar());
                        txt_nomproselected.setText(produit.getNomProduit() + " " + produit.getMarque() + " " + produit.getModele() + " "
                                + (produit.getTaille() == null ? "" : produit.getTaille()) + " " + (produit.getCouleur() == null ? "" : produit.getCouleur()));
                        File f = FileUtils.pointFile(produit.getUid() + ".jpeg");
                        InputStream is;
                        if (!f.exists()) {
                            is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                            f = FileUtils.streamTofile(is);
                        }
                        Image image = null;
                        try {
                            image = new Image(new FileInputStream(f));
                            img_vu_selected_pro.setImage(image);
                            Util.centerImage(img_vu_selected_pro);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });
            }
        });
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Produit>() {
            @Override
            public void changed(ObservableValue<? extends Produit> observable, Produit oldValue, Produit newValue) {
                produit = newValue;
            }
        });
    }

    @FXML
    private void selectRowPerPage(ActionEvent evt) {
        ComboBox cbx = (ComboBox) evt.getSource();
        rowsDataCount = (int) cbx.getSelectionModel().getSelectedItem();
        pagination.setPageFactory(this::createDataPage);
        System.out.println("Row set to " + rowsDataCount);
    }

    private Node createDataPage(int pgindex) {
        try {
            int offset = pgindex * rowsDataCount;
            int limit = Math.min(offset + rowsDataCount, produitsList.size());
            table.setItems(FXCollections.observableArrayList(produitsList.subList(offset, limit)));
        } catch (java.lang.IllegalArgumentException e) {
            pagination.setPageCount(pgindex);
            System.out.println("Page suivante non disponible");
        }
        return table;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    private List<Produit> findProductByCategory(String categoString) {
        List<Produit> rst = new ArrayList<>();
        for (Produit p : produitsList) {
            if (p.getCategoryId().getUid().equals(categoString)) {
                rst.add(p);
            }
        }
        return rst;
    }

    public void searchProduit(String produit) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ObservableList<Produit> produits = FXCollections.observableArrayList();
                if (produit != null) {
                    for (Produit p : produitsList) {
                        String comp = p.getCodebar() + " " + p.getCouleur() + " " + p.getMarque() + ""
                                + " " + p.getModele() + " " + p.getNomProduit() + " " + p.getTaille() + " "
                                + p.getMethodeInventaire();
                        if (comp.toUpperCase().contains(produit.toUpperCase())) {
                            if (Util.find(produits, p.getUid()) == null) {
                                produits.add(p);
                            }
                        }
                    }
                    table.getItems().clear();
                    table.setItems(produits);
                } else {
                    table.getItems().clear();
                    table.setItems(produitsList);
                }
                count.setText(table.getItems().size() + " éléments");
            }
        });
    }

    @FXML
    public void deleteProduct(Event e) {
        if (produit == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "La selection est vide");
            alert.setTitle("Selectionez un element!");
            alert.setHeaderText(null);
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer le produit selectionné", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        Optional<ButtonType> showAndWait = alert.showAndWait();
        if (showAndWait.get() == ButtonType.YES) {
            Executors.newSingleThreadExecutor()
                    .execute(()->{
                  
            List<Stocker> stox = StockerDelegate.findStockerByProduit(produit.getUid());//database.findByProduit(Stocker.class, produit.getUid());
            // Util.findStockersForProduit(nstocker.findAll(),);
            if (!stox.isEmpty()) {
                MainUI.notify(null, "Erreur", "Produit non supprimé, car il possède au moins un stock", 3, "error");
                return;
            }
            List<Mesure> ms = MesureDelegate.findMesureByProduit(produit.getUid());
            for (Mesure m : ms) {
                MesureDelegate.deleteMesure(m);
            }
            ProduitDelegate.deleteProduit(produit);
           // database.delete(produit);
            produitsList.remove(produit);
            MainUI.notify(null, "Succès", "Produit supprimé avec succès", 3, "Info");

            if (role.equals(Role.Trader.name())) {
                kazisafe.deleteProduit(role);
            }  
                    });

        }
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

    private void fillSites() {
        cbx_regions.setItems(regions);
        cbx_regions.getSelectionModel().selectFirst();
    }

    @FXML
    public void importDatasXls(Event evt) {
        fillSites();
        pane_4region.setVisible(true);
    }

    @FXML
    public void closeBox(Event e) {
        Node n = (Node) e.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
    }

    @FXML
    public void nextImport(Event evt) {
        String meth = pref.get("meth", "fifo");
        pane_4region.setVisible(false);
        Node node = (Node) evt.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer les produits depuis un fichier excel");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier excel", "*.xls"));
        final File choosenFile = fileChooser.showOpenDialog(thisStage);
        if (choosenFile != null) {
            pane_wait_import.setVisible(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String impId = "IMP" + ((int) (Math.random() * 1000000)) + "D";
                        List<DataImporter> datas = Util.importFromExcel(choosenFile, ProduitDelegate.findProduits(), region, chbx_convert_special_codebar.isSelected());
                        Fournisseur fx = FournisseurDelegate.findFournisseur(entreprise.getUid());
                        Fournisseur f = new Fournisseur(entreprise.getUid());
                        f.setAdresse(entreprise.getAdresse());

                        f.setIdentification(entreprise.getIdentification());
                        f.setNomFourn(entreprise.getNomEntreprise());
                        f.setPhone(entreprise.getPhones());
                        Livraison liv = new Livraison(DataId.generate());
                        liv.setObservation("");
                        liv.setDateLivr(LocalDate.now());
                        liv.setNumPiece(impId);
                        liv.setReference(impId);
                        liv.setLibelle("Stock Initial");
                        if (fx == null) {
                            liv.setFournId(f);
                            FournisseurDelegate.saveFournisseur(f);
                        } else {
                            liv.setFournId(fx);
                        }
                        liv.setRegion(region);
                        double somme = sumStockDetail(datas);
                        liv.setTopay(somme);
                        liv.setPayed(somme);
                        LivraisonDelegate.saveLivraison(liv);//database.insertAndSync(liv);
                        List<Category> cats = CategoryDelegate.findCategories("Divers");//database.getXDescritpion(Category.class, "Divers");
                        //nscategory.findAll("", "Divers");
                        Category cat;
                        if (cats.isEmpty()) {
                            Category cate = new Category(DataId.generate());
                            cate.setDescritption("Divers");
                            cat = CategoryDelegate.saveCategory(cate);
                            System.err.println("Insertion new category " + cat);
                        } else {
                            cat = cats.get(0);
                        }
                        for (DataImporter d : datas) {
                            Produit p = d.getProduct();
                            if (p.getUid().equals("")) {
                                continue;
                            }
                            Stocker s = d.getStock();
                            s.setNumlot(Constants.TIMESTAMPED_FORMAT.format(new Date()));
                            s.setLivraisId(liv);
                            p.setCategoryId(cat);
                            Destocker des = d.getDestockage();
                            des.setLibelle("Destockage " + impId);
                            des.setRegion(region);
                            des.setNumlot(s.getNumlot());
                            des.setObservation("Importation destockage");
                            des.setDestination(region);
                            Recquisition rq = d.getRecquisition();
                            rq.setNumlot(s.getNumlot());
                            PrixDeVente price = d.getSalePrice();
                            System.err.println("IMPORT- PRODUCTControl stock.pro = " + d.getStock().getProductId() + ""
                                    + " destock.pro : " + d.getDestockage().getProductId() + " "
                                    + " Recquis.pro : " + d.getRecquisition().getProductId() + " PRODUCT = " + d.getProduct());

                            Produit pxs = ProduitDelegate.findByCodebar(p.getCodebar());//database.findWithAndClause(Produit.class, new String[]{"codebar"}, new String[]{p.getCodebar()});
                            Produit inserted;
                            if (pxs==null) {
                                p.setCategoryId(cat);
                                inserted = ProduitDelegate.saveProduit(p);//database.insertAndSync(p);
                            } else {
                                inserted = pxs;//.get(0);
                            }
                            //Util.findProduitByCodebar(database.findAll(Produit.class), p.getCodebar());

                            System.out.println("Produit inserted >> " + inserted);
                            Mesure pcs;
                            List<Mesure> ms = MesureDelegate.findMesureByProduit(inserted.getUid(), "Pcs");//database.findWithAndClause(Mesure.class, new String[]{"produit_id", "description"}, new String[]{inserted.getUid(), "Pcs"});
                            if (ms.isEmpty()) {
                                pcs = new Mesure(DataId.generate());
                                pcs.setDescription("Pcs");
                                pcs.setQuantContenu(1d);
                                pcs.setProduitId(inserted);
                                MesureDelegate.saveMesure(pcs);
                               // database.insertAndSync(pcs);
                            } else {
                                pcs = ms.get(0);
                            }
                            produitsList.add(p);
                            des.setMesureId(pcs);
                            s.setMesureId(pcs);
                            s.setProductId(inserted);
                            rq.setMesureId(pcs);
                            des.setProductId(inserted);
                            price.setMesureId(pcs);
                            des.setReference(impId);
                            s.setLibelle("Stockage " + impId);
                            s.setReduction(0);
                            s.setObservation("Stockage importe");
                            rq.setObservation("Pret a la vente");
                            rq.setReference(des.getReference());
                            rq.setProductId(inserted);
                            rq.setRegion(region);
                            price.setPourcentParCunit(0d);
                            price.setRecquisitionId(rq);
                            System.out.println("Article = " + p);
                            if (s.getProductId() != null && s.getMesureId() != null) {
                                StockerDelegate.saveStocker(s);//database.insertAndSync(s);
                            }
                            System.err.println("IMPORT PRODCONTROLLER stock.pro = " + s.getProductId() + ""
                                    + " destock.pro : " + des.getProductId() + " "
                                    + " Recquis.pro : " + rq.getProductId());
                            if (!chkbx_stockage.isSelected()) {
                                des.setDestination(cbx_regions.getValue());
                                des.setRegion(cbx_regions.getValue());
                                DestockerDelegate.saveDestocker(des);//database.insertAndSync(des);
                                rq.setRegion(cbx_regions.getValue());
                                RecquisitionDelegate.saveRecquisition(rq);//database.insertAndSync(rq);
                                price.setRecquisitionId(rq);
                                PrixDeVenteDelegate.savePrixDeVente(price);//database.insertAndSync(price);
                            }

                        }

                        MainUI.notify(null, "Succes!", "Importation des produits términée", 3, "Info");
                        pane_select_xlss.setVisible(false);
                        xlsvisible = false;
                        pane_wait_import.setVisible(false);
                    } catch (IllegalStateException ex) {
                        MainUI.notify(null, "Erreur!", "Importation échouée, format des cellules non supporté", 4, "error");
                        ex.printStackTrace();
                    }
                }
            }).start();

        }
    }

    @FXML
    public void importGrosXls(Event evt) {
        String meth = pref.get("meth", "fifo");
        Node node = (Node) evt.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer les produits depuis un fichier excel");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier excel", "*.xls"));
        final File choosenFile = fileChooser.showOpenDialog(thisStage);
        if (choosenFile != null) {
            pane_wait_import.setVisible(true);
            SyncEngine.getInstance().shutdown();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        boolean canProceed = true;
                        String ref = "IMP" + (int) (Math.random() * 1000000) + "G";
                        Livraison livraison = new Livraison(DataId.generate());
                        livraison.setDateLivr(LocalDate.now());
                        livraison.setLibelle("importation en gros");
                        livraison.setNumPiece(ref);
                        livraison.setReference(ref);
                        livraison.setRegion(region);
                        livraison.setObservation("");
                        livraison.setToreceive(0d);
                        livraison.setRemained(0d);
                        livraison.setReduction(0d);
                        List<Category> cats = CategoryDelegate.findCategories("Divers");//database.getXDescritpion(Category.class, "Divers");
                        //nscategory.findAll("", "Divers");
                        Category cat;
                        if (cats.isEmpty()) {
                            Category cate = new Category(DataId.generate());
                            cate.setDescritption("Divers");
                            cat = CategoryDelegate.saveCategory(cate);
                            System.err.println("Insertion new category " + cat);
                        } else {
                            cat = cats.get(0);
                        }
                        if(entreprise==null){
                            MainUI.notify(null, "Erreur de chargement", "Impossible d'importer avant le chargement complet de l'entreprise", 3,"error");
                            return;
                        }
                        livraison.setRegion(region);
                        Fournisseur fx = FournisseurDelegate.findFournisseur(entreprise.getUid());
                        Fournisseur f = new Fournisseur(entreprise.getUid());
                        f.setAdresse(entreprise.getAdresse());
                        f.setIdentification(entreprise.getIdentification());
                        f.setNomFourn(entreprise.getNomEntreprise());
                        f.setPhone(entreprise.getPhones());
                        if (fx == null) {
                            fx = FournisseurDelegate.saveFournisseur(f);
                        }
                        livraison.setFournId(fx);
                        List<LigneImport> imps = Util.importGrosFromExcel(choosenFile, ProduitDelegate.findProduits(),
                                region, chbx_convert_special_codebar.isSelected(), ref, meth);
                        System.out.println("Size imps " + imps.size());
                        double[] pay = topayAndPayed(imps);
                        livraison.setTopay(pay[0]);
                        livraison.setPayed(pay[1]);
                        Livraison lins =LivraisonDelegate.saveLivraison(livraison);// database.insertAndSync(livraison);
                        //RecquisitionDelegate.beginTransaction();
                        //PrixDeVenteDelegate.beginTransaction();
                        for (LigneImport imp : imps) {
                            Produit px;
                            Produit p = imp.getProduit();
                            Produit pxs = ProduitDelegate.findByCodebar(p.getCodebar());//database.findWithAndClause(Produit.class, new String[]{"codebar"}, new String[]{p.getCodebar()});
                            if (pxs==null) {
                                p.setCategoryId(cat);
                                px = ProduitDelegate.saveProduit(p);//database.insertAndSync(p);
                            } else {
                                px = pxs;//.get(0);
                            }
                            Stocker s = imp.getStocker();
                            Destocker d = imp.getDestocker();
                            Recquisition r = imp.getRecquisition();
                            Mesure m = imp.getMesure();
                            Mesure mex;
                            m.setProduitId(px);
                            List<Mesure> ms = MesureDelegate.findMesureByProduit(px.getUid(), m.getDescription());//database.findWithAndClause(Mesure.class, new String[]{"produit_id", "description"}, new String[]{px.getUid(), m.getDescription()});
                            if (ms.isEmpty()) {
                                mex =MesureDelegate.saveMesure(m);// database.insertAndSync(m);
                            } else {
                                mex = ms.get(0);
                            }
                            s.setLivraisId(lins);

                            List<Stocker> ls = StockerDelegate.findStockerByProduitLot(px.getUid(), s.getNumlot());// database.findByProduitWithLot(Stocker.class, px.getUid(), s.getNumlot());
                            if (ls.isEmpty()) {
                                Mesure mz=MesureDelegate.findMesure(s.getMesureId().getUid());
                                if(mz==null){
                                    mz=new Mesure(DataId.generate());
                                    mz.setDescription("pcs");
                                    mz.setQuantContenu(1d);
                                    mz.setProduitId(px);
                                    Mesure msv = MesureDelegate.saveMesure(mz);
                                    s.setMesureId(msv);
                                    d.setMesureId(msv);
                                }
                                StockerDelegate.saveStocker(s);//database.insertAndSync(s);
                                DestockerDelegate.saveDestocker(d);//database.insertAndSync(d);
                            }
                            r.setMesureId(mex);
                            Recquisition rq;
                            List<Recquisition> lrs =RecquisitionDelegate.findRecquisitionByProduit(px.getUid(), r.getNumlot());
                                   // database.findByProduitWithLot(Recquisition.class, px.getUid(), r.getNumlot());
                            if (lrs.isEmpty()) {
                                rq =RecquisitionDelegate.saveRecquisition(r);
                                       // RecquisitionDelegate.appendToTransaction(r);//database.insertAndSync(r);
                            } else {
                                rq = lrs.get(0);
                            }
                            List<PrixDeVente> lpvs = imp.getSalesPrices();
                            for (PrixDeVente lpv : lpvs) {
                                lpv.setRecquisitionId(rq);
                                lpv.setMesureId(mex);
                                //PrixDeVenteDelegate.appendToTransaction(lpv);
                                PrixDeVenteDelegate.savePrixDeVente(lpv);//database.insertAndSync(lpv);
                            }
                        }
                        //RecquisitionDelegate.endTransaction();
                        //PrixDeVenteDelegate.endTransaction();
                        
                        MainUI.notify(null, "Succes!", "Importation des produits términée", 3, "Info");
//                        SyncEngine.getInstance().restart();
                        pane_select_xlss.setVisible(false);
                        xlsvisible = false;
                        pane_wait_import.setVisible(false);
                    } catch (IllegalStateException ex) {
                        MainUI.notify(null, "Erreur", "Importation échouée, format des cellules non supporté", 4, "error");
                        ex.printStackTrace();
                    }
                }
            }).start();

        }
    }

    private double sumStockDetail(List<DataImporter> datas) {
        double somm = 0;
        for (DataImporter data : datas) {
            Stocker s = data.getStock();
            somm += s.getPrixAchatTotal();
        }
        return somm;
    }

    private double[] topayAndPayed(List<LigneImport> imps) {
        double topay = 0, payed = 0;
        for (LigneImport imp : imps) {
            Stocker s = imp.getStocker();
            topay += s.getPrixAchatTotal();
            payed += s.getPrixAchatTotal();
        }
        return new double[]{topay, payed};
    }

    @FXML
    public void importFromExcel(Event evt) {
        if (!xlsvisible) {
            pane_select_xlss.setVisible(true);
            xlsvisible = true;
        } else {
            pane_select_xlss.setVisible(false);
            xlsvisible = false;
        }

    }

}
