package com.keuin.crosslink.messaging.router;

import com.keuin.crosslink.messaging.action.DropAction;
import com.keuin.crosslink.messaging.action.ReFilterAction;
import com.keuin.crosslink.messaging.action.RouteAction;
import com.keuin.crosslink.messaging.filter.IFilter;
import com.keuin.crosslink.messaging.filter.ReIdFilter;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.rule.ImmutableRule;
import com.keuin.crosslink.messaging.rule.ObjectType;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcreteRouterTest {

    private ConcreteRouter router;
    private final FakeEndpoint ep1 = new FakeEndpoint("a");
    private final FakeEndpoint ep2 = new FakeEndpoint("b");
    private final FakeEndpoint ep3 = new FakeEndpoint("c");

    @BeforeEach
    void setUp() {
        router = new ConcreteRouter();
        router.addEndpoint(ep1);
        router.addEndpoint(ep2);
        router.addEndpoint(ep3);
    }

    @Test
    void testBroadcast() throws ReIdFilter.InvalidPatternStringException {
        var ep0 = new FakeEndpoint("z");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(ep0, sender, "MSG,,,");
        var action = new RouteAction(() -> router.resolveEndpoints("server", Pattern.compile(".*")), true);
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:z"), Collections.singletonList(action));
        router.updateRuleChain(Collections.singletonList(rule));
        router.sendMessage(msg);
        assertEquals(1, ep1.messages.size());
        assertEquals(1, ep2.messages.size());
        assertEquals(1, ep3.messages.size());
        assertEquals(msg, ep1.messages.get(0));
        assertEquals(msg, ep2.messages.get(0));
        assertEquals(msg, ep3.messages.get(0));
    }

    @Test
    void testBroadcastBackflow() throws ReIdFilter.InvalidPatternStringException {
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(ep1, sender, "MSG,,,");
        var action = new RouteAction(() -> router.resolveEndpoints("server", Pattern.compile(".*")), true);
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:a"), Collections.singletonList(action));
        router.updateRuleChain(Collections.singletonList(rule));
        router.sendMessage(msg);
        assertEquals(1, ep1.messages.size());
        assertEquals(1, ep2.messages.size());
        assertEquals(1, ep3.messages.size());
        assertEquals(msg, ep1.messages.get(0));
        assertEquals(msg, ep2.messages.get(0));
        assertEquals(msg, ep3.messages.get(0));
    }

    @Test
    void testBroadcastBackflowDisabled() throws ReIdFilter.InvalidPatternStringException {
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(ep1, sender, "MSG,,,");
        var action = new RouteAction(() -> router.resolveEndpoints("server", Pattern.compile(".*")), false);
        var rule = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:a"), Collections.singletonList(action));
        router.updateRuleChain(Collections.singletonList(rule));
        router.sendMessage(msg);
        assertEquals(0, ep1.messages.size());
        assertEquals(1, ep2.messages.size());
        assertEquals(1, ep3.messages.size());
        assertEquals(msg, ep2.messages.get(0));
        assertEquals(msg, ep3.messages.get(0));
    }

    @Test
    void testFilteredMessagePass() throws ReIdFilter.InvalidPatternStringException {
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(ep1, sender, "MSG,,,");
        var filter = new ReFilterAction(Pattern.compile("asdasdasda"));
        var action = new RouteAction(() -> router.resolveEndpoints("server", Pattern.compile(".*")), false);
        var rule1 = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:.+"), Collections.singletonList(filter));
        var rule2 = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:a"), List.of(filter, action));
        var rule3 = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:a"), List.of(action));
        router.updateRuleChain(List.of(rule1, rule2, rule3));
        router.sendMessage(msg);
        assertEquals(0, ep1.messages.size());
        assertEquals(1, ep2.messages.size());
        assertEquals(1, ep3.messages.size());
        assertEquals(msg, ep2.messages.get(0));
        assertEquals(msg, ep3.messages.get(0));
    }

    @Test
    void testDropMessagePass() throws ReIdFilter.InvalidPatternStringException {
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(ep1, sender, "MSG,,,");
        var drop = new DropAction();
        var action = new RouteAction(() -> router.resolveEndpoints("server", Pattern.compile(".*")), false);
        var rule1 = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:a"), List.of(action));
        var rule2 = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:.+"), Collections.singletonList(drop));
        var rule3 = new ImmutableRule(ObjectType.CHAT_MESSAGE, IFilter.fromPatternString("server:a"), List.of(action));
        router.updateRuleChain(List.of(rule1, rule2, rule3));
        router.sendMessage(msg);
        assertEquals(0, ep1.messages.size());
        assertEquals(1, ep2.messages.size());
        assertEquals(1, ep3.messages.size());
        assertEquals(msg, ep2.messages.get(0));
        assertEquals(msg, ep3.messages.get(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        ep1.close();
        ep2.close();
        ep3.close();
        router.close();
    }
}