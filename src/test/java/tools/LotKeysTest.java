package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LotKeysTest {

    @Test
    void nullLotBecomesEmptyKey() {
        assertEquals("", LotKeys.normalize(null));
    }

    @Test
    void surroundingSpacesAreRemoved() {
        assertEquals("LOT-42", LotKeys.normalize("  LOT-42  "));
    }

    @Test
    void blankLotBecomesEmptyKey() {
        assertEquals("", LotKeys.normalize("   "));
    }

    @Test
    void alreadyNormalizedValueIsReturnedUnchanged() {
        assertEquals("LOT-1", LotKeys.normalize("LOT-1"));
    }
}
