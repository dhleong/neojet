package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.neovim.java.event.EventsManager;
import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.Packet;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.neovim.java.rpc.impl.JsonParserUtil.expectNext;
import static io.neovim.java.rpc.impl.JsonParserUtil.nextInt;
import static io.neovim.java.rpc.impl.JsonParserUtil.nextString;
import static io.neovim.java.rpc.impl.JsonParserUtil.nextValue;

/**
 * @author dhleong
 */
public class PacketDeserializer extends JsonDeserializer<Packet> {
    private final Map<Integer, Class<?>> requestedTypes;
    private final EventsManager eventsManager;
    private final boolean debug;

    public PacketDeserializer(
        Map<Integer, Class<?>> requestedTypes,
        EventsManager eventsManager
    ) {
        this(requestedTypes, eventsManager, false);
    }

    public PacketDeserializer(
        Map<Integer, Class<?>> requestedTypes,
        EventsManager eventsManager,
        boolean debug
    ) {
        this.requestedTypes = requestedTypes;
        this.eventsManager = eventsManager;
        this.debug = debug;
    }

    @Override
    public Packet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        expectNext(p, JsonToken.VALUE_NUMBER_INT);

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

    Packet readNotification(JsonParser p) throws IOException {
        final String event = nextString(p);
        final JavaType type = eventsManager.getEventValueType(event, true);
        final JsonParser actualParser;

        final JsonNode asNode;
        if (debug) {
            asNode = nextValue(p, JsonNode.class);
            actualParser = p.getCodec().treeAsTokens(asNode);
        } else {
            asNode = null;
            actualParser = p;
        }

        try {
            if (type == null) {
                return NotificationPacket.createFromList(event,
                    nextValue(actualParser, JsonNode.class));
            } else {
                //noinspection unchecked
                return NotificationPacket.create(
                    event,
                    (List) nextValue(actualParser, type)
                );
            }
        } catch (JsonMappingException|JsonParseException e) {
            String triedtoParse = asNode == null
                ? String.format("`%s` event", event)
                : asNode.toString();
            throw JsonMappingException.from(p,
                "Failed to parse:\n\n    " + triedtoParse + "\n\n as " + (type == null ? "JsonNode" : type),
                e);
        }
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
