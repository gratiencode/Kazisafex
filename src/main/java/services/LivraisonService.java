/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.LivraisonStorage;
import data.Category;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Livraison;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class LivraisonService implements LivraisonStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Livraison c WHERE c.uid = :id";
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

    public LivraisonService() {
        // initializing...
    }

    @Override
    public Livraison createLivraison(Livraison cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
            });
            return cat;
        }
        List<Livraison> exist = findLivrByNumAndSuplier(cat.getFournId().getUid(), cat.getNumPiece(), LocalDate.now());
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
    public Livraison updateLivraison(Livraison cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteLivraison(Livraison cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " deleted");
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
    public Livraison findLivraison(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Livraison.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Livraison.class, catId);
    }

    @Override
    public List<Livraison> findLivraisons() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Livraison.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Livraison.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findLivraisonBySupplier(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE fournid_uid = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    query.setParameter(1, objId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findLivraisons(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Livraison.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Livraison.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM livraison");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
                });
            }
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate(String region, int offset, int intValue) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE region = ? ORDER BY dateLivr DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    query.setParameter(1, region);
                    query.setFirstResult(offset);
                    query.setMaxResults(intValue);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, region);
            query.setFirstResult(offset);
            query.setMaxResults(intValue);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate(int offset, int intValue) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison ORDER BY dateLivr DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    query.setFirstResult(offset);
                    query.setMaxResults(intValue);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setFirstResult(offset);
            query.setMaxResults(intValue);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison ORDER BY dateLivr DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findDescSortedByDate(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE region = ? ORDER BY dateLivr DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    query.setParameter(1, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Double sumBySupplier(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(payed) s FROM livraison WHERE fournid_uid = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, uid);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public List<Livraison> mergeSet(Set<Livraison> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Livraison lj : bulk) {
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
        Enumeration<Livraison> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    private List<Livraison> findLivrByNumAndSuplier(String uid, String numPiece, LocalDate ld) {
        try {
            Date dateParam = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE fournid_uid = ? AND numpiece = ? AND dateLivr = ?");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, uid);
            query.setParameter(2, numPiece);
            query.setParameter(3, dateParam, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findLivraisonByReference(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison WHERE reference LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    query.setParameter(1, "%" + ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, "%" + ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Livraison> findUnSyncedLivraisons(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM livraison p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM livraison p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Livraison.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Livraison> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Livraison.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Livraison> result = query.getResultList();
        return !result.isEmpty();
    }

}
