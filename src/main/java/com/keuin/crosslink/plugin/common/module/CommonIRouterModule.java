package com.keuin.crosslink.plugin.common.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.messaging.router.ConcreteRouter;
import com.keuin.crosslink.messaging.router.IRouter;

public class CommonIRouterModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IRouter.class).to(ConcreteRouter.class);
    }
}
