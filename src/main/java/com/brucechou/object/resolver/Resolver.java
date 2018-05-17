package com.brucechou.object.resolver;

/**
 * An interface to resolve given class.
 */
public interface Resolver {

    /**
     * Resolve concrete class for a given class.
     *
     * @param clazz interface or abstract class
     * @param <T> the type of interface or abstract class
     * @return the resolved concrete class
     */
    <T> Class<? extends T> resolve(Class<T> clazz);

}
