package com.amazon.df.object.binding;

import com.amazon.df.object.provider.Provider;

import lombok.Getter;

import java.lang.reflect.Type;

public interface Binding {

    @Getter
    class FieldNameBinding implements Binding {

        private final Type type;
        private final String name;
        private final Provider provider;

        FieldNameBinding(Type type, String name, Provider provider) {
            this.type = type;
            this.name = name;
            this.provider = provider;
        }
    }

    @Getter
    class FieldTypeBinding implements Binding {

        private final Type container;
        private final Type fieldType;
        private final Provider provider;

        FieldTypeBinding(Type container, Type fieldType, Provider provider) {
            this.container = container;
            this.fieldType = fieldType;
            this.provider = provider;
        }
    }

    @Getter
    class GlobalFieldTypeBinding implements Binding {

        private final Type fieldType;
        private final Provider provider;

        GlobalFieldTypeBinding(Type fieldType, Provider provider) {
            this.fieldType = fieldType;
            this.provider = provider;
        }
    }

    @Getter
    class GlobalFieldNameBinding implements Binding {

        private final String fieldName;
        private final Provider provider;

        GlobalFieldNameBinding(String fieldName, Provider provider) {
            this.fieldName = fieldName;
            this.provider = provider;
        }
    }

}
