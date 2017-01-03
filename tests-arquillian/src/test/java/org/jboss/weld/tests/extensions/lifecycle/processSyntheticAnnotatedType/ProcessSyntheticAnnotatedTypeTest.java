/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.lifecycle.processSyntheticAnnotatedType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProcessSyntheticAnnotatedTypeTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProcessSyntheticAnnotatedTypeTest.class))
                .addPackage(Juicy.class.getPackage())
                .addAsServiceProvider(Extension.class, RegisteringExtension1.class, RegisteringExtension2.class,
                        ModifyingExtension.class, VerifyingExtension.class);
    }

    @Test
    public void testEventsFired() {
        Set<Class<?>> patClasses = extension.getPatClasses();
        Set<Class<?>> psatClasses = extension.getPsatClasses();
        assertTrue(psatClasses.contains(Orange.class));
        assertTrue(psatClasses.contains(Apple.class));
        assertTrue(psatClasses.contains(Pear.class));
        // also verify that PAT is fired for classes in a BDA
        assertTrue(patClasses.contains(Orange.class));
        assertTrue(patClasses.contains(Apple.class));
        assertTrue(patClasses.contains(Pear.class));
    }

    @Test
    public void testSource() {
        Map<Class<?>, Extension> sources = extension.getSources();
        assertTrue(sources.get(Apple.class) instanceof RegisteringExtension1);
        assertTrue(sources.get(Orange.class) instanceof RegisteringExtension1);
        assertTrue(sources.get(Pear.class) instanceof RegisteringExtension2);
    }

    @Test
    public void testChangesApplied(BeanManager manager) {
        Set<Bean<?>> oranges = manager.getBeans(Orange.class, Any.Literal.INSTANCE);
        assertEquals(1, oranges.size());
        assertFalse(oranges.iterator().next().getQualifiers().contains(Juicy.Literal.INSTANCE));

        Set<Bean<?>> apples = manager.getBeans(Apple.class, Any.Literal.INSTANCE);
        assertEquals(2, apples.size());
        Set<Bean<?>> juicyApples = manager.getBeans(Apple.class, Juicy.Literal.INSTANCE);
        assertEquals(1, juicyApples.size());
        assertTrue(juicyApples.iterator().next().getQualifiers().contains(Fresh.Literal.INSTANCE));

        assertEquals(2, manager.getBeans(Pear.class, Any.Literal.INSTANCE).size());
        Set<Bean<?>> juicyPears = manager.getBeans(Pear.class, Juicy.Literal.INSTANCE);
        assertEquals(1, juicyPears.size());
    }
}
