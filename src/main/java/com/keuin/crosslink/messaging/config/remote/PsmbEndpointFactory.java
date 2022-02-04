package com.keuin.crosslink.messaging.config.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.endpoint.remote.psmb.PsmbEndpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PsmbEndpointFactory implements IRemoteEndpointFactory {
    @Override
    public @NotNull String type() {
        return "psmb";
    }

    @Override
    public @Nullable IEndpoint create(@NotNull JsonNode config) throws InvalidEndpointConfigurationException {
        JsonNode node;
        // read id, not optional
        var id = Optional.ofNullable(config.get("id")).map(JsonNode::textValue).orElse(null);
        if (id == null || id.isEmpty()) throw new InvalidEndpointConfigurationException("Invalid \"id\"");

        // read enabled, optional (true by default)
        if (!config.get("enabled").isBoolean()) throw new InvalidEndpointConfigurationException("Invalid \"enabled\"");
        var enabled = Optional.ofNullable(config.get("enabled")).map(JsonNode::booleanValue).orElse(true);

        // read host, not optional
        var host = Optional.ofNullable(config.get("host")).map(JsonNode::textValue).orElse(null);
        if (host == null || host.isEmpty()) throw new InvalidEndpointConfigurationException("Invalid \"host\"");

        // read port, not optional
        node = config.get("port");
        if (node == null || !node.isIntegralNumber()) throw new InvalidEndpointConfigurationException("Invalid \"port\"");
        final int port = node.intValue();
        if (port <= 0 || port >= 65536) throw new InvalidEndpointConfigurationException("Invalid \"port\"");

        // read subscribe_from, not optional
        var pubTopic = Optional.ofNullable(config.get("publish_to")).map(JsonNode::textValue).orElse(null);
        if (pubTopic == null || pubTopic.isEmpty()) throw new InvalidEndpointConfigurationException("Invalid \"publish_to\"");

        // read subscribe_from, not optional
        var subPattern = Optional.ofNullable(config.get("subscribe_from")).map(JsonNode::textValue).orElse(null);
        if (subPattern == null || subPattern.isEmpty()) throw new InvalidEndpointConfigurationException("Invalid \"subscribe_from\"");

        // read subscriber_id, not optional
        node = config.get("subscriber_id");
        if (node == null || !node.isIntegralNumber()) {
            throw new InvalidEndpointConfigurationException("Invalid \"subscriber_id\"");
        }
        final var subId = node.longValue();

        // read keepalive, optional
        node = config.get("keepalive");
        if (node != null && !node.isIntegralNumber()) {
            throw new InvalidEndpointConfigurationException("Invalid \"keepalive\"");
        }
        final var keepalive = Optional.ofNullable(node).map(JsonNode::intValue).orElse(0);

        if (!enabled) return null; // not enabled, give a null value

        return new PsmbEndpoint(id, host, port, pubTopic, subPattern, subId, keepalive);
    }
}
