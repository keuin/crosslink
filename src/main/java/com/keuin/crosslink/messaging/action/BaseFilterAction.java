package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

public class BaseFilterAction implements IAction {
    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("actions").of("filter").toString());
    private final Predicate<IMessage> filter;

    public BaseFilterAction(Predicate<IMessage> filter) {
        this.filter = filter;
    }

    @Override
    public @NotNull IActionResult process(@NotNull IMessage message) {
        Objects.requireNonNull(message);
        if (filter.test(message)) {
            logger.debug("Message " + message + " passed filter.");
            return IActionResult.normal(message);
        } else {
            logger.debug("Message " + message + " is filtered out.");
            return IActionResult.filtered();
        }
    }
}
