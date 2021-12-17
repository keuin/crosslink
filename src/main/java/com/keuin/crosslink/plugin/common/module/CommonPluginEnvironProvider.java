package com.keuin.crosslink.plugin.common.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.keuin.crosslink.plugin.common.environ.PluginEnvironment;

public class CommonPluginEnvironProvider extends AbstractModule {
    private final PluginEnvironment pluginEnvironment;

    public CommonPluginEnvironProvider(PluginEnvironment pluginEnvironment) {
        this.pluginEnvironment = pluginEnvironment;
    }

    @Provides
    PluginEnvironment getPluginEnvironment() {
        return this.pluginEnvironment;
    }
}
