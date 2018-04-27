package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveTask;

/**
 * Default {@link Future} provider, generate a completed future with random value as result.
 */
@AllArgsConstructor
public class DefaultFutureProvider implements Provider {

    /**
     * Only support the following common future types.
     */
    private static final Set<Class<? extends Future>> SUPPORTED_FUTURE_CLASSES = new HashSet<>();

    static {
        SUPPORTED_FUTURE_CLASSES.add(Future.class);
        SUPPORTED_FUTURE_CLASSES.add(CompletableFuture.class);
        SUPPORTED_FUTURE_CLASSES.add(FutureTask.class);
        SUPPORTED_FUTURE_CLASSES.add(ForkJoinTask.class);
    }

    private final ObjectFactory objectFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            return createFuture(type, new Object());
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType futureType = (ParameterizedType) type;
            Type rawType = futureType.getRawType();

            if (futureType.getActualTypeArguments() == null || futureType.getActualTypeArguments().length == 0) {
                return createFuture(rawType, new Object());
            }

            Type actualType = futureType.getActualTypeArguments()[0];

            return createFuture(rawType, objectFactory.generate(actualType, cycleDetector));
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    /**
     * Create a completed future object with given value as result.
     *
     * @param type the type of future
     * @param value the given value as result
     * @param <T> the type of future
     * @return a completed future object
     */
    @SuppressWarnings("unchecked")
    private <T> T createFuture(Type type, Object value) {
        if (CompletableFuture.class.equals(type) || Future.class.equals(type)) {
            return (T) CompletableFuture.completedFuture(value);
        } else if (FutureTask.class.equals(type)) {
            FutureTask futureTask = new FutureTask<>(() -> value);
            futureTask.run();
            return (T) futureTask;
        } else if (ForkJoinTask.class.equals(type)) {
            ForkJoinTask forkJoinTask = new RecursiveTask() {
                @Override
                protected Object compute() {
                    return value;
                }
            };
            forkJoinTask.invoke();
            return (T) forkJoinTask;
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        if (type == null) {
            return false;
        }

        if (type instanceof Class) {
            return SUPPORTED_FUTURE_CLASSES.contains(type);
        }

        if (type instanceof ParameterizedType) {
            return SUPPORTED_FUTURE_CLASSES.contains(((ParameterizedType) type).getRawType());
        }

        return false;
    }

}
