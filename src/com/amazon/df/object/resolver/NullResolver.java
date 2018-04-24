package com.amazon.df.object.resolver;

public class NullResolver implements Resolver {

    @Override
    public <T> Class<? extends T> resolve(Class<T> clazz) {
        return null;
    }

}
