package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.neovim.java.Buffer;
import io.neovim.java.Rpc;
import io.neovim.java.rpc.Packet;

import java.util.Map;

/**
 * @author dhleong
 */
public class NeovimMapperModule extends SimpleModule {
    public NeovimMapperModule(Rpc rpc, Map<Integer, Class<?>> requestedTypes) {
        addDeserializer(Packet.class,
            new PacketDeserializer(requestedTypes));

        addDeserializer(Buffer.class,
            RemoteObjectDeserializer.create(Buffer::new, rpc));
        addSerializer(Buffer.class,
            RemoteObjectSerializer.create(Buffer.class));
    }

    public JsonDeserializer<?> findDeserializer(Class<?> klass) {
        try {
            return _deserializers.findEnumDeserializer(
                klass, null, null);
        } catch (JsonMappingException e) {
            throw new IllegalStateException("No Deserializer for " + klass, e);
        }
    }
}
