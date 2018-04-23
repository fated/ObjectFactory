package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;
import java.util.Random;
import java.util.UUID;

public class RandomStringProvider implements Provider {

    private final Random random;

    public RandomStringProvider(Random random) {
        this.random = random;
    }

    @Override
    public <T> T get(Type type) {
        return (T) (new UUID(random.nextLong(), random.nextLong()).toString() + randomChars(3));
    }

    @Override
    public boolean recognizes(Type type) {
        return String.class.equals(type);
    }

    private String randomChars(int length) {
        char[] sb = new char[length];
        for(int i = 0; i < length; ++i){
            // Only strings without surrogates
            sb[i] = (char)random.nextInt(0xD800);
        }
        return new String(sb);
    }
}
