package com.keuin.crosslink.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggerNamingTest {

    @Test
    void name() {
        assertEquals("crosslink", LoggerNaming.name().toString());
        assertEquals("crosslink.actions.replace", LoggerNaming.name().of("actions").of("replace").toString());
    }
}