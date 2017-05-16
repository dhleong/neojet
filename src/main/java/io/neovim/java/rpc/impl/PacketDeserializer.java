package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.Packet;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;

import java.io.IOException;

/**
 * @author dhleong
 */
public class PacketDeserializer extends JsonDeserializer<Packet> {
    @Override
    public Packet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        expect(p, JsonTokenId.ID_NUMBER_INT);

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
            /*  args = */ nextValue(p)
        );
    }

    static Packet readRequest(JsonParser p) throws IOException {
        return RequestPacket.create(
            /* requestId = */ nextInt(p),
            /*    method = */ nextString(p),
            /*      args = */ nextValue(p)
        );
    }

    static Packet readResponse(JsonParser p) throws IOException {
        return ResponsePacket.create(
            /* requestId = */ nextInt(p),
            /*     error = */ nextValue(p),
            /*    result = */ nextValue(p)
        );
    }

    static JsonToken expect(JsonParser p, int type) throws IOException {
        JsonToken tok = p.nextToken();
        if (tok.id() != type) {
            throw new IllegalStateException(
                "Expected "  + type + " but was " + tok.id());
        }

        return tok;
    }

    static int nextInt(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsInt();
    }

    static String nextString(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsString();
    }

    static Object nextValue(JsonParser p) throws IOException {
        JsonToken tok = p.nextValue();
        if (tok.id() == JsonTokenId.ID_END_ARRAY) {
            return null;
        }
        return p.readValueAs(Object.class);
    }

}
