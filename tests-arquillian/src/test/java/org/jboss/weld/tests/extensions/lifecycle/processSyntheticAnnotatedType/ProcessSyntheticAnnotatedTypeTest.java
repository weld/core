/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.extensions.lifecycle.processSyntheticAnnotatedType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.AnyLiteral;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProcessSyntheticAnnotatedTypeTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class)
                .addPackage(Juicy.class.getPackage())
                .addAsServiceProvider(Extension.class, RegisteringExtension1.class, RegisteringExtension2.class,
                        ModifyingExtension.class, VerifyingExtension.class);
    }

    @Test
    public void testEventsFired() {
        Set<Class<?>> patClasses = extension.getPatClasses();
        Set<Class<?>> psatClasses = extension.getPsatClasses();
        assertEquals(3, psatClasses.size());
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
        Set<Bean<?>> oranges = manager.getBeans(Orange.class, AnyLiteral.INSTANCE);
        assertEquals(1, oranges.size());
        assertFalse(oranges.iterator().next().getQualifiers().contains(Juicy.Literal.INSTANCE));

        Set<Bean<?>> apples = manager.getBeans(Apple.class, AnyLiteral.INSTANCE);
        assertEquals(2, apples.size());
        Set<Bean<?>> juicyApples = manager.getBeans(Apple.class, Juicy.Literal.INSTANCE);
        assertEquals(1, juicyApples.size());
        assertTrue(juicyApples.iterator().next().getQualifiers().contains(Fresh.Literal.INSTANCE));

        assertEquals(2, manager.getBeans(Pear.class, AnyLiteral.INSTANCE).size());
        Set<Bean<?>> juicyPears = manager.getBeans(Pear.class, Juicy.Literal.INSTANCE);
        assertEquals(1, juicyPears.size());
    }
}
