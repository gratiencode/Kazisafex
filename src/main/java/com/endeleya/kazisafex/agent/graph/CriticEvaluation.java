package com.endeleya.kazisafex.agent.graph;

import java.io.Serial;
import java.io.Serializable;

/**
 * Output of the Critic node for a given execution attempt.
 *
 * @param verdict  decision taken on the legacy results
 * @param feedback detailed feedback usable to re-plan or revise
 * @param score    0..100 quality score assigned by the critic
 * @param attempt  execution attempt number this evaluation refers to
 */
public record CriticEvaluation(CriticVerdict verdict, String feedback, int score, int attempt)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Neutral pre-execution evaluation. */
    public static CriticEvaluation unknown() {
        return new CriticEvaluation(CriticVerdict.REVISE, "Execution non encore evaluee.", 0, 0);
    }

    /**
     * Verdicts emitted by the critic after inspecting legacy results.
     */
    public enum CriticVerdict {
        /** The complete result set is sufficient; go to the formatter. */
        COMPLETE,
        /** Re-execute the same plan with adjusted parameters. */
        RETRY,
        /** The plan itself must be revised (different tools/arguments). */
        REVISE,
        /** Give up: the plan cannot be completed (échecs systématiques). */
        ABORT
    }
}