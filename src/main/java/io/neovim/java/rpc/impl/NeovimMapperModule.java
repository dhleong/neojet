package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.neovim.java.Buffer;
import io.neovim.java.IntPair;
import io.neovim.java.Rpc;
import io.neovim.java.Tabpage;
import io.neovim.java.Window;
import io.neovim.java.event.EventsManager;
import io.neovim.java.event.redraw.RedrawSubEvent;
import io.neovim.java.rpc.Packet;
import io.reactivex.functions.BiFunction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dhleong
 */
public class NeovimMapperModule extends SimpleModule {
    private final Rpc rpc;

    public final Map<Class<?>, JsonDeserializer<?>> extensionTypeDeserializers = new HashMap<>();

    public NeovimMapperModule(
            Rpc rpc,
            Map<Integer, Class<?>> requestedTypes,
            EventsManager eventsManager,
            boolean debug
    ) {
        this.rpc = rpc;

        addDeserializer(Packet.class,
            new PacketDeserializer(requestedTypes, eventsManager, debug));
        addDeserializer(IntPair.class,
            new IntPairDeserializer());

        defineRemoteObject(Buffer.class, Buffer::new);
        defineRemoteObject(Window.class, Window::new);
        defineRemoteObject(Tabpage.class, Tabpage::new);

        setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(
                    DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == RedrawSubEvent.class) {
                    return new RedrawSubEvent.Deserializer();
                }
                return deserializer;
            }
        });
    }

    public JsonDeserializer<?> findDeserializer(Class<?> klass) {
//        try {
//            return _deserializers.findEnumDeserializer(
//                klass, null, null);
//        } catch (JsonMappingException e) {
//            throw new IllegalStateException("No Deserializer for " + klass, e);
//        }

        JsonDeserializer<?> deserializer = extensionTypeDeserializers.get(klass);
        if (deserializer == null) {
            throw new IllegalStateException("No Deserializer for " + klass);
        }

        return deserializer;
    }

    private <T extends RemoteObject> void defineRemoteObject(
            Class<T> type, BiFunction<Rpc, Long, T> factory) {
//        addDeserializer(type,
//            RemoteObjectDeserializer.create(factory, rpc));
        extensionTypeDeserializers.put(type,
            RemoteObjectDeserializer.create(factory, rpc));
        addSerializer(type,
            RemoteObjectSerializer.create(type));
        addDeserializer(type,
            new JsonDeserializer<T>() {
                @Override
                public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    //noinspection unchecked
                    return (T) p.getEmbeddedObject();
                }
            });
    }

}
