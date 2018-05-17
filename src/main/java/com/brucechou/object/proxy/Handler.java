package com.brucechou.object.proxy;

import com.brucechou.object.ObjectFactory;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * Combined implementation of {@link java.lang.reflect.Proxy}'s {@link InvocationHandler}
 * and {@link javassist.util.proxy.Proxy}'s {@link MethodHandler}, that return random generated
 * values based on the return type.
 *
 * @see java.lang.reflect.Proxy
 * @see InvocationHandler
 * @see javassist.util.proxy.Proxy
 * @see MethodHandler
 */
@AllArgsConstructor
public class Handler implements InvocationHandler, MethodHandler {

    private static final Object[] NO_ARGS = {};

    private final ObjectFactory objectFactory;

    /**
     * Implementation of {@link java.lang.reflect.Proxy}'s {@link InvocationHandler}.
     *
     * {@inheritDoc}
     */
    @Override
    public final Object invoke(Object self, Method thisMethod, Object[] args) throws Throwable {
        return doInvoke(self, thisMethod, args,
            (s, a) -> isProxyOfSameInterfaces(a, s.getClass()) && equals(Proxy.getInvocationHandler(a)));
    }

    /**
     * Implementation of {@link javassist.util.proxy.Proxy}'s {@link MethodHandler}.
     *
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return doInvoke(self, thisMethod, args,
            (s, a) -> isProxyOfSameAbstract(a, s.getClass())
                              && equals(ProxyFactory.getHandler((javassist.util.proxy.Proxy) a)));
    }

    /**
     * Actually invoke the method.
     *
     * @param self the object itself
     * @param method method to invoke
     * @param args arguments to invoke
     * @param equals equals predicate
     * @return random generated return value
     */
    private Object doInvoke(Object self, Method method, Object[] args, BiPredicate<Object, Object> equals) {
        if (args == null) {
            args = NO_ARGS;
        }

        if (args.length == 0 && "hashCode".equals(method.getName())) {
            return hashCode();
        }

        if (args.length == 1 && "equals".equals(method.getName()) && method.getParameterTypes()[0] == Object.class) {
            Object arg = args[0];
            if (arg == null) {
                return false;
            }

            if (self == arg) {
                return true;
            }

            return equals.test(self, arg);
        }

        if (args.length == 0 && "toString".equals(method.getName())) {
            return toString();
        }

        return objectFactory.generate(method.getGenericReturnType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Check if an object is a proxy has all same interfaces compare to given proxy class or not.
     *
     * @param arg object to check
     * @param proxyClass given proxy class
     * @return true if given object is a proxy has all same interfaces as given proxy class, otherwise false
     */
    private static boolean isProxyOfSameInterfaces(Object arg, Class<?> proxyClass) {
        return proxyClass.isInstance(arg)
                       || (Proxy.isProxyClass(arg.getClass())
                                   && Arrays.equals(arg.getClass().getInterfaces(), proxyClass.getInterfaces()));
    }

    /**
     * Check if an object is a proxy has the same abstract class of given proxy class or not.
     *
     * @param arg object to check
     * @param proxyClass given proxy class
     * @return true if given object is a proxy has the same abstract class of given proxy class, otherwise false
     */
    private static boolean isProxyOfSameAbstract(Object arg, Class<?> proxyClass) {
        return proxyClass.isInstance(arg) || ProxyFactory.isProxyClass(arg.getClass());
    }

}
