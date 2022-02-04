package com.keuin.psmb4j.error;

/**
 * Expected protocol error.
 */
public class ProtocolFailureException extends Exception {
    public ProtocolFailureException() {
    }

    public ProtocolFailureException(String message) {
        super(message);
    }
}
