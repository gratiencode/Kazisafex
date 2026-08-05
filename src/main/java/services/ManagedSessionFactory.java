/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import services.utils.SecurePreferences;
import tools.MemoryGuard;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class ManagedSessionFactory {

    private static Preferences pref;
    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;
    private static final Map<Thread, EntityManager> trackedEMs = new ConcurrentHashMap<>();
    private static final boolean embedded;
    private static WriteQueueManager writeQueue;

    static {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        String databaseName = pref.get("eUid", null);
        boolean testMode = Boolean.getBoolean("kazisafe.test.mode");
        embedded = pref.getBoolean("embedded_db", true);
        if (databaseName == null || databaseName.isBlank()) {
            if (testMode) {
                databaseName = "kazisafe-test";
            } else {
                throw new IllegalStateException(
                    "eUid introuvable dans les préférences."
                );
            }
        }
        Map<String, String> properties = new HashMap<>();
        if (!embedded) {
            String dbPort = String.valueOf(
                pref.getInt("default_mysql_port", 3306)
            );
            String dbHost = pref.get("default_mysql_host", "localhost");
            boolean useSsl = pref.getBoolean("default_mysql_ssl", false);
            String dbUrl =
                "jdbc:mysql://" +
                dbHost +
                ":" +
                dbPort +
                "/ksf_" +
                databaseName +
                "?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=" +
                useSsl +
                "&" +
                "zeroDateTimeBehavior=convertToNull&sessionVariables=sql_mode=''" +
                "&tcpKeepAlive=true&autoReconnect=true&connectTimeout=10000";
            String dbUser = resolveDbUser();
            String dbPassword = resolveDbPassword();
            properties.put(
                EntityManagerProperties.JDBC_DRIVER,
                "com.mysql.cj.jdbc.Driver"
            );
            properties.put(EntityManagerProperties.JDBC_URL, dbUrl);
            properties.put(EntityManagerProperties.JDBC_USER, dbUser);
            properties.put(EntityManagerProperties.JDBC_PASSWORD, dbPassword);

            // Prefer HikariCP when available; otherwise fallback safely to Hibernate internal pool
            if (
                isClassAvailable(
                    "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
                )
            ) {
                properties.put(
                    "hibernate.connection.provider_class",
                    "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
                );
                properties.put(
                    "hibernate.hikari.poolName",
                    "KazisafeMySqlPool"
                );
                properties.put(
                    "hibernate.hikari.maximumPoolSize",
                    String.valueOf(resolveMySqlPoolMaxSize())
                );
                properties.put(
                    "hibernate.hikari.minimumIdle",
                    String.valueOf(resolveMySqlPoolMinIdle())
                );
                properties.put(
                    "hibernate.hikari.connectionTimeout",
                    String.valueOf(resolveMySqlConnectionTimeoutMs())
                );
                properties.put(
                    "hibernate.hikari.keepaliveTime",
                    String.valueOf(resolveMySqlKeepaliveTimeMs())
                );
                properties.put(
                    "hibernate.hikari.idleTimeout",
                    String.valueOf(resolveMySqlIdleTimeoutMs())
                );
                properties.put(
                    "hibernate.hikari.maxLifetime",
                    String.valueOf(resolveMySqlMaxLifetimeMs())
                );
                properties.put(
                    "hibernate.hikari.leakDetectionThreshold",
                    String.valueOf(resolveMySqlLeakDetectionThresholdMs())
                );
            } else {
                Logger.getLogger(ManagedSessionFactory.class.getName()).log(
                    Level.WARNING,
                    "HikariCP provider not found, fallback to Hibernate internal pool."
                );
                properties.put(
                    "hibernate.connection.pool_size",
                    String.valueOf(resolveMySqlPoolMaxSize())
                );
            }

            if (testMode) {
                emf = null;
                threadLocal = new ThreadLocal<>();
            } else {
                emf = Persistence.createEntityManagerFactory(
                    "kazisafe-jmx",
                    properties
                );
                SchemaAutoUpdater.ensureCoreSchema(
                    false,
                    dbUrl,
                    dbUser,
                    dbPassword
                );
                AggregateTriggerService.getInstance().rebuildAtStartup();
                threadLocal = new ThreadLocal<>();
            }
        } else {
            // ---SQLite---
            String dbPath = dbPath("kazi_" + databaseName);
            String dbUrl =
                "jdbc:sqlite:" +
                dbPath +
                ".db?journal_mode=WAL&busy_timeout=120000&synchronous=NORMAL&limit_compound_select=0";
            properties.put(
                "hibernate.connection.driver_class",
                "org.sqlite.JDBC"
            );
            properties.put("hibernate.connection.url", dbUrl);
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put(
                "hibernate.session_factory.statement_inspector",
                "services.dialect.SqliteStatementInspector"
            );
            if (!SecurePreferences.hasStoredValue()) {
                try {
                    String localDbSecret = resolveLocalDbSecret(databaseName);
                    SecurePreferences.storeEncryptedValue(
                        databaseName,
                        localDbSecret
                    );
                } catch (Exception ex) {
                    Logger.getLogger(ManagedSessionFactory.class.getName()).log(
                        Level.SEVERE,
                        null,
                        ex
                    );
                }
            }
            properties.put(
                "eclipselink.session.customizer",
                "services.utils.SQLiteSessionCustomizer"
            );
            emf = Persistence.createEntityManagerFactory(
                "SQlitePU",
                properties
            );
            SchemaAutoUpdater.ensureCoreSchema(true, dbUrl, null, null);
            writeQueue = new WriteQueueManager(emf);
            AggregateTriggerService.getInstance().rebuildAtStartup();
            threadLocal = null; // inutile pour sqlite
            System.out.println("-SQLite-");
        }
        if (!embedded && !testMode) {
            startLeakCleaner();
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
                "getEntityManager() réservé à MySQL, utilisez submitWrite/executeRead pour SQLite"
            );
        }
        EntityManager em = threadLocal.get();
        if (em == null) {
            em = emf.createEntityManager();
            threadLocal.set(em);
            trackedEMs.put(Thread.currentThread(), em);
        }
        return em;
    }

    public static void closeEntityManager() {
        if (!embedded) {
            EntityManager em = threadLocal.get();
            if (em != null) {
                trackedEMs.remove(Thread.currentThread());
                em.close();
                threadLocal.remove(); // .set(null);
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

    public static <T> CompletableFuture<T> submitWrite(
        Function<EntityManager, T> action
    ) {
        if (!embedded) {
            throw new IllegalStateException(
                "submitWrite() réservé à SQLite, utilisez getEntityManager() pour MySQL"
            );
        }
        return writeQueue.submit(action);
    }

    public static <T> T executeRead(Function<EntityManager, T> action) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }

    public static <T> T executeWrite(Function<EntityManager, T> action) {
        if (embedded) {
            try {
                return submitWrite(action).get();
            } catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new RuntimeException(cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Écriture SQLite interrompue", e);
            }
        } else {
            EntityManager em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            boolean started = !tx.isActive();
            if (started) {
                tx.begin();
            }
            try {
                T result = action.apply(em);
                if (started && tx.isActive()) {
                    tx.commit();
                }
                return result;
            } catch (RuntimeException e) {
                if (started && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                if (started) {
                    closeEntityManager();
                }
            }
        }
    }

    public static String dbPath(String dbname) {
        String path,
            fpath = null;
        if (PlatformUtil.isWindows()) {
            path =
                System.getenv("ProgramData") +
                File.separator +
                "Kazisafe" +
                File.separator +
                "datastore";
            fpath = path + File.separator + dbname;
        } else if (PlatformUtil.isLinux()) {
            path =
                "/home/" +
                System.getProperty("user.name") +
                "/Kazisafe/datastore";
            fpath = path + File.separator + dbname;
        } else if (PlatformUtil.isMac()) {
            path =
                "/Users" +
                File.separator +
                System.getProperty("user.name") +
                File.separator +
                "Kazisafe" +
                File.separator +
                "datastore";
            fpath = path + File.separator + dbname;
        }
        return fpath;
    }

    public static boolean isEmbedded() {
        return embedded;
    }

    /**
     * Taille maximale du pool Hikari MySQL (résolue via env/préférences).
     */
    public static int getMySqlPoolMaxSize() {
        return resolveMySqlPoolMaxSize();
    }

    /**
     * Nombre maximal de threads autorisés pour les traitements DB parallèles.
     * Contrainte : ThreadPool Max <= Hikari Max - 2 pour laisser une marge
     * de connexions au reste de l'application.
     */
    public static int recommendedDbThreadPoolSize() {
        return Math.max(2, resolveMySqlPoolMaxSize() - 2);
    }

    /**
     * Exécute une tâche et ferme proprement l'EntityManager du thread courant
     * (MySQL) pour libérer la connexion vers le pool. Sans effet sur SQLite.
     */
    public static void runWithCleanup(Runnable action) {
        try {
            action.run();
        } finally {
            closeEntityManager();
        }
    }

    /**
     * Exécute un travail DB et ferme l'EntityManager à la fin (MySQL).
     * Équivalent de executeRead() pour SQLite, mais pour MySQL.
     * Utilisation : ManagedSessionFactory.doInSession(em -> { ... return result; });
     */
    public static <T> T doInSession(Function<EntityManager, T> work) {
        if (embedded) {
            return executeRead(work);
        }
        try {
            return work.apply(getEntityManager());
        } finally {
            closeEntityManager();
        }
    }

    /**
     * Version void de doInSession pour les Runnable.
     */
    public static void runInSession(Runnable work) {
        doInSession(em -> {
            work.run();
            return null;
        });
    }

    /**
     * Version avec EntityManager injecté, pour éviter les appels getEntityManager()
     * redondants et garantir une fermeture homogène via doInSession().
     */
    public static void runInSession(Consumer<EntityManager> work) {
        doInSession(em -> {
            work.accept(em);
            return null;
        });
    }

    /**
     * Exécute une action dans un thread dédié, avec fermeture automatique
     * de l'EntityManager après l'opération.
     */
    public static void runInBackground(Runnable action) {
        MemoryGuard.newSingleThreadExecutor("kazisafe-bg-session")
                .submit(() -> runWithCleanup(action));
    }

    /**
     * Démarre un nettoyeur périodique qui ferme les EntityManager orphelins
     * (threads morts n'ayant pas appelé closeEntityManager()).
     * Exécute toutes les 30s, latence max de nettoyage = 30s.
     */
    private static void startLeakCleaner() {
        ScheduledExecutorService cleaner = MemoryGuard.newSingleThreadScheduledExecutor("Kazisafe-EM-LeakCleaner");
        cleaner.scheduleAtFixedRate(() -> {
            try {
                Iterator<Map.Entry<Thread, EntityManager>> it = trackedEMs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Thread, EntityManager> entry = it.next();
                    Thread thread = entry.getKey();
                    EntityManager em = entry.getValue();
                    if (!thread.isAlive()) {
                        it.remove();
                        try {
                            if (em.isOpen()) {
                                em.close();
                                Logger.getLogger(ManagedSessionFactory.class.getName()).log(
                                    Level.WARNING,
                                    "Closed leaked EntityManager from dead thread: {0}",
                                    thread.getName()
                                );
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(ManagedSessionFactory.class.getName()).log(
                                Level.WARNING,
                                "Error closing leaked EM from dead thread: {0}",
                                ex.getMessage()
                            );
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(ManagedSessionFactory.class.getName()).log(
                    Level.WARNING,
                    "Leak cleaner error",
                    ex
                );
            }
        }, 30, 30, TimeUnit.SECONDS);
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
        return pref.get("default_mysql_password", "");
    }

    private static String resolveLocalDbSecret(String databaseName) {
        String fromEnv = System.getenv("KAZISAFE_LOCAL_DB_SECRET");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return pref.get("local_db_secret", databaseName);
    }

    private static int resolveMySqlPoolMaxSize() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_MAX");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(5, Integer.parseInt(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(5, pref.getInt("mysql_pool_max", 50));
    }

    private static int resolveMySqlPoolMinIdle() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_MIN_IDLE");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(1, Integer.parseInt(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(1, pref.getInt("mysql_pool_min_idle", 5));
    }

    private static long resolveMySqlConnectionTimeoutMs() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_CONN_TIMEOUT_MS");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(1000L, Long.parseLong(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(
            1000L,
            pref.getLong("mysql_pool_conn_timeout_ms", 30000L)
        );
    }

    private static long resolveMySqlKeepaliveTimeMs() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_KEEPALIVE_MS");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(10000L, Long.parseLong(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(
            10000L,
            pref.getLong("mysql_pool_keepalive_ms", 300000L)
        );
    }

    private static long resolveMySqlIdleTimeoutMs() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_IDLE_TIMEOUT_MS");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(10000L, Long.parseLong(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(
            10000L,
            pref.getLong("mysql_pool_idle_timeout_ms", 600000L)
        );
    }

    private static long resolveMySqlMaxLifetimeMs() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_MAX_LIFETIME_MS");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(30000L, Long.parseLong(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(
            30000L,
            pref.getLong("mysql_pool_max_lifetime_ms", 1800000L)
        );
    }

    private static long resolveMySqlLeakDetectionThresholdMs() {
        String fromEnv = System.getenv("KAZISAFE_DB_POOL_LEAK_DETECT_MS");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Math.max(0L, Long.parseLong(fromEnv.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(0L, pref.getLong("mysql_pool_leak_detect_ms", 60000L));
    }

    private static boolean isClassAvailable(String fqcn) {
        try {
            Class.forName(fqcn);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
