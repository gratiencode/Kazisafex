package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;

public class SyncLogger {

    private static final String LOG_DIR = MainUI.cPath("/Media/proc/logs");
    private static final String LOG_FILE = LOG_DIR + "/client.log";
    private static final String LEGACY_LOG_FILE = LOG_DIR + "/client.logs";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS"
    );
    private static SyncLogger instance;
    private final String appVersion;
    private final String userId;
    private final String osName;
    private final String osArch;
    private final int cpuCores;

    private final ExecutorService logExecutor =
        MemoryGuard.newSingleThreadExecutor("SyncLogger-Thread");

    private SyncLogger() {
        this.appVersion = Constants.APP_VERSION;
        this.userId = Constants.getStringPref("eUid", "unknown");
        this.osName = System.getProperty("os.name", "unknown");
        this.osArch = System.getProperty("os.arch", "unknown");
        this.cpuCores = Runtime.getRuntime().availableProcessors();
        ensureLogFileExists();
    }

    public static synchronized SyncLogger getInstance() {
        if (instance == null) {
            instance = new SyncLogger();
        }
        return instance;
    }

    private void ensureLogFileExists() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            File legacy = new File(LEGACY_LOG_FILE);
            if (legacy.exists()) {
                boolean renamed = legacy.renameTo(file);
                if (!renamed) {
                    try {
                        java.nio.file.Files.copy(
                            legacy.toPath(),
                            file.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                    } catch (IOException e) {
                        System.err.println(
                            "SyncLogger: impossible de migrer l'ancien fichier de log: " +
                                e.getMessage()
                        );
                    }
                }
            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println(
                    "SyncLogger: impossible de créer le fichier de log: " +
                        e.getMessage()
                );
            }
        }
    }

    private String connectionStatus() {
        try {
            return Util.isInternetAndBaseApiReachable() ? "Online" : "Offline";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void writeHeader(
        PrintWriter pw,
        String context,
        String tableName,
        String entityId,
        LocalDateTime timestamp
    ) {
        pw.println("=== " + timestamp.format(DT_FMT) + " ===");
        pw.println("AppVersion: " + appVersion);
        pw.println("UserId: " + userId);
        pw.println("OS: " + osName + " (" + osArch + ")");
        pw.println("CPU Cores: " + cpuCores);
        pw.println("Status: " + connectionStatus());
        pw.println("Context: " + (context == null ? "N/A" : context));
        if (tableName != null) {
            pw.println("Table: " + tableName);
        }
        if (entityId != null) {
            pw.println("EntityId: " + entityId);
        }
    }

    public void log(Throwable throwable, String context) {
        log(throwable, context, null, null);
    }

    public void log(
        Throwable throwable,
        String context,
        String tableName,
        String entityId
    ) {
        final LocalDateTime timestamp = LocalDateTime.now();

        final String stacktrace;
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            stacktrace = sw.toString();
        } else {
            stacktrace = "";
        }

        logExecutor.submit(() -> {
            synchronized (SyncLogger.class) {
                ensureLogFileExists();
                try (
                    FileWriter fw = new FileWriter(LOG_FILE, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter pw = new PrintWriter(bw)
                ) {
                    writeHeader(pw, context, tableName, entityId, timestamp);

                    pw.println("Stacktrace:");
                    pw.println(stacktrace);
                    pw.println("=== END ===");
                    pw.println();
                } catch (IOException e) {
                    System.err.println(
                        "SyncLogger: erreur d'écriture dans le fichier de log: " +
                            e.getMessage()
                    );
                }
            }
        });
    }

    public static void init() {
        getInstance();
        System.out.println("SyncLogger initialisé: " + LOG_FILE);
    }
}
