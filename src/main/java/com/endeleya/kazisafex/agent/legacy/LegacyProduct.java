package com.endeleya.kazisafex.agent.legacy;

/**
 * Simple projection of a product as it exists in the legacy domain model.
 *
 * <p>This record deliberately lives in the {@code legacy} package: it mirrors the
 * shape of an entity owned by the pre-existing application layer and must not be
 * changed by the orchestrator. The orchestrator only reads it through the legacy
 * facade.</p>
 *
 * @param id               internal product identifier
 * @param name             product label
 * @param category         product category
 * @param unitPrice        current sale unit price
 * @param availableQuantity stock currently available
 */
public record LegacyProduct(
        String id,
        String name,
        String category,
        double unitPrice,
        double availableQuantity) {

    /** Builds a legacy product projected from a generic map of raw values. */
    public static LegacyProduct fromMap(java.util.Map<String, Object> values) {
        String id = String.valueOf(values.getOrDefault("id", ""));
        String name = String.valueOf(values.getOrDefault("name", ""));
        String category = String.valueOf(values.getOrDefault("category", ""));
        double price = ((Number) values.getOrDefault("unitPrice", 0d)).doubleValue();
        double qty = ((Number) values.getOrDefault("availableQuantity", 0d)).doubleValue();
        return new LegacyProduct(id, name, category, price, qty);
    }
}