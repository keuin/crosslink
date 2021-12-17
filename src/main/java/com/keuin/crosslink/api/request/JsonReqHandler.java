package com.keuin.crosslink.api.request;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public abstract class JsonReqHandler implements HttpHandler {
    private final Function<JsonHttpExchange, Integer> precondition;

    public JsonReqHandler() {
        precondition = (exc) -> -1; // always true
    }

    protected JsonReqHandler(Function<JsonHttpExchange, Integer> precondition) {
        this.precondition = precondition;
    }

    protected JsonReqHandler(@NotNull String method) {
        Objects.requireNonNull(method);
        this.precondition = (exc) -> method.equals(exc.getRequestMethod()) ? -1 : 400;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (var exc = new JsonHttpExchange(exchange)) {
            var v = precondition.apply(exc);
            if (v > 0) {
                // precondition failed with a http response code
                // user defined handler is not called
                exc.setResponseCode(v);
                return;
            }
            handle(exc);
        }
    }

    protected void handle(JsonHttpExchange exchange) throws IOException {

    }
}
