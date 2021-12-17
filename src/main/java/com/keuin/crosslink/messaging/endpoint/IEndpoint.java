package com.keuin.crosslink.messaging.endpoint;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import org.jetbrains.annotations.NotNull;

public interface IEndpoint {
    void sendMessage(IMessage message);
    void setRouter(IRouter router);
    void close();

    /**
     * Get the identifier of this endpoint.
     * @return the identifier.
     */
    @NotNull String id();

    @NotNull EndpointNamespace namespace();

    default @NotNull String friendlyName() {
        return id();
    }

    default String namespacedId() {
        return String.format("%s:%s", namespace(), id());
    }
}
