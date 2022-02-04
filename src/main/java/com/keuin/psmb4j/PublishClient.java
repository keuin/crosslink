package com.keuin.psmb4j;

import com.keuin.psmb4j.error.CommandFailureException;
import com.keuin.psmb4j.error.ServerMisbehaveException;
import com.keuin.psmb4j.util.InputStreamUtils;
import com.keuin.psmb4j.util.StringUtils;

import java.io.IOException;

public class PublishClient extends BaseClient {

    /**
     * Create a client in PUBLISH mode.
     * @param host server host.
     * @param port server port.
     */
    public PublishClient(String host, int port) {
        super(host, port);
    }

    public void setPublish(String topicId) throws IOException, CommandFailureException {
        if (!StringUtils.isPureAscii(topicId)) {
            throw new IllegalArgumentException("topicId cannot be encoded with ASCII");
        }
        setSocketTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
        synchronized (socketWriteLock) {
            os.writeBytes("PUB");
            os.writeBytes(topicId);
            os.writeByte('\0');
            os.flush();
        }

        synchronized (socketReadLock) {
            var response = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
            if (response.equals("FAILED")) {
                var errorMessage = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
                throw new CommandFailureException("Publish failed: " + errorMessage);
            } else if (!response.equals("OK")) {
                throw new ServerMisbehaveException("Unexpected response: " + response);
            }
        }
    }

    /**
     * Publish a message.
     * Note that this method is not thread-safe.
     * @param message the message to publish.
     * @throws CommandFailureException If a command was rejected by the server.
     * @throws IOException if an IO error occurred.
     */
    public void publish(byte[] message) throws CommandFailureException, IOException {
        synchronized (this.socketWriteLock) {
            os.writeBytes("MSG");
            os.writeLong(message.length);
            os.write(message);
            os.flush();
        }
    }
}
