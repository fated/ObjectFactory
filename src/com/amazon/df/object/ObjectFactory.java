package com.amazon.df.object;

import com.amazon.df.object.binding.Binding;
import com.amazon.df.object.cycle.CycleDetector;
import com.amazon.df.object.cycle.CycleTerminator;
import com.amazon.df.object.provider.Provider;
import com.amazon.df.object.resolver.Resolver;
import com.amazon.df.object.spy.ClassSpy;
import com.amazon.df.object.util.Inspector;

import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A thread-safe object factory to generate object with random values populated for given type.
 */
@ThreadSafe
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
    private final Map<String, Provider> globalNameBindings = new HashMap<>();

    // field type -> provider
    private final Map<Type, Provider> globalTypeBindings = new HashMap<>();

    private final List<Provider> providers;
    @Getter
    private final List<Resolver> resolvers;
    private final List<CycleTerminator> terminators;

    @Getter
    private final Random random;
    @Getter
    private final ClassSpy classSpy;

    @Getter
    private final int minSize;
    @Getter
    private final int maxSize;
    private final boolean failOnMissingPrimitiveProvider;

    /**
     * A package access level to instantiate object factory instance with object factory builder.
     *
     * @param builder object factory builder
     */
    ObjectFactory(ObjectFactoryBuilder builder) {
        this.resolvers = new ArrayList<>(builder.getResolvers());
        // additional providers are always put before default providers
        this.providers = Stream.concat(builder.getAdditionalProviders().stream(), builder.getProviders().stream())
                               .map(f -> f.apply(this, builder.getRandom()))
                               .collect(Collectors.toList());
        this.terminators = new ArrayList<>(builder.getTerminators());
        this.classSpy = builder.getClassSpy();
        this.random = builder.getRandom();
        this.minSize = builder.getMinSize();
        this.maxSize = builder.getMaxSize();
        this.failOnMissingPrimitiveProvider = builder.isFailOnMissingPrimitiveProvider();
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
        return generate(type, new CycleDetector());
    }

    /**
     * Internal logic to generate an object of type with cycle detector, used by {@link Provider} only.
     *
     * @param type the type to create
     * @param cycleDetector dependency cycle detector
     * @param <T> the type to create
     * @return generated value
     * @throws ObjectCreationException if failed to create object
     * @throws IllegalArgumentException if given type is not recognized
     */
    @SuppressWarnings("unchecked")
    public <T> T generate(Type type, CycleDetector cycleDetector) {

        CycleDetector.CycleNode cycle = cycleDetector.start(type);

        // if cycle detected, terminate the cycle
        if (cycle != null) {
            return getTerminator(cycle).terminate(cycle);
        }

        try {
            // check if type covered by configured providers
            Provider provider = getProvider(type);

            if (provider != null) {
                // use provider found to generate value for type
                return provider.get(type, cycleDetector);
            }

            // POJO case and Complex JO here, all other cases should be covered in providers
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (Inspector.isExplicitPrimitive(clazz)) {
                    // Provider wasn't provided for this primitive
                    if (failOnMissingPrimitiveProvider) {
                        throw new ObjectCreationException("Provider not found for primitive type %s", clazz);
                    }
                    return (T) DEFAULT_EXPLICIT_PRIMITIVES.get(clazz);
                }

                return generateObject(clazz, cycleDetector);
            }

        } finally {
            cycleDetector.end();
        }

        throw new IllegalArgumentException("Unrecognized type " + type);
    }

    /**
     * Generate instance for given class, and populate fields with arbitrary values.
     * Creation will be terminated if any cycle dependency detected.
     *
     * @param clazz given class to generate
     * @param cycleDetector dependency cycle detector
     * @param <T> the type of given class
     * @return generated object
     */
    @SuppressWarnings("unchecked")
    private <T> T generateObject(Class<?> clazz, CycleDetector cycleDetector) {
        // Create object with constructor
        Object instance = newInstance(clazz, cycleDetector);

        // First try setter to set values
        List<String> invokedSetter = populateFieldsBySetters(clazz, cycleDetector, instance);

        // Then try reflection to set values
        populateFields(clazz, cycleDetector, instance, invokedSetter);

        return (T) instance;
    }

    /**
     * Populate object's fields by using setters provided.
     * Any error happens while invoke setters will be suppressed.
     *
     * @param clazz the given class
     * @param cycleDetector dependency cycle detector
     * @param instance the instance of given class
     * @return field name list set by setters
     */
    private List<String> populateFieldsBySetters(Class<?> clazz, CycleDetector cycleDetector, Object instance) {
        List<String> invokedSetter = new ArrayList<>();

        for (Method setter : classSpy.findMethods(clazz, classSpy.getSetterFilter())) {
            Type argType = setter.getGenericParameterTypes()[0];
            String fieldName = classSpy.extractFieldNameFromSetter(setter);
            try {
                setter.invoke(instance, getArgValue(clazz, argType, fieldName, cycleDetector));
                // if setter actually invoked, collect the field name
                invokedSetter.add(fieldName);
            } catch (Exception e) {
                // make setter invoke not fail on error
                // intentionally ignored
            }
        }

        return invokedSetter;
    }

    /**
     * Populate object's fields by using reflections directly.
     * Any error happens while setting will result in an error.
     *
     * @param clazz the given class
     * @param cycleDetector dependency cycle detector
     * @param instance the instance of given class
     * @param filtered fields to filter, which should already be set before
     * @throws ObjectCreationException if error to set fields
     */
    private void populateFields(Class<?> clazz, CycleDetector cycleDetector, Object instance, List<String> filtered) {
        Predicate<Field> fieldFilter = classSpy.getFieldFilter().and(f -> !filtered.contains(f.getName()));

        for (Field field : classSpy.findFields(clazz, fieldFilter)) {
            boolean accessibility = field.isAccessible();
            field.setAccessible(true);
            try {
                field.set(instance, getArgValue(clazz, field.getGenericType(), field.getName(), cycleDetector));
            } catch (Exception e) {
                throw new ObjectCreationException("Fail to set field %s for instance type %s", field, clazz)
                              .withCause(e);
            } finally {
                field.setAccessible(accessibility);
            }
        }
    }

    /**
     * Get argument value by checking bound providers first and then try generating the object.
     *
     * @param containerType container type
     * @param fieldType field type
     * @param fieldName field name
     * @param cycleDetector cycle detector
     * @return generated object
     */
    private Object getArgValue(Type containerType, Type fieldType, String fieldName, CycleDetector cycleDetector) {
        return Optional.ofNullable(getBoundProvider(containerType, fieldType, fieldName))
                       .map(provider -> provider.get(fieldType, cycleDetector))
                       .orElseGet(() -> generate(fieldType, cycleDetector));
    }

    /**
     * Create an empty instance with constructor for given clazz. Constructor's arguments will be
     * generated automatically if there is any.
     *
     * @param clazz the given clazz
     * @param cycleDetector dependency cycle detector
     * @return created instance
     * @throws ObjectCreationException if failed to invoke constructor with generated parameters
     */
    private Object newInstance(Class<?> clazz, CycleDetector cycleDetector) {
        final Constructor<?> constructor = classSpy.findConstructor(clazz);

        // allow the invocation of non-public constructor
        boolean accessibility = constructor.isAccessible();
        constructor.setAccessible(true);

        try {
            // generate arguments based on argument list
            final List<Object> constructorArgs = new ArrayList<>();
            for (final Type genericType : constructor.getGenericParameterTypes()) {
                // it is not possible to get constructor's parameter name after compilation
                // hence we cannot check bound provider for constructor's arguments
                constructorArgs.add(generate(genericType, cycleDetector));
            }
            return constructor.newInstance(constructorArgs.toArray());
        } catch (Exception e) {
            throw new ObjectCreationException("Fail to create instance for type %s", clazz).withCause(e);
        } finally {
            constructor.setAccessible(accessibility);
        }
    }

    /**
     * Find suitable cycle terminator defined in object factory.
     *
     * @param cycle cycle node detected
     * @return configured cycle terminator for given cycle node
     * @throws IllegalStateException if no terminator can be found for given cycle
     */
    private CycleTerminator getTerminator(CycleDetector.CycleNode cycle) {
        for (CycleTerminator terminator : terminators) {
            if (terminator.canTerminate(cycle)) {
                return terminator;
            }
        }

        throw new IllegalStateException(String.format(
                "Unable to terminate cycle %s, configure the Object Factory with an appropriate "
                        + "cycle terminator to avoid this error", cycle));
    }

    /**
     * Get type providers for a given type, first check global type bindings configured,
     * then go through all configured providers to find a suitable one, FCFS.
     *
     * @param type the given type to check
     * @return bound provider found, or null if no one available
     */
    private Provider getProvider(Type type) {
        Provider provider = globalTypeBindings.get(type);
        if (provider != null) {
            return provider;
        }

        for (Provider p : providers) {
            if (p.recognizes(type)) {
                return p;
            }
        }

        return null;
    }

    /**
     * Get bound provider from local field name bindings, global name bindings and local field type bindings.
     *
     * @param containerType container type
     * @param fieldType field type
     * @param fieldName field name
     * @return bound provider found, or null if no one available
     */
    private Provider getBoundProvider(Type containerType, Type fieldType, String fieldName) {
        Map<String, Provider> nameBindings = fieldNameBindings.get(containerType);
        if (nameBindings != null) {
            Provider provider = nameBindings.get(fieldName);
            if (provider != null) {
                return provider;
            }
        }

        Provider globalNameBinding = globalNameBindings.get(fieldName);
        if (globalNameBinding != null) {
            return globalNameBinding;
        }

        Map<Type, Provider> typeBindings = fieldTypeBindings.get(containerType);
        if (typeBindings != null) {
            return typeBindings.get(fieldType);
        }

        return null;
    }

    /**
     * Process defined binding list to detailed binding map.
     *
     * @param bindings configured provider bindings
     * @throws IllegalArgumentException if multiple bindings provide for same combination,
     *         or unrecognized binding type found
     */
    private void processBindings(List<Binding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        for (Binding binding : bindings) {
            if (binding instanceof Binding.FieldNameBinding) {
                Binding.FieldNameBinding fieldNameBinding = (Binding.FieldNameBinding) binding;
                Map<String, Provider> nameBindings =
                        fieldNameBindings.getOrDefault(fieldNameBinding.getContainer(), new HashMap<>());
                if (nameBindings.containsKey(fieldNameBinding.getFieldName())) {
                    throw new IllegalArgumentException("Cannot provide multiple bindings for the same field name");
                }
                nameBindings.put(fieldNameBinding.getFieldName(), fieldNameBinding.getProvider());
                fieldNameBindings.putIfAbsent(fieldNameBinding.getContainer(), nameBindings);
            } else if (binding instanceof Binding.FieldTypeBinding) {
                Binding.FieldTypeBinding fieldTypeBinding = (Binding.FieldTypeBinding) binding;
                Map<Type, Provider> typeBindings =
                        fieldTypeBindings.getOrDefault(fieldTypeBinding.getContainer(), new HashMap<>());
                if (typeBindings.containsKey(fieldTypeBinding.getFieldType())) {
                    throw new IllegalArgumentException("Cannot provide multiple bindings for the same field type");
                }
                typeBindings.put(fieldTypeBinding.getFieldType(), fieldTypeBinding.getProvider());
                fieldTypeBindings.putIfAbsent(fieldTypeBinding.getContainer(), typeBindings);
            } else if (binding instanceof Binding.GlobalFieldTypeBinding) {
                Binding.GlobalFieldTypeBinding globalFieldTypeBinding = (Binding.GlobalFieldTypeBinding) binding;
                if (globalTypeBindings.containsKey(globalFieldTypeBinding.getFieldType())) {
                    throw new IllegalArgumentException(
                            "Cannot provide multiple global bindings for the same field type");
                }
                globalTypeBindings.put(globalFieldTypeBinding.getFieldType(), globalFieldTypeBinding.getProvider());
            } else if (binding instanceof Binding.GlobalFieldNameBinding) {
                Binding.GlobalFieldNameBinding globalFieldNameBinding = (Binding.GlobalFieldNameBinding) binding;
                if (globalNameBindings.containsKey(globalFieldNameBinding.getFieldName())) {
                    throw new IllegalArgumentException(
                            "Cannot provide multiple global bindings for the same field name");
                }
                globalNameBindings.put(globalFieldNameBinding.getFieldName(), globalFieldNameBinding.getProvider());
            } else {
                throw new IllegalArgumentException(String.format("Unrecognized binding type %s", binding.getClass()));
            }
        }
    }

}
