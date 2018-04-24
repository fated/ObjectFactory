package com.amazon.df.object.resolver;

import com.amazon.df.object.util.Inspector;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class ClasspathResolver implements Resolver {

    private static final List<Class<? extends Collection>> COLLECTIONS = Arrays.asList(
            ArrayList.class, LinkedList.class, Vector.class,
            HashSet.class, LinkedHashSet.class);

    private final Reflections reflections;

    public ClasspathResolver() {
        this(ClassLoader.getSystemClassLoader());
    }

    public ClasspathResolver(ClassLoader classLoader) {
        reflections = new Reflections(classLoader);
    }

    @Override
    public <T> Class<? extends T> resolve(Class<T> clazz) {
        if (List.class.equals(clazz)) {
            return (Class<? extends T>) ArrayList.class;
        }

        if (Collection.class.equals(clazz)) {
            return (Class<? extends T>) COLLECTIONS.get(new Random().nextInt(COLLECTIONS.size()));
        }

        if (Map.class.equals(clazz)) {
            return (Class<? extends T>) HashMap.class;
        }

        for (Class<? extends T> type : reflections.getSubTypesOf(clazz)) {
            if (Inspector.isAbstract(type) || Inspector.isInterface(type) || type.isAnonymousClass()) {
                continue;
            }
            if (type.isMemberClass() && (type.getModifiers() & Modifier.STATIC) == 0) {
                continue;
            }
            return type;
        }
        return null;
    }

}
