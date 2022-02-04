package com.keuin.psmb4j.error;

/**
 * The server speaks a bad protocol which is not compatible to the client.
 */
public class BadProtocolException extends RuntimeException {
    public BadProtocolException() {
    }

    public BadProtocolException(String message) {
        super(message);
    }
}
