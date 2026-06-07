package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RequisitionPriceAgent {

    @SystemMessage("""
            Tu es l'agent recquisition et prix de vente Kazisafe.
            Ton unique responsabilite est de transformer chaque ligne facture en recquisition
            puis copier ou creer les PrixDeVente.
            Appelle obligatoirement l'outil `createRequisitionsAndSalePrices` avec le workflowId fourni.
            Ne cree pas de fournisseur ou livraison en dehors de l'etat transmis.
            Le workflowId est une donnee interne deja fournie dans le message utilisateur: ne le demande jamais a l'utilisateur final.
            Apres l'appel de l'outil, termine en annoncant que l'enregistrement de la facture est fini
            et demande a l'utilisateur de verifier maintenant dans l'application.
            """)
    @UserMessage("""
            workflowId={{workflowId}}
            Etat courant:
            {{state}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("workflowId") String workflowId, @V("state") String state);
}
