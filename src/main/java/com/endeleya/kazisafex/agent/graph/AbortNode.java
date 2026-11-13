package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.TokenBudget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminal failure node: records the abort reason and stops the graph.
 *
 * <p>It is reached through the guardrail (blocked input), when the <b>token budget</b>
 * (plafond « tsub », somme entrée+sortie) is exhausted, or when the critic returns an
 * {@code ABORT} verdict après des échecs systématiques. Attention au vocabulaire : un
 * verdict critique {@code ABORT} n'est PAS un budget de tokens — ceci a conduit à des
 * messages « Budget de tokens epuise » affichés alors que des tokens restaient. Chaque
 * cause produit donc son propre message, chiffré si possible (tokens consommés, replans,
 * étapes en échec).</p>
 */
public final class AbortNode implements NodeAction<OrchestratorState> {

    private static final Logger LOG = LoggerFactory.getLogger(AbortNode.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_DETAIL = 160;

    private final TokenBudget budget;

    /**
     * Creates the abort node.
     *
     * @param budget budget de tokens partagé du chef d'orchestration (nullable en test)
     */
    public AbortNode(TokenBudget budget) {
        this.budget = budget;
    }

    /** Budget de tokens porté par ce nœud (nullable en test). */
    public TokenBudget budget() {
        return budget;
    }

    @Override
    public Map<String, Object> apply(OrchestratorState state) {
        String reason = abortReason(state);
        LOG.warn("Abort: {}", reason);

        return Map.of(
                "abortReason", reason,
                "messages", List.of(TranscriptEntry.system("Abandon: " + reason)));
    }

    private String abortReason(OrchestratorState state) {
        if (!state.guardrailReason().isBlank()) {
            return state.guardrailReason();
        }
        if (budget != null && budget.exhausted()) {
            return "Budget de tokens epuise : " + compact(budget.consumed()) + "/"
                    + compact(budget.cap()) + " tokens consommes. "
                    + state.criticEvaluation().feedback();
        }
        if (state.criticEvaluation().verdict() == CriticEvaluation.CriticVerdict.ABORT) {
            return "Abandon : l'execution a echoue apres " + state.iterationCount()
                    + " tentative(s)." + tokenStatus() + " "
                    + state.criticEvaluation().feedback() + failedSteps(state);
        }
        if (state.iterationCount() >= GraphRoutes.REPLAN_LIMIT) {
            return "Garde structurelle atteinte apres " + state.iterationCount()
                    + " replans (aucun modele n'est disponible) : "
                    + state.criticEvaluation().feedback() + failedSteps(state);
        }
        if (!state.abortReason().isBlank()) {
            return state.abortReason();
        }
        return "Execution interrompue par le chef d'orchestration.";
    }

    private long consumed() {
        return budget == null ? 0 : budget.consumed();
    }

    /** Note factuelle de consommation, jamais un motif d'abandon. */
    private String tokenStatus() {
        if (budget == null) {
            return "";
        }
        long cap = budget.cap();
        long remaining = budget.remaining();
        int pct = (int) Math.round(cap <= 0 ? 0 : (remaining * 100.0 / cap));
        return " Tokens consommes : " + compact(consumed())
                + " (reste " + Math.max(0, pct) + "% du plafond).";
    }

    private String compact(long value) {
        return TokenBudget.formatCompact(value);
    }

    /** Détail—étapes en échec (libellés lisibles) + erreurs observées. */
    private String failedSteps(OrchestratorState state) {
        List<String> labels = state.stepStatuses().entrySet().stream()
                .filter(e -> LegacyExecutorNode.STATUS_ERROR.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .distinct()
                .limit(4)
                .collect(Collectors.toList());
        StringBuilder detail = new StringBuilder();
        if (!labels.isEmpty()) {
            detail.append(" Echec observe sur les etapes : ")
                    .append(String.join(", ", labels)).append('.');
        }
        List<String> errors = extractErrors(state);
        if (!errors.isEmpty()) {
            detail.append(" Detail : ").append(String.join(" ; ", errors)).append('.');
        }
        return detail.toString();
    }

    /** Extrait les messages réels des résultats legacy en échec (jusqu'à 3, tronqués). */
    private List<String> extractErrors(OrchestratorState state) {
        List<String> errors = new ArrayList<>();
        for (Map.Entry<String, Object> entry : state.legacyContext().entrySet()) {
            if (entry.getKey().equals("iterations") || entry.getValue() instanceof Integer) {
                continue;
            }
            String message = errorText(entry.getValue());
            if (message != null) {
                errors.add(abbreviate(message));
                if (errors.size() >= 3) {
                    break;
                }
            }
        }
        return errors;
    }

    private String errorText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence raw) {
            String text = raw.toString();
            return text.isBlank() ? null : jsonErrorText(text, text);
        }
        if (value instanceof Map<?, ?> map && map.get("error") != null) {
            return String.valueOf(map.get("error"));
        }
        return jsonErrorText(String.valueOf(value), String.valueOf(value));
    }

    private String jsonErrorText(String raw, String fallback) {
        if (raw.startsWith("{") && raw.contains("\"error\"")) {
            try {
                JsonNode node = JSON.readTree(raw);
                JsonNode error = node.path("error");
                if (error.isTextual()) {
                    return error.asText();
                }
            } catch (Exception ignored) {
                // Pas du JSON -> on garde la valeur brute.
            }
        }
        return fallback;
    }

    private String abbreviate(String message) {
        String trimmed = message.replace('\n', ' ').trim();
        if (trimmed.length() <= MAX_DETAIL) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_DETAIL) + "…";
    }
}