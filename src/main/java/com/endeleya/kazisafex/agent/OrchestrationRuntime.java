package com.endeleya.kazisafex.agent;

import com.endeleya.ia.AiAgents;
import com.endeleya.kazisafex.agent.legacy.KazisafeLegacyFacade;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.time.Duration;

/**
 * Production holder of the {@link AgentEngine}.
 *
 * <p>The engine is built exactly once per process and reused across chat requests:
 * compiling the Pregel graphs and initializing the resilience registry on every
 * message would leak virtual-thread executors and circuit breakers. The engine is
 * immutable after wiring (no internal mutable session state), so sharing is safe.</p>
 *
 * <p>It is wired over the real legacy runtime ({@link KazisafeLegacyFacade} over the
 * actual {@code com.endeleya.ia.GratienTools}) and over the same Ollama model the chat
 * already uses ({@link AiAgents#ollamaBaseUrl()}/{@link AiAgents#MODEL_NAME}). When
 * the legacy context or the model is unavailable, the pipeline degrades to the
 * deterministic catalogue/planner instead of failing: the {@link PlannerNode} falls
 * back to the keyword router whenever the LLM call itself fails.</p>
 */
public final class OrchestrationRuntime {

    private static volatile AgentEngine engine;
    private static volatile ChatModel model;
    private static volatile OrchestrationChatAdapter adapter;
    private static volatile TokenBudget budget;

    private OrchestrationRuntime() {
    }

    /**
     * Returns the process-wide engine, building it lazily on first access.
     *
     * @return the shared {@link AgentEngine}
     */
    public static AgentEngine engine() {
        AgentEngine local = engine;
        if (local == null) {
            synchronized (OrchestrationRuntime.class) {
                local = engine;
                if (local == null) {
                    local = AgentEngine.wire(new KazisafeLegacyFacade(), chatModel());
                    engine = local;
                }
            }
        }
        return local;
    }

    /**
     * Retourne le budget de tokens SHARED du chef d'orchestration : l'unique plafond
     * « tsub » (chiffré) contre lequel toutes les consommations sont alignées. Première
     * visite : s'assure qu'une valeur existe dans les préférences, puis lit le plafond.
     *
     * @return le budget partagé (jamais {@code null})
     */
    public static TokenBudget budget() {
        TokenBudget local = budget;
        if (local == null) {
            synchronized (OrchestrationRuntime.class) {
                local = budget;
                if (local == null) {
                    TokenBudget.ensurePresent();
                    budget = new TokenBudget();
                }
            }
        }
        return budget;
    }

    /**
     * Returns the shared Ollama chat model the orchestrator runs on — wrapped in a
     * {@link TokenCountingChatModel} so that EVERY LLM call (planificateur, critic,
     * formateur, retentatives directes, classifieur sémantique de l'adaptateur) est
     * cumulé dans le budget « tsub » partagé. Also consumed by the semantic
     * (multi-language) routing fallback of {@link #adapter()}.
     *
     * @return the shared counting {@link ChatModel}, or {@code null} when unavailable
     */
    public static ChatModel chatModel() {
        ChatModel local = model;
        if (local == null) {
            synchronized (OrchestrationRuntime.class) {
                local = model;
                if (local == null) {
                    local = buildChatModel();
                    model = local;
                }
            }
        }
        return local;
    }

    private static ChatModel buildChatModel() {
        try {
            OllamaChatModel raw = OllamaChatModel.builder()
                    .baseUrl(AiAgents.ollamaBaseUrl())
                    .customHeaders(AiAgents.ollamaAuthHeaders())
                    .modelName(AiAgents.MODEL_NAME)
                    .temperature(0.25)
                    .timeout(Duration.ofMinutes(5))
                    .build();
            return new TokenCountingChatModel(raw, budget());
        } catch (Throwable ex) {
            return null;
        }
    }

    /**
     * Returns the shared chat adapter used by the UI: routing decision (lexical then
     * optional semantic classifier) and rendering of orchestrated responses. Reuses the
     * process-wide engine and semantic model.
     *
     * @return the shared {@link OrchestrationChatAdapter}
     */
    public static OrchestrationChatAdapter adapter() {
        OrchestrationChatAdapter local = adapter;
        if (local == null) {
            synchronized (OrchestrationRuntime.class) {
                local = adapter;
                if (local == null) {
                    local = new OrchestrationChatAdapter(engine(), chatModel());
                    adapter = local;
                }
            }
        }
        return local;
    }

    /**
     * Drops the cached engine/model/adapter (test/teardown only).
     */
    static void reset() {
        synchronized (OrchestrationRuntime.class) {
            engine = null;
            model = null;
            adapter = null;
            budget = null;
        }
    }
}