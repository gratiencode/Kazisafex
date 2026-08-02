package services;

import data.Destocker;
import data.LigneVente;
import data.Livraison;
import data.Operation;
import data.Recquisition;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import delegates.ImmobilisationDelegate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.Agregator;
import tools.MemoryGuard;

public final class AggregateTriggerService {

    private static final Logger LOG = Logger.getLogger(AggregateTriggerService.class.getName());
    private static final long CASCADE_DEBOUNCE_MS = 1_200L;
    private static final AggregateTriggerService INSTANCE = new AggregateTriggerService();

    private final ScheduledExecutorService executor = MemoryGuard.newSingleThreadScheduledExecutor(
            "kazisafe-javafx-aggregate-trigger");
    private final ConcurrentHashMap<RefreshKey, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    private AggregateTriggerService() {
    }

    public static AggregateTriggerService getInstance() {
        return INSTANCE;
    }

    public void notifyLivraison(Livraison livraison) {
        enqueue(dateOf(livraison == null ? null : livraison.getDateLivr()),
                livraison == null ? null : livraison.getRegion());
    }

    public void notifyVente(Vente vente) {
        enqueue(dateOf(vente == null ? null : vente.getDateVente()),
                vente == null ? null : vente.getRegion());
    }

    public void notifyLigneVente(LigneVente ligne) {
        Vente vente = ligne == null ? null : ligne.getReference();
        enqueue(dateOf(vente == null ? null : vente.getDateVente()),
                vente == null ? null : vente.getRegion());
    }

    public void notifyRecquisition(Recquisition recquisition) {
        enqueue(dateOf(recquisition == null ? null : recquisition.getDate()),
                recquisition == null ? null : recquisition.getRegion());
    }

    public void notifyStocker(Stocker stocker) {
        enqueue(dateOf(stocker == null ? null : stocker.getDateStocker()),
                stocker == null ? null : stocker.getRegion());
    }

    public void notifyDestocker(Destocker destocker) {
        enqueue(dateOf(destocker == null ? null : destocker.getDateDestockage()),
                destocker == null ? null : destocker.getRegion());
    }

    public void notifyOperation(Operation operation) {
        enqueue(dateOf(operation == null ? null : operation.getDate()),
                operation == null ? null : operation.getRegion());
    }

    public void notifyTraisorerie(Traisorerie traisorerie) {
        enqueue(dateOf(traisorerie == null ? null : traisorerie.getDate()),
                traisorerie == null ? null : traisorerie.getRegion());
    }

    public void notifyImmobilisation(LocalDate date, String region) {
        enqueue(date, region);
    }

    public void rebuildAtStartup() {
        enqueue(LocalDate.now(), null);
    }

    private void enqueue(LocalDate date, String region) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        String targetRegion = normalize(region);
        enqueueDebounced(new RefreshKey(targetDate, null));
        if (targetRegion != null) {
            enqueueDebounced(new RefreshKey(targetDate, targetRegion));
        }
    }

    private void enqueueDebounced(RefreshKey key) {
        // Ne pas planifier si la RAM est insuffisante
        if (!MemoryGuard.hasEnoughMemory()) {
            MemoryGuard.logMemoryState();
            LOG.log(Level.WARNING,
                "[AggregateTriggerService] Planification refusée (RAM insuffisante) pour la clé : " + key);
            return;
        }
        AtomicReference<ScheduledFuture<?>> scheduledRef = new AtomicReference<>();
        ScheduledFuture<?> scheduled = executor.schedule(
                () -> runRefresh(key, scheduledRef.get()),
                CASCADE_DEBOUNCE_MS,
                TimeUnit.MILLISECONDS);
        scheduledRef.set(scheduled);
        ScheduledFuture<?> previous = pending.put(key, scheduled);
        if (previous != null) {
            previous.cancel(false);
        }
    }

    private void runRefresh(RefreshKey key, ScheduledFuture<?> scheduled) {
        if (!pending.remove(key, scheduled)) {
            return;
        }
        try {
            // La clôture des stocks est strictement réservée à PosController.refreshPos.
            // On ne déclenche plus Agregator.getInstance().agregate(...) ici lors des mutations/matérialisations.
            ImmobilisationDelegate.agregate(key.date(), key.region());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Echec recalcul agregats JavaFX: " + key, ex);
        }
    }

    private LocalDate dateOf(LocalDate date) {
        return date;
    }

    private LocalDate dateOf(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    private String normalize(String region) {
        return region == null || region.trim().isBlank() ? null : region.trim();
    }

    private record RefreshKey(LocalDate date, String region) {
    }
}
