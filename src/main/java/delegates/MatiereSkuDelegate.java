/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.MatiereSkuStorage;
import data.MatiereSku;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class MatiereSkuDelegate {
    public static MatiereSku saveMatiereSku(MatiereSku d) {
        return getStorage().saveMatiereSku(d);
    }

    public static MatiereSku updateMatiereSku(MatiereSku d) {
        return getStorage().updateMatiereSku(d);
    }
    
    public static MatiereSku findMatiereSku(String d) {
        return getStorage().findMatiereSku(d);
    }
    
    public static List<MatiereSku> findMatiereSkus() {
        return getStorage().findMatiereSkus();
    }

    public static void removeMatiereSku(MatiereSku choosenMatiereSku) {
        getStorage().deleteMatiereSku(choosenMatiereSku);
    }

    public static MatiereSkuStorage getStorage() {
        MatiereSkuStorage cats = (MatiereSkuStorage) ServiceLocator.getInstance().getService(Tables.MATIERESKU);
        return cats;
    }

    public static List<MatiereSku> findMatiereSkuFor(String uid) {
     return getStorage().findMatiereSkuFor(uid);
    }


    public static MatiereSku findMatiereSku(String name, double q, String uid) {
        return getStorage().findMatiereSku(name,q,uid);
    }

    public static MatiereSku findMatiereSku(String txt, String uid) {
        return getStorage().findMatiereSku(txt,uid);
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
}
