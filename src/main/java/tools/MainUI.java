/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.endeleya.kazisafex.AgentController;
import com.endeleya.kazisafex.ClientController;
import com.endeleya.kazisafex.ClotureController;
import com.endeleya.kazisafex.DeliveryController;
import com.endeleya.kazisafex.DestockController;
import com.endeleya.kazisafex.EntrepriseController;
import com.endeleya.kazisafex.FichedestockController;
import com.endeleya.kazisafex.GoodstorageController;
import com.endeleya.kazisafex.ImmobilisationController;
import com.endeleya.kazisafex.Kazisafex;
import com.endeleya.kazisafex.MainuiController;
import com.endeleya.kazisafex.MezureController;
import com.endeleya.kazisafex.PanierappenderController;
import com.endeleya.kazisafex.ParametreController;
import com.endeleya.kazisafex.PaymentController;
import com.endeleya.kazisafex.PosController;
import com.endeleya.kazisafex.ProductionController;
import com.endeleya.kazisafex.ProduitItemController;
import com.endeleya.kazisafex.ProduitsController;
import com.endeleya.kazisafex.RecqController;
import com.endeleya.kazisafex.ReleveeController;
import com.endeleya.kazisafex.RepportController;
import com.endeleya.kazisafex.StoreformController;
import com.endeleya.kazisafex.SuppliersController;
import com.endeleya.kazisafex.TresorerieController;
import data.Client;
import data.ClientOrganisation;
import data.Destocker;
import data.Entreprise;
import data.Facture;
import data.Fournisseur;
import data.Inventaire;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.User;
import data.Vente;
import data.helpers.LoginWebResult;
import data.network.Kazisafe;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;
//import org.controlsfx.control.Notifications;
import services.PlatformUtil;

//import utilities.LoginResult;

/**
 *
 * @author eroot
 */
public class MainUI {

    private static double xOffset = 0;
    private static double yOffset = 0;
    private static final int TOLERANCE_THRESHOLD = 0xFF;
    private static final java.util.List<Stage> activeToasts =
        new java.util.concurrent.CopyOnWriteArrayList<>();
    public static Stage mainStage;

    /**
     * Charge la vue principale. Retourne {@code true} si le chargement a réussi,
     * {@code false} en cas d'erreur. Les appelants doivent vérifier la valeur
     * de retour avant de fermer la fenêtre de connexion.
     */
    public static boolean loadMainView(
        Class theClass,
        String fxmlPath,
        double h,
        double w,
        LoginWebResult loginr
    ) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                theClass.getResource("/guis/" + fxmlPath),
                Kazisafex.getInstance().getLangageBundle()
            );
            Parent main = fxmlLoader.load();
            MainuiController controller =
                fxmlLoader.<MainuiController>getController();
            controller.setLoginResult(loginr);
            mainStage = new Stage();
            Scene scene = new Scene(main, w, h);
            Kazisafex.applyTheme(scene);
            ancienh = h * 0.9;
            ancienw = w * 0.9;
            mainStage.initStyle(StageStyle.UNDECORATED);
            mainStage.setScene(scene);
            mainStage.show();
            mainStage.getScene().setRoot(main);
            mainStage.getScene().getWindow().setHeight(h);
            mainStage.getScene().getWindow().setWidth(w);
            mainStage.getScene().getWindow().setX(1);
            mainStage.getScene().getWindow().setY(1);
            mainStage
                .getIcons()
                .add(
                    new Image(
                        Kazisafex.class.getResourceAsStream(
                            "/icons/icone_ksf.png"
                        )
                    )
                );
            mainStage.centerOnScreen();
            main.setOnMousePressed((javafx.scene.input.MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            main.setOnMouseDragged((javafx.scene.input.MouseEvent event) -> {
                mainStage.setX(event.getScreenX() - xOffset);
                mainStage.setY(event.getScreenY() - yOffset);
            });
            ismax = true;
            return true;
        } catch (Exception ex) {
            Logger.getLogger(MainUI.class.getName()).log(
                Level.SEVERE,
                "Echec chargement vue principale",
                ex
            );
            return false;
        }
    }

    public static void minimize() {
        if (mainStage == null) {
            return;
        }
        mainStage.setIconified(true);
    }

    static boolean ismax = false;
    static double ancienh = 0,
        ancienw = 0;

    public static void enlarge() {
        if (mainStage == null) {
            return;
        }

        if (!ismax) {
            ismax = true;
            ancienh = mainStage.getScene().getWindow().getHeight();
            ancienw = mainStage.getScene().getWindow().getWidth();
            Rectangle2D vr = Screen.getPrimary().getVisualBounds();
            mainStage.getScene().getWindow().setHeight(vr.getHeight());
            mainStage.getScene().getWindow().setWidth(vr.getWidth());
            // mainStage.setMaximized(true);
        } else {
            ismax = false;
            mainStage.getScene().getWindow().setHeight(ancienh);
            mainStage.getScene().getWindow().setWidth(ancienw);
        }
    }

    public static void setShadowEffect(Node node) {
        if (Platform.isSupported(ConditionalFeature.EFFECT)) {
            node.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.8)));
        }
    }

    public static void setShadowAlertEffect(Node node) {
        if (Platform.isSupported(ConditionalFeature.EFFECT)) {
            DropShadow dsh = new DropShadow(8, Color.rgb(255, 0, 0, 0.5));
            dsh.setSpread(0.9);
            node.setEffect(dsh);
        }
    }

    public static void notifySync(
        String title,
        String message,
        String tooltip
    ) {
        playSyncSound();
        showToast("info", message, 4, Pos.BOTTOM_RIGHT);
    }

    private static void playSyncSound() {
        try {
            var url = MainUI.class.getResource("/icons/notify_sound.mp3");
            if (url != null) {
                new AudioClip(url.toExternalForm()).play();
            }
        } catch (Exception ex) {
            System.err.print(ex);
        }
    }

    public static void removeShaddowEffect(Node node) {
        if (Platform.isSupported(ConditionalFeature.EFFECT)) {
            node.setEffect(null);
        }
    }

    /**
     * Rebuilds virtualized controls after a cached page is reattached to the
     * scene graph. This only refreshes rendering; it never reloads controller
     * data from the database.
     * <p>
     * A plain {@code refresh()} is not enough: after detach/reattach, JavaFX
     * VirtualFlow often keeps stale row heights (large blank gaps) and stops
     * reacting to item/list updates (search looks "dead").
     */
    public static void refreshCachedPage(Node node) {
        if (node == null) {
            return;
        }
        if (node instanceof TableView<?> tableView) {
            rebuildVirtualizedItems(tableView);
            return;
        }
        if (node instanceof TreeTableView<?> treeTableView) {
            rebuildTreeTable(treeTableView);
            return;
        }
        if (node instanceof ListView<?> listView) {
            rebuildVirtualizedItems(listView);
            return;
        }
        if (node instanceof Pagination pagination) {
            int currentPage = pagination.getCurrentPageIndex();
            // Nudge the page factory so the current page content is rebuilt
            if (pagination.getPageCount() > 1) {
                pagination.setCurrentPageIndex(
                    currentPage == 0 ? 1 : 0
                );
            }
            pagination.setCurrentPageIndex(currentPage);
            return;
        }
        if (node instanceof TabPane tabPane) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getContent() != null) {
                    refreshCachedPage(tab.getContent());
                }
            }
            tabPane.applyCss();
            tabPane.requestLayout();
            return;
        }
        if (node instanceof ScrollPane scrollPane) {
            refreshCachedPage(scrollPane.getContent());
            scrollPane.applyCss();
            scrollPane.requestLayout();
            return;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                refreshCachedPage(child);
            }
            parent.applyCss();
            parent.requestLayout();
        } else {
            node.applyCss();
        }
    }

    /**
     * Forces VirtualFlow to discard cached cells and remeasure row heights by
     * briefly clearing then restoring the items list.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void rebuildVirtualizedItems(Control control) {
        ObservableList items;
        int selectedIndex = -1;
        Runnable clearSelection;
        Runnable restoreItems;
        Runnable selectAgain;

        if (control instanceof TableView tableView) {
            items = tableView.getItems();
            selectedIndex = tableView.getSelectionModel().getSelectedIndex();
            final int sel = selectedIndex;
            clearSelection = () -> tableView.getSelectionModel().clearSelection();
            restoreItems = () -> {
                tableView.setItems(null);
                tableView.layout();
                tableView.setItems(items);
                tableView.refresh();
            };
            selectAgain = () -> {
                if (sel >= 0 && items != null && sel < items.size()) {
                    tableView.getSelectionModel().select(sel);
                    tableView.scrollTo(Math.min(sel, Math.max(0, items.size() - 1)));
                }
            };
        } else if (control instanceof ListView listView) {
            items = listView.getItems();
            selectedIndex = listView.getSelectionModel().getSelectedIndex();
            final int sel = selectedIndex;
            clearSelection = () -> listView.getSelectionModel().clearSelection();
            restoreItems = () -> {
                listView.setItems(null);
                listView.layout();
                listView.setItems(items);
                listView.refresh();
            };
            selectAgain = () -> {
                if (sel >= 0 && items != null && sel < items.size()) {
                    listView.getSelectionModel().select(sel);
                    listView.scrollTo(Math.min(sel, Math.max(0, items.size() - 1)));
                }
            };
        } else {
            return;
        }

        if (items == null) {
            return;
        }

        clearSelection.run();
        restoreItems.run();
        control.applyCss();
        control.requestLayout();
        selectAgain.run();
        // Force skin/VirtualFlow to remeasure after the control has a real size
        // again (common after detach/reattach of a cached page).
        control.setVisible(false);
        control.setVisible(true);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void rebuildTreeTable(TreeTableView treeTableView) {
        TreeItem root = treeTableView.getRoot();
        TreeItem selected = (TreeItem) treeTableView
            .getSelectionModel()
            .getSelectedItem();
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.setRoot(null);
        treeTableView.layout();
        treeTableView.setRoot(root);
        treeTableView.refresh();
        treeTableView.applyCss();
        treeTableView.requestLayout();
        if (selected != null) {
            treeTableView.getSelectionModel().select(selected);
        }
    }

    public static void setPattern(DatePicker dtpk) {
        dtpk.setConverter(
            new StringConverter<LocalDate>() {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd"
                );

                @Override
                public String toString(LocalDate date) {
                    if (date != null) {
                        return dateFormatter.format(date);
                    } else {
                        return "";
                    }
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        return LocalDate.parse(string, dateFormatter);
                    } else {
                        return null;
                    }
                }
            }
        );
    }

    public static Image makeTransparent(Image inputImage) {
        int W = (int) inputImage.getWidth();
        int H = (int) inputImage.getHeight();
        if (W <= 0 || H <= 0) {
            return null;
        }
        WritableImage outputImage = new WritableImage(W, H);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = reader.getArgb(x, y);

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (
                    r >= TOLERANCE_THRESHOLD &&
                    g >= TOLERANCE_THRESHOLD &&
                    b >= TOLERANCE_THRESHOLD
                ) {
                    argb &= 0x00FFFFFF;
                }

                writer.setArgb(x, y, argb);
            }
        }

        return outputImage;
    }

    public static String createFileWithPath(
        String folderPath,
        String filename
    ) {
        String path;
        if (PlatformUtil.isWindows()) {
            path = mediaRootPath() + File.separator + folderPath;
        } else {
            path = mediaRootPath() + File.separator + folderPath;
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            folder.mkdir();
        }
        return path + filename;
    }

    public static String mediaRootPath() {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path =
                System.getenv("ProgramData") +
                File.separator +
                "Kazisafe" +
                File.separator +
                "Media";
        } else if (PlatformUtil.isLinux()) {
            path =
                "/home/" + System.getProperty("user.name") + "/Kazisafe/Media";
        } else if (PlatformUtil.isMac()) {
            path =
                "/Users/" + System.getProperty("user.name") + "/Kazisafe/Media";
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdirs();
        }
        return path;
    }

    public static String cPath(String added) {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path =
                System.getenv("ProgramData") +
                File.separator +
                "Kazisafe" +
                File.separator +
                added;
        } else if (PlatformUtil.isLinux()) {
            path =
                "/home/" +
                System.getProperty("user.name") +
                "/Kazisafe/" +
                added;
        } else if (PlatformUtil.isMac()) {
            path =
                "/Users/" +
                System.getProperty("user.name") +
                "/Kazisafe/" +
                added;
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            folder.mkdirs();
        }
        return path;
    }

    public static String rootPath() {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path = System.getenv("ProgramData") + File.separator + "Kazisafe";
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe";
        } else if (PlatformUtil.isMac()) {
            path = "/Users/" + System.getProperty("user.name") + "/Kazisafe";
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdirs();
        }
        return path;
    }

    public static void floatDialog(
        String res,
        int w,
        int h,
        String token,
        Kazisafe ksf,
        Object... objs
    ) {
        FXMLLoader fxmlLoader = new FXMLLoader(
            MainuiController.class.getResource("/guis/" + res),
            Kazisafex.getInstance().getLangageBundle()
        );
        try {
            Parent load = fxmlLoader.load();
            switch (res) {
                case tools.Constants.PRODUCT_DLG -> {
                    ProduitItemController controller =
                        fxmlLoader.<ProduitItemController>getController();
                    controller.setEntreprise((Entreprise) objs[0]);
                    controller.setProduct((Produit) objs[1]);
                }
                case tools.Constants.CLOTURE_DLG -> {
                    ClotureController clot =
                        fxmlLoader.<ClotureController>getController();
                    clot.setEntreprise((Entreprise) objs[0]);
                }
                case tools.Constants.MESURE_DLG -> {
                    MezureController mcontroller =
                        fxmlLoader.<MezureController>getController();
                    mcontroller.setProduct((Produit) objs[1]);
                    mcontroller.setDatabase((Entreprise) objs[0], ksf);
                }
                case tools.Constants.STOCKAGE_DLG -> {
                    Livraison s = (Livraison) objs[0];
                    String action = String.valueOf(objs[1]);
                    Entreprise e = (Entreprise) objs[2];
                    Stocker stock = (Stocker) objs[3];
                    StoreformController pcontroller =
                        fxmlLoader.<StoreformController>getController();
                    pcontroller.setAction(action);
                    pcontroller.setDatabase(e);
                    pcontroller.setStock(stock);
                    pcontroller.setChoosenDelivery(s);
                }
                case tools.Constants.DESTOCKAGE_DLG -> {
                    Destocker dx = (Destocker) objs[0];
                    String actionx = String.valueOf(objs[1]);
                    System.out.println(
                        "Affichange action destockage " + actionx
                    );
                    DestockController xcontroller =
                        fxmlLoader.<DestockController>getController();
                    xcontroller.setDestocker(dx);
                    xcontroller.setDatabase((Entreprise) objs[2]);
                    xcontroller.setAction(actionx);
                }
                case tools.Constants.FICHESTOCK_DLG -> {
                    Produit produit = (Produit) objs[0];
                    FichedestockController fdsc =
                        fxmlLoader.<FichedestockController>getController();
                    String movementSource = objs.length > 2
                        ? String.valueOf(objs[2])
                        : null;
                    fdsc.setDatabase(
                        (Entreprise) objs[1],
                        ksf,
                        produit,
                        movementSource
                    );
                }
                case tools.Constants.RECQ_DLG -> {
                    String actionz = String.valueOf(objs[0]);
                    RecqController reqc =
                        fxmlLoader.<RecqController>getController();

                    if (objs.length > 4) {
                        String paylod = String.valueOf(objs[3]);
                        reqc.setup(
                            (Entreprise) objs[2],
                            actionz,
                            paylod,
                            (Livraison) objs[4]
                        );
                    } else {
                        String paylod = String.valueOf(objs[3]);
                        reqc.setup((Entreprise) objs[2], actionz, paylod, null);
                    }
                    if (objs[1] != null) {
                        reqc.setRecq((Recquisition) objs[1]);
                    }
                }
                case tools.Constants.PANIER_DLG -> {
                    PanierappenderController pc =
                        fxmlLoader.<PanierappenderController>getController();
                    pc.setProduit(
                        (Entreprise) objs[1],
                        ksf,
                        (Produit) objs[0],
                        String.valueOf(objs[2]),
                        Long.parseLong(String.valueOf(objs[3]))
                    );
                }
                case tools.Constants.PAYMENT_DLG -> {
                    PaymentController pyc =
                        fxmlLoader.<PaymentController>getController();
                    pyc.setEntreprise((Entreprise) objs[2]);
                    pyc.setClient(objs[3] == null ? null : (Client) objs[3]);
                    pyc.setLines((List<LigneVente>) objs[0], (Vente) objs[1]);
                }
                case tools.Constants.CLIENT_DLG -> {
                    ClientController clt =
                        fxmlLoader.<ClientController>getController();
                    clt.setUp(
                        (Entreprise) objs[0],
                        token,
                        String.valueOf(objs[1])
                    );
                }
                case tools.Constants.FOURNISSEUR_DLG -> {
                    SuppliersController fsc =
                        fxmlLoader.<SuppliersController>getController();
                    fsc.setDataSource(
                        (Entreprise) objs[0],
                        (Fournisseur) objs[1]
                    );
                }
                case tools.Constants.RELEVEE_DLG -> {
                    ReleveeController rlvc =
                        fxmlLoader.<ReleveeController>getController();
                    rlvc.setup(
                        ksf,
                        (Entreprise) objs[0],
                        (ClientOrganisation) objs[1]
                    );
                }
                case tools.Constants.DELIVERY_DLG -> {
                    DeliveryController dc =
                        fxmlLoader.<DeliveryController>getController();
                    dc.setUp((Entreprise) objs[0], (Livraison) objs[1]);
                    if (objs.length == 3) {
                        dc.setWinCaller(String.valueOf(objs[2]));
                    }
                }
            }
            Scene scene = new Scene(load, w, h);
            Kazisafex.applyTheme(scene);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.setAlwaysOnTop(false);
            load.setOnMousePressed((javafx.scene.input.MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            load.setOnMouseDragged((javafx.scene.input.MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainUI.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
    }

    public static Initializable getLoadedController(
        Initializable init,
        String ress
    ) {
        FXMLLoader fxmlLoader = new FXMLLoader(
            init.getClass().getResource("/guis/" + ress),
            Kazisafex.getInstance().getLangageBundle()
        );
        try {
            fxmlLoader.load();
            return fxmlLoader.getController();
        } catch (IOException ex) {
            Logger.getLogger(MainUI.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
        return null;
    }

    public static AnchorPane getPage(
        Initializable init,
        String ress,
        String token,
        Object... objs
    ) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                init.getClass().getResource("/guis/" + ress),
                Kazisafex.getInstance().getLangageBundle()
            );
            AnchorPane main = fxmlLoader.load();
            switch (ress) {
                case tools.Constants.PRODUITS_VIEW:
                    ProduitsController controller =
                        fxmlLoader.<ProduitsController>getController();
                    controller.setEntreprise((Entreprise) objs[0]);
                    controller.setToken(token);
                    break;
                case tools.Constants.STORAGE_VIEW:
                    GoodstorageController chcontr =
                        fxmlLoader.<GoodstorageController>getController();
                    chcontr.setEntreprise((Entreprise) objs[0]);
                    chcontr.setDatabase("create");
                    break;
                case tools.Constants.POS_VIEW:
                    PosController cclt =
                        fxmlLoader.<PosController>getController();
                    cclt.setEntreprise((Entreprise) objs[0]);
                    cclt.setDatabase();
                    break;
                case tools.Constants.PRODUCTION_VIEW:
                    ProductionController controlp =
                        fxmlLoader.<ProductionController>getController();
                    controlp.initArgs((Entreprise) objs[0]);
                    break;
                case tools.Constants.IMMOBILISATION_VIEW:
                    ImmobilisationController ic =
                        fxmlLoader.<ImmobilisationController>getController();
                    ic.init((Entreprise) objs[0], (Kazisafe) objs[1]);
                    break;
                case Constants.CAISSE_VIEW:
                    TresorerieController tc =
                        fxmlLoader.<TresorerieController>getController();
                    tc.setUp(
                        (Entreprise) objs[0],
                        (Vente) objs[1],
                        (Facture) objs[2]
                    );
                    break;
                case Constants.REPPORT_VIEW:
                    RepportController rpc =
                        fxmlLoader.<RepportController>getController();
                    rpc.setup((Entreprise) objs[0], (Kazisafe) objs[1]);
                    break;
                case Constants.AGENTS_VIEW:
                    AgentController ac =
                        fxmlLoader.<AgentController>getController();
                    ac.init((Entreprise) objs[0], (Kazisafe) objs[1]);
                    break;
                case Constants.ENTREPRISE_VIEW:
                    EntrepriseController ec =
                        fxmlLoader.<EntrepriseController>getController();
                    ec.setup(
                        (Entreprise) objs[0],
                        (Kazisafe) objs[1],
                        (User) objs[2]
                    );
                    break;
                case Constants.PARAMETRE_VIEW:
                    ParametreController pc =
                        fxmlLoader.<ParametreController>getController();
                    pc.init();
                    break;
            }
            Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
            ThemeStyler.apply(
                main,
                pref.getBoolean(Kazisafex.DARK_THEME_PREF, false)
            );
            AnchorPane.setBottomAnchor(main, 28.3);
            AnchorPane.setRightAnchor(main, 28.3);
            AnchorPane.setLeftAnchor(main, 28.3);
            AnchorPane.setTopAnchor(main, 64.0);
            return main;
        } catch (Exception ex) {
            Logger.getLogger(MainUI.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
        return null;
    }

    public static void notify(
        Node graph,
        String title,
        String message,
        long duration,
        String tp
    ) {
        // Toast 100% JavaFX : l'ancien composant Swing (raven.toast) cree des
        // fenetres AWT dans le processus JavaFX/GTK et provoquait des crash
        // natifs (SIGSEGV, sortie 139) sous X11 apres le login.
        if (tp.equalsIgnoreCase("warning")) {
            showToast("warning", message, duration, Pos.CENTER);
        } else if (tp.equalsIgnoreCase("error")) {
            showToast("error", message, duration, Pos.CENTER);
        } else if (tp.equalsIgnoreCase("success")) {
            showToast("success", message, duration, Pos.CENTER);
        } else {
            showToast("info", message, duration, Pos.CENTER);
        }
    }

    public static void notifyConnect(
        Node graph,
        String title,
        String message,
        double duration
    ) {
        showToast("info", message, 4, Pos.CENTER);
    }

    private static void showToast(
        String type,
        String message,
        double durationSeconds,
        Pos position
    ) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(
                () -> showToast(type, message, durationSeconds, position)
            );
            return;
        }
        try {
            if (System.getProperty("kazisafe.toast.disabled", "false").equalsIgnoreCase("true")) {
                return;
            }
            Label label = new Label(message);
            label.setWrapText(true);
            label.setTextFill(Color.WHITE);
            label.setFont(new Font("System", 13));
            label.setPadding(new Insets(12, 18, 12, 18));
            label.setMaxWidth(380);
            String bg;
            if ("error".equalsIgnoreCase(type)) {
                bg = "#e74c3c";
            } else if ("warning".equalsIgnoreCase(type)) {
                bg = "#f39c12";
            } else if ("success".equalsIgnoreCase(type)) {
                bg = "#2ecc71";
            } else {
                bg = "#44cef5";
            }
            label.setStyle(
                "-fx-background-color: " +
                bg +
                ";-fx-background-radius: 10;" +
                "-fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0.2, 0, 6);"
            );
            StackPane root = new StackPane(label);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            root.applyCss();
            root.autosize();
            double w = Math.max(root.getWidth(), 1);
            double h = Math.max(root.getHeight(), 1);
            Rectangle2D sb = Screen.getPrimary().getVisualBounds();
            double offset = 0;
            for (Stage s : activeToasts) {
                if (s.isShowing()) {
                    offset += h + 10;
                }
            }
            double x, y;
            if (position == Pos.BOTTOM_RIGHT) {
                x = sb.getMaxX() - w - 20;
                y = sb.getMaxY() - h - 20 - offset;
            } else {
                x = sb.getMinX() + (sb.getWidth() - w) / 2.0;
                y = sb.getMinY() + (sb.getHeight() - h) / 2.0 - offset;
            }
            stage.setX(x);
            stage.setY(y);
            activeToasts.add(stage);
            stage.show();
            stage.setX(x);
            stage.setY(y);
            PauseTransition wait = new PauseTransition(
                Duration.seconds(Math.max(1, durationSeconds))
            );
            wait.setOnFinished(e -> {
                FadeTransition fade = new FadeTransition(
                    Duration.millis(400),
                    root
                );
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(f -> {
                    activeToasts.remove(stage);
                    stage.close();
                });
                fade.play();
            });
            wait.play();
        } catch (Exception ex) {
            Logger.getLogger(MainUI.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
    }

    public static <T> void initAutoChooser(
        ComboBox<T> elements,
        ObservableList<T> instances
    ) {
        elements.setConverter(
            new StringConverter<T>() {
                @Override
                public String toString(T obj) {
                    switch (obj) {
                        case null -> {
                            return null;
                        }
                        case Produit object -> {
                            return object == null
                                ? null
                                : object.getNomProduit() +
                                      " " +
                                      (object.getMarque() == null
                                          ? ""
                                          : object.getMarque()) +
                                      " " +
                                      (object.getModele() == null
                                          ? ""
                                          : object.getModele()) +
                                      " " +
                                      (object.getTaille() == null
                                          ? ""
                                          : object.getTaille()) +
                                      " " +
                                      (object.getCouleur() == null
                                          ? ""
                                          : object.getCouleur()) +
                                      " " +
                                      object.getCodebar();
                        }
                        case Mesure object -> {
                            return object == null
                                ? null
                                : object.getDescription();
                        }
                        case Client object -> {
                            return object == null
                                ? null
                                : object.getNomClient() +
                                      " " +
                                      object.getAdresse() +
                                      " " +
                                      object.getPhone();
                        }
                        case Fournisseur object -> {
                            return object == null
                                ? null
                                : object.getNomFourn() +
                                      " " +
                                      object.getAdresse() +
                                      " " +
                                      object.getPhone();
                        }
                        case Inventaire object -> {
                            return object == null
                                ? null
                                : object.getCodeInventaire() +
                                      " " +
                                      object.getEtat() +
                                      " " +
                                      object.getDateDebut().toString() +
                                      " " +
                                      object.getRegion();
                        }
                        default -> {
                            return null;
                        }
                    }
                }

                @Override
                public T fromString(String string) {
                    return elements
                        .getItems()
                        .stream()
                        .filter(objx -> {
                            switch (objx) {
                                case Produit object -> {
                                    return (
                                        object.getNomProduit() +
                                        " " +
                                        (object.getMarque() == null
                                            ? ""
                                            : object.getMarque()) +
                                        " " +
                                        (object.getModele() == null
                                            ? ""
                                            : object.getModele()) +
                                        " " +
                                        (object.getTaille() == null
                                            ? ""
                                            : object.getTaille()) +
                                        " " +
                                        (object.getCouleur() == null
                                            ? ""
                                            : object.getCouleur()) +
                                        " " +
                                        object.getCodebar()
                                    ).equalsIgnoreCase(string);
                                }
                                case Mesure object -> {
                                    return object
                                        .getDescription()
                                        .equalsIgnoreCase(string);
                                }
                                case Inventaire object -> {
                                    String value =
                                        object.getCodeInventaire() +
                                        " " +
                                        object.getEtat() +
                                        " " +
                                        object.getDateDebut().toString() +
                                        " " +
                                        object.getRegion();
                                    return value.equalsIgnoreCase(string);
                                }
                                case Client object -> {
                                    String value =
                                        object.getNomClient() +
                                        " " +
                                        object.getAdresse() +
                                        " " +
                                        object.getPhone();
                                    return value.equalsIgnoreCase(string);
                                }
                                case Fournisseur object -> {
                                    String value =
                                        object.getNomFourn() +
                                        " " +
                                        object.getAdresse() +
                                        " " +
                                        object.getPhone();
                                    return value.equalsIgnoreCase(string);
                                }
                                default -> {
                                    return false;
                                }
                            }
                        })
                        .findFirst()
                        .orElse(null);
                }
            }
        );
        elements.setItems(instances);
        ComboBoxAutoCompletion<T> comx = new ComboBoxAutoCompletion<>(elements);
    }

    public static <T> void initChooser(
        ComboBox<T> elements,
        ObservableList<T> instances
    ) {
        elements.setConverter(
            new StringConverter<T>() {
                @Override
                public String toString(T obj) {
                    switch (obj) {
                        case null -> {
                            return null;
                        }
                        case Produit object -> {
                            return object == null
                                ? null
                                : object.getNomProduit() +
                                      " " +
                                      (object.getMarque() == null
                                          ? ""
                                          : object.getMarque()) +
                                      " " +
                                      (object.getModele() == null
                                          ? ""
                                          : object.getModele()) +
                                      " " +
                                      (object.getTaille() == null
                                          ? ""
                                          : object.getTaille()) +
                                      " " +
                                      (object.getCouleur() == null
                                          ? ""
                                          : object.getCouleur()) +
                                      " " +
                                      object.getCodebar();
                        }
                        case Mesure object -> {
                            return object == null
                                ? null
                                : object.getDescription();
                        }
                        case Client object -> {
                            return object == null
                                ? null
                                : object.getNomClient() +
                                      " " +
                                      object.getAdresse() +
                                      " " +
                                      object.getPhone();
                        }
                        case Fournisseur object -> {
                            return object == null
                                ? null
                                : object.getNomFourn() +
                                      " " +
                                      object.getAdresse() +
                                      " " +
                                      object.getPhone();
                        }
                        case Inventaire object -> {
                            return object == null
                                ? null
                                : object.getCodeInventaire() +
                                      " " +
                                      object.getEtat() +
                                      " " +
                                      object.getDateDebut().toString() +
                                      " " +
                                      object.getRegion();
                        }
                        default -> {
                            return null;
                        }
                    }
                }

                @Override
                public T fromString(String string) {
                    return elements
                        .getItems()
                        .stream()
                        .filter(objx -> {
                            switch (objx) {
                                case Produit object -> {
                                    return (
                                        object.getNomProduit() +
                                        " " +
                                        (object.getMarque() == null
                                            ? ""
                                            : object.getMarque()) +
                                        " " +
                                        (object.getModele() == null
                                            ? ""
                                            : object.getModele()) +
                                        " " +
                                        (object.getTaille() == null
                                            ? ""
                                            : object.getTaille()) +
                                        " " +
                                        (object.getCouleur() == null
                                            ? ""
                                            : object.getCouleur()) +
                                        " " +
                                        object.getCodebar()
                                    ).equalsIgnoreCase(string);
                                }
                                case Mesure object -> {
                                    return object
                                        .getDescription()
                                        .equalsIgnoreCase(string);
                                }
                                case Inventaire object -> {
                                    String value =
                                        object.getCodeInventaire() +
                                        " " +
                                        object.getEtat() +
                                        " " +
                                        object.getDateDebut().toString() +
                                        " " +
                                        object.getRegion();
                                    return value.equalsIgnoreCase(string);
                                }
                                case Client object -> {
                                    String value =
                                        object.getNomClient() +
                                        " " +
                                        object.getAdresse() +
                                        " " +
                                        object.getPhone();
                                    return value.equalsIgnoreCase(string);
                                }
                                case Fournisseur object -> {
                                    String value =
                                        object.getNomFourn() +
                                        " " +
                                        object.getAdresse() +
                                        " " +
                                        object.getPhone();
                                    return value.equalsIgnoreCase(string);
                                }
                                default -> {
                                    return false;
                                }
                            }
                        })
                        .findFirst()
                        .orElse(null);
                }
            }
        );
        elements.setItems(instances);
    }
}
