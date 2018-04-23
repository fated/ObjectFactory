package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Random;

public class RandomDateProvider implements Provider {

    private Random random;

    public RandomDateProvider(Random random) {
        this.random = random;
    }

    @Override
    public <T>  T get(Type type) {
        long randomValue = random.nextLong();

        randomValue = randomValue == Long.MIN_VALUE ? 0 : Math.abs(randomValue);

        randomValue %= 6342969599999L; //milliseconds between jan 1 1900 and Dec 31 2100

        Date d = new Date(-2208988800000L + randomValue); //first second of 1900

        return (T)d;
    }

    @Override
    public boolean recognizes(Type type) {
        return Date.class.equals(type);
    }
}
