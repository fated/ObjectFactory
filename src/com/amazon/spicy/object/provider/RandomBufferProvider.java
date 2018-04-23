package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class RandomBufferProvider implements Provider {

    private static final int MAXIMUM_BYTE_LENGTH = 256;

    // TODO add other buffer types
    private static final Map<Type, Function<Random, Object>> FUNCTIONS = new HashMap<>();

    static {
        FUNCTIONS.put(ByteBuffer.class, r -> {
            byte[] bytes = new byte[r.nextInt(MAXIMUM_BYTE_LENGTH)];
            r.nextBytes(bytes);
            return ByteBuffer.wrap(bytes);
        });
    }

    private final Random random;

    public RandomBufferProvider(Random random) {
        this.random = random;
    }

    @Override
    public <T> T get(Type type) {
        return (T) FUNCTIONS.get(type).apply(random);
    }

    @Override
    public boolean recognizes(Type type) {
        return FUNCTIONS.containsKey(type);
    }

}
