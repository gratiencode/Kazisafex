/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.MesureStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Mesure;

/**
 *
 * @author eroot
 */
public class MesureService implements MesureStorage {

    EntityManager em;

    public MesureService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Mesure createMesure(Mesure cat) {
        List<Mesure> exist = findByProduit(cat.getProduitId().getUid(), cat.getDescription());
        if (exist.isEmpty()) {
            EntityTransaction tx = em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            em.persist(cat);
            tx.commit();
        }
        return cat;
    }

    @Override
    public Mesure updateMesure(Mesure cat) {
        try {
            EntityTransaction tx = em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            em.merge(cat);
            tx.commit();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            System.err.println("Erreur Message : " + e.getMessage());
        }
        return cat;
    }

    @Override
    public void deleteMesure(Mesure cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Mesure findMesure(String catId) {
        return em.find(Mesure.class, catId);
    }

    @Override
    public List<Mesure> findMesures() {
        try {
            Query query = em.createNamedQuery("Mesure.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findMesures(int start, int max) {
        try {
            Query query = em.createNamedQuery("Mesure.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findByProduit(String prodUid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id =  ? ");
            Query query = em.createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM mesure");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Mesure> findByProduit(String prodUid, String desc) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.description = ? ");
            Query query = em.createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, prodUid);
            query.setParameter(2, desc);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findAscSortedByQuantWithProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY m.quantcontenu ASC ");
            Query query = em.createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Mesure findMaxMesureByProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY quantcontenu DESC LIMIT 1");
            Query query = em.createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Mesure findByProduitAndQuant(String uid, Double quantContenu) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND quantcontenu = ? ");
            Query query = em.createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            query.setParameter(2, quantContenu);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> mergeSet(Set<Mesure> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Mesure lj : bulk) {
            i++;
            em.merge(lj);
            if (i % 16 == 0) {
                etr.commit();
                em.clear();
                EntityTransaction etr2 = em.getTransaction();
        if (!etr2.isActive()) {
            etr2.begin();
       }

            }
        }
        etr.commit();
        Enumeration<Mesure> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
