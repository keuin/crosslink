package com.keuin.crosslink.plugin.bungee;

import com.google.inject.Inject;
import com.keuin.crosslink.plugin.common.IEventBus;
import com.keuin.crosslink.plugin.common.event.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BungeeEventBus implements Listener, IEventBus {
    private final Plugin plugin;
    private final Logger logger = Logger.getLogger(BungeeEventBus.class.getName());
    private final Set<UUID> connectedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>()); // all players connected to the proxy
    private final Map<UUID, ServerInfo> joiningServers = new HashMap<>();
    private final Map<UUID, ServerInfo> serverPlayerLastJoined = new HashMap<>(); // the server players last connected to

    private final List<EventHandler> handlers = new ArrayList<>();

    @Inject
    public BungeeEventBus(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public void registerEventHandler(@NotNull com.keuin.crosslink.plugin.common.event.EventHandler handler) {
        Objects.requireNonNull(handler);
        handlers.add(handler);
    }

    @net.md_5.bungee.event.EventHandler
    public void onServerDisconnect(ServerDisconnectEvent event) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            for (var handler : handlers) {
                if (handler instanceof PlayerQuitServerEvent) {
                    ((PlayerQuitServerEvent) handler).onPlayerQuitServer(
                            event.getPlayer().getName(), event.getTarget().getName());
                }
            }
        });
    }

    @net.md_5.bungee.event.EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            var player = event.getPlayer();
            if (player != null) {
                var playerId = player.getName();
                var playerUuid = player.getUniqueId();
                var server = Optional
                        .ofNullable(serverPlayerLastJoined.get(player.getUniqueId()))
                        .map(ServerInfo::getName)
                        .orElse("<unknown server>"); // this should not happen
                logger.info("Player " + playerId + " disconnected. Remove him from local online list.");
                connectedPlayers.remove(playerUuid);
                ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
                    for (var handler : handlers) {
                        if (handler instanceof com.keuin.crosslink.plugin.common.event.PlayerDisconnectEvent) {
                            ((com.keuin.crosslink.plugin.common.event.PlayerDisconnectEvent) handler)
                                    .onPlayerDisconnect(playerId, server);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.warning(String.format("An exception caught while handling player disconnect event: %s.", e));
        }
    }

    @net.md_5.bungee.event.EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player == null)
            return;
        ServerInfo server = event.getTarget();
        joiningServers.put(player.getUniqueId(), server);
    }

    @net.md_5.bungee.event.EventHandler
    public void onPlayerJoined(ServerConnectedEvent event) {

        // after a player has joined the server
        ProxiedPlayer player = event.getPlayer();
        if (player == null)
            return;
        ProxyServer proxy = plugin.getProxy();
        ServerInfo server = joiningServers.get(player.getUniqueId());

        if (!joiningServers.containsKey(event.getPlayer().getUniqueId())) {
            logger.warning(String.format(
                    "Unexpected player %s. Login broadcast will not be sent.",
                    event.getPlayer().getName()));
            return;
        }

        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            for (var handler : handlers) {
                if (handler instanceof PlayerJoinServerEvent) {
                    ((PlayerJoinServerEvent) handler)
                            .onPlayerJoinServer(player.getName(), server.getName());
                }
            }
        }, 250, TimeUnit.MILLISECONDS); // to make the message sequence correct: a stupid work-around

        joiningServers.remove(event.getPlayer().getUniqueId());
        serverPlayerLastJoined.put(event.getPlayer().getUniqueId(), server);

        if (connectedPlayers.add(player.getUniqueId())) {
            // The BungeeCord provides a very tedious event API, so we have to work around like this.
            // Otherwise, the client won't receive any message.
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                logger.info("Player " + player.getName() + " logged in." +
                        " Add him to local login list and send him recent messages.");
                for (var handler : handlers) {
                    if (handler instanceof PlayerConnectEvent) {
                        ((PlayerConnectEvent) handler)
                                .onPlayerConnect(player.getName(), player.getUniqueId());
                    }
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
    }

    public void onPlayerChat(ChatEvent event) {
        var conn = event.getSender();
        if (!(conn instanceof ProxiedPlayer)) {
            logger.severe(String.format("Sender is not a ProxiedPlayer instance: %s", event.getSender().toString()));
            return;
        }

        var sender = ((ProxiedPlayer) conn).getDisplayName();
        var server = ((ProxiedPlayer) conn).getServer().getInfo().getName();
        var message = event.getMessage();

        if (message.startsWith("/"))
            return; // Do not repeat commands

        logger.info(String.format("Chat message: %s, sender: %s", message, sender));

        for (var handler : handlers) {
            if (handler instanceof PlayerChatEvent) {
                ((PlayerChatEvent) handler).onPlayerChat(sender, server, message);
            }
        }
    }
}