/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.ImputerStorage;
import data.Depense;
import data.Imputer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class ImputerDelegate {

    public static Imputer saveImputer(Imputer d) {
        return getStorage().saveImputer(d);
    }

    public static Imputer updateImputer(Imputer d) {
        return getStorage().updateImputer(d);
    }

    public static Imputer findImputer(String d) {
        return getStorage().findImputer(d);
    }

    public static List<Imputer> findImputers() {
        return getStorage().findImputers();
    }

    public static void removeImputer(Imputer choosenImputer) {
        getStorage().deleteImputer(choosenImputer);
    }

    public static ImputerStorage getStorage() {
        ImputerStorage cats = (ImputerStorage) ServiceLocator.getInstance().getService(Tables.IMPUTER);
        return cats;
    }

    public static List<Imputer> findForProduction(String uid) {
        return getStorage().findForProduction(uid);
    }

    public static List<Imputer> findByDateInterval(Depense value, LocalDate value0, LocalDate value1) {
        return getStorage().findByDateInterval(value, value0, value1);
    }

    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }
}
