package com.keuin.crosslink.messaging.message;


import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.messaging.util.Messaging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Immutable.
 */
public interface IMessage {
    @NotNull ISender sender();

    @NotNull IEndpoint source();

    @NotNull String pureString();

    /**
     * This is a fallback (basic) implementation.
     * Color information will be discarded after converting.
     */
    default Component kyoriMessage() {
        return Component.text().content(pureString()).build();
    }

    static IMessage create(@NotNull IMessage message) {
        Objects.requireNonNull(message);
        return IMessage.create(message.source(), message.sender(), message.kyoriMessage());
    }

    /**
     * Create a text message with given pure text content.
     */
    static IMessage create(@NotNull IEndpoint source, @NotNull ISender sender, @NotNull String content) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(sender);
        Objects.requireNonNull(content);
        return new TextBackedMessage(source, sender, content);
    }

    static IMessage create(IEndpoint source, ISender sender, Component component) {
        Objects.requireNonNull(component);
        Objects.requireNonNull(sender);
        return new ComponentBackedMessage(source, sender, component);
    }

    /**
     * This is a fallback (basic) implementation.
     * Color information will be discarded after converting.
     */
    default BaseComponent[] bungeeMessage() {
        return new ComponentBuilder().append(pureString()).create();
    }

    default Component velocityMessage() {
        return kyoriMessage();
    }

    /**
     * Get the component with sender id and message content.
     * Suitable for displaying in BungeeCord sub-servers directly.
     */
    default BaseComponent[] bungeeDisplay() {
        return new ComponentBuilder()
                .append(new ComponentBuilder(String.format("<%s@%s>", sender().plainTextId(), source().friendlyName()))
                        .italic(true).create())
                .append(new ComponentBuilder(" ").italic(false).create())
                .append(bungeeMessage())
                .create();
    }

    /**
     * Get the component with sender id and message content.
     * Suitable for displaying in Velocity sub-servers directly.
     */
    default Component velocityDisplay() {
        var cfg = JoinConfiguration.builder().separator(Component.text(" ")).build();
        return Component.join(cfg,
                Component.text()
                        .content(String.format("<%s@%s>", sender().plainTextId(), source().friendlyName()))
                        .style(Style.style(TextDecoration.ITALIC)).build(), velocityMessage()
        );
    }

    /**
     * Get the plain text form of this message, containing sender and source information.
     * This can be used to display this message in a plain text environment, such as command line, or text file.
     * @return the plain text form of this message.
     */
    default String plainTextDisplay() {
        return String.format("<%s@%s> %s", sender().plainTextId(), source().friendlyName(), pureString());
    }
}
