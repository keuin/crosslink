package com.keuin.crosslink.messaging.history;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.testable.FakeEndpoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HistoricMessageRecorderTest {

    @Test
    void testTTL() throws InterruptedException {
        var recorder = new HistoricMessageRecorder(1000);
        var ep0 = new FakeEndpoint("z");
        var sender = ISender.create("sender", UUID.randomUUID());
        var msg = IMessage.create(ep0, sender, "MSG,,,");
        recorder.addMessage(msg);
        Thread.sleep(500);
        var list = recorder.getMessages();
        assertEquals(1, list.size());
        assertEquals(msg, list.get(0));
        Thread.sleep(510);
        list = recorder.getMessages();
        assertEquals(0, list.size());
    }
}