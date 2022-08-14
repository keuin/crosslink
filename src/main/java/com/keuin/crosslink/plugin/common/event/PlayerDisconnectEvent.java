package com.keuin.crosslink.plugin.common.event;

import org.jetbrains.annotations.NotNull;

/**
 * When a player disconnects from the proxy server.
 */
public interface PlayerDisconnectEvent extends EventHandler {
    void onPlayerDisconnect(@NotNull String player, @NotNull String server);
}
