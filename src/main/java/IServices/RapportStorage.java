/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.ClientOrganisation;
import data.Facture;
import data.LigneVente;
import data.Mesure;
import data.Produit;
import data.SaleAgregate;
import data.StockAgregate;
import data.Traisorerie;
import data.Vente;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import tools.RecentSale;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import javafx.scene.chart.XYChart;
import services.ManagedSessionFactory;
import tools.Metric;
import tools.ResultStatementItem;
import tools.SaleReport;
import tools.VenteReporter;
import utilities.Relevee;

/**
 *
 * @author endeleya
 */
public interface RapportStorage {

    public Mesure findByProduitAndDescription(String prod, String descr);

    public Mesure findByProduitAndQuant(String prod, double quant);

    public List<VenteReporter> findReportSaleByProduct(LocalDate d1, LocalDate d2);

    public List<VenteReporter> findReportSaleByProduct(LocalDate d1, LocalDate d2, String region);

    public List<VenteReporter> findReportSaleByCategory(LocalDate d1, LocalDate d2);

    public List<VenteReporter> findReportSaleByCategory(LocalDate d1, LocalDate d2, String region);

    public List<VenteReporter> findReportSaleByClient(LocalDate d1, LocalDate d2, String region, String devise);

    public List<ResultStatementItem> findMargesPerProduct(LocalDate d1, LocalDate d2);

    public List<LigneVente> findLigneVenteFor(int venteId);

    public List<Produit> searchProduit(String produx);

    public HashMap<Long, String> getTop10ProductDesc();

    public HashMap<Long, String> getTop10ProductDesc(String region);

    public Double sumCdfRecoveredTraisorerie(String ref);

    public Double sumRecoveredByNumeroFacture(String vente, double taux);

    public List<Vente> findVenteCredit();

    public List<Vente> findVenteCredit(String region);

    public List<Vente> findVenteCreditByRef(String region, String ref);

    public List<Vente> findVenteCreditByRef(String ref);

    public List<Vente> findVenteCreditByLocalDateInterval(LocalDate d1, LocalDate d2);

    public List<Vente> findVenteCreditByLocalDateInterval(LocalDate d1, LocalDate d2, String region);

    public List<Traisorerie> getTresorTransactions(String traisor_id, String region);

    public ClientOrganisation findClientOrganisation(String idClient);

    public List<Relevee> getReleveFor(String orgId, LocalDate d1, LocalDate d2);

    public List<Facture> getSubsBills(String billno, String org, LocalDate d1, LocalDate d2, double taux);

    public Double sumUnitRetourMagasin(String prodUid, String region);

    public List<Facture> getFacturesByOrg(String orgUid);

    public List<Facture> getUnpaidFacturesByOrg(String orgUid);

    public long getSaleItemCount(int vuid);

    public boolean rapporter(LocalDate dateDebut, LocalDate dateFin, String region);

    public double dashCardDepense(String role, LocalDate d1, LocalDate kesho, String region);

    public HashMap<String, Double> getSalesInPerod(LocalDate d1, LocalDate d2, String region, double taux);

    public List<Vente> getDraftedCarts();

    public Vente getDraftedCart(int saved);

    public double dashCardVente(String role, LocalDate d1, LocalDate kesho, double taux, String region);

    public double dashCardResult(String role, LocalDate d1, LocalDate kesho, String region, double taux);

    public Double findStockValue(LocalDate date1, LocalDate date2, String region, String context);

    public void metrify(XYChart<String, Number> venteChart, String serie1, String serie2, String serie3, LocalDate d1,
            LocalDate d2, String role, String region, String periodName);

    public StockAgregate clotureStocks(StockAgregate cat);

    public SaleAgregate createMetrics(SaleAgregate cat);

    public StockAgregate updateStockAgregate(StockAgregate cat);

    public SaleAgregate refeshMetrics(SaleAgregate cat);

    public void deleteStockAgregate(StockAgregate cat);

    public void deleteMetrics(SaleAgregate cat);

    public StockAgregate findStockAgregate(String catId);

    public StockAgregate findMetrics(String catId);

    public List<StockAgregate> findStockAgregate();

    public List<StockAgregate> findSaleAgregate();

    public List<StockAgregate> findByContext(String context);

    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate today1);

    public SaleAgregate findSaleReportFor(String prod, LocalDate today, LocalDate today1, String region);

    public void purgerToutMetric(LocalDate date1, LocalDate date2, String region);

    public List<SaleReport> findSaleReportPerProduct(LocalDate today, LocalDate today1, String region);

    public List<SaleReport> findSaleReportPerCategory(LocalDate today, LocalDate today1, String region);

    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate otherDay, String region);

    public List<RecentSale> findRecentSales(String region);

    public double chiffreDaffaire(LocalDate dateDebut, LocalDate dateFin, String region);

    public double chargeVariable(LocalDate dateDebut, LocalDate dateFin, String region);

    public double chiffreDaffaireParCategory(String category, LocalDate dateDebut, LocalDate dateFin, String region);

    public double chiffreDaffaireParProduit(String produit, LocalDate dateDebut, LocalDate dateFin, String region);

    public List<Metric> kpi(LocalDate dateDebut, LocalDate dateFin, String region, String groupBy);

    public double sommeChiffreAffaire(LocalDate debut, LocalDate fin, String region);

    public double sommeSortieValeur(LocalDate debut, LocalDate fin, String region);

    public Double turnOverOf(LocalDate d1, LocalDate d2, String region);

    public Double expenseOf(LocalDate d1, LocalDate d2, String region);

    public Double operationExpenseOf(LocalDate d1, LocalDate d2, String region);

    public Double industrialExpenseOf(LocalDate d1, LocalDate d2, String region);
}
