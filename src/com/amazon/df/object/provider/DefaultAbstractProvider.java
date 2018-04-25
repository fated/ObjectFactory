package com.amazon.df.object.provider;

import static com.amazon.df.object.util.Throwables.sneakyThrow;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.proxy.Handler;
import com.amazon.df.object.util.Inspector;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

@AllArgsConstructor
public class DefaultAbstractProvider implements Provider, WithResolver {

    private final ObjectFactory objectFactory;

    @Override
    public <T> T get(Type type) {
        Class<?> clazz = (Class<?>) type;

        if (Inspector.isAbstract(clazz)) {
            // Try to resolve concrete type first
            Class<?> concreteClazz = resolveConcreteType(objectFactory, clazz);

            if (concreteClazz == null) {
                return handleAbstract(clazz);
            } else {
                return objectFactory.generate(concreteClazz);
            }
        }

        throw new IllegalStateException("Unable to create proxy for " + type);
    }

    @SuppressWarnings("unchecked")
    private <T> T handleAbstract(Class<?> clazz) {
        // Alternative solution is to use CGLib's Enhancer
        checkIfSupported(clazz);
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        Object object = newInstance(factory.createClass());

        ((Proxy) object).setHandler(new Handler(objectFactory));

        return (T) object;
    }

    private Object newInstance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * Checks if the abstract class is supported.
     * Currently only supports abstract class with no argument constructor.
     *
     * @param clazz the class to check.
     * @throws IllegalStateException if the class is not supported.
     */
    private void checkIfSupported(final Class<?> clazz) {
        final Constructor constructor = objectFactory.getClassSpy().findConstructor(clazz);
        if (constructor.getParameterCount() != 0) {
            throw new IllegalStateException(clazz + " doesn't have constructor with no arguments");
        }
    }

    @Override
    public boolean recognizes(Type type) {
        return (type instanceof Class) && Inspector.isAbstract((Class<?>) type);
    }

}