package com.keuin.crosslink.messaging.util;

import com.keuin.crosslink.messaging.filter.ReIdFilter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public class Messaging {
    public static @NotNull Component duplicate(@NotNull Component source) {
        // FIXME non-text-based message may lose information?
        Objects.requireNonNull(source);
        return Component.text().append(source).build();
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
}
