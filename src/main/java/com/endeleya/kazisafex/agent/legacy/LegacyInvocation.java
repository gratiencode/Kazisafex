package com.endeleya.kazisafex.agent.legacy;

/**
 * Callable body of a legacy workflow, decoupled from LangChain4j.
 *
 * <p>This functional interface is the "bridge" contract: the legacy object remains
 * untouched and is merely referenced through an arrow function so the adapter layer
 * can expose it as a dynamic LangChain4j tool.</p>
 */
@FunctionalInterface
public interface LegacyInvocation {

    /**
     * Invokes the underlying legacy method.
     *
     * @param arguments raw argument map extracted from the tool call
     * @return the raw legacy result (text payload, DTO, collection...)
     * @throws Exception when the legacy workflow fails
     */
    Object apply(java.util.Map<String, Object> arguments) throws Exception;
}