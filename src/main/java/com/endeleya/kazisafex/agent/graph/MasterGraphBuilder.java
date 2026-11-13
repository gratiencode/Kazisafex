package com.endeleya.kazisafex.agent.graph;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.endeleya.kazisafex.agent.TokenBudget;
import java.util.Map;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;

/**
 * Compiles the master orchestrator {@link StateGraph}.
 *
 * <p>Topology (conditional routing per the architecture specification):</p>
 * <pre>
 *   START -> guardrail
 *     guardrail --[guardrail-pass  ]--> planner
 *     guardrail --[guardrail-block ]--> abort
 *     guardrail --[guardrail-approved]--> legacy_executor   (human-in-the-loop resume)
 *   planner -> legacy_executor -> critic
 *     critic   --[critic-complete]--> formatter
 *     critic   --[critic-replan ]--> planner        (while token budget allows)
 *     critic   --[critic-abort  ]--> abort          (token budget exhausted)
 *   formatter -> END
 *   abort -> END
 * </pre>
 *
 * <p>A second, lighter graph ({@link #buildPlanningGraph()}) stops right after the
 * planner: it is the "proposal" phase of the human-in-the-loop flow, letting the UI
 * show the plan to the user before any legacy call runs.</p>
 *
 * <p>The critic/planner loop is gouverné par le budget de TOKENS (somme entrée+sortie
 * du plafond « tsub ») : il n'y a plus de compteur d'itérations arbitraire. La garde
 * {@link GraphRoutes#REPLAN_LIMIT} n'est qu'un secours structurel en mode déterministe
 * sans modèle, et le {@code recursionLimit} compilé reste le filet ultime.</p>
 */
public final class MasterGraphBuilder {

    private static final int RECURSION_LIMIT = 60;

    private final GuardrailNode guardrail;
    private final PlannerNode planner;
    private final LegacyExecutorNode executor;
    private final CriticNode critic;
    private final FormatterNode formatter;
    private final AbortNode abort;
    private final TokenBudget budget;

    /**
     * Creates the builder with the five governance/reconciliation nodes.
     *
     * @param guardrail guardrail node
     * @param planner   planner node
     * @param executor  legacy executor node
     * @param critic    critic node
     * @param formatter structured formatter node
     * @param abort     abort node
     * @param budget    budget de tokens partagé (plafond « tsub »)
     */
    public MasterGraphBuilder(GuardrailNode guardrail, PlannerNode planner,
            LegacyExecutorNode executor, CriticNode critic,
            FormatterNode formatter, AbortNode abort, TokenBudget budget) {
        this.guardrail = guardrail;
        this.planner = planner;
        this.executor = executor;
        this.critic = critic;
        this.formatter = formatter;
        this.abort = abort;
        this.budget = budget;
    }

    /**
     * Compiles the graph.
     *
     * @return the runnable {@link CompiledGraph} (Pregel runner)
     * @throws org.bsc.langgraph4j.GraphStateException if the topology is invalid
     */
    public CompiledGraph<OrchestratorState> build() throws org.bsc.langgraph4j.GraphStateException {
        return new StateGraph<>(OrchestratorState.SCHEMA, OrchestratorState::new)
                .addNode(GraphRoutes.NODE_GUARDRAIL, node_async(guardrail))
                .addNode(GraphRoutes.NODE_PLANNER, node_async(planner))
                .addNode(GraphRoutes.NODE_EXECUTOR, node_async(executor))
                .addNode(GraphRoutes.NODE_CRITIC, node_async(critic))
                .addNode(GraphRoutes.NODE_FORMATTER, node_async(formatter))
                .addNode(GraphRoutes.NODE_ABORT, node_async(abort))
                .addEdge(START, GraphRoutes.NODE_GUARDRAIL)
                .addConditionalEdges(GraphRoutes.NODE_GUARDRAIL,
                        edge_async(GraphRoutes.guardrailRoute()),
                        Map.of(
                                GraphRoutes.ROUTE_GUARDRAIL_PASS, GraphRoutes.NODE_PLANNER,
                                GraphRoutes.ROUTE_GUARDRAIL_APPROVED, GraphRoutes.NODE_EXECUTOR,
                                GraphRoutes.ROUTE_GUARDRAIL_BLOCK, GraphRoutes.NODE_ABORT))
                .addEdge(GraphRoutes.NODE_PLANNER, GraphRoutes.NODE_EXECUTOR)
                .addEdge(GraphRoutes.NODE_EXECUTOR, GraphRoutes.NODE_CRITIC)
                .addConditionalEdges(GraphRoutes.NODE_CRITIC,
                        edge_async(GraphRoutes.criticRoute(budget)),
                        Map.of(
                                GraphRoutes.ROUTE_CRITIC_COMPLETE, GraphRoutes.NODE_FORMATTER,
                                GraphRoutes.ROUTE_CRITIC_REPLAN, GraphRoutes.NODE_PLANNER,
                                GraphRoutes.ROUTE_CRITIC_ABORT, GraphRoutes.NODE_ABORT))
                .addEdge(GraphRoutes.NODE_FORMATTER, END)
                .addEdge(GraphRoutes.NODE_ABORT, END)
                .compile(CompileConfig.builder().recursionLimit(RECURSION_LIMIT).build());
    }

    /**
     * Compiles the lightweight proposal graph used by the human-in-the-loop flow.
     *
     * <pre>
     *   START -> guardrail -> planner -> END
     *     guardrail --[guardrail-block]--> abort -> END
     * </pre>
     *
     * It never touches the legacy executor: it only produces the plan that is shown
     * to the user for approval.
     *
     * @return the runnable {@link CompiledGraph} of the planning phase
     * @throws org.bsc.langgraph4j.GraphStateException if the topology is invalid
     */
    public CompiledGraph<OrchestratorState> buildPlanningGraph() throws org.bsc.langgraph4j.GraphStateException {
        return new StateGraph<>(OrchestratorState.SCHEMA, OrchestratorState::new)
                .addNode(GraphRoutes.NODE_GUARDRAIL, node_async(guardrail))
                .addNode(GraphRoutes.NODE_PLANNER, node_async(planner))
                .addNode(GraphRoutes.NODE_ABORT, node_async(abort))
                .addEdge(START, GraphRoutes.NODE_GUARDRAIL)
                .addConditionalEdges(GraphRoutes.NODE_GUARDRAIL,
                        edge_async(GraphRoutes.guardrailRoute()),
                        Map.of(
                                GraphRoutes.ROUTE_GUARDRAIL_PASS, GraphRoutes.NODE_PLANNER,
                                GraphRoutes.ROUTE_GUARDRAIL_BLOCK, GraphRoutes.NODE_ABORT))
                .addEdge(GraphRoutes.NODE_PLANNER, END)
                .addEdge(GraphRoutes.NODE_ABORT, END)
                .compile(CompileConfig.builder().recursionLimit(RECURSION_LIMIT).build());
    }
}