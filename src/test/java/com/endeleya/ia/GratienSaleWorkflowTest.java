package com.endeleya.ia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GratienSaleWorkflowTest {

    private GratienTools mockTools;
    private SaleAgentRunner mockRunner;
    private GratienSaleWorkflow workflow;

    @BeforeEach
    public void setUp() {
        mockTools = mock(GratienTools.class);
        mockRunner = mock(SaleAgentRunner.class);
        workflow = new GratienSaleWorkflow(mockTools, mockRunner);
    }

    @Test
    public void testCashSaleWorkflow() {
        // Step 1: Start workflow
        String response1 = workflow.handle("vente, 5, lait", null);
        System.out.println("CASH_STEP1: " + response1);
        assertTrue(response1.contains("Veuillez indiquer le type de paiement"));

        // Step 2: Answer cash
        String response2 = workflow.handle("cash", null);
        System.out.println("CASH_STEP2: " + response2);
        // Should skip currency (uses default), due date, client info, and go to summary confirmation
        assertTrue(response2.contains("Résumé de la vente"));
        assertTrue(response2.contains("Type de paiement: CASH"));
        assertTrue(response2.contains("lait"));
        assertTrue(response2.contains("5.0"));

        // Step 3: Confirm
        when(mockRunner.run(any())).thenReturn("Vente enregistrée avec succès");
        String response3 = workflow.handle("oui", null);
        System.out.println("CASH_STEP3: " + response3);
        assertEquals("Vente enregistrée avec succès", response3);
    }

    @Test
    public void testCreditSaleWorkflow() {
        // Step 1: Start workflow
        String response1 = workflow.handle("vente, 10, lait", null);
        System.out.println("CREDIT_STEP1: " + response1);
        assertTrue(response1.contains("Veuillez indiquer le type de paiement"));

        // Step 2: Answer credit
        String response2 = workflow.handle("credit", null);
        System.out.println("CREDIT_STEP2: " + response2);
        // Credit requires client info. Let's make sure it transitions to client info prompt.
        assertTrue(response2.contains("Veuillez indiquer le nom et le numéro de téléphone du client"));

        // Step 3: Answer client info
        String response3 = workflow.handle("Jean Dupont, +243812345678", null);
        System.out.println("CREDIT_STEP3: " + response3);
        // Due date defaults to 30 days, currency defaults to USD. Should now show summary.
        assertTrue(response3.contains("Résumé de la vente"));
        assertTrue(response3.contains("Type de paiement: CREDIT"));
        assertTrue(response3.contains("Client: Jean Dupont (+243812345678)"));

        // Step 4: Confirm
        when(mockRunner.run(any())).thenReturn("Vente crédit enregistrée");
        String response4 = workflow.handle("oui", null);
        System.out.println("CREDIT_STEP4: " + response4);
        assertEquals("Vente crédit enregistrée", response4);
    }

    @Test
    public void testPartialSaleWorkflow() {
        // Step 1: Start workflow
        String response1 = workflow.handle("vente, 3, lait", null);
        System.out.println("PARTIAL_STEP1: " + response1);
        assertTrue(response1.contains("Veuillez indiquer le type de paiement"));

        // Step 2: Answer partial
        String response2 = workflow.handle("partial", null);
        System.out.println("PARTIAL_STEP2: " + response2);
        // Partial requires client info first
        assertTrue(response2.contains("Veuillez indiquer le nom et le numéro de téléphone du client"));

        // Step 3: Answer client info
        String response3 = workflow.handle("Papa Wemba, +243999999999", null);
        System.out.println("PARTIAL_STEP3: " + response3);
        // Now it requires cash portion
        assertTrue(response3.contains("Veuillez indiquer le paiement cash effectué"));

        // Step 4: Answer cash portion (50%)
        String response4 = workflow.handle("50%", null);
        System.out.println("PARTIAL_STEP4: " + response4);
        assertTrue(response4.contains("Résumé de la vente"));
        assertTrue(response4.contains("Paiement partiel: 50.0%"));

        // Step 5: Confirm
        when(mockRunner.run(any())).thenReturn("Vente partielle enregistrée");
        String response5 = workflow.handle("oui", null);
        System.out.println("PARTIAL_STEP5: " + response5);
        assertEquals("Vente partielle enregistrée", response5);
    }

    @Test
    public void testCancelWorkflow() {
        workflow.handle("vente, 3, lait", null);
        String cancelResponse = workflow.handle("non", null);
        assertEquals("D'accord, la vente n'est pas enregistrée.", cancelResponse);
    }
}
