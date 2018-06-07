package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Iterator;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class DefaultStreamProviderTest implements ProviderTestBase {

    private DefaultStreamProvider provider = new DefaultStreamProvider(getObjectFactory(), getRandomSupplier());

    @Test
    void get() {
        Type genericStreamType = new TypeToken<Stream>() {}.getType();

        Stream genericStream = provider.get(genericStreamType);

        assertNotNull(genericStream);
        assertEquals(0, sizeOf(genericStream.iterator()));

        Type intStreamType = new TypeToken<IntStream>() {}.getType();

        IntStream intStream = provider.get(intStreamType);

        assertNotNull(intStream);
        assertEquals(DEFAULT_SIZE, sizeOf(intStream.iterator()));

        Type longStreamType = new TypeToken<LongStream>() {}.getType();

        LongStream longStream = provider.get(longStreamType);

        assertNotNull(longStream);
        assertEquals(DEFAULT_SIZE, sizeOf(longStream.iterator()));

        Type doubleStreamType = new TypeToken<DoubleStream>() {}.getType();

        DoubleStream doubleStream = provider.get(doubleStreamType);

        assertNotNull(doubleStream);
        assertEquals(DEFAULT_SIZE, sizeOf(doubleStream.iterator()));

        Type stringStreamType = new TypeToken<Stream<String>>() {}.getType();

        Stream<String> stringStream = provider.get(stringStreamType);

        assertNotNull(stringStream);
        assertEquals(DEFAULT_SIZE, sizeOf(stringStream.iterator()));

        Type otherStreamType = new TypeToken<OtherStream>() {}.getType();

        assertThrows(IllegalArgumentException.class, () -> provider.get(otherStreamType));

        Type wildcardType = Mockito.mock(WildcardType.class);

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

        Type streamType = new TypeToken<Stream>() {}.getType();

        assertTrue(provider.recognizes(streamType));

        Type intStreamType = new TypeToken<IntStream>() {}.getType();

        assertTrue(provider.recognizes(intStreamType));

        Type stringType = new TypeToken<String>() {}.getType();

        assertFalse(provider.recognizes(stringType));

        Type stringStreamType = new TypeToken<Stream<String>>() {}.getType();

        assertTrue(provider.recognizes(stringStreamType));

        Type stringListType = new TypeToken<List<String>>() {}.getType();

        assertFalse(provider.recognizes(stringListType));

        Type wildcardType = Mockito.mock(WildcardType.class);

        assertFalse(provider.recognizes(wildcardType));
    }

}
