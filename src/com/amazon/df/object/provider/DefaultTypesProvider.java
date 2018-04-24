package com.amazon.df.object.provider;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class DefaultTypesProvider implements Provider {

    @Override
    public <T> T get(Type type) {
        return null;
    }

    @Override
    public boolean recognizes(Type type) {
        return type instanceof TypeVariable || type instanceof WildcardType;
    }

}
