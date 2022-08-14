package com.keuin.crosslink.plugin.common;

import com.keuin.crosslink.data.PlayerInfo;
import com.keuin.crosslink.data.ServerInfo;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface ICoreAccessor {
    List<PlayerInfo> getOnlinePlayers();

    List<PlayerInfo> getOnlinePlayers(String serverName);

    void getServerInfo(Consumer<List<ServerInfo>> callback);

    Set<IEndpoint> getServerEndpoints();

    void sendPlayerMessage(UUID playerUuid, Component message);
}
