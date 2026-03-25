/*
 * Service for StockDepotAgregate aggregate operations
 * Handles inventory calculations according to PPPS, FIFO, LIFO methods
 */
package services;

import data.Mesure;
import data.Produit;
import data.StockDepotAgregate;
import data.Stocker;
import data.Destocker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import tools.SyncEngine;

/**
 *
 * @author eroot
 */
public class StockDepotAgregateService {

    private static final String METHODE_PPPS = "ppps";
    private static final String METHODE_FIFO = "fifo";
    private static final String METHODE_LIFO = "lifo";

    private final Preferences pref;
    private final String methode;

    public StockDepotAgregateService() {
        this.pref = Preferences.userNodeForPackage(SyncEngine.class);
        this.methode = pref.get("meth", METHODE_FIFO);
    }

    /**
     * Update stock depot when adding a stocker (incoming stock) Considers
     * inventory method: PPPS, FIFO, LIFO
     */
    public void addStock(Stocker stocker) {
        if (stocker == null || stocker.getProductId() == null) {
            return;
        }

        String productId = stocker.getProductId().getUid();
        String region = stocker.getRegion() != null ? stocker.getRegion() : "DEFAULT";
        LocalDate date = stocker.getDateStocker() != null
                ? stocker.getDateStocker().toLocalDate()
                : LocalDate.now();

        // Get or create today's aggregate record
        StockDepotAgregate depot = findOrCreateStockDepotAgregate(productId, region, date, stocker.getMesureId());

        // Calculate new average cost based on inventory method
        double newQuantite = depot.getQuantite() + stocker.getQuantite();
        double newCout;

        switch (methode.toLowerCase()) {
            case METHODE_PPPS:
                // PPPS: Cost based on expiry date - lower cost first
                newCout = calculatePppsCost(depot, stocker);
                break;
            case METHODE_LIFO:
                // LIFO: Last In First Out - new cost is the most recent cost
                newCout = stocker.getCoutAchat();
                break;
            case METHODE_FIFO:
            default:
                // FIFO: First In First Out - weighted average
                if (depot.getQuantite() > 0) {
                    double totalValue = (depot.getQuantite() * depot.getCoutAchat())
                            + (stocker.getQuantite() * stocker.getCoutAchat());
                    newCout = totalValue / newQuantite;
                } else {
                    newCout = stocker.getCoutAchat();
                }
                break;
        }

        depot.setQuantite(newQuantite);
        depot.setCoutAchat(newCout);
        depot.setValeurStock(newQuantite * newCout);

        // Set numlot and earliest expiry date from the stocker being added
        depot.setNumlot(stocker.getNumlot());
        LocalDate stockerExpiry = stocker.getDateExpir();
        if (stockerExpiry != null) {
            // If there's already an expiry date in depot, keep the earliest one
            LocalDate currentExpiry = depot.getDateExpiration();
            if (currentExpiry == null || stockerExpiry.isBefore(currentExpiry)) {
                depot.setDateExpiration(stockerExpiry);
            }
        }

        saveOrUpdate(depot);
    }

    /**
     * Add stock from a destocker record (used when correcting/deleting a destockage)
     */
    public void addStock(Destocker destocker) {
        if (destocker == null || destocker.getProductId() == null) {
            return;
        }

        Stocker s = new Stocker();
        s.setProductId(destocker.getProductId());
        s.setRegion(destocker.getRegion());
        s.setDateStocker(destocker.getDateDestockage());
        s.setMesureId(destocker.getMesureId());
        s.setQuantite(destocker.getQuantite());
        s.setCoutAchat(destocker.getCoutAchat());
        s.setNumlot(destocker.getNumlot());
        // dateExpir is unknown in Destocker, but addStock handles null correctly
        addStock(s);
    }

    /**
     * Update stock depot when adding a destocker (outgoing stock) Considers
     * inventory method: PPPS, FIFO, LIFO
     */
    public void removeStock(Destocker destocker) {
        if (destocker == null || destocker.getProductId() == null) {
            return;
        }

        String productId = destocker.getProductId().getUid();
        String region = destocker.getRegion() != null ? destocker.getRegion() : "DEFAULT";
        LocalDate date = destocker.getDateDestockage() != null
                ? destocker.getDateDestockage().toLocalDate()
                : LocalDate.now();

        // Get latest aggregate record for cost calculation
        StockDepotAgregate latest = findLatestStockDepotAgregate(productId, region);

        if (latest == null || latest.getQuantite() <= 0) {
            // No stock available - this shouldn't happen in normal operation
            System.err.println("Warning: Attempting to destock when no stock available for product "
                    + productId + " in region " + region);
            return;
        }

        // Get or create today's aggregate record
        StockDepotAgregate depot = findOrCreateStockDepotAgregate(productId, region, date, destocker.getMesureId());

        if (depot.getQuantite() <= 0) {
            // Initialize with latest values if starting fresh
            depot.setQuantite(latest.getQuantite());
            depot.setCoutAchat(latest.getCoutAchat());
        }

        // Calculate cost of goods sold based on inventory method
        double quantityToRemove = destocker.getQuantite();
        double costOfGoodsSold;

        switch (methode.toLowerCase()) {
            case METHODE_PPPS:
                // PPPS: Remove stock with lowest expiry first
                costOfGoodsSold = calculatePppsRemovalCost(productId, region, quantityToRemove);
                break;
            case METHODE_LIFO:
                // LIFO: Last In First Out - most recent cost
                costOfGoodsSold = latest.getCoutAchat();
                break;
            case METHODE_FIFO:
            default:
                // FIFO: First In First Out - oldest cost
                costOfGoodsSold = latest.getCoutAchat();
                break;
        }

        double newQuantite = Math.max(0, depot.getQuantite() - quantityToRemove);

        // Update cost based on remaining stock
        double newCout;
        if (newQuantite > 0) {
            double remainingValue = (depot.getQuantite() * depot.getCoutAchat())
                    - (quantityToRemove * costOfGoodsSold);
            newCout = remainingValue / newQuantite;
        } else {
            newCout = 0;
        }

        depot.setQuantite(newQuantite);
        depot.setCoutAchat(Math.max(0, newCout));
        depot.setValeurStock(newQuantite * depot.getCoutAchat());

        saveOrUpdate(depot);
    }

    /**
     * Get available stock quantity for a product in a region
     */
    public double getAvailableStock(String productId, String region) {
        if (productId == null || region == null) {
            return 0;
        }

        StockDepotAgregate latest = findLatestStockDepotAgregate(productId, region);

        return latest == null ? 0 : latest.getQuantite() * latest.getMesureId().getQuantContenu();
    }

    /**
     * Find or create a StockDepotAgregate record for a specific date
     */
    private StockDepotAgregate findOrCreateStockDepotAgregate(String productId, String region, LocalDate date,
            Mesure mesure) {
        Produit produit = new Produit(productId);

        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("StockDepotAgregate.findByProduitRegionDate");
                query.setParameter("productId", produit);
                query.setParameter("region", region);
                query.setParameter("date", date);
                try {
                    return (StockDepotAgregate) query.getSingleResult();
                } catch (NoResultException e) {
                    StockDepotAgregate newDepot = new StockDepotAgregate();
                    newDepot.setProductId(produit);
                    newDepot.setRegion(region);
                    newDepot.setDate(date);
                    newDepot.setMesureId(mesure);
                    newDepot.setQuantite(0);
                    newDepot.setCoutAchat(0);
                    newDepot.setValeurStock(0);
                    return newDepot;
                }
            });
        }

        EntityManager em = ManagedSessionFactory.getEntityManager();
        Query query = em.createNamedQuery("StockDepotAgregate.findByProduitRegionDate");
        query.setParameter("productId", produit);
        query.setParameter("region", region);
        query.setParameter("date", date);

        try {
            return (StockDepotAgregate) query.getSingleResult();
        } catch (NoResultException e) {
            StockDepotAgregate newDepot = new StockDepotAgregate();
            newDepot.setProductId(produit);
            newDepot.setRegion(region);
            newDepot.setDate(date);
            newDepot.setMesureId(mesure);
            newDepot.setQuantite(0);
            newDepot.setCoutAchat(0);
            newDepot.setValeurStock(0);
            return newDepot;
        }
    }

    /**
     * Find the latest stock depot record for a product in a region
     */
    public StockDepotAgregate findLatestStockDepotAgregate(String productId, String region) {
        Produit produit = new Produit(productId);

        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("StockDepotAgregate.findLatestByProduitAndRegion");
                query.setParameter("productId", produit);
                query.setParameter("region", region);
                query.setMaxResults(1);
                try {
                    List<StockDepotAgregate> results = query.getResultList();
                    return results.isEmpty() ? null : results.get(0);
                } catch (NoResultException e) {
                    return null;
                }
            });
        }

        EntityManager em = ManagedSessionFactory.getEntityManager();
        Query query = em.createNamedQuery("StockDepotAgregate.findLatestByProduitAndRegion");
        query.setParameter("productId", produit);
        query.setParameter("region", region);
        query.setMaxResults(1);

        try {
            List<StockDepotAgregate> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Save or update a StockDepotAgregate record
     */
    private void saveOrUpdate(StockDepotAgregate depot) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                if (depot.getUid() == null) {
                    em.persist(depot);
                } else {
                    em.merge(depot);
                }
                return depot;
            });
        } else {
            EntityManager em = ManagedSessionFactory.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }

            if (depot.getUid() == null) {
                em.persist(depot);
            } else {
                em.merge(depot);
            }

            tx.commit();
        }
    }

    /**
     * Calculate cost for PPPS method (lowest expiry first)
     */
    private double calculatePppsCost(StockDepotAgregate depot, Stocker newStocker) {
        // PPPS: Prioritize stock with earliest expiry
        // Cost is based on the specific batch being added
        return newStocker.getCoutAchat();
    }

    /**
     * Calculate cost for PPPS removal (remove lowest expiry first) This would
     * need to look at individual stocker records
     */
    private double calculatePppsRemovalCost(String productId, String region, double quantity) {
        // Simplified PPPS - use current cost
        StockDepotAgregate latest = findLatestStockDepotAgregate(productId, region);
        return latest != null ? latest.getCoutAchat() : 0;
    }

    /**
     * Get all stock depot records for a specific date
     */
    public List<StockDepotAgregate> findByDate(LocalDate date) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("StockDepotAgregate.findByDate");
                query.setParameter("date", date);
                return query.getResultList();
            });
        }

        EntityManager em = ManagedSessionFactory.getEntityManager();
        Query query = em.createNamedQuery("StockDepotAgregate.findByDate");
        query.setParameter("date", date);
        return query.getResultList();
    }

    /**
     * Get all stock depot records for a region
     */
    public List<StockDepotAgregate> findByRegion(String region) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNamedQuery("StockDepotAgregate.findByRegion");
                query.setParameter("region", region);
                return query.getResultList();
            });
        }

        EntityManager em = ManagedSessionFactory.getEntityManager();
        Query query = em.createNamedQuery("StockDepotAgregate.findByRegion");
        query.setParameter("region", region);
        return query.getResultList();
    }

    /**
     * Find stock depot records within a date range and calculate total value
     * Returns a StockSummary object containing the list of stocks and total
     * value
     */
    public StockSummary findByDateRange(LocalDate startDate, LocalDate endDate, String region) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                String jpql = "SELECT s FROM StockDepotAgregate s WHERE s.date BETWEEN :startDate AND :endDate";
                if (region != null && !region.isEmpty()) {
                    jpql += " AND s.region = :region";
                }
                jpql += " ORDER BY s.date DESC";

                Query query = em.createQuery(jpql);
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
                if (region != null && !region.isEmpty()) {
                    query.setParameter("region", region);
                }

                List<StockDepotAgregate> stocks = query.getResultList();
                double totalValue = stocks.stream()
                        .mapToDouble(StockDepotAgregate::getValeurStock)
                        .sum();

                return new StockSummary(stocks, totalValue);
            });
        }

        EntityManager em = ManagedSessionFactory.getEntityManager();
        String jpql = "SELECT s FROM StockDepotAgregate s WHERE s.date BETWEEN :startDate AND :endDate";
        if (region != null && !region.isEmpty()) {
            jpql += " AND s.region = :region";
        }
        jpql += " ORDER BY s.date DESC";

        Query query = em.createQuery(jpql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (region != null && !region.isEmpty()) {
            query.setParameter("region", region);
        }

        List<StockDepotAgregate> stocks = query.getResultList();
        double totalValue = stocks.stream()
                .mapToDouble(StockDepotAgregate::getValeurStock)
                .sum();

        return new StockSummary(stocks, totalValue);
    }

    /**
     * Helper class to return stock list with total value
     */
    public static class StockSummary {

        private final List<StockDepotAgregate> stocks;
        private final double totalValue;

        public StockSummary(List<StockDepotAgregate> stocks, double totalValue) {
            this.stocks = stocks;
            this.totalValue = totalValue;
        }

        public List<StockDepotAgregate> getStocks() {
            return stocks;
        }

        public double getTotalValue() {
            return totalValue;
        }
    }

    /**
     * Find stock depot records by product and region
     */
    public List<StockDepotAgregate> findByProduitAndRegion(String productId, String region) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                String jpql = "SELECT s FROM StockDepotAgregate s WHERE s.productId.uid = :productId";
                if (region != null && !region.isEmpty()) {
                    jpql += " AND s.region = :region";
                }
                jpql += " ORDER BY s.date DESC";

                Query query = em.createQuery(jpql);
                query.setParameter("productId", productId);
                if (region != null && !region.isEmpty()) {
                    query.setParameter("region", region);
                }
                return query.getResultList();
            });
        }

        EntityManager em = ManagedSessionFactory.getEntityManager();
        String jpql = "SELECT s FROM StockDepotAgregate s WHERE s.productId = :productId";
        if (region != null && !region.isEmpty()) {
            jpql += " AND s.region = :region";
        }
        jpql += " ORDER BY s.date DESC";

        Query query = em.createQuery(jpql);
        query.setParameter("productId", productId);
        if (region != null && !region.isEmpty()) {
            query.setParameter("region", region);
        }
        return query.getResultList();
    }
}
