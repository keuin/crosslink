package com.keuin.crosslink.messaging.config.remote;

public class InvalidTypeOfEndpointException extends InvalidEndpointConfigurationException {
    public InvalidTypeOfEndpointException() {
        super();
    }

    public InvalidTypeOfEndpointException(String message) {
        super(message);
    }

    public InvalidTypeOfEndpointException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTypeOfEndpointException(Throwable cause) {
        super(cause);
    }
}
