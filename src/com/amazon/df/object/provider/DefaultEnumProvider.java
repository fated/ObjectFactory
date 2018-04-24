package com.amazon.df.object.provider;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.Random;

@AllArgsConstructor
public class DefaultEnumProvider implements Provider {

    private final Random random;

    @Override
    public <T> T get(Type type) {
        Class<?> clazz = (Class<?>) type;

        final Object[] enums = clazz.getEnumConstants();

        return (T) (enums.length == 0 ? null : enums[random.nextInt(enums.length)]);
    }

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
