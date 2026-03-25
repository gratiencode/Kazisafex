/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.InventaireStorage;
import data.Inventaire;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class InventaireService implements InventaireStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Inventaire c WHERE c.uid = :id";
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

    public InventaireService() {
        //initializing...
    }

    @Override
    public Inventaire createInventaire(Inventaire cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element INV " + e.getCodeInventaire() + " enregistree");
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
    public Inventaire updateInventaire(Inventaire cat) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).thenAccept(e -> {
                    System.out.println("Element INv " + e.getCodeInventaire() + " enregistree");
                });
                return cat;
            }
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().merge(cat);
            tx.commit();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            System.err.println("Erreur Message : " + e.getMessage());
        }
        return cat;
    }

    @Override
    public void deleteInventaire(Inventaire obj) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(obj));
                return obj;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getCodeInventaire() + " supprimee");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(obj));
        etr.commit();
    }

    @Override
    public Inventaire findInventaire(String objId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Inventaire.class, objId));
        }
        return ManagedSessionFactory.getEntityManager().find(Inventaire.class, objId);
    }

    @Override
    public List<Inventaire> findInventaires() {

        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Inventaire.findAll");
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Inventaire.findAll");
        return query.getResultList();

    }

    @Override
    public List<Inventaire> findInventaires(String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Inventaire.findByRegion");
                    query.setParameter("region", region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Inventaire.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM inventaire");
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
    public List<Inventaire> findInventaires(Date dateDebut, Date dateFin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM inventaire m WHERE m.date_debut = ? AND m.date_fin = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                    query.setParameter(1, dateDebut);
                    query.setParameter(2, dateFin);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
            query.setParameter(1, dateDebut);
            query.setParameter(2, dateFin);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Inventaire> findInventaires(Date dateDebut, Date dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM inventaire m WHERE m.date_debut = ? AND m.date_fin = ? AND m.region = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                    query.setParameter(1, dateDebut);
                    query.setParameter(2, dateFin);
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
            query.setParameter(1, dateDebut);
            query.setParameter(2, dateFin);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Inventaire> findUnSyncedInventaires(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM inventaire p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
            query.setParameter(1, offline);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM inventaire p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Inventaire> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Inventaire> result = query.getResultList();
        return !result.isEmpty();
    }

    @Override
    public Inventaire findInventaireByCode(String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM inventaire p WHERE p.code_inventaire = ? ORDER BY p.updated_at DESC");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                query.setParameter(1, code);
                List<Inventaire> result = query.getResultList();
                return !result.isEmpty() ? result.get(0) : null;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
        query.setParameter(1, code);
        List<Inventaire> result = query.getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public Inventaire findLastInventaire() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM inventaire p ORDER BY p.updated_at DESC");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                query.setMaxResults(1);
                List<Inventaire> result = query.getResultList();
                return !result.isEmpty() ? result.get(0) : null;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
        query.setMaxResults(1);
        List<Inventaire> result = query.getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<Inventaire> findNonClosed() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM inventaire m WHERE m.etat = ? OR m.etat = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Inventaire.class);
                query.setParameter(1, "En cours...");
                query.setParameter(2, "Non commence");
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Inventaire.class);
        query.setParameter(1, "En cours...");
        query.setParameter(2, "Non commence");
        return query.getResultList();
    }

}
