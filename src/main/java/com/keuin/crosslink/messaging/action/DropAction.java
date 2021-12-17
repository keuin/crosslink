package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropAction implements IAction {
    private final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("actions").of("drop").toString());

    @Override
    public @NotNull IActionResult process(@NotNull IMessage message) {
        logger.debug("Drop message " + message);
        return IActionResult.dropped();
    }

    @Override
    public String toString() {
        return "DropAction{}";
    }
}
