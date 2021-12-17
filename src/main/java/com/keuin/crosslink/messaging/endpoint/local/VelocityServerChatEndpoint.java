package com.keuin.crosslink.messaging.endpoint.local;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.plugin.velocity.VelocityMainWrapper;
import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VelocityServerChatEndpoint implements IEndpoint {
    private final RegisteredServer server;
    private final ProxyServer proxy;
    private final VelocityMainWrapper plugin;
    private IRouter router = null;
    private final String id;

    // adapter registered on velocity event dispatcher
    private final EventHandler<PlayerChatEvent> eventHandler;

    public VelocityServerChatEndpoint(RegisteredServer server, ProxyServer proxy, VelocityMainWrapper plugin) {
        this.server = server;
        this.proxy = proxy;
        this.plugin = plugin;
        this.id = server.getServerInfo().getName();
        this.eventHandler = (event) -> {
            Objects.requireNonNull(event);
            if (!event.getPlayer().getCurrentServer()
                    .map((ps) -> ps.getServerInfo().equals(server.getServerInfo()))
                    .orElse(false)) return; // this message does not come from the server this endpoint bound to, skip
            var textMessage = event.getMessage();
            if (textMessage.startsWith("/")) return; // do not repeat commands
            var sender = ISender.create(event.getPlayer().getUsername(), event.getPlayer().getUniqueId());
            var message = IMessage.create(VelocityServerChatEndpoint.this, sender, textMessage);
            onMessage(message);
        };
        proxy.getEventManager().register(plugin, PlayerChatEvent.class, eventHandler);
    }

    @Override
    public void sendMessage(IMessage message) {
        proxy.getAllPlayers().forEach((player) -> {
            var info = player.getCurrentServer().map(ServerConnection::getServerInfo).orElse(null);
            if (Objects.equals(info, server.getServerInfo())) {
                player.sendMessage(message.velocityDisplay());
            }
        });
    }

    @Override
    public void setRouter(IRouter router) {
        this.router = router;
    }

    @Override
    public void close() {
        proxy.getEventManager().unregister(plugin, eventHandler);
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull EndpointNamespace namespace() {
        return EndpointNamespace.SERVER;
    }

    private void onMessage(@NotNull IMessage message) {
        var rt = router;
        if (rt == null) {
            throw new IllegalStateException("No route to endpoint");
        }
        rt.sendMessage(message);
    }

    @Override
    public String toString() {
        return "VelocityServerChatEndpoint{" +
                "id='" + id + '\'' +
                '}';
    }
}
