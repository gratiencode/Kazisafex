package com.endeleya.kazisafex.agent.legacy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Legacy database search workflow.
 *
 * <p>This class intentionally mimics a pre-existing, unmodifiable component of the
 * legacy agent: it does not implement any LangChain4j interface, uses plain blocking
 * JDBC-style access (simulated by a compact in-memory catalogue plus a simulated
 * latency), and returns a raw {@code String} payload. It is {@code public void run}
 * style API, adopted so that {@link com.endeleya.kazisafex.agent.LegacyToolAdapter}
 * can demonstrate the dynamic tool derivation pattern without touching this file.</p>
 */
public final class LegacyDatabaseSearchWorkflow {

    private static final Map<String, LegacyProduct> CATALOGUE = buildCatalogue();

    /**
     * Default constructor kept public so adapters can instantiate the legacy workflow
     * without any modification.
     */
    public LegacyDatabaseSearchWorkflow() {
    }

    /**
     * Legacy blocking entry point. Returns JSON-like text payload.
     *
     * @param searchTerm free-text filter on product labels and categories
     * @param limit      maximum number of rows returned
     * @return a raw JSON array of the matched products
     */
    public String run(String searchTerm, int limit) {
        String needle = normalize(searchTerm);
        List<LegacyProduct> matches = CATALOGUE.values().stream()
                .filter(p -> normalize(p.name()).contains(needle)
                        || normalize(p.category()).contains(needle))
                .limit(Math.max(1, limit))
                .toList();
        return toJson(matches);
    }

    private String toJson(List<LegacyProduct> products) {
        return "[" + products.stream()
                .map(this::toJson)
                .collect(Collectors.joining(",")) + "]";
    }

    private String toJson(LegacyProduct p) {
        return "{\"id\":\"" + p.id() + "\",\"name\":\"" + p.name()
                + "\",\"category\":\"" + p.category() + "\",\"unitPrice\":"
                + p.unitPrice() + ",\"availableQuantity\":" + p.availableQuantity() + "}";
    }

    private String normalize(String term) {
        return term == null ? "" : term.toLowerCase(Locale.ROOT).trim();
    }

    private static Map<String, LegacyProduct> buildCatalogue() {
        Map<String, LegacyProduct> catalogue = new LinkedHashMap<>();
        catalogue.put("P-1001", new LegacyProduct("P-1001", "Lait en poudre 1kg", "Epicerie", 12000d, 48d));
        catalogue.put("P-1002", new LegacyProduct("P-1002", "Huile vegetale 5L", "Epicerie", 28500d, 21d));
        catalogue.put("P-1003", new LegacyProduct("P-1003", "Sirop de quinquina 1L", "Boissons", 14500d, 63d));
        catalogue.put("P-1004", new LegacyProduct("P-1004", "Farine de ble 50kg", "Epicerie", 95000d, 8d));
        catalogue.put("P-1005", new LegacyProduct("P-1005", "Cahier 200 pages", "Fournitures", 4900d, 130d));
        return catalogue;
    }
}