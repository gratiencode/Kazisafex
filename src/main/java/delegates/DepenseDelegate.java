/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.DepenseStorage;
import static delegates.DepenseDelegate.getDepenseStorage;
import java.util.List;
import data.Depense;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class DepenseDelegate {
    
    public static Depense saveDepense(Depense cat) {
        return getDepenseStorage().createDepense(cat);
    }

    public static Depense updateDepense(Depense cat) {
        return getDepenseStorage().updateDepense(cat);
    }

    public static void deleteDepense(Depense cat) {
        getDepenseStorage().deleteDepense(cat);
    }

    public static Depense findDepense(String objId) {
        return getDepenseStorage().findDepense(objId);
    }
    
    
    public static List<Depense> findDepenses(){
       return getDepenseStorage().findDepenses();
    }
    
     public static List<Depense> findDepenses(String region){
       return getDepenseStorage().findDepenses(region);
    }
    
    public static List<Depense> findDepenses(int s,int m){
       return getDepenseStorage().findDepenses(s,m);
    }
    
   public static DepenseStorage getDepenseStorage(){
        DepenseStorage cats=(DepenseStorage)ServiceLocator.getInstance().getService(Tables.DEPENSE);
        return cats;
    } 

    public static Long getCount() {
        return getDepenseStorage().getCount();
    }

    public static List<Depense> findUnSyncedDepenses(long disconnected_at) {
        return getDepenseStorage().findUnSyncedDepenses(disconnected_at);
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getDepenseStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getDepenseStorage().isExists(uid);
    }
}
