package com.keuin.psmb4j.error;

/**
 * The server made an illegal response, which conflicts to the protocol definition.
 */
public class ServerMisbehaveException extends BadProtocolException {
    public ServerMisbehaveException(String message) {
        super(message);
    }
}
