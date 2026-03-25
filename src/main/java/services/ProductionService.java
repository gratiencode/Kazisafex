/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.ProductionStorage;
import data.MatiereSku;
import data.Production;
import data.Produit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class ProductionService implements ProductionStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Production c WHERE c.uid = :id";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                    .setParameter("id", uid)
                    .getSingleResult());
        }
        return ManagedSessionFactory.getEntityManager()
                .createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult();
    }

    public ProductionService() {
        //initializing...
    }

    @Override
    public Production saveProduction(Production d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(d);
                return d;
            }).thenAccept(e -> {
                System.out.println("Element dps " + e.getUid() + " enregistree");
            });
            return d;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(d);
        tx.commit();
        return d;
    }

    @Override
    public Production updateProduction(Production d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(d);
                return d;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getRegion() + " enregistree");
            });
            return d;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(d);
        tx.commit();
        return d;
    }

    @Override
    public Production findProduction(String d) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                return em.find(Production.class, d);
            });
        }
        return ManagedSessionFactory.getEntityManager().find(Production.class, d);
    }

    @Override
    public List<Production> findProductions() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM production r ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Production.class);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Production.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteProduction(Production d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(d));
                return d;
            }).thenAccept(e -> {
                System.out.println("Element dps " + e.getUid() + " supprimee");
            });
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(d));
        tx.commit();
    }

    @Override
    public List<Production> findProductionByProduitLot(String lot, String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM production r WHERE r.numlot = ? AND r.produit_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Production.class);
                    query.setParameter(1, lot)
                            .setParameter(2, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), Production.class);
            query.setParameter(1, lot)
                    .setParameter(2, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Production> findForProduct(Produit p, LocalDate value, LocalDate value0) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM production r WHERE r.produit_id = ? AND r.date_fin BETWEEN ? AND ?  ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Production.class);
                    query.setParameter(1, p.getUid())
                            .setParameter(2, value)
                            .setParameter(3, value0);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Production.class);
            query.setParameter(1, p.getUid()).setParameter(2, value)
                    .setParameter(3, value0);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM production p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Production.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Production> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Production.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Production> result = query.getResultList();
        return !result.isEmpty();
    }

}
