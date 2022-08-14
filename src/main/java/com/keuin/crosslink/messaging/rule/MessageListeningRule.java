package com.keuin.crosslink.messaging.rule;

import com.keuin.crosslink.messaging.action.IAction;
import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.filter.IFilter;
import com.keuin.crosslink.messaging.message.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Listen and record all global messages.
 */
public class MessageListeningRule implements IRule {
    private final List<Consumer<IMessage>> handlers = new ArrayList<>();

    public void addHandler(Consumer<IMessage> handler) {
        handlers.add(handler);
    }

    @Override
    public IActionResult process(@NotNull IMessage message) {
        handlers.forEach(c -> c.accept(message));
        return IActionResult.normal(message);
    }

    @Override
    public @NotNull ObjectType object() {
        return ObjectType.CHAT_MESSAGE;
    }

    @Override
    public @NotNull IFilter from() {
        return IFilter.filterAlwaysTrue;
    }

    @Override
    public @NotNull List<IAction> actions() {
        // this rule records all passing messages, and do nothing about message forwarding
        return Collections.emptyList();
    }
}
