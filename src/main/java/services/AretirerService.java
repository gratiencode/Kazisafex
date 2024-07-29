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

/**
 *
 * @author eroot
 */
public class AretirerService  implements AretirerStorage {

  
    EntityManager em;

    public AretirerService() {
        em =JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Aretirer createAretirer(Aretirer cat) {
        
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public Aretirer updateAretirer(Aretirer cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteAretirer(Aretirer cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Aretirer findAretirer(String catId) {
         return em.find(Aretirer.class, catId);
    }
    
    

    @Override
    public List<Aretirer> findAretirer() {
        try{
            Query query=em.createNamedQuery("Aretirer.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

     @Override
    public Long getCount() {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM aretirer");
           return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }
    }

    @Override
    public Aretirer findAretirerByReference(String ref) {
          try{
            Query query=em.createNamedQuery("Aretirer.findByReferenceVente");
            query.setParameter("referenceVente", ref);
            return (Aretirer) query.getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Aretirer> findAretirer(int start, int max) {
        try{
            Query query=em.createNamedQuery("Aretirer.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Aretirer> mergeSet(Set<Aretirer> bulk) {
   
       
        Enumeration<Aretirer> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
