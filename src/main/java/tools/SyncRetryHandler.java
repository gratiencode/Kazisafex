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
}
