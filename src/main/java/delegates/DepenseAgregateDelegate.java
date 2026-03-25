/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.DepenseAgregateStorage;
import java.util.List;
import data.DepenseAgregate;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class DepenseAgregateDelegate {
    
    public static DepenseAgregate saveDepenseAgregate(DepenseAgregate cat) {
        return getDepenseAgregateStorage().createDepenseAgregate(cat);
    }

    public static DepenseAgregate updateDepenseAgregate(DepenseAgregate cat) {
        return getDepenseAgregateStorage().updateDepenseAgregate(cat);
    }

    public static void deleteDepenseAgregate(DepenseAgregate cat) {
        getDepenseAgregateStorage().deleteDepenseAgregate(cat);
    }

    public static DepenseAgregate findDepenseAgregate(String objId) {
        return getDepenseAgregateStorage().findDepenseAgregate(objId);
    }
    
    public static List<DepenseAgregate> findDepenseAgregates(){
       return getDepenseAgregateStorage().findDepenseAgregates();
    }
    
     public static List<DepenseAgregate> findDepenseAgregates(String region){
       return getDepenseAgregateStorage().findDepenseAgregates(region);
    }
    
    public static List<DepenseAgregate> findDepenseAgregates(int s,int m){
       return getDepenseAgregateStorage().findDepenseAgregates(s,m);
    }
    
    public static List<DepenseAgregate> findDepenseAgregates(LocalDateTime date, String imputation){
       return getDepenseAgregateStorage().findDepenseAgregates(date, imputation);
    }
    
    public static DepenseAgregate aggregateDepense(LocalDateTime date, String imputation, Double usd, Double cdf, data.Depense depense) {
        List<DepenseAgregate> existingList = findDepenseAgregates(date, imputation);
        if (existingList != null && !existingList.isEmpty()) {
            DepenseAgregate existing = existingList.get(0);
            if (existing.getDepenseId() == null && depense != null) {
                existing.setDepenseId(depense);
            }
            existing.setMontantUsd((existing.getMontantUsd() == null ? 0.0 : existing.getMontantUsd()) + (usd == null ? 0.0 : usd));
            existing.setMontantCdf((existing.getMontantCdf() == null ? 0.0 : existing.getMontantCdf()) + (cdf == null ? 0.0 : cdf));
            return updateDepenseAgregate(existing);
        } else {
            DepenseAgregate newData = new DepenseAgregate();
            newData.setUid(tools.DataId.generate());
            newData.setDate(date);
            newData.setImputation(imputation);
            newData.setMontantUsd(usd == null ? 0.0 : usd);
            newData.setMontantCdf(cdf == null ? 0.0 : cdf);
            if (depense != null) {
                newData.setDepenseId(depense);
            }
            return saveDepenseAgregate(newData);
        }
    }
    
   public static DepenseAgregateStorage getDepenseAgregateStorage(){
        // Note: Assuming ServiceLocator is updated with Tables.DEPENSE_AGREGATE or we instantiate directly.
        // For simplicity and to avoid modifying tools.Tables if it's an enum we shouldn't touch, 
        // we will directly instantiate it or assume it's added. Let's use a workaround if Tables is enum.
        return new services.DepenseAgregateService();
    } 

    public static Long getCount() {
        return getDepenseAgregateStorage().getCount();
    }

    public static List<DepenseAgregate> findUnSyncedDepenseAgregates(long disconnected_at) {
        return getDepenseAgregateStorage().findUnSyncedDepenseAgregates(disconnected_at);
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getDepenseAgregateStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getDepenseAgregateStorage().isExists(uid);
    }
}
