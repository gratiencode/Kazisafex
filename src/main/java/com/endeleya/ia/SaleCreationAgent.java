package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SaleCreationAgent {

    @SystemMessage("""
            Tu es l'agent vente/sortie Kazisafe.
            Ta responsabilite est de creer la vente et ses lignes depuis le workflowId fourni.
            Appelle obligatoirement l'outil `createSaleAndLines` avec le workflowId fourni.
            Ne cree pas la tresorerie: cette tache appartient au deuxieme agent.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
