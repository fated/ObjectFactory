package com.amazon.df.object;

import com.amazon.df.object.binding.Binding;
import com.amazon.df.object.cycle.CycleTerminator;
import com.amazon.df.object.cycle.NullCycleTerminator;
import com.amazon.df.object.provider.DefaultAbstractProvider;
import com.amazon.df.object.provider.DefaultArrayProvider;
import com.amazon.df.object.provider.DefaultCollectionProvider;
import com.amazon.df.object.provider.DefaultEnumProvider;
import com.amazon.df.object.provider.DefaultFutureProvider;
import com.amazon.df.object.provider.DefaultInterfaceProvider;
import com.amazon.df.object.provider.DefaultIterableProvider;
import com.amazon.df.object.provider.DefaultMapProvider;
import com.amazon.df.object.provider.DefaultOptionalProvider;
import com.amazon.df.object.provider.DefaultStreamProvider;
import com.amazon.df.object.provider.DefaultTemporalProvider;
import com.amazon.df.object.provider.DefaultTypesProvider;
import com.amazon.df.object.provider.Provider;
import com.amazon.df.object.provider.RandomBigNumberProvider;
import com.amazon.df.object.provider.RandomBufferProvider;
import com.amazon.df.object.provider.RandomDateProvider;
import com.amazon.df.object.provider.RandomPrimitiveProvider;
import com.amazon.df.object.provider.RandomStringProvider;
import com.amazon.df.object.resolver.NullResolver;
import com.amazon.df.object.resolver.Resolver;
import com.amazon.df.object.spy.ClassSpy;
import com.amazon.df.object.spy.DefaultClassSpy;

import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

/**
 * Object factory builder to help create {@link ObjectFactory}.
 */
@SuppressWarnings("HiddenField")
@Getter(AccessLevel.PACKAGE)
public final class ObjectFactoryBuilder {

    private static final int DEFAULT_MIN_SIZE = 1;
    private static final int DEFAULT_MAX_SIZE = 10;

    private static final boolean DEFAULT_FAIL_ON_MISSING_PRIMITIVE_PROVIDER = false;

    private static final List<BiFunction<ObjectFactory, Random, Provider>> DEFAULT_PROVIDERS;

    static {
        // Order matters, primitive types -> enum type -> array type
        // -> interfaces needs special handling -> general interface -> abstract
        DEFAULT_PROVIDERS = Collections.unmodifiableList(Arrays.asList(
            (f, r) -> new DefaultTypesProvider(),
            (f, r) -> new RandomPrimitiveProvider(r),
            (f, r) -> new RandomBigNumberProvider(r),
            (f, r) -> new RandomDateProvider(r),
            (f, r) -> new RandomStringProvider(),
            (f, r) -> new RandomBufferProvider(r),
            (f, r) -> new DefaultTemporalProvider(f),
            (f, r) -> new DefaultEnumProvider(r),
            (f, r) -> new DefaultArrayProvider(f, r),
            (f, r) -> new DefaultCollectionProvider(f, r),
            (f, r) -> new DefaultIterableProvider(f, r),
            (f, r) -> new DefaultMapProvider(f, r),
            (f, r) -> new DefaultStreamProvider(f, r),
            (f, r) -> new DefaultOptionalProvider(f),
            (f, r) -> new DefaultFutureProvider(f),
            (f, r) -> new DefaultInterfaceProvider(f),
            (f, r) -> new DefaultAbstractProvider(f)
        ));
    }

    // container type -> [ field type -> provider ]
    private Map<Type, Map<Type, Provider>> fieldTypeBindings;

    // container type -> [ field name -> provider ]
    private Map<Type, Map<String, Provider>> fieldNameBindings;

    // field type -> provider
    private Map<Type, Provider> globalTypeBindings;

    // field name -> provider
    private Map<String, Provider> globalNameBindings;

    private List<BiFunction<ObjectFactory, Random, Provider>> providers;
    private List<BiFunction<ObjectFactory, Random, Provider>> additionalProviders;
    private List<Resolver> resolvers;
    private List<CycleTerminator> terminators;
    private ClassSpy classSpy;
    private Random random;

    private int minSize = DEFAULT_MIN_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;

    private boolean failOnMissingPrimitiveProvider = DEFAULT_FAIL_ON_MISSING_PRIMITIVE_PROVIDER;

    private static final ObjectFactoryBuilder DEFAULT_OBJECT_FACTORY_BUILDER =
            new ObjectFactoryBuilder().resolvers(new NullResolver())
                                      .classSpy(new DefaultClassSpy())
                                      .random(ThreadLocalRandom.current());

    /**
     * Get default object factory builder with default config.
     *
     * @return a copy of default object factory builder
     */
    public static ObjectFactoryBuilder getDefaultBuilder() {
        return DEFAULT_OBJECT_FACTORY_BUILDER.copy();
    }

    /**
     * Get default object factory with specific random instance.
     *
     * @param random random instance
     * @return a created object factory with default builder and specific random
     */
    public static ObjectFactory getDefaultObjectFactory(Random random) {
        return DEFAULT_OBJECT_FACTORY_BUILDER.copy().random(random).build();
    }

    /**
     * Constructor to initialize inner lists.
     */
    private ObjectFactoryBuilder() {
        this.additionalProviders = new ArrayList<>();
        this.resolvers = new ArrayList<>();
        this.terminators = new ArrayList<>();
        this.providers = new ArrayList<>(DEFAULT_PROVIDERS);
        this.fieldTypeBindings = new HashMap<>();
        this.fieldNameBindings = new HashMap<>();
        this.globalTypeBindings = new HashMap<>();
        this.globalNameBindings = new HashMap<>();
    }

    /**
     * Add bindings to current builder. Call multi times will add all bindings.
     *
     * @param bindings defined bindings to add
     * @return this object factory builder
     */
    public ObjectFactoryBuilder bindings(Binding... bindings) {
        processBindings(Arrays.asList(bindings));
        return this;
    }

    /**
     * Use with caution! this overwrite default providers. Use {@link #additionalProvider(BiFunction[])}
     * to add additional providers.
     *
     * @param providers defined providers to add
     * @return this object factory builder
     */
    @SafeVarargs
    public final ObjectFactoryBuilder providers(BiFunction<ObjectFactory, Random, Provider>... providers) {
        this.providers = new ArrayList<>(Arrays.asList(providers));
        return this;
    }

    /**
     * Add additional providers to current builder. Call multi times will add all providers.
     * Additional providers will always be put before default providers.
     *
     * @param providers defined additional providers to add
     * @return this object factory builder
     */
    @SafeVarargs
    public final ObjectFactoryBuilder additionalProvider(BiFunction<ObjectFactory, Random, Provider>... providers) {
        this.additionalProviders.addAll(Arrays.asList(providers));
        return this;
    }

    /**
     * Add resolvers to current builder. Call multi times will add all resolvers.
     *
     * @param resolvers defined resolvers to add
     * @return this object factory builder
     */
    public ObjectFactoryBuilder resolvers(Resolver... resolvers) {
        this.resolvers.addAll(Arrays.asList(resolvers));
        return this;
    }

    /**
     * Add terminators to current builder. Call multi times will add all terminators.
     *
     * @param terminators defined terminators to add
     * @return this object factory builder
     */
    public ObjectFactoryBuilder terminators(CycleTerminator... terminators) {
        this.terminators.addAll(Arrays.asList(terminators));
        return this;
    }

    /**
     * Add classSpy to current builder.
     *
     * @param classSpy defined classSpy to add
     * @return this object factory builder
     * @throws IllegalArgumentException if given class spy is null
     */
    public ObjectFactoryBuilder classSpy(ClassSpy classSpy) {
        if (classSpy == null) {
            throw new IllegalArgumentException("Class spy must be non-null");
        }
        this.classSpy = classSpy;
        return this;
    }

    /**
     * Set the min size for generated collection, map, array, stream etc.
     *
     * @param minSize min size
     * @return this object factory builder
     * @throws IllegalArgumentException if given min size is less than 0 or greater than current max size
     */
    public ObjectFactoryBuilder minSize(int minSize) {
        if (minSize < 0 || minSize > this.maxSize) {
            throw new IllegalArgumentException("Min size must be non-negative and <= max size");
        }

        this.minSize = minSize;
        return this;
    }

    /**
     * Set the max size for generated collection, map, array, stream etc.
     *
     * @param maxSize max size
     * @return this object factory builder
     * @throws IllegalArgumentException if given max size is less than 0 or less than current min size
     */
    public ObjectFactoryBuilder maxSize(int maxSize) {
        if (maxSize < 0 || maxSize < this.minSize) {
            throw new IllegalArgumentException("Max size must be non-negative and >= min size");
        }

        this.maxSize = maxSize;
        return this;
    }

    /**
     * Set the fail-on-missing-primitive-provider flag, will throw exception if set to true.
     * Otherwise, use the default value for primitive types.
     *
     * @param fail fail-on-missing-primitive-provider flag
     * @return this object factory builder
     */
    public ObjectFactoryBuilder failOnMissingPrimitiveProvider(boolean fail) {
        failOnMissingPrimitiveProvider = fail;
        return this;
    }

    /**
     * Add random to current builder.
     *
     * @param random defined random to add
     * @return this object factory builder
     * @throws IllegalArgumentException if given random is null
     */
    public ObjectFactoryBuilder random(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random must be non-null");
        }
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
        b.providers = new ArrayList<>(providers);
        b.additionalProviders = new ArrayList<>(additionalProviders);
        b.resolvers = new ArrayList<>(resolvers);
        b.terminators = new ArrayList<>(terminators);
        b.classSpy = classSpy;
        b.random = random;
        b.minSize = minSize;
        b.maxSize = maxSize;
        b.failOnMissingPrimitiveProvider = failOnMissingPrimitiveProvider;
        b.fieldTypeBindings = new HashMap<>(fieldTypeBindings);
        b.fieldNameBindings = new HashMap<>(fieldNameBindings);
        b.globalTypeBindings = new HashMap<>(globalTypeBindings);
        b.globalNameBindings = new HashMap<>(globalNameBindings);

        return b;
    }

    /**
     * Build the configured object factory.
     *
     * @return an instance of object factory
     * @throws IllegalArgumentException if any invalid values detected
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
            throw new IllegalArgumentException("Random instance must be non-null");
        }

        if (classSpy == null) {
            throw new IllegalArgumentException("Class Spy instance must be non-null");
        }

        // Add null cycle terminator at the end of terminator list to avoid cycle not terminated issue
        this.terminators.add(new NullCycleTerminator());

        return new ObjectFactory(this);
    }

    /**
     * Process defined binding list to detailed binding map.
     *
     * @param bindings configured provider bindings
     * @throws IllegalArgumentException if unrecognized binding type found
     */
    private void processBindings(List<Binding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        for (Binding binding : bindings) {
            if (binding instanceof Binding.FieldNameBinding) {
                processFieldNameBinding((Binding.FieldNameBinding) binding);
            } else if (binding instanceof Binding.FieldTypeBinding) {
                processFieldTypeBinding((Binding.FieldTypeBinding) binding);
            } else if (binding instanceof Binding.GlobalFieldTypeBinding) {
                processGlobalFieldTypeBinding((Binding.GlobalFieldTypeBinding) binding);
            } else if (binding instanceof Binding.GlobalFieldNameBinding) {
                processGlobalFieldNameBinding((Binding.GlobalFieldNameBinding) binding);
            } else {
                throw new IllegalArgumentException(String.format("Unrecognized binding type %s", binding.getClass()));
            }
        }
    }

    /**
     * Process local field name binding to detailed binding map.
     *
     * @param fieldNameBinding local field name binding
     * @throws IllegalArgumentException if multiple bindings provide for same combination
     */
    private void processFieldNameBinding(Binding.FieldNameBinding fieldNameBinding) {
        Map<String, Provider> nameBindings =
                fieldNameBindings.getOrDefault(fieldNameBinding.getContainer(), new HashMap<>());
        if (nameBindings.containsKey(fieldNameBinding.getFieldName())) {
            throw new IllegalArgumentException("Cannot provide multiple bindings for the same field name");
        }
        nameBindings.put(fieldNameBinding.getFieldName(), fieldNameBinding.getProvider());
        fieldNameBindings.putIfAbsent(fieldNameBinding.getContainer(), nameBindings);
    }

    /**
     * Process local field type binding to detailed binding map.
     *
     * @param fieldTypeBinding local field type binding
     * @throws IllegalArgumentException if multiple bindings provide for same combination
     */
    private void processFieldTypeBinding(Binding.FieldTypeBinding fieldTypeBinding) {
        Map<Type, Provider> typeBindings =
                fieldTypeBindings.getOrDefault(fieldTypeBinding.getContainer(), new HashMap<>());
        if (typeBindings.containsKey(fieldTypeBinding.getFieldType())) {
            throw new IllegalArgumentException("Cannot provide multiple bindings for the same field type");
        }
        typeBindings.put(fieldTypeBinding.getFieldType(), fieldTypeBinding.getProvider());
        fieldTypeBindings.putIfAbsent(fieldTypeBinding.getContainer(), typeBindings);
    }

    /**
     * Process global field type binding to detailed binding map.
     *
     * @param globalFieldTypeBinding global field type binding
     * @throws IllegalArgumentException if multiple bindings provide for same combination
     */
    private void processGlobalFieldTypeBinding(Binding.GlobalFieldTypeBinding globalFieldTypeBinding) {
        if (globalTypeBindings.containsKey(globalFieldTypeBinding.getFieldType())) {
            throw new IllegalArgumentException(
                    "Cannot provide multiple global bindings for the same field type");
        }
        globalTypeBindings.put(globalFieldTypeBinding.getFieldType(), globalFieldTypeBinding.getProvider());
    }

    /**
     * Process global field name binding to detailed binding map.
     *
     * @param globalFieldNameBinding global field name binding
     * @throws IllegalArgumentException if multiple bindings provide for same combination
     */
    private void processGlobalFieldNameBinding(Binding.GlobalFieldNameBinding globalFieldNameBinding) {
        if (globalNameBindings.containsKey(globalFieldNameBinding.getFieldName())) {
            throw new IllegalArgumentException(
                    "Cannot provide multiple global bindings for the same field name");
        }
        globalNameBindings.put(globalFieldNameBinding.getFieldName(), globalFieldNameBinding.getProvider());
    }

}
