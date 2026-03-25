/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import jakarta.persistence.EntityManager;
import data.Permission;
import IServices.PermissionStorage;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.List;
import java.util.prefs.Preferences;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class PermissionService implements PermissionStorage {

    Preferences pref;
    String contrat;

    @Override
    public boolean isExists(String name) {
        String jpql = "SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Permission p WHERE p.permissionname = :name AND p.tablename = :contract";
        if (ManagedSessionFactory.isEmbedded()) {
//            return true;
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                    .setParameter("name", name)
                    .setParameter("contract",contrat)
                    .getSingleResult());
        }
        return ManagedSessionFactory.getEntityManager()
                .createQuery(jpql, Boolean.class)
                .setParameter("name", name)
                .setParameter("contract",contrat)
                .getSingleResult();
    }

    public PermissionService() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        contrat=pref.get("ucontract", "none");
    }

    @Override
    public Permission savePermission(final Permission pro) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(pro);
                return pro;
            }).thenAccept(e -> {
                System.out.println("Permission " + e.getPermissionname() + " enregistree");
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
    public Permission updatePermission(Permission cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Permission " + e.getPermissionname() + " enregistree");
            });
            return cat;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Permission merged = em.merge(cat);
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
    public void deletePermission(Permission cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Permission " + e.getPermissionname() + " enregistree");
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
    public Permission findPermission(String d) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Permission.class, d));
        }
        return ManagedSessionFactory.getEntityManager().find(Permission.class, d);
    }

    @Override
    public List<Permission> findPermissions() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM permission WHERE tablename = ? ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), Permission.class)
                    .setParameter(1, contrat).getResultList());
        }
        try {
            EntityManager em = ManagedSessionFactory.getEntityManager();
            Query query = em.createNativeQuery(sb.toString(), Permission.class);
            return query.setParameter(1, contrat).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Permission findPermissionByName(String name) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM permission WHERE permissionname = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> (Permission) em.createNativeQuery(sb.toString(), Permission.class)
                        .setParameter(1, name).getSingleResult());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Permission.class)
                    .setParameter(1, name);
            return (Permission) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deletePermissions(List<Permission> choosenPermission) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                choosenPermission.forEach((var permission) -> {
                    em.remove(em.merge(permission));
                });
                return choosenPermission;
            }).thenAccept(e -> {
                System.out.println(e.size() + " permissions suprimmees avec succes");
            });
            return;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx;
        tx = em.getTransaction();
        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            choosenPermission.forEach((var permission) -> {
                em.remove(em.merge(permission));
            });
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e; // ou logger + rethrow
        }
    }

    @Override
    public List<Permission> savePermissions(List<Permission> perms) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (Permission p : perms) {
                    Permission existing = em.find(Permission.class, p.getUid());
                    if (existing == null) {
                        em.persist(p);
                    } else {
                        em.merge(p); // ou juste ignorer
                    }
                }
                return perms;
            }).thenAccept(e -> {
                System.out.println(e.size() + " permissions enregistrees");
            });
            return perms;
        }
        EntityManager em = ManagedSessionFactory.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (Permission p : perms) {
//                ManagedSessionFactory.getEntityManager().merge(p);
                Permission existing = em.find(Permission.class, p.getUid());
                if (existing == null) {
                    em.persist(p);
                } else {
                    em.merge(p); // ou juste ignorer
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e; // ou logger + rethrow
        }
        return perms;
    }

}
