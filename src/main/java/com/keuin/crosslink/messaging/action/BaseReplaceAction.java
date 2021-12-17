package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class BaseReplaceAction implements IAction {

    private final UnaryOperator<IMessage> replacer;
    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("actions").of("replace").toString());

    /**
     * Create a replacement action based on given replacer.
     * Note that the replacer should always return a non-null value.
     */
    public BaseReplaceAction(UnaryOperator<IMessage> replacer) {
        this.replacer = replacer;
    }

    @Override
    public @NotNull IActionResult process(@NotNull IMessage message) {
        logger.debug("Replace message " + message);
        return IActionResult.normal(Objects.requireNonNull(replacer.apply(message)));
    }
}
