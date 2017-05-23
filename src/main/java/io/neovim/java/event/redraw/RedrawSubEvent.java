package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.neovim.java.event.Event;
import io.neovim.java.event.EventsManager;

import java.io.IOException;

import static io.neovim.java.rpc.impl.JsonParserUtil.nextString;

/**
 * @author dhleong
 */
//@JsonTypeInfo(
//    use = JsonTypeInfo.Id.CUSTOM,
//    include = JsonTypeInfo.As.PROPERTY,
//    property = "type"
////    defaultImpl = UnknownRedrawEvent.class
////    defaultImpl = Void.class
//)
//@JsonTypeIdResolver(RedrawSubEvent.EventTypeIdResolver.class)
//@JsonSubTypes({
//    @JsonSubTypes.Type(name = "cursor_goto", value = CursorGotoEvent.class),
////    @JsonSubTypes.Type(name = "put", value = PutEvent.class),
//})
//@JsonDeserialize(using = RedrawSubEvent.Deserializer.class)
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public abstract class RedrawSubEvent<T> implements Event<T> {

    @JsonIgnore
    public String type;

    public String getType() {
        return type;
    }

    @Override
    public T value() {
        //noinspection unchecked
        return (T) this;
    }

    static class EventTypeIdResolver implements TypeIdResolver {
        TypeFactory typeFactory = TypeFactory.defaultInstance();

        @Override
        public void init(JavaType baseType) {

            System.out.println("Init" + baseType);
        }

        @Override
        public String idFromValue(Object value) {
            System.out.println("idFromValue: " + value);
            return null;
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            System.out.println("idFromValeAnd: " + value + " / " + suggestedType);
            return null;
        }

        @Override
        public String idFromBaseType() {
            System.out.println("idFromBase");
            return null;
        }

        @Override
        public JavaType typeFromId(String id) {
            return typeFromId(null, id);
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            System.err.println("typeFrom:" + id);
            return typeFactory.constructType(UnknownRedrawEvent.class);
        }

        @Override
        public String getDescForKnownTypeIds() {
            System.out.println("getDesc");
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.NAME;
        }
    }

    public static class Deserializer extends StdDeserializer<RedrawSubEvent<?>> {
        static final Class<?>[] KNOWN_EVENTS = {
            CursorGotoEvent.class,
            PutEvent.class
        };

        private static final EventsManager eventsManager = new EventsManager();
        static {
            for (Class<?> type : KNOWN_EVENTS) {
                eventsManager.register(type);
            }
        }
        public Deserializer() {
            super(RedrawSubEvent.class);
        }

        @Override
        public RedrawSubEvent<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                throw new JsonParseException(p, "Expected START_ARRAY but was " + p.getCurrentToken());
            }

            String event = nextString(p);
            if (event == null) {
                throw new JsonParseException(p, "Unable to read event!?");
            }
            JavaType type = resolveType(event);

            RedrawSubEvent<?> result;
            try {
                result = (RedrawSubEvent<?>) type.getRawClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JsonParseException(p, "Errr", e);
            }

            result.type = event;

            try {
                ctxt.findRootValueDeserializer(type)
                    .deserialize(p, ctxt, result);
            } catch (IOException e) {
                throw new JsonParseException(p, "Error reading Redraw subevent `" + event + "` as " + type, e);
            }

            System.out.println("!!! READ " + event + " AS " + type + " ->> " + result);
//            System.err.println();
            return result;
        }

        private JavaType resolveType(String event) {
            JavaType type = eventsManager.getEventValueType(event, true);
            return type == null
                ? TypeFactory.defaultInstance().constructType(UnknownRedrawEvent.class)
                : type;
        }
    }
}
