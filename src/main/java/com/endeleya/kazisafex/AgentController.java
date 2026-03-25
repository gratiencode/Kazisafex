package com.endeleya.kazisafex;

import data.Affecter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import data.Employee;
import data.Entreprise;
import data.Permission;
import data.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.Constants;
import tools.MainUI;
import tools.SyncEngine;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.PermissionDelegate;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import tools.DataId;
import data.Presence;
import delegates.PresenceDelegate;
import tools.Util;
import javafx.scene.control.DatePicker;
import data.FingerprintMapping;
import delegates.FingerprintMappingDelegate;
import tools.FingerprintScanner;

/**
 * kazisafe.endeleya.core FXML Controller class
 *
 * @author eroot
 */
public class AgentController implements Initializable {

    public static AgentController getInstance() {
        return instance;
    }

    @FXML
    private TextField tf_nom;
    @FXML
    private TextField tf_prenom;
    @FXML
    private ComboBox<String> cbx_region_affect, cbx_roles_agents;
    @FXML
    private TextField tf_search_field_cloud;
    @FXML
    private TableView<Employee> tbl_hired_agents;
    @FXML
    private TableColumn<Employee, String> col_date;
    @FXML
    private TableColumn<Employee, String> col_nom;
    @FXML
    private TableColumn<Employee, String> col_prenom;
    @FXML
    private TableColumn<Employee, String> col_phone;
    @FXML
    private TableColumn<Employee, String> col_poste;
    @FXML
    private TableColumn<Employee, String> col_region;
    @FXML
    private ListView<User> list_agent_from_cloud;
    @FXML
    private Label txt_phone_agent;
    @FXML
    Pane pane_progress;
    @FXML
    ImageView reafect;
    ResourceBundle bundle;

    Preferences pref;
    Kazisafe kazisafe;
    Entreprise entrepise;
    ObservableList<User> users;
    ObservableList<Employee> employes;
    ObservableList<String> regions;
    String euid;

    User choosenU;
    Employee choosenE;
    data.Role choosenR;

    private static AgentController instance;
    @FXML
    private Label txt_nom_prenom;
    @FXML
    private Label txt_role_attrib;
    @FXML
    private Label txt_region;
    @FXML
    private Label txt_nb_permissions;
    @FXML
    private ListView<Permission> lst_features;
    @FXML
    private ListView<Permission> lst_allowed_features;

    ObservableList<Permission> notallowed, allowed;
    @FXML
    private TabPane tabpn_collaboperm;
    @FXML
    private Tab tab_collabo;
    @FXML
    private Tab tab_permission;

    private Set<Permission> currentUserPermissions;
    @FXML
    private TextField tf_descr_role;
    @FXML
    private AnchorPane anchpn_newrole;
    private boolean enrollmentMode = false;

    @FXML
    private DatePicker dpk_debut_presence;
    @FXML
    private DatePicker dpk_fin_presence;
    @FXML
    private TableView<Presence> tbl_presence;
    @FXML
    private TableColumn<Presence, String> col_pres_date;
    @FXML
    private TableColumn<Presence, String> col_pres_agent;
    @FXML
    private TableColumn<Presence, String> col_pres_type;
    @FXML
    private TableColumn<Presence, String> col_pres_region;

    ObservableList<Presence> presences;

    public AgentController() {
        instance = this;
    }

    Permission choosenPermission;// choosenperm

    public void init(Entreprise entr, Kazisafe kazisafe) {
        this.entrepise = entr;
        this.kazisafe = kazisafe;
        currentUserPermissions = new HashSet();
        loadFingerprintMappings();
        startFingerprintScanner();
        users = FXCollections.observableArrayList();
        employes = FXCollections.observableArrayList();
        notallowed = FXCollections.observableArrayList();
        allowed = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        presences = FXCollections.observableArrayList();
        list_agent_from_cloud.setItems(users);
        tbl_hired_agents.setItems(employes);
        cbx_region_affect.setItems(regions);
        tbl_presence.setItems(presences);
        lst_features.setItems(notallowed);
        lst_allowed_features.setItems(allowed);
        cbx_roles_agents.setItems(FXCollections.observableArrayList(
                bundle.getString("cbx.fondator"),
                bundle.getString("cbx.manager"),
                bundle.getString("cbx.finance"),
                bundle.getString("cbx.magazin"),
                bundle.getString("cbx.vendor"),
                bundle.getString("cbx.magazinall"),
                bundle.getString("cbx.cashall"),
                bundle.getString("cbx.vendorall"),
                bundle.getString("cbx.managerall")));
        lst_features.setCellFactory((ListView<Permission> param) -> new ListCell<Permission>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Permission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getPermissionname());
                            imageView.setFitHeight(12);
                            imageView.setFitWidth(12);
                            imageView.setPreserveRatio(true);
                            imageView.setImage(
                                    new Image(AgentController.class.getResourceAsStream("/icons/lock-padlock.png")));
                            setGraphic(imageView);
                        }
                    });

                }
            }

        });
        lst_allowed_features.setCellFactory((ListView<Permission> param) -> new ListCell<Permission>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Permission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getPermissionname());
                            imageView.setFitHeight(12);
                            imageView.setFitWidth(12);
                            imageView.setPreserveRatio(true);
                            imageView.setImage(
                                    new Image(AgentController.class.getResourceAsStream("/icons/unlock-padlock.png")));
                            setGraphic(imageView);
                        }
                    });

                }
            }

        });

        list_agent_from_cloud.setCellFactory((ListView<User> param) -> new ListCell<User>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setText(item.getPrenom() + ", " + item.getNom() + " " + item.getPhone());
                            imageView.setFitHeight(30);
                            imageView.setFitWidth(30);
                            imageView.setPreserveRatio(true);
                            imageView.setImage(
                                    new Image(AgentController.class.getResourceAsStream("/icons/cloud_agent.png")));
                            setGraphic(imageView);
                        }
                    });

                }
            }

        });
        list_agent_from_cloud.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends User> observable, User oldValue, User newValue) -> {
                    if (newValue != null) {
                        choosenU = newValue;
                        tf_nom.setText(choosenU.getNom());
                        tf_prenom.setText(choosenU.getPrenom());
                        txt_phone_agent.setText(choosenU.getPhone());
                    }
                });
        kazisafe.getRegions().enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> rspns) {
                if (rspns.isSuccessful()) {
                    List<String> lreg = rspns.body();
                    regions.addAll(lreg);
                    int i = 0;
                    for (String reg : lreg) {
                        pref.put("region" + (++i), reg);
                    }
                    System.err.println("Agent regions " + lreg.size());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable thrwbl) {
                for (String key : regKeys()) {
                    String r = pref.get(key, "...");
                    if (!regions.contains(r)) {
                        regions.add(r);
                    }
                }
            }
        });
        refreshAgent(entr.getUid());
        refreshRoles();
        loadPresences();
        Tooltip.install(reafect, new Tooltip(bundle.getString("txt.changeaffectag")));
        txt_nb_permissions.textProperty().bind(Bindings
                .size(lst_allowed_features.getItems()).asString("%d Permissions"));
        lst_features.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Permission>() {
            @Override
            public void changed(ObservableValue<? extends Permission> ov, Permission t, Permission t1) {
                if (t1 != null) {
                    choosenPermission = t1;
                }
            }
        });
        lst_allowed_features.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Permission>() {
            @Override
            public void changed(ObservableValue<? extends Permission> ov, Permission t, Permission t1) {
                if (t1 != null) {
                    choosenPermission = t1;
                }
            }
        });

    }

    private void addAllPerms() {
        allowed.addAll(notallowed);
        notallowed.clear();
    }

    private void addPerm() {
        if (choosenPermission != null) {

            try {

                Response<Affecter> exec = kazisafe.createAffecter(
                        choosenPermission.getUid(),
                        cbx_roles_agents.getSelectionModel().getSelectedItem(),
                        choosenE.getRegion(),
                        choosenE.getEngagementId()).execute();
                if (exec.isSuccessful()) {
                    System.out.println("permission " + choosenPermission + " accordee");
                }

                if (exec.code() == 409) {
                    String value = exec.errorBody().string();
                    MainUI.notify(null, "", value, 5, "error");
                }
            } catch (IOException ex) {
                Logger.getLogger(AgentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            allowed.addAll(choosenPermission);
            notallowed.removeAll(choosenPermission);
            lst_features.getSelectionModel().clearSelection();

        } else {
            MainUI.notify(null, "", "Choisissez un utilisateur avec un role valide!", 4, "info");
        }

    }

    private void removeAllPerms() {
        notallowed.addAll(allowed);
        allowed.clear();
    }

    private void removePerm() {
        if (choosenPermission != null) {
            try {
                System.out.println("preparing - " + choosenPermission.getUid() + " *-* " + choosenE.getEngagementId());
                Response<String> exec = kazisafe
                        .changePermission(choosenPermission.getUid(), choosenE.getEngagementId()).execute();
                System.out.println("Info - " + exec.code());
                if (exec.isSuccessful()) {
                    System.out.println("permission supprimee " + exec.body());
                }
            } catch (IOException ex) {
                Logger.getLogger(AgentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            notallowed.addAll(choosenPermission);
            allowed.removeAll(choosenPermission);
        }
    }

    public void refreshAgent(String entr) {
        pane_progress.setVisible(true);
        kazisafe.findEmployees(entr)
                .enqueue(new Callback<List<Employee>>() {
                    @Override
                    public void onResponse(Call<List<Employee>> call, Response<List<Employee>> rspns) {
                        pane_progress.setVisible(false);
                        System.out.println("Response cloud " + rspns.code());
                        if (rspns.isSuccessful()) {
                            List<Employee> empl = rspns.body();
                            employes.setAll(empl);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Employee>> call, Throwable thrwbl) {
                        pane_progress.setVisible(false);
                        MainUI.notify(null, bundle.getString("error"), bundle.getString("notfinish"), 3, "error");
                    }
                });

    }

    private List<String> regKeys() {
        List<String> result = new ArrayList<>();
        try {

            for (String key : pref.keys()) {
                if (key.startsWith("region")) {
                    result.add(key);
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(DestockController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @FXML
    public void refreshAgents(Event e) {
        refreshAgent(this.entrepise.getUid());
        refreshRoles();

    }

    @FXML
    public void searchuser(Event e) {
        if (tf_search_field_cloud.getText().isEmpty()) {
            return;
        }
        pane_progress.setVisible(true);
        kazisafe.searchUser(tf_search_field_cloud.getText()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> rspns) {
                pane_progress.setVisible(false);
                System.out.println("search response " + rspns.code());
                if (rspns.isSuccessful()) {
                    List<User> usez = rspns.body();
                    MainUI.notify(null, "Info", usez.size() + " " + bundle.getString("itemfound"), 3, "info");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            users.setAll(usez);
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable thrwbl) {
                pane_progress.setVisible(false);
                System.err.println("error " + thrwbl.getMessage());
            }
        });
    }

    public void configTable() {
        col_date.setCellValueFactory((TableColumn.CellDataFeatures<Employee, String> param) -> {
            LocalDate de = param.getValue().getEngagementDate();
            return new SimpleStringProperty(de == null ? "" : de.toString());
        });
        col_nom.setCellValueFactory((TableColumn.CellDataFeatures<Employee, String> param) -> {
            return new SimpleStringProperty(param.getValue().getNom());
        });
        col_prenom.setCellValueFactory((TableColumn.CellDataFeatures<Employee, String> param) -> {
            return new SimpleStringProperty(param.getValue().getPrenom());
        });
        col_phone.setCellValueFactory((TableColumn.CellDataFeatures<Employee, String> param) -> {
            return new SimpleStringProperty(param.getValue().getPhone());
        });
        col_poste.setCellValueFactory((TableColumn.CellDataFeatures<Employee, String> param) -> {
            return new SimpleStringProperty(param.getValue().getPoste());
        });
        col_region.setCellValueFactory((TableColumn.CellDataFeatures<Employee, String> param) -> {
            return new SimpleStringProperty(param.getValue().getRegion());
        });
        tbl_hired_agents.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Employee>() {
            @Override
            public void changed(ObservableValue<? extends Employee> observable, Employee oldValue, Employee newValue) {
                if (newValue != null) {
                    choosenE = newValue;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            tf_nom.setText(choosenE.getNom());
                            tf_prenom.setText(choosenE.getPrenom());
                            txt_phone_agent.setText(choosenE.getPhone());
                            cbx_region_affect.setValue(choosenE.getRegion());
                            cbx_roles_agents.setValue(choosenE.getPoste());
                        }
                    });

                }
            }
        });

        ContextMenu cmenu = new ContextMenu();
        MenuItem menuitem = new MenuItem("Revoquer cet agent");
        MenuItem menuitemperm = new MenuItem("Gerer les permissions");
        cmenu.getItems().add(menuitem);
        cmenu.getItems().add(menuitemperm);
        menuitem.setOnAction((ActionEvent event) -> {
            if (choosenE != null) {
                Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION,
                        "Voulez vous vraiment revoquer " + choosenE.getPrenom() + " " + choosenE.getNom() + " ?",
                        ButtonType.YES, ButtonType.CANCEL);
                alertdlg.setTitle("Attention!");
                alertdlg.setHeaderText(null);

                Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                if (showAndWait.get() == ButtonType.YES) {
                    MainUI.notify(null, "Info", "Révocation de l'agent en cours...", 3, "info");
                    String cuser = pref.get("userid", entrepise.getUid());
                    kazisafe.affectAgent(cuser, choosenE.getUserId(),
                            entrepise.getUid(), cbx_roles_agents.getSelectionModel().getSelectedItem(),
                            "true", cbx_region_affect.getValue(), cbx_roles_agents.getValue(),
                            "Fired", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                            .enqueue(new Callback<Employee>() {
                                @Override
                                public void onResponse(Call<Employee> call, Response<Employee> rspns) {
                                    if (rspns.isSuccessful()) {
                                        Employee fc = rspns.body();
                                        MainUI.notify(null, "Info",
                                                fc.getPrenom() + " " + fc.getNom() + " révoqué avec succès", 4, "info");
                                        employes.remove(choosenE);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Employee> call, Throwable thrwbl) {

                                }
                            });
                }
            }
        });
        menuitemperm.setOnAction((ActionEvent event) -> {
            if (choosenE != null) {
                txt_nom_prenom.setText(choosenE.getPrenom() + " " + choosenE.getNom());
                txt_region.setText(choosenE.getRegion());
                txt_role_attrib.setText(choosenE.getPoste());
                tabpn_collaboperm.getSelectionModel().select(tab_permission);
                getNamedRole(choosenE.getPoste());
                allowed.clear();
                loadThisAgentPerms(choosenE.getEngagementId());

            }
        });
        tbl_hired_agents.setContextMenu(cmenu);
    }

    private void getNamedRole(String nom) {
        kazisafe.getRoleWithName(nom)
                .enqueue(new Callback<data.Role>() {
                    @Override
                    public void onResponse(Call<data.Role> call, Response<data.Role> rspns) {
                        System.out.println("role with name " + rspns.code());
                        if (rspns.isSuccessful()) {
                            choosenR = rspns.body();
                        } else {
                            data.Role role = new data.Role(DataId.generate());
                            role.setRolename(getRole(getRoleIndex(choosenE.getPoste())) == null ? nom
                                    : getRole(getRoleIndex(choosenE.getPoste())).name());
                            kazisafe.createRole(role).enqueue(new Callback<data.Role>() {
                                @Override
                                public void onResponse(Call<data.Role> call, Response<data.Role> rspns) {
                                    System.out.println("New rolenamed " + rspns.code());
                                    if (rspns.isSuccessful()) {
                                        choosenR = rspns.body();
                                    }
                                }

                                @Override
                                public void onFailure(Call<data.Role> call, Throwable thrwbl) {
                                    thrwbl.printStackTrace();
                                }
                            });

                        }
                    }

                    @Override
                    public void onFailure(Call<data.Role> call, Throwable thrwbl) {
                        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                    }
                });
    }

    private void loadThisAgentPerms(String engagementId) {
        kazisafe.loadAgentPermission(engagementId).enqueue(new Callback<List<Permission>>() {
            @Override
            public void onResponse(Call<List<Permission>> call, Response<List<Permission>> rspns) {
                pane_progress.setVisible(false);
                System.out.println("search response " + rspns.code());
                if (rspns.isSuccessful()) {
                    List<Permission> perms = rspns.body();
                    if (perms != null) {
                        currentUserPermissions.clear();
                        Platform.runLater(() -> {
                            perms.forEach((t) -> {
                                if (!allowed.contains(t)) {
                                    allowed.add(t);
                                    currentUserPermissions.add(t);
                                }
                            });
                        });
                    }
                    PermissionDelegate.renewPermissions(perms);
                }
            }

            @Override
            public void onFailure(Call<List<Permission>> call, Throwable thrwbl) {
                pane_progress.setVisible(false);
                System.err.println("error in perms : " + thrwbl.getMessage());
            }
        });
        loadAvailablePerms();
    }

    public void loadAvailablePerms() {
        kazisafe.loadPermissions().enqueue(new Callback<List<Permission>>() {
            @Override
            public void onResponse(Call<List<Permission>> call, Response<List<Permission>> rspns) {
                pane_progress.setVisible(false);
                System.out.println("search response " + rspns.code());
                if (rspns.isSuccessful()) {
                    List<Permission> perms = rspns.body();
                    if (perms != null) {
                        perms.forEach((t) -> {
                            if (!notallowed.contains(t)) {
                                notallowed.add(t);
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(Call<List<Permission>> call, Throwable thrwbl) {
                pane_progress.setVisible(false);
                System.err.println("error in perms : " + thrwbl.getMessage());
            }
        });
    }

    @FXML
    public void reaffect() {
        if (choosenE != null) {
            Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment changer l'affectation de "
                    + choosenE.getPrenom() + " " + choosenE.getNom() + " ?", ButtonType.YES, ButtonType.CANCEL);
            alertdlg.setTitle("Attention!");
            alertdlg.setHeaderText(null);
            Optional<ButtonType> showAndWait = alertdlg.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                MainUI.notify(null, "Info", "Réaffectation de l'agent en cours...", 3, "info");
                String cuser = pref.get("userid", entrepise.getUid());
                kazisafe.affectAgent(cuser, choosenE.getUserId(),
                        entrepise.getUid(), cbx_roles_agents.getSelectionModel().getSelectedItem(),
                        "false", cbx_region_affect.getValue(), cbx_roles_agents.getValue(),
                        "Reafectation", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .enqueue(new Callback<Employee>() {
                            @Override
                            public void onResponse(Call<Employee> call, Response<Employee> rspns) {
                                if (rspns.isSuccessful()) {
                                    Employee fc = rspns.body();
                                    MainUI.notify(null, "Info", "L'affectation de " + fc.getPrenom() + " " + fc.getNom()
                                            + " a été changée avec succès", 4, "info");
                                    employes.remove(choosenE);
                                    refreshAgent(entrepise.getUid());
                                }
                            }

                            @Override
                            public void onFailure(Call<Employee> call, Throwable thrwbl) {
                                // To change body of generated methods, choose Tools | Templates.
                            }
                        });
            }
        } else {
            MainUI.notify(null, "Erreur", "Selectionner un agent puis reessayer ", 4, "error");
        }
    }

    @FXML
    public void hireAgent(Event e) {
        if (tf_nom.getText().isEmpty() || tf_prenom.getText().isEmpty() || cbx_region_affect.getValue().isEmpty()
                || cbx_roles_agents.getValue().isEmpty()) {
            MainUI.notify(null, "Erreur", "Completez tous les champs puis reessayer", 4, "error");
            return;
        }
        MainUI.notify(null, "Info", "Création d'agent en cours...", 3, "info");
        String cuser = pref.get("userid", entrepise.getUid());
        Role rolex = getRole(cbx_roles_agents.getSelectionModel().getSelectedIndex());
        kazisafe.affectAgent(cuser,
                choosenU.getUid(),
                this.entrepise.getUid(),
                rolex != null ? rolex.name() : cbx_roles_agents.getSelectionModel().getSelectedItem(),
                "false", cbx_region_affect.getValue(), cbx_roles_agents.getValue(),
                choosenU.getPrenom(), LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> rspns) {
                        System.err.println("agent save " + rspns.message());
                        if (rspns.isSuccessful()) {
                            Employee e = rspns.body();
                            employes.add(e);
                            MainUI.notify(null, "Info", "Utilisateur engagé avec succès", 4, "info");
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable thrwbl) {

                    }
                });

    }

    private Role getRole(int index) {
        switch (index) {
            case 0:
                return Role.Trader;
            case 1:
                return Role.Manager;
            case 2:
                return Role.Finance;
            case 3:
                return Role.Magazinner;
            case 4:
                return Role.Saler;
            case 7:
                return Role.Saler_ALL_ACCESS;
            case 6:
                return Role.Finance_ALL_ACCESS;
            case 5:
                return Role.Magazinner_ALL_ACCESS;
            case 8:
                return Role.Manager_ALL_ACCESS;
            default:
                return null;
        }
    }

    private int getRoleIndex(String role) {
        return cbx_roles_agents.getItems().indexOf(role);
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

    public void search(String query) {
        if (query == null) {
            refreshAgent(this.entrepise.getUid());
            return;
        }
        ObservableList<Employee> rst = FXCollections.observableArrayList();
        for (Employee emp : employes) {
            String oc = emp.getNom() + " " + emp.getPhone() + " " + emp.getPoste() + " " + emp.getPrenom() + ""
                    + " " + emp.getRegion() + " " + Constants.USER_READABLE_FORMAT.format(emp.getEngagementDate());
            if (oc.toUpperCase().contains(query.toUpperCase())) {
                rst.add(emp);
            }

        }
        tbl_hired_agents.setItems(rst);
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
        euid = pref.get("eUid", "");
        configTable();
        configPresenceTable();
        dpk_debut_presence.setValue(LocalDate.now().minusDays(7));
        dpk_fin_presence.setValue(LocalDate.now());
       
    }

    private void configPresenceTable() {
        col_pres_date.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue().getTimestamp() != null ? param.getValue().getTimestamp().toString() : ""));
        col_pres_agent.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue().getAgentNom() + " " + param.getValue().getAgentPrenom()));
        col_pres_type.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTypePresence()));
        col_pres_region.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRegion()));
    }

    private void loadPresences() {
        presences.setAll(PresenceDelegate.findPresences());
    }

    @FXML
    private void filterPresence(ActionEvent event) {
        if (dpk_debut_presence.getValue() != null && dpk_fin_presence.getValue() != null) {
            LocalDateTime start = dpk_debut_presence.getValue().atStartOfDay();
            LocalDateTime end = dpk_fin_presence.getValue().atTime(23, 59, 59);
            presences.setAll(PresenceDelegate.findPresencesByPeriod(start, end));
        }
    }

    @FXML
    private void exportPresenceToExcel(ActionEvent event) {
        if (!presences.isEmpty()) {
            Util.exportXlsAttendance(presences, entrepise.getNomEntreprise());
        } else {
            MainUI.notify(null, "Info", "Aucune donnée à exporter", 3, "info");
        }
    }

    @FXML
    private void simulateFingerprintScan(ActionEvent event) {
        if (choosenE != null) {
            String hash = UUID.randomUUID().toString();
            handleScan(hash);
        } else {
            MainUI.notify(null, "Attention", "Veuillez sélectionner un agent dans la liste des collaborateurs", 4,
                    "warning");
        }
    }

    private void startFingerprintScanner() {
        FingerprintScanner.getInstance().startListening(this::handleScan);
    }

    private void loadFingerprintMappings() {
        List<FingerprintMapping> maps = FingerprintMappingDelegate.findAll();
        // Since employees are loaded asynchronisly, we might need to apply mappings
        // later
        // or just rely on findAgentByHash querying the Delegate
    }

    private void handleScan(String hash) {
        Platform.runLater(() -> {
            if (enrollmentMode) {
                if (choosenE != null) {
                    FingerprintMapping mapping = new FingerprintMapping(choosenE.getUserId(), hash);
                    mapping.setRegion(choosenE.getRegion());
                    mapping.setEntreprise(entrepise.getUid());

                    if (FingerprintMappingDelegate.isExists(choosenE.getUserId())) {
                        FingerprintMappingDelegate.updateMapping(mapping);
                    } else {
                        FingerprintMappingDelegate.createMapping(mapping);
                    }

                    choosenE.setFingerprintHash(hash);
                    MainUI.notify(null, "Info", "Empreinte enregistrée pour " + choosenE.getNom(), 4, "info");
                    enrollmentMode = false;
                }
            } else {
                FingerprintMapping mapping = FingerprintMappingDelegate.findByHash(hash);
                if (mapping != null) {
                    Employee emp = findAgentById(mapping.getAgentId());
                    if (emp != null) {
                        recordPresence(emp, hash);
                    } else {
                        // If not in memory, try to find in DB or just use mapping info
                        // But recordPresence needs Employee for names
                        // We can create a dummy employee from mapping if needed
                        MainUI.notify(null, "Attention", "Agent non trouvé dans la liste actuelle", 4, "warning");
                    }
                } else {
                    MainUI.notify(null, "Attention", "Empreinte inconnue", 4, "warning");
                }
            }
        });
    }

    private Employee findAgentById(String id) {
        for (Employee e : employes) {
            if (id.equals(e.getUserId())) {
                return e;
            }
        }
        return null;
    }

    private Employee findAgentByHash(String hash) {
        FingerprintMapping mapping = FingerprintMappingDelegate.findByHash(hash);
        if (mapping != null) {
            return findAgentById(mapping.getAgentId());
        }
        return null;
    }

    private void recordPresence(Employee emp, String hash) {
        Presence p = new Presence();
        p.setAgentId(emp.getUserId());
        p.setAgentNom(emp.getNom());
        p.setAgentPrenom(emp.getPrenom());
        p.setEntreprise(entrepise.getUid());
        p.setRegion(emp.getRegion());
        p.setTimestamp(LocalDateTime.now());
        p.setFingerprintHash(hash);

        List<Presence> last = PresenceDelegate.findPresencesByAgent(emp.getUserId());
        if (last != null && !last.isEmpty() && "CHECK_IN".equals(last.get(last.size() - 1).getTypePresence())) {
            p.setTypePresence("CHECK_OUT");
        } else {
            p.setTypePresence("CHECK_IN");
        }

        PresenceDelegate.savePresence(p);
        presences.add(0, p);
        MainUI.notify(null, "Succès",
                "Empreinte scannée pour " + p.getAgentNom() + " (" + p.getTypePresence() + ")", 4, "info");
    }

    @FXML
    private void enrollFingerprint(ActionEvent event) {
        if (choosenE != null) {
            enrollmentMode = true;
            MainUI.notify(null, "Info", "Veuillez scanner l'empreinte pour " + choosenE.getNom(), 5, "info");
        } else {
            MainUI.notify(null, "Attention", "Veuillez sélectionner un agent", 4, "warning");
        }
    }

    @FXML
    private void savePermissions(ActionEvent event) {

    }

    @FXML
    public void closeFloatingPane(Event evt) {
        Node n = (Node) evt.getSource();
        Parent p = n.getParent();
        p.setVisible(false);
    }

    @FXML
    private void addAllPermissions(ActionEvent event) {
        addAllPerms();
        loadThisAgentPerms(choosenE.getEngagementId());
    }

    @FXML
    private void addPermission(ActionEvent event) {
        addPerm();
        loadThisAgentPerms(choosenE.getEngagementId());
    }

    @FXML
    private void removePermission(ActionEvent event) {
        removePerm();
    }

    @FXML
    private void removeAllPermissions(ActionEvent event) {
        removeAllPerms();
        // loadThisAgentPerms(choosenE.getEngagementId());
    }

    public Set<Permission> getCurrentUserPermissions() {
        return currentUserPermissions;
    }

    @FXML
    private void createRole(ActionEvent event) {
        if (!tf_descr_role.getText().isEmpty()) {
            data.Role role = new data.Role(DataId.generate());
            role.setRolename(tf_descr_role.getText());
            kazisafe.createRole(role).enqueue(new Callback<data.Role>() {
                @Override
                public void onResponse(Call<data.Role> call, Response<data.Role> rspns) {
                    if (rspns.isSuccessful()) {
                        data.Role r = rspns.body();
                        cbx_roles_agents.getItems()
                                .add(r.getRolename());
                        MainUI.notify(null, "", "Role cree avec succes", 5, "info");
                    }
                }

                @Override
                public void onFailure(Call<data.Role> call, Throwable thrwbl) {
                    thrwbl.printStackTrace();
                }
            });
        } else {
            MainUI.notify(null, "", "Veuillez preciser la desrciption", 5, "error");
        }
    }

    @FXML
    private void showRolePane(ActionEvent event) {
        anchpn_newrole.setVisible(true);
    }

    private void refreshRoles() {
        kazisafe.getRoles().enqueue(new Callback<List<data.Role>>() {
            @Override
            public void onResponse(Call<List<data.Role>> call, Response<List<data.Role>> rspns) {
                System.out.println("Reponse role " + rspns.code());
                if (rspns.isSuccessful()) {
                    List<data.Role> rls = rspns.body();
                    for (data.Role rl : rls) {
                        cbx_roles_agents.getItems()
                                .add(rl.getRolename());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<data.Role>> call, Throwable thrwbl) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                               // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
        loadAvailablePerms();
    }

}
