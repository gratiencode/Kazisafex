/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RetourMagasinStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.RetourMagasin;

/**
 *
 * @author eroot
 */
public class RetourMagasinService  implements RetourMagasinStorage{

    EntityManager em;

    public RetourMagasinService() {
        em =JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public RetourMagasin createRetourMagasin(RetourMagasin cat) {
         EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public RetourMagasin updateRetourMagasin(RetourMagasin cat) {
        EntityTransaction etr =  em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
         em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteRetourMagasin(RetourMagasin cat) {
        EntityTransaction etr =  em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
         em.remove( em.merge(cat));
        etr.commit();
    }

    @Override
    public RetourMagasin findRetourMagasin(String catId) {
         return  em.find(RetourMagasin.class, catId);
    }
    
     @Override
    public Long getCount() {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM retour_magasin");
           return (Long)  em.createNativeQuery(sb.toString()).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }
    }

    @Override
    public List<RetourMagasin> findRetourMagasins() {
        try{
            Query query= em.createNamedQuery("RetourMagasin.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

   

    @Override
    public List<RetourMagasin> findRetourMagasins(int start, int max) {
        try{
            Query query= em.createNamedQuery("RetourMagasin.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public long getCountForVente(String uid) {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM retour_magasin WHERE reference_vente  = ? ");
           return (Long)  em.createNativeQuery(sb.toString()).setParameter(1, uid).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }  
    }

    @Override
    public List<RetourMagasin> findByLigneVente(Long uid) {
         try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT * FROM retour_magasin WHERE ligne_vente_id  = ? ");
           return   em.createNativeQuery(sb.toString(),RetourMagasin.class)
                   .setParameter(1, uid).getResultList();
       }catch(NoResultException e){
           return null;
       }  
    }

    @Override
    public List<RetourMagasin> mergeSet(Set<RetourMagasin> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
       
        int i = 0;
        for (RetourMagasin lj : bulk) {
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
        Enumeration<RetourMagasin> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }
    
}
