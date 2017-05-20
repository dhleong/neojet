package io.neovim.java.rpc.impl;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author dhleong
 */
public enum NotificationType {
    REDRAW(JsonNode.class);

    public final Class<?> valueType;

    static final HashMap<String, NotificationType> nameToType;
    static {
        NotificationType[] values = values();
        nameToType = new HashMap<>(values.length);

        for (NotificationType type : values) {
            nameToType.put(type.name().toLowerCase(), type);
        }
    }

    NotificationType(@Nonnull Class<?> valueType) {
        this.valueType = valueType;
    }

    @Nullable
    public static NotificationType fromString(@Nonnull String rpcName) {
        return nameToType.get(rpcName);
    }
}
