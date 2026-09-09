package tools;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class SyncRetryHandler {

    private static final int DEFAULT_MAX_RETRY = 5;
    private static final long BASE_DELAY_MS = 200;

    private SyncRetryHandler() {
    }

    @FunctionalInterface
    public interface SyncOperation<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    public interface VoidSyncOperation {
        void call() throws Exception;
    }

    public static <T> T retry(String entityName, String entityId, SyncOperation<T> operation)
            throws Exception {
        return retry(entityName, entityId, operation, DEFAULT_MAX_RETRY);
    }

    public static <T> T retry(String entityName, String entityId, SyncOperation<T> operation, int maxRetries)
            throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                System.err.println("SyncRetryHandler: échec tentative " + (attempt + 1) + "/" + maxRetries
                        + " pour " + entityName + " (" + entityId + "): " + e.getMessage());
                if (isPermanent(e)) {
                    SyncLogger.getInstance().log(lastException,
                            "SyncRetryHandler: erreur defininitive (serialisation/conversion) non retentee", entityName, entityId);
                    throw lastException;
                }
                if (attempt < maxRetries - 1) {
                    long delay = BASE_DELAY_MS * (long) Math.pow(2, attempt);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interruption pendant le backoff", ie);
                    }
                }
            }
        }
        SyncLogger.getInstance().log(lastException, "SyncRetryHandler: échec après " + maxRetries + " tentatives",
                entityName, entityId);
        throw lastException;
    }

    public static void retryVoid(String entityName, String entityId, VoidSyncOperation operation)
            throws Exception {
        retryVoid(entityName, entityId, operation, DEFAULT_MAX_RETRY);
    }

    public static void retryVoid(String entityName, String entityId, VoidSyncOperation operation, int maxRetries)
            throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                operation.call();
                return;
            } catch (Exception e) {
                lastException = e;
                System.err.println("SyncRetryHandler: échec tentative " + (attempt + 1) + "/" + maxRetries
                        + " pour " + entityName + " (" + entityId + "): " + e.getMessage());
                if (isPermanent(e)) {
                    SyncLogger.getInstance().log(lastException,
                            "SyncRetryHandler: erreur defininitive (serialisation/conversion) non retentee", entityName, entityId);
                    throw lastException;
                }
                if (attempt < maxRetries - 1) {
                    long delay = BASE_DELAY_MS * (long) Math.pow(2, attempt);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interruption pendant le backoff", ie);
                    }
                }
            }
        }
        SyncLogger.getInstance().log(lastException, "SyncRetryHandler: échec après " + maxRetries + " tentatives",
                entityName, entityId);
        throw lastException;
    }

    /**
     * Detecte une erreur <em>definitive</em> (non transitoire) : un echec de
     * serialisation/conversion du corps HTTP, typiquement reporter par
     * Retrofit/Jackson sous la forme "Unable to convert X to RequestBody" (y
     * compris la {@code JsonProcessingException} sous-jacente). Ce type d'erreur
     * est un bug de code : il ne se resoudra jamais par une nouvelle tentative,
     * donc on abandonne immediatement au lieu de faire X retries inutiles avec
     * backoff.
     *
     * On se base sur le nom de classe (pas de dependance compile-time vers
     * Jackson ici) et sur le message signature de Retrofit.
     *
     * @return {@code true} si l'erreur est definitive (a ne pas retenter).
     */
    private static boolean isPermanent(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String cn = c.getClass().getName();
            if (cn.startsWith("com.fasterxml.jackson.core") || cn.startsWith("com.fasterxml.jackson.databind")) {
                return true;
            }
            String msg = c.getMessage();
            if (msg != null && msg.contains("Unable to convert ") && msg.contains(" to RequestBody")) {
                return true;
            }
        }
        return false;
    }
}
