package com.keuin.crosslink.messaging.config.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * User should use this factory to create endpoints from a general JSON node.
 * This factory can create all supported configurable types of remote endpoints.
 */
public class RemoteEndpointFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("config").of("remotes").toString());
    private static final IRemoteEndpointFactory[] factories = new IRemoteEndpointFactory[]{
            new TelegramEndpointFactory(), new PsmbEndpointFactory()
    };

    /**
     * Create an {@link IEndpoint} instance based on given JSON config node.
     * If success, return the created instance.
     * If the given config node does not specify a valid type of endpoint,
     * an exception {@link InvalidTypeOfEndpointException} will be thrown.
     * If the config node specifies a valid type of endpoint but is invalid,
     * an exception {@link InvalidEndpointConfigurationException} will be thrown.
     * Note that if the endpoint is not configured to be enabled, null will be returned.
     *
     * @param config the config to create an endpoint from.
     * @return the created endpoint.
     */
    public static @Nullable IEndpoint create(@NotNull JsonNode config) throws InvalidEndpointConfigurationException {
        var type = Optional.ofNullable(config.get("type")).map(JsonNode::textValue).orElse(null);
        for (IRemoteEndpointFactory f : factories) {
            if (f.type().equals(type)) {
                var ep = f.create(config);
                if (ep != null) {
                    logger.debug("Create remote endpoint " + ep);
                }
                return ep;
            }
        }
        throw new InvalidTypeOfEndpointException("Unknown type: " + type);
    }
}
