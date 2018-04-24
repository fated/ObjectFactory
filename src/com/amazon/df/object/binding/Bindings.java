package com.amazon.df.object.binding;

import com.amazon.df.object.provider.Provider;

import java.lang.reflect.Type;

public abstract class Bindings {

    public static Binding bind(Type fieldType, Provider provider) {
        return new Binding.GlobalFieldTypeBinding(fieldType, provider);
    }

    public static Binding bind(String name, Provider provider) {
        return new Binding.GlobalFieldNameBinding(name, provider);
    }

    public static Binding bind(Type type, Type fieldType, Provider provider) {
        return new Binding.FieldTypeBinding(type, fieldType, provider);
    }

    public static Binding bind(Type type, String name, Provider provider) {
        return new Binding.FieldNameBinding(type, name, provider);
    }

}
