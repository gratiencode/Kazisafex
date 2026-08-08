package com.endeleya.ia;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Outil de delegation swarm reserve a l'assistant principal Gratien.
 * <p>
 * Permet a Gratien, pendant une conversation, de lancer une tache vers un
 * sous-agent specialise (approvisionnement, vente, depense) ou vers un agent
 * d'etape d'un workflow en cours (si un workflowId est fourni), puis de reprendre
 * la main avec le resultat, a la facon du mode swarm (agent principal + equipe
 * de sous-agents). Les sous-agents ne recoivent pas cet outil afin d'eviter
 * toute delegation recursive.
 * </p>
 */
public class GratienSwarmTools {

    @Tool("Délègue une tâche à un sous-agent spécialisé (mode swarm). "
            + "agent: 'invoice' (approvisionnement/facture), 'sale' (vente/sortie) ou 'expense' (dépense) pour "
            + "lancer le workflow multi-agents complet ; ou un agent d'étape (product_creator_agent, "
            + "supplier_delivery_agent, sale_creation_agent, sale_treasury_agent, "
            + "expense_preparation_agent, expense_operation_agent) si un workflowId en cours est fourni. "
            + "Les sous-agents ne servent qu'à créer les objets parents sans dépendance supérieure "
            + "(catalogue, fournisseur/livraison) ; les éléments à dépendance directe "
            + "(réquisitions, prix de vente) sont créés un à un directement par Gratien. "
            + "task: description détaillée de la tâche à réaliser. workflowId (optionnel): identifiant interne "
            + "d'un workflow déjà démarré. L'outil renvoie le résultat du sous-agent que tu dois présenter "
            + "à l'utilisateur.")
    public String delegateTask(
            @P("agent") String agent,
            @P("task") String task,
            @P("workflowId") String workflowId) {
        return AiAgents.getInstance().runSwarmDelegate(agent, task, workflowId);
    }
}
