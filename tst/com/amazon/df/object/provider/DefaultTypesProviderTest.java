package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

class DefaultTypesProviderTest {

    private DefaultTypesProvider provider = new DefaultTypesProvider();

    @Test
    void get() {
        assertNull(provider.get(Mockito.mock(TypeVariable.class)));
        assertNull(provider.get(Mockito.mock(TypeVariable.class), null));
        assertNull(provider.get(Mockito.mock(WildcardType.class)));
        assertNull(provider.get(String.class));
    }

    @Test
    void recognizes() {
        assertTrue(provider.recognizes(Mockito.mock(TypeVariable.class)));
        assertTrue(provider.recognizes(Mockito.mock(WildcardType.class)));
        assertFalse(provider.recognizes(String.class));
    }

}
