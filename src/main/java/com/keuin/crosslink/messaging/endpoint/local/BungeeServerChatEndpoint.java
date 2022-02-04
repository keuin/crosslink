package com.keuin.crosslink.messaging.endpoint.local;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.plugin.bungee.BungeeMainWrapper;
import com.keuin.crosslink.util.LoggerNaming;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class BungeeServerChatEndpoint implements IEndpoint {
    private final Logger logger;
    private final ServerInfo server;
    private final ProxyServer proxy;
    private final Listener chatListener;
    private final String id;
    private IRouter router = null;

    // We have to make this public, otherwise the BungeeCord event dispatcher
    // cannot access this and an exception will be thrown
    public class ChatListener implements Listener {
        @EventHandler
        public void onChat(ChatEvent event) {
            Objects.requireNonNull(event);
            var player = (ProxiedPlayer) event.getSender();
            if (!server.equals(player.getServer().getInfo())) return; // the message does not come from this endpoint
            var textMessage = event.getMessage();
            if (textMessage.startsWith("/")) return; // do not repeat commands
            var sender = ISender.create(player.getName(), player.getUniqueId());
            var message = IMessage.create(BungeeServerChatEndpoint.this, sender, textMessage);
            logger.info("Received chat message from game: " + message);
            onMessage(message);
        }
    }

    public BungeeServerChatEndpoint(@NotNull ServerInfo server,
                                    @NotNull ProxyServer proxy,
                                    @NotNull BungeeMainWrapper plugin) {
        this.logger =
                LoggerFactory.getLogger(LoggerNaming.name().of("endpoint").of("bungee").of(server.getName()).toString());
        this.server = Objects.requireNonNull(server);
        this.proxy = Objects.requireNonNull(proxy);
        this.id = Objects.requireNonNull(server.getName());
        this.chatListener = new ChatListener();
        proxy.getPluginManager().registerListener(plugin, chatListener);
    }

    @Override
    public void sendMessage(IMessage message) {
        proxy.getPlayers().forEach((player) -> {
            var info = player.getServer().getInfo();
            if (Objects.equals(info.getName(), server.getName()) &&
                    Objects.equals(info.getSocketAddress(), server.getSocketAddress())) {
                player.sendMessage(message.bungeeDisplay());
            }
        });
    }

    @Override
    public void setRouter(IRouter router) {
        this.router = router;
    }

    @Override
    public void close() {
        proxy.getPluginManager().unregisterListener(chatListener);
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
        // TODO make message routing async, probably utilizing a producer-consumer model
        var rt = router;
        if (rt == null) {
            throw new IllegalStateException("Current endpoint hasn't bound to any router");
        }
        rt.sendMessage(message);
    }

    @Override
    public String toString() {
        return "BungeeServerChatEndpoint{" +
                "id='" + id + '\'' +
                '}';
    }
}
