/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.RecquisitionStorage;
import java.util.List;
import data.Recquisition;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.RecquisitionDelegate.getStorage;
import java.util.Date;
import javafx.collections.ObservableList;
import tools.ListViewItem;
import tools.Rupture;

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
    
    
    public static List<Recquisition> findRecquisitions(){
       return getStorage().findRecquisitions();
    }
    
    public static List<Recquisition> findRecquisitionByProduit(String idprod){
       return getStorage().findRecquisitionByProduit(idprod);
    }
    
    public static List<Recquisition> findRecquisitionByProduit(String idprod,String lot){
       return getStorage().findRecquisitionByProduit(idprod, lot);
    }
    
    public static List<Recquisition> findRecquisitions(int s,int m){
       return getStorage().findRecquisitions(s,m);
    }
    
    public static RecquisitionStorage getStorage(){
        RecquisitionStorage cats=(RecquisitionStorage)ServiceLocator.getInstance().getService(Tables.RECQUISITION);
        return cats;
    }

    public static List<Recquisition> findRecquisitionByProduitRegion(String uid, String region) {
       return getStorage().findRecquisitionByProduitRegion(uid,region);
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

    public static List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region) {
        return getStorage().findRecquisitionByProduit(uid, numlot, region);
    }

    public static List<Recquisition> findByDateExpInterval(Date time, Date darg) {
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


    public static Recquisition appendToTransaction(Recquisition r) {
       return getStorage().addToTransaction(r);
    }
    public static void beginTransaction() {
       getStorage().startTransaction();
    }
    public static void endTransaction() {
       getStorage().commitTransaction();
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
    
    public static double findRemainedInMagasinForBatched(String uid,String numlot) {
        return getStorage().findRemainedInMagasinForBatched(uid,numlot);
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

    public static List<Recquisition> findRecquisitionByRegionGroupBylot( String region) {
       return getStorage().findRecquisitionByRegionGroupBylot(region); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<Recquisition> findRecquisitionGroupByLot() {
       return getStorage().findRecquisitionGroupByLot();//To change body of generated methods, choose Tools | Templates.
    }

    public static List<Recquisition> findRecquisitionByRegionGroupBylot(Date debut, Date fin, String region) {
         return getStorage().findRecquisitionByRegionGroupBylot(debut,fin,region);//To change body of generated methods, choose Tools | Templates.
    }

    public static List<Recquisition> findRecquisitionGroupByLot(Date debut, Date fin) {
        return getStorage().findRecquisitionGroupByLot(debut,fin); //To change body of generated methods, choose Tools | Templates.
    }

    public static double findRemainedInMagasinByLot(String puid, String numlot) {
       return getStorage().findRemainedInMagasinByLot(puid, numlot);
    }

    public static double findRemainedInMagasinByLot(String puid, String numlot, String region) {
        return getStorage().findRemainedInMagasinByLot(puid, numlot, region);
    }

    public static double sumByProduit(String uid) {
        return getStorage().sumByProduit(uid);
    }

    public static double sumByProduit(String uid, String region) {
        return getStorage().sumByProduit(uid,region);
    }

    public static List<Recquisition> findByReference(String ref, String uid, String numlot) {
        return getStorage().findByReference(ref,uid, numlot); 
    }

    public static double sumByProduit(String idpro, Date d1, Date d2) {
       return getStorage().sumByProduit(idpro,d1,d2);
    }

    public static double sumByProduit(String idpro, Date d1, Date d2, String region) {
       return getStorage().sumByProduit(idpro, d1, d2,region);
    }

    public static List<ListViewItem> populate() {
      return getStorage().populate();
    }

    public static Recquisition findCustomized(String uid, String numlot, String ref, Date dateStocker) {
       return getStorage().findCustomized(uid,numlot,ref,dateStocker);
    }

   
}
