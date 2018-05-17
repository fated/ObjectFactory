package com.brucechou.object.binding;

import com.brucechou.object.provider.Provider;

import lombok.Getter;

import java.lang.reflect.Type;

/**
 * Define a binding between field type, field name and provider.
 */
public interface Binding {

    /**
     * Define a local field name binding, works within a container.
     */
    @Getter
    class FieldNameBinding implements Binding {

        private final Type container;
        private final String fieldName;
        private final Provider provider;

        /**
         * Create a local field name binding.
         *
         * @param container container
         * @param fieldName field name
         * @param provider bound provider
         */
        FieldNameBinding(Type container, String fieldName, Provider provider) {
            this.container = container;
            this.fieldName = fieldName;
            this.provider = provider;
        }
    }

    /**
     * Define a local field type binding, works within a container.
     */
    @Getter
    class FieldTypeBinding implements Binding {

        private final Type container;
        private final Type fieldType;
        private final Provider provider;

        /**
         * Create a local field type binding.
         *
         * @param container container
         * @param fieldType field type
         * @param provider bound provider
         */
        FieldTypeBinding(Type container, Type fieldType, Provider provider) {
            this.container = container;
            this.fieldType = fieldType;
            this.provider = provider;
        }
    }

    /**
     * Define a global field type binding. This has the highest priority than normal provider.
     */
    @Getter
    class GlobalFieldTypeBinding implements Binding {

        private final Type fieldType;
        private final Provider provider;

        /**
         * Create a global field type binding.
         *
         * @param fieldType field type
         * @param provider bound provider
         */
        GlobalFieldTypeBinding(Type fieldType, Provider provider) {
            this.fieldType = fieldType;
            this.provider = provider;
        }
    }

    /**
     * Define a global field name binding.
     */
    @Getter
    class GlobalFieldNameBinding implements Binding {

        private final String fieldName;
        private final Provider provider;

        /**
         * Create a global field name binding.
         *
         * @param fieldName field name
         * @param provider provider
         */
        GlobalFieldNameBinding(String fieldName, Provider provider) {
            this.fieldName = fieldName;
            this.provider = provider;
        }
    }

}
