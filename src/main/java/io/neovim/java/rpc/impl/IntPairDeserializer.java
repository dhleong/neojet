package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.neovim.java.IntPair;

import java.io.IOException;

import static io.neovim.java.rpc.impl.JsonParserUtil.expect;
import static io.neovim.java.rpc.impl.JsonParserUtil.expectNext;
import static io.neovim.java.rpc.impl.JsonParserUtil.nextInt;

/**
 * @author dhleong
 */
public class IntPairDeserializer extends JsonDeserializer<IntPair> {
    @Override
    public IntPair deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        expect(p, JsonToken.START_ARRAY);
        IntPair pair = new IntPair(
            nextInt(p),
            nextInt(p)
        );
        expectNext(p, JsonToken.END_ARRAY);
        return pair;
    }
}
