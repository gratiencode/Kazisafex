/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.StockerStorage;
import data.Recquisition;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Stocker;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 *
 * @author eroot
 */
public class StockerService implements StockerStorage {

    EntityManager em;

    public StockerService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Stocker createStocker(Stocker cat) {
        List<Stocker> ss = findStockerByProduitLot(cat.getProductId().getUid(), cat.getNumlot(), cat.getDateStocker());
        if (ss.isEmpty()) {
            EntityTransaction tx = em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            em.persist(cat);
            tx.commit();
        }
        return cat;
    }

    @Override
    public Stocker updateStocker(Stocker cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteStocker(Stocker cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Stocker findStocker(String catId) {
        return em.find(Stocker.class, catId);
    }

    @Override
    public List<Stocker> findStockers() {
        try {
            Query query = em.createNamedQuery("Stocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findStockers(int start, int max) {
        try {
            Query query = em.createNamedQuery("Stocker.findAll");
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
            sb.append("SELECT SUM(s.quantite * m.quantcontenu) q FROM stocker s,mesure m WHERE s.product_id = ? AND s.mesure_id = m.uid ");
            Query query = em.createNativeQuery(sb.toString());
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
            return ((Long) em.createNativeQuery(sb.toString()).getSingleResult());
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Stocker> findStockerByProduit(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ?");
            Query query = SafeConnectionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, java.util.Date.from(date1.atStartOfDay().toInstant(ZoneOffset.of("+2"))), TemporalType.DATE);
            query.setParameter(2, java.util.Date.from(date2.atStartOfDay().toInstant(ZoneOffset.of("+2"))), TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Stocker> findStockerByProduitLot(String objId, String lot, Date date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? AND s.numlot = ? AND s.datestocker  = ?");
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, objId);
            query.setParameter(2, lot);
            query.setParameter(3, date, TemporalType.TIMESTAMP);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, java.util.Date.from(date1.atStartOfDay(ZoneId.systemDefault()).toInstant()), TemporalType.DATE);
            query.setParameter(2, java.util.Date.from(date2.atStartOfDay(ZoneId.systemDefault()).toInstant()), TemporalType.DATE);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, livuid);
            query.setParameter(2, prouid0);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> findByDateExpInterval(Date time, Date darg) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.dateExpir BETWEEN ? AND ? ");
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, time, TemporalType.DATE);
            query.setParameter(2, darg, TemporalType.DATE);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNamedQuery("Stocker.findByRegion");
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
            Query query = em.createNamedQuery("Stocker.findByRegion");
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Stocker> mergeSet(Set<Stocker> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Stocker lj : bulk) {
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
        Enumeration<Stocker> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Stocker> toFefoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stocker s WHERE s.product_id = ? ORDER BY s.dateExpir ASC");
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Stocker.class);
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
            sb.append("SELECT SUM((s.quantite*m.quant_contenu)) q FROM stocker s, mesure m WHERE s.product_id = ? AND s.mesure_id=m.uid ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

}
