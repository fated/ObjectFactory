package com.amazon.df.object.proxy;

import com.amazon.df.object.ObjectFactory;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.BiPredicate;

@AllArgsConstructor
public class Handler implements InvocationHandler, MethodHandler {

    private static final Object[] NO_ARGS = {};

    private final ObjectFactory objectFactory;

    @Override
    public final Object invoke(Object self, Method thisMethod, Object[] args) throws Throwable {
        return doInvoke(self, thisMethod, args,
            (s, a) -> isProxyOfSameInterfaces(a, s.getClass()) && equals(Proxy.getInvocationHandler(a)));
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return doInvoke(self, thisMethod, args,
            (s, a) -> isProxyOfSameAbstract(a, s.getClass())
                              && equals(ProxyFactory.getHandler((javassist.util.proxy.Proxy) a)));
    }

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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private static boolean isProxyOfSameInterfaces(Object arg, Class<?> proxyClass) {
        return proxyClass.isInstance(arg)
                       || (Proxy.isProxyClass(arg.getClass())
                                   && Arrays.equals(arg.getClass().getInterfaces(), proxyClass.getInterfaces()));
    }

    private static boolean isProxyOfSameAbstract(Object arg, Class<?> proxyClass) {
        return proxyClass.isInstance(arg) || ProxyFactory.isProxyClass(arg.getClass());
    }

}
