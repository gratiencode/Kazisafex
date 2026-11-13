package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.ResilientLegacyInvoker;
import com.endeleya.kazisafex.agent.legacy.LegacyAgentFacade;
import com.endeleya.kazisafex.agent.legacy.LegacyToolDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Third node: executes the planned steps on the legacy agent.
 *
 * <p>Each step is a legacy capability resolved through the facade and invoked through
 * {@link ResilientLegacyInvoker}, guaranteeing virtual-thread dispatch, circuit
 * breaker, retry and null/timeout fallback. Raw outputs are stored in
 * {@code legacyContext} under the tool name, and the iteration counter is advanced so
 * the graph can never loop forever.</p>
 *
 * <p>Human-in-the-loop todo feedback: every step id is tracked in {@code stepStatuses}
 * ({@code pending} / {@code running} / {@code done} / {@code error}) and pushed to the
 * optional {@link #setStepStatusListener(BiConsumer)} so the UI can tick off tasks as
 * they complete while the plan executes.</p>
 */
public final class LegacyExecutorNode implements NodeAction<OrchestratorState> {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyExecutorNode.class);

    /**
     * Argument keys that would try to override the identity of the run. They are
     * never forwarded to the legacy layer: the database/session is derived from the
     * {@code eUid}/{@code ucontract} stored in the preferences at login, never from
     * the user text or the tool arguments.
     */
    private static final Set<String> IDENTITY_KEYS = Set.of(
            "euid", "entrepriseuid", "entrepriseid", "entr",
            "engagementid", "engagement", "ucontract", "contract", "contrat",
            "database", "db", "dbname", "datasource", "tenant", "schema",
            "sessionid", "instance", "server", "serveur");

    private final LegacyAgentFacade facade;
    private final ResilientLegacyInvoker invoker;
    private volatile BiConsumer<String, String> listener;

    /**
     * Creates the executor.
     *
     * @param facade  legacy capability source
     * @param invoker resilience + virtual-thread guard
     */
    public LegacyExecutorNode(LegacyAgentFacade facade, ResilientLegacyInvoker invoker) {
        this.facade = facade;
        this.invoker = invoker;
    }

    /**
     * Installs a per-step listener receiving {@code (stepId, status)} events as the
     * plan executes ({@code running}, then {@code done} or {@code error}).
     *
     * @param listener callback, {@code null} to detach
     */
    public void setStepStatusListener(BiConsumer<String, String> listener) {
        this.listener = listener;
    }

    @Override
    public Map<String, Object> apply(OrchestratorState state) {
        Map<String, Object> context = new LinkedHashMap<>(state.legacyContext());
        List<TranscriptEntry> logs = new ArrayList<>();
        Map<String, String> statuses = new LinkedHashMap<>(state.stepStatuses());
        int iteration = state.iterationCount() + 1;

        for (PlanStep step : state.activePlan().steps()) {
            LegacyToolDescriptor tool = facade.describe(step.toolName()).orElse(null);
            if (tool == null) {
                statuses.put(PlanStep.label(step), "error");
                publish(PlanStep.label(step), "error");
                String failure = "{\"error\":\"unknown-capability\",\"tool\":\""
                        + step.toolName() + "\"}";
                context.put(step.toolName(), failure);
                logs.add(TranscriptEntry.system("Etape " + step.id() + " [" + step.toolName()
                        + "]: capacite inconnue de la facade legacy."));
                continue;
            }
            statuses.put(PlanStep.label(step), "running");
            publish(PlanStep.label(step), "running");
            try {
                Object output = invoker.invoke(tool, neutralizeIdentityKeys(step.arguments()));
                context.put(step.toolName(), output);
                statuses.put(PlanStep.label(step), "done");
                publish(PlanStep.label(step), "done");
                logs.add(TranscriptEntry.system("Etape " + step.id() + " [" + step.toolName() + "]: " + output));
                LOG.info("Executeur legacy '{}' -> {}", step.toolName(), output);
            } catch (RuntimeException ex) {
                statuses.put(PlanStep.label(step), "error");
                publish(PlanStep.label(step), "error");
                context.put(step.toolName(), ex.getMessage());
                logs.add(TranscriptEntry.system("Etape " + step.id() + " [" + step.toolName()
                        + "]: echec (" + ex.getMessage() + ")"));
                LOG.error("Executeur legacy '{}' a echoue", step.toolName(), ex);
            }
        }
        context.put("iterations", iteration);
        String eUid = state.enterpriseUid();
        String contract = state.engagementId();
        if (!eUid.isBlank() || !contract.isBlank()) {
            logs.add(TranscriptEntry.system(
                    "Identite entreprise (eUid#contrat): " + eUid + "#" + contract));
        }

        return Map.of(
                "legacyContext", context,
                "iterationCount", iteration,
                "stepStatuses", statuses,
                "messages", logs);
    }

    /** status keys used in the todo feedback. */
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_ERROR = "error";

    private void publish(String stepId, String status) {
        BiConsumer<String, String> target = listener;
        if (target != null) {
            target.accept(stepId, status);
        }
    }

    /**
     * Drops any argument that would try to override the identity of the execution.
     *
     * @param arguments proposed tool arguments, possibly {@code null}
     * @return a clean copy without identity keys (never {@code null})
     */
    public static Map<String, Object> neutralizeIdentityKeys(Map<String, Object> arguments) {
        Map<String, Object> clean = new LinkedHashMap<>();
        if (arguments == null) {
            return clean;
        }
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            if (IDENTITY_KEYS.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                LOG.warn("Argument d'identite '{}' force dans l'appel ignore "
                        + "(isolation eUid/contrat).", entry.getKey());
                continue;
            }
            clean.put(entry.getKey(), entry.getValue());
        }
        return clean;
    }
}