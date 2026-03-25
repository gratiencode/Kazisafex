/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import data.Client;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Entreposer;
import data.Fournisseur;
import data.Livraison;
import data.Matiere;
import data.Operation;
import data.Produit;
import data.Vente;


/**
 *
 * @author eroot
 */
public class ComboBoxAutoCompletion<T> implements EventHandler {

    private ComboBox<T> comboBox;
    final private ObservableList<T> data;
    private Integer sid;

    public ComboBoxAutoCompletion(final ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        data = this.comboBox.getItems();
        performAutoCompletion();
    }

    public ComboBoxAutoCompletion(ComboBox<T> comboBox, Integer sid) {
        this.comboBox = comboBox;
        data = this.comboBox.getItems();
        this.sid = sid;
    }

    @Override
    public void handle(Event evt) {
        KeyEvent event = (KeyEvent) evt;
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN
                || event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                || event.getCode() == KeyCode.HOME
                || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
            return;
        }
        if (event.getCode() == KeyCode.BACK_SPACE) {
            String str = this.comboBox.getEditor().getText();
            if (str != null && str.length() > 0) {
                str = str.substring(0, str.length() - 1);
            }
            if (str != null) {
                this.comboBox.getEditor().setText(str);
                moveCaret(str.length());
            }
            this.comboBox.getSelectionModel().clearSelection();
        }

        if (event.getCode() == KeyCode.ENTER && comboBox.getSelectionModel().getSelectedIndex() > -1) {
            return;
        }
        setItems();

    }

    private void setItems() {
        ObservableList<T> list = FXCollections.observableArrayList();
        for (T datum : this.data) {
            String s = this.comboBox.getEditor().getText().toUpperCase();
            if (datum instanceof Client) {
                Client tiers = (Client) datum;
                if ((tiers.getNomClient()+" "+tiers.getPhone()+" "+tiers.getAdresse()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            } else if (datum instanceof Vente) {
                Vente vehicule = (Vente) datum;
                if (vehicule.getReference().toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Fournisseur) {
                Fournisseur transporter = (Fournisseur) datum;
                if ((transporter.getNomFourn()+" "+transporter.getPhone()+""+transporter.getAdresse()+" "+transporter.getIdentification()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Destocker) {
                Destocker comptefin = (Destocker) datum;
                if ((comptefin.getLibelle()+" "+comptefin.getReference()+" "
                        + ""+comptefin.getProductId().getCodebar()+" "+comptefin.getProductId().getNomProduit()+""
                                + " "+comptefin.getProductId().getMarque()+" "+comptefin.getProductId().getModele()
                        +" "+comptefin.getProductId().getTaille()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Produit) {
                Produit p = (Produit) datum;
                if ((p.getNomProduit()+" "+p.getModele()+" "+
                        p.getMarque()+" "+p.getCodebar()+" "+p.getCouleur()
                                +" "+p.getTaille()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Matiere) {
                Matiere p = (Matiere) datum;
                if ((p.getMatiereName()+" - "+p.getTypeMatiere()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Livraison) {
                Livraison p = (Livraison) datum;
                Fournisseur f=p.getFournId();
                if ((p.getDateLivr().toString()+" "+p.getNumPiece()+" "+
                        p.getLibelle()+" "+(f==null?"":f.getNomFourn())+" "+p.getReference()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Entreposer) {
                Entreposer p = (Entreposer) datum;
                String c=p.getComment();
                String mat=p.getMatiereId().getMatiereName();
                String numlot = p.getNumlot();
                if ((c+" "+mat+" "+numlot+" "+p.getDepotId().getNomDepot()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Depense) {
                Depense p = (Depense) datum;
                String c=p.getNomDepense();
                String mat=p.getFrequence();
               
                if ((c+" "+mat).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof CompteTresor) {
                CompteTresor p = (CompteTresor) datum;
                String c=p.getIntitule();
                String mat=p.getBankName();
                String numlot = p.getTypeCompte();
                if ((c+" "+mat+" "+numlot+" "+p.getNumeroCompte()).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }else if (datum instanceof Operation) {
                Operation p = (Operation) datum;
                String c=p.getLibelle();
                String mat=p.getReferenceOp();
                String numlot = p.getDepenseId().getNomDepense();
                if ((c+" "+mat+" "+numlot).toUpperCase().contains(s.toUpperCase())) {
                    list.add(datum);
                }
            }

        }

        if (list.isEmpty()) {
            this.comboBox.hide();
        }
        this.comboBox.setItems(list);
        this.comboBox.show();
    }

    private void performAutoCompletion() {
        this.comboBox.setEditable(true);
        this.comboBox.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {//mean onfocus
                this.comboBox.show();
            }
        });

        this.comboBox.getEditor().setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    return;
                }
            }
            this.comboBox.show();
        });

        this.comboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            moveCaret(this.comboBox.getEditor().getText().length());
        });

        this.comboBox.setOnKeyPressed(t -> comboBox.hide());
        this.comboBox.setOnKeyReleased(ComboBoxAutoCompletion.this);

        if (this.sid != null) {
            this.comboBox.getSelectionModel().select(this.sid);
        }

    }

    private void moveCaret(int textLength) {
        this.comboBox.getEditor().positionCaret(textLength);
    }

}
