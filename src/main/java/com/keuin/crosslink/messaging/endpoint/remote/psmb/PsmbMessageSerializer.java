package com.keuin.crosslink.messaging.endpoint.remote.psmb;

import com.keuin.crosslink.messaging.endpoint.EndpointNamespace;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.router.IRouter;
import com.keuin.crosslink.messaging.sender.ISender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bson.BsonBinaryReader;
import org.bson.BsonType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class PsmbMessageSerializer {

    // psmb BSON message decoder
    // copied from BungeeCross

    public static class IllegalPackedMessageException extends Exception {
        public IllegalPackedMessageException() {
            super();
        }

        public IllegalPackedMessageException(String missingPropertyName) {
            super(String.format("missing BSON property `%s`.", missingPropertyName));
        }

        public IllegalPackedMessageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Deserialize a message from BSON data.
     *
     * @param type the type.
     * @param data the bytes.
     * @return the message.
     * @throws IOException when failed to decompress gzip data.
     */
    private static Component fromSerializedMessage(int type, byte[] data) throws IOException {
        if (type == MessageType.TEXT) {
            return Component.text(new String(data, StandardCharsets.UTF_8));
        } else if (type == MessageType.GZIP_TEXT) {
            // decompress gzip bytes, then decode to string
            var os = new GZIPInputStream(new ByteArrayInputStream(data));
            return Component.text(new String(os.readAllBytes(), StandardCharsets.UTF_8));
        } else if (type == MessageType.IMAGE) {
            return Component.text("[图片]", TextColor.color(0xFFAA00), TextDecoration.BOLD);
        } else {
            return Component.text("[未知消息]", TextColor.color(0xAAAAAA), TextDecoration.BOLD);
        }
    }

    private static class MessageType {
        public static final int TEXT = 0;
        public static final int IMAGE = 1;
        public static final int GZIP_TEXT = 2;
    }

    public static byte[] serialize(@NotNull IMessage message) {
        // TODO
        Objects.requireNonNull(message);
        throw new RuntimeException("not implemented");
    }

    public static @NotNull IMessage deserialize(@NotNull ByteBuffer buffer, @NotNull IEndpoint source) throws IllegalPackedMessageException {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(source);
        try {
            var reader = new BsonBinaryReader(buffer);

            reader.readStartDocument();

            if (isBsonKeyInvalid(reader, "endpoint"))
                throw new IllegalPackedMessageException("endpoint");
            var endpoint = reader.readString();

            if (isBsonKeyInvalid(reader, "sender"))
                throw new IllegalPackedMessageException("sender");
            var sender = reader.readString();

            // read message array
            var msgBuilder = Component.text();

            if (isBsonKeyInvalid(reader, "msg"))
                throw new IllegalPackedMessageException("msg");
            reader.readStartArray();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                // read sub array
                reader.readStartArray();
//                var i = reader.readInt32();
//                System.out.println("Index: " + i);
                // we only deal with text messages
                var messageType = reader.readInt32();
                var data = reader.readBinaryData().getData();
                try {
                    msgBuilder.append(fromSerializedMessage(messageType, data));
                } catch (IOException e) {
                    throw new IllegalPackedMessageException("Unsupported message block type: " + messageType, e);
                }
                reader.readEndArray();
            }
            reader.readEndArray();

            if (isBsonKeyInvalid(reader, "time"))
                throw new IllegalPackedMessageException("time");
            var createTime = reader.readInt64();

            reader.readEndDocument();

            // TODO: refactor, create a new class `UnpackedRedisMessage`,
            //  which has a more powerful internal representation
            var senderObj = ISender.create(sender, UUID.nameUUIDFromBytes(sender.getBytes(StandardCharsets.UTF_8)));
            // wrap the original source to rename the 'seen' endpoint to remote endpoint name
            var wrappedSource = new IEndpoint() {
                // delegate all methods to "source"
                @Override
                public void sendMessage(IMessage message) {
                    source.sendMessage(message);
                }

                @Override
                public void setRouter(IRouter router) {
                    source.setRouter(router);
                }

                @Override
                public void close() {
                    source.close();
                }

                @Override
                public @NotNull String id() {
                    return source.id();
                }

                @Override
                public @NotNull EndpointNamespace namespace() {
                    return source.namespace();
                }

                @Override
                public @NotNull String friendlyName() {
                    return endpoint;
                }

                @Override
                public String namespacedId() {
                    return source.namespacedId();
                }
            };
            return IMessage.create(wrappedSource, senderObj, msgBuilder.asComponent());
        } catch (Exception e) {
            throw new IllegalPackedMessageException("invalid packed message data", e);
        }
    }

    /**
     * Read in one BSON key and check if it is invalid.
     *
     * @param reader  the BSON reader.
     * @param keyName expected key.
     * @return if the key name equals to what is expected.
     */
    private static boolean isBsonKeyInvalid(BsonBinaryReader reader, String keyName) {
        var name = reader.readName();
        return !keyName.equals(name);
    }
}
