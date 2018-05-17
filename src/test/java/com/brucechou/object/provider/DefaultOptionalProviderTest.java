package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;
import com.google.common.reflect.TypeToken;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DefaultOptionalProviderTest implements ProviderTestBase {

    private DefaultOptionalProvider provider = new DefaultOptionalProvider(getObjectFactory());

    @Test
    void get() {
        Type genericOptionalType = new TypeToken<Optional>() {}.getType();

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

        Type stringOptionalType = new TypeToken<Optional<String>>() {}.getType();

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
    void testThreadSafe() throws ExecutionException, InterruptedException {
        ObjectFactory factory = ObjectFactoryBuilder.getDefaultBuilder().maxSize(1).build();

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Future<A> futureA1 = executor.submit(() -> factory.generate(A.class));
        Future<A> futureA2 = executor.submit(() -> factory.generate(A.class));
        Future<A> futureA3 = executor.submit(() -> factory.generate(A.class));
        Future<A> futureA4 = executor.submit(() -> factory.generate(A.class));

        A a1 = futureA1.get();
        assertNotNull(a1);
        assertNotNull(a1.b);
        assertNotNull(a1.b.a);
        assertFalse(a1.b.a.isPresent());

        A a2 = futureA2.get();
        assertNotNull(a2);
        assertNotNull(a2.b);
        assertNotNull(a2.b.a);
        assertFalse(a2.b.a.isPresent());

        A a3 = futureA3.get();

        assertNotNull(a3);
        assertNotNull(a3.b);
        assertNotNull(a3.b.a);
        assertFalse(a3.b.a.isPresent());

        A a4 = futureA4.get();
        assertNotNull(a4);
        assertNotNull(a4.b);
        assertNotNull(a4.b.a);
        assertFalse(a4.b.a.isPresent());
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertTrue(provider.recognizes(OptionalInt.class));
        assertTrue(provider.recognizes(OptionalLong.class));
        assertTrue(provider.recognizes(OptionalDouble.class));

        Type stringType = new TypeToken<String>() {}.getType();

        assertFalse(provider.recognizes(stringType));

        Type genericMapType = new TypeToken<Map>() {}.getType();

        assertFalse(provider.recognizes(genericMapType));

        Type stringListType = new TypeToken<List<String>>() {}.getType();

        assertFalse(provider.recognizes(stringListType));

        Type genericOptionalType = new TypeToken<Optional>() {}.getType();

        assertTrue(provider.recognizes(genericOptionalType));

        Type stringOptionalType = new TypeToken<Optional<String>>() {}.getType();

        assertTrue(provider.recognizes(stringOptionalType));
    }

}

class A {
    B b;
}
class B {
    Optional<A> a;
}
