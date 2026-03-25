/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import data.Traisorerie;
import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author eroot
 */
public interface TraisorerieStorage {

    public Traisorerie createTraisorerie(Traisorerie obj);

    public Traisorerie updateTraisorerie(Traisorerie obj);

    public void deleteTraisorerie(Traisorerie obj);

    public Long getCount();

    public Traisorerie findTraisorerie(String objId);

    public List<Traisorerie> findTraisoreries();

    public List<Traisorerie> findTraisoreries(String region);

    public List<Traisorerie> findTraisoreries(int start, int max);

    public List<Traisorerie> findTraisorerieByCompteTresor(String objId);

    public List<Traisorerie> findTraisorerieByCompteTresor(String objId, String typeCpte);

    public List<Traisorerie> findTraisorerieByCompteTresOR(String objId, String typeCpte);

    public List<Traisorerie> findByReference(String ref);

    public Double sumByReference(String ref, double taux);

    public List<Traisorerie> mergeSet(Set<Traisorerie> bulk);

    public double getCreditForCustomer(String clientId);

    public double findCurrentBalanceCdf(String tuid, LocalDate date, LocalDate date2, String region);

    public double findCurrentBalanceUsd(String tuid, LocalDate date, LocalDate date2, String region);

    public double soldeCdfOnPeriod(String uid, LocalDate d1, LocalDate d2, String region);

    public double soldeUsdOnPeriod(String uid, LocalDate d1, LocalDate d2, String region);

    public Traisorerie findExistingOf(String ref, LocalDate date, String tresorId, String region);

    public List<Traisorerie> findUnSyncedTraisoreries(long disconnected_at);

    public List<Traisorerie> findTraisorByCompteTresor(String uid, LocalDate d1, LocalDate d2, String region);

    public boolean isExists(String uid);

    public boolean isExists(String uid, LocalDateTime atime);

    public double getTotalBankDebt();
}
