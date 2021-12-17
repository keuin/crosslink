package com.keuin.crosslink.messaging.endpoint.remote.telegram;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.sender.ISender;
import com.keuin.crosslink.util.LoggerNaming;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TelegramGroupEndpoint implements IEndpoint {
    // TODO make this compatible with both chat and group
    private final Logger logger;
    private final TelegramBot bot;
    private final String endpointId;
    private final long chatId;
    private IRouter router = null;

    public TelegramGroupEndpoint(@NotNull String token, @NotNull String endpointId, long chatId) {
        this(token, endpointId, chatId, null);
    }

    public TelegramGroupEndpoint(@NotNull String token,
                                 @NotNull String endpointId,
                                 long chatId,
                                 @Nullable String proxyUrl) {
        Objects.requireNonNull(token);
        Objects.requireNonNull(endpointId);
        this.bot = new TelegramBot
                .Builder(token)
                .okHttpClient(new OkHttpClient.Builder()
                        .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 10808)))
                        .build())
                .build();
        this.endpointId = endpointId;
        this.chatId = chatId;
        this.logger = LoggerFactory.getLogger(LoggerNaming.name().of("endpoint").of("telegram").of(endpointId).toString());
        bot.setUpdatesListener(this::onUpdate);
    }

    private int onUpdate(List<Update> updates) {
        var lastId = UpdatesListener.CONFIRMED_UPDATES_NONE;
        for (Update u : updates) {
            lastId = u.updateId();
            if (u.message().chat().id() != chatId) continue;
            if (u.message() == null) continue;
            var message = u.editedMessage();
            if (message == null) message = u.message();
            if (message == null) continue;
            var msgText = message.text();
            if (msgText == null) continue;
            if (router == null) {
                logger.error("No router associated with this endpoint. Message is dropped.");
                continue;
            }
            // TODO support other types of messages (currently only plaintext is supported)
            var buf = ByteBuffer.allocate(Long.BYTES);
            buf.putLong(u.message().from().id());
            var sender = ISender.create(u.message().from().username(), UUID.nameUUIDFromBytes(buf.array()));
            var msgObj = IMessage.create(this, sender, msgText);
            logger.info("Received plain text message from telegram: " + msgObj);
            router.sendMessage(msgObj);
//            logger.debug(u.toString());
        }
        // return id of last processed update or confirm them all
        return lastId;
    }

    @Override
    public void sendMessage(IMessage message) {
        bot.execute(new SendMessage(chatId, message.plainTextDisplay()));
    }


    @Override
    public void setRouter(IRouter router) {
        this.router = router;
    }

    public void close() {
        bot.removeGetUpdatesListener();
        bot.shutdown();
    }

    @Override
    public @NotNull String id() {
        return endpointId;
    }

    @Override
    public @NotNull EndpointNamespace namespace() {
        return EndpointNamespace.REMOTE;
    }

    @Override
    public String toString() {
        return "TelegramGroupEndpoint{" +
                "id='" + endpointId + '\'' +
                ", chatId=" + chatId +
                '}';
    }
}
