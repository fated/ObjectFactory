package com.brucechou.object.provider;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;

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
