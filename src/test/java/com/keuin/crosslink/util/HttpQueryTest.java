package com.keuin.crosslink.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpQueryTest {
    @Test
    void testMixedFlagValue() {
        var map = HttpQuery.getParamMap("var1=1&flag1");
        assertTrue(map.containsKey("flag1"));
        assertTrue(map.containsKey("var1"));
        assertEquals("1", map.get("var1"));
        assertEquals("", map.get("flag1"));
    }

    @Test
    void testSingleFlag() {
        var map = HttpQuery.getParamMap("flag1");
        assertTrue(map.containsKey("flag1"));
        assertEquals("", map.get("flag1"));
    }

    @Test
    void testSingleVar() {
        var map = HttpQuery.getParamMap("var1=value");
        assertTrue(map.containsKey("var1"));
        assertEquals("value", map.get("var1"));
    }

    @Test
    void testMultipleValue() {
        var map = HttpQuery.getParamMap("var1=111&var2=222");
        assertTrue(map.containsKey("var1"));
        assertTrue(map.containsKey("var2"));
        assertEquals("111", map.get("var1"));
        assertEquals("222", map.get("var2"));
    }

    @Test
    void testMultipleKey() {
        var map = HttpQuery.getParamMap("flag1&flag2");
        assertTrue(map.containsKey("flag1"));
        assertTrue(map.containsKey("flag2"));
        assertEquals("", map.get("flag1"));
        assertEquals("", map.get("flag2"));
    }
}