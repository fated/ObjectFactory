package com.amazon.df.object;

import com.amazon.df.object.binding.Binding;
import com.amazon.df.object.cycle.CycleTerminator;
import com.amazon.df.object.cycle.NullCycleTerminator;
import com.amazon.df.object.provider.DefaultArrayProvider;
import com.amazon.df.object.provider.DefaultCollectionProvider;
import com.amazon.df.object.provider.DefaultEnumProvider;
import com.amazon.df.object.provider.DefaultMapProvider;
import com.amazon.df.object.provider.DefaultOptionalProvider;
import com.amazon.df.object.provider.DefaultStreamProvider;
import com.amazon.df.object.provider.Provider;
import com.amazon.df.object.provider.RandomBigNumberProvider;
import com.amazon.df.object.provider.RandomBufferProvider;
import com.amazon.df.object.provider.RandomPrimitiveProvider;
import com.amazon.df.object.provider.RandomStringProvider;
import com.amazon.df.object.resolver.NullResolver;
import com.amazon.df.object.resolver.Resolver;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

@SuppressWarnings("HiddenField")
@Getter(AccessLevel.PACKAGE)
public class ObjectFactoryBuilder {

    private static final int DEFAULT_MIN_SIZE = 1;
    private static final int DEFAULT_MAX_SIZE = 10;

    private static final boolean DEFAULT_FAIL_ON_MISSING_PRIMITIVE_PROVIDER = false;

    private static final List<BiFunction<ObjectFactory, Random, Provider>> DEFAULT_PROVIDERS;

    static {
        DEFAULT_PROVIDERS = Collections.unmodifiableList(Arrays.asList(
            (f, r) -> new RandomPrimitiveProvider(r),
            (f, r) -> new RandomBigNumberProvider(r),
            (f, r) -> new RandomStringProvider(r),
            (f, r) -> new RandomBufferProvider(r),
            (f, r) -> new DefaultEnumProvider(r),
            (f, r) -> new DefaultArrayProvider(f, r),
            (f, r) -> new DefaultCollectionProvider(f, r),
            (f, r) -> new DefaultMapProvider(f, r),
            (f, r) -> new DefaultOptionalProvider(f),
            (f, r) -> new DefaultStreamProvider(f, r)
        ));
    }

    private List<Binding> bindings;
    private List<BiFunction<ObjectFactory, Random, Provider>> providers;
    private List<BiFunction<ObjectFactory, Random, Provider>> additionalProviders;
    private List<Resolver> resolvers;
    private List<CycleTerminator> terminators;
    private Random random;

    private int minSize = DEFAULT_MIN_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;

    private boolean failOnMissingPrimitiveProvider = DEFAULT_FAIL_ON_MISSING_PRIMITIVE_PROVIDER;

    private static final ObjectFactoryBuilder DEFAULT_OBJECT_FACTORY_BUILDER =
            new ObjectFactoryBuilder().minSize(DEFAULT_MIN_SIZE)
                                      .maxSize(DEFAULT_MAX_SIZE)
                                      .terminators(new NullCycleTerminator())
                                      .resolvers(new NullResolver());

    public static ObjectFactoryBuilder getDefaultBuilder() {
        return DEFAULT_OBJECT_FACTORY_BUILDER.copy();
    }

    public static ObjectFactory getDefaultObjectFactory(Random random) {
        return DEFAULT_OBJECT_FACTORY_BUILDER.copy().random(random).build();
    }

    /**
     * Constructor to initialize inner lists.
     */
    public ObjectFactoryBuilder() {
        this.bindings = new ArrayList<>();
        this.additionalProviders = new ArrayList<>();
        this.resolvers = new ArrayList<>();
        this.terminators = new ArrayList<>();

        this.providers = new ArrayList<>(DEFAULT_PROVIDERS);
    }

    public ObjectFactoryBuilder bindings(Binding... bindings) {
        this.bindings.addAll(Arrays.asList(bindings));
        return this;
    }

    /**
     * Use with caution! this overwrite default providers.
     *
     * @param providers providers
     * @return this builder
     */
    public ObjectFactoryBuilder providers(BiFunction<ObjectFactory, Random, Provider>... providers) {
        this.providers = new ArrayList<>(Arrays.asList(providers));
        return this;
    }

    public ObjectFactoryBuilder additionalProvider(BiFunction<ObjectFactory, Random, Provider>... provider) {
        this.additionalProviders.addAll(Arrays.asList(provider));
        return this;
    }

    public ObjectFactoryBuilder resolvers(Resolver... resolvers) {
        this.resolvers.addAll(Arrays.asList(resolvers));
        return this;
    }

    public ObjectFactoryBuilder terminators(CycleTerminator... terminators) {
        this.terminators.addAll(Arrays.asList(terminators));
        return this;
    }

    /**
     * Set the min size for generated collection, map, array, stream etc.
     *
     * @param minSize min size
     * @return this builder
     */
    public ObjectFactoryBuilder minSize(int minSize) {
        if (minSize < 0) {
            throw new IllegalArgumentException("Min size must be non-negative");
        }

        this.minSize = minSize;
        return this;
    }

    /**
     * Set the max size for generated collection, map, array, stream etc.
     *
     * @param maxSize max size
     * @return this builder
     */
    public ObjectFactoryBuilder maxSize(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Max size must be non-negative");
        }

        this.maxSize = maxSize;
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

    /**
     * Copy this object factory builder for creating a new one.
     *
     * @return cloned object factory builder
     */
    public ObjectFactoryBuilder copy() {
        ObjectFactoryBuilder b = new ObjectFactoryBuilder();
        b.bindings = new ArrayList<>(bindings);
        b.providers = new ArrayList<>(providers);
        b.additionalProviders = new ArrayList<>(additionalProviders);
        b.resolvers = new ArrayList<>(resolvers);
        b.terminators = new ArrayList<>(terminators);
        b.random = random;
        b.minSize = minSize;
        b.maxSize = minSize;
        b.failOnMissingPrimitiveProvider = failOnMissingPrimitiveProvider;

        return b;
    }

    /**
     * Build the configured object factory.
     *
     * @return an instance of object factory
     */
    public ObjectFactory build() {
        if (minSize < 0) {
            throw new IllegalArgumentException("Min size must be non-negative");
        }

        if (maxSize < 0) {
            throw new IllegalArgumentException("Max size must be non-negative");
        }

        if (maxSize < minSize) {
            throw new IllegalArgumentException("Max size must be greater than or equal to min size");
        }

        if (random == null) {
            throw new IllegalArgumentException("Random instance must not be null");
        }

        return new ObjectFactory(this);
    }

}
