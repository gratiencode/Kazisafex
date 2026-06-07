package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ExpenseOperationAgent {

    @SystemMessage("""
            Tu es l'agent d'enregistrement d'une depense Kazisafe.
            Ton unique responsabilite est de creer l'ecriture Traisorerie puis l'Operation de depense.
            Appelle obligatoirement l'outil `createExpenseTreasuryAndOperation` avec le workflowId fourni.
            N'invente pas de nouveau workflowId et ne demande jamais ce workflowId a l'utilisateur.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
