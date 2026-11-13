package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.JsonSupport;
import com.endeleya.kazisafex.agent.legacy.LegacyAgentFacade;
import com.endeleya.kazisafex.agent.legacy.LegacyToolDescriptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Second node: builds the ordered {@link ExecutionPlan}.
 *
 * <p>Two strategies are supported:
 * <ul>
 *   <li><b>LLM planner</b> ({@link AgentAssistants.PlanningAgent}): maps the query over
 *       the discovered capability list and returns a structured plan;</li>
 *   <li><b>deterministic router</b> ({@link LegacyCapabilityRouter}): keyword mapping,
 *       used when no {@code ChatModel} is attached or when the LLM is too slow.</li>
 * </ul>
 * </p>
 */
public final class PlannerNode implements NodeAction<OrchestratorState> {

    private static final Logger LOG = LoggerFactory.getLogger(PlannerNode.class);

    /** Deadline d'une planification LLM: au-delà, repli deterministe tant attendu. */
    static final long LLM_TIMEOUT_MILLIS = 60_000L;

    /**
     * Retry timeout for the direct LLM plan re-call when the AiServices proxy parse
     * fails (Markdown fences or bad JSON). Half of the main deadline to cap the total
     * wall-clock to ~90 s.
     */
    static final long RETRY_TIMEOUT_MILLIS = 30_000L;

    /**
     * Reduction du catalogue transmis au modele. Le catalogue legacy reel contient des
     * dizaines d'outils tres verbaux : l'envoyer integralement allonge la geNeration et
     * peut faire « refléchir » le chatbot sans fin. On ne transmet que les capacites les
     * plus pertinentes par rapport aux termes de la demande.
     */
    private static final int CAPABILITY_LIMIT = 15;

    private static final ObjectMapper PLAN_JSON = new ObjectMapper();

    private final LegacyAgentFacade facade;
    private final AgentAssistants.Assistants assistants;
    private final LegacyCapabilityRouter router;
    private final ExecutorService plannerExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Creates the planner.
     *
     * @param facade     legacy capability source
     * @param assistants AiServices proxies (may be disabled)
     */
    public PlannerNode(LegacyAgentFacade facade, AgentAssistants.Assistants assistants) {
        this(facade, assistants, new LegacyCapabilityRouter());
    }

    /**
     * Creates the planner with a custom router.
     *
     * @param facade     legacy capability source
     * @param assistants AiServices proxies (may be disabled)
     * @param router     deterministic router fallback
     */
    public PlannerNode(LegacyAgentFacade facade, AgentAssistants.Assistants assistants, LegacyCapabilityRouter router) {
        this.facade = facade;
        this.assistants = assistants;
        this.router = router;
    }

    @Override
    public Map<String, Object> apply(OrchestratorState state) {
        String query = state.userQuery();
        ExecutionPlan plan;
        if (assistants.enabled()) {
            try {
                plan = planFromLlm(query);
            } catch (Exception ex) {
                LOG.warn("Planifieur LLM indisponible ou trop lent, repli deterministe ({}: {})",
                        ex.getClass().getSimpleName(), ex.getMessage());
                plan = null;
            }
            if (plan == null || !plan.hasSteps()) {
                LOG.warn("Planifieur LLM sans etape exploitable, repli deterministe ('{}')", query);
                plan = resolveFallback(query);
            }
        } else {
            plan = resolveFallback(query);
        }
        LOG.info("Planificateur -> {}", plan.summary());

        return Map.of(
                "activePlan", plan,
                "messages", List.of(TranscriptEntry.system("Plan: " + plan.summary())));
    }

    /**
     * Repli deterministe: routeur a mots-cles, puis, si le routeur ne trouve aucune
     * capacite (noms d'outils inconnus), plan de secours construit a partir des
     * capacites les plus proches de la demande et dont les parametres obligatoires
     * sont vides, afin qu'une liste d'etapes soit toujours presentee a l'approbation
     * plutot qu'un echec sans action.
     */
    private ExecutionPlan resolveFallback(String query) {
        ExecutionPlan routed = router.route(query, facade.discover());
        if (routed.hasSteps()) {
            return routed;
        }
        return capabilityFallbackPlan(query);
    }

    /**
     * Plan de derniere intention: les capacites du catalogue dont les termes croisent
     * la demande (pertinence {@code > 0}), ordonnees par pertinence decroissante, et
     * dont le schema ne possede aucun parametre obligatoire (appel possible avec
     * {@code {}}). L'execution produira alors un statut par etape plutot qu'une
     * reponse {@code "aucune action"} inexploitable.
     */
    private ExecutionPlan capabilityFallbackPlan(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<PlanStep> steps = facade.discover().stream()
                .sorted(Comparator
                        .comparingDouble((LegacyToolDescriptor tool) -> relevance(tool, q))
                        .reversed())
                .filter(tool -> q.isBlank() || relevance(tool, q) > 0)
                .filter(PlannerNode::noRequiredParameters)
                .limit(8)
                .map(tool -> PlanStep.of(tool.name(), tool.description(), Map.of()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (steps.isEmpty()) {
            return ExecutionPlan.empty();
        }
        return new ExecutionPlan(List.copyOf(steps),
                "Plan automatique etabli a partir des capacites les plus proches de la demande "
                        + "(plan non detaille : arguments a completer pendant l'execution).");
    }

    private static boolean noRequiredParameters(LegacyToolDescriptor tool) {
        JsonObjectSchema schema = tool.parameters();
        return schema == null || schema.required() == null || schema.required().isEmpty();
    }

    // -------------------------------------------------------------------------
    // LLM planning: proxy + retentative directe en cas de parsing fence
    // -------------------------------------------------------------------------

    private ExecutionPlan planFromLlm(String query) {
        String capabilities = topCapabilities(query);
        AgentAssistants.PlanDraft draft = planFromProxy(capabilities, query);
        if (draft == null) {
            draft = directPlanRetry(capabilities, query);
        }
        List<PlanStep> steps = draft == null || draft.steps() == null
                ? List.of()
                : draft.steps().stream()
                        .map(this::toStep)
                        .collect(Collectors.toCollection(ArrayList::new));
        if (steps.isEmpty()) {
            return ExecutionPlan.empty();
        }
        return new ExecutionPlan(List.copyOf(steps),
                draft.reasoning() == null ? "Plan genere par la modele." : draft.reasoning());
    }

    private AgentAssistants.PlanDraft planFromProxy(String capabilities, String query) {
        CompletableFuture<AgentAssistants.PlanDraft> planning =
                CompletableFuture.supplyAsync(
                        () -> assistants.planner().plan(capabilities, query), plannerExecutor);
        try {
            return planning.get(LLM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            throw new IllegalStateException(
                    "Planifieur LLM trop lent (>" + LLM_TIMEOUT_MILLIS + " ms)", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Planification interrompue", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof RuntimeException) {
                LOG.warn("Plan LLM non parse par le proxy ({}), retentative directe",
                        cause.getClass().getSimpleName());
                return null;
            }
            throw new IllegalStateException("Echec du planifieur LLM", cause);
        }
    }

    /**
     * Retentative en direct sur le modele avec extraction manuelle du JSON: résout les
     * réponses entourées de balises Markdown (ou bruitées) que le proxy AiServices
     * refuse de parser.
     */
    private AgentAssistants.PlanDraft directPlanRetry(String capabilities, String query) {
        try {
            ChatResponse response = CompletableFuture.supplyAsync(
                    () -> assistants.model().chat(ChatRequest.builder()
                            .messages(List.of(
                                    SystemMessage.from(AgentAssistants.PLANNER_SYSTEM),
                                    UserMessage.from("Capacités legacy disponibles:\n"
                                            + capabilities
                                            + "\n\nDemande utilisateur:\n" + query)))
                            .build()),
                    plannerExecutor)
                    .get(RETRY_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            AiMessage message = response == null ? null : response.aiMessage();
            return parsePlanDraft(message == null ? null : message.text());
        } catch (TimeoutException ex) {
            LOG.warn("Retentitive plan LLM trop lente");
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception ex) {
            LOG.warn("Retentitive plan LLM en echec", ex);
            return null;
        }
    }

    /**
     * Extraction tolérante d'un plan depuis la réponse brute du modèle (JSON potentiellement
     * entouré de balises Markdown, ou noyé dans du texte).
     */
    private AgentAssistants.PlanDraft parsePlanDraft(String text) {
        String json = JsonSupport.rawJsonObject(text);
        if (json == null) {
            return null;
        }
        try {
            JsonNode root = PLAN_JSON.readTree(json);
            JsonNode stepsNode = root.get("steps");
            if (stepsNode == null || !stepsNode.isArray()) {
                return null;
            }
            List<AgentAssistants.PlanDraft.StepDraft> steps = new ArrayList<>();
            for (JsonNode s : stepsNode) {
                if (s == null || !s.isObject()) {
                    continue;
                }
                steps.add(new AgentAssistants.PlanDraft.StepDraft(
                        textField(s, "toolName"),
                        textField(s, "description"),
                        textField(s, "argumentsJson")));
            }
            String reasoning = textField(root, "reasoning");
            return new AgentAssistants.PlanDraft(steps,
                    reasoning == null ? "Plan genere par la modele." : reasoning);
        } catch (Exception ex) {
            LOG.warn("Plan LLM illisible lors de la retentitive directe: {}", ex.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Catalogue + pertinence
    // -------------------------------------------------------------------------

    /**
     * Catalog des capacites pertinentes uniquement: les termes de la demande (au moins
     * 3 caracteres) sont mis en correspondance avec le nom+description de chaque outil;
     * on garde les {@link #CAPABILITY_LIMIT} meilleures. A defaut de termes, les
     * N premieres capacites sont transmises.
     */
    private String topCapabilities(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return facade.discover().stream()
                .sorted(Comparator
                        .comparingDouble((LegacyToolDescriptor tool) -> relevance(tool, q))
                        .reversed())
                .limit(CAPABILITY_LIMIT)
                .map(LegacyToolDescriptor::summary)
                .collect(Collectors.joining("\n"));
    }

    private double relevance(LegacyToolDescriptor tool, String q) {
        if (q.isBlank()) {
            return 1.0;
        }
        // Repli des accents des deux côtés : « recquisitions » doit correspondre à
        // « réquisitions », « eliminer » à « éliminer », etc.
        String hay = accentFold(tool.name() + ' ' + tool.description());
        return Arrays.stream(accentFold(q).split("\\W+"))
                .filter(token -> token.length() >= 3)
                .filter(hay::contains)
                .count();
    }

    /** Normalise un texte pour la recherche : minuscules et accents retirés. */
    private static String accentFold(String text) {
        if (text == null) {
            return "";
        }
        String lowered = text.toLowerCase(Locale.ROOT);
        return java.text.Normalizer.normalize(lowered, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PlanStep toStep(AgentAssistants.PlanDraft.StepDraft draft) {
        return PlanStep.of(
                draft.toolName(),
                draft.description(),
                JsonSupport.parseArguments(draft.argumentsJson()));
    }

    private static String textField(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value != null && value.isTextual() ? value.asText() : null;
    }
}