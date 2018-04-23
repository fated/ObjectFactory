package com.amazon.spicy.object.binding;

import com.amazon.spicy.object.provider.Provider;

import java.lang.reflect.Type;

@SuppressWarnings("VisibilityModifier")
public interface Binding {

    class FieldNameBinding implements Binding {

        public final Type type;
        public final String name;
        public final Provider provider;

        FieldNameBinding(Type type, String name, Provider provider) {
            this.type = type;
            this.name = name;
            this.provider = provider;
        }
    }

    class FieldTypeBinding implements Binding {

        public final Type container;
        public final Type fieldType;
        public final Provider provider;

        FieldTypeBinding(Type container, Type fieldType, Provider provider) {
            this.container = container;
            this.fieldType = fieldType;
            this.provider = provider;
        }
    }

    class GlobalFieldTypeBinding implements Binding {

        public final Type fieldType;
        public final Provider provider;

        GlobalFieldTypeBinding(Type fieldType, Provider provider) {
            this.fieldType = fieldType;
            this.provider = provider;
        }
    }

    class GlobalFieldNameBinding implements Binding {

        public final String fieldName;
        public final Provider provider;

        GlobalFieldNameBinding(String fieldName, Provider provider) {
            this.fieldName = fieldName;
            this.provider = provider;
        }
    }
}
