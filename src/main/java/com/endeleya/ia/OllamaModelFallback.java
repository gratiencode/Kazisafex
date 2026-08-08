package com.endeleya.ia;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enveloppe multi-modeles avec bascule automatique: chaque appel essaie les
 * modeles dans l'ordre de configuration et renvoie la premiere reponse obtenue.
 *
 * Sur le serveur distant, gemma4:31b-cloud ne supporte pas les images (HTTP 500
 * "Internal Server Error" des que le corps contient une image) alors que
 * minimax-m3:cloud les accepte. En cas d'echec du premier modele (500, modele
 * non multimodal, indisponible, reponse vide...), on reessaie avec le suivant
 * pour que les workflows (facture, depense, vente, image-produits, vision)
 * restent utilisables sans intervention.
 */
public final class OllamaModelFallback {

    private static final Logger LOGGER = Logger.getLogger(OllamaModelFallback.class.getName());
    /**
     * Nombre de jetons de reponse alloue a chaque modele. Les schemas JSON de
     * factures (fournisseur + lignes) peuvent etre longs; sans limite explicite,
     * le serveur tronque la reponse (num_predict par defaut souvent 2048) et le
     * JSON devient incomplet => extraction "non parsable".
     */
    private static final int DEFAULT_NUM_PREDICT = 8192;
    private final List<ChatModel> models;
    private final List<String> modelNames;

    /**
     * Construit un modele OllamaChatModel par nom, dans l'ordre de bascule.
     *
     * @param temperature temperature appliquee a chaque modele
     * @param timeout delai d'attente applique a chaque modele
     * @param modelNames modeles dans l'ordre de priorite (le premier est le principal)
     */
    public OllamaModelFallback(double temperature, Duration timeout, String... modelNames) {
        this(temperature, DEFAULT_NUM_PREDICT, timeout, modelNames);
    }

    /**
     * Variante avec nombre de jetons de reponse explicite.
     *
     * @param numPredict nombre maximal de jetons de reponse (voir DEFAULT_NUM_PREDICT)
     */
    public OllamaModelFallback(double temperature, int numPredict, Duration timeout, String... modelNames) {
        this(buildModels(temperature, numPredict, timeout, modelNames), List.of(modelNames));
    }

    /** Constructeur de test: permet d'injecter des modeles simules. */
    OllamaModelFallback(List<ChatModel> models, List<String> modelNames) {
        this.models = List.copyOf(models);
        this.modelNames = List.copyOf(modelNames);
    }

    private static List<ChatModel> buildModels(double temperature, int numPredict, Duration timeout, String... modelNames) {
        List<ChatModel> built = new ArrayList<>();
        for (String modelName : modelNames) {
            if (modelName == null || modelName.isBlank()) {
                continue;
            }
            built.add(OllamaChatModel.builder()
                    .baseUrl(AiAgents.OLLAMA_BASE_URL)
                    .modelName(modelName)
                    .temperature(temperature)
                    .numPredict(numPredict)
                    .timeout(timeout)
                    .build());
        }
        return built;
    }

    /**
     * Envoie la requete au premier modele operationnel et renvoie le texte de la
     * reponse. Si tous les modeles echouent, la derniere erreur est relevee.
     */
    public String chat(ChatRequest request) {
        Throwable lastError = null;
        for (int i = 0; i < models.size(); i++) {
            try {
                String answer = models.get(i).chat(request).aiMessage().text();
                if (answer != null) {
                    if (i > 0) {
                        LOGGER.log(Level.INFO, "Extraction reussie via le modele de secours {0} "
                                + "(le modele principal {1} a echoue).",
                                new Object[]{modelNames.get(i), modelNames.get(0)});
                    }
                    return answer;
                }
                lastError = new IllegalStateException("Reponse vide du modele " + modelNames.get(i));
            } catch (Exception ex) {
                lastError = ex;
                LOGGER.log(Level.WARNING, "Modele {0} en echec, bascule sur le modele suivant.",
                        modelNames.get(i));
            }
        }
        if (lastError == null) {
            throw new IllegalStateException("Aucun modele d'extraction configure.");
        }
        throw lastError instanceof RuntimeException re ? re : new RuntimeException(lastError);
    }
}
