# Object Factory

[![Build Status](https://travis-ci.org/fated/ObjectFactory.svg?branch=master)](https://travis-ci.org/fated/ObjectFactory)
[![Coverage Status](https://coveralls.io/repos/github/fated/ObjectFactory/badge.svg?branch=master)](https://coveralls.io/github/fated/ObjectFactory?branch=master)
[![license](https://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](https://opensource.org/licenses/MIT)

Object Factory aims at simplifying the process of creating randomized objects that could be used
for unit testing. It would save lots of developers' time and make writing unit tests not so tedious.

ObjectFactory is designed to be used by multi-threads, the main class and default implementations are all follow
thread-safe design. ObjectFactory also provides lots of providers to generating almost all common types we used 
in development, those providers can also be used solely to generate random objects for specific types.
ObjectFactory also follows an extensible design, it provides lots of extensible points that allow users to implement
their own logic to satisfy any of their special requirements.

## How To Start

ObjectFactory is fairly easy to use:

1. Add package as test dependency:
    ```
    TODO
    ```

1. The main class of ObjectFactory is ObjectFactory, which provides a single API `<T> T generate(Type type)`
   for generating random object for given type. Check out the below example that creates an ObjectFactory from
   ObjectFactoryBuilder and creates random objects:
    ```java
    // Create ObjectFactory with all default config, will use an instance of ThreadLocalRandom as our Random instance
    final ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultBuilder().build();

    // Create ObjectFactory with all default config, except an externally vended Random instance
    final ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultObjectFactory(new Random());

    // Create ObjectFactory with customized config, we provide ways to change almost all config through builder
    final ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultBuilder()
                                                            .maxSize(20)
                                                            .minSize(10)
                                                            .build();

    // Create object with object factory by specifying class
    final SomeInput input = objectFactory.generate(SomeInput.class);

    // Create object with object factory by specifying type,
    // TypeToken is vended in GoogleGuava or Gson, you can use all other ways to generate a type
    final Set<String> stringSet = objectFactory.generate(new TypeToken<Set<String>>() {}.getType());
    ```

## Supported Types 

ObjectFactory supports following types:

* Primitives
  * `boolean`, `byte`, `char`, `double`, `float`, `int`, `long`, `short`
  * `Boolean`, `Byte`, `Character`, `Double`, `Float`, `Integer`, `Long`, `Short`, `String`, `Date`, `BigInteger`, `BigDecimal`
* Enums and Arrays
  * Enums extend `java.lang.Enum`
  * Arrays like `int[]`, `Integer[]`, `SomePojo[]`
  * Generic Arrays like `List<String>[]`
* Buffers like `ByteBuffer`, `CharBuffer`, `ShortBuffer`, `IntBuffer`, `LongBuffer`, `FloatBuffer`, `DoubleBuffer`
* Temporal types (aka. Java 8 Time), currently only support: `Instant`, `LocalTime`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`
* Collections
  * Generic collection types like `List`, `Set`, `Queue`
  * Concrete collection types with valid constructor like `LinkedList`, `TreeSet`
* Maps
  * Generic map type `Map`
  * Concrete map types with valid constructor like `TreeMap`
* Optional of any supported types such as `Optional<SomePojo>`, `OptionalInt`, `OptionalLong`, `OptionalDouble`
* Stream of any supported types such as `Stream<SomePojo>`, `IntStream`, `LongStream`, `DoubleStream`
* Future of any supported types, currently only support: `Future`, `CompletableFuture`, `FutureTask`, `ForkJoinTask`
* Plan Old Java Object (POJO)
* Interface Proxy: When calling a method from Proxied abstract class/interface, each time the return value is randomized by default.
* Abstract Class Proxy: Only abstract class with empty parameter constructor is supported, this is a restriction of Javassist.

## Available Configurations

ObjectFactory also provides additional configurations you could use to customize your object factory.

### Provider

We build our generation logic for different types mainly in providers. Default providers cannot be removed, but
with additional providers, you could easily override them. You could call `additionalProvider(...)` to add
your customized provider, call multiple times to add multiple providers. Notice that, the order that additional
providers added is the same as they got processed.

```java
ObjectFactory objectFactory = 
        ObjectFactoryBuilder.getDefaultBuilder()
                            .additionalProvider(customizeProviderSupplier1)
                            .additionalProvider(customizeProviderSupplier2)
                            .additionalProvider(customizeProviderSupplier3)
                            .build()
```

Additional providers will always be processed before default providers.

### Bindings

Bindings provide simple ways to bind a specific provider to a field type, field name.
Currently we provide 4 pre-defined binding types:

1. Global field type binding  
    This binding binds a specific field type with a provider globally, this has a highest priority while 
    finding the provider among all bindings, default providers and additional providers.
1. Global field name binding  
    This binding binds a specific field name with a provider globally, and it is effective for fields
    with the specific name of all container types. This means if a field name `customerId` binds with a
    specific provider, `PojoA.customerId` and `PojoB.customerId` will use that provider to generate values.
1. Domain field type binding  
    This binding is similar to global field type binding, but works only within a specific container type. 
    This means if a field type `String` binds with a specific provider under a container type `PojoA`, then 
    all `String` type within `PojoA` will apply that provider, however `String` type in `PojoB` will not be 
    effected.
1. Domain field type binding  
    This binding is similar to global field name binding, but works only within a specific container type.
    This means if a field name `customerId` binds with a specific provider under a container type `PojoA`, 
    then `customerId` in `PojoA` will apply that provider, however `customerId` in `PojoB` will not be effected.

The priority of bindings and other providers are described below:

1. While processing container:  
    Global Field Type Binding > Additional Providers > Default Providers
1. While processing fields:  
    Domain Field Name Binding > Domain Field Type Binding > Global Field Name Binding > 
    Global Field Type Binding > Additional Providers > Default Providers

Although Binding is an interface, however, this requires some internal logic to make it works. So implement new 
binding type is meaningless before we move to a more extensible design on this part.

### Resolver

Resolver is used to resolve concrete type for an interface or abstract class while generating objects with given
interface or abstract. We've implement a classpath resolver to find concrete type from all available types from
the results of class path scanning. However, the resolved type is not stable, and chosen randomly from all available 
types. After some testing, we disable this feature by default due to the performance issues. 
If you want to enable this, you could config your ObjectFactory as below:

```java
ObjectFactory objectFactory = 
        ObjectFactoryBuilder.getDefaultBuilder()
                            .resolvers(new ClasspathResolver())
                            .build()
```

## Extensible Points

ObjectFactory cannot cover all problems while generating objects. But we provide lots of extensible points,
you could write your own implementation to customize object factory to satisfy your requirement.

### Provider

To write your own provider is the most easy way to introduce capability to handle specific type.
A provider can take into an instance of `ObjectFactory` and an instance of `Random` in their constructor
and can use them in their generation logic.

Provider provides three APIs as below:

1. `boolean recognizes(Type type)`: this accept a type as input, and return `true` if this provider can handle this type,
otherwise `false`. You could put any logic here to recognize a type, e.g. `type.getTypeName().startsWith(SOME_PREFIX)`
can recognize all types with name starting with a specific prefix.
1. `<T> T get(Type type, CycleDetector cycleDetector)`: this accept a type which is recognized from the previous method 
and a cycle detector to detect dependency cycle. This method is the actual method that ObjectFactory calls. If your provider
may introduce a dependency cycle, you should leverage the cycle detector we provide while generating objects to avoid
infinite recursion.
1. `<T> T get(Type type)`: this accept a type as input only, and will not be called by ObjectFactory, but it provides
a simpler way to interact provider outside ObjectFactory. The default implementation will call previous method with a
new cycle detector.

We also provide two additional interfaces: `WithRandomSize` and `WithResolver`, all methods in these two interfaces have
default implementation, if you need a random size, or need to resolve a concrete type while generating objects, you could
implement these interfaces and use the methods they provide directly.

### ClassSpy

ObjectFactory use ClassSpy to find specific constructor, find specific methods and find specific fields. We move some
default logic into the interface, so users can easily override them to satisfy their own requirements.

ClassSpy provides three main APIs and some auxiliary methods as below:

**Main APIs**:

1. `<T> Constructor<T> findConstructor(Class<T> clazz)`: find a constructor used to instantiate the object, default behavior
is to find the declared constructor with least number of parameters.
1. `List<Method> findMethods(Class<?> clazz, Predicate<Method> methodFilter)`: find all methods that satisfy the methodFilter,
the default behavior is to find all public methods including inherited methods that satisfy the setter filter. 
Since this API will mainly be called when populating values for a generated object.
1. `List<Field> findFields(Class<?> clazz, Predicate<Field> fieldFilter)`: finds all fields that satisfy the fieldFilter,
the default behavior is to find all fields including inherited and filter the `volatile`, `static` and `transient` fields.

**Auxiliary APIs**:

1. `String getSetterPrefix()`: define the setter prefix
1. `Predicate<Method> getSetterFilter()`: define the way we filter methods
1. `String extractFieldNameFromSetter(Method setter)`: define how to extract field name from setter
1. `Predicate<Field> getFieldFilter()`: define the way we filter fields to set

For example, we use default prefix `set` as our setter prefix while finding setters, if users use `withXXX` to set value,
they can simply write an extended class to override `getSetterPrefix()` method like below:

```java
public class CustomizeClassSpy extends DefaultClassSpy {

    private static final String CUSTOMIZED_PREFIX = "with";

    @Override
    public String getSetterPrefix() {
        return CUSTOMIZED_PREFIX;
    }

}
```

You can also change the logic to determine what fields to set, what constructor to use etc.

### CycleTerminator

ObjectFactory uses null cycle terminator as default, which terminates all cycle node and return null as the value of
node value. If you want to change the logic, just write your own terminator and add it while building your object factory.
We will still add our null cycle terminator at the end of terminator list to avoid all cycle node will be terminated.

CycleTerminator provides two APIs as below:

1. `boolean canTerminate(CycleNode cycle)`: this is used to determine if the detected cycle can be terminated by this
terminator.
1. `<T> T terminate(CycleNode cycle)`: this is used to get the value for the cycle node, in null cycle terminator, it will
always return `null`.

Users can add their own terminator to handle specific type of cycle detected, and return specific value instead of `null`.
