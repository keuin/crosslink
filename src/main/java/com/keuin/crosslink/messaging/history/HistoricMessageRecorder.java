package com.keuin.crosslink.messaging.history;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HistoricMessageRecorder implements IHistoricMessageRecorder {
    private final long ttlMillis;
    private final LinkedList<Pair<Long, IMessage>> que = new LinkedList<>();
    private final Object lock = new Object();

    public HistoricMessageRecorder(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    private void clean() {
        for (var head = que.peek();
             head != null &&
                     Math.abs(System.currentTimeMillis() - head.getK()) > ttlMillis;
             head = que.peek()
        ) {
            // head has expired, remove it
            que.removeFirst();
        }
    }
    /**
     * Add and memorize a message.
     * Note: this implementation is synchronized. Be caution if you requires a high performance.
     * @param message the message to save.
     */
    @Override
    public void addMessage(IMessage message) {
        Objects.requireNonNull(message);
        synchronized (lock) {
            que.add(new Pair<>(System.currentTimeMillis(), message));
            clean();
        }
    }

    @Override
    public List<IMessage> getMessages() {
        synchronized (lock) {
            clean();
            return que.stream().map(Pair::getV).collect(Collectors.toList());
        }
    }
}
