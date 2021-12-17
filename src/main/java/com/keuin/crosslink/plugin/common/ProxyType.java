package com.keuin.crosslink.plugin.common;

public enum ProxyType {
    BUNGEECORD("BungeeCord"), VELOCITY("Velocity");
    private final String name;

    ProxyType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
