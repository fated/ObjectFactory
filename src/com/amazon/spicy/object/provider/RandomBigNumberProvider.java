package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class RandomBigNumberProvider implements Provider {

    private static Map<Type, Function<Random, Object>> FUNCTIONS = new HashMap<>();
    static {
        FUNCTIONS.put(BigInteger.class, r -> {
            BigInteger bi = new BigInteger(r.nextInt(64), r);
            if (r.nextBoolean()) {
                bi = bi.negate();
            }
            return bi;
        });
        FUNCTIONS.put(BigDecimal.class, r -> {
            int scale = r.nextInt(64);
            if (r.nextBoolean()) {
                scale = -scale;
            }
            BigInteger bi = (BigInteger) FUNCTIONS.get(BigInteger.class).apply(r);
            return new BigDecimal(bi, scale);
        });
    }

    private final Random random;

    public RandomBigNumberProvider(Random random) {
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
