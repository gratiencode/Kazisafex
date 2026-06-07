package com.endeleya.kazisafex;

import data.Entreprise;
import data.Immobilisation;
import data.PermitTo;
import data.core.KazisafeServiceFactory;
import data.network.Kazisafe;
import delegates.ImmobilisationDelegate;
import delegates.PermissionDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.Util;

public class ImmobilisationController implements Initializable {

    private static ImmobilisationController instance;

    @FXML
    private TableView<Immobilisation> tb_immobilisation;
    @FXML
    private TableColumn<Immobilisation, String> col_libelle;
    @FXML
    private TableColumn<Immobilisation, String> col_categorie;
    @FXML
    private TableColumn<Immobilisation, String> col_region;
    @FXML
    private TableColumn<Immobilisation, String> col_date;
    @FXML
    private TableColumn<Immobilisation, String> col_valeur;
    @FXML
    private TableColumn<Immobilisation, String> col_residuelle;
    @FXML
    private TableColumn<Immobilisation, Number> col_duree;
    @FXML
    private TableColumn<Immobilisation, String> col_dotation;
    @FXML
    private TableColumn<Immobilisation, Boolean> col_actif;
    @FXML
    private TextField txt_libelle;
    @FXML
    private ComboBox<String> txt_categorie;
    @FXML
    private TextField txt_valeur;
    @FXML
    private TextField txt_residuelle;
    @FXML
    private TextField txt_duree;
    @FXML
    private Label lbl_status;
    @FXML
    private Button btn_create;
    @FXML
    private Button btn_update;
    @FXML
    private Button btn_delete;

    private final ObservableList<Immobilisation> immobilisations = FXCollections.observableArrayList();
    private Preferences pref;
    private Kazisafe kazisafe;
    private String role;
    private String region;
    @FXML
    private DatePicker dpk_date_acq_immo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        role = pref.get("priv", "");
        region = pref.get("region", "...");
        txt_categorie.setItems(FXCollections.observableArrayList("Corporelle", "Incorporelle", "Financiere"));
        configureTable();
        tb_immobilisation.setItems(immobilisations);
        MainUI.setPattern(dpk_date_acq_immo);
        dpk_date_acq_immo.setValue(LocalDate.now());
        applyPermissions();
        refresh();
    }

    private void loadData() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    List<Immobilisation> lims = ImmobilisationDelegate.findImmobilisations();
                    immobilisations.setAll(lims);
                });
    }

    public static ImmobilisationController getInstance() {
        return instance;
    }

    private void configureTable() {
        col_libelle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLibelle()));
        col_categorie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        col_region.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegion()));
        col_date.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateAcquisition() == null ? "-" : c.getValue().getDateAcquisition().toString()));
        col_valeur.setCellValueFactory(c -> new SimpleStringProperty(format(c.getValue().getValeurOrigineUsd())));
        col_residuelle
                .setCellValueFactory(c -> new SimpleStringProperty(format(c.getValue().getValeurResiduelleUsd())));
        col_duree.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getDureeAmortissementMois() == null ? 0 : c.getValue().getDureeAmortissementMois()));
        col_dotation.setCellValueFactory(c -> new SimpleStringProperty(format(c.getValue().dotationMensuelleUsd())));
        col_actif.setCellValueFactory(c -> new SimpleBooleanProperty(Boolean.TRUE.equals(c.getValue().getActif())));
    }

    private void applyPermissions() {
        boolean allowed = hasPermission(PermitTo.CREATE_OPERATION);
        btn_create.setDisable(!allowed);
        btn_create.setOpacity(allowed ? 1d : 0.5d);
        btn_update.setDisable(!allowed);
        btn_update.setOpacity(allowed ? 1d : 0.5d);
        btn_delete.setDisable(!allowed);
        btn_delete.setOpacity(allowed ? 1d : 0.5d);
    }

    public void init(Entreprise entreprise, Kazisafe service) {
        this.kazisafe = service;
        if (this.kazisafe == null) {
            String token = pref.get("token", null);
            if (token != null) {
                this.kazisafe = KazisafeServiceFactory.createService(token);
            }
        }
        refresh();
    }

    private void refresh(MouseEvent event) {
        refresh();
    }

    @FXML
    private void create(Event e) {
        if (!hasPermission(PermitTo.CREATE_OPERATION)) {
            MainUI.notify(null, "Permission", "Action non autorisee par vos permissions", 3, "warning");
            return;
        }
        String libelle = txt_libelle.getText();
        if (libelle == null || libelle.isBlank()) {
            MainUI.notify(null, "Validation", "Le libelle est obligatoire", 3, "warning");
            return;
        }

        String category = txt_categorie.getValue();
        if (category == null || category.isBlank()) {
            MainUI.notify(null, "Validation", "La categorie est obligatoire", 3, "warning");
            return;
        }

        String valeur = txt_valeur.getText();
        if (valeur == null || valeur.isBlank()) {
            MainUI.notify(null, "Validation", "La valeur est obligatoire", 3, "warning");
            return;
        }
        String duree = txt_duree.getText();
        if (duree == null || duree.isBlank()) {
            MainUI.notify(null, "Validation", "La duree de vie de l'immobilisation est obligatoire", 3, "warning");
            return;
        }

        String residuelle = txt_residuelle.getText();
        if (residuelle == null || residuelle.isBlank()) {
            MainUI.notify(null, "Validation", "La valeur residuelle de l'immobilisation est obligatoire", 3, "warning");
            return;
        }

        LocalDate dt = dpk_date_acq_immo.getValue();
        if (dt == null) {
            MainUI.notify(null, "Validation", "La date d'acquisition de l'immobilisation est obligatoire", 3,
                    "warning");
            return;
        }
        Immobilisation body = new Immobilisation(DataId.generate());
        body.setLibelle(libelle);
        body.setCategorie(txt_categorie.getValue());
        body.setRegion(region);
        body.setDateAcquisition(dt);
        body.setValeurOrigineUsd(parse(txt_valeur.getText(), 0d));
        body.setValeurResiduelleUsd(parse(txt_residuelle.getText(), 0d));
        body.setDureeAmortissementMois((int) parse(txt_duree.getText(), 12d));
        body.setActif(true);

        ImmobilisationDelegate.saveImmobilisation(body);
        if (kazisafe == null) {
            lbl_status.setText("Mode hors ligne: en attente de synchronisation");
            immobilisations.add(0, body);
            clearForm();
            return;
        }
        kazisafe.createImmobilisation(body).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<Immobilisation> call, retrofit2.Response<Immobilisation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> {
                        lbl_status.setText("Immobilisation enregistree et synchronisee");
                        if (!immobilisations.contains(body)) {
                            immobilisations.add(0, response.body());
                        }
                    });
                    clearForm();
                    return;
                }
                Platform.runLater(() -> {
                    lbl_status.setText("Enregistre localement (Echec synchro: " + response.code() + ")");
                    if (!immobilisations.contains(body)) {
                        immobilisations.add(0, body);
                    }
                });

            }

            @Override
            public void onFailure(retrofit2.Call<Immobilisation> call, Throwable throwable) {
                Platform.runLater(() -> {
                    lbl_status.setText("Enregistre localement (Hors ligne)");
                    if (!immobilisations.contains(body)) {
                        immobilisations.add(0, body);
                    }
                });
            }
        });
    }

    @FXML
    private void showDetails(MouseEvent event) {
        Immobilisation selected = tb_immobilisation.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txt_libelle.setText(selected.getLibelle());
            txt_categorie.setValue(selected.getCategorie());
            txt_valeur.setText(String.valueOf(selected.getValeurOrigineUsd()));
            txt_residuelle.setText(String.valueOf(selected.getValeurResiduelleUsd()));
            txt_duree.setText(String.valueOf(selected.getDureeAmortissementMois()));
            dpk_date_acq_immo.setValue(selected.getDateAcquisition());
        }
    }

    @FXML
    private void update(Event e) {
        Immobilisation selected = tb_immobilisation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainUI.notify(null, "Selection", "Veuillez selectionner une immobilisation a modifier", 3, "warning");
            return;
        }
        if (!hasPermission(PermitTo.CREATE_OPERATION)) {
            MainUI.notify(null, "Permission", "Action non autorisee par vos permissions", 3, "warning");
            return;
        }

        selected.setLibelle(txt_libelle.getText());
        selected.setCategorie(txt_categorie.getValue());
        selected.setValeurOrigineUsd(parse(txt_valeur.getText(), selected.getValeurOrigineUsd()));
        selected.setValeurResiduelleUsd(parse(txt_residuelle.getText(), selected.getValeurResiduelleUsd()));
        selected.setDureeAmortissementMois((int) parse(txt_duree.getText(), selected.getDureeAmortissementMois()));
        selected.setDateAcquisition(dpk_date_acq_immo.getValue());

        ImmobilisationDelegate.updateImmobilisation(selected);
        if (kazisafe != null) {
            kazisafe.updateImmobilisation(selected.getUid(), selected).enqueue(new retrofit2.Callback<Immobilisation>() {
                @Override
                public void onResponse(retrofit2.Call<Immobilisation> call,
                        retrofit2.Response<Immobilisation> response) {
                    Platform.runLater(() -> {
                        lbl_status.setText("Immobilisation modifiee et synchronisee");
                        tb_immobilisation.refresh();
                    });
                }

                @Override
                public void onFailure(retrofit2.Call<Immobilisation> call, Throwable throwable) {
                    Platform.runLater(() -> {
                        lbl_status.setText("Modifiee localement (Hors ligne)");
                        tb_immobilisation.refresh();
                    });
                }
            });
        } else {
            lbl_status.setText("Modifiee localement (Mode hors ligne)");
            tb_immobilisation.refresh();
        }
        clearForm();
    }

    @FXML
    private void delete(Event e) {
        Immobilisation selected = tb_immobilisation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainUI.notify(null, "Selection", "Veuillez selectionner une immobilisation a supprimer", 3, "warning");
            return;
        }
        if (!hasPermission(PermitTo.CREATE_OPERATION)) {
            MainUI.notify(null, "Permission", "Action non autorisee par vos permissions", 3, "warning");
            return;
        }

        ImmobilisationDelegate.deleteImmobilisation(selected);
        immobilisations.remove(selected);
        if (kazisafe != null) {
            kazisafe.deleteImmobilisation(selected.getUid()).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    Platform.runLater(() -> lbl_status.setText("Immobilisation supprimee et synchronisee"));
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable throwable) {
                    Platform.runLater(() -> lbl_status.setText("Supprimee localement (Hors ligne)"));
                }
            });
        } else {
            lbl_status.setText("Supprimee localement (Mode hors ligne)");
        }
        clearForm();
    }

    @FXML
    private void openAmortissement(Event e) {
        MainuiController.getInstance().switchToRepport(null);
        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
            javafx.application.Platform.runLater(() -> {
                RepportController controller = RepportController.getInstance();
                if (controller != null) {
                    controller.focusOnImmobilisation();
                }
            });
        }).start();
    }

    private void refresh() {
        loadData();
        if (kazisafe == null) {
            lbl_status.setText("Mode hors ligne");

            return;
        }
        kazisafe.getImmobilisations(selectRegion()).enqueue(new retrofit2.Callback<List<Immobilisation>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Immobilisation>> call,
                    retrofit2.Response<List<Immobilisation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    immobilisations.setAll(response.body());
                    Platform.runLater(() -> {
                        lbl_status.setText(immobilisations.size() + " immobilisation(s)");
                    });
                    return;
                }
                Platform.runLater(() -> {
                    lbl_status.setText("Echec chargement: " + response.code());
                });

            }

            @Override
            public void onFailure(retrofit2.Call<List<Immobilisation>> call, Throwable throwable) {
                lbl_status.setText("Mode hors ligne");
            }
        });
    }

    public void search(String term) {
        if (term == null || term.isBlank()) {
            tb_immobilisation.setItems(immobilisations);
            return;
        }
        ObservableList<Immobilisation> filtered = FXCollections.observableArrayList();
        for (Immobilisation imo : immobilisations) {
            String v = (imo.getLibelle() + " " + imo.getCategorie() + " " + imo.getRegion()).toUpperCase();
            if (v.contains(term.toUpperCase())) {
                filtered.add(imo);
            }
        }
        tb_immobilisation.setItems(filtered);
    }

    private boolean hasPermission(PermitTo permit) {
        return role != null && role.toUpperCase().contains("ALL_ACCESS")
                || PermissionDelegate.hasPermission(permit);
    }

    private String selectRegion() {
        return role != null && role.toUpperCase().contains("ALL_ACCESS") ? null : region;
    }

    private String format(Double value) {
        double v = value == null ? 0d : value;
        return Util.toPlain(BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
    }

    private double parse(String value, double fallback) {
        try {
            return Double.parseDouble(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            MainUI.notify(null, "Validation", "La valeur de ce champs doit etre en chiffre uniquement", 3, "error");
            return fallback;
        }
    }

    private void clearForm() {
        txt_libelle.clear();
        txt_valeur.clear();
        txt_residuelle.clear();
        txt_duree.clear();
    }
}
