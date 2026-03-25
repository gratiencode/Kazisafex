/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ClientStorage;
import data.Category;
import java.util.ArrayList;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.DataId;

/**
 *
 * @author eroot
 */
public class ClientService implements ClientStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Client c WHERE c.uid = :id";
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

    public ClientService() {
        // initializing...
    }

    @Override
    public Client createClient(Client cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomClient() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        try {
            if (!etr.isActive()) {
                etr.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            etr.commit();
            return cat;
        } catch (Exception e) {
            if (isUniqueConstraintViolation(e)) {
                etr.rollback();
                return cat;
            }
        }
        return null;
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
            if (t instanceof SQLIntegrityConstraintViolationException
                    || msg.contains("constraint")
                    || msg.contains("unique")
                    || msg.contains("duplicate")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    @Override
    public Client updateClient(Client cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomClient() + " enregistree");
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
    public void deleteClient(Client cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomClient() + " enregistree");
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
    public Client findClient(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Client.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Client.class, catId);
    }

    @Override
    public List<Client> findClients() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Client.class);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
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
    public List<Client> findClientByPhone(String phone) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client WHERE phone = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Client.class);
                    query.setParameter(1, phone);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
            query.setParameter(1, phone);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Client getAnonymousClient() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
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
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
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
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
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
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
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
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Client.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Client.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Client> mergeSet(Set<Client> bulk) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (Client lj : bulk) {
                    em.merge(lj);
                }
                return bulk;
            }).thenAccept(e -> {
                System.out.println("Bulk Client merged");
            });
            return new ArrayList<>(bulk);
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Client lj : bulk) {
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
        Enumeration<Client> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Client> findUnSyncedClients(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Client.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Client> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Client> result = query.getResultList();
        return !result.isEmpty();
    }

    @Override
    public double getTotalDebt() {
        String jpql = "SELECT SUM(v.montantDette) FROM Vente v";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double res = em.createQuery(jpql, Double.class).getSingleResult();
                return res == null ? 0d : res;
            });
        }
        Double res = ManagedSessionFactory.getEntityManager().createQuery(jpql, Double.class).getSingleResult();
        return res == null ? 0d : res;
    }
}
