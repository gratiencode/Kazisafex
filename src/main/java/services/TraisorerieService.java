/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.TraisorerieStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import data.Traisorerie;
import data.Traisorerie;
import org.eclipse.persistence.config.EntityManagerProperties;
import tools.DataId;
import tools.SyncEngine;

/**
 *
 * @author eroot
 */
public class TraisorerieService implements TraisorerieStorage {

    EntityManager em;

    public TraisorerieService() {
        em=JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Traisorerie createTraisorerie(Traisorerie cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.persist(cat);
        etr.commit();
//        em.clear();
        return cat;
    }

    @Override
    public Traisorerie updateTraisorerie(Traisorerie cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteTraisorerie(Traisorerie cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Traisorerie findTraisorerie(String catId) {
        return em.find(Traisorerie.class, catId);
    }

    @Override
    public List<Traisorerie> findTraisoreries() {
        try {
            Query query = em.createNamedQuery("Traisorerie.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisorerieByCompteTresor(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.tresor_id =  ? ");
            Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findByReference(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.reference =  ? ");
            Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Double sumByReference(String ref, double taux) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT (SUM(montantusd)+SUM(montantcdf)/").append(taux).append(") as somme FROM traisorerie p WHERE p.reference =  ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, ref);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisoreries(int start, int max) {
        try {
            Query query = em.createNamedQuery("Traisorerie.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisoreries(String region) {
        try {
            Query query = em.createNamedQuery("Traisorerie.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisorerieByCompteTresor(String objId, String typeCpte) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.tresor_id =  ? AND p.typeTresorerie = ?");
            Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, objId);
            query.setParameter(2, typeCpte);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM traisorerie");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Traisorerie> findTraisorerieByCompteTresOR(String objId, String typeCpte) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.tresor_id =  ? AND p.typeTresorerie = ?");
            Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, objId);
            query.setParameter(2, typeCpte);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> mergeSet(Set<Traisorerie> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Traisorerie lj : bulk) {
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
        Enumeration<Traisorerie> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
