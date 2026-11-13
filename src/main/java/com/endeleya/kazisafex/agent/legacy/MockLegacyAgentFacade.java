package com.endeleya.kazisafex.agent.legacy;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mock representation of the pre-existing agent: binds the legacy workflows to the
 * orchestrator without touching the legacy code.
 *
 * <p>Two capabilities are exposed, each backed by an unmodified legacy class:
 * <ul>
 *   <li>{@code search_products}          -> {@link LegacyDatabaseSearchWorkflow#run}</li>
 *   <li>{@code compute_stock_valuation}  -> {@link LegacyInventoryComputationWorkflow#run}</li>
 * </ul>
 * </p>
 */
public final class MockLegacyAgentFacade implements LegacyAgentFacade {

    private final String inventorySourceName;

    /**
     * Creates the demo facade.
     *
     * @param inventorySourceName label representing the underlying DB/ERP source
     */
    public MockLegacyAgentFacade(String inventorySourceName) {
        this.inventorySourceName = inventorySourceName;
    }

    /** Creates the demo facade with a default source label. */
    public MockLegacyAgentFacade() {
        this("kazisafe_local_erp");
    }

    @Override
    public List<LegacyToolDescriptor> discover() {
        return List.of(searchProducts(), computeStockValuation());
    }

    @Override
    public Optional<LegacyToolDescriptor> describe(String toolName) {
        return discover().stream()
                .filter(t -> t.name().equalsIgnoreCase(toolName))
                .findFirst();
    }

    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws LegacyExecutionException {
        Optional<LegacyToolDescriptor> tool = describe(toolName);
        if (tool.isEmpty()) {
            throw new LegacyExecutionException("Capability inconnue: " + toolName);
        }
        try {
            return tool.get().invocation().apply(arguments);
        } catch (LegacyExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LegacyExecutionException("Echec du workflow legacy '" + toolName + "'", ex);
        }
    }

    private LegacyToolDescriptor searchProducts() {
        JsonObjectSchema schema = JsonObjectSchema.builder()
                .addStringProperty("searchTerm", "Mot-clef de recherche libre")
                .addIntegerProperty("limit", "Nombre maxi de resultats (defaut 10)")
                .required("searchTerm")
                .build();
        return new LegacyToolDescriptor(
                "search_products",
                "Recherche des produits dans la base legacy par nom ou categorie "
                        + "autour de l'inventaire '" + inventorySourceName + "'.",
                schema,
                args -> new LegacyDatabaseSearchWorkflow().run(
                        String.valueOf(args.getOrDefault("searchTerm", "")),
                        ((Number) args.getOrDefault("limit", 10)).intValue()));
    }

    private LegacyToolDescriptor computeStockValuation() {
        JsonObjectSchema schema = JsonObjectSchema.builder()
                .addStringProperty("category", "Categorie a valoriser, ou '*' pour le stock entier")
                .build();
        return new LegacyToolDescriptor(
                "compute_stock_valuation",
                "Calcule la valeur monetaire du stock d'une categorie sur l'inventaire '"
                        + inventorySourceName + "' (legacy, synchrone).",
                schema,
                args -> new LegacyInventoryComputationWorkflow().run(
                        String.valueOf(args.getOrDefault("category", "*"))));
    }
}