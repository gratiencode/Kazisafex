/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.MatiereSkuStorage;
import data.Category;
import data.MatiereSku;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class MatiereSkuService implements MatiereSkuStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM MatiereSku c WHERE c.id = :id";
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

    public MatiereSkuService() {
        //initializing...
    }

    @Override
    public MatiereSku saveMatiereSku(MatiereSku d) {
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
    public MatiereSku updateMatiereSku(MatiereSku d) {
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
    public MatiereSku findMatiereSku(String d) {
        if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return ManagedSessionFactory.getEntityManager().find(MatiereSku.class, d);
                });
            }
        return ManagedSessionFactory.getEntityManager().find(MatiereSku.class, d);
    }

    @Override
    public List<MatiereSku> findMatiereSkus() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                     Query query = em.createNamedQuery("MatiereSku.findAll");
            return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("MatiereSku.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteMatiereSku(MatiereSku d) {
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
    public List<MatiereSku> findMatiereSkuFor(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM matiere_sku m WHERE m.matiere_id =  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), MatiereSku.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), MatiereSku.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public MatiereSku findMatiereSku(String name, double q, String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM matiere_sku m WHERE m.matiere_id =  ? AND quant_contenu_sku = ? AND nom_sku = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), MatiereSku.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, q);
                    query.setParameter(3, name).setMaxResults(1);
                    return (MatiereSku) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), MatiereSku.class);
            query.setParameter(1, uid);
            query.setParameter(2, q);
            query.setParameter(3, name);
            return (MatiereSku) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public MatiereSku findMatiereSku(String txt, String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM matiere_sku m WHERE m.matiere_id =  ? AND nom_sku = ? LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {

                    Query query = em.createNativeQuery(sb.toString(), MatiereSku.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, txt);
                    return (MatiereSku) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), MatiereSku.class);
            query.setParameter(1, uid);
            query.setParameter(2, txt);
            return (MatiereSku) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM matiere_sku p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), MatiereSku.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<MatiereSku> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), MatiereSku.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<MatiereSku> result = query.getResultList();
        return !result.isEmpty();
    }

}
