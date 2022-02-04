package com.keuin.psmb4j.util;

import com.keuin.psmb4j.util.error.SocketClosedException;
import com.keuin.psmb4j.util.error.StringLengthExceededException;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {
    /**
     * Read until '\0', the trailing '\0' is dropped.
     */
    public static String readCString(InputStream stream) throws IOException {
        var sb = new StringBuilder();
        int c;
        while ((c = stream.read()) > 0) {
            sb.append((char) c);
        }
        return sb.toString();
    }

    /**
     * Read a C style string, with a length limit.
     * If the string is longer than given limit,
     * a {@link StringLengthExceededException} will be thrown.
     */
    public static String readCString(InputStream stream, long maxLength) throws IOException {
        var sb = new StringBuilder();
        int c;
        long length = 0;
        while ((c = stream.read()) > 0) {
            sb.append((char) c);
            if (++length > maxLength) {
                throw new StringLengthExceededException(maxLength);
            }
        }
        return sb.toString();
    }

    /**
     * Read fixed length bytes from stream.
     * If not enough, a {@link SocketClosedException} will be thrown.
     */
    public static byte[] readBytes(InputStream stream, int length) throws IOException {
        var buffer = new byte[length];
        int c;
        for (int i = 0; i < length; i++) {
            if ((c = stream.read()) >= 0) {
                buffer[i] = (byte) c;
            } else {
                throw new SocketClosedException(length, i);
            }
        }
        return buffer;
    }
}
