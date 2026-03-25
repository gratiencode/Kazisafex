/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.StockerStorage;
import data.Category;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Stocker;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import data.Destocker;
import data.Mesure;
import data.Produit;
import data.StockDepotAgregate;
import delegates.MesureDelegate;

/**
 *
 * @author eroot
 */
public class StockerService implements StockerStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Stocker c WHERE c.uid = :id";
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

    public StockerService() {
        //initializing...
    }

    @Override
    public Stocker createStocker(Stocker cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNumlot() + " enregistree");
            });
            return cat;
        }
        List<Stocker> ss = findStockerByProduitLot(cat.getProductId().getUid(), cat.getNumlot(), cat.getDateStocker());
        if (ss.isEmpty()) {
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
    public Stocker updateStocker(Stocker cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNumlot() + " enregistree");
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
    public void deleteStocker(Stocker cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNumlot() + " supprimee");
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
    public Stocker findStocker(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Stocker.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Stocker.class, catId);
    }

    @Override
    public List<Stocker> findStockers() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Stocker.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Stocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockers(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Stocker.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Stocker.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * This function retrieves the quantity of product id given in argument,
     * expressed in unit
     *
     * @param idPro
     * @return the sum of quantity in unit
     */
    @Override
    public Double sumByProduit(String idPro) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(s.quantite * m.quantcontenu) q FROM stocker s,mesure m "
                    + "WHERE s.product_id = ? AND s.mesure_id = m.uid ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, idPro);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idPro);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM stocker");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return ((Long) em.createNativeQuery(sb.toString(), Long.class).getSingleResult());
                });
            }
            return ((Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult());
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Stocker> findStockerByProduit(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, objId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockerByProduitLot(String objId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, objId);
                    query.setParameter(2, lot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, objId);
            query.setParameter(2, lot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockerByLivraison(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT * FROM stocker s WHERE s.livraisid_uid = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findByDateIntervale(LocalDate date1, LocalDate date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.datestocker BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Stocker> findStockerByProduitLot(String objId, String lot, LocalDateTime date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.numlot = ? AND s.datestocker  = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, objId);
                    query.setParameter(2, lot);
                    query.setParameter(3, date);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, objId);
            query.setParameter(2, lot);
            query.setParameter(3, date);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.datestocker BETWEEN ? AND ? AND s.region = ? ");
             if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atTime(23, 59, 59));
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findAscSortedByDateExpir(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY dateExpir ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findAscSortedByDateExpir(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ORDER BY dateExpir ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findDescSortedByDateStock(String prouid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY datestocker DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, prouid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, prouid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findAscSortedByDateStock(String prouid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY datestocker ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, prouid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, prouid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockerByLivrAndProduit(String livuid, String prouid0) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.livraisid_uid = ? AND s.product_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, livuid);
                    query.setParameter(2, prouid0);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, livuid);
            query.setParameter(2, prouid0);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findByDateExpInterval(LocalDate time, LocalDate darg) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.dateExpir BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, time);
                    query.setParameter(2, darg);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, time);
            query.setParameter(2, darg);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockerByProduit(String pid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, pid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, pid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockers(String region, int s, int m) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Stocker.findByRegion");
                    query.setParameter("region", region);
                    query.setFirstResult(s);
                    query.setMaxResults(m);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Stocker.findByRegion");
            query.setParameter("region", region);
            query.setFirstResult(s);
            query.setMaxResults(m);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockers(String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Stocker.findByRegion");
                    query.setParameter("region", region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Stocker.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findDescSortedByDateStock(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.region = ? ORDER BY datestocker DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> mergeSet(Set<Stocker> bulk) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (Stocker lj : bulk) {
                    em.merge(lj);
                }
                return bulk;
            }).thenAccept(e -> {
                System.out.println("Bulk Stocker merged");
            });
            return new ArrayList<>(bulk);
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Stocker lj : bulk) {
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
        Enumeration<Stocker> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Stocker> toFefoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY s.dateExpir ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> toFifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY s.datestocker ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> toLifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY s.datestocker DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sum(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM((s.quantite*m.quantcontenu)) q FROM stocker s, mesure m WHERE s.product_id = ? AND s.mesure_id=m.uid ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public static List<Stocker> getStockers() {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Stocker.findAll");
                return query.getResultList();
            });
        }
        EntityManager mem = ManagedSessionFactory.getEntityManager();
        try {
            Query query = mem.createNamedQuery("Stocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findUnSyncedStockers(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, offline);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM stocker p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Stocker> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Stocker> result = query.getResultList();
        return !result.isEmpty();
    }

    @Override
    public double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(s.quantite, 0) * COALESCE(m.quantcontenu, 0)) FROM stocker s, mesure m "
                + "WHERE s.product_id = ? AND s.mesure_id = m.uid AND s.datestocker BETWEEN ? AND ? AND s.region LIKE ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double res = (Double) em.createNativeQuery(sb.toString())
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region)
                        .getSingleResult();
                return res == null ? 0 : res;
            });
        }
        Double res = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString())
                .setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region)
                .getSingleResult();
        return res == null ? 0 : res;
    }

    @Override
    public double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(d.quantite, 0) * COALESCE(m.quantcontenu, 0)) FROM destocker d, mesure m "
                + "WHERE d.product_id = ? AND d.mesure_id = m.uid AND d.datedestockage BETWEEN ? AND ? AND d.region LIKE ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double res = (Double) em.createNativeQuery(sb.toString())
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region)
                        .getSingleResult();
                return res == null ? 0 : res;
            });
        }
        Double res = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString())
                .setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region)
                .getSingleResult();
        return res == null ? 0 : res;
    }

    @Override
    public double calculerStockInitialEnUnite(String uid, LocalDate datedebut, String region) {
        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(s.quantite, 0) * COALESCE(m.quantcontenu, 0)) FROM stocker s, mesure m "
                + "WHERE s.product_id = ? AND s.mesure_id = m.uid AND s.datestocker < ? AND s.region LIKE ?");
        StringBuilder sbS = new StringBuilder();
        sbS.append("SELECT SUM(COALESCE(d.quantite, 0) * COALESCE(m.quantcontenu, 0)) FROM destocker d, mesure m "
                + "WHERE d.product_id = ? AND d.mesure_id = m.uid AND d.datedestockage < ? AND d.region LIKE ?");

        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) em.createNativeQuery(sbE.toString())
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, region)
                        .getSingleResult();
                Double sorties = (Double) em.createNativeQuery(sbS.toString())
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, region)
                        .getSingleResult();
                double stk = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
                return stk <= 0 ? 0 : stk;
            });
        }
        Double entrees = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbE.toString())
                .setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, region)
                .getSingleResult();
        Double sorties = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbS.toString())
                .setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, region)
                .getSingleResult();
        double stk = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
        return stk <= 0 ? 0 : stk;
    }

    @Override
    public void rectifyStockDepot(data.Produit produit, LocalDate dte, String region, double coutAch) {
        data.Mesure unite = delegates.MesureDelegate.findByProduitAndQuant(produit.getUid(), 1d);
        double E = sommeEntreeSurPeriode(produit.getUid(), dte, dte, region);
        double S = sommeSortieSurPeriode(produit.getUid(), dte, dte, region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), dte, region);
        double stockFinal = stockInit + E - S;

        StockDepotAgregate depot = findDepositAggregate(produit, region, dte);
        if (depot == null) {
            depot = new StockDepotAgregate();
            depot.setProductId(produit);
            depot.setRegion(region);
            depot.setDate(dte);
            depot.setMesureId(unite);
            depot.setQuantite(stockFinal);
            depot.setCoutAchat(coutAch);
            depot.setValeurStock(stockFinal * coutAch);

            if (ManagedSessionFactory.isEmbedded()) {
                final StockDepotAgregate finalDepot = depot;
                ManagedSessionFactory.submitWrite(em -> {
                    em.persist(finalDepot);
                    return finalDepot;
                });
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) tx.begin();
                ManagedSessionFactory.getEntityManager().persist(depot);
                tx.commit();
            }
        } else {
            depot.setQuantite(stockFinal);
            depot.setCoutAchat(coutAch);
            depot.setValeurStock(stockFinal * coutAch);
            if (ManagedSessionFactory.isEmbedded()) {
                final StockDepotAgregate finalDepot = depot;
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(finalDepot);
                    return finalDepot;
                });
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) tx.begin();
                ManagedSessionFactory.getEntityManager().merge(depot);
                tx.commit();
            }
        }
    }

    private StockDepotAgregate findDepositAggregate(data.Produit p, String region, LocalDate dte) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return (StockDepotAgregate) em.createNamedQuery("StockDepotAgregate.findByProduitRegionDate")
                            .setParameter("productId", p)
                            .setParameter("region", region)
                            .setParameter("date", dte)
                            .getSingleResult();
                });
            }
            return (StockDepotAgregate) ManagedSessionFactory.getEntityManager().createNamedQuery("StockDepotAgregate.findByProduitRegionDate")
                    .setParameter("productId", p)
                    .setParameter("region", region)
                    .setParameter("date", dte)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
