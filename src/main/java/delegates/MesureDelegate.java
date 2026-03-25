/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.MesureStorage;
import static delegates.MesureDelegate.getMesureStorage;
import java.util.List;
import java.util.Set;
import data.Mesure;
import static delegates.AretirerDelegate.getStorage;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class MesureDelegate {

    public static Mesure saveMesure(Mesure cat) {
        return getMesureStorage().createMesure(cat);
    }

    public static Mesure updateMesure(Mesure cat) {
        return getMesureStorage().updateMesure(cat);
    }

    public static void deleteMesure(Mesure cat) {
        getMesureStorage().deleteMesure(cat);
    }

    public static Mesure findMesure(String objId) {
        return getMesureStorage().findMesure(objId);
    }

    public static List<Mesure> findMesures() {
        return getMesureStorage().findMesures();
    }

    public static List<Mesure> findMesures(int s, int m) {
        return getMesureStorage().findMesures(s, m);
    }

    public static List<Mesure> findMesureByProduit(String prod) {
        return getMesureStorage().findByProduit(prod);
    }

    public static List<Mesure> findMesureByProduit(String prod, String desc) {
        return getMesureStorage().findByProduit(prod, desc);
    }

    public static MesureStorage getMesureStorage() {
        MesureStorage cats = (MesureStorage) ServiceLocator.getInstance().getService(Tables.MESURE);
        return cats;
    }

    public static List<Mesure> findAscSortedByQuantWithProduit(String uid) {
        return getMesureStorage().findAscSortedByQuantWithProduit(uid);
    }

    public static Mesure findMaxMesureByProduit(String uid) {
        return getMesureStorage().findMaxMesureByProduit(uid);
    }

    public static Mesure findByProduitAndQuant(String uid, Double quantContenu) {
        return getMesureStorage().findByProduitAndQuant(uid, quantContenu);
    }

    public static Long getCount() {
        return getMesureStorage().getCount();
    }

    public static List<Mesure> mergeSet(Set<Mesure> ms) {
        return getMesureStorage().mergeSet(ms);
    }

    public static List<Mesure> findByProduitAndQuantContenu(String uid, double quantM) {
        return getMesureStorage().findByProduitAndQuantContenu(uid, quantM);
    }

    public static List<Mesure> findUnSyncedMesure(long disconnected_at) {
        return getMesureStorage().findUnSyncedMesure(disconnected_at);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getMesureStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getMesureStorage().isExists(uid);
    }

}
