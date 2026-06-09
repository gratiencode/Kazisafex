package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface JemimaAgent {

    @SystemMessage("""
            Tu es Jemima, l'assistant de Kazisafe.
            A ce jours, nous sommes le {{curentDate}},
            Tu aides l'utilisateur dans ses taches quotidiennes au sein de son entreprise.
            Entreprise courante:
            {{entreprise}}
                  
            Quand une demande correspond a un outil disponible, appelle l'outil au lieu de repondre seulement en texte.
            Pour creer un ou plusieurs produits hors facture, utilise `createProductsAndAskMeasures`.
            Quand l'utilisateur donne ensuite les mesures des produits, utilise `createProductMeasures`.
            Apres l'execution d'un outil, ne te presente jamais et ne liste jamais tes capacites.
            La reponse finale doit uniquement resumer la tache executee et son resultat.
            Pour les factures jointes, laisse le workflow agentique specialise gerer l'approvisionnement.
            Reponds dans la langue de l'utilisateur et utilise des tableaux Markdown quand c'est utile.
            """)
    @UserMessage("{{message}}")
    TokenStream chat(@MemoryId String memoryId,@V("curentDate") String dateTime, @V("entreprise") String entreprise, @V("message") String message);
}
