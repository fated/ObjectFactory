package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectCreationException;
import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.cycle.CycleDetector;
import com.amazon.df.object.util.Inspector;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Default map provider, generate map with random keys and values.
 */
@AllArgsConstructor
public class DefaultMapProvider implements Provider, WithRandomSize {

    private final ObjectFactory objectFactory;
    private final Random random;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            return (T) createMap((Class<Map>) type, 0);
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> raw = (Class<?>) parameterizedType.getRawType();
            int entries = getRandomSize(objectFactory, random);

            Map<?, ?> map = createMap(raw, entries);

            Type key = parameterizedType.getActualTypeArguments()[0];
            Type value = parameterizedType.getActualTypeArguments()[1];

            for (int i = 0; i < entries; ++i) {
                map.put(objectFactory.generate(key, cycleDetector), objectFactory.generate(value, cycleDetector));
            }

            return (T) map;
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    /**
     * Create map of given type with specific capacity, if given map type is interface or abstract,
     * use the default {@link HashMap} as the concrete type.
     *
     * @param clazz the map type
     * @param length the capacity of the map
     * @return a created empty map
     */
    private Map<?, ?> createMap(Class<?> clazz, int length) {
        Map<?, ?> map;

        if (Inspector.isInterface(clazz) || Inspector.isAbstract(clazz)) {
            if (Map.class.isAssignableFrom(clazz)) {
                map = new HashMap<>(length);
            } else {
                throw new IllegalArgumentException("Unknown type: " + clazz);
            }
        } else {
            try {
                map = (Map<?, ?>) clazz.newInstance();
            } catch (Exception e) {
                throw new ObjectCreationException("Fail to create new instance for type %s", clazz).withCause(e);
            }
        }

        return map;
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
            return Map.class.isAssignableFrom((Class<?>) type);
        }

        if (type instanceof ParameterizedType) {
            return Map.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType());
        }

        return false;
    }

}
