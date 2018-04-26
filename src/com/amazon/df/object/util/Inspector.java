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

/**
 * A inspector utility to inspect class, methods, fields.
 */
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

    /**
     * Check if a class is explicit primitive or not.
     *
     * @param clazz a class
     * @return true if class is explicit primitive, otherwise false
     */
    public static boolean isExplicitPrimitive(Class<?> clazz) {
        return EXPLICIT_PRIMITIVES.contains(clazz);
    }

    /**
     * Check if a class is abstract or not.
     *
     * @param clazz a class
     * @return true if class is abstract, otherwise false
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }
    /**
     * Check if a class is an interface or not. Notice that every interface is implicitly abstract.
     *
     * @param clazz a class
     * @return true if class is static, otherwise false
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.1.1.1">
     *      https://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.1.1.1</a>
     */
    public static boolean isInterface(Class<?> clazz) {
        return Modifier.isInterface(clazz.getModifiers());
    }

    /**
     * Check if a class is static or not.
     *
     * @param clazz a class
     * @return true if class is static, otherwise false
     */
    public static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    /**
     * Check if a field, method, constructor is static or not.
     *
     * @param member a class member
     * @return true if member is static, otherwise false
     */
    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    /**
     * Check if a field, method, constructor is transient or not.
     *
     * @param member a class member
     * @return true if member is transient, otherwise false
     */
    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    /**
     * Check if a field, method, constructor is volatile or not.
     *
     * @param member a class member
     * @return true if member is volatile, otherwise false
     */
    public static boolean isVolatile(Member member) {
        return Modifier.isVolatile(member.getModifiers());
    }

    /**
     * Check if a field, method, constructor is final or not.
     *
     * @param member a class member
     * @return true if member is final, otherwise false
     */
    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

}
