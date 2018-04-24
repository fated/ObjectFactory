package com.amazon.df.object.binding;

import static org.junit.Assert.assertEquals;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.ObjectFactoryBuilder;
import com.amazon.df.object.provider.Provider;

import org.junit.Test;

import java.lang.reflect.Type;

public final class BindingTests {

    private static final class A {
        private int aInt;
        private int bInt;
        private Integer cInteger;
        private B b;
    }

    private static final class B {
        private int aInt;
        private int bInt;
    }

    @Test
    public void testGlobalBinding() {
        ObjectFactory factory = new ObjectFactoryBuilder()
            .failOnMissingPrimitiveProvider(false)
            .bindings(Bindings.bind(int.class, new Provider() {
                @Override
                public <T> T get(Type type) {
                    return (T) new Integer(2);
                }

                @Override
                public boolean recognizes(Type type) {
                    return int.class.equals(type);
                }
            }))
            .build();

        A a = factory.generate(A.class);
        assertEquals(2, a.aInt);
        assertEquals(2, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(2, a.b.aInt);
        assertEquals(2, a.b.bInt);
    }

    @Test
    public void testFieldNameBinding() {
        ObjectFactory factory = new ObjectFactoryBuilder()
            .failOnMissingPrimitiveProvider(false)
            .bindings(Bindings.bind(A.class, "aInt", new Provider() {
                @Override
                public <T> T get(Type type) {
                    return (T) new Integer(2);
                }

                @Override
                public boolean recognizes(Type type) {
                    return int.class.equals(type);
                }
            }))
            .build();

        A a = factory.generate(A.class);
        assertEquals(2, a.aInt);
        assertEquals(0, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(0, a.b.aInt);
        assertEquals(0, a.b.bInt);
    }

    @Test
    public void testFieldTypeBinding() {
        ObjectFactory factory = new ObjectFactoryBuilder()
        .failOnMissingPrimitiveProvider(false)
        .bindings(Bindings.bind(B.class, int.class, new Provider() {
            @Override
            public <T> T get(Type type) {
                return (T) new Integer(2);
            }

            @Override
            public boolean recognizes(Type type) {
                return int.class.equals(type);
            }
        }))
        .build();

        A a = factory.generate(A.class);
        assertEquals(0, a.aInt);
        assertEquals(0, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(2, a.b.aInt);
        assertEquals(2, a.b.bInt);
    }
}
