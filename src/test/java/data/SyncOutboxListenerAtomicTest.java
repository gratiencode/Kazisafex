package data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import services.ManagedSessionFactory;

class SyncOutboxListenerAtomicTest {

    @Test
    void writesOutboxInCurrentEntityManagerWithoutAsyncExecutor() {
        EntityManager em = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<SyncOutbox> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(SyncOutbox.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        Client client = new Client("client-atomic-test", "Client atomique");
        SyncOutboxListener listener = new SyncOutboxListener();

        ManagedSessionFactory.withCurrentEntityManager(em, () -> {
            listener.onPrePersist(client);
            return null;
        });

        verify(em).persist(any(SyncOutbox.class));
    }

    @Test
    void queryFailureAbortsInsteadOfCreatingDuplicate() {
        EntityManager em = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<SyncOutbox> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(SyncOutbox.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenThrow(new IllegalStateException("db down"));

        Client client = new Client("client-query-fail", "Client erreur");
        SyncOutboxListener listener = new SyncOutboxListener();

        assertThrows(IllegalStateException.class, () ->
                ManagedSessionFactory.withCurrentEntityManager(em, () -> {
                    listener.onPrePersist(client);
                    return null;
                }));
        verify(em, never()).persist(any(SyncOutbox.class));
    }
}
