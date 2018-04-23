package com.amazon.spicy.object.cycle;

import com.amazon.spicy.object.cycle.CycleDetector.CycleNode;

public class NullCycleTerminator implements CycleTerminator {

    @Override
    public <T> T terminate(CycleNode cycle) {
        return null;
    }

    @Override
    public boolean canTerminate(CycleNode cycle) {
        return true;
    }
}
