package com.keuin.crosslink.plugin.velocity;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.keuin.crosslink.plugin.common.PluginMain;
import com.keuin.crosslink.plugin.common.ProxyType;
import com.keuin.crosslink.plugin.common.environ.PluginEnvironment;
import com.keuin.crosslink.plugin.common.module.CommonApiServerProvider;
import com.keuin.crosslink.plugin.common.module.CommonIRouterModule;
import com.keuin.crosslink.plugin.common.module.CommonPluginEnvironProvider;
import com.keuin.crosslink.plugin.velocity.module.VelocityAccessorModule;
import com.keuin.crosslink.plugin.velocity.module.VelocityApiServerModule;
import com.keuin.crosslink.util.LoggerNaming;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

// Velocity plugin main class
// Initializes the core accessor and manages its life cycle (such as disabling the accessor when server is down)
@Plugin(id = "crosslink", name = "CrossLink", version = "1.0-SNAPSHOT",
        description = "Link your grouped servers with external world.", authors = {"Keuin"})
public final class VelocityMainWrapper {
    private final ProxyServer proxy;
    private final PluginMain plugin;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // reload event
        proxy.getEventManager().register(
                this, ProxyReloadEvent.class, (ev) -> plugin.reload());
        // shutdown event
        proxy.getEventManager().register(
                this, ProxyShutdownEvent.class, (ev) -> plugin.disable());
        plugin.enable();
    }

    @Inject
    public VelocityMainWrapper(ProxyServer proxy, Logger logger, @DataDirectory Path pluginDataPath) {
        this.proxy = proxy;
        var injector = Guice.createInjector(
                new VelocityAccessorModule(this),
                new VelocityApiServerModule(),
                new CommonIRouterModule(),
                new CommonPluginEnvironProvider(new PluginEnvironment(
                        ProxyType.VELOCITY, LoggerFactory.getLogger(LoggerNaming.name().toString()), pluginDataPath)),
                new CommonApiServerProvider()
        );
        this.plugin = injector.getInstance(PluginMain.class);
    }

    public ProxyServer getProxy() {
        return proxy;
    }
}