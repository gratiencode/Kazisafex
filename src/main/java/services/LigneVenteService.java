/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.LigneVenteStorage;
import data.Category;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.LigneVente;
import data.Produit;
import data.Stocker;
import data.Vente;
import delegates.StockerDelegate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import utilities.Peremption;

/**
 *
 * @author eroot
 */
public class LigneVenteService implements LigneVenteStorage {

    @Override
    public boolean isExists(long uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM LigneVente c WHERE c.uid = :id";
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

    public LigneVenteService() {
        //initializing...
    }

    @Override
    public LigneVente createLigneVente(LigneVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getProductId().getNomProduit() + " vendu");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            tx.commit();
            return cat;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
        }
        return null;
    }

    @Override
    public LigneVente updateLigneVente(LigneVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getProductId().getNomProduit() + " vendu modifiee");
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
    public void deleteLigneVente(LigneVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getProductId().getNomProduit() + " vendu supprimree");
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
    public LigneVente findLigneVente(Long catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(LigneVente.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(LigneVente.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM ligne_vente WHERE deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> (Long) em.createNativeQuery(sb.toString()).getSingleResult());
            }
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<LigneVente> findLigneVentes() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ligne_vente.uid, clientId, quantite, montantcdf, montantusd, COALESCE(coutAchat,0) as coutAchat,numlot,"
                    + "prixUnit, mesure_id, product_id, reference_uid, ligne_vente.deleted_at, ligne_vente.updated_at FROM ligne_vente "
                    + "JOIN produit ON ligne_vente.product_id=produit.uid");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), LigneVente.class)
                        .getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findLigneVentes(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("LigneVente.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("LigneVente.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id  = ? AND deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduitRegion(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id  = ? AND deleted_at IS NULL AND reference_uid IN (SELECT uid FROM vente WHERE region = ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByReference(Integer uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE reference_uid = ? AND deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public Double sumProductByLot(String prodId, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(l.quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id  = ? AND l.numlot = ? AND l.mesure_id=m.uid AND l.product_id=m.produit_id AND l.deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, prodId);
                    query.setParameter(2, numlot);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, prodId);
            query.setParameter(2, numlot);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region = ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduitWithLotInUnit(String idpro, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.numlot = ? AND l.mesure_id=m.uid AND l.deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, idpro);
                    query.setParameter(2, lot);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, lot);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot, LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND deleted_at IS NULL AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? )");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, debut.atStartOfDay());
                    query.setParameter(4, fin.atStartOfDay());
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, debut.atStartOfDay())
                    .setParameter(4, fin.atStartOfDay());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot, LocalDate debut, LocalDate fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND deleted_at IS NULL AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region = ? AND v.dateVente BETWEEN ? AND ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, region);
                    query.setParameter(4, debut.atStartOfDay());
                    query.setParameter(5, fin.atStartOfDay());
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, region);
            query.setParameter(4, debut.atStartOfDay())
                    .setParameter(5, fin.atStartOfDay());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<LigneVente> findExpiredProducts(LocalDate debut, LocalDate fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT l.clientId,(SUM(COALESCE(l.quantite,0))) as quantite,l.montantusd, l.montantCdf,l.numlot,"
                    + " l.coutAchat, l.uid, l.prixUnit, l.mesure_id, l.product_id, l.reference_uid,l.deleted_at,l.updated_at "
                    + "FROM ligne_vente l WHERE clientId = ? AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region LIKE ? AND v.dateVente BETWEEN ? AND ? AND v.observation LIKE ? ) ")
                    .append("GROUP BY l.product_id, l.numlot");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, "RABBISH");
                    query.setParameter(2, region);
                    query.setParameter(3, debut.atStartOfDay())
                            .setParameter(4, fin.atTime(23, 59, 59))
                            .setParameter(5, "DEC%");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, "RABBISH");
            query.setParameter(2, region);
            query.setParameter(3, debut.atStartOfDay())
                    .setParameter(4, fin.atTime(23, 59, 59))
                    .setParameter(5, "DEC%");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduit(String idpro) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid AND l.deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, idpro);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String idpro, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid AND l.deleted_at IS NULL AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region = ? )");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, idpro);
                    query.setParameter(2, region);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, region);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumSaleByProduct(String proId, LocalDate d, LocalDate f) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.montantUsd) s FROM ligne_vente r WHERE r.product_id = ? AND r.deleted_at IS NULL "
                    + "AND r.reference_uid IN ")
                    .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, proId);
                    query.setParameter(2, d.atStartOfDay());
                    query.setParameter(3, f.atStartOfDay());
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay())
                    .setParameter(3, f.atStartOfDay());
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public double sumSaleByProduct(String proId, LocalDate d, LocalDate f, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.montantUsd) s FROM ligne_vente r WHERE r.product_id = ? "
                    + "AND r.deleted_at IS NULL AND r.reference_uid IN ")
                    .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, proId);
                    query.setParameter(2, d.atStartOfDay());
                    query.setParameter(3, f.atStartOfDay());
                    query.setParameter(4, region);
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay())
                    .setParameter(3, f.atStartOfDay())
                    .setParameter(4, region);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<LigneVente> mergeSet(Set<LigneVente> bulk) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (LigneVente lj : bulk) {
                    em.merge(lj);
                }
                return bulk;
            }).thenAccept(e -> {
                System.out.println("Bulk LigneVente merged");
            });
            return new ArrayList<>(bulk);
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (LigneVente lj : bulk) {
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
        Enumeration<LigneVente> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public double sumByProduit(String idpro, LocalDate d1, LocalDate d2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid "
                    + "AND l.deleted_at IS NULL AND reference_uid IN (SELECT uid FROM vente WHERE dateVente BETWEEN ? AND ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, idpro);
                    query.setParameter(2, d1.atStartOfDay());
                    query.setParameter(3, d2.atStartOfDay());
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1.atStartOfDay());
            query.setParameter(3, d2.atStartOfDay());
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String idpro, LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? "
                    + "AND l.deleted_at IS NULL AND l.mesure_id=m.uid AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE dateVente BETWEEN ? AND ? AND v.region = ? )");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, idpro);
                    query.setParameter(2, d1.atStartOfDay());
                    query.setParameter(3, d2.atStartOfDay());
                    query.setParameter(4, region);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1.atStartOfDay());
            query.setParameter(3, d2.atStartOfDay());
            query.setParameter(4, region);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public LigneVente saveLigneVente(LigneVente i, Vente vente4save) {
        if (ManagedSessionFactory.isEmbedded()) {
            i.setReference(vente4save);
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(i);
                return i;
            }).thenAccept(e -> {
                System.out.println("LigneVente enregistree");
            });
            return i;
        }
        EntityTransaction tr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tr.isActive()) {
            tr.begin();
        }
        i.setReference(vente4save);
        ManagedSessionFactory.getEntityManager().persist(i);
        tr.commit();
        return i;
    }

    @Override
    public List<LigneVente> findUnSyncedLigneVentes(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT p.uid , p.clientId, p.montantCdf, p.montantUsd, p.numlot, p.prixUnit, p.quantite,"
                    + "  COALESCE(p.coutAchat,0) as coutAchat, p.mesure_id, p.product_id, p.reference_uid,"
                    + "  p.deleted_at, p.updated_at FROM ligne_vente p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(long uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ligne_vente p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<LigneVente> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<LigneVente> result = query.getResultList();
        return !result.isEmpty();
    }

    private String getLocation(String idpro) {
        List<Stocker> loc = StockerDelegate.findDescSortedByDateStock(idpro);
        if (loc.isEmpty()) {
            return null;
        }
        return loc.get(0).getLocalisation();
    }

    public List<Peremption> showExpiredAtInterval(LocalDate dateExp1, LocalDate dateEpx2, String region) {
        List<Peremption> result = new ArrayList<>();
        List<LigneVente> finds = findExpiredProducts(dateExp1, dateEpx2, region);
        for (LigneVente expin : finds) {
            Produit produit = expin.getProductId();
            String localisation=getLocation(produit.getUid());
            Peremption per = new Peremption();
            per.setCodebar(produit.getCodebar());
            per.setCoutAchat(expin.getCoutAchat());
            per.setDateExpiry(dateExp1);
            per.setLot(expin.getNumlot());
            per.setMesure(expin.getMesureId().getDescription());
            per.setProduit(produit.getNomProduit() + " " + produit.getModele() + " " + produit.getTaille());
            per.setProduitUid(produit.getUid());
            per.setLocalisation(localisation == null ? region : localisation);
            per.setQuantite(expin.getQuantite());
            per.setRegion(region);
            per.setValeur(BigDecimal.valueOf(expin.getQuantite() * expin.getCoutAchat()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            result.add(per);
        }
        return result;
    }
}
