package services.sync;

import data.network.Kazisafe;
import data.network.dto.SyncOutboxDto;
import java.util.List;
import java.util.prefs.Preferences;
import services.SyncOutboxService;
import tools.SyncLogger;

public class DownsyncCatchupService {

    private static final String LAST_MUTATION_TS_KEY = "lastAppliedMutationTs";

    public static void catchUp(Kazisafe api, String entrepriseId) {
        Preferences pref = Preferences.userNodeForPackage(
            DownsyncCatchupService.class
        );
        String since = pref.get(LAST_MUTATION_TS_KEY, "0");

        System.out.println("[SYNC-CATCHUP] Starting catch-up since: " + since);

        try {
            var response = api
                .getMissedMutations(entrepriseId, since)
                .execute();
            if (response.isSuccessful() && response.body() != null) {
                List<SyncOutboxDto> missed = response.body();
                System.out.println(
                    "[SYNC-CATCHUP] Found " +
                        missed.size() +
                        " missed mutations."
                );

                for (SyncOutboxDto mutation : missed) {
                    try {
                        SyncOutboxService.createDownsyncRecord(mutation);
                    } catch (Exception e) {
                        SyncLogger.getInstance().log(
                            e,
                            "Catch-up failed to create downsync record for " + mutation.entityType,
                            mutation.entityId,
                            null
                        );
                    }
                    pref.put(
                        LAST_MUTATION_TS_KEY,
                        String.valueOf(mutation.mutationTs)
                    );
                }
                System.out.println("[SYNC-CATCHUP] Catch-up completed. " + missed.size() + " downsync records created.");
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(
                e,
                "DownsyncCatchupService: error during catch-up",
                entrepriseId,
                null
            );
        }
    }
}
