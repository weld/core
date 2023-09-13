/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.interceptors.producer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.interceptors.producer.Producer.Bar;
import org.jboss.weld.tests.interceptors.producer.Producer.Foo;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @author Matej Novotny
 */
@RunWith(Arquillian.class)
public class InterceptionFactoryTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptionFactoryTest.class))
                .addPackage(InterceptionFactoryTest.class.getPackage());
    }

    @Test
    public void testFooPing(@Produced Instance<Foo> fooInstance) {
        Foo foo1 = fooInstance.get();
        assertEquals("Hello pong", foo1.ping());
        Foo foo2 = fooInstance.get();
        assertEquals("Hello pong", foo2.ping());
        // Test proxy classes are shared
        assertEquals(foo1.getClass(), foo2.getClass());
    }

    @Test
    public void testFooClassLevelBinding(@Produced("classLevel") Foo foo) {
        assertEquals("Hello pong", foo.ping());
    }

    @Test
    public void testFooEjbInterceptors(@Produced("ejbInterceptors") Foo foo) {
        assertEquals("Hello pong", foo.ping());
    }

    @Test
    public void testFooNoInterceptors(@Produced("empty") Foo foo) {
        // Note that the produced Foo is @Dependent
        assertEquals(Foo.class, foo.getClass());
        assertEquals("pong", foo.ping());
    }

    @Test
    public void testBarNoInterceptors(@Produced Bar bar) {
        assertEquals("ping", bar.pong());
    }

    @Test
    public void testMapPut(@Produced Map<String, Object> map) {
        Producer.reset();
        String[] params = new String[] { "foo", "bar" };
        map.put(params[0], params[1]);
        assertEquals(1, Producer.INVOCATIONS.size());
        assertEquals(Arrays.toString(params), Producer.INVOCATIONS.get(0));
    }

    @Test
    public void testListAdd(@Produced List<Object> list) {
        Producer.reset();
        list.add("foo");
        list.size(); // this won't trigger interception
        assertEquals(1, Producer.INVOCATIONS.size());
    }

    @Test
    public void testParent(@Produced Producer.FooParent foo) {
        assertEquals("Hello Parent pong", foo.ping());
    }

    @Test
    public void testAbstractBar(@Produced Producer.AbstractBar bar) {
        assertEquals("Hello BarImpl pong", bar.ping());
    }

    @Test
    public void testFactoryFromInterface(@Produced SomeInterface bean) {
        Producer.reset();
        assertEquals("Hello " + SomeImpl.class.getSimpleName(), bean.ping(2.50, "foo"));
        assertEquals(SomeImpl.class.getSimpleName(), bean.pong());
        assertEquals(2, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryFromInterfacewithDefaultMethod(@Produced InterfaceWithDefaultMethod bean) {
        Producer.reset();
        assertEquals("Hello " + InterfaceWithDefaultMethod.class.getSimpleName(), bean.ping());
        assertEquals(ImplOfInterfaceWithDefaultMethod.class.getSimpleName(), bean.pong());
        assertEquals(2, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryFromGenericInterface(@Produced SomeGenericInterface<List<String>> bean) {
        Producer.reset();
        ArrayList<String> testList = new ArrayList<String>();
        testList.add("foo");
        assertEquals("Hello " + SomeGenericImpl.class.getSimpleName(), bean.ping(testList, 2.5));
        assertEquals(SomeGenericImpl.class.getSimpleName(), bean.ping(testList, "bar"));
        assertEquals(2, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryBackedByUnproxyableImpl(@Produced ProxyableInterface bean) {
        // this test adds class level Monitor and method level Hello bindings
        Producer.reset();
        assertEquals("Hello " + UnproxyableImpl.class.getSimpleName(), bean.ping());
        assertEquals(1, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryFromInterfaceWithMethodAnnotation(@Produced ProxyableInterfaceWithMethodAnnotation bean) {
        // this test adds class level Monitor and method level Hello bindings
        Producer.reset();
        assertEquals("Hello " + UnproxyableInterfaceWithMethodAnnotationImpl.class.getSimpleName(), bean.ping());
        assertEquals(1, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryFromInterfaceWithClassAnnotation(@Produced ProxyableInterfaceWithClassAnnotation bean) {
        // this test adds class level Monitor and method level Hello bindings
        Producer.reset();
        assertEquals("Hello " + UnproxyableInterfaceWithClassAnnotationImpl.class.getSimpleName(), bean.ping());
        assertEquals(1, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryFromGenericInterfaceStructure(@Produced InterfaceWithGenericsB<String, Integer> bean) {
        Producer.reset();
        bean.ping(); // this doesn't trigger anything as we don't inherit annotations ATM
        assertEquals("Hello " + UnproxyableInterfaceWithGenericsChainImpl.class.getSimpleName(), bean.pong());
        assertEquals(1, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryWhenInterfaceHasEqualAnnotationsToThoseWeAreAdding(@Produced InterfaceWithAnnotation bean) {
        // Currently, we do not verify this shouldn't happen, checking it would require comparison between
        // added annotation and the AT we create from interface
        // hence this test should just "silently succeed"
        Producer.reset();
        assertEquals("Hello " + InterfaceWithAnnotationImpl.class.getSimpleName(), bean.ping());
        bean.pong();
        assertEquals(1, Producer.INVOCATIONS.size());
    }

    @Test
    public void testFactoryFromNonGenericInterfaceChain(@Produced InterfaceB bean) {
        Producer.reset();
        assertEquals("Hello pingB", bean.pingB());
        assertEquals("pingA", bean.pingA());
        assertEquals(1, Producer.INVOCATIONS.size());
    }
}
