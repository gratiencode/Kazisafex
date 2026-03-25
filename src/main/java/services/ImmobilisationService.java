/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ImmobilisationStorage;
import data.Immobilisation;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

/**
 *
 * @author eroot
 */
public class ImmobilisationService implements ImmobilisationStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Immobilisation c WHERE c.uid = :id";
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

    public ImmobilisationService() {
        // initializing...
    }

    @Override
    public Immobilisation createImmobilisation(Immobilisation obj) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(obj);
                return obj;
            }).thenAccept(e -> {
                System.out.println("Element immobilisation " + e.getLibelle() + " enregistree");
            });
            return obj;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(obj);
        tx.commit();
        return obj;
    }

    @Override
    public Immobilisation updateImmobilisation(Immobilisation obj) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(obj);
                return obj;
            }).thenAccept(e -> {
                System.out.println("Element immobilisation " + e.getLibelle() + " mise à jour");
            });
            return obj;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(obj);
        tx.commit();
        return obj;
    }

    @Override
    public void deleteImmobilisation(Immobilisation obj) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(obj));
                return obj;
            }).thenAccept(e -> {
                System.out.println("Element immobilisation " + e.getLibelle() + " supprimee");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(obj));
        etr.commit();
    }

    @Override
    public Immobilisation findImmobilisation(String objId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Immobilisation.class, objId));
        }
        return ManagedSessionFactory.getEntityManager().find(Immobilisation.class, objId);
    }

    @Override
    public List<Immobilisation> findImmobilisations() {
        try {
            String jpql = "SELECT i FROM Immobilisation i";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createQuery(jpql);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createQuery(jpql);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Immobilisation> findImmobilisations(int start, int max) {
        try {
            String jpql = "SELECT i FROM Immobilisation i";
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
    public List<Immobilisation> findImmobilisationByRegion(String region) {
        try {
            String jpql = "SELECT i FROM Immobilisation i WHERE i.region = :region";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createQuery(jpql);
                    query.setParameter("region", region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createQuery(jpql);
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            String sql = "SELECT COUNT(*) FROM immobilisation";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory
                        .executeRead(em -> ((Number) em.createNativeQuery(sql).getSingleResult()).longValue());
            }
            return ((Number) ManagedSessionFactory.getEntityManager().createNativeQuery(sql).getSingleResult())
                    .longValue();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Immobilisation> mergeSet(Set<Immobilisation> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Immobilisation obj : bulk) {
            i++;
            ManagedSessionFactory.getEntityManager().merge(obj);
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
        Enumeration<Immobilisation> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Immobilisation> findUnSynced(long since) {
        String jpql = "SELECT i FROM Immobilisation i WHERE i.updatedAt > :since";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Immobilisation.class)
                    .setParameter("since", since)
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager()
                .createQuery(jpql, Immobilisation.class)
                .setParameter("since", since)
                .getResultList();
    }

    @Override
    public List<Immobilisation> findUnSyncedImmobilisations(long disconnected_at) {
        return findUnSynced(disconnected_at);
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        return isExists(uid);
    }
}
