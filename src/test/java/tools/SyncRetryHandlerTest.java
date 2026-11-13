package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SyncRetryHandlerTest {

    @Test
    void retriesTransientFailuresUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = SyncRetryHandler.retry("PRODUIT", "prod-1", () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IOException("connexion interrompue");
            }
            return "ok";
        }, 3);

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void doesNotRetryPermanentSerializationError() {
        AtomicInteger attempts = new AtomicInteger();
        IOException permanent = new IOException("Unable to convert data to RequestBody");

        IOException error = assertThrows(IOException.class,
                () -> SyncRetryHandler.retryVoid("VENTE", "vente-1", () -> {
                    attempts.incrementAndGet();
                    throw permanent;
                }, 5));

        assertEquals(permanent, error);
        assertEquals(1, attempts.get(), "une erreur definitive ne doit pas etre retentee");
    }

    @Test
    void stopsWhenInterruptedDuringBackoff() {
        Thread.currentThread().interrupt();
        try {
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> SyncRetryHandler.retryVoid("COMPTER", "compter-1",
                            () -> { throw new IOException("reseau indisponible"); }, 3));
            assertTrue(error.getMessage().contains("Interruption"));
        } finally {
            Thread.interrupted();
        }
    }
}
