/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.ProduitStorage;
import data.Category;
import data.Permission;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Produit;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author eroot
 */
public class ProduitService implements ProduitStorage {

//    
    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Produit p WHERE p.uid = :id";
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

    public ProduitService() {
        //initializing...
    }

    @Override
    public Produit createProduit(Produit pro) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(pro);
                return pro;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomProduit() + " enregistree");
            });
            return pro;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(pro);
            tx.commit();
            return pro;
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.clear(); // libère le contexte
        }
    }

    @Override
    public Produit updateProduit(Produit cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomProduit() + " MAJ");
            });
            return cat;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Produit merged = em.merge(cat);
            tx.commit();
            return merged;
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.clear();
        }
    }

    @Override
    public void deleteProduit(Produit cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomProduit() + " supprimee");
            });
            return;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction etr = em.getTransaction();
        try {
            if (!etr.isActive()) {
                etr.begin();
            }
            em.remove(em.merge(cat));
            etr.commit();
        } catch (Exception ex) {
            if (etr.isActive()) {
                etr.rollback();
            }
            throw ex;
        } finally {
            em.clear();
        }
    }

    @Override
    public List<Produit> mergeSet(Set<Produit> bulk) {
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<Produit> result = new ArrayList<>();
        try {
            tx.begin();
            int i = 0;
            for (Produit p : bulk) {
                Produit merged = em.merge(p);
                result.add(merged);
                i++;
                if (i % 16 == 0) {
                    em.flush();
                    em.clear();
                }
            }
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.clear();
        }
    }

    @Override
    public Produit findProduit(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Produit.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Produit.class, catId);
    }

    @Override
    public List<Produit> findProduits() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Produit.findAll").getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Produit.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduitByCategory(String catId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.categoryid_uid =  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    query.setParameter(1, catId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, catId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM produit");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return (Long) em.createNativeQuery(sb.toString(), Long.class).getSingleResult();
                });
            }
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduits(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    Query query = em.createNamedQuery("Produit.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Produit.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Produit findByBarcode(String codebar) {
        try {

            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Produit.findByCodeBar");
                    query.setParameter("codeBar", codebar);
                    return (Produit) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Produit.findByCodeBar");
            query.setParameter("codeBar", codebar);
            return (Produit) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findAllByCodebar(String codebarr) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Produit.findByCodeBar").setParameter("codeBar", codebarr).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Produit.findByCodeBar");
            query.setParameter("codeBar", codebarr);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

//    @Override
//    public List<Produit> mergeSet(Set<Produit> bulk) {
//        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
//        if (!etr.isActive()) {
//            etr.begin();
//        }
//
//        int i = 0;
//        for (Produit lj : bulk) {
//            i++;
//            ManagedSessionFactory.getEntityManager().merge(lj);
//            if (i % 16 == 0) {
//                etr.commit();
//                ManagedSessionFactory.getEntityManager().clear();
//                EntityTransaction etr2 = ManagedSessionFactory.getEntityManager().getTransaction();
//                if (!etr2.isActive()) {
//                    etr2.begin();
//                }
//
//            }
//        }
//        etr.commit();
//        Enumeration<Produit> enums = Collections.enumeration(bulk);
//        return Collections.list(enums);
//    }
    @Override
    public List<Produit> findByDescription(String nomProduit, String marque, String modele, String taille) {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.nomproduit =  ? "
                    + "AND p.marque = ? AND p.modele = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    query.setParameter(1, nomProduit);
                    query.setParameter(2, marque);
                    query.setParameter(3, modele);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, nomProduit);
            query.setParameter(2, marque);
            query.setParameter(3, modele);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduitByName(String regex) {
        List<Produit> rsult = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ',p.couleur) LIKE ?");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em
                        -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    query.setParameter(1, "%" + regex + "%");
                    rsult.addAll(query.getResultList());
                    return rsult;
                });
            }

        } catch (NoResultException e) {
            System.err.println("Result is empty mon vieu");
        }
        return rsult;
    }

    @Override
    public List<Produit> findUnSyncedProduct(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM Produit p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Produit.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Produit> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Produit> result = query.getResultList();
        return !result.isEmpty();
    }

}
