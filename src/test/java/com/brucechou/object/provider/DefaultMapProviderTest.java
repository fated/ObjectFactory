package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brucechou.object.ObjectCreationException;
import com.google.common.reflect.TypeToken;
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

    private DefaultMapProvider provider = new DefaultMapProvider(getObjectFactory(), getRandomSupplier());

    @Test
    void get() {
        Type genericMapType = new TypeToken<Map>() {}.getType();

        Map genericMap = provider.get(genericMapType);

        assertAll(() -> assertNotNull(genericMap),
                  () -> assertTrue(genericMap.isEmpty()),
                  () -> assertTrue(genericMap instanceof HashMap));

        Type genericLinkedHashMapType = new TypeToken<LinkedHashMap>() {}.getType();

        Map genericLinkedHashMap = provider.get(genericLinkedHashMapType);

        assertAll(() -> assertNotNull(genericLinkedHashMap),
                  () -> assertTrue(genericLinkedHashMap.isEmpty()),
                  () -> assertTrue(genericLinkedHashMap instanceof LinkedHashMap));

        Type genericAbstractMapType = new TypeToken<AbstractMap>() {}.getType();

        Map genericAbstractMap = provider.get(genericAbstractMapType);

        assertAll(() -> assertNotNull(genericAbstractMap),
                  () -> assertTrue(genericAbstractMap.isEmpty()),
                  () -> assertTrue(genericAbstractMap instanceof HashMap));

        Type genericUnknownMapType = new TypeToken<UnknownMap>() {}.getType();

        assertThrows(IllegalArgumentException.class, () -> provider.get(genericUnknownMapType));

        Type genericPrivateConstructorMapType = new TypeToken<PrivateConstructorMap>() {}.getType();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericPrivateConstructorMapType));

        Type genericThrowsConstructorMapType = new TypeToken<ThrowsConstructorMap>() {}.getType();

        assertThrows(ObjectCreationException.class, () -> provider.get(genericThrowsConstructorMapType));

        Type parameterizedType = new TypeToken<Map<String, String>>() {}.getType();

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

        Type stringMapType = new TypeToken<Map<String, String>>() {}.getType();

        assertTrue(provider.recognizes(stringMapType));

        Type genericMapType = new TypeToken<Map>() {}.getType();

        assertTrue(provider.recognizes(genericMapType));

        Type genericHashMapType = new TypeToken<HashMap>() {}.getType();

        assertTrue(provider.recognizes(genericHashMapType));
    }

}
