/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.EntreposerStorage;
import data.Category;
import data.Entreposer;
import data.MatiereSku;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import tools.Constants;

/**
 *
 * @author endeleya
 */
public class EntreposerService implements EntreposerStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Entreposer c WHERE c.id = :id";
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

    public EntreposerService() {
        //initializing...
    }

    @Override
    public Entreposer saveEntreposer(Entreposer d) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(d);
                return d;
            }).thenAccept(e -> {
                System.out.println("Element dps " + e.getNumlot() + " enregistree");
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
    public Entreposer updateEntreposer(Entreposer d) {
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
    public Entreposer findEntreposer(String d) {
        return ManagedSessionFactory.getEntityManager().find(Entreposer.class, d);
    }

    @Override
    public List<Entreposer> findEntreposers() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Entreposer.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Entreposer.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteEntreposer(Entreposer d) {
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
    public double sumValueStockMP() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(((e.quantite*m.quant_contenu_sku)*(e.cout/m.quant_contenu_sku))) "
                    + "FROM entreposer e,matiere_sku m WHERE e.matiere_id = m.matiere_id AND e.sku_id=m.uid ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(),Double.class);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Entreposer> findEntreposersGroupedByIntrant() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.niveau_fabrication = ? ");
            sb.append("GROUP BY e.matiere_id");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, Constants.MANUFACTURING_LEVEL_RAW_MATERIAL);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, Constants.MANUFACTURING_LEVEL_RAW_MATERIAL);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findFinishedProduction() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.niveau_fabrication = ? OR e.niveau_fabrication = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, Constants.MANUFACTURING_LEVEL_MADE_PRODUCT);
                    query.setParameter(2, Constants.MANUFACTURING_LEVEL_MIDDLE_END_PRODUCT);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, Constants.MANUFACTURING_LEVEL_MADE_PRODUCT);
            query.setParameter(2, Constants.MANUFACTURING_LEVEL_MIDDLE_END_PRODUCT);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findEntreposerByLevel(String mp) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.niveau_fabrication = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, mp);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, mp);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double findSommeEntree(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM((e.quantite*m.quant_contenu_sku)) "
                    + "FROM entreposer e,matiere_sku m WHERE e.matiere_id = m.matiere_id AND e.sku_id=m.uid ");
            sb.append("AND e.matiere_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(),Double.class);
                    query.setParameter(1, uid);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Entreposer> toFefoOrdering(String matiereId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.matiere_id = ? ORDER BY e.expiry_date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, matiereId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, matiereId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toFifoOrdering(String matiereId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.matiere_id = ? ORDER BY e.date_ ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, matiereId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, matiereId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toLifoOrdering(String matiereId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.matiere_id = ? ORDER BY e.date_ DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, matiereId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, matiereId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toFefoOrdering(String matiereId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.matiere_id = ? AND e.region = ? ORDER BY e.expiry_date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, matiereId);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, matiereId);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toFifoOrdering(String matiereId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.matiere_id = ? AND e.region = ? ORDER BY e.date_ ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, matiereId);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, matiereId);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toLifoOrdering(String matiereId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.matiere_id = ? AND e.region = ? ORDER BY e.date_ DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, matiereId);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, matiereId);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findEntreposerByLevel(LocalDate value, LocalDate value0, String MANUFACTURING_LEVEL_RAW_MATERIAL) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.date_ BETWEEN ? AND ? AND e.niveau_fabrication = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, value);
                    query.setParameter(2, value0);
                    query.setParameter(3, MANUFACTURING_LEVEL_RAW_MATERIAL);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, value);
            query.setParameter(2, value0);
            query.setParameter(3, MANUFACTURING_LEVEL_RAW_MATERIAL);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findEntreposersGroupedByIntrant(LocalDate value, LocalDate value0) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.niveau_fabrication = ? AND e.date_ BETWEEN ? AND ? ");
            sb.append("GROUP BY e.matiere_id");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, Constants.MANUFACTURING_LEVEL_RAW_MATERIAL);
                    query.setParameter(2, value);
                    query.setParameter(3, value0);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, Constants.MANUFACTURING_LEVEL_RAW_MATERIAL);
            query.setParameter(2, value);
            query.setParameter(3, value0);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findEntreposersGroupedByProd(LocalDate value, LocalDate value0) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT e.uid,e.livraison_id, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,"
                    + "e.date_,e.expiry_date,e.sku_id,e.numlot,sum(e.quantite) as quantite,e.comment, "
                    + "e.cout,e.region,e.devise, e.niveau_fabrication,e.qualite,e.deleted_at,e.updated_at FROM entreposer e,production p WHERE e.production_id=p.uid"
                    + " AND e.date_ BETWEEN ? AND ? ");
            sb.append("GROUP BY p.produit_id");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, value);
                    query.setParameter(2, value0);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, value);
            query.setParameter(2, value0);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    //e.uid, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,
    //e.date_,e.expiry_date,e.numlot,sum(e.quantite) as quantite,e.cout,e.region

    @Override
    public List<Entreposer> findProdEntreposers(String uid, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT e.uid,e.livraison_id, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,"
                    + "e.date_,e.expiry_date,e.sku_id,e.numlot,sum(e.quantite) as quantite,e.comment, "
                    + "e.cout,e.region,e.devise, e.niveau_fabrication,e.qualite FROM entreposer e WHERE "
                    + "e.production_id = ? AND e.numlot = ? GROUP BY e.production_id");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findProdEntreposers(String prouid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT e.uid,e.livraison_id, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,"
                    + "e.date_,e.expiry_date,e.sku_id,e.numlot,e.quantite,e.comment, "
                    + "e.cout,e.region,e.devise, e.niveau_fabrication,e.qualite FROM entreposer e WHERE "
                    + "e.production_id = ? GROUP BY e.production_id");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, prouid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, prouid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toFefoOrderingProd(String prod) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT e.uid,e.livraison_id, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,"
                    + "e.date_,e.expiry_date,e.sku_id,e.numlot,e.quantite,e.comment, "
                    + "e.cout,e.region,e.devise, e.niveau_fabrication,e.qualite, e.deleted_at, e.updated_at "
                    + "FROM entreposer e,production p WHERE e.production_id=p.uid AND p.produit_id = ? ORDER BY e.expiry_date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, prod);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, prod);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toFifoOrderingProd(String prod) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT e.uid,e.livraison_id, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,"
                    + "e.date_,e.expiry_date,e.sku_id,e.numlot,e.quantite,e.comment, "
                    + "e.cout,e.region,e.devise, e.niveau_fabrication,e.qualite FROM entreposer e,production p "
                    + "WHERE e.production_id=p.uid AND p.produit_id = ? ORDER BY e.date_ ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, prod);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, prod);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> toLifoOrderingProd(String prod) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT e.uid,e.livraison_id, e.mesure_id,e.production_id,e.depot_id,e.matiere_id,"
                    + "e.date_,e.expiry_date,e.sku_id,e.numlot,e.quantite,e.comment, "
                    + "e.cout,e.region,e.devise, e.niveau_fabrication,e.qualite FROM entreposer e,production p"
                    + " WHERE e.production_id=p.uid AND p.produit_id  = ? ORDER BY e.date_ DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, prod);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, prod);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Entreposer> findByProduction(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM entreposer e WHERE e.production_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM entreposer p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Entreposer.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Entreposer> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Entreposer.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Entreposer> result = query.getResultList();
        return !result.isEmpty();
    }

}
