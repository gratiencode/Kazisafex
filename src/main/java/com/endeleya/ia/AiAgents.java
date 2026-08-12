package com.endeleya.ia;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import tools.SyncEngine;
import tools.SyncLogger;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public final class AiAgents {

    /**
     * Contournement du bug LangChain4j 1.5.0 : quand l'argument parsing d'un
     * outil échoue, ToolService.executeWithErrorHandling appelle le handler
     * avec une cause null (ToolArgumentsException(String) a une cause null),
     * et le handler par défaut fait error.getMessage() => NullPointerException
     * qui tue le thread de streaming. Ces handlers null-safe renvoient un
     * message lisible à la place, et le modèle peut se corriger.
     */
    private static final ToolArgumentsErrorHandler NULL_SAFE_TOOL_ARGUMENTS_ERROR_HANDLER =
            (error, context) -> ToolErrorHandlerResult.text(
                    safeErrorMessage(error, "Réflexion en cours..."));
    private static final ToolExecutionErrorHandler NULL_SAFE_TOOL_EXECUTION_ERROR_HANDLER =
            (error, context) -> ToolErrorHandlerResult.text(
                    safeErrorMessage(error, "Réflexion en cours..."));

    private static String safeErrorMessage(Throwable error, String fallback) {
        if (error == null) {
            return fallback;
        }
        // Les erreurs de chargement de classe (dépendance absente du classpath)
        // ont un message utile : on l'explicite en français pour l'IA.
        if (error instanceof NoClassDefFoundError || error instanceof ClassNotFoundException) {
            String missing = error.getMessage();
            if (missing == null || missing.isBlank()) {
                missing = error.getClass().getSimpleName();
            }
            String detail = "Classe manquante à l'exécution: " + missing
                    + " (dépendance absente du classpath). ";
            SyncLogger.getInstance().log(error, "Classe manquante : " + missing);
            return detail;
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            // Message vide : on remonte la chaîne des causes pour trouver la vraie
            // raison. Si aucune n'existe, on renvoie le message fixe et lisible
            // (jamais de texte technique ni le nom de l'outil).
            message = firstNonBlankCause(error);
            if (message == null || message.isBlank()) {
                SyncLogger.getInstance().log(error, "Echec de l'outil sans detail exploitable : " + fallback);
                return fallback;
            }
            SyncLogger.getInstance().log(error, "Echec de l'outil (message vide, cause utilisee) : " + fallback);
            return message;
        }
        SyncLogger.getInstance().log(error, "Echec de l'outil (cause complete) : " + fallback);
        return message;
    }

    private static String firstNonBlankCause(Throwable error) {
        Throwable current = error;
        int depth = 0;
        while (current != null && depth < 10) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
            current = current.getCause();
            depth++;
        }
        return null;
    }

    public static final String OLLAMA_BASE_URL = "https://ai.kazisafe.com";
    /**
     * Modele unique de Gratien, force en dur: gemma4:31b-cloud.
     * Aucun override (propriete/variable d'environnement) n'est applique.
     */
    public static final String MODEL_NAME = "gemma4:31b-cloud";
    /**
     * Modele multimodal (vision) pour les images jointes (factures, reçus,
     * listes de produits): gemma4:31b-cloud et gpt-oss:120b-cloud renvoient une
     * erreur HTTP 500 / "this model does not support image input" des que le
     * corps de la requete contient une image. minimax-m3:cloud est le seul
     * modele du serveur qui accepte les images et extrait les donnees en JSON.
     */
    public static final String VISION_MODEL_NAME = "minimax-m3:cloud";
    /**
     * Memoire de Gratien : 40 messages maximum, applique a Redis comme au
     * fallback InMemoryStorage. Des que la limite est atteinte, le contexte est
     * compacte en un resume (message numero 1) suivi des derniers messages.
     */
    private static final int MAX_MEMORY_MESSAGES = 40;
    private static final int COMPACTION_THRESHOLD = 10;
    private static final String COMPACTION_MARKER = "[Contexte compacte] ";
    private static final String SUMMARY_SYSTEM_PROMPT = """
            Tu es l'agent de memoire de Gratien, assistant de Kazisafe.
            Resume en francais la conversation ci-dessous en conservant les faits importants:
            entreprise et utilisateur, produits, prix, fournisseurs, commandes, ventes, depenses,
            decisions et actions deja realisees, et tout detail necessaire pour poursuivre sans relire l'historique.
            Ne reponds que par le resume, sans introduction ni commentaire.
            """;
    private static final Logger LOGGER = Logger.getLogger(AiAgents.class.getName());
    private static final AiAgents INSTANCE = new AiAgents();

    private final Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    private final GratienTools GratienTools;
    private final GratienInvoiceWorkflow invoiceWorkflow;
    private final GratienProductImageWorkflow productImageWorkflow;
    private final GratienSaleWorkflow saleWorkflow;
    private final GratienExpenseWorkflow expenseWorkflow;
    private final RedisMemoryStore memoryStore;
    private final ChatMemoryProvider memoryProvider;
    private final GratienAgent assistant;
    private final ProductCreatorAgent productCreatorAgent;
    private final SupplierDeliveryAgent supplierDeliveryAgent;
    private final SaleCreationAgent saleCreationAgent;
    private final SaleTreasuryAgent saleTreasuryAgent;
    private final ExpensePreparationAgent expensePreparationAgent;
    private final ExpenseOperationAgent expenseOperationAgent;
    private final WorkflowCancellationAgent workflowCancellationAgent;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean streamingActive = new AtomicBoolean(false);
    private final AtomicBoolean compactionPending = new AtomicBoolean(false);
    private final AtomicBoolean compacting = new AtomicBoolean(false);
    private final Object compactionLock = new Object();
    private volatile Consumer<String> compactionSignal;
    private volatile Consumer<String> progressSignal;
    private volatile ChatModel summaryModel;
    private volatile String sessionId = "anonymous";
    private volatile String lastSessionId;
    private volatile PendingInvoiceIntent pendingInvoiceIntent;
    private final Map<String, List<File>> lastDocumentAttachments = new ConcurrentHashMap<>();
    private final Map<String, Long> lastDocumentTurn = new ConcurrentHashMap<>();
    private final Map<String, Long> messageTurn = new ConcurrentHashMap<>();

    private AiAgents() {
        // GratienTools est partage avec le workflow pour garder un seul point d'acces aux actions base/metier.
        this.GratienTools = new GratienTools();
        // La memoire Redis garde le contexte par entreprise/utilisateur avec fallback local.
        // La meme limite est appliquee sur Redis et sur InMemoryStorage (fallback).
        this.memoryStore = new RedisMemoryStore(MAX_MEMORY_MESSAGES);
        this.memoryProvider = memoryProvider(memoryStore);
        StreamingChatModel oschatmodel = OllamaStreamingChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName(MODEL_NAME)
                .temperature(0.25)
                .returnThinking(true)
                .timeout(Duration.ofMinutes(5))
                .build();
        // Pattern LangChain4j demande: proxy agent + function calling via GratienTools + memoire.
        // GratienSwarmTools (delegation vers les sous-agents) est reserve a l'assistant principal.
        this.assistant = AiServices.builder(GratienAgent.class)
                .streamingChatModel(oschatmodel)
                .tools(GratienTools, new GratienSwarmTools())
                .chatMemoryProvider(memoryProvider)
                .toolArgumentsErrorHandler(NULL_SAFE_TOOL_ARGUMENTS_ERROR_HANDLER)
                .toolExecutionErrorHandler(NULL_SAFE_TOOL_EXECUTION_ERROR_HANDLER)
                .build();
        this.productCreatorAgent = buildAgent(ProductCreatorAgent.class, oschatmodel);
        this.supplierDeliveryAgent = buildAgent(SupplierDeliveryAgent.class, oschatmodel);
        this.saleCreationAgent = buildAgent(SaleCreationAgent.class, oschatmodel);
        this.saleTreasuryAgent = buildAgent(SaleTreasuryAgent.class, oschatmodel);
        this.expensePreparationAgent = buildAgent(ExpensePreparationAgent.class, oschatmodel);
        this.expenseOperationAgent = buildAgent(ExpenseOperationAgent.class, oschatmodel);
        this.workflowCancellationAgent = buildAgent(WorkflowCancellationAgent.class, oschatmodel);
        // Le workflow recoit les tools afin que les agents puissent creer livraison, recquisitions et prix.
        this.invoiceWorkflow = new GratienInvoiceWorkflow(GratienTools, this::runInvoiceAgents);
        this.productImageWorkflow = new GratienProductImageWorkflow(GratienTools);
        this.saleWorkflow = new GratienSaleWorkflow(GratienTools, this::runSaleAgents);
        this.expenseWorkflow = new GratienExpenseWorkflow(GratienTools, this::runExpenseAgents);
    }

    public static AiAgents getInstance() {
        return INSTANCE;
    }

    public GratienTools getGratienTools() {
        return GratienTools;
    }

    /**
     * Re-teste Redis apres un bootstrap (install/demarrage) afin que la memoire
     * de Gratien bascule sur Redis des qu'il est disponible.
     */
    public void recheckRedisMemory() {
        memoryStore.recheck();
        LOGGER.log(Level.INFO, "Memoire Gratien apres bootstrap: redis="
                + memoryStore.isRedisAvailable());
    }

    public void startForCurrentSession() {
        String enterprise = pref.get("eUid", "unknown-enterprise");
        String user = pref.get("userid", pref.get("operator", pref.get("uname", "unknown-user")));
        startForUser(user, enterprise);
    }

    public void startForUser(String userId, String entrepriseId) {
        String user = safe(userId, "unknown-user");
        String enterprise = safe(entrepriseId, pref.get("eUid", "unknown-enterprise"));
        String previous = lastSessionId;
        sessionId = enterprise + ":" + user;
        if (previous != null && !previous.equals(sessionId)) {
            // Changement d'entreprise/utilisateur: on purge les dialogues de workflow en
            // cours pour ne jamais reprendre le contexte d'une autre session (draft facture,
            // vente, dépense, image-produits, clarification de document).
            invoiceWorkflow.clearPendingWorkflow();
            saleWorkflow.clearPendingWorkflow();
            expenseWorkflow.clearPendingWorkflow();
            productImageWorkflow.clearPendingWorkflow();
            pendingInvoiceIntent = null;
            lastDocumentAttachments.remove(previous);
            lastDocumentTurn.remove(previous);
            messageTurn.remove(previous);
        }
        lastSessionId = sessionId;
        if (started.compareAndSet(false, true)) {
            appendMemory("system", "AiAgents demarre a " + Instant.now()
                    + ", redis=" + memoryStore.isRedisAvailable()
                    + ", entreprise=" + enterprise
                    + ", utilisateur=" + user);
        }
    }

    public boolean hasPendingWorkflowCancellationRequest() {
        startForCurrentSession();
        return GratienTools.hasPendingWorkflowCancellation(sessionId);
    }

    public boolean hasPendingInvoiceIntentClarification() {
        startForCurrentSession();
        PendingInvoiceIntent pending = pendingInvoiceIntent;
        return pending != null && sessionId.equals(pending.sessionId());
    }

    public boolean shouldClarifyInvoiceIntent(String question, List<File> attachments) {
        startForCurrentSession();
        if (!hasImageAttachment(attachments)) {
            return false;
        }
        String value = normalize(question);
        if (isSupplyIntent(value) || isSaleIntent(value) || isExpenseIntent(value) || isProductIntent(value)) {
            return false;
        }
        return value.isBlank()
                || value.contains("facture")
                || value.contains("recu")
                || value.contains("reçu")
                || value.contains("document")
                || value.contains("piece jointe")
                || value.contains("pièce jointe");
    }

    public String askInvoiceIntentClarification(String question, List<File> attachments) {
        startForCurrentSession();
        List<File> storedAttachments = attachments == null ? List.of() : new ArrayList<>(attachments);
        pendingInvoiceIntent = new PendingInvoiceIntent(sessionId, safe(question, ""), storedAttachments);
        String prompt = """
                Avant de traiter ce document, précisez la nature de la tâche:

                1. `approvisionnement` ou `entrées` pour une entrée en stock
                2. `vente` ou `sortie` pour une sortie de stock
                3. `dépense` pour enregistrer une dépense

                Répondez avec l'un de ces mots pour que Gratien lance le bon workflow.
                """;
        appendMemory("user", safe(question, "[document joint sans intention précisée]"));
        appendMemory("assistant", prompt);
        return prompt;
    }

    public String resolveInvoiceIntentClarification(String answer) {
        startForCurrentSession();
        PendingInvoiceIntent pending = pendingInvoiceIntent;
        if (pending == null || !sessionId.equals(pending.sessionId())) {
            return "Je n'ai pas de document en attente de clarification. Joignez à nouveau la facture ou le reçu.";
        }
        String value = normalize(answer);
        if (value.contains("annule") || value.contains("annuler") || value.equals("non")) {
            pendingInvoiceIntent = null;
            appendMemory("user", safe(answer, ""));
            appendMemory("assistant", "Traitement du document annulé avant workflow.");
            return "D'accord, je ne traite pas ce document.";
        }
        String originalQuestion = pending.question().isBlank()
                ? "Document joint confirmé par l'utilisateur: " + safe(answer, "")
                : pending.question() + "\n\nPrécision utilisateur: " + safe(answer, "");
        List<File> attachments = pending.attachments();
        pendingInvoiceIntent = null;
        if (isSupplyIntent(value)) {
            return orchestrateInvoice(originalQuestion + "\nTâche: approvisionnement / entrée en stock.", attachments);
        }
        if (isSaleIntent(value)) {
            return orchestrateSale(originalQuestion + "\nTâche: vente / sortie de stock.", attachments);
        }
        if (isExpenseIntent(value)) {
            return orchestrateExpense(originalQuestion + "\nTâche: dépense.", attachments);
        }
        if (isProductIntent(value)) {
            return orchestrateProductImage(originalQuestion + "\nTâche: liste de produits à approvisionner.", attachments);
        }
        // Reponse non reconnue: on ne boucle jamais indefiniment. On libere la demande
        // en attente et on laisse l'utilisateur reformuler avec l'un des mots attendus.
        return "Je n'ai pas reconnu la nature du document dans votre réponse. "
                + "Reformulez avec l'un de ces mots: `approvisionnement`/`entrées`, "
                + "`vente`/`sortie`, `dépense`, ou `produit`/`liste`.";
    }

    public boolean shouldHandleInvoice(String question, List<File> attachments) {
        return invoiceWorkflow.shouldHandle(question, resolvedDocumentAttachments(attachments));
    }

    public boolean shouldHandleProductImage(String question, List<File> attachments) {
        return productImageWorkflow.shouldHandle(question, attachments);
    }

    public boolean shouldHandleSale(String question, List<File> attachments) {
        return saleWorkflow.shouldHandle(question, resolvedDocumentAttachments(attachments));
    }

    public boolean shouldHandleExpense(String question, List<File> attachments) {
        return expenseWorkflow.shouldHandle(question, resolvedDocumentAttachments(attachments));
    }

    /**
     * Memoire de session: chaque message incremente un compteur de tour; si le
     * message porte une image, elle est memorisee avec le numero du tour. Ainsi le
     * tour suivant peut reprendre ce document sans que l'utilisateur ne le re-joigne
     * (ex: image de facture puis "Faites en un approvisionnement").
     */
    public void noteDocumentMessage(List<File> attachments) {
        startForCurrentSession();
        long turn = messageTurn.merge(sessionId, 1L, Long::sum);
        if (hasImageAttachment(attachments)) {
            lastDocumentAttachments.put(sessionId, new ArrayList<>(attachments));
            lastDocumentTurn.put(sessionId, turn);
        }
    }

    /**
     * Resolution des pieces jointes d'un message: les pieces jointes courantes si
     * presentes, sinon le document vu au tour precedent uniquement. La fraicheur
     * est limitee au tour immediatement precedent pour ne jamais reprendre un
     * document oublie dans une conversation plus longue.
     */
    private List<File> resolvedDocumentAttachments(List<File> current) {
        startForCurrentSession();
        if (hasImageAttachment(current)) {
            return current;
        }
        Long currentTurn = messageTurn.get(sessionId);
        Long docTurn = lastDocumentTurn.get(sessionId);
        if (currentTurn != null && docTurn != null && docTurn == currentTurn - 1) {
            List<File> cached = lastDocumentAttachments.get(sessionId);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        }
        return current == null ? List.of() : current;
    }

    private boolean hasImageAttachment(List<File> attachments) {
        return attachments != null && attachments.stream().anyMatch(this::isImageAttachment);
    }

    private boolean isImageAttachment(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName() == null ? "" : file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".webp") || name.endsWith(".bmp");
    }

    private boolean isSupplyIntent(String value) {
        return value.contains("approvisionnement")
                || value.contains("approvisionner")
                || value.contains("entree")
                || value.contains("entrees")
                || value.contains("entrée")
                || value.contains("entrées")
                || value.contains("stock entrant")
                || value.contains("achat fournisseur")
                || value.contains("fournisseur")
                || value.contains("livraison")
                || value.contains("requisition")
                || value.contains("réquisition");
    }

    private boolean isSaleIntent(String value) {
        return value.contains("vente")
                || value.contains("sortie")
                || value.contains("vendre")
                || value.contains("client")
                || value.contains("sortir du stock");
    }

    private boolean isExpenseIntent(String value) {
        return value.contains("depense")
                || value.contains("dépense")
                || value.contains("frais")
                || value.contains("note de frais")
                || value.contains("charge")
                || value.contains("recu")
                || value.contains("reçu")
                || value.contains("ticket");
    }

    private boolean isProductIntent(String value) {
        return value.contains("produit")
                || value.contains("article")
                || value.contains("liste produit")
                || value.contains("liste produits")
                || value.contains("liste de produits")
                || value.contains("approvisionnement generique")
                || value.contains("catalogue");
    }

    public boolean shouldHandleWorkflowCancellation(String question) {
        startForCurrentSession();
        if (GratienTools.hasPendingWorkflowCancellation(sessionId)) {
            return true;
        }
        String value = question == null ? "" : question.toLowerCase();
        return value.contains("annule")
                || value.contains("annuler")
                || value.contains("annulation")
                || value.contains("abandonne")
                || value.contains("stop workflow")
                || value.contains("arrête le workflow")
                || value.contains("arrete le workflow")
                || value.contains("cancel workflow");
    }

    public String orchestrateWorkflowCancellation(String question) {
        startForCurrentSession();
        emitProgress("\uD83D\uDDD1\uFE0F Délégation au sous-agent d'annulation de workflow en cours...");
        appendMemory("user", safe(question, ""));
        String state = GratienTools.pendingWorkflowCancellationState(sessionId);
        String result = runAgent("workflow_cancellation_agent",
                () -> workflowCancellationAgent.execute(sessionId, sessionId, state, safe(question, "")));
        appendMemory("workflow_cancellation_agent", result);
        return result == null || result.isBlank() ? state : result;
    }

    public String orchestrateInvoice(String question, List<File> attachments) {
        startForCurrentSession();
        emitProgress("\uD83D\uDCC4 Lancement du workflow d'approvisionnement "
                + "(catalogue, fournisseur/livraison, réquisitions)...");
        appendMemory("user", safe(question, "[facture jointe]"));
        List<File> resolved = resolvedDocumentAttachments(attachments);
        String[] answer = new String[1];
        try {
            // Graphe general: chaque noeud represente un agent metier de la chaine facture.
            CompiledGraph<ServiceAgentState> graph = new StateGraph<>(ServiceAgentState.SCHEMA, ServiceAgentState::new)
                    .addNode("product_creator_agent", node_async(state -> {
                        // Agent createur: prepare la categorie, le produit et les mesures si le produit est absent.
                        appendMemory("agent", "Agent createur: categorie, produit et mesures prepares si absents.");
                        return Map.of("step", "product_creator_agent");
                    }))
                    .addNode("supplier_delivery_agent", node_async(state -> {
                        // Agent livraison: fournisseur, total a payer, montant paye et dette fournisseur.
                        appendMemory("agent", "Agent fournisseur/livraison: fournisseur facture et dette fournisseur.");
                        return Map.of("step", "supplier_delivery_agent");
                    }))
                    .addNode("requisition_agent", node_async(state -> {
                        // Agent recquisition: transforme chaque ligne facture en recquisition et prix de vente.
                        answer[0] = invoiceWorkflow.handle(question, resolved);
                        appendMemory("assistant", answer[0]);
                        return Map.of("step", "requisition_agent");
                    }))
                    // Ordre impose par le metier: produit -> livraison -> recquisitions.
                    .addEdge(START, "product_creator_agent")
                    .addEdge("product_creator_agent", "supplier_delivery_agent")
                    .addEdge("supplier_delivery_agent", "requisition_agent")
                    .addEdge("requisition_agent", END)
                    .compile();
            Optional<ServiceAgentState> ignored = graph.invoke(Map.of("step", "start"));
        } catch (Exception ex) {
            // En cas d'echec LangGraph4j, le workflow direct garde Gratien utilisable.
            LOGGER.log(Level.WARNING, "LangGraph4j AiAgents a bascule en execution directe", ex);
            answer[0] = invoiceWorkflow.handle(question, resolved);
            appendMemory("assistant", answer[0]);
        }
        return answer[0] == null ? "Je n'ai pas pu orchestrer cette facture." : answer[0];
    }

    public String orchestrateProductImage(String question, List<File> attachments) {
        startForCurrentSession();
        appendMemory("user", safe(question, "[image de produits jointe]"));
        String answer = productImageWorkflow.handle(question, resolvedDocumentAttachments(attachments));
        appendMemory("assistant", answer);
        return answer;
    }

    public String orchestrateSale(String question, List<File> attachments) {
        startForCurrentSession();
        emitProgress("\uD83D\uDED2 Lancement du workflow de vente "
                + "(création de vente, trésorerie & synchronisation)...");
        appendMemory("user", safe(question, "[sortie jointe]"));
        String answer = saleWorkflow.handle(question, resolvedDocumentAttachments(attachments));
        appendMemory("assistant", answer);
        return answer;
    }

    public String orchestrateExpense(String question, List<File> attachments) {
        startForCurrentSession();
        emitProgress("\uD83D\uDCB5 Lancement du workflow de dépense "
                + "(préparation catégorie/compte, opération de dépense)...");
        appendMemory("user", safe(question, "[reçu de dépense joint]"));
        String answer = expenseWorkflow.handle(question, resolvedDocumentAttachments(attachments));
        appendMemory("assistant", answer);
        return answer;
    }

    private String runInvoiceAgents(InvoiceDraft draft) {
        String workflowId = GratienTools.registerInvoiceWorkflow(draft);
        String[] finalAnswer = new String[1];
        appendMemory("invoice-workflow", "Demarrage workflow " + workflowId + " : " + GratienTools.workflowState(workflowId));
        emitProgress("\uD83D\uDCC4 Démarrage du workflow " + workflowId + " "
                + "(catalogue → fournisseur/livraison → réquisitions)...");
        try {
            // Les sous-agents n'interviennent que pour creer les objets parents
            // sans dependance superieure (catalogue: categorie/produit/mesure,
            // puis fournisseur/livraison). La chaine requisition -> prix de vente
            // suit des dependances directes (produit -> requisition -> prix) :
            // un seul agent (Gratien) la cree un a un, sans sous-agent.
            CompiledGraph<ServiceAgentState> graph = new StateGraph<>(ServiceAgentState.SCHEMA, ServiceAgentState::new)
                    .addNode("product_creator_agent", node_async(state -> {
                        String result = runAgent("product_creator_agent",
                                () -> productCreatorAgent.execute(sessionId, workflowId, GratienTools.workflowState(workflowId)));
                        appendMemory("product_creator_agent", result);
                        return Map.of("step", "product_creator_agent");
                    }))
                    .addNode("supplier_delivery_agent", node_async(state -> {
                        String result = runAgent("supplier_delivery_agent",
                                () -> supplierDeliveryAgent.execute(sessionId, workflowId, GratienTools.workflowState(workflowId)));
                        appendMemory("supplier_delivery_agent", result);
                        return Map.of("step", "supplier_delivery_agent");
                    }))
                    .addEdge(START, "product_creator_agent")
                    .addEdge("product_creator_agent", "supplier_delivery_agent")
                    .addEdge("supplier_delivery_agent", END)
                    .compile();
            Optional<ServiceAgentState> ignored = graph.invoke(Map.of("step", "start"));
            finalAnswer[0] = GratienTools.createRequisitionsAndSalePrices(workflowId);
            appendMemory("requisition_agent", finalAnswer[0]);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Workflow multi-agents facture en fallback direct", ex);
            finalAnswer[0] = GratienTools.insertInvoiceSupply(draft);
        }
        return finalAnswer[0] == null || finalAnswer[0].isBlank()
                ? GratienTools.workflowState(workflowId)
                : finalAnswer[0];
    }

    private String runSaleAgents(SaleDraft draft) {
        String workflowId = GratienTools.registerSaleWorkflow(draft);
        String[] finalAnswer = new String[1];
        appendMemory("sale-workflow", "Demarrage workflow " + workflowId + " : " + GratienTools.saleWorkflowState(workflowId));
        emitProgress("\uD83D\uDED2 Démarrage du workflow " + workflowId + " "
                + "(création de vente → trésorerie/synchronisation)...");
        try {
            CompiledGraph<ServiceAgentState> graph = new StateGraph<>(ServiceAgentState.SCHEMA, ServiceAgentState::new)
                    .addNode("sale_creation_agent", node_async(state -> {
                        String result = runAgent("sale_creation_agent",
                                () -> saleCreationAgent.execute(sessionId, workflowId, GratienTools.saleWorkflowState(workflowId)));
                        appendMemory("sale_creation_agent", result);
                        return Map.of("step", "sale_creation_agent");
                    }))
                    .addNode("sale_treasury_agent", node_async(state -> {
                        finalAnswer[0] = runAgent("sale_treasury_agent",
                                () -> saleTreasuryAgent.execute(sessionId, workflowId, GratienTools.saleWorkflowState(workflowId)));
                        appendMemory("sale_treasury_agent", finalAnswer[0]);
                        return Map.of("step", "sale_treasury_agent");
                    }))
                    .addEdge(START, "sale_creation_agent")
                    .addEdge("sale_creation_agent", "sale_treasury_agent")
                    .addEdge("sale_treasury_agent", END)
                    .compile();
            Optional<ServiceAgentState> ignored = graph.invoke(Map.of("step", "start"));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Workflow vente/sortie en fallback direct", ex);
            finalAnswer[0] = GratienTools.insertSaleOutput(draft);
        }
        return finalAnswer[0] == null || finalAnswer[0].isBlank()
                ? GratienTools.saleWorkflowState(workflowId)
                : finalAnswer[0];
    }

    private String runExpenseAgents(ExpenseDraft draft) {
        String workflowId = GratienTools.registerExpenseWorkflow(draft);
        String[] finalAnswer = new String[1];
        appendMemory("expense-workflow", "Demarrage workflow " + workflowId + " : " + GratienTools.expenseWorkflowState(workflowId));
        emitProgress("\uD83D\uDCB5 Démarrage du workflow " + workflowId + " "
                + "(préparation catégorie/compte → opération de dépense)...");
        try {
            CompiledGraph<ServiceAgentState> graph = new StateGraph<>(ServiceAgentState.SCHEMA, ServiceAgentState::new)
                    .addNode("expense_preparation_agent", node_async(state -> {
                        String result = runAgent("expense_preparation_agent",
                                () -> expensePreparationAgent.execute(sessionId, workflowId, GratienTools.expenseWorkflowState(workflowId)));
                        appendMemory("expense_preparation_agent", result);
                        return Map.of("step", "expense_preparation_agent");
                    }))
                    .addNode("expense_operation_agent", node_async(state -> {
                        finalAnswer[0] = runAgent("expense_operation_agent",
                                () -> expenseOperationAgent.execute(sessionId, workflowId, GratienTools.expenseWorkflowState(workflowId)));
                        appendMemory("expense_operation_agent", finalAnswer[0]);
                        return Map.of("step", "expense_operation_agent");
                    }))
                    .addEdge(START, "expense_preparation_agent")
                    .addEdge("expense_preparation_agent", "expense_operation_agent")
                    .addEdge("expense_operation_agent", END)
                    .compile();
            Optional<ServiceAgentState> ignored = graph.invoke(Map.of("step", "start"));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Workflow depense en fallback direct", ex);
            finalAnswer[0] = GratienTools.insertExpenseOutput(draft);
        }
        return finalAnswer[0] == null || finalAnswer[0].isBlank()
                ? GratienTools.expenseWorkflowState(workflowId)
                : finalAnswer[0];
    }

    public void streamGeneral(String question, String entreprise, Consumer<String> onToken,
            Consumer<String> onProcess, Runnable onComplete, Consumer<Throwable> onError) {
        startForCurrentSession();
        appendMemory("user", safe(question, ""));
        String date=LocalDateTime.now().toString();
        TokenStream stream = assistant.chat(sessionId,date,entreprise == null ? "" : entreprise, safe(question, ""));
        Set<String> toolsStarted = ConcurrentHashMap.newKeySet();
        AtomicBoolean toolWasStarted = new AtomicBoolean(false);
        AtomicBoolean toolWasExecuted = new AtomicBoolean(false);
        AtomicBoolean reflectionShown = new AtomicBoolean(false);
        AtomicReference<String> lastToolName = new AtomicReference<>("");
        AtomicReference<String> lastToolResult = new AtomicReference<>("");
        StringBuilder bufferedToolResponse = new StringBuilder();
        stream.beforeToolExecution(tool -> {
            String toolName = tool.request() == null ? "outil" : tool.request().name();
            toolWasStarted.set(true);
            lastToolName.set(toolName);
            String toolKey = toolExecutionKey(toolName, tool.request() == null ? "" : tool.request().arguments());
            if (!toolsStarted.add(toolKey)) {
                appendMemory("tool-start-duplicate", toolLabel(toolName, true));
                return;
            }
            appendMemory("tool-start", toolLabel(toolName, true));
            // Le chat n'affiche qu'une seule ligne pendant le traitement, sans deverser
            // le raisonnement ni les statuts verbeux de chaque outil.
            if (reflectionShown.compareAndSet(false, true)) {
                onProcess.accept("*Réflexion en cours...*");
            }
        }).onPartialThinking(thinking -> {
            if (reflectionShown.compareAndSet(false, true)) {
                onProcess.accept("*Réflexion en cours...*");
            }
        }).onPartialResponse(token -> {
            if (toolWasStarted.get()) {
                bufferedToolResponse.append(token);
            } else {
                onToken.accept(sanitizeToolNamesForDisplay(token));
            }
        }).onToolExecuted(tool -> {
            String toolName = tool.request() == null ? "outil" : tool.request().name();
            toolWasExecuted.set(true);
            lastToolName.set(toolName);
            String toolKey = toolExecutionKey(toolName, tool.request() == null ? "" : tool.request().arguments());
            String result = tool.result() == null ? "" : tool.result();
            lastToolResult.set(result);
            appendMemory("tool-result", toolName + " => " + result);
        }).onCompleteResponse(response -> {
                    if (toolWasExecuted.get()) {
                        String finalAnswer = toolFinalAnswer(lastToolName.get(), lastToolResult.get());
                        appendMemory("assistant", finalAnswer);
                        onToken.accept(sanitizeToolNamesForDisplay(finalAnswer));
                    }
                    finishStreaming();
                    onComplete.run();
                })
                .onError(error -> {
                    appendMemory("error", throwableMessage(error));
                    finishStreaming();
                    onError.accept(error);
                });
        streamingActive.set(true);
        stream.start();
    }

    /**
     * Marque la fin du streaming puis execute le compactage de memoire reporte
     * si la limite a ete atteinte en cours de reponse.
     */
    private void finishStreaming() {
        streamingActive.set(false);
        if (compactionPending.compareAndSet(true, false)) {
            maybeCompactMemory();
        }
    }

    public void appendMemory(String role, String content) {
        if (streamingActive.get()) {
            // Pendant un streaming on ne bloque jamais : on reporte le compactage a la fin.
            memoryStore.append(sessionId, role, content);
            if (memoryAtLimit()) {
                compactionPending.set(true);
            }
            return;
        }
        // Hors streaming : a chaque limite atteinte, la prochaine ecriture evincerait
        // le plus ancien message. On compacte AVANT d'ajouter afin qu'aucun message ne
        // soit perdu et que le contexte compacte reste le message numero 1.
        if (memoryAtLimit()) {
            maybeCompactMemory();
        }
        memoryStore.append(sessionId, role, content);
    }

    public List<String> recentMemory(int limit) {
        return memoryStore.recent(sessionId, Math.min(limit, MAX_MEMORY_MESSAGES));
    }

    /**
     * Branche le signal de compactage de la session courante (affiche via le
     * canal de progression du chat). Chaque requete l'ecrase; les requetes sont
     * serializees par le controleur, donc un seul signal actif a la fois.
     */
    public void setCompactionSignal(Consumer<String> signal) {
        this.compactionSignal = signal;
    }

    /** Vrai pendant qu'un compactage de memoire est en cours (LLM de resume). */
    public boolean isCompacting() {
        return compacting.get();
    }

    private void emitCompactionSignal(String message) {
        Consumer<String> signal = compactionSignal;
        if (signal != null && message != null && !message.isBlank()) {
            try {
                signal.accept(message);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "Signal de compactage non affichable", ex);
            }
        }
    }

    /**
     * Branche le canal de progression de la session courante (raisonnement des
     * sous-agents et avancement des workflows affiches dans le chat).
     */
    public void setProgressSignal(Consumer<String> signal) {
        this.progressSignal = signal;
    }

    private void emitProgress(String message) {
        Consumer<String> signal = progressSignal;
        if (signal != null && message != null && !message.isBlank()) {
            try {
                signal.accept(message);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "Progression non affichable", ex);
            }
        }
    }

    /**
     * Point d'entree du mode swarm : lance la tache demandee vers le sous-agent
     * specialise (workflow complet ou agent d'etape d'un workflow en cours).
     */
    public String runSwarmDelegate(String agent, String task, String workflowId) {
        startForCurrentSession();
        String target = normalize(agent);
        String question = safe(task, "");
        switch (target) {
            case "invoice", "facture", "approvisionnement", "entree", "entrees",
                    "entrée", "entrées", "livraison", "fournisseur" -> {
                emitProgress("\uD83D\uDCC4 Délégation au workflow d'approvisionnement "
                        + "(sous-agents: catalogue, fournisseur/livraison, réquisitions & prix)...");
                return orchestrateInvoice(question, null);
            }
            case "sale", "vente", "sortie" -> {
                emitProgress("\uD83D\uDED2 Délégation au workflow de vente "
                        + "(sous-agents: création de vente, trésorerie & synchronisation)...");
                return orchestrateSale(question, null);
            }
            case "expense", "depense", "dépense", "frais" -> {
                emitProgress("\uD83D\uDCB5 Délégation au workflow de dépense "
                        + "(sous-agents: préparation catégorie/compte, opération)...");
                return orchestrateExpense(question, null);
            }
            case "product_creator_agent", "supplier_delivery_agent",
                    "sale_creation_agent", "sale_treasury_agent",
                    "expense_preparation_agent", "expense_operation_agent" -> {
                if (workflowId == null || workflowId.isBlank()) {
                    return "Impossible de lancer l'agent d'étape '" + target + "' sans workflowId en cours. "
                            + "Délègue d'abord le workflow complet (invoice/sale/expense) ou fournis un workflowId.";
                }
                return runStepAgent(target, workflowId.trim());
            }
            default -> {
                return "Sous-agent inconnu: '" + safe(agent, "") + "'. Agents disponibles: "
                        + "invoice, sale, expense, product_creator_agent, supplier_delivery_agent, "
                        + "sale_creation_agent, sale_treasury_agent, "
                        + "expense_preparation_agent, expense_operation_agent.";
            }
        }
    }

    private String runStepAgent(String agentName, String workflowId) {
        switch (agentName) {
            case "product_creator_agent":
                return runAgent(agentName,
                        () -> productCreatorAgent.execute(sessionId, workflowId, GratienTools.workflowState(workflowId)));
            case "supplier_delivery_agent":
                return runAgent(agentName,
                        () -> supplierDeliveryAgent.execute(sessionId, workflowId, GratienTools.workflowState(workflowId)));
            case "sale_creation_agent":
                return runAgent(agentName,
                        () -> saleCreationAgent.execute(sessionId, workflowId, GratienTools.saleWorkflowState(workflowId)));
            case "sale_treasury_agent":
                return runAgent(agentName,
                        () -> saleTreasuryAgent.execute(sessionId, workflowId, GratienTools.saleWorkflowState(workflowId)));
            case "expense_preparation_agent":
                return runAgent(agentName,
                        () -> expensePreparationAgent.execute(sessionId, workflowId, GratienTools.expenseWorkflowState(workflowId)));
            case "expense_operation_agent":
                return runAgent(agentName,
                        () -> expenseOperationAgent.execute(sessionId, workflowId, GratienTools.expenseWorkflowState(workflowId)));
            default:
                return "Agent d'étape inconnu: " + agentName;
        }
    }

    private String friendlyAgentName(String agentName) {
        return switch (safe(agentName, "sous-agent")) {
            case "product_creator_agent" -> "catalogue (produits & mesures)";
            case "supplier_delivery_agent" -> "fournisseur & livraison";
            case "sale_creation_agent" -> "création de vente";
            case "sale_treasury_agent" -> "trésorerie vente & synchronisation";
            case "expense_preparation_agent" -> "préparation de dépense";
            case "expense_operation_agent" -> "opération de dépense";
            case "workflow_cancellation_agent" -> "annulation de workflow";
            default -> "sous-agent " + agentName;
        };
    }

    /**
     * Compacte la memoire a l'eviction : purge l'historique et ne garde que le
     * contexte compacte en message numero 1.
     */
    private void maybeCompactMemory() {
        if (streamingActive.get()) {
            return;
        }
        synchronized (compactionLock) {
            try {
                List<String> raw = memoryStore.recentRaw(sessionId, MAX_MEMORY_MESSAGES);
                if (raw.size() < MAX_MEMORY_MESSAGES) {
                    return;
                }
                compactMemory(raw);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Compactage de memoire impossible pour " + sessionId, ex);
            }
        }
    }

    /**
     * Vrai des que la memoire est pleine : la prochaine ecriture evincerait un message.
     */
    private boolean memoryAtLimit() {
        return memoryStore.recentRaw(sessionId, MAX_MEMORY_MESSAGES).size() >= MAX_MEMORY_MESSAGES;
    }

    private void compactMemory(List<String> raw) {
        compacting.set(true);
        emitCompactionSignal(
                "\uD83E\uDDD1\u200D\uD83D\uDCBB Gratien compacte sa mémoire pour ne garder que l'essentiel... "
                        + "Les messages reçus pendant ce temps seront traités juste après.");
        try {
            String summary = summarize(raw);
            // Eviction: la memoire est entierement purgee, seul le contexte compacte subsiste.
            List<String> renewed = new ArrayList<>();
            renewed.add(RedisMemoryStore.serializePayload("system", COMPACTION_MARKER + summary));
            memoryStore.replaceRaw(sessionId, renewed);
            LOGGER.log(Level.INFO, "Memoire compactee pour " + sessionId + ": purgee, "
                    + renewed.size() + " message de contexte compacte en numero 1.");
            emitCompactionSignal("\u2705 Mémoire compactée.");
        } finally {
            compacting.set(false);
        }
    }

    private String summarize(List<String> raw) {
        StringBuilder transcript = new StringBuilder();
        for (String payload : raw) {
            String[] parts = RedisMemoryStore.parsePayload(payload);
            String role = parts[0];
            String content = parts[1];
            if (content == null || content.isBlank()) {
                continue;
            }
            if (content.length() > 1200) {
                content = content.substring(0, 1200) + "...";
            }
            transcript.append(role).append(": ").append(content.replace("\n", " ")).append("\n");
        }
        if (transcript.length() > 6000) {
            transcript.delete(0, transcript.length() - 6000);
        }
        String conversation = transcript.toString();
        try {
            ChatResponse response = summaryModel().chat(ChatRequest.builder()
                    .messages(SystemMessage.from(SUMMARY_SYSTEM_PROMPT),
                            UserMessage.from("Conversation a resumer:\n\n" + conversation))
                    .build());
            String text = response == null || response.aiMessage() == null
                    ? "" : response.aiMessage().text();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Resume LLM indisponible, compactage deterministe: {0}",
                    ex.getMessage());
        }
        return deterministicSummary(conversation);
    }

    private String deterministicSummary(String conversation) {
        String compact = conversation.trim().replace("\n", " | ");
        return compact.length() > 1500 ? compact.substring(0, 1500) + "..." : compact;
    }

    private ChatModel summaryModel() {
        ChatModel model = summaryModel;
        if (model == null) {
            model = OllamaChatModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName(MODEL_NAME)
                    .temperature(0.2)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            summaryModel = model;
        }
        return model;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private ChatMemoryProvider memoryProvider(RedisMemoryStore store) {
        LangChainRedisChatMemoryStore chatMemoryStore = new LangChainRedisChatMemoryStore(store);
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(MAX_MEMORY_MESSAGES)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }

    private <T> T buildAgent(Class<T> agentType, StreamingChatModel model) {
        return AiServices.builder(agentType)
                .streamingChatModel(model)
                .tools(GratienTools)
                .chatMemoryProvider(memoryProvider)
                .toolArgumentsErrorHandler(NULL_SAFE_TOOL_ARGUMENTS_ERROR_HANDLER)
                .toolExecutionErrorHandler(NULL_SAFE_TOOL_EXECUTION_ERROR_HANDLER)
                .build();
    }

    private String runAgent(String agentName, Supplier<TokenStream> streamSupplier) {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder answer = new StringBuilder();
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<String> lastToolResult = new AtomicReference<>("");
        try {
            appendMemory("agent-start", agentName + " lance son execution.");
            emitProgress("\uD83E\uDD16 Sous-agent " + friendlyAgentName(agentName) + " : analyse de la tâche...");
            streamSupplier.get()
                    .beforeToolExecution(tool -> {
                        String toolName = tool.request() == null ? "outil" : tool.request().name();
                        appendMemory(agentName + "-tool-start", "Execution outil " + toolName);
                        emitProgress(sanitizeToolNamesForDisplay(toolLabel(toolName, true)));
                    })
                    .onToolExecuted(tool -> {
                        String toolName = tool.request() == null ? "outil" : tool.request().name();
                        String result = tool.result() == null ? "" : tool.result();
                        lastToolResult.set(result);
                        appendMemory(agentName + "-tool-result", toolName + " => " + result);
                        emitProgress(sanitizeToolNamesForDisplay(toolLabel(toolName, false) + "\n\n" + result));
                    })
                    .onPartialThinking(thinking -> {
                        if (thinking != null && thinking.text() != null && !thinking.text().isBlank()) {
                            appendMemory(agentName + "-thinking", thinking.text());
                            // Le chat n'affiche qu'une seule ligne pendant le raisonnement du sous-agent.
                            emitProgress("*Réflexion en cours...*");
                        }
                    })
                    .onPartialResponse(answer::append)
                    .onCompleteResponse(response -> latch.countDown())
                    .onError(ex -> {
                        error.set(ex);
                        latch.countDown();
                    })
                    .start();
            if (!latch.await(5, TimeUnit.MINUTES)) {
                throw new IllegalStateException(agentName + " n'a pas termine dans le delai attendu");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, agentName + " interrompu", ex);
            return safeErrorMessage(ex, "L'agent " + agentName + " a ete interrompu.");
        } catch (Exception ex) {
            // Un echec d'agent ne doit jamais tuer le thread de streaming Gratien :
            // on renvoie un message lisible a la place (le fallback reste utilisable).
            LOGGER.log(Level.WARNING, agentName + " a echoue", ex);
            appendMemory(agentName + "-error", safeErrorMessage(ex,
                    "L'agent " + agentName + " a rencontre un probleme."));
            return safeErrorMessage(ex, "L'agent " + agentName + " a rencontre un probleme.");
        }
        if (error.get() != null) {
            LOGGER.log(Level.WARNING, agentName + " a echoue", error.get());
            appendMemory(agentName + "-error", safeErrorMessage(error.get(),
                    "L'agent " + agentName + " a rencontre un probleme."));
            return safeErrorMessage(error.get(), "L'agent " + agentName + " a rencontre un probleme.");
        }
        String finalAnswer = answer.toString();
        if (asksForInternalWorkflowId(finalAnswer)) {
            finalAnswer = invoiceTaskFinishedMessage(agentName, lastToolResult.get());
        }
        return finalAnswer == null || finalAnswer.isBlank()
                ? invoiceTaskFinishedMessage(agentName, lastToolResult.get())
                : finalAnswer;
    }

    private boolean asksForInternalWorkflowId(String answer) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        String normalized = answer.toLowerCase();
        return normalized.contains("workflowid")
                && (normalized.contains("veuillez")
                || normalized.contains("transmettre")
                || normalized.contains("donner")
                || normalized.contains("fournir")
                || normalized.contains("prêt")
                || normalized.contains("pret"));
    }

    private String invoiceTaskFinishedMessage(String agentName, String toolResult) {
        String result = toolResult == null ? "" : toolResult.trim();
        return result;
    }

    private String toolFinalAnswer(String toolName, String result) {
        String cleanResult = result == null ? "" : result.trim();
        return cleanResult.isBlank() ? toolLabel(toolName, false) : cleanResult;
    }

    private String sanitizeToolNamesForDisplay(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String sanitized = text;
        for (String toolName : List.of(
                "generateMysqlReplicaConfiguration",
                "executeMysqlReplicaConfiguration",
                "testMysqlReplicaStatus",
                "deleteCategory",
                "createCategory",
                "findCategories",
                "listAllProducts",
                "searchProductsByCriteria",
                "bulkUpdateProducts",
                "createProductsAndAskMeasures",
                "createProductMeasures",
                "createSaleAndLines",
                "createSaleTreasuryAndSync",
                "prepareExpenseCategoryAndAccount",
                "createExpenseTreasuryAndOperation",
                "requestWorkflowCancellation",
                "answerWorkflowCancellation",
                "fixEmptyProductBarcodes",
                "createProductsAndMeasures",
                "createSupplierAndDelivery",
                "createRequisitionsAndSalePrices",
                "insertInvoiceSupply",
                "generateFinancialStatementsPdf",
                "generateFinancialStatementsExcel")) {
            sanitized = sanitized.replace(toolName, toolLabel(toolName, false));
        }
        return sanitized;
    }

    private String throwableMessage(Throwable error) {
        if (error == null) {
            return "erreur inconnue retournée par le flux IA.";
        }
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    private String toolExecutionKey(String toolName, String arguments) {
        return safe(toolName, "outil") + "|" + safe(arguments, "");
    }

    private String toolLabel(String toolName, boolean running) {
        return switch (safe(toolName, "outil")) {
            case "deleteCategory" ->
                running ? "La tâche de suppression de la catégorie est en cours..."
                : "La suppression de la catégorie est terminée.";
            case "createCategory" ->
                running ? "La tâche de création de la catégorie est en cours..."
                : "La création de la catégorie est terminée.";
            case "findCategories" ->
                running ? "La recherche des catégories est en cours..."
                : "La recherche des catégories est terminée.";
            case "listAllProducts" ->
                running ? "La recherche de tous les produits est en cours..."
                : "La recherche de tous les produits est terminée.";
            case "searchProductsByCriteria" ->
                running ? "La recherche des produits selon vos critères est en cours..."
                : "La recherche des produits selon vos critères est terminée.";
            case "bulkUpdateProducts" ->
                running ? "La modification groupée des produits est en cours..."
                : "La modification groupée des produits est terminée.";
            case "createProductsAndAskMeasures" ->
                running ? "La création des produits est en cours..."
                : "La création des produits est terminée.";
            case "createProductMeasures" ->
                running ? "L'enregistrement des mesures est en cours..."
                : "L'enregistrement des mesures est terminé.";
            case "createSaleAndLines" ->
                running ? "La création de la vente et des lignes est en cours..."
                : "La création de la vente et des lignes est terminée.";
            case "createSaleTreasuryAndSync" ->
                running ? "L'enregistrement de la trésorerie de vente est en cours..."
                : "L'enregistrement de la trésorerie de vente est terminé.";
            case "prepareExpenseCategoryAndAccount" ->
                running ? "La préparation de la dépense et du compte est en cours..."
                : "La préparation de la dépense et du compte est terminée.";
            case "createExpenseTreasuryAndOperation" ->
                running ? "L'enregistrement de la dépense est en cours..."
                : "L'enregistrement de la dépense est terminé.";
            case "requestWorkflowCancellation" ->
                running ? "La demande de confirmation d'annulation est en cours..."
                : "La demande de confirmation d'annulation est prête.";
            case "answerWorkflowCancellation" ->
                running ? "Le traitement de la confirmation d'annulation est en cours..."
                : "Le traitement de la confirmation d'annulation est terminé.";
            case "fixEmptyProductBarcodes" ->
                running ? "La correction des codes-barres vides est en cours..."
                : "La correction des codes-barres vides est terminée.";
            case "createProductsAndMeasures" ->
                running ? "La préparation des produits et mesures est en cours..."
                : "La préparation des produits et mesures est terminée.";
            case "createSupplierAndDelivery" ->
                running ? "La création du fournisseur et de la livraison est en cours..."
                : "La création du fournisseur et de la livraison est terminée.";
            case "createRequisitionsAndSalePrices" ->
                running ? "La création des réquisitions et prix de vente est en cours..."
                : "La création des réquisitions et prix de vente est terminée.";
            case "insertInvoiceSupply" ->
                running ? "L'enregistrement de l'approvisionnement est en cours..."
                : "L'enregistrement de l'approvisionnement est terminé.";
            case "generateFinancialStatementsPdf" ->
                running ? "La génération des états financiers PDF est en cours..."
                : "La génération des états financiers PDF est terminée.";
            case "generateFinancialStatementsExcel" ->
                running ? "La génération des états financiers Excel est en cours..."
                : "La génération des états financiers Excel est terminée.";
            case "generateMysqlReplicaConfiguration" ->
                running ? "La préparation de la configuration de réplication MySQL est en cours..."
                : "La préparation de la configuration de réplication MySQL est terminée.";
            case "executeMysqlReplicaConfiguration" ->
                running ? "La configuration du replica MySQL est en cours..."
                : "La configuration du replica MySQL est terminée.";
            case "testMysqlReplicaStatus" ->
                running ? "Le test de réplication MySQL est en cours..."
                : "Le test de réplication MySQL est terminé.";
            default ->
                running ? "La tâche demandée est en cours..."
                : "La tâche demandée est terminée.";
        };
    }

    private record PendingInvoiceIntent(String sessionId, String question, List<File> attachments) {
    }

    public static class ServiceAgentState extends AgentState {

        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                "step", Channels.appender(ArrayList::new)
        );

        public ServiceAgentState(Map<String, Object> initData) {
            super(initData);
        }
    }
}
