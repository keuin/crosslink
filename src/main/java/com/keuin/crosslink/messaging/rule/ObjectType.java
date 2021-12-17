package com.keuin.crosslink.messaging.rule;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum ObjectType {
    CHAT_MESSAGE("chat_message");
    private final String name;

    ObjectType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static @Nullable ObjectType of(String s) {
        Objects.requireNonNull(s);
        for (ObjectType v : ObjectType.values()) {
            if (v.name.equals(s)) return v;
        }
        return null;
    }
}
