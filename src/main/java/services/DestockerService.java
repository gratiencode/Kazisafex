/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.DestockerStorage;
import java.time.LocalDate;
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
import java.time.ZoneOffset;
import java.util.Date;
import org.eclipse.persistence.expressions.ExpressionOperator;

/**
 *
 * @author eroot
 */
public class DestockerService implements DestockerStorage {

    EntityManager em;

    public DestockerService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Destocker createDestocker(Destocker cat) {
        Destocker ss = findCustomised(cat.getProductId().getUid(), cat.getNumlot(), cat.getReference(), cat.getDateDestockage());
        if (ss!=null) {
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
    public Destocker updateDestocker(Destocker cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();

        return cat;
    }

    @Override
    public void deleteDestocker(Destocker cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Destocker findDestocker(String catId) {
        return em.find(Destocker.class, catId);
    }

    @Override
    public List<Destocker> findDestockers() {
        try {
            Query query = em.createNamedQuery("Destocker.findAll");
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }  //To change body of generated methods, choose Tools | Templates.
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
            sb.append("SELECT SUM(d.quantite*m.quant_contenu) q FROM destocker d, mesure m WHERE d.product_id = ? AND d.mesure_id = m.uid");
            Query query = em.createNativeQuery(sb.toString());
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
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Destocker> findDestockers(int start, int max) {
        try {
            Query query = em.createNamedQuery("Destocker.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> findDescSortedByDate(String region, int start, int max) {
        try {
            Query query = em.createNamedQuery("Destocker.findByRegion");
            query.setParameter("region", region);
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findDescSortedByDate(int start, int max) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker ORDER BY dateDestockage DESC ");
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE dateDestockage BETWEEN ? AND ? ");
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, java.util.Date.from(date1.atStartOfDay().toInstant(ZoneOffset.of("+2"))), TemporalType.DATE);
            query.setParameter(2, java.util.Date.from(date2.atStartOfDay().toInstant(ZoneOffset.of("+2"))), TemporalType.DATE);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, java.util.Date.from(date1.atStartOfDay().toInstant(ZoneOffset.of("+2"))), TemporalType.DATE);
            query.setParameter(2, java.util.Date.from(date2.atStartOfDay().toInstant(ZoneOffset.of("+2"))), TemporalType.DATE);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }  //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Destocker> findDestockerByProduit(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND region = ? ");
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Destocker> mergeSet(Set<Destocker> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Destocker lj : bulk) {
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
        Enumeration<Destocker> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    private List<Destocker> findDestockerByProduitLot(String uid, String numlot, Date date) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND datedestockage = ? ");
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
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
            sb.append("SELECT SUM((d.quantite*m.quant_contenu)) q FROM destocker d,mesure m WHERE d.product_id = ? AND d.mesure_id=m.uid ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public Destocker findCustomised(String uid, String numlot, String ref, Date dateStocker) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM destocker WHERE product_id = ? AND numlot = ? AND reference = ? AND datedestockage = ?");
            Query query = em.createNativeQuery(sb.toString(), Destocker.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ref);
            query.setParameter(4, dateStocker, TemporalType.DATE);
            List<Destocker> dtks = query.getResultList();
            if(dtks.isEmpty()){
                return null;
            }
            return dtks.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

}
