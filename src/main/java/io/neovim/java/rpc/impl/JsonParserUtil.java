package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.JavaType;

import java.io.IOException;

/**
 * @author dhleong
 */
public class JsonParserUtil {
    static void expect(JsonParser p, JsonToken type) throws IOException {
        JsonToken tok = p.getCurrentToken();
        if (tok != type) {
            throw new JsonParseException(p,
                "Expected "  + type + " but was " + tok);
        }
    }

    public static void expectNext(JsonParser p, JsonToken type) throws IOException {
        p.nextToken();
        expect(p, type);
    }

    static int nextInt(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsInt();
    }

    static long nextLong(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsLong();
    }

    public static String nextString(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsString();
    }

    static Object nextValue(JsonParser p) throws IOException {
        return nextValue(p, Object.class);
    }

    static <T> T nextValue(JsonParser p, Class<T> type) throws IOException {
        JsonToken tok = p.nextValue();
        if (tok.id() == JsonTokenId.ID_END_ARRAY) {
            return null;
        }
        return p.readValueAs(type);
    }

    static <T> T nextValue(JsonParser p, JavaType type) throws IOException {
        JsonToken tok = p.nextValue();
        if (tok.id() == JsonTokenId.ID_END_ARRAY) {
            return null;
        }
        return p.getCodec().readValue(p, type);
    }
}
