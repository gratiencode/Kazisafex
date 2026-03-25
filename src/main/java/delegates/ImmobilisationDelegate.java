/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.ImmobilisationStorage;
import data.Immobilisation;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ImmobilisationDelegate {

    public static Immobilisation saveImmobilisation(Immobilisation obj) {
        return getImmobilisationStorage().createImmobilisation(obj);
    }

    public static Immobilisation updateImmobilisation(Immobilisation obj) {
        return getImmobilisationStorage().updateImmobilisation(obj);
    }

    public static void deleteImmobilisation(Immobilisation obj) {
        getImmobilisationStorage().deleteImmobilisation(obj);
    }

    public static Immobilisation findImmobilisation(String objId) {
        return getImmobilisationStorage().findImmobilisation(objId);
    }

    public static List<Immobilisation> findImmobilisations() {
        return getImmobilisationStorage().findImmobilisations();
    }

    public static List<Immobilisation> findImmobilisations(int s, int m) {
        return getImmobilisationStorage().findImmobilisations(s, m);
    }

    public static List<Immobilisation> findImmobilisationByRegion(String region) {
        return getImmobilisationStorage().findImmobilisationByRegion(region);
    }

    public static List<Immobilisation> findUnSynced(long since) {
        return getImmobilisationStorage().findUnSynced(since);
    }

    public static boolean isExists(String uid) {
        return getImmobilisationStorage().isExists(uid);
    }

    public static ImmobilisationStorage getImmobilisationStorage() {
        return (ImmobilisationStorage) ServiceLocator.getInstance()
                .getService(Tables.IMMOBILISATION);
    }

    public static Long getCount() {
        return getImmobilisationStorage().getCount();
    }

    public static List<Immobilisation> findUnSyncedImmobilisations(long disconnected_at) {
        return getImmobilisationStorage().findUnSyncedImmobilisations(disconnected_at);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getImmobilisationStorage().isExists(uid, attime);
    }
}
