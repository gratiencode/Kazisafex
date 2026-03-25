/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.DestockerStorage;
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
import data.Destocker;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class DestockerService implements DestockerStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Destocker c WHERE c.uid = :id";
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

    public DestockerService() {
        // initializing...
    }

    @Override
    public Destocker createDestocker(Destocker cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
            });
            return cat;
        }
        Destocker ss = findCustomised(cat.getProductId().getUid(), cat.getNumlot(), cat.getReference(),
                cat.getDateDestockage());
        if (ss == null) {
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
    public Destocker updateDestocker(Destocker cat) {
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
    public void deleteDestocker(Destocker cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
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
    public Destocker findDestocker(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Destocker.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Destocker.class, catId);
    }

    @Override
    public List<Destocker> findDestockers() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Destocker.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Destocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findDestockerByProduit(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, objId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, objId);
            return query.getResultList();
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
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, prodId);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, prodId);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM destocker");
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
    public List<Destocker> findDestockers(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Destocker.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Destocker.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findDescSortedByDate(String region, int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Destocker.findByRegion");
                    query.setParameter("region", region);
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Destocker.findByRegion");
            query.setParameter("region", region);
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (EntityNotFoundException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findDescSortedByDate(int start, int max) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id IN (SELECT uid FROM produit) ORDER BY dateDestockage DESC ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (EntityNotFoundException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeOrphans() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id NOT IN (SELECT uid FROM produit) ");
            if (ManagedSessionFactory.isEmbedded()) {
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
                return;
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            List<Destocker> lsd = query.getResultList();
            lsd.forEach(e -> {
                ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(e));
            });
        } catch (EntityNotFoundException e) {
            
        } //
    }

    @Override
    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE dateDestockage BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE dateDestockage BETWEEN ? AND ? AND region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, java.util.Date.from(date1.atStartOfDay().toInstant(ZoneOffset.of("+2"))),
                            TemporalType.DATE);
                    query.setParameter(2, java.util.Date.from(date2.atStartOfDay().toInstant(ZoneOffset.of("+2"))),
                            TemporalType.DATE);
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, java.util.Date.from(date1.atStartOfDay().toInstant(ZoneOffset.of("+2"))),
                    TemporalType.DATE);
            query.setParameter(2, java.util.Date.from(date2.atStartOfDay().toInstant(ZoneOffset.of("+2"))),
                    TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findDestockerByProduit(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByProduitLot(String uid, String nlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, nlot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, nlot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReference(String ref, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE reference = ? AND region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, ref);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, ref);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReference(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findByReferenceAndProduit(String uid, String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ?  AND reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findAscSortedByDate(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ?  ORDER BY datedestockage ASC ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> mergeSet(Set<Destocker> bulk) {
        if (ManagedSessionFactory.isEmbedded()) {
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
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Destocker lj : bulk) {
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
        Enumeration<Destocker> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    private List<Destocker> findDestockerByProduitLot(String uid, String numlot, Date date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND datedestockage = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, date, TemporalType.DATE);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, date, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Destocker> find(String ref, String uid, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ref);
            return query.getResultList();
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

    @Override
    public Destocker findCustomised(String uid, String numlot, String ref, LocalDateTime dateStocker) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND reference = ? AND datedestockage = ?");
            if (ManagedSessionFactory.isEmbedded()) {
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
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ref);
            query.setParameter(4, dateStocker);
            List<Destocker> dtks = query.getResultList();
            if (dtks.isEmpty()) {
                return null;
            }
            return dtks.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    public static List<Destocker> getDestockers() {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Destocker.findAll");
                return query.getResultList();
            });
        }
        EntityManager mem = ManagedSessionFactory.getEntityManager();
        try {
            Query query = mem.createNamedQuery("Destocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findUnSyncedDestockers(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, offline);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM destocker p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Destocker.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Destocker> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Destocker.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Destocker> result = query.getResultList();
        return !result.isEmpty();
    }
}
