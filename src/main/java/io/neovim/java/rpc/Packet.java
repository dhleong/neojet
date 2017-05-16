package io.neovim.java.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author dhleong
 */
@JsonFormat(shape=JsonFormat.Shape.ARRAY)
public abstract class Packet {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        return type == packet.type;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    public enum Type {
        REQUEST,
        RESPONSE,
        NOTIFICATION;

        static final Type[] VALUES = values();

        @JsonValue
        public int toValue() {
            return ordinal();
        }

        @JsonCreator
        public static Type create(int ordinal) {
            return VALUES[ordinal];
        }
    }

    public Type type;
}
