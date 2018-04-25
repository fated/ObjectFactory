package com.amazon.df.object.resolver;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

class NullResolverTest {

    @Test
    void resolve() {
        assertNull(new NullResolver().resolve(List.class));
    }

}
