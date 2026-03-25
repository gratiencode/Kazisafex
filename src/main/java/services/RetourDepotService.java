/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RetourDepotStorage;
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
import data.RetourDepot;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RetourDepotService implements RetourDepotStorage {

    @Override
    public boolean isExists(String uid) {
        return findRetourDepot(uid) != null;
    }

    public RetourDepotService() {
        //initializing...
    }

    @Override
    public RetourDepot createRetourDepot(RetourDepot cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public RetourDepot updateRetourDepot(RetourDepot cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteRetourDepot(RetourDepot cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public RetourDepot findRetourDepot(String catId) {
        return ManagedSessionFactory.getEntityManager().find(RetourDepot.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM retour_depot");
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<RetourDepot> findRetourDepots() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("RetourDepot.findAll").getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("RetourDepot.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<RetourDepot> findRetourDepots(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("RetourDepot.findAll")
                        .setFirstResult(start)
                        .setMaxResults(max).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("RetourDepot.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<RetourDepot> mergeSet(Set<RetourDepot> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (RetourDepot lj : bulk) {
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
        Enumeration<RetourDepot> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<RetourDepot> findUnSyncedRetourDepots(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM retour_depot p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), RetourDepot.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), RetourDepot.class);
            query.setParameter(1, offline);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM retour_depot p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), RetourDepot.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<RetourDepot> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), RetourDepot.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<RetourDepot> result = query.getResultList();
        return !result.isEmpty();
    }

}
