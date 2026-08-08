package com.endeleya.ia;

import com.endeleya.kazisafex.PaymentController;
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
import data.Aretirer;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Compter;
import data.Depot;
import data.Destocker;
import data.Depense;
import data.Entreposer;
import data.Entreprise;
import data.Facture;
import data.Fournisseur;
import data.Immobilisation;
import data.Imputer;
import data.Inventaire;
import data.LigneVente;
import data.Livraison;
import data.Matiere;
import data.MatiereSku;
import data.Mesure;
import data.Operation;
import data.Presence;
import data.PrixDeVente;
import data.Produit;
import data.Production;
import data.Recquisition;
import data.Repartir;
import data.RetourDepot;
import data.RetourMagasin;
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
import delegates.AretirerDelegate;
import delegates.CategoryDelegate;
import delegates.ClientDelegate;
import delegates.ClientAppartenirDelegate;
import delegates.ClientOrganisationDelegate;
import delegates.CompteTresorDelegate;
import delegates.CompterDelegate;
import delegates.DepotDelegate;
import delegates.DestockerDelegate;
import delegates.DepenseAgregateDelegate;
import delegates.DepenseDelegate;
import delegates.EntreposerDelegate;
import delegates.FactureDelegate;
import delegates.FournisseurDelegate;
import delegates.ImmobilisationDelegate;
import delegates.ImputerDelegate;
import delegates.InventaireDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MatiereDelegate;
import delegates.MatiereSkuDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.PresenceDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.ProductionDelegate;
import delegates.RecquisitionDelegate;
import delegates.RepportDelegate;
import delegates.RepartirDelegate;
import delegates.RetourDepotDelegate;
import delegates.RetourMagasinDelegate;
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
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.CurrencyConverter;
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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import utilities.PDFUtils;
import utilities.Peremption;
import tools.Rupture;
import tools.PurchaseBySupplier;
import tools.PurchaseByProduct;
import tools.PurchaseByMonth;
import tools.SaleReport;
import tools.VenteReporter;
import tools.ExpenseByImputation;
import retrofit2.Response;
import services.FinancialStatementAgregateService;
import services.ManagedSessionFactory;
import services.StockDepotAgregateService;
import com.endeleya.kazisafex.PaymentController;
import tools.Constants;
import tools.DataId;
import tools.FileUtils;
import tools.FinancialStatementPdfExporter;
import tools.FinancialStatementRow;
import tools.SyncEngine;
import tools.Tables;
import tools.Util;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.nio.file.Files;

public class GratienTools {

    private final Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    private final FinancialStatementAgregateService financialService = new FinancialStatementAgregateService();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, InvoiceWorkflowContext> INVOICE_WORKFLOWS = new ConcurrentHashMap<>();
    private static final Map<String, CurrencyConversionState> PENDING_CURRENCY_CONVERSIONS = new ConcurrentHashMap<>();
    private static final Map<String, SaleWorkflowContext> SALE_WORKFLOWS = new ConcurrentHashMap<>();
    private static final Map<String, ExpenseWorkflowContext> EXPENSE_WORKFLOWS = new ConcurrentHashMap<>();
    private static final Map<String, ProductCodePrintBatch> PRODUCT_CODE_PRINT_BATCHES = new ConcurrentHashMap<>();
    private static final Map<String, DuplicateProductBatch> DUPLICATE_PRODUCT_BATCHES = new ConcurrentHashMap<>();
    private static final Map<String, ProductVisibilityRepairRequest> PRODUCT_VISIBILITY_REPAIR_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<String, MysqlReplicationPlan> MYSQL_REPLICATION_PLANS = new ConcurrentHashMap<>();
    private static final Map<String, String> MYSQL_ROOT_PASSWORD_TOKENS = new ConcurrentHashMap<>();
    private static final Map<String, WorkflowCancellationRequest> WORKFLOW_CANCELLATION_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<String, ToolExecutionResult> RECENT_TOOL_EXECUTIONS = new ConcurrentHashMap<>();
    private static final long TOOL_EXECUTION_TTL_MS = 120_000L;
    private static final long WORKFLOW_CANCELLATION_TTL_MS = 180_000L;
    private static final long ACTIVE_WORKFLOW_MAX_AGE_MS = 30 * 60_000L;
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
            @P("Mot de passe de l'utilisateur de replication; si vide Gratien genere un mot de passe") String replicationPassword,
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
            @P("Mot de passe root MySQL du replica. Gratien doit le demander a l'utilisateur avant d'appeler ce tool") String mysqlRootPassword) {
        String key = safe(planId, "") + "|" + safe(replicaHost, "") + "|" + replicaPort + "|" + securePasswordKey(mysqlRootPassword);
        return executeOnce("executeMysqlReplicaConfiguration", key, () -> {
            MysqlReplicationPlan plan = MYSQL_REPLICATION_PLANS.get(safe(planId, "").trim());
            if (plan == null) {
                return "Plan de réplication introuvable. Demandez d'abord à Gratien de générer la configuration replica.";
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
            @P("Mot de passe root MySQL du replica. Gratien doit le demander a l'utilisateur avant d'appeler ce tool") String mysqlRootPassword) {
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

    @Tool("Retourne le prix de vente actuel d'un produit: prix unitaire, devise, intervalle de quantite et unite de mesure")
    public String getProductCurrentSalePrice(
            @P("Nom, code-barres ou uid du produit dont on veut le prix de vente actuel") String productQuery) {
        String query = safe(productQuery, "").trim();
        if (query.isBlank()) {
            return "Indiquez le nom, le code-barres ou le uid du produit dont vous voulez le prix de vente.";
        }
        try {
            Produit product = findProductByUidCodebarOrName(query);
            if (product == null) {
                return "Produit introuvable pour: " + query
                        + ". Utilisez `searchProductsByCriteria` ou `findProductCandidates` pour trouver le produit exact.";
            }
            List<PrixDeVente> prices = latestSalePrices(product);
            if (prices == null || prices.isEmpty()) {
                return "Aucun prix de vente configure pour le produit: " + productLine(product)
                        + ". Utilisez `diagnoseInvisibleProductInPos` pour diagnostiquer pourquoi ce produit n'a pas de prix de vente.";
            }
            StringBuilder builder = new StringBuilder();
            builder.append("Prix de vente actuel de: ").append(productLine(product)).append("\n\n");
            int index = 0;
            for (PrixDeVente price : prices) {
                if (price == null) {
                    continue;
                }
                String measure = price.getMesureId() == null ? "-" : safe(price.getMesureId().getDescription(), "-");
                builder.append(++index).append(". ")
                        .append("quantite de ").append(price.getQmin())
                        .append(" a ").append(price.getQmax())
                        .append(" ").append(measure)
                        .append(" -> ").append(price.getPrixUnitaire())
                        .append(" ").append(safe(price.getDevise(), "-"))
                        .append(" par unite\n");
            }
            return builder.toString().trim();
        } catch (Exception ex) {
            // Ne jamais faire echouer l'execution de l'outil: on retourne un diagnostic lisible.
            return "Impossible de lire le prix de vente actuel pour: " + query
                    + " (donnees de prix/recquisition incompletes: " + safe(ex.getMessage(), ex.getClass().getSimpleName())
                    + "). Verifiez que le produit a bien une recquisition et des prix de vente, "
                    + "ou utilisez `diagnoseInvisibleProductInPos` pour un diagnostic complet.";
        }
    }

    @Tool("Diagnostique pourquoi un produit n'est pas visible dans le POS/ListItemView: produit, mesures, mesure unitaire, dernière réquisition, livraison et prix de vente")
    public String diagnoseInvisibleProductInPos(
            @P("Nom, code-barres ou uid du produit invisible dans le POS") String productQuery) {
        return executeOnce("diagnoseInvisibleProductInPos", productQuery, () -> {
            String query = safe(productQuery, "").trim();
            if (query.isBlank()) {
                return "Indiquez le nom, le code-barres ou le uid du produit invisible dans le POS.";
            }
            String region = pref.get("region", "Goma");
            Produit product = findProductByUidCodebarOrName(query);
            if (product == null) {
                String batchId = registerProductVisibilityRepair(query, null, "PRODUIT_ABSENT");
                return "Diagnostic POS: le produit `" + query + "` n'existe pas dans la liste des produits.\n\n"
                        + "Cause probable: le ListItemView ne peut pas l'afficher parce que le catalogue ne contient pas ce produit.\n"
                        + productVisibilityRepairInstructions(batchId, query, "Créer le produit, sa mesure unitaire, une réquisition générique et son prix de vente.");
            }

            boolean existsInProductList = productExistsInCatalogList(product);
            List<Mesure> measures = safeList(MesureDelegate.findMesureByProduit(product.getUid()));
            Mesure unit = MesureDelegate.findByProduitAndQuant(product.getUid(), 1d);
            List<Recquisition> recquisitions = sortedProductRecquisitions(product, region);
            Recquisition latest = recquisitions.isEmpty() ? null : recquisitions.get(0);
            Livraison delivery = latest == null ? null : findDeliveryForRecquisition(latest);
            List<PrixDeVente> prices = latest == null ? List.of() : safeList(PrixDeVenteDelegate.findPricesForRecq(latest.getUid()));

            StringBuilder report = new StringBuilder();
            report.append("Diagnostic POS pour: ").append(productLine(product)).append("\n\n")
                    .append("- Produit dans la liste catalogue: ").append(existsInProductList ? "oui" : "non").append("\n")
                    .append("- Mesure(s): ").append(measures.size()).append(formatMeasuresForDiagnostic(measures)).append("\n")
                    .append("- Mesure unitaire quantContenu=1: ").append(unit == null ? "non" : "oui, " + unit.getDescription()).append("\n")
                    .append("- Dernier approvisionnement/réquisition: ").append(formatRecquisitionForDiagnostic(latest)).append("\n")
                    .append("- Livraison liée: ").append(formatDeliveryForDiagnostic(delivery)).append("\n")
                    .append("- Prix de vente sur cette réquisition: ").append(prices.isEmpty() ? "aucun" : prices.size() + " prix").append("\n");

            if (latest != null && prices.isEmpty()) {
                List<PrixDeVente> previousPrices = previousSalePrices(product, latest.getUid());
                if (!previousPrices.isEmpty()) {
                    List<PrixDeVente> copied = copySalePricesToRecquisition(previousPrices, latest, unit);
                    report.append("\nRéparation appliquée: aucun prix sur la dernière réquisition, mais Gratien a trouvé ")
                            .append(previousPrices.size())
                            .append(" prix sur une réquisition précédente et l'a copié vers la dernière réquisition.\n")
                            .append("Prix copié(s): ").append(copied.size()).append("\n")
                            .append("Demandez à l'utilisateur de rafraîchir le POS.");
                    return report.toString();
                }
                String batchId = registerProductVisibilityRepair(query, product.getUid(), "PRIX_ABSENT");
                report.append("\nCause probable: le produit existe et a une réquisition, mais aucun prix de vente exploitable n'a été trouvé.\n")
                        .append(productVisibilityPriceInstructions(batchId, product));
                return report.toString();
            }

            if (!existsInProductList || measures.isEmpty() || unit == null || latest == null || delivery == null) {
                String reason = !existsInProductList ? "PRODUIT_HORS_LISTE"
                        : measures.isEmpty() ? "MESURE_ABSENTE"
                        : unit == null ? "MESURE_UNITAIRE_ABSENTE"
                        : latest == null ? "RECQUISITION_ABSENTE"
                        : "LIVRAISON_ABSENTE";
                String batchId = registerProductVisibilityRepair(query, product.getUid(), reason);
                report.append("\nCause probable: ").append(productVisibilityReasonLabel(reason)).append(".\n")
                        .append(productVisibilityRepairInstructions(batchId, product.getNomProduit(),
                                "Compléter la mesure unitaire et/ou créer un approvisionnement générique avec prix de vente."));
                return report.toString();
            }

            report.append("\nAucun blocage structurel détecté dans le flow catalogue -> mesure -> réquisition -> livraison -> prix.")
                    .append("\nSi le produit reste invisible, rafraîchissez le POS ou vérifiez les filtres de région/stock courant dans l'écran POS.");
            return report.toString();
        });
    }

    @Tool("Applique la correction proposée par diagnoseInvisibleProductInPos pour créer un approvisionnement générique ou configurer un prix de vente")
    public String repairInvisibleProductInPos(
            @P("batchId retourné par diagnoseInvisibleProductInPos") String batchId,
            @P("Détails JSON: productName, quantity, purchaseUnitPrice, measureName, lotNumber, expiryDate, salePrice, qmin, qmax, currency") String repairJson) {
        return executeOnce("repairInvisibleProductInPos", safe(batchId, "") + "|" + safe(repairJson, ""), () -> {
            ProductVisibilityRepairRequest request = PRODUCT_VISIBILITY_REPAIR_REQUESTS.get(safe(batchId, "").trim());
            if (request == null) {
                return "Demande de réparation introuvable. Relancez d'abord le diagnostic du produit invisible.";
            }
            Map<String, Object> values = parseJsonMap(repairJson);
            Produit product = request.productUid() == null || request.productUid().isBlank()
                    ? null
                    : ProduitDelegate.findProduit(request.productUid());
            String productName = safe(firstValue(values, "productName", "nomProduit", "produit"), product == null ? request.query() : product.getNomProduit());
            double salePrice = parseDouble(firstValue(values, "salePrice", "prixVente", "prix"), 0d);

            if ("PRIX_ABSENT".equals(request.reason())) {
                if (product == null) {
                    return "Produit introuvable pour configurer le prix. Relancez le diagnostic.";
                }
                Recquisition latest = latestProductRecquisition(product, pref.get("region", "Goma"));
                if (latest == null) {
                    return "Aucune réquisition n'existe plus pour ce produit. Relancez la réparation en approvisionnement générique.";
                }
                if (salePrice <= 0) {
                    return "Le prix de vente manque. Répondez avec un JSON contenant au moins `salePrice`, par exemple: {\"salePrice\":25,\"qmin\":1,\"qmax\":999999,\"currency\":\"USD\"}.";
                }
                Mesure unit = MesureDelegate.findByProduitAndQuant(product.getUid(), 1d);
                if (unit == null) {
                    unit = ensureUnitMeasure(product, safe(firstValue(values, "measureName", "mesure"), "Pièce"));
                }
                PrixDeVente saved = createSalePriceFromRepair(values, latest, unit, salePrice);
                PRODUCT_VISIBILITY_REPAIR_REQUESTS.remove(request.batchId());
                return "Prix de vente configuré pour rendre le produit visible au POS: "
                        + product.getNomProduit() + ", prix=" + saved.getPrixUnitaire() + " " + saved.getDevise()
                        + ". Demandez à l'utilisateur de rafraîchir le POS.";
            }

            InvoiceDraft draft = buildGenericSupplyDraftForVisibilityRepair(values, productName);
            if (salePrice <= 0 && findExistingProduct(productName) == null) {
                return "Le prix de vente est nécessaire pour créer l'approvisionnement générique du produit `" + productName + "`.\n"
                        + "Répondez avec un JSON contenant `salePrice`, `quantity`, `purchaseUnitPrice`, `lotNumber` et `expiryDate`.";
            }
            String workflowId = registerInvoiceWorkflow(draft);
            createProductsAndMeasures(workflowId);
            InvoiceWorkflowContext context = workflow(workflowId);
            if (context == null) {
                return "Impossible d'ouvrir le workflow de réparation.";
            }
            context.supplier = genericCompanySupplier();
            context.delivery = findOrCreateGenericPosRecoveryDelivery(pref.get("region", "Goma"));
            context.deliveryCreated = true;
            String result = createRequisitionsAndSalePrices(workflowId);
            PRODUCT_VISIBILITY_REPAIR_REQUESTS.remove(request.batchId());
            return result + "\n\nRéparation POS terminée avec livraison générique "
                    + context.delivery.getReference()
                    + ". Demandez à l'utilisateur de vérifier maintenant le ListItemView du POS.";
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
        cleanupStaleWorkflows();
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
            @P("sessionId interne de Gratien") String sessionId,
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
                + "Sans réponse dans 3 minutes, Gratien abandonnera l'annulation et le workflow continuera normalement.";
    }

    @Tool("Confirme ou refuse l'annulation du workflow en attente pour la session Gratien")
    public String answerWorkflowCancellation(
            @P("sessionId interne de Gratien") String sessionId,
            @P("Réponse utilisateur: oui pour annuler, non pour garder le workflow") String answer) {
        cleanupWorkflowCancellationRequests();
        String sessionKey = normalizeToolKey(sessionId);
        WorkflowCancellationRequest request = WORKFLOW_CANCELLATION_REQUESTS.get(sessionKey);
        if (request == null) {
            return "Aucune demande d'annulation active. Le workflow en cours continue normalement.";
        }
        if (System.currentTimeMillis() > request.expiresAtMs()) {
            WORKFLOW_CANCELLATION_REQUESTS.remove(sessionKey);
            return "Le délai de 3 minutes est dépassé. Gratien quitte l'annulation et laisse le workflow continuer.";
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
            
            // Handle payment type
            String paymentType = safe(context.draft.getPaymentType(), "CASH");
            String currency = safe(context.draft.getCurrency(), "USD");
            
            if (newSale) {
                sale = new Vente(DataId.generateInt());
                sale.setReference(context.reference);
                sale.setDateVente(context.date);
                sale.setRegion(context.region);
                sale.setClientId(client);
                String mappedPaymentType = paymentType;
                if ("CASH".equals(paymentType)) mappedPaymentType = "Paiement Cash";
                else if ("CREDIT".equals(paymentType)) mappedPaymentType = "Paiement a credit";
                else if ("PARTIAL".equals(paymentType)) mappedPaymentType = "Paiement Credit partiel";
                sale.setPayment(mappedPaymentType);
                sale.setLibelle("Sortie créée par Gratien");
                sale.setObservation("Vente");
                sale.setDeviseDette(currency);
                
                // Set due date
                if (context.draft.getDueDate() != null && !context.draft.getDueDate().isBlank()) {
                    try {
                        LocalDate dueDate = LocalDate.parse(context.draft.getDueDate());
                        sale.setEcheance(dueDate);
                    } catch (Exception e) {
                        // If invalid, default to 30 days from context.date
                        sale.setEcheance(context.date.toLocalDate().plusDays(30));
                    }
                } else if ("CREDIT".equals(paymentType) || "PARTIAL".equals(paymentType)) {
                    // Default to 30 days
                    sale.setEcheance(context.date.toLocalDate().plusDays(30));
                }
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
                if ("CDF".equalsIgnoreCase(currency)) {
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
            
            // Calculate debt
            double total = "CDF".equalsIgnoreCase(currency) ? totalCdf : totalUsd;
            double cashPaid = 0d;
            if ("PARTIAL".equals(paymentType)) {
                if (context.draft.getCashAmount() != null && context.draft.getCashAmount() > 0) {
                    cashPaid = context.draft.getCashAmount();
                } else if (context.draft.getCashPercentage() != null && context.draft.getCashPercentage() > 0) {
                    cashPaid = (context.draft.getCashPercentage() / 100) * total;
                }
            } else if ("CASH".equals(paymentType)) {
                cashPaid = total;
            }
            
            double debt = total - cashPaid;
            sale.setMontantDette(debt);
            
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
                    + ", total USD=" + totalUsd + ", total CDF=" + totalCdf + ", paiement=" + paymentType + ", dette=" + debt;
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
            
            // Only create treasury entry if payment is not full credit
            String paymentType = safe(context.draft.getPaymentType(), "CASH");
            if (!"CREDIT".equals(paymentType)) {
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
                
                // Calculate amount to add (cash paid, not total)
                String currency = safe(context.draft.getCurrency(), "USD");
                double total = "CDF".equalsIgnoreCase(currency) ? context.sale.getMontantCdf() : context.sale.getMontantUsd();
                double cashPaid = 0d;
                if ("PARTIAL".equals(paymentType)) {
                    if (context.draft.getCashAmount() != null && context.draft.getCashAmount() > 0) {
                        cashPaid = context.draft.getCashAmount();
                    } else if (context.draft.getCashPercentage() != null && context.draft.getCashPercentage() > 0) {
                        cashPaid = (context.draft.getCashPercentage() / 100) * total;
                    }
                } else if ("CASH".equals(paymentType)) {
                    cashPaid = total;
                }
                
                // Get current balances
                double currentBalanceUsd = 0d;
                double currentBalanceCdf = 0d;
                if (!newTreasury) {
                    currentBalanceUsd = treasury.getSoldeUsd() != null ? treasury.getSoldeUsd() : 0d;
                    currentBalanceCdf = treasury.getSoldeCdf() != null ? treasury.getSoldeCdf() : 0d;
                }
                
                if ("CDF".equalsIgnoreCase(currency)) {
                    treasury.setMontantCdf(cashPaid);
                    treasury.setMontantUsd(0d);
                    treasury.setSoldeCdf(currentBalanceCdf + cashPaid);
                    treasury.setSoldeUsd(currentBalanceUsd);
                } else {
                    treasury.setMontantUsd(cashPaid);
                    treasury.setMontantCdf(0d);
                    treasury.setSoldeUsd(currentBalanceUsd + cashPaid);
                    treasury.setSoldeCdf(currentBalanceCdf);
                }
                
                treasury.setMouvement(Mouvment.AUGMENTATION.name());
                treasury.setTypeTresorerie(TypeTraisorerie.CAISSE.name());
                treasury.setRegion(context.region);
                treasury.setTresorId(account);
                
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
            } else {
                // Full credit, no treasury entry
                String http = syncSaleByHttps(context);
                return "Sortie enregistrée avec succès (crédit): " + context.sale.getReference()
                        + "\nSynchronisation HTTPS: " + http;
            }
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

    public String insertGenericProductImageSupply(InvoiceDraft draft) {
        return executeOnce("insertGenericProductImageSupply", draftKey(draft), () -> {
            if (draft == null || !draft.hasLines()) {
                return "Aucun produit valide à enregistrer depuis l'image.";
            }
            List<String> invalid = new ArrayList<>();
            for (int i = 0; i < draft.getLines().size(); i++) {
                InvoiceLine line = draft.getLines().get(i);
                if (line == null || line.getProductName() == null || line.getProductName().isBlank()) {
                    invalid.add((i + 1) + ". produit illisible");
                    continue;
                }
                if (line.getQuantity() <= 0) {
                    invalid.add((i + 1) + ". " + line.getProductName() + " sans quantité valide");
                }
                if (line.getSalePrice() == null || line.getSalePrice() <= 0) {
                    invalid.add((i + 1) + ". " + line.getProductName() + " sans prix de vente valide");
                }
            }
            if (!invalid.isEmpty()) {
                return "Certaines lignes ne sont pas prêtes pour l'enregistrement:\n" + String.join("\n", invalid);
            }

            String workflowId = registerInvoiceWorkflow(draft);
            InvoiceWorkflowContext context = workflow(workflowId);
            if (context == null) {
                return "Workflow générique introuvable.";
            }
            String region = pref.get("region", "Goma");
            context.supplier = genericCompanySupplier();
            context.delivery = findOrCreateGenericPosRecoveryDelivery(region);
            context.deliveryCreated = true;

            if (!context.productsCreated) {
                createProductsAndMeasures(workflowId);
            }

            for (InvoiceLine line : context.draft.getLines()) {
                Produit product = context.products.getOrDefault(lineKey(line), findOrCreateProduct(line));
                Mesure unit = context.units.getOrDefault(lineKey(line), findOrCreateUnit(product, line));
                Recquisition saved = findOrCreateRecquisition(context, line, product, unit, region);
                createExplicitSalePrice(saved, unit, line, safe(context.draft.getCurrency(), "USD"));
                RecquisitionDelegate.rectifyStock(product, LocalDate.now(), LocalDate.now(), region, saved.getNumlot());
                context.inserted.add(product.getNomProduit()
                        + " | quantité " + saved.getQuantite()
                        + " | prix vente " + line.getSalePrice() + " " + safe(line.getSaleCurrency(), safe(context.draft.getCurrency(), "USD")));
            }
            context.requisitionsCreated = true;
            return "Produits lus sur image enregistrés comme approvisionnement générique.\n"
                    + "Livraison utilisée: " + context.delivery.getReference()
                    + "\n\n" + String.join("\n", context.inserted);
        });
    }

    public String registerExpenseWorkflow(ExpenseDraft draft) {
        cleanupStaleWorkflows();
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
                    .append("Gratien utilisera le lot ").append(batchId).append(".");
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
                return "Lot de doublons introuvable. Demandez d'abord à Gratien d'afficher les produits doublons.";
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

    @Tool("Détecte et fusionne automatiquement tous les clients doublons ayant le même nom et le même téléphone. Leurs ventes, commandes, retours magasin, retraits, appartenances d'organisation et historiques de paiement sont fusionnés et réassociés au client unique.")
    public String mergeDuplicateClients() {
        return executeOnce("mergeDuplicateClients", "all-clients", () -> {
            try {
                int count = ClientDelegate.mergeAllDuplicateClients();
                if (count == 0) {
                    return "Aucun client doublon n'a été détecté (même nom et même téléphone).";
                }
                return "La fusion des clients doublons a été effectuée avec succès ! " + count + " client(s) doublon(s) ont été fusionné(s) et leurs transactions associées ont été réassignées.";
            } catch (Exception ex) {
                return "Une erreur s'est produite lors de la fusion des clients doublons : " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Imprime une facture par son numero de reference sur l'imprimante par defaut.")
    public String printInvoiceByReference(@P("Numero de reference de la facture a imprimer") String invoiceReference) {

        return executeOnce("printInvoiceByReference", invoiceReference, () -> {
            try {
                // 1. Find the sale (Vente) by reference
                List<Vente> ventes = VenteDelegate.findByRef(invoiceReference);
                if (ventes == null || ventes.isEmpty()) {
                    return "Aucune facture trouvée avec la référence: " + invoiceReference;
                }
                Vente vente = ventes.get(0); // Take the first one

                // 2. Get line items (LigneVente)
                List<LigneVente> items = LigneVenteDelegate.findByReference(vente.getUid());
                if (items == null) {
                    items = new ArrayList<>();
                }

                // 3. Get preferences and settings
                String defPrinterName = pref.get("def-printer", null);
                String entrepName = pref.get("ent_name", "unknown");
                String rccm = pref.get("ent_ID", "Aucun");
                double taux2change = CurrencyConverter.activeRate();
                String mainCurrency = pref.get("currency", "USD");
                
                // 4. Get customer info
                String clientName = "Anonyme";
                String clientPhone = "";
                Client client = vente.getClientId();
                if (client != null) {
                    clientName = client.getNomClient();
                    clientPhone = client.getPhone() == null ? "" : client.getPhone();
                }

                // 5. Get amount paid (total sale)
                double amountPaid = "USD".equalsIgnoreCase(mainCurrency) ? vente.getMontantUsd() : vente.getMontantCdf();

                // 6. Find printer
                PrintService ps = null;
                if (defPrinterName != null && !defPrinterName.isBlank()) {
                    ps = PrinterOutputStream.getPrintServiceByName(defPrinterName);
                }
                if (ps == null) {
                    // Try default printer from system
                    ps = PrintServiceLookup.lookupDefaultPrintService();
                }
                if (ps == null) {
                    return "Aucune imprimante par défaut configurée ou trouvée.";
                }

                // 7. Print! (Let's replicate PaymentController's printReceipt logic)
                PrinterOutputStream pos = new PrinterOutputStream(ps);
                try (EscPos printer = new EscPos(pos)) {
                    // Set character code table
                    printer.setCharacterCodeTable(EscPos.CharacterCodeTable.CP863_Canadian_French);
                    
                    // Styles (like PaymentController)
                    Style title = new Style()
                            .setJustification(EscPosConst.Justification.Center)
                            .setFontSize(
                                pref.getInt("print-title-size", 1) == 1 ? Style.FontSize._1 :
                                (pref.getInt("print-title-size", 1) == 2 ? Style.FontSize._2 : Style.FontSize._3),
                                pref.getInt("print-title-size", 1) == 1 ? Style.FontSize._1 :
                                (pref.getInt("print-title-size", 1) == 2 ? Style.FontSize._2 : Style.FontSize._3)
                            );
                    Style identite = new Style()
                            .setJustification(EscPosConst.Justification.Center)
                            .setFontSize(
                                pref.getInt("print-identite-size", 1) == 1 ? Style.FontSize._1 :
                                (pref.getInt("print-identite-size", 1) == 2 ? Style.FontSize._2 : Style.FontSize._3),
                                pref.getInt("print-identite-size", 1) == 1 ? Style.FontSize._1 :
                                (pref.getInt("print-identite-size", 1) == 2 ? Style.FontSize._2 : Style.FontSize._3)
                            );
                    Style right = new Style().setJustification(EscPosConst.Justification.Right);
                    Style customer = new Style();

                    // Print header (using preferences like MainuiController)
                    String nomEntreprise = pref.get("ent_name", "unknown");
                    String adresseEntreprise = pref.get("ent_adresse", "aucune");
                    String telephoneEntreprise = pref.get("ent_phones", "");
                    String emailEntreprise = pref.get("ent_email", "");
                    
                    printer.writeLF(identite, nomEntreprise);
                    if (adresseEntreprise != null && !adresseEntreprise.isBlank()) {
                        printer.writeLF(identite, adresseEntreprise);
                    }
                    if (telephoneEntreprise != null && !telephoneEntreprise.isBlank()) {
                        printer.writeLF(identite, telephoneEntreprise);
                    }
                    if (emailEntreprise != null && !emailEntreprise.isBlank()) {
                        printer.writeLF(identite, emailEntreprise);
                    }

                    // Invoice number and date
                    printer.writeLF(right, " Facture N.: " + vente.getReference());
                    LocalDateTime dv = vente.getDateVente();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    printer.writeLF(right, dv == null ? dtf.format(LocalDateTime.now()) : dtf.format(dv));
                    
                    // Client
                    printer.write("Client : ");
                    printer.writeLF(customer, clientName);
                    
                    // Separator
                    int lineWidth = pref.getInt("print-lines-dashcount", 48);
                    printer.writeLF("-".repeat(lineWidth));
                    
                    // Print items
                    boolean isUSD = "USD".equalsIgnoreCase(mainCurrency);
                    DecimalFormat moneyFormat = isUSD ? new DecimalFormat("0.00") : new DecimalFormat("#,##0.00");
                    for (LigneVente item : items) {
                        Produit p = ProduitDelegate.findProduit((String) item.getProductId().getUid());
                        List<String> nameLines = PaymentController.wrapText(
                            (p != null ? p.getNomProduit() + " " + (p.getModele() != null ? p.getModele() : "") : "Produit inconnu"),
                            lineWidth
                        );
                        for (String lineName : nameLines) {
                            printer.writeLF(lineName);
                        }
                        String qtyStr = String.valueOf(item.getQuantite());
                        String puStr = moneyFormat.format(item.getPrixUnit());
                        String totalStr = moneyFormat.format(
                            isUSD ? item.getMontantUsd() : item.getMontantCdf()
                        );
                        // Align right for quantity, price, total
                        String itemLine = String.format(
                            "%-" + (lineWidth - 25) + "s %6s %10s %10s",
                            "",
                            qtyStr,
                            puStr,
                            totalStr
                        );
                        printer.writeLF(itemLine);
                    }
                    
                    printer.writeLF("-".repeat(lineWidth));
                    
                    // Total
                    double grandTotal = isUSD ? vente.getMontantUsd() : vente.getMontantCdf();
                    printer.writeLF(right, "Total : " + moneyFormat.format(grandTotal) + " " + mainCurrency);
                    printer.feed(4);
                    printer.cut(EscPos.CutMode.FULL);
                } finally {
                    pos.close();
                }
                
                return "Impression de la facture " + invoiceReference + " terminée avec succès sur " + ps.getName() + ".";
            } catch (Exception e) {
                Logger.getLogger(GratienTools.class.getName()).log(Level.SEVERE, "Erreur lors de l'impression", e);
                return "Erreur lors de l'impression: " + e.getMessage();
            }
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
            builder.append("\n\nConfirmez si vous voulez tout imprimer. Après votre accord, Gratien utilisera le lot ")
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
                return "Lot d'impression introuvable. Demandez d'abord à Gratien de préparer les codes à imprimer.";
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
        boolean useSsl = pref.getBoolean("default_mysql_ssl", false);
        return "jdbc:mysql://" + host + ":" + port + db
                + "?useSSL=" + useSsl + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
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
                log_bin=mysql-bin
                binlog_format=ROW
                gtid_mode=ON
                enforce_gtid-consistency=ON
                ```

                Configuration recommandée sur le replica:
                ```ini
                [mysqld]
                server-id=%d
                relay-log=relay-bin
                read_only=ON
                gtid_mode=ON
                enforce_gtid-consistency=ON
                ```

                Pour exécuter la configuration côté replica, Gratien doit demander le mot de passe root MySQL du replica, puis appeler l'outil d'exécution avec ce `planId` et l'adresse de la machine replica. La machine replica doit être différente du master.
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
        delivery.setObservation("Livraison generique creee par Gratien pour transfert de doublons produits");
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

    private record FinancialReportPayload(List<String> headers, List<FinancialStatementRow> bilan,
            List<FinancialStatementRow> compteResultat, List<FinancialStatementRow> fluxTresorerie,
            String regionLabel, String periodLabel) {
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
        String conversionIssue = checkCurrencyConversionBeforeWorkflow(draft);
        if (conversionIssue != null) {
            return conversionIssue;
        }
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
        cleanupStaleWorkflows();
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

    @Tool("Affiche dans le chat un rapport d'achats (par fournisseur, par produit ou par mois) en tableau Markdown, avec suggestion d'export Excel ou PDF")
    public String previewPurchaseReportMarkdown(
            @P("Type de rapport: fournisseur, produit ou mois") String rapport,
            @P("Source des achats pour le rapport par produit: depot (Stocker) ou pdv (recquisition). Par defaut depot.") String source,
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(rapport, "") + "|" + safe(source, "") + "|" + safe(periodeDebut, "") + "|" + safe(periodeFin, "")
                + "|" + safe(region, "");
        return executeOnce("previewPurchaseReportMarkdown", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String type = resolvePurchaseReportType(rapport);
                String src = resolvePurchaseSource(source);
                StringBuilder builder = new StringBuilder();
                String title = "Rapport des achats " + purchaseTypeLabel(type) + purchaseSourceLabel(type, src);
                appendReportHeader(builder, currentEntreprise(), title, usedRegion, period[0], period[1]);
                appendPurchaseReportMarkdown(builder, type, src, period[0], period[1], usedRegion);
                appendReportFooter(builder);
                builder.append("\n\nSouhaitez-vous générer ce rapport en Excel ou en PDF ? ")
                        .append("Après votre confirmation, Gratien générera un seul fichier final pour cette demande.");
                return builder.toString();
            } catch (Exception ex) {
                return "Impossible d'afficher le rapport d'achats: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Génère et ouvre le rapport d'achats demandé en fichier Excel après confirmation de l'aperçu Markdown")
    public String generatePurchaseReportExcel(
            @P("Type de rapport: fournisseur, produit ou mois") String rapport,
            @P("Source des achats pour le rapport par produit: depot (Stocker) ou pdv (recquisition). Par defaut depot.") String source,
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(rapport, "") + "|" + safe(source, "") + "|" + safe(periodeDebut, "") + "|" + safe(periodeFin, "")
                + "|" + safe(region, "");
        return executeOnce("generatePurchaseReportExcel", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String type = resolvePurchaseReportType(rapport);
                String src = resolvePurchaseSource(source);
                String title = "Rapport des achats " + purchaseTypeLabel(type) + purchaseSourceLabel(type, src)
                        + " - " + (usedRegion.equals("%") ? "Toutes les succursales" : usedRegion)
                        + " du " + period[0] + " au " + period[1];
                File file;
                switch (type) {
                    case "fournisseur":
                        file = Util.exportXlsPurchasesBySupplier(
                                RepportDelegate.findPurchasesBySupplier(period[0], period[1], usedRegion), title);
                        break;
                    case "mois":
                        file = Util.exportXlsPurchasesByMonth(
                                RepportDelegate.findPurchasesByMonth(period[0], period[1], usedRegion), title);
                        break;
                    default:
                        file = "pdv".equals(src)
                                ? Util.exportXlsPurchasesByProduct(
                                        RepportDelegate.findRequisitionPurchasesByProduct(period[0], period[1], usedRegion), title)
                                : Util.exportXlsPurchasesByProduct(
                                        RepportDelegate.findPurchasesByProduct(period[0], period[1], usedRegion), title);
                        break;
                }
                if (file == null) {
                    return "Échec génération Excel: aucun fichier produit pour ce rapport.";
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "Rapport d'achats Excel généré: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération Excel: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Génère et ouvre le rapport d'achats demandé en fichier PDF après confirmation de l'aperçu Markdown")
    public String generatePurchaseReportPdf(
            @P("Type de rapport: fournisseur, produit ou mois") String rapport,
            @P("Source des achats pour le rapport par produit: depot (Stocker) ou pdv (recquisition). Par defaut depot.") String source,
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(rapport, "") + "|" + safe(source, "") + "|" + safe(periodeDebut, "") + "|" + safe(periodeFin, "")
                + "|" + safe(region, "");
        return executeOnce("generatePurchaseReportPdf", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String type = resolvePurchaseReportType(rapport);
                String src = resolvePurchaseSource(source);
                File file = exportPurchaseReportPdf(currentEntreprise(), type, src, period[0], period[1], usedRegion);
                if (file == null) {
                    return "Échec génération PDF: aucun fichier produit pour ce rapport.";
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "Rapport d'achats PDF généré: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération PDF: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Affiche dans le chat un rapport des ventes (par produit, par catégorie ou par client) en tableau Markdown, avec suggestion d'export Excel ou PDF")
    public String previewSalesReportMarkdown(
            @P("Type de rapport: produit, categorie ou client") String rapport,
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(rapport, "") + "|" + safe(periodeDebut, "") + "|" + safe(periodeFin, "") + "|" + safe(region, "");
        return executeOnce("previewSalesReportMarkdown", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String type = resolveSalesReportType(rapport);
                StringBuilder builder = new StringBuilder();
                String title = "Rapport des ventes " + salesTypeLabel(type);
                appendReportHeader(builder, currentEntreprise(), title, usedRegion, period[0], period[1]);
                appendSalesReportMarkdown(builder, type, period[0], period[1], usedRegion);
                appendReportFooter(builder);
                builder.append("\n\nSouhaitez-vous générer ce rapport en Excel ou en PDF ? ")
                        .append("Après votre confirmation, Gratien générera un seul fichier final pour cette demande.");
                return builder.toString();
            } catch (Exception ex) {
                return "Impossible d'afficher le rapport des ventes: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Génère et ouvre le rapport des ventes demandé en fichier Excel après confirmation de l'aperçu Markdown")
    public String generateSalesReportExcel(
            @P("Type de rapport: produit, categorie ou client") String rapport,
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(rapport, "") + "|" + safe(periodeDebut, "") + "|" + safe(periodeFin, "") + "|" + safe(region, "");
        return executeOnce("generateSalesReportExcel", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String type = resolveSalesReportType(rapport);
                String title = "Rapport des ventes " + salesTypeLabel(type) + " - "
                        + reportRegionLabel(usedRegion) + " du " + period[0] + " au " + period[1];
                File file = exportSalesReportExcel(type, period[0], period[1], usedRegion, title);
                if (file == null) {
                    return "Échec génération Excel: aucun fichier produit pour ce rapport.";
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "Rapport des ventes Excel généré: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération Excel: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Génère et ouvre le rapport des ventes demandé en fichier PDF après confirmation de l'aperçu Markdown")
    public String generateSalesReportPdf(
            @P("Type de rapport: produit, categorie ou client") String rapport,
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(rapport, "") + "|" + safe(periodeDebut, "") + "|" + safe(periodeFin, "") + "|" + safe(region, "");
        return executeOnce("generateSalesReportPdf", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String type = resolveSalesReportType(rapport);
                File file = exportSalesReportPdf(currentEntreprise(), type, period[0], period[1], usedRegion);
                if (file == null) {
                    return "Échec génération PDF: aucun fichier produit pour ce rapport.";
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "Rapport des ventes PDF généré: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération PDF: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Affiche dans le chat un rapport des dépenses par imputation en tableau Markdown, avec suggestion d'export Excel ou PDF")
    public String previewExpenseReportMarkdown(
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(periodeDebut, "") + "|" + safe(periodeFin, "") + "|" + safe(region, "");
        return executeOnce("previewExpenseReportMarkdown", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                StringBuilder builder = new StringBuilder();
                String title = "Rapport des dépenses par imputation";
                appendReportHeader(builder, currentEntreprise(), title, usedRegion, period[0], period[1]);
                appendExpenseReportMarkdown(builder, period[0], period[1], usedRegion);
                appendReportFooter(builder);
                builder.append("\n\nSouhaitez-vous générer ce rapport en Excel ou en PDF ? ")
                        .append("Après votre confirmation, Gratien générera un seul fichier final pour cette demande.");
                return builder.toString();
            } catch (Exception ex) {
                return "Impossible d'afficher le rapport des dépenses: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Génère et ouvre le rapport des dépenses demandé en fichier Excel après confirmation de l'aperçu Markdown")
    public String generateExpenseReportExcel(
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(periodeDebut, "") + "|" + safe(periodeFin, "") + "|" + safe(region, "");
        return executeOnce("generateExpenseReportExcel", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                String title = "Rapport des dépenses par imputation - "
                        + reportRegionLabel(usedRegion) + " du " + period[0] + " au " + period[1];
                List<ExpenseByImputation> items = RepportDelegate.findExpenseReportByImputation(
                        period[0], period[1], usedRegion);
                File file = Util.exportXlsExpenseByImputation(items, title);
                if (file == null) {
                    return "Échec génération Excel: aucun fichier produit pour ce rapport.";
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "Rapport des dépenses Excel généré: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération Excel: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Génère et ouvre le rapport des dépenses demandé en fichier PDF après confirmation de l'aperçu Markdown")
    public String generateExpenseReportPdf(
            @P("Date début au format yyyy-MM-dd, optionnelle") String periodeDebut,
            @P("Date fin au format yyyy-MM-dd, optionnelle") String periodeFin,
            @P("Région optionnelle") String region) {
        String key = safe(periodeDebut, "") + "|" + safe(periodeFin, "") + "|" + safe(region, "");
        return executeOnce("generateExpenseReportPdf", key, () -> {
            try {
                LocalDate[] period = resolveReportPeriod(periodeDebut, periodeFin);
                String usedRegion = resolveReportRegion(region);
                File file = exportExpenseReportPdf(currentEntreprise(), period[0], period[1], usedRegion);
                if (file == null) {
                    return "Échec génération PDF: aucun fichier produit pour ce rapport.";
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "Rapport des dépenses PDF généré: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération PDF: " + safe(ex.getMessage(), ex.getClass().getSimpleName());
            }
        });
    }

    @Tool("Recalcule à la demande les états financiers et affiche le rapport demandé en tableaux Markdown avant export")
    public String previewFinancialStatementsMarkdown(    @P("Mode: periode, annuel ou trimestriel") String mode,
            @P("Type: bilan, resultat, flux ou tous") String statement,
            @P("Date début yyyy-MM-dd pour mode periode") String start,
            @P("Date fin yyyy-MM-dd pour mode periode") String end,
            @P("Année d'ancrage pour mode annuel ou trimestriel, ex: 2026") int anchorYear,
            @P("Nombre d'années pour mode annuel: 3 ou 5") int years,
            @P("Région optionnelle. Une autre région est acceptée seulement avec un rôle ALL_ACCESS") String region) {
        String key = safe(mode, "") + "|" + safe(statement, "") + "|" + safe(start, "") + "|" + safe(end, "")
                + "|" + anchorYear + "|" + years + "|" + safe(region, "");
        return executeOnce("previewFinancialStatementsMarkdown", key, () -> {
            try {
                FinancialReportPayload payload = loadFinancialPayload(mode, statement, start, end, anchorYear, years, region);
                StringBuilder builder = new StringBuilder();
                String title = "Rapport financier - " + payload.periodLabel();
                appendReportHeader(builder, currentEntreprise(), title, payload.regionLabel(), null, null);
                appendMarkdownTable(builder, "Bilan", payload.headers(), payload.bilan());
                appendMarkdownTable(builder, "Compte de résultat", payload.headers(), payload.compteResultat());
                appendMarkdownTable(builder, "Flux de trésorerie", payload.headers(), payload.fluxTresorerie());
                appendReportFooter(builder);
                builder.append("\n\nSouhaitez-vous générer ce rapport en Excel ou en PDF ? ");
                builder.append("Après votre confirmation, Gratien générera un seul fichier final pour cette demande.");
                return builder.toString();
            } catch (Exception ex) {
                return "Impossible d'afficher le rapport financier: " + ex.getMessage();
            }
        });
    }

    @Tool("Rectifie les agrégats des états financiers pour une période ou une année donnée")
    public String rectifyFinancialStatements(
            @P("Mode: periode ou annee") String mode,
            @P("Date début yyyy-MM-dd si mode periode") String start,
            @P("Date fin yyyy-MM-dd si mode periode") String end,
            @P("Année à rectifier si mode annee, ex: 2026") int year,
            @P("Région optionnelle. Une autre région est acceptée seulement avec Trader ou ALL_ACCESS") String region) {
        String key = safe(mode, "") + "|" + safe(start, "") + "|" + safe(end, "") + "|" + year + "|" + safe(region, "");
        return executeOnce("rectifyFinancialStatements", key, () -> {
            try {
                String usedRegion = resolveFinancialRegion(region);
                if (safe(mode, "periode").toLowerCase(Locale.ROOT).contains("ann")) {
                    int resolvedYear = year <= 0 ? LocalDate.now().getYear() : year;
                    financialService.rectifyFinancialStatementsForYear(resolvedYear, usedRegion);
                    return "Rectification des états financiers terminée pour l'année " + resolvedYear + ".";
                }
                LocalDate d1 = start == null || start.isBlank() ? LocalDate.now().withDayOfMonth(1) : LocalDate.parse(start);
                LocalDate d2 = end == null || end.isBlank() ? LocalDate.now() : LocalDate.parse(end);
                if (d1.isAfter(d2)) {
                    LocalDate tmp = d1;
                    d1 = d2;
                    d2 = tmp;
                }
                financialService.rectifyFinancialStatements(d1, d2, usedRegion);
                return "Rectification des états financiers terminée pour la période " + d1 + " au " + d2 + ".";
            } catch (Exception ex) {
                return "Échec de rectification des états financiers: " + ex.getMessage();
            }
        });
    }

    @Tool("Génère et ouvre les états financiers PDF sur une période donnée après confirmation de l'aperçu Markdown")
    public String generateFinancialStatementsPdf(
            @P("Date début au format yyyy-MM-dd") String start,
            @P("Date fin au format yyyy-MM-dd") String end,
            @P("Région optionnelle") String region) {
        return executeOnce("generateFinancialStatementsPdf", start + "|" + end + "|" + region, () -> {
            try {
                LocalDate d1 = LocalDate.parse(start);
                LocalDate d2 = LocalDate.parse(end);
                if (d1.isAfter(d2)) {
                    LocalDate tmp = d1;
                    d1 = d2;
                    d2 = tmp;
                }
                String usedRegion = resolveFinancialRegion(region);
                financialService.rebuildStatements(d1, d2, usedRegion);
                Entreprise entreprise = currentEntreprise();
                List<String> headers = List.of("Période", "Période précédente 1", "Période précédente 2",
                        "Période précédente 3");
                File file = exportCombinedFinancialPdf(entreprise, d1, d2, headers,
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_BILAN, d1, d2, usedRegion),
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, d1, d2, usedRegion),
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, d1, d2, usedRegion),
                        "periode-" + d1 + "-" + d2 + "-" + usedRegion);
                if (file != null && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "États financiers PDF générés dans un seul fichier: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération PDF: " + ex.getMessage();
            }
        });
    }

    @Tool("Génère et ouvre les états financiers Excel sur une période donnée après confirmation de l'aperçu Markdown")
    public String generateFinancialStatementsExcel(
            @P("Date début au format yyyy-MM-dd") String start,
            @P("Date fin au format yyyy-MM-dd") String end,
            @P("Région optionnelle") String region) {
        return executeOnce("generateFinancialStatementsExcel", start + "|" + end + "|" + region, () -> {
            try {
                LocalDate d1 = LocalDate.parse(start);
                LocalDate d2 = LocalDate.parse(end);
                if (d1.isAfter(d2)) {
                    LocalDate tmp = d1;
                    d1 = d2;
                    d2 = tmp;
                }
                String usedRegion = resolveFinancialRegion(region);
                financialService.rebuildStatements(d1, d2, usedRegion);
                File file = FileUtils.pointFile("financial-statements-periode-" + safeFilePart(d1 + "-" + d2 + "-" + usedRegion) + ".xlsx");
                List<String> headers = List.of("Période", "Période précédente 1", "Période précédente 2",
                        "Période précédente 3");
                try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
                    writeSheet(workbook, "Bilan", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_BILAN, d1, d2, usedRegion), headers);
                    writeSheet(workbook, "Compte Resultat", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, d1, d2, usedRegion), headers);
                    writeSheet(workbook, "Flux Tresorerie", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, d1, d2, usedRegion), headers);
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

    @Tool("Génère et ouvre les états financiers PDF comparatifs par année après confirmation de l'aperçu Markdown")
    public String generateFinancialStatementsYearlyPdf(
            @P("Année finale de comparaison, ex: 2026") int anchorYear,
            @P("Nombre d'années à afficher, généralement 3 ou 5") int years,
            @P("Région optionnelle") String region) {
        int span = years <= 3 ? 3 : 5;
        int resolvedAnchorYear = anchorYear <= 0 ? LocalDate.now().getYear() : anchorYear;
        String key = resolvedAnchorYear + "|" + span + "|" + region;
        return executeOnce("generateFinancialStatementsYearlyPdf", key, () -> {
            try {
                String usedRegion = resolveFinancialRegion(region);
                financialService.ensureYearlyStatements(resolvedAnchorYear, span, usedRegion);
                Entreprise entreprise = currentEntreprise();
                List<String> headers = yearlyHeaders(resolvedAnchorYear, span);
                LocalDate start = LocalDate.of(resolvedAnchorYear - span + 1, 1, 1);
                LocalDate end = LocalDate.of(resolvedAnchorYear, 12, 31);
                File file = exportCombinedFinancialPdf(entreprise, start, end, headers,
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_BILAN,
                                resolvedAnchorYear, span, usedRegion),
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT,
                                resolvedAnchorYear, span, usedRegion),
                        financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE,
                                resolvedAnchorYear, span, usedRegion),
                        "annuel-" + resolvedAnchorYear + "-" + span + "-" + usedRegion);
                if (file != null && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "États financiers PDF générés dans un seul fichier: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération PDF comparatif: " + ex.getMessage();
            }
        });
    }

    @Tool("Génère et ouvre les états financiers Excel comparatifs par année après confirmation de l'aperçu Markdown")
    public String generateFinancialStatementsYearlyExcel(
            @P("Année finale de comparaison, ex: 2026") int anchorYear,
            @P("Nombre d'années à afficher, généralement 3 ou 5") int years,
            @P("Région optionnelle") String region) {
        int span = years <= 3 ? 3 : 5;
        int resolvedAnchorYear = anchorYear <= 0 ? LocalDate.now().getYear() : anchorYear;
        String key = resolvedAnchorYear + "|" + span + "|" + region;
        return executeOnce("generateFinancialStatementsYearlyExcel", key, () -> {
            try {
                String usedRegion = resolveFinancialRegion(region);
                financialService.ensureYearlyStatements(resolvedAnchorYear, span, usedRegion);
                File file = FileUtils.pointFile("financial-statements-yearly-" + safeFilePart(span + "y-"
                        + resolvedAnchorYear + "-" + usedRegion) + ".xlsx");
                List<String> headers = yearlyHeaders(resolvedAnchorYear, span);
                try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
                    writeSheet(workbook, "Bilan", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_BILAN, resolvedAnchorYear, span, usedRegion),
                            headers);
                    writeSheet(workbook, "Compte Resultat", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, resolvedAnchorYear, span, usedRegion),
                            headers);
                    writeSheet(workbook, "Flux Tresorerie", financialService.loadStatementRows(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, resolvedAnchorYear, span, usedRegion),
                            headers);
                    workbook.write(out);
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "États financiers Excel générés sur " + span + " ans: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération Excel comparatif: " + ex.getMessage();
            }
        });
    }

    @Tool("Génère et ouvre les états financiers PDF d'une année séparés par trimestre après confirmation de l'aperçu Markdown")
    public String generateFinancialStatementsQuarterlyPdf(
            @P("Année à afficher par trimestre, ex: 2026") int year,
            @P("Région optionnelle") String region) {
        int resolvedYear = year <= 0 ? LocalDate.now().getYear() : year;
        String key = resolvedYear + "|" + region;
        return executeOnce("generateFinancialStatementsQuarterlyPdf", key, () -> {
            try {
                String usedRegion = resolveFinancialRegion(region);
                LocalDate anchorDate = LocalDate.of(resolvedYear, 12, 31);
                financialService.ensureQuarterlyStatements(anchorDate, 4, usedRegion);
                Entreprise entreprise = currentEntreprise();
                List<String> headers = quarterlyHeaders(resolvedYear);
                LocalDate start = LocalDate.of(resolvedYear, 1, 1);
                LocalDate end = LocalDate.of(resolvedYear, 12, 31);
                File file = exportCombinedFinancialPdf(entreprise, start, end, headers,
                        financialService.loadStatementRowsQuarterly(FinancialStatementAgregateService.STATEMENT_BILAN,
                                anchorDate, 4, usedRegion),
                        financialService.loadStatementRowsQuarterly(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT,
                                anchorDate, 4, usedRegion),
                        financialService.loadStatementRowsQuarterly(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE,
                                anchorDate, 4, usedRegion),
                        "trimestriel-" + resolvedYear + "-" + usedRegion);
                if (file != null && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "États financiers PDF générés dans un seul fichier: " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération PDF trimestriel: " + ex.getMessage();
            }
        });
    }

    @Tool("Génère et ouvre les états financiers Excel d'une année séparés par trimestre après confirmation de l'aperçu Markdown")
    public String generateFinancialStatementsQuarterlyExcel(
            @P("Année à afficher par trimestre, ex: 2026") int year,
            @P("Région optionnelle") String region) {
        int resolvedYear = year <= 0 ? LocalDate.now().getYear() : year;
        String key = resolvedYear + "|" + region;
        return executeOnce("generateFinancialStatementsQuarterlyExcel", key, () -> {
            try {
                String usedRegion = resolveFinancialRegion(region);
                LocalDate anchorDate = LocalDate.of(resolvedYear, 12, 31);
                financialService.ensureQuarterlyStatements(anchorDate, 4, usedRegion);
                File file = FileUtils.pointFile("financial-statements-quarterly-"
                        + safeFilePart(resolvedYear + "-" + usedRegion) + ".xlsx");
                List<String> headers = quarterlyHeaders(resolvedYear);
                try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
                    writeSheet(workbook, "Bilan", financialService.loadStatementRowsQuarterly(
                            FinancialStatementAgregateService.STATEMENT_BILAN, anchorDate, 4, usedRegion),
                            headers);
                    writeSheet(workbook, "Compte Resultat", financialService.loadStatementRowsQuarterly(
                            FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, anchorDate, 4, usedRegion),
                            headers);
                    writeSheet(workbook, "Flux Tresorerie", financialService.loadStatementRowsQuarterly(
                            FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, anchorDate, 4, usedRegion),
                            headers);
                    workbook.write(out);
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return "États financiers Excel générés par trimestre pour l'année " + resolvedYear
                        + ": " + file.getAbsolutePath();
            } catch (Exception ex) {
                return "Échec génération Excel trimestriel: " + ex.getMessage();
            }
        });
    }

    @Tool("Affiche la liste des produits en rupture de stock (stock ≤ alerte ou stock = 0)")
    public String listLowStockProducts() {
        String region = resolveFinancialRegion(null);
        List<Rupture> ruptures = RecquisitionDelegate.findStockEnRupture(region);
        if (ruptures == null || ruptures.isEmpty()) {
            return "Aucun produit en rupture de stock trouvé.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("### Produits en rupture de stock (").append(ruptures.size()).append(")\n\n");
        sb.append("| N# | Produit | Quantité | Prix unitaire | Localisation |\n");
        sb.append("|---|---|---|---|---|\n");
        int count = 0;
        for (Rupture r : ruptures) {
            count++;
            Produit p = r.getProduit();
            if (p == null) continue;
            String designation = safe(p.getNomProduit(), "-")
                    + (p.getMarque() != null ? " " + p.getMarque() : "")
                    + (p.getModele() != null ? " " + p.getModele() : "");
            String quantStr = r.getQuant() + " " + (r.getMesure() != null ? r.getMesure().getDescription() : "");
            String puStr = String.valueOf(r.getUnitprice());
            sb.append("| ").append(count)
                    .append(" | ").append(designation)
                    .append(" | ").append(quantStr)
                    .append(" | ").append(puStr)
                    .append(" | ").append(safe(r.getLocalisation(), "-"))
                    .append(" |\n");
        }
        sb.append("\nPour générer un fichier PDF, utilisez la commande `exportLowStockProductsPdf()`.");
        return sb.toString();
    }

    @Tool("Génère un fichier PDF listant les produits en rupture de stock et l'ouvre")
    public String exportLowStockProductsPdf() {
        try {
            String region = resolveFinancialRegion(null);
            List<Rupture> ruptures = RecquisitionDelegate.findStockEnRupture(region);
            if (ruptures == null || ruptures.isEmpty()) {
                return "Aucun produit en rupture de stock à exporter.";
            }
            Entreprise entreprise = currentEntreprise();
            File file = generateLowStockPdf(entreprise, ruptures);
            if (file != null && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
            return "PDF généré avec succès: " + file.getAbsolutePath();
        } catch (Exception ex) {
            return "Échec génération PDF rupture de stock: " + ex.getMessage();
        }
    }

    @Tool("Recherche et affiche les produits expirés ou sur le point d'expirer dans un délai donné")
    public String listExpiringProducts(
            @P("Nombre de mois pour l'échéance (ex: 4 pour les produits qui expirent dans 4 mois). Laissez vide ou 0 pour les produits déjà expirés.") String monthsStr) {
        try {
            int months = 0;
            if (monthsStr != null && !monthsStr.isBlank()) {
                months = Integer.parseInt(monthsStr.trim());
                if (months < 0) months = 0;
            }
            LocalDate today = LocalDate.now();
            LocalDate endDate = months > 0 ? today.plusMonths(months) : today;
            String region = resolveFinancialRegion(null);
            List<Peremption> expired = RecquisitionDelegate.showExpiredAtInterval(
                    months > 0 ? today : LocalDate.of(1900, 1, 1), endDate, region);
            if (expired == null || expired.isEmpty()) {
                if (months > 0) {
                    return "Aucun produit expirant dans les " + months + " prochains mois.";
                }
                return "Aucun produit déjà expiré trouvé.";
            }
            StringBuilder sb = new StringBuilder();
            String title = months > 0
                    ? "Produits expirant dans les " + months + " prochains mois"
                    : "Produits déjà expirés";
            sb.append("### ").append(title).append(" (").append(expired.size()).append(")\n\n");
            sb.append("| N# | Produit | Lot | Quantité | Date expiration | Valeur |\n");
            sb.append("|---|---|---|---|---|---|\n");
            int count = 0;
            for (Peremption p : expired) {
                if (p == null || p.getQuantite() <= 0) continue;
                count++;
                sb.append("| ").append(count)
                        .append(" | ").append(safe(p.getProduit(), "-"))
                        .append(" | ").append(safe(p.getLot(), "-"))
                        .append(" | ").append(p.getQuantite()).append(" ").append(safe(p.getMesure(), ""))
                        .append(" | ").append(p.getDateExpiry() != null ? p.getDateExpiry().toString() : "-")
                        .append(" | ").append(p.getValeur())
                        .append(" |\n");
            }
            if (count == 0) {
                return "Aucun produit avec stock > 0 trouvé dans cette période.";
            }
            sb.append("\nPour générer un fichier PDF, utilisez la commande `exportExpiringProductsPdf(")
                    .append(months).append(")`.");
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Nombre de mois invalide. Veuillez entrer un nombre entier positif (ex: 4).";
        } catch (Exception ex) {
            return "Erreur lors de la recherche: " + ex.getMessage();
        }
    }

    @Tool("Génère un fichier PDF listant les produits expirés ou sur le point d'expirer selon le délai spécifié et l'ouvre")
    public String exportExpiringProductsPdf(
            @P("Nombre de mois pour l'échéance (ex: 4). 0 ou vide pour les produits déjà expirés.") String monthsStr) {
        try {
            int months = 0;
            if (monthsStr != null && !monthsStr.isBlank()) {
                months = Integer.parseInt(monthsStr.trim());
                if (months < 0) months = 0;
            }
            LocalDate today = LocalDate.now();
            LocalDate endDate = months > 0 ? today.plusMonths(months) : today;
            String region = resolveFinancialRegion(null);
            List<Peremption> expired = RecquisitionDelegate.showExpiredAtInterval(
                    months > 0 ? today : LocalDate.of(1900, 1, 1), endDate, region);
            if (expired == null || expired.isEmpty()) {
                return "Aucun produit à exporter.";
            }
            Entreprise entreprise = currentEntreprise();
            File file = generateExpiredPdf(entreprise, expired, months);
            if (file != null && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
            return "PDF généré avec succès: " + file.getAbsolutePath();
        } catch (NumberFormatException e) {
            return "Nombre de mois invalide.";
        } catch (Exception ex) {
            return "Échec génération PDF: " + ex.getMessage();
        }
    }

    @Tool("Verifie la derniere version de Kazisafe disponible et retourne les infos de mise a jour")
    public String checkLatestVersion() {
        try {
            retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl("https://cloud.kazisafe.com/v1/")
                    .addConverterFactory(retrofit2.converter.jackson.JacksonConverterFactory.create())
                    .build();
            data.network.Kazisafe api = retrofit.create(data.network.Kazisafe.class);
            retrofit2.Response<data.Module> response = api.checkUpdates().execute();
            if (!response.isSuccessful() || response.body() == null) {
                return "Impossible de contacter le serveur de mise a jour.";
            }
            data.Module module = response.body();
            String currentVersion = tools.Constants.APP_VERSION;
            return "Derniere version disponible: " + module.getVersion()
                    + "\nDate de publication: " + (module.getDateLancer() != null ? module.getDateLancer() : "inconnue")
                    + "\nFichier: " + (module.getNomModule() != null ? module.getNomModule() : "non specifie")
                    + "\nVersion actuelle installee: " + currentVersion
                    + "\n\nPage officielle: https://endeleya.com/products/kazisafe";
        } catch (Exception e) {
            return "Erreur lors de la verification: " + e.getMessage();
        }
    }

    @Tool("Consulte les pages officielles de Kazisafe sur https://endeleya.com/products/kazisafe et https://endeleya.com/kazisafe pour decouvrir les nouvelles fonctionnalites, les mises a jour recentes et la documentation")
    public String fetchKazisafePage() {
        StringBuilder result = new StringBuilder();
        result.append(fetchSinglePage("https://endeleya.com/products/kazisafe", "Produits Kazisafe")).append("\n\n");
        result.append(fetchSinglePage("https://endeleya.com/kazisafe", "Kazisafe")).append("\n\n");
        return result.toString().strip();
    }

    private String fetchSinglePage(String urlStr, String label) {
        try {
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int status = conn.getResponseCode();
            if (status != 200) {
                return "[" + label + "] Le site a repondu avec le code HTTP " + status;
            }
            StringBuilder content = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            conn.disconnect();
            String html = content.toString();
            String text = html.replaceAll("<script[^>]*>[\\s\\S]*?</script>", "")
                    .replaceAll("<style[^>]*>[\\s\\S]*?</style>", "")
                    .replaceAll("<[^>]+>", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (text.length() > 6000) {
                text = text.substring(0, 6000) + "...\n[Texte tronque - page trop longue]";
            }
            return "--- " + label + " ---\n" + text;
        } catch (Exception e) {
            return "[" + label + "] Impossible de charger la page: " + e.getMessage();
        }
    }

    @Tool("Recherche les produits existants dont le nom correspond a un texte de facture pour le desambiguiser")
    public String findProductCandidates(
            @P("Nom du produit tel qu'il apparait sur la facture") String productName) {
        if (productName == null || productName.isBlank()) {
            return "Nom de produit vide.";
        }
        List<Produit> exact = ProduitDelegate.findProduitByName(productName.trim());
        if (exact != null && !exact.isEmpty()) {
            Produit p = exact.get(0);
            return "Produit trouve exactement:\n"
                    + "- UID: " + p.getUid() + "\n"
                    + "- Nom: " + productSearchText(p) + "\n"
                    + "- Categorie: " + (p.getCategoryId() != null ? p.getCategoryId().getDescritption() : "-");
        }
        List<String> invoiceTokens = searchableProductTokens(productName);
        if (invoiceTokens.isEmpty()) {
            return "Impossible d'extraire des mots significatifs de ce nom.";
        }
        List<Produit> products = ProduitDelegate.findProduits();
        if (products == null || products.isEmpty()) {
            return "Aucun produit en base.";
        }
        List<Produit> candidates = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        for (Produit product : products) {
            List<String> productTokens = searchableProductTokens(productSearchText(product));
            if (productTokens.isEmpty()) continue;
            int matched = countMatchedInvoiceTokens(invoiceTokens, productTokens);
            int minRequired = requiredInvoiceTokenMatches(invoiceTokens.size());
            if (matched >= minRequired) {
                candidates.add(product);
                scores.add(matched);
            }
        }
        if (candidates.isEmpty()) {
            return "Aucun produit existant ne correspond a '" + productName + "'.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(candidates.size()).append(" produit(s) correspondent:\n\n");
        sb.append("| N# | UID | Nom complet | Score |\n");
        sb.append("|---|---|---|---|\n");
        for (int i = 0; i < candidates.size(); i++) {
            Produit p = candidates.get(i);
            sb.append("| ").append(i + 1)
                    .append(" | ").append(p.getUid())
                    .append(" | ").append(productSearchText(p))
                    .append(" | ").append(scores.get(i)).append("/").append(invoiceTokens.size())
                    .append(" |\n");
        }
        sb.append("\nUtilisez `assignProductToLine` avec l'UID du produit choisi par l'utilisateur.");
        return sb.toString();
    }

    @Tool("Assigne un produit existant a une ligne de facture dans le workflow en cours")
    public String assignProductToLine(
            @P("workflowId du workflow en cours") String workflowId,
            @P("Index de la ligne (0-based)") int lineIndex,
            @P("UID du produit a assigner") String productUid) {
        InvoiceWorkflowContext ctx = INVOICE_WORKFLOWS.get(normalizeToolKey(workflowId));
        if (ctx == null || ctx.draft == null) {
            return "Workflow ou facture introuvable.";
        }
        List<InvoiceLine> lines = ctx.draft.getLines();
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            return "Index de ligne invalide.";
        }
        Produit product = ProduitDelegate.findProduit(productUid);
        if (product == null) {
            return "Produit introuvable avec l'UID: " + productUid;
        }
        InvoiceLine line = lines.get(lineIndex);
        line.setProductName(productSearchText(product).strip());
        return "Ligne " + (lineIndex + 1) + " assignee au produit: " + productSearchText(product);
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

    private Produit findProductByUidCodebarOrName(String query) {
        String value = safe(query, "").trim();
        if (value.isBlank()) {
            return null;
        }
        Produit byUid = ProduitDelegate.findProduit(value);
        if (byUid != null) {
            return byUid;
        }
        Produit byCodebar = ProduitDelegate.findByCodebar(value);
        if (byCodebar != null) {
            return byCodebar;
        }
        return findExistingProduct(value);
    }

    private boolean productExistsInCatalogList(Produit product) {
        if (product == null || product.getUid() == null) {
            return false;
        }
        List<Produit> products = ProduitDelegate.findProduits();
        if (products == null) {
            return false;
        }
        for (Produit item : products) {
            if (item != null && Objects.equals(item.getUid(), product.getUid())) {
                return true;
            }
        }
        return false;
    }

    private List<Recquisition> sortedProductRecquisitions(Produit product, String region) {
        if (product == null || product.getUid() == null) {
            return List.of();
        }
        List<Recquisition> recquisitions = safeList(RecquisitionDelegate.findDescSortedByDateForProduit(product.getUid()));
        if (recquisitions.isEmpty()) {
            recquisitions = safeList(RecquisitionDelegate.findRecquisitionByProduitRegion(product.getUid(), region));
        }
        recquisitions.sort((left, right) -> {
            LocalDateTime dl = left == null || left.getDate() == null ? LocalDateTime.MIN : left.getDate();
            LocalDateTime dr = right == null || right.getDate() == null ? LocalDateTime.MIN : right.getDate();
            return dr.compareTo(dl);
        });
        return recquisitions;
    }

    private Recquisition latestProductRecquisition(Produit product, String region) {
        List<Recquisition> recquisitions = sortedProductRecquisitions(product, region);
        return recquisitions.isEmpty() ? null : recquisitions.get(0);
    }

    private Livraison findDeliveryForRecquisition(Recquisition recquisition) {
        if (recquisition == null || recquisition.getReference() == null || recquisition.getReference().isBlank()) {
            return null;
        }
        List<Livraison> deliveries = LivraisonDelegate.findByRef(recquisition.getReference());
        return deliveries == null || deliveries.isEmpty() ? null : deliveries.get(0);
    }

    private String formatMeasuresForDiagnostic(List<Mesure> measures) {
        if (measures == null || measures.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(" (");
        for (int i = 0; i < measures.size(); i++) {
            Mesure measure = measures.get(i);
            if (i > 0) {
                builder.append("; ");
            }
            builder.append(safe(measure.getDescription(), "-"))
                    .append(", quantContenu=")
                    .append(measure.getQuantContenu());
        }
        return builder.append(")").toString();
    }

    private String formatRecquisitionForDiagnostic(Recquisition recquisition) {
        if (recquisition == null) {
            return "aucune";
        }
        return recquisition.getUid()
                + ", ref=" + safe(recquisition.getReference(), "-")
                + ", date=" + recquisition.getDate()
                + ", lot=" + safe(recquisition.getNumlot(), "-")
                + ", quantite=" + recquisition.getQuantite()
                + ", coutAchat=" + recquisition.getCoutAchat();
    }

    private String formatDeliveryForDiagnostic(Livraison delivery) {
        if (delivery == null) {
            return "aucune";
        }
        String supplier = delivery.getFournId() == null ? "-" : safe(delivery.getFournId().getNomFourn(), "-");
        return delivery.getReference()
                + ", numPiece=" + safe(delivery.getNumPiece(), "-")
                + ", date=" + delivery.getDateLivr()
                + ", fournisseur=" + supplier;
    }

    private String registerProductVisibilityRepair(String query, String productUid, String reason) {
        String batchId = "pos-visible-" + DataId.generate();
        PRODUCT_VISIBILITY_REPAIR_REQUESTS.put(batchId,
                new ProductVisibilityRepairRequest(batchId, query, productUid, reason, LocalDateTime.now()));
        return batchId;
    }

    private String productVisibilityReasonLabel(String reason) {
        return switch (safe(reason, "")) {
            case "PRODUIT_HORS_LISTE" ->
                "le produit n'apparait pas dans la liste catalogue chargée par le POS";
            case "MESURE_ABSENTE" ->
                "aucune mesure n'est liée au produit";
            case "MESURE_UNITAIRE_ABSENTE" ->
                "aucune mesure unitaire avec quantContenu=1 n'est liée au produit";
            case "RECQUISITION_ABSENTE" ->
                "aucun approvisionnement/réquisition n'est lié au produit";
            case "LIVRAISON_ABSENTE" ->
                "la réquisition existe mais sa livraison de référence est introuvable";
            default ->
                "une donnée du flow POS est absente";
        };
    }

    private String productVisibilityRepairInstructions(String batchId, String productName, String action) {
        return "\nAction proposée: " + action + "\n"
                + "Lot de correction: " + batchId + "\n\n"
                + "Pour confirmer, demandez à Gratien d'appliquer la correction avec ce JSON:\n"
                + "{\n"
                + "  \"productName\":\"" + tableCell(productName) + "\",\n"
                + "  \"quantity\":1,\n"
                + "  \"purchaseUnitPrice\":0,\n"
                + "  \"measureName\":\"Pièce\",\n"
                + "  \"lotNumber\":\"AUTO-POS\",\n"
                + "  \"expiryDate\":\"none\",\n"
                + "  \"salePrice\":0,\n"
                + "  \"qmin\":1,\n"
                + "  \"qmax\":999999,\n"
                + "  \"currency\":\"USD\"\n"
                + "}\n"
                + "Remplacez `salePrice` et `purchaseUnitPrice` par les valeurs réelles avant validation.";
    }

    private String productVisibilityPriceInstructions(String batchId, Produit product) {
        return "Lot de correction: " + batchId + "\n\n"
                + "Pour configurer le prix, confirmez avec un JSON du genre:\n"
                + "{\n"
                + "  \"productName\":\"" + tableCell(product == null ? "Produit" : product.getNomProduit()) + "\",\n"
                + "  \"salePrice\":25,\n"
                + "  \"qmin\":1,\n"
                + "  \"qmax\":999999,\n"
                + "  \"currency\":\"USD\",\n"
                + "  \"measureName\":\"Pièce\"\n"
                + "}";
    }

    private List<PrixDeVente> previousSalePrices(Produit product, String excludedRecquisitionUid) {
        if (product == null || product.getUid() == null) {
            return List.of();
        }
        List<Recquisition> recquisitions = sortedProductRecquisitions(product, pref.get("region", "Goma"));
        for (Recquisition recquisition : recquisitions) {
            if (recquisition == null || recquisition.getUid() == null
                    || Objects.equals(recquisition.getUid(), excludedRecquisitionUid)) {
                continue;
            }
            List<PrixDeVente> prices = safeList(PrixDeVenteDelegate.findPricesForRecq(recquisition.getUid()));
            if (!prices.isEmpty()) {
                return prices;
            }
        }
        return List.of();
    }

    private List<PrixDeVente> copySalePricesToRecquisition(List<PrixDeVente> sourcePrices, Recquisition target, Mesure fallbackMeasure) {
        List<PrixDeVente> copied = new ArrayList<>();
        if (target == null || sourcePrices == null) {
            return copied;
        }
        for (PrixDeVente source : sourcePrices) {
            if (source == null) {
                continue;
            }
            Mesure measure = source.getMesureId() == null ? fallbackMeasure : source.getMesureId();
            PrixDeVente existing = findExistingSalePrice(target, measure, source.getQmin(), source.getQmax());
            boolean isNew = existing == null;
            PrixDeVente copy = isNew ? new PrixDeVente(DataId.generate()) : existing;
            copy.setRecquisitionId(target);
            copy.setMesureId(measure);
            copy.setDevise(safe(source.getDevise(), "USD"));
            copy.setPrixUnitaire(source.getPrixUnitaire());
            copy.setQmin(source.getQmin());
            copy.setQmax(source.getQmax());
            copy.setPourcentParCunit(source.getPourcentParCunit());
            copied.add(saveSalePrice(copy, isNew));
        }
        return copied;
    }

    private Mesure ensureUnitMeasure(Produit product, String description) {
        Mesure unit = MesureDelegate.findByProduitAndQuant(product.getUid(), 1d);
        if (unit != null) {
            return unit;
        }
        Mesure measure = new Mesure(DataId.generate());
        measure.setProduitId(product);
        measure.setDescription(safe(description, "Pièce"));
        measure.setQuantContenu(1d);
        Mesure saved = MesureDelegate.saveMesure(measure);
        syncCreate(saved, Tables.MESURE);
        return saved;
    }

    private PrixDeVente createSalePriceFromRepair(Map<String, Object> values, Recquisition recquisition, Mesure measure, double salePrice) {
        double qmin = parseDouble(firstValue(values, "qmin", "salePriceQmin", "quantiteMin"), 1d);
        double qmax = parseDouble(firstValue(values, "qmax", "salePriceQmax", "quantiteMax"), 999999d);
        PrixDeVente price = findExistingSalePrice(recquisition, measure, qmin, qmax);
        boolean isNew = price == null;
        if (isNew) {
            price = new PrixDeVente(DataId.generate());
        }
        price.setRecquisitionId(recquisition);
        price.setMesureId(measure);
        price.setPrixUnitaire(salePrice);
        price.setQmin(qmin);
        price.setQmax(qmax);
        price.setDevise(safe(firstValue(values, "currency", "devise", "saleCurrency"), "USD"));
        price.setPourcentParCunit(recquisition.getCoutAchat() <= 0 || salePrice <= 0 ? 0d : (salePrice - recquisition.getCoutAchat()) / salePrice);
        return saveSalePrice(price, isNew);
    }

    private InvoiceDraft buildGenericSupplyDraftForVisibilityRepair(Map<String, Object> values, String productName) {
        String region = pref.get("region", "Goma");
        InvoiceDraft draft = new InvoiceDraft();
        Entreprise entreprise = currentEntreprise();
        draft.setSupplier(safe(entreprise.getNomEntreprise(), "Entreprise connectee"));
        draft.setSupplierAddress(safe(entreprise.getAdresse(), ""));
        draft.setSupplierPhone(safe(entreprise.getPhones(), ""));
        draft.setSupplierIdNat(safe(entreprise.getIdNat(), ""));
        draft.setSupplierTaxNumber(safe(entreprise.getNumeroImpot(), ""));
        draft.setReference(genericPosRecoveryReference(region));
        draft.setInvoiceDate(LocalDate.now().toString());
        draft.setCurrency(safe(firstValue(values, "currency", "devise", "saleCurrency"), "USD"));
        draft.setPayed(0d);
        draft.setReduction(0d);
        InvoiceLine line = new InvoiceLine();
        line.setProductName(productName);
        line.setCategory(safe(firstValue(values, "category", "categorie"), inferProductCategory(productName)));
        line.setQuantity(parseDouble(firstValue(values, "quantity", "quantite"), 1d));
        line.setPurchaseUnitPrice(parseDouble(firstValue(values, "purchaseUnitPrice", "coutAchat", "prixAchat"), 0d));
        line.setTotal(parseDouble(firstValue(values, "total"), line.getQuantity() * line.getPurchaseUnitPrice()));
        line.setMeasureName(safe(firstValue(values, "measureName", "mesure"), "Pièce"));
        line.setLotNumber(safe(firstValue(values, "lotNumber", "numlot", "lot"), "AUTO-POS"));
        line.setExpiryDate(safe(firstValue(values, "expiryDate", "dateExpiry"), "none"));
        line.setSalePrice(parseDouble(firstValue(values, "salePrice", "prixVente", "prix"), 0d));
        line.setSalePriceQmin(parseDouble(firstValue(values, "qmin", "salePriceQmin", "quantiteMin"), 1d));
        line.setSalePriceQmax(parseDouble(firstValue(values, "qmax", "salePriceQmax", "quantiteMax"), 999999d));
        line.setSaleCurrency(draft.getCurrency());
        draft.setLines(List.of(line));
        return draft;
    }

    private Fournisseur genericCompanySupplier() {
        Entreprise entreprise = currentEntreprise();
        Fournisseur supplier = FournisseurDelegate.findOrCreate(entreprise);
        if (supplier != null) {
            return supplier;
        }
        supplier = new Fournisseur(DataId.generate());
        supplier.setNomFourn(safe(entreprise.getNomEntreprise(), "Entreprise connectee"));
        supplier.setAdresse(safe(entreprise.getAdresse(), ""));
        supplier.setIdentification(safe(entreprise.getIdentification(), ""));
        supplier.setPhone(safe(entreprise.getPhones(), "N/A-" + supplier.getUid().substring(0, 8)));
        Fournisseur saved = FournisseurDelegate.saveFournisseur(supplier);
        syncCreate(saved, Tables.FOURNISSEUR);
        return saved;
    }

    private Livraison findOrCreateGenericPosRecoveryDelivery(String region) {
        String reference = genericPosRecoveryReference(region);
        Fournisseur supplier = genericCompanySupplier();
        List<Livraison> deliveries = LivraisonDelegate.findByRef(reference);
        if (deliveries != null) {
            for (Livraison delivery : deliveries) {
                if (delivery != null && sameSupplier(delivery.getFournId(), supplier)) {
                    return delivery;
                }
            }
        }
        Livraison delivery = new Livraison(DataId.generate());
        delivery.setDateLivr(LocalDate.now());
        delivery.setFournId(supplier);
        delivery.setLibelle("Approvisionnement générique pour visibilité POS");
        delivery.setNumPiece(reference);
        delivery.setObservation("Livraison générique réutilisée par Gratien pour les produits absents du POS");
        delivery.setReference(reference);
        delivery.setRegion(region);
        delivery.setReduction(0d);
        delivery.setTopay(0d);
        delivery.setPayed(0d);
        delivery.setRemained(0d);
        delivery.setToreceive(0d);
        Livraison saved = LivraisonDelegate.saveLivraison(delivery);
        syncCreate(saved, Tables.LIVRAISON);
        return saved;
    }

    private String genericPosRecoveryReference(String region) {
        return "APPRO-GENERIQUE-POS-" + safe(region, "Goma").trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-");
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
        String numero = "Gratien-CAISSE-" + safe(region, "Goma").toUpperCase(Locale.ROOT);
        List<CompteTresor> found = CompteTresorDelegate.findByNumeroCompte(numero);
        if (found != null && !found.isEmpty()) {
            return found.get(0);
        }
        CompteTresor account = new CompteTresor(DataId.generate());
        account.setIntitule("Caisse Gratien");
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
        String accountName = safe(draft == null ? null : draft.getAccountName(), type.equals(TypeTraisorerie.CAISSE.name()) ? "Caisse Gratien" : type);
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
        String numero = "Gratien-" + type + "-" + safe(region, "Goma").toUpperCase(Locale.ROOT);
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
        // If measure name not specified, choose the smallest measure (quantContenu smallest)
        List<Mesure> allMeasures = MesureDelegate.findMesureByProduit(product.getUid());
        if (allMeasures != null && !allMeasures.isEmpty()) {
            Mesure smallest = null;
            for (Mesure m : allMeasures) {
                if (smallest == null || m.getQuantContenu() < smallest.getQuantContenu()) {
                    smallest = m;
                }
            }
            if (smallest != null) {
                return smallest;
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
        String supplierName = safe(draft.getSupplier(), "Fournisseur facture Gratien");
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
        delivery.setLibelle("Facture fournisseur via Gratien");
        delivery.setNumPiece(reference);
        delivery.setObservation("Facture lue par Gratien");
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
            supplier = findExistingSupplierByName(safe(context.draft.getSupplier(), "Fournisseur facture Gratien"));
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

               Vous pouvez envoyer plusieurs lignes a la fois. Si un prix de vente n'est pas rentable, Gratien vous le signalera avant l'enregistrement.
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

    private PrixDeVente createExplicitSalePrice(Recquisition recquisition, Mesure unit, InvoiceLine line, String currency) {
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
        return saveSalePrice(price, isNew);
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

    private void writeSheet(XSSFWorkbook workbook, String name, List<FinancialStatementRow> rows,
            List<String> periodHeaders) {
        var sheet = workbook.createSheet(name);
        boolean includeImmobilisationColumns = rows.stream().anyMatch(line -> line.getGrossAmount() != null
                || line.getAmortizationAmount() != null || line.getNetAmount() != null);
        List<String> headers = periodHeaders == null || periodHeaders.isEmpty()
                ? List.of("Période", "Période précédente 1", "Période précédente 2")
                : periodHeaders;
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Code");
        header.createCell(1).setCellValue("Rubrique");
        header.createCell(2).setCellValue("Nature");
        for (int i = 0; i < headers.size(); i++) {
            header.createCell(3 + i).setCellValue(headers.get(i));
        }
        int immobilisationStartColumn = 3 + headers.size();
        if (includeImmobilisationColumns) {
            header.createCell(immobilisationStartColumn).setCellValue("Valeur brute immobilisation");
            header.createCell(immobilisationStartColumn + 1).setCellValue("Amortissement");
            header.createCell(immobilisationStartColumn + 2).setCellValue("Valeur nette immobilisation");
        }
        int rowIndex = 1;
        for (FinancialStatementRow line : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(line.getCode());
            row.createCell(1).setCellValue(line.getRubrique());
            row.createCell(2).setCellValue(line.getNature());
            List<Double> values = List.of(
                    value(line.getAmountN()),
                    value(line.getAmountN1()),
                    value(line.getAmountN2()),
                    value(line.getAmountN3()),
                    value(line.getAmountN4()));
            for (int i = 0; i < headers.size() && i < values.size(); i++) {
                row.createCell(3 + i).setCellValue(values.get(i));
            }
            if (includeImmobilisationColumns) {
                row.createCell(immobilisationStartColumn).setCellValue(value(line.getGrossAmount()));
                row.createCell(immobilisationStartColumn + 1).setCellValue(value(line.getAmortizationAmount()));
                row.createCell(immobilisationStartColumn + 2).setCellValue(value(line.getNetAmount()));
            }
        }
        int columnCount = 3 + headers.size() + (includeImmobilisationColumns ? 3 : 0);
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private FinancialReportPayload loadFinancialPayload(String mode, String statement, String start, String end,
            int anchorYear, int years, String region) {
        String usedMode = safe(mode, "periode").trim().toLowerCase(Locale.ROOT);
        String usedRegion = resolveFinancialRegion(region);
        int span = years <= 3 ? 3 : 5;
        int resolvedYear = anchorYear <= 0 ? LocalDate.now().getYear() : anchorYear;
        String wanted = safe(statement, "tous").trim().toLowerCase(Locale.ROOT);
        List<FinancialStatementRow> bilan = List.of();
        List<FinancialStatementRow> compte = List.of();
        List<FinancialStatementRow> flux = List.of();
        List<String> headers;
        String periodLabel;

        if (usedMode.contains("ann")) {
            financialService.ensureYearlyStatements(resolvedYear, span, usedRegion);
            headers = yearlyHeaders(resolvedYear, span);
            periodLabel = (resolvedYear - span + 1) + " à " + resolvedYear;
            if (wantsStatement(wanted, "bilan")) {
                bilan = financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_BILAN,
                        resolvedYear, span, usedRegion);
            }
            if (wantsStatement(wanted, "resultat")) {
                compte = financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT,
                        resolvedYear, span, usedRegion);
            }
            if (wantsStatement(wanted, "flux")) {
                flux = financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE,
                        resolvedYear, span, usedRegion);
            }
            return new FinancialReportPayload(headers, bilan, compte, flux, usedRegion, periodLabel);
        }

        if (usedMode.contains("trim")) {
            LocalDate anchorDate = LocalDate.of(resolvedYear, 12, 31);
            financialService.ensureQuarterlyStatements(anchorDate, 4, usedRegion);
            headers = quarterlyHeaders(resolvedYear);
            periodLabel = "T1 à T4 " + resolvedYear;
            if (wantsStatement(wanted, "bilan")) {
                bilan = financialService.loadStatementRowsQuarterly(FinancialStatementAgregateService.STATEMENT_BILAN,
                        anchorDate, 4, usedRegion);
            }
            if (wantsStatement(wanted, "resultat")) {
                compte = financialService.loadStatementRowsQuarterly(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT,
                        anchorDate, 4, usedRegion);
            }
            if (wantsStatement(wanted, "flux")) {
                flux = financialService.loadStatementRowsQuarterly(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE,
                        anchorDate, 4, usedRegion);
            }
            return new FinancialReportPayload(headers, bilan, compte, flux, usedRegion, periodLabel);
        }

        LocalDate d1 = start == null || start.isBlank() ? LocalDate.now().withDayOfMonth(1) : LocalDate.parse(start);
        LocalDate d2 = end == null || end.isBlank() ? LocalDate.now() : LocalDate.parse(end);
        if (d1.isAfter(d2)) {
            LocalDate tmp = d1;
            d1 = d2;
            d2 = tmp;
        }
        financialService.rebuildStatements(d1, d2, usedRegion);
        headers = List.of("Période", "Période précédente 1", "Période précédente 2", "Période précédente 3");
        periodLabel = d1 + " au " + d2;
        if (wantsStatement(wanted, "bilan")) {
            bilan = financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_BILAN, d1, d2, usedRegion);
        }
        if (wantsStatement(wanted, "resultat")) {
            compte = financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_COMPTE_RESULTAT, d1, d2, usedRegion);
        }
        if (wantsStatement(wanted, "flux")) {
            flux = financialService.loadStatementRows(FinancialStatementAgregateService.STATEMENT_FLUX_TRESORERIE, d1, d2, usedRegion);
        }
        return new FinancialReportPayload(headers, bilan, compte, flux, usedRegion, periodLabel);
    }

    private boolean wantsStatement(String requested, String statement) {
        String value = safe(requested, "tous").toLowerCase(Locale.ROOT);
        if (value.contains("tous") || value.contains("tout") || value.contains("all")) {
            return true;
        }
        return switch (statement) {
            case "bilan" -> value.contains("bilan");
            case "resultat" -> value.contains("result") || value.contains("résultat") || value.contains("cr");
            case "flux" -> value.contains("flux") || value.contains("tresor") || value.contains("trésor");
            default -> false;
        };
    }

    private void appendMarkdownTable(StringBuilder builder, String title, List<String> headers,
            List<FinancialStatementRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<String> usedHeaders = headers == null || headers.isEmpty() ? List.of("Période") : headers;
        builder.append("### ").append(title).append("\n\n");
        builder.append("| Code | Rubrique |");
        for (String header : usedHeaders) {
            builder.append(' ').append(header).append(" |");
        }
        builder.append("\n|---|---|");
        for (int i = 0; i < usedHeaders.size(); i++) {
            builder.append("---:|");
        }
        builder.append('\n');
        for (FinancialStatementRow row : rows) {
            builder.append("| ").append(markdownCell(row.getCode()))
                    .append(" | ").append(markdownCell(row.getRubrique())).append(" |");
            List<Double> values = List.of(value(row.getAmountN()), value(row.getAmountN1()), value(row.getAmountN2()),
                    value(row.getAmountN3()), value(row.getAmountN4()));
            for (int i = 0; i < usedHeaders.size() && i < values.size(); i++) {
                builder.append(' ').append(Util.toPlain(values.get(i))).append(" |");
            }
            builder.append('\n');
        }
        builder.append('\n');
    }

    private String markdownCell(String value) {
        return safe(value, "-").replace("|", "/").replace('\n', ' ').trim();
    }

    private File exportCombinedFinancialPdf(Entreprise entreprise, LocalDate start, LocalDate end, List<String> headers,
            List<FinancialStatementRow> bilan, List<FinancialStatementRow> compte, List<FinancialStatementRow> flux,
            String businessKey) throws IOException {
        File output = FileUtils.pointFile("financial-statements-" + safeFilePart(businessKey) + ".pdf");
        if (output.exists() && !output.delete()) {
            throw new IOException("Impossible de remplacer le fichier PDF existant: " + output.getAbsolutePath());
        }
        List<File> sources = new ArrayList<>();
        sources.add(FinancialStatementPdfExporter.export(entreprise, "Bilan Comptable Financier", start, end, bilan, headers));
        sources.add(FinancialStatementPdfExporter.export(entreprise, "Compte de Résultat Standard", start, end, compte, headers));
        sources.add(FinancialStatementPdfExporter.export(entreprise, "Tableau de Flux de Trésorerie", start, end, flux, headers));
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(output.getAbsolutePath());
        for (File source : sources) {
            merger.addSource(source);
        }
        merger.mergeDocuments(null);
        for (File source : sources) {
            if (source != null && source.exists() && !source.equals(output)) {
                source.delete();
            }
        }
        return output;
    }

    private File generateLowStockPdf(Entreprise entreprise, List<Rupture> ruptures) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        int pageW = (int) PDRectangle.A4.getWidth();
        int pageH = (int) PDRectangle.A4.getHeight();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDFUtils pdf = new PDFUtils(document, contentStream);
        PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        java.awt.Color primary = new java.awt.Color(68, 206, 245);
        java.awt.Color egray = new java.awt.Color(218, 218, 219);

        String entName = entreprise != null ? safe(entreprise.getNomEntreprise(), "Kazisafe") : "Kazisafe";
        pdf.addTextLine(entName, 25, pageH - 38, hbold, 18, java.awt.Color.BLACK);
        pdf.addTextLine("Liste des produits en rupture de stock", 25, pageH - 65, hbold, 14, java.awt.Color.DARK_GRAY);
        String dateStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        pdf.addTextLine("Date : " + dateStr, 25, pageH - 85, hnormal, 10, java.awt.Color.DARK_GRAY);
        contentStream.setStrokingColor(primary);
        contentStream.setLineWidth(2);
        contentStream.moveTo(25, pageH - 95);
        contentStream.lineTo(pageW - 25, pageH - 95);
        contentStream.stroke();
        int[] tableCols = {40, 230, 80, 65, 85};
        pdf.addTable(tableCols, 25, 25, pageH - 130);
        pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
        pdf.setRightAlignedColumns(new int[]{2, 3});
        pdf.addCell("N#", primary);
        pdf.addCell("Designation", primary);
        pdf.addCell("Quantite", primary);
        pdf.addCell("P.U.", primary);
        pdf.addCell("Localisation", primary);
        pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
        int i = 0;
        int rowCount = 0;
        for (Rupture r : ruptures) {
            if (r == null || r.getProduit() == null) continue;
            i++;
            rowCount++;
            if (rowCount > 25) {
                contentStream.close();
                PDPage nextPage = new PDPage(PDRectangle.A4);
                document.addPage(nextPage);
                contentStream = new PDPageContentStream(document, nextPage);
                pdf = new PDFUtils(document, contentStream);
                pdf.addTable(tableCols, 25, 25, pageH - 68);
                pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                pdf.setRightAlignedColumns(new int[]{2, 3});
                rowCount = 1;
            }
            Produit p = r.getProduit();
            String designation = safe(p.getNomProduit(), "-")
                    + (p.getMarque() != null ? " " + p.getMarque() : "")
                    + (p.getModele() != null ? " " + p.getModele() : "");
            String quantStr = String.valueOf(r.getQuant()) + " " + (r.getMesure() != null ? r.getMesure().getDescription() : "");
            String puStr = String.valueOf(r.getUnitprice());
            pdf.setRightAlignedColumns(new int[]{2, 3});
            pdf.addCell(i + ".", egray);
            pdf.addCell(designation, egray);
            pdf.addCell(quantStr, egray);
            pdf.addCell(puStr, egray);
            pdf.addCell(safe(r.getLocalisation(), "-"), egray);
        }
        contentStream.close();
        File output = FileUtils.pointFile("rupture-stock-" + System.currentTimeMillis() + ".pdf");
        document.save(output);
        document.close();
        return output;
    }

    private File generateExpiredPdf(Entreprise entreprise, List<Peremption> expired, int months) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        int pageW = (int) PDRectangle.A4.getWidth();
        int pageH = (int) PDRectangle.A4.getHeight();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDFUtils pdf = new PDFUtils(document, contentStream);
        PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        java.awt.Color primary = new java.awt.Color(68, 206, 245);
        java.awt.Color egray = new java.awt.Color(218, 218, 219);

        String entName = entreprise != null ? safe(entreprise.getNomEntreprise(), "Kazisafe") : "Kazisafe";
        pdf.addTextLine(entName, 25, pageH - 38, hbold, 18, java.awt.Color.BLACK);
        String title = months > 0
                ? "Produits expirant dans les " + months + " mois"
                : "Produits deja expires";
        pdf.addTextLine(title, 25, pageH - 65, hbold, 14, java.awt.Color.DARK_GRAY);
        String dateStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        pdf.addTextLine("Date : " + dateStr, 25, pageH - 85, hnormal, 10, java.awt.Color.DARK_GRAY);
        contentStream.setStrokingColor(primary);
        contentStream.setLineWidth(2);
        contentStream.moveTo(25, pageH - 95);
        contentStream.lineTo(pageW - 25, pageH - 95);
        contentStream.stroke();
        int[] tableCols = {30, 180, 70, 55, 80, 65};
        pdf.addTable(tableCols, 25, 25, pageH - 130);
        pdf.setFont(hnormal, 9, java.awt.Color.WHITE);
        pdf.setRightAlignedColumns(new int[]{3, 5});
        pdf.addCell("N#", primary);
        pdf.addCell("Produit", primary);
        pdf.addCell("Lot", primary);
        pdf.addCell("Quantite", primary);
        pdf.addCell("Date exp.", primary);
        pdf.addCell("Valeur", primary);
        pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
        int i = 0;
        int rowCount = 0;
        for (Peremption p : expired) {
            if (p == null || p.getQuantite() <= 0) continue;
            i++;
            rowCount++;
            if (rowCount > 25) {
                contentStream.close();
                PDPage nextPage = new PDPage(PDRectangle.A4);
                document.addPage(nextPage);
                contentStream = new PDPageContentStream(document, nextPage);
                pdf = new PDFUtils(document, contentStream);
                pdf.addTable(tableCols, 25, 25, pageH - 68);
                pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                pdf.setRightAlignedColumns(new int[]{3, 5});
                rowCount = 1;
            }
            pdf.setRightAlignedColumns(new int[]{3, 5});
            pdf.addCell(i + ".", egray);
            pdf.addCell(safe(p.getProduit(), "-"), egray);
            pdf.addCell(safe(p.getLot(), "-"), egray);
            pdf.addCell(String.valueOf(p.getQuantite()) + " " + safe(p.getMesure(), ""), egray);
            pdf.addCell(p.getDateExpiry() != null ? p.getDateExpiry().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-", egray);
            pdf.addCell(String.valueOf(p.getValeur()), egray);
        }
        contentStream.close();
        File output = FileUtils.pointFile("produits-expires-" + System.currentTimeMillis() + ".pdf");
        document.save(output);
        document.close();
        return output;
    }

    private String resolvePurchaseReportType(String rapport) {
        String r = safe(rapport, "").toLowerCase(Locale.ROOT).trim();
        if (r.contains("fourn")) {
            return "fournisseur";
        }
        if (r.contains("mois") || r.contains("mens") || r.contains("month")) {
            return "mois";
        }
        return "produit";
    }

    private String resolvePurchaseSource(String source) {
        String s = safe(source, "").toLowerCase(Locale.ROOT).trim();
        if (s.contains("pdv") || s.contains("point") || s.contains("vente") || s.contains("recq") || s.contains("réq")) {
            return "pdv";
        }
        return "depot";
    }

    private String purchaseTypeLabel(String type) {
        return "fournisseur".equals(type) ? "par fournisseur" : "mois".equals(type) ? "par mois" : "par produit";
    }

    private String purchaseSourceLabel(String type, String source) {
        return "produit".equals(type) ? (" (source: " + ("pdv".equals(source) ? "Point de vente" : "Dépôt") + ")") : "";
    }

    private LocalDate[] resolveReportPeriod(String start, String end) {
        LocalDate d1;
        LocalDate d2;
        try {
            d1 = start == null || start.isBlank() ? LocalDate.now().withDayOfYear(1) : LocalDate.parse(start);
            d2 = end == null || end.isBlank() ? LocalDate.now() : LocalDate.parse(end);
        } catch (Exception ex) {
            d1 = LocalDate.now().withDayOfYear(1);
            d2 = LocalDate.now();
        }
        if (d1.isAfter(d2)) {
            LocalDate tmp = d1;
            d1 = d2;
            d2 = tmp;
        }
        return new LocalDate[]{d1, d2};
    }

    private String resolveReportRegion(String requestedRegion) {
        String currentRegion = pref.get("region", null);
        String requested = requestedRegion == null || requestedRegion.isBlank() ? null : requestedRegion.trim();
        if (requested == null || requested.isBlank()) {
            return "%";
        }
        String role = pref.get("priv", "");
        boolean globalAccess = role != null && (role.equals("Trader") || role.contains("ALL_ACCESS"));
        if (!globalAccess && currentRegion != null && !currentRegion.isBlank()
                && !requested.equalsIgnoreCase(currentRegion)) {
            throw new IllegalArgumentException("Votre rôle ne permet pas d'accéder au rapport de la région " + requested + ".");
        }
        return requested;
    }

    private String purchaseAmount(double value) {
        double v = Math.round(value * 100d) / 100d;
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private File companyLogoFile() {
        String eUid = pref.get("eUid", "");
        if (eUid == null || eUid.isBlank()) {
            return null;
        }
        File png = FileUtils.pointFile(eUid + ".png");
        if (png.exists() && png.length() > 0) {
            return png;
        }
        File jpeg = FileUtils.pointFile(eUid + ".jpeg");
        if (jpeg.exists() && jpeg.length() > 0) {
            return jpeg;
        }
        return null;
    }

    private String reportLogoMarkdown() {
        try {
            File logo = companyLogoFile();
            if (logo == null) {
                return "";
            }
            byte[] bytes = Files.readAllBytes(logo.toPath());
            if (bytes == null || bytes.length == 0) {
                return "";
            }
            return "![logo](data:image/png;base64," + Base64.getEncoder().encodeToString(bytes) + ")";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String reportUserName() {
        String name = pref.get("operator", null);
        if (name == null || name.isBlank()) {
            name = pref.get("uname", "Utilisateur");
        }
        return name;
    }

    private String reportDateLabel() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String reportRegionLabel(String region) {
        return (region == null || region.equals("%")) ? "Toutes les succursales" : region;
    }

    private String reportIdentityLine(Entreprise entreprise) {
        String rccm = entreprise != null ? safe(entreprise.getIdentification(), "") : "";
        String nif = entreprise != null ? safe(entreprise.getNumeroImpot(), "") : "";
        String idnat = entreprise != null ? safe(entreprise.getIdNat(), "") : "";
        List<String> parts = new ArrayList<>();
        if (!rccm.isBlank()) {
            parts.add("RCCM : " + markdownCell(rccm));
        }
        if (!nif.isBlank()) {
            parts.add("NIF : " + markdownCell(nif));
        }
        if (!idnat.isBlank()) {
            parts.add("ID-NAT : " + markdownCell(idnat));
        }
        return String.join("    ", parts);
    }

    private void appendReportHeader(StringBuilder builder, Entreprise entreprise, String title,
            String region, LocalDate d1, LocalDate d2) {
        String logo = reportLogoMarkdown();
        String entName = entreprise != null ? safe(entreprise.getNomEntreprise(), "Kazisafe") : "Kazisafe";
        if (!logo.isBlank()) {
            builder.append(logo).append(" ");
        }
        builder.append("**").append(markdownCell(entName)).append("**\n");
        String identity = reportIdentityLine(entreprise);
        if (!identity.isBlank()) {
            builder.append(identity).append("\n");
        }
        builder.append("Région : ").append(reportRegionLabel(region)).append("\n");
        if (d1 != null && d2 != null) {
            builder.append("Période : du ").append(d1).append(" au ").append(d2).append("\n");
        }
        builder.append("\n**").append(markdownCell(title)).append("**\n\n");
    }

    private void appendReportFooter(StringBuilder builder) {
        builder.append("\nÉtabli par : ").append(markdownCell(reportUserName()))
                .append("  |  Le : ").append(reportDateLabel()).append("\n\n");
        builder.append("Signature : ______________________");
    }

    private void drawReportPdfHeader(PDDocument document, PDPageContentStream contentStream, PDFUtils pdf,
            Entreprise entreprise, String title, String region, LocalDate d1, LocalDate d2,
            PDFont hbold, PDFont hnormal, java.awt.Color primary, int pageW, int pageH) throws IOException {
        File logoFile = companyLogoFile();
        if (logoFile != null) {
            try {
                PDImageXObject logo = PDImageXObject.createFromFile(logoFile.getPath(), document);
                contentStream.drawImage(logo, pageW - 110, pageH - 105, 80, 80);
            } catch (Exception ignored) {
            }
        }
        String entName = entreprise != null ? safe(entreprise.getNomEntreprise(), "Kazisafe") : "Kazisafe";
        pdf.addTextLine(entName, 25, pageH - 38, hbold, 18, java.awt.Color.BLACK);
        String identity = reportIdentityLine(entreprise);
        if (!identity.isBlank()) {
            pdf.addTextLine(identity, 25, pageH - 60, hnormal, 10, java.awt.Color.DARK_GRAY);
        }
        pdf.addTextLine("Région : " + reportRegionLabel(region) + "   -   Période : du " + d1 + " au " + d2,
                25, pageH - 78, hnormal, 10, java.awt.Color.DARK_GRAY);
        pdf.addTextLine(title, 25, pageH - 98, hbold, 14, java.awt.Color.DARK_GRAY);
        contentStream.setStrokingColor(primary);
        contentStream.setLineWidth(2);
        contentStream.moveTo(25, pageH - 112);
        contentStream.lineTo(pageW - 25, pageH - 112);
        contentStream.stroke();
    }

    private void drawReportPdfFooter(PDPageContentStream contentStream, PDFUtils pdf, PDFont hnormal,
            java.awt.Color primary, int pageW, int pageH) throws IOException {
        String generated = "Établi par : " + reportUserName() + "    le " + reportDateLabel();
        pdf.addTextLine(generated, 25, 55, hnormal, 10, java.awt.Color.DARK_GRAY);
        contentStream.setStrokingColor(primary);
        contentStream.setLineWidth(1);
        contentStream.moveTo(25, 42);
        contentStream.lineTo(300, 42);
        contentStream.stroke();
        pdf.addTextLine("Signature :", 25, 30, hnormal, 10, java.awt.Color.DARK_GRAY);
    }

    private void appendPurchaseReportMarkdown(StringBuilder builder, String type, String source,
            LocalDate d1, LocalDate d2, String region) {
        if ("fournisseur".equals(type)) {
            List<PurchaseBySupplier> items = RepportDelegate.findPurchasesBySupplier(d1, d2, region);
            builder.append("| Fournisseur | Adresse | Téléphone | Nb livraisons | Total achat |\n");
            builder.append("|---|---|---:|---:|---:|\n");
            for (PurchaseBySupplier p : items) {
                builder.append("| ").append(markdownCell(p.nom()))
                        .append(" | ").append(markdownCell(p.adresse()))
                        .append(" | ").append(markdownCell(p.phone()))
                        .append(" | ").append(p.nbLivraisons())
                        .append(" | ").append(purchaseAmount(p.montant()))
                        .append(" |\n");
            }
        } else if ("mois".equals(type)) {
            List<PurchaseByMonth> items = RepportDelegate.findPurchasesByMonth(d1, d2, region);
            builder.append("| Mois | Nb livraisons | Total achat |\n");
            builder.append("|---|---:|---:|\n");
            for (PurchaseByMonth p : items) {
                builder.append("| ").append(markdownCell(p.periode()))
                        .append(" | ").append(p.nbLivraisons())
                        .append(" | ").append(purchaseAmount(p.montant()))
                        .append(" |\n");
            }
        } else {
            List<PurchaseByProduct> items = "pdv".equals(source)
                    ? RepportDelegate.findRequisitionPurchasesByProduct(d1, d2, region)
                    : RepportDelegate.findPurchasesByProduct(d1, d2, region);
            builder.append("| Codebar | Produit | Quantité | Unité | Total achat |\n");
            builder.append("|---|---|---:|---:|---:|\n");
            for (PurchaseByProduct p : items) {
                builder.append("| ").append(markdownCell(p.codebar()))
                        .append(" | ").append(markdownCell(p.produit()))
                        .append(" | ").append(purchaseAmount(p.quantite()))
                        .append(" | ").append(markdownCell(p.unite()))
                        .append(" | ").append(purchaseAmount(p.montant()))
                        .append(" |\n");
            }
        }
    }

    private File exportPurchaseReportPdf(Entreprise entreprise, String type, String source,
            LocalDate d1, LocalDate d2, String region) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        int pageW = (int) PDRectangle.A4.getWidth();
        int pageH = (int) PDRectangle.A4.getHeight();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDFUtils pdf = new PDFUtils(document, contentStream);
        PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        java.awt.Color primary = new java.awt.Color(68, 206, 245);
        java.awt.Color egray = new java.awt.Color(218, 218, 219);

        String title = "Rapport des achats " + purchaseTypeLabel(type) + purchaseSourceLabel(type, source)
                + " - " + reportRegionLabel(region) + " du " + d1 + " au " + d2;
        drawReportPdfHeader(document, contentStream, pdf, entreprise, title, region, d1, d2,
                hbold, hnormal, primary, pageW, pageH);

        if ("fournisseur".equals(type)) {
            int[] tableCols = {120, 140, 90, 80, 100};
            pdf.addTable(tableCols, 25, 25, pageH - 145);
            pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{3, 4});
            pdf.addCell("Fournisseur", primary);
            pdf.addCell("Adresse", primary);
            pdf.addCell("Telephone", primary);
            pdf.addCell("Nb livraisons", primary);
            pdf.addCell("Total achat", primary);
            pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
            pdf.setRightAlignedColumns(new int[]{3, 4});
            for (PurchaseBySupplier p : RepportDelegate.findPurchasesBySupplier(d1, d2, region)) {
                pdf.addCell(markdownCell(p.nom()), egray);
                pdf.addCell(markdownCell(p.adresse()), egray);
                pdf.addCell(markdownCell(p.phone()), egray);
                pdf.addCell(String.valueOf(p.nbLivraisons()), egray);
                pdf.addCell(purchaseAmount(p.montant()), egray);
            }
        } else if ("mois".equals(type)) {
            int[] tableCols = {180, 140, 160};
            pdf.addTable(tableCols, 25, 25, pageH - 145);
            pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{1, 2});
            pdf.addCell("Mois", primary);
            pdf.addCell("Nb livraisons", primary);
            pdf.addCell("Total achat", primary);
            pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
            pdf.setRightAlignedColumns(new int[]{1, 2});
            for (PurchaseByMonth p : RepportDelegate.findPurchasesByMonth(d1, d2, region)) {
                pdf.addCell(markdownCell(p.periode()), egray);
                pdf.addCell(String.valueOf(p.nbLivraisons()), egray);
                pdf.addCell(purchaseAmount(p.montant()), egray);
            }
        } else {
            List<PurchaseByProduct> items = "pdv".equals(source)
                    ? RepportDelegate.findRequisitionPurchasesByProduct(d1, d2, region)
                    : RepportDelegate.findPurchasesByProduct(d1, d2, region);
            int[] tableCols = {90, 230, 70, 70, 100};
            pdf.addTable(tableCols, 25, 25, pageH - 145);
            pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{2, 4});
            pdf.addCell("Codebar", primary);
            pdf.addCell("Produit", primary);
            pdf.addCell("Quantite", primary);
            pdf.addCell("Unite", primary);
            pdf.addCell("Total achat", primary);
            pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
            pdf.setRightAlignedColumns(new int[]{2, 4});
            for (PurchaseByProduct p : items) {
                pdf.addCell(markdownCell(p.codebar()), egray);
                pdf.addCell(markdownCell(p.produit()), egray);
                pdf.addCell(purchaseAmount(p.quantite()), egray);
                pdf.addCell(markdownCell(p.unite()), egray);
                pdf.addCell(purchaseAmount(p.montant()), egray);
            }
        }

        drawReportPdfFooter(contentStream, pdf, hnormal, primary, pageW, pageH);
        contentStream.close();
        File output = FileUtils.pointFile("rapport-achats-" + safeFilePart(
                type + "-" + source + "-" + d1 + "-" + d2 + "-" + region) + ".pdf");
        document.save(output);
        document.close();
        return output;
    }

    private String resolveSalesReportType(String rapport) {
        String r = safe(rapport, "").toLowerCase(Locale.ROOT).trim();
        if (r.contains("categ")) {
            return "categorie";
        }
        if (r.contains("client")) {
            return "client";
        }
        return "produit";
    }

    private String salesTypeLabel(String type) {
        return "categorie".equals(type) ? "par catégorie" : "client".equals(type) ? "par client" : "par produit";
    }

    private void appendSalesReportMarkdown(StringBuilder builder, String type, LocalDate d1, LocalDate d2, String region) {
        if ("categorie".equals(type)) {
            List<SaleReport> items = RepportDelegate.findSaleReportPerCategory(d1, d2, region);
            builder.append("| Catégorie | Vente |\n");
            builder.append("|---|---:|\n");
            for (SaleReport s : items) {
                builder.append("| ").append(markdownCell(s.category()))
                        .append(" | ").append(purchaseAmount(s.vente()))
                        .append(" |\n");
            }
        } else if ("client".equals(type)) {
            List<VenteReporter> items = RepportDelegate.findReportSaleByClient(d1, d2, region, pref.get("mainCur", "USD"));
            builder.append("| Téléphone | Client | Catégorie | Vente |\n");
            builder.append("|---|---|---:|---:|\n");
            for (VenteReporter v : items) {
                Client c = v.getClient();
                Category cat = v.getCategory();
                builder.append("| ").append(markdownCell(c == null ? "-" : c.getPhone()))
                        .append(" | ").append(markdownCell(c == null ? "-" : c.getNomClient()))
                        .append(" | ").append(markdownCell(cat == null ? "-" : cat.getDescritption()))
                        .append(" | ").append(purchaseAmount(v.getChiffre()))
                        .append(" |\n");
            }
        } else {
            List<SaleReport> items = RepportDelegate.findSaleReportPerProduct(d1, d2, region);
            builder.append("| Codebar | Produit | Quantité | Unité | Vente | Marge |\n");
            builder.append("|---|---|---:|---:|---:|---:|\n");
            for (SaleReport s : items) {
                builder.append("| ").append(markdownCell(s.codebar()))
                        .append(" | ").append(markdownCell(s.produit()))
                        .append(" | ").append(purchaseAmount(s.quantite()))
                        .append(" | ").append(markdownCell(s.unite()))
                        .append(" | ").append(purchaseAmount(s.vente()))
                        .append(" | ").append(purchaseAmount(s.marge()))
                        .append(" |\n");
            }
        }
    }

    private void appendExpenseReportMarkdown(StringBuilder builder, LocalDate d1, LocalDate d2, String region) {
        List<ExpenseByImputation> items = RepportDelegate.findExpenseReportByImputation(d1, d2, region);
        builder.append("| Imputation | Montant USD | Montant CDF |\n");
        builder.append("|---|---:|---:|\n");
        for (ExpenseByImputation e : items) {
            builder.append("| ").append(markdownCell(e.imputation()))
                    .append(" | ").append(purchaseAmount(e.montantUsd()))
                    .append(" | ").append(purchaseAmount(e.montantCdf()))
                    .append(" |\n");
        }
        if (items.isEmpty()) {
            builder.append("| Aucune dépense sur cette période | - | - |\n");
        }
    }

    private File exportSalesReportExcel(String type, LocalDate d1, LocalDate d2, String region, String title)
            throws IOException {
        String filename = "rapport-ventes-" + safeFilePart(type + "-" + d1 + "-" + d2 + "-" + region) + ".xlsx";
        File file = FileUtils.pointFile(filename);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Ventes " + type);
            int rowid = 0;
            if (title != null && !title.isBlank()) {
                sheet.createRow(rowid++).createCell(0).setCellValue(title);
                rowid++;
            }
            List<String> headers;
            List<List<String>> rows = new ArrayList<>();
            if ("categorie".equals(type)) {
                headers = List.of("Catégorie", "Vente");
                for (SaleReport s : RepportDelegate.findSaleReportPerCategory(d1, d2, region)) {
                    rows.add(List.of(markdownCell(s.category()), purchaseAmount(s.vente())));
                }
            } else if ("client".equals(type)) {
                headers = List.of("Téléphone", "Client", "Catégorie", "Vente");
                for (VenteReporter v : RepportDelegate.findReportSaleByClient(d1, d2, region, pref.get("mainCur", "USD"))) {
                    Client c = v.getClient();
                    Category cat = v.getCategory();
                    rows.add(List.of(
                            markdownCell(c == null ? "-" : c.getPhone()),
                            markdownCell(c == null ? "-" : c.getNomClient()),
                            markdownCell(cat == null ? "-" : cat.getDescritption()),
                            purchaseAmount(v.getChiffre())));
                }
            } else {
                headers = List.of("Codebar", "Produit", "Quantité", "Unité", "Vente", "Marge");
                for (SaleReport s : RepportDelegate.findSaleReportPerProduct(d1, d2, region)) {
                    rows.add(List.of(markdownCell(s.codebar()), markdownCell(s.produit()),
                            purchaseAmount(s.quantite()), markdownCell(s.unite()),
                            purchaseAmount(s.vente()), purchaseAmount(s.marge())));
                }
            }
            Row header = sheet.createRow(rowid++);
            for (int c = 0; c < headers.size(); c++) {
                header.createCell(c).setCellValue(headers.get(c));
            }
            for (List<String> row : rows) {
                Row r = sheet.createRow(rowid++);
                for (int c = 0; c < row.size(); c++) {
                    r.createCell(c).setCellValue(row.get(c));
                }
            }
            workbook.write(out);
        }
        return file;
    }

    private File exportSalesReportPdf(Entreprise entreprise, String type, LocalDate d1, LocalDate d2, String region)
            throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        int pageW = (int) PDRectangle.A4.getWidth();
        int pageH = (int) PDRectangle.A4.getHeight();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDFUtils pdf = new PDFUtils(document, contentStream);
        PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        java.awt.Color primary = new java.awt.Color(68, 206, 245);
        java.awt.Color egray = new java.awt.Color(218, 218, 219);
        String title = "Rapport des ventes " + salesTypeLabel(type) + " - " + reportRegionLabel(region)
                + " du " + d1 + " au " + d2;
        drawReportPdfHeader(document, contentStream, pdf, entreprise, title, region, d1, d2,
                hbold, hnormal, primary, pageW, pageH);
        if ("categorie".equals(type)) {
            int[] tableCols = {300, 180};
            pdf.addTable(tableCols, 25, 25, pageH - 145);
            pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{1});
            pdf.addCell("Catégorie", primary);
            pdf.addCell("Vente", primary);
            pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
            pdf.setRightAlignedColumns(new int[]{1});
            for (SaleReport s : RepportDelegate.findSaleReportPerCategory(d1, d2, region)) {
                pdf.addCell(markdownCell(s.category()), egray);
                pdf.addCell(purchaseAmount(s.vente()), egray);
            }
        } else if ("client".equals(type)) {
            int[] tableCols = {120, 180, 160, 100};
            pdf.addTable(tableCols, 25, 25, pageH - 145);
            pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{3});
            pdf.addCell("Téléphone", primary);
            pdf.addCell("Client", primary);
            pdf.addCell("Catégorie", primary);
            pdf.addCell("Vente", primary);
            pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
            pdf.setRightAlignedColumns(new int[]{3});
            for (VenteReporter v : RepportDelegate.findReportSaleByClient(d1, d2, region, pref.get("mainCur", "USD"))) {
                Client c = v.getClient();
                Category cat = v.getCategory();
                pdf.addCell(markdownCell(c == null ? "-" : c.getPhone()), egray);
                pdf.addCell(markdownCell(c == null ? "-" : c.getNomClient()), egray);
                pdf.addCell(markdownCell(cat == null ? "-" : cat.getDescritption()), egray);
                pdf.addCell(purchaseAmount(v.getChiffre()), egray);
            }
        } else {
            int[] tableCols = {90, 230, 70, 70, 100, 100};
            pdf.addTable(tableCols, 25, 25, pageH - 145);
            pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
            pdf.setRightAlignedColumns(new int[]{2, 4, 5});
            pdf.addCell("Codebar", primary);
            pdf.addCell("Produit", primary);
            pdf.addCell("Quantite", primary);
            pdf.addCell("Unite", primary);
            pdf.addCell("Vente", primary);
            pdf.addCell("Marge", primary);
            pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
            pdf.setRightAlignedColumns(new int[]{2, 4, 5});
            for (SaleReport s : RepportDelegate.findSaleReportPerProduct(d1, d2, region)) {
                pdf.addCell(markdownCell(s.codebar()), egray);
                pdf.addCell(markdownCell(s.produit()), egray);
                pdf.addCell(purchaseAmount(s.quantite()), egray);
                pdf.addCell(markdownCell(s.unite()), egray);
                pdf.addCell(purchaseAmount(s.vente()), egray);
                pdf.addCell(purchaseAmount(s.marge()), egray);
            }
        }
        drawReportPdfFooter(contentStream, pdf, hnormal, primary, pageW, pageH);
        contentStream.close();
        File output = FileUtils.pointFile(
                "rapport-ventes-" + safeFilePart(type + "-" + d1 + "-" + d2 + "-" + region) + ".pdf");
        document.save(output);
        document.close();
        return output;
    }

    private File exportExpenseReportPdf(Entreprise entreprise, LocalDate d1, LocalDate d2, String region)
            throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        int pageW = (int) PDRectangle.A4.getWidth();
        int pageH = (int) PDRectangle.A4.getHeight();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDFUtils pdf = new PDFUtils(document, contentStream);
        PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        java.awt.Color primary = new java.awt.Color(68, 206, 245);
        java.awt.Color egray = new java.awt.Color(218, 218, 219);
        String title = "Rapport des dépenses par imputation - " + reportRegionLabel(region)
                + " du " + d1 + " au " + d2;
        drawReportPdfHeader(document, contentStream, pdf, entreprise, title, region, d1, d2,
                hbold, hnormal, primary, pageW, pageH);
        int[] tableCols = {320, 120, 120};
        pdf.addTable(tableCols, 25, 25, pageH - 145);
        pdf.setFont(hnormal, 10, java.awt.Color.WHITE);
        pdf.setRightAlignedColumns(new int[]{1, 2});
        pdf.addCell("Imputation", primary);
        pdf.addCell("Montant USD", primary);
        pdf.addCell("Montant CDF", primary);
        pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
        pdf.setRightAlignedColumns(new int[]{1, 2});
        for (ExpenseByImputation e : RepportDelegate.findExpenseReportByImputation(d1, d2, region)) {
            pdf.addCell(markdownCell(e.imputation()), egray);
            pdf.addCell(purchaseAmount(e.montantUsd()), egray);
            pdf.addCell(purchaseAmount(e.montantCdf()), egray);
        }
        drawReportPdfFooter(contentStream, pdf, hnormal, primary, pageW, pageH);
        contentStream.close();
        File output = FileUtils.pointFile(
                "rapport-depenses-" + safeFilePart(d1 + "-" + d2 + "-" + region) + ".pdf");
        document.save(output);
        document.close();
        return output;
    }

    private String resolveFinancialRegion(String requestedRegion) {        String currentRegion = pref.get("region", null);
        String requested = requestedRegion == null || requestedRegion.isBlank() ? currentRegion : requestedRegion.trim();
        if (requested == null || requested.isBlank()) {
            return "%";
        }
        String role = pref.get("priv", "");
        boolean globalAccess = role != null && (role.equals("Trader") || role.contains("ALL_ACCESS"));
        if (!globalAccess && currentRegion != null && !currentRegion.isBlank()
                && !requested.equalsIgnoreCase(currentRegion)) {
            throw new IllegalArgumentException("Votre rôle ne permet pas d'accéder au rapport de la région " + requested + ".");
        }
        return requested;
    }

    private String safeFilePart(String value) {
        return safe(value, "rapport").replaceAll("[^a-zA-Z0-9._-]+", "-").replaceAll("-+", "-");
    }

    private List<String> yearlyHeaders(int anchorYear, int span) {
        List<String> headers = new ArrayList<>();
        int normalizedSpan = span <= 3 ? 3 : 5;
        for (int year = anchorYear; year >= anchorYear - normalizedSpan + 1; year--) {
            headers.add(String.valueOf(year));
        }
        return headers;
    }

    private List<String> quarterlyHeaders(int year) {
        return List.of("T1 " + year, "T2 " + year, "T3 " + year, "T4 " + year);
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

    private String checkCurrencyConversionBeforeWorkflow(InvoiceDraft draft) {
        if (draft == null || !draft.hasLines()) return null;
        String draftCurrency = tools.CurrencyConverter.normalize(invoiceCurrency(draft));
        String mainCurrency = tools.CurrencyConverter.mainCurrency();
        if (draftCurrency.equals(mainCurrency)) return null;

        String dk = draftKey(draft);
        CurrencyConversionState state = PENDING_CURRENCY_CONVERSIONS.get(dk);

        if (state != null && state.resolved) {
            if (state.converted && state.convertedDraft != null) {
                applyConvertedDraft(draft, state.convertedDraft);
            }
            PENDING_CURRENCY_CONVERSIONS.remove(dk);
            return null;
        }

        if (state == null) {
            double defaultRate = tools.CurrencyConverter.rateFromUsd(draftCurrency);
            PENDING_CURRENCY_CONVERSIONS.put(dk, new CurrencyConversionState(
                    dk, draftCurrency, mainCurrency, defaultRate, false, false, 0, null, snapshotDraft(draft)
            ));
        }

        return "⚠️ **Conversion de devise détectée**\n\n"
                + "La facture est en **" + draftCurrency + "** mais la devise principale configurée est **" + mainCurrency + "**.\n\n"
                + "Souhaitez-vous convertir les prix en " + mainCurrency + " ?\n\n"
                + "- Répondez `oui` pour convertir avec le taux configuré (" + tools.CurrencyConverter.rateFromUsd(draftCurrency) + ")\n"
                + "- Répondez `oui taux=XXXX` pour convertir avec un taux personnalisé\n"
                + "- Répondez `non` pour ignorer et utiliser les montants originaux\n\n"
                + "Utilisez la commande : `answerInvoiceConversion(draftKey=\"" + dk + "\", answer=\"...\")`";
    }

    @Tool("Répondre à la demande de conversion de devise pour une facture fournisseur")
    public String answerInvoiceConversion(
            @P("Clé unique du brouillon de facture (draftKey)") String draftKey,
            @P("Réponse : oui, oui taux=XXXX, ou non") String answer) {
        CurrencyConversionState state = PENDING_CURRENCY_CONVERSIONS.get(normalizeToolKey(draftKey));
        if (state == null) {
            return "Aucune conversion en attente pour cette clé de facture. Vérifiez le draftKey.";
        }
        if (state.resolved) {
            return "Cette demande de conversion a déjà été traitée.";
        }

        String normalized = normalizeToolKey(answer);

        if (normalized.equals("non") || normalized.equals("no") || normalized.equals("skip")) {
            state.resolved = true;
            state.converted = false;
            return "✅ Conversion ignorée. Les montants originaux en **" + state.draftCurrency + "** seront utilisés.\n\n"
                    + "Vous pouvez maintenant rappeler `insertInvoiceSupply` avec la même facture pour continuer.";
        }

        if (normalized.startsWith("oui") || normalized.equals("yes") || normalized.equals("ok") || normalized.equals("confirme")) {
            double customRate = 0;
            double displayRate = state.defaultRate;
            if (normalized.contains("taux=")) {
                try {
                    String rateStr = normalized.substring(normalized.indexOf("taux=") + 5).trim();
                    customRate = Double.parseDouble(rateStr);
                    if (customRate <= 0) throw new NumberFormatException();
                    displayRate = customRate;
                } catch (NumberFormatException e) {
                    return "Taux invalide. Veuillez spécifier un nombre positif, ex: `oui taux=2500`.";
                }
            }

            state.resolved = true;
            state.converted = true;
            state.appliedRate = displayRate;
            state.convertedDraft = convertStoredDraft(state.originalDraft, state.mainCurrency, customRate);

            return "✅ Conversion acceptée avec un taux de 1 " + state.draftCurrency + " = " + displayRate + " " + state.mainCurrency + ".\n\n"
                    + "Utilisez `previewConvertedInvoice(draftKey=\"" + state.draftKey + "\")` pour voir l'aperçu de la facture convertie.\n"
                    + "Ensuite, rappelez `insertInvoiceSupply` avec la même facture pour enregistrer.";
        }

        return "Réponse non reconnue. Veuillez répondre par `oui`, `oui taux=XXXX` ou `non`.";
    }

    @Tool("Convertir explicitement une facture dans la devise principale et afficher l'aperçu. Utilisez ceci si vous savez déjà que la facture est en devise différente.")
    public String convertAndPreviewInvoice(
            @P("Brouillon facture à convertir") InvoiceDraft draft,
            @P("Taux de conversion (optionnel - laissez vide pour utiliser le taux configuré)") String customRate) {
        if (draft == null || !draft.hasLines()) {
            return "Facture invalide ou sans lignes.";
        }
        String draftCurrency = tools.CurrencyConverter.normalize(invoiceCurrency(draft));
        String mainCurrency = tools.CurrencyConverter.mainCurrency();
        if (draftCurrency.equals(mainCurrency)) {
            return "La facture est déjà en **" + mainCurrency + "**. Aucune conversion nécessaire.";
        }
        double customRateVal = 0;
        double displayRate = tools.CurrencyConverter.rateFromUsd(draftCurrency);
        if (customRate != null && !customRate.isBlank()) {
            try {
                customRateVal = Double.parseDouble(customRate.trim());
                if (customRateVal <= 0) throw new NumberFormatException();
                displayRate = customRateVal;
            } catch (NumberFormatException e) {
                return "Taux invalide. Veuillez entrer un nombre positif.";
            }
        }
        InvoiceDraft converted = convertStoredDraft(draft, mainCurrency, customRateVal);
        StringBuilder sb = new StringBuilder();
        sb.append("### Facture convertie en ").append(mainCurrency).append("\n\n");
        sb.append("Taux appliqué : 1 ").append(draftCurrency).append(" = ").append(displayRate).append(" ").append(mainCurrency).append("\n\n");
        sb.append("| Produit | Qté | Prix unitaire | Total | Prix vente |\n");
        sb.append("|---|---|---|---|---|\n");
        double grandTotal = 0;
        for (InvoiceLine line : converted.getLines()) {
            double total = tools.CurrencyConverter.round(line.getTotal());
            double salePx = line.getSalePrice() != null ? tools.CurrencyConverter.round(line.getSalePrice()) : 0;
            sb.append("| ").append(safe(line.getProductName(), "-"))
                    .append(" | ").append(line.getQuantity())
                    .append(" | ").append(tools.CurrencyConverter.round(line.getPurchaseUnitPrice()))
                    .append(" | ").append(total)
                    .append(" | ").append(salePx).append(" |\n");
            grandTotal += total;
        }
        sb.append("\n**Total : ").append(tools.CurrencyConverter.round(grandTotal))
                .append(" ").append(mainCurrency).append("**\n\n");
        sb.append("Si ce résultat vous convient, appelez `insertInvoiceSupply` avec les mêmes informations.");
        return sb.toString();
    }

    @Tool("Afficher l'aperçu de la facture convertie dans la devise principale avant enregistrement")
    public String previewConvertedInvoice(@P("Clé unique du brouillon de facture (draftKey)") String draftKey) {
        CurrencyConversionState state = PENDING_CURRENCY_CONVERSIONS.get(normalizeToolKey(draftKey));
        if (state == null) {
            return "Aucune conversion en attente pour cette clé.";
        }
        if (!state.resolved || !state.converted) {
            return "La conversion n'a pas encore été effectuée. Utilisez d'abord `answerInvoiceConversion`.";
        }
        if (state.convertedDraft == null) {
            return "Erreur : la facture convertie est introuvable. Utilisez à nouveau `answerInvoiceConversion`.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### Aperçu de la facture convertie en ").append(state.mainCurrency).append("\n\n");
        sb.append("| Produit | Qté | Prix unitaire (")
                .append(state.mainCurrency).append(") | Total (")
                .append(state.mainCurrency).append(") | Prix vente (")
                .append(state.mainCurrency).append(") |\n");
        sb.append("|---|---|---|---|---|\n");

        double grandTotal = 0;
        for (InvoiceLine line : state.convertedDraft.getLines()) {
            double unitPrice = tools.CurrencyConverter.round(line.getPurchaseUnitPrice());
            double total = tools.CurrencyConverter.round(line.getTotal());
            double salePx = line.getSalePrice() != null ? tools.CurrencyConverter.round(line.getSalePrice()) : 0;
            sb.append("| ").append(safe(line.getProductName(), "-"))
                    .append(" | ").append(line.getQuantity())
                    .append(" | ").append(unitPrice)
                    .append(" | ").append(total)
                    .append(" | ").append(salePx).append(" |\n");
            grandTotal += total;
        }

        sb.append("\n**Total converti : ").append(tools.CurrencyConverter.round(grandTotal))
                .append(" ").append(state.mainCurrency).append("**\n");
        sb.append("Taux appliqué : 1 ").append(state.draftCurrency)
                .append(" = ").append(state.appliedRate).append(" ").append(state.mainCurrency).append("\n\n");
        sb.append("Rappelez maintenant `insertInvoiceSupply` avec la même facture pour enregistrer.");
        return sb.toString();
    }

    private InvoiceDraft snapshotDraft(InvoiceDraft draft) {
        InvoiceDraft copy = new InvoiceDraft();
        copy.setSupplier(draft.getSupplier());
        copy.setSupplierIdNat(draft.getSupplierIdNat());
        copy.setSupplierRccm(draft.getSupplierRccm());
        copy.setSupplierTaxNumber(draft.getSupplierTaxNumber());
        copy.setSupplierAddress(draft.getSupplierAddress());
        copy.setSupplierPhone(draft.getSupplierPhone());
        copy.setReference(draft.getReference());
        copy.setInvoiceDate(draft.getInvoiceDate());
        copy.setCurrency(draft.getCurrency());
        copy.setPayed(draft.getPayed());
        copy.setReduction(draft.getReduction());
        List<InvoiceLine> linesCopy = new ArrayList<>();
        if (draft.getLines() != null) {
            for (InvoiceLine line : draft.getLines()) {
                InvoiceLine lc = new InvoiceLine();
                lc.setProductName(line.getProductName());
                lc.setCategory(line.getCategory());
                lc.setQuantity(line.getQuantity());
                lc.setPurchaseUnitPrice(line.getPurchaseUnitPrice());
                lc.setTotal(line.getTotal());
                lc.setSalePrice(line.getSalePrice());
                lc.setSalePriceQmin(line.getSalePriceQmin());
                lc.setSalePriceQmax(line.getSalePriceQmax());
                lc.setSaleCurrency(line.getSaleCurrency());
                lc.setMeasureName(line.getMeasureName());
                lc.setLotNumber(line.getLotNumber());
                lc.setExpiryDate(line.getExpiryDate());
                linesCopy.add(lc);
            }
        }
        copy.setLines(linesCopy);
        copy.setMissingSalePrices(new ArrayList<>(draft.getMissingSalePrices()));
        return copy;
    }

    private double convertAmount(double amount, String sourceCurrency, String targetCurrency, double customRate) {
        if (sourceCurrency.equals(targetCurrency)) return amount;
        double sourceToUsd;
        if (customRate > 0) {
            sourceToUsd = amount / customRate;
        } else {
            sourceToUsd = tools.CurrencyConverter.toUsd(amount, sourceCurrency);
        }
        return tools.CurrencyConverter.fromUsd(sourceToUsd, targetCurrency);
    }

    private InvoiceDraft convertStoredDraft(InvoiceDraft original, String targetCurrency, double customRate) {
        if (original == null) return null;
        String sourceCurrency = tools.CurrencyConverter.normalize(invoiceCurrency(original));
        InvoiceDraft converted = snapshotDraft(original);
        converted.setCurrency(targetCurrency);
        if (converted.getPayed() != null) {
            converted.setPayed(convertAmount(converted.getPayed(), sourceCurrency, targetCurrency, customRate));
        }
        if (converted.getReduction() != null) {
            converted.setReduction(convertAmount(converted.getReduction(), sourceCurrency, targetCurrency, customRate));
        }
        for (InvoiceLine line : converted.getLines()) {
            line.setPurchaseUnitPrice(convertAmount(line.getPurchaseUnitPrice(), sourceCurrency, targetCurrency, customRate));
            line.setTotal(convertAmount(line.getTotal(), sourceCurrency, targetCurrency, customRate));
            if (line.getSalePrice() != null) {
                line.setSalePrice(convertAmount(line.getSalePrice(), sourceCurrency, targetCurrency, customRate));
            }
            if (line.getSalePriceQmin() != null) {
                line.setSalePriceQmin(convertAmount(line.getSalePriceQmin(), sourceCurrency, targetCurrency, customRate));
            }
            if (line.getSalePriceQmax() != null) {
                line.setSalePriceQmax(convertAmount(line.getSalePriceQmax(), sourceCurrency, targetCurrency, customRate));
            }
            line.setSaleCurrency(targetCurrency);
        }
        return converted;
    }

    private void applyConvertedDraft(InvoiceDraft target, InvoiceDraft converted) {
        if (target == null || converted == null) return;
        target.setCurrency(converted.getCurrency());
        target.setPayed(converted.getPayed());
        target.setReduction(converted.getReduction());
        if (converted.getLines() != null && !converted.getLines().isEmpty()) {
            target.setLines(converted.getLines());
        }
    }

    private String invoiceReference(InvoiceDraft draft) {
        return draft == null || draft.getReference() == null || draft.getReference().isBlank()
                ? "LOT-" + System.currentTimeMillis()
                : draft.getReference();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
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

    private void cleanupStaleWorkflows() {
        long now = System.currentTimeMillis();
        INVOICE_WORKFLOWS.entrySet().removeIf(entry -> {
            InvoiceWorkflowContext context = entry.getValue();
            return !isActiveInvoiceWorkflow(context) || now - context.createdAtMs > ACTIVE_WORKFLOW_MAX_AGE_MS;
        });
        SALE_WORKFLOWS.entrySet().removeIf(entry -> {
            SaleWorkflowContext context = entry.getValue();
            return !isActiveSaleWorkflow(context) || now - context.createdAtMs > ACTIVE_WORKFLOW_MAX_AGE_MS;
        });
        EXPENSE_WORKFLOWS.entrySet().removeIf(entry -> {
            ExpenseWorkflowContext context = entry.getValue();
            return !isActiveExpenseWorkflow(context) || now - context.createdAtMs > ACTIVE_WORKFLOW_MAX_AGE_MS;
        });
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

    private record ProductVisibilityRepairRequest(String batchId, String query, String productUid, String reason, LocalDateTime createdAt) {
    }

    private static class CurrencyConversionState {
        private final String draftKey;
        private final String draftCurrency;
        private final String mainCurrency;
        private final double defaultRate;
        private boolean converted;
        private boolean resolved;
        private double appliedRate;
        private InvoiceDraft convertedDraft;
        private final InvoiceDraft originalDraft;

        private CurrencyConversionState(String draftKey, String draftCurrency, String mainCurrency,
                                         double defaultRate, boolean converted, boolean resolved,
                                         double appliedRate, InvoiceDraft convertedDraft,
                                         InvoiceDraft originalDraft) {
            this.draftKey = draftKey;
            this.draftCurrency = draftCurrency;
            this.mainCurrency = mainCurrency;
            this.defaultRate = defaultRate;
            this.converted = converted;
            this.resolved = resolved;
            this.appliedRate = appliedRate;
            this.convertedDraft = convertedDraft;
            this.originalDraft = originalDraft;
        }
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
                    ? "GRATIEN-" + System.currentTimeMillis()
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

    private static class NullFieldInfo {
        public final String entityType;
        public final String entityId;
        public final String fieldName;
        public final Class<?> fieldType;

        public NullFieldInfo(String entityType, String entityId, String fieldName, Class<?> fieldType) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }
    }

    private static class SyncFixWorkflowContext {
        private final String workflowId;
        private final long createdAtMs = System.currentTimeMillis();
        private final List<NullFieldInfo> nullFields = new ArrayList<>();
        private final Set<Integer> fixedFieldIndices = new HashSet<>();
        private final int itemsPerPage = 30;
        private int currentPage = 1;
        private boolean scanned = false;
        private boolean fixed = false;
        private boolean synced = false;
        private boolean cancelled = false;
        private String cancelMessage = "Workflow annulé.";

        private SyncFixWorkflowContext(String workflowId) {
            this.workflowId = workflowId;
        }

        private int getTotalPages() {
            if (nullFields.isEmpty()) return 1;
            return (int) Math.ceil((double) nullFields.size() / itemsPerPage);
        }

        private List<NullFieldInfo> getCurrentPageItems() {
            int fromIndex = (currentPage - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, nullFields.size());
            if (fromIndex >= nullFields.size()) {
                return new ArrayList<>();
            }
            return nullFields.subList(fromIndex, toIndex);
        }

        private String summary() {
            return "workflowId=" + workflowId
                    + ", scanned=" + scanned
                    + ", nullFieldsCount=" + nullFields.size()
                    + ", currentPage=" + currentPage
                    + ", totalPages=" + getTotalPages()
                    + ", fixed=" + fixed
                    + ", synced=" + synced
                    + ", cancelled=" + cancelled;
        }
    }

    private static final Map<String, SyncFixWorkflowContext> SYNC_FIX_WORKFLOWS = new ConcurrentHashMap<>();

    private void syncCreate(data.BaseModel model, Tables table) {
        try {
            Util.sync(model, Constants.ACTION_CREATE, table);
        } catch (Exception ex) {
            System.err.println("Gratien sync HTTP échouée pour " + table + ": " + ex.getMessage());
        }
    }

    private void syncUpdate(data.BaseModel model, Tables table) {
        try {
            Util.sync(model, Constants.ACTION_UPDATE, table);
        } catch (Exception ex) {
            System.err.println("Gratien sync HTTP update échouée pour " + table + ": " + ex.getMessage());
        }
    }

    private void syncDelete(data.BaseModel model, Tables table) {
        try {
            Util.sync(model, Constants.ACTION_DELETE, table);
        } catch (Exception ex) {
            System.err.println("Gratien sync HTTP delete échouée pour " + table + ": " + ex.getMessage());
        }
    }

    public static class NaturalLanguagePriceResult {
        public final int index; // 0-based
        public final double price;
        public final String currency;
        public final double qmin;
        public final double qmax;

        public NaturalLanguagePriceResult(int index, double price, String currency, double qmin, double qmax) {
            this.index = index;
            this.price = price;
            this.currency = currency;
            this.qmin = qmin;
            this.qmax = qmax;
        }
    }

    public static NaturalLanguagePriceResult parseNaturalLanguagePrice(String text) {
        if (text == null || text.isBlank()) return null;
        String normalized = text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        boolean hasFrenchWord = false;
        String[] frenchIndicators = {"c'est", "c est", "est", "produit", "premier", "deuxi", "troisi",
                "quatri", "cinqui", "sixi", "septi", "huiti", "neuvi", "dixi",
                "dollar", "euro", "franc", "euros", "dollars", "francs",
                "prix", "coûte", "coute", "à", "pour", "= ", "->"};
        for (String indicator : frenchIndicators) {
            if (normalized.contains(indicator)) {
                hasFrenchWord = true;
                break;
            }
        }
        if (!hasFrenchWord && !normalized.matches(".*\\d+.*")) {
            return null;
        }

        Map<String, Integer> ordinalMap = new java.util.HashMap<>();
        ordinalMap.put("premier", 1); ordinalMap.put("première", 1); ordinalMap.put("1er", 1); ordinalMap.put("1ère", 1);
        ordinalMap.put("deuxième", 2); ordinalMap.put("2ème", 2); ordinalMap.put("2e", 2); ordinalMap.put("2eme", 2);
        ordinalMap.put("troisième", 3); ordinalMap.put("3ème", 3); ordinalMap.put("3e", 3); ordinalMap.put("3eme", 3);
        ordinalMap.put("quatrième", 4); ordinalMap.put("4ème", 4); ordinalMap.put("4e", 4); ordinalMap.put("4eme", 4);
        ordinalMap.put("cinquième", 5); ordinalMap.put("5ème", 5); ordinalMap.put("5e", 5); ordinalMap.put("5eme", 5);
        ordinalMap.put("sixième", 6); ordinalMap.put("6ème", 6); ordinalMap.put("6e", 6); ordinalMap.put("6eme", 6);
        ordinalMap.put("septième", 7); ordinalMap.put("7ème", 7); ordinalMap.put("7e", 7); ordinalMap.put("7eme", 7);
        ordinalMap.put("huitième", 8); ordinalMap.put("8ème", 8); ordinalMap.put("8e", 8); ordinalMap.put("8eme", 8);
        ordinalMap.put("neuvième", 9); ordinalMap.put("9ème", 9); ordinalMap.put("9e", 9); ordinalMap.put("9eme", 9);
        ordinalMap.put("dixième", 10); ordinalMap.put("10ème", 10); ordinalMap.put("10e", 10); ordinalMap.put("10eme", 10);

        int idx = -1;
        for (Map.Entry<String, Integer> entry : ordinalMap.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                idx = entry.getValue() - 1;
                break;
            }
        }
        if (idx < 0) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("produit\\s*(\\d+)").matcher(normalized);
            if (m.find()) {
                idx = Integer.parseInt(m.group(1)) - 1;
            }
        }
        if (idx < 0) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:^|\\s)(\\d+)(?:\\s|$|er|ème|e)").matcher(normalized);
            if (m.find()) {
                idx = Integer.parseInt(m.group(1)) - 1;
            }
        }
        if (idx < 0) return null;

        String currency = null;
        String[] currencyPatterns = {"dollars?", "usd", "\\$", "francs?", "cdf", "fc", "euros?", "eur", "€"};
        for (String pat : currencyPatterns) {
            java.util.regex.Matcher cm = java.util.regex.Pattern.compile(pat).matcher(normalized);
            if (cm.find()) {
                String matched = cm.group();
                if (matched.equals("$")) currency = "USD";
                else if (matched.equals("€")) currency = "EUR";
                else if (matched.matches("dollars?|usd")) currency = "USD";
                else if (matched.matches("francs?|cdf|fc")) currency = "CDF";
                else if (matched.matches("euros?|eur")) currency = "EUR";
                break;
            }
        }
        if (currency == null) {
            currency = tools.CurrencyConverter.mainCurrency();
        }

        java.util.regex.Matcher priceM = java.util.regex.Pattern.compile(
                "(?:c'est\\s*|c est\\s*|est\\s*(?:de\\s*)?|(?:de\\s*)?|=\\s*|:?\\s*|à\\s*|->\\s*)?(\\d+(?:[.,]\\d+)?)\\s*"
        ).matcher(normalized);
        double price = -1;
        while (priceM.find()) {
            String val = priceM.group(1).replace(",", ".");
            double p = Double.parseDouble(val);
            if (p > price) price = p;
        }
        if (price < 0) {
            java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(normalized);
            double last = -1;
            while (m2.find()) {
                String val = m2.group(1).replace(",", ".");
                last = Double.parseDouble(val);
            }
            if (last > 0) price = last;
        }
        if (price < 0) return null;

        return new NaturalLanguagePriceResult(idx, price, currency, 0.001, 999999);
    }

    public record ProductAmbiguity(int lineIndex, String productName, List<String> candidates) {
        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("*").append(productName).append("*");
            sb.append(" a plusieurs correspondances:\n");
            for (int i = 0; i < candidates.size(); i++) {
                sb.append(i + 1).append(". ").append(candidates.get(i)).append("\n");
            }
            return sb.toString();
        }
    }

    public List<ProductAmbiguity> findAmbiguousProductNames(InvoiceDraft draft) {
        if (draft == null || draft.getLines() == null) return List.of();
        List<ProductAmbiguity> result = new ArrayList<>();
        for (int i = 0; i < draft.getLines().size(); i++) {
            String name = draft.getLines().get(i).getProductName();
            if (name == null || name.isBlank()) continue;
            List<Produit> exact = ProduitDelegate.findProduitByName(name.trim());
            if (exact != null && !exact.isEmpty()) continue;
            List<String> invoiceTokens = searchableProductTokens(name);
            if (invoiceTokens.isEmpty()) continue;
            List<Produit> all = ProduitDelegate.findProduits();
            if (all == null || all.isEmpty()) continue;
            List<String> candidates = new ArrayList<>();
            for (Produit p : all) {
                List<String> productTokens = searchableProductTokens(productSearchText(p));
                if (productTokens.isEmpty()) continue;
                int matched = countMatchedInvoiceTokens(invoiceTokens, productTokens);
                int minRequired = requiredInvoiceTokenMatches(invoiceTokens.size());
                if (matched >= minRequired) {
                    candidates.add(productSearchText(p));
                }
            }
            if (candidates.size() >= 2) {
                result.add(new ProductAmbiguity(i, name, candidates));
            }
        }
        return result;
    }

    private <T> void scanEntityType(List<T> entities, String entityType, Tables table, List<NullFieldInfo> nullFields) {
        if (entities == null) return;
        for (T entity : entities) {
            if (entity == null) continue;
            try {
                String entityId = null;
                try {
                    java.lang.reflect.Method getUid = entity.getClass().getMethod("getUid");
                    Object uidObj = getUid.invoke(entity);
                    entityId = uidObj != null ? uidObj.toString() : null;
                } catch (NoSuchMethodException e) {
                    try {
                        java.lang.reflect.Method getId = entity.getClass().getMethod("getId");
                        Object idObj = getId.invoke(entity);
                        entityId = idObj != null ? idObj.toString() : null;
                    } catch (NoSuchMethodException e2) {
                        try {
                            java.lang.reflect.Field uidField = entity.getClass().getDeclaredField("uid");
                            uidField.setAccessible(true);
                            Object uidObj = uidField.get(entity);
                            entityId = uidObj != null ? uidObj.toString() : null;
                        } catch (NoSuchFieldException e3) {
                            entityId = "unknown";
                        }
                    }
                }

                java.lang.reflect.Field[] fields = entity.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if ("uid".equals(fieldName) || "id".equals(fieldName) || "updatedAt".equals(fieldName) || "deletedAt".equals(fieldName) || "type".equals(fieldName) || "action".equals(fieldName) || "priority".equals(fieldName) || "payload".equals(fieldName) || "from".equals(fieldName) || "count".equals(fieldName) || "counter".equals(fieldName)) {
                        continue;
                    }
                    if (field.getType().isPrimitive()) {
                        continue;
                    }
                    Object value = field.get(entity);
                    if (value == null) {
                        nullFields.add(new NullFieldInfo(entityType, entityId, fieldName, field.getType()));
                    }
                }
            } catch (Exception e) {
                // Ignore, continue
            }
        }
    }

    @Tool("Démarre un workflow pour vérifier les champs null, les corriger, puis synchroniser")
    public String startSyncFixWorkflow() {
        return executeOnce("startSyncFixWorkflow", "sync-fix", () -> {
            String workflowId = "sync-fix-" + DataId.generate();
            SyncFixWorkflowContext ctx = new SyncFixWorkflowContext(workflowId);
            SYNC_FIX_WORKFLOWS.put(workflowId, ctx);

            // Step 1: Scan all entities
            scanEntityType(CategoryDelegate.findCategories(), "Category", Tables.CATEGORY, ctx.nullFields);
            scanEntityType(ClientDelegate.findClients(), "Client", Tables.CLIENT, ctx.nullFields);
            scanEntityType(CompteTresorDelegate.findCompteTresors(), "CompteTresor", Tables.COMPTETRESOR, ctx.nullFields);
            scanEntityType(DestockerDelegate.findDestockers(), "Destocker", Tables.DESTOCKER, ctx.nullFields);
            scanEntityType(DepenseDelegate.findDepenses(), "Depense", Tables.DEPENSE, ctx.nullFields);
            scanEntityType(FournisseurDelegate.findFournisseurs(), "Fournisseur", Tables.FOURNISSEUR, ctx.nullFields);
            scanEntityType(LigneVenteDelegate.findLigneVentes(), "LigneVente", Tables.LIGNEVENTE, ctx.nullFields);
            scanEntityType(LivraisonDelegate.findLivraisons(), "Livraison", Tables.LIVRAISON, ctx.nullFields);
            scanEntityType(MesureDelegate.findMesures(), "Mesure", Tables.MESURE, ctx.nullFields);
            scanEntityType(OperationDelegate.findOperations(), "Operation", Tables.OPERATION, ctx.nullFields);
            scanEntityType(PrixDeVenteDelegate.findPrixDeVentes(), "PrixDeVente", Tables.PRIXDEVENTE, ctx.nullFields);
            scanEntityType(ProduitDelegate.findProduits(), "Produit", Tables.PRODUIT, ctx.nullFields);
            scanEntityType(RecquisitionDelegate.findRecquisitions(), "Recquisition", Tables.RECQUISITION, ctx.nullFields);
            scanEntityType(StockerDelegate.findStockers(), "Stocker", Tables.STOCKER, ctx.nullFields);
            scanEntityType(TraisorerieDelegate.findTraisoreries(), "Traisorerie", Tables.TRAISORERIE, ctx.nullFields);
            scanEntityType(VenteDelegate.findVentes(), "Vente", Tables.VENTE, ctx.nullFields);
            scanEntityType(AretirerDelegate.findAretirers(), "Aretirer", Tables.ARETIRER, ctx.nullFields);
            scanEntityType(ClientAppartenirDelegate.findClientAppartenirs(), "ClientAppartenir", Tables.CLIENTAPPARTENIR, ctx.nullFields);
            scanEntityType(ClientOrganisationDelegate.findClientOrganisations(), "ClientOrganisation", Tables.CLIENTORGANISATION, ctx.nullFields);
            scanEntityType(RetourDepotDelegate.findRetourDepots(), "RetourDepot", Tables.RETOURDEPOT, ctx.nullFields);
            scanEntityType(RetourMagasinDelegate.findRetourMagasins(), "RetourMagasin", Tables.RETOURMAGASIN, ctx.nullFields);
            scanEntityType(DepotDelegate.findDepots(), "Depot", Tables.DEPOT, ctx.nullFields);
            scanEntityType(InventaireDelegate.findInventaires(), "Inventaire", Tables.INVENTORY, ctx.nullFields);
            scanEntityType(ImmobilisationDelegate.findImmobilisations(), "Immobilisation", Tables.IMMOBILISATION, ctx.nullFields);
            scanEntityType(MatiereDelegate.findMatieres(), "Matiere", Tables.MATIERE, ctx.nullFields);
            scanEntityType(MatiereSkuDelegate.findMatiereSkus(), "MatiereSku", Tables.MATIERESKU, ctx.nullFields);
            scanEntityType(ProductionDelegate.findProductions(), "Production", Tables.PRODUCTION, ctx.nullFields);
            scanEntityType(RepartirDelegate.findRepartirs(), "Repartir", Tables.REPARTIR, ctx.nullFields);
            scanEntityType(ImputerDelegate.findImputers(), "Imputer", Tables.IMPUTER, ctx.nullFields);
            scanEntityType(EntreposerDelegate.findEntreposers(), "Entreposer", Tables.ENTREPOSER, ctx.nullFields);
            // Compter doesn't have a find all method, skip for now
            // scanEntityType(CompterDelegate.findCompters(), "Compter", Tables.COMPTER, ctx.nullFields);
            scanEntityType(PresenceDelegate.findPresences(), "Presence", Tables.PRESENCE, ctx.nullFields);
            scanEntityType(FactureDelegate.findFactures(), "Facture", Tables.FACTURE, ctx.nullFields);

            ctx.scanned = true;

            if (ctx.nullFields.isEmpty()) {
                // No null fields, start sync immediately
                return triggerAdaptiveSync(workflowId);
            } else {
                // Show paginated null fields
                return buildPaginatedNullFieldsList(ctx);
            }
        });
    }

    private String buildPaginatedNullFieldsList(SyncFixWorkflowContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Champs null détectés (").append(ctx.nullFields.size()).append(" champs) - Page ")
                .append(ctx.currentPage).append("/").append(ctx.getTotalPages()).append("\n\n");
        sb.append("| # | Type d'entité | ID de l'entité | Nom du champ | Type de champ |\n");
        sb.append("|---|----------------|----------------|--------------|---------------|\n");
        
        List<NullFieldInfo> pageItems = ctx.getCurrentPageItems();
        int startIndex = (ctx.currentPage - 1) * ctx.itemsPerPage;
        for (int i = 0; i < pageItems.size(); i++) {
            NullFieldInfo nf = pageItems.get(i);
            int globalIndex = startIndex + i + 1;
            String fixedMarker = ctx.fixedFieldIndices.contains(globalIndex - 1) ? "[OK] " : "";
            sb.append("| ").append(fixedMarker).append(globalIndex).append(" | ")
                    .append(tableCell(nf.entityType)).append(" | ")
                    .append(tableCell(nf.entityId)).append(" | ")
                    .append(tableCell(nf.fieldName)).append(" | ")
                    .append(tableCell(nf.fieldType.getSimpleName())).append(" |\n");
        }

        sb.append("\nActions disponibles :\n");
        if (ctx.currentPage > 1) {
            sb.append("- page précédente (\"page précédente\" ou \"-1\")\n");
        }
        if (ctx.currentPage < ctx.getTotalPages()) {
            sb.append("- page suivante (\"page suivante\" ou \"+1\")\n");
        }
        sb.append("- aller à la page N (\"page N\")\n");
        sb.append("- corriger automatiquement TOUS les champs (\"corriger tout\")\n");
        sb.append("- corriger automatiquement la page actuelle (\"corriger page\")\n");
        sb.append("- lancer la synchronisation (\"synchroniser\")\n");
        sb.append("\nWorkFlow ID: ").append(ctx.workflowId);
        return sb.toString();
    }

    @Tool("Navigue vers la page suivante du workflow de sync")
    public String nextPage(@P("ID du workflow") String workflowId) {
        return executeOnce("nextPage", workflowId, () -> {
            SyncFixWorkflowContext ctx = SYNC_FIX_WORKFLOWS.get(workflowId);
            if (ctx == null) {
                return "Workflow introuvable. Veuillez d'abord démarrer un workflow avec startSyncFixWorkflow().";
            }
            if (ctx.currentPage < ctx.getTotalPages()) {
                ctx.currentPage++;
            }
            return buildPaginatedNullFieldsList(ctx);
        });
    }

    @Tool("Navigue vers la page précédente du workflow de sync")
    public String previousPage(@P("ID du workflow") String workflowId) {
        return executeOnce("previousPage", workflowId, () -> {
            SyncFixWorkflowContext ctx = SYNC_FIX_WORKFLOWS.get(workflowId);
            if (ctx == null) {
                return "Workflow introuvable. Veuillez d'abord démarrer un workflow avec startSyncFixWorkflow().";
            }
            if (ctx.currentPage > 1) {
                ctx.currentPage--;
            }
            return buildPaginatedNullFieldsList(ctx);
        });
    }

    @Tool("Navigue vers une page spécifique du workflow de sync")
    public String goToPage(@P("ID du workflow") String workflowId, @P("Numéro de page") int pageNumber) {
        return executeOnce("goToPage", workflowId + "-" + pageNumber, () -> {
            SyncFixWorkflowContext ctx = SYNC_FIX_WORKFLOWS.get(workflowId);
            if (ctx == null) {
                return "Workflow introuvable. Veuillez d'abord démarrer un workflow avec startSyncFixWorkflow().";
            }
            ctx.currentPage = Math.max(1, Math.min(pageNumber, ctx.getTotalPages()));
            return buildPaginatedNullFieldsList(ctx);
        });
    }

    @Tool("Corrige automatiquement la page actuelle du workflow de sync")
    public String autoFixCurrentPage(@P("ID du workflow") String workflowId) {
        return executeOnce("autoFixCurrentPage", workflowId, () -> {
            SyncFixWorkflowContext ctx = SYNC_FIX_WORKFLOWS.get(workflowId);
            if (ctx == null) {
                return "Workflow introuvable. Veuillez d'abord démarrer un workflow avec startSyncFixWorkflow().";
            }
            if (!ctx.scanned) {
                return "Workflow pas encore prêt. Veuillez d'abord démarrer un workflow.";
            }

            List<NullFieldInfo> pageItems = ctx.getCurrentPageItems();
            int startIndex = (ctx.currentPage - 1) * ctx.itemsPerPage;
            int fixedCount = 0;

            for (int i = 0; i < pageItems.size(); i++) {
                int globalIndex = startIndex + i;
                NullFieldInfo nf = pageItems.get(i);
                if (ctx.fixedFieldIndices.contains(globalIndex)) {
                    continue;
                }
                try {
                    Object entity = findEntityById(nf.entityType, nf.entityId);
                    if (entity != null) {
                        java.lang.reflect.Field field = entity.getClass().getDeclaredField(nf.fieldName);
                        field.setAccessible(true);
                        Object value = null;
                        if (String.class.isAssignableFrom(nf.fieldType)) {
                            value = "-";
                        } else if (Number.class.isAssignableFrom(nf.fieldType)) {
                            if (Integer.class.equals(nf.fieldType) || int.class.equals(nf.fieldType)) {
                                value = 0;
                            } else if (Long.class.equals(nf.fieldType) || long.class.equals(nf.fieldType)) {
                                value = 0L;
                            } else if (Double.class.equals(nf.fieldType) || double.class.equals(nf.fieldType)) {
                                value = 0.0;
                            } else if (Float.class.equals(nf.fieldType) || float.class.equals(nf.fieldType)) {
                                value = 0.0f;
                            } else if (Short.class.equals(nf.fieldType) || short.class.equals(nf.fieldType)) {
                                value = (short) 0;
                            } else if (Byte.class.equals(nf.fieldType) || byte.class.equals(nf.fieldType)) {
                                value = (byte) 0;
                            }
                        }

                        if (value != null) {
                            field.set(entity, value);
                            saveEntity(nf.entityType, entity);
                            ctx.fixedFieldIndices.add(globalIndex);
                            fixedCount++;
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            ctx.fixed = ctx.fixedFieldIndices.size() == ctx.nullFields.size();
            StringBuilder sb = new StringBuilder();
            sb.append("Page ").append(ctx.currentPage).append(" : ").append(fixedCount).append(" champs corrigés.\n\n");
            sb.append(buildPaginatedNullFieldsList(ctx));
            if (ctx.fixed) {
                sb.append("\n\nTous les champs ont été corrigés ! Vous pouvez lancer la synchronisation avec \"synchroniser\".");
            }
            return sb.toString();
        });
    }

    private Object findEntityById(String entityType, String entityId) {
        try {
            switch (entityType) {
                case "Category": return CategoryDelegate.findCategory(entityId);
                case "Client": return ClientDelegate.findClient(entityId);
                case "CompteTresor": return CompteTresorDelegate.findCompteTresor(entityId);
                case "Destocker": return DestockerDelegate.findDestocker(entityId);
                case "Depense": return DepenseDelegate.findDepense(entityId);
                case "Fournisseur": return FournisseurDelegate.findFournisseur(entityId);
                case "LigneVente": 
                    try {
                        return LigneVenteDelegate.findLigneVente(Long.parseLong(entityId)); 
                    } catch (NumberFormatException e) {
                        return null;
                    }
                case "Livraison": return LivraisonDelegate.findLivraison(entityId);
                case "Mesure": return MesureDelegate.findMesure(entityId);
                case "Operation": return OperationDelegate.findOperation(entityId);
                case "PrixDeVente": return PrixDeVenteDelegate.findPrixDeVente(entityId);
                case "Produit": return ProduitDelegate.findProduit(entityId);
                case "Recquisition": return RecquisitionDelegate.findRecquisition(entityId);
                case "Stocker": return StockerDelegate.findStocker(entityId);
                case "Traisorerie": return TraisorerieDelegate.findTraisorerie(entityId);
                case "Vente": 
                    try {
                        return VenteDelegate.findVente(Integer.parseInt(entityId)); 
                    } catch (NumberFormatException e) {
                        return null;
                    }
                case "Aretirer": return AretirerDelegate.findAretirer(entityId);
                case "ClientAppartenir": return ClientAppartenirDelegate.findClientAppartenir(entityId);
                case "ClientOrganisation": return ClientOrganisationDelegate.findClientOrganisation(entityId);
                case "RetourDepot": return RetourDepotDelegate.findRetourDepot(entityId);
                case "RetourMagasin": return RetourMagasinDelegate.findRetourMagasin(entityId);
                case "Depot": return DepotDelegate.findDepot(entityId);
                case "Inventaire": return InventaireDelegate.findInventaire(entityId);
                case "Immobilisation": return ImmobilisationDelegate.findImmobilisation(entityId);
                case "Matiere": return MatiereDelegate.findMatiere(entityId);
                case "MatiereSku": return MatiereSkuDelegate.findMatiereSku(entityId);
                case "Production": return ProductionDelegate.findProduction(entityId);
                case "Repartir": return RepartirDelegate.findRepartir(entityId);
                case "Imputer": return ImputerDelegate.findImputer(entityId);
                case "Entreposer": return EntreposerDelegate.findEntreposer(entityId);
                // Compter skipped
                case "Presence": return PresenceDelegate.findPresence(entityId);
                case "Facture": return FactureDelegate.findFacture(entityId);
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void saveEntity(String entityType, Object entity) {
        try {
            switch (entityType) {
                case "Category": CategoryDelegate.updateCategory((data.Category) entity); break;
                case "Client": ClientDelegate.updateClient((data.Client) entity); break;
                case "CompteTresor": CompteTresorDelegate.updateCompteTresor((data.CompteTresor) entity); break;
                case "Destocker": DestockerDelegate.updateDestocker((data.Destocker) entity); break;
                case "Depense": DepenseDelegate.updateDepense((data.Depense) entity); break;
                case "Fournisseur": FournisseurDelegate.updateFournisseur((data.Fournisseur) entity); break;
                case "LigneVente": LigneVenteDelegate.updateLigneVente((data.LigneVente) entity); break;
                case "Livraison": LivraisonDelegate.updateLivraison((data.Livraison) entity); break;
                case "Mesure": MesureDelegate.updateMesure((data.Mesure) entity); break;
                case "Operation": OperationDelegate.updateOperation((data.Operation) entity); break;
                case "PrixDeVente": PrixDeVenteDelegate.updatePrixDeVente((data.PrixDeVente) entity); break;
                case "Produit": ProduitDelegate.updateProduit((data.Produit) entity); break;
                case "Recquisition": RecquisitionDelegate.updateRecquisition((data.Recquisition) entity); break;
                case "Stocker": StockerDelegate.updateStocker((data.Stocker) entity); break;
                case "Traisorerie": TraisorerieDelegate.updateTraisorerie((data.Traisorerie) entity); break;
                case "Vente": VenteDelegate.updateVente((data.Vente) entity); break;
                case "Aretirer": AretirerDelegate.updateAretirer((data.Aretirer) entity); break;
                case "ClientAppartenir": ClientAppartenirDelegate.updateClientAppartenir((data.ClientAppartenir) entity); break;
                case "ClientOrganisation": ClientOrganisationDelegate.updateClientOrganisation((data.ClientOrganisation) entity); break;
                case "RetourDepot": RetourDepotDelegate.updateRetourDepot((data.RetourDepot) entity); break;
                case "RetourMagasin": RetourMagasinDelegate.updateRetourMagasin((data.RetourMagasin) entity); break;
                case "Depot": DepotDelegate.updateDepot((data.Depot) entity); break;
                case "Inventaire": InventaireDelegate.updateInventaire((data.Inventaire) entity); break;
                case "Immobilisation": ImmobilisationDelegate.updateImmobilisation((data.Immobilisation) entity); break;
                case "Matiere": MatiereDelegate.updateMatiere((data.Matiere) entity); break;
                case "MatiereSku": MatiereSkuDelegate.updateMatiereSku((data.MatiereSku) entity); break;
                case "Production": ProductionDelegate.updateProduction((data.Production) entity); break;
                case "Repartir": RepartirDelegate.updateRepartir((data.Repartir) entity); break;
                case "Imputer": ImputerDelegate.updateImputer((data.Imputer) entity); break;
                case "Entreposer": EntreposerDelegate.updateEntreposer((data.Entreposer) entity); break;
                // Compter skipped
                case "Presence": PresenceDelegate.updatePresence((data.Presence) entity); break;
                case "Facture": FactureDelegate.updateFacture((data.Facture) entity); break;
                default: break;
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @Tool("Corrige automatiquement les champs null (String → \"-\", Number → 0) pour un workflow")
    public String autoFixNullFields(@P("ID du workflow") String workflowId) {
        return executeOnce("autoFixNullFields", workflowId, () -> {
            SyncFixWorkflowContext ctx = SYNC_FIX_WORKFLOWS.get(workflowId);
            if (ctx == null) {
                return "Workflow introuvable. Veuillez d'abord démarrer un workflow avec startSyncFixWorkflow().";
            }
            if (!ctx.scanned) {
                return "Workflow pas encore prêt. Veuillez d'abord démarrer un workflow.";
            }
            if (ctx.fixed) {
                return "Champs déjà corrigés. Proceedez à la synchronisation.";
            }

            // Fix each null field
            for (NullFieldInfo nf : ctx.nullFields) {
                try {
                    Object entity = null;
                    // Find entity by type and id
                    switch (nf.entityType) {
                        case "Category": entity = CategoryDelegate.findCategory(nf.entityId); break;
                        case "Client": entity = ClientDelegate.findClient(nf.entityId); break;
                        case "CompteTresor": entity = CompteTresorDelegate.findCompteTresor(nf.entityId); break;
                        case "Destocker": entity = DestockerDelegate.findDestocker(nf.entityId); break;
                        case "Depense": entity = DepenseDelegate.findDepense(nf.entityId); break;
                        case "Fournisseur": entity = FournisseurDelegate.findFournisseur(nf.entityId); break;
                        case "LigneVente": 
                            try {
                                entity = LigneVenteDelegate.findLigneVente(Long.parseLong(nf.entityId)); 
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                            break;
                        case "Livraison": entity = LivraisonDelegate.findLivraison(nf.entityId); break;
                        case "Mesure": entity = MesureDelegate.findMesure(nf.entityId); break;
                        case "Operation": entity = OperationDelegate.findOperation(nf.entityId); break;
                        case "PrixDeVente": entity = PrixDeVenteDelegate.findPrixDeVente(nf.entityId); break;
                        case "Produit": entity = ProduitDelegate.findProduit(nf.entityId); break;
                        case "Recquisition": entity = RecquisitionDelegate.findRecquisition(nf.entityId); break;
                        case "Stocker": entity = StockerDelegate.findStocker(nf.entityId); break;
                        case "Traisorerie": entity = TraisorerieDelegate.findTraisorerie(nf.entityId); break;
                        case "Vente": 
                            try {
                                entity = VenteDelegate.findVente(Integer.parseInt(nf.entityId)); 
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                            break;
                        case "Aretirer": entity = AretirerDelegate.findAretirer(nf.entityId); break;
                        case "ClientAppartenir": entity = ClientAppartenirDelegate.findClientAppartenir(nf.entityId); break;
                        case "ClientOrganisation": entity = ClientOrganisationDelegate.findClientOrganisation(nf.entityId); break;
                        case "RetourDepot": entity = RetourDepotDelegate.findRetourDepot(nf.entityId); break;
                        case "RetourMagasin": entity = RetourMagasinDelegate.findRetourMagasin(nf.entityId); break;
                        case "Depot": entity = DepotDelegate.findDepot(nf.entityId); break;
                        case "Inventaire": entity = InventaireDelegate.findInventaire(nf.entityId); break;
                        case "Immobilisation": entity = ImmobilisationDelegate.findImmobilisation(nf.entityId); break;
                        case "Matiere": entity = MatiereDelegate.findMatiere(nf.entityId); break;
                        case "MatiereSku": entity = MatiereSkuDelegate.findMatiereSku(nf.entityId); break;
                        case "Production": entity = ProductionDelegate.findProduction(nf.entityId); break;
                        case "Repartir": entity = RepartirDelegate.findRepartir(nf.entityId); break;
                        case "Imputer": entity = ImputerDelegate.findImputer(nf.entityId); break;
                        case "Entreposer": entity = EntreposerDelegate.findEntreposer(nf.entityId); break;
                        // Compter skipped
                        case "Presence": entity = PresenceDelegate.findPresence(nf.entityId); break;
                        case "Facture": entity = FactureDelegate.findFacture(nf.entityId); break;
                        default: break;
                    }

                    if (entity != null) {
                        java.lang.reflect.Field field = entity.getClass().getDeclaredField(nf.fieldName);
                        field.setAccessible(true);
                        Object value = null;
                        if (String.class.isAssignableFrom(nf.fieldType)) {
                            value = "-";
                        } else if (Number.class.isAssignableFrom(nf.fieldType)) {
                            if (Integer.class.equals(nf.fieldType) || int.class.equals(nf.fieldType)) {
                                value = 0;
                            } else if (Long.class.equals(nf.fieldType) || long.class.equals(nf.fieldType)) {
                                value = 0L;
                            } else if (Double.class.equals(nf.fieldType) || double.class.equals(nf.fieldType)) {
                                value = 0.0;
                            } else if (Float.class.equals(nf.fieldType) || float.class.equals(nf.fieldType)) {
                                value = 0.0f;
                            } else if (Short.class.equals(nf.fieldType) || short.class.equals(nf.fieldType)) {
                                value = (short) 0;
                            } else if (Byte.class.equals(nf.fieldType) || byte.class.equals(nf.fieldType)) {
                                value = (byte) 0;
                            }
                        }

                        if (value != null) {
                            field.set(entity, value);
                            // Save the entity
                            switch (nf.entityType) {
                                case "Category": CategoryDelegate.updateCategory((data.Category) entity); break;
                                case "Client": ClientDelegate.updateClient((data.Client) entity); break;
                                case "CompteTresor": CompteTresorDelegate.updateCompteTresor((data.CompteTresor) entity); break;
                                case "Destocker": DestockerDelegate.updateDestocker((data.Destocker) entity); break;
                                case "Depense": DepenseDelegate.updateDepense((data.Depense) entity); break;
                                case "Fournisseur": FournisseurDelegate.updateFournisseur((data.Fournisseur) entity); break;
                                case "LigneVente": LigneVenteDelegate.updateLigneVente((data.LigneVente) entity); break;
                                case "Livraison": LivraisonDelegate.updateLivraison((data.Livraison) entity); break;
                                case "Mesure": MesureDelegate.updateMesure((data.Mesure) entity); break;
                                case "Operation": OperationDelegate.updateOperation((data.Operation) entity); break;
                                case "PrixDeVente": PrixDeVenteDelegate.updatePrixDeVente((data.PrixDeVente) entity); break;
                                case "Produit": ProduitDelegate.updateProduit((data.Produit) entity); break;
                                case "Recquisition": RecquisitionDelegate.updateRecquisition((data.Recquisition) entity); break;
                                case "Stocker": StockerDelegate.updateStocker((data.Stocker) entity); break;
                                case "Traisorerie": TraisorerieDelegate.updateTraisorerie((data.Traisorerie) entity); break;
                                case "Vente": VenteDelegate.updateVente((data.Vente) entity); break;
                                case "Aretirer": AretirerDelegate.updateAretirer((data.Aretirer) entity); break;
                                case "ClientAppartenir": ClientAppartenirDelegate.updateClientAppartenir((data.ClientAppartenir) entity); break;
                                case "ClientOrganisation": ClientOrganisationDelegate.updateClientOrganisation((data.ClientOrganisation) entity); break;
                                case "RetourDepot": RetourDepotDelegate.updateRetourDepot((data.RetourDepot) entity); break;
                                case "RetourMagasin": RetourMagasinDelegate.updateRetourMagasin((data.RetourMagasin) entity); break;
                                case "Depot": DepotDelegate.updateDepot((data.Depot) entity); break;
                                case "Inventaire": InventaireDelegate.updateInventaire((data.Inventaire) entity); break;
                                case "Immobilisation": ImmobilisationDelegate.updateImmobilisation((data.Immobilisation) entity); break;
                                case "Matiere": MatiereDelegate.updateMatiere((data.Matiere) entity); break;
                                case "MatiereSku": MatiereSkuDelegate.updateMatiereSku((data.MatiereSku) entity); break;
                                case "Production": ProductionDelegate.updateProduction((data.Production) entity); break;
                                case "Repartir": RepartirDelegate.updateRepartir((data.Repartir) entity); break;
                                case "Imputer": ImputerDelegate.updateImputer((data.Imputer) entity); break;
                                case "Entreposer": EntreposerDelegate.updateEntreposer((data.Entreposer) entity); break;
                                case "Compter": CompterDelegate.updateCompter((data.Compter) entity); break;
                                case "Presence": PresenceDelegate.updatePresence((data.Presence) entity); break;
                                case "Facture": FactureDelegate.updateFacture((data.Facture) entity); break;
                                default: break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore, continue
                }
            }
            ctx.fixed = true;
            return "Champs corrigés avec succès ! Maintenant, lancez la synchronisation avec triggerAdaptiveSync(\"" + workflowId + "\").";
        });
    }

    @Tool("Déclenche la synchronisation adaptive pour un workflow")
    public String triggerAdaptiveSync(@P("ID du workflow (optionnel)") String workflowId) {
        return executeOnce("triggerAdaptiveSync", workflowId != null ? workflowId : "manual", () -> {
            SyncFixWorkflowContext ctx = workflowId != null ? SYNC_FIX_WORKFLOWS.get(workflowId) : null;
            if (ctx != null && ctx.synced) {
                return "Synchronisation déjà effectuée.";
            }

            // Wake up BackgroundSyncService if it's paused
            services.BackgroundSyncService syncService = services.BackgroundSyncService.getInstance();
            if (syncService != null) {
                syncService.resumeSync();
            }

            if (ctx != null) {
                ctx.synced = true;
            }
            return "Synchronisation adaptive déclenchée ! Les données seront envoyées progressivement.";
        });
    }
}
