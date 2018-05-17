package com.brucechou.object.provider;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Random;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Default stream provider, with a random size of stream that generates a series random objects.
 */
@AllArgsConstructor
public class DefaultStreamProvider implements Provider, WithRandomSize {

    private final ObjectFactory objectFactory;
    private final Random random;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            if (IntStream.class.isAssignableFrom((Class<?>) type)) {
                return (T) random.ints(getRandomSize(objectFactory, random));
            } else if (LongStream.class.isAssignableFrom((Class<?>) type)) {
                return (T) random.longs(getRandomSize(objectFactory, random));
            } else if (DoubleStream.class.isAssignableFrom((Class<?>) type)) {
                return (T) random.doubles(getRandomSize(objectFactory, random));
            } else if (Stream.class.isAssignableFrom((Class<?>) type)) {
                return (T) Stream.empty();
            }
        }

        if (type instanceof ParameterizedType) {
            Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];

            return (T) Stream.generate(() -> objectFactory.generate(elementType, cycleDetector))
                             .limit(getRandomSize(objectFactory, random));
        }

        throw new IllegalArgumentException("Unknown type: " + type);
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
            return BaseStream.class.isAssignableFrom((Class<?>) type);
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return BaseStream.class.isAssignableFrom((Class<?>) rawType);
        }

        return false;
    }

}
