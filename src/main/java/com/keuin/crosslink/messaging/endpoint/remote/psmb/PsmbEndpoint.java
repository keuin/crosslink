package com.keuin.crosslink.messaging.endpoint.remote.psmb;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.util.LoggerNaming;
import com.keuin.psmb4j.PublishClient;
import com.keuin.psmb4j.SubscribeClient;
import com.keuin.psmb4j.error.CommandFailureException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class PsmbEndpoint implements IEndpoint {
    public static final int RETRY_INTERVAL_MILLIS = 10 * 1000;
    private IRouter router = null;
    private final String id;

    private final String host;
    private final int port;
    private final String pubTopic;
    private final String subPattern;
    private final long subId; // subscriber id
    private final int keepAliveInterval;

    /*
        PublishClient and SubscribeClient are networking components.
        When IO error occurred, they will be invalidated,
        thus we need to create a new instance for retry.
        As a result, they are not final.
     */

    // pub queue and its read-write lock
    private final BlockingQueue<IMessage> pubQueue = new ArrayBlockingQueue<>(32);
    private final Object pubLock = new Object();

    private final Thread pubThread = new Thread(this::publish);
    private final Thread subThread = new Thread(this::subscribe);

    public PsmbEndpoint(String id,
                        String host,
                        int port,
                        String publicTopic,
                        String subscribePattern,
                        long subscriberId,
                        int keepAliveInterval) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.pubTopic = publicTopic;
        this.subPattern = subscribePattern;
        this.subId = subscriberId;
        this.keepAliveInterval = keepAliveInterval;
        // start pubsub threads
        pubThread.start();
        subThread.start();
    }

    private void publish() {
        final var logger = LoggerFactory.getLogger(LoggerNaming.name().of("endpoint").of("psmb")
                .of(String.format("%s,%s", host, pubTopic)).of("pub").toString());
        try {
            // reconnect loop
            while (true) {
                try (final var pub = new PublishClient(host, port)) {
                    // try to connect
                    try {
                        pub.connect();
                        pub.setPublish(pubTopic);
                    } catch (IOException | CommandFailureException ex) {
                        logger.error("Cannot connect to server", ex);
                        Util.SleepBeforeReconnect(logger);
                        continue;
                    }
                    // connected successfully, send messages
                    try {
                        // publish loop
                        long lastBeat = -1;
                        while (true) {
                            if (Math.abs(System.currentTimeMillis() - lastBeat) >= keepAliveInterval) {
                                pub.keepAlive();
                                lastBeat = System.currentTimeMillis();
                            }
                            var message = pubQueue.poll(Math.max(keepAliveInterval, 0), TimeUnit.MILLISECONDS);
                            if (message == null) continue;
                            pub.publish(PsmbMessageSerializer.serialize(message));
                        }
                    } catch (IOException | CommandFailureException ex) {
                        logger.error("Cannot publish message", ex);
                        pub.disconnect(); // reconnect in the outer loop
                        Util.SleepBeforeReconnect(logger);
                    }
                }
            }
        } catch (InterruptedException ignored) {
            logger.info("Thread is interrupted.");
        } finally {
            logger.info("Thread is stopping.");
        }
    }

    private void subscribe() {
        final var logger = LoggerFactory.getLogger(LoggerNaming.name().of("endpoint").of("psmb")
                .of(String.format("%s,%d,%s", host, subId, subPattern)).of("sub").toString());
        try {
            // reconnect loop
            while (true) {
                try (final var sub = new SubscribeClient(host, port, subPattern, keepAliveInterval, subId)) {
                    // try to connect
                    try {
                        sub.connect();
                        sub.setSubscribe(subPattern, subId);
                    } catch (IOException | CommandFailureException ex) {
                        logger.error("Cannot connect to server", ex);
                        Util.SleepBeforeReconnect(logger);
                        continue;
                    }
                    // connected successfully, receive messages
                    try {
                        // subscribe loop
                        sub.subscribe(raw -> {
                            try {
                                onMessage(PsmbMessageSerializer.deserialize(raw, this));
                            } catch (PsmbMessageSerializer.IllegalPackedMessageException ex) {
                                logger.error("Cannot decode message", ex);
                            }
                        });
                    } catch (IOException | CommandFailureException ex) {
                        logger.error("Cannot receive message", ex);
                        sub.disconnect(); // reconnect in the outer loop
                        Util.SleepBeforeReconnect(logger);
                    }
                }
            }
        } catch (InterruptedException ignored) {
            logger.info("Thread is interrupted.");
        } finally {
            logger.info("Thread is stopping.");
        }
    }


    private void onMessage(@NotNull IMessage message) {
        Objects.requireNonNull(message);
        this.router.sendMessage(message);
    }

    @Override
    public void sendMessage(IMessage message) {
        synchronized (pubLock) {
            pubQueue.add(message);
        }
    }

    @Override
    public void setRouter(IRouter router) {
        this.router = router;
    }

    @Override
    public void close() {
        pubThread.interrupt();
        subThread.interrupt();
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull EndpointNamespace namespace() {
        return EndpointNamespace.REMOTE;
    }

    @Override
    public String toString() {
        return "PsmbEndpoint{" +
                "router=" + router +
                ", id='" + id + '\'' +
                '}';
    }
}
