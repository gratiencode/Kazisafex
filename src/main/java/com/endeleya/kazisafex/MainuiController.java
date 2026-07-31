/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import static tools.Constants.CAISSES;
import static tools.Constants.PARAMETRES;
import static tools.Constants.POS;
import static tools.Constants.REPPORTS;
import static tools.Constants.STORAGE;

import com.endeleya.ia.AiAgents;
import com.endeleya.ia.ChatHtmlTemplate;
import com.endeleya.ia.GratienAssistantClient;
import com.endeleya.ia.GratienTools;
import com.endeleya.ia.IaResponseHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import data.Abonnement;
import data.BaseModel;
import data.Entreprise;
import data.Facture;
import data.LigneVente;
import data.Module;
import data.Operation;
import data.Permission;
import data.Refresher;
import data.User;
import data.Vente;
import data.core.KazisafeServiceFactory;
import data.helpers.LoginWebResult;
import data.helpers.Role;
import data.helpers.Token;
import data.network.Kazisafe;
import delegates.LigneVenteDelegate;
import delegates.OperationDelegate;
import delegates.PermissionDelegate;
import delegates.RepportDelegate;
import delegates.VenteDelegate;
import jakarta.persistence.EntityExistsException;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import netscape.javascript.JSObject;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.PlatformUtil;
import tools.CurrencyConverter;
import tools.Agregator;
import tools.Constants;
import tools.Droit;
import tools.FileUtils;
import tools.LocalTaskStateListener;
import tools.MainUI;
import tools.Metric;
import tools.NetLoockup;
import tools.PriceMaker;
import tools.SubscriptionUtil;
import tools.SyncEngine;
import tools.Tables;
import tools.TopTen;
import tools.Util;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class MainuiController implements Initializable {

    Flow.Subscriber<Set<BaseModel>> saver = new Flow.Subscriber<>() {
        private Flow.Subscription abonnement;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            System.out.println("Final souscriber onSubc");
            this.abonnement = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Set<BaseModel> items) {
            System.out.println("fin fonction request now");
            abonnement.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Teminaison de la conso");
        }
    };

    EventSource evs;

    String initialHtml = ChatHtmlTemplate.content();
    private final GratienAssistantClient gratienAssistantClient =
        GratienAssistantClient.getInstance();
    private final List<File> aiAttachments = new ArrayList<>();
    private final Set<String> handledSecureMysqlRequests =
        ConcurrentHashMap.newKeySet();
    // File d'attente des messages envoyés pendant que Gratien est en cours de traitement.
    private final LinkedBlockingQueue<PendingUserMessage> gratienMessageQueue =
        new LinkedBlockingQueue<>();
    // Vrai si Gratien est en cours de raisonnement ou de génération de réponse.
    private final AtomicBoolean gratienIsProcessing = new AtomicBoolean(false);

    public static MainuiController getInstance() {
        if (instance == null) {
            instance = new MainuiController();
        }
        return instance;
    }

    public static MainuiController peekInstance() {
        return instance;
    }

    /**
     * @deprecated Use {@link tools.DataCache} instead.
     */
    @Deprecated
    public static ConcurrentHashMap<String, Object> dataCache;
    private static final double LAYOUTY = 59.0;
    private static final double LAYOUTX = 30.0;

    @FXML
    private Pane update_pane;

    @FXML
    private ImageView img_profile, app_image;

    @FXML
    private ImageView img_company_logo;

    @FXML
    private TextField searchField;

    @FXML
    private Label user_connected;

    @FXML
    private ImageView home;

    @FXML
    private ImageView stockage;

    @FXML
    private ImageView pos;

    @FXML
    private ImageView agents;

    @FXML
    private ImageView rapport;

    @FXML
    private ImageView compagnie;

    @FXML
    private ImageView products_gate;

    @FXML
    private ImageView immobilisation;

    @FXML
    private ImageView caisse, img_iconify, img_close, agrandir;

    @FXML
    private ImageView image_title, download_imgbtn;

    @FXML
    private ImageView parametre, aide;

    @FXML
    private Label pane_title, label_version, label_status;

    @FXML
    private AnchorPane mainpane;

    @FXML
    private AnchorPane showPane;

    /**
     * Replaced by {@link tools.DataCache}.  The old {@code pageCache} that
     * kept entire AnchorPane FXML trees alive has been removed: views are now
     * rebuilt fresh every time (solving VirtualFlow rendering issues) while
     * only the data lists are cached in {@link tools.DataCache}.
     */

    @FXML
    private AreaChart<String, Number> venteChart;

    @FXML
    private Label svente;

    @FXML
    private Label stresor;

    @FXML
    private PieChart piepane;

    @FXML
    private Label sdepense;

    @FXML
    private Label screance;

    @FXML
    private Label entrep_name;

    @FXML
    private Label depense_proportion;

    @FXML
    Label txt_region, appName;

    @FXML
    Hyperlink install_update_link;

    @FXML
    private ProgressBar download_update_pgb;

    @FXML
    private ProgressIndicator douwnload_update_pgi;

    ResourceBundle bundle;

    String token;
    private String phone;
    private String region, role, rccm;
    private String entrepiseId;
    private Module newModule;
    Preferences pref;
    double taux;
    int itemPerPage = 15;
    String localPath;
    private volatile String downloadedUpdateFilePath;
    Kazisafe kazisafe;
    User user;
    Entreprise entreprisex;
    PriceMaker maker;

    private String CURRENT_VIEW = tools.Constants.MAIN;
    boolean isConnected = false;

    private static MainuiController instance;
    private static final int BATCH_SIZE = 10;
    private Set<BaseModel> buffer;
    Agregator ag;
    NetLoockup network;
    private ImageView activeMenuIcon;

    @FXML
    private Label sync_txt_message;

    @FXML
    private ProgressBar sync_pg_bar;

    @FXML
    private ImageView production;

    @FXML
    private ToolBar tbar_menu;

    @FXML
    private Button btn_theme_toggle;

    @FXML
    public Label txt_states_features;

    @FXML
    private SplitPane main_spliter;

    @FXML
    private AnchorPane ai_panel;

    @FXML
    private WebView ia_webvu_chat;

    WebEngine webE;
    IaResponseHandler responseHandler;
    boolean isNofication = false;

    @FXML
    private TextArea txt_input_iaquery;

    @FXML
    private MenuButton btnmenu_choose_aimedia;

    @FXML
    private HBox box_ai_attachment_previews;

    @FXML
    private Label txt_selected_attachment;

    private ChatBridge chatBridge;

    public Label getSync_txt_message() {
        return sync_txt_message;
    }

    public void setBackgroundSyncStatus(String message) {
        String value = message == null ? "" : message;
        if (label_status != null && !label_status.textProperty().isBound()) {
            label_status.setText(value);
        }
        if (
            sync_txt_message != null &&
            !sync_txt_message.textProperty().isBound()
        ) {
            sync_txt_message.setText(value);
        }
    }

    public MainuiController() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        instance = this;
        buffer = new HashSet<>();
        dataCache = new ConcurrentHashMap<>();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        new Thread(() -> {
            new services.RecquisitionService().backfillNullLotAggregates();
        }).start();
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        appName.setText("Kazisafe");
        role = pref.get("priv", "Non disponible");
        installTooltips();
        Tooltip.install(img_profile, new Tooltip(role));
        Tooltip.install(
            img_iconify,
            new Tooltip(bundle.getString("xtooltip.redu_ire"))
        );
        Tooltip.install(
            agrandir,
            new Tooltip(bundle.getString("xtooltip.agran_dir"))
        );
        Tooltip.install(
            img_close,
            new Tooltip(bundle.getString("xtooltip.fer_me"))
        );
        Tooltip.install(
            download_imgbtn,
            new Tooltip("Télécharger les mises à jours")
        );
        Tooltip.install(aide, new Tooltip("Ouvrir le fichier d'aide"));
        Tooltip.install(
            app_image,
            new Tooltip(
                "© " +
                    Year.now() +
                    " Endeleya Corp. Kazisafe v" +
                    pref.get("ksf_version", tools.Constants.APP_VERSION)
            )
        );
        douwnload_update_pgi.setVisible(false);
        download_update_pgb.setVisible(false);
        install_update_link.setVisible(false);
        localPath = MainUI.cPath("/Media/Update");
        MainUI.cPath(File.separator + "datastore");
        MainUI.cPath("/Media/ia/gratien");
        MainUI.cPath("/Media/proc/logs");
        pref.put("ksf_version", tools.Constants.APP_VERSION);
        taux = CurrencyConverter.activeRate();
        network = new NetLoockup();
        network.setOnNetworkStateChangeListener((boolean isOnline) -> {
            SyncEngine.getInstance().onNetworkStateChanged(isOnline);
        });
        maker = new PriceMaker();
        maker.refreshFromPreferences();
        initGratienAgent();
        Platform.runLater(this::checkForUpdates);
        txt_states_features.setVisible(true);
        txt_states_features.setText("...");
        btn_theme_toggle.setText(
            pref.getBoolean(Kazisafex.DARK_THEME_PREF, false)
                ? bundle.getString("xbtn.theme.light")
                : bundle.getString("xbtn.theme.dark")
        );
        Platform.runLater(this::refreshThemeView);
        Platform.runLater(() -> setActiveMenu(home));
        searchField
            .focusedProperty()
            .addListener(
                (
                    ObservableValue<? extends Boolean> ov,
                    Boolean t,
                    Boolean t1
                ) -> {
                    if (t1) {
                        if (searchField.getText().isEmpty()) {
                            searchField.selectAll();
                        }
                    }
                }
            );

        cloturer(
            LocalDate.now(),
            LocalDate.now(),
            "Journalier du " + LocalDate.now().toString()
        );
        // sync();
        initializeAi();
        main_spliter.getItems().removeAll(ai_panel);
        main_spliter.setDividerPosition(0, 1);
    }

    public void initializeAi() {
        webE = ia_webvu_chat.getEngine();
        installChatBridge();
        installChatReplyFallback();
        webE.loadContent(initialHtml);
        txt_input_iaquery.addEventFilter(
            KeyEvent.KEY_PRESSED,
            this::handleAiInputKey
        );
        btnmenu_choose_aimedia.getItems().clear();
        responseHandler = new IaResponseHandler();
        responseHandler.setOnAiMessageListener(
            (String message, String name) -> {
                System.out.println("msg : " + message);
                Platform.runLater(() -> {
                    if (name.equals("[on-going]")) {
                        webE.executeScript(
                            "appendBotPartial(" + escapeForJS(message) + ")"
                        );
                    } else if (name.equals("[end]")) {
                        webE.executeScript(
                            "appendBotPartial(" + escapeForJS(message) + ")"
                        );
                        webE.executeScript("endBotMessage()");
                        evs.close();
                    } else if (name.equals("[notification]")) {
                        if (!isNofication) {
                            webE.executeScript(
                                "appendBotPartial(" + escapeForJS(message) + ")"
                            );
                            webE.executeScript("endBotMessage()");
                            isNofication = true;
                            evs.close();
                        }
                    }
                });
            }
        );
        MenuItem imagemenu = new MenuItem("Joindre une image");
        MenuItem fichiermenu = new MenuItem("Joindre un fichier");
        btnmenu_choose_aimedia.getItems().add(imagemenu);
        btnmenu_choose_aimedia.getItems().add(fichiermenu);
        imagemenu.setOnAction((ActionEvent event) -> {
            addAttachemnts();
        });
        fichiermenu.setOnAction((ActionEvent event) -> {
            addAttachemnts();
        });
    }

    private void installChatBridge() {
        chatBridge = new ChatBridge();
        webE.getLoadWorker()
            .stateProperty()
            .addListener((observable, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    exposeChatBridge();
                }
            });
        if (webE.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            exposeChatBridge();
        }
    }

    private void exposeChatBridge() {
        try {
            JSObject window = (JSObject) webE.executeScript("window");
            window.setMember("kazisafeChat", chatBridge);
        } catch (Exception ex) {
            Logger.getLogger(MainuiController.class.getName()).log(
                Level.FINE,
                "Chat bridge not ready",
                ex
            );
        }
    }

    private void installChatReplyFallback() {
        webE.titleProperty().addListener((observable, oldTitle, newTitle) ->
            handleChatReplyPayload(newTitle)
        );
        webE.setOnAlert((WebEvent<String> event) -> {
            handleChatReplyPayload(event.getData());
        });
    }

    private void handleChatReplyPayload(String data) {
        if (data == null) {
            return;
        }
        String replyPrefix = "__KAZISAFE_REPLY__";
        String copyPrefix = "__KAZISAFE_COPY__";
        if (data.startsWith(replyPrefix)) {
            attachGratienReplyToInput(
                decodeChatActionPayload(data.substring(replyPrefix.length()))
            );
        } else if (data.startsWith(copyPrefix)) {
            copyTextToClipboard(
                decodeChatActionPayload(data.substring(copyPrefix.length()))
            );
        }
    }

    private String decodeChatActionPayload(String payload) {
        int nonceIndex = payload.lastIndexOf('|');
        if (nonceIndex >= 0) {
            payload = payload.substring(0, nonceIndex);
        }
        return java.net.URLDecoder.decode(payload, StandardCharsets.UTF_8);
    }

    private byte[] processAndCompress(File nonCompressed, long maxsize)
        throws IOException {
        BufferedImage originalImg = ImageIO.read(nonCompressed);
        if (originalImg == null) {
            throw new IOException(
                "Le format de fichier non supporter ou fichier introuvable"
            );
        }

        if (
            originalImg.getType() == BufferedImage.TYPE_INT_ARGB ||
            originalImg.getType() == BufferedImage.TYPE_4BYTE_ABGR
        ) {
            BufferedImage rgbImage = new BufferedImage(
                originalImg.getWidth(),
                originalImg.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(originalImg, 0, 0, null);
            g.dispose();
            originalImg = rgbImage;
        }

        byte[] compressed;
        float quality = 0.85f;
        double scale = 1.0;
        do {
            BufferedImage image2Comp = originalImg;
            if (scale < 1.0) {
                int newW = (int) (originalImg.getWidth() * scale);
                int newH = (int) (originalImg.getHeight() * scale);
                image2Comp = resizeImage(originalImg, newW, newH);
            }
            //la compression jpeg en ram tampon
            compressed = compressToByte(image2Comp, quality);
            //si resultat est toujours sup a 700Ko
            if (compressed.length > maxsize) {
                if (quality > 0.50f) {
                    quality -= 0.10f;
                } else {
                    scale -= 0.15;
                    quality = 0.70f;
                }
            }
        } while (compressed.length > maxsize && scale > 0.15);
        return compressed;
    }

    private BufferedImage resizeImage(BufferedImage aCompres, int w, int h) {
        BufferedImage resized = new BufferedImage(
            w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g2d.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        );
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2d.drawImage(aCompres, 0, 0, w, h, null);
        g2d.dispose();
        return resized;
    }

    private byte[] compressToByte(BufferedImage bi, float qual)
        throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(
            "jpg"
        );
        if (!writers.hasNext()) {
            throw new IOException("Scribe JPEG introuvable");
        }
        ImageWriter imgWr = writers.next();
        ImageWriteParam param = imgWr.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(qual);
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos)
        ) {
            imgWr.setOutput(ios);
            imgWr.write(null, new IIOImage(bi, null, null), param);
            imgWr.dispose();
            return baos.toByteArray();
        }
    }

    public class ChatBridge {

        public void replyToGratien(String message) {
            Platform.runLater(() -> attachGratienReplyToInput(message));
        }

        public void copyGratienMessage(String message) {
            Platform.runLater(() -> copyTextToClipboard(message));
        }
    }

    private void copyTextToClipboard(String message) {
        ClipboardContent content = new ClipboardContent();
        content.putString(message == null ? "" : message);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void attachGratienReplyToInput(String message) {
        String cleaned = cleanQuotedGratienMessage(message);
        if (cleaned.isBlank()) {
            return;
        }
        String quote =
            "Message de Gratien auquel je réponds:\n\"\"\"\n" +
            cleaned +
            "\n\"\"\"\n\n";
        String current =
            txt_input_iaquery.getText() == null
                ? ""
                : txt_input_iaquery.getText();
        if (current.isBlank()) {
            txt_input_iaquery.setText(quote);
        } else if (!current.contains(quote)) {
            txt_input_iaquery.setText(quote + current);
        }
        txt_input_iaquery.requestFocus();
        txt_input_iaquery.positionCaret(txt_input_iaquery.getText().length());
    }

    private String cleanQuotedGratienMessage(String message) {
        if (message == null) {
            return "";
        }
        String cleaned = message
            .replaceAll("!\\[[^\\]]*\\]\\(data:image/[^)]+\\)", "[image]")
            .replaceAll("(?s)<think>.*?</think>", "")
            .replace("\r", "")
            .trim();
        if (cleaned.length() > 1800) {
            cleaned = cleaned.substring(0, 1800).trim() + "\n...";
        }
        return cleaned;
    }

    private String escapeForJS(String text) {
        return ChatHtmlTemplate.jsString(text);
    }

    private String throwableMessage(Throwable error) {
        if (error == null) {
            return "erreur inconnue retournée par le flux IA.";
        }
        String message = error.getMessage();
        return message == null || message.isBlank()
            ? error.getClass().getSimpleName()
            : message;
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        boolean darkEnabled = !pref.getBoolean(
            Kazisafex.DARK_THEME_PREF,
            false
        );
        pref.putBoolean(Kazisafex.DARK_THEME_PREF, darkEnabled);
        refreshThemeView();
    }

    private void refreshThemeView() {
        if (mainpane == null || mainpane.getScene() == null) {
            return;
        }
        Kazisafex.applyTheme(mainpane.getScene());
        boolean darkEnabled = pref.getBoolean(Kazisafex.DARK_THEME_PREF, false);
        btn_theme_toggle.setText(
            darkEnabled
                ? bundle.getString("xbtn.theme.light")
                : bundle.getString("xbtn.theme.dark")
        );
        if (darkEnabled) {
            appName.setTextFill(Color.web("#a7d8ff"));
            tbar_menu.setStyle("-fx-background-color: #111827;");
        } else {
            appName.setTextFill(Color.web("#44cef5"));
            tbar_menu.setStyle("-fx-background-color: #ffffff;");
        }
    }

    public Label getTxt_states_features() {
        return txt_states_features;
    }

    public void cloturer(LocalDate d1, LocalDate d2, String context) {
        try {
            sync_txt_message.setVisible(true);
            sync_pg_bar.setVisible(true);
            MainUI.notify(
                null,
                "",
                "Veuillez patientez que la cloture de stock se termine",
                15,
                "warning"
            );
            setSyncMessage("Cloture des stocks en cours....");
            ag = Agregator.getInstance();
            ag.setLocalTaskStateListener(
                new LocalTaskStateListener() {
                    @Override
                    public void onFinish(boolean isfinished, String name) {
                        Platform.runLater(() -> {
                            if (name.contains("stock")) {
                                sync_txt_message.setVisible(false);
                                sync_pg_bar.setVisible(false);
                                summarise();
                            }
                        });
                    }

                    @Override
                    public void onProgress(double progress, String message) {
                        Platform.runLater(() -> {
                            setSyncMessage(message);
                            sync_pg_bar.setProgress(progress);
                        });
                    }
                }
            );
            ag.agregate(
                d1,
                d2,
                d1.equals(d2) ? "Journalier du " + d1 : context
            );
            ag.setOnReportSavedListener(
                (double chiffreAffaire, double coutVariable) -> {
                    Platform.runLater(() -> {
                        dashCardVente(chiffreAffaire);
                        dashCardDepense(coutVariable);
                        dashCardResult(chiffreAffaire, coutVariable);
                    });
                }
            );
            ag.reportInBackground();
        } catch (java.lang.RuntimeException e) {}
    }

    private void setSyncMessage(String message) {
        if (sync_txt_message == null) {
            return;
        }
        if (sync_txt_message.textProperty().isBound()) {
            sync_txt_message.textProperty().unbind();
        }
        sync_txt_message.setText(message == null ? "" : message);
    }

    public void sync(Kazisafe ksf) {
        ScheduledExecutorService ses =
            Executors.newSingleThreadScheduledExecutor();
        ses.scheduleWithFixedDelay(
            () -> {
                if (ag != null) {
                    if (ag.isFinish() && NetLoockup.NETWORK_STATUS_ON) {
                        System.out.println("Connected on Internet");
                        Platform.runLater(() -> {
                            SyncEngine.getInstance().syncWithHttpProtocol(
                                sync_txt_message,
                                ksf
                            );
                        });
                    }
                }
            },
            1,
            8,
            TimeUnit.MINUTES
        );
    }

    @FXML
    public void iconify(Event evt) {
        MainUI.minimize();
    }

    public void cleanSearchBar() {
        searchField.clear();
        searchField.requestFocus();
    }

    public void reinitSearchBar() {
        searchField.selectAll();
    }

    public List<Vente> getVentes(LocalDate date) {
        LocalDate kesho = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, kesho); // db.findAllByDateInterval(Vente.class, d1,
        // kesho);
        if (vts == null) {
            return null;
        }
        return vts;
    }

    public List<Vente> getVentesDebt(LocalDate date) {
        LocalDate date1 = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, date1);
        return vts;
    }

    public List<Vente> getVentesDebt(LocalDate date, String region) {
        LocalDate date1 = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(
            date,
            date1,
            region
        );
        return vts;
    }

    public List<Vente> getVentesInMoth(String month) {
        List<Vente> result = new ArrayList<>();
        List<Vente> vts = VenteDelegate.findVentes(); // db.findAll(Vente.class);
        for (Vente vt : vts) {
            String dv = String.valueOf(vt.getDateVente().getMonthValue());
            if (dv.equals(month)) {
                result.add(vt);
            }
        }
        return result;
    }

    public List<Operation> getOpsInMonth(String month) {
        List<Operation> result = new ArrayList<>();
        List<Operation> vts = OperationDelegate.findOperations(); // db.findAll(Operation.class);
        if (vts != null) {
            for (Operation vt : vts) {
                String dv = String.valueOf(vt.getDate().getMonthValue());
                if (dv.equals(month)) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Vente> getVentesInMoth(String month, String region) {
        List<Vente> result = new ArrayList<>();
        List<Vente> vts = VenteDelegate.findVentes(region); // db.findAllByRegion(Vente.class, region);
        if (vts != null) {
            for (Vente vt : vts) {
                String dv = String.valueOf(vt.getDateVente().getMonthValue());
                if (dv.equals(month)) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Operation> getOpsInMonth(String month, String region) {
        List<Operation> result = new ArrayList<>();
        List<Operation> vts = OperationDelegate.findOperations(region); // db.findAllByRegion(Operation.class, region);
        if (vts != null) {
            for (Operation vt : vts) {
                String dv = tools.Constants.YEAR_AND_MONTH_FORMAT.format(
                    vt.getDate()
                );
                if (
                    dv.equals(month) && vt.getRegion().equalsIgnoreCase(region)
                ) {
                    result.add(vt);
                }
            }
        }
        return result;
    }

    public List<Operation> getOps(LocalDate date) {
        LocalDate kesho = date.plusDays(1);
        List<Operation> vts = OperationDelegate.findByDateInterval(date, kesho);
        return vts;
    }

    public List<Operation> getOps(LocalDate date, String region) {
        LocalDate kesho = date.plusDays(1);
        List<Operation> vts = OperationDelegate.findByDateInterval(
            date,
            kesho,
            region
        );
        return vts;
    }

    public List<Vente> getVentes(LocalDate date, String region) {
        LocalDate kesho = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(
            date,
            kesho,
            region
        ); // db.findAllByDateIntervalInRegion(Vente.class,
        // d1, kesho, region);
        return vts;
    }

    @FXML
    public void enlarge(Event e) {
        MainUI.enlarge();
    }

    public void dashCardVente(double sumSales) {
        System.out.println("Sum sale " + sumSales);
        String somV = maker.isUsd()
            ? "$ " +
              formatNumber(
                  BigDecimal.valueOf(sumSales)
                      .setScale(1, RoundingMode.FLOOR)
                      .doubleValue()
              )
            : "Fc " +
              formatNumber(
                  BigDecimal.valueOf(sumSales * taux)
                      .setScale(2, RoundingMode.FLOOR)
                      .doubleValue()
              );
        svente.setText(somV);
    }

    public void creanceToday() {
        if (
            role.equals(Role.Trader.name()) |
            role.contains(Role.ALL_ACCESS.name())
        ) {
            List<Vente> ventes = getVentesDebt(LocalDate.now());
            double sumSales = Util.sumCreditSales(ventes, taux);
            String somC = maker.isUsd()
                ? "$ " +
                  formatNumber(
                      BigDecimal.valueOf(sumSales)
                          .setScale(2, RoundingMode.HALF_EVEN)
                          .doubleValue()
                  )
                : "Fc " +
                  formatNumber(
                      BigDecimal.valueOf(sumSales * taux)
                          .setScale(2, RoundingMode.HALF_EVEN)
                          .doubleValue()
                  );
            screance.setText(somC);
        } else {
            List<Vente> ventes = getVentesDebt(LocalDate.now(), region);
            double sumSales = Util.sumCreditSales(ventes, taux);
            String somC = maker.isUsd()
                ? "$ " +
                  formatNumber(
                      BigDecimal.valueOf(sumSales)
                          .setScale(2, RoundingMode.HALF_EVEN)
                          .doubleValue()
                  )
                : "Fc " +
                  formatNumber(
                      BigDecimal.valueOf(sumSales * taux)
                          .setScale(2, RoundingMode.HALF_EVEN)
                          .doubleValue()
                  );
            screance.setText(somC);
        }
    }

    public void dashCardResult(double sumSales, double achat) {
        double result = sumSales - achat;
        String r = maker.isUsd()
            ? "$ " +
              formatNumber(
                  BigDecimal.valueOf(result)
                      .setScale(2, RoundingMode.HALF_EVEN)
                      .doubleValue()
              )
            : "Fc " +
              formatNumber(
                  BigDecimal.valueOf(result * taux)
                      .setScale(2, RoundingMode.HALF_EVEN)
                      .doubleValue()
              );
        stresor.setText(r);
    }

    public void dashCardDepense(double achat) {
        sdepense.setText(
            maker.isUsd()
                ? "$ " + formatNumber(achat)
                : "Fc " + formatNumber(achat * taux)
        );
    }

    public void metrify() {
        venteChart.setLegendVisible(true);
        XYChart.Series<String, Number> serie_vente = new XYChart.Series();
        XYChart.Series<String, Number> serie_prixderevient =
            new XYChart.Series();
        XYChart.Series<String, Number> serie_resultat = new XYChart.Series();
        serie_vente.setName(bundle.getString("xgraph.seri1_vente").trim());
        serie_prixderevient.setName(
            bundle.getString("xgraph.seri2_depens").trim()
        );
        serie_resultat.setName(bundle.getString("xgraph.seri3_marg").trim());
        int month = LocalDate.now().getMonthValue();
        // System.out.println("mois en cours " + month);
        List<Metric> kpis;

        if (
            role.equals(Role.Trader.name()) |
            role.contains(Role.ALL_ACCESS.name())
        ) {
            kpis = RepportDelegate.kpiValues(
                LocalDate.of(Year.now().getValue(), Month.JANUARY, 1),
                LocalDate.now(),
                "%",
                "Mensuel"
            );
        } else {
            kpis = RepportDelegate.kpiValues(
                LocalDate.of(Year.now().getValue(), Month.JANUARY, 1),
                LocalDate.now(),
                region,
                "Mensuel"
            );
        }
        for (Metric kpi : kpis) {
            LocalDate period = kpi.period();
            String moix = period
                .getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRANCE);
            serie_vente
                .getData()
                .add(new XYChart.Data<>(moix, kpi.chiffreAffaire()));
            serie_prixderevient
                .getData()
                .add(new XYChart.Data<>(moix, kpi.coutAchat()));
            serie_resultat
                .getData()
                .add(new XYChart.Data<>(moix, kpi.result()));
        }

        venteChart
            .getData()
            .addAll(serie_vente, serie_prixderevient, serie_resultat);
        for (XYChart.Series<String, Number> serie : venteChart.getData()) {
            for (XYChart.Data<String, Number> data : serie.getData()) {
                String text =
                    serie.getName() +
                    "\n" +
                    data.getXValue() +
                    " : " +
                    formatNumber(data.getYValue().doubleValue()) +
                    " " +
                    maker.getMainCurrency();
                Tooltip tooltip = new Tooltip(text);
                Tooltip.install(data.getNode(), tooltip);
                data.getNode().setStyle(
                    "-fx-background-color: #ff6600, white; -fx-padding: 5px;"
                );
            }
        }
        venteChart.setLegendSide(Side.BOTTOM);
    }

    private double sumCout(List<Vente> ventes) {
        double coutTotal = 0;
        for (Vente vente : ventes) {
            List<LigneVente> items = LigneVenteDelegate.findByReference(
                vente.getUid()
            );
            coutTotal += items
                .stream()
                .mapToDouble(
                    l ->
                        l.getQuantite() *
                        (l.getCoutAchat() == null ? 0 : l.getCoutAchat())
                )
                .sum();
        }
        return coutTotal;
    }

    public void summarise() {
        maker.refreshFromPreferences();
        // System.out.println("is summary called");
        creanceToday();
        loadSaleChart();
        loadProChart();
    }

    String reg = null;

    private void loadProChart() {
        piepane.setPrefSize(351, 318);
        piepane.setLabelsVisible(true);
        List<TopTen> entries;
        if (
            role.equals(Role.Trader.name()) ||
            role.contains(Role.ALL_ACCESS.name())
        ) {
            entries = VenteDelegate.getTop10ProductDesc();
        } else {
            entries = VenteDelegate.getTop10ProductDesc(region);
        }
        // System.out.println("entree " + entries.size());
        for (TopTen top : entries) {
            PieChart.Data data = new PieChart.Data(top.nomp(), top.quantite());

            piepane.setAnimated(true);
            if (!existPie(piepane.getData(), data.getName())) {
                piepane.getData().add(data);
                Tooltip bull = new Tooltip(
                    data.getName() +
                        " : " +
                        formatNumber(data.getPieValue()) +
                        " " +
                        top.mesure()
                );
                Tooltip.install(data.getNode(), bull);
                data.pieValueProperty().addListener(
                    new ChangeListener<Number>() {
                        @Override
                        public void changed(
                            ObservableValue<? extends Number> ov,
                            Number t,
                            Number t1
                        ) {
                            bull.setText(
                                data.getName() +
                                    " : " +
                                    formatNumber(data.getPieValue()) +
                                    " " +
                                    top.mesure()
                            );
                        }
                    }
                );
            }
        }
        piepane.setLabelsVisible(true);
    }

    private String formatNumber(double value) {
        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        } else {
            return Double.toString(value);
        }
    }

    private void loadSaleChart() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            () -> {
                Platform.runLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            venteChart.getData().clear();
                            metrify();
                        }
                    }
                );
            },
            2,
            60,
            TimeUnit.SECONDS
        );
    }

    public boolean existPie(ObservableList<PieChart.Data> data, String name) {
        for (PieChart.Data d : data) {
            if (d.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void setActiveMenu(ImageView target) {
        List<ImageView> menus = List.of(
            home,
            products_gate,
            caisse,
            immobilisation,
            stockage,
            pos,
            production,
            agents,
            rapport,
            compagnie,
            parametre
        );
        for (ImageView menu : menus) {
            if (menu == null) {
                continue;
            }
            if (menu.equals(target)) {
                menu.setOpacity(1d);
                menu.setScaleX(1.08);
                menu.setScaleY(1.08);
                menu.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, #44cef5, 14, 0.2, 0, 0);"
                );
            } else {
                menu.setOpacity(0.62);
                menu.setScaleX(1d);
                menu.setScaleY(1d);
                menu.setStyle("");
                MainUI.removeShaddowEffect(menu);
            }
        }
        activeMenuIcon = target;
    }

    private ImageView menuForView(String viewName) {
        return switch (viewName) {
            case tools.Constants.MAIN -> home;
            case tools.Constants.PRODUIT -> products_gate;
            case tools.Constants.CAISSES -> caisse;
            case tools.Constants.IMMOBILISATIONS -> immobilisation;
            case tools.Constants.STORAGE -> stockage;
            case tools.Constants.POS -> pos;
            case tools.Constants.PRODUCTION -> production;
            case tools.Constants.AGENTS -> agents;
            case tools.Constants.REPPORTS -> rapport;
            case tools.Constants.ENTREPRISE -> compagnie;
            case tools.Constants.PARAMETRES -> parametre;
            default -> null;
        };
    }

    /**
     * Always loads a fresh FXML page.  Views are rebuilt every time to avoid
     * VirtualFlow rendering issues.  Data is cached separately via
     * {@link tools.DataCache}.
     */
    private AnchorPane loadPage(String fxml, Object... arguments) {
        return MainUI.getPage(this, fxml, token, arguments);
    }

    /**
     * Pages are no longer cached.  Each navigation loads a fresh FXML view.
     */

    public void setUserPhone(String phone) {
        this.phone = phone;
    }

    @FXML
    private void switchToDashBoard(MouseEvent event) {
        mainpane.getChildren().remove(0);
        mainpane.getChildren().add(showPane);
        pane_title.setText("Tableau de bord");
        image_title.setImage(
            new Image(
                this.getClass().getResourceAsStream("/icons/dashboard(2).png")
            )
        );
        if (ag != null) {
            ag.reportInBackground();
        }
        CURRENT_VIEW = "DASHBOARD";
        setActiveMenu(home);
    }

    @FXML
    public void switchToStock(Event event) {
        if (go()) {
            switchSimpleScreens(
                tools.Constants.STORAGE_VIEW,
                STORAGE,
                "Stockage",
                "warehouse(1).png"
            );
        }
    }

    @FXML
    private void switchToSettings(MouseEvent event) {
        switchSimpleScreens(
            tools.Constants.PARAMETRE_VIEW,
            PARAMETRES,
            "Paramètres",
            "speedometer(2).png"
        );
    }

    public void switchSimpleScreens(
        String xml,
        String viewName,
        String title,
        String iconName
    ) {
        Executors.newCachedThreadPool().submit(() -> {
            Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        if (
                            CURRENT_VIEW == null
                                ? true
                                : !CURRENT_VIEW.equals(viewName)
                        ) {
                            txt_states_features.setText("...");
                            AnchorPane p = loadPage(
                                xml,
                                getEntreprisex(),
                                kazisafe
                            );
                            if (p == null) {
                                return;
                            }
                            p.setLayoutY(LAYOUTY);
                            p.setLayoutX(LAYOUTX);
                            mainpane.getChildren().remove(0);
                            mainpane.getChildren().add(p);
                            pane_title.setText(title);
                            image_title.setImage(
                                new Image(
                                    MainuiController.this
                                        .getClass()
                                        .getResourceAsStream(
                                            "/icons/" + iconName
                                        )
                                )
                            );
                            CURRENT_VIEW = viewName;
                            setActiveMenu(menuForView(viewName));
                        }
                    }
                }
            );
        });
    }

    public void switchScreens(
        String xml,
        String viewName,
        String title,
        String iconName,
        Vente v,
        Object liv
    ) {
        if (CURRENT_VIEW == null ? true : !CURRENT_VIEW.equals(viewName)) {
            txt_states_features.setText("...");
            AnchorPane p = loadPage(
                xml,
                getEntreprisex(),
                v,
                liv
            );
            if (p == null) {
                return;
            }
            p.setLayoutY(LAYOUTY);
            p.setLayoutX(LAYOUTX);
            mainpane.getChildren().remove(0);
            mainpane.getChildren().add(p);
            pane_title.setText(title);
            image_title.setImage(
                new Image(
                    this.getClass().getResourceAsStream("/icons/" + iconName)
                )
            );
            CURRENT_VIEW = viewName;
            setActiveMenu(menuForView(viewName));
        }
    }

    @FXML
    public void switchToPos(Event event) {
        if (go()) {
            switchSimpleScreens(
                tools.Constants.POS_VIEW,
                POS,
                "Ventes & Recquisitions",
                "shopping-cart(1).png"
            );
        }
    }

    @FXML
    public void switchToTresorerie(Event event) {
        if (go()) {
            switchScreens(
                tools.Constants.CAISSE_VIEW,
                CAISSES,
                "Trésorerie",
                "cashier.png",
                null,
                null
            );
        }
    }

    @FXML
    private void switchToAgents(MouseEvent event) {
        if (go()) {
            if (
                CURRENT_VIEW == null
                    ? true
                    : !CURRENT_VIEW.equals(tools.Constants.AGENTS)
            ) {
                txt_states_features.setText("...");
                AnchorPane p = loadPage(
                    tools.Constants.AGENTS_VIEW,
                    getEntreprisex(),
                    kazisafe
                );
                if (p == null) {
                    return;
                }
                p.setLayoutY(LAYOUTY);
                p.setLayoutX(LAYOUTX);
                mainpane.getChildren().remove(0);
                mainpane.getChildren().add(p);
                pane_title.setText("Agents");
                image_title.setImage(
                    new Image(
                        this.getClass().getResourceAsStream(
                            "/icons/hosting-services.png"
                        )
                    )
                );
                CURRENT_VIEW = tools.Constants.AGENTS;
                setActiveMenu(agents);
            }
        }
    }

    @FXML
    private void switchToCompany(MouseEvent event) {
        txt_states_features.setText("...");
        AnchorPane p = loadPage(
            tools.Constants.ENTREPRISE_VIEW,
            getEntreprisex(),
            kazisafe,
            user
        );
        if (p == null) {
            return;
        }
        p.setLayoutY(LAYOUTY);
        p.setLayoutX(LAYOUTX);
        mainpane.getChildren().remove(0);
        mainpane.getChildren().add(p);
        pane_title.setText("Entreprise");
        image_title.setImage(
            new Image(
                this.getClass().getResourceAsStream(
                    "/icons/office-building.png"
                )
            )
        );
        CURRENT_VIEW = tools.Constants.ENTREPRISE;
        setActiveMenu(compagnie);
    }

    public void sseSync() {
        if (go()) {
            Thread sseThread = new Thread(() -> {
                EventHandler evh = new tools.NotificationHandler();
                String url =
                    KazisafeServiceFactory.BASE_URL + "notification/events";
                Headers headers = new Headers.Builder()
                    .add("Authorization", "Bearer " + token)
                    .build();
                // readTimeout DOIT être supérieur à l'intervalle heartbeat du serveur (15s)
                // mais assez court pour détecter les connexions mortes rapidement.
                // 3 minutes = 12 heartbeats manqués → connexion considérée morte → reconnexion.
                // Cela évite les "sinks fantômes" qui s'accumulent côté Quarkus (SseRegistry).
                EventSource.Builder evb = new EventSource.Builder(
                    evh,
                    URI.create(url)
                )
                    .headers(headers)
                    .reconnectTime(30, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.MINUTES);
                EventSource evs = evb.build();
                if (evh instanceof tools.NotificationHandler) {
                    ((tools.NotificationHandler) evh).setEventSource(evs);
                }
                evs.start();
            }, "kazisafe-sse-listener");
            sseThread.setDaemon(true);
            sseThread.start();
        }
    }

    @FXML
    public void switchToProduct(Event event) {
        if (go()) {
            Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        if (
                            CURRENT_VIEW == null
                                ? true
                                : !CURRENT_VIEW.equals(tools.Constants.PRODUIT)
                        ) {
                            txt_states_features.setText("...");
                            AnchorPane p = loadPage(
                                tools.Constants.PRODUITS_VIEW,
                                entreprisex
                            );
                            if (p == null) {
                                return;
                            }
                            p.setLayoutY(LAYOUTY);
                            p.setLayoutX(LAYOUTX);
                            mainpane.getChildren().remove(0);
                            mainpane.getChildren().add(p);
                            pane_title.setText("Produits");
                            image_title.setImage(
                                new Image(
                                    this.getClass().getResourceAsStream(
                                        "/icons/boxes.png"
                                    )
                                )
                            );
                            CURRENT_VIEW = tools.Constants.PRODUIT;
                            setActiveMenu(products_gate);
                        }
                    }
                }
            );
        }
    }

    @FXML
    public void switchToRepport(Event event) {
        if (go()) {
            switchSimpleScreens(
                tools.Constants.REPPORT_VIEW,
                REPPORTS,
                "Rapports",
                "report.png"
            );
        }
    }

    @FXML
    private void switchToImmobilisation(MouseEvent event) {
        if (go()) {
            switchSimpleScreens(
                tools.Constants.IMMOBILISATION_VIEW,
                tools.Constants.IMMOBILISATIONS,
                "Immobilisations",
                "annual-report.png"
            );
        }
    }

    @FXML
    private void exit(Event event) {
        pref.putInt("exit", 1);
        if (!pref.getBoolean("session", false)) {
            pref.remove("token");
        }
        // if (sep != null) {
        // sep.closeSession();
        // }

        System.exit(0);
    }

    @FXML
    private void onHoverHome(Event event) {
        Object obj = event.getSource();
        if (obj instanceof ImageView img) {
            MainUI.setShadowEffect(img);
        } else if (obj instanceof Label img) {
            MainUI.setShadowEffect(img);
        }
    }

    @FXML
    private void onExitOverlay(Event event) {
        Object obj = event.getSource();
        if (obj instanceof ImageView img) {
            MainUI.setShadowAlertEffect(img);
        } else if (obj instanceof Label img) {
            MainUI.setShadowAlertEffect(img);
        }
    }

    @FXML
    private void onOutHome(MouseEvent event) {
        Object obj = event.getSource();
        if (obj instanceof ImageView img) {
            if (img == activeMenuIcon) {
                return;
            }
            MainUI.removeShaddowEffect(img);
        } else if (obj instanceof Label img) {
            MainUI.removeShaddowEffect(img);
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

    public void setEntrepiseId(String entrepiseId) {
        this.entrepiseId = entrepiseId;
    }

    public Entreprise getEntreprisex() {
        if (entreprisex == null) {
            String ezi = pref.get("eUid", "f3d81978a5524681bf1090d1d41edb15");
            String eName = pref.get("ent_name", null);
            String id = pref.get("ent_ID", null);
            String adresse = pref.get("ent_adresse", null);
            String email = pref.get("ent_email", null);
            String idnat = pref.get("ent_idnat", null);
            String impot = pref.get("ent_impot", null);
            String phonez = pref.get("ent_phones", null);
            token = pref.get("token", null);
            region = pref.get("region", "...");
            txt_region.setText(region);
            String user = pref.get("operator", "Chargement...");
            entreprisex = new Entreprise(ezi);
            entreprisex.setNomEntreprise(eName);
            entreprisex.setAdresse(adresse);
            entreprisex.setIdNat(idnat == null ? " " : idnat);
            entreprisex.setNumeroImpot(impot == null ? " " : impot);
            entreprisex.setPhones(phonez == null ? " " : phonez);
            entreprisex.setIdentification(id);
            entrep_name.setText(entreprisex.getNomEntreprise());
            user_connected.setText(user);
            entreprisex.setEmail(email);
        }
        return entreprisex;
    }

    private void initializePermissions(
        LoginWebResult loginResult,
        Runnable next
    ) {
        // System.out.println("Permix-ion "+loginResult.getJsonPermissions()+"
        // "+loginResult.getRole());
        if (loginResult.getJsonPermissions() == null) {
            next.run();
            return;
        }
        if (loginResult.getJsonPermissions().startsWith("[")) {
            try {
                ObjectMapper drx = KazisafeServiceFactory.mapper();
                region = loginResult.getRegion();
                role = loginResult.getRole();
                List<Permission> perms = drx.readValue(
                    loginResult.getJsonPermissions(),
                    new TypeReference<List<Permission>>() {}
                );
                List<Permission> tosave = perms
                    .stream()
                    .map(p -> {
                        p.setTablename(loginResult.getUserContract());
                        return p;
                    })
                    .collect(Collectors.toList());
                PermissionDelegate.renewPermissions(tosave);
                pref.put("priv", loginResult.getRole());
            } catch (JsonProcessingException ex) {
                Logger.getLogger(MainuiController.class.getName()).log(
                    Level.SEVERE,
                    null,
                    ex
                );
            }
        }
        next.run();
    }

    private void initializePreferencesAndGui(
        LoginWebResult logr,
        Runnable next
    ) {
        if (logr.getNomentreprise() != null) {
            pref.put("ent_name", logr.getNomentreprise());
            entrep_name.setText(logr.getNomentreprise());
        } else {
            String eName = pref.get("ent_name", "Chargement...");
            entrep_name.setText(eName);
            logr.setNomentreprise(eName);
        }
        role = logr.getRole();
        if (role == null) {
            role = pref.get("role", "Trader");
        }
        region = logr.getRegion();
        if (region == null) {
            region = pref.get("region", "Goma");
        }
        txt_region.setText(region);
        if (logr.getRccm() != null) {
            pref.put("ent_ID", logr.getRccm());
            this.rccm = logr.getRccm();
        } else {
            String id = pref.get("ent_ID", null);
            logr.setRccm(id);
            this.rccm = id;
        }
        if (logr.getAdresseEntreprise() != null) {
            pref.put("ent_adresse", logr.getAdresseEntreprise());
        } else {
            String adresse = pref.get("ent_adresse", null);
            logr.setAdresseEntreprise(adresse);
        }
        if (logr.getEmailEntreprise() != null) {
            pref.put("ent_email", logr.getEmailEntreprise());
        } else {
            String email = pref.get("ent_email", null);
            logr.setEmailEntreprise(email);
        }
        if (logr.getIdNat() != null) {
            pref.put("ent_idnat", logr.getIdNat());
        } else {
            String idnat = pref.get("ent_idnat", null);
            logr.setIdNat(idnat);
        }
        if (logr.getNumeroImpot() != null) {
            pref.put("ent_impot", logr.getNumeroImpot());
        } else {
            String impot = pref.get("ent_impot", null);
            logr.setNumeroImpot(impot);
        }
        if (logr.getPhoneEntrerprise() != null) {
            pref.put("ent_phones", logr.getPhoneEntrerprise());
        } else {
            String phonez = pref.get("ent_phones", null);
            logr.setPhoneEntrerprise(phonez);
        }
        String u = logr.getNomUtilisateur() + " " + logr.getPrenomUtilisateur();
        if (!u.contains("null")) {
            pref.put("operator", u);
        } else {
            u = pref.get("operator", "Chargement...");
            if (u.length() == 2) {
                logr.setNomUtilisateur(u.split(" ")[0]);
                logr.setPrenomUtilisateur(u.split(" ")[1]);
            }
        }
        entreprisex = new Entreprise(logr.getEntrepriseId());
        entreprisex.setNomEntreprise(logr.getNomentreprise());
        entreprisex.setAdresse(logr.getAdresseEntreprise());
        entreprisex.setCategory(logr.getCategoryEntreprise());
        entreprisex.setEmail(logr.getEmailEntreprise());
        entreprisex.setIdNat(logr.getIdNat());
        entreprisex.setIdentification(logr.getRccm());
        entreprisex.setNumeroImpot(logr.getNumeroImpot());
        entreprisex.setPhones(logr.getPhoneEntrerprise());
        AiAgents.getInstance().startForUser(
            logr.getUserId(),
            logr.getEntrepriseId()
        );

        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    String nom = logr.getNomUtilisateur(),
                        prenom = logr.getPrenomUtilisateur();
                    user_connected.setText(
                        nom != null && prenom != null
                            ? nom + " " + prenom
                            : pref.get("operator", "Chargement...")
                    );
                }
            }
        );
        next.run();
    }

    private void initializeImages(LoginWebResult logr, Runnable next) {
        if (logr.getUserId() != null && !logr.getUserId().isBlank()) {
            kazisafe.downloadUserPhotoSecurely(logr.getUserId()).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                        Call<ResponseBody> call,
                        Response<ResponseBody> rspns
                    ) {
                        if (rspns.isSuccessful()) {
                            InputStream is = rspns.body().byteStream();
                            Image image = new Image(is, 90, 94, true, true);
                            img_profile.setImage(MainUI.makeTransparent(image));
                            Circle clip = new Circle(16);
                            clip.setStrokeType(StrokeType.CENTERED);
                            clip.setStroke(Color.valueOf("#44cef5"));
                            clip.setStrokeWidth(3);
                            clip.setCenterX(img_profile.getFitWidth() / 2);
                            clip.setCenterY(img_profile.getFitHeight() / 2);
                            img_profile.setClip(clip);
                            centerImage(img_profile);
                        }
                    }

                    @Override
                    public void onFailure(
                        Call<ResponseBody> call,
                        Throwable thrwbl
                    ) {
                        System.err.println(
                            "Erreur image profile " + thrwbl.getMessage()
                        );
                    }
                }
            );
        }
        kazisafe.downloadLogo(logr.getEntrepriseId()).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> rspns
                ) {
                    if (rspns.isSuccessful()) {
                        try {
                            ResponseBody body = rspns.body();
                            byte[] logoBytes = body.bytes();

                            // Persist to disk for document generators (invoices, orders, etc.)
                            try {
                                FileUtils.byteToFile(
                                    logr.getEntrepriseId(),
                                    logoBytes,
                                    "png"
                                );
                            } catch (Exception eDisk) {
                                Logger.getLogger(
                                    MainuiController.class.getName()
                                ).log(
                                    Level.WARNING,
                                    "Failed to persist company logo to disk",
                                    eDisk
                                );
                            }

                            // Display in ImageView
                            ByteArrayInputStream bais =
                                new ByteArrayInputStream(logoBytes);
                            Image image = new Image(bais, 40, 40, true, true);
                            bais.close();
                            img_company_logo.setImage(
                                MainUI.makeTransparent(image)
                            );
                            img_company_logo.setVisible(true);

                            Circle clip = new Circle(16);
                            clip.setStrokeType(StrokeType.CENTERED);
                            clip.setStroke(Color.valueOf("#44cef5"));
                            clip.setStrokeWidth(3);
                            clip.setCenterX(img_company_logo.getFitWidth() / 2);
                            clip.setCenterY(
                                img_company_logo.getFitHeight() / 2
                            );
                            img_company_logo.setClip(clip);
                            centerImage(img_company_logo);
                        } catch (Exception e) {
                            img_company_logo.setVisible(false);
                            Logger.getLogger(
                                MainuiController.class.getName()
                            ).log(Level.SEVERE, null, e);
                        }
                    } else {
                        img_company_logo.setVisible(false);
                    }
                }

                @Override
                public void onFailure(
                    Call<ResponseBody> call,
                    Throwable thrwbl
                ) {
                    System.out.println("erreur logo e " + thrwbl.getMessage());
                }
            }
        );
        kazisafe.getAbonnements().enqueue(
            new Callback<List<Abonnement>>() {
                @Override
                public void onResponse(
                    Call<List<Abonnement>> call,
                    Response<List<Abonnement>> rspns
                ) {
                    if (rspns.isSuccessful()) {
                        List<Abonnement> abns = rspns.body();
                        System.out.println("Souscrizise-->> " + abns.size());
                        for (Abonnement abn : abns) {
                            String etat = abn.getEtat();
                            String typeAb = abn.getTypeAbonnement();

                            switch (typeAb) {
                                case "Gold", "Metal", "Super gold" -> {
                                    pref.put("type-sub", typeAb);
                                    String status =
                                        SubscriptionUtil.computeStatus(abn);
                                    Duration time =
                                        SubscriptionUtil.remainingDuration(abn);
                                    if (time.minusDays(7).isZero()) {
                                        MainUI.notify(
                                            null,
                                            "Attention",
                                            "Le crédit Kazisafe (Record) expire bientôt, pensez à le renouveller",
                                            5,
                                            "warning"
                                        );
                                    }

                                    long max =
                                        SubscriptionUtil.nextSubscriptionMillis(
                                            abn
                                        );
                                    System.err.println(
                                        "Abonnement total " +
                                            max +
                                            " rest " +
                                            time.toMillis()
                                    );
                                    pref.putDouble("sub", Double.valueOf(max));
                                    pref.put("etat-sub", etat);
                                    if (
                                        !status.equals(
                                            Constants.ETAT_SUBSCRIPTION_EXPIRY
                                        )
                                    ) {
                                        MainUI.notifySync(
                                            "Kazisafe-Abonnement",
                                            "Activation souscription " +
                                                typeAb +
                                                " faite avec succes",
                                            "Notification de souscription au service kazisafe"
                                        );
                                    }
                                }
                                case "PRO" -> {
                                    double nombreOper =
                                        abn.getNombreOperation();
                                    pref.put("pro-sub", typeAb);
                                    pref.putDouble("subscripro", nombreOper);
                                    pref.put("pro-etat", etat);
                                    MainUI.notifySync(
                                        "Kazisafe-Abonnement",
                                        "Abonnement " +
                                            typeAb +
                                            " de " +
                                            formatNumber(nombreOper) +
                                            " eBonus active",
                                        "Notification de souscription au service kazisafe"
                                    );
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(
                    Call<List<Abonnement>> call,
                    Throwable thrwbl
                ) {
                    System.err.println("No network");
                }
            }
        );
    }

    public void setLoginResult(LoginWebResult loginResult) {
        token = loginResult.getToken();
        kazisafe = KazisafeServiceFactory.createService(loginResult.getToken());
        KazisafeServiceFactory.setOnTokenRefreshCallback((Token var1) -> {
            MainuiController.this.token = var1.getToken();
            pref.put("token", var1.getToken());
        });
        // URL = KazisafeServiceFactory.WEBSOCKET + "/wssync/ez/" +
        // loginResult.getEntrepriseId() ;
        // sep = new SyncEndpoint(URL,token);
        pref.putInt("exit", 0);
        pref.putLong("entretime", loginResult.getCreationTimestamp());
        // if (services.ManagedSessionFactory.isEmbedded()) {
        // if (services.ManagedSessionFactory.isBdCreated()) {
        initializePermissions(loginResult, () -> {
            initializePreferencesAndGui(loginResult, () -> {
                initializeImages(loginResult, () -> {
                    kazisafe.getRegions().enqueue(
                        new retrofit2.Callback<List<String>>() {
                            @Override
                            public void onResponse(
                                Call<List<String>> call,
                                Response<List<String>> rspns
                            ) {
                                System.out.println(
                                    "Reponse web server " + rspns.code()
                                );
                                if (rspns.isSuccessful()) {
                                    List<String> lreg = rspns.body();
                                    int i = 0;
                                    for (String reg : lreg) {
                                        pref.put("region" + ++i, reg);
                                    }
                                    System.err.println(
                                        "Agent regions " + lreg.size()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                Call<List<String>> call,
                                Throwable thrwbl
                            ) {
                                System.err.println("Error : " + thrwbl);
                            }
                        }
                    );
                    SyncEngine.getInstance().setup(token);
                });
            });
        });
        // }
        // }
        System.out.println("permix " + loginResult.getRole());
        if (role.equals(Role.Saler.name())) {
            tbar_menu.getItems().remove(caisse);
            tbar_menu.getItems().remove(immobilisation);
            tbar_menu.getItems().remove(stockage);
            tbar_menu.getItems().remove(agents);
            tbar_menu.getItems().remove(production);
        } else if (role.equals(Role.Magazinner.name())) {
            tbar_menu.getItems().remove(caisse);
            tbar_menu.getItems().remove(immobilisation);
            tbar_menu.getItems().remove(agents);
            tbar_menu.getItems().remove(rapport);
        } else if (role.equals(Role.Finance.name())) {
            tbar_menu.getItems().remove(stockage);
            tbar_menu.getItems().remove(production);
            tbar_menu.getItems().remove(agents);
            tbar_menu.getItems().remove(rapport);
        } else if (
            role.equals(Role.Manager.name()) ||
            role.contains(Role.ALL_ACCESS.name())
        ) {
            tbar_menu.getItems().remove(agents);
        } else {
        }
        SyncEngine.getInstance().startChecking();
        searchField
            .textProperty()
            .addListener(
                (
                    ObservableValue<? extends String> observable,
                    String oldValue,
                    String newValue
                ) -> {
                    if (newValue == null) {
                        return;
                    }
                    switch (CURRENT_VIEW) {
                        case tools.Constants.PRODUIT:
                            ProduitsController pc =
                                ProduitsController.getInstance();
                            pc.searchProduit(newValue);
                            break;
                        case tools.Constants.STORAGE:
                            GoodstorageController gc =
                                GoodstorageController.getInstance();
                            gc.search(newValue);
                            break;
                        case tools.Constants.POS:
                            PosController poc = PosController.getInstance();
                            poc.search(newValue);
                            break;
                        case tools.Constants.CAISSES:
                            TresorerieController.getInstance().search(newValue);
                            break;
                        case tools.Constants.AGENTS:
                            AgentController.getInstance().search(newValue);
                            break;
                        case tools.Constants.IMMOBILISATIONS:
                            ImmobilisationController ic =
                                ImmobilisationController.getInstance();
                            if (ic != null) {
                                ic.search(newValue);
                            }
                            break;
                        default:
                            break;
                    }
                }
            );
        sseSync();
        sync(kazisafe);
    }

    private void subscribe() {
        // Executors.newSingleThreadExecutor()
        // .execute(() -> {
        // boolean network_on = pref.getBoolean(NetLoockup.NETWORK_STATUS,
        // NetLoockup.NETWORK_STATUS_DEFAULT);
        // if (network_on) {
        Executors.newCachedThreadPool().submit(() -> {
            Refresher ref = new Refresher();
            ref.setTarget("CHK_SUB");
            Util.sync(ref, "read", Tables.REFRESH);
        });
        // }
        // });
    }

    @FXML
    public void search(Event e) {
        if (!searchField.getText().isEmpty()) {
            if (searchField.getText() == null) {
                return;
            }
            if (CURRENT_VIEW.equals(tools.Constants.PRODUIT)) {
                ProduitsController pc = ProduitsController.getInstance();
                pc.searchProduit(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.STORAGE)) {
                GoodstorageController gc = GoodstorageController.getInstance();
                gc.search(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.POS)) {
                PosController poc = PosController.getInstance();
                poc.search(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.CAISSES)) {
                TresorerieController.getInstance().search(
                    searchField.getText()
                );
            } else if (CURRENT_VIEW.equals(tools.Constants.AGENTS)) {
                AgentController.getInstance().search(searchField.getText());
            } else if (CURRENT_VIEW.equals(tools.Constants.IMMOBILISATIONS)) {
                ImmobilisationController ic =
                    ImmobilisationController.getInstance();
                if (ic != null) {
                    ic.search(searchField.getText());
                }
            }
        }
    }

    private boolean go() {
        if (entrepiseId != null) {
            subscribe();
        }
        double max = pref.getDouble("sub", 0);
        String type = pref.get("type-sub", "Gold");
        if (type.equalsIgnoreCase("Gold")) {
            if (max > 0) {
                return true;
            } else {
                MainUI.notify(
                    null,
                    "Attention",
                    "Vous n'avez plus de crédit Kazisafe (record) dans votre compte, veuillez recharger votre compte",
                    5,
                    "warning"
                );
                return false;
            }
        } else {
            long d2 = System.currentTimeMillis();
            long jrs = BigDecimal.valueOf(max).setScale(9).longValue();
            long remaind = jrs - d2;
            long week = tools.Constants.MILLSECONDS_JOURN * 7;
            System.err.println("Remained " + remaind + " week " + week);

            if (remaind <= 0) {
                MainUI.notify(
                    null,
                    "Attention",
                    "Vous n'avez plus de crédit Kazisafe (record) dans votre compte, veuillez récharger votre compte",
                    5,
                    "warning"
                );
                return false;
            }
            if (Math.abs(remaind) <= week) {
                MainUI.notify(
                    null,
                    "Attention",
                    "Votre crédit Kazisafe expire bientôt, pensez à le renouveller",
                    5,
                    "warning"
                );
                return true;
            }
            return true;
        }
    }

    @FXML
    private void runhelp(Event e) {
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Desktop.getDesktop().open(
                            new File("./Kazisafe guide d'utilisation.pdf")
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (java.lang.IllegalArgumentException ex) {
                        MainUI.notify(
                            null,
                            "Erreur",
                            "Le fichier d'aide n'existe plus ou son nom \"Kazisafe guide d'utilisation.pdf\" d'origine a été modifié",
                            4,
                            "error"
                        );
                    }
                }
            }
        ).start();
    }

    @FXML
    private void installUpdate(Event e) {
        try {
            Alert alert = new Alert(
                Alert.AlertType.WARNING,
                "Cette action va fermer Kazisafe pour installer la mise a jour. Continuer ?",
                ButtonType.YES,
                ButtonType.CANCEL
            );
            alert.setTitle("Attention!");
            alert.setHeaderText(null);
            Optional<ButtonType> showAndWait = alert.showAndWait();
            if (showAndWait.isEmpty() || showAndWait.get() != ButtonType.YES) {
                return;
            }

            String path = downloadedUpdateFilePath;
            if (path == null || path.isBlank()) {
                path = localPath + File.separator + resolveUpdateFilename();
            }

            File downloaded = new File(path);
            if (!downloaded.exists()) {
                MainUI.notify(
                    null,
                    "Erreur",
                    "Le fichier de mise a jour est introuvable.",
                    4,
                    "error"
                );
                return;
            }

            if (PlatformUtil.isWindows()) {
                Runtime.getRuntime().exec(downloaded.getAbsolutePath());
                if (newModule != null && newModule.getVersion() != null) {
                    pref.put("ksf_version", newModule.getVersion());
                }
                exit(e);
            } else {
                try {
                    Desktop.getDesktop().open(downloaded.getParentFile());
                } catch (IOException ignored) {}
                MainUI.notify(
                    null,
                    "Info",
                    "Mise a jour telechargee. Ouvrez le fichier pour finaliser l'installation.",
                    6,
                    "info"
                );
            }
        } catch (IOException ex) {
            Logger.getLogger(MainuiController.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
    }

    @FXML
    private void closeUpdate(Event e) {
        update_pane.setVisible(false);
    }

    @FXML
    private void downloadUpdate(Event e) {
        if (newModule == null) {
            MainUI.notify(
                null,
                "Erreur",
                "Aucune mise a jour prete. Verifiez votre connexion et relancez la verification.",
                4,
                "error"
            );
            return;
        }

        String filename = resolveUpdateFilename();
        if (filename == null || filename.isBlank()) {
            MainUI.notify(
                null,
                "Erreur",
                "Nom du fichier de mise a jour invalide.",
                4,
                "error"
            );
            return;
        }

        Task<Void> downTask = new DownloadTask(filename, localPath);
        downTask
            .stateProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    downloadedUpdateFilePath =
                        localPath + File.separator + filename;
                    install_update_link.setVisible(true);
                    MainUI.notify(
                        null,
                        "Succes",
                        "Mise a jour telechargee avec succes.",
                        4,
                        "info"
                    );
                } else if (newValue == Worker.State.FAILED) {
                    Throwable ex = downTask.getException();
                    String msg =
                        ex != null
                            ? ex.getMessage()
                            : "Le telechargement a ete interrompu";
                    MainUI.notify(null, "Erreur", msg, 5, "error");
                }
            });

        download_update_pgb.setVisible(true);
        douwnload_update_pgi.setVisible(true);
        download_update_pgb.progressProperty().unbind();
        douwnload_update_pgi.progressProperty().unbind();
        download_update_pgb
            .progressProperty()
            .bind(downTask.progressProperty());
        douwnload_update_pgi
            .progressProperty()
            .bind(downTask.progressProperty());

        Thread kazi = new Thread(downTask, "UpdateDownload-Thread");
        kazi.setDaemon(true);
        kazi.start();
    }

    private String resolveUpdateFilename() {
        if (PlatformUtil.isWindows()) {
            if (
                newModule == null ||
                newModule.getNomModule() == null ||
                newModule.getNomModule().isBlank()
            ) {
                return "Kazisafex.exe";
            }
            return newModule.getNomModule();
        }
        if (PlatformUtil.isMac()) {
            return "Kazisafe-MacOS.zip";
        }
        return "Kazisafe-Linux.zip";
    }

    private void installTooltips() {
        Tooltip thome = new Tooltip();
        thome.setText(bundle.getString("xlbel.curen_tview"));
        thome.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(home, thome);

        Tooltip tclient = new Tooltip();
        tclient.setText(bundle.getString("xtooltip.fina_nce"));
        tclient.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(caisse, tclient);

        Tooltip timo = new Tooltip();
        timo.setText("Immobilisations");
        timo.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(immobilisation, timo);

        Tooltip tvente = new Tooltip();
        tvente.setText(bundle.getString("xtooltip.vente_recquis"));
        tvente.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(pos, tvente);

        Tooltip tproduction = new Tooltip();
        tproduction.setText("Production");
        tproduction.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(production, tproduction);

        Tooltip tdepot = new Tooltip();
        tdepot.setText(bundle.getString("xtooltip.entr_epot"));
        tdepot.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(stockage, tdepot);

        Tooltip tproducts = new Tooltip();
        tproducts.setText(bundle.getString("xtooltip.prodwi"));
        tproducts.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(products_gate, tproducts);

        Tooltip tagents = new Tooltip();
        tagents.setText(bundle.getString("xtooltip.ag_ents"));
        tagents.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(agents, tagents);

        Tooltip company = new Tooltip();
        company.setText(bundle.getString("xtooltip.com_panie"));
        company.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(compagnie, company);

        Tooltip trapport = new Tooltip();
        trapport.setText(bundle.getString("xtooltip.rap_poro"));
        trapport.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(rapport, trapport);

        Tooltip perf = new Tooltip();
        perf.setText(bundle.getString("xtooltip.param_ettre"));
        perf.setStyle(
            "-fx-font: normal bold 14 Langdon; " +
                "-fx-base: #EEEEEE; " +
                "-fx-text-fill: white;"
        );
        Tooltip.install(parametre, perf);
    }

    private void initGratienAgent() {
        try {
            String agentDir = MainUI.cPath("/Media/ia/gratien");
            File agentFile = new File(agentDir, "AGENT.md");
            if (!agentFile.exists()) {
                String content = """
                    # Contexte de l'application Kazisafex

                    ## Vue d'ensemble
                    Kazisafex est un logiciel de gestion commerciale et financiere destine aux PME en Republique Democratique du Congo.
                    Il gere les stocks, les ventes, les achats, la tresorerie, la production et les immobilisations.

                    ## Base de donnees
                    - Base integree: SQLite via H2 en mode embarque
                    - Base distante: MySQL/MariaDB sur le cloud
                    - ORM: Jakarta Persistence (JPA) avec EclipseLink
                    - L'application fonctionne avec la base locale meme hors-ligne et synchronise periodiquement

                    ## Modules principaux
                    1. **Produits**: Gestion des articles avec code-barres, categories, marques, modeles, couleurs, tailles
                    2. **Mesures**: Unites de mesure par produit (ex: Piece, Kg, Carton) avec quantite de contenu
                    3. **Fournisseurs**: Gestion des fournisseurs avec informations de contact et identification legale
                    4. **Livraisons/Approvisionnements**: Receptions de marchandises avec numero de piece, reference, lot, date d'expiration
                    5. **Requisitions**: Stocks par produit, lot, mesure avec prix d'achat et alerte de stock
                    6. **Prix de vente**: Prix par paliers de quantite (qmin-qmax) par devise et par unite
                    7. **Ventes**: Transactions de vente avec lignes de vente, clients, paiements
                    8. **Tresorerie**: Comptes tresorerie, operations, transactions, depenses
                    9. **Clients**: Gestion de la base clients
                    10. **Production**: Fabrication, matieres premieres, nomenclatures
                    11. **Immobilisations**: Gestion des actifs immobilises
                    12. **Inventaire**: Comptage physique des stocks
                    13. **Depots**: Gestion multi-depots et entreposage

                    ## Modeles de donnees cles
                    - **Produit** (uid, nomProduit, codebar, categorie, marque, modele, taille, couleur, image)
                    - **Mesure** (uid, description, quantContenu, produitId)
                    - **Livraison** (uid, reference, fournId, dateLivr, topay, payed, numPiece, region)
                    - **Recquisition** (uid, produitId, mesureId, quantite, coutAchat, dateExpiry, numlot, region, reference)
                    - **PrixDeVente** (uid, recquisitionId, mesureId, prixUnitaire, devise, qmin, qmax)
                    - **Vente** (uid, clientId, montantUsd, montantCdf, dateVente, reference)
                    - **LigneVente** (uid, reference, produitId, mesureId, quantite, prixUnit, montantUsd, montantCdf)
                    - **Fournisseur** (uid, nomFourn, adresse, telephone, identifiant, rccm, numImpot)
                    - **Client** (uid, nomClient, adresse, telephone)
                    - **Depense** (uid, motif, montantUsd, montantCdf, date, region, devise)
                    - **Stocker** (uid, livraisonId, produitId, mesureId, quantite, numlot, dateExpiry)
                    - **CompteTresor** (uid, nomCompte, solde, devise)
                    - **Traisorerie** (uid, tresorId, montantUsd, montantCdf, type, date, description)
                    - **Entreprise** (uid, nomEze, adresse, identifiant, telephone, devise)

                    ## Gestion des devises
                    - La devise principale est configuree dans les parametres (USD ou CDF)
                    - Le taux de change USD/CDF est stocke dans les preferences Java sous la cle "taux2change"
                    - Les montants sont stockes en USD et CDF separement (montantUsd, montantCdf)
                    - La classe utilitaire CurrencyConverter gere les conversions et les taux
                    - PrixDeVente a un champ devise pour stocker la devise du prix

                    ## Flux d'enregistrement d'une facture fournisseur (approvisionnement)
                    1. L'utilisateur soumet un InvoiceDraft avec les lignes de facture
                    2. Verifier que chaque ligne a un prix de vente (sinon demander)
                    3. Creer/retrouver les produits et mesures
                    4. Creer/retrouver le fournisseur et la livraison
                    5. Creer les requisitions et les prix de vente
                    6. Mettre a jour le stock via rectifyStock

                    ## Synchronisation HTTP
                    - Les donnees sont synchronisees avec le cloud via Retrofit2
                    - Chaque entite a une API REST correspondante
                    - Les WebSockets permettent la synchronisation en temps reel
                    - L'application peut fonctionner hors-ligne

                    ## Particularites
                    - Le systeme utilise des UID (String) comme cles primaires generees via DataId.generate()
                    - Les dates sont stockees en LocalDateTime ou LocalDate
                    - Le fichier de configuration utilisateur est dans les Preferences Java
                    - Les entrees de stock utilisent le FIFO
                    - Les prix de vente sont par paliers de quantite avec des bornes qmin et qmax
                    - La rupture de stock est determinee par stock <= stockAlert ou stock = 0 (via stock_agregate.final_quantity)
                    - Les produits expires/perimes sont interroges via stock_agregate.date_expiration avec RecquisitionDelegate.showExpiredAtInterval()

                    ## Nomenclature des produits
                    Chaque produit a 5 champs descriptifs : `nomProduit`, `marque`, `modele`, `taille`, `couleur`.
                    Dans l'interface utilisateur, l'affichage combine toujours :
                    `nomProduit + " " + marque + " " + modele + " " + taille + " " + couleur`

                    Exemples de decomposition :
                    - "Paracetamole ces 500mg" -> nomProduit="Paracetamole", modele="ces", taille="500mg"
                    - "Lux Soft Touch 200ml" -> nomProduit="Lux", marque="Soft Touch", taille="200ml"
                    - "Huile Tchiolo 1L" -> nomProduit="Huile Tchiolo", taille="1L"
                    - "Savon", "Riz 25kg", "Sucre 1kg" -> nomProduit=tout, taille=dernier mot si c'est une quantite

                    **Regles de decomposition pour l'IA :**
                    1. Le premier mot ou groupe de mots significatif est le `nomProduit`.
                    2. Les mots comme "ces", "extra", "plus", "bio", "premium", "classic" sont souvent le `modele`.
                    3. Les indications de taille/poids/volume/concentration (500mg, 1L, 25kg, 200ml, 5%) sont la `taille`.
                    4. Les marques connues (Lux, Coca, BIC, Tchiolo, Milka, etc.) sont la `marque`.
                    5. Si un champ n'est pas identifiable, le laisser vide ("").

                    **Format de passage aux outils :**
                    Pour que le systeme Java puisse decomposer correctement, utilisez le point-virgule (`;`)
                    comme separateur dans le champ `productName` ou `nomProduit` :
                    `"nomProduit;marque;modele"`
                    Exemple : `"Paracetamole;ces;500mg"` au lieu de `"Paracetamole ces 500mg"`
                    Exemple : `"Lux;Soft Touch;200ml"` au lieu de `"Lux Soft Touch 200ml"`

                    Quand vous creez un produit via `createProductsAndAskMeasures` ou via un workflow facture,
                    passez le nom avec des point-virgules : `productName: "Paracetamole;ces;500mg"`.
                    Les champs `taille` et `couleur` doivent etre passes explicitement dans le JSON
                    car ils ne sont pas extraits automatiquement des factures.

                    **Desambiguisation des produits similaires :**
                    Si l'utilisateur a des produits proches (ex: "Paracetamol ces 500mg (BTE/10)" et "Paracetamol ces 500mg"),
                    et que la facture ne precise pas exactement lequel, utilisez `findProductCandidates(nom)`
                    pour voir les produits existants qui correspondent, puis demandez a l'utilisateur
                    lequel utiliser. Ensuite, utilisez `assignProductToLine(workflowId, lineIndex, productUid)`
                    pour assigner le bon produit a la ligne de facture avant de continuer le workflow.
                    Note: desormais le workflow facture detecte automatiquement les ambiguites et
                    demande a l'utilisateur quel produit choisir avant d'enregistrer.

                    **Verification de mise a jour :**
                    Si l'utilisateur demande quelle est la derniere version de Kazisafe, ou ce qui a change,
                    utilisez `checkLatestVersion()` pour interroger le serveur de mise a jour.
                    Vous pouvez expliquer a l'utilisateur la difference entre sa version actuelle et la
                    derniere disponible. Referez-le a https://endeleya.com/products/kazisafe pour plus
                    d'informations sur les nouveautes.

                    ## Specification des prix de vente
                    Lorsque l'utilisateur doit specifier les prix de vente, deux formats sont acceptes :

                    1. **Format structure** (existant) : `numero, qmin, qmax, prix, devise`
                       Exemple : `1, 1, 999999, 25, USD`
                    2. **Format langage naturel** (nouveau) : phrases explicites
                       Exemples : "le premier produit c'est 5 dollars", "le produit 1 c'est 5USD",
                       "premier = 25 USD", "1 = 5 dollars"
                       - Dans ce cas, qmin=0.001 et qmax=999999 par defaut
                       - Si la devise n'est pas precisee, la devise principale configuree est utilisee

                    ## Outils de reporting stock
                    - `listLowStockProducts()` : liste les produits en rupture (stock <= alerte ou stock = 0)
                    - `exportLowStockProductsPdf()` : genere un PDF des produits en rupture et l'ouvre
                    - `listExpiringProducts(months)` : liste les produits expires (months=0) ou expirant dans N mois
                    - `exportExpiringProductsPdf(months)` : genere un PDF des produits expires et l'ouvre

                    ## Nouveautes et mises a jour
                    - `checkLatestVersion()` : verifie la derniere version disponible sur le serveur
                    - `fetchKazisafePage()` : consulte la page officielle https://endeleya.com/products/kazisafe
                      pour lire les nouvelles fonctionnalites et la documentation.
                      Utilisez cette commande quand l'utilisateur demande "quoi de neuf", "nouveautes",
                      "mise a jour", "changelog", "nouvelle version" ou "qu'est-ce qui a change".

                    ## Commandes speciales
                    - `/kanuni <instruction>` : enregistre une instruction personnalisee dans USER.md.
                      Cette instruction sera incluse dans le contexte de toutes les conversations futures.
                      Exemple : `/kanuni Je prefere que les produits pharmaceutiques soient prefixes par MED-`
                    """;
                Files.writeString(
                    agentFile.toPath(),
                    content,
                    java.nio.charset.StandardCharsets.UTF_8
                );
                System.out.println(
                    "Fichier AGENT.md cree: " + agentFile.getAbsolutePath()
                );
            }
        } catch (Exception e) {
            System.err.println(
                "Erreur lors de l'initialisation du contexte Gratien: " +
                    e.getMessage()
            );
        }
    }

    private void checkForUpdates() {
        try {
            tools.UpdateManager um = new tools.UpdateManager(kazisafe);
            um.checkForUpdate(
                new tools.UpdateManager.UpdateListener() {
                    @Override
                    public void onUpdateAvailable(data.Module module) {
                        Platform.runLater(() -> {
                            newModule = module;
                            update_pane.setVisible(true);
                            MainUI.notify(
                                null,
                                "Mise a jour",
                                "Version " +
                                    module.getVersion() +
                                    " disponible. Cliquez sur l'icone de telechargement.",
                                2,
                                "info"
                            );
                        });
                    }

                    @Override
                    public void onUpToDate() {}

                    @Override
                    public void onError(String message) {
                        System.err.println(
                            "Verification de mise a jour: " + message
                        );
                    }
                }
            );
        } catch (Exception e) {
            System.err.println("Erreur de mise a jour: " + e.getMessage());
        }
    }

    private void synchronizeWithServer(MouseEvent event) {
        if (isConnected) {
            SyncEngine.getInstance().syncWithHttpProtocol(
                label_status,
                kazisafe
            );
        } else {
            System.out.println("Pas de connection");
        }
    }

    @FXML
    private void switchToProduction(MouseEvent event) {
        if (go()) {
            switchSimpleScreens(
                tools.Constants.PRODUCTION_VIEW,
                Constants.PRODUCTION,
                "Production",
                "production-line.png"
            );
        }
    }

    @FXML
    private void callAssistantDialog(MouseEvent event) {
        openAiPanel(event);
        //        MainUI.showAssistantIa(tools.Constants.ASSISTANT_DLG, 663, 685, this.entreprisex,
        //                this.user_connected.getText());
    }

    private static class DownloadTask extends Task<Void> {

        String url;
        String localPath;

        public DownloadTask(String url, String localPath) {
            this.url = url;
            this.localPath = localPath;
        }

        @Override
        protected Void call() throws Exception {
            if (url == null || url.isBlank()) {
                throw new IllegalStateException(
                    "Nom de fichier de mise a jour invalide"
                );
            }

            Files.createDirectories(Paths.get(localPath));

            String encodedName = URLEncoder.encode(
                this.url,
                StandardCharsets.UTF_8
            ).replace("+", "%20");
            URLConnection connexion = new URL(
                "https://cloud.kazisafe.com/download/" + encodedName
            ).openConnection();
            long taille = connexion.getContentLengthLong();

            try (
                InputStream is = connexion.getInputStream();
                OutputStream os = Files.newOutputStream(
                    Paths.get(localPath, url)
                )
            ) {
                long nread = 0L;
                byte[] buffer = new byte[8192];
                int n;
                while ((n = is.read(buffer)) > 0) {
                    os.write(buffer, 0, n);
                    nread += n;
                    if (taille > 0) {
                        updateProgress(nread, taille);
                    }
                }
            }

            if (taille <= 0) {
                updateProgress(1, 1);
            }
            return null;
        }

        @Override
        protected void failed() {
            super.failed();
        }

        @Override
        protected void succeeded() {
            super.succeeded();
        }
    }

    private boolean isDeadlockException(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (
                cause
                    .getMessage()
                    .contains("Deadlock found when trying to get lock")
            ) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private boolean isEntityExistException(Exception e) {
        Throwable x = e;
        while (x != null) {
            if (x instanceof EntityExistsException) {
                return true;
            }
            x = x.getCause();
        }
        return false;
    }

    private boolean isEntityNotFoundException(Exception e) {
        Throwable x = e;
        while (x != null) {
            if (x instanceof jakarta.persistence.EntityNotFoundException) {
                return true;
            }
            x = x.getCause();
        }
        return false;
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private boolean isIllegalStateException(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof IllegalStateException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private String extractId(Object obj) {
        System.out.println("ID OBJKT " + obj);

        String s = String.valueOf(obj);
        String result;
        if (s.contains("{") || s.contains("}")) {
            result = s
                .replace("{", "")
                .replace("}", "")
                .split(",")[0]
                .split("=")[1];
        } else {
            result = s;
        }
        return result;
    }

    @FXML
    public void closeAiPanel(Event e) {
        main_spliter.getItems().removeAll(ai_panel);
        main_spliter.setDividerPosition(0, 1);
    }

    public void openAiPanel(Event e) {
        if (main_spliter.getItems().contains(ai_panel)) {
            return;
        }
        main_spliter.getItems().add(ai_panel);
        main_spliter.setDividerPosition(0, 0.9);
    }

    @FXML
    private void sendMessage(Event event) {
        this.token = pref.get("token", null);
        sendText();
    }

    private String urlWithMessage(String message) {
        String q = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String url = KazisafeServiceFactory.BASE_URL + "ia/ask?q=" + q;
        return url;
    }

    private void sendText() {
        String question =
            txt_input_iaquery.getText() == null
                ? ""
                : txt_input_iaquery.getText().trim();
        List<File> attachments = new ArrayList<>(aiAttachments);
        if (question.isBlank() && attachments.isEmpty()) {
            MainUI.notify(
                null,
                "",
                "Veuillez envoyer un message valide",
                4,
                "error"
            );
            return;
        }
        // Afficher immédiatement le message de l'utilisateur dans le chat.
        Platform.runLater(() -> {
            webE.executeScript(
                "appendUser(" +
                    escapeForJS(
                        question + userAttachmentPreviewMarkdown(attachments)
                    ) +
                    ")"
            );
            txt_input_iaquery.clear();
            aiAttachments.clear();
            refreshAttachmentLabel();
        });

        // Si Gratien est déjà en train de traiter, on met en file d'attente.
        if (gratienIsProcessing.get()) {
            gratienMessageQueue.offer(new PendingUserMessage(question, attachments));
            Platform.runLater(() ->
                webE.executeScript(
                    "showBotProcess(" +
                        escapeForJS(
                            "\u23f3 Message reçu. Gratien finit sa réponse actuelle et traitera votre message ensuite."
                        ) +
                        ")"
                )
            );
            return;
        }
        dispatchToGratien(question, attachments);
    }

    /**
     * Envoie effectivement un message à Gratien et, à la fin du traitement,
     * dépile le prochain message en attente s'il en existe un.
     */
    private void dispatchToGratien(String question, List<File> attachments) {
        gratienIsProcessing.set(true);
        new Thread(() -> {
            gratienAssistantClient.stream(
                question,
                attachments,
                getEntreprisex().getNomEntreprise(),
                new GratienAssistantClient.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        Platform.runLater(() -> {
                            if (!handleSecureMysqlPasswordRequest(token)) {
                                webE.executeScript(
                                    "appendBotAnswer(" +
                                        escapeForJS(token) +
                                        ")"
                                );
                            }
                        });
                    }

                    @Override
                    public void onProcess(String message) {
                        Platform.runLater(() -> {
                            if (!handleSecureMysqlPasswordRequest(message)) {
                                webE.executeScript(
                                    "showBotProcess(" +
                                        escapeForJS(message) +
                                        ")"
                                );
                            }
                        });
                    }

                    @Override
                    public void onComplete() {
                        Platform.runLater(() ->
                            webE.executeScript("endBotMessage()")
                        );
                        afterGratienFinished();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Platform.runLater(() -> {
                            webE.executeScript(
                                "appendBotPartial(" +
                                    escapeForJS(
                                        "Impossible de joindre Gratien via Ollama: " +
                                            throwableMessage(error)
                                    ) +
                                    ")"
                            );
                            webE.executeScript("endBotMessage()");
                        });
                        afterGratienFinished();
                    }
                }
            );
        }, "Gratien-stream-thread").start();
    }

    /**
     * Appelé après chaque fin de traitement (succès ou erreur).
     * Libère le verrou et traite le prochain message en file d'attente.
     */
    private void afterGratienFinished() {
        gratienIsProcessing.set(false);
        PendingUserMessage next = gratienMessageQueue.poll();
        if (next != null) {
            // Afficher le message en attente dans le chat avant de le traiter.
            Platform.runLater(() ->
                webE.executeScript(
                    "appendUser(" +
                        escapeForJS(
                            next.question() +
                                userAttachmentPreviewMarkdown(next.attachments())
                        ) +
                        ")"
                )
            );
            dispatchToGratien(next.question(), next.attachments());
        }
    }

    /** Encapsule un message utilisateur mis en file d'attente. */
    private record PendingUserMessage(String question, List<File> attachments) {}

    private boolean handleSecureMysqlPasswordRequest(String message) {
        SecureMysqlRequest request = SecureMysqlRequest.from(message);
        if (request == null) {
            return false;
        }
        if (!handledSecureMysqlRequests.add(request.key())) {
            return true;
        }
        webE.executeScript(
            "showBotProcess(" +
                escapeForJS(
                    "Saisie sécurisée du mot de passe root MySQL requise."
                ) +
                ")"
        );
        Optional<String> password = requestMysqlRootPassword(request);
        if (password.isEmpty() || password.get().isBlank()) {
            webE.executeScript(
                "appendBotAnswer(" +
                    escapeForJS(
                        "Configuration annulée: aucun mot de passe root MySQL n'a été saisi."
                    ) +
                    ")"
            );
            webE.executeScript("endBotMessage()");
            return true;
        }
        runSecureMysqlAction(request, password.get());
        return true;
    }

    private Optional<String> requestMysqlRootPassword(
        SecureMysqlRequest request
    ) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mot de passe MySQL");
        dialog.setHeaderText("Saisie sécurisée requise");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe root MySQL");
        VBox content = new VBox(8);
        content
            .getChildren()
            .addAll(
                new Label(
                    "Replica: " +
                        request.replicaHost() +
                        ":" +
                        request.replicaPort()
                ),
                new Label("Le mot de passe ne sera pas affiché dans le chat."),
                passwordField
            );
        dialog.getDialogPane().setContent(content);
        dialog
            .getDialogPane()
            .getButtonTypes()
            .addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button ->
            button == ButtonType.OK ? passwordField.getText() : null
        );
        Platform.runLater(passwordField::requestFocus);
        return dialog.showAndWait();
    }

    private void runSecureMysqlAction(
        SecureMysqlRequest request,
        String password
    ) {
        new Thread(() -> {
            GratienTools tools = AiAgents.getInstance().getGratienTools();
            String token = tools.registerMysqlRootPasswordToken(password);
            String result;
            if ("test".equalsIgnoreCase(request.action())) {
                result = tools.testMysqlReplicaStatus(
                    request.replicaHost(),
                    request.replicaPort(),
                    token
                );
            } else {
                result = tools.executeMysqlReplicaConfiguration(
                    request.planId(),
                    request.replicaHost(),
                    request.replicaPort(),
                    token
                );
            }
            Platform.runLater(() -> {
                webE.executeScript(
                    "appendBotAnswer(" + escapeForJS(result) + ")"
                );
                webE.executeScript("endBotMessage()");
            });
        }, "Gratien-secure-mysql-action").start();
    }

    private record SecureMysqlRequest(
        String action,
        String planId,
        String replicaHost,
        int replicaPort
    ) {
        private static SecureMysqlRequest from(String message) {
            if (message == null) {
                return null;
            }
            int index = message.indexOf(
                GratienTools.SECURE_MYSQL_PASSWORD_REQUEST
            );
            if (index < 0) {
                return null;
            }
            String payload = message.substring(index).split("\\R", 2)[0];
            String action = "";
            String planId = "";
            String replicaHost = "";
            int replicaPort = 3306;
            for (String part : payload.split("\\|")) {
                int equal = part.indexOf('=');
                if (equal <= 0) {
                    continue;
                }
                String key = part.substring(0, equal).trim();
                String value = part.substring(equal + 1).trim();
                switch (key) {
                    case "action" -> action = value;
                    case "planId" -> planId = value;
                    case "replicaHost" -> replicaHost = value;
                    case "replicaPort" -> replicaPort = parsePort(value);
                    default -> {
                    }
                }
            }
            if (replicaHost.isBlank()) {
                return null;
            }
            return new SecureMysqlRequest(
                action,
                planId,
                replicaHost,
                replicaPort
            );
        }

        private static int parsePort(String value) {
            try {
                int parsed = Integer.parseInt(value);
                return parsed <= 0 ? 3306 : parsed;
            } catch (NumberFormatException ex) {
                return 3306;
            }
        }

        private String key() {
            return (
                action + "|" + planId + "|" + replicaHost + "|" + replicaPort
            );
        }
    }

    private File compress(File nonCompressed) {
        long maxsize = 700 * 1024;
        try {
            byte[] fichbyte = processAndCompress(nonCompressed, maxsize);
            String path = MainUI.cPath("/Media/ia");
            Path pathf = Paths.get(path + File.separator + "compressedImg.jpg");
            return Files.write(pathf, fichbyte).toAbsolutePath().toFile();
        } catch (IOException e) {
            return null;
        }
    }

    private void addAttachemnts() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Joindre un fichier à Gratien");
        List<File> selectedFiles = chooser.showOpenMultipleDialog(
            ia_webvu_chat.getScene().getWindow()
        );
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }
        for (File selectedFile : selectedFiles) {
            aiAttachments.add(compress(selectedFile));
        }

        //        aiAttachments.addAll(selectedFiles);
        refreshAttachmentLabel();
    }

    private void refreshAttachmentLabel() {
        if (txt_selected_attachment == null) {
            return;
        }
        if (aiAttachments.isEmpty()) {
            txt_selected_attachment.setText("Aucun attache");
            clearAttachmentPreviews();
            return;
        }
        txt_selected_attachment.setText(
            aiAttachments.size() + " fichier(s) joint(s)"
        );
        refreshAttachmentPreviews();
    }

    private void refreshAttachmentPreviews() {
        if (box_ai_attachment_previews == null) {
            return;
        }
        clearAttachmentPreviews();
        int limit = Math.min(aiAttachments.size(), 8);
        for (int i = 0; i < limit; i++) {
            ImageView preview = createAttachmentPreview(aiAttachments.get(i));
            if (preview != null) {
                box_ai_attachment_previews.getChildren().add(preview);
            }
        }
        if (aiAttachments.size() > limit) {
            Label more = new Label("+" + (aiAttachments.size() - limit));
            more.setStyle(
                "-fx-text-fill: #72819c; -fx-font-size: 11px; -fx-padding: 0 4 0 2;"
            );
            box_ai_attachment_previews.getChildren().add(more);
        }
        box_ai_attachment_previews.setVisible(
            !box_ai_attachment_previews.getChildren().isEmpty()
        );
    }

    private ImageView createAttachmentPreview(File file) {
        if (file == null) {
            return null;
        }
        try {
            Image image = isImageAttachment(file)
                ? new Image(file.toURI().toString(), 30, 30, true, true)
                : new Image(
                      getClass().getResourceAsStream(attachmentIcon(file)),
                      30,
                      30,
                      true,
                      true
                  );
            ImageView preview = new ImageView(image);
            preview.setFitHeight(30);
            preview.setFitWidth(30);
            preview.setPreserveRatio(true);
            preview.setPickOnBounds(true);
            preview.setCursor(Cursor.HAND);
            preview.setOnMouseClicked(event -> {
                aiAttachments.remove(file);
                refreshAttachmentLabel();
            });
            Tooltip.install(
                preview,
                new Tooltip(
                    file.getName() + "\nCliquer pour retirer ce fichier"
                )
            );
            return preview;
        } catch (Exception ex) {
            return null;
        }
    }

    private void clearAttachmentPreviews() {
        if (box_ai_attachment_previews != null) {
            box_ai_attachment_previews.getChildren().clear();
            box_ai_attachment_previews.setVisible(false);
        }
    }

    private boolean isImageAttachment(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return (
            name.endsWith(".png") ||
            name.endsWith(".jpg") ||
            name.endsWith(".jpeg") ||
            name.endsWith(".gif") ||
            name.endsWith(".bmp") ||
            name.endsWith(".webp")
        );
    }

    private String userAttachmentPreviewMarkdown(List<File> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(
            "\n\n|Fichiers joints|\n|---|\n"
        );
        for (File file : attachments) {
            if (file == null) {
                continue;
            }
            String imageMarkdown = isImageAttachment(file)
                ? attachmentImageMarkdown(file)
                : attachmentIconMarkdown(file);
            builder
                .append("|")
                .append(imageMarkdown)
                .append(" ")
                .append(markdownTableCell(file.getName()))
                .append("|\n");
        }
        return builder.toString();
    }

    private String attachmentImageMarkdown(File file) {
        try {
            BufferedImage source = ImageIO.read(file);
            if (source == null) {
                return attachmentIconMarkdown(file);
            }
            BufferedImage thumb = thumbnail(source, 220, 160);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ImageIO.write(thumb, "png", out);
                return dataImageMarkdown(file.getName(), out.toByteArray());
            }
        } catch (IOException ex) {
            return attachmentIconMarkdown(file);
        }
    }

    private BufferedImage thumbnail(
        BufferedImage source,
        int maxWidth,
        int maxHeight
    ) {
        double scale = Math.min(
            (double) maxWidth / source.getWidth(),
            (double) maxHeight / source.getHeight()
        );
        scale = Math.min(1d, scale);
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage target = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            );
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private String attachmentIconMarkdown(File file) {
        try (
            InputStream input = getClass().getResourceAsStream(
                attachmentIcon(file)
            )
        ) {
            if (input == null) {
                return "`FICHIER`";
            }
            return dataImageMarkdown(file.getName(), input.readAllBytes());
        } catch (IOException ex) {
            return "`FICHIER`";
        }
    }

    private String dataImageMarkdown(String alt, byte[] bytes) {
        return (
            "![" +
            markdownAlt(alt) +
            "](data:image/png;base64," +
            Base64.getEncoder().encodeToString(bytes) +
            ")"
        );
    }

    private String markdownAlt(String value) {
        return value == null
            ? "fichier"
            : value.replace("]", "").replace("[", "").replace("\n", " ");
    }

    private String markdownTableCell(String value) {
        return value == null
            ? "-"
            : value
                  .replace("|", "/")
                  .replace("\n", " ")
                  .replace("\r", " ")
                  .trim();
    }

    private String attachmentIcon(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) {
            return "/icons/download-pdf.png";
        }
        if (
            name.endsWith(".xls") ||
            name.endsWith(".xlsx") ||
            name.endsWith(".csv")
        ) {
            return "/icons/xls(4).png";
        }
        return "/icons/effacer-le-fichier.png";
    }

    private void handleAiInputKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
            event.consume();
            txt_input_iaquery.insertText(
                txt_input_iaquery.getCaretPosition(),
                System.lineSeparator()
            );
        } else if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            sendMessage(event);
        } else if (event.getCode() == KeyCode.TAB) {
            String text = txt_input_iaquery.getText();
            int pos = txt_input_iaquery.getCaretPosition();
            int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
            String linePrefix = text.substring(lineStart, pos);
            if (linePrefix.equals("/") || linePrefix.startsWith("/")) {
                event.consume();
                showCommandCompletion(linePrefix);
            }
        }
    }

    private void showCommandCompletion(String prefix) {
        List<String> commands = List.of(
            "/kanuni <instruction> - Ajouter une instruction personnalisee"
        );
        ContextMenu menu = new ContextMenu();
        for (String cmd : commands) {
            if (
                cmd
                    .toLowerCase(Locale.ROOT)
                    .startsWith(prefix.toLowerCase(Locale.ROOT))
            ) {
                MenuItem item = new MenuItem(cmd);
                item.setOnAction(e -> {
                    int caretPos = txt_input_iaquery.getCaretPosition();
                    String text = txt_input_iaquery.getText();
                    int lineStart = text.lastIndexOf('\n', caretPos - 1) + 1;
                    String rest = text.substring(caretPos);
                    txt_input_iaquery.setText(
                        text.substring(0, lineStart) + cmd + rest
                    );
                    txt_input_iaquery.positionCaret(lineStart + cmd.length());
                });
                menu.getItems().add(item);
            }
        }
        if (!menu.getItems().isEmpty()) {
            menu.show(txt_input_iaquery, Side.BOTTOM, 0, 0);
        }
    }
}
