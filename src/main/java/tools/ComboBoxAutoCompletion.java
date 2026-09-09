/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private FilteredList<T> filteredData;
    private Integer sid;

    public ComboBoxAutoCompletion(final ComboBox<T> comboBox) {
        attach(comboBox);
    }

    public ComboBoxAutoCompletion(ComboBox<T> comboBox, Integer sid) {
        this.sid = sid;
        attach(comboBox);
    }

    private void attach(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        // Filtrage prédicat sur la liste d'origine : on ne remplace JAMAIS les
        // items du ComboBox à chaque frappe (ce remplacement + show() répété
        // faisaient perdre le focus et le texte saisi dans l'éditeur). Le popup
        // suit automatiquement la liste filtrée sans redéclencher d'ouverture.
        wrap(comboBox.getItems());
        // Si le contrôleur remplace les items après la construction (ex.
        // rechargement de liste), on ré-enveloppe la nouvelle liste pour que la
        // complétion et le focus restent synchronisés avec les items affichés.
        comboBox.itemsProperty().addListener((obs, oldList, newList) -> {
            if (newList != null && newList != filteredData) {
                wrap(newList);
            }
        });
        performAutoCompletion();
    }

    private void wrap(ObservableList<T> items) {
        filteredData = new FilteredList<>(
            items == null ? FXCollections.observableArrayList() : items
        );
        comboBox.setItems(filteredData);
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
        String str = this.comboBox.getEditor().getText();
        String query = str == null ? "" : str.toUpperCase();
        filteredData.setPredicate(datum -> matches(datum, query));
        if (filteredData.isEmpty()) {
            this.comboBox.hide();
        } else if (!this.comboBox.isShowing() && this.comboBox.getEditor().isFocused() && !query.isEmpty()) {
            // Rouvre la liste uniquement quand elle cesse d'être vide pendant la
            // saisie ; l'ouverture répétée pendant la frappe faisait sauter le
            // focus de l'éditeur vers la popup.
            this.comboBox.show();
        }
    }

    private boolean matches(T datum, String query) {
        if (datum instanceof Client) {
            Client tiers = (Client) datum;
            return (tiers.getNomClient() + " " + tiers.getPhone() + " " + tiers.getAdresse()).toUpperCase().contains(query);
        } else if (datum instanceof Vente) {
            Vente vehicule = (Vente) datum;
            return vehicule.getReference().toUpperCase().contains(query);
        } else if (datum instanceof Fournisseur) {
            Fournisseur transporter = (Fournisseur) datum;
            return (transporter.getNomFourn() + " " + transporter.getPhone() + "" + transporter.getAdresse() + " " + transporter.getIdentification()).toUpperCase().contains(query);
        } else if (datum instanceof Destocker) {
            Destocker comptefin = (Destocker) datum;
            return (comptefin.getLibelle() + " " + comptefin.getReference() + " "
                    + "" + comptefin.getProductId().getCodebar() + " " + comptefin.getProductId().getNomProduit() + ""
                    + " " + comptefin.getProductId().getMarque() + " " + comptefin.getProductId().getModele()
                    + " " + comptefin.getProductId().getTaille()).toUpperCase().contains(query);
        } else if (datum instanceof Produit) {
            Produit p = (Produit) datum;
            return (p.getNomProduit() + " " + p.getModele() + " "
                    + p.getMarque() + " " + p.getCodebar() + " " + p.getCouleur()
                    + " " + p.getTaille()).toUpperCase().contains(query);
        } else if (datum instanceof Matiere) {
            Matiere p = (Matiere) datum;
            return (p.getMatiereName() + " - " + p.getTypeMatiere()).toUpperCase().contains(query);
        } else if (datum instanceof Livraison) {
            Livraison p = (Livraison) datum;
            Fournisseur f = p.getFournId();
            return (p.getDateLivr().toString() + " " + p.getNumPiece() + " "
                    + p.getLibelle() + " " + (f == null ? "" : f.getNomFourn()) + " " + p.getReference()).toUpperCase().contains(query);
        } else if (datum instanceof Entreposer) {
            Entreposer p = (Entreposer) datum;
            String c = p.getComment();
            String mat = p.getMatiereId().getMatiereName();
            String numlot = p.getNumlot();
            return (c + " " + mat + " " + numlot + " " + p.getDepotId().getNomDepot()).toUpperCase().contains(query);
        } else if (datum instanceof Depense) {
            Depense p = (Depense) datum;
            String c = p.getNomDepense();
            String mat = p.getFrequence();
            return (c + " " + mat).toUpperCase().contains(query);
        } else if (datum instanceof CompteTresor) {
            CompteTresor p = (CompteTresor) datum;
            String c = p.getIntitule();
            String mat = p.getBankName();
            String numlot = p.getTypeCompte();
            return (c + " " + mat + " " + numlot + " " + p.getNumeroCompte()).toUpperCase().contains(query);
        } else if (datum instanceof Operation) {
            Operation p = (Operation) datum;
            String c = p.getLibelle();
            String mat = p.getReferenceOp();
            String numlot = p.getDepenseId().getNomDepense();
            return (c + " " + mat + " " + numlot).toUpperCase().contains(query);
        }
        return true;
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
