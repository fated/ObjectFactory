package com.amazon.df.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ObjectCreationExceptionTest {

    @Test
    void testNewException() {
        ObjectCreationException exception = new ObjectCreationException("test");

        assertEquals("test", exception.getMessage());
        assertNull(exception.getCause());

        exception.withCause(new RuntimeException());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);

        exception = new ObjectCreationException("test", new RuntimeException());

        assertEquals("test", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException);

        exception = new ObjectCreationException("hello, %s", "world");

        assertEquals("hello, world", exception.getMessage());
        assertNull(exception.getCause());
    }

}
