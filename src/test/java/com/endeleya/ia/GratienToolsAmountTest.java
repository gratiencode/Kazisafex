package com.endeleya.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class GratienToolsAmountTest {

    @Test
    void formatsWholeAndDecimalAmountsStably() throws Exception {
        Method m = GratienTools.class.getDeclaredMethod("purchaseAmount", double.class);
        m.setAccessible(true);

        assertEquals("1", m.invoke(new GratienTools(), 1.0d));
        assertEquals("1.01", m.invoke(new GratienTools(), 1.005d));
        assertEquals("0.1", m.invoke(new GratienTools(), 0.1d));
    }
}
