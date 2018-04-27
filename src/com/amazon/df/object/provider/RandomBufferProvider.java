package com.amazon.df.object.provider;

import com.amazon.df.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Default buffer provider, generate buffer filled with random values.
 */
@AllArgsConstructor
public class RandomBufferProvider implements Provider {

    private static final int MAXIMUM_BYTE_LENGTH = 256;

    private static final Map<Type, Function<Random, Object>> FUNCTIONS = new HashMap<>();

    static {
        FUNCTIONS.put(ByteBuffer.class, r -> {
            byte[] bytes = new byte[r.nextInt(MAXIMUM_BYTE_LENGTH)];
            r.nextBytes(bytes);
            return ByteBuffer.wrap(bytes);
        });
        FUNCTIONS.put(IntBuffer.class, r -> IntBuffer.wrap(r.ints(r.nextInt(MAXIMUM_BYTE_LENGTH)).toArray()));
        FUNCTIONS.put(LongBuffer.class, r -> LongBuffer.wrap(r.longs(r.nextInt(MAXIMUM_BYTE_LENGTH)).toArray()));
        FUNCTIONS.put(DoubleBuffer.class, r -> DoubleBuffer.wrap(r.doubles(r.nextInt(MAXIMUM_BYTE_LENGTH)).toArray()));
        FUNCTIONS.put(FloatBuffer.class, r -> {
            int length = r.nextInt(MAXIMUM_BYTE_LENGTH);
            float[] floats = new float[length];
            for (int i = 0; i < length; i++) {
                floats[i] = r.nextFloat();
            }
            return FloatBuffer.wrap(floats);
        });
        FUNCTIONS.put(ShortBuffer.class, r -> {
            int length = r.nextInt(MAXIMUM_BYTE_LENGTH);
            short[] shorts = new short[length];
            for (int i = 0; i < length; i++) {
                shorts[i] = (short) r.nextInt();
            }
            return ShortBuffer.wrap(shorts);
        });
        FUNCTIONS.put(CharBuffer.class, r -> {
            int length = r.nextInt(MAXIMUM_BYTE_LENGTH);
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = (char) r.nextInt();
            }
            return CharBuffer.wrap(chars);
        });
    }

    private final Random random;

    /**
     * Get random buffer.
     *
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        return (T) FUNCTIONS.get(type).apply(random);
    }

    /**
     * Get random buffer.
     *
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
    }

    /**
     * Recognize buffer type.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        return FUNCTIONS.containsKey(type);
    }

}
