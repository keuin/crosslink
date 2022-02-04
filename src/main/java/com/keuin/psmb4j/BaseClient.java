package com.keuin.psmb4j;

import com.keuin.psmb4j.error.IllegalParameterException;
import com.keuin.psmb4j.error.UnsupportedProtocolException;
import com.keuin.psmb4j.util.InputStreamUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class BaseClient implements AutoCloseable {

    protected final int protocolVersion = 1;
    protected final int MAX_CSTRING_LENGTH = 1024;
    protected final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 0;

    private final String host;
    private final int port;

    private Socket socket;
    protected DataInputStream is;
    protected DataOutputStream os;

    protected final Object socketWriteLock = new Object();
    protected final Object socketReadLock = new Object();

    public BaseClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connect to the server.
     * This method must be called before sending any other messages,
     * and should be called only once.
     * If an IO error occurred when doing some operation,
     * this client must be reconnected before next operations.
     * @throws IOException if a network error occurred
     */
    public void connect() throws IOException {
        try {
            if (this.socket != null) {
                throw new IllegalStateException("already connected");
            }
            this.socket = new Socket(host, port);
            this.socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
            this.is = new DataInputStream(this.socket.getInputStream());
            this.os = new DataOutputStream(this.socket.getOutputStream());

            os.writeBytes("PSMB");
            os.writeInt(protocolVersion);
            os.writeInt(0); // options
            os.flush();
            var response = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
            if (response.equals("UNSUPPORTED PROTOCOL")) {
                throw new UnsupportedProtocolException();
            } else if (response.equals("OK")) {
                var serverOptions = is.readInt();
                if (serverOptions != 0) {
                    throw new IllegalParameterException("Illegal server options: " + serverOptions);
                }
            }
        } catch (IOException ex) {
            // failed to connect, reset to initial state
            close();
            throw ex;
        }
    }

    public void keepAlive() throws IOException {
        final var nop = new byte[]{'N', 'O', 'P'};
        final var nil = new byte[]{'N', 'I', 'L'};
        synchronized (socketReadLock) {
            synchronized (socketWriteLock) {
                // lock the whole bidirectional communication
                os.write(nop);
                os.flush();
                // wait for a response NIL
                var response = InputStreamUtils.readBytes(is, 3);
                if (!Arrays.equals(response, nil)) {
                    throw new RuntimeException("illegal command from server: " +
                            new String(response, StandardCharsets.US_ASCII));
                }
            }
        }
    }

    public void disconnect() {
        if (os != null) {
            try {
                os.writeBytes("BYE");
                os.flush();
                os.close();
            } catch (IOException ignored) {
                os = null;
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException ignored) {
                is = null;
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                socket = null;
            }
        }
    }

    protected void setSocketTimeout(int t) throws SocketException {
        this.socket.setSoTimeout(t);
    }

    @Override
    public void close() {
        disconnect();
    }
}
