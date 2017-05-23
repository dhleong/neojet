package io.neovim.java.event.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author dhleong
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type rawType;
    private final Type[] typeArgs;

    public ParameterizedTypeImpl(Type rawType, Type[] typeArgs) {
        this.rawType = rawType;
        this.typeArgs = typeArgs;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return typeArgs;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
