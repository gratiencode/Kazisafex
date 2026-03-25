/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.PrixDeVenteStorage;
import data.Category;
import data.Livraison;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class PrixDeVenteService implements PrixDeVenteStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM PrixDeVente c WHERE c.uid = :id";
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

    public PrixDeVenteService() {
        // initializing...
    }

    @Override
    public PrixDeVente createPrixDeVente(PrixDeVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element prix " + e.getPrixUnitaire() + " enregistree");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        tx.commit();
        return cat;
    }

    @Override
    public PrixDeVente updatePrixDeVente(PrixDeVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element prix " + e.getPrixUnitaire() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deletePrixDeVente(PrixDeVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getPrixUnitaire() + " enregistree");
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
    public PrixDeVente findPrixDeVente(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(PrixDeVente.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(PrixDeVente.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM prix_de_vente");
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
    public List<PrixDeVente> findPrixDeVentes() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("PrixDeVente.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("PrixDeVente.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findPrixDeVentes(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("PrixDeVente.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("PrixDeVente.findAll");
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
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, ruid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
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
            sb.append(
                    "SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? AND q_min <= ?  AND q_max >= ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, choosenRecquisition.getUid());
                    query.setParameter(2, choosenmez.getUid());
                    query.setParameter(3, quant);
                    query.setParameter(4, quant);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
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
            sb.append(
                    "SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? ORDER BY prix_unitaire DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, req);
                    query.setParameter(2, muid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
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
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, req);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, req);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void startTransaction() {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
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
        ManagedSessionFactory.getEntityManager().persist(lpv);
        return lpv;
    }

    @Override
    public List<PrixDeVente> mergeSet(Set<PrixDeVente> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (PrixDeVente lj : bulk) {
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
        Enumeration<PrixDeVente> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<PrixDeVente> findPrixDeVente(Double qmin, String uid, String uid0) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? AND ? BETWEEN q_min AND q_max");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, uid0);
                    query.setParameter(2, uid);
                    query.setParameter(3, qmin);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, uid0);
            query.setParameter(2, uid);
            query.setParameter(3, qmin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findPrixDeVentes(Double qmin, double quantContenuMesure, String requisid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT p.uid, p.recquisition_id, p.mesureid_uid, p.q_max, p.q_min, p.devise, p.prix_unitaire FROM prix_de_vente p, mesure m WHERE p.recquisition_id = ? AND p.mesureid_uid = m.uid AND q_min = ? AND m.quantcontenu = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, requisid);
                    query.setParameter(2, qmin);
                    query.setParameter(3, quantContenuMesure);
                    return query.getResultList();

                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, requisid);
            query.setParameter(2, qmin);
            query.setParameter(3, quantContenuMesure);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findPrixDeVentes(double qmin, double qmax, double quantContenuMesure, String requisid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT p.uid, p.recquisition_id, p.mesureid_uid, p.q_max, p.q_min, p.devise, p.prix_unitaire FROM prix_de_vente p, mesure m WHERE p.recquisition_id = ? AND p.mesureid_uid = m.uid AND q_min = ? AND q_max >= ? AND m.quantcontenu = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, requisid);
                    query.setParameter(2, qmin);
                    query.setParameter(3, qmax);
                    query.setParameter(4, quantContenuMesure);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, requisid);
            query.setParameter(2, qmin);
            query.setParameter(3, qmax);
            query.setParameter(4, quantContenuMesure);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findUnSyncedPrixDeVentes(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM prix_de_vente p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<PrixDeVente> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<PrixDeVente> result = query.getResultList();
        return !result.isEmpty();
    }

}
