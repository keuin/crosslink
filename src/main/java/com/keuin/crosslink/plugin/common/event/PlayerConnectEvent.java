package com.keuin.crosslink.plugin.common.event;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * When a player connects to the proxy server.
 */
public interface PlayerConnectEvent extends EventHandler {
    void onPlayerConnect(@NotNull String player, @NotNull UUID uuid);
}
