/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.ImmobilisationStorage;
import data.AmortissementAgregate;
import data.Immobilisation;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.prefs.Preferences;
import services.ManagedSessionFactory;
import tools.ServiceLocator;
import tools.SyncEngine;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ImmobilisationDelegate {

    public static Immobilisation saveImmobilisation(Immobilisation obj) {
        Immobilisation saved = getImmobilisationStorage().createImmobilisation(obj);
        services.AggregateTriggerService.getInstance().notifyImmobilisation(
                obj == null ? null : obj.getDateAcquisition(),
                obj == null ? null : obj.getRegion());
        return saved;
    }

    public static Immobilisation updateImmobilisation(Immobilisation obj) {
        Immobilisation saved = getImmobilisationStorage().updateImmobilisation(obj);
        services.AggregateTriggerService.getInstance().notifyImmobilisation(
                obj == null ? null : obj.getDateAcquisition(),
                obj == null ? null : obj.getRegion());
        return saved;
    }

    public static void deleteImmobilisation(Immobilisation obj) {
        List<data.ImmobilisationAgregate> ags = getAgregateStorage().findByImmobilisation(obj.getUid());
        if (ags != null && !ags.isEmpty()) {
            for (data.ImmobilisationAgregate ag : ags) {
                getAgregateStorage().deleteImmobilisationAgregate(ag);
            }
        }
        getImmobilisationStorage().deleteImmobilisation(obj);
        services.AggregateTriggerService.getInstance().notifyImmobilisation(
                obj == null ? null : obj.getDateAcquisition(),
                obj == null ? null : obj.getRegion());
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
        agregate(LocalDate.now(), null);
    }

    public static void agregate(LocalDate referenceDate, String region) {
        List<Immobilisation> lims = findImmobilisations();
        if (lims == null || lims.isEmpty()) {
            return;
        }
        LocalDate targetDate = referenceDate == null ? LocalDate.now() : referenceDate;
        LocalDateTime now = targetDate.atStartOfDay();
        clearPeriodAggregates(targetDate, region);
        for (Immobilisation im : lims) {
            if (region != null && !region.isBlank() && !region.equals(im.getRegion())) {
                continue;
            }
            data.ImmobilisationAgregate ag = new data.ImmobilisationAgregate();
            ag.setDate(now);
            ag.setImmobilisationId(im);
            ag.setRegion(im.getRegion());
            ag.setValeurBrutte(im.getValeurOrigineUsd());
            ag.setAmmortissement(im.amortissementCumulUsd(targetDate));
            ag.setValeurNette(im.valeurNetteUsd(targetDate));
            getAgregateStorage().createImmobilisationAgregate(ag);

            AmortissementAgregate amort = new AmortissementAgregate();
            amort.setPeriode(targetDate.withDayOfMonth(1));
            amort.setImmobilisationId(im);
            amort.setRegion(im.getRegion());
            amort.setDotationUsd(im.dotationMensuelleUsd());
            amort.setCumulUsd(im.amortissementCumulUsd(targetDate));
            amort.setValeurComptableUsd(im.valeurNetteUsd(targetDate));
            persistAmortissement(amort);
        }
    }

    private static void clearPeriodAggregates(LocalDate referenceDate, String region) {
        if (referenceDate == null) {
            return;
        }
        runInTransaction(em -> {
            String regionClause = region == null || region.isBlank() ? " AND region IS NULL" : " AND region = :region";
            jakarta.persistence.Query immo = em.createNativeQuery(
                    "DELETE FROM immobilisation_agregate WHERE DATE(date) = :date" + regionClause);
            immo.setParameter("date", referenceDate);
            if (region != null && !region.isBlank()) {
                immo.setParameter("region", region);
            }
            immo.executeUpdate();

            jakarta.persistence.Query amort = em.createNativeQuery(
                    "DELETE FROM amortissement_agregate WHERE periode = :periode" + regionClause);
            amort.setParameter("periode", referenceDate.withDayOfMonth(1));
            if (region != null && !region.isBlank()) {
                amort.setParameter("region", region);
            }
            amort.executeUpdate();
        });
    }

    private static void persistAmortissement(AmortissementAgregate amortissement) {
        runInTransaction(em -> em.merge(amortissement));
    }

    private static void runInTransaction(java.util.function.Consumer<EntityManager> action) {
        ManagedSessionFactory.executeWrite(em -> {
            action.accept(em);
            return null;
        });
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
