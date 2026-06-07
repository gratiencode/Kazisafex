package com.endeleya.ia;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.BarCode;
import com.github.anastaciocintra.escpos.barcode.BarCodeWrapperInterface;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import data.Category;
import data.Client;
import data.CompteTresor;
import data.Destocker;
import data.Depense;
import data.Entreprise;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.SaleAgregate;
import data.StockAgregate;
import data.StockDepotAgregate;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import data.VenteHelper;
import data.core.KazisafeServiceFactory;
import data.helpers.Mouvment;
import data.helpers.TypeTraisorerie;
import data.network.Kazisafe;
import delegates.CategoryDelegate;
import delegates.ClientDelegate;
import delegates.CompteTresorDelegate;
import delegates.DestockerDelegate;
import delegates.DepenseAgregateDelegate;
import delegates.DepenseDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RepportDelegate;
import delegates.StockerDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import retrofit2.Response;
import services.FinancialStatementAgregateService;
import services.ManagedSessionFactory;
import services.StockDepotAgregateService;
import tools.Constants;
import tools.DataId;
import tools.FileUtils;
import tools.FinancialStatementPdfExporter;
import tools.FinancialStatementRow;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;

public class JemimaTools {

    private final Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    private final FinancialStatementAgregateService financialService = new FinancialStatementAgregateService();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, InvoiceWorkflowContext> INVOICE_WORKFLOWS = new ConcurrentHashMap<>();
    private static final Map<String, SaleWorkflowContext> SALE_WORKFLOWS = new ConcurrentHashMap<>();
    private static final Map<String, ExpenseWorkflowContext> EXPENSE_WORKFLOWS = new ConcurrentHashMap<>();
    private static final Map<String, ProductCodePrintBatch> PRODUCT_CODE_PRINT_BATCHES = new ConcurrentHashMap<>();
    private static final Map<String, DuplicateProductBatch> DUPLICATE_PRODUCT_BATCHES = new ConcurrentHashMap<>();
    private static final Map<String, MysqlReplicationPlan> MYSQL_REPLICATION_PLANS = new ConcurrentHashMap<>();
    private static final Map<String, String> MYSQL_ROOT_PASSWORD_TOKENS = new ConcurrentHashMap<>();
    private static final Map<String, WorkflowCancellationRequest> WORKFLOW_CANCELLATION_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<String, ToolExecutionResult> RECENT_TOOL_EXECUTIONS = new ConcurrentHashMap<>();
    private static final long TOOL_EXECUTION_TTL_MS = 120_000L;
    private static final long WORKFLOW_CANCELLATION_TTL_MS = 180_000L;
    public static final String SECURE_MYSQL_PASSWORD_REQUEST = "KAZISAFE_SECURE_MYSQL_PASSWORD_REQUEST";
    private static final String SECURE_MYSQL_PASSWORD_TOKEN_PREFIX = "secure:mysql:";

    @Tool("Insère en base la nouvelle category donnee en parametre")
    public String createCategory(@P("newcategory") String newcategory) {
        return executeOnce("createCategory", newcategory, () -> {
            Category cat = new Category();
            cat.setDescritption(newcategory);
            Category c = CategoryDelegate.saveCategory(cat);
            return "la category vien d'etre creee avec succes avec le nom " + c.getDescritption();
        });
    }
    
    @Tool("supprime la category donnee en parametre")
    public String deleteCategory(@P("dcategory") String dcategory) {
        return executeOnce("deleteCategory", dcategory, () -> {
            List<Category> dcats=CategoryDelegate.findCategories(dcategory);
            for (Category dcat : dcats) {
                CategoryDelegate.deleteCategory(dcat);
            }
            return "tout category nomee " + dcategory+" vient d'etre supprimee de la base de donnee";
        });
    }

    @Tool("Recherche et retourne la liste de toutes les categories disponibles")
    public String findCategories() {
        return executeOnce("findCategories", "all-categories", () -> {
            List<Category> categories = CategoryDelegate.findCategories();
            if (categories == null || categories.isEmpty()) {
                return "Aucune catégorie trouvée dans la base de données.";
            }
            StringBuilder result = new StringBuilder();
            result.append(categories.size()).append(" catégorie(s) trouvée(s):\n\n");
            result.append("|Numéro|UID|Catégorie|\n");
            result.append("|---|---|---|\n");
            int count = 0;
            for (Category category : categories) {
                if (category == null) {
                    continue;
                }
                count++;
                result.append("|")
                        .append(count)
                        .append("|")
                        .append(tableCell(category.getUid()))
                        .append("|")
                        .append(tableCell(category.getDescritption()))
                        .append("|\n");
            }
            return result.toString();
        });
    }

    @Tool("Genere le plan SQL et my.cnf pour configurer cette machine MySQL comme master de replication")
    public String generateMysqlReplicaConfiguration(
            @P("Utilisateur MySQL de replication a creer sur le master, ex: kazisafe_repl") String replicationUser,
            @P("Mot de passe de l'utilisateur de replication; si vide Jemima genere un mot de passe") String replicationPassword,
            @P("Port MySQL du master; mettre 0 pour utiliser la configuration actuelle") int masterPort,
            @P("Server id MySQL du master; ex: 1") int masterServerId,
            @P("Server id MySQL du replica; ex: 2") int replicaServerId) {
        String key = safe(replicationUser, "kazisafe_repl") + "|" + masterPort + "|" + masterServerId + "|" + replicaServerId;
        return executeOnce("generateMysqlReplicaConfiguration", key, () -> {
            String masterHost = publicMasterHost();
            int resolvedMasterPort = masterPort <= 0 ? pref.getInt("default_mysql_port", 3306) : masterPort;
            String replUser = safe(replicationUser, "kazisafe_repl").trim();
            String replPassword = replicationPassword == null || replicationPassword.isBlank()
                    ? generatedMysqlPassword()
                    : replicationPassword.trim();
            int sourceServerId = masterServerId <= 0 ? 1 : masterServerId;
            int targetServerId = replicaServerId <= 0 ? 2 : replicaServerId;
            String planId = "mysql-repl-" + DataId.generate().substring(0, 8);
            MysqlReplicationPlan plan = new MysqlReplicationPlan(planId, masterHost, resolvedMasterPort, replUser, replPassword, sourceServerId, targetServerId);
            MYSQL_REPLICATION_PLANS.put(planId, plan);
            return mysqlReplicationPlanMarkdown(plan);
        });
    }

    @Tool("Execute sur une machine replica distante la configuration MySQL generee par generateMysqlReplicaConfiguration apres verification du mot de passe root")
    public String executeMysqlReplicaConfiguration(
            @P("Identifiant du plan retourne par generateMysqlReplicaConfiguration") String planId,
            @P("Host ou IP du serveur MySQL replica; doit etre different du master") String replicaHost,
            @P("Port MySQL du replica; mettre 0 pour 3306") int replicaPort,
            @P("Mot de passe root MySQL du replica. Jemima doit le demander a l'utilisateur avant d'appeler ce tool") String mysqlRootPassword) {
        String key = safe(planId, "") + "|" + safe(replicaHost, "") + "|" + replicaPort + "|" + securePasswordKey(mysqlRootPassword);
        return executeOnce("executeMysqlReplicaConfiguration", key, () -> {
            MysqlReplicationPlan plan = MYSQL_REPLICATION_PLANS.get(safe(planId, "").trim());
            if (plan == null) {
                return "Plan de réplication introuvable. Demandez d'abord à Jemima de générer la configuration replica.";
            }
            String host = safe(replicaHost, "").trim();
            if (host.isBlank()) {
                return "Configuration refusée: indiquez le host ou l'adresse IP de la machine replica.";
            }
            if (sameMysqlMachine(plan.masterHost(), host)) {
                return "Configuration refusée: la machine replica doit être différente du master " + plan.masterHost() + ".";
            }
            String rootPassword = resolveMysqlRootPassword(mysqlRootPassword);
            if (rootPassword == null || rootPassword.isBlank()) {
                return secureMysqlPasswordRequest("execute", safe(planId, ""), host, replicaPort <= 0 ? 3306 : replicaPort);
            }
            int port = replicaPort <= 0 ? 3306 : replicaPort;
            try (Connection connection = DriverManager.getConnection(mysqlJdbcUrl(host, port, null), "root", rootPassword)) {
                validateMysqlRoot(connection);
                applyMysqlReplicaConfiguration(connection, plan);
            } catch (SQLException ex) {
                return "Configuration refusée: mot de passe root incorrect, connexion impossible ou privilège MySQL insuffisant. Détail: "
                        + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
            return "Configuration replica MySQL appliquée sur " + host + ":" + port
                    + ". Le master utilisé est " + plan.masterHost() + ":" + plan.masterPort()
                    + ". Voulez-vous que je lance maintenant le test de réplication sur le replica ?";
        });
    }

    @Tool("Teste l'etat de la replication MySQL sur une machine replica apres configuration")
    public String testMysqlReplicaStatus(
            @P("Host ou IP du serveur MySQL replica a tester") String replicaHost,
            @P("Port MySQL du replica; mettre 0 pour 3306") int replicaPort,
            @P("Mot de passe root MySQL du replica. Jemima doit le demander a l'utilisateur avant d'appeler ce tool") String mysqlRootPassword) {
        String key = safe(replicaHost, "") + "|" + replicaPort + "|" + securePasswordKey(mysqlRootPassword);
        return executeOnce("testMysqlReplicaStatus", key, () -> {
            String host = safe(replicaHost, "").trim();
            if (host.isBlank()) {
                return "Test refusé: indiquez le host ou l'adresse IP de la machine replica.";
            }
            String rootPassword = resolveMysqlRootPassword(mysqlRootPassword);
            if (rootPassword == null || rootPassword.isBlank()) {
                return secureMysqlPasswordRequest("test", "", host, replicaPort <= 0 ? 3306 : replicaPort);
            }
            int port = replicaPort <= 0 ? 3306 : replicaPort;
            try (Connection connection = DriverManager.getConnection(mysqlJdbcUrl(host, port, null), "root", rootPassword)) {
                validateMysqlRoot(connection);
                return mysqlReplicaStatusMarkdown(connection, host, port);
            } catch (SQLException ex) {
                return "Test refusé: mot de passe root incorrect, connexion impossible ou privilège MySQL insuffisant. Détail: "
                        + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Recherche et retourne la liste de tous les produits disponibles")
    public String listAllProducts() {
        return executeOnce("listAllProducts", "all", () -> formatProducts(ProduitDelegate.findProduits()));
    }

    @Tool("Recherche les produits selon n'importe quel critere parmi les champs du produit")
    public String searchProductsByCriteria(
            @P("Critères JSON: uid, nomProduit, codebar, marque, modele, taille, couleur, methodeInventaire, category") String criteriaJson) {
        return executeOnce("searchProductsByCriteria", criteriaJson, () -> {
            Map<String, Object> criteria = parseJsonMap(criteriaJson);
            List<Produit> products = ProduitDelegate.findProduits();
            if (criteria.isEmpty()) {
                return formatProducts(products);
            }
            List<Produit> filtered = products == null ? List.of() : products.stream()
                    .filter(product -> productMatches(product, criteria))
                    .toList();
            return formatProducts(filtered);
        });
    }

    @Tool("Modifie plusieurs produits en une seule operation selon une liste JSON de changements")
    public String bulkUpdateProducts(
            @P("Liste JSON d'objets. Chaque objet doit contenir uid ou codebar, puis les champs a modifier: nomProduit, codebar, marque, modele, taille, couleur, methodeInventaire, category") String updatesJson) {
        return executeOnce("bulkUpdateProducts", updatesJson, () -> {
            List<Map<String, Object>> updates = parseJsonList(updatesJson);
            if (updates.isEmpty()) {
                return "Aucune modification produit valide à appliquer.";
            }
            List<String> updated = new ArrayList<>();
            List<String> missing = new ArrayList<>();
            for (Map<String, Object> update : updates) {
                Produit product = findProductForUpdate(update);
                if (product == null) {
                    missing.add(String.valueOf(update));
                    continue;
                }
                applyProductUpdate(product, update);
                Produit saved = ProduitDelegate.updateProduit(product);
                syncUpdate(saved, Tables.PRODUIT);
                updated.add(productLine(saved));
            }
            StringBuilder result = new StringBuilder();
            result.append(updated.size()).append(" produit(s) modifié(s).");
            if (!updated.isEmpty()) {
                result.append("\n\n").append(String.join("\n", updated));
            }
            if (!missing.isEmpty()) {
                result.append("\n\nProduit(s) introuvable(s):\n").append(String.join("\n", missing));
            }
            return result.toString();
        });
    }

    @Tool("Cree un ou plusieurs produits, deduit la categorie si elle manque, puis demande les mesures a enregistrer")
    public String createProductsAndAskMeasures(
            @P("Liste JSON de produits: nomProduit obligatoire; marque, modele, taille, couleur, codebar, methodeInventaire et category optionnels") String productsJson) {
        return executeOnce("createProductsAndAskMeasures", productsJson, () -> {
            List<Map<String, Object>> inputs = parseJsonList(productsJson);
            if (inputs.isEmpty()) {
                Map<String, Object> single = parseJsonMap(productsJson);
                if (!single.isEmpty()) {
                    inputs = List.of(single);
                }
            }
            if (inputs.isEmpty()) {
                return "Aucun produit valide à créer.";
            }
            List<Produit> created = new ArrayList<>();
            List<String> skipped = new ArrayList<>();
            for (Map<String, Object> input : inputs) {
                String rawName = firstValue(input, "nomProduit", "nom", "name", "productName");
                if (rawName.isBlank()) {
                    skipped.add(String.valueOf(input));
                    continue;
                }
                ProductNameParts parts = productNameParts(rawName);
                String productName = parts.name();
                Produit existing = findExistingProduct(productName);
                if (existing != null) {
                    created.add(ensureProductBarcode(existing));
                    continue;
                }
                Produit product = new Produit();
                product.setUid(DataId.generate());
                product.setCodebar(safe(firstValue(input, "codebar", "barcode", "codebarre"), generateUniqueInvoiceBarcode()));
                product.setNomProduit(productName);
                product.setMarque(safe(firstValue(input, "marque", "brand"), parts.brand()));
                product.setModele(safe(firstValue(input, "modele", "model"), parts.model()));
                product.setTaille(safe(firstValue(input, "taille", "size"), ""));
                product.setCouleur(safe(firstValue(input, "couleur", "color"), ""));
                product.setMethodeInventaire(safe(firstValue(input, "methodeInventaire", "inventaire", "inventoryMethod"), "FIFO"));
                String category = firstValue(input, "category", "categorie", "catégorie");
                product.setCategoryId(findOrCreateCategory(category.isBlank() ? inferProductCategory(productName + " " + product.getMarque() + " " + product.getModele()) : category));
                product.setDateCreation(LocalDateTime.now());
                Produit saved = ProduitDelegate.saveProduit(product);
                syncCreate(saved, Tables.PRODUIT);
                created.add(saved);
            }
            if (created.isEmpty()) {
                return "Aucun produit n'a été créé.\n\nProduits ignorés:\n" + String.join("\n", skipped);
            }
            StringBuilder result = new StringBuilder();
            result.append(created.size()).append(" produit(s) prêt(s):\n");
            for (Produit product : created) {
                result.append("- ").append(productLine(product)).append("\n");
            }
            if (!skipped.isEmpty()) {
                result.append("\nProduit(s) ignoré(s):\n").append(String.join("\n", skipped)).append("\n");
            }
            result.append("""

                    Veuillez maintenant indiquer les mesures à enregistrer pour chaque produit au format:

                    uidProduit, descriptionMesure, quantiteContenu

                    Exemple:
                    """);
            for (Produit product : created) {
                result.append(product.getUid()).append(", Pièce, 1\n");
            }
            return result.toString().trim();
        });
    }

    @Tool("Enregistre les mesures de produits selon une liste JSON ou des lignes uidProduit, description, quantite")
    public String createProductMeasures(
            @P("Mesures JSON ou lignes CSV: uidProduit, descriptionMesure, quantiteContenu") String measuresInput) {
        return executeOnce("createProductMeasures", measuresInput, () -> {
            List<Map<String, Object>> measures = parseMeasureInputs(measuresInput);
            if (measures.isEmpty()) {
                return "Aucune mesure valide à enregistrer.";
            }
            List<String> savedLines = new ArrayList<>();
            List<String> missing = new ArrayList<>();
            for (Map<String, Object> measureInput : measures) {
                String productId = firstValue(measureInput, "uidProduit", "produitId", "productId", "uid");
                Produit product = productId.isBlank() ? null : ProduitDelegate.findProduit(productId);
                if (product == null) {
                    missing.add(String.valueOf(measureInput));
                    continue;
                }
                String description = safe(firstValue(measureInput, "descriptionMesure", "description", "mesure", "measureName"), "Pièce");
                double quantity = parseDouble(firstValue(measureInput, "quantiteContenu", "quantite", "quantity", "contenu"), 1d);
                Mesure existing = MesureDelegate.findByProduitAndQuant(product.getUid(), quantity);
                if (existing != null) {
                    savedLines.add(product.getNomProduit() + " -> mesure déjà existante: " + existing.getDescription() + " (" + existing.getQuantContenu() + ")");
                    continue;
                }
                Mesure mesure = new Mesure(DataId.generate());
                mesure.setProduitId(product);
                mesure.setDescription(description);
                mesure.setQuantContenu(quantity);
                Mesure saved = MesureDelegate.saveMesure(mesure);
                syncCreate(saved, Tables.MESURE);
                savedLines.add(product.getNomProduit() + " -> " + saved.getDescription() + " (" + saved.getQuantContenu() + ")");
            }
            StringBuilder result = new StringBuilder();
            result.append(savedLines.size()).append(" mesure(s) traitée(s).");
            if (!savedLines.isEmpty()) {
                result.append("\n\n").append(String.join("\n", savedLines));
            }
            if (!missing.isEmpty()) {
                result.append("\n\nProduit(s) introuvable(s) pour ces mesures:\n").append(String.join("\n", missing));
            }
            return result.toString();
        });
    }

    public String insertSaleOutput(SaleDraft draft) {
        if (draft == null || !draft.hasLines()) {
            return "Aucune ligne de vente valide à enregistrer.";
        }
        String workflowId = registerSaleWorkflow(draft);
        createSaleAndLines(workflowId);
        return createSaleTreasuryAndSync(workflowId);
    }

    public String registerSaleWorkflow(SaleDraft draft) {
        String workflowId = "sale-" + DataId.generate();
        SALE_WORKFLOWS.put(workflowId, new SaleWorkflowContext(draft));
        return workflowId;
    }

    public String saleWorkflowState(String workflowId) {
        SaleWorkflowContext context = saleWorkflow(workflowId);
        return context == null ? "Workflow vente introuvable: " + workflowId : context.summary();
    }

    @Tool("Demande confirmation avant d'annuler un workflow facture ou vente en cours")
    public String requestWorkflowCancellation(
            @P("sessionId interne de Jemima") String sessionId,
            @P("workflowId optionnel. Laisser vide pour le dernier workflow actif.") String workflowId) {
        cleanupWorkflowCancellationRequests();
        String sessionKey = normalizeToolKey(sessionId);
        WorkflowTarget target = resolveWorkflowTarget(workflowId);
        if (target == null) {
            WORKFLOW_CANCELLATION_REQUESTS.remove(sessionKey);
            return "Aucun workflow actif à annuler. Les workflows terminés ou déjà annulés ne sont pas modifiés.";
        }
        long now = System.currentTimeMillis();
        WorkflowCancellationRequest request = new WorkflowCancellationRequest(sessionKey, target.workflowId(), target.type(), now, now + WORKFLOW_CANCELLATION_TTL_MS);
        WORKFLOW_CANCELLATION_REQUESTS.put(sessionKey, request);
        return "Voulez-vous vraiment annuler le workflow en cours " + target.workflowId()
                + " (" + target.typeLabel() + ") ?\n\n"
                + "Répondez `oui` pour annuler ou `non` pour le laisser continuer.\n"
                + "Sans réponse dans 3 minutes, Jemima abandonnera l'annulation et le workflow continuera normalement.";
    }

    @Tool("Confirme ou refuse l'annulation du workflow en attente pour la session Jemima")
    public String answerWorkflowCancellation(
            @P("sessionId interne de Jemima") String sessionId,
            @P("Réponse utilisateur: oui pour annuler, non pour garder le workflow") String answer) {
        cleanupWorkflowCancellationRequests();
        String sessionKey = normalizeToolKey(sessionId);
        WorkflowCancellationRequest request = WORKFLOW_CANCELLATION_REQUESTS.get(sessionKey);
        if (request == null) {
            return "Aucune demande d'annulation active. Le workflow en cours continue normalement.";
        }
        if (System.currentTimeMillis() > request.expiresAtMs()) {
            WORKFLOW_CANCELLATION_REQUESTS.remove(sessionKey);
            return "Le délai de 3 minutes est dépassé. Jemima quitte l'annulation et laisse le workflow continuer.";
        }
        if (!isPositiveConfirmation(answer)) {
            WORKFLOW_CANCELLATION_REQUESTS.remove(sessionKey);
            return "Annulation refusée. Le workflow " + request.workflowId() + " continue normalement.";
        }
        String result = cancelWorkflow(request.workflowId(), "Workflow annulé après confirmation utilisateur.");
        WORKFLOW_CANCELLATION_REQUESTS.remove(sessionKey);
        return result;
    }

    public boolean hasPendingWorkflowCancellation(String sessionId) {
        cleanupWorkflowCancellationRequests();
        return WORKFLOW_CANCELLATION_REQUESTS.containsKey(normalizeToolKey(sessionId));
    }

    public String pendingWorkflowCancellationState(String sessionId) {
        cleanupWorkflowCancellationRequests();
        WorkflowCancellationRequest request = WORKFLOW_CANCELLATION_REQUESTS.get(normalizeToolKey(sessionId));
        if (request == null) {
            return "Aucune confirmation d'annulation en attente.";
        }
        long remainingSeconds = Math.max(0L, (request.expiresAtMs() - System.currentTimeMillis()) / 1000L);
        return "Confirmation en attente pour " + request.workflowId()
                + " (" + request.type() + "), temps restant " + remainingSeconds + " seconde(s).";
    }

    @Tool("Cree la vente et ses lignes de vente pour une sortie enregistree dans un workflow")
    public String createSaleAndLines(@P("workflowId") String workflowId) {
        return executeOnce("createSaleAndLines", workflowId, () -> {
            SaleWorkflowContext context = saleWorkflow(workflowId);
            if (context == null) {
                return "Workflow vente introuvable: " + workflowId;
            }
            if (context.cancelled) {
                return context.cancelMessage;
            }
            if (context.saleCreated) {
                return "Vente deja creee: " + context.sale.getReference();
            }
            Client client = findOrCreateSaleClient(context.draft);
            Vente sale = findExistingSale(context.reference, context.date, context.region);
            boolean newSale = sale == null;
            if (newSale) {
                sale = new Vente(DataId.generateInt());
                sale.setReference(context.reference);
                sale.setDateVente(context.date);
                sale.setRegion(context.region);
                sale.setClientId(client);
                sale.setPayment(TypeTraisorerie.CAISSE.name());
                sale.setLibelle("Sortie créée par Jemima");
                sale.setObservation("Vente");
                sale.setDeviseDette(safe(context.draft.getCurrency(), "USD"));
            }
            List<LigneVente> lines = new ArrayList<>();
            double totalUsd = 0d;
            double totalCdf = 0d;
            for (int i = 0; i < context.draft.getLines().size(); i++) {
                SaleLine draftLine = context.draft.getLines().get(i);
                Produit product = findSaleProduct(draftLine);
                if (product == null) {
                    return "Produit introuvable pour la ligne " + (i + 1) + ": " + draftLine.getProductName();
                }
                Mesure measure = findSaleMeasure(product, draftLine);
                double price = saleUnitPrice(product, draftLine);
                if (price <= 0) {
                    return "Le prix de vente manque pour " + product.getNomProduit()
                            + ". Répondez avec: " + (i + 1) + ", " + draftLine.getQuantity() + ", prix";
                }
                double quantity = draftLine.getQuantity() <= 0 ? 1d : draftLine.getQuantity();
                LigneVente line = findExistingSaleLine(sale, product, measure, draftLine);
                if (line == null) {
                    line = new LigneVente(System.currentTimeMillis() + i + 101);
                }
                line.setReference(sale);
                line.setClientId(client.getUid());
                line.setProductId(product);
                line.setMesureId(measure);
                line.setQuantite(quantity);
                line.setPrixUnit(price);
                line.setNumlot("");
                line.setCoutAchat(0d);
                if ("CDF".equalsIgnoreCase(context.draft.getCurrency())) {
                    line.setMontantCdf(price * quantity);
                    totalCdf += line.getMontantCdf();
                } else {
                    line.setMontantUsd(price * quantity);
                    totalUsd += line.getMontantUsd();
                }
                lines.add(line);
            }
            sale.setMontantUsd(totalUsd);
            sale.setMontantCdf(totalCdf);
            sale.setMontantDette(0d);
            Vente savedSale = newSale ? VenteDelegate.saveVente(sale) : VenteDelegate.updateVente(sale);
            if (newSale) {
                syncCreate(savedSale, Tables.VENTE);
            } else {
                syncUpdate(savedSale, Tables.VENTE);
            }
            List<LigneVente> savedLines = new ArrayList<>();
            for (LigneVente line : lines) {
                line.setReference(savedSale);
                boolean newLine = LigneVenteDelegate.findLigneVente(line.getUid()) == null;
                LigneVente savedLine = newLine ? LigneVenteDelegate.saveLigneVente(line) : LigneVenteDelegate.updateLigneVente(line);
                if (newLine) {
                    syncCreate(savedLine, Tables.LIGNEVENTE);
                } else {
                    syncUpdate(savedLine, Tables.LIGNEVENTE);
                }
                savedLines.add(savedLine);
            }
            savedSale.setLigneVenteList(savedLines);
            context.client = client;
            context.sale = savedSale;
            context.lines = savedLines;
            context.saleCreated = true;
            return (newSale ? "Vente creee: " : "Vente existante mise a jour: ") + savedSale.getReference() + ", lignes=" + savedLines.size()
                    + ", total USD=" + totalUsd + ", total CDF=" + totalCdf;
        });
    }

    @Tool("Cree ou retrouve le compte tresor, enregistre la traisorerie de vente et synchronise par HTTPS")
    public String createSaleTreasuryAndSync(@P("workflowId") String workflowId) {
        return executeOnce("createSaleTreasuryAndSync", workflowId, () -> {
            SaleWorkflowContext context = saleWorkflow(workflowId);
            if (context == null) {
                return "Workflow vente introuvable: " + workflowId;
            }
            if (context.cancelled) {
                return context.cancelMessage;
            }
            if (!context.saleCreated) {
                createSaleAndLines(workflowId);
            }
            if (context.cancelled) {
                return context.cancelMessage;
            }
            if (context.treasuryCreated) {
                return "Tresorerie deja creee pour la vente " + context.sale.getReference();
            }
            CompteTresor account = findOrCreateSaleAccount(context.region);
            String reference = "BE" + Constants.dateTodayRef(context.date.toLocalDate());
            Traisorerie treasury = TraisorerieDelegate.findExistingOf(reference, context.date.toLocalDate(), account.getUid(), context.region);
            boolean newTreasury = treasury == null;
            if (newTreasury) {
                treasury = new Traisorerie(DataId.generate());
                treasury.setReference(reference);
            }
            treasury.setDate(context.date);
            treasury.setLibelle("Ventes journalier");
            treasury.setMontantUsd(context.sale.getMontantUsd());
            treasury.setMontantCdf(context.sale.getMontantCdf());
            treasury.setMouvement(Mouvment.AUGMENTATION.name());
            treasury.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
            treasury.setRegion(context.region);
            treasury.setTresorId(account);
            treasury.setSoldeUsd(context.sale.getMontantUsd());
            treasury.setSoldeCdf(context.sale.getMontantCdf());
            Traisorerie savedTreasury = newTreasury ? TraisorerieDelegate.saveTraisorerie(treasury) : TraisorerieDelegate.updateTraisorerie(treasury);
            if (newTreasury) {
                syncCreate(savedTreasury, Tables.TRAISORERIE);
            } else {
                syncUpdate(savedTreasury, Tables.TRAISORERIE);
            }
            context.account = account;
            context.treasury = savedTreasury;
            context.treasuryCreated = true;
            String http = syncSaleByHttps(context);
            return "Sortie enregistrée avec succès: " + context.sale.getReference()
                    + "\nCompte: " + account.getIntitule()
                    + "\nTransaction: " + savedTreasury.getUid()
                    + "\nSynchronisation HTTPS: " + http;
        });
    }

    public String insertExpenseOutput(ExpenseDraft draft) {
        if (draft == null || !draft.isUsable()) {
            return "Aucune dépense valide à enregistrer.";
        }
        String workflowId = registerExpenseWorkflow(draft);
        prepareExpenseCategoryAndAccount(workflowId);
        return createExpenseTreasuryAndOperation(workflowId);
    }

    public String registerExpenseWorkflow(ExpenseDraft draft) {
        String workflowId = "expense-" + DataId.generate();
        EXPENSE_WORKFLOWS.put(workflowId, new ExpenseWorkflowContext(draft));
        return workflowId;
    }

    public String expenseWorkflowState(String workflowId) {
        ExpenseWorkflowContext context = expenseWorkflow(workflowId);
        return context == null ? "Workflow dépense introuvable: " + workflowId : context.summary();
    }

    @Tool("Prepare la categorie de depense et le compte tresor pour un workflow depense")
    public String prepareExpenseCategoryAndAccount(@P("workflowId") String workflowId) {
        return executeOnce("prepareExpenseCategoryAndAccount", workflowId, () -> {
            ExpenseWorkflowContext context = expenseWorkflow(workflowId);
            if (context == null) {
                return "Workflow dépense introuvable: " + workflowId;
            }
            if (context.cancelled) {
                return context.cancelMessage;
            }
            if (context.prepared) {
                return "Dépense et compte déjà préparés pour " + workflowId + ".";
            }
            context.expense = findOrCreateExpense(context.draft, context.region);
            context.account = findOrCreateExpenseAccount(context.draft, context.region);
            context.prepared = true;
            return "Préparation dépense terminée: "
                    + context.expense.getNomDepense()
                    + ", compte " + context.account.getIntitule()
                    + " (" + context.account.getTypeCompte() + ").";
        });
    }

    @Tool("Cree l'ecriture de traisorerie et l'operation pour un workflow depense")
    public String createExpenseTreasuryAndOperation(@P("workflowId") String workflowId) {
        return executeOnce("createExpenseTreasuryAndOperation", workflowId, () -> {
            ExpenseWorkflowContext context = expenseWorkflow(workflowId);
            if (context == null) {
                return "Workflow dépense introuvable: " + workflowId;
            }
            if (context.cancelled) {
                return context.cancelMessage;
            }
            if (!context.prepared) {
                prepareExpenseCategoryAndAccount(workflowId);
            }
            if (context.cancelled) {
                return context.cancelMessage;
            }
            if (context.operationCreated) {
                return "Dépense déjà enregistrée: " + context.operation.getReferenceOp();
            }
            Traisorerie treasury = findExistingExpenseTreasury(context);
            boolean newTreasury = treasury == null;
            if (newTreasury) {
                treasury = new Traisorerie(DataId.generate());
                treasury.setReference(context.reference);
            }
            treasury.setDate(context.date);
            treasury.setLibelle(safe(context.draft.getDescription(), context.draft.getExpenseName()));
            if (isCdf(context.draft.getCurrency())) {
                treasury.setMontantCdf(context.draft.getAmount());
                treasury.setMontantUsd(0d);
            } else {
                treasury.setMontantUsd(context.draft.getAmount());
                treasury.setMontantCdf(0d);
            }
            treasury.setMouvement(Mouvment.DIMINUTION.name());
            treasury.setTypeTresorerie(context.account.getTypeCompte());
            treasury.setRegion(context.region);
            treasury.setTresorId(context.account);
            treasury.setSoldeUsd(treasury.getMontantUsd());
            treasury.setSoldeCdf(treasury.getMontantCdf());
            Traisorerie savedTreasury = newTreasury ? TraisorerieDelegate.saveTraisorerie(treasury) : TraisorerieDelegate.updateTraisorerie(treasury);
            if (newTreasury) {
                syncCreate(savedTreasury, Tables.TRAISORERIE);
            } else {
                syncUpdate(savedTreasury, Tables.TRAISORERIE);
            }

            Operation operation = findExistingExpenseOperation(context, savedTreasury);
            boolean newOperation = operation == null;
            if (newOperation) {
                operation = new Operation(DataId.generate());
            }
            operation.setCaisseOpId(savedTreasury);
            operation.setDate(savedTreasury.getDate());
            operation.setImputation(safe(context.draft.getImputation(), "GENERAL"));
            operation.setLibelle(savedTreasury.getLibelle());
            operation.setMontantCdf(savedTreasury.getMontantCdf());
            operation.setMontantUsd(savedTreasury.getMontantUsd());
            operation.setMouvement(Mouvment.AUGMENTATION.name());
            operation.setReferenceOp(savedTreasury.getReference());
            operation.setRegion(context.region);
            operation.setTresorId(context.account);
            operation.setDepenseId(context.expense);
            Operation savedOperation = newOperation ? OperationDelegate.saveOperation(operation) : OperationDelegate.updateOperation(operation);
            if (newOperation) {
                syncCreate(savedOperation, Tables.OPERATION);
            } else {
                syncUpdate(savedOperation, Tables.OPERATION);
            }
            DepenseAgregateDelegate.aggregateDepense(savedOperation.getDate(), savedOperation.getImputation(),
                    savedOperation.getMontantUsd(), savedOperation.getMontantCdf(), context.expense);
            context.treasury = savedTreasury;
            context.operation = savedOperation;
            context.operationCreated = true;
            String http = syncExpenseByHttps(savedTreasury, savedOperation);
            return "Dépense enregistrée avec succès: " + savedOperation.getReferenceOp()
                    + "\nCatégorie: " + context.expense.getNomDepense()
                    + "\nCompte: " + context.account.getIntitule()
                    + "\nMontant USD=" + savedOperation.getMontantUsd()
                    + ", CDF=" + savedOperation.getMontantCdf()
                    + "\nSynchronisation HTTPS: " + http;
        });
    }

    @Tool("Corrige les produits dont le code-barres est vide en générant un code numérique unique de 13 chiffres")
    public String fixEmptyProductBarcodes() {
        return executeOnce("fixEmptyProductBarcodes", "all-empty-barcodes", () -> {
            List<Produit> products = ProduitDelegate.findProduits();
            if (products == null || products.isEmpty()) {
                return "Aucun produit trouvé.";
            }
            List<String> fixed = new ArrayList<>();
            List<String> missing = new ArrayList<>();
            for (Produit candidate : products) {
                if (candidate == null || candidate.getUid() == null || candidate.getUid().isBlank()) {
                    continue;
                }
                Produit product = ProduitDelegate.findProduit(candidate.getUid());
                if (product == null) {
                    missing.add(candidate.getUid());
                    continue;
                }
                if (product.getCodebar() != null && !product.getCodebar().isBlank()) {
                    continue;
                }
                product.setCodebar(generateUniqueInvoiceBarcode());
                Produit updated = ProduitDelegate.updateProduit(product);
                syncUpdate(updated, Tables.PRODUIT);
                fixed.add(productLine(updated));
            }
            if (fixed.isEmpty() && missing.isEmpty()) {
                return "Aucun produit avec code-barres vide n'a été trouvé.";
            }
            StringBuilder result = new StringBuilder();
            result.append(fixed.size()).append(" produit(s) corrigé(s).");
            if (!fixed.isEmpty()) {
                result.append("\n\n").append(String.join("\n", fixed));
            }
            if (!missing.isEmpty()) {
                result.append("\n\nProduit(s) introuvable(s) en base:\n").append(String.join("\n", missing));
            }
            return result.toString();
        });
    }

    @Tool("Detecte les produits doublons par nomProduit, marque, modele, taille et couleur, puis demande confirmation avant suppression")
    public String detectDuplicateProductsForDeletion() {
        return executeOnce("detectDuplicateProductsForDeletion", "all-products", () -> {
            List<Produit> products = ProduitDelegate.findProduits();
            if (products == null || products.isEmpty()) {
                return "Aucun produit trouvé.";
            }
            List<List<Produit>> groups = duplicateProductGroups(products);
            List<DuplicateProductEntry> entries = new ArrayList<>();
            int groupNumber = 1;
            for (List<Produit> group : groups) {
                if (group.size() < 2) {
                    continue;
                }
                for (Produit product : group) {
                    entries.add(new DuplicateProductEntry(entries.size() + 1, groupNumber, product));
                }
                groupNumber++;
            }
            if (entries.isEmpty()) {
                return "Aucun doublon produit trouvé par comparaison des mots de nom, marque, modele, taille et couleur.";
            }
            String batchId = "dup-prod-" + DataId.generate();
            DUPLICATE_PRODUCT_BATCHES.put(batchId, new DuplicateProductBatch(batchId, entries, LocalDateTime.now()));
            StringBuilder builder = new StringBuilder();
            builder.append("Produits doublons détectés.\n")
                    .append("Lot: ").append(batchId).append("\n\n")
                    .append("|numero|groupe|uid|code bar|nom produit|marque|modele|taille|couleur|categorie|\n")
                    .append("|---|---|---|---|---|---|---|---|---|---|\n");
            for (DuplicateProductEntry entry : entries) {
                builder.append("|").append(entry.number())
                        .append("|").append(entry.groupNumber())
                        .append("|").append(tableCell(entry.product().getUid()))
                        .append("|").append(tableCell(entry.product().getCodebar()))
                        .append("|").append(tableCell(entry.product().getNomProduit()))
                        .append("|").append(tableCell(entry.product().getMarque()))
                        .append("|").append(tableCell(entry.product().getModele()))
                        .append("|").append(tableCell(entry.product().getTaille()))
                        .append("|").append(tableCell(entry.product().getCouleur()))
                        .append("|").append(tableCell(entry.product().getCategoryId() == null ? "-" : entry.product().getCategoryId().getDescritption()))
                        .append("|\n");
            }
            builder.append("\nConfirmez les produits à supprimer avec le format: ")
                    .append("numero ou plusieurs numeros séparés par virgule, par exemple `2, 5, 8`.\n")
                    .append("Jemima utilisera le lot ").append(batchId).append(".");
            return builder.toString();
        });
    }

    @Tool("Supprime les produits doublons confirmes par numero depuis un lot detecte par detectDuplicateProductsForDeletion")
    public String deleteConfirmedDuplicateProducts(
            @P("batchId retourne par detectDuplicateProductsForDeletion") String batchId,
            @P("Numeros des produits a supprimer, separes par virgule, espace ou retour ligne") String selectedNumbers) {
        return executeOnce("deleteConfirmedDuplicateProducts", safe(batchId, "") + "|" + safe(selectedNumbers, ""), () -> {
            DuplicateProductBatch batch = DUPLICATE_PRODUCT_BATCHES.get(batchId == null ? "" : batchId.trim());
            if (batch == null) {
                return "Lot de doublons introuvable. Demandez d'abord à Jemima d'afficher les produits doublons.";
            }
            List<Integer> numbers = parseSelectedNumbers(selectedNumbers);
            if (numbers.isEmpty()) {
                return "Aucun numéro valide reçu. Indiquez les numéros à supprimer, par exemple `2, 5, 8`.";
            }
            List<String> deleted = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            TransferDeliveryContext transferDelivery = new TransferDeliveryContext();
            for (Integer number : numbers) {
                DuplicateProductEntry entry = batch.find(number);
                if (entry == null) {
                    failed.add("Numero " + number + ": introuvable dans le lot.");
                    continue;
                }
                Produit keeper = findDuplicateKeeper(batch, entry, numbers);
                if (keeper == null) {
                    failed.add("Numero " + number + ": impossible de supprimer car aucun produit du groupe "
                            + entry.groupNumber() + " ne reste comme produit à garder.");
                    continue;
                }
                Produit product = ProduitDelegate.findProduit(entry.product().getUid());
                if (product == null) {
                    failed.add("Numero " + number + ": produit deja absent.");
                    continue;
                }
                try {
                    MovementTransferSummary transferSummary = transferProductMovements(product, keeper, transferDelivery);
                    deleteProductMeasures(product);
                    ProduitDelegate.deleteProduit(product);
                    syncDelete(product, Tables.PRODUIT);
                    deleted.add(number + ". " + duplicateProductLine(product)
                            + " -> mouvements transférés vers " + safe(keeper.getNomProduit(), keeper.getUid())
                            + " (" + transferSummary.format() + ")");
                } catch (Exception ex) {
                    failed.add("Numero " + number + ": " + safe(ex.getMessage(), ex.getClass().getSimpleName()));
                }
            }
            if (!deleted.isEmpty()) {
                removeDeletedDuplicateEntries(batch, numbers);
            }
            StringBuilder builder = new StringBuilder();
            builder.append("Suppression des doublons terminée.\n");
            if (!deleted.isEmpty()) {
                builder.append("\nSupprimé(s):\n").append(String.join("\n", deleted)).append("\n");
            }
            if (!failed.isEmpty()) {
                builder.append("\nNon supprimé(s):\n").append(String.join("\n", failed));
            }
            return builder.toString().trim();
        });
    }

    @Tool("Prepare et affiche les codes-barres ou QR codes des produits avant impression thermique. Peut cibler tous les produits, un nombre limite, ou des noms explicites.")
    public String prepareAllProductCodesForThermalPrint(
            @P("Format souhaite: auto, barcode ou qrcode. auto utilise code-barres pour les codes numeriques 12/13 chiffres, QR code sinon.") String format,
            @P("Noms de produits a cibler, separes par virgule ou retour ligne. Laisser vide pour tous les produits.") String productTargets,
            @P("Nombre maximum de produits a preparer. Mettre vide, 0 ou negatif pour tous les produits retenus.") String maxProducts) {
        return executeOnce("prepareAllProductCodesForThermalPrint", safe(format, "auto") + "|" + safe(productTargets, "all") + "|" + safe(maxProducts, "all"), () -> {
            List<Produit> products = ProduitDelegate.findProduits();
            if (products == null || products.isEmpty()) {
                return "Aucun produit trouvé.";
            }
            List<String> targets = parseProductPrintTargets(productTargets);
            int max = parsePositiveInt(maxProducts);
            List<ProductCodePrintItem> items = new ArrayList<>();
            List<String> skipped = new ArrayList<>();
            List<String> matchedProductNames = new ArrayList<>();
            String requestedFormat = normalizeCodeFormat(format);
            for (Produit product : products) {
                if (!targets.isEmpty() && !matchesPrintTarget(product, targets)) {
                    continue;
                }
                matchedProductNames.add(safe(product.getNomProduit(), product.getUid()));
                if (product == null || product.getCodebar() == null || product.getCodebar().isBlank()) {
                    skipped.add(product == null ? "Produit inconnu" : safe(product.getNomProduit(), product.getUid()));
                    continue;
                }
                String code = product.getCodebar().trim();
                String formatLabel = codePrintFormat(code, requestedFormat);
                items.add(new ProductCodePrintItem(
                        safe(product.getNomProduit(), "Produit"),
                        safe(product.getMarque(), "-"),
                        safe(product.getModele(), "-"),
                        code,
                        formatLabel,
                        codeImageMarkdown(code, formatLabel)));
                if (max > 0 && items.size() >= max) {
                    break;
                }
            }
            if (!targets.isEmpty() && matchedProductNames.isEmpty()) {
                return "Aucun produit ne correspond au ciblage demandé: " + String.join(", ", targets);
            }
            if (items.isEmpty()) {
                return "Aucun produit avec codebar n'a été trouvé.";
            }
            String batchId = "codes-" + DataId.generate();
            PRODUCT_CODE_PRINT_BATCHES.put(batchId, new ProductCodePrintBatch(batchId, items, LocalDateTime.now()));
            StringBuilder builder = new StringBuilder();
            builder.append("Aperçu des codes générés avant impression thermique.\n")
                    .append("Lot: ").append(batchId).append("\n")
                    .append("Total à imprimer: ").append(items.size()).append("\n");
            if (!targets.isEmpty()) {
                builder.append("Ciblage: ").append(String.join(", ", targets)).append("\n");
            }
            if (max > 0) {
                builder.append("Limite demandée: ").append(max).append("\n");
            }
            builder.append("\n");
            builder.append("|code-bar / QR code|nom produit|\n")
                    .append("|---|---|\n");
            for (int i = 0; i < items.size(); i++) {
                ProductCodePrintItem item = items.get(i);
                builder.append("|")
                        .append(item.imageMarkdown()).append(" `").append(item.code()).append("`")
                        .append("|")
                        .append(tableCell(item.productName())).append(" ")
                        .append(tableCell(item.brand())).append(" ")
                        .append(tableCell(item.model())).append("|\n");
            }
            if (!skipped.isEmpty()) {
                builder.append("\nProduit(s) ignoré(s) car codebar vide: ").append(skipped.size()).append(".");
            }
            builder.append("\n\nConfirmez si vous voulez tout imprimer. Après votre accord, Jemima utilisera le lot ")
                    .append(batchId).append(".");
            return builder.toString();
        });
    }

    @Tool("Liste les imprimantes disponibles pour impression thermique ESC/POS")
    public String listThermalPrinters() {
        return executeOnce("listThermalPrinters", "all", () -> {
            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            StringBuilder builder = new StringBuilder("Imprimantes disponibles:\n");
            if (services == null || services.length == 0) {
                return "Aucune imprimante détectée par le système.";
            }
            for (int i = 0; i < services.length; i++) {
                String name = services[i].getName();
                builder.append(i + 1).append(". ").append(name);
                if (defaultService != null && name.equals(defaultService.getName())) {
                    builder.append(" (par défaut)");
                }
                builder.append("\n");
            }
            return builder.toString().trim();
        });
    }

    @Tool("Imprime sur imprimante thermique ESC/POS tous les codes d'un lot prepare apres confirmation utilisateur")
    public String printPreparedProductCodesOnThermal(
            @P("batchId retourne par prepareAllProductCodesForThermalPrint") String batchId,
            @P("Nom exact de l'imprimante. Laisser vide pour utiliser l'imprimante par defaut du systeme.") String printerName) {
        return executeOnce("printPreparedProductCodesOnThermal", safe(batchId, "") + "|" + safe(printerName, "default"), () -> {
            ProductCodePrintBatch batch = PRODUCT_CODE_PRINT_BATCHES.get(batchId == null ? "" : batchId.trim());
            if (batch == null) {
                return "Lot d'impression introuvable. Demandez d'abord à Jemima de préparer les codes à imprimer.";
            }
            PrintService printService = findPrintService(printerName);
            if (printService == null) {
                return "Aucune imprimante thermique disponible. Vérifiez l'imprimante système ou donnez son nom exact.";
            }
            try {
                printProductCodes(printService, batch.items());
                PRODUCT_CODE_PRINT_BATCHES.remove(batch.batchId());
                return "Impression thermique terminée: " + batch.items().size()
                        + " code(s) imprimé(s) sur " + printService.getName() + ".";
            } catch (IOException | IllegalArgumentException ex) {
                return "Échec impression thermique: " + ex.getMessage();
            }
        });
    }

    private String executeOnce(String toolName, String businessKey, Supplier<String> action) {
        cleanupToolExecutionCache();
        String key = toolName + "|" + normalizeToolKey(businessKey);
        ToolExecutionResult existing = RECENT_TOOL_EXECUTIONS.get(key);
        long now = System.currentTimeMillis();
        if (existing != null && now - existing.createdAtMs() <= TOOL_EXECUTION_TTL_MS) {
            return existing.result();
        }
        synchronized (RECENT_TOOL_EXECUTIONS) {
            existing = RECENT_TOOL_EXECUTIONS.get(key);
            if (existing != null && now - existing.createdAtMs() <= TOOL_EXECUTION_TTL_MS) {
                return existing.result();
            }
            String result = action.get();
            if (shouldRememberToolResult(result)) {
                RECENT_TOOL_EXECUTIONS.put(key, new ToolExecutionResult(result, now));
            }
            return result;
        }
    }

    private void cleanupToolExecutionCache() {
        long now = System.currentTimeMillis();
        RECENT_TOOL_EXECUTIONS.entrySet().removeIf(entry -> now - entry.getValue().createdAtMs() > TOOL_EXECUTION_TTL_MS);
    }

    private String normalizeToolKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public String registerMysqlRootPasswordToken(String mysqlRootPassword) {
        if (mysqlRootPassword == null || mysqlRootPassword.isBlank()) {
            return "";
        }
        String token = SECURE_MYSQL_PASSWORD_TOKEN_PREFIX + DataId.generate();
        MYSQL_ROOT_PASSWORD_TOKENS.put(token, mysqlRootPassword);
        return token;
    }

    private String securePasswordKey(String value) {
        return value != null && value.startsWith(SECURE_MYSQL_PASSWORD_TOKEN_PREFIX) ? value : "password-dialog-required";
    }

    private String resolveMysqlRootPassword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!value.startsWith(SECURE_MYSQL_PASSWORD_TOKEN_PREFIX)) {
            // Un mot de passe écrit dans le chat ne doit jamais être utilisé; il faut passer par la boîte masquée.
            return null;
        }
        return MYSQL_ROOT_PASSWORD_TOKENS.remove(value);
    }

    private String secureMysqlPasswordRequest(String action, String planId, String replicaHost, int replicaPort) {
        return SECURE_MYSQL_PASSWORD_REQUEST
                + "|action=" + safe(action, "")
                + "|planId=" + safe(planId, "")
                + "|replicaHost=" + safe(replicaHost, "")
                + "|replicaPort=" + replicaPort;
    }

    private String publicMasterHost() {
        String configuredHost = pref.get("default_mysql_host", "");
        if (configuredHost != null && !configuredHost.isBlank() && !isLoopbackHost(configuredHost)) {
            return configuredHost.trim();
        }
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address.getHostAddress().contains(".")) {
                        return address.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (IOException ex) {
            return safe(configuredHost, "127.0.0.1");
        }
    }

    private boolean isLoopbackHost(String host) {
        String normalized = safe(host, "").trim().toLowerCase(Locale.ROOT);
        return normalized.equals("localhost") || normalized.equals("127.0.0.1") || normalized.equals("::1");
    }

    private boolean sameMysqlMachine(String masterHost, String replicaHost) {
        try {
            Set<String> masterAddresses = resolvedAddresses(masterHost);
            Set<String> replicaAddresses = resolvedAddresses(replicaHost);
            if (masterAddresses.isEmpty() || replicaAddresses.isEmpty()) {
                return safe(masterHost, "").trim().equalsIgnoreCase(safe(replicaHost, "").trim());
            }
            for (String address : masterAddresses) {
                if (replicaAddresses.contains(address)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return safe(masterHost, "").trim().equalsIgnoreCase(safe(replicaHost, "").trim());
        }
    }

    private Set<String> resolvedAddresses(String host) throws IOException {
        Set<String> addresses = new HashSet<>();
        if (isLoopbackHost(host)) {
            addresses.add("127.0.0.1");
            addresses.add("::1");
            addresses.addAll(localMachineAddresses());
            return addresses;
        }
        for (InetAddress address : InetAddress.getAllByName(host)) {
            addresses.add(address.getHostAddress());
        }
        return addresses;
    }

    private Set<String> localMachineAddresses() throws SocketException {
        Set<String> addresses = new HashSet<>();
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                addresses.add(address.getHostAddress());
            }
        }
        return addresses;
    }

    private String generatedMysqlPassword() {
        return "KsRepl-" + DataId.generate().substring(0, 12) + "!";
    }

    private String mysqlJdbcUrl(String host, int port, String database) {
        String db = database == null || database.isBlank() ? "" : "/" + database;
        return "jdbc:mysql://" + host + ":" + port + db
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private void validateMysqlRoot(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        }
    }

    private void applyMysqlReplicaConfiguration(Connection connection, MysqlReplicationPlan plan) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET GLOBAL server_id = " + plan.replicaServerId());
            statement.execute("STOP REPLICA");
            statement.execute("RESET REPLICA ALL");
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                CHANGE REPLICATION SOURCE TO
                    SOURCE_HOST = ?,
                    SOURCE_PORT = ?,
                    SOURCE_USER = ?,
                    SOURCE_PASSWORD = ?,
                    SOURCE_AUTO_POSITION = 1
                """)) {
            statement.setString(1, plan.masterHost());
            statement.setInt(2, plan.masterPort());
            statement.setString(3, plan.replicationUser());
            statement.setString(4, plan.replicationPassword());
            statement.execute();
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("START REPLICA");
        }
    }

    private String mysqlReplicaStatusMarkdown(Connection connection, String host, int port) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = replicaStatus(statement)) {
                if (!result.next()) {
                    return "Aucune réplication active trouvée sur " + host + ":" + port
                            + ". Vérifiez que la configuration replica a bien été appliquée.";
                }
                String ioRunning = firstNonBlank(result, "Replica_IO_Running", "Slave_IO_Running");
                String sqlRunning = firstNonBlank(result, "Replica_SQL_Running", "Slave_SQL_Running");
                String secondsBehind = firstNonBlank(result, "Seconds_Behind_Source", "Seconds_Behind_Master");
                String lastIoError = firstNonBlank(result, "Last_IO_Error", "Last_IO_Error");
                String lastSqlError = firstNonBlank(result, "Last_SQL_Error", "Last_SQL_Error");
                boolean ok = "yes".equalsIgnoreCase(ioRunning) && "yes".equalsIgnoreCase(sqlRunning);
                StringBuilder response = new StringBuilder();
                response.append(ok ? "Test de réplication réussi." : "Test de réplication à corriger.")
                        .append("\n\n")
                        .append("|Champ|Valeur|\n")
                        .append("|---|---|\n")
                        .append("|Replica IO|").append(tableCell(ioRunning)).append("|\n")
                        .append("|Replica SQL|").append(tableCell(sqlRunning)).append("|\n")
                        .append("|Retard secondes|").append(tableCell(secondsBehind)).append("|\n");
                if (!safe(lastIoError, "").isBlank()) {
                    response.append("|Dernière erreur IO|").append(tableCell(lastIoError)).append("|\n");
                }
                if (!safe(lastSqlError, "").isBlank()) {
                    response.append("|Dernière erreur SQL|").append(tableCell(lastSqlError)).append("|\n");
                }
                return response.toString();
            }
        }
    }

    private ResultSet replicaStatus(Statement statement) throws SQLException {
        try {
            return statement.executeQuery("SHOW REPLICA STATUS");
        } catch (SQLException ex) {
            return statement.executeQuery("SHOW SLAVE STATUS");
        }
    }

    private String firstNonBlank(ResultSet result, String... columns) {
        for (String column : columns) {
            try {
                String value = result.getString(column);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            } catch (SQLException ignored) {
            }
        }
        return "";
    }

    private String mysqlReplicationPlanMarkdown(MysqlReplicationPlan plan) {
        String databaseName = pref.get("eUid", "");
        String dbLine = databaseName == null || databaseName.isBlank()
                ? ""
                : "\nBase Kazisafe courante: `ksf_" + databaseName + "`";
        return """
                Plan de réplication MySQL généré.

                planId: `%s`
                master: `%s:%d`%s

                A appliquer sur le master `%s`:
                ```sql
                CREATE USER IF NOT EXISTS '%s'@'%%' IDENTIFIED BY '%s';
                GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '%s'@'%%';
                FLUSH PRIVILEGES;
                ```

                Ajoutez ou adaptez ce bloc dans le fichier de configuration réellement chargé par MySQL.
                Emplacements fréquents:
                - Linux: `/etc/mysql/my.cnf`, `/etc/mysql/mysql.conf.d/mysqld.cnf`, `/etc/my.cnf`
                - Windows MySQL Installer: `C:\\ProgramData\\MySQL\\MySQL Server 8.0\\my.ini`
                - Windows XAMPP/WAMP: `C:\\xampp\\mysql\\bin\\my.ini`, `C:\\wamp64\\bin\\mysql\\mysql*\\my.ini`

                Pour vérifier les chemins lus par MySQL, utilisez `mysqld --help --verbose` puis cherchez `Default options`.

                Configuration recommandée sur le master:
                ```ini
                [mysqld]
                server-id=%d
                log-bin=mysql-bin
                binlog-format=ROW
                gtid-mode=ON
                enforce-gtid-consistency=ON
                ```

                Configuration recommandée sur le replica:
                ```ini
                [mysqld]
                server-id=%d
                relay-log=relay-bin
                read-only=ON
                gtid-mode=ON
                enforce-gtid-consistency=ON
                ```

                Pour exécuter la configuration côté replica, Jemima doit demander le mot de passe root MySQL du replica, puis appeler l'outil d'exécution avec ce `planId` et l'adresse de la machine replica. La machine replica doit être différente du master.
                """.formatted(
                plan.planId(),
                plan.masterHost(),
                plan.masterPort(),
                dbLine,
                plan.masterHost(),
                escapeSql(plan.replicationUser()),
                escapeSql(plan.replicationPassword()),
                escapeSql(plan.replicationUser()),
                plan.masterServerId(),
                plan.replicaServerId());
    }

    private String escapeSql(String value) {
        return safe(value, "").replace("'", "''");
    }

    private boolean shouldRememberToolResult(String result) {
        if (result == null || result.isBlank()) {
            return false;
        }
        String normalized = result.toLowerCase(Locale.ROOT);
        return !normalized.contains("certains produits n'ont pas encore de prix")
                && !normalized.contains("aucune ligne de facture valide")
                && !normalized.contains("workflow facture introuvable")
                && !normalized.startsWith("échec ")
                && !normalized.startsWith("echec ");
    }

    private String draftKey(InvoiceDraft draft) {
        if (draft == null) {
            return "null-draft";
        }
        StringBuilder key = new StringBuilder();
        key.append(safe(draft.getReference(), "sans-reference"))
                .append("|").append(safe(draft.getSupplier(), "sans-fournisseur"))
                .append("|").append(safe(draft.getInvoiceDate(), "sans-date"));
        if (draft.getLines() != null) {
            for (InvoiceLine line : draft.getLines()) {
                key.append("|")
                        .append(safe(line.getProductName(), "produit"))
                        .append(":").append(line.getQuantity())
                        .append(":").append(line.getPurchaseUnitPrice())
                        .append(":").append(line.getTotal());
            }
        }
        return key.toString();
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("nomProduit", json);
            return fallback;
        }
    }

    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception ex) {
            try {
                return List.of(mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                }));
            } catch (Exception ignored) {
                return List.of();
            }
        }
    }

    private List<Map<String, Object>> parseMeasureInputs(String input) {
        List<Map<String, Object>> jsonMeasures = parseJsonList(input);
        if (!jsonMeasures.isEmpty()) {
            return jsonMeasures;
        }
        if (input == null || input.isBlank()) {
            return List.of();
        }
        List<Map<String, Object>> measures = new ArrayList<>();
        for (String row : input.split("\\R")) {
            if (row.isBlank() || row.toLowerCase(Locale.ROOT).contains("uidproduit")) {
                continue;
            }
            String[] cells = row.split(",");
            if (cells.length < 2) {
                continue;
            }
            Map<String, Object> measure = new LinkedHashMap<>();
            measure.put("uidProduit", cells[0].trim());
            measure.put("descriptionMesure", cells[1].trim());
            measure.put("quantiteContenu", cells.length >= 3 ? cells[2].trim() : "1");
            measures.add(measure);
        }
        return measures;
    }

    private boolean productMatches(Produit product, Map<String, Object> criteria) {
        if (product == null) {
            return false;
        }
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String expected = stringValue(entry.getValue());
            if (expected.isBlank()) {
                continue;
            }
            if (isProductNameField(entry.getKey()) && expected.contains(";")) {
                ProductNameParts parts = productNameParts(expected);
                if (!matchesParsedProductPart(product.getNomProduit(), parts.name())
                        || !matchesParsedProductPart(product.getMarque(), parts.brand())
                        || !matchesParsedProductPart(product.getModele(), parts.model())) {
                    return false;
                }
                continue;
            }
            String actual = productField(product, entry.getKey());
            if (!containsIgnoreCase(actual, expected)) {
                return false;
            }
        }
        return true;
    }

    private String productField(Produit product, String field) {
        String normalized = normalizeToolKey(field).replace("_", "").replace("-", "");
        return switch (normalized) {
            case "uid", "id" ->
                product.getUid();
            case "nomproduit", "nom", "name", "productname" ->
                product.getNomProduit();
            case "codebar", "codebarre", "barcode" ->
                product.getCodebar();
            case "marque", "brand" ->
                product.getMarque();
            case "modele", "model" ->
                product.getModele();
            case "taille", "size" ->
                product.getTaille();
            case "couleur", "color" ->
                product.getCouleur();
            case "methodeinventaire", "inventaire", "inventorymethod" ->
                product.getMethodeInventaire();
            case "category", "categorie", "categoryid" ->
                product.getCategoryId() == null ? "" : product.getCategoryId().getDescritption();
            default ->
                "";
        };
    }

    private boolean containsIgnoreCase(String actual, String expected) {
        return actual != null && actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private List<String> parseProductPrintTargets(String productTargets) {
        if (productTargets == null || productTargets.isBlank()) {
            return List.of();
        }
        List<String> targets = new ArrayList<>();
        for (String token : productTargets.split("[,\\n\\r]+")) {
            String value = token.trim();
            if (!value.isBlank()
                    && !value.equalsIgnoreCase("all")
                    && !value.equalsIgnoreCase("tous")
                    && !value.equalsIgnoreCase("tout")) {
                targets.add(value);
            }
        }
        return targets;
    }

    private int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean matchesPrintTarget(Produit product, List<String> targets) {
        if (product == null || targets == null || targets.isEmpty()) {
            return true;
        }
        String searchable = (safe(product.getNomProduit(), "") + " "
                + safe(product.getMarque(), "") + " "
                + safe(product.getModele(), "") + " "
                + safe(product.getCodebar(), "")).toLowerCase(Locale.ROOT);
        for (String target : targets) {
            if (searchable.contains(target.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesParsedProductPart(String actual, String expected) {
        return "-".equals(expected) || containsIgnoreCase(actual, expected);
    }

    private boolean isProductNameField(String field) {
        String normalized = normalizeToolKey(field).replace("_", "").replace("-", "");
        return normalized.equals("nomproduit")
                || normalized.equals("nom")
                || normalized.equals("name")
                || normalized.equals("productname");
    }

    private Produit findProductForUpdate(Map<String, Object> update) {
        String uid = firstValue(update, "uid", "id", "produitId", "productId");
        if (!uid.isBlank()) {
            Produit product = ProduitDelegate.findProduit(uid);
            if (product != null) {
                return product;
            }
        }
        String codebar = firstValue(update, "codebar", "barcode", "codebarre");
        return codebar.isBlank() ? null : ProduitDelegate.findByCodebar(codebar);
    }

    private void applyProductUpdate(Produit product, Map<String, Object> update) {
        ProductNameParts parsedName = null;
        boolean explicitBrand = hasAnyKey(update, "marque", "brand");
        boolean explicitModel = hasAnyKey(update, "modele", "model");
        for (Map.Entry<String, Object> entry : update.entrySet()) {
            String field = normalizeToolKey(entry.getKey()).replace("_", "").replace("-", "");
            String value = stringValue(entry.getValue());
            if (value.isBlank()) {
                continue;
            }
            switch (field) {
                case "nomproduit", "nom", "name", "productname" -> {
                    parsedName = productNameParts(value);
                    product.setNomProduit(parsedName.name());
                    if (!explicitBrand) {
                        product.setMarque(parsedName.brand());
                    }
                    if (!explicitModel) {
                        product.setModele(parsedName.model());
                    }
                }
                case "codebar", "codebarre", "barcode" ->
                    product.setCodebar(value);
                case "marque", "brand" ->
                    product.setMarque(value);
                case "modele", "model" ->
                    product.setModele(value);
                case "taille", "size" ->
                    product.setTaille(value);
                case "couleur", "color" ->
                    product.setCouleur(value);
                case "methodeinventaire", "inventaire", "inventorymethod" ->
                    product.setMethodeInventaire(value);
                case "category", "categorie", "categoryid" ->
                    product.setCategoryId(findOrCreateCategory(value));
                default -> {
                }
            }
        }
    }

    private Produit ensureProductBarcode(Produit product) {
        if (product == null) {
            return null;
        }
        if (product.getCodebar() != null && !product.getCodebar().isBlank()) {
            return product;
        }
        product.setCodebar(generateUniqueInvoiceBarcode());
        Produit updated = ProduitDelegate.updateProduit(product);
        syncUpdate(updated, Tables.PRODUIT);
        return updated;
    }

    private boolean hasAnyKey(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            for (String existing : map.keySet()) {
                if (normalizeToolKey(existing).replace("_", "").replace("-", "")
                        .equals(normalizeToolKey(key).replace("_", "").replace("-", ""))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String firstValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (normalizeToolKey(entry.getKey()).equals(normalizeToolKey(key))) {
                    return stringValue(entry.getValue());
                }
            }
        }
        return "";
    }

    private String formatProducts(List<Produit> products) {
        if (products == null || products.isEmpty()) {
            return "Aucun produit trouvé.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(products.size()).append(" produit(s) trouvé(s):\n");
        int limit = Math.min(products.size(), 80);
        for (int i = 0; i < limit; i++) {
            builder.append(i + 1).append(". ").append(productLine(products.get(i))).append("\n");
        }
        if (products.size() > limit) {
            builder.append("... ").append(products.size() - limit).append(" autre(s) produit(s) non affiché(s).");
        }
        return builder.toString().trim();
    }

    private String productLine(Produit product) {
        String category = product.getCategoryId() == null ? "-" : safe(product.getCategoryId().getDescritption(), "-");
        return safe(product.getNomProduit(), "Produit")
                + " | uid=" + safe(product.getUid(), "-")
                + " | codebar=" + safe(product.getCodebar(), "-")
                + " | marque=" + safe(product.getMarque(), "-")
                + " | modele=" + safe(product.getModele(), "-")
                + " | taille=" + safe(product.getTaille(), "-")
                + " | couleur=" + safe(product.getCouleur(), "-")
                + " | categorie=" + category;
    }

    private String duplicateProductLine(Produit product) {
        return "uid=" + safe(product.getUid(), "-")
                + " | codebar=" + safe(product.getCodebar(), "-")
                + " | nom=" + safe(product.getNomProduit(), "-")
                + " | marque=" + safe(product.getMarque(), "-")
                + " | modele=" + safe(product.getModele(), "-")
                + " | taille=" + safe(product.getTaille(), "-")
                + " | couleur=" + safe(product.getCouleur(), "-");
    }

    private List<List<Produit>> duplicateProductGroups(List<Produit> products) {
        List<List<Produit>> groups = new ArrayList<>();
        for (Produit product : products) {
            if (product == null) {
                continue;
            }
            List<List<Produit>> matchedGroups = new ArrayList<>();
            for (List<Produit> group : groups) {
                if (matchesAnyDuplicateProduct(product, group)) {
                    matchedGroups.add(group);
                }
            }
            if (matchedGroups.isEmpty()) {
                List<Produit> group = new ArrayList<>();
                group.add(product);
                groups.add(group);
                continue;
            }
            List<Produit> primaryGroup = matchedGroups.get(0);
            primaryGroup.add(product);
            for (int i = 1; i < matchedGroups.size(); i++) {
                List<Produit> secondaryGroup = matchedGroups.get(i);
                primaryGroup.addAll(secondaryGroup);
                groups.remove(secondaryGroup);
            }
        }
        return groups;
    }

    private boolean matchesAnyDuplicateProduct(Produit product, List<Produit> group) {
        for (Produit candidate : group) {
            if (duplicateProductsMatch(product, candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean duplicateProductsMatch(Produit first, Produit second) {
        if (first == null || second == null || Objects.equals(first.getUid(), second.getUid())) {
            return false;
        }
        List<String> firstTokens = searchableProductTokens(productSearchText(first));
        List<String> secondTokens = searchableProductTokens(productSearchText(second));
        if (firstTokens.isEmpty() || secondTokens.isEmpty()) {
            return false;
        }
        // Pour les doublons, la comparaison doit etre bidirectionnelle afin d'éviter
        // qu'un libelle tres court englobe par erreur un produit plus precis mais different.
        return countMatchedInvoiceTokens(firstTokens, secondTokens) >= requiredInvoiceTokenMatches(firstTokens.size())
                && countMatchedInvoiceTokens(secondTokens, firstTokens) >= requiredInvoiceTokenMatches(secondTokens.size());
    }

    private List<Integer> parseSelectedNumbers(String selectedNumbers) {
        if (selectedNumbers == null || selectedNumbers.isBlank()) {
            return List.of();
        }
        List<Integer> numbers = new ArrayList<>();
        for (String token : selectedNumbers.split("[,;\\s]+")) {
            if (token.isBlank()) {
                continue;
            }
            try {
                int value = Integer.parseInt(token.trim());
                if (value > 0 && !numbers.contains(value)) {
                    numbers.add(value);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return numbers;
    }

    private void deleteProductMeasures(Produit product) {
        if (product == null || product.getUid() == null) {
            return;
        }
        List<Mesure> measures = MesureDelegate.findMesureByProduit(product.getUid());
        if (measures == null) {
            return;
        }
        for (Mesure measure : measures) {
            // La mesure n'est supprimée qu'après le transfert des feuilles qui la référencent.
            removeEntityBlocking(measure);
            syncDelete(measure, Tables.MESURE);
        }
    }

    private <T> T mergeEntityBlocking(T entity) {
        if (entity == null) {
            return null;
        }
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.submitWrite(em -> em.merge(entity)).join();
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        T merged = em.merge(entity);
        tx.commit();
        return merged;
    }

    private void removeEntityBlocking(Object entity) {
        if (entity == null) {
            return;
        }
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(entity));
                return entity;
            }).join();
            return;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.remove(em.merge(entity));
        tx.commit();
    }

    private Produit findDuplicateKeeper(DuplicateProductBatch batch, DuplicateProductEntry removed, List<Integer> selectedNumbers) {
        for (DuplicateProductEntry candidate : batch.entries()) {
            if (candidate.groupNumber() != removed.groupNumber() || selectedNumbers.contains(candidate.number())) {
                continue;
            }
            Produit keeper = ProduitDelegate.findProduit(candidate.product().getUid());
            if (keeper != null) {
                return keeper;
            }
        }
        return null;
    }

    private MovementTransferSummary transferProductMovements(Produit fromProduct, Produit toProduct, TransferDeliveryContext transferDelivery) {
        MovementTransferSummary summary = new MovementTransferSummary();
        transferLeafAggregates(fromProduct, toProduct, summary);
        transferRecquisitions(fromProduct, toProduct, transferDelivery, summary);
        transferLigneVentes(fromProduct, toProduct, summary);
        transferStockers(fromProduct, toProduct, summary);
        transferDestockers(fromProduct, toProduct, summary);
        return summary;
    }

    private void transferLeafAggregates(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        // Les agrégats sont des feuilles qui pointent directement vers mesure_id; on les déplace avant de supprimer les mesures.
        transferStockAggregates(fromProduct, toProduct, summary);
        transferSaleAggregates(fromProduct, toProduct, summary);
        transferStockDepotAggregates(fromProduct, toProduct, summary);
    }

    private void transferStockAggregates(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        List<StockAgregate> aggregates = RepportDelegate.getStorage().findStockAgregate();
        if (aggregates == null) {
            return;
        }
        for (StockAgregate aggregate : aggregates) {
            if (!sameUid(aggregate.getProductId(), fromProduct)) {
                continue;
            }
            StockAgregate target = findStockAggregateMergeTarget(aggregates, aggregate, toProduct);
            if (target != null) {
                mergeStockAggregateQuantities(target, aggregate);
                mergeEntityBlocking(target);
                removeEntityBlocking(aggregate);
                summary.stockAgregatesMerged++;
            } else {
                aggregate.setProductId(toProduct);
                aggregate.setMesureId(equivalentMeasureForTransfer(toProduct, aggregate.getMesureId()));
                mergeEntityBlocking(aggregate);
            }
            summary.stockAgregates++;
        }
    }

    private void transferSaleAggregates(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        List<?> aggregates = RepportDelegate.getStorage().findSaleAgregate();
        if (aggregates == null) {
            return;
        }
        for (Object row : aggregates) {
            if (!(row instanceof SaleAgregate aggregate) || !sameUid(aggregate.getProductId(), fromProduct)) {
                continue;
            }
            SaleAgregate target = findSaleAggregateMergeTarget(aggregates, aggregate, toProduct);
            if (target != null) {
                mergeSaleAggregateQuantities(target, aggregate);
                mergeEntityBlocking(target);
                removeEntityBlocking(aggregate);
                summary.saleAgregatesMerged++;
            } else {
                aggregate.setProductId(toProduct);
                aggregate.setCategoryId(toProduct.getCategoryId());
                aggregate.setMesureId(equivalentMeasureForTransfer(toProduct, aggregate.getMesureId()));
                mergeEntityBlocking(aggregate);
            }
            summary.saleAgregates++;
        }
    }

    private void transferStockDepotAggregates(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        List<StockDepotAgregate> aggregates = new StockDepotAgregateService().findByProduitAndRegion(fromProduct.getUid(), null);
        if (aggregates == null) {
            return;
        }
        for (StockDepotAgregate aggregate : aggregates) {
            StockDepotAgregate target = findStockDepotAggregateMergeTarget(aggregate, toProduct);
            if (target != null) {
                mergeStockDepotAggregateQuantities(target, aggregate);
                mergeEntityBlocking(target);
                removeEntityBlocking(aggregate);
                summary.stockDepotAgregatesMerged++;
            } else {
                aggregate.setProductId(toProduct);
                aggregate.setMesureId(equivalentMeasureForTransfer(toProduct, aggregate.getMesureId()));
                mergeEntityBlocking(aggregate);
            }
            summary.stockDepotAgregates++;
        }
    }

    private StockAgregate findStockAggregateMergeTarget(List<StockAgregate> aggregates, StockAgregate source, Produit toProduct) {
        for (StockAgregate candidate : aggregates) {
            if (candidate == null || Objects.equals(candidate.getUid(), source.getUid())) {
                continue;
            }
            if (sameUid(candidate.getProductId(), toProduct)
                    && Objects.equals(candidate.getDate(), source.getDate())
                    && sameText(candidate.getRegion(), source.getRegion())) {
                return candidate;
            }
        }
        return null;
    }

    private SaleAgregate findSaleAggregateMergeTarget(List<?> aggregates, SaleAgregate source, Produit toProduct) {
        for (Object row : aggregates) {
            if (!(row instanceof SaleAgregate candidate) || Objects.equals(candidate.getUid(), source.getUid())) {
                continue;
            }
            if (sameUid(candidate.getProductId(), toProduct)
                    && Objects.equals(localDateOf(candidate.getDate()), localDateOf(source.getDate()))
                    && sameText(candidate.getRegion(), source.getRegion())) {
                return candidate;
            }
        }
        return null;
    }

    private StockDepotAgregate findStockDepotAggregateMergeTarget(StockDepotAgregate source, Produit toProduct) {
        List<StockDepotAgregate> candidates = new StockDepotAgregateService().findByProduitAndRegion(toProduct.getUid(), source.getRegion());
        if (candidates == null) {
            return null;
        }
        for (StockDepotAgregate candidate : candidates) {
            if (candidate == null || Objects.equals(candidate.getUid(), source.getUid())) {
                continue;
            }
            if (Objects.equals(candidate.getDate(), source.getDate())
                    && sameText(candidate.getRegion(), source.getRegion())) {
                return candidate;
            }
        }
        return null;
    }

    private void mergeStockAggregateQuantities(StockAgregate target, StockAgregate source) {
        Mesure targetMeasure = smallestMeasure(target.getMesureId(), source.getMesureId());
        target.setInitialQuantity(sumInMeasure(target.getInitialQuantity(), target.getMesureId(), source.getInitialQuantity(), source.getMesureId(), targetMeasure));
        target.setEntrees(sumInMeasure(target.getEntrees(), target.getMesureId(), source.getEntrees(), source.getMesureId(), targetMeasure));
        target.setSorties(sumInMeasure(target.getSorties(), target.getMesureId(), source.getSorties(), source.getMesureId(), targetMeasure));
        target.setFinalQuantity(sumInMeasure(target.getFinalQuantity(), target.getMesureId(), source.getFinalQuantity(), source.getMesureId(), targetMeasure));
        target.setExpiree(sumInMeasure(target.getExpiree(), target.getMesureId(), source.getExpiree(), source.getMesureId(), targetMeasure));
        target.setCoutAchat(maxNullable(target.getCoutAchat(), source.getCoutAchat()));
        target.setMesureId(equivalentMeasureForTransfer(target.getProductId(), targetMeasure));
    }

    private void mergeSaleAggregateQuantities(SaleAgregate target, SaleAgregate source) {
        Mesure targetMeasure = smallestMeasure(target.getMesureId(), source.getMesureId());
        target.setQuantite(sumInMeasure(target.getQuantite(), target.getMesureId(), source.getQuantite(), source.getMesureId(), targetMeasure));
        target.setCoutAchatTotal(nvl(target.getCoutAchatTotal()) + nvl(source.getCoutAchatTotal()));
        target.setTotalSaleUsd(nvl(target.getTotalSaleUsd()) + nvl(source.getTotalSaleUsd()));
        target.setMesureId(equivalentMeasureForTransfer(target.getProductId(), targetMeasure));
    }

    private void mergeStockDepotAggregateQuantities(StockDepotAgregate target, StockDepotAgregate source) {
        Mesure targetMeasure = smallestMeasure(target.getMesureId(), source.getMesureId());
        double targetBaseQuantity = quantityInBase(target.getQuantite(), target.getMesureId());
        double sourceBaseQuantity = quantityInBase(source.getQuantite(), source.getMesureId());
        double totalBaseQuantity = targetBaseQuantity + sourceBaseQuantity;
        double totalValue = target.getValeurStock() + source.getValeurStock();
        target.setQuantite(quantityFromBase(totalBaseQuantity, targetMeasure));
        target.setCoutAchat(totalBaseQuantity == 0 ? Math.max(target.getCoutAchat(), source.getCoutAchat()) : totalValue / totalBaseQuantity);
        target.setValeurStock(totalValue);
        target.setMesureId(equivalentMeasureForTransfer(target.getProductId(), targetMeasure));
    }

    private void transferRecquisitions(Produit fromProduct, Produit toProduct, TransferDeliveryContext transferDelivery, MovementTransferSummary summary) {
        List<Recquisition> recquisitions = RecquisitionDelegate.findRecquisitionByProduit(fromProduct.getUid());
        if (recquisitions == null) {
            return;
        }
        for (Recquisition recquisition : recquisitions) {
            List<PrixDeVente> prices = salePricesForRecquisition(recquisition);
            Mesure mesure = equivalentMeasureForTransfer(toProduct, recquisition.getMesureId());
            Livraison delivery = transferDelivery.getOrCreate();
            recquisition.setProductId(toProduct);
            recquisition.setMesureId(mesure);
            recquisition.setReference(delivery.getReference());
            recquisition.setObservation("Transfert doublon produit via livraison " + delivery.getReference());
            Recquisition updated = mergeEntityBlocking(recquisition);
            syncUpdate(updated, Tables.RECQUISITION);
            transferAndMergeSalePrices(toProduct, updated, prices, summary);
            summary.recquisitions++;
        }
    }

    private List<PrixDeVente> salePricesForRecquisition(Recquisition recquisition) {
        if (recquisition == null || recquisition.getUid() == null) {
            return List.of();
        }
        List<PrixDeVente> prices = PrixDeVenteDelegate.findPricesForRecq(recquisition.getUid());
        return prices == null ? List.of() : prices;
    }

    private void transferAndMergeSalePrices(Produit toProduct, Recquisition recquisition, List<PrixDeVente> prices, MovementTransferSummary summary) {
        if (prices == null || prices.isEmpty()) {
            return;
        }
        for (PrixDeVente price : prices) {
            price = PrixDeVenteDelegate.findPrixDeVente(price.getUid());
            if (price == null) {
                continue;
            }
            price.setRecquisitionId(recquisition);
            price.setMesureId(equivalentMeasureForTransfer(toProduct, price.getMesureId()));
            PrixDeVente updated = mergeEntityBlocking(price);
            syncUpdate(updated, Tables.PRIXDEVENTE);
            mergeDuplicateSalePrice(toProduct, updated, summary);
        }
    }

    private void mergeDuplicateSalePrice(Produit product, PrixDeVente price, MovementTransferSummary summary) {
        if (product == null || price == null || price.getMesureId() == null) {
            return;
        }
        PrixDeVente best = price;
        List<PrixDeVente> duplicates = new ArrayList<>();
        List<Recquisition> recquisitions = RecquisitionDelegate.findRecquisitionByProduit(product.getUid());
        if (recquisitions == null) {
            return;
        }
        for (Recquisition req : recquisitions) {
            for (PrixDeVente candidate : salePricesForRecquisition(req)) {
                if (candidate == null || Objects.equals(candidate.getUid(), price.getUid())) {
                    continue;
                }
                if (sameSalePriceIntervalAndMeasure(candidate, price)) {
                    duplicates.add(candidate);
                    if (candidate.getPrixUnitaire() > best.getPrixUnitaire()) {
                        best = candidate;
                    }
                }
            }
        }
        for (PrixDeVente duplicate : duplicates) {
            if (Objects.equals(duplicate.getUid(), best.getUid())) {
                continue;
            }
            removeEntityBlocking(duplicate);
            syncDelete(duplicate, Tables.PRIXDEVENTE);
            summary.prixDeVenteSupprimes++;
        }
        if (!Objects.equals(price.getUid(), best.getUid())) {
            removeEntityBlocking(price);
            syncDelete(price, Tables.PRIXDEVENTE);
            summary.prixDeVenteSupprimes++;
        } else {
            summary.prixDeVenteGardes++;
        }
    }

    private boolean sameSalePriceIntervalAndMeasure(PrixDeVente first, PrixDeVente second) {
        return first != null && second != null
                && sameDouble(first.getQmin(), second.getQmin())
                && sameDouble(first.getQmax(), second.getQmax())
                && sameUid(first.getMesureId(), second.getMesureId());
    }

    private void transferLigneVentes(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        List<LigneVente> lines = LigneVenteDelegate.findLigneVentes();
        if (lines == null) {
            return;
        }
        for (LigneVente line : lines) {
            if (!sameUid(line.getProductId(), fromProduct)) {
                continue;
            }
            line.setProductId(toProduct);
            line.setMesureId(closestOrCreatedMeasureForLineVente(toProduct, line.getMesureId()));
            LigneVente updated = mergeEntityBlocking(line);
            syncUpdate(updated, Tables.LIGNEVENTE);
            summary.ligneVentes++;
        }
    }

    private void transferStockers(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        List<Stocker> stockers = StockerDelegate.findStockerByProduit(fromProduct.getUid());
        if (stockers == null) {
            return;
        }
        for (Stocker stocker : stockers) {
            stocker.setProductId(toProduct);
            stocker.setMesureId(equivalentMeasureForTransfer(toProduct, stocker.getMesureId()));
            Stocker updated = mergeEntityBlocking(stocker);
            syncUpdate(updated, Tables.STOCKER);
            summary.stockers++;
        }
    }

    private void transferDestockers(Produit fromProduct, Produit toProduct, MovementTransferSummary summary) {
        List<Destocker> destockers = DestockerDelegate.findByProduit(fromProduct.getUid());
        if (destockers == null) {
            return;
        }
        for (Destocker destocker : destockers) {
            destocker.setProductId(toProduct);
            destocker.setMesureId(equivalentMeasureForTransfer(toProduct, destocker.getMesureId()));
            Destocker updated = mergeEntityBlocking(destocker);
            syncUpdate(updated, Tables.DESTOCKER);
            summary.destockers++;
        }
    }

    private Mesure equivalentMeasureForTransfer(Produit keeper, Mesure source) {
        if (keeper == null || source == null) {
            return source;
        }
        List<Mesure> sameDescription = MesureDelegate.findMesureByProduit(keeper.getUid(), safe(source.getDescription(), ""));
        if (sameDescription != null) {
            for (Mesure candidate : sameDescription) {
                if (sameDouble(candidate.getQuantContenu(), source.getQuantContenu())) {
                    return candidate;
                }
            }
        }
        // La mesure du doublon est transferee seulement si le produit garde n'a pas la meme description avec le meme quantContenu.
        source.setProduitId(keeper);
        Mesure updated = mergeEntityBlocking(source);
        syncUpdate(updated, Tables.MESURE);
        return updated;
    }

    private Mesure closestOrCreatedMeasureForLineVente(Produit keeper, Mesure source) {
        if (keeper == null || source == null) {
            return source;
        }
        List<Mesure> measures = MesureDelegate.findMesureByProduit(keeper.getUid());
        if (measures != null) {
            for (Mesure candidate : measures) {
                if (sameMeasureDescription(candidate, source)
                        && sameDouble(candidate.getQuantContenu(), source.getQuantContenu())) {
                    return candidate;
                }
            }
            for (Mesure candidate : measures) {
                if (sameDouble(candidate.getQuantContenu(), source.getQuantContenu())) {
                    return candidate;
                }
            }
            for (Mesure candidate : measures) {
                if (sameMeasureDescription(candidate, source)) {
                    return candidate;
                }
            }
        }
        Mesure created = new Mesure(DataId.generate());
        created.setProduitId(keeper);
        created.setDescription(safe(source.getDescription(), "Pièce"));
        created.setQuantContenu(source.getQuantContenu() == null || source.getQuantContenu() <= 0 ? 1d : source.getQuantContenu());
        Mesure saved = MesureDelegate.saveMesure(created);
        syncCreate(saved, Tables.MESURE);
        return saved;
    }

    private boolean sameMeasureDescription(Mesure first, Mesure second) {
        return first != null && second != null
                && safe(first.getDescription(), "").trim().equalsIgnoreCase(safe(second.getDescription(), "").trim());
    }

    private void removeDeletedDuplicateEntries(DuplicateProductBatch batch, List<Integer> numbers) {
        batch.entries().removeIf(entry -> numbers.contains(entry.number()));
        if (batch.entries().isEmpty()) {
            DUPLICATE_PRODUCT_BATCHES.remove(batch.batchId());
        }
    }

    private Livraison createGenericTransferDelivery() {
        Entreprise entreprise = currentEntreprise();
        Fournisseur supplier = FournisseurDelegate.findOrCreate(entreprise);
        if (supplier == null) {
            supplier = new Fournisseur(DataId.generate());
            supplier.setNomFourn(safe(entreprise.getNomEntreprise(), "Entreprise connectee"));
            supplier.setAdresse(safe(entreprise.getAdresse(), ""));
            supplier.setIdentification(safe(entreprise.getIdentification(), ""));
            supplier.setPhone(safe(entreprise.getPhones(), "N/A-" + supplier.getUid().substring(0, 8)));
            supplier = FournisseurDelegate.saveFournisseur(supplier);
            syncCreate(supplier, Tables.FOURNISSEUR);
        }
        String reference = "TRANSFERT-DOUBLON-" + Constants.dateTodayRef(LocalDate.now()) + "-" + DataId.generate().substring(0, 6);
        Livraison delivery = new Livraison(DataId.generate());
        delivery.setDateLivr(LocalDate.now());
        delivery.setFournId(supplier);
        delivery.setLibelle("Transfert de mouvements depuis produits doublons");
        delivery.setNumPiece(reference);
        delivery.setObservation("Livraison generique creee par Jemima pour transfert de doublons produits");
        delivery.setReference(reference);
        delivery.setRegion(pref.get("region", "Goma"));
        delivery.setReduction(0d);
        delivery.setTopay(0d);
        delivery.setPayed(0d);
        delivery.setRemained(0d);
        delivery.setToreceive(0d);
        Livraison saved = LivraisonDelegate.saveLivraison(delivery);
        syncCreate(saved, Tables.LIVRAISON);
        return saved;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private double parseDouble(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private record ToolExecutionResult(String result, long createdAtMs) {
    }

    private record ProductCodePrintItem(String productName, String brand, String model, String code, String formatLabel, String imageMarkdown) {
    }

    private record ProductCodePrintBatch(String batchId, List<ProductCodePrintItem> items, LocalDateTime createdAt) {
    }

    private record DuplicateProductEntry(int number, int groupNumber, Produit product) {
    }

    private record DuplicateProductBatch(String batchId, List<DuplicateProductEntry> entries, LocalDateTime createdAt) {

        private DuplicateProductEntry find(int number) {
            for (DuplicateProductEntry entry : entries) {
                if (entry.number() == number) {
                    return entry;
                }
            }
            return null;
        }
    }

    private record WorkflowCancellationRequest(String sessionId, String workflowId, String type, long createdAtMs, long expiresAtMs) {
    }

    private record WorkflowTarget(String workflowId, String type, long createdAtMs) {

        private String typeLabel() {
            if ("invoice".equals(type)) {
                return "facture";
            }
            if ("expense".equals(type)) {
                return "dépense";
            }
            return "vente";
        }
    }

    private record MysqlReplicationPlan(
            String planId,
            String masterHost,
            int masterPort,
            String replicationUser,
            String replicationPassword,
            int masterServerId,
            int replicaServerId) {
    }

    private class TransferDeliveryContext {

        private Livraison delivery;

        private Livraison getOrCreate() {
            if (delivery == null) {
                delivery = createGenericTransferDelivery();
            }
            return delivery;
        }
    }

    private static class MovementTransferSummary {

        private int recquisitions;
        private int ligneVentes;
        private int stockers;
        private int destockers;
        private int stockAgregates;
        private int saleAgregates;
        private int stockDepotAgregates;
        private int stockAgregatesMerged;
        private int saleAgregatesMerged;
        private int stockDepotAgregatesMerged;
        private int prixDeVenteGardes;
        private int prixDeVenteSupprimes;

        private String format() {
            return "recquisitions=" + recquisitions
                    + ", ligne-vente=" + ligneVentes
                    + ", stocker=" + stockers
                    + ", destocker=" + destockers
                    + ", stock-agregate=" + stockAgregates
                    + ", sale-agregate=" + saleAgregates
                    + ", stock-depot-agregate=" + stockDepotAgregates
                    + ", agrégats fusionnés=" + (stockAgregatesMerged + saleAgregatesMerged + stockDepotAgregatesMerged)
                    + ", prix-de-vente gardés=" + prixDeVenteGardes
                    + ", prix-de-vente supprimés=" + prixDeVenteSupprimes;
        }
    }

    @Tool("Insère en base les articles d'une facture validée comme réquisitions/approvisionnements")
    public String insertInvoiceSupply(@P("Brouillon facture validé") InvoiceDraft draft) {
        return executeOnce("insertInvoiceSupply", draftKey(draft), () -> {
            if (draft == null || !draft.hasLines()) {
                return "Aucune ligne de facture valide à insérer.";
            }
            List<String> missing = findMissingSalePrices(draft);
            if (!missing.isEmpty()) {
                draft.setMissingSalePrices(missing);
                return missingPriceTemplate(draft);
            }
            String workflowId = registerInvoiceWorkflow(draft);
            createProductsAndMeasures(workflowId);
            InvoiceWorkflowContext context = workflow(workflowId);
            if (context != null && context.aborted) {
                return context.abortMessage;
            }
            createSupplierAndDelivery(workflowId);
            if (context != null && context.aborted) {
                return context.abortMessage;
            }
            return createRequisitionsAndSalePrices(workflowId);
        });
    }

    public String registerInvoiceWorkflow(InvoiceDraft draft) {
        String workflowId = "invoice-" + DataId.generate();
        INVOICE_WORKFLOWS.put(workflowId, new InvoiceWorkflowContext(draft));
        return workflowId;
    }

    public String workflowState(String workflowId) {
        InvoiceWorkflowContext context = workflow(workflowId);
        if (context == null) {
            return "Workflow facture introuvable: " + workflowId;
        }
        return context.summary();
    }

    @Tool("Cree ou retrouve les categories, produits et mesures d'une facture enregistree dans un workflow")
    public String createProductsAndMeasures(@P("workflowId") String workflowId) {
        return executeOnce("createProductsAndMeasures", workflowId, () -> {
            InvoiceWorkflowContext context = workflow(workflowId);
            if (context == null) {
                return "Workflow facture introuvable: " + workflowId;
            }
            if (context.aborted) {
                return context.abortMessage;
            }
            if (context.productsCreated) {
                return "Produits et mesures deja prepares pour " + workflowId + ".";
            }
            for (InvoiceLine line : context.draft.getLines()) {
                Produit product = findOrCreateProduct(line);
                Mesure unit = findOrCreateUnit(product, line);
                context.products.put(lineKey(line), product);
                context.units.put(lineKey(line), unit);
                context.catalogLines.add(product.getNomProduit() + " / " + unit.getDescription());
            }
            context.productsCreated = true;
            return "Catalogue prepare:\n" + String.join("\n", context.catalogLines);
        });
    }

    @Tool("Cree ou retrouve le fournisseur puis cree la livraison d'une facture enregistree dans un workflow")
    public String createSupplierAndDelivery(@P("workflowId") String workflowId) {
        return executeOnce("createSupplierAndDelivery", workflowId, () -> {
            InvoiceWorkflowContext context = workflow(workflowId);
            if (context == null) {
                return "Workflow facture introuvable: " + workflowId;
            }
            if (context.aborted) {
                return context.abortMessage;
            }
            if (context.deliveryCreated) {
                return "Livraison deja creee: " + context.delivery.getReference();
            }
            context.supplier = findOrCreateSupplier(context.draft);
            if (reuseExistingSupplierDelivery(context)) {
                return "Fournisseur et livraison existants retrouvés: "
                        + context.supplier.getNomFourn()
                        + ", reference " + context.delivery.getReference()
                        + ". Passage à la création des réquisitions et prix de vente.";
            }
            context.delivery = createDelivery(context.draft, context.supplier, context.reference, context.date.toLocalDate());
            context.deliveryCreated = true;
            return "Livraison creee pour " + context.supplier.getNomFourn()
                    + ", reference " + context.delivery.getReference()
                    + ", topay=" + context.delivery.getTopay()
                    + ", payed=" + context.delivery.getPayed()
                    + ", remained=" + context.delivery.getRemained();
        });
    }

    @Tool("Cree les recquisitions et prix de vente d'une facture enregistree dans un workflow")
    public String createRequisitionsAndSalePrices(@P("workflowId") String workflowId) {
        return executeOnce("createRequisitionsAndSalePrices", workflowId, () -> {
            InvoiceWorkflowContext context = workflow(workflowId);
            if (context == null) {
                return "Workflow facture introuvable: " + workflowId;
            }
            if (context.aborted) {
                return context.abortMessage;
            }
            if (context.requisitionsCreated) {
                return "Recquisitions deja creees.\n\n" + String.join("\n", context.inserted);
            }
            if (!context.productsCreated) {
                createProductsAndMeasures(workflowId);
            }
            if (!context.deliveryCreated) {
                createSupplierAndDelivery(workflowId);
                if (context.aborted) {
                    return context.abortMessage;
                }
            }
            String region = pref.get("region", "Goma");
            List<String> missing = findMissingSalePrices(context.draft);
            if (!missing.isEmpty()) {
                context.draft.setMissingSalePrices(missing);
                return missingPriceTemplate(context.draft);
            }
            for (InvoiceLine line : context.draft.getLines()) {
                Produit product = context.products.getOrDefault(lineKey(line), findOrCreateProduct(line));
                Mesure unit = context.units.getOrDefault(lineKey(line), findOrCreateUnit(product, line));
                Recquisition saved = findOrCreateRecquisition(context, line, product, unit, region);
                copyOrCreateSalePrices(product, saved, unit, line, safe(context.draft.getCurrency(), "USD"));
                RecquisitionDelegate.rectifyStock(product, LocalDate.now(), LocalDate.now(), region, saved.getNumlot());
                context.inserted.add(product.getNomProduit() + " x " + saved.getQuantite());
            }
            context.requisitionsCreated = true;
            return "Approvisionnement inséré avec succès.\n\n" + String.join("\n", context.inserted);
        });
    }

    @Tool("Génère et ouvre les états financiers PDF sur une période donnée")
    public String generateFinancialStatementsPdf(
            @P("Date début au format yyyy-MM-dd") String start,
            @P("Date fin au format yyyy-MM-dd") String end,
            @P("Région optionnelle") String region) {
        return executeOnce("generateFinancialStatementsPdf", start + "|" + end + "|" + region, () -> {
            try {
                LocalDate d1 = LocalDate.parse(start);
                LocalDate d2 = LocalDate.parse(end);
                String usedRegion = region == null || region.isBlank() ? pref.get("region", null) : region;
                financialService.rebuildStatements(d1, d2, usedRegion);
                Entreprise entreprise = currentEntreprise();
                List<String> headers = List.of("Période", "N-1", "N-2");
                File last = null;
                last = FinancialStatementPdfExporter.export(entreprise, "Bilan Comptable Financier", d1, d2,
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_BILAN, d1, d2, usedRegion), headers);
                FinancialStatementPdfExporter.export(entreprise, "Compte de Résultat Standard", d1, d2,
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, d1, d2, usedRegion), headers);
                FinancialStatementPdfExporter.export(entreprise, "Tableau de Flux de Trésorerie", d1, d2,
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, d1, d2, usedRegion), headers);
                if (last != null && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(last);
                }
                return "États financiers PDF générés pour la période " + start + " au " + end + ".";
            } catch (Exception ex) {
                return "Échec génération PDF: " + ex.getMessage();
            }
        });
    }

    @Tool("Génère et ouvre les états financiers Excel sur une période donnée")
    public String generateFinancialStatementsExcel(
            @P("Date début au format yyyy-MM-dd") String start,
            @P("Date fin au format yyyy-MM-dd") String end,
            @P("Région optionnelle") String region) {
        return executeOnce("generateFinancialStatementsExcel", start + "|" + end + "|" + region, () -> {
            try {
                LocalDate d1 = LocalDate.parse(start);
                LocalDate d2 = LocalDate.parse(end);
                String usedRegion = region == null || region.isBlank() ? pref.get("region", null) : region;
                financialService.rebuildStatements(d1, d2, usedRegion);
                File file = FileUtils.pointFile("financial-statements-" + System.currentTimeMillis() + ".xlsx");
                try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
                    writeSheet(workbook, "Bilan", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_BILAN, d1, d2, usedRegion));
                    writeSheet(workbook, "Compte Resultat", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, d1, d2, usedRegion));
                    writeSheet(workbook, "Flux Tresorerie", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, d1, d2, usedRegion));
                    workbook.write(out);
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "États financiers Excel générés: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération Excel: " + ex.getMessage();
            }
        });
    }

    public List<String> findMissingSalePrices(InvoiceDraft draft) {
        List<String> missing = new ArrayList<>();
        for (InvoiceLine line : draft.getLines()) {
            Produit product = findExistingProduct(line.getProductName());
            List<PrixDeVente> lastPrices = product == null ? List.of() : RecquisitionDelegate.findLastPrices(product.getUid());
            boolean hasLastPrices = lastPrices != null && !lastPrices.isEmpty();
            if (!hasLastPrices && (line.getSalePrice() == null || line.getSalePrice() <= 0)) {
                missing.add(line.getProductName());
            }
        }
        return missing;
    }

    public String missingPriceTemplate(List<String> productNames) {
        return missingPriceTemplate(productNames, null);
    }

    public List<String> findMissingLotDetails(InvoiceDraft draft) {
        List<String> missing = new ArrayList<>();
        if (draft == null || draft.getLines() == null) {
            return missing;
        }
        // Les deux informations sont necessaires avant d'enregistrer la recquisition.
        for (InvoiceLine line : draft.getLines()) {
            if (line.getLotNumber() == null || line.getLotNumber().isBlank()
                    || line.getExpiryDate() == null || line.getExpiryDate().isBlank()) {
                missing.add(line.getProductName());
            }
        }
        return missing;
    }

    public String missingLotTemplate(InvoiceDraft draft) {
        List<String> missing = findMissingLotDetails(draft);
        StringBuilder builder = new StringBuilder("""
               Certains articles n'ont pas de numero de lot ou de date d'expiration lisible sur la facture.
               Répondez ligne par ligne avec le format:

               numero, numlot, dateExpiry

               Articles concernés:
               """);
        for (int i = 0; i < missing.size(); i++) {
            builder.append(i + 1).append(". ").append(missing.get(i)).append("\n");
        }
        builder.append("""

               Exemple:
               1, """).append(invoiceReference(draft)).append("""
               , yyyy-MM-dd
               2, LOT-002, none

               `dateExpiry` doit être au format `yyyy-MM-dd`. Si le produit n'expire pas, mettez `none`.
               """);
        return builder.toString();
    }

    private Produit findOrCreateProduct(InvoiceLine line) {
        Produit existing = findExistingProduct(line.getProductName());
        if (existing != null) {
            return ensureProductBarcode(existing);
        }
        Category category = findOrCreateCategory(line.getCategory());
        Produit product = new Produit();
        product.setUid(DataId.generate());
        // Les produits crees depuis une facture sans code-barres recoivent un code numerique unique de 13 chiffres.
        product.setCodebar(generateUniqueInvoiceBarcode());
        ProductNameParts parts = productNameParts(line.getProductName());
        product.setNomProduit(parts.name());
        product.setMarque(parts.brand());
        product.setModele(parts.model());
        product.setTaille("");
        product.setCouleur("");
        product.setMethodeInventaire("FIFO");
        product.setDateCreation(LocalDateTime.now());
        product.setCategoryId(category);
        Produit saved = ProduitDelegate.saveProduit(product);
        syncCreate(saved, Tables.PRODUIT);
        return saved;
    }

    private Client findOrCreateSaleClient(SaleDraft draft) {
        String name = draft == null ? null : draft.getClientName();
        String phone = draft == null ? null : draft.getClientPhone();
        if (phone != null && !phone.isBlank()) {
            List<Client> found = ClientDelegate.findClientByPhone(phone.trim());
            if (found != null && !found.isEmpty()) {
                return found.get(0);
            }
        }
        if (name != null && !name.isBlank() && !name.equalsIgnoreCase("anonyme")) {
            List<Client> clients = ClientDelegate.findClients();
            if (clients != null) {
                for (Client client : clients) {
                    if (client.getNomClient() != null
                            && client.getNomClient().trim().equalsIgnoreCase(name.trim())) {
                        return client;
                    }
                }
            }
        }
        if (name == null || name.isBlank() || name.equalsIgnoreCase("anonyme")) {
            Client anonymous = ClientDelegate.findAnonymousClient();
            if (anonymous != null) {
                return anonymous;
            }
            name = "Anonyme";
            phone = "N/A-" + DataId.generate().substring(0, 8);
        }
        Client client = new Client(DataId.generate());
        client.setNomClient(name.trim());
        client.setPhone(safe(phone, "N/A-" + client.getUid().substring(0, 8)));
        client.setTypeClient("Client");
        Client saved = ClientDelegate.saveClient(client);
        syncCreate(saved, Tables.CLIENT);
        return saved;
    }

    private CompteTresor findOrCreateSaleAccount(String region) {
        String numero = "JEMIMA-CAISSE-" + safe(region, "Goma").toUpperCase(Locale.ROOT);
        List<CompteTresor> found = CompteTresorDelegate.findByNumeroCompte(numero);
        if (found != null && !found.isEmpty()) {
            return found.get(0);
        }
        CompteTresor account = new CompteTresor(DataId.generate());
        account.setIntitule("Caisse Jemima");
        account.setBankName("Caisse");
        account.setNumeroCompte(numero);
        account.setTypeCompte(TypeTraisorerie.CAISSE.name());
        account.setRegion(safe(region, "Goma"));
        account.setSoldeMinimum(0d);
        CompteTresor saved = CompteTresorDelegate.saveCompteTresor(account);
        syncCreate(saved, Tables.COMPTETRESOR);
        return saved;
    }

    private Depense findOrCreateExpense(ExpenseDraft draft, String region) {
        String requestedName = safe(draft == null ? null : draft.getExpenseName(), "");
        String sourceText = requestedName + " " + safe(draft == null ? null : draft.getDescription(), "");
        String name = safe(requestedName, inferExpenseCategoryFromKnowledge(sourceText));
        List<Depense> expenses = DepenseDelegate.findDepenses(region);
        if (expenses == null || expenses.isEmpty()) {
            expenses = DepenseDelegate.findDepenses();
        }
        if (expenses != null) {
            for (Depense expense : expenses) {
                if (expense != null && expense.getNomDepense() != null
                        && expense.getNomDepense().trim().equalsIgnoreCase(name.trim())) {
                    return expense;
                }
            }
            Depense matched = findExistingExpenseByWords(name, sourceText, expenses);
            if (matched != null) {
                return matched;
            }
        }
        Depense expense = new Depense(DataId.generate());
        expense.setNomDepense(name);
        expense.setRegion(region);
        expense.setDevise(normalizeCurrency(draft == null ? null : draft.getCurrency()));
        expense.setMontant(draft == null || draft.getAmount() == null ? 0d : draft.getAmount());
        expense.setFrequence("PONCTUELLE");
        Depense saved = DepenseDelegate.saveDepense(expense);
        syncCreate(saved, Tables.DEPENSE);
        return saved;
    }

    private Depense findExistingExpenseByWords(String name, String sourceText, List<Depense> expenses) {
        List<String> requestedTokens = searchableProductTokens(name + " " + safe(sourceText, ""));
        if (requestedTokens.isEmpty() || expenses == null) {
            return null;
        }
        Depense best = null;
        int bestScore = 0;
        int required = requiredInvoiceTokenMatches(requestedTokens.size());
        for (Depense expense : expenses) {
            if (expense == null || expense.getNomDepense() == null || expense.getNomDepense().isBlank()) {
                continue;
            }
            List<String> existingTokens = searchableProductTokens(expense.getNomDepense());
            int score = countMatchedInvoiceTokens(requestedTokens, existingTokens);
            if (score >= required && score > bestScore) {
                bestScore = score;
                best = expense;
            }
        }
        return best;
    }

    private String inferExpenseCategoryFromKnowledge(String text) {
        String value = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (value.contains("fuel") || value.contains("carburant") || value.contains("essence") || value.contains("gasoil")) {
            return "Carburant";
        }
        if (value.contains("transport") || value.contains("taxi") || value.contains("bus") || value.contains("parking")) {
            return "Transport";
        }
        if (value.contains("internet") || value.contains("data") || value.contains("wifi") || value.contains("telecom")
                || value.contains("communication")) {
            return "Internet et communication";
        }
        if (value.contains("loyer") || value.contains("rent")) {
            return "Loyer";
        }
        if (value.contains("electric") || value.contains("courant") || value.contains("eau")) {
            return "Eau et électricité";
        }
        if (value.contains("papier") || value.contains("stylo") || value.contains("fourniture")) {
            return "Fournitures de bureau";
        }
        if (value.contains("repas") || value.contains("restaurant") || value.contains("food")) {
            return "Restauration";
        }
        return "Dépense générale";
    }

    private CompteTresor findOrCreateExpenseAccount(ExpenseDraft draft, String region) {
        String type = normalizeTreasuryType(draft == null ? null : draft.getAccountType());
        String accountName = safe(draft == null ? null : draft.getAccountName(), type.equals(TypeTraisorerie.CAISSE.name()) ? "Caisse Jemima" : type);
        List<CompteTresor> accounts = CompteTresorDelegate.findCompteTresors(region);
        if (accounts == null || accounts.isEmpty()) {
            accounts = CompteTresorDelegate.findCompteTresors();
        }
        if (accounts != null) {
            for (CompteTresor account : accounts) {
                if (account == null) {
                    continue;
                }
                boolean sameType = type.equalsIgnoreCase(safe(account.getTypeCompte(), ""));
                String searchable = (safe(account.getIntitule(), "") + " "
                        + safe(account.getBankName(), "") + " "
                        + safe(account.getNumeroCompte(), "")).toLowerCase(Locale.ROOT);
                if (sameType && searchable.contains(accountName.toLowerCase(Locale.ROOT))) {
                    return account;
                }
            }
            for (CompteTresor account : accounts) {
                if (account != null && type.equalsIgnoreCase(safe(account.getTypeCompte(), ""))) {
                    return account;
                }
            }
        }
        String numero = "JEMIMA-" + type + "-" + safe(region, "Goma").toUpperCase(Locale.ROOT);
        List<CompteTresor> found = CompteTresorDelegate.findByNumeroCompte(numero);
        if (found != null && !found.isEmpty()) {
            return found.get(0);
        }
        CompteTresor account = new CompteTresor(DataId.generate());
        account.setIntitule(accountName);
        account.setBankName(type.equals(TypeTraisorerie.CAISSE.name()) ? "Caisse" : accountName);
        account.setNumeroCompte(numero);
        account.setTypeCompte(type);
        account.setRegion(region);
        account.setSoldeMinimum(0d);
        CompteTresor saved = CompteTresorDelegate.saveCompteTresor(account);
        syncCreate(saved, Tables.COMPTETRESOR);
        return saved;
    }

    private Traisorerie findExistingExpenseTreasury(ExpenseWorkflowContext context) {
        if (context == null || context.account == null) {
            return null;
        }
        return TraisorerieDelegate.findExistingOf(context.reference, context.date.toLocalDate(), context.account.getUid(), context.region);
    }

    private Operation findExistingExpenseOperation(ExpenseWorkflowContext context, Traisorerie treasury) {
        if (context == null || treasury == null) {
            return null;
        }
        List<Operation> operations = OperationDelegate.findOperations(context.region);
        if (operations == null) {
            return null;
        }
        for (Operation operation : operations) {
            if (operation != null
                    && sameText(operation.getReferenceOp(), treasury.getReference())
                    && sameEntityUid(operation.getDepenseId() == null ? null : operation.getDepenseId().getUid(),
                            context.expense == null ? null : context.expense.getUid())
                    && sameEntityUid(operation.getTresorId() == null ? null : operation.getTresorId().getUid(),
                            context.account == null ? null : context.account.getUid())
                    && Objects.equals(localDateOf(operation.getDate()), localDateOf(treasury.getDate()))) {
                return operation;
            }
        }
        return null;
    }

    private String normalizeCurrency(String currency) {
        return isCdf(currency) ? "CDF" : "USD";
    }

    private boolean isCdf(String currency) {
        return currency != null && currency.trim().equalsIgnoreCase("CDF");
    }

    private String normalizeTreasuryType(String type) {
        String value = safe(type, TypeTraisorerie.CAISSE.name()).toUpperCase(Locale.ROOT);
        if (value.contains("BANQUE") || value.contains("BANK")) {
            return TypeTraisorerie.BANQUE.name();
        }
        if (value.contains("ELECT") || value.contains("MOBILE") || value.contains("MONEY")) {
            return TypeTraisorerie.ELECTRONIQUE.name();
        }
        return TypeTraisorerie.CAISSE.name();
    }

    private Produit findSaleProduct(SaleLine line) {
        if (line == null) {
            return null;
        }
        if (line.getProductId() != null && !line.getProductId().isBlank()) {
            Produit product = ProduitDelegate.findProduit(line.getProductId());
            if (product != null) {
                return product;
            }
        }
        return findExistingProduct(line.getProductName());
    }

    private Mesure findSaleMeasure(Produit product, SaleLine line) {
        if (product == null) {
            return null;
        }
        if (line != null && line.getMeasureName() != null && !line.getMeasureName().isBlank()) {
            List<Mesure> found = MesureDelegate.findMesureByProduit(product.getUid(), line.getMeasureName().trim());
            if (found != null && !found.isEmpty()) {
                return found.get(0);
            }
        }
        Mesure unit = MesureDelegate.findByProduitAndQuant(product.getUid(), 1d);
        if (unit != null) {
            return unit;
        }
        Mesure measure = new Mesure(DataId.generate());
        measure.setProduitId(product);
        measure.setDescription("Pièce");
        measure.setQuantContenu(1d);
        Mesure saved = MesureDelegate.saveMesure(measure);
        syncCreate(saved, Tables.MESURE);
        return saved;
    }

    private double saleUnitPrice(Produit product, SaleLine line) {
        if (line != null && line.getSalePrice() != null && line.getSalePrice() > 0) {
            return line.getSalePrice();
        }
        if (product == null) {
            return 0d;
        }
        double quantity = line == null || line.getQuantity() <= 0 ? 1d : line.getQuantity();
        List<PrixDeVente> prices = RecquisitionDelegate.findLastPrices(product.getUid());
        if (prices == null || prices.isEmpty()) {
            return 0d;
        }
        for (PrixDeVente price : prices) {
            if (quantity >= price.getQmin() && quantity <= price.getQmax()) {
                return price.getPrixUnitaire();
            }
        }
        return prices.get(0).getPrixUnitaire();
    }

    private Vente findExistingSale(String reference, LocalDateTime date, String region) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        List<Vente> sales = date == null
                ? VenteDelegate.findByRef(reference.trim())
                : VenteDelegate.findByRef(reference.trim(), date.toLocalDate());
        if ((sales == null || sales.isEmpty())) {
            sales = VenteDelegate.findByRef(reference.trim());
        }
        if (sales == null) {
            return null;
        }
        for (Vente sale : sales) {
            if (region == null || region.isBlank()
                    || sale.getRegion() == null
                    || sale.getRegion().equalsIgnoreCase(region)) {
                return sale;
            }
        }
        return null;
    }

    private LigneVente findExistingSaleLine(Vente sale, Produit product, Mesure measure, SaleLine draftLine) {
        if (sale == null || sale.getUid() == null || product == null || product.getUid() == null) {
            return null;
        }
        List<LigneVente> lines = LigneVenteDelegate.findByReference(sale.getUid());
        if (lines == null) {
            return null;
        }
        for (LigneVente line : lines) {
            if (sameUid(line.getProductId(), product)
                    && sameUid(line.getMesureId(), measure)) {
                return line;
            }
        }
        return null;
    }

    private String saleReference() {
        int counter = pref.getInt("_bill_counter_", 0) + 1;
        pref.putInt("_bill_counter_", counter);
        return "AI-" + Constants.dateTodayRef(LocalDate.now()) + "-" + counter;
    }

    private String expenseReference() {
        int counter = pref.getInt("_expense_counter_", 0) + 1;
        pref.putInt("_expense_counter_", counter);
        return "DEP-AI-" + Constants.dateTodayRef(LocalDate.now()) + "-" + counter;
    }

    private String syncSaleByHttps(SaleWorkflowContext context) {
        try {
            if (!Util.isInternetAndBaseApiReachable()) {
                return "connexion indisponible";
            }
            Kazisafe kazisafe = KazisafeServiceFactory.createService(pref.get("token", null));
            VenteHelper helper = new VenteHelper();
            helper.setVente(context.sale);
            helper.setClient(context.client);
            helper.setTresor(context.account);
            helper.setTransactionId(context.treasury == null ? null : context.treasury.getUid());
            helper.setLigneVentes(context.lines);
            Response<Vente> response = kazisafe.syncSale(helper).execute();
            return response == null ? "aucune réponse serveur" : "code " + response.code();
        } catch (Exception ex) {
            return "échec: " + ex.getMessage();
        }
    }

    private String syncExpenseByHttps(Traisorerie treasury, Operation operation) {
        try {
            if (!Util.isInternetAndBaseApiReachable()) {
                return "connexion indisponible";
            }
            Kazisafe kazisafe = KazisafeServiceFactory.createService(pref.get("token", null));
            Response<Traisorerie> cashResponse = kazisafe.saveCash(treasury).execute();
            Response<Operation> operationResponse = kazisafe.saveOperation(operation).execute();
            int cashCode = cashResponse == null ? 0 : cashResponse.code();
            int operationCode = operationResponse == null ? 0 : operationResponse.code();
            return "trésorerie code " + cashCode + ", opération code " + operationCode;
        } catch (Exception ex) {
            return "échec: " + ex.getMessage();
        }
    }

    private String normalizeCodeFormat(String format) {
        String normalized = normalizeToolKey(format);
        if (normalized.equals("qr") || normalized.equals("qrcode") || normalized.equals("qr-code")) {
            return "qrcode";
        }
        if (normalized.equals("bar") || normalized.equals("barcode") || normalized.equals("codebar")
                || normalized.equals("codebarre") || normalized.equals("code-barres")) {
            return "barcode";
        }
        return "auto";
    }

    private String codePrintFormat(String code, String requestedFormat) {
        if ("qrcode".equals(requestedFormat)) {
            return "QR_CODE";
        }
        if ("barcode".equals(requestedFormat)) {
            return "CODE_BARRE";
        }
        return isEanLikeCode(code) ? "CODE_BARRE" : "QR_CODE";
    }

    private boolean isEanLikeCode(String code) {
        return code != null && code.matches("\\d{12,13}");
    }

    private String codeImageMarkdown(String code, String formatLabel) {
        try {
            BitMatrix matrix;
            if ("QR_CODE".equals(formatLabel)) {
                matrix = new MultiFormatWriter().encode(code, BarcodeFormat.QR_CODE, 180, 180);
            } else {
                BarcodeFormat barcodeFormat = isEanLikeCode(code) ? BarcodeFormat.EAN_13 : BarcodeFormat.CODE_128;
                matrix = new MultiFormatWriter().encode(code, barcodeFormat, 260, 90);
            }
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", out);
                String alt = "QR_CODE".equals(formatLabel) ? "qr-code" : "code-bar";
                return "![" + alt + "](data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray()) + ")";
            }
        } catch (Exception ex) {
            return "`" + safe(code, "-") + "`";
        }
    }

    private String tableCell(String value) {
        return safe(value, "-").replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
    }

    private String productCodeLabel(ProductCodePrintItem item) {
        List<String> parts = new ArrayList<>();
        if (item.productName() != null && !item.productName().isBlank() && !"-".equals(item.productName())) {
            parts.add(item.productName().trim());
        }
        if (item.brand() != null && !item.brand().isBlank() && !"-".equals(item.brand())) {
            parts.add(item.brand().trim());
        }
        if (item.model() != null && !item.model().isBlank() && !"-".equals(item.model())) {
            parts.add(item.model().trim());
        }
        return parts.isEmpty() ? "Produit" : String.join(" ", parts);
    }

    private PrintService findPrintService(String printerName) {
        if (printerName == null || printerName.isBlank()) {
            return PrintServiceLookup.lookupDefaultPrintService();
        }
        PrintService exact = PrinterOutputStream.getPrintServiceByName(printerName.trim());
        if (exact != null) {
            return exact;
        }
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services != null) {
            for (PrintService service : services) {
                if (service.getName().equalsIgnoreCase(printerName.trim())) {
                    return service;
                }
            }
        }
        return null;
    }

    private void printProductCodes(PrintService printService, List<ProductCodePrintItem> items) throws IOException {
        try (PrinterOutputStream output = new PrinterOutputStream(printService);
                EscPos printer = new EscPos(output)) {
            printer.setCharacterCodeTable(EscPos.CharacterCodeTable.CP863_Canadian_French);
            Style smallCentered = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setFontSize(Style.FontSize._1, Style.FontSize._1);
            for (ProductCodePrintItem item : items) {
                // Un interligne separe chaque paire: image code-barres/QR puis libelle produit en petit.
                printer.feed(1);
                if ("QR_CODE".equals(item.formatLabel())) {
                    QRCode qrcode = new QRCode();
                    qrcode.setSize(5);
                    qrcode.setJustification(EscPosConst.Justification.Center);
                    printer.write((BarCodeWrapperInterface) qrcode, item.code());
                } else {
                    BarCode barcode = new BarCode();
                    barcode.setBarCodeSize(12, 6).setJustification(EscPosConst.Justification.Center);
                    printer.write((BarCodeWrapperInterface) barcode, item.code());
                }
                printer.writeLF(smallCentered, productCodeLabel(item));
                printer.feed(1);
            }
            printer.feed(2);
            printer.cut(EscPos.CutMode.FULL);
        }
    }

    private String generateUniqueInvoiceBarcode() {
        String code;
        do {
            long value = ThreadLocalRandom.current().nextLong(1_000_000_000_000L, 10_000_000_000_000L);
            code = Long.toString(value);
        } while (ProduitDelegate.findByCodebar(code) != null);
        return code;
    }

    private ProductNameParts productNameParts(String rawName) {
        String[] tokens = safe(rawName, "Produit facture").split(";", -1);
        String name = tokenOrDash(tokens, 0);
        String brand = tokenOrDash(tokens, 1);
        String model = tokenOrDash(tokens, 2);
        return new ProductNameParts(name, brand, model);
    }

    private String tokenOrDash(String[] tokens, int index) {
        if (tokens == null || index >= tokens.length || tokens[index] == null || tokens[index].trim().isBlank()) {
            return "-";
        }
        return tokens[index].trim();
    }

    private String inferProductCategory(String text) {
        String value = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (value.contains("ordinateur") || value.contains("laptop") || value.contains("thinkpad")
                || value.contains("projecteur") || value.contains("imprimante") || value.contains("phone")
                || value.contains("telephone") || value.contains("smartphone") || value.contains("tablet")) {
            return "Electronique";
        }
        if (value.contains("riz") || value.contains("farine") || value.contains("sucre") || value.contains("huile")
                || value.contains("boisson") || value.contains("eau") || value.contains("lait")) {
            return "Alimentation";
        }
        if (value.contains("savon") || value.contains("shampoo") || value.contains("cosmetique")
                || value.contains("parfum") || value.contains("gel")) {
            return "Hygiène et cosmétique";
        }
        if (value.contains("medicament") || value.contains("pharma") || value.contains("sirop")
                || value.contains("comprime")) {
            return "Pharmacie";
        }
        if (value.contains("chemise") || value.contains("pantalon") || value.contains("robe")
                || value.contains("chaussure") || value.contains("t-shirt")) {
            return "Habillement";
        }
        if (value.contains("ciment") || value.contains("clou") || value.contains("peinture")
                || value.contains("tuyau") || value.contains("fer")) {
            return "Quincaillerie";
        }
        return "Divers";
    }

    private record ProductNameParts(String name, String brand, String model) {
    }

    private Produit findExistingProduct(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        List<Produit> matches = ProduitDelegate.findProduitByName(name.trim());
        if (matches != null && !matches.isEmpty()) {
            return matches.get(0);
        }
        ProductNameParts parts = productNameParts(name);
        if (!parts.name().equals(name.trim())) {
            matches = ProduitDelegate.findProduitByName(parts.name());
            if (matches != null && !matches.isEmpty()) {
                return matches.get(0);
            }
        }
        Produit fuzzyMatch = findExistingProductByInvoiceWords(name);
        if (fuzzyMatch != null) {
            return fuzzyMatch;
        }
        return null;
    }

    private Produit findExistingProductByInvoiceWords(String invoiceItemName) {
        List<String> invoiceTokens = searchableProductTokens(invoiceItemName);
        if (invoiceTokens.isEmpty()) {
            return null;
        }
        List<Produit> products = ProduitDelegate.findProduits();
        if (products == null || products.isEmpty()) {
            return null;
        }
        Produit bestProduct = null;
        int bestMatchedTokens = 0;
        int requiredMatches = requiredInvoiceTokenMatches(invoiceTokens.size());

        // Les libelles de facture peuvent melanger nom, marque, modele, taille et couleur.
        // On compare donc les mots importants de l'item avec ces champs concaténés avant de créer un nouveau produit.
        for (Produit product : products) {
            List<String> productTokens = searchableProductTokens(productSearchText(product));
            if (productTokens.isEmpty()) {
                continue;
            }
            int matchedTokens = countMatchedInvoiceTokens(invoiceTokens, productTokens);
            if (matchedTokens >= requiredMatches && matchedTokens > bestMatchedTokens) {
                bestMatchedTokens = matchedTokens;
                bestProduct = product;
            }
        }
        return bestProduct;
    }

    private int requiredInvoiceTokenMatches(int tokenCount) {
        if (tokenCount <= 2) {
            return tokenCount;
        }
        return (int) Math.ceil(tokenCount * 0.75d);
    }

    private int countMatchedInvoiceTokens(List<String> invoiceTokens, List<String> productTokens) {
        int matched = 0;
        for (String invoiceToken : invoiceTokens) {
            if (productTokenMatches(invoiceToken, productTokens)) {
                matched++;
            }
        }
        return matched;
    }

    private boolean productTokenMatches(String invoiceToken, List<String> productTokens) {
        for (String productToken : productTokens) {
            if (productToken.equals(invoiceToken)) {
                return true;
            }
            if (invoiceToken.length() >= 4 && (productToken.contains(invoiceToken) || invoiceToken.contains(productToken))) {
                return true;
            }
        }
        return false;
    }

    private String productSearchText(Produit product) {
        if (product == null) {
            return "";
        }
        return safe(product.getNomProduit(), "") + " "
                + safe(product.getMarque(), "") + " "
                + safe(product.getModele(), "") + " "
                + safe(product.getTaille(), "") + " "
                + safe(product.getCouleur(), "");
    }

    private List<String> searchableProductTokens(String value) {
        String normalized = normalizeSearchText(value);
        if (normalized.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (token.length() < 2 || !seen.add(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private String normalizeSearchText(String value) {
        String raw = safe(value, "").toLowerCase(Locale.ROOT);
        String withoutAccents = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.replaceAll("[^a-z0-9]+", " ").trim();
    }

    private Category findOrCreateCategory(String suggestion) {
        String name = suggestion == null || suggestion.isBlank() ? "Divers" : suggestion.trim();
        List<Category> found = CategoryDelegate.findCategories(name);
        if (found != null && !found.isEmpty()) {
            return found.get(0);
        }
        Category category = new Category(DataId.generate());
        category.setDescritption(name);
        Category saved = CategoryDelegate.saveCategory(category);
        syncCreate(saved, Tables.CATEGORY);
        return saved;
    }

    private Fournisseur findOrCreateSupplier(InvoiceDraft draft) {
        String supplierName = safe(draft.getSupplier(), "Fournisseur facture Jemima");
        Fournisseur existing = findExistingSupplierByName(supplierName);
        if (existing != null) {
            return enrichExistingSupplier(existing, draft);
        }
        Fournisseur supplier = new Fournisseur(DataId.generate());
        supplier.setNomFourn(supplierName);
        supplier.setAdresse(safe(draft.getSupplierAddress(), ""));
        supplier.setIdentification(supplierIdentification(draft));
        supplier.setPhone(safe(draft.getSupplierPhone(), "N/A-" + supplier.getUid().substring(0, 8)));
        Fournisseur saved = FournisseurDelegate.saveFournisseur(supplier);
        syncCreate(saved, Tables.FOURNISSEUR);
        return saved;
    }

    private Fournisseur findExistingSupplierByName(String supplierName) {
        if (supplierName == null || supplierName.isBlank()) {
            return null;
        }
        List<Fournisseur> suppliers = FournisseurDelegate.findFournisseurs();
        if (suppliers == null) {
            return null;
        }
        for (Fournisseur supplier : suppliers) {
            if (supplier.getNomFourn() != null
                    && supplier.getNomFourn().trim().equalsIgnoreCase(supplierName.trim())) {
                return supplier;
            }
        }
        return null;
    }

    private Fournisseur enrichExistingSupplier(Fournisseur supplier, InvoiceDraft draft) {
        boolean changed = false;
        if ((supplier.getAdresse() == null || supplier.getAdresse().isBlank())
                && draft.getSupplierAddress() != null && !draft.getSupplierAddress().isBlank()) {
            supplier.setAdresse(draft.getSupplierAddress());
            changed = true;
        }
        if ((supplier.getPhone() == null || supplier.getPhone().isBlank())
                && draft.getSupplierPhone() != null && !draft.getSupplierPhone().isBlank()) {
            supplier.setPhone(draft.getSupplierPhone());
            changed = true;
        }
        String identification = supplierIdentification(draft);
        if ((supplier.getIdentification() == null || supplier.getIdentification().isBlank())
                && !identification.isBlank()) {
            supplier.setIdentification(identification);
            changed = true;
        }
        if (changed) {
            Fournisseur updated = FournisseurDelegate.updateFournisseur(supplier);
            syncUpdate(updated, Tables.FOURNISSEUR);
            return updated;
        }
        return supplier;
    }

    private String supplierIdentification(InvoiceDraft draft) {
        List<String> ids = new ArrayList<>();
        if (draft.getSupplierIdNat() != null && !draft.getSupplierIdNat().isBlank()) {
            ids.add("ID-NAT: " + draft.getSupplierIdNat().trim());
        }
        if (draft.getSupplierRccm() != null && !draft.getSupplierRccm().isBlank()) {
            ids.add("RCCM: " + draft.getSupplierRccm().trim());
        }
        if (draft.getSupplierTaxNumber() != null && !draft.getSupplierTaxNumber().isBlank()) {
            ids.add("NUM-IMPOT: " + draft.getSupplierTaxNumber().trim());
        }
        return String.join(" | ", ids);
    }

    private Livraison createDelivery(InvoiceDraft draft, Fournisseur supplier, String reference, LocalDate date) {
        String region = pref.get("region", "Goma");
        double topay = invoiceTotal(draft);
        double payed = positive(draft.getPayed());
        double reduction = positive(draft.getReduction());
        double remained = Math.max(0d, topay - payed);
        Livraison delivery = new Livraison(DataId.generate());
        delivery.setDateLivr(date == null ? LocalDate.now() : date);
        delivery.setFournId(supplier);
        delivery.setLibelle("Facture fournisseur via Jemima");
        delivery.setNumPiece(reference);
        delivery.setObservation("Facture lue par Jemima");
        delivery.setReference(reference);
        delivery.setRegion(region);
        delivery.setReduction(reduction);
        delivery.setTopay(topay);
        delivery.setPayed(payed);
        delivery.setRemained(remained);
        delivery.setToreceive(0d);
        Livraison saved = LivraisonDelegate.saveLivraison(delivery);
        syncCreate(saved, Tables.LIVRAISON);
        return saved;
    }

    private boolean reuseExistingSupplierDelivery(InvoiceWorkflowContext context) {
        if (context == null || context.aborted) {
            return false;
        }
        Fournisseur supplier = context.supplier;
        if (supplier == null) {
            supplier = findExistingSupplierByName(safe(context.draft.getSupplier(), "Fournisseur facture Jemima"));
        }
        if (supplier == null) {
            return false;
        }
        Livraison duplicate = findExistingDelivery(context.reference, supplier, context.date.toLocalDate());
        if (duplicate == null) {
            return false;
        }
        context.supplier = supplier;
        context.delivery = duplicate;
        context.deliveryCreated = true;
        return true;
    }

    private Livraison findExistingDelivery(String reference, Fournisseur supplier, LocalDate invoiceDate) {
        if (reference == null || reference.isBlank() || supplier == null || supplier.getUid() == null) {
            return null;
        }
        List<Livraison> deliveries = LivraisonDelegate.findByRef(reference.trim());
        if (deliveries == null) {
            return null;
        }
        for (Livraison delivery : deliveries) {
            if (sameSupplier(delivery.getFournId(), supplier)
                    && Objects.equals(delivery.getDateLivr(), invoiceDate)) {
                return delivery;
            }
        }
        return null;
    }

    private boolean sameSupplier(Fournisseur first, Fournisseur second) {
        return first != null && second != null && Objects.equals(first.getUid(), second.getUid());
    }

    private Recquisition findOrCreateRecquisition(InvoiceWorkflowContext context, InvoiceLine line, Produit product, Mesure unit, String region) {
        String lot = safe(line.getLotNumber(), context.reference);
        LocalDate expiry = parseExpiryDate(line.getExpiryDate());
        Recquisition existing = findExistingRecquisition(product, lot, expiry, region);
        boolean isNew = existing == null;
        Recquisition recquisition = isNew ? new Recquisition(DataId.generate()) : existing;
        recquisition.setProductId(product);
        recquisition.setMesureId(unit);
        recquisition.setRegion(region);
        recquisition.setReference(context.delivery.getReference());
        recquisition.setObservation("Achat de Facture N : " + context.delivery.getNumPiece());
        // Le couple numero de lot + date d'expiration sert de cle metier pour eviter les doublons.
        recquisition.setNumlot(lot);
        recquisition.setDate(context.date);
        recquisition.setDateExpiry(expiry);
        recquisition.setQuantite(line.getQuantity() <= 0 ? 1d : line.getQuantity());
        // Recquisition.coutAchat est un cout unitaire, jamais le total de la ligne.
        recquisition.setCoutAchat(purchaseUnitCost(line));
        recquisition.setStockAlert(1d);
        Recquisition saved = isNew ? RecquisitionDelegate.saveRecquisition(recquisition) : RecquisitionDelegate.updateRecquisition(recquisition);
        if (isNew) {
            syncCreate(saved, Tables.RECQUISITION);
        } else {
            syncUpdate(saved, Tables.RECQUISITION);
        }
        return saved;
    }

    private Recquisition findExistingRecquisition(Produit product, String lot, LocalDate expiry, String region) {
        if (product == null || product.getUid() == null || lot == null || lot.isBlank()) {
            return null;
        }
        List<Recquisition> recquisitions = RecquisitionDelegate.findRecquisitionByProduit(product.getUid(), lot.trim(), region);
        if (recquisitions == null || recquisitions.isEmpty()) {
            recquisitions = RecquisitionDelegate.findRecquisitionByProduit(product.getUid(), lot.trim());
        }
        if (recquisitions == null) {
            return null;
        }
        for (Recquisition recquisition : recquisitions) {
            if (Objects.equals(recquisition.getDateExpiry(), expiry)) {
                return recquisition;
            }
        }
        return null;
    }

    private double invoiceTotal(InvoiceDraft draft) {
        return draft.getLines().stream()
                .mapToDouble(line -> line.getTotal() > 0
                ? line.getTotal()
                : Math.max(0d, line.getQuantity()) * Math.max(0d, line.getPurchaseUnitPrice()))
                .sum();
    }

    private double purchaseUnitCost(InvoiceLine line) {
        if (line == null) {
            return 0d;
        }
        if (line.getPurchaseUnitPrice() > 0) {
            return line.getPurchaseUnitPrice();
        }
        // Si la facture donne seulement le total, on deduit le cout unitaire par la quantite.
        double quantity = line.getQuantity() <= 0 ? 1d : line.getQuantity();
        return line.getTotal() > 0 ? line.getTotal() / quantity : 0d;
    }

    public String salePriceWarnings(InvoiceDraft draft) {
        if (draft == null || draft.getLines() == null) {
            return "";
        }
        StringBuilder warnings = new StringBuilder();
        for (InvoiceLine line : draft.getLines()) {
            double purchaseCost = purchaseUnitCost(line);
            if (line.getSalePrice() != null && line.getSalePrice() <= purchaseCost) {
                warnings.append("- Attention: le prix de vente de ")
                        .append(safe(line.getProductName(), "produit"))
                        .append(" (").append(line.getSalePrice()).append(") est inferieur ou egal au prix d'achat unitaire (")
                        .append(purchaseCost).append(").\n");
            }
        }
        return warnings.toString();
    }

    public String missingPriceTemplate(InvoiceDraft draft) {
        List<String> productNames = draft == null ? List.of() : draft.getMissingSalePrices();
        return missingPriceTemplate(productNames, draft);
    }

    private String missingPriceTemplate(List<String> productNames, InvoiceDraft draft) {
        List<String> names = productNames == null ? List.of() : productNames.stream()
                .filter(Objects::nonNull)
                .toList();
        StringBuilder builder = new StringBuilder("""
               Certains produits n'ont pas encore de prix de vente.
               Répondez ligne par ligne avec le format:

               numero, quantite min, quantite max, prix vente, devise

               Produits concernés:
               """);
        for (int i = 0; i < names.size(); i++) {
            builder.append(i + 1).append(". ").append(names.get(i)).append("\n");
        }
        builder.append("""

               Exemple:
               1, 1, 999999, 25, """).append(invoiceCurrency(draft)).append("""

               Vous pouvez envoyer plusieurs lignes a la fois. Si un prix de vente n'est pas rentable, Jemima vous le signalera avant l'enregistrement.
               """);
        return builder.toString();
    }

    private Mesure findOrCreateUnit(Produit product, InvoiceLine line) {
        String measureName = line == null ? null : line.getMeasureName();
        if (measureName != null && !measureName.isBlank()) {
            List<Mesure> found = MesureDelegate.findMesureByProduit(product.getUid(), measureName.trim());
            if (found != null && !found.isEmpty()) {
                return found.get(0);
            }
        }
        Mesure unit = MesureDelegate.findByProduitAndQuant(product.getUid(), 1d);
        if (unit != null) {
            return unit;
        }
        Mesure mesure = new Mesure(DataId.generate());
        mesure.setDescription(measureName == null || measureName.isBlank() ? "Pièce" : measureName.trim());
        mesure.setQuantContenu(1d);
        mesure.setProduitId(product);
        Mesure saved = MesureDelegate.saveMesure(mesure);
        syncCreate(saved, Tables.MESURE);
        return saved;
    }

    private void copyOrCreateSalePrices(Produit product, Recquisition recquisition, Mesure unit, InvoiceLine line, String currency) {
        List<PrixDeVente> prices = latestSalePrices(product);
        if (prices != null && !prices.isEmpty()) {
            for (PrixDeVente price : prices) {
                PrixDeVente copy = findExistingSalePrice(recquisition, price.getMesureId(), price.getQmin(), price.getQmax());
                boolean isNew = copy == null;
                if (isNew) {
                    copy = new PrixDeVente(DataId.generate());
                }
                copy.setRecquisitionId(recquisition);
                copy.setDevise(price.getDevise());
                copy.setMesureId(price.getMesureId());
                copy.setPourcentParCunit(price.getPourcentParCunit());
                copy.setPrixUnitaire(price.getPrixUnitaire());
                copy.setQmax(price.getQmax());
                copy.setQmin(price.getQmin());
                saveSalePrice(copy, isNew);
            }
            return;
        }
        double qmin = line.getSalePriceQmin() == null || line.getSalePriceQmin() <= 0 ? 1d : line.getSalePriceQmin();
        double qmax = line.getSalePriceQmax() == null || line.getSalePriceQmax() <= 0 ? 999999d : line.getSalePriceQmax();
        PrixDeVente price = findExistingSalePrice(recquisition, unit, qmin, qmax);
        boolean isNew = price == null;
        if (isNew) {
            price = new PrixDeVente(DataId.generate());
        }
        price.setRecquisitionId(recquisition);
        price.setDevise(line.getSaleCurrency() == null || line.getSaleCurrency().isBlank() ? currency : line.getSaleCurrency());
        price.setMesureId(unit);
        price.setPourcentParCunit(computePercentPerCostUnit(recquisition, line));
        price.setPrixUnitaire(line.getSalePrice());
        price.setQmin(qmin);
        price.setQmax(qmax);
        saveSalePrice(price, isNew);
    }

    private PrixDeVente findExistingSalePrice(Recquisition recquisition, Mesure measure, double qmin, double qmax) {
        if (recquisition == null || recquisition.getUid() == null || measure == null || measure.getUid() == null) {
            return null;
        }
        List<PrixDeVente> prices = PrixDeVenteDelegate.findPricesForRecq(recquisition.getUid());
        if (prices == null) {
            return null;
        }
        for (PrixDeVente price : prices) {
            if (sameUid(price.getMesureId(), measure)
                    && sameDouble(price.getQmin(), qmin)
                    && sameDouble(price.getQmax(), qmax)) {
                return price;
            }
        }
        return null;
    }

    private PrixDeVente saveSalePrice(PrixDeVente price, boolean isNew) {
        PrixDeVente saved = isNew ? PrixDeVenteDelegate.savePrixDeVente(price) : PrixDeVenteDelegate.updatePrixDeVente(price);
        if (isNew) {
            syncCreate(saved, Tables.PRIXDEVENTE);
        } else {
            syncUpdate(saved, Tables.PRIXDEVENTE);
        }
        return saved;
    }

    private double computePercentPerCostUnit(Recquisition recquisition, InvoiceLine line) {
        double sale = line == null || line.getSalePrice() == null ? 0d : line.getSalePrice();
        if (sale <= 0) {
            return 0d;
        }
        // Le pourcentage part du coutAchat deja porte par la recquisition sauvegardee.
        double purchase = recquisition == null ? 0d : recquisition.getCoutAchat();
        if (purchase <= 0 && line != null) {
            purchase = line.getPurchaseUnitPrice() > 0 ? line.getPurchaseUnitPrice() : line.getTotal();
        }
        return (sale - purchase) / sale;
    }

    private List<PrixDeVente> latestSalePrices(Produit product) {
        if (product == null || product.getUid() == null) {
            return List.of();
        }
        List<PrixDeVente> prices = RecquisitionDelegate.findLastPrices(product.getUid());
        if (prices == null || prices.isEmpty()) {
            return List.of();
        }
        LocalDateTime latestDate = null;
        String latestRecquisitionId = null;
        for (PrixDeVente price : prices) {
            if (price == null || price.getRecquisitionId() == null) {
                continue;
            }
            Recquisition req = price.getRecquisitionId();
            LocalDateTime date = req.getDate() == null ? LocalDateTime.MIN : req.getDate();
            if (latestDate == null || date.isAfter(latestDate)) {
                latestDate = date;
                latestRecquisitionId = req.getUid();
            }
        }
        if (latestRecquisitionId == null) {
            return List.of();
        }
        List<PrixDeVente> latestPrices = new ArrayList<>();
        for (PrixDeVente price : prices) {
            if (price != null
                    && price.getRecquisitionId() != null
                    && latestRecquisitionId.equals(price.getRecquisitionId().getUid())) {
                latestPrices.add(price);
            }
        }
        return latestPrices;
    }

    private void writeSheet(XSSFWorkbook workbook, String name, List<FinancialStatementRow> rows) {
        var sheet = workbook.createSheet(name);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Code");
        header.createCell(1).setCellValue("Rubrique");
        header.createCell(2).setCellValue("Nature");
        header.createCell(3).setCellValue("N");
        header.createCell(4).setCellValue("N-1");
        header.createCell(5).setCellValue("N-2");
        int rowIndex = 1;
        for (FinancialStatementRow line : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(line.getCode());
            row.createCell(1).setCellValue(line.getRubrique());
            row.createCell(2).setCellValue(line.getNature());
            row.createCell(3).setCellValue(value(line.getAmountN()));
            row.createCell(4).setCellValue(value(line.getAmountN1()));
            row.createCell(5).setCellValue(value(line.getAmountN2()));
        }
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private Entreprise currentEntreprise() {
        Preferences preferences = Preferences.userNodeForPackage(SyncEngine.class);
        Entreprise entreprise = new Entreprise(preferences.get("eUid", ""));
        entreprise.setNomEntreprise(preferences.get("ent_name", "Kazisafe"));
        entreprise.setAdresse(preferences.get("ent_adresse", ""));
        entreprise.setIdentification(preferences.get("ent_ID", ""));
        entreprise.setIdNat(preferences.get("ent_idnat", ""));
        entreprise.setNumeroImpot(preferences.get("ent_impot", ""));
        entreprise.setPhones(preferences.get("ent_phones", ""));
        return entreprise;
    }

    private LocalDateTime parseDate(String value) {
        try {
            return value == null || value.isBlank() ? LocalDateTime.now() : LocalDate.parse(value).atStartOfDay();
        } catch (Exception ignored) {
            return LocalDateTime.now();
        }
    }

    private LocalDate parseExpiryDate(String value) {
        try {
            if (value == null || value.isBlank()
                    || value.equalsIgnoreCase("none")
                    || value.equalsIgnoreCase("na")
                    || value.equalsIgnoreCase("n/a")) {
                return null;
            }
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private double value(Double value) {
        return value == null ? 0d : value;
    }

    private double positive(Double value) {
        return value == null || value < 0 ? 0d : value;
    }

    private String invoiceCurrency(InvoiceDraft draft) {
        return draft == null || draft.getCurrency() == null || draft.getCurrency().isBlank() ? "USD" : draft.getCurrency();
    }

    private String invoiceReference(InvoiceDraft draft) {
        return draft == null || draft.getReference() == null || draft.getReference().isBlank()
                ? "LOT-" + System.currentTimeMillis()
                : draft.getReference();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private InvoiceWorkflowContext workflow(String workflowId) {
        return workflowId == null || workflowId.isBlank() ? null : INVOICE_WORKFLOWS.get(workflowId.trim());
    }

    private SaleWorkflowContext saleWorkflow(String workflowId) {
        return workflowId == null || workflowId.isBlank() ? null : SALE_WORKFLOWS.get(workflowId.trim());
    }

    private ExpenseWorkflowContext expenseWorkflow(String workflowId) {
        return workflowId == null || workflowId.isBlank() ? null : EXPENSE_WORKFLOWS.get(workflowId.trim());
    }

    private void cleanupWorkflowCancellationRequests() {
        long now = System.currentTimeMillis();
        WORKFLOW_CANCELLATION_REQUESTS.entrySet().removeIf(entry -> now > entry.getValue().expiresAtMs());
    }

    private WorkflowTarget resolveWorkflowTarget(String workflowId) {
        String id = workflowId == null ? "" : workflowId.trim();
        if (!id.isBlank()) {
            InvoiceWorkflowContext invoice = workflow(id);
            if (isActiveInvoiceWorkflow(invoice)) {
                return new WorkflowTarget(id, "invoice", invoice.createdAtMs);
            }
            SaleWorkflowContext sale = saleWorkflow(id);
            if (isActiveSaleWorkflow(sale)) {
                return new WorkflowTarget(id, "sale", sale.createdAtMs);
            }
            ExpenseWorkflowContext expense = expenseWorkflow(id);
            if (isActiveExpenseWorkflow(expense)) {
                return new WorkflowTarget(id, "expense", expense.createdAtMs);
            }
            return null;
        }
        WorkflowTarget latest = null;
        for (Map.Entry<String, InvoiceWorkflowContext> entry : INVOICE_WORKFLOWS.entrySet()) {
            InvoiceWorkflowContext context = entry.getValue();
            if (isActiveInvoiceWorkflow(context) && (latest == null || context.createdAtMs > latest.createdAtMs())) {
                latest = new WorkflowTarget(entry.getKey(), "invoice", context.createdAtMs);
            }
        }
        for (Map.Entry<String, SaleWorkflowContext> entry : SALE_WORKFLOWS.entrySet()) {
            SaleWorkflowContext context = entry.getValue();
            if (isActiveSaleWorkflow(context) && (latest == null || context.createdAtMs > latest.createdAtMs())) {
                latest = new WorkflowTarget(entry.getKey(), "sale", context.createdAtMs);
            }
        }
        for (Map.Entry<String, ExpenseWorkflowContext> entry : EXPENSE_WORKFLOWS.entrySet()) {
            ExpenseWorkflowContext context = entry.getValue();
            if (isActiveExpenseWorkflow(context) && (latest == null || context.createdAtMs > latest.createdAtMs())) {
                latest = new WorkflowTarget(entry.getKey(), "expense", context.createdAtMs);
            }
        }
        return latest;
    }

    private boolean isActiveInvoiceWorkflow(InvoiceWorkflowContext context) {
        return context != null && !context.aborted && !context.requisitionsCreated;
    }

    private boolean isActiveSaleWorkflow(SaleWorkflowContext context) {
        return context != null && !context.cancelled && !context.treasuryCreated;
    }

    private boolean isActiveExpenseWorkflow(ExpenseWorkflowContext context) {
        return context != null && !context.cancelled && !context.operationCreated;
    }

    private String cancelWorkflow(String workflowId, String message) {
        InvoiceWorkflowContext invoice = workflow(workflowId);
        if (invoice != null) {
            if (!isActiveInvoiceWorkflow(invoice)) {
                return "Le workflow facture " + workflowId + " est déjà terminé ou annulé.";
            }
            invoice.aborted = true;
            invoice.abortMessage = message;
            return "Annulation confirmée. Le workflow facture " + workflowId + " est arrêté.";
        }
        SaleWorkflowContext sale = saleWorkflow(workflowId);
        if (sale != null) {
            if (!isActiveSaleWorkflow(sale)) {
                return "Le workflow vente " + workflowId + " est déjà terminé ou annulé.";
            }
            sale.cancelled = true;
            sale.cancelMessage = message;
            return "Annulation confirmée. Le workflow vente " + workflowId + " est arrêté.";
        }
        ExpenseWorkflowContext expense = expenseWorkflow(workflowId);
        if (expense != null) {
            if (!isActiveExpenseWorkflow(expense)) {
                return "Le workflow dépense " + workflowId + " est déjà terminé ou annulé.";
            }
            expense.cancelled = true;
            expense.cancelMessage = message;
            return "Annulation confirmée. Le workflow dépense " + workflowId + " est arrêté.";
        }
        return "Workflow introuvable: " + workflowId;
    }

    private boolean isPositiveConfirmation(String answer) {
        String value = normalizeToolKey(answer);
        return value.equals("oui")
                || value.equals("ok")
                || value.equals("confirme")
                || value.equals("j'accepte")
                || value.equals("jaccepte")
                || value.equals("yes");
    }

    private String lineKey(InvoiceLine line) {
        return normalizeForKey(line == null ? null : line.getProductName());
    }

    private String normalizeForKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean sameUid(Produit first, Produit second) {
        return first != null && second != null && Objects.equals(first.getUid(), second.getUid());
    }

    private boolean sameUid(Mesure first, Mesure second) {
        return first != null && second != null && Objects.equals(first.getUid(), second.getUid());
    }

    private boolean sameEntityUid(String firstUid, String secondUid) {
        return firstUid != null && secondUid != null && Objects.equals(firstUid, secondUid);
    }

    private boolean sameDouble(double first, double second) {
        return Math.abs(first - second) < 0.000001d;
    }

    private boolean sameText(String first, String second) {
        return safe(first, "").trim().equalsIgnoreCase(safe(second, "").trim());
    }

    private LocalDate localDateOf(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private Double sumInMeasure(Double firstQuantity, Mesure firstMeasure, Double secondQuantity, Mesure secondMeasure, Mesure targetMeasure) {
        double baseQuantity = quantityInBase(nvl(firstQuantity), firstMeasure) + quantityInBase(nvl(secondQuantity), secondMeasure);
        return quantityFromBase(baseQuantity, targetMeasure);
    }

    private double quantityInBase(double quantity, Mesure measure) {
        return quantity * measureUnit(measure);
    }

    private double quantityFromBase(double baseQuantity, Mesure measure) {
        double unit = measureUnit(measure);
        return unit == 0 ? baseQuantity : baseQuantity / unit;
    }

    private double measureUnit(Mesure measure) {
        return measure == null || measure.getQuantContenu() == null || measure.getQuantContenu() <= 0
                ? 1d
                : measure.getQuantContenu();
    }

    private Mesure smallestMeasure(Mesure first, Mesure second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return measureUnit(first) <= measureUnit(second) ? first : second;
    }

    private double nvl(Double value) {
        return value == null ? 0d : value;
    }

    private Double maxNullable(Double first, Double second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return Math.max(first, second);
    }

    private static class InvoiceWorkflowContext {

        private final long createdAtMs = System.currentTimeMillis();
        private final InvoiceDraft draft;
        private final String reference;
        private final LocalDateTime date;
        private final Map<String, Produit> products = new HashMap<>();
        private final Map<String, Mesure> units = new HashMap<>();
        private final List<String> catalogLines = new ArrayList<>();
        private final List<String> inserted = new ArrayList<>();
        private Fournisseur supplier;
        private Livraison delivery;
        private boolean productsCreated;
        private boolean deliveryCreated;
        private boolean requisitionsCreated;
        private boolean aborted;
        private String abortMessage = "";

        private InvoiceWorkflowContext(InvoiceDraft draft) {
            this.draft = draft;
            this.reference = draft == null || draft.getReference() == null || draft.getReference().isBlank()
                    ? "JEMIMA-" + System.currentTimeMillis()
                    : draft.getReference();
            LocalDateTime parsed;
            try {
                parsed = draft == null || draft.getInvoiceDate() == null || draft.getInvoiceDate().isBlank()
                        ? LocalDateTime.now()
                        : LocalDate.parse(draft.getInvoiceDate()).atStartOfDay();
            } catch (Exception ignored) {
                parsed = LocalDateTime.now();
            }
            this.date = parsed;
        }

        private String summary() {
            return "reference=" + reference
                    + ", supplier=" + (draft == null ? "-" : draft.getSupplier())
                    + ", productsCreated=" + productsCreated
                    + ", deliveryCreated=" + deliveryCreated
                    + ", requisitionsCreated=" + requisitionsCreated
                    + ", aborted=" + aborted
                    + ", lines=" + (draft == null || draft.getLines() == null ? 0 : draft.getLines().size());
        }
    }

    private class SaleWorkflowContext {

        private final long createdAtMs = System.currentTimeMillis();
        private final SaleDraft draft;
        private final LocalDateTime date;
        private final String region;
        private final String reference;
        private Client client;
        private Vente sale;
        private List<LigneVente> lines = new ArrayList<>();
        private CompteTresor account;
        private Traisorerie treasury;
        private boolean saleCreated;
        private boolean treasuryCreated;
        private boolean cancelled;
        private String cancelMessage = "Workflow vente annulé.";

        private SaleWorkflowContext(SaleDraft draft) {
            this.draft = draft;
            this.date = parseDate(draft == null ? null : draft.getSaleDate());
            this.region = pref.get("region", "Goma");
            this.reference = draft == null || draft.getReference() == null || draft.getReference().isBlank()
                    ? saleReference()
                    : draft.getReference().trim();
        }

        private String summary() {
            return "reference=" + reference
                    + ", saleCreated=" + saleCreated
                    + ", treasuryCreated=" + treasuryCreated
                    + ", cancelled=" + cancelled
                    + ", client=" + (client == null ? safe(draft.getClientName(), "Anonyme") : client.getNomClient())
                    + ", lines=" + (draft == null || draft.getLines() == null ? 0 : draft.getLines().size());
        }
    }

    private class ExpenseWorkflowContext {

        private final long createdAtMs = System.currentTimeMillis();
        private final ExpenseDraft draft;
        private final LocalDateTime date;
        private final String region;
        private final String reference;
        private Depense expense;
        private CompteTresor account;
        private Traisorerie treasury;
        private Operation operation;
        private boolean prepared;
        private boolean operationCreated;
        private boolean cancelled;
        private String cancelMessage = "Workflow dépense annulé.";

        private ExpenseWorkflowContext(ExpenseDraft draft) {
            this.draft = draft;
            this.date = parseDate(draft == null ? null : draft.getExpenseDate());
            this.region = pref.get("region", "Goma");
            this.reference = draft == null || draft.getReference() == null || draft.getReference().isBlank()
                    ? expenseReference()
                    : draft.getReference().trim();
        }

        private String summary() {
            return "reference=" + reference
                    + ", expense=" + (expense == null ? safe(draft.getExpenseName(), "-") : expense.getNomDepense())
                    + ", account=" + (account == null ? "-" : account.getIntitule())
                    + ", prepared=" + prepared
                    + ", operationCreated=" + operationCreated
                    + ", cancelled=" + cancelled
                    + ", amount=" + (draft == null ? 0d : draft.getAmount())
                    + " " + (draft == null ? "" : normalizeCurrency(draft.getCurrency()));
        }
    }

    private void syncCreate(data.BaseModel model, Tables table) {
        try {
            Util.sync(model, Constants.ACTION_CREATE, table);
        } catch (Exception ex) {
            System.err.println("Jemima sync HTTP échouée pour " + table + ": " + ex.getMessage());
        }
    }

    private void syncUpdate(data.BaseModel model, Tables table) {
        try {
            Util.sync(model, Constants.ACTION_UPDATE, table);
        } catch (Exception ex) {
            System.err.println("Jemima sync HTTP update échouée pour " + table + ": " + ex.getMessage());
        }
    }

    private void syncDelete(data.BaseModel model, Tables table) {
        try {
            Util.sync(model, Constants.ACTION_DELETE, table);
        } catch (Exception ex) {
            System.err.println("Jemima sync HTTP delete échouée pour " + table + ": " + ex.getMessage());
        }
    }
}
