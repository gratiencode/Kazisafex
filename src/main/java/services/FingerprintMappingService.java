package services;

import IServices.FingerprintMappingStorage;
import data.FingerprintMapping;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class FingerprintMappingService implements FingerprintMappingStorage {

    @Override
    public FingerprintMapping createMapping(FingerprintMapping mapping) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(mapping);
                return mapping;
            });
            return mapping;
        } else {
            EntityManager em = ManagedSessionFactory.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(mapping);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive())
                    tx.rollback();
                e.printStackTrace();
            } finally {
                ManagedSessionFactory.closeEntityManager();
            }
            return mapping;
        }
    }

    @Override
    public FingerprintMapping updateMapping(FingerprintMapping mapping) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> em.merge(mapping));
            return mapping;
        } else {
            EntityManager em = ManagedSessionFactory.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(mapping);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive())
                    tx.rollback();
                e.printStackTrace();
            } finally {
                ManagedSessionFactory.closeEntityManager();
            }
            return mapping;
        }
    }

    @Override
    public void deleteMapping(String agentId) {
        FingerprintMapping mapping = findByAgentId(agentId);
        if (mapping != null) {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    FingerprintMapping m = em.find(FingerprintMapping.class, agentId);
                    if (m != null)
                        em.remove(m);
                    return null;
                });
            } else {
                EntityManager em = ManagedSessionFactory.getEntityManager();
                EntityTransaction tx = em.getTransaction();
                try {
                    tx.begin();
                    FingerprintMapping m = em.find(FingerprintMapping.class, agentId);
                    if (m != null)
                        em.remove(m);
                    tx.commit();
                } catch (Exception e) {
                    if (tx.isActive())
                        tx.rollback();
                    e.printStackTrace();
                } finally {
                    ManagedSessionFactory.closeEntityManager();
                }
            }
        }
    }

    @Override
    public FingerprintMapping findByAgentId(String agentId) {
        return ManagedSessionFactory.executeRead(em -> em.find(FingerprintMapping.class, agentId));
    }

    @Override
    public FingerprintMapping findByHash(String hash) {
        return ManagedSessionFactory.executeRead(em -> {
            List<FingerprintMapping> results = em
                    .createQuery("SELECT f FROM FingerprintMapping f WHERE f.fingerprintHash = :hash",
                            FingerprintMapping.class)
                    .setParameter("hash", hash)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        });
    }

    @Override
    public List<FingerprintMapping> findAll() {
        return ManagedSessionFactory.executeRead(
                em -> em.createQuery("SELECT f FROM FingerprintMapping f", FingerprintMapping.class).getResultList());
    }
}
