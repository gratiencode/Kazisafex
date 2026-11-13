package com.endeleya.kazisafex.agent.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

/**
 * Typed, thread-safe graph state of the master orchestrator.
 *
 * <p>The state is an extension of LangGraph4j {@link AgentState}: every field is
 * backed by a {@link Channel} declared in {@link #SCHEMA}. List-based channels
 * (messages) use the appender reducer (append semantics); scalar channels use the
 * last-write-wins reducer provided by {@link Channels#base}. This keeps the graph
 * functional: no node ever mutates another node's data.</p>
 *
 * <p>Channels per the architecture:</p>
 * <ul>
 *   <li><b>userQuery</b>         original user input</li>
 *   <li><b>enterpriseUid</b>     enterprise identity (eUid) the session is confined to</li>
 *   <li><b>engagementId</b>      engagement/contract id the session is confined to</li>
 *   <li><b>messages</b>          LangChain4j chat history</li>
 *   <li><b>activePlan</b>        step-by-step plan targeting legacy tools</li>
 *   <li><b>approved</b>          human-in-the-loop flag: plan already approved, skip planner</li>
 *   <li><b>stepStatuses</b>      per-step todo feedback (pending/running/done/error)</li>
 *   <li><b>legacyContext</b>     raw outputs / execution state of the legacy agent</li>
 *   <li><b>iterationCount</b>    guard against infinite critic/planner loops</li>
 *   <li><b>criticEvaluation</b>  evaluation produced by the Critic node</li>
 *   <li><b>finalStructuredOutput</b> strongly-typed formatted answer</li>
 *   <li><b>abortReason</b>       reason when the graph aborts</li>
 *   <li><b>guardrailReason</b>   reason when the guardrail intercepts</li>
 * </ul>
 */
public final class OrchestratorState extends AgentState {

    /** Channel schema backing the state, shared with the graph builder. */
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry("userQuery", Channels.<String>base((oldValue, newValue) -> newValue, () -> "")),
            Map.entry("enterpriseUid", Channels.<String>base((oldValue, newValue) -> newValue, () -> "")),
            Map.entry("engagementId", Channels.<String>base((oldValue, newValue) -> newValue, () -> "")),
            Map.entry("messages", Channels.<TranscriptEntry>appender(ArrayList::new)),
            Map.entry("activePlan", Channels.<ExecutionPlan>base((oldValue, newValue) -> newValue, ExecutionPlan::empty)),
            Map.entry("approved", Channels.<Boolean>base((oldValue, newValue) -> newValue, () -> false)),
            Map.entry("stepStatuses", Channels.<Map<String, String>>base((oldValue, newValue) -> newValue, LinkedHashMap::new)),
            Map.entry("legacyContext", Channels.<Map<String, Object>>base((oldValue, newValue) -> newValue, LinkedHashMap::new)),
            Map.entry("iterationCount", Channels.<Integer>base((oldValue, newValue) -> newValue, () -> 0)),
            Map.entry("criticEvaluation", Channels.<CriticEvaluation>base((oldValue, newValue) -> newValue, CriticEvaluation::unknown)),
            Map.entry("finalStructuredOutput", Channels.<StructuredAnswer>base((oldValue, newValue) -> newValue)),
            Map.entry("abortReason", Channels.<String>base((oldValue, newValue) -> newValue, () -> "")),
            Map.entry("guardrailReason", Channels.<String>base((oldValue, newValue) -> newValue, () -> "")));

    /**
     * Creates the state wrapping the raw graph data.
     *
     * @param initData initial channel values
     */
    public OrchestratorState(Map<String, Object> initData) {
        super(initData);
    }

    /** Original user query. */
    public String userQuery() {
        return value("userQuery", "");
    }

    /** Enterprise identity (eUid) the execution is confined to. */
    public String enterpriseUid() {
        return value("enterpriseUid", "");
    }

    /** Engagement/contract identifier the execution is confined to. */
    public String engagementId() {
        return value("engagementId", "");
    }

    /** Chat history (append-only transcript). */
    public List<TranscriptEntry> messages() {
        return value("messages", ArrayList::new);
    }

    /** Current execution plan. */
    public ExecutionPlan activePlan() {
        return value("activePlan", ExecutionPlan::empty);
    }

    /** Whether the plan was approved by a human ({@code true} skips the planner). */
    public boolean approved() {
        return value("approved", false);
    }

    /** Per-step todo statuses keyed by step id (pending/running/done/error). */
    public Map<String, String> stepStatuses() {
        return value("stepStatuses", LinkedHashMap::new);
    }

    /** Raw legacy execution context. */
    public Map<String, Object> legacyContext() {
        return value("legacyContext", LinkedHashMap::new);
    }

    /** Iteration budget consumed so far. */
    public int iterationCount() {
        return value("iterationCount", 0);
    }

    /** Latest critic evaluation. */
    public CriticEvaluation criticEvaluation() {
        return value("criticEvaluation", CriticEvaluation::unknown);
    }

    /** Structured answer once formatted. */
    public Optional<StructuredAnswer> finalStructuredOutput() {
        return Optional.ofNullable(value("finalStructuredOutput", (StructuredAnswer) null));
    }

    /** Abort reason when the graph terminates abnormally. */
    public String abortReason() {
        return value("abortReason", "");
    }

    /** Guardrail reason when the input is intercepted. */
    public String guardrailReason() {
        return value("guardrailReason", "");
    }

    /** Raw value of a single legacy output by tool name. */
    public Optional<Object> legacyOutput(String toolName) {
        return Optional.ofNullable(legacyContext().get(toolName));
    }
}