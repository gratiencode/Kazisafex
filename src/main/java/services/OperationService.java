/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.OperationStorage;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Operation;

/**
 *
 * @author eroot
 */
public class OperationService implements OperationStorage{


    EntityManager em;

    public OperationService() {
        em=JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Operation createOperation(Operation cat) {
         EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public Operation updateOperation(Operation cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
       
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteOperation(Operation cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove( em.merge(cat));
        etr.commit();
    }

    @Override
    public Operation findOperation(String catId) {
         return em.find(Operation.class, catId);
    }
    
    

    @Override
    public List<Operation> findOperations() {
        try{
            Query query=  em.createNamedQuery("Operation.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Operation> findOperationByCompteTresor(String objId) {
       //To change body of generated methods, choose Tools | Templates.
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.tresor_id =  ? ");
            Query query = em.createNativeQuery(sb.toString(), Operation.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOpsByDepense(String depId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.depense_id =  ? ");
            Query query = em.createNativeQuery(sb.toString(), Operation.class);
            query.setParameter(1, depId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
     @Override
    public List<Operation> findOpsByDepense(String depId,String tresorId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.depense_id =  ? AND tresor_id = ? ");
            Query query = em.createNativeQuery(sb.toString(), Operation.class);
            query.setParameter(1, depId);
            query.setParameter(2, tresorId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOperations(int start, int max) {
       try{
            Query query= em.createNamedQuery("Operation.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

     @Override
    public Long getCount() {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM operation");
           return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }
    }
    
    @Override
    public List<Operation> findOperations(String region) {
        try{
            Query query= em.createNamedQuery("Operation.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Operation> findByDateInterval(Date date, Date addDays) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.date BETWEEN ? AND ? ");
            Query query = em.createNativeQuery(sb.toString(), Operation.class);
            query.setParameter(1, date,TemporalType.DATE);
            query.setParameter(2, addDays,TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findByDateInterval(Date d1, Date kesho, String region) {
   try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.date BETWEEN ? AND ? AND p.region = ? ");
            Query query = em.createNativeQuery(sb.toString(), Operation.class);
            query.setParameter(1, d1,TemporalType.DATE);
            query.setParameter(2, kesho,TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> mergeSet(Set<Operation> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
       
        int i = 0;
        for (Operation lj : bulk) {
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
        Enumeration<Operation> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    
    
}
