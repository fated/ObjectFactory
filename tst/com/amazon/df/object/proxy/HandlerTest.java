package com.amazon.df.object.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.df.object.ObjectFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;

class HandlerTest {

    @Mock
    private ObjectFactory objectFactory;

    @InjectMocks
    private Handler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void invoke() throws Throwable {
        Method method = Mockito.mock(Method.class);

        Mockito.doReturn("hashCode").when(method).getName();

        assertTrue(((int) handler.invoke(null, method, null)) > 0);

        Mockito.doReturn("toString").when(method).getName();

        assertNotNull(handler.invoke(null, method, null));

        Mockito.doReturn("equals").when(method).getName();
        Mockito.doReturn(new Class<?>[] {Object.class}).when(method).getParameterTypes();

        assertFalse((boolean) handler.invoke(null, method, new Object[] {null}));

        Object self = new Object();
        assertTrue((boolean) handler.invoke(self, method, new Object[] {self}));

        List proxy = (List) Proxy.newProxyInstance(List.class.getClassLoader(), new Class[] {List.class}, handler);

        assertFalse((boolean) handler.invoke(proxy, method, new Object[] {new Object()}));

        List proxy2 = (List) Proxy.newProxyInstance(List.class.getClassLoader(), new Class[] {List.class}, handler);

        assertTrue((boolean) handler.invoke(proxy, method, new Object[] {proxy2}));

        Mockito.doReturn("toString").when(method).getName();
        Mockito.doReturn(new Class<?>[] {Object.class, Object.class}).when(method).getParameterTypes();
        Mockito.doReturn(String.class).when(method).getGenericReturnType();
        Mockito.doReturn("test").when(objectFactory).generate(Mockito.any(Type.class));

        assertEquals("test", handler.invoke(null, method, null));

        Mockito.verify(objectFactory, Mockito.times(1)).generate(Mockito.any());
    }

    @Test
    void invoke1() throws Throwable {
        Method method = Mockito.mock(Method.class);

        Mockito.doReturn("hashCode").when(method).getName();

        assertTrue(((int) handler.invoke(null, method, null, null)) > 0);
    }

    @Test
    void equalsAndHashCode() {
        assertFalse(handler.equals(new Object()));
        assertTrue(handler.hashCode() > 0);
        assertNotNull(handler.toString());
    }

}
