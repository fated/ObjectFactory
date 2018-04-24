package com.amazon.df.object.provider;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
public class RandomStringProvider implements Provider {

    private static final int RANDOM_CHARS_LENGTH = 3;
    private static final int HIGH_CHAR_NO_SURROGATE = 0xD800;

    private final Random random;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        return (T) (new UUID(random.nextLong(), random.nextLong()).toString() + randomChars(RANDOM_CHARS_LENGTH));
    }

    @Override
    public boolean recognizes(Type type) {
        return String.class.equals(type);
    }

    private String randomChars(int length) {
        char[] sb = new char[length];
        for (int i = 0; i < length; ++i) {
            // Only strings without surrogates
            sb[i] = (char) random.nextInt(HIGH_CHAR_NO_SURROGATE);
        }
        return new String(sb);
    }

}
