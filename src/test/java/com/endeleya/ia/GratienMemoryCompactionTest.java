package com.endeleya.ia;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Valide la memoire de Gratien : fallback InMemoryStorage borne aux 10 derniers
 * messages, format de payload partage, et renouvellement par compactage ou le
 * contexte compacte devient le message numero 1.
 */
public class GratienMemoryCompactionTest {

    @Test
    public void testInMemoryStorageGardeLes10DerniersMessages() {
        InMemoryStorage storage = new InMemoryStorage(10);
        for (int i = 1; i <= 13; i++) {
            storage.append("session:1", RedisMemoryStore.serializePayload("user", "message " + i));
        }
        List<String> recent = storage.recent("session:1", 10);
        assertEquals(10, recent.size());
        assertTrue(recent.get(0).contains("message 4"));
        assertTrue(recent.get(9).contains("message 13"));
        assertFalse(storage.recent("session:1", 10).stream().anyMatch(m -> m.contains("content=message 1}")));
    }

    @Test
    public void testPayloadRoundTrip() {
        String payload = RedisMemoryStore.serializePayload("USER", "Bonjour, c'est le prix du lait ?");
        String[] parts = RedisMemoryStore.parsePayload(payload);
        assertEquals("user", parts[0]);
        assertTrue(parts[1].contains("prix du lait"));
    }

    @Test
    public void testPayloadAvecContenuDifficile() {
        String content = "Vente de 5 lait pour 12 500 FCFA, role = caissier, content=precedent, note } final";
        String payload = RedisMemoryStore.serializePayload("assistant", content);
        String[] parts = RedisMemoryStore.parsePayload(payload);
        assertEquals("assistant", parts[0]);
        assertEquals(content, parts[1]);
    }

    @Test
    public void testPayloadNonReconnu() {
        String[] parts = RedisMemoryStore.parsePayload("langchain:SOMETHING");
        assertEquals("", parts[0]);
        assertEquals("langchain:SOMETHING", parts[1]);
    }

    @Test
    public void testCompactageRemplaceLeContexteParMessageNumero1() {
        InMemoryStorage storage = new InMemoryStorage(10);
        String sessionKey = "kazisafex:Gratien:memory:ent:user";
        List<String> messages = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String payload = RedisMemoryStore.serializePayload(i % 2 == 0 ? "assistant" : "user",
                    "message " + i);
            messages.add(payload);
            storage.append(sessionKey, payload);
        }
        // Limite atteinte (10 messages substantifs) : compactage.
        String resume = RedisMemoryStore.serializePayload("system", "[Contexte compacte] resume du contexte");
        List<String> renewed = new ArrayList<>();
        renewed.add(resume);
        int tail = Math.min(6, messages.size());
        for (int i = messages.size() - tail; i < messages.size(); i++) {
            renewed.add(messages.get(i));
        }
        storage.replaceRaw(sessionKey, renewed);

        List<String> result = storage.recent(sessionKey, 10);
        assertEquals(7, result.size());
        String[] first = RedisMemoryStore.parsePayload(result.get(0));
        assertEquals("system", first[0]);
        assertTrue(first[1].startsWith("[Contexte compacte]"));
    }
}
