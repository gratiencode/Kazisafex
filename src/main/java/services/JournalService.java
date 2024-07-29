/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.JournalStorage;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Journal;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 *
 * @author eroot
 */
public class JournalService implements JournalStorage {

    EntityManager em;

    public JournalService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    @Override
    public Journal createJournal(Journal cat) {
        try {
            EntityTransaction etr = em.getTransaction();
            if (!etr.isActive()) {
                etr.begin();
            }
            em.merge(cat);
            etr.commit();
            return cat;
        } catch (Exception e) {
            if (isUniqueConstraintViolation(e)) {
                System.out.println("Duplicate...");
            }
            return null;
        }
    }

    @Override
    public Journal updateJournal(Journal cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }// em.getTransaction();
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteJournal(Journal obj) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(obj));
        etr.commit();
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM Journal");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public Journal findJournal(long objId) {
        return em.find(Journal.class, objId);
    }

    @Override
    public List<Journal> findJournalFor(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.stringuid = ?");
            return em.createNativeQuery(sb.toString(), Journal.class).setParameter(1, objId).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Journal> findJournalFor(long objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.longuid = ?");
            return em.createNativeQuery(sb.toString(), Journal.class).setParameter(1, objId).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Journal> findJournalFor(int objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.intuid = ?");
            return em.createNativeQuery(sb.toString(), Journal.class).setParameter(1, objId).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Journal> findSyncedJournal() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.synced = ?");
            return em.createNativeQuery(sb.toString(), Journal.class).setParameter(1, true).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Journal> findNonSyncedJournal() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.synced = ?");
            return em.createNativeQuery(sb.toString(), Journal.class).setParameter(1, false).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Journal> findJournals() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal");
            return em.createNativeQuery(sb.toString(), Journal.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Journal> findJournalInPeriod(Date date1, Date date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.syncTime BETWEEN ? AND ?");
            return em.createNativeQuery(sb.toString(), Journal.class)
                    .setParameter(1, date1, TemporalType.DATE)
                    .setParameter(2, date2, TemporalType.DATE).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isStringRecordSynced(String suid) {
        boolean resp = false;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.synced = ? AND j.stringuid = ? ");
            List<Journal> resultList = SafeConnectionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), Journal.class)
                    .setParameter(1, true).setParameter(2, suid).getResultList();
            resp = !resultList.isEmpty();
        } catch (NoResultException e) {

        }
        return resp;
    }

    @Override
    public boolean isLongRecordSynced(long luid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.synced = ? AND j.longuid = ? ");
            List<Journal> resultList = SafeConnectionFactory.getEntityManager().createNativeQuery(sb.toString(), Journal.class)
                    .setParameter(1, true).setParameter(2, luid).getResultList();
            return !resultList.isEmpty();
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public boolean isIntRecordSynced(int iuid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM journal j WHERE j.synced = ? AND j.intuid = ? ");
            List<Journal> resultList = SafeConnectionFactory.getEntityManager().createNativeQuery(sb.toString(), Journal.class)
                    .setParameter(1, true)
                    .setParameter(2, iuid).getResultList();
            return !resultList.isEmpty();
        } catch (NoResultException e) {
            return false;
        }
    }

    private List<Journal> checkStringIfCreated(Journal j) {
        Query query = em
                .createNativeQuery("SELECT * FROM journal j WHERE j.actionname = ? AND j.stringuid = ? ", Journal.class);
        query.setParameter(1, "create");
        query.setParameter(2, j.getStringUid());
        return query.getResultList();
    }

    private List<Journal> checkIntIfCreated(Journal j) {
        Query query = em
                .createNativeQuery("SELECT * FROM journal j WHERE j.actionname = ? AND j.intuid = ?  AND j.intuid != ? ", Journal.class);
        query.setParameter(1, "create");
        query.setParameter(2, j.getIntUid());
        query.setParameter(3, 0);
        return query.getResultList();
    }

    private List<Journal> checkLongIfCreated(Journal j) {
        Query query = em
                .createNativeQuery("SELECT * FROM journal j WHERE j.actionname = ? AND j.longuid = ?  AND j.longuid != ? ", Journal.class);
        query.setParameter(1, "create");
        query.setParameter(2, j.getLongUid());
        query.setParameter(3, 0);
        return query.getResultList();
    }

    private boolean isJournalAbsent(Journal j) {
        List<Journal> sl = checkStringIfCreated(j);
        List<Journal> ll = checkLongIfCreated(j);
        List<Journal> il = checkIntIfCreated(j);
        return sl.isEmpty() && ll.isEmpty() && il.isEmpty();
    }

    @Override
    public List<Journal> setJournals(Set<Journal> ljs) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Journal lj : ljs) {
            i++;
            if (isJournalAbsent(lj)) {
                em.merge(lj);
                if (i % 16 == 0) {
                    etr.commit();
                    em.clear();
                    EntityTransaction etr2 = em.getTransaction();
                    if (!etr2.isActive()) {
                        etr2.begin();
                    }

                }
                System.err.println("Write to journal " + lj.getTableName() + " : " + lj.getStringUid() + " lv=" + lj.getLongUid());
            }

        }
        etr.commit();
        Enumeration<Journal> enums = Collections.enumeration(ljs);
        return Collections.list(enums);
    }

    @Override
    public void swipe() {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        List<Journal> fjs = findJournals();
        for (Journal fj : fjs) {
            em.remove(em.merge(fj));
        }
        etr.commit();
    }

}
