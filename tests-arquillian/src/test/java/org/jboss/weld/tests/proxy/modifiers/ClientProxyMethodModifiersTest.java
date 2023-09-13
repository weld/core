/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.modifiers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1626
 */
@RunWith(Arquillian.class)
public class ClientProxyMethodModifiersTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ClientProxyMethodModifiersTest.class))
                .addPackage(ClientProxyMethodModifiersTest.class.getPackage());
    }

    @Test
    public void testMethodModifiers(NormalScopedBean proxy) {

        Method[] methods = proxy.getClass().getDeclaredMethods();

        Method foo = find("foo", methods);
        assertNotNull(foo);
        assertTrue(Modifier.isPublic(foo.getModifiers()));

        Method bar = find("bar", methods);
        assertNotNull(bar);
        assertTrue(Modifier.isProtected(bar.getModifiers()));

        Method baz = find("baz", methods);
        assertNull(baz);
    }

    private Method find(String name, Method[] methods) {
        for (Method method : methods) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

}
