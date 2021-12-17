package com.keuin.crosslink.plugin.velocity.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.keuin.crosslink.plugin.velocity.VelocityMainWrapper;

public class VelocityAccessorModule extends AbstractModule {
    private final VelocityMainWrapper plugin;

    public VelocityAccessorModule(VelocityMainWrapper plugin) {
        this.plugin = plugin;
    }

    @Provides
    VelocityMainWrapper getPlugin() {
        return this.plugin;
    }
}
