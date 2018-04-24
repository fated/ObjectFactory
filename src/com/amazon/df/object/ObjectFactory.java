package com.amazon.df.object;

import static com.amazon.df.object.util.Throwables.sneakyThrow;

import com.amazon.df.object.binding.Binding;
import com.amazon.df.object.cycle.CycleDetector;
import com.amazon.df.object.cycle.CycleTerminator;
import com.amazon.df.object.provider.Provider;
import com.amazon.df.object.resolver.Resolver;
import com.amazon.df.object.spy.ClassSpy;
import com.amazon.df.object.spy.DefaultClassSpy;
import com.amazon.df.object.util.Inspector;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ObjectFactory {

    private static final Map<Class<?>, Object> DEFAULT_EXPLICIT_PRIMITIVES = new HashMap<>();

    static {
        DEFAULT_EXPLICIT_PRIMITIVES.put(boolean.class, false);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Boolean.class, false);
        DEFAULT_EXPLICIT_PRIMITIVES.put(byte.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Byte.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(char.class, 'a');
        DEFAULT_EXPLICIT_PRIMITIVES.put(Character.class, 'a');
        DEFAULT_EXPLICIT_PRIMITIVES.put(short.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Short.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(int.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Integer.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(long.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Long.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(float.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Float.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(double.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(Double.class, 0);
        DEFAULT_EXPLICIT_PRIMITIVES.put(ByteBuffer.class, ByteBuffer.allocate(0));
        DEFAULT_EXPLICIT_PRIMITIVES.put(String.class, "");
        DEFAULT_EXPLICIT_PRIMITIVES.put(Date.class, new Date());
        DEFAULT_EXPLICIT_PRIMITIVES.put(Object.class, new Object());
    }

    // type -> { field type -> provider }
    private final Map<Type, Map<Type, Provider>> fieldTypeBindings = new HashMap<>();

    // type -> { field name -> provider }
    private final Map<Type, Map<String, Provider>> fieldNameBindings = new HashMap<>();

    // field type -> provider
    private final Map<Type, Provider> globalTypeBindings = new HashMap<>();

    // field type -> provider
    private final Map<String, Provider> globalNameBindings = new HashMap<>();

    private final CycleDetector cycleDetector = new CycleDetector();
    private final ClassSpy classSpy = new DefaultClassSpy();

    private final Provider[] providers;
    private final Resolver[] resolvers;
    private final CycleTerminator[] terminators;

    private final Random random;

    private final int minArrayLength;
    private final int maxArrayLength;
    private final int minCollectionLength;

    public int getMinArrayLength() {
        return minArrayLength;
    }

    public int getMaxArrayLength() {
        return maxArrayLength;
    }

    public int getMinCollectionLength() {
        return minCollectionLength;
    }

    public int getMaxCollectionLength() {
        return maxCollectionLength;
    }

    public int getMinMapEntries() {
        return minMapEntries;
    }

    public int getMaxMapEntries() {
        return maxMapEntries;
    }

    private final int maxCollectionLength;
    private final int minMapEntries;
    private final int maxMapEntries;

    private final boolean failOnMissingPrimitiveProvider;

    ObjectFactory(ObjectFactoryBuilder builder) {
        resolvers = builder.getResolvers();
        providers = builder.getProviders() != null
                    ? Arrays.stream(builder.getProviders())
                            .map(f -> f.apply(this, builder.getRandom()))
                            .toArray(Provider[]::new)
                    : null;
        terminators = builder.getTerminators();
        random = builder.getRandom();
        minArrayLength = builder.getMinArrayLength();
        maxArrayLength = builder.getMaxArrayLength();
        minCollectionLength = builder.getMinCollectionLength();
        maxCollectionLength = builder.getMaxCollectionLength();
        minMapEntries = builder.getMinMapEntries();
        maxMapEntries = builder.getMaxMapEntries();
        failOnMissingPrimitiveProvider = builder.isFailOnMissingPrimitiveProvider();
        processBindings(builder.getBindings());
    }

    /**
     * Generate an object of type.
     *
     * @param type the type to create
     * @param <T> the type to create
     * @return generated value
     */
    public <T> T generate(Type type) {
        CycleDetector.CycleNode cycle = cycleDetector.start(type);

        if (cycle != null) {
            return getTerminator(cycle).terminate(cycle);
        }
        try {
            Provider provider = getProvider(type);
            if (provider != null) {
                return provider.get(type);
            }

            if (type instanceof TypeVariable || type instanceof WildcardType) {
                return null;
            }

            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (Inspector.isExplicitPrimitive(clazz)) {
                    // Provider wasn't provided for this primitive
                    if (failOnMissingPrimitiveProvider) {
                        throw new IllegalStateException(String.format("Provider not found for %s", clazz));
                    }
                    return (T) DEFAULT_EXPLICIT_PRIMITIVES.get(clazz);
                }

                if (clazz.isEnum()) {
                    Object[] enums = clazz.getEnumConstants();
                    return (T) enums[random.nextInt(enums.length)];
                }

                if (clazz.isArray()) {
                    T array = (T) generateArray(clazz.getComponentType());
                    return array;
                }

                T object = generateObject(clazz);
                return object;

            }

            if (type instanceof GenericArrayType) {
                T array = (T) generateArray(((GenericArrayType) type).getGenericComponentType());
                return array;
            }

        } finally {
            cycleDetector.end();
        }

        throw new IllegalArgumentException(String.format("Unrecognized type %s", type));
    }

    private <T> T generateObject(Class<?> clazz) {
        if ("com.amazon.coral.Envelope".equals(clazz.getName())) {
            throw new IllegalArgumentException("Ouch! Looks like you're trying to generate a Coral Envelope."
                                                       + " This isn't implicitly supported, initialize "
                                                       + "your object factory with an appropriate Envelope provider");
        }

        if (Inspector.isInterface(clazz) || Inspector.isAbstract(clazz)) {
            clazz = resolveConcreteType(clazz);
        }

        Object instance = allocateInstance(clazz);

        for (Field field : Inspector.getFields(clazz)) {
            if (!shouldPopulate(field)) {
                continue;
            }

            boolean accessibility = field.isAccessible();
            field.setAccessible(true);
            Provider provider = getBoundProvider(clazz, field.getGenericType(), field.getName());
            try {
                if (provider != null) {
                    field.set(instance, provider.get(field.getGenericType()));
                } else {
                    field.set(instance, generate(field.getGenericType()));
                }
            } catch (Exception e) {
                throw sneakyThrow(e);
            }
            field.setAccessible(accessibility);
        }

        return (T) instance;
    }

    private Object allocateInstance(Class<?> clazz) {
        final Constructor<?> constructor = classSpy.findConstructor(clazz);

        // allow the invocation of non-public constructor
        boolean accessibility = constructor.isAccessible();

        constructor.setAccessible(true);

        final List<Object> constructorArgs = new ArrayList<>();
        for (final Type genericType : constructor.getGenericParameterTypes()) {
            // TODO: Add bound provider support
            constructorArgs.add(generate(genericType));
        }

        try {
            return constructor.newInstance(constructorArgs.toArray());
        } catch (Exception e) {
            throw sneakyThrow(e);
        } finally {
            constructor.setAccessible(accessibility);
        }
    }

    private Object generateArray(Type component) {
        int length = random.nextInt(maxArrayLength - minArrayLength + 1) + minArrayLength;

        Object array;
        if (component instanceof Class) {
            array = Array.newInstance((Class<?>) component, length);
        } else {
            array = Array.newInstance(Object.class, length);
        }

        for (int i = 0; i < length; ++i) {
            Array.set(array, i, generate(component));
        }
        return array;
    }

    private Class<?> resolveConcreteType(Class<?> clazz) {
        for (Resolver resolver : resolvers) {
            Class<?> resolved = resolver.resolve(clazz);
            if (resolved != null) {
                if (Inspector.isInterface(resolved) || Inspector.isAbstract(resolved)) {
                    throw new IllegalStateException(String.format("%s resolved %s to non-concrete type %s",
                                                                  resolver.getClass(), clazz, resolved));
                }
                return resolved;
            }
        }
        throw new IllegalStateException(String.format("Unable to resolve %s to concrete type", clazz));
    }

    private CycleTerminator getTerminator(CycleDetector.CycleNode cycle) {
        if (terminators == null) {
            throw new IllegalStateException(String.format(
                    "Unable to terminate cycle %s, configure the Object Factory with an appropriate "
                            + "cycle terminator to avoid this error", cycle));
        }

        for (CycleTerminator terminator : terminators) {
            if (terminator.canTerminate(cycle)) {
                return terminator;
            }
        }

        throw new IllegalStateException(String.format(
                "Unable to terminate cycle %s, configure the Object Factory with an appropriate "
                        + "cycle terminator to avoid this error", cycle));
    }

    private Provider getProvider(Type type) {
        Provider provider = globalTypeBindings.get(type);
        if (provider != null) {
            return provider;
        }

        if (providers == null) {
            return null;
        }
        for (Provider p : providers) {
            if (p.recognizes(type)) {
                return p;
            }
        }
        return null;
    }

    private Provider getBoundProvider(Type container, Type type, String field) {
        Map<String, Provider> nameBindings = fieldNameBindings.get(container);
        if (nameBindings != null) {
            Provider provider = nameBindings.get(field);
            if (provider != null) {
                return provider;
            }
        }

        Provider globalNameBinding = globalNameBindings.get(field);
        if (globalNameBinding != null) {
            return globalNameBinding;
        }

        Map<Type, Provider> typeBindings = fieldTypeBindings.get(container);
        if (typeBindings != null) {
            return typeBindings.get(type);
        }

        return null;
    }

    private void processBindings(Binding[] bindings) {
        if (bindings == null) {
            return;
        }
        for (Binding binding : bindings) {
            if (binding instanceof Binding.FieldNameBinding) {
                Binding.FieldNameBinding fieldNameBinding = (Binding.FieldNameBinding) binding;
                Map<String, Provider> nameBindings =
                        fieldNameBindings.getOrDefault(fieldNameBinding.type, new HashMap<>());
                if (nameBindings.containsKey(fieldNameBinding.name)) {
                    throw new IllegalArgumentException("Cannot provide multiple bindings for the same field name");
                }
                nameBindings.put(fieldNameBinding.name, fieldNameBinding.provider);
                fieldNameBindings.putIfAbsent(fieldNameBinding.type, nameBindings);
            } else if (binding instanceof Binding.FieldTypeBinding) {
                Binding.FieldTypeBinding fieldTypeBinding = (Binding.FieldTypeBinding) binding;
                Map<Type, Provider> typeBindings =
                        fieldTypeBindings.getOrDefault(fieldTypeBinding.container, new HashMap<>());
                if (typeBindings.containsKey(fieldTypeBinding.fieldType)) {
                    throw new IllegalArgumentException("Cannot provide multiple bindings for the same field type");
                }
                typeBindings.put(fieldTypeBinding.fieldType, fieldTypeBinding.provider);
                fieldTypeBindings.putIfAbsent(fieldTypeBinding.container, typeBindings);
            } else if (binding instanceof Binding.GlobalFieldTypeBinding) {
                Binding.GlobalFieldTypeBinding globalFieldTypeBinding = (Binding.GlobalFieldTypeBinding) binding;
                if (globalTypeBindings.containsKey(globalFieldTypeBinding.fieldType)) {
                    throw new IllegalArgumentException(
                            "Cannot provide multiple global bindings for the same field type");
                }
                globalTypeBindings.put(globalFieldTypeBinding.fieldType, globalFieldTypeBinding.provider);
            } else if (binding instanceof Binding.GlobalFieldNameBinding) {
                Binding.GlobalFieldNameBinding globalFieldNameBinding = (Binding.GlobalFieldNameBinding) binding;
                if (globalNameBindings.containsKey(globalFieldNameBinding.fieldName)) {
                    throw new IllegalArgumentException(
                            "Cannot provide multiple global bindings for the same field type");
                }
                globalNameBindings.put(globalFieldNameBinding.fieldName, globalFieldNameBinding.provider);
            } else {
                throw new IllegalArgumentException(String.format("Unrecognized binding type %s", binding.getClass()));
            }
        }
    }

    private boolean shouldPopulate(Field field) {
        return !Inspector.isVolatile(field) && !Inspector.isStatic(field) && !Inspector.isTransient(field);
    }

    private void validateParameterizedType(ParameterizedType parameterizedType) {
        Type raw = parameterizedType.getRawType();
        if (!(raw instanceof Class)) {
            throw new UnsupportedOperationException("Non-class raw types are not supported for parameterized types");
        }

        if (!Collection.class.isAssignableFrom((Class<?>) raw)
                    && !Map.class.isAssignableFrom((Class<?>) raw)) {
            throw new IllegalArgumentException(String.format("Unrecognized parameterized type %s",
                                                             parameterizedType));
        }
    }

    private void validateClass(Class<?> clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("Raw collection types are not supported");
        }

        if (Map.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("Raw map types are not supported");
        }
    }

    private <T> T newInstance(Class<?> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw sneakyThrow(e);
        }
    }

}
