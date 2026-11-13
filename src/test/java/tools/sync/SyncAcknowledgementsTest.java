package tools.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data.SyncOutbox;
import data.network.dto.BatchResultDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SyncAcknowledgements — un acquittement serveur par mutation")
class SyncAcknowledgementsTest {

    private static SyncOutbox outbox(String uid, String entityId, String table) {
        SyncOutbox record = new SyncOutbox();
        record.setUid(uid);
        record.setEntityId(entityId);
        record.setTableName(table);
        record.setAction("UPDATE");
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private static BatchResultDto.EntryResult success(String entityId, String table) {
        return new BatchResultDto.EntryResult(entityId, table, null);
    }

    @Test
    @DisplayName("Deux mutations d'une même entité, un seul succès serveur : une seule acquittée")
    void matchesAtMostOneMutationPerResult() {
        SyncOutbox first = outbox("o1", "vente-1", "VENTE");
        SyncOutbox second = outbox("o2", "vente-1", "VENTE");

        List<String> matched = SyncAcknowledgements.matchOnePerResult(
                List.of(first, second), List.of(success("vente-1", "VENTE")));

        assertEquals(List.of("o1"), matched,
                "La seconde mutation doit rester en file pour un nouvel envoi");
    }

    @Test
    @DisplayName("Deux succès serveur acquittent deux mutations distinctes")
    void matchesTwoDistinctResults() {
        SyncOutbox first = outbox("o1", "vente-1", "VENTE");
        SyncOutbox second = outbox("o2", "vente-1", "VENTE");

        List<String> matched = SyncAcknowledgements.matchOnePerResult(
                List.of(first, second),
                List.of(success("vente-1", "VENTE"), success("vente-1", "VENTE")));

        assertEquals(List.of("o1", "o2"), matched);
    }

    @Test
    @DisplayName("Le type d'entité est comparé sans tenir compte de la casse")
    void matchesEntityTypeCaseInsensitively() {
        SyncOutbox first = outbox("o1", "prod-1", "PRODUIT");

        List<String> matched = SyncAcknowledgements.matchOnePerResult(
                List.of(first), List.of(success("prod-1", "produit")));

        assertEquals(List.of("o1"), matched);
    }

    @Test
    @DisplayName("Le mutationId echo acquitte exactement les deux mutations d'une même entité")
    void exactMutationIdAcknowledgesBoth() {
        SyncOutbox first = outbox("o1", "vente-1", "VENTE");
        SyncOutbox second = outbox("o2", "vente-1", "VENTE");

        List<String> matched = SyncAcknowledgements.matchOnePerResult(
                List.of(first, second),
                List.of(new BatchResultDto.EntryResult("o2", "vente-1", "VENTE", null),
                        new BatchResultDto.EntryResult("o1", "vente-1", "VENTE", null)));

        assertEquals(List.of("o2", "o1"), matched);
    }

    @Test
    @DisplayName("Un mutationId inconnu n'acquitte rien, même avec un entityId valide")
    void unknownMutationIdDoesNotFallBack() {
        SyncOutbox first = outbox("o1", "vente-1", "VENTE");

        List<String> matched = SyncAcknowledgements.matchOnePerResult(
                List.of(first),
                List.of(new BatchResultDto.EntryResult("inconnu", "vente-1", "VENTE", null)));

        assertTrue(matched.isEmpty(),
                "un serveur qui echo un id inconnu ne doit pas faire perdre la mutation locale");
    }

    @Test
    @DisplayName("Un résultat orphelin ou incomplet n'acquitte rien")
    void ignoresUnknownAndIncompleteResults() {
        SyncOutbox first = outbox("o1", "prod-1", "PRODUIT");

        List<String> matched = SyncAcknowledgements.matchOnePerResult(
                List.of(first),
                List.of(success("prod-2", "PRODUIT"),
                        success("prod-1", null),
                        new BatchResultDto.EntryResult(null, "PRODUIT", null)));

        assertTrue(matched.isEmpty());
    }
}
