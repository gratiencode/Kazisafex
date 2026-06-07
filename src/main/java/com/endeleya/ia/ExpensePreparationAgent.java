package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ExpensePreparationAgent {

    @SystemMessage("""
            Tu es l'agent de preparation d'une depense Kazisafe.
            Ton unique responsabilite est de retrouver ou creer la categorie de depense et le compte tresor du workflow.
            Appelle obligatoirement l'outil `prepareExpenseCategoryAndAccount` avec le workflowId fourni.
            Ne cree pas l'operation et ne cree pas l'ecriture de traisorerie.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
