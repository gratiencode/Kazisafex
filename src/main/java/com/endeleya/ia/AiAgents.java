package com.endeleya.ia;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolErrorContext;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                    safeErrorMessage(error, "Arguments invalides fournis a l'outil: "
                            + toolName(context)));
    private static final ToolExecutionErrorHandler NULL_SAFE_TOOL_EXECUTION_ERROR_HANDLER =
            (error, context) -> ToolErrorHandlerResult.text(
                    safeErrorMessage(error, "Echec de l'execution de l'outil: "
                            + toolName(context)));

    private static String toolName(ToolErrorContext context) {
        if (context == null || context.toolExecutionRequest() == null
                || context.toolExecutionRequest().name() == null) {
            return "outil";
        }
        return context.toolExecutionRequest().name();
    }

    private static String safeErrorMessage(Throwable error, String fallback) {
        if (error == null) {
            return fallback;
        }
        String message = error.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }

    public static String getSpeedModel() {
       if (LocalTime.now().isAfter(LocalTime.of(17, 59))){
            return "minimax-m3:cloud";
        }else  if (LocalTime.now().isAfter(LocalTime.of(06, 59))) {
            return "gemma4:31b-cloud";
        } 
        return "gemma4:31b-cloud";
    }
    
    public static final String OLLAMA_BASE_URL = "https://ai.kazisafe.com";
    private static final String MODEL_NAME = System.getProperty(
            "kazisafex.ai.model",
            System.getenv().getOrDefault("AI_MODEL", 
                    getSpeedModel()
//                    "minimax-m3:cloud"
            ));
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
    private final RequisitionPriceAgent requisitionPriceAgent;
    private final SaleCreationAgent saleCreationAgent;
    private final SaleTreasuryAgent saleTreasuryAgent;
    private final ExpensePreparationAgent expensePreparationAgent;
    private final ExpenseOperationAgent expenseOperationAgent;
    private final WorkflowCancellationAgent workflowCancellationAgent;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private volatile String sessionId = "anonymous";
    private volatile PendingInvoiceIntent pendingInvoiceIntent;

    private AiAgents() {
        // GratienTools est partage avec le workflow pour garder un seul point d'acces aux actions base/metier.
        this.GratienTools = new GratienTools();
        // La memoire Redis garde le contexte par entreprise/utilisateur avec fallback local.
        this.memoryStore = new RedisMemoryStore();
        this.memoryProvider = memoryProvider(memoryStore);
        StreamingChatModel oschatmodel = OllamaStreamingChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName(MODEL_NAME)
                .temperature(0.25)
                .returnThinking(true)
                .timeout(Duration.ofMinutes(5))
                .build();
        // Pattern LangChain4j demande: proxy agent + function calling via GratienTools + memoire.
        this.assistant = AiServices.builder(GratienAgent.class)
                .streamingChatModel(oschatmodel)
                .tools(GratienTools)
                .chatMemoryProvider(memoryProvider)
                .toolArgumentsErrorHandler(NULL_SAFE_TOOL_ARGUMENTS_ERROR_HANDLER)
                .toolExecutionErrorHandler(NULL_SAFE_TOOL_EXECUTION_ERROR_HANDLER)
                .build();
        this.productCreatorAgent = buildAgent(ProductCreatorAgent.class, oschatmodel);
        this.supplierDeliveryAgent = buildAgent(SupplierDeliveryAgent.class, oschatmodel);
        this.requisitionPriceAgent = buildAgent(RequisitionPriceAgent.class, oschatmodel);
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
        sessionId = enterprise + ":" + user;
        if (started.compareAndSet(false, true)) {
            appendMemory("system", "AiAgents demarre a " + Instant.now()
                    + ", redis=" + memoryStore.isRedisAvailable()
                    + ", entreprise=" + enterprise
                    + ", utilisateur=" + user);
        }
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
        if (isSupplyIntent(value) || isSaleIntent(value) || isExpenseIntent(value)) {
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
        pendingInvoiceIntent = pending;
        return """
                Je dois d'abord savoir quel workflow lancer.
                Répondez simplement par `approvisionnement`/`entrées`, `vente`/`sortie`, ou `dépense`.
                """;
    }

    public boolean shouldHandleInvoice(String question, List<File> attachments) {
        return invoiceWorkflow.shouldHandle(question, attachments);
    }

    public boolean shouldHandleProductImage(String question, List<File> attachments) {
        return productImageWorkflow.shouldHandle(question, attachments);
    }

    public boolean shouldHandleSale(String question, List<File> attachments) {
        return saleWorkflow.shouldHandle(question, attachments);
    }

    public boolean shouldHandleExpense(String question, List<File> attachments) {
        return expenseWorkflow.shouldHandle(question, attachments);
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
        appendMemory("user", safe(question, ""));
        String state = GratienTools.pendingWorkflowCancellationState(sessionId);
        String result = runAgent("workflow_cancellation_agent",
                () -> workflowCancellationAgent.execute(sessionId, sessionId, state, safe(question, "")));
        appendMemory("workflow_cancellation_agent", result);
        return result == null || result.isBlank() ? state : result;
    }

    public String orchestrateInvoice(String question, List<File> attachments) {
        startForCurrentSession();
        appendMemory("user", safe(question, "[facture jointe]"));
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
                        answer[0] = invoiceWorkflow.handle(question, attachments);
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
            answer[0] = invoiceWorkflow.handle(question, attachments);
            appendMemory("assistant", answer[0]);
        }
        return answer[0] == null ? "Je n'ai pas pu orchestrer cette facture." : answer[0];
    }

    public String orchestrateProductImage(String question, List<File> attachments) {
        startForCurrentSession();
        appendMemory("user", safe(question, "[image de produits jointe]"));
        String answer = productImageWorkflow.handle(question, attachments);
        appendMemory("assistant", answer);
        return answer;
    }

    public String orchestrateSale(String question, List<File> attachments) {
        startForCurrentSession();
        appendMemory("user", safe(question, "[sortie jointe]"));
        String answer = saleWorkflow.handle(question, attachments);
        appendMemory("assistant", answer);
        return answer;
    }

    public String orchestrateExpense(String question, List<File> attachments) {
        startForCurrentSession();
        appendMemory("user", safe(question, "[reçu de dépense joint]"));
        String answer = expenseWorkflow.handle(question, attachments);
        appendMemory("assistant", answer);
        return answer;
    }

    private String runInvoiceAgents(InvoiceDraft draft) {
        String workflowId = GratienTools.registerInvoiceWorkflow(draft);
        String[] finalAnswer = new String[1];
        appendMemory("invoice-workflow", "Demarrage workflow " + workflowId + " : " + GratienTools.workflowState(workflowId));
        try {
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
                    .addNode("requisition_price_agent", node_async(state -> {
                        finalAnswer[0] = runAgent("requisition_price_agent",
                                () -> requisitionPriceAgent.execute(sessionId, workflowId, GratienTools.workflowState(workflowId)));
                        appendMemory("requisition_price_agent", finalAnswer[0]);
                        return Map.of("step", "requisition_price_agent");
                    }))
                    .addEdge(START, "product_creator_agent")
                    .addEdge("product_creator_agent", "supplier_delivery_agent")
                    .addEdge("supplier_delivery_agent", "requisition_price_agent")
                    .addEdge("requisition_price_agent", END)
                    .compile();
            Optional<ServiceAgentState> ignored = graph.invoke(Map.of("step", "start"));
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
        Set<String> toolsFinished = ConcurrentHashMap.newKeySet();
        AtomicBoolean toolWasStarted = new AtomicBoolean(false);
        AtomicBoolean toolWasExecuted = new AtomicBoolean(false);
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
            String message = toolLabel(toolName, true);
            appendMemory("tool-start", message);
            onProcess.accept(sanitizeToolNamesForDisplay(message));
        }).onPartialThinking(thinking -> {
            String text = thinking == null ? "" : thinking.text();
            if (text != null && !text.isBlank()) {
                appendMemory("thinking", text);
                onProcess.accept(sanitizeToolNamesForDisplay("*Raisonnement en cours...*\n\n" + text));
            }
        }).onPartialResponse(token -> {
            appendMemory("assistant-partial", token);
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
            if (toolsFinished.add(toolKey)) {
                onProcess.accept(sanitizeToolNamesForDisplay(toolLabel(toolName, false) + "\n\n" + result));
            }
        }).onCompleteResponse(response -> {
                    if (toolWasExecuted.get()) {
                        String finalAnswer = toolFinalAnswer(lastToolName.get(), lastToolResult.get());
                        appendMemory("assistant", finalAnswer);
                        onToken.accept(sanitizeToolNamesForDisplay(finalAnswer));
                    }
                    onComplete.run();
                })
                .onError(error -> {
                    appendMemory("error", throwableMessage(error));
                    onError.accept(error);
                })
                .start();
    }

    public void appendMemory(String role, String content) {
        memoryStore.append(sessionId, role, content);
    }

    public List<String> recentMemory(int limit) {
        return memoryStore.recent(sessionId, limit);
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
                .maxMessages(40)
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
            streamSupplier.get()
                    .beforeToolExecution(tool -> {
                        String toolName = tool.request() == null ? "outil" : tool.request().name();
                        appendMemory(agentName + "-tool-start", "Execution outil " + toolName);
                    })
                    .onToolExecuted(tool -> {
                        String toolName = tool.request() == null ? "outil" : tool.request().name();
                        String result = tool.result() == null ? "" : tool.result();
                        lastToolResult.set(result);
                        appendMemory(agentName + "-tool-result", toolName + " => " + result);
                    })
                    .onPartialThinking(thinking -> {
                        if (thinking != null && thinking.text() != null && !thinking.text().isBlank()) {
                            appendMemory(agentName + "-thinking", thinking.text());
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
        if ("requisition_price_agent".equals(agentName)) {
            return "L'enregistrement de la facture comme approvisionnement est terminé."
                    + (result.isBlank() ? "" : "\n\n" + result)
                    + "\n\nVeuillez vérifier maintenant dans l'application.";
        }
        return result;
    }

    private String toolFinalAnswer(String toolName, String result) {
        String label = toolLabel(toolName, false);
        String cleanResult = result == null ? "" : result.trim();
        return cleanResult.isBlank() ? label : label + "\n\n" + cleanResult;
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
