/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.utils.SecurePreferences;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class ManagedSessionFactory {

    private static Preferences pref;
    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;
    private static final boolean embedded;
    private static WriteQueueManager writeQueue;

    static {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        String databaseName = pref.get("eUid", null);
        embedded = pref.getBoolean("embedded_db", true);
        if (databaseName == null || databaseName.isBlank()) {
            throw new IllegalStateException("eUid introuvable dans les préférences.");
        }
        Map<String, String> properties = new HashMap<>();
        if (!embedded) {
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
            threadLocal = new ThreadLocal<>();
        } else {

            // ---SQLite---
            String dbPath = dbPath("kazi_" + databaseName);
            String dbUrl = "jdbc:sqlite:" + dbPath + ".db?journal_mode=WAL&busy_timeout=120000&synchronous=normal";
            properties.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
            properties.put("hibernate.connection.url", dbUrl);
            if (!isFileExist(dbPath + ".db")) {
                properties.put("hibernate.hbm2ddl.auto", "update");
            }
            properties.put("hibernate.session_factory.statement_inspector",
                    "services.dialect.SqliteStatementInspector");
            if (!SecurePreferences.hasStoredValue()) {
                try {
                    String localDbSecret = resolveLocalDbSecret(databaseName);
                    SecurePreferences.storeEncryptedValue(databaseName, localDbSecret);
                } catch (Exception ex) {
                    Logger.getLogger(ManagedSessionFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            properties.put("eclipselink.session.customizer", "services.utils.SQLiteSessionCustomizer");
            emf = Persistence.createEntityManagerFactory("SQlitePU", properties);
            writeQueue = new WriteQueueManager(emf);
            threadLocal = null; // inutile pour sqlite
            System.out.println("-SQLite-");
        }

    }

    public static boolean isBdCreated() {
        String databaseName = pref.get("eUid", null);
        String dbPath = dbPath("kazi_" + databaseName);
        return isFileExist(dbPath + ".db");
    }

    public static EntityManager getEntityManager() {
        if (embedded) {
            throw new IllegalStateException(
                    "getEntityManager() réservé à MySQL, utilisez submitWrite/executeRead pour SQLite");
        }
        EntityManager em = threadLocal.get();
        if (em == null) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        if (!embedded) {
            EntityManager em = threadLocal.get();
            if (em != null) {
                em.close();
                threadLocal.remove();// .set(null);
            }
        }
    }

    public static boolean isFileExist(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        Path p = Path.of(path);
        return Files.exists(p) && Files.isRegularFile(p);
    }

    public static void closeEntityManagerFactory() {
        emf.close();
        if (writeQueue != null) {
            writeQueue.shutdown();
        }
    }

    public static <T> CompletableFuture<T> submitWrite(Function<EntityManager, T> action) {
        if (!embedded) {
            throw new IllegalStateException("submitWrite() réservé à SQLite, utilisez getEntityManager() pour MySQL");
        }
        return writeQueue.submit(action);
    }

    public static <T> T executeRead(Function<EntityManager, T> action) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }

    public static String dbPath(String dbname) {
        String path, fpath = null;
        if (PlatformUtil.isWindows()) {
            path = System.getenv("ProgramData") + File.separator + "Kazisafe" + File.separator + "datastore";
            fpath = path + File.separator + dbname;
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe/datastore";
            fpath = path + File.separator + dbname;
        } else if (PlatformUtil.isMac()) {
            path = "/Users" + File.separator + System.getProperty("user.name") + File.separator + "Kazisafe"
                    + File.separator + "datastore";
            fpath = path + File.separator + dbname;
        }
        return fpath;
    }

    public static boolean isEmbedded() {
        return embedded;
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

    private static String resolveLocalDbSecret(String databaseName) {
        String fromEnv = System.getenv("KAZISAFE_LOCAL_DB_SECRET");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return pref.get("local_db_secret", databaseName);
    }
}
