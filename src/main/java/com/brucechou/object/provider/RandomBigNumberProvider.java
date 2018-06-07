package com.brucechou.object.provider;

import com.brucechou.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default big number provider, generate big integer or big decimal with random bit length,
 * random scale, and random sign.
 */
@AllArgsConstructor
public class RandomBigNumberProvider implements Provider {

    private static final int MAXIMUM_BIT_LENGTH = 64;
    private static final Map<Type, Function<Random, Object>> FUNCTIONS = new HashMap<>();

    static {
        FUNCTIONS.put(BigInteger.class, r -> {
            BigInteger bi = new BigInteger(r.nextInt(MAXIMUM_BIT_LENGTH), r);
            if (r.nextBoolean()) {
                bi = bi.negate();
            }
            return bi;
        });
        FUNCTIONS.put(BigDecimal.class, r -> {
            int scale = r.nextInt(MAXIMUM_BIT_LENGTH);
            if (r.nextBoolean()) {
                scale = -scale;
            }
            BigInteger bi = (BigInteger) FUNCTIONS.get(BigInteger.class).apply(r);
            return new BigDecimal(bi, scale);
        });
    }

    private final Supplier<Random> randomSupplier;

    /**
     * Get random big number.
     *
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        return (T) FUNCTIONS.get(type).apply(randomSupplier.get());
    }

    /**
     * Get random big number.
     *
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
    }

    /**
     * Recognize big number type.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        return FUNCTIONS.containsKey(type);
    }

}
