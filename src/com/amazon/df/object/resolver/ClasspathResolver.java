package com.amazon.df.object.resolver;

import com.amazon.df.object.util.Inspector;

import org.reflections.Reflections;

public class ClasspathResolver implements Resolver {

    private final Reflections reflections;

    public ClasspathResolver() {
        this(ClassLoader.getSystemClassLoader());
    }

    public ClasspathResolver(ClassLoader classLoader) {
        reflections = new Reflections(classLoader);
    }

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
