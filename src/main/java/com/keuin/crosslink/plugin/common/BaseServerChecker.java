package com.keuin.crosslink.plugin.common;

import com.keuin.crosslink.data.ServerStatus;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Get online status of given sub servers.
 * Copied from legacy BungeeCross code and generalized.
 */
public abstract class BaseServerChecker<S> {

    protected static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("common").of("server_checker").toString());
    private final Set<S> servers;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<S, ServerStatus> pingResult = new ConcurrentHashMap<>();
    private final AtomicInteger pingCountdown;
    private final int timeoutMillis;

    protected BaseServerChecker(@NotNull Collection<S> servers,
                                int timeoutMillis) {
        this.servers = new HashSet<>(servers);
        this.timeoutMillis = timeoutMillis;
        this.pingCountdown = new AtomicInteger(this.servers.size());
        this.servers.forEach(s -> pingResult.put(s, ServerStatus.TIMED_OUT));
        for (S server : this.servers) {
            Objects.requireNonNull(server);
            // initialize with TIMED OUT status,
            // which is default when no response received in waiting time range
            pingResult.put(server, ServerStatus.TIMED_OUT);
        }
    }

    protected abstract void scheduleTask(Runnable task);

    // The implementation should fire an asynchronous routine to ping, then call the callback when finishes.
    // In one word, this routine not block.
    protected abstract void pingServer(S server, Consumer<ServerStatus> callback);

    /**
     * Perform ping asynchronously.
     *
     * @param callback the callback. Will be invoked asynchronously.
     */
    public final void ping(Consumer<Map<S, ServerStatus>> callback) {
        if (isRunning.getAndSet(true)) {
            // already running, do not start new threads
            throw new IllegalStateException("The ServerStatusChecker is already started");
        }
        logger.debug("Start pinging.");

        final Object finishEvent = new Object();
//        final Consumer<Runnable> scheduler = (Runnable consumer) ->
//                plugin.getProxy().getScheduler().runAsync(plugin, consumer);

        // async ping
        scheduleTask(() -> servers.forEach(server -> {
            logger.debug("Async ping server " + server);
            pingServer(server, (status) -> {
                logger.debug("Result: server " + server + ", status " + status);
                Objects.requireNonNull(status);
                this.pingResult.put(server, status);
                var remaining = pingCountdown.decrementAndGet();
                logger.debug("Not responded servers: {}.", remaining);
                if (remaining == 0) {
                    synchronized (finishEvent) {
                        finishEvent.notifyAll();
                    }
                }
            });
        }));

        // async wait and invoke callback
        scheduleTask(() -> {
            // wait until all server respond
            // or timed out
            var startTime = System.currentTimeMillis();
            int remaining;
            while ((remaining = pingCountdown.get()) != 0
                    && (System.currentTimeMillis() - startTime <= timeoutMillis)) {
                logger.debug("Waiting for finish. remaining={}.", remaining);
                try {
                    synchronized (finishEvent) {
                        finishEvent.wait(1000, 0);
                    }
                } catch (InterruptedException ignored) {
                }
            }

            // all server have responded, or at least one server has timed out
            // copy to ignore further result
            callback.accept(new HashMap<>(pingResult));
        });
    }

}
