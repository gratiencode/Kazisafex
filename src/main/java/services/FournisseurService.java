/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.FournisseurStorage;
import data.Category;
import data.Entreprise;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Fournisseur;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class FournisseurService implements FournisseurStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Fournisseur c WHERE c.uid = :id";
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

    public FournisseurService() {
        // initializing...
    }

    @Override
    public Fournisseur createFournisseur(Fournisseur cat) {

        List<Fournisseur> exist = findByPhone(cat.getPhone());

        if (exist.isEmpty()) {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.persist(cat);
                    return cat;
                }).thenAccept(e -> {
                    System.out.println("Element " + e.getNomFourn() + " enregistree");
                });
                return cat;
            }
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            try {
                if (!tx.isActive()) {
                    tx.begin();
                }
                ManagedSessionFactory.getEntityManager().persist(cat);
                tx.commit();
            } catch (Exception e) {
                if (isUniqueConstraintViolation(e)) {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                    return cat;
                }
            }
        }
        return cat;
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
            if (t instanceof java.sql.SQLIntegrityConstraintViolationException
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
    public Fournisseur updateFournisseur(Fournisseur cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomFourn() + " enregistree");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteFournisseur(Fournisseur cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomFourn() + " supprimee");
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
    public Fournisseur findFournisseur(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Fournisseur.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Fournisseur.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM fournisseur");
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
    public List<Fournisseur> findFournisseurs() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(
                        em -> em.createNativeQuery("Select * from fournisseur", Fournisseur.class).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery("Select * from fournisseur",
                    Fournisseur.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Fournisseur> findFournisseurs(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Fournisseur.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Fournisseur.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Fournisseur> findByPhone(String text) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Fournisseur.findByPhone")
                        .setParameter("phone", text).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Fournisseur.findByPhone");
            query.setParameter("phone", text);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Fournisseur> mergeSet(Set<Fournisseur> bulk) {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }

        int i = 0;
        for (Fournisseur lj : bulk) {
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
        Enumeration<Fournisseur> enums = Collections.enumeration(bulk);
        return Collections.list(enums);
    }

    @Override
    public List<Fournisseur> findUnSyncedFournisseurs(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM fournisseur p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Fournisseur.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Fournisseur.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Fournisseur findOrCreate(Entreprise entreprise) {
        Fournisseur ff = findFournisseur(entreprise.getUid());
        if (ff == null) {
            Fournisseur f = new Fournisseur(entreprise.getUid());
            f.setAdresse(entreprise.getAdresse());
            f.setIdentification(entreprise.getIdentification());
            f.setNomFourn(entreprise.getNomEntreprise());
            f.setPhone(entreprise.getPhones());
            ff = createFournisseur(f);
        }
        return ff;
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM Fournisseur p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Fournisseur.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Fournisseur> result = query.getResultList();
                return !result.isEmpty();

            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Fournisseur.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Fournisseur> result = query.getResultList();
        return !result.isEmpty();
    }

    @Override
    public double getTotalDebt() {
        String jpql = "SELECT SUM(l.remained) FROM Livraison l";
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
