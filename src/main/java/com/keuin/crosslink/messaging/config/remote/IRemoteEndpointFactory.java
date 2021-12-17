package com.keuin.crosslink.messaging.config.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Creates a specific type of remote endpoint, based on given configuration.
public interface IRemoteEndpointFactory {
    @NotNull String type();
    @Nullable IEndpoint create(@NotNull JsonNode config) throws InvalidEndpointConfigurationException;
}
