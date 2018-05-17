package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class DefaultArrayProviderTest implements ProviderTestBase {

    private DefaultArrayProvider provider = new DefaultArrayProvider(getObjectFactory(), getRandom());

    @Test
    void testGet() throws Exception {
        Integer[] ints = provider.get(Integer[].class);
        assertNotNull(ints);
        Arrays.stream(ints).forEach(Assertions::assertNotNull);

        Object[] strings = provider.get((GenericArrayType) () -> new TypeToken<Optional<String>>() {}.getType());
        assertNotNull(strings);
        Arrays.stream(strings).forEach(actual -> assertAll(() -> assertNotNull(actual),
                                                           () -> assertTrue(actual instanceof Optional),
                                                           () -> assertTrue(((Optional) actual).isPresent())));

        assertThrows(IllegalArgumentException.class,
                     () -> provider.get(new TypeToken<List<String>>() {}.getType()));
    }

    @Test
    void testRecognizes() throws Exception {
        assertFalse(provider.recognizes(null));
        assertTrue(provider.recognizes(Integer[].class));
        assertTrue(provider.recognizes((GenericArrayType) new TypeToken<String>() {}::getType));
        assertFalse(provider.recognizes(new TypeToken<List<String>>() {}.getType()));
    }

}
