/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;



import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


/**
 *
 * @author endeleya
 */

public class WriteQueueManager {

    private final EntityManagerFactory emf;
    private final BlockingQueue<WriteTask<?>> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    public WriteQueueManager(EntityManagerFactory emf) {
        this.emf = emf;
        startWorker();
    }

    /**
     * Soumettre une écriture en base.
     */
    public <T> CompletableFuture<T> submit(Function<EntityManager, T> action) {
        CompletableFuture<T> future = new CompletableFuture<>();
        queue.add(new WriteTask<>(action, future));
        return future;
    }

    private void startWorker() {
        Thread worker = new Thread(() -> {
            while (running.get()) {
                try {
                    WriteTask<?> task = queue.take();
                    executeTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Kazisafe-SQLite-WriteQueueWorker");
        worker.setDaemon(true);
        worker.start();
    }

    private <T> void executeTask(WriteTask<T> task) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            T result = task.getAction().apply(em);
            em.getTransaction().commit();
            task.getFuture().complete(result);
        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            task.getFuture().completeExceptionally(ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void shutdown() {
        running.set(false);
    }
}

