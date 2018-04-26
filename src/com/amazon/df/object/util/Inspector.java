package com.amazon.df.object.util;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A inspector utility to inspect class, methods, fields.
 */
public final class Inspector {

    private Inspector() {}

    private static final Map<Class<?>, Object> DEFAULT_EXPLICIT_PRIMITIVES_VALUES = new HashMap<>();

    static {
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(boolean.class, false);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Boolean.class, false);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(byte.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Byte.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(char.class, 'a');
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Character.class, 'a');
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(short.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Short.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(int.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Integer.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(long.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Long.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(float.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Float.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(double.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Double.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(String.class, "");
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(ByteBuffer.class, ByteBuffer.allocate(0));
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(BigInteger.class, BigInteger.ZERO);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(BigDecimal.class, BigDecimal.ZERO);
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Date.class, new Date());
        DEFAULT_EXPLICIT_PRIMITIVES_VALUES.put(Object.class, new Object());
    }

    /**
     * Check if a class is explicit primitive or not.
     *
     * @param clazz a class
     * @return true if class is explicit primitive, otherwise false
     */
    public static boolean isExplicitPrimitive(Class<?> clazz) {
        return DEFAULT_EXPLICIT_PRIMITIVES_VALUES.keySet().contains(clazz);
    }

    /**
     * Get default explicit primitive value for class.
     *
     * @param clazz explicit primitive class
     * @return default value configured in this class
     */
    public static Object getDefaultExplicitPrimitiveValue(Class<?> clazz) {
        return DEFAULT_EXPLICIT_PRIMITIVES_VALUES.get(clazz);
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
