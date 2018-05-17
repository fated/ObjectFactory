package com.brucechou.object.spy;

import com.brucechou.object.util.Inspector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

/**
 * An interface to find the constructor, methods or fields for class.
 */
public interface ClassSpy {

    /**
     * Default setter prefix, {@code setXXX}.
     */
    String DEFAULT_SETTER_PREFIX = "set";

    /**
     * Get setter prefix.
     *
     * @return setter prefix
     */
    default String getSetterPrefix() {
        return DEFAULT_SETTER_PREFIX;
    }

    /**
     * Get setter filter, used to filter setters to invoke.
     *
     * @return a field filter
     */
    default Predicate<Method> getSetterFilter() {
        return method -> method.getName().startsWith(getSetterPrefix()) && method.getParameterCount() == 1;
    }

    /**
     * Extract field name from setter name.
     *
     * @param setter setter method
     * @return extracted field name from setter
     */
    default String extractFieldNameFromSetter(Method setter) {
        String capitalizedFieldName = setter.getName().substring(getSetterPrefix().length());

        if (capitalizedFieldName.length() == 0) {
            return capitalizedFieldName;
        }

        final char firstChar = capitalizedFieldName.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            // already uncapitalized
            return capitalizedFieldName;
        }

        return Character.toLowerCase(firstChar) + capitalizedFieldName.substring(1);
    }

    /**
     * Get field filter, used to filter fields to set.
     *
     * @return a field filter
     */
    default Predicate<Field> getFieldFilter() {
        return field -> !Inspector.isVolatile(field) && !Inspector.isStatic(field) && !Inspector.isTransient(field);
    }

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
