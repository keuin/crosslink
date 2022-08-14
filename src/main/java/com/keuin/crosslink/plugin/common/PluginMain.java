package com.keuin.crosslink.plugin.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.keuin.crosslink.api.IApiServer;
import com.keuin.crosslink.api.error.ApiStartupException;
import com.keuin.crosslink.config.ConfigLoadException;
import com.keuin.crosslink.config.GlobalConfigManager;
import com.keuin.crosslink.messaging.config.ConfigSyntaxError;
import com.keuin.crosslink.messaging.config.remote.InvalidEndpointConfigurationException;
import com.keuin.crosslink.messaging.config.remote.RemoteEndpointFactory;
import com.keuin.crosslink.messaging.config.router.RouterConfigurer;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.endpoint.system.ApiEndpoint;
import com.keuin.crosslink.messaging.history.IHistoricMessageRecorder;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.plugin.common.environ.PluginEnvironment;
import com.keuin.crosslink.plugin.common.event.PlayerConnectEvent;
import com.keuin.crosslink.util.LoggerNaming;
import com.keuin.crosslink.util.StartupMessagePrinter;
import com.keuin.crosslink.util.version.NewVersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Optional;

import static com.keuin.crosslink.config.GlobalConfigManager.mapper;

public final class PluginMain {
    private final PluginEnvironment environment;
    private final IApiServer apiServer;
    private final ICoreAccessor coreAccessor;
    private final IRouter messageRouter;
    private final IEventBus eventBus;
    private final IHistoricMessageRecorder historicMessageRecorder;
    private final Logger logger;

    @Inject
    public PluginMain(ICoreAccessor coreAccessor,
                      IRouter messageRouter,
                      IApiServer apiServer,
                      PluginEnvironment pluginEnvironment,
                      IEventBus eventBus,
                      IHistoricMessageRecorder historicMessageRecorder) {
        this.coreAccessor = coreAccessor;
        this.messageRouter = messageRouter;
        this.eventBus = eventBus;
        this.historicMessageRecorder = historicMessageRecorder;
        this.apiServer = apiServer;
        this.environment = pluginEnvironment;
        this.logger = environment.logger();
        // print startup message
        StartupMessagePrinter.print(logger::info, pluginEnvironment.proxyType().getName());
        logger.debug("Debug logging is enabled. You may see logs more than usual.");
        NewVersionChecker.checkNewVersionAsync(
                LoggerFactory.getLogger(LoggerNaming.name().of("update").toString())::info, true);
        // the plugin is not constructed here, so don't register event listener here
    }

    private void initialize() {
        logger.info("Initializing components.");
        // initialize message routing
        logger.info("Initializing message routing.");
        // Contains all enabled endpoints, including local and remote ones. Remote endpoints will be added later.
        final var endpoints = new HashSet<IEndpoint>();
        try {
            var messaging = GlobalConfigManager.getInstance().messaging();
            var local = Optional.ofNullable(messaging.get("local"))
                    .orElse(mapper.readTree("{}"));
            var routing = Optional.ofNullable(messaging.get("routing"))
                    .orElse(mapper.readTree("[]"));
            var remote = Optional.ofNullable(messaging.get("remotes"))
                    .orElse(mapper.readTree("[]"));

            // load local server endpoints
            final var locals = coreAccessor.getServerEndpoints();
            final var ttlSeconds = Optional.ofNullable(local.get("message_playback_seconds"))
                    .map(JsonNode::asLong).orElse(-1L);
            if (ttlSeconds > 0) {
                // enable message replay
                logger.info("Message replay is enabled. TTL: ");
                eventBus.registerEventHandler(
                        (PlayerConnectEvent) (player, uuid) -> historicMessageRecorder.getMessages()
                                .forEach(msg -> coreAccessor.sendPlayerMessage(uuid, msg.kyoriMessage()))
                );
            }
            endpoints.addAll(locals);

            // load routing table
            try {
                logger.debug("Loading rule chain.");
                var rc = new RouterConfigurer(routing);
                rc.configure(messageRouter); // update routing table, clear endpoints
                logger.debug("Finish configuring message router.");
            } catch (JsonProcessingException | ConfigSyntaxError ex) {
                logger.error("Failed to load routing config.", ex);
                throw new RuntimeException(ex);
            }

            // load remote endpoints
            try {
                logger.debug("Loading remote endpoints.");
                if (!remote.isArray()) {
                    logger.error("Failed to load remote endpoints: remotes should be a JSON array.");
                    throw new RuntimeException("Invalid remotes type");
                }
                for (var r : remote) {
                    var ep = RemoteEndpointFactory.create(r);
                    if (ep != null) {
                        logger.debug("Add remote endpoint: " + ep);
                        endpoints.add(ep);
                    }
                }
            } catch (InvalidEndpointConfigurationException ex) {
                logger.error("Invalid remote endpoint", ex);
                throw new RuntimeException(ex);
            }

        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse JSON config", ex);
            throw new RuntimeException(ex);
        }

        // API endpoint (send messages from HTTP api)
        ApiEndpoint.INSTANCE.setRouter(messageRouter);
        messageRouter.addEndpoint(ApiEndpoint.INSTANCE);

        // register all endpoints on the router
        for (IEndpoint ep : endpoints) {
            if (!messageRouter.addEndpoint(ep)) {
                logger.error("Cannot add endpoint " + ep);
                throw new RuntimeException("Cannot add endpoint " + ep);
            }
        }
        logger.info(String.format("Added %d sub-server(s) to message router.", endpoints.size()));

        logger.info("Starting API server.");
        try {
            var apiConfig = GlobalConfigManager.getInstance().api();
            var host = Optional.ofNullable(apiConfig.get("host")).map(JsonNode::textValue).orElse(null);
            if (host == null
                    || host.isEmpty()
                    || (Character.digit(host.charAt(0), 16) == -1 && (host.charAt(0) != ':'))) {
                throw new ApiStartupException("Invalid inet host to listen on");
            }
            int port = Optional.ofNullable(apiConfig.get("port")).map(JsonNode::intValue).orElse(-1);
            if (port <= 0) throw new ApiStartupException("Invalid port to listen on");
            // now the host is guaranteed to be an IP address string, so no DNS lookup will be performed
            apiServer.startup(new InetSocketAddress(InetAddress.getByName(host), port));
        } catch (IOException | ApiStartupException ex) {
            logger.error("Failed to start API server", ex);
            throw new RuntimeException(ex);
        }
    }

    public void enable() {
        // TODO refactor setup and teardown routine, split into hooks
        logger.info("Loading config from disk.");
        try {
            GlobalConfigManager.initializeGlobalManager(environment.pluginDataPath().toFile());
        } catch (ConfigLoadException | IOException ex) {
            logger.error("Failed to load configuration.", ex);
            throw new RuntimeException(ex);
        }
        logger.info("Config files are loaded.");
        initialize();
        logger.info("CrossLink is enabled.");
    }

    public void disable() {
        logger.info("Stopping API server.");
        apiServer.shutdown();
        logger.info("CrossLink is disabled.");
        GlobalConfigManager.destroyGlobalInstance();
    }

    // may throw unchecked exception
    public void reload() {
        disable();
        enable();
    }

    private String capital(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
