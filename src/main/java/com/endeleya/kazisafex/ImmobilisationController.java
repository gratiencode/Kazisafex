package com.endeleya.kazisafex;

import data.Entreprise;
import data.Immobilisation;
import data.PermitTo;
import data.core.KazisafeServiceFactory;
import data.network.Kazisafe;
import delegates.PermissionDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
    private TextField txt_categorie;
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

    private final ObservableList<Immobilisation> immobilisations = FXCollections.observableArrayList();
    private Preferences pref;
    private Kazisafe kazisafe;
    private String role;
    private String region;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        role = pref.get("priv", "");
        region = pref.get("region", "...");
        configureTable();
        tb_immobilisation.setItems(immobilisations);
        applyPermissions();
    }

    public static ImmobilisationController getInstance() {
        return instance;
    }

    private void configureTable() {
        col_libelle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLibelle()));
        col_categorie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        col_region.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegion()));
        col_date.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateAcquisition() == null ? "-" : c.getValue().getDateAcquisition().toString()));
        col_valeur.setCellValueFactory(c -> new SimpleStringProperty(format(c.getValue().getValeurOrigineUsd())));
        col_residuelle.setCellValueFactory(c -> new SimpleStringProperty(format(c.getValue().getValeurResiduelleUsd())));
        col_duree.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDureeAmortissementMois() == null ? 0 : c.getValue().getDureeAmortissementMois()));
        col_dotation.setCellValueFactory(c -> new SimpleStringProperty(format(c.getValue().dotationMensuelleUsd())));
        col_actif.setCellValueFactory(c -> new SimpleBooleanProperty(Boolean.TRUE.equals(c.getValue().getActif())));
    }

    private void applyPermissions() {
        boolean allowed = hasPermission(PermitTo.CREATE_OPERATION);
        btn_create.setDisable(!allowed);
        btn_create.setOpacity(allowed ? 1d : 0.5d);
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

    @FXML
    private void refresh(MouseEvent event) {
        refresh();
    }

    @FXML
    private void create() {
        if (!hasPermission(PermitTo.CREATE_OPERATION)) {
            MainUI.notify(null, "Permission", "Action non autorisee par vos permissions", 3, "warning");
            return;
        }
        String libelle = txt_libelle.getText();
        if (libelle == null || libelle.isBlank()) {
            MainUI.notify(null, "Validation", "Le libelle est obligatoire", 3, "warning");
            return;
        }
        Immobilisation body = new Immobilisation(DataId.generate());
        body.setLibelle(libelle);
        body.setCategorie(txt_categorie.getText());
        body.setRegion(region);
        body.setDateAcquisition(LocalDate.now());
        body.setValeurOrigineUsd(parse(txt_valeur.getText(), 0d));
        body.setValeurResiduelleUsd(parse(txt_residuelle.getText(), 0d));
        body.setDureeAmortissementMois((int) parse(txt_duree.getText(), 12d));
        body.setActif(true);

        if (kazisafe == null) {
            lbl_status.setText("Mode hors ligne: en attente de synchronisation");
            immobilisations.add(0, body);
            return;
        }
        kazisafe.createImmobilisation(body).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<Immobilisation> call, retrofit2.Response<Immobilisation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    immobilisations.add(0, response.body());
                    Platform.runLater(() -> {
                        lbl_status.setText("Immobilisation enregistree");
                    });
                    clearForm();
                    return;
                }
                Platform.runLater(() -> {
                    lbl_status.setText("Echec creation: " + response.code());
                });

            }

            @Override
            public void onFailure(retrofit2.Call<Immobilisation> call, Throwable throwable) {
                Platform.runLater(() -> {
                    lbl_status.setText("Hors ligne: element garde localement");
                });
                immobilisations.add(0, body);
            }
        });
    }

    @FXML
    private void openAmortissement() {
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
        if (kazisafe == null) {
            lbl_status.setText("Mode hors ligne");
            return;
        }
        kazisafe.getImmobilisations(selectRegion()).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<List<Immobilisation>> call, retrofit2.Response<List<Immobilisation>> response) {
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
            return fallback;
        }
    }

    private void clearForm() {
        txt_libelle.clear();
        txt_categorie.clear();
        txt_valeur.clear();
        txt_residuelle.clear();
        txt_duree.clear();
    }
}
