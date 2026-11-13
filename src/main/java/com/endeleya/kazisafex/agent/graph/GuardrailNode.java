package com.endeleya.kazisafex.agent.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First node of the graph: validates the user query before any LLM or legacy call.
 *
 * <p>On rejection it records the blocking reason in {@code guardrailReason}; the
 * conditional edge then routes to the Abort node. On success the input is appended to
 * the chat history and the graph advances to the Planner.</p>
 */
public final class GuardrailNode implements NodeAction<OrchestratorState> {

    private static final Logger LOG = LoggerFactory.getLogger(GuardrailNode.class);

    private final GuardrailEngine engine;

    /** Creates the node with the default guardrail engine. */
    public GuardrailNode() {
        this(new GuardrailEngine());
    }

    /**
     * Creates the node over a custom guardrail engine (testability).
     *
     * @param engine guardrail engine
     */
    public GuardrailNode(GuardrailEngine engine) {
        this.engine = engine;
    }

    @Override
    public Map<String, Object> apply(OrchestratorState state) {
        String query = state.userQuery();
        GuardrailVerdict verdict = engine.inspect(query);
        LOG.info("Guardrail: autorise={} ({})", verdict.allowed(), verdict.reason());

        Map<String, Object> update = new HashMap<>();
        if (!verdict.allowed()) {
            update.put("guardrailReason", verdict.reason());
        }
        update.put("messages", List.of(TranscriptEntry.user(query)));
        return update;
    }
}