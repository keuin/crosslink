package com.keuin.crosslink.plugin.common.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.api.ApiServer;
import com.keuin.crosslink.api.IApiServer;

public class CommonApiServerProvider extends AbstractModule {
    @Override
    protected void configure() {
        bind(IApiServer.class).to(ApiServer.class);
    }
}
