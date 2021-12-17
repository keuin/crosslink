package com.keuin.crosslink.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GlobalConfigManager {
    /**
     * Load config from disk.
     * If loaded successfully, the global 'loaded' status will be set to true.
     * @throws ConfigLoadException failed to load. The 'loaded' status will be set to false.
     */
    public static void initializeGlobalManager(File configFile) throws ConfigLoadException {
        // TODO read config from disk, create the singleton object
//        throw new RuntimeException();
    }

    public static @NotNull GlobalConfigManager getInstance() {
        // TODO get the singleton object
//        throw new RuntimeException();
        throw new RuntimeException("GlobalConfigManager is not initialized");
    }

    /**
     * Get an immutable view of the global config.
     * A view is a consistent, but not up-to-date snapshot.
     *
     * @return the config view.
     */
    public IConfigView getConfig() {
        // TODO
        throw new RuntimeException("Global config is not loaded");
    }

    public boolean isLoaded() {
        throw new RuntimeException();
    }

    /**
     * Reload the config file from disk.
     * If loaded successfully, the global 'loaded' status will be set to true.
     * @throws ConfigLoadException failed to reload.
     * If previously loaded, the global config won't be modified.
     * Otherwise, the 'loaded' status will be set to false.
     */
    public void reload() throws ConfigLoadException {
        // TODO
//        throw new RuntimeException();
    }
}
