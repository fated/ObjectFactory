package com.amazon.df.object;

import static com.amazon.df.object.util.Throwables.sneakyThrow;

import com.amazon.df.object.binding.Binding;
import com.amazon.df.object.cycle.CycleDetector;
import com.amazon.df.object.cycle.CycleTerminator;
import com.amazon.df.object.provider.Provider;
import com.amazon.df.object.proxy.Handler;
import com.amazon.df.object.resolver.Resolver;
import com.amazon.df.object.spy.ClassSpy;
import com.amazon.df.object.spy.DefaultClassSpy;
import com.amazon.df.object.util.Inspector;

import javassist.util.proxy.ProxyFactory;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
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

                return generateObject(clazz);
            }

        } finally {
            cycleDetector.end();
        }

        throw new IllegalArgumentException("Unrecognized type " + type);
    }

    private <T> T generateObject(Class<?> clazz) {
        Class<?> concreteClazz = clazz;

        if (Inspector.isInterface(clazz) || Inspector.isAbstract(clazz)) {
            concreteClazz = resolveConcreteType(clazz);

            if (concreteClazz == null) {
                return handleInterfaceOrAbstract(clazz);
            }
        }

        Object instance = allocateInstance(concreteClazz);

        return populateFields(concreteClazz, instance);
    }

    private <T> T populateFields(Class<?> concreteClazz, Object instance) {
        for (Field field : classSpy.findFields(concreteClazz, this::shouldPopulate)) {
            boolean accessibility = field.isAccessible();
            field.setAccessible(true);
            Provider provider = getBoundProvider(concreteClazz, field.getGenericType(), field.getName());
            try {
                if (provider != null) {
                    field.set(instance, provider.get(field.getGenericType()));
                } else {
                    field.set(instance, generate(field.getGenericType()));
                }
            } catch (Exception e) {
                throw sneakyThrow(e);
            } finally {
                field.setAccessible(accessibility);
            }
        }

        return (T) instance;
    }

    private <T> T handleInterfaceOrAbstract(Class<?> clazz) {
        if (Inspector.isInterface(clazz)) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new Handler(this));
        }

        if (Inspector.isAbstract(clazz)) {
            // Alternative solution is to use CGLib's Enhancer
            checkIfSupported(clazz);
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(clazz);
            Object object = newInstance(factory.createClass());

            ((javassist.util.proxy.Proxy) object).setHandler(new Handler(this));

            return (T) object;
        }

        throw new IllegalStateException("Unable to create proxy for " + clazz);
    }

    /**
     * Checks if the abstract class is supported.
     * Currently only supports abstract class with no argument constructor.
     *
     * @param clazz the class to check.
     * @throws IllegalStateException if the class is not supported.
     */
    private void checkIfSupported(final Class clazz) {
        assert clazz != null : "clazz cannot be null";

        final Constructor constructor = classSpy.findConstructor(clazz);
        if (constructor.getParameterCount() != 0) {
            throw new IllegalStateException(clazz + " doesn't have constructor with no arguments");
        }
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

        return null;
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

    private boolean shouldPopulate(Field field) {
        return !Inspector.isVolatile(field) && !Inspector.isStatic(field) && !Inspector.isTransient(field);
    }

    private <T> T newInstance(Class<?> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw sneakyThrow(e);
        }
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
