package com.amazon.df.object.spy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DefaultClassSpyTest {

    private ClassSpy spy = new DefaultClassSpy();

    @Test
    void findConstructor() {
        Constructor<ConcreteTestClass> constructor = spy.findConstructor(ConcreteTestClass.class);

        assertNotNull(constructor);
        assertEquals(0, constructor.getParameterCount());
    }

    @Test
    void findMethods() {
        List<Method> abstractSetters = spy.findMethods(AbstractTestClass.class, spy.getSetterFilter());

        assertFalse(abstractSetters.isEmpty());
        assertEquals(1, abstractSetters.size());
        assertEquals("setA", abstractSetters.get(0).getName());
        assertEquals("a", spy.extractFieldNameFromSetter(abstractSetters.get(0)));

        List<Method> concreteSetters = spy.findMethods(ConcreteTestClass.class, spy.getSetterFilter());

        assertFalse(concreteSetters.isEmpty());
        assertEquals(3, concreteSetters.size());

        Map<String, Method> methodNameMap =
                concreteSetters.stream().collect(Collectors.toMap(Method::getName, Function.identity()));

        assertTrue(methodNameMap.keySet().contains("setA"));
        assertEquals("a", spy.extractFieldNameFromSetter(methodNameMap.get("setA")));

        assertTrue(methodNameMap.keySet().contains("set"));
        assertEquals("", spy.extractFieldNameFromSetter(methodNameMap.get("set")));

        assertTrue(methodNameMap.keySet().contains("setc"));
        assertEquals("c", spy.extractFieldNameFromSetter(methodNameMap.get("setc")));
    }

    @Test
    void findFields() throws NoSuchFieldException {
        Field staticField = AbstractTestClass.class.getDeclaredField("staticField");
        Field volatileField = AbstractTestClass.class.getDeclaredField("volatileField");
        Field transientField = AbstractTestClass.class.getDeclaredField("transientField");
        Field finalField = AbstractTestClass.class.getDeclaredField("finalField");
        Field field = AbstractTestClass.class.getDeclaredField("field");

        Predicate<Field> fieldFilter = spy.getFieldFilter();

        assertTrue(fieldFilter.test(field));
        assertTrue(fieldFilter.test(finalField));
        assertFalse(fieldFilter.test(transientField));
        assertFalse(fieldFilter.test(volatileField));
        assertFalse(fieldFilter.test(staticField));

        List<Field> fields = spy.findFields(ConcreteTestClass.class, spy.getFieldFilter());

        assertFalse(fields.isEmpty());
        assertEquals(3, fields.size());
        Map<String, Field> fieldNameMap =
                fields.stream().collect(Collectors.toMap(Field::getName, Function.identity()));

        assertTrue(fieldNameMap.keySet().contains("cField"));
        assertTrue(fieldNameMap.keySet().contains("field"));
        assertTrue(fieldNameMap.keySet().contains("finalField"));
    }
}

abstract class AbstractTestClass {

    static int staticField;
    volatile int volatileField;
    transient int transientField;
    final int finalField = 0;
    int field;

    // setter
    public void setA(String a) {};

    // not setter
    public abstract void testA(String a);
}

class ConcreteTestClass extends AbstractTestClass {

    int cField;

    ConcreteTestClass() {}
    ConcreteTestClass(String a) {}
    ConcreteTestClass(String a, String c) {}

    // not setter
    @Override
    public void testA(String a) {}

    // setter
    public void set(String a) {}

    // setter
    public void setc(String c) {}

    // not setter
    public void setCc(String c1, String c2) {}

    // not setter
    public void testC() {}

}
