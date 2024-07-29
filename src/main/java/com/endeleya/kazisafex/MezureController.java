package com.endeleya.kazisafex;

import delegates.MesureDelegate;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import data.network.Kazisafe;
import data.Entreprise;
import data.Mesure;
import data.Produit;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.Tables;
import tools.Util;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class MezureController implements Initializable {

    @FXML
    private TextField mez_desc;
    @FXML
    private TextField mez_quants;
    @FXML
    private ListView<Mesure> list_mezr;

    Produit choosenProduct;
    ObservableList<Mesure> mesures;
    @FXML
    Label txt_label_select_pro;
    Mesure selectedMez;
    ResourceBundle bundle;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
    }

    @FXML
    private void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    @FXML
    public void modify(Event evt) {
        if (mez_desc.getText().isEmpty() || mez_quants.getText().isEmpty()) {
            return;
        }
        if (selectedMez != null) {
            selectedMez.setDescription(mez_desc.getText());
            selectedMez.setQuantContenu(Double.valueOf(mez_quants.getText()));
            selectedMez.setProduitId(choosenProduct);
            Mesure upd = MesureDelegate.updateMesure(selectedMez);
            if (upd != null) {
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(upd, Constants.ACTION_UPDATE, Tables.MESURE);
                        });
                MainUI.notify(null, "Modification", "Modification faite avec succes", 3, "info");
                close(evt);
            }
        }
    }

    @FXML
    private void save(Event evt) {
        if (mez_desc.getText().isEmpty() || mez_quants.getText().isEmpty()) {
            return;
        }
        try {
            Mesure m = new Mesure(DataId.generate());
            m.setDescription(mez_desc.getText());
            double q = Double.valueOf(mez_quants.getText());
            if (q == 0) {
                MainUI.notify(null, "Erreur", "Zero n'est pas une valeur valide", 4, "error");
                return;
            };
            m.setQuantContenu(q);
            m.setProduitId(choosenProduct);
            //List<Mesure> lms = db.findByProduit(Mesure.class, m.getProduitId().getUid());
            List<Mesure> mz = MesureDelegate.findMesureByProduit(m.getProduitId().getUid(), m.getDescription());
            if (!mz.isEmpty()) {
                MainUI.notify(null, "Erreur", "La meme mesure pour ce produit existe deja", 4, "error");
                return;
            }
            Mesure ms = MesureDelegate.findByProduitAndQuant(m.getProduitId().getUid(), m.getQuantContenu());
            if (ms != null) {
                MainUI.notify(null, "Erreur", "La mesure ayant la même quantité existe déjà ", 3, "error");
                return;
            }
            Mesure saved = MesureDelegate.saveMesure(m);//db.insertAndSync(m);
            if (saved != null) {
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(m, Constants.ACTION_CREATE, Tables.MESURE);
                        });
                mesures.add(m);
                MainUI.notify(null, "Succes", "Mesure du produit enregistre avec succes", 3, "Info");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void setProduct(Produit p) {
        this.choosenProduct = p;
        txt_label_select_pro.setText("Produit : " + p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " " + p.getTaille() + " \n Couleur : " + p.getCouleur());
    }

    public void setDatabase(Entreprise eze, Kazisafe ksf) {
        mesures = FXCollections.observableArrayList(MesureDelegate.findMesureByProduit(choosenProduct.getUid()));
        list_mezr.setItems(mesures);
        list_mezr.setCellFactory((ListView<Mesure> param) -> new ListCell<Mesure>() {
            @Override
            protected void updateItem(Mesure item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getDescription() + " : " + item.getQuantContenu());
                        }
                    });
                }
            }

        });
        list_mezr.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Mesure>() {
            @Override
            public void changed(ObservableValue<? extends Mesure> observable, Mesure oldValue, Mesure newValue) {
                selectedMez = newValue;
                mez_desc.setText(selectedMez.getDescription());
                mez_quants.setText(String.valueOf(selectedMez.getQuantContenu()));
            }
        });
        ContextMenu cm = new ContextMenu();
        MenuItem mi = new MenuItem("Retirer de la liste");
        cm.getItems().add(mi);
        list_mezr.setContextMenu(cm);
        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (selectedMez == null) {
                    return;
                }
                mesures.remove(selectedMez);
                MesureDelegate.deleteMesure(selectedMez);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(selectedMez, Constants.ACTION_DELETE, Tables.MESURE);
                        });
               

            }
        });

    }

    private List<Mesure> findMesure(List<Mesure> allMez, String produitId) {
        List<Mesure> lmz = new ArrayList<>();
        for (Mesure m : allMez) {
            if (m.getProduitId().getUid().equals(produitId)) {
                lmz.add(m);
            }
        }
        return lmz;
    }

    private Mesure findMesureWithDesc(List<Mesure> allMez, String descr) {

        for (Mesure m : allMez) {
            if (m.getDescription().equalsIgnoreCase(descr)) {
                return m;
            }
        }
        return null;
    }

    private Mesure findMesureWithQuant(List<Mesure> allMez, double q) {
        for (Mesure m : allMez) {
            if (m.getQuantContenu() == q) {
                return m;
            }
        }
        return null;
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

}
