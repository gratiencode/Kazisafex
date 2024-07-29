/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.LivraisonStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Livraison;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author eroot
 */
public class LivraisonService implements LivraisonStorage {

    EntityManager em;

    public LivraisonService() {
        em =JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Livraison createLivraison(Livraison cat) {
        List<Livraison> exist = findLivrByNumAndSuplier(cat.getFournId().getUid(), cat.getNumPiece(),LocalDate.now());
        if (exist.isEmpty()) {
            EntityTransaction tx = em.getTransaction();
            if(!tx.isActive()){
            tx.begin();
            }
            em.persist(cat);
           tx.commit();
        }
        return cat;
    }

    @Override
    public Livraison updateLivraison(Livraison cat) {
         EntityTransaction tx = em.getTransaction();
            if(!tx.isActive()){
            tx.begin();
            }
            em.merge(cat);
           tx.commit();
        return cat;
    }

    @Override
    public void deleteLivraison(Livraison cat) {
        EntityTransaction etr =em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Livraison findLivraison(String catId) {
        return em.find(Livraison.class, catId);
    }

    @Override
    public List<Livraison> findLivraisons() {
        try {
            Query query = em.createNamedQuery("Livraison.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findLivraisonBySupplier(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE fournid_uid = ? ");
            Query query = em.createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findLivraisons(int start, int max) {
        try {
            Query query = em.createNamedQuery("Livraison.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM livraison");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate(String region, int offset, int intValue) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE region = ? ORDER BY dateLivr DESC");
            Query query = em.createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, region);
            query.setFirstResult(offset);
            query.setMaxResults(intValue);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate(int offset, int intValue) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison ORDER BY dateLivr DESC");
            Query query = em.createNativeQuery(sb.toString(), Livraison.class);
            query.setFirstResult(offset);
            query.setMaxResults(intValue);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison ORDER BY dateLivr DESC");
            Query query = em.createNativeQuery(sb.toString(), Livraison.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE region = ? ORDER BY dateLivr DESC");
            Query query = em.createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Double sumBySupplier(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(payed) s FROM livraison WHERE fournid_uid = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public List<Livraison> mergeSet(Set<Livraison> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Livraison lj : bulk) {
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
        Enumeration<Livraison> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    private List<Livraison> findLivrByNumAndSuplier(String uid, String numPiece,LocalDate ld) {
        try {
            Date dateParam = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE fournid_uid = ? AND numpiece = ? AND dateLivr = ?");
            Query query = em.createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, uid);
            query.setParameter(2, numPiece);
            query.setParameter(3, dateParam,TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

}
