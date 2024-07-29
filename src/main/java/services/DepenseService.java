/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.DepenseStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Depense;

/**
 *
 * @author eroot
 */
public class DepenseService implements DepenseStorage{

    EntityManager em;
   

    public DepenseService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Depense createDepense(Depense cat) {
       EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public Depense updateDepense(Depense cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteDepense(Depense cat) {
        EntityTransaction etr =em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Depense findDepense(String catId) {
         return em.find(Depense.class, catId);
    }
    
    

    @Override
    public List<Depense> findDepenses() {
        try{
            Query query= em.createNamedQuery("Depense.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Depense> findDepenseByDescription(String objId) {
         try{
            Query query= em.createNamedQuery("Depense.findByNomDepense");
            query.setParameter("nomDepense", objId);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Depense> findDepenses(int start, int max) {
        try{
            Query query= em.createNamedQuery("Depense.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Depense> findDepenses(String region) {
        try{
            Query query= em.createNamedQuery("Depense.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
     @Override
    public Long getCount() {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM depense");
           return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }
    }

    @Override
    public List<Depense> mergeSet(Set<Depense> bulk) {
       EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
       
        int i = 0;
        for (Depense lj : bulk) {
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
        Enumeration<Depense> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }
    
}
