package com.brucechou.object.provider;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public interface ProviderTestBase {

    int DEFAULT_SIZE = 3;

    ObjectFactory DEFAULT_OBJECT_FACTORY =
            ObjectFactoryBuilder.getDefaultBuilder()
                                .minSize(DEFAULT_SIZE)
                                .maxSize(DEFAULT_SIZE)
                                .randomSupplier(ThreadLocalRandom::current)
                                .build();

    default Supplier<Random> getRandomSupplier() {
        return ThreadLocalRandom::current;
    }

    default ObjectFactory getObjectFactory() {
        return DEFAULT_OBJECT_FACTORY;
    }

    default ObjectFactoryBuilder getObjectFactoryBuilder() {
        return ObjectFactoryBuilder.getDefaultBuilder()
                                   .minSize(DEFAULT_SIZE)
                                   .maxSize(DEFAULT_SIZE)
                                   .randomSupplier(getRandomSupplier());
    }

}
