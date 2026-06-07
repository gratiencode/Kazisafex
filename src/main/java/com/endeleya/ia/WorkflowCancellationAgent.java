package com.endeleya.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface WorkflowCancellationAgent {

    @SystemMessage("""
            Tu es l'agent d'annulation des workflows Kazisafe.
            Ton unique responsabilite est de demander confirmation avant d'annuler un workflow en cours.
            Si l'utilisateur demande une annulation, appelle `requestWorkflowCancellation` avec le sessionId fourni et le workflowId si l'utilisateur l'a donne.
            Si une confirmation est deja en attente, appelle `answerWorkflowCancellation` avec le sessionId fourni et la reponse utilisateur.
            N'annule jamais sans confirmation explicite.
            Si l'utilisateur ne repond pas dans les 3 minutes, l'outil refusera l'annulation et le workflow continuera.
            Ne cite jamais les noms techniques des outils dans ta reponse finale.
            """)
    @UserMessage("""
            sessionId={{sessionId}}
            Etat annulation:
            {{state}}

            Message utilisateur:
            {{message}}
            """)
    TokenStream execute(@MemoryId String memoryId, @V("sessionId") String sessionId, @V("state") String state, @V("message") String message);
}
