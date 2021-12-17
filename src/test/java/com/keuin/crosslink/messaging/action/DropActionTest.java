package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropActionTest {

    @Test
    void testDrop() {
        var action = new DropAction();
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message = IMessage.create(source, sender, "message");
        var result = action.process(message);
        assertTrue(result.isDropped());
        assertFalse(result.isFiltered());
    }
}