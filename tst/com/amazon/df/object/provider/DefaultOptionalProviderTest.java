package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.arsenal.reflect.TypeBuilder;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

class DefaultOptionalProviderTest implements ProviderTestBase {

    private DefaultOptionalProvider provider = new DefaultOptionalProvider(getObjectFactory());

    @Test
    void get() {
        Type genericOptionalType = TypeBuilder.newInstance(Optional.class)
                                              .build();

        Optional genericOptional1 = provider.get(genericOptionalType);

        assertAll(() -> assertNotNull(genericOptional1),
                  () -> assertFalse(genericOptional1.isPresent()),
                  () -> assertTrue(genericOptional1 instanceof Optional));

        ParameterizedType mockOptionalType = Mockito.mock(ParameterizedType.class);
        Mockito.doReturn(null).when(mockOptionalType).getActualTypeArguments();

        Optional genericOptional2 = provider.get(mockOptionalType);

        assertAll(() -> assertNotNull(genericOptional2),
                  () -> assertFalse(genericOptional2.isPresent()),
                  () -> assertTrue(genericOptional2 instanceof Optional));

        Mockito.doReturn(new Type[0]).when(mockOptionalType).getActualTypeArguments();

        Optional genericOptional3 = provider.get(mockOptionalType);

        assertAll(() -> assertNotNull(genericOptional3),
                  () -> assertFalse(genericOptional3.isPresent()),
                  () -> assertTrue(genericOptional3 instanceof Optional));

        Type stringOptionalType = TypeBuilder.newInstance(Optional.class)
                                             .addTypeParam(String.class)
                                             .build();

        Optional<String> stringOptional = provider.get(stringOptionalType);

        assertAll(() -> assertNotNull(stringOptional),
                  () -> assertTrue(stringOptional.isPresent()),
                  () -> assertTrue(stringOptional instanceof Optional));

        OptionalInt intOptional = provider.get(OptionalInt.class);

        assertAll(() -> assertNotNull(intOptional),
                  () -> assertTrue(intOptional.isPresent()),
                  () -> assertTrue(intOptional instanceof OptionalInt));

        OptionalLong longOptional = provider.get(OptionalLong.class);

        assertAll(() -> assertNotNull(longOptional),
                  () -> assertTrue(longOptional.isPresent()),
                  () -> assertTrue(longOptional instanceof OptionalLong));

        OptionalDouble doubleOptional = provider.get(OptionalDouble.class);

        assertAll(() -> assertNotNull(doubleOptional),
                  () -> assertTrue(doubleOptional.isPresent()),
                  () -> assertTrue(doubleOptional instanceof OptionalDouble));

        assertThrows(IllegalArgumentException.class, () -> provider.get(String.class));
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertTrue(provider.recognizes(OptionalInt.class));
        assertTrue(provider.recognizes(OptionalLong.class));
        assertTrue(provider.recognizes(OptionalDouble.class));

        Type stringType = TypeBuilder.newInstance(String.class).build();

        assertFalse(provider.recognizes(stringType));

        Type genericMapType = TypeBuilder.newInstance(Map.class)
                                         .build();

        assertFalse(provider.recognizes(genericMapType));

        Type stringListType = TypeBuilder.newInstance(List.class)
                                         .addTypeParam(String.class)
                                         .build();

        assertFalse(provider.recognizes(stringListType));

        Type genericOptionalType = TypeBuilder.newInstance(Optional.class)
                                              .build();

        assertTrue(provider.recognizes(genericOptionalType));

        Type stringOptionalType = TypeBuilder.newInstance(Optional.class)
                                             .addTypeParam(String.class)
                                             .build();

        assertTrue(provider.recognizes(stringOptionalType));
    }

}
