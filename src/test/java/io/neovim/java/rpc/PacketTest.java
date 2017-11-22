package io.neovim.java.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neovim.java.event.EventName;
import io.neovim.java.event.EventsManager;
import org.junit.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.neovim.java.NeovimAssertions.assertThat;


/**
 * @author dhleong
 */
public class PacketTest {

    @Test
    public void readNotification() throws IOException {
        MessageBufferPacker pack = MessagePack.newDefaultBufferPacker();
        pack.packArrayHeader(3);
        pack.packInt(Packet.Type.NOTIFICATION.ordinal());
        pack.packString("takeoff");
        pack.packArrayHeader(2);
        pack.packInt(12);
        pack.packInt(34);

        ObjectMapper mapper = NeovimObjectMapper.newInstance();
        Packet packet = mapper.readValue(
            pack.toByteArray(),
            Packet.class
        );

        assertThat(packet)
            .isInstanceOf(NotificationPacket.class)
            .hasType(Packet.Type.NOTIFICATION);
        assertThat((NotificationPacket) packet)
            .hasEvent("takeoff")
            .hasArgs(listFromJson("[12, 34]").toArray());
//            .hasArgs(nodeFromJson("[12, 34]"));
    }


    @EventName("takeoff")
    static class TakeoffEvent extends NotificationPacket<Integer> {
    }

    @Test
    public void readCustomNotification() throws IOException {
        MessageBufferPacker pack = MessagePack.newDefaultBufferPacker();
        pack.packArrayHeader(3);
        pack.packInt(Packet.Type.NOTIFICATION.ordinal());
        pack.packString("takeoff");
        pack.packArrayHeader(2);
        pack.packInt(12);
        pack.packInt(34);

        EventsManager eventsManager = new EventsManager();
        eventsManager.register(TakeoffEvent.class);
        ObjectMapper mapper = NeovimObjectMapper.newInstance(eventsManager);
        Packet packet = mapper.readValue(
            pack.toByteArray(),
            Packet.class
        );

        assertThat(packet)
            .isInstanceOf(TakeoffEvent.class)
            .hasType(Packet.Type.NOTIFICATION);
        assertThat((TakeoffEvent) packet)
            .hasEvent("takeoff")
            .hasArgs(12, 34);
    }

    @Test
    public void writeNotification() throws IOException {
        NotificationPacket<JsonNode> notif = NotificationPacket.createFromList(
            "landing",
            nodeFromJson("[42, 9001]")
        );

        MessageBufferPacker pack = MessagePack.newDefaultBufferPacker();
        pack.packArrayHeader(3);
        pack.packInt(Packet.Type.NOTIFICATION.ordinal());
        pack.packString("landing");
        pack.packArrayHeader(2);
        pack.packInt(42);
        pack.packInt(9001);

        ObjectMapper mapper = NeovimObjectMapper.newInstance();
        byte[] actual = mapper.writeValueAsBytes(notif);

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(actual);
        assertThat(unpacker.unpackArrayHeader()).isEqualTo(3);
        assertThat(unpacker.unpackInt()).isEqualTo(Packet.Type.NOTIFICATION.ordinal());

        byte[] expected = pack.toByteArray();

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    public void writeRequest() throws IOException {
        RequestPacket request = RequestPacket.create(
            "cargo",
            Arrays.asList(12, 34)
        );
        request.requestId = 42;

        MessageBufferPacker pack = MessagePack.newDefaultBufferPacker();
        pack.packArrayHeader(4);
        pack.packInt(Packet.Type.REQUEST.ordinal());
        pack.packInt(42);
        pack.packString("cargo");
        pack.packArrayHeader(2);
        pack.packInt(12);
        pack.packInt(34);

        ObjectMapper mapper = NeovimObjectMapper.newInstance();
        byte[] actual = mapper.writeValueAsBytes(request);

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(actual);
        assertThat(unpacker.unpackArrayHeader()).isEqualTo(4);
        assertThat(unpacker.unpackInt()).isEqualTo(Packet.Type.REQUEST.ordinal());

        byte[] expected = pack.toByteArray();

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    public void symmetrical_RequestPacket() throws IOException {
        RequestPacket original = RequestPacket.create(
            "landing",
            Arrays.asList(42, 9001)
        );

        ObjectMapper mapper = NeovimObjectMapper.newInstance();
        byte[] bytes = mapper.writeValueAsBytes(original);
        Packet restored = mapper.readValue(bytes, Packet.class);

        assertThat(restored)
            .isInstanceOf(RequestPacket.class)
            .isEqualTo(original);
    }

    static JsonNode nodeFromJson(String json) throws IOException {
        return new ObjectMapper().readTree(json);
    }

    static List<JsonNode> listFromJson(String json) throws IOException {
        JsonNode node = nodeFromJson(json);
        final int size = node.size();
        ArrayList<JsonNode> result = new ArrayList<>(size);
        for (int i=0; i < size; ++i) {
            result.add(node.get(i));
        }
        return result;
    }
}
