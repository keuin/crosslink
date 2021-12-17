package com.keuin.crosslink.messaging.filter;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

public class ReIdFilter implements IFilter {
    public static class InvalidPatternStringException extends Exception {
        public InvalidPatternStringException(String message) {
            super(message);
        }

        public InvalidPatternStringException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final EndpointNamespace namespace;
    private final Pattern idPattern;

    public ReIdFilter(@NotNull EndpointNamespace namespace, @NotNull Pattern idPattern) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(idPattern);
        this.namespace = namespace;
        this.idPattern = idPattern;
    }

    public @NotNull String getIdPattern() {
        return idPattern.pattern();
    }

    @Override
    public boolean filter(@NotNull IEndpoint id) {
        if (!namespace.equals(id.namespace())) return false;
        return idPattern.matcher(id.id()).matches();
    }

    @Override
    public String toString() {
        return "ReIdFilter{" +
                "namespace=" + namespace +
                ", idPattern=" + idPattern +
                '}';
    }
}
