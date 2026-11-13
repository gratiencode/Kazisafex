package com.endeleya.kazisafex.agent.legacy;

import java.util.List;

/**
 * Legacy stock valuation workflow.
 *
 * <p>Like {@link LegacyDatabaseSearchWorkflow}, this class stands for a pre-existing
 * business workflow of the legacy agent (blocking, returning raw text). It computes the
 * monetary value of the current stock for a product category.</p>
 */
public final class LegacyInventoryComputationWorkflow {

    /**
     * Default constructor kept public so adapters can instantiate the legacy workflow
     * without any modification.
     */
    public LegacyInventoryComputationWorkflow() {
    }

    /**
     * Legacy blocking entry point. Returns JSON-like text payload.
     *
     * @param category product category to value, or {@code "*"} for the whole stock
     * @return raw JSON with computed figures
     */
    public String run(String category) {
        List<LegacyProduct> scope = GlobalWorkspace.catalogue().stream()
                .filter(p -> category == null || category.isBlank() || "*".equals(category)
                        || category.equalsIgnoreCase(p.category()))
                .toList();
        double value = scope.stream()
                .mapToDouble(p -> p.unitPrice() * p.availableQuantity())
                .sum();
        int lines = scope.size();
        return String.format(java.util.Locale.ROOT,
                "{\"category\":\"%s\",\"valuatedLines\":%d,\"stockValue\":%,.2f}",
                category == null ? "*" : category, lines, value);
    }
}