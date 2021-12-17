package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Re2placeActionTest {
    @Test
    public void testRemoveHeadingSharp() {
        var action = new Re2placeAction(Pattern.compile("^#(.*)"), "$1");
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message = IMessage.create(source, sender, "#message");
        assertEquals("#message", message.pureString());
        assertEquals("message", Objects.requireNonNull(action.process(message).getResult()).pureString());
        message = IMessage.create(source, sender, "message");
        assertEquals("message", message.pureString());
        assertEquals("message", Objects.requireNonNull(action.process(message).getResult()).pureString());
        message = IMessage.create(source, sender, "mess#age");
        assertEquals("mess#age", message.pureString());
        assertEquals("mess#age", Objects.requireNonNull(action.process(message).getResult()).pureString());
    }
}