package com.endeleya.kazisafex.agent;

import com.endeleya.kazisafex.agent.legacy.LegacyAgentFacade;
import com.endeleya.kazisafex.agent.legacy.LegacyToolDescriptor;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge between the legacy agent and LangChain4j's dynamic tool execution.
 *
 * <p>This adapter proves the integration pattern in two complementary ways:</p>
 * <ol>
 *   <li><b>Annotated legacy objects</b> (e.g. {@code com.endeleya.ia.GratienTools}, which
 *       already carries {@code @Tool}/{@code @P} annotations): the methods are turned into
 *       {@link ToolSpecification}s and {@link DefaultToolExecutor} is reused verbatim —
 *       the legacy class is never modified.</li>
 *   <li><b>Bare legacy workflows</b> (unannotated, e.g.
 *       {@code LegacyDatabaseSearchWorkflow.run}): a descriptor
 *       ({@link LegacyToolDescriptor}) is translated into a {@link ToolSpecification}
 *       (JSON schema) and a {@link ToolExecutor} that resolves the arguments, runs them
 *       through {@link ResilientLegacyInvoker} on virtual threads and returns the
 *       raw payload as a tool result.</li>
 * </ol>
 *
 * <p>The result is a standard {@code Map<ToolSpecification, ToolExecutor>} that can be
 * handed directly to {@code AiServices.tools(...)} or exposed lazily through a
 * {@link dev.langchain4j.service.tool.ToolProvider}.</p>
 */
public final class LegacyToolAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyToolAdapter.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper JSON =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private final LegacyAgentFacade facade;
    private final ResilientLegacyInvoker invoker;

    /**
     * Creates the adapter tied to a legacy facade and its resilience guard.
     *
     * @param facade source of legacy capabilities
     * @param invoker protective layer around legacy calls
     */
    public LegacyToolAdapter(LegacyAgentFacade facade, ResilientLegacyInvoker invoker) {
        this.facade = facade;
        this.invoker = invoker;
    }

    /**
     * Adapts a legacy object whose methods are already annotated with
     * {@code dev.langchain4j.agent.tool.Tool}. Nothing of the legacy class is changed.
     *
     * @param legacy annotated legacy instance
     * @return LangChain4j tool mappings, never null
     */
    public static Map<ToolSpecification, ToolExecutor> adaptAnnotatedLegacy(Object legacy) {
        Map<ToolSpecification, ToolExecutor> tools = new LinkedHashMap<>();
        for (Method method : legacy.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Tool.class)) {
                continue;
            }
            ToolSpecification spec = ToolSpecifications.toolSpecificationFrom(method);
            ToolExecutor executor = new DefaultToolExecutor(legacy, method);
            tools.put(spec, executor);
        }
        LOG.info("Adaptes {} outils annotes depuis {}", tools.size(), legacy.getClass().getSimpleName());
        return tools;
    }

    /**
     * Adapts a specific annotated method of a legacy object (single dispatch).
     *
     * @param legacy annotated legacy instance
     * @param methodName method name
     * @return the tool mapping, or empty if the method is not a tool
     */
    public Optional<Map.Entry<ToolSpecification, ToolExecutor>> adaptAnnotatedMethod(Object legacy, String methodName) {
        for (Method method : legacy.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.isAnnotationPresent(Tool.class)) {
                Map.Entry<ToolSpecification, ToolExecutor> entry = Map.entry(
                        ToolSpecifications.toolSpecificationFrom(method),
                        new DefaultToolExecutor(legacy, method));
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /**
     * Adapts every capability discovered on the facade into dynamic LangChain4j tools.
     *
     * @return LangChain4j tool mappings, never null
     */
    public Map<ToolSpecification, ToolExecutor> adaptDiscoveredLegacyTools() {
        Map<ToolSpecification, ToolExecutor> tools = new LinkedHashMap<>();
        for (LegacyToolDescriptor descriptor : facade.discover()) {
            tools.put(toToolSpecification(descriptor), toToolExecutor(descriptor));
        }
        return tools;
    }

    /**
     * Builds a single dynamic tool mapping for one legacy capability.
     *
     * @param descriptor the legacy capability
     * @return LangChain4j tool mapping
     */
    public Map.Entry<ToolSpecification, ToolExecutor> adapt(LegacyToolDescriptor descriptor) {
        return Map.entry(toToolSpecification(descriptor), toToolExecutor(descriptor));
    }

    /**
     * Resolves a named legacy capability as a tool mapping.
     *
     * @param toolName canonical tool name
     * @return the mapping, or empty when unknown
     */
    public Optional<Map.Entry<ToolSpecification, ToolExecutor>> adapt(String toolName) {
        return facade.describe(toolName).map(this::adapt);
    }

    private ToolSpecification toToolSpecification(LegacyToolDescriptor descriptor) {
        return ToolSpecification.builder()
                .name(descriptor.name())
                .description(descriptor.description())
                .parameters(descriptor.parameters())
                .build();
    }

    private ToolExecutor toToolExecutor(LegacyToolDescriptor descriptor) {
        return (ToolExecutionRequest request, Object memoryId) -> {
            Map<String, Object> arguments = parseArguments(request);
            LOG.info("Execution legacy '{}' arguments={} (thread {})",
                    request.name(), arguments, Thread.currentThread().getName());
            String raw = String.valueOf(invoker.invoke(descriptor, arguments));
            if (memoryId != null) {
                LOG.debug("Memoire de conversation ciblee: {}", memoryId);
            }
            return raw;
        };
    }

    private Map<String, Object> parseArguments(ToolExecutionRequest request) {
        if (request.arguments() == null || request.arguments().isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> parsed = JSON.readValue(
                    request.arguments(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            return parsed == null ? Map.of() : parsed;
        } catch (Exception ex) {
            LOG.warn("Arguments JSON invalides pour '{}': {}", request.name(), ex.getMessage());
            return Map.of();
        }
    }

    /**
     * Convenience: executes a tool request and returns a standard tool result message,
     * handy when replaying tool calls without an AiServices loop.
     *
     * @param toolName canonical tool name
     * @param arguments raw arguments
     * @return a LangChain4j {@link ToolExecutionResultMessage}
     */
    public Optional<ToolExecutionResultMessage> replayToolCall(String toolName, String arguments) {
        return adapt(toolName).map(entry -> {
            ToolExecutionRequest request = ToolExecutionRequest.builder()
                    .id("legacy-" + toolName)
                    .name(toolName)
                    .arguments(arguments)
                    .build();
            String result = entry.getValue().execute(request, null);
            return ToolExecutionResultMessage.from(request, result);
        });
    }

    /**
     * Builds the list view of all adapted tool specifications (for logging/UI).
     *
     * @return immutable list of the specifications
     */
    public List<ToolSpecification> specs() {
        List<ToolSpecification> specs = new ArrayList<>();
        for (LegacyToolDescriptor descriptor : facade.discover()) {
            specs.add(toToolSpecification(descriptor));
        }
        return List.copyOf(specs);
    }
}