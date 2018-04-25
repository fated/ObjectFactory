package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.arsenal.reflect.TypeBuilder;
import com.amazon.arsenal.reflect.impl.WildcardTypeImpl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class DefaultStreamProviderTest implements ProviderTestBase {

    private DefaultStreamProvider provider = new DefaultStreamProvider(getObjectFactory(), getRandom());

    @Test
    void get() {
        Type genericStreamType = TypeBuilder.newInstance(Stream.class)
                                            .build();

        Stream genericStream = provider.get(genericStreamType);

        assertNotNull(genericStream);
        assertEquals(0, sizeOf(genericStream.iterator()));

        Type intStreamType = TypeBuilder.newInstance(IntStream.class)
                                        .build();

        IntStream intStream = provider.get(intStreamType);

        assertNotNull(intStream);
        assertEquals(DEFAULT_SIZE, sizeOf(intStream.iterator()));

        Type longStreamType = TypeBuilder.newInstance(LongStream.class)
                                         .build();

        LongStream longStream = provider.get(longStreamType);

        assertNotNull(longStream);
        assertEquals(DEFAULT_SIZE, sizeOf(longStream.iterator()));

        Type doubleStreamType = TypeBuilder.newInstance(DoubleStream.class)
                                           .build();

        DoubleStream doubleStream = provider.get(doubleStreamType);

        assertNotNull(doubleStream);
        assertEquals(DEFAULT_SIZE, sizeOf(doubleStream.iterator()));

        Type stringStreamType = TypeBuilder.newInstance(Stream.class)
                                           .addTypeParam(String.class)
                                           .build();

        Stream<String> stringStream = provider.get(stringStreamType);

        assertNotNull(stringStream);
        assertEquals(DEFAULT_SIZE, sizeOf(stringStream.iterator()));

        Type otherStreamType = TypeBuilder.newInstance(OtherStream.class)
                                          .build();

        assertThrows(IllegalArgumentException.class, () -> provider.get(otherStreamType));

        Type wildcardType = new WildcardTypeImpl(new Type[0], new Type[0]);

        assertThrows(IllegalArgumentException.class, () -> provider.get(wildcardType));
    }

    private abstract class OtherStream<T> implements BaseStream<T, OtherStream<T>> {}

    private <E> int sizeOf(Iterator<E> iterator) {
        int size = 0;
        while (iterator != null && iterator.hasNext()) {
            ++size;
            iterator.next();
        }
        return size;
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));

        Type streamType = TypeBuilder.newInstance(Stream.class).build();

        assertTrue(provider.recognizes(streamType));

        Type intStreamType = TypeBuilder.newInstance(IntStream.class).build();

        assertTrue(provider.recognizes(intStreamType));

        Type stringType = TypeBuilder.newInstance(String.class)
                                     .build();

        assertFalse(provider.recognizes(stringType));

        Type stringStreamType = TypeBuilder.newInstance(Stream.class)
                                           .addTypeParam(String.class)
                                           .build();

        assertTrue(provider.recognizes(stringStreamType));

        Type stringListType = TypeBuilder.newInstance(List.class)
                                         .addTypeParam(String.class)
                                         .build();

        assertFalse(provider.recognizes(stringListType));

        Type wildcardType = new WildcardTypeImpl(new Type[0], new Type[0]);

        assertFalse(provider.recognizes(wildcardType));
    }

}
