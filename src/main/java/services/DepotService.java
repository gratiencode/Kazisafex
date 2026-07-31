/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.DepotStorage;
import data.Category;
import data.Depot;
import data.MatiereSku;
import jakarta.persistence.EntityManager;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class DepotService implements DepotStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Depot c WHERE c.uid = :id";
        return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult());
    }

    public DepotService() {
        //initializing...
    }

    @Override
    public Depot saveDepot(Depot d) {
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(d);
            return d;
        });
        return d;
    }

    @Override
    public Depot updateDepot(Depot d) {
        ManagedSessionFactory.executeWrite(em -> {
            em.merge(d);
            return d;
        });
        return d;
    }

    @Override
    public void deleteDepot(Depot d) {
        ManagedSessionFactory.executeWrite(em -> {
            em.remove(em.merge(d));
            return d;
        });
    }

    @Override
    public Depot findDepot(String d) {
        return ManagedSessionFactory.executeRead(em -> {
            return em.find(Depot.class, d);
        });
    }

    @Override
    public List<Depot> findDepots() {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Depot.findAll");
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM depot p WHERE p.uid = ? AND p.updated_at = ?");
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNativeQuery(sb.toString(), Depot.class);
            query.setParameter(1, uid);
            query.setParameter(2, atime);
            List<Depot> result = query.getResultList();
            return !result.isEmpty();
        });
    }

}
