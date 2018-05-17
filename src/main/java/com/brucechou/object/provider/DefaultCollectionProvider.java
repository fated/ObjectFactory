package com.brucechou.object.provider;

import com.brucechou.object.ObjectCreationException;
import com.brucechou.object.ObjectFactory;
import com.brucechou.object.cycle.CycleDetector;
import com.brucechou.object.util.Inspector;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Default collection provider, provide list, set, queue with random size and random elements.
 */
@AllArgsConstructor
public class DefaultCollectionProvider implements Provider, WithRandomSize, WithResolver {

    private final ObjectFactory objectFactory;
    private final Random random;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            return (T) createCollection((Class<Collection>) type, 0);
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> raw = (Class<?>) parameterizedType.getRawType();
            int length = getRandomSize(objectFactory, random);

            Collection<?> collection = createCollection(raw, length);

            Type component = parameterizedType.getActualTypeArguments()[0];
            for (int i = 0; i < length; ++i) {
                collection.add(objectFactory.generate(component, cycleDetector));
            }

            return (T) collection;
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    /**
     * Create an empty collection with specify capacity, if given class is interface or abstract,
     * it will try to use default concrete class or try to resolve the concrete class.
     *
     * @param clazz collection type to create
     * @param length the initial capacity for the collection
     * @return created empty collection
     * @throws IllegalArgumentException if type is unknown
     * @throws ObjectCreationException if fail to create object
     */
    private Collection<?> createCollection(Class<?> clazz, int length) {
        Class<?> concreteClazz = clazz;

        if (Inspector.isInterface(clazz) || Inspector.isAbstract(clazz)) {
            if (Set.class.isAssignableFrom(clazz)) {
                return new HashSet<>(length);
            } else if (List.class.isAssignableFrom(clazz)) {
                return new ArrayList<>(length);
            } else if (Queue.class.isAssignableFrom(clazz)) {
                return new ArrayDeque<>(length);
            } else {
                concreteClazz = resolveConcreteType(objectFactory, clazz);
                if (concreteClazz == null) {
                    throw new IllegalArgumentException("Unknown type: " + clazz);
                }
            }
        }

        try {
            return (Collection<?>) concreteClazz.newInstance();
        } catch (Exception e) {
            throw new ObjectCreationException("Fail to create new instance for type %s and concrete type %s",
                                              clazz, concreteClazz)
                          .withCause(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        if (type == null) {
            return false;
        }

        if (type instanceof Class) {
            return Collection.class.isAssignableFrom((Class<?>) type);
        }

        if (type instanceof ParameterizedType) {
            return Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType());
        }

        return false;
    }

}
