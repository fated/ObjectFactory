package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectCreationException;
import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.util.Inspector;

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

@AllArgsConstructor
public class DefaultCollectionProvider implements Provider, WithRandomSize {

    private final ObjectFactory objectFactory;
    private final Random random;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type) {
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
                collection.add(objectFactory.generate(component));
            }

            return (T) collection;
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    private Collection<?> createCollection(Class<?> clazz, int length) {
        Collection<?> collection;

        if (Inspector.isInterface(clazz) || Inspector.isAbstract(clazz)) {
            if (Set.class.isAssignableFrom(clazz)) {
                collection = new HashSet<>(length);
            } else if (List.class.isAssignableFrom(clazz)) {
                collection = new ArrayList<>(length);
            } else if (Queue.class.isAssignableFrom(clazz)) {
                collection = new ArrayDeque<>(length);
            } else {
                // TODO: add resolve concrete type
                throw new IllegalArgumentException("Unknown type: " + clazz);
            }
        } else {
            try {
                collection = (Collection<?>) clazz.newInstance();
            } catch (Exception e) {
                throw new ObjectCreationException("Fail to create new instance for type %s", clazz).withCause(e);
            }
        }

        return collection;
    }

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
