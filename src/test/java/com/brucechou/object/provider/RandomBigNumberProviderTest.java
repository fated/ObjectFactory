package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

class RandomBigNumberProviderTest implements ProviderTestBase {

    private RandomBigNumberProvider provider = new RandomBigNumberProvider(getRandomSupplier());

    @Test
    void get() {
        assertNotNull(provider.get(BigInteger.class));
        assertNotNull(provider.get(BigInteger.class, null));
        assertNotNull(provider.get(BigDecimal.class));
        assertThrows(NullPointerException.class, () -> provider.get(String.class));

        Random random = Mockito.spy(new Random());
        Mockito.doReturn(true).when(random).nextBoolean();

        RandomBigNumberProvider mockProvider = new RandomBigNumberProvider(() -> random);
        assertTrue(((BigInteger) mockProvider.get(BigInteger.class)).compareTo(BigInteger.ZERO) <= 0);
        assertTrue(((BigDecimal) mockProvider.get(BigDecimal.class)).scale() <= 0);

        Mockito.doReturn(false).when(random).nextBoolean();

        assertTrue(((BigInteger) mockProvider.get(BigInteger.class)).compareTo(BigInteger.ZERO) >= 0);
        assertTrue(((BigDecimal) mockProvider.get(BigDecimal.class)).scale() >= 0);
    }

    @Test
    void recognizes() {
        assertTrue(provider.recognizes(BigInteger.class));
        assertTrue(provider.recognizes(BigDecimal.class));
        assertFalse(provider.recognizes(String.class));
    }

}
