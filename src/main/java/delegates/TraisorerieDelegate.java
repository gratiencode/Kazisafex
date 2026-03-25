/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.TraisorerieStorage;
import java.util.List;
import data.Traisorerie;
import java.time.LocalDate;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class TraisorerieDelegate {

    public static Traisorerie saveTraisorerie(Traisorerie cat) {
        return getTraisorerieStorage().createTraisorerie(cat);
    }

    public static Traisorerie updateTraisorerie(Traisorerie cat) {
        return getTraisorerieStorage().updateTraisorerie(cat);
    }

    public static void deleteTraisorerie(Traisorerie cat) {
        getTraisorerieStorage().deleteTraisorerie(cat);
    }

    public static Traisorerie findTraisorerie(String objId) {
        return getTraisorerieStorage().findTraisorerie(objId);
    }

    public static List<Traisorerie> findTraisoreries() {
        return getTraisorerieStorage().findTraisoreries();
    }

    public static List<Traisorerie> findTraisoreries(String region) {
        return getTraisorerieStorage().findTraisoreries(region);
    }

    public static List<Traisorerie> findTraisorByCompteTresor(String cptId) {
        return getTraisorerieStorage().findTraisorerieByCompteTresor(cptId);
    }

    public static List<Traisorerie> findTraisoreries(int s, int m) {
        return getTraisorerieStorage().findTraisoreries(s, m);
    }

    public static TraisorerieStorage getTraisorerieStorage() {
        TraisorerieStorage cats = (TraisorerieStorage) ServiceLocator.getInstance().getService(Tables.TRAISORERIE);
        return cats;
    }

    public static List<Traisorerie> findTraisorByCompteTresor(String cuid, String name) {
        return getTraisorerieStorage().findTraisorerieByCompteTresor(cuid, name);
    }

    public static List<Traisorerie> findTraisorByCompteTresOR(String cuid, String name) {
        return getTraisorerieStorage().findTraisorerieByCompteTresOR(cuid, name);
    }

    public static Double sumByReference(String numero, double taux2change) {
        return getTraisorerieStorage().sumByReference(numero, taux2change);
    }

    public static Long getCount() {
        return getTraisorerieStorage().getCount();
    }

    public static double findCurrentBalanceCdf(String tuid, LocalDate date,LocalDate date2, String region) {
        return getTraisorerieStorage().findCurrentBalanceCdf(tuid, date,date2, region);
    }

    public static double findCurrentBalanceUsd(String tuid, LocalDate date,LocalDate date2, String region) {
        return getTraisorerieStorage().findCurrentBalanceUsd(tuid, date,date2, region);
    }

    public static Traisorerie findExistingOf(String ref, LocalDate date, String tresorId, String region) {
        return getTraisorerieStorage().findExistingOf(ref, date, tresorId, region);
    }

    public static double soldeCdfOnPeriod(String uid, LocalDate d1, LocalDate d2, String region) {
        return getTraisorerieStorage().soldeCdfOnPeriod(uid, d1, d2, region);
    }

    public static double soldeUsdOnPeriod(String uid, LocalDate d1, LocalDate d2, String region) {
        return getTraisorerieStorage().soldeUsdOnPeriod(uid, d1, d2, region);
    }

    public static double getCurentCreance() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static double getCreditForCustomer(String clientId) {
        return getTraisorerieStorage().getCreditForCustomer(clientId);
    }

    public static List<Traisorerie> findByRef(String reference) {
        return getTraisorerieStorage().findByReference(reference);
    }

    public static List<Traisorerie> findUnSyncedTraisoreries(long disconnected_at) {
        return getTraisorerieStorage().findUnSyncedTraisoreries(disconnected_at);
    }

    public static List<Traisorerie> findTraisorByCompteTresor(String uid, LocalDate d1, LocalDate d2, String region) {
        return getTraisorerieStorage().findTraisorByCompteTresor(uid, d1, d2, region);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getTraisorerieStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getTraisorerieStorage().isExists(uid);
    }

    public static double getTotalBankDebt() {
        return getTraisorerieStorage().getTotalBankDebt();
    }
}
