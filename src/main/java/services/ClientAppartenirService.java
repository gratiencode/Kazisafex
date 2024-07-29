/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ClientAppartenirStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.ClientAppartenir;

/**
 *
 * @author eroot
 */
public class ClientAppartenirService implements ClientAppartenirStorage {

    EntityManager em;

    public ClientAppartenirService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public ClientAppartenir createClientAppartenir(ClientAppartenir cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public ClientAppartenir updateClientAppartenir(ClientAppartenir cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteClientAppartenir(ClientAppartenir cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public ClientAppartenir findClientAppartenir(String catId) {
        return em.find(ClientAppartenir.class, catId);
    }

    @Override
    public List<ClientAppartenir> findClientAppartenirs() {
        try {
            Query query = em.createNamedQuery("ClientAppartenir.findAll");
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
            Query query = em.createNativeQuery(sb.toString(), ClientAppartenir.class);
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
            Query query = em.createNativeQuery(sb.toString(), ClientAppartenir.class);
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
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<ClientAppartenir> findClientAppartenirs(int start, int max) {
        try {
            Query query = em.createNamedQuery("ClientAppartenir.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientAppartenir> mergeSet(Set<ClientAppartenir> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (ClientAppartenir lj : bulk) {
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
        Enumeration<ClientAppartenir> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
