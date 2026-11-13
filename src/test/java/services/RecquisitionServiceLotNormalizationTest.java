package services;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import data.Recquisition;
import data.StockAgregate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class RecquisitionServiceLotNormalizationTest {

    @SuppressWarnings("unchecked")
    private static void stubRead(MockedStatic<ManagedSessionFactory> msf, EntityManager em) {
        msf.when(ManagedSessionFactory::isEmbedded).thenReturn(true);
        msf.when(() -> ManagedSessionFactory.executeRead(any()))
                .thenAnswer(inv -> ((Function<EntityManager, ?>) inv.getArgument(0)).apply(em));
    }

    private static Query stubQuery(Query query) {
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        return query;
    }

    @Test
    void findRecquisitionByProduitTreatsNullLotAsEmptyKey() {
        try (MockedStatic<ManagedSessionFactory> msf = mockStatic(ManagedSessionFactory.class)) {
            EntityManager em = mock(EntityManager.class);
            Query query = stubQuery(mock(Query.class));
            when(em.createNativeQuery(anyString(), eq(Recquisition.class))).thenReturn(query);
            when(query.getResultList()).thenReturn(List.of());
            stubRead(msf, em);

            new RecquisitionService().findRecquisitionByProduit("prod", null, "Lubumbashi");

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(em).createNativeQuery(sql.capture(), eq(Recquisition.class));
            assertTrue(sql.getValue().contains("COALESCE(s.numlot, '')"));
            verify(query).setParameter(2, "");
        }
    }

    @Test
    void findClosedStockByLotTreatsBlankLotAsEmptyKey() {
        try (MockedStatic<ManagedSessionFactory> msf = mockStatic(ManagedSessionFactory.class)) {
            EntityManager em = mock(EntityManager.class);
            Query query = stubQuery(mock(Query.class));
            when(em.createNativeQuery(anyString(), eq(StockAgregate.class))).thenReturn(query);
            when(query.getResultList()).thenReturn(List.of());
            stubRead(msf, em);

            LocalDate today = LocalDate.of(2026, 11, 13);
            assertNull(new RecquisitionService().findClosedStockByLot(
                    today, today, "prod", "Lubumbashi", "   ", "Journalier"));

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(em).createNativeQuery(sql.capture(), eq(StockAgregate.class));
            assertTrue(sql.getValue().contains("COALESCE(s.num_lot, '')"));
            verify(query).setParameter(5, "");
        }
    }
}
