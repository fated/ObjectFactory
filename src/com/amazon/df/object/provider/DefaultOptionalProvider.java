package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Default optional provider, generate an optional object with random inside value.
 */
@AllArgsConstructor
public class DefaultOptionalProvider implements Provider {

    private final ObjectFactory objectFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            if (Optional.class.equals(type)) {
                return (T) Optional.empty();
            } else if (OptionalInt.class.equals(type)) {
                return (T) OptionalInt.of(objectFactory.generate(int.class));
            } else if (OptionalLong.class.equals(type)) {
                return (T) OptionalLong.of(objectFactory.generate(long.class));
            } else if (OptionalDouble.class.equals(type)) {
                return (T) OptionalDouble.of(objectFactory.generate(double.class));
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType optionalType = (ParameterizedType) type;

            if (optionalType.getActualTypeArguments() == null || optionalType.getActualTypeArguments().length == 0) {
                return (T) Optional.empty();
            }

            Type actualType = optionalType.getActualTypeArguments()[0];

            return (T) Optional.ofNullable(objectFactory.generate(actualType, cycleDetector));
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean recognizes(Type type) {
        if (type == null) {
            return false;
        }

        if (Optional.class.equals(type)
                    || OptionalInt.class.equals(type)
                    || OptionalLong.class.equals(type)
                    || OptionalDouble.class.equals(type)) {
            return true;
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return Optional.class.equals(rawType);
        }

        return false;
    }

}
