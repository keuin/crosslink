package com.keuin.crosslink.messaging.filter;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.util.Messaging;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Select endpoints.
 */
public interface IFilter {
    boolean filter(@NotNull IEndpoint id);

    static @NotNull IFilter fromPatternString(@NotNull String pattern) throws ReIdFilter.InvalidPatternStringException {
        try {
            var parts = Messaging.splitIdSelector(pattern);
            if (parts == null) throw new ReIdFilter.InvalidPatternStringException("Invalid pattern");
            var ns = EndpointNamespace.of(parts[0]);
            if (ns == null) {
                throw new ReIdFilter.InvalidPatternStringException(String.format("Invalid namespace %s", parts[0]));
            }
            var p = Pattern.compile(parts[1]);
            return new ReIdFilter(ns, p);
        } catch (PatternSyntaxException ex) {
            throw new ReIdFilter.InvalidPatternStringException("Invalid pattern regular expression", ex);
        }
    }
}
