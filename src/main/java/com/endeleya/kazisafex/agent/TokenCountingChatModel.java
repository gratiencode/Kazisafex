package com.endeleya.kazisafex.agent;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import java.util.Set;

/**
 * Décorateur {@link ChatModel} qui compte les tokens consommés par TOUS les appels
 * qu'il reçoit (entrée {prompt} + sortie {réponse}) dans un {@link TokenBudget}
 * partagé.
 *
 * <p>Toute la chaîne passe par cet unique wrapper : les proxies AiServices
 * (planificateur, critic, formateur), les retentatives directes et le classifieur
 * sémantique multic-lingue. La somme cumulée alimente le budget qui remplace
 * l'ancien « budget d'itérations ».</p>
 */
public final class TokenCountingChatModel implements ChatModel {

    private final ChatModel delegate;
    private final TokenBudget budget;

    /**
     * Enveloppe le modèle pour compter ses tokens.
     *
     * @param delegate modèle réel (Ollama ou mock)
     * @param budget   budget à alimenter
     */
    public TokenCountingChatModel(ChatModel delegate, TokenBudget budget) {
        this.delegate = delegate;
        this.budget = budget;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatResponse response = delegate.chat(request);
        count(response);
        return response;
    }

    @Override
    public ChatResponse doChat(ChatRequest request) {
        ChatResponse response = delegate.doChat(request);
        count(response);
        return response;
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return delegate.defaultRequestParameters();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return delegate.supportedCapabilities();
    }

    /** Budget alimenté par ce wrapper. */
    public TokenBudget budget() {
        return budget;
    }

    private void count(ChatResponse response) {
        if (response == null) {
            return;
        }
        TokenUsage usage = response.tokenUsage();
        if (usage == null) {
            return;
        }
        Integer input = usage.inputTokenCount();
        Integer output = usage.outputTokenCount();
        budget.add(input == null ? 0 : input, output == null ? 0 : output);
    }
}