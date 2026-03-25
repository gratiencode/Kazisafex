/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.LivraisonStorage;
import java.util.List;
import data.Livraison;
import tools.ServiceLocator;
import tools.Tables;
import java.time.LocalDateTime;
import java.util.Set;

/**
 *
 * @author eroot
 */
public class LivraisonDelegate {
    
    public static Livraison saveLivraison(Livraison cat) {
        return getStorage().createLivraison(cat);
    }

    public static Livraison updateLivraison(Livraison cat) {
        return getStorage().updateLivraison(cat);
    }

    public static void deleteLivraison(Livraison cat) {
        getStorage().deleteLivraison(cat);
    }

    public static Livraison findLivraison(String objId) {
        return getStorage().findLivraison(objId);
    }
    
    
    public static List<Livraison> findLivraisons(){
       return getStorage().findLivraisons();
    }
    
    public static List<Livraison> findLivraisons(int s,int m){
       return getStorage().findLivraisons(s,m);
    }
    
    public static LivraisonStorage getStorage(){
        LivraisonStorage cats=(LivraisonStorage)ServiceLocator.getInstance().getService(Tables.LIVRAISON);
        return cats;
    }

    public static long getCount() {
       return getStorage().getCount();
    }


    public static List<Livraison> findDescSortedByDate(String region, int offset, int intValue) {
         return getStorage().findDescSortedByDate(region,offset,intValue);  
    }

    public static List<Livraison> findDescSortedByDate(int offset, int intValue) {
        return getStorage().findDescSortedByDate(offset,intValue);
    }

    public static List<Livraison> findDescSortedByDate() {
        return getStorage().findDescSortedByDate();
    }

    public static List<Livraison> findBySupplier(String fuid) {
        return getStorage().findLivraisonBySupplier(fuid);
    }
    
    public static List<Livraison> findDescSortedByDate(String region) {
        return getStorage().findDescSortedByDate(region);
    }

    public static Double sumBySupplier(String fuid) {
        return getStorage().sumBySupplier(fuid);
    }

    public static List<Livraison> mergeSet(Set<Livraison> ls) {
       return getStorage().mergeSet(ls);
    }
    
    public static List<Livraison> findByRef(String ref) {
       return getStorage().findLivraisonByReference(ref);
    }

    public static List<Livraison> findUnSyncedLivraisons(long disconnected_at) {
       return getStorage().findUnSyncedLivraisons(disconnected_at);
    } 
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
    
}
