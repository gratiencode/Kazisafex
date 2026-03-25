/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ClientAppartenirStorage;
import data.Aretirer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.ClientAppartenir;
import data.ClientAppartenir;
import data.Mesure;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientAppartenirService implements ClientAppartenirStorage {            

    
    
    @Override
    public boolean isExists(String uid) {
         String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM ClientAppartenir c WHERE c.uid = :id";
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

    public ClientAppartenirService() {
        //initializing...
    }

    @Override
    public ClientAppartenir createClientAppartenir(ClientAppartenir cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public ClientAppartenir updateClientAppartenir(ClientAppartenir cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteClientAppartenir(ClientAppartenir cat) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public ClientAppartenir findClientAppartenir(String catId) {
        return ManagedSessionFactory.getEntityManager().find(ClientAppartenir.class, catId);
    }

    @Override
    public List<ClientAppartenir> findClientAppartenirs() {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("ClientAppartenir.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientAppartenir> findClientAppartenirByClient(String clientId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ClientAppartenir ca WHERE ca.client_id =  ? ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), ClientAppartenir.class);
            query.setParameter(1, clientId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientAppartenir> findClientAppartenirByOrganisation(String orgId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client_appartenir ca WHERE ca.client_organisation_id =  ? ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), ClientAppartenir.class);
            query.setParameter(1, orgId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM client_appartenir");
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<ClientAppartenir> findClientAppartenirs(int start, int max) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("ClientAppartenir.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientAppartenir> mergeSet(Set<ClientAppartenir> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (ClientAppartenir lj : bulk) {
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
        Enumeration<ClientAppartenir> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<ClientAppartenir> findUnSyncedClientAppartenirs(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client_appartenir p WHERE p.updated_at >= ?");
            
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                      Query query = em.createNativeQuery(sb.toString(), ClientAppartenir.class);
            query.setParameter(1, offline);
            return query.getResultList();
                });
            }
            
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), ClientAppartenir.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
      @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client_appartenir p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), ClientAppartenir.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<ClientAppartenir> result = query.getResultList();
        return !result.isEmpty();
                });
            }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), ClientAppartenir.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<ClientAppartenir> result = query.getResultList();
        return !result.isEmpty();
    }

}
