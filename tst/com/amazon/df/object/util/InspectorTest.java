package com.amazon.df.object.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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

        assertTrue(Inspector.isExplicitPrimitive(boolean.class));
        assertFalse(Inspector.isExplicitPrimitive(List.class));
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
