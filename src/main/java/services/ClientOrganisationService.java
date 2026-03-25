/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ClientOrganisationStorage;
import data.Category;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.ClientOrganisation;
import data.ClientOrganisation;
import data.Mesure;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientOrganisationService implements ClientOrganisationStorage {

    @Override
    public boolean isExists(String uid) {
         String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM ClientOrganisation c WHERE c.uid = :id";
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

    public ClientOrganisationService() {
        //initializing...
    }

    @Override
    public ClientOrganisation createClientOrganisation(ClientOrganisation cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public ClientOrganisation updateClientOrganisation(ClientOrganisation cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteClientOrganisation(ClientOrganisation cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public ClientOrganisation findClientOrganisation(String catId) {
        return ManagedSessionFactory.getEntityManager().find(ClientOrganisation.class, catId);
    }

    @Override
    public List<ClientOrganisation> findClientOrganisations() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("ClientOrganisation.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("ClientOrganisation.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM client_organisation");
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<ClientOrganisation> findClientOrganisationByName(String objId) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("ClientOrganisation.findByNomOrganisation");
            query.setParameter("nomOrganisation", objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientOrganisation> findClientOrganisations(int start, int max) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("ClientOrganisation.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientOrganisation> mergeSet(Set<ClientOrganisation> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (ClientOrganisation lj : bulk) {
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
        Enumeration<ClientOrganisation> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<ClientOrganisation> findUnSyncedClientOrganisations(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client_organisation p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), ClientOrganisation.class);
            query.setParameter(1, offline);
            return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), ClientOrganisation.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client_organisation p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query =em.createNativeQuery(sb.toString(), ClientOrganisation.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<ClientOrganisation> result = query.getResultList();
        return !result.isEmpty();
                });
            }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), ClientOrganisation.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<ClientOrganisation> result = query.getResultList();
        return !result.isEmpty();
    }
}
