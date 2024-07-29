/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.LigneVenteStorage;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.LigneVente;
import data.Vente;
import jakarta.persistence.EntityManager;

/**
 *
 * @author eroot
 */
public class LigneVenteService implements LigneVenteStorage {

    EntityManager em;

    public LigneVenteService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public LigneVente createLigneVente(LigneVente cat) {
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
    public LigneVente updateLigneVente(LigneVente cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteLigneVente(LigneVente cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public LigneVente findLigneVente(Long catId) {
        return em.find(LigneVente.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM ligne_vente");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<LigneVente> findLigneVentes() {
        try {
            Query query = em.createNamedQuery("LigneVente.findAll");
            return query.getResultList();
        } catch (NoResultException | jakarta.persistence.EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findLigneVentes(int start, int max) {
        try {
            Query query = em.createNamedQuery("LigneVente.findAll");
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
            sb.append("SELECT * FROM ligne_vente WHERE product_id  = ? ");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
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
            sb.append("SELECT * FROM ligne_vente WHERE product_id  = ? AND reference_uid IN (SELECT uid FROM vente WHERE region = ?)");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
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
            sb.append("SELECT * FROM ligne_vente WHERE reference_uid = ?");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Double sumProductByLot(String prodId, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(l.quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id  = ? AND l.numlot = ? AND l.mesure_id=m.uid AND l.product_id=m.produit_id");
            Query query = em.createNativeQuery(sb.toString());
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
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ?");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region = ?)");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
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
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.numlot = ? AND l.mesure_id=m.uid");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, lot);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot, Date debut, Date fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? )");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, debut, TemporalType.DATE).setParameter(4, fin, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<LigneVente> findByProduitWithLot(String uid, String numlot, Date debut, Date fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE product_id = ? AND numlot = ? AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region = ? AND v.dateVente BETWEEN ? AND ?)");
            Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, region);
            query.setParameter(4, debut, TemporalType.DATE)
                    .setParameter(5, fin, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduit(String idpro) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid");
            Query query = em.createNativeQuery(sb.toString());
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
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE v.region = ? )");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, region);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<LigneVente> mergeSet(Set<LigneVente> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (LigneVente lj : bulk) {
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
        Enumeration<LigneVente> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public double sumByProduit(String idpro, Date d1, Date d2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid AND reference_uid IN (SELECT uid FROM vente WHERE dateVente BETWEEN ? AND ?)");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1, TemporalType.DATE);
            query.setParameter(3, d2, TemporalType.DATE);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String idpro, Date d1, Date d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(quantite*m.quantcontenu) as q FROM ligne_vente l, mesure m WHERE l.product_id = ? AND l.mesure_id=m.uid AND reference_uid IN ");
            sb.append("(SELECT v.uid FROM vente v WHERE dateVente BETWEEN ? AND ? AND v.region = ? )");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1, TemporalType.DATE);
            query.setParameter(3, d2, TemporalType.DATE);
            query.setParameter(4, region);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public LigneVente saveLigneVente(LigneVente i, Vente vente4save) {
        EntityTransaction tr = em.getTransaction();
        if (!tr.isActive()) {
            tr.begin();
        }
        i.setReference(vente4save);
        em.persist(i);
        tr.commit();
        return i;
    }

}
