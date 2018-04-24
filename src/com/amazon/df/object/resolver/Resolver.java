package com.amazon.df.object.resolver;

public interface Resolver {

    <T> Class<? extends T> resolve(Class<T> clazz);

}
