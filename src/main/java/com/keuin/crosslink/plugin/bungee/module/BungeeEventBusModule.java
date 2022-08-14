package com.keuin.crosslink.plugin.bungee.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.plugin.bungee.BungeeEventBus;
import com.keuin.crosslink.plugin.common.IEventBus;

public class BungeeEventBusModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IEventBus.class).to(BungeeEventBus.class);
    }
}