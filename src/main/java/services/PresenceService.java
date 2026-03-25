package services;

import IServices.PresenceStorage;
import data.Presence;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

public class PresenceService implements PresenceStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Presence p WHERE p.uid = :id";
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

    @Override
    public Presence createPresence(Presence presence) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(presence);
                return presence;
            });
            return presence;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive())
            tx.begin();
        ManagedSessionFactory.getEntityManager().merge(presence);
        tx.commit();
        return presence;
    }

    @Override
    public Presence updatePresence(Presence presence) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(presence);
                return presence;
            });
            return presence;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive())
            tx.begin();
        ManagedSessionFactory.getEntityManager().merge(presence);
        tx.commit();
        return presence;
    }

    @Override
    public void deletePresence(Presence presence) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(presence));
                return presence;
            });
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive())
            tx.begin();
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(presence));
        tx.commit();
    }

    @Override
    public Presence findPresence(String uid) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Presence.class, uid));
        }
        return ManagedSessionFactory.getEntityManager().find(Presence.class, uid);
    }

    @Override
    public List<Presence> findPresences() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Presence.findAll").getResultList());
            }
            return ManagedSessionFactory.getEntityManager().createNamedQuery("Presence.findAll").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Presence> findPresencesByAgent(String agentId) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Presence.findByAgentId")
                        .setParameter("agentId", agentId)
                        .getResultList());
            }
            return ManagedSessionFactory.getEntityManager().createNamedQuery("Presence.findByAgentId")
                    .setParameter("agentId", agentId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Presence> findPresencesByPeriod(LocalDateTime start, LocalDateTime end) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Presence.findByPeriod")
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList());
            }
            return ManagedSessionFactory.getEntityManager().createNamedQuery("Presence.findByPeriod")
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Presence> findPresencesByRegion(String region) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Presence.findByRegion")
                        .setParameter("region", region)
                        .getResultList());
            }
            return ManagedSessionFactory.getEntityManager().createNamedQuery("Presence.findByRegion")
                    .setParameter("region", region)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            String sql = "SELECT COUNT(*) FROM presence";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory
                        .executeRead(em -> ((Number) em.createNativeQuery(sql).getSingleResult()).longValue());
            }
            return ((Number) ManagedSessionFactory.getEntityManager().createNativeQuery(sql).getSingleResult())
                    .longValue();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Presence> mergeSet(Set<Presence> bulk) {
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive())
            tx.begin();
        int i = 0;
        for (Presence p : bulk) {
            i++;
            ManagedSessionFactory.getEntityManager().merge(p);
            if (i % 16 == 0) {
                tx.commit();
                ManagedSessionFactory.getEntityManager().clear();
                tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive())
                    tx.begin();
            }
        }
        tx.commit();
        return Collections.list(Collections.enumeration(bulk));
    }

    @Override
    public List<Presence> findUnSyncedPresences(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            String sql = "SELECT * FROM presence p WHERE p.updated_at >= ?";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sql, Presence.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql, Presence.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        String sql = "SELECT * FROM presence p WHERE p.uid = ? AND p.updated_at = ?";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sql, Presence.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                return !query.getResultList().isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql, Presence.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        return !query.getResultList().isEmpty();
    }
}
