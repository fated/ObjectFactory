package com.brucechou.object.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;
import com.brucechou.object.cycle.CycleDetector;
import com.brucechou.object.provider.DeterministicProvider;
import com.brucechou.object.provider.Provider;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Random;

final class BindingTests {

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
    void testGlobalBinding() {
        ObjectFactory factory = ObjectFactoryBuilder.getDefaultBuilder()
            .bindings(Bindings.bind(int.class, new Provider() {
                @Override
                public <T> T get(Type type, CycleDetector cycleDetector) {
                    return (T) new Integer(2);
                }

                @Override
                public boolean recognizes(Type type) {
                    return int.class.equals(type);
                }
            }))
            .providers()
            .random(new Random())
            .build();

        A a = factory.generate(A.class);
        assertEquals(2, a.aInt);
        assertEquals(2, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(2, a.b.aInt);
        assertEquals(2, a.b.bInt);

        assertThrows(IllegalArgumentException.class,
                     () -> ObjectFactoryBuilder.getDefaultBuilder()
                                               .bindings(Bindings.bind(int.class, new DeterministicProvider()),
                                                         Bindings.bind(int.class, new DeterministicProvider()))
                                               .build());
    }

    @Test
    void testFieldNameBinding() {
        ObjectFactory factory = ObjectFactoryBuilder.getDefaultBuilder()
            .bindings(Bindings.bind(A.class, "aInt", new Provider() {
                @Override
                public <T> T get(Type type, CycleDetector cycleDetector) {
                    return (T) new Integer(2);
                }

                @Override
                public boolean recognizes(Type type) {
                    return int.class.equals(type);
                }
            }))
            .providers()
            .random(new Random())
            .build();

        A a = factory.generate(A.class);
        assertEquals(2, a.aInt);
        assertEquals(0, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(0, a.b.aInt);
        assertEquals(0, a.b.bInt);

        assertThrows(IllegalArgumentException.class,
                     () -> ObjectFactoryBuilder.getDefaultBuilder()
                                               .bindings(Bindings.bind(A.class, "aInt", new DeterministicProvider()),
                                                         Bindings.bind(A.class, "aInt", new DeterministicProvider()))
                                               .build());
    }

    @Test
    void testFieldTypeBinding() {
        ObjectFactory factory = ObjectFactoryBuilder.getDefaultBuilder()
        .bindings(Bindings.bind(B.class, int.class, new Provider() {
            @Override
            public <T> T get(Type type, CycleDetector cycleDetector) {
                return (T) new Integer(2);
            }

            @Override
            public boolean recognizes(Type type) {
                return int.class.equals(type);
            }
        }))
        .providers()
        .random(new Random())
        .build();

        A a = factory.generate(A.class);
        assertEquals(0, a.aInt);
        assertEquals(0, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(2, a.b.aInt);
        assertEquals(2, a.b.bInt);

        assertThrows(IllegalArgumentException.class,
                     () -> ObjectFactoryBuilder.getDefaultBuilder()
                                               .bindings(Bindings.bind(B.class, int.class, new DeterministicProvider()),
                                                         Bindings.bind(B.class, int.class, new DeterministicProvider()))
                                               .build());
    }

    @Test
    void testGlobalFieldNameBinding() {
        ObjectFactory factory = ObjectFactoryBuilder.getDefaultBuilder()
        .bindings(Bindings.bind("aInt", new Provider() {
            @Override
            public <T> T get(Type type, CycleDetector cycleDetector) {
                return (T) new Integer(2);
            }

            @Override
            public boolean recognizes(Type type) {
                return int.class.equals(type);
            }
        }))
        .providers()
        .random(new Random())
        .build();

        A a = factory.generate(A.class);
        assertEquals(2, a.aInt);
        assertEquals(0, a.bInt);
        assertEquals(new Integer(0), a.cInteger);
        assertEquals(2, a.b.aInt);
        assertEquals(0, a.b.bInt);

        assertThrows(IllegalArgumentException.class,
                     () -> ObjectFactoryBuilder.getDefaultBuilder()
                                               .bindings(Bindings.bind("aInt", new DeterministicProvider()),
                                                         Bindings.bind("aInt", new DeterministicProvider()))
                                               .build());
    }

    @Test
    void testUnknownBinding() {
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectFactoryBuilder.getDefaultBuilder()
                                               .bindings(new Binding() {})
                                               .build());
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<?> constructor = Bindings.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

}
