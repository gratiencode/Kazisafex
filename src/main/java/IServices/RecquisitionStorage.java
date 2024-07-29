/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.Date;
import java.util.List;
import java.util.Set;
import data.Recquisition;
import javafx.collections.ObservableList;
import tools.ListViewItem;
import tools.Rupture;

/**
 *
 * @author eroot
 */
public interface RecquisitionStorage {
   public Recquisition createRecquisition(Recquisition obj);
    public Recquisition updateRecquisition(Recquisition obj);
    public void deleteRecquisition(Recquisition obj);
    public Long getCount();
    public Recquisition findRecquisition(String objId);
    public List<Recquisition> findRecquisitions();
    public List<Recquisition> findRecquisitions(int start,int max);
    public List<Recquisition> findRecquisitionByProduit(String objId);  
    public List<Recquisition> findRecquisitionByProduit(String objId,String lot);  

    public List<Recquisition> findRecquisitionByProduitRegion(String uid, String region);

    public List<Recquisition> findDescSortedByDateForProduit(String uid);

    public List<Recquisition> toFefoOrdering(String uid);

    public List<Recquisition> toFifoOrdering(String uid);

    public List<Recquisition> toLifoOrdering(String uid);

    public List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region);

    public List<Recquisition> findByDateExpInterval(Date time, Date darg);

    public List<Object[]> findGoods();

    public List<Object[]> findGoodsFromRegion(String region);

    public List<Object[]> findGoodsCategorized(String cat);

    public List<Object[]> findGoodsCategorized(String cat, String region);

    public List<Recquisition> findRecquisitions(String region);
    
    public Recquisition addToTransaction(Recquisition r);
    public void startTransaction();
    public void commitTransaction();

    public double sumByProduitWithLotInUnit(String idpro, String lot);

    public List<Recquisition> findByReference(String ref);

    public List<Recquisition> findByReference(String uid, String ref);

    public double findRemainedInMagasinFor(String uid);
    public double findRemainedInMagasinFor(String uid,String region);
    public List<Rupture> findStockEnRupture();

    public List<Rupture> findStockEnRupture(String region);

    public List<Recquisition> findRecquisitionByRegionGroupBylot(String region);

    public List<Recquisition> findRecquisitionGroupByLot();

    public List<Recquisition> findRecquisitionByRegionGroupBylot(Date debut, Date fin, String region);

    public List<Recquisition> findRecquisitionGroupByLot(Date debut, Date fin);

    public double findRemainedInMagasinByLot(String puid, String numlot);

    public double findRemainedInMagasinByLot(String puid, String numlot, String region);

    public double sumByProduit(String uid);

    public double sumByProduit(String uid, String region);
     public List<Recquisition> mergeSet(Set<Recquisition> bulk);

    public List<Recquisition> findByReference(String ref, String uid, String numlot);

    public double sumByProduit(String idpro, Date d1, Date d2);

    public double sumByProduit(String idpro, Date d1, Date d2, String region);

    public List<ListViewItem> populate();

    public Recquisition findCustomized(String uid, String numlot, String ref, Date dateStocker);

    public double findRemainedInMagasinForBatched(String uid, String numlot);


   

}
