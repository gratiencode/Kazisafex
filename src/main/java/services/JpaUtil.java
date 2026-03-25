/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.persistence.config.EntityManagerProperties;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class JpaUtil {

//    private static final Preferences pref;
//    private static final EntityManagerFactory emf;
    private static final Logger LOGGER = Logger.getLogger(JpaUtil.class.getName());
    private static final Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    private static EntityManagerFactory emf;
//
//    static {
//        pref = Preferences.userNodeForPackage(SyncEngine.class);
//        String databaseName = pref.get("eUid", null);
//        Map<String, String> properties = new HashMap<>();

    ////        boolean isEmbedded = pref.getBoolean("embedded_db", false);
////        if (!isEmbedded) {
//            String dbUrl = "jdbc:mysql://localhost:" + pref.getInt("default_mysql_port", 3306) + "/ksf_" + 
//                    databaseName + "?createDatabaseIfNotExist=true&useSsl=false&zeroDateTimeBehavior=convertToNull&"
//                    + "sessionVariables=sql_mode=''";
//            properties.put(EntityManagerProperties.JDBC_USER, "root");
//            properties.put(EntityManagerProperties.JDBC_URL, dbUrl);
//            properties.put(EntityManagerProperties.JDBC_PASSWORD, "Admin*21");
//            emf = Persistence.createEntityManagerFactory("kazisafe-jmx", properties);
////        } else {
////            String dbUrl = "jdbc:derby:" + dbPath("ksf_" + databaseName) + ";create=true";
//////            properties.put(EntityManagerProperties.JDBC_URL, dbUrl);
////            properties.put("jakarta.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
////            properties.put("jakarta.persistence.jdbc.url", dbUrl);
////            properties.put("jakarta.persistence.jdbc.user", "APP");
////            properties.put("jakarta.persistence.jdbc.password", "APP");
////            properties.put("hibernate.hbm2ddl.auto", "update");
////            properties.put("hibernate.show_sql", "true");
////            properties.put("hibernate.format_sql", "true");
////            emf = Persistence.createEntityManagerFactory("DerbyPU", properties);
////        }
//        //<property name="jakarta.persistence.jdbc.url" value="jdbc:derby:/data/myDB;create=true"/>
//
//    }
//    
//    
    public static void init() {
        try {
            String databaseName = pref.get("eUid", null);

            if (databaseName == null || databaseName.isBlank()) {
                throw new IllegalStateException("eUid introuvable dans les préférences.");
            }

            boolean isEmbedded = pref.getBoolean("embedded_db", false);
            Map<String, String> properties = new HashMap<>();

            if (!isEmbedded) {
                // --- MySQL ---
                String dbPort = String.valueOf(pref.getInt("default_mysql_port", 3306));
                String dbHost = pref.get("default_mysql_host", "localhost");
                String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/ksf_" + databaseName
                        + "?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&"
                        + "zeroDateTimeBehavior=convertToNull&sessionVariables=sql_mode=''";

                properties.put(EntityManagerProperties.JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
                properties.put(EntityManagerProperties.JDBC_URL, dbUrl);
                properties.put(EntityManagerProperties.JDBC_USER, resolveDbUser());
                properties.put(EntityManagerProperties.JDBC_PASSWORD, resolveDbPassword());
                emf = Persistence.createEntityManagerFactory("kazisafe-jmx", properties);
            } else {
                String dbPath = ManagedSessionFactory.dbPath("kazi_" + databaseName);
                String dbUrl = "jdbc:sqlite:" + dbPath + ".db";
                properties.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
                properties.put("hibernate.connection.url", dbUrl);
                properties.put("hibernate.dialect", "services.dialect.KSQLiteDialect");
                emf = Persistence.createEntityManagerFactory("SQlitePU", properties);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de l'EntityManagerFactory", ex);
            throw new RuntimeException("Échec d'initialisation de JPA : " + ex.getMessage(), ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        init();
        return emf;
    }

    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private static String resolveDbUser() {
        String fromEnv = System.getenv("KAZISAFE_DB_USER");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return pref.get("default_mysql_user", "root");
    }

    private static String resolveDbPassword() {
        String fromEnv = System.getenv("KAZISAFE_DB_PASSWORD");
        if (fromEnv != null) {
            return fromEnv;
        }
        return pref.get("default_mysql_password", "Admin*21");
    }

}
