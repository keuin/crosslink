package com.keuin.crosslink.messaging.config;

public class ConfigSyntaxError extends Exception {
    public ConfigSyntaxError() {
    }

    public ConfigSyntaxError(String message) {
        super(message);
    }

    public ConfigSyntaxError(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigSyntaxError(Throwable cause) {
        super(cause);
    }
}
