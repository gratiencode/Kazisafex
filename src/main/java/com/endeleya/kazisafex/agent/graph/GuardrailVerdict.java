package com.endeleya.kazisafex.agent.graph;

/**
 * Result of the deterministic guardrail inspection of a user query.
 *
 * @param allowed {@code true} when the query may reach the LLM and legacy tools
 * @param reason  human-readable justification (blocking reason when not allowed)
 */
public record GuardrailVerdict(boolean allowed, String reason) {

    /** Allows the query. */
    public static GuardrailVerdict pass(String reason) {
        return new GuardrailVerdict(true, reason);
    }

    /** Rejects the query. */
    public static GuardrailVerdict block(String reason) {
        return new GuardrailVerdict(false, reason);
    }
}