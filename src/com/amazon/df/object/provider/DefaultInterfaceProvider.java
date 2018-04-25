package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.proxy.Handler;
import com.amazon.df.object.util.Inspector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

@AllArgsConstructor
public class DefaultInterfaceProvider implements Provider, WithResolver {

    private final ObjectFactory objectFactory;

    @Override
    public <T> T get(Type type) {
        Class<?> clazz = (Class<?>) type;

        if (Inspector.isInterface(clazz)) {
            // Try to resolve concrete type first
            Class<?> concreteClazz = resolveConcreteType(objectFactory, clazz);

            if (concreteClazz == null) {
                return handleInterface(clazz);
            } else {
                return objectFactory.generate(concreteClazz);
            }
        }

        throw new IllegalStateException("Unable to create proxy for " + type);
    }

    @SuppressWarnings("unchecked")
    private <T> T handleInterface(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new Handler(objectFactory));
    }

    @Override
    public boolean recognizes(Type type) {
        return (type instanceof Class) && Inspector.isInterface((Class<?>) type);
    }

}
