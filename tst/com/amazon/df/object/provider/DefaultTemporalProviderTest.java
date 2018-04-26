package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class DefaultTemporalProviderTest implements ProviderTestBase {

    private DefaultTemporalProvider provider = new DefaultTemporalProvider(getObjectFactory());

    private Instant minStartExclusive = Instant.ofEpochMilli(-2208988800000L - 1L);
    private Instant maxEndExclusive = Instant.ofEpochMilli(4133980799999L);

    private LocalDate minDate = LocalDate.of(1900, 1, 1);
    private LocalDate maxDate = LocalDate.of(2100, 12, 31);

    @Test
    void get() {
        Instant instant = provider.get(Instant.class);

        assertAll(() -> assertTrue(instant.isAfter(minStartExclusive)),
                  () -> assertTrue(instant.isBefore(maxEndExclusive)));

        LocalTime localTime = provider.get(LocalTime.class);

        assertAll(() -> assertTrue(localTime.compareTo(LocalTime.MIN) >= 0),
                  () -> assertTrue(localTime.compareTo(LocalTime.MAX) <= 0));

        LocalDate localDate = provider.get(LocalDate.class);

        assertAll(() -> assertTrue(localDate.compareTo(minDate) >= 0),
                  () -> assertTrue(localDate.compareTo(maxDate) <= 0));

        LocalDateTime localDateTime = provider.get(LocalDateTime.class);

        assertAll(() -> assertTrue(localDateTime.compareTo(LocalDateTime.of(minDate, LocalTime.MIN)) >= 0),
                  () -> assertTrue(localDateTime.compareTo(LocalDateTime.of(maxDate, LocalTime.MAX)) <= 0));

        ZonedDateTime zonedDateTime = provider.get(ZonedDateTime.class);

        assertAll(() -> assertTrue(zonedDateTime.compareTo(LocalDateTime.of(minDate, LocalTime.MIN).atZone(ZoneOffset.UTC)) >= 0),
                  () -> assertTrue(zonedDateTime.compareTo(LocalDateTime.of(maxDate, LocalTime.MAX).atZone(ZoneOffset.UTC)) <= 0));

        assertThrows(IllegalArgumentException.class, () -> provider.get(String.class));
    }

    @Test
    void recognizes() {
        assertFalse(provider.recognizes(null));
        assertTrue(provider.recognizes(Instant.class));
        assertTrue(provider.recognizes(LocalTime.class));
        assertTrue(provider.recognizes(LocalDate.class));
        assertTrue(provider.recognizes(LocalDateTime.class));
        assertTrue(provider.recognizes(ZonedDateTime.class));
        assertFalse(provider.recognizes(String.class));
    }

}
