/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.DestockerStorage;
import java.time.LocalDate;
import java.util.List;
import data.Destocker;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.DestockerDelegate.getStorage;
import java.time.LocalDateTime;
import java.util.Set;
import services.DestockerService;

/**
 *
 * @author eroot
 */
public class DestockerDelegate {
    
    public static Destocker saveDestocker(Destocker cat) {
        return getStorage().createDestocker(cat);
    }

    public static Destocker updateDestocker(Destocker cat) {
        return getStorage().updateDestocker(cat);
    }

    public static void deleteDestocker(Destocker cat) {
        getStorage().deleteDestocker(cat);
    }

    public static Destocker findDestocker(String objId) {
        return getStorage().findDestocker(objId);
    }
    
    
    public static List<Destocker> findDestockers(){
       return getStorage().findDestockers();
    }
    
    public static List<Destocker> findDestockers(int s,int m){
       return getStorage().findDestockers(s,m);
    }
    
    public static DestockerStorage getStorage(){
        DestockerStorage cats=(DestockerStorage)ServiceLocator.getInstance().getService(Tables.DESTOCKER);
        return cats;
    }

    public static long getCount() {
       return getStorage().getCount();
    }

    
    public static List<Destocker> findDescSortedByDate(String region, int offsetd, int intValue) {
        return getStorage().findDescSortedByDate(region,offsetd,intValue);
    }

    public static List<Destocker> findDescSortedByDate(int offsetd, int intValue) {
        return getStorage().findDescSortedByDate(offsetd,intValue);
    }
    
    public static void removeOrphans(){
        getStorage().removeOrphans();
    }

    public static List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2) {
         return getStorage().findByDateIntervale(date1,date2);
    }

    public static List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region) {
        return getStorage().findByDateIntervale(date1,date2,region);
    }

    public static List<Destocker> findByProduit(String uid) {
        return getStorage().findDestockerByProduit(uid);
    }

    public static List<Destocker> findByProduit(String uid, String region) {
        return getStorage().findDestockerByProduit(uid,region); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static List<Destocker> findByReference(String ref, String region) {
        return getStorage().findByReference(ref,region); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static List<Destocker> findByReference(String ref) {
        return getStorage().findByReference(ref); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<Destocker> findByProduitLot(String uid, String nlot) {
        return getStorage().findByProduitLot(uid,nlot);//To change body of generated methods, choose Tools | Templates.
    }

    public static List<Destocker> findByReferenceAndProduit(String uid, String ref) {
       return getStorage().findByReferenceAndProduit(uid, ref);
    }

    public static List<Destocker> findAscSortedByDate(String uid) {
       return getStorage().findAscSortedByDate(uid);
    }

    public static List<Destocker> mergeSet(Set<Destocker> ds) {
        return getStorage().mergeSet(ds);
    }

    public static List<Destocker> findByReference(String ref, String uid, String numlot) {
       return getStorage().findByReference(ref,uid,numlot);
    }

    public static double sum(String uid) {
       return getStorage().sum(uid);
    }
    
    public static List<Destocker> getDestockers(){
        return DestockerService.getDestockers();
    }

    public static Destocker findCustomised(String uid, String numlot, String ref, LocalDateTime dateStocker) { 
      return getStorage().findCustomised(uid,numlot,ref,dateStocker);
    }

    public static List<Destocker> findUnSyncedDestockers(long disconnected_at) {
       return getStorage().findUnSyncedDestockers(disconnected_at);
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
}
