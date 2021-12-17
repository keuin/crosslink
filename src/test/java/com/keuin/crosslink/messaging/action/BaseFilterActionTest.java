package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaseFilterActionTest {
    @Test
    void processAlwaysTrue() {
        var action = new BaseFilterAction(m -> true);
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message = IMessage.create(source, sender, "message");
        assertTrue(action.process(message).isValid());
        assertFalse(action.process(message).isDropped());
        assertFalse(action.process(message).isFiltered());
        assertNotNull(action.process(message).getResult());
    }

    @Test
    void processAlwaysFalse() {
        var action = new BaseFilterAction(m -> false);
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message = IMessage.create(source, sender, "message");
        var result = action.process(message);
        assertFalse(result.isValid());
        assertFalse(result.isDropped());
        assertTrue(result.isFiltered());
    }
}