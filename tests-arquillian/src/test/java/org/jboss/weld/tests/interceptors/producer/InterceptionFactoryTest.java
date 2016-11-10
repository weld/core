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

import java.util.Arrays;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.interceptors.producer.Producer.Foo;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
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
    public void testMapPut(@Produced Map<String, Object> map) {
        Producer.reset();
        String[] params = new String[] { "foo", "bar" };
        map.put(params[0], params[1]);
        assertEquals(1, Producer.INVOCATIONS.size());
        assertEquals(Arrays.toString(params), Producer.INVOCATIONS.get(0));
    }
}
