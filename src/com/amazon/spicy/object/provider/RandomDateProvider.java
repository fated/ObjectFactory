package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Random;

public class RandomDateProvider implements Provider {

    private static final long MILLIS_RANGE = 6342969599999L; //milliseconds between jan 1 1900 and Dec 31 2100
    private static final long FIRST_MILLIS = -2208988800000L; //first second of 1900

    private Random random;

    public RandomDateProvider(Random random) {
        this.random = random;
    }

    @Override
    public <T> T get(Type type) {
        long randomValue = random.nextLong();

        randomValue = randomValue == Long.MIN_VALUE ? 0 : Math.abs(randomValue);

        randomValue %= MILLIS_RANGE;

        Date d = new Date(FIRST_MILLIS + randomValue);

        return (T) d;
    }

    @Override
    public boolean recognizes(Type type) {
        return Date.class.equals(type);
    }

}
