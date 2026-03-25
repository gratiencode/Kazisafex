package services;

import IServices.ImputerStorage;
import data.Category;
import data.Depense;
import data.Imputer;
import data.MatiereSku;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class ImputerService implements ImputerStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Imputer c WHERE c.uid = :id";
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

    public ImputerService() {
        //initializing...
    }

    @Override
    public Imputer saveImputer(Imputer d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(d);
                return d;
            }).thenAccept(e -> {
                System.out.println("Element dps " + e.getRegion() + " enregistree");
            });
            return d;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(d);
        tx.commit();
        return d;
    }

    @Override
    public Imputer updateImputer(Imputer d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(d);
                return d;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getRegion() + " enregistree");
            });
            return d;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(d);
        tx.commit();
        return d;
    }

    @Override
    public Imputer findImputer(String d) {
        if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                   return ManagedSessionFactory.getEntityManager().find(Imputer.class, d);
                });
            }
        return ManagedSessionFactory.getEntityManager().find(Imputer.class, d);
    }

    @Override
    public List<Imputer> findImputers() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM imputer r ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Imputer.class);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Imputer.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteImputer(Imputer d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(d));
                return d;
            }).thenAccept(e -> {
                System.out.println("Element dps " + e.getUid() + " supprimee");
            });
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(d));
        tx.commit();
    }

    @Override
    public List<Imputer> findForProduction(String uid) {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM imputer r ");
            sb.append("WHERE r.production_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Imputer.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Imputer.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Imputer> findByDateInterval(Depense value, LocalDate value0, LocalDate value1) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT i.uid,i.operation_id, i.production_id,i.date_,i.montant,i.devise,i.percent,i.region, i.deleted_at, i.updated_at"
                    + " FROM imputer i,operation o ");
            sb.append("WHERE i.operation_id=o.uid AND o.depense_id = ? AND i.date_ BETWEEN ? and ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Imputer.class);
                    query.setParameter(1, value.getUid());
                    query.setParameter(2, value0);
                    query.setParameter(3, value1);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Imputer.class);
            query.setParameter(1, value.getUid());
            query.setParameter(2, value0);
            query.setParameter(3, value1);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM imputer p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Imputer.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Imputer> result = query.getResultList();
        return !result.isEmpty();
                });
            }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Imputer.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Imputer> result = query.getResultList();
        return !result.isEmpty();
    }

}
