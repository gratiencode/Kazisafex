package com.endeleya.ia;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Valide la bascule automatique entre modeles d'extraction : si le premier
 * modele echoue (HTTP 500, modele non multimodal, indisponible...), l'appel
 * est retente avec le modele suivant.
 */
public class OllamaModelFallbackTest {

    private ChatRequest request() {
        return ChatRequest.builder().messages(UserMessage.from("test")).build();
    }

    @Test
    public void testPremierModeleOperationnelUtiliseDirectement() {
        ChatModel primary = mock(ChatModel.class);
        ChatResponse primaryResponse = response("json-primary");
        when(primary.chat(any(ChatRequest.class))).thenReturn(primaryResponse);
        ChatModel secondary = mock(ChatModel.class);

        OllamaModelFallback fallback = new OllamaModelFallback(List.of(primary, secondary), List.of("m1", "m2"));
        assertEquals("json-primary", fallback.chat(request()));
        verify(primary).chat(any(ChatRequest.class));
        verify(secondary, never()).chat(any(ChatRequest.class));
    }

    @Test
    public void testEchecDuPremierBasculeSurLeSuivant() {
        ChatModel failing = mock(ChatModel.class);
        when(failing.chat(any(ChatRequest.class))).thenThrow(new RuntimeException("500 Internal Server Error"));
        ChatModel fallbackOk = mock(ChatModel.class);
        ChatResponse fallbackResponse = response("json-minimax");
        when(fallbackOk.chat(any(ChatRequest.class))).thenReturn(fallbackResponse);

        OllamaModelFallback fallback = new OllamaModelFallback(List.of(failing, fallbackOk), List.of("gemma4", "minimax"));
        assertEquals("json-minimax", fallback.chat(request()));
        verify(failing).chat(any(ChatRequest.class));
        verify(fallbackOk).chat(any(ChatRequest.class));
    }

    @Test
    public void testReponseVideDuPremierBasculeSurLeSuivant() {
        ChatModel empty = mock(ChatModel.class);
        when(empty.chat(any(ChatRequest.class))).thenReturn(null);
        ChatModel ok = mock(ChatModel.class);
        ChatResponse okResponse = response("json-ok");
        when(ok.chat(any(ChatRequest.class))).thenReturn(okResponse);

        OllamaModelFallback fallback = new OllamaModelFallback(List.of(empty, ok), List.of("m1", "m2"));
        assertEquals("json-ok", fallback.chat(request()));
    }

    @Test
    public void testTousEchouentLeveLaDerniereErreur() {
        ChatModel failing = mock(ChatModel.class);
        when(failing.chat(any(ChatRequest.class))).thenThrow(new RuntimeException("boom"));

        OllamaModelFallback fallback = new OllamaModelFallback(List.of(failing), List.of("gemma4"));
        assertThrows(RuntimeException.class, () -> fallback.chat(request()));
    }

    private static ChatResponse response(String text) {
        ChatResponse response = mock(ChatResponse.class);
        when(response.aiMessage()).thenReturn(AiMessage.from(text));
        return response;
    }
}
