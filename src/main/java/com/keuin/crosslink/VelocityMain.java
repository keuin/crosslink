package com.keuin.crosslink;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id = "crosslink", name = "CrossLink", version = "1.0-SNAPSHOT",
        description = "Link your grouped servers with external world.", authors = {"Keuin"})
public class VelocityMain {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityMain(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("CrossLink is loading in Velocity mode.");
    }
}