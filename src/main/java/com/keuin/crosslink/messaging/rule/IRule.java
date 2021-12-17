package com.keuin.crosslink.messaging.rule;

import com.keuin.crosslink.messaging.action.IAction;
import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.filter.IFilter;
import com.keuin.crosslink.messaging.message.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IRule {
    /**
     * Process a message and decide whether to send to the next rule.
     *
     * @param message the message.
     * @return true if send to next rule, false if discarded.
     */
    IActionResult process(@NotNull IMessage message);

    @NotNull ObjectType object();

    @NotNull IFilter from();

    @NotNull List<IAction> actions();

}
