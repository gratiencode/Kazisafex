/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.PrixDeVenteStorage;
import java.util.List;
import data.PrixDeVente;
import tools.ServiceLocator;
import tools.Tables;
import data.Mesure;
import data.Recquisition;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author eroot
 */
public class PrixDeVenteDelegate {
    
    public static PrixDeVente savePrixDeVente(PrixDeVente cat) {
        return getStorage().createPrixDeVente(cat);
    }

    public static PrixDeVente updatePrixDeVente(PrixDeVente cat) {
        return getStorage().updatePrixDeVente(cat);
    }

    public static void deletePrixDeVente(PrixDeVente cat) {
        getStorage().deletePrixDeVente(cat);
    }

    public static PrixDeVente findPrixDeVente(String objId) {
        return getStorage().findPrixDeVente(objId);
    }
    
    
    public static List<PrixDeVente> findPrixDeVentes(){
       return getStorage().findPrixDeVentes();
    }
    
    
     public static List<PrixDeVente> findPrixDeVentes(int start,int max){
       return getStorage().findPrixDeVentes(start,max);
    }
    
    public static PrixDeVenteStorage getStorage(){
        PrixDeVenteStorage cats=(PrixDeVenteStorage)ServiceLocator.getInstance().getService(Tables.PRIXDEVENTE);
        return cats;
    }

    public static List<PrixDeVente> findPricesForRecq(String uid) {
        
       return getStorage().findPricesForRecq(uid);
    }
    
    public static Set<PrixDeVente> findPricesSetForRecq(String uid) {
        Set<PrixDeVente> set=new HashSet<>();
        set.addAll(getStorage().findPricesForRecq(uid));
       return set;
    }

    public static List<PrixDeVente> findSpecificByQuant(Recquisition choosenRecquisition, Mesure choosenmez, double quant) {
        return getStorage().findSpecificByQuant(choosenRecquisition, choosenmez, quant);
    }

    public static List<PrixDeVente> findDescSortedByRecqWithMesureByPrice(String req, String uid) {
        return getStorage().findDescSortedByRecqWithMesureByPrice(req, uid);
    }

    public static List<PrixDeVente> findDescOrderdByPriceForRecq(String req) {
        return getStorage().findDescOrderdByPriceForRecq(req);
    }

    public static void endTransaction() {
        getStorage().commitTransaction();
    }

    public static PrixDeVente appendToTransaction(PrixDeVente lpv) {
        return getStorage().addToTransaction(lpv);
    }

    public static void beginTransaction() {
        getStorage().startTransaction();
    }

    public static Long getCount() {
       return getStorage().getCount();
    }

    public static List<PrixDeVente> findPrixDeVentes(Double qmin, String uid, String uid0) {
       return getStorage().findPrixDeVente(qmin,uid,uid0);
    }
    
    public static List<PrixDeVente> findPrixDeVentes(Double qmin, double quantContenuMesure, String recquisId) {
       return getStorage().findPrixDeVentes(qmin, quantContenuMesure, recquisId);
    }
    
    public static List<PrixDeVente> findPrixDeVentes(double qmin, double qmax, double quantContenuMesure, String recquisId) {
       return getStorage().findPrixDeVentes(qmin, qmax, quantContenuMesure, recquisId);
    }

    public static List<PrixDeVente> findUnSyncedPrixDeVentes(long disconnected_at) {
      return getStorage().findUnSyncedPrixDeVentes(disconnected_at);  
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
}
