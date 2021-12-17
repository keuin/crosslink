package com.keuin.crosslink.plugin.common.environ;

import com.keuin.crosslink.plugin.common.ProxyType;
import org.slf4j.Logger;

import java.nio.file.Path;

public record PluginEnvironment(ProxyType proxyType, Logger logger, Path pluginDataPath) {
}