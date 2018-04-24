package com.amazon.df.object.cycle;

import com.amazon.df.object.cycle.CycleDetector.CycleNode;

public interface CycleTerminator {

    // terminates the given cycle, return type must match first and last node types
    <T> T terminate(CycleNode cycle);

    boolean canTerminate(CycleNode cycle);
}
