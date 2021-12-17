package com.keuin.crosslink.messaging.sender;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface UniquelyIdentifiedSender extends ISender {
    @NotNull UUID uuid();
}
