package io.neovim.java.event.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

import java.io.IOException;

/**
 * @author dhleong
 */
public class FakeArrayStartJsonParser extends JsonParserDelegate {
    boolean first = true;

    public FakeArrayStartJsonParser(JsonParser p) {
        super(p);
    }

    @Override
    public boolean isExpectedStartArrayToken() {
        return first || super.isExpectedStartArrayToken();
    }

    @Override
    public JsonToken nextToken() throws IOException {
        if (first) {
            first = false;
            return getCurrentToken();
        }
        return super.nextToken();
    }
}
