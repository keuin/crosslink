package com.keuin.crosslink.plugin.common.event;

import org.jetbrains.annotations.NotNull;

/**
 * When a player joins a sub-server.
 */
public interface PlayerJoinServerEvent extends EventHandler {
    void onPlayerJoinServer(@NotNull String player, @NotNull String server);
}
