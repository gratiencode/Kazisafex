package com.endeleya.kazisafex.agent.graph;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

/**
 * Strongly-typed {@link AiServices} contracts used by the planner, critic and
 * formatter nodes when a {@link ChatModel} is available.
 *
 * <p>Because the state DTOs ({@link ExecutionPlan}, {@link StructuredAnswer}) embed raw
 * JSON maps, the LLM-facing contracts use dedicated draft records with JSON-friendly
 * scalar fields; the nodes convert drafts into the typed state DTOs afterwards.</p>
 */
public final class AgentAssistants {

    private AgentAssistants() {
    }

    /** LLM-side draft of the execution plan (JSON-friendly). */
    public record PlanDraft(List<StepDraft> steps, String reasoning) {
        public record StepDraft(String toolName, String description, String argumentsJson) {
        }

        public static PlanDraft none() {
            return new PlanDraft(List.of(), "Aucune capacite legacy ne correspond.");
        }
    }

    /** LLM-side draft of the formatted answer. */
    public record FormatDraft(String summary, String answerText) {
    }

    /**
     * Message système du planificateur, réutilisé par {@link PlannerNode} lors d'une
     * retentative en direct quand le proxy AiServices échoue à extraire le JSON brut.
     */
    public static final String PLANNER_SYSTEM = """
            Tu es le planificateur du chef d'orchestration Kazisafe.
            Tu reçois la demande utilisateur et la liste des capacités legacy disponibles
            (présentées sous la forme: nom -> description).

            Tu proposes la SÉQUENCE ORDONNÉE d'outils à exécuter pour satisfaire la
            demande. Le plan devra être approuvé par l'utilisateur avant toute exécution :
            ta mission est UNIQUEMENT de planifier, pas d'exécuter.

            Règles:
            - Chaque étape référence EXACTEMENT un nom d'outil présent dans la liste.
            - SI l'utilisateur énumère lui-même les étapes à suivre (étapes numérotées,
              ou enchaînées par des connecteurs: d'abord... puis... ensuite... apres...
              enfin...), SUIS-LES EXACTEMENT, dans le même ordre, et mappe chaque étape
              sur l'outil correspondant.
            - SINON (tâche complexe mais sans étapes explicites), conçois toi-même un
              plan ordonné, logique et raisonnable qui atteint l'objectif assigné.
            - description: étape libellée en langage métier SIMPLE et non technique,
              compréhensible par un utilisateur sans vocabulaire technique. INTERDIT
              dans description: le nom de l'outil, les noms de tables, les identifiants
              techniques (eUid, ucontract), les codes internes.
            - Renseigne argumentsJson avec les valeurs déductibles de la demande
              (quantités, catégories, noms, seuils...), sinon un objet vide {}.
            - Ne renvoie une liste de steps VIDE que si AUCUN outil de la liste ne peut
              satisfaire la demande.

            Format attendu (uniquement du JSON, sans balise ni commentaire):
            {"steps":[{"toolName":"exact_name_from_the_list","description":"but de l'étape en langage simple","argumentsJson":"{...}"}],"reasoning":"justification concise"}

            Exemple pour "cree la categorie puis cree un produit":
            {"steps":[{"toolName":"creer_categorie","description":"Créer la catégorie demandée","argumentsJson":"{\\"name\\":\\"friandise\\"}"},{"toolName":"creer_produit","description":"Créer le produit dans la catégorie","argumentsJson":"{}"}],"reasoning":"Trois créations ordonnées"}
            """;

    /**
     * Planner contract: maps the user query onto the available legacy capabilities.
     * The tools are only described (names + descriptions): the model chooses the
     * sequence and proposes JSON arguments.
     */
    public interface PlanningAgent {

        @SystemMessage(PLANNER_SYSTEM)
        @UserMessage("""
                Capacités legacy disponibles:
                {{capabilities}}

                Demande utilisateur:
                {{query}}
                """)
        PlanDraft plan(
                @V("capabilities") String capabilities,
                @V("query") String query);
    }

    /**
     * Message système du critic, réutilisé par {@link CriticNode} lors d'une retentative
     * en direct quand le proxy AiServices échoue à extraire du JSON brut.
     */
    public static final String CRITIC_SYSTEM = """
            Tu es l'évaluateur (critic) du chef d'orchestration Kazisafe.
            Tu reçois les résultats bruts de l'exécution legacy.
            Rends un verdict COMPLETE (résultats suffisants), RETRY (réexécuter avec des
            paramètres ajustés), REVISE (réviser le plan) ou ABORT (abandonner).
            Ajoute un feedback concis en langage métier non technique (sans noms d'outils,
            ni noms de tables, ni identifiants techniques) et un score de 0 à 100.
            Réponds EXACTEMENT par un objet JSON brut, sans balise (aucun ```json), sans
            texte avant ni après :
            {"verdict":"COMPLETE","feedback":"...","score":100,"attempt":1}
            """;

    /**
     * Critic contract: evaluates the legacy outputs and decides the next step.
     */
    public interface CriticAgent {

        @SystemMessage(CRITIC_SYSTEM)
        @UserMessage("""
                Plan exécuté: {{plan}}
                Résultats legacy:
                {{results}}

                Tentative numéro {{attempt}}.
                """)
        CriticEvaluation evaluate(
                @V("plan") String plan,
                @V("results") String results,
                @V("attempt") int attempt);
    }

    /**
     * Formatter contract: converts the raw legacy outputs into a polished, structured
     * user-facing answer.
     */
    public interface FormatterAgent {

        @SystemMessage("""
                Tu es le formateur final du chef d'orchestration Kazisafe.
                A partir des résultats legacy bruts, produis une réponse utilisateur
                structurée (summary + answerText), dans la langue de l'utilisateur,
                avec des tableaux Markdown quand les données s'y prêtent.
                La réponse doit être en langage métier non technique (sans noms d'outils,
                ni noms de tables, ni identifiants techniques).
                Réponds EXACTEMENT par un objet JSON brut, sans balise (aucun ```json),
                sans texte avant ni après.
                """)
        @UserMessage("""
                Résultats legacy bruts:
                {{results}}
                """)
        FormatDraft format(@V("results") String results);
    }

    /**
     * Group of AiServices proxies built over a single {@link ChatModel}.
     *
     * @param planner   planning proxy
     * @param critic    critic proxy
     * @param formatter formatter proxy
     */
    public record Assistants(ChatModel model, PlanningAgent planner, CriticAgent critic, FormatterAgent formatter) {

        /** Convenience: {@code true} when bound to a real model. */
        public boolean enabled() {
            return model != null;
        }
    }

    /**
     * Builds the three proxies over the provided model.
     *
     * @param model chat model; may be {@code null} (deterministic nodes take over)
     * @return the assistant group
     */
    public static Assistants assemble(ChatModel model) {
        if (model == null) {
            return new Assistants(null, null, null, null);
        }
        PlanningAgent planner = AiServices.builder(PlanningAgent.class)
                .chatModel(model)
                .build();
        CriticAgent critic = AiServices.builder(CriticAgent.class)
                .chatModel(model)
                .build();
        FormatterAgent formatter = AiServices.builder(FormatterAgent.class)
                .chatModel(model)
                .build();
        return new Assistants(model, planner, critic, formatter);
    }
}