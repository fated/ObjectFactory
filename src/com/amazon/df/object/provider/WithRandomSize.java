package com.amazon.df.object.provider;

import com.amazon.df.object.ObjectFactory;

import java.util.Random;

public interface WithRandomSize {

    default int getRandomSize(ObjectFactory objectFactory, Random random) {
        return random.nextInt(objectFactory.getMaxSize() - objectFactory.getMinSize() + 1)
                       + objectFactory.getMinSize();
    }

}
