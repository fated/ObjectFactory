package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.arsenal.reflect.TypeBuilder;
import com.amazon.df.object.ObjectCreationException;
import com.amazon.df.object.resolver.ClasspathResolver;

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

    private DefaultCollectionProvider provider =
            new DefaultCollectionProvider(getObjectFactoryBuilder()
                                                  .resolvers(new ClasspathResolver(ConcreteCollection.class.getClassLoader()))
                                                  .build(),
                                          getRandom());

    @Test
    void get() {
        Type genericListType = TypeBuilder.newInstance(List.class)
                                          .build();

        List genericList = provider.get(genericListType);

        assertAll(() -> assertNotNull(genericList),
                  () -> assertTrue(genericList.isEmpty()),
                  () -> assertTrue(genericList instanceof ArrayList));

        Type genericLinkedListType = TypeBuilder.newInstance(LinkedList.class)
                                                .build();

        List genericLinkedList = provider.get(genericLinkedListType);

        assertAll(() -> assertNotNull(genericLinkedList),
                  () -> assertTrue(genericLinkedList.isEmpty()),
                  () -> assertTrue(genericLinkedList instanceof LinkedList));

        Type genericAbstractSetType = TypeBuilder.newInstance(AbstractSet.class)
                                                 .build();

        Set genericAbstractSet = provider.get(genericAbstractSetType);

        assertAll(() -> assertNotNull(genericAbstractSet),
                  () -> assertTrue(genericAbstractSet.isEmpty()),
                  () -> assertTrue(genericAbstractSet instanceof HashSet));

        Type genericQueueType = TypeBuilder.newInstance(Queue.class)
                                           .build();

        Queue genericQueue = provider.get(genericQueueType);

        assertAll(() -> assertNotNull(genericQueue),
                  () -> assertTrue(genericQueue.isEmpty()),
                  () -> assertTrue(genericQueue instanceof ArrayDeque));

        Type genericUnknownCollectionType = TypeBuilder.newInstance(UnknownCollection.class)
                                                       .build();

        UnknownCollection unknownCollection = provider.get(genericUnknownCollectionType);

        assertAll(() -> assertNotNull(unknownCollection),
                  () -> assertFalse(unknownCollection.isEmpty()),
                  () -> assertTrue(unknownCollection instanceof ConcreteCollection));

        Type genericUnknownCollection2Type = TypeBuilder.newInstance(UnknownCollection2.class)
                                                        .build();

        assertThrows(IllegalArgumentException.class, () -> provider.get(genericUnknownCollection2Type));

        Type genericPrivateConstructorListType = TypeBuilder.newInstance(PrivateConstructorList.class)
                                                            .build();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericPrivateConstructorListType));

        Type genericThrowsConstructorListType = TypeBuilder.newInstance(ThrowsConstructorList.class)
                                                           .build();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericThrowsConstructorListType));

        Type parameterizedType = TypeBuilder.newInstance(List.class).addTypeParam(String.class).build();

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

        Type stringListType = TypeBuilder.newInstance(List.class)
                                         .addTypeParam(String.class)
                                         .build();

        assertTrue(provider.recognizes(stringListType));

        Type genericListType = TypeBuilder.newInstance(List.class)
                                          .build();

        assertTrue(provider.recognizes(genericListType));

        Type genericArrayListType = TypeBuilder.newInstance(ArrayList.class)
                                               .build();

        assertTrue(provider.recognizes(genericArrayListType));
    }

}
