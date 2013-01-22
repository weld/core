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
}
