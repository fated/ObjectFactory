package com.amazon.spicy.object.binding;

import java.lang.reflect.Type;

import com.amazon.spicy.object.binding.Binding.FieldNameBinding;
import com.amazon.spicy.object.binding.Binding.FieldTypeBinding;
import com.amazon.spicy.object.binding.Binding.GlobalFieldTypeBinding;
import com.amazon.spicy.object.binding.Binding.GlobalFieldNameBinding;
import com.amazon.spicy.object.provider.Provider;

public abstract class Bindings {

    public static Binding bind(Type fieldType, Provider provider) {
        return new GlobalFieldTypeBinding(fieldType, provider);
    }

    public static Binding bind(String name, Provider provider) {
        return new GlobalFieldNameBinding( name, provider);
    }

    public static Binding bind(Type type, Type fieldType, Provider provider) {
        return new FieldTypeBinding(type, fieldType, provider);
    }

    public static Binding bind(Type type, String name, Provider provider) {
        return new FieldNameBinding(type, name, provider);
    }
}
