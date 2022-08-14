package com.keuin.crosslink.plugin.common;

import com.keuin.crosslink.plugin.common.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Provides framework-independent event registration for CrossLink's upper-level logic.
 */
public interface IEventBus {
    void registerEventHandler(@NotNull EventHandler handler);
}
