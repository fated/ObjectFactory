package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;
import com.amazon.df.object.ObjectFactoryBuilder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface ProviderTestBase {

    int DEFAULT_SIZE = 3;
    Random DEFAULT_RANDOM = ThreadLocalRandom.current();
    ObjectFactory DEFAULT_OBJECT_FACTORY =
            ObjectFactoryBuilder.getDefaultBuilder()
                                .minSize(DEFAULT_SIZE)
                                .maxSize(DEFAULT_SIZE)
                                .random(DEFAULT_RANDOM)
                                .build();

    default Random getRandom() {
        return DEFAULT_RANDOM;
    }

    default ObjectFactory getObjectFactory() {
        return DEFAULT_OBJECT_FACTORY;
    }

    default ObjectFactoryBuilder getObjectFactoryBuilder() {
        return ObjectFactoryBuilder.getDefaultBuilder()
                                   .minSize(DEFAULT_SIZE)
                                   .maxSize(DEFAULT_SIZE)
                                   .random(DEFAULT_RANDOM);
    }

}
