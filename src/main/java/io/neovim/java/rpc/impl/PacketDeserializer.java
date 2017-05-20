package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.Packet;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;

import java.io.IOException;
import java.util.Map;

import static io.neovim.java.rpc.impl.JsonParserUtil.nextInt;
import static io.neovim.java.rpc.impl.JsonParserUtil.nextString;
import static io.neovim.java.rpc.impl.JsonParserUtil.nextValue;

/**
 * @author dhleong
 */
public class PacketDeserializer extends JsonDeserializer<Packet> {
    private Map<Integer, Class<?>> requestedTypes;

    public PacketDeserializer(Map<Integer, Class<?>> requestedTypes) {
        this.requestedTypes = requestedTypes;
    }

    @Override
    public Packet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonParserUtil.expectNext(p, JsonTokenId.ID_NUMBER_INT);

        Packet.Type type = Packet.Type.create(p.getIntValue());
        final Packet packet;
        switch (type) {
        case REQUEST:
            packet = readRequest(p);
            break;
        case RESPONSE:
            packet = readResponse(p);
            break;
        default:
        case NOTIFICATION:
            packet = readNotification(p);
            break;
        }

        packet.type = type;
        return packet;
    }

    static Packet readNotification(JsonParser p) throws IOException {
        return NotificationPacket.create(
            /* event = */ nextString(p),
            /*  args = */ nextValue(p, JsonNode.class)
        );
    }

    static Packet readRequest(JsonParser p) throws IOException {
        return RequestPacket.create(
            /* requestId = */ nextInt(p),
            /*    method = */ nextString(p),
            /*      args = */ nextValue(p)
        );
    }

    Packet readResponse(JsonParser p) throws IOException {
        final int requestId = nextInt(p);
        final Object error = nextValue(p);
        final Object result;
        final Class<?> desiredType = this.requestedTypes.remove(requestId);
        if (desiredType == null) {
            result = nextValue(p);
        } else {
            result = nextValue(p, desiredType);
        }

        return ResponsePacket.create(
            requestId,
            error,
            result
        );
    }

}
