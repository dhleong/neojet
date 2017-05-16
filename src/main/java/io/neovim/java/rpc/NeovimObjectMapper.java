package io.neovim.java.rpc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.neovim.java.rpc.impl.PacketDeserializer;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * @author dhleong
 */
public class NeovimObjectMapper {
    private NeovimObjectMapper() {
        // no instances please
    }

    public static ObjectMapper newInstance() {
        MessagePackFactory factory = new MessagePackFactory();
        factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        SimpleModule packetModule = new SimpleModule();
        packetModule.addDeserializer(Packet.class, new PacketDeserializer());

        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.registerModule(packetModule);
        return mapper;
    }
}
