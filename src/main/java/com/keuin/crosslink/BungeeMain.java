package com.keuin.crosslink;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public class BungeeMain extends Plugin {

    private final Logger logger = getLogger();

    @Override
    public void onLoad() {
        logger.info("CrossLink is loading in BungeeCord mode.");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

}