package com.brucechou.object.resolver;

/**
 * A null resolver that always return null.
 */
public class NullResolver implements Resolver {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Class<? extends T> resolve(Class<T> clazz) {
        return null;
    }

}
