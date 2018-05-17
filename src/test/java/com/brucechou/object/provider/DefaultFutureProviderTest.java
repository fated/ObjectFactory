package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveTask;

class DefaultFutureProviderTest implements ProviderTestBase {

    private DefaultFutureProvider provider = new DefaultFutureProvider(getObjectFactory());

    @Test
    void get() {
        Type futureType = new TypeToken<Future>() {}.getType();

        Future future = provider.get(futureType);

        assertAll(() -> assertNotNull(future),
                  () -> assertTrue(future instanceof CompletableFuture),
                  () -> assertTrue(future.isDone()),
                  () -> assertNotNull(future.get()));

        Type cFutureType = new TypeToken<CompletableFuture>() {}.getType();

        CompletableFuture cFuture = provider.get(cFutureType);

        assertAll(() -> assertNotNull(cFuture),
                  () -> assertTrue(cFuture.isDone()),
                  () -> assertNotNull(cFuture.get()));

        Type futureTaskType = new TypeToken<FutureTask>() {}.getType();

        FutureTask futureTask = provider.get(futureTaskType);

        assertAll(() -> assertNotNull(futureTask),
                  () -> assertTrue(futureTask.isDone()),
                  () -> assertNotNull(futureTask.get()));

        Type forkJoinTaskType = new TypeToken<ForkJoinTask>() {}.getType();

        ForkJoinTask forkJoinTask = provider.get(forkJoinTaskType);

        assertAll(() -> assertNotNull(forkJoinTask),
                  () -> assertTrue(forkJoinTask instanceof RecursiveTask),
                  () -> assertTrue(forkJoinTask.isDone()),
                  () -> assertNotNull(forkJoinTask.get()));

        assertThrows(IllegalArgumentException.class, () -> provider.get(String.class));

        Type futureStringType = new TypeToken<Future<String>>() {}.getType();

        Future<String> futureString = provider.get(futureStringType);

        assertAll(() -> assertNotNull(futureString),
                  () -> assertTrue(futureString instanceof CompletableFuture),
                  () -> assertTrue(futureString.isDone()),
                  () -> assertTrue(futureString.get() instanceof String),
                  () -> assertNotNull(futureString.get()));

        Type cFutureStringType = new TypeToken<CompletableFuture<String>>() {}.getType();

        CompletableFuture<String> cFutureString = provider.get(cFutureStringType);

        assertAll(() -> assertNotNull(cFutureString),
                  () -> assertTrue(cFutureString.isDone()),
                  () -> assertTrue(cFutureString.get() instanceof String),
                  () -> assertNotNull(cFutureString.get()));

        Type futureTaskStringType = new TypeToken<FutureTask<String>>() {}.getType();

        FutureTask<String> futureTaskString = provider.get(futureTaskStringType);

        assertAll(() -> assertNotNull(futureTaskString),
                  () -> assertTrue(futureTaskString.isDone()),
                  () -> assertTrue(futureTaskString.get() instanceof String),
                  () -> assertNotNull(futureTaskString.get()));

        Type forkJoinTaskStringType = new TypeToken<ForkJoinTask<String>>() {}.getType();

        ForkJoinTask<String> forkJoinTaskString = provider.get(forkJoinTaskStringType);

        assertAll(() -> assertNotNull(forkJoinTaskString),
                  () -> assertTrue(forkJoinTaskString instanceof RecursiveTask),
                  () -> assertTrue(forkJoinTaskString.isDone()),
                  () -> assertTrue(forkJoinTaskString.get() instanceof String),
                  () -> assertNotNull(forkJoinTaskString.get()));

        ParameterizedType parameterizedType = Mockito.mock(ParameterizedType.class);
        Mockito.doReturn(Future.class).when(parameterizedType).getRawType();
        Mockito.doReturn(null).when(parameterizedType).getActualTypeArguments();

        Future parameterizedFuture = provider.get(parameterizedType);

        assertAll(() -> assertNotNull(parameterizedFuture),
                  () -> assertTrue(parameterizedFuture instanceof CompletableFuture),
                  () -> assertTrue(parameterizedFuture.isDone()),
                  () -> assertNotNull(parameterizedFuture.get()));

        Mockito.doReturn(new Type[0]).when(parameterizedType).getActualTypeArguments();

        Future parameterizedFuture2 = provider.get(parameterizedType);

        assertAll(() -> assertNotNull(parameterizedFuture2),
                  () -> assertTrue(parameterizedFuture2 instanceof CompletableFuture),
                  () -> assertTrue(parameterizedFuture2.isDone()),
                  () -> assertNotNull(parameterizedFuture2.get()));

        WildcardType wildcardType = Mockito.mock(WildcardType.class);
        assertThrows(IllegalArgumentException.class, () -> provider.get(wildcardType));
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertTrue(provider.recognizes(Future.class));
        assertTrue(provider.recognizes(CompletableFuture.class));
        assertTrue(provider.recognizes(FutureTask.class));
        assertTrue(provider.recognizes(ForkJoinTask.class));

        Type futureStringType = new TypeToken<Future<String>>() {}.getType();

        assertTrue(provider.recognizes(futureStringType));

        Type cFutureStringType = new TypeToken<CompletableFuture<String>>() {}.getType();

        assertTrue(provider.recognizes(cFutureStringType));

        Type futureTaskStringType = new TypeToken<FutureTask<String>>() {}.getType();

        assertTrue(provider.recognizes(futureTaskStringType));

        Type forkJoinTaskStringType = new TypeToken<ForkJoinTask<String>>() {}.getType();

        assertTrue(provider.recognizes(forkJoinTaskStringType));

        assertFalse(provider.recognizes(String.class));

        Type listStringType = new TypeToken<List<String>>() {}.getType();

        assertFalse(provider.recognizes(listStringType));

        Type wildcardType = Mockito.mock(WildcardType.class);

        assertFalse(provider.recognizes(wildcardType));
    }

}
