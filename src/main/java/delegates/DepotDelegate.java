/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import data.Depot;
import tools.ServiceLocator;
import tools.Tables;
import IServices.DepotStorage;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class DepotDelegate {

    public static Depot saveDepot(Depot d) {
        return getStorage().saveDepot(d);
    }

    public static Depot updateDepot(Depot d) {
        return getStorage().updateDepot(d);
    }
    
    public static Depot findDepot(String d) {
        return getStorage().findDepot(d);
    }
    
    public static List<Depot> findDepots() {
        return getStorage().findDepots();
    }

    public static void removeDepot(Depot choosenDepot) {
        getStorage().deleteDepot(choosenDepot);
    }

    public static DepotStorage getStorage() {
        DepotStorage cats = (DepotStorage) ServiceLocator.getInstance().getService(Tables.DEPOT);
        return cats;
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

}
