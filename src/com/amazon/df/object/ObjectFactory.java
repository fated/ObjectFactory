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

@Getter
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
    private final Map<Type, Provider> globalTypeBindings = new HashMap<>();

    // field type -> provider
    private final Map<String, Provider> globalNameBindings = new HashMap<>();

    private final ClassSpy classSpy;

    private final List<Provider> providers;
    private final List<Resolver> resolvers;
    private final List<CycleTerminator> terminators;

    private final Random random;

    private final int minSize;
    private final int maxSize;

    private final boolean failOnMissingPrimitiveProvider;

    ObjectFactory(ObjectFactoryBuilder builder) {
        this.resolvers = builder.getResolvers();
        this.providers = Stream.concat(builder.getAdditionalProviders().stream(), builder.getProviders().stream())
                               .map(f -> f.apply(this, builder.getRandom()))
                               .collect(Collectors.toList());
        this.terminators = builder.getTerminators();
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

    @SuppressWarnings("unchecked")
    private <T> T generate(Type type, CycleDetector cycleDetector) {
        CycleDetector.CycleNode cycle = cycleDetector.start(type);

        if (cycle != null) {
            return getTerminator(cycle).terminate(cycle);
        }

        try {
            Provider provider = getProvider(type);

            if (provider != null) {
                return provider.get(type);
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

                return generateObject(clazz, cycleDetector);
            }

        } finally {
            cycleDetector.end();
        }

        throw new IllegalArgumentException("Unrecognized type " + type);
    }

    @SuppressWarnings("unchecked")
    private <T> T generateObject(Class<?> clazz, CycleDetector cycleDetector) {
        Object instance = allocateInstance(clazz, cycleDetector);

        // First try setter to set values
        List<String> invokedSetter = new ArrayList<>();

        for (Method setter : classSpy.findMethods(clazz, classSpy.getSetterFilter())) {
            Type argType = setter.getGenericParameterTypes()[0];
            String fieldName = classSpy.extractFieldNameFromSetter(setter);
            try {
                setter.invoke(instance, getArgValue(clazz, argType, fieldName, cycleDetector));
                invokedSetter.add(fieldName);
            } catch (Exception e) {
                // make setter invoke not fail on error
                // intentionally ignored
            }
        }

        // Then try reflection to set values
        Predicate<Field> fieldFilter = classSpy.getFieldFilter().and(f -> !invokedSetter.contains(f.getName()));

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

        return (T) instance;
    }

    private Object getArgValue(Type concreteClazz, Type argType, String fieldName, CycleDetector cycleDetector) {
        return Optional.ofNullable(getBoundProvider(concreteClazz, argType, fieldName))
                       .map(provider -> provider.get(argType))
                       .orElseGet(() -> generate(argType, cycleDetector));
    }

    private Object allocateInstance(Class<?> clazz, CycleDetector cycleDetector) {
        final Constructor<?> constructor = classSpy.findConstructor(clazz);

        // allow the invocation of non-public constructor
        boolean accessibility = constructor.isAccessible();

        constructor.setAccessible(true);

        final List<Object> constructorArgs = new ArrayList<>();
        for (final Type genericType : constructor.getGenericParameterTypes()) {
            constructorArgs.add(generate(genericType, cycleDetector));
        }

        try {
            return constructor.newInstance(constructorArgs.toArray());
        } catch (Exception e) {
            throw new ObjectCreationException("Fail to create instance for type %s", clazz).withCause(e);
        } finally {
            constructor.setAccessible(accessibility);
        }
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

    private void processBindings(List<Binding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        for (Binding binding : bindings) {
            if (binding instanceof Binding.FieldNameBinding) {
                Binding.FieldNameBinding fieldNameBinding = (Binding.FieldNameBinding) binding;
                Map<String, Provider> nameBindings =
                        fieldNameBindings.getOrDefault(fieldNameBinding.getType(), new HashMap<>());
                if (nameBindings.containsKey(fieldNameBinding.getName())) {
                    throw new IllegalArgumentException("Cannot provide multiple bindings for the same field name");
                }
                nameBindings.put(fieldNameBinding.getName(), fieldNameBinding.getProvider());
                fieldNameBindings.putIfAbsent(fieldNameBinding.getType(), nameBindings);
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
