package com.amazon.df.object;

import com.amazon.df.object.binding.Binding;
import com.amazon.df.object.cycle.CycleTerminator;
import com.amazon.df.object.cycle.NullCycleTerminator;
import com.amazon.df.object.provider.Provider;
import com.amazon.df.object.provider.RandomBigNumberProvider;
import com.amazon.df.object.provider.RandomBufferProvider;
import com.amazon.df.object.provider.RandomPrimitiveProvider;
import com.amazon.df.object.provider.RandomStringProvider;
import com.amazon.df.object.resolver.ClasspathResolver;
import com.amazon.df.object.resolver.Resolver;

import java.util.Random;
import java.util.function.BiFunction;

@SuppressWarnings("HiddenField")
public class ObjectFactoryBuilder {

    private static final int DEFAULT_MIN_MAP_ENTRIES = 1;
    private static final int DEFAULT_MIN_ARRAY_LENGTH = 1;
    private static final int DEFAULT_MIN_COLLECTION_LENGTH = 1;
    private static final int DEFAULT_MAX_MAP_ENTRIES = 10;
    private static final int DEFAULT_MAX_ARRAY_LENGTH = 10;
    private static final int DEFAULT_MAX_COLLECTION_LENGTH = 10;

    private static final boolean DEFAULT_FAIL_ON_MISSING_PRIMITIVE_PROVIDER = false;

    private Binding[] bindings;
    private BiFunction<ObjectFactory, Random, Provider>[] providers;
    private Resolver[] resolvers;
    private CycleTerminator[] terminators;
    private Random random;

    private int minArrayLength = DEFAULT_MIN_ARRAY_LENGTH;
    private int maxArrayLength = DEFAULT_MAX_ARRAY_LENGTH;

    private int minCollectionLength = DEFAULT_MIN_COLLECTION_LENGTH;
    private int maxCollectionLength = DEFAULT_MAX_COLLECTION_LENGTH;

    private int minMapEntries = DEFAULT_MIN_MAP_ENTRIES;
    private int maxMapEntries = DEFAULT_MAX_MAP_ENTRIES;

    private boolean failOnMissingPrimitiveProvider = DEFAULT_FAIL_ON_MISSING_PRIMITIVE_PROVIDER;

    private static final ObjectFactoryBuilder DEFAULT_OBJECT_FACTORY_BUILDER =
            new ObjectFactoryBuilder()
                    .minMapEntries(DEFAULT_MIN_MAP_ENTRIES)
                    .maxMapEntries(DEFAULT_MAX_MAP_ENTRIES)
                    .minArrayLength(DEFAULT_MIN_ARRAY_LENGTH)
                    .maxArrayLength(DEFAULT_MAX_ARRAY_LENGTH)
                    .minCollectionLength(DEFAULT_MIN_COLLECTION_LENGTH)
                    .maxCollectionLength(DEFAULT_MAX_COLLECTION_LENGTH)
                    .terminators(new NullCycleTerminator())
                    .resolvers(new ClasspathResolver())
                    .providers(
                        (f, r) -> new RandomPrimitiveProvider(r),
                        (f, r) -> new RandomBigNumberProvider(r),
                        (f, r) -> new RandomStringProvider(r),
                        (f, r) -> new RandomBufferProvider(r)
                    );

    public static ObjectFactory getDefaultObjectFactory(Random random) {
        return DEFAULT_OBJECT_FACTORY_BUILDER.copy().random(random).build();
    }

    public ObjectFactoryBuilder bindings(Binding... bindings) {
        this.bindings = bindings;
        return this;
    }

    public ObjectFactoryBuilder providers(BiFunction<ObjectFactory, Random, Provider>... providers) {
        this.providers = providers;
        return this;
    }

    public ObjectFactoryBuilder resolvers(Resolver... resolvers) {
        this.resolvers = resolvers;
        return this;
    }

    public ObjectFactoryBuilder terminators(CycleTerminator... terminators) {
        this.terminators = terminators;
        return this;
    }

    public ObjectFactoryBuilder minArrayLength(int length) {
        minArrayLength = length;
        return this;
    }

    public ObjectFactoryBuilder maxArrayLength(int length) {
        maxArrayLength = length;
        return this;
    }

    public ObjectFactoryBuilder minCollectionLength(int length) {
        minCollectionLength = length;
        return this;
    }

    public ObjectFactoryBuilder maxCollectionLength(int length) {
        maxCollectionLength = length;
        return this;
    }

    public ObjectFactoryBuilder minMapEntries(int entries) {
        minMapEntries = entries;
        return this;
    }

    public ObjectFactoryBuilder maxMapEntries(int entries) {
        maxMapEntries = entries;
        return this;
    }

    public ObjectFactoryBuilder failOnMissingPrimitiveProvider(boolean fail) {
        failOnMissingPrimitiveProvider = fail;
        return this;
    }

    public ObjectFactoryBuilder random(Random random) {
        this.random = random;
        return this;
    }

    /*
     * Internal Getters
     */

    Binding[] getBindings() {
        return bindings;
    }

    BiFunction<ObjectFactory, Random, Provider>[] getProviders() {
        return providers;
    }

    Resolver[] getResolvers() {
        return resolvers;
    }

    CycleTerminator[] getTerminators() {
        return terminators;
    }

    Random getRandom() {
        return random;
    }

    int getMinArrayLength() {
        return minArrayLength;
    }

    int getMaxArrayLength() {
        return maxArrayLength;
    }

    int getMinCollectionLength() {
        return minCollectionLength;
    }

    int getMaxCollectionLength() {
        return maxCollectionLength;
    }

    int getMinMapEntries() {
        return minMapEntries;
    }

    int getMaxMapEntries() {
        return maxMapEntries;
    }

    boolean isFailOnMissingPrimitiveProvider() {
        return failOnMissingPrimitiveProvider;
    }

    /**
     * Copy this object factory builder for creating a new one.
     *
     * @return cloned object factory builder
     */
    public ObjectFactoryBuilder copy() {
        ObjectFactoryBuilder b = new ObjectFactoryBuilder();
        b.bindings = bindings;
        b.providers = providers;
        b.resolvers = resolvers;
        b.terminators = terminators;
        b.random = random;
        b.minArrayLength = minArrayLength;
        b.maxArrayLength = maxArrayLength;
        b.minCollectionLength = minCollectionLength;
        b.maxCollectionLength = maxCollectionLength;
        b.minMapEntries = minMapEntries;
        b.maxMapEntries = maxMapEntries;
        b.failOnMissingPrimitiveProvider = failOnMissingPrimitiveProvider;

        return b;
    }

    /**
     * Build the configured object factory.
     *
     * @return an instance of object factory
     */
    public ObjectFactory build() {
        if (minArrayLength < 0) {
            throw new IllegalArgumentException("Min array length must be non-negative");
        }

        if (maxArrayLength < 0) {
            throw new IllegalArgumentException("Max array length must be non-negative");
        }

        if (minCollectionLength < 0) {
            throw new IllegalArgumentException("Min collection length must be non-negative");
        }

        if (maxCollectionLength < 0) {
            throw new IllegalArgumentException("Max collection length must be non-negative");
        }

        if (minMapEntries < 0) {
            throw new IllegalArgumentException("Min map entries must be non-negative");
        }

        if (maxMapEntries < 0) {
            throw new IllegalArgumentException("Max map entries must be non-negative");
        }

        return new ObjectFactory(this);
    }

}
