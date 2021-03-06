package io.neovim.java.event;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.intellij.util.ParameterizedTypeImpl;
import io.neovim.java.rpc.NotificationListPacket;
import io.neovim.java.rpc.NotificationPacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;

/**
 * @author dhleong
 */
public final class EventsManager {

    static final Class<?>[] BUILT_IN_TYPES = {
        RedrawEvent.class
    };

    private final TypeFactory factory;
    final HashMap<Class<?>, String> eventNameCache = new HashMap<>();
    final HashMap<String, JavaType> eventValueClassCache = new HashMap<>();
    final HashMap<String, Class<?>> eventClassCache = new HashMap<>();

    public EventsManager() {
        factory = TypeFactory.defaultInstance();
        for (Class<?> klass : BUILT_IN_TYPES) {
            register(klass);
        }
    }

    public void register(Class<?> type) {
        getEventValueType(getEventName(type));
    }

    public String getEventName(Class<?> event) {
        String cached = eventNameCache.get(event);
        if (cached != null) return cached;

        EventName annot = event.getAnnotation(EventName.class);
        if (annot == null) {
            throw new IllegalArgumentException(
                event + " does not have an @EventName annotation");
        }

        String[] names = annot.value();
        for (String name : names) {
            eventNameCache.put(event, name);
            eventClassCache.put(name, event);
        }
        return names[0];
    }

    public @Nonnull Class<?> getEventType(String eventName) {
        Class<?> eventClass = eventClassCache.get(eventName);
        if (eventClass == null) {
            throw new IllegalArgumentException(
                "Unregistered event type: " + eventName
            );
        }
        return eventClass;
    }

    public @Nonnull JavaType getEventValueType(String eventName) {
        //noinspection ConstantConditions never returns null with allowNull: false
        return getEventValueType(eventName, false);
    }
    public @Nullable JavaType getEventValueType(String eventName, boolean allowNull) {
        JavaType cached = eventValueClassCache.get(eventName);
        if (cached != null) return cached;

        Class<?> eventClass = eventClassCache.get(eventName);
        if (eventClass == null) {
            if (allowNull) return null;

            throw new IllegalArgumentException(
                "Unregistered event type: " + eventName
            );
        }

        ParameterizedType packetType = getNotificationPacketType(eventClass);
        JavaType type = getJavaTypeForPacket(packetType);
        eventValueClassCache.put(eventName, type);
        return type;
    }

    @Nonnull JavaType getJavaTypeForPacket(ParameterizedType packetType) {
        Type eventValueType = packetType.getActualTypeArguments()[0];
        JavaType javaType = factory.constructType(eventValueType);
        if (packetType.getRawType() != NotificationListPacket.class) {
            return javaType;
        }

        // notification list packets are special since the the java type
        // is automatically a list of whatever was provided
        return factory.constructCollectionType(List.class, javaType);
    }

    static @Nonnull ParameterizedType getNotificationPacketType(Type base) {
        Type inputType = base;
        Type childType = null;
        do {
            if (base instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) base;
                if (parameterized.getRawType() == NotificationListPacket.class
                        || parameterized.getRawType() == NotificationPacket.class) {

                    if (childType instanceof ParameterizedType) {
                        Type typeVarValue = ((ParameterizedType) childType).getActualTypeArguments()[0];
                        return fillTypeVarWith(parameterized, typeVarValue);
                    }

                    return parameterized;
                }

                ParameterizedType fromInterface = checkInterfaces(base);
                if (fromInterface != null) return fillTypeVars(fromInterface, parameterized);

                childType = base;
                Class<?> asClass = ((Class<?>) ((ParameterizedType) base).getRawType());
                base = asClass.getGenericSuperclass();
            } else {
                ParameterizedType fromInterface = checkInterfaces(base);
                if (fromInterface != null) return fromInterface;

                childType = base;
                Class<?> asClass = (Class<?>) base;
                base = asClass.getGenericSuperclass();
            }
        } while (base != null && base != Object.class);

        throw new IllegalStateException(
            inputType + " does not extend NotificationPacket or Event");
    }

    /**
     * Given a type like <code>Notification&lt;BufferEvent&lt;T&gt;&gt;</code>,
     *  create a new ParameterizedType with that <code>T</code> filled in with the
     *  provided variableType
     *
     * @param type
     * @param variableType
     */
    private static ParameterizedType fillTypeVarWith(
            ParameterizedType type,
            Type variableType) {
        Type[] typeArgs = type.getActualTypeArguments();
        if (typeArgs[0] instanceof TypeVariable) {
            // NOTE this is very lazy and non-thorough...
            return new ParameterizedTypeImpl(
                type.getRawType(),
                variableType
            );
        }

        if (!(typeArgs[0] instanceof ParameterizedType)) {
            return type; // no change
        }

        return new ParameterizedTypeImpl(
            type,
            fillTypeVarWith((ParameterizedType) typeArgs[0], variableType)
        );
    }

    private static ParameterizedType fillTypeVars(
            ParameterizedType fromInterface,
            ParameterizedType base) {
        // NOTE: not thorough, but sufficient
        Type typeArg = (fromInterface.getActualTypeArguments())[0];
        if (!(typeArg instanceof ParameterizedType)) {
            // no change
            return fromInterface;
        }

        ParameterizedType paramTypeArg = (ParameterizedType) typeArg;
        Type[] nestedArgs = paramTypeArg.getActualTypeArguments();
        for (int i=0; i < nestedArgs.length; ++i) {
            if (nestedArgs[i] instanceof TypeVariable) {
                // NOTE: super non-rigorous, but should be sufficient for now
                nestedArgs[i] = base.getActualTypeArguments()[0];
                return new ParameterizedTypeImpl(
                    Event.class,
                    new ParameterizedTypeImpl(
                        paramTypeArg.getRawType(),
                        nestedArgs
                    )
                );
            }
        }

        // no type variables
        return fromInterface;
    }

    private static ParameterizedType checkInterfaces(Type originalType) {
        Class<?> asClass = (Class<?>) ((originalType instanceof Class<?>)
            ? originalType
            : ((ParameterizedType) originalType).getRawType());
        for (Type type : asClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                if (paramType.getRawType() == Event.class) {
                    if (!(paramType.getActualTypeArguments()[0] instanceof TypeVariable)) {
                        return paramType;
                    }

                    if (originalType instanceof ParameterizedType) {
                        // somewhat hacks
                        return (ParameterizedType) originalType;
                    }

                    throw new IllegalArgumentException(originalType + " implements Event, but couldn't determine kind");
                }
            }
        }

        return null;
    }
}
