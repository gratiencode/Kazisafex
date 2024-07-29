/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.RetourDepotStorage;
import static delegates.RetourDepotDelegate.getRetourDepotStorage;
import java.util.List;
import data.RetourDepot;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RetourDepotDelegate {
    
    public static RetourDepot saveRetourDepot(RetourDepot cat) {
        return getRetourDepotStorage().createRetourDepot(cat);
    }

    public static RetourDepot updateRetourDepot(RetourDepot cat) {
        return getRetourDepotStorage().updateRetourDepot(cat);
    }

    public static void deleteRetourDepot(RetourDepot cat) {
        getRetourDepotStorage().deleteRetourDepot(cat);
    }

    public static RetourDepot findRetourDepot(String objId) {
        return getRetourDepotStorage().findRetourDepot(objId);
    }
    
    
    public static List<RetourDepot> findRetourDepots(){
       return getRetourDepotStorage().findRetourDepots();
    }
    
     public static List<RetourDepot> findRetourDepots(int s,int m){
       return getRetourDepotStorage().findRetourDepots(s,m);
    }
    
   public static RetourDepotStorage getRetourDepotStorage(){
        RetourDepotStorage cats=(RetourDepotStorage)ServiceLocator.getInstance().getService(Tables.RETOURDEPOT);
        return cats;
    } 

    public static Long getCount() {
        return getRetourDepotStorage().getCount();
    }
}
