/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.RetourMagasinStorage;
import java.util.List;
import data.RetourMagasin;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RetourMagasinDelegate {
    
    public static RetourMagasin saveRetourMagasin(RetourMagasin cat) {
        return getRetourMagasinStorage().createRetourMagasin(cat);
    }

    public static RetourMagasin updateRetourMagasin(RetourMagasin cat) {
        return getRetourMagasinStorage().updateRetourMagasin(cat);
    }

    public static void deleteRetourMagasin(RetourMagasin cat) {
        getRetourMagasinStorage().deleteRetourMagasin(cat);
    }

    public static RetourMagasin findRetourMagasin(String objId) {
        return getRetourMagasinStorage().findRetourMagasin(objId);
    }
    
    
    public static List<RetourMagasin> findRetourMagasins(){
       return getRetourMagasinStorage().findRetourMagasins();
    }
    
    
    public static List<RetourMagasin> findRetourMagasins(int s,int m){
       return getRetourMagasinStorage().findRetourMagasins(s,m);
    }
    
   public static RetourMagasinStorage getRetourMagasinStorage(){
        RetourMagasinStorage cats=(RetourMagasinStorage)ServiceLocator.getInstance().getService(Tables.RETOURMAGASIN);
        return cats;
    } 

    public static Long getCount() {
        return getRetourMagasinStorage().getCount();
    }

    public static long getCountForVente(String uid) {
       return getRetourMagasinStorage().getCountForVente(uid);
    }

    public static List<RetourMagasin> findByLigneVente(Long uid) {
       return getRetourMagasinStorage().findByLigneVente(uid);
    }

    public static List<RetourMagasin> findUnSyncedRetourMagasins(long disconnected_at) {
    return getRetourMagasinStorage().findUnSyncedRetourMagasins(disconnected_at);
    }
    
    public static boolean isExists(String uid,LocalDateTime attime){
        return getRetourMagasinStorage().isExists(uid, attime);
    }
}
