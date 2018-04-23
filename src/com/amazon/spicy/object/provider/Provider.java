package com.amazon.spicy.object.provider;

import java.lang.reflect.Type;

public interface Provider {

    <T> T get(Type type);

    boolean recognizes(Type type);

}
