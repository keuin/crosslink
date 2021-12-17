package com.keuin.crosslink.messaging.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IFilterTest {

    @Test
    void fromPatternString() throws ReIdFilter.InvalidPatternStringException {
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(":::"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("::"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(":"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(""));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server:::"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server::"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server:"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(":server::"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(":server:"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(":server"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString(":::server"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("::server"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server:"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server:bbb:"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server:aaa:bbb"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("remote:aa\n"));
        assertThrows(ReIdFilter.InvalidPatternStringException.class, () -> IFilter.fromPatternString("server:aa\n"));
        assertDoesNotThrow(() -> IFilter.fromPatternString("server:b"));
        assertDoesNotThrow(() -> IFilter.fromPatternString("remote:bbb"));
    }
}