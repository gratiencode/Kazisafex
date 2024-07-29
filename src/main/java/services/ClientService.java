/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ClientStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import data.Client;
import java.sql.SQLIntegrityConstraintViolationException;
import tools.DataId;

/**
 *
 * @author eroot
 */
public class ClientService implements ClientStorage {

    EntityManager em;

    public ClientService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Client createClient(Client cat) {
        EntityTransaction etr = em.getTransaction();
        try {
            if (!etr.isActive()) {
                etr.begin();
            }
            em.persist(cat);
            etr.commit();
            return cat;
        } catch (Exception e) {
            if(isUniqueConstraintViolation(e)){
                etr.rollback();
                return cat;
            }            
        }
        return null;
    }
    
    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    @Override
    public Client updateClient(Client cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteClient(Client cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Client findClient(String catId) {
        return em.find(Client.class, catId);
    }

    @Override
    public List<Client> findClients() {
        try {
            Query query = em.createNamedQuery("Client.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM client");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Client> findClientByPhone(String phone) {
        try {
            Query query = em.createNamedQuery("Client.findByPhone");
            query.setParameter("phone", phone);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Client getAnonymousClient() {
        Query query = em.createNativeQuery("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ", Client.class);
        query.setParameter(1, "Unknown")
                .setParameter(2, "Anonyme")
                .setParameter(3, "09000");
        List<Client> anonymous = query.getResultList();
        if (anonymous.isEmpty()) {
            Client c = new Client(DataId.generate());
            c.setAdresse("Unknown");
            c.setEmail("Unknown");
            c.setNomClient("Anonyme");
            c.setPhone("09000");
            c.setTypeClient("Consommateur");
            c.setParentId(c);
            Client created = createClient(c);
            return created;
        } else {
            return anonymous.get(0);
        }
    }
    
    @Override
    public Client getImporterClient() { 
        Query query = em.createNativeQuery("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ", Client.class);
        query.setParameter(1, "Unknown")
                .setParameter(2, "Importer")
                .setParameter(3, "09001");
        List<Client> anonymous = query.getResultList();
        if (anonymous.isEmpty()) {
            Client c = new Client(DataId.generate());
            c.setAdresse("Unknown");
            c.setEmail("Unknown");
            c.setNomClient("Importer");
            c.setPhone("09001");
            c.setTypeClient("Consommateur");
            c.setParentId(getAnonymousClient());
            Client created = createClient(c);
            return created;
        } else {
            return anonymous.get(0);
        }
    }

    @Override
    public List<Client> findClients(int start, int max) {
        try {
            Query query = em.createNamedQuery("Client.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Client> mergeSet(Set<Client> bulk) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Client lj : bulk) {
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
        Enumeration<Client> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

}
