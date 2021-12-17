package com.keuin.crosslink.testable;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FakeEndpoint implements IEndpoint {
    public final List<IMessage> messages = new ArrayList<>();
    public IRouter router = null;
    private final String id;

    public FakeEndpoint(String id) {
        this.id = id;
    }

    public FakeEndpoint() {
        this.id = "fake";
    }

    @Override
    public void sendMessage(IMessage message) {
        Objects.requireNonNull(message);
        messages.add(message);
    }

    @Override
    public void setRouter(IRouter router) {
        this.router = router;
    }

    @Override
    public void close() {

    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull EndpointNamespace namespace() {
        return EndpointNamespace.SERVER;
    }
}
