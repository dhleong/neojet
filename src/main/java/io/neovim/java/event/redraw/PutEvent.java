package io.neovim.java.event.redraw;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neovim.java.event.EventName;

import java.io.IOException;

import static io.neovim.java.rpc.impl.JsonParserUtil.expectNext;

/**
 * @author dhleong
 */
@JsonDeserialize(using = PutEvent.Deserializer.class)
@EventName("put")
public class PutEvent extends RedrawSubEvent<PutEvent> {

    public String value;

//    public Jsonn

    @Override
    public String toString() {
        return "PutEvent{" +
            (value == null
                ? "value=null"
                : "value='" + value + '\'') +
            '}';
    }

    public static class Deserializer extends JsonDeserializer<PutEvent> {
        @Override
        public PutEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new UnsupportedOperationException("Can only read into existing instances");
        }

        @Override
        public PutEvent deserialize(JsonParser p, DeserializationContext ctxt, PutEvent intoValue) throws IOException {
            if (!"put".equals(p.getValueAsString())) {
                throw new JsonParseException(p, "Expected to start with `put`");
            }
            p.nextToken();

            StringBuilder stringBuilder = new StringBuilder(32);
            while (p.getCurrentToken() != JsonToken.END_ARRAY) {
                if (p.getCurrentToken() == JsonToken.START_ARRAY) {
                    p.nextValue();
                    Character read = p.readValueAs(Character.class);
                    stringBuilder.append(read);
                    expectNext(p, JsonToken.END_ARRAY);

                } else if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                    stringBuilder.append(p.getValueAsString());

                } else {
                    throw new IllegalStateException("?!?" + p.getCurrentToken());
                }

                p.nextToken();
            }
            System.out.println("READ: " + stringBuilder);
//            System.out.println(p.getCurrentToken() + " / " + p.getValueAsString());
//            System.out.println(p.nextTextValue());
//            while (p.getCurrentToken() != JsonToken.END_ARRAY) {
//                System.out.println(" " + p.readValueAs(JsonNode.class));
//            }
            intoValue.value = stringBuilder.toString();
            return intoValue;
        }
    }
}
