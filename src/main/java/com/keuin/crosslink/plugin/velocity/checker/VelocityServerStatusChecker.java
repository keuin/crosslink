package com.keuin.crosslink.plugin.velocity.checker;

import com.keuin.crosslink.data.ServerStatus;
import com.keuin.crosslink.plugin.common.BaseServerChecker;
import com.keuin.crosslink.plugin.velocity.VelocityMainWrapper;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Ping some given velocity sub servers.
 * Based on the shared asynchronous task firing code from legacy BungeeCross codebase.
 */
public class VelocityServerStatusChecker extends BaseServerChecker<RegisteredServer> {

    private final VelocityMainWrapper plugin;

    public VelocityServerStatusChecker(@NotNull Collection<RegisteredServer> servers, @NotNull VelocityMainWrapper plugin, int timeoutMillis) {
        super(servers, timeoutMillis);
        this.plugin = plugin;
    }

    @Override
    protected void scheduleTask(Runnable task) {
        plugin.getProxy().getScheduler().buildTask(plugin, task).schedule();
    }

    @Override
    protected void pingServer(RegisteredServer server, Consumer<ServerStatus> callback) {
        server.ping().whenComplete((ping, ex) -> {
            var status = ServerStatus.ONLINE;
            if (ex != null) {
                logger.warn(String.format("An exception occurred while pinging server %s",
                        server.getServerInfo().getName()), ex);
                status = ServerStatus.OFFLINE;
            } else {
                // the implementation assures that ping is not null if no exception was thrown
                Objects.requireNonNull(ping);
            }
            callback.accept(status);
        });
    }
}
