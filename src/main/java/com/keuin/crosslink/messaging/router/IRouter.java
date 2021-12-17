package com.keuin.crosslink.messaging.router;

import com.keuin.crosslink.messaging.message.IMessage;

public interface IRouter extends AutoCloseable, IRouterConfigurable {
    /**
     * Put a message into router. Called by implementation of endpoints.
     * Router will scan the rule chain and pass the message to the rules one by one.
     * Rules can filter, manipulate, and route messages.
     * One message may have zero, one, or multiple final destinations.
     *
     * @param message the message to be routed.
     */
    void sendMessage(IMessage message);

    @Override
    void close() throws Exception;
}
