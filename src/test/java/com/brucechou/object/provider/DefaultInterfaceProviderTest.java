package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;
import com.brucechou.object.resolver.Resolver;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

class DefaultInterfaceProviderTest implements ProviderTestBase {

    private DefaultInterfaceProvider provider = new DefaultInterfaceProvider(getObjectFactory());

    @Test
    void getProxy() {
        TestInterface obj = provider.get(TestInterface.class);
        assertNotNull(obj);
        assertNotNull(obj.echo());
        assertTrue(Proxy.isProxyClass(obj.getClass()));

        assertThrows(IllegalStateException.class, () -> provider.get(ConcreteTestInterface.class));
    }

    @Test
    void getConcrete() throws Exception {
        ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultBuilder()
                                                          .resolvers(new Resolver() {
                                                              @Override
                                                              public <T> Class<? extends T> resolve(Class<T> clazz) {
                                                                  return (Class<? extends T>) ConcreteTestInterface.class;
                                                              }
                                                          })
                                                          .randomSupplier(getRandomSupplier())
                                                          .build();

        DefaultInterfaceProvider newProvider = new DefaultInterfaceProvider(objectFactory);

        TestInterface obj = newProvider.get(TestInterface.class);
        assertNotNull(obj);
        assertNotNull(obj.echo());
        assertFalse(Proxy.isProxyClass(obj.getClass()));
        assertTrue(ConcreteTestInterface.class.isInstance(obj));
    }

    @Test
    void recognizes() {
        assertTrue(provider.recognizes(TestInterface.class));
        assertFalse(provider.recognizes(String.class));
        assertFalse(provider.recognizes(new TypeToken<List<String>>() {}.getType()));
    }

}

interface TestInterface {

    String echo();

}

class ConcreteTestInterface implements TestInterface {

    private String echo;

    public ConcreteTestInterface() {}

    @Override
    public String echo() {
        return echo;
    }

}
