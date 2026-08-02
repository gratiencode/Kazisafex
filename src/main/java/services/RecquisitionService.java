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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import java.time.Month;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import tools.MemoryGuard;
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

    private ClotureCallback clotureCallback;

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

    public void setClotureCallback(ClotureCallback clotureCallback) {
        this.clotureCallback = clotureCallback;
    }

    @Override
    public void setClotureListener(ClotureCallback listener) {
        setClotureCallback(clotureCallback);
    }

    private void notifyCallback(int index, int s, Produit p) {
        if (this.clotureCallback != null) {
            this.clotureCallback.onClosure(index, s, p);
        }
    }

    private void notifyClotureFinish(int size) {
        if (this.clotureCallback != null) {
            this.clotureCallback.onFinish(size);
        }
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
                AggregateTriggerService.getInstance().notifyRecquisition(e);
            });
            return cat;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        tx.commit();
        AggregateTriggerService.getInstance().notifyRecquisition(cat);
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
                AggregateTriggerService.getInstance().notifyVente(e);
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
            AggregateTriggerService.getInstance().notifyVente(cat);
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
                AggregateTriggerService.getInstance().notifyLigneVente(e);
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
            AggregateTriggerService.getInstance().notifyLigneVente(cat);
            return cat;
        } catch (Exception e) {
            return cat;
        }
    }

    @Override
    public Recquisition updateRecquisition(Recquisition cat) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).thenAccept(e -> {
                    System.out.println("Element req " + e.getReference() + " enregistree");
                    AggregateTriggerService.getInstance().notifyRecquisition(e);
                });
                return cat;
            }
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            ManagedSessionFactory.getEntityManager().merge(cat);
            tx.commit();
            AggregateTriggerService.getInstance().notifyRecquisition(cat);
            return cat;
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void deleteRecquisition(Recquisition cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> AggregateTriggerService.getInstance().notifyRecquisition(e));
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
        AggregateTriggerService.getInstance().notifyRecquisition(cat);
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
        LocalDate today = LocalDate.now();
        return saveStockFromRecquisition(e, today, today, "Journalier du " + today);
    }

    @Override
    public StockAgregate updateStockAgregate(StockAgregate cat) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(cat);
                    return cat;
                }).thenAccept(e -> {
                    System.out.println("Element stock agregate" + e.getNumlot() + " updated");
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
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public StockAgregate findStockAgregate(String prod, String numlot, String region, boolean destroyed) {
        if (prod == null || numlot == null) {
            return null;
        }
        String sql = """
                SELECT * FROM stock_agregate s
                WHERE s.product_id = ?
                  AND s.region LIKE ?
                  AND s.num_lot = ?
                  AND s.date BETWEEN ? AND ? AND s.destroyed = ?
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND s2.region = s.region
                        AND s2.num_lot = s.num_lot
                        AND s2.date BETWEEN ? AND ? AND s2.destroyed = s.destroyed
                  )
                ORDER BY s.date DESC
                """;
        LocalDate now = LocalDate.now();
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> (StockAgregate) em.createNativeQuery(sql, StockAgregate.class)
                    .setParameter(1, prod)
                    .setParameter(2, (region == null ? "%" : region))
                    .setParameter(3, numlot)
                    .setParameter(4, now)
                    .setParameter(5, now.plusDays(1))
                    .setParameter(6, destroyed)
                    .setParameter(7, now)
                    .setParameter(8, now.plusDays(1)).setMaxResults(1)
                    .getSingleResult());
        }
        return (StockAgregate) ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sql, StockAgregate.class)
                .setParameter(1, prod)
                .setParameter(2, (region == null ? "%" : region))
                .setParameter(3, numlot)
                .setParameter(4, now)
                .setParameter(5, now.plusDays(1))
                .setParameter(6, destroyed)
                .setParameter(7, now)
                .setParameter(8, now.plusDays(1)).setMaxResults(1)
                .getSingleResult();
    }

    /**
     * Totaux agrégés (tous lots) pour la ligne de synthèse sans numéro de lot.
     */
    private static final class LotClosureTotals {

        double entrees;
        double sorties;
        double initial;
        double expiree;
        double finalQty;

        void add(double e, double s, double i, double x, double f) {
            entrees += e;
            sorties += s;
            initial += i;
            expiree += x;
            finalQty += f;
        }
    }

    /**
     * Métriques d’agrégation (pièces) sur une période : ordre logique stock
     * initial (ouverture) → entrées période → sorties période → (périmés) →
     * stock final. Formule :
     * {@code finalValid = max(0, stockInitial + entrees - sorties - expiree)}.
     */
    private record LotPeriodPieces(double entrees, double sorties, double stockInitial, double expiree,
            double finalValid) {

    }

    private LotPeriodPieces computeLotPeriodPieces(String productUid, String numlot, String region,
            LocalDate datedebut, LocalDate datefin) {
        if (productUid == null || numlot == null || numlot.isBlank() || region == null || region.isBlank()
                || datedebut == null || datefin == null) {
            return new LotPeriodPieces(0, 0, 0, 0, 0);
        }
        System.out.println("Interval date : du " + datedebut + " a " + datefin);
        double entrees = sumBatchedRecqusitionFrom(productUid, numlot, datedebut, datefin, region);
        double sorties = sumBatchedLigneventeFrom(productUid, numlot, datedebut, datefin, region);
        double stockInit
                = //                isStockExists(productUid, numlot, datedebut, datefin)
                //                ? calculerStockInitialEnUniteByLot(productUid, numlot, datedebut, region):
                stockInitialAlternative(productUid, datedebut, numlot, region);
        double expiree = 0;
        //getStockExpireeByLot(productUid, numlot, datedebut, datefin, region);
        double finalValid = Math.max(0, (stockInit + entrees - sorties));
        System.out.println("LES TOCKS RETROUVES batch->" + numlot + ": -> SI=" + stockInit + " E=" + entrees + " S=" + sorties + " SF=" + finalValid);
        return new LotPeriodPieces(entrees, sorties, stockInit, expiree, finalValid);
    }

    /**
     * Borne inférieure pour cumuler l’ouverture de période lorsqu’aucune région
     * n’est filtrée.
     */
    private static final LocalDate GLOBAL_LEDGER_START = LocalDate.of(1970, 1, 1);

    /**
     * Même séquence que {@link #computeLotPeriodPieces} : d’abord stock initial
     * à l’ouverture de {@code datedebut} (cumul recquisitions − cumul ventes
     * jusqu’à la veille), puis entrées et sorties sur
     * {@code [datedebut, datefin]}, périmés 0 (non calculés sans région), enfin
     * stock final.
     */
    private LotPeriodPieces computeLotPeriodPiecesNoRegion(String productUid, String numlot, LocalDate datedebut,
            LocalDate datefin) {
        if (productUid == null || numlot == null || numlot.isBlank() || datedebut == null || datefin == null) {
            return new LotPeriodPieces(0, 0, 0, 0, 0);
        }
        double stockInitial = 0d;
        if (datedebut.isAfter(GLOBAL_LEDGER_START)) {
            LocalDate veille = datedebut.minusDays(1);
            stockInitial = Math.max(0,
                    sumBatchedRecqusitionFrom(productUid, numlot, GLOBAL_LEDGER_START, veille)
                    - sumBatchedLigneventeFrom(productUid, numlot, GLOBAL_LEDGER_START, veille));
        }
        double entrees = sumBatchedRecqusitionFrom(productUid, numlot, datedebut, datefin);
        double sorties = sumBatchedLigneventeFrom(productUid, numlot, datedebut, datefin);
        double expiree = 0d;
        double finalValid = Math.max(0, stockInitial + entrees - sorties - expiree);
        return new LotPeriodPieces(entrees, sorties, stockInitial, expiree, finalValid);
    }

    private StockAgregate applyStockAggregateValues(StockAgregate target, Produit produit, Mesure mesure, String region,
            String context, String numlot, LocalDate dateExpiration, LocalDate aggregateDate, double coutAchat,
            double stockInitial, double entrees, double sorties, double expiree, double stockFinal) {
        StockAgregate row = target == null ? new StockAgregate(DataId.generate()) : target;
        row.setProductId(produit);
        row.setMesureId(mesure);
        row.setRegion(normalizeStockAggregateText(region));
        row.setContext(normalizeStockAggregateText(context));
        row.setNumlot(normalizeStockAggregateText(numlot));
        row.setDateExpiration(dateExpiration);
        row.setDate(aggregateDate);
        row.setCoutAchat(coutAchat);
        row.setDestroyed(target == null ? false : (target.isDestroyed() == null ? false : target.isDestroyed()));
        row.setInitialQuantity(stockInitial);
        row.setEntrees(entrees);
        row.setSorties(sorties);
        row.setExpiree(expiree);
        row.setFinalQuantity(stockFinal);
        return row;
    }

    private String normalizeStockAggregateText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private double safeStockAggregateValue(Double value) {
        return value == null ? 0d : value;
    }

    private StockAgregate findStockAgregate(String uid) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    return em.find(StockAgregate.class, uid);
                });
            }
            return ManagedSessionFactory.getEntityManager().find(StockAgregate.class, uid);
        } catch (NoResultException e) {
            return null;
        }

    }

    private StockAgregate synthesizeStockAggregateFromLots(List<StockAgregate> lotRows, String region, String context) {
        if (lotRows == null || lotRows.isEmpty()) {
            return null;
        }
        StockAgregate latest = null;
        double stockInitial = 0d;
        double entrees = 0d;
        double sorties = 0d;
        double expiree = 0d;
        double stockFinal = 0d;
        for (StockAgregate lotRow : lotRows) {
            if (lotRow == null) {
                continue;
            }
            stockInitial += safeStockAggregateValue(lotRow.getInitialQuantity());
            entrees += safeStockAggregateValue(lotRow.getEntrees());
            sorties += safeStockAggregateValue(lotRow.getSorties());
            expiree += safeStockAggregateValue(lotRow.getExpiree());
            stockFinal += safeStockAggregateValue(lotRow.getFinalQuantity());
            if (latest == null || (lotRow.getDate() != null
                    && (latest.getDate() == null || lotRow.getDate().isAfter(latest.getDate())))) {
                latest = lotRow;
            }
        }
        if (latest == null) {
            return null;
        }
        return applyStockAggregateValues(new StockAgregate(DataId.generate()),
                latest.getProductId(),
                latest.getMesureId(),
                region,
                context != null ? context : latest.getContext(),
                null,
                null,
                latest.getDate(),
                safeStockAggregateValue(latest.getCoutAchat()),
                stockInitial,
                entrees,
                sorties,
                expiree,
                stockFinal);
    }

    /**
     * Régénère les {@link StockAgregate} par lot sur la période
     * {@code datedebut}–{@code datefin} et la région du mouvement source. Avec
     * région : {@link #upsertLotStockAggregates}, qui s’appuie sur
     * {@link #computeLotPeriodPieces} (entrées/sorties période batch région
     * exacte, stock initial {@link #calculerStockInitialEnUniteByLot}, périmés
     * {@link #getStockExpireeByLot}). Sans région :
     * {@link #computeLotPeriodPiecesNoRegion}. La synthèse retournée reste
     * calculée en mémoire, sans insertion globale dans {@code stock_agregate}.
     */
    private StockAgregate saveStockFromRecquisition(Recquisition e, LocalDate datedebut, LocalDate datefin,
            String context) {
        if (e == null || e.getProductId() == null) {
            return null;
        }
        final LocalDate dDeb = datedebut == null ? LocalDate.now() : datedebut;
        final LocalDate dFin = datefin == null ? dDeb : datefin;
        final String targetContext = (context == null || context.isBlank())
                ? "Journalier du " + dFin
                : context;
        final String productId = e.getProductId().getUid();
        final String region = e.getRegion();
        final Mesure unite = MesureDelegate.findByProduitAndQuant(productId, 1d);
        System.out.println("Region du seed entrant en param -> " + region);
        if (region != null && !region.isBlank()) {
//            purgeDailyLotAggregatesForProduct(productId, region, dDeb, dFin, targetContext);
            LotClosureTotals totals = upsertLotStockAggregates(e.getProductId(), region, dDeb, dFin, targetContext,
                    unite);
            LocalDate summaryDate = dFin.equals(LocalDate.now()) ? LocalDate.now() : dFin;
            StockAgregate summary = applyStockAggregateValues(new StockAgregate(DataId.generate()),
                    e.getProductId(),
                    unite == null ? e.getMesureId() : unite,
                    region,
                    targetContext,
                    e.getNumlot(),
                    null,
                    summaryDate,
                    resolveUnitCost(e),
                    totals.initial,
                    totals.entrees,
                    totals.sorties,
                    totals.expiree,
                    totals.finalQty);
//            System.out.println(" RT ---- OK ");
//            System.out.println("Stock lot-agregate regenere (region=" + region + "): produit="
//                    + e.getProductId().getNomProduit() + ", synthese pieces finales=" + totals.finalQty);
            return summary;
        }

        List<Recquisition> lotHeads = findDistinctLotHeads(e);
        if (lotHeads.isEmpty() && e.getNumlot() != null && !e.getNumlot().isBlank()) {
            lotHeads.add(e);
        }

        List<StockAgregate> rowsToPersist = new ArrayList<>();
        double totalInitial = 0d;
        double totalEntries = 0d;
        double totalSorties = 0d;
        double totalExpiree = 0d;
        double totalFinal = 0d;
        LocalDate aggregateTs = dFin.equals(LocalDate.now()) ? LocalDate.now() : dFin;

        for (Recquisition lotHead : lotHeads) {
            String lot = lotHead.getNumlot();
            if (lot == null || lot.isBlank()) {
                continue;
            }
            String lotNorm = lot.trim();
            LotPeriodPieces m = computeLotPeriodPiecesNoRegion(productId, lotNorm, dDeb, dFin);

            StockAgregate lotAggregate = applyStockAggregateValues(new StockAgregate(DataId.generate()),
                    e.getProductId(),
                    unite == null ? e.getMesureId() : unite,
                    region,
                    targetContext,
                    lotNorm,
                    resolveLotDateExpirationForStockAgregate(productId, lotNorm, region, lotHead.getDateExpiry()),
                    aggregateTs,
                    resolveUnitCost(lotHead),
                    m.stockInitial(),
                    m.entrees(),
                    m.sorties(),
                    m.expiree(),
                    m.finalValid());
            rowsToPersist.add(lotAggregate);

            totalInitial += m.stockInitial();
            totalEntries += m.entrees();
            totalSorties += m.sorties();
            totalExpiree += m.expiree();
            totalFinal += m.finalValid();
        }

        if (!rowsToPersist.isEmpty()) {
            // purgeDailyLotAggregatesForProduct(productId, region, dDeb, dFin, targetContext);
            persistLotAggregates(rowsToPersist);
            System.out.println("---VVVV---");
            System.out.println("Stock lot-agregate regenere: produit=" + e.getProductId().getNomProduit()
                    + ", lots=" + rowsToPersist.size() + ", totalFinal=" + totalFinal);
        }

        return applyStockAggregateValues(new StockAgregate(DataId.generate()),
                e.getProductId(),
                unite == null ? e.getMesureId() : unite,
                region,
                targetContext,
                null,
                null,
                aggregateTs,
                resolveUnitCost(e),
                totalInitial,
                totalEntries,
                totalSorties,
                totalExpiree,
                totalFinal);
    }

    private StockAgregate saveStockFromRecquisition(Recquisition e, String lotIn, LocalDate datedebut, LocalDate datefin) {
        if (e == null || e.getProductId() == null) {
            return null;
        }
        final LocalDate dDeb = datedebut == null ? LocalDate.now() : datedebut;
        final LocalDate dFin = datefin == null ? dDeb : datefin;
        final String productId = e.getProductId().getUid();
        final String region = e.getRegion();
        final Mesure unite = MesureDelegate.findByProduitAndQuant(productId, 1d);
        System.out.println("Region du seed One entrant en param -> " + region);
        LotClosureTotals totals = upsertOneLotStockAgregate(e, region, dDeb, dFin, lotIn, unite, 0d);
        LocalDate summaryDate = dFin.equals(LocalDate.now()) ? LocalDate.now() : dFin;
        StockAgregate summary = applyStockAggregateValues(new StockAgregate(DataId.generate()),
                e.getProductId(),
                unite == null ? e.getMesureId() : unite,
                region,
                "Journalier du " + summaryDate.toString(),
                e.getNumlot(),
                null,
                summaryDate,
                resolveUnitCost(e),
                totals.initial,
                totals.entrees,
                totals.sorties,
                totals.expiree,
                totals.finalQty);
        System.out.println(" RT --rectify1-- OK ");
        System.out.println("Stock lot-agregate regenere (region=" + region + "): produit="
                + e.getProductId().getNomProduit() + ", synthese pieces finales=" + totals.finalQty);
        return summary;

    }

    @Override
    public Recquisition findRecquisition(String ref, String prodId, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.reference = ? AND s.numlot = ? "
                    + "AND s.region = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, prodId).setParameter(2, ref)
                            .setParameter(3, numlot).setParameter(4, region);
                    query.setMaxResults(1);
                    return (Recquisition) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, prodId).setParameter(2, ref)
                    .setParameter(3, numlot).setParameter(4, region);
            query.setMaxResults(1);
            return (Recquisition) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private double resolveUnitCost(Recquisition rq) {
        if (rq == null) {
            return 0d;
        }
        if (rq.getMesureId() == null || rq.getMesureId().getQuantContenu() == null
                || rq.getMesureId().getQuantContenu() == 0d) {
            return rq.getCoutAchat();
        }
        return rq.getCoutAchat() / rq.getMesureId().getQuantContenu();
    }

    /**
     * Valeur MAX(dateexpiry) renvoyée par une requête native (SQLite / JDBC).
     */
    private static LocalDate localDateFromSqlNative(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (value instanceof Date ud) {
            return new java.sql.Date(ud.getTime()).toLocalDate();
        }
        return null;
    }

    /**
     * Date de péremption à enregistrer sur {@link StockAgregate} pour un lot :
     * d’abord celle du mouvement représentatif, sinon le maximum parmi les
     * réquisitions du même produit / lot (et région si fournie), pour permettre
     * les sélections « lots expirés » sur {@code stock_agregate}.
     */
    private LocalDate resolveLotDateExpirationForStockAgregate(String productUid, String numlot, String region,
            LocalDate candidateFromRecquisition) {
        if (candidateFromRecquisition != null) {
            return candidateFromRecquisition;
        }
        if (productUid == null || numlot == null || numlot.isBlank()) {
            return null;
        }
        String lotKey = numlot.trim();
        try {
            StringBuilder sb = new StringBuilder(
                    "SELECT MAX(r.dateexpiry) FROM recquisition r WHERE r.product_id = ? AND r.numlot = ? "
                    + "AND r.dateexpiry IS NOT NULL ");
            if (region != null && !region.isBlank()) {
                sb.append("AND r.region LIKE ? ");
            }
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query q = em.createNativeQuery(sb.toString());
                    q.setParameter(1, productUid);
                    q.setParameter(2, lotKey);
                    if (region != null && !region.isBlank()) {
                        q.setParameter(3, region);
                    }
                    List<?> rows = q.getResultList();
                    if (rows.isEmpty()) {
                        return null;
                    }
                    return localDateFromSqlNative(rows.get(0));
                });
            }
            Query q = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            q.setParameter(1, productUid);
            q.setParameter(2, lotKey);
            if (region != null && !region.isBlank()) {
                q.setParameter(3, region);
            }
            List<?> rows = q.getResultList();
            if (rows.isEmpty()) {
                return null;
            }
            return localDateFromSqlNative(rows.get(0));
        } catch (Exception ex) {
            Logger.getLogger(RecquisitionService.class.getName()).log(Level.FINE,
                    "resolveLotDateExpirationForStockAgregate: " + ex.getMessage(), ex);
            return null;
        }
    }

    private List<Recquisition> findDistinctLotHeads(Recquisition seed) {
        List<Recquisition> source;
        String productId = seed.getProductId().getUid();
        String region = seed.getRegion();
        if (region == null || region.isBlank()) {
            source = findRecquisitionByProduit(productId);
        } else {
            source = findRecquisitionByProduitRegion(productId, region);
        }
        Map<String, Recquisition> byLot = new HashMap<>();
        if (source != null) {
            for (Recquisition rq : source) {
                String lot = rq.getNumlot();
                if (lot == null || lot.isBlank()) {
                    continue;
                }
                Recquisition current = byLot.get(lot);
                if (current == null || (rq.getDate() != null
                        && (current.getDate() == null || rq.getDate().isAfter(current.getDate())))) {
                    byLot.put(lot, rq);
                }
            }
        }
        return new ArrayList<>(byLot.values());
    }

    @Override
    public List<Recquisition> findDistinctLotHeads(Produit p, String region) {
        List<Recquisition> source;
        String productId = p.getUid();
        if (region == null || region.isBlank()) {
            source = findRecquisitionByProduit(productId);
        } else {
            source = findRecquisitionByProduitRegion(productId, region);
        }
        Map<String, Recquisition> byLot = new HashMap<>();
        if (source != null) {
            for (Recquisition rq : source) {
                String lot = rq.getNumlot();
                if (lot == null || lot.isBlank()) {
                    continue;
                }
                Recquisition current = byLot.get(lot);
                if (current == null || (rq.getDate() != null
                        && (current.getDate() == null || rq.getDate().isAfter(current.getDate())))) {
                    byLot.put(lot, rq);
                }
            }
        }
        return new ArrayList<>(byLot.values());
    }

    private List<Recquisition> findWithInterval(LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.date BETWEEN ? AND ? AND s.region = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                    query.setParameter(1, d1.atStartOfDay());
                    query.setParameter(2, d2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, d1.atStartOfDay());
            query.setParameter(2, d2.atTime(23, 59, 59));
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void fixUndesiredRecqusitionOf(LocalDate d1, LocalDate d2, String region) {
        List<Recquisition> reqs = findWithInterval(d1, d2, region);
        for (Recquisition req : reqs) {
            List<PrixDeVente> pvs = findPricesFor(req.getUid());
            for (PrixDeVente pv : pvs) {
                if (pv.getUpdatedAt() == null) {
                    req.setDate(d1.minusDays(3).atStartOfDay());
                    req.setObservation("Auto-correct");
                    updateRecquisition(req);
                    break;
                }
                if (pv.getUpdatedAt().isBefore(req.getUpdatedAt())) {
                    req.setDate(pv.getUpdatedAt());
                    req.setObservation("Auto-correct");
                    updateRecquisition(req);
                    break;
                }
            }

        }
    }

    private void persistLotAggregates(List<StockAgregate> rows) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                for (StockAgregate row : rows) {
                    Produit p = row.getProductId();
                    boolean exists = isStockExists(p.getUid(), row.getNumlot(), row.getDate(), row.getDate());
                    if (!exists) {
                        em.persist(row);
                    } else {
                        StockAgregate stk = findStockAgregate(row.getUid());
                        stk.setSorties(row.getSorties());
                        stk.setCoutAchat(row.getCoutAchat());
                        stk.setEntrees(row.getEntrees());
                        stk.setFinalQuantity(row.getFinalQuantity());
                        stk.setInitialQuantity(row.getInitialQuantity());
                        stk.setNumlot(row.getNumlot());
                        em.merge(stk);
                    }
                }
                return rows.size();
            }).join();
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        for (StockAgregate row : rows) {
            Produit p = row.getProductId();
            boolean exists = isStockExists(p.getUid(), row.getNumlot(), row.getDate(), row.getDate());
            if (!exists) {
                ManagedSessionFactory.getEntityManager().persist(row);
            } else {
                StockAgregate stk = findStockAgregate(row.getUid());
                stk.setSorties(row.getSorties());
                stk.setCoutAchat(row.getCoutAchat());
                stk.setEntrees(row.getEntrees());
                stk.setFinalQuantity(row.getFinalQuantity());
                stk.setInitialQuantity(row.getInitialQuantity());
                stk.setNumlot(row.getNumlot());
                ManagedSessionFactory.getEntityManager().merge(stk);
            }

        }
        tx.commit();
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
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sb.toString(), Recquisition.class)
                    .setParameter(1, objId).getResultList());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM recquisition");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Long.class);
                Long dos = (Long) query.getSingleResult();
                return dos == null ? 0 : dos;
            });
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduit(String objId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ? ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, objId);
                query.setParameter(2, lot);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
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

            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
                query.setParameter(1, prodId);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduitRegion(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ORDER BY s.date DESC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                query.setParameter(2, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<StockAgregate> findAgregateDistinctlyByLot(String prod, String region) {
        try {
            Map<String, StockAgregate> result = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.product_id = ? AND s.region LIKE ? "
                    + "AND s.final_quantity > 0 AND s.destroyed = ? ORDER BY s.date_expiration ASC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                query.setParameter(1, prod);
                query.setParameter(2, region == null ? "%" : region).setParameter(3, Boolean.FALSE);
                List<StockAgregate> rst = query.getResultList();
                for (StockAgregate stockAgregate : rst) {
                    result.put(stockAgregate.getNumlot(), stockAgregate);
                }
                return List.copyOf(result.values());
            });
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
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                return query.getResultList();
            });
        } catch (Exception e) {

        }
        return result;
    }

    @Override
    public List<Recquisition> toFefoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.dateExpiry ASC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    public Compter findCompteForProduit(String puid, LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM compter p WHERE p.product_id =  ? AND p.date_count BETWEEN ? AND  ? AND p.region = ? ORDER BY p.date_count DESC LIMIT 1 ");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Compter.class);
                query.setParameter(1, puid);
                query.setParameter(2, dateDebut);
                query.setParameter(3, dateFin);
                query.setParameter(4, region);
                return (Compter) query.getSingleResult();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date ASC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                return query.getResultList();
            });
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
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                query.setParameter(2, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ORDER BY s.date ASC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                query.setParameter(2, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ORDER BY s.date DESC");
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
                query.setParameter(1, uid);
                query.setParameter(2, region);
                return query.getResultList();
            });
        } catch (NoResultException e) {
            return null;
        }
    }
    // end on region

    @Override
    public List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ?  AND s.region = ? ORDER BY date DESC ");
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
                    query.setParameter(2, region);
                    query.setParameter(3, numlot);
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
        try {
            return rupturesFromStockAggregate(findRuptureGroupsFromStockAggregate(null));
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Rupture> rupturesFromStockAggregate(List<Object[]> datas) {
        List<Rupture> result = new ArrayList<>();
        for (Object[] data : datas) {
            Rupture r = new Rupture();
            String uidP = String.valueOf(data[0]);
            Produit pro = ProduitDelegate.findProduit(uidP);
            if (pro == null) {
                continue;
            }
            r.setProduit(pro);
            String region = data[1] == null ? null : String.valueOf(data[1]);
            Recquisition seed = region == null || region.isBlank()
                    ? getLastEntry(uidP)
                    : getLastEntry(pro, region);
            double totalFinalPieces = data[2] == null ? 0d : ((Number) data[2]).doubleValue();
            Mesure mezr = seed != null && seed.getMesureId() != null
                    ? seed.getMesureId()
                    : findMinMesureForProduit(uidP);
            if (!isRuptureByAggregate(totalFinalPieces, seed, mezr)) {
                continue;
            }
            r.setMesure(mezr);
            r.setRegion(region);
            r.setUnitprice(resolveUnitCost(seed));
            if (data[3] instanceof LocalDateTime dateTime) {
                r.setDate(dateTime.toLocalDate().toString());
            } else if (data[3] instanceof Timestamp ts) {
                r.setDate(ts.toLocalDateTime().toLocalDate().toString());
            } else if (data[3] != null) {
                r.setDate(String.valueOf(data[3]));
            }
            Double alrt = seed == null ? 0d : seed.getStockAlert();
            r.setAlert(alrt);
            if (seed != null) {
                List<PrixDeVente> prices = findPricesFor(seed.getUid());
                if (!prices.isEmpty()) {
                    PrixDeVente pv = prices.get(0);
                    Mesure mp = MesureDelegate.findMesure(pv.getMesureId().getUid());
                    r.setSalePrice(pv.getPrixUnitaire() + " " + pv.getDevise() + "/" + mp.getDescription());
                }
            }
            List<Stocker> prox = StockerDelegate.findDescSortedByDateStock(uidP);
            String loc = (prox.isEmpty() ? "N/A" : prox.get(0).getLocalisation());
            r.setLocalisation(loc);
            r.setSelect(false);
            double quantContenu = mezr == null || mezr.getQuantContenu() == null || mezr.getQuantContenu() <= 0
                    ? 1d
                    : mezr.getQuantContenu();
            r.setQuant(BigDecimal.valueOf(totalFinalPieces / quantContenu).setScale(2, RoundingMode.HALF_EVEN)
                    .doubleValue());
            result.add(r);
        }
        return result;
    }

    private boolean isRuptureByAggregate(double totalFinalPieces, Recquisition seed, Mesure mesure) {
        if (totalFinalPieces <= 0) {
            return true;
        }
        if (seed == null || seed.getStockAlert() == null || seed.getStockAlert() <= 0) {
            return false;
        }
        double quantContenu = mesure == null || mesure.getQuantContenu() == null || mesure.getQuantContenu() <= 0
                ? 1d
                : mesure.getQuantContenu();
        double alertPieces = seed.getStockAlert() * quantContenu;
        return totalFinalPieces <= alertPieces;
    }

    private List<Object[]> findRuptureGroupsFromStockAggregate(String region) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.product_id, s.region, COALESCE(SUM(COALESCE(s.final_quantity, 0)), 0) AS total_final,
                       MAX(s.date) AS last_date
                FROM stock_agregate s
                WHERE s.num_lot IS NOT NULL AND s.destroyed = 'FALSE'
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND COALESCE(s2.region, '') = COALESCE(s.region, '')
                        AND s2.num_lot = s.num_lot AND s2.destroyed = s.destroyed
                  )
                """);
        if (region != null && !region.isBlank()) {
            sql.append(" AND s.region LIKE ? ");
        }
        sql.append("""
                GROUP BY s.product_id, s.region
                ORDER BY MAX(s.date) DESC
                """);
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sql.toString());
                if (region != null && !region.isBlank()) {
                    query.setParameter(1, region);
                }
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql.toString());
        if (region != null && !region.isBlank()) {
            query.setParameter(1, region);
        }
        return query.getResultList();
    }

    @Override
    public List<Rupture> findStockEnRupture(String region) {
        try {
            return rupturesFromStockAggregate(findRuptureGroupsFromStockAggregate(region));
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
                    + " s.observation,s.coutachat,s.mesure_id,s.product_id, s.deleted_at, s.updated_at FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.region = ?"
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
                    + "s.mesure_id,s.product_id, s.deleted_at, s.updated_at FROM recquisition s, mesure m WHERE s.mesure_id=m.uid  GROUP BY s.product_id, s.numlot");
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
                    + "s.coutachat,s.mesure_id,s.product_id, s.deleted_at, s.updated_at FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.region = ? AND s.date BETWEEN ? AND ?  GROUP BY s.product_id, s.numlot");
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
                    + "s.mesure_id,s.product_id, s.deleted_at, s.updated_at FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.date BETWEEN ? AND ? GROUP BY s.product_id, s.numlot");
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
        StockAgregate aggregate = findLatestStockAgregateByLot(puid, numlot);
        if (aggregate != null && aggregate.getFinalQuantity() != null) {
            return Math.max(0, aggregate.getFinalQuantity());
        }
        double en = sumRecqusitionByLotFrom(puid, numlot);
        double so = sumLigneventeByLotFrom(puid, numlot);
        double ret = sumRetourDepotByLot(puid, numlot);
        return en - so - ret;
    }

    @Override
    public double findRemainedInMagasinByLot(String puid, String numlot, String region) {
        StockAgregate aggregate = findLatestStockAgregateByLot(puid, numlot, region);
        if (aggregate != null && aggregate.getFinalQuantity() != null) {
            return Math.max(0, aggregate.getFinalQuantity());
        }
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
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, uid);
                    Object d = query.getSingleResult();
                    return d == null ? 0d : ((Number) d).doubleValue();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Object d = query.getSingleResult();
            return d == null ? 0d : ((Number) d).doubleValue();
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
        if (prod == null || prod.getUid() == null) {
            return null;
        }
        return synthesizeStockAggregateFromLots(findLatestLotRowsForProduct(prod.getUid(), today, today1, null), null, null);
    }

    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate otherDay, String region) {
        if (prod == null || prod.getUid() == null) {
            return null;
        }
        return synthesizeStockAggregateFromLots(findLatestLotRowsForProduct(prod.getUid(), today, otherDay, region),
                region, null);
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
        Recquisition r = getLastEntry(product, region);// getLastEntry(product);
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
        Recquisition r = getLastEntry(product, region);
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
                    PrixDeVente pvd = getExistingPricefor(r,
                            MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
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
            Recquisition r = getLastEntry(product, region);
            if (r == null) {
                r = getLastEntry(product.getUid());
            }

            if (aggreg != null && r != null) {
                Mesure mesure = (aggreg.getMesureId() == null) ? findMinMesureForProduit(product.getUid())
                        : aggreg.getMesureId();
                if (mesure != null) {
                    // AND must have a price
                    PrixDeVente pvd = getExistingPricefor(r,
                            MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
                    if (pvd != null) {
                        double qr = aggreg.getFinalQuantity();
                        // AND quantity must be > 0
                        if (qr > 0) {
                            double qw = BigDecimal.valueOf(qr / mesure.getQuantContenu())
                                    .setScale(2, RoundingMode.HALF_EVEN)
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
                    PrixDeVente pvd = getExistingPricefor(r,
                            MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
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
        return result;
    }

    @Override
    public List<ListViewItem> populateBy(String category, String region) {
        List<ListViewItem> result = new ArrayList<>();
        List<Produit> produits = getProduits(category);
        for (Produit product : produits) {
            StockAgregate aggreg = findLatestStockAgregate(product.getUid(), region);
            Recquisition r = getLastEntry(product, region);

            if (aggreg != null && r != null) {
                Mesure mesure = (aggreg.getMesureId() == null) ? findMinMesureForProduit(product.getUid())
                        : aggreg.getMesureId();
                if (mesure != null) {
                    PrixDeVente pvd = getExistingPricefor(r,
                            MesureDelegate.findAscSortedByQuantWithProduit(product.getUid()));
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
    public Recquisition getLastEntry(Produit prod, String region) {
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
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid  AND r.numlot = ?");
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
    public double sommeEntreeSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String lot, String region) {
        if (uid == null || lot == null || datedebut == null || datefin == null) {
            return 0;
        }
        if (region == null || region.isBlank()) {
            return sumBatchedRecqusitionFrom(uid, lot, datedebut, datefin);
        }
        return sumBatchedRecqusitionFrom(uid, lot, datedebut, datefin, region);
    }

    @Override
    public double sommeSortieSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String lot, String region) {
        if (uid == null || lot == null || datedebut == null || datefin == null) {
            return 0;
        }
        if (region == null || region.isBlank()) {
            return sumBatchedLigneventeFrom(uid, lot, datedebut, datefin);
        }
        return sumBatchedLigneventeFrom(uid, lot, datedebut, datefin, region);
    }

    public double stockInitialAlternative(String uid, LocalDate datedebut, String lot, String region) {
        LocalDate veuille = datedebut.minusDays(1);
        double entrees = sumBatchedRecqusitionFrom(uid, lot, LocalDate.of(2000, Month.JANUARY, 01), veuille, region);
        double sorties = sumBatchedLigneventeFrom(uid, lot, LocalDate.of(2000, Month.JANUARY, 01), veuille, region);
        return Math.max(0, (entrees - sorties));
    }

    @Override
    public double calculerStockInitialEnUnite(String uid, LocalDate datedebut, String lot, String region) {
        StringBuilder sbE = new StringBuilder();
        sbE.append("SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                + "WHERE r.product_id = :pid AND r.date < :date AND r.numlot = :lot AND r.region LIKE :regi AND r.mesure_id=m.uid");
        StringBuilder sbS = new StringBuilder();
        sbS.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = :pid AND s.numlot = :lot AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE :regi AND v.dateVente < :datefin)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double entrees = (Double) em.createNativeQuery(sbE.toString(),
                        Double.class).setParameter("pid", uid)
                        .setParameter("date", Timestamp.valueOf(datedebut.atStartOfDay()))
                        .setParameter("regi", region)
                        .setParameter("lot", lot)
                        .getSingleResult();
                Double sorties = (Double) em.createNativeQuery(sbS.toString(), Double.class)
                        .setParameter("pid", uid)
                        .setParameter("lot", lot)
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
                .setParameter("lot", lot)
                .getSingleResult();

        Double sorties = (Double) ManagedSessionFactory.getEntityManager().createNativeQuery(sbS.toString(),
                Double.class).setParameter("pid", uid)
                .setParameter("datefin", datedebut.atStartOfDay())
                .setParameter("lot", lot)
                .setParameter("regi", region)
                .getSingleResult();
        double stok = (entrees == null ? 0 : entrees) - (sorties == null ? 0 : sorties);
        return stok <= 0 ? 0 : stok;
    }

    @Override
    public double getStockExpiree(String uid, LocalDate datedebut, LocalDate datefin, String lot, String region) {
        if (uid == null || lot == null || lot.isBlank() || datedebut == null || datefin == null || region == null
                || region.isBlank()) {
            return 0;
        }
        return getStockExpireeByLot(uid, lot, datedebut, datefin, region);
    }

    @Override
    public boolean cloturerStocks(String region, LocalDate datedebut, LocalDate datefin, String context) {
        // Cloture journaliere: produit + lot + region.
        MemoryGuard.newSingleThreadExecutor("kazisafe-cloture-stocks")
                .submit(() -> ManagedSessionFactory.runWithCleanup(() -> {

                    LocalDate targetDay = datefin == null ? LocalDate.now() : datefin;
                    LocalDate dEffDeb = datedebut == null ? targetDay : datedebut;
                    LocalDate dEffFin = datefin == null ? targetDay : datefin;
                    String targetContext = (context == null || context.isBlank())
                            ? "Journalier du " + targetDay
                            : context;
                    List<Produit> produits = getProduits();
                    for (Produit produit : produits) {
                        Map<String, Recquisition> seeds = findLatestSeedsPerRegion(produit, region);
                        if (seeds.isEmpty()) {
                            continue;
                        }
                        int index = produits.indexOf(produit);
                        for (Recquisition seed : seeds.values()) {
                            if (seed != null) {
                                saveStockFromRecquisition(seed, dEffDeb, dEffFin, targetContext);
                            }
                        }
                        notifyCallback(index, produits.size(), produit);
                    }
                    notifyClotureFinish(produits.size());
                }));
        return true;
    }

    public double findOnlyStockUnits(Produit produit, String lot, String region, LocalDate datedebut, LocalDate datefin) {
        if (produit == null || produit.getUid() == null) {
            return 0;
        }
        return computeLotPeriodPieces(produit.getUid(), lot, region, datedebut, datefin).finalValid();
    }

    /**
     * Met à jour uniquement les lignes par lot avec la même agrégation que
     * {@link #saveStockFromRecquisition} avec région.
     */
    public void cloturons(Produit produit, String lot, String region, LocalDate datedebut, LocalDate datefin,
            String cloture_context) {
        if (produit == null || produit.getUid() == null || region == null || region.isBlank() || datedebut == null
                || datefin == null) {
            return;
        }
        Mesure unite = MesureDelegate.findByProduitAndQuant(produit.getUid(), 1d);
        LotClosureTotals totals = upsertLotStockAggregates(produit, region, datedebut, datefin, cloture_context, unite);

        List<Recquisition> desc = findDescSortedByDateForProduit(produit.getUid());
        if (desc == null || desc.isEmpty()) {
            return;
        }
        Recquisition get = desc.get(0);
        System.out.println("Dernier ---- rq " + get + " lot_param=" + lot + " synthese pieces finales=" + totals.finalQty);
        System.out.println("Cloture lot-agregate " + produit.getNomProduit() + " " + totals.finalQty
                + " sans synthese globale persistante");
        System.out.println("Stock agreagator " + produit.getNomProduit() + " synthese=" + totals.finalQty);
    }

    /**
     * Reconstruit les agrégats de stock où le numéro de lot est null, en
     * utilisant la même logique de remplissage des quantités en unité.
     */
    public void backfillNullLotAggregates() {
        System.out.println("Lancement du backfilling avec cloture a la veille...");
        try {
            List<StockAgregate> nullLots;
            String jpql = "SELECT s FROM StockAgregate s WHERE s.numlot IS NULL";
            if (ManagedSessionFactory.isEmbedded()) {
                nullLots = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, StockAgregate.class).getResultList());
            } else {
                nullLots = ManagedSessionFactory.getEntityManager().createQuery(jpql, StockAgregate.class).getResultList();
            }

            if (nullLots == null || nullLots.isEmpty()) {
                System.out.println("Aucun agrégat avec numlot null trouvé.");
                return;
            }
            LocalDate ledger = LocalDate.of(1970, 1, 1);
            int count = 0;

            for (StockAgregate oldAgg : nullLots) {
                Produit produit = oldAgg.getProductId();
                String region = oldAgg.getRegion();
                LocalDate aggDate = oldAgg.getDate();
                if (produit == null || aggDate == null) {
                    continue;
                }

                LocalDate veille = aggDate.minusDays(1);

                // 1. Trouver le lot en question
                String lotQ = "SELECT * FROM Recquisition r WHERE r.product_id = :pid AND r.date <= :dt ORDER BY r.date DESC LIMIT 1";
                List<data.Recquisition> recqs = ManagedSessionFactory.executeRead(em
                        -> em.createNativeQuery(lotQ, data.Recquisition.class)
                                .setParameter("pid", produit.getUid())
                                .setParameter("dt", aggDate.atStartOfDay())
                                .getResultList()
                );

                if (recqs.isEmpty()) {
                    continue;
                }
                data.Recquisition recq = recqs.get(0);
                String lot = recq.getNumlot();
                if (lot == null || lot.trim().isEmpty()) {
                    continue;
                }

                // 2. Compute date of expiration
                LocalDate exp = recq.getDateExpiry();
                if (exp == null) {
                    String prevQ = "SELECT * FROM Recquisition r WHERE r.product_id = :pid AND r.dateExpiry IS NOT NULL AND r.date < :dt ORDER BY r.date DESC LIMIT 1";
                    List<data.Recquisition> prevs = ManagedSessionFactory.executeRead(em
                            -> em.createNativeQuery(prevQ, data.Recquisition.class)
                                    .setParameter("pid", produit.getUid())
                                    .setParameter("dt", recq.getDate())
                                    .getResultList()
                    );
                    if (!prevs.isEmpty()) {
                        LocalDate prevExp = prevs.get(0).getDateExpiry();
                        if (prevExp != null) {
                            exp = prevExp.plusYears(5);
                        }
                    }
                }

                // 3. Date de derniere sortie ou derniere entree (jusqu'a la veille)
                String sortieQ = "SELECT MAX(l.reference.dateVente) FROM LigneVente l WHERE l.productId.uid = :pid AND l.numlot = :lot AND l.reference.dateVente <= :dt";
                LocalDateTime lastSortie = ManagedSessionFactory.executeRead(em -> {
                    try {
                        java.sql.Timestamp t = em.createQuery(sortieQ, java.sql.Timestamp.class)
                                .setParameter("pid", produit.getUid())
                                .setParameter("lot", lot)
                                .setParameter("dt", new java.sql.Timestamp(veille.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()).toLocalDateTime())
                                .getSingleResult();
                        return t != null ? t.toLocalDateTime() : null;
                    } catch (Exception e) {
                        return null;
                    }
                });

                LocalDate newDate = null;
                if (lastSortie != null) {
                    newDate = lastSortie.toLocalDate();
                }
                if (newDate == null) {
                    String entreeQ = "SELECT MAX(r.date) FROM Recquisition r WHERE r.productId.uid = :pid AND r.numlot = :lot AND r.date <= :dt";
                    LocalDateTime lastEntree = ManagedSessionFactory.executeRead(em -> {
                        try {
                            java.sql.Timestamp t = em.createQuery(entreeQ, java.sql.Timestamp.class)
                                    .setParameter("pid", produit.getUid())
                                    .setParameter("lot", lot)
                                    .setParameter("dt", new java.sql.Timestamp(veille.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()).toLocalDateTime())
                                    .getSingleResult();
                            return t != null ? t.toLocalDateTime() : null;
                        } catch (Exception e) {
                            return null;
                        }
                    });
                    if (lastEntree != null) {
                        newDate = lastEntree.toLocalDate();
                    }
                }

                // 4. Cloturer à la veille (LEDGER à la veille)
                LotPeriodPieces pieces = computeLotPeriodPieces(produit.getUid(), lot, region, ledger, veille);

                oldAgg.setNumlot(lot);
                oldAgg.setDateExpiration(exp);
                if (newDate != null) {
                    oldAgg.setDate(newDate);
                }

                oldAgg.setInitialQuantity(0d);
                oldAgg.setEntrees(pieces.entrees());
                oldAgg.setSorties(pieces.sorties());
                oldAgg.setExpiree(pieces.expiree());
                oldAgg.setFinalQuantity(pieces.finalValid());

                if (ManagedSessionFactory.isEmbedded()) {
                    final StockAgregate mergeAgg = oldAgg;
                    ManagedSessionFactory.submitWrite(em -> {
                        em.merge(mergeAgg);
                        return null;
                    }).join();
                } else {
                    EntityManager em = ManagedSessionFactory.getEntityManager();
                    EntityTransaction tx = em.getTransaction();
                    tx.begin();
                    em.merge(oldAgg);
                    tx.commit();
                }
                count++;
                if (count % 5 == 0) {
                    System.out.println(count + " agrégats backfillés à la veille...");
                }
            }
            System.out.println("Backfilling terminé avec succès. " + count + " agrégats remplacés.");
        } catch (Exception ex) {
            Logger.getLogger(RecquisitionService.class.getName()).log(Level.SEVERE, "Erreur lors du backfilling", ex);
        }
    }

    @Override
    public void rectifyStock(Produit produit, LocalDate datedebut, LocalDate datefin, String region, String numlot) {
        rectifyStockInternal(produit, datedebut, datefin, region, numlot);
    }

    /**
     * Implémentation centrale de la rectification du stock agrégé.
     * <p>
     * Quatre cas selon la présence de {@code region} et de {@code numlot} :
     * <ol>
     * <li><b>région + lot</b> – upsert du seul lot dans cette région via
     * {@link #upsertSingleLotStockAggregate}.</li>
     * <li><b>région sans lot</b> – tous les lots de la région via
     * {@link #upsertLotStockAggregates}.</li>
     * <li><b>pas de région + lot</b> – upsert global du seul lot via
     * {@link #upsertSingleLotStockAggregateNoRegion}.</li>
     * <li><b>pas de région, pas de lot</b> – comportement original :
     * {@link #saveStockFromRecquisition} sur la dernière réquisition (tous
     * lots).</li>
     * </ol>
     */
    private void rectifyStockInternal(Produit produit, LocalDate datedebut, LocalDate datefin,
            String region, String numlot) {
        if (produit == null || produit.getUid() == null || region == null || region.isBlank()) {
            System.out.println("produit/region est null pro " + produit + " region = " + region);
            return;
        }
        List<Recquisition> lseed = findRecquisitionByProduit(produit.getUid(), numlot, region);

        Recquisition seed = lseed.isEmpty() ? null : lseed.getFirst();

        if (seed == null) {
            System.out.println("Recquis seed single est null " + seed);
            return;
        }
        System.out.println("Lot entrant single lot param " + numlot + " lot req sortant ->" + seed.getNumlot());
        StockAgregate sa = saveStockFromRecquisition(seed, numlot, datedebut, datefin);
        System.out.println("le stock agregate single processed = " + sa.getNumlot() + " -> " + sa.getProductId().getNomProduit() + " -> " + sa.getFinalQuantity());

    }

    /**
     * Upsert d'un seul lot dans {@code stock_agregate} pour une région et une
     * période données. Suit exactement la même formule que
     * {@link #upsertLotStockAggregates} mais se limite au lot {@code lotNorm} :
     * calcul via {@link #computeLotPeriodPieces} (stock initial, entrées,
     * sorties, périmés, final), puis {@code persist} si la ligne est nouvelle
     * ou {@code merge} sinon.
     */
    private void upsertSingleLotStockAggregate(Produit produit, String region, LocalDate datedebut,
            LocalDate datefin, String clotureContext, Mesure unite, String lotNorm, double fallbackUnitCost) {
        LotPeriodPieces m = computeLotPeriodPieces(produit.getUid(), lotNorm, region, datedebut, datefin);

        // Réquisition représentative (la plus récente) pour récupérer coût et date de péremption
        List<Recquisition> lotRecqs = findRecquisitionByProduit(produit.getUid(), lotNorm, region);
        double coutAch = 0d;
        LocalDate dateExp = null;
        if (lotRecqs != null && !lotRecqs.isEmpty()) {
            Recquisition rep = lotRecqs.stream()
                    .filter(r -> r.getDate() != null)
                    .max((a, b) -> a.getDate().compareTo(b.getDate()))
                    .orElse(lotRecqs.get(0));
            coutAch = resolveUnitCost(rep);
            dateExp = rep.getDateExpiry();
        }
        if (coutAch <= 0 && fallbackUnitCost > 0) {
            coutAch = fallbackUnitCost;
        }

        LocalDate dte = datefin.equals(LocalDate.now()) ? LocalDate.now() : datefin;
        StockAgregate lotStock = findClosedStockByLot(
                datedebut, datefin, produit.getUid(), region, lotNorm, clotureContext);
        boolean isNew = lotStock == null;
        if (isNew) {
            lotStock = new StockAgregate(DataId.generate());
        }
        lotStock = applyStockAggregateValues(lotStock, produit, unite, region, clotureContext, lotNorm,
                resolveLotDateExpirationForStockAgregate(produit.getUid(), lotNorm, region, dateExp),
                dte, coutAch, m.stockInitial(), m.entrees(), m.sorties(), m.expiree(), m.finalValid());
        persistOrMergeStockAgregate(lotStock, isNew);
        System.out.println("rectifyStock lot-upsert (region=" + region + "): produit="
                + produit.getNomProduit() + ", lot=" + lotNorm + ", final=" + m.finalValid());
    }

    /**
     * Upsert d'un seul lot dans {@code stock_agregate} sans filtre de région
     * (grand livre global). Applique {@link #computeLotPeriodPiecesNoRegion}
     * puis purge la ligne existante avant d'insérer la nouvelle valeur
     * calculée.
     */
    private void upsertSingleLotStockAggregateNoRegion(Produit produit, LocalDate datedebut,
            LocalDate datefin, String clotureContext, Mesure unite, String lotNorm, double fallbackUnitCost) {
        LotPeriodPieces m = computeLotPeriodPiecesNoRegion(produit.getUid(), lotNorm, datedebut, datefin);

        List<Recquisition> lotRecqs = findRecquisitionByProduit(produit.getUid(), lotNorm);
        double coutAch = 0d;
        LocalDate dateExp = null;
        if (lotRecqs != null && !lotRecqs.isEmpty()) {
            Recquisition rep = lotRecqs.stream()
                    .filter(r -> r.getDate() != null)
                    .max((a, b) -> a.getDate().compareTo(b.getDate()))
                    .orElse(lotRecqs.get(0));
            coutAch = resolveUnitCost(rep);
            dateExp = rep.getDateExpiry();
        }
        if (coutAch <= 0 && fallbackUnitCost > 0) {
            coutAch = fallbackUnitCost;
        }

        LocalDate dte = datefin.equals(LocalDate.now()) ? LocalDate.now() : datefin;
        // Purge la ligne existante pour ce lot (sans filtre région) avant de réinsérer
        purgeSingleLotAggregate(produit.getUid(), null, datedebut, datefin, clotureContext, lotNorm);
        StockAgregate lotAggregate = applyStockAggregateValues(new StockAgregate(DataId.generate()),
                produit, unite, null, clotureContext, lotNorm,
                resolveLotDateExpirationForStockAgregate(produit.getUid(), lotNorm, null, dateExp),
                dte, coutAch, m.stockInitial(), m.entrees(), m.sorties(), m.expiree(), m.finalValid());
        persistLotAggregates(List.of(lotAggregate));
        System.out.println("rectifyStock lot-upsert (no-region): produit="
                + produit.getNomProduit() + ", lot=" + lotNorm + ", final=" + m.finalValid());
    }

    /**
     * Supprime la ligne {@code stock_agregate} correspondant exactement au
     * triplet produit / lot / période pour le contexte de clôture donné. Si
     * {@code region} est null ou vide, aucun filtre région n'est appliqué
     * (suppression globale pour ce lot).
     */
    private void purgeSingleLotAggregate(String productId, String region, LocalDate dayDebut,
            LocalDate dayFin, String context, String numlot) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM stock_agregate WHERE product_id = ? ");
        sb.append("AND date BETWEEN ? AND ? AND context = ? AND num_lot = ? AND destroyed = 'FALSE' ");
        if (region != null && !region.isBlank()) {
            sb.append("AND region = ? ");
        }
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                Query q = em.createNativeQuery(sb.toString());
                q.setParameter(1, productId);
                q.setParameter(2, dayDebut);
                q.setParameter(3, dayFin.plusDays(1));
                q.setParameter(4, context);
                q.setParameter(5, numlot);
                if (region != null && !region.isBlank()) {
                    q.setParameter(6, region);
                }
                q.executeUpdate();
                return true;
            }).join();
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        Query q = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        q.setParameter(1, productId);
        q.setParameter(2, dayDebut);
        q.setParameter(3, dayFin.plusDays(1));
        q.setParameter(4, context);
        q.setParameter(5, numlot);
        if (region != null && !region.isBlank()) {
            q.setParameter(6, region);
        }
        q.executeUpdate();
        tx.commit();
    }

    /**
     * Factorise la persistance ou la fusion d'un {@link StockAgregate}
     * unitaire, en respectant le mode embarqué ou non.
     */
    private void persistOrMergeStockAgregate(StockAgregate lotStock, boolean isNew) {
        if (ManagedSessionFactory.isEmbedded()) {
            StockAgregate finalRef = lotStock;
            if (isNew) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.persist(finalRef);
                    return finalRef;
                });
            } else {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(finalRef);
                    return finalRef;
                });
            }
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        if (isNew) {
            ManagedSessionFactory.getEntityManager().persist(lotStock);
        } else {
            ManagedSessionFactory.getEntityManager().merge(lotStock);
        }
        tx.commit();
    }

    public void cloturerUnProduit(Produit produit, String region, LocalDate datedebut, LocalDate datefin) {
        List<Recquisition> rqs = findRecquisitionByProduitRegion(produit.getUid(), region);
        for (Recquisition rq : rqs) {
            clotureStockProduit(produit, rq.getNumlot(), region, datedebut, datefin, null);
        }
    }

    @Override
    public void clotureStockProduit(Produit produit, String lot, String region, LocalDate datedebut, LocalDate datefin,
            String cloture_context) {
        if (produit == null || produit.getUid() == null || region == null || region.isBlank()) {
            System.out.println("produit/region est null pro " + produit + " region = " + region);
            return;
        }
        List<Recquisition> lseed = findRecquisitionByProduit(produit.getUid(), lot, region);

        Recquisition seed = lseed.isEmpty() ? null : lseed.getFirst();

        if (seed == null) {
            System.out.println("Recquis seed est null " + seed);
            return;
        }
        System.out.println("Lot entrant param " + lot + " lot req sortant ->" + seed.getNumlot());
        StockAgregate sa = saveStockFromRecquisition(seed, datedebut, datefin, cloture_context);
        System.out.println("le stock agregate processed = " + sa.getNumlot() + " -> " + sa.getProductId().getNomProduit() + " -> " + sa.getFinalQuantity());
    }

    /**
     * Une ligne {@link StockAgregate} par combinaison (produit, numéro de lot,
     * région, contexte de clôture, fenêtre de dates) : les quantités agrégées
     * (initial, entrées, sorties, périmé, final) correspondent à ce lot sur la
     * période.
     */
    private LotClosureTotals upsertLotStockAggregates(Produit produit, String region, LocalDate datedebut,
            LocalDate datefin, String clotureContext, Mesure unite) {
        return upsertLotStockAgregate(produit, region, datedebut, datefin, clotureContext, unite, 0d);
    }

    private LotClosureTotals upsertLotStockAgregate(Produit produit, String region, LocalDate datedebut,
            LocalDate datefin, String clotureContext, Mesure unite, double fallbackUnitCost) {
        LotClosureTotals totals = new LotClosureTotals();
        if (produit == null || region == null || datedebut == null || datefin == null) {
            return totals;
        }
        List<Recquisition> distinctLots = findDistinctLotsForProduitRegion(produit.getUid(), region);
        if (distinctLots.isEmpty()) {
            return totals;
        }
        LocalDate dte = datefin.equals(LocalDate.now()) ? LocalDate.now() : datefin;
        for (Recquisition lotRecq : distinctLots) {
            String lot = lotRecq.getNumlot();
            if (lot == null || lot.isBlank()) {
                continue;
            }
            String lotNorm = lot.trim();
            LotPeriodPieces m = computeLotPeriodPieces(produit.getUid(), lotNorm, region, datedebut, datefin);
            totals.add(m.entrees(), m.sorties(), m.stockInitial(), m.expiree(), m.finalValid());
            double stockFinalValid = m.finalValid();
            double coutAch = resolveUnitCost(lotRecq);
            if (coutAch <= 0 && fallbackUnitCost > 0) {
                coutAch = fallbackUnitCost;
            }
            StockAgregate lotStock = findClosedStockByLot(datedebut, datefin, produit.getUid(), region, lotNorm,
                    clotureContext);
            boolean exists = (lotStock != null);
            if (!exists) {
                lotStock = new StockAgregate(DataId.generate());
            }
            lotStock = applyStockAggregateValues(lotStock,
                    produit,
                    unite,
                    region,
                    clotureContext,
                    lotNorm,
                    resolveLotDateExpirationForStockAgregate(produit.getUid(), lotNorm, region, lotRecq.getDateExpiry()),
                    dte,
                    coutAch,
                    m.stockInitial(),
                    m.entrees(),
                    m.sorties(),
                    m.expiree(),
                    stockFinalValid);
            if (ManagedSessionFactory.isEmbedded()) {
                StockAgregate finalLotStock = lotStock;
                if (!exists) {
                    ManagedSessionFactory.submitWrite(em -> {
                        em.persist(finalLotStock);
                        return finalLotStock;
                    });
                } else {
                    ManagedSessionFactory.submitWrite(em -> {
                        em.merge(finalLotStock);
                        return finalLotStock;
                    });
                }
            } else {
                EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                if (!tx.isActive()) {
                    tx.begin();
                }
                if (!exists) {
                    // System.out.println("Persisting stock...");
                    ManagedSessionFactory.getEntityManager().persist(lotStock);
                } else {
                    // System.out.println("Merging stock....");
                    ManagedSessionFactory.getEntityManager().merge(lotStock);
                }
                tx.commit();
            }
        }
        return totals;
    }

    private LotClosureTotals upsertOneLotStockAgregate(Recquisition seed, String region, LocalDate datedebut,
            LocalDate datefin, String lot, Mesure unite, double fallbackUnitCost) {
        LotClosureTotals totals = new LotClosureTotals();
        Produit produit = seed.getProductId();
        if (produit == null || region == null || datedebut == null || datefin == null) {
            return totals;
        }

        LocalDate dte = datefin.equals(LocalDate.now()) ? LocalDate.now() : datefin;
        if (lot == null || lot.isBlank()) {
            return totals;
        }
        String lotNorm = lot.trim();
        LocalDate leo = LocalDate.now();
        LotPeriodPieces m = computeLotPeriodPieces(produit.getUid(), lotNorm, region, leo, leo);
        totals.add(m.entrees(), m.sorties(), m.stockInitial(), m.expiree(), m.finalValid());
        double stockFinalValid = m.finalValid();
        double coutAch = resolveUnitCost(seed);
        if (coutAch <= 0 && fallbackUnitCost > 0) {
            coutAch = fallbackUnitCost;
        }

        StockAgregate lotStock = findClosedStockByLot(datedebut, datefin, produit.getUid(), region, lotNorm,
                null);
        boolean exists = (lotStock != null);
        if (!exists) {
            lotStock = new StockAgregate(DataId.generate());
        }
        lotStock = applyStockAggregateValues(lotStock,
                produit,
                unite,
                region,
                "Journalier du " + dte,
                lotNorm,
                resolveLotDateExpirationForStockAgregate(produit.getUid(), lotNorm, region, seed.getDateExpiry()),
                dte,
                coutAch,
                m.stockInitial(),
                m.entrees(),
                m.sorties(),
                m.expiree(),
                stockFinalValid);
        if (ManagedSessionFactory.isEmbedded()) {
            StockAgregate finalLotStock = lotStock;
            if (!exists) {
                ManagedSessionFactory.submitWrite(em -> {
                    em.persist(finalLotStock);
                    return finalLotStock;
                });
            } else {
                ManagedSessionFactory.submitWrite(em -> {
                    em.merge(finalLotStock);
                    return finalLotStock;
                });
            }
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            if (!exists) {
                // System.out.println("Persisting stock...");
                ManagedSessionFactory.getEntityManager().persist(lotStock);
            } else {
                // System.out.println("Merging stock....");
                ManagedSessionFactory.getEntityManager().merge(lotStock);
            }
            tx.commit();
        }

        return totals;
    }

    public List<Recquisition> findDistinctLotsForProduitRegion(String productId, String region) {
        List<Recquisition> recquisitions = getSortedAccordingToInventoryMethodAt(productId, region);
        if (recquisitions == null || recquisitions.isEmpty()) {
            return List.of();
        }
        Map<String, Recquisition> lotMap = new HashMap<>();
        for (Recquisition recq : recquisitions) {
            if (recq == null || recq.getNumlot() == null || recq.getNumlot().isBlank()) {
                continue;
            }
            Recquisition existing = lotMap.get(recq.getNumlot());
            if (existing == null || (recq.getDate() != null
                    && (existing.getDate() == null || recq.getDate().isAfter(existing.getDate())))) {
                lotMap.put(recq.getNumlot(), recq);
            }
        }
        return new ArrayList<>(lotMap.values());
    }

    private double calculerStockInitialEnUniteByLot(String uid, String numlot, LocalDate datedebut, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.product_id = ? AND "
                    + "s.region LIKE ? AND s.num_lot = ? AND s.date < ? AND s.destroyed = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> rows = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, uid)
                            .setParameter(2, region)
                            .setParameter(3, numlot)
                            .setParameter(4, datedebut).setParameter(5, Boolean.FALSE)
                            .setMaxResults(1)
                            .getResultList();
                    if (!rows.isEmpty()) {
                        Double q = rows.get(0).getFinalQuantity();
                        return q == null ? 0 : Math.max(0, q);
                    }
                    return 0d;
                });
            }
            List<StockAgregate> rows = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, uid)
                    .setParameter(2, region)
                    .setParameter(3, numlot)
                    .setParameter(4, datedebut).setParameter(5, Boolean.FALSE)
                    .setMaxResults(1)
                    .getResultList();
            if (!rows.isEmpty()) {
                Double q = rows.get(0).getFinalQuantity();
                return q == null ? 0 : Math.max(0, q);
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private double getStockExpireeByLot(String uid, String numlot, LocalDate datedebut, LocalDate datefin,
            String region) {
        try {
            StringBuilder sbE = new StringBuilder();
            sbE.append(
                    "SELECT SUM(COALESCE(r.quantite, 0)*COALESCE(m.quantcontenu, 0)) piece FROM recquisition r,mesure m "
                    + "WHERE r.product_id = ?1 AND r.numlot = ?2 AND r.dateexpiry BETWEEN ?3 AND ?4 "
                    + "AND r.region LIKE ?5 AND r.mesure_id=m.uid");
            StringBuilder sbS = new StringBuilder();
            sbS.append(
                    "SELECT SUM(COALESCE(s.quantite,0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m "
                    + "WHERE s.product_id = ?1 AND s.numlot = ?2 AND s.mesure_id=m.uid AND s.reference_uid IN "
                    + "(SELECT v.uid FROM vente v WHERE v.region LIKE ?5 AND v.dateVente BETWEEN ?3 AND ?4)");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Double entreesExp = (Double) em.createNativeQuery(sbE.toString(), Double.class)
                            .setParameter(1, uid)
                            .setParameter(2, numlot)
                            .setParameter(3, Timestamp.valueOf(datedebut.atStartOfDay()))
                            .setParameter(4, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                            .setParameter(5, region)
                            .getSingleResult();
                    Double sortiesExp = (Double) em.createNativeQuery(sbS.toString(), Double.class)
                            .setParameter(1, uid)
                            .setParameter(2, numlot)
                            .setParameter(3, Timestamp.valueOf(datedebut.atStartOfDay()))
                            .setParameter(4, Timestamp.valueOf(datefin.atTime(23, 59, 59)))
                            .setParameter(5, region)
                            .getSingleResult();
                    double diff = (entreesExp == null ? 0 : entreesExp) - (sortiesExp == null ? 0 : sortiesExp);
                    return diff <= 0 ? 0 : diff;
                });
            }
            Double entreesExp = (Double) ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sbE.toString(), Double.class)
                    .setParameter(1, uid)
                    .setParameter(2, numlot)
                    .setParameter(3, datedebut.atStartOfDay())
                    .setParameter(4, datefin.atTime(23, 59, 59))
                    .setParameter(5, region)
                    .getSingleResult();
            Double sortiesExp = (Double) ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sbS.toString(), Double.class)
                    .setParameter(1, uid)
                    .setParameter(2, numlot)
                    .setParameter(3, datedebut.atStartOfDay())
                    .setParameter(4, datefin.atTime(23, 59, 59))
                    .setParameter(5, region)
                    .getSingleResult();
            double diff = (entreesExp == null ? 0 : entreesExp) - (sortiesExp == null ? 0 : sortiesExp);
            return diff <= 0 ? 0 : diff;
        } catch (Exception e) {
            return 0;
        }
    }

    //
    /**
     * Ligne d’agrégat lot pour la même clôture que l’écriture : produit, lot,
     * région, période (borne {@code date}), et {@code context} (ex. «
     * Journalier du … »).
     */
    @Override
    public StockAgregate findClosedStockByLot(LocalDate today, LocalDate today1, String uid, String region,
            String numlot, String context) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? "
                    + "AND s.product_id = ? AND s.num_lot = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, today)
                            .setParameter(2, today1.plusDays(1))
                            .setParameter(3, region)
                            .setParameter(4, uid)
                            .setParameter(5, numlot)
                            .setMaxResults(1)
                            .getResultList();
                    return results.isEmpty() ? null : results.get(0);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, today)
                    .setParameter(2, today1.plusDays(1))
                    .setParameter(3, region)
                    .setParameter(4, uid)
                    .setParameter(5, numlot)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    //
    private StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid, String region,
            String cloture_type) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? ");
        sb.append("AND s.product_id = ? AND s.num_lot IS NOT NULL AND s.context = ? AND s.destroyed = ? ORDER BY s.date DESC");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, today)
                            .setParameter(2, today1.plusDays(1))
                            .setParameter(3, region)
                            .setParameter(4, uid)
                            .setParameter(5, cloture_type).setParameter(6, Boolean.FALSE)
                            .getResultList();
                    return synthesizeStockAggregateFromLots(results, region, cloture_type);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, today)
                    .setParameter(2, today1.plusDays(1))
                    .setParameter(3, region)
                    .setParameter(4, uid)
                    .setParameter(5, cloture_type).setParameter(6, Boolean.FALSE)
                    .getResultList();
            return synthesizeStockAggregateFromLots(results, region, cloture_type);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public StockAgregate findClosedStock(LocalDate today, LocalDate today1, String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.date BETWEEN ? AND ? ");
            sb.append("AND s.product_id = ? AND s.num_lot IS NOT NULL AND s.destroyed = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, today)
                            .setParameter(2, today1.plusDays(1))
                            .setParameter(3, uid).setParameter(4, Boolean.FALSE)
                            .getResultList();
                    return synthesizeStockAggregateFromLots(results, null, null);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, today)
                    .setParameter(2, today1.plusDays(1))
                    .setParameter(3, uid).setParameter(4, Boolean.FALSE)
                    .getResultList();
            return synthesizeStockAggregateFromLots(results, null, null);
        } catch (Exception ex) {
            return null;
        }
    }

    private StockAgregate findLatestStockAgregate(String productId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.product_id = ? AND s.num_lot IS NOT NULL AND s.destroyed = ? ");
            sb.append("AND s.date = (");
            sb.append("SELECT MAX(s2.date) FROM stock_agregate s2 ");
            sb.append("WHERE s2.product_id = s.product_id ");
            sb.append("AND COALESCE(s2.region, '') = COALESCE(s.region, '') ");
            sb.append("AND s2.num_lot = s.num_lot AND s2.destroyed=s.destroyed ) ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, productId).setParameter(2, Boolean.FALSE)
                            .getResultList();
                    return synthesizeStockAggregateFromLots(results, null, null);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, productId).setParameter(2, Boolean.FALSE)
                    .getResultList();
            return synthesizeStockAggregateFromLots(results, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    private StockAgregate findLatestStockAgregate(String productId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.product_id = ? AND s.region LIKE ? ");
            sb.append("AND s.num_lot IS NOT NULL AND s.destroyed = ? AND s.date = (");
            sb.append("SELECT MAX(s2.date) FROM stock_agregate s2 ");
            sb.append("WHERE s2.product_id = s.product_id AND s2.region = s.region AND s2.num_lot = s.num_lot AND s2.destroyed = s.destroyed)");
            sb.append(" ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, productId)
                            .setParameter(2, region).setParameter(3, Boolean.FALSE)
                            .getResultList();
                    return synthesizeStockAggregateFromLots(results, region, null);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, region).setParameter(3, Boolean.FALSE)
                    .getResultList();
            return synthesizeStockAggregateFromLots(results, region, null);
        } catch (Exception e) {
            return null;
        }
    }

    private StockAgregate findLatestStockAgregateByLot(String productId, String numlot) {
        if (numlot == null || numlot.isBlank()) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate WHERE product_id = ? AND num_lot = ? AND destroyed = ? ORDER BY date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, productId)
                            .setParameter(2, numlot).setParameter(3, Boolean.FALSE)
                            .setMaxResults(1)
                            .getResultList();
                    return results.isEmpty() ? null : results.get(0);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, numlot).setParameter(3, Boolean.FALSE)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private StockAgregate findLatestStockAgregateByLot(String productId, String numlot, String region) {
        if (numlot == null || numlot.isBlank()) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM stock_agregate WHERE product_id = ? AND num_lot = ? AND region LIKE ? AND destroyed = ? ORDER BY date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    List<StockAgregate> results = em.createNativeQuery(sb.toString(), StockAgregate.class)
                            .setParameter(1, productId)
                            .setParameter(2, numlot)
                            .setParameter(3, region).setParameter(4, Boolean.FALSE)
                            .setMaxResults(1)
                            .getResultList();
                    return results.isEmpty() ? null : results.get(0);
                });
            }
            List<StockAgregate> results = ManagedSessionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, numlot)
                    .setParameter(3, region).setParameter(4, Boolean.FALSE)
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

    public boolean isStockExists(String puid, String lot, LocalDate debut, LocalDate fin) {
        String jpql = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END "
                + "FROM stock_agregate c WHERE c.product_id = ? AND c.num_lot = ? "
                + "AND c.date BETWEEN ? AND ? AND c.destroyed = ?";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> (Boolean) em.createNativeQuery(jpql, Boolean.class)
                    .setParameter(1, puid).setParameter(2, lot)
                    .setParameter(3, debut).setParameter(4, fin.plusDays(1)).setParameter(5, Boolean.FALSE)
                    .getSingleResult());
        }
        return (Boolean) ManagedSessionFactory.getEntityManager()
                .createNativeQuery(jpql, Boolean.class)
                .setParameter(1, puid).setParameter(2, lot)
                .setParameter(3, debut).setParameter(4, fin.plusDays(1)).setParameter(5, Boolean.FALSE)
                .getSingleResult();
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
            LotPeriodPieces aggLot = computeLotPeriodPieces(produit.getUid(), com.getNumlot(), region, debutInv,
                    finInv);
            double sommeCompterSurPeriode = sommeCompterSurPeriode(produit.getUid(), debutInv, finInv, region);
            StockAgregate stock = findClosedStock(finInv, finInv, produit.getUid(), region, "Journalier du " + finInv);
            double stokTheo_j = (stock == null) ? 0 : stock.getFinalQuantity();
            double stockJuste = ((aggLot.entrees() - aggLot.sorties()) + sommeCompterSurPeriode)
                    - ((sommeCompterSurPeriode == 0 || aggLot.entrees() == 0) ? 0 : stokTheo_j);
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
            clotureStockProduit(produit, com.getNumlot(), region, finInv, finInv, "Journalier du " + finInv);
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
        StockAgregate aggreg = findStock(produit.getUid(), leo, leo, region);
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
        return synthesizeStockAggregateFromLots(findLatestLotRowsForProduct(puid, today, today1, region), region, null);
    }

    private List<StockAgregate> findLatestLotRowsForProduct(String productId, LocalDate dateFrom, LocalDate dateTo,
            String region) {
        if (productId == null || dateFrom == null || dateTo == null || region == null || region.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT * FROM stock_agregate s
                WHERE s.product_id = ?
                  AND s.region LIKE ?
                  AND s.num_lot IS NOT NULL
                  AND s.date BETWEEN ? AND ? AND s.destroyed = ?
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND s2.region = s.region
                        AND s2.num_lot = s.num_lot
                        AND s2.date BETWEEN ? AND ? AND s2.destroyed = s.destroyed
                  )
                ORDER BY s.date DESC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sql, StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, region)
                    .setParameter(3, dateFrom)
                    .setParameter(4, dateTo.plusDays(1))
                    .setParameter(5, Boolean.FALSE)
                    .setParameter(6, dateFrom)
                    .setParameter(7, dateTo.plusDays(1))
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sql, StockAgregate.class)
                .setParameter(1, productId)
                .setParameter(2, region)
                .setParameter(3, dateFrom)
                .setParameter(4, dateTo.plusDays(1))
                .setParameter(5, Boolean.FALSE)
                .setParameter(6, dateFrom)
                .setParameter(7, dateTo.plusDays(1))
                .getResultList();
    }

    private List<StockAgregate> findLatestLotRowsForProduct(String productId,
            String lot, LocalDate dateFrom, LocalDate dateTo,
            String region) {
        if (productId == null || dateFrom == null || dateTo == null || region == null || region.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT * FROM stock_agregate s
                WHERE s.product_id = ?
                  AND s.region LIKE ?
                  AND s.num_lot = ?
                  AND s.date BETWEEN ? AND ? AND s.destroyed = ?
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND s2.region = s.region
                        AND s2.num_lot = s.num_lot
                        AND s2.date BETWEEN ? AND ? AND s2.destroyed=s.destroyed
                  )
                ORDER BY s.date DESC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sql, StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, region)
                    .setParameter(3, lot)
                    .setParameter(4, dateFrom)
                    .setParameter(5, dateTo.plusDays(1))
                    .setParameter(6, Boolean.FALSE)
                    .setParameter(7, dateFrom)
                    .setParameter(8, dateTo.plusDays(1))
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sql, StockAgregate.class)
                .setParameter(1, productId)
                .setParameter(2, region)
                .setParameter(3, lot)
                .setParameter(4, dateFrom)
                .setParameter(5, dateTo.plusDays(1))
                .setParameter(6, Boolean.FALSE)
                .setParameter(7, dateFrom)
                .setParameter(8, dateTo.plusDays(1))
                .getResultList();
    }

//    private List<StockAgregate> findLatestLotRowsForProduct(String productId, LocalDate dateFrom, LocalDate dateTo,
//            String region) {
//        if (productId == null || dateFrom == null || dateTo == null || region == null || region.isBlank()) {
//            return List.of();
//        }
//        String sql = """
//                SELECT * FROM stock_agregate s
//                WHERE s.product_id = ?
//                  AND s.region LIKE ?
//                  AND s.num_lot IS NOT NULL
//                  AND s.date BETWEEN ? AND ?
//                  AND s.date = (
//                      SELECT MAX(s2.date)
//                      FROM stock_agregate s2
//                      WHERE s2.product_id = s.product_id
//                        AND s2.region = s.region
//                        AND s2.num_lot = s.num_lot
//                        AND s2.date BETWEEN ? AND ?
//                  )
//                ORDER BY s.date DESC
//                """;
//        if (ManagedSessionFactory.isEmbedded()) {
//            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sql, StockAgregate.class)
//                    .setParameter(1, productId)
//                    .setParameter(2, region)
//                    .setParameter(3, dateFrom.atStartOfDay())
//                    .setParameter(4, dateTo.atTime(23, 59, 59))
//                    .setParameter(5, dateFrom.atStartOfDay())
//                    .setParameter(6, dateTo.atTime(23, 59, 59))
//                    .getResultList());
//        }
//        return ManagedSessionFactory.getEntityManager()
//                .createNativeQuery(sql, StockAgregate.class)
//                .setParameter(1, productId)
//                .setParameter(2, region)
//                .setParameter(3, dateFrom.atStartOfDay())
//                .setParameter(4, dateTo.atTime(23, 59, 59))
//                .setParameter(5, dateFrom.atStartOfDay())
//                .setParameter(6, dateTo.atTime(23, 59, 59))
//                .getResultList();
//    }
    @Override
    public List<StockAgregate> findLatestLotStockAgregates(String productId) {
        if (productId == null || productId.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT * FROM stock_agregate s
                WHERE s.product_id = ?
                  AND s.num_lot IS NOT NULL
                  AND COALESCE(s.final_quantity, 0) > 0 AND s.destroyed = ?
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND COALESCE(s2.region, '') = COALESCE(s.region, '')
                        AND s2.num_lot = s.num_lot AND s2.destroyed = s.destroyed
                  )
                ORDER BY s.date DESC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sql, StockAgregate.class)
                    .setParameter(1, productId).setParameter(2, Boolean.FALSE)
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sql, StockAgregate.class)
                .setParameter(1, productId).setParameter(2, Boolean.FALSE)
                .getResultList();
    }

    @Override
    public List<StockAgregate> findLatestLotStockAgregates(String productId, String region) {
        if (productId == null || productId.isBlank() || region == null || region.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT * FROM stock_agregate s
                WHERE s.product_id = ?
                  AND s.region LIKE ?
                  AND s.num_lot IS NOT NULL
                  AND COALESCE(s.final_quantity, 0) > 0 AND s.destroyed = ?
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND s2.region = s.region
                        AND s2.num_lot = s.num_lot AND s2.destroyed = s.destroyed
                  )
                ORDER BY s.date DESC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sql, StockAgregate.class)
                    .setParameter(1, productId)
                    .setParameter(2, region).setParameter(3, Boolean.FALSE)
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager()
                .createNativeQuery(sql, StockAgregate.class)
                .setParameter(1, productId)
                .setParameter(2, region).setParameter(3, Boolean.FALSE)
                .getResultList();
    }

    private List<StockAgregate> findLatestLotRowsByExpirationInterval(LocalDate dateExp1, LocalDate dateExp2,
            String region) {
        if (dateExp1 == null || dateExp2 == null) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM stock_agregate s
                WHERE s.num_lot IS NOT NULL
                  AND s.date_expiration IS NOT NULL
                  AND COALESCE(s.final_quantity, 0) > 0
                  AND s.date_expiration BETWEEN ? AND ? AND s.destroyed = 'FALSE'
                """);
        if (region != null && !region.isBlank()) {
            sql.append(" AND s.region LIKE ? ");
        }
        sql.append("""
                  AND s.date = (
                      SELECT MAX(s2.date)
                      FROM stock_agregate s2
                      WHERE s2.product_id = s.product_id
                        AND COALESCE(s2.region, '') = COALESCE(s.region, '')
                        AND s2.num_lot = s.num_lot AND s2.destroyed = s.destroyed
                  )
                ORDER BY s.date_expiration ASC, s.product_id ASC, s.num_lot ASC
                """);
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sql.toString(), StockAgregate.class);
                query.setParameter(1, dateExp1);
                query.setParameter(2, dateExp2);
                if (region != null && !region.isBlank()) {
                    query.setParameter(3, region);
                }
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql.toString(), StockAgregate.class);
        query.setParameter(1, dateExp1);
        query.setParameter(2, dateExp2);
        if (region != null && !region.isBlank()) {
            query.setParameter(3, region);
        }
        return query.getResultList();
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
    public double sumLatestLotFinalQuantityFromStockAggregate(String productId) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(COALESCE(s.final_quantity,0)),0)
                    FROM stock_agregate s
                    WHERE s.product_id = ? AND s.destroyed = ? 
                      AND s.num_lot IS NOT NULL
                      AND s.date = (
                          SELECT MAX(s2.date)
                          FROM stock_agregate s2
                          WHERE s2.product_id = s.product_id
                            AND s2.region = s.region
                            AND s2.num_lot = s.num_lot AND s2.destroyed=s.destroyed
                      )
                    """;
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Object rst = em.createNativeQuery(sql)
                            .setParameter(1, productId).setParameter(2, Boolean.FALSE)
                            .getSingleResult();
                    return rst == null ? 0d : ((Number) rst).doubleValue();
                });
            }
            Object rst = ManagedSessionFactory.getEntityManager().createNativeQuery(sql)
                    .setParameter(1, productId).setParameter(2, Boolean.FALSE)
                    .getSingleResult();
            return rst == null ? 0d : ((Number) rst).doubleValue();
        } catch (NoResultException ex) {
            return 0d;
        }
    }

    @Override
    public double sumLatestLotFinalQuantityFromStockAggregate(String productId, String region) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(COALESCE(s.final_quantity,0)),0)
                    FROM stock_agregate s
                    WHERE s.product_id = ?
                      AND s.region LIKE ?
                      AND s.num_lot IS NOT NULL AND s.destroyed = ? 
                      AND s.date = (
                          SELECT MAX(s2.date)
                          FROM stock_agregate s2
                          WHERE s2.product_id = s.product_id
                            AND s2.region = s.region
                            AND s2.num_lot = s.num_lot AND s2.destroyed = s.destroyed
                      )
                    """;
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Object rst = em.createNativeQuery(sql)
                            .setParameter(1, productId)
                            .setParameter(2, region)
                            .setParameter(3, Boolean.FALSE)
                            .getSingleResult();
                    return rst == null ? 0d : ((Number) rst).doubleValue();
                });
            }
            Object rst = ManagedSessionFactory.getEntityManager().createNativeQuery(sql)
                    .setParameter(1, productId)
                    .setParameter(2, region)
                    .setParameter(3, Boolean.FALSE)
                    .getSingleResult();
            return rst == null ? 0d : ((Number) rst).doubleValue();
        } catch (NoResultException ex) {
            return 0d;
        }
    }

    @Override
    public double sumLatestLotFinalQuantityFromStockAggregate(String productId, String lot, String region) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(COALESCE(s.final_quantity,0)),0)
                    FROM stock_agregate s
                    WHERE s.product_id = ?
                      AND s.region LIKE ?
                      AND s.num_lot = ? AND s.destroyed = ?
                      AND s.date = (
                          SELECT MAX(s2.date)
                          FROM stock_agregate s2
                          WHERE s2.product_id = s.product_id
                            AND s2.region = s.region
                            AND s2.num_lot = s.num_lot AND s2.destroyed=s.destroyed
                      )
                    """;
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Object rst = em.createNativeQuery(sql)
                            .setParameter(1, productId)
                            .setParameter(2, region).setParameter(3, lot).setParameter(4, Boolean.FALSE)
                            .getSingleResult();
                    return rst == null ? 0d : ((Number) rst).doubleValue();
                });
            }
            Object rst = ManagedSessionFactory.getEntityManager().createNativeQuery(sql)
                    .setParameter(1, productId)
                    .setParameter(2, region).setParameter(3, lot).setParameter(4, Boolean.FALSE)
                    .getSingleResult();
            return rst == null ? 0d : ((Number) rst).doubleValue();
        } catch (NoResultException ex) {
            return 0d;
        }
    }

    @Override
    public int verifyAndCorrectStockAggregateConsistency(String region) {
        int corrected = 0;
        final boolean globalScope = isGlobalScope(region);
        final double epsilon = 0.0001d;
        List<Produit> produits = ProduitDelegate.findProduits();
        for (Produit produit : produits) {
            String productId = produit.getUid();
            double fromAggregate = globalScope
                    ? sumLatestLotFinalQuantityFromStockAggregate(productId)
                    : sumLatestLotFinalQuantityFromStockAggregate(productId, region);
            double fromLedger = globalScope
                    ? findRemainedInMagasinFor(productId)
                    : findRemainedInMagasinFor(productId, region);
            if (Math.abs(fromAggregate - fromLedger) <= epsilon) {
                continue;
            }

            Map<String, Recquisition> seeds = findLatestSeedsPerRegion(produit, region);
            if (seeds.isEmpty()) {
                continue;
            }
            if (globalScope) {
                for (Recquisition seed : seeds.values()) {
                    if (seed != null) {
                        saveStockFromRecquisition(seed);
                    }
                }
                corrected++;
            } else {
                Recquisition seed = seeds.get(region);
                if (seed != null) {
                    saveStockFromRecquisition(seed);
                    corrected++;
                }
            }
        }
        return corrected;
    }

    private boolean isGlobalScope(String region) {
        return region == null || region.isBlank() || "%".equals(region);
    }

    private Map<String, Recquisition> findLatestSeedsPerRegion(Produit produit, String region) {
        List<Recquisition> recqs = isGlobalScope(region)
                ? findRecquisitionByProduit(produit.getUid())
                : findRecquisitionByProduitRegion(produit.getUid(), region);
        Map<String, Recquisition> latestPerRegion = new HashMap<>();
        if (recqs == null || recqs.isEmpty()) {
            return latestPerRegion;
        }
        recqs.forEach(rq -> {
            String rqRegion = rq.getRegion();
            if (!(rqRegion == null || rqRegion.isBlank())) {
                Recquisition current = latestPerRegion.get(rqRegion);
                Produit p = rq.getProductId();
                if (current == null
                        || (rq.getDate() != null && current.getDate() != null && rq.getDate().isAfter(current.getDate()))
                        || (current.getDate() == null && rq.getDate() != null)) {
                    StockAgregate s = findLastAgregateByLot(p.getUid(), rq.getNumlot(), region);
                    if (s == null || !s.isDestroyed()) {
                        latestPerRegion.put(rqRegion, rq);
                    }
                }
            }
        });
        return latestPerRegion;
    }

    private StockAgregate findLastAgregateByLot(String prod, String numlot, String region) {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.product_id = ? AND s.region LIKE ? "
                    + "AND s.final_quantity > 0 AND s.num_lot = ? ORDER BY s.date DESC");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                    query.setParameter(1, prod);
                    query.setParameter(2, region == null ? "%" : region);
                    query.setParameter(3, numlot).setMaxResults(1);
                    return (StockAgregate) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), StockAgregate.class);
            query.setParameter(1, prod);
            query.setParameter(2, region == null ? "%" : region);
            query.setParameter(3, numlot).setMaxResults(1);
            return (StockAgregate) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }
//    

    @Override
    public List<Peremption> showExpiredAtInterval(LocalDate dateExp1, LocalDate dateEpx2, String region) {
        List<Peremption> result = new ArrayList<>();
        if (dateExp1 == null || dateEpx2 == null) {
            return result;
        }
        List<StockAgregate> expiredLots = findLatestLotRowsByExpirationInterval(dateExp1, dateEpx2, region);
        for (StockAgregate lotStock : expiredLots) {
            if (lotStock == null || lotStock.getProductId() == null) {
                continue;
            }
            Produit produit = lotStock.getProductId();
            double reste = safeStockAggregateValue(lotStock.getFinalQuantity());
            if (reste <= 0) {
                continue;
            }
            String localisation = getLocation(produit.getUid());
            Mesure mesure = lotStock.getMesureId() == null ? findMinMesureForProduit(produit.getUid()) : lotStock.getMesureId();
            Peremption per = new Peremption();
            per.setCodebar(produit.getCodebar());
            per.setCoutAchat(safeStockAggregateValue(lotStock.getCoutAchat()));
            per.setDateExpiry(lotStock.getDateExpiration());
            per.setLot(lotStock.getNumlot());
            per.setMesure(mesure == null ? "" : mesure.getDescription());
            per.setProduit(produit.getNomProduit() + " " + produit.getModele() + " " + produit.getTaille());
            per.setProduitUid(produit.getUid());
            per.setLocalisation(localisation == null ? lotStock.getRegion() : localisation);
            per.setQuantite(reste);
            per.setRegion(lotStock.getRegion());
            per.setValeur(BigDecimal.valueOf(reste * safeStockAggregateValue(lotStock.getCoutAchat()))
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .doubleValue());
            result.add(per);
        }
        return result;
    }

    private record ExpiredItem(String uidProduit, String numlot, LocalDate dateExpire, double quantite,
            Mesure mesure, double coutAchat, String region) {

    }

}
