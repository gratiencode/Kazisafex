package com.endeleya.kazisafex.agent.graph;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Ordered execution plan produced by the Planner node.
 *
 * @param steps     the sequence of legacy calls to perform
 * @param reasoning short justification of the mapping done by the planner
 */
public record ExecutionPlan(List<PlanStep> steps, String reasoning) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Empty plan marker used when the planner finds nothing to do. */
    public static ExecutionPlan empty() {
        return new ExecutionPlan(List.of(), "Aucune capacite legacy ne correspond a la demande.");
    }

    /** Whether the plan contains at least one step. */
    public boolean hasSteps() {
        return steps != null && !steps.isEmpty();
    }

    /** Dense, one-line description for logs and state. */
    public String summary() {
        if (!hasSteps()) {
            return reasoning();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(steps.get(i).toolName());
        }
        return sb.toString();
    }
}