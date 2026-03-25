package tools;

import data.core.KazisafeServiceFactory;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.network.Kazisafe;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.Label;

/**
 *
 * @author eroot
 */
public class SyncEngine {

    private Kazisafe kazisafe;
    ScheduledExecutorService ses;
    SubmissionPublisher<List> publisher = new SubmissionPublisher<>();

    private static SyncEngine instance;

    public SyncEngine setup(String token) {
        this.kazisafe = KazisafeServiceFactory.createService(token);
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
        ses = Executors.newSingleThreadScheduledExecutor();
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
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    private void checkUpdate() {
        Executors.newSingleThreadExecutor()
                .execute(() -> {
//                    try {
//                        String version = pref.get("kazisafe_version", null);
//                        if (version == null) {
//                            pref.put("kazisafe_version", Constants.APP_VERSION);
//                            version = Constants.APP_VERSION;
//                        }
//                        Response<Module> update = kazisafe.checkUpdates().execute();
//                        if (update.isSuccessful()) {
//                            Module mod = update.body();
//                            String modver = mod.getVersion();
//                            if (!modver.equalsIgnoreCase(version)) {
//                                notifyNewUpate(mod);
//                            } else {
//                                notifySameUpate(mod);
//                            }
//                        }
//                    } catch (IOException ex) {
//                        Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                });
    }

    public void shutdown() {
        ses.shutdown();
    }

    public void syncWithHttpProtocol(Label label,Kazisafe kazisafe) {
        try {
            label.setVisible(true);
            HttpSyncHandler task = new HttpSyncHandler(kazisafe);
            task.stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) -> {
                if (t1 == Worker.State.SUCCEEDED || t1 == Worker.State.FAILED) {
                    label.setVisible(false);
                }
            });
            label.textProperty().bind(task.messageProperty());
            Executors.newSingleThreadExecutor().submit(task);
        } catch (Exception ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String syncInBackground() {
        try {
            if (kazisafe == null) return "error : no kazisafe";
            HttpSyncHandler task = new HttpSyncHandler(kazisafe);
            task.call();
            return "finish";
        } catch (Exception e) {
            return "error : " + e.getMessage();
        }
    }

}
