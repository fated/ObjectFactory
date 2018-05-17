package com.brucechou.object.provider;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Random;

/**
 * Default array provider, try to create array with random size, and fill it
 * with random generated objects.
 */
@AllArgsConstructor
public class DefaultArrayProvider implements Provider, WithRandomSize {

    private final ObjectFactory objectFactory;
    private final Random random;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            return (T) createArray(((Class<?>) type).getComponentType(), cycleDetector);
        }

        if (type instanceof GenericArrayType) {
            return (T) createArray(((GenericArrayType) type).getGenericComponentType(), cycleDetector);
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    /**
     * Create an array for specific type with cycle detector.
     *
     * @param type the component type of the array
     * @param cycleDetector dependency cycle detector
     * @return an array with random size with component objects filled
     */
    private Object createArray(Type type, CycleDetector cycleDetector) {
        Object array;
        int length = getRandomSize(objectFactory, random);
        if (type instanceof Class) {
            array = Array.newInstance((Class<?>) type, length);
        } else {
            array = Array.newInstance(Object.class, length);
        }

        for (int i = 0; i < length; ++i) {
            Array.set(array, i, objectFactory.generate(type, cycleDetector));
        }

        return array;
    }

    /**
     * {@inheritDoc}
     */
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
