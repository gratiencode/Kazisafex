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
        return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult());
    }

    public ProduitService() {
        //initializing...
    }

    @Override
    public Produit createProduit(Produit pro) {
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(pro);
            return pro;
        });
        return pro;
    }

    @Override
    public Produit updateProduit(Produit cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.merge(cat);
            return cat;
        });
        return cat;
    }

    @Override
    public void deleteProduit(Produit cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.remove(em.merge(cat));
            return cat;
        });
    }

    @Override
    public List<Produit> mergeSet(Set<Produit> bulk) {
        return ManagedSessionFactory.executeWrite(em -> {
            List<Produit> result = new ArrayList<>();
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
            return result;
        });
    }

    @Override
    public Produit findProduit(String catId) {
        return ManagedSessionFactory.executeRead(em -> em.find(Produit.class, catId));
    }

    @Override
    public List<Produit> findProduits() {
        try {
            return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Produit.findAll").getResultList());
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduitByCategory(String catId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.categoryid_uid =  ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Produit.class);
                query.setParameter(1, catId);
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
            sb.append("SELECT COUNT(*) FROM produit");
            return ManagedSessionFactory.executeRead(em -> {
                return (Long) em.createNativeQuery(sb.toString(), Long.class).getSingleResult();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduits(int start, int max) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Produit.findAll");
                query.setFirstResult(start);
                query.setMaxResults(max);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Produit findByBarcode(String codebar) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Produit.findByCodeBar");
                query.setParameter("codeBar", codebar);
                return (Produit) query.getSingleResult();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findAllByCodebar(String codebarr) {
        try {
            return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Produit.findByCodeBar").setParameter("codeBar", codebarr).getResultList());
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
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Produit.class);
                query.setParameter(1, nomProduit);
                query.setParameter(2, marque);
                query.setParameter(3, modele);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Produit> findProduitByName(String regex) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ',p.couleur) LIKE ?");
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Produit.class);
                query.setParameter(1, "%" + regex + "%");
                return query.getResultList();
            });
        } catch (NoResultException e) {
            System.err.println("Result is empty mon vieu");
        }
        return new ArrayList<>();
    }

    @Override
    public List<Produit> findUnSyncedProduct(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit p WHERE p.updated_at >= ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Produit.class);
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
        sb.append("SELECT * FROM Produit p WHERE p.uid = ? AND p.updated_at = ?");
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, uid);
            query.setParameter(2, atime);
            List<Produit> result = query.getResultList();
            return !result.isEmpty();
        });
    }

}
