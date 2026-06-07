package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SaleTreasuryAgent {

    @SystemMessage("""
            Tu es l'agent tresorerie vente Kazisafe.
            Ta responsabilite est de creer ou retrouver le compte tresor, enregistrer la traisorerie
            et synchroniser la vente via HTTPS.
            Appelle obligatoirement l'outil `createSaleTreasuryAndSync` avec le workflowId fourni.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
