package com.keuin.crosslink.messaging.sender;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

class GamePlayerSender implements UniquelyIdentifiedSender {

    private final UUID uuid;
    private final String id;

    GamePlayerSender(@NotNull UUID uuid, @NotNull String id) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(id);
        this.uuid = uuid;
        this.id = id;
    }

    @Override
    public @NotNull String plainTextId() {
        return id;
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "GamePlayerSender{" +
                "uuid=" + uuid +
                ", id='" + id + '\'' +
                '}';
    }
}
