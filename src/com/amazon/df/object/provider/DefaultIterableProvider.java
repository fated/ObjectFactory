package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Default Iterable provider, with a random size of iterable that iterate a series random objects.
 */
@AllArgsConstructor
public class DefaultIterableProvider implements Provider, WithRandomSize {

    private final ObjectFactory objectFactory;
    private final Random random;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            return (T) Collections.emptyList();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            int length = getRandomSize(objectFactory, random);

            List<?> collection = new ArrayList<>(length);

            Type component = parameterizedType.getActualTypeArguments()[0];
            for (int i = 0; i < length; ++i) {
                collection.add(objectFactory.generate(component, cycleDetector));
            }

            return (T) collection;
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
            // support only top level iterable interface,
            // other sub-interface will be covered by other providers
            return Iterable.class.equals(type);
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return Iterable.class.equals(rawType);
        }

        return false;
    }

}