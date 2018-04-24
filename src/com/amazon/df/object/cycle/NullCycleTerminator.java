package com.amazon.df.object.cycle;

import com.amazon.df.object.cycle.CycleDetector.CycleNode;

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
