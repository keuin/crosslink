package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RouteActionTest {

    @Test
    void testRouteNoBackflow() {
        var dest1 = new FakeEndpoint("a");
        var dest2 = new FakeEndpoint("b");
        var dest3 = new FakeEndpoint("c");
        var dests = new HashSet<IEndpoint>(Arrays.asList(dest1, dest2, dest3));
        var action = new RouteAction(() -> dests, false);
        var sender = ISender.create("123", UUID.randomUUID());
        var message = IMessage.create(dest1, sender, "message");
        assertSame(message, action.process(message).getResult());
        assertTrue(dest1.messages.isEmpty());
        assertEquals(1, dest2.messages.size());
        assertEquals(1, dest3.messages.size());
    }

    @Test
    void testRouteWithBackflow() {
        var dest1 = new FakeEndpoint("a");
        var dest2 = new FakeEndpoint("b");
        var dest3 = new FakeEndpoint("c");
        var dests = new HashSet<IEndpoint>(Arrays.asList(dest1, dest2, dest3));
        var action = new RouteAction(() -> dests, true);
        var sender = ISender.create("123", UUID.randomUUID());
        var message = IMessage.create(dest1, sender, "message");
        assertSame(message, action.process(message).getResult());
        assertEquals(1, dest1.messages.size());
        assertEquals(1, dest2.messages.size());
        assertEquals(1, dest3.messages.size());
    }
}
