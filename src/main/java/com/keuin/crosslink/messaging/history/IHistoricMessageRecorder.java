package com.keuin.crosslink.messaging.history;

import com.keuin.crosslink.messaging.message.IMessage;

import java.util.List;

/**
 * Save and keep messages with a TTL.
 * The recorder behaves like an unlimited queue which features TTL on the messages.
 * Messages can be appended and traverse at any time.
 * Outdated messages will be removed from the internal storage.
 */
public interface IHistoricMessageRecorder {
    /**
     * Add and memorize a message.
     * @param message the message to save.
     */
    void addMessage(IMessage message);

    /**
     * Get recent messages.
     * @return an {@link List} containing all recent messages in the time scope.
     */
    List<IMessage> getMessages();

    /**
     * Get message TTL.
     * @return the message TTL milliseconds.
     */
    long getTTL();

    /**
     * Set message TTL. (a workaround to implement later initialization)
     * @param ttl the message TTL milliseconds.
     */
    void setTTL(long ttl);
}
