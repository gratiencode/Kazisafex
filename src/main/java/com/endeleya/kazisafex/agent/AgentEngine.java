package com.endeleya.kazisafex.agent;

import com.endeleya.kazisafex.agent.graph.AbortNode;
import com.endeleya.kazisafex.agent.graph.AgentAssistants;
import com.endeleya.kazisafex.agent.graph.AgentResponse;
import com.endeleya.kazisafex.agent.graph.ApprovalStore;
import com.endeleya.kazisafex.agent.graph.CriticNode;
import com.endeleya.kazisafex.agent.graph.ExecutionPlan;
import com.endeleya.kazisafex.agent.graph.FormatterNode;
import com.endeleya.kazisafex.agent.graph.GuardrailNode;
import com.endeleya.kazisafex.agent.graph.LegacyExecutorNode;
import com.endeleya.kazisafex.agent.graph.MasterGraphBuilder;
import com.endeleya.kazisafex.agent.graph.OrchestratorState;
import com.endeleya.kazisafex.agent.graph.PendingApproval;
import com.endeleya.kazisafex.agent.graph.PlannerNode;
import com.endeleya.kazisafex.agent.legacy.LegacyAgentFacade;
import dev.langchain4j.model.chat.ChatModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main facade of the master orchestrator.
 *
 * <p>{@link #execute(String)} is asynchronous ({@link CompletableFuture}) and never
 * blocks a platform thread of the caller: the whole graph run happens on virtual
 * threads, and each legacy call is itself re-dispatched on virtual threads through
 * {@link ResilientLegacyInvoker}.</p>
 *
 * <p>Human-in-the-loop: {@link #plan(String, EnterpriseIdentity)} runs the planner and
 * returns a {@link AgentResponse.Status#PROPOSAL} with a token; the user reviews the
 * plan (todo list), then {@link #executeApproved(String, EnterpriseIdentity)} resumes
 * the graph on the approved plan — skipping the planner — while the identity stored
 * with the proposal is enforced.</p>
 *
 * <p>Runtime assembly:
 * <ul>
 *   <li>{@link LegacyAgentFacade} supplies the legacy capabilities (mock or real);</li>
 *   <li>{@link ChatModel} is optional: when absent, the planner/critic/formatter use
 *       their deterministic implementations;</li>
 *   <li>{@link MasterGraphBuilder} wires the {@link org.bsc.langgraph4j.StateGraph}
 *       into a compiled Pregel graph.</li>
 * </ul>
 * </p>
 */
public final class AgentEngine {

    private static final Logger LOG = LoggerFactory.getLogger(AgentEngine.class);

    private final CompiledGraph<OrchestratorState> graph;
    private final CompiledGraph<OrchestratorState> planningGraph;
    private final LegacyExecutorNode executorNode;
    private final ApprovalStore approvalStore;
    private final ExecutorService virtualThreads;
    private final AbortNode abort;

    /**
     * Creates the engine from the wired node set. Le budget de tokens du graphe est
     * celui porté par {@link AbortNode#budget()} (ou bien un budget neuf).
     *
     * @param guardrail guardrail node
     * @param planner   planner node
     * @param executor  legacy executor node
     * @param critic    critic node
     * @param formatter formatter node
     * @param abort     abort node
     */
    public AgentEngine(GuardrailNode guardrail, PlannerNode planner, LegacyExecutorNode executor,
            CriticNode critic, FormatterNode formatter, AbortNode abort) {
        this(executor, buildGraph(guardrail, planner, executor, critic, formatter, abort, budgetOf(abort)),
                buildPlanningGraph(guardrail, planner, executor, critic, formatter, abort, budgetOf(abort)),
                new ApprovalStore(), Executors.newVirtualThreadPerTaskExecutor(), abort);
    }

    private static TokenBudget budgetOf(AbortNode abort) {
        TokenBudget budget = abort.budget();
        return budget == null ? new TokenBudget() : budget;
    }

    private static CompiledGraph<OrchestratorState> buildGraph(
            GuardrailNode guardrail, PlannerNode planner, LegacyExecutorNode executor,
            CriticNode critic, FormatterNode formatter, AbortNode abort, TokenBudget budget) {
        try {
            return new MasterGraphBuilder(guardrail, planner, executor, critic, formatter, abort, budget).build();
        } catch (org.bsc.langgraph4j.GraphStateException ex) {
            throw new IllegalStateException("Topologie du graphe invalide", ex);
        }
    }

    private static CompiledGraph<OrchestratorState> buildPlanningGraph(
            GuardrailNode guardrail, PlannerNode planner, LegacyExecutorNode executor,
            CriticNode critic, FormatterNode formatter, AbortNode abort, TokenBudget budget) {
        try {
            return new MasterGraphBuilder(guardrail, planner, executor, critic, formatter, abort, budget)
                    .buildPlanningGraph();
        } catch (org.bsc.langgraph4j.GraphStateException ex) {
            throw new IllegalStateException("Topologie du graphe de planification invalide", ex);
        }
    }

    AgentEngine(LegacyExecutorNode executorNode, CompiledGraph<OrchestratorState> graph,
            CompiledGraph<OrchestratorState> planningGraph,
            ApprovalStore approvalStore, ExecutorService virtualThreads, AbortNode abort) {
        this.executorNode = executorNode;
        this.graph = graph;
        this.planningGraph = planningGraph;
        this.approvalStore = approvalStore;
        this.virtualThreads = virtualThreads;
        this.abort = abort;
    }

    /**
     * Builds a fully wired engine (budget de tokens par défaut, plafond « tsub »
     * chiffré).
     *
     * @param facade legacy capability source
     * @param model  optional chat model (planner/critic/formatter), {@code null} allowed
     * @return a ready-to-use engine
     */
    public static AgentEngine wire(LegacyAgentFacade facade, ChatModel model) {
        return wire(facade, model, null);
    }

    /**
     * Builds a fully wired engine with an explicit token cap (tests/tooling).
     *
     * <p>Quand {@code model} est déjà un {@link TokenCountingChatModel}, son budget est
     * réutilisé tel quel (aucun double-comptage) ; sinon le modèle est enveloppé dans un
     * compteur alimentant un budget neuf (défaut {@link TokenBudget#DEFAULT_TSUB} ou le
     * cap explicite).</p>
     *
     * @param facade    legacy capability source
     * @param model     optional chat model, {@code null} allowed
     * @param capTokens plafond de tokens (entrée+sortie) ; {@code null} = préférence « tsub »
     * @return a ready-to-use engine
     */
    public static AgentEngine wire(LegacyAgentFacade facade, ChatModel model, Long capTokens) {
        final TokenBudget budget;
        if (model instanceof TokenCountingChatModel counting) {
            budget = counting.budget();
        } else {
            budget = capTokens == null ? new TokenBudget() : new TokenBudget(capTokens);
            if (model != null) {
                model = new TokenCountingChatModel(model, budget);
            }
        }
        ResilientLegacyInvoker invoker = new ResilientLegacyInvoker();
        LegacyToolAdapter adapter = new LegacyToolAdapter(facade, invoker);
        AgentAssistants.Assistants assistants = AgentAssistants.assemble(model);
        return new AgentEngine(
                new GuardrailNode(),
                new PlannerNode(facade, assistants),
                new LegacyExecutorNode(facade, invoker),
                new CriticNode(assistants),
                new FormatterNode(assistants),
                new AbortNode(budget));
    }

    /**
     * Budget de tokens partagé du moteur (plafond « tsub », somme entrée+sortie).
     *
     * @return le budget alimenté par tous les appels LLM du moteur
     */
    public TokenBudget budget() {
        return abort.budget();
    }

    /**
     * Async entry point, attributed to the current {@code eUid}/{@code ucontract}
     * stored in the application preferences.
     *
     * @param userInput raw user query
     * @return future resolving to the final {@link AgentResponse}
     */
    public CompletableFuture<AgentResponse> execute(String userInput) {
        return execute(userInput, EnterpriseIdentity.current());
    }

    /**
     * Async entry point, confined to the given enterprise identity and engagement.
     *
     * @param userInput  raw user query
     * @param identity   {@link EnterpriseIdentity} of the connected user (never
     *                   derived from the user text; it is the identity boundary of
     *                   the run)
     * @return future resolving to the final {@link AgentResponse}
     */
    public CompletableFuture<AgentResponse> execute(String userInput, EnterpriseIdentity identity) {
        return CompletableFuture.supplyAsync(() -> run(userInput, identity), virtualThreads);
    }

    /**
     * Runs the graph synchronously for the current identity from the preferences.
     *
     * @param userInput raw user query
     * @return the final response
     */
    public AgentResponse run(String userInput) {
        return run(userInput, EnterpriseIdentity.current());
    }

    /**
     * Runs the graph synchronously, confined to the given enterprise identity and
     * engagement.
     *
     * @param userInput  raw user query
     * @param identity   {@link EnterpriseIdentity} of the connected user
     * @return the final response
     */
    public AgentResponse run(String userInput, EnterpriseIdentity identity) {
        EnterpriseIdentity effective = identity == null ? EnterpriseIdentity.current() : identity;
        Map<String, Object> initial = baseInitial(userInput, effective, false, null);
        return runGraph(initial, effective);
    }

    /**
     * Proposes a plan for human approval without executing anything.
     *
     * <p>Runs the lightweight planning graph (guardrail + planner only), then stores
     * the plan under a single-use token. The legacy layer is never touched in this
     * phase; {@link #executeApproved(String, EnterpriseIdentity)} resumes afterwards.</p>
     *
     * @param userInput raw user query
     * @return a PROPOSAL response carrying {@code proposedPlan} and {@code proposalToken},
     *         or BLOCKED/FAILED when the guardrail or the planner rejects the request
     */
    public AgentResponse plan(String userInput) {
        return plan(userInput, EnterpriseIdentity.current());
    }

    /**
     * Proposes a plan for the given identity, see {@link #plan(String)}.
     *
     * @param userInput raw user query
     * @param identity  identity to confine the proposal to
     * @return a PROPOSAL response, or BLOCKED/FAILED
     */
    public AgentResponse plan(String userInput, EnterpriseIdentity identity) {
        EnterpriseIdentity effective = identity == null ? EnterpriseIdentity.current() : identity;
        Map<String, Object> initial = baseInitial(userInput, effective, false, null);

        List<String> trace = new ArrayList<>();
        OrchestratorState finalState = null;
        try {
            AsyncGenerator<NodeOutput<OrchestratorState>> generator = planningGraph.stream(initial);
            for (NodeOutput<OrchestratorState> output : generator) {
                trace.add(output.node());
                finalState = output.state();
            }
        } catch (Exception ex) {
            LOG.error("Erreur de planification", ex);
            return AgentResponse.failed("Erreur de planification: " + ex.getMessage(),
                    Map.of(), 0, effective.enterpriseUid(), effective.engagementId(), Map.of());
        }
        if (finalState == null) {
            return AgentResponse.failed("La planification n'a produit aucun etat.", Map.of(), 0,
                    effective.enterpriseUid(), effective.engagementId(), Map.of());
        }
        if (!finalState.guardrailReason().isBlank()) {
            LOG.info("Planification bloquee (identite {}): {}", effective.attribution(),
                    finalState.guardrailReason());
            return AgentResponse.blocked(finalState.guardrailReason(),
                    finalState.enterpriseUid(), finalState.engagementId());
        }
        ExecutionPlan plan = finalState.activePlan();
        if (!plan.hasSteps()) {
            return AgentResponse.failed("Aucune action a executer: " + plan.reasoning(), Map.of(), 0,
                    finalState.enterpriseUid(), finalState.engagementId(), Map.of());
        }
        String token = approvalStore.store(finalState.userQuery(), plan, effective);
        LOG.info("Plan proposé (identite {}): {}", effective.attribution(), plan.summary());
        return AgentResponse.proposal(plan, token, finalState.enterpriseUid(), finalState.engagementId());
    }

    /**
     * Executes a previously approved plan (human-in-the-loop resume).
     *
     * <p>The token is consumed exactly once; execution is refused when the token is
     * unknown or expired, and when the identity stored with the proposal does not match
     * the identity of the caller — a plan is never executed under a different
     * enterprise/engagement than the one that requested it.</p>
     *
     * @param token    single-use approval token from {@code AgentResponse.proposalToken()}
     * @param identity identity to confine the approval to
     * @return the final response of the resumed run
     */
    public AgentResponse executeApproved(String token, EnterpriseIdentity identity) {
        EnterpriseIdentity effective = identity == null ? EnterpriseIdentity.current() : identity;
        Optional<PendingApproval> maybe = approvalStore.take(token);
        if (maybe.isEmpty()) {
            return AgentResponse.failed("Approbation introuvable ou deja consommee.", Map.of(), 0,
                    effective.enterpriseUid(), effective.engagementId(), Map.of());
        }
        PendingApproval approval = maybe.get();
        if (ApprovalStore.expired(approval)) {
            return AgentResponse.failed("Approbation expiree (>" + ApprovalStore.MAX_AGE_MILLIS / 60000
                    + " min). Relancez une planification.", Map.of(), 0,
                    approval.identity().enterpriseUid(), approval.identity().engagementId(), Map.of());
        }
        if (!approval.identity().equals(effective)) {
            return AgentResponse.failed(
                    "Approbation refusee: le plan appartient a l'identite "
                            + approval.identity().attribution() + ", pas a "
                            + effective.attribution() + ".",
                    Map.of(), 0, approval.identity().enterpriseUid(),
                    approval.identity().engagementId(), Map.of());
        }
        Map<String, Object> initial = baseInitial(
                approval.userQuery(), approval.identity(), true, approval.plan());
        return runGraph(initial, approval.identity());
    }

    /**
     * Attaches a listener receiving {@code (stepId, status)} events while the plan
     * executes, so the UI can tick off the planned todo list in real time.
     *
     * @param listener callback, {@code null} to detach
     */
    public void setStepStatusListener(BiConsumer<String, String> listener) {
        executorNode.setStepStatusListener(listener);
    }

    private Map<String, Object> baseInitial(String userInput, EnterpriseIdentity identity,
            boolean approved, ExecutionPlan preApprovedPlan) {
        Map<String, Object> initial = new LinkedHashMap<>();
        initial.put("userQuery", userInput == null ? "" : userInput);
        initial.put("enterpriseUid", identity.enterpriseUid());
        initial.put("engagementId", identity.engagementId());
        initial.put("messages", List.of());
        initial.put("approved", approved);
        if (preApprovedPlan != null) {
            initial.put("activePlan", preApprovedPlan);
        }
        initial.put("legacyContext", new LinkedHashMap<String, Object>());
        initial.put("iterationCount", 0);
        initial.put("guardrailReason", "");
        initial.put("abortReason", "");
        return initial;
    }

    private AgentResponse runGraph(Map<String, Object> initial, EnterpriseIdentity identity) {
        List<String> trace = new ArrayList<>();
        OrchestratorState finalState = null;
        try {
            AsyncGenerator<NodeOutput<OrchestratorState>> generator = graph.stream(initial);
            for (NodeOutput<OrchestratorState> output : generator) {
                trace.add(output.node());
                finalState = output.state();
            }
        } catch (Exception ex) {
            LOG.error("Erreur d'orchestration", ex);
            return AgentResponse.failed("Erreur d'orchestration: " + ex.getMessage(),
                    finalState == null ? Map.of() : finalState.legacyContext(),
                    iterationsOf(finalState), identity.enterpriseUid(), identity.engagementId(),
                    finalState == null ? Map.of() : finalState.stepStatuses());
        }
        if (finalState == null) {
            return AgentResponse.failed("Le graphe n'a produit aucun etat final.", Map.of(), 0,
                    identity.enterpriseUid(), identity.engagementId(), Map.of());
        }
        LOG.info("Chemin du graphe: {} (identite {})", trace, identity.attribution());
        return toResponse(finalState);
    }

    private AgentResponse toResponse(OrchestratorState state) {
        java.util.Optional<com.endeleya.kazisafex.agent.graph.StructuredAnswer> structured =
                state.finalStructuredOutput();
        if (structured.isPresent()) {
            return AgentResponse.success(structured.get(), state.legacyContext(),
                    state.iterationCount(), state.enterpriseUid(), state.engagementId(),
                    state.stepStatuses());
        }
        if (!state.guardrailReason().isBlank()) {
            return AgentResponse.blocked(state.guardrailReason(),
                    state.enterpriseUid(), state.engagementId());
        }
        String reason = state.abortReason().isBlank()
                ? "Execution interrompue sans raison explicite."
                : state.abortReason();
        return AgentResponse.failed(reason, state.legacyContext(),
                state.iterationCount(), state.enterpriseUid(), state.engagementId(),
                state.stepStatuses());
    }

    /** Convenience getter for tests/telemetry. */
    public CompiledGraph<OrchestratorState> graph() {
        return graph;
    }

    private int iterationsOf(OrchestratorState state) {
        return state == null ? 0 : state.iterationCount();
    }
}