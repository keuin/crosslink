package com.keuin.crosslink.plugin.velocity;

import com.google.inject.Inject;
import com.keuin.crosslink.plugin.bungee.BungeeEventBus;
import com.keuin.crosslink.plugin.common.IEventBus;
import com.keuin.crosslink.plugin.common.event.EventHandler;
import com.keuin.crosslink.plugin.common.event.PlayerConnectEvent;
import com.keuin.crosslink.plugin.common.event.PlayerJoinServerEvent;
import com.keuin.crosslink.plugin.common.event.PlayerQuitServerEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class VelocityEventBus implements IEventBus {
    private final Object plugin;
    private final ProxyServer velocity;
    private final Logger logger = Logger.getLogger(BungeeEventBus.class.getName());
    private final Set<UUID> connectedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>()); // all players connected to the proxy
    private final Map<UUID, ServerInfo> serverPlayerLastJoined = new HashMap<>(); // the server players last connected to

    private final List<EventHandler> handlers = new ArrayList<>();

    @Inject
    public VelocityEventBus(Object plugin, ProxyServer velocity) {
        this.plugin = Objects.requireNonNull(plugin);
        this.velocity = Objects.requireNonNull(velocity);
    }

    @Override
    public void registerEventHandler(@NotNull com.keuin.crosslink.plugin.common.event.EventHandler handler) {
        Objects.requireNonNull(handler);
        handlers.add(handler);
    }

    @Subscribe
    public void onPlayerConnect(PostLoginEvent event) {
        var player = event.getPlayer();
        var playerId = player.getUsername();
        var playerUuid = player.getUniqueId();
        velocity.getScheduler().buildTask(plugin, () -> {
            for (var handler : handlers) {
                if (handler instanceof PlayerConnectEvent) {
                    ((PlayerConnectEvent) handler)
                            .onPlayerConnect(playerId, playerUuid);
                }
            }
        }).schedule();
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        try {
            var player = event.getPlayer();
            if (player != null) {
                var playerId = player.getUsername();
                var playerUuid = player.getUniqueId();
                var server = Optional
                        .ofNullable(serverPlayerLastJoined.get(player.getUniqueId()))
                        .map(ServerInfo::getName)
                        .orElse(null); // if this is null, the player does not connect to a server yet
                logger.info("Player " + playerId + " disconnected. Remove him from local online list.");
                connectedPlayers.remove(playerUuid);
                if (server != null) {
                    // also disconnect from a server
                    velocity.getScheduler().buildTask(plugin, () -> {
                        for (var handler : handlers) {
                            if (handler instanceof PlayerQuitServerEvent) {
                                ((PlayerQuitServerEvent) handler)
                                        .onPlayerQuitServer(playerId, server);
                            }
                        }
                    }).schedule();
                }
                // disconnect from the proxy
                velocity.getScheduler().buildTask(plugin, () -> {
                    for (var handler : handlers) {
                        if (handler instanceof com.keuin.crosslink.plugin.common.event.PlayerDisconnectEvent) {
                            ((com.keuin.crosslink.plugin.common.event.PlayerDisconnectEvent) handler)
                                    .onPlayerDisconnect(playerId, Optional.ofNullable(server).orElse("<no server>"));
                        }
                    }
                }).schedule();
            }
        } catch (Exception e) {
            logger.warning(String.format("An exception caught while handling player disconnect event: %s.", e));
        }
    }

    @Subscribe
    public void onPlayerJoinedFinish(ServerConnectedEvent event) {
        var player = event.getPlayer();
        var playerId = player.getUsername();
        var serverInfo = event.getServer().getServerInfo();
        var server = serverInfo.getName();
        serverPlayerLastJoined.put(player.getUniqueId(), serverInfo);
        velocity.getScheduler().buildTask(plugin, () -> {
            for (var handler : handlers) {
                if (handler instanceof PlayerJoinServerEvent) {
                    ((PlayerJoinServerEvent) handler)
                            .onPlayerJoinServer(playerId, server);
                }
            }
        }).schedule();
    }
}