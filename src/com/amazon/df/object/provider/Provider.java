package com.amazon.df.object.provider;

import com.amazon.df.object.cycle.CycleDetector;

import java.lang.reflect.Type;

/**
 * A interface to recognize and provide value for a specific type.
 */
public interface Provider {

    /**
     * Process this type and return a value of this specific type.
     *
     * @param type given type
     * @param <T> the type represented by given type variable
     * @return value of given type
     */
    default <T> T get(Type type) {
        return get(type, new CycleDetector());
    }

    /**
     * Process this type and return a value of this specific type with a given cycle detector.
     *
     * @param type given type
     * @param cycleDetector dependency cycle detector
     * @param <T> the type represented by given type variable
     * @return value of given type
     */
    <T> T get(Type type, CycleDetector cycleDetector);

    /**
     * Check if a type can be recognized by this provider.
     *
     * @param type given type
     * @return true if given type can be processed by this provider, otherwise false
     */
    boolean recognizes(Type type);

}
