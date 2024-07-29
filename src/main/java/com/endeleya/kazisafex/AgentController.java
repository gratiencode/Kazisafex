package com.endeleya.kazisafex;

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
import javafx.event.EventHandler;
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
import data.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tools.Constants;
import tools.MainUI;
import tools.SyncEngine;
import data.helpers.Role; 
import data.network.Kazisafe;

/**kazisafe.endeleya.core
 * FXML Controller class
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

    private static AgentController instance;

    public AgentController() {
        instance = this;
    }

    public void init(Entreprise entr, Kazisafe kazisafe) {
        this.entrepise = entr;
        this.kazisafe = kazisafe;
        users = FXCollections.observableArrayList();
        employes = FXCollections.observableArrayList();
        regions = FXCollections.observableArrayList();
        list_agent_from_cloud.setItems(users);
        tbl_hired_agents.setItems(employes);
        cbx_region_affect.setItems(regions);
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
                            imageView.setImage(new Image(AgentController.class.getResourceAsStream("/icons/cloud_agent.png")));
                            setGraphic(imageView);
                        }
                    });

                }
            }

        });
        list_agent_from_cloud.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends User> observable, User oldValue, User newValue) -> {
            if (newValue != null) {
                choosenU = newValue;
                tf_nom.setText(choosenU.getNom());
                tf_prenom.setText(choosenU.getPrenom());
                txt_phone_agent.setText(choosenU.getPhone());
            }
        });
        kazisafe.getRegions(euid).enqueue(new retrofit2.Callback<List<String>>() {
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
        Tooltip.install(reafect, new Tooltip(bundle.getString("txt.changeaffectag")));
    }

    public void refreshAgent(String entr) {
        pane_progress.setVisible(true);
        kazisafe.findEmployees(entr)
                .enqueue(new Callback<List<Employee>>() {
                    @Override
                    public void onResponse(Call<List<Employee>> call, Response<List<Employee>> rspns) {
                        pane_progress.setVisible(false);
                        System.out.println("Response cloud " + rspns.message());
                        if (rspns.isSuccessful()) {
                            List<Employee> empl = rspns.body();
                            employes.setAll(empl);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Employee>> call, Throwable thrwbl) {
                        pane_progress.setVisible(false);
                        MainUI.notify(null, bundle.getString("error"),bundle.getString("notfinish") , 3, "error");
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
                System.out.println("search response " + rspns.message());
                if (rspns.isSuccessful()) {
                    List<User> usez = rspns.body();
                    MainUI.notify(null, "Info", usez.size() + " "+bundle.getString("itemfound"), 3, "info");
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
            Date de = param.getValue().getEngagementDate();
            return new SimpleStringProperty(de == null ? "" : Constants.USER_READABLE_FORMAT.format(de));
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
        cmenu.getItems().add(menuitem);
        menuitem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (choosenE != null) {
                    Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment revoquer " + choosenE.getPrenom() + " " + choosenE.getNom() + " ?", ButtonType.YES, ButtonType.CANCEL);
                    alertdlg.setTitle("Attention!");
                    alertdlg.setHeaderText(null);

                    Optional<ButtonType> showAndWait = alertdlg.showAndWait();
                    if (showAndWait.get() == ButtonType.YES) {
                        MainUI.notify(null, "Info", "Révocation de l'agent en cours...", 3, "info");
                        String cuser = pref.get("userid", entrepise.getUid());
                        kazisafe.affectAgent(cuser, choosenE.getUserId(),
                                entrepise.getUid(), getRole(cbx_roles_agents.getSelectionModel().getSelectedIndex()).name(),
                                "true", cbx_region_affect.getValue(), cbx_roles_agents.getValue(),
                                "Fired", (new Date()).getTime()).enqueue(new Callback<Employee>() {
                            @Override
                            public void onResponse(Call<Employee> call, Response<Employee> rspns) {
                                if (rspns.isSuccessful()) {
                                    Employee fc = rspns.body();
                                    MainUI.notify(null, "Info", fc.getPrenom() + " " + fc.getNom() + " révoqué avec succès", 4, "info");
                                    employes.remove(choosenE);
                                }
                            }

                            @Override
                            public void onFailure(Call<Employee> call, Throwable thrwbl) {
                               
                            }
                        });
                    }
                }
            }
        });
        tbl_hired_agents.setContextMenu(cmenu);
    }

    @FXML
    public void reaffect() {
        if (choosenE != null) {
            Alert alertdlg = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment changer l'affectation de " + choosenE.getPrenom() + " " + choosenE.getNom() + " ?", ButtonType.YES, ButtonType.CANCEL);
            alertdlg.setTitle("Attention!");
            alertdlg.setHeaderText(null);
            Optional<ButtonType> showAndWait = alertdlg.showAndWait();
            if (showAndWait.get() == ButtonType.YES) {
                MainUI.notify(null, "Info", "Réaffectation de l'agent en cours...", 3, "info");
                String cuser = pref.get("userid", entrepise.getUid());
                kazisafe.affectAgent(cuser, choosenE.getUserId(),
                        entrepise.getUid(), getRole(cbx_roles_agents.getSelectionModel().getSelectedIndex()).name(),
                        "false", cbx_region_affect.getValue(), cbx_roles_agents.getValue(),
                        "Reafectation", (new Date()).getTime()).enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> rspns) {
                        if (rspns.isSuccessful()) {
                            Employee fc = rspns.body();
                            MainUI.notify(null, "Info", "L'affectation de " + fc.getPrenom() + " " + fc.getNom() + " a été changée avec succès", 4, "info");
                            employes.remove(choosenE);
                            refreshAgent(entrepise.getUid());
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable thrwbl) {
                        //To change body of generated methods, choose Tools | Templates.
                    }
                });
            }
        } else {
            MainUI.notify(null, "Erreur", "Selectionner un agent puis reessayer ", 4, "error");
        }
    }

    @FXML
    public void hireAgent(Event e) {
        if (tf_nom.getText().isEmpty() || tf_prenom.getText().isEmpty() || cbx_region_affect.getValue().isEmpty() || cbx_roles_agents.getValue().isEmpty()) {
            MainUI.notify(null, "Erreur", "Completez tous les champs puis reessayer", 4, "error");
            return;
        }
        MainUI.notify(null, "Info", "Création d'agent en cours...", 3, "info");
        String cuser = pref.get("userid", entrepise.getUid());
        kazisafe.affectAgent(cuser,
                choosenU.getUid(),
                this.entrepise.getUid(),
                getRole(cbx_roles_agents.getSelectionModel().getSelectedIndex()).name(),
                "false", cbx_region_affect.getValue(), cbx_roles_agents.getValue(),
                choosenU.getPrenom(), (new Date()).getTime()).enqueue(new Callback<Employee>() {
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
               return Role.Saler;
        }
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
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle=rb;
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        euid=pref.get("eUid", "");
        configTable();
    }

}
