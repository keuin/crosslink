package com.keuin.crosslink.data;

import com.velocitypowered.api.proxy.Player;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public record PlayerInfo(String name, UUID uuid, String serverName) {
    public static PlayerInfo fromBungeePlayer(ProxiedPlayer bungeePlayer) {
        return new PlayerInfo(
                bungeePlayer.getName(),
                bungeePlayer.getUniqueId(),
                bungeePlayer.getServer().getInfo().getName()
        );
    }

    public static PlayerInfo fromVelocityPlayer(Player player) {
        return new PlayerInfo(
                player.getUsername(),
                player.getUniqueId(),
                player.getCurrentServer().map((conn) -> conn.getServerInfo().getName()).orElse("null")
        );
    }
}
