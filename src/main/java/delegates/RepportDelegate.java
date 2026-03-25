/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.RapportStorage;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.chart.XYChart;
import tools.ServiceLocator;
import tools.Tables;
import tools.VenteReporter;
import data.Produit;
import data.StockAgregate;
import data.SaleAgregate;
import tools.RecentSale;
import tools.Metric;
import tools.SaleReport;

/**
 *
 * @author endeleya
 */
public class RepportDelegate {

    public static RapportStorage getStorage() {
        RapportStorage cats = (RapportStorage) ServiceLocator.getInstance().getService(Tables.REPPORTING);
        return cats;
    }

    public static Double sumVente(LocalDate debut, LocalDate fin, double taux, String role, String region) {
        return getStorage().dashCardVente(role, debut, fin, taux, region);
    }

    public static Double sumResult(LocalDate debut, LocalDate fin, double taux, String role, String region) {
        return getStorage().dashCardResult(role, debut, fin, region, taux);
    }

    public static Double sumCoutAcq(LocalDate debut, LocalDate fin, String role, String region) {
        return getStorage().dashCardDepense(role, debut, fin, region);
    }

    /**
     * La fonction sert a afficher le graphique pour voir l'evolution du
     * business sur une soit annee
     *
     * @param vChart UI xy-chart
     * @param serie1 serie de vente
     * @param serie2 serie de cout-acquisition
     * @param serie3 serie de marge
     * @param debut  date debut periode
     * @param fin    date fin periode
     * @param role   role utilisateur
     * @param taux   taux de change
     * @param region region ou site
     */
    public static void metrify(XYChart<String, Number> vChart, String serie1, String serie2, String serie3,
            LocalDate debut, LocalDate fin, String role, String region, String periodname) {
        getStorage().metrify(vChart, serie1, serie2, serie3, debut, fin, role, region, periodname);
    }

    public static List<VenteReporter> findReportSaleByProduct(LocalDate date1, LocalDate date2) {
        return getStorage().findReportSaleByProduct(date1, date2);
    }

    public static List<VenteReporter> findReportSaleByProduct(LocalDate date1, LocalDate date2, String region) {
        return getStorage().findReportSaleByProduct(date1, date2, region);
    }

    public static List<VenteReporter> findReportSaleByCategory(LocalDate date1, LocalDate date2) {
        return getStorage().findReportSaleByCategory(date1, date2);
    }

    public static List<VenteReporter> findReportSaleByCategory(LocalDate date1, LocalDate date2, String region) {
        return getStorage().findReportSaleByCategory(date1, date2, region);
    }

    public static List<VenteReporter> findReportSaleByClient(LocalDate date1, LocalDate date2, String region,
            String devise) {
        return getStorage().findReportSaleByClient(date1, date2, region, devise);
    }

    public static StockAgregate findCurrentStock(Produit prod, LocalDate today, LocalDate today1) {
        return getStorage().findStockFor(prod, today, today1);
    }

    public static StockAgregate findCurrentStock(Produit prod, String region, LocalDate today, LocalDate otherDay) {
        return getStorage().findStockFor(prod, today, otherDay, region);
    }

    public static boolean repportInBackground(LocalDate dateDebut, LocalDate dateFin, String region) {
        return getStorage().rapporter(dateDebut, dateFin, region);
    }

    public static double chiffreDaffaire(LocalDate dateDebut, LocalDate dateFin, String region) {
        return getStorage().chiffreDaffaire(dateDebut, dateFin, region);
    }

    public static double chargeVariable(LocalDate dateDebut, LocalDate dateFin, String region) {
        return getStorage().chargeVariable(dateDebut, dateFin, region);
    }

    public static double chiffreDaffaireParProduit(String produit, LocalDate dateDebut, LocalDate dateFin,
            String region) {
        return getStorage().chiffreDaffaireParProduit(produit, dateDebut, dateFin, region);
    }

    public static double chiffreDaffaireParCategory(String category, LocalDate dateDebut, LocalDate dateFin,
            String region) {
        return getStorage().chiffreDaffaireParCategory(category, dateDebut, dateFin, region);
    }

    public static double findStockValue(LocalDate date1, LocalDate date2, String region, String contextx) {
        String context = date1.equals(date2) ? ("Journalier du " + date2) : ("Intervale du " + date1 + " au " + date2);
        return getStorage().findStockValue(date1, date2, region, context);
    }

    public static List<Metric> kpiValues(LocalDate date1, LocalDate date2, String region, String groupBy) {
        return getStorage().kpi(date1, date2, region, groupBy);
    }

    public static SaleAgregate findSaleReportFor(String prod, LocalDate today, LocalDate today1, String region) {
        return getStorage().findSaleReportFor(prod, today, today1, region);
    }

    public static List<SaleReport> findSaleReportPerProduct(LocalDate today, LocalDate today1, String region) {
        return getStorage().findSaleReportPerProduct(today, today1, region);
    }

    public static List<SaleReport> findSaleReportPerCategory(LocalDate today, LocalDate today1, String region) {
        return getStorage().findSaleReportPerCategory(today, today1, region);
    }

    public static List<RecentSale> findRecentSales(String region) {
        return getStorage().findRecentSales(region);
    }

    public static void purgerToutMetric(LocalDate date1, LocalDate date2, String region) {
        getStorage().purgerToutMetric(date1, date2, region);
    }

    public static SaleAgregate createMetric(SaleAgregate sa) {
        return getStorage().createMetrics(sa);
    }

    public static Double turnOverOf(LocalDate date1, LocalDate date2, String region) {
        return getStorage().turnOverOf(date1, date2, region);
    }

    public static Double expenseOf(LocalDate date1, LocalDate date2, String region) {
        return getStorage().expenseOf(date1, date2, region);
    }

    public static Double operationExpenseOf(LocalDate date1, LocalDate date2, String region) {
        return getStorage().operationExpenseOf(date1, date2, region);
    }

    public static Double industrialExpenseOf(LocalDate date1, LocalDate date2, String region) {
        return getStorage().industrialExpenseOf(date1, date2, region);
    }

    public static List<StockAgregate> findByContext(String context) {
        return getStorage().findByContext(context);
    }

    public static StockAgregate updateStockAgregate(StockAgregate sa) {
        return getStorage().updateStockAgregate(sa);
    }

}
