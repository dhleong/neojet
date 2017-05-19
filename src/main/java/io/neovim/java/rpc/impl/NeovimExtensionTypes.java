package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.databind.JsonDeserializer;
import io.neovim.java.Buffer;
import org.msgpack.jackson.dataformat.ExtensionTypeCustomDeserializers;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: update this with vim_get_api_info
 * @author dhleong
 */
public class NeovimExtensionTypes {
    static NeovimExtensionTypes instance;

    final Map<Class<?>, Byte> types = new ConcurrentHashMap<>(3);

    // singleton
    private NeovimExtensionTypes() {
        // default values:
        types.put(Buffer.class, (byte) 0);
        // TODO Window, Tabpage
    }

    public byte getTypeIdOf(Class<? extends RemoteObject> type) {
        Byte id =  types.get(type);
        if (id == null) throw new IllegalArgumentException("Unknown type: " + type);

        return id;
    }

    public <T extends RemoteObject> void setTypeId(Class<T> type, byte id) {
        types.put(type, id);
    }

    public ExtensionTypeCustomDeserializers getExtensionDeserializers(
            MessagePackFactory factory, NeovimMapperModule module) {

        final ExtensionTypeCustomDeserializers deserializers
            = new ExtensionTypeCustomDeserializers();

        for (Map.Entry<Class<?>, Byte> e : types.entrySet()) {
            // it'd be nice to just use the addTargetClass method, but
            //  that thing uses its own ObjectMapper that doesn't know
            //  about our custom types!
            Class<?> targetClass = e.getKey();
            JsonDeserializer<?> deserializer = module.findDeserializer(targetClass);
            deserializers.addCustomDeser(e.getValue(), data -> deserializer.deserialize(
                factory.createParser(data),
                null
            ));
        }

        return deserializers;
    }

    public static NeovimExtensionTypes getInstance() {
        NeovimExtensionTypes existing = instance;
        if (existing != null) return existing;
        return instance = new NeovimExtensionTypes();
    }
}
