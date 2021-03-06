package com.brucechou.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.brucechou.object.binding.Bindings;
import com.brucechou.object.cycle.CycleDetector;
import com.brucechou.object.cycle.CycleTerminator;
import com.brucechou.object.provider.DeterministicProvider;
import com.brucechou.object.provider.Provider;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("unused")
class ObjectFactoryTests {

    private static final double EPSILON = 1E-9;
    private static final Provider PROVIDER = new DeterministicProvider(new HashMap<Type, Object>() {{
        put(boolean.class, true);
        put(byte.class, new Byte("1"));
        put(char.class, 'a');
        put(short.class, new Short("1"));
        put(int.class, 1);
        put(long.class, new Long("1"));
        put(float.class, new Float("1"));
        put(double.class, new Double("1"));
        put(Boolean.class, true);
        put(Byte.class, new Byte("1"));
        put(Character.class, 'a');
        put(Short.class, new Short("1"));
        put(Integer.class, 1);
        put(Long.class, new Long("1"));
        put(Float.class, new Float("1"));
        put(Double.class, new Double("1"));
        put(BigInteger.class, BigInteger.ONE);
        put(BigDecimal.class, BigDecimal.ONE);
        put(ByteBuffer.class, ByteBuffer.allocate(0));
        put(String.class, "hello");
        put(Date.class, new Date(1));
    }});

    private static final class PrimitiveStruct {

        private boolean aBoolean;
        private byte aByte;
        private char aChar;
        private short aShort;
        private int anInt;
        private long aLong;
        private float aFloat;
        private double aDouble;
        private Boolean aBoxedBoolean;
        private Byte aBoxedByte;
        private Character aBoxedChar;
        private Short aBoxedShort;
        private Integer aBoxedInteger;
        private Long aBoxedLong;
        private Float aBoxedFloat;
        private Double aBoxedDouble;
        private BigInteger aBigInteger;
        private BigDecimal aBigDecimal;
        private ByteBuffer aByteBuffer;
        private String aString;
        private Date aDate;
    }

    private static final class MapStruct {
        // TODO
    }

    private static final class ArrayStruct {
        // TODO
    }

    private static ObjectFactory factory;

    @BeforeAll
    static void setup() {
        factory = ObjectFactoryBuilder.getDefaultBuilder()
                                      .maxSize(1)
                                      .providers((f, r) -> PROVIDER)
                                      .random(new Random())
                                      .build();
    }

    @Test
    void testPrimitiveStructGeneration() {
        PrimitiveStruct s = factory.generate(PrimitiveStruct.class);
        validatePrimitiveStruct(s);
    }

    @Test
    void testThreadSafe() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Future<D> futureD1 = executor.submit(() -> factory.generate(D.class));
        Future<D> futureD2 = executor.submit(() -> factory.generate(D.class));
        Future<D> futureD3 = executor.submit(() -> factory.generate(D.class));
        Future<D> futureD4 = executor.submit(() -> factory.generate(D.class));

        D d1 = futureD1.get();
        assertNotNull(d1);
        assertNotNull(d1.e);
        assertNull(d1.e.d);

        D d2 = futureD2.get();
        assertNotNull(d2);
        assertNotNull(d2.e);
        assertNull(d2.e.d);

        D d3 = futureD3.get();

        assertNotNull(d3);
        assertNotNull(d3.e);
        assertNull(d3.e.d);

        D d4 = futureD4.get();
        assertNotNull(d4);
        assertNotNull(d4.e);
        assertNull(d4.e.d);
    }

    @Test
    void testNoProviderThrows() {
        ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultBuilder()
                                                          .failOnMissingPrimitiveProvider(true)
                                                          .providers()
                                                          .random(new Random())
                                                          .build();

        assertThrows(ObjectCreationException.class, () -> objectFactory.generate(String.class));

        assertThrows(IllegalArgumentException.class,
                     () -> objectFactory.generate(new TypeToken<List<String>>() {}.getType()));
    }

    @Test
    void testGeneratePojo() {
        ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultObjectFactory(Random::new);

        A a = objectFactory.generate(A.class);

        assertNotNull(a.s);
        assertNotNull(a.sss);
        assertFalse(a.sss.isEmpty());
        assertNotNull(a.b);
        assertNotNull(a.b.bs);
        assertNotNull(a.b.c);
        assertNull(a.b.c.a);
    }

    @Test
    void testExceptionalCases() throws Exception {
        ObjectFactory objectFactory =
                ObjectFactoryBuilder.getDefaultBuilder()
                                    .bindings(Bindings.bind("something", new Provider() {
                                        @Override
                                        public <T> T get(Type type, CycleDetector cycleDetector) {
                                            throw new UnsupportedOperationException();
                                        }

                                        @Override
                                        public boolean recognizes(Type type) {
                                            return true;
                                        }
                                    }))
                                    .build();

        Field terminators = ObjectFactory.class.getDeclaredField("terminators");
        terminators.setAccessible(true);
        terminators.set(objectFactory, Collections.singletonList(new CycleTerminator() {
            @Override
            public <T> T terminate(CycleDetector.CycleNode cycle) {
                return null;
            }

            @Override
            public boolean canTerminate(CycleDetector.CycleNode cycle) {
                return false;
            }
        }));

        Method getTerminator = ObjectFactory.class.getDeclaredMethod("getTerminator", CycleDetector.CycleNode.class);
        getTerminator.setAccessible(true);

        assertThrows(IllegalStateException.class, () -> {
            try {
                getTerminator.invoke(objectFactory, (CycleDetector.CycleNode) null);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });

        Method newInstance = ObjectFactory.class.getDeclaredMethod("newInstance", Class.class, CycleDetector.class);
        newInstance.setAccessible(true);

        assertThrows(ObjectCreationException.class, () -> {
            try {
                newInstance.invoke(objectFactory, ClassThatThrows.class, null);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });

        Method populateFieldsBySetters = ObjectFactory.class.getDeclaredMethod("populateFieldsBySetters",
                                                                               Class.class, CycleDetector.class, Object.class);
        populateFieldsBySetters.setAccessible(true);

        try {
            populateFieldsBySetters.invoke(objectFactory, ClassThatThrows.class, null, new ClassThatThrows("test"));
        } catch (Exception e) {
            fail("should not throws");
        }

        Method populateFields = ObjectFactory.class.getDeclaredMethod("populateFields",
                                                                      Class.class, CycleDetector.class, Object.class, List.class);
        populateFields.setAccessible(true);

        assertThrows(ObjectCreationException.class, () -> {
            try {
                populateFields.invoke(objectFactory, ClassThatThrows.class, null, new ClassThatThrows("test"), new ArrayList<>());
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    //
    // Helpers
    //

    private void validatePrimitiveStruct(PrimitiveStruct s) {
        assertEquals(true, s.aBoolean);
        assertEquals(1, s.aByte);
        assertEquals('a', s.aChar);
        assertEquals(1, s.aShort);
        assertEquals(1, s.anInt);
        assertEquals(1, s.aLong);
        assertEquals(1, s.aFloat, EPSILON);
        assertEquals(1, s.aDouble, EPSILON);
        assertEquals(true, s.aBoxedBoolean);
        assertEquals(new Byte("1"), s.aBoxedByte);
        assertEquals(new Character('a'), s.aBoxedChar);
        assertEquals(new Short("1"), s.aBoxedShort);
        assertEquals(new Integer(1), s.aBoxedInteger);
        assertEquals(new Long("1"), s.aBoxedLong);
        assertEquals(new Float("1"), s.aBoxedFloat);
        assertEquals(new Double("1"), s.aBoxedDouble);
        assertEquals(BigInteger.ONE, s.aBigInteger);
        assertEquals(BigDecimal.ONE, s.aBigDecimal);
        assertNotNull(s.aByteBuffer);
        assertEquals("hello", s.aString);
        assertEquals(new Date(1), s.aDate);
    }

    class A {
        String s;
        List<String> sss;
        B b;
    }

    class B {
        String bs;
        C c;

        public void setBs(String bs) {
            this.bs = bs;
        }
    }

    class C {
        A a;
    }

    class D {
        E e;

        public void setSomething(String e) throws InterruptedException {
            // long run setter
            Thread.sleep(500L);
        }
    }

    class E {
        D d;
    }
}

class ClassThatThrows {

    private String something;

    public ClassThatThrows() {
        throw new UnsupportedOperationException();
    }

    public ClassThatThrows(String something) {}

    public void setSomething(String something) {
        throw new UnsupportedOperationException();
    }
}
