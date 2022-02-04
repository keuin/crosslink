package com.keuin.psmb4j.util.error;

import java.io.IOException;

/**
 * The socket closed before enough bytes have been read out.
 */
public class SocketClosedException extends IOException {
    public SocketClosedException() {
    }

    public SocketClosedException(long expected, long actual) {
        super(String.format("expected %d bytes, EOF after reading %d bytes", expected, actual));
    }
}
