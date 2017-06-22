package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.neovim.java.event.Event;
import io.neovim.java.event.EventName;
import io.neovim.java.event.EventsManager;
import io.neovim.java.event.impl.FakeArrayStartJsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static io.neovim.java.rpc.impl.JsonParserUtil.nextString;

/**
 * @author dhleong
 * @param <T> Type of data transmitted by this class. If you don't
 *            provide your own Deserializer, you MUST annotate it
 *            with <code>@JsonFormat(shape = JsonFormat.Shape.ARRAY)</code>
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public abstract class RedrawSubEvent<T> implements Event<List<T>> {

    public String redrawType;

    @JsonUnwrapped
    public List<T> value;

    @Override
    public String kind() {
        return redrawType;
    }

    @Override
    public List<T> value() {
        return value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "redrawType='" + redrawType + '\'' +
            ", value=" + value +
            '}';
    }

    public static class Deserializer extends StdDeserializer<RedrawSubEvent<?>> {
        static final Class<?>[] KNOWN_EVENTS = {
            ClearScreenEvent.class,
            CursorGotoEvent.class,
            EolClearEvent.class,
            ModeChangeEvent.class,
            ModeInfoSetEvent.class,
            PutEvent.class,
            ScrollEvent.class,
            SetScrollRegionEvent.class,
            UpdateColorEvent.class,
        };

        private static final EventsManager eventsManager = new EventsManager();
        private static final HashMap<String, Class<?>> eventToEventType = new HashMap<>();
        static {
            for (Class<?> type : KNOWN_EVENTS) {
                eventsManager.register(type);

                String[] names = type.getAnnotation(EventName.class).value();
                for (String name : names) {
                    eventToEventType.put(name, type);
                }
            }
        }
        public Deserializer() {
            super(RedrawSubEvent.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public RedrawSubEvent<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                throw new JsonParseException(p, "Expected START_ARRAY but was " + p.getCurrentToken());
            }

            String event = nextString(p);
            p.nextToken();
            if (event == null) {
                throw new JsonParseException(p, "Unable to read event!?");
            }
            Class<?> type = resolveType(event);

            RedrawSubEvent<?> result;
            try {
                result = (RedrawSubEvent<?>) type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JsonParseException(p, "Unable to instantiate " + type, e);
            }

            result.redrawType = event;

            JavaType valueType = resolveValueType(event);
            JsonDeserializer valueDeserializer =
                ctxt.findRootValueDeserializer(valueType);
            final List valueList;
            try {
                valueList = (List) valueDeserializer.deserialize(
                    new FakeArrayStartJsonParser(p),
                    ctxt
                );
            } catch (IOException e) {
                throw new JsonParseException(p, "Error reading Redraw subevent `" + event + "` as " + type, e);
            }

            result.value = valueList;
            return result;
        }

        private Class<?> resolveType(String event) {
            Class<?> type = eventToEventType.get(event);
            if (type == null) {
                return UnknownRedrawEvent.class;
            }

            return type;
        }

        private JavaType resolveValueType(String event) {
            JavaType type = eventsManager.getEventValueType(event, true);
            return type == null
                ? TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, JsonNode.class)
                : type;
        }
    }
}


