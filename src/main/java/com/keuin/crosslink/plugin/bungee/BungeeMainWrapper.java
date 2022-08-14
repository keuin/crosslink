package com.keuin.crosslink.plugin.bungee;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.keuin.crosslink.plugin.bungee.module.BungeeAccessorModule;
import com.keuin.crosslink.plugin.bungee.module.BungeeApiServerModule;
import com.keuin.crosslink.plugin.bungee.module.BungeeEventBusModule;
import com.keuin.crosslink.plugin.common.PluginMain;
import com.keuin.crosslink.plugin.common.ProxyType;
import com.keuin.crosslink.plugin.common.environ.PluginEnvironment;
import com.keuin.crosslink.plugin.common.module.CommonApiServerProvider;
import com.keuin.crosslink.plugin.common.module.CommonHistoricMessageRecorderModule;
import com.keuin.crosslink.plugin.common.module.CommonIRouterModule;
import com.keuin.crosslink.plugin.common.module.CommonPluginEnvironProvider;
import com.keuin.crosslink.util.LoggerNaming;
import net.md_5.bungee.api.plugin.Plugin;
import org.slf4j.LoggerFactory;

public final class BungeeMainWrapper extends Plugin {

    private final Injector injector = Guice.createInjector(
            new BungeeAccessorModule(this),
            new BungeeApiServerModule(),
            new BungeeEventBusModule(),
            new CommonHistoricMessageRecorderModule(),
            new CommonIRouterModule(),
            new CommonPluginEnvironProvider(new PluginEnvironment(
                    ProxyType.BUNGEECORD,
                    LoggerFactory.getLogger(LoggerNaming.name().toString()),
                    getDataFolder().toPath())),
            new CommonApiServerProvider()
    );
    private final PluginMain plugin = injector.getInstance(PluginMain.class);

    @Override
    public void onLoad() {
        // print startup message
        // do nothing here
    }

    @Override
    public void onEnable() {
        plugin.enable();
    }

    @Override
    public void onDisable() {
        plugin.disable();
    }
}