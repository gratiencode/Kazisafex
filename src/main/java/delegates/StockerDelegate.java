/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.StockerStorage;
import java.time.LocalDate;
import java.util.List;
import data.Stocker;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.StockerDelegate.getStorage;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author eroot
 */
public class StockerDelegate {

    public static Stocker saveStocker(Stocker cat) {
        return getStorage().createStocker(cat);
    }

    public static Stocker updateStocker(Stocker cat) {
        return getStorage().updateStocker(cat);
    }

    public static void deleteStocker(Stocker cat) {
        getStorage().deleteStocker(cat);
    }

    public static Stocker findStocker(String objId) {
        return getStorage().findStocker(objId);
    }

    public static List<Stocker> findStockers() {
        return getStorage().findStockers();
    }

    public static List<Stocker> findStockerByProduit(String pid) {
        return getStorage().findStockerByProduit(pid);
    }

    public static List<Stocker> findStockerByProduit(String pid, String region) {
        return getStorage().findStockerByProduit(pid, region);
    }

    public static List<Stocker> findStockerByProduitLot(String pid, String lot) {
        return getStorage().findStockerByProduitLot(pid, lot);
    }

    public static List<Stocker> findStockers(int s, int m) {
        return getStorage().findStockers(s, m);
    }

    public static List<Stocker> findStockers(String region, int s, int m) {
        return getStorage().findStockers(region, s, m);
    }

    public static StockerStorage getStorage() {
        StockerStorage cats = (StockerStorage) ServiceLocator.getInstance().getService(Tables.STOCKER);
        return cats;
    }

    public static List<Stocker> findStockerByLivraison(String uid) {
        return getStorage().findStockerByLivraison(uid);
    }

    public static List<Stocker> findByDateIntervale(LocalDate date1, LocalDate date2) {
        return getStorage().findByDateIntervale(date1, date2);
    }

    public static List<Stocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region) {
        return getStorage().findByDateIntervale(date1, date2, region);
    }

    public static List<Stocker> findAscSortedByDateExpir(String uid) {
        return getStorage().findAscSortedByDateExpir(uid);
    }

    public static List<Stocker> findAscSortedByDateExpir(String uid, String region) {
        return getStorage().findAscSortedByDateExpir(uid, region);
    }

    public static List<Stocker> findDescSortedByDateStock(String prouid) {
        return getStorage().findDescSortedByDateStock(prouid);
    }

    public static List<Stocker> findAscSortedByDateStock(String uid) {
        return getStorage().findAscSortedByDateStock(uid);
    }

    public static List<Stocker> findStockerByLivrAndProduit(String uid, String uid0) {
        return getStorage().findStockerByLivrAndProduit(uid, uid0);
    }

    public static List<Stocker> findByDateExpInterval(Date time, Date darg) {
        return getStorage().findByDateExpInterval(time, darg);
    }

    public static long getCount() {
        return getStorage().getCount();
    }

    public static List<Stocker> findStockers(String region) {
        return getStorage().findStockers(region);
    }

    public static List<Stocker> findDescSortedByDateStock(String uid, String region) {
        return getStorage().findDescSortedByDateStock(uid, region);
    }

    public static List<Stocker> mergeSet(Set<Stocker> ss) {
        return getStorage().mergeSet(ss);
    }

    public static List<Stocker> toLifoOrdering(String uid) {
        return getStorage().toLifoOrdering(uid);
    }

    public static List<Stocker> toFefoOrdering(String uid) {
        return getStorage().toFefoOrdering(uid);
    }

    public static List<Stocker> toFifoOrdering(String uid) {
        return getStorage().toFefoOrdering(uid);
    }


    public static double sum(String uid) { 
        return getStorage().sum(uid);
    }
}
