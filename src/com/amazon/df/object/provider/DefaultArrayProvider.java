package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;

import lombok.AllArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Random;

@AllArgsConstructor
public class DefaultArrayProvider implements Provider, WithRandomSize {

    private final ObjectFactory objectFactory;
    private final Random random;

    @Override
    public <T> T get(Type type) {
        if (type instanceof Class) {
            return (T) createArray(((Class<?>) type).getComponentType());
        }

        if (type instanceof GenericArrayType) {
            return (T) createArray(((GenericArrayType) type).getGenericComponentType());
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    private Object createArray(Type clazz) {
        Object array;
        int length = getRandomSize(objectFactory, random);
        if (clazz instanceof Class) {
            array = Array.newInstance((Class<?>) clazz, length);
        } else {
            array = Array.newInstance(Object.class, length);
        }

        for (int i = 0; i < length; ++i) {
            Array.set(array, i, objectFactory.generate(clazz));
        }

        return array;
    }

    @Override
    public boolean recognizes(Type type) {
        if (type == null) {
            return false;
        }

        if (type instanceof Class) {
            return ((Class<?>) type).isArray();
        }

        if (type instanceof GenericArrayType) {
            return true;
        }

        return false;
    }

}
