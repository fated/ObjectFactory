package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RandomPrimitiveProviderTest implements ProviderTestBase {

    private RandomPrimitiveProvider provider = new RandomPrimitiveProvider(getRandom());

    @Test
    void get() {
        assertNotNull(provider.get(boolean.class));
        assertNotNull(provider.get(byte.class));
        assertNotNull(provider.get(char.class));
        assertNotNull(provider.get(short.class));
        assertNotNull(provider.get(int.class));
        assertNotNull(provider.get(long.class));
        assertNotNull(provider.get(float.class));
        assertNotNull(provider.get(double.class));
        assertNotNull(provider.get(Boolean.class));
        assertNotNull(provider.get(Byte.class));
        assertNotNull(provider.get(Character.class));
        assertNotNull(provider.get(Short.class));
        assertNotNull(provider.get(Integer.class));
        assertNotNull(provider.get(Long.class));
        assertNotNull(provider.get(Float.class));
        assertNotNull(provider.get(Double.class));
        assertThrows(NullPointerException.class, () -> provider.get(String.class));
    }

    @Test
    void recognizes() {
        assertTrue(provider.recognizes(boolean.class));
        assertTrue(provider.recognizes(byte.class));
        assertTrue(provider.recognizes(char.class));
        assertTrue(provider.recognizes(short.class));
        assertTrue(provider.recognizes(int.class));
        assertTrue(provider.recognizes(long.class));
        assertTrue(provider.recognizes(float.class));
        assertTrue(provider.recognizes(double.class));
        assertTrue(provider.recognizes(Boolean.class));
        assertTrue(provider.recognizes(Byte.class));
        assertTrue(provider.recognizes(Character.class));
        assertTrue(provider.recognizes(Short.class));
        assertTrue(provider.recognizes(Integer.class));
        assertTrue(provider.recognizes(Long.class));
        assertTrue(provider.recognizes(Float.class));
        assertTrue(provider.recognizes(Double.class));
        assertFalse(provider.recognizes(String.class));
    }

}
