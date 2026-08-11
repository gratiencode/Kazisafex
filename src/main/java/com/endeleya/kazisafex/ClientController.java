/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.endeleya.kazisafex;

import delegates.ClientAppartenirDelegate;
import delegates.ClientDelegate;
import delegates.ClientOrganisationDelegate;
import delegates.VenteDelegate;
import data.Vente;
import data.core.KazisafeServiceFactory;
import tools.NotificationHandler;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.Entreprise;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.utils.UserRoleRegistry;
import tools.CurrencyConverter;
import tools.ComboBoxAutoCompletion;
import tools.Constants;
import tools.DataId;
import tools.MainUI;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import data.helpers.Role;
import data.network.Kazisafe;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import data.BaseModel;

/**
 * FXML Controller class
 *
 * @author eroot
 */
public class ClientController implements Initializable {

    @FXML
    private TextField tf_nom_client_;
    @FXML
    private TextField tf_phone_client_;
    @FXML
    private TextField tf_adresse_client_;
    @FXML
    private ComboBox<String> cbx_type_client_;
    @FXML
    private TextField tf_search_client;
    @FXML
    private ListView<Client> list_vu_saved_client_;
    @FXML
    private TextField tf_email_client_;
    @FXML
    private Label txt_count_client_;
    @FXML
    Pane paneOrgan;
    @FXML
    TextField tfnomorg;
    @FXML
    TextField tfrccm;
    @FXML
    TextField tfphone;
    @FXML
    TextField tfboitepostal;
    @FXML
    TextField tfmail;
    @FXML
    TextField tfsiteweb;
    @FXML
    TextField tfadresse;
    @FXML
    TextField tfsearch;
    @FXML
    Button btnaddorg;
    @FXML
    ComboBox<String> cbxdomainorg;
    @FXML
    ComboBox<ClientOrganisation> savedOrgs;
    @FXML
    ComboBox<Client> cbx_clients_parent;
    @FXML
    Label txtorgcount;
    @FXML
    ListView<ClientOrganisation> listOrganiz;
    @FXML
    private TextField tf_filter_client_debt;
    @FXML
    private ListView<Client> listvu_clients_debt;
    @FXML
    private TableView<Vente> tb_client_debt_details;
    @FXML
    private TableColumn<Vente, String> col_cli_date;
    @FXML
    private TableColumn<Vente, String> col_cli_numpiece;
    @FXML
    private TableColumn<Vente, String> col_cli_libelle;
    @FXML
    private TableColumn<Vente, Double> col_cli_topay;
    @FXML
    private TableColumn<Vente, Double> col_cli_payed;
    @FXML
    private TableColumn<Vente, Double> col_cli_remained;
    @FXML
    private Label lbl_total_client_debt;
    @FXML
    private ImageView btn_export_cli_pdf;

    private ObservableList<Vente> ls_client_debt_details;
    ResourceBundle bundle;
    Entreprise entreprise;
    Kazisafe kazisafe;
    private final java.util.concurrent.ExecutorService syncExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Kazisafe-Client-SSE");
                t.setDaemon(true);
                return t;
            });

    private static ClientController instance;
    private String devise;

    public ClientController() {
        // db = JpaStorage.getInstance();
        instance = this;
    }

    public static ClientController getInstance() {
        if (instance == null) {
            instance = new ClientController();
        }
        return instance;
    }

    Client choosenClient;
    ClientOrganisation organization;
    ObservableList<ClientOrganisation> orgs;
    ObservableList<Client> lsclts;
    Client choosenParent;
    String token, role;
    Preferences pref;
    String region;
    double tauxDeChange=2350;
    int indexorg = -1;

    public void addClient(Client liv) {
        Client l = ClientDelegate.findClient(liv.getUid());
        if (l == null) {
            l = ClientDelegate.saveClient(liv);
        } else {
            l = ClientDelegate.updateClient(liv);
        }
        if (lsclts != null) {
            final Client toAdd = l;
            Platform.runLater(() -> addClientToListSafe(toAdd));
        }
    }

    /**
     * Adds a client to lsclts only if no client with the same UID already exists.
     * If a client with the same UID is found, it is updated in place.
     * Must be called on the JavaFX Application Thread.
     */
    private void addClientToListSafe(Client client) {
        if (lsclts == null || client == null) {
            return;
        }
        for (int i = 0; i < lsclts.size(); i++) {
            if (lsclts.get(i).getUid().equals(client.getUid())) {
                lsclts.set(i, client); // update in place
                return;
            }
        }
        lsclts.add(client);
    }

    public void addClientOrganisation(ClientOrganisation liv) {
        ClientOrganisation l = ClientOrganisationDelegate.findClientOrganisation(liv.getUid());// db.findByUid(ClientOrganisation.class,
        // liv.getUid());
        if (l == null) {
            l = ClientOrganisationDelegate.saveClientOrganisation(liv);// db.insertOnly(liv);
        } else {
            l = ClientOrganisationDelegate.updateClientOrganisation(liv);// db.updateOnly(liv);
        }
        if (orgs != null) {
            orgs.add(l);
        }
    }

    public void addClientAppart(ClientAppartenir liv) {
        ClientAppartenir l = ClientAppartenirDelegate.findClientAppartenir(liv.getUid());// db.findByUid(ClientAppartenir.class,
        // liv.getUid());
        if (l == null) {
            l = ClientAppartenirDelegate.saveClientAppartenir(liv);// db.insertOnly(liv);
        } else {
            l = ClientAppartenirDelegate.updateClientAppartenir(liv);// db.updateOnly(liv);
        }
    }

    public void setUp(Entreprise eze, String token, String region) {
        this.token = token;
        this.region = region;
        entreprise = eze;
        kazisafe = KazisafeServiceFactory.createService(token);
        // this.db = JpaStorage.getInstance();

        lsclts = FXCollections.observableArrayList(ClientDelegate.findClients());
        orgs = FXCollections.observableArrayList(ClientOrganisationDelegate.findClientOrganisations());

        list_vu_saved_client_.setItems(lsclts);

        cbx_type_client_.setItems(FXCollections.observableArrayList(bundle.getString("consumer"),
                bundle.getString("wholesaler"), bundle.getString("detailor"), bundle.getString("subscriber")));
        cbx_type_client_.getSelectionModel().selectFirst();
        cbxdomainorg.setItems(FXCollections.observableArrayList("Santé", "Service de guardiennage", "Informatique",
                "ONG", "Telecomunication", "Bancaire", "Commerce général", "Agro-Business", "Minier",
                "Agro-alimentaire", "Service public", "Autres"));
        cbx_clients_parent.setItems(lsclts);
        initDebtTab();
        NotificationHandler.setOnDataSyncListener(this::handleSyncFromServer);
        tf_search_client.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.isEmpty()) {
                    list_vu_saved_client_.setItems(lsclts);
                    return;
                }
                ObservableList<Client> rstLocal = FXCollections.observableArrayList();
                for (Client c : lsclts) {
                    if (c.getTypeClient() == null) {
                        continue;
                    }
                    String typecli = c.getTypeClient().equals("#0") ? bundle.getString("consumer")
                            : c.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                            : c.getTypeClient().equals("#2") ? bundle.getString("detailor")
                            : c.getTypeClient().equals("#3") ? bundle.getString("subscriber")
                            : bundle.getString("consumer");
                    String oc = c.getAdresse() + " " + c.getEmail() + " " + c.getNomClient() + " " + c.getPhone() + " "
                            + typecli;
                    if (oc.toUpperCase().contains(newValue.toUpperCase())) {
                        rstLocal.add(c);
                    }
                }
                list_vu_saved_client_.setItems(rstLocal);
                kazisafe.getAllClientByValue(newValue).enqueue(new Callback<List<Client>>() {
                    @Override
                    public void onResponse(Call<List<Client>> call, Response<List<Client>> rspns) {
                        if (rspns.isSuccessful()) {
                            List<Client> clts = rspns.body();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    rstLocal.addAll(clts);
                                }
                            });

                        }
                    }

                    @Override
                    public void onFailure(Call<List<Client>> call, Throwable thrwbl) {
                    }
                });
            }
        });
        if (tfsearch != null) {
            tfsearch.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (listOrganiz == null || orgs == null) {
                        return;
                    }
                    if (newValue.isEmpty()) {
                        listOrganiz.setItems(orgs);
                        return;
                    }
                    ObservableList<ClientOrganisation> rstLocal = FXCollections.observableArrayList();
                    for (ClientOrganisation c : orgs) {
                        String oc = c.getAdresse() + " " + c.getEmailOrganisation() + " " + c.getNomOrganisation() + ""
                                + " " + c.getPhoneOrganisation() + " "
                                + " " + c.getDomaineOrganisation() + ""
                                + " " + c.getBoitePostalOrganisation() + " " + c.getWebsiteOrganisation();
                        if (oc.toUpperCase().contains(newValue.toUpperCase())) {
                            rstLocal.add(c);
                        }
                    }
                    listOrganiz.setItems(rstLocal);
                    Platform.runLater(() -> {
                        if (txtorgcount != null) {
                            txtorgcount.setText(String.format(bundle.getString("xitems"), rstLocal.size()));
                        }
                    });
                }
            });
        }
        listOrganiz.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ClientOrganisation>() {
            @Override
            public void changed(ObservableValue<? extends ClientOrganisation> observable, ClientOrganisation oldValue,
                    ClientOrganisation newValue) {
                if (newValue != null) {
                    organization = newValue;
                    tfnomorg.setText(organization.getNomOrganisation());
                    tfrccm.setText(organization.getRccmOrganisation());
                    tfadresse.setText(organization.getAdresse());
                    tfphone.setText(organization.getPhoneOrganisation());
                    cbxdomainorg.getSelectionModel().select(organization.getDomaineOrganisation());
                    tfboitepostal.setText(organization.getBoitePostalOrganisation());
                    tfsiteweb.setText(organization.getWebsiteOrganisation());
                    tfmail.setText(organization.getEmailOrganisation());
                }
            }
        });
        cbx_clients_parent.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Client>() {
            @Override
            public void changed(ObservableValue<? extends Client> observable, Client oldValue, Client newValue) {
                if (newValue != null) {
                    choosenParent = newValue;
                }
            }
        });
        ContextMenu cm = new ContextMenu();
        MenuItem m1 = new MenuItem(bundle.getString("attach2bill"));
        MenuItem m2 = new MenuItem(bundle.getString("delete"));
        cm.getItems().add(m1);
        cm.getItems().add(m2);
        list_vu_saved_client_.setContextMenu(cm);
        m1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenClient != null) {
                    PaymentController.getInstance().setClient(choosenClient);
                    Stage st = (Stage) list_vu_saved_client_.getScene().getWindow();
                    st.close();
                }
            }
        });
        m2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenClient != null) {
                    Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, bundle.getString("sure2deleteclient"),
                            ButtonType.YES, ButtonType.CANCEL);
                    alertdlg.setTitle(bundle.getString("warning"));
                    alertdlg.setHeaderText(null);
                    Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                    if (showAndWait.get() == ButtonType.YES) {
                        if (UserRoleRegistry.isTrader() || UserRoleRegistry.hasRole("Manager")) {
                            ClientDelegate.deleteClient(choosenClient);
                            lsclts.remove(choosenClient);
                            list_vu_saved_client_.getSelectionModel().clearSelection();
                            choosenClient = null;
                            tf_adresse_client_.clear();
                            tf_email_client_.clear();
                            tf_nom_client_.clear();
                            tf_phone_client_.clear();
                            cbx_type_client_.getSelectionModel().selectFirst();
                            savedOrgs.setValue(null);
                            cbx_clients_parent.setValue(null);
                            MainUI.notify(null, "Succes", "Client supprime avec succes", 3, "info");
                        }
                    }
                }
            }
        });
        MenuItem mMerge = new MenuItem(bundle.containsKey("xbtn.merge_duplicates") ? bundle.getString("xbtn.merge_duplicates") : "Fusionner tous les doublons");
        cm.getItems().add(mMerge);
        mMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, 
                        bundle.containsKey("sure2mergeduplicates") ? bundle.getString("sure2mergeduplicates") : "Êtes-vous sûr de vouloir fusionner tous les clients en double ayant le même nom et téléphone ? Tous leurs historiques seront consolidés.",
                        ButtonType.YES, ButtonType.CANCEL);
                alertdlg.setTitle(bundle.getString("warning"));
                alertdlg.setHeaderText(null);
                Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                if (showAndWait.isPresent() && showAndWait.get() == ButtonType.YES) {
                    java.util.concurrent.Executors.newSingleThreadExecutor().submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int count = ClientDelegate.mergeAllDuplicateClients();
                                javafx.application.Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        lsclts.clear();
                                        List<Client> refreshed = ClientDelegate.findClients();
                                        if (refreshed != null) {
                                            lsclts.addAll(refreshed);
                                        }
                                        list_vu_saved_client_.setItems(lsclts);
                                        if (listvu_clients_debt != null) {
                                            listvu_clients_debt.setItems(lsclts);
                                        }
                                        
                                        list_vu_saved_client_.getSelectionModel().clearSelection();
                                        choosenClient = null;
                                        tf_adresse_client_.clear();
                                        tf_email_client_.clear();
                                        tf_nom_client_.clear();
                                        tf_phone_client_.clear();
                                        cbx_type_client_.getSelectionModel().selectFirst();
                                        savedOrgs.setValue(null);
                                        cbx_clients_parent.setValue(null);
                                        
                                        String successMsg = bundle.containsKey("xduplicates_merged_success") ? bundle.getString("xduplicates_merged_success") : "Doublons fusionnés avec succès !";
                                        MainUI.notify(null, "Succès", successMsg + " (" + count + " fusions)", 4, "info");
                                    }
                                });
                            } catch (Exception ex) {
                                javafx.application.Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainUI.notify(null, "Erreur", "La fusion a échoué: " + ex.getMessage(), 4, "error");
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        MenuItem m3 = new MenuItem(bundle.containsKey("new") ? bundle.getString("new") : "Nouveau");
        cm.getItems().add(m3);
        m3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                list_vu_saved_client_.getSelectionModel().clearSelection();
                choosenClient = null;
                tf_adresse_client_.clear();
                tf_email_client_.clear();
                tf_nom_client_.clear();
                tf_phone_client_.clear();
                cbx_type_client_.getSelectionModel().selectFirst();
                savedOrgs.setValue(null);
                cbx_clients_parent.setValue(null);
            }
        });
        list_vu_saved_client_.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Client>() {
            @Override
            public void changed(ObservableValue<? extends Client> observable, Client oldValue, Client newValue) {
                if (newValue != null) {
                    choosenClient = newValue;
                    tf_adresse_client_.setText(choosenClient.getAdresse());
                    tf_email_client_.setText(choosenClient.getEmail());
                    tf_nom_client_.setText(choosenClient.getNomClient());
                    tf_phone_client_.setText(choosenClient.getPhone());
                    // , ,bundle.getString("detailor"),));
                    String type = choosenClient.getTypeClient().equals("#0") ? bundle.getString("consumer")
                            : choosenClient.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                            : choosenClient.getTypeClient().equals("#2") ? bundle.getString("detailor")
                            : choosenClient.getTypeClient().equals("#3")
                            ? bundle.getString("subscriber")
                            : bundle.getString("consumer");
                    if (choosenClient.getTypeClient().equals("#3")) {
                        savedOrgs.setDisable(false);
                        btnaddorg.setDisable(false);
                        List<ClientAppartenir> clientApps = ClientAppartenirDelegate
                                .findAppartenanceFor(choosenClient.getUid());// db.findClientTools(ClientAppartenir.class,
                        // choosenClient.getUid());
                        orgs.clear();
                        for (ClientAppartenir clientApp : clientApps) {
                            ClientOrganisation orga = ClientOrganisationDelegate
                                    .findClientOrganisation(clientApp.getClientOrganisationId().getUid());
                            orgs.add(orga);
                        }
                        savedOrgs.getSelectionModel().selectFirst();
                    } else {
                        savedOrgs.setDisable(true);
                        btnaddorg.setDisable(true);
                    }
                    cbx_type_client_.setValue(type);
                }
            }
        });
        cbx_type_client_.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                            Number newValue) {
                        if (newValue != null) {
                            int index = newValue.intValue();

                            int size = cbx_type_client_.getItems().size();
                            if (index == (size - 1) && cbx_type_client_.isFocused()) {
                                savedOrgs.setDisable(false);
                                btnaddorg.setDisable(false);
                                cbx_clients_parent.setDisable(false);
                                indexorg = 1;
                            } else {
                                indexorg = -1;
                                savedOrgs.setDisable(true);
                                btnaddorg.setDisable(true);
                                cbx_clients_parent.setDisable(true);
                            }
                        }
                    }
                });
        listOrganiz.setItems(orgs);
        savedOrgs.setItems(orgs);
        new ComboBoxAutoCompletion<>(savedOrgs);
        if (txtorgcount != null) {
            txtorgcount.setText(String.format(bundle.getString("xitems"), orgs.size()));
        }

    }

    @FXML
    private void showorgapane(Event e) {
        paneOrgan.setVisible(true);
    }

    @FXML
    private void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
    }

    @FXML
    private void saveOrganiclient(Event e) {
        if (tfnomorg.getText().isEmpty()
                || tfrccm.getText().isEmpty()
                || tfphone.getText().isEmpty()
                || tfadresse.getText().isEmpty()
                || tfmail.getText().isEmpty()
                || cbxdomainorg.getValue() == null) {
            MainUI.notify(null, bundle.getString("error"), "Completer tout ls champs non facultatif SVP", 3, "error");
            return;
        }
        boolean willcreate = true;
        if (organization == null) {
            organization = new ClientOrganisation(DataId.generate());
        } else {
            willcreate = false;
        }
        organization.setAdresse(tfadresse.getText());
        organization.setBoitePostalOrganisation(tfboitepostal.getText().isEmpty() ? "" : tfboitepostal.getText());
        organization.setDomaineOrganisation(cbxdomainorg.getValue());
        organization.setEmailOrganisation(tfmail.getText());
        organization.setNomOrganisation(tfnomorg.getText());
        organization.setPhoneOrganisation(tfphone.getText());
        organization.setRccmOrganisation(tfrccm.getText().isEmpty() ? "" : tfrccm.getText());
        organization.setRegion(region);
        organization.setWebsiteOrganisation(tfsiteweb.getText().isEmpty() ? "" : tfsiteweb.getText());

        if (willcreate) {
            ClientOrganisation svdo = ClientOrganisationDelegate.saveClientOrganisation(organization); // db.insertAndSync(organization);
            orgs.add(organization);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(svdo, Constants.ACTION_CREATE, Tables.CLIENTORGANISATION);
                    });
        } else {
            ClientOrganisation svdo = ClientOrganisationDelegate.updateClientOrganisation(organization); // db.update(organization);
            orgs.set(orgs.indexOf(organization), organization);
            Executors.newCachedThreadPool()
                    .submit(() -> {
                        Util.sync(svdo, Constants.ACTION_UPDATE, Tables.CLIENTORGANISATION);
                    });
        }
        MainUI.notify(null, "Info", "Organisation enregistree avec success", 3, "info");
        organization = null;
    }

    @FXML
    private void saveClient(Event e) {
        if (tf_adresse_client_.getText().isEmpty() || tf_nom_client_.getText().isEmpty()
                || tf_phone_client_.getText().isEmpty()) {
            MainUI.notify(null, bundle.getString("error"), bundle.getString("fillfield"), 5, "error");
            return;
        }
        int indextype = cbx_type_client_.getSelectionModel().getSelectedIndex();
        if (choosenClient == null) {
            List<Client> lclt = ClientDelegate.findClientByPhone(tf_phone_client_.getText().trim());
            if (!lclt.isEmpty()) {
                MainUI.notify(null, bundle.getString("error"), bundle.getString("clientexist"), 3, "error");
                return;
            }
            Client c = new Client();
            c.setAdresse(tf_adresse_client_.getText().trim());
            c.setEmail(tf_email_client_.getText().trim());
            c.setNomClient(tf_nom_client_.getText().trim());
            c.setPhone(tf_phone_client_.getText().trim());
            c.setTypeClient("#" + indextype);
            if (indextype == 3 && choosenParent == null) {
                choosenParent = ClientDelegate.findAnonymousClient();
            }
            c.setParentId(choosenParent == null ? ClientDelegate.findAnonymousClient() : choosenParent);
            Client svd = ClientDelegate.saveClient(c);
            saveClientByHttp(svd);

            if (indexorg == 1 && savedOrgs.getValue() != null) {
                ClientAppartenir cap = new ClientAppartenir(DataId.generate());
                cap.setClientId(svd);
                cap.setClientOrganisationId(savedOrgs.getValue());
                cap.setDateAppartenir(LocalDate.now());
                cap.setRegion(region);
                ClientAppartenir svap = ClientAppartenirDelegate.saveClientAppartenir(cap);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(svap, Constants.ACTION_CREATE, Tables.CLIENTAPPARTENIR);
                        });
            }
            if (svd != null) {
                addClientToListSafe(svd);
                MainUI.notify(null, bundle.getString("success"), bundle.getString("clientsaved"), 4, "success");
                // Clear fields
                tf_adresse_client_.clear();
                tf_email_client_.clear();
                tf_nom_client_.clear();
                tf_phone_client_.clear();
                cbx_type_client_.getSelectionModel().selectFirst();
                savedOrgs.setValue(null);
                cbx_clients_parent.setValue(null);
            }
        } else {
            List<Client> lclt = ClientDelegate.findClientByPhone(tf_phone_client_.getText().trim());
            if (!lclt.isEmpty() && !lclt.get(0).getUid().equals(choosenClient.getUid())) {
                MainUI.notify(null, bundle.getString("error"), bundle.getString("clientexist"), 3, "error");
                return;
            }
            choosenClient.setAdresse(tf_adresse_client_.getText().trim());
            choosenClient.setEmail(tf_email_client_.getText().trim());
            choosenClient.setNomClient(tf_nom_client_.getText().trim());
            choosenClient.setPhone(tf_phone_client_.getText().trim());
            choosenClient.setTypeClient("#" + indextype);
            if (indextype == 3 && choosenParent == null) {
                choosenParent = ClientDelegate.findAnonymousClient();
            }
            choosenClient.setParentId(choosenParent == null ? ClientDelegate.findAnonymousClient() : choosenParent);
            Client f = ClientDelegate.updateClient(choosenClient);
            if (f != null) {
                saveClientByHttp(f);
                Executors.newCachedThreadPool()
                        .submit(() -> {
                            Util.sync(f, Constants.ACTION_UPDATE, Tables.CLIENT);
                        });
                int idx = lsclts.indexOf(choosenClient);
                if (idx >= 0) {
                    lsclts.set(idx, choosenClient);
                }
                MainUI.notify(null, bundle.getString("success"), "Client modifié avec succès", 4, "success");
                // Clear fields and selection
                list_vu_saved_client_.getSelectionModel().clearSelection();
                choosenClient = null;
                tf_adresse_client_.clear();
                tf_email_client_.clear();
                tf_nom_client_.clear();
                tf_phone_client_.clear();
                cbx_type_client_.getSelectionModel().selectFirst();
                savedOrgs.setValue(null);
                cbx_clients_parent.setValue(null);
            }
        }
    }

    private void configList() {
        list_vu_saved_client_.setCellFactory((ListView<Client> param) -> new ListCell<Client>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.getTypeClient() == null) {
                        return;
                    }
                    String typecli = item.getTypeClient().equals("#0") ? bundle.getString("consumer")
                            : item.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                            : item.getTypeClient().equals("#2") ? bundle.getString("detailor")
                            : item.getTypeClient().equals("#3") ? bundle.getString("subscriber")
                            : bundle.getString("consumer");
                    setText(item.getNomClient() + ", " + item.getPhone() + " " + item.getAdresse() + " : " + typecli);
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(new Image(ClientController.class.getResourceAsStream("/icons/cloud_agent.png")));
                    setGraphic(imageView);
                }
            }

        });
        listOrganiz.setCellFactory((ListView<ClientOrganisation> param) -> new ListCell<ClientOrganisation>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(ClientOrganisation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNomOrganisation() + ", " + item.getDomaineOrganisation() + " "
                            + "" + item.getAdresse() + "  "
                            + (item.getWebsiteOrganisation() != null ? item.getWebsiteOrganisation() : ""));
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(
                            new Image(ClientController.class.getResourceAsStream("/icons/office-building.png")));
                    setGraphic(imageView);
                }
            }

        });
        savedOrgs.setConverter(new StringConverter<ClientOrganisation>() {
            @Override
            public String toString(ClientOrganisation item) {
                return item == null ? null
                        : item.getNomOrganisation() + ", " + item.getDomaineOrganisation() + " "
                        + "" + item.getAdresse() + " "
                        + (item.getWebsiteOrganisation() != null ? item.getWebsiteOrganisation() : "");
            }

            @Override
            public ClientOrganisation fromString(String string) {
                return savedOrgs.getItems()
                        .stream()
                        .filter(item -> (item.getNomOrganisation() + ", " + item.getDomaineOrganisation() + " "
                        + "" + item.getAdresse() + " "
                        + (item.getWebsiteOrganisation() != null ? item.getWebsiteOrganisation() : ""))
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_clients_parent.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client item) {
                return item == null ? null : item.getNomClient() + ", " + item.getPhone() + " " + item.getAdresse();
            }

            @Override
            public Client fromString(String string) {
                return cbx_clients_parent.getItems()
                        .stream()
                        .filter(item -> (item.getNomClient() + ", " + item.getPhone() + " " + item.getAdresse())
                        .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        configList();
        ContextMenu cmenu = new ContextMenu();
        role = UserRoleRegistry.getRole(pref);
        tauxDeChange = CurrencyConverter.activeRate();
        devise = CurrencyConverter.mainCurrency();
        MenuItem mi = new MenuItem("Voir la consommation");
        cmenu.getItems().add(mi);
        mi.setOnAction((event) -> {
            if (organization != null) {
                if (UserRoleRegistry.isTrader() || UserRoleRegistry.hasRole("Manager")
                        || UserRoleRegistry.hasRole("Finance")) {
                    MainUI.floatDialog(tools.Constants.RELEVEE_DLG, 988, 598, token, kazisafe, entreprise,
                            organization);
                } else {
                    MainUI.notify(null, "Erreur", "Vous n'avez pas assez des privileges pour acceder a cette option", 3,
                            "error");
                }
            }

        });
        listOrganiz.setContextMenu(cmenu);
    }

    @FXML
    public void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    @FXML
    public void closeFromOut() {
        Stage st = (Stage) savedOrgs.getScene().getWindow();
        st.close();
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

    private void initDebtTab() {
        ls_client_debt_details = FXCollections.observableArrayList();
        tb_client_debt_details.setItems(ls_client_debt_details);

        // Configuration des colonnes avec contraintes optimisées
        col_cli_date.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDateVente().toLocalDate().toString()));
        col_cli_date.setPrefWidth(120);

        col_cli_numpiece.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReference()));
        col_cli_numpiece.setPrefWidth(150);

        col_cli_libelle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLibelle()));
        col_cli_libelle.setPrefWidth(200);

        col_cli_topay.setCellValueFactory(cellData -> {
            Vente v = cellData.getValue();
            double cash = CurrencyConverter.legacyTotalInMainCurrency(v.getMontantUsd(), v.getMontantCdf());
            double debt = CurrencyConverter.debtInMainCurrency(
                    v.getMontantDette() == null ? 0 : v.getMontantDette(), v.getDeviseDette());
            return new SimpleObjectProperty<>(Math.max(0, cash + debt));
        });
        col_cli_topay.setPrefWidth(100);

        col_cli_payed.setCellValueFactory(cellData -> {
            Vente v = cellData.getValue();
            double payed = CurrencyConverter.legacyTotalInMainCurrency(v.getMontantUsd(), v.getMontantCdf());
            return new SimpleObjectProperty<>(payed);
        });
        col_cli_payed.setPrefWidth(100);

        col_cli_remained
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                        CurrencyConverter.debtInMainCurrency(
                                cellData.getValue().getMontantDette() == null ? 0 : cellData.getValue().getMontantDette(),
                                cellData.getValue().getDeviseDette())));
        col_cli_remained.setPrefWidth(100);

        listvu_clients_debt.setItems(lsclts);
        listvu_clients_debt.setCellFactory(list_vu_saved_client_.getCellFactory());
        ContextMenu cmDebt = new ContextMenu();
        MenuItem mMergeDebt = new MenuItem(bundle.containsKey("xbtn.merge_duplicates") ? bundle.getString("xbtn.merge_duplicates") : "Fusionner tous les doublons");
        cmDebt.getItems().add(mMergeDebt);
        listvu_clients_debt.setContextMenu(cmDebt);
        mMergeDebt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, 
                        bundle.containsKey("sure2mergeduplicates") ? bundle.getString("sure2mergeduplicates") : "Êtes-vous sûr de vouloir fusionner tous les clients en double ayant le même nom et téléphone ? Tous leurs historiques seront consolidés.",
                        ButtonType.YES, ButtonType.CANCEL);
                alertdlg.setTitle(bundle.getString("warning"));
                alertdlg.setHeaderText(null);
                Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                if (showAndWait.isPresent() && showAndWait.get() == ButtonType.YES) {
                    java.util.concurrent.Executors.newSingleThreadExecutor().submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int count = ClientDelegate.mergeAllDuplicateClients();
                                javafx.application.Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        lsclts.clear();
                                        List<Client> refreshed = ClientDelegate.findClients();
                                        if (refreshed != null) {
                                            lsclts.addAll(refreshed);
                                        }
                                        list_vu_saved_client_.setItems(lsclts);
                                        if (listvu_clients_debt != null) {
                                            listvu_clients_debt.setItems(lsclts);
                                        }
                                        
                                        list_vu_saved_client_.getSelectionModel().clearSelection();
                                        choosenClient = null;
                                        tf_adresse_client_.clear();
                                        tf_email_client_.clear();
                                        tf_nom_client_.clear();
                                        tf_phone_client_.clear();
                                        cbx_type_client_.getSelectionModel().selectFirst();
                                        savedOrgs.setValue(null);
                                        cbx_clients_parent.setValue(null);
                                        
                                        String successMsg = bundle.containsKey("xduplicates_merged_success") ? bundle.getString("xduplicates_merged_success") : "Doublons fusionnés avec succès !";
                                        MainUI.notify(null, "Succès", successMsg + " (" + count + " fusions)", 4, "info");
                                    }
                                });
                            } catch (Exception ex) {
                                javafx.application.Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainUI.notify(null, "Erreur", "La fusion a échoué: " + ex.getMessage(), 4, "error");
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });


        tf_filter_client_debt.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                listvu_clients_debt.setItems(lsclts);
                return;
            }
            ObservableList<Client> filtered = FXCollections.observableArrayList();
            for (Client c : lsclts) {
                if (c.getNomClient().toUpperCase().contains(newValue.toUpperCase())
                        || c.getPhone().contains(newValue)) {
                    filtered.add(c);
                }
            }
            listvu_clients_debt.setItems(filtered);
        });

        listvu_clients_debt.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showDebtDetails(newValue);
            }
        });

        ContextMenu cm = new ContextMenu();
        MenuItem miRecov = new MenuItem("Récouvrer cette dette");
        cm.getItems().add(miRecov);
        tb_client_debt_details.setContextMenu(cm);

        miRecov.setOnAction(event -> {
            Vente selected = tb_client_debt_details.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getMontantDette() > 0) {
                    MainuiController.getInstance().switchScreens(Constants.CAISSE_VIEW, "CAISSES", "Trésorerie",
                            "cashier.png", selected, null);
                }
            }
        });

        // Ajout du listener pour la case à cocher de filtre
        if (chk_show_unpaid_only != null) {
            chk_show_unpaid_only.setOnAction(e -> {
                Client selectedClient = listvu_clients_debt.getSelectionModel().getSelectedItem();
                if (selectedClient != null) {
                    showDebtDetails(selectedClient);
                }
            });
        }

        // Configuration de l'icône PDF
        btn_export_cli_pdf.setImage(new Image(ClientController.class.getResourceAsStream("/icons/download-pdf.png")));
        btn_export_cli_pdf.setFitHeight(32);
        btn_export_cli_pdf.setFitWidth(32);
        btn_export_cli_pdf.setPreserveRatio(true);
        Tooltip.install(btn_export_cli_pdf, new Tooltip("Exporter en PDF"));

        btn_export_cli_pdf.setOnMouseClicked(e -> {
            Client c = listvu_clients_debt.getSelectionModel().getSelectedItem();
            if (c != null) {
                exportClientDebtToPdf(c);
            } else {
                MainUI.notify(null, "Attention", "Veuillez séléctionner un client", 3, "warn");
            }
        });
    }

    private void showDebtDetails(Client c) {
        tauxDeChange = CurrencyConverter.activeRate();
        devise = CurrencyConverter.mainCurrency();
        ls_client_debt_details.clear();
        double totalDebt = 0;
        List<Vente> allCreditSales = VenteDelegate.findCreditSales();
        if (allCreditSales != null) {
            for (Vente v : allCreditSales) {
                if (v.getClientId() != null && v.getClientId().getUid().equals(c.getUid())) {
                    // Si la case est cochée, n'afficher que les dettes non soldées
                    if (chk_show_unpaid_only != null && chk_show_unpaid_only.isSelected()) {
                        if (v.getMontantDette() > 0) {
                            ls_client_debt_details.add(v);
                            totalDebt += CurrencyConverter.debtInMainCurrency(
                                    v.getMontantDette(), v.getDeviseDette());
                        }
                    } else {
                        // Sinon afficher toutes les ventes à crédit
                        ls_client_debt_details.add(v);
                        if (v.getMontantDette() > 0) {
                            totalDebt += CurrencyConverter.debtInMainCurrency(
                                    v.getMontantDette(), v.getDeviseDette());
                        }
                    }
                }
            }
        }
        lbl_total_client_debt.setText(CurrencyConverter.formatAmount(totalDebt, devise));
    }

    private void handleSyncFromServer(BaseModel baseModel) {
        if (baseModel == null) {
            return;
        }
        if (!(baseModel instanceof Client)
                && !(baseModel instanceof ClientOrganisation)
                && !(baseModel instanceof ClientAppartenir)
                && !(baseModel instanceof Vente)) {
            return;
        }
        syncExecutor.submit(() -> {
            List<Client> freshClients = ClientDelegate.findClients();
            List<ClientOrganisation> freshOrgs = ClientOrganisationDelegate.findClientOrganisations();
            Platform.runLater(() -> {
                if (lsclts != null && freshClients != null) {
                    lsclts.setAll(freshClients);
                }
                if (orgs != null && freshOrgs != null) {
                    orgs.setAll(freshOrgs);
                }
                if (cbx_clients_parent != null) {
                    cbx_clients_parent.setItems(lsclts);
                }
                if (list_vu_saved_client_ != null) {
                    list_vu_saved_client_.setItems(lsclts);
                }
                if (listvu_clients_debt != null) {
                    listvu_clients_debt.setItems(lsclts);
                }
                Client selected = listvu_clients_debt == null ? null : listvu_clients_debt.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showDebtDetails(selected);
                }
            });
        });
    }

    @FXML
    private CheckBox chk_show_unpaid_only;

    private void exportClientDebtToPdf(Client c) {
        List<Vente> dataCopy = new java.util.ArrayList<>(ls_client_debt_details);
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    File f = Util.exportPdfClientStatement(c, dataCopy, entreprise);
                    javafx.application.Platform.runLater(() -> {
                        if (f != null) {
                            MainUI.notify(null, "Succès", "L'état de dette a été exporté en PDF", 4, "info");
                        } else {
                            MainUI.notify(null, "Erreur", "Echec de l'exportation PDF", 4, "error");
                        }
                    });
                    
                    if (f != null) {
                        try {
                            Desktop.getDesktop().open(f);
                        } catch (IOException ex) {
                            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
    }

    private void saveClientByHttp(Client client) {
        kazisafe.saveByForm(client.getUid(),
                client.getNomClient(),
                client.getPhone(),
                client.getTypeClient(),
                client.getEmail(),
                client.getAdresse(),
                client.getParentId().getUid())
                .enqueue(new Callback<Client>() {
                    @Override
                    public void onResponse(Call<Client> call, Response<Client> response) {
                        System.out.println("Client syncing response " + response);
                    }

                    @Override
                    public void onFailure(Call<Client> call, Throwable t) {
                        System.err.println("Error 00 " + t.getMessage());
                    }
                });
    }

}
