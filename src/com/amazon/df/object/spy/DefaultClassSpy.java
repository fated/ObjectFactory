package com.amazon.df.object.spy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ClassSpy}.
 */
public final class DefaultClassSpy implements ClassSpy {

    /**
     * Finds the constructor with least number of parameters.
     *
     * {@inheritDoc}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Constructor<T> findConstructor(final Class<T> clazz) {
        return (Constructor<T>) Arrays.stream(clazz.getDeclaredConstructors())
                                      .min(Comparator.comparingInt(c -> c.getParameterTypes().length))
                                      .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Method> findMethods(final Class<?> clazz, final Predicate<Method> methodFilter) {
        // Get inherited methods too
        return Arrays.stream(clazz.getMethods())
                     .filter(methodFilter)
                     .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Field> findFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        Class<?> curClazz = clazz;
        List<Field> fields = new ArrayList<>();

        // Get inherited fields too
        while (curClazz != Object.class) {
            fields.addAll(Arrays.asList(curClazz.getDeclaredFields()));
            curClazz = curClazz.getSuperclass();
        }

        return fields.stream().filter(fieldFilter).collect(Collectors.toList());
    }

}
