package com.amazon.spicy.object.resolver;

public interface Resolver {

    <T> Class<? extends T> resolve(Class<T> clazz);

}
