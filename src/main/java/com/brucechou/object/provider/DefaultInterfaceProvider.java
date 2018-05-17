package com.brucechou.object.provider;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.cycle.CycleDetector;
import com.brucechou.object.proxy.Handler;
import com.brucechou.object.util.Inspector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

/**
 * Default interface handler, try to resolve concrete type and try to instantiate
 * it firstly, if not found, then it creates proxied object for the interface.
 */
@AllArgsConstructor
public class DefaultInterfaceProvider implements Provider, WithResolver {

    private final ObjectFactory objectFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        Class<?> clazz = (Class<?>) type;

        if (Inspector.isInterface(clazz)) {
            // Try to resolve concrete type first
            Class<?> concreteClazz = resolveConcreteType(objectFactory, clazz);

            if (concreteClazz == null) {
                return handleInterface(clazz);
            } else {
                return objectFactory.generate(concreteClazz, cycleDetector);
            }
        }

        throw new IllegalStateException("Unable to create proxy for " + type);
    }

    /**
     * Handle interface class, create proxy for the interface
     * with a method handler generates random return values.
     *
     * @param clazz the interface class
     * @param <T> the type of interface
     * @return a proxied object of interface class
     */
    @SuppressWarnings("unchecked")
    private <T> T handleInterface(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new Handler(objectFactory));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        return (type instanceof Class) && Inspector.isInterface((Class<?>) type);
    }

}
