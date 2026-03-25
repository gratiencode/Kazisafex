/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import com.endeleya.kazisafex.MainuiController;
import com.endeleya.kazisafex.PaymentController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.output.PrinterOutputStream;
import data.Client;
import data.CompteTresor;
import data.Entreprise;
import data.LigneVente;
import data.Mesure;
import data.Produit;
import data.ProduitHelper;
import data.Vente;
import data.VenteHelper;
import data.helpers.Role;
import data.network.Kazisafe;
import delegates.ClientDelegate;
import delegates.CompteTresorDelegate;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.print.Printer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import org.apache.commons.lang3.time.DateUtils;
import org.controlsfx.tools.Platform;
import retrofit2.Response;

/**
 *
 * @author endeleya
 */
public class CompactMode {

    OnCartValueChangedListener onCartValueChangedListener;
    ObservableList<LigneVente> saleitems = FXCollections.observableArrayList();
    ObservableList<Client> clients = FXCollections.observableArrayList();
    ObservableList<Produit> produits = FXCollections.observableArrayList();
    ObservableList<CompteTresor> compteTresors = FXCollections.observableArrayList();
    ObservableList<Mesure> mesures = FXCollections.observableArrayList();
    SerialPort chport;
    static String REGION;
    private double somme;
    Kazisafe kazisafe;
    private Printer defaultPrinter;
    private int title_s, identite_s, body_s, line_dashes;
    private Entreprise entreprise;
    Preferences pref;
    int count_logic;
    int copies;
    String entrepName;
    String rccm;
    String adresse;
    String email;
    String idNat;
    String nif;
    String phonez;
    private Double taux2change;
    private String user;
    private String devise;

    public CompactMode(Kazisafe kazisafe, Preferences pref) {
        this.kazisafe = kazisafe;
        this.pref = pref;
        this.taux2change = pref.getDouble("taux2change", 2800);
        devise = pref.get("mainCur", "USD");
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public void setOnCartValueChangedListener(OnCartValueChangedListener onCartValueChangedListener) {
        this.onCartValueChangedListener = onCartValueChangedListener;
    }

    public static String getRegion() {
        return REGION;
    }

    public static void setRegion(String region) {
        CompactMode.REGION = region;
    }

    public void addArticle(LigneVente lv) {
        if (!isLigneVenteExist(lv)) {
            saleitems.add(lv);
            somme += lv.getMontantUsd();
            Executors.newSingleThreadExecutor()
                    .submit(() -> {
                        Produit fpro = ProduitDelegate.findProduit(lv.getProductId().getUid());
                        List<Mesure> lsm = MesureDelegate.findMesureByProduit(fpro.getUid());
                        sendProduitIfNotExist(fpro, lsm);
                    });
        }
    }

    public double getSomme() {
        return somme;
    }

    public void removeArticle(LigneVente lv) {
        saleitems.remove(lv);
        somme -= lv.getMontantUsd();
    }

    public void addClient(Client c) {
        clients.add(c);
    }

    public void addAllClient(Collection<Client> c) {
        clients.addAll(c);
    }

    public void removeClient(Client c) {
        clients.remove(c);
    }

    public void initialiseDesplayPort() {
        SerialPort[] ps = SerialPort.getCommPorts();
        if (ps.length > 0) {
            if (Platform.getCurrent().equals(Platform.WINDOWS)) {
                String port = pref.get("display_port", "COM2");
                chport = SerialPort.getCommPort(port);
                chport.setComPortParameters(9600, 8, 1, 0);
                System.out.println("Port configured : " + port);
            }
        }
    }

    public void addCompteTresor(CompteTresor c) {
        compteTresors.add(c);
    }

    public void addAllCompteTresor(Collection<CompteTresor> c) {
        compteTresors.addAll(c);
    }

    public void removeCompteTresor(CompteTresor c) {
        compteTresors.remove(c);
    }

    public void addProduit(Produit c) {
        produits.add(c);
    }

    public void addAllProduit(Collection<Produit> c) {
        produits.addAll(c);
    }

    public void removeProduit(Produit c) {
        produits.remove(c);
    }

    public void addMesure(Mesure c) {
        mesures.add(c);
    }

    public ObservableList<LigneVente> getSaleitems() {
        return saleitems;
    }

    public void addAllArticle(Collection<LigneVente> articles) {
        saleitems.addAll(articles);
        for (LigneVente lv : articles) {
            somme += lv.getMontantUsd();
        }
    }

    private ObservableList<Printer> setToList(ObservableSet<Printer> osp) {
        ObservableList<Printer> rst = FXCollections.observableArrayList();
        for (Printer p : osp) {
            rst.add(p);
        }
        return rst;
    }

    public void printToDisplay(String text) {
        if (chport != null) {
            chport.openPort();
            if (chport.isOpen()) {
                byte[] clear = { 0x0C };
                chport.writeBytes(clear, clear.length);
                chport.closePort();
                chport.openPort();
                byte[] data = text.getBytes();
                chport.writeBytes(data, data.length);
                chport.closePort();
            }
        }
    }

    public void addAllMesure(Collection<Mesure> c) {
        mesures.addAll(c);
    }

    public void setMesures(Collection<Mesure> c) {
        mesures.setAll(c);
    }

    public void removeMesure(Mesure c) {
        mesures.remove(c);
    }

    public static Produit searchProductByCodebar(String codebar) {
        return ProduitDelegate.findByCodebar(codebar);
    }

    public static List<Client> getAvailableClients() {
        return ClientDelegate.findClients();
    }

    public static Client getAnonymousClient() {
        return ClientDelegate.findAnonymousClient();
    }

    public static List<CompteTresor> getTraisoryAccounts() {
        return CompteTresorDelegate.findCompteTresors();
    }

    public static List<CompteTresor> getTraisoryAccountForRegion() {
        return CompteTresorDelegate.findCompteTresors(REGION);
    }

    public static double getSoldeUsdCash(String compteId) {
        return TraisorerieDelegate.findCurrentBalanceUsd(compteId, LocalDate.now(), LocalDate.now(), REGION);
    }

    public static double getSoldeCdfCash(String compteId) {
        return TraisorerieDelegate.findCurrentBalanceCdf(compteId, LocalDate.now(), LocalDate.now(), REGION);
    }

    public static double getVolumeTotalCredit() {
        return TraisorerieDelegate.getCurentCreance();
    }

    public static double getVolumeTotalCredit(String clientId) {
        return TraisorerieDelegate.getCreditForCustomer(clientId);
    }

    public static double getVolumeTotalVente(String clientId) {
        return VenteDelegate.getVenteFor(clientId);
    }

    public void initializeTable(TableView<LigneVente> table,
            TableColumn<LigneVente, Number> col_quant,
            TableColumn<LigneVente, Number> col_prix_unit,
            TableColumn<LigneVente, Number> col_prixtot,
            TableColumn<LigneVente, String>... otherStringCols) {
        TableColumn<LigneVente, String> tc_codebar = otherStringCols[0];
        TableColumn<LigneVente, String> tc_nomprod = otherStringCols[1];
        TableColumn<LigneVente, String> tc_marque = otherStringCols[2];
        TableColumn<LigneVente, String> tc_modele = otherStringCols[3];
        TableColumn<LigneVente, String> tc_taille = otherStringCols[4];
        TableColumn<LigneVente, String> tc_color = otherStringCols[5];
        TableColumn<LigneVente, String> tc_mesure = otherStringCols[6];
        TableColumn<LigneVente, String> tc_numlot = otherStringCols[7];
        TableColumn<LigneVente, String> tc_region = otherStringCols[8];
        table.setItems(saleitems);
        col_quant.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, Number> param) -> {
            LigneVente p = param.getValue();
            return new SimpleDoubleProperty(p.getQuantite());
        });
        col_quant.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        col_quant.setOnEditCommit((TableColumn.CellEditEvent<LigneVente, Number> event) -> {
            LigneVente vl = event.getRowValue();
            somme -= vl.getMontantUsd();
            double q = (double) event.getNewValue().doubleValue();
            vl.setQuantite(q);
            vl.setMontantUsd(q * vl.getPrixUnit());
            vl.setMontantCdf(q * (vl.getPrixUnit() * taux2change));
            saleitems.set(indexOf(vl), vl);
            somme += vl.getMontantUsd();
            notify(vl, somme);

        });
        col_prix_unit.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, Number> param) -> {
            LigneVente p = param.getValue();
            return new SimpleDoubleProperty(p.getPrixUnit());
        });
        col_prix_unit.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        col_prix_unit.setOnEditCommit((TableColumn.CellEditEvent<LigneVente, Number> event) -> {
            LigneVente vl = event.getRowValue();
            somme -= vl.getMontantUsd();
            double pvunit = (double) event.getNewValue().doubleValue();
            vl.setPrixUnit(pvunit);
            vl.setMontantUsd(pvunit * vl.getQuantite());
            vl.setMontantCdf(vl.getQuantite() * (pvunit * taux2change));
            saleitems.set(indexOf(vl), vl);
            somme += vl.getMontantUsd();
            notify(vl, somme);
        });
        col_prixtot.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, Number> param) -> {
            LigneVente p = param.getValue();
            return new SimpleDoubleProperty(p.getMontantUsd());
        });
        tc_codebar.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getCodebar());
        });
        tc_nomprod.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getNomProduit());
        });
        tc_marque.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getMarque());
        });
        tc_modele.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getModele());
        });
        tc_taille.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getTaille());
        });
        tc_color.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Produit p = lv.getProductId();
            return new SimpleStringProperty(p.getCouleur());
        });
        tc_mesure.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            Mesure m = lv.getMesureId();
            return new SimpleStringProperty(m.getDescription());
        });
        tc_numlot.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            LigneVente lv = param.getValue();
            return new SimpleStringProperty(lv.getNumlot());
        });
        tc_region.setCellValueFactory((TableColumn.CellDataFeatures<LigneVente, String> param) -> {
            return new SimpleStringProperty(REGION);
        });
        title_s = pref.getInt("print-title-size", 1);
        body_s = pref.getInt("print-body-size", 1);
        identite_s = pref.getInt("print-identite-size", 1);
        line_dashes = pref.getInt("print-lines-dashcount", 48);
        count_logic = pref.getInt("count-logic", 0);
        copies = pref.getInt("bill-copy", 1);
        entrepName = pref.get("ent_name", "unknown");
        rccm = pref.get("ent_ID", "Aucun");
        adresse = pref.get("ent_adresse", "aucune");
        email = pref.get("ent_email", "");
        idNat = pref.get("ent_idnat", "Aucun");
        nif = pref.get("ent_impot", "Aucun");
        phonez = pref.get("ent_phones", "");
    }

    public int indexOf(LigneVente vl) {
        return saleitems.indexOf(vl);
    }

    public void initProductList(ComboBox<Produit> cbx_product) {
        cbx_product.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit object) {
                return object == null ? null
                        : object.getNomProduit() + " " + (object.getMarque() == null ? "" : object.getMarque()) + " "
                                + (object.getModele() == null ? "" : object.getModele()) + " "
                                + (object.getTaille() == null ? "" : object.getTaille()) + " "
                                + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar();
            }

            @Override
            public Produit fromString(String string) {
                return cbx_product.getItems()
                        .stream()
                        .filter(object -> (object.getNomProduit() + " "
                                + (object.getMarque() == null ? "" : object.getMarque()) + " "
                                + (object.getModele() == null ? "" : object.getModele()) + " "
                                + (object.getTaille() == null ? "" : object.getTaille()) + " "
                                + (object.getCouleur() == null ? "" : object.getCouleur()) + " " + object.getCodebar())
                                .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_product.setItems(produits);
        ComboBoxAutoCompletion<Produit> comx = new ComboBoxAutoCompletion<>(cbx_product);
    }

    public void initMesureList(ComboBox<Mesure> cbx_mesure) {
        cbx_mesure.setConverter(new StringConverter<Mesure>() {
            @Override
            public String toString(Mesure object) {
                return object == null ? null : object.getDescription();
            }

            @Override
            public Mesure fromString(String string) {
                return cbx_mesure.getItems()
                        .stream()
                        .filter(f -> (f.getDescription())
                                .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_mesure.setItems(mesures);
        cbx_mesure.getSelectionModel().selectFirst();
    }

    public void initClientList(ComboBox<Client> cbx_clients) {
        cbx_clients.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client object) {
                return object == null ? null
                        : object.getNomClient() + " " + (object.getPhone() == null ? ""
                                : (object.getPhone().length() < 8 ? "" : object.getPhone()));
            }

            @Override
            public Client fromString(String string) {
                return cbx_clients.getItems()
                        .stream()
                        .filter(v -> (v.getNomClient() + " " + v.getPhone())
                                .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        ComboBoxAutoCompletion<Client> comboBoxAutoCompletion = new ComboBoxAutoCompletion<>(cbx_clients);
        cbx_clients.setItems(clients);

    }

    public void initTresorAccountList(ComboBox<CompteTresor> cbx_comptes) {
        cbx_comptes.setConverter(new StringConverter<CompteTresor>() {
            @Override
            public String toString(CompteTresor object) {
                return object == null ? null
                        : object.getTypeCompte() + " " + object.getBankName() + " " + object.getNumeroCompte();
            }

            @Override
            public CompteTresor fromString(String string) {
                return cbx_comptes.getItems()
                        .stream()
                        .filter(object -> (object.getTypeCompte() + " " + object.getBankName() + " "
                                + object.getNumeroCompte())
                                .contains(string))
                        .findFirst().orElse(null);
            }
        });
        cbx_comptes.setItems(compteTresors);
        ComboBoxAutoCompletion<CompteTresor> comx = new ComboBoxAutoCompletion<>(cbx_comptes);
        cbx_comptes.getSelectionModel().selectFirst();
    }

    private void notify(LigneVente lv, double som) {
        if (this.onCartValueChangedListener != null) {
            this.onCartValueChangedListener.onCartValueChanged(lv, som);
        }
    }

    public void clearComptactCart() {
        saleitems.clear();
        somme = 0;
    }

    public void tryToSaveSale(final String transaction,
            final CompteTresor ct,
            final Client client,
            final Vente vente,
            final List<LigneVente> lignes) {
        Executors.newSingleThreadExecutor()
                .submit(() -> {

                    int retries = 0;
                    while (retries < 9) {
                        try {
                            vente.setClientId(client);
                            System.out.println("T3333333333 Client phone " + client.getPhone());
                            Response<Vente> rep = saveVenteByHttp(vente, client, ct, transaction, lignes);
                            if (rep == null) {
                                System.out.println("Rep est NULL" + rep);
                                continue;
                            }
                            int reponse = rep.code();
                            System.out.println("Reponse de vente " + reponse);
                            if (reponse == 417) {
                                // client
                                System.out.println("T3 Client " + reponse + " " + client.getPhone());
                                List<Client> cs = ClientDelegate.findClientByPhone(client.getPhone());
                                if (!cs.isEmpty()) {
                                    System.err.println("Clients is Empty");
                                    Client c = cs.get(0);
                                    boolean client_saved = saveClientByHttp(c);
                                    System.out.println("Client enregistre : " + (client_saved ? "OK" : "OOps! error"));
                                } else {
                                    Client sc = ClientDelegate.saveClient(client);
                                    System.out.println("Save clt " + sc.getPhone());
                                }
                            } else if (reponse == 412) {
                                // compte tresor
                                System.out.println("T3 Compte Tresor " + reponse);
                                List<CompteTresor> comptes = CompteTresorDelegate
                                        .findByNumeroCompte(ct.getNumeroCompte());
                                if (!comptes.isEmpty()) {
                                    System.err.println("After if compte tres");
                                    CompteTresor compte = comptes.get(0);
                                    // saveCompte(compte);
                                    // Executors.newCachedThreadPool()
                                    // .submit(() -> {
                                    Util.sync(compte, Constants.ACTION_CREATE, Tables.COMPTETRESOR);
                                    // });
                                }
                            } else if (reponse == 200) {
                                System.out.println("Vente enregistree au serveur avec succes");
                                break;
                            } else {
                                System.out.println("Reponse par defaut " + reponse);
                            }
                            retries++;

                            try {
                                TimeUnit.MILLISECONDS.sleep(200 * (long) Math.pow(2, retries)); // Delai exponentiel
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } catch (IOException ex) {
                            System.out.println("T3 ERROR " + ex.getMessage());
                            Logger.getLogger(PaymentController.class.getName()).log(Level.INFO, null, ex);
                            break;
                        }
                    }

                });
    }

    private boolean saveClientByHttp(Client clt) throws IOException {
        Response<Client> exec = kazisafe.saveByForm(clt.getUid(), clt.getNomClient(), clt.getPhone(),
                clt.getTypeClient(), clt.getEmail(), clt.getAdresse(), clt.getParentId().getUid())
                .execute();
        return exec.code() == 200;
    }

    private Response<Vente> saveVenteByHttp(Vente vente, Client client, CompteTresor tresor, String transaction,
            List<LigneVente> venteItems) throws IOException {
        try {
            ObjectMapper obm = new ObjectMapper();
            String ligneventes = obm.writeValueAsString(toSaleItemHelper(venteItems));
            System.out.println("Sale Item Helper " + ligneventes);
            VenteHelper hlp = new VenteHelper();
            hlp.setTransactionId(transaction);
            hlp.setTresor(tresor);
            hlp.setClient(client);
            hlp.setLigneVentes(venteItems);
            hlp.setVente(vente);
            Response<Vente> exe = kazisafe.syncSale(hlp).execute();
            return exe;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<SaleItemHelper> toSaleItemHelper(Collection<LigneVente> lvs) {
        List<SaleItemHelper> result = new ArrayList<>();
        for (LigneVente lv : lvs) {
            SaleItemHelper sih = new SaleItemHelper();
            sih.setClientId(lv.getClientId());
            sih.setMesureId(lv.getMesureId().getUid());
            sih.setMontantCdf(lv.getMontantCdf());
            sih.setMontantUsd(lv.getMontantUsd());
            sih.setNumlot(lv.getNumlot());
            sih.setProductId(lv.getProductId().getUid());
            sih.setQuantite(lv.getQuantite());
            sih.setSalePrice(lv.getPrixUnit());
            sih.setUid(lv.getUid());
            sih.setVenteId(lv.getReference().getUid());
            result.add(sih);

        }
        return result;
    }

    private boolean saveCompte(CompteTresor tr) throws IOException {
        Response<CompteTresor> exec = kazisafe.saveCompteTresorByForm(tr.getUid(), tr.getBankName(), tr.getIntitule(),
                tr.getSoldeMinimum(), tr.getNumeroCompte(), tr.getRegion(),
                tr.getTypeCompte()).execute();
        System.err.println("Reponse exec " + exec.code());
        return exec.code() == 200;
    }

    private void sendProduitIfNotExist(Produit produit, List<Mesure> mesures) {
        byte[] imageBytes = produit.getImage();
        if (imageBytes == null) {
            imageBytes = loadDefaultImage();
        }
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        saveProduitByHttp(produit, base64Image, mesures);
    }

    private byte[] loadDefaultImage() {
        try (InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png")) {
            return FileUtils.readAllBytes(is);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image par défaut" + e.getMessage());
            return new byte[0];
        }
    }

    private void saveProduitByHttp(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = createProduitHelper(produit, base64Image, mesures);
        try {
            Response<Produit> response = kazisafe.saveLite(produitHelper).execute();
            if (response.isSuccessful()) {
                System.out.println("Save synchrone Produit " + response.code());
            } else {
                System.err.println("Erreur lors de l'enregistrement du produit : " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement du produit" + e.getMessage());
        }
    }

    private ProduitHelper createProduitHelper(Produit produit, String base64Image, List<Mesure> mesures) {
        ProduitHelper produitHelper = new ProduitHelper();
        produitHelper.setUid(produit.getUid());
        produitHelper.setCategoryId(produit.getCategoryId().getUid());
        produitHelper.setCodebar(produit.getCodebar());
        produitHelper.setCouleur(produit.getCouleur());
        produitHelper.setMarque(produit.getMarque());
        produitHelper.setModele(produit.getModele());
        produitHelper.setNomProduit(produit.getNomProduit());
        produitHelper.setImage("data:image/jpeg;base64," + base64Image);
        produitHelper.setTaille(produit.getTaille());
        produitHelper.setMethodeInventaire(produit.getMethodeInventaire());
        produitHelper.setMesureList(mesures);
        return produitHelper;
    }

    public void printWithThermal(String printerName, Vente vente4save, Collection<LigneVente> lignes) {
        PrinterOutputStream pos = null;
        Client client = vente4save.getClientId();
        try {
            if (printerName == null) {
                return;
            }
            PrintService ps = PrinterOutputStream.getPrintServiceByName(printerName);
            pos = new PrinterOutputStream(ps);
            EscPos printer = new EscPos(pos);
            printer.setCharacterCodeTable(EscPos.CharacterCodeTable.CP863_Canadian_French);
            Style title = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(
                    title_s == 1 ? Style.FontSize._1 : title_s == 2 ? Style.FontSize._2 : Style.FontSize._3,
                    title_s == 1 ? Style.FontSize._1 : title_s == 2 ? Style.FontSize._2 : Style.FontSize._3);
            Style identite = new Style().setJustification(EscPosConst.Justification.Center).setFontSize(
                    identite_s == 1 ? Style.FontSize._1 : identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3,
                    identite_s == 1 ? Style.FontSize._1 : identite_s == 2 ? Style.FontSize._2 : Style.FontSize._3);
            Style ephone = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setFontSize(Style.FontSize._1, Style.FontSize._1);
            Style client_style = new Style(printer.getStyle()).setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);
            Style bold = new Style(printer.getStyle()).setJustification(EscPosConst.Justification.Left_Default)
                    .setBold(true);
            Style gras = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Right)
                    .setBold(true);
            Style right = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Right);
            Style left = new Style(printer.getStyle())
                    .setJustification(EscPosConst.Justification.Left_Default);
            Style centerbold = new Style().setJustification(EscPosConst.Justification.Center).setBold(true);
            File f = FileUtils.pointFile(entreprise.getUid() + ".png");
            if (f != null) {
                RasterBitImageWrapper imgWrapper = new RasterBitImageWrapper();
                imgWrapper.setJustification(EscPosConst.Justification.Center);
                // printer.feed(1);
                BufferedImage bimg = ImageIO.read(f);
                Bitonal bitonal = new BitonalThreshold(100);
                EscPosImage posimg = new EscPosImage(new CoffeeImageImpl(bimg), bitonal);

                try {
                    printer.write(imgWrapper, posimg);
                } catch (Exception e) {
                    MainUI.notify(null, "Attention",
                            "Veuillez mettre un bon logo (125X125px) au moins, pour votre facture", 3, "warning");
                }

            }

            printer.feed(1);
            printer.writeLF(title, entreprise.getNomEntreprise() == null ? entrepName : entreprise.getNomEntreprise());
            String idnat = entreprise.getIdNat() == null ? idNat : entreprise.getIdNat();
            String impot = entreprise.getNumeroImpot() == null ? nif : entreprise.getNumeroImpot();
            String phones = entreprise.getPhones() == null ? phonez : entreprise.getPhones();
            String stateId = "RCCM." + entreprise.getIdentification() + " " + (idnat == null ? "" : "ID NAT." + idnat)
                    + (impot == null ? ""
                            : " NIF." + impot
                                    + "\nAdresse : " + entreprise.getAdresse() + "\n"
                                    + (phones == null || phones.equals("-") ? "" : "Tel :" + phones));
            printer.writeLF(centerbold, stateId);
            if (entreprise.getWebsite() != null) {
                printer.writeLF(identite, entreprise.getWebsite());
            }

            printer.writeLF(right, " Facture N.: " + vente4save.getReference());
            LocalDateTime dv = vente4save.getDateVente();
            printer.writeLF(right, (dv == null ? LocalDateTime.now().toString() : dv.toString()));

            printer.write("Client : ");
            printer.writeLF(client_style, client != null ? ((client.getNomClient().equalsIgnoreCase("Anonyme")
                    || client.getNomClient().equalsIgnoreCase("Unknown"))
                            ? (client.getPhone().length() < 10 ? "Anonyme" : client.getPhone())
                            : client.getNomClient())
                    : !client.getPhone().isEmpty() || !client.getNomClient().isEmpty()
                            ? client.getNomClient() + ", " + client.getPhone()
                            : "Anonyme");
            printer.writeLF("-".repeat(48));
            String heads = String.format("""
                    %-4s  %-18s   %-10s   %-10s
                    """, "Qte   ", "Designation ", "P.U   ", "P.T");
            heads += ("-".repeat(48));
            // printer.writeLF("-".repeat(48));
            boolean printSup = pref.getBoolean("print_mark", true);
            boolean printMod = pref.getBoolean("print_modele", true);
            double total = 0;
            for (LigneVente ligne : lignes) {
                double prixdeventeunitaircdf = ligne.getPrixUnit();
                total += ligne.getMontantCdf();
                Produit p = ligne.getProductId();
                String concatenatedProductName = p.getNomProduit() + " " + (printSup ? p.getMarque() : "") + " "
                        + (printMod ? p.getModele() : "") + " " + p.getTaille();
                heads += String.format("""
                        %-4s  %-18s   %-10s   %-10s
                        """, ligne.getQuantite() + " " + ligne.getMesureId().getDescription(), concatenatedProductName,
                        prixdeventeunitaircdf, ligne.getMontantUsd());

            }
            System.out.println("La factura " + heads);
            printer.writeLF(heads);
            // for (LigneVente ligne : lignes) {
            // Produit pdx = ligne.getProductId();
            // Produit pd = ProduitDelegate.findProduit(pdx.getUid());
            // String l2 = ligne.getQuantite() + " " + ligne.getMesureId().getDescription()
            // + " " + ligne.getPrixUnit() + "$ ou " + (ligne.getPrixUnit() * taux2change) +
            // "Fc " + ligne.getMontantUsd() + " USD ou " + (ligne.getMontantUsd() *
            // taux2change) + " CDF";
            // printer.writeLF(bold, pd.getNomProduit() + " " +
            // (pref.getBoolean("print_mark", true)
            // ? (pd.getMarque() == null ? "" : pd.getMarque()) : "") + " " +
            // (pref.getBoolean("print_modele", true)
            // ? (pd.getModele() == null ? "" : pd.getModele()) : "") + " " +
            // (pref.getBoolean("print_tail", true)
            // ? ((pd.getTaille() == null) ? "" : pd.getTaille()) : ""));
            // printer.write(left, ((int) (ligne.getQuantite())) + " ");
            // printer.write(left, ligne.getMesureId().getDescription() + " ");
            // int dk = 48 - l2.length();
            // int dif = dk < 0 ? 0 : dk;
            // int g = d - (ligne.getPrixUnit() + "$ ").length();
            // if (g <= 0) {
            // printer.write(left, " ".repeat(dif));
            // printer.write(right, (ligne.getPrixUnit() * taux2change) + "Fc ");
            // } else {
            // int gx = g < 0 ? 0 : g;
            // int dx = dif - gx;
            // printer.write(left, " ".repeat(dx < 0 ? 0 : dx));
            // printer.write(right, (ligne.getPrixUnit() * taux2change) + "Fc " + "
            // ".repeat(gx));
            // }
            // d = (ligne.getPrixUnit() + "$ ").length();
            // printer.writeLF(right, (ligne.getMontantUsd() * taux2change) + "CDF " +
            // (pref.getBoolean("print_total_usd", true) ? ("ou " + ligne.getMontantUsd() +
            // " USD") : ""));
            //
            // }
            String dev = pref.get("mainCur", "USD");
            double actuel;
            double dette = vente4save.getMontantDette();
            double pred;

            try {
                pred = BigDecimal.valueOf(((dette / total) * 100)).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
            } catch (java.lang.NumberFormatException e) {
                pred = 0;
            }

            printer.writeLF("-".repeat(48));
            printer.writeLF(gras,
                    "Total cash : "
                            + BigDecimal.valueOf((total / taux2change)).setScale(3, RoundingMode.HALF_EVEN)
                                    .doubleValue()
                            + "USD ou " + BigDecimal.valueOf(total).setScale(3, RoundingMode.HALF_EVEN).doubleValue()
                            + " CDF");
            printer.writeLF("-".repeat(48));
            printer.writeLF("Operateur: " + user);
            printer.feed(1);
            boolean isavert = pref.getBoolean("averti", true);
            if (isavert) {
                String text = "CHER CLIENT S.V.P,VEUILLEZ VERIFIER VOS PRODUITS A LA RECEPTION PAS DES RECLAMATIONS APRES";
                printer.writeLF(centerbold, pref.get("ads_mesg", text));
            }

            printer.feed(1);
            printer.writeLF(identite, "Telecharger Kazisafe via Qr-Code suivant");
            QRCode qrcode = new QRCode();
            // printer.feed(1);
            qrcode.setSize(2);
            qrcode.setJustification(EscPosConst.Justification.Center);
            printer.write(qrcode, "https://www.endeleya.com");
            printer.feed(4);
            printer.cut(EscPos.CutMode.FULL);
            printer.close();
        } catch (IOException ex) {
            Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                pos.close();
            } catch (IOException ex) {
                Logger.getLogger(PaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public boolean isLigneVenteExist(LigneVente lv) {
        for (LigneVente saleitem : saleitems) {
            if (saleitem.getProductId().getUid().equals(lv.getProductId().getUid())) {
                return true;
            }
        }
        return false;
    }

    public LigneVente findLigneVente(String puid) {
        for (LigneVente saleitem : saleitems) {
            if (saleitem.getProductId().getUid().equals(puid)) {
                return saleitem;
            }
        }
        return null;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void loadInstalledPrinters(ComboBox<Printer> cbx_printers) {
        cbx_printers.setConverter(new StringConverter<Printer>() {
            @Override
            public String toString(Printer object) {
                return object == null ? null : object.getName();
            }

            @Override
            public Printer fromString(String string) {
                return cbx_printers.getItems()
                        .stream()
                        .filter(v -> (v.getName())
                                .equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        ObservableSet<Printer> osp = Printer.getAllPrinters();
        System.out.println("Printewrs count " + osp.size());
        cbx_printers.setItems(setToList(osp));
        defaultPrinter = Printer.getDefaultPrinter();

        cbx_printers.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) -> {
                    defaultPrinter = newValue;
                });
        cbx_printers.getSelectionModel().select(defaultPrinter);

    }

    public double dashCardVente(String role) {
        LocalDate d1 = LocalDate.now();
        LocalDate kesho = d1.plusDays(1);
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            double sumSales = VenteDelegate.sumVente(d1, kesho, taux2change, devise);
            return sumSales;
        } else {
            double sumSales = VenteDelegate.sumVente(d1, kesho, REGION, taux2change);
            return sumSales;
        }
    }

    public double creanceToday(String role) {
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            List<Vente> ventes = getVentesDebt(LocalDate.now());
            double sumSales = Util.sumCreditSales(ventes, taux2change);
            return sumSales;
        } else {
            List<Vente> ventes = getVentesDebt(LocalDate.now(), REGION);
            double sumSales = Util.sumCreditSales(ventes, taux2change);
            return sumSales;
        }
    }

    public List<Vente> getVentesDebt(LocalDate date) {
        LocalDate date1 = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, date1);
        return vts;
    }

    public List<Vente> getVentesDebt(LocalDate date, String region) {
        LocalDate date1 = date.plusDays(1);
        List<Vente> vts = VenteDelegate.findAllByDateInterval(date, date1, region);
        return vts;
    }

}
