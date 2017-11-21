package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.jackson.dataformat.MessagePackExtensionType;
import org.msgpack.jackson.dataformat.MessagePackGenerator;

import java.io.IOException;

/**
 * @author dhleong
 */
public class RemoteObjectSerializer<T extends RemoteObject> extends JsonSerializer<T> {

    private final Class<T> type;
    final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();

    private RemoteObjectSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // it should be safe to reuse the Packer since we only ever write serially
        packer.clear();
        packer.packLong(value.id);

        MessagePackGenerator mpgen = (MessagePackGenerator) gen;
        mpgen.writeExtensionType(new MessagePackExtensionType(
            NeovimExtensionTypes.getInstance().getTypeIdOf(type),
            packer.toByteArray()
        ));
    }

    public static <T extends RemoteObject> RemoteObjectSerializer<T> create(Class<T> type) {
        return new RemoteObjectSerializer<>(type);
    }

}
