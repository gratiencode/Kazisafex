package com.endeleya.kazisafex.agent.legacy;

import com.endeleya.ia.AiAgents;
import com.endeleya.ia.GratienSwarmTools;
import com.endeleya.ia.GratienTools;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Production adapter over the actual {@code com.endeleya.ia} legacy agent runtime.
 *
 * <p>It reflectively discovers every {@code @Tool}-annotated method of
 * {@link com.endeleya.ia.GratienTools} and exposes each one as a
 * {@link LegacyToolDescriptor}. The legacy class itself is never modified: the
 * adapter only reads its annotations and reuses LangChain4j's
 * {@link DefaultToolExecutor} to dispatch calls.</p>
 *
 * <p>If the real legacy context (Hibernate session, Redis, Ollama...) is unavailable,
 * the facade degrades gracefully to the {@link MockLegacyAgentFacade}, so the master
 * agent can still answer with its deterministic catalogue.</p>
 */
public final class KazisafeLegacyFacade implements LegacyAgentFacade {

    private static final Logger LOG = LoggerFactory.getLogger(KazisafeLegacyFacade.class);

    private final LegacyAgentFacade delegate;

    /**
     * Creates the adapter, resolving the legacy {@code GratienTools} instance.
     */
    public KazisafeLegacyFacade() {
        this(true);
    }

    KazisafeLegacyFacade(boolean requireLegacyRuntime) {
        if (requireLegacyRuntime) {
            LegacyAgentFacade built = buildFromLegacyRuntime();
            delegate = built == null ? new MockLegacyAgentFacade() : built;
        } else {
            delegate = new MockLegacyAgentFacade();
        }
    }

    private static LegacyAgentFacade buildFromLegacyRuntime() {
        try {
            AiAgents agents = AiAgents.getInstance();
            GratienTools tools = agents.getGratienTools();
            if (tools == null) {
                return null;
            }
            List<LegacyToolDescriptor> descriptors = new ArrayList<>();
            // Les outils @Tool de GratienTools ET le lanceur de workflows swarm
            // (GratienSwarmTools.delegateTask) sont exposés à l'orchestrateur : les
            // workflows complets (invoice/sale/expense) deviennent planifiables.
            collectDescriptors(tools, descriptors);
            collectDescriptors(new GratienSwarmTools(), descriptors);
            if (descriptors.isEmpty()) {
                return null;
            }
            return new StaticDescriptorFacade(descriptors);
        } catch (Throwable ex) {
            LOG.warn("Le runtime legacy est indisponible, passage en mode catalogue demo", ex);
            return null;
        }
    }

    /**
     * Outils réservés au chemin classique du chat : leur approbation interactive
     * ({@code planExecution} puis {@code answerPlanExecution}) est remplacée par le
     * dialogue d'approbation de l'orchestrateur. Les inclure dans le catalogue
     * produirait une double approbation et une fuite de nom technique d'outil.
     */
    public static final Set<String> CHAT_ONLY_TOOLS = Set.of("planExecution", "answerPlanExecution");

    /**
     * Collecte les capacités {@code @Tool} publiques de l'instance (GratienTools ou
     * GratienSwarmTools), en retirant les outils réservés au chat classique.
     *
     * @param instance instance dont les méthodes {@code @Tool} sont découvertes
     * @param out      liste de destination des capacités
     */
    public static void collectDescriptors(Object instance, List<LegacyToolDescriptor> out) {
        for (Method method : instance.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Tool.class)) {
                continue;
            }
            if (CHAT_ONLY_TOOLS.contains(method.getName())) {
                LOG.info("Capacité réservée au chat classique exclue de l'orchestrateur: {}",
                        method.getName());
                continue;
            }
            out.add(toDescriptor(instance, method));
        }
    }

    private static LegacyToolDescriptor toDescriptor(Object instance, Method method) {
        ToolSpecification spec = ToolSpecifications.toolSpecificationFrom(method);
        DefaultToolExecutor executor = new DefaultToolExecutor(instance, method);
        return new LegacyToolDescriptor(
                spec.name(),
                spec.description(),
                spec.parameters(),
                args -> {
                    // L'approbation humaine du plan est détenue par l'orchestrateur
                    // (dialogue). On lève donc la garde legacy « confirmer ce plan avant
                    // fusion/suppression » pendant l'invocation, au seul thread
                    // d'exécution du moteur (jamais sur le chemin classique).
                    GratienTools.markOrchestratorApproved();
                    try {
                        return executor.execute(request(spec.name(), args), null);
                    } finally {
                        GratienTools.clearOrchestratorApproved();
                    }
                });
    }

    private static ToolExecutionRequest request(String name, Map<String, Object> args) {
        return ToolExecutionRequest.builder()
                .id("legacy-" + name)
                .name(name)
                .arguments(toJson(args))
                .build();
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private static String toJson(Map<String, Object> args) {
        try {
            return JSON.writeValueAsString(args == null ? Map.of() : args);
        } catch (Exception ex) {
            return "{}";
        }
    }

    @Override
    public List<LegacyToolDescriptor> discover() {
        return delegate.discover();
    }

    @Override
    public Optional<LegacyToolDescriptor> describe(String toolName) {
        return delegate.describe(toolName);
    }

    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws LegacyExecutionException {
        return delegate.execute(toolName, arguments);
    }

    /**
     * Minimal facade over a fixed set of descriptors resolved once.
     */
    private static final class StaticDescriptorFacade implements LegacyAgentFacade {

        private final List<LegacyToolDescriptor> descriptors;

        private StaticDescriptorFacade(List<LegacyToolDescriptor> descriptors) {
            this.descriptors = List.copyOf(descriptors);
        }

        @Override
        public List<LegacyToolDescriptor> discover() {
            return descriptors;
        }

        @Override
        public Optional<LegacyToolDescriptor> describe(String toolName) {
            return descriptors.stream()
                    .filter(t -> t.name().equalsIgnoreCase(toolName))
                    .findFirst();
        }

        @Override
        public Object execute(String toolName, Map<String, Object> arguments) throws LegacyExecutionException {
            LegacyToolDescriptor tool = describe(toolName)
                    .orElseThrow(() -> new LegacyExecutionException("Capability inconnue: " + toolName));
            try {
                return tool.invocation().apply(arguments);
            } catch (Exception ex) {
                throw new LegacyExecutionException("Echec du workflow legacy '" + toolName + "'", ex);
            }
        }
    }
}