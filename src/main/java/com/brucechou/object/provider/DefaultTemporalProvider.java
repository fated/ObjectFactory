package com.brucechou.object.provider;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.cycle.CycleDetector;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link Temporal} provider, aka, Java 8 Time, generate random temporal based on
 * {@link Date} generation logic, it actually convert a date object into a temporal.
 */
@AllArgsConstructor
public class DefaultTemporalProvider implements Provider {

    /**
     * Only support the following temporal types.
     */
    private static final Set<Class<? extends Temporal>> SUPPORTED_TEMPORAL_CLASSES = new HashSet<>();

    static {
        SUPPORTED_TEMPORAL_CLASSES.add(Instant.class);
        SUPPORTED_TEMPORAL_CLASSES.add(LocalTime.class);
        SUPPORTED_TEMPORAL_CLASSES.add(LocalDate.class);
        SUPPORTED_TEMPORAL_CLASSES.add(LocalDateTime.class);
        SUPPORTED_TEMPORAL_CLASSES.add(ZonedDateTime.class);
    }

    private final ObjectFactory objectFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type type, CycleDetector cycleDetector) {
        if (type instanceof Class) {
            if (Instant.class.equals(type)) {
                return (T) generateInstant();
            } else if (LocalTime.class.equals(type)) {
                return (T) generateInstant().atZone(ZoneOffset.UTC).toLocalTime();
            } else if (LocalDate.class.equals(type)) {
                return (T) generateInstant().atZone(ZoneOffset.UTC).toLocalDate();
            } else if (LocalDateTime.class.equals(type)) {
                return (T) generateInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
            } else if (ZonedDateTime.class.equals(type)) {
                return (T) generateInstant().atZone(ZoneOffset.UTC);
            }
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    /**
     * Generate {@link Instant} instance from date instance generated by object factory.
     * {@link Date} is primitive type, do not require cycle detector
     *
     * @return random instant instance generated
     */
    private Instant generateInstant() {
        Date date = objectFactory.generate(Date.class);
        return date.toInstant();
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
            return SUPPORTED_TEMPORAL_CLASSES.contains(type);
        }

        return false;
    }

}
