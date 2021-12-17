package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ReFilterActionTest {
    @Test
    public void testFilterHeadingSharp() {
        var action = new ReFilterAction(Pattern.compile("#.*"));
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message1 = IMessage.create(source, sender, "#good");
        var message2 = IMessage.create(source, sender, "bad");
        assertFalse(action.process(message1).isFiltered());
        assertFalse(action.process(message1).isDropped());
        assertNotNull(action.process(message1).getResult());
        assertTrue(action.process(message2).isFiltered());
        assertFalse(action.process(message2).isDropped());
    }

    @Test
    public void testFilterAlwaysTrue() {
        var action = new ReFilterAction(Pattern.compile(".*"));
        var sender = ISender.create("123", UUID.randomUUID());
        var source = new FakeEndpoint();
        var message1 = IMessage.create(source, sender, "#good");
        var message2 = IMessage.create(source, sender, "bad");
        assertFalse(action.process(message1).isFiltered());
        assertFalse(action.process(message1).isDropped());
        assertNotNull(action.process(message1).getResult());
        assertFalse(action.process(message2).isFiltered());
        assertFalse(action.process(message2).isDropped());
        assertNotNull(action.process(message2).getResult());
    }
}