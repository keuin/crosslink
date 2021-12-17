package com.keuin.crosslink.api.error;

public class ApiStartupException extends Exception {
    public ApiStartupException() {
    }

    public ApiStartupException(String message) {
        super(message);
    }

    public ApiStartupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiStartupException(Throwable cause) {
        super(cause);
    }
}
