/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.JournalDelegate;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import tools.MainUI;
import tools.SyncEngine;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class ParametreController implements Initializable {

    @FXML
    private ToggleButton tgbtn_session;
    @FXML
    private TextField tf_taux_de_change;
    @FXML
    private ToggleButton tgbtn_sync;
    @FXML
    private ToggleButton tgbtn_print_bill;
    @FXML
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
    ComboBox<String> cbx_param_paper_size;
    @FXML private TextField text_msg;
    @FXML
    private RadioButton ppps;
    @FXML
    private RadioButton fifo;
    @FXML
    private RadioButton lifo;
    @FXML
    private CheckBox avertiBill;
    ToggleGroup methodGroup;
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

    public ParametreController() {
        instance = this;
    }

    public static ParametreController getInstance() {
        return instance;
    }

    @FXML
    public void configTaux(Event evt) {
        if (tf_taux_de_change.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("inputaux"), 4, "error");
            return;
        }
        try {
            pref.putDouble("taux2change", Double.parseDouble(tf_taux_de_change.getText()));
            MainUI.notify(null, "Info", bundle.getString("ratesaved"), 4, "Info");
        } catch (NumberFormatException e) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("rateerror"), 4, "error");
        }
    }

    @FXML
    public void configPrint(Event evt) {
        ToggleButton tbtn = (ToggleButton) evt.getSource();
        if (tbtn.getText().equals(bundle.getString("xbtn.no"))) {
            tbtn.setText(bundle.getString("xbtn.yes"));
            pref.putBoolean("print", true);
        } else {
            tbtn.setText(bundle.getString("xbtn.no"));
            pref.putBoolean("print", false);
        }
    }

    @FXML
    public void configSync(Event evt) {
        ToggleButton tbtn = (ToggleButton) evt.getSource();
        if (tbtn.getText().equals(bundle.getString("xbtn.no"))) {
            tbtn.setText(bundle.getString("xbtn.yes"));
            pref.putBoolean("sync", true);
            cbx_frequence.setDisable(false);
        } else {
            tbtn.setText(bundle.getString("xbtn.no"));
            pref.putBoolean("sync", false);
            cbx_frequence.setDisable(true);
        }
    }

    @FXML
    public void configFreqSync(Event evt) {
        pref.putInt("sync-freq", cbx_frequence.getValue());
//        SyncEngine.getInstance().start();
    Executors.newSingleThreadExecutor()
                    .execute(() -> {
  JournalDelegate.swipe();
        MainUI.notify(null, "Info", bundle.getString("synconfigsaved"), 4, "Info");
                    });      
    }

    @FXML
    public void configSession(Event evt) {
        ToggleButton tbtn = (ToggleButton) evt.getSource();

        if (tbtn.getText().equals(bundle.getString("xbtn.no"))) {
            tbtn.setText(bundle.getString("xbtn.yes"));
            pref.putBoolean("session", true);
        } else if (tbtn.getText().equals(bundle.getString("xbtn.yes"))) {
            tbtn.setText(bundle.getString("xbtn.no"));
            pref.putBoolean("session", false);
        }
    }

    @FXML
    public void setMessage4CustomersOnBill(Event evt) {
        pref.put("mesc", message4client.getText());
        MainUI.notify(null, bundle.getString("success"), bundle.getString("msgconf"), 4, "info");
    }

    public void init() {
        boolean session = pref.getBoolean("session", false);
        boolean sync = pref.getBoolean("sync", true),
                print = pref.getBoolean("print", true);

        mainCur = pref.get("mainCur", "USD");
        cbx_main_cur.setValue(mainCur);
        tf_taux_de_change.setText(pref.get("taux2change", "2700"));
        tgbtn_print_bill.setSelected(print);
        tgbtn_sync.setSelected(sync);
        tgbtn_session.setSelected(session);
        int cvalue = pref.getInt("sync-freq", 120);
        cbx_frequence.setValue(cvalue);
        tgbtn_session.setText(session ? bundle.getString("xbtn.yes") : bundle.getString("xbtn.no"));
        tgbtn_sync.setText(sync ? bundle.getString("xbtn.yes") : bundle.getString("xbtn.no"));
        tgbtn_print_bill.setText(print ? bundle.getString("xbtn.yes") : bundle.getString("xbtn.no"));
        String message = pref.get("mesc", bundle.getString("goodsoldmsg"));
        message4client.setText(message);
        cbx_param_paper_size.setItems(FXCollections.observableArrayList(bundle.getString("xlbl.level3"), bundle.getString("xlbl.level2"), bundle.getString("xlbl.level1")));
        cbx_main_cur.setItems(FXCollections.observableArrayList("USD", "CDF"));
        cbx_counter.setItems(FXCollections.observableArrayList(
                bundle.getString("xlbl.random_counter"),
                bundle.getString("xlbl.init_counter_bill_day"),
                bundle.getString("xlbl.init_counter_bill_month"),
                bundle.getString("xlbl.init_counter_bill_year"),
                 bundle.getString("xlbl.never_init_counter_bill_day")));
        boolean pmark = pref.getBoolean("print_mark", true);
        boolean pmodel = pref.getBoolean("print_mark", true);
        boolean ptail = pref.getBoolean("print_tail", true);
        boolean ptotal = pref.getBoolean("print_total_usd", true);
        print_mark.setSelected(pmark);
        print_modele.setSelected(pmodel);
        print_tail.setSelected(ptail);
        print_total_usd.setSelected(ptotal);
        int slt = pref.getInt("print-option-size", 0);
        int cont = pref.getInt("count-logic", 0);
        cbx_param_paper_size.getSelectionModel().select(slt);
        cbx_counter.getSelectionModel().select(cont);
        String meth = pref.get("meth", "fifo");
        ppps.setToggleGroup(methodGroup);
        fifo.setToggleGroup(methodGroup);
        lifo.setToggleGroup(methodGroup);
        boolean avert = pref.getBoolean("averti", true);
        avertiBill.setSelected(avert);
        if (meth.equals("ppps")) {
            ppps.setSelected(true);
        } else if (meth.equals("fifo")) {
            fifo.setSelected(true);
        } else if (meth.equals("lifo")) {
            lifo.setSelected(true);
        }
        cbx_main_cur.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null) {
                pref.put("mainCur", newValue);
            }
        });
        avertiBill.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                pref.putBoolean("averti", newValue);
            }
        });
        cbx_counter.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                /**
                 * Style title = new
                 * Style().setJustification(EscPosConst.Justification.Center).setFontSize(Style.FontSize._3,
                 * Style.FontSize._3); Style identite = new
                 * Style().setJustification(EscPosConst.Justification.Center).setFontSize(Style.FontSize._1,
                 * Style.FontSize._1); Style client = new
                 * Style(printer.getStyle()).setBold(true)
                 * .setUnderline(Style.Underline.OneDotThick); Style gras = new
                 * Style(printer.getStyle())
                 * .setJustification(EscPosConst.Justification.Right)
                 * .setBold(true); Style right = new Style(printer.getStyle())
                 * .setJustification(EscPosConst.Justification.Right); Style
                 * left = new Style(printer.getStyle())
                 */
                int index = newValue.intValue();
                if (index > -1) {
                    pref.putInt("count-logic", index);
                }
            }
        });

        cbx_param_paper_size.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                /**
                 * Style title = new
                 * Style().setJustification(EscPosConst.Justification.Center).setFontSize(Style.FontSize._3,
                 * Style.FontSize._3); Style identite = new
                 * Style().setJustification(EscPosConst.Justification.Center).setFontSize(Style.FontSize._1,
                 * Style.FontSize._1); Style client = new
                 * Style(printer.getStyle()).setBold(true)
                 * .setUnderline(Style.Underline.OneDotThick); Style gras = new
                 * Style(printer.getStyle())
                 * .setJustification(EscPosConst.Justification.Right)
                 * .setBold(true); Style right = new Style(printer.getStyle())
                 * .setJustification(EscPosConst.Justification.Right); Style
                 * left = new Style(printer.getStyle())
                 */
                int index = newValue.intValue();
                switch (index) {
                    case 2:
                        pref.putInt("print-option-size", 2);
                        pref.putInt("print-title-size", 2);
                        pref.putInt("print-body-size", 1);
                        pref.putInt("print-identite-size", 1);
                        pref.putInt("print-lines-dashcount", 30);
                        break;
                    case 1:
                        pref.putInt("print-option-size", 1);
                        pref.putInt("print-title-size", 2);
                        pref.putInt("print-body-size", 1);
                        pref.putInt("print-identite-size", 1);
                        pref.putInt("print-lines-dashcount", 42);
                        break;
                    case 0:
                        pref.putInt("print-option-size", 0);
                        pref.putInt("print-title-size", 3);
                        pref.putInt("print-body-size", 1);
                        pref.putInt("print-identite-size", 1);
                        pref.putInt("print-lines-dashcount", 48);
                        break;
                }
            }
        });
        ppps.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pref.put("meth", "ppps");
                    MainUI.notify(null, bundle.getString("success"), bundle.getString("methpppsaved"), 3, "info");
                }
            }
        });
        fifo.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pref.put("meth", "fifo");
                    MainUI.notify(null, bundle.getString("success"), bundle.getString("methfifosaved"), 3, "info");
                }
            }
        });
        lifo.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pref.put("meth", "lifo");
                    MainUI.notify(null, "Succes", bundle.getString("methlifosaved"), 3, "info");
                }
            }
        });
        text_msg.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.isEmpty()){
                    pref.put("ads_mesg", newValue);
                }
            }
        });

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

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        cbx_frequence.setItems(FXCollections.observableArrayList(30, 60, 90, 120, 180, 300, 360, 600, 1200, 1440));
        cbx_frequence.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
            Executors.newSingleThreadExecutor()
                    .execute(() -> {
//                        pref.putInt("sync-freq", newValue);
//                        
//                        SyncEngine.getInstance().start();
//                        MainUI.notify(null, "Info", bundle.getString("synconfigsaved"), 4, "Info");
                    });
        });
        methodGroup = new ToggleGroup();
        SpinnerValueFactory<Integer> values = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1);
        values.setValue(pref.getInt("bill-copy", 1));
        spinner.setValueFactory(values);
        spinner.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (newValue != null) {
                    pref.putInt("bill-copy", newValue);
                }
            }
        });
        
        print_mark.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    pref.putBoolean("print_mark", newValue);
            }
        });
        print_modele.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    pref.putBoolean("print_modele", newValue);
            }
        });
        print_tail.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    pref.putBoolean("print_tail", newValue);
            }
        });
        print_total_usd.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    pref.putBoolean("print_total_usd", newValue);
            }
        });
    }

}
