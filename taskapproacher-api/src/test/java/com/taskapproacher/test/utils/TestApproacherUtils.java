package com.taskapproacher.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestApproacherUtils {
    public static void assertStringsIfNotEmpty(Object firstValue, Object secondValue) {
        String firstString = (String) firstValue;
        String secondString = (String) secondValue;

        if (firstString.isEmpty() || secondString.isEmpty()) {
            return;
        }
        assertEquals(firstValue, secondValue);
    }

    public static void assertEqualsIfNotNull(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            return;
        }

        if (firstValue instanceof String) {
            assertStringsIfNotEmpty(firstValue, secondValue);
            return;
        }
        assertEquals(firstValue, secondValue);
    }
}
