package com.keuin.crosslink.messaging.router;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.rule.IRule;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Contains methods that are essential to router configuring, while not used by general routing routines.
 */
public interface IRouterConfigurable {
    class ConfigLoadException extends Exception {
        public ConfigLoadException() {
        }

        public ConfigLoadException(String message) {
            super(message);
        }

        public ConfigLoadException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConfigLoadException(Throwable cause) {
            super(cause);
        }
    }

    boolean addEndpoint(@NotNull IEndpoint endpoint);

    /**
     * Get endpoints satisfying given conditions on namespace and id.
     *
     * @param namespace namespace of endpoints. Only endpoints with this namespace will be returned.
     * @param idPattern regexp pattern to match id. Only endpoints with id matching this pattern will be returned.
     * @return all matched endpoints.
     */
    @NotNull Set<IEndpoint> resolveEndpoints(@NotNull String namespace, @NotNull Pattern idPattern);

    void updateRuleChain(@NotNull List<IRule> newChain);

    /**
     * Remove all existing endpoints.
     */
    void clearEndpoints();
}
