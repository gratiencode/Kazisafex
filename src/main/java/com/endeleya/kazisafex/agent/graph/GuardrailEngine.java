package com.endeleya.kazisafex.agent.graph;

import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Deterministic input guardrail run before any LLM or legacy invocation.
 *
 * <p>Checks are inexpensive and purely textual, honouring the checklist requirement
 * "is input validation handled before calling expensive LLM models?". Patterns cover
 * prompt injection, secret/PII harvesting, destructive SQL commands and attempts to
 * escape the enterprise identity boundary (another enterprise/contract/database).</p>
 */
public final class GuardrailEngine {

    private static final int MAX_QUERY_LENGTH = 8_000;

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?is)ignore\\s+(all\\s+)?(previous|prior|the\\s+above|system)"),
            Pattern.compile("(?is)ignorez\\s+(toutes\\s+)?(les\\s+)?(instructions|directives|consignes)"),
            Pattern.compile("(?is)(simulate|pretend|agis)\\s+(comme|as|that)\\s+(beings?|a\\s+developer)"),
            Pattern.compile("(?is)tu\\s+es\\s+maintenant"),
            Pattern.compile("(?is)(system\\s+prompt|developer\\s+message|tool_result)\\s*[:=]"),
            Pattern.compile("(?is)disclose\\s+(the\\s+)?system"),
            Pattern.compile("(?is)(reveal|affiche|fournis)\\s+(les?\\s+)?(instructions|le\\s+systeme)"));

    private static final List<Pattern> SECRET_PATTERNS = List.of(
            Pattern.compile("(?is)\\b(mot\\s+de\\s+passe|password|passwd)\\b"),
            Pattern.compile("(?is)\\b(secret|api[_-]?key|accesstoken|access[_-]?token)\\b"),
            Pattern.compile("(?is)\\b(num[eé]ro\\s+de\\s+(carte|compte)|card\\s+number|cvv)\\b"),
            Pattern.compile("(?is)\\b(iban|r[iI][bB])\\b"),
            Pattern.compile("(?is)\\b(code\\s+(secret|pin|otp))\\b"));

    private static final List<Pattern> DESTRUCTIVE_PATTERNS = List.of(
            Pattern.compile("(?is)\\b(drop|truncate)\\s+table\\b"),
            Pattern.compile("(?is)\\bdelete\\s+from\\b"),
            Pattern.compile("(?is)\\b(drop\\s+database|rm\\s+-rf|format\\s+c:)\\b"));

    /**
     * Attempts to escape the identity boundary of the run: switching to another
     * enterprise, another engagement/contract or an external database/server. The
     * database is derived from {@code eUid}/{@code ucontract} in the preferences,
     * never from the user text — these patterns intercept the phrasing before any
     * LLM/legacy cost.
     */
    private static final List<Pattern> CROSS_ENTERPRISE_PATTERNS = List.of(
            // Nom de base d'un autre tenant (convention ksf_<euid>)
            Pattern.compile("(?is)\\bksf_[0-9a-f]{8,}[-_0-9a-f]*\\b"),
            // Se connecter / pointer / brancher vers une AUTRE base/serveur/magasin
            Pattern.compile("(?is)(connecte|connexion|pointe|branche).{0,40}(autre|different|différente|une autre|un autre).{0,60}(base|bdd|database|serveur|server|magasin|boutique|depot|schema|tenant|entreprise)"),
            Pattern.compile("(?is)(connecte|connexion).{0,40}(sur|vers|to|au|a|.)\\s*(base|bdd|database|serveur|server|magasin|boutique|depot|tenant|schema)"),
            // Changer / basculer / switcher d'entreprise, de contrat ou de base
            Pattern.compile("(?is)(changer|change|switch|basculer).{0,45}(entreprise|societe|société|boutique|magasin|depot|base|bdd|database|serveur|server|contrat|contract|compte|tenant)"),
            Pattern.compile("(?is)(basculer|passer).{0,45}(sur|vers|de)\\s*(base|bdd|database|serveur|server|magasin|boutique|depot|entreprise|societe|société|contrat|contract)"),
            // Mention d'une ENTITE AUTRE que la sienne
            Pattern.compile("(?is)(autre|une autre|un autre|different|différente)\\s+(entreprise|societe|société|boutique|magasin|depot|compte|client|collaborateur|patron|base|bdd|database|serveur|server)"),
            Pattern.compile("(?is)contrat\\s+de\\s+(un|une)?\\s*(autre|different|différente|client|collaborateur|patron)"),
            Pattern.compile("(?is)cross[\\s-]tenant"));

    /**
     * Inspects the raw user query.
     *
     * @param query user input, possibly {@code null}
     * @return the guardrail verdict
     */
    public GuardrailVerdict inspect(String query) {
        if (query == null || query.isBlank()) {
            return GuardrailVerdict.block("Requete vide : aucune execution n'est lancee.");
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            return GuardrailVerdict.block("Requete trop longue (max " + MAX_QUERY_LENGTH
                    + " caracteres) : refusee avant toute invocation LLM/legacy.");
        }
        String needle = query.toLowerCase(Locale.ROOT);
        Pattern hit = firstMatch(INJECTION_PATTERNS, needle);
        if (hit != null) {
            return GuardrailVerdict.block(
                    "Injection de prompt detectee (" + hit.pattern() + ") : requete interceptee.");
        }
        hit = firstMatch(DESTRUCTIVE_PATTERNS, needle);
        if (hit != null) {
            return GuardrailVerdict.block(
                    "Commande destructive detectee (" + hit.pattern() + ") : requete interceptee.");
        }
        hit = firstMatch(CROSS_ENTERPRISE_PATTERNS, needle);
        if (hit != null) {
            return GuardrailVerdict.block(
                    "Changement d'identite entreprise / cible de donnees externe detecte ("
                            + hit.pattern() + ") : requete interceptee dans le contexte eUid/contrat courant.");
        }
        hit = firstMatch(SECRET_PATTERNS, needle);
        if (hit != null) {
            return GuardrailVerdict.block(
                    "Tentative de collecte d'information sensible detectee ("
                            + hit.pattern() + ") : requete interceptee.");
        }
        return GuardrailVerdict.pass("Requete validee.");
    }

    private Pattern firstMatch(List<Pattern> patterns, String needle) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(needle).find()) {
                return pattern;
            }
        }
        return null;
    }
}