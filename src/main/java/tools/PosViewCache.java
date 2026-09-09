/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates.
 */
package tools;

import data.Mesure;
import data.Produit;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

/**
 * Vue POS calculée dans le SGBD et mise en cache.
 * <p>
 * Au lieu de recharger tous les produits puis d'exécuter une pile de requêtes
 * par produit (stock, lot, recquisition, prix), les produits, mesures et
 * quantités disponibles sont obtenus par des JOINs SQL agrégés
 * ({@code RecquisitionDelegate#loadPosStockView}) en quelques requêtes
 * globales. Le résultat (des {@link ListViewItem}) est ensuite réutilisé par
 * le tableau de l'onglet POS. La vue est chauffée au démarrage de
 * l'application et rafraîchie sur événement de synchronisation.
 */
public final class PosViewCache {

    private static final ConcurrentHashMap<String, List<ListViewItem>> CACHE = new ConcurrentHashMap<>();

    // Verrou de construction : garanti qu'une seule requête DB est exécutée par
    // clé. Les appels concurrents (scroll, bascule, refresh) attendent la même
    // vue au lieu de relancer plusieurs constructions en parallèle.
    private static final ConcurrentHashMap<String, CompletableFuture<List<ListViewItem>>> IN_FLIGHT = new ConcurrentHashMap<>();

    private PosViewCache() {
    }

    /**
     * Retourne la vue POS en cache pour la combo (région, méthode, accès
     * global). Construit une première fois (et une seule) paresseusement.
     * Les appels concurrents pour la même clé partagent une unique construction.
     */
    public static List<ListViewItem> getPosView(String region, String meth, boolean global) {
        String key = key(region, meth, global);
        List<ListViewItem> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        CompletableFuture<List<ListViewItem>> existing = IN_FLIGHT.get(key);
        if (existing != null) {
            return existing.join();
        }
        CompletableFuture<List<ListViewItem>> future = new CompletableFuture<>();
        CompletableFuture<List<ListViewItem>> raced = IN_FLIGHT.putIfAbsent(key, future);
        if (raced != null) {
            return raced.join();
        }
        try {
            List<ListViewItem> built = build(region, meth, global);
            if (built != null) {
                CACHE.put(key, built);
            }
            future.complete(built);
            return built;
        } catch (RuntimeException | Error ex) {
            future.completeExceptionally(ex);
            throw ex;
        } finally {
            IN_FLIGHT.remove(key);
        }
    }

    /**
     * Retourne la vue déjà en cache ou {@code null} sans construire (non
     * bloquant pour le thread FX).
     */
    public static List<ListViewItem> getIfLoaded(String region, String meth, boolean global) {
        return CACHE.get(key(region, meth, global));
    }

    /**
     * Préchauffe la vue au démarrage de l'application (appeler sur un thread
     * d'arrière-plan).
     */
    public static void warm(String region, String meth, boolean global) {
        getPosView(region, meth, global);
    }

    /** Invalide toutes les vues (à appeler après une synchronisation/refresh). */
    public static void invalidateAll() {
        CACHE.clear();
    }

    private static String key(String region, String meth, boolean global) {
        return (region == null ? "*" : region) + "|" + (meth == null ? "" : meth) + "|" + global;
    }

    private static String lotKey(String productId, Object numlot) {
        return productId + "|" + (numlot == null ? "null" : numlot.toString());
    }

    private static LocalDate toLocalDate(Object o) {
        if (o instanceof LocalDate ld) {
            return ld;
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        if (o instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (o instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (o instanceof java.util.Date d) {
            return new java.sql.Date(d.getTime()).toLocalDate();
        }
        if (o instanceof Number n) {
            long v = n.longValue();
            if (v > 10_000_000_000L) {
                return LocalDate.ofInstant(java.time.Instant.ofEpochMilli(v), java.time.ZoneId.systemDefault());
            }
            if (v > 0) {
                return LocalDate.ofInstant(java.time.Instant.ofEpochSecond(v), java.time.ZoneId.systemDefault());
            }
            return null;
        }
        if (o instanceof String s && !s.isBlank()) {
            try {
                return LocalDate.parse(s);
            } catch (Exception ignore) {
                try {
                    return java.sql.Date.valueOf(s).toLocalDate();
                } catch (Exception ignore2) {
                    return null;
                }
            }
        }
        return null;
    }

    private static double asDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0d;
    }

    private static Map<String, Double> lotSums(List<Object[]> rows) {
        Map<String, Double> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 3 || row[0] == null) {
                continue;
            }
            map.put(lotKey((String) row[0], row[1]), asDouble(row[2]));
        }
        return map;
    }

    private static List<ListViewItem> build(String region, String meth, boolean global) {
        List<ListViewItem> normalized = new ArrayList<>();
        List<Object[]> rows = RecquisitionDelegate.loadPosStockView(region, meth, global);
        if (rows == null || rows.isEmpty()) {
            return normalized;
        }
        Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
        if (meth == null) {
            meth = pref.get("meth", "fifo");
        }

        Map<String, Produit> produits = new HashMap<>();
        for (Produit p : ProduitDelegate.findProduits()) {
            produits.put(p.getUid(), p);
        }
        Map<String, Mesure> mesures = new HashMap<>();
        for (Mesure m : MesureDelegate.findMesures()) {
            mesures.put(m.getUid(), m);
        }

        List<Object[]> priceRows = RecquisitionDelegate.loadPriceRows();
        List<Object[]> allRecqRows = RecquisitionDelegate.loadAllRecqs();
        Map<String, String[]> recqOwner = new HashMap<>();
        Map<String, LocalDate> recqDate = new HashMap<>();
        for (Object[] rr : allRecqRows) {
            if (rr[0] == null) {
                continue;
            }
            recqOwner.put((String) rr[0], new String[]{(String) rr[1], String.valueOf(rr[2])});
            recqDate.put((String) rr[0], toLocalDate(rr[3]));
        }
        List<Object[]> reqRows = RecquisitionDelegate.loadHeaderRecqs(region);
        Map<String, Double> entrees = lotSums(RecquisitionDelegate.loadRecqLotEntrees(region));
        Map<String, Double> sorties = lotSums(RecquisitionDelegate.loadLigneVenteLotSorties(region));
        Map<String, Double> retours = lotSums(RecquisitionDelegate.loadRetourDepotLotReturns(region));

        // Pré-indexation en un seul passage : remplace les boucles imbriquées
        // O(produits × lignes) d'origine par des recherches guidées par clé
        // (produit / recquisition). Le temps de construction de la vue POS
        // devient ~linéaire au lieu de quadratique sur les gros catalogues.
        Map<String, List<Object[]>> priceRowsByProduct = new HashMap<>();
        Map<String, List<Object[]>> priceRowsByRecq = new HashMap<>();
        for (Object[] pr : priceRows) {
            if (pr == null || pr.length < 5 || pr[4] == null) {
                continue;
            }
            String recqUid = (String) pr[4];
            priceRowsByRecq.computeIfAbsent(recqUid, k -> new ArrayList<>()).add(pr);
            String[] owner = recqOwner.get(recqUid);
            if (owner != null && owner[0] != null) {
                priceRowsByProduct.computeIfAbsent(owner[0], k -> new ArrayList<>()).add(pr);
            }
        }
        Map<String, List<Object[]>> recqsByProduct = new HashMap<>();
        for (Object[] rr : reqRows) {
            if (rr != null && rr[1] != null) {
                recqsByProduct.computeIfAbsent((String) rr[1], k -> new ArrayList<>()).add(rr);
            }
        }
        Map<String, List<Object[]>> allRecqsByProduct = new HashMap<>();
        for (Object[] rr : allRecqRows) {
            if (rr != null && rr[1] != null) {
                allRecqsByProduct.computeIfAbsent((String) rr[1], k -> new ArrayList<>()).add(rr);
            }
        }

        for (Object[] row : rows) {
            if (row == null || row.length < 16 || row[0] == null) {
                continue;
            }
            Produit prod = produits.get((String) row[0]);
            if (prod == null) {
                continue;
            }
            double pieces = asDouble(row[7]);
            if (pieces <= 0) {
                continue;
            }
            // Aucun lot actif exposable dans le scope -> même comportement que l'ancien lotToshow()
            String numlot = (String) row[8];
            if (numlot == null) {
                continue;
            }
            LocalDate peremption = toLocalDate(row[9]);
            double cout = asDouble(row[10]);

            String headLotMesureUid = (String) row[11];
            Double headQc = row[12] instanceof Number n ? n.doubleValue() : null;
            Mesure mesureUi = headLotMesureUid == null ? null : mesures.get(headLotMesureUid);
            if (mesureUi == null || headQc == null || headQc <= 0) {
                String smallUid = (String) row[14];
                mesureUi = smallUid == null ? null : mesures.get(smallUid);
                if (mesureUi == null) {
                    continue;
                }
            }

            // Prix détail : dernier prix (recquisition la plus récente) du produit.
            double detailPrice = 0d;
            Mesure mesureDetail = mesureUi;
            LocalDate bestDate = null;
            Object[] bestPrice = null;
            List<Object[]> prodPrices = priceRowsByProduct.get(prod.getUid());
            if (prodPrices != null) {
                for (Object[] pr : prodPrices) {
                    LocalDate d = recqDate.get((String) pr[4]);
                    if (d != null && (bestDate == null || d.compareTo(bestDate) > 0)) {
                        bestDate = d;
                        bestPrice = pr;
                    }
                }
            }
            if (bestPrice != null) {
                detailPrice = asDouble(bestPrice[2]);
                String mUid = (String) bestPrice[3];
                Mesure mDet = mUid == null ? null : mesures.get(mUid);
                if (mDet != null) {
                    mesureDetail = mDet;
                }
            }

            // Recquisition de tête (méthode FIFO/LIFO/PPPS) : premier lot du
            // courant de stock restant > 0, repli sur la dernière recquisition.
            Object[] header = null;
            List<Object[]> reqs = new ArrayList<>(recqsByProduct.getOrDefault(prod.getUid(), List.of()));
            if ("lifo".equalsIgnoreCase(meth)) {
                reqs.sort(Comparator.comparing(r -> toLocalDate(r[3]), Comparator.nullsLast(Comparator.reverseOrder())));
            } else if ("ppps".equalsIgnoreCase(meth)) {
                reqs.sort(Comparator.comparing(r -> toLocalDate(r[4]), Comparator.nullsLast(Comparator.naturalOrder())));
            } else {
                reqs.sort(Comparator.comparing(r -> toLocalDate(r[3]), Comparator.nullsLast(Comparator.naturalOrder())));
            }
            for (Object[] rr : reqs) {
                double entree = entrees.getOrDefault(lotKey(prod.getUid(), rr[2]), 0d);
                double sortie = sorties.getOrDefault(lotKey(prod.getUid(), rr[2]), 0d);
                double ret = retours.getOrDefault(lotKey(prod.getUid(), rr[2]), 0d);
                if ((entree - sortie - ret) > 0) {
                    header = rr;
                    break;
                }
            }
            if (header == null && !reqs.isEmpty()) {
                header = reqs.get(0);
            }
            if (header == null) {
                List<Object[]> fallbase = allRecqsByProduct.get(prod.getUid());
                if (fallbase != null) {
                    List<Object[]> fallback = new ArrayList<>(fallbase);
                    fallback.sort(Comparator.comparing(r -> toLocalDate(r[3]), Comparator.nullsLast(Comparator.reverseOrder())));
                    if (!fallback.isEmpty()) {
                        header = fallback.get(0);
                    }
                }
            }

            Mesure mesureGros = mesureDetail;
            double salePrice = detailPrice;
            if (header != null) {
                String headerUid = (String) header[0];
                String achatUid = mesureUi.getUid();
                List<Object[]> headerPrices = priceRowsByRecq.getOrDefault(headerUid, List.of());
                List<Object[]> gros = new ArrayList<>();
                for (Object[] pr : headerPrices) {
                    if (asDouble(pr[1]) > 1 && !achatUid.equals(pr[3])) {
                        gros.add(pr);
                    }
                }
                if (gros.isEmpty()) {
                    for (Object[] pr : headerPrices) {
                        if (asDouble(pr[1]) > 1 && achatUid.equals(pr[3])) {
                            gros.add(pr);
                        }
                    }
                }
                if (!gros.isEmpty()) {
                    Object[] grprix = gros.get(gros.size() - 1);
                    String mUid = (String) grprix[3];
                    if (mUid != null) {
                        Mesure gm = mesures.get(mUid);
                        if (gm != null) {
                            mesureGros = gm;
                        }
                    }
                    salePrice = asDouble(grprix[2]);
                }
            }

            double resteMesureUi = BigDecimal.valueOf(pieces / mesureUi.getQuantContenu())
                    .setScale(3, RoundingMode.HALF_EVEN)
                    .doubleValue();
            ListViewItem item = new ListViewItem();
            item.setProduit(prod);
            item.setMesureAchat(mesureUi);
            item.setCoutAchat(cout);
            item.setMesureDetail(mesureDetail);
            item.setMesureGros(mesureGros);
            item.setSalePrice(CurrencyConverter.priceFromStorageUsd(salePrice));
            item.setPurchasePrice(CurrencyConverter.priceFromStorageUsd(cout));
            item.setQuantiteRestant(resteMesureUi);
            item.setDetailPrice(CurrencyConverter.priceFromStorageUsd(detailPrice));
            item.setNumlot(numlot);
            item.setPeremption(peremption);
            normalized.add(item);
        }
        return normalized;
    }
}