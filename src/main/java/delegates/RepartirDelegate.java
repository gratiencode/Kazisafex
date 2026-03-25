/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.RepartirStorage;
import data.Repartir;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class RepartirDelegate {

    public static Repartir saveRepartir(Repartir d) {
        return getStorage().saveRepartir(d);
    }

    public static Repartir updateRepartir(Repartir d) {
        return getStorage().updateRepartir(d);
    }

    public static Repartir findRepartir(String d) {
        return getStorage().findRepartir(d);
    }

    public static List<Repartir> findRepartirs() {
        return getStorage().findRepartirs();
    }

    public static void removeRepartir(Repartir choosenRepartir) {
        getStorage().deleteRepartir(choosenRepartir);
    }

    public static RepartirStorage getStorage() {
        RepartirStorage cats = (RepartirStorage) ServiceLocator.getInstance().getService(Tables.REPARTIR);
        return cats;
    }

    public static double findSommeRepartir(String uid) {
        return getStorage().findSommeRepartir(uid);
    }

    public static List<Repartir> findForProduction(String uid) {
         return getStorage().findForProduction(uid);
    }

    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
}
