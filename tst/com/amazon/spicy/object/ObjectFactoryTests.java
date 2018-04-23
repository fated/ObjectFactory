package com.amazon.spicy.object;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;

import com.amazon.spicy.object.provider.DeterministicProvider;
import com.amazon.spicy.object.provider.Provider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class ObjectFactoryTests {

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

    private ObjectFactory factory;

    @Before
    public void setup() {
        factory = new ObjectFactory.Builder()
            .maxMapEntries(1)
            .maxArrayLength(1)
            .maxCollectionLength(1)
            .providers((f,r) -> PROVIDER)
            .build();
    }

    @Test
    public void testPrimitiveStructGeneration() {
        PrimitiveStruct s = factory.generate(PrimitiveStruct.class);
        validatePrimitiveStruct(s);
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
}
