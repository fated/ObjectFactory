package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class DefaultEnumProviderTest implements ProviderTestBase {

    private DefaultEnumProvider provider = new DefaultEnumProvider(getRandom());

    @Test
    void get() {
        assertNull(provider.get(EmptyTestEnum.class));
        assertNotNull(provider.get(TestEnum.class));
        assertNotNull(provider.get(TestEnum.class, null));
        assertTrue(Arrays.asList(TestEnum.values()).contains(provider.get(TestEnum.class)));

        assertThrows(RuntimeException.class, () -> provider.get(String.class));
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertFalse(provider.recognizes(String.class));
        assertTrue(provider.recognizes(TestEnum.class));
    }

}

enum TestEnum {
    A, B, C
}

enum EmptyTestEnum {
}
