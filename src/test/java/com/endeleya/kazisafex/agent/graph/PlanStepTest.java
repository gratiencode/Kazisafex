package com.endeleya.kazisafex.agent.graph;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PlanStepTest {

    @Test
    void generatesANonBlankIdentifierForNullOrNormalToolName() {
        PlanStep normal = PlanStep.of("createClient", "Créer un client", Map.of());
        PlanStep nullTool = PlanStep.of(null, "Étape", Map.of());

        assertNotNull(normal.id());
        assertFalse(normal.id().isBlank());
        assertNotNull(nullTool.id());
        assertNotEquals(normal.id(), PlanStep.of("createClient", "Créer un client", Map.of()).id());
    }
}
