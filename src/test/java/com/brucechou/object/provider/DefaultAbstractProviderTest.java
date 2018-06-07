package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brucechou.object.ObjectCreationException;
import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;
import com.brucechou.object.resolver.Resolver;
import com.google.common.reflect.TypeToken;
import javassist.util.proxy.ProxyFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

class DefaultAbstractProviderTest implements ProviderTestBase {

    private DefaultAbstractProvider provider = new DefaultAbstractProvider(getObjectFactory());

    @Test
    void testGetProxy() throws Exception {
        AbstractTestClass obj = provider.get(AbstractTestClass.class);
        assertNotNull(obj);
        assertNotNull(obj.echo());
        assertTrue(ProxyFactory.isProxyClass(obj.getClass()));

        assertThrows(IllegalStateException.class, () -> provider.get(AbstractTestClass1.class));

        assertThrows(ObjectCreationException.class, () -> provider.get(AbstractTestClass2.class));

        assertThrows(IllegalStateException.class, () -> provider.get(ConcreteTestClass.class));
    }

    @Test
    void testGetConcrete() throws Exception {
        ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultBuilder()
                                                          .resolvers(new Resolver() {
                                                              @Override
                                                              public <T> Class<? extends T> resolve(Class<T> clazz) {
                                                                  return (Class<? extends T>) ConcreteTestClass.class;
                                                              }
                                                          })
                                                          .randomSupplier(getRandomSupplier())
                                                          .build();

        DefaultAbstractProvider newProvider = new DefaultAbstractProvider(objectFactory);

        AbstractTestClass obj = newProvider.get(AbstractTestClass.class);
        assertNotNull(obj);
        assertNotNull(obj.echo());
        assertFalse(ProxyFactory.isProxyClass(obj.getClass()));
        assertTrue(ConcreteTestClass.class.isInstance(obj));
    }

    @Test
    void testRecognizes() throws Exception {
        assertTrue(provider.recognizes(AbstractTestClass.class));
        assertFalse(provider.recognizes(String.class));
        assertFalse(provider.recognizes(new TypeToken<List<String>>() {}.getType()));
    }

}

abstract class AbstractTestClass {

    public AbstractTestClass() {}

    abstract String echo();

}

class ConcreteTestClass extends AbstractTestClass {

    private String echo;

    public ConcreteTestClass() {}

    String echo() {
        return echo;
    }

}

abstract class AbstractTestClass1 {

    public AbstractTestClass1(String arg1) {}

    abstract String echo();

}

abstract class AbstractTestClass2 {

    public AbstractTestClass2() {
        throw new UnsupportedOperationException();
    }

    abstract String echo();

}
