package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.arsenal.reflect.TypeBuilder;
import com.amazon.df.object.ObjectCreationException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class DefaultMapProviderTest implements ProviderTestBase {

    private DefaultMapProvider provider = new DefaultMapProvider(getObjectFactory(), getRandom());

    @Test
    void get() {
        Type genericMapType = TypeBuilder.newInstance(Map.class)
                                         .build();

        Map genericMap = provider.get(genericMapType);

        assertAll(() -> assertNotNull(genericMap),
                  () -> assertTrue(genericMap.isEmpty()),
                  () -> assertTrue(genericMap instanceof HashMap));

        Type genericLinkedHashMapType = TypeBuilder.newInstance(LinkedHashMap.class)
                                                   .build();

        Map genericLinkedHashMap = provider.get(genericLinkedHashMapType);

        assertAll(() -> assertNotNull(genericLinkedHashMap),
                  () -> assertTrue(genericLinkedHashMap.isEmpty()),
                  () -> assertTrue(genericLinkedHashMap instanceof LinkedHashMap));

        Type genericAbstractMapType = TypeBuilder.newInstance(AbstractMap.class)
                                                 .build();

        Map genericAbstractMap = provider.get(genericAbstractMapType);

        assertAll(() -> assertNotNull(genericAbstractMap),
                  () -> assertTrue(genericAbstractMap.isEmpty()),
                  () -> assertTrue(genericAbstractMap instanceof HashMap));

        Type genericUnknownMapType = TypeBuilder.newInstance(UnknownMap.class)
                                                .build();

        assertThrows(IllegalArgumentException.class, () -> provider.get(genericUnknownMapType));

        Type genericPrivateConstructorMapType = TypeBuilder.newInstance(PrivateConstructorMap.class)
                                                           .build();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericPrivateConstructorMapType));

        Type genericThrowsConstructorMapType = TypeBuilder.newInstance(ThrowsConstructorMap.class)
                                                          .build();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericThrowsConstructorMapType));

        Type parameterizedType = TypeBuilder.newInstance(Map.class)
                                            .addTypeParam(String.class)
                                            .addTypeParam(String.class)
                                            .build();

        Map<String, String> stringMap = provider.get(parameterizedType);

        assertAll(() -> assertNotNull(stringMap),
                  () -> assertFalse(stringMap.isEmpty()),
                  () -> assertTrue(stringMap instanceof HashMap));

        assertThrows(IllegalArgumentException.class, () -> provider.get(Mockito.mock(TypeVariable.class)));
    }

    interface UnknownMap<K, V> {}

    private class PrivateConstructorMap<K, V> extends HashMap<K, V> {

        private PrivateConstructorMap() {
            super();
        }

    }

    private class ThrowsConstructorMap<K, V> extends HashMap<K, V> {

        private ThrowsConstructorMap() {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertFalse(provider.recognizes(String.class));
        assertFalse(provider.recognizes(Mockito.mock(WildcardType.class)));

        Type stringMapType = TypeBuilder.newInstance(Map.class)
                                        .addTypeParam(String.class)
                                        .addTypeParam(String.class)
                                        .build();

        assertTrue(provider.recognizes(stringMapType));

        Type genericMapType = TypeBuilder.newInstance(Map.class)
                                         .build();

        assertTrue(provider.recognizes(genericMapType));

        Type genericHashMapType = TypeBuilder.newInstance(HashMap.class)
                                             .build();

        assertTrue(provider.recognizes(genericHashMapType));
    }

}
