package services;

import data.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService();
    }

    @Test
    void testIsUniqueConstraintViolation_General() {
        Exception e = new RuntimeException("duplicate entry for key 'PRIMARY'");
        assertTrue(invokeIsUniqueConstraintViolation(e));

        Exception e2 = new RuntimeException("unique constraint failed");
        assertTrue(invokeIsUniqueConstraintViolation(e2));
    }

    @Test
    void testIsUniqueConstraintViolation_SQL() {
        Exception e = new SQLIntegrityConstraintViolationException("integrity violation");
        assertTrue(invokeIsUniqueConstraintViolation(e));
    }

    @Test
    void testIsUniqueConstraintViolation_False() {
        Exception e = new RuntimeException("some other error");
        assertFalse(invokeIsUniqueConstraintViolation(e));
    }

    // Helper to invoke private method via reflection if needed, but here we can
    // just test the logic if it was public or protected.
    // Since it's private and we want to test it, we might consider making it
    // protected or using reflection.
    // For simplicity in this test, I'll assume we can test it through a public
    // method that uses it.

    private boolean invokeIsUniqueConstraintViolation(Exception e) {
        // This is a bit of a hack since the method is private, ideally it should be
        // tested via createClient
        // but for unit testing the logic specifically:
        try {
            java.lang.reflect.Method method = ClientService.class.getDeclaredMethod("isUniqueConstraintViolation",
                    Exception.class);
            method.setAccessible(true);
            return (boolean) method.invoke(clientService, e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
