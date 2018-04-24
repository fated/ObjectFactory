package com.amazon.df.object.spy;

import com.amazon.df.object.util.Inspector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

/**
 * Finds the constructor and methods for class.
 */
public interface ClassSpy {

    String DEFAULT_SETTER_PREFIX = "set";

    default String getSetterPrefix() {
        return DEFAULT_SETTER_PREFIX;
    }

    default Predicate<Method> getSetterFilter() {
        return method -> method.getName().startsWith(getSetterPrefix())
                                 && method.getParameterCount() == 1;
    }

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
