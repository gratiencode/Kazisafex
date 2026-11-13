package com.endeleya.kazisafex.agent.legacy;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Facade over the pre-existing legacy agent tools and workflows.
 *
 * <p>Central to the adapter integration pattern: the orchestrator never talks to the
 * legacy classes directly, it only discovers and executes capabilities through this
 * seam. Production deployments supply an implementation bound to the actual
 * {@code com.endeleya.ia} runtime (see {@link KazisafeLegacyFacade}); tests and demos
 * use {@link MockLegacyAgentFacade}.</p>
 */
public interface LegacyAgentFacade {

    /**
     * Discovers every legacy workflow that can be exposed as a LangChain4j tool.
     *
     * @return the list of legacy capabilities
     */
    List<LegacyToolDescriptor> discover();

    /**
     * Looks up a single legacy capability by its tool name.
     *
     * @param toolName canonical tool name
     * @return the descriptor if the capability exists
     */
    Optional<LegacyToolDescriptor> describe(String toolName);

    /**
     * Executes a legacy capability with a flat argument map.
     *
     * @param toolName canonical tool name
     * @param arguments resolved tool arguments
     * @return the raw legacy result
     * @throws LegacyExecutionException if the workflow cannot be executed
     */
    Object execute(String toolName, Map<String, Object> arguments) throws LegacyExecutionException;
}