/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.TraisorerieStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Traisorerie;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author eroot
 */
public class TraisorerieService implements TraisorerieStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Traisorerie c WHERE c.uid = :id";
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

    public TraisorerieService() {
        // initializing...
    }

    @Override
    public Traisorerie createTraisorerie(Traisorerie cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element TX " + e.getReference() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        etr.commit();
        // ManagedSessionFactory.getEntityManager().clear();
        return cat;
    }

    @Override
    public Traisorerie updateTraisorerie(Traisorerie cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element TX " + e.getReference() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteTraisorerie(Traisorerie cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element TX " + e.getReference() + " supprimee");
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
    public Traisorerie findTraisorerie(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Traisorerie.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Traisorerie.class, catId);
    }

    @Override
    public List<Traisorerie> findTraisoreries() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Traisorerie.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Traisorerie.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisorerieByCompteTresor(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.tresor_id =  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, objId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findByReference(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.reference =  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Double sumByReference(String ref, double taux) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT (SUM(montantusd)+SUM(montantcdf)/").append(taux)
                    .append(") FROM traisorerie p WHERE p.reference = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query q = em.createNativeQuery(sb.toString());
                    q.setParameter(1, ref);
                    Object res = q.getSingleResult();
                    return res != null ? ((Number) res).doubleValue() : 0d;
                });
            }
            Query q = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString());
            q.setParameter(1, ref);
            Object res = q.getSingleResult();
            return res != null ? ((Number) res).doubleValue() : 0d;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public List<Traisorerie> findTraisoreries(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Traisorerie.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Traisorerie.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisoreries(String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Traisorerie.findByRegion");
                    query.setParameter("region", region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Traisorerie.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisorerieByCompteTresor(String objId, String typeCptex) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.tresor_id =  ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, objId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM traisorerie");
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
    public List<Traisorerie> findTraisorerieByCompteTresOR(String objId, String s) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.tresor_id =  ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, objId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> mergeSet(Set<Traisorerie> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Traisorerie lj : bulk) {
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
        Enumeration<Traisorerie> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public double getCreditForCustomer(String clientId) {
        return 0;
    }

    @Override
    public double findCurrentBalanceUsd(String tuid, LocalDate date, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(COALESCE(t.soldeUsd,0)) FROM traisorerie t WHERE t.tresor_id = ? ");
            sb.append(" AND t.region = ? AND t.date BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                Object obj = ManagedSessionFactory.executeRead(em -> {
                    Object res = em.createNativeQuery(sb.toString(), Double.class)
                            .setParameter(1, tuid)
                            .setParameter(2, region)
                            .setParameter(3, date.atStartOfDay())
                            .setParameter(4, date2.atTime(23, 59, 59))
                            .getSingleResult();
                    return res;

                });
                return Objects.isNull(obj) ? 0d : ((Double) obj);
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, tuid)
                    .setParameter(2, region)
                    .setParameter(3, date.atStartOfDay())
                    .setParameter(4, date.atTime(23, 59, 59));
            Object obj = query.getSingleResult();
            return obj != null ? ((Number) obj).doubleValue() : 0d;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public double findCurrentBalanceCdf(String tuid, LocalDate date, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(COALESCE(t.soldeCdf,0)) FROM traisorerie t WHERE t.tresor_id = ? ");
            sb.append(" AND t.region = ? AND t.date BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Object res = em.createNativeQuery(sb.toString())
                            .setParameter(1, tuid).setParameter(2, region)
                            .setParameter(3, date.atStartOfDay())
                            .setParameter(4, date2.atTime(23, 59, 59))
                            .getSingleResult();
                    return res != null ? ((Number) res).doubleValue() : 0d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, tuid)
                    .setParameter(2, region)
                    .setParameter(3, date.atStartOfDay())
                    .setParameter(4, date.atTime(23, 59, 59));
            Object obj = query.getSingleResult();
            return obj != null ? ((Number) obj).doubleValue() : 0d;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public double soldeCdfOnPeriod(String uid, LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(COALESCE(t.montantCdf,0)) FROM traisorerie t WHERE t.tresor_id = ? ");
            sb.append(" AND t.date BETWEEN ? AND ? AND t.region LIKE ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid).setParameter(2, d1.atStartOfDay())
                            .setParameter(3, d2.atTime(23, 59, 59)).setParameter(4, region);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid).setParameter(2, d1.atStartOfDay())
                    .setParameter(3, d2.atTime(23, 59, 59)).setParameter(4, region);
            Object obj = query.getSingleResult();
            return obj != null ? (Double) obj : 0;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double soldeUsdOnPeriod(String uid, LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(COALESCE(t.montantUsd,0)) FROM traisorerie t WHERE t.tresor_id = ? ");
            sb.append(" AND t.date BETWEEN ? AND ? AND t.region LIKE ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid).setParameter(2, d1.atStartOfDay())
                            .setParameter(3, d2.atTime(23, 59, 59)).setParameter(4, region);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid).setParameter(2, d1.atStartOfDay())
                    .setParameter(3, d2.atTime(23, 59, 59)).setParameter(4, region);
            Object obj = query.getSingleResult();
            return obj != null ? (Double) obj : 0;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public Traisorerie findExistingOf(String ref, LocalDate date, String tresorId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie t");
            sb.append(" WHERE t.region LIKE ? AND t.reference LIKE ? AND t.tresor_id = ? LIMIT 1 ");
            if (ManagedSessionFactory.isEmbedded()) {
                List<Traisorerie> lst = ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, region)
                            .setParameter(2, ref)
                            .setParameter(3, tresorId);
                    List<Traisorerie> res = query.getResultList();
                    return res;
                });
                if (lst == null || lst.isEmpty()) {
                    return null;
                }
                return lst.get(0);
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, region)
                    .setParameter(2, ref)
                    .setParameter(3, tresorId);
            List<Traisorerie> res = query.getResultList();
            return res.isEmpty() ? null : res.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findUnSyncedTraisoreries(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Traisorerie> findTraisorByCompteTresor(String uid, LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM traisorerie t WHERE t.tresor_id = ? ");
            sb.append(" AND t.date BETWEEN ? AND ? AND t.region LIKE ? ORDER BY t.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                    query.setParameter(1, uid)
                            .setParameter(2, d1.atStartOfDay())
                            .setParameter(3, d2.atTime(23, 59, 59))
                            .setParameter(4, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
            query.setParameter(1, uid).setParameter(2, d1.atStartOfDay())
                    .setParameter(3, d2.atTime(23, 59, 59)).setParameter(4, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM traisorerie p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Traisorerie> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Traisorerie> result = query.getResultList();
        return !result.isEmpty();
    }

    @Override
    public double getTotalBankDebt() {
        String jpql = "SELECT SUM(t.montantUsd) FROM Traisorerie t WHERE (t.libelle LIKE '%dette%' OR t.libelle LIKE '%pret%') AND t.tresorId.typeCompte = 'Banque'";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double res = em.createQuery(jpql, Double.class).getSingleResult();
                return res == null ? 0d : res;
            });
        }
        Double res = ManagedSessionFactory.getEntityManager().createQuery(jpql, Double.class).getSingleResult();
        return res == null ? 0d : res;
    }

}
