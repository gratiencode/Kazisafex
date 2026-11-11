package com.endeleya.kazisafex.agent.graph;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Step of the execution plan, targeting a legacy capability.
 *
 * @param id          unique step identifier inside the plan
 * @param toolName    canonical name of the legacy tool to invoke
 * @param description human-readable purpose of the step
 * @param arguments   resolved or proposed arguments for the invocation
 */
public record PlanStep(
        String id,
        String toolName,
        String description,
        java.util.Map<String, Object> arguments) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Builds the step.
     *
     * @param toolName the legacy capability to invoke
     * @param description purpose of the step
     * @param arguments proposed arguments
     * @return a new step with a generated identifier
     */
    public static PlanStep of(String toolName, String description, java.util.Map<String, Object> arguments) {
        int hash = toolName == null ? 0 : toolName.hashCode();
        return new PlanStep("step-" + Integer.toUnsignedString(hash)
                        + "-" + System.nanoTime() % 100_000,
                toolName, description, arguments == null ? java.util.Map.of() : arguments);
    }

    /**
     * Libellé présentable d'une étape: la description humaine quand elle existe, sinon
     * l'identifiant technique en dernier recours. Utilisé comme clé du todo list
     * ({@code stepStatuses}) pour que l'interface n'expose jamais le nom d'outil.
     *
     * @param step the plan step
     * @return the human-facing label of the step
     */
    public static String label(PlanStep step) {
        return step.description() != null && !step.description().isBlank()
                ? step.description()
                : step.id();
    }
}