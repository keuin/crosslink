package com.keuin.crosslink.messaging.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public class Messaging {
    public static @NotNull Component duplicate(@NotNull Component source) {
        Objects.requireNonNull(source);
        var ser = LegacyComponentSerializer.legacySection().serialize(source);
        return LegacyComponentSerializer.legacySection().deserialize(ser);
    }

    public static @Nullable String[] splitIdSelector(@NotNull String pattern) {
        Objects.requireNonNull(pattern);
        if (Pattern.compile("\\s").matcher(pattern).find()) {
            return null;
        }
        var parts = pattern.split(":");
        if (parts.length != 2 || pattern.startsWith(":") || pattern.endsWith(":")) {
            // the heading or trailing ':' does not count, so here we got >= 3 ':' in the string
            return null;
        }
        return parts;
    }

    public static @NotNull BaseComponent[] kyoriComponentToBungee(@NotNull Component comp) {
        // convert between two incompatible Component objects using legacy string such as "&6Hello &b&lworld&c!"
        return TextComponent.fromLegacyText(
                LegacyComponentSerializer.legacySection().serialize(comp));
    }
}
