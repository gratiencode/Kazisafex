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
        return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult());
    }

    public ClientService() {
        // initializing...
    }

    @Override
    public Client createClient(Client cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(cat);
            return cat;
        });
        System.out.println("Element " + cat.getNomClient() + " enregistree");
        return cat;
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
        ManagedSessionFactory.executeWrite(em -> {
            em.merge(cat);
            return cat;
        });
        System.out.println("Element " + cat.getNomClient() + " enregistree");
        return cat;
    }

    @Override
    public void deleteClient(Client cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.remove(em.merge(cat));
            return cat;
        });
        System.out.println("Element " + cat.getNomClient() + " enregistree");
    }

    @Override
    public Client findClient(String catId) {
        return ManagedSessionFactory.executeRead(em -> em.find(Client.class, catId));
    }

    @Override
    public List<Client> findClients() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM client");
            return ManagedSessionFactory.executeRead(em -> {
                return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
            });
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Client> findClientByPhone(String phone) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client WHERE phone = ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
                query.setParameter(1, phone);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Client getAnonymousClient() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ");
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

    @Override
    public Client getImporterClient() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ");
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

    @Override
    public List<Client> findClients(int start, int max) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Client.findAll");
                query.setFirstResult(start);
                query.setMaxResults(max);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Client> mergeSet(Set<Client> bulk) {
        ManagedSessionFactory.executeWrite(em -> {
            for (Client lj : bulk) {
                em.merge(lj);
            }
            return bulk;
        });
        System.out.println("Bulk Client merged");
        return new ArrayList<>(bulk);
    }

    @Override
    public List<Client> findUnSyncedClients(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client p WHERE p.updated_at >= ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
                query.setParameter(1, offline);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client p WHERE p.uid = ? AND p.updated_at = ?");
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNativeQuery(sb.toString(), Client.class);
            query.setParameter(1, uid);
            query.setParameter(2, atime);
            List<Client> result = query.getResultList();
            return !result.isEmpty();
        });
    }

    @Override
    public double getTotalDebt() {
        String jpql = "SELECT SUM(v.montantDette) FROM Vente v";
        return ManagedSessionFactory.executeRead(em -> {
            Double res = em.createQuery(jpql, Double.class).getSingleResult();
            return res == null ? 0d : res;
        });
    }

    @Override
    public void mergeDuplicateClients(Client keeper, Client duplicate) {
        ManagedSessionFactory.executeWrite(em -> {
            doMergeProgrammatically(em, keeper, duplicate);
            return null;
        });
    }

    @Override
    public int mergeAllDuplicateClients() {
        return ManagedSessionFactory.executeWrite(em -> {
            return doMergeAllDuplicates(em);
        });
    }

    private void doMergeProgrammatically(EntityManager em, Client keeper, Client duplicate) {
        Client keeperManaged = em.merge(keeper);
        Client duplicateManaged = em.merge(duplicate);

        // 1. Vente
        List<data.Vente> ventes = em.createQuery("SELECT v FROM Vente v WHERE v.clientId = :duplicate", data.Vente.class)
            .setParameter("duplicate", duplicateManaged)
            .getResultList();
        for (data.Vente v : ventes) {
            v.setClientId(keeperManaged);
            em.merge(v);
            tools.Util.sync(v, "update", tools.Tables.VENTE);
        }

        // 2. LigneVente
        List<data.LigneVente> lignes = em.createQuery("SELECT l FROM LigneVente l WHERE l.clientId = :duplicateUid", data.LigneVente.class)
            .setParameter("duplicateUid", duplicateManaged.getUid())
            .getResultList();
        for (data.LigneVente l : lignes) {
            l.setClientId(keeperManaged.getUid());
            em.merge(l);
            tools.Util.sync(l, "update", tools.Tables.LIGNEVENTE);
        }

        // 3. Commande
        List<data.Commande> commandes = em.createQuery("SELECT c FROM Commande c WHERE c.clientId = :duplicate", data.Commande.class)
            .setParameter("duplicate", duplicateManaged)
            .getResultList();
        for (data.Commande c : commandes) {
            c.setClientId(keeperManaged);
            em.merge(c);
            // Note: Commande does not extend BaseModel, sync handled by SyncOutbox on next cycle
        }

        // 4. RetourMagasin
        List<data.RetourMagasin> retours = em.createQuery("SELECT r FROM RetourMagasin r WHERE r.clientId = :duplicate", data.RetourMagasin.class)
            .setParameter("duplicate", duplicateManaged)
            .getResultList();
        for (data.RetourMagasin r : retours) {
            r.setClientId(keeperManaged);
            em.merge(r);
            tools.Util.sync(r, "update", tools.Tables.RETOURMAGASIN);
        }

        // 5. Aretirer
        List<data.Aretirer> retraits = em.createQuery("SELECT a FROM Aretirer a WHERE a.clientId = :duplicate", data.Aretirer.class)
            .setParameter("duplicate", duplicateManaged)
            .getResultList();
        for (data.Aretirer a : retraits) {
            a.setClientId(keeperManaged);
            em.merge(a);
            tools.Util.sync(a, "update", tools.Tables.ARETIRER);
        }

        // 6. Client parent/child self-references
        List<data.Client> children = em.createQuery("SELECT c FROM Client c WHERE c.parentId = :duplicate", data.Client.class)
            .setParameter("duplicate", duplicateManaged)
            .getResultList();
        for (data.Client c : children) {
            if (c.getUid().equals(duplicateManaged.getUid())) {
                continue;
            }
            c.setParentId(keeperManaged);
            em.merge(c);
            tools.Util.sync(c, "update", tools.Tables.CLIENT);
        }

        // 7. ClientAppartenir associations
        List<data.ClientAppartenir> dupAssocs = em.createQuery(
            "SELECT ca FROM ClientAppartenir ca WHERE ca.clientId = :duplicate", data.ClientAppartenir.class)
            .setParameter("duplicate", duplicateManaged)
            .getResultList();

        for (data.ClientAppartenir ca : dupAssocs) {
            List<data.ClientAppartenir> keepAssocs = em.createQuery(
                "SELECT ca2 FROM ClientAppartenir ca2 WHERE ca2.clientId = :keeper AND ca2.clientOrganisationId = :org", data.ClientAppartenir.class)
                .setParameter("keeper", keeperManaged)
                .setParameter("org", ca.getClientOrganisationId())
                .getResultList();

            if (!keepAssocs.isEmpty()) {
                em.remove(em.merge(ca));
                tools.Util.sync(ca, "delete", tools.Tables.CLIENTAPPARTENIR);
            } else {
                ca.setClientId(keeperManaged);
                em.merge(ca);
                tools.Util.sync(ca, "update", tools.Tables.CLIENTAPPARTENIR);
            }
        }

        // 8. Handle keeper parent
        if (keeperManaged.getParentId() != null && keeperManaged.getParentId().getUid().equals(duplicateManaged.getUid())) {
            keeperManaged.setParentId(keeperManaged);
            em.merge(keeperManaged);
            tools.Util.sync(keeperManaged, "update", tools.Tables.CLIENT);
        }

        // 9. Remove duplicate client
        em.remove(duplicateManaged);
        tools.Util.sync(duplicateManaged, "delete", tools.Tables.CLIENT);
    }

    private int doMergeAllDuplicates(EntityManager em) {
        List<Client> clients = em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
        if (clients == null || clients.isEmpty()) {
            return 0;
        }

        java.util.Map<String, List<Client>> groups = new java.util.HashMap<>();
        for (Client c : clients) {
            if (c.getNomClient() == null || c.getPhone() == null) {
                continue;
            }
            String name = c.getNomClient().trim().toLowerCase();
            String phone = c.getPhone().trim().replaceAll("\\s+", "");
            if (name.isEmpty() || phone.isEmpty()) {
                continue;
            }
            if (phone.equals("09000") || phone.equals("09001") || name.equals("anonyme") || name.equals("importer")) {
                continue;
            }
            String key = name + "|" + phone;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        int count = 0;
        for (java.util.Map.Entry<String, List<Client>> entry : groups.entrySet()) {
            List<Client> group = entry.getValue();
            if (group.size() < 2) {
                continue;
            }

            Client keeper = selectKeeper(em, group);
            for (Client dup : group) {
                if (dup.getUid().equals(keeper.getUid())) {
                    continue;
                }
                doMergeProgrammatically(em, keeper, dup);
                count++;
            }
        }
        return count;
    }

    private Client selectKeeper(EntityManager em, List<Client> group) {
        Client keeper = group.get(0);
        int maxSales = -1;
        for (Client c : group) {
            Long salesCount = em.createQuery("SELECT COUNT(v) FROM Vente v WHERE v.clientId = :c", Long.class)
                .setParameter("c", c)
                .getSingleResult();
            int countVal = salesCount == null ? 0 : salesCount.intValue();
            if (countVal > maxSales) {
                maxSales = countVal;
                keeper = c;
            } else if (countVal == maxSales) {
                int keeperScore = (keeper.getEmail() != null && !keeper.getEmail().trim().isEmpty() ? 1 : 0)
                                + (keeper.getAdresse() != null && !keeper.getAdresse().trim().isEmpty() ? 1 : 0);
                int score = (c.getEmail() != null && !c.getEmail().trim().isEmpty() ? 1 : 0)
                          + (c.getAdresse() != null && !c.getAdresse().trim().isEmpty() ? 1 : 0);
                if (score > keeperScore) {
                    keeper = c;
                }
            }
        }
        return keeper;
    }
}
