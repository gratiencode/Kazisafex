/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import data.DepenseAgregate;
import data.Depense;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;
import IServices.DepenseAgregateStorage;
import java.util.Set;
import java.util.Enumeration;
import java.time.LocalDate;

/**
 *
 * @author eroot
 */
public class DepenseAgregateService implements DepenseAgregateStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM DepenseAgregate c WHERE c.uid = :id";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                    .setParameter("id", uid)
                    .getSingleResult());
        }
        return ManagedSessionFactory.getEntityManager()
                .createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult();
    }


    @Override
    public DepenseAgregate createDepenseAgregate(DepenseAgregate da) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(da);
                return da;
            }).thenAccept(e -> {
                System.out.println("DepenseAgregate enregistré: " + e.getUid());
            });
            return da;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(da);
        tx.commit();
        return da;
    }
    @Override
    public DepenseAgregate updateDepenseAgregate(DepenseAgregate da) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(da);
                return da;
            }).thenAccept(e -> {
                System.out.println("DepenseAgregate mis à jour: " + e.getUid());
            });
            return da;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(da);
        tx.commit();
        return da;
    }
    @Override
    public void deleteDepenseAgregate(DepenseAgregate da) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(da));
                return da;
            }).thenAccept(e -> {
                System.out.println("DepenseAgregate supprimé: " + e.getUid());
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(da));
        etr.commit();
    }
    @Override
    public DepenseAgregate findDepenseAgregate(String uid) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(DepenseAgregate.class, uid));
        }
        return ManagedSessionFactory.getEntityManager().find(DepenseAgregate.class, uid);
    }
    @Override
    public List<DepenseAgregate> findDepenseAgregates() {
        String jpql = "SELECT da FROM DepenseAgregate da ORDER BY da.date DESC";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, DepenseAgregate.class).getResultList());
        }
        return ManagedSessionFactory.getEntityManager().createQuery(jpql, DepenseAgregate.class).getResultList();
    }
    @Override
    public List<DepenseAgregate> findDepenseAgregates(String region) {
        String jpql = "SELECT da FROM DepenseAgregate da WHERE da.region = :region ORDER BY da.date DESC";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, DepenseAgregate.class)
                    .setParameter("region", region)
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager().createQuery(jpql, DepenseAgregate.class)
                .setParameter("region", region)
                .getResultList();
    }

    @Override
    public List<DepenseAgregate> findDepenseAgregates(int start, int max) {
         try {
            String jpql = "SELECT d FROM DepenseAgregate d";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createQuery(jpql);
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createQuery(jpql);
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<DepenseAgregate> findDepenseAgregates(LocalDateTime date, String imputation) {
        try {
            String jpql = "SELECT d FROM DepenseAgregate d WHERE cast(d.date as date) = :date AND d.imputation = :imputation";
            LocalDate justDate = date.toLocalDate();
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createQuery(jpql);
                    query.setParameter("date", java.sql.Date.valueOf(justDate));
                    query.setParameter("imputation", imputation);
                    return query.getResultList();
                });
            }
             Query query = ManagedSessionFactory.getEntityManager().createQuery(jpql);
             query.setParameter("date", java.sql.Date.valueOf(justDate));
             query.setParameter("imputation", imputation);
             return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM depense_agregate");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
                });
            }
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<DepenseAgregate> mergeSet(Set<DepenseAgregate> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (DepenseAgregate lj : bulk) {
            i++;
            ManagedSessionFactory.getEntityManager().merge(lj);
            if (i % 16 == 0) {
                etr.commit();
                ManagedSessionFactory.getEntityManager().clear();
                EntityTransaction etr2 = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!etr2.isActive()) {
                    etr2.begin();
                }

            }
        }
        etr.commit();
        Enumeration<DepenseAgregate> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<DepenseAgregate> findUnSyncedDepenseAgregates(long disconnected_at) {
        return null; // Not implemented for now
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        return false; // Not implemented for now
    }
}
