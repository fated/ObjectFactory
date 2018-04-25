package com.amazon.df.object.util;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class Inspector {

    private Inspector() {}

    private static final Set<Class<?>> EXPLICIT_PRIMITIVES = new HashSet<>();

    static {
        EXPLICIT_PRIMITIVES.addAll(Arrays.asList(boolean.class, Boolean.class,
                                                 byte.class, Byte.class,
                                                 char.class, Character.class,
                                                 short.class, Short.class,
                                                 int.class, Integer.class,
                                                 long.class, Long.class,
                                                 float.class, Float.class,
                                                 double.class, Double.class,
                                                 String.class,
                                                 ByteBuffer.class,
                                                 BigDecimal.class,
                                                 BigInteger.class,
                                                 Date.class,
                                                 Object.class
        ));
    }

    public static boolean isExplicitPrimitive(Class<?> clazz) {
        return EXPLICIT_PRIMITIVES.contains(clazz);
    }

    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public static boolean isInterface(Class<?> clazz) {
        return Modifier.isInterface(clazz.getModifiers());
    }

    public static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    public static boolean isVolatile(Member member) {
        return Modifier.isVolatile(member.getModifiers());
    }

    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

}
