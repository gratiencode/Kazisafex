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
        return ManagedSessionFactory.executeWrite(em -> {
            em.persist(cat);
            return cat;
        });
    }

    @Override
    public Aretirer updateAretirer(Aretirer cat) {
        return ManagedSessionFactory.executeWrite(em -> {
            em.merge(cat);
            return cat;
        });
    }

    @Override
    public void deleteAretirer(Aretirer cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.remove(em.merge(cat));
            return null;
        });
    }

    @Override
    public Aretirer findAretirer(String catId) {
        return ManagedSessionFactory.executeRead(em -> em.find(Aretirer.class, catId));
    }

    @Override
    public List<Aretirer> findAretirer() {
        return ManagedSessionFactory.executeRead(em -> {
            try {
                Query query = em.createNamedQuery("Aretirer.findAll");
                return query.getResultList();
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    @Override
    public Long getCount() {
        return ManagedSessionFactory.executeRead(em -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT COUNT(*) FROM aretirer");
                return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
            } catch (NoResultException e) {
                return 0L;
            }
        });
    }

    @Override
    public Aretirer findAretirerByReference(String ref) {
        return ManagedSessionFactory.executeRead(em -> {
            try {
                Query query = em.createNamedQuery("Aretirer.findByReferenceVente");
                query.setParameter("referenceVente", ref);
                return (Aretirer) query.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    @Override
    public List<Aretirer> findAretirer(int start, int max) {
        return ManagedSessionFactory.executeRead(em -> {
            try {
                Query query = em.createNamedQuery("Aretirer.findAll");
                query.setFirstResult(start);
                query.setMaxResults(max);
                return query.getResultList();
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    @Override
    public List<Aretirer> mergeSet(Set<Aretirer> bulk) {

        Enumeration<Aretirer> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Aretirer> findUnSyncedAretirers(long disconnected_at) {
        return ManagedSessionFactory.executeRead(em -> {
            try {
                Timestamp offline = new Timestamp(disconnected_at);

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM aretirer p WHERE p.updated_at >= ?");
                Query query = em.createNativeQuery(sb.toString(), Aretirer.class);
                query.setParameter(1, offline);
                return query.getResultList();
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Aretirer c WHERE c.uid = :id";
        return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult());
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        return ManagedSessionFactory.executeRead(em -> {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM aretirer p WHERE p.uid = ? AND p.updated_at = ?");
            Query query = em.createNativeQuery(sb.toString(), Aretirer.class);
            query.setParameter(1, uid);
            query.setParameter(2, atime);
            List<Aretirer> result = query.getResultList();
            return !result.isEmpty();
        });
    }

}
