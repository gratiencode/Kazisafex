package services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import data.Inventaire;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class InventaireServiceTest {

    @Test
    void createWaitsForLocalFuture() {
        Inventaire inv = new Inventaire();
        inv.setCodeInventaire("INV-WAIT");
        CompletableFuture<Inventaire> future = spy(new CompletableFuture<>());
        future.complete(inv);

        try (MockedStatic<ManagedSessionFactory> msf = mockStatic(ManagedSessionFactory.class)) {
            msf.when(ManagedSessionFactory::isEmbedded).thenReturn(true);
            msf.when(() -> ManagedSessionFactory.submitWrite(any(Function.class)))
                    .thenReturn(future);

            new InventaireService().createInventaire(inv);

            verify(future).join();
        }
    }
}
