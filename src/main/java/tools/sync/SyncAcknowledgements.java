package tools.sync;

import data.SyncOutbox;
import data.network.dto.BatchResultDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Appariement des acquittements serveur avec les mutations de l'outbox.
 *
 * <p>Le client envoie desormais un {@code mutationId} (uid de la ligne
 * outbox). Si le serveur l'echo, l'appariement est exact 1:1, meme lorsque
 * plusieurs mutations successives d'une meme entite sont dans le lot.</p>
 *
 * <p>Pour les serveurs qui ne renvoient que {@code entityId} +
 * {@code entityType}, on retombe sur un acquittement conservateur : au plus
 * une mutation par resultat. Les autres restent en file et seront rejouees
 * (UPSERT cote serveur) au lieu de perdre une mutation non appliquee.</p>
 */
public final class SyncAcknowledgements {

    private SyncAcknowledgements() {
    }

    /**
     * @return les uids d'outbox acquittes, au plus un par resultat serveur
     */
    public static List<String> matchOnePerResult(
            List<SyncOutbox> chunk,
            List<BatchResultDto.EntryResult> results) {
        List<String> matched = new ArrayList<>();
        if (chunk == null || chunk.isEmpty() || results == null || results.isEmpty()) {
            return matched;
        }
        Set<String> consumed = new HashSet<>();
        for (BatchResultDto.EntryResult result : results) {
            if (result == null) {
                continue;
            }
            // 1:1 exact lorsque le serveur echo le mutationId envoye.
            if (result.mutationId != null && !result.mutationId.isBlank()) {
                for (SyncOutbox record : chunk) {
                    if (record == null || record.getUid() == null
                            || consumed.contains(record.getUid())) {
                        continue;
                    }
                    if (result.mutationId.equals(record.getUid())) {
                        consumed.add(record.getUid());
                        matched.add(record.getUid());
                        break;
                    }
                }
                // Un mutationId inconnu ne doit acquitter aucune autre ligne.
                continue;
            }
            // Repli conservateur pour les serveurs sans mutationId.
            if (result.entityId == null || result.entityType == null) {
                continue;
            }
            for (SyncOutbox record : chunk) {
                if (record == null || record.getUid() == null || consumed.contains(record.getUid())) {
                    continue;
                }
                if (result.entityId.equals(record.getEntityId())
                        && result.entityType.equalsIgnoreCase(record.getTableName())) {
                    consumed.add(record.getUid());
                    matched.add(record.getUid());
                    break;
                }
            }
        }
        return matched;
    }
}
