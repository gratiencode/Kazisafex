/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.PeriodeStorage;
import data.Category;
import data.Periode;
import data.Repartir;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import tools.Constants;

/**
 *
 * @author endeleya
 */
public class PeriodeService implements PeriodeStorage {

    @Override
    public boolean isExists(String uid) {
        return findPeriode(uid) != null;
    }

    public PeriodeService() {
        //initializing...
    }

    @Override
    public Periode createPeriode(Periode cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("report vente modifiee");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        tx.commit();
        return cat;
    }

    @Override
    public Periode updatePeriode(Periode cat) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).thenAccept(e -> {
                    System.out.println("report periode modifiee");
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
    public void deletePeriode(Periode obj) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(obj));
                return obj;
            }).thenAccept(e -> {
                System.out.println(" periode suprimee");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(obj));
        etr.commit();
    }

    @Override
    public Periode findPeriode(String objId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                return em.find(Periode.class, objId);
            });
        }
        return ManagedSessionFactory.getEntityManager().find(Periode.class, objId);
    }

    @Override
    public List<Periode> findPeriodes() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Periode.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Periode.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findPeriodes(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Periode.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Periode.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findByProduit(String prodUid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode m WHERE m.product_id =  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, prodUid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
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
            sb.append("SELECT COUNT(*) FROM periode");
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
    public List<Periode> findByProduit(String prodUid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode m WHERE m.product_id =  ? AND m.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, prodUid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
            query.setParameter(1, prodUid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findByProduit(String prodUid, LocalDate dateDebut, LocalDate dateFin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode m WHERE m.product_id =  ? AND m.date_debut = ? AND m.date_fin = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, prodUid);
                    query.setParameter(2, dateDebut);
                    query.setParameter(3, dateFin);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
            query.setParameter(1, prodUid);
            query.setParameter(2, dateDebut);
            query.setParameter(3, dateFin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findPeriodes(LocalDate dateDebut, LocalDate dateFin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode m WHERE m.date_debut = ? AND m.date_fin = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, dateDebut);
                    query.setParameter(2, dateFin);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
            query.setParameter(1, dateDebut);
            query.setParameter(2, dateFin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findNowDescByProduit(String prodUid, String comment, LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode m WHERE m.product_id =  ? AND m.mouvement = ? "
                    + "AND m.comment = ? AND m.date_debut = ? AND m.date_fin = ? ORDER BY m.date_fin DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, prodUid);
                    query.setParameter(2, Constants.COMPTE_OUVERT);
                    query.setParameter(3, comment);
                    query.setParameter(4, debut);
                    query.setParameter(5, fin);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
            query.setParameter(1, prodUid);
            query.setParameter(2, Constants.COMPTE_OUVERT);
            query.setParameter(3, comment);
            query.setParameter(4, debut);
            query.setParameter(5, fin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findPeriodOpened(String typeriod, LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode m WHERE m.mouvement = ? AND "
                    + "m.comment = ? AND m.date_debut = ? AND m.date_fin = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, Constants.COMPTE_OUVERT);
                    query.setParameter(2, typeriod);
                    query.setParameter(3, debut);
                    query.setParameter(4, fin);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
            query.setParameter(1, Constants.COMPTE_OUVERT);
            query.setParameter(2, typeriod);
            query.setParameter(3, debut);
            query.setParameter(4, fin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Periode> findLastClosedPeriodForProduit(String puid, String cment) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM periode p WHERE p.product_id =  ? AND p.mouvement = ? AND "
                    + "p.comment = ? ORDER BY p.date_fin DESC ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Periode.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, Constants.COMPTE_CLOTURE);
                    query.setParameter(3, cment);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
            query.setParameter(1, puid);
            query.setParameter(2, Constants.COMPTE_CLOTURE);
            query.setParameter(3, cment);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM periode p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Periode.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Periode> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Periode.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Periode> result = query.getResultList();
        return !result.isEmpty();
    }
}
