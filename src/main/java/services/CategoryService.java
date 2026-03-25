/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.CategoryStorage;
import data.Aretirer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Category;
import data.Category;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class CategoryService implements CategoryStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Category c WHERE c.uid = :id";
        if (ManagedSessionFactory.isEmbedded()) {
//            return true;
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                    .setParameter("id", uid)
                    .getSingleResult());
        }
        return ManagedSessionFactory.getEntityManager()
                .createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult();
    }

    public CategoryService() {
        //initializing...
    }

    @Override
    public Category createCategory(Category cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getDescritption() + " enregistree");
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
    public Category updateCategory(Category cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getDescritption() + " enregistree");
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
    public void deleteCategory(Category cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getDescritption() + " supprimee");
            });
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        tx.commit();
    }

    @Override
    public Category findCategory(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Category.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Category.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM category");
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
    public List<Category> findCategories() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM category c ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), Category.class).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Category.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Category findCategoryByDescription(String desc) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Category.findByDescritption");
                    query.setParameter("descritption", desc);
                    return (Category) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Category.findByDescritption");
            query.setParameter("descritption", desc);
            return (Category) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Category> findCategories(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Category.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Category.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Category> findCategories(String divers) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Category.findByDescritption").setParameter("descritption", divers).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Category.findByDescritption");
            query.setParameter("descritption", divers);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Category> setCategories(List<Category> c) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (Category cat1 : c) {
                    em.persist(cat1);
                }
                return c;
            }).thenAccept(e -> {
                System.out.println("Bulk Category persisted");
            });
            return c;
        }
        EntityTransaction etrx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etrx.isActive()) {
            etrx.begin();
        }
        for (int i = 0; i < c.size(); i++) {
            Category cat1 = c.get(i);
            ManagedSessionFactory.getEntityManager().persist(cat1);
            if (i % 16 == 0) {
                etrx.commit();
                ManagedSessionFactory.getEntityManager().clear();
                etrx.begin();
            }
        }
        etrx.commit();
        return c;
    }

    @Override
    public synchronized List<Category> updateCategorySet(Set<Category> cs) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (Category c : cs) {
                    em.merge(c);
                }
                return cs;
            }).thenAccept(e -> {
                System.out.println("Bulk Category merged");
            });
            return new ArrayList<>(cs);
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        };

        int i = 0;
        for (Category c : cs) {
            i++;
            ManagedSessionFactory.getEntityManager().merge(c);
            if (i % 16 == 0) {
                etr.commit();
                ManagedSessionFactory.getEntityManager().clear();
                EntityTransaction etr2 = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!etr2.isActive()) {
                    etr2.begin();
                }

            }
            System.err.println("save catset " + c.getDescritption());
        }
        etr.commit();
        Enumeration<Category> enums = Collections.enumeration(cs);
        return Collections.list(enums);
    }

    @Override
    public List<Category> findUnSyncedCategories(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM category p WHERE p.updated_at > ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Category.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Category.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM category p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Category.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Category> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Category.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Category> result = query.getResultList();
        return !result.isEmpty();
    }

}
