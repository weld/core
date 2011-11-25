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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProcessModuleTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class)
                .alternate(Lion.class)
                .decorate(Decorator1.class, Decorator2.class)
                .intercept(Interceptor1.class, Interceptor2.class)
                .addClasses(Animal.class, AnotherExtension.class, Binding.class, Decorator1.class, Decorator2.class,
                        Decorator3.class, Interceptor1.class, Interceptor2.class, Interceptor3.class, Lion.class,
                        ModuleProcessingExtension.class, Tiger.class)
                .addAsServiceProvider(Extension.class, ModuleProcessingExtension.class, AnotherExtension.class);
    }

    @Test
    public void testEventNotificationCount(ModuleProcessingExtension extension) {
        assertEquals(1, extension.getModuleCount());
    }

    @Test
    public void testAnnotatedTypes(ModuleProcessingExtension extension) {
        List<AnnotatedType<?>> types = extension.getAnnotatedTypes();
        assertContainsAll(types, Animal.class, Decorator1.class, Interceptor1.class, Lion.class,
                ModuleProcessingExtension.class, Tiger.class);
    }

    @Test
    public void testAlternatives() {
        Bean<?> bean = manager.resolve(manager.getBeans(Animal.class));
        assertEquals(Tiger.class, bean.getBeanClass());
    }

    @Test
    public void testDecorators() {
        List<Decorator<?>> decorators = manager.resolveDecorators(Collections.<Type> singleton(Animal.class));
        assertEquals(2, decorators.size());
        assertEquals(Decorator1.class, decorators.get(0).getBeanClass());
        assertEquals(Decorator3.class, decorators.get(1).getBeanClass());
    }

    @Test
    public void testInterceptors() {
        List<Interceptor<?>> interceptors = manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new Binding.Literal());
        assertEquals(2, interceptors.size());
        assertEquals(Interceptor3.class, interceptors.get(0).getBeanClass());
        assertEquals(Interceptor1.class, interceptors.get(1).getBeanClass());
    }

    private void assertContainsAll(Collection<AnnotatedType<?>> annotatedTypes, Class<?>... types) {
        Set<Class<?>> typeSet = new HashSet<Class<?>>(Arrays.asList(types));
        for (AnnotatedType<?> item : annotatedTypes) {
            typeSet.remove(item.getJavaClass());
        }
        if (!typeSet.isEmpty()) {
            throw new IllegalStateException("The following types are not contained: " + typeSet);
        }
    }
}
