package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brucechou.object.ObjectCreationException;
import com.brucechou.object.resolver.Resolver;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

class DefaultCollectionProviderTest implements ProviderTestBase {

    private Resolver mockResolver = Mockito.mock(Resolver.class);
    private DefaultCollectionProvider provider =
            new DefaultCollectionProvider(getObjectFactoryBuilder()
                                                  .resolvers(mockResolver)
                                                  .build(),
                                          getRandomSupplier());

    @Test
    void get() {
        Type genericListType = new TypeToken<List>() {}.getType();

        List genericList = provider.get(genericListType);

        assertAll(() -> assertNotNull(genericList),
                  () -> assertTrue(genericList.isEmpty()),
                  () -> assertTrue(genericList instanceof ArrayList));

        Type genericLinkedListType = new TypeToken<LinkedList>() {}.getType();

        List genericLinkedList = provider.get(genericLinkedListType);

        assertAll(() -> assertNotNull(genericLinkedList),
                  () -> assertTrue(genericLinkedList.isEmpty()),
                  () -> assertTrue(genericLinkedList instanceof LinkedList));

        Type genericAbstractSetType = new TypeToken<AbstractSet>() {}.getType();

        Set genericAbstractSet = provider.get(genericAbstractSetType);

        assertAll(() -> assertNotNull(genericAbstractSet),
                  () -> assertTrue(genericAbstractSet.isEmpty()),
                  () -> assertTrue(genericAbstractSet instanceof HashSet));

        Type genericQueueType = new TypeToken<Queue>() {}.getType();

        Queue genericQueue = provider.get(genericQueueType);

        assertAll(() -> assertNotNull(genericQueue),
                  () -> assertTrue(genericQueue.isEmpty()),
                  () -> assertTrue(genericQueue instanceof ArrayDeque));

        Type genericCollectionType = new TypeToken<Collection>() {}.getType();

        Collection genericCollection = provider.get(genericCollectionType);

        assertAll(() -> assertNotNull(genericCollection),
                  () -> assertTrue(genericCollection.isEmpty()),
                  () -> assertTrue(genericCollection instanceof ArrayList));

        Type stringCollectionType = new TypeToken<Collection<String>>() {}.getType();

        Collection stringCollection = provider.get(stringCollectionType);

        assertAll(() -> assertNotNull(stringCollection),
                  () -> assertFalse(stringCollection.isEmpty()),
                  () -> assertTrue(stringCollection instanceof ArrayList));

        Mockito.doReturn(ConcreteCollection.class).when(mockResolver).resolve(UnknownCollection.class);

        Type genericUnknownCollectionType = new TypeToken<UnknownCollection>() {}.getType();

        UnknownCollection unknownCollection = provider.get(genericUnknownCollectionType);

        assertAll(() -> assertNotNull(unknownCollection),
                  () -> assertFalse(unknownCollection.isEmpty()),
                  () -> assertTrue(unknownCollection instanceof ConcreteCollection));

        Type genericUnknownCollection2Type = new TypeToken<UnknownCollection2>() {}.getType();

        assertThrows(IllegalArgumentException.class, () -> provider.get(genericUnknownCollection2Type));

        Type genericPrivateConstructorListType = new TypeToken<PrivateConstructorList>() {}.getType();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericPrivateConstructorListType));

        Type genericThrowsConstructorListType = new TypeToken<ThrowsConstructorList>() {}.getType();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericThrowsConstructorListType));

        Type parameterizedType = new TypeToken<List<String>>() {}.getType();

        List<String> stringList = provider.get(parameterizedType);

        assertAll(() -> assertNotNull(stringList),
                  () -> assertFalse(stringList.isEmpty()),
                  () -> assertTrue(stringList instanceof ArrayList));

        assertThrows(IllegalArgumentException.class, () -> provider.get(Mockito.mock(TypeVariable.class)));
    }

    interface UnknownCollection<E> extends Collection<E> {}

    interface UnknownCollection2<E> extends Collection<E> {}

    static class ConcreteCollection<E> implements UnknownCollection<E> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(E e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    private class PrivateConstructorList<E> extends ArrayList<E> {

        private PrivateConstructorList() {
            super();
        }

    }

    private class ThrowsConstructorList<E> extends ArrayList<E> {

        private ThrowsConstructorList() {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertFalse(provider.recognizes(String.class));
        assertFalse(provider.recognizes(Mockito.mock(WildcardType.class)));

        Type stringListType = new TypeToken<List<String>>() {}.getType();

        assertTrue(provider.recognizes(stringListType));

        Type genericListType = new TypeToken<List>() {}.getType();

        assertTrue(provider.recognizes(genericListType));

        Type genericArrayListType = new TypeToken<ArrayList>() {}.getType();

        assertTrue(provider.recognizes(genericArrayListType));
    }

}
