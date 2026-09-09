/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import data.Produit;
import java.util.List;
import java.util.Set;
import data.Recquisition;
import data.Inventaire;
import data.Mesure;
import data.PrixDeVente;
import data.StockAgregate;
import data.helpers.CardHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import services.ClotureCallback;
import tools.ListViewItem;
import tools.Rupture;
import utilities.Peremption;

/**
 *
 * @author eroot
 */
public interface RecquisitionStorage {

    public Recquisition createRecquisition(Recquisition obj);

    public Recquisition updateRecquisition(Recquisition obj);

    public void deleteRecquisition(Recquisition obj);

    public Long getCount();
    
    public StockAgregate saveStockFromRecquisition(Recquisition e);

    public Recquisition findRecquisition(String objId);

    public List<Recquisition> findRecquisitions();

    public List<Recquisition> findRecquisitions(int start, int max);

    public List<Recquisition> findRecquisitionByProduit(String objId);
    
    public Recquisition findRecquisition(String ref, String prodId, String numlot, String region);

    public List<Recquisition> findRecquisitionByProduit(String objId, String lot);

    public List<Recquisition> findRecquisitionByProduitRegion(String uid, String region);
    
    public List<StockAgregate> findAgregateDistinctlyByLot(String prod, String region);

    public List<Recquisition> findDescSortedByDateForProduit(String uid);

    public List<Recquisition> toFefoOrdering(String uid);

    public List<Recquisition> toFifoOrdering(String uid);

    public List<Recquisition> toLifoOrdering(String uid);

    public List<Recquisition> toFefoOrdering(String uid, String region);

    public List<Recquisition> toFifoOrdering(String uid, String region);

    public List<Recquisition> toLifoOrdering(String uid, String region);

    public List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region);

    public List<Recquisition> findByDateExpInterval(LocalDate time, LocalDate darg);

    public List<Object[]> findGoods();

    public List<Object[]> findGoodsFromRegion(String region);
    
    public void setClotureListener(ClotureCallback listener);

    public List<Object[]> findGoodsCategorized(String cat);

    public List<Object[]> findGoodsCategorized(String cat, String region);

    public List<Recquisition> findRecquisitions(String region);

    public Recquisition addToTransaction(Recquisition r);

    public void startTransaction();

    public double sumByProduitWithLotInUnit(String idpro, String lot);

    public List<Recquisition> findByReference(String ref);

    public List<Recquisition> findByReference(String uid, String ref);

    public double findRemainedInMagasinFor(String uid);

    public double findRemainedInMagasinFor(String uid, LocalDate d, LocalDate f);

    public double findRemainedInMagasinFor(String uid, String region);

    public List<Rupture> findStockEnRupture();

    public List<Rupture> findStockEnRupture(String region);

    public List<Recquisition> findRecquisitionByRegionGroupBylot(String region);

    public List<Recquisition> findRecquisitionGroupByLot();

    public List<Recquisition> findRecquisitionByRegionGroupBylot(LocalDate debut, LocalDate fin, String region);

    public List<Recquisition> findRecquisitionGroupByLot(LocalDate debut, LocalDate fin);

    public double findRemainedInMagasinByLot(String puid, String numlot);

    public double findRemainedInMagasinByLot(String puid, String numlot, String region);

    public double sumByProduit(String uid);

    public double sumByProduit(String uid, String region);

    public List<Recquisition> mergeSet(Set<Recquisition> bulk);

    public List<Recquisition> findByReference(String ref, String uid, String numlot);

    public double sumByProduit(String idpro, LocalDate d1, LocalDate d2);

    public double sumByProduit(String idpro, LocalDate d1, LocalDate d2, String region);

    public List<ListViewItem> populate();

    public List<ListViewItem> populate(String region,String context);

    public Recquisition findCustomized(String uid, String numlot, String ref, LocalDateTime dateStocker);
    
    public List<PrixDeVente> findGrossPrices(String ruid,String mesure);

    public double findRemainedInMagasinForBatched(String uid, String numlot);

    public CardHelper populateCardFor(Produit produc);

    public double sumRetourDepot(String proId, LocalDate d, LocalDate f);

    public double sumRetourDepot(String proId, LocalDate d, LocalDate f, String region);

    public CardHelper populateCardFor(Produit product, String region);

    public CardHelper populateCardFor(Produit product, LocalDate debut, LocalDate fin);

    public CardHelper populateCardFor(Produit product, LocalDate debut, LocalDate fin, String region);

    public List<Recquisition> toFefoOrdering(String uid, LocalDate debut, LocalDate fin);

    public List<Recquisition> toFifoOrdering(String uid, LocalDate debut, LocalDate fin);

    public List<Recquisition> toLifoOrdering(String uid, LocalDate debut, LocalDate fin);

    public List<Recquisition> toFefoOrdering(String uid, LocalDate debut, LocalDate fin, String region);

    public List<Recquisition> toFifoOrdering(String uid, LocalDate debut, LocalDate fin, String region);

    public List<Recquisition> toLifoOrdering(String uid, LocalDate debut, LocalDate fin, String region);

    public Recquisition getHeaderRecq(String meth, Produit prod);

    public Recquisition getHeaderRecq(String meth, Produit prod, String region);

    public double sumLigneventeFrom(String proId, LocalDate d, LocalDate f, String region);

    public double sumLignevente(String proId, LocalDate d, LocalDate f);

    public List<ListViewItem> populateBy(String category);

    public List<ListViewItem> populateBy(String category, String region);

    public double findRemainedInMagasinForBatched(String uid, String numlot, LocalDate ouverture, LocalDate cloture);

    public double findRemainedInMagasinForBatched(String uid, String numlot, LocalDate ouverture, LocalDate cloture, String region);

    public List<Recquisition> findUnSyncedRecquisitions(long disconnected_at);

    public boolean isExists(String uid);

    public boolean isExists(String uid, LocalDateTime atime);
    
    public PrixDeVente getExistingPricefor(Recquisition r,List<Mesure> mesures);

    public void adjustAfterInventory(Inventaire inventaire, String region);

    public Recquisition getLastEntry(Produit prod, String region);

    public double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin,String lot, String region);

    public double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin,String lot, String region);

    public double calculerStockInitialEnUnite(String uid, LocalDate datedebut,String lot, String region);

    public double getStockExpiree(String uid, LocalDate datedebut, LocalDate datefin,String lot, String region);

    public boolean cloturerStocks(String region, LocalDate datedebut, LocalDate datefin,String context);
    
    public void rectifyStock(Produit produit, LocalDate datedebut, LocalDate datefin, String region, String numlot);
    
    public StockAgregate findClosedStockByLot(LocalDate today, LocalDate today1, String uid, String region, String numlot, String context);
    
    public double findCurrentStockFor(Produit produit,String region);
    
    public List<Peremption> showExpiredAtInterval(LocalDate dateExp1, LocalDate dateEpx2, String region);
    
    public StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid);
    public void cloturerUnProduit(Produit produit, String region, LocalDate datedebut, LocalDate datefin);

    public void clotureStockProduit(Produit produit, String lot,String region, LocalDate datedebut, LocalDate datefin,String context);
    public List<Recquisition> findDistinctLotHeads(Produit p, String region);
    public List<Recquisition> findOrphanRecquisitions(String prodId);
    public void fixUndesiredRecqusitionOf(LocalDate d1, LocalDate d2, String region);
    
    public List<PrixDeVente> findLastPrices(String prodId);

    public List<StockAgregate> findLatestLotStockAgregates(String productId);

    public List<StockAgregate> findLatestLotStockAgregates(String productId, String region);

    public double sumLatestLotFinalQuantityFromStockAggregate(String productId);

    public double sumLatestLotFinalQuantityFromStockAggregate(String productId, String region);
    
    public double sumLatestLotFinalQuantityFromStockAggregate(String productId,String lot, String region);
    
    public List<Recquisition> findDistinctLotsForProduitRegion(String productId, String region);

    public int verifyAndCorrectStockAggregateConsistency(String region);

    public StockAgregate updateStockAgregate(StockAgregate sa);

    public StockAgregate findStockAgregate(String prod, String numlot, String region, boolean destryed);

    /**
     * Vue SQL pour l'onglet POS : une ligne par produit, tous les calculs de
     * stock (somme des lots, lot exposé selon la méthode) réalisés dans le
     * SGBD via des JOINs. Colonne de l'Object[] :
     * 0 uid, 1 codebar, 2 nomproduit, 3 marque, 4 modele, 5 taille,
     * 6 categoryid_uid, 7 pieces (somme latest-per-lot), 8 numlot (lot exposé),
     * 9 date_expiration, 10 cout_achat, 11 mesure_id du lot exposé,
     * 12 quantcontenu de cette mesure, 13 description, 14 uid petite mesure,
     * 15 quantcontenu petite mesure.
     */
    public List<Object[]> loadPosStockView(String region, String meth, boolean global);

    /** Recquisitions (scope région) : uid, product_id, numlot, date, dateExpiry. */
    public List<Object[]> loadHeaderRecqs(String region);

    /** Recquisitions globales : uid, product_id, numlot, date, dateExpiry. */
    public List<Object[]> loadAllRecqs();

    /** Entrées par (product_id, numlot) en pièces (hors référence RTR), scope région. */
    public List<Object[]> loadRecqLotEntrees(String region);

    /** Sorties par (product_id, numlot) en pièces via ligne_vente/vente, scope région. */
    public List<Object[]> loadLigneVenteLotSorties(String region);

    /** Retours par (product_id, numlot) en pièces via retour_depot, scope région. */
    public List<Object[]> loadRetourDepotLotReturns(String region);

    /** Lignes prix : uid, q_min, prix_unitaire, mesureid_uid, recquisition_id. */
    public List<Object[]> loadPriceRows();

}
