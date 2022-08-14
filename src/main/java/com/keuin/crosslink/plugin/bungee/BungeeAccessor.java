package com.keuin.crosslink.plugin.bungee;

import com.google.inject.Inject;
import com.keuin.crosslink.config.GlobalConfigManager;
import com.keuin.crosslink.data.PlayerInfo;
import com.keuin.crosslink.data.ServerInfo;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.endpoint.local.BungeeServerChatEndpoint;
import com.keuin.crosslink.plugin.bungee.checker.BungeeServerStatusChecker;
import com.keuin.crosslink.plugin.common.ICoreAccessor;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BungeeAccessor implements ICoreAccessor {

    private final BungeeMainWrapper plugin;

    @Inject
    public BungeeAccessor(BungeeMainWrapper plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<PlayerInfo> getOnlinePlayers() {
        var players = plugin.getProxy().getPlayers();
        return players.stream().map(PlayerInfo::fromBungeePlayer).toList();
    }

    @Override
    public List<PlayerInfo> getOnlinePlayers(String serverName) {
        Objects.requireNonNull(serverName);
        return getOnlinePlayers().stream().filter((player) -> serverName.equals(player.serverName())).toList();
    }

    @Override
    public void getServerInfo(Consumer<List<ServerInfo>> callback) {
        var checker = new BungeeServerStatusChecker(
                plugin.getProxy().getServers().values(),
                plugin,
//                GlobalConfigManager.getInstance().getConfig().pingTimeoutMillis()
                2000
        );
        checker.ping((infoMap) -> callback.accept(infoMap.entrySet().stream().map(
                (ent) -> new ServerInfo(ent.getKey().getName(), ent.getValue())
        ).toList()));
    }

    @Override
    public Set<IEndpoint> getServerEndpoints() {
        return plugin.getProxy().getServers().values().stream()
                .map((si) -> new BungeeServerChatEndpoint(si, plugin.getProxy(), plugin))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void sendPlayerMessage(UUID playerUuid, Component message) {
        var player = plugin.getProxy().getPlayer(playerUuid);
        if (player == null) return;
        // FIXME keep color data
        var msg = new ComponentBuilder().append(message.toString()).create();
        player.sendMessage(msg);
    }
}
