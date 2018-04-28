package com.amazon.df.object;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.amazon.df.object.binding.Bindings;
import com.amazon.df.object.cycle.NullCycleTerminator;
import com.amazon.df.object.provider.DefaultTypesProvider;
import com.amazon.df.object.provider.RandomStringProvider;
import com.amazon.df.object.resolver.NullResolver;
import com.amazon.df.object.spy.DefaultClassSpy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

class ObjectFactoryBuilderTest {

    @Test
    void testIllegalInput() {
        ObjectFactoryBuilder builder = ObjectFactoryBuilder.getDefaultBuilder();

        assertThrows(IllegalArgumentException.class, () -> builder.random(null));
        assertThrows(IllegalArgumentException.class, () -> builder.classSpy(null));
        assertThrows(IllegalArgumentException.class, () -> builder.maxSize(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.maxSize(0));
        assertThrows(IllegalArgumentException.class, () -> builder.minSize(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.minSize(100));
    }

    @Test
    void testCustomize() {
        ObjectFactory objectFactory =
                ObjectFactoryBuilder.getDefaultBuilder()
                                    .bindings(Bindings.bind(String.class, new RandomStringProvider()))
                                    .additionalProvider((f, r) -> new DefaultTypesProvider())
                                    .resolvers(new NullResolver())
                                    .terminators(new NullCycleTerminator())
                                    .classSpy(new DefaultClassSpy())
                                    .minSize(3)
                                    .maxSize(3)
                                    .failOnMissingPrimitiveProvider(false)
                                    .random(new Random())
                                    .build();

        assertNotNull(objectFactory);

        assertNotNull(ObjectFactoryBuilder.getDefaultObjectFactory(new Random()));
    }

    @Test
    void testIllegalFieldsInBuild() throws Exception {
        ObjectFactoryBuilder builder = ObjectFactoryBuilder.getDefaultBuilder();

        Field minSize = ObjectFactoryBuilder.class.getDeclaredField("minSize");
        minSize.setAccessible(true);
        Field maxSize = ObjectFactoryBuilder.class.getDeclaredField("maxSize");
        maxSize.setAccessible(true);
        Field random = ObjectFactoryBuilder.class.getDeclaredField("random");
        random.setAccessible(true);
        Field classSpy = ObjectFactoryBuilder.class.getDeclaredField("classSpy");
        classSpy.setAccessible(true);

        minSize.set(builder, -1);

        assertThrows(IllegalArgumentException.class, builder::build);

        minSize.set(builder, 3);
        maxSize.set(builder, -1);

        assertThrows(IllegalArgumentException.class, builder::build);

        maxSize.set(builder, 1);

        assertThrows(IllegalArgumentException.class, builder::build);

        maxSize.set(builder, 10);
        random.set(builder, null);

        assertThrows(IllegalArgumentException.class, builder::build);

        random.set(builder, new Random());
        classSpy.set(builder, null);

        assertThrows(IllegalArgumentException.class, builder::build);

        classSpy.set(builder, new DefaultClassSpy());

        ObjectFactory factory = builder.build();
        assertNotNull(factory);
        assertNotNull(factory.getRandom());

        Method processBindings = ObjectFactoryBuilder.class.getDeclaredMethod("processBindings", List.class);
        processBindings.setAccessible(true);

        try {
            processBindings.invoke(builder, (List) null);
        } catch (Exception e) {
            fail("should not throws");
        }
    }

}
