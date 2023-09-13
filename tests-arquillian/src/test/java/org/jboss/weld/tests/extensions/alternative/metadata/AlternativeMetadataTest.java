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
package org.jboss.weld.tests.extensions.alternative.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1675
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class AlternativeMetadataTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AlternativeMetadataTest.class))
                .addAsServiceProvider(Extension.class, ModifyingExtension.class)
                .addPackage(Alpha.class.getPackage()).addClass(ForwardingAnnotatedType.class);
    }

    @SuppressWarnings("serial")
    @Test
    public void testGetTypeClosure(BeanManager beanManager) {
        assertEquals(0, beanManager.getBeans(Alpha.class).size());
        assertEquals(0, beanManager.getBeans(DeltaInterface.class).size());
        Set<Bean<?>> beans = beanManager.getBeans(AlphaInterface.class);
        assertEquals(1, beans.size());
        Set<Type> types = beans.iterator().next().getTypes();
        // Object, AlphaInterface
        assertEquals(2, types.size());
        assertTrue(types.contains(AlphaInterface.class));
        assertTrue(types.contains(Object.class));

        beans = beanManager.getBeans(Bravo.class);
        assertEquals(1, beans.size());
        types = beans.iterator().next().getTypes();
        // Object, Bravo, Charlie
        assertEquals(3, types.size());
        assertTrue(types.contains(Bravo.class));
        assertTrue(types.contains(Charlie.class));
        assertTrue(types.contains(Object.class));

        assertEquals(0, beanManager.getBeans(Echo.class).size());
        assertEquals(0, beanManager.getBeans(new TypeLiteral<EchoInterface<Integer>>() {
        }.getType()).size());
        beans = beanManager.getBeans(new TypeLiteral<FoxtrotInterface<Integer>>() {
        }.getType());
        assertEquals(1, beans.size());
        types = beans.iterator().next().getTypes();
        // Object, FoxtrotInterface
        assertEquals(2, types.size());
        assertTrue(types.contains(new TypeLiteral<FoxtrotInterface<Integer>>() {
        }.getType()));
        assertTrue(types.contains(Object.class));
    }
}
