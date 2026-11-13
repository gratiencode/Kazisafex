package com.endeleya.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.endeleya.ia.GratienTools.DedupMergeMode;
import tools.Util;

public class GratienGenericToolsTest {

    private GratienTools tools;

    @BeforeEach
    void setUp() {
        tools = new GratienTools();
    }

    private Object invokeInstance(Object target, String name, Class<?>[] types, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception ex) {
            throw new RuntimeException("Echec reflexion sur " + name, ex);
        }
    }

    private Object invokeStatic(String name, Class<?>[] types, Object... args) {
        try {
            Method method = GratienTools.class.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception ex) {
            throw new RuntimeException("Echec reflexion statique sur " + name, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> entitySpecs() {
        try {
            Field field = GratienTools.class.getDeclaredField("ENTITY_SPECS");
            field.setAccessible(true);
            return (Map<String, ?>) field.get(null);
        } catch (Exception ex) {
            throw new RuntimeException("Acces ENTITY_SPECS impossible", ex);
        }
    }

    private Object entitySpecField(String canonicalType, String methodName) {
        Object spec = entitySpecs().get(canonicalType);
        assertNotNull(spec, "Spec manquante pour " + canonicalType);
        try {
            Method accessor = spec.getClass().getMethod(methodName);
            accessor.setAccessible(true);
            return accessor.invoke(spec);
        } catch (Exception ex) {
            throw new RuntimeException("Acces " + methodName + " de " + canonicalType + " impossible", ex);
        }
    }

    @Test
    void registry_containsAllKnownTypes() {
        assertEquals(32, entitySpecs().size(), "Le registre doit couvrir les 32 entites cibles");
        assertEquals("CLIENT", entitySpecField("Client", "table").toString());
        assertEquals(data.Client.class, entitySpecField("Client", "entityClass"));
        assertEquals("CATEGORY", entitySpecField("Category", "table").toString());
        assertEquals("FOURNISSEUR", entitySpecField("Fournisseur", "table").toString());
        assertEquals("PRODUIT", entitySpecField("Produit", "table").toString());
        assertEquals("VENTE", entitySpecField("Vente", "table").toString());
        assertEquals("ARETIRER", entitySpecField("Aretirer", "table").toString());
        assertEquals("MATIERE", entitySpecField("Matiere", "table").toString());
        assertEquals("COMPTETRESOR", entitySpecField("CompteTresor", "table").toString());
        assertEquals("PRIXDEVENTE", entitySpecField("PrixDeVente", "table").toString());
    }

    @Test
    void resolveEntitySpec_normalizesMainType() {
        assertEquals("Client", entitySpecType("Client"));
        assertEquals("Client", entitySpecType("client"));
        assertEquals("Fournisseur", entitySpecType("Fournisseurs"));
        assertEquals("Produit", entitySpecType("produits"));
        assertEquals("Mesure", entitySpecType("unite"));
    }

    @Test
    void resolveEntitySpec_handlesAccentsAndSimpleAliases() {
        assertEquals("Category", entitySpecType("Catégorie"));
        assertEquals("Category", entitySpecType("categorie"));
        assertEquals("Matiere", entitySpecType("materiau"));
        assertEquals("CompteTresor", entitySpecType("banque"));
        assertEquals("Vente", entitySpecType("ventes"));
        assertEquals("Production", entitySpecType("Production"));
    }

    @Test
    void resolveEntitySpec_returnsNullForUnknown() {
        assertNull(invokeInstance(tools, "resolveEntitySpec", new Class<?>[]{String.class}, "QwertyInconnu"));
    }

    @Test
    void unknownEntityType_listsSupportedTypes() {
        String message = (String) invokeInstance(tools, "unknownEntityType", new Class<?>[]{String.class}, "Bogus");
        assertTrue(message.contains("Bogus"));
        assertTrue(message.contains("Types supportés"));
        assertTrue(message.contains("Client"));
    }

    @Test
    void staticNormalize_stripsAccentsAndKeepsAscii() {
        assertEquals("categorie", invokeStatic("staticNormalize", new Class<?>[]{String.class}, "Catégorie"));
        assertEquals("client", invokeStatic("staticNormalize", new Class<?>[]{String.class}, "  Client  "));
        assertEquals("prixdevente", invokeStatic("staticNormalize", new Class<?>[]{String.class}, "PrixDeVente"));
        assertEquals("", invokeStatic("staticNormalize", new Class<?>[]{String.class}, (Object) null));
    }

    @Test
    void entityUid_readsStoredOrBlank() {
        data.Client client = new data.Client();
        assertEquals("", invokeInstance(tools, "entityUid", new Class<?>[]{Object.class}, client));
        client.setUid("0123456789abcdef0123456789abcdef");
        assertEquals("0123456789abcdef0123456789abcdef",
                invokeInstance(tools, "entityUid", new Class<?>[]{Object.class}, client));
    }

    private Class<?> entitySpecClass() {
        try {
            return Class.forName("com.endeleya.ia.GratienTools$EntitySpec");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Classe EntitySpec introuvable", ex);
        }
    }

    @Test
    void parseEntity_bindsFieldsAndIgnoresUnknownProperties() {
        Object spec = entitySpecs().get("Client");
        Object client = invokeInstance(tools, "parseEntity",
                new Class<?>[]{entitySpecClass(), String.class},
                spec, "{\"nomClient\":\"Jean\",\"typeClient\":\"Client\",\"phone\":\"+243123\",\"inconnu\":\"x\"}");
        assertNotNull(client);
        assertTrue(client instanceof data.Client);
        data.Client parsed = (data.Client) client;
        assertEquals("Jean", parsed.getNomClient());
        assertEquals("+243123", parsed.getPhone());
    }

    @Test
    void parseEntity_returnsNullForInvalidJson() {
        Object spec = entitySpecs().get("Client");
        Object client = invokeInstance(tools, "parseEntity",
                new Class<?>[]{entitySpecClass(), String.class},
                spec, "{{{ pas du json");
        assertNull(client);
    }

    @Test
    void ensureEntityUid_generatesUidWhenBlank() {
        Object spec = entitySpecs().get("Client");
        Object client = invokeInstance(tools, "parseEntity",
                new Class<?>[]{entitySpecClass(), String.class},
                spec, "{\"nomClient\":\"Jean\"}");
        assertNotNull(client);
        invokeInstance(tools, "ensureEntityUid", new Class<?>[]{Object.class}, client);
        String uid = (String) invokeInstance(tools, "entityUid", new Class<?>[]{Object.class}, client);
        assertEquals(32, uid.length(), "uid genere doit faire 32 caracteres hex");
        assertTrue(uid.matches("[0-9a-f]{32}"));
    }

    @Test
    void entitySummary_containsUidAndScalarFields() {
        data.Client client = new data.Client();
        client.setUid("abcd1234");
        client.setNomClient("Jean");
        client.setPhone("+243");
        String summary = (String) invokeInstance(tools, "entitySummary", new Class<?>[]{Object.class}, client);
        assertTrue(summary.startsWith("Client { "));
        assertTrue(summary.contains("uid=abcd1234"));
        assertTrue(summary.contains("NomClient=Jean"));
        assertTrue(summary.contains("Phone=+243"));
        assertFalse(summary.contains("null"), "Les champs vides ne doivent pas apparaitre");
    }

    @Test
    void entityMatches_matchesSpecificFieldIgnoringCase() {
        data.Client jean = new data.Client();
        jean.setNomClient("Jean");
        data.Client marie = new data.Client();
        marie.setNomClient("Marie");
        assertTrue(match(jean, Map.of("nomClient", "jean")));
        assertFalse(match(marie, Map.of("nomClient", "Jean")));
    }

    @Test
    void entityMatches_freeTextSearchesAllFields() {
        data.Client jean = new data.Client();
        jean.setNomClient("Jean-Claude");
        jean.setPhone("+243999");
        assertTrue(match(jean, Map.of("q", "claude")));
        assertTrue(match(jean, Map.of("q", "+243999")));
        assertFalse(match(jean, Map.of("q", "introuvable")));
    }

    @Test
    void entityMatches_associationUidUsesReferencedEntityUid() {
        data.Client parent = new data.Client();
        parent.setUid("parent-uid-xyz");
        data.Client child = new data.Client();
        child.setNomClient("Fils");
        child.setParentId(parent);
        assertTrue(match(child, Map.of("parentId", "parent-uid-xyz")));
        assertFalse(match(child, Map.of("parentId", "autre-uid")));
    }

    private boolean match(Object entity, Map<String, Object> filters) {
        return (Boolean) invokeInstance(tools, "entityMatches",
                new Class<?>[]{Object.class, Map.class}, entity, filters);
    }

    @Test
    void dedupPlan_branchListsDetectAndKeeperRule() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons produits");
        assertFalse(plan.startsWith("Je n'ai pas identifié"),
                "La planification des doublons doit produire des étapes, pas un refus");
        assertTrue(plan.contains("detectDuplicateProductsForDeletion"));
        assertTrue(plan.contains("plus complet") || plan.contains("plus d'informations"),
                "Le plan doit énoncer la règle du gardien le plus complet");
    }

    @Test
    void dedupPlan_staysWithinRequestedEntityScope() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons clients dans la base");
        assertTrue(plan.startsWith("### Plan d'exécution proposé"),
                "Le plan doit être généré pour une demande de doublons");
        assertTrue(plan.contains("detectDuplicateClientsForDeletion"));
        assertTrue(plan.toLowerCase().contains("ligne de vente"),
                "La règle du gardien descend jusqu'à la ligne de vente");
        assertTrue(plan.contains("plus complet gagne") || plan.contains("plus d'informations"),
                "Le plan doit énoncer la règle du gardien le plus complet");
        assertFalse(plan.contains("detectDuplicateCategoriesForDeletion"),
                "Hors périmètre: la demande ne concerne QUE les clients, pas les catégories");
        assertFalse(plan.contains("detectDuplicateProductsForDeletion"),
                "Hors périmètre: la demande ne concerne QUE les clients, pas les produits");
        assertFalse(plan.contains("detectDuplicateVentesForDeletion"),
                "Hors périmètre: la demande ne concerne QUE les clients, pas les ventes");
        assertFalse(plan.contains("Créer le client"),
                "Une demande d'élimination de doublons ne doit pas déclencher la création de client");
    }

    @Test
    void dedupPlan_scopesMultiEntityRequestAndExcludesOthers() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons ventes et lignes de vente");
        assertTrue(plan.contains("detectDuplicateVentesForDeletion"));
        assertTrue(plan.contains("detectDuplicateLigneVentesForDeletion"));
        assertFalse(plan.contains("detectDuplicateTraisoreriesForDeletion"),
                "La trésorerie n'est pas dans le périmètre « ventes et lignes de vente »");
        assertFalse(plan.contains("detectDuplicateOperationsForDeletion"),
                "Les opérations ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateRecquisitionsForDeletion"),
                "Les réquisitions ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateProductionsForDeletion"),
                "Les productions ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateFacturesForDeletion"),
                "Les factures ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateMatieresForDeletion"),
                "Les matières ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateMatiereSkusForDeletion"),
                "Les matières SKU ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateDepotsForDeletion"),
                "Les dépôts ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateInventairesForDeletion"),
                "Les inventaires ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateClientOrganisationsForDeletion"),
                "Les organisations client ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateComptersForDeletion"),
                "Les comptes clients ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateTaxesForDeletion"),
                "Les taxes ne sont pas dans le périmètre");
        assertFalse(plan.contains("detectDuplicateTaxersForDeletion"),
                "Les lignes de taxation ne sont pas dans le périmètre");
    }

    @Test
    void dedupPlan_genericRequestCoversEverything() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine tous les doublons de la base");
        assertTrue(plan.contains("detectDuplicateClientsForDeletion"));
        assertTrue(plan.contains("detectDuplicateProductsForDeletion"));
        assertTrue(plan.contains("detectDuplicateSuppliersForDeletion"));
        assertTrue(plan.contains("detectDuplicateVentesForDeletion"));
        assertTrue(plan.contains("detectDuplicateTraisoreriesForDeletion"));
        assertTrue(plan.contains("detectDuplicateTaxersForDeletion"));
    }

    @Test
    void dedupPlan_productOnlyScopeKeepsPlanMinimal() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons sur les produits");
        assertTrue(plan.contains("detectDuplicateProductsForDeletion"));
        assertFalse(plan.contains("detectDuplicateClientsForDeletion"),
                "Uniquement les produits: pas de clients");
        assertFalse(plan.contains("detectDuplicateSuppliersForDeletion"),
                "Uniquement les produits: pas de fournisseurs");
        assertFalse(plan.contains("detectDuplicateVentesForDeletion"),
                "Uniquement les produits: pas de ventes");
    }

    @Test
    void dedupPlan_scopesTheObservedProductsAndRequisitionsRequest() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class},
                "Élimination des doublons pour les Produits et les Réquisitions.\n"
                        + "1. Détection des produits doublons via detectDuplicateProductsForDeletion.\n"
                        + "2. Détection des réquisitions doublons via detectDuplicateRecquisitionsForDeletion.");
        assertTrue(plan.contains("detectDuplicateProductsForDeletion"));
        assertTrue(plan.contains("detectDuplicateRecquisitionsForDeletion"));
        assertFalse(plan.contains("detectDuplicateClientsForDeletion"),
                "Hors périmètre: ni clients ni toute la chaîne, reçu: " + plan);
        assertFalse(plan.contains("detectDuplicateVentesForDeletion"),
                "Hors périmètre: pas de ventes, reçu: " + plan);
        assertFalse(plan.contains("detectDuplicateSuppliersForDeletion"),
                "Hors périmètre: pas de fournisseurs, reçu: " + plan);
    }

    @Test
    void dedupPlan_doesNotMixBusinessBranches() {
        String depenses = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons de dépenses");
        assertTrue(depenses.contains("detectDuplicateDepensesForDeletion"));
        assertFalse(depenses.contains("globalBusinessAnalysis"),
                "Une demande de doublons de dépenses ne doit pas déclencher l'analyse business");
        String livraisons = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "fusionne les doublons de livraisons");
        assertTrue(livraisons.contains("detectDuplicateLivraisonsForDeletion"));
        assertFalse(livraisons.contains("createStockForWarehouse"),
                "Une demande de doublons de livraisons ne doit pas déclencher la réception de stock");
    }

    @Test
    void dedupPlan_gateBlocksDeleteUntilPlanConfirmed() {        tools.setCurrentSessionId("test-plan-dedup-oui");
        String plan = tools.planExecution("élimine les doublons clients");
        assertTrue(plan.contains("detectDuplicateClientsForDeletion"));
        String refused = tools.deleteConfirmedDuplicateEntities("fake-batch", "1");
        assertTrue(refused.contains("attente de confirmation"),
                "La suppression doit être bloquée tant que le plan est en attente");
        String confirmed = tools.answerPlanExecution("oui");
        assertTrue(confirmed.contains("confirmé"));
        String opened = tools.deleteConfirmedDuplicateEntities("fake-batch", "1");
        assertTrue(opened.contains("Lot de doublons introuvable"),
                "Plan confirmé: le verrou doit s'ouvrir (lot inexistant, sans toucher la base)");
    }

    @Test
    void dedupPlan_gateRefusesDeleteWhenPlanRejected() {
        tools.setCurrentSessionId("test-plan-dedup-non");
        tools.planExecution("fusionne les doublons fournisseurs");
        String cancelled = tools.answerPlanExecution("non");
        assertTrue(cancelled.contains("annulé"));
        String refused = tools.deleteConfirmedDuplicateEntities("fake-batch", "1");
        assertTrue(refused.contains("refusé"),
                "Plan refusé: la suppression doit rester bloquée");
    }

    @Test
    void launchApprovedPlan_runsDeterministicDetectionsAfterConfirm() {
        tools.setCurrentSessionId("test-plan-launch-oui");
        String plan = tools.planExecution("élimine les doublons clients");
        assertTrue(plan.contains("detectDuplicateClientsForDeletion"));
        assertFalse(tools.hasApprovedDedupPlan(),
                "Plan en attente: aucun lancement déterministe avant approbation");
        assertNull(tools.launchApprovedPlan(),
                "Le lancement est impossible tant que le plan n'est pas confirmé");
        tools.decidePlan("oui");
        assertTrue(tools.hasApprovedDedupPlan(),
                "Plan confirmé: le lancement déterministe est disponible");
        String report = tools.launchApprovedPlan();
        assertTrue(report != null && (report.contains("Plan approuvé") || report.contains("aucun doublon")),
                "Le plan dédup confirmé doit produire un rapport d'exécution, reçu: " + report);
        assertFalse(tools.hasApprovedDedupPlan(),
                "La requête de plan doit être consommée après l'exécution");
    }

    @Test
    void launchApprovedPlan_respectsPlanScope() {
        tools.setCurrentSessionId("test-plan-launch-scope");
        String plan = tools.planExecution("élimine les doublons sur les produits");
        assertTrue(plan.contains("detectDuplicateProductsForDeletion"));
        assertFalse(plan.contains("detectDuplicateClientsForDeletion"),
                "Le plan doit être scopé aux produits uniquement, reçu: " + plan);
        tools.decidePlan("oui");
        String report = tools.launchApprovedPlan();
        assertTrue(report != null && report.contains("produits"),
                "Le rapport doit citer le périmètre produits, reçu: " + report);
        assertFalse(report.contains("clients"),
                "L'exécution doit rester sur le périmètre produits, reçu: " + report);
    }

    @Test
    void countDupEntries_countsPipeTableRows() {
        int n = (int) invokeInstance(tools, "countDupEntries", new Class<?>[]{String.class},
                "Clients doublons détectés.\nLot: dup-entity-abc\n\n"
                        + "|numero|groupe|uid|\n|---|---|---|\n"
                        + "|1|1|uid-a|\n|2|1|uid-b|\n|3|2|uid-c|\n");
        assertEquals(3, n);
    }

    @Test
    void launchApprovedPlan_refusedPlanIsNotLaunchable() {
        tools.setCurrentSessionId("test-plan-launch-non");
        tools.planExecution("fusionne les doublons fournisseurs");
        tools.decidePlan("non");
        assertFalse(tools.hasApprovedDedupPlan());
        assertNull(tools.launchApprovedPlan());
    }

    @Test
    void launchApprovedPlan_nonDedupPlanFallsBackToNoOp() {
        tools.setCurrentSessionId("test-plan-launch-stock");
        String plan = tools.planExecution("reçois la marchandise reçue ce matin au dépôt");
        assertTrue(plan.contains("createStockForWarehouse"),
                "Plan hors dédoublonnage attendu, reçu: " + plan);
        tools.decidePlan("oui");
        assertFalse(tools.hasApprovedDedupPlan(),
                "Un plan hors dédoublonnage ne doit jamais se lancer de façon déterministe");
        assertNull(tools.launchApprovedPlan());
    }

    @Test
    void mostCompleteProduit_prefersMoreFilledFields() {
        data.Produit sparse = new data.Produit();
        sparse.setUid("sparse-uid");
        sparse.setNomProduit("Savon");
        data.Produit complete = new data.Produit();
        complete.setUid("complete-uid");
        complete.setNomProduit("Savon");
        complete.setMarque("Multiphar");
        complete.setModele("B/20");
        complete.setTaille("20");
        complete.setCouleur("Blanc");
        complete.setCodebar("1234567890123");
        assertTrue(Util.dataCompletenessScore(complete)
                > Util.dataCompletenessScore(sparse));
        Object keeper = invokeStatic("mostCompleteProduit",
                new Class<?>[]{java.util.List.class},
                java.util.List.of(sparse, complete));
        assertTrue(keeper == complete, "Le gardien doit être l'élément le plus complet");
        Object tie = invokeStatic("mostCompleteProduit",
                new Class<?>[]{java.util.List.class},
                java.util.List.of(sparse, sparse));
        assertTrue(tie == sparse, "Ex-aequo: le premier est conservé");
    }

    private String entitySpecType(String input) {
        Object spec = invokeInstance(tools, "resolveEntitySpec", new Class<?>[]{String.class}, input);
        if (spec == null) {
            return null;
        }
        try {
            Method type = spec.getClass().getMethod("type");
            type.setAccessible(true);
            return (String) type.invoke(spec);
        } catch (Exception ex) {
            throw new RuntimeException("Lecture du type de spec impossible", ex);
        }
    }

    @Test
    void dedupIntent_keepsSaleAndExpenseWorkflowsAway() {
        assertTrue(Util.isDedupEliminationIntent("élimine les doublons clients"));
        assertTrue(Util.isDedupEliminationIntent("fusionne les doublons de dépenses"));
        assertTrue(Util.isDedupEliminationIntent("supprime les doublons produits"));
        assertFalse(Util.isDedupEliminationIntent("vente, 3, lait"));
        assertFalse(Util.isDedupEliminationIntent("dépense de carburant"));

        com.endeleya.ia.GratienSaleWorkflow sale = new com.endeleya.ia.GratienSaleWorkflow(null, null);
        assertFalse(sale.shouldHandle("élimine les doublons clients dans la base", java.util.List.of()),
                "La demande 'doublons clients' ne doit pas être détournée vers la vente");
        assertTrue(sale.shouldHandle("vente, 3, lait", null),
                "Une vente classique doit toujours passer par le workflow vente");

        com.endeleya.ia.GratienExpenseWorkflow expense = new com.endeleya.ia.GratienExpenseWorkflow(null, null);
        assertFalse(expense.shouldHandle("supprime les doublons de dépenses", java.util.List.of()),
                "La demande 'doublons de dépenses' ne doit pas être détournée vers le workflow dépenses");
    }

    @Test
    void planExecution_estUnOutilTerminalQuiArreteLaBoucleDOutils() throws Exception {
        Method plan = GratienTools.class.getMethod("planExecution", String.class);
        dev.langchain4j.agent.tool.Tool planTool = plan.getAnnotation(dev.langchain4j.agent.tool.Tool.class);
        assertNotNull(planTool, "@Tool manquant sur planExecution");
        assertEquals(dev.langchain4j.agent.tool.ReturnBehavior.IMMEDIATE, planTool.returnBehavior(),
                "planExecution doit terminer le tour apres UN SEUL appel (sinon Gratien boucle en preparation du plan)");

        Method answer = GratienTools.class.getMethod("answerPlanExecution", String.class);
        dev.langchain4j.agent.tool.Tool answerTool = answer.getAnnotation(dev.langchain4j.agent.tool.Tool.class);
        assertNotNull(answerTool, "@Tool manquant sur answerPlanExecution");
        assertEquals(dev.langchain4j.agent.tool.ReturnBehavior.TO_LLM, answerTool.returnBehavior(),
                "answerPlanExecution doit laisser la boucle continuer pour executer le plan confirme");
    }

    @Test
    void toolErrorText_estActionnableEtJamaisLePlaceholderQuiFaitBouclerLeModele() {
        String text = AiAgents.toolErrorText(null, null, "arguments invalides ou incomplets");
        assertTrue(text.contains("arguments invalides ou incomplets"),
                "Le message doit reprendre la raison lisible de l'echec");
        assertTrue(text.contains("Corrige l'appel"),
                "Le modele doit recevoir une consigne actionnable pour se corriger");
        assertFalse(text.contains("Réflexion en cours"),
                "Le placeholder 'Réflexion en cours...' laissait croire que l'outil tournait encore");
    }

    @Test
    void planPresentsPerimetreAsTableRowNotBlockquote() {
        String plan = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons produits");
        assertTrue(plan.contains("| — | **Périmètre** |"),
                "Le périmètre doit être une rangée du tableau markdown, reçu: " + plan);
        assertFalse(plan.contains("> **Périmètre**"),
                "Plus de citation hors tableau pour le périmètre, reçu: " + plan);
        assertTrue(plan.startsWith("### Plan d'exécution proposé"),
                "Le plan garde son titre dédié");
        assertTrue(plan.contains("| Étape | Outil / Workflow | Action |\n|---|---|---|"),
                "Le plan doit contenir l'en-tête du tableau markdown");
        assertTrue(plan.indexOf("| — | **Périmètre** |") > plan.indexOf("| Étape | Outil / Workflow | Action |"),
                "Le périmètre doit figurer comme première rangée du tableau");
    }

    @Test
    void sessionScope_neverReturnsToOutOfScopeEntities() {
        String first = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons produits");
        assertFalse(first.contains("detectDuplicateClientsForDeletion"));

        String generic = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine tous les doublons");
        assertTrue(generic.contains("detectDuplicateProductsForDeletion"),
                "La demande générique reste sur le périmètre produits: " + generic);
        assertFalse(generic.contains("detectDuplicateClientsForDeletion"),
                "Ne jamais revenir sur les clients sans demande explicite: " + generic);
        assertFalse(generic.contains("detectDuplicateVentesForDeletion"),
                "Ne jamais revenir sur les ventes sans demande explicite: " + generic);

        String clients = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "élimine les doublons clients");
        assertTrue(clients.contains("detectDuplicateClientsForDeletion"));
        assertFalse(clients.contains("detectDuplicateProductsForDeletion"),
                "Le périmètre de la question explicitement citée = clients uniquement");

        String generic2 = (String) invokeInstance(tools, "buildExecutionPlan",
                new Class<?>[]{String.class}, "nettoyage complet des doublons");
        assertTrue(generic2.contains("detectDuplicateProductsForDeletion"),
                "Union des entités déjà exprimées dans la conversation: " + generic2);
        assertTrue(generic2.contains("detectDuplicateClientsForDeletion"),
                "Union des entités déjà exprimées dans la conversation: " + generic2);
        assertFalse(generic2.contains("detectDuplicateVentesForDeletion"),
                "Les ventes n'ont jamais été demandées: " + generic2);
    }

    @Test
    void parseMergeReply_understandsDynamicConsignes() {
        Set<String> lots = Set.of("dup-entity-abc", "dup-prod-xyz");
        assertMergeMode("fusionnez tout", DedupMergeMode.ALL, null, false, null, null);
        assertMergeMode("fusez tout", DedupMergeMode.ALL, null, false, null, null);
        assertMergeMode("conserve le plus complet de chaque groupe", DedupMergeMode.ALL, null, false, null, null);
        assertMergeMode("tout", DedupMergeMode.ALL, null, false, null, null);

        assertMergeMode("gardez les numeros pairs", DedupMergeMode.PARITY, null, false, Boolean.TRUE, null);
        assertMergeMode("gardez les numeros impairs", DedupMergeMode.PARITY, null, false, Boolean.FALSE, null);
        assertMergeMode("supprimez les numeros pairs", DedupMergeMode.PARITY, null, false, Boolean.FALSE, null);
        assertMergeMode("supprimez les numeros impairs", DedupMergeMode.PARITY, null, false, Boolean.TRUE, null);
        assertMergeMode("les entrées paires", DedupMergeMode.PARITY, null, false, Boolean.TRUE, null);
        assertMergeMode("garde les numéros pairs svp", DedupMergeMode.PARITY, null, false, Boolean.TRUE, null);

        assertMergeMode("gardez 2 et 5", DedupMergeMode.NUMBERS, Set.of(2, 5), true, null, null);
        assertMergeMode("garder les numeros 2, 5", DedupMergeMode.NUMBERS, Set.of(2, 5), true, null, null);
        assertMergeMode("supprimez 2, 5", DedupMergeMode.NUMBERS, Set.of(2, 5), false, null, null);
        assertMergeMode("fusionnez 2, 5", DedupMergeMode.NUMBERS, Set.of(2, 5), false, null, null);
        assertMergeMode("conserver le numero 3", DedupMergeMode.NUMBERS, Set.of(3), true, null, null);
        assertMergeMode("lot dup-entity-abc -> 2, 5", DedupMergeMode.NUMBERS, Set.of(2, 5), false, null, Set.of("dup-entity-abc"));

        assertNull(parseMergeReply("bonjour, que fais-tu aujourd'hui ?", lots),
                "Un simple message de conversation n'est pas une consigne de fusion");
    }

    private GratienTools.DedupMergeInstruction parseMergeReply(String reply, Set<String> lots) {
        return (GratienTools.DedupMergeInstruction) invokeInstance(tools, "parseMergeInstruction",
                new Class<?>[]{String.class, java.util.Set.class}, reply, lots);
    }

    private void assertMergeMode(String reply, DedupMergeMode mode, Set<Integer> expectedNumbers,
            boolean expectedKeep, Boolean expectedKeepEven, Set<String> expectedLots) {
        GratienTools.DedupMergeInstruction i = parseMergeReply(reply,
                Set.of("dup-entity-abc", "dup-prod-xyz"));
        assertNotNull(i, "Consigne de fusion attendue pour: " + reply);
        assertEquals(mode, i.mode(), reply);
        if (expectedNumbers != null) {
            assertEquals(expectedNumbers, i.numbers(), reply);
            assertEquals(expectedKeep, i.keepNumbers(), reply);
        }
        if (expectedKeepEven != null) {
            assertEquals(expectedKeepEven, i.keepEven(), reply);
        }
        if (expectedLots != null) {
            assertEquals(expectedLots, Set.copyOf(i.lotIds()), reply);
        }
    }
}