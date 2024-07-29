/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ProduitStorage;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Produit;
import java.util.ArrayList;

/**
 *
 * @author eroot
 */
public class ProduitService implements ProduitStorage {

    EntityManager em;

    public ProduitService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }


    @Override
    public Produit createProduit(Produit pro) {
        EntityTransaction tx = em.getTransaction();
            if(!tx.isActive()){
            tx.begin();
            }
        em.persist(pro);
        tx.commit();
        return pro;
    }

    @Override
    public Produit updateProduit(Produit cat) {
        try {
            EntityTransaction tx = em.getTransaction();
            if(!tx.isActive()){
            tx.begin();
            }
            em.merge(cat);
            tx.commit();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // Syst em.err.println("Erreur Message : "+e.getMessage());
        }
        return cat;
    }

    @Override
    public void deleteProduit(Produit cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Produit findProduit(String catId) {
        return em.find(Produit.class, catId);
    }

    @Override
    public List<Produit> findProduits() {
        try {
            Query query = em.createNamedQuery("Produit.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduitByCategory(String catId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.categoryid_uid =  ? ");
            Query query = em.createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, catId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM produit");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduits(int start, int max) {
        try {
            Query query = em.createNamedQuery("Produit.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Produit findByBarcode(String codebar) {
        try {
            Query query = em.createNamedQuery("Produit.findByCodeBar");
            query.setParameter("codeBar", codebar);
            return (Produit) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findAllByCodebar(String codebarr) {
        try {
            Query query = em.createNamedQuery("Produit.findByCodeBar");
            query.setParameter("codeBar", codebarr);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> mergeSet(Set<Produit> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Produit lj : bulk) {
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
        Enumeration<Produit> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Produit> findByDescription(String nomProduit, String marque, String modele, String taille) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.nomproduit =  ? "
                    + "AND p.marque = ? AND p.modele = ? ");
            Query query = em.createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, nomProduit);
            query.setParameter(2, marque);
            query.setParameter(3, modele);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduitByName(String regex) {
        List<Produit> rsult = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ',p.couleur) LIKE ?");
        try {
            Query query = em
                    .createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, "%" + regex + "%");
            rsult.addAll(query.getResultList());
        } catch (NoResultException e) {
            System.err.println("Result is empty mon vieu");
        }
        return rsult;
    }

}
