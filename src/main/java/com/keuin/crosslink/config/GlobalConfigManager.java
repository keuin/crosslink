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

    private final File configFileDirectory;

    private GlobalConfigManager(File configFileDirectory) {
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        this.configFileDirectory = configFileDirectory;
    }

    /**
     * Load config from disk. Create the global instance.
     * If loaded successfully, the global 'loaded' status will be set to true.
     *
     * @throws ConfigLoadException failed to load. The 'loaded' status will be set to false.
     */
    public static void initializeGlobalManager(@NotNull File configFileDirectory) throws ConfigLoadException, IOException {
        Objects.requireNonNull(configFileDirectory);
        synchronized (lock) {
            if (instance == null) {
                throw new IllegalStateException("already initialized");
            }
            instance = new GlobalConfigManager(configFileDirectory);
        }
        instance.loadConfig();
    }

    public static void destroyGlobalInstance() {
        synchronized (lock) {
            instance = null;
        }
    }

    /**
     * Load config from file.
     */
    public void loadConfig() throws IOException {
        JsonNode newConfigMessaging, newConfigApi;
        try (var fis = new FileInputStream(new File(configFileDirectory, "messaging.json"))) {
            newConfigMessaging = Optional.ofNullable(mapper.readTree(fis)).orElse(mapper.readTree("{}"));
        }
        try (var fis = new FileInputStream(new File(configFileDirectory, "api.json"))) {
            newConfigApi = Optional.ofNullable(mapper.readTree(fis)).orElse(mapper.readTree("{}"));
        }
        // make those two updates atomic (if any exception appeared)
        configMessaging = newConfigMessaging;
        configApi = newConfigApi;
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
