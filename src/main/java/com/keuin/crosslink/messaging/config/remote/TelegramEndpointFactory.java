package com.keuin.crosslink.messaging.config.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.endpoint.remote.telegram.TelegramGroupEndpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TelegramEndpointFactory implements IRemoteEndpointFactory {
    @Override
    public @NotNull String type() {
        return "telegram";
    }

    @Override
    public @Nullable IEndpoint create(@NotNull JsonNode config) throws InvalidEndpointConfigurationException {
        // read id, not optional
        var id = Optional.ofNullable(config.get("id")).map(JsonNode::textValue).orElse(null);
        if (id == null || id.isEmpty()) throw new InvalidEndpointConfigurationException("Invalid \"id\"");

        // read enabled, optional (true by default)
        if (!config.get("enabled").isBoolean()) throw new InvalidEndpointConfigurationException("Invalid \"enabled\"");
        var enabled = Optional.ofNullable(config.get("enabled")).map(JsonNode::booleanValue).orElse(true);

        // read token, not optional
        var token = Optional.ofNullable(config.get("token")).map(JsonNode::textValue).orElse(null);
        if (token == null || token.isEmpty()) throw new InvalidEndpointConfigurationException("Invalid \"token\"");

        // read chat id
        var nodeChatId = config.get("chat_id");
        var chatId = Optional.ofNullable(nodeChatId).map(JsonNode::longValue).orElse(null);
        if (chatId == null) throw new InvalidEndpointConfigurationException("Invalid \"chat_id\"");
        if (!nodeChatId.isIntegralNumber() || nodeChatId.isBoolean()
                || nodeChatId.isFloatingPointNumber() || nodeChatId.isTextual()) {
            throw new InvalidEndpointConfigurationException("Invalid \"chat_id\"");
        }

        if (!enabled) return null; // not enabled, give a null value

        return new TelegramGroupEndpoint(token, id, chatId);
    }
}
