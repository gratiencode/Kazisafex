package services;

import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import data.Produit;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentLinkedQueue;
import tools.SyncLogger;

/**
 * Accumulates stock rectification tasks during a sync epoch and flushes them
 * at the end of the cycle. This prevents intermediate aggregate computation
 * on incomplete data while the downsync phases are still materializing.
 */
public final class SyncEpochManager {

    private static final ConcurrentLinkedQueue<Runnable> pending = new ConcurrentLinkedQueue<>();

    private SyncEpochManager() {}

    /** Enqueue a deferred rectification (called from sync handlers). */
    public static void enqueue(String productId, String region, String numlot) {
        pending.add(() -> {
            try {
                Produit p = ProduitDelegate.findProduit(productId);
                if (p != null) {
                    RecquisitionDelegate.rectifyStock(
                            p, LocalDate.now(), LocalDate.now(), region, numlot);
                }
            } catch (Exception e) {
                System.err.println("[EPOCH] Deferred rectifyStock failed: " + e.getMessage());
                SyncLogger.getInstance().log(e, "SyncEpochManager.flush", productId, null);
            }
        });
    }

    /** Drain and execute all accumulated rectifications. Call at epoch end. */
    public static void flush() {
        Runnable task;
        int count = 0;
        while ((task = pending.poll()) != null) {
            try {
                task.run();
                count++;
            } catch (Exception e) {
                System.err.println("[EPOCH] Rectification task failed: " + e.getMessage());
                SyncLogger.getInstance().log(e, "SyncEpochManager.flush", null, null);
            }
        }
        if (count > 0) {
            System.out.println("[EPOCH] Flushed " + count + " deferred stock rectification(s).");
        }
    }

    /** Discard all pending tasks without executing (e.g. on sync abort). */
    public static void discardAll() {
        pending.clear();
    }

    public static boolean isEmpty() {
        return pending.isEmpty();
    }
}
