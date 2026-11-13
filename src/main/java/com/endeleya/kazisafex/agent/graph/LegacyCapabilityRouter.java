package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.legacy.LegacyToolDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Deterministic planner used when no LLM is attached.
 *
 * <p>It evaluates the user query against the discovered legacy capabilities and emits
 * an ordered {@link ExecutionPlan}. This guarantees the orchestrator stays functional
 * (and testable) without any model: the LLM-based planner is only an enrichment.</p>
 */
public final class LegacyCapabilityRouter {

    /** Tool name constants matched against the demo facade. */
    public static final String SEARCH_PRODUCTS = "search_products";
    public static final String COMPUTE_STOCK_VALUATION = "compute_stock_valuation";

    /**
     * Routes a query onto a plan over the provided legacy capabilities.
     *
     * @param query original user query
     * @param tools discovered legacy capabilities
     * @return the execution plan
     */
    public ExecutionPlan route(String query, List<LegacyToolDescriptor> tools) {
        String q = normalize(query);
        List<PlanStep> steps = new ArrayList<>();
        String reason = "Planifieur deterministe (aucun modele).";

        if (looksLikeStockValuation(q)) {
            String category = extractCategory(query);
            if (findTool(tools, COMPUTE_STOCK_VALUATION)) {
                Map<String, Object> args = new LinkedHashMap<>();
                args.put("category", category);
                steps.add(PlanStep.of(COMPUTE_STOCK_VALUATION,
                        "Valoriser le stock (" + category + ")", args));
            }
        }
        if (looksLikeProductSearch(q)) {
            if (findTool(tools, SEARCH_PRODUCTS)) {
                Map<String, Object> args = new LinkedHashMap<>();
                args.put("searchTerm", defaultSearchTerm(query));
                args.put("limit", 10);
                steps.add(PlanStep.of(SEARCH_PRODUCTS,
                        "Rechercher les produits correspondants", args));
            }
        }
        if (steps.isEmpty()) {
            return ExecutionPlan.empty();
        }
        return new ExecutionPlan(List.copyOf(steps), reason);
    }

    private boolean looksLikeStockValuation(String q) {
        return q.contains("stock") || q.contains("inventaire")
                || q.contains("valorisation") || q.contains("valeur")
                || q.contains("montant du stock");
    }

    private boolean looksLikeProductSearch(String q) {
        return q.contains("produit") || q.contains("cherche")
                || q.contains("recherche") || q.contains("liste")
                || q.matches(".*(affiche|montre).*");
    }

    private String extractCategory(String query) {
        String lower = normalize(query);
        for (String candidate : candidateCategories(lower)) {
            if (lower.contains(candidate)) {
                return capitalize(candidate);
            }
        }
        return "*";
    }

    private List<String> candidateCategories(String q) {
        int iCategorie = q.indexOf("categorie");
        if (iCategorie >= 0) {
            String tail = q.substring(iCategorie).replace("categorie", "").trim();
            if (!tail.isEmpty()) {
                return List.of(stripPunctuation(tail));
            }
        }
        return List.of("epicerie", "boisson", "fourniture");
    }

    private String defaultSearchTerm(String query) {
        String q = normalize(query);
        for (String stop : List.of(
                "recherche", "cherche", "produits", "produit", "les", "des",
                "une", "un", "liste", "affiche", "montre", "moi", "stp", "svp")) {
            q = q.replace(" " + stop, "");
        }
        return stripPunctuation(q).isBlank() ? "*" : stripPunctuation(q);
    }

    private boolean findTool(List<LegacyToolDescriptor> tools, String name) {
        return tools.stream().anyMatch(t -> t.name().equalsIgnoreCase(name));
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private String stripPunctuation(String text) {
        return text.replaceAll("[?.!,:;\"']+", "")
                .replaceAll("[àáâä]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[îï]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ûü]", "u")
                .trim();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}