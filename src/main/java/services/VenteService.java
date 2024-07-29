/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.VenteStorage;
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
import data.RetourMagasin;
import data.Vente;

/**
 *
 * @author eroot
 */
public class VenteService implements VenteStorage {

    EntityManager em;

    public VenteService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Vente createVente(Vente cat) {
        EntityTransaction tx = em.getTransaction();
        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            em.persist(cat);
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
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteVente(Vente cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Vente findVente(int catId) {
        return em.find(Vente.class, catId);
    }

    @Override
    public List<Vente> findVentes() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v ORDER BY v.dateVente DESC ");
            Query query = em
                    .createNativeQuery(sb.toString(), Vente.class);
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
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
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
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Vente> findVentes(int start, int max) {
        try {
            Query query = em.createNamedQuery("Vente.findSet");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findByRef(String reference, Date date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.reference = ? AND v.dateVente = ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, reference);
            query.setParameter(2, date, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findAllByDateInterval(Date time, Date date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.dateVente BETWEEN ? AND ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, time, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findAllByDateInterval(Date time, Date date2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, time, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findCreditSaleByRef(String reference) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.reference = ? AND v.observation != ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
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
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
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
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
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
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
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
            Query query = em.createNativeQuery(sb.toString());
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
            Query query = em.createNamedQuery("Vente.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> findDraftedCarts(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.observation = ? AND v.region = ? ");
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
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
            Query query = em.createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumVente(Date date1, Date date2, double taux) {
        Double usd = sumVenteUsd(date1, date2, taux);
        Double dette = sumVenteDette(date1, date2);
        Double rtr = sumRetourMagasin(date1, date2);
        return (usd + dette) - rtr;
    }

    @Override
    public double sumVente(Date date1, Date date2, String region, double taux) {
        Double usd = sumVenteUsd(date1, date2, region, taux);
        //Double dette = sumVenteDette(date1, date2, region);
        Double rtr = sumRetourMagasin(date1, date2, region);
        return (usd - rtr);
    }

    @Override
    public double sumExpenses(Date date1, Date date2, String region, double taux) {
        Double usd = sumOpsUsd(date1, date2, region);
        Double cdf = sumOpsCdf(date1, date2, region);
        return usd + (cdf / taux);
    }

    @Override
    public double sumExpenses(Date date1, Date date2, double taux) {
        Double usd = sumOpsUsd(date1, date2);
        Double cdf = sumOpsCdf(date1, date2);
        return usd + (cdf / taux);
    }

    public Double sumOpsUsd(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Operation.findSumUSDByDateIntervalRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Operation.findSumCDFByDateIntervalRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Operation.findSumCDFByDateInterval");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsUsd(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Operation.findSumUSDByDateInterval");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsd(Date date1, Date date2, double taux) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ((SUM(v.montantcdf)/").append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                .append(" WHERE v.dateVente BETWEEN ? AND ?  AND v.observation != ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            query.setParameter(3, "Drafted");
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsd(Date date1, Date date2, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ((SUM(v.montantcdf)/").append(taux).append(")+SUM(v.montantusd)) as c FROM vente v ")
                .append(" WHERE v.dateVente BETWEEN ? AND ? AND v.region = ?  AND v.observation != ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
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

    public Double sumVenteCdf(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumCDFRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(Date date1, Date date2) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumDebt");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(Date date1, Date date2, String region) {
        try {
            Query query = em.createNamedQuery("Vente.findBySumDebtRegion");
            query.setParameter("date1", date1, TemporalType.DATE);
            query.setParameter("date2", date2, TemporalType.DATE);
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumRetourMagasin(Date date1, Date date2) {
        List<RetourMagasin> retrm = findRetours(date1, date2);
        double s = 0;
        for (RetourMagasin rtr : retrm) {
            Mesure mp = rtr.getMesureId();
            s += ((rtr.getQuantite() * mp.getQuantContenu()) * (rtr.getPrixVente() / mp.getQuantContenu()));
        }
        return s;
    }

    private List<RetourMagasin> findRetours(Date d0m, Date d1m) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM retour_magasin rm WHERE rm.date_ BETWEEN ? AND ? ");
        Query query = em.createNativeQuery(sb.toString(), RetourMagasin.class);
        query.setParameter(1, d0m, TemporalType.DATE);
        query.setParameter(2, d1m, TemporalType.DATE);
        return query.getResultList();
    }

    private List<RetourMagasin> findRetours(Date d0m, Date d1m, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM retour_magasin rm WHERE rm.date_ BETWEEN ? AND ? AND rm.region = ?");
        Query query = em.createNativeQuery(sb.toString(), RetourMagasin.class);
        query.setParameter(1, d0m, TemporalType.DATE);
        query.setParameter(2, d1m, TemporalType.DATE);
        query.setParameter(3, region);
        return query.getResultList();
    }

    public Double sumRetourMagasin(Date date1, Date date2, String region) {
        List<RetourMagasin> retrm
                = findRetours(date1, date2, region);
        double s = 0;
        for (RetourMagasin rtr : retrm) {
            Mesure mp = rtr.getMesureId();
            s += ((rtr.getQuantite() * mp.getQuantContenu()) * (rtr.getPrixVente() / mp.getQuantContenu()));
        }
        return s;
    }

    @Override
    public HashMap<Long, String> getTop10ProductDesc() {
        HashMap<Long, String> hash = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(l.product_id) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val FROM ligne_vente l, vente v, produit p "
                + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid GROUP BY l.product_id ORDER by count(l.product_id) DESC LIMIT 10");
        Query query = em.createNativeQuery(sb.toString());
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            Long e1 = Long.valueOf(String.valueOf(obj[0]));
            String nv = String.valueOf(obj[1]);
            hash.put(e1, nv);
        }
        return hash;
    }

    @Override
    public HashMap<Long, String> getTop10ProductDesc(String region) {
        HashMap<Long, String> hash = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(l.product_id) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val FROM ligne_vente l, vente v, produit p "
                + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid AND v.region = ? GROUP BY l.product_id ORDER by count(l.product_id) DESC LIMIT 10");
        Query query = em.createNativeQuery(sb.toString());
        query.setParameter(1, region);
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            Long e1 = (Long) obj[0];
            String nv = String.valueOf(obj[1]);
            hash.put(e1, nv);
        }
        return hash;
    }

    @Override
    public double sumCoutAchatArticleVendu(Date d1, Date d2, String region) {
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
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, d1, TemporalType.DATE);
            query.setParameter(2, d2, TemporalType.DATE);
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
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Vente lj : bulk) {
            i++;
            em.merge(lj);
            if (i % 16 == 0) {
                etr.commit();
                em.clear();
                EntityTransaction etr2 = em.getTransaction();
                if (!etr2.isActive()) {
                    etr2.begin();
                }

            }
        }
        etr.commit();
        Enumeration<Vente> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }
}
