package com.keuin.crosslink.messaging.rule;

import com.keuin.crosslink.messaging.action.IAction;
import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.filter.IFilter;
import com.keuin.crosslink.messaging.message.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ImmutableRule implements IRule {
    private final ObjectType object;
    private final IFilter from;
    private final List<IAction> actions;

    public ImmutableRule(@NotNull ObjectType object, @NotNull IFilter from, @NotNull List<IAction> actions) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(from);
        Objects.requireNonNull(actions);
        this.object = object;
        this.from = from;
        this.actions = List.copyOf(actions);
    }

    @Override
    public IActionResult process(@NotNull IMessage message) {
        if (!from.filter(message.source())) return IActionResult.filtered(); // "form" does not match, pass through
        var result = IActionResult.normal(Objects.requireNonNull(message));
        for (IAction action : actions) {
            if (result.isFiltered() || result.isDropped()) break;
            result = action.process(Objects.requireNonNull(result.getResult()));
            Objects.requireNonNull(result);
        }
        return result;
    }

    @Override
    public @NotNull ObjectType object() {
        return object;
    }

    @Override
    public @NotNull IFilter from() {
        return from;
    }

    @Override
    public @NotNull List<IAction> actions() {
        return actions;
    }

    @Override
    public String toString() {
        return "ImmutableRule{" +
                "object=" + object +
                ", from=" + from +
                ", actions=" + actions +
                '}';
    }
}
