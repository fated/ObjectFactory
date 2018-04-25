package com.amazon.df.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

class RandomDateProviderTest implements ProviderTestBase {

    private RandomDateProvider provider = new RandomDateProvider(getRandom());

    @Test
    void get() {
        Date date = provider.get(Date.class);

        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("UTC"));

        assertNotNull(zdt);
        assertTrue(zdt.getYear() >= 1900 && zdt.getYear() <= 2100);
    }

    @Test
    void getMinMaxLong() {
        Random random = Mockito.mock(Random.class);
        RandomDateProvider newProvider = new RandomDateProvider(random);

        Mockito.doReturn(Long.MIN_VALUE).when(random).nextLong();
        Date date = newProvider.get(Date.class);

        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("UTC"));

        assertNotNull(zdt);
        assertTrue(zdt.getYear() >= 1900 && zdt.getYear() <= 2100);

        Mockito.doReturn(Long.MAX_VALUE).when(random).nextLong();
        date = newProvider.get(Date.class);

        zdt = date.toInstant().atZone(ZoneId.of("UTC"));

        assertNotNull(zdt);
        assertTrue(zdt.getYear() >= 1900 && zdt.getYear() <= 2100);
    }

    @Test
    void recognizes() {
        assertTrue(provider.recognizes(Date.class));
        assertFalse(provider.recognizes(String.class));
    }

}
