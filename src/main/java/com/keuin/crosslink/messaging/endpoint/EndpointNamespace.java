package com.keuin.crosslink.messaging.endpoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum EndpointNamespace {
    SERVER("server"), REMOTE("remote"), SYSTEM("system");
    private final String namespace;

    EndpointNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return namespace;
    }

    public static @Nullable EndpointNamespace of(@NotNull String s) {
        Objects.requireNonNull(s);
        for (EndpointNamespace v : EndpointNamespace.values()) {
            if (v.toString().equals(s)) return v;
        }
        return null;
    }
}
