package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.neovim.java.IntPair;

import java.io.IOException;

import static io.neovim.java.rpc.impl.JsonParserUtil.nextInt;

/**
 * @author dhleong
 */
public class IntPairDeserializer extends JsonDeserializer<IntPair> {
    @Override
    public IntPair deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonParserUtil.expect(p, JsonTokenId.ID_START_ARRAY);
        IntPair pair = new IntPair(
            nextInt(p),
            nextInt(p)
        );
        JsonParserUtil.expectNext(p, JsonTokenId.ID_END_ARRAY);
        return pair;
    }
}
