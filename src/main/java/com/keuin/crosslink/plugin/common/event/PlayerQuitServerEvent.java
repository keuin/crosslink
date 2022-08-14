package com.keuin.crosslink.plugin.common.event;

import org.jetbrains.annotations.NotNull;

/**
 * When a player quits a sub-server.
 */
public interface PlayerQuitServerEvent extends EventHandler {
    void onPlayerQuitServer(@NotNull String player, @NotNull String server);
}
