package com.brucechou.object.provider;

import com.brucechou.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * Default enum provider, return random enum value from all available enums.
 */
@AllArgsConstructor
public class DefaultEnumProvider implements Provider {

    private final Random random;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        Class<?> clazz = (Class<?>) type;

        final Object[] enums = clazz.getEnumConstants();

        return (T) (enums.length == 0 ? null : enums[random.nextInt(enums.length)]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
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
            return ((Class<?>) type).isEnum();
        }

        return false;
    }

}
