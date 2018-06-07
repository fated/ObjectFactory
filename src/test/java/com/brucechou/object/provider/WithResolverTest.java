package com.brucechou.object.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.brucechou.object.ObjectFactory;
import com.brucechou.object.ObjectFactoryBuilder;
import com.brucechou.object.resolver.Resolver;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

class WithResolverTest implements ProviderTestBase {

    @Test
    void resolveConcreteType() {
        Resolver resolver = Mockito.mock(Resolver.class);

        ObjectFactory objectFactory = ObjectFactoryBuilder.getDefaultBuilder()
                                                          .resolvers(resolver)
                                                          .randomSupplier(getRandomSupplier())
                                                          .build();

        WithResolver sut = new WithResolver() {};

        Mockito.doReturn(null).when(resolver).resolve(Mockito.any());
        assertNull(sut.resolveConcreteType(objectFactory, List.class));

        Mockito.doReturn(List.class).when(resolver).resolve(Mockito.any());
        assertNull(sut.resolveConcreteType(objectFactory, List.class));

        Mockito.doReturn(AbstractList.class).when(resolver).resolve(Mockito.any());
        assertNull(sut.resolveConcreteType(objectFactory, List.class));

        Mockito.doReturn(ArrayList.class).when(resolver).resolve(Mockito.any());
        assertEquals(ArrayList.class, sut.resolveConcreteType(objectFactory, List.class));
    }

}
