package com.keuin.crosslink.messaging.config.remote;

/**
 * The given JSON config node is invalid to this factory.
 */
public class InvalidEndpointConfigurationException extends Exception {
    public InvalidEndpointConfigurationException() {
        super();
    }

    public InvalidEndpointConfigurationException(String message) {
        super(message);
    }

    public InvalidEndpointConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEndpointConfigurationException(Throwable cause) {
        super(cause);
    }
}
