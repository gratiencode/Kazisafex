package com.endeleya.kazisafex.agent;

import com.endeleya.kazisafex.agent.legacy.LegacyToolDescriptor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lazy {@link ToolProvider} exposing the legacy capabilities to an AiServices-driven
 * agent this is the discovery path required so the LLM can plan tool calls over
 * capabilities it did not know at static-annotation time.
 *
 * <p>The provider is re-evaluated for each user turn: the current user message could
 * be used later to filter the visible tools (routing guard), keeping the tool surface
 * small for the model.</p>
 */
public final class LegacyToolProvider implements ToolProvider {

    private final LegacyToolAdapter adapter;

    /**
     * Creates the provider.
     *
     * @param adapter bridge producing {@code ToolSpecification}/{@code ToolExecutor}
     */
    public LegacyToolProvider(LegacyToolAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest request) {
        Map<dev.langchain4j.agent.tool.ToolSpecification, dev.langchain4j.service.tool.ToolExecutor> tools =
                new LinkedHashMap<>(adapter.adaptDiscoveredLegacyTools());
        return ToolProviderResult.builder().addAll(tools).build();
    }

    /** Convenience for logging / tests. */
    public int visibleToolCount() {
        return adapter.specs().size();
    }
}