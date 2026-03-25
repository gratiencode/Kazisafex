/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.VenteStorage;
import data.LigneVente;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Mesure;
import data.Recquisition;
import data.RetourMagasin;
import data.Vente;
import delegates.RecquisitionDelegate;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import tools.Constants;
import tools.TopTen;

/**
 *
 * @author eroot
 */
public class VenteService implements VenteStorage {

    @Override
    public boolean isExists(int uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Vente c WHERE c.uid = :id";
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

    public VenteService() {
        // initializing...
    }

    @Override
    public Vente createVente(Vente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
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
    public Vente updateVente(Vente cat) {
        try {
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
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void deleteVente(Vente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " supprimee");
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
    public Vente findVente(int catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Vente.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Vente.class, catId);
    }

    @Override
    public List<Vente> findVentes() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v ORDER BY v.dateVente DESC ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory
                        .executeRead(em -> em.createNativeQuery(sb.toString(), Vente.class).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            return query.getResultList();
        } catch (NoResultException | jakarta.persistence.EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public Vente findVenteByRef(String ref) {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> (Vente) (em.createNativeQuery(sb.toString(), Vente.class)
                        .setParameter(1, ref).getSingleResult()));
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, ref);
            return (Vente) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM vente");
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
    public List<Vente> findVentes(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Vente.findSet");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findSet");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findByRef(String reference, LocalDate date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.reference = ? AND v.dateVente BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, reference);
                    query.setParameter(2, date.atStartOfDay());
                    query.setParameter(3, date.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, reference);
            query.setParameter(2, date.atStartOfDay());
            query.setParameter(3, date.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findAllByDateInterval(LocalDate time, LocalDate date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.dateVente BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), Vente.class)
                        .setParameter(1, time)
                        .setParameter(2, date2)
                        .getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, time);
            query.setParameter(2, date2);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findAllByDateInterval(LocalDate time, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(),
                            Vente.class);
                    query.setParameter(1, time.atStartOfDay());
                    query.setParameter(2, date2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, time.atStartOfDay());
            query.setParameter(2, date2.atTime(23, 59, 59));
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumCdfSaleOf(LocalDate time, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(v.montantCdf,0)) FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, time.atStartOfDay());
                    query.setParameter(2, date2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, time.atStartOfDay());
            query.setParameter(2, date2.atTime(23, 59, 59));
            query.setParameter(3, region);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumUsdSaleOf(LocalDate time, LocalDate date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(v.montantUsd,0)) FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, time.atStartOfDay());
                    query.setParameter(2, date2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, time.atStartOfDay());
            query.setParameter(2, date2.atTime(23, 59, 59));
            query.setParameter(3, region);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Vente> findCreditSaleByRef(String reference) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.reference = ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, reference);
                    query.setParameter(2, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, reference);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findDraftedCarts() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.observation = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findCreditSalesFromRegion(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.region = ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, region);
                    query.setParameter(2, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findCreditSales() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Double sumPayedCredit(String uid, double taux2change) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ((SUM(t.montantcdf)/")
                    .append(taux2change)
                    .append(")+SUM(t.montantusd)) s FROM traisorerie t WHERE t.reference = ? ");
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
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVentes(String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Vente.findByRegion");
                    query.setParameter("region", region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findDraftedCarts(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.observation = ? AND v.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, "Drafted");
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findByRef(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumVente(LocalDate date1, LocalDate date2, double taux, String devise) {
        Double usd = sumVenteUsd(date1, date2, taux, devise);
        Double dette = sumVenteDette(date1, date2);
        Double rtr = sumRetourMagasin(date1, date2);
        return (usd + dette) - rtr;
    }

    @Override
    public double sumVente(LocalDate date1, LocalDate date2, String region, double taux) {
        Double usd = sumVenteUsd(date1, date2, region, taux);
        Double rtr = sumRetourMagasin(date1, date2, region);
        return (usd - rtr);
    }

    @Override
    public double sumExpenses(LocalDate date1, LocalDate date2, String region, double taux) {
        Double usd = sumOpsUsd(date1, date2, region);
        Double cdf = sumOpsCdf(date1, date2, region);
        return usd + (cdf / taux);
    }

    @Override
    public double sumExpenses(LocalDate date1, LocalDate date2, double taux) {
        Double usd = sumOpsUsd(date1, date2);
        Double cdf = sumOpsCdf(date1, date2);
        return usd + (cdf / taux);
    }

    public Double sumOpsUsd(LocalDate date1, LocalDate date2, String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Operation.findSumUSDByDateIntervalRegion");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    query.setParameter("region", region);
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumUSDByDateIntervalRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(LocalDate date1, LocalDate date2, String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Operation.findSumCDFByDateIntervalRegion");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    query.setParameter("region", region);
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumCDFByDateIntervalRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(LocalDate date1, LocalDate date2) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Operation.findSumCDFByDateInterval");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumCDFByDateInterval");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsUsd(LocalDate date1, LocalDate date2) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Operation.findSumUSDByDateInterval");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumUSDByDateInterval");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsd(LocalDate date1, LocalDate date2, double taux, String devise) {
        StringBuilder sb = new StringBuilder();
        if (devise.equals("USD")) {
            sb.append("SELECT ((SUM(v.montantcdf)/")
                    .append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                    .append(" WHERE v.dateVente BETWEEN ? AND ?  AND v.observation != ? ");
        } else {
            sb.append("SELECT ((SUM(COALESCE(v.montantcdf,0)))+(SUM(COALESCE(v.montantusd,0))*")
                    .append(taux).append(")) as c FROM vente v ")
                    .append(" WHERE v.dateVente BETWEEN ? AND ?  AND v.observation NOT LIKE ? "
                            + "AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?"
                            + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?");
        }
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atStartOfDay());
                    query.setParameter(3, "Drafted");
                    query.setParameter(4, "DEC%");
                    query.setParameter(5, "Ajust%");
                    query.setParameter(6, "Cor%");
                    query.setParameter(7, "RTR%");
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }

            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atStartOfDay());
            query.setParameter(3, "Drafted");
            query.setParameter(4, "DEC%");
            query.setParameter(5, "Ajust%");
            query.setParameter(6, "Cor%");
            query.setParameter(7, "RTR%");
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsd(LocalDate date1, LocalDate date2, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ((SUM(v.montantcdf)/").append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                .append(" WHERE v.dateVente BETWEEN ? AND ? AND v.region = ?  AND v.observation != ? ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atStartOfDay());
                    query.setParameter(3, region);
                    query.setParameter(4, "Drafted");
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atStartOfDay());
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public Double sumCoutAchat(LocalDate date1, LocalDate date2, String region) {
        double result = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ligne_vente WHERE reference_uid IN ")
                .append(" (SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ?  AND v.observation != ? )");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                List<LigneVente> lgvs = ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atStartOfDay());
                    query.setParameter(3, region);
                    query.setParameter(4, "Drafted");
                    return query.getResultList();
                });
                for (LigneVente lgv : lgvs) {
                    Double ca = lgv.getCoutAchat();
                    if (ca == null) {
                        Recquisition geto = RecquisitionDelegate.getHeaderRecq(Constants.getStringPref("meth", "fifo"),
                                lgv.getProductId(), region);
                        double line1 = ((lgv.getQuantite() * lgv.getMesureId().getQuantContenu())
                                * (geto.getCoutAchat() / geto.getMesureId().getQuantContenu()));
                        result += line1;
                    } else {
                        result += (lgv.getQuantite() * ca);
                    }
                }
                return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atStartOfDay());
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            List<LigneVente> lgvs = query.getResultList();
            for (LigneVente lgv : lgvs) {
                Double ca = lgv.getCoutAchat();
                if (ca == null) {
                    Recquisition geto = RecquisitionDelegate.getHeaderRecq(Constants.getStringPref("meth", "fifo"),
                            lgv.getProductId(), region);

                    double line1 = ((lgv.getQuantite() * lgv.getMesureId().getQuantContenu())
                            * (geto.getCoutAchat() / geto.getMesureId().getQuantContenu()));
                    result += line1;
                } else {
                    result += (lgv.getQuantite() * ca);
                }
            }
            return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public Double sumCoutAchat(LocalDate date1, LocalDate date2) {
        double result = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ligne_vente WHERE reference_uid IN ")
                .append(" (SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ?  AND v.observation != ? )");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                List<LigneVente> lgvs = ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, date1.atStartOfDay());
                    query.setParameter(2, date2.atStartOfDay());
                    query.setParameter(3, "Drafted");
                    return query.getResultList();
                });
                for (LigneVente lgv : lgvs) {
                    Double ca = lgv.getCoutAchat();
                    if (ca == null) {
                        Recquisition geto = RecquisitionDelegate.getHeaderRecq(Constants.getStringPref("meth", "fifo"),
                                lgv.getProductId());
                        double line1 = ((lgv.getQuantite() * lgv.getMesureId().getQuantContenu())
                                * (geto.getCoutAchat() / geto.getMesureId().getQuantContenu()));
                        result += line1;
                    } else {
                        result += (lgv.getQuantite() * ca);
                    }
                }
                return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, date1.atStartOfDay());
            query.setParameter(2, date2.atStartOfDay());
            query.setParameter(3, "Drafted");
            List<LigneVente> lgvs = query.getResultList();
            for (LigneVente lgv : lgvs) {
                Double ca = lgv.getCoutAchat();
                if (ca == null) {
                    Recquisition geto = RecquisitionDelegate.getHeaderRecq(Constants.getStringPref("meth", "fifo"),
                            lgv.getProductId());
                    double line1 = ((lgv.getQuantite() * lgv.getMesureId().getQuantContenu())
                            * (geto.getCoutAchat() / geto.getMesureId().getQuantContenu()));
                    result += line1;
                } else {
                    result += (lgv.getQuantite() * ca);
                }
            }
            return BigDecimal.valueOf(result)
                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public List<Recquisition> findRecquisitionByProduitLot(String objId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, objId);
                    query.setParameter(2, lot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, objId);
            query.setParameter(2, lot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    public Double sumVenteUsd(String cltid, Date date1, Date date2, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ((SUM(v.montantcdf)/").append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                .append(" WHERE v.dateVente BETWEEN ? AND ? AND v.region = ?  AND v.observation != ? ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, date1, TemporalType.DATE);
                    query.setParameter(2, date2, TemporalType.DATE);
                    query.setParameter(3, region);
                    query.setParameter(4, "Drafted");
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteCdf(LocalDate date1, LocalDate date2, String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Vente.findBySumCDFRegion");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    query.setParameter("region", region);
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumCDFRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(LocalDate date1, LocalDate date2) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Vente.findBySumDebt");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumDebt");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(LocalDate date1, LocalDate date2, String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Vente.findBySumDebtRegion");
                    query.setParameter("date1", date1.atStartOfDay());
                    query.setParameter("date2", date2.atStartOfDay());
                    query.setParameter("region", region);
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumDebtRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumRetourMagasin(LocalDate date1, LocalDate date2) {
        List<RetourMagasin> retrm = findRetours(date1, date2);
        double s = 0;
        for (RetourMagasin rtr : retrm) {
            Mesure mp = rtr.getMesureId();
            s += ((rtr.getQuantite() * mp.getQuantContenu()) * (rtr.getPrixVente() / mp.getQuantContenu()));
        }
        return s;
    }

    private List<RetourMagasin> findRetours(LocalDate d0m, LocalDate d1m) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM retour_magasin rm WHERE rm.date_ BETWEEN ? AND ? ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), RetourMagasin.class);
                query.setParameter(1, d0m.atStartOfDay());
                query.setParameter(2, d1m.atStartOfDay());
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), RetourMagasin.class);
        query.setParameter(1, d0m.atStartOfDay());
        query.setParameter(2, d1m.atStartOfDay());
        return query.getResultList();
    }

    private List<RetourMagasin> findRetours(LocalDate d0m, LocalDate d1m, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM retour_magasin rm WHERE rm.date_ BETWEEN ? AND ? AND rm.region = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), RetourMagasin.class);
                query.setParameter(1, d0m.atStartOfDay());
                query.setParameter(2, d1m.atStartOfDay());
                query.setParameter(3, region);
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), RetourMagasin.class);
        query.setParameter(1, d0m.atStartOfDay());
        query.setParameter(2, d1m.atStartOfDay());
        query.setParameter(3, region);
        return query.getResultList();
    }

    public Double sumRetourMagasin(LocalDate date1, LocalDate date2, String region) {
        List<RetourMagasin> retrm = findRetours(date1, date2, region);
        double s = 0;
        for (RetourMagasin rtr : retrm) {
            Mesure mp = rtr.getMesureId();
            s += ((rtr.getQuantite() * mp.getQuantContenu()) * (rtr.getPrixVente() / mp.getQuantContenu()));
        }
        return s;
    }

    @Override
    public List<TopTen> getTop10ProductDesc() {
        List<TopTen> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT SUM(l.quantite) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val, m.description FROM ligne_vente l, mesure m, produit p "
                        + "WHERE p.uid = l.product_id AND l.mesure_id=m.uid AND l.clientId NOT LIKE ? AND l.clientId NOT LIKE ? "
                        + "AND l.clientId NOT LIKE ? AND l.clientId NOT LIKE ? GROUP BY l.product_id ORDER by cp DESC");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                List<Object[]> objs = query
                        .setParameter(1, "%INV%")
                        .setParameter(2, "-")
                        .setParameter(3, "RABBISH")
                        .setParameter(4, "EX%")
                        .setMaxResults(10).getResultList();
                for (Object[] obj : objs) {
                    Double e1 = Double.valueOf(String.valueOf(obj[0]));
                    String nv = String.valueOf(obj[1]);
                    String mesure = String.valueOf(obj[2]);
                    result.add(new TopTen(nv, e1, mesure));
                }
                return result;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        List<Object[]> objs = query.setParameter(1, "%INV%")
                .setParameter(2, "-")
                .setParameter(3, "RABBISH")
                .setParameter(4, "EX%")
                .setMaxResults(10).getResultList();
        for (Object[] obj : objs) {
            Double e1 = Double.valueOf(String.valueOf(obj[0]));
            String nv = String.valueOf(obj[1]);
            String mesure = String.valueOf(obj[2]);
            result.add(new TopTen(nv, e1, mesure));
        }
        return result;
    }

    @Override
    public List<TopTen> getTop10ProductDesc(String region) {
        List<TopTen> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT SUM(l.quantite) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val, m.description FROM ligne_vente l, vente v, produit p, mesure m "
                        + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid AND l.mesure_id=m.uid AND v.region = ? AND l.clientId NOT LIKE ? AND l.clientId NOT LIKE ? "
                        + "AND l.clientId NOT LIKE ? AND l.clientId NOT LIKE ? GROUP BY l.product_id ORDER by cp DESC ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                List<Object[]> objs = query.setParameter(1, region)
                        .setParameter(2, "#INV%")
                        .setParameter(3, "-")
                        .setParameter(4, "RABBISH")
                        .setParameter(5, "EX%")
                        .setMaxResults(10).getResultList();
                for (Object[] obj : objs) {
                    Double e1 = Double.valueOf(String.valueOf(obj[0]));
                    String nv = String.valueOf(obj[1]);
                    String mesure = String.valueOf(obj[2]);
                    result.add(new TopTen(nv, e1, mesure));
                }
                return result;
            });
        }

        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        List<Object[]> objs = query.setParameter(1, region)
                .setParameter(2, "#INV%")
                .setParameter(3, "-")
                .setParameter(4, "RABBISH")
                .setParameter(5, "EX%")
                .setMaxResults(10).getResultList();
        for (Object[] obj : objs) {
            Double e1 = Double.valueOf(String.valueOf(obj[0]));
            String nv = String.valueOf(obj[1]);
            String mesure = String.valueOf(obj[2]);
            result.add(new TopTen(nv, e1, mesure));
        }
        return result;
    }

    @Override
    public double sumCoutAchatArticleVendu(LocalDate d1, LocalDate d2, String region) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND (v.observation != ? AND v.observation != ?))) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id=w.product_id) as t");
        } else {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? AND (v.observation != ? AND v.observation != ?) )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id = w.product_id) as t");
        }
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay());
                    query.setParameter(2, d2.atStartOfDay());
                    if (region != null) {
                        query.setParameter(3, region);
                        query.setParameter(4, "Drafted");
                        query.setParameter(5, "CORRECTION");
                    } else {
                        query.setParameter(3, "Drafted");
                        query.setParameter(4, "CORRECTION");
                    }
                    Double r = (Double) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay());
            query.setParameter(2, d2.atStartOfDay());
            if (region != null) {
                query.setParameter(3, region);
                query.setParameter(4, "Drafted");
                query.setParameter(5, "CORRECTION");
            } else {
                query.setParameter(3, "Drafted");
                query.setParameter(4, "CORRECTION");
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public List<Vente> mergeSet(Set<Vente> bulk) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (Vente lj : bulk) {
                    em.merge(lj);
                }
                return bulk;
            }).thenAccept(e -> {
                System.out.println("Bulk Vente merged");
            });
            return new ArrayList<>(bulk);
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Vente lj : bulk) {
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
        Enumeration<Vente> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public double getSumVenteFor(String clt) {
        LocalDate day0 = LocalDate.now().withDayOfMonth(1);
        LocalDate d1 = LocalDate.now().withDayOfMonth(31);

        return 0;
        // sumVente(clt,date1, date2, CompactMode.getRegion(), 0);
    }

    public static List<Vente> getVentes() {

        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Vente.findSet");
                    return query.getResultList();
                });
            }
            EntityManager mem = ManagedSessionFactory.getEntityManager();
            Query query = mem.createNamedQuery("Vente.findSet");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findUnSyncedVentes(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT uid, dateVente, deviseDette, echeance, COALESCE(latitude,0) as latitude, libelle,"
                    + "  COALESCE(longitude,0) as longitude, COALESCE(montantCdf,0) as montantCdf,"
                    + "  COALESCE(montantDette,0) as montantDette,"
                    + "  COALESCE(montantUsd,0) as montantUsd,"
                    + "  observation, payment, reference,"
                    + "  region, clientId, deleted_at,"
                    + "  updated_at FROM vente p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(int uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM vente p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Vente.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Vente> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Vente> result = query.getResultList();
        return !result.isEmpty();
    }
}
