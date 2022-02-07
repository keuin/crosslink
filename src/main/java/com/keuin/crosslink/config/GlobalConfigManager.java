package com.keuin.crosslink.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class GlobalConfigManager {

    private static final Object lock = new Object();
    public static final ObjectMapper mapper = new ObjectMapper();

    private volatile static GlobalConfigManager instance;
    private JsonNode configMessaging; // mutable root node of file "messaging.json"
    private JsonNode configApi; // mutable root node of file "api.json"

    private GlobalConfigManager() {
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    /**
     * Load config from disk. Create the global instance.
     * If loaded successfully, the global 'loaded' status will be set to true.
     *
     * @throws ConfigLoadException failed to load. The 'loaded' status will be set to false.
     */
    public static void initializeGlobalManager(@NotNull File configFile) throws ConfigLoadException, IOException {
        Objects.requireNonNull(configFile);
        synchronized (lock) {
            if (instance == null) {
                throw new IllegalStateException("already initialized");
            }
            instance = new GlobalConfigManager();
        }
        instance.loadConfig(configFile);
    }

    private void loadConfig(File configDirectory) throws IOException {
        try (var fis = new FileInputStream(new File(configDirectory, "messaging.json"))) {
            configMessaging = Optional.ofNullable(mapper.readTree(fis)).orElse(mapper.readTree("{}"));
        }
        try (var fis = new FileInputStream(new File(configDirectory, "api.json"))) {
            configApi = Optional.ofNullable(mapper.readTree(fis)).orElse(mapper.readTree("{}"));
        }
    }

    public static @NotNull GlobalConfigManager getInstance() {
        final var in = instance;
        if (in == null) {
            throw new RuntimeException("GlobalConfigManager is not initialized");
        }
        return in;
    }

    /**
     * Config tree for messaging module.
     */
    public @NotNull JsonNode messaging() {
        return configMessaging.deepCopy();
    }

    /**
     * Config tree for HTTP API module.
     */
    public @NotNull JsonNode api() {
        return configApi.deepCopy();
    }

    public boolean isLoaded() {
        throw new RuntimeException();
    }

    /**
     * Reload the config file from disk.
     * If loaded successfully, the global 'loaded' status will be set to true.
     *
     * @throws ConfigLoadException failed to reload.
     *                             If previously loaded, the global config won't be modified.
     *                             Otherwise, the 'loaded' status will be set to false.
     */
    public void reload() throws ConfigLoadException {
        // TODO
//        throw new RuntimeException();
    }
}
