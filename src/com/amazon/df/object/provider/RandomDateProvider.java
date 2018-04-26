package com.amazon.df.object.provider;

import com.amazon.df.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Random;

@AllArgsConstructor
public class RandomDateProvider implements Provider {

    private static final long MILLIS_RANGE = 6342969599999L; //milliseconds between jan 1 1900 and Dec 31 2100
    private static final long FIRST_MILLIS = -2208988800000L; //first second of 1900

    private final Random random;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        long randomValue = random.nextLong();

        randomValue = randomValue == Long.MIN_VALUE ? 0 : Math.abs(randomValue);

        randomValue %= MILLIS_RANGE;

        Date d = new Date(FIRST_MILLIS + randomValue);

        return (T) d;
    }

    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
    }

    @Override
    public boolean recognizes(Type type) {
        return Date.class.equals(type);
    }

}
