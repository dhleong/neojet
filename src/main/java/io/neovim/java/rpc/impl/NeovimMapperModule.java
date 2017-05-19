package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.neovim.java.Buffer;
import io.neovim.java.Rpc;
import io.neovim.java.Tabpage;
import io.neovim.java.Window;
import io.neovim.java.rpc.Packet;
import io.reactivex.functions.BiFunction;

import java.util.Map;

/**
 * @author dhleong
 */
public class NeovimMapperModule extends SimpleModule {
    private final Rpc rpc;

    public NeovimMapperModule(Rpc rpc, Map<Integer, Class<?>> requestedTypes) {
        this.rpc = rpc;

        addDeserializer(Packet.class,
            new PacketDeserializer(requestedTypes));

        defineRemoteObject(Buffer.class, Buffer::new);
        defineRemoteObject(Window.class, Window::new);
        defineRemoteObject(Tabpage.class, Tabpage::new);
    }

    public JsonDeserializer<?> findDeserializer(Class<?> klass) {
        try {
            return _deserializers.findEnumDeserializer(
                klass, null, null);
        } catch (JsonMappingException e) {
            throw new IllegalStateException("No Deserializer for " + klass, e);
        }
    }

    private <T extends RemoteObject> void defineRemoteObject(
            Class<T> type, BiFunction<Rpc, Long, T> factory) {
        addDeserializer(type,
            RemoteObjectDeserializer.create(factory, rpc));
        addSerializer(type,
            RemoteObjectSerializer.create(type));
    }

}
