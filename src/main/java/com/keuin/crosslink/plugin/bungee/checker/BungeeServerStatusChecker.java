package com.keuin.crosslink.plugin.bungee.checker;


import com.keuin.crosslink.data.ServerStatus;
import com.keuin.crosslink.plugin.common.BaseServerChecker;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

public class BungeeServerStatusChecker extends BaseServerChecker<ServerInfo> {

    private final Plugin plugin;

    public BungeeServerStatusChecker(@NotNull Collection<ServerInfo> servers, @NotNull Plugin plugin, int timeoutMillis) {
        super(servers, timeoutMillis);
        this.plugin = plugin;
    }

    @Override
    protected void scheduleTask(Runnable task) {
        plugin.getProxy().getScheduler().runAsync(plugin, task);
    }

    @Override
    protected void pingServer(ServerInfo server, Consumer<ServerStatus> callback) {
        server.ping((result, error) -> {
            var isUp = result != null && error == null;
            var builder = new StringBuilder();
            builder.append(String.format("Server %s is %s.", server.getName(), isUp ? "up" : "down")).append(". ");
            if (result != null) {
                builder.append("MOTD: ").append(result.getDescriptionComponent().toPlainText()).append(" ");
            }
            if (error != null) {
                builder.append("Error: ").append(error.getClass().getName()).append(" ").append(error.getMessage());
            }
            logger.debug(builder.toString());
            callback.accept(isUp ? ServerStatus.ONLINE : ServerStatus.OFFLINE);
        });
    }
}