package com.brucechou.object.provider;

import com.brucechou.object.cycle.CycleDetector;

import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Default random string provider, generate random uuid.
 */
@NoArgsConstructor
public class RandomStringProvider implements Provider {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
        return (T) UUID.randomUUID().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        return String.class.equals(type);
    }

}
