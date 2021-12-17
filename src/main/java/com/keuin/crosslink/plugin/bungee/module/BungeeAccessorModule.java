package com.keuin.crosslink.plugin.bungee.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.keuin.crosslink.plugin.bungee.BungeeMainWrapper;

public class BungeeAccessorModule extends AbstractModule {
    private final BungeeMainWrapper plugin;

    public BungeeAccessorModule(BungeeMainWrapper plugin) {
        this.plugin = plugin;
    }

    @Provides
    BungeeMainWrapper getPlugin() {
        return this.plugin;
    }
}
