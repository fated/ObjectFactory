package com.amazon.df.object.provider;

import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.UUID;

@NoArgsConstructor
public class RandomStringProvider implements Provider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        return (T) UUID.randomUUID().toString();
    }

    @Override
    public boolean recognizes(Type type) {
        return String.class.equals(type);
    }

}
