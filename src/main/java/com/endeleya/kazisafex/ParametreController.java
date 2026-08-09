package com.endeleya.kazisafex;

import com.endeleya.kazisafex.Kazisafex;
import com.fazecast.jSerialComm.SerialPort;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import tools.CurrencyConverter;
import tools.DataCache;
import tools.MainUI;
import tools.SyncEngine;
import services.SyncOutboxService;
import services.sync.DownsyncCatchupService;
import tools.Constants;

/**
 *
 * @author endeleya
 */
public class ParametreController implements Initializable {

    @FXML
    private ToggleButton tgbtn_session;

    @FXML
    private TextField tf_taux_de_change;

    @FXML
    private TextField tf_taux_imposition_resultat;

    @FXML
    private ToggleButton tgbtn_sync;

    @FXML
    private ToggleButton tgbtn_print_bill;

    @FXML
    private ToggleButton tgbtn_dark_theme;

    ImageView image_sync;

    @FXML
    private TextField message4client;

    @FXML
    private ComboBox<String> cbx_counter;

    @FXML
    private ComboBox<String> cbx_main_cur;

    @FXML
    ComboBox<Integer> cbx_frequence;

    @FXML
    Spinner<Integer> spinner;

    @FXML
    ComboBox<String> cbx_param_font_size_head;

    @FXML
    ComboBox<String> cbx_param_font_size_body;

    @FXML
    ComboBox<String> cbx_param_font_size_footer;

    @FXML
    private TextField text_msg;

    @FXML
    private RadioButton ppps;

    @FXML
    private RadioButton fifo;

    @FXML
    private RadioButton lifo;

    @FXML
    private CheckBox avertiBill;

    ToggleGroup methodGroup;
    ToggleGroup mode_stock;
    ToggleGroup mode_printer;
    ResourceBundle bundle;
    String mainCur;
    Preferences pref;
    private static ParametreController instance;

    @FXML
    private CheckBox print_mark;

    @FXML
    private CheckBox print_modele;

    @FXML
    private CheckBox print_tail;

    @FXML
    private CheckBox print_total_usd;

    @FXML
    private ComboBox<SerialPort> cbx_display_ports;

    @FXML
    private RadioButton rb_emarque;

    @FXML
    private RadioButton rb_serveur;

    @FXML
    private TextField tf_ip_serveur;

    @FXML
    private TextField tf_port_serveur;

    @FXML
    private TextField tf_mysql_user;

    @FXML
    private PasswordField pf_mysql_password;

    @FXML
    private CheckBox cb_mysql_ssl;

    @FXML
    private RadioButton mm58;

    @FXML
    private RadioButton mm72;

    @FXML
    private RadioButton mm80;

    @FXML
    private Hyperlink logFileHyperlink;

    public void onCachedPageShown() {
        // No longer needed — views are rebuilt fresh each time.
    }

    public ParametreController() {
        instance = this;
    }

    public static ParametreController getInstance() {
        return instance;
    }

    @FXML
    public void configTaux(Event evt) {
        if (this.tf_taux_de_change.getText().isEmpty()) {
            MainUI.notify(
                null,
                (String) this.bundle.getString("error"),
                (String) this.bundle.getString("inputaux"),
                (long) 4L,
                (String) "error"
            );
            return;
        }
        try {
            String selectedCurrency = CurrencyConverter.normalize(
                this.cbx_main_cur.getValue()
            );
            double rate = Double.parseDouble(this.tf_taux_de_change.getText());
            if (
                CurrencyConverter.USD.equals(selectedCurrency) ||
                CurrencyConverter.CDF.equals(selectedCurrency)
            ) {
                CurrencyConverter.saveLegacyCdfRate(rate);
            } else {
                CurrencyConverter.saveRateFromUsd(selectedCurrency, rate);
                this.cbx_main_cur.setItems(CurrencyConverter.fxCurrencies());
                this.cbx_main_cur.setValue(selectedCurrency);
            }
            MainUI.notify(
                null,
                (String) "Info",
                (String) this.bundle.getString("ratesaved"),
                (long) 4L,
                (String) "Info"
            );
        } catch (NumberFormatException e) {
            MainUI.notify(
                null,
                (String) this.bundle.getString("error"),
                (String) this.bundle.getString("rateerror"),
                (long) 4L,
                (String) "error"
            );
        }
    }

    @FXML
    public void configTauxImpositionResultat(Event evt) {
        if (this.tf_taux_imposition_resultat.getText().isEmpty()) {
            MainUI.notify(
                null,
                (String) this.bundle.getString("error"),
                (String) "Veuillez entrer le taux d'imposition du resultat.",
                (long) 4L,
                (String) "error"
            );
            return;
        }
        try {
            double rate = Double.parseDouble(
                this.tf_taux_imposition_resultat.getText().replace(",", ".")
            );
            if (rate < 0d || rate > 100d) {
                MainUI.notify(
                    null,
                    (String) this.bundle.getString("error"),
                    (String) "Le taux doit etre compris entre 0 et 100%.",
                    (long) 4L,
                    (String) "error"
                );
                return;
            }
            this.pref.putDouble("taux_imposition_resultat", rate);
            MainUI.notify(
                null,
                (String) this.bundle.getString("success"),
                (String) "Taux d'imposition du resultat enregistre.",
                (long) 4L,
                (String) "info"
            );
        } catch (NumberFormatException e) {
            MainUI.notify(
                null,
                (String) this.bundle.getString("error"),
                (String) "Taux d'imposition invalide.",
                (long) 4L,
                (String) "error"
            );
        }
    }

    @FXML
    public void configPrint(Event evt) {
        ToggleButton tbtn = (ToggleButton) evt.getSource();
        if (tbtn.getText().equals(this.bundle.getString("xbtn.no"))) {
            tbtn.setText(this.bundle.getString("xbtn.yes"));
            this.pref.putBoolean("print", true);
        } else {
            tbtn.setText(this.bundle.getString("xbtn.no"));
            this.pref.putBoolean("print", false);
        }
    }

    @FXML
    public void configSync(Event evt) {
        ToggleButton tbtn = (ToggleButton) evt.getSource();
        if (tbtn.getText().equals(this.bundle.getString("xbtn.no"))) {
            tbtn.setText(this.bundle.getString("xbtn.yes"));
            this.pref.putBoolean("sync", true);
            this.cbx_frequence.setDisable(false);
        } else {
            tbtn.setText(this.bundle.getString("xbtn.no"));
            this.pref.putBoolean("sync", false);
            this.cbx_frequence.setDisable(true);
        }
    }

    public void configFreqSync(Event evt) {
        if (this.cbx_frequence.getValue() == null) return;
        this.pref.putInt("sync-freq", (Integer) this.cbx_frequence.getValue());
        Executors.newSingleThreadExecutor().execute(() ->
            MainUI.notify(
                null,
                (String) "Info",
                (String) this.bundle.getString("synconfigsaved"),
                (long) 4L,
                (String) "Info"
            )
        );
    }

    @FXML
    public void configSession(Event evt) {
        ToggleButton tbtn = (ToggleButton) evt.getSource();
        if (tbtn.getText().equals(this.bundle.getString("xbtn.no"))) {
            tbtn.setText(this.bundle.getString("xbtn.yes"));
            this.pref.putBoolean("session", true);
        } else if (tbtn.getText().equals(this.bundle.getString("xbtn.yes"))) {
            tbtn.setText(this.bundle.getString("xbtn.no"));
            this.pref.putBoolean("session", false);
        }
    }

    @FXML
    public void configDarkTheme(Event evt) {
        ToggleButton tbtn;
        boolean darkEnabled = (tbtn = (ToggleButton) evt.getSource())
            .getText()
            .equals(this.bundle.getString("xbtn.no"));
        tbtn.setText(
            darkEnabled
                ? this.bundle.getString("xbtn.yes")
                : this.bundle.getString("xbtn.no")
        );
        tbtn.setSelected(darkEnabled);
        this.pref.putBoolean("dark_theme_enabled", darkEnabled);
        Node source = (Node) evt.getSource();
        if (source != null && source.getScene() != null) {
            Kazisafex.applyTheme((Scene) source.getScene());
        }
        if (MainUI.mainStage != null && MainUI.mainStage.getScene() != null) {
            Kazisafex.applyTheme((Scene) MainUI.mainStage.getScene());
        }
    }

    @FXML
    public void setMessage4CustomersOnBill(Event evt) {
        this.pref.put("mesc", this.message4client.getText());
        MainUI.notify(
            null,
            (String) this.bundle.getString("success"),
            (String) this.bundle.getString("msgconf"),
            (long) 4L,
            (String) "info"
        );
    }

    public void init() {
        boolean session = this.pref.getBoolean("session", false);
        boolean darkTheme = this.pref.getBoolean("dark_theme_enabled", false);
        boolean sync = this.pref.getBoolean("sync", true);
        boolean print = this.pref.getBoolean("print", true);
        boolean embd = this.pref.getBoolean("embedded_db", true);
        this.mainCur = DataCache.getOrLoad("param.mainCurrency", CurrencyConverter::mainCurrency);
        this.cbx_main_cur.setItems(
            DataCache.getOrLoad("param.fxCurrencies", CurrencyConverter::fxCurrencies)
        );
        this.cbx_main_cur.setEditable(true);
        this.cbx_main_cur.setValue(this.mainCur);
        this.tf_taux_de_change.setText(
            String.valueOf(DataCache.getOrLoad("param.legacyCdfRate", CurrencyConverter::legacyCdfRate))
        );
        this.tf_taux_imposition_resultat.setText(
            String.valueOf(this.pref.getDouble("taux_imposition_resultat", 2d))
        );
        int w = this.pref.getInt("print-lines-dashcount", 48);
        switch (w) {
            case 48: {
                this.mm80.setSelected(true);
                break;
            }
            case 42: {
                this.mm72.setSelected(true);
                break;
            }
            case 30: {
                this.mm58.setSelected(true);
                break;
            }
        }
        this.tgbtn_print_bill.setSelected(print);
        this.tgbtn_sync.setSelected(sync);
        this.tgbtn_session.setSelected(session);
        this.tgbtn_dark_theme.setSelected(darkTheme);
        int cvalue = this.pref.getInt("sync-freq", 120);
        this.cbx_frequence.setValue(cvalue);
        this.tgbtn_session.setText(
            session
                ? this.bundle.getString("xbtn.yes")
                : this.bundle.getString("xbtn.no")
        );
        this.tgbtn_dark_theme.setText(
            darkTheme
                ? this.bundle.getString("xbtn.yes")
                : this.bundle.getString("xbtn.no")
        );
        this.tgbtn_sync.setText(
            sync
                ? this.bundle.getString("xbtn.yes")
                : this.bundle.getString("xbtn.no")
        );
        this.tgbtn_print_bill.setText(
            print
                ? this.bundle.getString("xbtn.yes")
                : this.bundle.getString("xbtn.no")
        );
        String message = this.pref.get(
            "mesc",
            this.bundle.getString("goodsoldmsg")
        );
        this.message4client.setText(message);
        this.cbx_param_font_size_head.setItems(
            FXCollections.observableArrayList(
                this.bundle.getString("xlbl.level1"),
                this.bundle.getString("xlbl.level2"),
                this.bundle.getString("xlbl.level3")
            )
        );
        this.cbx_param_font_size_body.setItems(
            FXCollections.observableArrayList(
                this.bundle.getString("xlbl.level1"),
                this.bundle.getString("xlbl.level2"),
                this.bundle.getString("xlbl.level3")
            )
        );
        this.cbx_param_font_size_footer.setItems(
            FXCollections.observableArrayList(
                this.bundle.getString("xlbl.level1"),
                this.bundle.getString("xlbl.level2"),
                this.bundle.getString("xlbl.level3")
            )
        );
        this.cbx_counter.setItems(
            FXCollections.observableArrayList(
                this.bundle.getString("xlbl.random_counter"),
                this.bundle.getString("xlbl.init_counter_bill_day"),
                this.bundle.getString("xlbl.init_counter_bill_month"),
                this.bundle.getString("xlbl.init_counter_bill_year"),
                this.bundle.getString("xlbl.never_init_counter_bill_day")
            )
        );
        boolean pmark = this.pref.getBoolean("print_mark", true);
        boolean pmodel = this.pref.getBoolean("print_mark", true);
        boolean ptail = this.pref.getBoolean("print_tail", true);
        boolean ptotal = this.pref.getBoolean("print_total_usd", true);
        this.print_mark.setSelected(pmark);
        this.print_modele.setSelected(pmodel);
        this.print_tail.setSelected(ptail);
        this.print_total_usd.setSelected(ptotal);
        int slt = this.pref.getInt("print-option-size", 0);
        int body = this.pref.getInt("print-body-size", 0);
        int foot = this.pref.getInt("print-footer-size", 0);
        int cont = this.pref.getInt("count-logic", 0);
        this.cbx_param_font_size_head.getSelectionModel().select(slt - 1);
        this.cbx_param_font_size_body.getSelectionModel().select(body - 1);
        this.cbx_param_font_size_footer.getSelectionModel().select(foot - 1);
        this.cbx_counter.getSelectionModel().select(cont);
        String meth = this.pref.get("meth", "fifo");
        this.ppps.setToggleGroup(this.methodGroup);
        this.fifo.setToggleGroup(this.methodGroup);
        this.lifo.setToggleGroup(this.methodGroup);
        this.rb_emarque.setToggleGroup(this.mode_stock);
        this.rb_serveur.setToggleGroup(this.mode_stock);
        this.mm58.setToggleGroup(this.mode_printer);
        this.mm72.setToggleGroup(this.mode_printer);
        this.mm80.setToggleGroup(this.mode_printer);
        boolean avert = this.pref.getBoolean("averti", true);
        this.avertiBill.setSelected(avert);
        if (meth.equals("ppps")) {
            this.ppps.setSelected(true);
        } else if (meth.equals("fifo")) {
            this.fifo.setSelected(true);
        } else if (meth.equals("lifo")) {
            this.lifo.setSelected(true);
        }
        int port = this.pref.getInt("default_mysql_port", 3306);
        String host = this.pref.get("default_mysql_host", "localhost");
        this.tf_ip_serveur.setText(host);
        this.tf_port_serveur.setText(Integer.toString(port));
        this.tf_mysql_user.setText(this.pref.get("default_mysql_user", "root"));
        this.pf_mysql_password.setText(this.pref.get("default_mysql_password", "Admin*21"));
        this.cb_mysql_ssl.setSelected(this.pref.getBoolean("default_mysql_ssl", false));
        this.mm80
            .selectedProperty()
            .addListener((ov, t, t1) ->
                this.pref.putInt("print-lines-dashcount", 48)
            );
        this.mm58
            .selectedProperty()
            .addListener((ov, t, t1) ->
                this.pref.putInt("print-lines-dashcount", 30)
            );
        this.mm72
            .selectedProperty()
            .addListener((ov, t, t1) ->
                this.pref.putInt("print-lines-dashcount", 42)
            );
        this.rb_emarque.selectedProperty().addListener((ov, t, t1) -> {
            if (t1.booleanValue()) {
                this.pref.putBoolean("embedded_db", true);
                MainUI.notify(
                    null,
                    (String) this.bundle.getString("success"),
                    (String) "Utilisation de la Base interne activee",
                    (long) 4L,
                    (String) "info"
                );
            }
        });
        this.rb_serveur.selectedProperty().addListener((ov, t, t1) -> {
            if (t1.booleanValue()) {
                this.pref.putBoolean("embedded_db", false);
                MainUI.notify(
                    null,
                    (String) this.bundle.getString("success"),
                    (String) "Utilisation de la Base MySQL activee",
                    (long) 4L,
                    (String) "info"
                );
            }
        });
        if (embd) {
            this.rb_emarque.setSelected(true);
        } else {
            this.rb_serveur.setSelected(true);
        }
        this.cbx_main_cur
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    String selectedCurrency = CurrencyConverter.normalize(
                        (String) newValue
                    );
                    CurrencyConverter.setMainCurrency(selectedCurrency);
                    this.cbx_main_cur.setValue(selectedCurrency);
                    if (
                        CurrencyConverter.USD.equals(selectedCurrency) ||
                        CurrencyConverter.CDF.equals(selectedCurrency)
                    ) {
                        this.tf_taux_de_change.setText(
                            String.valueOf(CurrencyConverter.legacyCdfRate())
                        );
                        return;
                    }
                    try {
                        this.tf_taux_de_change.setText(
                            String.valueOf(
                                CurrencyConverter.rateFromUsd(selectedCurrency)
                            )
                        );
                    } catch (IllegalStateException e) {
                        this.tf_taux_de_change.clear();
                    }
                }
            });
        this.avertiBill.selectedProperty().addListener(
            (ChangeListener) new ChangeListener<Boolean>() {
                public void changed(
                    ObservableValue<? extends Boolean> observable,
                    Boolean oldValue,
                    Boolean newValue
                ) {
                    ParametreController.this.pref.putBoolean(
                        "averti",
                        newValue
                    );
                }
            }
        );
        this.cbx_counter
            .getSelectionModel()
            .selectedIndexProperty()
            .addListener(
                (ChangeListener) new ChangeListener<Number>() {
                    public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue,
                        Number newValue
                    ) {
                        int index = newValue.intValue();
                        if (index > -1) {
                            ParametreController.this.pref.putInt(
                                "count-logic",
                                index
                            );
                        }
                    }
                }
            );
        this.cbx_param_font_size_head
            .getSelectionModel()
            .selectedIndexProperty()
            .addListener(
                (ChangeListener) new ChangeListener<Number>() {
                    public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue,
                        Number newValue
                    ) {
                        int index = newValue.intValue();
                        ParametreController.this.pref.putInt(
                            "print-option-size",
                            index + 1
                        );
                        ParametreController.this.pref.putInt(
                            "print-title-size",
                            index + 1
                        );
                        ParametreController.this.pref.putInt(
                            "print-identite-size",
                            index + 1
                        );
                    }
                }
            );
        this.cbx_param_font_size_body
            .getSelectionModel()
            .selectedIndexProperty()
            .addListener(
                (ChangeListener) new ChangeListener<Number>() {
                    public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue,
                        Number newValue
                    ) {
                        int index = newValue.intValue();
                        ParametreController.this.pref.putInt(
                            "print-body-size",
                            index + 1
                        );
                    }
                }
            );
        this.cbx_param_font_size_footer
            .getSelectionModel()
            .selectedIndexProperty()
            .addListener(
                (ChangeListener) new ChangeListener<Number>() {
                    public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue,
                        Number newValue
                    ) {
                        int index = newValue.intValue();
                        ParametreController.this.pref.putInt(
                            "print-footer-size",
                            index + 1
                        );
                    }
                }
            );
        this.ppps.selectedProperty().addListener(
            (ChangeListener) new ChangeListener<Boolean>() {
                public void changed(
                    ObservableValue<? extends Boolean> observable,
                    Boolean oldValue,
                    Boolean newValue
                ) {
                    if (newValue.booleanValue()) {
                        ParametreController.this.pref.put("meth", "ppps");
                        MainUI.notify(
                            null,
                            (String) ParametreController.this.bundle.getString(
                                "success"
                            ),
                            (String) ParametreController.this.bundle.getString(
                                "methpppsaved"
                            ),
                            (long) 3L,
                            (String) "info"
                        );
                    }
                }
            }
        );
        this.fifo.selectedProperty().addListener(
            (ChangeListener) new ChangeListener<Boolean>() {
                public void changed(
                    ObservableValue<? extends Boolean> observable,
                    Boolean oldValue,
                    Boolean newValue
                ) {
                    if (newValue.booleanValue()) {
                        ParametreController.this.pref.put("meth", "fifo");
                        MainUI.notify(
                            null,
                            (String) ParametreController.this.bundle.getString(
                                "success"
                            ),
                            (String) ParametreController.this.bundle.getString(
                                "methfifosaved"
                            ),
                            (long) 3L,
                            (String) "info"
                        );
                    }
                }
            }
        );
        this.lifo
            .selectedProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue.booleanValue()) {
                    this.pref.put("meth", "lifo");
                    MainUI.notify(
                        null,
                        (String) "Succes",
                        (String) this.bundle.getString("methlifosaved"),
                        (long) 3L,
                        (String) "info"
                    );
                }
            });
        this.text_msg
            .textProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    this.pref.put("ads_mesg", (String) newValue);
                }
            });
        this.cbx_display_ports.setConverter(
            (StringConverter) new StringConverter<SerialPort>() {
                public String toString(SerialPort object) {
                    return object == null ? null : object.getSystemPortName();
                }

                public SerialPort fromString(String string) {
                    return ParametreController.this.cbx_display_ports
                        .getItems()
                        .stream()
                        .filter(f ->
                            f.getSystemPortName().equalsIgnoreCase(string)
                        )
                        .findFirst()
                        .orElse(null);
                }
            }
        );
        SerialPort[] ports = SerialPort.getCommPorts();
        this.cbx_display_ports.setItems(
            FXCollections.observableArrayList(ports)
        );
        this.cbx_display_ports
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    this.pref.put("display_port", newValue.getSystemPortName());
                }
            });
    }

    private void onHoverHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.setShadowEffect((Node) img);
    }

    private void onOutHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.removeShaddowEffect((Node) img);
    }

    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        this.pref = Preferences.userNodeForPackage(SyncEngine.class);
        this.cbx_frequence.setItems(
            FXCollections.observableArrayList(new Integer[] {
                30,
                60,
                90,
                120,
                180,
                300,
                360,
                600,
                1200,
                1440,
            })
        );
        this.cbx_frequence
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    this.pref.putInt(
                        "sync-freq",
                        (Integer) newValue
                    );
                    System.out.println(
                        "Frequence de synchronisation reglee a " +
                        newValue +
                        "s (appliquee au prochain cycle)"
                    );
                }
            });
        this.logFileHyperlink.setText(getLogPath());
        this.methodGroup = new ToggleGroup();
        this.mode_stock = new ToggleGroup();
        this.mode_printer = new ToggleGroup();
        SpinnerValueFactory.IntegerSpinnerValueFactory values =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1);
        values.setValue(this.pref.getInt("bill-copy", 1));
        this.spinner.setValueFactory((SpinnerValueFactory) values);
        this.spinner
            .valueProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    this.pref.putInt("bill-copy", (int) newValue);
                }
            });
        this.print_mark
            .selectedProperty()
            .addListener((observable, oldValue, newValue) ->
                this.pref.putBoolean("print_mark", (boolean) newValue)
            );
        this.print_modele
            .selectedProperty()
            .addListener((observable, oldValue, newValue) ->
                this.pref.putBoolean("print_modele", (boolean) newValue)
            );
        this.print_tail
            .selectedProperty()
            .addListener((observable, oldValue, newValue) ->
                this.pref.putBoolean("print_tail", (boolean) newValue)
            );
        this.print_total_usd
            .selectedProperty()
            .addListener((observable, oldValue, newValue) ->
                this.pref.putBoolean("print_total_usd", (boolean) newValue)
            );
    }

    @FXML
    private void saveConfigurations(ActionEvent event) {
        boolean go = true;
        if (this.tf_ip_serveur.getText().isEmpty()) {
            go = false;
        }
        if (this.tf_port_serveur.getText().isEmpty()) {
            go = false;
        }
        if (go) {
            try {
                if (
                    InetAddress.getByName(
                        this.tf_ip_serveur.getText()
                    ).isReachable(5000)
                ) {
                    this.pref.putInt(
                        "default_mysql_port",
                        Integer.parseInt(this.tf_port_serveur.getText())
                    );
                    this.pref.put(
                        "default_mysql_host",
                        this.tf_ip_serveur.getText()
                    );
                    this.pref.put(
                        "default_mysql_user",
                        this.tf_mysql_user.getText() == null
                            ? ""
                            : this.tf_mysql_user.getText().trim()
                    );
                    this.pref.put(
                        "default_mysql_password",
                        this.pf_mysql_password.getText()
                    );
                    this.pref.putBoolean(
                        "default_mysql_ssl",
                        this.cb_mysql_ssl.isSelected()
                    );
                    MainUI.notify(
                        null,
                        (String) "Info",
                        (String) "Succes : Configuration enregistree",
                        (long) 4L,
                        (String) "info"
                    );
                }
            } catch (IOException ex) {
                Logger.getLogger(ParametreController.class.getName()).log(
                    Level.SEVERE,
                    null,
                    ex
                );
            }
        } else {
            MainUI.notify(
                null,
                (String) "Info",
                (String) "Erreur : Configuration non enregistree",
                (long) 4L,
                (String) "error"
            );
        }
    }

    @FXML
    private void reinitSync(ActionEvent event) {
        // 1. Curseur du sync HTTP legacy (full sync)
        this.pref.putLong(Constants.SYNC_ELLAPSED_TIME, 0L);
        // 2. Horodatage de fin de synchronisation : l'upsync ne filtre plus par
        //    ancienneté (tous les PENDING/UNSYNCED sont re-téléversés), mais on
        //    le remet à zéro pour rester cohérent avec le « full sync » legacy.
        this.pref.putLong(SyncEngine.LAST_SYNC_TS_KEY, 0L);
        // 3. Curseur « Page since=… » du rattrapage downsync (noeud DownsyncCatchupService)
        DownsyncCatchupService.resetCursor();
        // 4. Outbox : re-téléverser les mutations déjà appliquées / en échec
        SyncOutboxService.resetForFullResync();
        // 5. Relance immédiate d'un cycle de synchronisation
        try {
            SyncEngine.getInstance().syncInBackground();
        } catch (Exception e) {
            Logger.getLogger(ParametreController.class.getName()).log(
                Level.SEVERE,
                "Echec relance du cycle de synchronisation",
                e
            );
        }
        MainUI.notify(
            null,
            "Info",
            "Synchronisation reinitialisee, le prochain cycle sera total",
            4L,
            "info"
        );
    }

    private static String LOG_PATH = null;

    private static String getLogPath() {
        if (LOG_PATH == null) {
            String dir = MainUI.cPath("/Media/proc/logs");
            String newPath = dir + File.separator + "client.log";
            String legacyPath = dir + File.separator + "client.logs";
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                File legacyFile = new File(legacyPath);
                if (legacyFile.exists()) {
                    boolean renamed = legacyFile.renameTo(newFile);
                    if (!renamed) {
                        try {
                            Files.copy(
                                legacyFile.toPath(),
                                newFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                            );
                        } catch (IOException ignored) {}
                    }
                }
            }
            LOG_PATH = newPath;
        }
        return LOG_PATH;
    }

    private static String getLogDir() {
        return MainUI.cPath("/Media/proc/logs");
    }

    @FXML
    private void openLogFile(ActionEvent event) {
        File logFile = new File(getLogPath());
        if (!logFile.exists()) {
            MainUI.notify(
                null,
                "Info",
                "Aucun fichier de logs trouve.",
                4,
                "error"
            );
            return;
        }
        Thread t = new Thread(() -> {
            try {
                Desktop.getDesktop().open(logFile);
            } catch (IOException e) {
                javafx.application.Platform.runLater(() ->
                    MainUI.notify(
                        null,
                        "Erreur",
                        "Impossible d'ouvrir le fichier: " + e.getMessage(),
                        4,
                        "error"
                    )
                );
            }
        }, "OpenLogFile-Thread");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void saveLogCopy(ActionEvent event) {
        File logFile = new File(getLogPath());
        if (!logFile.exists()) {
            MainUI.notify(
                null,
                "Info",
                "Aucun fichier de logs trouve.",
                4,
                "error"
            );
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer une copie du fichier de logs");
        fileChooser.setInitialFileName("client.log");
        fileChooser
            .getExtensionFilters()
            .add(
                new FileChooser.ExtensionFilter(
                    "Fichier log",
                    "*.logs",
                    "*.log",
                    "*.txt"
                )
            );
        File dest = fileChooser.showSaveDialog(null);
        if (dest != null) {
            Thread t = new Thread(() -> {
                try {
                    Files.copy(
                        logFile.toPath(),
                        dest.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    );
                    javafx.application.Platform.runLater(() ->
                        MainUI.notify(
                            null,
                            "Succes",
                            "Copie enregistree : " + dest.getName(),
                            4,
                            "info"
                        )
                    );
                    try {
                        Desktop.getDesktop().open(dest.getParentFile());
                    } catch (IOException ignored) {}
                } catch (IOException e) {
                    javafx.application.Platform.runLater(() ->
                        MainUI.notify(
                            null,
                            "Erreur",
                            "Impossible de copier le fichier: " +
                                e.getMessage(),
                            4,
                            "error"
                        )
                    );
                }
            }, "SaveLogCopy-Thread");
            t.setDaemon(true);
            t.start();
        }
    }

    @FXML
    private void openLogFolder(ActionEvent event) {
        File dir = new File(getLogDir());
        if (!dir.exists()) {
            MainUI.notify(
                null,
                "Info",
                "Le dossier de logs n'existe pas encore.",
                4,
                "error"
            );
            return;
        }
        Thread t = new Thread(() -> {
            try {
                Desktop.getDesktop().open(dir);
            } catch (IOException e) {
                javafx.application.Platform.runLater(() ->
                    MainUI.notify(
                        null,
                        "Erreur",
                        "Impossible d'ouvrir le dossier: " + e.getMessage(),
                        4,
                        "error"
                    )
                );
            }
        }, "OpenLogFolder-Thread");
        t.setDaemon(true);
        t.start();
    }
}
