/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.AretirerStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Aretirer;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 *
 * @author eroot
 */
public class AretirerService implements AretirerStorage {

    //

    public AretirerService() {
        // initializing... em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Aretirer createAretirer(Aretirer cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public Aretirer updateAretirer(Aretirer cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteAretirer(Aretirer cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public Aretirer findAretirer(String catId) {
        return ManagedSessionFactory.getEntityManager().find(Aretirer.class, catId);
    }

    @Override
    public List<Aretirer> findAretirer() {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Aretirer.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM aretirer");
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public Aretirer findAretirerByReference(String ref) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Aretirer.findByReferenceVente");
            query.setParameter("referenceVente", ref);
            return (Aretirer) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Aretirer> findAretirer(int start, int max) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Aretirer.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Aretirer> mergeSet(Set<Aretirer> bulk) {

        Enumeration<Aretirer> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Aretirer> findUnSyncedAretirers(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM aretirer p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Aretirer.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Aretirer.class);
            query.setParameter(1, offline);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Aretirer c WHERE c.uid = :id";
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
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM aretirer p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Aretirer.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Aretirer> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Aretirer.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Aretirer> result = query.getResultList();
        return !result.isEmpty();
    }

}
