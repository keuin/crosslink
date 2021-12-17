package com.keuin.crosslink.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LazyEvaluatedTest {

    @Test
    void get() {
        var flag = new AtomicBoolean(false); // if evaluated
        var cnt = new AtomicInteger(0); // counter
        var le = new LazyEvaluated<>(() -> {
            flag.set(true);
            return cnt.getAndIncrement();
        });
        assertFalse(flag.get());
        assertEquals(0, le.get());
        assertTrue(flag.get());
        assertEquals(0, le.get());
        assertEquals(0, le.get());
        assertEquals(0, le.get());
        assertEquals(0, le.get());
    }
}