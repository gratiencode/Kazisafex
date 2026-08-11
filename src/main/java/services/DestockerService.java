/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.DestockerStorage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Destocker;
import data.Produit;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 *
 * @author eroot
 */
public class DestockerService implements DestockerStorage {

    @Override
    public List<Destocker> findDestockers(String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM destocker WHERE region = ? ORDER BY dateDestockage DESC");
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, region);
            return query.getResultList();
        });
    }

    private static final class DepotLotSnapshot {

        private final Produit produit;
        private final String numlot;
        private final String region;
        private final double coutAchat;

        private DepotLotSnapshot(Produit produit, String numlot, String region, double coutAchat) {
            this.produit = produit;
            this.numlot = numlot;
            this.region = region;
            this.coutAchat = coutAchat;
        }

        private boolean isValid() {
            return produit != null && produit.getUid() != null
                    && numlot != null && !numlot.isBlank()
                    && region != null && !region.isBlank();
        }
    }

    private DepotLotSnapshot snapshotOf(Destocker destocker) {
        if (destocker == null) {
            return null;
        }
        return new DepotLotSnapshot(
                destocker.getProductId(),
                destocker.getNumlot(),
                destocker.getRegion(),
                destocker.getCoutAchat());
    }

    private void rectifyDepotAggregate(DepotLotSnapshot snapshot) {
        if (snapshot == null || !snapshot.isValid()) {
            return;
        }
        new StockerService().rectifyStockDepotByLot(
                snapshot.produit,
                snapshot.numlot,
                snapshot.region,
                snapshot.coutAchat,
                null);
    }

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Destocker c WHERE c.uid = :id";
        return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult());
    }

    public DestockerService() {
        // initializing...
    }

    @Override
    public Destocker createDestocker(Destocker cat) {
        ManagedSessionFactory.submitWrite(em -> {
            em.persist(cat);
            return cat;
        }).thenAccept(e -> {
            System.out.println("Element " + e.getReference() + " enregistree");
            rectifyDepotAggregate(snapshotOf(e));
            AggregateTriggerService.getInstance().notifyDestocker(e);
        });
        return cat;
    }

    @Override
    public Destocker updateDestocker(Destocker cat) {
        DepotLotSnapshot before = snapshotOf(findDestocker(cat.getUid()));
        ManagedSessionFactory.submitWrite(em -> {
            em.merge(cat);
            return cat;
        }).thenAccept(e -> {
            System.out.println("Element " + e.getReference() + " enregistree");
            rectifyDepotAggregate(before);
            rectifyDepotAggregate(snapshotOf(e));
            AggregateTriggerService.getInstance().notifyDestocker(e);
        });
        return cat;
    }

    @Override
    public void deleteDestocker(Destocker cat) {
        DepotLotSnapshot before = snapshotOf(findDestocker(cat.getUid()));
        ManagedSessionFactory.submitWrite(em -> {
            em.remove(em.merge(cat));
            return cat;
        }).thenAccept(e -> {
            System.out.println("Element " + e.getReference() + " enregistree");
            rectifyDepotAggregate(before == null ? snapshotOf(e) : before);
            AggregateTriggerService.getInstance().notifyDestocker(e);
        });
    }

    @Override
    public Destocker findDestocker(String catId) {
        return ManagedSessionFactory.executeRead(em -> em.find(Destocker.class, catId));
    }

    @Override
    public List<Destocker> findDestockers() {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Destocker.findAll");
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findDestockerByProduit(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, objId);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    /**
     * La fonction calcule la somme de toute les sortie d'un produit en unite
     *
     * @param prodId l'id du produit
     * @return la valeur de sortie en unite. par example en piece
     */
    @Override
    public Double sumDestockerByProduit(String prodId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(d.quantite*m.quantcontenu) q FROM destocker d, mesure m WHERE d.product_id = ? AND d.mesure_id = m.uid");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                query.setParameter(1, prodId);
                return (Double) query.getSingleResult();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM destocker");
            return ManagedSessionFactory.executeRead(em -> {
                return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
            });
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Destocker> findDestockers(int start, int max) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Destocker.findAll");
                query.setFirstResult(start);
                query.setMaxResults(max);
                return query.getResultList();
            });
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findDescSortedByDate(String region, int start, int max) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Destocker.findByRegion");
                query.setParameter("region", region);
                query.setFirstResult(start);
                query.setMaxResults(max);
                return query.getResultList();
            });
        } catch (EntityNotFoundException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findDescSortedByDate(int start, int max) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id IN (SELECT uid FROM produit) ORDER BY dateDestockage DESC ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setFirstResult(start);
                query.setMaxResults(max);
                return query.getResultList();
            });
        } catch (EntityNotFoundException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeOrphans() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id NOT IN (SELECT uid FROM produit) ");
            List<Destocker> lsd = ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                return query.getResultList();
            });
            lsd.forEach(e -> {
                ManagedSessionFactory.submitWrite(em -> {
                    em.remove(em.merge(e));
                    return e;
                }).thenAccept(t -> {
                    System.out.println("Element " + e.getUid() + " supprimee");
                });
            });
        } catch (EntityNotFoundException e) {

        } //
    }

    @Override
    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE dateDestockage BETWEEN ? AND ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, date1.atStartOfDay());
                query.setParameter(2, date2.atTime(23, 59, 59));
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE dateDestockage BETWEEN ? AND ? AND region = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, java.util.Date.from(date1.atStartOfDay().toInstant(ZoneOffset.of("+2"))),
                        TemporalType.DATE);
                query.setParameter(2, java.util.Date.from(date2.atStartOfDay().toInstant(ZoneOffset.of("+2"))),
                        TemporalType.DATE);
                query.setParameter(3, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region, String destination) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE dateDestockage BETWEEN ? AND ? ");
            if (region != null && !region.isBlank()) {
                sb.append("AND region = ? ");
            }
            if (destination != null && !destination.isBlank()) {
                sb.append("AND destination = ? ");
            }
            String sql = sb.toString();
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sql, Destocker.class);
                query.setParameter(1, date1.atStartOfDay());
                query.setParameter(2, date2.atTime(23, 59, 59));
                int i = 3;
                if (region != null && !region.isBlank()) {
                    query.setParameter(i++, region);
                }
                if (destination != null && !destination.isBlank()) {
                    query.setParameter(i, destination);
                }
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findDestockerByProduit(String uid, String region) {
        try {
            boolean isGlobal = region == null || region.isBlank() || "Tout".equalsIgnoreCase(region) || "All".equalsIgnoreCase(region);
            return ManagedSessionFactory.executeRead(em -> {
                Query query;
                if (isGlobal) {
                    query = em.createNativeQuery("SELECT * FROM destocker WHERE product_id = ?", Destocker.class);
                    query.setParameter(1, uid);
                } else {
                    query = em.createNativeQuery("SELECT * FROM destocker WHERE product_id = ? AND region = ?", Destocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                }
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByProduitLot(String uid, String nlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, nlot);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReference(String ref, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE reference = ? AND region = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, ref);
                query.setParameter(2, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReference(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE reference = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, ref);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReferenceAndProduit(String uid, String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ?  AND reference = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, ref);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findAscSortedByDate(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ?  ORDER BY datedestockage ASC ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> mergeSet(Set<Destocker> bulk) {
        ManagedSessionFactory.submitWrite(em -> {
            for (Destocker lj : bulk) {
                em.merge(lj);
            }
            return bulk;
        }).thenAccept(e -> {
            System.out.println("Bulk Destocker merged");
        });
        return new ArrayList<>(bulk);
    }

    private List<Destocker> findDestockerByProduitLot(String uid, String numlot, Date date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND datedestockage = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, numlot);
                query.setParameter(3, date, TemporalType.DATE);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Destocker> find(String ref, String uid, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND reference = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, numlot);
                query.setParameter(3, ref);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReference(String ref, String uid, String numlot) {
        return find(ref, uid, numlot);
    }

    @Override
    public double sum(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM((d.quantite*m.quantContenu)) q FROM destocker d,mesure m "
                    + "WHERE d.product_id = ? AND d.mesure_id=m.uid ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, uid);
                Double d = (Double) query.getSingleResult();
                return d == null ? 0 : d;
            });
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public Destocker findCustomised(String uid, String numlot, String ref, LocalDateTime dateStocker) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND reference = ? AND datedestockage = ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, numlot);
                query.setParameter(3, ref);
                query.setParameter(4, dateStocker);
                List<Destocker> dtks = query.getResultList();
                if (dtks.isEmpty()) {
                    return null;
                }
                return dtks.get(0);
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    public static List<Destocker> getDestockers() {
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNamedQuery("Destocker.findAll");
            return query.getResultList();
        });
    }

    @Override
    public List<Destocker> findUnSyncedDestockers(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker p WHERE p.updated_at >= ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, offline);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM destocker p WHERE p.uid = ? AND p.updated_at = ?");
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, atime);
            List<Destocker> result = query.getResultList();
            return !result.isEmpty();
        });
    }
}
