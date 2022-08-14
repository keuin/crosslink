package com.keuin.crosslink.messaging.message;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.messaging.util.Messaging;
import com.keuin.crosslink.util.LazyEvaluated;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ComponentBackedMessage implements IMessage {
    private final IEndpoint source;
    private final ISender sender;
    private final Component component;
    private final LazyEvaluated<String> lazyString;

    public ComponentBackedMessage(@NotNull IEndpoint source, @NotNull ISender sender, @NotNull Component component) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(sender);
        Objects.requireNonNull(component);
        this.source = source;
        this.sender = sender;
        this.component = Messaging.duplicate(component);
        this.lazyString = new LazyEvaluated<>(() -> PlainTextComponentSerializer.plainText().serialize(component));
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
        return lazyString.get();
    }

    @Override
    public Component kyoriMessage() {
        return Messaging.duplicate(component);
    }

    @Override
    public BaseComponent[] bungeeMessage() {
        return Messaging.kyoriComponentToBungee(component);
    }
}
