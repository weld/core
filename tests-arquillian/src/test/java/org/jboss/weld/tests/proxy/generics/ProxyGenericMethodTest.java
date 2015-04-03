/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.proxy.generics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.tests.category.EmbeddedContainer;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@Category(EmbeddedContainer.class)
@RunWith(Arquillian.class)
public class ProxyGenericMethodTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(ProxyGenericMethodTest.class.getPackage());
    }

    @Inject
    SimpleNormalScoped simple;

    @Inject
    Child child;

    @Inject
    InterceptedDependent interceptedDependent;

    @Test
    public void testClientProxy() throws Exception {
        assertNotNull(simple);
        assertTrue(simple instanceof ProxyObject);
        assertMethods(simple.getClass());
    }

    @Test
    public void testClientProxySyntheticMethod() throws Exception {
        assertNotNull(child);
        assertTrue(child instanceof ProxyObject);
        child.invoke("foo");
        assertMethods(child.getClass());
    }

    // We can't add generic signatures on subclasses because of JDK-8062582 and others
    // @Test
    // public void testSubclass() throws Exception {
    // assertNotNull(interceptedDependent);
    // assertTrue(interceptedDependent instanceof ProxyObject);
    // assertMethods(interceptedDependent.getClass());
    // }

    private void assertMethods(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        Method m = clazz.getMethod("getCopy", List.class);
        assertNotNull(m);
        Type[] paramTypes = m.getGenericParameterTypes();
        assertEquals(1, paramTypes.length);
        assertTrue(paramTypes[0] instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) paramTypes[0];
        assertEquals(List.class, paramType.getRawType());
        Type[] typeArguments = paramType.getActualTypeArguments();
        assertEquals(1, typeArguments.length);
        assertTrue(typeArguments[0].equals(String.class));

        m = clazz.getMethod("getCopy", Set.class);
        assertNotNull(m);
        paramTypes = m.getGenericParameterTypes();
        assertEquals(1, paramTypes.length);
        assertTrue(paramTypes[0] instanceof ParameterizedType);
        paramType = (ParameterizedType) paramTypes[0];
        assertEquals(Set.class, paramType.getRawType());
        typeArguments = paramType.getActualTypeArguments();
        assertEquals(1, typeArguments.length);
        assertTrue(typeArguments[0] instanceof TypeVariable);
        TypeVariable<?> typeVariable = (TypeVariable<?>) typeArguments[0];
        assertEquals("T", typeVariable.getName());
    }

}
