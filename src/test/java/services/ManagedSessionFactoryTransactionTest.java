package services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Non-régression : {@link ManagedSessionFactory#executeWrite} ne doit pas
 * faire de begin() imbriqué quand une transaction est déjà active sur
 * l'EntityManager du thread (cas du downsync SSE exécuté dans la session
 * ouverte par NotificationHandler#executeDownsyncMutation).
 */
@DisplayName("ManagedSessionFactory.executeWrite — transaction déjà active")
class ManagedSessionFactoryTransactionTest {

    private EntityManager em;
    private EntityTransaction tx;
    private MockedStatic<ManagedSessionFactory> msf;

    private void setUp(boolean txAlreadyActive) {
        em = mock(EntityManager.class);
        tx = mock(EntityTransaction.class);
        java.util.concurrent.atomic.AtomicBoolean active =
                new java.util.concurrent.atomic.AtomicBoolean(txAlreadyActive);
        when(em.getTransaction()).thenReturn(tx);
        when(tx.isActive()).thenAnswer(inv -> active.get());
        doAnswer(inv -> {
            active.set(true);
            return null;
        }).when(tx).begin();
        doAnswer(inv -> {
            active.set(false);
            return null;
        }).when(tx).commit();
        doAnswer(inv -> {
            active.set(false);
            return null;
        }).when(tx).rollback();
        msf = mockStatic(ManagedSessionFactory.class);
        msf.when(ManagedSessionFactory::isEmbedded).thenReturn(false);
        msf.when(ManagedSessionFactory::getEntityManager).thenReturn(em);
        msf.when(() -> ManagedSessionFactory.executeWrite(any())).thenCallRealMethod();
        msf.when(() -> ManagedSessionFactory.withCurrentEntityManager(
                any(EntityManager.class), any(java.util.function.Supplier.class)))
                .thenCallRealMethod();
    }

    private void tearDown() {
        if (msf != null) {
            msf.close();
        }
    }

    @Test
    @DisplayName("À la racine: begin + commit + fermeture, comme avant")
    void standaloneOwnsItsTransaction() {
        setUp(false);
        try {
            AtomicBoolean ran = new AtomicBoolean(false);
            ManagedSessionFactory.executeWrite(e -> {
                ran.set(true);
                return e;
            });

            assertTrue(ran.get(), "L'action doit être exécutée");
            verify(tx).begin();
            verify(tx).commit();
            verify(tx, never()).rollback();
            msf.verify(() -> ManagedSessionFactory.closeEntityManager(), times(1));
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Rollback + rethrow quand l'action échoue à la racine")
    void standaloneRollsBackOnFailure() {
        setUp(false);
        try {
            assertThrows(IllegalStateException.class,
                    () -> ManagedSessionFactory.executeWrite(e -> {
                        throw new IllegalStateException("boom");
                    }));
            verify(tx).begin();
            verify(tx, never()).commit();
            verify(tx).rollback();
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Dans une transaction déjà active: pas de begin/commit/rollback/fermeture imbriqués")
    void joinsActiveTransactionWithoutNestedBegin() {
        setUp(true);
        try {
            AtomicBoolean ran = new AtomicBoolean(false);
            ManagedSessionFactory.executeWrite(e -> {
                ran.set(true);
                return e;
            });

            assertTrue(ran.get(), "L'action doit être exécutée dans la transaction courante");
            verify(tx, never()).begin();
            verify(tx, never()).commit();
            verify(tx, never()).rollback();
            msf.verify(() -> ManagedSessionFactory.closeEntityManager(), never());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Dans une transaction déjà active: une exception n'est ni commitée ni rollbackée par le nid intérieur")
    void joinedTransactionDoesNotTouchOuterTransactionOnFailure() {
        setUp(true);
        try {
            assertThrows(IllegalStateException.class,
                    () -> ManagedSessionFactory.executeWrite(e -> {
                        throw new IllegalStateException("boom");
                    }));
            verify(tx, never()).begin();
            verify(tx, never()).commit();
            verify(tx, never()).rollback();
            msf.verify(() -> ManagedSessionFactory.closeEntityManager(), never());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Un échec de begin ferme quand même l'EntityManager")
    void beginFailureClosesEntityManager() {
        setUp(false);
        try {
            doThrow(new IllegalStateException("begin refused")).when(tx).begin();
            assertThrows(IllegalStateException.class,
                    () -> ManagedSessionFactory.executeWrite(e -> e));
            verify(tx, never()).commit();
            msf.verify(() -> ManagedSessionFactory.closeEntityManager(), times(1));
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Une écriture imbriquée rejoint la transaction au lieu de la file")
    void nestedWritesJoinTheCurrentTransactionInsteadOfQueueing() {
        EntityManager nestedEm = mock(EntityManager.class);

        String result = ManagedSessionFactory.withCurrentEntityManager(nestedEm, () -> {
            CompletableFuture<String> nested = ManagedSessionFactory.submitWrite(ignored -> "ok");
            assertTrue(nested.isDone(), "une écriture imbriquée doit s'exécuter immédiatement");
            return nested.join();
        });

        assertEquals("ok", result);
        verifyNoInteractions(nestedEm);
    }

    @Test
    @DisplayName("Les lectures/écritures imbriquées réutilisent l'EntityManager courant")
    void nestedReadsAndWritesReuseTheCurrentEntityManager() {
        EntityManager nestedEm = mock(EntityManager.class);
        AtomicReference<EntityManager> readEm = new AtomicReference<>();
        AtomicReference<EntityManager> writeEm = new AtomicReference<>();

        ManagedSessionFactory.withCurrentEntityManager(nestedEm, () -> {
            ManagedSessionFactory.executeRead(ignored -> {
                readEm.set(ignored);
                return null;
            });
            ManagedSessionFactory.executeWrite(ignored -> {
                writeEm.set(ignored);
                return null;
            });
            return null;
        });

        assertSame(nestedEm, readEm.get());
        assertSame(nestedEm, writeEm.get());
        verifyNoInteractions(nestedEm);
    }
}
