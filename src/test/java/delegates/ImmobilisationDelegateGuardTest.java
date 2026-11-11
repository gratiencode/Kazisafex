package delegates;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class ImmobilisationDelegateGuardTest {

    @Test
    void deleteNullImmobilisationIsSafe() {
        assertDoesNotThrow(() -> ImmobilisationDelegate.deleteImmobilisation(null));
    }
}
