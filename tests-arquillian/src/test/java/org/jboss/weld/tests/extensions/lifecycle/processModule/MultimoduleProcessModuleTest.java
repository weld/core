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
package org.jboss.weld.tests.extensions.lifecycle.processModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.extensions.lifecycle.processModule.MultimoduleProcessingExtension.ProcessModuleHolder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class MultimoduleProcessModuleTest {

    @Deployment
    public static WebArchive getDeployment() {
        JavaArchive nonBda = ShrinkWrap.create(JavaArchive.class).addClasses(Elephant.class, ElephantExtension.class)
                .addAsServiceProvider(Extension.class, ElephantExtension.class);
        JavaArchive bda = ShrinkWrap
                .create(BeanArchive.class)
                .alternate(Tiger.class)
                .decorate(Decorator1.class)
                .intercept(Interceptor1.class)
                .addClasses(Animal.class, Decorator1.class, Interceptor1.class, Tiger.class, Binding.class,
                        MultimoduleProcessingExtension.class)
                .addAsServiceProvider(Extension.class, MultimoduleProcessingExtension.class);
        WebArchive war = ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(Lion.class).addAsLibraries(nonBda, bda);
        return war;
    }

    @Test
    public void test(MultimoduleProcessingExtension extension) {
        assertEquals(2, extension.getEvents().size());
        List<ProcessModuleHolder> events = new ArrayList<ProcessModuleHolder>(extension.getEvents());
        for (Iterator<ProcessModuleHolder> i = events.iterator(); i.hasNext(); ) {
            ProcessModuleHolder event = i.next();
            if (event.getClasses().contains(Tiger.class)) {
                assertEquals(1, event.getAlternatives().size());
                assertEquals(Tiger.class, event.getAlternatives().iterator().next());
                assertEquals(1, event.getDecorators().size());
                assertEquals(Decorator1.class, event.getDecorators().get(0));
                assertEquals(1, event.getInterceptors().size());
                assertEquals(Interceptor1.class, event.getInterceptors().get(0));
                assertTrue(event.getClasses().contains(Animal.class));
                assertTrue(event.getClasses().contains(Decorator1.class));
                assertTrue(event.getClasses().contains(Interceptor1.class));
                assertTrue(event.getClasses().contains(Tiger.class));
                assertTrue(event.getClasses().contains(MultimoduleProcessingExtension.class));
                assertFalse(event.getClasses().contains(Elephant.class));
                assertFalse(event.getClasses().contains(ElephantExtension.class));
                assertFalse(event.getClasses().contains(Lion.class));
                i.remove();
            } else if (event.getClasses().contains(Lion.class)) {
                assertTrue(event.getAlternatives().isEmpty());
                assertTrue(event.getInterceptors().isEmpty());
                assertTrue(event.getDecorators().isEmpty());
                assertFalse(event.getClasses().contains(Tiger.class));
                assertFalse(event.getClasses().contains(MultimoduleProcessingExtension.class));
                assertFalse(event.getClasses().contains(ElephantExtension.class));
                i.remove();
            }
        }
        assertEquals(0, events.size());
    }
}
