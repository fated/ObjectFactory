package com.brucechou.object.provider;

import com.brucechou.object.ObjectCreationException;
import com.brucechou.object.ObjectFactory;
import com.brucechou.object.cycle.CycleDetector;
import com.brucechou.object.proxy.Handler;
import com.brucechou.object.util.Inspector;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * Default abstract class provider, try to resolve concrete class for abstract first,
 * if not resolved, then create proxy class for the abstract class.
 */
@AllArgsConstructor
public class DefaultAbstractProvider implements Provider, WithResolver {

    private final ObjectFactory objectFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        Class<?> clazz = (Class<?>) type;

        if (Inspector.isAbstract(clazz)) {
            // Try to resolve concrete type first
            Class<?> concreteClazz = resolveConcreteType(objectFactory, clazz);

            if (concreteClazz == null) {
                return handleAbstract(clazz);
            } else {
                return objectFactory.generate(concreteClazz, cycleDetector);
            }
        }

        throw new IllegalStateException("Unable to create proxy for " + type);
    }

    /**
     * Handle abstract class, 1) check if abstract class has a no-arg constructor,
     * 2) create proxy for the abstract class, 3) set the method handler to generate
     * random object according to the return type.
     *
     * @param clazz an abstract class to handle
     * @param <T> the type of the abstract class
     * @return an proxied abstract with method handler returns random bean
     */
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

    /**
     * Simply create an instance with no-arg constructor for class.
     *
     * @param clazz class to create
     * @return an object of class
     * @throws ObjectCreationException if instance creation failed
     */
    private Object newInstance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ObjectCreationException("Fail to create new instance for type %s", clazz).withCause(e);
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
        // Inner abstract class will have resident class as parameter for the constructor
        // so this provider does not support proxying inner abstract class
        if (constructor == null || constructor.getParameterCount() != 0) {
            throw new IllegalStateException(clazz + " doesn't have constructor with no arguments");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        return (type instanceof Class) && Inspector.isAbstract((Class<?>) type);
    }

}
