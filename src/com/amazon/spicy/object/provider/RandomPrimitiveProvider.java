package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class RandomPrimitiveProvider implements Provider {

    private static Map<Type, Function<Random, Object>> FUNCTIONS = new HashMap<>();
    static {
        FUNCTIONS.put(boolean.class, r -> r.nextBoolean());
        FUNCTIONS.put(byte.class, r -> (byte) r.nextInt());
        FUNCTIONS.put(char.class, r -> (char) (r.nextInt(95) + 32));
        FUNCTIONS.put(short.class, r -> (short) r.nextInt());
        FUNCTIONS.put(int.class, r -> r.nextInt());
        FUNCTIONS.put(long.class, r -> Double.doubleToLongBits(r.nextDouble()));
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

    private final Random random;

    public RandomPrimitiveProvider(Random random) {
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
