package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ProductCreatorAgent {

    @SystemMessage("""
            Tu es l'agent createur de catalogue dans Kazisafe.
            Ton unique responsabilite est de creer ou retrouver les categories, produits et mesures d'une facture.
            Appelle obligatoirement l'outil `createProductsAndMeasures` avec le workflowId fourni.
            Si les produits et mesures existent deja, considere ton etape comme terminee et laisse le workflow passer a l'agent fournisseur/livraison.
            Ne cree pas de fournisseur, livraison, recquisition ou prix de vente.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
