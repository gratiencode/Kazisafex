package com.endeleya.kazisafex.agent.graph;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serializable chat-transcript entry stored in the graph state.
 *
 * <p>LangGraph4j snapshots the state between nodes with Java serialization; the
 * LangChain4j {@code ChatMessage} objects are not {@link Serializable} and would
 * break the flow. This lightweight record carries the same semantic (role + content)
 * while keeping the whole {@link OrchestratorState} graph-safe.</p>
 *
 * @param role    {@code "user"} or {@code "system"}
 * @param content message content
 */
public record TranscriptEntry(String role, String content) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** User-side transcript entry. */
    public static TranscriptEntry user(String content) {
        return new TranscriptEntry("user", content);
    }

    /** System-side transcript entry. */
    public static TranscriptEntry system(String content) {
        return new TranscriptEntry("system", content);
    }
}