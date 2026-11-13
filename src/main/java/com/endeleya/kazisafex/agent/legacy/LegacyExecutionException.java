package com.endeleya.kazisafex.agent.legacy;

/**
 * Wraps any failure raised while delegating to a legacy workflow.
 *
 * <p>This checked exception gives the resilience layer a stable, non-polluted
 * boundary: every {@link LegacyInvocation} converts its underlying exception into
 * this type so that retry/circuit-breaker policies can react on it.</p>
 */
public class LegacyExecutionException extends Exception {

    /**
     * Creates a legacy execution failure.
     *
     * @param message descriptive message (tool name / step)
     * @param cause   root cause, if any
     */
    public LegacyExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a legacy execution failure without a root cause.
     *
     * @param message descriptive message
     */
    public LegacyExecutionException(String message) {
        super(message);
    }
}