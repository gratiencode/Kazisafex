package com.endeleya.ia;

import java.util.ArrayList;
import java.util.List;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
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
        for (int i = 1; i <= 10; i++) {
            String payload = RedisMemoryStore.serializePayload(i % 2 == 0 ? "assistant" : "user",
                    "message " + i);
            storage.append(sessionKey, payload);
        }
        // Limite atteinte (10 messages substantifs) : compactage.
        // L'eviction purge toute la memoire et ne garde que le contexte compacte.
        String resume = RedisMemoryStore.serializePayload("system", "[Contexte compacte] resume du contexte");
        List<String> renewed = new ArrayList<>();
        renewed.add(resume);
        storage.replaceRaw(sessionKey, renewed);

        List<String> result = storage.recent(sessionKey, 10);
        assertEquals(1, result.size());
        String[] first = RedisMemoryStore.parsePayload(result.get(0));
        assertEquals("system", first[0]);
        assertTrue(first[1].startsWith("[Contexte compacte]"));
    }

    @Test
    public void testRecentConversationalTailIgnoreLeSpamEtCommenceParUnUtilisateur() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .name("planExecution")
                .arguments("{}")
                .build();
        List<ChatMessage> raw = new ArrayList<>();
        raw.add(SystemMessage.from("prompt systeme"));
        raw.add(UserMessage.from("supprime les doublons clients"));
        raw.add(AiMessage.from(request));
        raw.add(ToolExecutionResultMessage.from(request, "voici le plan"));
        raw.add(UserMessage.from("oui"));
        raw.add(AiMessage.from("Je lance l'execution."));

        List<ChatMessage> tail = AiAgents.recentConversationalTail(raw, 10);

        assertTrue(tail.stream().noneMatch(m -> m instanceof SystemMessage));
        assertTrue(tail.stream().noneMatch(m -> m instanceof ToolExecutionResultMessage));
        assertTrue(tail.stream().noneMatch(m -> m instanceof AiMessage ai
                && (ai.text() == null || ai.text().isBlank())));
        assertEquals(3, tail.size());
        assertTrue(tail.get(0) instanceof UserMessage);
        assertEquals("supprime les doublons clients", ((UserMessage) tail.get(0)).singleText());
        assertEquals("oui", ((UserMessage) tail.get(1)).singleText());
        assertEquals("Je lance l'execution.", ((AiMessage) tail.get(2)).text());
    }

    @Test
    public void testRecentConversationalTailBorneLeNombreDeMessages() {
        List<ChatMessage> raw = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            raw.add(UserMessage.from("question " + i));
            raw.add(AiMessage.from("reponse " + i));
        }
        List<ChatMessage> tail = AiAgents.recentConversationalTail(raw, 10);
        assertEquals(10, tail.size());
        assertTrue(tail.get(0) instanceof UserMessage);
        assertEquals("question 26", ((UserMessage) tail.get(0)).singleText());
        assertEquals("reponse 30", ((AiMessage) tail.get(9)).text());
    }

    @Test
    public void testRecentConversationalTailVideOuNull() {
        assertTrue(AiAgents.recentConversationalTail(null, 10).isEmpty());
        assertTrue(AiAgents.recentConversationalTail(new ArrayList<>(), 10).isEmpty());
        assertTrue(AiAgents.recentConversationalTail(
                new ArrayList<>(List.of(UserMessage.from("bonjour"))), 0).isEmpty());
    }
}
