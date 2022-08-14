package com.keuin.crosslink.plugin.bungee.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.keuin.crosslink.plugin.bungee.BungeeEventBus;
import com.keuin.crosslink.plugin.common.IEventBus;

public class BungeeEventBusProvider extends AbstractModule {
    private final BungeeEventBus bus;

    public BungeeEventBusProvider(BungeeEventBus bus) {
        this.bus = bus;
    }

    @Provides
    public IEventBus provide() {
        return bus;
    }
}