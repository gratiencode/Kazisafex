/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import java.util.List;
import tools.ServiceLocator;
import tools.Tables;
import IServices.CompterStorage;
import data.Compter;
import data.Inventaire;
import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public class CompterDelegate {

    public static CompterStorage getStorage() {
        CompterStorage cats = (CompterStorage) ServiceLocator.getInstance().getService(Tables.COMPTER);
        return cats;
    }
    
    public static Compter createCompter(Compter c) {
        return getStorage().createCompter(c);
    }
    
    public static Compter updateCompter(Compter c) {
        return getStorage().updateCompter(c);
    }
    
    public static void deleteCompter(Compter c) {
        getStorage().deleteCompter(c);
    }
    
    public static void closeInventoryByFixingNoCounts(Inventaire inv){
        getStorage().removeNoCountedProducts(inv);
    }
    
    public static Compter findCompter(String uid) {
        return getStorage().findCompter(uid);
    }

    public static List<Compter> findCompterBYInventaire(String inventaireId) {
        return getStorage().findComptages(inventaireId);
    }

    public static List<Compter> findCompterBYInventaire(String inventaireId, String region) {
        return getStorage().findComptages(inventaireId, region);
    }

    public static List<Compter> findUnSyncedCounts(long lastSession) {
        return getStorage().findUnSyncedCompter(lastSession);
    }
    
    public static boolean isExists(String uid,LocalDateTime time){
        return getStorage().isExists(uid, time);
    }
    
    public static boolean isExists(String uid){
        return getStorage().isExists(uid);
    }
    
    public static List<Compter> findCompterForProduct(String puid,String iuid){
         return getStorage().findComptageForProduit(puid, iuid);
    }

    public static Compter findCompterByInventaireAndProduit(String iuid, String puid) {
        return getStorage().findComptageByInventaireProduit(iuid, puid);
    }
}
