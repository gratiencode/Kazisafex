/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.PrixDeVenteStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Mesure;
import data.PrixDeVente;
import data.Recquisition;

/**
 *
 * @author eroot
 */
public class PrixDeVenteService implements PrixDeVenteStorage {

    EntityManager em;

    public PrixDeVenteService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public PrixDeVente createPrixDeVente(PrixDeVente cat) {
        EntityTransaction tx = em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            em.persist(cat);
            tx.commit();
            return cat;
    }

    @Override
    public PrixDeVente updatePrixDeVente(PrixDeVente cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deletePrixDeVente(PrixDeVente cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public PrixDeVente findPrixDeVente(String catId) {
        return em.find(PrixDeVente.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM prix_de_vente");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<PrixDeVente> findPrixDeVentes() {
        try {
            Query query = em.createNamedQuery("PrixDeVente.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findPrixDeVentes(int start, int max) {
        try {
            Query query = em.createNamedQuery("PrixDeVente.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findPricesForRecq(String ruid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ?");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, ruid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findSpecificByQuant(Recquisition choosenRecquisition, Mesure choosenmez, double quant) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? AND q_min <= ?  AND q_max >= ? ");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, choosenRecquisition.getUid());
            query.setParameter(2, choosenmez.getUid());
            query.setParameter(3, quant);
            query.setParameter(4, quant);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findDescSortedByRecqWithMesureByPrice(String req, String muid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? ORDER BY prix_unitaire DESC");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, req);
            query.setParameter(2, muid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findDescOrderdByPriceForRecq(String req) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? ORDER BY prix_unitaire DESC");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, req);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void startTransaction() {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

    }

    @Override
    @Deprecated(forRemoval = true)
    public void commitTransaction() {

    }

    @Override
    public PrixDeVente addToTransaction(PrixDeVente lpv) {
        em.persist(lpv);
        return lpv;
    }

    @Override
    public List<PrixDeVente> mergeSet(Set<PrixDeVente> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (PrixDeVente lj : bulk) {
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
        Enumeration<PrixDeVente> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<PrixDeVente> findPrixDeVente(Double qmin, String uid, String uid0) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? AND q_min = ? ");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, uid0);
            query.setParameter(2, uid);
            query.setParameter(3, qmin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

}
