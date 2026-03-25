/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.MesureStorage;
import data.Category;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Mesure;
import data.Produit;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class MesureService implements MesureStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Mesure c WHERE c.uid = :id";
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

    public MesureService() {
        //initializing...
    }

    @Override
    public Mesure createMesure(Mesure cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getDescription() + " enregistree");
            });
            return cat;
        }
        List<Mesure> exist = findByProduit(cat.getProduitId().getUid(), cat.getDescription());
        if (exist.isEmpty()) {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            tx.commit();
        }
        return cat;
    }

    @Override
    public Mesure updateMesure(Mesure cat) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).thenAccept(e -> {
                    System.out.println("Element " + e.getDescription() + " enregistree");
                });
                return cat;
            }
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().merge(cat);
            tx.commit();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            System.err.println("Erreur Message : " + e.getMessage());
        }
        return cat;
    }

    @Override
    public void deleteMesure(Mesure cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getDescription() + " supprimree");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public Mesure findMesure(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Mesure.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Mesure.class, catId);
    }

    @Override
    public List<Mesure> findMesures() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em-> {
                    Query query = em.createNamedQuery("Mesure.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Mesure.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findMesures(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em-> {
                    Query query = em.createNamedQuery("Mesure.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Mesure.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findByProduit(String prodUid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id =  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, prodUid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, prodUid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM mesure");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    return (Long) em.createNativeQuery(sb.toString(), Long.class).getSingleResult();
                });
            }
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Mesure> findByProduit(String prodUid, String desc) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.description = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, prodUid);
                    query.setParameter(2, desc);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, prodUid);
            query.setParameter(2, desc);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findAscSortedByQuantWithProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY m.quantcontenu ASC ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Mesure findMaxMesureByProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY quantcontenu DESC LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, uid);
                    return (Mesure) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Mesure findMinMesureByProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY quantcontenu ASC LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, uid);
                    return (Mesure) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Mesure findByProduitAndQuant(String uid, Double quantContenu) {
        List<Mesure> meso = findByProduitAndQuantContenu(uid, quantContenu);
        if (meso.isEmpty()) {
            return null;
        }
        return meso.get(0);
    }

    @Override
    public List<Mesure> mergeSet(Set<Mesure> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Mesure lj : bulk) {
            i++;
            ManagedSessionFactory.getEntityManager().merge(lj);
            if (i % 16 == 0) {
                etr.commit();
                ManagedSessionFactory.getEntityManager().clear();
                EntityTransaction etr2 = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!etr2.isActive()) {
                    etr2.begin();
                }

            }
        }
        etr.commit();
        Enumeration<Mesure> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Mesure> findByProduitAndQuantContenu(String uid, double quantM) {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.quantcontenu = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), Mesure.class)
                        .setParameter(1, uid).setParameter(2, quantM).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            query.setParameter(2, quantM);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Mesure> findUnSyncedMesure(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM mesure p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Mesure> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Mesure> result = query.getResultList();
        return !result.isEmpty();
    }

}
