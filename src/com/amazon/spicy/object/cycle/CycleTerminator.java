package com.amazon.spicy.object.cycle;


import com.amazon.spicy.object.cycle.CycleDetector.CycleNode;

public interface CycleTerminator {

    // terminates the given cycle, return type must match first and last node types
    <T> T terminate(CycleNode cycle);

    boolean canTerminate(CycleNode cycle);
}
