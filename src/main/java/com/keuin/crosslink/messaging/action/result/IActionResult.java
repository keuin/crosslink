package com.keuin.crosslink.messaging.action.result;

import com.keuin.crosslink.messaging.message.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Process result of an action. The intermediate state in the middle of a chain.
 */
public interface IActionResult {
    boolean isDropped();

    /**
     * If the message is filtered out by a filter action or by the rule itself (such as the "from" filter in rule)
     *
     * @return if the message is filtered out.
     */
    boolean isFiltered();

    /**
     * Null if and only if isDropped or isFiltered returns true.
     */
    @Nullable IMessage getResult(); // TODO is it better to make this always not null?

    /**
     * Returns if the message has not been dropped or filtered out.
     */
    default boolean isValid() {
        var valid = !isDropped() && !isFiltered();
        if (valid) {
            Objects.requireNonNull(getResult());
        }
        return valid;
    }

    static IActionResult dropped() {
        return new IActionResult() {
            @Override
            public boolean isDropped() {
                return true;
            }

            @Override
            public boolean isFiltered() {
                return false;
            }

            @Override
            public IMessage getResult() {
                return null;
            }
        };
    }

    static IActionResult filtered() {
        return new IActionResult() {
            @Override
            public boolean isDropped() {
                return false;
            }

            @Override
            public boolean isFiltered() {
                return true;
            }

            @Override
            public IMessage getResult() {
                return null;
            }
        };
    }

    static IActionResult normal(@NotNull IMessage message) {
        Objects.requireNonNull(message);
        return new IActionResult() {
            @Override
            public boolean isDropped() {
                return false;
            }

            @Override
            public boolean isFiltered() {
                return false;
            }

            @Override
            public IMessage getResult() {
                return message;
            }
        };
    }
}
