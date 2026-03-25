/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import IServices.CompterStorage;
import data.Client;
import data.Compter;
import data.LigneVente;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Vente;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import data.Inventaire;
import data.StockAgregate;
import delegates.MesureDelegate;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import tools.Agregator;
import tools.Constants;
import tools.DataId;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class CompterService implements CompterStorage {

    Preferences pref;
    String devise;
    double taux;

    @Override
    public boolean isExists(String uid) {
        String jpql = "SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END "
                + "FROM Compter c WHERE c.uid = :id";
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

    public CompterService() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux = pref.getDouble("taux2change", 2000);
        devise = pref.get("mainCur", "USD");
    }

    @Override
    public Compter createCompter(Compter cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                editStock(cat);
                System.out.println("Element " + e.getProductId().getNomProduit() + " comptee");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        tx.commit();
        editStock(cat);
        return cat;
    }

    @Override
    public Compter updateCompter(Compter cat) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).thenAccept(e -> {
                    editStock(cat);
                    System.out.println("Element " + e.getProductId().getNomProduit() + " recomptee");
                });
                return cat;
            }
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().merge(cat);
            tx.commit();
            editStock(cat);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            System.err.println("Erreur Message : " + e.getMessage());
        }
        return cat;
    }

    @Override
    public void deleteCompter(Compter obj) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(obj));
                return obj;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getProductId().getNomProduit() + " comptee supprimee");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(obj));
        etr.commit();
    }

    @Override
    public Compter findCompter(String objId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Compter.class, objId));
        }
        return ManagedSessionFactory.getEntityManager().find(Compter.class, objId);
    }

    @Override
    public List<Compter> findCompters() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Compter.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Compter.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Produit> findProducts() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Produit.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Produit.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM compter");
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
    public List<Compter> findCompters(LocalDate dateDebut, LocalDate dateFin) {
        try {
            if (dateDebut == null || dateFin == null) {
                return Collections.emptyList();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter m WHERE m.date_count BETWEEN ? AND  ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, dateDebut.atStartOfDay());
                    query.setParameter(2, dateFin.atTime(23, 59, 59));
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, dateDebut.atStartOfDay());
            query.setParameter(2, dateFin.atTime(23, 59, 59));
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findCompters(LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            if (dateDebut == null || dateFin == null) {
                return Collections.emptyList();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter m WHERE m.date_count BETWEEN ? AND  ? AND m.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, dateDebut.atStartOfDay());
                    query.setParameter(2, dateFin.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, dateDebut.atStartOfDay());
            query.setParameter(2, dateFin.atTime(23, 59, 59));
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Mesure findMesure(String objId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Mesure.class, objId));
        }
        return ManagedSessionFactory.getEntityManager().find(Mesure.class, objId);
    }

    public List<Compter> findCompteForProduit(String puid, LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            if (dateDebut == null || dateFin == null) {
                return Collections.emptyList();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.product_id =  ? AND p.date_count BETWEEN ? AND  ? AND p.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, dateDebut);
                    query.setParameter(3, dateFin);
                    query.setParameter(4, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, puid);
            query.setParameter(2, dateDebut);
            query.setParameter(3, dateFin);
            query.setParameter(4, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
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

    @Override
    public List<Compter> findComptageForProduit(String puid, String inventaireId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ?  ORDER BY p.updated_at ASC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, inventaireId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, puid);
            query.setParameter(2, inventaireId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptageForProduit(String puid, String inventaireId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ? AND p.numlot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, inventaireId);
                    query.setParameter(3, lot);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, puid);
            query.setParameter(2, inventaireId);
            query.setParameter(3, lot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptageForProduit(String puid, String inventaireId, String lot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ? AND p.numlot = ? AND p.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, inventaireId);
                    query.setParameter(3, lot);
                    query.setParameter(4, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, puid);
            query.setParameter(2, inventaireId);
            query.setParameter(3, lot);
            query.setParameter(4, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptages(String inventaireId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.inventaire_id = ? AND p.region = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, inventaireId);
                    query.setParameter(2, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, inventaireId);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Compter> findUnSyncedCompter(long disconnected_at) {
        try {
            Timestamp offline = new Timestamp(disconnected_at);
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.updated_at >= ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, offline);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, offline);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isExists(String uid, LocalDateTime atime) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM compter p WHERE p.uid = ? AND p.updated_at = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, uid);
                query.setParameter(2, atime);
                List<Compter> result = query.getResultList();
                return !result.isEmpty();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
        query.setParameter(1, uid);
        query.setParameter(2, atime);
        List<Compter> result = query.getResultList();
        return !result.isEmpty();
    }

    public double sommeComptage(Produit p, Inventaire inv) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(c.quantite,0)*COALESCE(m.quantcontenu,0)) s FROM compter c,mesure m WHERE c.product_id = ? AND c.inventaire_id = ? AND c.mesure_id=m.uid");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, p.getUid());
                query.setParameter(2, inv.getUid());
                Object result = query.getSingleResult();
                return result == null ? 0 : ((Double) result);
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter(1, p.getUid());
        query.setParameter(2, inv.getUid());
        Object result = query.getSingleResult();
        return result == null ? 0 : ((Double) result);
    }

    @Override
    public Compter findComptageByInventaireProduit(String iuid, String puid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ? ORDER BY p.updated_at DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Compter.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, iuid);
                    query.setMaxResults(1);
                    List<Compter> lc = query.getResultList();
                    return lc.isEmpty() ? null : lc.getFirst();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Compter.class);
            query.setParameter(1, puid);
            query.setParameter(2, iuid);
            query.setMaxResults(1);
            List<Compter> lc = query.getResultList();
            return lc.isEmpty() ? null : lc.getFirst();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void editStock(Compter compter) {
        Produit prod = compter.getProductId();
        String region = compter.getRegion();
        Inventaire inv = compter.getInventaireId();
        Mesure mx = compter.getMesureId();
        System.out.println("avant somme comptage");
        double stockReel = sommeComptage(prod, inv);
        System.out.println("apres somme comptage " + stockReel);
        double stockTheorik = findOnlyStockUnits(prod, region, inv.getDateDebut(), inv.getDateFin());
        System.out.println("zzzzzzzz " + stockTheorik);
        double difference = stockReel - stockTheorik;
        Double quantQ = findMesure(mx.getUid()).getQuantContenu();
        double coutUnit = compter.getCoutAchat() / ((quantQ == null || quantQ == 0) ? 1 : quantQ);
        Mesure mes = findUnitForProduit(prod.getUid(), 1);
        int existing = DataId.from(inv.getDateDebut());
        System.out.println("Difference " + difference);
        if (difference < 0) {
            //vente
            Vente found = findOrCreateVenteAj(inv.getCodeInventaire(), region);
            LigneVente lv = new LigneVente(DataId.generateLong());
            lv.setCoutAchat(0d);
            lv.setMesureId(mes);
            lv.setClientId("#INV");
            lv.setMontantCdf(0);
            lv.setMontantUsd(0);
            lv.setNumlot(compter.getNumlot());
            lv.setPrixUnit(0d);
            lv.setProductId(prod);
            lv.setQuantite(Math.abs(difference));
            lv.setReference(found);
            createLigneVente(inv, lv);
            System.out.println("Creation Vente ajust avec " + difference);
        } else if (difference > 0) {
            Recquisition last = getLastEntry(prod.getUid());
            List<PrixDeVente> prices = findPricesForRecq(last.getUid());
            Recquisition rq = new Recquisition(DataId.generate());
            rq.setCoutAchat(coutUnit);
            rq.setDate(LocalDateTime.now());
            rq.setDateExpiry(compter.getDateExpiration());
            rq.setMesureId(mes);
            rq.setNumlot(compter.getNumlot());
            rq.setObservation("Ajustement Inventaire");
            rq.setProductId(prod);
            rq.setQuantite(difference);
            rq.setReference("#INV-" + existing);
            rq.setRegion(region);
            rq.setStockAlert(1d);
            Recquisition crq = createRecquisition(inv, rq);
            for (PrixDeVente price : prices) {
                PrixDeVente pv = new PrixDeVente(DataId.generate());
                pv.setRecquisitionId(crq);
                pv.setDevise(price.getDevise());
                pv.setMesureId(price.getMesureId());
                pv.setPourcentParCunit(0d);
                pv.setPrixUnitaire(price.getPrixUnitaire());
                pv.setQmax(price.getQmax());
                pv.setQmin(price.getQmin());
                createPrixDeVente(pv);
            }
            //recquisition
        }

    }

    public void removeNoCountedProducts(Inventaire inv) {
        List<Produit> lspr = findProducts();
        for (Produit produit : lspr) {
            Compter cpte = findComptageByInventaireProduit(inv.getUid(), produit.getUid());
            if (cpte == null) {
                StockAgregate sa = delegates.RepportDelegate.findCurrentStock(produit, inv.getRegion(), LocalDate.now(), LocalDate.now());
                if (sa == null) {
                    continue;
                }
                double theorik = (sa.getFinalQuantity() == null) ? 0.0 : sa.getFinalQuantity();
                Mesure mes = sa.getMesureId();
                Vente found = findOrCreateVenteAj(inv.getCodeInventaire(), inv.getRegion());
                LigneVente lv = new LigneVente(DataId.generateLong());
                lv.setCoutAchat(0d);
                lv.setMesureId(mes);
                lv.setClientId("#INV");
                lv.setMontantCdf(0);
                lv.setMontantUsd(0);
                lv.setNumlot("0AJI0");
                lv.setPrixUnit(0d);
                lv.setProductId(produit);
                lv.setQuantite(theorik);
                lv.setReference(found);
                createLigneVente(inv, lv);
            }
        }
    }
    StockAgregate stock;

    private void rectifyStock(Inventaire inv, Produit produit, LocalDate datedebut, LocalDate datefin, String region, double coutAch) {
        if (datedebut == null || datefin == null) {
            return;
        }
        Mesure unite = MesureDelegate.findByProduitAndQuant(produit.getUid(), 1d);
        stock = findClosedStock(datedebut, datefin, produit.getUid());
        double E = sommeEntreeSurPeriode(produit.getUid(), inv.getDateDebut(), inv.getDateFin(), region);
        double SV = sommeSortieSurPeriode(produit.getUid(), inv.getDateDebut(), inv.getDateFin(), region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), inv.getDateDebut(), region);
        double expiree = getStockExpiree(produit.getUid(), inv.getDateDebut(), inv.getDateFin(), region);
        double stockFinal = stockInit + E - SV;
        System.out.println("SI = " + stockInit + " Entr = " + E + " SV = " + SV);
        double stockFinalValid = stockFinal - expiree;
        System.out.println("Stock final " + produit.getNomProduit() + " = " + stockFinalValid);
        if (stock == null) {
            System.out.println("nouveau stock " + stockFinalValid);
            stock = new StockAgregate(DataId.generate());
            stock.setCoutAchat(coutAch);
            stock.setDate(LocalDateTime.now());
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
                    System.out.println("Stock de " + e.getProductId().getNomProduit() + " ajustee");
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
            System.out.println("modif stock " + stockFinalValid);
            stock.setCoutAchat(coutAch);
            stock.setDate(LocalDateTime.now());
            stock.setEntrees(E);
            stock.setSorties(SV);
            stock.setInitialQuantity(stockInit);
            stock.setExpiree(expiree);
            stock.setRegion(region);
            stock.setContext("Journalier du " + LocalDate.now());
            stock.setFinalQuantity(stockFinalValid);
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(stock);
                    return stock;
                }).thenAccept(e -> {
                    System.out.println("Stock de " + e.getProductId().getNomProduit() + " modifiee et ajustee");
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

    public double findOnlyStockUnits(Produit produit, String region, LocalDate datedebut, LocalDate datefin) {
        double E = sommeEntreeSurPeriode(produit.getUid(), datedebut, datefin, region);
        System.out.println("Stockonly E = " + E);
        double SV = sommeSortieSurPeriode(produit.getUid(), datedebut, datefin, region);
        System.out.println("Stockonly SV = " + SV);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), datedebut, region);
        System.out.println("Stockonly SI = " + stockInit);
        double expiree = getStockExpiree(produit.getUid(), datedebut, datefin, region);
        double stockFinal = stockInit + E - SV;
        System.out.println("Stockonly SF = " + stockInit + "+" + E + "-" + SV + "=" + stockFinal);
        double stockFinalValid = stockFinal - expiree;
        return stockFinalValid;
    }

    public Vente findAjuVente(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente m WHERE m.reference = ? AND m.observation = ? LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, ref);
                    query.setParameter(2, "Ajustement Inventaire");
                    return (Vente) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, ref);
            query.setParameter(2, "Ajustement Inventaire");
            return (Vente) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Vente findOrCreateVenteAj(String codeInventaire, String region) {
        Vente vente = findAjuVente(codeInventaire);
        if (vente == null) {
            System.out.println("Vente aju not exist " + codeInventaire);
            int id = (int) (Math.random() * 1094061);
            Vente aju = new Vente(id);
            aju.setReference(codeInventaire);
            aju.setDateVente(LocalDateTime.now());
            aju.setClientId(getAnonymousClient());
            aju.setPayment(Constants.PAYEMENT_CREDIT);
            aju.setLatitude(0d);
            aju.setLibelle("Ajustement Inventaire");
            aju.setLongitude(0d);
            aju.setMontantCdf(0);
            aju.setMontantDette(0d);
            aju.setMontantUsd(0d);
            aju.setObservation("Ajustement Inventaire");
            aju.setRegion(region);
            aju.setDeviseDette(devise);
            vente = createVente(aju);
        }
        return vente;
    }

    public double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        if (datedebut == null || datefin == null) {
            return 0;
        }
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
                Double.class
        ).setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region).getSingleResult();
        return entrees == null ? 0 : entrees;
    }

    public double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        if (datedebut == null || datefin == null) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ? AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double sorties = (Double) em.createNativeQuery(sb.toString(), Double.class)
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region)
                        .getSingleResult();
                return sorties == null ? 0 : sorties;
            });
        }
        Double sorties = (Double) (ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class)
                .setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region)
                .getSingleResult());
        return sorties == null ? 0 : sorties;
    }

    public double calculerStockInitialEnUnite(String uid, LocalDate datedebut, String region) {
        if (datedebut == null) {
            return 0;
        }
        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                + "WHERE r.product_id = ? AND r.date < ? AND r.region LIKE ? AND r.mesure_id=m.uid");
        StringBuilder sbS = new StringBuilder();
        sbS.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ? AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE ? AND v.dateVente < ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) em.createNativeQuery(sbE.toString(),
                        Double.class
                ).setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, region)
                        .getSingleResult();
                Double sorties = (Double) em.createNativeQuery(sbS.toString(), Double.class)
                        .setParameter(1, uid).setParameter(2, region)
                        .setParameter(3, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .getSingleResult();
                double stok = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
                return stok <= 0 ? 0 : stok;
            });
        }
        Double entrees = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbE.toString(),
                Double.class
        ).setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, region)
                .getSingleResult();

        Double sorties = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbS.toString(),
                Double.class
        ).setParameter(1, uid).setParameter(2, region)
                .setParameter(3, datedebut.atStartOfDay())
                .getSingleResult();
        double stok = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
        return stok <= 0 ? 0 : stok;
    }

    public double getStockExpiree(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        if (datedebut == null || datefin == null) {
            return 0;
        }
        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                + "WHERE r.product_id = ?1 AND r.dateexpiry BETWEEN ?2 AND ?3"
                + " AND r.region LIKE ?4 AND r.mesure_id=m.uid");
        StringBuilder sbRq = new StringBuilder();
        sbRq.append("SELECT * FROM recquisition r "
                + "WHERE r.product_id = ?1 AND r.dateexpiry BETWEEN ?2 AND ?3 AND r.region LIKE ?4");
        StringBuilder sbS = new StringBuilder();
        sbS.append("SELECT (SUM(COALESCE(s.quantite,0)*COALESCE(m.quantcontenu, 0))) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ?1 AND s.mesure_id=m.uid AND s.numlot = ?4 AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE ?5 AND v.dateVente BETWEEN ?2 AND ?3)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                double sorties_exp0 = 0;
                Double entrees_exp = (Double) em.createNativeQuery(sbE.toString(), Double.class)
                        .setParameter(1, uid)
                        .setParameter(2, datedebut)
                        .setParameter(3, datefin)
                        .setParameter(4, region)
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
        Double entrees_exp = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbE.toString(), Double.class)
                .setParameter(1, uid)
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region).setParameter(2, datedebut.atStartOfDay())
                .getSingleResult();
        List<Recquisition> experqs = ManagedSessionFactory.getEntityManager().createNativeQuery(sbRq.toString(), Recquisition.class)
                .setParameter(1, uid).setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region)
                .getResultList();
        for (Recquisition experq : experqs) {
            Double stx = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbS.toString(), Double.class)
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

    public PrixDeVente createPrixDeVente(PrixDeVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element prix " + e.getPrixUnitaire() + " enregistree");
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

    public List<PrixDeVente> findPricesForRecq(String ruid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                    query.setParameter(1, ruid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, ruid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Recquisition createRecquisition(Inventaire inv, Recquisition cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                rectifyStock(inv, cat.getProductId(), LocalDate.now(), LocalDate.now(), cat.getRegion(), cat.getCoutAchat());
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
        rectifyStock(inv, cat.getProductId(), LocalDate.now(), LocalDate.now(), cat.getRegion(), cat.getCoutAchat());
        return cat;
    }

    public LigneVente createLigneVente(Inventaire inv, LigneVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                rectifyStock(inv, cat.getProductId(), LocalDate.now(), LocalDate.now(), inv.getRegion(), cat.getCoutAchat());
                System.out.println("Element " + e.getProductId().getNomProduit() + " vendu");
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            tx.commit();
            rectifyStock(inv, cat.getProductId(), LocalDate.now(), LocalDate.now(), inv.getRegion(), cat.getCoutAchat());
            return cat;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
        }
        return null;
    }

    private Vente createVente(Vente cat) {
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
        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().persist(cat);
            tx.commit();
            return cat;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
        }
        return null;
    }

    public Client getAnonymousClient() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client c WHERE c.adresse = ? AND c.nom_client = ? AND c.phone = ? ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Client.class);
                query.setParameter(1, "Unknown")
                        .setParameter(2, "Anonyme")
                        .setParameter(3, "09000");
                List<Client> anonymous = query.getResultList();
                if (anonymous.isEmpty()) {
                    Client c = new Client(DataId.generate());
                    c.setAdresse("Unknown");
                    c.setEmail("Unknown");
                    c.setNomClient("Anonyme");
                    c.setPhone("09000");
                    c.setTypeClient("Consommateur");
                    c.setParentId(c);
                    Client created = createClient(c);
                    return created;
                } else {
                    return anonymous.get(0);
                }
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Client.class);
        query.setParameter(1, "Unknown")
                .setParameter(2, "Anonyme")
                .setParameter(3, "09000");
        List<Client> anonymous = query.getResultList();
        if (anonymous.isEmpty()) {
            Client c = new Client(DataId.generate());
            c.setAdresse("Unknown");
            c.setEmail("Unknown");
            c.setNomClient("Anonyme");
            c.setPhone("09000");
            c.setTypeClient("Consommateur");
            c.setParentId(c);
            Client created = createClient(c);
            return created;
        } else {
            return anonymous.get(0);
        }
    }

    public Client createClient(Client cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("Element " + e.getNomClient() + " enregistree");
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
            if (isUniqueConstraintViolation(e)) {
                etr.rollback();
                return cat;
            }
        }
        return null;
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    public Mesure findUnitForProduit(String uid, double quantM) {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.quantcontenu = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> (Mesure) em.createNativeQuery(sb.toString(), Mesure.class)
                        .setParameter(1, uid)
                        .setParameter(2, quantM)
                        .setMaxResults(1).getSingleResult());
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            query.setParameter(2, quantM).setMaxResults(1);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            Query query;
            sb.append("SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.product_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                try {
                    return ManagedSessionFactory.executeRead(em -> (StockAgregate) em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, Timestamp.valueOf(today.atStartOfDay()))
                            .setParameter(2, Timestamp.valueOf(today1.atTime(23, 59, 59)))
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
//
//    public Vente findVenteByRef(String ref) {
//        try {
//
//            StringBuilder sb = new StringBuilder();
//            sb.append("SELECT * FROM vente v WHERE v.reference = ? ");
//            if (ManagedSessionFactory.isEmbedded()) {
//                return ManagedSessionFactory.executeRead(em -> (Vente) (em.createNativeQuery(sb.toString(), Vente.class)
//                        .setParameter(1, ref).getSingleResult()));
//            }
//            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
//            query.setParameter(1, ref);
//            return (Vente) query.getSingleResult();
//        } catch (NoResultException e) {
//            return null;
//        }


////    }
////
//    public List<LigneVente> findByReference(Integer uid, String pro) {
//        try {
//            StringBuilder sb = new StringBuilder();
//            sb.append("SELECT * FROM ligne_vente WHERE reference_uid = ? AND product_id = ? ");
//            if (ManagedSessionFactory.isEmbedded()) {
//                return ManagedSessionFactory.executeRead(em -> {
//                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
//                    query.setParameter(1, uid).setParameter(2, pro);
//                    return query.getResultList();
//                });
//            }
//            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
//            query.setParameter(1, uid);
//            return query.getResultList();
//        } catch (EntityNotFoundException e) {
//            return null;
//        }
//    }
}
