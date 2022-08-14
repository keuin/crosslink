package com.keuin.crosslink.messaging.history;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.util.LoggerNaming;
import com.keuin.crosslink.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class HistoricMessageRecorder implements IHistoricMessageRecorder {
    private long ttlMillis;
    private final LinkedList<Pair<Long, IMessage>> que = new LinkedList<>();
    private final Object lock = new Object();

    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name()
                    .of("history").of("message_recorder").toString());

    public HistoricMessageRecorder(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public HistoricMessageRecorder() {
        this.ttlMillis = 0;
    }

    private void clean() {
        for (var head = que.peek();
             head != null &&
                     Math.abs(System.currentTimeMillis() - head.getK()) > ttlMillis;
             head = que.peek()
        ) {
            // head has expired, remove it
            logger.debug("Remove expired history message " + head);
            que.removeFirst();
        }
    }

    /**
     * Add and memorize a message.
     * Note: this implementation is synchronized. Be caution if you requires a high performance.
     *
     * @param message the message to save.
     */
    @Override
    public void addMessage(IMessage message) {
        Objects.requireNonNull(message);
        logger.debug("Add message " + message);
        synchronized (lock) {
            que.add(new Pair<>(System.currentTimeMillis(), message));
            clean();
        }
    }

    @Override
    public List<Pair<Long, IMessage>> getMessages() {
        synchronized (lock) {
            clean();
            var list = new ArrayList<>(que);
            logger.debug("History messages: " + list);
            return list;
        }
    }

    @Override
    public long getTTL() {
        return ttlMillis;
    }

    @Override
    public void setTTL(long ttl) {
        logger.debug("TTL is set to " + ttl + "ms");
        this.ttlMillis = ttl;
    }
}
