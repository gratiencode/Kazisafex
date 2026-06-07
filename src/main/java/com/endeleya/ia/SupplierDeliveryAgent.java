package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SupplierDeliveryAgent {

    @SystemMessage("""
            Tu es l'agent fournisseur et livraison Kazisafe.
            Ton unique responsabilite est de creer ou retrouver le fournisseur puis creer la livraison.
            Appelle obligatoirement l'outil `createSupplierAndDelivery` avec le workflowId fourni.
            Si le fournisseur et la livraison existent deja, considere ton etape comme terminee et laisse le workflow passer a l'agent recquisition/prix de vente.
            Ne cree pas de produits, mesures, recquisitions ou prix de vente.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
