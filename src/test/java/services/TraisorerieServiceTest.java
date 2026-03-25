package services;

import data.Traisorerie;
import data.CompteTresor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraisorerieServiceTest {

    private TraisorerieService traisorerieService;

    @BeforeEach
    void setUp() {
        traisorerieService = new TraisorerieService();
    }

    @Test
    void testSumByReference_Embedded() {
        try (MockedStatic<ManagedSessionFactory> mockedFactory = mockStatic(ManagedSessionFactory.class)) {
            mockedFactory.when(ManagedSessionFactory::isEmbedded).thenReturn(true);

            String ref = "REF001";
            double taux = 2500.0;
            double expectedSum = 100.0;

            mockedFactory.when(() -> ManagedSessionFactory.executeRead(any())).thenReturn(expectedSum);

            Double actualSum = traisorerieService.sumByReference(ref, taux);

            assertEquals(expectedSum, actualSum);
        }
    }

    @Test
    void testFindCurrentBalanceUsd_NullResult() {
        try (MockedStatic<ManagedSessionFactory> mockedFactory = mockStatic(ManagedSessionFactory.class)) {
            mockedFactory.when(ManagedSessionFactory::isEmbedded).thenReturn(true);
            mockedFactory.when(() -> ManagedSessionFactory.executeRead(any())).thenReturn(null);

            double balance = traisorerieService.findCurrentBalanceUsd("TID", LocalDate.now(), LocalDate.now(), "REG");

            assertEquals(0.0, balance);
        }
    }

    @Test
    void testFindExistingOf_EmptyList() {
        try (MockedStatic<ManagedSessionFactory> mockedFactory = mockStatic(ManagedSessionFactory.class)) {
            mockedFactory.when(ManagedSessionFactory::isEmbedded).thenReturn(true);
            mockedFactory.when(() -> ManagedSessionFactory.executeRead(any())).thenReturn(new ArrayList<>());

            Traisorerie existing = traisorerieService.findExistingOf("REF", LocalDate.now(), "TID", "REG");

            assertNull(existing);
        }
    }
}
