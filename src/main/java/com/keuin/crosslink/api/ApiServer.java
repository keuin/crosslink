package com.keuin.crosslink.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.keuin.crosslink.api.error.ApiStartupException;
import com.keuin.crosslink.api.request.JsonHttpExchange;
import com.keuin.crosslink.api.request.JsonReqHandler;
import com.keuin.crosslink.data.PlayerInfo;
import com.keuin.crosslink.data.ServerInfo;
import com.keuin.crosslink.plugin.common.ICoreAccessor;
import com.keuin.crosslink.util.LoggerNaming;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * CrossLink API server.
 * Serves API on a specific port. Fetch data from given accessor.
 */
public class ApiServer implements IApiServer {
    private final Logger logger = LoggerFactory.getLogger(LoggerNaming.name().of("api").of("server").toString());
    private volatile HttpServer server = null;
    private final ICoreAccessor coreAccessor;

    @Inject
    public ApiServer(@NotNull ICoreAccessor coreAccessor) {
        Objects.requireNonNull(coreAccessor);
        this.coreAccessor = coreAccessor;
    }

    @Override
    public void startup(InetSocketAddress listen) throws ApiStartupException {
        try {
            if (this.server != null) throw new IllegalStateException("API server is already started.");
            this.server = HttpServer.create(listen, 0);
            ImmutableMap.<String, JsonReqHandler>builder()
                    .put("/", new JsonReqHandler("GET") {
                        @Override
                        protected void handle(JsonHttpExchange exc) {
                            logger.debug("Root handler is called.");
                        }
                    })
                    .put("/online_players", new JsonReqHandler("GET") {
                        @Override
                        protected void handle(JsonHttpExchange exchange) {
                            // FIXME "true" or other non-zero values does not work
                            if (exchange.queryMap().getOrDefault("grouped", "0").equals("1")) {
                                // grouped by server name
                                var map = exchange.getResponseBody().putObject("players");
                                coreAccessor.getOnlinePlayers().stream()
                                        .collect(Collectors.groupingBy(PlayerInfo::serverName))
                                        .forEach((serverName, players) -> {
                                            var arr = map.putArray(serverName);
                                            players.stream().map(PlayerInfo::name).forEach(arr::add);
                                        });
                            } else {
                                var players = exchange.getResponseBody().putArray("players");
                                coreAccessor.getOnlinePlayers().stream().map(PlayerInfo::name).forEach(players::add);
                            }
                        }
                    })
                    .put("/server_status", new JsonReqHandler("GET") {
                        @Override
                        protected void handle(JsonHttpExchange exchange) throws IOException {
                            // FIXME make this http server async, prevent serialize non-blocking operation
                            var ev = new Object();
                            var isDone = new AtomicBoolean(false);
                            logger.debug("Start reading server info.");
                            coreAccessor.getServerInfo((infoList) -> {
                                logger.debug("Async reading status.");
                                var response = exchange.getResponseBody().putObject("servers");
                                for (ServerInfo info : infoList) {
                                    var obj = response.putObject(info.name());
                                    obj.put("status", info.status().getValue());
                                }
                                // unlock the main routine
                                logger.debug("Async notifying main routine.");
                                isDone.set(true);
                                synchronized (ev) {
                                    ev.notifyAll();
                                }
                            });
                            // block until the async routine returns
                            logger.debug("Wait for reading status.");
                            while (!isDone.get()) {
                                try {
                                    logger.debug("Spin waiting for pinging to finish.");
                                    synchronized (ev) {
                                        ev.wait(1000);
                                    }
                                } catch (InterruptedException ignored) {
                                }
                            }
                            logger.debug("Finished reading server status. Sending response to HTTP client.");
                        }
                    })
                    .build().forEach((p, h) -> server.createContext(p).setHandler(h));
            server.start();
            logger.info("API server is listening on {}:{}.",
                    listen.getAddress().toString().replaceFirst("/(.+)", "$1"),
                    listen.getPort());
        } catch (IOException ex) {
            throw new ApiStartupException(ex);
        }
    }

    @Override
    public void shutdown() {
        var s = server;
        if (s != null) {
            logger.info("Waiting for incoming connections to close.");
            s.stop(1);
            logger.info("API server is stopped.");
        }
    }

    // TODO http server, get data from coreAccessor
}
