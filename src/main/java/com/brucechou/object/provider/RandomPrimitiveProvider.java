package com.brucechou.object.provider;

import com.brucechou.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default primitive provider, generate random values with given random instance.
 */
@AllArgsConstructor
public class RandomPrimitiveProvider implements Provider {

    private static final int PRINTABLE_ASCII_CHAR_SIZE = 95;
    private static final int PRINTABLE_ASCII_CHAR_START = 32;

    private static final Map<Type, Function<Random, Object>> FUNCTIONS = new HashMap<>();

    static {
        FUNCTIONS.put(boolean.class, r -> r.nextBoolean());
        FUNCTIONS.put(byte.class, r -> (byte) r.nextInt());
        FUNCTIONS.put(char.class, r -> (char) (r.nextInt(PRINTABLE_ASCII_CHAR_SIZE) + PRINTABLE_ASCII_CHAR_START));
        FUNCTIONS.put(short.class, r -> (short) r.nextInt());
        FUNCTIONS.put(int.class, r -> r.nextInt());
        FUNCTIONS.put(long.class, r -> r.nextLong());
        FUNCTIONS.put(float.class, r -> r.nextFloat());
        FUNCTIONS.put(double.class, r -> r.nextDouble());
        FUNCTIONS.put(Boolean.class, FUNCTIONS.get(boolean.class));
        FUNCTIONS.put(Byte.class, FUNCTIONS.get(byte.class));
        FUNCTIONS.put(Character.class, FUNCTIONS.get(char.class));
        FUNCTIONS.put(Short.class, FUNCTIONS.get(short.class));
        FUNCTIONS.put(Integer.class, FUNCTIONS.get(int.class));
        FUNCTIONS.put(Long.class, FUNCTIONS.get(long.class));
        FUNCTIONS.put(Float.class, FUNCTIONS.get(float.class));
        FUNCTIONS.put(Double.class, FUNCTIONS.get(double.class));
    }

    private final Supplier<Random> randomSupplier;

    /**
     * Get random primitive value.
     *
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        return (T) FUNCTIONS.get(type).apply(randomSupplier.get());
    }

    /**
     * Get random primitive value.
     *
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
    }

    /**
     * Recognize primitive type.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        return FUNCTIONS.containsKey(type);
    }

}
