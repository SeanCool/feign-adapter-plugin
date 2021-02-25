package com.s6.plugin.feign.adapter.utils;

import static org.junit.Assert.fail;

public class AssertTestUtils {
    private AssertTestUtils() {

    }

    public static void assertArrayNotEquals(byte[] array1, byte[] array2) {
        if (array1 == array2) {
            fail();
        }
        if (array1 == null || array2 == null) {
            return;
        }
        if (array1.length != array2.length) {
            return;
        }
        int equalCount = 0;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                equalCount++;
            }
        }
        if (equalCount == array1.length) {
            fail();
        }
    }
}
