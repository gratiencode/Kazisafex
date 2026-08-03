package services.sync;

import data.network.Kazisafe;
import data.network.dto.SyncOutboxDto;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import services.SyncOutboxService;
import tools.SyncLogger;

public class DownsyncCatchupService {

    private static final String LAST_MUTATION_TS_KEY = "lastAppliedMutationTs";

    /**
     * Rattrapage incrémental : reprend depuis le dernier timestamp appliqué.
     * L'entreprise est résolue côté serveur à partir de l'utilisateur connecté
     * (endpoint sync/outbox/missed), elle n'est donc pas transmise.
     */
    public static void catchUp(Kazisafe api) {
        Preferences pref = Preferences.userNodeForPackage(
            DownsyncCatchupService.class
        );
        String since = pref.get(LAST_MUTATION_TS_KEY, "0");
        runCatchUp(api, since, null, false);
    }

    /**
     * Rattrapage complet : télécharge depuis le début de l'outbox serveur
     * (getMissedMutations depuis l'epoch), crée les enregistrements downsync
     * locaux puis matérialise en base locale (MySQL ou SQLite selon la base
     * active), en respectant l'ordre de dépendance entre les entités.
     * Utilisé par le full resync.
     */
    public static void catchUpFull(
        Kazisafe api,
        Consumer<String> statusUpdater
    ) {
        runCatchUp(api, "0", statusUpdater, true);
    }

    private static void runCatchUp(
        Kazisafe api,
        String since,
        Consumer<String> statusUpdater,
        boolean materialize
    ) {
        Preferences pref = Preferences.userNodeForPackage(
            DownsyncCatchupService.class
        );

        System.out.println("[SYNC-CATCHUP] Starting catch-up since: " + since);
        if (statusUpdater != null) {
            statusUpdater.accept(
                "Téléchargement des mutations depuis le serveur..."
            );
        }

        try {
            var response = api.getMissedMutations(since).execute();
            if (response.isSuccessful() && response.body() != null) {
                List<SyncOutboxDto> missed = response.body();
                System.out.println(
                    "[SYNC-CATCHUP] Found " +
                        missed.size() +
                        " missed mutations."
                );

                int created = 0;
                for (SyncOutboxDto mutation : missed) {
                    try {
                        SyncOutboxService.createDownsyncRecord(mutation);
                        created++;
                    } catch (Exception e) {
                        SyncLogger.getInstance().log(
                            e,
                            "Catch-up failed to create downsync record for " + mutation.entityType,
                            mutation.entityType,
                            mutation.entityId
                        );
                    }
                    pref.put(
                        LAST_MUTATION_TS_KEY,
                        String.valueOf(mutation.mutationTs)
                    );
                }
                System.out.println("[SYNC-CATCHUP] Catch-up completed. " + created + " downsync records created.");
                if (statusUpdater != null) {
                    statusUpdater.accept(created + " mutations récupérées et enregistrées.");
                }

                if (materialize) {
                    if (statusUpdater != null) {
                        statusUpdater.accept(
                            "Matérialisation des données en base locale..."
                        );
                    }
                    SyncOutboxService.materializeDownsyncRecords();
                    SyncOutboxService.cleanupAppliedRecords();
                    if (statusUpdater != null) {
                        statusUpdater.accept("Matérialisation terminée.");
                    }
                }
            } else {
                System.err.println(
                    "[SYNC-CATCHUP] Server error during catch-up: " + response.code()
                );
                SyncLogger.getInstance().log(
                    new RuntimeException("HTTP " + response.code()),
                    "DownsyncCatchupService: error during catch-up",
                    null,
                    null
                );
                if (statusUpdater != null) {
                    statusUpdater.accept(
                        "Impossible de récupérer les mutations (HTTP " + response.code() + ")."
                    );
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(
                e,
                "DownsyncCatchupService: error during catch-up",
                null,
                null
            );
            if (statusUpdater != null) {
                statusUpdater.accept(
                    "Erreur lors du rattrapage des mutations."
                );
            }
        }
    }
}
