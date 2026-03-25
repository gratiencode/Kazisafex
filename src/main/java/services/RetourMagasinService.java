/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RetourMagasinStorage;
import data.Category;
import data.Mesure;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.RetourMagasin;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RetourMagasinService implements RetourMagasinStorage {

    @Override
    public boolean isExists(String uid) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> findRetourMagasin(uid) != null);
        }
        return findRetourMagasin(uid) != null;
    }

    public RetourMagasinService() {
    }

    @Override
    public RetourMagasin createRetourMagasin(RetourMagasin cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public RetourMagasin updateRetourMagasin(RetourMagasin cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteRetourMagasin(RetourMagasin cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public RetourMagasin findRetourMagasin(String catId) {
        return ManagedSessionFactory.getEntityManager().find(RetourMagasin.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM retour_magasin");
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<RetourMagasin> findRetourMagasins() {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("RetourMagasin.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<RetourMagasin> findRetourMagasins(int start, int max) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("RetourMagasin.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public long getCountForVente(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM retour_magasin WHERE reference_vente  = ? ");
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).setParameter(1, uid)
                    .getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<RetourMagasin> findByLigneVente(Long uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM retour_magasin WHERE ligne_vente_id  = ? ");
            return ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), RetourMagasin.class)
                    .setParameter(1, uid).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<RetourMagasin> mergeSet(Set<RetourMagasin> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (RetourMagasin lj : bulk) {
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
        Enumeration<RetourMagasin> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<RetourMagasin> findUnSyncedRetourMagasins(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM retour_magasin p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), RetourMagasin.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
                    RetourMagasin.class);
            query.setParameter(1, offline);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM retour_magasin p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), RetourMagasin.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<RetourMagasin> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), RetourMagasin.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<RetourMagasin> result = query.getResultList();
        return !result.isEmpty();
    }

}
