package com.keuin.crosslink.messaging.message;

import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentBackedMessageTest {

    @Test
    void testPureString() {
        var source = new FakeEndpoint("endpoint");
        var sender = ISender.create("sender", UUID.randomUUID());
        var comp = Component.text("text").append(Component.text("message"));
        var msg = new ComponentBackedMessage(source, sender, comp);
        assertEquals("textmessage", msg.pureString());
    }
}