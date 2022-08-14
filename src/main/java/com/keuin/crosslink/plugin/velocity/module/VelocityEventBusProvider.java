package com.keuin.crosslink.plugin.velocity.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.keuin.crosslink.plugin.common.IEventBus;
import com.keuin.crosslink.plugin.velocity.VelocityEventBus;

public class VelocityEventBusProvider extends AbstractModule {
    private final VelocityEventBus bus;

    public VelocityEventBusProvider(VelocityEventBus bus) {
        this.bus = bus;
    }

    @Provides
    public IEventBus provide() {
        return bus;
    }
}