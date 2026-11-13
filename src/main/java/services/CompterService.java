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
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import data.Inventaire;
import data.StockAgregate;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import delegates.RepportDelegate;
import delegates.RecquisitionDelegate;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import tools.Agregator;
import tools.ComptageItem;
import tools.Constants;
import tools.DataId;
import tools.LotKeys;
import tools.SyncEngine;
import tools.SyncLogger;

/**
 *
 * @author endeleya
 */
public class CompterService implements CompterStorage {

    private static final Logger LOG = Logger.getLogger(CompterService.class.getName());

    /**
     * Sérialise les ajustements d'un même inventaire dans ce processus. Le
     * verrou est réentrant : la clôture peut créer des comptages zéro via
     * createCompter sans se bloquer elle-même. Les autres inventaires restent
     * indépendants.
     */
    private static final ConcurrentHashMap<String, ReentrantLock> INVENTORY_LOCKS =
            new ConcurrentHashMap<>();

    Preferences pref;
    String devise;
    double taux;

    static ReentrantLock inventoryLock(Inventaire inv) {
        String key = inv == null || inv.getUid() == null || inv.getUid().isBlank()
                ? "__no_inventory__"
                : inv.getUid();
        return INVENTORY_LOCKS.computeIfAbsent(key, ignored -> new ReentrantLock());
    }

    private <T> T withInventoryLock(Inventaire inv, Supplier<T> action) {
        ReentrantLock lock = inventoryLock(inv);
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    private void withInventoryLock(Inventaire inv, Runnable action) {
        withInventoryLock(inv, () -> {
            action.run();
            return null;
        });
    }

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
        taux = tools.CurrencyConverter.activeRate();
        devise = tools.CurrencyConverter.mainCurrency();
    }

    @Override
    public Compter createCompter(Compter cat) {
        validateCompter(cat);
        return withInventoryLock(cat.getInventaireId(), () -> createCompterLocked(cat));
    }

    /**
     * Un comptage physique ne peut pas porter une quantite negative, NaN ou
     * infinie, ni etre rattache a un inventaire/produit absent : ces valeurs
     * produiraient des ajustements de stock incoherents.
     */
    static void validateCompter(Compter cat) {
        if (cat == null) {
            throw new IllegalArgumentException("Comptage d'inventaire requis");
        }
        if (cat.getInventaireId() == null) {
            throw new IllegalArgumentException("Inventaire requis pour un comptage");
        }
        if (cat.getProductId() == null) {
            throw new IllegalArgumentException("Produit requis pour un comptage");
        }
        double quantite = cat.getQuantite();
        if (!Double.isFinite(quantite) || quantite < 0) {
            throw new IllegalArgumentException(
                    "Quantité physique invalide : " + quantite
                            + " (une quantité négative ou non finie est interdite)");
        }
    }

    private Compter createCompterLocked(Compter cat) {
        boolean isDeleted = cat.getDeletedAt() != null;
        if (!isDeleted) {
            // Photographie immuable : figee a la creation du comptage, elle
            // sert de reference pour tous les recalculs ulterieurs.
            if (cat.getTheoreticalSnapshot() == null) {
                cat.setTheoreticalSnapshot(theoreticalStockAtOpening(
                        cat.getProductId(), cat.getNumlot(), cat.getInventaireId(), cat.getRegion()));
            }
            if (cat.getQuantiteTheorik() == null) {
                double qc = contentFactor(cat.getMesureId());
                cat.setQuantiteTheorik(cat.getTheoreticalSnapshot() / qc);
            }
        }
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).join();
            if (!isDeleted) editStock(cat, true);
            LOG.fine(() -> "Comptage cree pour " + productLabel(cat));
            return cat;
        }
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(cat);
            return null;
        });
        if (!isDeleted) editStock(cat, true);
        return cat;
    }

    @Override
    public Compter updateCompter(Compter cat) {
        validateCompter(cat);
        return withInventoryLock(cat.getInventaireId(), () -> updateCompterLocked(cat));
    }

    private Compter updateCompterLocked(Compter cat) {
        boolean isDeleted = cat.getDeletedAt() != null;
        String uid = cat.getUid();
        if (cat.getInventaireId() == null) {
            SyncLogger.getInstance().log(
                new IllegalStateException("Compter " + uid + " has null inventaireId — skipped"),
                "CompterService.updateCompter", "compter", uid
            );
            return cat;
        }
        if (cat.getProductId() == null) {
            SyncLogger.getInstance().log(
                new IllegalStateException("Compter " + uid + " has null productId — skipped"),
                "CompterService.updateCompter", "compter", uid
            );
            return cat;
        }
        requireInventoryOpen(cat.getInventaireId());
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).join();
                if (!isDeleted) editStock(cat, false);
                LOG.fine(() -> "Comptage mis a jour pour " + productLabel(cat));
                return cat;
            }
            ManagedSessionFactory.executeWrite(em -> {
                em.merge(cat);
                return null;
            });
            if (!isDeleted) editStock(cat, false);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            LOG.log(Level.WARNING, "Comptage introuvable lors de la mise a jour", e);
        }
        return cat;
    }

    private static String productLabel(Compter compter) {
        if (compter == null || compter.getProductId() == null) {
            return "?";
        }
        String name = compter.getProductId().getNomProduit();
        return name == null || name.isBlank() ? compter.getProductId().getUid() : name;
    }

    /**
     * Un inventaire termine ne doit plus etre modifie. La creation reste
     * volontairement non gardee : la cloture cree elle-meme les comptages
     * zero des produits non comptes.
     */
    static void requireInventoryOpen(Inventaire inv) {
        if (isInventoryClosed(inv)) {
            throw new IllegalStateException(
                    "Inventaire clôturé : la modification des comptages est interdite");
        }
    }

    static boolean isInventoryClosed(Inventaire inv) {
        return inv != null && inv.getEtat() != null
                && "Terminé".equalsIgnoreCase(inv.getEtat().trim());
    }

    @Override
    public void deleteCompter(Compter obj) {
        withInventoryLock(obj.getInventaireId(), () -> deleteCompterLocked(obj));
    }

    private void deleteCompterLocked(Compter obj) {
        // Sauvegarder les champs necessaires avant suppression
        Produit prod = obj.getProductId();
        String region = obj.getRegion();
        Inventaire inv = obj.getInventaireId();
        String lot = obj.getNumlot();
        LocalDateTime dateCountLdt = obj.getDateCount();
        LocalDate dateCount = dateCountLdt != null ? dateCountLdt.toLocalDate() : LocalDate.now();
        Mesure mesure = obj.getMesureId();
        double coutAchat = obj.getCoutAchat();
        LocalDate dateExp = obj.getDateExpiration();

        // Supprimer le compter de la base
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(obj));
                return obj;
            }).join();
        } else {
            ManagedSessionFactory.executeWrite(em -> {
                em.remove(em.merge(obj));
                return null;
            });
        }
        LOG.fine(() -> "Comptage supprime pour " + (prod != null ? prod.getNomProduit() : "?"));

        // Revertir les ajustements de stock generes par ce comptage
        revertCompterAdjustments(prod, lot, region, inv, dateCount, mesure, coutAchat, dateExp);
    }

    private void revertCompterAdjustments(Produit prod, String lot, String region, Inventaire inv,
            LocalDate dateCount, Mesure mesure, double coutAchat, LocalDate dateExp) {
        if (prod == null || inv == null) return;

        // 1. Purger les anciens ajustements #INV pour ce produit/lot
        String refJour = inv.getCodeInventaire() + "-" + dateCount.toString();
        purgeAjustementLigneVente(refJour, prod.getUid(), lot, region);
        purgeAjustementRecquisition("#INV-" + inv.getCodeInventaire(), prod.getUid(), lot, region);

        // 2. Verifier s'il reste d'autres comptages pour ce produit+lot+inventaire
        double remainingReel = sommeComptage(prod, lot, inv);

        if (remainingReel > 0) {
            // 3. Recalculer les ajustements avec les lignes restantes, sur la
            // photographie immuable prise a l'ouverture de l'inventaire.
            Double snapshot = snapshotOfRemaining(prod, lot, inv, region);
            double stockTheorik = snapshot != null
                    ? snapshot
                    : theoreticalStockAtOpening(prod, lot, inv, region);
            double difference = remainingReel - stockTheorik;

            // Mettre a jour les metadonnees des comptages restants
            double qc = contentFactor(mesure);
            double ecartEnMesure = difference / qc;
            double thEnMesure = stockTheorik / qc;
            updateCompterMeta(prod.getUid(), inv.getUid(), lot, region, ecartEnMesure, thEnMesure);

            Mesure mesUnit = findUnitForProduit(prod.getUid(), 1.0);
            if (mesUnit == null) mesUnit = mesure;

            if (difference < 0) {
                Vente found = findOrCreateVenteAj(inv.getCodeInventaire(), region);
                LigneVente lv = new LigneVente(DataId.generateLong());
                lv.setCoutAchat(0d);
                lv.setMesureId(mesUnit);
                lv.setClientId("#INV");
                lv.setMontantCdf(0);
                lv.setMontantUsd(0);
                lv.setNumlot(lot);
                lv.setPrixUnit(0d);
                lv.setProductId(prod);
                lv.setQuantite(Math.abs(difference));
                lv.setReference(found);
                createLigneVente(inv, lv);
            } else if (difference > 0) {
                Recquisition last = getLastEntry(prod.getUid());
                double coutUnit = (coutAchat > 0) ? (coutAchat / qc) : 0.0;
                Recquisition rq = new Recquisition(DataId.generate());
                rq.setCoutAchat(coutUnit);
                rq.setDate(LocalDateTime.now());
                rq.setDateExpiry(dateExp);
                rq.setMesureId(mesUnit);
                rq.setNumlot(lot);
                rq.setObservation("Ajustement Inventaire (+) " + inv.getCodeInventaire());
                rq.setProductId(prod);
                rq.setQuantite(difference);
                rq.setReference("#INV-" + inv.getCodeInventaire());
                rq.setRegion(region);
                rq.setStockAlert(1d);
                Recquisition crq = createRecquisition(inv, rq);
                if (last != null) {
                    List<PrixDeVente> prices = delegates.RecquisitionDelegate.findLastPrices(prod.getUid());
                    if (prices != null) {
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
                    }
                }
            }
        }
        // else: aucun comptage restant -> les purges ont deja ramene le stock
        // a son etat d'avant le comptage via rectifyStock appele dans les purge*()
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
                    Query query = em.createNamedQuery("Compter.findAll");
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
                    Query query = em.createNamedQuery("Produit.findAll");
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
            sb.append(
                    "SELECT * FROM compter p WHERE p.product_id =  ? AND p.date_count BETWEEN ? AND  ? AND p.region = ? ");
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

    /**
     * Date de reference de l'inventaire. Une date d'ouverture explicite fige
     * la quantite theorique ; les anciens inventaires sans date conservent le
     * comportement historique (stock courant).
     */
    static LocalDate inventoryReferenceDate(Inventaire inv) {
        return inv != null && inv.getDateDebut() != null
                ? inv.getDateDebut()
                : LocalDate.now();
    }

    /**
     * Quantite theorique a la date d'ouverture. Si aucun agregat n'existe pour
     * cette date (ancienne base, date anterieure au premier mouvement), on
     * retombe explicitement sur le stock courant pour ne pas produire un faux
     * ecart de zero.
     */
    double theoreticalStockAtOpening(Produit prod, String lot, Inventaire inv, String region) {
        if (prod == null) {
            return 0d;
        }
        StockAgregate opening = findLotStock(prod.getUid(), lot,
                inventoryReferenceDate(inv), region);
        if (opening != null && opening.getFinalQuantity() != null) {
            return opening.getFinalQuantity();
        }
        return delegates.RecquisitionDelegate.sumLatestLotFinalQuantityFromStockAggregate(
                prod.getUid(), lot, region);
    }

    /**
     * Reference theorique d'un comptage : la photographie persistee si elle
     * existe, sinon (anciennes lignes) la valeur calculee a l'ouverture.
     */
    double resolveTheoreticalReference(Compter compter, Produit prod, String lot,
            Inventaire inv, String region) {
        if (compter != null && compter.getTheoreticalSnapshot() != null
                && Double.isFinite(compter.getTheoreticalSnapshot())) {
            return compter.getTheoreticalSnapshot();
        }
        return theoreticalStockAtOpening(prod, lot, inv, region);
    }

    private Double snapshotOfRemaining(Produit prod, String lot, Inventaire inv, String region) {
        if (prod == null || inv == null || inv.getUid() == null) {
            return null;
        }
        List<Compter> remaining = findComptageForProduit(prod.getUid(), inv.getUid(), lot, region);
        if (remaining == null) {
            return null;
        }
        for (Compter line : remaining) {
            if (line != null && line.getTheoreticalSnapshot() != null
                    && Double.isFinite(line.getTheoreticalSnapshot())) {
                return line.getTheoreticalSnapshot();
            }
        }
        return null;
    }

    private static double contentFactor(Mesure mesure) {
        return (mesure != null && mesure.getQuantContenu() != null && mesure.getQuantContenu() > 0)
                ? mesure.getQuantContenu()
                : 1.0;
    }

    public StockAgregate findLotStock(String pId, String lot, LocalDate now, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM stock_agregate p WHERE p.product_id = ? AND COALESCE(p.num_lot, '') = ? AND p.date BETWEEN ? AND ? AND p.region = ? ORDER BY p.date DESC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                query.setParameter(1, pId);
                query.setParameter(2, LotKeys.normalize(lot));
                query.setParameter(3, now.atStartOfDay());
                query.setParameter(4, now.atTime(23, 59, 59));
                query.setParameter(5, region).setMaxResults(1);
                return (StockAgregate) query.getSingleResult();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptageForProduit(String puid, String inventaireId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ?  ORDER BY p.updated_at ASC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, puid);
                query.setParameter(2, inventaireId);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptageForProduit(String puid, String inventaireId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ? AND COALESCE(p.numlot, '') = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, puid);
                query.setParameter(2, inventaireId);
                query.setParameter(3, LotKeys.normalize(lot));
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptageForProduit(String puid, String inventaireId, String lot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ? AND COALESCE(p.numlot, '') = ? AND p.region = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, puid);
                query.setParameter(2, inventaireId);
                query.setParameter(3, LotKeys.normalize(lot));
                query.setParameter(4, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Compter> findComptages(String inventaireId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM compter p WHERE p.inventaire_id = ? AND p.region = ?");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, inventaireId);
                query.setParameter(2, region);
                return query.getResultList();
            });
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
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, offline);
                return query.getResultList();
            });
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

    public double sommeComptage(Produit p, String lot, Inventaire inv) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT SUM(COALESCE(c.quantite,0)*COALESCE(m.quantcontenu,0)) s FROM compter c,mesure m WHERE c.product_id = ? AND c.inventaire_id = ? AND COALESCE(c.numlot, '') = ? AND c.mesure_id=m.uid");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, p.getUid());
                query.setParameter(2, inv.getUid());
                query.setParameter(3, LotKeys.normalize(lot));
                Object result = query.getSingleResult();
                return result == null ? 0 : ((Number) result).doubleValue();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter(1, p.getUid());
        query.setParameter(2, inv.getUid());
        query.setParameter(3, LotKeys.normalize(lot));
        Object result = query.getSingleResult();
        return result == null ? 0 : ((Number) result).doubleValue();
    }

    @Override
    public Compter findComptageByInventaireProduit(String iuid, String puid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM compter p WHERE p.product_id =  ? AND p.inventaire_id = ? ORDER BY p.updated_at DESC");
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

    private void updateCompterMeta(String productId, String inventaireId, String lot, String region, double ecart,
            double th) {
        String sql = "UPDATE compter SET ecart = ?, quantite_theorik = ? WHERE product_id = ? AND inventaire_id = ? AND COALESCE(numlot, '') = ? AND region = ?";
        // executeWrite gere deja la transaction et la fermeture de l'EntityManager
        // dans les deux modes. L'ancien chemin MySQL commitait une transaction
        // potentiellement ouverte par l'appelant, ce qui validait des ecritures
        // metier partielles.
        ManagedSessionFactory.executeWrite(em -> {
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, ecart);
            query.setParameter(2, th);
            query.setParameter(3, productId);
            query.setParameter(4, inventaireId);
            query.setParameter(5, LotKeys.normalize(lot));
            query.setParameter(6, region);
            query.executeUpdate();
            return null;
        });
    }
    
    @Override
    public void confirmerComptage(ComptageItem comptage) {
        if (comptage == null || comptage.getProduit() == null) {
            return;
        }
        Compter c = new Compter();
        c.setProductId(comptage.getProduit());
        c.setMesureId(comptage.getMesure());
        Inventaire inv = delegates.InventaireDelegate.
                findInventaireByCode(comptage.getCodeInventaire());
        if (inv != null) {
            // editStock(..., false) lit dateCount pour construire la reference
            // d'ajustement : sans date, la confirmation levait une NPE.
            LocalDateTime dateCompter = comptage.getDateCompter() != null
                    ? comptage.getDateCompter() : LocalDateTime.now();
            c.setDateCount(dateCompter);
            c.setTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
            c.setRegion(inv.getRegion());
            c.setInventaireId(inv);
            c.setNumlot(comptage.getNumlot());
            c.setCoutAchat(comptage.getCoutAchat());
            c.setDateExpiration(comptage.getDateExpiration());
            editStock(c, false);
        }
    }

    private void editStock(Compter compter,boolean isNew) {
        Produit prod = compter.getProductId();
        String region = compter.getRegion();
        Inventaire inv = compter.getInventaireId();
        Mesure mz = compter.getMesureId();

        // Avant tout ajustement : purger les anciens enregistrements #INV pour ce produit/lot
        // (évite le doublon lors d'une mise à jour de comptage)
        // On purge d'abord pour que le calcul du théorique suivant soit basé sur le stock "propre"
        LocalDate reference = (!isNew && compter.getDateCount() != null)
                ? compter.getDateCount().toLocalDate() : LocalDate.now();
        String refJour = inv.getCodeInventaire() + "-" + reference;
        purgeAjustementLigneVente(refJour, prod.getUid(), compter.getNumlot(), region);
        purgeAjustementRecquisition("#INV-" + inv.getCodeInventaire(), prod.getUid(), compter.getNumlot(), region);

        // 1. Somme physique cumulée pour ce lot (tous les comptages de cet inventaire)
        double stockReel = sommeComptage(prod, compter.getNumlot(), inv);
        LOG.fine(() -> "Stock physique cumule = " + stockReel);
        // 2. Reference theorique : photographie immuable de l'ouverture.
        // Les mouvements survenus pendant le comptage ne la modifient pas.
        double stockTheorik = resolveTheoreticalReference(compter, prod, compter.getNumlot(), inv, region);
        // 3. Différence à ajuster en pièces (Stock Physique Souhaité - Stock Actuel)
        double difference = stockReel - stockTheorik;

        // Mise à jour de l'écart et du théorique sur l'objet compter pour reflet en
        // base de données
        double qc = contentFactor(mz);
        double ecartEnMesure = difference / qc;
        double thEnMesure = stockTheorik / qc;

        compter.setEcart(ecartEnMesure);
        compter.setQuantiteTheorik(thEnMesure);
        updateCompterMeta(prod.getUid(), inv.getUid(), compter.getNumlot(), region, ecartEnMesure, thEnMesure);

        Mesure mesUnit = findUnitForProduit(prod.getUid(), 1.0);
        if (mesUnit == null) {
            mesUnit = mz;
        }

        if (difference < 0) {
            // Sortie de stock par une vente d'ajustement (ex: -2 pièces)
            Vente found = findOrCreateVenteAj(inv.getCodeInventaire(), region);
            LigneVente lv = new LigneVente(DataId.generateLong());
            lv.setCoutAchat(0d);
            lv.setMesureId(mesUnit);
            lv.setClientId("#INV");
            lv.setMontantCdf(0);
            lv.setMontantUsd(0);
            lv.setNumlot(compter.getNumlot());
            lv.setPrixUnit(0d);
            lv.setProductId(prod);
            lv.setQuantite(Math.abs(difference));
            lv.setReference(found);
            createLigneVente(inv, lv);
            LOG.info(() -> "Inventaire : sortie de " + Math.abs(difference) + " unites pour ajuster le stock");
        } else if (difference > 0) {
            // Entrée de stock par une réquisition d'ajustement (ex: +3 pièces)
            Recquisition last = getLastEntry(prod.getUid());
            double coutUnit = (compter.getCoutAchat() > 0) ? (compter.getCoutAchat() / qc) : 0.0;

            Recquisition rq = new Recquisition(DataId.generate());
            rq.setCoutAchat(coutUnit);
            rq.setDate(LocalDateTime.now());
            rq.setDateExpiry(compter.getDateExpiration());
            rq.setMesureId(mesUnit);
            rq.setNumlot(compter.getNumlot());
            rq.setObservation("Ajustement Inventaire (+) " + inv.getCodeInventaire());
            rq.setProductId(prod);
            rq.setQuantite(difference);
            rq.setReference("#INV-" + inv.getCodeInventaire());
            rq.setRegion(region);
            rq.setStockAlert(1d);
            Recquisition crq = createRecquisition(inv, rq);

            // Recopier les prix s'ils existent pour la nouvelle réquisition
            if (last != null) {
                List<PrixDeVente> prices = delegates.RecquisitionDelegate.findLastPrices(prod.getUid());
                // findPricesForRecq(last.getUid());
                if (prices != null) {
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
                }
            }
            LOG.info(() -> "Inventaire : entree de " + difference + " unites pour ajuster le stock");
        } else {
            LOG.fine("Inventaire : aucun ajustement, stock physique = stock theorique");
        }

        // 4. Clôturer à nouveau pour mettre à jour l'agrégat daily avec les nouvelles
        // transactions
        LocalDate leo = LocalDate.now();
        RecquisitionDelegate.rectifyStock(prod, leo, leo, region, compter.getNumlot());
    }

    /**
     * Pour chaque produit sans aucune ligne {@link Compter} sur cet inventaire
     * : sortir par vente dédiée (référence = code inventaire, observation
     * "Ajustement Inventaire") la quantité théorique encore en stock, en unités
     * de la mesure « 1 pièce » comme {@link #editStock}, pour ramener le
     * disponible à 0 via {@link #rectifyStock}. Idempotent si la ligne
     * d’ajustement #INV existe déjà pour ce produit sur cette vente.
     */
    @Override
    public void removeNoCountedProducts(Inventaire inv) {
        removeNoCountedProducts(inv, null);
    }

    @Override
    public void removeNoCountedProducts(Inventaire inv, java.util.function.Consumer<Compter> onCreated) {
        if (isInventoryClosed(inv)) {
            // Clôture rejouable : une seconde invocation ne doit pas recréer
            // de comptages zéro ni de régularisation.
            tools.SyncLogger.getInstance().logMessage(
                    "INVENTORY-CLOSE",
                    "Inventaire " + inv.getUid() + " déjà clôturé : clôture ignorée (idempotente).");
            return;
        }
        withInventoryLock(inv, () -> {
            // Sauvegarde immédiate sous le verrou (clôtures concurrentes) puis
            // transaction unique : les comptages zéro et tous les ajustements
            // de stock sont validés ou annulés ensemble.
            services.utils.DatabaseBackup.backupNow(
                    ManagedSessionFactory.localDatabasePath(), "inventory-close", 10);
            ManagedSessionFactory.executeWrite(em -> {
                removeNoCountedProductsLocked(inv, onCreated);
                return null;
            });
        });
    }

    private void removeNoCountedProductsLocked(Inventaire inv, java.util.function.Consumer<Compter> onCreated) {
        if (inv == null || inv.getUid() == null || inv.getRegion() == null || inv.getCodeInventaire() == null) {
            return;
        }
        List<Produit> lspr = findProducts();
        if (lspr == null) {
            return;
        }

        for (Produit produit : lspr) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Cloture annulee par l'utilisateur");
            }
            if (produit == null || produit.getUid() == null) {
                continue;
            }
            List<String> allLots = findAllDistinctLots(produit.getUid(), inv.getRegion());
            for (String lot : allLots) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException("Cloture annulee par l'utilisateur");
                }
                lot = LotKeys.normalize(lot);
                List<Compter> existing = findComptageForProduit(produit.getUid(), inv.getUid(), lot, inv.getRegion());
                if (existing != null && !existing.isEmpty()) continue;
                StockAgregate stock = findLotStock(produit.getUid(), lot,
                        inventoryReferenceDate(inv), inv.getRegion());
                double stockTheorik = (stock != null && stock.getFinalQuantity() != null)
                        ? stock.getFinalQuantity()
                        : delegates.RecquisitionDelegate.sumLatestLotFinalQuantityFromStockAggregate(
                                produit.getUid(), lot, inv.getRegion());
                Compter co = new Compter();
                // Uid deterministe : une cloture rejouee (ou concurrente sur un
                // autre poste) vise la meme ligne, la cle primaire empeche alors
                // un second comptage zero et un double ajustement de stock.
                co.setUid(DataId.deterministic("compter-zero", inv.getUid(),
                        produit.getUid(), lot, inv.getRegion()));
                co.setCoutAchat(stock != null && stock.getCoutAchat() != null ? stock.getCoutAchat() : 0d);
                co.setDateCount(LocalDateTime.now());
                co.setEcart(-stockTheorik);
                co.setInventaireId(inv);
                co.setMesureId(stock != null ? stock.getMesureId() : null);
                co.setNumlot(lot);
                co.setProductId(produit);
                co.setQuantite(0);
                co.setTheoreticalSnapshot(stockTheorik);
                co.setQuantiteTheorik(stockTheorik);
                co.setRegion(inv.getRegion());
                co.setTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
                try {
                    createCompter(co);
                } catch (RuntimeException ex) {
                    if (isDuplicateKey(ex)) {
                        tools.SyncLogger.getInstance().logMessage("INVENTORY-CLOSE",
                                "Comptage zero deja present pour " + produit.getUid()
                                        + " / lot " + lot + " (cloture concurrente ignoree).");
                        continue;
                    }
                    throw ex;
                }
                if (onCreated != null) onCreated.accept(co);
            }
        }
    }

    private static boolean isDuplicateKey(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase(java.util.Locale.ROOT);
                if (lower.contains("unique constraint") || lower.contains("duplicate entry")
                        || lower.contains("constraint failed") || lower.contains("primary key")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private List<String> findAllDistinctLots(String productId, String region) {
        String sql = "SELECT DISTINCT COALESCE(numlot, '') FROM recquisition WHERE product_id = ? AND region = ? "
                + "UNION "
                + "SELECT DISTINCT COALESCE(num_lot, '') FROM stock_agregate WHERE product_id = ? AND region = ?";
        return ManagedSessionFactory.executeRead(em -> {
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, productId);
            q.setParameter(2, region);
            q.setParameter(3, productId);
            q.setParameter(4, region);
            @SuppressWarnings("unchecked")
            List<String> rows = q.getResultList();
            return rows;
        });
    }

    StockAgregate stock;

    public double findOnlyStockUnits(Produit produit, String lot, String region, LocalDate datedebut,
            LocalDate datefin) {
        double E = sommeEntreeSurPeriode(produit.getUid(), lot, datedebut, datefin, region);
        double SV = sommeSortieSurPeriode(produit.getUid(), lot, datedebut, datefin, region);
        double stockInit = calculerStockInitialEnUnite(produit.getUid(), lot, datedebut, region);
        LOG.fine(() -> "Stock uniquement : entrees=" + E + ", sorties=" + SV + ", initial=" + stockInit);
        // double expiree = getStockExpiree(produit.getUid(), datedebut, datefin,
        // region);
        double stockFinal = stockInit + E - SV;
        return stockFinal;
    }

    public Vente findAjuVente(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente m WHERE m.reference = ? AND m.libelle = ? LIMIT 1");
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
        String refJour = codeInventaire + "-" + LocalDate.now().toString();
        Vente vente = findAjuVente(refJour);
        if (vente == null) {
            LOG.fine(() -> "Creation de la vente d'ajustement " + refJour);
            Vente aju = new Vente(DataId.generateInt());
            aju.setReference(refJour);
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

    public double sommeEntreeSurPeriode(String uid, String lot, LocalDate datedebut, LocalDate datefin, String region) {
        if (datedebut == null || datefin == null) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(r.quantite,0)*COALESCE(m.quantcontenu,0)) px FROM recquisition r,mesure m "
                + "WHERE r.product_id = ? AND r.date BETWEEN ? AND ? AND r.region LIKE ? AND "
                + "r.numlot = ? AND r.mesure_id=m.uid");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) (em.createNativeQuery(sb.toString(), Double.class)
                        .setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(4, region).setParameter(5, lot).getSingleResult());
                return entrees == null ? 0 : entrees;
            });
        }
        Double entrees = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
                Double.class).setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, datefin.atTime(23, 59, 59))
                .setParameter(4, region).setParameter(5, lot).getSingleResult();
        return entrees == null ? 0 : entrees;
    }

    public double sommeSortieSurPeriode(String uid, String lot, LocalDate datedebut, LocalDate datefin, String region) {
        if (datedebut == null || datefin == null) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ?) ");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double sorties = (Double) em.createNativeQuery(sb.toString(), Double.class)
                        .setParameter(1, uid)
                        .setParameter(2, lot)
                        .setParameter(3, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(4, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                        .setParameter(5, region)
                        .getSingleResult();
                return sorties == null ? 0 : sorties;
            });
        }
        Double sorties = (Double) (ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sb.toString(), Double.class)
                .setParameter(1, uid)
                .setParameter(2, lot)
                .setParameter(3, datedebut.atStartOfDay())
                .setParameter(4, datefin.atTime(23, 59, 59))
                .setParameter(5, region)
                .getSingleResult());
        return sorties == null ? 0 : sorties;
    }

    public double calculerStockInitialEnUnite(String uid, String lot, LocalDate datedebut, String region) {
        if (datedebut == null) {
            return 0;
        }
        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                + "WHERE r.product_id = ? AND r.date < ? AND r.region LIKE ? AND r.numlot = ? AND r.mesure_id=m.uid");
        StringBuilder sbS = new StringBuilder();
        sbS.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE ? AND v.dateVente < ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) em.createNativeQuery(sbE.toString(),
                        Double.class).setParameter(1, uid)
                        .setParameter(2, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter(3, region).setParameter(4, lot)
                        .getSingleResult();
                Double sorties = (Double) em.createNativeQuery(sbS.toString(), Double.class)
                        .setParameter(1, uid).setParameter(2, lot).setParameter(3, region)
                        .setParameter(4, Timestamp.valueOf(datedebut.atStartOfDay()))
                        .getSingleResult();
                double stok = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
                return stok <= 0 ? 0 : stok;
            });
        }
        Double entrees = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbE.toString(),
                Double.class).setParameter(1, uid)
                .setParameter(2, datedebut.atStartOfDay())
                .setParameter(3, region).setParameter(4, lot)
                .getSingleResult();

        Double sorties = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbS.toString(),
                Double.class).setParameter(1, uid).setParameter(2, lot).setParameter(3, region)
                .setParameter(4, datedebut.atStartOfDay())
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
        sbS.append(
                "SELECT (SUM(COALESCE(s.quantite,0)*COALESCE(m.quantcontenu, 0))) pieces FROM ligne_vente s, mesure m"
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
            }).join();
            LOG.fine(() -> "Prix de vente enregistre : " + cat.getPrixUnitaire());
            return cat;
        }
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(cat);
            return null;
        });
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
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), PrixDeVente.class);
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
            }).join();
            LOG.fine(() -> "Recquisition inventaire enregistree : " + cat.getReference());
            return cat;
        }
        ManagedSessionFactory.executeWrite(em -> {
            em.persist(cat);
            return null;
        });
        return cat;
    }

    /**
     * Supprime les LigneVente d'ajustement inventaire (#INV) existantes pour
     * éviter les doublons lors d'un re-comptage.
     */
    private void purgeAjustementLigneVente(String venteRef, String prodId, String numlot, String region) {
        try {
            String sql = "DELETE FROM ligne_vente WHERE product_id = ? AND COALESCE(numlot, '') = ? AND clientid = '#INV'"
                    + " AND reference_uid IN (SELECT uid FROM vente WHERE reference = ? AND region = ?)";
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.createNativeQuery(sql)
                            .setParameter(1, prodId)
                            .setParameter(2, LotKeys.normalize(numlot))
                            .setParameter(3, venteRef)
                            .setParameter(4, region)
                            .executeUpdate();
                    return null;
                }).join();
            } else {
                ManagedSessionFactory.executeWrite(em -> {
                    em.createNativeQuery(sql)
                            .setParameter(1, prodId)
                            .setParameter(2, LotKeys.normalize(numlot))
                            .setParameter(3, venteRef)
                            .setParameter(4, region)
                            .executeUpdate();
                    return null;
                });
            }
            // Update stock aggregate to maintain coherence after deletion
            Produit p = findProduitById(prodId);
            if (p != null) {
                LocalDate leo = LocalDate.now();
                RecquisitionDelegate.rectifyStock(p, leo, leo, region, numlot);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Echec de purge des lignes de vente d'ajustement", e);
        }
    }

    private Produit findProduitById(String id) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(Produit.class, id));
        }
        return ManagedSessionFactory.getEntityManager().find(Produit.class, id);
    }

    private void purgeAjustementPrices(String ref, String prodId, String numlot, String region) {
        Recquisition r = delegates.RecquisitionDelegate.findRecquisition(ref, prodId, numlot, region);
        if (r != null) {
            List<PrixDeVente> lps = findPricesForRecq(r.getUid());
            LOG.fine(() -> "Purge des prix d'ajustement pour " + r);
            for (PrixDeVente lp : lps) {
                LOG.fine(() -> "Suppression du prix associe " + lp);
                deletePrixDeVente(lp);
            }
        }
    }

    public void deletePrixDeVente(PrixDeVente cat) {
        ManagedSessionFactory.executeWrite(em -> {
            em.remove(em.merge(cat));
            return null;
        });
        LOG.fine(() -> "Prix de vente supprime : " + cat.getPrixUnitaire());
    }

    /**
     * Supprime les Recquisitions d'ajustement inventaire (#INV) existantes pour
     * éviter les doublons lors d'un re-comptage.
     */
    private void purgeAjustementRecquisition(String ref, String prodId, String numlot, String region) {
        try {
            // 1. Purge associated prices first to maintain data coherence
            purgeAjustementPrices(ref, prodId, numlot, region);

            // 2. Delete the adjustment requisitions
            String sql = "DELETE FROM recquisition WHERE product_id = ? AND COALESCE(numlot, '') = ? AND reference = ? AND region = ?";
            ManagedSessionFactory.executeWrite(em -> {
                em.createNativeQuery(sql)
                        .setParameter(1, prodId)
                        .setParameter(2, LotKeys.normalize(numlot))
                        .setParameter(3, ref)
                        .setParameter(4, region)
                        .executeUpdate();
                return null;
            });

            // 3. Update stock aggregate to maintain coherence after deletion
            Produit p = findProduitById(prodId);
            if (p != null) {
                LocalDate leo = LocalDate.now();
                RecquisitionDelegate.rectifyStock(p, leo, leo, region, numlot);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Echec de purge des requisations d'ajustement", e);
        }
    }

    public LigneVente createLigneVente(Inventaire inv, LigneVente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).join();
            LOG.fine(() -> "Ligne de vente inventaire creee pour "
                    + (cat.getProductId() != null ? cat.getProductId().getNomProduit() : "?"));
            return cat;
        }
        try {
            ManagedSessionFactory.executeWrite(em -> {
                em.persist(cat);
                return null;
            });
            return cat;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Vente createVente(Vente cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).join();
            LOG.fine(() -> "Vente d'ajustement inventaire creee : " + cat.getReference());
            return cat;
        }
        try {
            ManagedSessionFactory.executeWrite(em -> {
                em.persist(cat);
                return null;
            });
            return cat;
        } catch (RuntimeException e) {
            return null;
        }
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
        try {
            ManagedSessionFactory.executeWrite(em -> {
                em.persist(cat);
                return null;
            });
            LOG.fine(() -> "Client enregistre : " + cat.getNomClient());
            return cat;
        } catch (RuntimeException e) {
            if (isUniqueConstraintViolation(e)) {
                LOG.fine(() -> "Client deja present : " + cat.getUid());
                return cat;
            }
            LOG.log(Level.WARNING, "Echec de creation du client " + cat.getUid(), e);
            return null;
        }
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
                return ManagedSessionFactory
                        .executeRead(em -> (Mesure) em.createNativeQuery(sb.toString(), Mesure.class)
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
            sb.append(
                    "SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.product_id = ? AND (s.num_lot IS NULL OR s.num_lot = '')");
            if (ManagedSessionFactory.isEmbedded()) {
                try {
                    return ManagedSessionFactory
                            .executeRead(em -> (StockAgregate) em.createNativeQuery(sb.toString(), StockAgregate.class)
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
    // public Vente findVenteByRef(String ref) {
    // try {
    //
    // StringBuilder sb = new StringBuilder();
    // sb.append("SELECT * FROM vente v WHERE v.reference = ? ");
    // if (ManagedSessionFactory.isEmbedded()) {
    // return ManagedSessionFactory.executeRead(em -> (Vente)
    // (em.createNativeQuery(sb.toString(), Vente.class)
    // .setParameter(1, ref).getSingleResult()));
    // }
    // Query query =
    // ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
    // Vente.class);
    // query.setParameter(1, ref);
    // return (Vente) query.getSingleResult();
    // } catch (NoResultException e) {
    // return null;
    // }


//// }
    ////
    // public List<LigneVente> findByReference(Integer uid, String pro) {
    // try {
    // StringBuilder sb = new StringBuilder();
    // sb.append("SELECT * FROM ligne_vente WHERE reference_uid = ? AND product_id =
    //// ? ");
    // if (ManagedSessionFactory.isEmbedded()) {
    // return ManagedSessionFactory.executeRead(em -> {
    // Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
    // query.setParameter(1, uid).setParameter(2, pro);
    // return query.getResultList();
    // });
    // }
    // Query query =
    //// ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
    //// LigneVente.class);
    // query.setParameter(1, uid);
    // return query.getResultList();
    // } catch (EntityNotFoundException e) {
    // return null;
    // }
    // }
}
