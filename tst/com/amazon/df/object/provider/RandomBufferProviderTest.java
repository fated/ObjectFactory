package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

class RandomBufferProviderTest implements ProviderTestBase {

    private RandomBufferProvider provider = new RandomBufferProvider(getRandom());

    @Test
    void get() {
        assertNotNull(((ByteBuffer) provider.get(ByteBuffer.class)).get());
        assertNotNull(((CharBuffer) provider.get(CharBuffer.class)).get());
        assertNotNull(((ShortBuffer) provider.get(ShortBuffer.class)).get());
        assertNotNull(((IntBuffer) provider.get(IntBuffer.class)).get());
        assertNotNull(((LongBuffer) provider.get(LongBuffer.class)).get());
        assertNotNull(((FloatBuffer) provider.get(FloatBuffer.class)).get());
        assertNotNull(((DoubleBuffer) provider.get(DoubleBuffer.class)).get());

        assertThrows(NullPointerException.class, () -> provider.get(String.class));
    }

    @Test
    void recognizes() {
        assertTrue(provider.recognizes(ByteBuffer.class));
        assertTrue(provider.recognizes(CharBuffer.class));
        assertTrue(provider.recognizes(ShortBuffer.class));
        assertTrue(provider.recognizes(IntBuffer.class));
        assertTrue(provider.recognizes(LongBuffer.class));
        assertTrue(provider.recognizes(FloatBuffer.class));
        assertTrue(provider.recognizes(DoubleBuffer.class));
        assertFalse(provider.recognizes(String.class));
    }

}
