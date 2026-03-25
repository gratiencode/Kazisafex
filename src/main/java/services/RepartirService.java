/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.RepartirStorage;
import data.MatiereSku;
import data.Repartir;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class RepartirService implements RepartirStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Repartir c WHERE c.uid = :id";
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

    public RepartirService() {
        //initializing...
    }

    @Override
    public Repartir saveRepartir(Repartir d) {
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
    public Repartir updateRepartir(Repartir d) {
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
    public Repartir findRepartir(String d) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                return ManagedSessionFactory.getEntityManager().find(Repartir.class, d);
            });
        }
        return ManagedSessionFactory.getEntityManager().find(Repartir.class, d);
    }

    @Override
    public List<Repartir> findRepartirs() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Repartir.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Repartir.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteRepartir(Repartir d) {
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
    public double findSommeRepartir(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM((r.quantite*m.quant_contenu_sku)) "
                    + "FROM repartir r,matiere_sku m WHERE r.matiere_id = m.matiere_id AND r.sku_id=m.uid ");
            sb.append("AND r.matiere_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(),Double.class);
                    query.setParameter(1, uid);
                    Object o = query.getSingleResult();
                    return (Double) (o == null ? 0d : o);
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Object o = query.getSingleResult();
            return (Double) (o == null ? 0d : o);
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Repartir> findForProduction(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM repartir r ");
            sb.append("WHERE r.production_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Repartir.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Repartir.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM repartir p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Repartir.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Repartir> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Repartir.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Repartir> result = query.getResultList();
        return !result.isEmpty();
    }

}
