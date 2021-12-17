package com.keuin.crosslink.messaging.sender;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ISender {
    @NotNull String plainTextId();

    static ISender create(String id, UUID uuid) {
        return new GamePlayerSender(uuid, id);
    }
}
