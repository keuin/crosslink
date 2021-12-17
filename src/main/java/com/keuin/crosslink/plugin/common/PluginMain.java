package com.keuin.crosslink.plugin.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.keuin.crosslink.api.IApiServer;
import com.keuin.crosslink.api.error.ApiStartupException;
import com.keuin.crosslink.messaging.config.ConfigSyntaxError;
import com.keuin.crosslink.messaging.config.remote.InvalidEndpointConfigurationException;
import com.keuin.crosslink.messaging.config.remote.RemoteEndpointFactory;
import com.keuin.crosslink.messaging.config.router.RouterConfigurer;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.router.IRouterConfigurable;
import com.keuin.crosslink.plugin.common.environ.PluginEnvironment;
import com.keuin.crosslink.util.LoggerNaming;
import com.keuin.crosslink.util.StartupMessagePrinter;
import com.keuin.crosslink.util.version.NewVersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Optional;

public final class PluginMain {
    private final PluginEnvironment environment;
    private final IApiServer apiServer;
    private final ICoreAccessor coreAccessor;
    private final IRouter messageRouter;
    private final Logger logger;

    @Inject
    public PluginMain(ICoreAccessor coreAccessor,
                      IRouter messageRouter,
                      IApiServer apiServer,
                      PluginEnvironment pluginEnvironment) {
        this.coreAccessor = coreAccessor;
        this.messageRouter = messageRouter;
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

    public void enable() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // TODO refactor setup and teardown routine, split into hooks
        // load config
        // TODO global config (such as API config)
        logger.info("Loading message routing configuration.");
        JsonNode messagingConfig = null, routingConfig = null, remoteConfig = null;
        try (var fis = new FileInputStream(new File(environment.pluginDataPath().toFile(), "messaging.json"))) {
            messagingConfig = Optional.ofNullable(mapper.readTree(fis)).orElse(mapper.readTree("{}"));
            routingConfig = Optional.ofNullable(messagingConfig.get("routing")).orElse(mapper.readTree("[]"));
            remoteConfig = Optional.ofNullable(messagingConfig.get("remotes")).orElse(mapper.readTree("[]"));
        } catch (IOException ex) {
            logger.error("Failed to load message routing configuration", ex);
            throw new RuntimeException(ex);
        }

        // initialize message routing
        logger.info("Initializing message routing.");
        var endpoints = new HashSet<IEndpoint>();
        try {
            try {
                logger.debug("Loading rule chain.");
                var rc = new RouterConfigurer(routingConfig);
                rc.configure(messageRouter);
                logger.debug("Message router is configured successfully.");
            } catch (JsonProcessingException | ConfigSyntaxError ex) {
                throw new IRouterConfigurable.ConfigLoadException(ex);
            }
            //noinspection CollectionAddAllCanBeReplacedWithConstructor
            endpoints.addAll(coreAccessor.getServerEndpoints());
        } catch (IRouter.ConfigLoadException ex) {
            logger.error("Failed to read routing config", ex);
            throw new RuntimeException(ex);
        }

        try {
            logger.debug("Loading remote endpoints.");
            if (!remoteConfig.isArray()) {
                logger.error("Failed to load remote endpoints: remotes should be a JSON array.");
                throw new RuntimeException("Invalid remotes type");
            }
            for (JsonNode remote : remoteConfig) {
                var ep = RemoteEndpointFactory.create(remote);
                if (ep != null) {
                    logger.debug("Add remote endpoint: " + ep);
                    endpoints.add(ep);
                }
            }
        } catch (InvalidEndpointConfigurationException ex) {
            logger.error("Invalid remote endpoint", ex);
            throw new RuntimeException(ex);
        }

        for (IEndpoint ep : endpoints) {
            if (!messageRouter.addEndpoint(ep)) {
                logger.error("Cannot add endpoint " + ep);
                throw new RuntimeException("Cannot add endpoint " + ep);
            }
        }
        logger.info(String.format("Added %d sub-server(s) to message router.", endpoints.size()));

        logger.info("Starting API server.");
        try (var fis = new FileInputStream(new File(environment.pluginDataPath().toFile(), "api.json"))) {
            var apiConfig = Optional.ofNullable(mapper.readTree(fis)).orElse(mapper.readTree("{}"));
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
        } catch (IOException ex) {
            logger.error("Failed to load message routing configuration", ex);
            throw new RuntimeException(ex);
        } catch (ApiStartupException ex) {
            logger.error("Failed to start API server", ex);
            return;
        }
        logger.info("CrossLink is enabled.");
    }

    public void disable() {
        logger.info("Stopping API server.");
        apiServer.shutdown();
        logger.info("CrossLink is disabled.");
    }

    // may throw unchecked exception
    public void reload() {
        // TODO make api server and router reloadable
    }

    private String capital(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
