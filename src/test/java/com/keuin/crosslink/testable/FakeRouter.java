package com.keuin.crosslink.testable;

import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.rule.IRule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FakeRouter implements IRouter {
    private final List<IMessage> messages = new ArrayList<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final List<IEndpoint> endpoints = new ArrayList<>();
    private List<IRule> rules = Collections.emptyList();

    @Override
    public void sendMessage(IMessage message) {
        messages.add(message);
    }

    @Override
    public void close() throws Exception {
        isClosed.set(true);
    }

    @Override
    public boolean addEndpoint(@NotNull IEndpoint endpoint) {
        return endpoints.add(endpoint);
    }

    @Override
    public @NotNull Set<IEndpoint> resolveEndpoints(@NotNull String namespace, @NotNull Pattern idPattern) {
        return endpoints.stream()
                .filter((ep) -> ep.namespace().toString().equals(namespace) && idPattern.matcher(ep.id()).matches())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void updateRuleChain(@NotNull List<IRule> newChain) {
        this.rules = newChain;
    }

    public List<IMessage> getMessages() {
        return messages;
    }

    public AtomicBoolean getIsClosed() {
        return isClosed;
    }

    public List<IEndpoint> getEndpoints() {
        return endpoints;
    }

    public List<IRule> getRules() {
        return rules;
    }
}
