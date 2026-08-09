package tools;

import com.endeleya.kazisafex.MainuiController;
import data.Module;
import data.core.KazisafeServiceFactory;
import data.network.Kazisafe;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import tools.MemoryGuard;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.Label;
import services.BackgroundSyncService;

/**
 *
 * @author eroot
 */
public class SyncEngine {

    /**
     * Préférence : timestamp (epoch millis) capturé à la fin de la dernière
     * synchronisation réussie. L'upsync ne renvoie que les enregistrements
     * d'outbox dont {@code createdAt} est strictement supérieur à ce
     * timestamp, afin de ne pas resynchroniser à chaque cycle la même
     * quantité de données. Valeur 0 (défaut) = synchronisation complète.
     */
    public static final String LAST_SYNC_TS_KEY = "lastSyncTimestamp";

    private Kazisafe kazisafe;
    ScheduledExecutorService ses;
    SubmissionPublisher<List> publisher = new SubmissionPublisher<>();
    private BackgroundSyncService backgroundSyncService;

    private static SyncEngine instance;

    /**
     * Timestamp (epoch millis) de fin de la dernière synchronisation réussie.
     * 0 = aucune synchronisation encore réalisée (tout synchroniser).
     */
    public static long getLastSyncTimestamp() {
        return Preferences.userNodeForPackage(SyncEngine.class)
                .getLong(LAST_SYNC_TS_KEY, 0L);
    }

    /**
     * Capture le timestamp de fin de synchronisation. La valeur est
     * strictement croissante : on ne revient jamais en arrière.
     */
    public static void setLastSyncTimestamp(long epochMillis) {
        if (epochMillis <= 0) {
            return;
        }
        Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
        long previous = pref.getLong(LAST_SYNC_TS_KEY, 0L);
        if (epochMillis > previous) {
            pref.putLong(LAST_SYNC_TS_KEY, epochMillis);
        }
    }

    public SyncEngine setup(String token) {
        this.kazisafe = KazisafeServiceFactory.createService(token);

        // Purge APPLIED outbox records older than the retention window (10 days)
        try {
            services.SyncOutboxService.purgeOldRecords(
                    services.SyncOutboxService.APPLIED_RETENTION_DAYS);
            System.out.println("SyncEngine: Old outbox records purged.");
        } catch (Exception e) {
            System.err.println(
                    "SyncEngine: Outbox purge failed: " + e.getMessage());
        }

        // Start backfilling background process
        try {
            services.SyncOutboxService.startBackfillingBackground();
        } catch (Exception e) {
            System.err.println("SyncEngine: Failed to start backfilling: " + e.getMessage());
        }

        startBackgroundSync();
        return this;
    }

    private final Flow.Subscriber<List> subs = new Flow.Subscriber<List>() {
        private Flow.Subscription sub;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.sub = subscription;
            this.sub.request(1);
        }

        @Override
        public void onNext(List item) {
            Util.syncList(item);
            this.sub.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Sync de list terminee avec succes");
        }
    };

    private SyncEngine() {
        publisher.subscribe(subs);
        ses = MemoryGuard.newSingleThreadScheduledExecutor("kazisafe-sync-engine");
        int cpus = Runtime.getRuntime().availableProcessors();
        System.out.println("vCPUS : " + cpus);
    }

    public static SyncEngine getInstance() {
        if (instance == null) {
            instance = new SyncEngine();
        }
        return instance;
    }

    public void startChecking() {
        ScheduledExecutorService ses = MemoryGuard.newSingleThreadScheduledExecutor("kazisafe-update-checker");
        ses.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        checkUpdate();
                    }
                },
                1,
                60,
                TimeUnit.SECONDS);
    }

    private void checkUpdate() {
        if (kazisafe == null)
            return;
        UpdateManager um = new UpdateManager(kazisafe);
        um.checkForUpdate(
                new UpdateManager.UpdateListener() {
                    @Override
                    public void onUpdateAvailable(Module module) {
                        System.out.println(
                                "Nouvelle version disponible: " + module.getVersion());
                    }

                    @Override
                    public void onUpToDate() {
                    }

                    @Override
                    public void onError(String message) {
                        System.err.println(
                                "Erreur de verification de mise a jour: " + message);
                    }
                });
    }

    /**
     * Starts the background outbox-driven upsync engine.
     * This continuously polls the sync_outbox table and pushes
     * pending mutations to the backend via Retrofit2.
     * Automatically pauses/resumes based on network connectivity.
     */
    private void startBackgroundSync() {
        if (kazisafe == null) {
            System.err.println(
                    "BackgroundSyncService: Cannot start — no Kazisafe service available.");
            return;
        }

        if (backgroundSyncService != null) {
            // Already running or previously started; cancel and restart
            backgroundSyncService.cancel();
        }

        backgroundSyncService = new BackgroundSyncService(
                kazisafe,
                30,
                message -> {
                    MainuiController controller = MainuiController.peekInstance();
                    if (controller != null) {
                        controller.setBackgroundSyncStatus(message);
                    }
                });
        backgroundSyncService.setOnFailed(event -> {
            Throwable ex = backgroundSyncService.getException();
            System.err.println(
                    "BackgroundSyncService failed: " +
                            (ex != null ? ex.getMessage() : "unknown"));
            if (ex != null)
                ex.printStackTrace();
        });
        backgroundSyncService.setOnSucceeded(event -> {
            System.out.println("BackgroundSyncService completed a cycle.");
        });

        backgroundSyncService.start();
        System.out.println(
                "BackgroundSyncService: Started with 30s poll interval.");
    }

    /**
     * Called by NetLoockup's network state change listener to
     * pause/resume the background sync engine based on connectivity.
     */
    public void onNetworkStateChanged(boolean isOnline) {
        if (backgroundSyncService == null)
            return;
        if (isOnline) {
            backgroundSyncService.resumeSync();
        } else {
            backgroundSyncService.pauseSync();
        }
    }

    public Kazisafe getKazisafe() {
        return kazisafe;
    }

    public void shutdown() {
        ses.shutdown();
        if (backgroundSyncService != null) {
            backgroundSyncService.cancel();
            System.out.println("BackgroundSyncService: Shutdown.");
        }
    }

    public void syncWithHttpProtocol(Label label, Kazisafe kazisafe) {
        try {
            label.setVisible(true);
            // Use BackgroundSyncService instead of HttpSyncHandler
            if (backgroundSyncService == null) {
                backgroundSyncService = new BackgroundSyncService(
                        kazisafe,
                        30,
                        message -> {
                            Platform.runLater(() -> label.setText(message));
                        });
            }
            backgroundSyncService.setOnFailed(event -> {
                Throwable ex = backgroundSyncService.getException();
                System.err.println(
                        "BackgroundSyncService failed: " +
                                (ex != null ? ex.getMessage() : "unknown"));
                if (ex != null)
                    ex.printStackTrace();
                Platform.runLater(() -> {
                    label.setVisible(false);
                    label.setText("Échec de la synchronisation");
                });
            });
            backgroundSyncService.setOnSucceeded(event -> {
                System.out.println("BackgroundSyncService completed a cycle.");
                Platform.runLater(() -> {
                    label.setVisible(false);
                    label.setText("Synchronisation terminée");
                });
            });
            ensureSyncServiceStarted();
            // Réveille la boucle : le cycle en cours (s'il y en a un) se
            // termine complètement avant qu'un nouveau cycle démarre.
            backgroundSyncService.requestCycle();
            System.out.println("BackgroundSyncService: Started manually.");
        } catch (Exception ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(
                    Level.SEVERE,
                    null,
                    ex);
        }
    }

    public String syncInBackground() {
        try {
            if (kazisafe == null)
                return "error : no kazisafe";
            // Use BackgroundSyncService instead of HttpSyncHandler
            if (backgroundSyncService == null) {
                backgroundSyncService = new BackgroundSyncService(
                        kazisafe,
                        30,
                        message -> System.out.println(message));
            }
            ensureSyncServiceStarted();
            // Réveille la boucle : le cycle en cours se termine avant le suivant.
            backgroundSyncService.requestCycle();
            return "finish";
        } catch (Exception e) {
            return "error : " + e.getMessage();
        }
    }

    /**
     * Démarre le service s'il n'est pas encore actif. Ne cancelle jamais un
     * cycle en cours (un nouveau cycle attend la fin du précédent).
     */
    private void ensureSyncServiceStarted() {
        if (backgroundSyncService == null) {
            return;
        }
        Worker.State state = backgroundSyncService.getState();
        if (state == Worker.State.READY) {
            backgroundSyncService.start();
        } else if (state == Worker.State.SUCCEEDED
                || state == Worker.State.CANCELLED
                || state == Worker.State.FAILED) {
            backgroundSyncService.reset();
            backgroundSyncService.start();
        }
    }
}
