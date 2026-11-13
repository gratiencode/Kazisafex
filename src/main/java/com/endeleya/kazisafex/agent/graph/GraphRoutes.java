package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.TokenBudget;
import org.bsc.langgraph4j.action.EdgeAction;

/**
 * Constants and routing functions of the master graph.
 *
 * <p>All node names, route keys and the token budget live here so the topology is
 * readable and auditable in one place.</p>
 */
public final class GraphRoutes {

    /** Node identifiers. */
    public static final String NODE_GUARDRAIL = "guardrail";
    public static final String NODE_PLANNER = "planner";
    public static final String NODE_EXECUTOR = "legacy_executor";
    public static final String NODE_CRITIC = "critic";
    public static final String NODE_FORMATTER = "formatter";
    public static final String NODE_ABORT = "abort";

    /** Route keys emitted by the guardrail edge. */
    public static final String ROUTE_GUARDRAIL_PASS = "guardrail-pass";
    public static final String ROUTE_GUARDRAIL_BLOCK = "guardrail-block";
    public static final String ROUTE_GUARDRAIL_APPROVED = "guardrail-approved";

    /** Route keys emitted by the critic edge. */
    public static final String ROUTE_CRITIC_COMPLETE = "critic-complete";
    public static final String ROUTE_CRITIC_REPLAN = "critic-replan";
    public static final String ROUTE_CRITIC_ABORT = "critic-abort";

    /**
     * Garde-fou STRUCTUREL anti-boucle (dégradé sans modèle) : quand aucun LLM
     * n'est présent, la consommation de tokens est nulle, donc le budget de tokens
     * ne peut pas s'épuiser. Ce compteur n'est PAS le budget d'itérations — il évite
     * seulement qu'une boucle de reconciliation deterministe ne finisse en exception
     * de recursion au-dessus du recursionLimit du graphe.
     */
    public static final int REPLAN_LIMIT = 8;

    private GraphRoutes() {
    }

    /**
     * Guardrail routing: a blank {@code guardrailReason} means the query passed. When
     * the plan was already approved by a human ({@code approved==true}) the executor is
     * reached directly (the planner is skipped: it already ran during the proposal).
     *
     * @return route key
     */
    public static EdgeAction<OrchestratorState> guardrailRoute() {
        return state -> {
            if (!state.guardrailReason().isBlank()) {
                return ROUTE_GUARDRAIL_BLOCK;
            }
            if (state.approved()) {
                return ROUTE_GUARDRAIL_APPROVED;
            }
            return ROUTE_GUARDRAIL_PASS;
        };
    }

    /**
     * Critic routing: {@code COMPLETE} goes to the formatter; any other verdict is
     * re-planned while the TOKEN budget allows, otherwise the graph aborts.
     *
     * <p>Le budget d'itérations n'existe plus : la boucle est gouvernée par
     * {@link TokenBudget#exhausted()} (somme des tokens entrée+sortie rapportée au
     * plafond « tsub »). Le palier {@link #REPLAN_LIMIT} n'intervient qu'en secours
     * purement structurel (mode déterministe sans modèle).</p>
     *
     * @param budget budget de tokens partagé (jamais {@code null} en production)
     * @return route key
     */
    public static EdgeAction<OrchestratorState> criticRoute(TokenBudget budget) {
        return state -> {
            CriticEvaluation evaluation = state.criticEvaluation();
            if (evaluation.verdict() == CriticEvaluation.CriticVerdict.COMPLETE) {
                return ROUTE_CRITIC_COMPLETE;
            }
            if (budget != null && budget.exhausted()) {
                return ROUTE_CRITIC_ABORT;
            }
            if (state.iterationCount() >= REPLAN_LIMIT) {
                return ROUTE_CRITIC_ABORT;
            }
            return ROUTE_CRITIC_REPLAN;
        };
    }
}