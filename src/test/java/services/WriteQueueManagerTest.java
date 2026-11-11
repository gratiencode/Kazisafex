package services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManagerFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class WriteQueueManagerTest {

    @Test
    void entityManagerCreationFailureCompletesFuture() {
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        when(emf.createEntityManager()).thenThrow(new IllegalStateException("pool exhausted"));
        WriteQueueManager queue = new WriteQueueManager(emf);
        try {
            var future = queue.submit(em -> "never");
            assertThrows(ExecutionException.class,
                    () -> future.get(2, TimeUnit.SECONDS));
        } finally {
            queue.shutdown();
        }
    }
}
