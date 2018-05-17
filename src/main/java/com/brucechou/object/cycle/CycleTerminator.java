package com.brucechou.object.cycle;

import com.brucechou.object.cycle.CycleDetector.CycleNode;

/**
 * An interface to check if cycle node can be terminated and terminate the cycle node.
 */
public interface CycleTerminator {

    /**
     * Terminates the given cycle, return type must match first and last node types.
     *
     * @param cycle cycle node detected
     * @param <T> the type of first and last node type
     * @return an instance of the same type of first and last node type, can be null.
     */
    <T> T terminate(CycleNode cycle);

    /**
     * Check the given cycle can be terminated or not.
     *
     * @param cycle cycle node detected
     * @return true if cycle node can be terminated, otherwise false
     */
    boolean canTerminate(CycleNode cycle);

}
