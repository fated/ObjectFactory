package com.amazon.df.object.provider;

import com.amazon.df.object.cycle.CycleDetector;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Default {@link TypeVariable} and {@link WildcardType} provider, return null by default.
 */
public class DefaultTypesProvider implements Provider {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type) {
        return null;
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
        return type instanceof TypeVariable || type instanceof WildcardType;
    }

}
