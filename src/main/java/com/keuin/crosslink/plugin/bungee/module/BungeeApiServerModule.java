package com.keuin.crosslink.plugin.bungee.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.plugin.bungee.BungeeAccessor;
import com.keuin.crosslink.plugin.common.ICoreAccessor;

public class BungeeApiServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ICoreAccessor.class).to(BungeeAccessor.class);
    }
}
