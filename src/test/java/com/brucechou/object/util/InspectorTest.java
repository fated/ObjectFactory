package com.brucechou.object.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

class InspectorTest {

    @Test
    void testInspector() throws NoSuchFieldException {
        assertTrue(Inspector.isAbstract(AbstractClass.class));
        // Every interface is implicitly abstract.
        // https://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.1.1.1
        assertTrue(Inspector.isAbstract(InterfaceClass.class));
        assertFalse(Inspector.isAbstract(StaticClass.class));

        assertTrue(Inspector.isInterface(InterfaceClass.class));
        assertFalse(Inspector.isInterface(AbstractClass.class));
        assertFalse(Inspector.isInterface(StaticClass.class));

        assertTrue(Inspector.isStatic(StaticClass.class));
        assertFalse(Inspector.isStatic(AbstractClass.class));
        assertFalse(Inspector.isStatic(InterfaceClass.class));

        Field staticField = AbstractClass.class.getDeclaredField("staticField");

        assertTrue(Inspector.isStatic(staticField));
        assertFalse(Inspector.isTransient(staticField));
        assertFalse(Inspector.isVolatile(staticField));
        assertFalse(Inspector.isFinal(staticField));

        Field volatileField = AbstractClass.class.getDeclaredField("volatileField");

        assertTrue(Inspector.isVolatile(volatileField));
        assertFalse(Inspector.isStatic(volatileField));
        assertFalse(Inspector.isTransient(volatileField));
        assertFalse(Inspector.isFinal(volatileField));

        Field transientField = AbstractClass.class.getDeclaredField("transientField");

        assertTrue(Inspector.isTransient(transientField));
        assertFalse(Inspector.isStatic(transientField));
        assertFalse(Inspector.isVolatile(transientField));
        assertFalse(Inspector.isFinal(transientField));

        Field finalField = AbstractClass.class.getDeclaredField("finalField");

        assertTrue(Inspector.isFinal(finalField));
        assertFalse(Inspector.isStatic(finalField));
        assertFalse(Inspector.isTransient(finalField));
        assertFalse(Inspector.isVolatile(finalField));
    }

    @Test
    void testExplicitPrimitives() {
        assertTrue(Inspector.isExplicitPrimitive(boolean.class));
        assertEquals(false, Inspector.getDefaultExplicitPrimitiveValue(boolean.class));

        assertTrue(Inspector.isExplicitPrimitive(Boolean.class));
        assertEquals(false, Inspector.getDefaultExplicitPrimitiveValue(Boolean.class));

        assertTrue(Inspector.isExplicitPrimitive(byte.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(byte.class));

        assertTrue(Inspector.isExplicitPrimitive(Byte.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(Byte.class));

        assertTrue(Inspector.isExplicitPrimitive(char.class));
        assertEquals('a', Inspector.getDefaultExplicitPrimitiveValue(char.class));

        assertTrue(Inspector.isExplicitPrimitive(Character.class));
        assertEquals('a', Inspector.getDefaultExplicitPrimitiveValue(Character.class));

        assertTrue(Inspector.isExplicitPrimitive(short.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(short.class));

        assertTrue(Inspector.isExplicitPrimitive(Short.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(Short.class));

        assertTrue(Inspector.isExplicitPrimitive(int.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(int.class));

        assertTrue(Inspector.isExplicitPrimitive(Integer.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(Integer.class));

        assertTrue(Inspector.isExplicitPrimitive(long.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(long.class));

        assertTrue(Inspector.isExplicitPrimitive(Long.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(Long.class));

        assertTrue(Inspector.isExplicitPrimitive(float.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(float.class));

        assertTrue(Inspector.isExplicitPrimitive(Float.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(Float.class));

        assertTrue(Inspector.isExplicitPrimitive(double.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(double.class));

        assertTrue(Inspector.isExplicitPrimitive(Double.class));
        assertEquals(0, Inspector.getDefaultExplicitPrimitiveValue(Double.class));

        assertTrue(Inspector.isExplicitPrimitive(BigInteger.class));
        assertEquals(BigInteger.ZERO, Inspector.getDefaultExplicitPrimitiveValue(BigInteger.class));

        assertTrue(Inspector.isExplicitPrimitive(BigDecimal.class));
        assertEquals(BigDecimal.ZERO, Inspector.getDefaultExplicitPrimitiveValue(BigDecimal.class));

        assertTrue(Inspector.isExplicitPrimitive(String.class));
        assertEquals("", Inspector.getDefaultExplicitPrimitiveValue(String.class));

        assertTrue(Inspector.isExplicitPrimitive(ByteBuffer.class));
        assertEquals(ByteBuffer.allocate(0), Inspector.getDefaultExplicitPrimitiveValue(ByteBuffer.class));

        assertTrue(Inspector.isExplicitPrimitive(Date.class));
        assertNotNull(Inspector.getDefaultExplicitPrimitiveValue(Date.class));

        assertTrue(Inspector.isExplicitPrimitive(Object.class));
        assertNotNull(Inspector.getDefaultExplicitPrimitiveValue(Object.class));

        assertFalse(Inspector.isExplicitPrimitive(List.class));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<?> constructor = Inspector.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    static class StaticClass {}

}

abstract class AbstractClass {

    static int staticField;
    volatile int volatileField;
    transient int transientField;
    final int finalField = 0;

}

interface InterfaceClass {

    void test();
}
