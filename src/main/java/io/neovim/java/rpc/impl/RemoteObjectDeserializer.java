package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.neovim.java.Rpc;
import io.reactivex.functions.BiFunction;

import java.io.IOException;

import static io.neovim.java.rpc.impl.JsonParserUtil.nextLong;

/**
 * @author dhleong
 */
public class RemoteObjectDeserializer<T extends RemoteObject>
        extends JsonDeserializer<T> {

    private final BiFunction<Rpc, Long, T> factory;
    private final Rpc rpc;

    private RemoteObjectDeserializer(BiFunction<Rpc, Long, T> factory, Rpc rpc) {
        this.factory = factory;
        this.rpc = rpc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        long id = nextLong(p);

        try {
            return factory.apply(rpc, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends RemoteObject> RemoteObjectDeserializer<T> create(
            BiFunction<Rpc, Long, T> factory, Rpc rpc) {
        return new RemoteObjectDeserializer<>(factory, rpc);
    }
}
