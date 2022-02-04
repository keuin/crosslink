package com.keuin.psmb4j.error;

/**
 * Some parameters are illegal according to current version's protocol definition.
 */
public class IllegalParameterException extends ServerMisbehaveException {
    public IllegalParameterException(String message) {
        super(message);
    }
}
