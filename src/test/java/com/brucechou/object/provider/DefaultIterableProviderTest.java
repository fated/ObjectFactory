package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

class DefaultIterableProviderTest implements ProviderTestBase {

    private DefaultIterableProvider provider = new DefaultIterableProvider(getObjectFactory(), getRandom());

    @Test
    void get() {
        Type genericIterableType = new TypeToken<Iterable>() {}.getType();

        Iterable genericIterable = provider.get(genericIterableType);

        assertAll(() -> assertNotNull(genericIterable),
                  () -> assertFalse(genericIterable.iterator().hasNext()),
                  () -> assertTrue(genericIterable instanceof List));

        Type parameterizedType = new TypeToken<Iterable<String>>() {}.getType();

        Iterable<String> stringIterable = provider.get(parameterizedType);

        assertAll(() -> assertNotNull(stringIterable),
                  () -> assertTrue(stringIterable.iterator().hasNext()),
                  () -> assertTrue(stringIterable instanceof ArrayList));

        assertThrows(IllegalArgumentException.class, () -> provider.get(Mockito.mock(TypeVariable.class)));
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertFalse(provider.recognizes(String.class));
        assertFalse(provider.recognizes(Mockito.mock(WildcardType.class)));

        Type stringIterableType = new TypeToken<Iterable<String>>() {}.getType();

        assertTrue(provider.recognizes(stringIterableType));

        Type genericIterableType = new TypeToken<Iterable>() {}.getType();

        assertTrue(provider.recognizes(genericIterableType));

        Type genericListType = new TypeToken<List>() {}.getType();

        assertFalse(provider.recognizes(genericListType));
    }

}
