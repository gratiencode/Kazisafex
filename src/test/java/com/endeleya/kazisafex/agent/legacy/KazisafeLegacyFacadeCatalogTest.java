package com.endeleya.kazisafex.agent.legacy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.endeleya.ia.GratienSwarmTools;
import com.endeleya.ia.GratienTools;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Vérifie que le catalogue de l'orchestrateur (KazisafeLegacyFacade) inclut bien les
 * nouveaux outils de dédoublonnage et le lanceur de workflows swarm, et exclut les
 * outils interactifs réservés au chat classique (planExecution/answerPlanExecution).
 */
class KazisafeLegacyFacadeCatalogTest {

    @Test
    void catalogIncludesNewToolsAndWorkflows() {
        List<LegacyToolDescriptor> descriptors = new ArrayList<>();
        KazisafeLegacyFacade.collectDescriptors(new GratienTools(), descriptors);
        KazisafeLegacyFacade.collectDescriptors(new GratienSwarmTools(), descriptors);

        List<String> names = descriptors.stream().map(LegacyToolDescriptor::name).toList();

        assertTrue(names.contains("detectDuplicateClientsForDeletion"),
                "les outils de dédoublonnage doivent être planifiables/exécutables par l'orchestrateur");
        assertTrue(names.contains("detectDuplicateRecquisitionsForDeletion"),
                "la détection des réquisitions doublons doit être exposée à l'orchestrateur");
        assertTrue(names.contains("deleteConfirmedDuplicateEntities"),
                "la fusion confirmée doit être exécutable par l'orchestrateur");
        assertTrue(names.contains("mergeDuplicateClients"),
                "la fusion automatique des clients doit rester planifiable");
        assertTrue(names.contains("delegateTask"),
                "le lanceur de workflows (invoice/sale/expense) doit être exposé à l'orchestrateur");

        assertFalse(names.contains("planExecution"),
                "planExecution est réservé au chat classique : à exclure du catalogue orchestrateur");
        assertFalse(names.contains("answerPlanExecution"),
                "answerPlanExecution est réservé au chat classique : à exclure du catalogue orchestrateur");
    }
}