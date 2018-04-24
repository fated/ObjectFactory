package com.amazon.df.object.spy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

/**
 * Finds the constructor and methods for class.
 */
public interface ClassSpy {

    /**
     * Finds an arbitrary constructor in the class.
     * In the case of multiple constructor, the way to choose depends on implementation.
     *
     * @param clazz the class to check.
     * @param <T> a type of the class.
     * @return the constructor.
     */
    <T> Constructor<T> findConstructor(Class<T> clazz);

    /**
     * Finds all methods in the class, that satisfy the methodFilter.
     *
     * @param clazz the class to check.
     * @param methodFilter method filter for the methods
     * @return the methods.
     */
    List<Method> findMethods(Class<?> clazz, Predicate<Method> methodFilter);

    /**
     * Finds all fields in the class, that satisfy the fieldFilter.
     *
     * @param clazz the class to check.
     * @param fieldFilter field filter for the fields
     * @return the fields.
     */
    List<Field> findFields(Class<?> clazz, Predicate<Field> fieldFilter);

}
