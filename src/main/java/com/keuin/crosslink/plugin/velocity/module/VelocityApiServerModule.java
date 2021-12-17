package com.keuin.crosslink.plugin.velocity.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.plugin.common.ICoreAccessor;
import com.keuin.crosslink.plugin.velocity.VelocityAccessor;

public class VelocityApiServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ICoreAccessor.class).to(VelocityAccessor.class);
    }
}
