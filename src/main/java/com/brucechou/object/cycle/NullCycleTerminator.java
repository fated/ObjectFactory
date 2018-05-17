package com.brucechou.object.cycle;

import com.brucechou.object.cycle.CycleDetector.CycleNode;

/**
 * A null cycle terminator which terminate all cycle node and return null as result.
 */
public class NullCycleTerminator implements CycleTerminator {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T terminate(CycleNode cycle) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canTerminate(CycleNode cycle) {
        return true;
    }

}
