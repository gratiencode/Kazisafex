package com.endeleya.kazisafex.agent;

import com.endeleya.kazisafex.agent.graph.AgentResponse;
import com.endeleya.kazisafex.agent.graph.ExecutionPlan;
import com.endeleya.kazisafex.agent.graph.PlanStep;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Chat-friendly façade over the {@link AgentEngine} used by the Gratien conversation.
 *
 * <p>It owns the routing decision ("does this user message ask for an approval-gated
 * execution?") and the rendering of the {@link AgentResponse} as plain markdown the
 * chat bubble understands:
 * <ul>
 *   <li>{@link AgentResponse.Status#PROPOSAL} renders the planned todo list
 *       ({@code - [ ] tool — description}) awaiting consent;</li>
 *   <li>{@link AgentResponse.Status#SUCCESS} renders the same todo list checked off
 *       ({@code - [x]}) followed by the structured answer;</li>
 *   <li>{@link AgentResponse.Status#BLOCKED}/{@link AgentResponse.Status#FAILED}
 *       render the guardrail/failure message.</li>
 * </ul></p>
 *
 * <p>Routing into planning mode works the same way regardless of the user language.
 * The detection is staged so ordinary messages stay free of network calls:
 * <ul>
 *   <li><b>lexical</b> (zero cost): command prefix, explicit plan-then-approve markers,
 *       and multi-step chained connectors in French, English, Spanish and Portuguese
 *       ({@code d'abord... puis... ensuite...}, {@code first... then... afterwards...},
 *       {@code primero... luego... despues...}, {@code primeiro... depois... em seguida...});</li>
 *   <li><b>structural</b> (zero cost, language-agnostic): numbered steps
 *       ({@code 1. ... 2. ...}, {@code step 1}, {@code etape 1}...) and rich
 *       multi-clause requests with quantities;</li>
 *   <li><b>semantic</b> (optional, network): when a structural suspicion exists but no
 *       known connector matched, the attached {@link ChatModel} is asked a single
 *       yes/no question — this is what makes <i>any other language</i> work, not just
 *       the hardcoded connectors. It only runs when explicitly enabled via the
 *       {@link #OrchestrationChatAdapter(AgentEngine, ChatModel)} constructor.</li>
 * </ul></p>
 */
public final class OrchestrationChatAdapter {

    /** Minimum signal for the semantic fallback to be consulted. */
    private static final int SEMANTIC_MIN_LENGTH = 25;

    /** Deadline d'un classement semantique: au-delà, on reste sur le chemin classique. */
    private static final long SEMANTIC_TIMEOUT_MILLIS = 20_000L;

    private static final ExecutorService SEMANTIC_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    /** « puis » comme connecteur de suite d'étapes (hors forme interrogative « puis-je »). */
    private static final Pattern CONNECTOR_PUIS = Pattern.compile("\\bpuis\\b(?![\\s-]?je\\b)");

    /** Marqueurs d'amorce de séquence, multi-langues (d'abord / first / primero / primeiro...). */
    private static final Pattern[] FIRST_MARKERS = patterns(
            "d ?'?abord", "commence par", "commencez par", "tu commence",
            "tu commences", "premiere etape", "premierement", "on commence",
            "\\bfirst\\b", "\\bfirstly\\b", "start by", "begin by", "the first step",
            "step 1", "step one", "my first step",
            "\\bprimero\\b", "empieza por", "empiece por", "en primer lugar",
            "primera etapa", "primer paso", "primeramente",
            "\\bprimeiro\\b", "comece por", "em primeiro lugar", "primeira etapa", "primeiro passo");

    /** Marqueurs de poursuite de séquence, multi-langues (puis / then / luego / depois...). */
    private static final Pattern[] NEXT_MARKERS = patterns(
            "ensuite", "et ensuit", "en suite", "par apres", "finalement",
            "pour finir", "apres avoir", "enfin", "et etape", "ensuit",
            "\\bthen\\b", "\\bnext\\b", "and then", "\\bafterwards\\b", "after that",
            "\\bfinally\\b", "\\blastly\\b", "\\bsubsequently\\b",
            "\\bluego\\b", "\\bdespues\\b", "\\bentonces\\b", "a continuacion",
            "\\bfinalmente\\b", "por ultimo", "seguidamente", "\\btras\\b",
            "\\bdepois\\b", "em seguida", "\\bentao\\b", "a seguir", "por ultimo",
            "na sequencia");

    /** Étape numérotée agnostique à la langue, ex. {@code 1. ... 2. ...}. */
    private static final Pattern ENUMERATED_STEP = Pattern.compile("\\b\\d{1,2}[.)]");

    private final AgentEngine engine;
    private final ChatModel semanticModel;

    private static Pattern[] patterns(String... regexes) {
        Pattern[] out = new Pattern[regexes.length];
        for (int i = 0; i < regexes.length; i++) {
            out[i] = Pattern.compile(regexes[i]);
        }
        return out;
    }

    /**
     * Creates the adapter without the semantic fallback.
     *
     * @param engine the wired orchestrator ({@link OrchestrationRuntime#engine()} in
     *               production)
     */
    public OrchestrationChatAdapter(AgentEngine engine) {
        this(engine, null);
    }

    /**
     * Creates the adapter with an optional semantic (LLM) fallback.
     *
     * @param engine        the wired orchestrator
     * @param semanticModel optional chat model used only to classify structurally
     *                      suspicious requests when no connector matched (may be
     *                      {@code null})
     */
    public OrchestrationChatAdapter(AgentEngine engine, ChatModel semanticModel) {
        this.engine = engine;
        this.semanticModel = semanticModel;
    }

    /**
     * Whether the user query opts into the approval-gated (planning) flow.
     *
     * <p>Instance entry point used by the chat: lexical detection first, and only when
     * the message is structurally suspicious (multi-clause, quantities, numbered steps)
     * is the optional semantic classifier consulted. Never blocks on the caller thread
     * for a plain message.</p>
     *
     * @param userInput raw chat message
     * @return {@code true} when the message should be planned and shown for approval
     */
    public boolean wantsPlanning(String userInput) {
        if (lexicalApprovalRequested(userInput)) {
            return true;
        }
        return semanticSuspicious(userInput) && semanticWantsPlanning(userInput);
    }

    /**
     * Whether the user query opts into the approval-gated (planning) flow.
     *
     * <p>Lexical-only detection (French/English/Spanish/Portuguese connectors, prefixes
     * and consent markers); kept for tests and callers that want the zero-cost check.</p>
     *
     * @param userInput raw chat message
     * @return {@code true} when a hardcoded signal matched
     */
    public static boolean wantsApprovalFlow(String userInput) {
        return lexicalApprovalRequested(userInput);
    }

    /** Lexical detection shared by the static and instance entry points. */
    private static boolean lexicalApprovalRequested(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String norm = normalize(userInput);
        if (norm.matches("(?s)^(/(plan|agent)|!(plan|task))\\b.*")) {
            return true;
        }
        return (planningIntent(norm) && consentMarker(norm))
                || multiStepSequence(norm);
    }

    /** Exact planning intent verbs (multi-language), without accents. */
    private static boolean planningIntent(String norm) {
        return norm.contains("planifie")
                || norm.contains("planier")
                || norm.contains("planej")
                || norm.contains("prepar")
                || norm.contains("monte un plan")
                || norm.contains("monter un plan")
                || norm.contains("sequence")
                || norm.contains("plan") && norm.contains("task")
                || norm.contains("plan") && norm.contains("etapa")
                || norm.contains("plan") && norm.contains("passo")
                || (norm.contains("plan") && norm.contains("tache"));
    }

    /**
     * Explicit "do not start without my consent" markers (multi-language).
     */
    private static boolean consentMarker(String norm) {
        return norm.matches("(?s).*apres[^\\n]{0,40}(accord|approbation).*")
                || norm.matches("(?s).*(after|once)[^\\n]{0,40}(approval|consent|approbation).*")
                || norm.contains("ne lance rien")
                || norm.contains("ne rien lancer")
                || norm.contains("avant mon approbation")
                || norm.contains("avant mon accord")
                || norm.contains("avant de lancer")
                || norm.contains("sans mon approbation")
                || norm.contains("attend mon approbation")
                || norm.contains("attends mon approbation")
                || norm.contains("attend mon accord")
                || norm.contains("attends mon accord")
                || norm.contains("mon accord avant")
                || norm.contains("mon approbation avant")
                || norm.contains("avant l'exécuter")
                || norm.matches("(?s).*before (launching|executing|starting|running).*")
                || norm.matches("(?s).*without my (approval|consent).*")
                || norm.matches("(?s).*don'?t (do|run|start) anything.*")
                || norm.matches("(?s).*don'?t (do|run|start|execute) until.*")
                || norm.matches("(?s).*until[^\\n]{0,30}approval.*")
                || norm.matches("(?s).*wait for my (approval|ok|go).*")
                || norm.matches("(?s).*sin mi (aprobacion|consentimiento).*")
                || norm.matches("(?s).*espera mi (aprobacion|consentimiento).*")
                || norm.matches("(?s).*antes de lanzar.*")
                || norm.matches("(?s).*antes de ejecutar.*")
                || norm.matches("(?s).*no ejecutes nada.*")
                || norm.matches("(?s).*no hagas nada.*")
                || norm.matches("(?s).*sem minha (aprovacao|autorizacao).*")
                || norm.matches("(?s).*aguarde minha (aprovacao|autorizacao).*")
                || norm.matches("(?s).*aguardar minha (aprovacao|autorizacao).*")
                || norm.matches("(?s).*antes de lancer.*")
                || norm.matches("(?s).*antes de executar.*")
                || norm.matches("(?s).*nao faca nada.*")
                || norm.matches("(?s).*nao execute nada.*");
    }

    /**
     * Detects a step-by-step chained task: at least two sequence connectors
     * ({@code d'abord tu commence...}, {@code first... then...}, {@code luego...}...).
     * Such a "make it happen in order" request switches to planning mode.
     *
     * @param norm normalized query
     * @return {@code true} when the query chains two or more sequential actions
     */
    private static boolean multiStepSequence(String norm) {
        int steps = 0;
        for (Pattern marker : FIRST_MARKERS) {
            steps += countOccurrences(norm, marker);
        }
        for (Pattern marker : NEXT_MARKERS) {
            steps += countOccurrences(norm, marker);
        }
        steps += countOccurrences(norm, CONNECTOR_PUIS);
        return steps >= 2;
    }

    /**
     * Language-agnostic structural suspicion: numbered steps, or a rich multi-clause
     * actionable request with quantities. Cheap gate that decides whether the semantic
     * classifier deserves a call.
     *
     * @param userInput raw message
     * @return {@code true} when a semantic look is warranted
     */
    static boolean semanticSuspicious(String userInput) {
        if (userInput == null || userInput.isBlank() || userInput.length() < SEMANTIC_MIN_LENGTH) {
            return false;
        }
        String norm = normalize(userInput);
        int enumerated = countOccurrences(norm, ENUMERATED_STEP);
        if (enumerated >= 2) {
            return true;
        }
        boolean hasDigits = norm.matches("(?s).*\\d.*");
        int clauses = clauseCount(norm);
        int words = norm.trim().split("\\s+").length;
        return (hasDigits && clauses >= 2) || (clauses >= 3 && words >= 10);
    }

    private static int clauseCount(String norm) {
        int count = 1;
        for (int i = 0; i < norm.length(); i++) {
            char c = norm.charAt(i);
            if (c == ',' || c == ';' || c == '.' || c == ':' || c == '\n' || c == '!') {
                count++;
            }
        }
        return count;
    }

    /**
     * Semantic fallback: one yes/no question to the attached model about whether the
     * request describes an ordered multi-step task. Never routes on a plain question.
     *
     * @param userInput raw message
     * @return {@code true} when the model confirms a step-by-step actionable task
     */
    boolean semanticWantsPlanning(String userInput) {
        if (semanticModel == null || userInput == null || userInput.isBlank()) {
            return false;
        }
        try {
            ChatResponse response;
            try {
                response = CompletableFuture.supplyAsync(
                        () -> semanticModel.chat(ChatRequest.builder()
                                .messages(List.of(
                                        SystemMessage.from(
                                                "You classify user requests. Decide whether the request asks to "
                                                        + "perform at least two ordered actions step by step (for example "
                                                        + "numbered steps, or chained actions like first X then Y afterwards "
                                                        + "Z). It does not matter which language the user writes in: base "
                                                        + "your decision on the sequential, multi-action structure, not on "
                                                        + "specific words. A single simple request or a plain question is NO. "
                                                        + "Answer with exactly one word: YES or NO."),
                                        UserMessage.from(userInput)))
                                .build()),
                        SEMANTIC_EXECUTOR)
                        .get(SEMANTIC_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            } catch (java.util.concurrent.TimeoutException | java.util.concurrent.ExecutionException ex) {
                return false;
            }
            AiMessage answer = response == null ? null : response.aiMessage();
            String reply = answer == null || answer.text() == null ? "" : answer.text().trim().toUpperCase(Locale.ROOT);
            return reply.matches("(?s)^(YES|OUI)\\b.*");
        } catch (Exception ex) {
            return false;
        }
    }

    private static int countOccurrences(String text, Pattern pattern) {
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /** Lowercases and strips accents/punctuation so all dialects match the ASCII lexicon. */
    private static String normalize(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String decomposed = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return decomposed.replace('\'', ' ').replaceAll("[„“”«»¿¡]", "");
    }

    /**
     * Runs the planning phase only (no legacy call).
     *
     * @param userInput raw user query
     * @return a PROPOSAL (plan + token), or BLOCKED/FAILED
     */
    public AgentResponse plan(String userInput) {
        return engine.plan(userInput);
    }

    /**
     * Executes a previously approved plan.
     *
     * @param token single-use approval token
     * @return the final response of the resumed run
     */
    public AgentResponse execute(String token) {
        return engine.executeApproved(token, null);
    }

    /**
     * Branche un ecouteur recevant les evenements {@code (etape, statut)} pendant
     * l'execution du plan ({@code running}, puis {@code done} ou {@code error}),
     * afin que le chat affiche la progression etape par etape.
     *
     * @param listener callback, {@code null} pour detacher
     */
    public void setStepStatusListener(BiConsumer<String, String> listener) {
        engine.setStepStatusListener(listener);
    }

    /** Chat markdown of the response. */
    public String render(AgentResponse response) {
        if (response == null) {
            return "Aucune reponse de l'orchestrateur.";
        }
        switch (response.status()) {
            case PROPOSAL:
                return renderProposal(response);
            case SUCCESS:
                return renderSuccess(response);
            case BLOCKED:
                return "\u26d4 " + response.message();
            case FAILED:
            default:
                return "\u274c " + response.message();
        }
    }

    private String renderProposal(AgentResponse response) {
        StringBuilder sb = new StringBuilder("\ud83d\udccb **Plan proposé - en attente d'approbation**");
        ExecutionPlan plan = response.proposedPlan();
        if (plan != null && plan.hasSteps()) {
            int i = 1;
            for (PlanStep step : plan.steps()) {
                sb.append("\n- [ ] ").append(i++).append(". ").append(labelOf(step));
                Map<String, Object> business = businessArguments(step.arguments());
                if (!business.isEmpty()) {
                    sb.append("  _").append(arguments(business)).append('_');
                }
            }
        }
        sb.append("\n\n_Approuvez le plan pour lancer l'execution, ou annulez._");
        return sb.toString();
    }

    private String renderSuccess(AgentResponse response) {
        StringBuilder sb = new StringBuilder("\u2705 **Execution terminee**");
        Map<String, String> statuses = response.stepStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            sb.append('\n');
            int i = 1;
            for (Map.Entry<String, String> entry : statuses.entrySet()) {
                boolean done = "done".equals(entry.getValue());
                String label = userLabel(entry.getKey());
                if (label.isBlank()) {
                    label = "Etape " + i;
                }
                sb.append("\n- [").append(done ? 'x' : ' ').append("] ").append(i++)
                        .append(". ").append(label)
                        .append(done ? "" : " (echec)");
            }
        }
        if (response.structuredOutput() != null
                && !response.structuredOutput().answer().isBlank()) {
            sb.append("\n\n").append(response.structuredOutput().answer());
        }
        return sb.toString();
    }

    private static String labelOf(PlanStep step) {
        String label = userLabel(PlanStep.label(step));
        return label.isBlank() ? "Action a executer" : label;
    }
    /** Libellé présentable à l'utilisateur: expurge les mentions techniques (tables, identifiants). */
    private static String userLabel(String text) {
        if (text == null) {
            return "";
        }
        String clean = text
                .replaceAll("(?i)\\s*\\(\\s*table[s]?\\s*[:=]?\\s*[^)]*\\)", " ")
                .replaceAll("(?i)\\b(euid|ucontract|uid[#]?contrat|engagementid)\\b", " ")
                .replaceAll("\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
        return clean;
    }

    /** Arguments métier présentables: identité expurgée, valeurs vides retirées. */
    private static Map<String, Object> businessArguments(Map<String, Object> arguments) {
        Map<String, Object> clean = new java.util.LinkedHashMap<>();
        if (arguments == null || arguments.isEmpty()) {
            return clean;
        }
        for (Map.Entry<String, Object> entry
                : com.endeleya.kazisafex.agent.graph.LegacyExecutorNode
                        .neutralizeIdentityKeys(arguments).entrySet()) {
            if (entry.getValue() == null || String.valueOf(entry.getValue()).isBlank()) {
                continue;
            }
            clean.put(entry.getKey(), entry.getValue());
        }
        return clean;
    }

    private static String arguments(Map<String, Object> arguments) {
        return arguments.entrySet().stream()
                .map(e -> e.getKey() + "=" + String.valueOf(e.getValue()))
                .collect(Collectors.joining(", "));
    }
}