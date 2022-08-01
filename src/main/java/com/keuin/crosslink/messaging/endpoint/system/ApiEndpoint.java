package com.keuin.crosslink.messaging.endpoint.system;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.sender.ISender;
import org.jetbrains.annotations.NotNull;

public class ApiEndpoint implements IEndpoint {

    public static final ApiEndpoint INSTANCE = new ApiEndpoint(); // For simplicity, we use singleton.
    private IRouter router;

    private ApiEndpoint() {
    }

    /**
     * The actual API provides message sent from this endpoint.
     *
     * @param sender  message sender.
     * @param content message content.
     */
    public void offerMessage(@NotNull ISender sender, @NotNull String content) {
        router.sendMessage(IMessage.create(this, sender, content));
    }

    @Override
    public void sendMessage(IMessage message) {
        // This endpoint does not receive messages,
        // so we do nothing here.
    }

    @Override
    public void setRouter(IRouter router) {
        this.router = router;
    }

    @Override
    public void close() {
        // Since sendMessage is empty, we do nothing here.
    }

    @Override
    public @NotNull String id() {
        return "system"; // TODO move this into config file
    }

    @Override
    public @NotNull EndpointNamespace namespace() {
        return EndpointNamespace.SYSTEM;
    }
}
