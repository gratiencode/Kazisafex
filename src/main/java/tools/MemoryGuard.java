package tools;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitaire centralisé de protection mémoire pour tous les threads du projet.
 *
 * <p>Avant de soumettre ou de démarrer une tâche, {@code MemoryGuard} vérifie
 * qu'il reste suffisamment de mémoire vive (RAM JVM) libre. Si la mémoire
 * disponible est inférieure au seuil configuré, la tâche est refusée plutôt que
 * de risquer une saturation ({@link OutOfMemoryError}).</p>
 *
 * <h3>Usage simple :</h3>
 * <pre>{@code
 * // Au lieu de : Executors.newSingleThreadExecutor().submit(task)
 * MemoryGuard.submit(task);
 *
 * // Au lieu de : Executors.newSingleThreadExecutor()
 * ExecutorService es = MemoryGuard.newSingleThreadExecutor();
 *
 * // Au lieu de : Executors.newSingleThreadScheduledExecutor()
 * ScheduledExecutorService ses = MemoryGuard.newSingleThreadScheduledExecutor();
 * }</pre>
 */
public final class MemoryGuard {

    private static final Logger LOG = Logger.getLogger(MemoryGuard.class.getName());

    /**
     * Seuil par défaut : 64 Mo de RAM libre minimum avant d'accepter un nouveau thread.
     * Peut être ajusté via {@link #setMinFreeMemoryBytes(long)}.
     */
    private static volatile long minFreeMemoryBytes = 64L * 1024 * 1024; // 64 MB

    /**
     * Pourcentage maximum d'utilisation de la mémoire totale autorisé (0.0 – 1.0).
     * Par défaut : 85% → refus si plus de 85% de la heap est utilisée.
     */
    private static volatile double maxUsageRatio = 0.85;

    private MemoryGuard() {
        // Utilitaire statique – non instanciable
    }

    // ─────────────────────── Configuration ───────────────────────────────────

    /**
     * Redéfinit le seuil de mémoire libre minimale (en octets).
     *
     * @param bytes taille minimale en octets (ex : 64L * 1024 * 1024 pour 64 Mo)
     */
    public static void setMinFreeMemoryBytes(long bytes) {
        minFreeMemoryBytes = bytes;
    }

    /**
     * Redéfinit le pourcentage maximal d'utilisation heap accepté.
     *
     * @param ratio valeur entre 0.0 et 1.0 (ex : 0.85 = 85%)
     */
    public static void setMaxUsageRatio(double ratio) {
        if (ratio <= 0.0 || ratio > 1.0) {
            throw new IllegalArgumentException("Le ratio doit être compris entre 0.0 et 1.0");
        }
        maxUsageRatio = ratio;
    }

    // ─────────────────────── Vérification RAM ────────────────────────────────

    /**
     * Retourne la mémoire libre effective (libre + récupérable = max - used).
     */
    public static long effectiveFreeMemory() {
        Runtime rt = Runtime.getRuntime();
        long maxMem  = rt.maxMemory();
        long totalMem = rt.totalMemory();
        long freeMem  = rt.freeMemory();
        // mémoire réellement disponible = ce qui est libre dans la heap allouée
        // + ce que la JVM peut encore allouer depuis le système
        return freeMem + (maxMem - totalMem);
    }

    /**
     * Retourne le ratio d'utilisation courant de la heap (0.0 – 1.0).
     */
    public static double currentUsageRatio() {
        Runtime rt = Runtime.getRuntime();
        long maxMem = rt.maxMemory();
        long usedMem = rt.totalMemory() - rt.freeMemory();
        if (maxMem <= 0) return 0.0;
        return (double) usedMem / maxMem;
    }

    /**
     * @return {@code true} si la mémoire est suffisante pour démarrer un thread.
     */
    public static boolean hasEnoughMemory() {
        long free = effectiveFreeMemory();
        double usage = currentUsageRatio();
        return free >= minFreeMemoryBytes && usage < maxUsageRatio;
    }

    /**
     * Vérifie la mémoire disponible et lève une {@link RejectedExecutionException}
     * si le seuil est dépassé.
     *
     * @param taskDescription description courte de la tâche (pour le log)
     * @throws RejectedExecutionException si la RAM est insuffisante
     */
    public static void checkMemoryOrReject(String taskDescription) {
        if (!hasEnoughMemory()) {
            Runtime rt = Runtime.getRuntime();
            long freeMB   = effectiveFreeMemory() / (1024 * 1024);
            double usagePct = currentUsageRatio() * 100;
            String msg = String.format(
                "[MemoryGuard] Tâche refusée [%s] – RAM libre : %d Mo, utilisation heap : %.1f%% " +
                "(seuil min : %d Mo, seuil max : %.0f%%)",
                taskDescription,
                freeMB,
                usagePct,
                minFreeMemoryBytes / (1024 * 1024),
                maxUsageRatio * 100
            );
            LOG.log(Level.WARNING, msg);
            throw new RejectedExecutionException(msg);
        }
    }

    // ─────────────────────── Soumission directe ──────────────────────────────

    /**
     * Soumet une tâche {@link Runnable} sur un nouveau thread dédié,
     * après vérification de la mémoire disponible.
     *
     * <p>Remplace : {@code Executors.newSingleThreadExecutor().submit(runnable)}</p>
     *
     * @param taskName    nom de la tâche (pour les logs)
     * @param runnable    la tâche à exécuter
     * @return le {@link Future} de la soumission, ou {@code null} si refusée faute de RAM
     */
    public static Future<?> submit(String taskName, Runnable runnable) {
        try {
            checkMemoryOrReject(taskName);
            ExecutorService es = Executors.newSingleThreadExecutor(daemonFactory(taskName));
            Future<?> f = es.submit(runnable);
            es.shutdown();
            return f;
        } catch (RejectedExecutionException e) {
            LOG.log(Level.WARNING, "[MemoryGuard] submit refusé : " + e.getMessage());
            return null;
        }
    }

    /**
     * Soumet une tâche {@link Callable} sur un nouveau thread dédié,
     * après vérification de la mémoire disponible.
     *
     * @param taskName nom de la tâche (pour les logs)
     * @param callable la tâche à exécuter
     * @return le {@link Future} de la soumission, ou {@code null} si refusée
     */
    public static <T> Future<T> submit(String taskName, Callable<T> callable) {
        try {
            checkMemoryOrReject(taskName);
            ExecutorService es = Executors.newSingleThreadExecutor(daemonFactory(taskName));
            Future<T> f = es.submit(callable);
            es.shutdown();
            return f;
        } catch (RejectedExecutionException e) {
            LOG.log(Level.WARNING, "[MemoryGuard] submit refusé : " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────── Factories d'ExecutorService ─────────────────────

    /**
     * Crée un {@link ExecutorService} mono-thread vérifiant la RAM avant chaque soumission.
     *
     * <p>Remplace : {@code Executors.newSingleThreadExecutor()}</p>
     */
    public static ExecutorService newSingleThreadExecutor() {
        return newSingleThreadExecutor("kazisafe-pool");
    }

    /**
     * Crée un {@link ExecutorService} mono-thread avec un nom de thread personnalisé.
     */
    public static ExecutorService newSingleThreadExecutor(String threadName) {
        return Executors.newSingleThreadExecutor(daemonFactory(threadName));
    }

    /**
     * Crée un {@link ExecutorService} mono-thread avec une {@link ThreadFactory} personnalisée.
     */
    public static ExecutorService newSingleThreadExecutor(ThreadFactory factory) {
        return Executors.newSingleThreadExecutor(factory);
    }

    /**
     * Crée un {@link ScheduledExecutorService} mono-thread vérifiant la RAM.
     *
     * <p>Remplace : {@code Executors.newSingleThreadScheduledExecutor()}</p>
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return newSingleThreadScheduledExecutor("kazisafe-scheduled");
    }

    /**
     * Crée un {@link ScheduledExecutorService} mono-thread avec un nom de thread personnalisé.
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String threadName) {
        return Executors.newSingleThreadScheduledExecutor(daemonFactory(threadName));
    }

    /**
     * Crée un {@link ScheduledExecutorService} mono-thread avec une {@link ThreadFactory} personnalisée.
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory factory) {
        return Executors.newSingleThreadScheduledExecutor(factory);
    }

    // ─────────────────────── Wrapping d'ExecutorService existant ─────────────

    /**
     * Enveloppe un {@link ExecutorService} existant pour y ajouter la vérification
     * mémoire avant chaque {@code submit()}.
     *
     * <p>Utile pour les pools partagés déjà créés (ex : {@code newFixedThreadPool}).</p>
     */
    public static ExecutorService wrap(ExecutorService delegate) {
        return new MemoryGuardedExecutorService(delegate);
    }

    // ─────────────────────── Diagnostics ─────────────────────────────────────

    /**
     * Journalise l'état actuel de la mémoire (utile pour le debugging).
     */
    public static void logMemoryState() {
        Runtime rt = Runtime.getRuntime();
        long maxMB   = rt.maxMemory()   / (1024 * 1024);
        long totalMB = rt.totalMemory() / (1024 * 1024);
        long freeMB  = rt.freeMemory()  / (1024 * 1024);
        long effMB   = effectiveFreeMemory() / (1024 * 1024);
        LOG.info(String.format(
            "[MemoryGuard] Heap – max:%dMo  allouée:%dMo  libre:%dMo  eff.libre:%dMo  usage:%.1f%%",
            maxMB, totalMB, freeMB, effMB, currentUsageRatio() * 100
        ));
    }

    // ─────────────────────── Helpers internes ────────────────────────────────

    /**
     * Crée une {@link ThreadFactory} produisant des threads démons nommés.
     */
    public static ThreadFactory daemonFactory(String name) {
        return runnable -> {
            Thread t = new Thread(runnable, name);
            t.setDaemon(true);
            return t;
        };
    }

    // ─────────────────────── Inner class wrapper ─────────────────────────────

    /**
     * {@link ExecutorService} qui vérifie la mémoire avant chaque soumission.
     */
    private static final class MemoryGuardedExecutorService
            implements ExecutorService {

        private final ExecutorService delegate;

        MemoryGuardedExecutorService(ExecutorService delegate) {
            this.delegate = delegate;
        }

        private void guard(String hint) {
            checkMemoryOrReject(hint);
        }

        @Override
        public Future<?> submit(Runnable task) {
            guard(task.getClass().getSimpleName());
            return delegate.submit(task);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            guard(task.getClass().getSimpleName());
            return delegate.submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            guard(task.getClass().getSimpleName());
            return delegate.submit(task, result);
        }

        @Override public void execute(Runnable command) {
            guard(command.getClass().getSimpleName());
            delegate.execute(command);
        }

        @Override public void shutdown()                           { delegate.shutdown(); }
        @Override public java.util.List<Runnable> shutdownNow()   { return delegate.shutdownNow(); }
        @Override public boolean isShutdown()                      { return delegate.isShutdown(); }
        @Override public boolean isTerminated()                    { return delegate.isTerminated(); }

        @Override
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit)
                throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends Callable<T>> tasks)
                throws InterruptedException, java.util.concurrent.ExecutionException {
            guard("invokeAny");
            return delegate.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends Callable<T>> tasks,
                               long timeout, java.util.concurrent.TimeUnit unit)
                throws InterruptedException, java.util.concurrent.ExecutionException,
                       java.util.concurrent.TimeoutException {
            guard("invokeAny");
            return delegate.invokeAny(tasks, timeout, unit);
        }

        @Override
        public <T> java.util.List<Future<T>> invokeAll(
                java.util.Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            guard("invokeAll");
            return delegate.invokeAll(tasks);
        }

        @Override
        public <T> java.util.List<Future<T>> invokeAll(
                java.util.Collection<? extends Callable<T>> tasks,
                long timeout, java.util.concurrent.TimeUnit unit)
                throws InterruptedException {
            guard("invokeAll");
            return delegate.invokeAll(tasks, timeout, unit);
        }
    }
}
