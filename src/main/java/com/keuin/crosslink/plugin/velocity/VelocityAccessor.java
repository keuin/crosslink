package com.keuin.crosslink.plugin.velocity;

import com.google.inject.Inject;
import com.keuin.crosslink.data.PlayerInfo;
import com.keuin.crosslink.data.ServerInfo;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.endpoint.local.VelocityServerChatEndpoint;
import com.keuin.crosslink.plugin.common.ICoreAccessor;
import com.keuin.crosslink.plugin.velocity.checker.VelocityServerStatusChecker;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VelocityAccessor implements ICoreAccessor {
    private final VelocityMainWrapper plugin;

    @Inject
    public VelocityAccessor(VelocityMainWrapper plugin) {
        this.plugin = plugin;
        // TODO handle shutdown event
    }

    @Override
    public List<PlayerInfo> getOnlinePlayers() {
        return plugin.getProxy().getAllPlayers().stream().map(PlayerInfo::fromVelocityPlayer).toList();
    }

    @Override
    public List<PlayerInfo> getOnlinePlayers(String serverName) {
        return getOnlinePlayers().stream().filter((player) -> serverName.equals(player.serverName())).toList();
    }

    @Override
    public void getServerInfo(Consumer<List<ServerInfo>> callback) {
        var checker = new VelocityServerStatusChecker(
                plugin.getProxy().getAllServers(),
                plugin,
                //GlobalConfigManager.getInstance().getConfig().pingTimeoutMillis()
                2000
        );
        checker.ping((infoMap) -> callback.accept(infoMap.entrySet().stream().map(
                (ent) -> new ServerInfo(ent.getKey().getServerInfo().getName(), ent.getValue())
        ).toList()));
    }

    @Override
    public Set<IEndpoint> getServerEndpoints() {
        return plugin.getProxy().getAllServers().stream()
                .map((si) -> new VelocityServerChatEndpoint(si, plugin.getProxy(), plugin))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void sendPlayerMessage(UUID playerUuid, Component message) {
        var player = plugin.getProxy().getPlayer(playerUuid).orElse(null);
        if (player == null) return;
        player.sendMessage(message);
    }
}
