package com.brucechou.object.provider;

import com.brucechou.object.cycle.CycleDetector;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DeterministicProvider implements Provider {

    private final Map<Type, Object> mapping;

    public DeterministicProvider() {
        this(new HashMap<>());
    }

    public DeterministicProvider(Map<Type, Object> mapping) {
        this.mapping = mapping;
    }

    public void put(Type type, Object object) {
        mapping.put(type, object);
    }

    @Override
    public <T> T get(Type type) {
        return (T) mapping.get(type);
    }

    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        return get(type);
    }

    @Override
    public boolean recognizes(Type type) {
        return mapping.containsKey(type);
    }
}
