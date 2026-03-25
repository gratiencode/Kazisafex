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

    public List<Recquisition> findRecquisitionByProduit(String objId, String lot);

    public List<Recquisition> findRecquisitionByProduitRegion(String uid, String region);

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

    public Recquisition getLastEntry(String meth, Produit prod, String region);

    public double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region);

    public double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region);

    public double calculerStockInitialEnUnite(String uid, LocalDate datedebut, String region);

    public double getStockExpiree(String uid, LocalDate datedebut, LocalDate datefin, String region);

    public boolean cloturerStocks(String region, LocalDate datedebut, LocalDate datefin,String context);
    
    public void rectifyStock(Produit produit,LocalDate datedebut,LocalDate datefin,String region,double coutAch);
    
    public double findCurrentStockFor(Produit produit,String region);
    
    public List<Peremption> showExpiredAtInterval(LocalDate dateExp1, LocalDate dateEpx2, String region);
    
    public StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid);

    public Recquisition clotureStockProduit(Produit produit, String region, LocalDate datedebut, LocalDate datefin,String context);
    
    public List<Recquisition> findOrphanRecquisitions(String prodId);
    
    public List<PrixDeVente> findLastPrices(String prodId);

}
