package com.keuin.crosslink.messaging.rule;

import com.keuin.crosslink.messaging.action.Re2placeAction;
import com.keuin.crosslink.messaging.action.ReFilterAction;
import com.keuin.crosslink.messaging.filter.IFilter;
import com.keuin.crosslink.messaging.filter.ReIdFilter;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableRuleTest {

    @Test
    void testEmptyRule() throws ReIdFilter.InvalidPatternStringException {
        var fromFilter = IFilter.fromPatternString("server:.*");
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, fromFilter, Collections.emptyList());
        var source = new FakeEndpoint("endpoint");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(source, sender, "message");
        var result = rule.process(msg);
        assertTrue(result.isValid());
        assertEquals(msg, result.getResult());
    }

    @Test
    void testSingleFilter() throws ReIdFilter.InvalidPatternStringException {
        var fromFilter = IFilter.fromPatternString("server:.*");
        var msgFilter = new ReFilterAction(Pattern.compile("mess..."));
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, fromFilter, Collections.singletonList(msgFilter));
        var source = new FakeEndpoint("endpoint");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(source, sender, "message");
        var result = rule.process(msg);
        assertTrue(result.isValid());
        assertEquals(msg, result.getResult());
    }

    @Test
    void testFilterReplace() throws ReIdFilter.InvalidPatternStringException {
        var fromFilter = IFilter.fromPatternString("server:.*");
        var msgFilter = new ReFilterAction(Pattern.compile("mess..."));
        var msgReplace = new Re2placeAction(Pattern.compile("me."), "u");
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, fromFilter, List.of(msgFilter, msgReplace));
        var source = new FakeEndpoint("endpoint");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(source, sender, "message");
        var result = rule.process(msg);
        assertTrue(result.isValid());
        assertFalse(result.isFiltered());
        assertFalse(result.isDropped());
        assertEquals("usage", Objects.requireNonNull(result.getResult()).pureString());
    }

    @Test
    void testFilterReplaceFilter1() throws ReIdFilter.InvalidPatternStringException {
        var fromFilter = IFilter.fromPatternString("server:.*");
        var msgFilter = new ReFilterAction(Pattern.compile("mess..."));
        var msgReplace = new Re2placeAction(Pattern.compile("me."), "u");
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, fromFilter, List.of(msgFilter, msgReplace, msgFilter));
        var source = new FakeEndpoint("endpoint");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(source, sender, "message");
        var result = rule.process(msg);
        assertTrue(result.isFiltered());
        assertFalse(result.isDropped());
        assertFalse(result.isValid());
    }

    @Test
    void testFilterReplaceFilter2() throws ReIdFilter.InvalidPatternStringException {
        var fromFilter = IFilter.fromPatternString("server:.*");
        var msgFilter = new ReFilterAction(Pattern.compile("mess..."));
        var msgReplace = new Re2placeAction(Pattern.compile("me."), "u");
        var msgFilter2 = new ReFilterAction(Pattern.compile("us..."));
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, fromFilter, List.of(msgFilter, msgReplace, msgFilter2));
        var source = new FakeEndpoint("endpoint");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(source, sender, "message");
        var result = rule.process(msg);
        assertFalse(result.isFiltered());
        assertFalse(result.isDropped());
        assertTrue(result.isValid());
        assertEquals("usage", Objects.requireNonNull(result.getResult()).pureString());
    }
}