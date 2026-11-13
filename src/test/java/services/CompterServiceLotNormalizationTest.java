package services;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

import data.Compter;
import data.StockAgregate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class CompterServiceLotNormalizationTest {

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
    void findLotStockTreatsNullAndEmptyLotsAsTheSameKey() {
        try (MockedStatic<ManagedSessionFactory> msf = mockStatic(ManagedSessionFactory.class)) {
            EntityManager em = mock(EntityManager.class);
            Query query = stubQuery(mock(Query.class));
            when(em.createNativeQuery(anyString(), eq(StockAgregate.class))).thenReturn(query);
            when(query.getSingleResult()).thenThrow(new NoResultException());
            stubRead(msf, em);

            CompterService service = new CompterService();
            assertNull(service.findLotStock("prod", null, LocalDate.now(), "Lubumbashi"));

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(em).createNativeQuery(sql.capture(), eq(StockAgregate.class));
            assertTrue(sql.getValue().contains("COALESCE(p.num_lot, '')"),
                    "les lignes heritees en NULL doivent etre comparees comme le lot vide");
            verify(query).setParameter(2, "");
        }
    }

    @Test
    void findComptageForProduitNormalizesBlankLotParameter() {
        try (MockedStatic<ManagedSessionFactory> msf = mockStatic(ManagedSessionFactory.class)) {
            EntityManager em = mock(EntityManager.class);
            Query query = stubQuery(mock(Query.class));
            when(em.createNativeQuery(anyString(), eq(Compter.class))).thenReturn(query);
            when(query.getResultList()).thenReturn(List.of());
            stubRead(msf, em);

            CompterService service = new CompterService();
            service.findComptageForProduit("prod", "inv", "   ", "Lubumbashi");

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(em).createNativeQuery(sql.capture(), eq(Compter.class));
            assertTrue(sql.getValue().contains("COALESCE(p.numlot, '')"));
            verify(query).setParameter(3, "");
        }
    }

    @Test
    void distinctLotsIncludeTheBlankLotInsteadOfSkippingIt() throws Exception {
        try (MockedStatic<ManagedSessionFactory> msf = mockStatic(ManagedSessionFactory.class)) {
            EntityManager em = mock(EntityManager.class);
            Query query = stubQuery(mock(Query.class));
            when(em.createNativeQuery(anyString())).thenReturn(query);
            when(query.getResultList()).thenReturn(List.of("", "LOT-1"));
            stubRead(msf, em);

            Method method = CompterService.class.getDeclaredMethod(
                    "findAllDistinctLots", String.class, String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<String> lots = (List<String>) method.invoke(new CompterService(), "prod", "Lubumbashi");

            assertTrue(lots.contains(""), "le lot vide doit etre propose a la cloture");
            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(em).createNativeQuery(sql.capture());
            assertTrue(sql.getValue().contains("COALESCE(numlot, '')"));
            assertTrue(sql.getValue().contains("COALESCE(num_lot, '')"));
            assertFalse(sql.getValue().contains("num_lot IS NOT NULL"));
        }
    }
}
