package com.keuin.crosslink.data;

/**
 * Server running status.
 * Copied from legacy BungeeCross code.
 */
public enum ServerStatus {
    ONLINE("UP"), OFFLINE("DOWN"), TIMED_OUT("TIMED OUT");
    private final String value;

    ServerStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}