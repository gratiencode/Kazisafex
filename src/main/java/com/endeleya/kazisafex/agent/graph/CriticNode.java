package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.JsonSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fourth node: inspects the legacy outputs and decides whether the task is complete,
 * must be retried / re-planned, or must abort.
 *
 * <p>Default evaluation is deterministic (missing, empty, {@code error} payloads), so
 * the critic never depends on a model. When an LLM critic is bound it is preferred and
 * its verdict replaces the deterministic one. LLM outputs are parsed tolerantly
 * (Markdown-fenced JSON or noisy text): if the structured proxy fails, the model is
 * re-called directly and the JSON is extracted manually, so a parsing hiccup never
 * crashes the graph.</p>
 */
public final class CriticNode implements NodeAction<OrchestratorState> {

    private static final Logger LOG = LoggerFactory.getLogger(CriticNode.class);

    private static final ObjectMapper JSON = new ObjectMapper();

    private final AgentAssistants.Assistants assistants;

    /**
     * Creates the critic.
     *
     * @param assistants AiServices proxies (may be disabled)
     */
    public CriticNode(AgentAssistants.Assistants assistants) {
        this.assistants = assistants;
    }

    @Override
    public Map<String, Object> apply(OrchestratorState state) {
        CriticEvaluation evaluation =
                assistants.enabled() ? evaluateFromLlm(state) : evaluateDeterministic(state);
        LOG.info("Critique -> {} (score {}) : {}", evaluation.verdict(), evaluation.score(),
                evaluation.feedback());

        return Map.of(
                "criticEvaluation", evaluation,
                "messages", List.of(TranscriptEntry.system("Critique: " + evaluation.verdict()
                        + " - " + evaluation.feedback())));
    }

    private CriticEvaluation evaluateDeterministic(OrchestratorState state) {
        ExecutionPlan plan = state.activePlan();
        if (!plan.hasSteps()) {
            return new CriticEvaluation(CriticEvaluation.CriticVerdict.REVISE,
                    "Le plan est vide : aucune etape reutilisable.", 0, state.iterationCount());
        }
        Map<String, Object> context = state.legacyContext();
        List<String> issues = new ArrayList<>();
        for (PlanStep step : plan.steps()) {
            Object output = context.get(step.toolName());
            if (output == null || String.valueOf(output).isBlank()) {
                issues.add(step.toolName() + ": resultat absent ou vide");
            } else if (String.valueOf(output).contains("\"error\"")) {
                issues.add(step.toolName() + ": echec signale par le workflow (" + output + ")");
            }
        }
        if (issues.isEmpty()) {
            return new CriticEvaluation(CriticEvaluation.CriticVerdict.COMPLETE,
                    "Toutes les etapes ont retourne un resultat exploitable.", 100,
                    state.iterationCount());
        }
        String feedback = issues.stream().collect(Collectors.joining(" ; "));
        boolean replan = issues.stream().anyMatch(i -> i.contains("capacite inconnue"));
        return new CriticEvaluation(replan
                        ? CriticEvaluation.CriticVerdict.REVISE
                        : CriticEvaluation.CriticVerdict.RETRY,
                feedback, Math.max(0, 60 - 10 * state.iterationCount()), state.iterationCount());
    }

    private CriticEvaluation evaluateFromLlm(OrchestratorState state) {
        String plan = state.activePlan().summary();
        String results = state.legacyContext().entrySet().stream()
                .filter(e -> !"iterations".equals(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
        CriticEvaluation evaluation;
        try {
            evaluation = assistants.critic().evaluate(
                    plan, JsonSupport.trimTo(results, 12_000), state.iterationCount());
        } catch (RuntimeException ex) {
            LOG.warn("Critique LLM non parsee par le proxy ({}), nouvelle tentative directe",
                    ex.getClass().getSimpleName());
            evaluation = retryLlmCritic(plan, results, state.iterationCount());
        }
        return evaluation == null ? evaluateDeterministic(state) : evaluation;
    }

    /**
     * Retentative en direct sur le modèle avec extraction manuelle du JSON: résout les
     * réponses entourées de balises Markdown que le proxy AiServices refuse de parser.
     */
    private CriticEvaluation retryLlmCritic(String plan, String results, int attempt) {
        try {
            ChatResponse response = assistants.model().chat(ChatRequest.builder()
                    .messages(List.of(
                            SystemMessage.from(AgentAssistants.CRITIC_SYSTEM),
                            UserMessage.from("Plan exécuté: " + plan + "\nRésultats legacy:\n"
                                    + JsonSupport.trimTo(results, 12_000)
                                    + "\n\nTentative numéro " + attempt + ".")))
                    .build());
            AiMessage message = response == null ? null : response.aiMessage();
            return parse(message == null ? null : message.text(), attempt);
        } catch (Exception ex) {
            LOG.warn("Retentative critique directe en echec", ex);
            return null;
        }
    }

    /**
     * Parse de façon tolérante un verdict de critique produit par le modèle: extraction
     * du premier objet JSON (balises Markdown ignorées), champs manquants par défaut.
     *
     * @param text    texte renvoyé par le modèle
     * @param attempt numéro de tentative à reporter si le champ {@code attempt} manque
     * @return l'évaluation, ou {@code null} si aucun JSON exploitable
     */
    public static CriticEvaluation parse(String text, int attempt) {
        String json = JsonSupport.rawJsonObject(text);
        if (json == null) {
            return null;
        }
        try {
            JsonNode root = JSON.readTree(json);
            CriticEvaluation.CriticVerdict verdict = parseVerdict(field(root, "verdict"));
            String feedback = root.has("feedback") && root.get("feedback").isTextual()
                    ? root.get("feedback").asText()
                    : "";
            int score = intField(root, "score", 0);
            int refAttempt = intField(root, "attempt", attempt);
            return new CriticEvaluation(verdict, feedback, score, refAttempt);
        } catch (Exception ex) {
            LOG.warn("Critique LLM illisible: {}", ex.getMessage());
            return null;
        }
    }

    private static CriticEvaluation.CriticVerdict parseVerdict(String verdict) {
        if (verdict == null || verdict.isBlank()) {
            return CriticEvaluation.CriticVerdict.REVISE;
        }
        try {
            return CriticEvaluation.CriticVerdict.valueOf(verdict.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CriticEvaluation.CriticVerdict.REVISE;
        }
    }

    private static String field(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static int intField(JsonNode node, String name, int fallback) {
        JsonNode value = node.get(name);
        return value != null && value.canConvertToInt() ? value.asInt() : fallback;
    }
}