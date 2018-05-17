package com.brucechou.object.cycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brucechou.object.cycle.CycleDetector.CycleNode;

import org.junit.jupiter.api.Test;

class CycleDetectorTest {

    @Test
    void start() {
        // D -> D
        CycleDetector cycleDetector = new CycleDetector();

        CycleNode cn = cycleDetector.start(D.class);

        assertNull(cn);

        cn = cycleDetector.start(D.class);

        assertNotNull(cn);
        assertNull(cn.getNext());
        assertNull(cn.getPrevious());
        assertNotNull(cn.getType());
        assertEquals(D.class, cn.getType());

        assertEquals("com.brucechou.object.cycle.CycleDetectorTest$D", cn.toString());

        cycleDetector.end();

        // D -> C -> C
        cycleDetector = new CycleDetector();

        cycleDetector.start(D.class);
        cn = cycleDetector.start(C.class);

        assertNull(cn);

        cn = cycleDetector.start(C.class);

        assertNotNull(cn);
        assertNull(cn.getNext());
        assertNotNull(cn.getPrevious());

        assertEquals("com.brucechou.object.cycle.CycleDetectorTest$D -> com.brucechou.object.cycle.CycleDetectorTest$C",
                     cn.getPrevious().toString());

        assertEquals("com.brucechou.object.cycle.CycleDetectorTest$C", cn.toString());

        assertFalse(cn.equals(cn.getPrevious()));
        assertFalse(cn.equals(new Object()));
        assertTrue(cn.hashCode() > 0);

        cycleDetector.end();
        cycleDetector.end();
    }

    @Test
    void testRaceCondition() {
        CycleDetector cycleDetector = new CycleDetector();

        assertNull(cycleDetector.start(D.class));
        // Should be null, called from different callers on same type
        assertNotNull(cycleDetector.start(D.class));

        cycleDetector.end();
        assertNull(cycleDetector.start(D.class));
    }

    @Test
    void endBeforeStart() {
        assertThrows(IllegalStateException.class, new CycleDetector()::end);
    }

    private class C {}
    private class D {}

}
