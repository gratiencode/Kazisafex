/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.OperationStorage;
import data.Category;
import data.Depense;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Operation;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class OperationService implements OperationStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Operation c WHERE c.uid = :id";
        return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Boolean.class)
                .setParameter("id", uid)
                .getSingleResult());
    }

    public OperationService() {
        // initializing...
    }

    @Override
    public Operation createOperation(Operation cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(cat);
            return cat;
        });
        System.out.println("Element ops " + cat.getLibelle() + " enregistree");
        AggregateTriggerService.getInstance().notifyOperation(cat);
        return cat;
    }

    @Override
    public Operation updateOperation(Operation cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.merge(cat);
            return cat;
        });
        System.out.println("Element ops " + cat.getLibelle() + " supprrimee");
        AggregateTriggerService.getInstance().notifyOperation(cat);
        return cat;
    }

    @Override
    public void deleteOperation(Operation cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.remove(em.merge(cat));
            return cat;
        });
        System.out.println("Element ops " + cat.getLibelle() + " enregistree");
        AggregateTriggerService.getInstance().notifyOperation(cat);
    }

    @Override
    public Operation findOperation(String catId) {
        return ManagedSessionFactory.executeRead(em -> em.find(Operation.class, catId));
    }

    @Override
    public List<Operation> findOperations() {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Operation.findAll");
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOperationByCompteTresor(String objId) {
        // To change body of generated methods, choose Tools | Templates.
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.tresor_id =  ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, objId);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOpsByDepense(String depId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.depense_id =  ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, depId);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOpsByDepense(String depId, String tresorId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.depense_id =  ? AND tresor_id = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, depId);
                query.setParameter(2, tresorId);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOperations(int start, int max) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Operation.findAll");
                query.setFirstResult(start);
                query.setMaxResults(max);
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
            sb.append("SELECT COUNT(*) FROM operation");
            return ManagedSessionFactory.executeRead(em -> {
                return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
            });
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Operation> findOperations(String region) {
        try {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("Operation.findByRegion");
                query.setParameter("region", region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findByDateInterval(LocalDate date, LocalDate addDays) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.date BETWEEN ? AND ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, date.atStartOfDay());
                query.setParameter(2, addDays.atStartOfDay());
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findByDateInterval(LocalDate d1, LocalDate kesho, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.date BETWEEN ? AND ? AND p.region = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, d1.atStartOfDay());
                query.setParameter(2, kesho.atStartOfDay());
                query.setParameter(3, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> mergeSet(Set<Operation> bulk) {
        return ManagedSessionFactory.executeRead(em -> {
            int i = 0;
            for (Operation lj : bulk) {
                i++;
                em.merge(lj);
            }
            Enumeration<Operation> enums = Collections.enumeration(bulk);
            return Collections.list(enums);
        });
    }

    @Override
    public List<Operation> findUnSyncedOperations(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.updated_at >= ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, offline);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findOperationByImputation(String DEPT) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.imputation = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, DEPT);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Operation> findByDateInterval(Depense dep, LocalDate date1, LocalDate date2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM operation p WHERE p.depense_id =  ? AND p.date BETWEEN ? AND ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Operation.class);
                query.setParameter(1, dep.getUid());
                query.setParameter(2, date1.atStartOfDay());
                query.setParameter(3, date2.atStartOfDay());
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM operation p WHERE p.uid = ? AND p.updated_at = ?");
        return ManagedSessionFactory.executeRead(em -> {
            Query query = em.createNativeQuery(sb.toString(), Operation.class);
            query.setParameter(1, uid);
            query.setParameter(2, atime);
            List<Operation> result = query.getResultList();
            return !result.isEmpty();
        });
    }

}
