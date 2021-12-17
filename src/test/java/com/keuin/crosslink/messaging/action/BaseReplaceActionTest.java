package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseReplaceActionTest {
    @Test
    void processSimpleReplace() {
        var action = new BaseReplaceAction((msg) -> IMessage.create(msg.source(), msg.sender(), "replaced"));
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message = IMessage.create(source, sender, "message");
        assertEquals("message", message.pureString());
        assertEquals("replaced", Objects.requireNonNull(action.process(message).getResult()).pureString());
    }
}