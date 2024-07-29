/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.CategoryStorage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Category;

/**
 *
 * @author eroot
 */
public class CategoryService implements CategoryStorage {

    EntityManager em;

    public CategoryService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Category createCategory(Category cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.persist(cat);
        tx.commit();
        return cat;
    }

    @Override
    public Category updateCategory(Category cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteCategory(Category cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.remove(em.merge(cat));
        tx.commit();
    }

    @Override
    public Category findCategory(String catId) {
        return em.find(Category.class, catId);
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM category");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Category> findCategories() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT c.uid, c.descritption FROM category c ");
            Query query = em.createNativeQuery(sb.toString(), Category.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Category findCategoryByDescription(String desc) {
        try {
            Query query = em.createNamedQuery("Category.findByDescritption");
            query.setParameter("descritption", desc);
            return (Category) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Category> findCategories(int start, int max) {
        try {
            Query query = em.createNamedQuery("Category.findAll");
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
            Query query = em.createNamedQuery("Category.findByDescritption");
            query.setParameter("descritption", divers);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Category> setCategories(List<Category> c) {
        EntityTransaction etrx = em.getTransaction();
        if (!etrx.isActive()) {
            etrx.begin();
        }
        for (int i = 0; i < c.size(); i++) {
            Category cat1 = c.get(i);
            em.persist(cat1);
            if (i % 16 == 0) {
                etrx.commit();
                em.clear();
                etrx.begin();
            }
        }
        etrx.commit();
        return c;
    }

    @Override
    public synchronized List<Category> updateCategorySet(Set<Category> cs) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        };

        int i = 0;
        for (Category c : cs) {
            i++;
            em.merge(c);
            if (i % 16 == 0) {
                etr.commit();
                em.clear();
                EntityTransaction etr2 = em.getTransaction();
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

}
