/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

//import com.mysql.management.MysqldResource;
//import com.mysql.management.MysqldResource;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException; 
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Aretirer;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Facture;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Taxe;
import data.Taxer;
import data.Traisorerie;
import data.Vente;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import tools.Constants;
import tools.DataId;
import tools.InventoryMagasin;
import tools.JsonUtil;
import tools.ResultStatementItem;
import tools.SyncEngine;
import tools.VenteReporter;
import utilities.Relevee;

/**
 *
 * @author eroot
 */
public class JpaStorage {

    Preferences pref;

    private static String createPath(final String dbname) {
        String path = null;
        //fpath = null;
        if (PlatformUtil.isWindows()) {
            path = "C:" + File.separator + "Kazisafe" + File.separator + "datastore" + File.separator + dbname;
            // fpath = path + File.separator + dbname + ".odb";
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe/datastore/" + dbname;
            //fpath = path + File.separator + dbname + ".odb";
        } else if (PlatformUtil.isMac()) {

            path = "/Users" + File.separator + System.getProperty("user.name") + File.separator + "Kazisafe" + File.separator + "datastore" + File.separator + dbname;
            // fpath = path + File.separator + dbname + ".odb";
        }
        File folder = new File(path);
        //File file = null;
        boolean dir = folder.exists();
        if (!dir) {
            dir = folder.mkdir();
            System.out.println("New Folder " + dir);
        }
        return path;
    }

    private static JpaStorage instance = null;

    EntityManager em;
//    MysqldResource myresource;
    private String databaseName;
    EntityManagerFactory emf;

    private JpaStorage() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        databaseName = pref.get("eUid", null);
        String dbpath = createPath("kazidb");
//        myresource = startDeamon(dbpath, "database");
        /**
         * <property name="serverName" value="localhost"/>
         * <property name="portNumber" value="3306"/>
         * <property name="databaseName" value="kazisafe_db"/>
         * <property name="User" value="root"/>
         * <property name="Password" value="Admin*21"/>
         * <property name="URL" value="jdbc:mysql://localhost:"+pref.getInt("default_mysql_port",3306)+"/kazisafe_db"/>
         * <property name="driverClass" value="com.mysql.jdbc.Driver"/>
         * <property name="useSSL" value="false"/>
         */

        Map<String, String> properties = new HashMap<>();
        //String h2url = "jdbc:h2:file:" + dbpath + File.separator + databaseName + ";CIPHER=AES";
        String mysqlx = "jdbc:mysql://localhost:"+pref.getInt("default_mysql_port",3306)+"/ksf_" + databaseName + "?createDatabaseIfNotExist=true&useSsl=false&zeroDateTimeBehavior=convertToNull&"
                + "sessionVariables=sql_mode=''";
        properties.put(EntityManagerProperties.JDBC_USER, "root");
        properties.put(EntityManagerProperties.JDBC_URL, mysqlx);
//        properties.put(PersistenceUnitProperties.DDL_GENERATION,PersistenceUnitProperties.CREATE_ONLY);
//         properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE,PersistenceUnitProperties.DDL_BOTH_GENERATION);
//          properties.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE,"databaze.sql");
        properties.put(EntityManagerProperties.JDBC_PASSWORD, "Admin*21");
        emf = Persistence.createEntityManagerFactory("kazisafe-jmx", properties);
        em = emf.createEntityManager();
    }

    public EntityManager newEntityManager() {
        em = emf.createEntityManager();
        return em;
    }

//    private MysqldResource startDeamon(String dbpath, String dbname) {
//        int port = 3336;
//        MysqldResource ress = new MysqldResource(new File(dbpath, dbname));
//        Map<String, Object> options = new HashMap();
//        options.put(MysqldResource.INITIALIZE_USER, "true");
//        options.put(MysqldResource.INITIALIZE_PASSWORD, "mxj");
//        options.put(MysqldResource.INITIALIZE_USER_NAME, "ksf");
//        while (!ress.isRunning()) {
//            options.put(MysqldResource.PORT, port);
//            ress.start("embedb-mxj-th0", options);
//            port++;
//            System.err.println("Kazisafe-MXJ-Started succesfully");
//        }
//        return ress;
//    }
//    public int runDDL(String ddsql) {
//        EntityTransaction etr = em.getTransaction();
//        if (!etr.isActive()) {
//            etr.begin();
//        }
//        Query ddl = em.createNativeQuery(ddsql);
//        int v = ddl.executeUpdate();
//        etr.commit();
//        return v;
//    }
    public <T> T insertOnly(T obj) {
        try {
            if (obj instanceof Category) {
                Category category = (Category) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(category);
                etr.commit();
            } else if (obj instanceof Produit) {
                Produit produit = (Produit) obj;

                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                Category c = em.find(Category.class, produit.getCategoryId().getUid());
                if (c == null) {
                    List<Category> categs = getXDescritpion(Category.class, "Divers");
                    if (categs != null) {
                        if (!categs.isEmpty()) {
                            produit.setCategoryId(categs.get(0));
                        } else {
                            Category cx = new Category();
                            cx.setDescritption("Divers");
                            em.persist(cx);
                            produit.setCategoryId(cx);
                        }
                    }
                }
                Produit p = em.find(Produit.class, produit.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(produit);
                etr.commit();
                // etx.commit();
            } else if (obj instanceof Mesure) {
                Mesure mesure = (Mesure) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Mesure p = em.find(Mesure.class, mesure.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(mesure);
                etr.commit();
            } else if (obj instanceof Fournisseur) {
                Fournisseur fourn = (Fournisseur) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Fournisseur p = em.find(Fournisseur.class, fourn.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(fourn);
                etr.commit();
            } else if (obj instanceof Livraison) {
                Livraison livr = (Livraison) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Livraison p = em.find(Livraison.class, livr.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(livr);
                etr.commit();
            } else if (obj instanceof Stocker) {
                Stocker stocker = (Stocker) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Stocker p = em.find(Stocker.class, stocker.getUid());
                if (p != null) {
                    return obj;
                }
                try {
                    em.persist(stocker);
                } catch (java.lang.IllegalStateException e) {
                }
                etr.commit();
            } else if (obj instanceof Destocker) {
                Destocker destocker = (Destocker) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Destocker p = em.find(Destocker.class, destocker.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(destocker);
                etr.commit();
            } else if (obj instanceof Recquisition) {
                Recquisition req = (Recquisition) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Recquisition p = em.find(Recquisition.class, req.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(req);
                etr.commit();
            } else if (obj instanceof PrixDeVente) {
                PrixDeVente price = (PrixDeVente) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                PrixDeVente p = em.find(PrixDeVente.class, price.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(price);
                etr.commit();
            } else if (obj instanceof Client) {
                Client client = (Client) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Client p = em.find(Client.class, client.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(client);
                etr.commit();
            } else if (obj instanceof Vente) {
                Vente vente = (Vente) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Vente p = em.find(Vente.class, vente.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(vente);
                etr.commit();
            } else if (obj instanceof LigneVente) {
                LigneVente ligv = (LigneVente) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                LigneVente p = em.find(LigneVente.class, ligv.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(ligv);
                etr.commit();
            } else if (obj instanceof Taxe) {
                Taxe taxe = (Taxe) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Taxe p = em.find(Taxe.class, taxe.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(taxe);
                etr.commit();
            } else if (obj instanceof Taxer) {
                Taxer taxer = (Taxer) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Taxer p = em.find(Taxer.class, taxer.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(taxer);
                etr.commit();
            } else if (obj instanceof CompteTresor) {
                CompteTresor trais = (CompteTresor) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                CompteTresor p = em.find(CompteTresor.class, trais.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(trais);
                etr.commit();
            } else if (obj instanceof Traisorerie) {
                Traisorerie trais = (Traisorerie) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Traisorerie p = em.find(Traisorerie.class, trais.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(trais);
                etr.commit();
            } else if (obj instanceof Depense) {
                Depense trais = (Depense) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Depense p = em.find(Depense.class, trais.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(trais);
                etr.commit();
            } else if (obj instanceof Operation) {
                Operation oper = (Operation) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Operation p = em.find(Operation.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            } else if (obj instanceof Aretirer) {
                Aretirer oper = (Aretirer) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Aretirer p = em.find(Aretirer.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            } else if (obj instanceof ClientAppartenir) {
                ClientAppartenir oper = (ClientAppartenir) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                ClientAppartenir p = em.find(ClientAppartenir.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            } else if (obj instanceof ClientOrganisation) {
                ClientOrganisation oper = (ClientOrganisation) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                ClientOrganisation p = em.find(ClientOrganisation.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            } else if (obj instanceof RetourDepot) {
                RetourDepot oper = (RetourDepot) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                RetourDepot p = em.find(RetourDepot.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            } else if (obj instanceof RetourMagasin) {
                RetourMagasin oper = (RetourMagasin) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                RetourMagasin p = em.find(RetourMagasin.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            } else if (obj instanceof Facture) {
                Facture oper = (Facture) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Facture p = em.find(Facture.class, oper.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(oper);
                etr.commit();
            }

        } catch (Exception ex) {
            return obj;
        }

        return obj;
    }

    public <T> T insertAndSync(final T obj) {
      String jsondata=null;  
      try {
            if (obj instanceof Category) {
                Category category = (Category) obj;

                Category c = em.find(Category.class, category.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(category).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(category);
                etr.commit();
            } else if (obj instanceof Produit) {
                Produit produit = (Produit) obj;
                jsondata = JsonUtil.jsonify(produit).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Category c = em.find(Category.class, produit.getCategoryId().getUid());
                if (c == null) {
                    List<Category> categs = getXDescritpion(Category.class, "Divers");
                    if (categs != null) {
                        if (!categs.isEmpty()) {
                            produit.setCategoryId(categs.get(0));
                        } else {
                            Category cx = new Category();
                            cx.setDescritption("Divers");
                            em.persist(cx);
                            produit.setCategoryId(cx);
                        }
                    }
                }
                Produit p = em.find(Produit.class, produit.getUid());
                if (p != null) {
                    return obj;
                }
                em.persist(produit);
                etr.commit();
                // etx.commit();
            } else if (obj instanceof Mesure) {
                Mesure mesure = (Mesure) obj;
                Mesure c = em.find(Mesure.class, mesure.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(mesure).toString();

                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(mesure);
                etr.commit();
            } else if (obj instanceof Fournisseur) {
                Fournisseur fourn = (Fournisseur) obj;
                Fournisseur c = em.find(Fournisseur.class, fourn.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(fourn).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(fourn);
                etr.commit();
            } else if (obj instanceof Livraison) {
                Livraison livr = (Livraison) obj;
                Livraison c = em.find(Livraison.class, livr.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(livr).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(livr);
                etr.commit();
            } else if (obj instanceof Stocker) {
                try {
                    Stocker stocker = (Stocker) obj;
                    Stocker c = em.find(Stocker.class, stocker.getUid());
                    if (c != null) {
                        return obj;
                    }
                    jsondata = JsonUtil.jsonify(stocker).toString();
                    Mesure m = stocker.getMesureId();
                    Mesure find = em.find(Mesure.class, m.getUid());
                    EntityTransaction etr = em.getTransaction();
                    if (!etr.isActive()) {
                        etr.begin();
                    }
                    if (find == null) {
                        em.persist(m);
                    }
                    em.persist(stocker);
                    etr.commit();
                } catch (java.lang.IllegalStateException | PersistenceException e) {
                }

            } else if (obj instanceof Destocker) {
                Destocker destocker = (Destocker) obj;

                Mesure m = destocker.getMesureId();
                Mesure find = em.find(Mesure.class, m.getUid());

                Destocker c = em.find(Destocker.class, destocker.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(destocker).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                if (find == null) {
                    em.persist(m);
                }
                em.persist(destocker);
                etr.commit();
            } else if (obj instanceof Recquisition) {
                try {
                    Recquisition req = (Recquisition) obj;
                    Recquisition c = em.find(Recquisition.class, req.getUid());
                    if (c != null) {
                        return obj;
                    }
                    jsondata = JsonUtil.jsonify(req).toString();
                    EntityTransaction etr = em.getTransaction();
                    if (!etr.isActive()) {
                        etr.begin();
                    }

                    em.persist(req);
                    etr.commit();
                } catch (java.lang.IllegalStateException | PersistenceException e) {
                }
            } else if (obj instanceof PrixDeVente) {
                try {
                    PrixDeVente price = (PrixDeVente) obj;

                    PrixDeVente c = em.find(PrixDeVente.class, price.getUid());
                    if (c != null) {
                        return obj;
                    }
                    jsondata = JsonUtil.jsonify(price).toString();
                    EntityTransaction etr = em.getTransaction();
                    if (!etr.isActive()) {
                        etr.begin();
                    }

                    em.persist(price);
                    etr.commit();
                } catch (java.lang.IllegalStateException | PersistenceException e) {
                }
            } else if (obj instanceof Client) {
                try {
                    Client client = (Client) obj;
                    Client c = em.find(Client.class, client.getUid());
                    if (c != null) {
                        return obj;
                    }
                    jsondata = JsonUtil.jsonify(client).toString();
                    EntityTransaction etr = em.getTransaction();
                    if (!etr.isActive()) {
                        etr.begin();
                    }

                    em.persist(client);
                    etr.commit();
                } catch (java.lang.IllegalStateException | PersistenceException e) {
                }
            } else if (obj instanceof Vente) {
                try {
                    Vente vente = (Vente) obj;
                    Vente c = em.find(Vente.class, vente.getUid());
                    if (c != null) {
                        return obj;
                    }
                    jsondata = JsonUtil.jsonify(vente).toString();
                    EntityTransaction etr = em.getTransaction();
                    if (!etr.isActive()) {
                        etr.begin();
                    }

                    em.persist(vente);
                    etr.commit();
                } catch (java.lang.IllegalStateException | PersistenceException e) {
                }
            } else if (obj instanceof LigneVente) {
                try {
                    LigneVente ligv = (LigneVente) obj;
                    LigneVente c = em.find(LigneVente.class, ligv.getUid());
                    if (c != null) {
                        return obj;
                    }
                    jsondata = JsonUtil.jsonify(ligv).toString();
                    EntityTransaction etr = em.getTransaction();
                    if (!etr.isActive()) {
                        etr.begin();
                    }

                    em.persist(ligv);
                    etr.commit();
                } catch (java.lang.IllegalStateException | PersistenceException e) {
                }
            } else if (obj instanceof Taxe) {
                Taxe taxe = (Taxe) obj;
                Taxe c = em.find(Taxe.class, taxe.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(taxe).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(taxe);
                etr.commit();
            } else if (obj instanceof Taxer) {
                Taxer taxer = (Taxer) obj;
                Taxer c = em.find(Taxer.class, taxer.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(taxer).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(taxer);
                etr.commit();
            } else if (obj instanceof CompteTresor) {
                CompteTresor trais = (CompteTresor) obj;
                jsondata = JsonUtil.jsonify(trais).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(trais);
                etr.commit();
            } else if (obj instanceof Traisorerie) {
                Traisorerie trais = (Traisorerie) obj;
                Traisorerie c = em.find(Traisorerie.class, trais.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(trais).toString();
                EntityTransaction etr = em.getTransaction();

                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(trais);
                etr.commit();
            } else if (obj instanceof Depense) {
                Depense trais = (Depense) obj;
                Depense c = em.find(Depense.class, trais.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(trais).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(trais);
                etr.commit();
            } else if (obj instanceof Operation) {
                Operation oper = (Operation) obj;
                Operation c = em.find(Operation.class, oper.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(oper).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(oper);
                etr.commit();
            } else if (obj instanceof Aretirer) {
                Aretirer oper = (Aretirer) obj;
                Aretirer c = em.find(Aretirer.class, oper.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(oper).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(oper);
                etr.commit();
            } else if (obj instanceof ClientAppartenir) {
                ClientAppartenir oper = (ClientAppartenir) obj;
                ClientAppartenir c = em.find(ClientAppartenir.class, oper.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(oper).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(oper);
                etr.commit();
            } else if (obj instanceof ClientOrganisation) {
                ClientOrganisation oper = (ClientOrganisation) obj;
                ClientOrganisation c = em.find(ClientOrganisation.class, oper.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(oper).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(oper);
                etr.commit();
            } else if (obj instanceof RetourDepot) {
                RetourDepot oper = (RetourDepot) obj;
                RetourDepot c = em.find(RetourDepot.class, oper.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(oper).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(oper);
                etr.commit();
            } else if (obj instanceof RetourMagasin) {
                RetourMagasin oper = (RetourMagasin) obj;
                RetourMagasin c = em.find(RetourMagasin.class, oper.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(oper).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }

                em.persist(oper);
                etr.commit();
            } else if (obj instanceof Facture) {
                Facture fact = (Facture) obj;
                Facture c = em.find(Facture.class, fact.getUid());
                if (c != null) {
                    return obj;
                }
                jsondata = JsonUtil.jsonify(fact).toString();
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.persist(fact);
                etr.commit();
            }
        } catch (Exception ex) {
            return obj;
        }
        
        
        
//        Executors.newSingleThreadExecutor()
//                .execute(() -> {
//
//                    if (obj instanceof Fournisseur) {
//                        Fournisseur fss = (Fournisseur) obj;
//                        JsonObject json = JsonUtil.jsonify(fss);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Client) {
//                        Client clt = (Client) obj;
//                        JsonObject json = JsonUtil.jsonify(clt);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Category) {
//                        Category cat = (Category) obj;
//                        JsonObject json = JsonUtil.jsonify(cat);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Produit) {
//                        Produit produit = (Produit) obj;
//                        byte[] img = produit.getImage();
//                        JsonObject jsonp = JsonUtil.jsonify(produit);
//                        if (jsonp != null) {
//                            MainuiController.getInstance().sendMessage(jsonp.toString());
//                        }
//                        if (img != null) {
//                            ImageProduit imgp = new ImageProduit();
//                            imgp.setIdProduit(produit.getUid());
//                            String base64 = DatatypeConverter.printBase64Binary(img);
//                            imgp.setImageBase64(base64);
//                            JsonObject json = JsonUtil.jsonify(imgp);
//                            if (json != null) {
//                                MainuiController.getInstance().sendMessage(json.toString());
//                            }
//                        }
//                    } else if (obj instanceof Mesure) {
//                        Mesure mesure = (Mesure) obj;
//                        JsonObject json = JsonUtil.jsonify(mesure);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Livraison) {
//                        Livraison livraison = (Livraison) obj;
//                        JsonObject json = JsonUtil.jsonify(livraison);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Stocker) {
//                        Stocker stocker = (Stocker) obj;
//
//                        JsonObject json = JsonUtil.jsonify(stocker);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Destocker) {
//                        Destocker destocker = (Destocker) obj;
//                        String destring = JsonUtil.jsonify(destocker).toString();
//                        System.err.println("DESTOCKAJSON " + destring);
//                        MainuiController.getInstance().sendMessage(destring);
//                    } else if (obj instanceof Recquisition) {
//                        Recquisition recquisition = (Recquisition) obj;
//                        JsonObject json = JsonUtil.jsonify(recquisition);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof PrixDeVente) {
//                        PrixDeVente prixdevente = (PrixDeVente) obj;
//                        System.out.println("RECQUSITION FROM PRICE " + prixdevente.getRecquisitionId());
//                        JsonObject json = JsonUtil.jsonify(prixdevente);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Vente) {
//                        Vente vente = (Vente) obj;
//                        System.out.println("VENTE vxvxvxvxvxvxv XXXXXXXXXXXX " + vente.getMontantDette());
//                        JsonObject json = JsonUtil.jsonify(vente);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof LigneVente) {
//                        LigneVente lv = (LigneVente) obj;
//                        JsonObject json = JsonUtil.jsonify(lv);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Traisorerie) {//
//                        Traisorerie traisorerie = (Traisorerie) obj;
//                        JsonObject json = JsonUtil.jsonify(traisorerie);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof CompteTresor) {
//                        CompteTresor traisorerie = (CompteTresor) obj;
//                        JsonObject json = JsonUtil.jsonify(traisorerie);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Depense) {
//                        Depense traisorerie = (Depense) obj;
//                        JsonObject json = JsonUtil.jsonify(traisorerie);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Operation) {
//                        Operation operation = (Operation) obj;
//                        JsonObject json = JsonUtil.jsonify(operation);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Aretirer) {
//                        Aretirer oper = (Aretirer) obj;
//                        JsonObject json = JsonUtil.jsonify(oper);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof ClientAppartenir) {
//                        ClientAppartenir oper = (ClientAppartenir) obj;
//                        JsonObject json = JsonUtil.jsonify(oper);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof ClientOrganisation) {
//                        ClientOrganisation oper = (ClientOrganisation) obj;
//                        JsonObject json = JsonUtil.jsonify(oper);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof RetourDepot) {
//                        RetourDepot oper = (RetourDepot) obj;
//                        JsonObject json = JsonUtil.jsonify(oper);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof RetourMagasin) {
//                        RetourMagasin oper = (RetourMagasin) obj;
//                        JsonObject json = JsonUtil.jsonify(oper);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    } else if (obj instanceof Facture) {
//                        Facture bill = (Facture) obj;
//                        JsonObject json = JsonUtil.jsonify(bill);
//                        if (json != null) {
//                            MainuiController.getInstance().sendMessage(json.toString());
//                        }
//                    }
//
//                });

        return obj;
    }

    public <T> T update(T obj) {
        // em.setFlushMode(FlushModeType.COMMIT);
        String jsonfied;
        if (obj instanceof Category) {
            Category category = (Category) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(category);
            //em.flush();
            etr.commit();
        } else if (obj instanceof Produit) {
            Produit produit = (Produit) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            Category c = em.find(Category.class, produit.getCategoryId().getUid());
            if (c == null) {
                List<Category> categs = getXDescritpion(Category.class, "Divers");
                if (categs != null) {
                    if (!categs.isEmpty()) {
                        produit.setCategoryId(categs.get(0));
                    } else {
                        Category cx = new Category();
                        cx.setDescritption("Divers");
                        em.merge(cx);
                        produit.setCategoryId(cx);
                    }
                }
            }

            em.merge(produit);
            etr.commit();
            // etx.commit();
        } else if (obj instanceof Mesure) {
            Mesure mesure = (Mesure) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(mesure);
            etr.commit();
        } else if (obj instanceof Fournisseur) {
            Fournisseur fourn = (Fournisseur) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(fourn);
            etr.commit();
        } else if (obj instanceof Livraison) {
            Livraison livr = (Livraison) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(livr);
            etr.commit();
        } else if (obj instanceof Stocker) {
            Stocker stocker = (Stocker) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(stocker);
            etr.commit();
        } else if (obj instanceof Destocker) {
            Destocker destocker = (Destocker) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(destocker);
            etr.commit();
        } else if (obj instanceof Recquisition) {
            Recquisition req = (Recquisition) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(req);
            etr.commit();
        } else if (obj instanceof PrixDeVente) {
            PrixDeVente price = (PrixDeVente) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(price);
            etr.commit();
        } else if (obj instanceof Client) {
            Client client = (Client) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(client);
            etr.commit();
        } else if (obj instanceof Vente) {
            Vente vente = (Vente) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(vente);
            etr.commit();
        } else if (obj instanceof LigneVente) {
            LigneVente ligv = (LigneVente) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(ligv);
            etr.commit();
        } else if (obj instanceof Taxe) {
            Taxe taxe = (Taxe) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(taxe);
            etr.commit();
        } else if (obj instanceof Taxer) {
            Taxer taxer = (Taxer) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(taxer);
            etr.commit();
        } else if (obj instanceof CompteTresor) {
            CompteTresor trais = (CompteTresor) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(trais);
            etr.commit();
        } else if (obj instanceof Traisorerie) {
            Traisorerie trais = (Traisorerie) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(trais);
            etr.commit();
        } else if (obj instanceof Depense) {
            Depense trais = (Depense) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(trais);
            etr.commit();
        } else if (obj instanceof Operation) {
            Operation oper = (Operation) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(oper);
            etr.commit();
        } else if (obj instanceof Facture) {
            Facture oper = (Facture) obj;
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(oper);
            etr.commit();
        }
       
        return obj;
    }

    public <T> T updateOnly(T obj) {
        try {
            // em.setFlushMode(FlushModeType.COMMIT);
            if (obj instanceof Category) {
                Category category = (Category) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(category);
                //em.flush();
                etr.commit();
            } else if (obj instanceof Produit) {
                Produit produit = (Produit) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                Category c = em.find(Category.class, produit.getCategoryId().getUid());
                if (c == null) {
                    List<Category> categs = getXDescritpion(Category.class, "Divers");
                    if (categs != null) {
                        if (!categs.isEmpty()) {
                            produit.setCategoryId(categs.get(0));
                        } else {
                            Category cx = new Category();
                            cx.setDescritption("Divers");
                            em.merge(cx);
                            produit.setCategoryId(cx);
                        }
                    }
                }

                em.merge(produit);
                etr.commit();
                // etx.commit();
            } else if (obj instanceof Mesure) {
                Mesure mesure = (Mesure) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(mesure);
                etr.commit();
            } else if (obj instanceof Fournisseur) {
                Fournisseur fourn = (Fournisseur) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(fourn);
                etr.commit();
            } else if (obj instanceof Livraison) {
                Livraison livr = (Livraison) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(livr);
                etr.commit();
            } else if (obj instanceof Stocker) {
                Stocker stocker = (Stocker) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(stocker);
                etr.commit();
            } else if (obj instanceof Destocker) {
                Destocker destocker = (Destocker) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(destocker);
                etr.commit();
            } else if (obj instanceof Recquisition) {
                Recquisition req = (Recquisition) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(req);
                etr.commit();
            } else if (obj instanceof PrixDeVente) {
                PrixDeVente price = (PrixDeVente) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(price);
                etr.commit();
            } else if (obj instanceof Client) {
                Client client = (Client) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(client);
                etr.commit();
            } else if (obj instanceof Vente) {
                Vente vente = (Vente) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(vente);
                etr.commit();
            } else if (obj instanceof LigneVente) {
                LigneVente ligv = (LigneVente) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(ligv);
                etr.commit();
            } else if (obj instanceof Taxe) {
                Taxe taxe = (Taxe) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(taxe);
                etr.commit();
            } else if (obj instanceof Taxer) {
                Taxer taxer = (Taxer) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(taxer);
                etr.commit();
            } else if (obj instanceof CompteTresor) {
                CompteTresor trais = (CompteTresor) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(trais);
                etr.commit();
            } else if (obj instanceof Traisorerie) {
                Traisorerie trais = (Traisorerie) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(trais);
                etr.commit();
            } else if (obj instanceof Depense) {
                Depense trais = (Depense) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(trais);
                etr.commit();
            } else if (obj instanceof Operation) {
                Operation oper = (Operation) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(oper);
                etr.commit();
            } else if (obj instanceof Facture) {
                Facture oper = (Facture) obj;
                EntityTransaction etr = em.getTransaction();
                if (!etr.isActive()) {
                    etr.begin();
                }
                em.merge(oper);
                etr.commit();
            }
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public <T> List<T> insertOnlyBulk(List<T> obj) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        for (T t : obj) {
            em.persist(t);
        }
        etr.commit();
        return obj;

    }

    public <T> List<T> insertAndSyncBulk(List<T> objs) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        for (T t : objs) {
            em.persist(t);
        }
        etr.commit();
       
        return objs;

    }

    public <T> void deleteOnly(T obj) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(obj));
        etr.commit();
    }

    public <T> void delete(T obj) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(obj));
        etr.commit();
        
    }

    public void close() {
        em.close();
//        myresource.shutdown();
    }

    public <X> List<X> getXDescritpion(Class<X> clsx, String descr) {
        Query query;
        if (clsx.isAssignableFrom(Category.class)) {
            query = em.createNamedQuery("Category.findByDescritption");
            query.setParameter("descritption", descr);
            return query.getResultList();
        } else if (clsx.isAssignableFrom(Mesure.class)) {
            query = em.createNamedQuery("Mesure.findByDescription");
            query.setParameter("description", descr);
            return query.getResultList();
        }
        return null;
    }

    public Mesure findByProduitAndDescription(String prod, String descr) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.description = ? ");
            Query q = em.createNativeQuery(sb.toString(), Mesure.class);
            q.setParameter(1, prod).setParameter(2, descr);
            return (Mesure) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Mesure findByProduitAndQuant(String prod, double quant) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.quantcontenu = ? ");
            Query q = em.createNativeQuery(sb.toString(), Mesure.class);
            q.setParameter(1, prod).setParameter(2, quant);
            return (Mesure) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> T findByUid(Class<T> ct, String uid) {
        return em.find(ct, uid);
    }

    public <T> T findByUid(Class<T> ct, Integer uid) {
        return em.find(ct, uid);
    }

    public <T> T findByUid(Class<T> ct, Long uid) {
        return em.find(ct, uid);
    }

    public <T> T findClientTools(Class<T> type, String uid) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir l WHERE l.client_id = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, uid);
            return (T) query.getSingleResult();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM client_organisation s WHERE s.uid = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, uid);
            return (T) query.getSingleResult();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client s WHERE s.uid = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, uid);
            return (T) query.getSingleResult();
        }
        return null;
    }

    public <T> Long findCount(Class<T> type, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT COUNT(*) FROM livraison l WHERE l.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT COUNT(*) FROM stocker s WHERE s.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT COUNT(*) FROM destocker d WHERE d.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT COUNT(*) FROM recquisition r WHERE r.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT COUNT(*) FROM vente v WHERE v.region = ? AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT COUNT(*) FROM ligne_vente v WHERE v.reference_uid IN (SELECT uid FROM vente v WHERE v.region = ? ) ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT COUNT(*) FROM traisorerie t WHERE t.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT COUNT(*) FROM operation o WHERE o.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT COUNT(*) FROM client_appartenir ca WHERE ca.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT COUNT(*) FROM client_organisation co WHERE co.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT COUNT(*) FROM aretirer a WHERE a.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT COUNT(*) FROM retour_depot rd WHERE rd.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT COUNT(*) FROM retour_magasin rm WHERE rm.region = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return (Long) query.getSingleResult();
        }
        return null;
    }

    public <T> Long findCountByProduit(Class<T> type, String uidPro, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT COUNT(*) FROM stocker s WHERE s.region = ? AND s.product_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT COUNT(*) FROM destocker d WHERE d.region = ? AND d.product_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT COUNT(*) FROM recquisition r WHERE r.region = ? AND r.product_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT COUNT(*) FROM ligne_vente v WHERE v.product_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT COUNT(*) FROM mesure v WHERE v.produit_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uidPro);
            return (Long) query.getSingleResult();
        }
        return null;
    }

    public <T> Long findCountByProduit(Class<T> type, String uidPro) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT COUNT(*) FROM stocker s WHERE  s.product_id = ? ");
            query = em.createNativeQuery(sb.toString());

            query.setParameter(1, uidPro);
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT COUNT(*) FROM destocker d WHERE  d.product_id = ? ");
            query = em.createNativeQuery(sb.toString());

            query.setParameter(1, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT COUNT(*) FROM recquisition r WHERE  r.product_id = ? ");
            query = em.createNativeQuery(sb.toString());

            query.setParameter(1, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT COUNT(*) FROM ligne_vente v WHERE v.product_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uidPro);
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT COUNT(*) FROM mesure v WHERE v.produit_id = ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uidPro);
            return (Long) query.getSingleResult();
        }
        return null;
    }

    public <T> Long findCount(Class<T> type) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT COUNT(*) FROM livraison l");
            query = em.createNativeQuery(sb.toString());
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT COUNT(*) FROM stocker s ");
            query = em.createNativeQuery(sb.toString());
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT COUNT(*) FROM destocker d");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT COUNT(*) FROM recquisition r");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT COUNT(*) FROM vente v WHERE v.observation != ? ");
            query = em.createNativeQuery(sb.toString());
            query.setParameter(1, "Drafted");
            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT COUNT(*) FROM traisorerie t ");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT COUNT(*) FROM operation o");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT COUNT(*) FROM client_appartenir ca");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT COUNT(*) FROM client_organisation co ");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT COUNT(*) FROM aretirer a ");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT COUNT(*) FROM retour_depot rd ");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT COUNT(*) FROM retour_magasin rm ");
            query = em.createNativeQuery(sb.toString());

            return (Long) query.getSingleResult();
        }
        return 0l;
    }

    /**
     * Columns returned : uid, product_id,
     * mesure_id,nomproduit,marque,modele,taille,q,description,coutAchat,
     * numlot, dateExpiry
     *
     * @return objet[]IFNULL(B.tb,0)
     */
    public List<Object[]> findGoods() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(cal/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta - IFNULL(B.tb,0)) as cal,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.cal > 0");
        try {
            Query query = em.createNativeQuery(sb.toString());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Object[]> findGoodsOnRegion(String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Object[]> findGoodsCategorized(String category) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, category);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Object[]> findGoodsCategorized(String category, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, category);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Object[]> searchGoodsCategorized(String category, String searchQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? AND CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) LIKE ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, category);
            query.setParameter(2, "%" + searchQuery + "%");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Object[]> searchGoodsCategorized(String category, String region, String searchQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id,r.numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? AND CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) LIKE ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, category);
            query.setParameter(3, "%" + searchQuery + "%");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    /**
     * columns : uid,product_id,mesure_id,produx,quantite,
     * description,coutachat,numlot,dateexpiry
     *
     * @param q request
     * @return
     */
    public List<Object[]> searchGoods(String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque, p.modele,p.taille,p.couleur,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id,r.numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B ")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND CONCAT(p.nomproduit,' ',p.marque,' ', p.modele,' ',p.taille,' ',p.couleur,' ',p.codebar)  LIKE ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, "%" + q + "%");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Object[]> searchGoods(String q, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque, p.modele,p.taille,p.couleur,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id,r.numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B ")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND CONCAT(p.nomproduit,' ',p.marque,' ', p.modele,' ',p.taille,' ',p.couleur,' ',p.codebar)  LIKE ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, "%" + q + "%");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Recquisition> findFefofiedRecqUnit(String produx) {
        List<Recquisition> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,pipe.region,pipe.pieces,pipe.coutAchat,pipe.numlot,pipe.dateExpiry,pipe.date,"
                + "pipe.reference,pipe.stockAlert,pipe.observation FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id,A.region, A.product_id,A.numlot,A.quantcontenu,A.description,"
                        + "A.dateExpiry,A.stockAlert,A.date,A.observation,A.reference FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry,r.date, r.region,"
                        + "r.stockAlert,r.reference,r.observation FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.product_id = ? GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot,l.mesure_id,n.uid FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid  AND l.product_id = ?"
                        + " GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 ORDER BY pipe.dateExpiry ASC");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, produx);
            query.setParameter(2, produx);
            List<Object[]> rsts = query.getResultList();
            for (Object[] rst : rsts) {
                Recquisition r = new Recquisition(String.valueOf(rst[0]));
                r.setCoutAchat(Double.parseDouble(String.valueOf(rst[5])));
                try {
                    r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(rst[8])));
                    if (!Objects.isNull(rst[7])) {
                        String dt = String.valueOf(rst[7]);
                        //if (StringUtil.isNumeric(dt)) {
                        r.setDateExpiry(Constants.DATE_ONLY_FORMAT.parse(dt));
                        // }
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                Mesure mez = findByUid(Mesure.class, String.valueOf(rst[2]));
                r.setMesureId(mez);
                r.setNumlot(String.valueOf(rst[6]));
                r.setObservation(String.valueOf(rst[11]));
                Produit p = findByUid(Produit.class, String.valueOf(rst[1]));
                r.setProductId(p);
                r.setQuantite(Double.valueOf(String.valueOf(rst[4])));
                r.setReference(String.valueOf(rst[9]));
                r.setRegion(String.valueOf(rst[3]));
                r.setStockAlert(Double.valueOf(String.valueOf(Objects.isNull(rst[10]) ? "0" : rst[10])));
                result.add(r);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Recquisition> findFifofiedRecqUnit(String produx) {
        List<Recquisition> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,pipe.region,pipe.pieces,pipe.coutAchat,pipe.numlot,pipe.dateExpiry,pipe.date,pipe.reference,pipe.stockAlert,pipe.observation FROM ")
                .append("(SELECT A.uid,(ta-IFNULL(tb,0)) as pieces,A.coutAchat,A.mesure_id,A.region, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry,A.stockAlert,A.date,A.observation,A.reference FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry,r.date, r.region,r.stockAlert,r.reference,r.observation FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.product_id = ? GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(IFNULL(0,l.quantite)*m.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure m WHERE l.mesure_id=m.uid  AND l.product_id = ? GROUP BY l.product_id,numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 ORDER BY pipe.date ASC");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, produx);
            query.setParameter(2, produx);
            List<Object[]> rsts = query.getResultList();
            for (Object[] rst : rsts) {
                Recquisition r = new Recquisition(String.valueOf(rst[0]));
                r.setCoutAchat(Double.parseDouble(String.valueOf(rst[5])));
                try {
                    r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(rst[8])));
                    if (!Objects.isNull(rst[7])) {
                        String dt = String.valueOf(rst[7]);

                        r.setDateExpiry(Constants.DATE_ONLY_FORMAT.parse(dt));
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                Mesure m = findByUid(Mesure.class, String.valueOf(rst[2]));
                r.setMesureId(m);
                r.setNumlot(String.valueOf(rst[6]));
                r.setObservation(String.valueOf(rst[11]));
                Produit p = findByUid(Produit.class, String.valueOf(rst[1]));
                r.setProductId(p);
                r.setQuantite(Double.valueOf(String.valueOf(rst[4])));
                r.setReference(String.valueOf(rst[9]));
                r.setRegion(String.valueOf(rst[3]));
                r.setStockAlert(Double.valueOf(String.valueOf(Objects.isNull(rst[10]) ? "0" : rst[10])));
                result.add(r);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Recquisition> findLifofiedRecqUnit(String produx) {
        List<Recquisition> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,pipe.region,pipe.pieces,pipe.coutAchat,pipe.numlot,pipe.dateExpiry,pipe.date,pipe.reference,pipe.stockAlert,pipe.observation FROM ")
                .append("(SELECT A.uid,(ta-IFNULL(tb,0)) as pieces,A.coutAchat,A.mesure_id,A.region, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry,A.stockAlert,A.date,A.observation,A.reference FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry,r.date, r.region,r.stockAlert,r.reference,r.observation FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.product_id = ? GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(IFNULL(0,l.quantite)*m.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure m WHERE l.mesure_id=m.uid  AND l.product_id = ? GROUP BY l.product_id,numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 ORDER BY pipe.date DESC");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, produx);
            query.setParameter(2, produx);
            List<Object[]> rsts = query.getResultList();
            for (Object[] rst : rsts) {
                Recquisition r = new Recquisition(String.valueOf(rst[0]));
                r.setCoutAchat(Double.parseDouble(String.valueOf(rst[5])));
                try {
                    r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(rst[8])));
                    if (!Objects.isNull(rst[7])) {
                        String dt = String.valueOf(rst[7]);
                        r.setDateExpiry(Constants.DATE_ONLY_FORMAT.parse(dt));
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                Mesure m = findByUid(Mesure.class, String.valueOf(rst[2]));
                r.setMesureId(m);
                r.setNumlot(String.valueOf(rst[6]));
                r.setObservation(String.valueOf(rst[11]));
                Produit p = findByUid(Produit.class, String.valueOf(rst[1]));
                r.setProductId(p);
                r.setQuantite(Double.valueOf(String.valueOf(rst[4])));
                r.setReference(String.valueOf(rst[9]));
                r.setRegion(String.valueOf(rst[3]));
                r.setStockAlert(Double.valueOf(String.valueOf(Objects.isNull(rst[10]) ? "0" : rst[10])));
                result.add(r);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<VenteReporter> findReportSaleByProduct(Date d1, Date d2) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.codebar,CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) as prods,"
                + "SUM(IFNULL((l.quantite*l.prixunit),0)) as c, SUM(l.quantite*m.quantcontenu) as qvpc, l.mesure_id FROM produit p,ligne_vente l,vente v,"
                + " mesure m WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND m.uid = l.mesure_id AND v.datevente BETWEEN ? AND ? "
                + "GROUP BY l.product_id order by c desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.valueOf(String.valueOf(li[4])));
                vi.setCodebar(String.valueOf(li[2]));
                vi.setQuantite(Double.valueOf(String.valueOf(li[5])));
                DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
                try {
                    if (!Objects.isNull(li[0])) {
                        vi.setDate(df.parse(String.valueOf(li[0])));
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                String mid = String.valueOf(li[6]);
                Mesure mzr = findByUid(Mesure.class, mid);
                vi.setMesure(mzr);
                vi.setProduit(String.valueOf(li[3]));
                result.add(vi);

            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }
    
    public List<VenteReporter> findReportSaleByProduct(Date d1, Date d2,String region) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.codebar,CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) as prods,"
                + "SUM(IFNULL((l.quantite*l.prixunit),0)) as c, SUM(l.quantite*m.quantcontenu) as qvpc, l.mesure_id FROM produit p,ligne_vente l,vente v,"
                + " mesure m WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND m.uid = l.mesure_id AND v.datevente BETWEEN ? AND ? AND v.region = ? "
                + "GROUP BY l.product_id order by c desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE)
                    .setParameter(3, region);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.valueOf(String.valueOf(li[4])));
                vi.setCodebar(String.valueOf(li[2]));
                vi.setQuantite(Double.valueOf(String.valueOf(li[5])));
                 DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
                try {
                    if (!Objects.isNull(li[0])) {
                        vi.setDate(df.parse(String.valueOf(li[0])));
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                String mid = String.valueOf(li[6]);
                Mesure mzr = findByUid(Mesure.class, mid);
                vi.setMesure(mzr);
                vi.setProduit(String.valueOf(li[3]));
                result.add(vi);

            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<VenteReporter> findReportSaleByCategory(Date d1, Date d2) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.categoryid_uid,c.descritption,"
                + "SUM(IFNULL(l.montantusd,0)) as som FROM produit p,ligne_vente l,vente v,"
                + " category c WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND c.uid = p.categoryid_uid AND v.datevente BETWEEN ? AND ? "
                + "GROUP BY c.uid order by som desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.valueOf(String.valueOf(li[4])));
                Category categz = findByUid(Category.class, String.valueOf(li[2]));
                vi.setCategory(categz);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }
    
    public List<VenteReporter> findReportSaleByCategory(Date d1, Date d2,String region) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.categoryid_uid,c.descritption,"
                + "SUM(IFNULL(l.montantusd,0)) as som FROM produit p,ligne_vente l,vente v,"
                + " category c WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND c.uid = p.categoryid_uid AND v.datevente BETWEEN ? AND ? AND v.region = ? "
                + "GROUP BY c.uid order by som desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE)
                    .setParameter(3, region);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.valueOf(String.valueOf(li[4])));
                Category categz = findByUid(Category.class, String.valueOf(li[2]));
                vi.setCategory(categz);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<VenteReporter> findReportSaleByClient(Date d1, Date d2, double taux) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,v.clientid_uid, (SUM(IFNULL(v.montantusd,0))+(SUM(IFNULL(v.montantcdf,0))/")
                .append(taux).append(")) as som FROM vente v, client c WHERE c.uid=v.clientid_uid AND v.datevente BETWEEN ? AND ? GROUP BY c.uid order by som desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.valueOf(String.valueOf(li[2])));
                Client client = findByUid(Client.class, String.valueOf(li[1]));
                vi.setClient(client);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }
    
    public List<VenteReporter> findReportSaleByClient(Date d1, Date d2,String region, double taux) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,v.clientid_uid, (SUM(IFNULL(v.montantusd,0))+(SUM(IFNULL(v.montantcdf,0))/")
                .append(taux).append(")) as som FROM vente v, client c WHERE c.uid=v.clientid_uid AND v.datevente BETWEEN ? AND ? AND v.region = ? GROUP BY c.uid order by som desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE)
                    .setParameter(3, region);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.valueOf(String.valueOf(li[2])));
                Client client = findByUid(Client.class, String.valueOf(li[1]));
                vi.setClient(client);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }
    

    public List<ResultStatementItem> findMargesPerProduct(Date d1, Date d2) {
        List<ResultStatementItem> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.codebar,CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) as prods,"
                + "SUM(IFNULL(l.montantusd,0)) as c,(r.coutachat*l.quantite) as CA ,(SUM(IFNULL(l.montantusd,0))-r.coutachat*l.quantite) as marge, "
                + "SUM(l.quantite) as qv FROM produit p,ligne_vente l,vente v,recquisition r "
                + " WHERE p.uid=l.product_id AND v.uid=l.reference_uid and r.numlot=l.numlot and r.product_id=l.product_id "
                + "AND v.datevente BETWEEN ? AND ? GROUP BY l.product_id order by c desc ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE);
            List<Object[]> objects = query.getResultList();
            for (Object[] obj : objects) {
                ResultStatementItem ris = new ResultStatementItem();
                if (!Objects.isNull(obj[0])) {
                    ris.setPeriode(Constants.USER_READABLE_FORMAT.format(d1) + " - " + Constants.USER_READABLE_FORMAT.format(d2));
                }
                ris.setDescription(String.valueOf(obj[3]));
                ris.setMontantRevenu(Double.valueOf(String.valueOf(obj[4])));
                ris.setMontantDepense(Double.valueOf(String.valueOf(obj[5])));
                ris.setMontantMarge(Double.valueOf(String.valueOf(obj[6])));
                result.add(ris);
            }
        } catch (NoResultException e) {
        }
        return result;
    }

    public List<LigneVente> findLigneVenteFor(int venteId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente c WHERE c.reference_uid  = ? ");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, venteId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> List<T> findByRef(Class<T> type, String ref) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker c WHERE c.livraisId IN (SELECT l.uid FROM livraison l WHERE l.reference = ? )");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT * FROM ligne_vente c WHERE c.reference_uid IN (SELECT v.uid FROM vente v WHERE v.reference = ? )");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();

        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer c WHERE c.referencevente = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.referencevente = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByRef(Class<T> type, String ref, Date date) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison c WHERE c.reference = ? AND c.date = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker c WHERE c.livraisId IN (SELECT v.uid FROM livraison v WHERE v.reference = ?)  AND c.dateStocker = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker c WHERE c.reference = ?  AND c.dateDestockage = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition c WHERE c.reference = ?  AND c.date = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente c WHERE c.reference = ?  AND c.dateVente = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT * FROM ligne_vente c WHERE c.reference_uid IN (SELECT v.uid FROM vente v WHERE v.reference = ?  AND v.date = ? )");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie c WHERE c.reference = ?  AND c.date = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation c WHERE c.reference = ?  AND c.date = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();

        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer c WHERE c.referencevente = ?  AND c.date = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.referencevente = ?  AND c.date = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByRef(Class<T> type, Object ref) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);

        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT * FROM ligne_vente c WHERE c.reference_uid IN (SELECT v.uid FROM vente v WHERE v.reference = ?)");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation c WHERE c.reference = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();

        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer c WHERE c.referencevente = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.referencevente = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, ref);
            return query.getResultList();
        }
        return null;
    }

    public Double sumRetourMagasin(Date date1, Date date2) {
        List<RetourMagasin> retrm = findAllByDateInterval(RetourMagasin.class, date1, date2);
        double s = 0;
        for (RetourMagasin rtr : retrm) {
            Mesure mp = rtr.getMesureId();
            s += ((rtr.getQuantite() * mp.getQuantContenu()) * (rtr.getPrixVente() / mp.getQuantContenu()));
        }
        return s;
    }

    public Double sumRetourMagasin(Date date1, Date date2, String region) {
        List<RetourMagasin> retrm = findAllByDateIntervalInRegion(RetourMagasin.class, date1, date2, region);
        double s = 0;
        for (RetourMagasin rtr : retrm) {
            Mesure mp = rtr.getMesureId();
            s += ((rtr.getQuantite() * mp.getQuantContenu()) * (rtr.getPrixVente() / mp.getQuantContenu()));
        }
        return s;
    }

    public Double sumUsdRecoveredTraisorerie(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(t.montantusd) s FROM traisorerie t WHERE t.libelle = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, ref);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Produit> searchProduit(String produx) {
        List<Produit> rsult = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ',p.couleur) LIKE ?");
        try {
            Query query = em.createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, "%" + produx + "%");
            rsult.addAll(query.getResultList());
        } catch (NoResultException e) {
            System.err.println("Result is empty mon vieu");
        }
        return rsult;
    }

    public HashMap<Long, String> getTop10ProductDesc() {
        HashMap<Long, String> hash = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(l.product_id) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val FROM ligne_vente l, vente v, produit p "
                + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid GROUP BY l.product_id ORDER by count(l.product_id) DESC LIMIT 10");
        Query query = em.createNativeQuery(sb.toString());
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            Long e1 = (Long) obj[0];
            String nv = String.valueOf(obj[1]);
            hash.put(e1, nv);
        }
        return hash;
    }

    public HashMap<Long, String> getTop10ProductDesc(String region) {

        HashMap<Long, String> hash = new HashMap<>();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT count(l.product_id) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val FROM ligne_vente l,vente v, produit p "
                    + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid AND v.region = ? GROUP BY l.product_id ORDER by count(l.product_id) DESC LIMIT 10");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            List<Object[]> objs = query.getResultList();
            for (Object[] obj : objs) {
                Long e1 = (Long) obj[0];
                String nv = String.valueOf(obj[1]);
                hash.put(e1, nv);
            }
        } catch (NoResultException e) {
        }
        return hash;
    }

    public Double sumCdfRecoveredTraisorerie(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(t.montantcdf) s FROM traisorerie t WHERE t.libelle = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, ref);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Double sumRecoveredByVente(int vente, double taux) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ((SUM(t.montantcdf)/")
                    .append(taux)
                    .append(")+SUM(t.montantusd)) s FROM traisorerie t WHERE t.reference = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, vente);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Double sumRecoveredByNumeroFacture(String vente, double taux) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ((SUM(t.montantcdf)/")
                    .append(taux)
                    .append(")+SUM(t.montantusd)) s FROM traisorerie t WHERE t.reference = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, vente);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Vente> findVenteCredit() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Vente> findVenteCredit(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.region = ? AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Vente> findVenteCreditByRef(String region, String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.region = ? AND v.reference = ? AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, region);
            query.setParameter(2, ref);
            query.setParameter(3, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Vente> findVenteCreditByRef(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.reference = ? AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, ref);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Vente> findVenteCreditByDateInterval(Date d1, Date d2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.dateVente BETWEEN ? AND ? AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);

            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE)
                    .setParameter(3, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Vente> findVenteCreditByDateInterval(Date d1, Date d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);

            query.setParameter(1, d1, TemporalType.DATE)
                    .setParameter(2, d2, TemporalType.DATE);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public <T> List<T> findByDateExpInterval(Class<T> tofind, Date date1, Date date2) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.dateExpir BETWEEN ? AND ? ");

            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.dateExpiry BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByDateExpInterval(Class<T> tofind, Date date1, Date date2, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.dateExpir BETWEEN ? AND ? AND s.region = ? ");

            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.dateExpiry BETWEEN ? AND ? AND s.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduit(Class<T> tofind, String prodUid) {
        StringBuilder sb = new StringBuilder();
        Query query;
        try {
            if (tofind.isAssignableFrom(Mesure.class)) {
                sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(Stocker.class)) {

                sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(Destocker.class)) {

                sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(Recquisition.class)) {

                sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(LigneVente.class)) {

                sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                return query.getResultList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T findMaxMesure(Class<T> tofind, String sqlCol, String prodUid) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(tofind.getSimpleName().toLowerCase())
                .append(" m WHERE m.produit_id = ? AND m.").append(sqlCol).append(" = (SELECT MAX(").append(sqlCol).append(") FROM ").append(tofind.getSimpleName().toLowerCase())
                .append(" n WHERE n.produit_id = ? ) ");
        Query query = em.createNativeQuery(sb.toString(), tofind);
        query.setParameter(1, prodUid);
        query.setParameter(2, prodUid);
        return (T) query.getSingleResult();
    }

    public <T> Double findMax(Class<T> tofind, String sqlCol, String prodUid) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT MAX(").append(sqlCol).append(") FROM ").append(tofind.getSimpleName().toLowerCase()).append(" m WHERE m.product_id = ? ");
        Query query = em.createNativeQuery(sb.toString());
        query.setParameter(1, prodUid);
        return (Double) query.getSingleResult();
    }

    public <T> Double findAvg(Class<T> tofind, String sqlCol, String prodUid) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT AVG(").append(sqlCol).append(") FROM ").append(tofind.getSimpleName().toLowerCase()).append(" m WHERE m.product_id = ? ");
        Query query = em.createNativeQuery(sb.toString());
        query.setParameter(1, prodUid);
        return (Double) query.getSingleResult();
    }

    public List<Traisorerie> getTresorTransactions(String traisor_id, String region) {
        StringBuilder sb = new StringBuilder();
        boolean reg = false;
        if (region == null) {
            sb.append("SELECT * FROM traisorerie t WHERE t.tresor_id = ?");
            reg = false;
        } else {
            sb.append("SELECT * FROM traisorerie t WHERE t.tresor_id = ? AND t.region = ?");
            reg = true;
        }

        Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
        if (reg) {
            query.setParameter(1, traisor_id)
                    .setParameter(2, region);
        } else {
            query.setParameter(1, traisor_id);
        }
        return query.getResultList();
    }

    public <T> Double findAvgFromRegion(Class<T> tofind, String sqlCol, String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT AVG(").append(sqlCol).append(") FROM ").append(tofind.getSimpleName())
                .append(" m WHERE m.product_id = ? ").append("AND m.region =? ");
        Query query = em.createNativeQuery(sb.toString(), tofind);
        query.setParameter(1, prodUid);
        query.setParameter(2, region);
        return (Double) query.getSingleResult();
    }

    public <T> List<T> findByProduit(Class<T> tofind, String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
            sb.append("AND m.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ");
            sb.append("AND s.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ");
            sb.append("AND d.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ");
            sb.append("AND r.region =? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ");
            sb.append("AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region =? ) ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduit(Class<T> tofind, String prodUid, String orderby, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? AND d.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {
            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? )");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    public ClientOrganisation findClientOrganisation(String idClient) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client_organisation o WHERE o.uid "
                + "IN (SELECT organisation_id FROM client_appartenir c WHERE c.client_id = ? )");
        try {
            Query query = em.createNativeQuery(sb.toString(), ClientOrganisation.class);
            query.setParameter(1, idClient);
            return (ClientOrganisation) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * use ksf_d79fe68013f4405b9519d0227eee306b; SELECT
     * v.dateVente,v.libelle,c.nom_client,p.nomproduit,k.nom_client,l.quantite,l.prixunit,(l.quantite*l.prixunit)
     * as tot, c.parent_id,v.observation FROM vente v,client c,ligne_vente
     * l,client k,client_appartenir a,client_organisation o,produit p WHERE
     * v.clientid_uid=c.uid AND c.parent_id=k.uid AND c.parent_id=a.client_id
     * AND l.product_id=p.uid AND a.client_organisation_id=o.uid AND
     * l.reference_uid=v.uid AND o.uid = "b24d578c8c0e49a4ba77bdc4e810cb1e" AND
     * v.datevente BETWEEN "2023-11-01" AND "2023-12-31"
     *
     * @param orgId
     * @param d1
     * @param d2
     * @return
     */
    public List<Relevee> getReleveFor(String orgId, Date d1, Date d2) {
        List<Relevee> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,v.libelle,c.nom_client,p.nomproduit,k.nom_client,l.quantite,l.prixunit,(l.quantite*l.prixunit) as tot, c.parent_id,v.observation, m.uid "
                + " FROM vente v,client c,ligne_vente l,client k,client_appartenir a,client_organisation o,produit p, mesure m"
                + " WHERE v.clientid_uid=c.uid AND c.parent_id=k.uid AND c.parent_id=a.client_id AND l.product_id=p.uid AND l.mesure_id=m.uid"
                + " AND a.client_organisation_id=o.uid AND l.reference_uid=v.uid AND o.uid = ? AND v.datevente BETWEEN ? AND ?");

        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, orgId)
                    .setParameter(2, d1, TemporalType.TIMESTAMP)
                    .setParameter(3, d2, TemporalType.TIMESTAMP);
            List<Object[]> objs = query.getResultList();

            for (Object[] obj : objs) {
                Relevee r = new Relevee();
                try {
                    r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(obj[0])));
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                String mid = String.valueOf(obj[10]);
                Mesure m = findByUid(Mesure.class, mid);
                r.setMesure(m);
                r.setNomClient(String.valueOf(obj[2]));
                r.setNomProduit(String.valueOf(obj[3]));
                r.setNumeroBon(String.valueOf(obj[1]));
                r.setParent(String.valueOf(obj[4]));
                r.setPrixunitaire(Double.parseDouble(String.valueOf(obj[6])));
                r.setQuantite(Double.parseDouble(String.valueOf(obj[5])));
                r.setMontant(Double.parseDouble(String.valueOf(obj[7])));
                result.add(r);
            }
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Facture> getSubsBills(String billno, String org, Date d1, Date d2, double taux) {
        List<Facture> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT MONTHNAME(v.dateVente),v.libelle,v.region,SUM(t.montantusd+t.montantcdf/")
                .append(taux)
                .append(") as pyd, SUM(l.quantite*l.prixunit) as tot "
                        + "FROM vente v,ligne_vente l,client c,client as k,traisorerie t,client_appartenir a"
                        + " WHERE l.reference_uid=v.uid AND v.client_id=c.uid AND "
                        + "c.parent_id = k.uid AND k.uid=a.client_id AND a.client_organisation_id = ? "
                        + "AND t.libelle LIKE ? AND v.libelle LIKE ? AND v.datevente BETWEEN ? AND ?"
                        + " GROUP BY MONTHNAME(v.datevente)");

        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, org)
                    .setParameter(2, "%" + billno + "%")
                    .setParameter(3, "%" + billno + "%")
                    .setParameter(4, d1, TemporalType.TIMESTAMP)
                    .setParameter(5, d2, TemporalType.TIMESTAMP);
            List<Object[]> objs = query.getResultList();

            for (Object[] obj : objs) {
                Facture f = new Facture();
                f.setEndDate(d2);
                f.setNumero(billno);
                f.setOrganisId(new ClientOrganisation(org));
                f.setPayedamount(Double.valueOf(String.valueOf(obj[3])));
                f.setRegion(String.valueOf(obj[2]));
                f.setStartDate(d1);
                f.setTotalamount(Double.valueOf(String.valueOf(obj[3])));
                result.add(f);
            }
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> List<T> findByProduitAsc(Class<T> tofind, String prodUid, String orderby, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ?  ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? AND d.region = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ?  ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ?)");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitAsc(Class<T> tofind, String prodUid, String orderby) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" ASC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitDesc(Class<T> tofind, String prodUid, String orderby) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitDesc(Class<T> tofind, String prodUid, String orderby, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Mesure.class)) {
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? AND d.region = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? AND v.observation != ? )");
            sb.append(" ORDER BY ").append(orderby).append(" DESC ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setParameter(3, "Drafted");
            return query.getResultList();
        }
        return null;
    }

    public <T> Double sumUnitByProduitWithLot(Class<T> tofind, String prodUid, String lot) {
        StringBuilder sb = new StringBuilder();
        Query query;
        Double result = 0d;
        try {
            if (tofind.isAssignableFrom(Stocker.class)) {

                sb.append("SELECT SUM(t1.tot) as dt FROM (SELECT SUM(s.quantite*m.quantcontenu) as tot FROM stocker s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid GROUP BY s.mesure_id) as t1 ");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                result = (Double) query.getSingleResult();
            } else if (tofind.isAssignableFrom(Destocker.class)) {

                sb.append("SELECT SUM(t1.tot) as dt FROM (SELECT SUM(s.quantite*m.quantcontenu) as tot FROM destocker s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid GROUP BY s.mesure_id) as t1");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                result = (Double) query.getSingleResult();
            } else if (tofind.isAssignableFrom(Recquisition.class)) {

                sb.append("SELECT SUM(t1.tot) as tr FROM (SELECT SUM(s.quantite*m.quantcontenu) as tot FROM recquisition s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid GROUP BY s.mesure_id) as t1");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                result = (Double) query.getSingleResult();
            } else if (tofind.isAssignableFrom(LigneVente.class)) {
                sb.append("SELECT SUM(t1.tot) as dt FROM (SELECT SUM(s.quantite*m.quantcontenu) as tot FROM ligne_vente s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid GROUP BY s.mesure_id) as t1");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                result = (Double) query.getSingleResult();
            }
        } catch (jakarta.persistence.NoResultException ex) {
            return 0d;
        }
        return result == null ? 0 : result;
    }

    public Double sumUnitRetourMagasin(String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        Double result;
        try {
            if (region != null) {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM retour_magasin s, mesure m, ligne_vente l "
                        + "WHERE s.product_id = ? AND s.region = ?  AND s.mesure_id=m.uid AND s.linge_vente_id=l.uid GROUP BY s.mesure_id ");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, region);
            } else {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM retour_magasin s, mesure m,ligne_vente l "
                        + "WHERE s.product_id = ? AND s.linge_vente_id=l.uid AND s.mesure_id=m.uid GROUP BY s.mesure_id ");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
            }
            result = (Double) query.getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return 0d;
        }
        return result == null ? 0 : result;
    }

    public <T> Double sumUnitByProduitWithLot(Class<T> tofind, String prodUid, String lot, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        Double result = 0d;
        try {
            if (tofind.isAssignableFrom(Stocker.class)) {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM stocker s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.region = ?  AND s.mesure_id=m.uid GROUP BY s.mesure_id ");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                query.setParameter(3, region);
                result = (Double) query.getSingleResult();
            } else if (tofind.isAssignableFrom(Destocker.class)) {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM destocker s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.region = ? AND s.mesure_id=m.uid GROUP BY s.mesure_id");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                query.setParameter(3, region);
                result = (Double) query.getSingleResult();
            } else if (tofind.isAssignableFrom(Recquisition.class)) {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM recquisition s, mesure m "
                        + "WHERE s.product_id = ? AND s.numlot = ?  AND s.region = ? AND s.mesure_id=m.uid GROUP BY s.mesure_id");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                query.setParameter(3, region);
                result = (Double) query.getSingleResult();
            } else if (tofind.isAssignableFrom(LigneVente.class)) {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM ligne_vente s, mesure m, vente v "
                        + "WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid AND s.reference_uid = v.uid AND v.region = ? GROUP BY s.mesure_id");
                query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                query.setParameter(3, region);
                result = (Double) query.getSingleResult();
            }
        } catch (jakarta.persistence.NoResultException ex) {
            return 0d;
        }
        return result == null ? 0 : result;
    }

    public <T> List<T> findByProduitWithLot(Class<T> tofind, String prodUid, String lot) {
        StringBuilder sb = new StringBuilder();
        Query query;
        try {
            if (tofind.isAssignableFrom(Stocker.class)) {

                sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                        .append(" AND s.numlot = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(Destocker.class)) {

                sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ")
                        .append(" AND d.numlot = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(Recquisition.class)) {

                sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                        .append(" AND r.numlot = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                return query.getResultList();
            } else if (tofind.isAssignableFrom(LigneVente.class)) {

                sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ")
                        .append(" AND l.numlot = ? ");
                query = em.createNativeQuery(sb.toString(), tofind);
                query.setParameter(1, prodUid);
                query.setParameter(2, lot);
                return query.getResultList();
            }
        } catch (jakarta.persistence.NoResultException ex) {
            return null;
        }
        return null;
    }

    public <T> List<T> findByProduitWithLotAsc(Class<T> tofind, String prodUid, String lot, String orderbyField) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" AND s.numlot = ? ");
            sb.append("ORDER BY s.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ")
                    .append(" AND d.numlot = ? ");
            sb.append("ORDER BY d.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" AND r.numlot = ? ");
            sb.append("ORDER BY r.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ")
                    .append(" AND l.numlot = ? ");
            sb.append("ORDER BY l.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitWithLotDesc(Class<T> tofind, String prodUid, String lot, String orderbyField) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" AND s.numlot = ? ");
            sb.append("ORDER BY s.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ")
                    .append(" AND d.numlot = ? ");
            sb.append("ORDER BY d.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" AND r.numlot = ? ");
            sb.append("ORDER BY r.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ")
                    .append(" AND l.numlot = ? ");
            sb.append("ORDER BY l.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitWithLotAsc(Class<T> tofind, String prodUid, String lot, String orderbyField, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" AND s.numlot = ?  AND s.region = ?  ");
            sb.append("ORDER BY s.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ")
                    .append(" AND d.numlot = ?  AND d.region = ?  ");
            sb.append("ORDER BY d.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" AND r.numlot = ?  AND r.region = ?  ");
            sb.append("ORDER BY r.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ")
                    .append(" AND l.numlot = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? AND v.observation != ? ) ");
            sb.append("ORDER BY l.").append(orderbyField).append(" ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitWithLotDesc(Class<T> tofind, String prodUid, String lot, String orderbyField, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" AND s.numlot = ?  AND s.region = ? ");
            sb.append("ORDER BY s.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ")
                    .append(" AND d.numlot = ?  AND d.region = ? ");
            sb.append("ORDER BY d.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" AND r.numlot = ?  AND r.region = ? ");
            sb.append("ORDER BY r.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ")
                    .append(" AND l.numlot = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? AND v.observation != ? ) ");
            sb.append("ORDER BY l.").append(orderbyField).append(" DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitWithLot(Class<T> tofind, String prodUid, String lot, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" AND s.numlot = ? AND s.region = ?  ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE d.product_id = ? ")
                    .append(" AND d.numlot = ? AND d.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" AND r.numlot = ? AND r.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE l.product_id = ? ")
                    .append(" AND l.numlot = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? AND v.observation != ? ) ");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByLot(Class<T> tofind, String lot, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE ")
                    .append(" s.numlot = ? AND s.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE ")
                    .append(" d.numlot = ? AND d.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE ")
                    .append(" r.numlot = ? AND r.region = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE ")
                    .append(" l.numlot = ? AND l.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? ) ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByLot(Class<T> tofind, String lot) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE ")
                    .append(" s.numlot = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);

            return query.getResultList();
        } else if (tofind.isAssignableFrom(Destocker.class)) {

            sb.append("SELECT * FROM destocker d WHERE ")
                    .append(" d.numlot = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);

            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE ")
                    .append(" r.numlot = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);

            return query.getResultList();
        } else if (tofind.isAssignableFrom(LigneVente.class)) {

            sb.append("SELECT * FROM ligne_vente l WHERE ")
                    .append(" l.numlot = ? ");
            query = em.createNativeQuery(sb.toString(), tofind);

            query.setParameter(1, lot);

            return query.getResultList();
        }
        return null;
    }

    /**
     * To apply to First Expired First Out
     *
     * @param <T>
     * @param tofind
     * @param prodUid
     * @param lot
     * @return
     */
    public <T> List<T> findByProduitOrderByExp(Class<T> tofind, String prodUid) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" ORDER BY s.dateExpir ASC");

            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" ORDER BY dateExpiry ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitOrderByExpLimited(Class<T> tofind, String prodUid, int st, int max) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" ORDER BY s.dateExpir ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setFirstResult(st);
            query.setMaxResults(max);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" ORDER BY dateExpiry ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setFirstResult(st);
            query.setMaxResults(max);
            return query.getResultList();
        }
        return null;
    }

    /**
     * FEFO by region and produit
     *
     * @param <T> type d'objet a traiter
     * @param tofind la class d'objet a traiter
     * @param prodUid l'id du produit en argument
     * @param region la region en argument
     * @return
     */
    public <T> List<T> findByProduitOrderByExp(Class<T> tofind, String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ")
                    .append(" ORDER BY s.dateExpir ASC");

            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ")
                    .append(" ORDER BY dateExpiry ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    /**
     * FEFO by region and produit limited
     *
     * @param <T> type d'objet a traiter
     * @param tofind la class d'objet a traiter
     * @param prodUid l'id du produit en argument
     * @param region la region en argument
     * @return
     */
    public <T> List<T> findByProduitOrderByExpLimited(Class<T> tofind, String prodUid, String region, int st, int lim) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ")
                    .append(" ORDER BY s.dateExpir ASC");

            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setFirstResult(st);
            query.setMaxResults(lim);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ")
                    .append(" ORDER BY dateExpiry ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setFirstResult(st);
            query.setMaxResults(lim);
            return query.getResultList();
        }
        return null;
    }

    /**
     * To apply in First In First Out
     *
     * @param <T>
     * @param tofind
     * @param prodUid
     * @param lot
     * @return
     */
    public <T> List<T> findByProduitOrderByDateAsc(Class<T> tofind, String prodUid) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" ORDER BY s.dateStocker ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" ORDER BY date ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitOrderByDateAsc(Class<T> tofind, String prodUid, int st, int max) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" ORDER BY s.dateStocker ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setFirstResult(st);
            query.setMaxResults(max);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" ORDER BY date ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setFirstResult(st);
            query.setMaxResults(max);
            return query.getResultList();
        }
        return null;
    }

    /**
     * To apply in Last In First Out
     *
     * @param <T>
     * @param tofind
     * @param prodUid
     * @param lot
     * @return
     */
    public <T> List<T> findByProduitOrderByDateDesc(Class<T> tofind, String prodUid) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" ORDER BY s.dateStocker DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" ORDER BY date DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitOrderByDateDesc(Class<T> tofind, String prodUid, int st, int lim) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ")
                    .append(" ORDER BY s.dateStocker DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setFirstResult(st);
            query.setMaxResults(lim);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? ")
                    .append(" ORDER BY date DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setFirstResult(st);
            query.setMaxResults(lim);
            return query.getResultList();
        }
        return null;
    }

    /**
     * LIFO by region
     *
     * @param <T>
     * @param tofind
     * @param prodUid
     * @param region
     * @return
     */
    public <T> List<T> findByProduitOrderByDateDescByRegion(Class<T> tofind, String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ")
                    .append(" ORDER BY s.dateStocker DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ")
                    .append(" ORDER BY date DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitOrderByDateDescByRegion(Class<T> tofind, String prodUid, String region, int st, int lim) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ")
                    .append(" ORDER BY s.dateStocker DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setFirstResult(st);
            query.setMaxResults(lim);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ")
                    .append(" ORDER BY date DESC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setFirstResult(st);
            query.setMaxResults(lim);
            return query.getResultList();
        }
        return null;
    }

    /**
     * FIFO by region
     *
     * @param <T>
     * @param tofind
     * @param prodUid
     * @param region
     * @return
     */
    public <T> List<T> findByProduitOrderByDateAscByRegion(Class<T> tofind, String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ")
                    .append(" ORDER BY s.dateStocker ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ")
                    .append(" ORDER BY date ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findByProduitOrderByDateAscByRegion(Class<T> tofind, String prodUid, String region, int st, int max) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (tofind.isAssignableFrom(Stocker.class)) {

            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ")
                    .append(" ORDER BY s.dateStocker ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setFirstResult(st);
            query.setMaxResults(max);
            return query.getResultList();
        } else if (tofind.isAssignableFrom(Recquisition.class)) {

            sb.append("SELECT * FROM recquisition r WHERE r.product_id = ? AND r.region = ? ")
                    .append(" ORDER BY date ASC");
            query = em.createNativeQuery(sb.toString(), tofind);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            query.setFirstResult(st);
            query.setMaxResults(max);
            return query.getResultList();
        }
        return null;
    }

    public Client getAnonymousClient() {
        Query query = em.createNativeQuery("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ", Client.class);
        query.setParameter(1, "Unknown").setParameter(2, "Anonyme").setParameter(3, "09000");
        List<Client> anonymous = query.getResultList();
        if (anonymous.isEmpty()) {
            Client c = new Client(DataId.generate());
            c.setAdresse("Anonynme");
            c.setEmail("Anonyme");
            c.setNomClient("Anonyme");
            c.setPhone("Unknown");
            c.setTypeClient("Consommateur");
            return insertAndSync(c);
        } else {
            return anonymous.get(0);
        }
    }

    public <T> List<T> findAllByDate(Class<T> type, Date date) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l WHERE l.date = ?  ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.dateStocker = ?  ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.dateDestockage = ?  ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.date = ?  ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.dateVente = ?  AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.date = ?  ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.date = ?  ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date, TemporalType.DATE);
            return query.getResultList();
        }
        return null;
    }

    /**
     * for populating dashboard
     *
     * @param <T>
     * @param type
     * @param date1
     * @param date2
     * @return
     */
    public <T> List<T> findAllByDateInterval(Class<T> type, Date date1, Date date2) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l WHERE l.date BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.dateStocker BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.dateDestockage BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.date BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, "Dafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.date BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.date BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a WHERE a.date BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd WHERE rd.date_ BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.date_ BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } else if (type.isAssignableFrom(Facture.class)) {
            sb.append("SELECT * FROM facture f WHERE f.start_date BETWEEN ? AND ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        }
        return null;
    }

    public List<Facture> getFacturesByOrg(String orgUid) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM facture f WHERE f.organis_id = ?");
            Query query = em.createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, orgUid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Facture> getUnpaidFacturesByOrg(String orgUid) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM facture f WHERE f.organis_id = ? AND (f.status = ? OR f.status = ?) ");
            Query query = em.createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, orgUid);
            query.setParameter(2, Constants.BILL_STATUS_UNPAID);
            query.setParameter(3, Constants.BILL_STATUS_INPAYMENT);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<InventoryMagasin> getShopInventories(String region) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT p.codebar,p.nomproduit,p.modele,p.marque,p.taille,p.couleur"
                + ", m.quantcontenu,m.description,SUM(r.quantite/m.quantcontenu) as entree, r.numlot,SUM(l.quantite/m.quant_contenu) as sortie"
                + ", (SUM(r.quantite/m.quantcontenu)-SUM(l.quantite/m.quant_contenu)) as reste, l.numlot FROM produit p,mesure m,recquisition r,ligne_vente l"
                + " WHERE m.produit_id=p.uid AND p.uid=r.product_id AND r.mesure_id=m.uid AND l.product_id=p.uid GROUP BY p.uid ");
        return null;
    }

    public <T> List<T> findEq(Class<T> resultClass, String colParam, String colValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(resultClass.getSimpleName().toLowerCase()).append(" x WHERE x.").append(colParam).append(" = ? ");
        Query query = em.createNativeQuery(sb.toString(), resultClass);
        query.setParameter(1, colValue);
        return query.getResultList();
    }

    public Double sumVente(Date date1, Date date2, double taux) {
        Double usd = sumVenteUsd(date1, date2, taux);
        Double dette = sumVenteDette(date1, date2);
        Double rtr = sumRetourMagasin(date1, date2);
        return (usd + dette) - rtr;
    }

    public Double sumVente(Date date1, Date date2, String region, double taux) {
        Double usd = sumVenteUsd(date1, date2, region, taux);
        //Double dette = sumVenteDette(date1, date2, region);
        Double rtr = sumRetourMagasin(date1, date2, region);
        return (usd - rtr);
    }

    public Double sumExpenses(Date date1, Date date2, String region, double taux) {
        Double usd = sumOpsUsd(date1, date2, region);
        Double cdf = sumOpsCdf(date1, date2, region);
        return usd + (cdf / taux);
    }

    public Double sumExpenses(Date date1, Date date2, double taux) {
        Double usd = sumOpsUsd(date1, date2);
        Double cdf = sumOpsCdf(date1, date2);
        return usd + (cdf / taux);
    }

    public Double sumVenteUsd(Date date1, Date date2, double taux) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ((SUM(v.montantcdf)/").append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                .append(" WHERE v.dateVente BETWEEN ? AND ?  AND v.observation != ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, "Drafted");
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsd(Date date1, Date date2, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ((SUM(v.montantcdf)/").append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                .append(" WHERE v.dateVente BETWEEN ? AND ? AND v.region = ?  AND v.observation != ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public long getSaleItemCount(int vuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) as c FROM ligne_vente v ")
                .append(" WHERE v.reference_uid = ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, vuid);
            Long r = (Long) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0l;
        }
    }

    public List<Stocker> findStockerByLivr(String livid) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM stocker v ")
                .append(" WHERE v.livraisid_uid = ? ");
        try {
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, livid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public HashMap<String, Double> getSalesInPerod(Date d1, Date d2, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Double> result = new HashMap();
        if (region == null) {
            sb.append("SELECT (SUM(v.montantUsd)+(SUM(v.montantCdf)/").append(taux).append(")) as amount, MONTHNAME(v.dateVente) mois FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.observation != ? GROUP BY MONTHNAME(v.dateVente) Order By MONTH(v.dateVente) asc");
        } else {

            sb.append("SELECT (SUM(v.montantUsd)+(SUM(v.montantCdf)/").append(taux).append(")) as amount, MONTHNAME(v.dateVente) mois FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? GROUP BY MONTHNAME(v.dateVente) Order By MONTH(v.dateVente) asc");
        }
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE);
            query.setParameter(2, d2, TemporalType.DATE);
            if (region != null) {
                query.setParameter(3, region);
                query.setParameter(4, "Drafted");
            } else {
                query.setParameter(3, "Drafted");
            }

            List<Object[]> objs = query.getResultList();
            for (Object[] obj : objs) {
                String mois = String.valueOf(obj[1]);
                double am = Double.valueOf(String.valueOf(obj[0]));
                result.put(mois, am);
            }

        } catch (NoResultException e) {

        }
        return result;
    }

    public Double sumCoutAchatArticleVendu(Date d1, Date d2, String region) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id=w.product_id) as t");
        } else {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id = w.product_id) as t");
        }
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE);
            query.setParameter(2, d2, TemporalType.DATE);
            if (region != null) {
                query.setParameter(3, region);
                query.setParameter(4, "Drafted");
            } else {
                query.setParameter(3, "Drafted");
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumCoutAchatArticleVendu(String monthname, String region) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE MONTHNAME(v.dateVente) = ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id=w.product_id) as t");
        } else {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE MONTHNAME(v.dateVente) = ? AND v.region = ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id = w.product_id) as t");
        }
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, monthname);
            if (region != null) {
                query.setParameter(2, region);
                query.setParameter(3, "Drafted");
            } else {
                query.setParameter(2, "Drafted");
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsdx(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumUSDRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public double sumExpenses(String monthName, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT (SUM(o.montantUsd)+ (SUM(o.montantCdf)*").append(taux)
                    .append(")) as tot FROM operation o WHERE MONTHNAME(o.date) = ? ");
        } else {
            sb.append("SELECT (SUM(o.montantUsd)+ (SUM(o.montantCdf)*").append(taux)
                    .append(")) as tot FROM operation o WHERE MONTHNAME(o.date) = ? AND o.region = ? ");
        }
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, monthName);
            if (region != null) {
                query.setParameter(2, region);
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsUsd(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Operation.findSumUSDByDateIntervalRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Operation.findSumCDFByDateIntervalRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Operation.findSumCDFByDateInterval");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsUsd(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Operation.findSumUSDByDateInterval");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteCdf(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumCDF");

            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public List<Vente> findVentes(Date d1, Date d2, String region) {
        List<Vente> ventes;
        if (region == null) {
            ventes = findAllByDateInterval(Vente.class, d1, d2);
        } else {
            ventes = findAllByDateIntervalInRegion(Vente.class, d1, d2, region);
        }
        return ventes;
    }

    public Double sumVenteCdf(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumCDFRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumDebt");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumDebtRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    /**
     * for populating dashboard in region
     *
     * @param <T>
     * @param type
     * @param date1
     * @param date2
     * @param region
     * @return
     */
    public <T> List<T> findAllByDateIntervalInRegion(Class<T> type, Date date1, Date date2, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;

        if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l WHERE l.date BETWEEN ? AND ? AND l.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.dateStocker BETWEEN ? AND ? AND s.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.dateDestockage BETWEEN ? AND ? AND d.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.date BETWEEN ? AND ? AND r.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.date BETWEEN ? AND ? AND t.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.date BETWEEN ? AND ? AND o.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a WHERE a.date BETWEEN ? AND ? AND a.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd WHERE rd.date_ BETWEEN ? AND ? AND rd.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.date_ BETWEEN ? AND ? AND rm.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        }
        return null;
    }

    public List<Stocker> findStocksAtLocation(String location) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM stocker s WHERE s.localisation = ? ");
        query = em.createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, location);
        return query.getResultList();
    }

    public List<Stocker> findStocksAtLocation(String location, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM stocker s WHERE s.localisation = ? AND s.region = ? ");
        query = em.createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, location);
        query.setParameter(2, region);
        return query.getResultList();
    }

    public List<Aretirer> findRetraitByStatus(String status, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM aretirer s WHERE s.status = ? AND s.region = ? ");
        query = em.createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, status);
        query.setParameter(2, region);
        return query.getResultList();
    }

    public List<Aretirer> findRetraitByStatus(String status) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM aretirer s WHERE s.status = ? ");
        query = em.createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, status);
        return query.getResultList();
    }

    public <T> List<T> findAllByRegion(Class<T> type, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        try {
            if (type.isAssignableFrom(Livraison.class)) {
                sb.append("SELECT * FROM livraison l WHERE l.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Stocker.class)) {
                sb.append("SELECT * FROM stocker s WHERE s.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Destocker.class)) {
                sb.append("SELECT * FROM destocker d WHERE d.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Recquisition.class)) {
                sb.append("SELECT * FROM recquisition r WHERE r.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Vente.class)) {
                sb.append("SELECT * FROM vente v WHERE v.region = ? AND v.observation != ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                query.setParameter(2, "Drafted");
                return query.getResultList();
            } else if (type.isAssignableFrom(Traisorerie.class)) {
                sb.append("SELECT * FROM traisorerie t WHERE t.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(CompteTresor.class)) {
                sb.append("SELECT * FROM compte_tresor t WHERE t.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Depense.class)) {
                sb.append("SELECT * FROM depense t WHERE t.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Operation.class)) {
                sb.append("SELECT * FROM operation o WHERE o.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(ClientAppartenir.class)) {
                sb.append("SELECT * FROM client_appartenir ca WHERE ca.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(ClientOrganisation.class)) {
                sb.append("SELECT * FROM client_organisation co WHERE co.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Aretirer.class)) {
                sb.append("SELECT * FROM aretirer a WHERE a.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(RetourDepot.class)) {
                sb.append("SELECT * FROM retour_depot rd WHERE rd.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(RetourMagasin.class)) {
                sb.append("SELECT * FROM retour_magasin rm WHERE rm.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            } else if (type.isAssignableFrom(Facture.class)) {
                sb.append("SELECT * FROM facture f WHERE f.region = ? ");
                query = em.createNativeQuery(sb.toString(), type);
                query.setParameter(1, region);
                return query.getResultList();
            }
        } catch (Exception e) {

        }
        return new ArrayList<>();
    }

    /**
     * For paginating result and ordering asc
     *
     * @param <T>
     * @param type
     * @param sqlCol
     * @param start
     * @param limit
     * @return
     */
    public <T> List<T> findAllByAscOrdering(Class<T> type, String sqlCol, int start, int limit) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison s  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.observation != ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, "Drafted");
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t  ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findAllByAscOrdering(Class<T> type, String sqlCol) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison s  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v  WHERE v.observation != ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findAllByDescOrdering(Class<T> type, String sqlCol, int start, int limit) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.observation != ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, "Drafted");
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findAllByDescOrdering(Class<T> type, String sqlCol) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.observation != ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findAllByAscOrdering(Class<T> type, String sqlCol, String region, int start, int limit) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison s  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.region = ? AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca WHERE ca.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co WHERE co.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a WHERE a.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd WHERE rd.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findAllByAscOrdering(Class<T> type, String sqlCol, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison s  ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.region = ? AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca WHERE ca.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co WHERE co.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a WHERE a.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd WHERE rd.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        }
        return null;
    }

    public <T> List<T> findAllByDescOrdering(Class<T> type, String sqlCol, String region, int start, int limit) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l WHERE l.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.region = ? AND v.observation != ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca WHERE ca.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co WHERE co.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a WHERE a.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd WHERE rd.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.region = ? ");
            query = em.createNativeQuery(sb.toString(), type);
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return query.getResultList();
        }
        return null;
    }

    public Produit findByCodebarr(String codebar) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit l WHERE l.codebar = ?");
            Query query = em.createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, codebar);
            return (Produit) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Produit> findProduitLike(String prod) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT  * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ', p.couleur) LIKE ?");
        try {
            Query q = em.createNativeQuery(sb.toString(), Produit.class);
            q.setParameter(1, "%" + prod + "%");
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> List<T> findAllByDescOrdering(Class<T> type, String sqlCol, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        if (type.isAssignableFrom(Produit.class)) {
            sb.append("SELECT * FROM produit l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Client.class)) {
            sb.append("SELECT * FROM client l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Fournisseur.class)) {
            sb.append("SELECT * FROM fournisseur l ");
            sb.append(" ORDER BY ").append(sqlCol).append(" ASC");
            query = em.createNativeQuery(sb.toString(), type);

            return query.getResultList();
        } else if (type.isAssignableFrom(Livraison.class)) {
            sb.append("SELECT * FROM livraison l WHERE l.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Stocker.class)) {
            sb.append("SELECT * FROM stocker s WHERE s.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Destocker.class)) {
            sb.append("SELECT * FROM destocker d WHERE d.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Recquisition.class)) {
            sb.append("SELECT * FROM recquisition r WHERE r.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Vente.class)) {
            sb.append("SELECT * FROM vente v WHERE v.region = ? AND v.observation != ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } else if (type.isAssignableFrom(Traisorerie.class)) {
            sb.append("SELECT * FROM traisorerie t WHERE t.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Operation.class)) {
            sb.append("SELECT * FROM operation o WHERE o.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientAppartenir.class)) {
            sb.append("SELECT * FROM client_appartenir ca WHERE ca.region = ?  ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(ClientOrganisation.class)) {
            sb.append("SELECT * FROM client_organisation co WHERE co.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(Aretirer.class)) {
            sb.append("SELECT * FROM aretirer a WHERE a.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourDepot.class)) {
            sb.append("SELECT * FROM retour_depot rd WHERE rd.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        } else if (type.isAssignableFrom(RetourMagasin.class)) {
            sb.append("SELECT * FROM retour_magasin rm WHERE rm.region = ? ");
            sb.append(" ORDER BY ").append(sqlCol).append(" DESC");
            query = em.createNativeQuery(sb.toString(), type);
            query.setParameter(1, region);

            return query.getResultList();
        }
        return null;
    }

    public List<PrixDeVente> findPricesForReq(String reqid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente p WHERE p.recquisition_id = ? ");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, reqid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public PrixDeVente getSpecificPriceByQuant(Recquisition req, Mesure mesure, double quant) {
        String reqId = req.getUid();
        List<PrixDeVente> pvs = delegue(reqId, mesure.getUid(), quant);
        if (!pvs.isEmpty()) {
            PrixDeVente pv = pvs.get(0);
            if (pv == null) {
                Produit p = req.getProductId();
                List<Mesure> mezs = findByProduit(Mesure.class, p.getUid());
                for (Mesure mez : mezs) {
                    List<PrixDeVente> pdgs = delegue(reqId, mez.getUid(), 1);
                    if (pdgs.isEmpty()) {
                        continue;
                    }
                    PrixDeVente pvx = pdgs.get(0);
                    Mesure m = pvx.getMesureId();
                    if (m == null) {
                        continue;
                    }
                    double qm = m.getQuantContenu();
                    double pu = (pvx.getPrixUnitaire() * mesure.getQuantContenu()) / qm;
                    PrixDeVente pvt = new PrixDeVente(DataId.generate());
                    pvt.setDevise("USD");
                    pvt.setMesureId(mesure);
                    pvt.setPourcentParCunit(0d);
                    pvt.setQmax(1d);
                    pvt.setQmin(10000000d);
                    pvt.setPrixUnitaire(pu);
                    pvt.setRecquisitionId(req);
                    return pvt;
                }
                return null;
            } else {
                return pv;
            }
        }
        return null;
    }

    private List<PrixDeVente> delegue(String reqId, String mesure, double quant) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM prix_de_vente p WHERE p.recquisition_id = ? AND p.mesureid_uid = ? AND p.q_min <= ? AND p.q_max >= ? ");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, reqId);
            query.setParameter(2, mesure);
            query.setParameter(3, quant);
            query.setParameter(4, quant);
            return query.getResultList();
        } catch (jakarta.persistence.NoResultException ex) {
            return null;
        }
    }

    /**
     * Querying object by fields and values
     *
     * @param <T>
     * @param type
     * @param fields
     * @param values
     * @return
     */
    public <T> List<T> findWithAndClause(Class<T> type, String[] fields, Object[] values) {
        try {
            StringBuilder sb = new StringBuilder();
            if (fields.length != values.length || fields.length == 0 || values.length == 0 || type == null) {
                throw new IllegalArgumentException();
            }
            String tab = type.getSimpleName().toLowerCase();

            System.out.println("Table " + tab);
            if (tab.equalsIgnoreCase("lignevente")) {
                tab = "ligne_vente";
            } else if (tab.equalsIgnoreCase("prixdevente")) {
                tab = "prix_de_vente";
            } else if (tab.equalsIgnoreCase("retourdepot")) {
                tab = "retour_depot";
            } else if (tab.equalsIgnoreCase("retour_magasin")) {
                tab = "retour_magasin";
            } else if (tab.equalsIgnoreCase("clientappartenir")) {
                tab = "client_appartenir";
            } else if (tab.equalsIgnoreCase("client_organisation")) {
                tab = "client_organisation";
            } else if (tab.equalsIgnoreCase("comptetresor")) {
                tab = "compte_tresor";
            }
            sb.append("SELECT * FROM ").append(tab).append(" x WHERE ");
            String lastfield = fields[fields.length - 1];
            for (String field : fields) {
                if (field.equals(lastfield)) {
                    sb.append("x.").append(field).append(" = ? ");
                } else {
                    sb.append("x.").append(field).append(" = ? AND ");
                }
            }
            Query query;
            query = em.createNativeQuery(sb.toString(), type);
            for (int i = 0; i < values.length; i++) {
                query.setParameter((i + 1), values[i]);
            }
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> List<T> findWithOrClause(Class<T> type, String[] fields, Object[] values) {
        try {
            StringBuilder sb = new StringBuilder();
            if (fields.length != values.length || fields.length == 0 || values.length == 0 || type == null) {
                throw new IllegalArgumentException();
            }
            String tab = type.getSimpleName().toLowerCase();

            System.out.println("Table " + tab);
            if (tab.equalsIgnoreCase("lignevente")) {
                tab = "ligne_vente";
            } else if (tab.equalsIgnoreCase("prixdevente")) {
                tab = "prix_de_vente";
            } else if (tab.equalsIgnoreCase("retourdepot")) {
                tab = "retour_depot";
            } else if (tab.equalsIgnoreCase("retour_magasin")) {
                tab = "retour_magasin";
            } else if (tab.equalsIgnoreCase("clientappartenir")) {
                tab = "client_appartenir";
            } else if (tab.equalsIgnoreCase("client_organisation")) {
                tab = "client_organisation";
            } else if (tab.equalsIgnoreCase("comptetresor")) {
                tab = "compte_tresor";
            }
            sb.append("SELECT * FROM ").append(tab).append(" x WHERE ");
            String lastfield = fields[fields.length - 1];
            for (String field : fields) {
                if (field.equals(lastfield)) {
                    sb.append("x.").append(field).append(" = ? ");
                } else {
                    sb.append("x.").append(field).append(" = ? OR ");
                }
            }
            Query query;
            query = em.createNativeQuery(sb.toString(), type);
            for (int i = 0; i < values.length; i++) {
                query.setParameter((i + 1), values[i]);
            }
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> List<T> findWithAndClauseAsc(Class<T> type, String[] fields, Object[] values, String orderbyField) {
        try {
            StringBuilder sb = new StringBuilder();
            if (fields.length != values.length || fields.length == 0 || values.length == 0 || type == null) {
                throw new IllegalArgumentException();
            }
            String tab = type.getSimpleName().toLowerCase();

            System.out.println("Table " + tab);
            if (tab.equalsIgnoreCase("lignevente")) {
                tab = "ligne_vente";
            } else if (tab.equalsIgnoreCase("prixdevente")) {
                tab = "prix_de_vente";
            } else if (tab.equalsIgnoreCase("retourdepot")) {
                tab = "retour_depot";
            } else if (tab.equalsIgnoreCase("retour_magasin")) {
                tab = "retour_magasin";
            } else if (tab.equalsIgnoreCase("clientappartenir")) {
                tab = "client_appartenir";
            } else if (tab.equalsIgnoreCase("client_organisation")) {
                tab = "client_organisation";
            } else if (tab.equalsIgnoreCase("comptetresor")) {
                tab = "compte_tresor";
            }
            sb.append("SELECT * FROM ").append(tab).append(" x WHERE ");
            String lastfield = fields[fields.length - 1];
            for (String field : fields) {
                if (field.equals(lastfield)) {
                    sb.append("x.").append(field).append(" = ? AND s.product_id = ? ");
                } else {
                    sb.append("x.").append(field).append(" = ? AND ");
                }
            }
            sb.append("ORDER BY x.").append(orderbyField).append(" ASC");
            Query query;
            query = em.createNativeQuery(sb.toString(), type);
            for (int i = 0; i < values.length; i++) {
                query.setParameter((i + 1), values[i]);
            }
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> List<T> findWithAndClauseDesc(Class<T> type, String[] fields, Object[] values, String orderbyField) {
        try {
            StringBuilder sb = new StringBuilder();
            if (fields.length != values.length || fields.length == 0 || values.length == 0 || type == null) {
                throw new IllegalArgumentException();
            }
            String tab = type.getSimpleName().toLowerCase();

            System.out.println("Table " + tab);
            if (tab.equalsIgnoreCase("lignevente")) {
                tab = "ligne_vente";
            } else if (tab.equalsIgnoreCase("prixdevente")) {
                tab = "prix_de_vente";
            } else if (tab.equalsIgnoreCase("retourdepot")) {
                tab = "retour_depot";
            } else if (tab.equalsIgnoreCase("retour_magasin")) {
                tab = "retour_magasin";
            } else if (tab.equalsIgnoreCase("clientappartenir")) {
                tab = "client_appartenir";
            } else if (tab.equalsIgnoreCase("client_organisation")) {
                tab = "client_organisation";
            } else if (tab.equalsIgnoreCase("comptetresor")) {
                tab = "compte_tresor";
            }
            sb.append("SELECT * FROM ").append(tab).append(" x WHERE ");
            String lastfield = fields[fields.length - 1];
            for (String field : fields) {
                if (field.equals(lastfield)) {
                    sb.append("x.").append(field).append(" = ? ");
                } else {
                    sb.append("x.").append(field).append(" = ? AND ");
                }
            }
            sb.append("ORDER BY x.").append(orderbyField).append(" DESC");
            Query query;
            query = em.createNativeQuery(sb.toString(), type);
            for (int i = 0; i < values.length; i++) {
                query.setParameter((i + 1), values[i]);
            }
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> List<T> findAll(Class<T> type) {
        if (!em.isOpen()) {
            return null;
        }
        Query query = null;
        try {
            if (type.isAssignableFrom(Category.class)) {
                query = em.createNativeQuery("SELECT distinct * FROM category", Category.class);
            } else if (type.isAssignableFrom(Produit.class)) {
                query = em.createNamedQuery("Produit.findAll");
            } else if (type.isAssignableFrom(Mesure.class)) {
                query = em.createNamedQuery("Mesure.findAll");
            } else if (type.isAssignableFrom(Fournisseur.class)) {
                query = em.createNamedQuery("Fournisseur.findAll");
            } else if (type.isAssignableFrom(Livraison.class)) {
                query = em.createNamedQuery("Livraison.findAll");
            } else if (type.isAssignableFrom(Stocker.class)) {
                query = em.createNamedQuery("Stocker.findAll");
            } else if (type.isAssignableFrom(Destocker.class)) {
                query = em.createNamedQuery("Destocker.findAll");
            } else if (type.isAssignableFrom(Recquisition.class)) {
                query = em.createNamedQuery("Recquisition.findAll");
            } else if (type.isAssignableFrom(PrixDeVente.class)) {
                query = em.createNamedQuery("PrixDeVente.findAll");
            } else if (type.isAssignableFrom(Client.class)) {
                query = em.createNamedQuery("Client.findAll");
            } else if (type.isAssignableFrom(Vente.class)) {
                query = em.createNamedQuery("Vente.findAll");
                query.setParameter("draft", "Drafted");
            } else if (type.isAssignableFrom(Taxe.class)) {
                query = em.createNamedQuery("Taxe.findAll");
            } else if (type.isAssignableFrom(Taxer.class)) {
                query = em.createNamedQuery("Taxer.findAll");
            } else if (type.isAssignableFrom(LigneVente.class)) {
                query = em.createNamedQuery("LigneVente.findAll");
            } else if (type.isAssignableFrom(CompteTresor.class)) {
                query = em.createNamedQuery("CompteTresor.findAll");
            } else if (type.isAssignableFrom(Traisorerie.class)) {
                query = em.createNamedQuery("Traisorerie.findAll");
            } else if (type.isAssignableFrom(Depense.class)) {
                query = em.createNamedQuery("Depense.findAll");
            } else if (type.isAssignableFrom(Traisorerie.class)) {
                query = em.createNamedQuery("Traisorerie.findAll");
            } else if (type.isAssignableFrom(Aretirer.class)) {
                query = em.createNamedQuery("Aretirer.findAll");
            } else if (type.isAssignableFrom(ClientAppartenir.class)) {
                query = em.createNamedQuery("ClientAppartenir.findAll");
            } else if (type.isAssignableFrom(ClientOrganisation.class)) {
                query = em.createNamedQuery("ClientOrganisation.findAll");
            } else if (type.isAssignableFrom(RetourDepot.class)) {
                query = em.createNamedQuery("RetourDepot.findAll");
            } else if (type.isAssignableFrom(RetourMagasin.class)) {
                query = em.createNamedQuery("RetourMagasin.findAll");
            } else if (type.isAssignableFrom(Facture.class)) {
                query = em.createNamedQuery("Facture.findAll");
            }
            return query == null ? null : query.getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public <T> List<T> findAll(Class<T> type, int start, int max) {
        if (!em.isOpen()) {
            return null;
        }
        Query query = null;
        try {
            if (type.isAssignableFrom(Category.class)) {
                query = em.createNativeQuery("SELECT distinct * FROM category", Category.class);
            } else if (type.isAssignableFrom(Produit.class)) {
                query = em.createNamedQuery("Produit.findAll");
            } else if (type.isAssignableFrom(Mesure.class)) {
                query = em.createNamedQuery("Mesure.findAll");
            } else if (type.isAssignableFrom(Fournisseur.class)) {
                query = em.createNamedQuery("Fournisseur.findAll");
            } else if (type.isAssignableFrom(Livraison.class)) {
                query = em.createNamedQuery("Livraison.findAll");
            } else if (type.isAssignableFrom(Stocker.class)) {
                query = em.createNamedQuery("Stocker.findAll");
            } else if (type.isAssignableFrom(Destocker.class)) {
                query = em.createNamedQuery("Destocker.findAll");
            } else if (type.isAssignableFrom(Recquisition.class)) {
                query = em.createNamedQuery("Recquisition.findAll");
            } else if (type.isAssignableFrom(PrixDeVente.class)) {
                query = em.createNamedQuery("PrixDeVente.findAll");
            } else if (type.isAssignableFrom(Client.class)) {
                query = em.createNamedQuery("Client.findAll");
            } else if (type.isAssignableFrom(Vente.class)) {
                query = em.createNamedQuery("Vente.findAll");
                query.setParameter("draft", "Drafted");
            } else if (type.isAssignableFrom(Taxe.class)) {
                query = em.createNamedQuery("Taxe.findAll");
            } else if (type.isAssignableFrom(Taxer.class)) {
                query = em.createNamedQuery("Taxer.findAll");
            } else if (type.isAssignableFrom(LigneVente.class)) {
                query = em.createNamedQuery("LigneVente.findAll");
            } else if (type.isAssignableFrom(CompteTresor.class)) {
                query = em.createNamedQuery("CompteTresor.findAll");
            } else if (type.isAssignableFrom(Traisorerie.class)) {
                query = em.createNamedQuery("Traisorerie.findAll");
            } else if (type.isAssignableFrom(Depense.class)) {
                query = em.createNamedQuery("Depense.findAll");
            } else if (type.isAssignableFrom(Traisorerie.class)) {
                query = em.createNamedQuery("Traisorerie.findAll");
            } else if (type.isAssignableFrom(Aretirer.class)) {
                query = em.createNamedQuery("Aretirer.findAll");
            } else if (type.isAssignableFrom(ClientAppartenir.class)) {
                query = em.createNamedQuery("ClientAppartenir.findAll");
            } else if (type.isAssignableFrom(ClientOrganisation.class)) {
                query = em.createNamedQuery("ClientOrganisation.findAll");
            } else if (type.isAssignableFrom(RetourDepot.class)) {
                query = em.createNamedQuery("RetourDepot.findAll");
            } else if (type.isAssignableFrom(RetourMagasin.class)) {
                query = em.createNamedQuery("RetourMagasin.findAll");
            } else if (type.isAssignableFrom(Facture.class)) {
                query = em.createNamedQuery("Facture.findAll");
            }
            return query == null ? null : query.setFirstResult(start).setMaxResults(max).getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static synchronized JpaStorage getInstance() {
        if (instance == null) {
            instance = new JpaStorage();
        }
        return instance;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<Vente> getDraftedCarts() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.observation = ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Vente getDraftedCart(int saved) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.uid = ? AND v.observation = ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, saved);
            query.setParameter(2, "Drafted");
            return (Vente) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
