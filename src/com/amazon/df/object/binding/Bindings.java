package com.amazon.df.object.binding;

import com.amazon.df.object.provider.Provider;

import java.lang.reflect.Type;

public final class Bindings {

    private Bindings() {}

    /**
     * Create a global field type binding.
     *
     * @param fieldType field type
     * @param provider bound provider
     * @return a global field type binding
     */
    public static Binding bind(Type fieldType, Provider provider) {
        return new Binding.GlobalFieldTypeBinding(fieldType, provider);
    }

    /**
     * Create a global field name binding.
     *
     * @param fieldName field name
     * @param provider bound provider
     * @return a global field name binding
     */
    public static Binding bind(String fieldName, Provider provider) {
        return new Binding.GlobalFieldNameBinding(fieldName, provider);
    }

    /**
     * Create a local field type binding.
     *
     * @param container container
     * @param fieldType field type
     * @param provider bound provider
     * @return a local field type binding
     */
    public static Binding bind(Type container, Type fieldType, Provider provider) {
        return new Binding.FieldTypeBinding(container, fieldType, provider);
    }

    /**
     * Create a local field name binding.
     *
     * @param container container
     * @param fieldName field name
     * @param provider bound provider
     * @return a local field name binding
     */
    public static Binding bind(Type container, String fieldName, Provider provider) {
        return new Binding.FieldNameBinding(container, fieldName, provider);
    }

}
