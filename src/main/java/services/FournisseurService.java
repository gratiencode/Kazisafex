/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.FournisseurStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Fournisseur;

/**
 *
 * @author eroot
 */
public class FournisseurService implements FournisseurStorage {

    EntityManager em;

    public FournisseurService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Fournisseur createFournisseur(Fournisseur cat) {
        List<Fournisseur> exist = findByPhone(cat.getPhone());
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
    public Fournisseur updateFournisseur(Fournisseur cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.persist(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteFournisseur(Fournisseur cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
       em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Fournisseur findFournisseur(String catId) {
        return em.find(Fournisseur.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM fournisseur");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Fournisseur> findFournisseurs() {
        try {
            Query query = em.createNativeQuery("Select * from fournisseur", Fournisseur.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Fournisseur> findFournisseurs(int start, int max) {
        try {
            Query query = em.createNamedQuery("Fournisseur.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Fournisseur> findByPhone(String text) {
        try {
            Query query = em.createNamedQuery("Fournisseur.findByPhone");
            query.setParameter("phone", text);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Fournisseur> mergeSet(Set<Fournisseur> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Fournisseur lj : bulk) {
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
        Enumeration<Fournisseur> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
