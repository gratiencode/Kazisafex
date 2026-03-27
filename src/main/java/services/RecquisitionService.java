/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RecquisitionStorage;
import com.endeleya.kazisafex.PosController;
import data.Category;
import data.Client;
import data.Compter;
import data.LigneVente;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.StockerDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.Mesure;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.PrixDeVente;
import data.Inventaire;
import data.StockAgregate;
import data.Vente;
import data.helpers.CardHelper;
import delegates.LigneVenteDelegate;
import delegates.RecquisitionDelegate;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import data.PermitTo;
import delegates.PermissionDelegate;
import java.util.NoSuchElementException;
import java.util.prefs.Preferences;
import tools.Constants;
import tools.DataId;
import tools.ListViewItem;
import tools.Rupture;
import utilities.Peremption;
import tools.Util;

/**
 *
 * @author eroot
 */
public class RecquisitionService implements RecquisitionStorage {

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Recquisition c WHERE c.uid = :id";
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

    Preferences pref;

    public RecquisitionService() {
        // initializing...
        pref = Preferences.userNodeForPackage(tools.SyncEngine.class);
    }

    @Override
    public Recquisition createRecquisition(Recquisition cat) {
        // if(!PermissionDelegate.hasPermission(PermitTo.CREATE_RECQUISITION)){
        // return null;
        // }
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
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

    public Client createClient(Client cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getUid() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        try {
            if (!etr.isActive()) {
                etr.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            etr.commit();
            return cat;
        } catch (Exception e) {
            return cat;
        }
    }

    public Vente createVente(Vente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        try {
            if (!etr.isActive()) {
                etr.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            etr.commit();
            return cat;
        } catch (Exception e) {
            return cat;
        }

    }

    public LigneVente createLigneVente(LigneVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element LV " + e.getNumlot() + " enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        try {
            if (!etr.isActive()) {
                etr.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            etr.commit();
            return cat;
        } catch (Exception e) {
            return cat;
        }
    }

    @Override
    public Recquisition updateRecquisition(Recquisition cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
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
    public void deleteRecquisition(Recquisition cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public Recquisition findRecquisition(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Recquisition.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(Recquisition.class, catId);
    }

    @Override
    public List<Recquisition> findRecquisitions() {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createNamedQuery("Recquisition.findAll").getResultList());
        }
        Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Recquisition.findAll");
        return query.getResultList();
    }

    @Override
    public StockAgregate saveStockFromRecquisition(Recquisition e) {
        stock = new StockAgregate(DataId.generate());
        LocalDate leo = LocalDate.now();
        stock.setCoutAchat(e.getCoutAchat());
        stock.setDate(leo.atStartOfDay());
        stock.setEntrees(e.getQuantite());
        stock.setSorties(0d);
        stock.setInitialQuantity(0d);
        stock.setExpiree(0d);
        stock.setFinalQuantity(e.getQuantite());
        stock.setContext("Journalier du " + leo);
        stock.setMesureId(e.getMesureId());
        stock.setProductId(e.getProductId());
        stock.setRegion(e.getRegion());
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(stock);
                return stock;
            }).thenAccept(es -> {
                System.out.println("Stock de " + es.getProductId().getNomProduit() + " enregistree");
            });
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(stock);
            tx.commit();
        }
        return stock;
    }

    @Override
    public List<Recquisition> findRecquisitions(int start, int max) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Recquisition.findAll");
                    query.setFirstResult(start);
                    query.setMaxResults(max);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Recquisition.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduit(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), Recquisition.class)
                        .setParameter(1, objId).getResultList());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM recquisition");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Long.class);
                    Long dos = (Long) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            return (Long) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduit(String objId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, objId);
                    query.setParameter(2, lot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, objId);
            query.setParameter(2, lot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    public List<Recquisition> findOrphanRecquisitions(String prodId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.uid NOT IN "
                    + "(SELECT p.recquisition_id FROM prix_de_vente p) AND"
                    + " s.product_id = ? ORDER BY s.date DESC ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, prodId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, prodId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    public List<PrixDeVente> findLastPrices(String prodId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente p WHERE p.recquisition_id IN ");
            sb.append("(SELECT s.uid FROM recquisition s WHERE s.uid IN ");
            sb.append("(SELECT pv.recquisition_id FROM prix_de_vente pv) AND s.product_id = ? ORDER BY s.date DESC)");

            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, prodId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, prodId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Recquisition> findRecquisitionByProduitRegion(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findDescSortedByDateForProduit(String uid) {
        List<Recquisition> result = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    List<Recquisition> l = query.getResultList();
                    result.addAll(l);
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            List<Recquisition> l = query.getResultList();
            result.addAll(l);
        } catch (Exception e) {

        }
        return result;
    }

    @Override
    public List<Recquisition> toFefoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.dateExpiry ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Compter findCompteForProduit(String puid, LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM compter p WHERE p.product_id =  ? AND p.date_count BETWEEN ? AND  ? AND p.region = ? ORDER BY p.date_count DESC LIMIT 1 ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, dateDebut);
                    query.setParameter(3, dateFin);
                    query.setParameter(4, region);
                    return (Compter) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, puid);
            query.setParameter(2, dateDebut);
            query.setParameter(3, dateFin);
            query.setParameter(4, region);
            return (Compter) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    // on region
    @Override
    public List<Recquisition> toFefoOrdering(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ORDER BY s.dateExpiry ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ORDER BY s.date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    // end on region

    @Override
    public List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ?  AND s.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findByDateExpInterval(LocalDate time, LocalDate darg) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.dateExpiry BETWEEN ? AND ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, time);
                    query.setParameter(2, darg);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, time);
            query.setParameter(2, darg);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoods() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(cal/pipe.quantcontenu) as q,pipe.description,(pipe.cta*pipe.quantcontenu),pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta - IFNULL(B.tb,0)) as cal,A.cta,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,(r.coutAchat/m.quantcontenu) as cta,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoodsFromRegion(String region) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,(pipe.cta*pipe.quantcontenu),pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.cta,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,(r.coutAchat/m.quantcontenu) as cta,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoodsCategorized(String cat) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, cat);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, cat);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoodsCategorized(String cat, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, region);
                    query.setParameter(2, cat);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, cat);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<Recquisition> findRecquisitions(String region) {
        try {

            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Recquisition.findByRegion");
                    query.setParameter("region", region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Recquisition.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduitWithLotInUnit(String idpro, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(s.quantite*m.quantcontenu) as q FROM recquisition s,mesure m WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, idpro);
                    query.setParameter(2, lot);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, lot);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public Recquisition addToTransaction(Recquisition r) {
        ManagedSessionFactory.getEntityManager().persist(r);
        return r;
    }

    @Override
    public void startTransaction() {
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
    }

    @Override
    public List<Recquisition> findByReference(String ref) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Recquisition.findByReference");
                    query.setParameter("reference", ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Recquisition.findByReference");
            query.setParameter("reference", ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Recherche une recquisition par produit et par reference
     *
     * @param uid le uid du produit
     * @param ref reference du destockage
     * @return
     */
    @Override
    public List<Recquisition> findByReference(String uid, String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, ref);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retourne le stock disponible en piece pour un produit donne en argument
     *
     * @param uid
     * @return le stock en piece
     */
    @Override
    public double findRemainedInMagasinFor(String uid) {
        StockAgregate aggreg = findClosedStock(LocalDate.now(), LocalDate.now(), uid);
        if (aggreg == null) {
            Recquisition dernierR = getLastEntry(uid);
            if (dernierR == null) {
                return 0;
            }
            aggreg = saveStockFromRecquisition(dernierR);
        }
        return aggreg.getFinalQuantity();
    }

    @Override
    public double findRemainedInMagasinFor(String uid, LocalDate d, LocalDate f) {
        StockAgregate aggreg = findClosedStock(d, f, uid);
        if (aggreg == null) {
            Recquisition dernierR = getLastEntry(uid);
            if (dernierR == null) {
                return 0;
            }
            aggreg = saveStockFromRecquisition(dernierR);
        }
        return aggreg.getFinalQuantity();
    }

    private double sumRetourDepot(String proId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id = m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.region = ? ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRetourDepotByLot(String proId, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id = m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, numlot);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, numlot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRetourDepotByLot(String proId, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id = m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.numlot = ? AND r.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, numlot);
                    query.setParameter(3, region);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, numlot);
            query.setParameter(3, region);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumRetourDepot(String proId, LocalDate d, LocalDate f) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id=m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.date_ BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, d.atStartOfDay());
                    query.setParameter(3, f.atStartOfDay());
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay());
            query.setParameter(3, f.atStartOfDay());
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public double sumRetourDepot(String proId, String lot, LocalDate d, LocalDate f) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id=m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.date_ BETWEEN ? AND ? AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, d.atStartOfDay());
                    query.setParameter(3, f.atStartOfDay());
                    query.setParameter(4, lot);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay());
            query.setParameter(3, f.atStartOfDay());
            query.setParameter(4, lot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumRetourDepot(String proId, LocalDate d, LocalDate f, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id=m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.date_ BETWEEN ? AND ? AND r.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, d.atStartOfDay());
                    query.setParameter(3, f.atStartOfDay());
                    query.setParameter(4, region);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay());
            query.setParameter(3, f.atStartOfDay());
            query.setParameter(4, region);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRetourDepot(String proId, String numlot, LocalDate d, LocalDate f, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM retour_depot r,recquisition q,mesure m "
                    + "WHERE q.product_id = ? AND r.mesure_id=m.uid ");
            sb.append(" AND q.uid = r.recquisition_id AND r.date_ BETWEEN ? AND ?"
                    + " AND r.region = ? AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, numlot);
                    query.setParameter(3, region);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay());
            query.setParameter(3, f.atStartOfDay());
            query.setParameter(4, region);
            query.setParameter(5, numlot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumLignevente(String proId, LocalDate d, LocalDate f) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(r.quantite,0)*COALESCE(m.quantcontenu,0)) s FROM ligne_vente r,mesure m WHERE r.product_id = ? "
                    + "AND r.mesure_id=m.uid AND r.reference_uid IN ")
                    .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ?)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, d.atStartOfDay())
                            .setParameter(3, f.atTime(23, 59, 59));
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, d.atStartOfDay())
                    .setParameter(3, f.atTime(23, 59, 59));
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionFrom(String proId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(r.quantite,0)*COALESCE(m.quantcontenu,0)) e FROM recquisition r,mesure m WHERE r.product_id = ?"
                    + " AND r.region = ? AND r.mesure_id=m.uid ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, region);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionFrom(String proId, LocalDate d, LocalDate f, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(r.quantite,0)*COALESCE(m.quantcontenu,0)) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.region = ?"
                    + " AND r.mesure_id=m.uid AND r.date BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, region);
                    query.setParameter(3, d.atStartOfDay()).setParameter(4, f.atTime(23, 59, 59));
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            query.setParameter(3, d.atStartOfDay()).setParameter(4, f.atTime(23, 59, 59));
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumBatchedRecqusitionFrom(String proId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m"
                    + " WHERE r.product_id = ? AND r.numlot = ? AND r.mesure_id=m.uid  ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, lot);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLigneventeFrom(String proId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? ) ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, region);
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumLigneventeFrom(String proId, LocalDate d, LocalDate f, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(r.quantite,0)*COALESCE(m.quantcontenu,0)) s FROM ligne_vente r,mesure m WHERE r.product_id = ? "
                    + "AND r.mesure_id=m.uid AND r.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? AND v.dateVente BETWEEN ? AND ? ) ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, region)
                            .setParameter(3, d.atStartOfDay())
                            .setParameter(4, f.atTime(23, 59, 59));
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region)
                    .setParameter(3, d.atStartOfDay())
                    .setParameter(4, f.atTime(23, 59, 59));
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumBatchedLigneventeFrom(String proId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, lot);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }

            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionByLotFrom(String proId, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m "
                    + "WHERE r.product_id = ? AND r.region = ? AND r.mesure_id=m.uid  AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, numlot);
                    query.setParameter(3, region);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            query.setParameter(3, numlot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLigneventeByLotFrom(String proId, String lot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? AND r.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? ) ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, lot);
                    query.setParameter(3, region);
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionByLotFrom(String proId, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m "
                    + "WHERE r.product_id = ? AND r.mesure_id=m.uid  AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, numlot);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, proId);
            query.setParameter(2, numlot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLigneventeByLotFrom(String proId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, proId);
                    query.setParameter(2, lot);
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    /**
     * Retourne le stock disponible en piece, cad en unite a partir d'une region
     * fourni en argument pour un produit
     *
     * @param uid
     * @param region
     * @return le stock en piece
     */
    @Override
    public double findRemainedInMagasinFor(String uid, String region) {
        double entree = sumRecqusitionFrom(uid, region);
        double sortie = sumLigneventeFrom(uid, region);
        double retour = sumRetourDepot(uid, region);
        return (entree - sortie) - retour;
    }

    public double findRemainedInMagasinFor(String uid, LocalDate d, LocalDate f, String region) {
        double entree = sumRecqusitionFrom(uid, d, f, region);
        double sortie = sumLigneventeFrom(uid, d, f, region);
        double retour = sumRetourDepot(uid, d, f, region);
        return (entree - sortie) - retour;
    }

    @Override
    public List<Rupture> findStockEnRupture() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT r.product_id, r.mesure_id, r.stockAlert, r.dateExpiry, r.date, r.region, r.coutachat, r.uid FROM recquisition r GROUP BY r.product_id ORDER BY r.date DESC ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    List<Object[]> datas = query.getResultList();
                    return ruptures(datas);
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            List<Object[]> datas = query.getResultList();
            return ruptures(datas);
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Rupture> ruptures(List<Object[]> datas) {
        List<Rupture> result = new ArrayList<>();
        for (Object[] data : datas) {
            Rupture r = new Rupture();
            String uidP = String.valueOf(data[0]);
            Produit pro = ProduitDelegate.findProduit(uidP);
            r.setProduit(pro);
            Mesure mezr = MesureDelegate.findMesure(String.valueOf(data[1]));
            r.setMesure(mezr);
            r.setRegion(String.valueOf(data[5]));
            r.setUnitprice(Double.parseDouble(String.valueOf(data[6])));
            if (!Objects.isNull(data[4])) {
                try {
                    String expr = String.valueOf(data[4]);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date dt = sdf.parse(String.valueOf(expr));
                    r.setDate(sdf.format(dt));
                } catch (ParseException ex) {
                    Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Double alrt = Double.valueOf(String.valueOf(data[2] == null ? "0" : data[2]));
            r.setAlert(alrt);
            String uidr = String.valueOf(data[7]);
            List<PrixDeVente> prices = findPricesFor(uidr);
            if (!prices.isEmpty()) {
                PrixDeVente pv = prices.get(0);
                Mesure mp = MesureDelegate.findMesure(pv.getMesureId().getUid());
                r.setSalePrice(pv.getPrixUnitaire() + " " + pv.getDevise() + "/" + mp.getDescription());
            }
            List<Stocker> prox = StockerDelegate.findDescSortedByDateStock(uidP);
            String loc = (prox.isEmpty() ? "N/A" : prox.get(0).getLocalisation());
            r.setLocalisation(loc);
            r.setSelect(false);
            double alrpc = (alrt == null ? 0 : alrt) * mezr.getQuantContenu();
            double rstpc = findRemainedInMagasinFor(uidP);
            if (rstpc <= alrpc || r.isSelect()) {
                r.setQuant(BigDecimal.valueOf(rstpc / mezr.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN)
                        .doubleValue());
                result.add(r);
                // add to result list
            }
        }
        return result;
    }

    @Override
    public List<Rupture> findStockEnRupture(String region) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT r.product_id, r.mesure_id, r.stockAlert, r.dateExpiry, r.date, r.region, r.coutachat, r.uid FROM recquisition r WHERE r.region = ? GROUP BY r.product_id ORDER BY r.date DESC ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    List<Object[]> datas = query.getResultList();
                    return ruptures(datas);
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, region);
            List<Object[]> datas = query.getResultList();
            return ruptures(datas);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * La quantity en piece ici est a diviser par contenu mesure
     *
     * @param region
     * @return
     */
    @Override
    public List<Recquisition> findRecquisitionByRegionGroupBylot(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference,"
                    + " s.observation,s.coutachat,s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.region = ?"
                    + "  GROUP BY s.product_id, s.numlot ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);

            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionGroupByLot() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,s.coutachat,"
                    + "s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid  GROUP BY s.product_id, s.numlot");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByRegionGroupBylot(LocalDate debut, LocalDate fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,"
                    + "s.coutachat,s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.region = ? AND s.date BETWEEN ? AND ?  GROUP BY s.product_id, s.numlot");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, region);
                    query.setParameter(2, debut.atStartOfDay()).setParameter(3, fin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, region);
            query.setParameter(2, debut.atStartOfDay()).setParameter(3, fin.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionGroupByLot(LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,s.coutachat,"
                    + "s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.date BETWEEN ? AND ? GROUP BY s.product_id, s.numlot");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, debut.atStartOfDay());
                    query.setParameter(2, fin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, debut.atStartOfDay());
            query.setParameter(2, fin.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double findRemainedInMagasinByLot(String puid, String numlot) {
        double en = sumRecqusitionByLotFrom(puid, numlot);
        double so = sumLigneventeByLotFrom(puid, numlot);
        double ret = sumRetourDepotByLot(puid, numlot);
        return en - so - ret;
    }

    @Override
    public double findRemainedInMagasinByLot(String puid, String numlot, String region) {
        double en = sumRecqusitionByLotFrom(puid, numlot, region);
        double so = sumLigneventeByLotFrom(puid, numlot, region);
        double ret = sumRetourDepotByLot(puid, numlot, region);
        return en - so - ret;
    }

    @Override
    public double sumByProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m"
                    + " WHERE r.product_id = ? AND r.mesure_id=m.uid  ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m "
                    + "WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    Double d = (Double) query.getSingleResult();
                    return d == null ? 0 : d;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            query.setParameter(2, region);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Recquisition> mergeSet(Set<Recquisition> bulk) {
        return null;
    }

    @Override
    public List<Recquisition> findByReference(String ref, String uid, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.reference = ? AND s.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid)
                            .setParameter(2, ref)
                            .setParameter(3, numlot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid)
                    .setParameter(2, ref)
                    .setParameter(3, numlot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduit(String idpro, LocalDate d1, LocalDate d2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m "
                    + "WHERE r.product_id = ? AND r.mesure_id=m.uid  AND date BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, idpro);
                    query.setParameter(2, d1.atStartOfDay());
                    query.setParameter(3, d2.atTime(23, 59, 59));
                    Object dbl = query.getSingleResult();
                    return dbl == null ? 0 : (Double) dbl;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1.atStartOfDay());
            query.setParameter(3, d2.atTime(23, 59, 59));
            Object dbl = query.getSingleResult();
            return dbl == null ? 0 : (Double) dbl;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String idpro, LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m "
                    + "WHERE r.product_id = ? AND r.mesure_id=m.uid AND date BETWEEN ? AND ? AND region = ?  ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, idpro);
                    query.setParameter(2, d1.atStartOfDay());
                    query.setParameter(3, d2.atStartOfDay());
                    query.setParameter(4, region);
                    Object dbl = query.getSingleResult();
                    return dbl == null ? 0 : (Double) dbl;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1.atStartOfDay());
            query.setParameter(3, d2.atStartOfDay());
            query.setParameter(4, region);
            Object dbl = query.getSingleResult();
            return dbl == null ? 0 : (Double) dbl;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate today1) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT uid, SUM(s.initial_quantity) as initial_quantity , SUM(s.entrees) as entrees"
                + ", SUM(s.sorties) as sorties,SUM(s.final_quantity) as final_quantity, SUM(s.expiree) as expiree, "
                + "s.cout_achat, s.region, s.context, s.date, s.mesure_id, s.product_id "
                + "FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.product_id = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                query.setParameter(1, today.atStartOfDay());
                query.setParameter(2, today1.atTime(23, 59, 59));
                query.setParameter(3, prod.getUid());
                List<StockAgregate> results = query.getResultList();
                return results.isEmpty() ? null : results.get(0);
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), StockAgregate.class);
        query.setParameter(1, today.atStartOfDay());
        query.setParameter(2, today1.atTime(23, 59, 59));
        query.setParameter(3, prod.getUid());
        List<StockAgregate> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate otherDay, String region) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT uid, SUM(s.initial_quantity) as initial_quantity , SUM(s.entrees) as entrees"
                + ", SUM(s.sorties) as sorties,SUM(s.final_quantity) as final_quantity, SUM(s.expiree) as expiree, "
                + "s.cout_achat, s.region, s.context, s.date,  s.mesure_id, s.product_id "
                + "FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.region = ? AND s.product_id = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                query.setParameter(1, today.atStartOfDay());
                query.setParameter(2, otherDay.atTime(23, 59, 59));
                query.setParameter(3, region);
                query.setParameter(4, prod.getUid());
                List<StockAgregate> results = query.getResultList();
                return results.isEmpty() ? null : results.get(0);
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), StockAgregate.class);
        query.setParameter(1, today.atStartOfDay());
        query.setParameter(2, otherDay.atTime(23, 59, 59));
        query.setParameter(3, region);
        query.setParameter(4, prod.getUid());
        List<StockAgregate> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public CardHelper populateCardFor(Produit product) {
        StockAgregate curentStock = findStockFor(product, LocalDate.now(), LocalDate.now());
        if (curentStock == null) {
            return null;
        }
        double pieces = curentStock.getFinalQuantity();
        if (pieces <= 0) {
            return null;
        }
        List<Recquisition> rs = getSortedAccordingToInventoryMethod(product.getUid());
        if (rs == null) {
            return null;
        }
        List<PrixDeVente> prices = new ArrayList<>();
        Recquisition r = getLastEntry(product.getUid());// getLastEntry(product);
        for (Recquisition rx : rs) {
            List<PrixDeVente> pricez = findPricesFor(rx.getUid());
            if (!pricez.isEmpty()) {
                for (PrixDeVente prixDeVente : pricez) {
                    if (prixDeVente.getPrixUnitaire() > 0) {
                        prices.add(prixDeVente);
                    }
                }
                if (!prices.isEmpty()) {
                    break;
                }
            }
        }
        if (prices.isEmpty()) {
            return null;
        }
        r.setPrixDeVenteList(prices);
        Mesure mesure = r.getMesureId();
        double resteEnMesure = pieces / mesure.getQuantContenu();
        CardHelper helper = new CardHelper();
        helper.setRecquisition(r);
        helper.setRemainedQuantity(resteEnMesure);
        helper.setRemainedMesure(mesure);
        return helper;
    }

    @Override
    public CardHelper populateCardFor(Produit product, String region) {
        StockAgregate curentStock = findStockFor(product, LocalDate.now(), LocalDate.now(), region);
        double pieces = curentStock.getFinalQuantity();
        if (pieces <= 0) {
            return null;
        }
        List<Recquisition> rs = getSortedAccordingToInventoryMethodAt(product.getUid(), region);
        if (rs == null) {
            return null;
        }
        List<PrixDeVente> prices = new ArrayList<>();
        Recquisition r = getLastEntry(Constants.getStringPref("meth", "fifo"), product, region);// getLastEntry(product);
        for (Recquisition rx : rs) {
            List<PrixDeVente> pricez = findPricesFor(rx.getUid());
            if (!pricez.isEmpty()) {
                for (PrixDeVente prixDeVente : pricez) {
                    if (prixDeVente.getPrixUnitaire() > 0) {
                        prices.add(prixDeVente);
                    }
                }
                if (!prices.isEmpty()) {
                    break;
                }
            }
        }
        if (prices.isEmpty()) {
            return null;
        }
        r.setPrixDeVenteList(prices);
        Mesure mesure = r.getMesureId();
        double resteEnMesure = pieces / mesure.getQuantContenu();
        CardHelper helper = new CardHelper();
        helper.setRecquisition(r);
        helper.setRemainedQuantity(resteEnMesure);
        helper.setRemainedMesure(mesure);
        return helper;
    }

    @Override
    public CardHelper populateCardFor(Produit product, LocalDate debut, LocalDate fin) {
        double pieces = findRemainedInMagasinFor(product.getUid(), debut, fin);
        if (pieces <= 0) {
            return null;
        }
        List<Recquisition> rs = getSortedAccordingToInventoryMethod(product.getUid(), debut, fin);
        if (rs == null) {
            return null;
        }
        List<PrixDeVente> prices = new ArrayList<>();
        Recquisition r = getLastEntry(product.getUid());
        for (Recquisition rx : rs) {
            List<PrixDeVente> pricez = findPricesFor(rx.getUid());
            if (!pricez.isEmpty()) {
                for (PrixDeVente prixDeVente : pricez) {
                    if (prixDeVente.getPrixUnitaire() > 0) {
                        prices.add(prixDeVente);
                    }
                }
                if (!prices.isEmpty()) {
                    break;
                }
            }
        }
        if (prices.isEmpty()) {
            return null;
        }
        r.setPrixDeVenteList(prices);
        Mesure mesure = r.getMesureId();
        double resteEnMesure = pieces / mesure.getQuantContenu();
        CardHelper helper = new CardHelper();
        helper.setRecquisition(r);
        helper.setRemainedQuantity(resteEnMesure);
        helper.setRemainedMesure(mesure);
        return helper;
    }

    @Override
    public CardHelper populateCardFor(Produit product, LocalDate debut, LocalDate fin, String region) {
        double pieces = findRemainedInMagasinFor(product.getUid(), debut, fin, region);
        if (pieces <= 0) {
            return null;
        }
        List<Recquisition> rs = getSortedAccordingToInventoryMethodAt(product.getUid(), debut, fin, region);
        if (rs == null) {
            return null;
        }
        List<PrixDeVente> prices = new ArrayList<>();
        Recquisition r = getHeaderRecq(Constants.getStringPref("meth", "fifo"), product, region);
        for (Recquisition rx : rs) {
            List<PrixDeVente> pricez = findPricesFor(rx.getUid());
            if (!pricez.isEmpty()) {
                for (PrixDeVente prixDeVente : pricez) {
                    if (prixDeVente.getPrixUnitaire() > 0) {
                        prices.add(prixDeVente);
                    }
                }
                if (!prices.isEmpty()) {
                    break;
                }
            }
        }
        if (prices.isEmpty()) {
            return null;
        }
        r.setPrixDeVenteList(prices);
        Mesure mesure = r.getMesureId();
        double resteEnMesure = pieces / mesure.getQuantContenu();
        CardHelper helper = new CardHelper();
        helper.setRecquisition(r);
        helper.setRemainedQuantity(resteEnMesure);
        helper.setRemainedMesure(mesure);
        return helper;
    }

    @Override
    public List<ListViewItem> populate() {
        List<ListViewItem> result = new ArrayList<>();
        List<Produit> produits = getProduits();
        System.out.println("Populating products, total product count = " + produits.size());

        for (Produit product : produits) {
            // Must have a recent StockAgregate record
            StockAgregate aggreg = findLatestStockAgregate(product.getUid());
            // AND must have a recquisition
            Recquisition r = getLastEntry(product.getUid());

            if (aggreg != null && r != null) {
                Mesure mesure = aggreg.getMesureId();
                if (mesure != null) {
                    // AND must have a price
                    PrixDeVente pvd = getExistingPricefor(r, MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
                    if (pvd != null) {
                        double qr = aggreg.getFinalQuantity();
                        // AND quantity must be > 0
                        if (qr > 0) {
                            double qw = BigDecimal.valueOf(qr / mesure.getQuantContenu())
                                    .setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                            List<PrixDeVente> gros = findGrossPrices(r.getUid(), mesure.getUid());
                            
                            ListViewItem item = new ListViewItem();
                            item.setQuantiteRestant(qw);
                            item.setMesureAchat(mesure);
                            item.setCoutAchat(aggreg.getCoutAchat());
                            item.setNumlot(r.getNumlot());
                            item.setPeremption(r.getDateExpiry());
                            item.setProduit(product);
                            item.setPurchasePrice(r.getCoutAchat());
                            
                            Mesure detailMesure = pvd.getMesureId() == null
                                    ? findMinMesureForProduit(product.getUid())
                                    : pvd.getMesureId();
                            item.setMesureDetail(detailMesure);
                            item.setDetailPrice(pvd.getPrixUnitaire());
                            
                            if (!gros.isEmpty()) {
                                PrixDeVente grprix = gros.getLast();
                                Mesure grosMesure = grprix.getMesureId() == null
                                        ? findMinMesureForProduit(product.getUid())
                                        : grprix.getMesureId();
                                item.setMesureGros(grosMesure);
                                item.setSalePrice(grprix.getPrixUnitaire());
                            } else {
                                item.setMesureGros(detailMesure);
                                item.setSalePrice(pvd.getPrixUnitaire());
                            }
                            result.add(item);
                        }
                    }
                }
            }
        }
        System.out.println("Populate result size = " + result.size());
        return result;
    }

    @Override
    public List<ListViewItem> populate(String region, String context_cloture) {
        List<ListViewItem> result = new ArrayList<>();
        List<Produit> produits = getProduits();
        for (Produit product : produits) {
            // Must have a recent StockAgregate record for the region
            StockAgregate aggreg = findLatestStockAgregate(product.getUid(), region);
            
            // AND must have a recquisition
            Recquisition r = getLastEntry("lifo", product, region);
            if (r == null) {
                r = getLastEntry(product.getUid());
            }

            if (aggreg != null && r != null) {
                Mesure mesure = (aggreg.getMesureId() == null) ? findMinMesureForProduit(product.getUid())
                        : aggreg.getMesureId();
                if (mesure != null) {
                    // AND must have a price
                    PrixDeVente pvd = getExistingPricefor(r, MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
                    if (pvd != null) {
                        double qr = aggreg.getFinalQuantity();
                        // AND quantity must be > 0
                        if (qr > 0) {
                            double qw = BigDecimal.valueOf(qr / mesure.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN)
                                    .doubleValue();
                            List<PrixDeVente> gros = findGrossPrices(r.getUid(), mesure.getUid());
                            
                            ListViewItem item = new ListViewItem();
                            item.setQuantiteRestant(qw);
                            item.setMesureAchat(mesure);
                            item.setCoutAchat(aggreg.getCoutAchat());
                            item.setNumlot(r.getNumlot());
                            item.setPeremption(r.getDateExpiry());
                            item.setProduit(product);
                            item.setPurchasePrice(r.getCoutAchat());
                            
                            Mesure detailMesure = pvd.getMesureId() == null ? findMinMesureForProduit(product.getUid())
                                    : pvd.getMesureId();
                            item.setMesureDetail(detailMesure);
                            item.setDetailPrice(pvd.getPrixUnitaire());
                            
                            if (!gros.isEmpty()) {
                                PrixDeVente grprix = gros.getLast();
                                Mesure grosMesure = grprix.getMesureId() == null ? findMinMesureForProduit(product.getUid())
                                        : grprix.getMesureId();
                                item.setMesureGros(grosMesure);
                                item.setSalePrice(grprix.getPrixUnitaire());
                            } else {
                                item.setMesureGros(detailMesure);
                                item.setSalePrice(pvd.getPrixUnitaire());
                            }
                            result.add(item);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<ListViewItem> populateBy(String category) {
        List<ListViewItem> result = new ArrayList<>();
        List<Produit> produits = getProduits(category);
        for (Produit product : produits) {
            StockAgregate aggreg = findLatestStockAgregate(product.getUid());
            Recquisition r = getLastEntry(product.getUid());

            if (aggreg != null && r != null) {
                Mesure mesure = (aggreg.getMesureId() == null) ? findMinMesureForProduit(product.getUid())
                        : aggreg.getMesureId();
                if (mesure != null) {
                    PrixDeVente pvd = getExistingPricefor(r, MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
                    if (pvd != null) {
                        double qr = aggreg.getFinalQuantity();
                        if (qr > 0) {
                            List<PrixDeVente> gros = findGrossPrices(r.getUid(), mesure.getUid());
                            ListViewItem item = new ListViewItem();
                            item.setQuantiteRestant(qr);
                            item.setMesureAchat(mesure);
                            item.setCoutAchat(aggreg.getCoutAchat());
                            item.setNumlot(r.getNumlot());
                            item.setPeremption(r.getDateExpiry());
                            item.setProduit(product);
                            item.setPurchasePrice(r.getCoutAchat());
                            Mesure detailMesure = pvd.getMesureId() == null ? findMinMesureForProduit(product.getUid())
                                    : pvd.getMesureId();
                            item.setMesureDetail(detailMesure);
                            item.setDetailPrice(pvd.getPrixUnitaire());
                            if (!gros.isEmpty()) {
                                PrixDeVente grprix = gros.getLast();
                                Mesure grosMesure = grprix.getMesureId() == null ? findMinMesureForProduit(product.getUid())
                                        : grprix.getMesureId();
                                item.setMesureGros(grosMesure);
                                item.setSalePrice(grprix.getPrixUnitaire());
                            } else {
                                item.setMesureGros(detailMesure);
                                item.setSalePrice(pvd.getPrixUnitaire());
                            }
                            result.add(item);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<ListViewItem> populateBy(String category, String region) {
        List<ListViewItem> result = new ArrayList<>();
        List<Produit> produits = getProduits(category);
        for (Produit product : produits) {
            StockAgregate aggreg = findLatestStockAgregate(product.getUid(), region);
            Recquisition r = getHeaderRecq(Constants.getStringPref("meth", "fifo"), product, region);
            
            if (aggreg != null && r != null) {
                Mesure mesure = (aggreg.getMesureId() == null) ? findMinMesureForProduit(product.getUid())
                        : aggreg.getMesureId();
                if (mesure != null) {
                    PrixDeVente pvd = getExistingPricefor(r, MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
                    if (pvd != null) {
                        double qr = aggreg.getFinalQuantity();
                        if (qr > 0) {
                            List<PrixDeVente> gros = findGrossPrices(r.getUid(), mesure.getUid());
                            ListViewItem item = new ListViewItem();
                            item.setQuantiteRestant(qr);
                            item.setMesureAchat(mesure);
                            item.setCoutAchat(aggreg.getCoutAchat());
                            item.setNumlot(r.getNumlot());
                            item.setPeremption(r.getDateExpiry());
                            item.setProduit(product);
                            item.setPurchasePrice(r.getCoutAchat());
                            Mesure detailMesure = pvd.getMesureId() == null ? findMinMesureForProduit(product.getUid())
                                    : pvd.getMesureId();
                            item.setMesureDetail(detailMesure);
                            item.setDetailPrice(pvd.getPrixUnitaire());
                            if (!gros.isEmpty()) {
                                PrixDeVente grprix = gros.getLast();
                                Mesure grosMesure = grprix.getMesureId() == null ? findMinMesureForProduit(product.getUid())
                                        : grprix.getMesureId();
                                item.setMesureGros(grosMesure);
                                item.setSalePrice(grprix.getPrixUnitaire());
                            } else {
                                item.setMesureGros(detailMesure);
                                item.setSalePrice(pvd.getPrixUnitaire());
                            }
                            result.add(item);
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<Produit> getProduits() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM produit ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Produit.class);
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
        return query.getResultList();

    }

    public Category findProductCategory(String catid) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return em.find(Category.class, catid);
                });
            }
            return ManagedSessionFactory.getEntityManager().find(Category.class, catid);
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Produit> getProduits(String category) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit WHERE categoryId_uid = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    query.setParameter(1, category);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, category);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Mesure findMesure(String uid) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.find(Mesure.class, uid));
            }
            return ManagedSessionFactory.getEntityManager().find(Mesure.class, uid);
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Recquisition> getSortedAccordingToInventoryMethod(String uid) {
        String meth = Constants.getStringPref("meth", "fifo");
        List<Recquisition> result = null;
        switch (meth) {
            case "ppps" ->
                result = toFefoOrdering(uid);
            case "fifo" ->
                result = toFifoOrdering(uid);
            case "lifo" ->
                result = toLifoOrdering(uid);
            default -> {
            }
        }
        return result;
    }

    private List<Recquisition> getSortedAccordingToInventoryMethod(String uid, LocalDate d, LocalDate f) {
        String meth = Constants.getStringPref("meth", "fifo");
        List<Recquisition> result = null;
        switch (meth) {
            case "ppps" ->
                result = toFefoOrdering(uid, d, f);
            case "fifo" ->
                result = toFifoOrdering(uid, d, f);
            case "lifo" ->
                result = toLifoOrdering(uid, d, f);
            default -> {
            }
        }
        return result;
    }

    private List<Recquisition> getSortedAccordingToInventoryMethodAt(String uid, String region) {
        String meth = Constants.getStringPref("meth", "fifo");
        List<Recquisition> result = null;
        switch (meth) {
            case "ppps" ->
                result = toFefoOrdering(uid, region);
            case "fifo" ->
                result = toFifoOrdering(uid, region);
            case "lifo" ->
                result = toLifoOrdering(uid, region);
            default -> {
            }
        }
        return result;
    }

    private List<Recquisition> getSortedAccordingToInventoryMethodAt(String uid, LocalDate d, LocalDate f,
            String region) {
        String meth = Constants.getStringPref("meth", "fifo");
        List<Recquisition> result = null;
        switch (meth) {
            case "ppps" ->
                result = toFefoOrdering(uid, d, f, region);
            case "fifo" ->
                result = toFifoOrdering(uid, d, f, region);
            case "lifo" ->
                result = toLifoOrdering(uid, d, f, region);
            default -> {
            }
        }
        return result;
    }

    private List<PrixDeVente> findPricesFor(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND q_min <= ? ORDER BY prix_unitaire ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, 1);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, 1);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<PrixDeVente> findPricesFor(String ruid, String mesure) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND mesureid_uid = ? LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, ruid);
                    query.setParameter(2, mesure);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, ruid);
            query.setParameter(2, mesure);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PrixDeVente> findGrossPrices(String ruid, String mesure) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND q_min > ? AND mesureid_uid != ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                query.setParameter(1, ruid);
                query.setParameter(2, 1);
                query.setParameter(3, mesure);
                List<PrixDeVente> prix = query.getResultList();
                if (!prix.isEmpty()) {
                    return prix;
                } else {
                    sb.setLength(0);
                    sb.append(
                            "SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND q_min > ? AND mesureid_uid = ?");
                    query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, ruid);
                    query.setParameter(2, 1);
                    query.setParameter(3, mesure);
                    return query.getResultList();
                }
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
        query.setParameter(1, ruid);
        query.setParameter(2, 1);
        query.setParameter(3, mesure);
        List<PrixDeVente> prix = query.getResultList();
        if (!prix.isEmpty()) {
            return prix;
        } else {
            sb.setLength(0);
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND q_min > ? AND mesureid_uid = ?");
            query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, ruid);
            query.setParameter(2, 1);
            query.setParameter(3, mesure);
            return query.getResultList();
        }
    }

    @Override
    public Recquisition findCustomized(String uid, String numlot, String ref, LocalDateTime dateStocker) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition WHERE product_id = ? AND numlot = ? AND reference = ? AND date = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, ref);
                    query.setParameter(4, dateStocker);
                    List<Recquisition> dtks = query.getResultList();
                    if (dtks.isEmpty()) {
                        return null;
                    }
                    return dtks.get(0);
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ref);
            query.setParameter(4, dateStocker);
            List<Recquisition> dtks = query.getResultList();
            if (dtks.isEmpty()) {
                return null;
            }
            return dtks.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double findRemainedInMagasinForBatched(String uid, String numlot) {
        double entree = sumBatchedRecqusitionFrom(uid, numlot);
        double sortie = sumBatchedLigneventeFrom(uid, numlot);
        double ret = sumRetourDepotByLot(uid, numlot);
        return (entree - sortie) - ret;
    }

    @Override
    public List<Recquisition> toFefoOrdering(String uid, LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM recquisition s WHERE s.product_id = ? AND s.date BETWEEN ? AND ? ORDER BY s.dateExpiry ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, debut.atStartOfDay());
                    query.setParameter(3, fin.atStartOfDay());
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, debut.atStartOfDay());
            query.setParameter(3, fin.atStartOfDay());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid, LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM recquisition s WHERE s.product_id = ? AND s.date BETWEEN ? AND ? ORDER BY s.date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, debut.atStartOfDay());
                    query.setParameter(3, fin.atStartOfDay());
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, debut.atStartOfDay());
            query.setParameter(3, fin.atStartOfDay());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid, LocalDate debut, LocalDate fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM recquisition s WHERE s.product_id = ? AND s.date BETWEEN ? AND ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, debut.atStartOfDay());
                    query.setParameter(3, fin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, debut.atStartOfDay());
            query.setParameter(3, fin.atStartOfDay());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFefoOrdering(String uid, LocalDate debut, LocalDate fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? AND s.date BETWEEN ? AND ? ORDER BY s.dateExpiry ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    query.setParameter(3, debut.atStartOfDay());
                    query.setParameter(4, fin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            query.setParameter(3, debut.atStartOfDay());
            query.setParameter(4, fin.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid, LocalDate debut, LocalDate fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? AND s.date BETWEEN ? AND ? ORDER BY s.date ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    query.setParameter(3, debut.atStartOfDay());
                    query.setParameter(4, fin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            query.setParameter(3, debut.atStartOfDay());
            query.setParameter(4, fin.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid, LocalDate debut, LocalDate fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? AND s.date BETWEEN ? AND ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, region);
                    query.setParameter(3, debut.atStartOfDay());
                    query.setParameter(4, fin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            query.setParameter(3, debut.atStartOfDay());
            query.setParameter(4, fin.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Recquisition getHeaderRecq(String meth, Produit prod) {
        List<Recquisition> lsks = new ArrayList<>();
        if (meth.equals("ppps")) {
            lsks.addAll(toFefoOrdering(prod.getUid()));
        } else if (meth.equals("fifo")) {
            lsks.addAll(toFifoOrdering(prod.getUid()));
        } else if (meth.equals("lifo")) {
            lsks.addAll(toLifoOrdering(prod.getUid()));
        }
        return lsks.isEmpty() ? null : chooseValideRecquisition(prod, lsks);
    }

    @Override
    public Recquisition getHeaderRecq(String meth, Produit prod, String region) {
        List<Recquisition> lsks = new ArrayList<>();
        if (meth.equals("ppps")) {
            lsks.addAll(toFefoOrdering(prod.getUid(), region));
        } else if (meth.equals("fifo")) {
            lsks.addAll(toFifoOrdering(prod.getUid(), region));
        } else if (meth.equals("lifo")) {
            lsks.addAll(toLifoOrdering(prod.getUid(), region));
        }
        return lsks.isEmpty() ? null : chooseValideRecquisition(prod, lsks, region);
    }

    @Override
    public Recquisition getLastEntry(String meth, Produit prod, String region) {
        if (region == null) {
            return getLastEntry(prod.getUid());
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region LIKE ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, prod.getUid());
                    query.setParameter(2, region);
                    query.setMaxResults(1);
                    List<Recquisition> lsks = query.getResultList();
                    return lsks.isEmpty() ? null : lsks.get(0);
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, prod.getUid());
            query.setParameter(2, region);
            query.setMaxResults(1);
            List<Recquisition> lsks = query.getResultList();
            return lsks.isEmpty() ? null : lsks.get(0);
        } catch (NoResultException e) {
            return null;
        }

    }

    private Recquisition getLastEntry(String prod) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, prod);
                    query.setMaxResults(1);
                    return (Recquisition) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, prod);
            query.setMaxResults(1);
            return (Recquisition) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }

    private Recquisition chooseValideRecquisition(Produit prod, List<Recquisition> reqs) {
        List<Recquisition> lrq;
        List<LigneVente> llv;
        for (Recquisition req : reqs) {
            String numlot = req.getNumlot();
            lrq = RecquisitionDelegate.findRecquisitionByProduit(prod.getUid(), numlot);
            llv = LigneVenteDelegate.findByProduitWithLot(prod.getUid(), numlot);
            double ret = sumRetourDepotByLot(prod.getUid(), numlot);
            double entree = Util.sumQuantInPc(lrq);
            double sortie = Util.sumQuantInPc(llv);
            double reste = (entree - sortie) - ret;
            if (reste > 0) {
                return req;
            }
        }
        if (reqs.isEmpty()) {
            return null;
        }
        return reqs.get(0);
    }

    private Recquisition chooseValideRecquisition(Produit prod, List<Recquisition> reqs, String region) {
        List<Recquisition> lrq;
        List<LigneVente> llv;
        for (Recquisition req : reqs) {
            String numlot = req.getNumlot();

            llv = LigneVenteDelegate.findByProduitWithLot(prod.getUid(), numlot, region);
            lrq = RecquisitionDelegate.findRecquisitionByProduit(prod.getUid(), numlot, region);
            double ret = sumRetourDepotByLot(prod.getUid(), numlot, region);
            double entree = Util.sumQuantInPc(lrq);
            double sortie = Util.sumQuantInPc(llv);
            double reste = (entree - sortie) - ret;
            if (reste > 0) {
                return req;
            }
        }
        if (reqs.isEmpty()) {
            return null;
        }
        return reqs.get(0);
    }

    @Override
    public double findRemainedInMagasinForBatched(String uid, String numlot, LocalDate ouverture, LocalDate cloture) {
        double entree = sumBatchedRecqusitionFrom(uid, numlot, ouverture, cloture);
        double sortie = sumBatchedLigneventeFrom(uid, numlot, ouverture, cloture);
        double ret = sumRetourDepot(uid, numlot, ouverture, cloture);
        return entree - sortie - ret;
    }

    @Override
    public double findRemainedInMagasinForBatched(String uid, String numlot, LocalDate ouverture, LocalDate cloture,
            String region) {
        double entree = sumBatchedRecqusitionFrom(uid, numlot, ouverture, cloture, region);
        double sortie = sumBatchedLigneventeFrom(uid, numlot, ouverture, cloture, region);
        double ret = sumRetourDepot(uid, numlot, ouverture, cloture, region);
        return entree - sortie - ret;
    }

    private double sumBatchedRecqusitionFrom(String uid, String numlot, LocalDate ouverture, LocalDate cloture) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid  AND r.numlot = ?");
            sb.append(" AND r.date BETWEEN ? AND ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, ouverture.atStartOfDay());
                    query.setParameter(4, cloture.atTime(23, 59, 59));
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ouverture.atStartOfDay());
            query.setParameter(4, cloture.atTime(23, 59, 59));
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumBatchedLigneventeFrom(String uid, String numlot, LocalDate ouverture, LocalDate cloture) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? AND r.reference_uid IN (SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? ) ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, ouverture.atStartOfDay());
                    query.setParameter(4, cloture.atTime(23, 59, 59));
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ouverture.atStartOfDay());
            query.setParameter(4, cloture.atTime(23, 59, 59));
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumBatchedRecqusitionFrom(String uid, String numlot, LocalDate ouverture, LocalDate cloture,
            String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid  AND r.numlot = ?");
            sb.append(" AND r.date BETWEEN ? AND ? AND r.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, ouverture.atStartOfDay());
                    query.setParameter(4, cloture.atTime(23, 59, 59));
                    query.setParameter(5, region);
                    Double dos = (Double) query.getSingleResult();
                    return dos == null ? 0 : dos;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ouverture.atStartOfDay());
            query.setParameter(4, cloture.atTime(23, 59, 59));
            query.setParameter(5, region);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumBatchedLigneventeFrom(String uid, String numlot, LocalDate ouverture, LocalDate cloture,
            String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ?"
                    + " AND r.mesure_id=m.uid AND r.numlot = ? AND r.reference_uid IN "
                    + "(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? ) ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, uid);
                    query.setParameter(2, numlot);
                    query.setParameter(3, ouverture.atStartOfDay());
                    query.setParameter(4, cloture.atTime(23, 59, 59));
                    query.setParameter(5, region);
                    Double rst = (Double) query.getSingleResult();
                    return rst == null ? 0 : rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ouverture.atStartOfDay());
            query.setParameter(4, cloture.atTime(23, 59, 59));
            query.setParameter(5, region);
            Double rst = (Double) query.getSingleResult();

            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public static List<Recquisition> getRecquisitions() {
        EntityManager mem = ManagedSessionFactory.getEntityManager();
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("Recquisition.findAll");
                    return query.getResultList();
                });
            }
            Query query = mem.createNamedQuery("Recquisition.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findUnSyncedRecquisitions(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(r.quantite,0)*COALESCE(m.quantcontenu,0)) px FROM recquisition r,mesure m "
                + "WHERE r.product_id = ? AND r.date BETWEEN ? AND ? AND r.region LIKE ? AND "
                + "r.mesure_id=m.uid");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) (em.createNativeQuery(sb.toString(), Double.class)
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region).getSingleResult());
                return entrees == null ? 0 : entrees;
            });
        }
        Double entrees = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
                Double.class).setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region).getSingleResult();
        return entrees == null ? 0 : entrees;
    }

    @Override
    public double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = :pid AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE :regi "
                + "AND v.dateVente BETWEEN :date1 AND :date2)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double sorties = (Double) em.createNativeQuery(sb.toString(), Double.class)
                        .setParameter("pid", uid)
                        .setParameter("date1", datedebut.atStartOfDay())
                        .setParameter("date2", datefin.atTime(23, 59, 59))
                        .setParameter("regi", region)
                        .getSingleResult();
                return sorties == null ? 0 : sorties;
            });
        }
        Double sorties = (Double) ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sb.toString(), Double.class)
                .setParameter("pid", uid)
                .setParameter("date1", datedebut.atStartOfDay())
                .setParameter("date2", datefin.atTime(23, 59, 59))
                .setParameter("regi", region)
                .getSingleResult();
        return sorties == null ? 0 : sorties;
    }

    @Override
    public double calculerStockInitialEnUnite(String uid, LocalDate datedebut, String region) {
        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                + "WHERE r.product_id = :pid AND r.date < :date AND r.region LIKE :regi AND r.mesure_id=m.uid");
        StringBuilder sbS = new StringBuilder();
        sbS.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = :pid AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE :regi AND v.dateVente < :datefin)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) em.createNativeQuery(sbE.toString(),
                        Double.class).setParameter("pid", uid)
                        .setParameter("date", Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter("regi", region)
                        .getSingleResult();
                Double sorties = (Double) em.createNativeQuery(sbS.toString(), Double.class)
                        .setParameter("pid", uid)
                        .setParameter("datefin", Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter("regi", region)
                        .getSingleResult();
                double stok = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
                return stok <= 0 ? 0 : stok;
            });
        }
        Double entrees = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbE.toString(),
                Double.class).setParameter("pid", uid)
                .setParameter("date", datedebut.atStartOfDay())
                .setParameter("regi", region)
                .getSingleResult();

        Double sorties = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbS.toString(),
                Double.class).setParameter("pid", uid)
                .setParameter("datefin", datedebut.atStartOfDay())
                .setParameter("regi", region)
                .getSingleResult();
        double stok = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
        return stok <= 0 ? 0 : stok;
    }

    @Override
    public double getStockExpiree(String uid, LocalDate datedebut, LocalDate datefin, String region) {

        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                + "WHERE r.product_id = ?1 AND r.dateexpiry BETWEEN ?2 AND ?3"
                + " AND r.region LIKE ?4 AND r.mesure_id=m.uid");
        StringBuilder sbRq = new StringBuilder();
        sbRq.append("SELECT * FROM recquisition r "
                + "WHERE r.product_id = ?1 AND r.dateexpiry BETWEEN ?2 AND ?3 AND r.region LIKE ?4");
        StringBuilder sbS = new StringBuilder();
        sbS.append(
                "SELECT (SUM(COALESCE(s.quantite,0)*COALESCE(m.quantcontenu, 0))) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ?1 AND s.mesure_id=m.uid AND s.numlot = ?4 AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE ?5 AND v.dateVente BETWEEN ?2 AND ?3)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                double sorties_exp0 = 0;
                Double entrees_exp = (Double) em.createNativeQuery(sbE.toString(), Double.class)
                        .setParameter(1, uid)
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region).setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .getSingleResult();
                List<Recquisition> experqs = em.createNativeQuery(sbRq.toString(), Recquisition.class)
                        .setParameter(1, uid).setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region)
                        .getResultList();
                for (Recquisition experq : experqs) {
                    Double stx = (Double) em.createNativeQuery(sbS.toString(), Double.class)
                            .setParameter(1, uid).setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                            .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                            .setParameter(5, region)
                            .setParameter(4, experq.getNumlot())
                            .getSingleResult();
                    sorties_exp0 += (stx == null ? 0 : stx);
                }
                double diff = (entrees_exp == null ? 0 : entrees_exp) - sorties_exp0;
                return diff <= 0 ? 0 : diff;
            });

        }

        double sorties_exp = 0;
        Double entrees_exp = (Double) ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sbE.toString(), Double.class)
                .setParameter(1, uid)
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region).setParameter(2, datedebut.atStartOfDay())
                .getSingleResult();
        List<Recquisition> experqs = ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sbRq.toString(), Recquisition.class)
                .setParameter(1, uid).setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region)
                .getResultList();
        for (Recquisition experq : experqs) {
            Double stx = (Double) ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sbS.toString(), Double.class)
                    .setParameter(1, uid).setParameter(2, datedebut.atStartOfDay())
                    .setParameter(3, datefin.atTime(23, 59, 59))
                    .setParameter(5, region)
                    .setParameter(4, experq.getNumlot())
                    .getSingleResult();
            sorties_exp += (stx == null ? 0 : stx);
        }
        double diff = (entrees_exp == null ? 0 : entrees_exp) - sorties_exp;
        return diff <= 0 ? 0 : diff;
    }

    @Override
    public boolean cloturerStocks(String region, LocalDate datedebut, LocalDate datefin, String context) {
        // Reset all aggregates that are NOT for this context to 0
        List<StockAgregate> allAggregates = delegates.RepportDelegate.getStorage().findStockAgregate();
        if (allAggregates != null) {
            for (StockAgregate ag : allAggregates) {
                if (ag.getContext() != null && !ag.getContext().equals(context)) {
                    ag.setFinalQuantity(0d);
                    ag.setEntrees(0d);
                    ag.setSorties(0d);
                    delegates.RepportDelegate.updateStockAgregate(ag);
                }
            }
        }

        List<Produit> produits = getProduits();
        for (Produit produit : produits) {
            Recquisition rn = clotureStockProduit(produit, region, datedebut, datefin, context);
            if (rn == null) {
                cloturons(produit, region, datedebut, datedebut, context);
            }
        }
        return true;
    }

    public double findOnlyStockUnits(Produit produit, String region, LocalDate datedebut, LocalDate datefin) {
        double E = sommeEntreeSurPeriode(produit.getUid(), datedebut, datefin, region);
        double SV = sommeSortieSurPeriode(produit.getUid(), datedebut, datefin, region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), datedebut, region);
        double expiree = getStockExpiree(produit.getUid(), datedebut, datefin, region);
        double stockFinal = stockInit + E - SV;
        double stockFinalValid = stockFinal - expiree;
        return stockFinalValid;
    }

    public void cloturons(Produit produit, String region, LocalDate datedebut, LocalDate datefin,
            String cloture_context) {
        double E = sommeEntreeSurPeriode(produit.getUid(), datedebut, datefin, region);
        double SV = sommeSortieSurPeriode(produit.getUid(), datedebut, datefin, region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), datedebut, region);
        double expiree = getStockExpiree(produit.getUid(), datedebut, datefin, region);
        double stockFinal = stockInit + E - SV;
        double stockFinalValid = stockFinal - expiree;
        Recquisition get = findDescSortedByDateForProduit(produit.getUid()).get(0);
        System.out.println("Dernier rq " + get + " En " + E);
        Mesure unite = MesureDelegate.findByProduitAndQuant(produit.getUid(), 1d);
        StockAgregate stock = findClosedStock(datedebut, datefin, produit.getUid(), region, cloture_context);
        Double qdR = get.getMesureId().getQuantContenu();
        double coutAch = get.getCoutAchat() / qdR;
        if (stock == null) {
            stock = new StockAgregate(DataId.generate());
            stock.setCoutAchat(coutAch);
            LocalDateTime dte = datefin.equals(LocalDate.now()) ? LocalDateTime.now() : datefin.atTime(23, 59, 58);
            stock.setDate(dte);
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setFinalQuantity(stockFinalValid < 0 ? 0 : stockFinalValid);
            stock.setContext(cloture_context);
            stock.setMesureId(unite);
            stock.setProductId(produit);
            stock.setRegion(region);
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(stock);
            tx.commit();
            System.out.println("Stock agreagator..." + produit.getNomProduit() + " " + stockFinalValid);
        } else {
            stock.setCoutAchat(coutAch);
            LocalDateTime dte = datefin.equals(LocalDate.now()) ? LocalDateTime.now() : datefin.atTime(23, 59, 59);
            stock.setDate(dte);
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setContext(cloture_context);
            stock.setFinalQuantity(stockFinalValid);
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().merge(stock);
            tx.commit();
            System.out.println("Update Stock agreagator..." + produit.getNomProduit() + " " + stockFinalValid);
        }

    }

    StockAgregate stock;

    public void rectifyStock(Produit produit, LocalDate datedebut, LocalDate datefin, String region, double coutAch) {
        Mesure unite = MesureDelegate.findByProduitAndQuant(produit.getUid(), 1d);
        stock = findClosedStock(datedebut, datefin, produit.getUid(), region, "Journalier du " + LocalDate.now());
        double E = sommeEntreeSurPeriode(produit.getUid(), datedebut, datefin, region);
        double SV = sommeSortieSurPeriode(produit.getUid(), datedebut, datefin, region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), datedebut, region);
        double expiree = getStockExpiree(produit.getUid(), datedebut, datefin, region);
        double stockFinal = stockInit + E - SV;
        double stockFinalValid = stockFinal - expiree;
        System.out.println("Stock final " + produit.getNomProduit() + " = " + stockFinalValid);
        if (stock == null) {
            stock = new StockAgregate(DataId.generate());
            stock.setCoutAchat(coutAch);
            LocalDateTime dte = datefin.equals(LocalDate.now()) ? LocalDateTime.now() : datefin.atTime(23, 59, 59);
            stock.setDate(dte);
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setFinalQuantity(stockFinalValid < 0 ? 0 : stockFinalValid);
            stock.setMesureId(unite);
            stock.setProductId(produit);
            stock.setContext("Journalier du " + LocalDate.now());
            stock.setRegion(region);
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.persist(stock);
                    return stock;
                }).thenAccept(e -> {
                    System.out.println("Stock de " + e.getProductId().getNomProduit() + " enregistree");
                });
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) {
                    tx.begin();
                }
                ManagedSessionFactory.getEntityManager().persist(stock);
                tx.commit();
                System.out.println("Stock agreagator..." + produit.getNomProduit() + " " + stockFinalValid);
            }
        } else {
            stock.setCoutAchat(coutAch);
            LocalDateTime dte = datefin.equals(LocalDate.now()) ? LocalDateTime.now() : datefin.atTime(23, 59, 58);
            stock.setDate(dte);
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setContext("Journalier du " + LocalDate.now());
            stock.setFinalQuantity(stockFinalValid < 0 ? 0 : stockFinalValid);
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(stock);
                    return stock;
                }).thenAccept(e -> {
                    System.out.println("Stock de " + e.getProductId().getNomProduit() + " modifiee");
                });
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) {
                    tx.begin();
                }
                ManagedSessionFactory.getEntityManager().merge(stock);
                tx.commit();
                System.out.println("Update Stock agreagator..." + produit.getNomProduit() + " " + stockFinalValid);
            }
        }
    }

    @Override
    public Recquisition clotureStockProduit(Produit produit, String region, LocalDate datedebut, LocalDate datefin,
            String cloture_context) {
        Recquisition dernierR = getLastEntry("lifo", produit, region);
        if (dernierR == null) {
            return null;
        }
        double E = sommeEntreeSurPeriode(produit.getUid(), datedebut, datefin, region);
        double SV = sommeSortieSurPeriode(produit.getUid(), datedebut, datefin, region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), datedebut, region);
        double expiree = getStockExpiree(produit.getUid(), datedebut, datefin, region);
        double stockFinal = stockInit + E - SV;
        double stockFinalValid = stockFinal - expiree;
        Mesure unite = MesureDelegate.findByProduitAndQuant(produit.getUid(), 1d);
        stock = findClosedStock(datedebut, datefin, produit.getUid(), region, cloture_context);
        Double qdR = dernierR.getMesureId().getQuantContenu();
        double coutAch = dernierR.getCoutAchat() / qdR;
        if (stock == null) {
            stock = new StockAgregate(DataId.generate());
            stock.setCoutAchat(coutAch);
            LocalDateTime dte = datefin.equals(LocalDate.now()) ? LocalDateTime.now() : datefin.atTime(23, 59, 59);
            stock.setDate(dte);
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setFinalQuantity(stockFinalValid < 0 ? 0 : stockFinalValid);
            stock.setMesureId(unite);
            stock.setProductId(produit);
            stock.setContext(cloture_context);
            stock.setRegion(region);
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.persist(stock);
                    return stock;
                }).thenAccept(e -> {
                    System.out.println("Stock de " + e.getProductId().getNomProduit() + " enregistree");
                });
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) {
                    tx.begin();
                }
                ManagedSessionFactory.getEntityManager().persist(stock);
                tx.commit();
                System.out.println("Stock agreagator..." + produit.getNomProduit() + " " + stockFinalValid);
            }
        } else {
            stock.setCoutAchat(coutAch);
            LocalDateTime dte = datefin.equals(LocalDate.now()) ? LocalDateTime.now() : datefin.atTime(23, 59, 58);
            stock.setDate(dte);
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setContext(cloture_context);
            stock.setFinalQuantity(stockFinalValid);
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(stock);
                    return stock;
                }).thenAccept(e -> {
                    System.out.println("Stock de " + e.getProductId().getNomProduit() + " modifiee");
                });
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) {
                    tx.begin();
                }
                ManagedSessionFactory.getEntityManager().merge(stock);
                tx.commit();
                System.out.println("Update Stock agreagator..." + produit.getNomProduit() + " " + stockFinalValid);
            }
        }
        return dernierR;
    }

    //
    private StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid, String region,
            String cloture_type) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append(
                "SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            try {
                return ManagedSessionFactory
                        .executeRead(em -> {
                            List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                                .setParameter(1, today.atStartOfDay())
                                .setParameter(2, today1.atTime(23, 59, 59))
                                .setParameter(3, region)
                                .setParameter(4, uid)
                                .getResultList();
                            return results.isEmpty() ? null : results.get(0);
                        });
            } catch (Exception e) {
                return null;
            }
        }
        query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), StockAgregate.class);
        query.setParameter(1, today.atStartOfDay());
        query.setParameter(2, today1.atTime(23, 59, 59));
        query.setParameter(3, region);
        query.setParameter(4, uid);
        List<StockAgregate> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            Query query;
            sb.append("SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.product_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                try {
                    return ManagedSessionFactory
                            .executeRead(em -> (StockAgregate) em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, today.atStartOfDay())
                            .setParameter(2, today1.atTime(23, 59, 59))
                            .setParameter(3, uid)
                            .setMaxResults(1).getSingleResult());
                } catch (NoResultException e) {
                    return null;
                }
            }
            query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), StockAgregate.class);
            query.setParameter(1, today.atStartOfDay());
            query.setParameter(2, today1.atTime(23, 59, 59));
            query.setParameter(3, uid);
            query.setMaxResults(1);
            return (StockAgregate) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    private StockAgregate findLatestStockAgregate(String productId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate WHERE product_id = ? ORDER BY date DESC LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, productId)
                            .setMaxResults(1)
                            .getResultList();
                    return results.isEmpty() ? null : results.get(0);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, productId)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private StockAgregate findLatestStockAgregate(String productId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate WHERE product_id = ? AND region LIKE ? ORDER BY date DESC LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, productId)
                            .setParameter(2, region)
                            .setMaxResults(1)
                            .getResultList();
                    return results.isEmpty() ? null : results.get(0);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, region)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM recquisition p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Recquisition> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Recquisition> result = query.getResultList();
        return !result.isEmpty();
    }

    public double sommeCompterSurPeriode(String prod, LocalDate dateDebut, LocalDate dateFin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT (SUM(COALESCE(c.quantite,0)*COALESCE(m.quantcontenu, 0))) pieces FROM compter c, mesure m"
                + " WHERE c.product_id = ? AND c.date_count BETWEEN ? AND  ? AND c.region = ? AND c.mesure_id=m.uid");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, prod);
                query.setParameter(2, dateDebut.atStartOfDay());
                query.setParameter(3, dateFin.atTime(23, 59, 59));
                query.setParameter(4, region);
                Double r = (Double) query.getSingleResult();
                return r == null ? 0 : r;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
        query.setParameter(1, prod);
        query.setParameter(2, dateDebut.atStartOfDay());
        query.setParameter(3, dateFin.atTime(23, 59, 59));
        query.setParameter(4, region);
        Double r = (Double) query.getSingleResult();
        return r == null ? 0 : r;
    }

    public Mesure findMinMesureForProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY m.quantcontenu ASC LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, uid);
                    return (Mesure) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Mesure> findMinMesureForProduits(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY m.quantcontenu ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public PrixDeVente getExistingPricefor(Recquisition r, List<Mesure> mesures) {
        for (Mesure mesure : mesures) {
            List<PrixDeVente> prices = findPricesFor(r.getUid(), mesure.getUid());
            if (prices.isEmpty()) {
                prices = findPricesFor(r.getUid());
                if (prices.isEmpty()) {
                    continue;
                }
            }
            return prices.get(0);
        }
        return null;
    }

    public Vente findAjuVente(LocalDate begInv, LocalDate closeInv, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM vente m WHERE m.dateVente BETWEEN ? AND ? AND m.observation = ? AND m.region = ? LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, closeInv.atStartOfDay());
                    query.setParameter(2, closeInv.atTime(23, 59, 59));
                    query.setParameter(3, "Ajustement Inventaire");
                    query.setParameter(4, region);
                    return (Vente) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, closeInv.atStartOfDay());
            query.setParameter(2, closeInv.atTime(23, 59, 59));
            query.setParameter(3, "Ajustement Inventaire");
            query.setParameter(4, region);
            return (Vente) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Client getAnonymousClient() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Client.class);
                    query.setParameter(1, "Unknown").setParameter(2, "Anonyme")
                            .setParameter(3, "09000").setMaxResults(1);
                    Client anonymous = (Client) query.getSingleResult();
                    return anonymous;
                });
            }

            Query query = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), Client.class);
            query.setParameter(1, "Unknown")
                    .setParameter(2, "Anonyme")
                    .setParameter(3, "09000").setMaxResults(1);
            Client anonymous = (Client) query.getSingleResult();
            return anonymous;
        } catch (NoResultException e) {
            return createAnonymousIfNotExist();
        }
    }

    private Client createAnonymousIfNotExist() {
        Client c = new Client(DataId.generate());
        c.setAdresse("Unknown");
        c.setEmail("Unknown");
        c.setNomClient("Anonyme");
        c.setPhone("09000");
        c.setTypeClient("Consommateur");
        c.setParentId(c);
        Client created = createClient(c);
        return created;
    }

    public Vente searchVenteAjustement(LocalDate debutInv, LocalDate finInv, String region) {
        Vente vente = findAjuVente(debutInv, finInv, region);
        if (vente == null) {
            int ref = (int) (Math.random() * 1094061);
            Vente aju = new Vente(ref);
            aju.setReference("COR" + ref);
            aju.setDateVente(finInv.atTime(23, 59, 59));
            aju.setClientId(getAnonymousClient());
            aju.setPayment(Constants.PAYEMENT_CREDIT);
            aju.setLatitude(0d);
            aju.setLibelle("Ajustement Iinventaire");
            aju.setLongitude(0d);
            aju.setMontantCdf(0);
            aju.setMontantDette(0d);
            aju.setMontantUsd(0d);
            aju.setObservation("Ajustement Inventaire");
            aju.setRegion(region);
            aju.setDeviseDette("USD");
            vente = createVente(aju);
        }
        return vente;
    }

    // Adjust stocks smartly
    @Override
    public void adjustAfterInventory(Inventaire inventaire, String region) {
        List<Compter> comptages = findComptages(inventaire.getUid());
        LocalDate debutInv = inventaire.getDateDebut();
        LocalDate finInv = inventaire.getDateFin();
        Vente aju = searchVenteAjustement(debutInv, finInv, region);
        for (Compter com : comptages) {
            Produit produit = com.getProductId();
            Compter co = findCompteForProduit(produit.getUid(), debutInv, finInv, region);
            if (co == null) {
                continue;
            }
            double sommeEntreeSurPeriode = sommeEntreeSurPeriode(produit.getUid(), debutInv, finInv, region);
            double sommeSortieSurPeriode = sommeSortieSurPeriode(produit.getUid(), debutInv, finInv, region);
            double sommeCompterSurPeriode = sommeCompterSurPeriode(produit.getUid(), debutInv, finInv, region);
            StockAgregate stock = findClosedStock(finInv, finInv, produit.getUid(), region, "Journalier du " + finInv);
            double stokTheo_j = (stock == null) ? 0 : stock.getFinalQuantity();
            double stockJuste = ((sommeEntreeSurPeriode - sommeSortieSurPeriode) + sommeCompterSurPeriode)
                    - ((sommeCompterSurPeriode == 0 || sommeEntreeSurPeriode == 0) ? 0 : stokTheo_j);
            Mesure mez = findMinMesureForProduit(produit.getUid());
            System.out.println("Stock juste pour " + produit.getNomProduit() + " est : " + stockJuste + " "
                    + mez.getDescription());
            if (stockJuste > 0) {
                double quantTosave = Math.abs(stockJuste);
                // requisition
                Recquisition r = new Recquisition(DataId.generate());
                r.setProductId(produit);
                r.setMesureId(mez);
                r.setQuantite(quantTosave);
                r.setCoutAchat(co.getCoutAchat());
                r.setDateExpiry(co.getDateExpiration());
                r.setNumlot(co.getNumlot());
                r.setDate(LocalDateTime.now());
                r.setUpdatedAt(LocalDateTime.now());
                r.setObservation("Ajustement inventaire");
                r.setReference(aju.getReference());
                r.setRegion(region);
                r.setStockAlert(1d);
                createRecquisition(r);
            } else if (stockJuste < 0) {
                double quantToSave = Math.abs(stockJuste);
                // lignvente
                LigneVente lv = new LigneVente(DataId.generateLong());
                lv.setProductId(produit);
                lv.setMesureId(mez);
                lv.setQuantite(quantToSave);
                lv.setNumlot(co.getNumlot());
                lv.setPrixUnit(0d);
                lv.setReference(aju);
                lv.setCoutAchat(co.getCoutAchat());
                lv.setMontantCdf(0d);
                lv.setMontantUsd(0d);
                lv.setClientId("Ajustement Inventaire");
                createLigneVente(lv);

            }
            // la cloture du stock le jour meme de cloture d'inventaire
            clotureStockProduit(produit, region, finInv, finInv, "Journalier du " + finInv);
        }
        List<LigneVente> lvs = findLvByReference(aju.getUid());
        if (lvs.isEmpty()) {
            removeVente(aju);
        }
    }

    public List<LigneVente> findLvByReference(Integer uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente WHERE reference_uid = ? AND deleted_at IS NULL");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, uid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public List<Compter> findComptages(String inventaireId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.inventaire_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, inventaireId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, inventaireId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void removeVente(Vente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getReference() + " enregistree");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public double findCurrentStockFor(Produit produit, String region) {
        LocalDate leo = LocalDate.now();
        StockAgregate aggreg = findClosedStock(leo, leo, produit.getUid(), region, "Journalier du " + leo);
        if (aggreg == null) {
            Recquisition dernierR = getLastEntry(produit.getUid());
            if (dernierR == null) {
                return 0;
            }
            aggreg = saveStockFromRecquisition(dernierR);
        }
        return aggreg.getFinalQuantity();
    }

    private StockAgregate findStock(String puid, LocalDate today, LocalDate today1, String region) {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(
                    "SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                    query.setParameter(1, today.atStartOfDay());
                    query.setParameter(2, today1.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    query.setParameter(4, puid);
                    StockAgregate result = (StockAgregate) query.setMaxResults(1).getSingleResult();
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
                    StockAgregate.class);
            query.setParameter(1, today.atStartOfDay());
            query.setParameter(2, today1.atTime(23, 59, 59));
            query.setParameter(3, region);
            query.setParameter(4, puid);
            StockAgregate result = (StockAgregate) query.setMaxResults(1).getSingleResult();
            return result;
        } catch (NoResultException e) {
            return null;
        }
    }

    // expireds
    /**
     *
     * @param uid
     * @param datedebut
     * @param datefin
     * @param region
     * @return
     */
    private List<ExpiredItem> entreeExpiree(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        List<ExpiredItem> result = new ArrayList<>();
        StringBuilder sbEex = new StringBuilder();
        sbEex.append(
                "SELECT r.product_id,r.numlot,r.dateExpiry,SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) quantite,"
                + " r.mesure_id,r.coutAchat,r.region FROM recquisition r,mesure m "
                + "WHERE r.product_id = ? AND r.dateexpiry BETWEEN ? AND ? AND r.region LIKE ? AND r.mesure_id=m.uid GROUP BY numlot");

        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                List<Object[]> objs = em.createNativeQuery(sbEex.toString()).setParameter(1, uid)
                        .setParameter(2, datedebut)
                        .setParameter(3, datefin)
                        .setParameter(4, region)
                        .getResultList();
                for (Object[] obj : objs) {
                    Mesure mesure = findMesure(String.valueOf(obj[4]));
                    ExpiredItem e = new ExpiredItem(String.valueOf(obj[0]),
                            String.valueOf(obj[1]),
                            LocalDate.parse(String.valueOf(obj[2])),
                            Double.parseDouble(String.valueOf(obj[3])),
                            mesure, Double.parseDouble(String.valueOf(obj[5])),
                            String.valueOf(obj[6]));
                    result.add(e);
                }
                return result;
            });
        }
        List<Object[]> objs = ManagedSessionFactory.getEntityManager().createNativeQuery(sbEex.toString())
                .setParameter(1, uid)
                .setParameter(2, datedebut)
                .setParameter(3, datefin)
                .setParameter(4, region)
                .getResultList();
        for (Object[] obj : objs) {
            Mesure mesure = findMesure(String.valueOf(obj[4]));
            ExpiredItem e = new ExpiredItem(String.valueOf(obj[0]),
                    String.valueOf(obj[1]),
                    LocalDate.parse(String.valueOf(obj[2])),
                    Double.parseDouble(String.valueOf(obj[3])),
                    mesure, Double.parseDouble(String.valueOf(obj[5])),
                    String.valueOf(obj[6]));
            result.add(e);
        }
        return result;
    }

    private List<ExpiredItem> sortieExpiree(String uid, String numlot, LocalDate datedebut, LocalDate datefin,
            String region) {
        List<ExpiredItem> result = new ArrayList<>();
        StringBuilder sbex = new StringBuilder();
        sbex.append(
                "SELECT s.product_id,s.numlot, SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces, s.mesure_id,"
                + " s.coutAchat FROM ligne_vente s, mesure m WHERE s.product_id = ? AND s.mesure_id=m.uid AND s.numlot = ?"
                + " AND s.reference_uid IN"
                + " (SELECT v.uid FROM vente v WHERE v.region LIKE ? AND v.dateVente BETWEEN ? AND ?) GROUP BY s.numlot");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                List<Object[]> objs = em.createNativeQuery(sbex.toString())
                        .setParameter(1, uid)
                        .setParameter(2, numlot)
                        .setParameter(3, region)
                        .setParameter(4, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(5, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .getResultList();
                for (Object[] obj : objs) {
                    Mesure ob = (Mesure) obj[3];
                    System.out.println("Convenrsion obj en mesure " + ob.getUid());
                    Mesure mesure = findMesure(ob.getUid());
                    ExpiredItem e = new ExpiredItem(String.valueOf(obj[0]),
                            String.valueOf(obj[1]), datefin,
                            Double.parseDouble(String.valueOf(obj[2])),
                            mesure, Double.parseDouble(String.valueOf(obj[5])),
                            String.valueOf(obj[6]));
                    result.add(e);
                }
                return result;
            });
        }
        List<Object[]> objs = ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sbex.toString())
                .setParameter(1, uid)
                .setParameter(2, numlot)
                .setParameter(3, region)
                .setParameter(4, Timestamp.valueOf(datedebut.atStartOfDay()))
                .setParameter(5, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                .getResultList();
        for (Object[] obj : objs) {
            Mesure ob = (Mesure) obj[3];
            System.out.println("Convenrsion obj en mesure " + ob.getUid());
            Mesure mesure = findMesure(ob.getUid());
            ExpiredItem e = new ExpiredItem(String.valueOf(obj[0]),
                    String.valueOf(obj[1]), datefin,
                    Double.parseDouble(String.valueOf(obj[2])),
                    mesure, Double.parseDouble(String.valueOf(obj[5])),
                    String.valueOf(obj[6]));
            result.add(e);
        }
        return result;
    }

    private String getLocation(String idpro) {
        List<Stocker> loc = StockerDelegate.findDescSortedByDateStock(idpro);
        if (loc.isEmpty()) {
            return null;
        }
        return loc.get(0).getLocalisation();
    }

    @Override
    public List<Peremption> showExpiredAtInterval(LocalDate dateExp1, LocalDate dateEpx2, String region) {
        List<Peremption> result = new ArrayList<>();
        List<Produit> produits = getProduits();
        for (Produit produit : produits) {
            String localisation = getLocation(produit.getUid());
            List<ExpiredItem> expins = entreeExpiree(produit.getUid(), dateExp1, dateEpx2, region);
            for (ExpiredItem expin : expins) {
                List<ExpiredItem> sorties = sortieExpiree(produit.getUid(), expin.numlot(), dateExp1, dateEpx2, region);
                double reste = expin.quantite();
                if (!sorties.isEmpty()) {
                    ExpiredItem ei = sorties.getFirst();
                    reste = reste - ei.quantite();
                }
                Peremption per = new Peremption();
                per.setCodebar(produit.getCodebar());
                per.setCoutAchat(expin.coutAchat());
                per.setDateExpiry(expin.dateExpire());
                per.setLot(expin.numlot());
                per.setMesure(expin.mesure().getDescription());
                per.setProduit(produit.getNomProduit() + " " + produit.getModele() + " " + produit.getTaille());
                per.setProduitUid(produit.getUid());
                per.setLocalisation(localisation == null ? region : localisation);
                per.setQuantite(reste);
                per.setRegion(expin.region());
                per.setValeur(BigDecimal.valueOf(reste * expin.coutAchat()).setScale(2, RoundingMode.HALF_EVEN)
                        .doubleValue());
                result.add(per);
            }
        }
        return result;
    }

    private record ExpiredItem(String uidProduit, String numlot, LocalDate dateExpire, double quantite,
            Mesure mesure, double coutAchat, String region) {

    }

}
