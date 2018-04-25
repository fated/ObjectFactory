package com.amazon.df.object.cycle;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NullCycleTerminatorTest {

    @Test
    void terminate() {
        CycleTerminator terminator = new NullCycleTerminator();

        assertNull(terminator.terminate(null));
        assertTrue(terminator.canTerminate(null));
    }

}
