package com.brucechou.object.resolver;

import com.brucechou.object.util.Inspector;

import org.reflections.Reflections;

/**
 * A class path resolver leverage Reflections' API.
 */
public class ClasspathResolver implements Resolver {

    private final Reflections reflections;

    /**
     * Create a class path resolver with default class loader.
     */
    public ClasspathResolver() {
        this(ClassLoader.getSystemClassLoader());
    }

    /**
     * Create a class path resolver with specific class loader.
     *
     * @param classLoader class loader
     */
    public ClasspathResolver(ClassLoader classLoader) {
        reflections = new Reflections(classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Class<? extends T> resolve(Class<T> clazz) {
        for (Class<? extends T> type : reflections.getSubTypesOf(clazz)) {
            if (Inspector.isInterface(type)
                        || Inspector.isAbstract(type)
                        || type.isAnonymousClass()
                        || (type.isMemberClass() && !Inspector.isStatic(type))) {
                continue;
            }
            return type;
        }
        return null;
    }

}
