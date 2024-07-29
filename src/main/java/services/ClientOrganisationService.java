/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ClientOrganisationStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.ClientOrganisation;

/**
 *
 * @author eroot
 */
public class ClientOrganisationService implements ClientOrganisationStorage {

    EntityManager em;

    public ClientOrganisationService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public ClientOrganisation createClientOrganisation(ClientOrganisation cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public ClientOrganisation updateClientOrganisation(ClientOrganisation cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteClientOrganisation(ClientOrganisation cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public ClientOrganisation findClientOrganisation(String catId) {
        return em.find(ClientOrganisation.class, catId);
    }

    @Override
    public List<ClientOrganisation> findClientOrganisations() {
        try {
            Query query = em.createNamedQuery("ClientOrganisation.findAll");
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
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<ClientOrganisation> findClientOrganisationByName(String objId) {
        try {
            Query query = em.createNamedQuery("ClientOrganisation.findByNomOrganisation");
            query.setParameter("nomOrganisation", objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientOrganisation> findClientOrganisations(int start, int max) {
        try {
            Query query = em.createNamedQuery("ClientOrganisation.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClientOrganisation> mergeSet(Set<ClientOrganisation> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (ClientOrganisation lj : bulk) {
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
        Enumeration<ClientOrganisation> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
