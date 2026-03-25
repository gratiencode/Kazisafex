/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.ProductionStorage;
import data.Production;
import data.Produit;
import static delegates.AretirerDelegate.getStorage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class ProductionDelegate {
    public static Production saveProduction(Production d) {
        return getStorage().saveProduction(d);
    }

    public static Production updateProduction(Production d) {
        return getStorage().updateProduction(d);
    }
    
    public static Production findProduction(String d) {
        return getStorage().findProduction(d);
    }
    
    public static List<Production> findProductions() {
        return getStorage().findProductions();
    }

    public static void removeProduction(Production choosenProduction) {
        getStorage().deleteProduction(choosenProduction);
    }

    public static ProductionStorage getStorage() {
        ProductionStorage cats = (ProductionStorage) ServiceLocator.getInstance().getService(Tables.PRODUCTION);
        return cats;
    }

    public static List<Production> findProductionByProduitLot(String lot, String uid) {
        return getStorage().findProductionByProduitLot(lot, uid);
    }

    public static List<Production> findForProduct(Produit p, LocalDate value, LocalDate value0) {
        return getStorage().findForProduct(p,value,value0);
    }

    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
   
}
