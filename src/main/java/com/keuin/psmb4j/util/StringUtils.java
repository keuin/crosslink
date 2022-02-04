package com.keuin.psmb4j.util;

import java.nio.charset.StandardCharsets;

public class StringUtils {
    /**
     * If the string can be encoded into binary using ASCII.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPureAscii(String v) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(v);
        // or "ISO-8859-1" for ISO Latin 1
    }
}
