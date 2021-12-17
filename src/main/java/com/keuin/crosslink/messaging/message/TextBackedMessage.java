package com.keuin.crosslink.messaging.message;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.sender.ISender;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class TextBackedMessage implements IMessage {

    private final IEndpoint source;
    private final ISender sender;
    private final String content;

    TextBackedMessage(@NotNull IEndpoint source, @NotNull ISender sender, @NotNull String content) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(sender);
        Objects.requireNonNull(content);
        this.source = source;
        this.sender = sender;
        this.content = content;
    }

    @Override
    public @NotNull ISender sender() {
        return sender;
    }

    @Override
    public @NotNull IEndpoint source() {
        return source;
    }

    @Override
    public @NotNull String pureString() {
        return content;
    }

    @Override
    public Component kyoriMessage() {
        return Component.text(content);
    }

    @Override
    public String toString() {
        return "TextBackedMessage{" +
                "source=" + source +
                ", sender=" + sender +
                ", content='" + content + '\'' +
                '}';
    }
}
