package com.endeleya.kazisafex.agent.legacy;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;

/**
 * Describes a legacy capability so that it can be discovered and invoked as a
 * LangChain4j tool without modifying the legacy implementation.
 *
 * @param name          canonical tool name used by the LLM
 * @param description   free-text description shown to the LLM
 * @param parameters    JSON-schema of the expected arguments
 * @param invocation    bridge to the unmodified legacy workflow
 */
public record LegacyToolDescriptor(
        String name,
        String description,
        JsonObjectSchema parameters,
        LegacyInvocation invocation) {

    /** Short, immutable view of the descriptor used for planning and logs. */
    public String summary() {
        return name + " -> " + description;
    }
}