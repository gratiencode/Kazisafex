package com.endeleya.kazisafex.agent.legacy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared read-only catalogue used by the legacy workflows.
 *
 * <p>Represents the legacy database, wrapped by a plain holder so that the mock
 * facade and the legacy workflows agree on the same "database" without coupling.
 * This is intentionally simple: it only exists to simulate the legacy persistence
 * that the orchestrator must not own.</p>
 */
public final class GlobalWorkspace {

    private static final Map<String, LegacyProduct> CATALOGUE;
    private static final String[] CATEGORIES = {"Epicerie", "Boissons", "Fournitures"};

    static {
        Map<String, LegacyProduct> catalogue = new LinkedHashMap<>();
        catalogue.put("P-1001", new LegacyProduct("P-1001", "Lait en poudre 1kg", "Epicerie", 12000d, 48d));
        catalogue.put("P-1002", new LegacyProduct("P-1002", "Huile vegetale 5L", "Epicerie", 28500d, 21d));
        catalogue.put("P-1003", new LegacyProduct("P-1003", "Sirop de quinquina 1L", "Boissons", 14500d, 63d));
        catalogue.put("P-1004", new LegacyProduct("P-1004", "Farine de ble 50kg", "Epicerie", 95000d, 8d));
        catalogue.put("P-1005", new LegacyProduct("P-1005", "Cahier 200 pages", "Fournitures", 4900d, 130d));
        CATALOGUE = Map.copyOf(catalogue);
    }

    private GlobalWorkspace() {
    }

    /** Read-only view of the legacy product catalogue. */
    public static List<LegacyProduct> catalogue() {
        return List.copyOf(CATALOGUE.values());
    }

    /** Legacy categories used by the workflows. */
    public static String[] categories() {
        return CATEGORIES.clone();
    }
}