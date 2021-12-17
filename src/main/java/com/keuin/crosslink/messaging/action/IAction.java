package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.message.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An immutable, constant behaving optional mutator on {@link IMessage} instances.
 * Specific action will be taken on given message and the result will be returned.
 */
public interface IAction {
    @NotNull IActionResult process(@NotNull IMessage message);

    static @NotNull IAction compounded(@NotNull IAction... actions) {
        return new IAction() {
            @Override
            public @NotNull IActionResult process(@NotNull IMessage message) {
                Objects.requireNonNull(message);
                var msg = IActionResult.normal(message);
                for (IAction action : actions) {
                    if (msg.isDropped() || msg.isFiltered()) {
                        return msg;
                    }
                    // not dropped and not filtered
                    // move on
                    // not null guaranteed by post condition of method "getResult"
                    msg = action.process(Objects.requireNonNull(msg.getResult()));
                }
                return msg;
            }

            @Override
            public String toString() {
                return String.format("CompoundedAction{actions=%s}", (Object) actions);
            }
        };
    }
}
