/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.CompteTresorStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.CompteTresor;

/**
 *
 * @author eroot
 */
public class CompteTresorService implements CompteTresorStorage {

    EntityManager em;

    public CompteTresorService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public CompteTresor createCompteTresor(CompteTresor cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public CompteTresor updateCompteTresor(CompteTresor cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteCompteTresor(CompteTresor cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public CompteTresor findCompteTresor(String catId) {
        return em.find(CompteTresor.class, catId);
    }

    @Override
    public List<CompteTresor> findCompteTresors() {
        try {
            Query query = em.createNamedQuery("CompteTresor.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<CompteTresor> findCompteTresors(int start, int max) {
        try {
            Query query = em.createNamedQuery("CompteTresor.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<CompteTresor> findCompteTresors(String region) {
        try {
            Query query = em.createNamedQuery("CompteTresor.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<CompteTresor> findCompteTresorByNumero(String numero) {
        try {
            Query query = em.createNamedQuery("CompteTresor.findByNumeroCompte");
            query.setParameter("region", numero);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM compte_tresor");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<CompteTresor> findCompteTresorByBankName(String bname) {
        try {
            Query query = em.createNamedQuery("CompteTresor.findByBankName");
            query.setParameter("bankName", bname);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<CompteTresor> findByNumeroCompte(String numeroCompte) {
        try {
            Query query = em.createNamedQuery("CompteTresor.findByNumeroCompte");
            query.setParameter("numeroCompte", numeroCompte);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<CompteTresor> mergeSet(Set<CompteTresor> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (CompteTresor lj : bulk) {
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
        Enumeration<CompteTresor> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
