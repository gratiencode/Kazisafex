/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.RecquisitionStorage;
import data.Produit;
import java.util.List;
import data.Recquisition;
import data.helpers.CardHelper;
import data.Inventaire;
import data.PrixDeVente;
import data.Mesure;
import data.StockAgregate;
import tools.ServiceLocator;
import tools.Tables;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import services.RecquisitionService;
import tools.ListViewItem;
import tools.Rupture;
import utilities.Peremption;

/**
 *
 * @author eroot
 */
public class RecquisitionDelegate {

    public static Recquisition saveRecquisition(Recquisition cat) {
        return getStorage().createRecquisition(cat);
    }

    public static Recquisition updateRecquisition(Recquisition cat) {
        return getStorage().updateRecquisition(cat);
    }

    public static void deleteRecquisition(Recquisition cat) {
        getStorage().deleteRecquisition(cat);
    }

    public static Recquisition findRecquisition(String objId) {
        return getStorage().findRecquisition(objId);
    }

    public static List<Recquisition> findRecquisitions() {
        return getStorage().findRecquisitions();
    }

    public static List<Recquisition> findRecquisitionByProduit(String idprod) {
        return getStorage().findRecquisitionByProduit(idprod);
    }

    public static List<Recquisition> findRecquisitionByProduit(String idprod, String lot) {
        return getStorage().findRecquisitionByProduit(idprod, lot);
    }

    public static List<Recquisition> findRecquisitions(int s, int m) {
        return getStorage().findRecquisitions(s, m);
    }

    public static RecquisitionStorage getStorage() {
        RecquisitionStorage cats = (RecquisitionStorage) ServiceLocator.getInstance().getService(Tables.RECQUISITION);
        return cats;
    }

    public static List<Recquisition> findRecquisitionByProduitRegion(String uid, String region) {
        return getStorage().findRecquisitionByProduitRegion(uid, region);
    }

    public static List<Recquisition> findDescSortedByDateForProduit(String uid) {
        return getStorage().findDescSortedByDateForProduit(uid);
    }

    public static List<Recquisition> toFefoOrdering(String uid) {
        return getStorage().toFefoOrdering(uid);
    }

    public static List<Recquisition> toFifoOrdering(String uid) {
        return getStorage().toFifoOrdering(uid);
    }

    public static List<Recquisition> toLifoOrdering(String uid) {
        return getStorage().toLifoOrdering(uid);
    }

    public static List<Recquisition> toFefoOrdering(String uid, String region) {
        return getStorage().toFefoOrdering(uid, region);
    }

    public static List<Recquisition> toFifoOrdering(String uid, String region) {
        return getStorage().toFifoOrdering(uid, region);
    }

    public static List<Recquisition> toLifoOrdering(String uid, String region) {
        return getStorage().toLifoOrdering(uid, region);
    }

    public static Recquisition getHeaderRecq(String meth, Produit prod) {
        return getStorage().getHeaderRecq(meth, prod);
    }

    public static Recquisition getHeaderRecq(String meth, Produit prod, String region) {
        return getStorage().getHeaderRecq(meth, prod, region);
    }

    public static Recquisition getLastEntry(String meth, Produit prod, String region) {
        return getStorage().getLastEntry(meth, prod, region);
    }

    public static List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region) {
        return getStorage().findRecquisitionByProduit(uid, numlot, region);
    }

    public static List<Recquisition> findByDateExpInterval(LocalDate time, LocalDate darg) {
        return getStorage().findByDateExpInterval(time, darg);
    }

    public static List<Object[]> findGoods() {
        return getStorage().findGoods();
    }

    public static List<Object[]> findGoodsFromRegion(String region) {
        return getStorage().findGoodsFromRegion(region);
    }

    public static List<Object[]> findGoodsCategorized(String cat) {
        return getStorage().findGoodsCategorized(cat);
    }

    public static List<Object[]> findGoodsCategorized(String cat, String region) {
        return getStorage().findGoodsCategorized(cat, region);
    }

    public static List<Recquisition> findRecquisitions(String region) {
        return getStorage().findRecquisitions(region);
    }

    public static double sumByProduitWithLotInUnit(String idpro, String lot) {
        return getStorage().sumByProduitWithLotInUnit(idpro, lot);
    }

    public static StockAgregate saveFromRecqusition(Recquisition e) {
        return getStorage().saveStockFromRecquisition(e);
    }

    public static StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid) {
        return getStorage().findClosedStock(today, today1, uid);
    }

    public static List<PrixDeVente> findGrossPrices(String ruid, String mesure) {
        return getStorage().findGrossPrices(ruid, mesure);
    }

    public static PrixDeVente getPrice(Recquisition r, List<Mesure> mesures) {
        return getStorage().getExistingPricefor(r, mesures);
    }

    public static List<PrixDeVente> getGrosFor(String ruid, String mesure) {
        return getStorage().findGrossPrices(ruid, mesure);
    }

    public static double sumRetourEnStock(String idpro, LocalDate d, LocalDate f) {
        return getStorage().sumRetourDepot(idpro, d, f);
    }

    public static double findCurrentStockFor(Produit p, String region) {
        return getStorage().findCurrentStockFor(p, region);
    }

    public static double sumRetourEnStock(String idpro, LocalDate d, LocalDate f, String region) {
        return getStorage().sumRetourDepot(idpro, d, f, region);
    }

    public static double sumLigneventeFrom(String proId, LocalDate d, LocalDate f, String region) {
        return getStorage().sumLigneventeFrom(proId, d, f, region);
    }

    public static double sumLignevente(String proId, LocalDate d, LocalDate f) {
        return getStorage().sumLignevente(proId, d, f);
    }

    public static Recquisition appendToTransaction(Recquisition r) {
        return getStorage().addToTransaction(r);
    }

    public static void beginTransaction() {
        getStorage().startTransaction();
    }

  

    public static List<Recquisition> findByReference(String ref) {
        return getStorage().findByReference(ref);
    }

    public static List<Recquisition> findByReference(String uid, String ref) {
        return getStorage().findByReference(uid, ref);
    }

    public static double findRemainedInMagasinFor(String uid) {
        return getStorage().findRemainedInMagasinFor(uid);
    }

    public static double findRemainedInMagasinFor(String uid, LocalDate d1, LocalDate d2) {
        return getStorage().findRemainedInMagasinFor(uid, d1, d2);
    }

    public static double findRemainedInMagasinForBatched(String uid, String numlot) {
        return getStorage().findRemainedInMagasinForBatched(uid, numlot);
    }

    public static double findRemainedInMagasinForBatched(String uid, String numlot, LocalDate ouverture, LocalDate cloture) {
        return getStorage().findRemainedInMagasinForBatched(uid, numlot, ouverture, cloture);
    }

    public static double findRemainedInMagasinForBatched(String uid, String numlot, LocalDate ouverture, LocalDate cloture, String region) {
        return getStorage().findRemainedInMagasinForBatched(uid, numlot, ouverture, cloture, region);
    }

    public static double findRemainedInMagasinFor(String uid, String region) {
        return getStorage().findRemainedInMagasinFor(uid, region);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }

    public static List<Rupture> findStockEnRupture() {
        return getStorage().findStockEnRupture();
    }

    public static List<Rupture> findStockEnRupture(String region) {
        return getStorage().findStockEnRupture(region);
    }

    public static List<Recquisition> findRecquisitionByRegionGroupBylot(String region) {
        return getStorage().findRecquisitionByRegionGroupBylot(region); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<Recquisition> findRecquisitionGroupByLot() {
        return getStorage().findRecquisitionGroupByLot();//To change body of generated methods, choose Tools | Templates.
    }

    public static List<Recquisition> findRecquisitionByRegionGroupBylot(LocalDate debut, LocalDate fin, String region) {
        return getStorage().findRecquisitionByRegionGroupBylot(debut, fin, region);//To change body of generated methods, choose Tools | Templates.
    }

    public static List<Recquisition> findRecquisitionGroupByLot(LocalDate debut, LocalDate fin) {
        return getStorage().findRecquisitionGroupByLot(debut, fin); //To change body of generated methods, choose Tools | Templates.
    }

    public static double findRemainedInMagasinByLot(String puid, String numlot) {
        return getStorage().findRemainedInMagasinByLot(puid, numlot);
    }

    public static List<Recquisition> findOrphanRecquisitions(String prodId) {
        return getStorage().findOrphanRecquisitions(prodId);
    }

    public static List<PrixDeVente> findLastPrices(String prodId) {
        List<PrixDeVente> ps = getStorage().findLastPrices(prodId);
        if (ps.isEmpty()) {
            return List.of();
        }
        Map<String, Recquisition> recquisitionCache
                = ps.stream()
                        .map(p -> p.getRecquisitionId().getUid())
                        .distinct()
                        .collect(Collectors.toMap(
                                uid -> uid,
                                uid -> findRecquisition(uid)));
        Recquisition lastRecquisition
                = recquisitionCache.values().stream()
                        .max(Comparator.comparing(Recquisition::getDate))
                        .orElseThrow();
        return ps.stream()
                .filter(p -> p.getRecquisitionId().getUid().equals(lastRecquisition.getUid()))
                .toList();
    }

    public static double findRemainedInMagasinByLot(String puid, String numlot, String region) {
        return getStorage().findRemainedInMagasinByLot(puid, numlot, region);
    }

    public static double sumByProduit(String uid) {
        return getStorage().sumByProduit(uid);
    }

    public static double sumByProduit(String uid, String region) {
        return getStorage().sumByProduit(uid, region);
    }

    public static List<Recquisition> findByReference(String ref, String uid, String numlot) {
        return getStorage().findByReference(ref, uid, numlot);
    }

    public static double sumByProduit(String idpro, LocalDate d1, LocalDate d2) {
        return getStorage().sumByProduit(idpro, d1, d2);
    }

    public static double sumByProduit(String idpro, LocalDate d1, LocalDate d2, String region) {
        return getStorage().sumByProduit(idpro, d1, d2, region);
    }

    public static List<ListViewItem> populate() {
        List<ListViewItem> r = getStorage().populate();
        System.out.println("En delegate populate size = " + r.size());
        return r;
    }

    public static List<ListViewItem> populate(String region, String context) {
        return getStorage().populate(region, context);
    }

    public static Recquisition findCustomized(String uid, String numlot, String ref, LocalDateTime dateStocker) {
        return getStorage().findCustomized(uid, numlot, ref, dateStocker);
    }

    public static CardHelper makeCardFor(Produit p) {
        return getStorage().populateCardFor(p);
    }

    public static CardHelper makeCardFor(Produit p, String region) {
        return getStorage().populateCardFor(p, region);
    }

    public static CardHelper makeCardFor(Produit p, LocalDate debut, LocalDate fin) {
        return getStorage().populateCardFor(p, debut, fin);
    }

    public static CardHelper makeCardFor(Produit p, LocalDate debut, LocalDate fin, String region) {
        return getStorage().populateCardFor(p, debut, fin, region);
    }

    public static List<ListViewItem> populateBy(String category) {
        return getStorage().populateBy(category);
    }

    public static List<ListViewItem> populateBy(String category, String region) {
        return getStorage().populateBy(category, region);
    }

    public static List<Recquisition> getRecqusitions() {
        return RecquisitionService.getRecquisitions();
    }

    public static List<Recquisition> findUnSyncedRecquisitions(long disconnected_at) {
        return getStorage().findUnSyncedRecquisitions(disconnected_at);
    }

    public static double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        return getStorage().sommeEntreeSurPeriode(uid, datedebut, datefin, region);
    }

    public static double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        return getStorage().sommeSortieSurPeriode(uid, datedebut, datefin, region);
    }

    public static double calculerStockInitialEnUnite(String uid, LocalDate datedebut, String region) {
        return getStorage().calculerStockInitialEnUnite(uid, datedebut, region);
    }

    public static double getStockExpiree(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        return getStorage().getStockExpiree(uid, datedebut, datefin, region);
    }

    public static boolean cloturerStocks(String region, LocalDate datedebut, LocalDate datefin, String context) {
        return getStorage().cloturerStocks(region, datedebut, datefin, context);
    }

    public static Recquisition cloturerUnProduit(Produit produit, String region, LocalDate datedebut, LocalDate datefin, String context) {
        return getStorage().clotureStockProduit(produit, region, datedebut, datefin, context);
    }

    public static void rectifyStock(Produit produit, LocalDate datedebut, LocalDate datefin, String region, double coutAch) {
        getStorage().rectifyStock(produit, datedebut, datefin, region, coutAch);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

    public static void ajustementInventaire(Inventaire inventaire, String region) {
        getStorage().adjustAfterInventory(inventaire, region);
    }

    public static List<Peremption> showExpiredAtInterval(LocalDate dateExp1, LocalDate dateEpx2, String region) {
        return getStorage().showExpiredAtInterval(dateExp1, dateEpx2, region);
    }

}
