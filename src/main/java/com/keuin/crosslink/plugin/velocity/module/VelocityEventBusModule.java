package com.keuin.crosslink.plugin.velocity.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.plugin.common.IEventBus;
import com.keuin.crosslink.plugin.velocity.VelocityEventBus;

public class VelocityEventBusModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IEventBus.class).to(VelocityEventBus.class);
    }
}