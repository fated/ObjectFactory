package com.brucechou.object.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ClasspathResolverTest {

    static {
        // anonymous class
        new AbstractClass() {
            @Override
            public void test() {}
        }.test();

        // anonymous class
        new AbstractClass2() {
            @Override
            public void test() {}
        }.test();
    }

    @Test
    void resolve() {
        Resolver resolver = new ClasspathResolver();

        assertEquals(StaticConcreteClass.class, resolver.resolve(InterfaceClass.class));
        assertEquals(StaticConcreteClass.class, resolver.resolve(AbstractClass.class));
        assertNull(resolver.resolve(ExtInterfaceClass.class));

        assertNull(resolver.resolve(InterfaceClass2.class));
        assertNull(resolver.resolve(AbstractClass2.class));
        assertNull(resolver.resolve(ExtInterfaceClass2.class));

        assertEquals(ConcreteClass3.class, resolver.resolve(AbstractClass3.class));
    }

    class MemberConcreteClass extends AbstractClass {
        @Override
        public void test() {}
    }

    static class StaticConcreteClass extends AbstractClass {
        @Override
        public void test() {}
    }

    class MemberConcreteClass2 extends AbstractClass2 {
        @Override
        public void test() {}
    }

}

interface InterfaceClass {

    void test();
}

interface ExtInterfaceClass extends InterfaceClass {}

abstract class AbstractClass implements InterfaceClass {}

interface InterfaceClass2 {

    void test();
}

interface ExtInterfaceClass2 extends InterfaceClass2 {}

abstract class AbstractClass2 implements InterfaceClass2 {}

abstract class AbstractClass3 {}

class ConcreteClass3 extends AbstractClass3{}
