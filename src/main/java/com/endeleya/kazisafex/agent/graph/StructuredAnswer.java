package com.endeleya.kazisafex.agent.graph;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Strongly-typed structured answer produced by the Formatter node.
 *
 * @param summary   one-line synthesis of what was done
 * @param answer    detailed human-readable answer (Markdown allowed)
 * @param data      normalized payload extracted from the legacy outputs
 * @param warnings  anomalies observed during execution
 */
public record StructuredAnswer(String summary, String answer, Map<String, Object> data, List<String> warnings)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Builds a structured answer without warnings.
     *
     * @param summary one-line synthesis
     * @param answer  detailed answer
     * @param data    normalized payload
     * @return the structured answer
     */
    public static StructuredAnswer of(String summary, String answer, Map<String, Object> data) {
        return new StructuredAnswer(summary, answer, data, List.of());
    }
}