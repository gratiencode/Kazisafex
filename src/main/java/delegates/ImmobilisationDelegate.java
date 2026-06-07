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
        List<data.ImmobilisationAgregate> ags = getAgregateStorage().findByImmobilisation(obj.getUid());
        if (ags != null && !ags.isEmpty()) {
            for (data.ImmobilisationAgregate ag : ags) {
                getAgregateStorage().deleteImmobilisationAgregate(ag);
            }
        }
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

    public static IServices.ImmobilisationAgregateStorage getAgregateStorage() {
        return (IServices.ImmobilisationAgregateStorage) ServiceLocator.getInstance()
                .getService(Tables.IMMOBILISATION_AGREGATE);
    }

    public static void agregate() {
        List<Immobilisation> lims = findImmobilisations();
        if (lims == null || lims.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Immobilisation im : lims) {
            data.ImmobilisationAgregate ag = new data.ImmobilisationAgregate();
            ag.setDate(now);
            ag.setImmobilisationId(im);
            ag.setRegion(im.getRegion());
            ag.setValeurBrutte(im.getValeurOrigineUsd());
            ag.setAmmortissement(im.amortissementCumulUsd(now.toLocalDate()));
            ag.setValeurNette(im.valeurNetteUsd(now.toLocalDate()));
            getAgregateStorage().createImmobilisationAgregate(ag);
        }
    }

    public static boolean shouldAgregate() {
        List<data.ImmobilisationAgregate> last = getAgregateStorage().findImmobilisationAgregates(0, 1);
        if (last == null || last.isEmpty()) {
            return true;
        }
        LocalDateTime lastDate = last.get(0).getDate();
        return java.time.Duration.between(lastDate, LocalDateTime.now()).toDays() >= 15;
    }
}
