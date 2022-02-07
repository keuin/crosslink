package com.keuin.crosslink.messaging.router;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.rule.IRule;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConcreteRouter implements IRouter {
    private final Map<String, Map<String, IEndpoint>> endpoints = new HashMap<>(); // namespace / id / endpoint
    private final AtomicBoolean isOpened = new AtomicBoolean(true);
    private volatile List<IRule> ruleChain = Collections.emptyList();
    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("router").of("impl").toString());

    @Override
    public synchronized boolean addEndpoint(@NotNull IEndpoint endpoint) {
        var ns = endpoint.namespace().toString();
        if (!endpoints.containsKey(ns)) {
            endpoints.put(ns, new HashMap<>());
        }
        var map = endpoints.get(ns);
        if (map.containsKey(endpoint.id())) {
            logger.error("Endpoint {} is already added into router.", endpoint.namespacedId());
            return false; // already exists
        }
        endpoint.setRouter(this);
        logger.debug("Added endpoint \"" + endpoint.namespacedId() + "\".");
        map.put(endpoint.id(), endpoint);
        return true;
    }

    public void clearEndpoints() {
        endpoints.clear();
    }

    @Override
    public @NotNull Set<IEndpoint> resolveEndpoints(@NotNull String namespace, @NotNull Pattern idPattern) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(idPattern);
        return Optional.ofNullable(endpoints.get(namespace))
                .map(m -> m.entrySet().stream()
                        .filter((ent) -> idPattern.matcher(ent.getKey()).matches())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toUnmodifiableSet())
                ).orElse(Collections.emptySet());
    }

    @Override
    public void updateRuleChain(@NotNull List<IRule> newChain) {
        this.ruleChain = List.copyOf(newChain);
    }

    @Override
    public void sendMessage(IMessage message) {
        logger.debug("Routing message " + message);
        if (!isOpened.get()) {
            throw new IllegalStateException("Router is closed");
        }
        for (IRule rule : ruleChain) {
            logger.debug("Applying rule " + rule + " on message " + message);
            var result = rule.process(message);
            if (result.isDropped()) {
                // the message is dropped when processed by this rule
                // stop processing and do not pass to next rules
                break;
            }
        }
    }

    @Override
    public void close() throws Exception {
        isOpened.set(false);
    }
}
