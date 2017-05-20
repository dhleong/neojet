package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;

import java.io.IOException;

/**
 * @author dhleong
 */
public class JsonParserUtil {
    static JsonToken expect(JsonParser p, int type) throws IOException {
        JsonToken tok = p.getCurrentToken();
        if (tok.id() != type) {
            throw new IllegalStateException(
                "Expected "  + type + " but was " + tok.id());
        }

        return tok;
    }

    static JsonToken expectNext(JsonParser p, int type) throws IOException {
        JsonToken tok = p.nextToken();
        expect(p, type);
        return tok;
    }

    static int nextInt(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsInt();
    }

    static long nextLong(JsonParser p) throws IOException {
        p.nextValue();
        return p.getValueAsLong();
    }

    static String nextString(JsonParser p) throws IOException {
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
}
