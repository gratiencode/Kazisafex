package services.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.network.Kazisafe;
import data.network.dto.SyncOutboxDto;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import retrofit2.Response;
import services.SyncOutboxService;
import tools.SyncLogger;

public class DownsyncCatchupService {

    private static final String LAST_MUTATION_TS_KEY = "lastAppliedMutationTs";

    /**
     * Taille de page par défaut : le serveur la borne dynamiquement selon la
     * RAM JVM disponible (header {@code Sync-Max-Batch-Size}). Le client
     * boucle jusqu'à recevoir une page plus petite que la limite appliquée.
     */
    private static final int DEFAULT_PAGE_SIZE = 2000;
    private static final int MAX_PAGE_SIZE = 5000;

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
     * Remet le curseur de rattrapage (« Page since=… ») à zéro : le prochain
     * cycle de synchronisation re-téléchargera toutes les mutations depuis le
     * début, au lieu de reprendre au dernier timestamp appliqué.
     */
    public static void resetCursor() {
        Preferences pref = Preferences.userNodeForPackage(
            DownsyncCatchupService.class
        );
        pref.put(LAST_MUTATION_TS_KEY, "0");
        System.out.println(
            "[SYNC-CATCHUP] Curseur de rattrapage reinitialise a 0 (re-download complet au prochain cycle)."
        );
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

        String originalSince = since;
        System.out.println("[SYNC-CATCHUP] Starting catch-up since: " + since + ", originalSince: " + originalSince);
        if (statusUpdater != null) {
            statusUpdater.accept(
                "Téléchargement des mutations depuis le serveur..."
            );
        }

        int pageSize = DEFAULT_PAGE_SIZE;
        int totalCreated = 0;
        long maxMutationTs = -1L;
        int lastPriority = -1;

        try {
            // Pagination adaptative : on télécharge page après page, en repartant
            // du timestamp ET de la priorité de la dernière mutation reçue
            // (curseur composé), jusqu'à ce que le serveur renvoie moins de
            // lignes que la limite qu'il applique.
            while (!Thread.currentThread().isInterrupted()) {
                Response<List<SyncOutboxDto>> response = executeWithRetry(api, since, originalSince, lastPriority, pageSize);

                if (!response.isSuccessful() || response.body() == null) {
                    if (response.code() == 429) {
                        // Backpressure serveur (RAM faible) : on réduit la page
                        // selon la taille conseillée et on retente.
                        int retrySize = parseHeaderInt(
                            response.headers().get("Retry-With-Batch-Size"),
                            pageSize
                        );
                        pageSize = Math.max(1, Math.min(retrySize, MAX_PAGE_SIZE));
                        System.out.println(
                            "[SYNC-CATCHUP] Backpressure serveur (429), page réduite à " +
                                pageSize
                        );
                        continue;
                    }
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
                    break;
                }

                List<SyncOutboxDto> missed = response.body();

                // Taille de page réellement appliquée par le serveur
                // (adaptative à la RAM) : elle sert de seuil de fin de pagination.
                int appliedPageSize = parseHeaderInt(
                    response.headers().get("Sync-Max-Batch-Size"),
                    pageSize
                );
                pageSize = Math.max(1, Math.min(appliedPageSize, MAX_PAGE_SIZE));

                System.out.println(
                    "[SYNC-CATCHUP] Page since=" + since +
                        " : " + missed.size() + " mutations.");

                int created = 0;
                long pageMaxTs = -1L;
                for (SyncOutboxDto mutation : missed) {
                    try {
                        System.out.println("[SYNC-CATCHUP] La mutation recu = " + new ObjectMapper().writeValueAsString(mutation));
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
                    // Ancienneté : le prochain pull reprendra depuis le
                    // timestamp de mutation le plus récent reçu (jamais
                    // « maintenant », pour ne rien manquer côté serveur).
                    if (mutation.mutationTs > pageMaxTs) {
                        pageMaxTs = mutation.mutationTs;
                    }
                }
                totalCreated += created;
                if (pageMaxTs > maxMutationTs) {
                    maxMutationTs = pageMaxTs;
                }

                // Curseur composé : la priorité de la dernière ligne reçue (le
                // serveur trie par niveau de dépendance puis par timestamp).
                int prevPriority = lastPriority;
                if (!missed.isEmpty()) {
                    Integer p = missed.get(missed.size() - 1).priority;
                    if (p != null) {
                        lastPriority = p;
                    }
                }

                // Dernière page : le serveur a tout renvoyé.
                if (missed.isEmpty() || missed.size() < pageSize) {
                    break;
                }
                // Aucun progrès (toutes les lignes restantes partagent le même
                // timestamp ET la même priorité) : protection contre une boucle
                // infinie. Le cycle de synchronisation suivant reprendra au même
                // curseur, et le serveur renverra ces lignes en doublon (UPSERT).
                if (pageMaxTs <= 0
                    || (String.valueOf(pageMaxTs).equals(since)
                        && lastPriority == prevPriority)) {
                    System.out.println(
                        "[SYNC-CATCHUP] Aucun progrès à since=" + since +
                            " (page pleine, timestamps et priorité identiques) : arrêt de la pagination."
                    );
                    break;
                }
                since = String.valueOf(pageMaxTs);
            }

            if (maxMutationTs > 0) {
                pref.put(
                    LAST_MUTATION_TS_KEY,
                    String.valueOf(maxMutationTs)
                );
            }
            System.out.println("[SYNC-CATCHUP] Catch-up completed. " + totalCreated + " downsync records created.");
            if (statusUpdater != null) {
                statusUpdater.accept(totalCreated + " mutations récupérées et enregistrées.");
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
            // Nettoyage: aucun enregistrement d'outbox avec payload null ne doit subsister.
            SyncOutboxService.deleteNullPayloadRecords();
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

    private static int parseHeaderInt(String value, int fallback) {        if (value != null) {
            try {
                int parsed = Integer.parseInt(value.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // valeur illisible : on garde le fallback
            }
        }
        return fallback;
    }

    /**
     * Exécute le téléchargement d'une page avec repli sur les erreurs réseau
     * transitoires (réinitialisation HTTP/2 « stream was reset: CANCEL »,
     * timeouts, connexions interrompues) : jusqu'à 3 tentatives avec backoff,
     * en réduisant la taille de page à chaque échec. La backpressure serveur
     * (HTTP 429) est traitée de la même façon qu'avant (réduction de page),
     * mais avec un retry immédiat avant de rendre la main.
     */
    private static Response<List<SyncOutboxDto>> executeWithRetry(
        Kazisafe api, String since, String originalSince, int lastPriority, int pageSize
    ) throws java.io.IOException {
        int size = pageSize;
        for (int attempt = 1; ; attempt++) {
            try {
                Response<List<SyncOutboxDto>> resp =
                    api.getMissedMutations(since, originalSince, lastPriority, size).execute();
                if (resp.isSuccessful() || resp.code() != 429 || attempt >= 3) {
                    return resp;
                }
                int retrySize = parseHeaderInt(
                    resp.headers().get("Retry-With-Batch-Size"), size
                );
                size = Math.max(1, Math.min(retrySize, MAX_PAGE_SIZE));
                System.out.println(
                    "[SYNC-CATCHUP] Backpressure serveur (429), page réduite à " + size);
                sleepQuietly(1000L * attempt);
            } catch (java.io.InterruptedIOException e) {
                // Laisse le contrôle au cycle de synchronisation (arrêt propre).
                throw e;
            } catch (java.io.IOException e) {
                if (attempt >= 3) {
                    throw e;
                }
                size = Math.max(1, size / 2);
                System.out.println(
                    "[SYNC-CATCHUP] Erreur réseau (tentative " + attempt +
                        "/3), page réduite à " + size + " : " + e.getMessage());
                SyncLogger.getInstance().log(e,
                    "DownsyncCatchupService: réseau instable pendant le catch-up",
                    null, null);
                sleepQuietly(1500L * attempt);
            }
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
