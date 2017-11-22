package io.neovim.java.event;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.neovim.java.event.redraw.RedrawSubEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class EventsManagerTest {
    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();

    EventsManager manager;

    @Before public void setUp() {
        manager = new EventsManager();
    }

    @Test public void handleBuiltInRedrawType() {
        assertThat(manager.getEventValueType("redraw"))
            .isEqualTo(listTypeOf(genericType(RedrawSubEvent.class)));
    }

    static JavaType listTypeOf(JavaType elementType) {
        return typeFactory.constructCollectionType(List.class, elementType);
    }

    static JavaType genericType(Class<?> rawType) {
        return typeFactory.constructParametricType(rawType, Object.class);
    }
}