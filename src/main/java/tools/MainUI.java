/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.endeleya.kazisafex.MainuiController;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
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
import data.network.Kazisafe;
import com.endeleya.kazisafex.AgentController;
import com.endeleya.kazisafex.ClientController;
import com.endeleya.kazisafex.DeliveryController;
import com.endeleya.kazisafex.DestockController;
import com.endeleya.kazisafex.EntrepriseController;
import com.endeleya.kazisafex.FichedestockController;
import com.endeleya.kazisafex.GoodstorageController;
import com.endeleya.kazisafex.Kazisafex;
import com.endeleya.kazisafex.MezureController;
import com.endeleya.kazisafex.PanierappenderController;
import com.endeleya.kazisafex.ParametreController;
import com.endeleya.kazisafex.PaymentController;
import com.endeleya.kazisafex.PosController;
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
import data.LigneVente;
import data.Livraison;
import data.LoginResult;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.User;
import data.Vente;
import raven.toast.Notifications;
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
    public static Stage mainStage;

    public static void loadMainView(Class theClass, String fxmlPath, double h, double w, String phone, String token, String rccm, LoginResult loginr) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource("/guis/" + fxmlPath), Kazisafex.getInstance().getLangageBundle());
            Parent main = fxmlLoader.load();
            MainuiController controller = fxmlLoader.<MainuiController>getController();
//           controller.setToken(token); 
            controller.setLoginResult(token, rccm, loginr);
            controller.setUserPhone(phone);
            mainStage = new Stage();
            Scene scene = new Scene(main, w, h);
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
            mainStage.getIcons().add(new Image(Kazisafex.class.getResourceAsStream("/icons/icone_ksf.png")));
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
        } catch (IOException ex) {
            Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void minimize() {
        if (mainStage == null) {
            return;
        }
        mainStage.setIconified(true);
    }
    static boolean ismax = false;
    static double ancienh = 0, ancienw = 0;

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
//            mainStage.setMaximized(true);
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

    public static void notifySync(String title, String message, String tooltip) {
        try {
            if (!SystemTray.isSupported()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = Toolkit.getDefaultToolkit().createImage(MainUI.class.getResource("/icons/icone_ksf.png"));
            TrayIcon trayIcon = new TrayIcon(image, "Kazisafe");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip(tooltip);
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, MessageType.INFO);
            new AudioClip(MainUI.class.getResource("/icons/notify_sound.mp3").toExternalForm()).play();
            // Error:
            // trayIcon.displayMessage("Hello, World", "Java Notification Demo", MessageType.ERROR);
            // Warning:
            // trayIcon.displayMessage("Hello, World", "Java Notification Demo", MessageType.WARNING);
            tray.remove(trayIcon);

        } catch (Exception ex) {
            System.err.print(ex);
        }
    }

    public static void removeShaddowEffect(Node node) {
        if (Platform.isSupported(ConditionalFeature.EFFECT)) {
            node.setEffect(null);
        }
    }

    public static void setPattern(DatePicker dtpk) {
        dtpk.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        });
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

                if (r >= TOLERANCE_THRESHOLD
                        && g >= TOLERANCE_THRESHOLD
                        && b >= TOLERANCE_THRESHOLD) {
                    argb &= 0x00FFFFFF;
                }

                writer.setArgb(x, y, argb);
            }
        }

        return outputImage;
    }

    public static String createFileWithPath(String folderPath, String filename) {
        String path;
        if (PlatformUtil.isWindows()) {
            path = mediaRootPath() + "/" + folderPath;
        } else {
            path = mediaRootPath() + "/" + folderPath;
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdir();
        }
        return path + filename;
    }

    public static String mediaRootPath() {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path = "C:" + File.separator + "Kazisafe" + File.separator + "Media";
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe/Media";
        } else if (PlatformUtil.isMac()) {
            path = "/Users/" + System.getProperty("user.name") + "/Kazisafe/Media";
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdir();
        }
        return path;
    }

    public static String cPath(String added) {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path = "C:" + File.separator + "Kazisafe" + File.separator + added;
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe/" + added;
        } else if (PlatformUtil.isMac()) {
            path = "/Users/" + System.getProperty("user.name") + "/Kazisafe/" + added;
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdir();
        }
        return path;
    }

    public static String rootPath() {
        String path = null;
        if (PlatformUtil.isWindows()) {
            path = "C:" + File.separator + "Kazisafe";
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe";
        } else if (PlatformUtil.isMac()) {
            path = "/Users/" + System.getProperty("user.name") + "/Kazisafe";
        }
        File folder = new File(path);
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdir();
        }
        return path;
    }

//
    public static void floatDialog(String res, int w, int h, String token, Kazisafe ksf, Object... objs) {
        FXMLLoader fxmlLoader = new FXMLLoader(MainuiController.class.getResource("/guis/" + res), Kazisafex.getInstance().getLangageBundle());
        try {
            Parent load = fxmlLoader.load();
            switch (res) {
                case tools.Constants.PRODUCT_DLG:
                    ProduitItemController controller = fxmlLoader.<ProduitItemController>getController();
                    controller.setEntreprise((Entreprise) objs[0]);
                    controller.setProduct((Produit) objs[1]);

                    break;
                case tools.Constants.MESURE_DLG:
                    MezureController mcontroller = fxmlLoader.<MezureController>getController();
                    mcontroller.setProduct((Produit) objs[1]);
                    mcontroller.setDatabase((Entreprise) objs[0], ksf);
                    break;
                case tools.Constants.STOCKAGE_DLG:
                    Livraison s = (Livraison) objs[0];
                    String action = String.valueOf(objs[1]);
                    Entreprise e = ((Entreprise) objs[2]);
                    Stocker stock = ((Stocker) objs[3]);
                    StoreformController pcontroller = fxmlLoader.<StoreformController>getController();
                    pcontroller.setAction(action);
                    pcontroller.setDatabase(e);
                    pcontroller.setStock(stock);

                    pcontroller.setChoosenDelivery(s);
                    break;
                case tools.Constants.DESTOCKAGE_DLG:
                    Destocker dx = (Destocker) objs[0];
                    String actionx = String.valueOf(objs[1]);
                    System.out.println("Affichange action destockage " + actionx);
                    DestockController xcontroller = fxmlLoader.<DestockController>getController();
                    xcontroller.setDestocker(dx);
                    xcontroller.setDatabase((Entreprise) objs[2]);
                    xcontroller.setAction(actionx);

                    break;
                case tools.Constants.FICHESTOCK_DLG:
                    Produit produit = (Produit) objs[0];
                    FichedestockController fdsc = fxmlLoader.<FichedestockController>getController();
                    fdsc.setDatabase((Entreprise) objs[1], ksf, produit);
                    break;
                case tools.Constants.RECQ_DLG:
                    String actionz = String.valueOf(objs[0]);
                    RecqController reqc = fxmlLoader.<RecqController>getController();
                    reqc.setup((Entreprise) objs[2], actionz);
                    reqc.setRecq((Recquisition) objs[1]);
                    break;
                case tools.Constants.PANIER_DLG:
                    PanierappenderController pc = fxmlLoader.<PanierappenderController>getController();
                    pc.setProduit((Entreprise) objs[1], ksf, ((Produit) objs[0]), String.valueOf(objs[2]), Long.valueOf(String.valueOf(objs[3])));
                    break;
                case tools.Constants.PAYMENT_DLG:
                    PaymentController pyc = fxmlLoader.<PaymentController>getController();
                    pyc.setEntreprise((Entreprise) objs[2]);
                    pyc.setClient(objs[3] == null ? null : (Client) objs[3]);
                    pyc.setLines(((List<LigneVente>) objs[0]), ((Vente) objs[1]));
                    break;
                case tools.Constants.CLIENT_DLG:
                    ClientController clt = fxmlLoader.<ClientController>getController();
                    clt.setUp((Entreprise) objs[0], token, String.valueOf(objs[1]));
                    break;
                case tools.Constants.FOURNISSEUR_DLG:
                    SuppliersController fsc = fxmlLoader.<SuppliersController>getController();
                    fsc.setDataSource((Entreprise) objs[0], (Fournisseur) objs[1]);
                    break;
                case tools.Constants.RELEVEE_DLG:
                    ReleveeController rlvc = fxmlLoader.<ReleveeController>getController();
                    rlvc.setup(ksf, (Entreprise) objs[0], (ClientOrganisation) objs[1]);
                    break;
                case tools.Constants.DELIVERY_DLG:
                    DeliveryController dc = fxmlLoader.<DeliveryController>getController();
                    dc.setUp((Entreprise) objs[0], (Livraison) objs[1]);
                    break;

            }
            Scene scene = new Scene(load, w, h);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
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
            Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Initializable getLoadedController(Initializable init, String ress) {
        FXMLLoader fxmlLoader = new FXMLLoader(init.getClass().getResource("/guis/" + ress), Kazisafex.getInstance().getLangageBundle());
        try {
            fxmlLoader.load();
            return fxmlLoader.getController();
        } catch (IOException ex) {
            Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static AnchorPane getPage(Initializable init, String ress, String token, Object... objs) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(init.getClass().getResource("/guis/" + ress), Kazisafex.getInstance().getLangageBundle());
            AnchorPane main = fxmlLoader.load();
            switch (ress) {
                case tools.Constants.PRODUITS_VIEW:
                    ProduitsController controller = fxmlLoader.<ProduitsController>getController();
                    controller.setEntreprise(((Entreprise) objs[0]));
                    controller.setToken(token);
                    break;
                case tools.Constants.STORAGE_VIEW:
                    GoodstorageController chcontr = fxmlLoader.<GoodstorageController>getController();
                    chcontr.setEntreprise((Entreprise) objs[0]);
                    chcontr.setDatabase("create");
                    break;
                case tools.Constants.POS_VIEW:
                    PosController cclt = fxmlLoader.<PosController>getController();
                    cclt.setEntreprise((Entreprise) objs[0]);
                    cclt.setDatabase();

                    break;
                case Constants.CAISSE_VIEW:
                    TresorerieController tc = fxmlLoader.<TresorerieController>getController();
                    tc.setUp((Entreprise) objs[0], (Vente) objs[1], (Facture) objs[2]);
                    break;
                case Constants.REPPORT_VIEW:
                    RepportController rpc = fxmlLoader.<RepportController>getController();
                    rpc.setup((Entreprise) objs[0], (Kazisafe) objs[1]);
                    break;
                case Constants.AGENTS_VIEW:
                    AgentController ac = fxmlLoader.<AgentController>getController();
                    ac.init((Entreprise) objs[0], (Kazisafe) objs[1]);
                    break;
                case Constants.ENTREPRISE_VIEW:
                    EntrepriseController ec = fxmlLoader.<EntrepriseController>getController();
                    ec.setup((Entreprise) objs[0], (Kazisafe) objs[1], (User) objs[2]);
                    break;
                case Constants.PARAMETRE_VIEW:
                    ParametreController pc = fxmlLoader.<ParametreController>getController();
                    pc.init();
                    break;
            }
            AnchorPane.setBottomAnchor(main, 28.3);
            AnchorPane.setRightAnchor(main, 28.3);
            AnchorPane.setLeftAnchor(main, 28.3);
            AnchorPane.setTopAnchor(main, 64.0);
            return main;
        } catch (Exception ex) {
            Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void notify(Node graph, String title, String message, long duration, String tp) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Notifications instance = Notifications.getInstance();
                long dur = duration * 1000;
                if (tp.equalsIgnoreCase("warning")) {
                    instance.show(Notifications.Type.WARNING, Notifications.Location.BOTTOM_RIGHT, dur, message);
                } else if (tp.equalsIgnoreCase("error")) {
                    instance.show(Notifications.Type.ERROR, Notifications.Location.BOTTOM_RIGHT, dur, message);
                } else {
                    instance.show(Notifications.Type.SUCCESS, Notifications.Location.BOTTOM_RIGHT, dur, message);
                }
            }
        });

    }

    public static void notifyConnect(Node graph, String title, String message, double duration) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, message);

//                Notifications n = Notifications.create()
//                        .title(title)
//                        .text(message)
//                        .position(Pos.BOTTOM_RIGHT)
//                        .hideAfter(Duration.seconds(duration));
//               
//                if (graph == null) {
//                        n.showInformation();
//                } else {
//                    n.graphic(graph);
//                    n.show();
//                }
            }
        });

    }

}
