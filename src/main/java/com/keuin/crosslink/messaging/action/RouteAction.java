package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.action.result.IActionResult;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.util.LoggerNaming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class RouteAction implements IAction {
    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("actions").of("route").toString());
    private final Supplier<Set<IEndpoint>> destinations;
    private final boolean allowBackFlow;

    public RouteAction(Supplier<Set<IEndpoint>> destinations, boolean allowBackFlow) {
        this.allowBackFlow = allowBackFlow;
        this.destinations = destinations; // late evaluated destinations, ensuring the result to be up-to-date
    }

    @Override
    public @NotNull IActionResult process(@NotNull IMessage message) {
        // FIXME implement equals() and hashCode() for all IEndpoint subclasses
        var dest = destinations.get().stream();
        if (!allowBackFlow) {
            dest = dest.filter((ep) -> !ep.equals(message.source()));
        }
        dest.forEach((ep) -> {
            logger.debug("Route message " + message + " to endpoint " + ep + ", backflow=" + allowBackFlow);
            ep.sendMessage(message);
        });
        return IActionResult.normal(Objects.requireNonNull(message));
    }

    @Override
    public String toString() {
        return "RouteAction{" +
                "destinations=" + destinations +
                ", allowBackFlow=" + allowBackFlow +
                '}';
    }
}
