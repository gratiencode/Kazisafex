/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.EntreposerStorage;
import data.Entreposer;
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
public class EntreposerDelegate {

    public static Entreposer saveEntreposer(Entreposer d) {
        return getStorage().saveEntreposer(d);
    }

    public static Entreposer updateEntreposer(Entreposer d) {
        return getStorage().updateEntreposer(d);
    }

    public static Entreposer findEntreposer(String d) {
        return getStorage().findEntreposer(d);
    }

    public static List<Entreposer> findEntreposers() {
        return getStorage().findEntreposers();
    }

    public static void removeEntreposer(Entreposer choosenEntreposer) {
        getStorage().deleteEntreposer(choosenEntreposer);
    }

    public static EntreposerStorage getStorage() {
        EntreposerStorage cats = (EntreposerStorage) ServiceLocator.getInstance().getService(Tables.ENTREPOSER);
        return cats;
    }

    public static double sumValueStockMP() {
        return getStorage().sumValueStockMP();
    }

    public static List<Entreposer> findEntreposersGroupedByIntrant() {
        return getStorage().findEntreposersGroupedByIntrant();
    }

    public static List<Entreposer> findEntreposerByLevel(String mp) {
        return getStorage().findEntreposerByLevel(mp);
    }

    public static double findSommeEntree(String uid) {
        return getStorage().findSommeEntree(uid);
    }

    public static List<Entreposer> toFefoOrdering(String matiereId) {
        return getStorage().toFefoOrdering(matiereId);
    }

    public static List<Entreposer> toFifoOrdering(String matiereId) {
        return getStorage().toFifoOrdering(matiereId);
    }

    public static List<Entreposer> toLifoOrdering(String matiereId) {
        return getStorage().toLifoOrdering(matiereId);
    }

    public static List<Entreposer> toFefoOrderingProd(String matiereId) {
        return getStorage().toFefoOrderingProd(matiereId);
    }

    public static List<Entreposer> toFifoOrderingProd(String matiereId) {
        return getStorage().toFifoOrderingProd(matiereId);
    }

    public static List<Entreposer> toLifoOrderingProd(String matiereId) {
        return getStorage().toLifoOrderingProd(matiereId);
    }

    public static List<Entreposer> toFefoOrdering(String matiereId, String region) {
        return getStorage().toFefoOrdering(matiereId, region);
    }

    public static List<Entreposer> toFifoOrdering(String matiereId, String region) {
        return getStorage().toFifoOrdering(matiereId, region);
    }

    public static List<Entreposer> toLifoOrdering(String matiereId, String region) {
        return getStorage().toLifoOrdering(matiereId, region);
    }

    public static List<Entreposer> findEntreposerByLevel(LocalDate value,
            LocalDate value0, String MANUFACTURING_LEVEL_RAW_MATERIAL) {
        return getStorage().findEntreposerByLevel(value, value0, MANUFACTURING_LEVEL_RAW_MATERIAL);
    }

    public static List<Entreposer> findEntreposersGroupedByIntrant(LocalDate value, LocalDate value0) {
        return getStorage().findEntreposersGroupedByIntrant(value, value0);
    }

    public static List<Entreposer> findProdEntreposers(String uid, String numlot) {
        return getStorage().findProdEntreposers(uid, numlot);
    }

    public static List<Entreposer> findProdEntreposers(String prouid) {
        return getStorage().findProdEntreposers(prouid);
    }

    public static List<Entreposer> findByProduction(String uid) {
        return getStorage().findByProduction(uid);
    }

    public List<Entreposer> findFinishedProduction() {
        return getStorage().findFinishedProduction();
    }

    public static List<Entreposer> findEntreposersGroupedByProd(LocalDate value, LocalDate value0) {
        return getStorage().findEntreposersGroupedByProd(value, value0);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

}
