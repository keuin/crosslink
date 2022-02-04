package com.keuin.psmb4j.error;

/**
 * The previous command was rejected by the server for certain reasons.
 */
public class CommandFailureException extends ProtocolFailureException {
    public CommandFailureException(String message) {
        super(message);
    }
}
