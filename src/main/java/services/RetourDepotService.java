/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RetourDepotStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set; 
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.RetourDepot;

/**
 *
 * @author eroot
 */
public class RetourDepotService implements RetourDepotStorage{

    EntityManager em;

    public RetourDepotService() {
        em=JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public RetourDepot createRetourDepot(RetourDepot cat) {
         EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public RetourDepot updateRetourDepot(RetourDepot cat) {
        EntityTransaction etr =  em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
         em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteRetourDepot(RetourDepot cat) {
        EntityTransaction etr =  em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
         em.remove( em.merge(cat));
        etr.commit();
    }

    @Override
    public RetourDepot findRetourDepot(String catId) {
         return  em.find(RetourDepot.class, catId);
    }
    
     @Override
    public Long getCount() {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM retour_depot");
           return (Long)  em.createNativeQuery(sb.toString()).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }
    }

    @Override
    public List<RetourDepot> findRetourDepots() {
        try{
            Query query= em.createNamedQuery("RetourDepot.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

 
    @Override
    public List<RetourDepot> findRetourDepots(int start, int max) {
        try{
            Query query= em.createNamedQuery("RetourDepot.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<RetourDepot> mergeSet(Set<RetourDepot> bulk) {
         EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
       
        int i = 0;
        for (RetourDepot lj : bulk) {
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
        Enumeration<RetourDepot> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }
    
}
